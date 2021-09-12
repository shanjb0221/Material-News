package com.java.shanjingbo.bean;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.google.gson.annotations.Expose;
import com.java.shanjingbo.utils.ImageUtil;
import com.java.shanjingbo.utils.TimeUtil;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Entity(tableName = "news", indices = {
        @Index(value = "newsID", unique = true),
        @Index(value = "readTime"),
        @Index(value = "star")
})
public class NewsBean implements Serializable {

    @NonNull
    @PrimaryKey
    private String newsID = "";

    private String image;
    @TypeConverters(DateConverter.class)
    private Date publishTime;
    private String language;
    private String video;
    private String title;
    private String content;
    private String url;
    private String publisher;
    private String category;

    @Expose(deserialize = false)
    private boolean star = false;
    @TypeConverters(DateConverter.class)
    @Expose(deserialize = false)
    private Date readTime;

    @Ignore
    @Expose(deserialize = false)
    private List<String> images;

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

    @NonNull
    public String getNewsID() {
        return newsID;
    }

    public void setNewsID(@NonNull String newsID) {
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

    public boolean getStar() {
        return star;
    }

    public void setStar(boolean star) {
        this.star = star;
    }

    public Date getReadTime() {
        return readTime;
    }

    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    public void toggleStar() {
        star = !star;
    }

    public static class DateConverter {
        @TypeConverter
        public String objectToString(Date date) {
            if (date == null) return "";
            return TimeUtil.formatter.format(date);
        }

        @TypeConverter
        public Date stringToObject(String str) {
            try {
                return TimeUtil.formatter.parse(str);
            } catch (ParseException e) {
                return null;
            }
        }
    }
}