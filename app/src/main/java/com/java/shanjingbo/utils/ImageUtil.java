package com.java.shanjingbo.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageUtil {
    public static List<String> split(String imgStr) {
        List<String> res = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\[([^\\[]*)]");
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

    public static Bitmap createVideoThumbnail(String url) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(url, new HashMap<>());
            bitmap = retriever.getFrameAtTime(5000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            // Assume this is a corrupt video file
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                // Ignore failures while cleaning up.
            }
        }
        return bitmap;
    }

}
