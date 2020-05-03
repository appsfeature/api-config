package com.config.network;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;

import com.config.config.ConfigManager;
import com.config.util.Logger;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NetworkSchedulerService extends JobService implements
        ConnectivityListener {

    private static final String TAG = NetworkSchedulerService.class.getSimpleName();

    private ConnectivityReceiver mConnectivityReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.e(TAG, "Service created");
        try {
            mConnectivityReceiver = new ConnectivityReceiver(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * When the app's NetworkConnectionActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCallback()"
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.e(TAG, "onStartCommand");
        return START_NOT_STICKY;
    }


    @Override
    public boolean onStartJob(JobParameters params) {
        Logger.e(TAG, "onStartJob" + mConnectivityReceiver);
        try {
            if (mConnectivityReceiver != null)
                registerReceiver(mConnectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Logger.e(TAG, "onStopJob");
        try {
            if (mConnectivityReceiver != null)
                unregisterReceiver(mConnectivityReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onNetworkStateChanged(boolean isConfigLoaded, boolean isConnected) {
        Logger.e(TAG, "onNetworkStateChanged");
        if (ConfigManager.getInstance() != null) {
            ConfigManager.getInstance().getNetworkMonitor().refreshConfig(getApplicationContext(), isConnected);
        }
    }
}