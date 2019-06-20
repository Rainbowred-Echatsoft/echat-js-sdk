package com.github.echatmulti.sample;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.echatmulti.sample.ui.SpecialTab;
import com.github.echatmulti.sample.ui.SpecialTabRound;
import com.gyf.barlibrary.ImmersionBar;

import me.majiajie.pagerbottomtabstrip.MaterialMode;
import me.majiajie.pagerbottomtabstrip.NavigationController;
import me.majiajie.pagerbottomtabstrip.PageNavigationView;
import me.majiajie.pagerbottomtabstrip.item.BaseTabItem;
import me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectedListener;

public class MainActivity extends AppCompatActivity {
    private TextView mTextMessage;
    private ImmersionBar mImmersionBar;

    NavigationController mNavigationController;
    PageNavigationView pageBottomTabLayout;
    ViewPager viewPager;
    Toolbar toolbar;

    int[] testColors = {0xFF455A64, 0xFF00796B, 0xFF795548, 0xFF5B4947, 0xFFF57C00};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.viewPager);
        pageBottomTabLayout = findViewById(R.id.bottomFab);
        setSupportActionBar(toolbar);

        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.titleBar(toolbar)
                .keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                .init();
        toolbar.setBackgroundColor(testColors[0]);

        mNavigationController = pageBottomTabLayout.custom()
                .addItem(newItem(R.drawable.ic_home_default, R.drawable.ic_home_selected, "平台"))
                .addItem(newItem(R.drawable.ic_person_default, R.drawable.ic_person_selected, "商户1"))
                .addItem(newRoundItem(R.drawable.ic_message_default, R.drawable.ic_message_selected, "消息"))
                .addItem(newItem(R.drawable.ic_person_default, R.drawable.ic_person_selected, "商户2"))
                .addItem(newItem(R.drawable.ic_setting_default, R.drawable.ic_home_selected, "设置"))
                .build();

//        viewPager.setAdapter(new MainViewPagerAdpater(getSupportFragmentManager(), mNavigationController.getItemCount()));

        //自动适配ViewPager页面切换
        mNavigationController.setupWithViewPager(viewPager);

        //也可以设置Item选中事件的监听
        mNavigationController.addTabItemSelectedListener(new OnTabItemSelectedListener() {
            @Override
            public void onSelected(int index, int old) {
                //LogUtils.i("selected: " + index + " old: " + old);
                toolbar.setBackgroundColor(testColors[index]);
            }

            @Override
            public void onRepeat(int index) {
                //LogUtils.i("onRepeat selected: " + index);
            }
        });

        mNavigationController.setMessageNumber(2, 3);
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

        public MainViewPagerAdpater(FragmentManager fm, int size) {
            super(fm);
            this.size = size;
        }

        @Override
        public Fragment getItem(int i) {
            // TODO: 2019-06-20
            return null;
        }

        @Override
        public int getCount() {
            return size;
        }
    }

}
