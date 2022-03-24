package com.longseong.logcenter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LogActivity extends AppCompatActivity {

    private static final int ID_CLEAR_LOGCAT = R.id.menu_clear_logcat;
    private static final int ID_SCROLL_TO_END = R.id.menu_scroll_to_end;

    RecyclerView mLogListView;
    TextView mNoLogAlertView;

    LogListAdapter logListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logcenter_activity_log);

        actionBarControl();
        initContent();

        LogCenter.registerLogAddedListener((index) -> {
            logListAdapter.setData();
            logListAdapter.notifyItemRangeInserted(index, 1);
            mLogListView.smoothScrollToPosition(mLogListView.getHeight());
            setLogVisibility();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogCenter.registerLogAddedListener(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.logcenter_menu_log, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        } else if (itemId == ID_SCROLL_TO_END) {
            mLogListView.smoothScrollToPosition(mLogListView.getHeight());
        } else if (itemId == ID_CLEAR_LOGCAT) {
            LogCenter.clearLogcat(this);
            logListAdapter.notifyItemRangeRemoved(0, logListAdapter.getItemCount());
            logListAdapter.setData();
            setLogVisibility();
        }

        return super.onOptionsItemSelected(item);
    }

    private void actionBarControl() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            ViewGroup rootView = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content).getRootView()).getChildAt(0);
            Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.support_toolbar, rootView, false);
            rootView.addView(toolbar, 0);
            setSupportActionBar(toolbar);
            actionBar = getSupportActionBar();
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("로그");
    }

    private void initContent() {
        mLogListView = findViewById(R.id.log_list);
        mNoLogAlertView = findViewById(R.id.no_log_alert);

        mLogListView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            mLogListView.smoothScrollToPosition(mLogListView.getHeight());
        });
        logListAdapter = new LogListAdapter(this);
        mLogListView.setLayoutManager(new LinearLayoutManager(this));
        mLogListView.setAdapter(logListAdapter);
        setLogVisibility();
    }

    private void setLogVisibility() {
        if (logListAdapter.getItemCount() > 0) {
            mNoLogAlertView.setVisibility(View.GONE);
            mLogListView.setVisibility(View.VISIBLE);
        } else {
            mNoLogAlertView.setVisibility(View.VISIBLE);
            mLogListView.setVisibility(View.GONE);
        }
    }

}
