package com.lge.camera.managers;

import com.lge.camera.device.CameraParameters;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.SettingKeyWrapper;
import java.util.ArrayList;

public class ManualDataMF extends ManualData {
    public ManualDataMF(ModuleInterface manualModeInterface, String shotMode) {
        super(manualModeInterface, shotMode);
    }

    public void loadData(CameraParameters parameter) {
        super.loadData(parameter);
        ArrayList<String> manualFocusValueList = new ArrayList();
        ArrayList<String> manualFocusEntryList = new ArrayList();
        boolean[] showEntryValue = new boolean[61];
        for (int i = 0; i < 61; i++) {
            String item = i + "";
            manualFocusValueList.add(item);
            if (i % 3 == 0) {
                manualFocusEntryList.add(item);
                showEntryValue[i] = true;
            } else {
                item = "";
                manualFocusEntryList.add("");
                showEntryValue[i] = false;
            }
        }
        this.mEntryArray = (String[]) manualFocusEntryList.toArray(new String[manualFocusEntryList.size()]);
        this.mValueArray = (String[]) manualFocusValueList.toArray(new String[manualFocusValueList.size()]);
        this.mDisabledString = "MF";
    }

    public String getSettingKey() {
        return SettingKeyWrapper.getManualSettingKey(this.mShotMode, Setting.KEY_MANUAL_FOCUS_STEP);
    }

    public String matchValue() {
        return ((int) this.mCurrentValue) + "";
    }

    public String getUserInfoValue() {
        if (isDefaultValue()) {
            return getAutoInfoValue();
        }
        return "MF";
    }

    public String getAutoInfoValue() {
        return "AF";
    }
}
