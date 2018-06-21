package com.planx.anichat.activity.friend;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.planx.anichat.adapter.FriendSubscribeAdapter;
import com.planx.anichat.MyApplication;
import com.planx.anichat.R;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

public class ReceiveSubscribeActivity extends AppCompatActivity implements FriendSubscribeAdapter.InnerItemOnclickListener,AdapterView.OnItemClickListener {
    private final String TAG = MyApplication.class.getSimpleName();
    private FriendSubscribeAdapter mAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_subscribe);
        listView = findViewById(R.id.list_subscribe);
        mAdapter = new FriendSubscribeAdapter(MyApplication.getSubscribeList(),this);
        mAdapter.setOnInnerItemOnClickListener(this);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void itemClick(View v) {
        String jid = MyApplication.getSubscribeList().get(Integer.parseInt(v.getTag().toString()));
        Presence presence;
        switch (v.getId()){
            case R.id.bt_subscribe_accept:
                Log.i(TAG,"同意"+jid);
                //先同意订阅
                presence = new Presence(Presence.Type.subscribed);
                MyApplication.getSubscribeList().remove(Integer.parseInt(v.getTag().toString()));
                presence.setTo(jid);
                try {
                    MyApplication.getConnection().sendStanza(presence);
                    //后添加Roster并发送订阅请求（包含在createEntry中），对方上线后会自动接受请求
                    MyApplication.getRoster().createEntry(jid,"",new String[]{"Friends"});
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                } catch (SmackException.NotLoggedInException e) {
                    e.printStackTrace();
                } catch (SmackException.NoResponseException e) {
                    e.printStackTrace();
                } catch (XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                }
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.bt_subscribe_reject:
                Log.i(TAG,"拒绝"+jid);
                presence = new Presence(Presence.Type.unsubscribed);
                presence.setTo(jid);
                try {
                    MyApplication.getConnection().sendStanza(presence);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
