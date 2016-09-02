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

package com.chaincloud.chaincloudv.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.chaincloud.chaincloudv.ChainCloudVApplication_;

import java.text.DecimalFormat;

public class UIUtil {
    public static final int SwitchToAbcActionId = 18203;
    public static final int SwitchTo123ActionId = 18204;
    private static final DecimalFormat moneyFormater = new DecimalFormat("0.00");

    public static final int dip2pix(float dip) {
        final float scale = ChainCloudVApplication_.getInstance().getApplicationContext().getResources
                ().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    public static int getScreenWidth() {
        return ChainCloudVApplication_.getInstance().getApplicationContext().getResources()
                .getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return ChainCloudVApplication_.getInstance().getApplicationContext().getResources()
                .getDisplayMetrics().heightPixels;
    }

    public static Bitmap getBitmapFromView(View v, int width, int height) {
        v.measure(width, height);
        v.layout(0, 0, width, height);
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        v.draw(new Canvas(bmp));
        return bmp;
    }

    public static Bitmap getBitmapFromView(View v) {
        Bitmap bmp = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        v.draw(new Canvas(bmp));
        return bmp;
    }

    public static final int getStatusBarHeight(Window window) {
        Rect frame = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }

    public static void gotoBrower(Context activity, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent
                .FLAG_ACTIVITY_NEW_TASK);
        try {
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            if (activity instanceof Activity) {
                Toast.makeText(activity, "error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static String formatMoney(double money) {
        return moneyFormater.format(money);
    }

    public static String makeFragmentName(int paramInt1, int paramInt2) {
        return "android:switcher:" + paramInt1 + ":" + paramInt2;
    }
}
