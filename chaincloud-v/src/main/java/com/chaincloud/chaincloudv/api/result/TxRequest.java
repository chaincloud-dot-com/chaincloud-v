package com.chaincloud.chaincloudv.api.result;

/**
 * Created by zhumingu on 16/7/25.
 */
public class TxRequest {
    public String coinCode;
    public String userTxNo;
    public String outs;
    public int isDynamicFee;

    public TxRequest(){}

    public TxRequest(String userTxNo){
        this.userTxNo = userTxNo;
    }
}
