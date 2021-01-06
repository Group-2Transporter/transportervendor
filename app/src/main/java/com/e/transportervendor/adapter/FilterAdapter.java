package com.e.transportervendor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.e.transportervendor.R;
import com.e.transportervendor.bean.States;
import com.e.transportervendor.databinding.FilterListBinding;

import java.util.ArrayList;

public class FilterAdapter extends ArrayAdapter {

    Context context;
    ArrayList<States> list;
    ArrayList<States>stateList;

    public FilterAdapter(@NonNull Context context,  ArrayList<States> list) {
        super(context, R.layout.filter_list,list);
        this.list = list;
        this.context = context;
        stateList = new ArrayList<>();
    }
    public void update(ArrayList<States> result){
        list = new ArrayList<>();
        list.addAll(result);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final FilterListBinding binding = FilterListBinding.inflate(LayoutInflater.from(parent.getContext()));
        try {
            final States state = list.get(position);
            binding.tvState.setText(state.getStateName());
            if (state.isCheck()) {
                binding.checkLoaded.setVisibility(View.VISIBLE);
                binding.cLoaded.setVisibility(View.GONE);
            } else {
                binding.checkLoaded.setVisibility(View.GONE);
                binding.cLoaded.setVisibility(View.VISIBLE);
                binding.rl2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        state.setCheck(!state.isCheck());
                        if (state.isCheck()) {
                            binding.checkLoaded.setVisibility(View.VISIBLE);
                            binding.cLoaded.setVisibility(View.GONE);
                            stateList.add(state);
                        } else {
                            binding.checkLoaded.setVisibility(View.GONE);
                            binding.cLoaded.setVisibility(View.VISIBLE);
                            stateList.remove(state);
                        }
                    }
                });
            }
        }catch (Exception e){
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return binding.getRoot();
    }
    public ArrayList<States> getSelectedState(){
        return stateList;
    }

}
