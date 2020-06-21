//package com.config.config;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.os.Build;
//import android.text.TextUtils;
//
//import com.config.BuildConfig;
//import com.config.ConfigProvider;
//import com.config.network.NetworkMonitor;
//import com.config.util.ConfigUtil;
//import com.config.util.Logger;
//import com.google.gson.Gson;
//
//import java.util.Map;
//
//import okhttp3.MultipartBody;
//import okhttp3.RequestBody;
//import retrofit2.Call;
//
///**
// * @author Created by Abhijit on 3/28/2018.
// */
//
//public class ConfigManager2 {
//
//    @SuppressLint("StaticFieldLeak") private static ConfigManager2 configManager;
//    private Context context;
//    private com.config.config.ConfigPreferences configPreferences;
//    private NetworkMonitor networkMonitor;
//
//    public ApiInterface getApiInterface() {
//        return getHostInterface();
//    }
//
//    private ApiInterface getHostInterface() {
//        return RetrofitGenerator.getClient(getHostConfigPath(), securityCode, isDebug).create(ApiInterface.class);
//    }
//
//    private String securityCode;
//    public boolean isDebug = false;
//
//    public static ConfigManager2 getInstance() {
//        if(configManager == null){
//            configManager = getInstance(ConfigProvider.context, ConfigUtil.getSecurityCode(ConfigProvider.context));
//        }
//        return configManager;
//    }
//
//    public static ConfigManager2 getInstance(Context context, String securityCode) {
//        return getInstance(context, securityCode, BuildConfig.DEBUG);
//    }
//
//    public static ConfigManager2 getInstance(Context context, String securityCode, boolean isDebug) {
//        if (configManager == null) {
//            synchronized (ConfigManager2.class) {
//                if (configManager == null) {
//                    configManager = new ConfigManager2(context, securityCode, isDebug);
//                }
//            }
//        }
//        return configManager;
//    }
//
//
//    private ConfigManager2(Context context, String securityCode, boolean isDebug) {
//        init(context, securityCode, isDebug);
//    }
//
//    private void init(Context context, String securityCode, boolean isDebug) {
//        if (context != null) {
//            this.context = context;
//            this.securityCode = securityCode;
//            this.isDebug = isDebug;
//            configPreferences = new com.config.config.ConfigPreferences(context);
//            networkMonitor = getNetworkMonitor();
//        }
//    }
//
//    public void setConfigHost(Context context , String host){
//        if ( context != null && !TextUtils.isEmpty(host)) {
//            getConfigPreferences(context).putString(com.config.config.ConfigConstant.CONFIG_HOST, host);
//        }
//    }
//
//    private String getHostConfigPath() {
//        String s = configPreferences.getString(com.config.config.ConfigConstant.CONFIG_HOST);
//        if (ConfigUtil.isEmptyOrNull(s)) {
//            s = com.config.config.ConfigConstant.CONFIG_HOST_URL;
//        }
//        return s;
//    }
//
//    private com.config.config.ConfigPreferences getConfigPreferences(Context context){
//        if ( configPreferences == null && context != null ){
//            configPreferences = new com.config.config.ConfigPreferences(context);
//        }
//        return configPreferences;
//    }
//
//    private static Gson gson = new Gson();
//
//    public static Gson getGson() {
//        return gson;
//    }
//
//    public void getData(final int type, final String endPoint, final Map<String, String> param
//            , final OnNetworkCall onNetworkCall) {
//        getData(type, endPoint, param, null, null, onNetworkCall);
//    }
//
//    public void getData(final int type, final String endPoint, final Map<String, String> param
//            , RequestBody requestBody, MultipartBody.Part multipartBody, final OnNetworkCall onNetworkCall) {
//        if (context != null && ConfigUtil.isConnected(context)) {
//            if (param != null) {
//                if (param.get("application_id") == null) {
//                    param.put("application_id", context.getPackageName());
//                }
//                if (param.get("app_version") == null) {
//                    param.put("app_version", getAppVersion());
//                }
//            }
//            getDataRelease(type, endPoint, param, requestBody, multipartBody, onNetworkCall);
//        } else if (onNetworkCall != null) {
//            onNetworkCall.onComplete(false, "");
//            Logger.e(Logger.getClassPath(Thread.currentThread().getStackTrace()), "ApiEndPoint:" + endPoint, "No Internet Connection");
//        }
//    }
//
//    public void getDataRelease(int type, final String endPoint, Map<String, String> param
//            , RequestBody requestBody, MultipartBody.Part multipartBody, final OnNetworkCall onNetworkCall) {
//        ApiInterface apiInterface = getApiInterface();
//        if (apiInterface != null) {
//            Logger.i("getData -- " + endPoint);
//            Call<BaseModel> call = getCall(type, apiInterface, endPoint, param, requestBody, multipartBody);
//            call.enqueue(new ResponseCallBackSimple(onNetworkCall, endPoint));
//        } else if (onNetworkCall != null) {
//            onNetworkCall.onComplete(false, "");
//            Logger.e(Logger.getClassPath(Thread.currentThread().getStackTrace()), "ApiEndPoint:" + endPoint);
//        }
//    }
//
//    private Call<BaseModel> getCall(int callType, ApiInterface apiInterface, String endPoint, Map<String, String> param
//            , RequestBody requestBody, MultipartBody.Part multipartBody) {
//        Call<BaseModel> call;
//        switch (callType) {
//            case com.config.config.ConfigConstant.CALL_TYPE_POST:
//                call = apiInterface.postData(endPoint, param);
//                break;
//            case com.config.config.ConfigConstant.CALL_TYPE_POST_FORM:
//                call = apiInterface.postDataForm(endPoint, param);
//                break;
//            case com.config.config.ConfigConstant.CALL_TYPE_POST_FILE:
//                call = apiInterface.postDataForm(endPoint, param, requestBody, multipartBody);
//                break;
//            case com.config.config.ConfigConstant.CALL_TYPE_GET:
//            default:
//                call = apiInterface.getData(endPoint, param);
//                break;
//        }
//        return call;
//    }
//
//
//    private String getAppVersion() {
//        String appVersion = "0";
//        try {
//            if(context!=null) {
//                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                    appVersion = (int) pInfo.getLongVersionCode() + ""; // avoid huge version numbers and you will be ok
//                } else {
//                    //noinspection deprecation
//                    appVersion = pInfo.versionCode + "";
//                }
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        return appVersion;
//    }
//
//    public interface OnNetworkCall {
//        void onComplete(boolean status, String data);
//    }
//
//    public NetworkMonitor getNetworkMonitor() {
//        if (networkMonitor == null) {
//            networkMonitor = NetworkMonitor.getInstance();
//        }
//        return networkMonitor;
//    }
//
//}
