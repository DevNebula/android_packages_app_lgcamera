package com.lge.camera.managers;

import android.content.res.Configuration;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.app.MiniActivity;
import com.lge.camera.C0088R;
import com.lge.camera.components.HorizontalSeekBar;
import com.lge.camera.components.HorizontalSeekBar.OnVerticalSeekBarListener;
import com.lge.camera.components.JogZoomMinimap;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.components.ZoomBar;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.ConfigurationHALUtil;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ICameraCallback.ZoomChangeCallback;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;

public class ZoomManager extends ZoomManagerIF {
    protected final int DEFAULT_ZOG_ZOOM_PROGRESS_VALUE = 9;
    private final int JOG_ZOOM_BUTTON_VALUE = 3;
    protected final float JOG_ZOOM_HEIGHT = 0.128f;
    protected final float JOG_ZOOM_MARGIN_END = 0.057f;
    protected final float JOG_ZOOM_MARGIN_TOP = 0.651f;
    protected final float JOG_ZOOM_MARGIN_TOP_USP = 0.561f;
    protected int JOG_ZOOM_MAX_STEP = 64;
    protected final float JOG_ZOOM_MINIMAP_HEIGHT = 0.067f;
    protected final float JOG_ZOOM_MINIMAP_MARGIN_TOP = 5.0E-4f;
    private final int JOG_ZOOM_MIN_STEP = 4;
    protected final float JOG_ZOOM_WIDTH = 0.069f;
    private final int MAX_ZOG_ZOOM_PROGRESS_VALUE = 18;
    private final float MINIMAP_TEXT_SIZE = 0.035f;
    protected final int PREVIEW_DEFAULT_HEIGHT = CameraConstantsEx.QHD_SCREEN_RESOLUTION;
    protected final int PREVIEW_DEFAULT_WIDTH = 2550;
    protected final int STEP_DIVISION_COEFFICIENT = 1;
    protected final long TIME_TO_UPDATE_JOG_ZOOM = 30;
    protected int mAimToZoomValue = -1;
    protected int mCurJogZoomSpeed = 0;
    protected HandlerRunnable mHideJogZoomRunnable = null;
    private boolean mHideZoomPosted = false;
    protected boolean mIsInitialized = false;
    protected boolean mIsJogZoomTouched = false;
    protected boolean mIsJogZoomWorking = false;
    protected View mJogZoomBottomBtnLayout = null;
    protected View mJogZoomControlLayout = null;
    protected int mJogZoomFactor = 0;
    protected JogZoomMinimap mJogZoomMinimap = null;
    protected View mJogZoomMinimapLayout = null;
    protected int mJogZoomStep = 4;
    protected View mJogZoomTopBtnLayout = null;
    protected HandlerRunnable mJogZoomUpdate = new HandlerRunnable(this) {
        public void handleRun() {
            if (ZoomManager.this.mGet != null && ZoomManager.this.mGet.isJogZoomAvailable() && ZoomManager.this.mIsJogZoomWorking) {
                ZoomManager.this.mAimToZoomValue = -1;
                ZoomManager.this.mAimToZoomValue = ZoomManager.this.calZoomProgress(ZoomManager.this.mLastSeekBarProgressValue);
                ZoomManager.this.changeJogZoomValue(ZoomManager.this.mJogZoomStep, ZoomManager.this.mJogZoomFactor);
                ZoomManager.this.mGet.removePostRunnable(ZoomManager.this.mHideJogZoomRunnable);
                ZoomManager.this.setJogZoomMinimapVisibility(0);
                ZoomManager.this.updateJogZoomMinimap();
                ZoomManager.this.mGet.postOnUiThread(this, 30);
            }
        }
    };
    protected int mLastSeekBarProgressValue = 9;
    protected HorizontalSeekBar mMiniActivityZoomBar = null;
    protected RotateTextView mMinimapTextT = null;
    protected RotateTextView mMinimapTextW = null;
    protected boolean mResetJogSpeed = false;
    protected View mZoomBarView = null;
    protected ZoomChangeCallback mZoomChangeCallback = new C10056();
    private HandlerRunnable mZoomHide = new HandlerRunnable(this) {
        public void handleRun() {
            if (ZoomManager.this.mZoomInterface != null) {
                ZoomManager.this.mZoomInterface.onZoomHide();
            }
            ZoomManager.this.mHideZoomPosted = false;
        }
    };

    /* renamed from: com.lge.camera.managers.ZoomManager$1 */
    class C10001 implements OnVerticalSeekBarListener {
        C10001() {
        }

        public void onBarValueChanged(int progress) {
            ZoomManager.this.moveJogZoom(progress);
        }

        public void onBarTouchUp() {
            ZoomManager.this.mIsJogZoomTouched = false;
            ZoomManager.this.stopJogZoom();
        }

        public void onBarTouchDown() {
            ZoomManager.this.mIsJogZoomTouched = true;
            ZoomManager.this.stopDrawingExceedsLevel();
        }
    }

    /* renamed from: com.lge.camera.managers.ZoomManager$6 */
    class C10056 implements ZoomChangeCallback {
        C10056() {
        }

        public void onZoomChange(int zoomValue) {
            ZoomManager.this.setZoomValue(zoomValue);
        }
    }

    /* renamed from: com.lge.camera.managers.ZoomManager$7 */
    class C10067 implements OnTouchListener {
        C10067() {
        }

        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case 0:
                    CamLog.m3d(CameraConstants.TAG, "[jog] top button touch down");
                    ZoomManager.this.mJogZoomFactor = 1;
                    ZoomManager.this.moveJogZoom(6);
                    break;
                case 1:
                    CamLog.m3d(CameraConstants.TAG, "[jog] top button touch up");
                    ZoomManager.this.stopJogZoom();
                    break;
            }
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.ZoomManager$8 */
    class C10078 implements OnTouchListener {
        C10078() {
        }

        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case 0:
                    CamLog.m3d(CameraConstants.TAG, "[jog] bottom button touch down");
                    ZoomManager.this.mJogZoomFactor = -1;
                    ZoomManager.this.moveJogZoom(12);
                    break;
                case 1:
                    CamLog.m3d(CameraConstants.TAG, "[jog] bottom button touch up");
                    ZoomManager.this.stopJogZoom();
                    break;
            }
            return true;
        }
    }

    public ZoomManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        initZoomBar();
        setJogZoomController(true);
    }

    public void onPauseBefore() {
        setZoomBarVisibility(8);
        setJogZoomController(false);
        if (this.mGet != null) {
            this.mGet.removePostRunnable(this.mHideJogZoomRunnable);
            this.mHideJogZoomRunnable = null;
            this.mGet.removePostRunnable(this.mJogZoomUpdate);
        }
        this.mCurJogZoomSpeed = 0;
        super.onPauseBefore();
    }

    public void initZoomBar() {
        if (!this.mIsInitialized) {
            this.mZoomBarView = this.mGet.inflateView(C0088R.layout.zoombar);
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            if (vg != null && this.mZoomBarView != null) {
                vg.addView(this.mZoomBarView, 0, new LayoutParams(-1, -1));
                this.mIsInitialized = true;
            } else {
                return;
            }
        }
        this.mZoomBar = (ZoomBar) this.mGet.findViewById(C0088R.id.zoom_bar);
        if (this.mZoomBar != null) {
            this.mZoomBar.initBar(this, 2);
            this.mZoomBar.setBarListener(this);
            CameraProxy cameraDevice = this.mGet.getCameraDevice();
            if (cameraDevice != null) {
                CameraParameters parameters = cameraDevice.getParameters();
                if (parameters != null) {
                    CamLog.m3d(CameraConstants.TAG, "initZoomBar getMaxZoom : " + parameters.getMaxZoom());
                    this.mZoomBar.setMaxValue(this.mZoomMaxValue);
                    this.mZoomBar.setBarValue(parameters.getZoom());
                }
            }
        }
    }

    public View getZoomBarView() {
        return this.mZoomBar;
    }

    public void initZoomBarValue(int zoomValue) {
        this.mZoomBar.setBarValue(zoomValue);
    }

    public int getSecondCameraMaxZoomLevel() {
        return ConfigurationHALUtil.sSECOND_CAMERA_MAX_ZOOM_LEVEL;
    }

    private void setJogZoomController(boolean set) {
        if (this.mGet != null && this.mGet.isRearCamera()) {
            if (set) {
                MiniActivity jogZoomLayout = getJogZoomLayout();
                if (jogZoomLayout != null) {
                    this.mJogZoomControlLayout = jogZoomLayout.findViewById(C0088R.id.jog_zoom_controller_rotate);
                    if (this.mJogZoomControlLayout != null) {
                        this.mMiniActivityZoomBar = (HorizontalSeekBar) this.mJogZoomControlLayout.findViewById(C0088R.id.mini_activity_zoom_bar);
                        if (this.mMiniActivityZoomBar != null) {
                            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.mMiniActivityZoomBar.getLayoutParams();
                            lp.width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.45f);
                            this.mMiniActivityZoomBar.setLayoutParams(lp);
                            this.mMiniActivityZoomBar.init(0, 18, 9);
                            this.mMiniActivityZoomBar.setOnVerticalSeekBarListener(new C10001());
                        }
                    } else {
                        return;
                    }
                }
                setJogZoomButtonListener();
                initJogZoomMinimap();
                return;
            }
            setJogZoomVisibility(false);
            this.mJogZoomTopBtnLayout = null;
            this.mJogZoomBottomBtnLayout = null;
            this.mMiniActivityZoomBar = null;
            if (this.mGet != null) {
                ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
                if (!(vg == null || this.mJogZoomMinimapLayout == null)) {
                    vg.removeView(this.mJogZoomMinimapLayout);
                    this.mJogZoomMinimapLayout = null;
                }
            }
            this.mJogZoomControlLayout = null;
            this.mJogZoomMinimap = null;
        }
    }

    private MiniActivity getJogZoomLayout() {
        if (!FunctionProperties.isSupportedConeUI()) {
            return null;
        }
        MiniActivity miniActivity = this.mGet.getMiniActivity();
        FrameLayout root = (FrameLayout) miniActivity.findViewById(C0088R.id.mini_activity_layout);
        if (root == null) {
            return null;
        }
        miniActivity.getLayoutInflater().inflate(C0088R.layout.mini_activity_jog_zoom, root);
        return miniActivity;
    }

    protected void initJogZoomMinimap() {
        if (this.mJogZoomMinimap == null && this.mJogZoomMinimapLayout == null) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    ViewGroup vg = (ViewGroup) ZoomManager.this.mGet.findViewById(C0088R.id.camera_controls);
                    ZoomManager.this.mJogZoomMinimapLayout = ZoomManager.this.mGet.inflateView(C0088R.layout.jog_zoom_minimap);
                    if (vg != null && ZoomManager.this.mJogZoomMinimapLayout != null) {
                        vg.addView(ZoomManager.this.mJogZoomMinimapLayout, 0);
                        ZoomManager.this.mJogZoomMinimap = (JogZoomMinimap) ZoomManager.this.mJogZoomMinimapLayout.findViewById(C0088R.id.jog_zoom_minimap_bar);
                        ZoomManager.this.setJogZoomMinimapLayout(new LayoutParams(2550, CameraConstantsEx.QHD_SCREEN_RESOLUTION));
                    }
                }
            });
            if (this.mHideJogZoomRunnable == null) {
                this.mHideJogZoomRunnable = new HandlerRunnable(this) {
                    public void handleRun() {
                        ZoomManager.this.setJogZoomMinimapVisibility(8);
                    }
                };
            }
        }
    }

    public void setJogZoomMinimapLayout(LayoutParams params) {
        if (this.mJogZoomMinimap != null && this.mJogZoomMinimapLayout != null) {
            int w = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.069f);
            LayoutParams lp = new LayoutParams(w, RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.128f));
            lp.addRule(21);
            lp.addRule(10);
            lp.rightMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.057f);
            lp.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, this.mGet.isUspZoneSupportedMode(this.mGet.getShotMode()) ? 0.561f : 0.651f);
            this.mJogZoomMinimapLayout.setLayoutParams(lp);
            int marginTop = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 5.0E-4f);
            if (this.mJogZoomMinimap != null) {
                int h = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.067f);
                LayoutParams lp2 = (LayoutParams) this.mJogZoomMinimap.getLayoutParams();
                lp2.width = w;
                lp2.height = h;
                lp2.topMargin = marginTop;
                this.mJogZoomMinimap.setLayoutParams(lp2);
            }
            rotateJogZoomText();
            if (this.mMinimapTextT != null) {
                LayoutParams tvLp = (LayoutParams) this.mMinimapTextT.getLayoutParams();
                tvLp.topMargin = marginTop;
                this.mMinimapTextT.setLayoutParams(tvLp);
            }
        }
    }

    private void rotateJogZoomText() {
        if (this.mGet != null) {
            LayoutParams params;
            CamLog.m3d(CameraConstants.TAG, "-jog- rotateJogZoomText");
            int degree = this.mGet.getOrientationDegree();
            this.mMinimapTextT = (RotateTextView) this.mGet.findViewById(C0088R.id.jog_zoom_minimap_text_t);
            this.mMinimapTextW = (RotateTextView) this.mGet.findViewById(C0088R.id.jog_zoom_minimap_text_w);
            int size = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.035f);
            if (this.mMinimapTextT != null) {
                params = (LayoutParams) this.mMinimapTextT.getLayoutParams();
                params.width = size;
                params.height = size;
                this.mMinimapTextT.setLayoutParams(params);
                this.mMinimapTextT.setDegree(degree, true);
            }
            if (this.mMinimapTextW != null) {
                params = (LayoutParams) this.mMinimapTextW.getLayoutParams();
                params.width = size;
                params.height = size;
                this.mMinimapTextW.setLayoutParams(params);
                this.mMinimapTextW.setDegree(degree, true);
            }
            if (this.mJogZoomControlLayout != null) {
                RotateImageButton jogZoomTopBtn = (RotateImageButton) this.mJogZoomControlLayout.findViewById(C0088R.id.jog_zoom_t_image_btn);
                if (jogZoomTopBtn != null) {
                    jogZoomTopBtn.setDegree(degree, true);
                }
                RotateImageButton jogZoomBottomBtn = (RotateImageButton) this.mJogZoomControlLayout.findViewById(C0088R.id.jog_zoom_w_image_btn);
                if (jogZoomBottomBtn != null) {
                    jogZoomBottomBtn.setDegree(degree, true);
                }
            }
        }
    }

    public void updateJogZoomMinimap() {
        if (this.mGet != null && this.mJogZoomMinimap != null) {
            this.mJogZoomMinimap.moveZoomLine(this.mZoomValue);
        }
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        this.mIsInitialized = false;
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (!(vg == null || this.mZoomBarView == null)) {
            unbindZoomBarView();
            vg.removeView(this.mZoomBarView);
            this.mZoomBarView = this.mGet.inflateView(C0088R.layout.zoombar);
            if (this.mZoomBarView != null) {
                vg.addView(this.mZoomBarView, 0, new LayoutParams(-1, -1));
                this.mIsInitialized = true;
                initZoomBar();
            }
        }
        setJogZoomController(false);
        setJogZoomController(true);
    }

    public void setDegree(int degree, boolean animation) {
        super.setDegree(degree, animation);
        rotateJogZoomText();
    }

    public void onDestroy() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                ViewGroup vg = (ViewGroup) ZoomManager.this.mGet.findViewById(C0088R.id.camera_controls);
                if (!(vg == null || ZoomManager.this.mZoomBarView == null)) {
                    vg.removeView(ZoomManager.this.mZoomBarView);
                    ZoomManager.this.mZoomBarView = null;
                }
                ZoomManager.this.unbindZoomBarView();
                if (ZoomManager.this.mRatioList != null) {
                    ZoomManager.this.mRatioList.clear();
                }
                if (ZoomManager.this.mJogZoomControlLayout != null) {
                    ZoomManager.this.mJogZoomControlLayout = null;
                }
            }
        });
        this.mZoomInterface = null;
        this.mIsInitialized = false;
        super.onDestroy();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if ((event.getActionMasked() & 255) != 1 || !isZoomBarVisible()) {
            return false;
        }
        resetBarDisappearTimer(2, 0);
        return true;
    }

    public void setZoomBarVisibility(int visibility) {
        if (this.mZoomInterface != null && this.mZoomBar != null && this.mZoomBar.getVisibility() != visibility) {
            if (visibility == 0) {
                this.mZoomBar.updateExtraInfo(Integer.toString(getZoomRatio()));
                this.mZoomBar.startRotation(getOrientationDegree(), false);
                if (checkZoomBarVisibilityCondition()) {
                    this.mZoomBar.setVisibility(visibility);
                    this.mZoomInterface.onZoomShow();
                    setJogZoomMinimapVisibility(8);
                    return;
                }
                return;
            }
            this.mZoomBar.setVisibility(visibility);
            this.mZoomInterface.onZoomHide();
        }
    }

    public boolean checkZoomBarVisibilityCondition() {
        if (this.mIsJogZoomWorking || this.mGet.isManualVideoAudioPopupShowing()) {
            return false;
        }
        return super.checkZoomBarVisibilityCondition();
    }

    public boolean skipShutterZoom(int aimValue) {
        return false;
    }

    public void stopSkipShutterZoom() {
    }

    public boolean moveJogZoom(int aimValue) {
        if (this.mGet == null) {
            return false;
        }
        if ((this.mLastSeekBarProgressValue == aimValue && this.mIsJogZoomWorking && !this.mResetJogSpeed) || isInAndOutSwithing()) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "[jog] moveJogZoom");
        if (this.mZoomBar != null) {
            this.mZoomBar.setVisibility(8);
        }
        if (!this.mIsJogZoomWorking) {
            startJogZoom();
        }
        this.mLastSeekBarProgressValue = aimValue;
        this.mIsJogZoomWorking = true;
        this.mResetJogSpeed = false;
        this.mGet.removePostRunnable(this.mJogZoomUpdate);
        this.mGet.runOnUiThread(this.mJogZoomUpdate);
        this.mGet.removePostRunnable(this.mHideJogZoomRunnable);
        return true;
    }

    protected void setZoomChangeCallback(ZoomChangeCallback zoomChangeCallback) {
        if (this.mGet.getCameraDevice() != null) {
            this.mGet.getCameraDevice().setZoomChangeCallback(zoomChangeCallback);
        }
    }

    protected void changeJogZoomValue(int value, int factor) {
        for (int i = 0; i < 1 && this.mAimToZoomValue != -1 && this.mGet != null && this.mGet.isJogZoomAvailable() && this.mIsJogZoomWorking; i++) {
            doZoomAction(value / 2, factor, 36, false, true);
        }
    }

    public void startJogZoom() {
        CamLog.m3d(CameraConstants.TAG, "[Jog] startJogZoom");
        if (this.mZoomInterface != null) {
            this.mZoomInterface.onZoomShow();
        }
        this.mResetJogSpeed = false;
        CamLog.m3d(CameraConstants.TAG, "[Jog] mResetJogSpeed = false");
        setZoomChangeCallback(this.mZoomChangeCallback);
    }

    public void stopJogZoom() {
        this.mJogZoomFactor = 0;
        this.mIsJogZoomWorking = false;
        this.mAimToZoomValue = -1;
        this.mCurJogZoomSpeed = 0;
        setZoomChangeCallback(null);
        setJogZoomMinimapVisibility(8);
        if (this.mGet != null) {
            this.mGet.removePostRunnable(this.mJogZoomUpdate);
            if (this.mZoomInterface != null) {
                this.mZoomInterface.onZoomHide();
            }
            if (this.mGet.getCameraDevice() != null) {
                if (this.mGet.checkModuleValidate(15)) {
                    CameraParameters param = this.mGet.getCameraDevice().getParameters();
                    param.set(ParamConstants.KEY_JOG_ZOOM, 0);
                    this.mGet.getCameraDevice().setParameters(param);
                    this.mZoomValue = this.mGet.getCameraDevice().getParameters().getZoom();
                    CamLog.m3d(CameraConstants.TAG, "mZoomValue = " + this.mZoomValue);
                    CamLog.m3d(CameraConstants.TAG, "[Jog] stopZogZoom, mZoomValue : " + this.mZoomValue);
                }
                notifySwitchingFinished();
            }
        }
    }

    protected void setJogZoomButtonListener() {
        if (this.mJogZoomControlLayout != null) {
            this.mJogZoomTopBtnLayout = this.mJogZoomControlLayout.findViewById(C0088R.id.jog_zoom_t_image_btn_layout);
            if (this.mJogZoomTopBtnLayout != null) {
                this.mJogZoomTopBtnLayout.setOnTouchListener(new C10067());
            }
            this.mJogZoomBottomBtnLayout = this.mJogZoomControlLayout.findViewById(C0088R.id.jog_zoom_w_image_btn_layout);
            if (this.mJogZoomBottomBtnLayout != null) {
                this.mJogZoomBottomBtnLayout.setOnTouchListener(new C10078());
            }
        }
    }

    public boolean isJogZoomMoving() {
        return this.mIsJogZoomWorking;
    }

    private int calZoomProgress(int currentProgress) {
        int gap = 9 - currentProgress;
        this.mJogZoomFactor = gap > 0 ? 1 : -1;
        this.mJogZoomStep = (int) Math.pow(2.0d, (double) Math.abs(gap));
        if (this.mJogZoomStep > this.JOG_ZOOM_MAX_STEP) {
            this.mJogZoomStep = this.JOG_ZOOM_MAX_STEP;
        } else if (this.mJogZoomStep < 4) {
            this.mJogZoomStep = 4;
        }
        int zoomValue = getZoomValue();
        int calZoom = gap > 0 ? zoomValue + this.mJogZoomStep : zoomValue - this.mJogZoomStep;
        if (this.mZoomMaxValue < calZoom) {
            return this.mZoomMaxValue;
        }
        if (calZoom < 0) {
            return 0;
        }
        return calZoom;
    }

    public void setJogZoomVisibility(boolean show) {
        if (FunctionProperties.isSupportedConeUI() && this.mGet != null && this.mJogZoomControlLayout != null) {
            CamLog.m3d(CameraConstants.TAG, "show JogZoom Controller : " + show);
            if (show) {
                this.mGet.showConeViewMode(false);
                this.mJogZoomControlLayout.setVisibility(0);
                return;
            }
            stopJogZoom();
            this.mGet.showConeViewMode(true);
            this.mJogZoomControlLayout.setVisibility(8);
            setJogZoomMinimapVisibility(8);
        }
    }

    public void setJogZoomMinimapVisibility(int visibility) {
        if (this.mJogZoomMinimapLayout != null && this.mGet != null) {
            this.mGet.onChangeZoomMinimapVisibility(visibility == 0);
            if (this.mJogZoomMinimapLayout.getVisibility() != visibility) {
                this.mJogZoomMinimapLayout.setVisibility(visibility);
            }
        }
    }

    public void setZoomMaxValue(int value) {
        super.setZoomMaxValue(value);
        if (this.mZoomBar != null) {
            this.mZoomBar.setMaxValue(this.mZoomMaxValue);
            CamLog.m3d(CameraConstants.TAG, "setMaxValue = " + this.mZoomMaxValue);
        } else {
            CamLog.m3d(CameraConstants.TAG, "mZoomBar's null");
        }
        if (this.mJogZoomMinimap != null) {
            this.mJogZoomMinimap.setMaxZoomValue(this.mZoomMaxValue);
        }
    }

    public void onGestureZoomStep(int gapSpan, int totalSpan) {
        if (!this.mIsJogZoomWorking && !this.mGet.isManualVideoAudioPopupShowing() && gapSpan != 0) {
            if (!this.mGet.checkModuleValidate(128)) {
                setJogZoomMinimapVisibility(8);
            }
            if (this.mZoomInterface != null && !this.mZoomInterface.isZoomAvailable()) {
                String toastMsg = this.mGet.getAppContext().getString(C0088R.string.volume_key_zoom_disable);
                if (this.mGet.isSlowMotionMode()) {
                    if (CameraConstants.MODE_MANUAL_VIDEO.equals(this.mGet.getShotMode())) {
                        toastMsg = this.mGet.getAppContext().getString(C0088R.string.volume_key_zoom_disable_resolution);
                    } else if (this.mGet.getCameraState() == 6 && !CameraConstants.MODE_SLOW_MOTION.equals(this.mGet.getShotMode())) {
                        toastMsg = this.mGet.getAppContext().getString(C0088R.string.volume_key_zoom_disable_resolution);
                    }
                }
                this.mGet.showToastConstant(toastMsg);
            } else if (this instanceof InAndOutZoomManager) {
                int gapSpanAbs = Math.abs(gapSpan) * this.mScaleFactor;
                int zoomStep = ((gapSpanAbs * gapSpanAbs) / this.mBaseSpan) + this.mMaxZoomScaleRatio;
                if (zoomStep >= this.mStepLimit) {
                    zoomStep = this.mStepLimit;
                }
                doZoomAction(gapSpan > 0 ? 1 : -1, zoomStep, gapSpan, false, false);
            } else {
                doGestureZoom(totalSpan);
            }
        }
    }

    public void doGestureZoom(int totalSpan) {
        int prevZoomValue = this.mZoomValue;
        setZoomValue(this.mPreZoomValue + ((int) (((float) totalSpan) * this.mPinchZoomFactor)));
        if (prevZoomValue != this.mZoomValue) {
            if (this.mZoomBar != null) {
                this.mZoomBar.setCursorValue(this.mZoomValue);
                this.mZoomBar.setCursor(this.mZoomValue);
                this.mZoomBar.updateExtraInfo(Integer.toString(getZoomRatio()));
                CamLog.m3d(CameraConstants.TAG, "[zoom] totalSpan : " + totalSpan);
            }
            if (this.mZoomInterface != null) {
                this.mZoomInterface.setZoomStep(this.mZoomValue, false, false, false);
            }
        }
    }

    public void onGestureZoomEnd() {
        this.mIsGestureZooming = false;
        if (!this.mGet.isManualVideoAudioPopupShowing()) {
            doZoomAction(0, 0, 0, true, false);
        }
    }

    public void setGestureZooming(boolean isStart) {
        this.mIsGestureZooming = isStart;
    }

    public void doZoomAction(int cursorStep, int factor, int gapSpan, boolean scaleEnd, boolean forJogZoom) {
        int curZoomValue = this.mZoomValue + (factor * cursorStep);
        if (this.mZoomValue != curZoomValue) {
            setZoomStep(cursorStep, factor, scaleEnd, forJogZoom, curZoomValue);
            if (this.mZoomBar != null) {
                this.mZoomBar.setCursorValue(this.mZoomValue);
                this.mZoomBar.setCursor(this.mZoomValue);
                this.mZoomBar.updateExtraInfo(Integer.toString(getZoomRatio()));
            }
        }
    }

    protected void setZoomStep(int cursorStep, int factor, boolean scaleEnd, boolean forJogZoom, int zoomValue) {
        if (this.mZoomInterface != null) {
            if (forJogZoom) {
                int newSpeed = factor * cursorStep;
                if (this.mCurJogZoomSpeed != newSpeed) {
                    this.mCurJogZoomSpeed = newSpeed;
                    this.mZoomInterface.setZoomStep(this.mCurJogZoomSpeed, scaleEnd, false, true);
                }
                CameraProxy cameraDevice = this.mGet.getCameraDevice();
                if (cameraDevice != null) {
                    cameraDevice.refreshParameters();
                    if (FunctionProperties.getSupportedHal() != 2) {
                        CameraParameters param = cameraDevice.getParameters();
                        if (param != null) {
                            setZoomValue(param.getZoom());
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            this.mZoomInterface.setZoomStep(zoomValue, scaleEnd, false, false);
            setZoomValue(zoomValue);
        }
    }

    public void onKeyZoomInOut(int factor, boolean bKeyUp) {
        try {
            CamLog.m3d(CameraConstants.TAG, "factor = " + factor + ", scaleEnd = " + bKeyUp);
            if (this.mZoomInterface != null) {
                this.mZoomInterface.setZoomStep(factor, bKeyUp, true, false);
            }
            if (bKeyUp) {
                this.mGet.postOnUiThread(this.mZoomHide, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                this.mHideZoomPosted = true;
            } else if (this.mHideZoomPosted) {
                this.mGet.removePostRunnable(this.mZoomHide);
                this.mHideZoomPosted = false;
            }
        } catch (NullPointerException e) {
            CamLog.m12w(CameraConstants.TAG, "NullPointerException:", e);
        }
    }

    public void stopZoomRepeat() {
        if (this.mZoomBar != null) {
            this.mZoomBar.updateBarWithTimer(0, false, false, true);
            this.mZoomBar.stopTimerTask();
        }
    }

    public boolean setBarSetting(String key, String value, boolean save) {
        this.mZoomValue = Integer.parseInt(value);
        if (this.mZoomBar != null) {
            this.mZoomBar.updateExtraInfo(Integer.toString(getZoomRatio()));
        }
        if (this.mZoomInterface != null) {
            this.mZoomInterface.setZoomStep(this.mZoomValue, false, false, false);
        }
        return false;
    }

    public boolean isZoomControllersMoving() {
        if ((this.mZoomBar == null || !this.mZoomBar.isBarTouched()) && !isJogZoomMoving()) {
            return false;
        }
        return true;
    }

    public boolean isZoomControllersGetTouched() {
        if (this.mZoomBar == null || !this.mZoomBar.isBarTouched()) {
            return this.mIsJogZoomTouched;
        }
        return true;
    }

    public boolean isZoomBarVisible() {
        if (this.mZoomBar == null || this.mZoomBar.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    public void setRotateDegree(int degree, boolean animation) {
        super.setRotateDegree(degree, animation);
        final int mDegree = degree;
        final boolean mAnimation = animation;
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (ZoomManager.this.mZoomBar != null) {
                    ZoomManager.this.mZoomBar.startRotation(mDegree, mAnimation);
                }
            }
        });
    }

    public void setZoomBarLayoutMarginEnd(int marginEnd) {
        View zoomBarParentLayout = this.mGet.findViewById(C0088R.id.zoom_bar_layout);
        if (zoomBarParentLayout != null) {
            LayoutParams rl = (LayoutParams) zoomBarParentLayout.getLayoutParams();
            if (rl != null) {
                rl.bottomMargin = marginEnd;
                zoomBarParentLayout.setLayoutParams(rl);
            }
        }
    }

    public void setRecordingZoomBtnPosition(boolean manualMode) {
    }

    public void initZoomValues(CameraParameters param) {
        setZoomMaxValue(param.getMaxZoom());
        setZoomRatioList(param.getZoomRatios());
    }

    public void updateExtraInfo(String value) {
        if (this.mZoomBar != null) {
            this.mZoomBar.setCursorValue(this.mZoomValue);
            this.mZoomBar.setCursor(this.mZoomValue);
            this.mZoomBar.updateExtraInfo(value);
        }
    }
}
