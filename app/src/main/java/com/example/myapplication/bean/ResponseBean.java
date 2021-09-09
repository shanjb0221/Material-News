/**
 * Copyright 2021 bejson.com
 */
package com.example.myapplication.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Auto-generated: 2021-09-08 23:32:28
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class ResponseBean {

    private String pageSize;
    private int total;
    @SerializedName("data")
    private List<NewsBean> data;
    private String currentPage;

    public String getPageSize() {
        return pageSize;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<NewsBean> getData() {
        return data;
    }

    public void setData(List<NewsBean> data) {
        this.data = data;
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }

    @Override
    public String toString() {
        return "ResponseBean{" +
                "pageSize='" + pageSize + '\'' +
                ", total=" + total +
                ", data=" + data +
                ", currentPage='" + currentPage + '\'' +
                '}';
    }
}