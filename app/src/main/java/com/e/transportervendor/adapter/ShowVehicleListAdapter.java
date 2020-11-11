package com.e.transportervendor.adapter;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e.transportervendor.bean.Vehicle;
import com.e.transportervendor.databinding.ManageVehicleListBinding;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ShowVehicleListAdapter extends RecyclerView.Adapter<ShowVehicleListAdapter.VehicleListViewHolder> {
    List<Vehicle> vehicleList;
    OnRecyclerViewClick listner;
    public ShowVehicleListAdapter(List<Vehicle> vehicleList){
        this.vehicleList = vehicleList;
    }

    @NonNull
    @Override
    public VehicleListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ManageVehicleListBinding binding = ManageVehicleListBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new VehicleListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final VehicleListViewHolder holder, int position) {
        Vehicle v = vehicleList.get(position);
        Picasso.get().load(v.getImageUrl()).into(holder.vehicleListBinding.ivVehicle);
        holder.vehicleListBinding.tvVehicleName.setText(v.getName());
        holder.vehicleListBinding.tvVehicleNumber.setText(""+v.getCount());

    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    public class VehicleListViewHolder extends RecyclerView.ViewHolder {
        ManageVehicleListBinding vehicleListBinding;
        public VehicleListViewHolder(final ManageVehicleListBinding vehicleListBinding) {
            super(vehicleListBinding.getRoot());
            this.vehicleListBinding = vehicleListBinding;
            vehicleListBinding.ivMoreVert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(itemView.getContext(),vehicleListBinding.ivMoreVert);
                    Menu menu = popupMenu.getMenu();
                    menu.add("Delete");
                    menu.add("Edit");
                    final int position = getAdapterPosition();
                    final Vehicle vehicle = ShowVehicleListAdapter.this.vehicleList.get(position);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            String title = menuItem.getTitle().toString();
                            if(title.equals("Delete")){
                               if(position!=RecyclerView.NO_POSITION && listner !=null)
                                   listner.onItemClick(vehicle,position,"Delete");
                            }else if (title.equals("Edit")){
                                if(position!=RecyclerView.NO_POSITION && listner !=null)
                                    listner.onItemClick(vehicle,position,"Edit");
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
        }
    }

    public interface OnRecyclerViewClick{
        public void onItemClick(Vehicle vehicle, int postion, String status);
    }

    public void setOnRecyclerListener(OnRecyclerViewClick listner){
        this.listner = listner;
    }

}
