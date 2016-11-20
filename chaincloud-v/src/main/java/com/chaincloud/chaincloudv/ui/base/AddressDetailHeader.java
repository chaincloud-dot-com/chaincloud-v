/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chaincloud.chaincloudv.ui.base;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaincloud.chaincloudv.GlobalParams;
import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.model.BitcoinUnit;
import com.chaincloud.chaincloudv.model.User;
import com.chaincloud.chaincloudv.ui.base.dialog.DialogSimpleQr_;
import com.chaincloud.chaincloudv.util.BitcoinUtil;
import com.chaincloud.chaincloudv.util.ClipboardUtil;
import com.chaincloud.chaincloudv.util.Coin;
import com.joanzapata.iconify.widget.IconTextView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.layout_address_detail_header)
public class AddressDetailHeader extends FrameLayout {

    User user;

    @ViewById(R.id.tv_address)
    TextView tvAddress;

    @ViewById(R.id.btn_balance)
    IconTextView btnBalance;

    @ViewById(R.id.iv_qrcode)
    QrCodeImageView ivQrcode;


    public AddressDetailHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Click(R.id.fl_address)
    void clickCopyAddress(){
        ClipboardUtil.copyString(user.getAddress());

        showMsg(getContext().getString(R.string.me_address_copied));
    }

    @Click(R.id.iv_qrcode)
    void clickQr(){
        DialogSimpleQr_.builder()
                .content(user.getAddress())
                .build()
                .show(((FragmentActivity)getContext()).getSupportFragmentManager());
    }


    public void setUser(User user){
        this.user = user;

        tvAddress.setText(BitcoinUtil.formatHash(user.getAddress(), 4, 12));
        ivQrcode.setContent(user.getAddress());

        btnBalance.setText(Coin.fromValue(GlobalParams.coinCode).showMoney(user.getBalance()));
    }


    private void showMsg(String msg){
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
