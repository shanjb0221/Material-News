package com.java.shanjingbo.service;

import com.google.common.util.concurrent.FutureCallback;
import com.java.shanjingbo.bean.NewsBean;
import com.java.shanjingbo.constants.Constants;

import java.util.List;

public abstract class AbstractPager {
    protected final int pageSize;
    protected int curPage;
    protected int count;

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
