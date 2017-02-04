package com.chaincloud.chaincloudv.activity;

import android.app.Activity;
import android.widget.EditText;
import android.widget.Toast;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.preference.Preference_;

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
        etBalanceThresholdBtc.setText(String.valueOf(preference.balanceThresholdBtc().get()));
        etBalanceThresholdLtc.setText(String.valueOf(preference.balanceThresholdLtc().get()));
        etBalanceThresholdDoge.setText(String.valueOf(preference.balanceThresholdDoge().get()));
    }

    @Click
    void ibtnBack(){
        finish();
    }

    @Click
    void btnSave(){
        preference.edit()
                .balanceThresholdBtc().put(Long.parseLong(etBalanceThresholdBtc.getText().toString()))
                .balanceThresholdLtc().put(Long.parseLong(etBalanceThresholdLtc.getText().toString()))
                .balanceThresholdDoge().put(Long.parseLong(etBalanceThresholdDoge.getText().toString()))
                .apply();

        Toast.makeText(this, "setting balance threshold is ok", Toast.LENGTH_SHORT).show();
    }
}
