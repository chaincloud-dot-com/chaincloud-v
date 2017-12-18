package com.chaincloud.chaincloudv.ui.base.iconfont;

import com.joanzapata.iconify.Icon;

/**
 * Package: com.bitpie
 * Creator:  wlong.yi@gmail.com
 * Date: 2017/7/6
 */

public enum  BitpieIcon implements Icon {
    fa_ltc('\ue601'),
    fa_etc('\ue60e'),
    fa_eth('\ue60b'),
    fa_token('\ue60b'),
    fa_doge('\ue602'),
    fa_bcc('\ue603'),
    fa_zec('\ue60d'),
    fa_omg('\ue621'),
    fa_pay('\ue611'),
    fa_eos('\ue626'),
    fa_bat('\ue61f'),
    fa_1st('\ue61c'),
    fa_snt('\ue60f'),
    fa_qtum('\ue624'),
    fa_btg('\ue627'),
    fa_sbtc('\ue628');
    char character;

    BitpieIcon(char character) {
        this.character = character;
    }

    @Override
    public String key() {
        return name().replace('_', '-');
    }

    @Override
    public char character() {
        return character;
    }
}
