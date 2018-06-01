package com.ssig.smartcap.mobile.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.ssig.smartcap.mobile.R;
import com.ssig.smartcap.mobile.utils.AdapterListInbox;
import com.ssig.smartcap.mobile.utils.Inbox;
import com.ssig.smartcap.mobile.utils.ParserJSON.ReadJsonFile;
import com.ssig.smartcap.mobile.widget.LineItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArchiveFragment extends AbstractMainFragment {
    final private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Smartcap";
    final private File dir = new File(path);

    private boolean delete;
    private AppCompatDelegate delegate = null;
    private RecyclerView recyclerView;
    private AdapterListInbox mAdapter;
    private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    private Toolbar toolbar;
    private ArrayList<HashMap<String,String>> list_itens = new ArrayList<>();
    private List<Inbox> items = new ArrayList<>();

    public ArchiveFragment(){
        super("Capture Archive", R.layout.fragment_archive);
    }

    @Override
    public void onResume() {
        super.onResume();
        }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        delegate = AppCompatDelegate.create(getActivity(), (AppCompatCallback) getActivity());
        getJsonFiles();
        initComponent();
    }

    private void getJsonFiles() {
        File[] files = dir.listFiles();
        for(File f: files){
            Inbox obj;
            HashMap<String, String> new_item;
            ReadJsonFile jsonReader = new ReadJsonFile();
            new_item = jsonReader.ReadFile(f.getName());
            obj = setInboxObj(new_item, f);
            items.add(obj);
        }
    }

    private Inbox setInboxObj(HashMap<String, String> item, File f) {
        Inbox obj = new Inbox();
        obj.name = item.get("name");
        obj.duration = item.get("duration");
        obj.date = item.get("date");
        obj.file_name = f.getName();
        return obj;
    }

    private List<Inbox> getInboxData(){
        List<Inbox> items = new ArrayList<>();
        for(int i = 0; i < list_itens.size(); i++){
            Inbox obj = new Inbox();
            obj.name = list_itens.get(i).get("name");
            obj.duration = list_itens.get(i).get("duration");
            obj.date = list_itens.get(i).get("date");
            items.add(obj);
        }
        return items;
    }

    private void initComponent() {
        recyclerView = getActivity().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new LineItemDecoration(getContext(), LinearLayout.VERTICAL));
        recyclerView.setHasFixedSize(true);
        mAdapter = new AdapterListInbox(getContext(), items);
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnClickListener(new AdapterListInbox.OnClickListener() {
            @Override
            public void onItemClick(View view, Inbox obj, int pos) {
                if (mAdapter.getSelectedItemCount() > 0) {
                    enableActionMode(pos);
                } else {
                    // read the inbox which removes bold from the row
                    Inbox inbox = mAdapter.getItem(pos);
                }
            }

            @Override
            public void onItemLongClick(View view, Inbox obj, int pos) {
                enableActionMode(pos);
            }
        });
        actionModeCallback = new ActionModeCallback();
    }

    private void enableActionMode(int position) {
        if (actionMode == null) {
            actionMode = delegate.startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_archive, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_delete) {
                deleteDialog(mode);
                return true;
            }
            if(id == R.id.action_share){
                shareFiles();
                return true;
            }
            return false;
        }

        private void deleteDialog(final ActionMode mode) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Delete selected files ?");
            builder.setMessage("\nAll files will be deleted permanently!\n");
            builder.setPositiveButton("AGREE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    deleteInboxes();
                    mode.finish();
                }
            });
            builder.setNegativeButton("DISAGREE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mode.finish();
                }
            });
            builder.show();
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelections();
            actionMode = null;
        }
    }

    private void shareFiles() {
        List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
        ArrayList<Uri> uri_files = new ArrayList<>();
        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND_MULTIPLE);
        share.setType("application/json");
        share.putExtra(Intent.EXTRA_SUBJECT, "Email");
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            Inbox item;
            item = mAdapter.getItem(selectedItemPositions.get(i));
            File[] files = dir.listFiles();
            for(File f: files) {
                if(f.getName().equals(item.file_name)) {
                    Uri uri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", f);
                    uri_files.add(uri);
                }
            }
        }
        share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uri_files);
        getContext().startActivity(Intent.createChooser(share, "Send files..."));
    }

    private void deleteInboxes() {
        List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            Inbox item;
            item = mAdapter.getItem(selectedItemPositions.get(i));
            File[] files = dir.listFiles();
            for(File f: files) {
                if(f.getName().equals(item.file_name)){
                    boolean success = f.delete();
                    if(success == true){
                        mAdapter.removeData(selectedItemPositions.get(i));
                    }
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}


