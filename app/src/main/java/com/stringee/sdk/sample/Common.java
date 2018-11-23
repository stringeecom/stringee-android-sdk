package com.stringee.sdk.sample;

import android.content.SharedPreferences;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by luannguyen on 3/18/2016.
 */
public class Common {
    public static SharedPreferences preferences;
    public static StringeeClient client;
    public static Map<String, StringeeCall> callsMap = new HashMap<>();
}
