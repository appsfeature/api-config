package com.config.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Amit on 3/28/2018.
 */

public class ConfigModel {
    @SerializedName("backup_host")
    @Expose
    private String backup_host;

    @SerializedName("config_host")
    @Expose
    private String config_host;

    @SerializedName("app_version")
    @Expose
    private AppVersionModel app_version;

    @SerializedName("host_section")
    @Expose
    private List<HostSectionModel> host_section;

    public String getConfig_host() {
        return config_host;
    }

    public void setConfig_host(String config_host) {
        this.config_host = config_host;
    }

    public String getBackup_host() {
        return backup_host;
    }

    public void setBackup_host(String backup_host) {
        this.backup_host = backup_host;
    }

    public AppVersionModel getApp_version() {
        return app_version;
    }

    public void setApp_version(AppVersionModel app_version) {
        this.app_version = app_version;
    }

    public List<HostSectionModel> getHost_section() {
        return host_section;
    }

    public void setHost_section(List<HostSectionModel> host_section) {
        this.host_section = host_section;
    }
}
