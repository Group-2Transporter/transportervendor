package com.e.transportervendor;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.e.transportervendor.api.VehicleService;
import com.e.transportervendor.bean.Vehicle;
import com.e.transportervendor.databinding.AddVehicleBinding;
import com.e.transportervendor.utility.FileUtils;
import com.e.transportervendor.utility.InternetUtilityActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

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
    ProgressBar pd;
    boolean image = false;

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
                image = true;
                Picasso.get().load(v.getImageUrl()).into(vehicleBinding.image);
                vehicleBinding.vehicletype.setText(v.getName());
                vehicleBinding.edit.setVisibility(View.GONE);
                vehicleBinding.Numberofvehicle.setText("" + v.getCount());
                vehicleBinding.btnSave.setText("update");
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 111);
            }
            if (InternetUtilityActivity.isNetworkConnected(this)) {

                vehicleBinding.btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (image || imageUri != null) {
                            String name = vehicleBinding.vehicletype.getText().toString();
                            if (TextUtils.isEmpty(name)) {
                                vehicleBinding.vehicletype.setError("Enter type");
                                return;
                            }
                            String counter = vehicleBinding.Numberofvehicle.getText().toString();
                            if (counter.isEmpty()) {
                                vehicleBinding.Numberofvehicle.setError("Select Number of Vehicle");
                                return;
                            }
                            int count = Integer.parseInt(counter);
                            String button = vehicleBinding.btnSave.getText().toString();
                            if (button.equalsIgnoreCase("save")) {

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
                                    if (InternetUtilityActivity.isNetworkConnected(AddVehicleActivity.this)) {
                                        pd = new ProgressBar(AddVehicleActivity.this);
                                        pd.startLoadingDialog();
                                        call.enqueue(new Callback<Vehicle>() {
                                            @Override
                                            public void onResponse(Call<Vehicle> call, Response<Vehicle> response) {
                                                int status = response.code();
                                                pd.dismissDialog();
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
                                                pd.dismissDialog();
                                                Toast.makeText(AddVehicleActivity.this, "Something went wrong : " + t, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        getInternetAlert();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(AddVehicleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else if (button.equalsIgnoreCase("update")) {
                                v.setCount(count);
                                v.setName(name);
                                if (InternetUtilityActivity.isNetworkConnected(AddVehicleActivity.this)) {
                                    pd = new ProgressBar(AddVehicleActivity.this);
                                    pd.startLoadingDialog();
                                    Call<Vehicle> call = vehicleApi.updateVehicle(currentUserId, v);
                                    call.enqueue(new Callback<Vehicle>() {
                                        @Override
                                        public void onResponse(Call<Vehicle> call, Response<Vehicle> response) {
                                            pd.dismissDialog();
                                            if (response.code() == 200) {
                                                Toast.makeText(AddVehicleActivity.this, "Vehicle Update SuccessFully", Toast.LENGTH_SHORT).show();
                                                sendUserToManageVehicleActivity();
                                                finish();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Vehicle> call, Throwable t) {
                                            pd.dismissDialog();
                                        }
                                    });
                                } else {
                                    getInternetAlert();
                                }
                            }
                        }else
                            Toast.makeText(AddVehicleActivity.this, "Image is mendatory..", Toast.LENGTH_SHORT).show();
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
            } else {
                getInternetAlert();
            }
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                if (InternetUtilityActivity.isNetworkConnected(AddVehicleActivity.this)) {
                    ab.dismiss();
                }
            }
        });
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
