package com.e.transportervendor.bean;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Lead implements Serializable
{

    @SerializedName("userId")
    @Expose
    private String userId;
    @SerializedName("leadId")
    @Expose
    private String leadId;
    @SerializedName("typeOfMaterial")
    @Expose
    private String typeOfMaterial;
    @SerializedName("weight")
    @Expose
    private String weight;
    @SerializedName("pickUpAddress")
    @Expose
    private String pickUpAddress;
    @SerializedName("deliveryAddress")
    @Expose
    private String deliveryAddress;
    @SerializedName("contactForPickup")
    @Expose
    private String contactForPickup;
    @SerializedName("contactForDelivery")
    @Expose
    private String contactForDelivery;
    @SerializedName("dateOfCompletion")
    @Expose
    private String dateOfCompletion;
    @SerializedName("timestamp")
    @Expose
    private long timestamp;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("materialStatus")
    @Expose
    private String materialStatus;
    @SerializedName("dealLockedWith")
    @Expose
    private String dealLockedWith;
    @SerializedName("bidCount")
    @Expose
    private Integer bidCount;
    @SerializedName("transporterName")
    @Expose
    private String transporterName;
    @SerializedName("userName")
    @Expose
    private String userName;
    @SerializedName("amount")
    @Expose
    private Integer amount;
    @SerializedName("remark")
    @Expose
    private String remark;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLeadId() {
        return leadId;
    }

    public void setLeadId(String leadId) {
        this.leadId = leadId;
    }

    public String getTypeOfMaterial() {
        return typeOfMaterial;
    }

    public void setTypeOfMaterial(String typeOfMaterial) {
        this.typeOfMaterial = typeOfMaterial;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getPickUpAddress() {
        return pickUpAddress;
    }

    public void setPickUpAddress(String pickUpAddress) {
        this.pickUpAddress = pickUpAddress;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getContactForPickup() {
        return contactForPickup;
    }

    public void setContactForPickup(String contactForPickup) {
        this.contactForPickup = contactForPickup;
    }

    public String getContactForDelivery() {
        return contactForDelivery;
    }

    public void setContactForDelivery(String contactForDelivery) {
        this.contactForDelivery = contactForDelivery;
    }

    public String getDateOfCompletion() {
        return dateOfCompletion;
    }

    public void setDateOfCompletion(String dateOfCompletion) {
        this.dateOfCompletion = dateOfCompletion;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMaterialStatus() {
        return materialStatus;
    }

    public void setMaterialStatus(String materialStatus) {
        this.materialStatus = materialStatus;
    }

    public String getDealLockedWith() {
        return dealLockedWith;
    }

    public void setDealLockedWith(String dealLockedWith) {
        this.dealLockedWith = dealLockedWith;
    }

    public Integer getBidCount() {
        return bidCount;
    }

    public void setBidCount(Integer bidCount) {
        this.bidCount = bidCount;
    }

    public String getTransporterName() {
        return transporterName;
    }

    public void setTransporterName(String transporterName) {
        this.transporterName = transporterName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}