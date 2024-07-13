package com.config.config;

import com.config.network.download.DownloadProgressCallback;
import com.config.network.download.DownloadProgressInterceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RetrofitGenerator {

    public static Retrofit getClient(String host, String securityCode) {
        return getClient(host, securityCode, ConfigManager.getInstance().isDebugMode());
    }

    public static Retrofit getClient(String host, String securityCode, boolean isDebug) {
        return getClient(host, securityCode, null, isDebug);
    }
    public static Retrofit getClient(String host, String securityCode, DownloadProgressCallback progressListener, boolean isDebug) {
        Retrofit retrofit = null;
        try {
            retrofit = new Retrofit.Builder()
                    .baseUrl(host)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getHttpClient(securityCode, isDebug, progressListener).build())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retrofit;
    }

    private static OkHttpClient.Builder getHttpClient(final String securityCode, boolean isDebug, DownloadProgressCallback progressListener) {
        DownloadProgressInterceptor progressInterceptor = null;
        if(progressListener != null) {
            progressInterceptor =new DownloadProgressInterceptor(progressListener);
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();

                        Request request = original.newBuilder()
                                .header("Authorization", securityCode)
                                .method(original.method(), original.body())
                                .build();

                        return chain.proceed(request);
                    }
                });
        if(progressInterceptor != null){
            builder.addInterceptor(progressInterceptor);
        }
        if (isDebug) {
            builder.addInterceptor(loggingInterceptor);
        }
        return builder;
    }

    private static HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

}
