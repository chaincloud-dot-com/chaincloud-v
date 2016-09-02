package com.chaincloud.chaincloudv.ui.base.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.chaincloud.chaincloudv.R;

/**
 * Created by songchenwen on 15/8/3.
 */

public abstract class DialogCentered extends DialogFragment {
    FrameLayout container;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.DialogCentered);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        container = new FrameLayout(inflater.getContext());
        container.setBackgroundResource(R.drawable.light_center_dialog_background);
        container.removeAllViews();
        return inflater.inflate(contentView(), container, true);
    }

    abstract protected int contentView();

    public void show(FragmentManager manager) {
        FragmentTransaction tx = manager.beginTransaction();
        super.show(tx, getClass().getSimpleName());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(isCancelable());
        Window window = dialog.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams
                .FLAG_FORCE_NOT_FULLSCREEN);
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setWindowAnimations(windowAnimationStyle());
        window.getAttributes().dimAmount = 0.5f;
        return dialog;
    }

    int windowAnimationStyle() {
        return R.style.DialogCentered_Animation;
    }


}
