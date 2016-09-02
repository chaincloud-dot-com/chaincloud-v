package com.chaincloud.chaincloudv.event;

/**
 * Created by zhumingu on 16/8/5.
 */
public class UpdateWorkState {

    public enum Type {
        StartLoop(0),
        StopLoop(1);

        private int value;

        Type(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static Type typeForValue(int value) {
            for (Type t : Type.values()) {
                if (t.value() == value) {
                    return t;
                }
            }
            return StartLoop;
        }
    }


    public Type type;

    public UpdateWorkState(Type type){
        this.type = type;
    }
}
