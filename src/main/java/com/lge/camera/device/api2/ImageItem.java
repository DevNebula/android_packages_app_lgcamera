package com.lge.camera.device.api2;

import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;

public class ImageItem {
    private Image mExtraImage = null;
    private Image mImage = null;
    private boolean mIsFocused = false;
    private TotalCaptureResult mMetadata = null;

    public Image getImage() {
        return this.mImage;
    }

    public void setImage(Image image) {
        if (this.mImage != null) {
            this.mImage.close();
        }
        this.mImage = image;
    }

    public TotalCaptureResult getMetadata() {
        return this.mMetadata;
    }

    public boolean getFocusState() {
        return this.mIsFocused;
    }

    public void setMetadata(TotalCaptureResult metadata, boolean isFocused) {
        this.mMetadata = metadata;
        this.mIsFocused = isFocused;
    }

    public void closeImage() {
        if (this.mImage != null) {
            this.mImage.close();
        }
        this.mImage = null;
        if (this.mExtraImage != null) {
            this.mExtraImage.close();
        }
        this.mExtraImage = null;
    }

    public void closeMeta() {
        this.mMetadata = null;
        this.mIsFocused = false;
    }

    public void close() {
        closeImage();
        closeMeta();
    }

    public boolean isValid() {
        if (this.mImage == null || this.mMetadata == null) {
            return false;
        }
        return true;
    }
}
