package com.chaincloud.chaincloudv.ui.base.dialog;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.qr.Qr;
import com.chaincloud.chaincloudv.util.UIUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by songchenwen on 15/8/21.
 */
@EFragment
public class DialogSimpleQr extends DialogCentered {

    @ViewById
    View flContainer, pb;
    @ViewById
    ImageView iv;

    @FragmentArg
    String content;

    @AfterViews
    void init() {
        setCancelable(true);
        int size = Math.min(UIUtil.getScreenHeight(), UIUtil.getScreenWidth()) - UIUtil.dip2pix(60);
        ViewGroup.LayoutParams lp = flContainer.getLayoutParams();
        lp.width = size;
        lp.height = size;
        iv.setVisibility(View.INVISIBLE);
        pb.setVisibility(View.VISIBLE);
        generateQr();
    }

    @Background
    void generateQr() {
        showQr(Qr.bitmap(content, flContainer.getLayoutParams().width));
    }

    @UiThread
    void showQr(Bitmap qr) {
        iv.setImageBitmap(qr);
        iv.setVisibility(View.VISIBLE);
        pb.setVisibility(View.GONE);
    }

    @Click
    void flContainerClicked() {
        dismiss();
    }

    @Override
    protected int contentView() {
        return R.layout.dialog_simple_qr;
    }
}
