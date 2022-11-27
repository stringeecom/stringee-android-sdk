package com.stringee.sdk.sample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StringeeConnectionListener;

import org.json.JSONObject;

/**
 * Created by luannguyen on 6/14/2017.
 */

public class StartActivity extends MActivity {

    private EditText etRoomId;
    private String username;
    private String token = "eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS0NsejhzQ2tKeDNzdU13SmdCdDJ6bUc2T01JbVRYb2Y1LTE2Njc5ODk1ODYiLCJpc3MiOiJTS0NsejhzQ2tKeDNzdU13SmdCdDJ6bUc2T01JbVRYb2Y1IiwiZXhwIjoxNjcwNTgxNTg2LCJ1c2VySWQiOiJsdWFuIn0.7OzOidv4x7E_2qZnAHYjxQ4QoUcqbLK2NYxnulhU4jY";
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

        Button btnCallOUt = (Button) findViewById(R.id.btn_callout);
        btnCallOUt.setOnClickListener(this);

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

            case R.id.btn_callout:
                String phone3 = etRoomId.getText().toString().trim();
                if (phone3.trim().length() > 0) {
                    Intent intent1 = new Intent(this, OutgoingCallActivity.class);
                    intent1.putExtra("phone", phone3);
                    intent1.putExtra("is_callout", true);
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
            public void onIncomingCall2(StringeeCall2 stringeeCall) {
                String callId = stringeeCall.getCallId();
                Common.callsMap2.put(callId, stringeeCall);
                Intent intent = new Intent(StartActivity.this, IncomingCallActivity.class);
                intent.putExtra("call_id", callId);
                startActivity(intent);
            }

            @Override
            public void onConnectionError(StringeeClient client, StringeeError error) {
                Log.d("Stringee", "onConnectionError: " + error.getCode() + " " + error.getMessage());
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
        Common.client.connect(token);
    }
}
