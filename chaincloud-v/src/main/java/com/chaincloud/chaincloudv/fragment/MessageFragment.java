package com.chaincloud.chaincloudv.fragment;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.api.Api;
import com.chaincloud.chaincloudv.api.result.BooleanResult;
import com.chaincloud.chaincloudv.api.result.TxRequest;
import com.chaincloud.chaincloudv.api.result.TxStatus;
import com.chaincloud.chaincloudv.api.service.ChainCloudHotSendService;
import com.chaincloud.chaincloudv.dao.ChannelDao;
import com.chaincloud.chaincloudv.dao.ORMLiteDBHelper;
import com.chaincloud.chaincloudv.event.UpdateChannelState;
import com.chaincloud.chaincloudv.model.Channel;
import com.chaincloud.chaincloudv.service.WorkService;
import com.chaincloud.chaincloudv.service.WorkService_;
import com.chaincloud.chaincloudv.ui.base.dialog.DialogAlert_;
import com.chaincloud.chaincloudv.util.DateTimeUtil;
import com.chaincloud.chaincloudv.util.crypto.BitcoinUtils;
import com.chaincloud.chaincloudv.util.crypto.ECKey;
import com.google.gson.Gson;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by zhumingu on 16/6/20.
 */
@EFragment(R.layout.fragment_message)
public class MessageFragment extends Fragment implements WorkService.MsgListener {

    private final static int MAX_LOG_LINE = 100;

    private WorkService.WorkBinder binder;

    @ViewById
    TextView tvChannelId, tvLog;

    @ViewById
    ScrollView svLog;


    @AfterViews
    void init(){
        initChannelId();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        EventBus.getDefault().unregister(this);
    }

    @Click
    void btnBind(){
        if(binder == null) {
            getActivity().bindService(new Intent(getActivity(), WorkService_.class),
                    conn, Service.BIND_AUTO_CREATE);
        }

        showMsg("bunding server...");
    }

    @Click
    void btnUnbind(){
        if(binder != null) {
            binder.unBinder();

            getActivity().unbindService(conn);

            binder = null;
        }

        showMsg("unbunding server...");
    }

    @Click
    void btnWorkStart(){
        if (binder != null) {
            binder.startLoop();

            showMsg("start loop...");
        }else {
            showMsg("Please bund server first");
        }
    }

    @Click
    void btnWorkStop(){
        DialogAlert_.builder()
                .msg(getString(R.string.dialog_close_loop_confirm))
                .build()
                .setRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (binder != null) {
                            binder.stopLoop();
                        }

                        showMsg("stop Loop...");
                    }
                })
                .show(getFragmentManager());
    }

    @Click
    void btnWorkTest(){
        if (binder != null) {
            binder.startOnceTest();

            showMsg("send once");
        }else {
            showMsg("Please bund server first");
        }
    }

    @Click
    void btnPing(){
        if (binder != null) {
            binder.ping(new WorkService.MsgListener() {
                @Override
                public void onMsgReceive(String msg) {
                    showMsg(msg);
                }
            });
        }else {
            showMsg("Please bund server first");
        }
    }

    @UiThread
    @Override
    public void onMsgReceive(String msg) {
        writeLog(msg);
    }

    @UiThread
    void showMsg(String msg){
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void onEventMainThread(UpdateChannelState updateChannelState) {
        tvChannelId.setText(String.valueOf(updateChannelState.cId));
    }


    private void initChannelId() {
        ORMLiteDBHelper helper = OpenHelperManager.getHelper(getContext(), ORMLiteDBHelper.class);
        try {
            ChannelDao channelDao =
                    new ChannelDao(((Dao<Channel, Integer> ) helper.getDao(Channel.class)));

            Channel okChannel = channelDao.getOkChannel();

            if (okChannel != null) {
                tvChannelId.setText(String.valueOf(okChannel.cId));
            }
        } catch (SQLException e) {
            Log.e("AddressBatchActivity_", "Could not create DAO addressBatchDao", e);
        }
    }

    public void writeLog(String data) {
        tvLog.append(DateTimeUtil.getHMSDateTimeString(new Date()) + "  " + data + "\n");
        // Erase excessive lines
        int excessLineNumber = tvLog.getLineCount() - MAX_LOG_LINE;
        if (excessLineNumber > 0) {
            int eolIndex = -1;
            CharSequence charSequence = tvLog.getText();
            for(int i=0; i<excessLineNumber; i++) {
                do {
                    eolIndex++;
                } while(eolIndex < charSequence.length() && charSequence.charAt(eolIndex) != '\n');
            }
            if (eolIndex < charSequence.length()) {
                tvLog.getEditableText().delete(0, eolIndex+1);
            }
            else {
                tvLog.setText("");
            }
        }

        svLog.fullScroll(ScrollView.FOCUS_DOWN);
    }


    //region ServiceConnection
    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (WorkService.WorkBinder) service;

            binder.setMsgListener(MessageFragment.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
        }
    };
    //endregion
}
