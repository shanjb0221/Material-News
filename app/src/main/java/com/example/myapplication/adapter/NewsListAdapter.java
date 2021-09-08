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

import com.example.myapplication.bean.NewsBean;
import com.example.myapplication.databinding.ItemCardBinding;
import com.example.myapplication.databinding.ItemLoaderBinding;

import java.util.List;

public class NewsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_ITEM = 0;
    public static final int TYPE_FOOTER = 1;

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
        if (viewType == TYPE_ITEM)
            return new BindingViewHolder<>(ItemCardBinding.inflate(inflater, parent, false));
        if (viewType == TYPE_FOOTER)
            return new BindingViewHolder<>(ItemLoaderBinding.inflate(inflater, parent, false));
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BindingViewHolder) {
            BindingViewHolder h = (BindingViewHolder) holder;
            if (h.B instanceof ItemCardBinding) {
                ItemCardBinding b = (ItemCardBinding) h.B;
                b.tv.setText(items.get(position).getTitle());
            }
            if (h.B instanceof ItemLoaderBinding) {
                ItemLoaderBinding b = (ItemLoaderBinding) h.B;
                switch (loaderStatus) {
                    case IDLE:
                        b.loaderTextView.setText("上拉加载更多...");
                        break;
                    case LOADING:
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
        return position == items.size() ? TYPE_FOOTER : TYPE_ITEM;
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
