package com.config.config;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;



public interface ApiConfigBackup {

    @GET("api/v1/get-new-backup-config")
    Call<ConfigModel> getConfig(@Query("application_id") String pkg_id, @Query("error") String error, @Query("app_version") String appVersion);

}
