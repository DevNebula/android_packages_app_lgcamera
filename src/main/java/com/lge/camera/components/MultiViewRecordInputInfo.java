package com.lge.camera.components;

import java.util.ArrayList;

public class MultiViewRecordInputInfo {
    public static final int IMAGE = 1;
    public static final int VIDEO = 2;
    ArrayList<String> mFilePath;
    private int[] mFileType;
    private int mImageCount = 0;
    ArrayList<Integer> mRecordingDegrees;
    private int mTotalCount = 0;

    public MultiViewRecordInputInfo(ArrayList<String> filePath, ArrayList<Integer> degree, int[] fileType, int totalCount, int imageCount) {
        this.mFilePath = filePath;
        this.mFileType = fileType;
        this.mRecordingDegrees = degree;
        this.mTotalCount = totalCount;
        this.mImageCount = imageCount;
    }

    public ArrayList<String> getFilePath() {
        return this.mFilePath;
    }

    public int[] getFileType() {
        return this.mFileType;
    }

    public ArrayList<Integer> getRecordingDegrees() {
        return this.mRecordingDegrees;
    }

    public int getTotalCount() {
        return this.mTotalCount;
    }

    public int getImageCount() {
        return this.mImageCount;
    }
}
