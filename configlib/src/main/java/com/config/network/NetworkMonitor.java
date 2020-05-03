package com.config.network;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.SparseArray;
import android.util.SparseIntArray;

import androidx.annotation.RequiresApi;

import com.config.config.ConfigManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class NetworkMonitor {

    private static NetworkMonitor mInstance;
    private ConnectivityReceiver mNetworkReceiver;

    //    private ConnectivityListener connectivityListener;
    private boolean isConfigLoaded = false;
    private HashMap<Integer, ConnectivityListener> connectivityListenerList = new HashMap<>();

    public static NetworkMonitor getInstance() {
        if (mInstance == null) {
            mInstance = new NetworkMonitor();
        }
        return mInstance;
    }

//    public ConnectivityListener getConnectivityListener() {
//        return connectivityListener;
//    }

    /**
     * @param connectivityListener : use only for check network state and update UI
     *                             this.hashCode();
     */
    public void setConnectivityListener(int hashCode, ConnectivityListener connectivityListener) {
        this.connectivityListenerList.put(hashCode, connectivityListener);
    }

    /**
     * remove ConnectivityListener from Activity HashCode
     *
     * @param hashCode
     */
    public void removeConnectivityListener(int hashCode) {
        if (connectivityListenerList != null && connectivityListenerList.get(hashCode) != null) {
            this.connectivityListenerList.remove(hashCode);
        }
    }


    private List<OnConfigLoadedCallback> onConfigLoadedList = new ArrayList<>();

    public void setOnConfigLoadedList(OnConfigLoadedCallback onConfigLoaded) {
        this.onConfigLoadedList.add(onConfigLoaded);
    }

    public List<OnConfigLoadedCallback> getOnConfigLoadedList() {
        return onConfigLoadedList;
    }

    public interface OnConfigLoadedCallback {
        boolean onConfigLoaded(boolean isConfigLoaded);
    }

    private void refreshOnConfigLoadedCallbacks() {
        try {
            if (onConfigLoadedList != null && onConfigLoadedList.size() > 0) {
                for (OnConfigLoadedCallback onConfigLoadedCallback : onConfigLoadedList) {
                    if (onConfigLoadedCallback != null) {
                        onConfigLoadedCallback.onConfigLoaded(isConfigLoaded);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        refreshConnectivityListener();
    }

    private void refreshConnectivityListener() {
        try {
            if (connectivityListenerList != null && connectivityListenerList.size() > 0) {
                Set<Integer> integerSet = connectivityListenerList.keySet();
                if (integerSet != null && integerSet.size() > 0) {
                    for (Integer integer : integerSet) {
                        if (integer != null){
                            ConnectivityListener listener = connectivityListenerList.get(integer);
                            if ( listener != null ){
                                listener.onNetworkStateChanged(isConfigLoaded, mIsConnected);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param activity MainActivity reference
     * @since Use this in activity onStart() method
     */
    public void register(Activity activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                scheduleJob(activity);
            } else {
                registerConnectivityReceiver(activity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param activity MainActivity reference
     * @since Use this in activity onDestroy() method before super method call
     */
    public void unregister(Activity activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                stopService(activity);
            } else {
                unregisterConnectivityReceiver(activity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void refreshConfig(Context context, boolean isConnected) {
        if (ConfigManager.getInstance() != null && !isConfigLoaded) {
            ConfigManager.getInstance().refreshConfig();
            if (context != null) {
                BroadcastManager.sendBroadcast(context, BroadcastManager.ACTION_NAME_UNIQUE, isConnected);
            }
        }
        updateCallback(isConnected);
    }

    private boolean isFirstHit = true;
    private boolean mIsConnected = false;

    private void updateCallback(boolean isConnected) {
        if (mIsConnected != isConnected) {
            mIsConnected = isConnected;
            isFirstHit = true;
        }
        if (isFirstHit && connectivityListenerList.size() > 0) {
            isFirstHit = false;
            refreshConnectivityListener();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void scheduleJob(Activity activity) {
        JobInfo myJob = new JobInfo.Builder(0, new ComponentName(activity, NetworkSchedulerService.class))
                .setRequiresCharging(true)
                .setMinimumLatency(1000)
                .setOverrideDeadline(2000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build();
//                .setPersisted(true)

        JobScheduler jobScheduler = (JobScheduler) activity.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(myJob);
        startService(activity);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startService(Activity activity) {
//        super.onStart();
        Intent startServiceIntent = new Intent(activity, NetworkSchedulerService.class);
        activity.startService(startServiceIntent);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void stopService(Activity activity) {
        // A service can be "started" and/or "bound". In this case, it's "started" by this Activity
        // and "bound" to the JobScheduler (also called "Scheduled" by the JobScheduler). This call
        // to stopService() won't prevent scheduled jobs to be processed. However, failing
        // to call stopService() would keep it alive indefinitely.
        activity.stopService(new Intent(activity, NetworkSchedulerService.class));
//        super.onStop();
    }


    private void registerConnectivityReceiver(final Context context) {
        mNetworkReceiver = new ConnectivityReceiver(new ConnectivityListener() {
            @Override
            public void onNetworkStateChanged(boolean isConfigLoaded, boolean isConnected) {
                refreshConfig(context, isConnected);
            }
        });
        context.registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void unregisterConnectivityReceiver(Context context) {
        if (mNetworkReceiver != null) {
            try {
                context.unregisterReceiver(mNetworkReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    public void setConfigLoaded(boolean isConfigLoaded, boolean mIsConnected) {
        this.isConfigLoaded = isConfigLoaded;
        this.mIsConnected = mIsConnected;
        refreshOnConfigLoadedCallbacks();
    }
}
