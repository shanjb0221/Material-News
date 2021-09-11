package com.example.myapplication.constants;

import com.example.myapplication.channel_editor.ChannelEntity;

import java.util.Arrays;
import java.util.List;

public class Constants {
    public final static List<ChannelEntity> allChannels = Arrays.asList(
            new ChannelEntity("全部", true),
            new ChannelEntity("体育"),
            new ChannelEntity("其他"),
            new ChannelEntity("科技"),
            new ChannelEntity("汽车"),
            new ChannelEntity("母婴"),
            new ChannelEntity("新闻"),
            new ChannelEntity("娱乐"),
            new ChannelEntity("其它"),
            new ChannelEntity("文化"),
            new ChannelEntity("财经"),
            new ChannelEntity("军事"),
            new ChannelEntity("健康"),
            new ChannelEntity("社会"),
            new ChannelEntity("教育"),
            new ChannelEntity("国内"));

    public final static String baseURL = "https://api2.newsminer.net/";
    public final static int pageSize = 25;
}
