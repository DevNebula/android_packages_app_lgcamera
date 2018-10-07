package com.lge.camera.app.ext;

import com.lge.camera.app.ActivityBridge;
import com.lge.camera.components.MultiViewLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.CameraSecondHolder;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;

public class MultiGLView extends MultiGLViewBase {
    public MultiGLView(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void setLCDSizeForMultiLayout() {
        int[] lcdSize = Utils.getLCDsize(this.mGet.getAppContext(), true);
        MultiViewLayout.setLCDSize(lcdSize[1], lcdSize[0]);
    }

    protected void setNumOfCameraForLayout(int num, boolean removeExtCamId) {
        if (this.mMultiViewLayoutList != null) {
            ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).setNumOfCameras(num, removeExtCamId);
        }
    }

    protected void doOnStartPreviewResult(boolean result) {
        CamLog.m3d(CameraConstants.TAG, "doOnStartPreviewResult result = " + result);
        if (this.mMultiviewFrame != null) {
            this.mMultiviewFrame.restoreVertex(this.mGet.getOrientationDegree());
        }
        if (this.mIsCameraAlreadyOpened) {
            if (this.mCameraDevice != null) {
                this.mCameraDevice.startPreview();
            }
            if (CameraSecondHolder.isSecondCameraOpened()) {
                CameraSecondHolder.subinstance().startPreview();
            }
        } else {
            startPreviewFirst();
            startPreviewSecond();
            setCameraState(1);
        }
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.setShutterButtonEnable(true, 3);
        }
    }

    protected void setupPreview(CameraParameters params) {
        CamLog.m3d(CameraConstants.TAG, "Multiview- setupPreview");
        if (checkForSetupPreview()) {
            setupMultiviewPreview();
        }
    }

    protected void setMultiLayoutDegree(int degree) {
        CamLog.m3d(CameraConstants.TAG, "-c1- setMultiLayoutDegree degree = " + degree);
        if (this.mMultiViewLayoutList != null && this.mMultiviewFrame != null) {
            if (!checkModuleValidate(128) || this.mIsMultiviewRecording) {
                CamLog.m3d(CameraConstants.TAG, "-c1- do not change degree in recording state");
            } else {
                this.mMultiviewFrame.changeLayout(this.mMultiviewFrame.getCurrentPreviewMode(), false);
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onOrientationChanged(int orientation, boolean isFirst) {
        super.onOrientationChanged(orientation, isFirst);
        if (!isFirst) {
            CamLog.m3d(CameraConstants.TAG, "-c1- onOrientationChanged");
            if (!checkModuleValidate(128) || this.mIsMultiviewRecording) {
                CamLog.m3d(CameraConstants.TAG, "-c1- do not change degree in recording state");
                return;
            }
            setMultiLayoutDegree(orientation);
            if (this.mMultiviewFrame != null) {
                this.mMultiviewFrame.restoreVertex(this.mGet.getOrientationDegree());
            }
        }
    }

    public void onStop() {
        super.onStop();
        CamLog.m3d(CameraConstants.TAG, "-multiview- onStop");
        this.mDialogManager.onDismissRotateDialog();
    }
}
