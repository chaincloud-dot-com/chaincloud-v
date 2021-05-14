package com.chaincloud.chaincloudv.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.chaincloud.chaincloudv.GlobalParams;
import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.activity.AddressDetailActivity_;
import com.chaincloud.chaincloudv.adapter.AccountExpandableAdapter;
import com.chaincloud.chaincloudv.api.Api;
import com.chaincloud.chaincloudv.api.service.ChainCloudColdReceiveService;
import com.chaincloud.chaincloudv.api.service.ChainCloudHotSendService;
import com.chaincloud.chaincloudv.model.User;
import com.chaincloud.chaincloudv.util.Coin;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.IgnoreWhen;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RetrofitError;

/**
 * Created by zhumingu on 16/6/20.
 */
@EFragment(R.layout.fragment_account_list)
public class AccountListFragment extends Fragment implements Refreshable,
        ExpandableListView.OnChildClickListener, SwipeRefreshLayout.OnRefreshListener {

    private AccountExpandableAdapter mAdapter;

    @ViewById(R.id.refresher)
    SwipeRefreshLayout refresher;

    @ViewById(R.id.lv)
    ExpandableListView phaeLv;


    @AfterViews
    void init(){
        initLv();

        loadData();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
                                final int groupPosition, final int childPosition, long id) {

        User user = mAdapter.getChild(groupPosition, childPosition);

        boolean isHotSend;
        if (mAdapter.getGroup(groupPosition).equals(getString(R.string.account_hotsend))) {
            isHotSend = true;
        }else {
            isHotSend = false;
        }

        AddressDetailActivity_.intent(getContext())
                .user(user)
                .isHot(isHotSend)
                .start();

        return false;
    }

    @IgnoreWhen(IgnoreWhen.State.DETACHED)
    @Background
    void loadData(){

        ChainCloudHotSendService bhss = Api.apiService(ChainCloudHotSendService.class);
        ChainCloudColdReceiveService bcrs = Api.apiService(ChainCloudColdReceiveService.class);

        List<String> groups = new ArrayList<>();
        Map<Integer, List<User>> childs = new HashMap<>();
        try {
            User hUser = bhss.currentUser(GlobalParams.coinCode);

            if (hUser != null && hUser.getId() != 0) {
                groups.add(getString(R.string.account_hotsend));

                List<User> users = new ArrayList<>();
                users.add(hUser);

                childs.put(childs.size(), users);
            }
        } catch (RetrofitError error) {
            showNetError(error);
        }

        try {
            User cUser = bcrs.currentUser(GlobalParams.coinCode);

            if (cUser != null && cUser.getId() != 0) {
                groups.add(getString(R.string.account_cold_receive));

                List<User> users = new ArrayList<>();
                users.add(cUser);

                childs.put(childs.size(), users);
            }

        } catch (RetrofitError error) {
            showNetError(error);
        }

        showData(groups, childs);

        closeRefresh();
    }

    @UiThread
    void showData(List<String> groups, Map<Integer, List<User>> childs){
        mAdapter.updateData(groups, childs);

        for (int i = 0; i < mAdapter.getGroupCount(); i++) {
            phaeLv.expandGroup(i);
        }

        refresher.setRefreshing(false);
    }

    @UiThread
    void showPrompt(String msg){
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @UiThread
    void closeRefresh(){
        refresher.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        loadData();
    }

    @Override
    public void doRefresh() {
        refresher.setRefreshing(true);
        onRefresh();
    }


    private void initLv() {
        mAdapter = new AccountExpandableAdapter(getContext());

        phaeLv.setAdapter(mAdapter);
        phaeLv.setOnChildClickListener(this);

        refresher.setOnRefreshListener(this);
        refresher.post(new Runnable() {
            @Override
            public void run() {
                refresher.setRefreshing(true);
            }
        });
    }

    private void showNetError(RetrofitError error) {
        switch (error.getKind()){
            case NETWORK:
                showPrompt(getString(R.string.unable_access_network));
                break;
            case HTTP:
                switch (error.getResponse().getStatus()){
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
