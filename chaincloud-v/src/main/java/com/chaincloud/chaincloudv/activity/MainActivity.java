package com.chaincloud.chaincloudv.activity;

import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.adapter.MFragmentPagerAdapter;
import com.chaincloud.chaincloudv.dao.AddressBatchDao;
import com.chaincloud.chaincloudv.dao.AddressDao;
import com.chaincloud.chaincloudv.dao.ORMLiteDBHelper;
import com.chaincloud.chaincloudv.fragment.AccountListFragment_;
import com.chaincloud.chaincloudv.fragment.MessageFragment_;
import com.chaincloud.chaincloudv.fragment.SettingFragment_;
import com.chaincloud.chaincloudv.ui.base.TabButton;
import com.chaincloud.chaincloudv.util.crypto.EncryptedData;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.ViewById;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhumingu on 16/6/23.
 */
@EActivity(R.layout.activity_main)
public class MainActivity extends FragmentActivity {

    MFragmentPagerAdapter mAdapter;

    @ViewById
    TabButton tbtnMessage;
    @ViewById
    TabButton tbtnMain;
    @ViewById
    TabButton tbtnSetting;
    @ViewById
    ViewPager pager;


    @AfterViews
    void init() {
        initTab();
        initViewPager();
    }

    private void initViewPager() {
        mAdapter = new MFragmentPagerAdapter(getSupportFragmentManager(),
                new Class[]{MessageFragment_.class, AccountListFragment_.class, SettingFragment_.class});
        pager.setAdapter(mAdapter);
        pager.setOffscreenPageLimit(2);
        pager.addOnPageChangeListener(new PageChangeListener(new TabButton[]{tbtnMessage,
                tbtnMain, tbtnSetting}, pager));
        pager.post(new Runnable() {
            @Override
            public void run() {
                pager.setCurrentItem(1);
            }
        });
    }

    private void initTab() {
        tbtnMessage.setText(getString(R.string.tab_message));
        tbtnMain.setText(getString(R.string.tab_account));
        tbtnSetting.setText(getString(R.string.tab_setting));
    }

    private class PageChangeListener implements ViewPager.OnPageChangeListener {
        private List<TabButton> indicators;
        private ViewPager pager;

        public PageChangeListener(TabButton[] buttons, ViewPager viewPager) {
            this.indicators = new ArrayList<>();
            this.pager = viewPager;
            int size = buttons.length;
            for (int i = 0; i < size; i++) {
                TabButton button = buttons[i];
                indicators.add(button);
                button.setOnClickListener(new IndicatorClick(i));
            }

        }

        public void onPageScrollStateChanged(int state) {

        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        private class IndicatorClick implements View.OnClickListener {

            private int position;

            public IndicatorClick(int position) {
                this.position = position;
            }

            public void onClick(View v) {
                if (pager.getCurrentItem() != position) {
                    pager.setCurrentItem(position, true);
                }
            }
        }

        public void onPageSelected(int position) {

            if (position >= 0 && position < indicators.size()) {
                for (int i = 0;
                     i < indicators.size();
                     i++) {
                    indicators.get(i).setChecked(i == position);
                }
            }
        }
    }
}
