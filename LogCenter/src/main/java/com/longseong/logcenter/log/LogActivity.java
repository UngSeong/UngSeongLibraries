package com.longseong.logcenter.log;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.longseong.logcenter.R;

public class LogActivity extends AppCompatActivity {

    private static final int ID_CLEAR_LOGCAT = R.id.menu_clear_logcat;
    private static final int ID_SCROLL_TO_END = R.id.menu_scroll_to_end;

    Toolbar toolbar;
    RecyclerView logList;
    TextView noLogAlert;

    LogListAdapter logListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Theme_LogActivity);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_log, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        } else if (itemId == ID_SCROLL_TO_END) {
            logList.smoothScrollToPosition(logList.getHeight());
        } else if (itemId == ID_CLEAR_LOGCAT) {
            LogCenter.clearLogcat(this);
            logListAdapter.setData();
            logListAdapter.notifyDataSetChanged();
            setNoLogAlertVisibility();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        initToolbar();
        initContent();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initContent() {
        logList = findViewById(R.id.log_list);
        noLogAlert = findViewById(R.id.no_log_alert);

        logList.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            logList.smoothScrollToPosition(logList.getHeight());
        });
        logListAdapter = new LogListAdapter(this);
        logList.setLayoutManager(new LinearLayoutManager(this));
        logList.setAdapter(logListAdapter);
        setNoLogAlertVisibility();
    }

    private void setNoLogAlertVisibility() {
        if (logListAdapter.getItemCount() > 0) {
            noLogAlert.setVisibility(View.GONE);
        } else {
            noLogAlert.setVisibility(View.VISIBLE);
        }
    }

}
