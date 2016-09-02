package com.chaincloud.chaincloudv.model;

import com.google.gson.annotations.SerializedName;

public enum Path {
    @SerializedName("0")
    External(0),
    @SerializedName("1")
    Internal(1);
    private int value;

    Path(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static Path fromValue(int value) {
        for (Path p : Path.values()) {
            if (p.value == value) {
                return p;
            }
        }
        return null;
    }
}
