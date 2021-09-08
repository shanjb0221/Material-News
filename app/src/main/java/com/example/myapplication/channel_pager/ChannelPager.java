package com.example.myapplication.channel_pager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.myapplication.channel_editor.ChannelEntity;
import com.example.myapplication.databinding.FragmentMainBinding;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class ChannelPager {

    private final FragmentMainBinding B;

    private final ChannelPagerAdapter adapter;
    private final TabLayoutMediator mediator;

    public ChannelPager(@NonNull Fragment parent, FragmentMainBinding binding, List<ChannelEntity> channels) {
        this.B = binding;

        adapter = new ChannelPagerAdapter(parent, channels);
        B.viewPager.setAdapter(adapter);

        mediator = new TabLayoutMediator(B.tabLayout, B.viewPager, (tab, position) -> tab.setText(channels.get(position).getName()));
        mediator.attach();
    }

    public ChannelPagerAdapter getAdapter() {
        return adapter;
    }
}
