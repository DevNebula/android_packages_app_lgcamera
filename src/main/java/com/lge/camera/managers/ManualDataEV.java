package com.lge.camera.managers;

import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.SettingKeyWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManualDataEV extends ManualData {
    public float mExposureCompensationStep = 0.167f;
    private boolean mIsCameraDeviceValue = false;
    private float mMaxStep = 0.2f;
    private float mMinStep = -0.2f;

    public ManualDataEV(ModuleInterface manualModeInterface, String shotMode) {
        super(manualModeInterface, shotMode);
    }

    public void loadData(CameraParameters parameter) {
        super.loadData(parameter);
        int minCompen = parameter.getMinExposureCompensation();
        int maxCompen = parameter.getMaxExposureCompensation();
        this.mExposureCompensationStep = Float.parseFloat(parameter.get(ParamConstants.KEY_EXPOSURE_COMPENSATION_STEP));
        List<String> evList = new ArrayList();
        List<String> evValueList = new ArrayList();
        for (int i = minCompen; i <= maxCompen; i++) {
            float label = ((float) i) * this.mExposureCompensationStep;
            String index = "";
            String abs = "";
            index = String.format(Locale.US, "%.1f", new Object[]{Float.valueOf(label)});
            abs = label > 0.0f ? "+" : "";
            evList.add(String.format("%s%s", new Object[]{abs, index}));
            evValueList.add(String.valueOf(i));
            if (i == minCompen) {
                this.mMinStep = label;
            } else if (i == maxCompen) {
                this.mMaxStep = label;
            }
        }
        this.mEntryArray = (String[]) evList.toArray(new String[evList.size()]);
        this.mValueArray = (String[]) evValueList.toArray(new String[evValueList.size()]);
        this.mDisabledString = "0.0";
    }

    public String getSettingKey() {
        return SettingKeyWrapper.getManualSettingKey(this.mShotMode, Setting.KEY_LG_EV_CTRL);
    }

    public String matchValue() {
        return Integer.toString(Math.round(this.mCurrentValue));
    }

    public String getAutoInfoValue() {
        return getUserInfoValue();
    }

    public String getUserInfoValue() {
        int valueInt = 0;
        try {
            valueInt = Integer.parseInt(this.mIsCameraDeviceValue ? matchValue() : this.mValueString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        float label = ((float) valueInt) * this.mExposureCompensationStep;
        if (this.mIsCameraDeviceValue && FunctionProperties.getSupportedHal() == 2) {
            label = this.mCurrentValue;
            if (label > this.mMaxStep) {
                label = this.mMaxStep;
            } else if (label < this.mMinStep) {
                label = this.mMinStep;
            }
        }
        String index = "";
        String abs = "";
        index = String.format(Locale.US, "%.1f", new Object[]{Float.valueOf(label)});
        abs = label > 0.0f ? "+" : "";
        return String.format(String.format("%s%s", new Object[]{abs, index}), new Object[0]);
    }

    public void setUserInfoType(boolean isShowCameraDeviceValue) {
        this.mIsCameraDeviceValue = isShowCameraDeviceValue;
    }
}
