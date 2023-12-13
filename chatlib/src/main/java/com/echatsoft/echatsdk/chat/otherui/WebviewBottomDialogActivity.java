package com.echatsoft.echatsdk.chat.otherui;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.echatsoft.echatsdk.chat.utils.Constants;
import com.echatsoft.echatsdk.chat.otherui.widget.EChatWebviewBottomSheetDialog;

public class WebviewBottomDialogActivity extends AppCompatActivity {
    private EChatWebviewBottomSheetDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // fixing portrait mode problem for SDK 26 if using windowIsTranslucent = true
        if (Build.VERSION.SDK_INT == 26) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        String url = getIntent().getStringExtra(Constants.EXTRA_BROWER_URL);
        if (TextUtils.isEmpty(url)) {
            finish();
            return;
        }

        dialog = new EChatWebviewBottomSheetDialog(this);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        dialog.loadUrl(url);
        dialog.show();
    }

    //取消进入动画
    @Override
    public void overridePendingTransition(int enterAnim, int exitAnim) {
        super.overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //取消后退 退出动画
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
