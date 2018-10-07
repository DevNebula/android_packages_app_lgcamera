package com.lge.camera.managers;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.MathUtil;

public class ManualDefaultControlManager extends ManualControlManager {
    public ManualDefaultControlManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public synchronized void loadManualData(CameraParameters parameter) {
        super.loadManualData(parameter);
        if (getAELock()) {
            this.mEV.setUserInfoType(true);
            this.mShutterSpeed.setUserInfoType(true);
        }
    }

    public int getSupportedFeature() {
        return 31;
    }

    public void setLockedFeature() {
    }

    public int getLockedFeature() {
        int lockedTypes = 0;
        if (!this.mIsInitValues) {
            return 0;
        }
        if (!this.mISO.isDefaultValue()) {
            lockedTypes = 0 | 8;
        }
        if (!this.mShutterSpeed.isDefaultValue()) {
            lockedTypes |= 1;
        }
        return lockedTypes;
    }

    public boolean setISO(String key, String value, boolean save) {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (!this.mIsInitValues || cameraDevice == null || !this.mGet.checkModuleValidate(64)) {
            return false;
        }
        CameraParameters parameter = cameraDevice.getParameters();
        if (parameter == null) {
            return false;
        }
        int updateType = 8;
        if (ManualControlManager.LOCK.equals(value)) {
            setManualAELockParameters(parameter);
            if (this.mCallBack != null) {
                this.mCallBack.updateButtonValue(8);
            }
            return true;
        }
        this.mISO.setValue(value);
        this.mGet.setParamUpdater(parameter, this.mManualControlInterface.getISOParamKey(), this.mManualControlInterface.convertISOParamValue(value));
        if (this.mShutterSpeed.mIsFakeMode) {
            this.mGet.setParamUpdater(parameter, this.mManualControlInterface.getShutterSpeedParamKey(), this.mManualControlInterface.convertShutterSpeedParamValue(this.mShutterSpeed.mValueString));
            updateType = 8 | 1;
        }
        this.mISO.setFakeMode(false);
        this.mShutterSpeed.setFakeMode(false);
        cameraDevice.setParameters(parameter);
        if (this.mCallBack != null) {
            this.mCallBack.updateButtonValue(updateType);
        }
        return true;
    }

    public boolean setSS(String key, String value, boolean save) {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (!this.mIsInitValues || cameraDevice == null || !this.mGet.checkModuleValidate(64)) {
            return false;
        }
        CameraParameters parameter = cameraDevice.getParameters();
        if (parameter == null) {
            return false;
        }
        int updateType = 1;
        if (ManualControlManager.LOCK.equals(value)) {
            setManualAELockParameters(parameter);
            if (this.mCallBack != null) {
                this.mCallBack.updateButtonValue(1);
            }
            return true;
        }
        if (this.mShutterSpeed.mIsFakeMode) {
            value = this.mShutterSpeed.getValue();
        }
        this.mShutterSpeed.setValue(value);
        this.mGet.setParamUpdater(parameter, this.mManualControlInterface.getShutterSpeedParamKey(), this.mManualControlInterface.convertShutterSpeedParamValue(this.mShutterSpeed.getValue()));
        if (this.mISO.mIsFakeMode) {
            this.mGet.setParamUpdater(parameter, this.mManualControlInterface.getISOParamKey(), this.mManualControlInterface.convertISOParamValue(this.mISO.getValue()));
            updateType = 1 | 8;
        }
        this.mISO.setFakeMode(false);
        this.mShutterSpeed.setFakeMode(false);
        cameraDevice.setParameters(parameter);
        if (this.mCallBack != null) {
            this.mCallBack.notifyLockStatus();
            this.mCallBack.updateButtonValue(updateType);
        }
        return true;
    }

    private boolean setManualAELockParameters(CameraParameters parameter) {
        if ((getLockedFeature() & 1) != 0 || (getLockedFeature() & 8) != 0) {
            return false;
        }
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (!this.mIsInitValues || cameraDevice == null) {
            return false;
        }
        if (parameter == null) {
            parameter = cameraDevice.getParameters();
            if (parameter == null) {
                return false;
            }
        }
        this.mUncontrollableFeature |= 2;
        if (FunctionProperties.getSupportedHal() == 2) {
            this.mISO.setValue(this.mISO.getUserInfoValue());
            this.mShutterSpeed.setValue(this.mShutterSpeed.getUserInfoValue());
        } else {
            this.mISO.setValue(this.mISO.matchValue());
            this.mShutterSpeed.setValue(this.mShutterSpeed.matchValue());
        }
        this.mEV.setUserInfoType(true);
        this.mShutterSpeed.setUserInfoType(true);
        this.mEV.setSetting();
        this.mISO.setSetting();
        this.mShutterSpeed.setSetting();
        int valueInt = 0;
        try {
            valueInt = Integer.parseInt(this.mEV.getValue());
        } catch (NumberFormatException e) {
            CamLog.m3d(CameraConstants.TAG, "NumberFormatException occured, mEV.getValue() = " + this.mEV.getValue());
        }
        parameter.setExposureCompensation(valueInt);
        this.mShutterSpeed.setFakeMode(true);
        this.mISO.setFakeMode(true);
        if (FunctionProperties.getSupportedHal() == 2) {
            this.mGet.setParamUpdater(parameter, this.mManualControlInterface.getISOParamKey(), this.mISO.mValueString);
            this.mGet.setParamUpdater(parameter, this.mManualControlInterface.getShutterSpeedParamKey(), this.mManualControlInterface.convertShutterSpeedParamValue(this.mShutterSpeed.mValueString));
        } else {
            this.mGet.setParamUpdater(parameter, this.mManualControlInterface.getISOParamKey(), ParamConstants.PARAM_VALUE_NOSET);
            this.mGet.setParamUpdater(parameter, this.mManualControlInterface.getShutterSpeedParamKey(), ParamConstants.PARAM_VALUE_NOSET);
        }
        parameter.set(ParamConstants.KEY_MANUAL_MODE_RESET, "0");
        cameraDevice.setParameters(parameter);
        if (this.mCallBack != null) {
            this.mCallBack.notifyLockStatus();
        }
        return true;
    }

    private boolean setManualAEUnlockParameters(CameraParameters parameter, boolean setParam) {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (!this.mIsInitValues || cameraDevice == null) {
            return false;
        }
        if (parameter == null) {
            parameter = cameraDevice.getParameters();
            if (parameter == null) {
                return false;
            }
        }
        this.mUncontrollableFeature &= this.mUncontrollableFeature & -3;
        this.mEV.resetDefaultValue();
        this.mGet.setSetting(this.mEV.getSettingKey(), this.mEV.mValueString, true);
        int valueInt = 0;
        try {
            valueInt = Integer.parseInt(this.mEV.mValueString);
        } catch (NumberFormatException e) {
            CamLog.m3d(CameraConstants.TAG, "NumberFormatException occured, mEV.mValueString = " + this.mEV.mValueString);
        }
        parameter.setExposureCompensation(valueInt);
        this.mISO.setFakeMode(false);
        this.mShutterSpeed.setFakeMode(false);
        this.mISO.resetDefaultValue();
        this.mShutterSpeed.resetDefaultValue();
        this.mEV.setUserInfoType(false);
        this.mShutterSpeed.setUserInfoType(false);
        this.mGet.setSetting(this.mISO.getSettingKey(), this.mISO.mValueString, true);
        this.mGet.setSetting(this.mShutterSpeed.getSettingKey(), this.mShutterSpeed.mValueString, true);
        this.mGet.setParamUpdater(parameter, this.mManualControlInterface.getISOParamKey(), this.mManualControlInterface.convertISOParamValue(this.mISO.mValueString));
        this.mGet.setParamUpdater(parameter, this.mManualControlInterface.getShutterSpeedParamKey(), this.mManualControlInterface.convertShutterSpeedParamValue(this.mShutterSpeed.mValueString));
        parameter.set(ParamConstants.KEY_MANUAL_MODE_RESET, 1);
        if (setParam) {
            cameraDevice.setParameters(parameter);
        }
        return true;
    }

    public boolean setAELock(Boolean isLock) {
        return setAELock(null, isLock);
    }

    public boolean setAELock(CameraParameters parameters, Boolean isLock) {
        if (!this.mIsInitValues) {
            return false;
        }
        if (parameters == null) {
            CameraProxy cameraDevice = this.mGet.getCameraDevice();
            if (cameraDevice == null) {
                return false;
            }
            parameters = cameraDevice.getParameters();
            if (parameters == null) {
                return false;
            }
        }
        if (isLock.booleanValue()) {
            return setManualAELockParameters(parameters);
        }
        if (!setManualAEUnlockParameters(parameters, true)) {
            return false;
        }
        if (this.mCallBack != null) {
            this.mCallBack.notifyLockStatus();
            this.mCallBack.updateButtonValue(11);
        }
        return true;
    }

    public boolean getAELock() {
        if (!this.mIsInitValues) {
            return false;
        }
        if (!this.mISO.isDefaultValue()) {
            return true;
        }
        if (this.mShutterSpeed.isDefaultValue()) {
            return false;
        }
        return true;
    }

    public void setInitialParameters() {
        super.setInitialParameters();
        if (this.mIsInitValues && this.mManualControlInterface != null) {
            for (Integer intValue : this.mParamMap.keySet()) {
                int key = intValue.intValue();
                ManualData data = getManualData(key);
                if (data != null) {
                    String paramKey = (String) this.mParamMap.get(Integer.valueOf(key));
                    CamLog.m3d(CameraConstants.TAG, data.getSettingKey() + " currentSettingValue = " + data.getValue());
                    if (data.isDefaultValue()) {
                        this.mManualControlInterface.updateParameters(paramKey, data.getDefaultValue());
                    } else {
                        this.mManualControlInterface.updateParameters(paramKey, data.getValue());
                    }
                }
            }
        }
    }

    public String getInitialParameters() {
        if (!this.mIsInitValues || this.mManualControlInterface == null) {
            return "";
        }
        String returnValue = "";
        for (Integer intValue : this.mParamMap.keySet()) {
            int key = intValue.intValue();
            ManualData data = getManualData(key);
            if (data != null) {
                returnValue = returnValue + ((String) this.mParamMap.get(Integer.valueOf(key))) + "=" + data.getValue() + ";";
            }
        }
        return returnValue;
    }

    public void onInAndOutRecording() {
        this.mIsInitValues = false;
        saveManualSettings();
    }

    public void resetParameters(int type) {
        CamLog.m3d(CameraConstants.TAG, "resetParameters");
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (this.mIsInitValues && cameraDevice != null) {
            CameraParameters parameter = cameraDevice.getParameters();
            if (parameter != null) {
                if ((type & 8) != 0) {
                    this.mISO.setSetting();
                    this.mGet.setParamUpdater(parameter, this.mManualControlInterface.getISOParamKey(), this.mManualControlInterface.convertISOParamValue(this.mISO.getDefaultValue()));
                }
                if ((type & 1) != 0) {
                    this.mShutterSpeed.setSetting();
                    this.mGet.setParamUpdater(parameter, this.mManualControlInterface.getShutterSpeedParamKey(), this.mShutterSpeed.getDefaultValue());
                }
                super.resetParameters(type);
            }
        }
    }

    public boolean isEnableAutoFuntion(int type) {
        if ((type & getControllableFeature()) != type || type == 0 || type == 2) {
            return false;
        }
        return true;
    }

    public void setGraphyData(String iso, String shutterSpeed, String wb) {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null) {
            CameraParameters parameter = cameraDevice.getParameters();
            if (parameter != null) {
                if (!(this.mISO == null || iso == null)) {
                    this.mISO.setValue(iso);
                    if (!"auto".equals(iso.trim())) {
                        this.mISO.setValue(Float.valueOf(iso).floatValue());
                    }
                    String matchValue = this.mISO.matchValue();
                    CamLog.m3d(CameraConstants.TAG, "[Graphy] iso : " + matchValue);
                    this.mISO.setValue(matchValue);
                    this.mGet.setParamUpdater(parameter, this.mManualControlInterface.getISOParamKey(), this.mManualControlInterface.convertISOParamValue(matchValue));
                }
                if (!(this.mShutterSpeed == null || shutterSpeed == null)) {
                    this.mShutterSpeed.setValue(shutterSpeed);
                    this.mShutterSpeed.setValue(MathUtil.parseStringToFloat(shutterSpeed));
                    CamLog.m3d(CameraConstants.TAG, "[Graphy] shutterSpeed : " + shutterSpeed);
                    this.mGet.setParamUpdater(parameter, this.mManualControlInterface.getShutterSpeedParamKey(), this.mManualControlInterface.convertShutterSpeedParamValue(shutterSpeed));
                }
                if (!(this.mWB == null || wb == null)) {
                    this.mWB.setValue(wb);
                    this.mWB.setValue(Float.valueOf(wb).floatValue());
                    CamLog.m3d(CameraConstants.TAG, "[Graphy] wb : " + wb);
                    this.mGet.setParamUpdater(parameter, "lg-wb", wb);
                }
                cameraDevice.setParameters(parameter);
                if (this.mCallBack != null) {
                    this.mCallBack.updateAllButtonValue();
                }
            }
        }
    }
}
