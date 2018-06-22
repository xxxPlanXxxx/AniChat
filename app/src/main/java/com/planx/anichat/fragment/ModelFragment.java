package com.planx.anichat.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.planx.anichat.R;
import com.planx.anichat.adapter.UltraPagerAdapter;
import com.tmall.ultraviewpager.UltraViewPager;


public class ModelFragment extends Fragment {
    private PagerAdapter adapter;
    private UltraViewPager ultraViewPager;
    private Activity activity;
    private View layout;

    public UltraViewPager getUltraViewPager() {
        return ultraViewPager;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(layout==null){
            activity=this.getActivity();
            layout=activity.getLayoutInflater().inflate(R.layout.fragment_model,null);
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
    public void initView(){
        ultraViewPager = (UltraViewPager)layout.findViewById(R.id.model_viewpager);
        ultraViewPager.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);
        adapter = new UltraPagerAdapter(true);
        ultraViewPager.setAdapter(adapter);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    public void onResume() {
        super.onResume();
    }

}
