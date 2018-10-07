package com.lge.camera.managers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.api2.Camera2Util;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class IntervalShotManager extends IntervalShotManagerIF {
    private final float INTERVAL_SHOT_GUIDE_MARGIN_BOTTOM = 0.15f;
    private final float INTERVAL_SHOT_GUIDE_MARGIN_BOTTOM_4X3 = 0.18f;
    protected TextView mGuideTextView = null;
    protected RotateLayout mGuideTextViewLayout = null;
    protected int mIntervalShotState = 0;
    protected View mIntervalshotView = null;
    protected View mThumbView = null;
    protected RotateImageButton mThumbnail01 = null;
    protected RotateImageButton mThumbnail02 = null;
    protected RotateImageButton mThumbnail03 = null;
    protected RotateImageButton mThumbnail04 = null;
    protected ArrayList<RotateImageButton> mThumbnailList = null;
    protected int mWaitingOrder = 0;
    protected Timer mWaitingTimer = null;

    /* renamed from: com.lge.camera.managers.IntervalShotManager$2 */
    class C10402 implements AnimationListener {
        C10402() {
        }

        public void onAnimationStart(Animation arg0) {
        }

        public void onAnimationRepeat(Animation arg0) {
        }

        public void onAnimationEnd(Animation arg0) {
            IntervalShotManager.this.mGuideTextView.setVisibility(4);
        }
    }

    public IntervalShotManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        initLayout();
    }

    private void initLayout() {
        if (this.mIntervalshotView == null) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            this.mIntervalshotView = this.mGet.inflateView(C0088R.layout.intervalshot);
            if (this.mIntervalshotView != null) {
                if (!(vg == null || this.mIntervalshotView == null)) {
                    vg.addView(this.mIntervalshotView, 0);
                }
                this.mThumbView = this.mIntervalshotView.findViewById(C0088R.id.intervalshot_thumbnail_view);
                if (this.mThumbView != null) {
                    LayoutParams thumbnailParam = (LayoutParams) this.mThumbView.getLayoutParams();
                    thumbnailParam.topMargin += RatioCalcUtil.getNotchDisplayHeight(getAppContext());
                }
                this.mThumbnail01 = (RotateImageButton) this.mIntervalshotView.findViewById(C0088R.id.intervalshot_thumbnail_item_view_1);
                this.mThumbnail02 = (RotateImageButton) this.mIntervalshotView.findViewById(C0088R.id.intervalshot_thumbnail_item_view_2);
                this.mThumbnail03 = (RotateImageButton) this.mIntervalshotView.findViewById(C0088R.id.intervalshot_thumbnail_item_view_3);
                this.mThumbnail04 = (RotateImageButton) this.mIntervalshotView.findViewById(C0088R.id.intervalshot_thumbnail_item_view_4);
                this.mGuideTextView = (TextView) this.mIntervalshotView.findViewById(C0088R.id.intervalshot_text);
                this.mGuideTextViewLayout = (RotateLayout) this.mIntervalshotView.findViewById(C0088R.id.intervalshot_text_layout);
                if (this.mThumbnailList == null) {
                    this.mThumbnailList = new ArrayList();
                    this.mThumbnailList.clear();
                }
                if (this.mThumbnailList != null) {
                    this.mThumbnailList.add(this.mThumbnail01);
                    this.mThumbnailList.add(this.mThumbnail02);
                    this.mThumbnailList.add(this.mThumbnail03);
                    this.mThumbnailList.add(this.mThumbnail04);
                }
                setDegree(this.mGet.getOrientationDegree(), false);
            }
        }
    }

    public void onPauseBefore() {
        stopWaitingUI();
        releaseLayout();
        super.onPauseBefore();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void releaseLayout() {
        if (this.mThumbnailList != null) {
            for (int i = 0; i < this.mThumbnailList.size(); i++) {
                if (this.mThumbnailList.get(i) != null) {
                    ((RotateImageButton) this.mThumbnailList.get(i)).setImageBitmap(null);
                }
            }
        }
        this.mThumbnail01 = null;
        this.mThumbnail02 = null;
        this.mThumbnail03 = null;
        this.mThumbnail04 = null;
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (!(vg == null || this.mIntervalshotView == null)) {
            vg.removeView(this.mIntervalshotView);
            this.mIntervalshotView = null;
        }
        if (this.mThumbnailList != null) {
            this.mThumbnailList.clear();
            this.mThumbnailList = null;
        }
        this.mThumbView = null;
        this.mGuideTextView = null;
        this.mGuideTextViewLayout = null;
    }

    public void showIntervalshotLayout() {
        if (this.mThumbView != null) {
            this.mThumbView.setVisibility(0);
        }
    }

    public int getIntervalshotVisibiity() {
        if (this.mThumbView != null) {
            return this.mThumbView.getVisibility();
        }
        return 4;
    }

    public void hideIntervalshotLayout() {
        if (this.mThumbnailList != null) {
            for (int i = 0; i < this.mThumbnailList.size(); i++) {
                ((RotateImageButton) this.mThumbnailList.get(i)).setImageBitmap(null);
            }
        }
        if (this.mThumbView != null) {
            this.mThumbView.setVisibility(4);
        }
    }

    public void updateThumbnail(int index, byte[] data, boolean setFlip) {
        if (this.mThumbnailList != null) {
            ExifInterface exif = Exif.readExif(data);
            int exifDegree = Exif.getOrientation(exif);
            Bitmap thumbBmp = null;
            if (exif != null) {
                thumbBmp = exif.getThumbnailBitmap();
                if (thumbBmp == null) {
                    return;
                }
                if (setFlip) {
                    byte[] exifThumbnail = exif.getThumbnail();
                    if (exifThumbnail != null) {
                        thumbBmp = BitmapManagingUtil.makeFlipBitmap(exifThumbnail, true, exifDegree);
                    }
                }
            }
            Bitmap reviewBmp = null;
            if (thumbBmp != null) {
                reviewBmp = BitmapManagingUtil.getRotatedImage(thumbBmp, exifDegree, false);
            }
            ((RotateImageButton) this.mThumbnailList.get(index)).setImageBitmap(reviewBmp);
        }
    }

    public void updateThumbnail(int index, byte[] data, int degree, float ratio) {
        if (this.mThumbnailList != null) {
            Size thumbnailSize;
            if (FunctionProperties.getSupportedHal() == 2) {
                thumbnailSize = Camera2Util.getThumbnailSize(ratio);
            } else {
                int[] previewSizeInt = Utils.sizeStringToArray(this.mGet.getCurrentSelectedPreviewSize());
                previewSizeInt = Exif.calcThumbnailSize(previewSizeInt[0], previewSizeInt[1]);
                thumbnailSize = new Size(previewSizeInt[0], previewSizeInt[0]);
            }
            if (thumbnailSize == null) {
                CamLog.m7i(CameraConstants.TAG, "thumbnailSize is null");
                return;
            }
            Bitmap thumbBmp = null;
            Bitmap reviewBmp = null;
            try {
                thumbBmp = Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), thumbnailSize.getWidth(), thumbnailSize.getHeight(), true);
            } catch (OutOfMemoryError oom) {
                oom.printStackTrace();
            }
            if (thumbBmp != null) {
                reviewBmp = BitmapManagingUtil.getRotatedImage(thumbBmp, degree, false);
            }
            ((RotateImageButton) this.mThumbnailList.get(index)).setImageBitmap(reviewBmp);
        }
    }

    public void setDegree(int degree, boolean animation) {
        super.setDegree(degree, animation);
        if (this.mIntervalshotView != null) {
            if (this.mThumbnailList != null) {
                for (int i = 0; i < this.mThumbnailList.size(); i++) {
                    if (this.mThumbnailList.get(i) != null) {
                        ((RotateImageButton) this.mThumbnailList.get(i)).setDegree(degree, false);
                    }
                }
            }
            if (this.mGuideTextViewLayout != null) {
                this.mGuideTextViewLayout.setAngle(degree);
            }
        }
    }

    public void startWatingUI(final int index) {
        if (index >= 0 && this.mIntervalshotView != null && this.mThumbnailList != null) {
            if (index >= this.mThumbnailList.size()) {
                stopWaitingUI();
                return;
            }
            stopWaitingUI();
            TimerTask waitingUI = new TimerTask() {
                public void run() {
                    IntervalShotManager.this.mGet.runOnUiThread(new HandlerRunnable(IntervalShotManager.this) {
                        public void handleRun() {
                            if (IntervalShotManager.this.mThumbnailList != null && IntervalShotManager.this.mThumbnailList.get(index) != null) {
                                IntervalShotManager intervalShotManager = IntervalShotManager.this;
                                intervalShotManager.mWaitingOrder++;
                                if (IntervalShotManager.this.mWaitingOrder % 2 == 0) {
                                    ((RotateImageButton) IntervalShotManager.this.mThumbnailList.get(index)).setBackgroundResource(C0088R.drawable.camera_selfie_frame_full);
                                } else {
                                    ((RotateImageButton) IntervalShotManager.this.mThumbnailList.get(index)).setBackgroundResource(C0088R.drawable.camera_selfie_frame);
                                }
                            }
                        }
                    });
                }
            };
            this.mWaitingTimer = new Timer("timer_waitingui_check");
            this.mWaitingTimer.scheduleAtFixedRate(waitingUI, 0, 400);
        }
    }

    public void stopWaitingUI() {
        if (this.mWaitingTimer != null) {
            this.mWaitingTimer.cancel();
            this.mWaitingTimer.purge();
            this.mWaitingTimer = null;
        }
        this.mWaitingOrder = 0;
        if (this.mThumbnailList != null) {
            for (int i = 0; i < this.mThumbnailList.size(); i++) {
                if (this.mThumbnailList.get(i) != null) {
                    ((RotateImageButton) this.mThumbnailList.get(i)).setBackgroundResource(C0088R.drawable.camera_selfie_frame);
                }
            }
        }
    }

    public void showIntervalshotEnteringGuide() {
        Animation animFadein = AnimationUtils.loadAnimation(this.mGet.getAppContext(), C0088R.anim.fade_in);
        animFadein.setDuration(380);
        setGuideTextLayoutParam();
        this.mGuideTextView.setVisibility(0);
        this.mGuideTextView.setText(this.mGet.getAppContext().getString(C0088R.string.intervalshot_guide));
        this.mGuideTextView.startAnimation(animFadein);
    }

    public void hideIntervalshotEnteringGuide() {
        Animation animFadeout = AnimationUtils.loadAnimation(this.mGet.getAppContext(), C0088R.anim.fade_out);
        animFadeout.setDuration(150);
        animFadeout.setAnimationListener(new C10402());
        this.mGuideTextView.startAnimation(animFadeout);
    }

    public void setGuideTextLayoutParam() {
        RotateLayout guideTextViewLayout = (RotateLayout) this.mGet.findViewById(C0088R.id.intervalshot_text_layout);
        if (guideTextViewLayout != null && (guideTextViewLayout.getVisibility() & 12) == 0) {
            LayoutParams lp = (LayoutParams) guideTextViewLayout.getLayoutParams();
            Utils.resetLayoutParameter(lp);
            String previewSize = this.mGet.getCurrentSelectedPreviewSize();
            int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
            switch (Utils.convertDegree(this.mGet.getAppContext().getResources(), this.mGet.getOrientationDegree())) {
                case 0:
                case 180:
                    lp.addRule(12);
                    lp.addRule(14);
                    int bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, Utils.calculate4by3Preview(previewSize) ? 0.18f : 0.15f);
                    if (!this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                        lp.bottomMargin = bottomMargin;
                        break;
                    } else {
                        lp.bottomMargin = lcdSize[1] + (bottomMargin / 2);
                        break;
                    }
                case 90:
                    lp.addRule(21);
                    lp.setMarginEnd(Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.intervalshot_entering_guidetext_marginStart));
                    if (!this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                        lp.addRule(15);
                        break;
                    } else {
                        lp.topMargin = (lcdSize[1] - this.mGuideTextView.getWidth()) / 2;
                        break;
                    }
                case 270:
                    lp.addRule(20);
                    lp.setMarginStart(Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.intervalshot_entering_guidetext_marginStart));
                    if (!this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                        lp.addRule(15);
                        break;
                    } else {
                        lp.topMargin = (lcdSize[1] - this.mGuideTextView.getWidth()) / 2;
                        break;
                    }
            }
            guideTextViewLayout.setLayoutParams(lp);
        }
    }

    public int getIntervalShotState() {
        return this.mIntervalShotState;
    }

    public void setIntervalShotState(int state) {
        CamLog.m3d(CameraConstants.TAG, "mIntervalShotState : " + this.mIntervalShotState);
        this.mIntervalShotState = state;
    }
}
