package com.github.echatmulti.sample;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.github.echat.chat.EChatActivity;
import com.github.echat.chat.utils.Constants;
import com.github.echatmulti.sample.ui.MenuItemBadge;
import com.github.echatmulti.sample.ui.SpecialTab;
import com.github.echatmulti.sample.ui.SpecialTabRound;
import com.github.echatmulti.sample.utils.DataViewModel;
import com.github.echatmulti.sample.utils.RemoteNotificationUtils;
import com.gyf.barlibrary.ImmersionBar;

import java.util.ArrayList;
import java.util.List;

import me.majiajie.pagerbottomtabstrip.NavigationController;
import me.majiajie.pagerbottomtabstrip.PageNavigationView;
import me.majiajie.pagerbottomtabstrip.item.BaseTabItem;
import me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectedListener;

import static com.github.echat.chat.utils.Constants.CHAT_UNREAD_COUNT;
import static com.github.echatmulti.sample.utils.Constants.STATUSBAR_COLOR;

public class MainActivity extends AppCompatActivity {
    private TextView mTextMessage;
    private ImmersionBar mImmersionBar;
    private DataViewModel dataViewModel;
    private ListenUnreadCountReceiver receiver;

    NavigationController mNavigationController;
    PageNavigationView pageBottomTabLayout;
    ViewPager viewPager;
    int page;
    Toolbar toolbar;

    int[] testColors = {0xFF455A64, 0xFF00796B, 0xFF795548, 0xFF5B4947, 0xFFF57C00};
    private MenuItem notificationItem;
    private MainViewPagerAdpater adapter;


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
                .addItem(newItem(R.mipmap.ic_home_default, R.mipmap.ic_home_selected, "主页"))
                .addItem(newItem(R.mipmap.ic_person_default, R.mipmap.ic_person_selected, "商品"))
                .addItem(newItem(R.mipmap.ic_order_default, R.mipmap.ic_order_selected, "订单"))
                .addItem(newItem(R.mipmap.ic_settings_default, R.mipmap.ic_settings_selected, "设置"))
                .build();

        adapter = new MainViewPagerAdpater(getSupportFragmentManager(), mNavigationController.getItemCount());
        viewPager.setAdapter(adapter);

        //自动适配ViewPager页面切换
        mNavigationController.setupWithViewPager(viewPager);

        //也可以设置Item选中事件的监听
        mNavigationController.addTabItemSelectedListener(new OnTabItemSelectedListener() {
            @Override
            public void onSelected(int index, int old) {
                LogUtils.i("selected: " + index + " old: " + old);
                toolbar.setBackgroundColor(testColors[index]);
                page = index;
                if (index == 3) {
                    notificationItem.setVisible(false);
                } else {
                    notificationItem.setVisible(true);
                }
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
                LogUtils.i(String.format("unreadCount:%d", dataViewModel.unReadCount.getValue()));

            }
        }, 5000);

        dataViewModel.unReadCount.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                final int remoteCount = dataViewModel.unReadRemoteCount.getValue();
                if (integer >= 0) {
                    if (notificationItem != null) {
                        //显示角标
                        if (integer > 0) {
                            MenuItemBadge.getBadgeTextView(notificationItem).setBadgeCount(integer + remoteCount);
                        } else {
                            MenuItemBadge.getBadgeTextView(notificationItem).setBadgeCount(0 + remoteCount);
                        }
                    }
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
        App.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dataViewModel.loadUnreadCount();//更新未读消息数
            }
        }, 300);
        receiver = new ListenUnreadCountReceiver();
        IntentFilter filter = new IntentFilter(Constants.ACTION_UNREAD_COUNT);
        registerReceiver(receiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        notificationItem = menu.findItem(R.id.action_notifications);
        MenuItemBadge.update(this, notificationItem, new MenuItemBadge.Builder()
                .iconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_notifications_24dp))
                .iconTintColor(Color.WHITE)
                .textBackgroundColor(Color.parseColor("#EF4738"))
                .textColor(Color.WHITE));
        MenuItemBadge.getBadgeTextView(notificationItem).setBadgeCount(0);
        LogUtils.i(SPUtils.getInstance().getInt(com.github.echatmulti.sample.utils.Constants.UNREAD_COUNT));
        dataViewModel.loadUnreadCount();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notifications) {
            final Intent intent = new Intent(this, MessageActivity.class);
            intent.putExtra(STATUSBAR_COLOR, testColors[page]);
            startActivity(intent);
        }
        return true;
    }

    private void openChat() {
        RemoteNotificationUtils.cancelAll(this);
        EChatActivity.openChat(
                this,
                dataViewModel.companyId.getValue(),
                dataViewModel.deviceToken.getValue(),
                dataViewModel.metaDataOnlyUid.getValue(),
                null,
                "app_android",
                ""
        );
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
            mFragments.add(HomeFragment.newInstance());
            mFragments.add(VisEvtFragment.newInstance(1));
            mFragments.add(OrderFragment.newInstance());
            mFragments.add(SettingFragment.newInstance());
        }

        @Override
        public Fragment getItem(int i) {
            return mFragments.get(i);
        }

        @Override
        public int getCount() {
            return size;
        }

        public List<Fragment> getFragments() {
            return mFragments;
        }
    }


    class ListenUnreadCountReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();

            if (Constants.ACTION_UNREAD_COUNT.equals(action)) {
                int notificationCount = bundle.getInt(CHAT_UNREAD_COUNT);
                LogUtils.iTag("MainActivity", "收到修改消息数通知 -> " + notificationCount);
                dataViewModel.unReadCount.setValue(notificationCount);
                dataViewModel.saveUnreadCount();
            }
        }
    }

}
