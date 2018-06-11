package com.planx.anichat.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.planx.anichat.R;

import java.util.List;

/**
 * Author: WoDeFeiZhu
 * Date: 2018/6/4
 */
public class FriendSubscribeAdapter extends BaseAdapter implements View.OnClickListener {
    private List<String> mList;
    private Context mContext;
    private FriendSubscribeAdapter.InnerItemOnclickListener mListener;
    private int mPosition;

    public FriendSubscribeAdapter(List<String> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }
    @Override
    public void onClick(View v) {
        mListener.itemClick(v);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mPosition = position;
        final FriendSubscribeAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new FriendSubscribeAdapter.ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_subscribe,
                    null);
            viewHolder.bt_accept = (Button) convertView.findViewById(R.id.bt_subscribe_accept);
            viewHolder.bt_reject = convertView.findViewById(R.id.bt_subscribe_reject);
            viewHolder.tv = (TextView) convertView.findViewById(R.id.subscribe_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (FriendSubscribeAdapter.ViewHolder) convertView.getTag();
        }
        viewHolder.bt_accept.setOnClickListener(this);
        viewHolder.bt_accept.setTag(position);
        viewHolder.bt_reject.setTag(position);
        viewHolder.bt_reject.setOnClickListener(this);
        viewHolder.tv.setText(mList.get(position));
        return convertView;
    }

    public final class ViewHolder {
        Button bt_accept;
        Button bt_reject;
        TextView tv;
    }

    public interface InnerItemOnclickListener {
        void itemClick(View v);
    }

    public void setOnInnerItemOnClickListener(FriendSubscribeAdapter.InnerItemOnclickListener listener){
        this.mListener=listener;
    }
}
