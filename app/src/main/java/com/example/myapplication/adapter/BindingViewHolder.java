package com.example.myapplication.adapter;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

public class BindingViewHolder<BindingType extends ViewBinding> extends RecyclerView.ViewHolder {
    public final BindingType B;

    public BindingViewHolder(BindingType B) {
        super(B.getRoot());
        this.B = B;
    }
}