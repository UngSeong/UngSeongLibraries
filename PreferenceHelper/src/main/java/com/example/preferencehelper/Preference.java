package com.example.preferencehelper;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;

import java.util.LinkedList;

public class Preference {

    public enum Mode {
        SWITCH_MODE,
        SEEKBAR_MODE,
        EDITTEXT_MODE,
        INTENT_MODE
    }

    private int mId;
    private Mode mMode;
    private String mTitle;
    private String mContent;
    private boolean mEnabled;

    private final LinkedList<Preference> mSubPreferenceList;

    public Preference(Context context) {
        mSubPreferenceList = new LinkedList<>();

    }


}
