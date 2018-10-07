package com.lge.camera.app;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore.Files;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class ThumbnailLoader extends AsyncTaskLoader<Cursor> {
    private String bucket;
    Context context;
    private long mCurrentTimeSec = 0;
    private String mLimit = "";

    public ThumbnailLoader(Context context, String bkt, long time) {
        super(context);
        this.context = context;
        this.bucket = bkt;
        this.mCurrentTimeSec = time;
    }

    public Cursor loadInBackground() {
        Uri baseImageUri = Files.getContentUri(CameraConstants.STORAGE_NAME_EXTERNAL);
        String[] imageProjection = new String[]{"_id", "_data", "mime_type", CameraConstants.ORIENTATION, "datetaken", "_display_name", CameraConstants.MODE_COLUMN, "burst_id", "count(*) as num"};
        StringBuilder mSel = new StringBuilder();
        mSel.append("(mime_type='video/mp4'");
        mSel.append(" OR mime_type='video/3gpp'");
        mSel.append(" OR mime_type='image/gif'");
        mSel.append(" OR mime_type='image/x-adobe-dng'");
        mSel.append(" OR mime_type='image/jpeg')");
        if (this.mCurrentTimeSec != 0) {
            mSel.append(" AND date_added>" + Long.toString(this.mCurrentTimeSec));
        }
        mSel.append(" AND (" + this.bucket + ")) GROUP BY burst_id , bucket_id HAVING MIN(" + "datetaken");
        String order = "datetaken DESC,_id DESC" + this.mLimit;
        long startTime = SystemClock.uptimeMillis();
        Cursor cursor = this.context.getContentResolver().query(baseImageUri, imageProjection, mSel.toString(), null, order);
        CamLog.m3d(CameraConstants.TAG, "[Tile] query time : " + (SystemClock.uptimeMillis() - startTime));
        return cursor;
    }

    public Cursor getCursorForLimitCount(int limitCount) {
        this.mLimit = " limit " + limitCount;
        Cursor cursor = loadInBackground();
        this.mLimit = "";
        return cursor;
    }

    protected void onReset() {
        super.onReset();
        onStopLoading();
    }

    public void setCurrentTimeSec(long mCurrentTimeSec) {
        this.mCurrentTimeSec = mCurrentTimeSec;
    }

    public Cursor getCursorWithIds(String[] ids) {
        if (ids == null || ids.length == 0) {
            return null;
        }
        Uri baseImageUri = Files.getContentUri(CameraConstants.STORAGE_NAME_EXTERNAL);
        String[] imageProjection = new String[]{"_id"};
        StringBuilder mSel = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            if (i == 0) {
                mSel.append("_id=?");
            } else {
                mSel.append(" OR _id=?");
            }
        }
        String order = "datetaken DESC,_id DESC" + this.mLimit;
        long startTime = SystemClock.uptimeMillis();
        Cursor cursor = this.context.getContentResolver().query(baseImageUri, imageProjection, mSel.toString(), ids, order);
        CamLog.m3d(CameraConstants.TAG, "[Tile] query time : " + (SystemClock.uptimeMillis() - startTime));
        return cursor;
    }
}
