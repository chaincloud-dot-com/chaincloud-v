package com.chaincloud.chaincloudv.activity;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.EditText;
import android.widget.Toast;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.preference.Preference_;
import com.chaincloud.chaincloudv.service.SMSServiceImpl;
import com.chaincloud.chaincloudv.service.SMSServiceImpl_;
import com.chaincloud.chaincloudv.ui.base.dialog.DialogAlert_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * Created by zhumingu on 16/7/21.
 */
@EActivity(R.layout.activity_channel_setting)
public class SettingChannelActivity extends FragmentActivity implements SMSServiceImpl.OnReceiveMsgListener {

    private static final int P_REQUEST_CODE = 8888;
    private static final int READSMS_REQUEST_CODE = 9999;

    @ViewById
    EditText etPhoneNo1;

    @ViewById
    EditText etPhoneNo2;

    @ViewById
    EditText etPhoneNoAdmin;

    @Pref
    Preference_ preference;

    SMSServiceImpl.SMSBinder binder;


    @AfterViews
    void init(){
        etPhoneNo1.setText(preference.chaincloudPhoneNo1().get());
        etPhoneNo2.setText(preference.chaincloudPhoneNo2().get());
        etPhoneNoAdmin.setText(preference.vAdminPhoneNo().get());

        bindService(new Intent(this, SMSServiceImpl_.class), conn, Service.BIND_AUTO_CREATE);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, P_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS}, READSMS_REQUEST_CODE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(conn);
    }

    @Click
    void ibtnBack(){
        finish();
    }

    @Click
    void btnOk(){
        String phoneNo1 = etPhoneNo1.getText().toString();
        String phoneNo2 = etPhoneNo2.getText().toString();
        String phoneNoAdmin = etPhoneNoAdmin.getText().toString();

        if(phoneNo1.equals("") && phoneNo1.equals("")){
            Toast.makeText(this, "chaincloud至少填写一个手机号", Toast.LENGTH_SHORT).show();

            return;
        }

        if(phoneNoAdmin.equals("")){
            Toast.makeText(this, "管理员手机号不能为空", Toast.LENGTH_SHORT).show();

            return;
        }

        preference.edit()
                .chaincloudPhoneNo1().put(phoneNo1)
                .chaincloudPhoneNo2().put(phoneNo2)
                .vAdminPhoneNo().put(phoneNoAdmin)
                .apply();

        binder.reloadPreference();
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
    }

    @Click
    void btnCreate(){
        if((!preference.chaincloudPhoneNo1().getOr("").equals("")
                || !preference.chaincloudPhoneNo2().get().equals(""))
            && !preference.vAdminPhoneNo().getOr("").equals("")){

            DialogAlert_.builder()
                    .msg("请在 设置->应用 里手动打开 读短信 写短信 的权限！")
                    .ok("已设置")
                    .build()
                    .setRunnable(new Runnable() {
                        @Override
                        public void run() {
                            binder.openSMSChannel();

                            binder.createChannel();
                            showMsg("开始建立通道...");
                        }
                    })
                    .show(getSupportFragmentManager());
        }else {
            Toast.makeText(this, "请先完善手机号信息", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReceiveMsg(String msg) {
        showMsg(msg);
    }

    @UiThread
    void showMsg(String msg){
        Toast.makeText(SettingChannelActivity.this, msg, Toast.LENGTH_SHORT).show();
    }


    //region ServiceConnection
    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (SMSServiceImpl.SMSBinder) service;

            binder.setOnReceiveMsgListener(SettingChannelActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder.setOnReceiveMsgListener(null);
            binder = null;
        }
    };
    //endregion
}
