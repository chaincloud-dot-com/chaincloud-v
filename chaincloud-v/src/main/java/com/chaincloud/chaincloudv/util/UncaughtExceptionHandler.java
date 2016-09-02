package com.chaincloud.chaincloudv.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.chaincloud.chaincloudv.ChainCloudVApplication_;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Created by songchenwen on 16/3/16.
 */
public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private String appInfo;
    private String logFile;

    public UncaughtExceptionHandler() {
        appInfo = "ver:" + Integer.toString(versionCode()) + ",sdk:" + Build.VERSION.SDK_INT + ",";
        logFile = getLogFile();
    }

    public static final String getLogFile() {
        return getLogDir() + File.separator + "error.log";
    }

    public static final String getLogDir() {
        String logDir;
        if (ChainCloudVApplication_.getInstance().getExternalCacheDir() != null) {
            logDir = ChainCloudVApplication_.getInstance().getExternalCacheDir().getAbsolutePath();
        } else {
            logDir = ChainCloudVApplication_.getInstance().getCacheDir().getAbsolutePath();
        }
        logDir = logDir + File.separator + "bitpie-admin";
        File path = new File(logDir);
        if (!path.exists()) {
            path.mkdirs();
        }
        return logDir;
    }

    private int versionCode() {
        try {
            PackageManager pm = ChainCloudVApplication_.getInstance().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ChainCloudVApplication_.getInstance().getPackageName(),
                    0);
            return pi.versionCode;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
            return 0;
        }
    }

    @Override
    public void uncaughtException(Thread thread, final Throwable ex) {
        new Thread() {
            @Override
            public void run() {
                String info = null;
                FileOutputStream fileOutPutStream = null;
                PrintStream printStream = null;
                ByteArrayOutputStream baos = null;
                try {
                    baos = new ByteArrayOutputStream();
                    File path = new File(logFile);

                    fileOutPutStream = new FileOutputStream(path);
                    baos.write(appInfo.getBytes());
                    printStream = new PrintStream(baos);
                    ex.printStackTrace(printStream);
                    byte[] data = baos.toByteArray();
                    info = new String(data);
                    fileOutPutStream.write(data);
                    Log.e("UE", info);
                    android.os.Process.killProcess(android.os.Process.myPid());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (printStream != null) {
                            printStream.close();
                        }
                        if (fileOutPutStream != null) {
                            fileOutPutStream.flush();
                            fileOutPutStream.close();
                        }
                        if (baos != null) {
                            baos.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}
