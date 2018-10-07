package com.lge.camera.managers;

import android.content.res.Configuration;
import android.os.Handler;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageView;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;

public class InclinometerManager extends ManagerInterfaceImpl {
    private static final long TIME_TO_UPDATE_INCLINOMETER = 17;
    private final float ALPHA_DIM = 0.4f;
    private final float ALPHA_NORMAL = 1.0f;
    private final float mALPHA = 0.1f;
    private Runnable mEnableUpdateInclinometer = new C10146();
    private Handler mHander = new Handler();
    private float mHorizontalArrowDegree = 0.0f;
    private RotateLayout mHorizontalMeterArrow;
    private View mInclinometerLayout = null;
    private View mInclinometerView = null;
    private boolean mIsEnabledInclinometer;
    private int mOrientationDegree = 0;
    private OrientationEventListener mOrientationListener = new OrientationEventListener(this.mGet.getAppContext(), 3) {
        public void onOrientationChanged(int orientation) {
            if (orientation < 0) {
                orientation = InclinometerManager.this.mOrientationDegree;
            }
            InclinometerManager.this.mOrientationDegree = orientation;
        }
    };
    private int mRotateDegree = 0;
    private final float mSUCCESS_DEGREE_180_RANGE_MAX = 184.0f;
    private final float mSUCCESS_DEGREE_180_RANGE_MIN = 176.0f;
    private final float mSUCCESS_DEGREE_270_RANGE_MAX = 274.0f;
    private final float mSUCCESS_DEGREE_270_RANGE_MIN = 266.0f;
    private final float mSUCCESS_DEGREE_360_RANGE_MAX = 4.0f;
    private final float mSUCCESS_DEGREE_360_RANGE_MIN = 356.0f;
    private final float mSUCCESS_DEGREE_90_RANGE_MAX = 94.0f;
    private final float mSUCCESS_DEGREE_90_RANGE_MIN = 86.0f;
    private final float mSUCCESS_DEGREE_DELTA_HORIZONTAL = 4.0f;
    private Handler mUpdateInclinometer = new Handler();

    /* renamed from: com.lge.camera.managers.InclinometerManager$4 */
    class C10124 implements AnimationListener {
        C10124() {
        }

        public void onAnimationStart(Animation arg0) {
        }

        public void onAnimationRepeat(Animation arg0) {
        }

        public void onAnimationEnd(Animation arg0) {
            InclinometerManager.this.showInclinometerView(InclinometerManager.this.mIsEnabledInclinometer);
        }
    }

    /* renamed from: com.lge.camera.managers.InclinometerManager$6 */
    class C10146 implements Runnable {
        C10146() {
        }

        public void run() {
            InclinometerManager.this.rotateHorizontalMeterArrow(InclinometerManager.this.mOrientationDegree, false);
            InclinometerManager.this.mUpdateInclinometer.postDelayed(InclinometerManager.this.mEnableUpdateInclinometer, InclinometerManager.TIME_TO_UPDATE_INCLINOMETER);
        }
    }

    public InclinometerManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void initInclinometerView() {
        if (this.mInclinometerLayout == null) {
            this.mInclinometerLayout = this.mGet.inflateView(C0088R.layout.inclinometer_control);
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            if (!(vg == null || this.mInclinometerLayout == null)) {
                vg.addView(this.mInclinometerLayout, 0, new LayoutParams(-1, -1));
            }
        }
        if (this.mInclinometerLayout != null) {
            if (this.mInclinometerView == null) {
                this.mInclinometerView = this.mInclinometerLayout.findViewById(C0088R.id.inclinometer_layout);
            }
            initInclinometerLayout();
            setRotateDegree(this.mGet.getOrientationDegree(), false);
        }
    }

    private void initInclinometerLayout() {
        View innerLayout = this.mInclinometerView.findViewById(C0088R.id.inclinometer_inner_layout);
        LayoutParams innerLayoutParam = (LayoutParams) innerLayout.getLayoutParams();
        innerLayoutParam.width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.7f);
        innerLayoutParam.height = innerLayoutParam.width;
        innerLayout.setLayoutParams(innerLayoutParam);
        View bg = this.mInclinometerView.findViewById(C0088R.id.inclinometer_horizontal_bg);
        LayoutParams bgParam = (LayoutParams) bg.getLayoutParams();
        bgParam.width = innerLayoutParam.width;
        bgParam.height = bgParam.width;
        bg.setLayoutParams(bgParam);
        ((ImageView) bg).setScaleType(ScaleType.FIT_XY);
    }

    private void rotateHorizontalMeterArrow(int orientation, boolean init) {
        float degreeGap = Math.abs(((float) orientation) - this.mHorizontalArrowDegree);
        if (this.mHorizontalMeterArrow != null && Float.compare(degreeGap, 0.0f) != 0) {
            if (this.mHorizontalMeterArrow == null || degreeGap < 0.1f) {
                this.mHorizontalArrowDegree = (float) orientation;
            }
            if (Float.compare(degreeGap, 90.0f) > 0) {
                if (Float.compare(this.mHorizontalArrowDegree, 90.0f) < 0 && orientation > 270) {
                    this.mHorizontalArrowDegree += 360.0f;
                } else if (Float.compare(this.mHorizontalArrowDegree, 270.0f) <= 0 || orientation >= 90) {
                    this.mHorizontalArrowDegree = (float) orientation;
                } else {
                    this.mHorizontalArrowDegree -= 360.0f;
                }
            }
            this.mHorizontalArrowDegree += (((float) orientation) - this.mHorizontalArrowDegree) * 0.1f;
            float rotateDegree = 360.0f - this.mHorizontalArrowDegree;
            changeHorizontalArrowImage(rotateDegree);
            this.mHorizontalMeterArrow.setRotation(rotateDegree);
        }
    }

    private void changeHorizontalArrowImage(float rotateDegree) {
        if (this.mInclinometerView != null) {
            ImageView left = (ImageView) this.mInclinometerView.findViewById(C0088R.id.inclinometer_horizontal_left_arrow);
            ImageView right = (ImageView) this.mInclinometerView.findViewById(C0088R.id.inclinometer_horizontal_right_arrow);
            if (left != null && right != null) {
                if ((Float.compare(rotateDegree, 86.0f) <= 0 || Float.compare(rotateDegree, 94.0f) >= 0) && ((Float.compare(rotateDegree, 176.0f) <= 0 || Float.compare(rotateDegree, 184.0f) >= 0) && ((Float.compare(rotateDegree, 266.0f) <= 0 || Float.compare(rotateDegree, 274.0f) >= 0) && Float.compare(rotateDegree, 356.0f) <= 0 && Float.compare(rotateDegree, 4.0f) >= 0))) {
                    left.setImageResource(C0088R.drawable.camera_inclinometer_level);
                    right.setImageResource(C0088R.drawable.camera_inclinometer_level);
                    return;
                }
                left.setImageResource(C0088R.drawable.camera_inclinometer_level_succeed);
                right.setImageResource(C0088R.drawable.camera_inclinometer_level_succeed);
            }
        }
    }

    public void show(final boolean enable) {
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                InclinometerManager.this.showInclinometer(enable);
            }
        }, 0);
    }

    public void showBySettingValue() {
        show("on".equals(this.mGet.getSettingValue(Setting.KEY_INCLINOMETER)));
    }

    public void setAlpha(boolean enable) {
        if (this.mInclinometerView != null) {
            this.mInclinometerView.setAlpha(enable ? 0.4f : 1.0f);
        }
    }

    private void showInclinometer(final boolean enable) {
        if (this.mInclinometerView != null) {
            if (this.mOrientationListener == null) {
                this.mHander.postDelayed(new Runnable() {
                    public void run() {
                        InclinometerManager.this.showInclinometer(enable);
                    }
                }, 1000);
            } else if (this.mIsEnabledInclinometer != enable) {
                long j;
                this.mIsEnabledInclinometer = enable;
                if (this.mIsEnabledInclinometer) {
                    this.mOrientationListener.enable();
                    scheduleUpdateInclinometer();
                } else {
                    if (this.mUpdateInclinometer != null) {
                        this.mUpdateInclinometer.removeCallbacks(this.mEnableUpdateInclinometer);
                    }
                    this.mOrientationListener.disable();
                }
                this.mInclinometerView.clearAnimation();
                View view = this.mInclinometerView;
                boolean z = this.mIsEnabledInclinometer;
                if (this.mIsEnabledInclinometer) {
                    j = 300;
                } else {
                    j = 0;
                }
                AnimationUtil.startShowingAnimation(view, z, j, new C10124());
            }
        }
    }

    public void setInclinometerMargin(int width, int height, int startMargin, int topMargin) {
        if (this.mInclinometerView != null) {
            View view = this.mGet.findViewById(C0088R.id.inclinometer_inner_layout);
            if (view != null) {
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                if (lp != null) {
                    int lpWidth = lp.width;
                    this.mInclinometerView.setPaddingRelative(this.mInclinometerView.getPaddingStart(), ((height - lpWidth) / 2) + startMargin, this.mInclinometerView.getPaddingEnd(), this.mInclinometerView.getPaddingBottom());
                }
            }
        }
    }

    private void showInclinometerView(final boolean enable) {
        if (this.mInclinometerView != null) {
            this.mHorizontalMeterArrow = (RotateLayout) this.mInclinometerView.findViewById(C0088R.id.inclinometer_horizontal_arrow_layout);
            this.mHorizontalMeterArrow.setVisibility(4);
            this.mInclinometerView.setVisibility(enable ? 0 : 4);
            this.mInclinometerView.setAlpha(1.0f);
        }
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (InclinometerManager.this.mHorizontalMeterArrow != null) {
                    InclinometerManager.this.mHorizontalArrowDegree = (float) InclinometerManager.this.mGet.getOrientationDegree();
                    InclinometerManager.this.rotateHorizontalMeterArrow(InclinometerManager.this.mGet.getOrientationDegree(), true);
                    InclinometerManager.this.mHorizontalMeterArrow.setVisibility(enable ? 0 : 4);
                    InclinometerManager.this.setRotateDegree(InclinometerManager.this.mGet.getOrientationDegree(), false);
                }
            }
        }, 0);
    }

    public void onConfigurationChanged(Configuration config) {
        this.mInclinometerView = null;
        this.mHorizontalMeterArrow = null;
        this.mInclinometerLayout = null;
        this.mInclinometerView = null;
        super.onConfigurationChanged(config);
        initInclinometerView();
    }

    public void onPauseBefore() {
        if (this.mOrientationListener != null) {
            this.mOrientationListener.disable();
        }
        if (this.mInclinometerView != null) {
            this.mInclinometerView.setVisibility(4);
        }
        if (this.mUpdateInclinometer != null) {
            this.mUpdateInclinometer.removeCallbacks(this.mEnableUpdateInclinometer);
        }
        this.mIsEnabledInclinometer = false;
        this.mRotateDegree = 0;
        super.onPauseBefore();
    }

    public void onDestroy() {
        if (this.mInclinometerLayout != null) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            if (vg != null) {
                vg.removeView(this.mInclinometerLayout);
            }
            this.mInclinometerLayout = null;
        }
        this.mInclinometerView = null;
        this.mHorizontalMeterArrow = null;
        super.onDestroy();
    }

    public void onCameraSwitchingStart() {
        CamLog.m3d(CameraConstants.TAG, "Disable view because start changing");
        showInclinometer(false);
        super.onCameraSwitchingStart();
    }

    public void setRotateDegree(int degree, boolean animation) {
        if (this.mInclinometerView != null) {
            if (degree == 0 || degree == 180) {
            }
            if (this.mRotateDegree != degree) {
                this.mRotateDegree = degree;
                ((RotateImageView) this.mInclinometerView.findViewById(C0088R.id.inclinometer_horizontal_bg)).setImageDrawable(getAppContext().getDrawable(C0088R.drawable.camera_inclinometer_main_bg));
                ((RotateImageView) this.mInclinometerView.findViewById(C0088R.id.inclinometer_horizontal_bg)).setDegree(this.mRotateDegree, animation);
            }
        }
    }

    private void scheduleUpdateInclinometer() {
        if (this.mUpdateInclinometer != null) {
            this.mUpdateInclinometer.removeCallbacks(this.mEnableUpdateInclinometer);
            this.mUpdateInclinometer.postDelayed(this.mEnableUpdateInclinometer, TIME_TO_UPDATE_INCLINOMETER);
        }
    }
}
