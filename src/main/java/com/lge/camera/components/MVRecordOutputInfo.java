package com.lge.camera.components;

public class MVRecordOutputInfo {
    private final int mAudioBitRate;
    private final int mAudioChannelCount;
    private final String mAudioMimeType;
    private final int mAudioSampleRate;
    private final String mOutputDir;
    private final String mOutputFileExt;
    private final String mOutputFileName;
    private final int mVideoBitRate;
    private final int mVideoFrameRate;
    private final int mVideoHeight;
    private final int mVideoIFrameInterval;
    private final String mVideoMimeType;
    private final int mVideoOrientationHint;
    private final int mVideoWidth;

    public MVRecordOutputInfo(String vMimeType, int vWidth, int vHeight, int vBitRate, int vFrameRate, int vIframeInterval, int vOrientationHint, String aMimeType, int aSampleRate, int aChannelCount, int aBitRate, String outputDir, String outputFileName, String outputFileExt) {
        this.mVideoMimeType = vMimeType;
        this.mVideoWidth = vWidth;
        this.mVideoHeight = vHeight;
        this.mVideoBitRate = vBitRate;
        this.mVideoFrameRate = vFrameRate;
        this.mVideoIFrameInterval = vIframeInterval;
        this.mVideoOrientationHint = vOrientationHint;
        this.mAudioMimeType = aMimeType;
        this.mAudioSampleRate = aSampleRate;
        this.mAudioChannelCount = aChannelCount;
        this.mAudioBitRate = aBitRate;
        this.mOutputDir = outputDir;
        this.mOutputFileName = outputFileName;
        this.mOutputFileExt = outputFileExt;
    }

    public MVRecordOutputInfo(int vOrientationHint) {
        this.mVideoMimeType = null;
        this.mVideoBitRate = 0;
        this.mVideoWidth = 0;
        this.mVideoHeight = 0;
        this.mVideoFrameRate = 0;
        this.mVideoIFrameInterval = 0;
        this.mVideoOrientationHint = vOrientationHint;
        this.mAudioMimeType = null;
        this.mAudioSampleRate = 0;
        this.mAudioChannelCount = 0;
        this.mAudioBitRate = 0;
        this.mOutputDir = null;
        this.mOutputFileName = null;
        this.mOutputFileExt = null;
    }

    public String getVideoMimeType() {
        return this.mVideoMimeType;
    }

    public int getVideoBitRate() {
        return this.mVideoBitRate;
    }

    public int getVideoWidth() {
        return this.mVideoWidth;
    }

    public int getVideoHeight() {
        return this.mVideoHeight;
    }

    public int getVideoFrameRate() {
        return this.mVideoFrameRate;
    }

    public int getVideoIFrameInterval() {
        return this.mVideoIFrameInterval;
    }

    public int getVideoOrientationHint() {
        return this.mVideoOrientationHint;
    }

    public String getAudioMimeType() {
        return this.mAudioMimeType;
    }

    public int getAudioSampleRate() {
        return this.mAudioSampleRate;
    }

    public int getAudioChannelCount() {
        return this.mAudioChannelCount;
    }

    public int getAudioBitRate() {
        return this.mAudioBitRate;
    }

    public String getOutputDir() {
        return this.mOutputDir;
    }

    public String getOutputFileName() {
        return this.mOutputFileName;
    }

    public String getOutputFileExt() {
        return this.mOutputFileExt;
    }
}
