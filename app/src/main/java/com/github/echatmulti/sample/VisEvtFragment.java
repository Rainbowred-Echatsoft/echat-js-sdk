package com.github.echatmulti.sample;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.blankj.utilcode.util.FragmentUtils;
import com.bumptech.glide.Glide;
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
public class VisEvtFragment extends BaseLazyFragment implements FragmentUtils.OnBackClickListener {
    public final static String BUSINESS_NUM = "business_num";
    private int num;

    public static VisEvtFragment newInstance(int businessNum) {
        Bundle args = new Bundle();
        args.putInt(BUSINESS_NUM, businessNum);
        VisEvtFragment fragment = new VisEvtFragment();
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
        num = bundle.getInt(BUSINESS_NUM, 0);
        viewModel = initViewModel();
        viewModel.loadData();
    }

    @Override
    public int bindLayout() {
        return R.layout.fragment_layout_visevt;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        final ImageView imageView = findViewById(R.id.iv);
        if (num == 1) {
            Glide.with(getContext()).load(R.mipmap.cook1002_1).into(imageView);
        } else if (num == 2) {
            Glide.with(getContext()).load(R.mipmap.cook1003_1).into(imageView);
        }

        findViewById(R.id.btn_open_chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChat();
            }
        });
    }


    @Override
    public void onDebouncingClick(@NonNull View view) {
    }

     void openChat() {
        RemoteNotificationUtils.cancelAll(getContext());
         String echatTag = TextUtils.isEmpty(viewModel.echatTag1.getValue()) ? "app_android" : viewModel.echatTag1.getValue();

         String visevt = null;
        String companyId = viewModel.companyId.getValue();
        if (num == 1) {
            JSONObject object = new JSONObject();
            try {
                object.putOpt("eventId", "cook1002");
                object.put("title", "西西里＃韩国秋冬百搭纯色V领衬衫");
                object.put("content", "<div style=\\'color:#666;line-height:20px\\'>原价：<span style=\\'text-decoration:line-through\\'>¥185.50</span></div><div style=\\'color:#666;line-height:20px\\'>促销：<span style=\\'color:red\\'>¥104.70</span></div><div style=\\'color:#666;line-height:20px\\'>运费：<span style=\\'color:#ccc\\'>卖家承担运费</span></div>");
                object.put("imageUrl", "https://demo.echatsoft.com/web/html/demoMall/url/visitorUrl/myproduct/images/2.jpg");
                object.put("urlForVisitor", "http('https://demo.echatsoft.com/web/html/demoMall/url/staffUrl/myproduct/?eventId=cook1002','inner')");
                object.put("urlForStaff", "apiUrl(123,'hash')");
                object.put("memo", "评价（2958）");
                visevt = URLEncoder.encode(object.toString(), "UTF-8");
            } catch (Exception e) {
            }
        }/*else if (num == 2) {
            JSONObject object = new JSONObject();
            try {
                object.putOpt("eventId", "cook1003");
                object.put("title", "秋冬深V领长袖系带修身显瘦百褶裙中长款针织连衣裙");
                object.put("content", "<div style=\\'color:#666;line-height:20px\\'>原价：<span style=\\'text-decoration:line-through\\'>¥202.50</span></div><div style=\\'color:#666;line-height:20px\\'>促销：<span style=\\'color:red\\'>¥109.41</span></div><div style=\\'color:#666;line-height:20px\\'>运费：<span style=\\'color:#ccc\\'>卖家承担运费</span></div>");
                object.put("imageUrl", "https://demo.echatsoft.com/web/html/demoMall/url/visitorUrl/myproduct/images/3.jpg");
                object.put("urlForVisitor", "http('https://demo.echatsoft.com/web/html/demoMall/url/staffUrl/myproduct/?eventId=cook1003','inner')");
                object.put("urlForStaff", "apiUrl(123,'hash')");
                object.put("memo", "评价（1224）");
                visevt = URLEncoder.encode(object.toString(), "UTF-8");
            } catch (Exception e) {
            }
        }*/
        EChatActivity.openChat(
                getContext(),
                companyId,
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
