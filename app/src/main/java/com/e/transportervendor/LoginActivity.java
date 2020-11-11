package com.e.transportervendor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.e.transportervendor.utility.InternetUtilityActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;
public class LoginActivity extends AppCompatActivity {
    private  static  final int RC_SIGN_IN = 123;
    FirebaseUser currentUser;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final InternetUtilityActivity internet = new InternetUtilityActivity();

            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build());
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setTheme(R.style.GreenTheme)
                            .setLogo(R.drawable.eagleshipperlogo)
                            .build(),
                    RC_SIGN_IN);
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Intent in=new Intent(LoginActivity.this,MainActivity.class);
                in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(in);
                finish();
            } else {
                Toast.makeText(this,"Sign Failed",Toast.LENGTH_SHORT).show();
            }
        }
    }
}