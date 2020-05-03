package com.config.config;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Amit on 3/28/2018.
 */

public class ConfigPreferences {

    public ConfigPreferences(Context context) {
        PRE_TEXT = context.getPackageName() + "_" ;
        initSharedPreferenceObj(context);
    }

    private ConfigPreferences(){}
    private static SharedPreferences sharedPreferences;
    private String PRE_TEXT ;

    public static SharedPreferences initSharedPreferenceObj(Context context){
        if ( sharedPreferences == null )
            sharedPreferences = context.getSharedPreferences(context.getPackageName() , Context.MODE_PRIVATE);

        return sharedPreferences ;
    }

    String getString(String key){
        return sharedPreferences.getString( PRE_TEXT + key , null );
    }

    void putString(String key , String value){
        sharedPreferences.edit().putString( PRE_TEXT + key, value ).apply();
    }
}
