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
import com.e.transportervendor.bean.Message;
import com.e.transportervendor.databinding.ChatListBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MessageAdapter extends ArrayAdapter {
    Context context;
    ArrayList<Message> al;
    String currentUser;

    public MessageAdapter(Context context, ArrayList<Message> al) {
        super(context, R.layout.chat_list, al);
        this.context = context;
        this.al = al;
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ChatListBinding binding = ChatListBinding.inflate(LayoutInflater.from(parent.getContext()));
        //ChatListBinding binding = ChatListBinding.inflate(LayoutInflater.from(parent.getContext()));
        final Message message = al.get(position);
        try {
            if (currentUser.equals(message.getFrom())) {
                binding.received.setVisibility(View.GONE);
                binding.sent.setVisibility(View.VISIBLE);
                binding.tvChatSent.setText(message.getMessage());
            } else {
                binding.received.setVisibility(View.VISIBLE);
                binding.sent.setVisibility(View.GONE);
                binding.tvChatReceived.setText(message.getMessage());
            }
        }catch (Exception e){
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return binding.getRoot();
    }
}
