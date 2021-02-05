package com.e.transportervendor.api;

import com.e.transportervendor.bean.Transporter;
import com.e.transportervendor.bean.User;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class UserService {

    public static UserApi userApi;

    public static UserApi getUserApiInstance() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100,TimeUnit.SECONDS).build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAddress.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        if (userApi == null)
            userApi = retrofit.create(UserService.UserApi.class);
        return userApi;
    }

    public interface UserApi {
        @GET("user/{userId}")
        public Call<User> getUserById(@Path("userId") String userId);

    }

}
