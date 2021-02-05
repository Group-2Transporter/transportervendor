package com.e.transportervendor.adapter;

import android.content.Context;
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

public class MarketListShowAdapter extends RecyclerView.Adapter<MarketListShowAdapter.MarketListViewHolder> {
    ArrayList<Lead> leadList;
    ArrayList<Bid> bidList;
    String cancel;
    SharedPreferences language = null;
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
        language = parent.getContext().getSharedPreferences("Language", Context.MODE_PRIVATE);
        return new MarketListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final MarketListViewHolder holder, int position) {
        if (cancel != null) {
            try {
                holder.binding.btnBid.setText(holder.itemView.getContext().getResources().getString(R.string.cancel_button));
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
                                String lang = language.getString("language","en");
                                Log.e("LAnguage on Home","=========>"+lang);
                                final Lead lead;
                                if(lang.equalsIgnoreCase("en")) {
                                    lead = response.body();
                                }
                                else {
                                    lead = identifyLanguage(holder,response.body());
                                }
                                String[] pickup = lead.getPickUpAddress().split(",");
                                String[] delivery = lead.getDeliveryAddress().split(",");
                                holder.binding.tvAddress.setText(pickup[1] + " " + pickup[2] + " To " + delivery[1] + " " + delivery[2]);
                                holder.binding.tvExpiryDate.setText(bid.getEstimatedDate());
                                holder.binding.tvWeight.setText(lead.getWeight());
                                holder.binding.llAmount.setVisibility(View.VISIBLE);
                                holder.binding.tvAmount.setText(""+bid.getAmount());
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
                holder.binding.btnBid.setText(holder.itemView.getContext().getString(R.string.bid_now));
                String lang = language.getString("language","en");
                Log.e("LAnguage on Home","=========>"+lang);
                final Lead lead;
                if(lang.equalsIgnoreCase("en")) {
                    lead = leadList.get(position);
                }
                else {
                    lead = identifyLanguage(holder,leadList.get(position));
                }
                String[] pickup = lead.getPickUpAddress().split(",");
                String[] delivery = lead.getDeliveryAddress().split(",");
                holder.binding.tvAddress.setText(pickup[1] + " " + pickup[2] + " To " + delivery[1] + " " + delivery[2]);
                holder.binding.tvTypeOfaterial.setText(lead.getTypeOfMaterial());
                holder.binding.tvUserName.setText(lead.getUserName());
                holder.binding.llAmount.setVisibility(View.GONE);
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
                    if (button.equalsIgnoreCase(itemView.getContext().getString(R.string.bid_now))) {
                        try {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION && listner != null) {
                                listner.onItemClick(leadList.get(position), position);
                            }
                        } catch (Exception e) {
                            Toast.makeText(itemView.getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else if (button.equalsIgnoreCase(itemView.getContext().getString(R.string.cancel_button))) {
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
    private Lead identifyLanguage(MarketListViewHolder holder, final Lead lead) {

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

    private Lead getLanguageCode(MarketListViewHolder holder, Lead lead) {
        int langCode = FirebaseTranslateLanguage.EN;
        return  translateText(holder,langCode,lead);
    }

    String address = "";
    private Lead translateText(final MarketListViewHolder holder, int langCode, final Lead lead) {
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
                        holder.binding.tvTypeOfaterial.setText(s);
                    }
                });
                translator.translate(lead.getUserName()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        holder.binding.tvUserName.setText(s);
                    }
                });
            }
        });

        return lead;
    }


}
