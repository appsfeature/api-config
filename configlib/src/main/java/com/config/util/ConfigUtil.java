package com.config.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;

import com.config.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ConfigUtil {
    public static boolean isEmptyOrNull(String s) {
        return (s == null || TextUtils.isEmpty(s));
    }

    public static String getDeviceId(Context context) {
        if (context != null) {
            return Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } else {
            return "";
        }
    }

    public static boolean isConnected(Context context) {
        boolean isConnected = false;
        try {
            if ( context != null && context.getSystemService(Context.CONNECTIVITY_SERVICE) != null
                    && context.getSystemService(Context.CONNECTIVITY_SERVICE) instanceof ConnectivityManager ) {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                isConnected = false;
                if (connectivityManager != null) {
                    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                    isConnected = (activeNetwork != null) && (activeNetwork.isConnected());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isConnected;
    }


    public static String getSecurityCode(Context ctx) {
        String keyHash = null;
        try {
            Signature[] signatures;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageInfo info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
                signatures = info.signingInfo.getSigningCertificateHistory();
            } else {
                PackageInfo info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_SIGNATURES);
                signatures = info.signatures;
            }
            for (Signature signature : signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }
//        Log.e("printHashKey", "keyHash : " + keyHash);
        return keyHash;
    }


    /**
     * add flag when open activity with context reference
     * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     */
    public static void share(Context context, String message) {
        String appLink = message + "Download " + context.getString(R.string.app_name) + " app. \nLink : http://play.google.com/store/apps/details?id=";
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, appLink + context.getPackageName());
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * add flag when open activity with context reference
     * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     */
    public static void rateUs(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * add flag when open activity with context reference
     * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     */
    public static void moreApps(Context context, String developerName) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id="+ developerName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


}
