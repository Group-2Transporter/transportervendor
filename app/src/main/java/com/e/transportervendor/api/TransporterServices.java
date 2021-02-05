package com.e.transportervendor.api;

import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.bean.Transporter;
import com.e.transportervendor.bean.Vehicle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public class  TransporterServices {
    public static TransportApi transportApi;

    public static TransportApi getTransporterApiIntance() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100,TimeUnit.SECONDS).build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAddress.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).client(client)
                .build();
        if (transportApi == null)
            transportApi = retrofit.create(TransportApi.class);
        return transportApi;
    }

    public interface TransportApi {
        @GET("transporter/{transporterId}")
        public Call<Transporter> getTransporterVehicleList(@Path("transporterId") String transporterId);

        @POST("transporter/update")
        public Call<Transporter> updateTransporter(@Body Transporter transporter);

        @Multipart
        @POST("/transporter/")
        public Call<Transporter> saveTransporter(
                @Part MultipartBody.Part file,
                @Part("transporterId") RequestBody transporterId,
                @Part("type") RequestBody type,
                @Part("name") RequestBody name,
                @Part("contactNumber") RequestBody contactNumber,
                @Part("address") RequestBody address,
                @Part("gstNumber") RequestBody gstNumber,
                @Part("token") RequestBody token,
                @Part("rating") RequestBody rating);

        @Multipart
        @POST("/transporter/update/image")
        public Call<Transporter> updateImageTransporter(@Part MultipartBody.Part file,@Part("transporterId") RequestBody transporterId);

    }

}
