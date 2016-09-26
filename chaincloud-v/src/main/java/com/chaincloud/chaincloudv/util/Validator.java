package com.chaincloud.chaincloudv.util;

import android.util.Log;

import com.chaincloud.chaincloudv.util.crypto.Base58;

/**
 * Created by songchenwen on 15/8/6.
 */
public class Validator {
    private static final String Tag = "Validator";

    public static final boolean validPhone(CharSequence str) {
        Log.d(Tag, "phone " + str);
        if (Utils.isEmpty(str)) {
            return false;
        }
        return str.toString().matches("^[+]?[0-9]{10,13}$");
    }

    public static final boolean validPassword(CharSequence str) {
        if (Utils.isEmpty(str)) {
            return false;
        }
        return str.length() >= 6;
    }

    public static final boolean validPercentage(CharSequence str) {
        if (Utils.isEmpty(str)) {
            return false;
        }
        return str.toString().matches("^[0-9]{1,3}(\\.[0-9]{0,2})?");
    }

    public static final boolean validAdvertVol(CharSequence str) {
        if (Utils.isEmpty(str)) {
            return false;
        }
        return str.toString().matches("^[0-9]+(\\.[0-9]{0,4})?");
    }

    public static final boolean validBitcoinAddress(CharSequence str) {
        int addressHeader = 0;
        int p2shHeader = 5;
        try {
            byte[] tmp = Base58.decodeChecked(str.toString());
            int header = tmp[0] & 0xFF;
            return (header == p2shHeader || header == addressHeader);
        } catch (Exception e) {
            return false;
        }
    }

    public static final boolean validAddress(Coin coin, CharSequence str) {
        int addressHeader = coin.getAddress();
        int p2shHeader = coin.getPayToScript();
        try {
            byte[] tmp = Base58.decodeChecked(str.toString());
            int header = tmp[0] & 0xFF;
            return (header == p2shHeader || header == addressHeader);
        } catch (Exception e) {
            return false;
        }
    }
}
