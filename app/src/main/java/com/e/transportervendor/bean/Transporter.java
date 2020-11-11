
package com.e.transportervendor.bean;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Transporter {

    @SerializedName("transporterId")
    @Expose
    private String transporterId;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("imageUrl")
    @Expose
    private String imageUrl;
    @SerializedName("contactNumber")
    @Expose
    private String contactNumber;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("gstNumber")
    @Expose
    private String gstNumber;
    @SerializedName("rating")
    @Expose
    private String rating;
    @SerializedName("token")
    @Expose
    private Object token;
    @SerializedName("vehicleList")
    @Expose
    private List<Vehicle> vehicle = null;

    public String getTransporterId() {
        return transporterId;
    }

    public void setTransporterId(String transporterId) {
        this.transporterId = transporterId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public Object getToken() {
        return token;
    }

    public void setToken(Object token) {
        this.token = token;
    }

    public List<Vehicle> getVehicle() {
        return vehicle;
    }

    public void setVehicle(List<Vehicle> vehicle) {
        this.vehicle = vehicle;
    }

}
