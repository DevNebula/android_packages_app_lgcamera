package com.lge.camera.device;

import android.graphics.Rect;
import android.hardware.camera2.params.MeteringRectangle;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class OutfocusCaptureResult {
    static final String SEPERATE = ",";
    private byte[] mBluredImage;
    private int mErrorType;
    private MeteringRectangle[] mFocusRegions;
    private byte[] mMeta;
    private byte[] mOriginImage;
    private Rect mSensorActiveSize;
    private int mSolutionType;

    public int getErrorType() {
        return this.mErrorType;
    }

    public void setErrorType(int errorType) {
        this.mErrorType = errorType;
    }

    public int getSolutionType() {
        return this.mSolutionType;
    }

    public void setSolutionType(int solutionType) {
        this.mSolutionType = solutionType;
    }

    public String getFocusRegions() {
        int i = 0;
        if (this.mFocusRegions == null || this.mFocusRegions.length == 0) {
            return "";
        }
        MeteringRectangle bestRect = this.mFocusRegions[0];
        MeteringRectangle[] meteringRectangleArr = this.mFocusRegions;
        int length = meteringRectangleArr.length;
        while (i < length) {
            MeteringRectangle rect = meteringRectangleArr[i];
            if (bestRect.getMeteringWeight() < rect.getMeteringWeight()) {
                bestRect = rect;
            }
            i++;
        }
        return bestRect.getX() + SEPERATE + bestRect.getY() + SEPERATE + bestRect.getWidth() + SEPERATE + bestRect.getHeight();
    }

    public void setFocusRegions(MeteringRectangle[] focusRegions) {
        this.mFocusRegions = focusRegions;
    }

    public byte[] getBluredImage() {
        return this.mBluredImage;
    }

    public void setBluredImage(byte[] bluredImage) {
        this.mBluredImage = bluredImage;
    }

    public byte[] getOriginImage() {
        return this.mOriginImage;
    }

    public int getOriginalImageSize() {
        return this.mOriginImage == null ? 0 : this.mOriginImage.length;
    }

    public void setOriginImage(byte[] originImage) {
        this.mOriginImage = originImage;
    }

    public byte[] getMeta() {
        return this.mMeta;
    }

    public int getMetaSize() {
        return this.mMeta == null ? 0 : this.mMeta.length;
    }

    public void setMeta(byte[] meta) {
        this.mMeta = meta;
    }

    public boolean hasVaildImage() {
        return (this.mOriginImage == null && this.mBluredImage == null) ? false : true;
    }

    public void setSensorActiveSize(Rect activeArraySize) {
        this.mSensorActiveSize = activeArraySize;
    }

    public String getSensorActiveSize() {
        if (this.mSensorActiveSize == null) {
            return "";
        }
        return this.mSensorActiveSize.left + SEPERATE + this.mSensorActiveSize.top + SEPERATE + this.mSensorActiveSize.width() + SEPERATE + this.mSensorActiveSize.height();
    }

    public void printDebugLog() {
        CamLog.m7i(CameraConstants.TAG, "Outfocus errorType " + this.mErrorType + " solutionType " + this.mSolutionType + " blured " + this.mBluredImage + " origin " + this.mOriginImage);
    }
}
