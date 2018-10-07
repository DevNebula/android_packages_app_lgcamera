package com.lge.camera.managers;

import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.lge.camera.C0088R;
import com.lge.camera.components.DrumBarController;
import com.lge.camera.components.DrumControllerListener;
import com.lge.camera.components.DrumItem;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.ManualModeItem;
import com.lge.camera.util.Utils;

public class ManualFocusManager extends ManagerInterfaceImpl {
    protected static final float DRUM_MARGIN_END_PERCENTAGE = 0.164f;
    protected static final float DRUM_MARGIN_END_PERCENTAGE_LONG_LCD = 0.148f;
    public static final String MODEITEM_MANUAL_FOCUS = "Manual focus";
    private View mBaseView;
    protected boolean mInit = false;
    protected boolean mIsManualFocusMode;
    ManualDataMF mMFManualData;
    ManualModeItem mMFManualModeItem;
    protected DrumBarController mManualFocusDrum;
    private RotateLayout mRotateView;

    /* renamed from: com.lge.camera.managers.ManualFocusManager$2 */
    class C10952 implements DrumControllerListener {
        C10952() {
        }

        public void onItemSelected(DrumItem item, boolean updateWheel) {
            if (item != null && !ManualFocusManager.this.isDrumControlUnavailable() && ManualFocusManager.this.mManualFocusDrum.getVisibility() == 0) {
                ManualFocusManager.this.setMF(item.getKey(), item.getValue(), false);
            }
        }

        public void playDrumEffectSound() {
            ManualFocusManager.this.mGet.playSound(11, false, 0);
        }

        public void onDrumScrollReleased(DrumItem item) {
            ManualFocusManager.this.onScrollReleased(16, item.getValue());
        }

        public boolean isAvailableToMoveDrum() {
            return ManualFocusManager.this.isDrumMovingAvailable();
        }
    }

    public ManualFocusManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        if (!this.mInit) {
            initAllViews();
            this.mInit = true;
        }
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        setManualFocusViewEnable(true);
    }

    public void updateViews() {
        setDegree(this.mGet.getOrientationDegree(), false);
        setButtonAndDrumUnselected();
    }

    public void setButtonAndDrumUnselected() {
    }

    public void initAllViews() {
        setupViews();
        CameraProxy camera = this.mGet.getCameraDevice();
        if (camera != null) {
            CameraParameters parameter = camera.getParameters();
            if (parameter != null) {
                loadManualData(parameter);
                buildManualFocusItem(parameter);
                createManualFocusDrums();
            }
        }
    }

    private void buildManualFocusItem(CameraParameters parameter) {
        if (parameter != null && this.mMFManualData != null && this.mMFManualData.getEntryArray() != null) {
            String[] entryArray = this.mMFManualData.getEntryArray();
            int length = entryArray.length;
            this.mMFManualModeItem = new ManualModeItem();
            this.mMFManualModeItem.setTitle("Manual focus");
            this.mMFManualModeItem.setKey(ParamConstants.MANUAL_FOCUS_STEP);
            this.mMFManualModeItem.setSettingKey(this.mMFManualData.getSettingKey());
            boolean[] showEntryValue = new boolean[length];
            for (int i = 0; i < length; i++) {
                if ("".equals(entryArray[i])) {
                    showEntryValue[i] = false;
                } else {
                    showEntryValue[i] = true;
                }
            }
            this.mMFManualModeItem.setEntries(entryArray);
            this.mMFManualModeItem.setValues(this.mMFManualData.getValueArray());
            this.mMFManualModeItem.setShowEntryValue(showEntryValue);
            this.mMFManualModeItem.setSelectedIndex(0);
            this.mMFManualModeItem.setPrefDefaultValue(this.mMFManualData.getDefaultValue());
            this.mMFManualModeItem.setDefaultValue("-1");
            this.mMFManualModeItem.setDefaultEntryValue("-1");
            this.mMFManualModeItem.setPrefDefaultValue("-1");
        }
    }

    public synchronized void loadManualData(CameraParameters parameter) {
        this.mMFManualData = new ManualDataMF(this.mGet, this.mGet.getShotMode());
        this.mMFManualData.loadData(parameter);
    }

    protected void makeData() {
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        init();
    }

    private void setupViews() {
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
        if (vg != null) {
            this.mBaseView = this.mGet.inflateView(C0088R.layout.manual_focus_drum_layout);
            vg.addView(this.mBaseView, 0);
            this.mRotateView = (RotateLayout) this.mGet.findViewById(C0088R.id.manual_focus_drum_rotate);
        }
    }

    public void setListenerAfterOneShotCallback() {
    }

    public void setDegree(int degree, boolean animation) {
        relocateMFButton(degree);
        rotateDrumLayout(270, degree);
        rotateDrumComponents(degree);
    }

    private void rotateDrumComponents(int degree) {
        if (this.mManualFocusDrum != null) {
            int convertDegree = (degree + 90) % 360;
            if (Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
                convertDegree = (degree + 180) % 360;
            }
            this.mManualFocusDrum.setDegree(convertDegree);
        }
    }

    public void rotateDrumLayout(int layoutDegree, int degree) {
        RotateLayout manualDrumRotate = (RotateLayout) this.mGet.findViewById(C0088R.id.manual_focus_drum_rotate);
        RelativeLayout wrapper = (RelativeLayout) this.mGet.findViewById(C0088R.id.manual_focus_drum_rotate_wrapper);
        RelativeLayout wrapperInner = (RelativeLayout) this.mGet.findViewById(C0088R.id.manual_focus_drum_rotate_wrapper_inner);
        if (!(manualDrumRotate == null || wrapper == null || wrapperInner == null)) {
            LayoutParams wrapperParams = (LayoutParams) wrapper.getLayoutParams();
            RelativeLayout.LayoutParams wrapperInnerParams = (RelativeLayout.LayoutParams) wrapperInner.getLayoutParams();
            if (!(wrapperParams == null || wrapperInnerParams == null)) {
                int[] size = Utils.getLCDsize(getAppContext(), true);
                wrapperParams.width = Math.min(size[0], size[1]);
                wrapperParams.height = Math.max(size[0], size[1]);
                wrapper.setLayoutParams(wrapperParams);
                wrapperInner.setLayoutParams(wrapperInnerParams);
            }
            manualDrumRotate.rotateLayout(layoutDegree);
        }
        updateDrumPadding();
    }

    public void updateDrumPadding() {
        this.mManualFocusDrum = (DrumBarController) this.mGet.findViewById(C0088R.id.drum_mf_in_auto_mode);
        if (this.mManualFocusDrum != null) {
            this.mManualFocusDrum.setPaddingRelative(this.mManualFocusDrum.getPaddingStart(), this.mManualFocusDrum.getPaddingTop(), this.mGet.getAEControlLayoutButtomMargin(), this.mManualFocusDrum.getPaddingBottom());
            this.mManualFocusDrum.initResources(16, this.mMFManualModeItem);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
        if (vg != null && this.mBaseView != null) {
            vg.removeView(this.mBaseView);
            this.mInit = false;
        }
    }

    public void onCameraSwitchingStart() {
        super.onCameraSwitchingStart();
    }

    public void onCameraSwitchingEnd() {
        super.onCameraSwitchingEnd();
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        if (getManualFocusModeEx()) {
            setManualFocusModeEx(false);
            this.mGet.setManualFocusButtonVisibility(false);
        }
    }

    public void setButtonsSelected() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
            }
        });
    }

    protected void setManualFocusListener() {
        if (this.mManualFocusDrum != null) {
            this.mManualFocusDrum.setDrumControllerListener(new C10952());
        }
    }

    public boolean setMF(String key, String value, boolean save) {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice == null) {
            return false;
        }
        CameraParameters parameter = cameraDevice.getParameters();
        if (parameter == null || this.mMFManualData == null) {
            return false;
        }
        if (ManualControlManager.LOCK.equals(value)) {
            this.mMFManualData.setValue(this.mMFManualData.matchValue());
            this.mGet.setManualFocus(true);
            this.mGet.setParamUpdater(parameter, "focus-mode", "normal");
            this.mGet.setParamUpdater(parameter, ParamConstants.MANUAL_FOCUS_STEP, this.mMFManualData.mValueString);
            cameraDevice.setParameters(parameter);
            if (this.mManualFocusDrum != null) {
                this.mManualFocusDrum.setSelectedItem(this.mMFManualData.getValue(), false);
            }
        } else if ("auto".equals(value)) {
            this.mMFManualData.setValue(value);
            this.mGet.setParamUpdater(parameter, ParamConstants.MANUAL_FOCUS_STEP, value);
            this.mGet.setManualFocus(false);
        } else {
            this.mMFManualData.setValue(value);
            setParameters(key, value);
        }
        if (save) {
            this.mGet.setSetting(this.mMFManualData.getSettingKey(), this.mMFManualData.mValueString, true);
        }
        this.mGet.setTrackingFocusState(false);
        return true;
    }

    public void setParameters(String key, String value) {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null) {
            CameraParameters parameters = cameraDevice.getParameters();
            if (parameters != null) {
                this.mGet.setParamUpdater(parameters, key, value);
                cameraDevice.setParameters(parameters);
            }
        }
    }

    protected boolean isDrumControlUnavailable() {
        if (this.mGet.checkModuleValidate(64) && !this.mGet.isTimerShotCountdown()) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "drum control unavailable ");
        return true;
    }

    protected void onScrollReleased(int type, String value) {
        this.mGet.setSetting(Setting.KEY_MANUAL_FOCUS_STEP, value, true);
    }

    public boolean isDrumMovingAvailable() {
        return true;
    }

    protected void createManualFocusDrums() {
        updateDrumPadding();
        setManualFocusListener();
    }

    public void setVisible(boolean show) {
        if (this.mRotateView == null) {
            CamLog.m3d(CameraConstants.TAG, "mRotateView is null.");
            return;
        }
        this.mRotateView.setVisibility(show ? 0 : 4);
        if (getManualFocusModeEx()) {
            this.mGet.setManualFocusButtonVisibility(show);
        }
    }

    public boolean getVisible() {
        if (this.mRotateView == null) {
            CamLog.m3d(CameraConstants.TAG, "mRotateView is null.");
            return false;
        } else if (this.mRotateView.getVisibility() == 0) {
            return true;
        } else {
            return false;
        }
    }

    private void relocateMFButton(int degree) {
        if (this.mRotateView != null) {
            this.mRotateView.rotateLayout(degree);
        }
    }

    public void setManualDataMFValue(float currentMFStep) {
        if (this.mMFManualData != null) {
            this.mMFManualData.setValue(currentMFStep);
        }
    }

    public void setManualFocusModeEx(boolean isSet) {
        if (this.mMFManualModeItem != null && this.mRotateView != null) {
            CamLog.m3d(CameraConstants.TAG, "ManualFocusModeEx : " + isSet);
            this.mIsManualFocusMode = isSet;
            if (isSet) {
                setMF(this.mMFManualModeItem.getKey(), ManualControlManager.LOCK, false);
                this.mRotateView.setVisibility(0);
                return;
            }
            setMF(this.mMFManualModeItem.getKey(), "auto", false);
            this.mRotateView.setVisibility(4);
        }
    }

    public boolean getManualFocusModeEx() {
        return this.mIsManualFocusMode;
    }

    public void setManualFocusViewEnable(boolean enable) {
        if (this.mRotateView == null || this.mManualFocusDrum == null) {
            CamLog.m3d(CameraConstants.TAG, "Manual focus view state is weird");
            return;
        }
        this.mRotateView.setEnabled(enable);
        this.mManualFocusDrum.setEnabled(enable);
    }

    public boolean isAvailableManualFocus(boolean fromInitOpenCameraRoutine) {
        String mode;
        if (fromInitOpenCameraRoutine) {
            mode = this.mGet.getSettingValue(Setting.KEY_MODE);
        } else {
            mode = this.mGet.getShotMode();
        }
        return FunctionProperties.isSupportedManualFocus() && "mode_normal".equals(mode) && this.mGet.isAFSupported() && this.mGet.isRearCamera();
    }
}
