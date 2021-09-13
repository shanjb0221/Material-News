package com.java.shanjingbo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.java.shanjingbo.adapter.NewsListAdapter;
import com.java.shanjingbo.databinding.FragmentListBaseBinding;
import com.java.shanjingbo.service.AbstractPager;
import com.java.shanjingbo.service.web.WebPager;

import java.util.HashMap;
import java.util.Map;

public class ListFragment extends AbstractListFragment {
    private final NewsListAdapter.OnNewsClickListener listener;
    private final Map<String, String> params;
    private FragmentListBaseBinding B;

    public ListFragment(String category, NewsListAdapter.OnNewsClickListener listener) {
        this.listener = listener;
        params = new HashMap<>();
        params.put("category", category);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (B == null) {
            B = FragmentListBaseBinding.inflate(inflater, container, false);
            super.onInitialize(B, listener);
        }
        return B.getRoot();
    }

    @Override
    protected AbstractPager onCreatePager() {
        return new WebPager(requireContext(), executor, params);
    }
}