package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class CameraFocusNormalView extends CameraFocusView {
    protected static final int ANI_DRAW = 2;
    protected static final int ANI_NONE = 0;
    protected static final int ANI_READY = 1;
    public static final int STATE_AF_AE_LOCK = 12;
    public static final int STATE_AF_TRACKING = 11;
    public static final int STATE_CONTINUOUS_FAIL = 5;
    public static final int STATE_CONTINUOUS_SEARCHING = 3;
    public static final int STATE_CONTINUOUS_SUCCESS = 4;
    public static final int STATE_FAIL = 2;
    public static final int STATE_FRONT_AE = 9;
    public static final int STATE_NORMAL = 0;
    public static final int STATE_REAR_AE = 10;
    public static final int STATE_SUCCESS = 1;
    public static final int STATE_TOUCH_FAIL = 8;
    public static final int STATE_TOUCH_NORMAL = 6;
    public static final int STATE_TOUCH_SUCCESS = 7;
    protected long mAniDuration = 0;
    protected int mAnimationState = 0;
    private BitmapDrawable mContiNormalHorizon = null;
    private BitmapDrawable mContiNormalVertical = null;
    private BitmapDrawable mContiSuccessHorizon = null;
    private BitmapDrawable mContiSuccessVertical = null;
    protected float mCurAlpha = 0.0f;
    protected float mCurScale = 0.0f;
    protected float mEndAlpha = 0.0f;
    protected float mEndScale = 0.0f;
    protected long mEndTime = 0;
    protected float mImageHeight = 0.0f;
    protected float mImageWidth = 0.0f;
    protected DecelerateInterpolator mInterpolator = new DecelerateInterpolator(1.25f);
    private boolean mIsSupportedMultiFocus = true;
    private boolean mIsSupportedTrackingFocus = false;
    protected float mPivotX = 0.0f;
    protected float mPivotY = 0.0f;
    protected float mStartAlpha = 0.0f;
    protected float mStartScale = 0.0f;
    protected long mStartTime = 0;
    private BitmapDrawable mTAEFrontHorizon = null;
    private BitmapDrawable mTAEFrontVertical = null;
    private BitmapDrawable mTAERearHorizon = null;
    private BitmapDrawable mTAERearVertical = null;
    private Drawable mTAFAELockHorizon = null;
    private Drawable mTAFAELockVertical = null;
    private BitmapDrawable mTAFNormalHorizon = null;
    private BitmapDrawable mTAFNormalVertical = null;
    protected BitmapDrawable mTAFSuccessHorizon = null;
    protected BitmapDrawable mTAFSuccessVertical = null;
    protected Drawable mTAFTrackingHorizon = null;
    protected Drawable mTAFTrackingVertical = null;
    int[] previewSize;

    /* renamed from: com.lge.camera.components.CameraFocusNormalView$1 */
    class C05331 implements Runnable {
        C05331() {
        }

        public void run() {
            CameraFocusNormalView.this.animationTrigger();
        }
    }

    public CameraFocusNormalView(Context context) {
        super(context);
    }

    public CameraFocusNormalView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraFocusNormalView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init() {
        if (!this.mInit) {
            initResources();
            this.mInit = true;
        }
    }

    public void setSupportedMultiFocus(boolean set, int[] previewSizeOnScreen) {
        this.mIsSupportedMultiFocus = set;
        this.previewSize = previewSizeOnScreen;
    }

    public void getSupportedTrackingFocus(boolean isSupportedTracking) {
        this.mIsSupportedTrackingFocus = isSupportedTracking;
    }

    public void initResources() {
        if (!this.mIsSupportedMultiFocus) {
            this.mContiNormalHorizon = getBitmapDrawable(C0088R.drawable.focus_touch_taf, 0);
            this.mContiNormalVertical = getBitmapDrawable(C0088R.drawable.focus_touch_taf, 90);
            this.mContiSuccessHorizon = getBitmapDrawable(C0088R.drawable.camera_focus_rear_af_ae, 0);
            this.mContiSuccessVertical = getBitmapDrawable(C0088R.drawable.camera_focus_rear_af_ae, 90);
        }
        this.mTAFNormalHorizon = getBitmapDrawable(C0088R.drawable.focus_touch_taf, 0);
        this.mTAFNormalVertical = getBitmapDrawable(C0088R.drawable.focus_touch_taf, 90);
        this.mTAFSuccessHorizon = getBitmapDrawable(C0088R.drawable.camera_focus_rear_af_ae, 0);
        this.mTAFSuccessVertical = getBitmapDrawable(C0088R.drawable.camera_focus_rear_af_ae, 90);
        if (this.mIsSupportedTrackingFocus) {
            this.mTAFTrackingHorizon = this.mContext.getDrawable(C0088R.drawable.camera_focus_tracking_af);
            this.mTAFTrackingVertical = this.mContext.getDrawable(C0088R.drawable.camera_focus_tracking_af);
        }
        this.mTAEFrontHorizon = getBitmapDrawable(C0088R.drawable.camera_focus_front_ae, 0);
        this.mTAEFrontVertical = getBitmapDrawable(C0088R.drawable.camera_focus_front_ae, 90);
        this.mTAERearHorizon = getBitmapDrawable(C0088R.drawable.camera_focus_rear_ae, 0);
        this.mTAERearVertical = getBitmapDrawable(C0088R.drawable.camera_focus_rear_ae, 90);
        this.mTAFAELockHorizon = this.mContext.getDrawable(C0088R.drawable.camera_focus_ae_af_lock);
        this.mTAFAELockVertical = this.mContext.getDrawable(C0088R.drawable.camera_focus_ae_af_lock);
    }

    public void unbind() {
        this.mInit = false;
        this.mContiNormalHorizon = null;
        this.mContiNormalVertical = null;
        this.mContiSuccessHorizon = null;
        this.mContiSuccessVertical = null;
        this.mTAFSuccessHorizon = null;
        this.mTAFSuccessVertical = null;
        this.mTAFNormalHorizon = null;
        this.mTAFNormalVertical = null;
        this.mTAEFrontHorizon = null;
        this.mTAEFrontVertical = null;
        this.mTAERearHorizon = null;
        this.mTAERearVertical = null;
    }

    public void setState(int state) {
        CamLog.m7i(CameraConstants.TAG, "setState state = " + state);
        if (this.mInit) {
            super.setState(state);
            switch (state) {
                case 0:
                case 2:
                case 3:
                case 5:
                    if (!this.mIsSupportedMultiFocus) {
                        setImageDrawable(isHorizontal() ? this.mContiNormalHorizon : this.mContiNormalVertical);
                        break;
                    }
                    break;
                case 1:
                case 4:
                    if (!this.mIsSupportedMultiFocus) {
                        setImageDrawable(isHorizontal() ? this.mContiSuccessHorizon : this.mContiSuccessVertical);
                        break;
                    }
                    break;
                case 6:
                case 8:
                    setImageDrawable(isHorizontal() ? this.mTAFNormalHorizon : this.mTAFNormalVertical);
                    break;
                case 7:
                    setImageDrawable(isHorizontal() ? this.mTAFSuccessHorizon : this.mTAFSuccessVertical);
                    break;
                case 9:
                    setImageDrawable(isHorizontal() ? this.mTAEFrontHorizon : this.mTAEFrontVertical);
                    break;
                case 10:
                    setImageDrawable(isHorizontal() ? this.mTAERearHorizon : this.mTAERearVertical);
                    break;
                case 11:
                    CamLog.m3d(CameraConstants.TAG, "-tf- STATE_AF_TRACKING");
                    setImageDrawable(isHorizontal() ? this.mTAFTrackingHorizon : this.mTAFTrackingVertical);
                    break;
                case 12:
                    CamLog.m3d(CameraConstants.TAG, "-tf- STATE_AF_AE_LOCK");
                    setImageDrawable(isHorizontal() ? this.mTAFAELockHorizon : this.mTAFAELockVertical);
                    break;
                default:
                    CamLog.m3d(CameraConstants.TAG, "focus indicator state out of range!");
                    return;
            }
            invalidate();
        }
    }

    public void startFocusAnimation(int duration, float startScale, float endScale, float px, float py, float startAlpha, float endAlpha) {
        if (this.mInit) {
            this.mAniDuration = (long) duration;
            this.mStartTime = 0;
            this.mEndTime = 0;
            this.mAnimationState = 1;
            this.mStartScale = startScale;
            this.mEndScale = endScale;
            this.mPivotX = px;
            this.mPivotY = py;
            this.mStartAlpha = startAlpha;
            this.mEndAlpha = endAlpha;
            CamLog.m3d(CameraConstants.TAG, "mStartScale = " + this.mStartScale + " mEndScale = " + this.mEndScale + " mPivotX = " + this.mPivotX + " mPivotY = " + this.mPivotY);
            invalidate();
            if (this.mAniDuration == 0) {
                animationTrigger();
            } else {
                postDelayed(new C05331(), 100);
            }
        }
    }

    private void animationTrigger() {
        this.mStartTime = System.currentTimeMillis();
        this.mEndTime = this.mStartTime + this.mAniDuration;
        this.mAnimationState = 2;
    }

    public void setFocusLocation(int px, int py) {
        this.mPivotX = (float) px;
        this.mPivotY = (float) py;
    }

    protected void applyTransfomation(boolean animationEnded, long curTime) {
        if (animationEnded) {
            this.mCurScale = this.mEndScale;
            this.mCurAlpha = this.mEndAlpha;
            return;
        }
        float interpolate = this.mInterpolator.getInterpolation(Math.min(((float) (curTime - this.mStartTime)) / ((float) this.mAniDuration), 1.0f));
        if (this.mEndScale >= this.mStartScale) {
            this.mCurScale = this.mStartScale + ((this.mEndScale - this.mStartScale) * interpolate);
        } else {
            this.mCurScale = this.mEndScale + ((this.mStartScale - this.mEndScale) * (1.0f - interpolate));
        }
        if (this.mEndAlpha >= this.mStartAlpha) {
            this.mCurAlpha = this.mStartAlpha + ((this.mEndAlpha - this.mStartAlpha) * interpolate);
        } else {
            this.mCurAlpha = this.mEndAlpha + ((this.mStartAlpha - this.mEndAlpha) * (1.0f - interpolate));
        }
    }

    public void clearAnimation() {
        super.clearAnimation();
        this.mAnimationState = 0;
        postInvalidate();
    }

    protected void onDraw(Canvas canvas) {
        boolean animationEnded = false;
        if (this.mInit && getVisibility() == 0) {
            Drawable drawable = getDrawable();
            if (drawable != null) {
                int left;
                int right;
                int top;
                int bottom;
                switch (this.mAnimationState) {
                    case 1:
                        drawable.setAlpha(Math.round(this.mStartAlpha * 255.0f));
                        this.mImageWidth = ((float) drawable.getIntrinsicWidth()) * this.mStartScale;
                        this.mImageHeight = ((float) drawable.getIntrinsicHeight()) * this.mStartScale;
                        left = Math.round(this.mPivotX - (this.mImageWidth / 2.0f));
                        right = left + ((int) this.mImageWidth);
                        top = Math.round(this.mPivotY - (this.mImageHeight / 2.0f));
                        bottom = top + ((int) this.mImageHeight);
                        postInvalidate();
                        break;
                    case 2:
                        long curTime = System.currentTimeMillis();
                        if (curTime >= this.mEndTime) {
                            animationEnded = true;
                        }
                        applyTransfomation(animationEnded, curTime);
                        drawable.setAlpha(Math.round(this.mCurAlpha * 255.0f));
                        this.mImageWidth = ((float) drawable.getIntrinsicWidth()) * this.mCurScale;
                        this.mImageHeight = ((float) drawable.getIntrinsicHeight()) * this.mCurScale;
                        left = Math.round(this.mPivotX - (this.mImageWidth / 2.0f));
                        right = left + ((int) this.mImageWidth);
                        top = Math.round(this.mPivotY - (this.mImageHeight / 2.0f));
                        bottom = top + ((int) this.mImageHeight);
                        if (!animationEnded) {
                            postInvalidate();
                            break;
                        }
                        break;
                    default:
                        this.mImageWidth = getShutterLength(drawable.getIntrinsicWidth()) * 1.0f;
                        this.mImageHeight = getShutterLength(drawable.getIntrinsicHeight()) * 1.0f;
                        left = Math.max(0, Math.round(this.mPivotX - (this.mImageWidth / 2.0f)));
                        top = Math.max(0, Math.round(this.mPivotY - (this.mImageHeight / 2.0f)));
                        right = left + ((int) this.mImageWidth);
                        bottom = top + ((int) this.mImageHeight);
                        break;
                }
                drawable.setBounds(left, top, right, bottom);
                drawable.draw(canvas);
            }
        }
    }

    protected float getShutterLength(int defaultLength) {
        if (this.mIsSupportedMultiFocus || this.previewSize == null || (getState() != 4 && getState() != 3 && getState() != 5)) {
            return (float) defaultLength;
        }
        int width = (int) (((double) this.previewSize[1]) * 0.18d);
        int height = (int) (((double) this.previewSize[0]) * 0.18d);
        return width > height ? (float) height : (float) width;
    }
}
