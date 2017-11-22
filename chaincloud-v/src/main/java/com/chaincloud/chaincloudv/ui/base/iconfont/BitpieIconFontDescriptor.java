package com.chaincloud.chaincloudv.ui.base.iconfont;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconFontDescriptor;

/**
 * Package: com.bitpie
 * Creator:  wlong.yi@gmail.com
 * Date: 2017/7/6
 */

public class BitpieIconFontDescriptor implements IconFontDescriptor {

    @Override
    public String ttfFileName() {
        return "bitpie_font.ttf";
    }

    @Override
    public Icon[] characters() {
        return BitpieIcon.values();
    }
}