package com.planx.anichat.activity.login;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.planx.anichat.Friend.AddFriendListener;
import com.planx.anichat.R;
import com.planx.anichat.activity.MainActivity;
import com.planx.anichat.activity.register.RegisterActivity;
import com.planx.anichat.MyApplication;
import com.planx.anichat.chat.ChatListener;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameText;
    private EditText passwordText;
    private String username;
    private String password;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameText = findViewById(R.id.username);
        passwordText = findViewById(R.id.password);
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
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                    Intent intent = new Intent(LoginActivity.this,
                            MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case 2:
                    MyApplication.logout();
                    Toast.makeText(getApplicationContext(), "账号或密码错误，请重试！", Toast.LENGTH_LONG).show();
                    usernameText.setText("");
                    passwordText.setText("");
                    break;
                case 3:
                    MyApplication.logout();
                    Toast.makeText(getApplicationContext(), "密码已发生更改，请重新登陆！", Toast.LENGTH_LONG).show();
            }
        }
    };

    //登陆事件
    public void LoginClick(View view) {
            username = usernameText.getText().toString();
            password = passwordText.getText().toString();
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getApplicationContext(), "账号或密码不能为空", Toast.LENGTH_LONG).show();
                return;
            }
            final SharedPreferences.Editor editor = MyApplication.getAccount().edit();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
//                        if(!MyApplication.getConnection().isConnected()){
                            Log.i("LoginActivity","MyApplication.getConnection().connect();");
                            MyApplication.getConnection().connect();
//                        }
                        //if(!MyApplication.getConnection().isAuthenticated()){
                            MyApplication.getConnection().login(username, password);
                            Log.i("LoginActivity","MyApplication.getConnection().login("+username+", "+password+");");
                       // }
                    } catch (SmackException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    } finally {
                        Message msg = new Message();
                        if (MyApplication.getConnection().isAuthenticated()) {
                            msg.what = 1;
                            editor.putString("username", username);
                            editor.putString("passwd", password);
                            editor.commit();
                        } else {
                            msg.what = 2;
                        }
                        mHandler.sendMessage(msg);
                    }
                }
            }).start();
    }

    public void SignUpClick(View view){
        Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(intent);
    }
}
