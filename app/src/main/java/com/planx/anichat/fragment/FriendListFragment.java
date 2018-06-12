/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.planx.anichat.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.itheima.pulltorefreshlib.PullToRefreshBase;
import com.itheima.pulltorefreshlib.PullToRefreshListView;
import com.planx.anichat.R;
import com.planx.anichat.Adapter.FriendListAdapter;
import com.planx.anichat.MyApplication;

import org.jivesoftware.smack.roster.RosterEntry;
import java.util.ArrayList;
import java.util.Set;


public class FriendListFragment extends Fragment implements FriendListAdapter.InnerItemOnclickListener,
        AdapterView.OnItemClickListener {
    private  final String TAG = FriendListFragment.class.getSimpleName();
    private FriendListAdapter mArrayAdapter;
    private PullToRefreshListView mPullToRefreshListView;
    private ArrayList<String> mItems = new ArrayList<String>();
    private ArrayList<String> mPresences = new ArrayList<String>();
    private View layout;
    private Activity activity;
//    private WindowManager windowManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(layout==null){
            activity=this.getActivity();
            layout=activity.getLayoutInflater().inflate(R.layout.list_friend,null);
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
        friendList();
        mArrayAdapter = new FriendListAdapter(mItems,mPresences,activity);
        mArrayAdapter.setOnInnerItemOnClickListener(this);
        mPullToRefreshListView.setAdapter(mArrayAdapter);
        mPullToRefreshListView.setOnItemClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mItems.clear();
//        MyApplication.the().getmAgoraAPI().logout();
    }
    @Override
    public void onResume() {
        super.onResume();
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshListView.setOnRefreshListener(mListViewOnRefreshListener2);
    }



    @Override
    public void itemClick(View v) {
//        Toast.makeText(FriendListActicity.this,"呼叫"+mItems.get(m-1),Toast.LENGTH_LONG).show();
        String callwho = mItems.get(Integer.parseInt(v.getTag().toString()));
//        Toast.makeText(FriendListActicity.this,"呼叫"+callwho,Toast.LENGTH_LONG).show();
        MyApplication.the().getmAgoraAPI().queryUserStatus(callwho);
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(activity,"点击"+position,Toast.LENGTH_LONG).show();
    }

    private void friendList(){
        Set<RosterEntry> entries = MyApplication.getRoster().getEntries();
        Log.i(TAG,"获取Collection<RosterEntry> entries");
        Log.i(TAG,"添加自己");
        for (RosterEntry entry : entries) {
            String nameId = entry.getUser();
            String name = nameId.split("@")[0];
            mItems.add(name);
            mPresences.add(MyApplication.getRoster().getPresence(entry.getUser()).getType().toString());
        }
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
