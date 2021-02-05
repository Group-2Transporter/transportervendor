package com.e.transportervendor.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e.transportervendor.ClientInfoActivity;
import com.e.transportervendor.R;
import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.bean.Vehicle;
import com.e.transportervendor.databinding.CurrentLoadHomeBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;

public class HomeCurrentLoadShowAdapter extends RecyclerView.Adapter<HomeCurrentLoadShowAdapter.HomeCurrentLoadViewHolder> {
    ArrayList<Lead> leadList;
    OnRecyclerViewClickListner listner;
    SharedPreferences language = null;
    public HomeCurrentLoadShowAdapter(ArrayList<Lead> leadList) {
        this.leadList = leadList;
    }

    @NonNull
    @Override
    public HomeCurrentLoadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CurrentLoadHomeBinding binding = CurrentLoadHomeBinding.inflate(LayoutInflater.from(parent.getContext()));
        language = parent.getContext().getSharedPreferences("Language", Context.MODE_PRIVATE);
        return new HomeCurrentLoadViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final HomeCurrentLoadViewHolder holder, int position) {
        String lang = language.getString("language","en");
        Log.e("LAnguage on Home","=========>"+lang);
        final Lead lead;
        if(lang.equalsIgnoreCase("en")) {
             lead = leadList.get(position);
        }
        else {
             lead = identifyLanguage(holder,leadList.get(position));
        }
        try {
            String address = lead.getPickUpAddress()+""+holder.itemView.getContext().getResources().getString(R.string.to)+" "+lead.getDeliveryAddress();
            int c = address.indexOf(""+holder.itemView.getContext().getResources().getString(R.string.to));

            SpannableString ss = new SpannableString(address);
            ForegroundColorSpan black = new ForegroundColorSpan(Color.BLACK);
            ss.setSpan(black,c,c+2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.binding.tvAddress.setText(ss);
            holder.binding.tvTypeOfMaterial.setText(lead.getTypeOfMaterial());
            holder.binding.tvCLDate.setText(lead.getDateOfCompletion());
            holder.binding.tvUserName.setText(lead.getUserName());
            holder.binding.tvWeight.setText(lead.getWeight());
            holder.binding.tvAmount.setText("" + lead.getAmount());
            holder.binding.tvPickupContact.setText(lead.getContactForPickup());
            holder.binding.tvDeliveryContact.setText(lead.getContactForDelivery());
            if(!lead.getSecondaryMaterial().isEmpty()){
                holder.binding.tvSpecialRequirment.setVisibility(View.VISIBLE);
                holder.binding.rlSpecialMaterial.setVisibility(View.VISIBLE);
                holder.binding.specialPickupAddress.setText(lead.getSecondaryPickupAddress());
                holder.binding.specialDeliveryAddress.setText(lead.getSecondaryDeliveryAddress());
                holder.binding.specialMaterial.setText(lead.getSecondaryMaterial());
                holder.binding.specialWeight.setText(lead.getSecondaryWeight());
                holder.binding.specialPickupContact.setText(lead.getSecondaryPickupContact());
                holder.binding.specialDeliveryContact.setText(lead.getSecondaryDeliveryContact());
            }else{
                holder.binding.tvSpecialRequirment.setVisibility(View.GONE);
                holder.binding.rlSpecialMaterial.setVisibility(View.GONE);
            }
            holder.binding.cardCurrent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(holder.itemView.getContext(), ClientInfoActivity.class);
                    intent.putExtra("leadId",lead.getLeadId());
                    holder.itemView.getContext().startActivity(intent);
                }
            });
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

    private Lead identifyLanguage(HomeCurrentLoadViewHolder holder,final Lead lead) {

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

    private Lead getLanguageCode(HomeCurrentLoadViewHolder holder,Lead lead) {
        int langCode = FirebaseTranslateLanguage.EN;
        return  translateText(holder,langCode,lead);
    }

    String address = "";
    private Lead translateText(final HomeCurrentLoadViewHolder holder, int langCode, final Lead lead) {
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
                        address = s;
                    }
                });
                translator.translate(lead.getDeliveryAddress()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        address += " "+holder.itemView.getContext().getResources().getString(R.string.to)+" "+s;
                        int c = address.indexOf(""+holder.itemView.getContext().getResources().getString(R.string.to));

                        SpannableString ss = new SpannableString(address);
                        ForegroundColorSpan black = new ForegroundColorSpan(Color.BLACK);
                        ss.setSpan(black,c,c+2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        holder.binding.tvAddress.setText(ss);
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

    public interface OnRecyclerViewClickListner{
        void onItemClick(Lead lead, int postion, String status);
    }

    public void setOnRecyclerListener(OnRecyclerViewClickListner listner){
        this.listner = listner;
    }

}
