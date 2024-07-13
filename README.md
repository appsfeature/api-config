# Config Library

#### Library size is : 453Kb

Android library that manages your app's networking with ease.
   

Add this to your project build.gradle
``` gradle
allprojects {
    repositories {
        maven {
            url "https://jitpack.io"
        }
    }
    ext {
        firebase_crashlytics_version = '2.9.9' 
    }
}
```
 

#### Dependency
[![](https://jitpack.io/v/appsfeature/api-config.svg)](https://jitpack.io/#appsfeature/api-config)
```gradle
dependencies {
        implementation 'com.github.appsfeature:api-config:x.y'
}
```

# Simple Integration
Add this to your AppApplication.class
```java 
    @Override
    public void onCreate() {
        super.onCreate(); 
        configManager = ConfigManager.getInstance(this, SupportUtil.getSecurityCode(this), BuildConfig.DEBUG);
    }
    
    public void loadConfig() {
        if (configManager != null) {
            configManager.loadConfig();
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

```
Add this to your MainActivity.class
```java 

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); 
        callConfig();
    }
    
    private void callConfig() {
        if (SupportUtil.isConnected(this)) {
            if (MyApplication.get() != null &&
                    (MyApplication.get().getConfigManager() == null
                            || !MyApplication.get().getConfigManager().isConfigLoaded())) {
                showConnectingTextView(NetworkConfig.CONNECTING);
                MyApplication.get().loadConfig();
            } else if (MyApplication.get() != null && MyApplication.get().getConfigManager() != null
                    && MyApplication.get().getConfigManager().isConfigLoaded())
                showConnectingTextView(NetworkConfig.CONNECTED);
        } else {
            showConnectingTextView(NetworkConfig.NOT_CONNECTED);
        }
    }
}
public enum NetworkConfig {
   CONNECTING,CONNECTED,NOT_CONNECTED
}
```

# Advance Integration
Add this to your AppApplication.class
```java 
    @Override
    public void onCreate() {
        super.onCreate(); 
        configManager = ConfigManager.getInstance(this, SupportUtil.getSecurityCode(this), BuildConfig.DEBUG); 
        initLoginSdk();
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }

     /**
     * register from MainActivity
     */
    public void registerReceiver() {
        registerReceiver(configReceiver, new IntentFilter(getPackageName() + ConfigConstant.CONFIG_LOADED)); 
    }

    /**
     * unregister from MainActivity
     */
    public void unregisterReceiver() {
        Logger.e("appApplication","onTerminate");
        try { 
            if (configReceiver != null)
                unregisterReceiver(configReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isSync = false;

    BroadcastReceiver configReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isSync) {
                syncData();
                isSync = true;
            }
        }
    };
    
    public void syncData() {
//        getCatData();
        if (configManager != null && configManager.getHostAlias() != null
                && configManager.getHostAlias().get(ConfigConstant.HOST_LOGIN) != null && LoginSdk.getInstance() != null) {
            LoginSdk.getInstance().setApiInterface(RetrofitGenerator.getClient(configManager.getHostAlias()
                    .get(ConfigConstant.HOST_LOGIN), SupportUtil.getSecurityCode(this), true));
        }
        if (configManager != null
                && configManager.getHostAlias() != null
                && configManager.getHostAlias().size() > 0
                && configManager.getHostAlias().get(ConfigConstant.HOST_ANALYTICS) != null) {
//            SupportUtil.getSecurityCodeByte(this);
        }
//        syncLocalData();
    }

    public void initOperations() {
        isSync = false;
        configManager.loadConfig();
        initAds();
        initLoginSdk();
    }

    private void initLoginSdk() {
        if (loginSdk == null) {
            Retrofit retrofit = null;
            if (configManager.getHostAlias() != null && configManager.getHostAlias().get(ConfigConstant.HOST_LOGIN) != null) {
                retrofit = RetrofitGenerator.getClient(configManager.getHostAlias()
                        .get(ConfigConstant.HOST_LOGIN), SupportUtil.getSecurityCode(this));
            }
            loginSdk = LoginSdk.getInstance(this, retrofit, false
                    , true, true, getPackageName());
            LoginSdk.getInstance().setUserImageUrl(SupportUtil.getUserImageUrl());
            LoginSdk.getInstance().setCallConfig(new LoginSdk.CallConfig() {
                @Override
                public void callConfig(boolean isMain, boolean isBug, String bug) {
                    configManager.callConfig(isMain, isBug, bug);
                }
            });
            handlePreVersionLogin();
        }
    }

    public void handlePreVersionLogin() {
        if (getLoginSdk() != null) {
            if (SupportUtil.isEmptyOrNull(getLoginSdk().getUserId())
                    && !SupportUtil.isEmptyOrNull(SharedPrefUtil.getString(AppConstant.SharedPref.USER_ID_AUTO))) {
                Util.updatePref(AppConstant.SharedPref.USER_ID_AUTO, SharedPrefUtil.getString(AppConstant.SharedPref.USER_ID_AUTO));
                Util.updatePref(AppConstant.SharedPref.USER_EMAIL, SharedPrefUtil.getString(AppConstant.SharedPref.USER_EMAIL));
                Util.updatePref(AppConstant.SharedPref.USER_NAME, SharedPrefUtil.getString(AppConstant.SharedPref.USER_NAME));
                Util.updatePref(AppConstant.SharedPref.USER_UID, SharedPrefUtil.getString(AppConstant.SharedPref.USER_UID));
                Util.updatePref(AppConstant.SharedPref.USER_PHOTO_URL, SharedPrefUtil.getString(AppConstant.SharedPref.USER_PHOTO_URL));
                getLoginSdk().setLoginComplete(true);
                getLoginSdk().setRegComplete(true);
            }
        }
    }


```

Add this to your MainActivity.class
```java 

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
         
         registerReceiverCallBacks();
         callConfig();
    }

    @Override
    public void finish() {
        AppApplication.getInstance().unregisterReceiver(); 
        AppApplication.getInstance().getConfigManager().getNetworkMonitor().unregister(this);
        super.finish();
    }

    private void registerReceiverCallBacks() { 
        AppApplication.getInstance().getConfigManager().getNetworkMonitor().register(this);
        AppApplication.getInstance().registerReceiver();
    } 
    
    private void callConfig() {
        if (SupportUtil.isConnected(this)) {
            if (AppApplication.getInstance() != null &&
                    (AppApplication.getInstance().getConfigManager() == null
                            || !AppApplication.getInstance().getConfigManager().isConfigLoaded())) {
                showConnectingTextView();
                AppApplication.getInstance().initOperations();
            } else if (AppApplication.getInstance().getConfigManager() != null
                    && AppApplication.getInstance().getConfigManager().isConfigLoaded())
                showSuccessConnectionTextView();
        } else
            showNotConnectTextView();
    }
 

```

#### Usage method
In your activity class:
```java 

    ConfigManager configManager = ConfigManager.getInstance(this, SupportUtil.getSecurityCode(this), BuildConfig.DEBUG);
        
    protected void detectConnectivityChange() {
        configManager.getNetworkMonitor().setConnectivityListener(new ConnectivityListener() {
            @Override
            public void onNetworkStateChanged(boolean isConfigLoaded, boolean isConnected) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        configManager.getNetworkMonitor().register(this);
    }
 
    @Override
    public void onDestroy() {
        configManager.getNetworkMonitor().unregister(this);
        super.onDestroy();
    }

```

 
