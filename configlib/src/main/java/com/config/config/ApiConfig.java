package com.config.config;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Amit on 3/28/2018.
 */

public interface ApiConfig {

    @GET("config/api/v1/get-config")
    Call<ConfigModel> getConfig(@Query("package_name") String pkg_id, @Query("app_version") String appVersion);

    @GET("config/api/v1/get-new-backup-config")
    Call<ConfigModel> getConfigBug(@Query("package_name") String pkg_id, @Query("error") String error, @Query("app_version") String appVersion);

}
