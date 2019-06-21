package com.github.echatmulti.sample.base;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;


public interface IBaseView {

    void initData(@Nullable Bundle bundle);

    int bindLayout();

    void setRootLayout(@LayoutRes int layoutId);

    void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView);

    void doBusiness();

    void onDebouncingClick(@NonNull View view);
}
