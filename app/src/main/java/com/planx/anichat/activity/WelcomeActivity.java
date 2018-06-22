package com.planx.anichat.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.planx.anichat.MyApplication;
import com.planx.anichat.R;
import com.planx.anichat.activity.login.LoginActivity;
import com.planx.anichat.chat.ChatListener;
import com.planx.anichat.friend.AddFriendListener;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;

import java.io.IOException;

import static com.planx.anichat.utils.MyUtils.verifyStoragePermissions;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        verifyStoragePermissions(this);
        initView();
    }

    private void initView(){
        final String username = MyApplication.getAccount().getString("username","");
        final String passwd = MyApplication.getAccount().getString("passwd","");
        Log.i("LoginActivity","username:"+username+"  passwd:"+passwd);
        if(!username.isEmpty()&&!passwd.isEmpty()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i("LoginActicity","自动登陆");
                        MyApplication.getConnection().connect();
                        Log.i("LoginActivity","MyApplication.getConnection().connect();");
                        MyApplication.getConnection().login(username, passwd);
                        Log.i("LoginActivity","MyApplication.getConnection().login("+username+", "+passwd+");");
                    } catch (SmackException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    } finally {
                        Message msg = new Message();
                        if (MyApplication.getConnection().isAuthenticated()) {
                            msg.what=1;
                        } else {
                            msg.what=3;
                        }
                        mHandler.sendMessage(msg);
                    }
                }
            }).start();
        }else{
            Intent intent = new Intent(getApplicationContext(),
                    LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    Toast.makeText(getApplicationContext(), "登陆成功", Toast.LENGTH_LONG).show();
                    Presence presence = new Presence(Presence.Type.available);
                    try {
                        MyApplication.getConnection().sendStanza(presence);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                    MyApplication.setRoster(Roster.getInstanceFor(MyApplication.getConnection()));
                    MyApplication.getRoster().setSubscriptionMode(Roster.SubscriptionMode.manual);
                    AddFriendListener.add();
                    ChatListener.add();
                    Intent intent = new Intent(getApplicationContext(),
                            MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case 3:
                    MyApplication.logout();
                    Toast.makeText(getApplicationContext(), "密码已发生更改，请重新登陆！", Toast.LENGTH_LONG).show();
                    Intent intent1 = new Intent(getApplicationContext(),
                            MainActivity.class);
                    startActivity(intent1);
                    finish();
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
