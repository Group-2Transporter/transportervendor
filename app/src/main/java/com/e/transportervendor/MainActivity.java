package com.e.transportervendor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.e.transportervendor.api.TransporterServices;
import com.e.transportervendor.bean.Transporter;
import com.e.transportervendor.databinding.ActivityMainBinding;
import com.e.transportervendor.databinding.LanguageSelectAlertBinding;
import com.e.transportervendor.fragment.HistoryFragment;
import com.e.transportervendor.fragment.HomeFragment;
import com.e.transportervendor.fragment.MarketFragment;
import com.e.transportervendor.utility.InternetUtilityActivity;
import com.e.transportervendor.utility.LocaleHelper;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    ActionBarDrawerToggle toggle;
    ActivityMainBinding binding;
    String currentUserId;
    SharedPreferences sp = null,language = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("transporter", MODE_PRIVATE);
        language = getSharedPreferences("Language", MODE_PRIVATE);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        String lang = language.getString("language", "en");
        LocaleHelper.updateResources(binding.getRoot().getContext(),lang);
        setContentView(binding.getRoot());
        Log.e("Language","========>"+lang);
        Intent i = getIntent();
        String newBid = (String) i.getCharSequenceExtra("newBid");
        try {
            String name = sp.getString("name", "not_found");
            String image = sp.getString("image", "not_found");
            if (!image.equals("not_found")) {
                Picasso.get().load(image).into(binding.civUser);
                binding.civUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendUserToUpdateProfile();
                    }
                });
            }
            if (!name.equals("not_found"))
                binding.tvTransporter.setText(name);
            currentUserId = FirebaseAuth.getInstance().getUid();
            binding.bottomNav.setItemIconTintList(null);
            binding.navDrawer.setItemIconTintList(null);
            if (newBid != null && newBid.equalsIgnoreCase("newBid")) {
                Toast.makeText(this, "New Bid Received", Toast.LENGTH_SHORT).show();
                getFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, new MarketFragment()).commit();
                binding.bottomNav.setSelectedItemId(R.id.nav_market);
            } else {
                getFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, new HomeFragment()).commit();
            }
            getDrawer();
            setSupportActionBar(binding.toolbar);
            toggle = new ActionBarDrawerToggle(this, binding.drawer, binding.toolbar, R.string.open, R.string.close);
            toggle.syncState();
            toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void sendUserToUpdateProfile() {
        Intent inte = new Intent(MainActivity.this, ProfileUpdateActivity.class);
        startActivity(inte);
    }

    private void getDrawer() {
        binding.navDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selected = null;
                binding.drawer.closeDrawer(GravityCompat.START);
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.websitepolicies.com/policies/view/pPBqbkNc"));
                switch (item.getItemId()) {
                    case R.id.profile:
                        sendUserToUpdateProfile();
                        break;
                    case R.id.terms:
                        startActivity(i);
                        break;
                    case R.id.privacy:
                        startActivity(i);
                        break;
                    case R.id.about:
                        Toast.makeText(MainActivity.this, "About us", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.contact:
                        break;
                    case R.id.nav_home:
                        selected = new HomeFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame, selected).commitNow();
                        binding.bottomNav.setSelectedItemId(R.id.nav_home);
                        break;
                    case R.id.nav_market:
                        selected = new MarketFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame, selected).commitNow();
                        binding.bottomNav.setSelectedItemId(R.id.nav_market);
                        break;
                    case R.id.nav_history:
                        selected = new HistoryFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame, selected).commitNow();
                        binding.bottomNav.setSelectedItemId(R.id.nav_history);
                        break;
                    case R.id.language:
                        final AlertDialog ab = new AlertDialog.Builder(MainActivity.this).create();
                        LanguageSelectAlertBinding languageBinding = LanguageSelectAlertBinding.inflate(LayoutInflater.from(MainActivity.this));
                        ab.setView(languageBinding.getRoot());
                        ArrayList<String> languageList = new ArrayList<>();
                        languageList.add("English");
                        languageList.add("हिन्दी");
                        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, languageList);
                        languageBinding.lv.setAdapter(itemsAdapter);
                        languageBinding.lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                SharedPreferences.Editor editor = language.edit();

                                switch (position) {
                                    case 0:
                                        editor.clear();
                                        editor.putString("language", "en").commit();
                                        LocaleHelper.updateResources(MainActivity.this, "en");
                                        recreate();
                                        ab.dismiss();
                                        break;
                                    case 1:
                                        editor.clear();
                                        editor.putString("language", "hi").commit();
                                        LocaleHelper.updateResources(MainActivity.this, "hi");
                                        recreate();
                                        ab.dismiss();
                                        break;
                                }
                            }
                        });
                        ab.show();
                        break;
                    case R.id.logout:
                        if (InternetUtilityActivity.isNetworkConnected(MainActivity.this)) {
                            AuthUI.getInstance()
                                    .signOut(MainActivity.this)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                            SharedPreferences.Editor editor = sp.edit();
                                            editor.clear();
                                            editor.commit();
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                        }

                        break;
                }
                return false;
            }
        });
    }

    private void getFragment() {
        binding.bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selected = null;
                try {
                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            selected = new HomeFragment();
                            break;
                        case R.id.nav_market:
                            selected = new MarketFragment();
                            break;
                        case R.id.nav_history:
                            selected = new HistoryFragment();
                            break;
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "" + e.toString(), Toast.LENGTH_SHORT).show();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, selected).commit();
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUserId == null) {
            sendUserToLoginActivity();
        } else {
            String name = sp.getString("name","not");
            Log.e("Name","========>"+name);
            if (name.equalsIgnoreCase("not"))
                updateProfile();
        }
    }

    private void sendUserToLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    private void updateProfile() {
        final TransporterServices.TransportApi transportApi = TransporterServices.getTransporterApiIntance();
            if (InternetUtilityActivity.isNetworkConnected(this)) {
                try {
                    Call<Transporter> call = transportApi.getTransporterVehicleList(currentUserId);
                        call.enqueue(new Callback<Transporter>() {
                            @Override
                            public void onResponse(Call<Transporter> call, Response<Transporter> response) {
                                if (response.code() == 200) {
                                    final Transporter transporter = response.body();
                                    String token = FirebaseInstanceId.getInstance().getToken();
                                    transporter.setToken(token);
                                    Call<Transporter> call1 = transportApi.updateTransporter(transporter);
                                    call1.enqueue(new Callback<Transporter>() {
                                        @Override
                                        public void onResponse(Call<Transporter> call, Response<Transporter> response) {
                                            if (response.code() == 200) {
                                                saveTransporterLocally(response.body());
                                                Picasso.get().load(response.body().getImageUrl()).into(binding.civUser);
                                                binding.tvTransporter.setText(response.body().getName());
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Transporter> call, Throwable t) {
                                            Log.e("Response Error =", "========" + t.getMessage());
                                            Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else if (response.code() == 404) {
                                    sendUserToCreateProfile();
                                }
                            }

                            @Override
                            public void onFailure(Call<Transporter> call, Throwable t) {

                            }
                        });
                } catch (Exception e) {
                    Log.e("Exception","======="+e.getMessage());
                    Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                getInternetAlert();
            }
    }

    private void getInternetAlert() {
        final androidx.appcompat.app.AlertDialog ab = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Network Not Connected")
                .setMessage("Please check your network connection")
                .setPositiveButton("Retry", null)
                .show();
        Button positive = ab.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (InternetUtilityActivity.isNetworkConnected(MainActivity.this)) {
                    ab.dismiss();
                }
            }
        });
    }

    private void sendUserToCreateProfile() {
        Intent in = new Intent(this, ProfileActivity.class);
        startActivity(in);
    }

    private void saveTransporterLocally(Transporter t) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("name", t.getName());
        editor.putString("contactNumber", t.getContactNumber());
        editor.putString("address", t.getAddress());
        editor.putString("type", t.getType());
        String gstNo = "";
        if (t.getGstNumber() != null)
            gstNo = t.getGstNumber();
        editor.putString("gst", gstNo);
        editor.putString("image", t.getImageUrl());
        editor.putString("transporterId", t.getTransporterId());
        editor.putString("rating", t.getRating());
        editor.commit();
    }
}