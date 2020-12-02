package com.e.transportervendor.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e.transportervendor.api.LeadService;
import com.e.transportervendor.bean.Bid;
import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.databinding.HistoryCompletedBinding;
import com.e.transportervendor.utility.InternetUtilityActivity;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompletedLeadsShowAdapter extends RecyclerView.Adapter<CompletedLeadsShowAdapter.CompletedViewHolder>  {
    ArrayList<Lead> leadList;
    LeadService.LeadApi leadApi;
    Context context;
    public CompletedLeadsShowAdapter(ArrayList<Lead> leadList){
        this.leadList = leadList;
        leadApi = LeadService.getTransporterApiIntance();
    }

    @NonNull
    @Override
    public CompletedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        HistoryCompletedBinding binding = HistoryCompletedBinding.inflate(LayoutInflater.from(parent.getContext()));
        context = parent.getContext();
        return new CompletedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final CompletedViewHolder holder, int position) {
        try {
            final Lead lead = leadList.get(position);
            String[] pickup = lead.getPickUpAddress().split(",");
            String[] delivery = lead.getDeliveryAddress().split(",");
            holder.binding.tvAddress.setText(pickup[2] + " To " + delivery[2]);
            holder.binding.tvAmount.setText("" + lead.getAmount());
            holder.binding.tvDate.setText(lead.getDateOfCompletion());
            holder.binding.tvTypeOfMaterial.setText(lead.getTypeOfMaterial());
            holder.binding.tvUserName.setText(lead.getUserName());
            holder.binding.tvWeight.setText(lead.getWeight());
            holder.binding.card .setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(),holder.binding.card);
                    final Menu menu = popupMenu.getMenu();
                    menu.add("Delete");
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            try {
                                String title = menuItem.getTitle().toString();
                                if (title.equalsIgnoreCase("Delete")) {

                                    AlertDialog.Builder ab = new AlertDialog.Builder(holder.itemView.getContext());
                                    ab.setMessage("Are you sure to Cancel bid ?");
                                    ab.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(InternetUtilityActivity.isNetworkConnected(holder.itemView.getContext())) {
                                                leadApi.deleteLead(lead.getLeadId()).enqueue(new Callback<Lead>() {
                                                    @Override
                                                    public void onResponse(Call<Lead> call, Response<Lead> response) {
                                                        if(response.code() == 200){
                                                            Toast.makeText(holder.itemView.getContext(), "Completed Load Deleted", Toast.LENGTH_SHORT).show();
                                                            notifyDataSetChanged();
                                                        }else{
                                                            Toast.makeText(context, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<Lead> call, Throwable t) {
                                                        Toast.makeText(context, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
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
                            }catch (Exception e){
                                Toast.makeText(holder.itemView.getContext(), ""+e.toString(), Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        }
                    });
                    popupMenu.show();

                    return false;
                }
            });
        }catch (Exception e){
            Toast.makeText(holder.itemView.getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public int getItemCount() {
        return leadList.size();
    }

    public class CompletedViewHolder extends RecyclerView.ViewHolder {
        HistoryCompletedBinding binding;

        public CompletedViewHolder(HistoryCompletedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    private void getInternetAlert(){
        final androidx.appcompat.app.AlertDialog ab = new androidx.appcompat.app.AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle("Network Not Connected")
                .setMessage("Please check your network connection")
                .setPositiveButton("Retry", null)
                .show();
        Button positive = ab.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(InternetUtilityActivity.isNetworkConnected(context)) {
                    ab.dismiss();
                }
            }
        });
    }

}
