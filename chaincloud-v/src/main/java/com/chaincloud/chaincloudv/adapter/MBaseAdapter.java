package com.chaincloud.chaincloudv.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public abstract class MBaseAdapter<T> extends BaseAdapter{
    protected List<T> data;
    protected Context context;

    public MBaseAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        if(data != null){
            return data.size();
        }

        return 0;
    }

    @Override
    public T getItem(int position) {
        if(data != null){
            return data.get(position);
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    public void updateData(List<T> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }

    public void updateData() {
        this.notifyDataSetChanged();
    }

    public List<T> getData(){
        return data;
    }
}
