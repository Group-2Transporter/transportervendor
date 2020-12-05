package com.e.transportervendor;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.e.transportervendor.api.TransporterServices;
import com.e.transportervendor.bean.Transporter;
import com.e.transportervendor.databinding.ActivityCreateProfileBinding;
import com.e.transportervendor.utility.FileUtils;
import com.e.transportervendor.utility.InternetUtilityActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    ActivityCreateProfileBinding profileBinding;
    String category = "";
    String currentUserId;
    Uri imageUri;
    Transporter transporter;
    TransporterServices.TransportApi transportApi;
    boolean gstVisibility = false;
    SharedPreferences sp = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileBinding = ActivityCreateProfileBinding.inflate(LayoutInflater.from(this));
        setContentView(profileBinding.getRoot());
        try {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            transportApi = TransporterServices.getTransporterApiIntance();
            sp = getSharedPreferences("transporter", MODE_PRIVATE);
            setSupportActionBar(profileBinding.toolBar);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 111);
            }
            profileBinding.transporterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    category = profileBinding.transporterCategory.getSelectedItem().toString();
                    Toast.makeText(ProfileActivity.this, category, Toast.LENGTH_SHORT).show();
                    if (category.equalsIgnoreCase("Transporter company")) {
                        profileBinding.etGstNum.setVisibility(View.VISIBLE);
                        gstVisibility = true;
                    } else
                        profileBinding.etGstNum.setVisibility(View.GONE);
                    gstVisibility = false;

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

                profileBinding.btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(category.equalsIgnoreCase("Select category")) {
                            Toast.makeText(ProfileActivity.this, "Please select Category", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (imageUri != null) {
                            String name = profileBinding.etUserName.getText().toString();
                            if (name.isEmpty()) {
                                profileBinding.etUserName.setError("Enter Name");
                                return;
                            }
                            String contact = profileBinding.etPhoneNumber.getText().toString();
                            if (contact.length() < 10) {
                                profileBinding.etPhoneNumber.setError("Minimum 10 digits required");
                                return;
                            }
                            String streetAddress = profileBinding.etStreetAdrees.getText().toString();
                            if (streetAddress.isEmpty()) {
                                profileBinding.etStreetAdrees.setError("Require data");
                                return;
                            }
                            String cityAddress = profileBinding.etCityAddress.getText().toString();
                            if (cityAddress.isEmpty()) {
                                profileBinding.etCityAddress.setError("Require data");
                                return;
                            }
                            String stateAddress = profileBinding.etStateAdress.getText().toString();
                            if (stateAddress.isEmpty()) {
                                profileBinding.etStateAdress.setError("Require data");
                                return;
                            }
                            String gstNumber = profileBinding.etGstNum.getText().toString();
                            if (gstVisibility && gstNumber.length() < 14) {
                                profileBinding.etGstNum.setError("Minimum 14 characters required");
                                return;
                            }
                            String rating = "";
                            String token = FirebaseInstanceId.getInstance().getToken();
                            String address = streetAddress + "," + cityAddress + "," + stateAddress;


                            File file = FileUtils.getFile(ProfileActivity.this, imageUri);
                            RequestBody requestFile =
                                    RequestBody.create(
                                            MediaType.parse(getContentResolver().getType(imageUri)),
                                            file);
                            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                            RequestBody transporterId = RequestBody.create(MultipartBody.FORM, currentUserId);
                            RequestBody transporterName = RequestBody.create(MultipartBody.FORM, name);
                            RequestBody transproterType = RequestBody.create(MultipartBody.FORM, category);
                            RequestBody transproterContactNo = RequestBody.create(MultipartBody.FORM, contact);
                            RequestBody transporterAddress = RequestBody.create(MultipartBody.FORM, address);
                            RequestBody transporterGstNo = RequestBody.create(MultipartBody.FORM, gstNumber);
                            RequestBody transporterRating = RequestBody.create(MultipartBody.FORM, rating);
                            RequestBody transporterToken = RequestBody.create(MultipartBody.FORM, token);
                            Call<Transporter> call = transportApi.saveTransporter(body,
                                    transporterId,
                                    transproterType,
                                    transporterName,
                                    transproterContactNo,
                                    transporterAddress,
                                    transporterGstNo,
                                    transporterToken,
                                    transporterRating);
                            if(InternetUtilityActivity.isNetworkConnected(ProfileActivity.this)) {
                            final ProgressDialog pd = new ProgressDialog(ProfileActivity.this);
                            pd.setMessage("please wait while creating profile..");
                            pd.show();
                            call.enqueue(new Callback<Transporter>() {
                                @Override
                                public void onResponse(Call<Transporter> call, Response<Transporter> response) {
                                    Toast.makeText(ProfileActivity.this, "" + response.code(), Toast.LENGTH_SHORT).show();
                                    pd.dismiss();
                                    int status = response.code();
                                    if (status == 200) {
                                        Transporter t = response.body();
                                        Toast.makeText(ProfileActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                        saveTransporterLocally(t);
                                        Intent in = new Intent(ProfileActivity.this, ManageVehicleActivity.class);
                                        startActivity(in);
                                        finish();
                                    } else if (status == 500) {
                                        Toast.makeText(ProfileActivity.this, "Failed..", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ProfileActivity.this, "Failed.." + response.code(), Toast.LENGTH_SHORT).show();

                                    }
                                }

                                @Override
                                public void onFailure(Call<Transporter> call, Throwable t) {
                                    Toast.makeText(ProfileActivity.this, "Something went wrong : " + t, Toast.LENGTH_SHORT).show();
                                    pd.dismiss();
                                }
                            });
                            }else{
                                getInternetAlert();
                            }
                        } else
                            Toast.makeText(ProfileActivity.this, "Image is mendatory..", Toast.LENGTH_SHORT).show();

                    }
                });

                profileBinding.eteditImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ActivityCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 11);
                        } else {
                            Intent in = new Intent();
                            in.setAction(Intent.ACTION_GET_CONTENT);
                            in.setType("image/*");
                            startActivityForResult(in, 111);
                        }
                    }
                });

        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getInternetAlert() {
        final AlertDialog ab = new AlertDialog.Builder(ProfileActivity.this)
                .setCancelable(false)
                .setTitle("Network Not Connected")
                .setMessage("Please check your network connection")
                .setPositiveButton("Retry", null)
                .show();
        Button positive = ab.getButton(AlertDialog.BUTTON_POSITIVE);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (InternetUtilityActivity.isNetworkConnected(ProfileActivity.this)) {
                    ab.dismiss();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111 && resultCode == RESULT_OK) {
            imageUri = data.getData();
            profileBinding.profile.setImageURI(imageUri);
        }

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
        editor.commit();
    }
}
