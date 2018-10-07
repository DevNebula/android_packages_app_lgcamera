package com.lge.camera.components;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.Utils;

public class ThumbnailButton extends RotateImageView implements OnLongClickListener, OnClickListener, OnRemoveHandler {
    private int mCircleRadius = 0;
    private int mDefaultImgResId = C0088R.drawable.shutter_icon_gallery_default;
    private ModuleInterface mGet = null;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = null;
    private boolean mIsLastBitmapNull = true;
    private boolean mIsThumbnailExtracting = false;
    private Object mLockThumbs = new Object();
    private Resources mResources = null;
    private Bitmap mThumb = null;
    private ThumbnailTransitionDrawable mThumbTransition = null;
    private Drawable[] mThumbs = null;

    private class ThumbnailInfomation {
        public boolean checkSourceRef;
        public int height;
        public Bitmap roundBmp;
        public boolean useTransition;
        public int width;

        ThumbnailInfomation(boolean useTransition, Bitmap roundBmp, boolean checkSourceRef, int width, int height) {
            this.useTransition = useTransition;
            this.roundBmp = roundBmp;
            this.checkSourceRef = checkSourceRef;
            this.width = width;
            this.height = height;
        }
    }

    public ThumbnailButton(Context context) {
        super(context);
        setDrawingCacheEnabled(true);
        buildDrawingCache();
        initThumbnailButton();
    }

    public ThumbnailButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDrawingCacheEnabled(true);
        buildDrawingCache();
        initThumbnailButton();
        initThumbnailThread();
    }

    public ThumbnailButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setDrawingCacheEnabled(true);
        buildDrawingCache();
        initThumbnailButton();
        initThumbnailThread();
    }

    private void initThumbnailButton() {
        this.mCircleRadius = (int) (((float) Utils.getPx(getContext(), C0088R.dimen.review_thumbnail.size)) * 0.95f);
    }

    protected boolean checkRecordingStateValidate(int checkType) {
        int i;
        boolean invalid = false;
        int cameraState = this.mGet.getCameraState();
        if ((checkType & 64) != 0) {
            i = (cameraState == 5 || cameraState == 8) ? 1 : 0;
            invalid = false | i;
        }
        if ((checkType & 128) != 0) {
            if (cameraState == 6 || cameraState == 7) {
                i = 1;
            } else {
                i = 0;
            }
            invalid |= i;
        }
        if (invalid) {
            return false;
        }
        return true;
    }

    private void initThumbnailThread() {
        this.mResources = getResources();
        this.mHandlerThread = new HandlerThread("ThumbnailThread");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                ThumbnailInfomation info = msg.obj;
                int width = info.width;
                int height = info.height;
                Bitmap roundBmp = info.roundBmp;
                boolean chkSource = info.checkSourceRef;
                boolean mUseTrans = info.useTransition;
                ThumbnailButton.this.mThumb = BitmapManagingUtil.getRoundedImage(roundBmp.copy(Config.ARGB_8888, true), width, height, ThumbnailButton.this.mCircleRadius);
                if (ThumbnailButton.this.mThumb != null) {
                    Bitmap defaultImage;
                    if (chkSource || !mUseTrans) {
                        synchronized (ThumbnailButton.this.mLockThumbs) {
                            if (ThumbnailButton.this.mThumbs == null) {
                                ThumbnailButton.this.mThumbs = new Drawable[2];
                            }
                            defaultImage = BitmapManagingUtil.getRoundedImage(BitmapFactory.decodeResource(ThumbnailButton.this.mResources, ThumbnailButton.this.mDefaultImgResId), width, height, ThumbnailButton.this.mCircleRadius);
                            if (defaultImage == null) {
                                return;
                            }
                            ThumbnailButton.this.mThumbs[0] = new BitmapDrawable(ThumbnailButton.this.mResources, defaultImage);
                            ThumbnailButton.this.mThumbs[1] = new BitmapDrawable(ThumbnailButton.this.mResources, ThumbnailButton.this.mThumb);
                            ThumbnailButton.this.mGet.runOnUiThread(new HandlerRunnable(ThumbnailButton.this) {
                                public void handleRun() {
                                    if (ThumbnailButton.this.mThumbs != null) {
                                        ThumbnailButton.this.setImageDrawable(ThumbnailButton.this.mThumbs[1]);
                                        ThumbnailButton.this.startTransition(0);
                                    }
                                }
                            });
                            return;
                        }
                    }
                    synchronized (ThumbnailButton.this.mLockThumbs) {
                        if (ThumbnailButton.this.mThumbs == null) {
                            defaultImage = BitmapManagingUtil.getRoundedImage(BitmapFactory.decodeResource(ThumbnailButton.this.mResources, ThumbnailButton.this.mDefaultImgResId), width, height, ThumbnailButton.this.mCircleRadius);
                            if (defaultImage == null) {
                                return;
                            }
                            ThumbnailButton.this.mThumbs = new Drawable[2];
                            ThumbnailButton.this.mThumbs[0] = new BitmapDrawable(ThumbnailButton.this.mResources, defaultImage);
                            ThumbnailButton.this.mThumbs[1] = new BitmapDrawable(ThumbnailButton.this.mResources, ThumbnailButton.this.mThumb);
                        } else {
                            ThumbnailButton.this.mThumbs[0] = ThumbnailButton.this.mThumbs[1];
                            ThumbnailButton.this.mThumbs[1] = new BitmapDrawable(ThumbnailButton.this.mResources, ThumbnailButton.this.mThumb);
                        }
                        ThumbnailButton.this.mThumbTransition = new ThumbnailTransitionDrawable(ThumbnailButton.this.mThumbs);
                        ThumbnailButton.this.mGet.runOnUiThread(new HandlerRunnable(ThumbnailButton.this) {
                            public void handleRun() {
                                ThumbnailButton.this.mThumbTransition = new ThumbnailTransitionDrawable(ThumbnailButton.this.mThumbs);
                                ThumbnailButton.this.setImageDrawable(ThumbnailButton.this.mThumbTransition);
                                if (ThumbnailButton.this.mGet.checkModuleValidate(192)) {
                                    ThumbnailButton.this.startTransition(50);
                                }
                            }
                        });
                    }
                }
            }
        };
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }

    public void setModuleIF(ModuleInterface get) {
        this.mGet = get;
    }

    /* JADX WARNING: Missing block: B:17:?, code:
            return;
     */
    public void resizeArray(int r9, int r10) {
        /*
        r8 = this;
        r3 = r8.mLockThumbs;
        monitor-enter(r3);
        r2 = r8.mThumbs;	 Catch:{ all -> 0x007b }
        if (r2 != 0) goto L_0x0009;
    L_0x0007:
        monitor-exit(r3);	 Catch:{ all -> 0x007b }
    L_0x0008:
        return;
    L_0x0009:
        r2 = r8.mThumbs;	 Catch:{ all -> 0x007b }
        r4 = 0;
        r2 = r2[r4];	 Catch:{ all -> 0x007b }
        if (r2 == 0) goto L_0x0041;
    L_0x0010:
        r2 = android.graphics.Bitmap.Config.ARGB_8888;	 Catch:{ all -> 0x007b }
        r0 = android.graphics.Bitmap.createBitmap(r9, r10, r2);	 Catch:{ all -> 0x007b }
        r1 = new android.graphics.Canvas;	 Catch:{ all -> 0x007b }
        r1.<init>(r0);	 Catch:{ all -> 0x007b }
        r2 = r8.mThumbs;	 Catch:{ all -> 0x007b }
        r4 = 0;
        r2 = r2[r4];	 Catch:{ all -> 0x007b }
        r4 = 0;
        r5 = 0;
        r6 = r1.getWidth();	 Catch:{ all -> 0x007b }
        r7 = r1.getHeight();	 Catch:{ all -> 0x007b }
        r2.setBounds(r4, r5, r6, r7);	 Catch:{ all -> 0x007b }
        r2 = r8.mThumbs;	 Catch:{ all -> 0x007b }
        r4 = 0;
        r2 = r2[r4];	 Catch:{ all -> 0x007b }
        r2.draw(r1);	 Catch:{ all -> 0x007b }
        r2 = new android.graphics.drawable.BitmapDrawable;	 Catch:{ all -> 0x007b }
        r4 = r8.getResources();	 Catch:{ all -> 0x007b }
        r2.<init>(r4, r0);	 Catch:{ all -> 0x007b }
        r8.setImageDrawable(r2);	 Catch:{ all -> 0x007b }
    L_0x0041:
        r2 = r8.mThumbs;	 Catch:{ all -> 0x007b }
        r4 = 1;
        r2 = r2[r4];	 Catch:{ all -> 0x007b }
        if (r2 == 0) goto L_0x0079;
    L_0x0048:
        r2 = android.graphics.Bitmap.Config.ARGB_8888;	 Catch:{ all -> 0x007b }
        r0 = android.graphics.Bitmap.createBitmap(r9, r10, r2);	 Catch:{ all -> 0x007b }
        r1 = new android.graphics.Canvas;	 Catch:{ all -> 0x007b }
        r1.<init>(r0);	 Catch:{ all -> 0x007b }
        r2 = r8.mThumbs;	 Catch:{ all -> 0x007b }
        r4 = 1;
        r2 = r2[r4];	 Catch:{ all -> 0x007b }
        r4 = 0;
        r5 = 0;
        r6 = r1.getWidth();	 Catch:{ all -> 0x007b }
        r7 = r1.getHeight();	 Catch:{ all -> 0x007b }
        r2.setBounds(r4, r5, r6, r7);	 Catch:{ all -> 0x007b }
        r2 = r8.mThumbs;	 Catch:{ all -> 0x007b }
        r4 = 1;
        r2 = r2[r4];	 Catch:{ all -> 0x007b }
        r2.draw(r1);	 Catch:{ all -> 0x007b }
        r2 = new android.graphics.drawable.BitmapDrawable;	 Catch:{ all -> 0x007b }
        r4 = r8.getResources();	 Catch:{ all -> 0x007b }
        r2.<init>(r4, r0);	 Catch:{ all -> 0x007b }
        r8.setImageDrawable(r2);	 Catch:{ all -> 0x007b }
    L_0x0079:
        monitor-exit(r3);	 Catch:{ all -> 0x007b }
        goto L_0x0008;
    L_0x007b:
        r2 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x007b }
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.components.ThumbnailButton.resizeArray(int, int):void");
    }

    public void setEnabled(boolean enabled) {
        if (this.mHandler == null) {
            CamLog.m3d(CameraConstants.TAG, "mHandler is null");
            return;
        }
        super.setEnabled(enabled);
        if (this.mIsLastBitmapNull && this.mDefaultImgResId != C0088R.drawable.shutter_icon_gallery_lock) {
            this.mDefaultImgResId = enabled ? C0088R.drawable.shutter_icon_gallery_detail : C0088R.drawable.shutter_icon_gallery_default;
            LayoutParams params = getLayoutParams();
            this.mHandler.obtainMessage(0, new ThumbnailInfomation(false, BitmapFactory.decodeResource(this.mResources, this.mDefaultImgResId), true, (params.width - getPaddingStart()) - getPaddingEnd(), (params.height - getPaddingTop()) - getPaddingBottom())).sendToTarget();
        }
    }

    public void setSecureDefaultImage(boolean isSecureCamera) {
        int i = isSecureCamera ? C0088R.drawable.shutter_icon_gallery_lock : isEnabled() ? C0088R.drawable.shutter_icon_gallery_detail : C0088R.drawable.shutter_icon_gallery_default;
        this.mDefaultImgResId = i;
    }

    public void setData(Bitmap source, boolean useTransition, boolean isEmptyImg) {
        setData(source, useTransition, isEmptyImg, false);
    }

    public void setData(Bitmap source, boolean useTransition, boolean isEmptyImg, boolean notUseSecureImg) {
        LayoutParams params = getLayoutParams();
        setupTransition(source, useTransition, (params.width - getPaddingStart()) - getPaddingEnd(), (params.height - getPaddingTop()) - getPaddingBottom(), isEmptyImg, notUseSecureImg);
    }

    private void setupTransition(Bitmap source, boolean useTransition, int width, int height, boolean isEmptyImg, boolean notUseSecureImg) {
        if (this.mHandler == null) {
            CamLog.m3d(CameraConstants.TAG, "mHandler is null");
            return;
        }
        boolean z;
        Bitmap roundBmp;
        boolean z2;
        CamLog.m3d(CameraConstants.TAG, "setupTransition : source = " + source + ", width = " + width + ", height = " + height);
        if (notUseSecureImg || !SecureImageUtil.useSecureLockImage()) {
            z = false;
        } else {
            z = true;
        }
        setSecureDefaultImage(z);
        if (source == null || source.isRecycled()) {
            int drawableId = this.mDefaultImgResId;
            if (isEmptyImg) {
                drawableId = C0088R.drawable.shutter_icon_gallery_none;
            }
            roundBmp = BitmapFactory.decodeResource(this.mResources, drawableId);
            if (isEmptyImg) {
                z = false;
            } else {
                z = true;
            }
            this.mIsLastBitmapNull = z;
        } else {
            this.mIsThumbnailExtracting = true;
            roundBmp = ThumbnailUtils.extractThumbnail(source, width, height);
            this.mIsLastBitmapNull = false;
            this.mIsThumbnailExtracting = false;
        }
        if (source == null) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.mHandler.obtainMessage(0, new ThumbnailInfomation(useTransition, roundBmp, z2, width, height)).sendToTarget();
    }

    public boolean isThumbnailExtracting() {
        return this.mIsThumbnailExtracting;
    }

    public void startTransition(int time) {
        if (this.mThumbTransition != null) {
            this.mThumbTransition.startTransition(time);
        }
    }

    public void onClick(View v) {
    }

    public boolean onLongClick(View v) {
        return false;
    }

    public void close() {
        if (this.mThumbTransition != null) {
            this.mThumbTransition.unbind();
            this.mThumbTransition = null;
        }
        if (this.mThumb != null) {
            this.mThumb.recycle();
            this.mThumb = null;
        }
        synchronized (this.mLockThumbs) {
            if (this.mThumbs != null) {
                for (int i = 0; i < this.mThumbs.length; i++) {
                    BitmapManagingUtil.recycleBitmapDrawable(this.mThumbs[i]);
                    this.mThumbs[i] = null;
                }
            }
            this.mThumbs = null;
        }
        setOnClickListener(null);
        setOnLongClickListener(null);
        setOnTouchListener(null);
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages(null);
            this.mHandler.getLooper().quit();
            this.mHandler = null;
        }
        if (this.mHandlerThread != null) {
            this.mHandlerThread = null;
        }
    }
}
