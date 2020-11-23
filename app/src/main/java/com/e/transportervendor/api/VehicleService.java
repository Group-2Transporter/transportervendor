package com.e.transportervendor.api;

import com.e.transportervendor.bean.Transporter;
import com.e.transportervendor.bean.Vehicle;

import okhttp3.MultipartBody;
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

public class VehicleService {
    public static VehicleService.VehicleApi vehicleApi;

    public static VehicleService.VehicleApi getVehicleApiIntance() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAddress.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        if (vehicleApi == null)
            vehicleApi = retrofit.create(VehicleService.VehicleApi.class);
        return vehicleApi;
    }

    public interface VehicleApi {

        @DELETE("vehicle/{vehicleId}/{transporterId}")
        public Call<Vehicle> deleteTransporterVehicle(@Path("vehicleId") String vehicleId, @Path("transporterId") String transporterId);

        @Multipart
        @POST("/vehicle/")
        public Call<Vehicle> saveVehicle(
                @Part MultipartBody.Part file,
                @Part("name") RequestBody name,
                @Part("count") RequestBody count,
                @Part("transporterId") RequestBody transporterId);


        @POST("/vehicle/{transporterId}")
        public Call<Vehicle> updateVehicle(@Path("transporterId") String transporterId, @Body Vehicle vehicle);

    }
}
