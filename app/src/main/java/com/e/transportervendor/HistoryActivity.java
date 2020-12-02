package com.e.transportervendor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.e.transportervendor.adapter.CompletedLeadsShowAdapter;
import com.e.transportervendor.api.LeadService;
import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.databinding.ActivityHistoryBinding;
import com.e.transportervendor.databinding.HistoryCompletedBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {
    ActivityHistoryBinding binding;
    String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        currentUserId = FirebaseAuth.getInstance().getUid();
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
                            binding.rv.setAdapter(adapter);
                            binding.rv.setLayoutManager(new LinearLayoutManager(HistoryActivity.this));
                        }
                    }catch (Exception e){
                        Toast.makeText(HistoryActivity.this, ""+e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Lead>> call, Throwable t) {
                Toast.makeText(HistoryActivity.this, ""+t, Toast.LENGTH_SHORT).show();
            }
        });

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}