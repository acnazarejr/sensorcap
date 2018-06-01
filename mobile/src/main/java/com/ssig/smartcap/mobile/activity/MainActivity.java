package com.ssig.smartcap.mobile.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ssig.btmanager.BTConnectorServer;
import com.ssig.smartcap.mobile.R;
import com.ssig.smartcap.mobile.fragment.AbstractMainFragment;
import com.ssig.smartcap.mobile.fragment.ArchiveFragment;
import com.ssig.smartcap.mobile.fragment.CaptureFragment;
import com.ssig.smartcap.mobile.fragment.SmartphoneFragment;
import com.ssig.smartcap.mobile.fragment.SmartwatchFragment;
import com.ssig.smartcap.mobile.fragment.TimeToolFragment;
import com.ssig.smartcap.mobile.utils.DialogBuilder;
import com.ssig.smartcap.mobile.utils.TimeUtils;
import com.ssig.smartcap.mobile.utils.Tools;

import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private AbstractMainFragment currentFragment;
    private BottomNavigationView navigation;
    private Toolbar toolbar;
    private Dialog dialogNtpSynchronization;
    private Dialog dialogNtpNetworkError;
    MenuItem ntpMenuItem;

    private BTConnectorServer connectorServer;
    private SharedPreferences sharedPreferences;
    private Thread threadNtpSynchronization;
    private Runnable runnableNtpSynchronization;
    private String ntpPool;
    String toastMessage;

    boolean isBottomNavigationHide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectorServer = BTConnectorServer.getInstance();
        sharedPreferences = this.getPreferences(MODE_PRIVATE);

        this.initToolbar();
        this.initBottomNavigation();
        this.initFragmentContainer();
        this.initNTPObjects();


    }

    // ---------------------------------------------------------------------------------------------
    // ACTION TOOLBAR STUFFS
    // ---------------------------------------------------------------------------------------------
    private void initToolbar() {
        this.toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(this.toolbar);
        Tools.setSystemBarColor(this, R.color.grey_3);
        Tools.setSystemBarLight(this);

    }


    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        Tools.changeMenuIconColor(menu, ContextCompat.getColor(this, R.color.grey_60));
        ntpMenuItem = menu.findItem(R.id.action_ntp);
        updateNTPMenuItem();
        return true;
    }

    public void updateNTPMenuItem(){
        if (TimeUtils.ntpIsInitialized())
            ntpMenuItem.setIcon(R.drawable.ic_earth);
        else
            ntpMenuItem.setIcon(R.drawable.ic_earth_off);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        if (itemID != android.R.id.home) {
            switch (itemID){
                case R.id.action_ntp:
                    startNTPSynchronization();
                    break;
                case R.id.action_settings:
                    Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.action_about:
                    Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
                    break;
            }

        }
        return super.onOptionsItemSelected(item);
    }


    // ---------------------------------------------------------------------------------------------
    // BOTTOM NAVIGATION STUFFS
    // ---------------------------------------------------------------------------------------------
    private void initBottomNavigation() {

        this.isBottomNavigationHide = false;
        this.navigation = findViewById(R.id.navigation);
        this.navigation.setOnNavigationItemSelectedListener(this);

        NestedScrollView nested_content = findViewById(R.id.nested_scroll_view);
        nested_content.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                animateNavigation((scrollY > oldScrollY));
            }
        });

    }

    private void animateNavigation(final boolean hide) {
        if (isBottomNavigationHide && hide || !isBottomNavigationHide && !hide) return;
        isBottomNavigationHide = hide;
        int moveY = hide ? (2 * navigation.getHeight()) : 0;
        navigation.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }


    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_capture:
                fragment = new CaptureFragment();
                break;

            case R.id.navigation_smartphone:
                fragment = new SmartphoneFragment();
                break;

            case R.id.navigation_smartwatch:
                fragment = new SmartwatchFragment();
                break;

            case R.id.navigation_time_tool:
                fragment = new TimeToolFragment();
                break;

            case R.id.navigation_archive:
                fragment = new ArchiveFragment();
                break;
        }
        return loadFragment(fragment);
    }


    // ---------------------------------------------------------------------------------------------
    // FRAGMENT STUFFS
    // ---------------------------------------------------------------------------------------------
    private void initFragmentContainer() {
        this.loadFragment(new CaptureFragment());
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
//            this.navigation.setBackgroundColor(((AbstractMainFragment)fragment).color);
            int color = ContextCompat.getColor(this,((AbstractMainFragment)fragment).getPrimaryColor());
            this.navigation.setBackgroundColor(color);
            this.toolbar.setTitle(((AbstractMainFragment)fragment).getTitle());
            this.toolbar.setNavigationIcon(((AbstractMainFragment)fragment).getIcon());
            Objects.requireNonNull(this.toolbar.getNavigationIcon()).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            currentFragment = (AbstractMainFragment)fragment;
            return true;
        }
        return false;
    }


    // ---------------------------------------------------------------------------------------------
    // NTP STUFFS
    // ---------------------------------------------------------------------------------------------
    private void initNTPObjects(){


        dialogNtpNetworkError = DialogBuilder.getBuilder(this)
                .setTitle("Network Error")
                .setIcon(R.drawable.ic_wifi_off)
                .setColor(R.color.red_600)
                .setMessage(getString(R.string.network_not_connected))
                .setPrimaryAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogNtpNetworkError.dismiss();
                        startNTPSynchronization();
                    }
                })
                .setSecondaryAction("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogNtpNetworkError.cancel();
                    }
                })
                .build();


        dialogNtpSynchronization = DialogBuilder.getBuilder(this)
                .setTitle(getString(R.string.ntp_synchronization))
                .setIcon(R.drawable.ic_earth)
                .setColor(R.color.green_600)
                .setMessage(getString(R.string.sync_ntp_with) + ntpPool)
                .showProgress()
                .setCancelable()
                .setPrimaryAction(getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogNtpSynchronization.cancel();
                    }
                })
                .build();
        dialogNtpSynchronization.setOnCancelListener(new Dialog.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialog) {
                if (threadNtpSynchronization.isAlive())
                    threadNtpSynchronization.interrupt();
                    threadNtpSynchronization = null;

            }
        });
        dialogNtpSynchronization.setOnDismissListener(new Dialog.OnDismissListener(){
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateNTPMenuItem();
                currentFragment.reload();
            }
        });


        ntpPool = sharedPreferences.getString("ntp_pool", "pool.ntp.br");
        this.runnableNtpSynchronization = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    TimeUtils.clearNTPCache(MainActivity.this);
                    TimeUtils.initializeNTP(MainActivity.this, ntpPool);
                    toastMessage = MainActivity.this.getString(R.string.ntp_synchronized);
                } catch (IOException|InterruptedException e) {
                    toastMessage = e.getMessage();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogNtpSynchronization.dismiss();
                        if (toastMessage != null) {
                            Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        };
        this.threadNtpSynchronization = null;
    }

    // ---------------------------------------------------------------------------------------------
    // NTP STUFFS
    // ---------------------------------------------------------------------------------------------
    public void startNTPSynchronization(){
        if (Tools.isNetworkConnected(this)) {
            dialogNtpSynchronization.show();
            threadNtpSynchronization = new Thread(runnableNtpSynchronization);
            threadNtpSynchronization.start();
        } else{
            dialogNtpNetworkError.show();
        }
    }
}
