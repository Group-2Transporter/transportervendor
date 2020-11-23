package com.e.transportervendor.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;



public class InternetUtilityActivity {
    public static boolean isNetworkConnected(Context context) {
            boolean connection=false;
            try {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    connection = true;
                } else {
                    Toast.makeText(context, "Check Internet Connection\nInternet not Connected", Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e){
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }finally {
                return connection;
            }
    }
}
