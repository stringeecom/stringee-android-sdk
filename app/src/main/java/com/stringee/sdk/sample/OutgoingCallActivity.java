package com.stringee.sdk.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.common.StringeeAudioManager;
import com.stringee.video.StringeeVideoTrack;

import org.json.JSONObject;
import org.webrtc.RendererCommon;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class OutgoingCallActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvName;
    private TextView tvState;
    private ImageButton btnMute;
    private ImageButton btnSpeaker;
    private ImageButton btnVideo;
    private ImageView imNetwork;
    private FrameLayout vLocal;
    private FrameLayout vRemote;

    private boolean isMute = false;
    private boolean isSpeaker = false;
    private String phoneNumber;
    private StringeeCall2 mStringeeCall;
    private boolean isVideoCall;
    private boolean isVideoOn;
    private boolean isCallout;
    private StringeeCall stringeeCall;

    private double mPrevCallTimestamp = 0;
    private long mPrevCallBytes = 0;
    private long mCallBw = 0;

    public static final int REQUEST_PERMISSION_CALLOUT = 1;

    private StringeeCall2.MediaState mMediaState;
    private StringeeCall2.SignalingState mState;
    private StringeeCall.MediaState mediaState;
    private StringeeCall.SignalingState state;

    private StringeeAudioManager audioManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_call);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            phoneNumber = extras.getString("phone");
            isVideoCall = extras.getBoolean("video");
            isCallout = extras.getBoolean("is_callout");
        }

        isVideoOn = isVideoCall;

        tvName = (TextView) findViewById(R.id.tv_name);
        tvState = (TextView) findViewById(R.id.tv_state);
        btnMute = (ImageButton) findViewById(R.id.btn_mute);
        btnMute.setOnClickListener(this);
        btnSpeaker = (ImageButton) findViewById(R.id.btn_speaker);
        btnSpeaker.setOnClickListener(this);
        btnVideo = (ImageButton) findViewById(R.id.btn_video);
        btnVideo.setOnClickListener(this);
        if (isVideoCall) {
            btnSpeaker.setImageResource(R.drawable.ic_speaker_on);
            btnVideo.setImageResource(R.drawable.ic_video);
        } else {
            btnSpeaker.setImageResource(R.drawable.ic_speaker_off);
            btnVideo.setImageResource(R.drawable.ic_video_off);
        }
        ImageButton btnEndcall = (ImageButton) findViewById(R.id.btn_end_call);
        btnEndcall.setOnClickListener(this);
        imNetwork = (ImageView) findViewById(R.id.im_network);
        vLocal = (FrameLayout) findViewById(R.id.v_local);
        vRemote = (FrameLayout) findViewById(R.id.v_remote);

        if (phoneNumber != null) {
            tvName.setText(phoneNumber);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> lstPermissions = new ArrayList<>();

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                lstPermissions.add(Manifest.permission.RECORD_AUDIO);
            }

            if (isVideoCall) {
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
                ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CALLOUT);
                return;
            }
        }
        if (isVideoCall) {
            startCall2();
        } else {
            startCall();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("phone", phoneNumber);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
            case REQUEST_PERMISSION_CALLOUT:
                if (!isGranted) {
                    finish();
                } else {
                    if (isVideoCall) {
                        startCall2();
                    } else {
                        startCall();
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_mute) {
            isMute = !isMute;
            if (isMute) {
                btnMute.setImageResource(R.drawable.ic_mute);
            } else {
                btnMute.setImageResource(R.drawable.ic_mic);
            }
            if (mStringeeCall != null) {
                mStringeeCall.mute(isMute);
            } else if (stringeeCall != null) {
                stringeeCall.mute(isMute);
            }
        } else if (v.getId() == R.id.btn_speaker) {
            isSpeaker = !isSpeaker;
            if (isSpeaker) {
                btnSpeaker.setImageResource(R.drawable.ic_speaker_on);
            } else {
                btnSpeaker.setImageResource(R.drawable.ic_speaker_off);
            }
            audioManager.setSpeakerphoneOn(isSpeaker);
        } else if (v.getId() == R.id.btn_end_call) {
            if (mStringeeCall != null) {
                mStringeeCall.hangup(null);
            } else if (stringeeCall != null) {
                stringeeCall.hangup(null);
            }
            audioManager.stop();

            finish();
        } else if (v.getId() == R.id.btn_video) {
            isVideoOn = !isVideoOn;
            if (isVideoOn) {
                btnVideo.setImageResource(R.drawable.ic_video);
            } else {
                btnVideo.setImageResource(R.drawable.ic_video_off);
            }
            if (mStringeeCall != null) {
                mStringeeCall.enableVideo(isVideoOn);
            } else if (stringeeCall != null) {
                stringeeCall.enableVideo(isVideoOn);
            }
        }
    }

    private void startCall() {
        String from = Common.client.getUserId();
        if (isCallout) {
            from = "842873030095";
        }
        stringeeCall = new StringeeCall(Common.client, from, phoneNumber);
        stringeeCall.setCallListener(new StringeeCall.StringeeCallListener() {
            @Override
            public void onSignalingStateChange(final StringeeCall call, final StringeeCall.SignalingState signalingState, String reason, int sipCode, String sipReason) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Stringee", "======== state: " + signalingState);
                        state = signalingState;
                        if (state == StringeeCall.SignalingState.ANSWERED && mediaState == StringeeCall.MediaState.CONNECTED) {
                            tvState.setText("Started");
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    call.getStats(new StringeeCall.CallStatsListener() {
                                        @Override
                                        public void onCallStats(StringeeCall.StringeeCallStats statsReport) {
                                            checkCallStats(statsReport);
                                        }
                                    });
                                }
                            }, 0, 2000);
                        }

                        if (signalingState == StringeeCall.SignalingState.ENDED || signalingState == StringeeCall.SignalingState.BUSY) {
                            if (stringeeCall != null && stringeeCall.getCallId().equals(call.getCallId())) {
                                tvState.setText(R.string.call_ended);
                                stringeeCall.hangup(null);
                                audioManager.stop();
                                finish();
                            }
                        }
                    }
                });
            }

            @Override
            public void onError(StringeeCall stringeeCall, int code, String description) {
                Log.d("Stringee", description);
            }

            @Override
            public void onHandledOnAnotherDevice(StringeeCall stringeeCall, StringeeCall.SignalingState signalingState, String description) {

            }

            @Override
            public void onMediaStateChange(final StringeeCall stringeeCall, final StringeeCall.MediaState mState) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mediaState = mState;
                        if (mediaState == StringeeCall.MediaState.CONNECTED && state == StringeeCall.SignalingState.ANSWERED) {

                            tvState.setText("Started");
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    stringeeCall.getStats(new StringeeCall.CallStatsListener() {
                                        @Override
                                        public void onCallStats(StringeeCall.StringeeCallStats statsReport) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    checkCallStats(statsReport);
                                                }
                                            });
                                        }
                                    });
                                }
                            }, 0, 2000);
                        }
                    }
                });
            }

            @Override
            public void onLocalStream(final StringeeCall stringeeCall) {
                Log.e("Stringee", "========= onLocalStream");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (stringeeCall.isVideoCall()) {
                            vLocal.removeAllViews();
                            vLocal.addView(stringeeCall.getLocalView());
                            stringeeCall.renderLocalView(true, RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                        }
                    }
                });
            }

            @Override
            public void onRemoteStream(final StringeeCall stringeeCall) {
                Log.e("Stringee", "========= onRemoteStream");
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
            public void onCallInfo(StringeeCall stringeeCall, final JSONObject callInfo) {
                Log.d("Stringee", "onCallInfo: " + callInfo.toString());
            }
        });
        audioManager = StringeeAudioManager.create(this);
        audioManager.start(new StringeeAudioManager.AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(StringeeAudioManager.AudioDevice audioDevice, Set<StringeeAudioManager.AudioDevice> set) {

            }
        });
        stringeeCall.makeCall(null);
    }

    private void startCall2() {
        mStringeeCall = new StringeeCall2(Common.client, Common.client.getUserId(), phoneNumber);
        mStringeeCall.setVideoCall(isVideoCall);
        mStringeeCall.setCallListener(new StringeeCall2.StringeeCallListener() {
            @Override
            public void onSignalingStateChange(final StringeeCall2 call, final StringeeCall2.SignalingState signalingState, String reason, int sipCode, String sipReason) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Stringee", "======== state: " + signalingState);
                        mState = signalingState;
                        if (mState == StringeeCall2.SignalingState.ANSWERED && mMediaState == StringeeCall2.MediaState.CONNECTED) {
                            tvState.setText("Started");
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    call.getStats(new StringeeCall2.CallStatsListener() {
                                        @Override
                                        public void onCallStats(StringeeCall2.StringeeCallStats statsReport) {
                                            checkCallStats2(statsReport);
                                        }
                                    });
                                }
                            }, 0, 2000);
                        }

                        if (signalingState == StringeeCall2.SignalingState.ENDED || signalingState == StringeeCall2.SignalingState.BUSY) {
                            if (mStringeeCall != null && mStringeeCall.getCallId().equals(call.getCallId())) {
                                tvState.setText(R.string.call_ended);
                                mStringeeCall.hangup(null);
                                audioManager.stop();
                                finish();
                            }
                        }
                    }
                });
            }

            @Override
            public void onError(StringeeCall2 stringeeCall, int code, String description) {

            }

            @Override
            public void onHandledOnAnotherDevice(StringeeCall2 stringeeCall, StringeeCall2.SignalingState signalingState, String description) {

            }

            @Override
            public void onMediaStateChange(final StringeeCall2 stringeeCall, final StringeeCall2.MediaState mediaState) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMediaState = mediaState;
                        if (mMediaState == StringeeCall2.MediaState.CONNECTED && mState == StringeeCall2.SignalingState.ANSWERED) {

                            tvState.setText("Started");
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    stringeeCall.getStats(new StringeeCall2.CallStatsListener() {
                                        @Override
                                        public void onCallStats(StringeeCall2.StringeeCallStats statsReport) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    checkCallStats2(statsReport);
                                                }
                                            });
                                        }
                                    });
                                }
                            }, 0, 2000);
                        }
                    }
                });
            }

            @Override
            public void onLocalStream(final StringeeCall2 stringeeCall) {
                Log.e("Stringee", "========= onLocalStream");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (stringeeCall.isVideoCall()) {
                            vLocal.removeAllViews();
                            vLocal.addView(stringeeCall.getLocalView());
                            stringeeCall.renderLocalView(true, RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                        }
                    }
                });
            }

            @Override
            public void onRemoteStream(final StringeeCall2 stringeeCall) {
                Log.e("Stringee", "========= onRemoteStream");
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
            public void onVideoTrackAdded(StringeeVideoTrack stringeeVideoTrack) {

            }

            @Override
            public void onVideoTrackRemoved(StringeeVideoTrack stringeeVideoTrack) {

            }

            @Override
            public void onCallInfo(StringeeCall2 stringeeCall, final JSONObject callInfo) {
                Log.d("Stringee", "onCallInfo: " + callInfo.toString());
            }

            @Override
            public void onTrackMediaStateChange(String s, StringeeVideoTrack.MediaType mediaType, boolean b) {

            }
        });
        audioManager = StringeeAudioManager.create(this);
        audioManager.start(new StringeeAudioManager.AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(StringeeAudioManager.AudioDevice audioDevice, Set<StringeeAudioManager.AudioDevice> set) {

            }
        });
        mStringeeCall.makeCall(null);
    }

    private void checkCallStats(StringeeCall.StringeeCallStats stats) {
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

    private void checkCallStats2(StringeeCall2.StringeeCallStats stats) {
        double videoTimestamp = stats.timeStamp / 1000;

        //initialize values
        if (mPrevCallTimestamp == 0) {
            mPrevCallTimestamp = videoTimestamp;
            mPrevCallBytes = stats.videoBytesReceived;
        } else {
            //calculate video bandwidth
            mCallBw = (long) ((8 * (stats.videoBytesReceived - mPrevCallBytes)) / (videoTimestamp - mPrevCallTimestamp));
            mPrevCallTimestamp = videoTimestamp;
            mPrevCallBytes = stats.videoBytesReceived;

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
}
