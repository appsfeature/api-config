package com.config.config;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.os.Build;

import com.config.BuildConfig;
import com.config.ConfigProvider;
import com.config.network.NetworkMonitor;
import com.config.util.AppConstant;
import com.config.util.AppPreferences;
import com.config.util.Logger;
import com.config.util.SupportUtil;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.config.config.NetworkStatusCode.BAD_GATEWAY;
import static com.config.config.NetworkStatusCode.GATEWAY_TIMEOUT;
import static com.config.config.NetworkStatusCode.INTERNAL_SERVER_ERROR;
import static com.config.config.NetworkStatusCode.NOT_FOUND;
import static com.config.config.NetworkStatusCode.SERVICE_UNAVAILABLE;

/**
 * @author Created by Abhijit on 3/28/2018.
 */

public class ConfigManager {

    @SuppressLint("StaticFieldLeak") private static ConfigManager configManager;
    private Context context;
    private ConfigPreferences configPreferences;
    private int backupConfigCallCount = 0;
    private HashMap<String, String> hostAlias;
    private HashMap<String, ApiInterface> apiInterfaceHashMap;
    private HashMap<String, HashMap<String, String>> apiHostHashMap;
    private boolean isConfigLoaded = false;
    //    private ConnectivityReceiver mNetworkReceiver;
    private NetworkMonitor networkMonitor;

    /*
     * get Host Alias
     */
    public HashMap<String, String> getHostAlias() {
        return hostAlias;
    }

    /**
     * It Return ApiInterFace Object. It can return null so please check the null before using it.
     *
     * @param host     -- Its the HostSection Title
     * @param endPoint -- Its the API name
     * @return
     */
    public ApiInterface getApiInterface(String host, String endPoint) {
        ApiInterface apiInterface = null;
        if (apiHostHashMap != null) {
            if (apiHostHashMap.get(host) != null && apiHostHashMap.get(host).get(endPoint) != null) {
                String endPointHost = apiHostHashMap.get(host).get(endPoint);
                apiInterface = getHostInterface(endPointHost);
            } else if (connectHostHashMap.get(host) != null && connectHostHashMap.get(host) && hostAlias.get(host) != null) {
                String endPointHost = hostAlias.get(host);
                apiInterface = getHostInterface(endPointHost);
            }
        }
        return apiInterface;
    }

    private ApiInterface getHostInterface(String endPointHost) {
        if (apiInterfaceHashMap.get(endPointHost) != null) {
            return apiInterfaceHashMap.get(endPointHost);
        } else {
            return RetrofitGenerator.getClient(endPointHost, securityCode, isDebug).create(ApiInterface.class);
        }
    }

    public boolean isConfigLoaded() {
        return isConfigLoaded;
    }

    private String securityCode;
    public boolean isDebug = false;

    public static ConfigManager getInstance() {
        if(configManager == null){
            configManager = getInstance(ConfigProvider.context, SupportUtil.getSecurityCode(ConfigProvider.context));
        }
        return configManager;
    }

    public static ConfigManager getInstance(Context context, String securityCode) {
        return getInstance(context, securityCode, BuildConfig.DEBUG);
    }

    public static ConfigManager getInstance(Context context, String securityCode, boolean isDebug) {
        if (configManager == null) {
            synchronized (ConfigManager.class) {
                if (configManager == null) {
                    configManager = new ConfigManager(context, securityCode, isDebug);
                }
            }
        }
        return configManager;
    }


    private void sendBroadCast() {
        Logger.d("Config loaded : success");
        context.sendBroadcast(new Intent(context.getPackageName() + ConfigConstant.CONFIG_LOADED));
    }

    private void sendBroadCastFailure() {
        Logger.e("Config loaded : failure");
        context.sendBroadcast(new Intent(context.getPackageName() + ConfigConstant.CONFIG_FAILURE));
    }

    private ConfigManager(Context context, String securityCode, boolean isDebug) {
        init(context, securityCode, isDebug);
    }

    private void init(Context context, String securityCode, boolean isDebug) {
        if (context != null) {
            this.context = context;
            this.securityCode = securityCode;
            this.isDebug = isDebug;
            configPreferences = new ConfigPreferences(context);
            networkMonitor = getNetworkMonitor();
        }
    }

    public void setConfigHost(Context context , String host){
        if ( context != null && !TextUtils.isEmpty(host)) {
            getConfigPreferences(context).putString(ConfigConstant.CONFIG_HOST, host);
        }
    }

    private ConfigPreferences getConfigPreferences(Context context){
        if ( configPreferences == null && context != null ){
            configPreferences = new ConfigPreferences(context);
        }
        return configPreferences;
    }

    public void loadConfig() {
        refreshConfig();
    }

    public void refreshConfig() {
        refreshConfig(null);
    }

    public void refreshConfig(Callable<Void> function) {
        callConfig(true, false, null, function);
    }

    private void callBackUpConfig(String error) {
        callConfig(false, true, error);
    }

    private String getHostConfigPath() {
        String s = configPreferences.getString(ConfigConstant.CONFIG_HOST);
        if (SupportUtil.isEmptyOrNull(s)) {
            s = ConfigConstant.CONFIG_HOST_URL;
        }
        return s;
    }

    private String getBackupHostConfigPath() {
        String s = configPreferences.getString(ConfigConstant.CONFIG_HOST_BACKUP);
        if (SupportUtil.isEmptyOrNull(s)) {
            s = ConfigConstant.CONFIG_HOST_BACKUP_URL;
        }
        return s;
    }

    private boolean isConfigLoading = false;

    public void callConfig(final boolean isMain, boolean isBug, final String bug) {
        callConfig(isMain, isBug, bug, null);
    }

    public void callConfig(final boolean isMain, boolean isBug, final String bug, final Callable<Void> function) {
        if (SupportUtil.isConnected(context) && !isConfigLoading) {
            isConfigLoading = true;
            String host = isMain ? getHostConfigPath() : getBackupHostConfigPath();
            Retrofit retrofit = RetrofitGenerator.getClient(host, securityCode, isDebug);
            Call<ConfigModel> call = null;
            if ( retrofit != null ) {
                if (isMain) {
                    call = retrofit.create(ApiConfig.class).getConfig(context.getPackageName(), getAppVersion());
                } else if (isMain && isBug) {
                    call = retrofit.create(ApiConfig.class).getConfigBug(context.getPackageName(), bug, getAppVersion());
                } else if (backupConfigCallCount <= ConfigConstant.BACKUP_CONFIG_CALL_COUNT) {
                    call = retrofit.create(ApiConfigBackup.class).getConfig(context.getPackageName(), bug, getAppVersion());
                    backupConfigCallCount++;
                }
            }

            if (call != null) {
                Logger.i("Config api called");
                call.enqueue(new Callback<ConfigModel>() {
                    @Override
                    public void onResponse(Call<ConfigModel> call, Response<ConfigModel> response) {
                        Logger.i("Config onResponse code - " + response.code());
                        isConfigLoading = false;
                        if (response != null & response.code() != 0) {
                            int responseCode = response.code();
                            if (responseCode == NetworkStatusCode.SUCCESS) {
                                handleConfigResponse(response.body());
                                sendBroadCast();
                                callFunction(function);
                            } else if (responseCode == INTERNAL_SERVER_ERROR || responseCode == NOT_FOUND
                                    || responseCode == BAD_GATEWAY || responseCode == SERVICE_UNAVAILABLE
                                    || responseCode == GATEWAY_TIMEOUT) {
                                if (isMain) {
                                    callBackUpConfig(getMainConfigError(responseCode, response.message(), bug, getRequestBody(call)));
                                } else {
                                    sendBroadCastFailure();
                                }
                            } else
                                sendBroadCastFailure();
                        } else
                            sendBroadCastFailure();
                    }

                    @Override
                    public void onFailure(Call<ConfigModel> call, Throwable t) {
                        isConfigLoading = false;
                        if (t instanceof JSONException) {
                            if (isMain) {
                                callBackUpConfig(getMainConfigError(JSON_EXCEPTION, t.getMessage(), bug, getRequestBody(call)));
                            } else
                                sendBroadCastFailure();
                        } else
                            sendBroadCastFailure();
                    }
                });
            } else {
                isConfigLoading = false;
                sendBroadCastFailure();
            }
        }
    }

    public void callFunction(Callable<Void> function) {
        if (function != null) {
            try {
                function.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getRequestBody(Call call) {
        String api = "";
        if (call != null && call.request() != null)
            api = call.request().toString();
        return api;

    }

    private String getAppVersion() {
        String appVersion = "0";
        try {
            if(context!=null) {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    appVersion = (int) pInfo.getLongVersionCode() + ""; // avoid huge version numbers and you will be ok
                } else {
                    //noinspection deprecation
                    appVersion = pInfo.versionCode + "";
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appVersion;
    }

    public static final int JSON_EXCEPTION = 1000;

    private String getMainConfigError(int code, String msg, String apiMsg, String api) {
        JSONObject object = new JSONObject();
        try {
            object.putOpt("response_code", code);
            object.putOpt("response_msg", getErrorTypeMsg(code));
            object.putOpt("timestamp", getFormatDate(System.currentTimeMillis()));
            object.putOpt("device_id", SupportUtil.getDeviceId(context));
            object.putOpt("msg", msg);
            object.putOpt("api_failure", apiMsg);
            object.putOpt("api", api);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    SimpleDateFormat simpleDateFormat;

    private String getFormatDate(long time) {
        if (simpleDateFormat == null)
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date(time));
    }


    public String getApiConfigError(int code, String msg, String apiMsg) {
        JSONObject object = new JSONObject();
        try {
            object.putOpt("response_code", code);
            object.putOpt("response_msg", getErrorTypeMsg(code));
            object.putOpt("timestamp", DateFormat.getInstance().format(new Date(System.currentTimeMillis())));
            object.putOpt("device_id", SupportUtil.getDeviceId(context));
            object.putOpt("msg", msg);
            object.putOpt("api_failure", apiMsg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    private static Gson gson = new Gson();

    public static Gson getGson() {
        return gson;
    }

    public void getData(final int type, final String host, final String endPoint, final Map<String, String> param
            , final OnNetworkCall onNetworkCall) {
        getData(type, host, endPoint, param, null, null, onNetworkCall);
    }

    public void getData(final int type, final String host, final String endPoint, final Map<String, String> param
            , RequestBody requestBody, MultipartBody.Part multipartBody, final OnNetworkCall onNetworkCall) {
        if (context != null && SupportUtil.isConnected(context)) {
            if (param != null) {
                if (param.get("application_id") == null) {
                    param.put("application_id", context.getPackageName());
                }
                if (param.get("app_version") == null) {
                    param.put("app_version", getAppVersion());
                }
            }
            if (isConfigLoaded) {
                if (BuildConfig.DEBUG) {
                    getDataDebug(type, host, endPoint, param, requestBody, multipartBody, onNetworkCall);
                } else {
                    getDataRelease(type, host, endPoint, param, requestBody, multipartBody, onNetworkCall);
                }
            } else if (!isConfigLoading) {
                refreshConfig(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        getData(type, host, endPoint, param, onNetworkCall);
                        return null;
                    }
                });
            } else if (onNetworkCall != null) {
                onNetworkCall.onComplete(false, "");
                Logger.e(Logger.getClassPath(Thread.currentThread().getStackTrace()), "ApiEndPoint:" + endPoint, "Config not loaded");
            }
        } else if (onNetworkCall != null) {
            onNetworkCall.onComplete(false, "");
            Logger.e(Logger.getClassPath(Thread.currentThread().getStackTrace()), "ApiEndPoint:" + endPoint, "No Internet Connection");
        }
    }

    private ApiInterface debugApiInterface;

    public void getDataDebug(int type, String host, final String endPoint, Map<String, String> param
            , RequestBody requestBody, MultipartBody.Part multipartBody, final OnNetworkCall onNetworkCall) {

        ApiInterface apiInterface;
//            if (host.equalsIgnoreCase(ConfigConstant.HOST_PAID)) {
//                apiInterface = debugApiInterface;
//            } else {
        apiInterface = getApiInterface(host, endPoint);
//            }
        if (apiInterface != null) {
            Logger.i("getData -- " + endPoint);
            Call<BaseModel> call = getCall(type, apiInterface, endPoint, param, requestBody, multipartBody);
            call.enqueue(new ResponseCallBack(onNetworkCall, endPoint, this));
        } else if (onNetworkCall != null) {
            onNetworkCall.onComplete(false, "");
            Logger.e(Logger.getClassPath(Thread.currentThread().getStackTrace()), "ApiEndPoint:" + endPoint, "Invalid Hostname:" + (host != null ? host : "null"));
        }
    }


    public void getDataRelease(int type, String host, final String endPoint, Map<String, String> param
            , RequestBody requestBody, MultipartBody.Part multipartBody, final OnNetworkCall onNetworkCall) {
        ApiInterface apiInterface = getApiInterface(host, endPoint);
        if (apiInterface != null) {
            Logger.i("getData -- " + endPoint);
            Call<BaseModel> call = getCall(type, apiInterface, endPoint, param, requestBody, multipartBody);
            call.enqueue(new ResponseCallBack(onNetworkCall, endPoint, this));
        } else if (onNetworkCall != null) {
            onNetworkCall.onComplete(false, "");
            Logger.e(Logger.getClassPath(Thread.currentThread().getStackTrace()), "ApiEndPoint:" + endPoint, "Invalid Hostname:" + (host != null ? host : "null"));
        }
    }

    public void getDataDebug1(int type, String host, final String endPoint, Map<String, String> param
            , final OnNetworkCall onNetworkCall) {
        if (SupportUtil.isConnected(context) && isConfigLoaded) {
            ApiInterface apiInterface = getApiInterface(host, endPoint);
            if (apiInterface != null) {
                Logger.i("getData -- " + endPoint);
                Call<BaseModel> call = getCall(type, apiInterface, endPoint, param);
                call.enqueue(new ResponseCallBack(onNetworkCall, endPoint, this));
            } else if (onNetworkCall != null) {
                onNetworkCall.onComplete(false, "");
            }
        } else if (onNetworkCall != null) {
            onNetworkCall.onComplete(false, "");
        }
    }

    private Call<BaseModel> getCall(int callType, ApiInterface apiInterface, String endPoint, Map<String, String> param) {
        return getCall(callType, apiInterface, endPoint, param, null, null);
    }

    private Call<BaseModel> getCall(int callType, ApiInterface apiInterface, String endPoint, Map<String, String> param
            , RequestBody requestBody, MultipartBody.Part multipartBody) {
        Call<BaseModel> call;
        switch (callType) {
            case ConfigConstant.CALL_TYPE_POST:
                call = apiInterface.postData(endPoint, param);
                break;
            case ConfigConstant.CALL_TYPE_POST_FORM:
                call = apiInterface.postDataForm(endPoint, param);
                break;
            case ConfigConstant.CALL_TYPE_POST_FILE:
                call = apiInterface.postDataForm(endPoint, param, requestBody, multipartBody);
                break;
            case ConfigConstant.CALL_TYPE_GET:
            default:
                call = apiInterface.getData(endPoint, param);
                break;
        }
        return call;
    }

    /*
     * Return error Message String
     */
    private String getErrorTypeMsg(int code) {
        String msg;
        switch (code) {
            case INTERNAL_SERVER_ERROR:
                msg = "INTERNAL_SERVER_ERROR";
                break;
            case NOT_FOUND:
                msg = "NOT_FOUND";
                break;
            case BAD_GATEWAY:
                msg = "BAD_GATEWAY";
                break;
            case SERVICE_UNAVAILABLE:
                msg = "SERVICE_UNAVAILABLE";
                break;
            case GATEWAY_TIMEOUT:
                msg = "GATEWAY_TIMEOUT";
                break;
            case JSON_EXCEPTION:
                msg = "JSON_EXCEPTION";
                break;
            default:
                msg = "error";
                break;
        }
        return msg;
    }

    private HashMap<String, String> apiJsonException;
    private HashMap<String, String> apiErrorCode;
    private static boolean isCallConfig = false;

    public static boolean isCallConfig() {
        return isCallConfig;
    }

    public static void setIsCallConfig(boolean isCallConfig) {
        ConfigManager.isCallConfig = isCallConfig;
    }

    public interface OnNetworkCall {
        void onComplete(boolean status, String data);
    }

    private void handleConfigResponse(ConfigModel model) {
        if (model != null) {
            isConfigLoaded = true;
            configPreferences.putString(ConfigConstant.CONFIG_HOST, model.getConfig_host());
            configPreferences.putString(ConfigConstant.CONFIG_HOST_BACKUP, model.getBackup_host());
            handleHostSection(model.getHost_section());
            Logger.i("Config is loaded");
            apiJsonException = new HashMap<>();
            apiErrorCode = new HashMap<>();

            handleAppVersion(model.getApp_version());
            networkMonitor.setConfigLoaded(isConfigLoaded, SupportUtil.isConnected(context));
        }
    }

    private void handleAppVersion(AppVersionModel model) {
        if (model != null) {
            int myVersion = Integer.parseInt(getAppVersion());
            try {
                int minSupportVersion = SupportUtil.isEmptyOrNull(model.getMin_version()) ? 0 : Integer.parseInt(model.getMin_version());
                if (myVersion != 0 && minSupportVersion != 0 && myVersion <= minSupportVersion) {
                    sendBroadCast(model, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                String s = model.getNot_supported_version();
                if (!SupportUtil.isEmptyOrNull(s)) {
                    if (s.contains(",")) {
                        String[] arr = s.split(",");
                        for (String s1 : arr) {
                            int notSupportVersion = SupportUtil.isEmptyOrNull(s1) ? 0 : Integer.parseInt(s1);
                            if (myVersion != 0 && notSupportVersion != 0 && myVersion == notSupportVersion) {
                                sendBroadCast(model, false);
                            }
                        }
                    } else {
                        int notSupportVersion = SupportUtil.isEmptyOrNull(s) ? 0 : Integer.parseInt(s);
                        if (myVersion != 0 && notSupportVersion != 0 && myVersion == notSupportVersion) {
                            sendBroadCast(model, false);
                        }
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            int currentVersion = 0;
            try {
                currentVersion = SupportUtil.isEmptyOrNull(model.getCurrent_version()) ? 0 : Integer.parseInt(model.getCurrent_version());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (myVersion != 0 && myVersion < currentVersion) {
                sendBroadCast(model, true);
            }
        }
    }

    private void sendBroadCast(AppVersionModel model, boolean type) {
        Intent intent = new Intent(context.getPackageName() + AppConstant.APP_UPDATE);
        intent.putExtra(AppConstant.TITLE, SupportUtil.isEmptyOrNull(model.getMsg()) ? AppConstant.MSG_UPDATE : model.getMsg());
        intent.putExtra(AppConstant.TYPE, type);
        context.sendBroadcast(intent);
    }

    private HashMap<String, Boolean> connectHostHashMap;

    private void handleHostSection(List<HostSectionModel> models) {
        if (models != null && models.size() > 0) {
            hostAlias = new HashMap<>(models.size());
            connectHostHashMap = new HashMap<>(models.size());
            apiInterfaceHashMap = new HashMap<>();
            ApiInterface apiInterface;
            apiHostHashMap = new HashMap<>();
            for (HostSectionModel model : models) {
                connectHostHashMap.put(model.getTitle(), model.getConnect_to_host().equalsIgnoreCase(ConfigConstant.TRUE));
                if (!SupportUtil.isEmptyOrNull(model.getConnect_to_host())
                        && model.getConnect_to_host().equalsIgnoreCase(ConfigConstant.TRUE)) {
                    hostAlias.put(model.getTitle(), model.getHost());
                    Logger.i("HOST : " + model.getTitle());
                    if (model.getTitle().equalsIgnoreCase(ConfigConstant.HOST_DOWNLOAD_PDF)) {
                        Logger.i("HOST_DOWNLOAD_PDF : " + model.getHost());
                        AppPreferences.setBaseUrl(context, model.getHost());
                        Logger.i("HOST_DOWNLOAD_PDF Pref : " + AppPreferences.getBaseUrl(context));
                    }
                    if (model.getTitle().equalsIgnoreCase(ConfigConstant.HOST_TRANSLATOR)) {
                        AppPreferences.setBaseUrl_3(context, model.getHost());
                    }
                    configPreferences.putString(model.getTitle(), model.getHost());
                    apiInterface = RetrofitGenerator.getClient(model.getHost(), securityCode, isDebug).create(ApiInterface.class);
                    apiInterfaceHashMap.put(model.getHost(), apiInterface);
                    handleApiHost(model.getTitle(), model.getApi_host());
                }
            }
        }
    }

    public HashMap<String, String> getApiJsonException() {
        return apiJsonException;
    }

    public HashMap<String, String> getApiErrorCode() {
        return apiErrorCode;
    }

    private void handleApiHost(String title, List<ApiHostModel> models) {

        if (models != null && models.size() > 0) {
            HashMap<String, String> map = new HashMap<>(models.size());
            for (ApiHostModel model : models) {
                map.put(model.getApi_name(), model.getApi_host());
            }
            apiHostHashMap.put(title, map);
        }
    }

    public NetworkMonitor getNetworkMonitor() {
        if (networkMonitor == null) {
            networkMonitor = NetworkMonitor.getInstance();
        }
        return networkMonitor;
    }

}
