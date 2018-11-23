package com.stringee.sdk.sample;

import android.app.Application;

/**
 * Created by luannguyen on 2/12/2018.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Common.preferences = getSharedPreferences(Constant.PREF_BASE, MODE_PRIVATE);
    }
}
