package com.lge.camera.app.ext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.FrameLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.app.BeautyShotCameraModule;
import com.lge.camera.app.Module;
import com.lge.camera.app.VideoRecorder;
import com.lge.camera.app.ext.MultiViewRenderer.GLFrameCaptureListener;
import com.lge.camera.app.ext.MultiViewRenderer.STAvailableListener;
import com.lge.camera.app.ext.MultiViewRenderer.STAvailableListenerCollage;
import com.lge.camera.components.MultiViewLayout;
import com.lge.camera.components.MultiViewLayoutInfoQuad;
import com.lge.camera.components.MultiViewLayoutInfoQuadFrontRearWide;
import com.lge.camera.components.MultiViewLayoutInfoSingle;
import com.lge.camera.components.MultiViewLayoutInfoSplit;
import com.lge.camera.components.MultiViewLayoutInfoSplitFrontRearWide;
import com.lge.camera.components.MultiViewLayoutInfoTriple1;
import com.lge.camera.components.MultiViewLayoutInfoTriple1FrontRearWide;
import com.lge.camera.components.MultiViewLayoutInfoTriple2;
import com.lge.camera.components.MultiViewLayoutInfoTriple2FrontRearWide;
import com.lge.camera.components.MultiViewLayoutQuad;
import com.lge.camera.components.MultiViewLayoutSingle;
import com.lge.camera.components.MultiViewLayoutSplit;
import com.lge.camera.components.MultiViewLayoutSplitForSplice;
import com.lge.camera.components.MultiViewLayoutTriple1;
import com.lge.camera.components.MultiViewLayoutTriple2;
import com.lge.camera.components.SpliceLayoutInfoSplitFrontRearWide;
import com.lge.camera.components.SpliceLayoutInfoSplitFrontRearWideMidTier;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.CameraSecondHolder;
import com.lge.camera.device.ICameraCallback.CameraPreviewDataCallback;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamUpdater;
import com.lge.camera.device.ParamUtils;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.DebugUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.MDMUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.TelephonyUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MultiGLViewBase extends BeautyShotCameraModule implements IMultiViewModule {
    public static final int LDB_MULTIVIEW_LAYOUT_QUAD = 13;
    public static final int LDB_MULTIVIEW_LAYOUT_SPLIT = 10;
    public static final int LDB_MULTIVIEW_LAYOUT_TRIPLE1 = 11;
    public static final int LDB_MULTIVIEW_LAYOUT_TRIPLE2 = 12;
    public static final String MULTIVIEW_RECORDING_FPS_RANGE = "24000,24000";
    protected static final String PREVIEW_SIZE_16BY9_WIDE = "2560x1440";
    private static CountDownLatch sLatch = null;
    protected static CountDownLatch sLatch2 = null;
    protected final String FRONT_PREVIEW_FPS_RANGE = "12000,24000";
    protected final int LDB_MULTIVIEW_CAPTURE = 0;
    protected final String LDB_MULTIVIEW_CAPTURE_TXT = CameraConstants.VOLUME_KEY_CAPTURE;
    protected final int LDB_MULTIVIEW_CHANGECAMERA = 4;
    protected final String LDB_MULTIVIEW_CHANGECAMERA_TXT = "change_camera";
    public final int LDB_MULTIVIEW_EXTCAM_CAPTURE = 31;
    public final String LDB_MULTIVIEW_EXTCAM_CAPTURE_TXT = "extcam_capture";
    public final int LDB_MULTIVIEW_EXTCAM_CONNECTION = 30;
    public final String LDB_MULTIVIEW_EXTCAM_CONNECTION_TXT = "extcam_connection";
    public final int LDB_MULTIVIEW_EXTCAM_VIDEO = 32;
    public final String LDB_MULTIVIEW_EXTCAM_VIDEO_TXT = "extcam_video";
    protected final int LDB_MULTIVIEW_INTERVAL_CAPTURE = 2;
    protected final String LDB_MULTIVIEW_INTERVAL_CAPTURE_TXT = "interval_capture";
    protected final int LDB_MULTIVIEW_INTERVAL_VIDEO = 3;
    protected final String LDB_MULTIVIEW_INTERVAL_VIDEO_TXT = "interval_video";
    public final int LDB_MULTIVIEW_SEQUENTIAL_MODE = 20;
    public final String LDB_MULTIVIEW_SEQUENTIAL_MODE_TXT = "sequential_mode";
    public final int LDB_MULTIVIEW_SIMUL_CAPTURE = 22;
    public final String LDB_MULTIVIEW_SIMUL_CAPTURE_TXT = "simultaneous_capture";
    public final int LDB_MULTIVIEW_SIMUL_MODE = 21;
    public final String LDB_MULTIVIEW_SIMUL_MODE_TXT = "simultaneous_mode";
    public final int LDB_MULTIVIEW_SIMUL_VIDEO = 23;
    public final String LDB_MULTIVIEW_SIMUL_VIDEO_TXT = "simultaneous_video";
    protected final int LDB_MULTIVIEW_VIDEO = 1;
    protected final String LDB_MULTIVIEW_VIDEO_TXT = "video";
    protected final String MULTIVIEW_ALLSHOT_VIDEO_SIZE = "1280x720";
    protected final String MULTIVIEW_FRAME_VIDEO_SIZE_FRONT = "1280x960";
    protected final String MULTIVIEW_FRAME_VIDEO_SIZE_REAR = "1280x720";
    protected final String MULTIVIEW_IMAGE_SIZE_FINAL = (this.mLcdSize[0] + "x" + this.mLcdSize[1]);
    protected final String MULTIVIEW_VIDEO_SIZE_FINAL = "1920x1080";
    protected final String PICTURE_SIZE_16BY9 = "1280x720";
    protected final String PICTURE_SIZE_4BY3 = "1280x960";
    protected final String PREVIEW_SIZE_16BY9 = "1280x720";
    protected final String PREVIEW_SIZE_1BY1 = "1080x1080";
    protected final String PREVIEW_SIZE_4BY3 = "1280x960";
    protected final String PREVIEW_SIZE_4BY3_LOW_RESOLUTION = "960x720";
    protected final String REAR_PREVIEW_FPS_RANGE = "8000,24000";
    protected final String SPLICE_VIDEO_SIZE_FINAL = "2880x1440";
    protected final int STATE_MULTIVIEW_COLLAGE_IMAGE_ON = 8;
    protected final int STATE_MULTIVIEW_COLLAGE_RECORD_ON = 4;
    protected final int STATE_MULTIVIEW_IDLE = 1;
    protected final int STATE_MULTIVIEW_INTERVAL_SHOT_IMAGE_ON = 16;
    protected final int STATE_MULTIVIEW_INTERVAL_SHOT_VIDEO_ON = 32;
    protected final int STATE_MULTIVIEW_RECORD_STOP = 2;
    protected final int VIDEO_FPS_FOR_CREAT_SURFACE = 30;
    protected boolean mBeautyOn = false;
    protected int mBeautyStrength = 0;
    protected int mCameraDegree = 0;
    protected CountDownLatch mCollageSTLatch;
    private GLFrameCaptureListener mFrameCaptureListener = new C03884();
    protected Handler mHolderHandler = new Handler();
    protected boolean mIsCameraAlreadyOpened = false;
    protected boolean mIsImportedImage = false;
    protected boolean mIsMultiviewRecording = false;
    private boolean mIsStopTimershotCountdown = false;
    protected boolean mIsStoppingRecord = false;
    protected boolean mLayoutRecreated = false;
    protected int[] mLcdSize = Utils.getLCDsize(this.mGet.getAppContext(), true);
    protected ArrayList<MultiViewLayout> mMultiViewLayoutList = null;
    protected MultiViewFrameBase mMultiviewFrame;
    protected boolean mMultiviewFrameReady = false;
    protected MultiViewRenderer mRenderThread;
    private STAvailableListener mSTAvailableListener = new C03905();
    private STAvailableListenerCollage mSTAvailableListenerCollage = new C03916();
    protected CountDownLatch mSTLatch;
    protected int mState = 1;
    protected SurfaceTexture[] mSurfaceTexture;
    protected SurfaceTexture[] mSurfaceTextureCollage;
    private SurfaceTextureListener mSurfaceTextureListenerMV = new C03862();
    protected TextureView mTextureViewMV = null;
    protected int mTouchedView = 0;
    protected boolean mUseDualRecorder = false;
    protected SurfaceTexture sSurfaceTextureMV = null;

    /* renamed from: com.lge.camera.app.ext.MultiGLViewBase$2 */
    class C03862 implements SurfaceTextureListener {
        C03862() {
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            CamLog.m3d(CameraConstants.TAG, "MultiView - onSurfaceTextureDestroyed");
            if (CameraSecondHolder.isSecondCameraOpened()) {
                CameraSecondHolder.subinstance().stopPreview();
                CameraSecondHolder.subinstance().release();
            }
            if (MultiGLViewBase.this.sSurfaceTextureMV != null) {
                MultiGLViewBase.this.sSurfaceTextureMV.release();
                MultiGLViewBase.this.sSurfaceTextureMV = null;
            }
            return true;
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "MultiView - onSurfaceTextureAvailable");
            MultiGLViewBase.this.sSurfaceTextureMV = surface;
            if (MultiGLViewBase.sLatch != null) {
                MultiGLViewBase.sLatch.countDown();
                CamLog.m3d(CameraConstants.TAG, "MultiView - mLatch countdown mLatch = " + MultiGLViewBase.sLatch);
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "MultiView - onSurfaceTextureAvailable mLatch is null");
        }
    }

    /* renamed from: com.lge.camera.app.ext.MultiGLViewBase$4 */
    class C03884 implements GLFrameCaptureListener {
        C03884() {
        }

        public void onGLFrameCaptured(final byte[] jpegData) {
            MultiGLViewBase.this.mGet.postOnUiThread(new HandlerRunnable(MultiGLViewBase.this) {
                public void handleRun() {
                    CamLog.m3d(CameraConstants.TAG, "MultiView - onGLFrameCaptured");
                    MultiGLViewBase.this.mSnapShotChecker.setPictureCallbackState(2);
                    Module.sFirstTaken = true;
                    MultiGLViewBase.this.access$200(jpegData, 0, false);
                    DebugUtil.setStartTime("[+] MV : updateThumbnail");
                    MultiGLViewBase.this.access$300(jpegData, null);
                    if (MultiGLViewBase.this.checkModuleValidate(128)) {
                        if (MultiGLViewBase.this.mCaptureButtonManager != null) {
                            if (!MultiGLViewBase.this.mGet.isLGUOEMCameraIntent()) {
                                MultiGLViewBase.this.mCaptureButtonManager.setShutterButtonEnable(true, MultiGLViewBase.this.getShutterButtonType());
                            }
                            if (TelephonyUtil.phoneInCall(MultiGLViewBase.this.mGet.getAppContext()) || !MDMUtil.allowMicrophone()) {
                                MultiGLViewBase.this.mCaptureButtonManager.setShutterButtonEnable(false, 1);
                            }
                        }
                        if (!"on".equals(MultiGLViewBase.this.getSettingValue(Setting.KEY_VOICESHUTTER))) {
                            AudioUtil.setAudioFocus(MultiGLViewBase.this.getAppContext(), false);
                        }
                        if ((MultiGLViewBase.this.getMVState() & 32) != 0) {
                            MultiGLViewBase.this.setMVState(33);
                        } else if ((MultiGLViewBase.this.getMVState() & 16) != 0) {
                            MultiGLViewBase.this.setMVState(17);
                        } else {
                            MultiGLViewBase.this.setMVState(1);
                            MultiGLViewBase.this.setQuickClipIcon(false, false);
                        }
                        if (MultiGLViewBase.this.mRenderThread != null && (MultiGLViewBase.this.mGet.isPaused() || MultiGLViewBase.this.mRenderThread.isRequiredToFinishAfterSaving())) {
                            MultiGLViewBase.this.mRenderThread.stopThread();
                            CamLog.m3d(CameraConstants.TAG, "-th- stopThread mRenderThread = null");
                            MultiGLViewBase.this.release();
                        }
                        MultiGLViewBase.this.multiviewMakingCollageEnd();
                        MultiGLViewBase.this.onTakePictureAfter();
                    }
                }
            });
        }

        public Bitmap onPreGLFrameCapture(Bitmap bitmap) {
            if (MultiGLViewBase.this.isSignatureEnableCondition() && CameraConstants.MODE_SQUARE_SPLICE.equals(MultiGLViewBase.this.getShotMode())) {
                return MultiGLViewBase.this.mGet.composeSignatureImage(bitmap, MultiGLViewBase.this.mCameraDegree);
            }
            return bitmap;
        }
    }

    /* renamed from: com.lge.camera.app.ext.MultiGLViewBase$5 */
    class C03905 implements STAvailableListener {
        C03905() {
        }

        public void onSurfaceTextureReady(SurfaceTexture[] st) {
            CamLog.m3d(CameraConstants.TAG, "MultiView - onSurfaceTextureReady");
            MultiGLViewBase.this.mSurfaceTexture = st;
            if (MultiGLViewBase.this.mSTLatch != null) {
                MultiGLViewBase.this.mSTLatch.countDown();
            }
        }
    }

    /* renamed from: com.lge.camera.app.ext.MultiGLViewBase$6 */
    class C03916 implements STAvailableListenerCollage {
        C03916() {
        }

        public void onSurfaceTextureReady(SurfaceTexture[] st) {
            CamLog.m3d(CameraConstants.TAG, "MultiView - onSurfaceTextureReady - Collage");
            MultiGLViewBase.this.mSurfaceTextureCollage = st;
            if (MultiGLViewBase.this.mCollageSTLatch != null) {
                MultiGLViewBase.this.mCollageSTLatch.countDown();
            }
        }
    }

    /* renamed from: com.lge.camera.app.ext.MultiGLViewBase$7 */
    class C03927 implements CameraPreviewDataCallback {
        C03927() {
        }

        public void onPreviewFrame(byte[] data, CameraProxy camera) {
            if (!MultiGLViewBase.this.islistenerRegisteredAfterOneShotCallback()) {
                CamLog.m3d(CameraConstants.TAG, "setListenerRegister");
                MultiGLViewBase.this.access$1200();
            }
            if (MultiGLViewBase.this.mGestureShutterManager != null) {
                MultiGLViewBase.this.mGestureShutterManager.runGestureEngine(false);
            }
        }
    }

    public MultiGLViewBase(ActivityBridge activityBridge) {
        super(activityBridge);
        if (ModelProperties.isMTKChipset()) {
            this.mUseDualRecorder = true;
            CamLog.m3d(CameraConstants.TAG, "MTK model : mUseDualRecorder  true");
        }
    }

    public void onPauseBefore() {
        CamLog.m3d(CameraConstants.TAG, "MultiView - onPauseBefore");
        if (sLatch != null) {
            sLatch.countDown();
            CamLog.m3d(CameraConstants.TAG, "MultiView - sLatch countdown");
        }
        this.mIsCameraAlreadyOpened = false;
        super.onPauseBefore();
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        if (sLatch2 != null) {
            sLatch2.countDown();
            CamLog.m3d(CameraConstants.TAG, "MultiView - sLatch2 countdown");
            sLatch2 = null;
        }
    }

    public void release() {
        CamLog.m3d(CameraConstants.TAG, "Multiview - release");
        this.mRenderThread = null;
        if (this.mSurfaceTexture != null) {
            for (int i = 0; i < this.mSurfaceTexture.length; i++) {
                if (this.mSurfaceTexture[i] != null) {
                    this.mSurfaceTexture[i].release();
                    this.mSurfaceTexture[i] = null;
                }
            }
        }
        this.mSurfaceTexture = null;
    }

    protected boolean onSurfaceDestroyed(SurfaceHolder holder) {
        CamLog.m3d(CameraConstants.TAG, "onSurfaceDestroyed. mCameraDevice is null ? " + (this.mCameraDevice == null));
        if (!(this.mCameraDevice == null || isModuleChanging())) {
            this.mCameraDevice.stopPreview();
            this.mCameraDevice.setPreviewDisplay(null);
        }
        setCameraState(-2);
        return true;
    }

    protected void setMVState(int state) {
        setMVState(state, false);
    }

    protected void setMVState(int state, boolean isForced) {
        if (!isPaused() && this.mIsStoppingRecord && (state & 2) == 0) {
            CamLog.m3d(CameraConstants.TAG, "-th- do not save MVState during recording stopping : " + state);
            if (!isForced) {
                return;
            }
        }
        CamLog.m3d(CameraConstants.TAG, "-th- setMVState: " + state);
        this.mState = state;
    }

    protected int getMVState() {
        return this.mState;
    }

    private void setupTextureViewMV() {
        if (this.mTextureViewMV == null) {
            this.mTextureViewMV = (TextureView) this.mGet.findViewById(C0088R.id.preview_texture_multiview);
            this.mTextureViewMV.setSurfaceTextureListener(this.mSurfaceTextureListenerMV);
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    MultiGLViewBase.this.mTextureViewMV.setLayoutParams(new LayoutParams(MultiGLViewBase.this.mLcdSize[1], MultiGLViewBase.this.mLcdSize[0]));
                }
            });
        }
    }

    protected boolean checkForSetupPreview() {
        if (!checkModuleValidate(192)) {
            runStartRecorder(false);
            CamLog.m3d(CameraConstants.TAG, "ignore setupPreview - in the process of recording start");
            return false;
        } else if (!this.mGet.isPaused()) {
            return true;
        } else {
            CamLog.m3d(CameraConstants.TAG, "ignore setupPreview - onPause state");
            return false;
        }
    }

    private void setStateOnRecordStop() {
        setCameraState(1);
        if ((getMVState() & 32) != 0) {
            setMVState(33);
        } else {
            setMVState(1);
        }
    }

    private void waitForCollageEnd() {
        if ((getMVState() & 4) != 0) {
            sLatch2 = new CountDownLatch(1);
            try {
                CamLog.m3d(CameraConstants.TAG, "MultiView - sLatch2.await() await");
                sLatch2.await();
                CamLog.m3d(CameraConstants.TAG, "MultiView - sLatch2.await() continue");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sLatch2 = null;
        }
    }

    protected void setupMultiviewPreview() {
        CamLog.m3d(CameraConstants.TAG, "Multiview- setupMultiviewPreview start");
        if (!checkForSetupPreview()) {
            return;
        }
        if ((getMVState() & 2) != 0) {
            this.mIsStoppingRecord = false;
            startPreviewFirst();
            setStateOnRecordStop();
            CamLog.m3d(CameraConstants.TAG, "setupPreview - in the process of recording stop");
            return;
        }
        this.mMultiviewFrameReady = false;
        waitForCollageEnd();
        if (this.mGet.isPaused()) {
            CamLog.m3d(CameraConstants.TAG, "ignore setupPreview - onPause state");
            return;
        }
        sLatch = new CountDownLatch(1);
        CamLog.m3d(CameraConstants.TAG, "sLatch is created = " + sLatch);
        setupTextureViewMV();
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                MultiGLViewBase.this.mTextureViewMV.setTranslationY(0.0f);
                MultiGLViewBase.this.mTextureViewMV.setVisibility(0);
            }
        });
        if (this.sSurfaceTextureMV == null) {
            CamLog.m3d(CameraConstants.TAG, "MultiView - mSurfaceTextureMV == null");
            try {
                CamLog.m3d(CameraConstants.TAG, "MultiView - sLatch.await() await");
                sLatch.await(4000, TimeUnit.MILLISECONDS);
                CamLog.m3d(CameraConstants.TAG, "MultiView - sLatch.await() continue");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        sLatch = null;
        if (!this.mIsCameraAlreadyOpened) {
            startRenderThread();
        }
        if (this.mPostviewManager == null || !this.mPostviewManager.isPostviewShowing()) {
            this.mSnapShotChecker.removeBurstState(1);
            startPreviewFirst();
            startPreviewSecond();
            setCameraState(1);
            CamLog.m3d(CameraConstants.TAG, "Multiview- setupMultiviewPreview end");
        }
    }

    protected void multiviewMakingCollageEnd() {
    }

    protected void onTakePictureBefore() {
        this.mIsStopTimershotCountdown = false;
        super.onTakePictureBefore();
    }

    protected void onTakePictureAfter() {
        if (!(this.mQuickClipManager == null || isTimerShotCountdown() || this.mIsStopTimershotCountdown)) {
            this.mQuickClipManager.setAfterShot();
        }
        super.onTakePictureAfter();
    }

    public boolean doBackKey() {
        if (this.mQuickClipManager != null && isTimerShotCountdown()) {
            this.mIsStopTimershotCountdown = true;
        }
        return super.doBackKey();
    }

    private void startRenderThread() {
        int captureRotation = Utils.isConfigureLandscape(this.mGet.getAppContext().getResources()) ? 0 : 270;
        if (this.mRenderThread != null) {
            try {
                CamLog.m3d(CameraConstants.TAG, "-th- waiting for finishing previous render thread");
                this.mRenderThread.join();
                CamLog.m3d(CameraConstants.TAG, "-th- finishing previous render thread is done");
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        this.mRenderThread = new MultiViewRenderer(this.sSurfaceTextureMV, getShotMode());
        CamLog.m3d(CameraConstants.TAG, "-th- new renderer thread is created");
        setupTextureViewMV();
        int index = getLayoutIndex();
        if (index == 0) {
            CamLog.m3d(CameraConstants.TAG, "Change multiview layout to triple1 forcely.");
            index = 2;
            this.mGet.setSetting(Setting.KEY_MULTIVIEW_LAYOUT, CameraConstants.MULTIVIEW_LAYOUT_TRIPLE01, true);
        }
        this.mRenderThread.init(this.mTextureViewMV, this.mFrameCaptureListener, this.mSTAvailableListener, this.mSTAvailableListenerCollage, captureRotation, this.mCameraId, index, this);
        this.mMultiviewFrame = this.mRenderThread.getMultiviewFrame();
        this.mRenderThread.setName("MVRenderThread");
        this.mSTLatch = new CountDownLatch(1);
        this.mRenderThread.start();
        try {
            CamLog.m3d(CameraConstants.TAG, "MultiView - Wait for surface texture ready notification from render thread");
            this.mSTLatch.await(CameraConstants.TOAST_LENGTH_MIDDLE_SHORT, TimeUnit.MILLISECONDS);
            CamLog.m3d(CameraConstants.TAG, "MultiView - continue - Render thread notifies that surface textures are ready");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void startPreviewFirst() {
        if (this.mCameraDevice != null) {
            CamLog.m3d(CameraConstants.TAG, "MultiView - startPreview - first camera - start");
            CameraParameters params = this.mCameraDevice.getParameters();
            if (params == null) {
                CamLog.m5e(CameraConstants.TAG, "params is null");
                return;
            }
            if (this.mUseDualRecorder) {
                params.set(ParamConstants.KEY_DUAL_RECORDER, 1);
            }
            params.set(ParamConstants.KEY_LGE_CAMERA, 1);
            params.set("picture-size", getLiveSnapShotSize(params, "1280x720"));
            if (FunctionProperties.useWideRearAsDefault()) {
                params.set(ParamConstants.KEY_PREVIEW_SIZE, PREVIEW_SIZE_16BY9_WIDE);
            } else if (FunctionProperties.isSupportedCollageRecording()) {
                params.set(ParamConstants.KEY_PREVIEW_SIZE, "1280x720");
            } else {
                params.set(ParamConstants.KEY_PREVIEW_SIZE, "1080x1080");
            }
            if (FunctionProperties.getSupportedHal() != 2) {
                Camera camera = (Camera) this.mCameraDevice.getCamera();
                if (camera != null) {
                    camera.setParameters((Parameters) params.getParameters());
                } else {
                    CamLog.m3d(CameraConstants.TAG, "Camera is null return.");
                    return;
                }
            }
            this.mCameraDevice.setErrorCallback(this.mHandler, this.mErrorCallback);
            if (this.mCameraState > 0 && this.mCameraState != 3) {
                stopPreview();
            }
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (!(this.mParamUpdater == null || parameters == null)) {
                this.mParamUpdater.updateAllParameters(parameters);
                setParamFirst(parameters);
                Log.i(CameraConstants.TAG, "[Time Info][4] App Param setting End : Camera Parameter setting " + DebugUtil.interimCheckTime(true) + " ms");
                Log.i(CameraConstants.TAG, "[Time Info][5] Device Param setting Start : Device setting " + DebugUtil.interimCheckTime(false));
                this.mCameraDevice.setParameters(parameters);
                Log.i(CameraConstants.TAG, "[Time Info][5] Device Param setting End : Device setting " + DebugUtil.interimCheckTime(true) + " ms");
                CamLog.m3d(CameraConstants.TAG, "first camera - paramPictureSize = " + parameters.get("picture-size") + ", paramPreviewSize = " + parameters.get(ParamConstants.KEY_PREVIEW_SIZE));
            }
            setDisplayOrientation(false);
            if (this.mSurfaceTexture != null) {
                CamLog.m3d(CameraConstants.TAG, "MultiView -  setPreviewDisplay Texture");
                setupPreviewBuffer(parameters, this.mSurfaceTexture[0]);
                this.mCameraDevice.setPreviewTexture(this.mSurfaceTexture[0]);
                setOneShotPreviewCallback();
                Log.i(CameraConstants.TAG, "[Time Info][6] Device StartPreview Start : Driver Preview Operation " + DebugUtil.interimCheckTime(false));
                setToStartPreview("1280x720", 0);
                setZoomCompensation(parameters);
                CamLog.m3d(CameraConstants.TAG, "MultiView - startPreview - first camera - end");
            }
        }
    }

    protected void setParamFirst(CameraParameters parameters) {
        setParamCommon(parameters, false);
        setParamFirstIndividual(parameters);
    }

    protected void setParamSecond(CameraParameters parameters) {
        setParamCommon(parameters, true);
        setParamSecondIndividual(parameters);
    }

    protected void setParamCommon(CameraParameters parameters, boolean isBeauty) {
        CamLog.m3d(CameraConstants.TAG, "-multiview- setParamCommon");
        this.mParamUpdater.removeRequester(ParamConstants.KEY_LGE_CAMERA);
        if (this.mUseDualRecorder) {
            this.mParamUpdater.removeRequester(ParamConstants.KEY_DUAL_RECORDER);
        }
        this.mParamUpdater.removeRequester(ParamConstants.KEY_ZSL);
        this.mParamUpdater.removeRequester("zoom");
        this.mParamUpdater.removeRequester("hdr-mode");
        this.mParamUpdater.addRequester(ParamConstants.KEY_LGE_CAMERA, "1", false, true);
        if (this.mUseDualRecorder) {
            this.mParamUpdater.addRequester(ParamConstants.KEY_DUAL_RECORDER, "1", false, true);
        }
        this.mParamUpdater.addRequester(ParamConstants.KEY_ZSL, "off", false, true);
        this.mParamUpdater.addRequester("zoom", "0", false, true);
        this.mParamUpdater.addRequester("hdr-mode", "0", false, true);
        this.mParamUpdater.addRequester("flash-mode", "off", false, true);
        this.mParamUpdater.addRequester(ParamConstants.KEY_APP_SHOT_MODE, ParamUtils.convertShotMode(getShotMode()), false, true);
        if (isBeauty) {
            this.mParamUpdater = setBeautyParam(this.mParamUpdater);
        }
        this.mParamUpdater.updateAllParameters(parameters);
    }

    public ParamUpdater setBeautyParam(ParamUpdater param) {
        this.mBeautyStrength = Integer.parseInt(SharedPreferenceUtil.getBeautyLevel(this.mGet.getAppContext(), "front")) * 10;
        CamLog.m3d(CameraConstants.TAG, "-multiview- beautyLevel = " + this.mBeautyStrength);
        param.removeRequester(ParamConstants.KEY_BEAUTY_LEVEL);
        param.removeRequester("beautyshot");
        param.addRequester(ParamConstants.KEY_BEAUTY_LEVEL, "" + this.mBeautyStrength, false, false);
        param.addRequester("beautyshot", "on", false, false);
        param.addRequester(ParamConstants.KEY_MULTISHOT, this.mIsMultiviewRecording ? "on" : "off", false, false);
        return param;
    }

    protected void setParamFirstIndividual(CameraParameters parameters) {
    }

    protected void setParamSecondIndividual(CameraParameters parameters) {
    }

    protected void setParamSecondIndividual(CameraParameters parameters, String previewSize) {
    }

    protected int getLoopRecordingType() {
        return 0;
    }

    private int getSecondCameraId() {
        return 1;
    }

    protected void startPreviewSecond() {
        CamLog.m3d(CameraConstants.TAG, "MultiView - startPreview - second camera - START");
        closeCamera2();
        int cameraId = getSecondCameraId();
        CamLog.m3d(CameraConstants.TAG, "get mLGCamera1 cameraId = " + cameraId);
        CameraSecondHolder.subinstance().open(this.mHolderHandler, cameraId, null, false, this.mGet.getActivity());
        if (this.mSurfaceTexture != null) {
            CameraSecondHolder.subinstance().setPreviewTexture(this.mSurfaceTexture[1]);
            CameraParameters parameters = CameraSecondHolder.subinstance().getParameters();
            setParamSecond(parameters);
            CameraSecondHolder.subinstance().setParameters(parameters);
            setupPreviewBuffer(parameters, this.mSurfaceTexture[1]);
            CamLog.m3d(CameraConstants.TAG, "camera 1 - paramPictureSize = " + parameters.get("picture-size") + ", paramPreviewSize = " + parameters.get(ParamConstants.KEY_PREVIEW_SIZE));
            setOneShotPreviewCallback2();
            setToStartPreview("1280x960", 1);
            CamLog.m3d(CameraConstants.TAG, "MultiView - startPreview - second camera  - END");
        }
    }

    private void setToStartPreview(String size, int cameraIndex) {
        if (cameraIndex == 0) {
            if (FunctionProperties.getSupportedHal() == 2) {
                VideoRecorder.createPersistentSurface(size, cameraIndex, null);
                this.mCameraDevice.startRecordingPreview(VideoRecorder.getSurface(false, cameraIndex));
                return;
            }
            this.mCameraDevice.startPreview();
        } else if (cameraIndex != 1) {
        } else {
            if (FunctionProperties.getSupportedHal() == 2) {
                VideoRecorder.createPersistentSurface(size, cameraIndex, null);
                CameraSecondHolder.subinstance().startRecordingPreview(VideoRecorder.getSurface(false, cameraIndex));
                return;
            }
            CameraSecondHolder.subinstance().startPreview();
        }
    }

    protected void setOneShotPreviewCallback2() {
        if (CameraSecondHolder.subinstance() != null) {
            CameraSecondHolder.subinstance().setOneShotPreviewDataCallback(this.mHolderHandler, new C03927());
        }
    }

    protected void closeCamera() {
        CamLog.m3d(CameraConstants.TAG, "MultiView - closeCamera");
        closeCamera2();
        super.closeCamera();
    }

    protected void closeCamera1() {
        CamLog.m3d(CameraConstants.TAG, "MultiView - closeCamera1");
        if (this.mCameraDevice != null) {
            this.mCameraDevice.release(true);
            this.mCameraDevice = null;
        }
    }

    protected void closeCamera2() {
        CamLog.m3d(CameraConstants.TAG, "MultiView - closeCamera2");
        if (CameraSecondHolder.isSecondCameraOpened()) {
            CameraSecondHolder.subinstance().release();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        CamLog.m3d(CameraConstants.TAG, "Multiview - onDestroy");
        if (sLatch != null) {
            sLatch.countDown();
            CamLog.m3d(CameraConstants.TAG, "MultiView - mLatch countdown");
        }
        releaseMultiviewLayout();
        this.mSurfaceTextureListenerMV = null;
    }

    public Context getAppContext() {
        return this.mGet.getAppContext();
    }

    public void afterPreviewCaptured() {
    }

    public ArrayList<MultiViewLayout> getMultiviewArrayList() {
        return this.mMultiViewLayoutList;
    }

    public synchronized ArrayList<MultiViewLayout> makeMultiviewLayout() {
        int degree = this.mGet.getOrientationDegree();
        CamLog.m3d(CameraConstants.TAG, "makeMultiviewLayout : " + degree);
        if (this.mMultiViewLayoutList == null) {
            this.mMultiViewLayoutList = new ArrayList(4);
        } else {
            CamLog.m3d(CameraConstants.TAG, "makeMultiviewLayout : " + this.mMultiViewLayoutList.size());
            this.mMultiViewLayoutList.clear();
        }
        if (FunctionProperties.multiviewFrontRearWideSupported()) {
            this.mMultiViewLayoutList.add(new MultiViewLayoutSingle(degree, new MultiViewLayoutInfoSingle()));
            if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
                this.mMultiViewLayoutList.add(new MultiViewLayoutSplitForSplice(degree, new SpliceLayoutInfoSplitFrontRearWide()));
                if (this.mIsImportedImage && !isSplicePostViewVisible()) {
                    this.mLayoutRecreated = true;
                }
            } else {
                this.mMultiViewLayoutList.add(new MultiViewLayoutSplit(degree, new MultiViewLayoutInfoSplitFrontRearWide()));
            }
            this.mMultiViewLayoutList.add(new MultiViewLayoutTriple1(degree, new MultiViewLayoutInfoTriple1FrontRearWide()));
            this.mMultiViewLayoutList.add(new MultiViewLayoutTriple2(degree, new MultiViewLayoutInfoTriple2FrontRearWide()));
            this.mMultiViewLayoutList.add(new MultiViewLayoutQuad(degree, new MultiViewLayoutInfoQuadFrontRearWide()));
        } else {
            this.mMultiViewLayoutList.add(new MultiViewLayoutSingle(degree, new MultiViewLayoutInfoSingle()));
            if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
                this.mMultiViewLayoutList.add(new MultiViewLayoutSplitForSplice(degree, new SpliceLayoutInfoSplitFrontRearWideMidTier()));
                if (this.mIsImportedImage && !isSplicePostViewVisible()) {
                    this.mLayoutRecreated = true;
                }
            } else {
                this.mMultiViewLayoutList.add(new MultiViewLayoutSplit(degree, new MultiViewLayoutInfoSplit()));
            }
            this.mMultiViewLayoutList.add(new MultiViewLayoutTriple1(degree, new MultiViewLayoutInfoTriple1()));
            this.mMultiViewLayoutList.add(new MultiViewLayoutTriple2(degree, new MultiViewLayoutInfoTriple2()));
            this.mMultiViewLayoutList.add(new MultiViewLayoutQuad(degree, new MultiViewLayoutInfoQuad()));
        }
        CamLog.m3d(CameraConstants.TAG, "makeMultiviewLayout end");
        return this.mMultiViewLayoutList;
    }

    protected int getLayoutIndex() {
        return getSettingIndex(Setting.KEY_MULTIVIEW_LAYOUT);
    }

    protected int getLayoutIndex(String layoutType) {
        if (CameraConstants.MULTIVIEW_LAYOUT_SPLIT.equals(layoutType)) {
            return 1;
        }
        if (CameraConstants.MULTIVIEW_LAYOUT_TRIPLE01.equals(layoutType)) {
            return 2;
        }
        if (CameraConstants.MULTIVIEW_LAYOUT_TRIPLE02.equals(layoutType)) {
            return 3;
        }
        if (CameraConstants.MULTIVIEW_LAYOUT_QUAD.equals(layoutType)) {
            return 4;
        }
        return 0;
    }

    public boolean isMultiviewFrameShot() {
        return false;
    }

    public void multiviewFrameReady() {
    }

    public boolean isPaused() {
        if (this.mGet != null) {
            return this.mGet.isPaused();
        }
        return false;
    }

    protected boolean useAFTrackingModule() {
        return false;
    }

    public void releaseMultiviewLayout() {
        CamLog.m3d(CameraConstants.TAG, "release makeMultiviewLayout");
        if (this.mMultiViewLayoutList != null) {
            this.mMultiViewLayoutList.clear();
        }
        this.mMultiViewLayoutList = null;
    }

    protected boolean isFastShotSupported() {
        return false;
    }

    protected void setPreviewLayoutParam() {
        this.mGet.setTextureLayoutParams(this.mLcdSize[0], this.mLcdSize[1], -1);
        setAnimationLayout(3);
    }

    public Bitmap getBitmapForImport() {
        return null;
    }

    public Bitmap[] getTransformedImage() {
        return null;
    }

    public int getRecordedCameraIdForReverse() {
        return -1;
    }

    public int getCurrentPostViewType() {
        return -1;
    }

    public void savePostViewContens(int type) {
    }

    public boolean getPostviewVisibility() {
        return false;
    }

    public void setBitmapToPrePostView(boolean isImage) {
    }

    public void setSwapBitmapReady() {
    }

    public boolean isImportedImage() {
        return false;
    }

    protected boolean isSplicePostViewVisible() {
        return false;
    }

    public void resetStatus() {
    }

    public void setReverseState(boolean isReverse) {
    }

    public boolean getReverseState() {
        return false;
    }

    protected boolean isAEAFLockSupportedMode() {
        return false;
    }

    protected boolean isAEControlSupportedMode() {
        return false;
    }

    public boolean isHALSignatureCaptureMode() {
        return false;
    }

    public boolean isInitialHelpSupportedModule() {
        return false;
    }

    public boolean isSupportedFilterMenu() {
        return false;
    }
}
