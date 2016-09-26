package com.chaincloud.chaincloudv.event;

import com.chaincloud.chaincloudv.model.Address;

import java.util.List;

/**
 * Created by zhumingu on 16/8/16.
 */
public class AddressBatchResult {
    public String batchNo;
    public List<Address> addressList;
    public String coinType = "BTC";
    public Integer status;
    public Integer addressType;
}
