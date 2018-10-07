package com.lge.camera.managers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.p000v4.view.InputDeviceCompat;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.Utils;

public class FingerDetectionManager extends ManagerInterfaceImpl {
    AnimationListener mAniListener = new C09033();
    private final int mAniRepeatTime = 5;
    private final int mAnimationDuration = 600;
    private boolean mDrawLock = true;
    private FingerDetectingView mFingerDetectingView1 = null;
    private FingerDetectingView mFingerDetectingView2 = null;
    private View mGuideBaseView = null;
    private final int mHideDelay = 3000;
    private HandlerRunnable mHideDetectingView = new HandlerRunnable(this) {
        public void handleRun() {
            if (FingerDetectionManager.this.mFingerDetectingView1 != null && FingerDetectionManager.this.mFingerDetectingView2 != null) {
                CamLog.m3d(CameraConstants.TAG, "finger detection hide");
                FingerDetectionManager.this.cancelAnimation();
                FingerDetectionManager.this.mFingerDetectingView1.setVisibility(8);
                FingerDetectionManager.this.mFingerDetectingView2.setVisibility(8);
                FingerDetectionManager.this.setGuideVisibility(false);
                FingerDetectionManager.this.setGuideViewVisibility(false);
                FingerDetectionManager.this.mGet.setTextGuideVisibilityForEachMode(true);
                FingerDetectionManager.this.mDrawLock = true;
            }
        }
    };
    private boolean mIssAnimating = false;
    private int mPreX = -1;
    private int mPreY = -1;
    private int mPreviewLayoutX = 0;
    private int mPreviewLayoutY = 0;
    private int mPreviewX = 0;
    private final float mRadius = 200.0f;
    private HandlerRunnable mShowDetectingView = new HandlerRunnable(this) {
        public void handleRun() {
            if (FingerDetectionManager.this.mFingerDetectingView1 != null && FingerDetectionManager.this.mFingerDetectingView2 != null && FingerDetectionManager.this.checkCondition()) {
                CamLog.m3d(CameraConstants.TAG, "finger detection show");
                FingerDetectionManager.this.setFingerDetecingViewLayoutParam();
                FingerDetectionManager.this.mFingerDetectingView1.setVisibility(0);
                FingerDetectionManager.this.mFingerDetectingView2.setVisibility(0);
                FingerDetectionManager.this.setGuideVisibility(true);
                FingerDetectionManager.this.setGuideViewVisibility(true);
                FingerDetectionManager.this.mGet.setTextGuideVisibilityForEachMode(false);
                FingerDetectionManager.this.startAnimation();
                AudioUtil.performHapticFeedback(FingerDetectionManager.this.mFingerDetectingView1, 65591);
                FingerDetectionManager.this.mDrawLock = false;
            }
        }
    };
    private SurfaceView mSurfaceView = null;
    private final float mTextWidthRatio = 0.83f;
    private final float mTextWidthRatioForLandscape = 0.61f;
    private final float mTextWidthRatioForManual = 0.71f;
    private final float mTextWidthRatioForSquareLand = 0.66f;
    private final float mTextWidthRatioForSquarePort = 0.8f;
    private ListView mTileView = null;
    /* renamed from: mX */
    private int f31mX = -1;
    /* renamed from: mY */
    private int f32mY = -1;

    /* renamed from: com.lge.camera.managers.FingerDetectionManager$3 */
    class C09033 implements AnimationListener {
        C09033() {
        }

        public void onAnimationEnd(Animation arg0) {
            CamLog.m3d(CameraConstants.TAG, "[finger-detection] onAnimationEnd");
            FingerDetectionManager.this.mIssAnimating = false;
        }

        public void onAnimationRepeat(Animation arg0) {
            CamLog.m3d(CameraConstants.TAG, "[finger-detection] onAnimationRepeat");
        }

        public void onAnimationStart(Animation arg0) {
            CamLog.m3d(CameraConstants.TAG, "[finger-detection] onAnimationStart");
            FingerDetectionManager.this.mIssAnimating = true;
        }
    }

    class FingerDetectingView extends View {
        Paint mPaint;
        RectF mRect;

        public FingerDetectingView(Context context) {
            super(context);
            this.mPaint = null;
            this.mRect = null;
            this.mPaint = new Paint();
            this.mPaint.setColor(InputDeviceCompat.SOURCE_ANY);
            this.mPaint.setAntiAlias(true);
            this.mRect = new RectF(0.0f, 0.0f, 400.0f, 400.0f);
        }

        public FingerDetectingView(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.mPaint = null;
            this.mRect = null;
        }

        public FingerDetectingView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            this.mPaint = null;
            this.mRect = null;
        }

        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawArc(this.mRect, 0.0f, 180.0f, true, this.mPaint);
        }
    }

    public FingerDetectionManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void initializeAfterStartPreviewDone() {
        super.initializeAfterStartPreviewDone();
        this.mSurfaceView = (SurfaceView) this.mGet.findViewById(C0088R.id.preview_surface_view);
        this.mTileView = (ListView) this.mGet.findViewById(C0088R.id.thumbnail_listview);
        FrameLayout previewLayout = (FrameLayout) this.mGet.findViewById(C0088R.id.preview_layout);
        if (this.mFingerDetectingView1 == null) {
            this.mFingerDetectingView1 = new FingerDetectingView(this.mGet.getAppContext());
            previewLayout.addView(this.mFingerDetectingView1);
        }
        if (this.mFingerDetectingView2 == null) {
            this.mFingerDetectingView2 = new FingerDetectingView(this.mGet.getAppContext());
            previewLayout.addView(this.mFingerDetectingView2);
        }
        this.mFingerDetectingView1.setVisibility(8);
        this.mFingerDetectingView2.setVisibility(8);
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        FrameLayout previewLayout = (FrameLayout) this.mGet.findViewById(C0088R.id.preview_layout);
        if (previewLayout != null) {
            if (this.mFingerDetectingView1 != null) {
                this.mFingerDetectingView1.setVisibility(8);
                previewLayout.removeView(this.mFingerDetectingView1);
                this.mFingerDetectingView1 = null;
            }
            if (this.mFingerDetectingView2 != null) {
                this.mFingerDetectingView2.setVisibility(8);
                previewLayout.removeView(this.mFingerDetectingView2);
                this.mFingerDetectingView2 = null;
            }
        }
        setGuideVisibility(false);
        setGuideViewVisibility(false);
    }

    public void setPreviewSize(String previewSize) {
        CamLog.m3d(CameraConstants.TAG, "previewSize>>" + previewSize);
        this.mPreviewX = Utils.sizeStringToArray(previewSize)[1];
    }

    public void reset() {
        CamLog.m3d(CameraConstants.TAG, "finger detection reset");
        this.f31mX = -1;
        this.f32mY = -1;
        this.mPreX = -1;
        this.mPreY = -1;
        if ((this.mFingerDetectingView1 != null && this.mFingerDetectingView1.getVisibility() == 0) || (this.mFingerDetectingView2 != null && this.mFingerDetectingView2.getVisibility() == 0)) {
            this.mGet.removePostRunnable(this.mShowDetectingView);
            this.mGet.runOnUiThread(this.mHideDetectingView);
        }
    }

    public void onFingerDetected(int x, int y) {
        if (this.mFingerDetectingView1 == null || this.mFingerDetectingView2 == null || this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL)) {
            CamLog.m3d(CameraConstants.TAG, "mFingerDetectingView is null");
            return;
        }
        this.f31mX = x;
        this.f32mY = y;
        if (this.f31mX < 0 || this.f32mY < 0) {
            if (this.mPreX == -1 && this.mPreY == -1 && (this.mFingerDetectingView1.getVisibility() == 0 || this.mFingerDetectingView2.getVisibility() == 0)) {
                this.mGet.removePostRunnable(this.mHideDetectingView);
                this.mGet.runOnUiThread(this.mHideDetectingView);
            }
            this.mGet.removePostRunnable(this.mShowDetectingView);
        } else {
            if (checkPositionDiff() && !this.mDrawLock) {
                this.mGet.runOnUiThread(this.mShowDetectingView);
            }
            if (this.mPreX == -1 && this.mPreY == -1) {
                this.mGet.runOnUiThread(this.mShowDetectingView);
                this.mGet.postOnUiThread(this.mHideDetectingView, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
            }
        }
        this.mPreX = this.f31mX;
        this.mPreY = this.f32mY;
    }

    private boolean checkPositionDiff() {
        return Math.abs(this.f31mX - this.mPreX) > 100;
    }

    protected void setGuideVisibility(boolean show) {
        if (!show) {
            View mGuidView = this.mGet.findViewById(C0088R.id.finger_detection_guide_layout);
            if (mGuidView != null) {
                ViewGroup parent = (ViewGroup) mGuidView.getParent();
                if (parent != null) {
                    parent.removeView(mGuidView);
                }
            }
        } else if (this.mGet.findViewById(C0088R.id.finger_detection_guide_layout) == null) {
            this.mGuideBaseView = this.mGet.inflateView(C0088R.layout.finger_detection_guide, (FrameLayout) this.mGet.findViewById(C0088R.id.contents_base));
        }
    }

    protected void setGuideViewVisibility(boolean show) {
        CamLog.m3d(CameraConstants.TAG, "setGuideViewVisibility : " + show);
        if (this.mGuideBaseView != null) {
            updateGuideViewDegree();
            int visibility = show ? 0 : 8;
            View guideTextView = this.mGuideBaseView.findViewById(C0088R.id.finger_detection_guide_textview);
            if (guideTextView != null) {
                guideTextView.setVisibility(visibility);
            }
        }
    }

    private void updateGuideViewDegree() {
        RotateLayout mGuideTextLayout = (RotateLayout) this.mGet.findViewById(C0088R.id.finger_detection_guide_layout_rotate);
        TextView textView = (TextView) this.mGet.findViewById(C0088R.id.finger_detection_guide_textview);
        if (mGuideTextLayout != null && textView != null) {
            int location = getLocation();
            mGuideTextLayout.rotateLayout(getDegree());
            LayoutParams lp = (LayoutParams) mGuideTextLayout.getLayoutParams();
            LayoutParams textLp = (LayoutParams) textView.getLayoutParams();
            textLp.width = Math.round(((float) this.mPreviewLayoutX) * 0.83f);
            Utils.resetLayoutParameter(lp);
            switch (location) {
                case 0:
                    setLayoutParamFor0degree(lp, textLp);
                    break;
                case 90:
                    setLayoutParamFor90degree(lp, textLp);
                    break;
                case 180:
                    setLayoutParamFor180degree(lp, textLp);
                    break;
                case 270:
                    setLayoutParamFor270degree(lp, textLp);
                    break;
            }
            mGuideTextLayout.setLayoutParams(lp);
            textView.setLayoutParams(textLp);
        }
    }

    private int getLocation() {
        if (this.mGet.getShotMode().contains(CameraConstants.MODE_PANORAMA)) {
            if (this.mManagerDegree == 90 || this.mManagerDegree == 270) {
                return 0;
            }
        } else if (CameraConstants.MODE_SNAP.equals(this.mGet.getShotMode())) {
            int fixedDegree = this.mGet.getSnapFixedDegree();
            if (fixedDegree < 0) {
                return this.mManagerDegree;
            }
            return fixedDegree;
        }
        if (this.mManagerDegree != 180) {
            return this.mManagerDegree;
        }
        return 0;
    }

    private int getDegree() {
        if (this.mGet.getShotMode().contains(CameraConstants.MODE_PANORAMA)) {
            if (this.mManagerDegree == 90 || this.mManagerDegree == 270) {
                return 0;
            }
        } else if (CameraConstants.MODE_SNAP.equals(this.mGet.getShotMode())) {
            int fixedDegree = this.mGet.getSnapFixedDegree();
            if (fixedDegree < 0) {
                return this.mManagerDegree;
            }
            return fixedDegree;
        }
        return this.mManagerDegree;
    }

    private void setLayoutParamFor0degree(LayoutParams lp, LayoutParams textLp) {
        String shotMode = this.mGet.getShotMode();
        if (CameraConstants.MODE_MANUAL_CAMERA.equals(shotMode) || CameraConstants.MODE_MANUAL_VIDEO.equals(shotMode)) {
            textLp.width = Math.round(((float) this.mPreviewLayoutX) * 0.71f);
        } else if (shotMode.contains(CameraConstants.MODE_SQUARE)) {
            textLp.width = Math.round(((float) this.mPreviewLayoutX) * 0.8f);
        }
        lp.addRule(14, 1);
        lp.addRule(10, 1);
        if (shotMode.contains(CameraConstants.MODE_SQUARE)) {
            lp.topMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.inline_finger_detection_guid_text_port_marginTop_squaremode);
        } else {
            lp.topMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.inline_finger_detection_guid_text_port_marginTop) + getMarginTopByTileView();
        }
    }

    private void setLayoutParamFor180degree(LayoutParams lp, LayoutParams textLp) {
        String shotMode = this.mGet.getShotMode();
        if (CameraConstants.MODE_MANUAL_CAMERA.equals(shotMode) || CameraConstants.MODE_MANUAL_VIDEO.equals(shotMode)) {
            textLp.width = Math.round(((float) this.mPreviewLayoutX) * 0.71f);
        } else if (shotMode.contains(CameraConstants.MODE_SQUARE)) {
            textLp.width = Math.round(((float) this.mPreviewLayoutX) * 0.8f);
        }
        lp.addRule(14, 1);
        lp.addRule(12, 1);
        lp.bottomMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.inline_finger_detection_guid_text_port_marginBottom);
    }

    private void setLayoutParamFor90degree(LayoutParams lp, LayoutParams textLp) {
        if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
            textLp.width = Math.round(((float) this.mPreviewLayoutY) * 0.66f);
        } else {
            textLp.width = Math.round(((float) this.mPreviewLayoutY) * 0.61f);
        }
        int marginStart = Utils.getPx(getAppContext(), C0088R.dimen.inline_finger_detection_guid_text_port_marginEnd);
        lp.addRule(10, 1);
        int marginTop = (getMarginTopOfPreview() + (this.mPreviewLayoutY / 2)) - (textLp.width / 2);
        lp.addRule(20, 1);
        lp.setMargins(marginStart, marginTop, 0, 0);
    }

    private void setLayoutParamFor270degree(LayoutParams lp, LayoutParams textLp) {
        if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
            textLp.width = Math.round(((float) this.mPreviewLayoutY) * 0.66f);
        } else {
            textLp.width = Math.round(((float) this.mPreviewLayoutY) * 0.61f);
        }
        int marginEnd = Utils.getPx(getAppContext(), C0088R.dimen.inline_finger_detection_guid_text_port_marginEnd);
        lp.addRule(10, 1);
        int marginTop = (getMarginTopOfPreview() + (this.mPreviewLayoutY / 2)) - (textLp.width / 2);
        lp.addRule(21, 1);
        lp.setMargins(0, marginTop, marginEnd, 0);
    }

    private int getMarginTopOfPreview() {
        if (this.mSurfaceView != null) {
            return ((FrameLayout.LayoutParams) this.mSurfaceView.getLayoutParams()).topMargin;
        }
        return 0;
    }

    private int getMarginTopByTileView() {
        if (this.mTileView == null || this.mTileView.getVisibility() != 0) {
            return 0;
        }
        return this.mTileView.getWidth();
    }

    private boolean checkCondition() {
        if (this.mManagerDegree % 180 == 90) {
            return true;
        }
        if (this.mManagerDegree % 180 == 0) {
            return false;
        }
        return false;
    }

    private void setFingerDetecingViewLayoutParam() {
        if (this.mSurfaceView == null || this.mFingerDetectingView1 == null || this.mFingerDetectingView2 == null) {
            CamLog.m3d(CameraConstants.TAG, "view is null");
            return;
        }
        int start;
        int end;
        this.mPreviewLayoutX = this.mSurfaceView.getWidth();
        this.mPreviewLayoutY = this.mSurfaceView.getHeight();
        FrameLayout.LayoutParams layoutParam = (FrameLayout.LayoutParams) this.mFingerDetectingView1.getLayoutParams();
        int xOfHalfCircleCenter = (this.f31mX * this.mPreviewLayoutX) / this.mPreviewX;
        int top = getMarginTopOfPreview() - 200;
        layoutParam.width = 400;
        layoutParam.height = 400;
        if (this.f31mX < this.mPreviewX / 2) {
            layoutParam.gravity = 8388659;
            start = xOfHalfCircleCenter - 200;
            end = 0;
        } else {
            layoutParam.gravity = 8388661;
            start = 0;
            end = (this.mPreviewLayoutX - xOfHalfCircleCenter) - 200;
        }
        CamLog.m3d(CameraConstants.TAG, "[finger-detection] start : " + start + " end : " + end);
        layoutParam.setMarginsRelative(start, top, end, 0);
        this.mFingerDetectingView1.setLayoutParams(layoutParam);
        this.mFingerDetectingView2.setLayoutParams(layoutParam);
    }

    private void startAnimation() {
        if (!this.mIssAnimating) {
            AnimationSet set1 = new AnimationSet(true);
            set1.setInterpolator(new AccelerateInterpolator());
            Animation alphaAni1 = new AlphaAnimation(0.5f, 0.0f);
            alphaAni1.setDuration(600);
            alphaAni1.setRepeatCount(5);
            alphaAni1.setRepeatMode(1);
            Animation scaleAni1 = new ScaleAnimation(0.4f, 1.2f, 0.4f, 1.2f, 200.0f, 200.0f);
            scaleAni1.setDuration(600);
            scaleAni1.setRepeatCount(5);
            scaleAni1.setRepeatMode(1);
            set1.addAnimation(alphaAni1);
            set1.addAnimation(scaleAni1);
            set1.setAnimationListener(this.mAniListener);
            this.mFingerDetectingView1.startAnimation(set1);
            AnimationSet set2 = new AnimationSet(true);
            set2.setInterpolator(new AccelerateInterpolator());
            Animation alphaAni2 = new AlphaAnimation(0.0f, 0.5f);
            alphaAni2.setDuration(300);
            alphaAni2.setStartOffset(300);
            alphaAni2.setRepeatCount(5);
            alphaAni2.setRepeatMode(1);
            Animation scaleAni2 = new ScaleAnimation(0.8f, 0.4f, 0.8f, 0.4f, 200.0f, 200.0f);
            scaleAni2.setDuration(300);
            scaleAni2.setStartOffset(300);
            scaleAni2.setRepeatCount(5);
            scaleAni2.setRepeatMode(1);
            set2.addAnimation(alphaAni2);
            set2.addAnimation(scaleAni2);
            this.mFingerDetectingView2.startAnimation(set2);
        }
    }

    private void cancelAnimation() {
        if (this.mFingerDetectingView1 != null) {
            this.mFingerDetectingView1.clearAnimation();
        }
        if (this.mFingerDetectingView2 != null) {
            this.mFingerDetectingView2.clearAnimation();
        }
    }
}
