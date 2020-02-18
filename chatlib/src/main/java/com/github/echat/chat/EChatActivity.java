package com.github.echat.chat;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.github.echat.chat.utils.Constants;
import com.github.echat.chat.utils.FragmentUtils;

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
        if (savedInstanceState == null) {
            final Bundle bundle = getIntent().getExtras();
            LogUtils.iTag(TAG, bundle);
            FragmentUtils.replace(getSupportFragmentManager(),
                    EChatFragment.newInstance(bundle),
                    R.id.fragment);

        } else {
            LogUtils.iTag(TAG, "onCreate: " + savedInstanceState.toString());
        }

        if (EChatFragment.checkStoragePermission()) {
            SPUtils.getInstance().put("fixMediaError2", false);
            if (!SPUtils.getInstance().getBoolean("fixMediaError2", false)) {
                //clear old version media files
                ContentResolver contentResolver = getContentResolver();
                int count = contentResolver.delete(MediaStore.Files.getContentUri("external"),
                        MediaStore.MediaColumns.DATA + " like ?",
                        new String[]{"%com.echat.echatjsdemo.single/files/DCIM/Echat%"});
                SPUtils.getInstance().put("fixMediaError2", true);
            }
        }
    }

    @Override
    protected void onRestart() {
        LogUtils.i("onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        LogUtils.i("onResume");
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
     * 打开对话
     *
     * @param companyId 公司id
     * @param pushInfo  推送信息
     * @param metaData  客户加密数据
     * @param visEvt    图文消息
     */
    public static void openChat(Context context, String companyId, String pushInfo, String metaData, String visEvt, String echatTag, String routeEntranceId) {
        Intent intent = new Intent(context, EChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.COMPANY_ID, companyId);
        bundle.putString(Constants.PUSH_INFO, pushInfo);
        bundle.putString(Constants.METADATA, metaData);
        bundle.putString(Constants.VISEVT, visEvt);
        bundle.putString(Constants.ECHATTAG, echatTag);
        bundle.putString(Constants.ROUTEENTRANCEID, routeEntranceId);
        bundle.putString(Constants.TYPE, Constants.TYPE_CHAT);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

}
