package com.chaincloud.chaincloudv.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.activity.AddressDetailActivity_;
import com.chaincloud.chaincloudv.adapter.AccountExpandableAdapter;
import com.chaincloud.chaincloudv.api.Api;
import com.chaincloud.chaincloudv.api.service.ChainCloudColdReceiveService;
import com.chaincloud.chaincloudv.api.service.ChainCloudHotSendService;
import com.chaincloud.chaincloudv.model.User;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
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

    @Background
    void loadData(){

        try {
            ChainCloudHotSendService bhss = Api.apiService(ChainCloudHotSendService.class);
            ChainCloudColdReceiveService bcrs = Api.apiService(ChainCloudColdReceiveService.class);

            User hUser = bhss.currentUser();
            User cUser = bcrs.currentUser();

            List<String> groups = new ArrayList<>();
            Map<Integer, List<User>> childs = new HashMap<>();

            if (hUser != null){
                groups.add(getString(R.string.account_hotsend));

                List<User> users = new ArrayList<>();
                users.add(hUser);

                childs.put(childs.size(), users);
            }

            if (cUser != null){
                groups.add(getString(R.string.account_cold_receive));

                List<User> users = new ArrayList<>();
                users.add(cUser);

                childs.put(childs.size(), users);
            }

            showData(groups, childs);
        }catch (RetrofitError error){
            showNetError(error);
            return;
        }finally {
            closeRefresh();
        }
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
                showPrompt("无法访问网络！");
                break;
            case HTTP:
                switch (error.getResponse().getStatus()){
                    case 403:
                        showPrompt("token过期，重新输入！");
                        break;
                    case 404:
                        showPrompt("此用户id不存在！");
                        break;
                    default:
                        showPrompt("服务器异常");
                }
                break;
        }
    }
}
