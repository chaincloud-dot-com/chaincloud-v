/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chaincloud.chaincloudv.ui.base;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.util.UIUtil;


public class TabButton extends FrameLayout {
    private ImageView ivIcon;
    private ToggleButton tbtnBottom;

    private TextView tvText;
    private boolean ellipsized = true;

    private int uncheckedIcon;
    private int checkedIcon;


    public TabButton(Context context) {
        super(context);
        init();
    }

    public TabButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TabButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LinearLayout llIcon = new LinearLayout(getContext());
        llIcon.setOrientation(LinearLayout.HORIZONTAL);
        addView(llIcon, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT,
                Gravity.CENTER));
        ivIcon = new ImageView(getContext());
        ivIcon.setPadding(0, 0, 0, UIUtil.dip2pix(0.75f));
        LinearLayout.LayoutParams lpIcon = new LinearLayout.LayoutParams(LayoutParams
                .MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lpIcon.topMargin = UIUtil.dip2pix(3);
        lpIcon.bottomMargin = UIUtil.dip2pix(3);
        lpIcon.gravity = Gravity.CENTER;
        ivIcon.setScaleType(ScaleType.CENTER_INSIDE);
        llIcon.addView(ivIcon, lpIcon);
        tvText = new TextView(getContext());
        tvText.setTextColor(Color.WHITE);
        tvText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tvText.setTypeface(null, Typeface.BOLD);
        tvText.setShadowLayer(0.5f, 1, -1, Color.argb(100, 0, 0, 0));
        tvText.setPadding(0, 0, 0, UIUtil.dip2pix(0.75f));
        tvText.setGravity(Gravity.CENTER_HORIZONTAL);
        tvText.setLines(1);
        tvText.setEllipsize(TruncateAt.END);
        llIcon.addView(tvText);
        LinearLayout.LayoutParams lpText = (LinearLayout.LayoutParams) tvText.getLayoutParams();
        lpText.weight = 1;
        lpText.width = 0;
        lpText.gravity = Gravity.CENTER_VERTICAL;
        LayoutParams lpBottom = new LayoutParams(LayoutParams.MATCH_PARENT, UIUtil.dip2pix(2.67f));
        lpBottom.bottomMargin = UIUtil.dip2pix(0.75f);
        lpBottom.gravity = Gravity.BOTTOM;
        tbtnBottom = new ToggleButton(getContext());
        tbtnBottom.setTextOff("");
        tbtnBottom.setTextOn("");
        tbtnBottom.setText("");
        tbtnBottom.setBackgroundResource(R.drawable.tab_bottom_background_selector);
        tbtnBottom.setFocusable(false);
        tbtnBottom.setClickable(false);
        addView(tbtnBottom, lpBottom);

        tvText.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                                                                   @Override
                                                                   public void onGlobalLayout() {
                                                                       if (!ellipsized) {
                                                                           return;
                                                                       }
                                                                       ellipsized = false;
                                                                       Layout l = tvText.getLayout();
                                                                       if (l != null) {
                                                                           int lines = l.getLineCount();
                                                                           if (lines > 0) {
                                                                               if (l.getEllipsisCount(lines - 1) > 0) {
                                                                                   ellipsized = true;
                                                                               }
                                                                           }
                                                                       }
                                                                   }
                                                               }
        );
    }

    public void setIconResource(int unchecked, int checked) {
        this.uncheckedIcon = unchecked;
        this.checkedIcon = checked;
        configureIcon();
    }

    private void configureIcon() {
        if (isChecked()) {
            ivIcon.setImageResource(checkedIcon);
            tvText.setTextColor(Color.WHITE);
        } else {
            ivIcon.setImageResource(uncheckedIcon);
            tvText.setTextColor(Color.parseColor("#a2b3c2"));
        }
    }

    public void setText(String text) {
        ellipsized = true;
        tvText.setText(text);
    }

    public void setChecked(boolean checked) {
        tbtnBottom.setChecked(checked);
        configureIcon();
    }

    public boolean isChecked() {
        return tbtnBottom.isChecked();
    }
}
