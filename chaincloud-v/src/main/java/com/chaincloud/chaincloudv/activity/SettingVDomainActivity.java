package com.chaincloud.chaincloudv.activity;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.EditText;
import android.widget.Toast;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.api.Api;
import com.chaincloud.chaincloudv.event.UpdateVWebService;
import com.chaincloud.chaincloudv.preference.Preference_;
import com.chaincloud.chaincloudv.service.AddressService;
import com.chaincloud.chaincloudv.service.AddressService_;
import com.chaincloud.chaincloudv.service.WorkService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import de.greenrobot.event.EventBus;

/**
 * Created by zhumingu on 16/7/25.
 */
@EActivity(R.layout.activity_setting_domain)
public class SettingVDomainActivity extends Activity implements WorkService.MsgListener {

    private AddressService.AddressBinder binder;

    @ViewById
    EditText etDomain;

    @Pref
    Preference_ preference;


    @AfterViews
    void init(){
        etDomain.setText(preference.vwebDomain().get());

        bindService(new Intent(this, AddressService_.class),
                conn, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(binder != null) {
            unbindService(conn);

            binder.removeListener();
            binder = null;
        }
    }

    @Click
    void ibtnBack(){
        finish();
    }

    @Click
    void btnSave(){
        String domain = etDomain.getText().toString();

        if (!domain.equals("")){
            preference.edit()
                    .vwebDomain().put(domain)
                    .apply();

            Api.setVWebDomain(domain);

            EventBus.getDefault().post(new UpdateVWebService());

            Toast.makeText(SettingVDomainActivity.this, "save successful", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(SettingVDomainActivity.this, "domain is empty", Toast.LENGTH_SHORT).show();
        }
    }

    @Click
    void btnLoopAddressCheck(){
        if (binder != null){
            showMsg("start");

            binder.startLoop();
        }
    }

    @Click
    void btnUnloopAddressCheck(){
        if (binder != null) {
            showMsg("stop");

            binder.stopLoop();
        }
    }


    private void showMsg(String msg){
        Toast.makeText(SettingVDomainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }


    //region ServiceConnection
    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (AddressService.AddressBinder) service;
            binder.setListener(SettingVDomainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
        }
    };

    @Override
    public void onMsgReceive(String msg) {
        showMsg(msg);
    }
    //endregion
}
