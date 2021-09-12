package com.java.shanjingbo.channel_editor;

/**
 * 频道实体类
 * tangxianfeng 2021.7.19
 */
public class ChannelEntity {

    private final boolean fixed;
    private String name;

    public ChannelEntity(String name) {
        this(name, false);
    }

    public ChannelEntity(String name, boolean fixed) {
        this.name = name;
        this.fixed = fixed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFixed() {
        return fixed;
    }
}
