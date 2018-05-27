package com.ssig.smartcap.mobile.activity;

import android.annotation.SuppressLint;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.ssig.smartcap.mobile.R;
import com.ssig.smartcap.mobile.utils.Tools;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView navigation;
    private ActionBar actionBar;
    private Toolbar toolbar;

    boolean isBottomNavigationHide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initBottomNavigation();
    }

    // ---------------------------------------------------------------------------------------------
    // ACTION TOOLBAR STUFFS
    // ---------------------------------------------------------------------------------------------

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.logo);
        toolbar.getLogo().setColorFilter(ContextCompat.getColor(this, R.color.teal_800), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Capture");
//        actionBar.setDisplayShowHomeEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(false);
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

        isBottomNavigationHide = false;

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                actionBar.setTitle(item.getTitle());
                return true;
            }
        });

        NestedScrollView nested_content = findViewById(R.id.nested_scroll_view);
        nested_content.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                animateNavigation((scrollY > oldScrollY));
            }
        });


        // display image
        Tools.displayImageOriginal(this, (ImageView) findViewById(R.id.image_1), R.drawable.image_8);
        Tools.displayImageOriginal(this, (ImageView) findViewById(R.id.image_2), R.drawable.image_9);
        Tools.displayImageOriginal(this, (ImageView) findViewById(R.id.image_3), R.drawable.image_15);
        Tools.displayImageOriginal(this, (ImageView) findViewById(R.id.image_4), R.drawable.image_14);
        Tools.displayImageOriginal(this, (ImageView) findViewById(R.id.image_5), R.drawable.image_12);
        Tools.displayImageOriginal(this, (ImageView) findViewById(R.id.image_6), R.drawable.image_2);
        Tools.displayImageOriginal(this, (ImageView) findViewById(R.id.image_7), R.drawable.image_5);

    }

    private void animateNavigation(final boolean hide) {
        if (isBottomNavigationHide && hide || !isBottomNavigationHide && !hide) return;
        isBottomNavigationHide = hide;
        int moveY = hide ? (2 * navigation.getHeight()) : 0;
        navigation.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }


}
