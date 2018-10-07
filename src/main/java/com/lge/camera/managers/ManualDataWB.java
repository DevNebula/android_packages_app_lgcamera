package com.lge.camera.managers;

import android.graphics.Color;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.file.ExifInterface.GpsSpeedRef;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.ellievision.parceldata.ISceneCategory;
import java.util.ArrayList;
import java.util.List;

public class ManualDataWB extends ManualData {
    int mPivotColorValue = 0;

    public ManualDataWB(ModuleInterface manualModeInterface, String shotMode) {
        super(manualModeInterface, shotMode);
    }

    public void loadData(CameraParameters parameter) {
        super.loadData(parameter);
        String supportedMin = parameter.get("lg-wb-supported-min");
        String supportedMax = parameter.get("lg-wb-supported-max");
        String supportedStep = parameter.get("lg-wb-supported-step");
        int min = supportedMin == null ? 2400 : Integer.parseInt(supportedMin);
        int max = supportedMax == null ? 7500 : Integer.parseInt(supportedMax);
        int step = supportedStep == null ? 100 : Integer.parseInt(supportedStep);
        List<String> wbEntryList = new ArrayList();
        List<String> wbValueList = new ArrayList();
        for (int i = min; i <= max; i += step) {
            wbEntryList.add(i + GpsSpeedRef.KILOMETERS);
            wbValueList.add(i + "");
        }
        this.mEntryArray = (String[]) wbEntryList.toArray(new String[wbEntryList.size()]);
        this.mValueArray = (String[]) wbValueList.toArray(new String[wbValueList.size()]);
    }

    public String getSettingKey() {
        return SettingKeyWrapper.getManualSettingKey(this.mShotMode, "lg-wb");
    }

    public String matchValue() {
        return Integer.toString((((int) this.mCurrentValue) / 100) * 100);
    }

    public void setValue(String value) {
        if (FunctionProperties.getSupportedHal() == 2) {
            this.mValueString = Integer.toString((Integer.parseInt(value) / 100) * 100);
        } else {
            super.setValue(value);
        }
    }

    public String getUserInfoValue() {
        String value = getSelectedValue();
        return value != null ? value : matchValue() + GpsSpeedRef.KILOMETERS;
    }

    public String getAutoInfoValue() {
        return ISceneCategory.CATEGORY_DISPLAY_AUTO;
    }

    public void makeColorValues(int[] barColorArray) {
        if (barColorArray != null && this.mValueArray != null && barColorArray.length == this.mValueArray.length) {
            int i;
            int min = Integer.parseInt(this.mValueArray[0]);
            int max = Integer.parseInt(this.mValueArray[this.mValueArray.length - 1]);
            int step = Integer.parseInt(this.mValueArray[1]) - min;
            int curPosition = (this.mPivotColorValue / 100) * 100;
            if (curPosition == 0) {
                curPosition = (((min + max) / 2) / 100) * 100;
            } else if (curPosition < min) {
                curPosition = min;
            } else if (curPosition > max) {
                curPosition = max;
            }
            int colorStep = 255 / (((max - min) / 100) / 2);
            int curIndex = (curPosition - min) / 100;
            int curColor = 255;
            for (i = curPosition; i >= min; i -= step) {
                barColorArray[curIndex] = Color.rgb(curColor, curColor, 255);
                curColor -= colorStep;
                if (curColor <= 0) {
                    curColor = 0;
                }
                curIndex--;
            }
            curIndex = (curPosition - min) / 100;
            curColor = 255;
            for (i = curPosition; i <= max; i += step) {
                barColorArray[curIndex] = Color.rgb(255, curColor, curColor);
                curColor -= colorStep;
                if (curColor <= 0) {
                    curColor = 0;
                }
                curIndex++;
            }
        }
    }

    public void setPivotColorValue() {
        setPivotColorValue((int) this.mCurrentValue);
    }

    public void setPivotColorValue(int value) {
        this.mPivotColorValue = value;
    }
}
