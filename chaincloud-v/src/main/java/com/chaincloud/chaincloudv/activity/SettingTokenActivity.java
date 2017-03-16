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
 * Created by zhumingu on 16/7/25.
 */
@EActivity(R.layout.activity_setting_token)
public class SettingTokenActivity extends Activity {

    @ViewById
    EditText etTokenVtest;

    @ViewById
    EditText etTokenHot;

    @ViewById
    EditText etTokenCold;

    @ViewById
    EditText etTokenHotAlt;

    @ViewById
    EditText etTokenColdAlt;

    @Pref
    Preference_ preference;


    @AfterViews
    void init(){
        etTokenVtest.setText(preference.tokenVTest().get());
        etTokenHot.setText(preference.tokenChainCloudHotSend().get());
        etTokenCold.setText(preference.tokenChainCloudColdReceive().get());
        etTokenHotAlt.setText(preference.tokenChainCloudHotSendAlt().get());
        etTokenColdAlt.setText(preference.tokenChainCloudColdReceiveAlt().get());
    }

    @Click
    void ibtnBack(){
        finish();
    }

    @Click
    void btnSave(){
        String vTestToken = etTokenVtest.getText().toString();
        String hotToken = etTokenHot.getText().toString();
        String coldToken = etTokenCold.getText().toString();
        String hotTokenAlt = etTokenHotAlt.getText().toString();
        String coldTokenAlt = etTokenColdAlt.getText().toString();

//        if(!vTestToken.equals("") && !hotToken.equals("")
//                && !coldToken.equals("")) {

            preference.edit()
                    .tokenVTest().put(vTestToken)
                    .tokenChainCloudHotSend().put(hotToken)
                    .tokenChainCloudColdReceive().put(coldToken)
                    .tokenChainCloudHotSendAlt().put(hotTokenAlt)
                    .tokenChainCloudColdReceiveAlt().put(coldTokenAlt)
                    .apply();

            Toast.makeText(SettingTokenActivity.this, "save successful", Toast.LENGTH_SHORT).show();
//        }else {
//            Toast.makeText(SettingTokenActivity.this, "token is empty", Toast.LENGTH_SHORT).show();
//        }
    }
}
