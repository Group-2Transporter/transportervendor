package com.e.transportervendor.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.e.transportervendor.ProgressBar;
import com.e.transportervendor.adapter.FilterAdapter;
import com.e.transportervendor.adapter.MarketListShowAdapter;
import com.e.transportervendor.api.LeadService;
import com.e.transportervendor.api.StatesService;
import com.e.transportervendor.bean.Bid;
import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.bean.States;
import com.e.transportervendor.databinding.FilterAlertBinding;
import com.e.transportervendor.databinding.MarketFragmentBinding;
import com.e.transportervendor.utility.InternetUtilityActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MarketFragment extends Fragment {

    LeadService.LeadApi leadApi;
    MarketFragmentBinding market;
    ArrayList<States> state;
    ArrayList<States> stateList;
    String currentUserId;
    String name;
    ProgressBar pd;
    MarketListShowAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        market = MarketFragmentBinding.inflate(getLayoutInflater());
        getStateList();
        if (InternetUtilityActivity.isNetworkConnected(getContext())) {
            try {
                SharedPreferences sp = getActivity().getSharedPreferences("transporter", Context.MODE_PRIVATE);
                name = sp.getString("name", "");
                leadApi = LeadService.getTransporterApiIntance();
                currentUserId = FirebaseAuth.getInstance().getUid();
                stateList = new ArrayList<>();
                market.filter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (InternetUtilityActivity.isNetworkConnected(getContext())) {
                            getFilterAlertDialog();
                        } else {
                            getInternetAlert();
                        }
                    }
                });
                if (InternetUtilityActivity.isNetworkConnected(getContext())) {
                    getAllCreatedLeads(currentUserId);
                } else {
                    getInternetAlert();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "" + e.toString(), Toast.LENGTH_SHORT).show();
            }
        } else {
            getInternetAlert();
        }
        return market.getRoot();
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
                if (InternetUtilityActivity.isNetworkConnected(getContext())) {
                    ab.dismiss();
                }
            }
        });
    }

    private void getAllCreatedLeads(final String currentUserId) {
        try {
            Call<ArrayList<Lead>> call = leadApi.getAllCreatedLeads(currentUserId);
            pd = new ProgressBar(getActivity());
            pd.startLoadingDialog();
            call.enqueue(new Callback<ArrayList<Lead>>() {
                public void onResponse(Call<ArrayList<Lead>> call, Response<ArrayList<Lead>> response) {
                    pd.dismissDialog();
                    if (response.code() == 200) {
                        market.noRecords.setVisibility(View.GONE);
                        market.rv.setVisibility(View.VISIBLE);
                        final ArrayList<Lead> bidList = response.body();
                        try {
                            adapter = new MarketListShowAdapter(bidList);
                            market.rv.setAdapter(adapter);
                            market.rv.setLayoutManager(new LinearLayoutManager(getContext()));
                            adapter.onMarketViewClickLitner(new MarketListShowAdapter.OnRecyclerViewClickListner() {
                                @Override
                                public void onItemClick(Lead lead, final int positon) {
                                    BidBottomSheetFragment bottomSheetFragment = new BidBottomSheetFragment(lead, currentUserId, name, bidList, adapter, positon);
                                    bottomSheetFragment.show(getFragmentManager(), "");
                                    if(bidList.size() == 0){
                                        market.noRecords.setVisibility(View.VISIBLE);
                                        market.rv.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelButton(Bid bid, int position) {

                                }
                            });
                        } catch (Exception e) {
                            pd.dismissDialog();
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else if (response.code() == 404) {
                        market.rv.setVisibility(View.GONE);
                        market.noRecords.setVisibility(View.VISIBLE);
                    }else if(response.code() == 500){
                        Toast.makeText(getContext(), "Server not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<Lead>> call, Throwable t) {
                    pd.dismissDialog();
                    Toast.makeText(getContext(), "" + t, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "" + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
    private void getStateList(){
        StatesService.StatesApi statesApi = StatesService.getStatesApiInstance();
        statesApi.getStateList().enqueue(new Callback<ArrayList<States>>() {
            @Override
            public void onResponse(Call<ArrayList<States>> call, Response<ArrayList<States>> response) {
                if (response.code() == 200 ){
                        state = response.body();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<States>> call, Throwable t) {

            }
        });
    }

    private void getFilterAlertDialog() {

        final AlertDialog ab = new AlertDialog.Builder(getContext()).create();
        final FilterAlertBinding filterAlert = FilterAlertBinding.inflate(LayoutInflater.from(getContext()));
        ab.setView(filterAlert.getRoot());
        ab.setCancelable(false);
        final FilterAdapter adapter = new FilterAdapter(getContext(), state);
        filterAlert.rv.setAdapter(adapter);

        filterAlert.ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateList = adapter.getSelectedState();
                for(States s : stateList){
                    s.setCheck(false);
                }
                ab.dismiss();
            }
        });
        filterAlert.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateList = adapter.getSelectedState();
                getAllBids(stateList);
                ab.dismiss();
                for(States s : stateList){
                    s.setCheck(false);
                }

            }
        });
        filterAlert.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<States> result = new ArrayList<>();

                for (States s : state){
                    if(s.getStateName().contains(newText)){
                        result.add(s);
                    }
                }
                adapter.update(result);

                return false;
            }
        });

        ab.show();
    }

    private void getAllBids(ArrayList<States> stateList) {
        try {
            Call<ArrayList<Lead>> call = leadApi.getFilterLeads(currentUserId, stateList);
            pd = new ProgressBar(getActivity());
            pd.startLoadingDialog();
            call.enqueue(new Callback<ArrayList<Lead>>() {
                @Override
                public void onResponse(Call<ArrayList<Lead>> call, Response<ArrayList<Lead>> response) {
                    pd.dismissDialog();
                    if (response.code() == 200) {
                        market.noRecords.setVisibility(View.GONE);
                        market.rv.setVisibility(View.VISIBLE);
                        final ArrayList<Lead> bidList = response.body();
                        try {
                            adapter = new MarketListShowAdapter(bidList);
                            market.rv.setAdapter(adapter);
                            market.rv.setLayoutManager(new LinearLayoutManager(getContext()));
                            adapter.onMarketViewClickLitner(new MarketListShowAdapter.OnRecyclerViewClickListner() {
                                @Override
                                public void onItemClick(Lead lead, final int positon) {
                                    BidBottomSheetFragment bottomSheetFragment = new BidBottomSheetFragment(lead, currentUserId, name, bidList, adapter, positon);
                                    bottomSheetFragment.show(getFragmentManager(), "");
                                    if(bidList.size() == 0){
                                        market.noRecords.setVisibility(View.VISIBLE);
                                        market.rv.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelButton(Bid bid, int position) {

                                }
                            });
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else if (response.code() == 404) {
                        market.noRecords.setVisibility(View.VISIBLE);
                        market.rv.setVisibility(View.GONE);
                    } else if (response.code() == 500) {
                        Toast.makeText(getContext(), "Server not respond", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<Lead>> call, Throwable t) {
                    pd.dismissDialog();
                    Toast.makeText(getContext(), "" + t, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "" + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

}

