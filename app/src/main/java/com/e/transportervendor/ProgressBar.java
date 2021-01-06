package com.e.transportervendor;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;

import com.github.ybq.android.spinkit.style.DoubleBounce;

public class ProgressBar {
   private AlertDialog dialog;
   private Activity activity;


    public ProgressBar(Activity activity){
       this. activity = activity;
    }

    public void startLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater =  activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.progress_alert,null));
        builder.setCancelable(true);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }
    public void dismissDialog(){
        dialog.dismiss();
    }
}
