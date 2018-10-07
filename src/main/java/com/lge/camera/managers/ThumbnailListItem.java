package com.lge.camera.managers;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import com.lge.camera.constants.CameraConstants;

public class ThumbnailListItem {
    /* renamed from: id */
    public long f33id = 0;
    public String mBurstId;
    public boolean mIsBurstShot = false;
    public boolean mIsImage = false;
    public String mMediaType = "image";
    public int mModeColumn = 0;
    public int mOri;
    public Uri mUri;
    public String path;

    public void setBurstId(String burstId) {
        this.mBurstId = burstId;
    }

    public void setIsBurstShot(boolean isBurstShot) {
        this.mIsBurstShot = isBurstShot;
    }

    public void setModeColumn(int type) {
        this.mModeColumn = type;
    }

    public void setUri(Uri uri) {
        this.mUri = uri;
    }

    public void setMediaType(String mediaType) {
        this.mMediaType = mediaType;
    }

    public void unbind() {
        this.mUri = null;
    }

    public int hashCode() {
        return this.mUri != null ? this.mUri.hashCode() : 0;
    }

    public boolean equals(Object item) {
        if (item == null || !(item instanceof ThumbnailListItem)) {
            return false;
        }
        return this.mUri.equals(((ThumbnailListItem) item).mUri);
    }

    public static ThumbnailListItem fromCursor(Cursor cursor) {
        ThumbnailListItem item = new ThumbnailListItem();
        item.updateFromCursor(cursor);
        return item;
    }

    public void updateFromCursor(Cursor cursor) {
        this.f33id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
        this.mMediaType = cursor.getString(cursor.getColumnIndexOrThrow("mime_type"));
        this.mIsImage = this.mMediaType.contains("image");
        this.mUri = ContentUris.withAppendedId(this.mIsImage ? Media.EXTERNAL_CONTENT_URI : Video.Media.EXTERNAL_CONTENT_URI, this.f33id);
        this.mModeColumn = cursor.getInt(cursor.getColumnIndexOrThrow(CameraConstants.MODE_COLUMN));
        this.mOri = cursor.getInt(cursor.getColumnIndexOrThrow(CameraConstants.ORIENTATION));
        this.mBurstId = cursor.getString(cursor.getColumnIndexOrThrow("burst_id"));
        this.path = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
        if (this.mBurstId != null && !this.mBurstId.contains(".") && this.mIsImage && cursor.getInt(cursor.getColumnIndexOrThrow("num")) > 1) {
            this.mIsBurstShot = true;
        }
    }
}
