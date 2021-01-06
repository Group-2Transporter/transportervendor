package com.e.transportervendor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.e.transportervendor.adapter.MessageAdapter;
import com.e.transportervendor.api.TransporterServices;
import com.e.transportervendor.api.UserService;
import com.e.transportervendor.bean.Message;
import com.e.transportervendor.bean.Transporter;
import com.e.transportervendor.bean.User;
import com.e.transportervendor.databinding.ChatActivityBinding;
import com.e.transportervendor.utility.InternetUtilityActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    ChatActivityBinding binding;
    String userId;
    String currentUserId;
    DatabaseReference firebaseDatabase;
    MessageAdapter adapter;
    String leadId;
    int count = 0;
    ArrayList<Message>al;
    ArrayList<Message> deleteSelection = new ArrayList<>();
    String currentUserName;
    User user;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChatActivityBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        try {
            setSupportActionBar(binding.toolbar);
            Intent in = getIntent();
            currentUserId = FirebaseAuth.getInstance().getUid();
            userId = (String) in.getCharSequenceExtra("userId");
            leadId = (String) in.getCharSequenceExtra("leadId");
            UserService.UserApi userApi = UserService.getUserApiInstance();
            TransporterServices.TransportApi transportApi = TransporterServices.getTransporterApiIntance();
            transportApi.getTransporterVehicleList(currentUserId).enqueue(new Callback<Transporter>() {
                @Override
                public void onResponse(Call<Transporter> call, Response<Transporter> response) {
                    if(response.code() == 200){
                        currentUserName = response.body().getName();
                    }
                }

                @Override
                public void onFailure(Call<Transporter> call, Throwable t) {
                    Toast.makeText(ChatActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            binding.etMessage.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    binding.etMessage.setMinLines(0);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    binding.etMessage.setMaxLines(3);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            if(InternetUtilityActivity.isNetworkConnected(this)) {
                Call<User> call = userApi.getUserById(userId);
                if (InternetUtilityActivity.isNetworkConnected(this)) {
                    firebaseDatabase = FirebaseDatabase.getInstance().getReference();
                    call.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if (response.code() == 200) {
                                user = response.body();
                                binding.tvName.setText(user.getName());
                                Picasso.get().load(user.getImageUrl()).into(binding.civUser);
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            Toast.makeText(ChatActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "please enable internet connection.", Toast.LENGTH_SHORT).show();
                }
                binding.ivBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                binding.rv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
                binding.rv.setMultiChoiceModeListener(modeListener);
            }else{
                getInternetAlert();
            }
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    AbsListView.MultiChoiceModeListener modeListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

            if(checked) {
                count += 1;
            }
            else
                count -=1;
            mode.setTitle(count + " item selected");
            deleteSelection.add(al.get(position));
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.delete,menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            getSupportActionBar().hide();
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()){
                case R.id.delete :
                    for (final Message msg : deleteSelection){
                        firebaseDatabase.child("Messages").child(leadId).child(currentUserId).child(userId).child(msg.getMessageId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    //Toast.makeText(ChatActivity.this, "Message Delete", Toast.LENGTH_SHORT).show();
                                    if(msg.getFrom().equalsIgnoreCase(currentUserId)){
                                        Toast.makeText(ChatActivity.this, "1 dkn", Toast.LENGTH_SHORT).show();
                                        firebaseDatabase.child("Messages").child(leadId).child(userId).child(currentUserId).child(msg.getMessageId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(!task.isSuccessful())
                                                    Toast.makeText(ChatActivity.this, ""+task.getException().toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    adapter.remove(msg);
                                }else{
                                    Toast.makeText(ChatActivity.this, ""+task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            getSupportActionBar().show();
            deleteSelection.clear();
            count = 0;
        }
    };
    @Override
    protected void onStart() {
        super.onStart();
        binding.tvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String message = binding.etMessage.getText().toString().trim();
                if (message.isEmpty()) {
                    return;
                }
                final Calendar c = Calendar.getInstance();
                long timeStamp = c.getTimeInMillis();
                if (InternetUtilityActivity.isNetworkConnected(ChatActivity.this)) {
                    final String messageId = FirebaseDatabase.getInstance().getReference().push().getKey();
                    binding.etMessage.setText("");
                    final Message msg = new Message(messageId,currentUserId,userId,message,timeStamp);
                    firebaseDatabase.child("Messages").child(leadId).child(currentUserId).child(userId).child(messageId).setValue(msg).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                firebaseDatabase.child("Messages").child(leadId).child(userId).child(currentUserId).child(messageId).setValue(msg).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            sendNotification(msg);
                                        }
                                    }
                                });
                            }
                        }
                    });
                } else
                    getInternetAlert();

            }
        });
        al=new ArrayList<>();

        firebaseDatabase.child("Messages").child(leadId).child(currentUserId).child(userId).orderByChild("timeStamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    try {
                        Message msg = dataSnapshot.getValue(Message.class);
                        al.add(msg);
                        adapter = new MessageAdapter(ChatActivity.this, al);
                        binding.rv.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }catch (Exception e){
                        Toast.makeText(ChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getInternetAlert() {
        final androidx.appcompat.app.AlertDialog ab = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Network Not Connected")
                .setMessage("Please check your network connection")
                .setPositiveButton("Retry", null)
                .show();
        Button positive = ab.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(InternetUtilityActivity.isNetworkConnected(ChatActivity.this)) {
                    ab.dismiss();
                }
            }
        });
    }

    private void sendNotification(final Message message){
        String token = user.getToken();
        try{
            RequestQueue queue = Volley.newRequestQueue(ChatActivity.this);
            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title","New Message Send By "+currentUserName);
            data.put("body", "message : "+message.getMessage());
            data.put("resultCode","message");
            data.put("transporterId",currentUserId);
            data.put("leadId",leadId);

            JSONObject notification_data = new JSONObject();
            notification_data.put("data", data);
            notification_data.put("to",token);

            JsonObjectRequest request = new JsonObjectRequest(url, notification_data, new com.android.volley.Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    String api_key_header_value = "Key=AAAARoiepkM:APA91bHVqjULid8wCt5Sf_EwC4Y0engqgafGEhEdMMhlb2Ix2TbXQldPyAffP7hEPDxLSBoPo1jizb_hX2hFpADDEaNCa5prcG9fR8uPvJt4xEfF-hYQEKmbG8Gn5zouwyRKAXQ98YCZ";
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", api_key_header_value);
                    return headers;
                }
            };
            queue.add(request);
        }catch (Exception e){
            Toast.makeText(ChatActivity.this, ""+e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }
}