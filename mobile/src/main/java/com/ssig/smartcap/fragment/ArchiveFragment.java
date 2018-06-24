package com.ssig.smartcap.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.ssig.sensorsmanager.SensorType;
import com.ssig.sensorsmanager.data.CaptureData;
import com.ssig.sensorsmanager.data.DeviceData;
import com.ssig.sensorsmanager.data.SensorData;
import com.ssig.sensorsmanager.util.JSONUtil;
import com.ssig.smartcap.R;
import com.ssig.smartcap.adapter.AdapterCaptureList;
import com.ssig.smartcap.model.CaptureListItem;
import com.ssig.smartcap.utils.Tools;
import com.ssig.smartcap.widget.EmptyRecyclerView;
import com.ssig.smartcap.widget.LineItemDecoration;

import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ArchiveFragment extends AbstractMainFragment implements
        DataClient.OnDataChangedListener,
        MessageClient.OnMessageReceivedListener {

    public enum ReceiveSmartwatchFilesTaskResponseType{
        FOLDER_FAILURE, ASSET_FAILURE,
        IO_FAILURE, EXECUTION_FAILURE,
        TIME_OUT_FAILURE, SUCCESS
    }

    private SwipeRefreshLayout swipeRefreshLayout;
    private AppCompatDelegate appCompatDelegate;
    private AdapterCaptureList adapterCaptureList;
    private ActionMode actionMode;
    private ActionModeCallback actionModeCallback;

    ReceiveSmartwatchFilesTask receiveSmartwatchFilesTask;

    public ArchiveFragment(){
        super(R.layout.fragment_archive);
    }

    //----------------------------------------------------------------------------------------------
    // Override Functions
    //----------------------------------------------------------------------------------------------
    @Override
    public void refresh() {
        this.adapterCaptureList.replaceItemsList(this.listCaptureItems(this.listCaptureFiles()));
        this.adapterCaptureList.notifyDataSetChanged();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        this.appCompatDelegate = AppCompatDelegate.create(Objects.requireNonNull(getActivity()), (AppCompatCallback) getActivity());

        this.actionMode = null;
        this.receiveSmartwatchFilesTask = null;
        this.initUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        this.refresh();
        final String uri = String.format("wear://*%s", getString(R.string.message_path_host_archive_fragment_prefix));
        Wearable.getDataClient(Objects.requireNonNull(this.getContext())).addListener(this, Uri.parse(uri), DataClient.FILTER_PREFIX);
        Wearable.getMessageClient(Objects.requireNonNull(this.getContext())).addListener(this, Uri.parse(uri), MessageClient.FILTER_PREFIX);
    }

    @Override
    public void onStop() {
        Wearable.getDataClient(Objects.requireNonNull(this.getContext())).removeListener(this);
        Wearable.getMessageClient(Objects.requireNonNull(this.getContext())).removeListener(this);
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    //----------------------------------------------------------------------------------------------
    // UI STUFFS
    //----------------------------------------------------------------------------------------------
    private void initUI() {

        this.swipeRefreshLayout = Objects.requireNonNull(this.getView()).findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullAndRefresh();
            }
        });

        EmptyRecyclerView recyclerViewCaptures = Objects.requireNonNull(this.getView()).findViewById(R.id.captures_recycler_view);
        recyclerViewCaptures.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerViewCaptures.addItemDecoration(new LineItemDecoration(Objects.requireNonNull(this.getContext()), LinearLayout.VERTICAL));
        recyclerViewCaptures.setHasFixedSize(true);
        recyclerViewCaptures.setItemAnimator(new DefaultItemAnimator());
        recyclerViewCaptures.setEmptyView(this.getView().findViewById(R.id.layout_no_capture));

        this.adapterCaptureList = new AdapterCaptureList(getContext(), this.listCaptureItems(this.listCaptureFiles()));
        recyclerViewCaptures.setAdapter(this.adapterCaptureList);

        this.adapterCaptureList.setOnClickListener(new AdapterCaptureList.OnClickListener() {
            @Override
            public void onItemClick(View view, CaptureListItem obj, int pos) {
                if (adapterCaptureList.getSelectedItemCount() > 0) {
                    enableActionMode(pos);
                }
            }

            @Override
            public void onItemLongClick(View view, CaptureListItem obj, int pos) {
                enableActionMode(pos);
            }
        });

        this.adapterCaptureList.setOnCloseButtonClickListener(new AdapterCaptureList.OnCloseButtonClickListener() {
            @Override
            public void onItemClick(View view, final CaptureListItem obj, int pos) {
                new MaterialDialog.Builder(getContext())
                        .title(R.string.archive_close_dialog_confirm_title)
                        .content(R.string.archive_close_dialog_confirm_content)
                        .icon(Tools.changeDrawableColor(Objects.requireNonNull(getContext().getDrawable(R.drawable.ic_package_down)), ContextCompat.getColor(getContext(), R.color.colorPrimary)))
                        .cancelable(true)
                        .canceledOnTouchOutside(true)
                        .positiveText(R.string.button_yes)
                        .negativeText(R.string.button_no)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                checkCloseCaptureData(obj);
                            }
                        })
                        .show();
            }
        });

        this.actionModeCallback = new ActionModeCallback();
    }

    private void pullAndRefresh() {
        swipeProgress(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh();
                swipeProgress(false);
            }
        }, 500);
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    private void enableActionMode(int position) {
        if (this.actionMode == null) {
            this.actionMode = this.appCompatDelegate.startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    @SuppressLint("DefaultLocale")
    private void toggleSelection(int position) {
        adapterCaptureList.toggleSelection(position);
        int count = adapterCaptureList.getSelectedItemCount();
        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.format("%d selected", count));
            actionMode.invalidate();
        }
    }

    //----------------------------------------------------------------------------------------------
    // Capture Items STUFFS
    //----------------------------------------------------------------------------------------------
    public List<File> listCaptureFiles() {
        List<File> listedFiles = new LinkedList<>();
        if (this.getSystemArchiveFolder().exists()) {
            File[] files = this.getSystemArchiveFolder().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".json");
                }
            });
            if(files != null)
                listedFiles = Arrays.asList(files);
        }
        return listedFiles;
    }

    private List<CaptureListItem> listCaptureItems(List<File> captureFiles){
        List<CaptureListItem> captureListItems = new LinkedList<>();
        for(File file: captureFiles){
            try {
                CaptureData captureData = JSONUtil.load(file, CaptureData.class);
                CaptureListItem captureListItem = new CaptureListItem(captureData);
                captureListItems.add(captureListItem);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        return captureListItems;
    }

    public void addCaptureData(CaptureData captureData){
        if (this.adapterCaptureList != null){
            this.adapterCaptureList.addItem(new CaptureListItem(captureData));
            this.adapterCaptureList.notifyDataSetChanged();
        }
    }

    //----------------------------------------------------------------------------------------------
    // Actions STUFFS
    //----------------------------------------------------------------------------------------------
    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Tools.setSystemBarColor(Objects.requireNonNull(getActivity()), R.color.colorGreyLight);
            Tools.setSystemBarLight(getActivity());
            mode.getMenuInflater().inflate(R.menu.menu_archive, menu);
            Tools.changeMenuIconColor(menu, ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorGreyLight));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            switch (id){
                case R.id.action_delete:
                    deleteFiles(mode);
                    return true;
                case R.id.action_share:
                    shareFiles(mode);
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapterCaptureList.clearSelections();
            Tools.setSystemBarColor(Objects.requireNonNull(getActivity()), R.color.colorGreyLight);
            Tools.setSystemBarLight(getActivity());
            actionMode = null;
        }
    }

    private void shareFiles(final ActionMode mode) {
        List<Integer> selectedItemPositions = this.adapterCaptureList.getSelectedItems();

        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            CaptureListItem item;
            item = this.adapterCaptureList.getItem(selectedItemPositions.get(i));
            if (!item.closed){
                new MaterialDialog.Builder(Objects.requireNonNull(this.getContext()))
                        .title(R.string.archive_share_dialog_error_title)
                        .titleColorRes(R.color.colorAlert)
                        .content(R.string.archive_share_dialog_error_content)
                        .icon(Tools.changeDrawableColor(Objects.requireNonNull(this.getContext().getDrawable(R.drawable.ic_package_open)), ContextCompat.getColor(this.getContext(), R.color.colorAlert)))
                        .cancelable(true)
                        .positiveText(R.string.button_ok)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return;
            }
        }

        ArrayList<Uri> uri_files = new ArrayList<>();
        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND_MULTIPLE);
        share.setType("application/json");
        share.putExtra(Intent.EXTRA_SUBJECT, "SmartCap Capture Data");
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            CaptureListItem item;
            item = this.adapterCaptureList.getItem(selectedItemPositions.get(i));

            File captureDataFile = new File(String.format("%s%s%s.zip", this.getSystemArchiveFolder(), File.separator, item.captureUUID));
            Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(this.getContext()), this.getContext().getApplicationContext().getPackageName() + ".provider", captureDataFile);
            uri_files.add(uri);

        }
        share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uri_files);
        getContext().startActivity(Intent.createChooser(share, "Send files..."));
        mode.finish();
    }

    private void deleteFiles(final ActionMode mode) {
        new MaterialDialog.Builder(Objects.requireNonNull(getActivity()))
                .title(R.string.archive_delete_dialog_title)
                .titleColorRes(R.color.colorAlert)
                .content(R.string.archive_delete_dialog_content)
                .icon(Tools.changeDrawableColor(Objects.requireNonNull(getActivity().getDrawable(R.drawable.ic_delete)), ContextCompat.getColor(getActivity(), R.color.colorAlert)))
                .cancelable(true)
                .positiveText(R.string.button_yes)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        List<Integer> selectedItemPositions = adapterCaptureList.getSelectedItems();
                        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                            CaptureListItem item = adapterCaptureList.getItem(selectedItemPositions.get(i));

                            File captureDataFile = new File(String.format("%s%s%s.json", getSystemArchiveFolder(), File.separator, item.captureUUID));
                            if(item.closed){
                                File captureDataCompressedFile = new File(String.format("%s%s%s.zip", getSystemArchiveFolder(), File.separator, item.captureUUID));
                                if(captureDataCompressedFile.delete() && captureDataFile.delete())
                                    adapterCaptureList.removeData(selectedItemPositions.get(i));
                            }else{
                                if(captureDataFile.delete())
                                    adapterCaptureList.removeData(selectedItemPositions.get(i));
                            }
                        }
                        adapterCaptureList.notifyDataSetChanged();
                        mode.finish();
                    }
                })
                .negativeText(R.string.button_no)
                .show();
    }

    //----------------------------------------------------------------------------------------------
    // Close Capture Stuffs
    //----------------------------------------------------------------------------------------------
    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        String path = messageEvent.getPath();

        if (path.equals(getString(R.string.message_path_host_archive_fragment_sensor_files_error))) {
            byte[] data = messageEvent.getData();
            String errorMessge = new String(data);

        }
    }


    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(getString(R.string.message_path_host_archive_fragment_sensor_files_sent))) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    DataMap dataMap = dataMapItem.getDataMap();
                    if (this.receiveSmartwatchFilesTask != null)
                        this.receiveSmartwatchFilesTask.execute(dataMap);
                }
            }
        }
    }

    private void checkCloseCaptureData(CaptureListItem captureListItem){

        this.receiveSmartwatchFilesTask = null;

        File captureDataFile = new File(String.format("%s%s%s.json", getSystemArchiveFolder(), File.separator, captureListItem.captureUUID));
        CaptureData captureData = null;
        try {
            captureData = JSONUtil.load(captureDataFile, CaptureData.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        DeviceData clientDeviceData = Objects.requireNonNull(captureData).getClientDeviceData();
        try{
            if (clientDeviceData != null){
                if (this.isWearClientConnected() && this.getWearService().getClientDeviceInfo().getDeviceKey().equals(clientDeviceData.getDeviceKey())){
                    this.receiveSmartwatchFilesTask = new ReceiveSmartwatchFilesTask(captureData);
                    this.receiveSmartwatchFilesTask.init();
                }else{
                    new MaterialDialog.Builder(Objects.requireNonNull(this.getContext()))
                            .title(R.string.archive_close_dialog_no_smartwatch_error_title)
                            .titleColorRes(R.color.colorAlert)
                            .content(R.string.archive_close_dialog_no_smartwatch_error_content)
                            .icon(Tools.changeDrawableColor(Objects.requireNonNull(this.getContext().getDrawable(R.drawable.ic_smartwatch_off)), ContextCompat.getColor(this.getContext(), R.color.colorAlert)))
                            .cancelable(true)
                            .positiveText(R.string.button_ok)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            } else {
                new CloseCaptureTask().execute(captureData);
            }
        } catch (ApiException e) {
            e.printStackTrace();
            Toast.makeText(this.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }


    }

    @SuppressLint("StaticFieldLeak")
    private class ReceiveSmartwatchFilesTask extends AsyncTask<DataMap, Void, ReceiveSmartwatchFilesTaskResponseType> {

        MaterialDialog dialogWaitingSmartwatchFiles;
        CaptureData captureData;
        File deviceCaptureFolder;

        ReceiveSmartwatchFilesTask(CaptureData captureData){
            this.dialogWaitingSmartwatchFiles = null;
            this.captureData = captureData;
            this.deviceCaptureFolder = null;
        }

        void init() throws ApiException {
            this.dialogWaitingSmartwatchFiles = new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
                    .title(R.string.archive_dialog_wear_sensors_title)
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(getContext().getDrawable(R.drawable.ic_smartwatch_on)), ContextCompat.getColor(getContext(), R.color.colorPrimary)))
                    .content(R.string.archive_dialog_wear_sensors_content_waiting)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
            getWearService().requestSensorFiles(this.captureData.getClientDeviceData());
        }

        boolean configureDeviceCaptureFolder() {
            this.deviceCaptureFolder = new File(String.format("%s%s%s%s%s", getSystemCapturesFolder(), File.separator, captureData.getCaptureDataUUID(), File.separator, captureData.getClientDeviceData().getDeviceDataUUID()));
            return deviceCaptureFolder.exists() || deviceCaptureFolder.mkdirs();
        }


        MaterialDialog makeResponseErrorDialog(ReceiveSmartwatchFilesTaskResponseType response){

            MaterialDialog responseErrorDialog = new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
                    .title(R.string.archive_dialog_wear_sensors_error_title)
                    .titleColorRes(R.color.colorAlert)
                    .content("")
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(getContext().getDrawable(R.drawable.ic_package_open)), ContextCompat.getColor(getContext(), R.color.colorAlert)))
                    .cancelable(true)
                    .positiveText(R.string.button_ok)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .build();

            switch (response){

                case FOLDER_FAILURE:
                    responseErrorDialog.setContent(R.string.archive_dialog_wear_sensors_error_folder);
                    break;

                case ASSET_FAILURE:
                    responseErrorDialog.setContent(R.string.archive_dialog_wear_sensors_error_asset);
                    break;

                case IO_FAILURE:
                    responseErrorDialog.setContent(R.string.archive_dialog_wear_sensors_error_io);
                    break;

                case EXECUTION_FAILURE:
                    responseErrorDialog.setContent(R.string.archive_dialog_wear_sensors_error_execution);
                    break;

                case TIME_OUT_FAILURE:
                    responseErrorDialog.setContent(R.string.archive_dialog_wear_sensors_error_timeout);
                    break;

            }
            return responseErrorDialog;
        }

        @Override
        protected ReceiveSmartwatchFilesTaskResponseType doInBackground(DataMap... dataMaps) {

            if(!configureDeviceCaptureFolder())
                return ReceiveSmartwatchFilesTaskResponseType.FOLDER_FAILURE;

            try {
                DataMap dataMap = dataMaps[0];

                int nFiles = this.captureData.getClientDeviceData().getSensorsData().size();
                int elapsedProgress = 0;
                for (final Map.Entry<SensorType, SensorData> entry : this.captureData.getClientDeviceData().getSensorsData().entrySet()){

                    if (entry.getValue().isEnable()) {

                        Asset asset = dataMap.getAsset(entry.getKey().code());
                        if (asset == null)
                            return ReceiveSmartwatchFilesTaskResponseType.ASSET_FAILURE;

                        File sensorFile = new File(String.format("%s%s%s.dat", this.deviceCaptureFolder, File.separator, entry.getValue().getSensorDataUUID()));
                        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialogWaitingSmartwatchFiles.setContent(String.format("%s: %s", getString(R.string.archive_dialog_wear_sensors_content_prefix), entry.getKey().toString()));
                            }
                        });
                        if(!sensorFile.exists()) {

                            Task<DataClient.GetFdForAssetResponse> getFdForAssetResponseTask = Wearable.getDataClient(Objects.requireNonNull(getContext())).getFdForAsset(asset);
                            DataClient.GetFdForAssetResponse getFdForAssetResponse = Tasks.await(getFdForAssetResponseTask, 20, TimeUnit.SECONDS);
                            InputStream assetInputStream = getFdForAssetResponse.getInputStream();
//                            int totalEstimatedBytes = assetInputStream.available();

                            FileOutputStream fileOutputStream = new FileOutputStream(sensorFile);

                            byte[] bytes = new byte[1024];
                            int lengthReadBytes;
//                            int currentReadBytes = 0;
                            while ((lengthReadBytes = assetInputStream.read(bytes)) >= 0) {
                                fileOutputStream.write(bytes, 0, lengthReadBytes);
//                                currentReadBytes += lengthReadBytes;
//                                float fileProgress = (int) ((float) currentReadBytes / (float) totalEstimatedBytes * (100.0f / (float) nFiles));
//                                publishProgress((int) (elapsedProgress + fileProgress));
                            }
                            fileOutputStream.close();
                            assetInputStream.close();

                        }
//                        elapsedProgress += (int) (100.0f / (float) nFiles);
//                        publishProgress((int) elapsedProgress);

                    }
                }
//                publishProgress(100);
                return ReceiveSmartwatchFilesTaskResponseType.SUCCESS;

            } catch (IOException e) {
                e.printStackTrace();
                return ReceiveSmartwatchFilesTaskResponseType.IO_FAILURE;
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                return ReceiveSmartwatchFilesTaskResponseType.EXECUTION_FAILURE;
            } catch (TimeoutException e) {
                e.printStackTrace();
                return ReceiveSmartwatchFilesTaskResponseType.TIME_OUT_FAILURE;
            }

        }

//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            super.onProgressUpdate(values);
//            dialogWaitingSmartwatchFiles.setProgress(values[0]);
//        }

        @Override
        protected void onPostExecute(ReceiveSmartwatchFilesTaskResponseType response) {
            super.onPostExecute(response);
            this.dialogWaitingSmartwatchFiles.dismiss();

            if (response == ReceiveSmartwatchFilesTaskResponseType.SUCCESS) {
                Toast.makeText(getContext(), R.string.archive_toast_smartwatch_files_received, Toast.LENGTH_LONG).show();
                new CloseCaptureTask().execute(this.captureData);
            } else {
                MaterialDialog responseErrorDialog = this.makeResponseErrorDialog(response);
                responseErrorDialog.show();
            }

        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CloseCaptureTask extends AsyncTask<CaptureData, Void, Boolean> {

        MaterialDialog dialogWaitingCaptureClose;

        CloseCaptureTask(){
            this.dialogWaitingCaptureClose = null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialogWaitingCaptureClose = new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
                    .title(R.string.archive_dialog_close_capture_title)
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(getContext().getDrawable(R.drawable.ic_package_close)), ContextCompat.getColor(getContext(), R.color.colorPrimary)))
                    .content(R.string.archive_dialog_close_capture_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected Boolean doInBackground(CaptureData... values) {

            CaptureData captureData = values[0];

            File captureFolder = new File(String.format("%s%s%s", getSystemCapturesFolder(), File.separator, captureData.getCaptureDataUUID()));
            File compressedCaptureFile = new File(String.format("%s%s%s.zip", getSystemArchiveFolder(), File.separator, captureData.getCaptureDataUUID()));
            File captureDataFile = new File(String.format("%s%s%s.json", getSystemArchiveFolder(), File.separator, captureData.getCaptureDataUUID()));


            captureData.setClosed(true);
            try {
                JSONUtil.save(captureData, captureDataFile);
                ZipUtil.pack(captureFolder, compressedCaptureFile);
                ZipUtil.addEntry(compressedCaptureFile, "capture.json", captureDataFile);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean response) {
            super.onPostExecute(response);
            this.dialogWaitingCaptureClose.dismiss();
            if (response) {
                refresh();
                Toast.makeText(getContext(), R.string.archive_toast_closed_success, Toast.LENGTH_LONG).show();
            }
        }

    }


}

