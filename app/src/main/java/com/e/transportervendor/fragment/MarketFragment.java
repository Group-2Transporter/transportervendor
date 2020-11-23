package com.e.transportervendor.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.e.transportervendor.adapter.FilterAdapter;
import com.e.transportervendor.adapter.MarketListShowAdapter;
import com.e.transportervendor.api.LeadService;
import com.e.transportervendor.bean.Bid;
import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.bean.State;
import com.e.transportervendor.databinding.FilterAlertBinding;
import com.e.transportervendor.databinding.MarketFragmentBinding;
import com.e.transportervendor.utility.InternetUtilityActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MarketFragment extends Fragment {

    LeadService.LeadApi leadApi;
    MarketFragmentBinding market;
    ArrayList<State> state;
    ArrayList<State> stateList;
    String currentUserId;
    String name;
    MarketListShowAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        market = MarketFragmentBinding.inflate(getLayoutInflater());
        if(InternetUtilityActivity.isNetworkConnected(getContext())) {
            try {
                SharedPreferences sp = getActivity().getSharedPreferences("transporter",Context.MODE_PRIVATE);
                name = sp.getString("name","");
                leadApi = LeadService.getTransporterApiIntance();
                currentUserId = FirebaseAuth.getInstance().getUid();
                stateList = new ArrayList<>();
                market.filter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getFilterAlertDialog();
                    }
                });
                getAllCreatedLeads(currentUserId);
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
        return market.getRoot();
    }

    private void getAllCreatedLeads(final String currentUserId) {
        try {
            Call<ArrayList<Lead>> call = leadApi.getAllCreatedLeads(currentUserId);
            call.enqueue(new Callback<ArrayList<Lead>>() {
                @Override
                public void onResponse(Call<ArrayList<Lead>> call, Response<ArrayList<Lead>> response) {
                    if (response.code() == 200) {
                        final ArrayList<Lead> bidList = response.body();
                        if(bidList.size() != 0) {
                            try {
                                adapter = new MarketListShowAdapter(bidList);
                                market.rv.setAdapter(adapter);
                                market.rv.setLayoutManager(new LinearLayoutManager(getContext()));
                                adapter.onMarketViewClickLitner(new MarketListShowAdapter.OnRecyclerViewClickListner() {
                                    @Override
                                    public void onItemClick(Lead lead, final int positon) {
                                        BidBottomSheetFragment bottomSheetFragment = new BidBottomSheetFragment(lead, currentUserId, name,bidList,adapter,positon);
                                        bottomSheetFragment.show(getFragmentManager(), "");

                                    }

                                    @Override
                                    public void onMoreSelected(Bid bid, int position) {

                                    }
                                });
                            }catch (Exception e){
                                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }else if(response.code() == 404){
                        Toast.makeText(getContext(), "Match Bid Not Found", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ArrayList<Lead>> call, Throwable t) {
                    Toast.makeText(getContext(), "" + t, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "" + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    ProgressDialog pd;
    private void getFilterAlertDialog() {
        state = new ArrayList<>();
        state.add(new State("West Bengal"));
        state.add(new State("Andhra Pradesh"));
        state.add(new State("Arunachal Pradesh"));
        state.add(new State("Assam"));
        state.add(new State("Bihar"));
        state.add(new State("Chhattisgarh"));
        state.add(new State("Goa"));
        state.add(new State("Gujarat"));
        state.add(new State("Haryana"));
        state.add(new State("Himachal Pradesh"));
        state.add(new State("Jharkhand"));
        state.add(new State("Karnataka"));
        state.add(new State("Kerala"));
        state.add(new State("Madhya Pradesh"));
        state.add(new State("Maharashtra"));
        state.add(new State("Manipur"));
        state.add(new State("West Bengal"));
        final AlertDialog ab = new AlertDialog.Builder(getContext()).create();
        final FilterAlertBinding filterAlert = FilterAlertBinding.inflate(LayoutInflater.from(getContext()));
        ab.setView(filterAlert.getRoot());
        ab.setCancelable(false);
        final FilterAdapter adapter = new FilterAdapter(getContext(),state);
        filterAlert.rv.setAdapter(adapter);
        filterAlert.ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ab.dismiss();
            }
        });
        filterAlert.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateList = adapter.getSelectedState();
                pd = new ProgressDialog(getContext());
                pd.setMessage("Wait..");
                pd.setCancelable(false);
                getAllBids(stateList);
                pd.dismiss();
                ab.dismiss();
            }
        });
        ab.show();
    }

    private void getAllBids(ArrayList<State> stateList) {
        try {
            Call<ArrayList<Lead>> call = leadApi.getFilterLeads(currentUserId,stateList);
            call.enqueue(new Callback<ArrayList<Lead>>() {
                @Override
                public void onResponse(Call<ArrayList<Lead>> call, Response<ArrayList<Lead>> response) {
                    if (response.code() == 200) {
                        final ArrayList<Lead> bidList = response.body();
                        if(bidList.size() != 0) {
                            try {
                                adapter = new MarketListShowAdapter(bidList);
                                market.rv.setAdapter(adapter);
                                market.rv.setLayoutManager(new LinearLayoutManager(getContext()));
                                adapter.onMarketViewClickLitner(new MarketListShowAdapter.OnRecyclerViewClickListner() {
                                    @Override
                                    public void onItemClick(Lead lead, final int positon) {
                                        BidBottomSheetFragment bottomSheetFragment = new BidBottomSheetFragment(lead, currentUserId, name,bidList,adapter,positon);
                                        bottomSheetFragment.show(getFragmentManager(), "");

                                    }

                                    @Override
                                    public void onMoreSelected(Bid bid, int position) {

                                    }
                                });
                            }catch (Exception e){
                                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }else if(response.code() == 404){
                        Toast.makeText(getContext(), "Match Bid Not Found", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ArrayList<Lead>> call, Throwable t) {

                    Toast.makeText(getContext(), "" + t, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "" + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

}

