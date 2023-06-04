package com.ungseong.preference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.XmlRes;
import androidx.core.content.res.ResourcesCompat;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Objects;

public class Preference {
    /**
     * Preference(이하 설정값)의 모든 데이터를 담는 클래스이다.<p>
     * <p>
     * mId : 설정값을 특정할 때 사용한다.<p>
     * mAccessName : 설정값의 SharedPreference 데이터를 특정할 때 사용한다.<p>
     * mType : 설정값의 타입을 저장한다.<p>
     * <p>
     * mTitle : 리스트로 보여지는 설정값의 제목이다.<p>
     * mDescription : 리스트로 보여지는 설정값의 설명이다. 간략한 설명을 담는다.<p>
     * mDetailedDescription : 액티비티로 보여지는 설정값의 설명이다. 자세한 설명을 담는다.<p>
     * mContentValue : 사용자에 의해 설정된 설정값의 값을 나타낸다.<p>
     * mContentValueRaw : 사용자에 의해 설정된 설정값의 가공되지 않은 값을 나타낸다.<p>
     * <p>
     * mSubPreferenceList : 설정값이 가지는 하위 설정값 리스트다. 리스트가 비어있지 않을 때 설정값의 mType이 Type.TEXT_TYPE이 아닌 경우 설정값 액티비티에서 하위 설정값들이 보여진다.<p>
     * mSwitch : 설정값의 스위치 데이터를 나타낸다.<p>
     * mRadio : 설정값의 라디오 데이터를 나타낸다.<p>
     * mInputType : 설정값의 텍스트 편집 다이얼로그의 EditText의 inputType이다. 추후 텍스트 편집 다이얼로그를 컨트롤 할 수 있는 참조형으로 바꿀 것이다.<p>
     */

    //설정값 주요 데이터
    @IdRes
    private final int mId;
    @NonNull
    private final String mAccessName;
    @NonNull
    private final Type mType;
    @DrawableRes
    private final int mIconRes;

    //설정값 데이터
    private String mTitle;
    private String mDescription;
    private String mDetailedDescription;
    private boolean mEnabled;
    private String mDefaultValue;
    private String mContentValue;
    private String mContentValueRaw;

    //선택적 개별 데이터
    private Switch mSwitch;
    private Input mInput;
    private Radio mRadio;
    private SeekBar mSeekBar;
    private Intent mIntent;
    private LinkedList<Preference> mSubPreferenceList;

    public Preference(Bundle bundle) {

        mId = bundle.getInt(Builder.KEY_ID);
        mAccessName = bundle.getString(Builder.KEY_ACCESS_NAME);
        mType = Type.valueOf(bundle.getInt(Builder.KEY_TYPE));
        mIconRes = bundle.getInt(Builder.KEY_ICON_RES);

        initData(bundle);
    }

    private void initData(Bundle bundle) {
        mTitle = bundle.getString(Builder.KEY_TITLE);
        mDescription = bundle.getString(Builder.KEY_DESCRIPTION);
        mDetailedDescription = bundle.getString(Builder.KEY_DETAILED_DESCRIPTION);
        mEnabled = bundle.getBoolean(Builder.KEY_ENABLED);
        mDefaultValue = bundle.getString(Builder.KEY_DEFAULT_VALUE);
    }

    protected void initSubPreferenceList(LinkedList<Preference> subPreferenceList) {
        mSubPreferenceList = subPreferenceList;
    }

    protected void initSwitch(Bundle bundle) {
        boolean switchUsage = bundle.getBoolean(Builder.KEY_SWITCH_USAGE) && Type.valueOf(bundle.getInt(Builder.KEY_TYPE)).switchAvailable();
        boolean switchDefaultValue = bundle.getBoolean(Builder.KEY_SWITCH_DEFAULT_VALUE);
        mSwitch = new Switch(switchUsage, switchDefaultValue);
    }

    protected void initText(Bundle bundle) {
        boolean textUsage = mType.equals(Type.TEXT_TYPE);
        int inputType = bundle.getInt(Builder.KEY_INPUT_TYPE);
        mInput = new Input(textUsage, inputType);

    }

    protected void initRadio(Bundle bundle, Context context) {
        boolean enabled = false;
        LinkedHashMap<String, Radio.RadioInfo> radioMap = null;
        @XmlRes int xmlResource = bundle.getInt(Builder.KEY_RADIO_XML_RESOURCE);
        if (xmlResource != ResourcesCompat.ID_NULL) {
            try {
                radioMap = Radio.parseXmlMap(context, xmlResource);
                enabled = true;
            } catch (XmlPullParserException | IOException ignored) {
            }
        }
        mRadio = new Radio(enabled, radioMap);
    }

    protected void initSeekBar(Bundle bundle) {
        boolean enabled = Type.SEEK_BAR_TYPE.equals(Type.valueOf(bundle.getInt(Builder.KEY_TYPE)));
        int max = bundle.getInt(Builder.KEY_SEEKBAR_MAX);
        int min = bundle.getInt(Builder.KEY_SEEKBAR_MIN);
        boolean replaceIcon = bundle.getBoolean(Builder.KEY_SEEKBAR_REPLACE_ICON);
        boolean muteUsage = bundle.getBoolean(Builder.KEY_SEEKBAR_MUTE_USAGE);

        mSeekBar = new SeekBar(mIconRes, enabled, mDefaultValue, max, min, replaceIcon, muteUsage);
    }

    protected void initIntent(Bundle bundle) {
        boolean enabled = Type.INTENT_TYPE.equals(Type.valueOf(bundle.getInt(Builder.KEY_TYPE)));
        mIntent = new Intent(enabled, mIconRes);
    }

    public int getId() {
        return mId;
    }

    public String getAccessName() {
        return mAccessName;
    }

    public Type getType() {
        return mType;
    }

    public int getIconRes() {
        return mIconRes;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setContent(String content) {
        mDescription = content;
    }

    public String getDetailedDescription() {
        return mDetailedDescription;
    }

    public void setDetailedContent(String detailedContent) {
        mDetailedDescription = detailedContent;
    }

    public String getContentValue() {
        if (mContentValue == null) {
            mContentValue = mDefaultValue;
        }
        return mContentValue;
    }

    public void setContentValue(String contentValue) {
        if (contentValue == null) {
            contentValue = mDefaultValue;
        }
        this.mContentValue = contentValue;
    }

    public String getContentValueRaw() {
        if (mContentValueRaw == null) {
            mContentValueRaw = mDefaultValue;
        }
        return mContentValueRaw;
    }

    public void setContentValueRaw(String contentValueRaw) {
        if (contentValueRaw == null) {
            contentValueRaw = mDefaultValue;
        }
        this.mContentValueRaw = contentValueRaw;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public String getDefaultValue() {
        return mDefaultValue;
    }

    public LinkedList<Preference> getSubPreferenceList() {
        return mSubPreferenceList;
    }

    public Switch getSwitch() {
        return mSwitch;
    }

    public Input getInput() {
        return mInput;
    }

    public Radio getRadio() {
        return mRadio;
    }

    public SeekBar getSeekBar() {
        return mSeekBar;
    }

    public Intent getIntent() {
        return mIntent;
    }

    protected void setSubPreferencesEnabled(boolean enabled) {
        for (Preference preference : mSubPreferenceList) {
            preference.setEnabled(enabled);
        }
    }

    public enum Type {

        /**
         * 설정값의 타입을 나타내는 열거형 enum 클래스<p>
         * <p>
         * EXPLAIN_TYPE : 기본형 설정값이다.<p>
         * TEXT_TYPE : 텍스트 수정용 다이얼로그형 설정값이다.<p>
         * RADIO_TYPE : 라디오 그룹을 포함하는 설정값이다.<p>
         * SEEK_BAR_TYPE : 시크바를 사용하는 설정값이다.<p>
         * INTENT_TYPE : 도큐먼트 등 다른 액티비티를 열어주는 설정값이다.<p>
         */

        //데이터 열거
        EXPLAIN_TYPE(0),
        TEXT_TYPE(1),
        RADIO_TYPE(2),
        SEEK_BAR_TYPE(3),
        INTENT_TYPE(4);

        static public Type valueOf(int value) {
            if (value == EXPLAIN_TYPE.value) {
                return EXPLAIN_TYPE;
            } else if (value == TEXT_TYPE.value) {
                return TEXT_TYPE;
            } else if (value == RADIO_TYPE.value) {
                return RADIO_TYPE;
            } else if (value == SEEK_BAR_TYPE.value) {
                return SEEK_BAR_TYPE;
            } else if (value == INTENT_TYPE.value) {
                return INTENT_TYPE;
            } else {
                return EXPLAIN_TYPE;
            }
        }

        private final int value;

        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public boolean switchAvailable() {
            return !(equals(SEEK_BAR_TYPE) || equals(INTENT_TYPE));
        }

    }

    public static class PreferenceComponent {

        /**
         * 설정값의 특성을 정의하기 위한 기본 클래스이다.<p>
         * <p>
         * mEnabled : 특성 사용여부이다. 변경할 수 없다.<p>
         */

        final boolean mEnabled;

        protected PreferenceComponent(boolean enabled) {
            mEnabled = enabled;
        }

        public boolean isValid() {
            return mEnabled;
        }

    }

    public static class Switch extends PreferenceComponent {

        /**
         * 설정값 우측의 스위치버튼의 데이터를 담는 클래스이다.<p>
         * <p>
         * mChecked : 스위치의 on/off 여부이다.<p>
         */

        private boolean mDefaultCheckedValue;
        private boolean mChecked;

        public Switch(boolean enabled) {
            super(enabled);
        }

        public Switch(boolean enabled, boolean mChecked) {
            super(enabled);
            this.mDefaultCheckedValue = mChecked;
        }

        public boolean isChecked() {
            return mChecked;
        }

        public void setChecked(boolean checked) {
            this.mChecked = checked;
        }

        public void toggle() {
            mChecked = !mChecked;
        }

        public boolean getDefaultValue() {
            return mDefaultCheckedValue;
        }
    }

    public static class Input extends PreferenceComponent {

        /**
         * 설정값의 텍스트 다이얼로그의 특성을 담는 클래스이다.<p>
         * <p>
         */

        private AlertDialog mDialog;
        private int mInputType;

        protected Input(boolean enabled, int inputType) {
            super(enabled);
            if (!enabled) return;
            mInputType = inputType;
        }

        @Override
        public boolean isValid() {
            return super.isValid() && mDialog != null;
        }

        public void setDialog(AlertDialog dialog) {
            this.mDialog = dialog;
        }

        public int getInputType() {
            return mInputType;
        }

        public void setInputType(int inputType) {
            mInputType = inputType;
        }
    }

    public static class Radio extends PreferenceComponent {

        /**
         * 설정값의 라디오버튼의 데이터를 담는 클래스이다.
         * 설정값의 mType이 Type.RADIO_TYPE일 경우 사용된다.
         * <p>
         * mRadioMap : 각각의 라디오버튼에 해당하는 데이터가 담겨있다.
         */

        private LinkedHashMap<String, RadioInfo> mRadioMap;

        public Radio(boolean enabled, LinkedHashMap<String, RadioInfo> radioMap) {
            super(enabled);
            if (!enabled) return;
            mRadioMap = radioMap;
        }

        @Override
        public boolean isValid() {
            return super.isValid() && mRadioMap != null && mRadioMap.size() > 0;
        }

        public LinkedHashMap<String, RadioInfo> getRadioMap() {
            return mRadioMap;
        }

        public void setRadioMap(LinkedHashMap<String, RadioInfo> radioMap) {
            mRadioMap = radioMap;
        }

        protected static LinkedHashMap<String, RadioInfo> parseXmlMap(Context context, @XmlRes int xmlResource) throws XmlPullParserException, IOException {
            LinkedHashMap<String, RadioInfo> map = new LinkedHashMap<>();
            XmlResourceParser parser = context.getResources().getXml(xmlResource);

            int resId;
            String resValue, key = null, title = null, description = null;
            String[] resAttributes = {"key", "title", "description"};

            int eventType = parser.getEventType();
            int radioInfoIndex = 0;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    Log.d("utils", "Start document");
                } else if (eventType == XmlPullParser.START_TAG) {

                    if (parser.getName().equals("entry")) {
                        for (String attr : resAttributes) {
                            resId = parser.getAttributeResourceValue(PreferenceManager.XML_NAMESPACE, attr, ResourcesCompat.ID_NULL);
                            if (resId == ResourcesCompat.ID_NULL) {
                                resValue = parser.getAttributeValue(PreferenceManager.XML_NAMESPACE, attr);
                            } else {
                                resValue = context.getString(resId);
                            }
                            if (attr.equals("key")) {
                                key = resValue;
                            } else if (attr.equals("title")) {
                                title = resValue;
                            } else if (attr.equals("description")) {
                                description = resValue;
                            }
                        }
                        if (key != null && title != null) {
                            if (key.isEmpty()) {
                                throw new XmlPullParserException(parser.getLineNumber() + ": attribute \\'key\\' must not have empty string");
                            } else {
                                map.put(key, new RadioInfo(radioInfoIndex, key, title, description));
                                radioInfoIndex++;
                            }
                        }
                    }
                }

                eventType = parser.next();
            }

            return map;
        }

        public static class RadioInfo {

            /**
             * 라디오 객체의 관리를 쉽게 하기위한 데이터 클래스
             * <p>
             * title : 라디오의 제목이다. 설정값의 값에 해당한다.
             * description : 라디오의 설명이다.
             */

            int index;
            String key;
            String title;
            String description;

            public RadioInfo(int index, String key, String title, String description) {
                this.index = index;
                this.key = key;
                this.title = title;
                this.description = description == null ? "" : description;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                RadioInfo radioInfo = (RadioInfo) o;
                return Objects.equals(key, radioInfo.key) && Objects.equals(title, radioInfo.title) && Objects.equals(description, radioInfo.description);
            }

            @Override
            public int hashCode() {
                return Objects.hash(title, description);
            }
        }
    }

    public static class SeekBar extends PreferenceComponent {

        /**
         * 설정값의 시크바의 데이터를 담는 클래스이다.<p>
         * 설정값의 mType이 Type.SEEKBAR_TYPE일 경우 사용된다.<p>
         * <p>
         * mDefaultValue : 시크바의 기본 값이다. max보다 클 수 없고 min보다 작을 수 없다.<p>
         * mProgressRaw : 시크바의 가공되지 않은 값을 나타낸다.<p>
         * mProgress : 시크바의 실제 값을 나타낸다.<p>
         * mMaxValue : 시크바의 최대값이다.<p>
         * mMinValue : 시크바의 최소값이다.<p>
         * <p>
         * mMuteUsing : 뮤트 사용 여부다.<p>
         * mMuted : 시크바 값의 뮤트 여부다.<p>
         * <p>
         * mIconRes : 아이콘의 리소스 값이다.<p>
         * mIconUsing : 아이콘 사용 여부다.<p>
         * mReplaceIcon : 대체 아이콘 사용 여부다.<p>
         */

        private int mDefaultValue;
        private int mProgressRaw;
        private int mProgress;
        private int mMaxValue;
        private int mMinValue;

        private boolean mMuteUsage;
        private boolean mMuted;

        private boolean mIconUsing;
        private boolean mReplaceIcon;

        protected SeekBarProgressChangedListener seekBarListener = null;

        public SeekBar(@DrawableRes int iconRes, boolean enabled, String defaultValue, int max, int min, boolean replaceIcon, boolean muteUsage) {
            super(enabled);
            if (!enabled) return;

            mDefaultValue = defaultValue.isEmpty() ? (max + min) / 2 : Integer.parseInt(defaultValue);
            mMaxValue = max;
            mMinValue = min;

            mMuteUsage = muteUsage;

            mReplaceIcon = replaceIcon;
            if (iconRes == ResourcesCompat.ID_NULL && !mReplaceIcon) {
                mIconUsing = false;
            }

        }

        public SeekBar(@DrawableRes int iconRes, boolean enabled, String defaultValue, int max, int min, boolean replaceIcon) {
            this(iconRes, enabled, defaultValue, max, min, replaceIcon, true);
        }

        @Override
        public boolean isValid() {
            return super.isValid() && mMaxValue > mMinValue;
        }

        public void resetToDefault() {
            setProgress(mDefaultValue);
            setProgressRaw(mDefaultValue);
        }

        public void setProgress(int value) {
            setProgressRaw(value);
            if (value > mMaxValue) {
                value = mMaxValue;
            } else if (value < mMinValue) {
                value = mMinValue;
            }
            mProgress = value;
        }

        public int getProgressValue() {
            if (mMuted) {
                return mMinValue;
            } else {
                return mProgress;
            }
        }

        protected void setProgressRaw(int value) {
            mProgressRaw = value;
        }

        public int getProgressRaw() {
            return mProgressRaw;
        }

        public int getDefaultValue() {
            return mDefaultValue;
        }

        public void setDefaultValue(int mDefaultValue) {
            this.mDefaultValue = mDefaultValue;
        }

        public int getMaxValue() {
            return mMaxValue;
        }

        public void setMaxValue(int mMaxValue) {
            this.mMaxValue = mMaxValue;
        }

        public int getMinValue() {
            return mMinValue;
        }

        public void setMinValue(int mMinValue) {
            this.mMinValue = mMinValue;
        }

        public boolean isIconHolderUsing() {
            return mIconUsing || mReplaceIcon;
        }

        public boolean isMuteUsed() {
            return mMuteUsage;
        }

        public void setReplaceIcon(boolean replaceIcon) {
            mReplaceIcon = replaceIcon;
        }

        public boolean isReplaceIcon() {
            return mReplaceIcon;
        }

        public boolean isMuted() {
            return mMuted;
        }

        public void setMute(boolean muted) {
            mMuted = muted;
        }

        public void toggleMute() {
            mMuted = !mMuted;
        }

        public void setProgressChangedListener(SeekBarProgressChangedListener listener) {
            seekBarListener = listener;
        }

        public static class SeekBarProgressChangedListener {
            public void onProgressChange() {
            }

            public void onStartChange() {
            }

            public void onStopChange() {
            }
        }
    }

    public static class Intent extends PreferenceComponent {

        /**
         * 설정값의 인텐트의 데이터를 담는 클래스이다.<p>
         * <p>
         * mLauncher : 액티비티 실행 후 결과값을 받기위한 ActivityResultLauncher<p>
         * <p>
         * mDefaultIntent : 기본 인텐트<p>
         * mDefaultUri : 기본 URI<p>
         * <p>
         * mIconRes : 아이콘의 리소스 값이다.<p>
         * mIconUsing : 아이콘 사용 여부다.<p>
         * mReplaceIcon : 대체 아이콘 사용 여부다.<p>
         */

        private ActivityResultLauncher mLauncher;

        private android.content.Intent mLaunchIntent;
        private Uri mLaunchUri;
        private Activity mLauncherActivity;

        private boolean mIconUsing;
        private boolean mLauncherNotUsing;

        public Intent(boolean enabled, @DrawableRes int iconRes) {
            this(enabled, iconRes, null);
        }

        public Intent(boolean enabled, @DrawableRes int iconRes, ActivityResultLauncher launcher) {
            super(enabled);
            if (!enabled) return;

            mLauncher = launcher;

            if (iconRes != ResourcesCompat.ID_NULL) {
                mIconUsing = true;
            }
        }

        public boolean isLaunchable() {
            return isValid() &&
                    ((mLauncher != null && (mLaunchIntent != null || mLaunchUri != null)) || (mLauncherNotUsing && !mLauncherActivity.isDestroyed() && mLaunchIntent != null))
                    ;
        }

        public void setLaunchIntent(android.content.Intent intent) {
            mLaunchIntent = intent;
        }

        public void setLaunchUri(Uri uri) {
            mLaunchUri = uri;
        }

        public void notUseLauncher(@NonNull Activity activity) {
            mLauncherActivity = activity;
            mLauncherNotUsing = true;
        }

        public void setLauncher(ActivityResultLauncher launcher) {
            mLauncher = launcher;
        }

        public ActivityResultLauncher getLauncher() {
            return mLauncher;
        }

        /*package-private*/
        void launch() {

            if (mLauncherNotUsing) {
                mLauncherActivity.startActivity(mLaunchIntent);
            } else {
                try {
                    mLauncher.launch(mLaunchIntent);
                } catch (ClassCastException e) {
                    mLauncher.launch(mLaunchUri);
                }
            }
        }

        public boolean isIconUsing() {
            return mIconUsing;
        }
    }

    public static class Builder {

        /**
         * 설정값의 인스턴스화를 돕는 Builder 클래스이다.<p>
         * <p>
         * 필드는 Preference의 필드와 동일하다.<p>
         */

        //빌더 변수
        private Context mContext;
        private Bundle mBundle;

        //필수 데이터
        public static String KEY_ID = "id";
        public static String KEY_ACCESS_NAME = "access_name";
        public static String KEY_TYPE = "type";
        public static String KEY_ICON_RES = "icon_res";

        //공통 데이터
        public static String KEY_TITLE = "title";
        public static String KEY_DESCRIPTION = "description";
        public static String KEY_DETAILED_DESCRIPTION = "detailed_description";
        public static String KEY_ENABLED = "enabled";
        public static String KEY_DEFAULT_VALUE = "default_value";
        public static String KEY_SAVED_VALUE = "saved_value";

        //선택적 개별 데이터

        // 스위치
        public static String KEY_SWITCH_USAGE = "switch_usage";
        public static String KEY_SWITCH_DEFAULT_VALUE = "switch_default_value";
        public static String KEY_SWITCH_SAVED_VALUE = "switch_saved_value";

        // 텍스트
        public static String KEY_INPUT_TYPE = "input_type";

        // 라디오
        public static String KEY_RADIO_ENABLED = "radio_enabled";
        public static String KEY_RADIO_XML_RESOURCE = "radio_xml_resource";

        //시크바
        public static String KEY_SEEKBAR_ENABLED = "seekbar_enabled";
        public static String KEY_SEEKBAR_SAVED_VALUE = "seekbar_saved_value";
        public static String KEY_SEEKBAR_MAX = "seekbar_max";
        public static String KEY_SEEKBAR_MIN = "seekbar_min";
        public static String KEY_SEEKBAR_REPLACE_ICON = "seekbar_replace_icon";
        private static final String KEY_SEEKBAR_MUTE_USAGE = "seekbar_mutable";

        //인텐트
        public static String KEY_INTENT_USAGE = "intent_usage";

        // 하위 설정값 데이터 리스트
        private final LinkedList<Preference> mSubPreferenceList = new LinkedList<>();

        public Builder(Context context) {
            mContext = context;
            mBundle = new Bundle();

            mBundle.putInt(KEY_ID, ResourcesCompat.ID_NULL);
            mBundle.putString(KEY_ACCESS_NAME, "");
            mBundle.putInt(KEY_TYPE, Type.EXPLAIN_TYPE.getValue());
            mBundle.putInt(KEY_ICON_RES, ResourcesCompat.ID_NULL);

            mBundle.putString(KEY_TITLE, "");
            mBundle.putString(KEY_DEFAULT_VALUE, "");
            mBundle.putString(KEY_DESCRIPTION, "");
            mBundle.putString(KEY_DETAILED_DESCRIPTION, "");
            mBundle.putBoolean(KEY_ENABLED, true);

            mBundle.putInt(KEY_INPUT_TYPE, EditorInfo.TYPE_CLASS_TEXT);
        }

        public Builder setId(int id) {
            mBundle.putInt(KEY_ID, id);
            return this;
        }

        public void setAccessName(String accessName) {
            mBundle.putString(KEY_ACCESS_NAME, accessName);

//            try {
//                mBundle.putString(KEY_SAVED_VALUE, PreferenceManager.getInstance(mContext).loadPreferenceContentValue(accessName));
//                mBundle.putInt(KEY_SEEKBAR_SAVED_VALUE, Integer.parseInt(PreferenceManager.getInstance(mContext).loadPreferenceContentValue(accessName)));
//                mBundle.putBoolean(KEY_SWITCH_SAVED_VALUE, PreferenceManager.getInstance(mContext).loadPreferenceSwitchValue(accessName));
//            } catch (NumberFormatException ignored) {
//                //시크바는 어차피 스위치 없으니 시크바에서 에러 났으니 스위치를 건너뛰었다가 성립되지 않음 (둘이 순서 상관 없다)
//            }
        }

        public Builder setType(@NonNull Type type) {
            mBundle.putInt(KEY_TYPE, type.getValue());
            return this;
        }

        public Builder setIconRes(@DrawableRes int iconRes) {
            mBundle.putInt(KEY_ICON_RES, iconRes);
            return this;
        }

        public Builder setTitle(@NonNull String title) {
            mBundle.putString(KEY_TITLE, title);
            return this;
        }

        public Builder setDefaultValue(@NonNull String defaultValue) {
            mBundle.putString(KEY_DEFAULT_VALUE, defaultValue);
            return this;
        }

        public Builder setDescription(@NonNull String description) {
            mBundle.putString(KEY_DESCRIPTION, description);
            return this;
        }

        public Builder setDetailedDescription(@NonNull String detailedDescription) {
            mBundle.putString(KEY_DETAILED_DESCRIPTION, detailedDescription);
            return this;
        }

        public Builder setEnabled(boolean enabled) {
            mBundle.putBoolean(KEY_ENABLED, enabled);
            return this;
        }

        public Builder setInputType(int inputType) {
            mBundle.putInt(KEY_INPUT_TYPE, inputType);
            return this;
        }

        public Builder addSubPreference(@NonNull Preference preference) {
            mSubPreferenceList.add(preference);
            return this;
        }

        public Builder setSwitchDefaultValue(boolean switchDefaultValue) {
            mBundle.putBoolean(KEY_SWITCH_DEFAULT_VALUE, switchDefaultValue);
            return this;
        }

        public Builder setSwitch(boolean switchUsage) {
            mBundle.putBoolean(KEY_SWITCH_USAGE, switchUsage);
            return this;
        }

        public Builder setRadio(@XmlRes int xmlResource) {
            mBundle.putInt(KEY_RADIO_XML_RESOURCE, xmlResource);
            return this;
        }

        public Builder setSeekBar(int max, int min, boolean replaceIcon, boolean muteUsage) {
            mBundle.putInt(KEY_SEEKBAR_MAX, max);
            mBundle.putInt(KEY_SEEKBAR_MIN, min);
            mBundle.putBoolean(KEY_SEEKBAR_REPLACE_ICON, replaceIcon);
            mBundle.putBoolean(KEY_SEEKBAR_MUTE_USAGE, muteUsage);
            return this;
        }

        @NonNull
        public Preference create() {
            Preference preference = new Preference(mBundle);
            initPreference(preference);

            return preference;
        }

        @NonNull
        public PreferenceGroup createGroup() {
            mBundle.putInt(Builder.KEY_TYPE, Type.EXPLAIN_TYPE.getValue());
            PreferenceGroup preferenceGroup = new PreferenceGroup(mBundle);
            initPreference(preferenceGroup);

            return preferenceGroup;
        }

        private void initPreference(Preference preference) {
            preference.initSubPreferenceList(mSubPreferenceList);

            preference.initSwitch(mBundle);

            preference.initText(mBundle);

            preference.initRadio(mBundle, mContext);

            preference.initSeekBar(mBundle);

            preference.initIntent(mBundle);
        }
    }

}
