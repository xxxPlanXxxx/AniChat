package com.planx.anichat.thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.planx.anichat.MyApplication;

/**
 * Author: WoDeFeiZhu
 * Date: 2018/6/21
 */
public class NetThread extends Thread {
    private NetHandler netHandler;
    public static final class NetHandler extends Handler{

        private NetThread netThread;

        NetHandler(NetThread thread) {
            this.netThread = thread;
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        netHandler = new NetHandler(this);
        Log.i("NetThread","+++++++++++++++++++run()");
        Looper.loop();
    }

    public static void send(){

    }
}
