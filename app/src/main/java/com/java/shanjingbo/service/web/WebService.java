package com.java.shanjingbo.service.web;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.java.shanjingbo.constants.Constants;

import java.util.concurrent.Executors;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebService {
    private static final String TAG = "Web";
    private static final WebService instance = new WebService();
    final Retrofit retrofit;
    final WebInterface service;
    final Gson formatter;
    final ListeningExecutorService executorService;

    private WebService() {
        formatter = new GsonBuilder()
                .registerTypeAdapter(String.class, (JsonDeserializer<String>) (json, typeOfT, context) -> {
                    try {
                        return json.getAsString();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        return json.getAsJsonArray().toString();
                    }
                })
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
        retrofit = new Retrofit.Builder().baseUrl(Constants.baseURL)
                .addConverterFactory(GsonConverterFactory.create(formatter)).build();
        service = retrofit.create(WebInterface.class);
        executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    }

    public static WebService getInstance() {
        return instance;
    }
}
