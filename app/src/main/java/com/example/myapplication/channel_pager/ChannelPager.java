package com.example.myapplication.channel_pager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.myapplication.adapter.NewsListAdapter;
import com.example.myapplication.channel_editor.ChannelEntity;
import com.example.myapplication.databinding.FragmentMainBinding;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class ChannelPager {

    private final ChannelPagerAdapter adapter;

    public ChannelPager(@NonNull Fragment parent, FragmentMainBinding binding, List<ChannelEntity> channels, NewsListAdapter.OnNewsClickListener listener) {
        adapter = new ChannelPagerAdapter(parent, channels, listener);
        binding.viewPager.setAdapter(adapter);

        TabLayoutMediator mediator = new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> tab.setText(channels.get(position).getName()));
        mediator.attach();
    }

    public ChannelPagerAdapter getAdapter() {
        return adapter;
    }
}
