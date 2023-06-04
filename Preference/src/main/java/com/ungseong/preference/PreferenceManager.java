package com.ungseong.preference;

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
    public static final String ATTRIBUTE_DEFAULT_VALUE = "preference_defaultValue";
    public static final String ATTRIBUTE_DESCRIPTION = "preference_description";
    public static final String ATTRIBUTE_DETAILED_DESCRIPTION = "preference_detailedDescription";
    public static final String ATTRIBUTE_ENABLED = "preference_enabled";
    public static final String ATTRIBUTE_ICON = "preference_icon";
    public static final String ATTRIBUTE_SWITCH_USAGE = "preference_switchUsage";
    public static final String ATTRIBUTE_SWITCH_DEFAULT_VALUE = "preference_switchDefaultValue";
    public static final String ATTRIBUTE_INPUT_TYPE = "inputType";
    public static final String ATTRIBUTE_RADIO_MAP = "preference_radioMap";
    public static final String ATTRIBUTE_MAX_VALUE = "preference_maxValue";
    public static final String ATTRIBUTE_MIN_VALUE = "preference_minValue";
    public static final String ATTRIBUTE_REPLACE_ICON = "preference_replaceIcon";
    public static final String ATTRIBUTE_MUTE_USAGE = "preference_muteUsage";

    public static final String SWITCH_VALUE = "_switchValue";
    public static final String CONTENT_VALUE = "_contentValue";
    public static final String CONTENT_VALUE_RAW = "_contentValueRaw";

    private static PreferenceManager mSingleInstance;

    /**
     * use newInstance instead
     */
    @Deprecated
    public static PreferenceManager getInstance(Context context, @XmlRes int xmlRes, SaveOptimizer saveOptimizer, PreferenceViewCreatedListener createdListener) {
        if (mSingleInstance == null) {
            mSingleInstance = new PreferenceManager(context, xmlRes, saveOptimizer, createdListener);
        }
        return mSingleInstance;
    }

    /**
     * use newInstance instead
     */
    @Deprecated
    public static PreferenceManager getInstance(Context context, @XmlRes int xmlRes, SaveOptimizer saveOptimizer) {
        return getInstance(context, xmlRes, saveOptimizer, null);
    }

    public static PreferenceManager newInstance(Context context, @XmlRes int xmlRes, SaveOptimizer saveOptimizer, PreferenceViewCreatedListener createdListener) {
        if (mSingleInstance == null) {
            mSingleInstance = new PreferenceManager(context, xmlRes, saveOptimizer, createdListener);
        }
        return mSingleInstance;
    }

    public static PreferenceManager newInstance(Context context, @XmlRes int xmlRes, SaveOptimizer saveOptimizer) {
        return newInstance(context, xmlRes, saveOptimizer, null);
    }

    public static PreferenceManager newInstance(Context context, @XmlRes int xmlRes) {
        return newInstance(context, xmlRes, null, null);
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
    private final LinkedHashMap<Integer, ViewHolder> mViewHolderMap = new LinkedHashMap<>();

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

    protected void addViewHolder(ViewHolder holder) {
        mViewHolderMap.put(holder.mPreference.getId(), holder);
    }

    protected void destroyViewHolder(Activity activity) {
        mViewHolderMap.entrySet().removeIf(integerViewHolderEntry -> ((Activity) integerViewHolderEntry.getValue().itemView.getContext()).isDestroyed());
    }

    public ViewHolder getViewHolder(Preference preference) {
        return getViewHolder(preference.getId());
    }

    public ViewHolder getViewHolder(int id) {
        return mViewHolderMap.get(id);
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

                    //SeekBar default attribute data
                    int maxProgress = 100;
                    int minProgress = 0;
                    boolean replaceIcon = true;
                    boolean muteUsage = false;

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
                            case ATTRIBUTE_DEFAULT_VALUE: {
                                if (resId == ResourcesCompat.ID_NULL) {
                                    builderCursor.setDefaultValue(parser.getAttributeValue(i));
                                } else {
                                    builderCursor.setDefaultValue(resources.getString(resId));

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
                            case ATTRIBUTE_SWITCH_DEFAULT_VALUE: {
                                if (resId == ResourcesCompat.ID_NULL) {
                                    builderCursor.setSwitchDefaultValue(parser.getAttributeBooleanValue(i, true));
                                } else {
                                    builderCursor.setSwitchDefaultValue(resources.getBoolean(resId));
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
                            case ATTRIBUTE_MAX_VALUE: {
                                if (resId == ResourcesCompat.ID_NULL) {
                                    maxProgress = parser.getAttributeIntValue(i, 100);
                                } else {
                                    maxProgress = resources.getInteger(resId);
                                }
                                break;
                            }
                            case ATTRIBUTE_MIN_VALUE: {
                                if (resId == ResourcesCompat.ID_NULL) {
                                    minProgress = parser.getAttributeIntValue(i, 0);
                                } else {
                                    minProgress = resources.getInteger(resId);
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
                            case ATTRIBUTE_MUTE_USAGE: {
                                if (resId == ResourcesCompat.ID_NULL) {
                                    muteUsage = parser.getAttributeBooleanValue(i, false);
                                } else {
                                    muteUsage = resources.getBoolean(resId);
                                }
                                break;
                            }
                        }
                    }

                    builderCursor.setSeekBar(maxProgress, minProgress, replaceIcon, muteUsage);

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
        String[] contentValues = new String[]{loadPreferenceContentValue(preference), loadPreferenceContentValueRaw(preference)};
        boolean dataChanged;

        preference.getSwitch().setChecked(loadPreferenceSwitchValue(preference));
        dataChanged = setPreferenceContentValuesCorrectWrongData(preference, contentValues);

        //저장되어 있는 데이터가 설정값 타입의 형식에 맞지 않게 저장되었다면 형식화된 값을 새롭게 저장함
        if (dataChanged) {
            savePreferenceContentValue(preference);
            savePreferenceContentValueRaw(preference);
        }
    }

    private boolean setPreferenceContentValuesCorrectWrongData(Preference preference, String[] contentValues) {
        //contentValueRaw가 유효하지 않다면 기본값으로 첫 번째 라디오 값 선택
        if (preference.getRadio().isValid()) {
            Preference.Radio radio = preference.getRadio();
            if (!contentValues[1].isEmpty()) {
                for (Map.Entry<String, Preference.Radio.RadioInfo> entry : radio.getRadioMap().entrySet()) {
                    if (contentValues[1].equals(entry.getValue().key)) {
                        //라디오와 시크바는 공존할 수 없으며 라디오 체크후 문제 없으므로 false return
                        preference.setContentValue(entry.getValue().title);
                        return false;
                    }
                }
            }
            Preference.Radio.RadioInfo radioInfo = radio.getRadioMap().entrySet().iterator().next().getValue();
            preference.setContentValueRaw(radioInfo.key);
            preference.setContentValue(radioInfo.title);
            //라디오와 시크바는 공존할 수 없으며 라디오 체크후 문제가 있으므로 true return
            return true;
        }

        if (preference.getSeekBar().isValid()) {
            boolean correctionEmergence = false;
            Preference.SeekBar seekBar = preference.getSeekBar();

            try {
                seekBar.setProgress(Integer.parseInt(contentValues[0]));
                seekBar.setProgressRaw(Integer.parseInt(contentValues[1]));
            } catch (NumberFormatException e) {
                seekBar.resetToDefault();
                correctionEmergence = true;
            }
            preference.setContentValueRaw(seekBar.getProgressRaw() + "");
            preference.setContentValue(seekBar.getProgressValue() + "");

            if (seekBar.getProgressValue() == seekBar.getMinValue() && seekBar.isMuteUsed()) {
                seekBar.setMute(true);
            }
            return correctionEmergence;
        }

        //아무 문제 없으므로 false return
        return false;
    }

    public boolean loadPreferenceSwitchValue(Preference preference) {
        return loadPreferenceSwitchValue(preference.getAccessName(), preference.getSwitch().getDefaultValue());
    }

    protected boolean loadPreferenceSwitchValue(String PreferenceAccessName, boolean switchDefaultValue) {
        return mSharedPreferences.getBoolean(PreferenceAccessName + SWITCH_VALUE, switchDefaultValue);
    }

    public String loadPreferenceContentValue(Preference preference) {
        return loadPreferenceContentValue(preference.getAccessName(), preference.getDefaultValue());
    }

    protected String loadPreferenceContentValue(String PreferenceAccessName, String defaultValue) {
        return mSharedPreferences.getString(PreferenceAccessName + CONTENT_VALUE, defaultValue);
    }

    public String loadPreferenceContentValueRaw(Preference preference) {
        return loadPreferenceContentValueRaw(preference.getAccessName(), preference.getDefaultValue());
    }

    protected String loadPreferenceContentValueRaw(String PreferenceAccessName, String defaultValue) {
        return mSharedPreferences.getString(PreferenceAccessName + CONTENT_VALUE_RAW, defaultValue);
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
