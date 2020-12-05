package com.e.transportervendor.api;

import com.e.transportervendor.bean.States;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class StatesService {
    public static StatesService.StatesApi statesApi;

    public static StatesService.StatesApi getStatesApiInstance() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAddress.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        if (statesApi == null)
            statesApi = retrofit.create(StatesService.StatesApi.class);
        return statesApi;
    }

    public interface StatesApi {

        @GET("states/")
        public Call<ArrayList<States>> getStateList();

    }
}

