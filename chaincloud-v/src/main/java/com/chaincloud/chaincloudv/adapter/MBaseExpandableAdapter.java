package com.chaincloud.chaincloudv.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MBaseExpandableAdapter<G, C> extends BaseExpandableListAdapter {
    protected List<G> groups;
    protected Map<Integer, List<C>> child;
    protected Context context;

    Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            notifyDataSetChanged();
            super.handleMessage(msg);
        }
    };


    public MBaseExpandableAdapter(Context context){
        this.context = context;
    }

    /*********************Group************************/
    @Override
    public int getGroupCount() {
        if(groups != null){
            return groups.size();
        }

        return 0;
    }

    @Override
    public G getGroup(int groupPosition) {
        if(groups != null){
            return groups.get(groupPosition);
        }

        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public abstract View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent);


    /*********************Children************************/
    @Override
    public int getChildrenCount(int groupPosition) {
        if(child != null){
            List<C> data = child.get(groupPosition);
            if(data != null) {
                return data.size();
            }
        }

        return 0;
    }

    @Override
    public C getChild(int groupPosition, int childPosition) {
        if(child != null){
            List<C> data = child.get(groupPosition);
            if(data != null) {
                return data.get(childPosition);
            }
        }

        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public abstract View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent);


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    /*********************My************************/
    public void updateData(List<G> groups, Map<Integer, List<C>> child){
        this.groups = groups;
        this.child = child;

        handler.sendMessage(new Message());
    }

    public void updateGroup(List<G> groups) {
        this.groups = groups;
        child = null;

        handler.sendMessage(new Message());
    }

    public void updateGroup() {
        handler.sendMessage(new Message());
    }

    public List<G> getGroups(){
        return groups;
    }

    public void updateChild(int groupPosition, List<C> data) {
        if(child != null){
            child.put(groupPosition, data);
        }else {
            child = new HashMap<>();
            child.put(groupPosition, data);
        }
    }

    public void updateChild(Map<Integer, List<C>> child) {
        this.child = child;
    }

    public List<C> getChild(int groupPosition){
        if(child != null) {
            return child.get(groupPosition);
        }

        return null;
    }

    public Map<Integer, List<C>> getChild(){
        return child;
    }
}
