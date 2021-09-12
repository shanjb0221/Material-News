package com.java.shanjingbo.service.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.java.shanjingbo.bean.NewsBean;

@Database(entities = {NewsBean.class}, version = 1, exportSchema = false)
public abstract class NewsDB extends RoomDatabase {
    private static final String DB_NAME = "NewsDB.db";
    private static volatile NewsDB instance;

    public static synchronized NewsDB getInstance(Context context) {
        if (instance == null) instance = create(context);
        return instance;
    }

    private static NewsDB create(final Context context) {
        return Room.databaseBuilder(context, NewsDB.class, DB_NAME).allowMainThreadQueries().build();
    }

    public abstract NewsDao getNewsDao();
}


