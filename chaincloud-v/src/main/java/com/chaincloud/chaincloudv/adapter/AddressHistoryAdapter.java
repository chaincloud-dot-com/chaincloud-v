package com.chaincloud.chaincloudv.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.chaincloud.chaincloudv.ui.base.EndlessRecyclerViewAdapter;
import com.chaincloud.chaincloudv.ui.base.item.AddressHistoryListItem;
import com.chaincloud.chaincloudv.ui.base.item.AddressHistoryListItem_;

import java.util.List;

/**
 * Created by songchenwen on 16/4/6.
 */
public class AddressHistoryAdapter extends EndlessRecyclerViewAdapter {
    private List<String> addresses;

    public AddressHistoryAdapter(List<String> addresses) {
        this.addresses = addresses;
    }

    @Override
    public RecyclerView.ViewHolder onCreateDataViewHolder(ViewGroup parent, int viewType) {
        return new VH(AddressHistoryListItem_.build(parent.getContext()));
    }

    @Override
    public void onBindDataViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VH) {
            ((VH) holder).v().setData(addresses.get(position), position);
        }
    }

    @Override
    public int getDataItemCount() {
        return addresses.size();
    }

    @Override
    public int getDataItemViewType(int position) {
        return 0;
    }

    private static final class VH extends RecyclerView.ViewHolder {
        private AddressHistoryListItem v;

        public VH(AddressHistoryListItem itemView) {
            super(itemView);
            this.v = itemView;
            itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams
                    .MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        public AddressHistoryListItem v() {
            return v;
        }
    }
}
