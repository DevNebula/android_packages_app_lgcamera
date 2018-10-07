package com.lge.camera.postview;

import android.net.Uri;
import java.util.ArrayList;

public class PostviewParameters {
    private int mContentType = 0;
    private Uri mSavedUri = null;
    private Uri mTimeCatchOneShotFile = null;
    private ArrayList<Uri> mUriList = new ArrayList();

    public ArrayList<Uri> getUriList() {
        return this.mUriList;
    }

    public void setUriList(ArrayList<Uri> mUriList) {
        this.mUriList = mUriList;
    }

    public void clearUriList() {
        if (this.mUriList != null) {
            this.mUriList.clear();
            this.mUriList = null;
        }
    }

    public int getContentType() {
        return this.mContentType;
    }

    public void setContentType(int type) {
        this.mContentType = type;
    }

    public void setTimeCatchOneShotUri(Uri uri) {
        this.mTimeCatchOneShotFile = uri;
    }

    public Uri getTimeCatchOneShotFileUri() {
        return this.mTimeCatchOneShotFile;
    }

    public void setSavedUri(Uri uri) {
        this.mSavedUri = uri;
    }

    public Uri getSavedUri() {
        return this.mSavedUri;
    }
}
