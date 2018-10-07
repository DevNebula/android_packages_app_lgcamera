package com.lge.camera.file;

import android.location.Location;

public class ImageRegisterRequest {
    public long mDateTaken = 0;
    public int mDegree = 0;
    public String mDirectory = null;
    public int[] mExifSize = null;
    public String mFileName = null;
    public boolean mIsBurstShot = true;
    public Location mLocation = null;
    public boolean mUpdateThumbnail = false;

    public void unbind() {
        this.mDirectory = null;
        this.mFileName = null;
        this.mDateTaken = 0;
        this.mLocation = null;
        this.mDegree = 0;
        this.mExifSize = null;
        this.mIsBurstShot = true;
        this.mUpdateThumbnail = false;
    }
}
