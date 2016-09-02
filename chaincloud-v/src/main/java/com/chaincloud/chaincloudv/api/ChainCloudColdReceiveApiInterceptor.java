package com.chaincloud.chaincloudv.api;

import com.chaincloud.chaincloudv.ChainCloudVApplication_;
import com.chaincloud.chaincloudv.preference.Preference_;

import retrofit.RequestInterceptor;

/**
 * Created by songchenwen on 15/8/5.
 */
public class ChainCloudColdReceiveApiInterceptor implements RequestInterceptor {
    private static ChainCloudColdReceiveApiInterceptor instance;
    private static Object newInstanceLock = new Object();

    public static ChainCloudColdReceiveApiInterceptor instance() {
        synchronized (newInstanceLock) {
            if (instance == null) {
                instance = new ChainCloudColdReceiveApiInterceptor();
            }
        }
        return instance;
    }

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("Accept", "application/json");

        String token = new Preference_(ChainCloudVApplication_
                .getInstance()).tokenChainCloudColdReceive().getOr("");
        request.addHeader("Token", token);
    }
}
