package com.stringee.sdk.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.stringee.call.StringeeCall2;
import com.stringee.common.StringeeAudioManager;
import com.stringee.listener.StatusListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by luannguyen on 8/19/2017.
 */

public class IncomingCallActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvName;
    private TextView tvState;
    private View vSpeaker;
    private View vAccept;
    private ImageButton btnSpeaker;
    private ImageButton btnMute;
    private ImageButton btnEndcall;
    private ImageButton btnVideo;
    private ImageView imNetwork;
    private FrameLayout vLocal;
    private FrameLayout vRemote;

    private ImageButton btnEndcall2;

    private boolean isSpeaker = false;
    private boolean isMute = false;
    private boolean isVideoOn;

    private double mPrevCallTimestamp = 0;
    private long mPrevCallBytes = 0;
    private long mCallBw = 0;

    public static final int REQUEST_PERMISSION_CALLIN = 1;

    private StringeeCall2 mStringeeCall;
    private StringeeAudioManager audioManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_call);

        String callId = getIntent().getStringExtra("call_id");
        mStringeeCall = Common.callsMap.get(callId);
        isVideoOn = mStringeeCall.isVideoCall();

        tvName = (TextView) findViewById(R.id.tv_name);
        tvName.setText(mStringeeCall.getFrom());
        tvState = (TextView) findViewById(R.id.tv_status);
        tvState.setText(R.string.incoming);

        vAccept = findViewById(R.id.v_answer);
        vSpeaker = findViewById(R.id.v_speaker);

        btnSpeaker = (ImageButton) findViewById(R.id.btn_speaker);
        btnSpeaker.setOnClickListener(this);

        btnVideo = (ImageButton) findViewById(R.id.btn_video);
        btnVideo.setOnClickListener(this);
        if (mStringeeCall.isVideoCall()) {
            btnSpeaker.setImageResource(R.drawable.ic_speaker_on);
            btnVideo.setImageResource(R.drawable.ic_video);
        } else {
            btnSpeaker.setImageResource(R.drawable.ic_speaker_off);
            btnVideo.setImageResource(R.drawable.ic_video_off);
        }

        btnMute = (ImageButton) findViewById(R.id.btn_mute);
        btnMute.setOnClickListener(this);
        btnEndcall = (ImageButton) findViewById(R.id.btn_end);
        btnEndcall.setOnClickListener(this);

        imNetwork = (ImageView) findViewById(R.id.im_network);

        ImageButton btnAnswer = (ImageButton) findViewById(R.id.btn_answer);
        btnAnswer.setOnClickListener(this);

        ImageButton btnReject = (ImageButton) findViewById(R.id.btn_reject);
        btnReject.setOnClickListener(this);

        vLocal = (FrameLayout) findViewById(R.id.v_local);
        vRemote = (FrameLayout) findViewById(R.id.v_remote);

        btnEndcall2 = (ImageButton) findViewById(R.id.btn_end2);
        btnEndcall2.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> lstPermissions = new ArrayList<>();

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                lstPermissions.add(Manifest.permission.RECORD_AUDIO);
            }

            if (isVideoOn) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    lstPermissions.add(Manifest.permission.CAMERA);
                }
            }

            if (lstPermissions.size() > 0) {
                String[] permissions = new String[lstPermissions.size()];
                for (int i = 0; i < lstPermissions.size(); i++) {
                    permissions[i] = lstPermissions.get(i);
                }
                ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CALLIN);
                return;
            }
        }
        audioManager = StringeeAudioManager.create(this);
        startCall(mStringeeCall);
    }

    private void startCall(final StringeeCall2 stringeeCall) {
        stringeeCall.setCallListener(new StringeeCall2.StringeeCallListener() {

            @Override
            public void onSignalingStateChange(final StringeeCall2 stringeeCall, final StringeeCall2.SignalingState signalingState, String reason, int sipCode, String sipReason) {
                Log.e("Stringee", "onSignalingStateChange " + reason);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (signalingState == StringeeCall2.SignalingState.ENDED) {
                            if (mStringeeCall != null && mStringeeCall.getCallId().equals(stringeeCall.getCallId())) {
                                tvState.setText(R.string.call_ended);
                                mStringeeCall.hangup();
                                audioManager.stop();
                                finish();
                            }
                        }
                    }
                });
            }

            @Override
            public void onError(StringeeCall2 stringeeCall, int code, String description) {
                Log.e("Stringee", "========== error: " + description);
            }

            @Override
            public void onHandledOnAnotherDevice(StringeeCall2 stringeeCall, final StringeeCall2.SignalingState callState, String description) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (callState) {
                            case ANSWERED:
                            case BUSY:
                            case ENDED:
                                mStringeeCall.hangup();
                                audioManager.stop();
                                finish();
                                break;
                        }
                    }
                });
            }

            @Override
            public void onMediaStateChange(final StringeeCall2 stringeeCall, StringeeCall2.MediaState mediaState) {
                if (mediaState == StringeeCall2.MediaState.CONNECTED) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mStringeeCall != null && mStringeeCall.getCallId().equals(stringeeCall.getCallId())) {
                                tvState.setText("Started");
                            }
                        }
                    });
                }
            }

            @Override
            public void onLocalStream(final StringeeCall2 stringeeCall) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (stringeeCall.isVideoCall()) {
                            vLocal.removeAllViews();
                            vLocal.addView(stringeeCall.getLocalView());
                            stringeeCall.renderLocalView(true);
                        }
                    }
                });
            }

            @Override
            public void onRemoteStream(final StringeeCall2 stringeeCall) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (stringeeCall.isVideoCall()) {
                            vRemote.removeAllViews();
                            vRemote.addView(stringeeCall.getRemoteView());
                            stringeeCall.renderRemoteView(false);
                        }
                    }
                });
            }

            @Override
            public void onCallInfo(final StringeeCall2 stringeeCall, final JSONObject callInfo) {
                Log.d("Stringee", "onCallInfo: " + callInfo.toString());
            }
        });
        audioManager.start(new StringeeAudioManager.AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(StringeeAudioManager.AudioDevice audioDevice, Set<StringeeAudioManager.AudioDevice> set) {

            }
        });
        stringeeCall.ringing(new StatusListener() {
            @Override
            public void onSuccess() {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_answer) {
            tvState.setText(R.string.starting);
            vSpeaker.setVisibility(View.VISIBLE);
            vAccept.setVisibility(View.GONE);
            if (mStringeeCall != null) {
                mStringeeCall.answer();
            }
        } else if (v.getId() == R.id.btn_reject) {
            tvState.setText(R.string.call_ended);
            if (mStringeeCall != null) {
                mStringeeCall.hangup();
                audioManager.stop();
            }
            finish();
        } else if (v.getId() == R.id.btn_end) {
            tvState.setText(R.string.call_ended);
            mStringeeCall.hangup();
            audioManager.stop();
            finish();
        } else if (v.getId() == R.id.btn_speaker) {
            isSpeaker = !isSpeaker;
            if (isSpeaker) {
                btnSpeaker.setImageResource(R.drawable.ic_speaker_on);
            } else {
                btnSpeaker.setImageResource(R.drawable.ic_speaker_off);
            }
            audioManager.setSpeakerphoneOn(isSpeaker);
        } else if (v.getId() == R.id.btn_mute) {
            isMute = !isMute;
            if (isMute) {
                btnMute.setImageResource(R.drawable.ic_mute);
            } else {
                btnMute.setImageResource(R.drawable.ic_mic);
            }
            if (mStringeeCall != null) {
                mStringeeCall.mute(isMute);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("name", "Luan");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mStringeeCall.sendCallInfo(jsonObject, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("Stringee", "Send call info");
                    }
                });
            }
        } else if (v.getId() == R.id.btn_video) {
            isVideoOn = !isVideoOn;
            if (isVideoOn) {
                btnVideo.setImageResource(R.drawable.ic_video);
            } else {
                btnVideo.setImageResource(R.drawable.ic_video_off);
            }
            if (mStringeeCall != null) {
                mStringeeCall.enableVideo(isVideoOn);
            }
        }
    }

    private void checkCallStats(StringeeCall2.StringeeCallStats stats) {
        double videoTimestamp = stats.timeStamp / 1000;

        //initialize values
        if (mPrevCallTimestamp == 0) {
            mPrevCallTimestamp = videoTimestamp;
            mPrevCallBytes = stats.callBytesReceived;
        } else {
            //calculate video bandwidth
            mCallBw = (long) ((8 * (stats.callBytesReceived - mPrevCallBytes)) / (videoTimestamp - mPrevCallTimestamp));
            mPrevCallTimestamp = videoTimestamp;
            mPrevCallBytes = stats.callBytesReceived;

            Log.d("Stringee", "Call bandwidth (bps): " + mCallBw);

            checkNetworkQuality();
        }
    }

    private void checkNetworkQuality() {
        if (mCallBw < 15000) {
            imNetwork.setImageResource(R.drawable.signal1);
        } else {
            if (mCallBw >= 35000) {
                imNetwork.setImageResource(R.drawable.signal4);
            } else {
                if (mCallBw >= 15000 && mCallBw <= 25000) {
                    imNetwork.setImageResource(R.drawable.signal2);
                } else if (mCallBw >= 25000 && mCallBw < 35000) {
                    imNetwork.setImageResource(R.drawable.signal3);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        boolean isGranted = false;
        if (grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                    break;
                } else {
                    isGranted = true;
                }
            }
        }
        switch (requestCode) {
            case REQUEST_PERMISSION_CALLIN:
                if (!isGranted) {
                    finish();
                } else {
                    startCall(mStringeeCall);
                }
                break;
        }
    }
}
