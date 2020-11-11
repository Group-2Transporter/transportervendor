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
import com.e.transportervendor.utility.InternetUtilityActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    FirebaseUser curretUser ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        curretUser = FirebaseAuth.getInstance().getCurrentUser();
        SplashBinding splash = SplashBinding.inflate(getLayoutInflater());
        setContentView(splash.getRoot());
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(InternetUtilityActivity.isNetworkConnected(SplashActivity.this)) {
                    if(curretUser != null)
                        navigateUserToMainActivity();
                    else
                        navigateUserToLoginActivity();
                }
                else{
                    final AlertDialog ab = new AlertDialog.Builder(SplashActivity.this)
                            .setCancelable(false)
                            .setTitle("Network Not Connected")
                            .setMessage("Please check your network connection")
                            .setPositiveButton("Retry", null)
                            .show();
                    Button positive = ab.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
                    positive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(InternetUtilityActivity.isNetworkConnected(SplashActivity.this)) {
                                ab.dismiss();
                                if (curretUser != null)
                                    navigateUserToMainActivity();
                                else
                                    navigateUserToLoginActivity();
                            }
                        }
                    });
                }
            }
        },2500);
    }

    private void navigateUserToMainActivity(){
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateUserToLoginActivity(){
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
