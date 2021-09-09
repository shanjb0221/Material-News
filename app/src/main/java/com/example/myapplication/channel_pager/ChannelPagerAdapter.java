package com.example.myapplication.channel_pager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplication.channel_editor.ChannelEntity;
import com.example.myapplication.fragment.DemoObjectFragment;
import com.example.myapplication.fragment.NewsListFragment;

import java.util.List;

public class ChannelPagerAdapter extends FragmentStateAdapter {

    private final String TAG = "CollectionDemoFragment";
    private final List<ChannelEntity> channels;

    public ChannelPagerAdapter(@NonNull Fragment fragment, List<ChannelEntity> channels) {
        super(fragment);
        this.channels = channels;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment res = new NewsListFragment(channels.get(position).getName());
        Bundle args = new Bundle();
        args.putInt(DemoObjectFragment.ARG_OBJECT, position);
        res.setArguments(args);
        return res;
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }
}
