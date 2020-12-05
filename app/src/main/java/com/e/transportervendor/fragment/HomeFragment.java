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

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.e.transportervendor.ChatActivity;
import com.e.transportervendor.ProgressBar;
import com.e.transportervendor.R;
import com.e.transportervendor.adapter.HomeCurrentLoadShowAdapter;
import com.e.transportervendor.api.LeadService;
import com.e.transportervendor.api.UserService;
import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.bean.User;
import com.e.transportervendor.databinding.HomeFragmentBinding;
import com.e.transportervendor.databinding.UpdateStatusBinding;
import com.e.transportervendor.utility.InternetUtilityActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    String userToken;
    ProgressBar pd;
    ArrayList<Lead> leadList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = HomeFragmentBinding.inflate(getLayoutInflater());
        if(InternetUtilityActivity.isNetworkConnected(getContext())) {
            try {
                leadApi = LeadService.getTransporterApiIntance();
                currentUserId = FirebaseAuth.getInstance().getUid();
                if(InternetUtilityActivity.isNetworkConnected(getContext())) {
                    Call<ArrayList<Lead>> call = leadApi.getCurrentLoadByTransporterId(currentUserId);
                   pd = new ProgressBar(getActivity());
                   pd.startLoadingDialog();

                    call.enqueue(new Callback<ArrayList<Lead>>() {
                        @Override
                        public void onResponse(Call<ArrayList<Lead>> call, Response<ArrayList<Lead>> response) {
                            leadList = response.body();
                            pd.dismissDialog();
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
                                                    in.putExtra("userId", lead.getUserId());
                                                    in.putExtra("leadId",lead.getLeadId());
                                                    startActivity(in);
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(getContext(), "No Records Found", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), "" + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ArrayList<Lead>> call, Throwable t) {
                            pd.dismissDialog();
                            Toast.makeText(getContext(), ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    getInternetAlert();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "" + e.toString(), Toast.LENGTH_SHORT).show();
            }
        }else{
            getInternetAlert();
        }
        return binding.getRoot();
    }

    private void getInternetAlert() {
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

    private void getStatusAlertDialog(final Lead lead, final int position) {
        UserService.UserApi userApi = UserService.getUserApiInstance();
        Call<User> call = userApi.getUserById(currentUserId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.code() == 200){
                    userToken = response.body().getToken();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
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
                pd = new ProgressBar(getActivity());
                pd.startLoadingDialog();
                if(InternetUtilityActivity.isNetworkConnected(getContext())) {
                    Call<Lead> call = leadApi.updateLeadById(lead.getLeadId(), lead);
                    call.enqueue(new Callback<Lead>() {
                        @Override
                        public void onResponse(Call<Lead> call, Response<Lead> response) {
                            if (response.code() == 200) {
                                try {
                                    pd.dismissDialog();
                                    if (!materialStatus.equalsIgnoreCase("unloaded")) {
                                        Toast.makeText(getContext(), "" + lead.getMaterialStatus() + " Update Successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        leadList.remove(position);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(getContext(), "" + lead.getMaterialStatus() + " Load Completed Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    sendNotification(response.body().getMaterialStatus());
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), "" + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Lead> call, Throwable t) {
                            Toast.makeText(getContext(), "" + t, Toast.LENGTH_SHORT).show();
                            pd.dismissDialog();
                        }
                    });
                    ab.dismiss();
                }else{
                    getInternetAlert();
                }
            }
        });

        ab.show();
    }


    private void sendNotification(String status) {
        try {
            RequestQueue queue = Volley.newRequestQueue(getContext());
            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title", "Material Status");
            data.put("body", "Material " + status);

            JSONObject notification_data = new JSONObject();
            notification_data.put("data", data);
            notification_data.put("to", userToken);


            JsonObjectRequest request = new JsonObjectRequest(url, notification_data, new com.android.volley.Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    String api_key_header_value = "Key=AAAARoiepkM:APA91bHVqjULid8wCt5Sf_EwC4Y0engqgafGEhEdMMhlb2Ix2TbXQldPyAffP7hEPDxLSBoPo1jizb_hX2hFpADDEaNCa5prcG9fR8uPvJt4xEfF-hYQEKmbG8Gn5zouwyRKAXQ98YCZ";
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", api_key_header_value);
                    return headers;
                }
            };
            queue.add(request);
        } catch (Exception e) {
            Toast.makeText(getContext(), "" + e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }


}
