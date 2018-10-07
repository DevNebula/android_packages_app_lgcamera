package com.lge.camera.util;

import android.graphics.Bitmap;
import android.net.Uri;

public class UpdateThumbnailRequest {
    public int mExifDegree = 0;
    public String mFilePath = null;
    public boolean mIsBurstShot = false;
    public Bitmap mThumbBitmap = null;
    public boolean mUpdateThumbnail = true;
    public Uri mUri = null;

    public UpdateThumbnailRequest(Bitmap bmp, String fileName, Uri uri, int exifDegree, boolean update, boolean isBurst) {
        this.mThumbBitmap = bmp;
        this.mFilePath = fileName;
        this.mUri = uri;
        this.mExifDegree = exifDegree;
        this.mUpdateThumbnail = update;
        this.mIsBurstShot = isBurst;
    }

    public void unbind() {
        this.mThumbBitmap = null;
        this.mFilePath = null;
        this.mUri = null;
        this.mExifDegree = 0;
    }
}
