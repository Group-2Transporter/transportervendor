package com.e.transportervendor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.e.transportervendor.databinding.SplashBinding;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashBinding splash = SplashBinding.inflate(getLayoutInflater());
        setContentView(splash.getRoot());
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(InternetUtilityActivity.isNetworkConnected(SplashActivity.this)) {
                    parseIntent();
                }
                else{
                    AlertDialog ab = new AlertDialog.Builder(SplashActivity.this)
                            .setCancelable(false)
                            .setTitle("Network Not Connected")
                            .setMessage("Please check your network connection")
                            .setPositiveButton("Retry", null)
                            .show();
                    Button positive = ab.getButton(AlertDialog.BUTTON_POSITIVE);
                    positive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(InternetUtilityActivity.isNetworkConnected(SplashActivity.this))
                                parseIntent();
                        }
                    });
                }
            }
        },2500);
    }

    private void parseIntent(){
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
