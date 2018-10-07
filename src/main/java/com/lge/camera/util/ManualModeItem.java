package com.lge.camera.util;

public class ManualModeItem {
    public int[] mBarColors;
    public String mDefaultEntryValue;
    public String mDefaultValue;
    public String[] mEntries;
    public Integer[] mIcons;
    public String mKey;
    public String mPrefDefaultValue;
    public int mSeletecIndex;
    public String mSettingKey;
    public boolean[] mShowEntryValue;
    public String mTitle;
    public String[] mValues;

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public void setKey(String key) {
        this.mKey = key;
    }

    public String getKey() {
        return this.mKey;
    }

    public void setSettingKey(String settingKey) {
        this.mSettingKey = settingKey;
    }

    public String getSettingKey() {
        return this.mSettingKey;
    }

    public void setEntries(String[] entries) {
        this.mEntries = entries;
    }

    public String[] getEntries() {
        return this.mEntries;
    }

    public void setValues(String[] values) {
        this.mValues = values;
    }

    public String[] getValues() {
        return this.mValues;
    }

    public void setIcons(Integer[] icons) {
        this.mIcons = icons;
    }

    public Integer[] getIcons() {
        return this.mIcons;
    }

    public void setDefaultValue(String defaultValue) {
        this.mDefaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return this.mDefaultValue;
    }

    public void setDefaultEntryValue(String defaultEntryValue) {
        this.mDefaultEntryValue = defaultEntryValue;
    }

    public String getDefaultEntryValue() {
        return this.mDefaultEntryValue;
    }

    public void setSelectedIndex(int index) {
        this.mSeletecIndex = index;
    }

    public int getSelectedIndex() {
        return this.mSeletecIndex;
    }

    public void setPrefDefaultValue(String value) {
        this.mPrefDefaultValue = value;
    }

    public String getPrefDefaultValue() {
        return this.mPrefDefaultValue;
    }

    public int[] getBarColors() {
        return this.mBarColors;
    }

    public void setBarColors(int[] barColors) {
        this.mBarColors = barColors;
    }

    public boolean[] getShowEntryValue() {
        return this.mShowEntryValue;
    }

    public void setShowEntryValue(boolean[] showEntryValue) {
        this.mShowEntryValue = showEntryValue;
    }
}
