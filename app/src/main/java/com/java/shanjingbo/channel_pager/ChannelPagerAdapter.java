package com.java.shanjingbo.channel_pager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.java.shanjingbo.adapter.NewsListAdapter;
import com.java.shanjingbo.channel_editor.ChannelEntity;
import com.java.shanjingbo.fragment.NewsListFragment;

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
