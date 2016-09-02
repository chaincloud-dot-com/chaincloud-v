package com.chaincloud.chaincloudv.api.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chaincloud.chaincloudv.api.result.BooleanResult;
import com.chaincloud.chaincloudv.api.result.TxStatus;
import com.chaincloud.chaincloudv.model.Address;
import com.chaincloud.chaincloudv.model.Tx;
import com.chaincloud.chaincloudv.model.User;

import java.util.List;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by zhumingu on 16/6/23.
 */
public interface ChainCloudHotSendService {

    @GET("/open/user")
    User currentUser();

    @GET("/open/tx/detail/{tx_hash}")
    Tx getDetail(@NonNull @Path("tx_hash") String txHash);

    @GET("/open/address/history/{path}")
    List<Object> addressHistory(@Path("path") int path,
                                @Query("since_address") String sinceAddress);

    @GET("/open/tx")
    List<Tx> getTxs(@Nullable @Query("tx_hash") String sinceTxHash);

    @FormUrlEncoded
    @POST("/open/tx/request")
    BooleanResult postTxs(@Field("coin_code") String coinCode,
                          @Nullable @Field("user_tx_no") String userTxNo,
                          @Nullable @Field("outs") String outs,
                          @Nullable @Field("vc_code") String vcCode,
                          @Nullable @Field("is_dynamic_fee") Integer isDynamicFee,
                          @Nullable @Field("c_id") Integer cId);

    @GET("/open/tx/{user_tx_no}")
    TxStatus getTxStatus(@Path("user_tx_no") String userTxNo);

    @GET("/open/address/batch/{batch_index}")
    List<Address> getAddressBatch(@Path("batch_index") Integer batchIndex);
}
