package com.chaincloud.chaincloudv.api;

import com.chaincloud.chaincloudv.ChainCloudVApplication_;
import com.chaincloud.chaincloudv.preference.Preference_;

import retrofit.RequestInterceptor;

/**
 * Created by songchenwen on 15/8/5.
 */
public class ChainCloudColdReceiveAltApiInterceptor implements RequestInterceptor {
    private static ChainCloudColdReceiveAltApiInterceptor instance;
    private static Object newInstanceLock = new Object();

    public static ChainCloudColdReceiveAltApiInterceptor instance() {
        synchronized (newInstanceLock) {
            if (instance == null) {
                instance = new ChainCloudColdReceiveAltApiInterceptor();
            }
        }
        return instance;
    }

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("Accept", "application/json");

        String token = new Preference_(ChainCloudVApplication_
                .getInstance()).tokenChainCloudColdReceiveAlt().getOr("");
        request.addHeader("Token", token);
    }
}
