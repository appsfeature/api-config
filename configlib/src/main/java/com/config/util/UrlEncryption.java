package com.config.util;

import android.text.TextUtils;

import com.config.config.ConfigManager;
import com.helper.util.EncryptionHandler;


public class UrlEncryption {

    public static String get(String fbServerId) {
        if(!TextUtils.isEmpty(ConfigManager.getInstance().getEncDataKey())){
            return EncryptionHandler.decrypt(ConfigManager.getInstance().getEncDataKey(), fbServerId);
        }else {
            return fbServerId;
        }
    }
}
