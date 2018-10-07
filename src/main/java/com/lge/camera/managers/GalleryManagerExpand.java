package com.lge.camera.managers;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class GalleryManagerExpand extends GalleryManager {
    protected LinkedBlockingQueue<CaptureAnimationRequest> mAnimationQueue;

    class CaptureAnimationRequest {
        public Bitmap mCurBitmap;
        public Bitmap mPreBitmap;

        public CaptureAnimationRequest(Bitmap cur, Bitmap pre) {
            this.mCurBitmap = cur;
            this.mPreBitmap = pre;
        }

        public void unbind(boolean doRecycle) {
            if (doRecycle) {
                if (!(this.mCurBitmap == null || this.mCurBitmap.isRecycled())) {
                    this.mCurBitmap.recycle();
                }
                if (!(this.mPreBitmap == null || this.mPreBitmap.isRecycled())) {
                    this.mPreBitmap.recycle();
                }
            }
            this.mCurBitmap = null;
            this.mPreBitmap = null;
        }
    }

    public GalleryManagerExpand(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void initLayout() {
        super.initLayout();
        this.mAnimationQueue = new LinkedBlockingQueue();
    }

    public void releaseViews() {
        super.releaseViews();
        if (this.mAnimationQueue != null) {
            for (int i = 0; i < this.mAnimationQueue.size(); i++) {
                ((CaptureAnimationRequest) this.mAnimationQueue.poll()).unbind(true);
            }
            this.mAnimationQueue.clear();
            this.mAnimationQueue = null;
        }
    }

    public int getSquareSnapshotState() {
        return this.mSquareSnapState;
    }

    public boolean isDeleteProcessing() {
        return this.mIsDeleting;
    }

    public boolean isMainAccessView() {
        if (this.mMainAccessView == null || this.mMainAccessView.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    public void readyCaptureAnimation(Bitmap bmp, boolean isGIFImage) {
        if (this.mSquareSnapshotAnimationFrontView != null && this.mSquareSnapshotAnimationRearView != null && this.mGalleryItemList != null) {
            if (this.mIsDeleting) {
                CamLog.m11w(CameraConstants.TAG, "[Cell] delete animation is working, so reset the flag");
                this.mGalleryViewPager.setEnableScroll(true);
                this.mIsDeleting = false;
            }
            Bitmap result = null;
            Bitmap bitmap;
            if (this.mMainAccessView.getVisibility() != 0) {
                bitmap = getThumbnailBitmap(((SquareSnapGalleryItem) this.mGalleryItemList.get(this.mCurrentIndex)).mUri, ((SquareSnapGalleryItem) this.mGalleryItemList.get(this.mCurrentIndex)).mType, true);
                if (bitmap != null) {
                    result = bitmap;
                } else {
                    return;
                }
            } else if (this.mGalleryLayout != null) {
                this.mGalleryLayout.setDrawingCacheEnabled(true);
                bitmap = this.mGalleryLayout.getDrawingCache();
                if (bitmap == null) {
                    this.mGalleryLayout.setDrawingCacheEnabled(false);
                    return;
                }
                result = Bitmap.createBitmap(bitmap);
                this.mSquareSnapshotAnimationRearView.setImageBitmap(result);
                this.mGalleryLayout.setDrawingCacheEnabled(false);
            }
            this.mAnimationQueue.add(new CaptureAnimationRequest(bmp, result));
            CamLog.m7i(CameraConstants.TAG, "[Cell] readyCaptureAnimation");
        }
    }

    public void doCaptureEffect(boolean isFirstShot) {
        CamLog.m3d(CameraConstants.TAG, "[Cell] doCaptureEffect");
        if (this.mGet.checkModuleValidate(192) && this.mAnimationQueue.size() != 0) {
            if (this.mIsDeleting) {
                CamLog.m11w(CameraConstants.TAG, "[Cell] delete animation is working, so reset the flag");
                this.mGalleryViewPager.setEnableScroll(true);
                this.mIsDeleting = false;
            }
            if (this.mSquareSnapshotAnimationFrontView != null && this.mSquareSnapshotAnimationRearView != null) {
                for (int i = 0; i < this.mAnimationQueue.size() && this.mAnimationQueue.size() != 1; i++) {
                    ((CaptureAnimationRequest) this.mAnimationQueue.poll()).unbind(true);
                }
                final CaptureAnimationRequest request = (CaptureAnimationRequest) this.mAnimationQueue.poll();
                this.mSquareSnapshotAnimationFrontView.setImageBitmap(request.mCurBitmap);
                this.mSquareSnapshotAnimationRearView.setImageBitmap(request.mPreBitmap);
                this.mSquareSnapshotAnimationRearView.setVisibility(0);
                this.mSquareSnapshotAnimationFrontView.setVisibility(0);
                if (!isFirstShot) {
                    AnimationUtil.startSnapshotGalleryViewAnimation(this.mSquareSnapshotAnimationRearView, getAppContext(), getOrientationDegree(), null);
                }
                AnimationUtil.startSnapshotCaptureThumbnailAnimation(this.mSquareSnapshotAnimationFrontView, getAppContext(), getOrientationDegree(), isFirstShot, new AnimationListener() {
                    public void onAnimationStart(Animation arg0) {
                    }

                    public void onAnimationRepeat(Animation arg0) {
                    }

                    public void onAnimationEnd(Animation arg0) {
                        GalleryManagerExpand.this.removeAnimationViews();
                        request.unbind(false);
                    }
                });
            }
        }
    }

    public void addUriToGalleryItemList(Uri uri, Bitmap bitmap, int type) {
        if (this.mGalleryItemList != null && this.mListener != null) {
            this.mGalleryItemList.add(new SquareSnapGalleryItem(uri, bitmap, type));
        }
    }

    public void loadTheFirstOfSavedItems(Uri uri, Bitmap bitmap, int type) {
        int i = 0;
        CamLog.m3d(CameraConstants.TAG, "[cell]loadSavedFirstItem ");
        if (this.mGalleryItemList != null && this.mListener != null) {
            if (this.mThreadHandler != null) {
                this.mThreadHandler.removeMessages(1);
            }
            this.mIsCanceledForNewItem = false;
            this.mGalleryItemList.add(0, new SquareSnapGalleryItem(uri, bitmap, type));
            this.mPreviousIndex = this.mCurrentIndex;
            this.mCurrentIndex = 0;
            this.mListener.onGalleryBitmapLoaded(false);
            this.mGalleryPagerAdapter.notifyDataSetChanged();
            this.mGalleryViewPager.setCurrentItem(0, false);
            if (this.mThreadHandler != null) {
                Message msg = new Message();
                msg.what = 1;
                msg.arg1 = 0;
                if (this.mGalleryItemList != null) {
                    i = this.mGalleryItemList.size();
                }
                msg.arg2 = i;
                msg.obj = uri;
                this.mThreadHandler.removeMessages(1);
                this.mThreadHandler.sendMessage(msg);
            }
        }
    }

    public void loadTheRestOfSavedItems(ArrayList<Uri> uriList, Bitmap bitmap, int alreadyLoaded, int type) {
        if (this.mGalleryItemList != null) {
            CamLog.m3d(CameraConstants.TAG, "[cell]onDefaultItemAdded " + this.mGalleryItemList.size() + " uriList size " + uriList.size() + " alreadyLoaded " + alreadyLoaded);
            for (int i = 0; i < uriList.size(); i++) {
                int galleryIndex = i + alreadyLoaded;
                this.mGalleryItemList.add(galleryIndex, new SquareSnapGalleryItem((Uri) uriList.get(i), null, type));
            }
        }
    }

    public void changeGalleryItemList(HashMap<String, Integer> newListMap, String[] oldUriList, boolean forceRecentIndex) {
        int i;
        String uriStr;
        int i2 = 0;
        CamLog.m3d(CameraConstants.TAG, "[cell]changeGalleryItemList mCurrentIndex " + this.mCurrentIndex);
        int index = -1;
        for (i = this.mCurrentIndex; i < oldUriList.length; i++) {
            uriStr = oldUriList[i];
            if (newListMap.containsKey(uriStr)) {
                index = ((Integer) newListMap.get(uriStr)).intValue();
                break;
            }
        }
        if (index == -1) {
            for (i = this.mCurrentIndex - 1; i >= 0; i--) {
                uriStr = oldUriList[i];
                if (newListMap.containsKey(uriStr)) {
                    index = ((Integer) newListMap.get(uriStr)).intValue();
                    break;
                }
            }
        }
        this.mPreviousIndex = this.mCurrentIndex;
        if (forceRecentIndex) {
            index = 0;
        }
        this.mCurrentIndex = index;
        this.mGalleryPagerAdapter.notifyDataSetChanged();
        this.mGalleryViewPager.setCurrentItem(this.mCurrentIndex, false);
        if (this.mThreadHandler != null) {
            Message msg = new Message();
            msg.what = 1;
            msg.arg1 = this.mCurrentIndex;
            if (this.mGalleryItemList != null) {
                i2 = this.mGalleryItemList.size();
            }
            msg.arg2 = i2;
            this.mThreadHandler.removeMessages(1);
            this.mThreadHandler.sendMessage(msg);
        }
    }

    public void resetBitmapList() {
        CamLog.m3d(CameraConstants.TAG, "resetBitmapList");
        cancelOriginalImageMaking();
        for (int i = 0; i < this.mGalleryItemList.size(); i++) {
            ((SquareSnapGalleryItem) this.mGalleryItemList.get(i)).unbind();
        }
        this.mGalleryItemList.clear();
    }

    public boolean isAnimationViewShowing() {
        return (this.mSquareSnapshotAnimationFrontView != null && this.mSquareSnapshotAnimationFrontView.getVisibility() == 0) || (this.mSquareSnapshotAnimationRearView != null && this.mSquareSnapshotAnimationRearView.getVisibility() == 0);
    }
}
