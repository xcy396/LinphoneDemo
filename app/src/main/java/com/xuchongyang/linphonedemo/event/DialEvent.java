package com.xuchongyang.linphonedemo.event;

/**
 * Created by Mark Xu on 17/3/23.
 */

public class DialEvent {
    private String mNumber;

    public DialEvent(String number) {
        this.mNumber = number;
    }

    public String getNumber() {
        return mNumber;
    }
}
