package com.config.config;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by Amit on 3/29/2018.
 */

public interface ApiInterface {

    @GET("{endpoint}")
    Call<BaseModel> getData(@Path("endpoint") String endpoint, @QueryMap Map<String, String> options);

    @POST("{endpoint}")
    Call<BaseModel> postData(@Path("endpoint") String endpoint, @QueryMap Map<String, String> options);

    @FormUrlEncoded
    @POST("{endpoint}")
    Call<BaseModel> postDataForm(@Path("endpoint") String endpoint, @FieldMap Map<String, String> options);

    @Multipart
    @POST("{endpoint}")
    Call<BaseModel> postDataForm(@Path("endpoint") String endpoint, @QueryMap Map<String, String> options
            , @Part("name") RequestBody requestBody, @Part MultipartBody.Part multipartBody);

    @Streaming
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlAsync(@Url String fileUrl, @QueryMap Map<String, String> options);

    @Streaming
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlAsync(@Url String fileUrl, @QueryMap Map<String, String> options, @Header("Content-Type") String contentType);

    @Streaming
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlAsync(@Url String fileUrl);

    @Streaming
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlAsync(@Url String fileUrl, @Header("Content-Type") String contentType);

    @Headers({"Accept-Encoding: *","Content-Type: application/pdf"})
    @Streaming
    @GET
    Call<ResponseBody> downloadPDFFileWithDynamicUrlAsync(@Url String fileUrl, @QueryMap Map<String, String> options);
}
