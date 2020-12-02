package com.e.transportervendor.adapter;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e.transportervendor.R;
import com.e.transportervendor.api.LeadService;
import com.e.transportervendor.bean.Bid;
import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.databinding.MarketViewListBinding;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MarketListShowAdapter extends RecyclerView.Adapter<MarketListShowAdapter.MarketListViewHolder> {
    ArrayList<Lead> leadList;
    ArrayList<Bid> bidList;
    String cancel;
    OnRecyclerViewClickListner listner;

    public MarketListShowAdapter(ArrayList<Lead> leadList) {
        cancel = null;
        this.leadList = leadList;
    }

    public MarketListShowAdapter(ArrayList<Bid> bidList, String cancel) {
        this.bidList = bidList;
        this.cancel = cancel;
    }

    @NonNull
    @Override
    public MarketListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MarketViewListBinding binding = MarketViewListBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new MarketListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final MarketListViewHolder holder, int position) {
        if (cancel != null) {
            try {
                holder.binding.btnBid.setText("Cancel");
                final Bid bid = bidList.get(position);
                LeadService.LeadApi leadApi = LeadService.getTransporterApiIntance();
                Call<Lead> call = leadApi.getLead(bid.getLeadId());
                final Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.button_bounce);
                holder.binding.btnBid.setAnimation(animation);
                call.enqueue(new Callback<Lead>() {
                    @Override
                    public void onResponse(Call<Lead> call, Response<Lead> response) {
                        if (response.code() == 200) {
                            try {
                                Lead lead = response.body();
                                String[] pickup = lead.getPickUpAddress().split(",");
                                String[] delivery = lead.getDeliveryAddress().split(",");
                                holder.binding.tvAddress.setText(pickup[1] + " " + pickup[2] + " To " + delivery[1] + " " + delivery[2]);
                                holder.binding.tvExpiryDate.setText(bid.getEstimatedDate());
                                holder.binding.tvWeight.setText(lead.getWeight());
                                holder.binding.tvTypeOfaterial.setText(lead.getTypeOfMaterial());
                                holder.binding.tvUserName.setText(lead.getUserName());
                            } catch (Exception e) {
                                Toast.makeText(holder.itemView.getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Lead> call, Throwable t) {
                        Toast.makeText(holder.itemView.getContext(), "" + t, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Toast.makeText(holder.itemView.getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                holder.binding.btnBid.setText("Bid Now");
                Lead lead = leadList.get(position);
                String[] pickup = lead.getPickUpAddress().split(",");
                String[] delivery = lead.getDeliveryAddress().split(",");
                holder.binding.tvAddress.setText(pickup[1] + " " + pickup[2] + " To " + delivery[1] + " " + delivery[2]);
                holder.binding.tvTypeOfaterial.setText(lead.getTypeOfMaterial());
                holder.binding.tvUserName.setText(lead.getUserName());
                holder.binding.tvWeight.setText(lead.getWeight());
                holder.binding.tvExpiryDate.setText(lead.getDateOfCompletion());
            } catch (Exception e) {
                Toast.makeText(holder.itemView.getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public int getItemCount() {
        if (cancel != null)
            return bidList.size();
        return leadList.size();
    }

    public class MarketListViewHolder extends RecyclerView.ViewHolder {
        MarketViewListBinding binding;

        public MarketListViewHolder(final MarketViewListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.btnBid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String button = binding.btnBid.getText().toString();
                    if (button.equalsIgnoreCase("bid now")) {
                        try {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION && listner != null) {
                                listner.onItemClick(leadList.get(position), position);
                            }
                        } catch (Exception e) {
                            Toast.makeText(itemView.getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else if (button.equalsIgnoreCase("cancel")) {
                        try {
                            Toast.makeText(itemView.getContext(), "Cancel", Toast.LENGTH_SHORT).show();
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION && listner != null) {
                                listner.onCancelButton(bidList.get(position), position);
                            }
                        } catch (Exception e) {
                            Toast.makeText(itemView.getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    public interface OnRecyclerViewClickListner {
        public void onItemClick(Lead lead, int positon);

        public void onCancelButton(Bid bid, int position);
    }

    public void onMarketViewClickLitner(OnRecyclerViewClickListner listner) {
        this.listner = listner;
    }
}
