package com.longseong.preference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.XmlRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class PreferenceManager {

    /**
     * PreferenceManager(이하 설정값 매니저)는 설정값의 생성 및 관리를 돕는 클래스이다.
     * 설정값 매니저는 해당 프로세스에서 하나의 인스턴스만 생성될 수 있다.
     * <p>
     * mSingleInstance : 유일한 설정값 매니저의 인스턴스
     * <p>
     * mPreferenceMap : 모든 설정값의 id와 인스턴스가 포함되어있다.
     * mPreferenceList : 최상위 설정값의 인스턴스만 포함되어있다.
     * mPreferenceListView : 최상위 설정값의 RecyclerView다.
     * mSharedPreference : SharedPreference 인스턴스이다.
     */

    public static final String XML_NAMESPACE = "http://schemas.android.com/apk/res-auto";
    public static final String ATTRIBUTE_ID = "id";
    public static final String ATTRIBUTE_ACCESS_NAME = "preference_accessName";
    public static final String ATTRIBUTE_TYPE = "preference_type";
    public static final String ATTRIBUTE_TITLE = "preference_title";
    public static final String ATTRIBUTE_DESCRIPTION = "preference_description";
    public static final String ATTRIBUTE_DETAILED_DESCRIPTION = "preference_detailedDescription";
    public static final String ATTRIBUTE_ENABLED = "preference_enabled";
    public static final String ATTRIBUTE_ICON = "preference_icon";
    public static final String ATTRIBUTE_SWITCH_USAGE = "preference_switchUsage";
    public static final String ATTRIBUTE_INPUT_TYPE = "inputType";
    public static final String ATTRIBUTE_RADIO_MAP = "preference_radioMap";
    public static final String ATTRIBUTE_DEFAULT_PROGRESS = "preference_progressDefault";
    public static final String ATTRIBUTE_MAX_PROGRESS = "preference_progressMax";
    public static final String ATTRIBUTE_MIN_PROGRESS = "preference_progressMin";
    public static final String ATTRIBUTE_REPLACE_ICON = "preference_replaceIcon";

    public static final String SWITCH_VALUE = "_switchValue";
    public static final String CONTENT_VALUE = "_contentValue";
    public static final String CONTENT_VALUE_RAW = "_contentValueRaw";

    private static PreferenceManager mSingleInstance;

    public static PreferenceManager getInstance(Context context, @XmlRes int xmlRes, SaveOptimizer saveOptimizer, PreferenceViewCreatedListener createdListener) {
        if (mSingleInstance == null) {
            mSingleInstance = new PreferenceManager(context, xmlRes, saveOptimizer, createdListener);
        }
        return mSingleInstance;
    }

    public static PreferenceManager getInstance(Context context, @XmlRes int xmlRes, SaveOptimizer saveOptimizer) {
        return getInstance(context, xmlRes, saveOptimizer, null);
    }

    public static PreferenceManager getInstance(@NonNull Context context) {
        if (mSingleInstance == null) {
            return null;
        }
        return mSingleInstance;
    }

    public static LinkedHashMap<Integer, Preference> getPreferenceMap() {
        if (mSingleInstance == null) {
            return null;
        }

        return mSingleInstance.mPreferenceMap;
    }

    public static ViewGroup getPreferenceListHolder(@NonNull Context context, @Nullable ViewGroup parent) {
        return (ViewGroup) LayoutInflater.from(context).inflate(R.layout.preference_list_item, parent, false);
    }

    private final LinkedHashMap<Integer, Preference> mPreferenceMap = new LinkedHashMap<>();

    private final LinkedList<Preference> mPreferenceList;

    private SharedPreferences mSharedPreferences;

    private NestedScrollView mPreferenceLayout;
    private PreferenceListWrapper mPreferenceListWrapper;

    private SaveOptimizer mSaveOptimizer;
    private PreferenceViewCreatedListener mPreferenceViewCreatedListener;

    public void setSaveOptimizer(SaveOptimizer optimizer) {
        if (optimizer == null) {
            mSaveOptimizer = (manager, preference, saveFlag) -> preference;
        } else {
            mSaveOptimizer = optimizer;
        }
    }

    public void setPreferenceCreatedListener(PreferenceViewCreatedListener listener) {
        if (listener == null) {
            mPreferenceViewCreatedListener = (manager, preference, isUpperCase) -> {
            };
        } else {
            mPreferenceViewCreatedListener = listener;
        }

    }

    private PreferenceManager(Context context, int xmlRes, SaveOptimizer optimizer, PreferenceViewCreatedListener listener) {
        mPreferenceList = new LinkedList<>();

        setSaveOptimizer(optimizer);
        setPreferenceCreatedListener(listener);

        try {
            parseXmlPreference(context, context.getResources().getXml(xmlRes));
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        //설정값 불러오기
        loadAllPreference();
    }

    public Preference getPreferenceById(@IdRes int id) {
        return mPreferenceMap.get(id);
    }

    public LinkedList<Preference> getPreferenceList() {
        return mPreferenceList;
    }

    public NestedScrollView getPreferenceLayout() {
        return mPreferenceLayout;
    }

    public PreferenceListWrapper getPreferenceListWrapper() {
        return mPreferenceListWrapper;
    }

    /*package-private*/
    PreferenceViewCreatedListener getPreferenceViewCreatedListener() {
        return mPreferenceViewCreatedListener;
    }

    private void parseXmlPreference(Context context, XmlResourceParser parser) throws XmlPullParserException, IOException {

        Resources resources = context.getResources();

        LinkedList<Preference.Builder> builderQueue = new LinkedList<>();
        Preference.Builder builderCursor = null;

        for (int eventType = -1; eventType != XmlResourceParser.END_DOCUMENT; eventType = parser.next()) {
            String element = parser.getName();

            if ("PreferenceSet".equals(element)) {
                if (eventType == XmlResourceParser.START_TAG) {
                    int resId = parser.getAttributeResourceValue(XML_NAMESPACE, ATTRIBUTE_ACCESS_NAME, ResourcesCompat.ID_NULL);
                    String accessName;

                    if (resId == ResourcesCompat.ID_NULL) {
                        accessName = (parser.getAttributeValue(XML_NAMESPACE, ATTRIBUTE_ACCESS_NAME));
                    } else {
                        accessName = (resources.getString(resId));
                    }

                    mSharedPreferences = context.getSharedPreferences(accessName, Activity.MODE_PRIVATE);
                }
            }

            if ("item".equals(element) || "group".equals(element)) {

                if (eventType == XmlResourceParser.START_TAG) {

                    Preference.Type type;

                    int defaultProgress = 50;
                    int maxProgress = 100;
                    int minProgress = 0;
                    boolean replaceIcon = true;

                    if (builderCursor != null) {
                        builderQueue.push(builderCursor);
                    }
                    builderCursor = new Preference.Builder(context);

                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        int resId = parser.getAttributeResourceValue(i, ResourcesCompat.ID_NULL);

                        switch (parser.getAttributeName(i)) {
                            case ATTRIBUTE_ID: {
                                builderCursor.setId(resId);
                                break;
                            }
                            case ATTRIBUTE_ACCESS_NAME: {
                                if (resId == ResourcesCompat.ID_NULL) {
                                    builderCursor.setAccessName(parser.getAttributeValue(i));
                                } else {
                                    builderCursor.setAccessName(resources.getString(resId));
                                }
                                break;
                            }
                            case ATTRIBUTE_TYPE: {
                                if (resId == ResourcesCompat.ID_NULL) {
                                    type = Preference.Type.valueOf(parser.getAttributeIntValue(i, Preference.Type.EXPLAIN_TYPE.getValue()));
                                    builderCursor.setType(type);

                                    if (type.equals(Preference.Type.INTENT_TYPE)) {
                                        builderCursor.setIntent();
                                    }
                                }
                                break;
                            }
                            case ATTRIBUTE_ICON: {
                                if (resId != ResourcesCompat.ID_NULL) {
                                    builderCursor.setIconRes(resId);
                                }
                                break;
                            }
                            case ATTRIBUTE_TITLE: {
                                if (resId == ResourcesCompat.ID_NULL) {
                                    builderCursor.setTitle(parser.getAttributeValue(i));
                                } else {
                                    builderCursor.setTitle(resources.getString(resId));
                                }
                                break;
                            }
                            case ATTRIBUTE_DESCRIPTION: {
                                if (resId == ResourcesCompat.ID_NULL) {
                                    builderCursor.setDescription(parser.getAttributeValue(i));
                                } else {
                                    builderCursor.setDescription(resources.getString(resId));
                                }
                                break;
                            }
                            case ATTRIBUTE_DETAILED_DESCRIPTION: {
                                if (resId == ResourcesCompat.ID_NULL) {
                                    builderCursor.setDetailedDescription(parser.getAttributeValue(i));
                                } else {
                                    builderCursor.setDetailedDescription(resources.getString(resId));
                                }
                                break;
                            }
                            case ATTRIBUTE_ENABLED: {
                                if (resId == ResourcesCompat.ID_NULL) {
                                    builderCursor.setEnabled(parser.getAttributeBooleanValue(i, true));
                                } else {
                                    builderCursor.setEnabled(resources.getBoolean(resId));
                                }
                                break;
                            }
                            case ATTRIBUTE_SWITCH_USAGE: {
                                if (resId == ResourcesCompat.ID_NULL) {
                                    builderCursor.setSwitch(parser.getAttributeBooleanValue(i, true));
                                } else {
                                    builderCursor.setSwitch(resources.getBoolean(resId));
                                }
                                break;
                            }
                            case ATTRIBUTE_INPUT_TYPE: {
                                builderCursor.setInputType(parser.getAttributeIntValue(i, EditorInfo.TYPE_CLASS_TEXT));
                                break;
                            }
                            case ATTRIBUTE_RADIO_MAP: {
                                builderCursor.setRadio(resId);
                                break;
                            }
                            case ATTRIBUTE_MAX_PROGRESS: {
                                if (resId == ResourcesCompat.ID_NULL) {
                                    maxProgress = parser.getAttributeIntValue(i, 100);
                                } else {
                                    maxProgress = resources.getInteger(resId);
                                }
                                break;
                            }
                            case ATTRIBUTE_MIN_PROGRESS: {
                                if (resId == ResourcesCompat.ID_NULL) {
                                    minProgress = parser.getAttributeIntValue(i, 0);
                                } else {
                                    minProgress = resources.getInteger(resId);
                                }
                                break;
                            }
                            case ATTRIBUTE_DEFAULT_PROGRESS: {
                                if (resId == ResourcesCompat.ID_NULL) {
                                    defaultProgress = parser.getAttributeIntValue(i, 50);
                                } else {
                                    defaultProgress = resources.getInteger(resId);
                                }
                                break;
                            }
                            case ATTRIBUTE_REPLACE_ICON: {
                                if (resId == ResourcesCompat.ID_NULL) {
                                    replaceIcon = parser.getAttributeBooleanValue(i, true);
                                } else {
                                    replaceIcon = resources.getBoolean(resId);
                                }
                                break;
                            }
                        }
                    }

                    builderCursor.setSeekBar(defaultProgress, maxProgress, minProgress, replaceIcon);
                    builderCursor.setIntent();

                } else if (eventType == XmlResourceParser.END_TAG) {

                    if (builderCursor == null) continue;

                    Preference preference;
                    if ("item".equals(element)) {
                        preference = builderCursor.create();
                    } else {
                        preference = builderCursor.createGroup();
                    }

                    if (builderQueue.size() == 0) {
                        //최상위 설정값은 필드변수에 대입
                        mPreferenceList.add(preference);
                        builderCursor = null;
                    } else {
                        //하위 설정값은 상위 설정값 내부에 포함
                        builderQueue.getFirst().addSubPreference(preference);
                        builderCursor = builderQueue.pop();
                    }
                    mPreferenceMap.put(preference.getId(), preference);
                }
            }
        }
    }

    private void saveAllPreferences() {
        for (Map.Entry<Integer, Preference> entry : mPreferenceMap.entrySet()) {
            if (entry.getValue() != null) {
                savePreference(entry.getValue());
            }
        }
    }

    public void savePreference(Preference preference) {
        preference = mSaveOptimizer.optimize(this, preference, SaveOptimizer.FLAG_SAVE_ALL);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        String accessName = preference.getAccessName();

        if (preference.getSwitch().isValid()) {
            editor.putBoolean(accessName + SWITCH_VALUE, preference.getSwitch().isChecked());
        }
        if (preference.getContentValue() != null) {
            editor.putString(accessName + CONTENT_VALUE, preference.getContentValue());
        }
        if (preference.getContentValueRaw() != null) {
            editor.putString(accessName + CONTENT_VALUE_RAW, preference.getContentValueRaw());
        }
        editor.apply();
    }

    public void savePreferenceSwitchValue(Preference preference) {
        preference = mSaveOptimizer.optimize(this, preference, SaveOptimizer.FLAG_SAVE_SWITCH_VALUE);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        String accessName = preference.getAccessName();

        if (preference.getSwitch().isValid()) {
            editor.putBoolean(accessName + SWITCH_VALUE, preference.getSwitch().isChecked());
        }
        editor.apply();
    }

    public void savePreferenceContentValue(Preference preference) {
        preference = mSaveOptimizer.optimize(this, preference, SaveOptimizer.FLAG_SAVE_CONTENT_VALUE);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        String accessName = preference.getAccessName();

        if (preference.getContentValue() != null) {
            editor.putString(accessName + CONTENT_VALUE, preference.getContentValue());
        }
        editor.apply();
    }

    public void savePreferenceContentValueRaw(Preference preference) {
        preference = mSaveOptimizer.optimize(this, preference, SaveOptimizer.FLAG_SAVE_CONTENT_VALUE_RAW);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        String accessName = preference.getAccessName();

        if (preference.getContentValue() != null) {
            editor.putString(accessName + CONTENT_VALUE_RAW, preference.getContentValueRaw());
        }
        editor.apply();
    }

    public void loadAllPreference() {
        for (Map.Entry<Integer, Preference> entry : mPreferenceMap.entrySet()) {
            if (entry.getValue() != null) {
                loadPreference(entry.getValue());
            }
        }
    }

    public void loadPreference(Preference preference) {
        boolean switchValue;
        String contentValue;
        String contentValueRaw;

        switchValue = loadPreferenceSwitchValue(preference);
        contentValue = loadPreferenceContentValue(preference);
        contentValueRaw = loadPreferenceContentValueRaw(preference);

        preference.getSwitch().setChecked(loadPreferenceSwitchValueInvalidateWrongData(preference));
        preference.setContentValue(loadPreferenceContentValueInvalidateWrongData(preference));
        preference.setContentValueRaw(loadPreferenceContentValueRawInvalidateWrongData(preference));

        //저장된 데이터가 없거나 현재 데이터와 다르면 다시 저장
        if (!mSharedPreferences.contains(preference.getAccessName() + SWITCH_VALUE) || switchValue != preference.getSwitch().isChecked()) {
            savePreferenceSwitchValue(preference);
        }
        if (!mSharedPreferences.contains(preference.getAccessName() + CONTENT_VALUE) || !contentValue.equals(preference.getContentValue())) {
            savePreferenceContentValue(preference);
        }
        if (!mSharedPreferences.contains(preference.getAccessName() + CONTENT_VALUE_RAW) || contentValueRaw.equals(preference.getContentValueRaw())) {
            savePreferenceContentValueRaw(preference);
        }
    }

    private boolean loadPreferenceSwitchValueInvalidateWrongData(Preference preference) {
        boolean switchChecked = loadPreferenceSwitchValue(preference);

        if (preference.getSwitch().isValid()) {
            preference.getSwitch().setChecked(switchChecked);
        }

        return switchChecked;
    }

    private String loadPreferenceContentValueInvalidateWrongData(Preference preference) {
        String contentValue = loadPreferenceContentValue(preference);

        //contentValue가 유효하지 않다면 기본값으로 첫 번째 라디오 값 선택
        if (preference.getRadio().isValid()) {
            Preference.Radio radio = preference.getRadio();
            if (!contentValue.equals("")) {
                for (Map.Entry<String, Preference.Radio.RadioInfo> entry : radio.getRadioMap().entrySet()) {
                    if (contentValue.equals(entry.getValue().title)) {
                        return contentValue;
                    }
                }
            }
            contentValue = radio.getRadioMap().entrySet().iterator().next().getValue().title;
        }

        if (preference.getSeekBar().isValid()) {
            Preference.SeekBar seekBar = preference.getSeekBar();
            try {
                seekBar.setProgress(Integer.parseInt(contentValue));
            } catch (NumberFormatException e) {
                seekBar.resetProgressToDefault();
            }
            if (seekBar.getProgressValue() == 0) {
                seekBar.setMute(true);
            }
        }

        return contentValue;
    }

    private String loadPreferenceContentValueRawInvalidateWrongData(Preference preference) {
        String contentValueRaw = loadPreferenceContentValueRaw(preference);

        if (preference.getSeekBar().isValid()) {
            Preference.SeekBar seekBar = preference.getSeekBar();
            try {
                seekBar.setProgressRaw(Integer.parseInt(contentValueRaw));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return contentValueRaw;
    }

    public boolean loadPreferenceSwitchValue(Preference preference) {
        return loadPreferenceSwitchValue(preference.getAccessName());
    }

    public boolean loadPreferenceSwitchValue(String PreferenceAccessName) {
        return mSharedPreferences.getBoolean(PreferenceAccessName + SWITCH_VALUE, false);
    }

    public String loadPreferenceContentValue(Preference preference) {
        return loadPreferenceContentValue(preference.getAccessName());
    }

    public String loadPreferenceContentValue(String PreferenceAccessName) {
        return mSharedPreferences.getString(PreferenceAccessName + CONTENT_VALUE, "");
    }

    public String loadPreferenceContentValueRaw(Preference preference) {
        return loadPreferenceContentValueRaw(preference.getAccessName());
    }

    public String loadPreferenceContentValueRaw(String PreferenceAccessName) {
        return mSharedPreferences.getString(PreferenceAccessName + CONTENT_VALUE_RAW, "");
    }

    public void registerPreferenceLayout(AppCompatActivity activityContext, NestedScrollView preferenceLayout) {
        mPreferenceLayout = preferenceLayout;
        mPreferenceListWrapper = new PreferenceListWrapper(activityContext);

        View view = getPreferenceListHolder(activityContext, preferenceLayout);

        preferenceLayout.addView(view);

        bindPreferenceLayout(mPreferenceListWrapper, mPreferenceList, view.findViewById(R.id.preference_list_item_linear_layout));
    }

    public void bindPreferenceLayout(PreferenceListWrapper wrapper, Preference preference, LinearLayout linearLayout) {
        bindPreferenceLayout(wrapper, preference.getSubPreferenceList(), linearLayout);
    }

    public void bindPreferenceLayout(PreferenceListWrapper wrapper, LinkedList<Preference> preferenceList, LinearLayout linearLayout) {
        if (linearLayout == null) return;

        wrapper.onBindPreferenceList(linearLayout, preferenceList);
    }

    void bindRadioLayout(PreferenceListWrapper wrapper, Preference preference, LinearLayout linearLayout, PreferenceRadioGroup radioGroup, PreferenceRadioGroup.RadioCheckedChangedListener listener) {
        if (linearLayout == null) return;

        wrapper.onBindRadioGroup(preference, linearLayout, radioGroup, listener);
    }

    public interface SaveOptimizer {

        int FLAG_SAVE_ALL = -1;
        int FLAG_SAVE_SWITCH_VALUE = 1;
        int FLAG_SAVE_CONTENT_VALUE = 1 << 1;
        int FLAG_SAVE_CONTENT_VALUE_RAW = 1 << 2;

        @NonNull
        Preference optimize(PreferenceManager manager, Preference preference, int saveFlag);
    }

    public interface PreferenceViewCreatedListener {

        void onCreated(PreferenceManager manager, Preference preference, AppCompatActivity activity);
    }
}
