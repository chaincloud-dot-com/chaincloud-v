package com.chaincloud.chaincloudv.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.model.AddressBatch;

/**
 * Created by zhumingu on 16/6/24.
 */
public class BatchAdapter extends MBaseAdapter<AddressBatch> {

    public interface MOnClickListener{
        void onClick(int position);
    }

    private MOnClickListener clickListener;


    public BatchAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        BatchHolder holder;
        if(convertView != null) {
            view = convertView;
            holder = (BatchHolder) view.getTag();
        } else {
            view = View.inflate(context, R.layout.list_item_address_batch, null);

            holder = new BatchHolder();
            holder.ivState = (ImageView) view.findViewById(R.id.iv_state);
            holder.tvIndex = (TextView)view.findViewById(R.id.tv_index);

            view.setTag(holder);
        }

        if(data != null && data.size() > 0) {
            AddressBatch addressBatch = data.get(position);

            holder.ivState.setImageResource(addressBatch.status.imgRes());
            holder.tvIndex.setText(String.format(
                    context.getString(R.string.address_batch_index), addressBatch.index + 1));
        }

        return view;
    }

    static class BatchHolder{
        TextView tvIndex;
        ImageView ivState;
    }
}
