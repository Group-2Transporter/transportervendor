package com.e.transportervendor.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e.transportervendor.R;
import com.e.transportervendor.adapter.CompletedLeadsShowAdapter;
import com.e.transportervendor.adapter.MarketListShowAdapter;
import com.e.transportervendor.api.BidService;
import com.e.transportervendor.api.LeadService;
import com.e.transportervendor.bean.Bid;
import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.databinding.FilterAlertBinding;
import com.e.transportervendor.databinding.HistoryFragmentBinding;
import com.e.transportervendor.utility.InternetUtilityActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {
    HistoryFragmentBinding history;
    String currentUserId;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        history  = HistoryFragmentBinding.inflate(getLayoutInflater());
        try {
            currentUserId = FirebaseAuth.getInstance().getUid();
            final ArrayList<String> list = new ArrayList<>();
            list.add("Completed Loads");
            list.add("Pending Bids");
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, list);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            history.spinner.setAdapter(arrayAdapter);
            history.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String spin = spin = history.spinner.getSelectedItem().toString();
                    try {
                        if(InternetUtilityActivity.isNetworkConnected(getContext())) {
                            if (spin.equals("Completed Loads")) {
                                getAllCompletedLeads();
                            } else if (spin.equals("Pending Bids")) {
                                getAllPendingBids();
                            }
                        }else{
                            getInternetAlert();
                        }
                    }catch (Exception e){
                        Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        }catch (Exception e){
            Toast.makeText(getContext(), ""+e.toString(), Toast.LENGTH_SHORT).show();
        }

        return history.getRoot();
    }

    private void getAllCompletedLeads(){
        LeadService.LeadApi leadApi = LeadService.getTransporterApiIntance();
        Call<ArrayList<Lead>> call = leadApi.getAllCompletedLeads(currentUserId);
        call.enqueue(new Callback<ArrayList<Lead>>() {
            @Override
            public void onResponse(Call<ArrayList<Lead>> call, Response<ArrayList<Lead>> response) {
                if (response.code() == 200) {
                    try {
                        ArrayList<Lead> leadList = response.body();
                        if (leadList != null) {
                            CompletedLeadsShowAdapter adapter = new CompletedLeadsShowAdapter(leadList);
                            history.rv.setAdapter(adapter);
                            history.rv.setLayoutManager(new LinearLayoutManager(getContext()));
                        }
                    }catch (Exception e){
                        Toast.makeText(getContext(), ""+e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Lead>> call, Throwable t) {
                Toast.makeText(getContext(), ""+t, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAllPendingBids(){
        final BidService.BidApi bidApi = BidService.getBidApiInstance();
        Call<ArrayList<Bid>> call  = bidApi.getAllPendingBids(currentUserId);
        call.enqueue(new Callback<ArrayList<Bid>>() {
            @Override
            public void onResponse(Call<ArrayList<Bid>> call, Response<ArrayList<Bid>> response) {
                if(response.code() == 200) {
                    try {
                        final ArrayList<Bid> bidList = response.body();
                        if (bidList != null) {
                            final MarketListShowAdapter adapter = new MarketListShowAdapter(bidList, "cancel");
                            history.rv.setAdapter(adapter);
                            history.rv.setLayoutManager(new LinearLayoutManager(getContext()));
                            adapter.onMarketViewClickLitner(new MarketListShowAdapter.OnRecyclerViewClickListner() {
                                @Override
                                public void onItemClick(Lead lead, int positon) {
                                    //lead code
                                }

                                @Override
                                public void onCancelButton(final Bid bid, final int position) {
                                    AlertDialog.Builder ab = new AlertDialog.Builder(getContext());
                                    ab.setMessage("Are you sure to Cancel bid ?");
                                    ab.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(InternetUtilityActivity.isNetworkConnected(getContext())) {
                                                Call<Bid> call = bidApi.deleteBid(bid.getBidId());
                                                call.enqueue(new Callback<Bid>() {
                                                    @Override
                                                    public void onResponse(Call<Bid> call, Response<Bid> response) {
                                                        if (response.code() == 200) {
                                                            try {
                                                                Toast.makeText(getContext(), "Cancel Bid  Successfully", Toast.LENGTH_SHORT).show();
                                                                bidList.remove(position);
                                                                adapter.notifyDataSetChanged();
                                                            } catch (Exception e) {
                                                                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<Bid> call, Throwable t) {
                                                        Toast.makeText(getContext(), "" + t, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }else{
                                                getInternetAlert();
                                            }
                                        }
                                    });
                                    ab.setNegativeButton("Cancel", null);
                                    ab.show();
                                }
                            });
                        }
                    }catch (Exception e){
                        Toast.makeText(getContext(), ""+e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Bid>> call, Throwable t) {
                Toast.makeText(getContext(), ""+t, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getInternetAlert(){
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



}
