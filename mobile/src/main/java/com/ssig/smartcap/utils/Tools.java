package com.ssig.smartcap.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ssig.sensorsmanager.info.SensorInfo;
import com.ssig.sensorsmanager.SensorType;
import com.ssig.smartcap.R;
import com.ssig.smartcap.adapter.AdapterListSensor;
import com.ssig.smartcap.model.SensorListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class Tools {

//    public static void setSystemBarColor(Activity act) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = act.getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.setStatusBarColor(act.getResources().getColor(R.color.colorPrimaryDark));
//        }
//    }

    public static void setSystemBarColor(Activity act, @ColorRes int color) {
        Window window = act.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(act, color));
    }

    public static void setSystemBarLight(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = act.findViewById(android.R.id.content);
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

//    public static void clearSystemBarLight(Activity act) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Window window = act.getWindow();
//            window.setStatusBarColor(ContextCompat.getColor(act, R.color.colorPrimaryDark));
//        }
//    }
//
//    /**
//     * Making notification bar transparent
//     */
//    public static void setSystemBarTransparent(Activity act) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = act.getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
//        }
//    }
//
//    public static void displayImageOriginal(Context ctx, ImageView img, @DrawableRes int drawable) {
//        try {
//            Glide.with(ctx).load(drawable)
//                    .crossFade()
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .into(img);
//        } catch (Exception e) {
//        }
//    }
//
//    public static void displayImageRound(final Context ctx, final ImageView img, @DrawableRes int drawable) {
//        try {
//            Glide.with(ctx).load(drawable).asBitmap().centerCrop().into(new BitmapImageViewTarget(img) {
//                @Override
//                protected void setResource(Bitmap resource) {
//                    RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(ctx.getResources(), resource);
//                    circularBitmapDrawable.setCircular(true);
//                    img.setImageDrawable(circularBitmapDrawable);
//                }
//            });
//        } catch (Exception e) {
//        }
//    }
//
//    public static void displayImageOriginal(Context ctx, ImageView img, String url) {
//        try {
//            Glide.with(ctx).load(url)
//                    .crossFade()
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .into(img);
//        } catch (Exception e) {
//        }
//    }
//
//    public static String getFormattedDateSimple(Long dateTime) {
//        SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy");
//        return newFormat.format(new Date(dateTime));
//    }
//
//    public static String getFormattedDateEvent(Long dateTime) {
//        SimpleDateFormat newFormat = new SimpleDateFormat("EEE, MMM dd yyyy");
//        return newFormat.format(new Date(dateTime));
//    }
//
//    public static String getFormattedTimeEvent(Long time) {
//        SimpleDateFormat newFormat = new SimpleDateFormat("h:mm a");
//        return newFormat.format(new Date(time));
//    }
//
//    public static String getEmailFromName(String name) {
//        if (name != null && !name.equals("")) {
//            String email = name.replaceAll(" ", ".").toLowerCase().concat("@mail.com");
//            return email;
//        }
//        return name;
//    }
//
//    public static int dpToPx(Context c, int dp) {
//        Resources r = c.getResources();
//        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
//    }
//
////    public static GoogleMap configActivityMaps(GoogleMap googleMap) {
////        // set map type
////        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
////        // Enable / Disable zooming controls
////        googleMap.getUiSettings().setZoomControlsEnabled(false);
////
////        // Enable / Disable Compass icon
////        googleMap.getUiSettings().setCompassEnabled(true);
////        // Enable / Disable Rotate gesture
////        googleMap.getUiSettings().setRotateGesturesEnabled(true);
////        // Enable / Disable zooming functionality
////        googleMap.getUiSettings().setZoomGesturesEnabled(true);
////
////        googleMap.getUiSettings().setScrollGesturesEnabled(true);
////        googleMap.getUiSettings().setMapToolbarEnabled(true);
////
////        return googleMap;
////    }
//
//    public static void copyToClipboard(Context context, String data) {
//        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//        ClipData clip = ClipData.newPlainText("clipboard", data);
//        clipboard.setPrimaryClip(clip);
//        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
//    }
//
//    public static void nestedScrollTo(final NestedScrollView nested, final View targetView) {
//        nested.post(new Runnable() {
//            @Override
//            public void run() {
//                nested.scrollTo(500, targetView.getBottom());
//            }
//        });
//    }
//
//    public static int dip2px(Context context, float dpValue) {
//        final float scale = context.getResources().getDisplayMetrics().density;
//        return (int) (dpValue * scale + 0.5f);
//    }
//
//    public static int px2dip(Context context, float pxValue) {
//        final float scale = context.getResources().getDisplayMetrics().density;
//        return (int) (pxValue / scale + 0.5f);
//    }
//
//    public static boolean toggleArrow(View view) {
//        if (view.getRotation() == 0) {
//            view.animate().setDuration(200).rotation(180);
//            return true;
//        } else {
//            view.animate().setDuration(200).rotation(0);
//            return false;
//        }
//    }

    public static void toggleArrow(boolean show, View view) {
        toggleArrow(show, view, true);
    }

    public static void toggleArrow(boolean show, View view, boolean delay) {
        if (show) {
            view.animate().setDuration(delay ? 200 : 0).rotation(180);
        } else {
            view.animate().setDuration(delay ? 200 : 0).rotation(0);
        }
    }
//
//    public static void changeNavigateionIconColor(Toolbar toolbar, @ColorInt int color) {
//        Drawable drawable = toolbar.getNavigationIcon();
//        drawable.mutate();
//        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
//    }
//
    public static void changeMenuIconColor(Menu menu, @ColorInt int color) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable == null) continue;
            drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static Drawable changeDrawableColor(Drawable drawable, @ColorInt int color){
        drawable.mutate();
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        return drawable;
    }

//
//    public static void changeOverflowMenuIconColor(Toolbar toolbar, @ColorInt int color) {
//        try {
//            Drawable drawable = toolbar.getOverflowIcon();
//            drawable.mutate();
//            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
//        } catch (Exception e) {
//        }
//    }
//
//    public static int getScreenWidth() {
//        return Resources.getSystem().getDisplayMetrics().widthPixels;
//    }
//
//    public static int getScreenHeight() {
//        return Resources.getSystem().getDisplayMetrics().heightPixels;
//    }
//
//    public static String toCamelCase(String input) {
//        input = input.toLowerCase();
//        StringBuilder titleCase = new StringBuilder();
//        boolean nextTitleCase = true;
//
//        for (char c : input.toCharArray()) {
//            if (Character.isSpaceChar(c)) {
//                nextTitleCase = true;
//            } else if (nextTitleCase) {
//                c = Character.toTitleCase(c);
//                nextTitleCase = false;
//            }
//
//            titleCase.append(c);
//        }
//
//        return titleCase.toString();
//    }
//
//    public static String insertPeriodically(String text, String insert, int period) {
//        StringBuilder builder = new StringBuilder(text.length() + insert.length() * (text.length() / period) + 1);
//        int index = 0;
//        String prefix = "";
//        while (index < text.length()) {
//            builder.append(prefix);
//            prefix = insert;
//            builder.append(text.substring(index, Math.min(index + period, text.length())));
//            index += period;
//        }
//        return builder.toString();
//    }
//
//    public static int manipulateColor(int color, float factor) {
//        int a = Color.alpha(color);
//        int r = Math.round(Color.red(color) * factor);
//        int g = Math.round(Color.green(color) * factor);
//        int b = Math.round(Color.blue(color) * factor);
//        return Color.argb(a,
//                Math.min(r,255),
//                Math.min(g,255),
//                Math.min(b,255));
//    }

    public static AdapterListSensor populateSensorsList(Context context, RecyclerView recyclerView, String preferencesName, Map<SensorType, SensorInfo> sensors){

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new com.ssig.smartcap.mobile.widget.LineItemDecoration(Objects.requireNonNull(context), LinearLayout.VERTICAL));
        recyclerView.setHasFixedSize(true);

        SharedPreferences sharedPreferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);

        List<SensorListItem> items = new ArrayList<>();
        for (Map.Entry<SensorType, SensorInfo> entry : sensors.entrySet()) {
            SensorInfo sensorInfo = entry.getValue();
            if (sensorInfo != null) {
                SensorListItem sensorListItem = new SensorListItem(sensorInfo);
                sensorListItem.enabled = sharedPreferences.getBoolean(sensorListItem.getSensorType().abbrev() + context.getString(R.string.preference_sensor_enabled_suffix), true);
                sensorListItem.frequency = sharedPreferences.getInt(sensorListItem.getSensorType().abbrev() + context.getString(R.string.preference_sensor_frequency_suffix), sensorListItem.getDefaultFrequency());
                items.add(sensorListItem);
            }
        }

        AdapterListSensor adapterListSensor = new AdapterListSensor(context, items);
        recyclerView.setAdapter(adapterListSensor);
        return adapterListSensor;
    }

    public static void saveSensorsPreferences(Context context, AdapterListSensor adapterListSensor, String preferencesName){
        SharedPreferences sharedPreferences = Objects.requireNonNull(context).getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        List<SensorListItem> sensorsSensorListItems = adapterListSensor.getSensorListItems();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(SensorListItem sensorListItem : sensorsSensorListItems){
            editor.putBoolean(sensorListItem.getSensorType().abbrev() + context.getString(R.string.preference_sensor_enabled_suffix), sensorListItem.enabled);
            editor.putInt(sensorListItem.getSensorType().abbrev() + context.getString(R.string.preference_sensor_frequency_suffix), sensorListItem.frequency);
        }
        editor.apply();
    }

    public static void resetSensorsPreferences(Context context, final AdapterListSensor adapterListSensor){
        new MaterialDialog.Builder(context)
                .title(R.string.dialog_reset_defaults_title)
                .content(R.string.dialog_reset_defaults_content)
                .icon(Tools.changeDrawableColor(Objects.requireNonNull(context.getDrawable(R.drawable.ic_smartphone)), ContextCompat.getColor(context, R.color.colorPrimary)))
                .cancelable(true)
                .positiveText(R.string.button_yes)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        List<SensorListItem> sensorsSensorListItems = adapterListSensor.getSensorListItems();
                        for(SensorListItem sensorListItem : sensorsSensorListItems){
                            sensorListItem.enabled = true;
                            sensorListItem.frequency = sensorListItem.getDefaultFrequency();
                        }
                        adapterListSensor.notifyDataSetChanged();
                    }
                })
                .negativeText(R.string.button_no)
                .show();
    }

}
