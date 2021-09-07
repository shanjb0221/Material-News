package com.example.myapplication.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentEditChannelBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EditChannelFragment extends BottomSheetDialogFragment {

    private FragmentEditChannelBinding B;
    public static final String TAG = "fEditChannel";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL,R.style.TransparentBottomSheetStyle);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (B == null) {
            B = FragmentEditChannelBinding.inflate(inflater, container, false);
        }
        return B.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        B = null;
    }
}