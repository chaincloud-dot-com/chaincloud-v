package com.chaincloud.chaincloudv.api.result;

import java.util.Date;

/**
 * Created by zhumingu on 16/7/25.
 */
public class TxResult {
    public String vtestInfo;
    public Date vtestAt;
    public String vtestId;

    public Info info;
    public String sign;
    public String coinCode = "BTC";
    public Integer cId;

    public final class Info{
        public String outs;
        public int dynamic;
    }
}
