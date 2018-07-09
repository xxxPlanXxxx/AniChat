package com.planx.anichat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.planx.anichat.MyApplication;
import com.planx.anichat.R;
import com.planx.anichat.activity.account.AccountActivity;
import com.planx.anichat.activity.friend.AddFriendActivity;
import com.planx.anichat.activity.video.CallActivity;
import com.planx.anichat.entity.TabEntity;
import com.planx.anichat.fragment.ChatFragment;
import com.planx.anichat.fragment.HomeFragment;
import com.planx.anichat.fragment.ModelFragment;
import com.planx.anichat.utils.MyUtils;
import com.planx.facecapture.CameraActivity;

import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.util.ArrayList;

import io.agora.AgoraAPI;
import io.agora.IAgoraAPI;

public class MainActivity extends AppCompatActivity {
    private String[] mTitles = {"模型", "消息", "主页",};
    private int[] mIconUnselectIds = {
            R.mipmap.tab_more_unselect,R.mipmap.tab_speech_unselect,R.mipmap.tab_contact_unselect};
    private int[] mIconSelectIds = {
            R.mipmap.tab_more_select,R.mipmap.tab_speech_select,R.mipmap.tab_contact_select};
    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private HomeFragment homeFragment; //主页面
    private ModelFragment moudelFragment;
    private ChatFragment chatFragment;

    private Toolbar toolbar;
    private  final String TAG = MainActivity.class.getSimpleName();

    private CommonTabLayout tabLayout;
    private String appId;
    private String account;
    private final int REQUEST_CODE = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG,"onCreate");
        //声网SDK
        appId = getString(R.string.agora_app_id);
        account = MyApplication.getAccount().getString("username","");
        MyApplication.the().getmAgoraAPI().login2(appId, account, "_no_need_token", 0, "" ,5,3  );
        addCallback();
        initView();
    }
    /**
     * 界面初始化
     */
    private void initView(){



        //标题栏
        toolbar =  findViewById(R.id.toolbar);
        toolbar.setTitle("AniChat");
        setSupportActionBar(toolbar);

        //底部导航
        tabLayout=findViewById(R.id.tl_main);
        for (int i = 0; i < mTitles.length; i++) {
            mTabEntities.add(new TabEntity(mTitles[i], mIconSelectIds[i], mIconUnselectIds[i]));
        }
        homeFragment = new HomeFragment();
        moudelFragment = new ModelFragment();
        chatFragment = new ChatFragment();
        mFragments.add(moudelFragment);
        mFragments.add(chatFragment);
        mFragments.add(homeFragment);
        tabLayout.setTabData(mTabEntities,this,R.id.fl_main,mFragments);
        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
        tabLayout.setCurrentTab(2);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent2 = new Intent(MainActivity.this,
                        AddFriendActivity.class);
                startActivity(intent2);
                return true;
            case R.id.action_video:
                startActivity(new Intent(this, CameraActivity.class));
                return true;

            default:

                return super.onOptionsItemSelected(item);

        }
    }


    /**
     * 点击了头像
     *
     * @param v 按钮
     */
    public void onAvatarClick(View v){
        Intent intent = new Intent(getApplicationContext(), AccountActivity.class);
        startActivity(intent);
    }

    /**
     * 点击了选择模型
     *
     * @param v 按钮
     */
    public void onClickModelSelect(View v){
        final int tag = moudelFragment.getUltraViewPager().getCurrentItem();
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
                        vCard.setEmailHome(String.valueOf(tag));//占用此属性来存放用户使用的模型
                        vCardManager.saveVCard(vCard);
                    } catch (Exception e) {
                        e.printStackTrace();//
                    }
                }
            }
        });
        Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bar, menu);
        return true;
    }

    /**
     * 声网SDK的回调函数
     */
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
                        intent.putExtra("account", account);
                        intent.putExtra("channelName", channelID);
                        intent.putExtra("subscriber", account);
                        intent.putExtra("type", MyUtils.CALL_IN);
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
                        intent.putExtra("type", MyUtils.CALL_OUT);
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
                Log.i("onQueryUserStatusResult","+++++++状态"+status);
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
        addCallback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG ,"onDestroy");
//        RtcEngine.destroy();
    }

}
