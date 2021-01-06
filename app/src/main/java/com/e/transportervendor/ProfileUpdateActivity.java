package com.e.transportervendor;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.e.transportervendor.api.BidService;
import com.e.transportervendor.api.LeadService;
import com.e.transportervendor.api.TransporterServices;
import com.e.transportervendor.bean.Bid;
import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.bean.Transporter;
import com.e.transportervendor.databinding.CreateProfileUpdateActivityBinding;
import com.e.transportervendor.utility.FileUtils;
import com.e.transportervendor.utility.InternetUtilityActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileUpdateActivity extends AppCompatActivity {
    CreateProfileUpdateActivityBinding profileBinding;
    SharedPreferences sp;
    String imageUrl;
    Uri imageUri;
    String[] separated;
    ProgressBar pd;
    String category = "";
    boolean gstVisibility = false;
    TransporterServices.TransportApi transportApi;
    String currentUserId;
    Transporter transporter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transportApi = TransporterServices.getTransporterApiIntance();
        profileBinding = CreateProfileUpdateActivityBinding.inflate(LayoutInflater.from(this));
        setContentView(profileBinding.getRoot());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 111);
        }
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        sp = getSharedPreferences("transporter",MODE_PRIVATE);
        profileBinding.etUserName.setText(sp.getString("name",""));
        String gs = sp.getString("gst","not_found");
        if(!gs.equalsIgnoreCase("not_found"))
        profileBinding.etGstNum.setText(gs);
        String currentString = sp.getString("address","");
        separated = currentString.split(",");
        profileBinding.etStreetAdrees.setText(separated[0]);
        profileBinding.etCityAddress.setText(separated[1]);
        profileBinding.etStateAdress.setText(separated[2]);
        profileBinding.etPhoneNumber.setText(sp.getString("contactNumber",""));
        imageUrl=sp.getString("image","");
        Picasso.get().load(imageUrl).into(profileBinding.civProfile);
        getTransporterThroughApi();
        setLoadsDetails();
        String compareValue = sp.getString("type","not_found");
        Toast.makeText(this, ""+compareValue, Toast.LENGTH_SHORT).show();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.transporterType, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileBinding.transporterCategory.setAdapter(adapter);
        if (!compareValue.equalsIgnoreCase("not_found")) {
            int spinnerPosition = adapter.getPosition(compareValue);
            profileBinding.transporterCategory.setSelection(spinnerPosition);
        }
        profileBinding.civProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent();
                in.setAction(Intent.ACTION_GET_CONTENT);
                in.setType("image/*");
                startActivityForResult(in,111);
            }
        });



        profileBinding.transporterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                category = profileBinding.transporterCategory.getSelectedItem().toString();
                Toast.makeText(ProfileUpdateActivity.this, category, Toast.LENGTH_SHORT).show();
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

        if(InternetUtilityActivity.isNetworkConnected(this)) {
            profileBinding.btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(category.equalsIgnoreCase("Select category")){
                        Toast.makeText(ProfileUpdateActivity.this, "Please select Category", Toast.LENGTH_SHORT).show();
                        return;
                    }

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
                    String token = FirebaseInstanceId.getInstance().getToken();
                    String address = streetAddress + "," + cityAddress + "," + stateAddress;
                    transporter.setToken(token);
                    transporter.setAddress(address);
                    transporter.setContactNumber(contact);
                    transporter.setGstNumber(gstNumber);
                    transporter.setName(name);
                    transporter.setType(category);

                    Call<Transporter> call = transportApi.updateTransporter(transporter);
                    pd = new ProgressBar(ProfileUpdateActivity.this);
                    pd.startLoadingDialog();
                    call.enqueue(new Callback<Transporter>() {
                        @Override
                        public void onResponse(Call<Transporter> call, Response<Transporter> response) {
                            try {
                                int status = response.code();
                                pd.dismissDialog();
                                if (status == 200) {
                                    Transporter t = response.body();
                                    saveTransporterLocally(t);
                                    Toast.makeText(ProfileUpdateActivity.this, "Update Successfully", Toast.LENGTH_SHORT).show();
                                } else if (status == 500) {
                                    Toast.makeText(ProfileUpdateActivity.this, "Failed..", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(ProfileUpdateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Transporter> call, Throwable t) {
                            Toast.makeText(ProfileUpdateActivity.this, "Something went wrong : " + t, Toast.LENGTH_SHORT).show();
                            pd.dismissDialog();
                        }
                    });
                }
            });
        }else{
            getInternetAlert();
        }
        profileBinding.manageVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(ProfileUpdateActivity.this,ManageVehicleActivity.class);
                startActivity(in);
            }
        });

        profileBinding.completedLoads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(ProfileUpdateActivity.this,HistoryActivity.class);
                startActivity(in);
            }
        });

        getChangeListner();
        setSupportActionBar(profileBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void getChangeListner() {
        profileBinding.tvUserName.setText(sp.getString("name",""));
        profileBinding.etUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()!=0)
                    profileBinding.tvUserName.setText(s);
                else
                    profileBinding.tvUserName.setText("");
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        profileBinding.tvStreetAddress.setText(separated[0]);
        profileBinding.etStreetAdrees.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()!=0)
                    profileBinding.tvStreetAddress.setText(s);
                else
                    profileBinding.tvStreetAddress.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        profileBinding.tvCityAddress.setText(separated[1]);
        profileBinding.etCityAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()!=0)
                    profileBinding.tvCityAddress.setText(s);
                else
                    profileBinding.tvCityAddress.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        profileBinding.tvStateAddress.setText(separated[2]);
        profileBinding.etStateAdress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()!=0)
                    profileBinding.tvStateAddress.setText(s);
                else
                    profileBinding.tvStateAddress.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void getInternetAlert() {
        final AlertDialog ab = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Network Not Connected")
                .setMessage("Please check your network connection")
                .setPositiveButton("Retry", null)
                .show();
        Button positive = ab.getButton(AlertDialog.BUTTON_POSITIVE);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (InternetUtilityActivity.isNetworkConnected(ProfileUpdateActivity.this)) {
                    ab.dismiss();
                }
            }
        });
    }


    private  void getTransporterThroughApi(){
        try {
            Call<Transporter> transporterCall = transportApi.getTransporterVehicleList(currentUserId);
            transporterCall.enqueue(new Callback<Transporter>() {
                @Override
                public void onResponse(Call<Transporter> call, Response<Transporter> response) {
                    try {
                        if (response.code() == 200) {
                            transporter = response.body();
                        }
                    }catch (Exception e){
                        Toast.makeText(ProfileUpdateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Transporter> call, Throwable t) {
                    Toast.makeText(ProfileUpdateActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                File file = FileUtils.getFile(ProfileUpdateActivity.this, imageUri);
                RequestBody requestFile =
                        RequestBody.create(
                                MediaType.parse(getContentResolver().getType(imageUri)),
                                file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                RequestBody transporterId = RequestBody.create(MultipartBody.FORM, currentUserId);
                Toast.makeText(this, "come on ", Toast.LENGTH_SHORT).show();
                Call<Transporter> call = transportApi.updateImageTransporter(body, transporterId);
                pd = new ProgressBar(this);
                pd.startLoadingDialog();
                call.enqueue(new Callback<Transporter>() {
                    @Override
                    public void onResponse(Call<Transporter> call, Response<Transporter> response) {
                        try {
                            pd.dismissDialog();
                            if (response.code() == 200) {
                                profileBinding.civProfile.setImageURI(imageUri);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("image","").clear().commit();
                                editor.putString("image",imageUrl).commit();
                                Toast.makeText(ProfileUpdateActivity.this, "Image Change SuccessFully", Toast.LENGTH_SHORT).show();
                            } else if (response.code() == 500) {
                                Toast.makeText(ProfileUpdateActivity.this, "Failed To Upload Image", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(ProfileUpdateActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Transporter> call, Throwable t) {
                        Toast.makeText(ProfileUpdateActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        pd.dismissDialog();
                    }
                });
            }catch (Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }
    private void setLoadsDetails(){
        LeadService.LeadApi leadApi = LeadService.getTransporterApiIntance();
        leadApi.getCurrentLoadByTransporterId(currentUserId).enqueue(new Callback<ArrayList<Lead>>() {
            @Override
            public void onResponse(Call<ArrayList<Lead>> call, Response<ArrayList<Lead>> response) {
                if(response.code() == 200){
                    profileBinding.active.setText(""+response.body().size());
                }else if(response.code() == 404){
                    profileBinding.active.setText("0");
                }else{
                    Toast.makeText(ProfileUpdateActivity.this, "Something is wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Lead>> call, Throwable t) {
                Toast.makeText(ProfileUpdateActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        BidService.BidApi bidApi = BidService.getBidApiInstance();
        bidApi.getAllPendingBids(currentUserId).enqueue(new Callback<ArrayList<Bid>>() {
            @Override
            public void onResponse(Call<ArrayList<Bid>> call, Response<ArrayList<Bid>> response) {
                if(response.code() == 200){
                    profileBinding.pending.setText(""+response.body().size());
                }else if(response.code() == 404){
                    profileBinding.pending.setText("0");
                }else{
                    Toast.makeText(ProfileUpdateActivity.this, "Something is wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Bid>> call, Throwable t) {
                Toast.makeText(ProfileUpdateActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        leadApi.getAllCompletedLeads(currentUserId).enqueue(new Callback<ArrayList<Lead>>() {
            @Override
            public void onResponse(Call<ArrayList<Lead>> call, Response<ArrayList<Lead>> response) {
                if(response.code() == 200){
                    profileBinding.complelte.setText(""+response.body().size());
                }else if(response.code() == 404){
                    profileBinding.complelte.setText("0");
                }else{
                    Toast.makeText(ProfileUpdateActivity.this, "Something is wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Lead>> call, Throwable t) {
                Toast.makeText(ProfileUpdateActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
}
