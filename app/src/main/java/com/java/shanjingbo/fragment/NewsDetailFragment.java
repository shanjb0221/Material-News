package com.java.shanjingbo.fragment;

import static androidx.recyclerview.widget.RecyclerView.HORIZONTAL;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.exoplayer2.util.Log;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.java.shanjingbo.R;
import com.java.shanjingbo.adapter.ImagesAdapter;
import com.java.shanjingbo.bean.NewsBean;
import com.java.shanjingbo.databinding.FragmentNewsDetailBinding;
import com.java.shanjingbo.service.DBService;
import com.java.shanjingbo.service.ImageService;
import com.java.shanjingbo.utils.ImageUtil;
import com.java.shanjingbo.utils.TimeUtil;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;

import java.util.List;

public class NewsDetailFragment extends Fragment {

    private static final String TAG = "NewsDetail";

    FragmentNewsDetailBinding B;
    NavController nav;
    GSYVideoOptionBuilder videoBuilder;
    NewsBean bean;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (B == null) {
            B = FragmentNewsDetailBinding.inflate(inflater, container, false);
            nav = Navigation.findNavController(getParentFragment().requireView());
            AppBarConfiguration conf =
                    new AppBarConfiguration.Builder(nav.getGraph()).build();
            NavigationUI.setupWithNavController(
                    B.toolbar, nav, conf);
            videoBuilder = new GSYVideoOptionBuilder();
        }
        bean = NewsDetailFragmentArgs.fromBundle(getArguments()).getNews();
        int duration = NewsDetailFragmentArgs.fromBundle(getArguments()).getDuration();

        saveToDB(bean);

        B.titleText.setText(bean.getTitle());
        B.publisher.setText(bean.getPublisher());
        B.time.setText(TimeUtil.format(bean.getPublishTime()));
        B.content.setText(bean.getContent());

        B.singleImage.setVisibility(View.GONE);
        B.multiImages.setVisibility(View.GONE);
        B.multiImagesTip.setVisibility(View.GONE);
        B.video.setVisibility(View.GONE);

        if (!bean.getVideo().equals("")) {
            B.video.setVisibility(View.VISIBLE);
            initVideo(bean.getTitle(), bean.getVideo(), duration);
        } else {
            List<String> images = bean.getImages();
            if (images.size() == 1) {
                B.singleImage.setVisibility(View.VISIBLE);
                ImageService.load(this, images.get(0), B.singleImage);
            } else if (images.size() > 1) {
                B.multiImages.setVisibility(View.VISIBLE);
                B.multiImagesTip.setVisibility(View.VISIBLE);
                B.multiImages.setLayoutManager(new LinearLayoutManager(getContext(), HORIZONTAL, false));
                B.multiImages.setAdapter(new ImagesAdapter(getContext(), images));
            }
        }

        setupToolBar();
        return B.getRoot();
    }

    private void setupToolBar() {
        if (bean.getUrl().equals(""))
            B.toolbar.getMenu().findItem(R.id.menu_open).setVisible(false);
        B.toolbar.getMenu().findItem(R.id.menu_star).setIcon(bean.getStar() ?
                R.drawable.ic_star_24 : R.drawable.ic_star_outline_24);
        B.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_star) {
                bean.toggleStar();
                saveToDB(bean);
                if (bean.getStar()) {
                    Snackbar.make(B.coordinator, "收藏成功", BaseTransientBottomBar.LENGTH_LONG).show();
                    item.setIcon(R.drawable.ic_star_24);
                } else item.setIcon(R.drawable.ic_star_outline_24);
            }
            if (id == R.id.menu_open) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(bean.getUrl()));
                requireContext().startActivity(intent);
            }
            return false;
        });

    }

    private void saveToDB(NewsBean bean) {
        bean.setReadTime(TimeUtil.getCurrentTime());
        DBService.getInstance(requireContext()).add(bean);
    }

    private void initVideo(String title, String video, int savedPosition) {
        OrientationUtils utils = new OrientationUtils(getActivity(), B.video);
        //初始化不打开外部的旋转
        utils.setEnable(false);

        ImageView image = new ImageView(requireContext());
        Handler handler = new Handler();
        new Thread() {
            @Override
            public void run() {
                super.run();
                Bitmap map = ImageUtil.createVideoThumbnail(video);
                handler.post(() -> {
                    image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    image.setImageBitmap(map);
                });
            }
        }.start();
        videoBuilder.setThumbImageView(image)
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setRotateWithSystem(true)
                .setLockLand(true)
                .setAutoFullWithSize(true)
                .setShowFullAnimation(false)
                .setNeedLockFull(true)
                .setUrl(video)
                .setCacheWithPlay(false)
                .setVideoTitle(title)
                .setRotateWithSystem(true)
                .setVideoAllCallBack(new GSYSampleCallBack() {
                    @Override
                    public void onPrepared(String url, Object... objects) {
                        super.onPrepared(url, objects);
                        //开始播放了才能旋转和全屏
                        utils.setEnable(B.video.isRotateWithSystem());
                        if (savedPosition != -1) {
                            B.video.seekTo(savedPosition);
                            Log.e(TAG, "play video: pos = " + savedPosition);
                        }
                    }

                    @Override
                    public void onQuitFullscreen(String url, Object... objects) {
                        super.onQuitFullscreen(url, objects);
                        utils.backToProtVideo();
                    }
                })
                .setLockClickListener((view, lock) -> {
                    utils.setEnable(!lock);     //配合下方的onConfigurationChanged
                })
                .build(B.video);
        B.video.getFullscreenButton().setOnClickListener(v -> {
            utils.resolveByClick();
            B.video.startWindowFullscreen(requireContext(), true, true);
        });
        B.video.getBackButton().setVisibility(View.GONE);
        B.video.getTitleTextView().setVisibility(View.GONE);
        B.video.getBackButton().setOnClickListener(view -> utils.backToProtVideo());

        if (savedPosition != -1) B.video.startPlayLogic();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        B.video.release();
        B = null;
        nav = null;
    }
}