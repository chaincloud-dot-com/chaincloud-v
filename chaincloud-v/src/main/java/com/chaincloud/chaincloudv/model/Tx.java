package com.chaincloud.chaincloudv.model;

import com.chaincloud.chaincloudv.ChainCloudVApplication_;
import com.chaincloud.chaincloudv.R;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by songchenwen on 16/1/11.
 */
public class Tx implements Serializable{
    private String txHash;
    private Date txAt;
    private Date confirmAt;
    private int confirmation;
    private long value;
    private long gas;
    private long gasPrice;
    private long gasUsed;


    private List<In> inputs;
    private List<Out> outputs;

    public Tx(String hash){
        this.txHash = hash;
    }

    public String getTxHash() {
        return txHash;
    }

    public Date getTxAt() {
        return txAt;
    }

    public Date getConfirmAt() {
        return confirmAt;
    }

    public int getConfirmation() {
        return confirmation;
    }

    public long getValue() {
        return value;
    }

    public long getGas() {
        return gas;
    }

    public void setGas(long gas) {
        this.gas = gas;
    }

    public long getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(long gasPrice) {
        this.gasPrice = gasPrice;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed = gasUsed;
    }

    public List<In> getInputs() {
        return inputs;
    }

    public List<Out> getOutputs() {
        return outputs;
    }

    public static class In implements Serializable {
        private int sn;
        private long value;
        private String address;
        private String prevTxHash;
        private String prevOutSn;
        private boolean isMine;

        public int getSn() {
            return sn;
        }

        public long getValue() {
            return value;
        }

        public String getAddress() {
            return address;
        }

        public String getPrevTxHash() {
            return prevTxHash;
        }

        public String getPrevOutSn() {
            return prevOutSn;
        }

        public boolean isMine() {
            return isMine;
        }
    }

    public static class Out implements Serializable {
        public enum OutStatus {
            @SerializedName("0")
            Unspent(0),
            @SerializedName("1")
            Spent(1);

            private int value;

            OutStatus(int value) {
                this.value = value;
            }

            public int value() {
                return value;
            }

            public int nameRes() {
                switch (this) {
                    case Unspent:
                        return R.string.tx_out_status_unspent_name;
                    case Spent:
                        return R.string.tx_out_status_spent_name;
                    default:
                        return 0;
                }
            }

            public String displayName() {
                int nameRes = nameRes();
                if (nameRes == 0) {
                    return null;
                }
                return ChainCloudVApplication_.getInstance().getString(nameRes);
            }
        }

        private int sn;
        private long value;
        private String address;
        private OutStatus status;
        private boolean isMine;

        public int getSn() {
            return sn;
        }

        public long getValue() {
            return value;
        }

        public String getAddress() {
            return address;
        }

        public OutStatus getStatus() {
            return status;
        }

        public boolean isMine() {
            return isMine;
        }
    }
}
