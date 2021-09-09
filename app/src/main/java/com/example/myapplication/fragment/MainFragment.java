package com.example.myapplication.fragment;

import static com.example.myapplication.R.id.menu_about;
import static com.example.myapplication.R.id.menu_editChannel;
import static com.example.myapplication.R.id.menu_history;
import static com.example.myapplication.R.id.menu_refresh;
import static com.example.myapplication.R.id.menu_search;
import static com.example.myapplication.R.id.menu_star;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.R;
import com.example.myapplication.channel_editor.ChannelEditor;
import com.example.myapplication.channel_editor.ChannelEntity;
import com.example.myapplication.channel_pager.ChannelPager;
import com.example.myapplication.constants.Constants;
import com.example.myapplication.databinding.FragmentMainBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private static final String CHANNEL_SETTINGS = "channel_settings";
    private final int MODE_BUTTON = 0;
    private final int MODE_NAVIGATION_VIEW = 1;
    private final int MODE_TOP_APP_BAR = 2;
    private final int FIXED = -1;
    private final int MINE = 0;
    private final int OTHER = 1;
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
        channelSettings = getContext().getSharedPreferences(CHANNEL_SETTINGS, Context.MODE_PRIVATE);
        loadChannels();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (B == null) {
            B = FragmentMainBinding.inflate(inflater, container, false);
            nav = NavHostFragment.findNavController(this);
            setupTopAppBar();
            setupNavigationView();
            setupChannelPager();
            setupChannelEditor();
        }
        return B.getRoot();
    }

    private void setupChannelEditor() {
        channelEditor = new ChannelEditor(getContext(), B.channelEditor, mineChannels, otherChannels, 4);
        channelEditor.getAdapter().setOnMyChannelItemClickListener((view, position) -> {
            Snackbar.make(B.coordinatorLayout, "Channel: " + mineChannels.get(position).getName(), BaseTransientBottomBar.LENGTH_SHORT).show();
        });
        channelEditor.getAdapter().setChannelPagerAdapter(channelPager.getAdapter(), fixedChannels);
        channelEditor.getAdapter().setOnDatasetChangedListener(this::saveChannels);
    }

    private void setupChannelPager() {
        B.editChannelButton.setOnClickListener(view -> navigateTo(menu_editChannel, MODE_BUTTON));
        channelPager = new ChannelPager(this, B, mineChannels);
    }

    private void setupNavigationView() {
        B.navigationView.setNavigationItemSelectedListener(item -> navigateTo(item.getItemId(), MODE_NAVIGATION_VIEW));
    }

    private boolean navigateTo(int destination, int mode) {
        if (mode == MODE_NAVIGATION_VIEW) {
            B.drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (destination == menu_search) {
            nav.navigate(MainFragmentDirections.actionMainFragmentToSearchFragment());
            return true;
        }
        if (destination == menu_editChannel) {
            channelEditor.getAdapter().resetEditingMode();
            channelEditor.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            return true;
        }
        if (destination == menu_star) {
            nav.navigate(MainFragmentDirections.actionMainFragmentToStarFragment());
            return true;
        }
        if (destination == menu_history) {
            nav.navigate(MainFragmentDirections.actionMainFragmentToHistoryFragment());
            return true;
        }
        if (destination == menu_about) {
            Snackbar.make(B.coordinatorLayout, R.string.style, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.link, view -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://material.io/"));
                        requireContext().startActivity(intent);
                    }).show();
            return true;
        }
        if (destination == menu_refresh) {
            NewsListFragment cur = (NewsListFragment) getChildFragmentManager().findFragmentByTag("f" + B.viewPager.getCurrentItem());
            cur.getBinding().swipeRefreshLayout.setRefreshing(true);
            cur.refresh();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveChannels();
        B = null;
        nav = null;
    }

    private void setupTopAppBar() {
        B.topAppBar.setNavigationOnClickListener(view -> B.drawerLayout.openDrawer(GravityCompat.START));

        B.topAppBar.setOnMenuItemClickListener(item -> navigateTo(item.getItemId(), MODE_TOP_APP_BAR));

        MenuItem search = B.topAppBar.getMenu().findItem(menu_search);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Snackbar.make(B.coordinatorLayout, "Searching " + query, BaseTransientBottomBar.LENGTH_LONG).show();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

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