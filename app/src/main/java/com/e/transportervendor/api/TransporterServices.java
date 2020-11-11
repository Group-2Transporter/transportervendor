package com.e.transportervendor.api;

import com.e.transportervendor.bean.Transporter;
import com.e.transportervendor.bean.Vehicle;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class TransporterServices {
    private final static String BASE_URL = "http://192.168.43.125:8080/";
    public static TransportApi transportApi;

    public static TransportApi getTransporterApiIntance() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        if (transportApi == null)
            transportApi = retrofit.create(TransportApi.class);
        return transportApi;
    }

    public interface TransportApi {
        @GET("transporter/{transporterId}")
        public Call<Transporter> getTransporterVehicleList(@Path("transporterId") String transporterId);

        @DELETE("vehicle/{vehicleId}/{transporterId}")
        public Call<Vehicle> deleteTransporterVehicle(@Path("vehicleId")String vehicleId, @Path("transporterId")String transporterId);

    }
}
