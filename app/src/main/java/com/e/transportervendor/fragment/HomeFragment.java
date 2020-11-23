package com.e.transportervendor.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.e.transportervendor.ChatActivity;
import com.e.transportervendor.R;
import com.e.transportervendor.adapter.HomeCurrentLoadShowAdapter;
import com.e.transportervendor.api.LeadService;
import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.databinding.HomeFragmentBinding;
import com.e.transportervendor.databinding.UpdateStatusBinding;
import com.e.transportervendor.utility.InternetUtilityActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    HomeCurrentLoadShowAdapter adapter;
    HomeFragmentBinding binding;
    UpdateStatusBinding alertBinding;
    LeadService.LeadApi leadApi;
    String currentUserId;
    String materialStatus;
    ArrayList<Lead> leadList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = HomeFragmentBinding.inflate(getLayoutInflater());
        if(InternetUtilityActivity.isNetworkConnected(getContext())) {
            try {
                leadApi = LeadService.getTransporterApiIntance();
                currentUserId = FirebaseAuth.getInstance().getUid();
                Call<ArrayList<Lead>> call = leadApi.getCurrentLoadByTransporterId(currentUserId);
                call.enqueue(new Callback<ArrayList<Lead>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Lead>> call, Response<ArrayList<Lead>> response) {
                        leadList = response.body();
                        if (response.code() == 200) {
                            try {
                                if (leadApi != null) {
                                    adapter = new HomeCurrentLoadShowAdapter(leadList);
                                    binding.rv.setAdapter(adapter);
                                    binding.rv.setLayoutManager(new LinearLayoutManager(getContext()));
                                    adapter.setOnRecyclerListener(new HomeCurrentLoadShowAdapter.OnRecyclerViewClickListner() {
                                        @Override
                                        public void onItemClick(Lead lead, int postion, String status) {
                                            if (status.equalsIgnoreCase("Update Status")) {
                                                getStatusAlertDialog(lead, postion);
                                            } else if (status.equalsIgnoreCase("Chat with Client")) {
                                                Intent in = new Intent(getContext(), ChatActivity.class);
                                                in.putExtra("userId",lead.getUserId());
                                                startActivity(in);
                                            }
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Toast.makeText(getContext(), "" + e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Lead>> call, Throwable t) {

                    }
                });
            } catch (Exception e) {
                Toast.makeText(getContext(), "" + e.toString(), Toast.LENGTH_SHORT).show();
            }
        }else{
            final androidx.appcompat.app.AlertDialog ab = new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setCancelable(false)
                    .setTitle("Network Not Connected")
                    .setMessage("Please check your network connection")
                    .setPositiveButton("Retry", null)
                    .show();
            Button positive = ab.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            positive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(InternetUtilityActivity.isNetworkConnected(getContext())) {
                        ab.dismiss();
                    }
                }
            });
        }
        return binding.getRoot();
    }

    private void getStatusAlertDialog(final Lead lead, final int position) {
        final AlertDialog ab = new AlertDialog.Builder(getContext()).create();
        alertBinding = UpdateStatusBinding.inflate(LayoutInflater.from(getContext()));
        ab.setView(alertBinding.getRoot());
        ab.setCancelable(false);
        ab.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if(lead.getMaterialStatus().equalsIgnoreCase("loaded")){
            alertBinding.rbLoaded.setChecked(true);
        }else if(lead.getMaterialStatus().equalsIgnoreCase("inTransit")){
            alertBinding.rbInTransit.setChecked(true);
        }else if(lead.getMaterialStatus().equalsIgnoreCase("reached")){
            alertBinding.rbReached.setChecked(true);
        }
        alertBinding.ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ab.dismiss();
            }
        });

        alertBinding.radios.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.rbLoaded){
                    materialStatus = "Loaded";
                }else if(i==R.id.rbInTransit ){
                    materialStatus = "inTransit";
                }else if (i == R.id.rbReached){
                    materialStatus = "Reached";
                }else{
                    materialStatus = "Unloaded";
                }
            }
        });
        alertBinding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lead.setMaterialStatus(materialStatus);
                if(materialStatus.equalsIgnoreCase("Unloaded")) {
                    lead.setStatus("completed");
                }
                final ProgressDialog pd = new ProgressDialog(getContext());
                pd.setMessage("Wait...");
                pd.show();
                pd.setCancelable(false);
                Call<Lead> call = leadApi.updateLeadById(lead.getLeadId(),lead);
                call.enqueue(new Callback<Lead>() {
                    @Override
                    public void onResponse(Call<Lead> call, Response<Lead> response) {
                        if(response.code() ==200){
                            try {
                                pd.dismiss();
                                if (!materialStatus.equalsIgnoreCase("unloaded")) {
                                    Toast.makeText(getContext(), "" + lead.getMaterialStatus() + " Update Successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    leadList.remove(position);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(getContext(), "" + lead.getMaterialStatus() + " Load Completed Successfully", Toast.LENGTH_SHORT).show();
                                }
                            }catch (Exception e){
                                Toast.makeText(getContext(), ""+e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Lead> call, Throwable t) {
                        Toast.makeText(getContext(), ""+t, Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                });
                ab.dismiss();
                pd.show();
            }
        });

        ab.show();
    }



}
