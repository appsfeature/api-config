package com.config.config;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Amit on 3/28/2018.
 */

public interface ApiConfig {

    @GET("config/v2/get-config")
    Call<ConfigModel> getConfig(@Query("id") String pkg_id, @Query("app_version") String appVersion);

    @GET("api/v1/get-new-backup-config")
    Call<ConfigModel> getConfigBug(@Query("application_id") String pkg_id, @Query("error") String error, @Query("app_version") String appVersion);

}
