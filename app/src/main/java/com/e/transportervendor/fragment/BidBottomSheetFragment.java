package com.e.transportervendor.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.e.transportervendor.R;
import com.e.transportervendor.adapter.MarketListShowAdapter;
import com.e.transportervendor.api.BidService;
import com.e.transportervendor.api.UserService;
import com.e.transportervendor.bean.Bid;
import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.bean.User;
import com.e.transportervendor.databinding.BidBottomBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BidBottomSheetFragment extends BottomSheetDialogFragment {
    Lead lead;
    String currentUserId;
    String transporterName;
    String rateType;
    ArrayList<Lead> leadList;
    MarketListShowAdapter adapter;
    int position ;
    String userToken;
    double total;
    public BidBottomSheetFragment(Lead lead, String currentUserId, String transporterName, ArrayList<Lead> leadList, MarketListShowAdapter adapter, int position){
        this.lead = lead;
        this.currentUserId =currentUserId;
        this.transporterName = transporterName;
        this.leadList = leadList;
        this.adapter = adapter;
        this.position = position;
        UserService.UserApi userApi = UserService.getUserApiInstance();
        Call<User> call = userApi.getUserById(lead.getUserId());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200){
                    User user = response.body();
                    userToken =  user.getToken();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final BidBottomBinding binding = BidBottomBinding.inflate(inflater);
        try {
            this.getDialog().getWindow().setBackgroundDrawableResource(R.drawable.transparent);

            binding.tvUser.setText(lead.getUserName());
            binding.tvLocation.setText(lead.getPickUpAddress()+" To "+lead.getDeliveryAddress());
            binding.tvTypeOfMaterial.setText("Material : "+lead.getTypeOfMaterial());
            binding.tvWeight.setText("Weight : "+lead.getWeight());
            binding.tvDate.setText("Expiry Date : "+lead.getDateOfCompletion());
            binding.checkedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId){
                        case R.id.btnFixed :
                            rateType = "fixed";
                            binding.tvLayout.setHint("Enter Fixed Price");
                            binding.totalAmount.setVisibility(View.GONE);
                            break;
                        case R.id.btnPerTon:
                            rateType = "perTon";
                            binding.tvLayout.setHint("Enter Per Ton Rate");
                            binding.totalAmount.setVisibility(View.VISIBLE);
                            binding.tvType.setText("Freight Amount (Rate * Number of Tonnes)");
                            break;
                        case R.id.btnKm :
                            rateType = "km";
                            binding.tvLayout.setHint("Enter Per Km Rate");
                            binding.totalAmount.setVisibility(View.VISIBLE);
                            binding.tvType.setText("Freight Amount (Rate * Number of Km)");
                            break;
                    }
                }
            });
            String w[] = lead.getWeight().split(" ");
            final long mult = Integer.parseInt(w[0]);
            binding.etRate.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.length()!=0){
                        long total = Integer.parseInt(s.toString());
                        double amount = mult * total;
                        binding.tvTotal.setText("₹  "+amount);
                    }else{
                        binding.tvTotal.setText("₹  0");
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            final BidService.BidApi bidApi = BidService.getBidApiInstance();
            binding.btnBid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String rate = binding.etRate.getText().toString();
                    if (rate.isEmpty()) {
                        binding.etRate.setError("please enter rate");
                        return;
                    }
                    long amount = Long.parseLong(rate);

                    String remark = binding.etRemark.getText().toString();
                    if (remark.isEmpty()) {
                        binding.etRemark.setError("please enter Remark");
                        return;
                    }
                    Bid bid = new Bid(currentUserId, lead.getLeadId(), transporterName, amount, remark, lead.getDateOfCompletion(), lead.getTypeOfMaterial());

                    Call<Bid> call = bidApi.createBid(bid);
                    call.enqueue(new Callback<Bid>() {

                        @Override
                        public void onResponse(Call<Bid> call, Response<Bid> response) {
                            if (response.code() == 200) {
                                leadList.remove(position);
                                adapter.notifyDataSetChanged();
                                Bid bid = response.body();
                                getUser(bid);
                                Toast.makeText(getContext(), "Bid Created ", Toast.LENGTH_SHORT).show();
                                dismiss();
                            }

                        }

                        @Override
                        public void onFailure(Call<Bid> call, Throwable t) {
                            Toast.makeText(getContext(), "" + t, Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
                    });
                }
            });
            binding.down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }catch (Exception e){
            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return binding.getRoot();
    }
    private void getUser(final Bid bid) {
        try {
            RequestQueue queue = Volley.newRequestQueue(getContext());
            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title", "Send Bid Request");
            data.put("body", "By " + bid.getTransporterName());

            data.put("resultCode","Bid");
            data.put("transporterId",currentUserId);
            data.put("leadId",bid.getLeadId());
            String address = lead.getPickUpAddress()+" To "+lead.getDeliveryAddress();
            data.put("location",address);

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
