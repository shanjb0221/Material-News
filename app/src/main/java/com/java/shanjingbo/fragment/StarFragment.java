package com.java.shanjingbo.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.java.shanjingbo.adapter.NewsListAdapter;
import com.java.shanjingbo.bean.NewsBean;
import com.java.shanjingbo.databinding.FragmentListBinding;
import com.java.shanjingbo.service.AbstractPager;
import com.java.shanjingbo.service.database.DBPager;

public class StarFragment extends AbstractListFragment {
    FragmentListBinding B;
    NavController nav;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (B == null) {
            B = FragmentListBinding.inflate(inflater, container, false);
            setupToolBar();
            super.onInitialize(B.list, new NewsListAdapter.OnNewsClickListener() {
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
        }
        return B.getRoot();
    }

    private void setupToolBar() {
        nav = NavHostFragment.findNavController(this);
        AppBarConfiguration conf = new AppBarConfiguration.Builder(nav.getGraph()).build();
        NavigationUI.setupWithNavController(B.toolbar, nav, conf);
    }

    @Override
    protected AbstractPager onCreatePager() {
        return new DBPager(requireContext(), executor, true);
    }
}