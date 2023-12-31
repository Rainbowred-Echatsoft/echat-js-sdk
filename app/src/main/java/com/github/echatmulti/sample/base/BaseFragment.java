package com.github.echatmulti.sample.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;


public abstract class BaseFragment extends Fragment
        implements IBaseView {

    private static final String TAG                  = "BaseFragment";
    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";

    private   WeakReference<Activity> mActivityWeakReference;
    protected LayoutInflater          mInflater;
    protected View                    mContentView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivityWeakReference = new WeakReference<>((Activity) context);
    }

    protected Activity getWActivity() {
        return mActivityWeakReference.get();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            boolean             isSupportHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);
            FragmentTransaction ft              = getFragmentManager().beginTransaction();
            if (isSupportHidden) {
                ft.hide(this);
            } else {
                ft.show(this);
            }
            ft.commitAllowingStateLoss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        mInflater = inflater;
        setRootLayout(bindLayout());
        return mContentView;
    }

    @SuppressLint("ResourceType")
    @Override
    public void setRootLayout(@LayoutRes int layoutId) {
        if (layoutId <= 0) return;
        mContentView = mInflater.inflate(layoutId, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        initData(bundle);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated: ");
        super.onActivityCreated(savedInstanceState);
        initView(savedInstanceState, mContentView);
        doBusiness();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: ");
        if (mContentView != null && mContentView.getParent() != null) {
            ((ViewGroup) mContentView.getParent()).removeView(mContentView);
        }
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: ");
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SAVE_IS_HIDDEN, isHidden());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    public <T extends View> T findViewById(@IdRes int id) {
        if (mContentView == null) throw new NullPointerException("ContentView is null.");
        return mContentView.findViewById(id);
    }
}
