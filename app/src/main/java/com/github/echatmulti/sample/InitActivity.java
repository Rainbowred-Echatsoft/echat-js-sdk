package com.github.echatmulti.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.github.echatmulti.sample.utils.DefaultCallback;
import com.github.echatmulti.sample.utils.NetworkChangeReceiver;

import static com.github.echatmulti.sample.utils.Constants.ACTION_DEVICE_TOKEN;
import static com.github.echatmulti.sample.utils.Constants.DEVICE_TOKEN_FUN;
import static com.github.echatmulti.sample.utils.Constants.FIRST_BOOT;
import static com.github.echatmulti.sample.utils.Constants.HANDLER_WHAT_BACK;

public class InitActivity extends AppCompatActivity {

    private boolean               firstBoot;
    private AlertDialog.Builder   dialogBuilder;
    private NetworkChangeReceiver mNetworkChangeReceive = new NetworkChangeReceiver(new DefaultCallback() {
        @Override
        public void doSomething() {
            connected = NetworkUtils.isConnected();
        }
    });
    private DeviceTokenReceiver   deviceTokenReceiver   = new DeviceTokenReceiver();

    class DeviceTokenReceiver extends BroadcastReceiver {
        private static final String TAG = "DeviceTokenReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            final String devicetoken = intent.getStringExtra(DEVICE_TOKEN_FUN);
            if (!TextUtils.isEmpty(devicetoken)) {
                deviceToken = devicetoken;
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HANDLER_WHAT_BACK) {
                backExit = false;
            }
        }
    };

    private boolean backExit;//退出APP
    private String  deviceToken;
    private boolean connected;//网络是否连接

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter mFilter  = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        IntentFilter dtFilter = new IntentFilter(ACTION_DEVICE_TOKEN);
        registerReceiver(mNetworkChangeReceive, mFilter);
        registerReceiver(deviceTokenReceiver, dtFilter);

        firstBoot = SPUtils.getInstance().getBoolean(FIRST_BOOT, true);

        if (!firstBoot) {
            goMain();
            return;
        }

        //等待获得devicetoken
        setContentView(R.layout.activity_init);
        //判断当前是否有网络
        connected = NetworkUtils.isConnected();
        if (!connected) {
            //没有联网
            //提示联网

            alertNotNetwork();
        } else {
            checkDeviceToken(5000);
        }

    }

    private void checkDeviceToken(int delay) {
        App.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                deviceToken = SPUtils.getInstance().getString(DEVICE_TOKEN_FUN, "");
                //5s后还没获得devicetoken
                if (TextUtils.isEmpty(deviceToken)) {
                    //没有Device token
                    showCustomDialog("缺少关键数据", "缺少关键数据，请重试", "重新检测", "退出", "跳过(无远程推送)", new DialogCallback() {
                        @Override
                        public void yes() {
                            if (TextUtils.isEmpty(deviceToken)) {
                                checkDeviceToken(1000);
                            }
                        }

                        @Override
                        public void no() {
                            finish();
                            System.exit(0);
                        }

                        @Override
                        public void skip() {
                            goMain();
                        }
                    });
                } else {
                    firstBoot = false;
                    SPUtils.getInstance().put(FIRST_BOOT, firstBoot);
                    goMain();
                }
            }
        }, delay);
    }

    private void goMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void alertNotNetwork() {
        showCustomDialog("无网络",
                "请连接网络，否者无法进行必要的初始化数据",
                "重试",
                "退出",
                "跳过(无远程推送)", new DialogCallback() {
                    @Override
                    public void yes() {
                        if (!connected) {
                            alertNotNetwork();
                        } else {
                            checkDeviceToken(5000);
                        }
                    }

                    @Override
                    public void no() {
                        finish();
                        System.exit(0);
                    }

                    @Override
                    public void skip() {
                        goMain();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mNetworkChangeReceive);
        unregisterReceiver(deviceTokenReceiver);
        super.onDestroy();
    }

    interface DialogCallback {
        void yes();

        void no();

        void skip();
    }

    private void showCustomDialog(String title, String content, String positive, String negative, String neutral, DialogCallback callback) {
        dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(title)
                .setMessage(content)
                .setNeutralButton(neutral, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) callback.skip();
                    }
                })
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) callback.yes();
                    }
                })
                .setNegativeButton(negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) callback.no();
                    }
                }).setCancelable(false)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (backExit) {
            finish();
            System.exit(0);
        } else {
            backExit = true;
            ToastUtils.showShort("再按一次退出程序");
            handler.sendEmptyMessageDelayed(HANDLER_WHAT_BACK, 2000);
        }
    }
}
