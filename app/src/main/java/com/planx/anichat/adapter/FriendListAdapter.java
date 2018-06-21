package com.planx.anichat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.planx.anichat.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendListAdapter extends BaseAdapter implements View.OnClickListener {
    private List<String> mList;
    private List<Bitmap> bitmaps;
    private List<String> mPresences;
    private Context mContext;
    private InnerItemOnclickListener mListener;

    public FriendListAdapter(List<String> mList,List<String> mPresences,List<Bitmap> bitmaps, Context mContext) {
        this.mList = mList;
        this.bitmaps = bitmaps;
        this.mPresences = mPresences;
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
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_friend,
                    null);
            viewHolder.bt = (Button) convertView.findViewById(R.id.bt_call);
            viewHolder.tv = (TextView) convertView.findViewById(R.id.friend_name);
            viewHolder.fa =  convertView.findViewById(R.id.friend_avatar);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.bt.setOnClickListener(this);
        viewHolder.bt.setTag(position);
        viewHolder.tv.setText(mList.get(position));
        viewHolder.fa.setImageBitmap(bitmaps.get(position));
        if(mPresences.get(position).equals("available")){
            viewHolder.tv.setTextColor(Color.BLACK);
        }else {
            viewHolder.tv.setTextColor(Color.GRAY);
        }
        return convertView;
    }

    public final class ViewHolder {
        Button bt;
        TextView tv;
        CircleImageView fa;
    }

    public interface InnerItemOnclickListener {
        void itemClick(View v);
    }

    public void setOnInnerItemOnClickListener(InnerItemOnclickListener listener){
        this.mListener=listener;
    }
}
