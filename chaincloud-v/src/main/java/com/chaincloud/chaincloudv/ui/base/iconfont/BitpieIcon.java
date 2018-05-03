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
    fa_pok('\ue639'),
    fa_usdt('\ue64a'),
    fa_scny('\ue64b'),
    fa_jpy_yen('\ue649'),
    fa_snt('\ue60f'),
    fa_qtum('\ue624'),
    fa_btg('\ue627'),
    fa_btw('\ue62b'),
    fa_bcd('\ue62e'),
    fa_hsr('\ue630'),
    fa_dash('\ue632'),
    fa_safe('\ue640'),
    fa_lch('\ue641'),
    fa_btf('\ue635'),
    fa_btp('\ue634'),
    fa_btn('\ue636'),
    fa_btnold('\ue631'),
    fa_btv('\ue63d'),
    fa_bpa('\ue63c'),
    fa_cdy('\ue604'),
    fa_bbc('\ue644'),
    fa_bcx('\ue64f'),
    fa_lbtc('\ue643'),
    fa_sbtc('\ue62a');
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
