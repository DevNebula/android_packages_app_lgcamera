package com.lge.camera.managers;

import android.content.res.Configuration;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.Histogram;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.ICameraCallback.CameraHistogramDataCallback;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class HistogramManager extends ManagerInterfaceImpl {
    private static final float HISTOGRAM_HEIGHT = 0.056f;
    private static final float HISTOGRAM_WIDTH = 0.07f;
    private static final long TIME_TO_UPDATE_HISTOGRAM = 300;
    CameraHistogramDataCallback mCameraHistogramDataCallback = new C09941();
    private DrawHistogram mDrawHistogram = new DrawHistogram(this);
    private Runnable mEnableUpdateHistogram = new C09952();
    private Histogram mHistogram = null;
    private View mHistogramView = null;
    private boolean mIsConfigurationChangingDone = true;
    private boolean mIsEnable = false;
    private boolean mIsEnableUpdateHistogram = false;
    private Handler mUpdateHistogram = new Handler();

    /* renamed from: com.lge.camera.managers.HistogramManager$1 */
    class C09941 implements CameraHistogramDataCallback {
        C09941() {
        }

        public void onCameraData(int[] data, CameraProxy camera) {
            if (HistogramManager.this.mIsEnableUpdateHistogram && HistogramManager.this.mDrawHistogram != null) {
                HistogramManager.this.mDrawHistogram.removeRunnable();
                HistogramManager.this.mDrawHistogram.setHistogram(data);
                HistogramManager.this.mGet.postOnUiThread(HistogramManager.this.mDrawHistogram, 0);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.HistogramManager$2 */
    class C09952 implements Runnable {
        C09952() {
        }

        public void run() {
            HistogramManager.this.mIsEnableUpdateHistogram = true;
        }
    }

    private class DrawHistogram extends HandlerRunnable {
        private int[] mHistogramArr;

        public DrawHistogram(OnRemoveHandler removeFunc) {
            super(removeFunc);
        }

        public void setHistogram(int[] histogram) {
            if (histogram != null) {
                this.mHistogramArr = histogram;
            }
        }

        public void handleRun() {
            HistogramManager.this.scheduleUpdateHistogram();
            HistogramManager.this.mIsEnableUpdateHistogram = false;
            if (HistogramManager.this.mHistogram != null && this.mHistogramArr != null) {
                HistogramManager.this.mHistogram.setHistogram(this.mHistogramArr);
            }
        }

        public void unbind() {
            if (this.mHistogramArr != null) {
                this.mHistogramArr = null;
            }
        }
    }

    public HistogramManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void initHistogramView() {
        if (this.mHistogramView == null) {
            this.mHistogramView = this.mGet.inflateView(C0088R.layout.histogram);
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            if (!(vg == null || this.mHistogramView == null)) {
                vg.addView(this.mHistogramView, 0, new LayoutParams(-1, -1));
            }
        }
        if (this.mHistogram == null) {
            this.mHistogram = (Histogram) this.mGet.findViewById(C0088R.id.histogram_view);
        }
        LayoutParams rlp = (LayoutParams) this.mHistogram.getLayoutParams();
        rlp.width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.056f);
        rlp.height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, HISTOGRAM_WIDTH);
        this.mHistogram.setLayoutParams(rlp);
        int orientationDegree = this.mGet.getOrientationDegree();
        if (orientationDegree == 90 || orientationDegree == 270) {
            rotateHistorgram(270, orientationDegree);
        } else {
            rotateHistorgram(270, 270);
        }
    }

    private void setVisibility(boolean visible, boolean useAnim) {
        if (this.mHistogram != null) {
            if (!visible || this.mHistogram.getVisibility() != 0) {
                if (useAnim) {
                    AnimationUtil.startShowingAnimation(this.mHistogram, visible, 300, null);
                } else {
                    this.mHistogram.setVisibility(visible ? 0 : 4);
                }
                if (!visible) {
                    this.mHistogram.releaseHistogram();
                }
            }
        }
    }

    public void setRotateDegree(int degree, boolean animation) {
        super.setRotateDegree(degree, animation);
        if (degree == 90 || degree == 270) {
            rotateHistorgram(270, degree);
        }
    }

    private void rotateHistorgram(int layoutDegree, int degree) {
        if (this.mHistogramView != null) {
            RotateLayout rl = (RotateLayout) this.mHistogramView.findViewById(C0088R.id.histogram_rotate_layout);
            if (rl != null) {
                LayoutParams lp = (LayoutParams) rl.getLayoutParams();
                if (lp != null) {
                    int marginBottom = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.055f);
                    int marginStart = RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext()) + RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.0867f);
                    int marginTopPortrait = Utils.getPx(getAppContext(), C0088R.dimen.histogram_panel_Top_portrait);
                    int marginStartPortrait = Utils.getPx(getAppContext(), C0088R.dimen.manual_upper_panel_marginTop_portrait);
                    Utils.resetLayoutParameter(lp);
                    switch (layoutDegree) {
                        case 0:
                            lp.addRule(20);
                            lp.addRule(10);
                            lp.setMarginsRelative(marginStartPortrait, marginTopPortrait, 0, 0);
                            break;
                        case 90:
                            lp.addRule(21);
                            lp.addRule(12);
                            lp.setMarginsRelative(0, 0, marginBottom, marginStart);
                            break;
                        case 180:
                            lp.addRule(21);
                            lp.addRule(12);
                            lp.setMarginsRelative(0, 0, marginStartPortrait, marginTopPortrait);
                            break;
                        case 270:
                            lp.addRule(20);
                            lp.addRule(10);
                            lp.setMarginsRelative(marginBottom, marginStart, 0, 0);
                            break;
                    }
                    rl.setLayoutParams(lp);
                    rl.rotateLayout((degree + 90) % 360);
                }
            }
        }
    }

    public void enable(boolean enable, boolean useAnim) {
        setVisibility(enable, useAnim);
        registerCallback(enable);
    }

    public boolean isEnable() {
        return this.mIsEnable;
    }

    public void onDestroy() {
        if (this.mUpdateHistogram != null) {
            this.mUpdateHistogram.removeCallbacks(this.mEnableUpdateHistogram);
        }
        if (this.mDrawHistogram != null) {
            this.mDrawHistogram.removeRunnable();
            this.mDrawHistogram.unbind();
        }
        if (this.mHistogramView != null) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            if (vg != null) {
                vg.removeView(this.mHistogramView);
            }
        }
        if (this.mHistogram != null) {
            this.mHistogram.unbind();
            this.mHistogram = null;
        }
        this.mHistogramView = null;
        this.mIsEnable = false;
        super.onDestroy();
    }

    public boolean isConfigurationChangingDone() {
        return this.mIsConfigurationChangingDone;
    }

    public void doConfigurationChange(boolean enable) {
        enable(false, false);
        this.mHistogramView = null;
        this.mHistogram = null;
        enable(enable, true);
        this.mIsConfigurationChangingDone = true;
    }

    public void onConfigurationChanged(Configuration config) {
        CamLog.m3d(CameraConstants.TAG, "onConfigurationChanged");
        this.mIsConfigurationChangingDone = false;
        super.onConfigurationChanged(config);
        doConfigurationChange(false);
        initHistogramView();
    }

    public void onCameraSwitchingStart() {
        CamLog.m3d(CameraConstants.TAG, "Disable view because start changing");
        enable(false, false);
        super.onCameraSwitchingStart();
    }

    private void registerCallback(boolean enable) {
        CamLog.m3d(CameraConstants.TAG, "### registerCallback histogram data callback enable=" + enable);
        if (this.mIsEnable == enable) {
            return;
        }
        if (!enable || !this.mGet.isPaused()) {
            CameraProxy cameraDevice = this.mGet.getCameraDevice();
            if (cameraDevice != null) {
                this.mIsEnable = enable;
                if (cameraDevice != null) {
                    cameraDevice.setHistogramDataCallback(this.mGet.getHandler(), enable ? this.mCameraHistogramDataCallback : null);
                }
                this.mIsEnableUpdateHistogram = true;
                scheduleUpdateHistogram();
            }
        }
    }

    private void scheduleUpdateHistogram() {
        if (this.mUpdateHistogram != null) {
            this.mUpdateHistogram.removeCallbacks(this.mEnableUpdateHistogram);
            this.mUpdateHistogram.postDelayed(this.mEnableUpdateHistogram, 300);
        }
    }
}
