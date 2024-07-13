package com.config.config;

import com.config.util.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ResponseCallBackBasic implements Callback<Object> {

    private ConfigManager.OnNetworkCallBasic onNetworkCall;
    private String endPoint;

    public ResponseCallBackBasic(ConfigManager.OnNetworkCallBasic onNetworkCall, String endPoint) {
        this.onNetworkCall = onNetworkCall;
        this.endPoint = endPoint;
    }

    private ResponseCallBackBasic() {
    }

    @Override
    public void onResponse(Call<Object> call, Response<Object> response) {
        if (response != null && response.code() != 0) {
            int responseCode = response.code();
            if (responseCode == 200) {
                if (response.body() != null) {
                    if (onNetworkCall != null) {
                        Logger.i( "ApiEndPoint:" + endPoint + " onResponse s -- " + response.body());
                        String s = ConfigManager.getGson().toJson(response.body());
                        onNetworkCall.onComplete(true, s);
                    }
                }else {
                    if (onNetworkCall != null) {
                        onNetworkCall.onComplete(false, response != null ? response.toString() : "");
                    }
                }
            } else if (responseCode == INTERNAL_SERVER_ERROR || responseCode == NOT_FOUND
                    || responseCode == BAD_GATEWAY || responseCode == SERVICE_UNAVAILABLE
                    || responseCode == GATEWAY_TIMEOUT) {
                Logger.e(Logger.getClassPath(Thread.currentThread().getStackTrace()), "ApiEndPoint:" );
                if (onNetworkCall != null) {
                    onNetworkCall.onComplete(false, response != null ? response.toString() : "");
                }
            }
        } else {
            if (onNetworkCall != null) {
                onNetworkCall.onComplete(false, response != null ? response.toString() : "");
                Logger.e(Logger.getClassPath(Thread.currentThread().getStackTrace()), "ApiEndPoint:" + endPoint, "Invalid response!");
            }
        }
    }

    @Override
    public void onFailure(Call<Object> call, Throwable t) {
        if (onNetworkCall != null) {
            onNetworkCall.onComplete(false, t != null ? t.toString() : "");
        }
    }

    private final int NOT_FOUND = 404;
    private final int INTERNAL_SERVER_ERROR = 500;
    private final int BAD_GATEWAY = 502;
    private final int SERVICE_UNAVAILABLE = 503;
    private final int GATEWAY_TIMEOUT = 504;

}
