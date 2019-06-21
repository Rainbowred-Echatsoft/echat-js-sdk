package com.github.echatmulti.sample;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.github.echat.chat.EChatActivity;
import com.github.echat.chat.utils.Constants;
import com.github.echat.chat.utils.EChatUtils;
import com.github.echatmulti.sample.ui.SpecialTab;
import com.github.echatmulti.sample.ui.SpecialTabRound;
import com.github.echatmulti.sample.utils.DataViewModel;
import com.gyf.barlibrary.ImmersionBar;

import java.util.ArrayList;
import java.util.List;

import me.majiajie.pagerbottomtabstrip.NavigationController;
import me.majiajie.pagerbottomtabstrip.PageNavigationView;
import me.majiajie.pagerbottomtabstrip.item.BaseTabItem;
import me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectedListener;

import static com.github.echat.chat.utils.Constants.CHAT_LAST_CHAT_TIME;
import static com.github.echat.chat.utils.Constants.CHAT_UNREAD_COUNT;
import static com.github.echat.chat.utils.Constants.EXTRA_COMPANY_ID;
import static com.github.echat.chat.utils.Constants.EXTRA_NOTIFY;
import static com.github.echat.chat.utils.Constants.EXTRA_URL;
import static com.github.echatmulti.sample.utils.Constants.LASTCHAT;
import static com.github.echatmulti.sample.utils.Constants.UNREAD_COUNT;

public class MainActivity extends AppCompatActivity {
    private TextView mTextMessage;
    private ImmersionBar mImmersionBar;
    private DataViewModel dataViewModel;
    private ListenUnreadCountReceiver receiver;

    NavigationController mNavigationController;
    PageNavigationView pageBottomTabLayout;
    ViewPager viewPager;
    Toolbar toolbar;

    int[] testColors = {0xFF455A64, 0xFF00796B, 0xFF795548, 0xFF5B4947, 0xFFF57C00};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataViewModel = initViewModel();

        LogUtils.i("onCreate");
        LogUtils.i(getIntent().getExtras());

        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.viewPager);
        pageBottomTabLayout = findViewById(R.id.bottomFab);
        setSupportActionBar(toolbar);

        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.titleBar(toolbar)
                .keyboardEnable(true)
                .init();

        toolbar.setBackgroundColor(testColors[0]);

        mNavigationController = pageBottomTabLayout.custom()
                .addItem(newItem(R.mipmap.ic_home_default, R.mipmap.ic_home_selected, "平台"))
                .addItem(newItem(R.mipmap.ic_person_default, R.mipmap.ic_person_selected, "商户1"))
                .addItem(newRoundItem(R.drawable.ic_message_default, R.drawable.ic_message_selected, "消息"))
                .addItem(newItem(R.mipmap.ic_person_default, R.mipmap.ic_person_selected, "商户2"))
                .addItem(newItem(R.mipmap.ic_settings_default, R.mipmap.ic_settings_selected, "设置"))
                .build();

        viewPager.setAdapter(new MainViewPagerAdpater(getSupportFragmentManager(), mNavigationController.getItemCount()));

        //自动适配ViewPager页面切换
        mNavigationController.setupWithViewPager(viewPager);

        //也可以设置Item选中事件的监听
        mNavigationController.addTabItemSelectedListener(new OnTabItemSelectedListener() {
            @Override
            public void onSelected(int index, int old) {
                LogUtils.i("selected: " + index + " old: " + old);
                toolbar.setBackgroundColor(testColors[index]);
            }

            @Override
            public void onRepeat(int index) {
                LogUtils.i("onRepeat selected: " + index);
            }
        });


        App.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtils.i(String.format("deviceToken:%s", dataViewModel.deviceToken.getValue()));
                LogUtils.i(String.format("metaData:%s", dataViewModel.metaDataOnlyUid.getValue()));
                LogUtils.i(String.format("platformSgin:%s", dataViewModel.platformSgin.getValue()));

            }
        }, 5000);

        dataViewModel.unReadCount.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer >= 0) {
                    mNavigationController.setMessageNumber(2, integer);
                }
            }
        });
        dataViewModel.loadData();
        dataViewModel.getUnreadCountFromNetwork(this);
    }

    @Override
    public void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        LogUtils.i("onNewIntent");
        setIntent(newIntent);
    }


    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.i("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.i("onStop");
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.i("onResume");
        dataViewModel.loadUnreadCount();//更新未读消息数
        receiver = new ListenUnreadCountReceiver();
        IntentFilter filter = new IntentFilter(Constants.ACTION_UNREAD_COUNT);
        registerReceiver(receiver, filter, Constants.BroadcastPermission.MESSAGE_SEND_PERMISSION, null);
    }

    private DataViewModel initViewModel() {
        return ViewModelProviders.of(this).get(DataViewModel.class);
    }

    /**
     * 正常tab
     */
    private BaseTabItem newItem(int drawable, int checkedDrawable, String text) {
        SpecialTab mainTab = new SpecialTab(this);
        mainTab.initialize(drawable, checkedDrawable, text);
        mainTab.setTextDefaultColor(0xFF888888);
        mainTab.setTextCheckedColor(0xFFdd264a);
        return mainTab;
    }

    /**
     * 圆形tab
     */
    private BaseTabItem newRoundItem(int drawable, int checkedDrawable, String text) {
        SpecialTabRound mainTab = new SpecialTabRound(this);
        mainTab.initialize(drawable, checkedDrawable, text);
        mainTab.setTextDefaultColor(0xFF888888);
        mainTab.setTextCheckedColor(0xFFdd264a);
        return mainTab;
    }

    class MainViewPagerAdpater extends FragmentPagerAdapter {

        private int size;
        private List<Fragment> mFragments;

        public MainViewPagerAdpater(FragmentManager fm, int size) {
            super(fm);
            this.size = size;
            mFragments = new ArrayList<>();
            mFragments.add(PlatformFragment.newInstance());
            mFragments.add(BusinessFragment.newInstance(1));
            mFragments.add(MessageFragment.newInstance());
            mFragments.add(BusinessFragment.newInstance(2));
            mFragments.add(SettingFragment.newInstance());
        }

        @Override
        public Fragment getItem(int i) {
            LogUtils.i("ViewPagerAdpater", "getItem: " + i);
            return mFragments.get(i);
        }

        @Override
        public int getCount() {
            return size;
        }
    }


    class ListenUnreadCountReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();

            if (Constants.ACTION_UNREAD_COUNT.equals(action)) {
                int notificationCount = bundle.getInt(CHAT_UNREAD_COUNT);
                dataViewModel.unReadCount.postValue(notificationCount);
            }
        }
    }

}
