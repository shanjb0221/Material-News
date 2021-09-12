package com.java.shanjingbo.channel_editor;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.java.shanjingbo.databinding.ChannelEditorBinding;

import java.util.List;

public class ChannelEditor {
    private final int spanCount;
    private final ChannelEditAdapter adapter;
    private final BottomSheetBehavior<LinearLayout> behavior;

    public ChannelEditor(Context context, ChannelEditorBinding binding, List<ChannelEntity> mine, List<ChannelEntity> other, int spanCount) {
        this.spanCount = spanCount;

        GridLayoutManager manager = new GridLayoutManager(context, this.spanCount);
        binding.editorView.setLayoutManager(manager);
        ItemDragHelperCallback callback = new ItemDragHelperCallback();
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(binding.editorView);

        this.adapter = new ChannelEditAdapter(context, binding.editorView, helper, mine, other, manager);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (adapter.getItemViewType(position) & ChannelEditAdapter.ITEM) == 1 ? 1 : ChannelEditor.this.spanCount;
            }
        });
        binding.editorView.setAdapter(adapter);

        this.behavior = BottomSheetBehavior.from(binding.bottomSheet);
        binding.closeButton.setOnClickListener(view -> {
            adapter.resetEditingMode();
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
    }

    public ChannelEditAdapter getAdapter() {
        return adapter;
    }

    public BottomSheetBehavior<LinearLayout> getBehavior() {
        return behavior;
    }

}
