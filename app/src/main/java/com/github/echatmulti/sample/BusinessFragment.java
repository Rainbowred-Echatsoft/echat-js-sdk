package com.github.echatmulti.sample;

import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.github.echatmulti.sample.base.BaseLazyFragment;
import com.github.echat.chat.EChatActivity;
import com.github.echatmulti.sample.utils.DataViewModel;
import com.github.echat.chat.utils.EChatUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: xhy
 * @Email: xuhaoyang3x@gmail.com
 * @program: EChatMultiSample
 * @create: 2019-06-21
 * @describe
 */
public class BusinessFragment extends BaseLazyFragment implements FragmentUtils.OnBackClickListener {
    public final static String BUSINESS_NUM = "business_num";
    private int num;

    public static BusinessFragment newInstance(int businessNum) {
        Bundle args = new Bundle();
        args.putInt(BUSINESS_NUM, businessNum);
        BusinessFragment fragment = new BusinessFragment();
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
        LogUtils.i("doLazyBusiness call()");

    }

    @Override
    public void initData(@Nullable Bundle bundle) {
        num = bundle.getInt(BUSINESS_NUM, 0);
        viewModel = initViewModel();
        viewModel.loadData();
    }

    @Override
    public int bindLayout() {
        return R.layout.fragment_layout_business;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        LogUtils.i("initView call()");
        final TextView textView = findViewById(android.R.id.text1);
        final ImageView imageView = findViewById(R.id.iv);

        textView.setText("商户" + num);
        if (num == 1) {
            Glide.with(getContext()).load(R.mipmap.cook1002_1).into(imageView);
        } else if (num == 2) {
            Glide.with(getContext()).load(R.mipmap.cook1003_1).into(imageView);
        }

        findViewById(R.id.btn_open_chat).setOnClickListener(this::onDebouncingClick);

    }


    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btn_open_chat) {
            String visevt = null;
            String companyId = null;
            if (num == 1) {
                companyId = viewModel.bus1Id.getValue();
                try {
                    visevt = URLEncoder.encode("{\"eventId\":\"cook1003\",\"title\":\"秋冬深V领长袖系带修身显瘦百褶裙中长款针织连衣裙\",\"content\":\"<div style='color:#666;line-height:20px'>原价：<span style='text-decoration:line-through'>¥185.50</span></div><div style='color:#666;line-height:20px'>促销：<span style='color:red'>¥104.70</span></div><div style='color:#666;line-height:20px'>运费：<span style='color:#ccc'>卖家承担运费</span></div>\",\"imageUrl\":\"https://echat.rainbowred.com/vmini/mycookie/images/2.jpg\",\"urlForVisitor\":\"http('https://demo.echatsoft.com/web/html/demoMall/url/staffUrl/myproduct/?eventId=cook1002','inner')\",\"urlForStaff\":\"apiUrl(123,'hash')\",\"memo\":\"评价（2958）\"}", "UTF-8");
                } catch (Exception e) {
                }
            } else if (num == 2) {
                companyId = viewModel.bus2Id.getValue();
                try {
                    visevt = URLEncoder.encode("{\"content\":\"<div style='color:#666;line-height:20px'>原价：<span style='text-decoration:line-through'>¥202.50<\\/span><\\/div><div style='color:#666;line-height:20px'>促销：<span style='color:red'>¥109.41<\\/span><\\/div><div style='color:#666;line-height:20px'>运费：<span style='color:#ccc'>卖家承担运费<\\/span><\\/div>\",\"memo\":\"评价（1224）\",\"title\":\"秋冬深V领长袖系带修身显瘦百褶裙中长款针织连衣裙\",\"imageUrl\":\"https:\\/\\/echat.rainbowred.com\\/vmini\\/mycookie\\/images\\/3.jpg\",\"urlForVisitor\":\"http('https://demo.echatsoft.com/web/html/demoMall/url/staffUrl/myproduct/?eventId=cook1003','inner')\",\"eventId\":\"cook1003\",\"urlForStaff\":\"apiUrl(123,'hash')\"}", "UTF-8");
                } catch (Exception e) {
                }
            }
            String sign = EChatUtils.getSHA1(viewModel.token.getValue(), viewModel.appid.getValue(), companyId);
            EChatActivity.openChat(
                    getContext(),
                    companyId,
                    sign,
                    viewModel.deviceToken.getValue(),
                    viewModel.metaDataOnlyUid.getValue(),
                    visevt,
                    null);
        }
    }

    private DataViewModel viewModel;

    private DataViewModel initViewModel() {
        return ViewModelProviders.of((FragmentActivity) mActivity).get(DataViewModel.class);
    }
}
