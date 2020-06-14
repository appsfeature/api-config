package com.config.util;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigPreferences {

    private static SharedPreferences sharedPreferences;

    static String HOSTNAME = "http://appsfeature.com/" ;
    public static final String API_URL = "api_url";
    public static final String API_URL_3 = "api_url_3";

    public static SharedPreferences getSharedPreferenceObj(Context context){
        if ( sharedPreferences == null )
            sharedPreferences = context.getSharedPreferences(context.getPackageName() , Context.MODE_PRIVATE);

        return sharedPreferences ;
    }
    public static String getBaseUrl(Context context){
        return getSharedPreferenceObj(context).getString( API_URL, HOSTNAME );
    }

    public static void setBaseUrl(Context context , String reg){
        getSharedPreferenceObj(context).edit().putString( API_URL, reg).commit();
    }

    public static void setBaseUrl_3(Context context , String reg){
        getSharedPreferenceObj(context).edit().putString( API_URL_3, reg).commit();
    }
}
