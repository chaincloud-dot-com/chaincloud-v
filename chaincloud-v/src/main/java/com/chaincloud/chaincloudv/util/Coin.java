package com.chaincloud.chaincloudv.util;

import android.util.Pair;

import com.chaincloud.chaincloudv.R;
import com.google.gson.annotations.SerializedName;

/**
 * Created by zhumingu on 16/9/19.
 */
public enum Coin {
    @SerializedName("BTC")
    BTC("BTC"),
    @SerializedName("LTC")
    LTC("LTC"),
    @SerializedName("DOGE")
    DOGE("DOGE");

    private String code;


    Coin(String code) {
        this.code = code;
    }

    public static Coin fromValue(String value) {
        for (Coin t : Coin.values()) {
            if (t.code.equals(value)) {
                return t;
            }
        }
        return BTC;
    }

    public String getSymbol(){
        switch (this){
            case BTC:
                return "{fa-btc} ";
            case LTC:
                return "Ł ";
            case DOGE:
                return "Ð ";
        }

        return null;
    }

    public String getSwitch() {
        switch (this){
            case BTC:
                return "切换到LTC";
            default: LTC:
            return "切换到BTC";
        }
    }

    public Pair<Integer, String> getBlockChainInfo(){
        switch (this){
            case BTC:
                return new Pair(R.string.tx_detail_view_on_blockchain_tx_btc, "http://blockchain.info/tx/");
            case LTC:
                return new Pair(R.string.tx_detail_view_on_blockchain_tx_ltc, "http://explorer.litecoin.net/tx/");
            case DOGE:
                return new Pair(R.string.tx_detail_view_on_blockchain_tx_doge, "https://dogechain.info/tx/");
        }

        return null;
    }

    public String getCode() {
        return code;
    }
}
