package com.stringee.sdk.sample;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.stringee.listener.StatusListener;

/**
 * Created by luannguyen on 2/6/2018.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (Common.client != null) {
            Common.client.registerPushToken(refreshedToken, new StatusListener() {
                @Override
                public void onSuccess() {

                }
            });
        }
    }
}
