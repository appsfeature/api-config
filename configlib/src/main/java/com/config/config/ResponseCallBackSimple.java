package com.config.config;

import com.config.util.ConfigUtil;
import com.config.util.Logger;

import org.json.JSONException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



/**
 * Created by Amit on 4/10/2018.
 */

public class ResponseCallBackSimple implements Callback<BaseModel> {

    private ConfigManager.OnNetworkCall onNetworkCall;
    private String endPoint;

    public ResponseCallBackSimple(ConfigManager.OnNetworkCall onNetworkCall, String endPoint) {
        this.onNetworkCall = onNetworkCall;
        this.endPoint = endPoint;
    }

    private ResponseCallBackSimple() {
    }

    @Override
    public void onResponse(Call<BaseModel> call, Response<BaseModel> response) {
        if (response != null && response.code() != 0) {
            int responseCode = response.code();
            if (responseCode == 200) {
                if (response.body() != null && response.body() instanceof BaseModel) {
                    BaseModel baseModel = (BaseModel) response.body();
                    boolean status = !ConfigUtil.isEmptyOrNull(baseModel.getStatus())
                            && baseModel.getStatus().equals(ConfigConstant.SUCCESS);
                    String s = ConfigManager.getGson().toJson(baseModel.getData());
                    if (onNetworkCall != null) {
                        Logger.i( "ApiEndPoint:" + endPoint + " onResponse s -- " + s);
                        onNetworkCall.onComplete(status, status ? s : baseModel.getMessage());
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
    public void onFailure(Call<BaseModel> call, Throwable t) {
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
