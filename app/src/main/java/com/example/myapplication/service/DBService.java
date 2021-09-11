package com.example.myapplication.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.myapplication.bean.NewsBean;
import com.example.myapplication.constants.Constants;
import com.example.myapplication.dao.NewsDao;
import com.example.myapplication.database.NewsDB;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.Executor;

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
