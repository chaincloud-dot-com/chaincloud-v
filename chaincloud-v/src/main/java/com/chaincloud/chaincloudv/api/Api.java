package com.chaincloud.chaincloudv.api;

import com.chaincloud.chaincloudv.BuildConfig;
import com.chaincloud.chaincloudv.GlobalParams;
import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.api.service.ChainCloudColdReceiveService;
import com.chaincloud.chaincloudv.api.service.ChainCloudHotSendService;
import com.chaincloud.chaincloudv.api.service.VWebService;
import com.chaincloud.chaincloudv.api.type.DateTypeAdapter;
import com.chaincloud.chaincloudv.util.Coin;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

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
    public static final String ChainCloudHotSendAltDomain = "https://chaincloud-api.getcai.com";
    public static final String ChainCloudColdReceiveAltDomain = "https://chaincloud-api.getcai.com";
//    public static final String ChainCloudHotSendDomain = "http://192.168.1.161:5000";
//    public static final String ChainCloudColdReceiveDomain = "http://192.168.1.161:5000";
//    public static final String ChainCloudHotSendAltDomain = "http://192.168.1.161:5000";
//    public static final String ChainCloudColdReceiveAltDomain = "http://192.168.1.161:5000";
    public static final String ChainCloudHotSendApiRootPath = "/api/v1/";
    public static final String ChainCloudColdReiceveApiRootPath = "/api/v1/";
    public static final String ChainCloudHotSendAltApiRootPath = "/api/v1/";
    public static final String ChainCloudColdReiceveAltApiRootPath = "/api/v1/";
    public static final String ChainCloudHotSendApiEndpoint = ChainCloudHotSendDomain + ChainCloudHotSendApiRootPath;
    public static final String ChainCloudColdReceiveApiEndpoint = ChainCloudColdReceiveDomain + ChainCloudColdReiceveApiRootPath;
    public static final String ChainCloudHotSendAltApiEndpoint = ChainCloudHotSendAltDomain + ChainCloudHotSendAltApiRootPath;
    public static final String ChainCloudColdReceiveAltApiEndpoint = ChainCloudColdReceiveAltDomain + ChainCloudColdReiceveAltApiRootPath;

//    public static String VTestDomain;
    public static final String VTestApiRootPath = "/api/v1/";
//    public static String VTestApiEndpoint;

    private static final String LogTag = "API";
    private static final RestAdapter.LogLevel LogLevel = BuildConfig.DEBUG ?
            RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE;

    public static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy
            .LOWER_CASE_WITH_UNDERSCORES).registerTypeAdapter(Date.class, new DateTypeAdapter())
            .create();

    public static ChainCloudHotSendService chainCloudHotSendService, chainCloudHotSendAltService;
    public static ChainCloudColdReceiveService chainCloudColdReceiveService, chainCloudColdReceiveAltService;
    public static VWebService vWebService;

    public static <T> T apiService(Class<T> service) {
        return apiServiceAlt(service, !GlobalParams.coinCode.equals(Coin.BTC.getCode()));
    }

    public static <T> T apiServiceAlt(Class<T> service, boolean isAlt) {
        if (service == VWebService.class){
            return (T) vWebService;
        }

        if (!isAlt){
            if (service == ChainCloudHotSendService.class){
                if (chainCloudHotSendService == null){
                    chainCloudHotSendService = (ChainCloudHotSendService) constructService(service, isAlt);
                }

                return (T) chainCloudHotSendService;
            }else if (service == ChainCloudColdReceiveService.class){
                if (chainCloudColdReceiveService == null){
                    chainCloudColdReceiveService = (ChainCloudColdReceiveService) constructService(service, isAlt);
                }

                return (T) chainCloudColdReceiveService;
            }
        }else {
            if (service == ChainCloudHotSendService.class){
                if (chainCloudHotSendAltService == null){
                    chainCloudHotSendAltService = (ChainCloudHotSendService) constructService(service, isAlt);
                }

                return (T) chainCloudHotSendAltService;
            }else if (service == ChainCloudColdReceiveService.class){
                if (chainCloudColdReceiveAltService == null){
                    chainCloudColdReceiveAltService = (ChainCloudColdReceiveService) constructService(service, isAlt);
                }

                return (T) chainCloudColdReceiveAltService;
            }
        }

        return null;
    }

    public static void setVWebDomain(String domain){
        String VTestApiEndpoint = domain + VTestApiRootPath;

        RestAdapter adapter = getBaseBuilder()
                .setEndpoint(VTestApiEndpoint)
                .setRequestInterceptor(VWebApiInterceptor.instance())
                .build();

        vWebService = adapter.create(VWebService.class);
    }


    private static <T> T constructService(Class<T> service, boolean isAlt){
        if (service == ChainCloudHotSendService.class){
            RestAdapter adapter;
            if (!isAlt){
                adapter = getBaseBuilder()
                        .setEndpoint(ChainCloudHotSendApiEndpoint)
                        .setRequestInterceptor(ChainCloudHotSendApiInterceptor.instance())
                        .build();
            }else {
                adapter = getBaseBuilder()
                        .setEndpoint(ChainCloudHotSendAltApiEndpoint)
                        .setRequestInterceptor(ChainCloudHotSendAltApiInterceptor.instance())
                        .build();
            }

            return adapter.create(service);
        }else if (service == ChainCloudColdReceiveService.class){
            RestAdapter adapter;
            if (!isAlt){
                adapter = getBaseBuilder()
                        .setEndpoint(ChainCloudColdReceiveApiEndpoint)
                        .setRequestInterceptor(ChainCloudColdReceiveApiInterceptor.instance())
                        .build();
            }else {
                adapter = getBaseBuilder()
                        .setEndpoint(ChainCloudColdReceiveAltApiEndpoint)
                        .setRequestInterceptor(ChainCloudColdReceiveAltApiInterceptor.instance())
                        .build();
            }

            return adapter.create(service);
        }else if (service == VWebService.class){
            return (T) vWebService;
        }else {
            return null;
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
