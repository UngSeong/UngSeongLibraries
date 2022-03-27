package com.longseong.preference;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public class PreferenceGroup extends Preference {

    public PreferenceGroup(int id, @NonNull String accessName, String title, String description, String detailedDescription, @DrawableRes int iconRes) {
        super(id, accessName, Type.EXPLAIN_TYPE,iconRes , title, description, detailedDescription, true);
    }

}
