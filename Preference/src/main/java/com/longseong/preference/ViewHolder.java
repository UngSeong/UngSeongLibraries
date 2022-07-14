package com.longseong.preference;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.longseong.preference.R;

public class ViewHolder {

    public View itemView;

    public final TextView mTitle;
    public final TextView mDescription;

    /*package-private*/ Preference mPreference;

    public ViewHolder(View itemView) {
        this(itemView, null);
    }

    public ViewHolder(View itemView, Preference preference) {
        this.itemView = itemView;
        mPreference = preference;

        mTitle = itemView.findViewById(R.id.list_item_title);
        mDescription = itemView.findViewById(R.id.list_item_description);
    }

    public int getIndex() {
        ViewParent parent = itemView.getParent();
        if (parent != null) {
            return ((ViewGroup) parent).indexOfChild(itemView);
        }
        return -1;
    }

    public void update() {
        mTitle.setText(mPreference.getTitle());
        mDescription.setText(mPreference.getDescription());
    }

    public void setTitle(@NonNull String title) {
        mTitle.setText(title);
    }

    public void setDescription(@NonNull String description) {
        mDescription.setText(description);
        if (description.equals("")) {
            mDescription.setVisibility(View.GONE);
        } else {
            mDescription.setVisibility(View.VISIBLE);
        }
    }
}
