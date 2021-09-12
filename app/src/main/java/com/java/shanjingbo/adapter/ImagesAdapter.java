package com.java.shanjingbo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.java.shanjingbo.databinding.ItemImageBinding;
import com.java.shanjingbo.service.ImageService;

import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<String> images;
    private final LayoutInflater inflater;

    public ImagesAdapter(Context context, List<String> images) {
        this.images = images;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingViewHolder<>(ItemImageBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        assert holder instanceof BindingViewHolder;
        BindingViewHolder<?> h = (BindingViewHolder<?>) holder;
        assert h.B instanceof ItemImageBinding;
        ItemImageBinding b = (ItemImageBinding) h.B;
        ImageService.load(b.getRoot(), images.get(position), b.itemImage);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}
