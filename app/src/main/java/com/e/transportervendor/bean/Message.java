package com.e.transportervendor.bean;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Message implements Serializable {

    @SerializedName("messageId")
    @Expose
    private String messageId;
    @SerializedName("from")
    @Expose
    private String from;
    @SerializedName("to")
    @Expose
    private String to;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("timeStamp")
    @Expose
    private Long timeStamp;

    public String getMessageId() {
        return messageId;
    }

    public Message() {
    }

    public Message(String messageId, String from, String to, String message, Long timeStamp) {
        this.messageId = messageId;
        this.from = from;
        this.to = to;
        this.message = message;
        this.timeStamp = timeStamp;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

}