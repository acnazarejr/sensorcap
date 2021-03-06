package br.ufmg.dcc.ssig.sensorcap.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import br.ufmg.dcc.ssig.sensorcap.R;
import br.ufmg.dcc.ssig.sensorcap.activity.MainActivity;
import br.ufmg.dcc.ssig.sensorcap.service.WearService;

import java.io.File;
import java.util.Objects;


@SuppressWarnings("WeakerAccess")
public abstract class AbstractMainFragment extends Fragment {

    private final int layout;

    private View fragmentContainer;

    private SharedPreferences sharedPreferences;
    private File systemCapturesFolder;
    private File systemArchiveFolder;

    protected AbstractMainFragment(@LayoutRes int layout) {
        this.layout = layout;
        this.fragmentContainer = null;
        this.sharedPreferences = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(this.layout, container, false);
        this.fragmentContainer = view.findViewById(R.id.fragment_container);
        this.sharedPreferences = Objects.requireNonNull(this.getActivity()).getPreferences(Context.MODE_PRIVATE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String systemFolderName = getString(R.string.system_folder_name);
        String captureFolderName = getString(R.string.capture_folder_name);
        String archiveFolderName = getString(R.string.archive_folder_name);
        this.systemCapturesFolder = new File(String.format("%s%s%s%s%s", Environment.getExternalStorageDirectory().getAbsolutePath(), File.separator, systemFolderName, File.separator, captureFolderName));
        this.systemArchiveFolder = new File(String.format("%s%s%s%s%s", Environment.getExternalStorageDirectory().getAbsolutePath(), File.separator, systemFolderName, File.separator, archiveFolderName));
    }

    @Override
    public void onStart() {
        super.onStart();
        this.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        this.hide();
    }

    public void show(){
        if (this.fragmentContainer != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
            this.fragmentContainer.startAnimation(fadeIn);
        }
    }

    public void hide() {
        if (this.fragmentContainer != null) {
            Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
            this.fragmentContainer.startAnimation(fadeOut);
        }
    }

    @SuppressWarnings("unused")
    public abstract void refresh();

    protected WearService getWearService() {
        return ((MainActivity) Objects.requireNonNull(this.getActivity())).getWearService();
    }

    protected boolean isWearClientConnected(){
        return this.getWearService() != null && this.getWearService().isConnected();
    }

    protected SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    protected File getSystemCapturesFolder() {
        return systemCapturesFolder;
    }

    protected File getSystemArchiveFolder() {
        return systemArchiveFolder;
    }
}
