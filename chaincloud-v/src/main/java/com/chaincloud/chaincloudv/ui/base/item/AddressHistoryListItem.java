package com.chaincloud.chaincloudv.ui.base.item;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.util.BitcoinUtil;
import com.chaincloud.chaincloudv.util.ClipboardUtil;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by songchenwen on 16/4/6.
 */
@EViewGroup(R.layout.list_item_address_history)
public class AddressHistoryListItem extends FrameLayout {
    @ViewById
    TextView tvAddress;

    @ViewById
    TextView tvIndex;

    private String address;

    public AddressHistoryListItem(Context context) {
        super(context);
    }

    public AddressHistoryListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AddressHistoryListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AddressHistoryListItem(Context context, AttributeSet attrs, int defStyleAttr, int
            defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setData(String address, int index) {
        this.address = address;
        tvAddress.setText(BitcoinUtil.formatHash(address, 4, 20));
        tvIndex.setText(String.valueOf(index));
    }

    @Click
    void flAddressClicked() {
        if (address != null) {
            ClipboardUtil.copyString(address);

            Toast.makeText(getContext(), R.string.me_address_copied, Toast.LENGTH_SHORT).show();
        }
    }
}
