package com.e.transportervendor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.e.transportervendor.databinding.ActivityMainBinding;
import com.e.transportervendor.fragment.HistoryFragment;
import com.e.transportervendor.fragment.HomeFragment;
import com.e.transportervendor.fragment.MarketFragment;
import com.e.transportervendor.utility.InternetUtilityActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    ActionBarDrawerToggle toggle;
    ActivityMainBinding binding ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        binding.bottomNav.setItemIconTintList(null);
        binding.navDrawer.setItemIconTintList(null);
        getFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame,new HomeFragment()).commit();
        getDrawer();
        setSupportActionBar(binding.toolbar);
        toggle = new ActionBarDrawerToggle(this,binding.drawer,binding.toolbar,R.string.open,R.string.close);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));

    }

    private void getDrawer(){
        binding.navDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selected = null;
                binding.drawer.closeDrawer(GravityCompat.START);
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.websitepolicies.com/policies/view/pPBqbkNc"));
                switch(item.getItemId()){
                    case R.id.profile :
                        Toast.makeText(MainActivity.this,"User",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.terms :
                        startActivity(i);
                        break;
                    case R.id.privacy:
                        startActivity(i);
                        break;
                    case R.id.about:
                       Intent intent = new Intent(MainActivity.this,ManageVehicleActivity.class);
                       startActivity(intent);
                        break;
                    case R.id.contact:
                        break;
                    case R.id.nav_home:
                        selected = new HomeFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame,selected).commit();
                        break;
                    case R.id.nav_market:
                        selected = new MarketFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame,selected).commit();
                        break;
                    case R.id.nav_history:
                        selected = new HistoryFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame,selected).commit();
                        break;
                    case R.id.logout:
                        if(InternetUtilityActivity.isNetworkConnected(MainActivity.this)) {
                            AuthUI.getInstance()
                                    .signOut(MainActivity.this)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
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

    private void getFragment(){
        binding.bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selected = null;
                switch (item.getItemId()){
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
                getSupportFragmentManager().beginTransaction().replace(R.id.frame,selected).commit();
                return true;
            }
        });
    }
}