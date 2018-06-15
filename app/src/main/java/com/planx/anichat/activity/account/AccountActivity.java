package com.planx.anichat.activity.account;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.planx.anichat.MyApplication;
import com.planx.anichat.R;
import com.planx.anichat.activity.login.LoginActivity;
import com.planx.anichat.utils.MyUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.stringencoder.Base64;
import org.jivesoftware.smackx.si.packet.StreamInitiation;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static com.planx.anichat.utils.FileUtil.getRealFilePathFromUri;

public class AccountActivity extends AppCompatActivity {
    private final String TAG=AccountActivity.class.getSimpleName();
    private Toolbar toolbar;
    private Bitmap bitMap;
    private ImageView imageView;
    private VCardManager vCardManager;
    private EditText editText;
    private String nickName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        imageView = findViewById(R.id.img_avatar);
        editText = findViewById(R.id.account_name);
        toolbar = findViewById(R.id.toolbar_account);
        toolbar.setTitle("账户设置");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initData();
    }

    public void initData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    vCardManager = VCardManager.getInstanceFor(MyApplication.getConnection());
                    VCard vCard = vCardManager.loadVCard();
                    if(vCard.getNickName()==null){
                        nickName="匿名";
                    }else {
                        nickName=vCard.getNickName();
                    }
                    if(vCard.getAvatar()!=null) {
                        ByteArrayInputStream bais = new ByteArrayInputStream(
                                vCard.getAvatar());
                        bitMap = BitmapFactory.decodeStream(bais);
                        Message msg = new Message();
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                    }
                } catch (SmackException.NoResponseException e) {
                    e.printStackTrace();
                } catch (XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    imageView.setImageBitmap(bitMap);
                    editText.setText(nickName);
                    break;
            }
        }
    };

    public void onAccountSetClick(View v) {
        switch (v.getId()) {
            case R.id.bt_sign_out:
                MyApplication.the().getmAgoraAPI().logout();
                SharedPreferences user = getSharedPreferences("account", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = user.edit();
                editor.remove("username");
                editor.remove("passwd");
                editor.commit();
                Intent intent1 = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(intent1);
                MyApplication.logout();
                finish();
                break;

            case R.id.bt_set_avatar:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, MyUtils.SCAN_OPEN_PHONE);
                break;
            case R.id.bt_set_nickname:
                nickName = editText.getText().toString();
                new Thread(new Runnable() {
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
                                VCard vCard = vCardManager.loadVCard();
                                vCard.setNickName(nickName);
                                vCardManager.saveVCard(vCard);
                            } catch (Exception e) {
                                e.printStackTrace();//
                            }
                        }
                    }
                }).start();
                break;
        }
    }

    public void setAvatar(final Bitmap bitmap) {
        new Thread(new Runnable() {
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
                        VCard vCard = vCardManager.loadVCard();
                        byte[] bytes = bitmapToByte(bitmap);
                        vCard.setAvatar(bytes);
                        vCardManager.saveVCard(vCard);
                    } catch (Exception e) {
                        e.printStackTrace();//
                    }
                }
            }
        }).start();
    }

    private static byte[] bitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public void gotoClipActivity(Uri uri) {
        Log.i("onActivityResult","开使编辑");
        if (uri == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setClass(this, ClipImageActivity.class);
        intent.setData(uri);
        Log.i("onActivityResult","编辑");
        startActivityForResult(intent, MyUtils.PHONE_CROP);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d("onActivityResult","+++++++++++++++++");
        switch (requestCode) {
            case MyUtils.SCAN_OPEN_PHONE:  //调用系统相册返回
                Log.i("onActivityResult","调用系统相册返回");
                if (resultCode == RESULT_OK) {
                    Uri uri = intent.getData();
                    gotoClipActivity(uri);

                }
                break;
            case MyUtils.PHONE_CROP:  //剪切图片返回
                if (resultCode == RESULT_OK) {
                    final Uri uri = intent.getData();
                    if (uri == null) {
                        return;
                    }
                    String cropImagePath = getRealFilePathFromUri(getApplicationContext(), uri);
                    bitMap = BitmapFactory.decodeFile(cropImagePath);
                        imageView.setImageBitmap(bitMap);
                    Log.i(TAG,"开始上传");
                    setAvatar(bitMap);
                }
                break;
        }
    }
}
