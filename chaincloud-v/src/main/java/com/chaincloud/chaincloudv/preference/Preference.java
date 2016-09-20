package com.chaincloud.chaincloudv.preference;

import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * Created by songchenwen on 15/7/24.
 */
@SharedPref(SharedPref.Scope.APPLICATION_DEFAULT)
public interface Preference {
    String tokenChainCloudHotSend();

    String tokenChainCloudColdReceive();

    String tokenVTest();

    int serverType();

    String currency();

    String chaincloudPhoneNo1();

    String chaincloudPhoneNo2();

    String vAdminPhoneNo();

    String passwdTransfer();

    String lastUserTxNo();

    @DefaultString("http://192.168.1.222:5000")
    String vwebDomain();

    @DefaultString("BTC")
    String coinCode();
}
