package com.planx.facecapture;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.planx.anichat.MyApplication;
import com.planx.anichat.R;

import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;


public class CameraActivity extends AppCompatActivity {

    private String TAG = "CameraActivity";
    private int CAMERA_REQUEST_CODE = 20;
    private int modelTag;

    private CameraPreview mCameraPreview;
    public Live2dGLSurfaceView mGLSurfaceView;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Log.d(TAG, "activity created.");

        if(Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //申请权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        CAMERA_REQUEST_CODE);
            }
        }
        mCameraPreview = (CameraPreview) findViewById(R.id.cam_preview);
        mCameraPreview.init(CameraActivity.this);


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
                        modelTag= Integer.parseInt(vCard.getEmailHome());//占用此属性来存放用户使用的模型
                        vCardManager.saveVCard(vCard);
                        Message msg = new Message();
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                    } catch (Exception e) {
                        e.printStackTrace();//
                    }
                }
            }
        });


    }
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:

                    RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
                    mGLSurfaceView = new Live2dGLSurfaceView(CameraActivity.this);
                    mGLSurfaceView.init(CameraActivity.this, modelTag, 1, 1);
                    container.addView(mGLSurfaceView);
                    break;
            }

        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // ignore orientation/keyboard change
        super.onConfigurationChanged(newConfig);
    }

}