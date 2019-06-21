package com.github.echat.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;
import com.github.echat.chat.utils.Constants;
import com.github.echat.chat.utils.FragmentUtils;

import static com.github.echat.chat.utils.Constants.EXTRA_CHAT_URL;
import static com.github.echat.chat.utils.Constants.EXTRA_NOTIFY;

/**
 * @Author: xhy
 * @Email: xuhaoyang3x@gmail.com
 * @program: EChatMultiSample
 * @create: 2019-06-20
 * @describe
 */
public class EChatActivity extends AppCompatActivity {

    private static final String TAG = "EChatActivity";
    private boolean newIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_at_echat);

        LogUtils.iTag(TAG, "onCreate");
        LogUtils.wTag(TAG, this);
        LogUtils.iTag(TAG, FragmentUtils.getAllFragments(getSupportFragmentManager()));
        if (savedInstanceState == null) {
            final Bundle bundle = getIntent().getExtras();
            LogUtils.iTag(TAG, bundle);
            FragmentUtils.replace(getSupportFragmentManager(),
                    EChatFragment.newInstance(bundle),
                    R.id.fragment);

        } else {
            LogUtils.iTag(TAG, "onCreate: " + savedInstanceState.toString());
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (newIntent) {
            Bundle extras = getIntent().getExtras();
            boolean notify = extras.getBoolean(EXTRA_NOTIFY);
            if (notify) {
                FragmentUtils.replace(getSupportFragmentManager(),
                        EChatFragment.newInstance(extras),
                        R.id.fragment);
            }
            newIntent = false;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final Bundle bundle = intent.getExtras();
        if (bundle != null) {
            LogUtils.iTag("推送数据", "应该收到推送数据");
            //更新
            LogUtils.iTag("推送数据", "加载地址: " + bundle.getString(EXTRA_CHAT_URL));
            this.newIntent = true;
        }
        setIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if (FragmentUtils.dispatchBackPress(getSupportFragmentManager())) {
            super.onBackPressed();
        }
    }

    /**
     * 打开消息盒子
     *
     * @param companyId    平台id
     * @param platformSign 平台校验码
     * @param pushInfo     推送信息
     * @param metaData     客户加密数据
     */
    public static void openMessageBox(Context context, String companyId, String platformSign, String pushInfo, String metaData) {
        Intent intent = new Intent(context, EChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.COMPANY_ID, companyId);
        bundle.putString(Constants.PLATFORM_SIGN, platformSign);
        bundle.putString(Constants.PUSH_INFO, pushInfo);
        bundle.putString(Constants.METADATA, metaData);
        bundle.putString(Constants.TYPE, Constants.TYPE_MSGBOX);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    /**
     * 打开对话
     *
     * @param companyId    平台id
     * @param platformSign 平台校验码
     * @param pushInfo     推送信息
     * @param metaData     客户加密数据
     * @param visEvt       图文消息
     */
    public static void openChat(Context context, String companyId, String platformSign, String pushInfo, String metaData, String visEvt, String echatTag) {
        Intent intent = new Intent(context, EChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.COMPANY_ID, companyId);
        bundle.putString(Constants.PLATFORM_SIGN, platformSign);
        bundle.putString(Constants.PUSH_INFO, pushInfo);
        bundle.putString(Constants.METADATA, metaData);
        bundle.putString(Constants.VISEVT, visEvt);
        bundle.putString(Constants.ECHATTAG, echatTag);
        bundle.putString(Constants.TYPE, Constants.TYPE_CHAT);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

}
