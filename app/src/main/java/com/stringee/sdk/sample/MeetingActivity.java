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
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.stringee.conference.StringeeRoom;
import com.stringee.conference.StringeeStream;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StringeeRoomListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by luannguyen on 3/17/2016.
 */
public class MeetingActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton btnMute;
    private FrameLayout localView;
    private FrameLayout remoteView1;
    //    private FrameLayout remoteView2;
    private TextView tvTitle;
    private TextView tvNotify;
    private ImageView imNetwork;

    private boolean isMute = false;
    private boolean isSpeakerOn = true;
    private String action = "make";
    private HashMap<String, FrameLayout> viewsMap = new HashMap<>();
    private Timer statsTimer;

    private StringeeRoom mRoom;
    private int mRoomId;
    private StringeeStream localStream;

    public static final int REQUEST_PERMISSION_MEETING = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.meeting);
        // Keep screen active
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            action = extras.getString("action");
            mRoomId = extras.getInt("roomId");
        }

        tvTitle = (TextView) findViewById(R.id.tv_title);

        ImageButton btnSwitch = (ImageButton) findViewById(R.id.btn_switch);
        btnSwitch.setOnClickListener(this);

        btnMute = (ImageButton) findViewById(R.id.btn_mute);
        btnMute.setOnClickListener(this);

        ImageButton btnEndCall = (ImageButton) findViewById(R.id.btn_end_call);
        btnEndCall.setOnClickListener(this);

        localView = (FrameLayout) findViewById(R.id.v_local);
        remoteView1 = (FrameLayout) findViewById(R.id.v_remote1);
//        remoteView2 = (FrameLayout) findViewById(R.id.v_remote2);

        tvNotify = (TextView) findViewById(R.id.tv_notify);
        imNetwork = (ImageView) findViewById(R.id.im_network);

        // Check permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> lstPermissions = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                lstPermissions.add(Manifest.permission.RECORD_AUDIO);
            }

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                lstPermissions.add(Manifest.permission.CAMERA);
            }

            if (lstPermissions.size() > 0) {
                String[] permissions = new String[lstPermissions.size()];
                for (int i = 0; i < lstPermissions.size(); i++) {
                    permissions[i] = lstPermissions.get(i);
                }
                ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_MEETING);
                return;
            }
        }
        startConference();
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
            case REQUEST_PERMISSION_MEETING:
                if (!isGranted) {
                    return;
                } else {
                    startConference();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_mute:
                isMute = !isMute;
                if (localStream != null) {
                    localStream.mute(isMute);
                }
                if (isMute) {
                    btnMute.setImageResource(R.drawable.ic_mute);
                } else {
                    btnMute.setImageResource(R.drawable.ic_mic);
                }

                break;
            case R.id.btn_switch:
                if (localStream != null) {
                    localStream.switchCamera();
                }
                break;
            case R.id.btn_end_call:
                if (statsTimer != null) {
                    statsTimer.cancel();
                    statsTimer = null;
                }
                if (mRoom != null) {
                    mRoom.leaveRoom();
                }
                finish();
                break;
        }
    }

    private void startConference() {
        if (action.equals("make")) {
            mRoom = new StringeeRoom(Common.client);
        } else {
            mRoom = new StringeeRoom(Common.client, mRoomId);
        }
        mRoom.setRoomListener(new StringeeRoomListener() {
            @Override
            public void onRoomConnected(final StringeeRoom stringeeRoom) {
                Log.e("Stringee", "================== Room connected");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvTitle.setText(String.valueOf(stringeeRoom.getId()));
                    }
                });
                localStream = new StringeeStream(MeetingActivity.this);
                localStream.setStreamListener(new StringeeStream.StringeeStreamListener() {
                    @Override
                    public void onStreamMediaAvailable(final StringeeStream stream) {
                        Log.e("Stringee", "============ on local stream media available");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                localView.addView(stream.getView());
                                stream.renderView(true);
                            }
                        });
                    }
                });
                mRoom.publish(localStream);
            }

            @Override
            public void onRoomDisconnected(StringeeRoom stringeeRoom) {
                Log.e("Stringee", "================== Room disconnected " + stringeeRoom.getId());
            }

            @Override
            public void onRoomError(StringeeRoom stringeeRoom, StringeeError error) {
                Log.e("Stringee", "================== onRoomError");
            }

            @Override
            public void onStreamAdded(StringeeStream stream) {
                stream.setStreamListener(new StringeeStream.StringeeStreamListener() {
                    @Override
                    public void onStreamMediaAvailable(final StringeeStream stream) {
                        Log.e("Stringee", "============ on remote stream media available");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                remoteView1.removeAllViews();
                                remoteView1.addView(stream.getView());
                                stream.renderView(false);
                            }
                        });
                    }
                });
                mRoom.subscribe(stream);
            }

            @Override
            public void onStreamRemoved(final StringeeStream stream) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        remoteView1.removeAllViews();
                        if (statsTimer != null) {
                            statsTimer.cancel();
                            statsTimer = null;
                        }

                        mPrevAudioBytes = 0;
                        mPrevAudioTimestamp = 0;
                    }
                });
            }

            @Override
            public void onStreamPublished(StringeeStream stream, boolean isReconnecting) {

            }

            @Override
            public void onStreamPublishError(StringeeStream stream, StringeeError error, boolean isReconnecting) {

            }

            @Override
            public void onStreamUnPublished(final StringeeStream stream) {
            }

            @Override
            public void onStreamUnPublishError(StringeeStream stream, StringeeError error) {

            }

            @Override
            public void onStreamSubscribed(final StringeeStream stream, boolean isReconnecting) {
                if (statsTimer != null) {
                    statsTimer.cancel();
                }
                statsTimer = new Timer();
                TimerTask statsTimerTask = new TimerTask() {

                    @Override
                    public void run() {
                        stream.getStats(new StringeeStream.StringeeStreamStatsListener() {
                            @Override
                            public void onCallStats(final StringeeStream.StringeeStreamStats statsReport) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        checkStreamStats(statsReport);
                                    }
                                });
                            }
                        });
                    }
                };
                statsTimer.schedule(statsTimerTask, 0, 3000);
            }

            @Override
            public void onStreamUnSubscribed(StringeeStream stream) {

            }

            @Override
            public void onStreamSubscribeError(StringeeStream stream, StringeeError error, boolean isReconnecting) {

            }

            @Override
            public void onStreamUnSubscribeError(StringeeStream stream, StringeeError error) {

            }
        });

        if (action.equals("make")) {
            mRoom.makeRoom();
        } else {
            mRoom.joinRoom();
        }
    }

    private double mPrevAudioTimestamp = 0;
    private long mPrevAudioBytes = 0;

    private long mAudioBw = 0;

    private void checkStreamStats(StringeeStream.StringeeStreamStats stats) {
        double audioTimestamp = stats.timeStamp / 1000;

        //initialize values
        if (mPrevAudioTimestamp == 0) {
            mPrevAudioTimestamp = audioTimestamp;
            mPrevAudioBytes = stats.bytesReceived;
        } else {
            //calculate  bandwidth
            if (stats.bytesReceived == 0) {
                mAudioBw = 0;
            } else {
                mAudioBw = (long) ((8 * (stats.bytesReceived - mPrevAudioBytes)) / (audioTimestamp - mPrevAudioTimestamp));
            }
            mPrevAudioTimestamp = audioTimestamp;
            mPrevAudioBytes = stats.bytesReceived;

            Log.e("Stringee", "Audio bandwidth (bps): " + mAudioBw);
            checkNetworkQuality();
        }
    }

    private void checkNetworkQuality() {
        if (mAudioBw < 15000) {
            imNetwork.setImageResource(R.drawable.signal1);
        } else {
            if (mAudioBw >= 35000) {
                imNetwork.setImageResource(R.drawable.signal4);
            } else {
                if (mAudioBw < 25000) {
                    imNetwork.setImageResource(R.drawable.signal2);
                } else {
                    imNetwork.setImageResource(R.drawable.signal3);
                }
            }
        }
    }
}
