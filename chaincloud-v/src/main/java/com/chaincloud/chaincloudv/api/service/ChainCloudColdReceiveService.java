package com.chaincloud.chaincloudv.api.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chaincloud.chaincloudv.model.Address;
import com.chaincloud.chaincloudv.model.Tx;
import com.chaincloud.chaincloudv.model.User;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by zhumingu on 16/6/23.
 */
public interface ChainCloudColdReceiveService {

    @GET("/open/user")
    User currentUser();

    @GET("/open/address/batch/{batch_index}")
    List<Address> getAddressBatch(@Path("batch_index") Integer batchIndex);

    @GET("/open/tx")
    List<Tx> getTxs(@Nullable @Query("tx_hash") String sinceTxHash);

    @GET("/open/tx/detail/{tx_hash}")
    Tx getDetail(@NonNull @Path("tx_hash") String txHash);

    @GET("/open/address/history/{path}")
    List<Object> addressHistory(@Path("path") int path,
                                @Query("since_address") String sinceAddress);
}
