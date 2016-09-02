package com.chaincloud.chaincloudv.model;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

/**
 * Created by zhumingu on 16/8/1.
 */
public class Address implements Serializable {
    @DatabaseField(columnName = "address_id", generatedId = true)
    public Integer addressId;
    @DatabaseField
    public String address;
    @DatabaseField
    public Integer index;
    @DatabaseField(columnName = "address_batch", foreign = true, foreignAutoRefresh = true)
    public AddressBatch addressBatch;
}
