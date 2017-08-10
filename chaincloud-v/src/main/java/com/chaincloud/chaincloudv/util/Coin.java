package com.chaincloud.chaincloudv.util;

import android.util.Pair;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.preference.Preference_;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

/**
 * Created by zhumingu on 16/9/19.
 */
public enum Coin {
    @SerializedName("BTC")
    BTC("BTC", 0x80, 0, 5, 0, 100000000),
    @SerializedName("BCC")
    BCC("BCC", 0x80, 0, 5, 145, 100000000),
    @SerializedName("LTC")
    LTC("LTC", 0xb0, 0x30, 5, 2, 100000000),
//    @SerializedName("ETH")
//    ETH("ETH", 0x9e, 0x1e, 0x16, 60, 1000000000000000000L);
    @SerializedName("DOGE")
    DOGE("DOGE", 0x9e, 0x1e, 0x16, 3, 100000000);

    private String code;
    private int wif;
    private int address;
    private int payToScript;
    private int pathNumber;
    private long unit;

    Coin(String code, int wif, int address, int payToScript, int pathNumber, long unit) {
        this.code = code;
        this.wif = wif;
        this.address = address;
        this.payToScript = payToScript;
        this.pathNumber = pathNumber;
        this.unit = unit;
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
            case BCC:
                return "฿ ";
            case LTC:
                return "Ł ";
//            case ETH:
//                return "Ξ ";
            case DOGE:
                return "Ð ";
        }

        return null;
    }

    public long getBalance(Preference_ preference){
        switch (this){
            case BTC:
                return preference.balanceBtc().get();
            case BCC:
                return preference.balanceBcc().get();
            case LTC:
                return preference.balanceLtc().get();
//            case ETH:
//                return preference.balanceEth().get();
            case DOGE:
                return preference.balanceDoge().get();
        }

        return 0;
    }

    public void setBalance(Preference_ preference, long balance){
        switch (this){
            case BTC:
                preference.edit().balanceBtc().put(balance).apply();
            case BCC:
                preference.edit().balanceBcc().put(balance).apply();
            case LTC:
                preference.edit().balanceLtc().put(balance).apply();
//            case ETH:
//                preference.edit().balanceEth().put(balance).apply();
            case DOGE:
                preference.balanceDoge().get();
        }
    }

    public long getBalanceThreshold(Preference_ preference){
        switch (this){
            case BTC:
                return preference.balanceThresholdBtc().get();
            case BCC:
                return preference.balanceThresholdBcc().get();
            case LTC:
                return preference.balanceThresholdLtc().get();
//            case ETH:
//                return preference.balanceThresholdEth().get();
            case DOGE:
                return preference.balanceThresholdDoge().get();
        }

        return 0;
    }

    public String showMoney(long money){
        return getSymbol() + new BigDecimal(money).divide(BigDecimal.valueOf(unit)).toPlainString();
    }

    public String showMoneyNoSymbol(long money){
        return new BigDecimal(money).divide(BigDecimal.valueOf(unit)).toPlainString();
    }

    public Pair<Integer, String> getBlockChainInfo(){
        switch (this){
            case BTC:
                return new Pair(R.string.tx_detail_view_on_blockchain_tx_btc, "http://blockchain.info/tx/");
            case BCC:
                return new Pair(R.string.tx_detail_view_on_blockchain_tx_bcc, "http://blockdozer.com/insight/tx/");
            case LTC:https://ltc.blockr.io/tx/info/
                return new Pair(R.string.tx_detail_view_on_blockchain_tx_ltc, "https://ltc.blockr.io/tx/info/");
//            case ETH:
//                return new Pair(R.string.tx_detail_view_on_blockchain_tx_eth, "https://etherscan.io/tx/");
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
