package com.lge.camera.app.ext;

import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.RggbChannelVector;
import android.support.p000v4.app.NotificationManagerCompat;
import android.widget.SeekBar;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.app.DefaultCameraModule;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraManagerFactory;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ICameraCallback.CameraImageMetaCallback;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;

public class FoodModuleBase extends DefaultCameraModule {
    final int MEDIUM_LEVEL = 10;
    int mCurrentLevel = -1;

    abstract class FoodWBValue {
        static final int KEY_FAST_SETTING = 4;
        static final int KEY_MAX_VALUE = 2;
        static final int KEY_MIN_VALUE = 1;
        static final int KEY_ON_CALLBACK = 3;
        static final int VALUE_NONE = Integer.MIN_VALUE;
        int mCallbackCount = 0;

        public abstract void changeRequseter();

        public abstract String getLdbStringExtra();

        public abstract String getParamKey();

        public abstract int getThreshold();

        public abstract boolean isFixedInitValue();

        public abstract boolean isFixedLastValue();

        public abstract void onProgressChanged(SeekBar seekBar, int i);

        public abstract void setMetaCallback(boolean z);

        FoodWBValue() {
        }

        public void setValue(int key) {
            setValue(key, Integer.MIN_VALUE);
        }

        public void setValue(int key, int value) {
        }

        public void onPauseAfter() {
            setMetaCallback(false);
        }
    }

    class API1FoodWBValue extends FoodWBValue {
        private final int DEFAULT_MEDIUM_VALUE = 5000;
        private final int WB_MAX_1_SIDE_RANGE = 1000;
        private int mCurrentValue = -1;
        private int mMaxValue = 7500;
        private int mMediumValue = -1;
        private int mMinValue = 2400;
        private int mStepToMax = -1;
        private int mStepToMin = -1;

        API1FoodWBValue() {
            super();
        }

        public void setValue(int key, int value) {
            switch (key) {
                case 1:
                case 2:
                    setMinMaxValue();
                    return;
                case 3:
                    onMetaCallback(value);
                    return;
                default:
                    return;
            }
        }

        private void onMetaCallback(int currentWB) {
            CamLog.m3d(CameraConstants.TAG, "currentWB = " + currentWB);
            if (currentWB != 0) {
                if (this.mCallbackCount < getThreshold()) {
                    this.mCallbackCount++;
                    return;
                }
                this.mMediumValue = currentWB;
                this.mMinValue = Math.max(this.mMinValue, this.mMediumValue + NotificationManagerCompat.IMPORTANCE_UNSPECIFIED);
                this.mMaxValue = Math.min(this.mMaxValue, this.mMediumValue + 1000);
                this.mStepToMax = (this.mMaxValue - this.mMediumValue) / 10;
                this.mStepToMin = (this.mMediumValue - this.mMinValue) / 10;
                CamLog.m3d(CameraConstants.TAG, "medium value = " + this.mMediumValue + ", min value = " + this.mMinValue + ", max value = " + this.mMaxValue);
                setMetaCallback(false);
            }
        }

        private void setMinMaxValue() {
            CameraParameters parameters = FoodModuleBase.this.mCameraDevice.getParameters();
            if (parameters == null) {
                CamLog.m5e(CameraConstants.TAG, "getParameters Fail, set supported min/max with default value");
                return;
            }
            String supportedMin = parameters.get("lg-wb-supported-min");
            String supportedMax = parameters.get("lg-wb-supported-max");
            this.mMinValue = supportedMin == null ? 2400 : Integer.parseInt(supportedMin);
            this.mMaxValue = supportedMax == null ? 7500 : Integer.parseInt(supportedMax);
            CamLog.m3d(CameraConstants.TAG, "supported LG WB min Value : " + this.mMinValue + ", max Value = " + this.mMaxValue);
        }

        public int getThreshold() {
            return 25;
        }

        public String getParamKey() {
            return "lg-wb";
        }

        public void changeRequseter() {
            if (FoodModuleBase.this.mParamUpdater == null) {
                CamLog.m5e(CameraConstants.TAG, "mParamUpdater is null");
                return;
            }
            setMetaCallback(true);
            FoodModuleBase.this.mParamUpdater.addRequester(getParamKey(), Math.max(0, this.mCurrentValue) + "", false, true);
            FoodModuleBase.this.mParamUpdater.addRequester("mode_food", "on", false, false);
        }

        public void setMetaCallback(boolean set) {
            if (FoodModuleBase.this.mCameraDevice == null) {
                return;
            }
            if (this.mMediumValue == -1 || !set) {
                CamLog.m3d(CameraConstants.TAG, "setFoodMetaDataCallback : " + set);
                FoodModuleBase.this.mCameraDevice.setManualCameraMetadataCb(set ? FoodModuleBase.this.mFoodMetaDataCallback : null);
            }
        }

        public void onProgressChanged(SeekBar bar, int level) {
            if (this.mMediumValue == -1) {
                CamLog.m3d(CameraConstants.TAG, "mMediumValue not set, set default value 5000");
                this.mCallbackCount = getThreshold();
                onMetaCallback(5000);
            }
            if (FoodModuleBase.this.mCameraDevice != null) {
                CameraParameters parameters = FoodModuleBase.this.mCameraDevice.getParameters();
                if (parameters == null) {
                    CamLog.m3d(CameraConstants.TAG, "parameters is null");
                    return;
                }
                int value;
                if (level < 10) {
                    value = this.mMinValue + (this.mStepToMin * level);
                } else {
                    value = this.mMediumValue + ((level - 10) * this.mStepToMax);
                }
                FoodModuleBase.this.setParamUpdater(parameters, getParamKey(), Integer.toString(value));
                FoodModuleBase.this.mCameraDevice.setParameters(parameters);
                CamLog.m3d(CameraConstants.TAG, "set wb level = " + level + ", value = " + Integer.toString(value));
                CameraManagerFactory.getAndroidCameraManager(FoodModuleBase.this.mGet.getActivity()).setParamToBackup("lg-wb", Integer.valueOf(value));
                this.mCurrentValue = value;
            }
        }

        public String getLdbStringExtra() {
            return "food_wb=" + this.mCurrentValue + ";";
        }

        public boolean isFixedInitValue() {
            return this.mMediumValue != -1;
        }

        public boolean isFixedLastValue() {
            return this.mCurrentValue != -1;
        }

        public void onPauseAfter() {
            super.onPauseAfter();
            if ((FoodModuleBase.this.mGet.isPaused() || FoodModuleBase.this.mGet.isModuleChanging()) && FoodModuleBase.this.mCameraDevice != null) {
                CameraParameters parameter = FoodModuleBase.this.mCameraDevice.getParameters();
                if (parameter != null) {
                    FoodModuleBase.this.setParamUpdater(parameter, "lg-wb", "0");
                    FoodModuleBase.this.setParamUpdater(parameter, "mode_food", "off");
                    FoodModuleBase.this.mCameraDevice.setParameters(parameter);
                }
            }
        }
    }

    class API2FoodWBValue extends FoodWBValue {
        private final float GAINS_FACTOR_BLUE = 0.03f;
        private final float GAINS_FACTOR_RED = 0.015f;
        private CameraImageMetaCallback mFoodMetaCallback = new C03841();
        private RggbChannelVector mInitGains;
        private RggbChannelVector mLastGains;
        private boolean needSetParamOnMetaCallback = false;

        /* renamed from: com.lge.camera.app.ext.FoodModuleBase$API2FoodWBValue$1 */
        class C03841 implements CameraImageMetaCallback {
            C03841() {
            }

            public void onImageMetaData(TotalCaptureResult result) {
                if (API2FoodWBValue.this.mLastGains != null) {
                    CamLog.m3d(CameraConstants.TAG, "mInitGains = " + API2FoodWBValue.this.mInitGains.toString());
                    API2FoodWBValue.this.setMetaCallback(false);
                    return;
                }
                RggbChannelVector gains = (RggbChannelVector) result.get(CaptureResult.COLOR_CORRECTION_GAINS);
                if (gains == null) {
                    CamLog.m5e(CameraConstants.TAG, "gains null");
                } else if (API2FoodWBValue.this.needSetParamOnMetaCallback && FoodModuleBase.this.mCameraDevice != null) {
                    API2FoodWBValue.this.updateLastGains(gains);
                    CameraParameters params = FoodModuleBase.this.mCameraDevice.getParameters();
                    params.setColorCorrectionGains(API2FoodWBValue.this.mLastGains);
                    FoodModuleBase.this.mCameraDevice.setParameters(params);
                    CamLog.m3d(CameraConstants.TAG, "set gains fast! mInitGains : " + gains.toString());
                    API2FoodWBValue.this.mInitGains = gains;
                    API2FoodWBValue.this.needSetParamOnMetaCallback = false;
                } else if (API2FoodWBValue.this.mCallbackCount < API2FoodWBValue.this.getThreshold()) {
                    API2FoodWBValue aPI2FoodWBValue = API2FoodWBValue.this;
                    aPI2FoodWBValue.mCallbackCount++;
                    CamLog.m3d(CameraConstants.TAG, "onImageMetaData current red = " + gains.getRed() + ", blue = " + gains.getBlue());
                } else {
                    API2FoodWBValue.this.mInitGains = gains;
                }
            }
        }

        API2FoodWBValue() {
            super();
        }

        public void setValue(int key, int value) {
            switch (key) {
                case 4:
                    this.needSetParamOnMetaCallback = true;
                    return;
                default:
                    return;
            }
        }

        public int getThreshold() {
            return 15;
        }

        public String getParamKey() {
            return ParamConstants.KEY_COLOR_CORRECTION_GAINS;
        }

        public void changeRequseter() {
            if (this.mLastGains != null && FoodModuleBase.this.mCameraDevice != null) {
                updateLastGains();
                CameraParameters params = FoodModuleBase.this.mCameraDevice.getParameters();
                if (params != null) {
                    FoodModuleBase.this.mParamUpdater.addRequester(getParamKey(), params.getStrValue(this.mLastGains), false, true);
                    FoodModuleBase.this.mParamUpdater.addRequester("mode_food", "on", false, false);
                }
            } else if (FoodModuleBase.this.mParamUpdater != null) {
                setMetaCallback(true);
                FoodModuleBase.this.mParamUpdater.addRequester(getParamKey(), "", false, true);
                FoodModuleBase.this.mParamUpdater.addRequester("mode_food", "on", false, false);
            }
        }

        public void setMetaCallback(boolean set) {
            if (FoodModuleBase.this.mCameraDevice == null || (this.mLastGains != null && set)) {
                CamLog.m3d(CameraConstants.TAG, "mLastGains already set");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "setFoodMetaDataCallback : " + set);
            FoodModuleBase.this.mCameraDevice.setImageMetaCallback(set ? this.mFoodMetaCallback : null);
        }

        public void onProgressChanged(SeekBar bar, int level) {
            if (FoodModuleBase.this.mCameraDevice != null) {
                CameraParameters parameters = FoodModuleBase.this.mCameraDevice.getParameters();
                if (parameters == null) {
                    CamLog.m7i(CameraConstants.TAG, "parameters is null");
                    return;
                }
                updateLastGains();
                if (FoodModuleBase.this.mSnapShotChecker != null && FoodModuleBase.this.mSnapShotChecker.getSnapShotState() < 1) {
                    FoodModuleBase.this.mParamUpdater.setParameters(parameters, getParamKey(), parameters.getStrValue(this.mLastGains));
                    FoodModuleBase.this.mCameraDevice.setParameters(parameters);
                }
                CameraManagerFactory.getAndroidCameraManager(FoodModuleBase.this.mGet.getActivity()).setParamToBackup(getParamKey(), this.mLastGains);
                CamLog.m3d(CameraConstants.TAG, "set level = " + level + ", gains = " + this.mLastGains.toString());
            }
        }

        public String getLdbStringExtra() {
            if (FoodModuleBase.this.mCameraDevice == null) {
                return null;
            }
            CameraParameters parameters = FoodModuleBase.this.mCameraDevice.getParameters();
            if (parameters != null) {
                return "food_wb_rggb=" + parameters.get(getParamKey()) + ";";
            }
            return null;
        }

        public boolean isFixedInitValue() {
            return this.mInitGains != null;
        }

        public boolean isFixedLastValue() {
            return this.mLastGains != null;
        }

        public void onPauseAfter() {
            super.onPauseAfter();
            if ((FoodModuleBase.this.mGet.isPaused() || FoodModuleBase.this.mGet.isModuleChanging()) && FoodModuleBase.this.mCameraDevice != null) {
                CameraParameters parameter = FoodModuleBase.this.mCameraDevice.getParameters();
                if (parameter != null) {
                    FoodModuleBase.this.setParamUpdater(parameter, ParamConstants.KEY_COLOR_CORRECTION_GAINS, "");
                    FoodModuleBase.this.mCameraDevice.setParameters(parameter);
                }
            }
        }

        private void updateLastGains() {
            updateLastGains(this.mInitGains);
        }

        private void updateLastGains(RggbChannelVector referencGains) {
            if (referencGains == null) {
                CamLog.m11w(CameraConstants.TAG, "mInitGains is null");
                return;
            }
            float blue = referencGains.getBlue() - (0.03f * ((float) (FoodModuleBase.this.mCurrentLevel - 10)));
            this.mLastGains = new RggbChannelVector(Math.min(Math.max(1.0f, referencGains.getRed() + (0.015f * ((float) (FoodModuleBase.this.mCurrentLevel - 10)))), 3.0f), referencGains.getGreenEven(), referencGains.getGreenOdd(), Math.min(Math.max(1.0f, blue), 3.0f));
        }
    }

    public FoodModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public boolean isIndicatorSupported(int indicatorId) {
        if (isRecordingState()) {
            switch (indicatorId) {
                case C0088R.id.indicator_item_hdr_or_flash:
                    return "1".equals(getSettingValue("hdr-mode"));
                case C0088R.id.indicator_item_steady:
                    return isAvailableSteadyCam();
            }
        }
        switch (indicatorId) {
            case C0088R.id.indicator_item_cheese_shutter_or_timer:
            case C0088R.id.indicator_item_hdr_or_flash:
                return true;
        }
        return false;
    }
}
