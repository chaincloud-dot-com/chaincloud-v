package com.chaincloud.chaincloudv.util;

import android.util.Pair;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.preference.Preference_;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by zhumingu on 16/9/19.
 */
public enum Coin {
    @SerializedName("BTC")
    BTC("BTC", 0x80, "00", "05", 0, new BigInteger("100000000"), "bitcoin"),
    @SerializedName("BCC")
    BCC("BCC", 0x80, "00", "05", 145, new BigInteger("100000000"), "bcash"),
    @SerializedName("LTC")
    LTC("LTC", 0xb0, "30", "05", 2, new BigInteger("100000000"), "litecoin"),
    @SerializedName("DOGE")
    DOGE("DOGE", 0x9e, "1e", "16", 3, new BigInteger("100000000"), "dogecoin"),
    @SerializedName("ETH")
    ETH("ETH", 0x9e, "1e", "16", 60, new BigInteger("1000000000000000000"), "Ethereum"),
    @SerializedName("ZEC")
    ZEC("ZEC", 0x80, "1cb8", "1cbd", 133, new BigInteger("100000000"), "zcash"),
    @SerializedName("ETC")
    ETC("ETC", 0x80, "1cb8", "1cbd", 61, new BigInteger("1000000000000000000"), "Ethereum Classic"),
    //    @SerializedName("ETH-OMG")
//    OMG("OMG", 0x9e, "1e", "16", 60, new BigInteger("1000000000000000000"), "OmiseGo"),
//    @SerializedName("ETH-PAY")
//    PAY("PAY", 0x9e, "1e", "16", 60, new BigInteger("1000000000000000000"), "TenXPay"),
//    @SerializedName("ETH-EOS")
//    EOS("EOS", 0x9e, "1e", "16", 60, new BigInteger("1000000000000000000"), "EOS"),
//    @SerializedName("ETH-BAT")
//    BAT("BAT", 0x9e, "1e", "16", 60, new BigInteger("1000000000000000000"), "BAT"),
//    @SerializedName("ETH-SNT")
//    SNT("SNT", 0x9e, "1e", "16", 60, new BigInteger("1000000000000000000"), "StatusNetwork"),
//    @SerializedName("ETH-1ST")
//    FST("1ST", 0x9e, "1e", "16", 60, new BigInteger("1000000000000000000"), "FirstBlood"),
    @SerializedName("QTUM")
    QTUM("QTUM", 0x80, "3a", "32", 65535, new BigInteger("100000000"), "quantum");

    private String code;
    private int wif;
    private String address;
    private String payToScript;
    private int pathNumber;
    private BigInteger unit;
    private String name;

    Coin(String code, int wif, String address, String payToScript, int pathNumber, BigInteger unit, String name) {
        this.code = code;
        this.wif = wif;
        this.address = address;
        this.payToScript = payToScript;
        this.pathNumber = pathNumber;
        this.unit = unit;
        this.name = name;
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
            case ETH:
                return "Ξ ";
            case ETC:
                return "⟠ ";
            case DOGE:
                return "Ð ";
            case BCC:
                return "฿ ";
            case ZEC:
                return "Z ";
            case QTUM:
                return "Q ";
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

    public String showMoney(long money, BigInteger moneyStr){
        if (this == Coin.ETH || this == Coin.ETC){
            return getSymbol() + new BigDecimal(moneyStr).divide(new BigDecimal(unit)).toPlainString();
        }else {
            return getSymbol() + new BigDecimal(money).divide(new BigDecimal(unit)).toPlainString();
        }
    }

    public Pair<Integer, String> getBlockChainInfo(){
        switch (this) {
            case BTC:
                return new Pair(R.string.tx_detail_view_on_blockchain_tx_btc, "http://blockchain.info/tx/");
            case BCC:
                return new Pair(R.string.tx_detail_view_on_blockdozer_tx_bcc, "http://blockdozer.com/insight/tx/");
            case LTC:
                return new Pair(R.string.tx_detail_view_on_blockchain_tx_ltc, "http://block.okcoin.cn/ltc/tx/");
            case DOGE:
                return new Pair(R.string.tx_detail_view_on_blockchain_tx_doge, "https://dogechain.info/tx/");
            case ETH:
                return new Pair(R.string.tx_detail_view_on_etherscan_tx_eth, "https://etherscan.io/tx/");
            case ZEC:
                return new Pair(R.string.tx_detail_view_on_explorer_zcha_in_tx_zec, "https://explorer.zcha.in/transactions/");
            case ETC:
                return new Pair(R.string.tx_detail_view_on_gastracker_io_etc, "https://gastracker.io/tx/");
//            case OMG:
//            case PAY:
//            case EOS:
//            case BAT:
//            case FST:
//            case SNT:
//                return new Pair(R.string.tx_detail_view_on_etherscan_tx_eth, "https://etherscan.io/tx/");
            case QTUM:
                return new Pair(R.string.tx_detail_view_on_qtuminfo_tx_qtum, "https://explorer.qtum.org/tx/");

        }

        return null;
    }

    public String getCode() {
        return code;
    }

    public int getWif() {
        return wif;
    }

    public String getAddressPrefix() {
        return address;
    }

    public String getPayToScriptPrefix() {
        return payToScript;
    }

    public int getPathNumber() {
        return pathNumber;
    }

//    public int path(HDSeed.Path path) {
//        return getPathNumber() * 2 + path.value();
//    }
}
