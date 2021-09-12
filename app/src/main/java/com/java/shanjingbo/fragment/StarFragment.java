package com.java.shanjingbo.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.FutureCallback;
import com.java.shanjingbo.R;
import com.java.shanjingbo.adapter.NewsListAdapter;
import com.java.shanjingbo.bean.NewsBean;
import com.java.shanjingbo.databinding.FragmentListBaseBinding;
import com.java.shanjingbo.databinding.FragmentListBinding;
import com.java.shanjingbo.service.DBPager;
import com.java.shanjingbo.video_player.ScrollCalculatorHelper;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class StarFragment extends Fragment {
    FragmentListBinding binding;
    FragmentListBaseBinding B;
    NavController nav;
    private boolean requesting = false;
    private DBPager pager;
    private NewsListAdapter adapter;
    private LinearLayoutManager manager;
    private ScrollCalculatorHelper helper;
    private Executor executor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executor = new Handler()::post;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (binding == null) {
            binding = FragmentListBinding.inflate(inflater, container, false);
            setupToolBar();
            setupList();
        }
        return binding.getRoot();
    }

    private void setupList() {
        B = binding.list;

        List<NewsBean> items = new ArrayList<>();
        adapter = new NewsListAdapter(this.getContext(), getLayoutInflater(), items,
                new NewsListAdapter.OnNewsClickListener() {
                    @Override
                    public void onImageItemClick(NewsBean news) {
                        nav.navigate(StarFragmentDirections.actionStarFragmentToNewsDetailFragment(news));
                    }

                    @Override
                    public void onVideoItemClick(NewsBean news, int duration) {
                        StarFragmentDirections.ActionStarFragmentToNewsDetailFragment action =
                                StarFragmentDirections.actionStarFragmentToNewsDetailFragment(news);
                        action.setDuration(duration);
                        Log.e("NewsDetail", "onVideoItemClick: " + duration);
                        nav.navigate(action);
                    }
                });
        manager = new LinearLayoutManager(this.getContext());
        B.recyclerView.setAdapter(adapter);
        B.recyclerView.setLayoutManager(manager);

        initListeners();
        requesting = false;
        refresh(false);
    }

    private void setupToolBar() {
        nav = NavHostFragment.findNavController(this);
        AppBarConfiguration conf = new AppBarConfiguration.Builder(nav.getGraph()).build();
        NavigationUI.setupWithNavController(binding.toolbar, nav, conf);
    }

    public void refresh(boolean showSuccess) {
        requesting = true;
        B.swipeRefreshLayout.setRefreshing(true);
        pager = new DBPager(requireContext(), executor, true);
        pager.nextPage(new FutureCallback<List<NewsBean>>() {
            @Override
            public void onSuccess(List<NewsBean> result) {
                B.swipeRefreshLayout.setRefreshing(false);
                requesting = false;
                adapter.replaceItems(result);
                if (showSuccess)
                    Snackbar.make(B.swipeRefreshLayout, "刷新成功", Snackbar.LENGTH_SHORT).show();
                if (pager.isLastPage()) adapter.setLoaderStatus(NewsListAdapter.NO_MORE);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                B.swipeRefreshLayout.setRefreshing(false);
                requesting = false;
                Snackbar.make(B.swipeRefreshLayout, "刷新失败！(" + t.getMessage() + ")", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    public void loadMore() {
        if (pager == null || requesting) {
            adapter.setLoaderStatus(NewsListAdapter.IDLE);
            return;
        }
        requesting = true;
        pager.nextPage(new FutureCallback<List<NewsBean>>() {
            @Override
            public void onSuccess(List<NewsBean> result) {
                adapter.setLoaderStatus(NewsListAdapter.IDLE);
                requesting = false;
                adapter.appendItemsToBack(result);
                Snackbar.make(B.swipeRefreshLayout, "加载了 " + result.size() + " 条", Snackbar.LENGTH_SHORT).show();
                if (pager.isLastPage()) {
                    adapter.setLoaderStatus(NewsListAdapter.NO_MORE);
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                adapter.setLoaderStatus(NewsListAdapter.IDLE);
                requesting = false;
                Snackbar.make(B.swipeRefreshLayout, "加载失败！(" + t.getMessage() + ")", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void initListeners() {
        // refresh
        B.swipeRefreshLayout.setOnRefreshListener(() -> refresh(true));

        // load more
        B.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem;

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                        lastVisibleItem + 1 == adapter.getItemCount() &&
                        adapter.getLoaderStatus() == NewsListAdapter.IDLE) {
                    adapter.setLoaderStatus(NewsListAdapter.LOADING);
                    loadMore();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                lastVisibleItem = manager.findLastVisibleItemPosition();
            }
        });

        // 视频自动播放
        // 限定范围为屏幕一半的上下偏移180
        int height = CommonUtil.getScreenHeight(requireContext());
        //自定播放帮助类
        helper = new ScrollCalculatorHelper(R.id.video, 0, height);

        B.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int firstVisibleItem, lastVisibleItem;

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                helper.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                firstVisibleItem = manager.findFirstVisibleItemPosition();
                lastVisibleItem = manager.findLastVisibleItemPosition();
                helper.onScroll(recyclerView, firstVisibleItem, lastVisibleItem);
            }
        });

    }
}