package com.ungseong.preference;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

public class PreferenceRadioGroup {

    public static final int CHECKED_NULL = -1;

    private int mCheckedIndex = CHECKED_NULL;

    private final Preference mPreference;
    private final ViewGroup mRadioRootView;

    public PreferenceRadioGroup(Preference preference, ViewGroup radioRootView) {
        mPreference = preference;
        mRadioRootView = radioRootView;
    }

    public boolean check(Preference.Radio.RadioInfo radioInfo) {
        return check(radioInfo.index);
    }

    public boolean check(int checkedIndex) {
        //같은 버튼 선택
        if (checkedIndex != CHECKED_NULL && checkedIndex == mCheckedIndex) {
            return false;
        }

        if (mCheckedIndex != CHECKED_NULL) {
            setCheckedStateForView(mCheckedIndex, false);
        }

        if (checkedIndex != CHECKED_NULL) {
            setCheckedStateForView(checkedIndex, true);
        }

        setChecked(checkedIndex);
        return true;
    }

    private void setChecked(int checkedIndex) {
        mCheckedIndex = checkedIndex;
    }

    void setIndexOnly(int checkedIndex) {
        if (checkedIndex != CHECKED_NULL) {
            mCheckedIndex = checkedIndex;
        }
    }

    private void setCheckedStateForView(int checkIndex, boolean checked) {
        if (mRadioRootView == null) return;

        View checkedView = mRadioRootView.getChildAt(checkIndex).findViewById(R.id.radio);
        if (checkedView instanceof RadioButton) {
            ((RadioButton) checkedView).setChecked(checked);
        }
    }

    interface RadioCheckedChangedListener {
        void radioCheckedChanged(PreferenceRadioGroup radioGroup, Preference preference, Preference.Radio.RadioInfo radioInfo);
    }
}
