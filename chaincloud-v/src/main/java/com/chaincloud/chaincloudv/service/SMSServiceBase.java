package com.chaincloud.chaincloudv.service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import com.chaincloud.chaincloudv.event.SmsPing;
import com.chaincloud.chaincloudv.event.SwitchSmsObserverType;
import com.chaincloud.chaincloudv.event.UpdateWorkState;
import com.chaincloud.chaincloudv.preference.Preference_;
import com.chaincloud.chaincloudv.util.DateTimeUtil;
import com.chaincloud.chaincloudv.util.SMSCommandUtil;
import com.chaincloud.chaincloudv.util.SMSUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

import de.greenrobot.event.EventBus;

/**
 * Created by zhumingu on 16/7/19.
 */
public abstract class SMSServiceBase extends Service {

    private static final Logger log = LoggerFactory.getLogger(SMSServiceBase.class);

    private final String TAG = "SMSServiceBase";

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    private final String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    private final String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
    private final String ALARM = "com.chaincloud.chaincloudv.service.alarm";
    private final Uri OBSERVER_URI = Uri.parse("content://sms/");
    private final Uri QUERY_URI = Uri.parse("content://sms/inbox");
    private final String[] PROJECTION = new String[]{
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY
//            Telephony.Sms.DATE,
//            Telephony.Sms.TYPE,
//            Telephony.Sms.STATUS,
//            Telephony.Sms.READ
    };

    private boolean isKeepChannelOpen = false;
    private Integer smsObserverType;

    private BroadcastReceiver smsReceiver;
    private ContentObserver mSmsContentObserver;


    protected PendingIntent sendIntent, backIntent;

    protected String hcPhoneNo1;
    protected String hcPhoneNo2;
    protected String vAdminPhoneNo;


    @Override
    public void onCreate() {
        super.onCreate();

        loadPreferrence();

        initBroadcastReceiver();

        initSMSObserver();

        startUpdateChannelTask();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        destroySmsObserver();

        unregisterReceiver(sendReportReceiver);
        unregisterReceiver(deliverReportReceiver);
        unregisterReceiver(alarmReceiver);

        EventBus.getDefault().unregister(this);
    }

    public void onEventBackgroundThread(SwitchSmsObserverType switchSmsObserverType){
        smsObserverType = switchSmsObserverType.smsObserverType;

        destroySmsObserver();
        initSMSObserver();
    }


    abstract void handleSMS(String phoneNo, String content);

    abstract void createChannelSMS();


    private void initSMSObserver() {
        if (smsObserverType == null) {
            smsObserverType = new Preference_(getApplicationContext()).smsObserverType().get();
        }else {
            new Preference_(getApplicationContext()).edit().smsObserverType().put(smsObserverType).apply();
        }

        if (smsObserverType == 1){// db observer
            createSMSObserver();

            getContentResolver()
                    .registerContentObserver(OBSERVER_URI, true, mSmsContentObserver);
        }else{// broadcast receiver
            createSMSReceiver();

            IntentFilter intentFilter = new IntentFilter(SMS_RECEIVED);
            intentFilter.setPriority(Integer.MAX_VALUE);
            registerReceiver(smsReceiver, intentFilter);
        }
    }

    private void destroySmsObserver(){
        if (smsObserverType == 1){// broadcast receiver
            unregisterReceiver(smsReceiver);
            smsReceiver = null;
        }else{// db observer
            getContentResolver().unregisterContentObserver(mSmsContentObserver);
            mSmsContentObserver = null;
        }
    }

    protected void initBroadcastReceiver() {
        //sendReportReceiver
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        sendIntent =
                PendingIntent.getBroadcast(getApplicationContext(), 0, sentIntent, 0);

        registerReceiver(sendReportReceiver, new IntentFilter(SENT_SMS_ACTION));

        //deliverReportReceiver
        Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
        backIntent =
                PendingIntent.getBroadcast(getApplicationContext(), 0, deliverIntent, 0);

        registerReceiver(deliverReportReceiver, new IntentFilter(DELIVERED_SMS_ACTION));

        registerReceiver(alarmReceiver, new IntentFilter(ALARM));
    }

    protected void loadPreferrence() {
        Preference_ preference = new Preference_(getApplicationContext());


        vAdminPhoneNo = preference.vAdminPhoneNo().get();
        hcPhoneNo1 = preference.chaincloudPhoneNo1().get();
        hcPhoneNo2 = preference.chaincloudPhoneNo2().get();
    }

    protected boolean isTimeOk() {
        if (isKeepChannelOpen){
            return true;
        }

        Calendar calendar = Calendar.getInstance();
        int currHour = calendar.get(Calendar.HOUR);
        int amPm = calendar.get(Calendar.AM_PM);

        if ((amPm == 1 && currHour >= 11 && currHour <= 12 )
                || (amPm == 0 && currHour >= 0 && currHour <= 1)){

            return true;
        }

        return false;
    }

    private void startUpdateChannelTask(){
        Intent intent = new Intent(ALARM);
        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(getApplicationContext(), 0, intent, 0);

        /*** update channel in 00:00 ***/
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 24);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.ZONE_OFFSET, 8 * 3600000);

        long firstime = calendar.getTimeInMillis();
        long period = 24 * 3600 * 1000;//a day

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, firstime, period, pendingIntent);
    }

    private void handleSMSAdmin(String phoneNo, String content){
        String[] splits = content.split(":");

        if (splits.length == 1){
            if (splits[0].equals("START_LOOP")){
                EventBus.getDefault().post(new UpdateWorkState(UpdateWorkState.Type.StartLoop));

                SMSUtil.sendSMS(phoneNo, "the loop is started", sendIntent, backIntent);
            }else if (splits[0].equals("STOP_LOOP")){
                EventBus.getDefault().post(new UpdateWorkState(UpdateWorkState.Type.StopLoop));

                SMSUtil.sendSMS(phoneNo, "the loop is stopped", sendIntent, backIntent);
            }else if (splits[0].equals("PING")){
                EventBus.getDefault().post(new SmsPing(phoneNo));
            }else if (splits[0].equals("TIME")){
                SMSUtil.sendSMS(
                        phoneNo,
                        SMSCommandUtil.getVTime(DateTimeUtil.getDateTimeString(new Date())),
                        sendIntent, backIntent);
            }else {
                String msg = "sms format is error";

                log.error(msg);
                SMSUtil.sendSMS(phoneNo, msg, null, null);
            }
        }
    }

    private void dispatchHandleSms(String sms, String fromNumber){
        if (sms != null && sms.trim().length() > 0){

            if (sms.startsWith("CC:")) {

                sms = sms.substring(3);

                if(fromNumber.contains(vAdminPhoneNo)){
                    handleSMSAdmin(fromNumber, sms);
                }else {
                    handleSMS(fromNumber, sms);
                }
            }else {
                if (!sms.toLowerCase().contains("sms format is error")){
                    String msg = "sms format is error";

                    log.error(msg);
                    SMSUtil.sendSMS(fromNumber, msg, null, null);
                }
            }
        }
    }


    //region smsReceiver listener received sms
    private void createSMSReceiver(){
        smsReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle bundle = intent.getExtras();

                if (null != bundle) {
                    Object[] smsObj = (Object[]) bundle.get("pdus");
                    for (Object object : smsObj) {
                        SmsMessage msg = SmsMessage.createFromPdu((byte[]) object);

                        final String fromNumber = msg.getOriginatingAddress();
                        final String body = msg.getMessageBody();

                        log.info("receive a msg and fromNumber:" + fromNumber + "...msg:" + body);

                        if ((hcPhoneNo1.length() > 0 && fromNumber.contains(hcPhoneNo1))
                                || (hcPhoneNo2.length() > 0 && fromNumber.contains(hcPhoneNo2))
                                || (vAdminPhoneNo.length() > 0 && fromNumber.contains(vAdminPhoneNo))) {

                            dispatchHandleSms(body, fromNumber);
                        }
                    }
                }
            }

        };
    }
    //endregion

    //region SMSObserver
    private void createSMSObserver(){
        mSmsContentObserver = new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);

                ContentResolver cr = getContentResolver();
                String where = " (address ='+86" + hcPhoneNo1
                        + "' or address ='+86" + hcPhoneNo2
                        + "' or address ='+86" + vAdminPhoneNo + "') AND read = 0";

                Cursor cur = cr.query(QUERY_URI, PROJECTION, where, null, "date asc");
                if (null == cur)
                    return;
                if (cur.moveToNext()) {
                    String ID = cur.getString(cur.getColumnIndex(Telephony.Sms._ID));//id
                    String number = cur.getString(cur.getColumnIndex(Telephony.Sms.ADDRESS));//手机号
                    String body = cur.getString(cur.getColumnIndex(Telephony.Sms.BODY));
//                String data = cur.getString(cur.getColumnIndex(Telephony.Sms.DATE));
//                String type = cur.getString(cur.getColumnIndex(Telephony.Sms.TYPE));
//                String status = cur.getString(cur.getColumnIndex(Telephony.Sms.STATUS));
//                String read = cur.getString(cur.getColumnIndex(Telephony.Sms.READ));

                    ContentValues values = new ContentValues();
                    values.put("read", "1");
                    cr.update(QUERY_URI, values, "_id=?", new String[]{ID});

                    log.info("receive a msg, phone number is " + number + ", msg is " + body);

                    dispatchHandleSms(body, number);
                }
            }
        };
    }
    //endregion


    //region sendReportReceiver
    BroadcastReceiver sendReportReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    log.info("send SMS successful");
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    log.error("send SMS failed");
                    break;
            }
        }
    };
    //endregion

    //region deliverReportReceiver
    BroadcastReceiver deliverReportReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context _context, Intent _intent) {
            log.info(TAG, "deliver SMS success");
        }
    };
    //endregion

    //region alarm receiver
    BroadcastReceiver alarmReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context _context, Intent _intent) {
            createChannelSMS();
            log.info("update channel task start..." + new Date().getMinutes());
        }
    };
    //endregion


    public class BaseSMSBinder extends Binder {

        public void reloadPreference(){
            loadPreferrence();
        }

        public void openSMSChannel(){
            isKeepChannelOpen = true;
        }

        public void closeSMSChannel(){
            isKeepChannelOpen = false;
        }
    }
}
