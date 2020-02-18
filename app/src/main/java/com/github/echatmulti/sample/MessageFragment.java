package com.github.echatmulti.sample;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.github.echat.chat.EChatActivity;
import com.github.echat.chat.utils.EChatUtils;
import com.github.echatmulti.sample.base.BaseLazyFragment;
import com.github.echatmulti.sample.utils.DataViewModel;
import com.github.echatmulti.sample.utils.RemoteNotificationUtils;

import static com.github.echat.chat.utils.Constants.ACTION_LOCAL_UNREAD_COUNT;
import static com.github.echat.chat.utils.Constants.ACTION_REMOTE_UNREAD_COUNT;
import static com.github.echat.chat.utils.Constants.ACTION_UPDATE_LAST_CONTENT;
import static com.github.echat.chat.utils.Constants.CHAT_LOCAL_UNREAD_COUNT;
import static com.github.echat.chat.utils.Constants.CHAT_REMOTE_UNREAD_COUNT;
import static com.github.echat.chat.utils.Constants.NOTIFICATION_LAST_CONTENT;
import static com.github.echatmulti.sample.utils.Constants.STATUSBAR_COLOR;

/**
 * @Author: xhy
 * @Email: xuhaoyang3x@gmail.com
 * @program: EChatMultiSample
 * @create: 2019-06-21
 * @describe
 */
public class MessageFragment extends BaseLazyFragment implements FragmentUtils.OnBackClickListener {

    private final static String TAG = "MessageFragment";
    private TextView tvNum;
    private TextView tvLastContent;
    private String lastContent;
    private int color;
    private UpdateMessageReceiver receiver;

    class UpdateMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();

            //实时更新界面数据 最后一条消息内容
            if (ACTION_UPDATE_LAST_CONTENT.equals(action)) {
                if (!TextUtils.isEmpty(bundle.getString(NOTIFICATION_LAST_CONTENT))) {
                    lastContent = EChatUtils.getLastChatMsgContent();
                    LogUtils.iTag(TAG, "收到新的最后一条消息 广播：" + lastContent);
                    if (tvLastContent != null) {
                        if (TextUtils.isEmpty(lastContent)) tvLastContent.setVisibility(View.GONE);
                        tvLastContent.setText(lastContent);
                    }
                }else {
                    LogUtils.iTag(TAG, "只更改了时间戳");
                }
            }

            /**
             * 本地推送的数据
             */
            if (ACTION_LOCAL_UNREAD_COUNT.equals(action)) {
                final long localUnreadCount = bundle.getInt(CHAT_LOCAL_UNREAD_COUNT);
                LogUtils.iTag(TAG, "收到本地消息 数改变：" + viewModel.unReadCount.getValue() + " -> " + localUnreadCount);
                viewModel.loadUnreadCount();
            }

            /**
             * 远程推送的数据
             */
            else if (ACTION_REMOTE_UNREAD_COUNT.equals(action)) {
                final long remoteUnreadCount = bundle.getInt(CHAT_REMOTE_UNREAD_COUNT);
                LogUtils.iTag(TAG, "收到本地消息 数改变：" + viewModel.unReadRemoteCount.getValue() + " -> " + remoteUnreadCount);
                viewModel.loadUnreadCount();
            }
        }
    }


    public static MessageFragment newInstance(int statusBarColor) {
        Bundle args = new Bundle();
        args.putInt(STATUSBAR_COLOR, statusBarColor);
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
        viewModel.loadUnreadCount();
        color = bundle.getInt(STATUSBAR_COLOR);
        lastContent = SPUtils.getInstance().getString(NOTIFICATION_LAST_CONTENT);
        LogUtils.iTag(TAG, "这样读数据：" + viewModel.encodingKey.getValue());
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
        tvLastContent = contentView.findViewById(R.id.tv_notificationLastContent);
        if (!TextUtils.isEmpty(lastContent)) {
            tvLastContent.setVisibility(View.VISIBLE);
            tvLastContent.setText(lastContent);
        } else {
            tvLastContent.setVisibility(View.GONE);
        }
        tvNum = contentView.findViewById(R.id.tvNum);
        viewModel.unReadCount.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                final int unReadRemoteCount = viewModel.unReadRemoteCount.getValue();
                int result = integer + unReadRemoteCount;
                if (result <= 0) {
                    tvNum.setVisibility(View.GONE);
                } else if (result >= 100) {
                    tvNum.setText("..");
                    tvNum.setVisibility(View.VISIBLE);
                } else {
                    tvNum.setText(String.valueOf(result));
                    tvNum.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.unReadRemoteCount.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                final int unReadCount = viewModel.unReadCount.getValue();
                int result = integer + unReadCount;
                if (result <= 0) {
                    tvNum.setVisibility(View.GONE);
                } else if (result >= 100) {
                    tvNum.setText("..");
                    tvNum.setVisibility(View.VISIBLE);
                } else {
                    tvNum.setText(String.valueOf(result));
                    tvNum.setVisibility(View.VISIBLE);
                }
            }
        });

        receiver = new UpdateMessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE_LAST_CONTENT);
        filter.addAction(ACTION_LOCAL_UNREAD_COUNT);
        filter.addAction(ACTION_REMOTE_UNREAD_COUNT);
        getContext().registerReceiver(receiver, filter);

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.btn2) {
            RemoteNotificationUtils.cancelAll(getContext());
            EChatActivity.openChat(getContext(),
                    viewModel.companyId.getValue(),
                    viewModel.deviceToken.getValue(),
                    viewModel.metaDataOnlyUid.getValue(),
                    null,
                    "app_android",
                    viewModel.routeEntranceId.getValue());

        } else {
            final Intent intent = new Intent(getActivity(), HandleMessageActivity.class);
            intent.putExtra(STATUSBAR_COLOR, color);
            startActivity(intent);
        }

    }

    @Override
    public void onDestroyView() {
        if (receiver != null) {
            getContext().unregisterReceiver(receiver);
        }
        super.onDestroyView();
    }

    private DataViewModel viewModel;

    private DataViewModel initViewModel() {
        return ViewModelProviders.of((FragmentActivity) getWActivity()).get(DataViewModel.class);
    }
}
