package com.chaincloud.chaincloudv.model;

import com.chaincloud.chaincloudv.R;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

/**
 * Created by zhumingu on 16/8/1.
 */
public class AddressBatch implements Serializable {
    @DatabaseField(columnName = "address_batch_id", generatedId = true)
    public Integer addressBatchId;
    @DatabaseField
    public Integer index;
    @DatabaseField
    public Status status;
    @DatabaseField
    public Type type;


    public enum Type {
        @SerializedName("1")
        Hot(1),
        @SerializedName("2")
        Cold(2);

        private int value;

        Type(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static Type fromValue(int value) {
            for (Type type : Type.values()) {
                if (type.value() == value) {
                    return type;
                }
            }
            return Hot;
        }
    }

    public enum Status {
        @SerializedName("1")
        NULL(1),
        @SerializedName("2")
        OK(2),
        @SerializedName("3")
        ERROR(3);

        private int value;

        Status(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static Status fromValue(int value) {
            for (Status status : Status.values()) {
                if (status.value() == value) {
                    return status;
                }
            }
            return NULL;
        }

        public int imgRes() {
            switch (this) {
                case NULL:
                    return R.drawable.check_null;
                case OK:
                    return R.drawable.checkmark;
                case ERROR:
                    return R.drawable.check_failed;
            }

            return 0;
        }
    }
}
