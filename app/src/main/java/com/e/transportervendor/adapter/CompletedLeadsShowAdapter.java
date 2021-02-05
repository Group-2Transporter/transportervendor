package com.e.transportervendor.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
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

import com.e.transportervendor.R;
import com.e.transportervendor.api.LeadService;
import com.e.transportervendor.bean.Bid;
import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.databinding.HistoryCompletedBinding;
import com.e.transportervendor.utility.InternetUtilityActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompletedLeadsShowAdapter extends RecyclerView.Adapter<CompletedLeadsShowAdapter.CompletedViewHolder>  {
    ArrayList<Lead> leadList;
    LeadService.LeadApi leadApi;
    Context context;
    SharedPreferences language = null;
    public CompletedLeadsShowAdapter(ArrayList<Lead> leadList){
        this.leadList = leadList;
        leadApi = LeadService.getTransporterApiIntance();
    }

    @NonNull
    @Override
    public CompletedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        HistoryCompletedBinding binding = HistoryCompletedBinding.inflate(LayoutInflater.from(parent.getContext()));
        language = parent.getContext().getSharedPreferences("Language", Context.MODE_PRIVATE);
        context = parent.getContext();
        return new CompletedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final CompletedViewHolder holder, final int position) {
        String lang = language.getString("language","en");
        final Lead lead;
        if(lang.equalsIgnoreCase("en")) {
            lead = leadList.get(position);
        }
        else {
            lead = identifyLanguage(holder,leadList.get(position));
        }
        try {
            holder.binding.tvPickupAddress.setText(lead.getPickUpAddress());
            holder.binding.tvDeliveryAddress.setText(lead.getDeliveryAddress());
            holder.binding.tvAmount.setText("" + lead.getAmount());
            holder.binding.tvDate.setText(lead.getDateOfCompletion());
            holder.binding.tvTypeOfMaterial.setText(lead.getTypeOfMaterial());
            holder.binding.tvUserName.setText(lead.getUserName());
            holder.binding.tvWeight.setText(lead.getWeight());
            if(!lead.getSecondaryMaterial().equalsIgnoreCase("")){
                holder.binding.tvSpecialRequirment.setVisibility(View.VISIBLE);
                holder.binding.rlSpecialMaterial.setVisibility(View.VISIBLE);
                holder.binding.specialDeliveryAddress.setText(lead.getSecondaryDeliveryAddress());
                holder.binding.specialMaterial.setText(lead.getSecondaryMaterial());
                holder.binding.specialPickupAddress.setText(lead.getSecondaryPickupAddress());
//                holder.binding.specialWeight.setText(lead.getSecondaryWeight());

            }else{
                holder.binding.tvSpecialRequirment.setVisibility(View.GONE);
                holder.binding.rlSpecialMaterial.setVisibility(View.GONE);
            }
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
                                                            leadList.remove(position);
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


    private Lead identifyLanguage(CompletedViewHolder holder, final Lead lead) {

        FirebaseLanguageIdentification identifier = FirebaseNaturalLanguage.getInstance()
                .getLanguageIdentification();

        identifier.identifyLanguage(lead.getPickUpAddress()).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                lead.setLang(s);
            }
        });
        return  getLanguageCode(holder,lead);
    }

    private Lead getLanguageCode(CompletedViewHolder holder, Lead lead) {
        int langCode = FirebaseTranslateLanguage.EN;
        return  translateText(holder,langCode,lead);
    }

    private Lead translateText(final CompletedViewHolder holder, int langCode, final Lead lead) {
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                //from language
                .setSourceLanguage(langCode)
                // to language
                .setTargetLanguage(FirebaseTranslateLanguage.HI)
                .build();

        final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance()
                .getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .build();



        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                translator.translate(lead.getPickUpAddress()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        holder.binding.tvPickupAddress.setText(s);
                    }
                });
                translator.translate(lead.getDeliveryAddress()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        holder.binding.tvDeliveryAddress.setText(s);
                    }
                });
                translator.translate(lead.getTypeOfMaterial()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        holder.binding.tvTypeOfMaterial.setText(s);
                    }
                });
                translator.translate(lead.getUserName()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        holder.binding.tvUserName.setText(s);
                    }
                });
                translator.translate(lead.getSecondaryPickupAddress()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        holder.binding.specialPickupAddress.setText(s);
                    }
                });
                translator.translate(lead.getSecondaryDeliveryAddress()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        holder.binding.specialDeliveryAddress.setText(s);
                    }
                });
                translator.translate(lead.getSecondaryMaterial()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        holder.binding.specialMaterial.setText(s);
                    }
                });
            }
        });

        return lead;
    }



}
