package com.chaincloud.chaincloudv.util;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;

import com.chaincloud.chaincloudv.ChainCloudVApplication_;


/**
 * Created by songchenwen on 15/8/21.
 */
public class ClipboardUtil {
    @SuppressLint("NewApi")
    public static void copyString(String text) {
        if (android.os.Build.VERSION.SDK_INT > 10) {
            android.content.ClipboardManager clip = (android.content.ClipboardManager)
                    ChainCloudVApplication_.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
            clip.setPrimaryClip(ClipData.newPlainText(text, text));
        } else {
            android.text.ClipboardManager clipM = (android.text.ClipboardManager)
                    ChainCloudVApplication_.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
            clipM.setText(text);
        }
    }
}
