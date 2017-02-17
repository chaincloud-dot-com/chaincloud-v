package com.chaincloud.chaincloudv.activity;

import android.app.Activity;
import android.widget.EditText;
import android.widget.Toast;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.model.BitcoinUnit;
import com.chaincloud.chaincloudv.preference.Preference_;
import com.chaincloud.chaincloudv.util.BitcoinUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * Created by zhumingu on 16/7/21.
 */
@EActivity(R.layout.activity_setting_balance_threshold)
public class SettingBalanceThresholdActivity extends Activity {

    @ViewById
    EditText etBalanceThresholdBtc, etBalanceThresholdLtc, etBalanceThresholdDoge;

    @Pref
    Preference_ preference;


    @AfterViews
    void init(){
        Long btc = preference.balanceThresholdBtc().get();
//        Long ltc = preference.balanceThresholdLtc().get();
//        Long doge = preference.balanceThresholdDoge().get();

        etBalanceThresholdBtc.setText(String.valueOf(btc < 0? btc : BitcoinUnit.BTC.format(btc)));

//        int precisionLtc = (int) Math.floor(Math.log10(ltc));
//        etBalanceThresholdLtc.setText(String.valueOf(ltc < 0? ltc : BitcoinUtil.formatValue(ltc, precisionLtc, 8 - precisionLtc)));

//        int precisionDoge = (int) Math.floor(Math.log10(doge));
//        etBalanceThresholdDoge.setText(String.valueOf(doge < 0? doge : BitcoinUtil.formatValue(doge, precisionDoge, 8 - precisionDoge)));
    }

    @Click
    void ibtnBack(){
        finish();
    }

    @Click
    void btnSave(){
        try {
            double value = Double.parseDouble(etBalanceThresholdBtc.getText().toString());

            preference.edit()
                    .balanceThresholdBtc().put(value < 0 ? (long) value : (long)(value * Math.pow(10, 8)))
//                .balanceThresholdLtc().put(Long.parseLong(etBalanceThresholdLtc.getText().toString()))
//                .balanceThresholdDoge().put(Long.parseLong(etBalanceThresholdDoge.getText().toString()))
            .apply();

            Toast.makeText(this, "setting balance threshold is ok", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(this, "format is error", Toast.LENGTH_SHORT).show();
            return;
        }
    }
}
