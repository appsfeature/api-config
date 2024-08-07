package com.config.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BaseModel {

    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("call_config")
    @Expose
    private String call_config;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName(value="data", alternate={"result"})
    @Expose
    private Object data;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCall_config() {
        return call_config;
    }

    public void setCall_config(String call_config) {
        this.call_config = call_config;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
