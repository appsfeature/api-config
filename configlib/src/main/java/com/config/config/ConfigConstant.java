package com.config.config;

/**
 * Created by Amit on 3/28/2018.
 */

public interface ConfigConstant {

    String HOST_ANALYTICS = "analytics";
    String CONFIG_LOADED = "_config_loaded";
    String CONFIG_FAILURE = "_config_failure";
    String HOST_PAID = "gk_paid";
    String HOST_MAIN = "gk_main_host";
    String HOST_TRANSLATOR = "translater_host";
    String HOST_LOGIN = "login_host";
    String HOST_LEADER_BOARD = "leaderboard_host";
    String HOST_DOWNLOAD_PDF = "download_pdf";
    String CONFIG_HOST = "config_host";
    String CONFIG_HOST_URL = "http://appsfeature.com/";
    String CONFIG_HOST_BACKUP = "config_host_backup";
    String CONFIG_HOST_BACKUP_URL = "http://appsfeature.com/";
    int BACKUP_CONFIG_CALL_COUNT = 5;
    String SUCCESS = "success";
    String TRUE = "true";
    String FALSE = "false";
    int CALL_TYPE_GET = 0;
    int CALL_TYPE_POST = 1;
    int CALL_TYPE_POST_FORM = 2;
    int CALL_TYPE_POST_FILE = 3;
}
