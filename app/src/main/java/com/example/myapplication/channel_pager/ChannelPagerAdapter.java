package com.example.myapplication.channel_pager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplication.adapter.NewsListAdapter;
import com.example.myapplication.channel_editor.ChannelEntity;
import com.example.myapplication.fragment.NewsListFragment;

import java.util.List;

public class ChannelPagerAdapter extends FragmentStateAdapter {

    private final List<ChannelEntity> channels;
    private final NewsListAdapter.OnNewsClickListener listener;

    public ChannelPagerAdapter(@NonNull Fragment fragment, List<ChannelEntity> channels, NewsListAdapter.OnNewsClickListener listener) {
        super(fragment);
        this.channels = channels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new NewsListFragment(channels.get(position).getName(), listener);
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }
}
