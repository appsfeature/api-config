package com.config.config;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.config.BuildConfig;
import com.config.ConfigProvider;
import com.config.network.NetworkMonitor;
import com.config.network.download.ConfigDownloadListener;
import com.config.network.download.DownloadProgressCallback;
import com.config.util.ConfigConstant;
import com.config.util.ConfigPreferences;
import com.config.util.ConfigUtil;
import com.config.util.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.helper.util.GsonParser;

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

    @SuppressLint("StaticFieldLeak")
    private static ConfigManager configManager;
    private Context context;
    private com.config.config.ConfigPreferences configPreferences;
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

    private final DownloadProgressCallback mProgressListener = new DownloadProgressCallback() {
        @Override
        public void update(long bytesRead, long contentLength, boolean done) {
            try {
                int progressUpdate = (int) ((bytesRead * 100) / contentLength);
                if (mDownloadListener != null) {
                    mDownloadListener.onProgressUpdate(progressUpdate);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    public ConfigDownloadListener mDownloadListener;

    /**
     * @param downloadListener  add Handler for show data on Main Thread
     * @return Note. getting data in background thread.
     */
    public ConfigManager setDownloadListener(ConfigDownloadListener downloadListener) {
        mDownloadListener = null;
        this.mDownloadListener = downloadListener;
        return this;
    }

    public ApiInterface getHostInterfaceSaved(String endPointHost) {
        return getHostInterfaceSaved(endPointHost, false);
    }

    public ApiInterface getHostInterfaceSaved(String endPointHost, boolean isAddDownloadProgressListener) {
        if (apiHostHashMap != null && !TextUtils.isEmpty(endPointHost) ) {
            if (apiInterfaceHashMap != null && apiInterfaceHashMap.get(endPointHost) != null) {
                return apiInterfaceHashMap.get(endPointHost);
            } else {
                ApiInterface apiInterface;
                if(!isAddDownloadProgressListener){
                    apiInterface = getHostInterface(endPointHost);
                }else {
                    apiInterface = getDownloadHostInterface(endPointHost);
                }
                apiInterfaceHashMap.put(endPointHost, apiInterface);
                return apiInterface;
            }
        }
        return null;
    }

    // used for download file with progress update
    private ApiInterface getDownloadHostInterface(String endPointHost) {
        if (apiInterfaceHashMap != null && apiInterfaceHashMap.get(endPointHost) != null) {
            return apiInterfaceHashMap.get(endPointHost);
        } else {
            return RetrofitGenerator.getClient(endPointHost, securityCode, mProgressListener, isDebug).create(ApiInterface.class);
        }
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
        if (configManager == null) {
            configManager = getInstance(ConfigProvider.context, ConfigUtil.getSecurityCode(ConfigProvider.context));
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
        context.sendBroadcast(new Intent(context.getPackageName() + com.config.config.ConfigConstant.CONFIG_LOADED));
    }

    private void sendBroadCastFailure() {
        Logger.e("Config loaded : failure");
        context.sendBroadcast(new Intent(context.getPackageName() + com.config.config.ConfigConstant.CONFIG_FAILURE));
    }

    private ConfigManager(Context context, String securityCode, boolean isDebug) {
        init(context, securityCode, isDebug);
    }

    private void init(Context context, String securityCode, boolean isDebug) {
        if (context != null) {
            this.context = context;
            this.securityCode = securityCode;
            this.isDebug = isDebug;
            configPreferences = new com.config.config.ConfigPreferences(context);
            networkMonitor = getNetworkMonitor();
        }
    }

    public ConfigManager setConfigHost(Context context, String host) {
        if (context != null && !TextUtils.isEmpty(host)) {
            getConfigPreferences(context).putString(com.config.config.ConfigConstant.CONFIG_HOST, host);
        }
        return this;
    }

    private com.config.config.ConfigPreferences getConfigPreferences(Context context) {
        if (configPreferences == null && context != null) {
            configPreferences = new com.config.config.ConfigPreferences(context);
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
        String s = configPreferences.getString(com.config.config.ConfigConstant.CONFIG_HOST);
        if (ConfigUtil.isEmptyOrNull(s)) {
            s = com.config.config.ConfigConstant.CONFIG_HOST_URL;
        }
        return s;
    }

    private String getBackupHostConfigPath() {
        String s = configPreferences.getString(com.config.config.ConfigConstant.CONFIG_HOST_BACKUP);
        if (ConfigUtil.isEmptyOrNull(s)) {
            s = com.config.config.ConfigConstant.CONFIG_HOST_BACKUP_URL;
        }
        return s;
    }

    private boolean isConfigLoading = false;

    public void callConfig(final boolean isMain, boolean isBug, final String bug) {
        callConfig(isMain, isBug, bug, null);
    }

    public void callConfig(final boolean isMain, boolean isBug, final String bug, final Callable<Void> function) {
        if (isEnableConfigManager && ConfigUtil.isConnected(context) && !isConfigLoading) {
            isConfigLoading = true;
            String host = isMain ? getHostConfigPath() : getBackupHostConfigPath();
            Retrofit retrofit = RetrofitGenerator.getClient(host, securityCode, isDebug);
            Call<ConfigModel> call = null;
            if (retrofit != null) {
                if (isMain) {
                    call = retrofit.create(ApiConfig.class).getConfig(context.getPackageName(), getAppVersion());
                } else if (isMain && isBug) {
                    call = retrofit.create(ApiConfig.class).getConfigBug(context.getPackageName(), bug, getAppVersion());
                } else if (backupConfigCallCount <= com.config.config.ConfigConstant.BACKUP_CONFIG_CALL_COUNT) {
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
                                loadHostSectionFromCache();
                            } else {
                                sendBroadCastFailure();
                                loadHostSectionFromCache();
                            }
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

                        loadHostSectionFromCache();
                    }
                });
            } else {
                isConfigLoading = false;
                sendBroadCastFailure();
            }
        }
    }

    private void loadHostSectionFromCache() {
        if(isLoadFromCache) {
            String s = configPreferences.getString(com.config.config.ConfigConstant.CONFIG_HOST_SECTION);
            if (!TextUtils.isEmpty(s)) {
                handleHostSection(GsonParser.fromJson(s, new TypeToken<List<HostSectionModel>>() {
                }));
                isConfigLoaded = true;
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
            if (context != null) {
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
            object.putOpt("device_id", ConfigUtil.getDeviceId(context));
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
            object.putOpt("device_id", ConfigUtil.getDeviceId(context));
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
        if (!isEnableConfigManager) {
            getData(type, endPoint, param, requestBody, multipartBody, onNetworkCall);
        } else {
            if (context != null && ConfigUtil.isConnected(context)) {
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
        if (ConfigUtil.isConnected(context) && isConfigLoaded) {
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
            case com.config.config.ConfigConstant.CALL_TYPE_POST:
                call = apiInterface.postData(endPoint, param);
                break;
            case com.config.config.ConfigConstant.CALL_TYPE_POST_FORM:
                call = apiInterface.postDataForm(endPoint, param);
                break;
            case com.config.config.ConfigConstant.CALL_TYPE_POST_FILE:
                call = apiInterface.postDataForm(endPoint, param, requestBody, multipartBody);
                break;
            case com.config.config.ConfigConstant.CALL_TYPE_GET:
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

    public interface OnNetworkCallBasic {
        void onComplete(boolean status, String data);
    }

    private void handleConfigResponse(ConfigModel model) {
        if (model != null) {
            isConfigLoaded = true;
            configPreferences.putString(com.config.config.ConfigConstant.CONFIG_HOST, model.getConfig_host());
            configPreferences.putString(com.config.config.ConfigConstant.CONFIG_HOST_BACKUP, model.getBackup_host());
            configPreferences.putString(com.config.config.ConfigConstant.CONFIG_HOST_SECTION, GsonParser.toJson(model.getHost_section(), new TypeToken<List<HostSectionModel>>() {}));
            handleHostSection(model.getHost_section());

            Logger.i("Config is loaded");
            apiJsonException = new HashMap<>();
            apiErrorCode = new HashMap<>();

            handleAppVersion(model.getApp_version());
            networkMonitor.setConfigLoaded(isConfigLoaded, ConfigUtil.isConnected(context));
        }
    }

    private void handleAppVersion(AppVersionModel model) {
        if (model != null) {
            int myVersion = Integer.parseInt(getAppVersion());
            try {
                int minSupportVersion = ConfigUtil.isEmptyOrNull(model.getMin_version()) ? 0 : Integer.parseInt(model.getMin_version());
                if (myVersion != 0 && minSupportVersion != 0 && myVersion <= minSupportVersion) {
                    sendBroadCast(model, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                String s = model.getNot_supported_version();
                if (!ConfigUtil.isEmptyOrNull(s)) {
                    if (s.contains(",")) {
                        String[] arr = s.split(",");
                        for (String s1 : arr) {
                            int notSupportVersion = ConfigUtil.isEmptyOrNull(s1) ? 0 : Integer.parseInt(s1);
                            if (myVersion != 0 && notSupportVersion != 0 && myVersion == notSupportVersion) {
                                sendBroadCast(model, false);
                            }
                        }
                    } else {
                        int notSupportVersion = ConfigUtil.isEmptyOrNull(s) ? 0 : Integer.parseInt(s);
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
                currentVersion = ConfigUtil.isEmptyOrNull(model.getCurrent_version()) ? 0 : Integer.parseInt(model.getCurrent_version());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (myVersion != 0 && myVersion < currentVersion) {
                sendBroadCast(model, true);
            }
        }
    }

    private void sendBroadCast(AppVersionModel model, boolean type) {
        Intent intent = new Intent(context.getPackageName() + ConfigConstant.APP_UPDATE);
        intent.putExtra(ConfigConstant.TITLE, ConfigUtil.isEmptyOrNull(model.getMsg()) ? ConfigConstant.MSG_UPDATE : model.getMsg());
        intent.putExtra(ConfigConstant.TYPE, type);
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
                connectHostHashMap.put(model.getTitle(), model.getConnect_to_host().equalsIgnoreCase(com.config.config.ConfigConstant.TRUE));
                if (!ConfigUtil.isEmptyOrNull(model.getConnect_to_host())
                        && model.getConnect_to_host().equalsIgnoreCase(com.config.config.ConfigConstant.TRUE)) {
                    hostAlias.put(model.getTitle(), model.getHost());
                    Logger.i("HOST : " + model.getTitle());
                    if (model.getTitle().equalsIgnoreCase(com.config.config.ConfigConstant.HOST_DOWNLOAD_PDF)) {
                        Logger.i("HOST_DOWNLOAD_PDF : " + model.getHost());
                        ConfigPreferences.setBaseUrl(context, model.getHost());
                        Logger.i("HOST_DOWNLOAD_PDF Pref : " + ConfigPreferences.getBaseUrl(context));
                    }
                    if (model.getTitle().equalsIgnoreCase(com.config.config.ConfigConstant.HOST_TRANSLATOR)) {
                        ConfigPreferences.setBaseUrl_3(context, model.getHost());
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


    //***************************************** Simple Network Call without host**************************

    private boolean isEnableConfigManager = true;

    public ConfigManager setEnableConfigManager(boolean enableConfigManager) {
        this.isEnableConfigManager = enableConfigManager;
        return this;
    }

    private boolean isLoadFromCache = false;

    public ConfigManager setEnableLoadFromCache(boolean isLoadFromCache) {
        this.isLoadFromCache = isLoadFromCache;
        return this;
    }

    public void getData(final int type, final String endPoint, final Map<String, String> param
            , final OnNetworkCall onNetworkCall) {
        getData(type, endPoint, param, null, null, onNetworkCall);
    }

    public void getData(final int type, final String endPoint, final Map<String, String> param
            , RequestBody requestBody, MultipartBody.Part multipartBody, final OnNetworkCall onNetworkCall) {
        if (context != null && ConfigUtil.isConnected(context)) {
            if (param != null) {
                if (param.get("application_id") == null) {
                    param.put("application_id", context.getPackageName());
                }
                if (param.get("app_version") == null) {
                    param.put("app_version", getAppVersion());
                }
            }
            getDataRelease(type, endPoint, param, requestBody, multipartBody, onNetworkCall);
        } else if (onNetworkCall != null) {
            onNetworkCall.onComplete(false, "");
            Logger.e(Logger.getClassPath(Thread.currentThread().getStackTrace()), "ApiEndPoint:" + endPoint, "No Internet Connection");
        }
    }

    public void getDataRelease(int type, final String endPoint, Map<String, String> param
            , RequestBody requestBody, MultipartBody.Part multipartBody, final OnNetworkCall onNetworkCall) {
        ApiInterface apiInterface = getApiInterface();
        if (apiInterface != null) {
            Logger.i("getData -- " + endPoint);
            Call<BaseModel> call = getCall(type, apiInterface, endPoint, param, requestBody, multipartBody);
            call.enqueue(new ResponseCallBackSimple(onNetworkCall, endPoint));
        } else if (onNetworkCall != null) {
            onNetworkCall.onComplete(false, "");
            Logger.e(Logger.getClassPath(Thread.currentThread().getStackTrace()), "ApiEndPoint:" + endPoint);
        }
    }

    //***************************************** Simple Network Call without host and fix model**************************

    public void getData(final int type, final String endPoint, final Map<String, String> param
            , final OnNetworkCallBasic onNetworkCall) {
        getData(type, endPoint, param, null, null, onNetworkCall);
    }

    public void getData(final int type, final String endPoint, final Map<String, String> param
            , RequestBody requestBody, MultipartBody.Part multipartBody, final OnNetworkCallBasic onNetworkCall) {
        if (context != null && ConfigUtil.isConnected(context)) {
            if (param != null) {
                if (param.get("application_id") == null) {
                    param.put("application_id", context.getPackageName());
                }
                if (param.get("app_version") == null) {
                    param.put("app_version", getAppVersion());
                }
            }
            getDataRelease(type, endPoint, param, requestBody, multipartBody, onNetworkCall);
        } else if (onNetworkCall != null) {
            onNetworkCall.onComplete(false, "");
            Logger.e(Logger.getClassPath(Thread.currentThread().getStackTrace()), "ApiEndPoint:" + endPoint, "No Internet Connection");
        }
    }

    public void getDataRelease(int type, final String endPoint, Map<String, String> param
            , RequestBody requestBody, MultipartBody.Part multipartBody, final OnNetworkCallBasic onNetworkCall) {
        ApiInterfaceBasic apiInterface = getHostInterfaceBasic();
        if (apiInterface != null) {
            Logger.i("getData -- " + endPoint);
            getCall(type, apiInterface, endPoint, param, requestBody, multipartBody)
                    .enqueue(new ResponseCallBackBasic(onNetworkCall, endPoint));
        } else if (onNetworkCall != null) {
            onNetworkCall.onComplete(false, "");
            Logger.e(Logger.getClassPath(Thread.currentThread().getStackTrace()), "ApiEndPoint:" + endPoint);
        }
    }


    private Call<Object> getCall(int callType, ApiInterfaceBasic apiInterface, String endPoint, Map<String, String> param
            , RequestBody requestBody, MultipartBody.Part multipartBody) {
        Call<Object> call;
        switch (callType) {
            case com.config.config.ConfigConstant.CALL_TYPE_POST:
                call = apiInterface.postData(endPoint, param);
                break;
            case com.config.config.ConfigConstant.CALL_TYPE_POST_FORM:
                call = apiInterface.postDataForm(endPoint, param);
                break;
            case com.config.config.ConfigConstant.CALL_TYPE_POST_FILE:
                call = apiInterface.postDataForm(endPoint, param, requestBody, multipartBody);
                break;
            case com.config.config.ConfigConstant.CALL_TYPE_GET:
            default:
                call = apiInterface.getData(endPoint, param);
                break;
        }
        return call;
    }


    public ApiInterface getApiInterface() {
        return getHostInterface();
    }

    private ApiInterface getHostInterface() {
        return RetrofitGenerator.getClient(getHostConfigPath(), securityCode, isDebug).create(ApiInterface.class);
    }

    private ApiInterfaceBasic getHostInterfaceBasic() {
        return RetrofitGenerator.getClient(getHostConfigPath(), securityCode, isDebug).create(ApiInterfaceBasic.class);
    }
}
