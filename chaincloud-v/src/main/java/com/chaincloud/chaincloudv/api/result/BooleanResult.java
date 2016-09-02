package com.chaincloud.chaincloudv.api.result;

import java.io.Serializable;

/**
 * Created by songchenwen on 15/8/5.
 */
public class BooleanResult implements Serializable {
    private boolean result = false;

    public BooleanResult(boolean result) {
        this.result = result;
    }

    public boolean result() {
        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(result());
    }
}
