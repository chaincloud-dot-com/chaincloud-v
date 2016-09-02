package com.chaincloud.chaincloudv.fragment;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.activity.SettingChannelActivity_;
import com.chaincloud.chaincloudv.activity.SettingPasswdActivity_;
import com.chaincloud.chaincloudv.activity.SettingTokenActivity_;
import com.chaincloud.chaincloudv.activity.SettingVDomainActivity_;
import com.chaincloud.chaincloudv.ui.base.dialog.DialogAlert_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by zhumingu on 16/6/20.
 */
@EFragment(R.layout.fragment_setting)
public class SettingFragment extends Fragment {

    @ViewById(R.id.tv_version)
    TextView tvVersion;


    @AfterViews
    void init(){
        initVersion();
    }

    @Click
    void tvTokenSetting(){
        SettingTokenActivity_.intent(getContext()).start();
    }

    @Click
    void tvChannelSetting(){
        SettingChannelActivity_.intent(getContext()).start();
    }

    @Click
    void tvPasswdSetting(){
        SettingPasswdActivity_.intent(getContext()).start();
    }

    @Click
    void tvVwebdomainSetting(){
        SettingVDomainActivity_.intent(getContext()).start();
    }

    @Click
    void tvLogSetting(){
        final File saveDir = getContext().getExternalCacheDir();
        final File logDir = getContext().getDir("log", Context.MODE_WORLD_READABLE);

        DialogAlert_.builder()
                .msg("确认导出log到外存？")
                .ok("确定")
                .build()
                .setRunnable(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            showMsg("日志导出至" + saveDir.getAbsolutePath());

                            copyFile(logDir, saveDir);
                        } catch (Exception e) {
                            Log.e(getClass().getSimpleName(), "copy log error", e);
                        }
                    }
                })
                .show(getFragmentManager());
    }

    @UiThread
    void showMsg(String msg){
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }


    private void copyFile(File src, File tar) throws Exception {

        if (src.isFile()) {
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                InputStream is = new FileInputStream(src);
                bis = new BufferedInputStream(is);
                OutputStream op = new FileOutputStream(tar);
                bos = new BufferedOutputStream(op);
                byte[] bt = new byte[8192];
                int len = bis.read(bt);
                while (len != -1) {
                    bos.write(bt, 0, len);
                    len = bis.read(bt);
                }
                bis.close();
                bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }

        } else if (src.isDirectory()) {
            File[] files = src.listFiles();
            tar.mkdir();
            for (int i = 0;
                 i < files.length;
                 i++) {
                copyFile(files[i].getAbsoluteFile(),
                        new File(tar.getAbsoluteFile() + File.separator
                                + files[i].getName())
                );
            }
        } else {
            throw new FileNotFoundException();
        }

    }


    private void initVersion() {
        try {
            tvVersion.setText(String.format(getString(R.string.setting_version),
                    getActivity().getPackageManager().getPackageInfo(getActivity()
                            .getPackageName(), 0).versionName));

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
