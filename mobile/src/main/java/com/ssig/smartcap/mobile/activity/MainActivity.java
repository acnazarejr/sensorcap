package com.ssig.smartcap.mobile.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
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
import android.widget.Toast;

import com.ssig.smartcap.mobile.R;
import com.ssig.smartcap.mobile.fragment.AbstractMainFragment;
import com.ssig.smartcap.mobile.fragment.ArchiveFragment;
import com.ssig.smartcap.mobile.fragment.CaptureFragment;
import com.ssig.smartcap.mobile.fragment.SmartphoneFragment;
import com.ssig.smartcap.mobile.fragment.SmartwatchFragment;
import com.ssig.smartcap.mobile.fragment.TimeToolFragment;
import com.ssig.smartcap.mobile.utils.Tools;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView navigation;
    private Toolbar toolbar;

    boolean isBottomNavigationHide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.initToolbar();
        this.initBottomNavigation();
        this.initFragmentContainer();


    }

    // ---------------------------------------------------------------------------------------------
    // ACTION TOOLBAR STUFFS
    // ---------------------------------------------------------------------------------------------
    private void initToolbar() {
        this.toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(this.toolbar);
        Tools.setSystemBarColor(this, R.color.grey_20);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != android.R.id.home) {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
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
            int color = ContextCompat.getColor(this,((AbstractMainFragment)fragment).color);
            this.navigation.setBackgroundColor(color);
            this.toolbar.setTitle(((AbstractMainFragment)fragment).title);
            this.toolbar.setNavigationIcon(((AbstractMainFragment)fragment).icon);
            this.toolbar.getNavigationIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            return true;
        }
        return false;
    }

}
