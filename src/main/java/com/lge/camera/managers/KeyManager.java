package com.lge.camera.managers;

import android.view.KeyEvent;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.TelephonyUtil;

public class KeyManager extends ManagerInterfaceImpl {
    public static final int KEYCODE_QCLIP_HOT_KEY = 165;
    private boolean mIsCenterKeyPressed = false;
    private boolean mIsLongKeyPressed = false;
    private KeyInterface mKeyInterface = null;

    public KeyManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
    }

    public void setKeyInterface(KeyInterface keyIterface) {
        this.mKeyInterface = keyIterface;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        CamLog.m3d(CameraConstants.TAG, "onKeyDown" + keyCode);
        if (event != null && event.getRepeatCount() != 0 && keyCode != 24 && keyCode != 25 && keyCode != 79 && keyCode != 66 && keyCode != 23 && keyCode != 165 && keyCode != 172 && keyCode != 165) {
            return false;
        }
        switch (keyCode) {
            case 4:
                CamLog.m3d(CameraConstants.TAG, "KEYCODE_BACK");
                if (this.mKeyInterface != null) {
                    return this.mKeyInterface.doBackKey();
                }
                return false;
            case 19:
            case 20:
            case 21:
            case 22:
            case 84:
            case 218:
                if (this.mKeyInterface == null) {
                    return false;
                }
                CamLog.m3d(CameraConstants.TAG, "KEYCODE_DPAD DOWN");
                return this.mKeyInterface.doDpadKey(keyCode, event);
            case 23:
                if (this.mKeyInterface == null || event == null) {
                    return false;
                }
                CamLog.m3d(CameraConstants.TAG, "KEYCODE_DPAD_CENTER Key down");
                if (ModelProperties.isKeyPadSupported(getAppContext())) {
                    return doDpadCenterKey(event);
                }
                return doKeyEnter(66, event);
            case 24:
            case 25:
                CamLog.m3d(CameraConstants.TAG, "KEYCODE_VOLUME_UP_DOWN=" + keyCode);
                return doVolumeUpDown(keyCode, event);
            case 27:
                CamLog.m3d(CameraConstants.TAG, "KEYCODE_CAMERA key down");
                return doCameraKey(event);
            case 66:
                CamLog.m3d(CameraConstants.TAG, "KEYCODE_ENTER");
                return doKeyEnter(keyCode, event);
            case 79:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 126:
            case 127:
                if (TelephonyUtil.phoneInCall(this.mGet.getAppContext())) {
                    CamLog.m3d(CameraConstants.TAG, "go to incomming call");
                    return false;
                }
                CamLog.m3d(CameraConstants.TAG, "MEDIA_KEY or HEADSETHOOK DOWN ");
                if (event == null) {
                    return true;
                }
                return doHeadSetHookAndMediaKey(event);
            case 82:
                CamLog.m3d(CameraConstants.TAG, "KEYCODE_MENU");
                return false;
            case 165:
                CamLog.m3d(CameraConstants.TAG, "KEYCODE_HOT_KEY");
                return true;
            case 172:
                CamLog.m3d(CameraConstants.TAG, "KEYCODE_FINGER_PRINT_SENSOR");
                return doFingerPrintShot(keyCode, event);
            default:
                return false;
        }
    }

    private boolean doHeadSetHookAndMediaKey(KeyEvent event) {
        if (TelephonyUtil.phoneInCall(this.mGet.getAppContext())) {
            CamLog.m3d(CameraConstants.TAG, "go to incomming call");
            return false;
        } else if (this.mKeyInterface != null) {
            return this.mKeyInterface.doHeadSetHookAndMediaKey(event);
        } else {
            return false;
        }
    }

    private boolean doKeyEnter(int keyCode, KeyEvent event) {
        if (ModelProperties.isKeyPadSupported(getAppContext())) {
            return doDpadCenterKey(event);
        }
        if (event == null || this.mKeyInterface == null) {
            return true;
        }
        if (!(event.getAction() == 0 && this.mKeyInterface.onShutterDown(keyCode, event)) && event.getAction() == 1 && this.mKeyInterface.onShutterUp(keyCode, event)) {
        }
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event != null && event.getRepeatCount() != 0 && keyCode != 24 && keyCode != 25 && keyCode != 23 && keyCode != 79 && keyCode != 165) {
            return false;
        }
        switch (keyCode) {
            case 4:
                CamLog.m3d(CameraConstants.TAG, "KEYCODE_BACK keyup");
                return false;
            case 19:
            case 20:
            case 21:
            case 22:
                if (this.mKeyInterface == null) {
                    return false;
                }
                CamLog.m3d(CameraConstants.TAG, "KEYCODE_DPAD UP");
                return this.mKeyInterface.doDpadKey(keyCode, event);
            case 23:
                if (this.mKeyInterface == null || event == null) {
                    return false;
                }
                if (ModelProperties.isKeyPadSupported(getAppContext())) {
                    return doDpadCenterKey(event);
                }
                return doKeyEnter(66, event);
            case 24:
            case 25:
                CamLog.m3d(CameraConstants.TAG, "KEYCODE_VOLUME keyUp.");
                return doVolumeUpDown(keyCode, event);
            case 66:
                CamLog.m3d(CameraConstants.TAG, "KEYCODE_ENTER keyUp.");
                return doKeyEnter(keyCode, event);
            case 79:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 126:
            case 127:
                if (this.mKeyInterface != null) {
                    return this.mKeyInterface.doHeadSetHookAndMediaKey(event);
                }
                return false;
            case 82:
                CamLog.m3d(CameraConstants.TAG, "KEYCODE_MENU keyup. doMenuKey().");
                if (event == null || this.mKeyInterface == null || event.isCanceled()) {
                    return false;
                }
                return this.mKeyInterface.doMenuKey();
            case 165:
                CamLog.m3d(CameraConstants.TAG, "KEYCODE_HOT_KEY");
                return doHotKeyUp(keyCode, event);
            case 172:
                return true;
            default:
                return false;
        }
    }

    private boolean doFingerPrintShot(int keyCode, KeyEvent event) {
        if (!FunctionProperties.isFingerPrintShotEnabled(this.mGet.getAppContext())) {
            return true;
        }
        if (event == null || this.mKeyInterface == null || this.mGet == null) {
            return false;
        }
        int state = this.mGet.getCameraState();
        if (!this.mGet.checkModuleValidate(15)) {
            return true;
        }
        if ((state != 1 && state != 6 && state != 7) || this.mGet.isTimerShotCountdown() || this.mGet.isRearCamera() || this.mGet.isVideoCaptureMode() || this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_FULL_SCREEN_GUIDE) || event.getAction() != 0 || event.getRepeatCount() != 1) {
            return true;
        }
        this.mKeyInterface.onFingerPrintSensorDown(keyCode, event);
        return true;
    }

    private boolean doHotKeyUp(int keyCode, KeyEvent event) {
        if (event == null || this.mKeyInterface == null) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "doHotKeyUp");
        if (event.getAction() == 0 || event.getRepeatCount() != 0 || !FunctionProperties.isSupportedHotKeyRecording(getAppContext()) || !this.mGet.checkModuleValidate(80)) {
            return true;
        }
        if (this.mGet.isAttachIntent() && !this.mGet.isVideoCaptureMode()) {
            return true;
        }
        this.mKeyInterface.doHotKeyUp();
        return true;
    }

    private boolean doVolumeUpDown(int keyCode, KeyEvent event) {
        if (this.mGet == null) {
            return true;
        }
        if (event == null || this.mKeyInterface == null || this.mGet == null || (CameraConstants.MODE_MANUAL_VIDEO.equals(this.mGet.getShotMode()) && this.mGet.isLoopbackAvailable())) {
            return false;
        }
        if (this.mGet.isJogZoomMoving() || this.mGet.isZoomControllerTouched() || this.mGet.isGestureZooming()) {
            return true;
        }
        int state = this.mGet.getCameraState();
        if (!this.mGet.checkModuleValidate(15)) {
            return true;
        }
        if ((state != 1 && state != 6 && state != 7) || this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_FULL_SCREEN_GUIDE)) {
            return true;
        }
        boolean isNotNeedToCheckingRepeatCount = CameraConstants.MODE_MANUAL_VIDEO.equals(this.mGet.getShotMode());
        CamLog.m3d(CameraConstants.TAG, "doVolumeUpDown : repeat count = " + event.getRepeatCount() + ", isNotNeedToCheckingRepeatCount : " + isNotNeedToCheckingRepeatCount);
        if (event.getRepeatCount() == 5 && !isNotNeedToCheckingRepeatCount) {
            this.mIsLongKeyPressed = true;
            this.mKeyInterface.onVolumeKeyLongPressed(keyCode, event);
            return true;
        } else if (event.getRepeatCount() != 0 && !isNotNeedToCheckingRepeatCount) {
            return true;
        } else {
            if (event.getAction() == 0) {
                this.mKeyInterface.onShutterDown(keyCode, event);
                return true;
            }
            this.mKeyInterface.onShutterUp(keyCode, event);
            this.mIsLongKeyPressed = false;
            return true;
        }
    }

    private boolean doCameraKey(KeyEvent event) {
        if (event == null || this.mKeyInterface == null || this.mGet == null || this.mGet.isRotateDialogVisible()) {
            return false;
        }
        this.mKeyInterface.onCameraKeyUp(event);
        return true;
    }

    private boolean doDpadCenterKey(KeyEvent event) {
        if (event == null || this.mKeyInterface == null) {
            return false;
        }
        if (event.getAction() == 0) {
            this.mIsCenterKeyPressed = true;
            if (event.getRepeatCount() == 20) {
                return this.mKeyInterface.doDpadCenterKey(event);
            }
            return false;
        } else if (event.getAction() != 1) {
            return false;
        } else {
            this.mIsCenterKeyPressed = false;
            return this.mKeyInterface.doDpadCenterKey(event);
        }
    }

    public boolean isVolumeLongKeyPressed() {
        return this.mIsLongKeyPressed;
    }

    public boolean isCenterKeyPressed() {
        return this.mIsCenterKeyPressed;
    }

    public void setDegree(int degree, boolean animation) {
    }
}
