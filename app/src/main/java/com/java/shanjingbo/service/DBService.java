package com.java.shanjingbo.service;

import android.content.Context;

import com.google.common.util.concurrent.ListenableFuture;
import com.java.shanjingbo.bean.NewsBean;
import com.java.shanjingbo.dao.NewsDao;
import com.java.shanjingbo.database.NewsDB;

import java.util.List;

public class DBService {
    private static final String TAG = "DB";
    private static DBService instance;

    final NewsDao dao;

    private DBService(Context context) {
        dao = NewsDB.getInstance(context).getNewsDao();
    }

    public static DBService getInstance(Context context) {
        if (instance == null) instance = new DBService(context);
        return instance;
    }

    public void add(NewsBean bean) {
        new Thread(() -> dao.insert(bean)).start();
    }

    public void delete(NewsBean bean) {
        new Thread(() -> dao.delete(bean)).start();
    }

    public ListenableFuture<List<NewsBean>> fetch() {
        return dao.getAll();
    }

    public void update(NewsBean bean) {
        new Thread(() -> dao.update(bean)).start();
    }

    public interface OnSyncSuccessListener {
        void onSuccess();
    }


}
