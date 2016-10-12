package com.chaincloud.chaincloudv.util;

import android.util.Pair;

import com.chaincloud.chaincloudv.R;
import com.google.gson.annotations.SerializedName;

/**
 * Created by zhumingu on 16/9/19.
 */
public enum Coin {
    @SerializedName("BTC")
    BTC("BTC", 0x80, 0, 5, 0),
    @SerializedName("LTC")
    LTC("LTC", 0xb0, 0x30, 5, 2),
    @SerializedName("DOGE")
    DOGE("DOGE", 0x9e, 0x1e, 0x16, 3);

    private String code;
    private int wif;
    private int address;
    private int payToScript;
    private int pathNumber;

    Coin(String code, int wif, int address, int payToScript, int pathNumber) {
        this.code = code;
        this.wif = wif;
        this.address = address;
        this.payToScript = payToScript;
        this.pathNumber = pathNumber;
    }

    public static Coin fromValue(String value) {
        for (Coin t : Coin.values()) {
            if (t.code.equalsIgnoreCase(value)) {
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

    public int getSwitch() {
        switch (this){
            case BTC:
                return R.string.switch_to_ltc;
            default: LTC:
            return R.string.switch_to_btc;
        }
    }

    public Pair<Integer, String> getBlockChainInfo(){
        switch (this){
            case BTC:
                return new Pair(R.string.tx_detail_view_on_blockchain_tx_btc, "http://blockchain.info/tx/");
            case LTC:https://ltc.blockr.io/tx/info/
                return new Pair(R.string.tx_detail_view_on_blockchain_tx_ltc, "https://ltc.blockr.io/tx/info/");
            case DOGE:
                return new Pair(R.string.tx_detail_view_on_blockchain_tx_doge, "https://dogechain.info/tx/");
        }

        return null;
    }

    public String getCode() {
        return code;
    }

    public int getWif() {
        return wif;
    }

    public int getAddress() {
        return address;
    }

    public int getPayToScript() {
        return payToScript;
    }

    public int getPathNumber() {
        return pathNumber;
    }

//    public int path(HDSeed.Path path) {
//        return getPathNumber() * 2 + path.value();
//    }
}
