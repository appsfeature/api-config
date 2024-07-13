package com.config.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class HostSectionModel {

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("host")
    @Expose
    private String host;

    @SerializedName("connect_to_host")
    @Expose
    private String connect_to_host;

    @SerializedName("api_host")
    @Expose
    private List<ApiHostModel> api_host;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getConnect_to_host() {
        return connect_to_host;
    }

    public void setConnect_to_host(String connect_to_host) {
        this.connect_to_host = connect_to_host;
    }

    public List<ApiHostModel> getApi_host() {
        return api_host;
    }

    public void setApi_host(List<ApiHostModel> api_host) {
        this.api_host = api_host;
    }
}
