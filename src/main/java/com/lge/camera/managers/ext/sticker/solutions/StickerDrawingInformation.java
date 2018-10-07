package com.lge.camera.managers.ext.sticker.solutions;

import com.lge.camera.util.CamLog;

public class StickerDrawingInformation {
    private static final String TAG = "StickerDrawingInformation";
    private int mCameraID;
    private boolean mInfoChanged = false;
    private int mOrientation;
    private int[] mPreviewSize;
    private int[] mVideoSize;

    public StickerDrawingInformation(int[] priviewSize, int[] videoSize, int orientation, int cameraID) {
        this.mPreviewSize = priviewSize;
        this.mVideoSize = videoSize;
        this.mOrientation = orientation;
        this.mCameraID = cameraID;
        this.mInfoChanged = true;
        CamLog.m5e(TAG, " create : " + toString());
    }

    public void setData(int[] priviewSize, int[] videoSize, int orientation, int cameraID) {
        if (!(this.mPreviewSize[0] == priviewSize[0] && this.mPreviewSize[1] == priviewSize[1])) {
            this.mInfoChanged = true;
        }
        this.mPreviewSize = priviewSize;
        if (!(this.mVideoSize[0] == videoSize[0] && this.mVideoSize[1] == videoSize[1])) {
            this.mInfoChanged = true;
        }
        this.mVideoSize = videoSize;
        if (orientation != this.mOrientation) {
            this.mInfoChanged = true;
        }
        this.mOrientation = orientation;
        if (cameraID != this.mCameraID) {
            this.mInfoChanged = true;
        }
        this.mCameraID = cameraID;
        CamLog.m3d(TAG, " setData mInfoChanged : " + this.mInfoChanged);
        CamLog.m3d(TAG, " setData : " + toString());
    }

    public int[] getPreviewSize() {
        return this.mPreviewSize;
    }

    public int getPreviewWidth() {
        return this.mPreviewSize[0];
    }

    public int getPreviewHeight() {
        return this.mPreviewSize[1];
    }

    public int getVideoWidth() {
        return this.mVideoSize[0];
    }

    public int getVideoHeight() {
        return this.mVideoSize[1];
    }

    public int[] getVideoSize() {
        return this.mVideoSize;
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    public int getCameraID() {
        return this.mCameraID;
    }

    public boolean isInfoChanged() {
        return this.mInfoChanged;
    }

    public void setIsInfoChangedFalse() {
        this.mInfoChanged = false;
    }

    public boolean isFrontCamera() {
        return this.mCameraID == 1;
    }

    public String toString() {
        if (this.mPreviewSize == null || this.mVideoSize == null) {
            return "NOT INSTANCIATED!!";
        }
        return String.format("preview : %d x %d || video : %d x %d || orientation : %d || cameraID = %d", new Object[]{Integer.valueOf(this.mPreviewSize[0]), Integer.valueOf(this.mPreviewSize[1]), Integer.valueOf(this.mVideoSize[0]), Integer.valueOf(this.mVideoSize[1]), Integer.valueOf(this.mOrientation), Integer.valueOf(this.mCameraID)});
    }

    public boolean isValid() {
        if (this.mPreviewSize == null || this.mPreviewSize[0] <= 0 || this.mPreviewSize[1] <= 0 || this.mVideoSize == null || this.mVideoSize[0] <= 0 || this.mVideoSize[1] <= 0) {
            return false;
        }
        return true;
    }
}
