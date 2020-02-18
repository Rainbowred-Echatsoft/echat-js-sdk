package com.github.echatmulti.sample;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.FragmentUtils;
import com.github.echat.chat.EChatActivity;
import com.github.echatmulti.sample.base.BaseLazyFragment;
import com.github.echatmulti.sample.utils.DataViewModel;
import com.github.echatmulti.sample.utils.RemoteNotificationUtils;

import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * @Author: xhy
 * @Email: xuhaoyang3x@gmail.com
 * @program: EChatMultiSample
 * @create: 2019-06-21
 * @describe
 */
public class OrderFragment extends BaseLazyFragment implements FragmentUtils.OnBackClickListener {

    public static OrderFragment newInstance() {
        Bundle args = new Bundle();
        OrderFragment fragment = new OrderFragment();
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
        viewModel = initViewModel();
        viewModel.loadData();
    }

    @Override
    public int bindLayout() {
        return R.layout.fragment_layout_order;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        final TextView priceTv = findViewById(R.id.pricetv);
        final TextView logisticsTv = findViewById(R.id.logisticsTv);
        findViewById(R.id.btn_open_chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChatByOrder();
            }
        });
        priceTv.setText(Html.fromHtml("<font color='#000'>实付：</font><font color='#ff3366'> ¥199.60</font>"));
        logisticsTv.setText(Html.fromHtml("<font color='#000'>物流：</font>买家已收货"));

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
    }

    private void openChatByOrder() {
        RemoteNotificationUtils.cancelAll(getContext());
        String echatTag = TextUtils.isEmpty(viewModel.echatTag2.getValue()) ? "app_android" : viewModel.echatTag2.getValue();
        String visevt = null;
        JSONObject object = new JSONObject();
        try {
            object.putOpt("eventId", "D97483381");
            object.put("title", "订单号：D97483381");
            object.put("content", "<div style=\\'color:#666;line-height:20px\\'>BADDIARY-2016秋季新款韩版高低摆连衣裙腰带套装</div><div style=\\'color:#666;line-height:20px\\'>金额：<span style=\\'color:red\\'>¥199.60</span></div><div style=\\'color:#666;line-height:20px\\'>物流：<span style=\\'color:#ccc\\'>买家已收货</span></div>");
            object.put("imageUrl", "https://demo.echatsoft.com/web/html/demoMall/url/visitorUrl/myorder/images/1.jpg");
            object.put("urlForVisitor", "http('https://demo.echatsoft.com/web/html/demoMall/url/staffUrl/myorder/order.asp?eventId=D97483381','inner')");
            object.put("urlForStaff", "http('https://demo.echatsoft.com/web/html/demoMall/url/staffUrl/myorder/order.asp?eventId=D97483381','inner')");
            object.put("memo", "下单时间：2018/12/03-10:30");
            visevt = URLEncoder.encode(object.toString(), "UTF-8");
        } catch (Exception e) {
        }
        EChatActivity.openChat(
                getContext(),
                viewModel.companyId.getValue(),
                viewModel.deviceToken.getValue(),
                viewModel.metaDataOnlyUid.getValue(),
                visevt,
                echatTag,
                "");
    }

    private DataViewModel viewModel;

    private DataViewModel initViewModel() {
        return ViewModelProviders.of((FragmentActivity) getWActivity()).get(DataViewModel.class);
    }
}
