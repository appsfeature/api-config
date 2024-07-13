package com.config.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ApiHostModel {
    @SerializedName("api_name")
    @Expose
    private String api_name;

    @SerializedName("api_host")
    @Expose
    private String api_host;

    public String getApi_name() {
        return api_name;
    }

    public void setApi_name(String api_name) {
        this.api_name = api_name;
    }

    public String getApi_host() {
        return api_host;
    }

    public void setApi_host(String api_host) {
        this.api_host = api_host;
    }
}
