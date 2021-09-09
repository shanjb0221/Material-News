package com.example.myapplication.news_service;

import static java.net.HttpURLConnection.HTTP_OK;

import android.util.Log;

import com.example.myapplication.bean.NewsBean;
import com.example.myapplication.bean.ResponseBean;
import com.example.myapplication.constants.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebService {

    private static final String TAG = "Web";
    private static final WebService instance = new WebService();
    Retrofit retrofit;
    NewsMinerInterface service;
    Gson formatter;
    SimpleDateFormat dateFormatter;

    private WebService() {
        formatter = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        retrofit = new Retrofit.Builder().baseUrl(Constants.baseURL)
                .addConverterFactory(GsonConverterFactory.create(formatter)).build();
        service = retrofit.create(NewsMinerInterface.class);
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    }

    public static WebService getInstance() {
        return instance;
    }

    private String getCurrentTime() {
        return dateFormatter.format(new Date(System.currentTimeMillis()));
    }

    public static class NewsPager {
        private final NewsMinerInterface service;
        private final String startDate;
        private final String endDate;
        private final String keywords;
        private int curPage, maxPage;
        private String category;

        public NewsPager() {
            this.endDate = getInstance().getCurrentTime();
            this.startDate = "";
            this.service = getInstance().service;
            this.category = "";
            this.keywords = "";
            curPage = 0;
            maxPage = 1;
        }

        public NewsPager setTotal(int total) {
            this.maxPage = (total + Constants.pageSize - 1) / Constants.pageSize;
            return this;
        }

        public NewsPager setCategory(String category) {
            if (category.equals("全部")) category = "";
            this.category = category;
            return this;
        }

        public Call<ResponseBean> nextPage() {
            return service.getPage(Constants.pageSize, ++curPage, startDate, endDate, keywords, category);
        }

        public boolean hasNextPage() {
            return curPage < maxPage;
        }
    }
}
