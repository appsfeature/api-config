package com.config.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class BroadcastManager {

    public static final String ACTION_NAME_UNIQUE = "ACTION_NAME_UNIQUE";
    public static final String INTENT_BOOLEAN_CONNECTED = "intent_data";

    private final Context context;

    public BroadcastManager(Context context, BroadcastReceiver mRandomNumberReceiver) {
        this.mRandomNumberReceiver = mRandomNumberReceiver;
        this.context = context;
    }

    private BroadcastReceiver mRandomNumberReceiver;


    // Send the broadcast
    public static void sendBroadcast(Context context, String actionName, boolean isConnected) {
        Intent intent = new Intent(actionName);
        intent.putExtra(INTENT_BOOLEAN_CONNECTED, isConnected);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    public void register(String actionName) {
        if (actionName == null)
            actionName = ACTION_NAME_UNIQUE;

        // Register the local broadcast
        LocalBroadcastManager.getInstance(context).registerReceiver(
                mRandomNumberReceiver,
                new IntentFilter(actionName)
        );
    }

    public void unregister() {
        if (mRandomNumberReceiver != null) {
            // Register the local broadcast
            LocalBroadcastManager.getInstance(context).unregisterReceiver(
                    mRandomNumberReceiver
            );
        }
    }
}
