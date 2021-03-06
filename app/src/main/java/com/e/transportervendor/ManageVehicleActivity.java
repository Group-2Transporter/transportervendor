package com.e.transportervendor;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.e.transportervendor.databinding.DeleteAlertDialogBinding;
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
    ProgressBar pd;
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
                pd = new ProgressBar(this);
                pd.startLoadingDialog();
                call.enqueue(new Callback<Transporter>() {
                    @Override
                    public void onResponse(final Call<Transporter> call, Response<Transporter> response) {
                        pd.dismissDialog();
                        if (response.code() == 200) {
                            try {
                                Transporter t = response.body();
                                vehicleList = t.getVehicle();
                                if(t.getVehicle()!=null && t.getVehicle().size()!=0) {
                                    manageVehicleBinding.rv.setVisibility(View.VISIBLE);
                                    manageVehicleBinding.noRecords.setVisibility(View.GONE);
                                    adapter = new ShowVehicleListAdapter(vehicleList);
                                    manageVehicleBinding.rv.setAdapter(adapter);
                                    manageVehicleBinding.rv.setLayoutManager(new LinearLayoutManager(ManageVehicleActivity.this));
                                    adapter.setOnRecyclerListener(new ShowVehicleListAdapter.OnRecyclerViewClick() {
                                        @Override
                                        public void onItemClick(final Vehicle vehicle, final int postion, final String status) {
                                            if (status.equalsIgnoreCase("Delete")) {
                                                final AlertDialog ab = new AlertDialog.Builder(ManageVehicleActivity.this).create();
                                                final DeleteAlertDialogBinding deleteBinding = DeleteAlertDialogBinding.inflate(LayoutInflater.from(ManageVehicleActivity.this));
                                                ab.setView(deleteBinding.getRoot());
                                                ab.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                                ab.setCancelable(false);
                                                deleteBinding.tvDeleteLead.setText("Delete Vehicle");
                                                deleteBinding.btnConfirm.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        final ProgressBar pd1 = new ProgressBar(ManageVehicleActivity.this);
                                                        pd1.startLoadingDialog();
                                                        if (InternetUtilityActivity.isNetworkConnected(ManageVehicleActivity.this)) {
                                                            Call<Vehicle> call1 = vehicleApi.deleteTransporterVehicle(vehicle.getVehicelId(), currentUserId);
                                                            call1.enqueue(new Callback<Vehicle>() {
                                                                @Override
                                                                public void onResponse(Call<Vehicle> call, Response<Vehicle> response) {
                                                                    if (response.code() == 200) {
                                                                        vehicleList.remove(postion);
                                                                        if(vehicleList.size() == 0){
                                                                            manageVehicleBinding.rv.setVisibility(View.GONE);
                                                                            manageVehicleBinding.noRecords.setVisibility(View.VISIBLE);
                                                                        }
                                                                        adapter.notifyDataSetChanged();
                                                                        Snackbar.make(manageVehicleBinding.rv, "Vehicle Delete Successfully", Snackbar.LENGTH_LONG);
                                                                    }
                                                                    ab.dismiss();
                                                                    pd1.dismissDialog();
                                                                }

                                                                @Override
                                                                public void onFailure(Call<Vehicle> call, Throwable t) {
                                                                    Toast.makeText(ManageVehicleActivity.this, "" + t, Toast.LENGTH_SHORT).show();
                                                                    pd1.dismissDialog(); 
                                                                    ab.dismiss();
                                                                }
                                                            });
                                                        } else {
                                                            getInternetAlert();
                                                        }
                                                    }
                                                });
                                                deleteBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        ab.dismiss();
                                                    }
                                                });
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
                                }else{
                                    manageVehicleBinding.noRecords.setVisibility(View.VISIBLE);
                                    manageVehicleBinding.rv.setVisibility(View.GONE);
                                }

                            } catch (Exception e) {
                                pd.dismissDialog();
                                Toast.makeText(ManageVehicleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Transporter> call, Throwable t) {
                        Toast.makeText(ManageVehicleActivity.this, "Something went rong", Toast.LENGTH_SHORT).show();
                        pd.dismissDialog();
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
