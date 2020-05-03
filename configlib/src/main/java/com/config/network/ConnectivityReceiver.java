package com.config.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import com.config.config.ConfigManager;

public class ConnectivityReceiver extends BroadcastReceiver {

    private ConnectivityListener mConnectivityReceiverListener;
    private static Boolean mLastState = false;

    ConnectivityReceiver(ConnectivityListener listener) {
        mConnectivityReceiverListener = listener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mConnectivityReceiverListener.onNetworkStateChanged(false, isConnected(context));
        }else{ //due to 3 or more times call when network switch
            boolean isConnected;
            if (isConnected(context)) {
                isConnected = true;
            } else {
                isConnected = false;
                mLastState = false;
            }
            if (!mLastState) {
                mLastState = isConnected;
                mConnectivityReceiverListener.onNetworkStateChanged(false, isConnected);
            }
        }
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}