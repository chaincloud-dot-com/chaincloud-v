package com.chaincloud.chaincloudv.util;

/**
 * Created by zhumingu on 16/7/20.
 */
public class SMSCommandUtil {

    private static final String H2CExchange = "CC:QH:%1$s:H_ID:%2$s:";
    private static final String H2COK = "CC:CHANNEL_OK:%1$s:";
    private static final String HotAddressCheck = "CC:VERIFY_HOT_ADDRESSES:%1$s:";
    private static final String ColdAddressCheck = "CC:VERIFY_COLD_ADDRESSES:%1$s:";


    public static String getH2CExchange(String qh, Integer hId){
        return String.format(H2CExchange, qh, hId);
    }

    public static String getH2COK(Integer cId){
        return String.format(H2COK, cId);
    }

    public static String getHotAddressCheck(Integer index){
        return String.format(HotAddressCheck, index);
    }

    public static String getColdAddressCheck(Integer index){
        return String.format(ColdAddressCheck, index);
    }
}
