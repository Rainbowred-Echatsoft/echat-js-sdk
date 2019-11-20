package com.github.echat.chat;

import android.content.Context;

/**
 * @Author: xhy
 * @Email: xuhaoyang3x@gmail.com
 * @program: EChatMultiSample
 * @create: 2019-07-08
 * @describe
 */
public class EChatCore {

    private static EChatCore core;
    private Callback callback;

    private EChatCore() {
        callback = new Callback() {
            @Override
            public boolean openLink(Context context, String url, String type) {
                return false;
            }
        };
    }

    public static EChatCore getInstance() {
        if (null == core) {
            synchronized (EChatCore.class) {
                if (null == core) {
                    core = new EChatCore();
                }
            }
        }
        return core;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public Callback getCallback() {
        return callback;
    }

    public interface Callback {
        boolean openLink(Context context, String url, String type);
    }
}
