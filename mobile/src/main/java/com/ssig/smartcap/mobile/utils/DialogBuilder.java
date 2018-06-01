package com.ssig.smartcap.mobile.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ssig.smartcap.mobile.R;


public class DialogBuilder {

    private Dialog dialog;

    private DialogBuilder(Dialog dialog){
        this.dialog = dialog;
    }

    public static DialogBuilder getBuilder(Context context){

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_with_action);
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


        return new DialogBuilder(dialog);
    }

    public DialogBuilder setIcon(Integer icon){
        if (icon == null)
            return this;

        ImageView imageView = this.dialog.findViewById(R.id.icon);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(icon);
        return this;
    }

    public DialogBuilder setTitle(String title){
        if (title == null)
            return this;

        TextView textView = this.dialog.findViewById(R.id.title);
        textView.setVisibility(View.VISIBLE);
        textView.setText(title);
        return this;
    }

    public DialogBuilder setColor(Integer color){
        if (color == null)
            return this;

        View layoutTitle = this.dialog.findViewById(R.id.layout_title);
        layoutTitle.setBackgroundColor(ContextCompat.getColor(this.dialog.getContext(), color));

        Button actionButton = this.dialog.findViewById(R.id.button_primary_action);
        actionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this.dialog.getContext(), color)));

        ProgressBar progress = this.dialog.findViewById(R.id.progress);
        progress.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this.dialog.getContext(), color)));

        return this;
    }

    public DialogBuilder setMessage(String message){
        if (message == null)
            return this;

        TextView textView = this.dialog.findViewById(R.id.message);
        textView.setVisibility(View.VISIBLE);
        textView.setText(message);
        return this;
    }

    public DialogBuilder showProgress(){
        View progress = this.dialog.findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);
        return this;
    }


    public DialogBuilder setPrimaryAction(String actionText, View.OnClickListener onClickListener){
        Button actionButton = this.dialog.findViewById(R.id.button_primary_action);
        actionButton.setVisibility(View.VISIBLE);
        actionButton.setText(actionText);
        actionButton.setOnClickListener(onClickListener);
        return this;
    }

    public DialogBuilder setSecondaryAction(String actionText, View.OnClickListener onClickListener){
        Button actionButton = this.dialog.findViewById(R.id.button_secondary_action);
        actionButton.setVisibility(View.VISIBLE);
        actionButton.setText(actionText);
        actionButton.setOnClickListener(onClickListener);
        return this;
    }

    public DialogBuilder setCancelable() {
        this.dialog.setCancelable(true);
        return this;
    }


    public Dialog build(){
        return dialog;
    }



}
