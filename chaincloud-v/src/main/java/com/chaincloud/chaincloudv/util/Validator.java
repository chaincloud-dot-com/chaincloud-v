package com.chaincloud.chaincloudv.util;

import android.util.Log;

import com.chaincloud.chaincloudv.util.crypto.Base58;
import com.chaincloud.chaincloudv.util.crypto.Bech32;
import com.chaincloud.chaincloudv.util.crypto.BitcoinUtils;

import java.util.Arrays;

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
        if (coin.isEOS()) {
            return str.toString().matches("^[12345abcdefghijklmnopqrstuvwxyz.]{1,12}$");
        }else if (coin.isBTM()){
            return str != null && str.toString().startsWith("bm") &&
                    str.toString().matches("[0-9a-z]{42}");
        }else if (coin == Coin.TRX){
            return TrxUtils.decodeFromBase58Check(str.toString()) != null;
        }else if (!coin.isEther()) {
            try {
                if (coin.equals(Coin.BTC) && str.toString().startsWith("bc")){
                    Bech32.decode(str.toString());
                    return true;
                }

                String addressHeader = coin.getAddressPrefix();
                String p2shHeader = coin.getPayToScriptPrefix();

                byte[] tmp = Base58.decodeChecked(str.toString());

                byte[] addressHeaderTmp = Arrays.copyOfRange(tmp, 0, addressHeader.length() / 2);
                byte[] p2shHeaderTmp = Arrays.copyOfRange(tmp, 0, p2shHeader.length() / 2);

                return (BitcoinUtils.bytesToHexString(p2shHeaderTmp).equals(p2shHeader)
                        || BitcoinUtils.bytesToHexString(addressHeaderTmp).equals(addressHeader));
            } catch (Exception e) {
                return false;
            }
        }else {
            return str.toString().matches("[0-9a-fA-FXx]{42}");
        }
    }
}
