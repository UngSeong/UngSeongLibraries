package com.longseong.preference;

import static com.longseong.preference.PreferenceActivity.INTENT_KEY_ROOT_PREFERENCE_ID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;

import com.longseong.preference.Preference.Type;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class PreferenceListWrapper {

    private final AppCompatActivity mContext;
    private final PreferenceManager mPreferenceManager;

    private ActivityResultLauncher<Intent> mPreferenceLauncher;
    private final HashMap<Integer, Runnable> mPreferenceResultRunnableMap = new HashMap<>();

    public PreferenceListWrapper(AppCompatActivity activityContext) {
        mContext = activityContext;
        mPreferenceManager = PreferenceManager.getInstance(activityContext);
        setResultLauncher(activityContext);
    }

    public void onBindPreferenceList(LinearLayout linearLayout, LinkedList<Preference> preferenceList) {

        for (Preference preference : preferenceList) {

            PreferenceViewHolder holder;
            if (preference.getType().equals(Type.SEEK_BAR_TYPE)) {
                holder = new PreferenceSeekBarViewHolder(LayoutInflater.from(mContext).inflate(R.layout.preference_list_item_preference_seekbar, linearLayout, false), preference);
            } else if (preference.getType().equals(Type.INTENT_TYPE) || preference instanceof PreferenceGroup) {
                holder = new PreferenceIntentViewHolder(LayoutInflater.from(mContext).inflate(R.layout.preference_list_item_preference_intent, linearLayout, false), preference);
            } else {
                holder = new PreferenceViewHolder(LayoutInflater.from(mContext).inflate(R.layout.preference_list_item_preference, linearLayout, false), preference);
            }

            mPreferenceManager.addViewHolder(holder);
            linearLayout.addView(holder.itemView);


            //데이터 출력
            initDataView(holder, preference);

            //개별적 레이아웃 설정
            initIndividualLayout(preference, holder);

            //holder 클릭 이벤트
            holder.itemView.setOnClickListener(v -> {
                if (preference.getType() == Type.EXPLAIN_TYPE) {
                    //explain 타입의 설정값 클릭시 이벤트
                    explainTypedPreferenceClickEvent(holder, preference);
                } else if (preference.getType().equals(Type.TEXT_TYPE)) {
                    //text 타입의 설정값 클릭시 이벤트
                    textTypedPreferenceClickEvent(holder, preference);
                } else if (preference.getType().equals(Type.RADIO_TYPE)) {
                    //radio 타입의 설정값 클릭시 이벤트
                    radioTypedPreferenceClickEvent(holder, preference);
                } else if (preference.getType().equals(Type.INTENT_TYPE)) {
                    //intent 타입의 설정값 클릭시 이벤트
                    intentTypedPreferenceClickEvent(holder, preference);
                } else if (preference.getType().equals(Type.EVENT_TYPE)) {
                    //intent 타입의 설정값 클릭시 이벤트
                    eventTypedPreferenceClickEvent(holder, preference);
                }
            });

            //holder 롱클릭 이벤트
            holder.itemView.setOnLongClickListener(v -> {
                if (preference.getType() == Type.EXPLAIN_TYPE) {
                    //explain 타입의 설정값 클릭시 이벤트
                    return explainTypedPreferenceLongClickEvent(holder, preference);
                } else if (preference.getType().equals(Type.SEEK_BAR_TYPE)) {
                    //seekBar 타입의 설정값 클릭시 이벤트
                    return seekBarTypedPreferenceLongClickEvent(holder, preference);
                } else if (preference.getType().equals(Type.INTENT_TYPE)) {
                    //intent 타입의 설정값 클릭시 이벤트
                    return intentTypedPreferenceLongClickEvent(holder, preference);
                } else if (preference.getType().equals(Type.EVENT_TYPE)) {
                    //event 타입의 설정값 클릭시 이벤트
                    return eventTypedPreferenceLongClickEvent(holder, preference);
                }
                return false;
            });

            if (!preference.isEnabled()) {
                PreferenceActivity.setViewAndChildrenEnabled((ViewGroup) holder.itemView, false, false);
            }

            mPreferenceManager.getPreferenceViewCreatedListener().onCreated(mPreferenceManager, preference, mContext);

        }

    }

    private void initDataView(PreferenceViewHolder holder, Preference preference) {
        holder.setTitle(preference.getTitle());
        holder.setDescription(preference.getDescription());
        holder.setContentValue(preference.getContentValue(), preference);
    }

    private void initIndividualLayout(Preference preference, PreferenceViewHolder holder) {
        if (preference.getSwitch().isValid() && !(preference instanceof PreferenceGroup)) {
            initSwitchView(holder, preference);
        } else if (preference.getSeekBar().isValid()) {
            initSeekBarView((PreferenceSeekBarViewHolder) holder, preference);
        } else if (preference.getIntent().isValid() || preference instanceof PreferenceGroup) {
            initIntentView((PreferenceIntentViewHolder) holder, preference);
        }
    }

    private void initSwitchView(@NonNull PreferenceViewHolder holder, Preference preference) {

        boolean switchEnabled = preference.getSwitch().isValid();
        boolean dividerEnabled = switchEnabled && (preference.getSubPreferenceList().size() > 0 || preference.getRadio().isValid());

        holder.showSwitch(switchEnabled);
        holder.setSwitch(preference.getSwitch().isChecked());
        holder.mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preference.getSwitch().setChecked(isChecked);
            mPreferenceManager.savePreferenceSwitchValue(preference);
            //contentValue의 가시도 설정
            holder.setContentValue(preference.getContentValue(), preference);
        });

        holder.showDivider(dividerEnabled);
    }

    private void initSeekBarView(PreferenceSeekBarViewHolder holder, Preference preference) {

        Preference.SeekBar seekBarData = preference.getSeekBar();

        int progress = seekBarData.getProgressValue();
        int max = seekBarData.getMaxValue();
        int min = seekBarData.getMinValue();

        boolean muted = seekBarData.isMuted() || ((min == progress) && (progress == 0));
        boolean showIcon = seekBarData.isIconHolderUsing();
        boolean replaceIcon = seekBarData.isReplaceIcon();

        holder.mContentSeekBar.setMin(min);
        holder.mContentSeekBar.setMax(max);
        holder.mContentSeekBar.setProgress(progress);
        holder.setMute(muted);
        holder.showIcon(showIcon);
        if (showIcon) {
            holder.replaceIcon(replaceIcon);
            holder.setIcon(holder.itemView.getContext(), preference.getIconRes());
            holder.setContentValue(preference.getContentValueRaw() + "", preference);
            holder.setMuteUsing(seekBarData.isMutable());
            if (seekBarData.isMutable()) {
                holder.mContentValueHolder.setOnClickListener(v -> {
                    seekBarData.toggleMute();
                    holder.setMute(seekBarData.isMuted());

                    if (seekBarData.isMuted()) {
                        holder.mContentSeekBar.setProgress(seekBarData.getMinValue(), true);
                    } else {
                        if (seekBarData.getProgressRaw() == min) {
                            seekBarData.resetProgressToDefault();
                        }
                        holder.mContentSeekBar.setProgress(seekBarData.getProgressRaw(), true);
                    }

                    holder.setContentValue(seekBarData.getProgressRaw() + "", preference);
                    preference.setContentValue(seekBarData.getProgressValue() + "");
                    mPreferenceManager.savePreferenceContentValue(preference);
                });
            }
        }
        holder.mContentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                if (fromUser) {
                    holder.setContentValue(progress + "", preference);

                    holder.setMute(progress == 0);
                    seekBarData.setMute(progress == 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarData.setProgress(seekBar.getProgress());

                preference.setContentValue(seekBarData.getProgressValue() + "");
                mPreferenceManager.savePreferenceContentValue(preference);
                preference.setContentValueRaw(seekBarData.getProgressRaw() + "");
                mPreferenceManager.savePreferenceContentValueRaw(preference);
            }
        });
    }

    private void initIntentView(PreferenceIntentViewHolder holder, Preference preference) {
        holder.setIcon(mContext, preference.getIconRes());
        if (preference instanceof PreferenceGroup) {
            holder.setIconTint();
        }
    }

    private void explainTypedPreferenceClickEvent(PreferenceViewHolder holder, Preference preference) {
        if (preference.getSwitch().isValid() && preference.getSubPreferenceList().isEmpty()) {
            preference.getSwitch().toggle();
            holder.setSwitch(preference.getSwitch().isChecked());
            mPreferenceManager.savePreferenceSwitchValue(preference);
        } else if (preference.getSubPreferenceList().size() > 0) {
            startPreferenceActivity(preference);
        }
    }

    private void textTypedPreferenceClickEvent(@NonNull PreferenceViewHolder holder, Preference preference) {
        TextView dialogTitle, dialogDescription;
        EditText dialogInput;
        Button dialogCancel, dialogConfirm;

        View contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_item_text_type, null, false);

        if (holder.mTextDialog == null) {
            holder.mTextDialog = new AlertDialog.Builder(mContext).setView(contentView).create();
        } else {
            //다이얼로그가 이미 생성 되어 있다면 설정값 클릭 무시
            return;
        }
        holder.mTextDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialogTitle = contentView.findViewById(R.id.dialog_title);
        dialogDescription = contentView.findViewById(R.id.dialog_description);
        dialogInput = contentView.findViewById(R.id.dialog_input);
        dialogCancel = contentView.findViewById(R.id.dialog_button_cancel);
        dialogConfirm = contentView.findViewById(R.id.dialog_button_confirm);

        dialogTitle.setText(preference.getTitle());
        dialogDescription.setText(preference.getDetailedDescription());
        dialogInput.setText(preference.getContentValue());
        dialogInput.setInputType(preference.getInputType());
        dialogInput.requestFocus();

        dialogCancel.setOnClickListener(v1 -> holder.mTextDialog.dismiss());
        dialogConfirm.setOnClickListener(v1 -> {
            preference.setContentValue(dialogInput.getText().toString());
            mPreferenceManager.savePreferenceContentValue(preference);

            holder.setContentValue(preference.getContentValue(), preference);
            holder.mTextDialog.dismiss();
        });

        holder.mTextDialog.setOnDismissListener(dialog -> {
            //다이얼로그가 종료되면 인스턴스 제거
            holder.mTextDialog = null;
        });

        holder.mTextDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        holder.mTextDialog.show();
    }

    private void radioTypedPreferenceClickEvent(PreferenceViewHolder holder, Preference preference) {
        if (preference.getRadio().isValid()) {
            startPreferenceActivity(preference);
        }
    }

    private boolean seekBarTypedPreferenceLongClickEvent(PreferenceViewHolder holder, Preference preference) {
        Toast.makeText(mContext, preference.getDetailedDescription(), Toast.LENGTH_SHORT).show();
        return true;
    }

    private void intentTypedPreferenceClickEvent(PreferenceViewHolder holder, Preference preference) {
        if (preference.getIntent().isLaunchable()) {
            preference.getIntent().launch();
        } else {
            Toast.makeText(mContext, "인텐트 지정되지 않았습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void eventTypedPreferenceClickEvent(PreferenceViewHolder holder, Preference preference) {
        if (preference.getEvent().isValid()) {
            preference.getEvent().startEvent(preference);
        }
    }

    private boolean explainTypedPreferenceLongClickEvent(PreferenceViewHolder holder, Preference preference) {
        if (!preference.getSubPreferenceList().isEmpty()) {
            return false;
        }
        Toast.makeText(mContext, preference.getDetailedDescription(), Toast.LENGTH_SHORT).show();
        return true;
    }

    private boolean intentTypedPreferenceLongClickEvent(PreferenceViewHolder holder, Preference preference) {
        Toast.makeText(mContext, preference.getDetailedDescription(), Toast.LENGTH_SHORT).show();
        return true;
    }

    private boolean eventTypedPreferenceLongClickEvent(PreferenceViewHolder holder, Preference preference) {
        Toast.makeText(mContext, preference.getDetailedDescription(), Toast.LENGTH_SHORT).show();
        return true;
    }

    private void startPreferenceActivity(Preference preference) {
        Intent preferenceIntent = new Intent(mContext, PreferenceActivity.class);
        preferenceIntent.putExtra(INTENT_KEY_ROOT_PREFERENCE_ID, preference.getId());

        if (mContext != null) {
            launchSubPreference(preferenceIntent, preference.getId());
        }
    }

    public void onBindRadioGroup(Preference preference, LinearLayout linearLayout, PreferenceRadioGroup radioGroup, @NonNull PreferenceRadioGroup.RadioCheckedChangedListener radioCheckedChangedListener) {
        String savedValue = preference.getContentValue();

        for (Map.Entry<String, Preference.Radio.RadioInfo> entry : preference.getRadio().getRadioMap().entrySet()) {
            RadioViewHolder holder = new RadioViewHolder(LayoutInflater.from(mContext).inflate(R.layout.preference_list_item_radio, linearLayout, false));

            Preference.Radio.RadioInfo radioInfo = entry.getValue();

            holder.setTitle(radioInfo.title);
            holder.setDescription(radioInfo.description);

            linearLayout.addView(holder.itemView);

            if (radioInfo.title.equals(savedValue)) {
                holder.mRadioButton.setChecked(true);
                radioGroup.setIndexOnly(linearLayout.indexOfChild(holder.itemView));
            }

            holder.itemView.setOnClickListener(v -> {
                boolean changed = radioGroup.check(linearLayout.indexOfChild(v));
                if (changed) {
                    mPreferenceManager.savePreferenceContentValue(preference);
                    radioCheckedChangedListener.radioCheckedChanged(radioGroup, preference, linearLayout.indexOfChild(v));
                }
            });
        }
    }

    private void setResultLauncher(AppCompatActivity activity) {
        mPreferenceLauncher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        int preferenceId = result.getData().getIntExtra(INTENT_KEY_ROOT_PREFERENCE_ID, ResourcesCompat.ID_NULL);
                        Runnable runnable = mPreferenceResultRunnableMap.get(preferenceId);
                        if (runnable != null) {
                            runnable.run();
                        }
                    }
                });
    }

    private void launchSubPreference(Intent intent, int preferenceId) {
        mPreferenceLauncher.launch(intent);
        mPreferenceResultRunnableMap.put(preferenceId, () -> mPreferenceManager.getViewHolder(preferenceId).update());
    }

    public static class PreferenceViewHolder extends ViewHolder {

        protected final TextView mContentValue;
        protected final SwitchCompat mSwitch;
        protected final View mDivider;

        private AlertDialog mTextDialog;

        public PreferenceViewHolder(@NonNull View itemView, Preference preference) {
            super(itemView, preference);

            mContentValue = itemView.findViewById(R.id.list_item_content_value);
            mSwitch = itemView.findViewById(R.id.preference_switch);
            mDivider = itemView.findViewById(R.id.divider);
        }

        @Override
        public void update() {
            super.update();

            setContentValue(mPreference.getContentValue(), mPreference);
            setSwitch(mPreference.getSwitch().isChecked());
        }

        public void setContentValue(@NonNull String contentValue, Preference preference) {
            mContentValue.setText(contentValue);
            if ((preference.getSwitch().isValid() && !preference.getSwitch().isChecked()) || contentValue.equals("")) {
                mContentValue.setVisibility(View.GONE);
            } else {
                mContentValue.setVisibility(View.VISIBLE);
            }
        }

        public void setSwitch(boolean checked) {
            mSwitch.setChecked(checked);
        }

        public void showSwitch(boolean visible) {
            if (visible) {
                mSwitch.setVisibility(View.VISIBLE);
            } else {
                mSwitch.setVisibility(View.GONE);
            }
        }

        public void showDivider(boolean visible) {
            if (visible) {
                mDivider.setVisibility(View.VISIBLE);
            } else {
                mDivider.setVisibility(View.GONE);
            }
        }

        public void update(Preference preference) {
            setTitle(preference.getTitle());
            setContentValue(preference.getContentValue(), preference);
            setDescription(preference.getDescription());
            setSwitch(preference.getSwitch().isChecked());
        }
    }

    public static class RadioViewHolder extends ViewHolder {

        private final TextView mContentValue;
        private final RadioButton mRadioButton;

        public RadioViewHolder(View itemView) {
            super(itemView);

            mContentValue = itemView.findViewById(R.id.list_item_content_value);
            mRadioButton = itemView.findViewById(R.id.radio);
        }

        public void setContentValue(@NonNull String contentValue, Preference preference) {
            mContentValue.setText(contentValue);
            if ((preference.getSwitch().isValid() && !preference.getSwitch().isChecked()) || contentValue.equals("")) {
                mContentValue.setVisibility(View.GONE);
            } else {
                mContentValue.setVisibility(View.VISIBLE);
            }
        }
    }

    public static class PreferenceSeekBarViewHolder extends PreferenceViewHolder {

        private FrameLayout mContentValueHolder;
        private ImageView mContentValueIcon;
        private SeekBar mContentSeekBar;

        public PreferenceSeekBarViewHolder(@NonNull View itemView, Preference preference) {
            super(itemView, preference);

            mContentSeekBar = itemView.findViewById(R.id.list_item_content_seekbar);
            mContentValueHolder = itemView.findViewById(R.id.list_item_content_value_holder);
            mContentValueIcon = itemView.findViewById(R.id.list_item_content_value_image);

            //xml에서 작동하지 않기 때문에 직접 수정함
            mContentValueIcon.setImageTintList(ResourcesCompat.getColorStateList(itemView.getContext().getResources(), R.color.preference_text_content_value, itemView.getContext().getTheme()));
        }

        @Override
        public void setContentValue(@NonNull String contentValue, Preference preference) {
            if (preference.getSeekBar().isIconHolderUsing()) {
                if (preference.getSeekBar().isReplaceIcon()) {
                    mContentValue.setText(contentValue);
                }
            }
        }

        public void setMuteUsing(boolean muteUsing) {
            if (!muteUsing) {
                mContentValueHolder.setClickable(false);
                mContentValueHolder.setFocusable(false);
                mContentValueHolder.setBackground(null);
            }
        }

        public void setMute(boolean mute) {
            PreferenceActivity.setViewAndChildrenEnabled(mContentValueHolder, !mute, true);
        }

        public void showIcon(boolean visible) {
            if (visible) {
                mContentValueHolder.setVisibility(View.VISIBLE);
            } else {
                mContentValueHolder.setVisibility(View.GONE);
            }
        }

        public void replaceIcon(boolean replace) {
            if (replace) {
                mContentValueIcon.setVisibility(View.GONE);
                mContentValue.setVisibility(View.VISIBLE);
            } else {
                mContentValueIcon.setVisibility(View.VISIBLE);
                mContentValue.setVisibility(View.GONE);
            }
        }

        public void setIcon(Context context, @DrawableRes int iconRes) {
            if (iconRes == ResourcesCompat.ID_NULL) {
                if (mContentValue.getVisibility() == View.GONE) {
                    showIcon(false);
                }
                return;
            }
            Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), iconRes, null);
            mContentValueIcon.setImageDrawable(drawable);
            showIcon(true);
            if (mContentValue.getVisibility() == View.VISIBLE) {
                replaceIcon(false);
            }
        }

        @Override
        public void setSwitch(boolean visible) {
        }

        @Override
        public void showSwitch(boolean visible) {
        }

        @Override
        public void showDivider(boolean visible) {
        }
    }

    public static class PreferenceIntentViewHolder extends PreferenceViewHolder {

        private ImageView mIconView;
        private FrameLayout mIconLayout;

        public PreferenceIntentViewHolder(@NonNull View itemView, Preference preference) {
            super(itemView, preference);

            mIconLayout = itemView.findViewById(R.id.preference_icon_layout);
            mIconView = itemView.findViewById(R.id.preference_icon);
        }

        public void showIcon(boolean visible) {
            if (visible) {
                mIconLayout.setVisibility(View.VISIBLE);
            } else {
                mIconLayout.setVisibility(View.GONE);
            }
        }

        public void setIcon(Context context, @DrawableRes int iconRes) {
            if (iconRes == ResourcesCompat.ID_NULL) {
                showIcon(false);
                return;
            }
            Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), iconRes, null);
            mIconView.setImageDrawable(drawable);
            showIcon(true);
        }

        public void setIconTint() {
            TypedValue typedValue = new TypedValue();

            TypedArray a = itemView.getContext().obtainStyledAttributes(typedValue.data, new int[]{androidx.appcompat.R.attr.colorPrimary});
            int color = a.getColor(0, 0);

            a.recycle();
            mIconView.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

        @Override
        public void setSwitch(boolean checked) {
        }

        @Override
        public void showSwitch(boolean visible) {
        }

        @Override
        public void showDivider(boolean visible) {
        }

    }

}
