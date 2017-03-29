package com.chaincloud.chaincloudv.api.service;

import android.support.annotation.NonNull;

import com.chaincloud.chaincloudv.api.result.BooleanResult;
import com.chaincloud.chaincloudv.api.result.TxResult;
import com.chaincloud.chaincloudv.event.AddressBatchResult;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Created by zhumingu on 16/6/23.
 */
public interface VWebService {

    @GET("/open/vtest")
    TxResult getNextUnSignTx();

    @FormUrlEncoded
    @POST("/open/vtest")
    BooleanResult postStatus(@NonNull @Field("vtest_id") String vtestId,
                             @Field("tx_hash") String txHash);

    @GET("/open/batch")
    AddressBatchResult getNextAddressBatch();

    @FormUrlEncoded
    @POST("/open/batch")
    BooleanResult postAddressBatchStatus(@NonNull @Field("batch_no") Integer batchNo,
                                         @Field("status") Integer status,
                                         @Field("type") Integer type,
                                         @Field("coin_type") String coinType);
}
