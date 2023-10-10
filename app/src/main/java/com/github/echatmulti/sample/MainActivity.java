package com.github.echatmulti.sample;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.github.echat.chat.EChatActivity;
import com.github.echat.chat.utils.Constants;
import com.github.echatmulti.sample.ui.MenuItemBadge;
import com.github.echatmulti.sample.ui.SpecialTab;
import com.github.echatmulti.sample.ui.SpecialTabRound;
import com.github.echatmulti.sample.utils.DataViewModel;
import com.github.echatmulti.sample.utils.RemoteNotificationUtils;
import com.gyf.immersionbar.ImmersionBar;

import java.util.ArrayList;
import java.util.List;

import me.majiajie.pagerbottomtabstrip.NavigationController;
import me.majiajie.pagerbottomtabstrip.PageNavigationView;
import me.majiajie.pagerbottomtabstrip.item.BaseTabItem;
import me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectedListener;

import static com.github.echat.chat.utils.Constants.ACTION_LOCAL_UNREAD_COUNT;
import static com.github.echat.chat.utils.Constants.ACTION_REMOTE_UNREAD_COUNT;
import static com.github.echat.chat.utils.Constants.CHAT_LOCAL_UNREAD_COUNT;
import static com.github.echat.chat.utils.Constants.CHAT_REMOTE_UNREAD_COUNT;
import static com.github.echatmulti.sample.utils.Constants.STATUSBAR_COLOR;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
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
    private MenuItem             notificationItem;
    private MainViewPagerAdapter adapter;


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

        adapter = new MainViewPagerAdapter(getSupportFragmentManager(), mNavigationController.getItemCount());
        viewPager.setAdapter(adapter);

        //自动适配ViewPager页面切换
        mNavigationController.setupWithViewPager(viewPager);

        //也可以设置Item选中事件的监听
        mNavigationController.addTabItemSelectedListener(new OnTabItemSelectedListener() {
            @Override
            public void onSelected(int index, int old) {
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
                LogUtils.i(String.format("deviceToken:%s \n metaData:%s \n unreadCount:%d \n unReadRemoteCount:%d",
                        dataViewModel.deviceToken.getValue(),
                        dataViewModel.metaDataOnlyUid.getValue(),
                        dataViewModel.unReadCount.getValue(),
                        dataViewModel.unReadRemoteCount.getValue()));
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
                LogUtils.i("更新未读消息数");
                dataViewModel.loadUnreadCount();//更新未读消息数
            }
        }, 300);
        receiver = new ListenUnreadCountReceiver();
        IntentFilter filter = new IntentFilter(ACTION_LOCAL_UNREAD_COUNT);
        filter.addAction(Constants.ACTION_REMOTE_UNREAD_COUNT);
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

    class MainViewPagerAdapter extends FragmentPagerAdapter {

        private int size;
        private List<Fragment> mFragments;

        public MainViewPagerAdapter(FragmentManager fm, int size) {
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

            /**
             * 本地推送的数据
             */
            if (ACTION_LOCAL_UNREAD_COUNT.equals(action)) {
                final long localUnreadCount = bundle.getInt(CHAT_LOCAL_UNREAD_COUNT);
                LogUtils.iTag(TAG, "收到本地消息 数改变：" + dataViewModel.unReadCount.getValue() + " -> " + localUnreadCount);
                dataViewModel.loadUnreadCount();
            }

            /**
             * 远程推送的数据
             */
            else if (ACTION_REMOTE_UNREAD_COUNT.equals(action)) {
                final long remoteUnreadCount = bundle.getInt(CHAT_REMOTE_UNREAD_COUNT);
                LogUtils.iTag(TAG, "收到本地消息 数改变：" + dataViewModel.unReadRemoteCount.getValue() + " -> " + remoteUnreadCount);
                dataViewModel.loadUnreadCount();
            }
        }
    }

}
