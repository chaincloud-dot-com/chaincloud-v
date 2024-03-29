package com.chaincloud.chaincloudv.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.chaincloud.chaincloudv.api.Api;
import com.chaincloud.chaincloudv.api.result.ApiError;
import com.chaincloud.chaincloudv.api.result.BooleanResult;
import com.chaincloud.chaincloudv.api.result.TxRequest;
import com.chaincloud.chaincloudv.api.result.TxResult;
import com.chaincloud.chaincloudv.api.result.TxStatus;
import com.chaincloud.chaincloudv.api.service.ChainCloudHotSendService;
import com.chaincloud.chaincloudv.api.service.VWebService;
import com.chaincloud.chaincloudv.dao.ChannelDao;
import com.chaincloud.chaincloudv.dao.ORMLiteDBHelper;
import com.chaincloud.chaincloudv.event.SmsPing;
import com.chaincloud.chaincloudv.event.UpdateVWebService;
import com.chaincloud.chaincloudv.event.UpdateWorkState;
import com.chaincloud.chaincloudv.model.Channel;
import com.chaincloud.chaincloudv.preference.Preference_;
import com.chaincloud.chaincloudv.util.Coin;
import com.chaincloud.chaincloudv.util.SMSUtil;
import com.chaincloud.chaincloudv.util.Validator;
import com.chaincloud.chaincloudv.util.crypto.BitcoinUtils;
import com.chaincloud.chaincloudv.util.crypto.ECKey;
import com.chaincloud.chaincloudv.util.crypto.EncryptedData;
import com.google.gson.Gson;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EService;
import org.androidannotations.ormlite.annotations.OrmLiteDao;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by zhumingu on 16/7/25.
 */
@EService
public class WorkService extends Service {

    private static final Logger log = LoggerFactory.getLogger(WorkService.class);

    public interface MsgListener{
        void onMsgReceive(String msg);
    }


    private VWebService vWebService;
    private ChainCloudHotSendService chainCloudHotSendService;
    private ChainCloudHotSendService chainCloudHotSendAltService;

    private WorkBinder workBinder = new WorkBinder();

    private MsgListener msgListener;

    private boolean isLoopTx;
    private boolean isLoopTxStatus;
    private boolean isPingLoop;

    private Gson gson = Api.gson;

    @OrmLiteDao(helper = ORMLiteDBHelper.class)
    ChannelDao channelDao;

    @Pref
    Preference_ preference;


    @Override
    public void onCreate() {
        super.onCreate();

        vWebService = Api.apiServiceAlt(VWebService.class, false);
        chainCloudHotSendService = Api.apiServiceAlt(ChainCloudHotSendService.class, false);
        chainCloudHotSendAltService = Api.apiServiceAlt(ChainCloudHotSendService.class, true);

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return workBinder;
    }

    @Background
    void startPullTxFromVWeb(boolean isOnceTxTest){
        isLoopTx = true;
        isLoopTxStatus = true;

        do {
            showMsg("pull a tx from vweb...");

            //1.pull tx from vweb
            TxResult encryptTx = getTxFromVWeb();
            if (encryptTx == null || encryptTx.vtestInfo == null ){

                showMsg("tx is empty...");
                if (isOnceTxTest){

                    isLoopTx = false;
                    return;
                }else {
                    continue;
                }
            }

            showMsg("decrypt tx...");

            //2.decrypt
            TxResult decryptTx = decrypt(encryptTx);
            if (decryptTx == null){

                isLoopTx = false;
                return;
            }

            showMsg("address valid...");
            //address valid and account send value
            if(decryptTx.info.outType != 8 &&  decryptTx.info.outType != 9 && !isAddressValid(decryptTx.info.outs, decryptTx.info.coinCode)){
                String msg ="tx out invalid";
                SMSUtil.sendSMS(preference.vAdminPhoneNo().get(), msg, null, null);
                showMsg(msg);

                isLoopTx = false;
                return;
            }

            showMsg("balance check...");
            //balance check
            if (!isBalanceEnough(decryptTx.info.coinCode)){

                isLoopTx = false;
                return;
            }

            showMsg("sign data...");
            //3.sign
            boolean isSuccess = setSignAndCId(decryptTx);
            if (!isSuccess){

                isLoopTx = false;
                return;
            }

            //4.send to chaincloud
            if (!preference.lastUserTxNo().getOr("").equals(decryptTx.vtestId)) {
                showMsg("send to chaincloud");
                isSuccess = postTxToChainCloud(decryptTx);
                if (!isSuccess) {
                    continue;
                }
            }

            //5.wait and loop chaincloud tx status
            TxStatus txStatus = null;
            while (isLoopTxStatus){
                showMsg("loop chaincloud tx status");

                txStatus = getTxStatusFromChainCloud(decryptTx.info.coinCode, decryptTx.vtestId);

                if (txStatus == null){
                    sleep();
                }else if( txStatus.hotWalletTxStatus == TxStatus.Status.Fail){
                    log.info("transaction failed：" + txStatus.txInfo);
                    showMsg("tx is failed");
                    break;
                }else if (txStatus.hotWalletTxStatus == TxStatus.Status.OK){
                    showMsg("tx is ok");
                    break;
                }else {
                    sleep();
                }
            }

            if (!isLoopTxStatus){

                isLoopTx = false;
                return;
            }

            //6.post tx status to vweb
            if (postStatus2VWeb(txStatus)) {

                showMsg("update tx status is ok");

                preference.edit()
                        .lastUserTxNo().put(null)
                        .apply();
            }else {
                showMsg("update tx status is error");
            }

            if (isOnceTxTest){

                isLoopTx = false;
                return;
            }
        }while(isLoopTx && sleep());
    }

    @Background
    void vcPing(MsgListener listener, String phoneNo){
        Channel okChannel = channelDao.getOkChannel();

        if (okChannel != null){
            showPingMsg("start ping...", listener);

            //txRequest
            TxRequest pingTxRequest = new TxRequest();
            pingTxRequest.outs = "PING";
            pingTxRequest.isDynamicFee = 1;
            pingTxRequest.coinCode = "BTC";
            pingTxRequest.userTxNo = UUID.randomUUID().toString();

            //sign
            String sign = sign(pingTxRequest, okChannel);

            //post ping
            boolean b;
            try {
                BooleanResult result = chainCloudHotSendService.postTxs(pingTxRequest.coinCode,
                        pingTxRequest.userTxNo, pingTxRequest.outs, sign,
                        pingTxRequest.isDynamicFee, okChannel.cId);

                if (result.result()){
                    b = true;
                }else {
                    String msg = "post tx to ChainCloud is error";

                    showPingMsg(msg, listener);
                    showSmsMsg(msg, phoneNo);
                    return;
                }
            }catch (RetrofitError error){
                String msg = "post tx to ChainCloud is error";

                showPingMsg(msg, listener);
                showSmsMsg(msg, phoneNo);
                return;
            }

            if (b){
                isPingLoop = true;
                while (isPingLoop){
                    TxStatus txStatus = null;
                    try {
                        txStatus = chainCloudHotSendService.getTxStatus(pingTxRequest.userTxNo);
                    }catch (RetrofitError error){
                        String msg = "get tx status from ChainCloud is error";

                        showPingMsg(msg, listener);
                        showSmsMsg(msg, phoneNo);
                    }

                    if (txStatus == null){
                        return;
                    }else if( txStatus.hotWalletTxStatus == TxStatus.Status.Fail){
                        String msg = "ping failed";

                        showPingMsg(msg, listener);
                        showSmsMsg(msg, phoneNo);
                        break;
                    }else if (txStatus.hotWalletTxStatus == TxStatus.Status.OK){
                        String msg = "ping is ok";

                        showPingMsg(msg, listener);
                        showSmsMsg(msg, phoneNo);
                        break;
                    }

                    sleep();
                }
            }
        }else {
            String msg = "SMS channel is not established, please first SMS channel...";

            showPingMsg(msg, listener);
            showSmsMsg(msg, phoneNo);
        }
    }

    public void onEventBackgroundThread(UpdateWorkState workState){
        if (workState.type == UpdateWorkState.Type.StartLoop){
            workBinder.startLoop();
        }else if (workState.type == UpdateWorkState.Type.StopLoop){
            workBinder.stopLoop();
        }
    }

    public void onEventBackgroundThread(UpdateVWebService UpdateVWebService){
        vWebService = Api.apiService(VWebService.class);
    }

    public void onEvent(SmsPing smsPing){
        vcPing(null, smsPing.phoneNo);
    }


    private void stopPullTxFromVWeb(){
        isLoopTxStatus = false;
        isLoopTx = false;
        isPingLoop = false;
    }

    private boolean sleep(){
        try {
            Thread.currentThread().sleep(preference.loopInternal().getOr(6) * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    private TxResult getTxFromVWeb(){
        try{
            TxResult txResult = vWebService.getNextUnSignTx();

            return txResult;
        }catch (RetrofitError error){
            log.error("pull tx from vweb error", error);
            showMsg("pull tx from vweb error");
        }

        return null;
    }

    private TxResult decrypt(TxResult encryptTx){
        try {
            String passwd = preference.passwdTransfer().get();

            String decryptStr;
            if (!TextUtils.isEmpty(passwd)) {
                byte[] decrypt = new EncryptedData(encryptTx.vtestInfo).decrypt(passwd);

                decryptStr = new String(decrypt);
            }else {
                decryptStr = encryptTx.vtestInfo;
            }
            encryptTx.info = gson.fromJson(decryptStr, TxResult.Info.class);
            if (encryptTx.info.coinCode == null){
                encryptTx.info.coinCode = "BTC";
            }

            return encryptTx;
        }catch (Exception e){
            log.error("decrypt error", e);
            SMSUtil.sendSMS(preference.vAdminPhoneNo().get(), "AES decrypt error", null, null);
            showMsg("decrypt error");
        }

        return null;
    }

    private BigInteger amount;
    private boolean isAddressValid(String outs, String coinCode) {

        if (Coin.isEtherToken(coinCode)) {
            coinCode = coinCode.split("-")[0];
        } else if (coinCode.startsWith("EOS-")){
            coinCode = "EOS-EOS";
        } else if (coinCode.startsWith("OMNI-")) {
            coinCode = coinCode.split("-")[1];
        } else if (coinCode.startsWith("TRX-")) {
            coinCode = "TRX-TRX";
        }

        if (outs != null && outs.length() > 0){
            String[] outsArr = outs.split(";");

            amount = BigInteger.ZERO;
            for (String out : outsArr){
                String[] addressValue = out.split(",");
                if (addressValue.length == 2){
                    try {
                        BigInteger value = new BigInteger(addressValue[1]);
                        if(!Validator.validAddress(Coin.fromValue(coinCode), addressValue[0])
                                || value.signum() < 0){
                            return false;
                        }

                        amount = amount.add(value);
                    }catch (Exception e){
                        return false;
                    }
                }else {
                    return false;
                }
            }
        }else {
            return false;
        }

        return true;
    }

    private boolean setSignAndCId(TxResult txResult){
        Channel okChannel = channelDao.getOkChannel();
        if (okChannel != null){
            txResult.cId = okChannel.cId;

            TxRequest txRequest = new TxRequest();
            txRequest.outs = txResult.info.outs;
            txRequest.isDynamicFee = txResult.info.dynamic;
            txRequest.coinCode = txResult.info.coinCode;
            txRequest.userTxNo = txResult.vtestId;
            txRequest.outType = txResult.info.outType;
            txRequest.memo = txResult.info.memo;

            txResult.sign = sign(txRequest, okChannel);

            Log.d("WorkService", gson.toJson(txRequest));

            return true;
        }else {
            log.error("channel is not established");
            showMsg("channel is not established");
        }

        return false;
    }

    private boolean postTxToChainCloud(TxResult txResult){
        try {
            BooleanResult result;
            if (txResult.info.coinCode.equals(Coin.BTC.getCode())){
                result = chainCloudHotSendService.postTxs(
                        txResult.info.coinCode,
                        txResult.info.coinCode,
                        txResult.vtestId,
                        txResult.info.outs,
                        txResult.sign,
                        txResult.info.dynamic,
                        txResult.info.confirmed,
                        txResult.cId,
                        txResult.info.outType,
                        txResult.info.memo,
                        txResult.info.nonce,
                        txResult.info.gasPrice);
            }else {
                result = chainCloudHotSendAltService.postTxs(
                        txResult.info.coinCode,
                        txResult.info.coinCode,
                        txResult.vtestId,
                        txResult.info.outs,
                        txResult.sign,
                        txResult.info.dynamic,
                        txResult.info.confirmed,
                        txResult.cId,
                        txResult.info.outType,
                        txResult.info.memo,
                        txResult.info.nonce,
                        txResult.info.gasPrice);
            }

            if (result.result()){
                preference.edit()
                        .lastUserTxNo().put(txResult.vtestId)
                        .apply();

                return true;
            }

            log.error("post tx to chaincloud is error");
        }catch (RetrofitError error){
            if (error.getKind() == RetrofitError.Kind.HTTP
                    && error.getResponse().getStatus() == 400){

                ApiError apiError = (ApiError) error.getBodyAs(ApiError.class);
                if (apiError != null) {
                    if (apiError.code == 3) {
                        return true;
                    }
                }
            }
            log.error("post tx to chaincloud is error", error);
        }

        showMsg("post tx to chaincloud is error");

        return false;
    }

    private TxStatus getTxStatusFromChainCloud(String coinCode, String userTxNo){
        try {
            TxStatus txStatus;
            if (coinCode.equals(Coin.BTC.getCode())) {
                txStatus = chainCloudHotSendService.getTxStatus(coinCode, userTxNo);
            }else {
                txStatus = chainCloudHotSendAltService.getTxStatus(coinCode, userTxNo);
            }

            return  txStatus;

        }catch (RetrofitError error){
            if (error.getKind() == RetrofitError.Kind.HTTP
                    && (error.getResponse().getStatus() == 400 || error.getResponse().getStatus() == 404)){

                ApiError apiError = (ApiError) error.getBodyAs(ApiError.class);

                String msg;
                if (apiError != null) {
                    msg = "error code" + apiError.code + ":" + apiError.message;
                }else {
                    msg = "error code 404";
                }

                log.error(msg);
                showMsg(msg);

                return new TxStatus(new TxRequest(userTxNo), TxStatus.Status.Fail, msg);
            }

            log.error("get tx status from chaincloud is error");
            showMsg("get tx status from chaincloud is error");
        }

        return null;
    }

    private boolean postStatus2VWeb(TxStatus txStatus){
        try{
            BooleanResult result = vWebService.postStatus(txStatus.sendRequest.userTxNo, txStatus.txHash);

            return result.result();
        }catch (RetrofitError error){
            log.error("post tx status to vweb is error", error);
            showMsg("post tx status to vweb is error");

            return false;
        }
    }

    private String sign(TxRequest txRequest, Channel okChannel){
        ECKey ecKey = new ECKey(BitcoinUtils.hexStringToByteArray(okChannel.dh),
                BitcoinUtils.hexStringToByteArray(okChannel.qh));

        return ecKey.signMessage(txRequest.toString());
    }

    private long getBalanceFromNet(String coin){
        showMsg(coin + " get balance... ");

        try {
            return chainCloudHotSendService.currentUser(coin).getBalance();
        }catch (RetrofitError error){
            error.printStackTrace();
            showMsg(coin + " get balance error " + error.getKind().name());
            return -1;
        }
    }

    private boolean isBalanceEnough(String coinCode){
        Coin coin = Coin.fromValue(coinCode);
        long balanceThreshold = coin.getBalanceThreshold(preference);
        if (balanceThreshold > 0 && new BigInteger((coin.getBalance(preference) - balanceThreshold) + "").compareTo(amount) <= 0){
            long balance = getBalanceFromNet(coin.getCode());
            if (balance >= 0){
                if (new BigInteger(balance - balanceThreshold + "").compareTo(amount) <= 0){
                    if (amount.compareTo(new BigInteger(balance + "")) > 0){
                        String msg = "balance is not enough and loop is stop";
                        showMsg(msg);
                        if (!preference.vAdminPhoneNo().getOr("").isEmpty()) {
                            showSmsMsg(msg, preference.vAdminPhoneNo().get());
                        }

                        coin.setBalance(preference, balance);

                        return false;
                    }else {
                        String msg = "Balance has reached the minimum limit, please recharge as soon as possible";
                        showMsg(msg);
                        if (!preference.vAdminPhoneNo().getOr("").isEmpty()) {
                            showSmsMsg(msg, preference.vAdminPhoneNo().get());
                        }

                        coin.setBalance(preference, balance);
                    }
                }
            }
        }

        return true;
    }

    private void showMsg(String msg){
        log.info(msg);

        if (msgListener != null){
            msgListener.onMsgReceive(msg);
        }
    }

    private void showPingMsg(String msg, MsgListener listener){
        if (listener != null){
            listener.onMsgReceive(msg);
        }
    }

    private void showSmsMsg(String msg, String phoneNo){
        if (phoneNo != null){
            SMSUtil.sendSMS(phoneNo, msg, null, null);
        }
    }


    public class WorkBinder extends Binder {

        public void startLoop(){
            Channel okChannel = channelDao.getOkChannel();
            if (okChannel != null) {
                if(!isLoopTx) {
                    startPullTxFromVWeb(false);
                }
            }else {
                showMsg("channel is not established...");
            }
        }

        public void stopLoop(){
            stopPullTxFromVWeb();
        }

        public void startOnceTest(){
            Channel okChannel = channelDao.getOkChannel();
            if (okChannel != null) {
                startPullTxFromVWeb(true);
            }else {
                showMsg("channel is not established...");
            }
        }

        public void ping(MsgListener listener){
            vcPing(listener, null);
        }

        public void setMsgListener(MsgListener listener){
            msgListener = listener;
        }

        public void unBinder() {
            msgListener = null;
        }
    }
}
