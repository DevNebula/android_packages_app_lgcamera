package com.lge.camera.managers;

import android.util.Range;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.MathUtil;
import com.lge.camera.util.SettingKeyWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ManualDataSS extends ManualData {
    public static final float TIMEUNIT_MILLISECOND = 1000.0f;
    private boolean mIsAELock = false;
    public float[] mSSKeyArray = null;

    /* renamed from: com.lge.camera.managers.ManualDataSS$1 */
    class C10931 implements Comparator<String> {
        C10931() {
        }

        public int compare(String lhs, String rhs) {
            String[] parseValues = lhs.split("/");
            if (parseValues == null) {
                return 0;
            }
            float leftVal;
            if (parseValues.length == 1) {
                leftVal = Float.parseFloat(parseValues[0]);
            } else if (Float.compare(Float.parseFloat(parseValues[1]), 0.0f) == 0) {
                leftVal = 0.0f;
            } else {
                leftVal = Float.parseFloat(parseValues[0]) / Float.parseFloat(parseValues[1]);
            }
            parseValues = rhs.split("/");
            if (parseValues == null) {
                return 0;
            }
            float rightVal;
            if (parseValues.length == 1) {
                rightVal = Float.parseFloat(parseValues[0]);
            } else if (Float.compare(Float.parseFloat(parseValues[1]), 0.0f) == 0) {
                rightVal = 0.0f;
            } else {
                rightVal = Float.parseFloat(parseValues[0]) / Float.parseFloat(parseValues[1]);
            }
            if (leftVal > rightVal) {
                return -1;
            }
            if (leftVal < rightVal) {
                return 1;
            }
            return 0;
        }
    }

    public ManualDataSS(ModuleInterface manualModeInterface, String shotMode) {
        super(manualModeInterface, shotMode);
    }

    public void loadData(CameraParameters parameter) {
        String[] ss;
        int i;
        super.loadData(parameter);
        String ssValues = null;
        if (parameter != null) {
            ssValues = parameter.get(ParamConstants.KEY_SHUTTER_SPEED_SUPPOPRTED_VALUES);
        }
        Range<Long> rangeNano = null;
        if (FunctionProperties.getSupportedHal() == 2 && ssValues != null) {
            ss = ssValues.split(",");
            rangeNano = new Range(Long.valueOf(ss[0]), Long.valueOf(ss[1]));
            ssValues = null;
        }
        if (ssValues == null) {
            ssValues = "1/4000,1/3200,1/2500,1/2000,1/1600,1/1250,1/1000,1/800,1/640,1/500,1/400,1/320,1/250,1/200,1/160,1/125,1/100,1/80,1/60,1/50,1/40,1/30,1/25,1/20,1/15,1/13,1/10,1/8,1/6,1/5,1/4,3/10,4/10,5/10,6/10,8/10,1,13/10,16/10,2,25/10,32/10,4,5,6,8,10,13,15,20,25,30";
        }
        CamLog.m3d(CameraConstants.TAG, "shutter spped supported list : " + ssValues);
        ss = ssValues.split(",");
        List<String> ssValueList = new ArrayList();
        for (i = 0; i < ss.length; i++) {
            if (!"0".equals(ss[i])) {
                ssValueList.add(ss[i]);
            }
        }
        collectionSortForShutterSpeed(ssValueList);
        if (FunctionProperties.getSupportedHal() == 2 && rangeNano != null) {
            clampForShutterSpeed(ssValueList, rangeNano);
        }
        this.mValueArray = (String[]) ssValueList.toArray(new String[ssValueList.size()]);
        this.mSSKeyArray = new float[this.mValueArray.length];
        for (i = 0; i < this.mValueArray.length; i++) {
            float parseFloat;
            String[] parseValues = this.mValueArray[i].split("/");
            float[] fArr = this.mSSKeyArray;
            if (parseValues.length == 1) {
                parseFloat = Float.parseFloat(parseValues[0]);
            } else {
                parseFloat = Float.parseFloat(parseValues[0]) / Float.parseFloat(parseValues[1]);
            }
            fArr[i] = parseFloat;
            if (FunctionProperties.isSupportedManualZSL()) {
                float[] fArr2 = this.mSSKeyArray;
                fArr2[i] = fArr2[i] * 1000.0f;
            }
        }
        this.mEntryArray = convertSSValueToEntry(ssValueList);
    }

    public String getSettingKey() {
        return SettingKeyWrapper.getManualSettingKey(this.mShotMode, "shutter-speed");
    }

    public void cutUnreachableShutterSpeed(String frameRate) {
        float frameRateFloat = 0.0f;
        try {
            frameRateFloat = Float.parseFloat(frameRate);
        } catch (NumberFormatException e) {
            CamLog.m3d(CameraConstants.TAG, "NunberFormatOccured!");
        }
        if (Float.compare(0.0f, frameRateFloat) != 0) {
            String shutterSpeedLimit = matchValue(1.0f / frameRateFloat);
            if (shutterSpeedLimit != null) {
                int i;
                List<String> ssValueList = new ArrayList();
                for (i = 0; i < this.mValueArray.length; i++) {
                    if (!"0".equals(this.mValueArray[i])) {
                        ssValueList.add(this.mValueArray[i]);
                    }
                }
                int idx = ssValueList.indexOf(shutterSpeedLimit);
                if (idx > 0 && ssValueList.size() > idx) {
                    for (i = idx - 1; i >= 0; i--) {
                        ssValueList.remove(i);
                    }
                    this.mValueArray = (String[]) ssValueList.toArray(new String[ssValueList.size()]);
                    this.mSSKeyArray = new float[this.mValueArray.length];
                    for (i = 0; i < this.mValueArray.length; i++) {
                        float parseFloat;
                        String[] parseValues = this.mValueArray[i].split("/");
                        float[] fArr = this.mSSKeyArray;
                        if (parseValues.length == 1) {
                            parseFloat = Float.parseFloat(parseValues[0]);
                        } else {
                            parseFloat = Float.parseFloat(parseValues[0]) / Float.parseFloat(parseValues[1]);
                        }
                        fArr[i] = parseFloat;
                        if (FunctionProperties.isSupportedManualZSL()) {
                            float[] fArr2 = this.mSSKeyArray;
                            fArr2[i] = fArr2[i] * 1000.0f;
                        }
                    }
                    this.mEntryArray = convertSSValueToEntry(ssValueList);
                }
            }
        }
    }

    String[] convertSSValueToExposureTime(List<String> entryValueList) {
        if (entryValueList == null || entryValueList.size() == 0) {
            return null;
        }
        String[] entryList = new String[entryValueList.size()];
        for (int i = 0; i < entryValueList.size(); i++) {
            String[] parsedValue = ((String) entryValueList.get(i)).split("/");
            if (!(parsedValue == null || parsedValue.length == 0)) {
                if (parsedValue.length < 2) {
                    entryList[i] = parsedValue[0];
                } else {
                    int numerator = Integer.parseInt(parsedValue[0]);
                    int denominator = Integer.parseInt(parsedValue[1]);
                    if (denominator == 0) {
                        entryList[i] = (String) entryValueList.get(i);
                    } else {
                        entryList[i] = String.valueOf(((float) numerator) / ((float) denominator));
                    }
                }
                entryList[i] = (Float.valueOf(entryList[i]).floatValue() * 1000.0f) + "";
            }
        }
        return entryList;
    }

    String[] convertSSValueToEntry(List<String> entryValueList) {
        String[] entryList = new String[entryValueList.size()];
        for (int i = 0; i < entryValueList.size(); i++) {
            String[] parsedValue = ((String) entryValueList.get(i)).split("/");
            if (!(parsedValue == null || parsedValue.length == 0)) {
                if (parsedValue.length < 2) {
                    entryList[i] = parsedValue[0];
                } else if ("1".equals(parsedValue[0])) {
                    entryList[i] = (String) entryValueList.get(i);
                } else {
                    int numerator = Integer.parseInt(parsedValue[0]);
                    int denominator = Integer.parseInt(parsedValue[1]);
                    if (denominator == 0) {
                        entryList[i] = (String) entryValueList.get(i);
                    } else {
                        float result = ((float) numerator) / ((float) denominator);
                        entryList[i] = String.format(Locale.US, "%.1f", new Object[]{Float.valueOf(result)});
                    }
                }
            }
        }
        return entryList;
    }

    public void collectionSortForShutterSpeed(List<String> ssValueList) {
        Collections.sort(ssValueList, new C10931());
    }

    private void clampForShutterSpeed(List<String> list, Range<Long> range) {
        List<String> result = new ArrayList();
        for (String string : list) {
            Long tmp = Long.valueOf(ManualUtil.convertShutterSpeedMilliToNano(string));
            if (Long.compare(tmp.longValue(), ((Long) range.getLower()).longValue()) >= 0 && Long.compare(tmp.longValue(), ((Long) range.getUpper()).longValue()) <= 0) {
                result.add(string);
            }
        }
        list = result;
    }

    public String matchValue() {
        return matchValue(this.mCurrentValue);
    }

    public String matchValue(float shutterSpeedToMatch) {
        if (this.mSSKeyArray == null || this.mValueArray == null) {
            CamLog.m3d(CameraConstants.TAG, "shutter speed table is not set");
            return null;
        }
        int i;
        int arrayLen = this.mSSKeyArray.length;
        if (FunctionProperties.isSupportedManualZSL()) {
            shutterSpeedToMatch = (float) (((double) Math.round(100.0f * (shutterSpeedToMatch * 1000.0f))) / 100.0d);
        }
        for (i = 0; i < arrayLen; i++) {
            if (Float.compare(shutterSpeedToMatch, this.mSSKeyArray[i]) == 0) {
                return this.mValueArray[i];
            }
        }
        i = 0;
        while (i < arrayLen) {
            if (Float.compare(shutterSpeedToMatch, this.mSSKeyArray[i]) > 0) {
                if (i == 0) {
                    return this.mValueArray[i];
                }
                if (Math.abs(this.mSSKeyArray[i] - shutterSpeedToMatch) <= Math.abs(this.mSSKeyArray[i - 1] - shutterSpeedToMatch)) {
                    return this.mValueArray[i];
                }
                return this.mValueArray[i - 1];
            } else if (i == arrayLen - 1) {
                return this.mValueArray[i];
            } else {
                i++;
            }
        }
        return this.mEntryArray[this.mEntryArray.length / 2];
    }

    public String getUserInfoValue() {
        String value = getSelectedValue();
        if (value != null) {
            return value;
        }
        float curSS = this.mCurrentValue;
        if (this.mIsAELock) {
            curSS = MathUtil.parseStringToFloat(this.mValueString);
        }
        if (Float.compare(curSS, 0.0f) == 0) {
            return this.mEntryArray[this.mEntryArray.length / 2];
        }
        if (curSS < 1.0f) {
            return String.format(Locale.US, "%d/%d", new Object[]{Integer.valueOf(1), Integer.valueOf((int) (0.5f + (1.0f / curSS)))});
        }
        int integer = (int) curSS;
        curSS -= (float) integer;
        return String.valueOf(integer);
    }

    public String getDisabledInfoValue() {
        return getUserInfoValue();
    }

    public String convertShutterSpeedToExposureTime(String value) {
        String convertValue = value;
        if (!FunctionProperties.isSupportedManualZSL() || "0".equals(value)) {
            return convertValue;
        }
        return Float.toString(MathUtil.parseStringToFloat(value) * 1000.0f);
    }

    public void setUserInfoType(boolean isAELock) {
        this.mIsAELock = isAELock;
    }
}
