package com.chaincloud.chaincloudv.service;

import android.content.Intent;
import android.os.IBinder;

import com.chaincloud.chaincloudv.dao.AddressBatchDao;
import com.chaincloud.chaincloudv.dao.AddressDao;
import com.chaincloud.chaincloudv.dao.ChannelDao;
import com.chaincloud.chaincloudv.dao.ORMLiteDBHelper;
import com.chaincloud.chaincloudv.event.UpdateAddressBatchState;
import com.chaincloud.chaincloudv.event.UpdateChannelState;
import com.chaincloud.chaincloudv.model.Address;
import com.chaincloud.chaincloudv.model.AddressBatch;
import com.chaincloud.chaincloudv.model.Channel;
import com.chaincloud.chaincloudv.util.Coin;
import com.chaincloud.chaincloudv.util.SMSCommandUtil;
import com.chaincloud.chaincloudv.util.SMSUtil;
import com.chaincloud.chaincloudv.util.crypto.BitcoinUtils;
import com.chaincloud.chaincloudv.util.crypto.ECKey;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.OrmLiteDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by zhumingu on 16/7/19.
 */
@EService
public class SMSServiceImpl extends SMSServiceBase {

    private static final Logger log = LoggerFactory.getLogger(SMSServiceImpl.class);

    private OnReceiveMsgListener onReceiveMsgListener;

    private SMSBinder binder = new SMSBinder();

    @OrmLiteDao(helper = ORMLiteDBHelper.class)
    ChannelDao channelDao;


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Background
    @Override
    protected void handleSMS(String phoneNo, String content) {
        String[] splits = content.split(":");

        //address check sms
        if(handleSMSAddressCheck(splits)){
            return;
        }

        //channel update sms
        if (handleSMSChannel(splits)){
            return;
        }
    }

    @Background
    @Override
    void createChannelSMS(){
        if (hcPhoneNo1 != null && !hcPhoneNo1.trim().equals("")) {
            log.info("create channel and send sms...");

            Channel channel = createChannel();

            String command = SMSCommandUtil.getH2CExchange(channel.qh, channel.channelId);
            SMSUtil.sendSMS(hcPhoneNo1, command, sendIntent, backIntent);
        }else {
            String msg = "Please configure the phone number of open platform first";

            log.info(msg);
            showMsg(msg);
        }
    }


    private boolean handleSMSAddressCheck(String[] splits){
        if(splits.length == 5){
            if(splits[0].equals("HOT_ADDRESSES") || splits[0].equals("COLD_ADDRESSES")){

                AddressBatch.Type type = null;

                if(splits[0].equals("HOT_ADDRESSES")){
                    type = AddressBatch.Type.Hot;
                }else if(splits[0].equals("COLD_ADDRESSES")){
                    type = AddressBatch.Type.Cold;
                }

                String index = splits[1],
                        sign = splits[2],
                        cId = splits[3],
                        coinCode = splits[4];

                try {
                    checkAddress(Integer.parseInt(index), Integer.parseInt(cId), sign, type, Coin.fromValue(coinCode));
                    return true;
                }catch (NumberFormatException e){
                    String msg = "address check sms format is error";

                    log.error(msg);
                    SMSUtil.sendSMS(vAdminPhoneNo, msg, null, null);

                    return true;
                }
            }
        }

        return false;
    }

    private boolean handleSMSChannel(String[] splits) {

        //channel establish
        if(splits.length == 6){
            String qc, cId, hId;
            if(splits[0].equals("QC")){

                qc = splits[1];

                if(splits[2].equals("C_ID")){
                    cId = splits[3];

                    if (splits[4].equals("H_ID")){
                        hId = splits[5];

                        if (!canHandleChannel()){
                            String msg = "time is not ok";

                            log.error(msg);
                            SMSUtil.sendSMS(vAdminPhoneNo, "channel establish--" + msg, null, null);
                            showMsg(msg);
                            return true;
                        }

                        try {
                            Integer hIdInt = Integer.valueOf(hId);

                            updateChannelSMS(qc, hIdInt, Integer.valueOf(cId));

                            log.info("handle sms channel ok and send...");
                            return true;
                        }catch (Exception e){
                            String msg = "channel update sms format is error";

                            log.error(msg);
                            SMSUtil.sendSMS(vAdminPhoneNo, msg, null, null);

                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private void updateChannelSMS(String qc, Integer hId, Integer cId){
        log.info("update channel and send sms...");

        Channel channel = channelDao.queryForId(hId);
        Channel lastChannel = channelDao.getLastChannel();

        if(channel != null && lastChannel != null &&  lastChannel.channelId == hId){
            updateChannel(channel, qc, cId);
        }else {
            String msg = "channel is not exist";

            log.error(msg);
            SMSUtil.sendSMS(vAdminPhoneNo, "create channel failed-- channel is not exist", null, null);
            showMsg(msg);
            return;
        }

        String command = SMSCommandUtil.getH2COK(cId);
        SMSUtil.sendSMS(hcPhoneNo1, command, sendIntent, backIntent);

        EventBus.getDefault().post(new UpdateChannelState(cId));

        binder.closeSMSChannel();
    }

    private void updateChannel(Channel channel, String qc, Integer cId) {
        channel.createAt = new Date();
        channel.qc = qc;
        channel.cId = cId;
        channel.channelStatus = Channel.ChannelStatus.OK;

        channelDao.update(channel);
    }

    private Channel createChannel(){
        ECKey ecKey = ECKey.generateECKey(new SecureRandom());

        Channel channel = new Channel();
        channel.dh = BitcoinUtils.bytesToHexString(ecKey.getPrivKeyBytes()).toUpperCase();
        channel.qh = BitcoinUtils.bytesToHexString(ecKey.getPubKey()).toUpperCase();
        channel.channelType = Channel.ChannelType.HC;
        channel.requestAt = new Date();
        channel.channelStatus = Channel.ChannelStatus.Creating;

        channelDao.create(channel);

        return channel;
    }

    private boolean canHandleChannel() {
        if(isTimeOk()){
            return true;
        }

        Channel channel = channelDao.getLastChannel();
        if (channel == null || channel.channelStatus == Channel.ChannelStatus.Creating){
            return true;
        }

        return false;
    }

    protected void showMsg(String msg) {
        log.info(msg);

        if (onReceiveMsgListener != null){
            onReceiveMsgListener.onReceiveMsg(msg);
        }
    }

    //region address check
    private void checkAddress(int index, int cId, String sign, AddressBatch.Type type, Coin coin){
        AddressDao addressDao = getAddressDao();
        AddressBatchDao addressBatchDao = getAddressBatchDao();

        AddressBatch addressBatch = addressBatchDao.getByIndex(index, type, coin);
        List<Address> adresses = addressDao.getByBatchId(addressBatch.addressBatchId);

        try {
            String addressContent = getAddressContent(adresses);

            ECKey ecKey = ECKey.signedMessageToKey(addressContent, sign);
            String publicKey = BitcoinUtils.bytesToHexString(ecKey.getPubKey()).toUpperCase();

            Channel channel = channelDao.getByCId(cId);

            log.info("address check result " + "...qv:" + channel.qc + "...publicKey:" + publicKey);

            UpdateAddressBatchState uabs = new UpdateAddressBatchState();
            uabs.type = type.value();
            uabs.index = index;


            if(publicKey.equals(channel.qc)) {
                addressBatch.status = AddressBatch.Status.OK;
                addressBatchDao.update(addressBatch);

                uabs.status = AddressBatch.Status.OK.value();
            }else {
                addressBatch.status = AddressBatch.Status.ERROR;
                addressBatchDao.update(addressBatch);

                uabs.status = AddressBatch.Status.ERROR.value();
            }

            EventBus.getDefault().post(uabs);
        } catch (SignatureException e) {
            log.error(e.getMessage(), e);
            SMSUtil.sendSMS(vAdminPhoneNo, "address sign check exception", null, null);
            showMsg("address sign check exception");
        }
    }

    private String getAddressContent(List<Address> adresses) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < adresses.size(); i ++){
            sb.append(i + "," + adresses.get(i).address + ":");
        }

        return sb.toString();
    }

    private AddressDao getAddressDao(){
        try {
            ORMLiteDBHelper ormLiteDBHelper = OpenHelperManager.getHelper(this, ORMLiteDBHelper.class);

            return new AddressDao(((Dao<Address, Integer>) ormLiteDBHelper
                    .getDao(Address.class)));
        } catch (SQLException e) {
            log.error("Could not create DAO", e);
        }

        return null;
    }

    private AddressBatchDao getAddressBatchDao(){
        try {
            ORMLiteDBHelper ormLiteDBHelper = OpenHelperManager.getHelper(this, ORMLiteDBHelper.class);

            return new AddressBatchDao(((Dao<AddressBatch, Integer>) ormLiteDBHelper
                    .getDao(AddressBatch.class)));
        } catch (SQLException e) {
            log.error("Could not create DAO", e);
        }

        return null;
    }
    //endregion

    public interface OnReceiveMsgListener{
        void onReceiveMsg(String msg);
    }

    public class SMSBinder extends BaseSMSBinder {
        public void createChannel(){
            createChannelSMS();
        }

        public void setOnReceiveMsgListener(OnReceiveMsgListener listener){
            onReceiveMsgListener = listener;
        }

        public void hotAddressCheck(int index, AddressBatch.Type type, Coin coin){
            Channel okChannel = channelDao.getOkChannel();
            if (okChannel == null){
                String msg = "SMS channel is not established, please first SMS channel...";

                showMsg(msg);
                log.info(msg);
                return;
            }

            if (type == AddressBatch.Type.Hot){
                SMSUtil.sendSMS(hcPhoneNo1, SMSCommandUtil.getHotAddressCheck(index, coin.getCode()), sendIntent, backIntent);
            }else {
                SMSUtil.sendSMS(hcPhoneNo1, SMSCommandUtil.getColdAddressCheck(index, coin.getCode()), sendIntent, backIntent);
            }
        }
    }
}
