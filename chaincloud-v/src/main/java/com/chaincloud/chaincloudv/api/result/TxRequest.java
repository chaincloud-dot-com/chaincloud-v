package com.chaincloud.chaincloudv.api.result;

/**
 * Created by zhumingu on 16/7/25.
 */
public class TxRequest {
    public String coinCode;
    public String userTxNo;
    public String outs;
    public int isDynamicFee;
    public int outType;

    public TxRequest(){}

    public TxRequest(String userTxNo){
        this.userTxNo = userTxNo;
    }

    @Override
    public String toString() {
        String result =  "{\"coin_code\":\""+ coinCode
                +"\",\"is_dynamic_fee\":"+ isDynamicFee
                +",\"outs\":\""+ outs
                +"\",\"user_tx_no\":\""+ userTxNo +"\"";

        if (outType != 0){
            result += ",\"outType\":" + outType;
        }
        result += "}";

        return result;
    }
}
