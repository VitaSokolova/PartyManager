package com.vsu.nastya.partymanager;

import android.app.Application;

import com.vk.sdk.VKSdk;

/**
 * Created by nastya on 27.11.16.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
    }
}
