package com.java.shanjingbo.adapter;

/* REFERENCES:
 * https://blog.csdn.net/zk1382091/article/details/89462395
 */

/* TODO: consider migrating to ExoPlayer */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.java.shanjingbo.bean.NewsBean;
import com.java.shanjingbo.databinding.ItemCardMultiImagesBinding;
import com.java.shanjingbo.databinding.ItemCardSingleImageBinding;
import com.java.shanjingbo.databinding.ItemCardVideoBinding;
import com.java.shanjingbo.databinding.ItemLoaderBinding;
import com.java.shanjingbo.service.ImageService;
import com.java.shanjingbo.utils.ImageUtil;
import com.java.shanjingbo.utils.TimeUtil;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;

import java.util.List;
import java.util.concurrent.Executor;

public class NewsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_ITEM_SINGLE_IMAGE = 0;
    public static final int TYPE_ITEM_MULTI_IMAGES = 1;
    public static final int TYPE_ITEM_VIDEO = 2;
    public static final int TYPE_FOOTER = 3;
    public static final int IDLE = 0;
    public static final int LOADING = 1;
    public static final int NO_MORE = 2;
    private static final String TAG = "NewsList";
    private final Context context;
    private final LayoutInflater inflater;
    private final OnNewsClickListener listener;
    private final Executor executor;
    DrawableCrossFadeFactory factory;
    private int loaderStatus = IDLE;
    private List<NewsBean> items;

    public NewsListAdapter(Context context, LayoutInflater inflater, List<NewsBean> items, OnNewsClickListener listener) {
        this.context = context;
        this.inflater = inflater;
        this.items = items;
        this.listener = listener;
        Handler handler = new Handler();
        executor = handler::post;
        factory = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
    }

    public int getLoaderStatus() {
        return loaderStatus;
    }

    public void setLoaderStatus(int status) {
        loaderStatus = status;
        notifyItemChanged(items.size());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM_SINGLE_IMAGE)
            return new BindingViewHolder<>(ItemCardSingleImageBinding.inflate(inflater, parent, false));
        if (viewType == TYPE_ITEM_MULTI_IMAGES)
            return new BindingViewHolder<>(ItemCardMultiImagesBinding.inflate(inflater, parent, false));
        if (viewType == TYPE_FOOTER)
            return new BindingViewHolder<>(ItemLoaderBinding.inflate(inflater, parent, false));
        if (viewType == TYPE_ITEM_VIDEO)
            return new ItemVideoViewHolder(ItemCardVideoBinding.inflate(inflater, parent, false));
        assert false;
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        assert holder instanceof BindingViewHolder;
        BindingViewHolder<?> h = (BindingViewHolder<?>) holder;
        if (h.B instanceof ItemCardSingleImageBinding) {
            ItemCardSingleImageBinding b = (ItemCardSingleImageBinding) h.B;
            NewsBean bean = items.get(position);

            b.title.setText(bean.getTitle());
            b.publisher.setText(bean.getPublisher());
            b.time.setText(TimeUtil.format(bean.getPublishTime()));
            b.star.setVisibility(bean.getStar() ? View.VISIBLE : View.GONE);
            b.title.setAlpha(bean.getReadTime() == null ? 1F : 0.5F);

            List<String> images = bean.getImages();
            if (images.size() > 0) {
                b.singleCardImage.setVisibility(View.VISIBLE);
                ImageService.load(b.getRoot(), images.get(0), b.singleCardImage);
            } else {
                b.singleCardImage.setVisibility(View.GONE);
            }
            b.getRoot().setOnClickListener(view -> {
                b.title.setAlpha(0.5F);
                listener.onImageItemClick(bean);
            });
        }
        if (h.B instanceof ItemCardMultiImagesBinding) {
            ItemCardMultiImagesBinding b = (ItemCardMultiImagesBinding) h.B;
            NewsBean bean = items.get(position);

            b.title.setText(bean.getTitle());
            b.publisher.setText(bean.getPublisher());
            b.time.setText(TimeUtil.format(bean.getPublishTime()));
            b.star.setVisibility(bean.getStar() ? View.VISIBLE : View.GONE);
            b.title.setAlpha(bean.getReadTime() == null ? 1F : 0.5F);

            List<String> images = bean.getImages();
            ImageService.load(b.getRoot(), images.get(0), b.image1);
            ImageService.load(b.getRoot(), images.get(1), b.image2);
            ImageService.load(b.getRoot(), images.get(2), b.image3);
            b.getRoot().setOnClickListener(view -> {
                b.title.setAlpha(0.5F);
                listener.onImageItemClick(bean);
            });
        }
        if (h.B instanceof ItemLoaderBinding) {
            ItemLoaderBinding b = (ItemLoaderBinding) h.B;
            switch (loaderStatus) {
                case IDLE:
                    b.loaderProgressBar.setVisibility(View.GONE);
                    b.loaderTextView.setText("上拉加载更多");
                    break;
                case LOADING:
                    b.loaderProgressBar.setVisibility(View.VISIBLE);
                    b.loaderTextView.setText("加载中...");
                    break;
                case NO_MORE:
                    b.loaderProgressBar.setVisibility(View.GONE);
                    b.loaderTextView.setText("暂无更多");
                    break;
            }
        }
        if (h instanceof ItemVideoViewHolder) {
            ItemVideoViewHolder H = (ItemVideoViewHolder) h;
            ItemCardVideoBinding b = H.B;
            NewsBean bean = items.get(position);

            b.titleText.setText(bean.getTitle());
            b.publisher.setText(bean.getPublisher());
            b.time.setText(TimeUtil.format(bean.getPublishTime()));
            b.star.setVisibility(bean.getStar() ? View.VISIBLE : View.GONE);
            b.titleText.setAlpha(bean.getReadTime() == null ? 1F : 0.5F);

            H.builder.setIsTouchWiget(false)
                    .setUrl(bean.getVideo())
                    .setVideoTitle(bean.getTitle())
                    .setCacheWithPlay(false)
                    .setRotateViewAuto(false)
                    .setLockLand(true)
                    .setPlayTag(TAG)
                    .setShowFullAnimation(true)
                    .setNeedLockFull(true)
                    .setPlayPosition(position)
                    .setVideoAllCallBack(new GSYSampleCallBack() {
                        @Override
                        public void onPrepared(String url, Object... objects) {
                            super.onPrepared(url, objects);
                            if (!b.video.isIfCurrentIsFullscreen())
                                GSYVideoManager.instance().setNeedMute(true);
                        }
                    }).build(b.video);

            ImageView image = new ImageView(context);
            b.video.setThumbImageView(image);
            b.video.getTitleTextView().setVisibility(View.GONE);
            b.video.getBackButton().setVisibility(View.GONE);
            b.video.getFullscreenButton().setVisibility(View.GONE);

            Handler handler = new Handler();
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    Bitmap map = ImageUtil.createVideoThumbnail(bean.getVideo());
                    handler.post(() -> {
                        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        image.setImageBitmap(map);
                    });
                }
            }.start();

            b.getRoot().setOnClickListener(view -> {
                b.titleText.setAlpha(0.5F);
                listener.onVideoItemClick(bean,
                        b.video.isInPlayingState() ? b.video.getCurrentPositionWhenPlaying() : -1);
            });
        }

    }

    @Override
    public int getItemCount() {
        return items.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == items.size()) return TYPE_FOOTER;
        NewsBean bean = items.get(position);
        if (!bean.getVideo().equals("")) return TYPE_ITEM_VIDEO;
        return bean.getImages().size() >= 3 ? TYPE_ITEM_MULTI_IMAGES : TYPE_ITEM_SINGLE_IMAGE;
    }

    public void appendItemsToBack(List<NewsBean> items) {
        int position = this.items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(position, items.size());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void replaceItems(List<NewsBean> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public interface OnNewsClickListener {
        void onImageItemClick(NewsBean news);

        void onVideoItemClick(NewsBean news, int duration);
    }

    private static class ItemVideoViewHolder extends BindingViewHolder<ItemCardVideoBinding> {
        GSYVideoOptionBuilder builder;

        public ItemVideoViewHolder(ItemCardVideoBinding B) {
            super(B);
            builder = new GSYVideoOptionBuilder();
        }
    }

}
