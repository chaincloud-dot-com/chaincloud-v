package com.chaincloud.chaincloudv.ui.base;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chaincloud.chaincloudv.ChainCloudVApplication_;
import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.ui.base.dialog.DialogCentered;
import com.chaincloud.chaincloudv.util.ThreadUtil;

import java.util.List;


/**
 * Created by songchenwen on 15/8/3.
 */
public class DialogWithActions extends DialogCentered implements View.OnClickListener,
        DialogInterface.OnDismissListener {
    private static final int ActionTagIndex = R.id.dialog_with_actions_action;
    private View clickedView;
    private List<Action> actions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        v.findViewById(R.id.tv_close).setOnClickListener(this);
        LinearLayout ll = (LinearLayout) v.findViewById(R.id.ll_action_container);
        for (Action a : actions) {
            View item = inflater.inflate(R.layout.list_item_dialog_with_actions, ll, false);
            TextView tvName = (TextView) item.findViewById(R.id.tv_name);
            tvName.setText(a.getName());
            tvName.setTag(ActionTagIndex, a);
            tvName.setOnClickListener(this);
            ll.addView(item);
        }
        return v;
    }

    @Override
    protected int contentView() {
        return R.layout.dialog_with_actions;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnDismissListener(this);
        return dialog;
    }

    public void show(FragmentManager manager) {
        clickedView = null;
        super.show(manager);
    }

    public DialogWithActions setActions(List<Action> actions){
        this.actions = actions;
        return this;
    }

    @Override
    public void onClick(View v) {
        clickedView = v;
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (clickedView != null) {
            Object t = clickedView.getTag(ActionTagIndex);
            if (t != null && t instanceof Action) {
                Runnable r = ((Action) t).getAction();
                if (r != null) {
                    ThreadUtil.getMainThreadHandler().postDelayed(r, 220);
                }
            }
        }
    }

    public static final class Action {
        private String name;
        private Runnable action;

        public Action(String name, Runnable action) {
            setName(name);
            setAction(action);
        }

        public Action(int name, Runnable action) {
            setName(ChainCloudVApplication_.getInstance().getApplicationContext().getString(name));
            setAction(action);
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Runnable getAction() {
            return action;
        }

        public void setAction(Runnable action) {
            this.action = action;
        }

    }

    public static abstract class DialogWithActionsClickListener implements View.OnClickListener {
        private FragmentManager manager;

        public DialogWithActionsClickListener(FragmentManager manager) {
            this.manager = manager;
        }

        @Override
        public void onClick(View v) {
            new DialogWithActions().setActions(getActions()).show(manager);
        }

        protected abstract List<Action> getActions();
    }


}
