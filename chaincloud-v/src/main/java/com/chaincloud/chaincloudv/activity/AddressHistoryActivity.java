package com.chaincloud.chaincloudv.activity;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.chaincloud.chaincloudv.GlobalParams;
import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.adapter.AddressHistoryAdapter;
import com.chaincloud.chaincloudv.api.Api;
import com.chaincloud.chaincloudv.api.result.AddressHistory;
import com.chaincloud.chaincloudv.api.service.ChainCloudColdReceiveService;
import com.chaincloud.chaincloudv.api.service.ChainCloudHotSendService;
import com.chaincloud.chaincloudv.model.Path;
import com.chaincloud.chaincloudv.model.User;
import com.chaincloud.chaincloudv.ui.base.EndlessRecyclerViewAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by songchenwen on 16/4/6.
 */
@EActivity(R.layout.activity_address_history)
public class AddressHistoryActivity extends AppCompatActivity implements SwipeRefreshLayout
        .OnRefreshListener, EndlessRecyclerViewAdapter.EndlessRecyclerViewLoadNextPageListener {

    @Extra
    User user;

    @Extra
    Path path;

    @Extra
    boolean isHot;

    @ViewById
    Toolbar tb;
    @ViewById
    SwipeRefreshLayout refresher;
    @ViewById
    RecyclerView rv;

    private AddressHistoryAdapter adapter;
    private List<AddressHistory> addresses = new ArrayList<>();

    @AfterViews
    void initViews() {
        if (adapter == null) {
            adapter = new AddressHistoryAdapter(addresses);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        adapter.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);
        rv.addOnScrollListener(adapter.onScrollListener);
        adapter.setAutoLoadThreshold(2);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        refresher.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!adapter.isLoading()) {
                    refresh();
                }
                adapter.setLoadNextPageListener(AddressHistoryActivity.this);
            }
        }, 300);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void refresh() {
        refresher.setRefreshing(true);
        adapter.setLoading(true);
        onRefresh();
    }

    @AfterViews
    void configureTitle() {
        if (path == null) {
            throw new RuntimeException("AddressHistoryActivity needs path argument");
        }
        switch (path) {
            case Internal:
                tb.setTitle(R.string.address_history_change);
                return;
            case External:
                tb.setTitle(R.string.address_history_receive);
                return;
            default:
                tb.setTitle(R.string.address_history_receive);
        }
    }

    @AfterViews
    void configureRefresher() {
        refresher.setColorSchemeColors(getResources().getIntArray(R.array.material_colors));
        refresher.setOnRefreshListener(this);
    }

    @Override
    @Background
    public void onRefresh() {
        adapter.setLoading(true);
        List<AddressHistory> addressHistories = null;
        try {
            if(isHot) {
                addressHistories = Api.apiService(ChainCloudHotSendService.class)
                        .addressHistory(GlobalParams.coinCode, path.value(), null);
            }else {
                addressHistories = Api.apiService(ChainCloudColdReceiveService.class)
                        .addressHistory(GlobalParams.coinCode, path.value(), null);
            }

            apiDataListGot(true, addressHistories);
        } catch (RetrofitError e) {
            apiDataListGot(true, null);
            refresher.post(new Runnable() {
                @Override
                public void run() {
                    adapter.setLoading(false);
                    refresher.setRefreshing(false);
                }
            });
        }
    }

    @Override
    @Background
    public void onPageAutoLoad() {
        if (addresses.size() == 0) {
            apiDataListGot(false, null);
            return;
        }
        List<AddressHistory> addressHistories = null;
        try {
            if(isHot) {
                addressHistories = Api.apiService(ChainCloudHotSendService.class)
                        .addressHistory(GlobalParams.coinCode, path.value(), addresses.get(addresses.size() - 1).address);
            }else {
                addressHistories = Api.apiService(ChainCloudColdReceiveService.class)
                        .addressHistory(GlobalParams.coinCode, path.value(), addresses.get(addresses.size() - 1).address);
            }

            apiDataListGot(false, addressHistories);
        } catch (RetrofitError e) {
            e.printStackTrace();
            apiDataListGot(false, null);
        }
    }

    @UiThread
    void apiDataListGot(boolean replace, List<AddressHistory> deltaAddresses) {
        if (refresher.isRefreshing() && !replace) {
            return;
        }
        if (replace) {
            refresher.setRefreshing(false);
            if (deltaAddresses != null && deltaAddresses.size() > 0) {
                addresses.clear();
            }
        }
        if (deltaAddresses != null) {
            int from = addresses.size();
            addresses.addAll(deltaAddresses);
            if (!replace) {
                adapter.notifyItemRangeInserted(from, deltaAddresses.size());
            }
        }
        if (deltaAddresses != null && replace) {
            adapter.notifyDataSetChanged();
        }
        adapter.setLoading(false);
        adapter.setNoMore(deltaAddresses == null || deltaAddresses.size() == 0);
    }
}
