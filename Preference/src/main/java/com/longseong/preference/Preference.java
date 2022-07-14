package com.longseong.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Handler;
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

import com.longseong.preference.R;

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
    private int mIconRes;

    //설정값 데이터
    private String mTitle;
    private String mDescription;
    private String mDetailedDescription;
    private boolean mEnabled;
    private String mContentValue;
    private String mContentValueRaw;

    //선택적 개별 데이터
    private Switch mSwitch;
    private Text mText;
    private Radio mRadio;
    private SeekBar mSeekBar;
    private Intent mIntent;
    private Event mEvent;
    private LinkedList<Preference> mSubPreferenceList;

    public Preference(int id, @NonNull String accessName, @NonNull Type type, @DrawableRes int iconRes, String title, String description, String detailedDescription, boolean enabled) {
        mId = id;
        mAccessName = accessName;
        mType = type;
        mIconRes = iconRes;

        initData(title, description, detailedDescription, enabled);
    }

    private void initData(String title, String description, String detailedContent, boolean enabled) {
        mTitle = title;
        mDescription = description;
        mDetailedDescription = detailedContent;
        mEnabled = enabled;
        mContentValue = "";

    }

    protected void initSubPreferenceList(LinkedList<Preference> subPreferenceList) {
        mSubPreferenceList = subPreferenceList;
    }

    protected void initSwitch(boolean switchUsage) {
        mSwitch = new Switch(switchUsage);
    }

    protected void initInputType(int inputType) {
        mText = new Text(mType.equals(Type.TEXT_TYPE));
        mText.setInputType(inputType);
    }

    protected void initRadio(boolean enabled, Context context, @XmlRes int xmlResource) {
        mRadio = new Radio(enabled, context, xmlResource);
    }

    protected void initSeekBar(boolean enabled, int defaultValue, int max, int min, boolean replaceIcon) {
        mSeekBar = new SeekBar(mIconRes, enabled, defaultValue, max, min, replaceIcon);
    }

    protected void initIntent(boolean enabled) {
        mIntent = new Intent(enabled, mIconRes);
    }

    protected void initEvent(boolean enabled) {
        mEvent = new Event(enabled);
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
        return mContentValue;
    }

    public void setContentValue(String contentValue) {
        this.mContentValue = contentValue;
    }

    public String getContentValueRaw() {
        return mContentValueRaw;
    }

    public void setContentValueRaw(String contentValueRaw) {
        this.mContentValueRaw = contentValueRaw;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public LinkedList<Preference> getSubPreferenceList() {
        return mSubPreferenceList;
    }

    public Switch getSwitch() {
        return mSwitch;
    }

    public int getInputType() {
        return mText.mInputType;
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

    public Event getEvent() {
        return mEvent;
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
        INTENT_TYPE(4),
        EVENT_TYPE(5);

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
            } else if (value == EVENT_TYPE.value) {
                return EVENT_TYPE;
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
            return !(equals(SEEK_BAR_TYPE) || equals(INTENT_TYPE) || equals(EVENT_TYPE));
        }

        /*public boolean intentAvailable() {
            return !(equals(SEEK_BAR_TYPE) || equals(TEXT_TYPE) || equals(EVENT_TYPE));
        }*/

    }

    public abstract static class Basic {

        /**
         * 설정값의 특성을 정의하기 위한 기본 클래스이다.<p>
         * <p>
         * mEnabled : 특성 사용여부이다. 변경할 수 없다.<p>
         */

        final boolean mEnabled;

        protected Basic(boolean enabled) {
            mEnabled = enabled;
        }

        public boolean isValid() {
            return mEnabled;
        }

    }

    public static class Switch extends Basic {

        /**
         * 설정값 우측의 스위치버튼의 데이터를 담는 클래스이다.<p>
         * <p>
         * mChecked : 스위치의 on/off 여부이다.<p>
         */

        private boolean mChecked;

        public Switch(boolean enabled) {
            super(enabled);
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
    }

    public static class Text extends Basic {

        /**
         * 설정값의 텍스트 다이얼로그의 특성을 담는 클래스이다.<p>
         * <p>
         */

        private AlertDialog mDialog;
        private int mInputType;

        protected Text(boolean enabled) {
            super(enabled);
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

    public static class Radio extends Basic {

        /**
         * 설정값의 라디오버튼의 데이터를 담는 클래스이다.
         * 설정값의 mType이 Type.RADIO_TYPE일 경우 사용된다.
         * <p>
         * mRadioMap : 각각의 라디오버튼에 해당하는 데이터가 담겨있다.
         */

        private LinkedHashMap<String, RadioInfo> mRadioMap;

        public Radio(boolean enabled, @NonNull Context context, @XmlRes int xmlResource) {
            super(enabled);
            if (xmlResource != ResourcesCompat.ID_NULL) {
                mRadioMap = parseXmlMap(context, xmlResource);
            }
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

        protected LinkedHashMap<String, RadioInfo> parseXmlMap(Context context, @XmlRes int xmlResource) {
            LinkedHashMap<String, RadioInfo> map = new LinkedHashMap<>();
            XmlResourceParser parser = context.getResources().getXml(xmlResource);

            int resId;
            String resValue, key = null, title = null, description = null;
            String[] resAttributes = {"key", "title", "description"};

            try {
                int eventType = parser.getEventType();

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
                                map.put(key, new RadioInfo(title, description));
                            }
                        }
                    }

                    eventType = parser.next();
                }
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
                return null;
            }

            return map;
        }

        public class RadioInfo {

            /**
             * 라디오 객체의 관리를 쉽게 하기위한 데이터 클래스
             * <p>
             * title : 라디오의 제목이다. 설정값의 값에 해당한다.
             * description : 라디오의 설명이다.
             */

            String title;
            String description;

            public RadioInfo(String title, String description) {
                this.title = title;
                this.description = description == null ? "" : description;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                RadioInfo radioInfo = (RadioInfo) o;
                return Objects.equals(title, radioInfo.title) && Objects.equals(description, radioInfo.description);
            }

            @Override
            public int hashCode() {
                return Objects.hash(title, description);
            }
        }
    }

    public static class SeekBar extends Basic {

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

        private boolean mMuteUsing;
        private boolean mMuted;

        private boolean mIconUsing;
        private boolean mReplaceIcon;

        public SeekBar(@DrawableRes int iconRes, boolean enabled, int defaultValue, int max, int min, boolean replaceIcon, boolean muteUsing) {
            super(enabled);

            mDefaultValue = defaultValue;
            mMaxValue = max;
            mMinValue = min;

            mMuteUsing = muteUsing;

            mReplaceIcon = replaceIcon;
            if (iconRes == ResourcesCompat.ID_NULL && !mReplaceIcon) {
                mIconUsing = false;
            }

        }

        public SeekBar(@DrawableRes int iconRes, boolean enabled, int defaultValue, int max, int min, boolean replaceIcon) {
            this(iconRes, enabled, defaultValue, max, min, replaceIcon, true);
        }

        @Override
        public boolean isValid() {
            return super.isValid() && mMaxValue > mMinValue;
        }

        public void resetProgressToDefault() {
            setProgress(mDefaultValue);
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

        public boolean isMutable() {
            return mMuteUsing && mMinValue == 0;
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
    }

    public static class Intent extends Basic {

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

        private android.content.Intent mDefaultIntent;
        private Uri mDefaultUri;

        private boolean mIconUsing;

        public Intent(boolean enabled, @DrawableRes int iconRes) {
            this(enabled, iconRes, null);
        }

        public Intent(boolean enabled, @DrawableRes int iconRes, ActivityResultLauncher launcher) {
            super(enabled);

            mLauncher = launcher;

            if (iconRes != ResourcesCompat.ID_NULL) {
                mIconUsing = true;
            }
        }

        public boolean isLaunchable() {
            return isValid() && mLauncher != null &&
                    (mDefaultIntent != null || mDefaultUri != null);
        }

        public void setDefaultIntent(android.content.Intent intent) {
            mDefaultIntent = intent;
        }

        public void setDefaultUri(Uri uri) {
            mDefaultUri = uri;
        }

        public void setLauncher(ActivityResultLauncher launcher) {
            mLauncher = launcher;
        }

        public ActivityResultLauncher getLauncher() {
            return mLauncher;
        }

        /*package-private*/
        void launch() {

            try {
                mLauncher.launch(mDefaultIntent);
            } catch (ClassCastException e) {
                mLauncher.launch(mDefaultUri);
            }

        }

        public boolean isIconUsing() {
            return mIconUsing;
        }
    }

    public static class Event extends Basic {

        /**
         * 설정값의 클릭이벤트를 담는 클래스이다.<p>
         * <p>
         * mEnabled : 특성 사용여부이다. 변경할 수 없다.<p>
         */

        private boolean mRunOnUiThread;
        private Handler mHandler;
        private Runnable mRunnable;

        protected Event(boolean enabled) {
            super(enabled);
        }

        @Override
        public boolean isValid() {
            return super.isValid() && mRunnable != null;
        }

        public void setRunnable(Runnable runnable) {
            setRunnable(null, runnable);
        }

        public void setRunnable(Handler handler, Runnable runnable) {
            mHandler = handler;
            mRunnable = runnable;

            setRunOnUiThread(handler != null);
        }

        private void setRunOnUiThread(boolean runOnUiThread) {
            mRunOnUiThread = runOnUiThread;
        }

        public void startEvent(Preference preference) {
            if (mRunnable == null) {
                return;
            }
            if (mRunOnUiThread) {
                mHandler.post(mRunnable);
            } else {
                new Thread(mRunnable, "click event from " + preference.getTitle()).start();
            }
        }
    }

    public static class Builder {

        /**
         * 설정값의 인스턴스화를 돕는 Builder 클래스이다.<p>
         * <p>
         * 필드는 Preference의 필드와 동일하다.<p>
         */

        //컨텍스트
        Context mContext;

        //필수 데이터
        @IdRes
        private int mId;
        @NonNull
        private String mAccessName;
        @NonNull
        private Type mType;
        @DrawableRes
        private int mIconRes;

        //데이터
        private String mTitle;
        private String mDescription;
        private String mDetailedDescription;
        private boolean mEnabled;

        //선택적 개별 데이터
        // 하위 설정값 데이터 리스트
        private final LinkedList<Preference> mSubPreferenceList = new LinkedList<>();

        // 스위치
        private boolean mSwitchUsage;

        // 텍스트
        private int mInputType;

        // 라디오
        private boolean mRadioEnabled;
        @XmlRes
        private int mRadioXmlResource;

        //시크바
        private boolean mSeekBarEnabled;
        private int mSeekBarDefaultValue;
        private int mSeekBarMax;
        private int mSeekBarMin;
        private boolean mSeekBarReplaceIcon;

        //인텐트
        private boolean mIntentUsage;

        //이벤트
        private boolean mEventUsage;

        public Builder(Context context) {
            mContext = context;

            mId = ResourcesCompat.ID_NULL;
            mAccessName = "";
            mType = Type.EXPLAIN_TYPE;
            mIconRes = ResourcesCompat.ID_NULL;

            mTitle = "";
            mDescription = "";
            mDetailedDescription = "";
            mEnabled = true;

            mInputType = EditorInfo.TYPE_CLASS_TEXT;
        }

        public Builder setId(int id) {
            mId = id;
            return this;
        }

        public void setAccessName(String accessName) {
            mAccessName = accessName;
        }

        public Builder setType(@NonNull Type type) {
            mType = type;
            return this;
        }

        public Builder setIconRes(@DrawableRes int iconRes) {
            mIconRes = iconRes;
            return this;
        }

        public Builder setTitle(@NonNull String title) {
            mTitle = title;
            return this;
        }

        public Builder setDescription(@NonNull String description) {
            mDescription = description;
            return this;
        }

        public Builder setDetailedDescription(@NonNull String detailedDescription) {
            mDetailedDescription = detailedDescription;
            return this;
        }

        public Builder setEnabled(boolean enabled) {
            mEnabled = enabled;
            return this;
        }

        public Builder setInputType(int inputType) {
            mInputType = inputType;
            return this;
        }

        public Builder addSubPreference(@NonNull Preference preference) {
            mSubPreferenceList.add(preference);
            return this;
        }

        public Builder setSwitch(boolean switchUsage) {
            mSwitchUsage = switchUsage;
            return this;
        }

        public Builder setRadio(@XmlRes int xmlResource) {
            if (xmlResource != ResourcesCompat.ID_NULL) {
                mRadioEnabled = true;
            } else {
                mRadioEnabled = false;
            }
            mRadioXmlResource = xmlResource;
            return this;
        }

        public Builder setEvent(boolean eventUsage) {
            mEventUsage = eventUsage;
            return this;
        }

        public Builder setSeekBar(int defaultValue, int max, int min, boolean replaceIcon) {

            mSeekBarEnabled = mType.equals(Type.SEEK_BAR_TYPE);
            mSeekBarDefaultValue = defaultValue;
            mSeekBarMax = max;
            mSeekBarMin = min;
            mSeekBarReplaceIcon = replaceIcon;

            return this;
        }

        public Builder setIntent() {
            mIntentUsage = mType.equals(Type.INTENT_TYPE);
            return this;
        }

        @NonNull
        public Preference create() {
            Preference preference = new Preference(mId, mAccessName, mType, mIconRes, mTitle, mDescription, mDetailedDescription, mEnabled);
            initPreference(preference);

            return preference;
        }

        @NonNull
        public PreferenceGroup createGroup() {
            PreferenceGroup preferenceGroup = new PreferenceGroup(mId, mAccessName, mTitle, mDescription, mDetailedDescription, mIconRes);
            initPreference(preferenceGroup);

            return preferenceGroup;
        }

        private void initPreference(Preference preference) {
            preference.initSubPreferenceList(mSubPreferenceList);

            preference.initSwitch(mType.switchAvailable() && mSwitchUsage);

            preference.initInputType(mInputType);

            preference.initRadio(mRadioEnabled, mContext, mRadioXmlResource);

            preference.initSeekBar(mSeekBarEnabled, mSeekBarDefaultValue, mSeekBarMax, mSeekBarMin, mSeekBarReplaceIcon);

            preference.initIntent(mType.equals(Type.INTENT_TYPE)/* && mIntentUsage*/);

            preference.initEvent(mType.equals(Type.EVENT_TYPE));
        }
    }

}
