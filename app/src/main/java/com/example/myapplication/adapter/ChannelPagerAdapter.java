package com.example.myapplication.adapter;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplication.fragment.DemoObjectFragment;
import com.example.myapplication.fragment.NewsListFragment;

public class ChannelPagerAdapter extends FragmentStateAdapter {

    private String TAG = "CollectionDemoFragment";

    public ChannelPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment res = new NewsListFragment();
        Bundle args = new Bundle();
        args.putInt(DemoObjectFragment.ARG_OBJECT, position);
        res.setArguments(args);
        return res;
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getCount: ");
        return 10;
    }
}
