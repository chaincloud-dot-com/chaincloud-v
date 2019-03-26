package com.chaincloud.chaincloudv.util;

import android.util.Pair;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.preference.Preference_;
import com.chaincloud.chaincloudv.ui.base.iconfont.BitpieIcon;
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
    @SerializedName("QTUM")
    QTUM("QTUM", 0x80, "3a", "32", 65535, new BigInteger("100000000"), "quantum"),
    @SerializedName("BTG")
    BTG("BTG", 0x80, "26", "17", 156, new BigInteger("100000000"), "bitcoin gold"),
    @SerializedName("SBTC")
    SBTC("SBTC", 0x80, "00", "05", 8888, new BigInteger("100000000"), "super bitcoin"),
    @SerializedName("HSR")
    HSR("HSR", 0x80, "28", "64", 171, new BigInteger("100000000"), "Hcash"),
    @SerializedName("BCD")
    BCD("BCD", 0x80, "00", "05", 999, new BigInteger("10000000"), "bitcoin diamond"),
    @SerializedName("BTW")
    BTW("BTW", 0x80, "49", "1f", 777, new BigInteger("10000"), "bitcoin world"),
    @SerializedName("DASH")
    DASH("DASH", 0xcc, "4c", "10", 5, new BigInteger("100000000"), "dashcoin"),
    @SerializedName("BTF")
    BTF("BTF", 0x80, "24", "28", 9888, new BigInteger("100000000"), "bitcoinfaith"),
    @SerializedName("BTP")
    BTP("BTP", 0x80, "38", "3a", 8999, new BigInteger("10000000"), "bitcoinpay"),
    @SerializedName("BTN-NEW")
    BTN("BTN-NEW", 0x80, "00", "05", 1000, new BigInteger("100000000"), "bitcoinnew"),
    @SerializedName("BPA")
    BPA("BPA", 0x80, "37", "50", 6666, new BigInteger("100000000"), "bitcoinpizza"),
    @SerializedName("BBC")
    BBC("BBC", 0x80, "19", "55", 1111, new BigInteger("10000000"), "bigbitcoin"),
    @SerializedName("BTV-NEW")
    BTV("BTV-NEW", 0x80, "00", "05", 7777, new BigInteger("100000000"), "bitvote"),
    @SerializedName("CDY")
    CDY("CDY", 0x80, "1c", "58", 1145, new BigInteger("100000"), "candy"),
    @SerializedName("LCH")
    LCH("LCH", 0xb0, "30", "05", 189, new BigInteger("100000000"), "litecoincash"),
    @SerializedName("SAFE")
    SAFE("SAFE", 0xcc, "4c", "10", 6688, new BigInteger("100000000"), "safe"),
    @SerializedName("BCX")
    BCX("BCX", 0x80, "4b", "3f", 1688, new BigInteger("10000"), "bitcoinx"),
    @SerializedName("LBTC")
    LBTC("LBTC", 0x80, "00", "05", 998, new BigInteger("100000000"), "lightningbitcoin"),
    @SerializedName("EOS-EOS")
    EOS("EOS-EOS", 0x80, "", "", 194, new BigInteger("1000000"), "eos"),
    @SerializedName("BTM-BTM")
    BTM("BTM-BTM", 0x80, "", "", 153, new BigInteger("100000000"), "bytom"),
    @SerializedName("BCHSV")
    BCHSV("BCHSV", 0x80, "00", "05", 236, new BigInteger("100000000"), "bchsv"),
    @SerializedName("TRX-TRX")
    TRX("TRX-TRX", 0x80, "", "", 195, new BigInteger("1000000"), "trx");


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
                return "{fa-ltc} ";
            case ETH:
                return "{fa-eth} ";
            case ETC:
                return "{fa-etc} ";
            case DOGE:
                return "{fa-doge} ";
            case BCC:
                return "{fa-bcc} ";
            case ZEC:
                return "{fa-zec} ";
            case QTUM:
                return "{fa-qtum} ";
            case BTG:
                return "{fa-btg} ";
            case SBTC:
                return "{fa-sbtc} ";
            case BTW:
                return "{fa-btw} ";
            case HSR:
                return "{fa-hsr} ";
            case BCD:
                return "{fa-bcd} ";
            case DASH:
                return "{fa-dash} ";
            case BTF:
                return "{fa-btf} ";
            case BTP:
                return "{fa-btp} ";
            case BTN:
                return "{fa-btn} ";
            case BPA:
                return "{fa-bpa} ";
            case BBC:
                return "{fa-bbc} ";
            case BTV:
                return "{fa-btv} ";
            case CDY:
                return "{fa-cdy} ";
            case LCH:
                return "{fa-lch} ";
            case SAFE:
                return "{fa-safe} ";
            case BCX:
                return "{fa-bcx} ";
            case LBTC:
                return "{fa-lbtc} ";
            case BCHSV:
                return "{fa-bchsv} ";
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
            case BTG:
                return new Pair(R.string.tx_detail_view_on_btgexp_tx_qtum, "http://btgexp.com/tx/");
            case SBTC:
                return new Pair(R.string.tx_detail_view_on_superbtc_tx_sbtc, "http://block.superbtc.org/tx/");
            case HSR:
                return new Pair(R.string.tx_detail_view_on_h_cash_hsr, "http://explorer.h.cash/tx/");
            case BCD:
                return new Pair(R.string.tx_detail_view_on_btcd_tx_bcd, "http://explorer.btcd.io/#/TX?loading=true&TX=");
            case DASH:
                return new Pair(R.string.tx_detail_view_on_dash_tx_dash, "http://explorer.dash.org/tx/");
            case BTF:
                return new Pair(R.string.tx_detail_view_on_dash_org, "http://exp.btceasypay.com/insight/tx/");
            case BTP:
                return new Pair(R.string.tx_detail_view_on_btp, "http://exp.btceasypay.com/insight/tx/");
            case BTN:
                return new Pair(R.string.tx_detail_view_on_btn, "http://explorer.btn.org/tx/");
            case BTV:
                return new Pair(R.string.tx_detail_view_on_btv, "https://block.bitvote.one/tx/");
            case BPA:
                return new Pair(R.string.tx_detail_view_on_dash_org, "https://explorer.dash.org/tx/");
            case CDY:
                return new Pair(R.string.tx_detail_view_on_cdy, "https://block.cdy.one/tx/");
            case SAFE:
                return new Pair(R.string.tx_detail_view_on_safe, "http://chain.anwang.com/tx/");
            case LCH:
                return new Pair(R.string.tx_detail_view_on_lch, "http://explorer.litecoincash.tech/tx/");
            case BBC:
                return new Pair(R.string.tx_detail_view_on_bbc, "http://blockchain.bigbitcoins.org/insight/tx/");
            case BCX:
                return new Pair(R.string.tx_detail_view_on_bcx, "https://bcx.info/tx/");
            case LBTC:
                return new Pair(R.string.tx_detail_view_on_lbtc, "http://explorer.lbtc.io/transinfo?param=");
            case BCHSV:
                return new Pair(R.string.tx_detail_view_on_bsv, "https://www.svblox.com/tx/");

        }

        return null;
    }

    public boolean isEther() {
        if (this == Coin.ETH || this == Coin.ETC){
            return true;
        }else {
            return false;
        }
    }

    public boolean isEOS() {
        if (this.getCode().startsWith("EOS")){
            return true;
        }else {
            return false;
        }
    }

    public boolean isBTM() {
        if (this.getCode().startsWith("BTM")){
            return true;
        }else {
            return false;
        }
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

    @Override
    public String toString() {
        return getCode();
    }
}
