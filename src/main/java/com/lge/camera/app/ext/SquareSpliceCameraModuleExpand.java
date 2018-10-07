package com.lge.camera.app.ext;

import android.net.Uri;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.file.FileManager;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;

public class SquareSpliceCameraModuleExpand extends SquareSpliceCameraModule {
    public SquareSpliceCameraModuleExpand(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        if (!FunctionProperties.isSupportedCollageRecording()) {
            if (this.mSplicePostViewManager == null || !(this.mSplicePostViewManager == null || this.mSplicePostViewManager.isPostviewVisible())) {
                this.mCaptureButtonManager.changeButtonByMode(12);
            }
        }
    }

    public void onShutterLargeButtonClicked() {
        CamLog.m3d(CameraConstants.TAG, "shutter large clicked.");
        switch (this.mCaptureButtonManager.getShutterButtonMode(4)) {
            case 12:
                CamLog.m3d(CameraConstants.TAG, "CAM_SHUTTER_ONLY");
                onCameraShutterButtonClicked();
                return;
            default:
                super.onShutterLargeButtonClicked();
                return;
        }
    }

    public int getShutterButtonType() {
        return FunctionProperties.isSupportedCollageRecording() ? 3 : 4;
    }

    public void notifyNewMedia(final Uri uri, final boolean updateThumbnail) {
        CamLog.m3d(CameraConstants.TAG, "splice view notifyNewMedia : URI = " + uri);
        new Thread() {
            public void run() {
                FileManager.broadcastNewMedia(SquareSpliceCameraModuleExpand.this.mGet.getAppContext(), uri);
                SquareSpliceCameraModuleExpand.this.mReviewThumbnailManager.setLaunchGalleryAvailable(true);
                if (SquareSpliceCameraModuleExpand.this.mGet.isLGUOEMCameraIntent()) {
                    SquareSpliceCameraModuleExpand.this.mGet.postOnUiThread(SquareSpliceCameraModuleExpand.this.mChangeToAttachModule);
                } else if (updateThumbnail && SquareSpliceCameraModuleExpand.this.mReviewThumbnailManager != null) {
                    SquareSpliceCameraModuleExpand.this.mReviewThumbnailManager.doAfterCaptureProcess(uri, false);
                }
                if (SquareSpliceCameraModuleExpand.this.mIsCollageShared && !SquareSpliceCameraModuleExpand.this.mIsSaveBtnClicked) {
                    SquareSpliceCameraModuleExpand.this.launchQuickClipSharedApp();
                }
                SquareSpliceCameraModuleExpand.this.mSplicePostViewManager.resetSaveButtonClickedFlag();
                AudioUtil.setAudioFocus(SquareSpliceCameraModuleExpand.this.mGet.getActivity(), false);
                SquareSpliceCameraModuleExpand.this.mIsCollageShared = false;
                SquareSpliceCameraModuleExpand.this.mSpliceViewImageImportManager.setPrePostBitmap(null);
            }
        }.start();
    }

    public boolean isCollageProgressing() {
        return getMVState() == 4;
    }

    public void setReverseState(boolean isReverse) {
        if (this.mSpliceViewImageImportManager != null) {
            this.mSpliceViewImageImportManager.setReverseState(isReverse);
        }
    }

    public boolean getReverseState() {
        if (this.mSpliceViewImageImportManager != null) {
            return this.mSpliceViewImageImportManager.getReverseState();
        }
        return false;
    }

    public boolean isIndicatorSupported(int indicatorId) {
        switch (indicatorId) {
            case C0088R.id.indicator_item_cheese_shutter_or_timer:
                if (isRecordingState()) {
                    return false;
                }
                return true;
            default:
                return false;
        }
    }
}
