package com.chaincloud.chaincloudv.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.chaincloud.chaincloudv.GlobalParams;
import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.adapter.TxAdapter;
import com.chaincloud.chaincloudv.api.Api;
import com.chaincloud.chaincloudv.api.service.ChainCloudColdReceiveService;
import com.chaincloud.chaincloudv.api.service.ChainCloudHotSendService;
import com.chaincloud.chaincloudv.model.AddressBatch;
import com.chaincloud.chaincloudv.model.Path;
import com.chaincloud.chaincloudv.model.Tx;
import com.chaincloud.chaincloudv.model.User;
import com.chaincloud.chaincloudv.ui.base.AddressDetailHeader;
import com.chaincloud.chaincloudv.ui.base.DialogWithActions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by zhumingu on 16/6/23.
 */
@EActivity(R.layout.activity_address_detail)
public class AddressDetailActivity extends FragmentActivity implements SwipeRefreshLayout.OnRefreshListener {

    private boolean hasMore = true;
    private boolean isLoding = false;
    private boolean isRefresh = true;

    private ProgressDialog pd;

    TxAdapter mAdapter;
    ChainCloudHotSendService chainCloudHotSendService;
    ChainCloudColdReceiveService chainCloudColdReceiveService;
    AddressDetailHeader headView;

    @Extra
    User user;

    @Extra
    boolean isHot;

    @ViewById(R.id.refresher)
    SwipeRefreshLayout refresher;

    @ViewById(R.id.lv)
    ListView lv;


    @AfterViews
    void init(){
        if (isHot) {
            chainCloudHotSendService = Api.apiService(ChainCloudHotSendService.class);
        }else {
            chainCloudColdReceiveService = Api.apiService(ChainCloudColdReceiveService.class);
        }

        initLv();

        isRefresh = true;

        showProgress();
        loadTx();
    }

    @Click(R.id.ibtn_back)
    void clickBack(){
        finish();
    }

    @Click(R.id.ibtn_option)
    void clickIBtnOption(){
        ArrayList<DialogWithActions.Action> actions = new ArrayList<>();
        actions.add(new DialogWithActions.Action(getString(R.string.address_history_receive), new Runnable() {
            @Override
            public void run() {
                AddressHistoryActivity_.intent(AddressDetailActivity.this)
                        .path(Path.External)
                        .user(user)
                        .isHot(isHot)
                        .start();
            }
        }));

        actions.add(new DialogWithActions.Action(getString(R.string.address_history_change), new Runnable() {
            @Override
            public void run() {
                AddressHistoryActivity_.intent(AddressDetailActivity.this)
                        .path(Path.Internal)
                        .user(user)
                        .isHot(isHot)
                        .start();
            }
        }));

        if (isHot) {
            actions.add(new DialogWithActions.Action(getString(R.string.address_hot_check), new Runnable() {
                @Override
                public void run() {
                    AddressBatchActivity_.intent(AddressDetailActivity.this)
                            .type(AddressBatch.Type.Hot)
                            .start();
                }
            }));
        }else {
            actions.add(new DialogWithActions.Action(getString(R.string.address_cold_check), new Runnable() {
                @Override
                public void run() {
                    AddressBatchActivity_.intent(AddressDetailActivity.this)
                            .type(AddressBatch.Type.Cold)
                            .start();
                }
            }));
        }

        DialogWithActions dialogWithActions = new DialogWithActions();
        dialogWithActions.setActions(actions).show(getSupportFragmentManager());
    }

    @Override
    public void onRefresh() {
        refresher.setRefreshing(true);

        showProgress();
        loadTx();
    }

    @ItemClick(R.id.lv)
    void clickItem(int position){
        int index = position - 1;

        Intent intent = new Intent(this, TxDetailActivity_.class);
        intent.putExtra("tx", mAdapter.getItem(index));
        intent.putExtra("userId", user.getId());
        intent.putExtra("isHot", isHot);

        startActivity(intent);
    }

    @Background
    void loadTx() {
        isLoding = true;

        try{
            List<Tx> txs = mAdapter.getData();
            String sinceTxHash = null;
            if(!isRefresh && txs != null && txs.size() > 0){
                sinceTxHash = txs.get(txs.size() - 1).getTxHash();
            }

            if (isHot) {
                txs = chainCloudHotSendService.getTxs(GlobalParams.coinCode, sinceTxHash, "desc");
            }else {
                txs = chainCloudColdReceiveService.getTxs(GlobalParams.coinCode, sinceTxHash, "desc");
            }

            if(txs != null && txs.size() > 0){
                hasMore = true;
            }else {
                hasMore = false;
            }

            showData(txs);
        }catch (RetrofitError error){
            showNetException(error);

            return;
        }finally {
            closeRefresh();
            closeProgress();
            isLoding = false;
        }
    }

    @UiThread
    void showData(List<Tx> txs){
        List<Tx> data = mAdapter.getData();
        if(data != null && !isRefresh){
            data.addAll(txs);
        }else {
            data = txs;
            isRefresh = false;
        }

        mAdapter.updateData(data);

    }

    @UiThread
    void closeRefresh(){
        refresher.setRefreshing(false);
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

    @UiThread
    void showPrompt(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    private void initLv() {
        mAdapter = new TxAdapter(this);

        headView = (AddressDetailHeader) View.inflate(this, R.layout.list_header_address_detail, null);
        headView.setUser(user);

        lv.addHeaderView(headView, null, false);

        lv.setAdapter(mAdapter);
        refresher.setOnRefreshListener(this);
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int lastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (firstVisibleItem + visibleItemCount >= totalItemCount - 6
                        && hasMore && !isLoding
                        && lastFirstVisibleItem < firstVisibleItem) {
                    showProgress();
                    loadTx();
                }
                lastFirstVisibleItem = firstVisibleItem;

            }
        });
    }

    private void showNetException(RetrofitError error) {
        switch (error.getKind()) {
            case NETWORK:
                showPrompt(getString(R.string.unable_access_network));
                break;
            case HTTP:
                switch (error.getResponse().getStatus()) {
                    case 403:
                        showPrompt(getString(R.string.token_expired));
                        break;
                    case 404:
                        showPrompt(getString(R.string.user_not_found));
                        break;
                    default:
                        showPrompt(getString(R.string.server_exception));
                }
                break;
        }
    }
}
