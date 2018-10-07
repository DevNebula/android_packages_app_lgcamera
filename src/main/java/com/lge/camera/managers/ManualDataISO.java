package com.lge.camera.managers;

import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.ellievision.parceldata.ISceneCategory;
import java.util.ArrayList;

public class ManualDataISO extends ManualData {
    public Integer[] mValueIntegerArray = null;

    public ManualDataISO(ModuleInterface manualModeInterface, String shotMode) {
        super(manualModeInterface, shotMode);
    }

    public void loadData(CameraParameters parameter) {
        int i;
        super.loadData(parameter);
        String isoValues = parameter.get(ParamConstants.KEY_MANUAL_ISO_VALUES);
        if (isoValues == null) {
            isoValues = "auto,50,100,200,400,800,1200,1600,2400,3200";
        }
        String[] isoItem = isoValues.split(",");
        ArrayList<String> isoValueList = new ArrayList();
        for (String item : isoItem) {
            if (!(item == null || "auto".equalsIgnoreCase(item) || "hjr".equalsIgnoreCase(item))) {
                isoValueList.add(item);
            }
        }
        this.mValueArray = (String[]) isoValueList.toArray(new String[isoValueList.size()]);
        this.mEntryArray = (String[]) isoValueList.toArray(new String[isoValueList.size()]);
        this.mValueIntegerArray = new Integer[this.mValueArray.length];
        for (i = 0; i < this.mValueArray.length; i++) {
            this.mValueIntegerArray[i] = Integer.valueOf(Integer.parseInt(this.mValueArray[i]));
        }
    }

    public String getSettingKey() {
        return SettingKeyWrapper.getManualSettingKey(this.mShotMode, Setting.KEY_LG_MANUAL_ISO);
    }

    public String matchValue() {
        if (this.mValueIntegerArray == null) {
            return null;
        }
        int maxIndex = this.mValueIntegerArray.length - 1;
        if (this.mCurrentValue < ((float) this.mValueIntegerArray[0].intValue())) {
            return this.mValueArray[0];
        }
        if (this.mCurrentValue > ((float) this.mValueIntegerArray[maxIndex].intValue())) {
            return this.mValueArray[maxIndex];
        }
        int i = 0;
        while (i < this.mValueIntegerArray.length && this.mCurrentValue >= ((float) this.mValueIntegerArray[i].intValue())) {
            i++;
        }
        if (i > maxIndex) {
            return this.mValueArray[maxIndex];
        }
        int index;
        if (Math.abs(this.mCurrentValue - ((float) this.mValueIntegerArray[i - 1].intValue())) < Math.abs(this.mCurrentValue - ((float) this.mValueIntegerArray[i].intValue()))) {
            index = i - 1;
        } else {
            index = i;
        }
        return this.mValueArray[index];
    }

    public String getUserInfoValue() {
        String value = getSelectedValue();
        return value != null ? value : ((int) this.mCurrentValue) + "";
    }

    public String getAutoInfoValue() {
        return ISceneCategory.CATEGORY_DISPLAY_AUTO;
    }

    public String getValue() {
        if (this.mIsFakeMode) {
            matchValue();
        }
        return this.mValueString;
    }

    public String getSelectedValue() {
        if (this.mIsFakeMode || this.mEntryArray == null || this.mValueString == null) {
            return null;
        }
        if (FunctionProperties.getSupportedHal() == 2) {
            if (this.mValueString.equals("auto")) {
                return null;
            }
            return this.mValueString;
        } else if (this.mEntryArray.length != this.mValueArray.length) {
            return null;
        } else {
            for (int i = 0; i < this.mEntryArray.length; i++) {
                if (this.mValueString.equals(this.mValueArray[i])) {
                    return this.mEntryArray[i];
                }
            }
            return null;
        }
    }
}
