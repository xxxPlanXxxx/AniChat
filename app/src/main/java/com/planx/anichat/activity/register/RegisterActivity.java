package com.planx.anichat.activity.register;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mob.MobSDK;
import com.planx.anichat.MyApplication;
import com.planx.anichat.R;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.iqregister.packet.Registration;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;

import java.io.IOException;
import java.util.List;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;


public class RegisterActivity extends AppCompatActivity{
    private String phone;
    private EditText phoneEdit;
    private EditText codeEdit;
    private final int SEND_SUCESS =1;
    private final int SEND_FAILED =2;
    private final int SUBMINT_SUCESS =3;
    private final int SUBMINT_FAILED =4;
    private Boolean isExsit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        MobSDK.init(this);
        init();
    }

    private void init(){
        phoneEdit = findViewById(R.id.phone_number);
        codeEdit = findViewById(R.id.verification_code);
    }

    public void RegisterClick(View v) throws XMPPException, IOException, SmackException {
        switch (v.getId()){
            case R.id.bt_sendMessage:
                SMSSDK.unregisterAllEventHandler();
                SMSSDK.registerEventHandler(sendCallBack); //发送短信回调
                phone = phoneEdit.getText().toString().trim();
                ExsitPhone();
                break;
            case R.id.bt_register:
                SMSSDK.unregisterAllEventHandler();
                SMSSDK.registerEventHandler(submitCallBack); //验证短信回调
                String code = codeEdit.getText().toString().trim();
                SMSSDK.submitVerificationCode("86", phone, code); //验证验证码，第一个参数为国家，中国为"86"，第二个参数为手机号，第三个参数为要验证的验证码
                break;
        }

    }
    private void ExsitPhone(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Message msg = new Message();
                    msg.what=66; //该号码没有没注册过
                    if(!MyApplication.getConnection().isConnected()) {
                        MyApplication.getConnection().connect();
                    }
                    if (!MyApplication.getConnection().isAuthenticated()){
                        MyApplication.getConnection().login("search", "search");
                    }
                    UserSearchManager usm = new UserSearchManager(MyApplication.getConnection());
                    Form searchForm = usm.getSearchForm("search."+ MyApplication.getConnection().getServiceName());
                    Form answerForm = searchForm.createAnswerForm();
                    answerForm.setAnswer("Username", true);
                    answerForm.setAnswer("search", phone);
                    ReportedData data = usm.getSearchResults(answerForm, "search."+MyApplication.getConnection().getServiceName());
                    List<ReportedData.Row> rowList = data.getRows();
                    for (ReportedData.Row row : rowList) {
                        if(row.getValues("Username").get(0).equals(phone)){
                            msg.what=88;//该号码已经被注册
                        }
                    }
                    mHandler.sendMessage(msg);

                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XMPPException e) {
                    e.printStackTrace();
                }finally {
                    MyApplication.logout();
                    Log.i("RegisterActivity","MyApplication.logout();");
                }

            }
        }).start();


    }
    private void Register(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    MyApplication.getConnection().connect();
                    AccountManager accountManager = AccountManager.getInstance(MyApplication.getConnection());
                    accountManager.sensitiveOperationOverInsecureConnection(true);
                    accountManager.createAccount(phone,"123");
                    Message msg = new Message();
                    msg.what=100;
                    mHandler.sendMessage(msg);
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SEND_SUCESS:
                    codeEdit.setClickable(true);
                    Toast.makeText(getApplicationContext(),"发送成功",Toast.LENGTH_LONG).show();
                    break;
                case SEND_FAILED:
                    Toast.makeText(getApplicationContext(),"发送失败",Toast.LENGTH_LONG).show();
                    break;
                case  SUBMINT_SUCESS:
                    Register();
//                    Toast.makeText(getApplicationContext(),"注册成功",Toast.LENGTH_LONG).show();
                    break;
                case SUBMINT_FAILED:
                    Toast.makeText(getApplicationContext(),"验证码错误",Toast.LENGTH_LONG).show();
                    break;
                case 66:
                    SMSSDK.getVerificationCode("86", phone); //发送验证码，第一个参数为国家，中国为"86"，第二个参数为手机号
                    break;
                case 88:
                    Toast.makeText(getApplicationContext(),"手机号已被注册",Toast.LENGTH_LONG).show();
                    break;
                case 100:
                    Toast.makeText(getApplicationContext(),"注册成功",Toast.LENGTH_LONG).show();
                    break;

            }
        }
    };

    EventHandler sendCallBack=new EventHandler(){
        @Override
        public void afterEvent(int event, int result, Object data) {
            Message msg = new Message();
            if (result == SMSSDK.RESULT_COMPLETE) {
                //回调完成
                msg.what=SEND_SUCESS;
            }else{
                msg.what=SEND_FAILED;
                ((Throwable)data).printStackTrace();
            }
            mHandler.sendMessage(msg);
        }
    };

    EventHandler submitCallBack=new EventHandler(){
        @Override
        public void afterEvent(int event, int result, Object data) {
            Message msg = new Message();
            if (result == SMSSDK.RESULT_COMPLETE) {
                //回调完成
                msg.what = SUBMINT_SUCESS;
            }else{
                msg.what = SUBMINT_FAILED;
                ((Throwable)data).printStackTrace();
            }
            mHandler.sendMessage(msg);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
