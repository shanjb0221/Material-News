package com.example.myapplication.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageUtil {
    public static List<String> split(String imgStr) {
        List<String> res = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\[([^\\[]*)\\]");
        Matcher matcher = pattern.matcher(imgStr);
        if (matcher.find()) {
            do res.addAll(split(matcher.group(1)));
            while (matcher.find());
        } else {
            imgStr = imgStr + ",";
            pattern = Pattern.compile("(https?://.+?),");
            matcher = pattern.matcher(imgStr);
            while (matcher.find())
                res.add(matcher.group(1));
        }

        return res;
    }
}
