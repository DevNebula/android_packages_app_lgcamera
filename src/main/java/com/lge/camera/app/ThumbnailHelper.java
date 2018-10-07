package com.lge.camera.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CancellationSignal;
import android.provider.MediaStore.Images.Thumbnails;
import android.provider.MediaStore.Video;
import android.widget.ImageView;
import com.lge.camera.app.ProviderExecutor.Preemptable;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.ThumbnailListItem;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;

public class ThumbnailHelper {
    private static Context sContext;
    private ThumbnailCache mCropCache = LGCameraApplication.getCropCache(sContext);
    private ThumbnailCache mRotateCache = LGCameraApplication.getRotateCache(sContext);

    public static class LoaderTask extends AsyncTask<Uri, Void, Bitmap> implements Preemptable {
        private final ImageView mIconThumb;
        private ThumbnailListItem mItem;
        public final int mPriority;
        private final CancellationSignal mSignal = new CancellationSignal();

        public LoaderTask(ThumbnailListItem item, ImageView iconThumb, int priority) {
            this.mItem = item;
            this.mIconThumb = iconThumb;
            this.mPriority = priority;
        }

        public void preempt() {
            cancel(false);
            this.mSignal.cancel();
        }

        protected Bitmap doInBackground(Uri... params) {
            if (isCancelled()) {
                return null;
            }
            CamLog.m7i(CameraConstants.TAG, "[Tile] execute doInBackground..mUri : " + this.mItem.mUri.toString() + " mPriority : " + this.mPriority);
            ThumbnailCache cropThumbs = LGCameraApplication.getCropCache(ThumbnailHelper.sContext);
            ThumbnailCache rotateThumbs = LGCameraApplication.getRotateCache(ThumbnailHelper.sContext);
            Bitmap cropBitmap = (Bitmap) cropThumbs.get(this.mItem.mUri);
            Bitmap rotateBitmap = (Bitmap) rotateThumbs.get(this.mItem.mUri);
            if (cropBitmap == null || rotateBitmap == null) {
                Bitmap orgBitmap;
                if (this.mItem.mIsImage) {
                    orgBitmap = Thumbnails.getThumbnail(ThumbnailHelper.sContext.getContentResolver(), this.mItem.f33id, 0, this.mPriority == 50 ? 3 : 1, null);
                } else {
                    orgBitmap = Video.Thumbnails.getThumbnail(ThumbnailHelper.sContext.getContentResolver(), this.mItem.f33id, 0, this.mPriority == 50 ? 3 : 1, null);
                }
                rotateBitmap = BitmapManagingUtil.getRotatedImage(orgBitmap, this.mItem.mOri, false);
                if (this.mPriority == 50) {
                    if (rotateBitmap != null) {
                        cropBitmap = Bitmap.createScaledBitmap(rotateBitmap, 320, 320, true);
                        if (rotateBitmap != null) {
                            rotateBitmap.recycle();
                            rotateBitmap = null;
                        }
                    }
                } else if (!(rotateBitmap == null || this.mItem == null || this.mItem.mUri == null)) {
                    cropBitmap = BitmapManagingUtil.cropBitmap(ThumbnailHelper.sContext, rotateBitmap);
                    rotateThumbs.put(this.mItem.mUri, rotateBitmap);
                    cropThumbs.put(this.mItem.mUri, cropBitmap);
                }
            }
            if (this.mPriority != 70) {
                return cropBitmap;
            }
            if (rotateBitmap == null || rotateBitmap.isRecycled()) {
                return null;
            }
            return rotateBitmap;
        }

        protected void onPostExecute(Bitmap result) {
            if (this.mIconThumb != null && this.mIconThumb.getTag() == this && result != null && !result.isRecycled()) {
                this.mIconThumb.setTag(null);
                this.mIconThumb.setImageBitmap(result);
                this.mIconThumb.setAlpha(0.0f);
                this.mIconThumb.animate().alpha(1.0f).start();
            }
        }
    }

    public ThumbnailHelper(Context context, Activity activity) {
        sContext = context;
    }

    public void stopLoading(ImageView icon) {
        LoaderTask oldTask = (LoaderTask) icon.getTag();
        if (oldTask != null) {
            oldTask.preempt();
            icon.setTag(null);
        }
    }

    public void loadThumbnail(ThumbnailListItem item, ImageView iconThumb, int priority) {
        if (item != null) {
            Bitmap cropResult = (Bitmap) this.mCropCache.get(item.mUri);
            Bitmap rotateResult = (Bitmap) this.mRotateCache.get(item.mUri);
            if (cropResult == null || rotateResult == null || cropResult.isRecycled() || rotateResult.isRecycled()) {
                if (iconThumb == null) {
                    ProviderExecutor.forAuthority(item.mUri.getAuthority()).execute(new LoaderTask(item, iconThumb, priority), new Uri[0]);
                    return;
                }
                iconThumb.setImageDrawable(null);
                LoaderTask task = new LoaderTask(item, iconThumb, priority);
                iconThumb.setTag(task);
                ProviderExecutor.forAuthority(item.mUri.getAuthority()).execute(task, new Uri[0]);
            } else if (iconThumb == null) {
            } else {
                if (priority == 70) {
                    iconThumb.setImageBitmap(rotateResult);
                } else {
                    iconThumb.setImageBitmap(cropResult);
                }
            }
        }
    }
}
