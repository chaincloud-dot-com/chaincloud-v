package com.chaincloud.chaincloudv.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chaincloud.chaincloudv.GlobalParams;
import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.model.BitcoinUnit;
import com.chaincloud.chaincloudv.model.Tx;
import com.chaincloud.chaincloudv.ui.base.TintableImageView;
import com.chaincloud.chaincloudv.util.Coin;
import com.chaincloud.chaincloudv.util.DateTimeUtil;
import com.joanzapata.iconify.widget.IconTextView;

/**
 * Created by zhumingu on 16/6/24.
 */
public class TxAdapter extends MBaseAdapter<Tx> {

    public TxAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        TxHolder holder;
        if(convertView != null) {
            view = convertView;
            holder = (TxHolder) view.getTag();
        } else {
            view = View.inflate(context, R.layout.list_item_address_detail_transaction, null);

            holder = new TxHolder();
            holder.ivIcon = (TintableImageView) view.findViewById(R.id.iv_icon);
            holder.ivConfirmation = (ImageView) view.findViewById(R.id.iv_confirmation);

            holder.tvValue = (IconTextView) view.findViewById(R.id.tv_value);
            holder.tvDate = (TextView) view.findViewById(R.id.tv_date);
            holder.tvDirection = (TextView)view.findViewById(R.id.tv_direction);

            view.setTag(holder);
        }

        if(data != null && data.size() > 0) {
            Tx tx = data.get(position);

            holder.tvValue.setText(Coin.fromValue(GlobalParams.coinCode).showMoney(tx.getValue(), tx.getValueStr()));
            if (tx.getValue() > 0 || (tx.getValueStr() != null && tx.getValueStr().signum() > 0)) {
                holder.ivIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.menu_deposit));
                holder.ivIcon.setColorFilter(context.getResources().getColorStateList(R.color.fab_color_pressed));

                holder.tvValue.setTextColor(context.getResources().getColor(R.color.green));
                holder.tvDirection.setText(R.string.receive_btc);
            } else {
                holder.tvValue.setTextColor(context.getResources().getColor(R.color.red));
                holder.tvDirection.setText(R.string.send_btc);
                holder.ivIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.menu_withdraw));
                holder.ivIcon.setColorFilter(context.getResources().getColorStateList(R.color.scanner_result_dots));
            }
            holder.tvDate.setText(DateTimeUtil.getRelativeDate(context, tx.getTxAt()));
            showTxConfirmationWithImage(tx, holder.ivConfirmation);
        }

        return view;
    }

    public void showTxConfirmationWithImage(Tx tx, ImageView imageView) {
        int depth = tx.getConfirmation();
        switch (depth) {
            case 0:
                imageView.setImageResource(R.drawable.transaction_pending_icon);
                break;
            case 1:
                imageView.setImageResource(R.drawable.transaction_building_icon_1);
                break;
            case 2:
                imageView.setImageResource(R.drawable.transaction_building_icon_2);
                break;
            case 3:
                imageView.setImageResource(R.drawable.transaction_building_icon_3);
                break;
            case 4:
                imageView.setImageResource(R.drawable.transaction_building_icon_4);
                break;
            case 5:
                imageView.setImageResource(R.drawable.transaction_building_icon_5);
                break;
            case 6:
                imageView.setImageResource(R.drawable.transaction_building_icon_6);
                break;
            default:
                imageView.setImageResource(R.drawable.transaction_building_icon_6);
                break;
        }
        if (depth >= 100) {
            imageView.setImageResource(R.drawable.transaction_building_icon_100);
        }
    }


    static class TxHolder{
        TintableImageView ivIcon;
        IconTextView tvValue;
        TextView tvDirection, tvDate;
        ImageView ivConfirmation;
    }
}
