package com.example.myapplication.fragment;

import static com.example.myapplication.R.id.menu_about;
import static com.example.myapplication.R.id.menu_editChannel;
import static com.example.myapplication.R.id.menu_history;
import static com.example.myapplication.R.id.menu_refresh;
import static com.example.myapplication.R.id.menu_search;
import static com.example.myapplication.R.id.menu_star;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.example.myapplication.R;
import com.example.myapplication.adapter.ChannelPagerAdapter;
import com.example.myapplication.channel_editor.ChannelAdapter;
import com.example.myapplication.channel_editor.ChannelEntity;
import com.example.myapplication.channel_editor.ItemDragHelperCallback;
import com.example.myapplication.databinding.FragmentMainBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private final int MODE_BUTTON = 0;
    private final int MODE_NAVIGATION_VIEW = 1;
    private final int MODE_TOP_APP_BAR = 2;
    String TAG = "MainFragment";
    List<ChannelEntity> mineChannels, otherChannels;
    private FragmentMainBinding B;
    private NavController nav;
    private ChannelAdapter channelAdapter;
    private BottomSheetBehavior<LinearLayout> behavior;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (B == null) {
            B = FragmentMainBinding.inflate(inflater, container, false);
            nav = NavHostFragment.findNavController(this);
            setupTopAppBar();
            setupNavigationView();
            setupViewPager();
            setupBottomSheet();
        }
        return B.getRoot();
    }

    private void setupBottomSheet() {
        behavior = BottomSheetBehavior.from(B.channelEditor.bottomSheet);
        B.channelEditor.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });


        mineChannels = new ArrayList<>();
        otherChannels = new ArrayList<>();
        for (int i = 0; i < 10; ++i)
            mineChannels.add(new ChannelEntity("频道" + i));
        for (int i = 0; i < 20; ++i)
            otherChannels.add(new ChannelEntity("其他" + i));

        GridLayoutManager manager = new GridLayoutManager(getContext(), 4);
        B.channelEditor.editorView.setLayoutManager(manager);

        ItemDragHelperCallback callback = new ItemDragHelperCallback();
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(B.channelEditor.editorView);

        channelAdapter = new ChannelAdapter(getContext(), helper, mineChannels, otherChannels);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (channelAdapter.getItemViewType(position) & ChannelAdapter.ITEM) == 1 ? 1 : 4;
            }
        });
        B.channelEditor.editorView.setAdapter(channelAdapter);

        channelAdapter.setOnMyChannelItemClickListener((view, position) -> {
            Snackbar.make(B.coordinatorLayout, "Channel: " + mineChannels.get(position).getName(), BaseTransientBottomBar.LENGTH_SHORT).show();
        });
    }

    private void setupViewPager() {
        B.editChannelButton.setOnClickListener(view -> navigateTo(menu_editChannel, MODE_BUTTON));
        B.viewPager.setAdapter(new ChannelPagerAdapter(this));
        new TabLayoutMediator(B.tabLayout, B.viewPager, (tab, position) -> tab.setText("item #" + position)).attach();
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
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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

}