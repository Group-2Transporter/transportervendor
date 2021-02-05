package com.e.transportervendor.api;

import com.e.transportervendor.bean.Bid;
import com.e.transportervendor.bean.Lead;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class BidService {
    public static BidService.BidApi bidApi;

    public static BidService.BidApi getBidApiInstance() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100,TimeUnit.SECONDS).build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAddress.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        if (bidApi == null)
            bidApi = retrofit.create(BidService.BidApi.class);
        return bidApi;
    }

    public interface BidApi {
        @POST("bid/")
        public Call<Bid> createBid(@Body Bid bid);

        @GET("bid/transporter/pending/{transporterId}")
        public Call<ArrayList<Bid>> getAllPendingBids(@Path("transporterId")String transporterId);

        @DELETE("bid/{bidId}")
        public Call<Bid> deleteBid(@Path("bidId")String bidId);

    }

}
