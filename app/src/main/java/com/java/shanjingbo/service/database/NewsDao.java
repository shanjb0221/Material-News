package com.java.shanjingbo.service.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;
import com.java.shanjingbo.bean.NewsBean;

import java.util.List;

@Dao
public abstract class NewsDao {
    public NewsDao() {
    }

    @Query("SELECT COUNT(*) FROM news")
    public abstract int countAll();

    @Query("SELECT COUNT(*) FROM news WHERE star = 1")
    public abstract int countStar();

    @Query("SELECT * FROM news")
    public abstract ListenableFuture<List<NewsBean>> getAll();

    @Query("SELECT * FROM news ORDER BY readTime DESC LIMIT :size  OFFSET :offset")
    public abstract ListenableFuture<List<NewsBean>> getAllPage(int size, int offset);

    @Query("SELECT * FROM news WHERE star = 1 ORDER BY readTime DESC LIMIT :size OFFSET :offset")
    public abstract ListenableFuture<List<NewsBean>> getStarPage(int size, int offset);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(NewsBean bean);

    @Query("DELETE FROM news")
    public abstract ListenableFuture<Integer> deleteAll();

    @Query("SELECT * FROM news WHERE newsID = :id LIMIT 1")
    public abstract NewsBean findById(String id);

    public int count(boolean star) {
        return star ? countStar() : countAll();
    }

    public ListenableFuture<List<NewsBean>> getPage(boolean star, int size, int page) {
        if (star) return getStarPage(size, (page - 1) * size);
        return getAllPage(size, (page - 1) * size);
    }
}
