package com.github.echatmulti.sample;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.gyf.immersionbar.ImmersionBar;

import static com.github.echatmulti.sample.utils.Constants.STATUSBAR_COLOR;


public class HandleMessageActivity extends AppCompatActivity {

    int color = 0xFF795548;
    private ImmersionBar mImmersionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handle_message);

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

        getSupportActionBar().setTitle("消息");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
