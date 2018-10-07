package com.lge.camera.managers;

import android.content.res.Configuration;
import android.media.Image;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.GestureGuideBox;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.managers.GestureShutterManagerIF.OnGestureRecogListener;
import com.lge.camera.managers.GestureShutterManagerIF.onGestureUIListener;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import com.lge.gestureshot.library.GestureEngine;
import com.lge.gestureshot.library.GestureEngine.GestureCallBack;
import com.lge.gestureshot.library.HandInfo;
import com.lge.gestureshot.library.MotionEngineProcessor;
import com.lge.gestureshot.library.MotionEngineProcessor.MotionCallback;
import java.nio.ByteBuffer;

public class GestureShutterManager extends GestureShutterManagerIF {
    public static final int GESTURETYPE_FIST = 1;
    public static final int GESTURETYPE_HAND = 0;
    private final float GESTURE_GUIDE_IMAGE_MARGIN_START = 0.015f;
    private final float GESTURE_GUIDE_MARGIN_TOP = 0.208333f;
    private final float GESTURE_GUIDE_MARGIN_TOP_MODE_SQUARE = 0.156f;
    private final float GESTURE_GUIDE_STRING_FONT_SIZE = 0.022f;
    private final float GESTURE_GUIDE_STRING_MARGIN_START = 0.002f;
    private final float GESTURE_SHOT_ONLY_GUIDE_MARGIN_TOP_ADD = 0.15f;
    private int mCaptureEventType = 0;
    private int mDegree = 0;
    private HandInfo mDetectedHandInfo = new HandInfo();
    private int mFontSize = 0;
    private View mGestureGuideRotateView = null;
    private View mGestureGuideView = null;
    private OnGestureRecogListener mGestureRecogListener = null;
    private onGestureUIListener mGestureUIListener = null;
    private ImageView mGuideImage = null;
    private ImageView mGuideImage2 = null;
    private RotateTextView mGuideString = null;
    private RotateTextView mGuideString2 = null;
    private View mGuideStringLayout = null;
    private RotateLayout mGuideTextViewLayout = null;
    private GestureGuideBox mHandGuideBox = null;
    private boolean mIsSupportIntervaShot = false;
    private MotionEngineProcessor mMotionEngine = null;
    private int mPreviewBufferSize = 0;
    private Size mPreviewSize = null;
    private int mTranHeight = 0;
    private int mTranMinX = 0;
    private int mTranMinY = 0;
    private int mTranWidth = 0;

    private class GestureRegEngineCallback implements GestureCallBack {
        private GestureRegEngineCallback() {
        }

        /* synthetic */ GestureRegEngineCallback(GestureShutterManager x0, C09431 x1) {
            this();
        }

        public void onGestureEngineErrorCallback(int errorType) {
            CamLog.m3d(CameraConstants.TAG, "onGestureEngineErrorCallback()");
        }

        public void onGestureEngineDrawCallback(HandInfo handInfo) {
            if (GestureShutterManager.this.mDetectedHandInfo != null) {
                GestureShutterManager.this.mDetectedHandInfo.setHandInfo(handInfo);
                if (GestureShutterManager.this.isAvailableGestureShutter()) {
                    GestureShutterManager.this.showGestureGuide();
                    if (GestureShutterManager.this.mGestureUIListener != null) {
                        GestureShutterManager.this.mGestureUIListener.onShowGestureGuide();
                    }
                    GestureShutterManager.this.mGet.getActivity().runOnUiThread(new HandlerRunnable(GestureShutterManager.this) {
                        public void handleRun() {
                            GestureShutterManager.this.drawHandGuideRect(GestureShutterManager.this.mDetectedHandInfo);
                        }
                    });
                    return;
                }
                GestureShutterManager.this.hideGestureGuide();
            }
        }

        public void onGestureEngineEventCallback(int eventType) {
            if (GestureShutterManager.this.mDetectedHandInfo == null || GestureShutterManager.this.mHandGuideBox == null || !GestureShutterManager.this.isAvailableGestureShutter()) {
                GestureShutterManager.this.hideGestureGuide();
            } else if (GestureShutterManager.this.isAvailableIntervalShot() || eventType != 3) {
                switch (eventType) {
                    case 2:
                        CamLog.m3d(CameraConstants.TAG, "GESTURE_ENGINE_CAPTURE_EVENT");
                        GestureShutterManager.this.hideGestureGuide();
                        if (GestureShutterManager.this.mGestureUIListener != null) {
                            GestureShutterManager.this.mGestureUIListener.onHideGestureGuide();
                        }
                        GestureShutterManager.this.mHandGuideBox.invalidate();
                        GestureShutterManager.this.stopGestureEngine();
                        GestureShutterManager.this.mCaptureEventType = 1;
                        GestureShutterManager.this.executeTimershot();
                        LdbUtil.setShutterType(CameraConstants.SHUTTER_TYPE_GESTURE);
                        return;
                    case 3:
                        CamLog.m3d(CameraConstants.TAG, "GESTURE_ENGINE_INTERVALSHOT_EVENT");
                        GestureShutterManager.this.hideGestureGuide();
                        if (GestureShutterManager.this.mGestureUIListener != null) {
                            GestureShutterManager.this.mGestureUIListener.onHideGestureGuide();
                        }
                        GestureShutterManager.this.mHandGuideBox.invalidate();
                        GestureShutterManager.this.stopGestureEngine();
                        GestureShutterManager.this.mCaptureEventType = 2;
                        GestureShutterManager.this.executeTimershot();
                        LdbUtil.setShutterType(CameraConstants.SHUTTER_TYPE_GESTURE_INTERVAL);
                        return;
                    case 4:
                        if (GestureShutterManager.this.mHandGuideBox.getVisibility() == 0) {
                            GestureShutterManager.this.hideGestureGuide();
                            if (GestureShutterManager.this.mGestureUIListener != null) {
                                GestureShutterManager.this.mGestureUIListener.onHideGestureGuide();
                                return;
                            }
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        }
    }

    private class MotionRegEngineCallback implements MotionCallback {
        private MotionRegEngineCallback() {
        }

        /* synthetic */ MotionRegEngineCallback(GestureShutterManager x0, C09431 x1) {
            this();
        }

        public void onMotionEnginePullCallback() {
            CamLog.m3d(CameraConstants.TAG, "onMotionEnginePullCallback call onShowQuickView()");
            if (GestureShutterManager.this.mGestureRecogListener != null) {
                GestureShutterManager.this.mGestureRecogListener.onShowQuickView();
            }
        }

        public void onMotionEnginePushCallback() {
            CamLog.m3d(CameraConstants.TAG, "onMotionEnginePushCallback call onHideQuickView()");
            if (GestureShutterManager.this.mGestureRecogListener != null) {
                GestureShutterManager.this.mGestureRecogListener.onHideQuickView();
            }
        }
    }

    public GestureShutterManager(ModuleInterface moduleInterface, boolean isSupportIntervalShot) {
        boolean z = false;
        super(moduleInterface);
        if (FunctionProperties.isSupportedFrontIntervalShot() && isSupportIntervalShot) {
            z = true;
        }
        this.mIsSupportIntervaShot = z;
    }

    public void setGestureRecogEngineListener(OnGestureRecogListener listener) {
        this.mGestureRecogListener = listener;
    }

    public void setGestureUIListener(onGestureUIListener listener) {
        this.mGestureUIListener = listener;
    }

    public void onResumeBefore() {
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        this.mCaptureEventType = 0;
        initLayout();
    }

    public void runGestureEngine(boolean useOnlyMotionQuickview) {
        boolean enableEngine;
        if (useOnlyMotionQuickview) {
            enableEngine = isAvailableMotionQuickView();
        } else {
            enableEngine = isAvailableGestureShutterStarted();
        }
        if (enableEngine) {
            initGestureEngine();
            initMotionEngine();
            startGestureEngine();
            return;
        }
        stopGestureEngine();
        releaseGestureEngine();
    }

    public void onPauseAfter() {
        this.mCaptureEventType = 0;
        stopGestureEngine();
        releaseGestureEngine();
        releaseLayout();
        super.onPauseAfter();
    }

    public void onConfigurationChanged(Configuration config) {
        releaseLayout();
        initLayout();
        hideGestureGuide();
        super.onConfigurationChanged(config);
    }

    public void setDegree(int degree, boolean animation) {
        if (!(this.mHandGuideBox == null || this.mDegree == degree)) {
            hideGestureGuide();
            CamLog.m3d(CameraConstants.TAG, "setDegree " + degree);
            this.mHandGuideBox.setDegree(degree);
            this.mDegree = degree;
        }
        if (this.mGuideTextViewLayout != null) {
            this.mGuideTextViewLayout.setAngle(degree);
        }
        super.setDegree(degree, animation);
    }

    public void initLayout() {
        if (this.mGestureGuideView == null) {
            CamLog.m3d(CameraConstants.TAG, "initLayout");
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            this.mGestureGuideView = this.mGet.inflateView(C0088R.layout.gestureshutter);
            this.mGestureGuideRotateView = this.mGestureGuideView.findViewById(C0088R.id.gestureshutter_rotate);
            this.mGuideStringLayout = this.mGestureGuideView.findViewById(C0088R.id.gestureshutter_guide_layout);
            LayoutParams lp = (LayoutParams) this.mGuideStringLayout.getLayoutParams();
            lp.setMarginStart(RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.015f));
            this.mGuideStringLayout.setLayoutParams(lp);
            this.mGuideString = (RotateTextView) this.mGestureGuideView.findViewById(C0088R.id.gestureshutter_guide_string);
            LinearLayout.LayoutParams tv1Param = (LinearLayout.LayoutParams) this.mGuideString.getLayoutParams();
            tv1Param.setMarginStart(RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.002f));
            this.mGuideString.setLayoutParams(tv1Param);
            this.mGuideImage = (ImageView) this.mGestureGuideView.findViewById(C0088R.id.gestureshutter_guide_image);
            this.mGuideString2 = (RotateTextView) this.mGestureGuideView.findViewById(C0088R.id.gestureshutter_guide_string2);
            this.mGuideString2.setLayoutParams(tv1Param);
            this.mGuideImage2 = (ImageView) this.mGestureGuideView.findViewById(C0088R.id.gestureshutter_guide_image2);
            this.mGuideTextViewLayout = (RotateLayout) this.mGestureGuideView.findViewById(C0088R.id.gestureshutter_guide_text_layout);
            if (!(vg == null || this.mGestureGuideView == null || this.mGestureGuideView.getParent() != null)) {
                vg.addView(this.mGestureGuideView, 0, new LayoutParams(-1, -1));
            }
            setGuideText(0);
            setRotateDegree(getOrientationDegree(), false);
            View previewFrame = this.mGet.getPreviewFrameLayout();
            if (previewFrame != null) {
                this.mHandGuideBox = (GestureGuideBox) previewFrame.findViewById(C0088R.id.hand_shutter_view);
                if (this.mHandGuideBox != null) {
                    this.mHandGuideBox.setVisibility(4);
                    this.mHandGuideBox.init();
                    this.mHandGuideBox.setInitialDegree(this.mGet.getOrientationDegree());
                }
            }
            this.mDegree = this.mGet.getOrientationDegree();
        }
    }

    private void setGuideText(int GestureType) {
        String guideText = "";
        this.mFontSize = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.022f);
        if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
            this.mFontSize = (int) (((float) this.mFontSize) * 0.9f);
            this.mGuideString.setTextSize(this.mFontSize);
            this.mGuideString2.setTextSize(this.mFontSize);
        }
        if (this.mIsSupportIntervaShot) {
            if (GestureType == 0) {
                this.mGuideString.setText(String.format(getAppContext().getString(C0088R.string.gesture_single_guide), new Object[]{Integer.valueOf(1)}));
                this.mGuideImage.setImageResource(C0088R.drawable.shutter_hand_gesture_icon);
                this.mGuideString2.setText(String.format(getAppContext().getString(C0088R.string.gesture_multiple_guide), new Object[]{Integer.valueOf(4)}));
                this.mGuideImage2.setImageResource(C0088R.drawable.shutter_hand_gesture_jamjam_shot);
            } else {
                this.mGuideString.setText(String.format(getAppContext().getString(C0088R.string.gesture_single_guide), new Object[]{Integer.valueOf(1)}));
                this.mGuideImage.setImageResource(C0088R.drawable.shutter_hand_gesture_icon_02);
                this.mGuideString2.setText(String.format(getAppContext().getString(C0088R.string.gesture_multiple_guide), new Object[]{Integer.valueOf(4)}));
                this.mGuideImage2.setImageResource(C0088R.drawable.shutter_hand_gesture_jamjam_shot_02);
            }
        } else if (GestureType == 0) {
            this.mGuideString.setText(getAppContext().getString(C0088R.string.gesture_guide));
            this.mGuideImage.setImageResource(C0088R.drawable.shutter_hand_gesture_icon);
        } else {
            this.mGuideString.setText(getAppContext().getString(C0088R.string.sp_gesture_guide_msg_fist));
            this.mGuideImage.setImageResource(C0088R.drawable.shutter_hand_gesture_icon_02);
        }
        if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
            this.mGuideImage.setScaleX(0.9f);
            this.mGuideImage.setScaleY(0.9f);
            this.mGuideImage2.setScaleX(0.9f);
            this.mGuideImage2.setScaleY(0.9f);
            if (this.mGestureGuideView != null) {
                ((LinearLayout.LayoutParams) this.mGestureGuideView.findViewById(C0088R.id.gestureshutter_guide_view2).getLayoutParams()).setMarginStart(0);
            }
        }
    }

    public void releaseLayout() {
        CamLog.m3d(CameraConstants.TAG, "releaseLayout");
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (vg != null && this.mGestureGuideView != null) {
            vg.removeView(this.mGestureGuideView);
            if (this.mHandGuideBox != null) {
                this.mHandGuideBox.unbind();
                this.mHandGuideBox = null;
            }
            this.mGestureGuideRotateView = null;
            this.mGestureGuideView = null;
            this.mGuideImage = null;
            this.mGuideString = null;
            this.mGuideStringLayout = null;
            this.mGuideImage2 = null;
            this.mGuideString2 = null;
            this.mGuideTextViewLayout = null;
        }
    }

    public void initGestureEngine() {
        if (GestureEngine.GetEngineStatus() == 0) {
            int multiModeForFistPalm;
            GestureEngine.GestureCreate();
            int multiModeForPalmFist = this.mIsSupportIntervaShot ? 2 : 1;
            if (this.mIsSupportIntervaShot) {
                multiModeForFistPalm = 2;
            } else {
                multiModeForFistPalm = 1;
            }
            GestureEngine.SetGestureMode(multiModeForPalmFist, multiModeForFistPalm, this.mIsSupportIntervaShot ? 1.0f : 0.0f);
            CamLog.m3d(CameraConstants.TAG, "initGestureEngine");
        }
        this.mPreviewSize = null;
    }

    public void initMotionEngine() {
        if (this.mMotionEngine == null) {
            CamLog.m3d(CameraConstants.TAG, "initMotionEngine");
            this.mMotionEngine = new MotionEngineProcessor(this.mGet.getAppContext(), new MotionRegEngineCallback(this, null));
        }
    }

    public void showGestureGuide() {
        if (this.mGuideStringLayout != null && this.mGuideString != null && this.mGuideImage != null && this.mGuideString2 != null && this.mGuideImage2 != null && this.mHandGuideBox != null && this.mGet.checkModuleValidate(240) && GestureEngine.GetEngineStatus() == 3) {
            setRotateDegree(getOrientationDegree(), false);
            if (this.mDetectedHandInfo != null) {
                setGuideText(this.mDetectedHandInfo.mGestureType);
            }
            setGestureGuideVisibility(0);
        }
    }

    public void hideGestureGuide() {
        setGestureGuideVisibility(8);
    }

    private void setGestureGuideVisibility(final int visibility) {
        this.mGet.getActivity().runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                int i = 8;
                if (GestureShutterManager.this.mGuideStringLayout != null && GestureShutterManager.this.mHandGuideBox != null) {
                    if (!GestureShutterManager.this.mIsSupportIntervaShot || GestureShutterManager.this.mGet.isAttachIntent()) {
                        if (GestureShutterManager.this.mGuideString2.getVisibility() != 8) {
                            GestureShutterManager.this.mGuideString2.setTextSize(GestureShutterManager.this.mFontSize);
                            GestureShutterManager.this.mGuideString2.setVisibility(8);
                        }
                        if (GestureShutterManager.this.mGuideImage2.getVisibility() != 8) {
                            GestureShutterManager.this.mGuideImage2.setVisibility(8);
                        }
                    } else {
                        if (GestureShutterManager.this.mGuideString2.getVisibility() != visibility) {
                            GestureShutterManager.this.mGuideString2.setTextSize(GestureShutterManager.this.mFontSize);
                            GestureShutterManager.this.mGuideString2.setVisibility(visibility);
                        }
                        if (GestureShutterManager.this.mGuideImage2.getVisibility() != visibility) {
                            GestureShutterManager.this.mGuideImage2.setVisibility(visibility);
                        }
                    }
                    GestureShutterManager gestureShutterManager = GestureShutterManager.this;
                    if (!GestureShutterManager.this.mGet.isSelfieOptionVisible()) {
                        i = visibility;
                    }
                    gestureShutterManager.setGestureTextGuideVisibility(i);
                    if (GestureShutterManager.this.mHandGuideBox.getVisibility() != visibility) {
                        GestureShutterManager.this.mHandGuideBox.setVisibility(visibility);
                    }
                }
            }
        });
    }

    public void setGestureTextGuideVisibility(int visibility) {
        if (this.mGuideStringLayout.getVisibility() != visibility) {
            this.mGuideString.setTextSize(this.mFontSize);
            this.mGuideString2.setTextSize(this.mFontSize);
            CamLog.m3d(CameraConstants.TAG, visibility == 0 ? "showGestureGuide" : "hideGestureGuide");
            this.mGuideStringLayout.setVisibility(visibility);
            if (visibility == 0) {
                AudioUtil.performHapticFeedback(this.mGuideStringLayout, 65576);
            }
        }
    }

    public boolean getGesutreGuideVisibility() {
        if (this.mHandGuideBox != null && this.mHandGuideBox.getVisibility() == 0) {
            return true;
        }
        return false;
    }

    public void startGestureEngine() {
        if (GestureEngine.GetEngineStatus() != 0 && GestureEngine.GetEngineStatus() != 3) {
            GestureEngine.AddCallBack(new GestureRegEngineCallback(this, null));
            GestureEngine.GestureStart();
            CamLog.m3d(CameraConstants.TAG, "startGestureEngine");
        }
    }

    public void stopGestureEngine() {
        if (GestureEngine.GetEngineStatus() == 3) {
            GestureEngine.GestureStop();
            GestureEngine.ClearCallBack();
            CamLog.m3d(CameraConstants.TAG, "stopGestureEngine");
        }
        hideGestureGuide();
        if (this.mGestureUIListener != null) {
            this.mGestureUIListener.onHideGestureGuide();
        }
    }

    public void resetGuideRectArea() {
        if (this.mHandGuideBox != null) {
            this.mTranHeight = 0;
            this.mTranWidth = 0;
            this.mTranMinX = 0;
            this.mTranMinY = 0;
            this.mHandGuideBox.setPreviewSize(this.mGet.getAppContext(), 0, 0);
            this.mHandGuideBox.setRectangleArea(0, 0, 0, 0);
        }
    }

    public void releaseGestureEngine() {
        if (GestureEngine.GetEngineStatus() != 0) {
            if (this.mMotionEngine != null) {
                this.mMotionEngine.Stop();
            }
            GestureEngine.GestureRelease();
            CamLog.m3d(CameraConstants.TAG, "releaseGestureEngine");
        }
    }

    public void startMotionEngine() {
        if (this.mMotionEngine != null) {
            this.mMotionEngine.Start();
            CamLog.m3d(CameraConstants.TAG, "startMotionEngine");
        }
    }

    public void resumePushMotionEngine() {
        if (this.mMotionEngine != null) {
            this.mMotionEngine.ResumePush();
            CamLog.m3d(CameraConstants.TAG, "resumePushMotionEngine");
        }
    }

    public void stopMotionEngine() {
        if (this.mMotionEngine != null) {
            this.mMotionEngine.Stop();
            CamLog.m3d(CameraConstants.TAG, "stopMotionEngine");
        }
    }

    private void drawHandGuideRect(HandInfo handInfo) {
        if (this.mHandGuideBox != null && handInfo != null) {
            convertCoordinate(handInfo);
            if (this.mPreviewSize != null) {
                this.mHandGuideBox.setPreviewSize(this.mGet.getAppContext(), this.mPreviewSize.getWidth(), this.mPreviewSize.getHeight());
                this.mHandGuideBox.setRectangleArea(this.mTranMinX, this.mTranMinY, this.mTranWidth, this.mTranHeight);
            }
        }
    }

    private void convertCoordinate(HandInfo handInfo) {
        int tmpOrientation = getOrientation();
        this.mTranMinX = handInfo.mMinX;
        this.mTranMinY = handInfo.mMinY;
        this.mTranWidth = handInfo.mWidth;
        this.mTranHeight = handInfo.mHeight;
        if (this.mPreviewSize != null) {
            int previewWidth;
            int previewHeight;
            if (tmpOrientation == 1 || tmpOrientation == 3) {
                previewWidth = this.mPreviewSize.getWidth();
                previewHeight = this.mPreviewSize.getHeight();
            } else {
                previewHeight = this.mPreviewSize.getWidth();
                previewWidth = this.mPreviewSize.getHeight();
            }
            switch (tmpOrientation) {
                case 0:
                    if (this.mGet.getDualviewType() >= 4) {
                        this.mTranMinY -= 200;
                        return;
                    }
                    return;
                case 1:
                    this.mTranMinX = (previewHeight - (handInfo.mMinY + handInfo.mHeight)) - 1;
                    this.mTranMinY = handInfo.mMinX;
                    if (this.mGet.getDualviewType() >= 4) {
                        this.mTranMinY -= 250;
                    }
                    this.mTranWidth = handInfo.mHeight;
                    this.mTranHeight = handInfo.mWidth;
                    return;
                case 2:
                    this.mTranMinX = (previewWidth - (handInfo.mMinX + handInfo.mWidth)) - 1;
                    this.mTranMinY = (previewHeight - (handInfo.mMinY + handInfo.mHeight)) - 1;
                    if (this.mGet.getDualviewType() >= 4) {
                        this.mTranMinY -= 200;
                    }
                    this.mTranWidth = handInfo.mWidth;
                    this.mTranHeight = handInfo.mHeight;
                    return;
                case 3:
                    this.mTranMinX = handInfo.mMinY;
                    this.mTranMinY = (previewWidth - (handInfo.mMinX + handInfo.mWidth)) - 1;
                    if (this.mGet.getDualviewType() >= 4) {
                        this.mTranMinY -= 200;
                    }
                    this.mTranWidth = handInfo.mHeight;
                    this.mTranHeight = handInfo.mWidth;
                    return;
                default:
                    return;
            }
        }
    }

    public int getGestureEngineStatus() {
        return GestureEngine.GetEngineStatus();
    }

    private void executeTimershot() {
        CamLog.m3d(CameraConstants.TAG, "executeTimershot");
        if (this.mHandGuideBox != null) {
            this.mHandGuideBox.setVisibility(4);
        }
        if (this.mGestureRecogListener != null) {
            this.mGestureRecogListener.doTimershotByGestureRecog(this.mCaptureEventType);
        }
    }

    private boolean isAvailableGestureShutter() {
        return (!this.mGet.checkModuleValidate(16) || this.mGet.isTimerShotCountdown() || this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_FULL_SCREEN_GUIDE) || !this.mGet.getSettingMenuEnable(Setting.KEY_SHUTTERLESS_SELFIE) || this.mGet.isRotateDialogVisible()) ? false : true;
    }

    public void onPreviewFrame(final byte[] data, CameraProxy camera) {
        if (FunctionProperties.isSupportedGestureShot() && data != null) {
            this.mGet.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (GestureShutterManager.this.getGestureEngineStatus() != 4 && GestureShutterManager.this.getGestureEngineStatus() != 0 && GestureShutterManager.this.mGet.checkModuleValidate(240)) {
                        if (GestureShutterManager.this.mPreviewSize == null || GestureShutterManager.this.mPreviewBufferSize == 0) {
                            CamLog.m11w(CameraConstants.TAG, "Wrong preview size. return onPreviewFrame");
                        } else if (data.length != GestureShutterManager.this.mPreviewBufferSize) {
                            CamLog.m3d(CameraConstants.TAG, "Preview data received don't match preview size!!!actual size = " + data.length + "  expected size " + GestureShutterManager.this.mPreviewBufferSize);
                        } else {
                            GestureEngine.GesturePutPreviewFrame(data, GestureShutterManager.this.getOrientation(), GestureShutterManager.this.mPreviewSize.getWidth(), GestureShutterManager.this.mPreviewSize.getHeight(), 6);
                        }
                    }
                }
            });
        }
    }

    public void onImageData(Image image) {
        if (FunctionProperties.isSupportedGestureShot() && image != null && getGestureEngineStatus() != 4 && getGestureEngineStatus() != 0 && this.mGet.checkModuleValidate(240)) {
            if (this.mPreviewSize == null || this.mPreviewBufferSize == 0) {
                CamLog.m11w(CameraConstants.TAG, "Wrong preview size. return onImageData");
                return;
            }
            int nOrientation = getOrientation();
            ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
            yBuffer.rewind();
            ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();
            int strides = image.getPlanes()[0].getRowStride();
            int data_length = (int) (((float) (image.getWidth() * image.getHeight())) * 1.5f);
            if (data_length != this.mPreviewBufferSize) {
                CamLog.m3d(CameraConstants.TAG, "Preview data received don't match preview size!!!actual size = " + data_length + "  expected size " + this.mPreviewBufferSize);
            } else {
                GestureEngine.GesturePutPreviewFrameByteBufferNV21(yBuffer, vBuffer, strides, nOrientation, image.getWidth(), image.getHeight(), 6);
            }
        }
    }

    public void setPreviewSize(Size previewSize) {
        CamLog.m7i(CameraConstants.TAG, "for gesture shot, setPreviewSize : " + previewSize);
        this.mPreviewSize = previewSize;
        if (this.mPreviewSize == null) {
            CamLog.m11w(CameraConstants.TAG, "mPreviewSize is null. return set mPreviewBufferSize.");
        } else {
            this.mPreviewBufferSize = (int) (((double) (this.mPreviewSize.getWidth() * this.mPreviewSize.getHeight())) * 1.5d);
        }
    }

    private int getOrientation() {
        switch (this.mGet.getOrientationDegree()) {
            case 90:
                return 3;
            case 180:
                return 2;
            case 270:
                return 1;
            default:
                return 0;
        }
    }

    public void setRotateDegree(int degree, boolean animation) {
        boolean isNotch = false;
        if (this.mGestureGuideRotateView != null) {
            int bottomMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.gestureshutter_guide_bottom_margin);
            int bottomMarginVer = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.362f);
            LayoutParams guideParams = (LayoutParams) this.mGestureGuideRotateView.getLayoutParams();
            Utils.resetLayoutParameter(guideParams);
            guideParams.setMarginStart(0);
            guideParams.setMarginEnd(0);
            guideParams.topMargin = 0;
            guideParams.bottomMargin = 0;
            switch (degree) {
                case 0:
                case 180:
                    guideParams.addRule(14, 1);
                    guideParams.addRule(12, 1);
                    if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                        bottomMarginVer = (this.mGet.getAppContext().getDrawable(C0088R.drawable.btn_selfie_tone_normal).getIntrinsicHeight() * 2) + Utils.getLCDsize(this.mGet.getAppContext(), true)[1];
                    } else if (CameraConstants.MODE_FRONT_OUTFOCUS.equals(this.mGet.getShotMode())) {
                        if (ModelProperties.getLCDType() == 2) {
                            isNotch = true;
                        }
                        int outfocusBottom = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, isNotch ? 0.315f : 0.289f);
                        if (bottomMarginVer < this.mGet.getOutfocusErrorTextHeight() + outfocusBottom) {
                            bottomMarginVer = outfocusBottom + this.mGet.getOutfocusErrorTextHeight();
                        }
                    }
                    guideParams.bottomMargin = bottomMarginVer;
                    break;
                case 90:
                case 270:
                    guideParams.addRule(20, 1);
                    if (CameraConstants.MODE_FRONT_OUTFOCUS.equals(this.mGet.getShotMode())) {
                        bottomMargin += this.mGet.getOutfocusErrorTextHeight();
                    }
                    guideParams.setMarginStart(bottomMargin);
                    if (!this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                        guideParams.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.208333f);
                        if (!isAvailableIntervalShot()) {
                            guideParams.topMargin += RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.15f);
                            break;
                        }
                    }
                    guideParams.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.156f);
                    break;
                    break;
            }
            this.mGestureGuideRotateView.setLayoutParams(guideParams);
            ((RotateLayout) this.mGestureGuideRotateView).rotateLayout(degree);
        }
    }

    public boolean isAvailableGestureShutterStarted() {
        String curValue = this.mGet.getSettingValue(Setting.KEY_SHUTTERLESS_SELFIE);
        String shotMode = this.mGet.getSettingValue(Setting.KEY_MODE);
        if (((this.mGet.isRearCamera() || !"mode_normal".equals(shotMode)) && !shotMode.contains(CameraConstants.MODE_SQUARE)) || !this.mGet.checkModuleValidate(128) || this.mGet.isVideoCaptureMode() || !FunctionProperties.isSupportedGestureShot() || ((FunctionProperties.isShutterlessSupported(this.mGet.getAppContext()) && !CameraConstants.GESTURESHOT.equals(curValue)) || this.mGet.isIntervalShotProgress())) {
            return false;
        }
        return true;
    }

    public boolean isAvailableMotionQuickView() {
        if (!FunctionProperties.isSupportedMotionQuickView() || this.mGet.isRearCamera() || !this.mGet.checkModuleValidate(128) || this.mGet.isVideoCaptureMode() || this.mGet.isAttachIntent() || !"on".equals(this.mGet.getSettingValue(Setting.KEY_MOTION_QUICKVIEWER))) {
            return false;
        }
        return true;
    }

    public boolean isAvailableIntervalShot() {
        return isAvailableGestureShutterStarted() && !this.mGet.isAttachIntent() && !this.mGet.isVideoCaptureMode() && this.mIsSupportIntervaShot;
    }

    public int getGestureCaptureType() {
        return this.mCaptureEventType;
    }

    public void resetGestureCaptureType() {
        this.mCaptureEventType = 0;
    }
}
