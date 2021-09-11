package com.example.myapplication.service;

import com.example.myapplication.bean.NewsBean;
import com.example.myapplication.constants.Constants;
import com.google.common.util.concurrent.FutureCallback;

import java.util.List;
import java.util.concurrent.Executor;

public abstract class AbstractPager {
    protected int curPage, count, pageSize;

    public AbstractPager(int pageSize) {
        this.pageSize = pageSize;
        this.count = -1;
        this.curPage = 0;
    }

    public AbstractPager() {
        this(Constants.pageSize);
    }

    public boolean isLastPage() {
        return curPage * pageSize >= count;
    }

    public int getCount() {
        return count;
    }

    public abstract void nextPage(FutureCallback<List<NewsBean>> callback);
}
