package com.example.myapplication.fragment;

import static java.net.HttpURLConnection.HTTP_OK;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.NewsListAdapter;
import com.example.myapplication.bean.NewsBean;
import com.example.myapplication.bean.ResponseBean;
import com.example.myapplication.databinding.FragmentNewsListBinding;
import com.example.myapplication.news_service.WebService;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsListFragment extends Fragment {

    static final String TAG = "NewsListFragment";

    private final String category;
    private FragmentNewsListBinding B;
    private NewsListAdapter adapter;
    private LinearLayoutManager manager;
    private WebService.NewsPager newsPager;
    public NewsListFragment(String category) {
        this.category = category;
    }

    public FragmentNewsListBinding getBinding() {
        return B;
    }

    public void refresh() {
        B.swipeRefreshLayout.setRefreshing(true);
        newsPager = new WebService.NewsPager().setCategory(category);
        newsPager.nextPage().enqueue(new Callback<ResponseBean>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBean> call, @NonNull Response<ResponseBean> response) {
                Log.e(TAG, "getItems(refresh): code = " + response.code());
                if (response.code() != HTTP_OK) {
                    B.swipeRefreshLayout.setRefreshing(false);
                    Snackbar.make(B.swipeRefreshLayout, "刷新失败！(网络错误)", BaseTransientBottomBar.LENGTH_LONG).show();
                    return;
                }
                ResponseBean bean = response.body();
                assert bean != null;
                newsPager.setTotal(bean.getTotal());
                adapter.replaceItems(bean.getData());

                for (NewsBean item : bean.getData())
                    Log.d(TAG, "img: " + item.getImage());

                B.swipeRefreshLayout.setRefreshing(false);
                Snackbar.make(B.swipeRefreshLayout, "刷新成功", Snackbar.LENGTH_SHORT).show();
                if (!newsPager.hasNextPage())
                    adapter.setLoaderStatus(NewsListAdapter.NO_MORE);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBean> call, @NonNull Throwable t) {
                B.swipeRefreshLayout.setRefreshing(false);
                Snackbar.make(B.swipeRefreshLayout, "刷新失败！(" + t.getMessage() + ")", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    public void loadMore() {
        if (newsPager == null) {
            adapter.setLoaderStatus(NewsListAdapter.IDLE);
            return;
        }
        newsPager.nextPage().enqueue(new Callback<ResponseBean>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBean> call, @NonNull Response<ResponseBean> response) {
                Log.e(TAG, "getItems(loadMore): code = " + response.code());
                if (response.code() != HTTP_OK) {
                    adapter.setLoaderStatus(NewsListAdapter.IDLE);
                    Snackbar.make(B.swipeRefreshLayout, "加载失败！(网络错误)", BaseTransientBottomBar.LENGTH_LONG).show();
                    return;
                }
                ResponseBean bean = response.body();
                Log.e(TAG, "onResponse: \n" + bean);
                assert bean != null;
                adapter.appendItemsToBack(bean.getData());


                for (NewsBean item : bean.getData()) {
                    Log.d(TAG, "img: " + item.getImage());

                }

                adapter.setLoaderStatus(NewsListAdapter.IDLE);
                Snackbar.make(B.swipeRefreshLayout, "加载了 " + bean.getData().size() + " 条", BaseTransientBottomBar.LENGTH_LONG).show();
                if (!newsPager.hasNextPage())
                    adapter.setLoaderStatus(NewsListAdapter.NO_MORE);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBean> call, @NonNull Throwable t) {
                t.printStackTrace();
                adapter.setLoaderStatus(NewsListAdapter.IDLE);
                Snackbar.make(B.swipeRefreshLayout, "加载失败！(" + t.getMessage() + ")", Snackbar.LENGTH_SHORT).show();
            }
        });
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
            adapter = new NewsListAdapter(this.getContext(), getLayoutInflater(), items);
            manager = new LinearLayoutManager(this.getContext());
            B.recyclerView.setAdapter(adapter);
            B.recyclerView.setLayoutManager(manager);
            initListeners();
        }
        return B.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        refresh();
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

    }
}