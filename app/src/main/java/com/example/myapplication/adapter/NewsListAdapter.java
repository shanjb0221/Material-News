package com.example.myapplication.adapter;

/* REFERENCES:
 * https://blog.csdn.net/zk1382091/article/details/89462395
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.myapplication.R;
import com.example.myapplication.bean.NewsBean;
import com.example.myapplication.databinding.ItemCardMultiImagesBinding;
import com.example.myapplication.databinding.ItemCardSingleImageBinding;
import com.example.myapplication.databinding.ItemLoaderBinding;
import com.example.myapplication.utils.TimeUtil;

import java.util.List;

public class NewsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_ITEM_SINGLE_IMAGE = 0;
    public static final int TYPE_ITEM_MULTI_IMAGES = 1;
    public static final int TYPE_FOOTER = 2;

    public static final int IDLE = 0;
    public static final int LOADING = 1;
    public static final int NO_MORE = 2;
    private final Context context;
    private final LayoutInflater inflater;
    private int loaderStatus = IDLE;
    private List<NewsBean> items;

    public NewsListAdapter(Context context, LayoutInflater inflater, List<NewsBean> items) {
        this.context = context;
        this.inflater = inflater;
        this.items = items;
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
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BindingViewHolder) {
            BindingViewHolder<?> h = (BindingViewHolder<?>) holder;
            if (h.B instanceof ItemCardSingleImageBinding) {
                ItemCardSingleImageBinding b = (ItemCardSingleImageBinding) h.B;
                NewsBean bean = items.get(position);
                b.title.setText(bean.getTitle());
                b.publisher.setText(bean.getPublisher());
                b.time.setText(TimeUtil.format(bean.getPublishTime()));

                List<String> images = bean.getImages();
                if (images.size() > 0) {
                    Glide.with(b.getRoot())
                            .load(images.get(0))
                            .placeholder(R.drawable.ic_photo_64)
                            .centerCrop()
                            .transition(DrawableTransitionOptions.withCrossFade(100))
                            .into(b.image);
                } else {
                    b.image.setVisibility(View.GONE);
                }
            }
            if (h.B instanceof ItemCardMultiImagesBinding) {
                ItemCardMultiImagesBinding b = (ItemCardMultiImagesBinding) h.B;
                NewsBean bean = items.get(position);
                b.title.setText(bean.getTitle());
                b.publisher.setText(bean.getPublisher());
                b.time.setText(TimeUtil.format(bean.getPublishTime()));

                List<String> images = bean.getImages();
                Glide.with(b.getRoot())
                        .load(images.get(0))
                        .placeholder(R.drawable.ic_photo_64)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade(100))
                        .into(b.image1);
                Glide.with(b.getRoot())
                        .load(images.get(1))
                        .placeholder(R.drawable.ic_photo_64)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade(100))
                        .into(b.image2);
                Glide.with(b.getRoot())
                        .load(images.get(2))
                        .placeholder(R.drawable.ic_photo_64)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade(100))
                        .into(b.image3);

            }
            if (h.B instanceof ItemLoaderBinding) {
                ItemLoaderBinding b = (ItemLoaderBinding) h.B;
                switch (loaderStatus) {
                    case IDLE:
                        b.loaderLayout.setVisibility(View.VISIBLE);
                        b.loaderTextView.setText("上拉加载更多...");
                        b.loaderProgressBar.setVisibility(View.GONE);
                        break;
                    case LOADING:
                        b.loaderLayout.setVisibility(View.VISIBLE);
                        b.loaderProgressBar.setVisibility(View.VISIBLE);
                        b.loaderTextView.setText("加载中...");
                        break;
                    case NO_MORE:
                        b.loaderLayout.setVisibility(View.GONE);
                        break;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == items.size())
            return TYPE_FOOTER;
        return items.get(position).getImages().size() >= 3 ? TYPE_ITEM_MULTI_IMAGES : TYPE_ITEM_SINGLE_IMAGE;
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

}
