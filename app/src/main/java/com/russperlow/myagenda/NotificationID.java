package com.russperlow.myagenda;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationID {

    private static final AtomicInteger counter = new AtomicInteger();
    public static final String NOTIFICATION_COUTNTER = "NOTIFICATION_COUNTER_PREF";

    public static final int nextValue(){
        return counter.getAndIncrement();
    }

    public static final void setStartValue(int startValue){
        counter.set(startValue);
    }

}
