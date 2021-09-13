package com.java.shanjingbo.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.os.Bundle;
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
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.java.shanjingbo.R;
import com.java.shanjingbo.adapter.NewsListAdapter;
import com.java.shanjingbo.bean.NewsBean;
import com.java.shanjingbo.channel_editor.ChannelEntity;
import com.java.shanjingbo.constants.Constants;
import com.java.shanjingbo.databinding.FragmentSearchBinding;
import com.java.shanjingbo.databinding.SearchBackBinding;
import com.java.shanjingbo.databinding.SearchFrontBinding;
import com.java.shanjingbo.service.AbstractPager;
import com.java.shanjingbo.service.web.WebPager;
import com.java.shanjingbo.utils.TimeUtil;
import com.roacult.backdrop.BackdropLayout.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;

public class SearchFragment extends AbstractListFragment {

    private FragmentSearchBinding B;
    private SearchBackBinding backB;
    private SearchFrontBinding frontB;
    private NavController nav;
    private State backdropState = State.CLOSE;

    private Map<String, String> params;

    @Override
    protected AbstractPager onCreatePager() {
        return new WebPager(requireContext(), executor, params);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        params = new HashMap<>();
        if (B == null) {
            B = FragmentSearchBinding.inflate(inflater, container, false);
            setupToolBar();
            setupFrontLayer();
            setupBackLayer();
        }
        return B.getRoot();
    }

    private void setupBackLayer() {
        backB = B.back;
        List<String> categories = new ArrayList<>();
        for (ChannelEntity entity : Constants.allChannels)
            categories.add(entity.getName());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                R.layout.item_category, R.id.item_category, categories);
        backB.category.setAdapter(adapter);

        backB.category.setOnDismissListener(() -> {
            params.put("category", backB.category.getText().toString());
            onRefresh(false);
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
                onRefresh(false);
            }
        });
        backB.startDateField.setEndIconOnClickListener(view -> {
            backB.startDate.setText("");
            backB.startDateField.setError(null);
            params.remove("startDate");
            onRefresh(false);
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
                onRefresh(false);
            }
        });
        backB.endDateField.setEndIconOnClickListener(view -> {
            backB.endDate.setText("");
            backB.endDateField.setError(null);
            params.remove("endDate");
            onRefresh(false);
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
        MaterialToolbar bar = B.toolbar;
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
        EditText editText = view.findViewById(androidx.appcompat.R.id.search_src_text);
        editText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.material_on_primary_disabled));
        editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.design_default_color_on_primary));
        editText.requestFocus();
        view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                params.put("keyword", query);
                B.dropdown.close();
                closeKeyBoard();
                onRefresh(false);
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
                    B.dropdown.open();
                else
                    B.dropdown.close();
                return true;
            }
            return false;
        });

        B.dropdown.setOnBackdropChangeStateListener(state -> {
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
        frontB = B.front;
        super.onInitialize(frontB.list, new NewsListAdapter.OnNewsClickListener() {
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
    }

    @Override
    protected void onRefreshSuccess() {
        frontB.title.setText("约 " + pager.getCount() + " 条结果");
    }

    @Override
    public void onRefresh(boolean showSuccess) {
        frontB.title.setText("加载中...");
        super.onRefresh(showSuccess);
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