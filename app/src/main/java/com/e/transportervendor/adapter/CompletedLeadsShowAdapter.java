package com.e.transportervendor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e.transportervendor.api.LeadService;
import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.databinding.HistoryCompletedBinding;

import java.util.ArrayList;

public class CompletedLeadsShowAdapter extends RecyclerView.Adapter<CompletedLeadsShowAdapter.CompletedViewHolder> {
    ArrayList<Lead> leadList;
    public CompletedLeadsShowAdapter(ArrayList<Lead> leadList){
        this.leadList = leadList;
    }

    @NonNull
    @Override
    public CompletedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        HistoryCompletedBinding binding = HistoryCompletedBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new CompletedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CompletedViewHolder holder, int position) {
        try {
            Lead lead = leadList.get(position);
            String[] pickup = lead.getPickUpAddress().split(",");
            String[] delivery = lead.getDeliveryAddress().split(",");
            holder.binding.tvAddress.setText(pickup[2] + " To " + delivery[2]);
            holder.binding.tvAmount.setText("" + lead.getAmount());
            holder.binding.tvDate.setText(lead.getDateOfCompletion());
            holder.binding.tvTypeOfMaterial.setText(lead.getTypeOfMaterial());
            holder.binding.tvUserName.setText(lead.getUserName());
            holder.binding.tvWeight.setText(lead.getWeight());
        }catch (Exception e){
            Toast.makeText(holder.itemView.getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public int getItemCount() {
        return leadList.size();
    }

    public class CompletedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener , View.OnLongClickListener {
        HistoryCompletedBinding binding;
        public CompletedViewHolder(HistoryCompletedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(itemView.getContext(), "Hello", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onLongClick(View v) {
            Toast.makeText(itemView.getContext(), "Pressed", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

}
