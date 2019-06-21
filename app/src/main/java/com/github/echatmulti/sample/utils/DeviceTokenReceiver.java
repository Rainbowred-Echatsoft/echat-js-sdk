package com.github.echatmulti.sample.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blankj.utilcode.util.LogUtils;

import static com.github.echatmulti.sample.utils.Constants.DEVICE_TOKEN_FUN;

/**
 * @Author: xhy
 * @Email: xuhaoyang3x@gmail.com
 * @program: EChatMultiSample
 * @create: 2019-06-24
 * @describe
 */
public class DeviceTokenReceiver extends BroadcastReceiver {
    private static final String TAG = "DeviceTokenReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.iTag(TAG, "收到DeviceToken广播");
        final String devicetoken = intent.getStringExtra(DEVICE_TOKEN_FUN);

    }
}
