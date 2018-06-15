/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.planx.anichat.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.itheima.pulltorefreshlib.PullToRefreshBase;
import com.itheima.pulltorefreshlib.PullToRefreshListView;
import com.planx.anichat.R;
import com.planx.anichat.Adapter.FriendListAdapter;
import com.planx.anichat.MyApplication;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeFragment extends Fragment implements FriendListAdapter.InnerItemOnclickListener,
        AdapterView.OnItemClickListener {
    private ArrayList<String> mUsername=new ArrayList<String>();
    private FriendListAdapter mArrayAdapter;
    private PullToRefreshListView mPullToRefreshListView;
    private ArrayList<String> mItems = new ArrayList<String>();
    private ArrayList<String> mPresences = new ArrayList<String>();
    private View layout;
    private Activity activity;
    private CircleImageView circleImageView;
    private VCardManager vCardManager;
    private Bitmap bitmap;
    private ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
    private TextView textView;
    private String nickName;
//    private WindowManager windowManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(layout==null){
            activity=this.getActivity();
            layout=activity.getLayoutInflater().inflate(R.layout.fragment_home,null);
//            windowManager=(WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
            initView();
        }
        else {
            ViewGroup parent = (ViewGroup) layout.getParent();
            if (parent != null) {
                parent.removeView(layout);
            }
        }
        return layout;
    }

    private void initView(){
        mPullToRefreshListView = layout.findViewById(R.id.pull_to_refresh_list_view);
        circleImageView = layout.findViewById(R.id.home_avatar);
        textView = layout.findViewById(R.id.home_name);
        getAvatar();
        friendList();
        mArrayAdapter = new FriendListAdapter(mItems,mPresences,bitmaps,activity);
        mArrayAdapter.setOnInnerItemOnClickListener(this);
        mPullToRefreshListView.setAdapter(mArrayAdapter);
        mPullToRefreshListView.setOnItemClickListener(this);
    }

    //获取当前用户头像
    private void getAvatar()  {
        vCardManager = VCardManager.getInstanceFor(MyApplication.getConnection());
        new Thread(new Runnable() {
            @Override
            public void run() {
                VCard vCard= null;
                try {
                    vCard = vCardManager.loadVCard();
                    if(vCard.getNickName()==null){
                        nickName = "匿名";
                    }else {
                        nickName = vCard.getNickName();
                    }
                    if(vCard.getAvatar()!=null) {
                        ByteArrayInputStream bais = new ByteArrayInputStream(
                                vCard.getAvatar());

                        bitmap = BitmapFactory.decodeStream(bais);
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
                    circleImageView.setImageBitmap(bitmap);
                    textView.setText(nickName);
                    break;
                case 2:
                    mArrayAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mItems.clear();
//        MyApplication.the().getmAgoraAPI().logout();
    }
    @Override
    public void onResume() {
        super.onResume();
        getAvatar();
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshListView.setOnRefreshListener(mListViewOnRefreshListener2);
    }


    @Override
    public void itemClick(View v) {
//        Toast.makeText(FriendListActicity.this,"呼叫"+mItems.get(m-1),Toast.LENGTH_LONG).show();
        String callwho = mUsername.get(Integer.parseInt(v.getTag().toString()));
//        Toast.makeText(FriendListActicity.this,"呼叫"+callwho,Toast.LENGTH_LONG).show();
        MyApplication.the().getmAgoraAPI().queryUserStatus(callwho);
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(activity,"点击"+position,Toast.LENGTH_LONG).show();
    }

    private void friendList(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                Set<RosterEntry> entries = MyApplication.getRoster().getEntries();
                for (RosterEntry entry : entries) {
                    String nameId = entry.getUser();
                    Log.i("HomeFragment",nameId);
                    String username = nameId.split("@")[0];
                    mUsername.add(username);
                    String name = "匿名";
                    Log.i("++++++++++",nameId);
                    try {
                        VCard vCard = vCardManager.loadVCard(nameId);
                        if (vCard.getAvatar() != null) {
                            ByteArrayInputStream bais = new ByteArrayInputStream(
                                    vCard.getAvatar());
                            bitmap = BitmapFactory.decodeStream(bais);
                            bitmaps.add(bitmap);
                        }else{
                            bitmaps.add(null);
                        }
                        if (vCard.getNickName() != null) {
                            name = vCard.getNickName();
                        }
                        mItems.add(name);
                    } catch (SmackException.NoResponseException e) {
                        e.printStackTrace();
                    } catch (XMPPException.XMPPErrorException e) {
                        e.printStackTrace();
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                    mPresences.add(MyApplication.getRoster().getPresence(entry.getUser()).getType().toString());
                }
                Message msg = new Message();
                msg.what = 2;
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    private PullToRefreshBase.OnRefreshListener2<ListView> mListViewOnRefreshListener2 = new PullToRefreshBase.OnRefreshListener2<ListView>() {

        /**
         * 下拉刷新回调
         * @param refreshView
         */
        @Override
        public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
            //模拟延时三秒刷新
            mPullToRefreshListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mItems.clear();
                    mPresences.clear();
                    bitmaps.clear();
                    friendList();
                    mArrayAdapter.notifyDataSetChanged();
                    mPullToRefreshListView.onRefreshComplete();//下拉刷新结束，下拉刷新头复位

                }
            }, 3000);
        }

        /**
         * 上拉加载更多回调
         * @param refreshView
         */
        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            //模拟延时三秒加载更多数据
            mPullToRefreshListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mArrayAdapter.notifyDataSetChanged();
                    mPullToRefreshListView.onRefreshComplete();//上拉加载更多结束，上拉加载头复位
                }
            }, 3000);
        }
    };



}
