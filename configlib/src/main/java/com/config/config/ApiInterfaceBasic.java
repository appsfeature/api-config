package com.config.config;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;



public interface ApiInterfaceBasic {

    @GET("{endpoint}")
    Call<Object> getData(@Path("endpoint") String endpoint, @QueryMap Map<String, String> options);

    @POST("{endpoint}")
    Call<Object> postData(@Path("endpoint") String endpoint, @QueryMap Map<String, String> options);

    @FormUrlEncoded
    @POST("{endpoint}")
    Call<Object> postDataForm(@Path("endpoint") String endpoint, @FieldMap Map<String, String> options);

    @Multipart
    @POST("{endpoint}")
    Call<Object> postDataForm(@Path("endpoint") String endpoint, @QueryMap Map<String, String> options
            , @Part("name") RequestBody requestBody, @Part MultipartBody.Part multipartBody);
}
