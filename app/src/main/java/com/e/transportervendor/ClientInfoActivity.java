
package com.e.transportervendor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.e.transportervendor.adapter.CompletedLeadsShowAdapter;
import com.e.transportervendor.api.LeadService;
import com.e.transportervendor.api.UserService;
import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.bean.User;
import com.e.transportervendor.databinding.ClientInfoBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClientInfoActivity extends AppCompatActivity {
    ClientInfoBinding binding ;
    String leadId;
    UserService.UserApi userApi;
    SharedPreferences language = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ClientInfoBinding.inflate(LayoutInflater.from(this));
        LeadService.LeadApi leadApi = LeadService.getTransporterApiIntance();
        language = getSharedPreferences("Language",MODE_PRIVATE);
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("");
        final ProgressBar pd = new ProgressBar(this);
        pd.startLoadingDialog();
        userApi = UserService.getUserApiInstance();
        Intent intent = getIntent();
        leadId = intent.getStringExtra("leadId");
        leadApi.getLead(leadId).enqueue(new Callback<Lead>() {
            @Override
            public void onResponse(Call<Lead> call, Response<Lead> response) {
                pd.dismissDialog();
                if(response.code() == 200){
                    initialize(response.body());
                }
            }
            @Override
            public void onFailure(Call<Lead> call, Throwable t) {
                pd.dismissDialog();
                Toast.makeText(ClientInfoActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initialize(Lead lead){
        getSupportActionBar().setTitle("");
        userApi.getUserById(lead.getUserId()).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.code() == 200){
                    Picasso.get().load(response.body().getImageUrl()).into(binding.backdrop);
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ClientInfoActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        String lang = language.getString("language","en");
        if(!lang.equalsIgnoreCase("en")) {
            translateText(lead);
        }else {
            binding.tvUserName.setText(lead.getUserName());
            binding.tvMaterial.setText(lead.getTypeOfMaterial());
            binding.tvWeight.setText(lead.getWeight());
            binding.tvAmount.setText("" + lead.getAmount());
            binding.tvMaterialStatus.setText(lead.getMaterialStatus());
            binding.tvPickupContact.setText(lead.getContactForPickup());
            binding.tvDeliveryContact.setText(lead.getContactForDelivery());
            binding.tvPickupAddress.setText(lead.getPickUpAddress());
            binding.tvDeliveryAddress.setText(lead.getDeliveryAddress());
            binding.lastDate.setText("" + this.getResources().getString(R.string.estimated_date) + " :" + lead.getDateOfCompletion());
            binding.tvRemark.setText(lead.getRemark());
            if (!lead.getSecondaryMaterial().isEmpty()) {
                binding.tvSpecialRequirment.setVisibility(View.VISIBLE);
                binding.rlSpecialMaterial.setVisibility(View.VISIBLE);
                binding.specialPickupAddress.setText(lead.getSecondaryPickupAddress());
                binding.specialDeliveryAddress.setText(lead.getSecondaryDeliveryAddress());
                binding.specialMaterial.setText(lead.getSecondaryMaterial());
                binding.specialPickupContact.setText(lead.getSecondaryPickupContact());
                binding.specialDeliveryContact.setText(lead.getSecondaryDeliveryContact());
            } else {
                binding.tvSpecialRequirment.setVisibility(View.GONE);

                binding.rlSpecialMaterial.setVisibility(View.GONE);
            }
            if (lead.isHandleWithCare())
                binding.cardHandleWithCare.setVisibility(View.VISIBLE);
            else
                binding.cardHandleWithCare.setVisibility(View.GONE);
        }
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void translateText(final Lead lead) {
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                //from language
                .setSourceLanguage(FirebaseTranslateLanguage.EN)
                // to language
                .setTargetLanguage(FirebaseTranslateLanguage.HI)
                .build();

        final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance()
                .getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .build();



        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                translator.translate(lead.getPickUpAddress()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        binding.tvPickupAddress.setText(s);
                    }
                });
                translator.translate(lead.getDeliveryAddress()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        binding.tvDeliveryAddress.setText(s);
                    }
                });
                translator.translate(lead.getTypeOfMaterial()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        binding.tvMaterial.setText(s);
                    }
                });
                translator.translate(lead.getUserName()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        binding.tvUserName.setText(s);
                    }
                });
                translator.translate(lead.getSecondaryPickupAddress()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        binding.specialPickupAddress.setText(s);
                    }
                });
                translator.translate(lead.getSecondaryDeliveryAddress()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        binding.specialDeliveryAddress.setText(s);
                    }
                });
                translator.translate(lead.getSecondaryMaterial()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        binding.specialMaterial.setText(s);
                    }
                });
            }
        });
    }


}
