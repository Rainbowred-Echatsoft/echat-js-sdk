package com.github.echatmulti.sample;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.github.echatmulti.sample.base.BaseLazyFragment;
import com.github.echatmulti.sample.utils.Constants;
import com.github.echatmulti.sample.utils.DataViewModel;
import com.github.echat.chat.utils.EChatUtils;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static android.app.Activity.RESULT_OK;

/**
 * @Author: xhy
 * @Email: xuhaoyang3x@gmail.com
 * @program: EChatMultiSample
 * @create: 2019-06-21
 * @describe
 */
public class SettingFragment extends BaseLazyFragment implements FragmentUtils.OnBackClickListener {


    private EditText edAppid;
    private EditText edToken;
    private EditText edCompanyid;
    private EditText edEncodingKey;
    private DataViewModel dataViewModel;

    public static SettingFragment newInstance() {
        Bundle args = new Bundle();
        SettingFragment fragment = new SettingFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public boolean onBackClick() {
        if (FragmentUtils.dispatchBackPress(getChildFragmentManager())) return true;
        if (getChildFragmentManager().getBackStackEntryCount() == 0) return false;
        else {
            getChildFragmentManager().popBackStack();
            return true;
        }
    }

    @Override
    public void doLazyBusiness() {

    }

    @Override
    public void initData(@Nullable Bundle bundle) {
        dataViewModel = initViewModel();
        dataViewModel.loadData();
    }

    @Override
    public int bindLayout() {
        return R.layout.fragment_layout_setting;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        edAppid = findViewById(R.id.edit_appid);
        edToken = findViewById(R.id.edit_token);
        edCompanyid = findViewById(R.id.edit_companyid);
        edEncodingKey = findViewById(R.id.edit_encodingKey);
        ((TextView) findViewById(R.id.tvDevicetoken)).setText(String.format("device token:%s", dataViewModel.deviceToken.getValue()));
        edAppid.setText(dataViewModel.appid.getValue());
        edToken.setText(dataViewModel.token.getValue());
        edCompanyid.setText(dataViewModel.companyId.getValue());
        edEncodingKey.setText(dataViewModel.encodingKey.getValue());
        findViewById(R.id.btn_save).setOnClickListener(this::onDebouncingClick);
        findViewById(R.id.btn_qr).setOnClickListener(this::onDebouncingClick);
        findViewById(R.id.btn_reset).setOnClickListener(this::onDebouncingClick);

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btn_save) {
            dataViewModel.appid.postValue(edAppid.getText().toString());
            dataViewModel.token.postValue(edToken.getText().toString());
            dataViewModel.companyId.postValue(edCompanyid.getText().toString());
            dataViewModel.encodingKey.postValue(edEncodingKey.getText().toString());
            dataViewModel.makeNewMetadata();
            dataViewModel.saveData();
            ToastUtils.showShort("save successful");
        }

        if (view.getId() == R.id.btn_qr) {
            PermissionUtils.permission(PermissionConstants.CAMERA, PermissionConstants.STORAGE)
                    .rationale(new PermissionUtils.OnRationaleListener() {
                        @Override
                        public void rationale(ShouldRequest shouldRequest) {
                            new AlertDialog.Builder(getContext())
                                    .setTitle(android.R.string.dialog_alert_title)
                                    .setMessage(com.github.echat.chat.R.string.permission_rationale_message)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            shouldRequest.again(true);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            shouldRequest.again(false);
                                        }
                                    })
                                    .setCancelable(false)
                                    .create()
                                    .show();
                        }
                    }).callback(new PermissionUtils.SimpleCallback() {
                @Override
                public void onGranted() {
                    Intent intent = new Intent(getActivity(), CaptureActivity.class);
                    startActivityForResult(intent, Constants.RESULT_CODE_SETTINGS_QR);
                }

                @Override
                public void onDenied() {
                    new AlertDialog.Builder(getContext())
                            .setTitle(android.R.string.dialog_alert_title)
                            .setMessage(R.string.permission_denied_forever_message)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PermissionUtils.launchAppDetailsSettings();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setCancelable(false)
                            .create()
                            .show();
                }
            }).request();
        }

        if (view.getId() == R.id.btn_reset) {
            dataViewModel.resetData();
            edAppid.setText(dataViewModel.appid.getValue());
            edToken.setText(dataViewModel.token.getValue());
            edCompanyid.setText(dataViewModel.companyId.getValue());
            edEncodingKey.setText(dataViewModel.encodingKey.getValue());
            ToastUtils.showShort("还原成功");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RESULT_CODE_SETTINGS_QR && resultCode == RESULT_OK) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }

                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    LogUtils.i("解析结果：" + result);
                    try {
                        JSONObject resultObj = new JSONObject(result);
                        final String appIdString = resultObj.optString("appId");
                        final String aesKey = resultObj.optString("aesKey");
                        final String tokenString = resultObj.optString("token");
                        final String companyId = resultObj.optString("companyId");

                        dataViewModel.appid.setValue(appIdString);
                        dataViewModel.token.setValue(tokenString);
                        dataViewModel.encodingKey.setValue(aesKey);
                        dataViewModel.companyId.setValue(companyId);
                        dataViewModel.makeNewMetadata();
                        dataViewModel.saveData();

                        edAppid.setText(appIdString);
                        edToken.setText(tokenString);
                        edEncodingKey.setText(aesKey);
                        edCompanyid.setText(companyId);
                        ToastUtils.setGravity(Gravity.CENTER, 0, 0);
                        ToastUtils.showLong("修改成功");

                    } catch (JSONException e) {
                        ToastUtils.showShort("二维码格式不准确");
                    }
                }
            }
        }
    }

    private DataViewModel initViewModel() {
        return ViewModelProviders.of((FragmentActivity) mActivity).get(DataViewModel.class);
    }

}
