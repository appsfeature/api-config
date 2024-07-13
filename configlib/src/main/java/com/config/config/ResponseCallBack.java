package com.config.config;

import com.config.util.Logger;
import com.config.util.ConfigUtil;

import org.json.JSONException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.config.config.ConfigManager.JSON_EXCEPTION;


public class ResponseCallBack implements Callback<BaseModel> {

    private ConfigManager.OnNetworkCall onNetworkCall;
    private String endPoint;
    private ConfigManager configManager;

    public ResponseCallBack(ConfigManager.OnNetworkCall onNetworkCall, String endPoint, ConfigManager configManager) {
        this.onNetworkCall = onNetworkCall;
        this.endPoint = endPoint;
        this.configManager = configManager;
    }

    private ResponseCallBack() {
    }

    @Override
    public void onResponse(Call<BaseModel> call, Response<BaseModel> response) {
        Logger.i("onResponse");
        Logger.i("onResponse code - " + response.code());
        if (response != null & response.code() != 0) {
            int responseCode = response.code();
            if (responseCode == 200) {
                if (response.body() != null && response.body() instanceof BaseModel) {
                    BaseModel baseModel = (BaseModel) response.body();
                    boolean callConfig = !ConfigUtil.isEmptyOrNull(baseModel.getCall_config())
                            && baseModel.getCall_config().equals(ConfigConstant.TRUE);
                    if (callConfig && !ConfigManager.isCallConfig()) {
                        ConfigManager.setIsCallConfig(true);
                        if (configManager != null) {
                            String bug = configManager.getApiConfigError(responseCode,
                                    "Call_config=" + baseModel.getCall_config()
                                    , configManager.getRequestBody(call));
                            configManager.callConfig(true, true, bug);
                        }
                    }
                    boolean status = !callConfig && !ConfigUtil.isEmptyOrNull(baseModel.getStatus())
                            && baseModel.getStatus().equals(ConfigConstant.SUCCESS);
                    String s = ConfigManager.getGson().toJson(baseModel.getData());
                    if (onNetworkCall != null) {
                        Logger.i( "ApiEndPoint:" + endPoint + " onResponse s -- " + s);
                        onNetworkCall.onComplete(status, status ? s : baseModel.getMessage());
                    }
                }else {
                    if (onNetworkCall != null) {
                        onNetworkCall.onComplete(false, response.toString());
                    }
                }
            } else if (responseCode == INTERNAL_SERVER_ERROR || responseCode == NOT_FOUND
                    || responseCode == BAD_GATEWAY || responseCode == SERVICE_UNAVAILABLE
                    || responseCode == GATEWAY_TIMEOUT) {
                Logger.e(Logger.getClassPath(Thread.currentThread().getStackTrace()), "ApiEndPoint:" + endPoint, "OnError : " + configManager.getRequestBody(call));
                if (onNetworkCall != null) {
                    onNetworkCall.onComplete(false, response.toString());
                }
                if (configManager != null) {
                    String apiCall = configManager.getApiErrorCode().get(endPoint);
                    if (apiCall == null) {
                        String bug = configManager.getApiConfigError(responseCode, response.message()
                                , configManager.getRequestBody(call));
                        configManager.getApiErrorCode().put(endPoint, bug);
                        if (!configManager.isConfigLoaded()) {
                            configManager.callConfig(true, true, bug);
                        }
                    }
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
//        Logger.e(Logger.getClassPath(Thread.currentThread().getStackTrace()), "onFailure : " + configManager.getRequestBody(call));
        if (onNetworkCall != null) {
            onNetworkCall.onComplete(false, t != null ? t.toString() : "");
        }
        if (t instanceof JSONException) {
            if (configManager != null) {
                String apiCall = configManager.getApiJsonException().get(endPoint);
                if (apiCall == null) {
                    String bug = configManager.getApiConfigError(JSON_EXCEPTION, t.getMessage()
                            , configManager.getRequestBody(call));
                    configManager.getApiJsonException().put(endPoint, bug);
                    if (!configManager.isConfigLoaded()) {
                        configManager.callConfig(true, true, bug);
                    }
                }
            }
        }
    }

    private final int NOT_FOUND = 404;
    private final int INTERNAL_SERVER_ERROR = 500;
    private final int BAD_GATEWAY = 502;
    private final int SERVICE_UNAVAILABLE = 503;
    private final int GATEWAY_TIMEOUT = 504;

}
