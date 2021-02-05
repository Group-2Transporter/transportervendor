package com.e.transportervendor.bean;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Bid implements Serializable
{

    @SerializedName("bidId")
    @Expose
    private String bidId;
    @SerializedName("transporterId")
    @Expose
    private String transporterId;
    @SerializedName("leadId")
    @Expose
    private String leadId;
    @SerializedName("transporterName")
    @Expose
    private String transporterName;
    @SerializedName("amount")
    @Expose
    private Double amount;
    @SerializedName("remark")
    @Expose
    private String remark;
    @SerializedName("estimatedDate")
    @Expose
    private String estimatedDate;
    @SerializedName("materialType")
    @Expose
    private String materialType;
    private final static long serialVersionUID = 4348143418672905913L;

    public Bid(String transporterId, String leadId, String transporterName, Double amount, String remark, String estimatedDate, String materialType) {
        this.transporterId = transporterId;
        this.leadId = leadId;
        this.transporterName = transporterName;
        this.amount = amount;
        this.remark = remark;
        this.estimatedDate = estimatedDate;
        this.materialType = materialType;
    }

    public String getBidId() {
        return bidId;
    }

    public void setBidId(String bidId) {
        this.bidId = bidId;
    }

    public String getTransporterId() {
        return transporterId;
    }

    public void setTransporterId(String transporterId) {
        this.transporterId = transporterId;
    }

    public String getLeadId() {
        return leadId;
    }

    public void setLeadId(String leadId) {
        this.leadId = leadId;
    }

    public String getTransporterName() {
        return transporterName;
    }

    public void setTransporterName(String transporterName) {
        this.transporterName = transporterName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getEstimatedDate() {
        return estimatedDate;
    }

    public void setEstimatedDate(String estimatedDate) {
        this.estimatedDate = estimatedDate;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

}