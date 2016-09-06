package com.chaincloud.chaincloudv.api;

import android.util.Log;

import com.chaincloud.chaincloudv.ChainCloudVApplication_;
import com.chaincloud.chaincloudv.BuildConfig;
import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.api.service.ChainCloudColdReceiveService;
import com.chaincloud.chaincloudv.api.service.ChainCloudHotSendService;
import com.chaincloud.chaincloudv.api.service.VWebService;
import com.chaincloud.chaincloudv.api.type.DateTypeAdapter;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;
import java.util.Hashtable;

import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.converter.GsonConverter;

/**
 * Created by songchenwen on 15/8/5.
 */
public class Api {
    public enum ServerType {
        ChainCloudHotSend(0), ChainCloudColdReceive(2), VTest(3);

        private int value;

        ServerType(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public RestAdapter adapter() {
            switch (this) {
                case ChainCloudHotSend:
                    return ChainCloudHotSendApiAdapter;
                case ChainCloudColdReceive:
                    return ChainCloudColdReceiveApiAdapter;
                case VTest:
                    return VTestApiAdapter;
            }
            return ChainCloudHotSendApiAdapter;
        }

        public int nameRes(){
            switch (this){
                case ChainCloudHotSend:
                    return R.string.setting_network_hs_name;
                case ChainCloudColdReceive:
                    return R.string.setting_network_cr_name;
                case VTest:
                    return R.string.setting_network_vtest_name;
            }
            return R.string.setting_network_hs_name;
        }
    }

    public static final String ChainCloudHotSendDomain = "https://chaincloud-api.getcai.com";
    public static final String ChainCloudColdReceiveDomain = "https://chaincloud-api.getcai.com";
//    public static final String ChainCloudHotSendDomain = "http://192.168.1.222:5000";
//    public static final String ChainCloudColdReceiveDomain = "http://192.168.1.222:5000";
    public static final String ChainCloudHotSendApiRootPath = "/api/v1/";
    public static final String ChainCloudColdReiceveApiRootPath = "/api/v1/";
    public static final String ChainCloudHotSendApiEndpoint = ChainCloudHotSendDomain + ChainCloudHotSendApiRootPath;
    public static final String ChainCloudColdReceiveApiEndpoint = ChainCloudColdReceiveDomain + ChainCloudColdReiceveApiRootPath;

//    public static String VTestDomain = "http://192.168.1.222:5000";
    public static final String VTestApiRootPath = "/api/v1/";
//    public static String VTestApiEndpoint = VTestDomain + VTestApiRootPath;

    private static final String LogTag = "API";
    private static final RestAdapter.LogLevel LogLevel = BuildConfig.DEBUG ?
            RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE;

    private static final Hashtable<Class, Object> ChainCloudHotSendApiServices = new Hashtable<Class, Object>();
    private static final Hashtable<Class, Object> ChainCloudColdReceiveApiServices = new Hashtable<Class, Object>();
    private static final Hashtable<Class, Object> VTestApiServices = new Hashtable<Class, Object>();
    public static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy
            .LOWER_CASE_WITH_UNDERSCORES).registerTypeAdapter(Date.class, new DateTypeAdapter())
            .create();

    public static RestAdapter ChainCloudHotSendApiAdapter = getBaseBuilder()
            .setEndpoint(ChainCloudHotSendApiEndpoint)
            .setRequestInterceptor(ChainCloudHotSendApiInterceptor.instance())
            .build();

    public static RestAdapter ChainCloudColdReceiveApiAdapter = getBaseBuilder()
            .setEndpoint(ChainCloudColdReceiveApiEndpoint)
            .setRequestInterceptor(ChainCloudColdReceiveApiInterceptor.instance())
            .build();

    public static RestAdapter VTestApiAdapter;

    public static <T> T apiService(Class<T> service) {
        ServerType serverType = currentServerType(service);
        RestAdapter currentAdapter = serverType.adapter();

        Hashtable<Class, Object> services = ChainCloudHotSendApiServices;
        if (serverType == ServerType.ChainCloudColdReceive) {
            services = ChainCloudColdReceiveApiServices;
        }else if (serverType == ServerType.VTest) {
            services = VTestApiServices;
        }

        if (!services.containsKey(service)) {
            Log.i(LogTag, "creating service for " + service.getSimpleName());
            T result = currentAdapter.create(service);
            services.put(service, result);
        }
        return (T) services.get(service);
    }

    public static void setVWebDomain(String domain){

        String VTestApiEndpoint = domain + VTestApiRootPath;

        VTestApiAdapter = getBaseBuilder()
                .setEndpoint(VTestApiEndpoint)
                .setRequestInterceptor(VWebApiInterceptor.instance())
                .build();

        VTestApiServices.put(VWebService.class,
                ServerType.VTest.adapter().create(VWebService.class));
    }

    private static ServerType currentServerType(Class service) {
        if (service == ChainCloudHotSendService.class){
            return ServerType.ChainCloudHotSend;
        }else if (service == ChainCloudColdReceiveService.class){
            return ServerType.ChainCloudColdReceive;
        }else {
            return ServerType.VTest;
        }
    }

    private static RestAdapter.Builder getBaseBuilder(){
        return new RestAdapter.Builder()
                .setLog(new AndroidLog(LogTag))
                .setLogLevel(LogLevel)
                .setConverter(new GsonConverter(gson))
                .setErrorHandler(ApiErrorHandler.instance());
    }
}
