package com.chaincloud.chaincloudv.api.result;

import com.chaincloud.chaincloudv.R;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by zhumingu on 16/7/25.
 */
public class TxStatus {

    public enum Status {
        @SerializedName("0")
        Not(0),
        @SerializedName("1")
        OK(1),
        @SerializedName("9")
        Fail(9);

        private int value;

        Status(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static Status fromValue(int value) {
            for (Status status : Status.values()) {
                if (status.value() == value) {
                    return status;
                }
            }
            return Not;
        }

        public int nameRes() {
            switch (this) {
                case Not:
                    return R.string.tx_not;
                case OK:
                    return R.string.tx_ok;
                case Fail:
                    return R.string.tx_fail;
            }

            return 0;
        }
    }

    public Integer cId;
    public Integer userId;
    public TxRequest sendRequest;
    public String vcCode;
    public Date requestAt;
    public Integer hotWalletTx;
    public Status hotWalletTxStatus;
    public String txInfo;
    public String txHash;
}
