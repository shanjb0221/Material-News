/**
 * Copyright 2021 bejson.com
 */
package com.example.myapplication.bean;

import com.example.myapplication.utils.ImageUtil;
import com.google.gson.annotations.Expose;

import java.util.Date;
import java.util.List;

/**
 * Auto-generated: 2021-09-08 23:32:28
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class NewsBean {

    private String image;
    @Expose(deserialize = false)
    private List<String> images;
    private Date publishTime;
    private String language;
    private String video;
    private String title;
    private String content;
    private String url;
    private String newsID;
    private String publisher;
    private String category;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNewsID() {
        return newsID;
    }

    public void setNewsID(String newsID) {
        this.newsID = newsID;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getImages() {
        if (images == null) {
            images = ImageUtil.split(image);
        }
        return images;
    }
}