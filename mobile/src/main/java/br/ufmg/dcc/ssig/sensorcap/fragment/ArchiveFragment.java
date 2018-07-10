package br.ufmg.dcc.ssig.sensorcap.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
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
import com.google.android.gms.wearable.*;
import br.ufmg.dcc.ssig.sensorsmanager.SensorType;
import br.ufmg.dcc.ssig.sensorsmanager.data.CaptureData;
import br.ufmg.dcc.ssig.sensorsmanager.data.DeviceData;
import br.ufmg.dcc.ssig.sensorsmanager.data.SensorData;
import br.ufmg.dcc.ssig.sensorsmanager.util.JSONUtil;
import br.ufmg.dcc.ssig.sensorcap.R;
import br.ufmg.dcc.ssig.sensorcap.adapter.AdapterCaptureList;
import br.ufmg.dcc.ssig.sensorcap.model.CaptureListItem;
import br.ufmg.dcc.ssig.sensorcap.utils.Tools;
import br.ufmg.dcc.ssig.sensorcap.widget.EmptyRecyclerView;
import br.ufmg.dcc.ssig.sensorcap.widget.LineItemDecoration;
import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.ZipOutputStream;

public class ArchiveFragment extends AbstractMainFragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private AppCompatDelegate appCompatDelegate;
    private AdapterCaptureList adapterCaptureList;
    private ActionMode actionMode;
    private ActionModeCallback actionModeCallback;

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
        this.initUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        this.refresh();
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
                                new CloseCaptureTask().execute(obj.captureData);
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
                if(captureData.isClosed()){
                    File compressedFile = new File(file.toString().replace(".json", ".zip"));
                    if(!compressedFile.exists())
                        continue;
                }
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
        share.putExtra(Intent.EXTRA_SUBJECT, "SensorCap Capture Data");
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            CaptureListItem item;
            item = this.adapterCaptureList.getItem(selectedItemPositions.get(i));

            File captureDataFile = new File(String.format("%s%s%s.zip", this.getSystemArchiveFolder(), File.separator, item.captureDataUUID));
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

                            if(item.closed){
                                File captureDataFile = new File(String.format("%s%s%s.json", getSystemArchiveFolder(), File.separator, item.captureDataUUID));
                                File captureDataCompressedFile = new File(String.format("%s%s%s.zip", getSystemArchiveFolder(), File.separator, item.captureDataUUID));
                                if(captureDataCompressedFile.delete() && captureDataFile.delete())
                                    adapterCaptureList.removeData(selectedItemPositions.get(i));
                            }else{
                                CaptureData captureData = item.captureData;
                                File captureDataFile = new File(String.format("%s%s%s.json", getSystemArchiveFolder(), File.separator, captureData.getCaptureDataUUID()));
                                File captureFolder = new File(String.format("%s%s%s", getSystemCapturesFolder(), File.separator, captureData.getCaptureDataUUID()));
                                if(captureDataFile.delete() && deleteRecursive(captureFolder))
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



    //----------------------------------------------------------------------------------------------
    // CloseCaptureTask
    //----------------------------------------------------------------------------------------------
    @SuppressLint("StaticFieldLeak")
    private class CloseCaptureTask extends AsyncTask<CaptureData, Object, Integer> implements
            DataClient.OnDataChangedListener,
            MessageClient.OnMessageReceivedListener{

        private final Integer RESPONSE_NO_SMARTWATCH = 0;
        private final Integer RESPONSE_FOLDER_FAILURE = 1;
        private final Integer RESPONSE_ASSET_FAILURE = 2;
        private final Integer RESPONSE_IO_FAILURE = 3;
        private final Integer RESPONSE_EXECUTION_FAILURE = 4;
        private final Integer RESPONSE_TIME_OUT_FAILURE = 5;
        private final Integer RESPONSE_CANCEL = 50;
        private final Integer RESPONSE_SUCCESS = 100;

        private String messageOnCancel = null;
        @DrawableRes private Integer iconOnCancel = null;

        private final BlockingQueue<DataMap> dataItemsBlockingQueue = new LinkedBlockingQueue<>(50);
        private MaterialDialog dialogReceiveFiles;
        private MaterialDialog dialogCompressFiles;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            final String uri = String.format("wear://*%s", getString(R.string.message_path_host_archive_fragment_prefix));
            Wearable.getMessageClient(Objects.requireNonNull(getContext())).addListener(this, Uri.parse(uri), MessageClient.FILTER_PREFIX);
            Wearable.getDataClient(Objects.requireNonNull(getContext())).addListener(this, Uri.parse(uri), DataClient.FILTER_PREFIX);
            this.cleanDataItems();
        }

        @Override
        protected Integer doInBackground(CaptureData... values) {

            //--------------------------------------------------------------------------------------
            // PRE-INITIALIZATION
            //--------------------------------------------------------------------------------------
            CaptureData captureData = values[0];
            File captureDataFile = new File(String.format("%s%s%s.json", getSystemArchiveFolder(), File.separator, captureData.getCaptureDataUUID()));
            File captureFolder = new File(String.format("%s%s%s", getSystemCapturesFolder(), File.separator, captureData.getCaptureDataUUID()));

            DeviceData clientDeviceData = captureData.getClientDeviceData();
            List<FileSource> clientCapturesFiles = new LinkedList<>();

            //--------------------------------------------------------------------------------------
            // SMARTWATCH FILES
            //--------------------------------------------------------------------------------------
            if (clientDeviceData != null){
                if (isWearClientConnected() && getWearService().getClientDeviceInfo().getDeviceKey().equals(clientDeviceData.getDeviceKey())){

                    File clientCaptureFolder = new File(String.format("%s%s%s", captureFolder, File.separator, clientDeviceData.getDeviceDataUUID()));
                    if(!(clientCaptureFolder.exists() || clientCaptureFolder.mkdirs()))
                        return this.RESPONSE_FOLDER_FAILURE;

                    try {
                        getWearService().requestSensorFiles(clientDeviceData);
                    } catch (ApiException e) {
                        e.printStackTrace();
                        return this.RESPONSE_NO_SMARTWATCH;
                    }

                    Map<SensorType, Boolean> sensorsFilesReceived = new HashMap<>();
                    for (final Map.Entry<SensorType, SensorData> entry : clientDeviceData.getSensorsData().entrySet()){
                        if(entry.getValue().isEnable())
                            sensorsFilesReceived.put(entry.getKey(), false);
                    }
                    publishProgress(0, sensorsFilesReceived.size());

                    while(sensorsFilesReceived.containsValue(false)){

                        if(isCancelled()){
                            return this.RESPONSE_CANCEL;
                        }

                        try {
                            DataMap dataMap = this.dataItemsBlockingQueue.poll(120, TimeUnit.SECONDS);
                            String sensorCode = dataMap.getString(getString(R.string.data_item_sensors_smartwatch_key));
                            Asset asset = dataMap.getAsset(getString(R.string.data_item_sensors_smartwatch_asset));
                            if (asset == null || sensorCode == null)
                                return this.RESPONSE_ASSET_FAILURE;

                            SensorType sensorType = SensorType.fromCode(sensorCode);
                            SensorData sensorData = clientDeviceData.getSensorsData().get(sensorType);
                            publishProgress(1, sensorType);

                            File sensorFile = new File(String.format("%s%s%s.%s", clientCaptureFolder, File.separator, sensorData.getSensorDataUUID(), clientDeviceData.getSensorWriterType().fileExtension()));
                            if(!sensorFile.exists()) {

                                Task<DataClient.GetFdForAssetResponse> getFdForAssetResponseTask = Wearable.getDataClient(Objects.requireNonNull(getContext())).getFdForAsset(asset);
                                DataClient.GetFdForAssetResponse getFdForAssetResponse = Tasks.await(getFdForAssetResponseTask, 20, TimeUnit.SECONDS);
                                InputStream assetInputStream = getFdForAssetResponse.getInputStream();
                                BufferedOutputStream bufferedOutputStream =new BufferedOutputStream(new FileOutputStream(sensorFile), 512 * 1024);

                                byte[] bytes = new byte[1024];
                                int lengthReadBytes;
                                while ((lengthReadBytes = assetInputStream.read(bytes)) >= 0) {
                                    bufferedOutputStream.write(bytes, 0, lengthReadBytes);
                                }
                                bufferedOutputStream.flush();
                                bufferedOutputStream.close();
                                assetInputStream.close();

                            }

                            sensorsFilesReceived.put(sensorType, true);
                            clientCapturesFiles.add(new FileSource(String.format("smartwatch/%s", sensorFile.getName()), sensorFile));

                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                            return this.RESPONSE_EXECUTION_FAILURE;
                        } catch (TimeoutException e) {
                            e.printStackTrace ( );
                            return this.RESPONSE_TIME_OUT_FAILURE;
                        } catch (IOException e) {
                            e.printStackTrace ( );
                            return this.RESPONSE_IO_FAILURE;
                        }

                    }

                    publishProgress(-1);

                }else{
                    return this.RESPONSE_NO_SMARTWATCH;
                }
            }

            //--------------------------------------------------------------------------------------
            // SMARTPHONE FILES
            //--------------------------------------------------------------------------------------
            DeviceData hostDeviceData = captureData.getHostDeviceData();
            List<FileSource> hostCapturesFiles = new LinkedList<>();
            if (hostDeviceData != null) {
                File hostCaptureFolder = new File(String.format("%s%s%s", captureFolder, File.separator, hostDeviceData.getDeviceDataUUID()));
                for (final SensorData sensorData : hostDeviceData.getSensorsData().values()){
                    File sensorFile = new File(String.format("%s%s%s.%s", hostCaptureFolder, File.separator, sensorData.getSensorDataUUID(), hostDeviceData.getSensorWriterType().fileExtension()));
                    if(sensorData.isEnable() && sensorFile.exists()) {
                        hostCapturesFiles.add(new FileSource(String.format("smartphone/%s", sensorFile.getName()), sensorFile));
                    }
                }
            }

            List<FileSource> filesToZip = new LinkedList<>();
            filesToZip.addAll(hostCapturesFiles);
            filesToZip.addAll(clientCapturesFiles);
            publishProgress(2, filesToZip.size() + 1);

            File compressedCaptureFile = new File(String.format("%s%s%s.zip", getSystemArchiveFolder(), File.separator, captureData.getCaptureDataUUID()));
            if(compressedCaptureFile.exists() && !compressedCaptureFile.delete())
                return this.RESPONSE_IO_FAILURE;

            try {
                new ZipOutputStream(new FileOutputStream(compressedCaptureFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return this.RESPONSE_IO_FAILURE;
            }

            ZipUtil.packEntry(captureDataFile, compressedCaptureFile, "capture.json");
            publishProgress(3);
            for(FileSource fileSource : filesToZip){
                ZipUtil.addEntry(compressedCaptureFile, fileSource);
                publishProgress(3);
            }

            captureData.setClosed(true);
            try {
                JSONUtil.save(captureData, captureDataFile);
            } catch (IOException e) {
                e.printStackTrace();
                return this.RESPONSE_IO_FAILURE;
            }

            if (!deleteRecursive(captureFolder))
                return this.RESPONSE_IO_FAILURE;

            return RESPONSE_SUCCESS;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            int command = (int)values[0];
            switch (command){
                case -1:
                    this.dismissDialogs();
                    break;
                case 0:
                    this.dialogReceiveFiles = this.makeReceiveFilesDialog((int)values[1]);
                    this.dialogReceiveFiles.show();
                    break;
                case 1:
                    if(this.dialogReceiveFiles != null){
                        this.dialogReceiveFiles.incrementProgress(1);
                        this.dialogReceiveFiles.setContent(String.format("%s: %s", getString(R.string.archive_dialog_wear_sensors_content_prefix), values[1].toString()));
                    }
                    break;
                case 2:
                    this.dialogCompressFiles = this.makeCompressFilesDialog((int)values[1]);
                    this.dialogCompressFiles.show();
                    break;
                case 3:
                    if(this.dialogCompressFiles != null){
                        this.dialogCompressFiles.incrementProgress(1);
                    }
                    break;
            }
        }

        @Override
        protected void onPostExecute(Integer response) {
            super.onPostExecute(response);
            this.unregisterListeners();
            this.dismissDialogs();

            if(response.equals(this.RESPONSE_SUCCESS)){
                refresh();
                Toast.makeText(getContext(), R.string.archive_toast_closed_success, Toast.LENGTH_LONG).show();
            }else if (response >= 0 & response <= 10){
                this.makeResponseErrorDialog(response).show();
            }else if (response.equals(this.RESPONSE_CANCEL)){
                if(this.messageOnCancel != null)
                    this.makeResponseErrorDialog(messageOnCancel, this.iconOnCancel != null ? this.iconOnCancel : R.drawable.ic_package_open);
            }

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            this.unregisterListeners();
            this.dismissDialogs();

            if(this.messageOnCancel != null)
                this.makeResponseErrorDialog(messageOnCancel, this.iconOnCancel != null ? this.iconOnCancel : R.drawable.ic_package_open);
        }

        @Override
        protected void onCancelled(Integer response) {
            super.onCancelled(response);
            this.unregisterListeners();
            this.dismissDialogs();

            if (response.equals(this.RESPONSE_CANCEL)){
                if(this.messageOnCancel != null)
                    this.makeResponseErrorDialog(messageOnCancel, this.iconOnCancel != null ? this.iconOnCancel : R.drawable.ic_package_open);
            }
        }

        @Override
        public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
            for (DataEvent event : dataEventBuffer) {
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    DataItem dataItem = event.getDataItem();
                    String path = dataItem.getUri().getPath();
                    if (path.startsWith(getString(R.string.message_path_host_archive_fragment_sensor_files_sent))) {
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                        DataMap dataMap = dataMapItem.getDataMap();
                        this.dataItemsBlockingQueue.offer(dataMap);
                    }
                }
            }
        }

        @Override
        public void onMessageReceived(@NonNull MessageEvent messageEvent) {
            String path = messageEvent.getPath();
            if (path.equals(getString(R.string.message_path_host_archive_fragment_sensor_files_error))) {
                byte[] data = messageEvent.getData();
                this.messageOnCancel = String.format("%s: %s", getString(R.string.archive_failure_from_smartwatch), new String(data));
                this.iconOnCancel = R.drawable.ic_smartphone_off;
                cancel(true);
            }
        }

        MaterialDialog makeResponseErrorDialog(Integer response) {

            String[] messages = {
                    getString(R.string.archive_close_dialog_no_smartwatch_error_content),
                    getString(R.string.archive_dialog_close_capture_error_folder),
                    getString(R.string.archive_dialog_close_capture_error_asset),
                    getString(R.string.archive_dialog_close_capture_error_io),
                    getString(R.string.archive_dialog_close_capture_error_execution),
                    getString(R.string.archive_dialog_close_capture_error_timeout)
            };

            int[] icons = {
                    R.drawable.ic_smartphone_off,
                    R.drawable.ic_package_open,
                    R.drawable.ic_package_open,
                    R.drawable.ic_package_open,
                    R.drawable.ic_package_open,
                    R.drawable.ic_package_open
            };

            return this.makeResponseErrorDialog(messages[response], icons[response]);

        }

        MaterialDialog makeResponseErrorDialog(String message, @DrawableRes int icon){
            return new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
                    .title(R.string.archive_dialog_close_capture_error_title)
                    .titleColorRes(R.color.colorAlert)
                    .content(message)
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(getContext().getDrawable(icon)), ContextCompat.getColor(getContext(), R.color.colorAlert)))
                    .cancelable(true)
                    .canceledOnTouchOutside(true)
                    .positiveText(R.string.button_ok)
                    .build();
        }

        MaterialDialog makeReceiveFilesDialog(int nFiles){
            return new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
                    .title(R.string.archive_dialog_wear_sensors_title)
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(getContext().getDrawable(R.drawable.ic_smartwatch_on)), ContextCompat.getColor(getContext(), R.color.colorPrimary)))
                    .content(R.string.archive_dialog_wear_sensors_content_waiting)
                    .progress(false, nFiles, true)
                    .cancelable(false)
                    .build();
        }

        MaterialDialog makeCompressFilesDialog(int nFiles){
            return new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
                    .title(R.string.archive_dialog_close_capture_title)
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(getContext().getDrawable(R.drawable.ic_package_close)), ContextCompat.getColor(getContext(), R.color.colorPrimary)))
                    .content(R.string.archive_dialog_close_capture_content)
                    .progress(false, nFiles, true)
                    .cancelable(false)
                    .build();
        }

        private void unregisterListeners(){
            Wearable.getDataClient(Objects.requireNonNull(getContext())).removeListener(this);
            Wearable.getMessageClient(Objects.requireNonNull(getContext())).removeListener(this);
            this.cleanDataItems();
        }

        private void dismissDialogs(){
            if (this.dialogReceiveFiles != null)
                this.dialogReceiveFiles.dismiss();
            if (this.dialogCompressFiles != null)
                this.dialogCompressFiles.dismiss();
        }

        private void cleanDataItems(){
            Uri uri = Uri.parse(String.format("wear://*%s", getString(R.string.message_path_host_archive_fragment_sensor_files_sent)));
            Wearable.getDataClient(Objects.requireNonNull(getContext())).deleteDataItems(uri, DataClient.FILTER_PREFIX);
        }

    }

    private boolean deleteRecursive(File fileOrDirectory) {
        if (!fileOrDirectory.exists())
            return false;
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        return fileOrDirectory.delete();
    }

}

