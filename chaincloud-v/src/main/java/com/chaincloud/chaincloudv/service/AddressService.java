package com.chaincloud.chaincloudv.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.chaincloud.chaincloudv.api.Api;
import com.chaincloud.chaincloudv.api.service.VWebService;
import com.chaincloud.chaincloudv.dao.AddressBatchDao;
import com.chaincloud.chaincloudv.dao.AddressDao;
import com.chaincloud.chaincloudv.dao.ChannelDao;
import com.chaincloud.chaincloudv.dao.ORMLiteDBHelper;
import com.chaincloud.chaincloudv.event.AddressBatchResult;
import com.chaincloud.chaincloudv.event.UpdateAddressBatchState;
import com.chaincloud.chaincloudv.model.Address;
import com.chaincloud.chaincloudv.model.AddressBatch;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.OrmLiteDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by zhumingu on 16/7/25.
 */
@EService
public class AddressService extends Service {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);


    private VWebService vWebService;

    private boolean isStartLoop;

    private AddressBinder addressBinder = new AddressBinder();

    private WorkService.MsgListener msgListener;

    SMSServiceImpl.SMSBinder binder;

    @OrmLiteDao(helper = ORMLiteDBHelper.class)
    ChannelDao channelDao;

    @OrmLiteDao(helper = ORMLiteDBHelper.class)
    AddressBatchDao addressBatchDao;

    @OrmLiteDao(helper = ORMLiteDBHelper.class)
    AddressDao addressDao;


    @Override
    public void onCreate() {
        super.onCreate();

        vWebService = Api.apiService(VWebService.class);
        bindService(new Intent(this, SMSServiceImpl_.class), conn, Service.BIND_AUTO_CREATE);

        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return addressBinder;
    }

    @Background
    void startLoopAddress(){
        isStartLoop = true;

        while (isStartLoop){
            sleep();

            showMsg("pull a batch address...");

            //1.pull address from vweb
            AddressBatchResult result = pullAddress();

            if (result == null){
                showMsg("batch address is empty");
                continue;
            }

            showMsg("get a batch address and save to db... " + result.addressType);

            //2.save db
            save2DB(result);

            showMsg("save to db success and send sms to check...");

            //3.check address
            checkAddress(result);
        }
    }

    public void onEvent(UpdateAddressBatchState state){
        showMsg("check complete and update the result of check " + state.status);

        updateBatchCheckStatus(state);
    }


    private void updateBatchCheckStatus(UpdateAddressBatchState state) {
        try {
            vWebService.postAddressBatchStatus(state.index, state.status, state.type);

            startLoopAddress();
        }catch (RetrofitError error){
            String msg = "";
            switch (error.getKind()) {
                case NETWORK:
                    msg = "network not access";
                    break;
                case HTTP:
                    msg = error.getResponse().getStatus() + "";
                    break;
            }
            log.error("pull address batch from vweb error " + msg);
        }
    }

    private void save2DB(AddressBatchResult data) {
        Integer batchNo = Integer.parseInt(data.batchNo);
        AddressBatch.Type addressType = AddressBatch.Type.fromValue(data.addressType);

        //if exist delete
        AddressBatch ab = addressBatchDao.getByIndex(batchNo, addressType);
        if (ab != null){
            addressDao.deleteByBatchId( ab.addressBatchId );
            addressBatchDao.delete(ab);
        }

        //save data
        AddressBatch addressBatch = new AddressBatch();
        addressBatch.index = batchNo;
        addressBatch.status = AddressBatch.Status.NULL;
        addressBatch.type = addressType;

        addressBatchDao.create(addressBatch);

        for (Address address : data.addressList){
            address.addressBatch = addressBatch;
        }

        addressDao.addBatch(data.addressList);
    }

    private void checkAddress(AddressBatchResult addressBatchResult) {
        binder.hotAddressCheck(
                Integer.parseInt(addressBatchResult.batchNo),
                AddressBatch.Type.fromValue(addressBatchResult.addressType));
    }

    private AddressBatchResult pullAddress() {
        try{
            return vWebService.getNextAddressBatch();
        }catch (RetrofitError error){
            String msg = "";
            switch (error.getKind()) {
                case NETWORK:
                    msg = "network not access";
                    break;
                case HTTP:
                    msg = error.getResponse().getStatus() + "";
                    break;
            }
            log.error("pull address batch from vweb error " + msg);
        }

        return null;
    }

    private void showMsg(String msg){
        log.info(msg);

        if (msgListener != null){
            msgListener.onMsgReceive(msg);
        }
    }

    private void sleep(){
        try {
            Thread.currentThread().sleep(1000 * 60 * 5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public class AddressBinder extends Binder{
        public void startLoop(){
            if (!isStartLoop) {
                startLoopAddress();
            }
        }

        public void stopLoop(){
            isStartLoop = false;
        }

        public void setListener(WorkService.MsgListener listener){
            msgListener = listener;
        }

        public void removeListener(){
            msgListener = null;
        }
    }


    //region ServiceConnection
    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (SMSServiceImpl.SMSBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder.setOnReceiveMsgListener(null);
            binder = null;
        }
    };
    //endregion
}
