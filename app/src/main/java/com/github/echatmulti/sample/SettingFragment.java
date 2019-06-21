package com.github.echatmulti.sample;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.github.echatmulti.sample.base.BaseLazyFragment;
import com.github.echatmulti.sample.utils.DataViewModel;
import com.github.echat.chat.utils.EChatUtils;

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
    private EditText edPlatform;
    private EditText edBus1;
    private EditText edBus2;
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
        edPlatform = findViewById(R.id.edit_platform);
        edBus1 = findViewById(R.id.edit_bus1);
        edBus2 = findViewById(R.id.edit_bus2);
        edEncodingKey = findViewById(R.id.edit_encodingKey);
        ((TextView) findViewById(R.id.tvDevicetoken)).setText(String.format("device token:%s", dataViewModel.deviceToken.getValue()));
        edAppid.setText(dataViewModel.appid.getValue());
        edToken.setText(dataViewModel.token.getValue());
        edPlatform.setText(dataViewModel.platformId.getValue());
        edBus1.setText(dataViewModel.bus1Id.getValue());
        edBus2.setText(dataViewModel.bus2Id.getValue());
        edEncodingKey.setText(dataViewModel.encodingKey.getValue());
        findViewById(R.id.btn_save).setOnClickListener(this::onDebouncingClick);
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        LogUtils.i("onDebouncingClick");
        if (view.getId() == R.id.btn_save) {
            dataViewModel.appid.postValue(edAppid.getText().toString());
            dataViewModel.token.postValue(edToken.getText().toString());
            dataViewModel.platformId.postValue(edPlatform.getText().toString());
            dataViewModel.bus1Id.postValue(edBus1.getText().toString());
            dataViewModel.bus2Id.postValue(edBus2.getText().toString());
            dataViewModel.platformSgin.postValue(EChatUtils.getSHA1(dataViewModel.token.getValue(), dataViewModel.appid.getValue(), dataViewModel.platformId.getValue()));
            dataViewModel.encodingKey.postValue(edEncodingKey.getText().toString());
            dataViewModel.makePlatformSgin();
            dataViewModel.makeNewMetadata();
            dataViewModel.saveData();
            ToastUtils.showShort("save successful");
        }
    }

    private DataViewModel initViewModel() {
        return ViewModelProviders.of((FragmentActivity) mActivity).get(DataViewModel.class);
    }

}
