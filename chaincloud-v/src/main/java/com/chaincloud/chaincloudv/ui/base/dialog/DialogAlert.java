package com.chaincloud.chaincloudv.ui.base.dialog;

import android.content.DialogInterface;
import android.widget.TextView;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.util.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

/**
 * Created by songchenwen on 15/9/29.
 */
@EFragment
public class DialogAlert extends DialogCentered {
    @FragmentArg
    String msg;

    @FragmentArg
    int msgRes;

    @FragmentArg
    String ok;

    @ViewById
    TextView tvMessage;

    @ViewById
    TextView btnOk;

    private Runnable okRunnable;

    private Runnable cancelRunnable;

    private Runnable dismissRunnable;

    @AfterViews
    void showMsg() {
        if (Utils.isEmpty(msg)) {
            tvMessage.setText(msgRes);
        } else {
            tvMessage.setText(msg);
        }

        if (ok != null) {
            btnOk.setText(ok);
        }
    }

    @Click
    void btnOkClicked() {
        dismissRunnable = okRunnable;
        dismiss();
    }

    @Click
    void btnCancelClicked() {
        dismissRunnable = cancelRunnable;
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dismissRunnable != null) {
            dismissRunnable.run();
        }
    }

    public DialogAlert setRunnable(Runnable run) {
        okRunnable = run;
        return this;
    }

    public DialogAlert setCancelRunnable(Runnable run) {
        cancelRunnable = run;
        dismissRunnable = cancelRunnable;
        return this;
    }

    @Override
    protected int contentView() {
        return R.layout.dialog_alert;
    }
}
