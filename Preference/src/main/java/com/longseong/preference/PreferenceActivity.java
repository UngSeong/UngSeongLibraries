package com.longseong.preference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PreferenceActivity extends AppCompatActivity {

    public static final String INTENT_KEY_ROOT_PREFERENCE_ID = "intent.key.root_id";

    public static void setViewAndChildrenEnabled(ViewGroup view, boolean enabled, boolean childrenOnly) {
        if (!childrenOnly) {
            view.setEnabled(enabled);
        }
        for (int i = 0; i < view.getChildCount(); i++) {
            View child = view.getChildAt(i);
            child.setEnabled(enabled);
            if (child instanceof ViewGroup) {
                setViewAndChildrenEnabled((ViewGroup) child, enabled, true);
            }
        }
    }

    private Intent mIntent;
    private Preference mPreference;
    private PreferenceManager mPreferenceManager;

    private View mSwitchView;
    private TextView mDetailedContent, mSwitchAbility;
    private SwitchCompat mSwitch;

    private PreferenceListWrapper mPreferenceListWrapper;
    private NestedScrollView mDataScrollView;
    private LinearLayoutCompat mDataLayout;
    private PreferenceRadioGroup mRadioGroup;
    private LinearLayout mRadioGroupView;
    private LinearLayout mSubPreferenceListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        mIntent = getIntent();
        mDataScrollView = findViewById(R.id.preference_data_holder_layout);
        mDataLayout = (LinearLayoutCompat) mDataScrollView.getChildAt(0);

        parseIntent();

        actionBarControl();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public Preference getMainPreference() {
        return mPreference;
    }

    public NestedScrollView getDataScrollView() {return mDataScrollView;}

    public LinearLayoutCompat getDataLayout() {return mDataLayout;}

    public PreferenceListWrapper getPreferenceListWrapper() {
        return mPreferenceListWrapper;
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
        actionBar.setTitle(mPreference.getTitle());
    }

    private void parseIntent() {
        int id = mIntent.getExtras().getInt(INTENT_KEY_ROOT_PREFERENCE_ID, ResourcesCompat.ID_NULL);

        mPreferenceManager = PreferenceManager.getInstance(this);
        mPreference = PreferenceManager.getPreferenceMap().get(id);
        mPreferenceListWrapper = new PreferenceListWrapper(this);

        if (mPreference == null) {
            Toast.makeText(this, "설정값을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        //설정값의 스위치 사용 여부 확인 및 초기화
        initSwitchView();

        //설정값의 세부사항 여부 확인 및 초기화
        initDetailedDescriptionView();

        //설정값의 타입 확인 및 초기화
        initTypedView();

    }

    private void initSwitchView() {
        if (mPreference.getSwitch().isValid()) {
            mSwitchView = findViewById(R.id.preference_switchView);
            mSwitchView.setVisibility(View.VISIBLE);

            mSwitch = findViewById(R.id.preference_switch);
            mSwitchAbility = findViewById(R.id.preference_switchAbility);

            mSwitchView.setOnClickListener(v -> mSwitch.toggle());

            mSwitch.setChecked(mPreference.getSwitch().isChecked());
            rootPreferenceSwitchCheckedEvent(mPreference.getSwitch().isChecked());

            mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mIntent.putExtra("switchResult", isChecked);
                setResult(RESULT_OK, mIntent);

                rootPreferenceSwitchCheckedEvent(isChecked);
                mPreferenceManager.savePreferenceSwitchValue(mPreference);
            });
        }
    }

    private void initDetailedDescriptionView() {
        if (!mPreference.getDetailedDescription().equals("")) {
            mDetailedContent = findViewById(R.id.preference_detailedContent);
            mDetailedContent.setVisibility(View.VISIBLE);
            mDetailedContent.setText(mPreference.getDetailedDescription());
        }
    }

    private void initTypedView() {
        if (mPreference.getType() == Preference.Type.EXPLAIN_TYPE) {
            initSubPreferenceList();
        } else if (mPreference.getType() == Preference.Type.RADIO_TYPE) {
            initRadioGroup();
            initSubPreferenceList();
        }

        if (mPreference.getSwitch().isValid() && !mPreference.getSwitch().isChecked()) {
            setViewAndChildrenEnabled(mDataLayout, false, false);
        }
    }

    private void rootPreferenceSwitchCheckedEvent(boolean isChecked) {
        mPreference.getSwitch().setChecked(isChecked);
        mSwitchAbility.setText(isChecked ? "사용 중" : "사용 안 함");
        mSwitchAbility.setTypeface(mSwitchAbility.getTypeface(), isChecked ? Typeface.BOLD : Typeface.NORMAL);
        mPreference.setSubPreferencesEnabled(isChecked);
        setViewAndChildrenEnabled(mDataLayout, isChecked, false);
    }

    private void initSubPreferenceList() {
        ViewGroup itemRootView = PreferenceManager.getPreferenceListHolder(this, mDataLayout);
        mSubPreferenceListView = itemRootView.findViewById(R.id.preference_list_item_linear_layout);

        mDataLayout.addView(itemRootView);

        mPreferenceManager.bindPreferenceLayout(mPreferenceListWrapper, mPreference, mSubPreferenceListView);
    }

    private void initRadioGroup() {
        ViewGroup itemRootView = PreferenceManager.getPreferenceListHolder(this, mDataLayout);
        mRadioGroupView = itemRootView.findViewById(R.id.preference_list_item_linear_layout);

        mDataLayout.addView(itemRootView);
        mRadioGroup = new PreferenceRadioGroup(mPreference, mRadioGroupView);

        mPreferenceManager.bindRadioLayout(mPreferenceListWrapper, mPreference, mRadioGroupView, mRadioGroup, (radioGroup, preference, checkedIndex) -> {
            mIntent.putExtra("contentResult", preference.getContentValue());
            setResult(RESULT_OK, mIntent);

        });
    }
}