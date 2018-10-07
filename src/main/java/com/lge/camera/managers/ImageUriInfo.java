package com.lge.camera.managers;

import android.content.ContentResolver;
import android.location.Location;
import com.lge.camera.file.ExifInterface;

public class ImageUriInfo {
    public ContentResolver mCR;
    public long mDateTaken;
    public int mDegree;
    public String mDirectory;
    public ExifInterface mExif;
    public String mFileName;
    public boolean mIsBurstShot;
    public Location mLocation;

    public ImageUriInfo(ContentResolver cr, String directory, String fileName, long dateTaken, Location location, int degree, ExifInterface exif, boolean isBurstshot) {
        this.mCR = cr;
        this.mDirectory = directory;
        this.mFileName = fileName;
        this.mDateTaken = dateTaken;
        this.mLocation = location;
        this.mDegree = degree;
        this.mExif = exif;
        this.mIsBurstShot = isBurstshot;
    }
}
