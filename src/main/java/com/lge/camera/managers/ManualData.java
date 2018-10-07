package com.lge.camera.managers;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.settings.ListPreference;

public abstract class ManualData {
    protected float mCurrentValue = 0.0f;
    protected String mDefaultValueString = null;
    protected String mDisabledString = null;
    protected String[] mEntryArray = null;
    protected ModuleInterface mGet = null;
    protected boolean mIsFakeMode = false;
    protected String mShotMode = CameraConstants.MODE_MANUAL_CAMERA;
    protected String[] mValueArray = null;
    protected String mValueString = null;

    public abstract String getSettingKey();

    public ManualData(ModuleInterface manualModeInterface, String shotMode) {
        this.mGet = manualModeInterface;
        this.mShotMode = shotMode;
    }

    public void loadData(CameraParameters parameter) {
        ListPreference pref = (ListPreference) this.mGet.getListPreference(getSettingKey());
        if (pref != null) {
            this.mDefaultValueString = pref.getDefaultValue();
            this.mValueString = pref.getValue();
        }
        this.mDisabledString = "";
    }

    protected String matchValue() {
        return this.mValueString;
    }

    public String getUserInfoValue() {
        return this.mValueString;
    }

    public String getDisabledInfoValue() {
        return this.mDisabledString;
    }

    public String getAutoInfoValue() {
        return getUserInfoValue();
    }

    public String getValue() {
        return this.mValueString;
    }

    public String getDefaultValue() {
        return this.mDefaultValueString;
    }

    public String[] getEntryArray() {
        return this.mEntryArray;
    }

    public String[] getValueArray() {
        return this.mValueArray;
    }

    public String getSelectedValue() {
        if (this.mIsFakeMode || this.mEntryArray == null || this.mValueString == null || this.mEntryArray.length != this.mValueArray.length) {
            return null;
        }
        for (int i = 0; i < this.mEntryArray.length; i++) {
            if (this.mValueString.equals(this.mValueArray[i])) {
                return this.mEntryArray[i];
            }
        }
        return null;
    }

    public void setFakeMode(boolean isFake) {
        this.mIsFakeMode = isFake;
    }

    public boolean getFakeMode() {
        return this.mIsFakeMode;
    }

    public void setValue(String value) {
        this.mValueString = value;
    }

    public void setValue(float value) {
        this.mCurrentValue = value;
    }

    public void resetDefaultValue() {
        this.mValueString = this.mDefaultValueString;
    }

    public void restoretValue() {
        this.mValueString = this.mGet.getSettingValue(getSettingKey());
    }

    public void setSetting() {
        this.mGet.setSetting(getSettingKey(), this.mValueString, true);
    }

    public boolean isDefaultValue() {
        if (this.mValueString != null && this.mValueString.equals(this.mDefaultValueString)) {
            return true;
        }
        ListPreference pref = (ListPreference) this.mGet.getListPreference(getSettingKey());
        if (pref == null || !"not found".equals(pref.getValue())) {
            return false;
        }
        return true;
    }
}
