package com.planx.anichat;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.planx.anichat.thread.NetThread;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.util.ArrayList;

import io.agora.AgoraAPIOnlySignal;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

public class MyApplication extends Application {

//    public static String MODEL_PATH = "live2d/miku/miku.moc";
//    public static String[] TEXTURE_PATHS = {
//            "live2d/miku/miku.2048/texture_00.png"
//    };

    public static String[] getTexturePaths(int tag){
        String[] TEXTURE_PATHS ;
        switch (tag) {
            case 0:
                TEXTURE_PATHS = new String[]{"live2d/eplison/Epsilon_free.2048/texture_00.png"};
                return TEXTURE_PATHS;
//                Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
            case 1:
                TEXTURE_PATHS = new String[]{
                        "live2d/haru/haru.1024/texture_00.png",
                        "live2d/haru/haru.1024/texture_01.png",
                        "live2d/haru/haru.1024/texture_02.png"
                };

                return TEXTURE_PATHS;
//                Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
            case 2:
                TEXTURE_PATHS = new String[]{
                        "live2d/hibiki/hibiki.2048/texture_00.png"
                };
                return TEXTURE_PATHS;
//                Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
            case 3:
                TEXTURE_PATHS = new String[]{
                        "live2d/miku/miku.2048/texture_00.png"
                };
                return TEXTURE_PATHS;
//                Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
            case 4:
                TEXTURE_PATHS = new String[]{
                        "live2d/shizuku/shizuku.1024/texture_00.png",
                        "live2d/shizuku/shizuku.1024/texture_01.png",
                        "live2d/shizuku/shizuku.1024/texture_02.png",
                        "live2d/shizuku/shizuku.1024/texture_03.png",
                        "live2d/shizuku/shizuku.1024/texture_04.png"
                };
                return TEXTURE_PATHS;
//                Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_SHORT).show();

            default:
                TEXTURE_PATHS = new String[]{"live2d/eplison/Epsilon_free.2048/texture_00.png"};
                return TEXTURE_PATHS;
        }
    }

    public static String getModelPath(int tag){
        String MODEL_PATH;
        switch (tag) {
            case 0:
                MODEL_PATH =  "live2d/eplison/Epsilon_free.moc";
                return MODEL_PATH;
            case 1:
                MODEL_PATH = "live2d/haru/haru.moc";
              return MODEL_PATH;
            case 2:
                MODEL_PATH = "live2d/hibiki/hibiki.moc";
                return MODEL_PATH;
            case 3:
                MODEL_PATH = "live2d/miku/miku.moc";
                return MODEL_PATH;
            case 4:
                MODEL_PATH = "live2d/shizuku/shizuku.moc";
                return MODEL_PATH;

            default:
                MODEL_PATH =  "live2d/eplison/Epsilon_free.moc";
                return MODEL_PATH;
        }
    }

//    public static boolean setModel(int tag){
//        switch (tag) {
//            case 0:
//                MyApplication.MODEL_PATH = "live2d/eplison/Epsilon_free.moc";
//                MyApplication.TEXTURE_PATHS = new String[]{"live2d/eplison/Epsilon_free.2048/texture_00.png"};
////                Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
//                return true;
//            case 1:
//                MyApplication.MODEL_PATH = "live2d/haru/haru.moc";
//                MyApplication.TEXTURE_PATHS = new String[]{
//                        "live2d/haru/haru.1024/texture_00.png",
//                        "live2d/haru/haru.1024/texture_01.png",
//                        "live2d/haru/haru.1024/texture_02.png"
//                };
////                Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
//                return true;
//            case 2:
//                MyApplication.MODEL_PATH = "live2d/hibiki/hibiki.moc";
//                MyApplication.TEXTURE_PATHS = new String[]{
//                        "live2d/hibiki/hibiki.2048/texture_00.png"
//                };
////                Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
//                return true;
//            case 3:
//                MyApplication.MODEL_PATH = "live2d/miku/miku.moc";
//                MyApplication.TEXTURE_PATHS = new String[]{
//                        "live2d/miku/miku.2048/texture_00.png"
//                };
//                return true;
////                Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
//            case 4:
//                MyApplication.MODEL_PATH = "live2d/shizuku/shizuku.moc";
//                MyApplication.TEXTURE_PATHS = new String[]{
//                        "live2d/shizuku/shizuku.1024/texture_00.png",
//                        "live2d/shizuku/shizuku.1024/texture_01.png",
//                        "live2d/shizuku/shizuku.1024/texture_02.png",
//                        "live2d/shizuku/shizuku.1024/texture_03.png",
//                        "live2d/shizuku/shizuku.1024/texture_04.png"
//                };
//                return true;
////                Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
//
//                default:
//                    return false;
//        }
//    }

    public static String callWho;
    public static NetThread netThread;
    public static double[] emotion = new double[10];
    public static double[] emotionH = new double[10];
    private final String TAG = MyApplication.class.getSimpleName();

    private static SharedPreferences account;

    private static Roster roster;
    private static AbstractXMPPConnection connection;
    private static MyApplication mInstance ;

    private AgoraAPIOnlySignal m_agoraAPI;
    private RtcEngine mRtcEngine;

    private static String domin;
    private static String ip;
    private static int port;


    private static ArrayList<String> subscribeList = new ArrayList<>();

    public static MyApplication the() {
        return mInstance;
    }

    public MyApplication() {
        mInstance = this;
    }

    private OnAgoraEngineInterface onAgoraEngineInterface;

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            if (onAgoraEngineInterface != null){
                onAgoraEngineInterface.onFirstRemoteVideoDecoded(uid ,width,height,elapsed);
            }
        }


        @Override
        public void onUserOffline(int uid, int reason) {
            Log.i(TAG, "onUserOffline uid: " + uid +" reason:" + reason);
            if (onAgoraEngineInterface != null){
                onAgoraEngineInterface.onUserOffline(uid ,reason);
            }

        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) {
            if (onAgoraEngineInterface != null){
                onAgoraEngineInterface.onUserMuteVideo(uid, muted);
            }

        }
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            Log.i(TAG ,"onJoinChannelSuccess channel:" + channel+ " uid:" + uid);
            if (onAgoraEngineInterface != null){
                onAgoraEngineInterface.onJoinChannelSuccess(channel, uid, elapsed);
            }
        }

    };

    public static void logout(){
        connection.disconnect();
        connection=initConnection();
    }

    private void initXmpp(String domin,String ip ,int port){
        MyApplication.domin=domin;
        MyApplication.ip=ip;
        MyApplication.port=port;
    }

    private static AbstractXMPPConnection initConnection() {
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setServiceName(domin)
                .setHost(ip)
                .setPort(port)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setConnectTimeout(3000)
                .setDebuggerEnabled(true)
                .setSendPresence(true)
                .build();
        AbstractXMPPConnection connection = new XMPPTCPConnection(config);
        return connection;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        setupAgoraEngine();
        initXmpp(getString(R.string.xmpp_domain),getString(R.string.xmpp_ip),Integer.parseInt(getString(R.string.xmpp_port)));
        connection=initConnection();
        account = getSharedPreferences("account", Context.MODE_PRIVATE);
        netThread = new NetThread();
        netThread.start();
//        Takt.stock(this).play();
    }
//    @Override
//    public void onTerminate() {
//        Takt.finish();
//        super.onTerminate();
//    }

    public RtcEngine getmRtcEngine() {
        return mRtcEngine;
    }

    public AgoraAPIOnlySignal getmAgoraAPI() {
        return m_agoraAPI;
    }


    private void setupAgoraEngine() {
        String appID = getString(R.string.agora_app_id);

        try {
            m_agoraAPI = AgoraAPIOnlySignal.getInstance(this, appID);
            mRtcEngine = RtcEngine.create(getBaseContext(),appID , mRtcEventHandler);
            Log.i(TAG ,"setupAgoraEngine mRtcEngine :" +mRtcEngine);

        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    public void setOnAgoraEngineInterface(OnAgoraEngineInterface onAgoraEngineInterface) {
        this.onAgoraEngineInterface = onAgoraEngineInterface;
    }

    public interface OnAgoraEngineInterface{
         void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed);


         void onUserOffline(int uid, int reason) ;


         void onUserMuteVideo(final int uid, final boolean muted);

         void onJoinChannelSuccess(String channel, int uid, int elapsed);



    }

    public static SharedPreferences getAccount() {
        return account;
    }


    public static Roster getRoster() {
        return roster;
    }

    public static void setRoster(Roster roster) {
        MyApplication.roster = roster;
    }

    public static AbstractXMPPConnection getConnection() {
        return connection;
    }

    public static void setConnection(AbstractXMPPConnection connection) {
        MyApplication.connection = connection;
    }
    public static ArrayList<String> getSubscribeList() {
        return subscribeList;
    }

    public static void setSubscribeList(ArrayList<String> subscribeList) {
        MyApplication.subscribeList = subscribeList;
    }
}

