package com.config.util;

import android.text.TextUtils;
import android.util.Log;

import com.config.config.ConfigManager;


/**
 * @author Created by Abhijit on 01/03/2019.
 *
 * Usage method
 *             Logger.d(Logger.getClassPath(Thread.currentThread().getStackTrace())+" <- fetchRankFromServer", ErrorCode.ERROR_102+": Invalid User detail");
 */

public class Logger {

    public static final String SDK_NAME = "config-sdk";
    public static final String TAG = SDK_NAME + "-log";
    public static final String TAG_OK_HTTP = SDK_NAME + "-okhttp-log";

    public static void e(String s) {
        if ( ConfigManager.getInstance() != null ) {
            if (ConfigManager.getInstance().isDebugMode()) {
                Log.e(TAG, LINE_BREAK_START);
                Log.e(TAG, s);
                Log.e(TAG, LINE_BREAK_END);
            }
        }
    }

    public static void i(String s) {
        if ( ConfigManager.getInstance() != null ) {
            if (ConfigManager.getInstance().isDebugMode()) {
                Log.d(TAG, s);
            }
        }
    }

    public static void e(String q ,String s ) {
        if ( ConfigManager.getInstance() != null ) {
            if (ConfigManager.getInstance().isDebugMode()) {
                Log.d(TAG, LINE_BREAK_START);
                Log.d(TAG, q + " : " + s);
                Log.d(TAG, LINE_BREAK_END);
            }
        }
    }

    public static void d(String s) {
        if (ConfigManager.getInstance().isDebugMode()) {
            Log.d(TAG, LINE_BREAK_START);
            Log.d(TAG, s);
            Log.d(TAG, LINE_BREAK_END);
        }
    }

    public static void d(String... s) {
        if ( ConfigManager.getInstance() != null ) {
            if (ConfigManager.getInstance().isDebugMode()) {
                Log.d(TAG, LINE_BREAK_START);
                for (String m : s) {
                    Log.d(TAG, m);
                }
                Log.d(TAG, LINE_BREAK_END);
            }
        }
    }

    public static void e(String... s) {
        if ( ConfigManager.getInstance() != null ) {
            if (ConfigManager.getInstance().isDebugMode()) {
                Log.e(TAG_OK_HTTP, LINE_BREAK_START);
                for (String m : s) {
                    Log.e(TAG_OK_HTTP, m);
                }
                Log.e(TAG_OK_HTTP, LINE_BREAK_END);
            }
        }
    }


    /**
     * @param currentThread = Thread.currentThread().getStackTrace()
     * @return Getting the name of the currently executing method
     */
    public static String getClassPath(StackTraceElement[] currentThread) {
        try {
            if(currentThread!=null && currentThread.length>=3){
                if(currentThread[2]!=null){
                    return currentThread[2].toString()+" [Line Number = "+currentThread[2].getLineNumber()+"]";
                }
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }

//    public static String getClassPath(Class<?> classReference, String methodName) {
////        if(methodName == null){
////            methodName = "";
////        }
////        return classReference.getName() + "->" + methodName + "";
////    }


    public static String leak(String... errors) {
        String errorList = TextUtils.join(",", errors);
        return "Might be null (" + errorList + ")";
    }

    public static final String LINE_BREAK_START = "-----------------------------"+SDK_NAME+"----------------------------->";
    public static final String LINE_BREAK_END = "<-----------------------------"+SDK_NAME+"------------------------------";

}
