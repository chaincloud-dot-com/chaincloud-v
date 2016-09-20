package com.chaincloud.chaincloudv.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chaincloud.chaincloudv.GlobalParams;
import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.model.BitcoinUnit;
import com.chaincloud.chaincloudv.model.User;
import com.chaincloud.chaincloudv.util.Coin;
import com.joanzapata.iconify.widget.IconTextView;

import java.util.List;

public class AccountExpandableAdapter extends MBaseExpandableAdapter<String, User> {

    public AccountExpandableAdapter(Context context) {
        super(context);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view;
        TypeHolder holder;
        if(convertView != null) {
            view = convertView;
            holder = (TypeHolder) view.getTag();
        } else {
            view = View.inflate(context, R.layout.list_item_group_account, null);

            holder = new TypeHolder();
            holder.tvType = (TextView) view.findViewById(R.id.tv_group);
            holder.ivIndicator = (ImageView) view.findViewById(R.id.iv_indicator);

            view.setTag(holder);
        }

        if(groups != null && groups.size() > 0) {
            String type = groups.get(groupPosition);

            if(isExpanded){
                holder.ivIndicator.setImageResource(R.drawable.list_item_address_group_indicator_expanded);
            }else {
                holder.ivIndicator.setImageResource(R.drawable.list_item_address_group_indicator);
            }

            holder.tvType.setText(type);
        }

        return view;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View view;
        AccountHolder holder;
        if(convertView != null) {
            view = convertView;
            holder = (AccountHolder) view.getTag();
        } else {
            view = View.inflate(context, R.layout.list_item_child_account, null);

            holder = new AccountHolder();
            holder.tvUserId = (TextView) view.findViewById(R.id.tv_userid);
            holder.tvUserName = (TextView) view.findViewById(R.id.tv_username);
            holder.tvBalance = (IconTextView) view.findViewById(R.id.tv_balance);

            view.setTag(holder);
        }


        if(child != null && child.size() > 0){
            List<User> users = child.get(groupPosition);
            if(users != null && groups.size() > 0){
                User user = users.get(childPosition);

                holder.tvUserId.setText(String.format("id:%1$s", user.getId()));
                holder.tvUserName.setText(String.format("name:%1$s", user.getName()));
                holder.tvBalance.setText(String.format("balance:%1$s",
                        Coin.fromValue(GlobalParams.coinCode).getSymbol() + BitcoinUnit.BTC.format(user.getBalance())));
            }
        }

        return view;
    }


    static class TypeHolder{
        ImageView ivIndicator;
        TextView tvType;
    }

    static class AccountHolder{
        TextView tvUserId, tvUserName;
        IconTextView tvBalance;
    }
}
