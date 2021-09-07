package com.example.myapplication.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.adapter.ListRefreshAdapter;
import com.example.myapplication.bean.NewsBean;
import com.example.myapplication.databinding.FragmentNewsListBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class NewsListFragment extends Fragment {

    static final String TAG = "NewsListFragment";

    int globalDelta = 0;

    private FragmentNewsListBinding B;

    private ListRefreshAdapter adapter;
    private LinearLayoutManager manager;

    public FragmentNewsListBinding getBinding() {
        return B;
    }

    public void refresh() {
        globalDelta += 100;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<NewsBean> items = new ArrayList<>();
                for (int i = 0; i < 100; ++i) {
                    NewsBean item = new NewsBean();
                    item.setTitle("NewsItem #" + (i + globalDelta));
                    items.add(item);
                }
                adapter.replaceItems(items);
                B.swipeRefreshLayout.setRefreshing(false);
                Snackbar.make(B.swipeRefreshLayout, "刷新成功", Snackbar.LENGTH_SHORT).show();
            }
        }, 2000);
    }

    public void loadMore() {
        int delta = adapter.getItemCount() - 1;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<NewsBean> items = new ArrayList<>();

                for (int i = 0; i < 10; ++i) {
                    NewsBean item = new NewsBean();
                    item.setTitle("NewsItem #" + (i + delta));
                    items.add(item);
                }

                adapter.appendItemsToBack(items);
                adapter.setLoaderStatus(ListRefreshAdapter.IDLE);
                Snackbar.make(B.getRoot(), "更新了 " + items.size() + " 条", BaseTransientBottomBar.LENGTH_LONG).show();
            }
        }, 2000);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (B == null) {
            B = FragmentNewsListBinding.inflate(inflater, container, false);
            List<NewsBean> items = new ArrayList<>();
            for (int i = 0; i < 100; ++i) {
                NewsBean item = new NewsBean();
                item.setTitle("NewsItem #" + i);
                items.add(item);
            }
            adapter = new ListRefreshAdapter(this.getContext(), getLayoutInflater(), items);
            manager = new LinearLayoutManager(this.getContext());
            B.recyclerView.setAdapter(adapter);
            B.recyclerView.setLayoutManager(manager);
            initListeners();
        }
        return B.getRoot();
    }

    private void initListeners() {
        // refresh
        B.swipeRefreshLayout.setOnRefreshListener(this::refresh);

        // load more
        B.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem;

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                        lastVisibleItem + 1 == adapter.getItemCount() &&
                        adapter.getLoaderStatus() == ListRefreshAdapter.IDLE) {
                    adapter.setLoaderStatus(ListRefreshAdapter.LOADING);
                    loadMore();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                lastVisibleItem = manager.findLastVisibleItemPosition();
            }
        });

    }
}