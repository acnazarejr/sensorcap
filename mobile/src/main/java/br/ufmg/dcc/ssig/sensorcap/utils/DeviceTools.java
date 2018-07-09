package br.ufmg.dcc.ssig.sensorcap.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;

public class DeviceTools {

    public static boolean isBluetoothDisabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled();
    }

    public static boolean isAppInstalled(Context ctx, String uri) {
        PackageManager packageManager = ctx.getPackageManager();
        try {
            packageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}
