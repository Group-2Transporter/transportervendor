package com.e.transportervendor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.e.transportervendor.api.TransporterServices;
import com.e.transportervendor.bean.Transporter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceivingPushNotification extends FirebaseMessagingService {
    String currentUserId;

    @Override
    public void onNewToken(String token) {
        updateToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
            Map<String, String> map = remoteMessage.getData();
            String title = map.get("title");
            String description = map.get("body");

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            String channelId = "My id";
            String channelName = "My channel";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
            }
            NotificationCompat.Builder nb = new NotificationCompat.Builder(this, channelId);

            nb.setContentTitle(title);
            nb.setContentText(description);
            nb.setSmallIcon(R.drawable.eagleshipperlogo);
            manager.notify(1, nb.build());
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateToken(final String token) {
        currentUserId = FirebaseAuth.getInstance().getUid();
        final TransporterServices.TransportApi transportApi = TransporterServices.getTransporterApiIntance();
        Call<Transporter> call = transportApi.getTransporterVehicleList(currentUserId);
        call.enqueue(new Callback<Transporter>() {
            @Override
            public void onResponse(Call<Transporter> call, Response<Transporter> response) {
                if(response.code() == 200){
                    try {
                        Transporter transporter = response.body();
                        transporter.setToken(token);
                        Call<Transporter> call1 = transportApi.updateTransporter(transporter);
                        call1.enqueue(new Callback<Transporter>() {
                            @Override
                            public void onResponse(Call<Transporter> call, Response<Transporter> response) {

                            }

                            @Override
                            public void onFailure(Call<Transporter> call, Throwable t) {
                                Toast.makeText(ReceivingPushNotification.this, "" + t, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }catch (Exception e){
                        Toast.makeText(ReceivingPushNotification.this, "", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Transporter> call, Throwable t) {

            }
        });
    }

}
