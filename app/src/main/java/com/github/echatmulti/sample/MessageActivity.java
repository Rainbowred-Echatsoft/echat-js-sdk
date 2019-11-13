package com.github.echatmulti.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.gyf.barlibrary.ImmersionBar;

import static com.github.echatmulti.sample.utils.Constants.STATUSBAR_COLOR;


public class MessageActivity extends AppCompatActivity {
    int color = 0xFF795548;
    private ImmersionBar mImmersionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        final Intent intent = getIntent();
        if (intent != null) {
            color = intent.getIntExtra(STATUSBAR_COLOR, 0xFF795548);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(color);
        setSupportActionBar(toolbar);

        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.titleBar(toolbar)
                .keyboardEnable(true)
                .init();

        getSupportActionBar().setTitle("消息中心");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MessageFragment.newInstance(color))
                    .commitNow();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
