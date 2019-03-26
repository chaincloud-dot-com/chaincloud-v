package com.chaincloud.chaincloudv.util;

import com.chaincloud.chaincloudv.util.crypto.Base58;

/**
 * Created by Hzz on 2018/6/25.
 */

public class TrxUtils {

    public static byte[] decodeFromBase58Check(String addressBase58) {
        if (Utils.isEmpty(addressBase58)) {
            return null;
        }
        byte[] address = Base58.decodeChecked(addressBase58);
        if (!isAddressValid(address)) {
            return null;
        }
        return address;
    }


    public static boolean isAddressValid(byte[] address) {
        byte ADD_PRE_FIX_BYTE = (byte) 0x41;   //a0 + address  ,a0 is version
        int ADDRESS_SIZE = 21;

        if (address == null || address.length == 0) {
            return false;
        }
        if (address.length != ADDRESS_SIZE) {
            return false;
        }
        byte preFixbyte = address[0];
        if (preFixbyte != ADD_PRE_FIX_BYTE) {
            return false;
        }

        return true;
    }

    public static int compare(long x, long y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
}
