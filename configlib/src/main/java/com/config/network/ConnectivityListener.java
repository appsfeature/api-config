package com.config.network;

public interface ConnectivityListener {
    /**
     * @param isConfigLoaded (param1) give the state of config library
     * @param isConnected (param2) give the status of network connectivity
     */
    void onNetworkStateChanged(boolean isConfigLoaded, boolean isConnected);
}
