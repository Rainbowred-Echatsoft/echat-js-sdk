package com.github.echatmulti.sample;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.FragmentUtils;
import com.github.echat.chat.EChatActivity;
import com.github.echatmulti.sample.base.BaseLazyFragment;
import com.github.echatmulti.sample.utils.DataViewModel;
import com.github.echatmulti.sample.utils.RemoteNotificationUtils;

/**
 * @Author: xhy
 * @Email: xuhaoyang3x@gmail.com
 * @program: EChatMultiSample
 * @create: 2019-06-21
 * @describe
 */
public class HomeFragment extends BaseLazyFragment implements FragmentUtils.OnBackClickListener {
    private DataViewModel viewModel;

    public static HomeFragment newInstance() {
        Bundle args = new Bundle();
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        return R.layout.fragment_layout_home;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        findViewById(R.id.btn_open_chat).setOnClickListener(this::onDebouncingClick);

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btn_open_chat) {
            openChat();
        }
    }

    private void openChat() {
        RemoteNotificationUtils.cancelAll(getContext());
        EChatActivity.openChat(
                getContext(),
                viewModel.companyId.getValue(),
                viewModel.deviceToken.getValue(),
                viewModel.metaDataOnlyUid.getValue(),
                null,
                "app_android"
        );
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

    private DataViewModel initViewModel() {
        return ViewModelProviders.of((FragmentActivity) mActivity).get(DataViewModel.class);
    }
}
