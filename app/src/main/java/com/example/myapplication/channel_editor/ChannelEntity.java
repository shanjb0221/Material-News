package com.example.myapplication.channel_editor;

/**
 * 频道实体类
 * tangxianfeng 2021.7.19
 */
public class ChannelEntity {

    private final boolean removable;
    private long id;
    private String name;

    public ChannelEntity(String name) {
        this(name, true);
    }

    public ChannelEntity(String name, boolean removable) {
        this.name = name;
        this.removable = removable;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getRemovable() {
        return removable;
    }
}
