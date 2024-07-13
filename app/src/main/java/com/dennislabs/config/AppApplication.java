package com.dennislabs.config;

import com.config.config.ConfigManager;
import com.config.util.ConfigUtil;
import com.helper.application.BaseApplication;

public class AppApplication extends BaseApplication {
    @Override
    public boolean isDebugMode() {
        return BuildConfig.DEBUG;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ConfigManager.getInstance(this, ConfigUtil.getSecurityCode(this))
                .setDebugMode(isDebugMode());
    }

    @Override
    public void initLibs() {

    }
}
