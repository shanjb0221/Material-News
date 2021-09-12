package com.java.shanjingbo.service;

import com.java.shanjingbo.bean.ResponseBean;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/*
 * https://api2.newsminer.net/
 *      svc/news/queryNewsList
 *      ?   size       = 15
 *      &   startDate  = 2021-08-20
 *      &   endDate    = 2021-08-30
 *      &   words      = 拜登
 *      &   categories = 科技
 *      &   page       = 1
 */

public interface NewsMinerInterface {
    @GET("svc/news/queryNewsList")
    Call<ResponseBean> getPage(@Query("size") int size,
                               @Query("page") int page,
                               @Query("startDate") String startDate,
                               @Query("endDate") String endDate,
                               @Query("words") String words,
                               @Query("categories") String channel);
}
