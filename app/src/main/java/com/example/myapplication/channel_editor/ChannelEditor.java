package com.example.myapplication.channel_editor;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.example.myapplication.databinding.ChannelEditorBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.List;

public class ChannelEditor {
    private final Context context;
    private final ChannelEditorBinding B;
    private final int spanCount;
    private final ChannelEditAdapter adapter;
    private final BottomSheetBehavior<LinearLayout> behavior;

    public ChannelEditor(Context context, ChannelEditorBinding binding, List<ChannelEntity> mine, List<ChannelEntity> other, int spanCount) {
        this.context = context;
        this.B = binding;
        this.spanCount = spanCount;

        GridLayoutManager manager = new GridLayoutManager(context, this.spanCount);
        B.editorView.setLayoutManager(manager);
        ItemDragHelperCallback callback = new ItemDragHelperCallback();
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(B.editorView);

        this.adapter = new ChannelEditAdapter(context, B.editorView, helper, mine, other, manager);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (adapter.getItemViewType(position) & ChannelEditAdapter.ITEM) == 1 ? 1 : ChannelEditor.this.spanCount;
            }
        });
        B.editorView.setAdapter(adapter);

        this.behavior = BottomSheetBehavior.from(B.bottomSheet);
        B.closeButton.setOnClickListener(view -> behavior.setState(BottomSheetBehavior.STATE_COLLAPSED));
    }

    public ChannelEditAdapter getAdapter() {
        return adapter;
    }

    public BottomSheetBehavior<LinearLayout> getBehavior() {
        return behavior;
    }

}
