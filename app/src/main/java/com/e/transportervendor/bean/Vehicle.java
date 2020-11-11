
package com.e.transportervendor.bean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Vehicle {

    @SerializedName("vehicelId")
    @Expose
    private String vehicelId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("count")
    @Expose
    private int count;
    @SerializedName("imageUrl")
    @Expose
    private String imageUrl;

    public String getVehicelId() {
        return vehicelId;
    }

    public void setVehicelId(String vehicelId) {
        this.vehicelId = vehicelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
