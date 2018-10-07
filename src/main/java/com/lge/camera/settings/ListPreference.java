package com.lge.camera.settings;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.List;

public class ListPreference extends CameraPreference {
    private String mDefaultValue;
    private CharSequence[] mDesc;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private CharSequence[] mExtraInfos;
    private CharSequence[] mExtraInfos2;
    private CharSequence[] mExtraInfos3;
    private int[] mExtraInfos4Int;
    private int[] mExtraInfos5Int;
    private boolean mIsBulletDividerSetting = false;
    private boolean mKeepLastValue = false;
    private String mKey;
    private boolean mLoaded = false;
    private String mMenuCommand;
    private int[] mMenuIconResources;
    private boolean mPersist;
    private boolean mSaveSettingEnabled = true;
    private String mSettingDesc;
    private int[] mSettingMenuIconResources;
    private String mTitle;
    private String mValue;

    public ListPreference(Context context, String prefName) {
        super(context, prefName);
    }

    public ListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, C0088R.styleable.ListPreference, 0, 0);
        this.mKey = (String) Utils.checkNotNull(styledAttrs.getString(1));
        this.mTitle = styledAttrs.getString(0);
        this.mDefaultValue = styledAttrs.getString(5);
        setEntries(styledAttrs.getTextArray(6));
        setEntryValues(styledAttrs.getTextArray(7));
        setMenuIconResources(context, styledAttrs);
        setSettingMenuIconResources(context, styledAttrs);
        this.mMenuCommand = styledAttrs.getString(4);
        setExtraInfos(styledAttrs.getTextArray(9), 1);
        setExtraInfos(styledAttrs.getTextArray(11), 2);
        setExtraInfos(styledAttrs.getTextArray(12), 3);
        setExtraInfoIntegerResources(context, styledAttrs, 4);
        setExtraInfoIntegerResources(context, styledAttrs, 5);
        this.mPersist = styledAttrs.getBoolean(10, true);
        styledAttrs.recycle();
    }

    public String getKey() {
        return this.mKey;
    }

    public void setKey(String key) {
        this.mKey = key;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setPersist(boolean persist) {
        this.mPersist = persist;
    }

    public String getDefaultValue() {
        return this.mDefaultValue;
    }

    public void setDefaultValue(String defValue) {
        if (defValue == null) {
            defValue = "";
        }
        this.mDefaultValue = defValue;
    }

    public CharSequence[] getEntries() {
        return this.mEntries;
    }

    public CharSequence[] getEntryValues() {
        return this.mEntryValues;
    }

    public int[] getMenuIconResources() {
        return this.mMenuIconResources;
    }

    public void setMenuIconResources(int[] values) {
        if (values == null) {
            values = new int[0];
        }
        this.mMenuIconResources = values;
    }

    public int[] getSettingMenuIconResources() {
        return this.mSettingMenuIconResources;
    }

    public void setSettingMenuIconResources(int[] values) {
        if (values == null) {
            values = new int[0];
        }
        this.mSettingMenuIconResources = values;
    }

    private void setSettingMenuIconResources(Context context, TypedArray styledAttrs) {
        int settingIconResId = styledAttrs.getResourceId(3, 0);
        if (settingIconResId != 0) {
            TypedArray ta = context.getResources().obtainTypedArray(settingIconResId);
            int arrayLength = ta.length();
            this.mSettingMenuIconResources = new int[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                this.mSettingMenuIconResources[i] = ta.getResourceId(i, 0);
            }
            ta.recycle();
        }
    }

    private void setMenuIconResources(Context context, TypedArray styledAttrs) {
        int selectedIconsResId = styledAttrs.getResourceId(2, 0);
        if (selectedIconsResId != 0) {
            TypedArray ta = context.getResources().obtainTypedArray(selectedIconsResId);
            int arrayLength = ta.length();
            this.mMenuIconResources = new int[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                this.mMenuIconResources[i] = ta.getResourceId(i, 0);
            }
            ta.recycle();
        }
    }

    private void setExtraInfoIntegerResources(Context context, TypedArray styledAttrs, int numInfo) {
        int selectedIconsResId = styledAttrs.getResourceId(13, 0);
        if (selectedIconsResId != 0) {
            TypedArray ta = context.getResources().obtainTypedArray(selectedIconsResId);
            int arrayLength = ta.length();
            int[] extraInfosInt = new int[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                extraInfosInt[i] = ta.getResourceId(i, 0);
            }
            ta.recycle();
            switch (numInfo) {
                case 4:
                    this.mExtraInfos4Int = extraInfosInt;
                    return;
                case 5:
                    this.mExtraInfos5Int = extraInfosInt;
                    return;
                default:
                    return;
            }
        }
    }

    public void setEntries(CharSequence[] entries) {
        if (entries == null) {
            entries = new CharSequence[0];
        }
        this.mEntries = entries;
    }

    public void setEntryValues(CharSequence[] values) {
        if (values == null) {
            values = new CharSequence[0];
        }
        this.mEntryValues = values;
    }

    public void setDescription(String[] desc) {
        if (desc == null) {
            desc = new CharSequence[0];
        }
        this.mDesc = desc;
    }

    public String getDesc() {
        int index = findIndexOfValue(getValue());
        if (index < 0 || this.mDesc == null || this.mDesc.length == 0) {
            return "";
        }
        return this.mDesc[index].toString();
    }

    public CharSequence[] getDescList() {
        return this.mDesc;
    }

    public String getExtraInfo(int numInfo) {
        return getExtraInfo(numInfo, findIndexOfValue(getValue()));
    }

    public String getExtraInfo(int numInfo, int index) {
        CharSequence[] extraInfo = getExtraInfoByNum(numInfo);
        if (index < 0) {
            return "";
        }
        if (extraInfo == null || extraInfo.length == 0) {
            return "";
        }
        return extraInfo[index].toString();
    }

    public CharSequence[] getExtraInfoByNum(int num) {
        switch (num) {
            case 1:
                return this.mExtraInfos;
            case 2:
                return this.mExtraInfos2;
            case 3:
                return this.mExtraInfos3;
            default:
                return null;
        }
    }

    public int[] getIntExtraInfo(int num) {
        switch (num) {
            case 4:
                return this.mExtraInfos4Int;
            case 5:
                return this.mExtraInfos5Int;
            default:
                return null;
        }
    }

    public void setExtraInfos(CharSequence[] extraInfos, int numInfo) {
        CharSequence[] extraInfo;
        if (extraInfos == null) {
            extraInfo = new CharSequence[0];
        } else {
            extraInfo = extraInfos;
        }
        switch (numInfo) {
            case 1:
                this.mExtraInfos = extraInfo;
                return;
            case 2:
                this.mExtraInfos2 = extraInfo;
                return;
            case 3:
                this.mExtraInfos3 = extraInfo;
                return;
            default:
                return;
        }
    }

    public void setExtraInfos(int[] values, int num) {
        int[] extraInfosInt;
        if (values == null) {
            extraInfosInt = new int[0];
        } else {
            extraInfosInt = values;
        }
        switch (num) {
            case 4:
                this.mExtraInfos4Int = extraInfosInt;
                return;
            case 5:
                this.mExtraInfos5Int = extraInfosInt;
                return;
            default:
                return;
        }
    }

    public String getCommand() {
        return this.mMenuCommand;
    }

    public void setCommand(String menuCommand) {
        this.mMenuCommand = menuCommand;
    }

    public void setSaveSettingEnabled(boolean state) {
        this.mSaveSettingEnabled = state;
    }

    public String getValue() {
        if (!this.mLoaded) {
            if (this.mPersist || this.mKeepLastValue) {
                this.mValue = getSharedPreferences().getString(this.mKey, this.mDefaultValue);
                if (!(this.mEntryValues == null || this.mEntryValues.length == 0 || findIndexOfValue(this.mValue) != -1)) {
                    this.mValue = this.mDefaultValue;
                    if (this.mSaveSettingEnabled) {
                        persistStringValue(this.mValue);
                    }
                }
                this.mKeepLastValue = false;
            } else {
                this.mValue = this.mDefaultValue;
                setValue(this.mValue);
            }
            this.mLoaded = true;
        }
        return this.mValue;
    }

    public String loadSavedValue() {
        return getSharedPreferences().getString(this.mKey, this.mDefaultValue);
    }

    public void setValue(String value) {
        if (value != null) {
            if (findIndexOfValue(value) >= 0 || this.mEntryValues.length <= 0) {
                this.mValue = value;
                if (this.mSaveSettingEnabled) {
                    persistStringValue(value);
                    return;
                }
                return;
            }
            CamLog.m11w(CameraConstants.TAG, "setValue failed, mKey : " + this.mKey + ", value : " + value);
            int n = this.mEntryValues.length;
            for (int i = 0; i < n; i++) {
                CamLog.m11w(CameraConstants.TAG, "mEntryValues[" + i + "] [" + this.mEntryValues[i] + "]");
            }
            throw new IllegalArgumentException();
        }
    }

    public void setValueIndex(int index) {
        if (index >= 0 && index < this.mEntryValues.length) {
            setValue(this.mEntryValues[index].toString());
        }
    }

    public String findValueOfIndex(int index) {
        if (index < 0 || index >= this.mEntryValues.length) {
            return "not found";
        }
        return this.mEntryValues[index].toString();
    }

    public int findIndexOfValue(String value) {
        int n = this.mEntryValues.length;
        for (int i = 0; i < n; i++) {
            if (Utils.equals(this.mEntryValues[i], value)) {
                return i;
            }
        }
        return -1;
    }

    public String getEntry() {
        int index = findIndexOfValue(getValue());
        if (index < 0) {
            return "";
        }
        return this.mEntries[index].toString();
    }

    public void persistStringValue(String value) {
        Editor editor = getSharedPreferences().edit();
        editor.putString(this.mKey, value);
        editor.apply();
    }

    public void reloadValue() {
        this.mLoaded = false;
    }

    public void filterUnsupported(List<String> supported) {
        int i;
        ArrayList<CharSequence> entries = new ArrayList();
        ArrayList<CharSequence> entryValues = new ArrayList();
        CamLog.m3d(CameraConstants.TAG, "Preference ---------------------------------------------------------");
        for (i = 0; i < supported.size(); i++) {
            CamLog.m3d(CameraConstants.TAG, "Preference Device support item [" + String.format("%02d", new Object[]{Integer.valueOf(i)}) + "]\t\t: [" + ((String) supported.get(i)) + "]");
        }
        CamLog.m3d(CameraConstants.TAG, "Preference ---------------------------------------------------------");
        int len = this.mEntryValues.length;
        for (i = 0; i < len; i++) {
            CamLog.m3d(CameraConstants.TAG, "Preference XML Defined values/entries\t: [" + this.mEntryValues[i].toString() + "] / [" + this.mEntries[i].toString() + "]");
            if (supported.indexOf(this.mEntryValues[i].toString()) >= 0) {
                entries.add(this.mEntries[i]);
                entryValues.add(this.mEntryValues[i]);
            }
        }
        int size = entries.size();
        CamLog.m3d(CameraConstants.TAG, "Preference supported entries count [" + size + "]");
        CamLog.m3d(CameraConstants.TAG, "Preference ---------------------------------------------------------");
        this.mEntries = (CharSequence[]) entries.toArray(new CharSequence[size]);
        this.mEntryValues = (CharSequence[]) entryValues.toArray(new CharSequence[size]);
    }

    public void keepLastValue() {
        this.mKeepLastValue = true;
    }

    public void setSettingDecription(String settingDescription) {
        this.mSettingDesc = settingDescription;
    }

    public String getSettingDescription() {
        return this.mSettingDesc;
    }

    public void setIsBulletDividerSetting(boolean isBullet) {
        this.mIsBulletDividerSetting = isBullet;
    }

    public boolean isBulletDividerSetting() {
        return this.mIsBulletDividerSetting;
    }
}
