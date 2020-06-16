package com.stringee.sdk.sample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeConnectionListener;

import org.json.JSONObject;

/**
 * Created by luannguyen on 6/14/2017.
 */

public class StartActivity extends MActivity {

    private EditText etRoomId;
    private String username;
    private String token;
    TextView tvTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        username = getIntent().getStringExtra(Constant.USER_NAME);
        if (username == null) {
            username = PrefUtils.getString(Constant.USER_NAME, "");
        }

        tvTitle = (TextView) findViewById(R.id.tv_title);

        Button btnMakeRoom = (Button) findViewById(R.id.btn_make);
        btnMakeRoom.setOnClickListener(this);

        Button btnJoin = (Button) findViewById(R.id.btn_join);
        btnJoin.setOnClickListener(this);

        Button btnVoiceCall = (Button) findViewById(R.id.btn_voice_call);
        btnVoiceCall.setOnClickListener(this);

        Button btnVideoCall = (Button) findViewById(R.id.btn_video_call);
        btnVideoCall.setOnClickListener(this);

        etRoomId = (EditText) findViewById(R.id.et_room_id);

        initStringee();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_voice_call:
                String phone = etRoomId.getText().toString().trim();
                if (phone.trim().length() > 0) {
                    Intent intent1 = new Intent(this, OutgoingCallActivity.class);
                    intent1.putExtra("phone", phone);
                    startActivity(intent1);
                }
                break;

            case R.id.btn_video_call:
                String phone2 = etRoomId.getText().toString().trim();
                if (phone2.trim().length() > 0) {
                    Intent intent1 = new Intent(this, OutgoingCallActivity.class);
                    intent1.putExtra("phone", phone2);
                    intent1.putExtra("video", true);
                    startActivity(intent1);
                }
                break;
            case R.id.btn_make:
                Intent intent = new Intent(this, MeetingActivity.class);
                intent.putExtra("action", "make");
                startActivity(intent);
                break;
            case R.id.btn_join:
                String roomId = etRoomId.getText().toString().trim();
                if (roomId.trim().length() > 0) {
                    Intent intent1 = new Intent(this, MeetingActivity.class);
                    intent1.putExtra("action", "join");
                    intent1.putExtra("roomId", Integer.parseInt(roomId));
                    startActivity(intent1);
                }
                break;
        }
    }

    private void initStringee() {
        Common.client = new StringeeClient(this);
        Common.client.setConnectionListener(new StringeeConnectionListener() {
            @Override
            public void onConnectionConnected(StringeeClient client, boolean isReconnecting) {
                PrefUtils.putString(Constant.USER_NAME, client.getUserId());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvTitle.setText("My username: " + client.getUserId());
                    }
                });
                Log.e("Stringee", "========= onConnectionConnected");
                final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                Common.client.registerPushToken(refreshedToken, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("Stringee", "registerPushToken success.");
                        PrefUtils.putString("token", refreshedToken);
                    }

                    public void onError(StringeeError error) {
                        Log.d("Stringee", error.getMessage());
                    }
                });
            }

            @Override
            public void onConnectionDisconnected(StringeeClient client, boolean isReconnecting) {
                Log.e("Stringee", "========= onConnectionDisconnected");
            }

            @Override
            public void onIncomingCall(StringeeCall stringeeCall) {
                String callId = stringeeCall.getCallId();
                Common.callsMap.put(callId, stringeeCall);
                Intent intent = new Intent(StartActivity.this, IncomingCallActivity.class);
                intent.putExtra("call_id", callId);
                startActivity(intent);
            }

            @Override
            public void onConnectionError(StringeeClient client, StringeeError error) {

            }

            @Override
            public void onRequestNewToken(StringeeClient client) {
            }

            @Override
            public void onCustomMessage(String from, JSONObject msg) {

            }

            @Override
            public void onTopicMessage(String s, JSONObject jsonObject) {

            }
        });
        Common.client.connect("eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS0NsejhzQ2tKeDNzdU13SmdCdDJ6bUc2T01JbVRYb2Y1LTE1OTIyMTM4MDEiLCJpc3MiOiJTS0NsejhzQ2tKeDNzdU13SmdCdDJ6bUc2T01JbVRYb2Y1IiwiZXhwIjoxNTk0ODA1ODAxLCJ1c2VySWQiOiJsdWFuIn0.RbnkbNC6N5ThBbmKncNxUeWbUG5fJYbk15zTQpQ98Mc");
    }
}
