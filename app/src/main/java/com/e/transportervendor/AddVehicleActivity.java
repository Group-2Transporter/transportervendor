package com.e.transportervendor;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.e.transportervendor.api.VehicleService;
import com.e.transportervendor.bean.Vehicle;
import com.e.transportervendor.databinding.AddVehicleBinding;
import com.e.transportervendor.utility.FileUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddVehicleActivity extends AppCompatActivity {
    AddVehicleBinding vehicleBinding;
    Uri imageUri;
    String currentUserId;
    VehicleService.VehicleApi vehicleApi;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vehicleBinding = AddVehicleBinding.inflate(LayoutInflater.from(this));
        setContentView(vehicleBinding.getRoot());
        try {
            currentUserId = FirebaseAuth.getInstance().getUid();
            setSupportActionBar(vehicleBinding.toolBar);
            vehicleApi = VehicleService.getVehicleApiIntance();
            final Intent i = getIntent();
            final Vehicle v = (Vehicle) i.getSerializableExtra("vehicle");
            if (v != null) {
                Picasso.get().load(v.getImageUrl()).into(vehicleBinding.image);
                vehicleBinding.vehicletype.setText(v.getName());
                vehicleBinding.edit.setVisibility(View.GONE);
                vehicleBinding.Numberofvehicle.setText("" + v.getCount());
                vehicleBinding.btnSave.setText("update");
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 111);
            }
            vehicleBinding.btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String name = vehicleBinding.vehicletype.getText().toString();
                    if (name.isEmpty()) {
                        vehicleBinding.vehicletype.setError("Enter type");
                        return;
                    }
                    int count = Integer.parseInt(vehicleBinding.Numberofvehicle.getText().toString());
                    if (TextUtils.isEmpty(count + "")) {
                        vehicleBinding.Numberofvehicle.setError("Select Number of Vehicle");
                        return;
                    }
                    String button = vehicleBinding.btnSave.getText().toString();
                    if (button.equalsIgnoreCase("save")) {
                        if (imageUri != null) {
                            try {
                                File file = FileUtils.getFile(AddVehicleActivity.this, imageUri);
                                RequestBody requestFile =
                                        RequestBody.create(
                                                MediaType.parse(getContentResolver().getType(imageUri)),
                                                file);

                                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                                RequestBody vehicleType = RequestBody.create(MultipartBody.FORM, name);
                                RequestBody transporterid = RequestBody.create(MultipartBody.FORM, currentUserId);
                                RequestBody numberOfVehicle = RequestBody.create(MultipartBody.FORM, String.valueOf(count));
                                Call<Vehicle> call = vehicleApi.saveVehicle(body,
                                        vehicleType,
                                        numberOfVehicle,
                                        transporterid
                                );
                                call.enqueue(new Callback<Vehicle>() {
                                    @Override
                                    public void onResponse(Call<Vehicle> call, Response<Vehicle> response) {
                                        int status = response.code();
                                        if (status == 200) {
                                            try {
                                                Vehicle v = response.body();
                                                Toast.makeText(AddVehicleActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                                sendUserToManageVehicleActivity();
                                            } catch (Exception e) {
                                                Toast.makeText(AddVehicleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                        } else if (status == 500) {
                                            Toast.makeText(AddVehicleActivity.this, "Failed..", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Vehicle> call, Throwable t) {
                                        Toast.makeText(AddVehicleActivity.this, "Something went wrong : " + t, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }catch (Exception e){
                                Toast.makeText(AddVehicleActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else
                            Toast.makeText(AddVehicleActivity.this, "Image is mendatory..", Toast.LENGTH_SHORT).show();
                    } else if (button.equalsIgnoreCase("update")) {
                        v.setCount(count);
                        v.setName(name);
                        Call<Vehicle> call = vehicleApi.updateVehicle(currentUserId, v);
                        call.enqueue(new Callback<Vehicle>() {
                            @Override
                            public void onResponse(Call<Vehicle> call, Response<Vehicle> response) {
                                if (response.code() == 200) {
                                    Toast.makeText(AddVehicleActivity.this, "Vehicle Update SuccessFully", Toast.LENGTH_SHORT).show();
                                    sendUserToManageVehicleActivity();
                                }
                            }

                            @Override
                            public void onFailure(Call<Vehicle> call, Throwable t) {

                            }
                        });

                    }
                }
            });
            vehicleBinding.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ActivityCompat.checkSelfPermission(AddVehicleActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(AddVehicleActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 11);
                    } else {
                        Intent in = new Intent();
                        in.setAction(Intent.ACTION_GET_CONTENT);
                        in.setType("image/*");
                        startActivityForResult(in, 111);
                    }
                }
            });
            setSupportActionBar(vehicleBinding.toolBar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendUserToManageVehicleActivity() {
        Intent in = new Intent(AddVehicleActivity.this, ManageVehicleActivity.class);
        startActivity(in);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111 && resultCode == RESULT_OK) {
            imageUri = data.getData();
            vehicleBinding.image.setImageURI(imageUri);
        }

    }
}
