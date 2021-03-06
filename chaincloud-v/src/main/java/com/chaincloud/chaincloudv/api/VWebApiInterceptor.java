package com.chaincloud.chaincloudv.api;

import com.chaincloud.chaincloudv.ChainCloudVApplication_;
import com.chaincloud.chaincloudv.preference.Preference_;

import retrofit.RequestInterceptor;

/**
 * Created by songchenwen on 15/8/5.
 */
public class VWebApiInterceptor implements RequestInterceptor {
    private static VWebApiInterceptor instance;
    private static Object newInstanceLock = new Object();

    public static VWebApiInterceptor instance() {
        synchronized (newInstanceLock) {
            if (instance == null) {
                instance = new VWebApiInterceptor();
            }
        }
        return instance;
    }

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("Accept", "application/json");
        String token = new Preference_(ChainCloudVApplication_.getInstance()).tokenVTest().getOr("");
        request.addHeader("Token", token);
        request.addHeader("Accept-Language", "zh_CN");
    }
}
