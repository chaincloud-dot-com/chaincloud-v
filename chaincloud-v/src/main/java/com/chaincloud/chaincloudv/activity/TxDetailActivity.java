package com.chaincloud.chaincloudv.activity;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaincloud.chaincloudv.GlobalParams;
import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.api.Api;
import com.chaincloud.chaincloudv.api.service.ChainCloudColdReceiveService;
import com.chaincloud.chaincloudv.api.service.ChainCloudHotSendService;
import com.chaincloud.chaincloudv.model.BitcoinUnit;
import com.chaincloud.chaincloudv.model.Tx;
import com.chaincloud.chaincloudv.ui.base.DialogWithActions;
import com.chaincloud.chaincloudv.util.BitcoinUtil;
import com.chaincloud.chaincloudv.util.ClipboardUtil;
import com.chaincloud.chaincloudv.util.Coin;
import com.chaincloud.chaincloudv.util.DateTimeUtil;
import com.chaincloud.chaincloudv.util.UIUtil;

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
 * Created by zhumingu on 16/6/23.
 */
@EActivity(R.layout.activity_tx_detail)
public class TxDetailActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    @Extra
    Tx tx;
    @Extra
    int userId;
    @Extra
    boolean isHot;

    @ViewById
    TextView tvHash, tvConfirmation, tvDate, tvValue, tvFee;
    @ViewById
    LinearLayout llInputs, llOutput;
    @ViewById
    Toolbar tb;
    @ViewById
    FrameLayout flHash;
    @ViewById
    SwipeRefreshLayout refresher;

    private String hash;
    private String inputAddress;
    private String outPutAddress;
    private long fee;
    private long inputValue;
    private long outputValue;
    private int width;


    @AfterViews
    void configureRefresher() {
        refresher.setColorSchemeColors(getResources().getIntArray(R.array.material_colors));
        refresher.setOnRefreshListener(this);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @AfterViews
    void initViews() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        if (tb != null) {
            setSupportActionBar(tb);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setElevation(0);

            tb.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
        showtx();
        getDataFromServer();
    }

    @UiThread
    void showtx() {
        inputValue = 0;
        outputValue = 0;
        flHash.setOnClickListener(copyClick);
        llInputs.removeAllViews();
        llOutput.removeAllViews();
        if (tx.getTxAt() != null) {
            tvDate.setText(DateTimeUtil.getDateTimeString(tx.getTxAt()));
        }
        if (tx.getTxHash() != null) {
            hash = tx.getTxHash();
            tvHash.setText(width <= 640 ? BitcoinUtil.formatHash(tx.getTxHash(), 4, 16) : BitcoinUtil.formatHash(tx.getTxHash(), 4, 24));
        }
        String confirmationCount = String.valueOf(tx.getConfirmation());
        if (confirmationCount != null) {
            tvConfirmation.setText(confirmationCount);
        }
        if (String.valueOf(tx.getValue()) != null) {
            tvValue.setText(Coin.fromValue(GlobalParams.coinCode).showMoney(tx.getValue()));
        }
        if (tx.getInputs() != null) {
            List<Tx.In> ins = tx.getInputs();
            for (Tx.In input : ins) {
                View v = View.inflate(this, R.layout.list_item_tx_input, null);
                llInputs.addView(v, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                TextView tvInputAddress = (TextView) v.findViewById(R.id.tv_input_address);
                inputAddress = input.getAddress();
                tvInputAddress.setText(width <= 640 ? BitcoinUtil.formatHash(input.getAddress(), 4, 16) : BitcoinUtil.formatHash(input.getAddress(), 4, 20));
                if (input.isMine() == true) {
                    tvInputAddress.setTextColor(getResources().getColor(R.color.green));
                }
                FrameLayout flInputAddress = (FrameLayout) v.findViewById(R.id.fl_input_address);
                copyAddress(inputAddress, flInputAddress);
                TextView tvInputValue = (TextView) v.findViewById(R.id.tv_input_value);
                tvInputValue.setText(Coin.fromValue(GlobalParams.coinCode).showMoney(input.getValue()));
                inputValue = inputValue + input.getValue();
            }
        }
        if (tx.getOutputs() != null) {
            List<Tx.Out> outs = tx.getOutputs();
            for (Tx.Out output : outs) {
                View v = View.inflate(this, R.layout.list_item_tx_output, null);
                llOutput.addView(v, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                TextView tvOutputAddress = (TextView) v.findViewById(R.id.tv_output_address);
                outPutAddress = output.getAddress();
                tvOutputAddress.setText(width <= 640 ? BitcoinUtil.formatHash(output.getAddress(), 4, 16) : BitcoinUtil.formatHash(output.getAddress(), 4, 20));
                if (output.isMine() == true) {
                    tvOutputAddress.setTextColor(getResources().getColor(R.color.green));
                }
                FrameLayout flOutputAddress = (FrameLayout) v.findViewById(R.id.fl_output_address);
                copyAddress(outPutAddress, flOutputAddress);
                TextView tvOutputValue = (TextView) v.findViewById(R.id.tv_output_value);
                tvOutputValue.setText(Coin.fromValue(GlobalParams.coinCode).showMoney(output.getValue()));
                outputValue = outputValue + output.getValue();
            }
        }
        if (inputValue != 0 && outputValue != 0) {
            Coin coin = Coin.fromValue(GlobalParams.coinCode);
//            if (coin != Coin.ETH) {
                fee = inputValue - outputValue;
                tvFee.setText(coin.showMoney(fee));
//            }else {
//                tvFee.setText(String.format(
//                        getString(R.string.eth_fee),
//                        String.valueOf(tx.getGas()),
//                        coin.showMoney(tx.getGasPrice()),
//                        coin.showMoney(tx.getGasUsed() * tx.getGasPrice())));
//            }
        }
    }

    @Background
    void getDataFromServer() {
        try {
            Tx detail = null;
            if (isHot) {
                detail = Api.apiService(ChainCloudHotSendService.class).getDetail(GlobalParams.coinCode, tx.getTxHash());
            }else {
                detail = Api.apiService(ChainCloudColdReceiveService.class).getDetail(GlobalParams.coinCode, tx.getTxHash());
            }
            tx = detail;
            showtx();
        } catch (RetrofitError ex) {
            ex.printStackTrace();
        }
        refresher.post(new Runnable() {
            @Override
            public void run() {
                refresher.setRefreshing(false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Pair<Integer, String> blockChainInfoPair = Coin.fromValue(GlobalParams.coinCode).getBlockChainInfo();

            final String blockchainInfoTxUrl = blockChainInfoPair.second;
            ArrayList<DialogWithActions.Action> actions = new ArrayList<DialogWithActions.Action>();
            actions.add(new DialogWithActions.Action(blockChainInfoPair.first, new Runnable() {
                @Override
                public void run() {
                    UIUtil.gotoBrower(TxDetailActivity.this, blockchainInfoTxUrl +
                            tx.getTxHash());
                }
            }));
            new DialogWithActions().setActions(actions).show(getSupportFragmentManager());
            return true;
        }
        if (tb != null) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    @Background
    public void onRefresh() {
        getDataFromServer();
    }

    void showPrompt(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private View.OnClickListener copyClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (hash != null) {
                ClipboardUtil.copyString(hash);
                showPrompt(getString(R.string.me_address_copied));
            }
        }
    };

    public void copyAddress(final String address, View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardUtil.copyString(address);
                showPrompt(getString(R.string.me_address_copied));
            }
        });

    }
}
