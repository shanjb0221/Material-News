package com.example.myapplication.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.NewsListAdapter;
import com.example.myapplication.bean.NewsBean;
import com.example.myapplication.channel_editor.ChannelEntity;
import com.example.myapplication.constants.Constants;
import com.example.myapplication.databinding.FragmentListBaseBinding;
import com.example.myapplication.databinding.FragmentSearchBinding;
import com.example.myapplication.databinding.SearchBackBinding;
import com.example.myapplication.databinding.SearchFrontBinding;
import com.example.myapplication.service.WebPager;
import com.example.myapplication.utils.TimeUtil;
import com.example.myapplication.video_player.ScrollCalculatorHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.FutureCallback;
import com.roacult.backdrop.BackdropLayout.State;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import kotlin.Unit;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchBackBinding backB;
    private SearchFrontBinding frontB;
    private FragmentListBaseBinding B;
    private NavController nav;
    private State backdropState = State.CLOSE;

    private NewsListAdapter adapter;
    private LinearLayoutManager manager;
    private WebPager pager = null;
    private boolean requesting = false;
    private Executor executor;
    private Map<String, String> params;
    private ScrollCalculatorHelper helper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executor = new Handler()::post;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        params = new HashMap<>();
        if (binding == null) {
            binding = FragmentSearchBinding.inflate(inflater, container, false);
            setupToolBar();
            setupFrontLayer();
            setupBackLayer();
        }
        return binding.getRoot();
    }

    private void setupBackLayer() {
        backB = binding.back;
        List<String> categories = new ArrayList<>();
        for (ChannelEntity entity : Constants.allChannels)
            categories.add(entity.getName());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                R.layout.item_category, R.id.item_category, categories);
        backB.category.setAdapter(adapter);

        backB.category.setOnDismissListener(() -> {
            params.put("category", backB.category.getText().toString());
            refresh(false);
        });

        backB.startDate.addTextChangedListener(new BaseWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                backB.startDateField.setError(isAcceptable(editable.toString()) ? null : "格式错误，请检查输入");
            }
        });
        backB.startDate.setOnFocusChangeListener((view, b) -> {
            if (!b) {
                String input = backB.startDate.getText().toString();
                input = TimeUtil.normalize(input);
                backB.startDate.setText(input);
                params.put("startDate", input);
                refresh(false);
            }
        });
        backB.startDateField.setEndIconOnClickListener(view -> {
            backB.startDate.setText("");
            backB.startDateField.setError(null);
            params.remove("startDate");
            refresh(false);
        });

        backB.endDate.addTextChangedListener(new BaseWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                backB.endDateField.setError(isAcceptable(editable.toString()) ? null : "格式错误，请检查输入");
            }
        });
        backB.endDate.setOnFocusChangeListener((view, b) -> {
            if (!b) {
                String input = backB.endDate.getText().toString();
                input = TimeUtil.normalize(input);
                backB.endDate.setText(input);
                params.put("endDate", input);
                refresh(false);
            }
        });
        backB.endDateField.setEndIconOnClickListener(view -> {
            backB.endDate.setText("");
            backB.endDateField.setError(null);
            params.remove("endDate");
            refresh(false);
        });
    }

    private boolean isAcceptable(String str) {
        return str.equals("") || TimeUtil.normalize(str) != null;
    }

    private void closeKeyBoard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
        backB.startDate.clearFocus();
        backB.endDate.clearFocus();
    }

    private void setupToolBar() {
        MaterialToolbar bar = binding.toolbar;
        nav = NavHostFragment.findNavController(this);
        AppBarConfiguration conf = new AppBarConfiguration.Builder(nav.getGraph()).build();
        NavigationUI.setupWithNavController(bar, nav, conf);
        backdropState = State.CLOSE;
        MenuItem filter = bar.getMenu().findItem(R.id.menu_filter);
        MenuItem search = bar.getMenu().findItem(R.id.menu_search);

        SearchView view = (SearchView) search.getActionView();
        view.setIconifiedByDefault(false);
        view.setIconified(false);
        view.requestFocusFromTouch();
        view.setQueryHint("搜索");
        EditText editText = (EditText) view.findViewById(androidx.appcompat.R.id.search_src_text);
        editText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.material_on_primary_disabled));
        editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.design_default_color_on_primary));
        editText.requestFocus();
        view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                params.put("keyword", query);
                binding.dropdown.close();
                refresh(false);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        bar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_filter) {
                if (backdropState == State.CLOSE)
                    binding.dropdown.open();
                else
                    binding.dropdown.close();
                return true;
            }
            return false;
        });

        binding.dropdown.setOnBackdropChangeStateListener(state -> {
            backdropState = state;
            if (state == State.CLOSE) {
                closeKeyBoard();
                filter.setIcon(R.drawable.ic_filter_list_24);
            } else {
                filter.setIcon(R.drawable.ic_check_24);
            }
            return Unit.INSTANCE;
        });
    }

    private void setupFrontLayer() {
        frontB = binding.front;
        B = binding.front.list;
        List<NewsBean> items = new ArrayList<>();
        adapter = new NewsListAdapter(this.getContext(), getLayoutInflater(), items,
                new NewsListAdapter.OnNewsClickListener() {
                    @Override
                    public void onImageItemClick(NewsBean news) {
                        nav.navigate(SearchFragmentDirections.actionSearchFragmentToNewsDetailFragment(news));
                    }

                    @Override
                    public void onVideoItemClick(NewsBean news, int duration) {
                        SearchFragmentDirections.ActionSearchFragmentToNewsDetailFragment action = SearchFragmentDirections.actionSearchFragmentToNewsDetailFragment(news);
                        action.setDuration(duration);
                        Log.e("NewsDetail", "onVideoItemClick: " + duration);
                        nav.navigate(action);
                    }
                });
        manager = new LinearLayoutManager(this.getContext());
        B.recyclerView.setAdapter(adapter);
        B.recyclerView.setLayoutManager(manager);

        initListeners();
        refresh(false);
        requesting = false;
    }

    public void refresh(boolean showSuccess) {
        if (requesting && showSuccess) return;
        requesting = true;
        binding.front.title.setText("加载中...");
        B.swipeRefreshLayout.setRefreshing(true);
        pager = new WebPager(requireContext(), executor, params);
        pager.nextPage(new FutureCallback<List<NewsBean>>() {
            @Override
            public void onSuccess(List<NewsBean> result) {
                B.swipeRefreshLayout.setRefreshing(false);
                requesting = false;
                binding.front.title.setText(getString(R.string.text_result_count, pager.getCount()));
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

    private static class BaseWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // do nothing
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // do nothing
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // do nothing
        }
    }
}