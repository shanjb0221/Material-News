package com.java.shanjingbo.fragment;

import static com.java.shanjingbo.R.id.menu_about;
import static com.java.shanjingbo.R.id.menu_clear;
import static com.java.shanjingbo.R.id.menu_edit_channel;
import static com.java.shanjingbo.R.id.menu_refresh;
import static com.java.shanjingbo.R.id.menu_search;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.FutureCallback;
import com.java.shanjingbo.R;
import com.java.shanjingbo.adapter.NewsListAdapter;
import com.java.shanjingbo.bean.NewsBean;
import com.java.shanjingbo.channel_editor.ChannelEditor;
import com.java.shanjingbo.channel_editor.ChannelEntity;
import com.java.shanjingbo.channel_pager.ChannelPager;
import com.java.shanjingbo.constants.Constants;
import com.java.shanjingbo.databinding.FragmentMainBinding;
import com.java.shanjingbo.service.database.DBService;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private static final String CHANNEL_SETTINGS = "channel_settings";
    private final int MODE_BUTTON = 0;
    private final int MODE_NAVIGATION_VIEW = 1;
    private final int MODE_TOP_APP_BAR = 2;
    SharedPreferences channelSettings;
    List<ChannelEntity> mineChannels, otherChannels;
    int fixedChannels;
    private FragmentMainBinding B;
    private NavController nav;
    private ChannelPager channelPager;
    private ChannelEditor channelEditor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        channelSettings = requireContext().getSharedPreferences(CHANNEL_SETTINGS, Context.MODE_PRIVATE);
        loadChannels();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (B == null) {
            B = FragmentMainBinding.inflate(inflater, container, false);
            nav = NavHostFragment.findNavController(this);
            AppBarConfiguration conf = new AppBarConfiguration.Builder(nav.getGraph()).setOpenableLayout(B.drawerLayout).build();
            NavigationUI.setupWithNavController(B.toolbar, nav, conf);
            setupToolBar();
            setupNavigationView();
            setupChannelPager();
            setupChannelEditor();
        }
        return B.getRoot();
    }

    private void setupChannelEditor() {
        channelEditor = new ChannelEditor(getContext(), B.channelEditor, mineChannels, otherChannels, 4);
        channelEditor.getAdapter().setOnMyChannelItemClickListener((view, position) -> {
            B.viewPager.setCurrentItem(position);
            channelEditor.getBehavior().setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        channelEditor.getAdapter().setChannelPagerAdapter(channelPager.getAdapter(), fixedChannels);
        channelEditor.getAdapter().setOnDatasetChangedListener(this::saveChannels);

    }

    private void setupChannelPager() {
        B.editChannelButton.setOnClickListener(view -> navigateTo(menu_edit_channel, MODE_BUTTON));
        channelPager = new ChannelPager(this, B, mineChannels, new NewsListAdapter.OnNewsClickListener() {
            @Override
            public void onImageItemClick(NewsBean news) {
                nav.navigate(MainFragmentDirections.actionMainFragmentToNewsDetailFragment(news));
            }

            @Override
            public void onVideoItemClick(NewsBean news, int duration) {
                MainFragmentDirections.ActionMainFragmentToNewsDetailFragment action = MainFragmentDirections.actionMainFragmentToNewsDetailFragment(news);
                action.setDuration(duration);
                nav.navigate(action);
            }
        });
    }

    private void setupNavigationView() {
        ImageView view = B.navigationView
                .getHeaderView(0)
                .findViewById(R.id.logo);
        view.setOnClickListener(new View.OnClickListener() {
            long preTime = 0;
            int clickCount = 0;

            @Override
            public void onClick(View view) {
                long nowTime = System.currentTimeMillis();
                if (nowTime - preTime < 500) clickCount++;
                else clickCount = 1;
                preTime = nowTime;
                if (clickCount == 5) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/shanjb0221/"));
                    requireContext().startActivity(intent);
                }
            }
        });
        B.navigationView.setNavigationItemSelectedListener(item ->
                NavigationUI.onNavDestinationSelected(item, nav)
                        || navigateTo(item.getItemId(), MODE_NAVIGATION_VIEW));
    }

    private void setupToolBar() {
        B.toolbar.setOnMenuItemClickListener(item ->
                NavigationUI.onNavDestinationSelected(item, nav)
                        || navigateTo(item.getItemId(), MODE_TOP_APP_BAR));
    }

    private boolean navigateTo(int destination, int mode) {
        if (mode == MODE_NAVIGATION_VIEW) {
            B.drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (destination == menu_search) {
            nav.navigate(MainFragmentDirections.actionMainFragmentToSearchFragment());
            return true;
        }
        if (destination == menu_edit_channel) {
            channelEditor.getAdapter().resetEditingMode();
            channelEditor.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            return true;
        }
        if (destination == menu_clear) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_warning_24)
                    .setTitle(R.string.menu_clear)
                    .setMessage("此操作将清空所有历史记录、收藏记录！")
                    .setPositiveButton("继续", (dialogInterface, i) -> DBService
                            .getInstance(requireContext())
                            .clear(new Handler()::post, new FutureCallback<Integer>() {
                                @Override
                                public void onSuccess(Integer result) {
                                    Snackbar.make(B.coordinatorLayout, "删除了 " + result + " 条记录", BaseTransientBottomBar.LENGTH_LONG).show();
                                    navigateTo(menu_refresh, MODE_BUTTON);
                                }

                                @Override
                                public void onFailure(@NonNull Throwable t) {
                                    Snackbar.make(B.coordinatorLayout, "操作失败！(" + t.getMessage() + ")", BaseTransientBottomBar.LENGTH_SHORT).show();
                                }
                            }))
                    .setNegativeButton("取消", (dialogInterface, i) -> {
                    }).show();

        }
        if (destination == menu_about) {
            Snackbar.make(B.coordinatorLayout, R.string.app_style, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.link, view -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://material.io/"));
                        requireContext().startActivity(intent);
                    }).show();
            return true;
        }
        if (destination == menu_refresh) {
            ListFragment cur = (ListFragment) getChildFragmentManager()
                    .findFragmentByTag("f" + mineChannels
                            .get(B.viewPager.getCurrentItem())
                            .getName().hashCode());
            cur.onRefresh(mode == MODE_TOP_APP_BAR);
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        B = null;
        nav = null;
    }

    private void loadChannels() {
        fixedChannels = 0;
        mineChannels = new ArrayList<>();
        otherChannels = new ArrayList<>();
        for (ChannelEntity channel : Constants.allChannels)
            if (channel.isFixed()) {
                mineChannels.add(channel);
                ++fixedChannels;
            }
        for (ChannelEntity channel : Constants.allChannels) {
            if (channel.isFixed()) continue;
            if (channelSettings.getBoolean(channel.getName(), false))
                mineChannels.add(channel);
            else
                otherChannels.add(channel);
        }
    }

    private void saveChannels() {
        SharedPreferences.Editor editor = channelSettings.edit();
        for (ChannelEntity item : mineChannels)
            editor.putBoolean(item.getName(), true);
        for (ChannelEntity item : otherChannels)
            editor.putBoolean(item.getName(), false);
        editor.apply();
    }

    @Override
    public void onDestroy() {
        saveChannels();
        super.onDestroy();
    }
}