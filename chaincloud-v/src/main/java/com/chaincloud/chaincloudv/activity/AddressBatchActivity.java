package com.chaincloud.chaincloudv.activity;

import android.app.ProgressDialog;
import android.support.v4.app.FragmentActivity;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.adapter.BatchAdapter;
import com.chaincloud.chaincloudv.api.Api;
import com.chaincloud.chaincloudv.api.service.ChainCloudColdReceiveService;
import com.chaincloud.chaincloudv.api.service.ChainCloudHotSendService;
import com.chaincloud.chaincloudv.dao.AddressBatchDao;
import com.chaincloud.chaincloudv.dao.AddressDao;
import com.chaincloud.chaincloudv.dao.ORMLiteDBHelper;
import com.chaincloud.chaincloudv.model.AddressBatch;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by zhumingu on 16/6/23.
 */
@EActivity(R.layout.activity_address_batch)
public class AddressBatchActivity extends FragmentActivity {

    private ProgressDialog pd;
    BatchAdapter mAdapter;

    ChainCloudHotSendService chainCloudHotSendService;
    ChainCloudColdReceiveService chainCloudColdReceiveService;

    @Extra
    AddressBatch.Type type;

    @ViewById
    TextView tvTitle;

    @ViewById(R.id.lv)
    ListView lv;

    @OrmLiteDao(helper = ORMLiteDBHelper.class)
    AddressBatchDao addressBatchDao;

    @OrmLiteDao(helper = ORMLiteDBHelper.class)
    AddressDao addressDao;


    @AfterViews
    void init(){
        if (type == AddressBatch.Type.Hot) {
            chainCloudHotSendService = Api.apiService(ChainCloudHotSendService.class);
        }else {
            chainCloudColdReceiveService = Api.apiService(ChainCloudColdReceiveService.class);
        }

        initTitle();

        initLv();

        loadDataFromDB();
    }

    @Click(R.id.ibtn_back)
    void clickBack(){
        finish();
    }

    @ItemClick(R.id.lv)
    void clickItem(int position){
        AddressActivity_.intent(this)
                .batchId(mAdapter.getItem(position).addressBatchId)
                .batchIndex(position)
                .start();
    }

    @Background
    void loadDataFromDB() {
        showProgress();

        try{
            List<AddressBatch> addressBatches = addressBatchDao.getByType(type);

            showData(addressBatches);
        }finally {
            closeProgress();
        }
    }

    @UiThread
    void showData(List<AddressBatch> data){
        mAdapter.updateData(data);
    }

    @UiThread
    void showProgress(){
        if(pd != null){
            pd.dismiss();
        }

        pd = ProgressDialog.show(this, null, getString(R.string.loading));
        pd.setCancelable(true);
    }

    @UiThread
    void closeProgress(){
        if(pd != null) {
            pd.dismiss();
            pd = null;
        }
    }

    @UiThread
    void showPrompt(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    private void initTitle() {
        if (type == AddressBatch.Type.Hot) {
            tvTitle.setText(R.string.address_hot_send);
        }else {
            tvTitle.setText(R.string.address_cold_receive);
        }
    }

    private void initLv() {
        mAdapter = new BatchAdapter(this);

        lv.setAdapter(mAdapter);
    }
}
