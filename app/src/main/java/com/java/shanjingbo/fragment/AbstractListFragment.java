package com.java.shanjingbo.fragment;

import android.os.Bundle;
import android.os.Handler;

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
import com.java.shanjingbo.service.AbstractPager;
import com.java.shanjingbo.utils.ScrollCalculatorHelper;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public abstract class AbstractListFragment extends Fragment {
    protected FragmentListBaseBinding baseBinding;

    protected NewsListAdapter adapter;
    protected LinearLayoutManager manager;
    protected AbstractPager pager = null;
    protected boolean requesting = false;
    protected Executor executor;
    protected ScrollCalculatorHelper helper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executor = new Handler()::post;
    }

    protected abstract AbstractPager onCreatePager();

    protected void onInitialize(FragmentListBaseBinding baseBinding, NewsListAdapter.OnNewsClickListener listener) {
        this.baseBinding = baseBinding;
        List<NewsBean> items = new ArrayList<>();
        adapter = new NewsListAdapter(this.getContext(), getLayoutInflater(), items, listener);
        manager = new LinearLayoutManager(this.getContext());
        baseBinding.recyclerView.setAdapter(adapter);
        baseBinding.recyclerView.setLayoutManager(manager);
        onInitListeners();
        requesting = false;
        onRefresh(false);
    }

    protected void onRefreshSuccess() {
        // default: do nothing
    }

    public void onRefresh(boolean showSuccess) {
        requesting = true;
        baseBinding.swipeRefreshLayout.setRefreshing(true);
        pager = onCreatePager();
        pager.nextPage(new FutureCallback<List<NewsBean>>() {
            @Override
            public void onSuccess(List<NewsBean> result) {
                baseBinding.swipeRefreshLayout.setRefreshing(false);
                baseBinding.recyclerView.scrollToPosition(0);
                requesting = false;
                adapter.replaceItems(result);
                if (showSuccess)
                    Snackbar.make(baseBinding.swipeRefreshLayout, "刷新成功", Snackbar.LENGTH_SHORT).show();
                if (pager.isLastPage()) adapter.setLoaderStatus(NewsListAdapter.NO_MORE);
                else adapter.setLoaderStatus(NewsListAdapter.IDLE);
                onRefreshSuccess();
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                baseBinding.swipeRefreshLayout.setRefreshing(false);
                requesting = false;
                Snackbar.make(baseBinding.swipeRefreshLayout, "刷新失败！(" + t.getMessage() + ")", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    public void onLoadMore() {
        if (pager == null || requesting) {
            adapter.setLoaderStatus(NewsListAdapter.IDLE);
            return;
        }
        requesting = true;
        pager.nextPage(new FutureCallback<List<NewsBean>>() {
            @Override
            public void onSuccess(List<NewsBean> result) {
                requesting = false;
                adapter.appendItemsToBack(result);
                Snackbar.make(baseBinding.swipeRefreshLayout, "加载了 " + result.size() + " 条", BaseTransientBottomBar.LENGTH_LONG).show();
                if (pager.isLastPage()) adapter.setLoaderStatus(NewsListAdapter.NO_MORE);
                else adapter.setLoaderStatus(NewsListAdapter.IDLE);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                adapter.setLoaderStatus(NewsListAdapter.IDLE);
                requesting = false;
                Snackbar.make(baseBinding.swipeRefreshLayout, "加载失败！(" + t.getMessage() + ")", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    protected void onInitListeners() {
        // refresh
        baseBinding.swipeRefreshLayout.setOnRefreshListener(() -> onRefresh(true));

        // load more
        baseBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem;

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                        lastVisibleItem + 1 == adapter.getItemCount() &&
                        adapter.getLoaderStatus() == NewsListAdapter.IDLE) {
                    adapter.setLoaderStatus(NewsListAdapter.LOADING);
                    onLoadMore();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                lastVisibleItem = manager.findLastVisibleItemPosition();
            }
        });

        // video auto-play
        int height = CommonUtil.getScreenHeight(requireContext());
        helper = new ScrollCalculatorHelper(R.id.video, 0, height);

        baseBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
