package com.java.shanjingbo.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.FutureCallback;
import com.java.shanjingbo.R;
import com.java.shanjingbo.adapter.NewsListAdapter;
import com.java.shanjingbo.bean.NewsBean;
import com.java.shanjingbo.databinding.FragmentListBaseBinding;
import com.java.shanjingbo.service.WebPager;
import com.java.shanjingbo.video_player.ScrollCalculatorHelper;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class NewsListFragment extends Fragment {

    private final NewsListAdapter.OnNewsClickListener listener;
    private final Map<String, String> params;
    private FragmentListBaseBinding B;
    private NewsListAdapter adapter;
    private LinearLayoutManager manager;
    private WebPager pager = null;
    private boolean requesting;
    private ScrollCalculatorHelper helper;
    private Executor executor;

    public NewsListFragment(String category, NewsListAdapter.OnNewsClickListener listener) {
        this.listener = listener;
        params = new HashMap<>();
        params.put("category", category);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executor = new Handler()::post;
    }

    @NonNull
    public FragmentListBaseBinding getBinding() {
        return B;
    }

    public void refresh(boolean showSuccess) {
        if (requesting && showSuccess) return;
        requesting = true;
        B.swipeRefreshLayout.setRefreshing(true);
        pager = new WebPager(requireContext(), executor, params);
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
                Snackbar.make(B.swipeRefreshLayout, "加载了 " + result.size() + " 条", BaseTransientBottomBar.LENGTH_LONG).show();
                if (pager.isLastPage()) adapter.setLoaderStatus(NewsListAdapter.NO_MORE);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                adapter.setLoaderStatus(NewsListAdapter.IDLE);
                requesting = false;
                Snackbar.make(B.swipeRefreshLayout, "加载失败！(" + t.getMessage() + ")", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (B == null) {
            B = FragmentListBaseBinding.inflate(inflater, container, false);
            List<NewsBean> items = new ArrayList<>();
            adapter = new NewsListAdapter(this.getContext(), getLayoutInflater(), items, listener);
            manager = new LinearLayoutManager(this.getContext());
            B.recyclerView.setAdapter(adapter);
            B.recyclerView.setLayoutManager(manager);
            initListeners();
            requesting = false;
            refresh(false);
        }
        return B.getRoot();
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
        //限定范围为屏幕一半的上下偏移180
        int height = CommonUtil.getScreenHeight(requireContext());
        int delta = CommonUtil.dip2px(requireContext(), 180);
        //自定播放帮助类
        helper = new ScrollCalculatorHelper(R.id.video, height / 2 - delta, height / 2 + delta);

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