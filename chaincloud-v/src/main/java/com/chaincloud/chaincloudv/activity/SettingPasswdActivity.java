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
@EActivity(R.layout.activity_setting_passwd)
public class SettingPasswdActivity extends Activity {

    @ViewById
    EditText etPasswd;

    @Pref
    Preference_ preference;


    @AfterViews
    void init(){
        etPasswd.setText(preference.passwdTransfer().get());
    }

    @Click
    void ibtnBack(){
        finish();
    }

    @Click
    void btnSave(){
        String passwd = etPasswd.getText().toString();

        if (!passwd.equals("")){
            preference.edit()
                    .passwdTransfer().put(passwd)
                    .apply();

            Toast.makeText(SettingPasswdActivity.this, "save successful", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(SettingPasswdActivity.this, "passwd is empty", Toast.LENGTH_SHORT).show();
        }
    }
}
