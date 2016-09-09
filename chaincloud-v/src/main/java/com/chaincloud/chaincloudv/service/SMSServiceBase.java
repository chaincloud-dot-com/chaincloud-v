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
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;

import com.chaincloud.chaincloudv.event.SmsPing;
import com.chaincloud.chaincloudv.event.UpdateWorkState;
import com.chaincloud.chaincloudv.preference.Preference_;
import com.chaincloud.chaincloudv.util.SMSUtil;
import com.chaincloud.chaincloudv.util.crypto.ECKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;

import de.greenrobot.event.EventBus;

/**
 * Created by zhumingu on 16/7/19.
 */
public abstract class SMSServiceBase extends Service {

    private static final Logger log = LoggerFactory.getLogger(SMSServiceBase.class);

    private final String TAG = "SMSServiceBase";

    private final String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    private final String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
    private final String ALARM = "com.chaincloud.chaincloudv.service.alarm";
    private final Uri OBSERVER_URI = Uri.parse("content://sms/");
    private final Uri QUERY_URI = Uri.parse("content://sms/inbox");

    private boolean isKeepChannelOpen = false;

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getContentResolver().unregisterContentObserver(mSmsContentObserver);

        unregisterReceiver(sendReportReceiver);
        unregisterReceiver(deliverReportReceiver);
        unregisterReceiver(alarmReceiver);
    }


    abstract void handleSMS(String phoneNo, String content);

    abstract void createChannelSMS();


    private void initSMSObserver() {
        getContentResolver()
                .registerContentObserver(OBSERVER_URI, true, mSmsContentObserver);
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
            }else if (splits[0].equals("STOP_LOOP")){
                EventBus.getDefault().post(new UpdateWorkState(UpdateWorkState.Type.StopLoop));
            }else if (splits[0].equals("PING")){
                EventBus.getDefault().post(new SmsPing(phoneNo));
            }else {
                String msg = "sms format is error";

                log.error(msg);
                SMSUtil.sendSMS(phoneNo, msg, null, null);
            }
        }
    }



    //region sendReportReceiver
    BroadcastReceiver sendReportReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Log.i(TAG, "send SMS successful");
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
            Log.i(TAG, "deliver SMS success");
        }
    };
    //endregion

    //region alarm receiver
    BroadcastReceiver alarmReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context _context, Intent _intent) {
            createChannelSMS();
            Log.i(TAG, "update channel task start..." + new Date().getMinutes());
        }
    };
    //endregion

    //region SMSObserver
    private static final String[] PROJECTION = new String[]{
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY
//            Telephony.Sms.DATE,
//            Telephony.Sms.TYPE,
//            Telephony.Sms.STATUS,
//            Telephony.Sms.READ
    };
    ContentObserver mSmsContentObserver = new ContentObserver(null) {
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

                log.info(TAG, "receive a msg, phone number is " + number + ", msg is " + body);

                if (body != null && body.trim().length() > 0){

                    if (body.startsWith("CC:")) {

                        body = body.substring(3);

                        if(number.contains(vAdminPhoneNo)){
                            handleSMSAdmin(number, body);
                        }else {
                            handleSMS(number, body);
                        }
                    }else {
                        String msg = "sms format is error";

                        log.error(msg);
                        SMSUtil.sendSMS(number, msg, null, null);
                    }
                }
            }
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
