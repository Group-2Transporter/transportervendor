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

import com.e.transportervendor.ManageVehicleActivity;
import com.e.transportervendor.ProgressBar;
import com.e.transportervendor.R;
import com.e.transportervendor.adapter.CompletedLeadsShowAdapter;
import com.e.transportervendor.adapter.MarketListShowAdapter;
import com.e.transportervendor.api.BidService;
import com.e.transportervendor.api.LeadService;
import com.e.transportervendor.bean.Bid;
import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.bean.Vehicle;
import com.e.transportervendor.databinding.DeleteAlertDialogBinding;
import com.e.transportervendor.databinding.FilterAlertBinding;
import com.e.transportervendor.databinding.HistoryFragmentBinding;
import com.e.transportervendor.utility.InternetUtilityActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {
    HistoryFragmentBinding history;
    String currentUserId;
    ProgressBar pd;
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
        pd =new ProgressBar(getActivity());
        pd.startLoadingDialog();

        LeadService.LeadApi leadApi = LeadService.getTransporterApiIntance();
        Call<ArrayList<Lead>> call = leadApi.getAllCompletedLeads(currentUserId);
        call.enqueue(new Callback<ArrayList<Lead>>() {
            @Override
            public void onResponse(Call<ArrayList<Lead>> call, Response<ArrayList<Lead>> response) {
                pd.dismissDialog();
                if (response.code() == 200) {
                    history.rv.setVisibility(View.VISIBLE);
                    history.noRecords.setVisibility(View.GONE);
                    try {
                        ArrayList<Lead> leadList = response.body();
                            CompletedLeadsShowAdapter adapter = new CompletedLeadsShowAdapter(leadList);
                            history.rv.setAdapter(adapter);
                            history.rv.setLayoutManager(new LinearLayoutManager(getContext()));
                    }catch (Exception e){
                        Toast.makeText(getContext(), ""+e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }else if(response.code() == 404){
                    history.rv.setVisibility(View.GONE);
                    history.noRecords.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Lead>> call, Throwable t) {
                pd.dismissDialog();
                Toast.makeText(getContext(), ""+t, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAllPendingBids(){
        pd = new ProgressBar(getActivity());
        pd.startLoadingDialog();
        final BidService.BidApi bidApi = BidService.getBidApiInstance();
        Call<ArrayList<Bid>> call  = bidApi.getAllPendingBids(currentUserId);
        call.enqueue(new Callback<ArrayList<Bid>>() {
            @Override
            public void onResponse(Call<ArrayList<Bid>> call, Response<ArrayList<Bid>> response) {
                pd.dismissDialog();
                if(response.code() == 200) {
                    history.rv.setVisibility(View.VISIBLE);
                    history.noRecords.setVisibility(View.GONE);
                    try {
                        final ArrayList<Bid> bidList = response.body();
                        if (bidList != null && bidList.size()>0) {
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
                                    final AlertDialog ab = new AlertDialog.Builder(getContext()).create();
                                    final DeleteAlertDialogBinding deleteBinding = DeleteAlertDialogBinding.inflate(LayoutInflater.from(getActivity()));
                                    ab.setView(deleteBinding.getRoot());
                                    ab.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                    ab.setCancelable(false);
                                    deleteBinding.tvDeleteLead.setText(getContext().getResources().getString(R.string.cancel_bid));
                                    deleteBinding.btnConfirm.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            pd = new ProgressBar(getActivity());
                                            pd.startLoadingDialog();
                                            if (InternetUtilityActivity.isNetworkConnected(getActivity())) {
                                                Call<Bid> call = bidApi.deleteBid(bid.getBidId());
                                                call.enqueue(new Callback<Bid>() {
                                                    @Override
                                                    public void onResponse(Call<Bid> call, Response<Bid> response) {
                                                        pd.dismissDialog();
                                                        ab.dismiss();
                                                        if (response.code() == 200) {
                                                            try {
                                                                Toast.makeText(getContext(), "Cancel Bid  Successfully", Toast.LENGTH_SHORT).show();
                                                                bidList.remove(position);
                                                                if (bidList.size() == 0) {
                                                                    history.rv.setVisibility(View.GONE);
                                                                    history.noRecords.setVisibility(View.VISIBLE);
                                                                }
                                                                adapter.notifyDataSetChanged();
                                                            } catch (Exception e) {
                                                                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<Bid> call, Throwable t) {
                                                        pd.dismissDialog();
                                                        ab.dismiss();
                                                        Toast.makeText(getContext(), "" + t, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                getInternetAlert();
                                            }
                                        }
                                    });
                                    deleteBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            ab.dismiss();
                                        }
                                    });
                                    ab.show();
                                }
                            });
                        }else{
                            history.rv.setVisibility(View.GONE);
                            history.noRecords.setVisibility(View.VISIBLE);
                        }
                    }catch (Exception e){
                        Toast.makeText(getContext(), ""+e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }else if(response.code() == 404){
                    history.rv.setVisibility(View.GONE);
                    history.noRecords.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Bid>> call, Throwable t) {
                pd.dismissDialog();
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
