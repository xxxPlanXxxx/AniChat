package com.planx.anichat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.planx.anichat.R;

import java.util.List;

public class FriendListAdapter extends BaseAdapter implements View.OnClickListener {
    private List<String> mList;
    private List<String> mPresence;
    private Context mContext;
    private InnerItemOnclickListener mListener;
    private int mPosition;

    public FriendListAdapter(List<String> mList,List<String> mPresence, Context mContext) {
        this.mList = mList;
        this.mPresence = mPresence;
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
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_friend,
                    null);
            viewHolder.bt = (Button) convertView.findViewById(R.id.bt_call);
            viewHolder.tv = (TextView) convertView.findViewById(R.id.friend_name);
            viewHolder.fp = (TextView) convertView.findViewById(R.id.friend_presence);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.bt.setOnClickListener(this);
        viewHolder.bt.setTag(position);
        viewHolder.tv.setText(mList.get(position));
        viewHolder.fp.setText(mPresence.get(position));
        return convertView;
    }

    public final class ViewHolder {
        Button bt;
        TextView tv;
        TextView fp;
    }

    public interface InnerItemOnclickListener {
        void itemClick(View v);
    }

    public void setOnInnerItemOnClickListener(InnerItemOnclickListener listener){
        this.mListener=listener;
    }
}
