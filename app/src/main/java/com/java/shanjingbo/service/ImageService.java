package com.java.shanjingbo.service;

import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.java.shanjingbo.R;

public class ImageService {
    private static final RequestOptions options = new RequestOptions()
            .placeholder(R.drawable.ic_image_24)
            .error(R.drawable.ic_broken_image_24)
            .fallback(R.drawable.ic_broken_image_24)
            .centerCrop();

    private static final DrawableCrossFadeFactory factory = new DrawableCrossFadeFactory.Builder()
            .setCrossFadeEnabled(true).build();

    public static void load(View view, String url, ImageView dest) {
        Glide.with(view).load(url).transition(DrawableTransitionOptions.withCrossFade(factory))
                .apply(options).into(dest);
    }

    public static void load(Fragment fragment, String url, ImageView view) {
        Glide.with(fragment).load(url).transition(DrawableTransitionOptions.withCrossFade(factory))
                .apply(options).into(view);
    }
}