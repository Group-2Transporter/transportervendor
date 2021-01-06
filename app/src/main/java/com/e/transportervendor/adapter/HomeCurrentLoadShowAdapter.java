package com.e.transportervendor.adapter;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.bean.Vehicle;
import com.e.transportervendor.databinding.CurrentLoadHomeBinding;

import java.util.ArrayList;

public class HomeCurrentLoadShowAdapter extends RecyclerView.Adapter<HomeCurrentLoadShowAdapter.HomeCurrentLoadViewHolder> {
    ArrayList<Lead> leadList;
    OnRecyclerViewClickListner listner;
    public HomeCurrentLoadShowAdapter(ArrayList<Lead> leadList) {
        this.leadList = leadList;
    }

    @NonNull
    @Override
    public HomeCurrentLoadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CurrentLoadHomeBinding binding = CurrentLoadHomeBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new HomeCurrentLoadViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final HomeCurrentLoadViewHolder holder, int position) {
        final Lead lead = leadList.get(position);
        try {
            holder.binding.tvAddress.setText(lead.getPickUpAddress() + " To " + lead.getDeliveryAddress());
            holder.binding.tvTypeOfMaterial.setText(lead.getTypeOfMaterial());
            holder.binding.tvCLDate.setText(lead.getDateOfCompletion());
            holder.binding.tvUserName.setText(lead.getUserName());
            holder.binding.tvWeight.setText(lead.getWeight());
            holder.binding.tvAmount.setText("" + lead.getAmount());
            holder.binding.llLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        String[] pickup = lead.getPickUpAddress().split(",");
                        String[] delivery = lead.getDeliveryAddress().split(",");
                        Uri uri = Uri.parse("https://www.google.co.in/maps/dir/"+pickup[1]+"/"+delivery[1]);
                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                        intent.setPackage("com.google.android.apps.maps");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        holder.itemView.getContext().startActivity(intent);

                    }catch (ActivityNotFoundException e){
                        Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setPackage("com.google.android.apps.maps");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        holder.itemView.getContext().startActivity(intent);
                    }
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

    public class HomeCurrentLoadViewHolder extends RecyclerView.ViewHolder {
        CurrentLoadHomeBinding binding;
        public HomeCurrentLoadViewHolder(final CurrentLoadHomeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.ivMoreVert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(itemView.getContext(),binding.ivMoreVert);
                    final Menu menu = popupMenu.getMenu();
                    menu.add("Update Status");
                    menu.add("Chat with Client");
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            try {
                                String title = menuItem.getTitle().toString();
                                int postion = getAdapterPosition();
                                Lead lead = leadList.get(postion);
                                if (title.equalsIgnoreCase("Update Status")) {
                                    if (postion != RecyclerView.NO_POSITION && listner != null)
                                        listner.onItemClick(lead, postion, title);
                                } else if (title.equalsIgnoreCase("Chat with Client")) {
                                    if (postion != RecyclerView.NO_POSITION && listner != null)
                                        listner.onItemClick(lead, postion, title);
                                }
                            }catch (Exception e){
                                Toast.makeText(itemView.getContext(), ""+e.toString(), Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
        }
    }


    public interface OnRecyclerViewClickListner{
        public void onItemClick(Lead lead, int postion, String status);
    }

    public void setOnRecyclerListener(OnRecyclerViewClickListner listner){
        this.listner = listner;
    }

}
