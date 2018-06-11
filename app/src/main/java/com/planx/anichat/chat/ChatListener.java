package com.planx.anichat.chat;

import com.planx.anichat.MyApplication;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

/**
 * Author: WoDeFeiZhu
 * Date: 2018/6/5
 */
public class ChatListener {
    public static void add(){
        ChatManager chatManager = ChatManager.getInstanceFor(MyApplication.getConnection());
        chatManager.addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean createdLocally) {
                chat.addMessageListener(new ChatMessageListener() {
                    @Override
                    public void processMessage(Chat chat, Message message) {
                        String friend = message.getFrom().split("@")[0];
                    }
                });
            }
        });
    }
}
