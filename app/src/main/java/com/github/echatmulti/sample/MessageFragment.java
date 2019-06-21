package com.github.echatmulti.sample;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.LogUtils;
import com.github.echatmulti.sample.base.BaseLazyFragment;
import com.github.echat.chat.EChatActivity;
import com.github.echatmulti.sample.utils.DataViewModel;

/**
 * @Author: xhy
 * @Email: xuhaoyang3x@gmail.com
 * @program: EChatMultiSample
 * @create: 2019-06-21
 * @describe
 */
public class MessageFragment extends BaseLazyFragment implements FragmentUtils.OnBackClickListener {

    private TextView tvNum;

    public static MessageFragment newInstance() {

        Bundle args = new Bundle();

        MessageFragment fragment = new MessageFragment();
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
        viewModel = initViewModel();
        viewModel.loadData();
    }

    @Override
    public int bindLayout() {
        return R.layout.fragment_layout_message;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        contentView.findViewById(R.id.btn1).setOnClickListener(this::onDebouncingClick);
        contentView.findViewById(R.id.btn2).setOnClickListener(this::onDebouncingClick);
        contentView.findViewById(R.id.btn3).setOnClickListener(this::onDebouncingClick);
        contentView.findViewById(R.id.btn4).setOnClickListener(this::onDebouncingClick);
        contentView.findViewById(R.id.btn5).setOnClickListener(this::onDebouncingClick);
        contentView.findViewById(R.id.btn6).setOnClickListener(this::onDebouncingClick);
        tvNum = contentView.findViewById(R.id.tvNum);
        viewModel.unReadCount.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == null || integer <= 0) {
                    tvNum.setVisibility(View.GONE);
                } else if (integer >= 100) {
                    tvNum.setText("..");
                    tvNum.setVisibility(View.VISIBLE);
                } else {
                    tvNum.setText(integer.toString());
                    tvNum.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btn2) {
            EChatActivity.openMessageBox(
                    getContext(),
                    viewModel.platformId.getValue(),
                    viewModel.platformSgin.getValue(),
                    viewModel.deviceToken.getValue(),
                    viewModel.metaDataOnlyUid.getValue()
            );
        } else {
            startActivity(new Intent(getActivity(), HandleMessageActivity.class));
        }

    }


    private DataViewModel viewModel;

    private DataViewModel initViewModel() {
        return ViewModelProviders.of((FragmentActivity) mActivity).get(DataViewModel.class);
    }
}
