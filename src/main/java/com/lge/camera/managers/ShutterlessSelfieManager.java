package com.lge.camera.managers;

import android.content.res.Configuration;
import android.hardware.Camera.Face;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.components.ShutterlessFaceView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.FaceCommon;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import com.lge.shutterlessshot.library.ShutterlessEngine;
import com.lge.shutterlessshot.library.ShutterlessEngine.StatusListener;
import com.lge.shutterlessshot.library.TiltDetector;
import java.util.Timer;
import java.util.TimerTask;

public class ShutterlessSelfieManager extends ManagerInterfaceImpl {
    private final int MSG_CANCEL_SHUTTER = 2;
    private final int MSG_START_INSTANT_SHUTTER = 1;
    private final float SHUTTERLESS_GUIDE_TEXT_HORIZONTAL_MARGIN = 0.0486f;
    private final float SHUTTERLESS_GUIDE_TEXT_MARGINTOP = 0.2657f;
    private final float SHUTTERLESS_GUIDE_TEXT_MARGINTOP_SQUARE = 0.196f;
    private final int SHUTTERLESS_STATUS_INIT_AND_IN_MOTION = 1;
    private final int SHUTTERLESS_STATUS_MOTION_STOP_FOR_INSTANT_SHUTTER = 5;
    private final int SHUTTERLESS_STATUS_REST = 0;
    private final int START_SHUTTER_DELAY = 200;
    private final int STATUS_CANCEL_TIMER_AND_IN_MOTION = 3;
    private ShutterlessFaceView mFaceView;
    private RelativeLayout mFrame;
    private final Handler mHandler = new C11532();
    private boolean mIsFirstShot = true;
    private OnShutterlessSelfieListener mListener;
    private View mPreviewView;
    private ShutterlessEngine mShutterlessEngine;
    private View mShutterlessGuideLayout = null;
    private RotateLayout mShutterlessGuideRotateLayout = null;
    private RotateTextView mShutterlessGuideTextView = null;
    private final StatusListener mStatusListener = new C11521();
    private TimerTask mTask;
    private TiltDetector mTiltDetector;
    private Timer mTimer;

    public interface OnShutterlessSelfieListener {
        void showFaceViewVisible(boolean z);

        void stopShutterlessSelfie();

        boolean takeShutterlessSelfie();
    }

    /* renamed from: com.lge.camera.managers.ShutterlessSelfieManager$1 */
    class C11521 implements StatusListener {
        C11521() {
        }

        public void onStatusChanged(int status) {
            switch (status) {
                case 1:
                    if (ShutterlessSelfieManager.this.mShutterlessEngine.isLocked()) {
                        ShutterlessSelfieManager.this.mShutterlessEngine.unlock();
                        return;
                    }
                    return;
                case 3:
                    if (ShutterlessSelfieManager.this.mHandler != null) {
                        ShutterlessSelfieManager.this.mHandler.removeMessages(2);
                        ShutterlessSelfieManager.this.mHandler.sendEmptyMessage(2);
                        return;
                    }
                    return;
                case 5:
                    ShutterlessSelfieManager.this.lockEngine();
                    if (ShutterlessSelfieManager.this.mHandler != null) {
                        ShutterlessSelfieManager.this.mHandler.removeMessages(1);
                        ShutterlessSelfieManager.this.mHandler.sendEmptyMessageDelayed(1, 200);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ShutterlessSelfieManager$2 */
    class C11532 extends Handler {
        C11532() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (ShutterlessSelfieManager.this.mListener != null) {
                switch (msg.what) {
                    case 1:
                        if (!ShutterlessSelfieManager.this.mListener.takeShutterlessSelfie()) {
                            ShutterlessSelfieManager.this.unlockEngine();
                            return;
                        }
                        return;
                    case 2:
                        ShutterlessSelfieManager.this.mListener.stopShutterlessSelfie();
                        return;
                    default:
                        return;
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ShutterlessSelfieManager$4 */
    class C11554 extends TimerTask {
        C11554() {
        }

        public void run() {
            if (ShutterlessSelfieManager.this.mShutterlessEngine != null && ShutterlessSelfieManager.this.isEnginPaused()) {
                CamLog.m3d(CameraConstants.TAG, "-sh- resume shutterless by Timer");
                ShutterlessSelfieManager.this.mGet.resumeShutterless();
            }
        }
    }

    public ShutterlessSelfieManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
        if (this.mGet != null && FunctionProperties.isShutterlessSupported(this.mGet.getAppContext())) {
            createTimerTask();
            this.mTimer = new Timer();
        }
    }

    public void init() {
        SensorManager manager = (SensorManager) this.mGet.getAppContext().getSystemService(ParamConstants.VALUE_BINNING_SENSOR);
        this.mShutterlessEngine = new ShutterlessEngine(manager);
        this.mShutterlessEngine.registerListener(this.mStatusListener);
        this.mTiltDetector = new TiltDetector(manager);
        super.init();
    }

    public void setListener(OnShutterlessSelfieListener listener) {
        this.mListener = listener;
    }

    private void initFaceView() {
        this.mPreviewView = this.mGet.findViewById(C0088R.id.preview_surface_view);
        this.mFrame = (RelativeLayout) this.mGet.findViewById(C0088R.id.frame);
        if (this.mFaceView == null) {
            this.mFaceView = new ShutterlessFaceView(this.mGet.getAppContext(), this.mGet);
            if (this.mPreviewView != null && this.mFrame != null && this.mFaceView.getParent() == null) {
                this.mFaceView.setDegree(this.mGet.getOrientationDegree());
                this.mFrame.addView(this.mFaceView, 0, new LayoutParams(-1, -1));
                this.mFaceView.mIsFirstDraw = true;
                setIsFaceDetected(false);
                CamLog.m7i(CameraConstants.TAG, "mFaceView is attached");
            }
        }
    }

    public void setIsFirstShot(boolean isFirst) {
        this.mIsFirstShot = isFirst;
    }

    public boolean getIsFirstShot() {
        return this.mIsFirstShot;
    }

    public void setIsFaceDetected(boolean isDetected) {
        if (this.mFaceView != null) {
            this.mFaceView.setIsFaceDetected(isDetected);
        }
    }

    private void initGuideView() {
        if (this.mGet != null) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            this.mShutterlessGuideLayout = this.mGet.inflateView(C0088R.layout.shutterless_guide);
            if (this.mShutterlessGuideLayout != null) {
                if (vg != null) {
                    vg.addView(this.mShutterlessGuideLayout, 2, new LayoutParams(-1, -1));
                    if (this.mShutterlessGuideRotateLayout == null) {
                        this.mShutterlessGuideRotateLayout = (RotateLayout) this.mShutterlessGuideLayout.findViewById(C0088R.id.shutterless_guide_rotate_layout);
                        if (this.mShutterlessGuideRotateLayout != null) {
                            this.mShutterlessGuideTextView = (RotateTextView) this.mShutterlessGuideRotateLayout.findViewById(C0088R.id.shutterless_guide_string1);
                            if (this.mShutterlessGuideTextView != null) {
                                this.mShutterlessGuideTextView.setText(getAppContext().getResources().getString(C0088R.string.shutterless_guide_text));
                            }
                        }
                    }
                }
                setGuideTextLayoutParam(this.mGet.getOrientationDegree());
            }
        }
    }

    private void releaseLayout() {
        CamLog.m7i(CameraConstants.TAG, "-sh- mFaceView is released");
        if (this.mFrame != null) {
            this.mFrame.removeView(this.mFaceView);
            this.mFrame = null;
        }
        if (this.mFaceView != null) {
            this.mFaceView.clearFaceCoordinate();
            this.mFaceView.setVisibility(8);
            this.mFaceView.releaseLayout();
            this.mFaceView = null;
        }
        if (this.mListener != null) {
            this.mListener.showFaceViewVisible(false);
        }
        this.mShutterlessGuideLayout = null;
        this.mShutterlessGuideRotateLayout = null;
        if (this.mShutterlessGuideTextView != null) {
            this.mShutterlessGuideTextView.setVisibility(8);
            this.mShutterlessGuideTextView = null;
        }
    }

    private void processFaceBox(FaceCommon[] faces) {
        if (faces != null) {
            this.mFaceView.drawFaceBox(faces, this.mPreviewView);
        }
    }

    public boolean isFaceViewVisible() {
        if (this.mFaceView != null) {
            return this.mFaceView.getVisibility() == 0;
        } else {
            return false;
        }
    }

    public void setFaceVisibility(final boolean visibility) {
        if (this.mGet != null) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    int i = 0;
                    if (ShutterlessSelfieManager.this.mIsFirstShot) {
                        ShutterlessSelfieManager.this.setGuideVisibility(visibility);
                    }
                    if (ShutterlessSelfieManager.this.mFaceView != null) {
                        if (visibility == (ShutterlessSelfieManager.this.mFaceView.getVisibility() != 0)) {
                            if (!visibility) {
                                ShutterlessSelfieManager.this.mFaceView.clearFaceCoordinate();
                            }
                            CamLog.m3d(CameraConstants.TAG, "-sh- setFaceVisibility visibility = " + visibility);
                            ShutterlessFaceView access$400 = ShutterlessSelfieManager.this.mFaceView;
                            if (!visibility) {
                                i = 4;
                            }
                            access$400.setVisibility(i);
                            ShutterlessSelfieManager.this.mListener.showFaceViewVisible(visibility);
                        }
                    }
                }
            });
        }
    }

    public void setGuideVisibility(boolean visibility) {
        int i = 0;
        if (this.mShutterlessGuideTextView != null) {
            if (visibility == (this.mShutterlessGuideTextView.getVisibility() != 0) && this.mGet != null) {
                if (visibility) {
                    setGuideTextLayoutParam(this.mGet.getOrientationDegree());
                }
                RotateTextView rotateTextView = this.mShutterlessGuideTextView;
                if (!visibility) {
                    i = 4;
                }
                rotateTextView.setVisibility(i);
            }
        }
    }

    private void setGuideTextLayoutParam(int degree) {
        if (this.mShutterlessGuideRotateLayout != null && this.mShutterlessGuideTextView != null && this.mGet != null) {
            LayoutParams lp = (LayoutParams) this.mShutterlessGuideRotateLayout.getLayoutParams();
            Utils.resetLayoutParameter(lp);
            int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
            boolean isSquareMode = this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE);
            int marginBottomPx = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.362f);
            switch (Utils.convertDegree(this.mGet.getAppContext().getResources(), degree)) {
                case 0:
                case 180:
                    lp.addRule(12);
                    lp.addRule(14);
                    lp.setMarginStart(RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0486f));
                    lp.setMarginEnd(RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0486f));
                    lp.bottomMargin = marginBottomPx;
                    if (isSquareMode) {
                        lp.bottomMargin = lcdSize[1] + RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.111f);
                        break;
                    }
                    break;
                case 90:
                case 270:
                    lp.addRule(20);
                    lp.setMarginStart(Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.shutterless_guide_marginBottom_land));
                    if (CameraConstants.MODE_SQUARE_OVERLAP.equals(this.mGet.getShotMode())) {
                        lp.setMarginStart(RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.096f));
                    }
                    if (!isSquareMode) {
                        lp.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.2657f);
                        break;
                    } else {
                        lp.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.196f);
                        break;
                    }
            }
            this.mShutterlessGuideRotateLayout.setLayoutParams(lp);
            this.mShutterlessGuideRotateLayout.setAngle(this.mGet.getOrientationDegree());
        }
    }

    public void onFaceDetection(FaceCommon[] faces) {
        if (faces != null && this.mShutterlessEngine != null) {
            if (isTilting()) {
                if (faces.length != 0) {
                    setFaceVisibility(true);
                    processFaceBox(faces);
                }
                Face[] faceCamera1 = new Face[faces.length];
                for (int i = 0; i < faces.length; i++) {
                    faceCamera1[i] = new Face();
                    faceCamera1[i].id = faces[i].getId();
                    faceCamera1[i].leftEye = faces[i].getLeftEye();
                    faceCamera1[i].rightEye = faces[i].getRightEye();
                    faceCamera1[i].mouth = faces[i].getMouth();
                    faceCamera1[i].rect = faces[i].getRect();
                    faceCamera1[i].score = faces[i].getScore();
                }
                this.mShutterlessEngine.onFaceDetection(faceCamera1);
                return;
            }
            this.mShutterlessEngine.onFaceDetection(null);
            setFaceVisibility(false);
        }
    }

    public void unlockEngine() {
        if (this.mListener != null && this.mShutterlessEngine != null) {
            if (this.mShutterlessEngine.isLocked()) {
                CamLog.m3d(CameraConstants.TAG, "-sh- unlock ShutterlessEngine");
                this.mShutterlessEngine.unlock();
            } else if (this.mShutterlessEngine.isPaused()) {
                this.mShutterlessEngine.unpause();
            }
        }
    }

    public void lockEngine() {
        if (this.mListener != null && this.mShutterlessEngine != null && !this.mShutterlessEngine.isLocked()) {
            CamLog.m3d(CameraConstants.TAG, "-sh- lock ShutterlessEngine");
            this.mShutterlessEngine.lock();
        }
    }

    public void resetShutterlessEngine() {
        if (this.mShutterlessEngine != null) {
            if (this.mShutterlessEngine.getStatus() != 0 || this.mShutterlessEngine.getStatus() != 1) {
                this.mShutterlessEngine.reset();
            }
        }
    }

    public void onTakePicture() {
        if (this.mShutterlessEngine != null && this.mShutterlessEngine.getStatus() == 5 && this.mShutterlessEngine.isLocked()) {
            this.mShutterlessEngine.onTakePicture();
            if (this.mIsFirstShot) {
                this.mIsFirstShot = false;
                setGuideVisibility(false);
                setFaceVisibility(false);
            }
        }
    }

    public void startShutterlessEngin() {
        if (this.mGet != null && this.mGet.getCameraId() == 1 && !this.mGet.isIntervalShotProgress() && !this.mGet.isVideoCaptureMode() && this.mShutterlessEngine != null) {
            cancelTimerTask();
            if (!this.mShutterlessEngine.isRunning()) {
                CamLog.m3d(CameraConstants.TAG, "-sh- startShutterlessEngin");
                this.mShutterlessEngine.start(this.mGet.getOrientationDegree());
                initFaceView();
                if (this.mIsFirstShot) {
                    initGuideView();
                }
            }
            if (this.mShutterlessEngine.isPaused()) {
                this.mShutterlessEngine.unpause();
            }
            if (this.mShutterlessEngine.isLocked()) {
                this.mShutterlessEngine.unlock();
            }
            if (this.mFaceView != null) {
                this.mFaceView.clearFaceCoordinate();
            }
            startTiltDetector();
        }
    }

    public void stopShutterlessEngin() {
        if (this.mShutterlessEngine != null) {
            cancelTimerTask();
            if (this.mShutterlessEngine.isRunning()) {
                CamLog.m3d(CameraConstants.TAG, "-sh- stopShutterlessEngin");
                this.mShutterlessEngine.stop();
                setFaceVisibility(false);
                setIsFaceDetected(false);
                setGuideVisibility(false);
                this.mHandler.removeMessages(1);
            }
            stopTiltDetector();
        }
    }

    private void createTimerTask() {
        this.mTask = new C11554();
    }

    public void cancelTimerTask() {
        if (this.mTimer != null) {
            this.mTimer.cancel();
            if (this.mTask != null) {
                this.mTask.cancel();
                this.mTask = null;
            }
        }
    }

    public void pauseEngine() {
        setFaceVisibility(false);
        if (isEnginRunning() && !isEnginPaused()) {
            CamLog.m3d(CameraConstants.TAG, "-sh- pause Shutterless Engine");
            if (this.mHandler != null) {
                this.mHandler.removeMessages(1);
            }
            this.mShutterlessEngine.pause();
            if (this.mTimer != null) {
                this.mTimer.cancel();
                if (this.mTask != null) {
                    this.mTask.cancel();
                    this.mTask = null;
                }
            }
            createTimerTask();
            this.mTimer = new Timer();
            if (this.mTask != null) {
                this.mTimer.schedule(this.mTask, CameraConstants.TOAST_LENGTH_SHORT, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
            }
        }
    }

    public void resumeEngine() {
        if (this.mShutterlessEngine != null) {
            cancelTimerTask();
            if (this.mShutterlessEngine.isPaused()) {
                CamLog.m3d(CameraConstants.TAG, "-sh- resume Shutterless Engine");
                this.mShutterlessEngine.unpause();
                this.mShutterlessEngine.reset();
            }
        }
    }

    public boolean isEnginPaused() {
        if (this.mShutterlessEngine != null) {
            return this.mShutterlessEngine.isPaused();
        }
        return false;
    }

    public boolean isEnginRunning() {
        if (this.mShutterlessEngine != null) {
            return this.mShutterlessEngine.isRunning();
        }
        return false;
    }

    public void onPauseBefore() {
        stopShutterlessEngin();
        releaseLayout();
        super.onPauseBefore();
        this.mIsFirstShot = true;
    }

    public void onConfigurationChanged(Configuration config) {
        if (this.mShutterlessEngine != null && this.mShutterlessEngine.isRunning()) {
            releaseLayout();
            initFaceView();
            initGuideView();
        }
        super.onConfigurationChanged(config);
    }

    public void setDegree(int degree, boolean animation) {
        if (this.mShutterlessEngine != null) {
            this.mShutterlessEngine.setDegree(degree);
        }
        if (this.mFaceView != null) {
            this.mFaceView.setDegree(degree);
            this.mFaceView.invalidate();
        }
        setGuideTextLayoutParam(degree);
        super.setDegree(degree, animation);
    }

    public void onDestroy() {
        super.onDestroy();
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (!(vg == null || this.mShutterlessGuideLayout == null)) {
            vg.removeView(this.mShutterlessGuideLayout);
            this.mShutterlessGuideLayout = null;
        }
        releaseLayout();
        this.mListener = null;
        this.mFaceView = null;
        this.mFrame = null;
        this.mPreviewView = null;
        cancelTimerTask();
        this.mTimer = null;
        this.mTiltDetector = null;
    }

    public void setIsBlingAniStarted(final boolean show) {
        if (this.mGet != null) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (ShutterlessSelfieManager.this.mFaceView != null) {
                        ShutterlessSelfieManager.this.mFaceView.setBlingAnimation(show);
                    }
                }
            });
        }
    }

    private void startTiltDetector() {
        if (this.mTiltDetector != null) {
            CamLog.m3d(CameraConstants.TAG, "-sh- start TiltDetector");
            this.mTiltDetector.start();
        }
    }

    private void stopTiltDetector() {
        if (this.mTiltDetector != null) {
            CamLog.m3d(CameraConstants.TAG, "-sh- stop TiltDetector");
            this.mTiltDetector.stop();
        }
    }

    private boolean isTilting() {
        if (this.mTiltDetector == null) {
            return false;
        }
        return this.mTiltDetector.isTilting();
    }
}
