package com.config.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppVersionModel {

    @SerializedName("min_version")
    @Expose
    private String min_version;
    @SerializedName("not_supported_version")
    @Expose
    private String not_supported_version;
    @SerializedName("current_version")
    @Expose
    private String current_version;
    @SerializedName("app_update_message")
    @Expose
    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMin_version() {
        return min_version;
    }

    public void setMin_version(String min_version) {
        this.min_version = min_version;
    }

    public String getNot_supported_version() {
        return not_supported_version;
    }

    public void setNot_supported_version(String not_supported_version) {
        this.not_supported_version = not_supported_version;
    }

    public String getCurrent_version() {
        return current_version;
    }

    public void setCurrent_version(String current_version) {
        this.current_version = current_version;
    }
}
