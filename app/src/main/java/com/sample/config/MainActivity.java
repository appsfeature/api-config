package com.sample.config;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ConfigManager configManager = ConfigManager.getInstance(this, SupportUtil.getSecurityCode(this));
    }
}
