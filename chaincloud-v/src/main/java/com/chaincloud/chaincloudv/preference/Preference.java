package com.chaincloud.chaincloudv.preference;

import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.DefaultLong;
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

    @DefaultInt(1)
    int smsObserverType(); //1->observer db; 2->broadcast

    @DefaultLong(0)
    long balanceBtc();

    @DefaultLong(0)
    long balanceLtc();

    @DefaultLong(0)
    long balanceDoge();

    @DefaultLong(-1)
    long balanceThresholdBtc();

    @DefaultLong(-1)
    long balanceThresholdLtc();

    @DefaultLong(-1)
    long balanceThresholdDoge();
}
