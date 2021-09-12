package com.java.shanjingbo.service.web;

import static java.net.HttpURLConnection.HTTP_OK;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.java.shanjingbo.bean.NewsBean;
import com.java.shanjingbo.bean.ResponseBean;
import com.java.shanjingbo.service.AbstractPager;
import com.java.shanjingbo.service.database.DBService;
import com.java.shanjingbo.utils.TimeUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebPager extends AbstractPager {
    private final DBService db;
    private final Executor executor;
    private final WebService web;
    private final String startDate;
    private final String endDate;
    private final String keyword;
    private String category;

    public WebPager(Context context, Executor executor, Map<String, String> params) {
        super();
        this.executor = executor;
        this.db = DBService.getInstance(context);
        this.web = WebService.getInstance();
        endDate = nonNull(params.get("endDate"), TimeUtil.getCurrentTimeStr());
        startDate = nonNull(params.get("startDate"), "");
        keyword = nonNull(params.get("keyword"), "");
        category = nonNull(params.get("category"), "");
        if (Objects.equals(category, "全部")) category = "";
        curPage = 0;
        count = -1;
    }

    private String nonNull(String value, @NonNull String backup) {
        return value == null ? backup : value;
    }

    @Override
    public void nextPage(FutureCallback<List<NewsBean>> callback) {
        web.service.getPage(pageSize, ++curPage, startDate, endDate, keyword, category)
                .enqueue(new Callback<ResponseBean>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBean> call, @NonNull Response<ResponseBean> response) {
                        if (response.code() != HTTP_OK || response.body() == null) {
                            callback.onFailure(new IOException("Web Error"));
                            return;
                        }
                        if (count == -1) count = response.body().getTotal();
                        Futures.addCallback(WebService.getInstance().executorService.submit(() -> {
                            List<NewsBean> items = response.body().getData();
                            for (NewsBean item : items) {
                                NewsBean bean = db.dao.findById(item.getNewsID());
                                if (bean == null) continue;
                                item.setReadTime(bean.getReadTime());
                                item.setStar(bean.getStar());
                            }
                            return items;
                        }), callback, executor);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBean> call, @NonNull Throwable t) {
                        callback.onFailure(t);
                    }
                });
    }


}