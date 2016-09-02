package com.chaincloud.chaincloudv.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.model.Address;
import com.chaincloud.chaincloudv.util.BitcoinUtil;
import com.chaincloud.chaincloudv.util.ClipboardUtil;

/**
 * Created by zhumingu on 16/6/24.
 */
public class AddressAdapter extends MBaseAdapter<Address> {

    public AddressAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        AddressHolder holder;
        if(convertView != null) {
            view = convertView;
            holder = (AddressHolder) view.getTag();
        } else {
            view = View.inflate(context, R.layout.list_item_address_history, null);

            holder = new AddressHolder();
            holder.tvAddress = (TextView) view.findViewById(R.id.tv_address);
            holder.tvIndex = (TextView)view.findViewById(R.id.tv_index);
            holder.vContainer = view.findViewById(R.id.fl_address);

            view.setTag(holder);
        }

        if(data != null && data.size() > 0) {
            final Address address = data.get(position);

            holder.tvAddress.setText(BitcoinUtil.formatHash(address.address, 4, 20));
            holder.tvIndex.setText(String.valueOf(address.index));
            holder.vContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardUtil.copyString(address.address);

                    Toast.makeText(context, R.string.me_address_copied, Toast.LENGTH_SHORT).show();
                }
            });
        }

        return view;
    }


    static class AddressHolder{
        TextView tvAddress, tvIndex;
        View vContainer;
    }
}
