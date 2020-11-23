package com.e.transportervendor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.e.transportervendor.api.TransporterServices;
import com.e.transportervendor.bean.Transporter;
import com.e.transportervendor.databinding.ActivityCreateProfileBinding;
import com.e.transportervendor.utility.FileUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateProfileActivity extends AppCompatActivity {
    ActivityCreateProfileBinding profileBinding;
    SharedPreferences sp;
    String imageUrl;
    Uri imageUri;
    boolean gstVisibility = false;
    TransporterServices.TransportApi transportApi;
    String currentUserId;
    Transporter transporter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transportApi = TransporterServices.getTransporterApiIntance();
        profileBinding = ActivityCreateProfileBinding.inflate(LayoutInflater.from(this));
        setContentView(profileBinding.getRoot());
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        sp = getSharedPreferences("transporter",MODE_PRIVATE);
        profileBinding.btnSave.setText("Update");
        profileBinding.etUserName.setText(sp.getString("name",""));
        profileBinding.etGstNum.setText(sp.getString("gst",""));
        String currentString = sp.getString("address","");
        String[] separated = currentString.split(",");
        profileBinding.etStreetAdrees.setText(separated[0]);
        profileBinding.etCityAddress.setText(separated[1]);
        profileBinding.etStateAdress.setText(separated[2]);
        profileBinding.etPhoneNumber.setText(sp.getString("contactNumber",""));
        imageUrl=sp.getString("image","");
        Picasso.get().load(imageUrl).into(profileBinding.profile);
        getTransporterThroughApi();

        profileBinding.eteditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent();
                in.setAction(Intent.ACTION_GET_CONTENT);
                in.setType("image/*");
                startActivityForResult(in,111);
            }
        });

        profileBinding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    String name = profileBinding.etUserName.getText().toString();
                    if(name.isEmpty()){
                        profileBinding.etUserName.setError("Enter Name");
                        return;
                    }
                    String contact = profileBinding.etPhoneNumber.getText().toString();
                    if(contact.length()<10){
                        profileBinding.etPhoneNumber.setError("Minimum 10 digits required");
                        return;
                    }
                    String streetAddress = profileBinding.etStreetAdrees.getText().toString();
                    if(streetAddress.isEmpty()){
                        profileBinding.etStreetAdrees.setError("Require data");
                        return;
                    }
                    String cityAddress = profileBinding.etCityAddress.getText().toString();
                    if(cityAddress.isEmpty()){
                        profileBinding.etCityAddress.setError("Require data");
                        return;
                    }
                    String stateAddress = profileBinding.etStateAdress.getText().toString();
                    if(stateAddress.isEmpty()){
                        profileBinding.etStateAdress.setError("Require data");
                        return;
                    }
                    String gstNumber = profileBinding.etGstNum.getText().toString();
                    if(gstVisibility && gstNumber.length()<14){
                        profileBinding.etGstNum.setError("Minimum 14 characters required");
                        return;
                    }
                    String token = FirebaseInstanceId.getInstance().getToken();
                    String address = streetAddress + "," + cityAddress + "," + stateAddress;
                    transporter.setToken(token);
                    transporter.setAddress(address);
                    transporter.setContactNumber(contact);
                    transporter.setGstNumber(gstNumber);
                    transporter.setName(name);

                    Call<Transporter> call = transportApi.updateTransporter(transporter);
                    final ProgressDialog pd = new ProgressDialog(UpdateProfileActivity.this);
                    pd.setMessage("please wait while updating profile..");
                    pd.show();
                    call.enqueue(new Callback<Transporter>() {
                        @Override
                        public void onResponse(Call<Transporter> call, Response<Transporter> response) {
                            try {
                                int status = response.code();
                                if (status == 200) {
                                    Transporter t = response.body();
                                    saveTransporterLocally(t);
                                    Toast.makeText(UpdateProfileActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                } else if (status == 500) {
                                    Toast.makeText(UpdateProfileActivity.this, "Failed..", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e){
                                Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }finally {
                                pd.dismiss();
                            }
                        }

                        @Override
                        public void onFailure(Call<Transporter> call, Throwable t) {
                            Toast.makeText(UpdateProfileActivity.this, "Something went wrong : " + t, Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }
                    });
            }
        });


        setSupportActionBar(profileBinding.toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Update Profile");
    }


    private  void getTransporterThroughApi(){
        try {
            Call<Transporter> transporterCall = transportApi.getTransporterVehicleList(currentUserId);
            transporterCall.enqueue(new Callback<Transporter>() {
                @Override
                public void onResponse(Call<Transporter> call, Response<Transporter> response) {
                    try {
                        Toast.makeText(UpdateProfileActivity.this, ""+response.code(), Toast.LENGTH_SHORT).show();
                        if (response.code() == 200) {
                            transporter = response.body();
                        }
                    }catch (Exception e){
                        Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Transporter> call, Throwable t) {
                    Toast.makeText(UpdateProfileActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 111 && resultCode == RESULT_OK){
            imageUri = data.getData();
            try {
                File file = FileUtils.getFile(UpdateProfileActivity.this, imageUri);
                RequestBody requestFile =
                        RequestBody.create(
                                MediaType.parse(getContentResolver().getType(imageUri)),
                                file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                RequestBody transporterId = RequestBody.create(MultipartBody.FORM, currentUserId);
                Toast.makeText(this, "come on ", Toast.LENGTH_SHORT).show();
                Call<Transporter> call = transportApi.updateImageTransporter(body, transporterId);
                final ProgressDialog pd = new ProgressDialog(this);
                pd.setMessage("please wait...");
                pd.setCancelable(false);
                pd.show();
                profileBinding.profile.setImageURI(imageUri);
                call.enqueue(new Callback<Transporter>() {
                    @Override
                    public void onResponse(Call<Transporter> call, Response<Transporter> response) {
                        try {
                            if (response.code() == 200) {
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("image","").clear().commit();
                                editor.putString("image",imageUrl).commit();
                                Toast.makeText(UpdateProfileActivity.this, "Image Change SuccessFully", Toast.LENGTH_SHORT).show();
                            } else if (response.code() == 500) {
                                Toast.makeText(UpdateProfileActivity.this, "Failed To Upload Image", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {

                            Toast.makeText(UpdateProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } finally {
                            pd.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<Transporter> call, Throwable t) {
                        Toast.makeText(UpdateProfileActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                });
            }catch (Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }
    private void saveTransporterLocally(Transporter t){
        SharedPreferences.Editor editor= sp.edit();
        editor.putString("name",t.getName()).clear().commit();
        editor.putString("contactNumber",t.getContactNumber()).clear().commit();
        editor.putString("address",t.getAddress()).clear().commit();
        editor.putString("type",t.getType()).clear().commit();
        String gstNo = "";
        if(t.getGstNumber()!=null)
            gstNo = t.getGstNumber();
        editor.putString("gst",gstNo).clear().commit();
        editor.putString("name",t.getName()).commit();
        editor.putString("contactNumber",t.getContactNumber()).commit();
        editor.putString("address",t.getAddress()).commit();
        editor.putString("type",t.getType()).commit();
        String gstNo1 = "";
        if(t.getGstNumber()!=null)
            gstNo1 = t.getGstNumber();
        editor.putString("gst",gstNo1).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage_vehicle,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.manage :
                Intent in = new Intent(this,ManageVehicleActivity.class);
                startActivity(in);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
