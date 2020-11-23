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
import com.e.transportervendor.bean.State;
import com.e.transportervendor.databinding.FilterListBinding;

import java.util.ArrayList;

public class FilterAdapter extends ArrayAdapter {

    Context context;
    ArrayList<State> list;
    ArrayList<State>stateList;

    public FilterAdapter(@NonNull Context context,  ArrayList<State> list) {
        super(context, R.layout.filter_list,list);
        this.list = list;
        this.context = context;
        stateList = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final FilterListBinding binding = FilterListBinding.inflate(LayoutInflater.from(parent.getContext()));
        try {
            final State state = list.get(position);
            binding.tvState.setText(state.getStateList());
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
    public ArrayList<State> getSelectedState(){
        return stateList;
    }


}
