package com.e.transportervendor.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.e.transportervendor.R;
import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.bean.States;
import com.e.transportervendor.databinding.FilterListBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;

public class FilterAdapter extends ArrayAdapter {

    Context context;
    ArrayList<States> list;
    ArrayList<States>stateList;
    SharedPreferences language = null;

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
        language = parent.getContext().getSharedPreferences("Language", Context.MODE_PRIVATE);
        final FilterListBinding binding = FilterListBinding.inflate(LayoutInflater.from(parent.getContext()));
        String lang = language.getString("language","en");
        try {
            final States state;
        if(lang.equalsIgnoreCase("en")) {
                state = list.get(position);
            }
            else {
                state = getLanguageCode(binding,list.get(position));
            }
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


//    private States identifyLanguage(CompletedLeadsShowAdapter.CompletedViewHolder holder, final States states) {
//
//        FirebaseLanguageIdentification identifier = FirebaseNaturalLanguage.getInstance()
//                .getLanguageIdentification();
//
//        identifier.identifyLanguage(states.getStateName()).addOnSuccessListener(new OnSuccessListener<String>() {
//            @Override
//            public void onSuccess(String s) {
//                lead.setLang(s);
//            }
//        });
//        return  getLanguageCode(binding,states);
//    }

    private States getLanguageCode(FilterListBinding binding, States states) {
        int langCode = FirebaseTranslateLanguage.EN;
        return  translateText(binding,langCode,states);
    }

    private States translateText(final FilterListBinding binding, int langCode, final States states) {
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
                translator.translate(states.getStateName()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        binding.tvState.setText(s);
                    }
                });
            }
        });

        return states;
    }

}
