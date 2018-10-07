package com.lge.camera.file;

public class SupportedExif {
    public boolean mExposureBiasTag = false;
    public boolean mFlashTag = false;
    public boolean mFocalLengthTag = false;
    public boolean mGPSHeadingTag = false;
    public boolean mMeteringTag = false;
    public boolean mUpdateThumbnail = false;
    public boolean mWhiteBalanceTag = false;
    public boolean mZoomRatioTag = false;

    public SupportedExif(boolean focalLengthTag, boolean flashTag, boolean whiteBalanceTag, boolean meteringTag, boolean zoomRatioTag, boolean exposureBiasTag, boolean updateThumbnail, boolean gpsHeadingTag) {
        this.mFocalLengthTag = focalLengthTag;
        this.mFlashTag = flashTag;
        this.mWhiteBalanceTag = whiteBalanceTag;
        this.mMeteringTag = meteringTag;
        this.mZoomRatioTag = zoomRatioTag;
        this.mExposureBiasTag = exposureBiasTag;
        this.mUpdateThumbnail = updateThumbnail;
        this.mGPSHeadingTag = gpsHeadingTag;
    }
}
