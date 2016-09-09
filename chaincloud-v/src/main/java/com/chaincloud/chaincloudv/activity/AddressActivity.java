package com.chaincloud.chaincloudv.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.adapter.AddressAdapter;
import com.chaincloud.chaincloudv.dao.AddressDao;
import com.chaincloud.chaincloudv.dao.ORMLiteDBHelper;
import com.chaincloud.chaincloudv.model.Address;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by songchenwen on 16/4/6.
 */
@EActivity(R.layout.activity_address)
public class AddressActivity extends Activity {

    private AddressAdapter mAdapter;
    private ProgressDialog pd;

    @Extra
    Integer batchId;

    @Extra
    Integer batchIndex;

    @ViewById
    TextView tvTitle;

    @ViewById
    ListView lv;

    @OrmLiteDao(helper = ORMLiteDBHelper.class)
    AddressDao addressDao;


    @AfterViews
    void initViews() {
        initTitle();

        initLv();

        loadDataFromDB();
    }

    @Click
    void ibtnBack(){
        finish();
    }

    @Background
    void loadDataFromDB(){
        showProgress();
        try{
            List<Address> addresses = addressDao.getByBatchId(batchId);

            showData(addresses);
        }catch (Exception e){
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
        }finally {
            closeProgress();
        }
    }

    @UiThread
    void showData(List<Address> addresses) {
        mAdapter.updateData(addresses);
    }

    @UiThread
    void showProgress(){
        if(pd != null){
            pd.dismiss();
        }

        pd = ProgressDialog.show(this, null, getString(R.string.loading));
    }

    @UiThread
    void closeProgress(){
        pd.dismiss();
    }


    private void initTitle() {
        tvTitle.setText(String.format(getString(R.string.address_batch_index), batchIndex + 1));
    }

    private void initLv() {
        mAdapter = new AddressAdapter(this);
        lv.setAdapter(mAdapter);
    }
}
