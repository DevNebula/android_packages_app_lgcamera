package com.lge.camera.managers;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public abstract class ManualControlManager extends ManagerInterfaceImpl {
    public static final String LOCK = "lock";
    protected static final String PARAM_VALUE_NOSET = "-10000";
    protected ManualControlCallback mCallBack = null;
    protected int mDisabledFeature = 0;
    ManualDataEV mEV = null;
    ManualDataISO mISO = null;
    protected boolean mIsAEUnLockSupported = true;
    protected boolean mIsInitValues = false;
    ManualDataMF mMF = null;
    protected ManualControlInterface mManualControlInterface = null;
    ArrayList<ManualData> mManualData = new ArrayList();
    protected HashMap<Integer, String> mParamMap = new HashMap();
    private int mShowingLogCount = 0;
    ManualDataSS mShutterSpeed = null;
    protected int mUncontrollableFeature = 0;
    ManualDataWB mWB = null;

    public interface ManualControlInterface {
        boolean IsDataChangedByGraphy();

        String convertISOParamValue(String str);

        String convertShutterSpeedParamValue(String str);

        String getISOParamKey();

        String getShutterSpeedParamKey();

        void setInitFocus(boolean z);

        void setManualFocus(boolean z);

        void setParameters(String str, String str2);

        void updateParameters(String str, String str2);
    }

    public interface ManualControlCallback {
        void notifyLockStatus();

        void refreshShutterSpeedData();

        void updateAllButtonValue();

        void updateButtonValue(int i);
    }

    public abstract boolean getAELock();

    public abstract int getLockedFeature();

    public abstract boolean isEnableAutoFuntion(int i);

    public abstract boolean setAELock(CameraParameters cameraParameters, Boolean bool);

    public abstract boolean setAELock(Boolean bool);

    public abstract void setGraphyData(String str, String str2, String str3);

    public abstract boolean setISO(String str, String str2, boolean z);

    public abstract void setLockedFeature();

    public abstract boolean setSS(String str, String str2, boolean z);

    public ManualControlManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    protected void makeData() {
        this.mManualData.clear();
        this.mISO = new ManualDataISO(this.mGet, this.mGet.getShotMode());
        this.mShutterSpeed = new ManualDataSS(this.mGet, this.mGet.getShotMode());
        this.mWB = new ManualDataWB(this.mGet, this.mGet.getShotMode());
        this.mMF = new ManualDataMF(this.mGet, this.mGet.getShotMode());
        this.mEV = new ManualDataEV(this.mGet, this.mGet.getShotMode());
        this.mManualData.add(this.mISO);
        this.mManualData.add(this.mShutterSpeed);
        this.mManualData.add(this.mWB);
        this.mManualData.add(this.mMF);
        this.mManualData.add(this.mEV);
        makeParamList();
        if (this.mGet.isRecordingPriorityMode()) {
            refreshShutterSpeedData(null, this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE));
        }
    }

    public void refreshShutterSpeedData(CameraParameters param, String fps) {
        this.mManualData.remove(this.mShutterSpeed);
        if (this.mShutterSpeed == null) {
            this.mShutterSpeed = new ManualDataSS(this.mGet, this.mGet.getShotMode());
        }
        if (param == null) {
            CameraProxy cameraDevice = this.mGet.getCameraDevice();
            if (cameraDevice != null) {
                param = cameraDevice.getParameters();
                if (param == null) {
                    return;
                }
            }
            return;
        }
        this.mShutterSpeed.loadData(param);
        this.mShutterSpeed.cutUnreachableShutterSpeed(fps);
        this.mManualData.add(this.mShutterSpeed);
        this.mCallBack.refreshShutterSpeedData();
    }

    public void setManualControlInterface(ManualControlInterface ManualControlInterface) {
        this.mManualControlInterface = ManualControlInterface;
    }

    protected void makeParamList() {
        this.mParamMap.put(Integer.valueOf(4), "lg-wb");
        this.mParamMap.put(Integer.valueOf(16), ParamConstants.MANUAL_FOCUS_STEP);
        this.mParamMap.put(Integer.valueOf(8), this.mManualControlInterface.getISOParamKey());
        this.mParamMap.put(Integer.valueOf(1), this.mManualControlInterface.getShutterSpeedParamKey());
    }

    public void setInitialParameters() {
        CamLog.m3d(CameraConstants.TAG, "setInitialParameters!");
        CamLog.m3d(CameraConstants.TAG, "mParamMap null? " + this.mParamMap);
        CamLog.m3d(CameraConstants.TAG, "mParamMap.keySet() = " + this.mParamMap.keySet().size());
        if (this.mIsInitValues) {
            if (this.mMF.isDefaultValue() || !this.mGet.isAFSupported()) {
                this.mManualControlInterface.setInitFocus(false);
            } else {
                this.mManualControlInterface.setInitFocus(true);
                this.mManualControlInterface.updateParameters("focus-mode", "normal");
                this.mManualControlInterface.setManualFocus(true);
                if (SharedPreferenceUtil.getFocusPeakingEnable(getAppContext()) && this.mGet.checkModuleValidate(128)) {
                    this.mManualControlInterface.updateParameters(ParamConstants.KEY_FOCUS_PEAKING, "on");
                }
            }
            if (!this.mEV.isDefaultValue()) {
                this.mManualControlInterface.updateParameters(ParamConstants.KEY_EXPOSURE_COMPENSATION, this.mEV.getValue());
            }
        }
    }

    protected void setUncontrollableParametersAuto(String paramKey, String settingKey, String defaultValue) {
        this.mGet.setSetting(settingKey, defaultValue, true);
        if (this.mManualControlInterface != null) {
            this.mManualControlInterface.updateParameters(paramKey, defaultValue);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mManualData.clear();
        this.mParamMap.clear();
        this.mIsInitValues = false;
        this.mISO = null;
        this.mShutterSpeed = null;
        this.mWB = null;
        this.mMF = null;
        this.mEV = null;
    }

    public void onCameraSwitchingStart() {
        resetParameters(31);
        super.onCameraSwitchingStart();
    }

    public void onPauseAfter() {
        if (this.mGet.isPaused() || this.mGet.isModuleChanging()) {
            resetParameters(getSupportedFeature());
        }
        super.onPauseAfter();
    }

    public synchronized void loadManualData(CameraParameters parameter) {
        if (!this.mIsInitValues) {
            this.mUncontrollableFeature = 0;
            this.mIsAEUnLockSupported = true;
            makeData();
            Iterator it = this.mManualData.iterator();
            while (it.hasNext()) {
                ((ManualData) it.next()).loadData(parameter);
            }
            this.mIsInitValues = true;
            if (getAELock()) {
                this.mUncontrollableFeature |= 2;
            }
        }
    }

    public ManualData getManualData(int type) {
        if (type == 8) {
            return this.mISO;
        }
        if (type == 1) {
            return this.mShutterSpeed;
        }
        if (type == 2) {
            return this.mEV;
        }
        if (type == 4) {
            return this.mWB;
        }
        if (type == 16) {
            return this.mMF;
        }
        return null;
    }

    public int getSupportedFeature() {
        return 31;
    }

    public int getControllableFeature() {
        return getEnabledFeature() & (this.mUncontrollableFeature ^ -1);
    }

    public int getEnabledFeature() {
        return getSupportedFeature() & (this.mDisabledFeature ^ -1);
    }

    public boolean isSupportedAEUnlock() {
        return this.mIsAEUnLockSupported;
    }

    public void setManualCallbackData(float currentWB, float currentEV, float currentISO, float curSS, float curMFStep) {
        if (this.mIsInitValues) {
            this.mWB.setValue(currentWB);
            this.mEV.setValue(currentEV);
            if (!(this.mManualControlInterface.IsDataChangedByGraphy() || getAELock())) {
                this.mISO.setValue((float) ManualUtil.convertISO(currentISO));
                this.mShutterSpeed.setValue(curSS);
            }
            this.mMF.setValue(curMFStep);
            this.mShowingLogCount++;
            if (this.mShowingLogCount == 30) {
                CamLog.m3d(CameraConstants.TAG, "Cur WB : " + currentWB + " EV : " + currentEV + " SS : " + curSS + " ISO : " + currentISO + " MF : " + curMFStep);
                this.mShowingLogCount = 0;
            }
            if (this.mCallBack != null) {
                this.mCallBack.updateAllButtonValue();
            }
        }
    }

    public boolean setWB(String key, String value, boolean save) {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (!this.mIsInitValues || cameraDevice == null || !this.mGet.checkModuleValidate(64) || cameraDevice.getParameters() == null) {
            return false;
        }
        if (!LOCK.equals(value)) {
            this.mWB.setValue(value);
        } else if ((getLockedFeature() & 4) != 0) {
            return false;
        } else {
            this.mWB.setValue(this.mWB.matchValue());
            this.mWB.setPivotColorValue();
            value = "-10000";
        }
        this.mManualControlInterface.setParameters(key, value);
        if (save) {
            this.mGet.setSetting(this.mWB.getSettingKey(), this.mWB.mValueString, true);
        }
        if (this.mCallBack != null) {
            this.mCallBack.updateButtonValue(4);
        }
        return true;
    }

    public boolean setMF(String key, String value, boolean save) {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (!this.mIsInitValues || cameraDevice == null || this.mManualControlInterface == null || !this.mGet.checkModuleValidate(64)) {
            return false;
        }
        CameraParameters parameter = cameraDevice.getParameters();
        if (parameter == null) {
            return false;
        }
        if (LOCK.equals(value)) {
            if ((getLockedFeature() & 16) != 0) {
                return false;
            }
            this.mMF.setValue(this.mMF.matchValue());
            this.mManualControlInterface.setManualFocus(true);
            this.mGet.setParamUpdater(parameter, "focus-mode", "normal");
            this.mGet.setParamUpdater(parameter, ParamConstants.MANUAL_FOCUS_STEP, this.mMF.mValueString);
            if (SharedPreferenceUtil.getFocusPeakingEnable(getAppContext()) && this.mGet.checkModuleValidate(192)) {
                this.mGet.setParamUpdater(parameter, ParamConstants.KEY_FOCUS_PEAKING, "on");
            }
            cameraDevice.setParameters(parameter);
        } else if (this.mMF.mDefaultValueString.equals(value)) {
            this.mMF.setValue(value);
            this.mGet.setParamUpdater(parameter, ParamConstants.MANUAL_FOCUS_STEP, value);
            this.mGet.setParamUpdater(parameter, ParamConstants.KEY_FOCUS_PEAKING, "off");
            parameter.setManualFocusStep(value);
            this.mManualControlInterface.setManualFocus(false);
        } else {
            this.mMF.setValue(value);
            this.mManualControlInterface.setParameters(key, value);
        }
        if (save) {
            this.mGet.setSetting(this.mMF.getSettingKey(), this.mMF.mValueString, true);
        }
        if (this.mCallBack != null) {
            this.mCallBack.updateButtonValue(16);
        }
        this.mGet.setTrackingFocusState(false);
        return true;
    }

    public boolean setEV(String key, String value, boolean save) {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (!this.mIsInitValues || cameraDevice == null || this.mManualControlInterface == null || !this.mGet.checkModuleValidate(64)) {
            return false;
        }
        CameraParameters parameter = cameraDevice.getParameters();
        if (parameter == null) {
            return false;
        }
        if (!LOCK.equals(value)) {
            this.mEV.setValue(value);
            int evValue = 0;
            try {
                evValue = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                CamLog.m3d(CameraConstants.TAG, "Number format exception occured, value : " + value);
            }
            parameter.setExposureCompensation(evValue);
            this.mGet.setParamUpdater(parameter, ParamConstants.KEY_EXPOSURE_COMPENSATION, value);
            cameraDevice.setParameters(parameter);
        } else if ((getLockedFeature() & 2) != 0) {
            return false;
        } else {
            this.mEV.setValue(this.mEV.matchValue());
            resetParameters(9);
        }
        if (save) {
            this.mGet.setSetting(this.mEV.getSettingKey(), this.mEV.mValueString, true);
        }
        if (this.mCallBack != null) {
            this.mCallBack.updateButtonValue(2);
        }
        return true;
    }

    public void resetParameters(int type) {
        CamLog.m3d(CameraConstants.TAG, "resetParameters");
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (this.mIsInitValues && cameraDevice != null) {
            CameraParameters parameter = cameraDevice.getParameters();
            if (parameter != null) {
                ListPreference listPref = (ListPreference) this.mGet.getListPreference("hdr-mode");
                if (listPref != null) {
                    this.mGet.setSetting("hdr-mode", listPref.loadSavedValue(), true);
                }
                this.mWB.setSetting();
                if ((type & 4) != 0) {
                    this.mGet.setParamUpdater(parameter, "lg-wb", this.mWB.getDefaultValue());
                }
                this.mMF.setSetting();
                if (!((type & 16) == 0 || this.mManualControlInterface == null)) {
                    this.mManualControlInterface.setManualFocus(false);
                }
                if ((type & 2) != 0) {
                    this.mEV.setSetting();
                    int evValue = 0;
                    try {
                        evValue = Integer.parseInt(this.mEV.getDefaultValue());
                    } catch (NumberFormatException e) {
                        CamLog.m3d(CameraConstants.TAG, "Number format exception occured, value : " + this.mEV.getDefaultValue());
                    }
                    parameter.setExposureCompensation(evValue);
                }
                cameraDevice.setParameters(parameter);
            }
        }
    }

    public void setUncontrollableFeatureByViewMode() {
        if (this.mCallBack != null) {
            this.mCallBack.notifyLockStatus();
        }
    }

    public void setDisabledFeature(int disabledFeatures) {
        this.mDisabledFeature = 0;
        this.mDisabledFeature |= disabledFeatures;
    }

    public void saveManualSettings() {
        for (Integer intValue : this.mParamMap.keySet()) {
            ManualData data = getManualData(intValue.intValue());
            if (data != null) {
                data.setSetting();
            }
        }
    }

    public String getInitialParameters() {
        return "";
    }

    public boolean isInitValues() {
        return this.mIsInitValues;
    }

    public void onInAndOutRecording() {
    }

    public void setCallback(ManualControlCallback callback) {
        this.mCallBack = callback;
    }
}
