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
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        vWebService = Api.apiService(VWebService.class);
        chainCloudHotSendService = Api.apiService(ChainCloudHotSendService.class);

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

        while (isLoopTx){
            sleep();

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
            //address valid
            if(!isAddressValid(decryptTx.info.outs, decryptTx.coinCode)){
                String msg ="tx out invalid";
                SMSUtil.sendSMS(preference.vAdminPhoneNo().get(), msg, null, null);
                showMsg(msg);

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

                txStatus = getTxStatusFromChainCloud(decryptTx.coinCode, decryptTx.vtestId);

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
            postStatus2VWeb(txStatus);

            showMsg("update tx status is ok");

            preference.edit()
                    .lastUserTxNo().put(null)
                    .apply();

            if (isOnceTxTest){

                isLoopTx = false;
                return;
            }
        }
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

    private void sleep(){
        try {
            Thread.currentThread().sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private TxResult getTxFromVWeb(){
        try{
            TxResult txResult = vWebService.getNextUnSignTx();

            return txResult;
        }catch (RetrofitError error){
            log.error("pull tx from vweb error");
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

            return encryptTx;
        }catch (Exception e){
            log.error("decrypt error");
            SMSUtil.sendSMS(preference.vAdminPhoneNo().get(), "AES decrypt error", null, null);
            showMsg("decrypt error");
        }

        return null;
    }

    private boolean isAddressValid(String outs, String coinCode) {

        if (outs != null && outs.length() > 0){
            String[] outsArr = outs.split(";");

            for (String out : outsArr){
                String[] addressValue = out.split(",");
                if (addressValue.length == 2){
                    try {
                        if(!Validator.validAddress(Coin.fromValue(coinCode), addressValue[0])
                                || Long.parseLong(addressValue[1]) <= 0){
                            return false;
                        }
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
            txRequest.coinCode = txResult.coinCode;
            txRequest.userTxNo = txResult.vtestId;

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
            BooleanResult result = chainCloudHotSendService.postTxs(
                    txResult.coinCode,
                    txResult.coinCode,
                    txResult.vtestId,
                    txResult.info.outs,
                    txResult.sign,
                    txResult.info.dynamic,
                    txResult.cId);

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
            TxStatus txStatus = chainCloudHotSendService.getTxStatus(coinCode, userTxNo);

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

    private void postStatus2VWeb(TxStatus txStatus){
        try{
            BooleanResult result = vWebService.postStatus(txStatus.sendRequest.userTxNo, txStatus.txHash);
        }catch (RetrofitError error){
            log.error("post tx status to vweb is error");
            showMsg("post tx status to vweb is error");
        }
    }

    private String sign(TxRequest txRequest, Channel okChannel){
        ECKey ecKey = new ECKey(BitcoinUtils.hexStringToByteArray(okChannel.dh),
                BitcoinUtils.hexStringToByteArray(okChannel.qh));

        return ecKey.signMessage(gson.toJson(txRequest));
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
