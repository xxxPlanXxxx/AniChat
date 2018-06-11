package com.planx.anichat.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import io.agora.AgoraAPI;
import io.agora.IAgoraAPI;

import com.planx.anichat.MyApplication;
import com.planx.anichat.R;
import com.planx.anichat.activity.friend.AddFriendActivity;
import com.planx.anichat.activity.friend.FriendListActicity;
import com.planx.anichat.activity.login.LoginActivity;
import com.planx.anichat.activity.video.CallActivity;
import com.planx.anichat.utils.Constant;

import io.agora.rtc.RtcEngine;


public class MainActivity extends AppCompatActivity {


    private  final String TAG = MainActivity.class.getSimpleName();

    private TextView welcomeText;
    private String appId;
    private String account;
    private final int REQUEST_CODE = 0x01;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG,"onCreate");
        welcomeText = findViewById(R.id.main_title);
        appId = getString(R.string.agora_app_id);
        account = MyApplication.getAccount().getString("username","");
        welcomeText.setText("欢迎"+account);
        MyApplication.the().getmAgoraAPI().login2(appId, account, "_no_need_token", 0, "" ,5,3  );
        addCallback();
    }

    public void onClickMain(View v){
        Log.i(TAG ,"onClickLogin");
        switch (v.getId()){
            case R.id.bt_friend:

                Intent intent = new Intent(MainActivity.this,FriendListActicity.class);
                Log.i(TAG ,"startActivity：FriendListActicity.class");
                startActivity(intent);
                break;
            case R.id.bt_out:
                MyApplication.the().getmAgoraAPI().logout();
                SharedPreferences user = getSharedPreferences("account", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = user.edit();
                editor.remove("username");
                editor.remove("passwd");
                editor.commit();
                Intent intent1 = new Intent(MainActivity.this,
                        LoginActivity.class);
                startActivity(intent1);
                MyApplication.logout();
                finish();
                break;
            case R.id.bt_add_friend:
                Intent intent2 = new Intent(MainActivity.this,
                        AddFriendActivity.class);
                startActivity(intent2);
                break;
        }

    }

    private void addCallback() {
        MyApplication.the().getmAgoraAPI().callbackSet(new AgoraAPI.CallBack() {

            @Override
            public void onLoginSuccess(int i, int i1) {
                Log.i(TAG ,"onLoginSuccess " + i + "  " + i1 );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       Log.i(TAG,"欢迎"+MyApplication.getAccount().getString("username",""));
                    }
                });
            }

            @Override
            public void onLogout(final int i) {
                Log.i(TAG ,"onLogout  i = " + i);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (i == IAgoraAPI.ECODE_LOGOUT_E_KICKED) { // other login the account
                            Toast.makeText(MainActivity.this, "账号在另一地登陆", Toast.LENGTH_SHORT).show();

                        } else if (i == IAgoraAPI.ECODE_LOGOUT_E_NET) { // net
                            Toast.makeText(MainActivity.this, "网络断开", Toast.LENGTH_SHORT).show();

                        }
                        MyApplication.getConnection().disconnect();
                        finish();
                    }
                });
            }

            @Override
            public void onLoginFailed(final int i) {
                Log.i(TAG ,"onLoginFailed " + i);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (i == IAgoraAPI.ECODE_LOGIN_E_NET){
                            Toast.makeText(MainActivity.this ,"Login Failed for the network is not available",Toast.LENGTH_SHORT ).show();
                        }
                    }
                });
            }


            @Override
            public void onInviteReceived(final String channelID, final String account, int uid, String s2) { //call out other remote receiver
                Log.i(TAG, "onInviteReceived  channelID = " + channelID + " account = " + account);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, CallActivity.class);
                        intent.putExtra("account", MyApplication.getAccount().getString("username",""));
                        intent.putExtra("channelName", channelID);
                        intent.putExtra("subscriber", account);
                        intent.putExtra("type", Constant.CALL_IN);
                        startActivityForResult(intent, REQUEST_CODE);
                    }
                });
            }

            @Override
            public void onInviteReceivedByPeer(final String channelID, final String account, int uid) {//call out other local receiver
                Log.i(TAG, "onInviteReceivedByPeer  channelID = " + channelID + "  account = " + account);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, CallActivity.class);
                        intent.putExtra("account", MyApplication.getAccount().getString("username",""));
                        intent.putExtra("channelName", channelID);
                        intent.putExtra("subscriber", account);
                        intent.putExtra("type", Constant.CALL_OUT);
                        startActivityForResult(intent, REQUEST_CODE);
                    }
                });

            }

            @Override
            public void onInviteFailed(String channelID, String account, int uid, int i1, String s2) {
                Log.i(TAG, "onInviteFailed  channelID = " + channelID + " account = " + account + " s2: " + s2 + " i1: " + i1);
            }

            @Override
            public void onError(final String s, int i, final String s1) {
                Log.e(TAG, "onError s = " + s + " i = " + i + " s1 = " + s1);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (s.equals("query_user_status")) {
                            Toast.makeText(MainActivity.this, s1, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onQueryUserStatusResult(final String name, final String status) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (status.equals("1")) {
                            String channelName = MyApplication.getAccount().getString("username","")+name;
                            MyApplication.the().getmAgoraAPI().channelInviteUser(channelName, name, 0);
                        } else if (status.equals("0")) {
                            Toast.makeText(MainActivity.this, name + " is offline ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        });

    }

    @Override
    protected void onResume() {
        Log.i(TAG,"onCreate");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG ,"onDestroy");
        RtcEngine.destroy();
    }
}
