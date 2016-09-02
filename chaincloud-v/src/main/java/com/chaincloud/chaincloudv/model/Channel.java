package com.chaincloud.chaincloudv.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhumingu on 16/6/16.
 */
@DatabaseTable(tableName = "channel")
public class Channel implements Serializable{

    public enum ChannelType {
        @SerializedName("0")
        HC(0),
        @SerializedName("1")
        VC(1);

        private int value;

        ChannelType(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static ChannelType fromValue(int value) {
            for (ChannelType type : ChannelType.values()) {
                if (type.value() == value) {
                    return type;
                }
            }
            return HC;
        }
    }

    public enum ChannelStatus {
        @SerializedName("0")
        Creating(0),
        @SerializedName("1")
        OK(1);

        private int value;

        ChannelStatus(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static ChannelStatus fromValue(int value) {
            for (ChannelStatus status : ChannelStatus.values()) {
                if (status.value() == value) {
                    return status;
                }
            }
            return Creating;
        }
    }


    @DatabaseField(columnName = "channel_id", generatedId = true)
    public Integer channelId;
    @DatabaseField(columnName = "channel_type")
    public ChannelType channelType;
    @DatabaseField(columnName = "qc")
    public String qc;
    @DatabaseField(columnName = "dc")
    public String dc;
    @DatabaseField(columnName = "qh")
    public String qh;
    @DatabaseField(columnName = "dh")
    public String dh;
    @DatabaseField(columnName = "c_id")
    public Integer cId;
    @DatabaseField(columnName = "request_at")
    public Date requestAt;
    @DatabaseField(columnName = "create_at")
    public Date createAt;
    @DatabaseField(columnName = "channel_status")
    public ChannelStatus channelStatus;
    @DatabaseField(columnName = "user_id")
    public Integer userId;
}
