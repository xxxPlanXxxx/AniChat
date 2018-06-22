/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.planx.anichat.activity.video;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.widget.*;

import java.util.Locale;

import io.agora.AgoraAPI;
import io.agora.AgoraAPIOnlySignal;
import io.agora.IAgoraAPI;

import com.planx.anichat.R;
import com.planx.anichat.MyApplication;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.mediaio.IVideoSource;


import com.planx.anichat.utils.MyUtils;
import com.planx.anichat.view.CameraPreview;
import com.planx.anichat.view.Live2dGLSurfaceView;

import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import javax.microedition.khronos.egl.EGLContext;

public class CallActivity extends AppCompatActivity implements MyApplication.OnAgoraEngineInterface {
    private final String TAG = CallActivity.class.getSimpleName();

    private int tagMe;
    private int tagNotMe=2;
    private String accountNotMe;

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
    private static final int PERMISSION_REQ_ID_STORAGE = PERMISSION_REQ_ID_CAMERA + 1;

    private AgoraAPIOnlySignal mAgoraAPI;
    private RtcEngine mRtcEngine;

    private String mSubscriber;

    private CheckBox mCheckMute;
    private TextView mCallTitle;
    private ImageView mCallHangupBtn;
    private RelativeLayout mLayoutCallIn;

    private RelativeLayout mLayoutBigView;
    private RelativeLayout mLayoutSmallView;

    private String channelName = "channelid";
    private MediaPlayer mPlayer;
    private int callType = -1;
    private boolean mIsCallInRefuse = false;
    private int mRemoteUid = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        InitUI();

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)
                && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_STORAGE)) {
            initAgoraEngineAndJoinChannel();
        }


    }

    private void InitUI() {

        mCallTitle = (TextView) findViewById(R.id.meet_title);

        mCheckMute = (CheckBox) findViewById(R.id.call_mute_button);
        mCheckMute.setOnCheckedChangeListener(oncheckChangeListerener);

        mCallHangupBtn = (ImageView) findViewById(R.id.call_button_hangup);
        mLayoutCallIn = (RelativeLayout) findViewById(R.id.call_layout_callin);

        mLayoutBigView = (RelativeLayout) findViewById(R.id.remote_video_view_container);
        mLayoutSmallView = (RelativeLayout) findViewById(R.id.local_video_view_container);
    }

    private void setupData() {
        Intent intent = getIntent();

        mSubscriber = intent.getStringExtra("subscriber");
        channelName = intent.getStringExtra("channelName");
        accountNotMe = intent.getStringExtra("account");
        callType = intent.getIntExtra("type", -1);
        if (callType == MyUtils.CALL_IN) {
            mIsCallInRefuse = true;
            mLayoutCallIn.setVisibility(View.VISIBLE);
            mCallHangupBtn.setVisibility(View.GONE);
            mCallTitle.setText(String.format(Locale.US, "%s is calling...", mSubscriber));

            try {
                //电话铃声响起
                mPlayer = MediaPlayer.create(this, R.raw.basic_ring);
                mPlayer.setLooping(true);
                mPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            setupLocalVideo(); // Tutorial Step 3
        } else if (callType == MyUtils.CALL_OUT) {
            mLayoutCallIn.setVisibility(View.GONE);
            mCallHangupBtn.setVisibility(View.VISIBLE);
            mCallTitle.setText(String.format(Locale.US, "%s is be called...", mSubscriber));

            try {
                mPlayer = MediaPlayer.create(this, R.raw.basic_tones);
                mPlayer.setLooping(true);
                mPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            setupLocalVideo(); // Tutorial Step 3
            joinChannel(); // Tutorial Step 4
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "onNewIntent");
        setupData();
    }

    @Override
    public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) { // Tutorial Step 5
        Log.i(TAG, "onFirstRemoteVideoDecoded  uid:" + uid);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRemoteUid != 0) {
                    return;
                }
                mRemoteUid = uid;
                setupRemoteVideo(uid);
            }
        });
    }

    @Override
    public void onUserOffline(final int uid, int reason) { // Tutorial Step 7
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onRemoteUserLeft(uid);
            }
        });
    }

    @Override
    public void onUserMuteVideo(final int uid, final boolean muted) { // Tutorial Step 10
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onRemoteUserVideoMuted(uid, muted);
            }
        });
    }

    private CompoundButton.OnCheckedChangeListener oncheckChangeListerener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mRtcEngine.muteLocalAudioStream(isChecked);
        }
    };

    public void CallClickInit(View v) {
        switch (v.getId()) {

            case R.id.call_in_hangup:
                callInRefuse();
                break;

            case R.id.call_in_pickup:
                mIsCallInRefuse = false;
                joinChannel(); // Tutorial Step 4
                mAgoraAPI.channelInviteAccept(channelName, mSubscriber, 0, null);
                mLayoutCallIn.setVisibility(View.GONE);
                mCallHangupBtn.setVisibility(View.VISIBLE);
                mCallTitle.setVisibility(View.GONE);
                if (mPlayer != null && mPlayer.isPlaying()) {
                    mPlayer.stop();
                }
                setupRemoteVideo(mRemoteUid);
                break;

            case R.id.call_button_hangup: // call out canceled or call ended

                callOutHangup();
                break;
        }
    }

    private void callOutHangup() {
        if (mAgoraAPI != null)
            mAgoraAPI.channelInviteEnd(channelName, mSubscriber, 0);
    }

    private void callInRefuse() {
        // "status": 0 // Default
        // "status": 1 // Busy
        if (mAgoraAPI != null)
            mAgoraAPI.channelInviteRefuse(channelName, mSubscriber, 0, "{\"status\":0}");

        onEncCallClicked();
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        Log.i(TAG, "onJoinChannelSuccess channel: " + channel + " uid: " + uid);
    }

    private void addSignalingCallback() {
        if (mAgoraAPI == null) {
            return;
        }

        mAgoraAPI.callbackSet(new AgoraAPI.CallBack() {


            @Override
            public void onMessageInstantReceive(String account, int uid, final String msg) {
                Log.i("onMessageInstantReceive",account+": "+msg);
                final String message = msg;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String[] reciveEmotions = message.split("@");
                        int i = 0;
                        for (String reciveEmotion :reciveEmotions){
                            double rEmotion = Double.valueOf(reciveEmotion);
                            MyApplication.emotionH[i]=rEmotion;
                            i++;
                        }
                        String mm=MyApplication.emotionH[0]+" "+MyApplication.emotionH[1]+" "+MyApplication.emotionH[2]+" "+MyApplication.emotionH[3];
                        Log.i(TAG,mm);
                    }
                });

            }

            @Override
            public void onLogout(final int i) {
                Log.i(TAG, "onLogout  i = " + i);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (i == IAgoraAPI.ECODE_LOGOUT_E_KICKED) { // other login the account
                            Toast.makeText(CallActivity.this, "Other login account ,you are logout.", Toast.LENGTH_SHORT).show();

                        } else if (i == IAgoraAPI.ECODE_LOGOUT_E_NET) { // net
                            Toast.makeText(CallActivity.this, "Logout for Network can not be.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        Intent intent = new Intent();
                        intent.putExtra("result", "finish");
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });

            }

            /**
             * call in receiver
             */
            @Override
            public void onInviteReceived(final String channelID, final String account, final int uid, String s2) {
                Log.i(TAG, "CallActicity.onInviteReceived  channelID = " + channelID + "  account = " + account);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                      "status": 0 // Default
//                      "status": 1 // Busy
                        mAgoraAPI.channelInviteRefuse(channelID, account, uid, "{\"status\":1}");

                    }
                });
            }

            /**
             * call out other ,local receiver
             */
            @Override
            public void onInviteReceivedByPeer(final String channelID, String account, int uid) {
                Log.i(TAG, "onInviteReceivedByPeer  channelID = " + channelID + "  account = " + account);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCallHangupBtn.setVisibility(View.VISIBLE);

                        mCallTitle.setText(String.format(Locale.US, "%s is being called ...", mSubscriber));
                    }
                });
            }

            /**
             * other receiver call accept callback
             * @param channelID
             * @param account
             * @param uid
             * @param s2
             */
            @Override
            public void onInviteAcceptedByPeer(String channelID, String account, final int uid, String s2) {
                accountNotMe = account;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mPlayer != null && mPlayer.isPlaying()) {
                            mPlayer.stop();
                        }
                        mCallTitle.setVisibility(View.GONE);
                        setupRemoteVideo(uid);
                    }
                });

            }

            /**
             * other receiver call refuse callback
             * @param channelID
             * @param account
             * @param uid
             * @param s2
             */

            @Override
            public void onInviteRefusedByPeer(String channelID, final String account, int uid, final String s2) {
                Log.i(TAG, "onInviteRefusedByPeer channelID = " + channelID + " account = " + account + " s2 = " + s2);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mPlayer != null && mPlayer.isPlaying()) {
                            mPlayer.stop();
                        }
                        if (s2.contains("status") && s2.contains("1")) {
                            Toast.makeText(CallActivity.this, account + " reject your call for busy", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CallActivity.this, account + " reject your call", Toast.LENGTH_SHORT).show();
                        }

                        onEncCallClicked();
                    }
                });
            }


            /**
             * end call remote receiver callback
             * @param channelID
             * @param account
             * @param uid
             * @param s2
             */
            @Override
            public void onInviteEndByPeer(final String channelID, String account, int uid, String s2) {
                Log.i(TAG, "onInviteEndByPeer channelID = " + channelID + " account = " + account);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (channelID.equals(channelName)) {
                            onEncCallClicked();
                        }

                    }
                });
            }

            /**
             * end call local receiver callback
             * @param channelID
             * @param account
             * @param uid
             */
            @Override
            public void onInviteEndByMyself(String channelID, String account, int uid) {
                Log.i(TAG, "onInviteEndByMyself channelID = " + channelID + "  account = " + account);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onEncCallClicked();
                    }
                });
            }
        });
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        addSignalingCallback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy");
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
        }


        if (mRtcEngine != null) {
            mRtcEngine.stopPreview();
            mRtcEngine.leaveChannel();
        }
        mRtcEngine = null;

    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed callType: " + callType + " mIsCallInRefuse: " + mIsCallInRefuse);
        if (callType == MyUtils.CALL_IN && mIsCallInRefuse) {
            callInRefuse();
        } else {
            callOutHangup();
        }
        super.onBackPressed();
    }

    // Tutorial Step 8
    public void onSwitchCameraClicked(View view) {
        mRtcEngine.switchCamera();
    }

    // Tutorial Step 6
    public void onEncCallClicked() {
        finish();
    }

    // Tutorial Step 1
    private void initializeAgoraEngine() {
        mAgoraAPI = MyApplication.the().getmAgoraAPI();
        mRtcEngine = MyApplication.the().getmRtcEngine();
        Log.i(TAG, "initializeAgoraEngine mRtcEngine :" + mRtcEngine);
        if (mRtcEngine != null) {
            mRtcEngine.setLogFile("/sdcard/sdklog.txt");
        }
        setupVideoProfile();

    }

    // Tutorial Step 2
    private void setupVideoProfile() {
        mRtcEngine.enableVideo();
        mRtcEngine.setExternalVideoSource(true, true, true);
        mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_480P, true);
    }

    // Tutorial Step 3
    private void setupLocalVideo() {
        Log.d("setupLocalVideo " ,"+++++++++++++");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!MyApplication.getConnection().isConnected())
                        MyApplication.getConnection().connect();
                } catch (Exception e) {
                    e.printStackTrace();
                    MyApplication.getConnection().disconnect();
                }
                if (MyApplication.getConnection().isConnected()) {
                    try {
                        VCardManager vCardManager = VCardManager.getInstanceFor(MyApplication.getConnection());
                        VCard vCard = vCardManager.loadVCard();
                        tagMe= Integer.parseInt(vCard.getEmailHome());//占用此属性来存放用户使用的模型
                        Message msg = new Message();
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                    } catch (Exception e) {
                        e.printStackTrace();//
                    }
                }
            }
        });
//        int ret = mRtcEngine.startPreview();
//        Log.i(TAG, "setupLocalVideo startPreview enter << ret :" + ret);
    }

    // Tutorial Step 4
    private void joinChannel() {
        int ret = mRtcEngine.joinChannel(null, channelName, "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
        Log.i(TAG, "joinChannel enter ret :" + ret);
    }

    // Tutorial Step 5
    private void setupRemoteVideo(int uid) {
        Log.i(TAG, "setupRemoteVideo uid: " + uid + " " + mLayoutBigView.getChildCount());
        if (mLayoutBigView.getChildCount() >= 1) {
            mLayoutBigView.removeAllViews();
        }
        if (mLayoutSmallView.getChildCount() >= 1) {
            mLayoutSmallView.removeAllViews();
        }
        CameraPreview cameraPreview = new CameraPreview(this);
        cameraPreview.init(this);
//        Live2dGLSurfaceView mGLSurfaceView = new Live2dGLSurfaceView(CallActivity.this);
//        mGLSurfaceView.init(true,CallActivity.this, MODEL_PATH, TEXTURE_PATHS, 1, 1);
//        mGLSurfaceView.setOnFrameAvailableHandler(new Live2dGLSurfaceView.OnFrameAvailableListener(){
//
//            @Override
//            public void onFrameAvailable(int texture, EGLContext eglContext, int rotation) {
//                AgoraVideoFrame vf = new AgoraVideoFrame();
//                vf.format = AgoraVideoFrame.FORMAT_TEXTURE_2D;
//                vf.timeStamp = System.currentTimeMillis();
//                vf.stride = 1080;
//                vf.height = 1920;
//                vf.textureID = texture;
//                vf.syncMode = true;
//                vf.eglContext11 = eglContext;
//                vf.transform = new float[]{
//                        1.0f, 0.0f, 0.0f, 0.0f,
//                        0.0f, 1.0f, 0.0f, 0.0f,
//                        0.0f, 0.0f, 1.0f, 0.0f,
//                        0.0f, 0.0f, 0.0f, 1.0f
//                };
//
//                boolean result = mRtcEngine.pushExternalVideoFrame(vf);
//                Log.d("onFrameAvailable " , eglContext + " " + rotation + " " + texture + " " + result);
//            }
//        });

//        mGLSurfaceView.setOnEGLContextHandler(new Live2dGLSurfaceView.OnEGLContextListener() {
//            @Override
//            public void onEGLContextReady(EGLContext eglContext) {
//
//            }
//        });
//        SurfaceView surfaceViewSmall = RtcEngine.CreateRendererView(getBaseContext());
//        surfaceViewSmall.setZOrderMediaOverlay(true);
        mLayoutSmallView.addView(cameraPreview);
//        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceViewSmall));
        mLayoutSmallView.setVisibility(View.VISIBLE);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!MyApplication.getConnection().isConnected())
                        MyApplication.getConnection().connect();
                } catch (Exception e) {
                    e.printStackTrace();
                    MyApplication.getConnection().disconnect();
                }
                if (MyApplication.getConnection().isConnected()) {
                    try {
                        VCardManager vCardManager = VCardManager.getInstanceFor(MyApplication.getConnection());
                        Log.i("tagNotMe", accountNotMe+"@"+getString(R.string.xmpp_domain));
                        VCard vCard = vCardManager.loadVCard(accountNotMe+"@"+getString(R.string.xmpp_domain));
                        tagNotMe= Integer.parseInt(vCard.getEmailHome());//占用此属性来存放用户使用的模型
                        Message msg = new Message();
                        msg.what = 2;
                        mHandler.sendMessage(msg);
                    } catch (Exception e) {
                        e.printStackTrace();//
                    }
                }
            }
        });


    }


    // Tutorial Step 7
    private void onRemoteUserLeft(int uid) {
        if (uid == mRemoteUid) {
            finish();
        }
    }

    // Tutorial Step 10
    private void onRemoteUserVideoMuted(int uid, boolean muted) {
        RelativeLayout container = (RelativeLayout) findViewById(R.id.remote_video_view_container);

        SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);

        Object tag = surfaceView.getTag();
        if (tag != null && (Integer) tag == uid) {
            surfaceView.setVisibility(muted ? View.GONE : View.VISIBLE);
        }
    }


    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                    onEncCallClicked();
                }
                break;
            }
            case PERMISSION_REQ_ID_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_STORAGE);
                } else {
                    showLongToast("No permission for " + Manifest.permission.CAMERA);
                    onEncCallClicked();
                }
                break;
            }
            case PERMISSION_REQ_ID_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel();
                } else {
                    showLongToast("No permission for " + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    onEncCallClicked();
                }
                break;
            }
        }
    }

    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();
        MyApplication.the().setOnAgoraEngineInterface(this);
        setupData();
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    CameraPreview cameraPreview = new CameraPreview(CallActivity.this);
                    cameraPreview.init(CallActivity.this);
                    Live2dGLSurfaceView mGLSurfaceView = new Live2dGLSurfaceView(CallActivity.this);
                    mGLSurfaceView.init(true,CallActivity.this,tagMe ,1, 1);
//        mGLSurfaceView.setOnFrameAvailableHandler(new Live2dGLSurfaceView.OnFrameAvailableListener(){
//
//            @Override
//                public void onFrameAvailable(int texture, EGLContext eglContext, int rotation) {
//                Log.d("onFrameAvailable " ,"+++++++++++++");
//                AgoraVideoFrame vf = new AgoraVideoFrame();
//                vf.format = AgoraVideoFrame.FORMAT_TEXTURE_2D;
//                vf.timeStamp = System.currentTimeMillis();
//                vf.stride = 1080;
//                vf.height = 1920;
//                vf.textureID = texture;
//                vf.syncMode = true;
//                vf.eglContext11 = eglContext;
//                vf.transform = new float[]{
//                        1.0f, 0.0f, 0.0f, 0.0f,
//                        0.0f, 1.0f, 0.0f, 0.0f,
//                        0.0f, 0.0f, 1.0f, 0.0f,
//                        0.0f, 0.0f, 0.0f, 1.0f
//                };
//
//                boolean result = mRtcEngine.pushExternalVideoFrame(vf);
//                Log.d("onFrameAvailable " , eglContext + " " + rotation + " " + texture + " " + result);
//            }
//        });
//        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
                    mLayoutBigView.addView(mGLSurfaceView);
                    mLayoutSmallView.addView(cameraPreview);
//        mRtcEngine.setupLocalVideo(new VideoCanvas(mGLSurfaceView));
                    mLayoutBigView.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    Log.i("tagNotMe", String.valueOf(tagNotMe));
                    Live2dGLSurfaceView mGLSurfaceView1 = new Live2dGLSurfaceView(CallActivity.this);
                    mGLSurfaceView1.init(false,CallActivity.this,tagNotMe ,1, 1);
                    mLayoutBigView.addView(mGLSurfaceView1);
//        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
                    mLayoutBigView.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

}
