package com.github.echatmulti.sample.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private DefaultCallback callback;

    public NetworkChangeReceiver(DefaultCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (callback != null) callback.doSomething();
    }
}
