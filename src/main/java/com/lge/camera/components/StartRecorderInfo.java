package com.lge.camera.components;

public class StartRecorderInfo {
    public int mDegree;
    public String mFileName;
    public boolean mNeedRun = true;
    public String mOutFilePath;
    public int mPurpose;
    public int mStorageType;
    public int mVideoBitrate;
    public int mVideoFlipType;
    public double mVideoFps;
    public String mVideoSize;

    public StartRecorderInfo(int storageType, String fileName, String outFilePath, String videoSize, int purpose, double videoFps, int videoBitrate, int videoFlipType) {
        this.mStorageType = storageType;
        this.mFileName = fileName;
        this.mOutFilePath = outFilePath;
        this.mVideoSize = videoSize;
        this.mPurpose = purpose;
        this.mVideoFps = videoFps;
        this.mVideoBitrate = videoBitrate;
        this.mVideoFlipType = videoFlipType;
    }
}
