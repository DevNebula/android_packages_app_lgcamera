package com.lge.camera.device.api2;

import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;

public class ActionStateMachine {
    public static final int ACTION_AUTOFOCUS = 2;
    public static final int ACTION_AUTOFOCUS_FLASH = 3;
    public static final int ACTION_CANCEL_FOCUS = 4;
    public static final int ACTION_FLASH_SHOT = 1;
    public static final int ATCION_NO_TYPE = 0;
    private int mActionStep = 0;
    private int mActionType = 0;
    private ArrayList<Action> mActions = null;
    private ICameraOps mCameraOps;
    private Handler mHandler;

    public ActionStateMachine(Handler handler, ICameraOps cameraOps) {
        this.mCameraOps = cameraOps;
        this.mHandler = handler;
    }

    public boolean doActionForFlash(boolean isReprocess) {
        CamLog.m3d(CameraConstants.TAG, "doActionForFlash");
        if (!isValidAction() || this.mActionType != 3) {
            createActionsForFlashShot(isReprocess);
        } else if ((this.mActions.get(this.mActionStep) instanceof ActionPrecapture) || (this.mActions.get(this.mActionStep) instanceof ActionLockExposure)) {
            int i = this.mActionStep;
            while (i < this.mActions.size()) {
                if ((this.mActions.get(i) instanceof ActionCaptureYuvFlash) || (this.mActions.get(i) instanceof ActionCaptureJpegFlash)) {
                    ((Action) this.mActions.get(i)).setChcekStep(0);
                    CamLog.m3d(CameraConstants.TAG, "setChcekStep ");
                }
                i++;
            }
        } else {
            CamLog.m3d(CameraConstants.TAG, "doActionStepByCameraOps ");
            doActionStepByCameraOps();
        }
        return true;
    }

    public void doActionForAutoFocus(boolean isPreFlash, boolean isReprocess) {
        CamLog.m3d(CameraConstants.TAG, "doActionForAutoFocus " + isPreFlash);
        if (isValidAction() && this.mActionType == 4) {
            if (isPreFlash) {
                addActionsForAutoFocusFlash(isReprocess);
            } else {
                addActionsForAutoFocus();
            }
        } else if (isPreFlash) {
            createActionsForAutoFocusFlash(isReprocess);
        } else {
            createActionsForAutoFocus();
        }
    }

    public void doActionForCancelFocus() {
        CamLog.m3d(CameraConstants.TAG, "doActionForCancelFocus");
        if (this.mActionType != 4 && this.mActionType != 1) {
            if ((this.mActionType != 2 && this.mActionType != 3) || !(this.mActions.get(this.mActionStep) instanceof ActionCancelFocus)) {
                createActionsForCancelFocus();
            }
        }
    }

    public void onCaptureResult(final TotalCaptureResult result) {
        if (isValidAction()) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    int resultCode = result.getRequest().hashCode();
                    if (ActionStateMachine.this.isValidAction() && ((Action) ActionStateMachine.this.mActions.get(ActionStateMachine.this.mActionStep)).canGoNextStep(result, resultCode)) {
                        ActionStateMachine.this.mActionStep = ActionStateMachine.this.mActionStep + 1;
                        ActionStateMachine.this.doActionStepByCaptureResult();
                    }
                }
            });
        }
    }

    public void onCaptureFailed(final CaptureRequest request) {
        if (isValidAction()) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    int requestCode = request.hashCode();
                    if (ActionStateMachine.this.isValidAction() && ((Action) ActionStateMachine.this.mActions.get(ActionStateMachine.this.mActionStep)).skipStep(requestCode)) {
                        ActionStateMachine.this.mActionStep = ActionStateMachine.this.mActionStep + 1;
                        ActionStateMachine.this.doActionStepByCaptureResult();
                    }
                }
            });
        }
    }

    private void createActionsForFlashShot(boolean isReprocess) {
        CamLog.m3d(CameraConstants.TAG, "createActionsForFlashShot");
        this.mActionType = 1;
        this.mActionStep = 0;
        this.mActions = new ArrayList();
        this.mActions.add(new ActionPrecapture(this.mCameraOps, 0));
        this.mActions.add(new ActionLockExposure(this.mCameraOps, 0));
        if (isReprocess) {
            this.mActions.add(new ActionCaptureYuvFlash(this.mCameraOps, 0));
            this.mActions.add(new ActionTakePicture(this.mCameraOps, 0));
        } else {
            this.mActions.add(new ActionCaptureJpegFlash(this.mCameraOps, 0));
        }
        doActionStepByCameraOps();
    }

    private void createActionsForAutoFocus() {
        CamLog.m3d(CameraConstants.TAG, "createActionsForAutoFocus");
        this.mActionType = 2;
        this.mActionStep = 0;
        this.mActions = new ArrayList();
        this.mActions.add(new ActionAutoFocus(this.mCameraOps, 0));
        doActionStepByCameraOps();
    }

    private void createActionsForAutoFocusFlash(boolean isReprocess) {
        CamLog.m3d(CameraConstants.TAG, "createActionsForAutoFocusFlash");
        this.mActionType = 3;
        this.mActionStep = 0;
        this.mActions = new ArrayList();
        this.mActions.add(new ActionPrecapture(this.mCameraOps, 0));
        this.mActions.add(new ActionLockExposure(this.mCameraOps, 0));
        if (isReprocess) {
            this.mActions.add(new ActionCaptureYuvFlash(this.mCameraOps, 1));
            this.mActions.add(new ActionTakePicture(this.mCameraOps, 0));
        } else {
            this.mActions.add(new ActionCaptureJpegFlash(this.mCameraOps, 1));
        }
        doActionStepByCameraOps();
    }

    private void addActionsForAutoFocus() {
        CamLog.m3d(CameraConstants.TAG, "addActionsForAutoFocus");
        this.mActionType = 2;
        this.mActions.add(new ActionAutoFocus(this.mCameraOps, 0));
    }

    private void addActionsForAutoFocusFlash(boolean isReprocess) {
        CamLog.m3d(CameraConstants.TAG, "addActionsForAutoFocusFlash");
        this.mActionType = 3;
        this.mActions.add(new ActionPrecapture(this.mCameraOps, 0));
        this.mActions.add(new ActionLockExposure(this.mCameraOps, 0));
        if (isReprocess) {
            this.mActions.add(new ActionCaptureYuvFlash(this.mCameraOps, 1));
            this.mActions.add(new ActionTakePicture(this.mCameraOps, 0));
            return;
        }
        this.mActions.add(new ActionCaptureJpegFlash(this.mCameraOps, 1));
    }

    private void createActionsForCancelFocus() {
        CamLog.m3d(CameraConstants.TAG, "createActionsForCancelFocus");
        this.mActionType = 4;
        this.mActionStep = 0;
        this.mActions = new ArrayList();
        this.mActions.add(new ActionCancelFocus(this.mCameraOps, 0));
        doActionStepByCameraOps();
    }

    private void doActionStepByCaptureResult() {
        if (!isValidAction()) {
            resetActionState();
        } else if (((Action) this.mActions.get(this.mActionStep)).isRunnableByCaptureResult()) {
            ((Action) this.mActions.get(this.mActionStep)).run();
        }
    }

    private void doActionStepByCameraOps() {
        if (isValidAction()) {
            ((Action) this.mActions.get(this.mActionStep)).run();
        } else {
            resetActionState();
        }
    }

    private boolean isValidAction() {
        return (this.mActions == null || this.mActionType == 0 || this.mActionStep >= this.mActions.size()) ? false : true;
    }

    public void resetActionState() {
        CamLog.m3d(CameraConstants.TAG, "resetActionState");
        this.mActions = null;
        this.mActionType = 0;
        this.mActionStep = 0;
    }

    public boolean isAction(int actionType) {
        return isValidAction() && this.mActionType == actionType;
    }

    public boolean isFlashAction() {
        return isValidAction() && (this.mActionType == 1 || this.mActionType == 3);
    }

    public boolean isFlashPreCapturing() {
        return isFlashAction() && ((this.mActions.get(this.mActionStep) instanceof ActionPrecapture) || (this.mActions.get(this.mActionStep) instanceof ActionLockExposure));
    }
}
