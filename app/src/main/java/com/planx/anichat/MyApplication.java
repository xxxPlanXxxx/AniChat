package com.planx.anichat;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import io.agora.AgoraAPIOnlySignal;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;


public class MyApplication extends Application {
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
    }

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

