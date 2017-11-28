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
@EActivity(R.layout.activity_setting_loop_internal)
public class SettingLoopInternalActivity extends Activity {

    @ViewById
    EditText etInternal;

    @Pref
    Preference_ preference;


    @AfterViews
    void init(){
        etInternal.setText(String.valueOf(preference.loopInternal().get()));
    }

    @Click
    void ibtnBack(){
        finish();
    }

    @Click
    void btnSave(){
        String internal = etInternal.getText().toString();

        if (!internal.equals("")){
            int i = Integer.parseInt(internal);
            if (i >= 3) {
                preference.edit()
                        .loopInternal().put(i)
                        .apply();

                Toast.makeText(SettingLoopInternalActivity.this, "save successful", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(SettingLoopInternalActivity.this, "must be > 3s int", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(SettingLoopInternalActivity.this, "passwd is empty", Toast.LENGTH_SHORT).show();
        }
    }
}
