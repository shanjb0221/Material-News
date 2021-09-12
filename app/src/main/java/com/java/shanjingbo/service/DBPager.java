package com.java.shanjingbo.service;

import android.content.Context;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.java.shanjingbo.bean.NewsBean;
import com.java.shanjingbo.dao.NewsDao;

import java.util.List;
import java.util.concurrent.Executor;

public class DBPager extends AbstractPager {
    private final NewsDao dao;
    private final Executor executor;
    private boolean star = false;

    public DBPager(Context context, Executor executor, boolean star) {
        this.dao = DBService.getInstance(context).dao;
        this.executor = executor;
        this.star = star;
    }

    @Override
    public void nextPage(FutureCallback<List<NewsBean>> callback) {
        if (curPage == 0) count = dao.count(star);
        Futures.addCallback(dao.getPage(star, pageSize, ++curPage), callback, executor);
    }
}