package com.lge.panorama;

import android.util.Size;

public final class BufferCalculator {
    private final float ANGLE_MARGIN = 5.0f;
    private final boolean ARM_COMPEN = true;
    private int mHorizontalPanoramaLength = 0;
    private int mMaximumAngleCoverage = 0;
    private int mVerticalPanoramaLength = 0;

    public void setBufferCalculator(float horiFov, float vertFov, Size frameSize, float sensorWidth, float sensorHeight, float focalLength, float focusDistance, int maximumAngleCoverage, float armDistance, boolean is360mode) {
        float horizontalFOV = calculateFov(sensorWidth, focalLength);
        float ratioVerticalFOV = (((float) frameSize.getHeight()) * horizontalFOV) / ((float) frameSize.getWidth());
        float armDistanceRatio = 1.12f;
        if (is360mode) {
            armDistanceRatio = 1.3f;
            this.mHorizontalPanoramaLength = (int) (((((float) maximumAngleCoverage) - (ratioVerticalFOV / 2.0f)) / ratioVerticalFOV) * ((float) frameSize.getHeight()));
            this.mVerticalPanoramaLength = (int) (((((float) maximumAngleCoverage) - (horizontalFOV / 2.0f)) / horizontalFOV) * ((float) frameSize.getWidth()));
        } else {
            this.mHorizontalPanoramaLength = (int) ((((((float) maximumAngleCoverage) - ratioVerticalFOV) - 5.0f) / ratioVerticalFOV) * ((float) frameSize.getHeight()));
            this.mVerticalPanoramaLength = (int) ((((((float) maximumAngleCoverage) - horizontalFOV) - 5.0f) / horizontalFOV) * ((float) frameSize.getWidth()));
        }
        this.mMaximumAngleCoverage = maximumAngleCoverage;
        this.mHorizontalPanoramaLength = (int) (((float) this.mHorizontalPanoramaLength) * armDistanceRatio);
        this.mVerticalPanoramaLength = (int) (((float) this.mVerticalPanoramaLength) * armDistanceRatio);
    }

    private float calculateFov(float sensorSize, float focalLength) {
        return (float) ((2.0d * Math.atan2((double) (sensorSize / 2.0f), (double) focalLength)) * 57.29577951308232d);
    }

    public float calculateRealFovFromFrameRatio(float sensorSize, float focalLength, float frame_width, float frame_height) {
        float frame_ratio = 0.0f;
        if (frame_width > 0.0f) {
            frame_ratio = frame_height / frame_width;
        }
        return (float) ((2.0d * Math.atan2((double) ((frame_ratio * sensorSize) / 2.0f), (double) focalLength)) * 57.29577951308232d);
    }

    public int getVerticalPanoramaMaxLength() {
        return this.mVerticalPanoramaLength;
    }

    public int getHorizontalPanoramaMaxLength() {
        return this.mHorizontalPanoramaLength;
    }

    public int getmMaximumRotationAngleCoverage() {
        return this.mMaximumAngleCoverage;
    }
}
