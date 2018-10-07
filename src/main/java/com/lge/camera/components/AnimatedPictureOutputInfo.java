package com.lge.camera.components;

public class AnimatedPictureOutputInfo {
    private int mVideoBitrate;
    private int mVideoFrameRate;
    private int mVideoHeight;
    private int mVideoIFrameInterval;
    private int mVideoOrientationHint;
    private int mVideoTimeLength;
    private int mVideoWidth;

    public AnimatedPictureOutputInfo(int width, int height, int fps, int bitrate, int iFrame, int timeLength, int orientationHint) {
        this.mVideoWidth = width;
        this.mVideoHeight = height;
        this.mVideoFrameRate = fps;
        this.mVideoBitrate = bitrate;
        this.mVideoIFrameInterval = iFrame;
        this.mVideoTimeLength = timeLength;
        this.mVideoOrientationHint = orientationHint;
    }

    public int getVideoWidth() {
        return this.mVideoWidth;
    }

    public void setVideoWidth(int mVideoWidth) {
        this.mVideoWidth = mVideoWidth;
    }

    public int getVideoHeight() {
        return this.mVideoHeight;
    }

    public void setVideoHeight(int mVideoHeight) {
        this.mVideoHeight = mVideoHeight;
    }

    public int getVideoBitrate() {
        return this.mVideoBitrate;
    }

    public void setVideoBitrate(int mVideoBitrate) {
        this.mVideoBitrate = mVideoBitrate;
    }

    public int getVideoFrameRate() {
        return this.mVideoFrameRate;
    }

    public void setVideoFrameRate(int mVideoFrameRate) {
        this.mVideoFrameRate = mVideoFrameRate;
    }

    public int getVideoIFrameInterval() {
        return this.mVideoIFrameInterval;
    }

    public void setVideoIFrameInterval(int mVideoIFrameInterval) {
        this.mVideoIFrameInterval = mVideoIFrameInterval;
    }

    public int getVideoTimeLength() {
        return this.mVideoTimeLength;
    }

    public void setVideoTimeLength(int mVideoTimeLength) {
        this.mVideoTimeLength = mVideoTimeLength;
    }

    public int getVideoOrientationHint() {
        return this.mVideoOrientationHint;
    }

    public void setVideoOrientationHint(int mVideoOrientationHint) {
        this.mVideoOrientationHint = mVideoOrientationHint;
    }
}
