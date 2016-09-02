package com.chaincloud.chaincloudv.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import de.greenrobot.event.EventBus;
import retrofit.ErrorHandler;
import retrofit.RetrofitError;

/**
 * Created by songchenwen on 15/8/11.
 */
public class ApiErrorHandler implements ErrorHandler {
    private static ApiErrorHandler instance;
    private static final Object newInstanceLock = new Object();

    public static final ApiErrorHandler instance() {
        synchronized (newInstanceLock) {
            if (instance == null) {
                instance = new ApiErrorHandler();
            }
        }
        return instance;
    }

    @Override
    public Throwable handleError(RetrofitError cause) {
        if (shouldNotifyError(cause)) {
            EventBus.getDefault().post(cause);
        }
        return cause;
    }

    private static HashSet<String> ignoredUrls = new HashSet<String>(Arrays.asList(new String[]{}));

    private boolean shouldNotifyError(RetrofitError error) {
        try {
            URL url = new URL(error.getUrl());
            if (ignoredUrls.contains(url.getPath())) {
                return false;
            }
        } catch (MalformedURLException e) {
        }
        return true;
    }
}
