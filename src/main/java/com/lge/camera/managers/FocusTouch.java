package com.lge.camera.managers;

import android.hardware.Camera.Area;
import android.util.Log;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraManager;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraManagerFactory;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ICameraCallback.CameraAFCallback;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.LdbUtil;
import java.util.ArrayList;

public class FocusTouch extends FocusBase implements FocusTouchInterface {
    private TouchFocusAnimationRunnable mTouchFocusAnimationRunnable = new TouchFocusAnimationRunnable(this);

    /* renamed from: com.lge.camera.managers.FocusTouch$1 */
    class C09141 implements CameraAFCallback {
        C09141() {
        }

        public void onAutoFocus(boolean success, CameraProxy camera) {
            FocusTouch.this.callbackOnTouchShotFocus(success, camera);
        }
    }

    /* renamed from: com.lge.camera.managers.FocusTouch$2 */
    class C09152 implements CameraAFCallback {
        C09152() {
        }

        public void onAutoFocus(boolean success, CameraProxy camera) {
            FocusTouch.this.callbackOnTouchFocus(success, camera);
        }
    }

    class TouchFocusAnimationRunnable extends HandlerRunnable {
        public int mTouchX = -1;
        public int mTouchY = -1;

        public TouchFocusAnimationRunnable(OnRemoveHandler removeFunc) {
            super(removeFunc);
        }

        public void setTouchPosition(int x, int y) {
            this.mTouchX = x;
            this.mTouchY = y;
        }

        public void handleRun() {
            if (this.mTouchX != -1 && this.mTouchY != -1) {
                FocusTouch.this.showFocus();
                FocusTouch.this.setMoveNormalFocusRect(this.mTouchX, this.mTouchY, true);
            }
        }
    }

    public FocusTouch(ModuleInterface iModule) {
        super(iModule);
    }

    public void setCameraFocusView(View view) {
        super.setCameraFocusView(view);
    }

    public void startFocusByTouchPress(int x, int y, boolean bTakePicture) {
        if (this.mGet.checkModuleValidate(96)) {
            CameraProxy cameraDevice = this.mGet.getCameraDevice();
            if (cameraDevice != null) {
                cameraDevice.cancelAutoFocus();
                setFocusState(0);
                setMoveNormalFocusRect(x, y, false);
                setFocusWindow(this.mGet.getRealFocusWindow(this.mFocusRect, this.mFocusAreaWidth, this.mFocusAreaHeight, x, y));
                setFocusState(1);
                updateFocusStateIndicator();
                if (bTakePicture) {
                    this.mTouchFocusAnimationRunnable.setTouchPosition(x, y);
                    this.mGet.postOnUiThread(this.mTouchFocusAnimationRunnable, 100);
                    cameraDevice.autoFocus(this.mGet.getHandler(), new C09141());
                } else {
                    this.mGet.removePostRunnable(this.mHideTouchFocus);
                    this.mTouchFocusAnimationRunnable.setTouchPosition(x, y);
                    setEVShutterButtonPosition(x, y);
                    this.mGet.runOnUiThread(this.mTouchFocusAnimationRunnable);
                    cameraDevice.autoFocus(this.mGet.getHandler(), new C09152());
                }
                setFocusModeToBackupParam();
                CamLog.m3d(CameraConstants.TAG, "### startFocusByTouchPress : x = " + x + ", y = " + y);
            }
        }
    }

    private void setFocusModeToBackupParam() {
        if (this.mGet.isAFSupported()) {
            CameraManager cameraManager = CameraManagerFactory.getAndroidCameraManager(this.mGet.getActivity());
            if (cameraManager != null) {
                CamLog.m3d(CameraConstants.TAG, "setFocusModeToBackupParam, focus mode = auto");
                cameraManager.setParamToBackup("focus-mode", "auto");
            }
        }
    }

    public void startFocusByTouchPressForTracking(int x, int y) {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null) {
            if (x < 0 && y < 0) {
                CameraParameters parameters = cameraDevice.getParameters();
                if (parameters == null) {
                    CamLog.m11w(CameraConstants.TAG, "parameter is null");
                    return;
                }
                if (this.mFocusArea == null) {
                    this.mFocusArea = new ArrayList();
                    this.mFocusArea.add(new Area(this.mFocusRect, 1));
                }
                ((Area) this.mFocusArea.get(0)).rect.left = -1;
                ((Area) this.mFocusArea.get(0)).rect.right = -1;
                ((Area) this.mFocusArea.get(0)).rect.top = -1;
                ((Area) this.mFocusArea.get(0)).rect.bottom = -1;
                setTrackingsWindowParameters(parameters);
            } else if (this.mGet.checkModuleValidate(96)) {
                cameraDevice.cancelAutoFocus();
                setMoveNormalFocusRect(x, y, false);
                setFocusWindow(this.mGet.getRealFocusWindow(this.mFocusRect, this.mFocusAreaWidth, this.mFocusAreaHeight, x, y));
                updateFocusStateIndicator();
                CamLog.m3d(CameraConstants.TAG, "### startFocusByTouchPressForTracking : x = " + x + ", y = " + y);
            }
        }
    }

    protected void updateFocusStateIndicator(int focusState) {
        CamLog.m3d(CameraConstants.TAG, "updateFocusStateIndicator : " + focusState);
        switch (focusState) {
            case 0:
                setFocusViewState(0);
                break;
            case 1:
                setFocusViewState(6);
                break;
            case 2:
            case 3:
                setFocusViewState(7);
                if (this.mCameraFocusView != null && this.mCameraFocusView.isShown()) {
                    setEVShutterVisiblity(true);
                    break;
                }
            case 4:
                setFocusViewState(8);
                if (this.mCameraFocusView != null && this.mCameraFocusView.isShown()) {
                    setEVShutterVisiblity(true);
                    break;
                }
            case 11:
                int i = (this.mGet.isAFSupported() || !this.mGet.isRearCamera()) ? 9 : 10;
                setFocusViewState(i);
                break;
            case 14:
                CamLog.m3d(CameraConstants.TAG, "-tf- FOCUS_TRACKING");
                setFocusViewState(11);
                break;
            default:
                CamLog.m3d(CameraConstants.TAG, "Wrong focus state!: " + this.mGet.getFocusState());
                break;
        }
        setStateOnFocusLock();
    }

    private void setStateOnFocusLock() {
        if (this.mGet.isFocusLock()) {
            setFocusViewState(12);
        }
    }

    public int getTouchGuideRes() {
        return C0088R.drawable.focus_touch_taf;
    }

    private void callbackOnTouchShotFocus(boolean focused, CameraProxy Camera) {
        Log.d(CameraConstants.TAG, "TIME CHECK : Auto focus [END] - callbackOnTouchShotFocus() : " + (focused ? "FOCUS_SUCCESS " : "FOCUS_FAIL"));
        CamLog.m3d(CameraConstants.TAG, "callbackOnTouchShotFocus focused = " + focused);
        if (this.mGet.checkModuleValidate(128)) {
            this.mGet.removePostRunnable(this.mReleaseTouchFocus);
            this.mGet.postOnUiThread(this.mReleaseTouchFocus, 1000);
        }
        if (this.mGet.getFocusState() == 0) {
            CamLog.m3d(CameraConstants.TAG, "callbackOnTouchShotFocus : focus state is FOCUS_NOT_STARTED.");
            return;
        }
        this.mGet.removePostRunnable(this.mTouchFocusAnimationRunnable);
        this.mTouchFocusAnimationRunnable.setTouchPosition(-1, -1);
        if (focused) {
            setFocusState(this.mGet.isFocusLock() ? 3 : 2);
        } else {
            setFocusState(4);
        }
        if (this.mGet.getCameraState() != 6) {
            this.mGet.playSound(1, focused, 0);
        }
        if (this.mGet.checkModuleValidate(128)) {
            this.mGet.removePostRunnable(this.mReleaseTouchFocus);
            if (this.mHandler != null) {
                this.mHandler.removeMessages(4);
                this.mHandler.sendEmptyMessage(4);
            }
        } else {
            if (focused) {
                this.mGet.removePostRunnable(this.mHideTouchFocus);
                this.mGet.postOnUiThread(this.mHideTouchFocus, 1000);
            } else {
                this.mGet.removePostRunnable(this.mHideTouchFocus);
                this.mGet.postOnUiThread(this.mHideTouchFocus, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
            }
            if (this.mHandler != null) {
                this.mHandler.sendEmptyMessage(7);
            }
        }
        updateFocusStateIndicator();
    }

    private void callbackOnTouchFocus(boolean focused, CameraProxy Camera) {
        Log.d(CameraConstants.TAG, "TIME CHECK : Auto focus [END] - callbackOnTouchFocus() : " + (focused ? "FOCUS_SUCCESS " : "FOCUS_FAIL"));
        CamLog.m3d(CameraConstants.TAG, "callbackOnTouchFocus focused = " + focused);
        if (this.mGet.getFocusState() == 0 || this.mGet.getFocusState() == 14) {
            CamLog.m3d(CameraConstants.TAG, "callbackOnTouchFocus : focus state is " + this.mGet.getFocusState());
        } else if (this.mGet.getFocusState() == 2) {
            callbackOnTouchShotFocus(focused, Camera);
        } else {
            this.mGet.removePostRunnable(this.mTouchFocusAnimationRunnable);
            this.mGet.removePostRunnable(this.mReleaseTouchFocus);
            this.mTouchFocusAnimationRunnable.setTouchPosition(-1, -1);
            setFocusState(focused ? 3 : 4);
            if (this.mGet.checkModuleValidate(192)) {
                boolean requestAudioFocus;
                if (!this.mGet.isRecordingPriorityMode()) {
                    if (this.mGet.isAELock() || this.mGet.isFocusLock()) {
                        CameraDeviceUtils.setEnable3ALocks(Camera, true, false);
                    }
                    try {
                        this.mGet.setParameters(Camera.getParameters());
                    } catch (RuntimeException e) {
                        CamLog.m8i(CameraConstants.TAG, "RuntimeException:", e);
                    }
                }
                if ("on".equals(this.mGet.getSettingValue(Setting.KEY_VOICESHUTTER)) || !AudioUtil.isWiredHeadsetOn()) {
                    requestAudioFocus = false;
                } else {
                    requestAudioFocus = true;
                }
                this.mGet.playSound(1, focused, 0, requestAudioFocus);
                AudioUtil.performHapticFeedback(this.mCameraFocusView, 65576);
                if (!this.mGet.isAeControlBarTouched()) {
                    this.mGet.postOnUiThread(this.mReleaseTouchFocus, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                }
            } else {
                if (focused) {
                    this.mGet.removePostRunnable(this.mHideTouchFocus);
                    if (!this.mGet.isAeControlBarTouched()) {
                        this.mGet.postOnUiThread(this.mHideTouchFocus, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                    }
                } else {
                    this.mGet.removePostRunnable(this.mHideTouchFocus);
                    if (!this.mGet.isAeControlBarTouched()) {
                        this.mGet.postOnUiThread(this.mHideTouchFocus, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                    }
                }
                if (this.mHandler != null) {
                    this.mHandler.sendEmptyMessage(7);
                }
            }
            if (this.mGet.isFocusLock() || this.mGet.isAELock()) {
                LdbUtil.sendLDBIntent(this.mGet.getAppContext(), LdbConstants.LDB_FEATURE_NAME_AE_AF_LOCK);
            } else {
                LdbUtil.sendLDBIntent(this.mGet.getAppContext(), LdbConstants.LDB_FEATURE_NAME_TOUCH_FOCUS);
            }
            updateFocusStateIndicator();
        }
    }

    public void releaseFocusHandler() {
        if (this.mReleaseTouchFocus != null) {
            this.mGet.removePostRunnable(this.mReleaseTouchFocus);
            this.mGet.removePostRunnable(this.mTouchFocusAnimationRunnable);
            this.mGet.removePostRunnable(this.mHideTouchFocus);
        }
    }

    public void releaseTouchFocus() {
        CamLog.m3d(CameraConstants.TAG, "releaseTouchFocus");
        if (this.mGet.checkModuleValidate(208)) {
            cancelAutoFocus();
            hideFocus();
            if (this.mHandler != null) {
                this.mHandler.sendEmptyMessage(1);
                this.mHandler.sendEmptyMessage(10);
            }
        }
    }

    public void unregisterCallback() {
    }

    public boolean cancelAutoFocus() {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice == null || !this.mGet.checkModuleValidate(79) || !this.mGet.checkModuleValidate(2)) {
            return false;
        }
        cameraDevice.cancelAutoFocus();
        return true;
    }

    public boolean hideFocus() {
        if (this.mTouchFocusAnimationRunnable != null) {
            this.mGet.removePostRunnable(this.mTouchFocusAnimationRunnable);
            this.mTouchFocusAnimationRunnable.setTouchPosition(-1, -1);
        }
        return super.hideFocus();
    }

    public void release() {
        unregisterCallback();
        super.release();
    }

    public void onAEControlBarDown() {
        releaseFocusHandler();
    }

    public void onAEControlBarUp() {
        HandlerRunnable runnable;
        if (this.mGet.checkModuleValidate(192)) {
            runnable = this.mReleaseTouchFocus;
        } else {
            runnable = this.mHideTouchFocus;
        }
        this.mGet.postOnUiThread(runnable, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
    }

    public boolean hideFocusForce() {
        return super.hideFocusForce();
    }
}
