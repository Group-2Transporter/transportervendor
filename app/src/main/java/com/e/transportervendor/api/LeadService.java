package com.e.transportervendor.api;

import com.e.transportervendor.bean.Lead;
import com.e.transportervendor.bean.State;
import com.e.transportervendor.bean.Transporter;
import com.e.transportervendor.bean.Vehicle;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class LeadService {
    public static LeadService.LeadApi leadApi;

    public static LeadService.LeadApi getTransporterApiIntance() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAddress.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        if (leadApi == null)
            leadApi = retrofit.create(LeadService.LeadApi.class);
        return leadApi;
    }

    public interface LeadApi {

        @GET("lead/leadByTransporterId/{transporterId}")
        public Call<ArrayList<Lead>> getCurrentLoadByTransporterId(@Path("transporterId")String transporterId);

        @POST("lead/update/{leadId}")
        public Call<Lead> updateLeadById(@Path("leadId")String leadId, @Body Lead lead);

        @POST("lead/filter/{transporterId}")
        public Call<ArrayList<Lead>> getFilterLeads(@Path("transporterId")String transporterId,@Body ArrayList<State> stateList);

        @GET("lead/bids/{transporterId}")
        public Call<ArrayList<Lead>> getAllLeads(@Path("transporterId")String transporterId);

        @GET("lead/{leadId}")
        public Call<Lead> getLead(@Path("leadId")String leadId);

        @GET("lead/completedLead/transporterId/{transporterId}")
        public Call<ArrayList<Lead>> getAllCompletedLeads(@Path("transporterId")String transporterId);

        @GET("lead/filter/all/{transporterId}")
        public Call<ArrayList<Lead>> getAllCreatedLeads(@Path("transporterId")String transporterId);

        @DELETE("lead/{leadId}")
        public Call<Lead> deleteLead(@Path("leadId")String leadId);

    }
}
