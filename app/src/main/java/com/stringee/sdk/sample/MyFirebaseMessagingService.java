package com.stringee.sdk.sample;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by luannguyen on 2/6/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (Common.client == null) {
            startActivity(new Intent(this, StartActivity.class));
        }
    }
}
