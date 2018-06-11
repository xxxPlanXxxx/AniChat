package com.planx.anichat.Friend;

import android.util.Log;

import com.planx.anichat.MyApplication;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.packet.RosterPacket;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: WoDeFeiZhu
 * Date: 2018/6/4
 */
public class AddFriendListener {

    static AndFilter filter = new AndFilter(new StanzaTypeFilter(Presence.class));         //条件过滤器

    public static void add(){
        MyApplication.getConnection().addAsyncStanzaListener(packetListener,filter);
    }
    //packet监听器
    static StanzaListener packetListener = new StanzaListener() {

        @Override
        public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
            if (packet instanceof Presence) {
                Presence presence = (Presence) packet;
                String fromId = presence.getFrom();

                if (presence.getType().equals(Presence.Type.subscribe)) {
                    Log.i("AddFriendListener",fromId+"请求添加好友");
                    if(MyApplication.getRoster().getEntry(fromId)==null) {
                        MyApplication.getSubscribeList().add(fromId);
                    }
                }

                /*
                * 若是发送方，上线后自动接受请求
                * 若是接受方，不需要做处理
                * */
                else if (presence.getType().equals(Presence.Type.subscribed)) {//对方同意订阅
                    Log.i("AddFriendListener",fromId+"同意订阅");

                    //避免重复添加，判断是接受方还是发送方
                    Log.i("AddFriendListener", MyApplication.getRoster().getEntry(fromId).getType().toString());
                    //若此时接到的是接受方，则此时关系已经为both，不需要做处理
                    //若此时是发送方，关系不是both，还需要确认接受方的信息
                    if(!MyApplication.getRoster().getEntry(fromId).getType().toString().equals("both")) {
                        Log.i("AddFriendListener","对方不在列表中");
                        //发送方确认
                        Presence pre = new Presence(Presence.Type.subscribed);
                        pre.setTo(fromId);
                        MyApplication.getConnection().sendStanza(pre);
                    }

                }

                else if (presence.getType().equals(Presence.Type.unsubscribe)) {//取消订阅
                    Log.i("AddFriendListener",fromId+"取消订阅");
                } else if (presence.getType().equals(Presence.Type.unsubscribed)) {//拒绝订阅
                    Log.i("AddFriendListener",fromId+"拒绝订阅" );
                } else if (presence.getType().equals(Presence.Type.unavailable)) {//离线
                    Log.i("AddFriendListener",fromId+"离线" );
                } else if (presence.getType().equals(Presence.Type.available)) {//上线
                    Log.i("AddFriendListener",fromId+"上线" );
                }
            }
        }
    };
}