package com.e.transportervendor;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.e.transportervendor.adapter.ShowVehicleListAdapter;
import com.e.transportervendor.api.TransporterServices;
import com.e.transportervendor.api.VehicleService;
import com.e.transportervendor.bean.Transporter;
import com.e.transportervendor.bean.Vehicle;
import com.e.transportervendor.databinding.ManageVehicleBinding;
import com.e.transportervendor.utility.InternetUtilityActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageVehicleActivity extends AppCompatActivity {
    ManageVehicleBinding manageVehicleBinding;
    List<Vehicle> vehicleList;
    ShowVehicleListAdapter adapter;
    ProgressDialog pd;
    String currentUserId;
    TransporterServices.TransportApi transporterApi;
    VehicleService.VehicleApi vehicleApi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            currentUserId = FirebaseAuth.getInstance().getUid();
            vehicleApi = VehicleService.getVehicleApiIntance();
            manageVehicleBinding = ManageVehicleBinding.inflate(getLayoutInflater());
            setContentView(manageVehicleBinding.getRoot());
            if (InternetUtilityActivity.isNetworkConnected(this)) {
                transporterApi = TransporterServices.getTransporterApiIntance();
                Call<Transporter> call = transporterApi.getTransporterVehicleList(currentUserId);
                pd = new ProgressDialog(this);
                pd.setMessage("Check Data Please Wait...");
                pd.show();
                call.enqueue(new Callback<Transporter>() {
                    @Override
                    public void onResponse(final Call<Transporter> call, Response<Transporter> response) {
                        Toast.makeText(ManageVehicleActivity.this, "" + response.code(), Toast.LENGTH_SHORT).show();
                        if (response.code() == 200) {
                            try {
                                Transporter t = response.body();
                                vehicleList = t.getVehicle();
                                pd.dismiss();
                                adapter = new ShowVehicleListAdapter(vehicleList);
                                manageVehicleBinding.rv.setAdapter(adapter);
                                manageVehicleBinding.rv.setLayoutManager(new LinearLayoutManager(ManageVehicleActivity.this));
                                adapter.setOnRecyclerListener(new ShowVehicleListAdapter.OnRecyclerViewClick() {
                                    @Override
                                    public void onItemClick(final Vehicle vehicle, final int postion, final String status) {
                                        if (status.equalsIgnoreCase("Delete")) {
                                            AlertDialog.Builder ab = new AlertDialog.Builder(ManageVehicleActivity.this);
                                            ab.setTitle("Delete");
                                            ab.setMessage("Are You Sure ?");
                                            ab.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    final ProgressDialog pd1 = new ProgressDialog(ManageVehicleActivity.this);
                                                    pd1.setMessage("Delete Vehicles");
                                                    pd1.show();
                                                    if (InternetUtilityActivity.isNetworkConnected(ManageVehicleActivity.this)) {
                                                        Call<Vehicle> call1 = vehicleApi.deleteTransporterVehicle(vehicle.getVehicelId(), currentUserId);
                                                        call1.enqueue(new Callback<Vehicle>() {
                                                            @Override
                                                            public void onResponse(Call<Vehicle> call, Response<Vehicle> response) {
                                                                if (response.code() == 200) {
                                                                    vehicleList.remove(postion);
                                                                    adapter.notifyDataSetChanged();
                                                                    Snackbar.make(manageVehicleBinding.rv, "Vehicle Delete Successfully", Snackbar.LENGTH_LONG);
                                                                }
                                                                pd1.dismiss();
                                                            }

                                                            @Override
                                                            public void onFailure(Call<Vehicle> call, Throwable t) {
                                                                Toast.makeText(ManageVehicleActivity.this, "" + t, Toast.LENGTH_SHORT).show();
                                                                pd1.dismiss();
                                                            }
                                                        });
                                                    } else {
                                                        getInternetAlert();
                                                    }
                                                }
                                            });
                                            ab.setNegativeButton("Cancel", null);
                                            ab.show();

                                        } else if (status.equalsIgnoreCase("Edit")) {
                                            try {
                                                Toast.makeText(ManageVehicleActivity.this, "hell", Toast.LENGTH_SHORT).show();
                                                Intent in = new Intent(ManageVehicleActivity.this, AddVehicleActivity.class);
                                                in.putExtra("vehicle", vehicle);
                                                startActivity(in);
                                            } catch (Exception e) {
                                                Toast.makeText(ManageVehicleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });

                            } catch (Exception e) {
                                Toast.makeText(ManageVehicleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Transporter> call, Throwable t) {
                        Toast.makeText(ManageVehicleActivity.this, "Something went rong", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                });


                manageVehicleBinding.fbAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent in = new Intent(ManageVehicleActivity.this, AddVehicleActivity.class);
                        startActivity(in);
                    }
                });
            } else {
                getInternetAlert();
            }
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        setSupportActionBar(manageVehicleBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void getInternetAlert() {
        final AlertDialog ab = new AlertDialog.Builder(ManageVehicleActivity.this)
                .setCancelable(false)
                .setTitle("Network Not Connected")
                .setMessage("Please check your network connection")
                .setPositiveButton("Retry", null)
                .show();
        Button positive = ab.getButton(AlertDialog.BUTTON_POSITIVE);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (InternetUtilityActivity.isNetworkConnected(ManageVehicleActivity.this)) {
                    ab.dismiss();
                }
            }
        });
    }


}
