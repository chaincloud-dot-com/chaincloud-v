package com.chaincloud.chaincloudv.model;

import com.chaincloud.chaincloudv.util.BitcoinUtil;

/**
 * Created by songchenwen on 15/8/12.
 */
public enum BitcoinUnit {
    BTC(100000000), bits(100);

    public long satoshis;

    BitcoinUnit(long satoshis) {
        this.satoshis = satoshis;
    }

    public String format(long value){
        int precision = (int) Math.floor(Math.log10(this.satoshis));
        return BitcoinUtil.formatValue(value, precision, 8 - precision);
    }
}
