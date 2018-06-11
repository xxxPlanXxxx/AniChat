package com.planx.anichat.activity.friend;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.planx.anichat.Adapter.FriendListAdapter;
import com.planx.anichat.Adapter.FriendSearchListAdapter;
import com.planx.anichat.Adapter.FriendSubscribeAdapter;
import com.planx.anichat.MyApplication;
import com.planx.anichat.R;
import com.planx.anichat.activity.MainActivity;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AddFriendActivity extends AppCompatActivity implements FriendSearchListAdapter.InnerItemOnclickListener,
        AdapterView.OnItemClickListener {
    private EditText searchIdEdit;
    private FriendSearchListAdapter mFriendSearchListAdapter;
    private ListView mListView;
    private ArrayList<String> mItems = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        mListView = findViewById(R.id.list_search);
        mFriendSearchListAdapter = new FriendSearchListAdapter(mItems,this);
        mFriendSearchListAdapter.setOnInnerItemOnClickListener(this);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mFriendSearchListAdapter);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        searchIdEdit = findViewById(R.id.searchid);
        super.onResume();
    }

    public void onClickSubscribe(View v){
        Intent intent = new Intent(AddFriendActivity.this, ReceiveSubscribeActivity.class);
        startActivity(intent);
    }

    public void onClickSearchFriend(View v) {
        if (searchIdEdit.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "输入为空", Toast.LENGTH_SHORT).show();
        } else if (searchIdEdit.getText().toString().equals("*")) {
            Toast.makeText(getApplicationContext(), "输入无效", Toast.LENGTH_SHORT).show();
            searchIdEdit.setText("");
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        mItems.clear();
                        UserSearchManager usm = new UserSearchManager(MyApplication.getConnection());
                        Form searchForm = usm.getSearchForm("search." + MyApplication.getConnection().getServiceName());
                        Form answerForm = searchForm.createAnswerForm();
                        answerForm.setAnswer("Username", true);
                        answerForm.setAnswer("search", searchIdEdit.getText().toString());
                        ReportedData data = usm.getSearchResults(answerForm, "search." + MyApplication.getConnection().getServiceName());
                        List<ReportedData.Row> rowList = data.getRows();
                        for (ReportedData.Row row : rowList) {
                            if(!row.getValues("Username").get(0).equals(MyApplication.getAccount().getString("username",""))){
                            mItems.add(row.getValues("Username").get(0));
                            }
                        }
                        Message msg = new Message();
                        msg.what = 1;
                        mHandler.sendMessage(msg);
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
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    //发送好友请求
    @Override
    public void itemClick(View v) {
        final String addWho = mItems.get(Integer.parseInt(v.getTag().toString()));
        new Thread(new Runnable() {
            @Override
            public void run() {
//                    MyApplication.getRoster().createEntry(addWho+"@"+MyApplication.getConnection().getServiceName(),"", new String[]{"Friends"});
                    Presence presence = new Presence(Presence.Type.subscribe);
                    presence.setTo(addWho+"@"+MyApplication.getConnection().getServiceName());
                    try {
                        MyApplication.getConnection().sendStanza(presence);
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
                    mFriendSearchListAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
}
