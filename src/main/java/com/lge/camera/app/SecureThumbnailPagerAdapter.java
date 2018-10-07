package com.lge.camera.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.MultimediaProperties;
import com.lge.camera.managers.ThumbnailListItem;
import com.lge.camera.managers.ThumbnailListPagerAdapter;
import com.lge.camera.managers.ThumbnailListPagerAdapter.ThumbnailListPagerListener;
import com.lge.camera.util.AppControlUtilBase;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;

public class SecureThumbnailPagerAdapter extends ThumbnailListPagerAdapter {
    SecureThumbnailAdapter mAdapter;

    public SecureThumbnailPagerAdapter(Context context, Activity activity, ThumbnailListPagerListener listener, SecureThumbnailAdapter adapter, ThumbnailHelper mThumbnailHelper) {
        super(context, activity, listener, null, mThumbnailHelper);
        this.mAdapter = adapter;
    }

    public int getCount() {
        if (this.mAdapter == null) {
            return 0;
        }
        return this.mAdapter.getCount();
    }

    protected ThumbnailListItem getItem(int position) {
        return this.mAdapter.getItem(position);
    }

    protected boolean checkAdapter(int position) {
        if (this.mAdapter == null || this.mAdapter.getCount() < position + 1) {
            return false;
        }
        return true;
    }

    protected void playVideo(Uri uri) {
        CamLog.m3d(CameraConstants.TAG, "mPlayButtonListener clicked.");
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(uri, MultimediaProperties.VIDEO_MIME_TYPE);
        intent.putExtra("mimeType", MultimediaProperties.VIDEO_MIME_TYPE);
        intent.putExtra("full_screen_only", true);
        intent.putExtra("android.intent.extra.finishOnCompletion", true);
        intent.putExtra("secure_mode", true);
        intent.setClassName(CameraConstants.PACKAGE_VIDEOPLAYER, "com.lge.videoplayer.player.SecureMediaView");
        intent.putExtra("QslideSupported", false);
        intent.putExtra("secure_time", this.mSecureTime);
        if (!(this.mBucketIdSelection == null || "".equals(this.mBucketIdSelection.trim()))) {
            intent.putExtra("bucket_ids", this.mBucketIdSelection);
            intent.putExtra("sort", "datetaken DESC,_id DESC");
        }
        if (this.mContext != null && FileUtil.isCNasContents(uri, (Context) this.mContext.get())) {
            intent.setPackage(CameraConstants.PACKAGE_VIDEOPLAYER);
        }
        try {
            AppControlUtilBase.setLaunchingVideo(true);
            ((Activity) this.mActivity.get()).startActivity(intent);
        } catch (ActivityNotFoundException e) {
            CamLog.m11w(CameraConstants.TAG, "ActivityNotFoundException");
        }
    }
}
