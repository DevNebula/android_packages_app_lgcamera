package com.lge.camera.app.ext;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.Image;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.components.DrawingPanel;
import com.lge.camera.components.PanoramaMiniPreview;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.SpeedWarningHandler;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.PanoramaControl.IPanoramaControl;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import com.lge.panorama.PanoramaDebug;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public class PanoramaModuleCommonImpl extends PanoramaModuleCommon implements IPanoramaControl {
    public static final int ADDIMG_ERROR_AFTER_SAVE = 4;
    public static final int ADDIMG_ERROR_GENERAL = 1;
    public static final int ADDIMG_ERROR_NONE = 0;
    public static final int ADDIMG_ERROR_ON_PREPARE = 2;
    public static final int ADDIMG_ERROR_ON_SAVING = 3;
    protected static final Object AddBufferSyncOBJ = new Object();
    protected static final int CHECK_Q_SIZE = 2;
    protected static final boolean DEBUG = false;
    protected static final Object EngineSynchronizedOBJ = new Object();
    protected static final String FRAME_CB_DISABLE = "0";
    protected static final String FRAME_CB_PREV_ENABLE = "2";
    protected static final String FRAME_CB_YUV_ENABLE = "1";
    protected static final boolean FRAME_DEBUG = false;
    protected static final boolean FRAME_DEBUG_SPARSE = false;
    protected static final long IDLE_CHECKTIME_BEFORE_DICIDE_DIRECTION = 10000;
    protected static final int INPUT_TYPE_PREVIEW = 0;
    protected static final int INPUT_TYPE_RAW_NORMAL_NV21 = 1;
    protected static final int INPUT_TYPE_RAW_QCT_NV21 = 2;
    protected static final String KEY_LG_PANORAMA_YUV_PPMASK_ENABLE = "raw_ppmask_enable";
    protected static final String KEY_REPEATING_FRAME = "frame_repeating_enable";
    protected static final boolean MINI_PREV_DEBUG = false;
    protected static final Object PanoPreviewSyncOBJ = new Object();
    protected static final boolean SAVE_DEBUG = false;
    protected static final long SLEEP_MILLISEC = 5;
    protected static final float WIDE_ANGLE_PREV_RATIO = 2.05f;
    protected static final float WIDE_ANGLE_RATIO = 1.72f;
    protected static final String YUV_PPMASK_DISABLE = "0";
    protected static final String YUV_PPMASK_ENABLE = "1";
    protected static int sBUFFER_SIZE = 6;
    protected static boolean sREAD_EXIF = false;
    protected final double BUFFER_SCALE = 1.5d;
    protected long mC_cnt = 0;
    protected long mC_dur = 0;
    protected long mC_s = 0;
    protected int mCameraOrientation = 0;
    protected int mCaptureDirection = 0;
    protected int mCurBufferId = 0;
    protected long[] mDateTaken = new long[2];
    protected boolean mDecideDirection = false;
    protected String mDirectory = null;
    protected int mDispH = 0;
    protected int mDispW = 0;
    protected Bitmap mDisplayPreviewImageMini;
    protected DrawingPanel mDrawingPanel = null;
    protected long mDur = 0;
    protected ExecutorService mExecutor = null;
    protected long mF_cnt = 0;
    protected long mF_dur = 0;
    protected long mF_s = 0;
    protected String mFileNameWithExt = null;
    protected float mFocalLength = 0.0f;
    protected int mFrameCnt = 0;
    protected int mFrameRotation = 0;
    protected Size mFrameSize = null;
    protected SpeedWarningHandler mHandlerTaking = null;
    protected Size mHoriPanPrevSize = null;
    protected long mIdleCheckTime = 0;
    protected int mInputType = 0;
    protected boolean mIsFeeding = false;
    protected String mModelName = "";
    protected String mOutputFileName = null;
    protected PanoramaDebug mPanoDebug = null;
    protected int mPanoramaMaxLength = 0;
    protected ByteBuffer[] mPrevByteBuffer = null;
    protected Bitmap mPreviewImageMini;
    protected PanoramaMiniPreview mPreviewMini;
    protected Canvas mPreviewMiniCanvas;
    protected int mRawBufSize = 0;
    protected ByteBuffer[] mRawByteBuffer = null;
    protected Rect mRectPreviewMini;
    protected SaveThread mSaveThread = null;
    protected long mSavingTime = 0;
    protected int mScanline = 0;
    protected boolean mShowDialog = false;
    protected long mStart = 0;
    protected int mStride = 0;
    protected CameraParameters mTempParams = null;
    protected long mTotalSavingTime = 0;
    protected Size mVertPanoPrevSize = null;
    protected float mViewAngleH = 60.0f;
    protected float mViewAngleV = 40.0f;

    public class SaveThread extends Thread {
        public SaveThread() {
            setName("SaveThread");
        }

        public void run() {
            PanoramaModuleCommonImpl.this.doSaveThread();
        }
    }

    public PanoramaModuleCommonImpl(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void createPreviewBuffer(int size, int buffer_size) {
        this.mPrevByteBuffer = new ByteBuffer[buffer_size];
        for (int i = 0; i < buffer_size; i++) {
            this.mPrevByteBuffer[i] = ByteBuffer.allocateDirect(size);
        }
    }

    protected byte[] getPreviewBuffer() {
        if (this.mPrevByteBuffer == null || this.mPrevByteBuffer[this.mCurBufferId] == null) {
            return null;
        }
        return this.mPrevByteBuffer[this.mCurBufferId].array();
    }

    protected void createRawBuffer(int size, int buffer_size) {
        this.mRawByteBuffer = new ByteBuffer[buffer_size];
        for (int i = 0; i < buffer_size; i++) {
            this.mRawByteBuffer[i] = ByteBuffer.allocateDirect(size);
        }
    }

    protected byte[] getRawBuffer() {
        if (this.mRawByteBuffer == null || this.mRawByteBuffer[this.mCurBufferId] == null) {
            return null;
        }
        return this.mRawByteBuffer[this.mCurBufferId].array();
    }

    protected void releaseByteBuffers(ByteBuffer[] byteBuffers) {
        if (byteBuffers != null) {
            for (int i = 0; i < byteBuffers.length; i++) {
                byteBuffers[i] = null;
            }
        }
    }

    protected void setInputType(int inputType) {
        this.mInputType = inputType;
    }

    protected Size getInputSize() {
        Size pictureSize;
        switch (this.mInputType) {
            case 0:
                if (this.mFrameSize == null) {
                    Size previewSize = this.mCameraDevice.getParameters().getPreviewSize();
                    this.mFrameSize = new Size(previewSize.getWidth(), previewSize.getHeight());
                }
                return this.mFrameSize;
            case 1:
                if (this.mFrameSize == null) {
                    pictureSize = this.mCameraDevice.getParameters().getPictureSize();
                    this.mFrameSize = new Size(pictureSize.getWidth(), pictureSize.getHeight());
                }
                return this.mFrameSize;
            case 2:
                if (this.mFrameSize == null) {
                    pictureSize = this.mCameraDevice.getParameters().getPictureSize();
                    this.mFrameSize = new Size(pictureSize.getWidth(), pictureSize.getHeight());
                }
                return this.mFrameSize;
            default:
                Size size = new Size(640, CameraConstants.SHUTTER_ZOOM_SUPPORTED_MIN_ZOOM_LEVEL);
                this.mFrameSize = size;
                return size;
        }
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (this.mBaseView != null) {
            ((ViewGroup) this.mBaseView).removeAllViews();
            this.mBaseView = null;
        }
        createViews(!isMenuShowing(CameraConstants.MENU_TYPE_ALL));
    }

    protected void createViews(boolean visible) {
        if (this.mBaseView == null) {
            this.mGet.layoutInflate(C0088R.layout.panorama_view_lg, (ViewGroup) this.mBaseParentView);
            if (this.mBaseParentView == null) {
                CamLog.m3d(CameraConstants.TAG, "mBaseParentView is null");
                return;
            }
            this.mBaseView = this.mBaseParentView.findViewById(C0088R.id.panorama_view_layout_lg);
            if (ModelProperties.getLCDType() == 2) {
                this.mBaseView.setPaddingRelative(0, RatioCalcUtil.getQuickButtonWidth(getAppContext()), 0, 0);
            }
            this.mPreviewMiniLayout = (RotateLayout) this.mBaseView.findViewById(C0088R.id.panorama_preview_mini_layout);
            this.mPreviewMiniLayoutOutline = (RelativeLayout) this.mBaseView.findViewById(C0088R.id.panorama_preview_mini_layout_outline);
            this.mPreviewMiniLayoutArrow = (RelativeLayout) this.mBaseView.findViewById(C0088R.id.panorama_preview_mini_layout_arrow);
            this.mStartAndStopGuideTextLayout = (RelativeLayout) this.mBaseView.findViewById(C0088R.id.guide_text_layout);
            this.mPreviewMini = (PanoramaMiniPreview) this.mBaseView.findViewById(C0088R.id.panorama_preview_mini);
            this.mButtonLayout = (LinearLayout) this.mBaseView.findViewById(C0088R.id.panorama_btn_layout);
            LayoutParams buttonLp = (LayoutParams) this.mButtonLayout.getLayoutParams();
            if (ModelProperties.isLongLCDModel() && buttonLp != null) {
                buttonLp.setMargins(buttonLp.leftMargin, buttonLp.topMargin, buttonLp.rightMargin, RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.213f));
                this.mButtonLayout.setLayoutParams(buttonLp);
            }
        }
        View previewLayout = this.mBaseView.findViewById(C0088R.id.panorama_background_preview_layout);
        if (!previewLayout.isShown()) {
            this.mDisplayPreviewImageMini = null;
        }
        previewLayout.setVisibility(0);
        this.mBaseView.setVisibility(visible ? 0 : 4);
        this.mButtonLayout.setVisibility(8);
        setDrawingPanelView();
        if (this.mHandlerTaking != null) {
            this.mHandlerTaking.unbind();
        }
        this.mHandlerTaking = new SpeedWarningHandler(this.mBaseView);
        this.mOldDegreeMiniOutline = -1;
        setMiniOutline(0);
    }

    protected void setDrawingPanelView() {
        this.mDrawingPanel = (DrawingPanel) this.mBaseView.findViewById(C0088R.id.preview_drawingpanel);
        this.mDrawingPanel.setArrowRes(C0088R.drawable.panorama_guide_arrow_right, C0088R.drawable.panorama_guide_arrow_left, C0088R.drawable.panorama_guide_arrow_up, C0088R.drawable.panorama_guide_arrow_down);
    }

    protected boolean setMiniOutline(int degree) {
        if (!super.setMiniOutline(degree)) {
            return false;
        }
        setMiniPreviewSize(degree);
        return true;
    }

    protected void setMiniPreviewSize(int degree) {
        float displayH = (float) RatioCalcUtil.getSizeRatioInPano(getAppContext(), 216.0f);
        float displayW = ((float) RatioCalcUtil.getSizeRatioInPano(getAppContext(), 292.0f)) * 0.8f;
        if (degree == 0 || degree == 180) {
            displayW *= 0.95f;
            displayH *= 0.95f;
        } else if (degree == 90 || degree == 270) {
            displayW *= 1.05f;
            displayH *= 1.05f;
        }
        float factor = ((float) this.mPreviewH) / ((float) this.mPreviewW);
        int padding = (int) Utils.dpToPx(getAppContext(), 3.0f);
        int imageW = (int) displayW;
        int imageH = (int) displayH;
        if (getCameraId() == 2) {
            displayW *= WIDE_ANGLE_PREV_RATIO;
        } else {
            displayW += 0.2f * displayW;
        }
        displayW -= (float) padding;
        CamLog.m3d(CameraConstants.TAG, "Panorama mini preview size origin - w : " + displayW + ", h : " + displayH);
        displayW -= displayW % 8.0f;
        displayH = (float) ((int) (displayW * factor));
        displayH -= displayH % 8.0f;
        CamLog.m3d(CameraConstants.TAG, "Panorama mini preview size after - w : " + displayW + ", h : " + displayH);
        imageW = (int) displayW;
        imageH = (int) displayH;
        displayH += (float) padding;
        this.mDispW = (int) (displayW + ((float) padding));
        this.mDispH = (int) displayH;
        CamLog.m3d(CameraConstants.TAG, String.format(Locale.US, "setMiniPreviewSize : (mDispW x mDispH) = (%d x %d), (imageW x imageH) = (%d x %d)", new Object[]{Integer.valueOf(this.mDispW), Integer.valueOf(this.mDispH), Integer.valueOf(imageW), Integer.valueOf(imageH)}));
        if (this.mDisplayPreviewImageMini == null) {
            this.mDisplayPreviewImageMini = Bitmap.createBitmap(imageW, imageH, Config.ARGB_8888);
        }
        createPreviewImageMiniBitmap(imageW, imageH, this.mPreviewW, this.mPreviewH);
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(0, info);
        this.mPreviewMiniLayout.setRotation((float) (((info.orientation - CameraDeviceUtils.getDisplayRotation(this.mGet.getActivity())) + 360) % 360));
        LayoutParams lpMini = (LayoutParams) this.mPreviewMiniLayout.getLayoutParams();
        lpMini.setMarginStart(-Math.abs((this.mDispW - this.mDispH) / 2));
        lpMini.width = this.mDispW;
        lpMini.height = this.mDispH;
        this.mPreviewMini.getLayoutParams().width = this.mDispW;
        this.mPreviewMini.getLayoutParams().height = this.mDispH;
        this.mPreviewMiniLayout.setLayoutParams(lpMini);
        if (this.mPreviewMiniCanvas == null) {
            this.mPreviewMiniCanvas = new Canvas(this.mDisplayPreviewImageMini);
        } else {
            this.mPreviewMiniCanvas.setBitmap(this.mDisplayPreviewImageMini);
        }
        if (this.mRectPreviewMini == null) {
            this.mRectPreviewMini = new Rect(0, 0, imageW, imageH);
        } else {
            this.mRectPreviewMini.set(0, 0, imageW, imageH);
        }
        this.mPreviewMini.setScaleType(ScaleType.FIT_XY);
        this.mPreviewMini.setImageBitmap(this.mDisplayPreviewImageMini);
    }

    protected void createPreviewImageMiniBitmap(int width, int height, int previewW, int previewH) {
        this.mPreviewImageMini = Bitmap.createBitmap(width, height, Config.ARGB_8888);
    }

    protected void setSpeedWarningLayout() {
        RelativeLayout layout = (RelativeLayout) findViewById(C0088R.id.panorama_warning_text_layout);
        if (layout != null) {
            RotateLayout rotateLayout = (RotateLayout) layout.findViewById(C0088R.id.panorama_warning_text_rotate_layout);
            TextView textView = (TextView) layout.findViewById(C0088R.id.panorama_warning_text);
            LayoutParams lpLayout = (LayoutParams) layout.getLayoutParams();
            LayoutParams lpTextView = (LayoutParams) textView.getLayoutParams();
            Utils.resetLayoutParameter(lpLayout);
            Utils.resetLayoutParameter(lpTextView);
            int margin = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 20.0f);
            int marginTop = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 20.0f);
            int previewSmallerLength = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 256.0f);
            if (getCameraId() == 2) {
                previewSmallerLength = (int) (((float) previewSmallerLength) * WIDE_ANGLE_RATIO);
            }
            int panel_y = ((this.mDrawingPanel.getPanelHeight() / 2) + (previewSmallerLength / 2)) + marginTop;
            rotateLayout.rotateLayout(0);
            switch (null) {
                case null:
                    lpTextView.addRule(14, 1);
                    lpTextView.addRule(12, 1);
                    lpLayout.bottomMargin = panel_y;
                    break;
                case 90:
                    lpTextView.addRule(15, 1);
                    lpTextView.addRule(21, 1);
                    lpLayout.topMargin = margin;
                    break;
                case 180:
                    lpTextView.addRule(14, 1);
                    lpTextView.addRule(10, 1);
                    lpLayout.bottomMargin = panel_y;
                    break;
                case 270:
                    lpTextView.addRule(15, 1);
                    lpTextView.addRule(20, 1);
                    lpLayout.topMargin = margin;
                    break;
            }
            layout.setLayoutParams(lpLayout);
            textView.setLayoutParams(lpTextView);
        }
    }

    public void hide() {
        CamLog.m3d(CameraConstants.TAG, "hide panorama view");
        if (this.mBaseView != null) {
            this.mBaseView.setVisibility(4);
        }
    }

    protected void restorePanoramaParams() {
        if (this.mCameraDevice != null) {
            this.mTempParams = this.mCameraDevice.getParameters();
            if (this.mTempParams != null) {
                this.mTempParams.set(KEY_REPEATING_FRAME, "0");
                if (this.mInputType != 0) {
                    this.mTempParams.set(KEY_LG_PANORAMA_YUV_PPMASK_ENABLE, "0");
                } else {
                    this.mTempParams.set(ParamConstants.KEY_PREVIEW_SIZE, ModelProperties.getPanoramaPreviewSize(true));
                }
                activateFingerDetection(true, this.mTempParams, false);
                setParameterByLGSF(this.mTempParams, CameraConstants.PANO_PARAM_SAVE, false);
            }
        }
    }

    protected void doStopPanoramaJob(boolean isNeedSaving) {
        CamLog.m3d(CameraConstants.TAG, "doStopPanoramaJob saving : " + isNeedSaving);
        this.mSnapShotChecker.releaseSnapShotChecker();
        setCameraState(1);
        if (FunctionProperties.getSupportedHal() == 2) {
            setCameraCallbackAll(-1, null, null, null);
        } else {
            setCameraCallbackAll(null, null);
        }
        releaseByteBuffers(this.mRawByteBuffer);
        this.mRawByteBuffer = null;
        if (isNeedSaving) {
            this.mState = 6;
            this.mSavingTime = System.currentTimeMillis();
            this.mTotalSavingTime = System.currentTimeMillis();
            needSaveInStopPanorama();
        }
        if (FunctionProperties.getSupportedHal() != 2) {
            restorePanoramaParams();
        }
        keepScreenOnAwhile();
        showGuideText(false);
        setDoubleCameraEnable(true);
        showDoubleCamera(true);
        if (this.mCaptureButtonManager != null) {
            if (isNeedSaving) {
                this.mCaptureButtonManager.setShutterButtonEnable(false, getShutterButtonType());
                this.mCaptureButtonManager.changeButtonByMode(9);
                this.mCaptureButtonManager.setExtraButtonEnable(true, 3);
            } else {
                this.mCaptureButtonManager.setShutterButtonVisibility(4, getShutterButtonType());
                this.mCaptureButtonManager.changeButtonByMode(this.mState == 4 ? 7 : 12);
                this.mCaptureButtonManager.setExtraButtonEnable(true, 3);
            }
        }
        if (this.mDotIndicatorManager != null) {
            this.mDotIndicatorManager.show();
        }
        sFirstTaken = true;
        if (!"on".equals(getSettingValue(Setting.KEY_VOICESHUTTER))) {
            AudioUtil.setAudioFocus(getAppContext(), false);
        }
    }

    protected void needSaveInStopPanorama() {
        this.mSaveThread = new SaveThread();
        this.mSaveThread.start();
    }

    public void onQueueStatus(int count) {
        if (!isProcessingFinishTask()) {
            super.onQueueStatus(count);
        }
    }

    protected boolean isProcessingFinishTask() {
        return this.mSaveThread != null && this.mSaveThread.isAlive();
    }

    protected void resetByPausing() {
        if (this.mGet != null) {
            if (!(this.mReviewThumbnailManager == null || this.mGet.isLGUOEMCameraIntent())) {
                this.mReviewThumbnailManager.setThumbnailVisibility(0);
            }
            if (!this.mGet.isSettingMenuVisible()) {
                this.mQuickButtonManager.show(true, false, true);
            }
        }
        this.mSnapShotChecker.releaseSnapShotChecker();
        if (isProcessingFinishTask()) {
            this.mState = 6;
            return;
        }
        this.mIsOnRecording = false;
        this.mState = 0;
    }

    protected int addImage() {
        return 0;
    }

    protected int[] setExifSize(ExifInterface exif) {
        return Exif.getExifSize(exif);
    }

    protected ExifInterface readExif() {
        return Exif.readExif(this.mOutputFileName);
    }

    protected boolean checkIdleMoving() {
        if (System.currentTimeMillis() - this.mIdleCheckTime < IDLE_CHECKTIME_BEFORE_DICIDE_DIRECTION) {
            return true;
        }
        this.mIsFeeding = false;
        resetPanoramaOnUiThread();
        this.mIdleCheckTime = 0;
        return false;
    }

    protected boolean isStartedByInAndOutZoom() {
        return true;
    }

    protected void onDropTakePicture(int error) {
        CamLog.m11w(CameraConstants.TAG, "Panorama - onDropTakePicture : " + error);
        switch (error) {
            case 99:
                resetPanoramaOnUiThread();
                return;
            default:
                return;
        }
    }

    protected boolean checkAvailableUpdatingPreview() {
        return (isPaused() || this.mPreviewMini == null || this.mPreviewMiniCanvas == null || this.mPreviewImageMini == null || this.mPreviewImageMini.isRecycled() || this.mState != 2) ? false : true;
    }

    protected void cameraStartUpEnd() {
    }

    public CameraProxy getCameraDevice() {
        return this.mCameraDevice;
    }

    protected void doSaveThread() {
    }

    protected void resetEngine() {
    }

    protected void resetEngine(boolean needFinish) {
    }

    protected void resetPanoramaOnUiThread() {
    }

    protected void stopPanoramaBefore(boolean isNeedSaving) {
    }

    protected void doPanoramaJobStartAfter() {
    }

    protected void initDebugFps() {
        this.mC_dur = 0;
        this.mC_cnt = 0;
        this.mF_dur = 0;
        this.mF_cnt = 0;
    }

    protected void debugInformation() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (PanoramaModuleCommonImpl.this.mC_dur != 0 && PanoramaModuleCommonImpl.this.mF_dur != 0) {
                    String msg = String.format("Raw callback : %dfps\nFeed frame : %dfps", new Object[]{Integer.valueOf(Math.round((float) ((PanoramaModuleCommonImpl.this.mC_cnt * 1000) / PanoramaModuleCommonImpl.this.mC_dur))), Integer.valueOf(Math.round((float) ((PanoramaModuleCommonImpl.this.mF_cnt * 1000) / PanoramaModuleCommonImpl.this.mF_dur)))});
                    if (PanoramaModuleCommonImpl.this.mToastManager != null) {
                        PanoramaModuleCommonImpl.this.mToastManager.showShortToast(msg);
                    }
                }
            }
        });
    }

    protected void debugExifInformation(int isoValue, float nr_strength, float unsharp_strength) {
        final int i = isoValue;
        final float f = nr_strength;
        final float f2 = unsharp_strength;
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                String msg = String.format("ISO : %d\nNR value : %.2f\nUnsharpen value : %.2f", new Object[]{Integer.valueOf(i), Float.valueOf(f), Float.valueOf(f2)});
                if (PanoramaModuleCommonImpl.this.mToastManager != null) {
                    PanoramaModuleCommonImpl.this.mToastManager.showShortToast(msg);
                }
            }
        });
    }

    protected void debugSavingTime() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            /* JADX WARNING: Removed duplicated region for block: B:51:? A:{SYNTHETIC, RETURN} */
            /* JADX WARNING: Removed duplicated region for block: B:18:0x007c A:{SYNTHETIC, Splitter: B:18:0x007c} */
            /* JADX WARNING: Removed duplicated region for block: B:55:? A:{SYNTHETIC, RETURN} */
            /* JADX WARNING: Removed duplicated region for block: B:32:0x0098 A:{SYNTHETIC, Splitter: B:32:0x0098} */
            /* JADX WARNING: Removed duplicated region for block: B:38:0x00a4 A:{SYNTHETIC, Splitter: B:38:0x00a4} */
            public void handleRun() {
                /*
                r12 = this;
                r6 = "Saving Time : %dms\nTotal Saving Time : %dms";
                r7 = 2;
                r7 = new java.lang.Object[r7];
                r8 = 0;
                r9 = com.lge.camera.app.ext.PanoramaModuleCommonImpl.this;
                r10 = r9.mSavingTime;
                r9 = java.lang.Long.valueOf(r10);
                r7[r8] = r9;
                r8 = 1;
                r9 = com.lge.camera.app.ext.PanoramaModuleCommonImpl.this;
                r10 = r9.mTotalSavingTime;
                r9 = java.lang.Long.valueOf(r10);
                r7[r8] = r9;
                r5 = java.lang.String.format(r6, r7);
                r6 = com.lge.camera.app.ext.PanoramaModuleCommonImpl.this;
                r6 = r6.mToastManager;
                if (r6 == 0) goto L_0x0030;
            L_0x0027:
                r6 = com.lge.camera.app.ext.PanoramaModuleCommonImpl.this;
                r6 = r6.mToastManager;
                r6.showShortToast(r5);
            L_0x0030:
                r3 = new java.io.File;
                r6 = new java.lang.StringBuilder;
                r6.<init>();
                r7 = "sdcard/";
                r6 = r6.append(r7);
                r7 = com.lge.camera.app.ext.PanoramaModuleCommonImpl.this;
                r7 = r7.mFileName;
                r6 = r6.append(r7);
                r7 = "_saving_stats.txt";
                r6 = r6.append(r7);
                r6 = r6.toString();
                r3.<init>(r6);
                if (r3 != 0) goto L_0x0057;
            L_0x0056:
                return;
            L_0x0057:
                r0 = 0;
                r1 = new java.io.BufferedReader;	 Catch:{ FileNotFoundException -> 0x00b3, IOException -> 0x0092 }
                r6 = new java.io.FileReader;	 Catch:{ FileNotFoundException -> 0x00b3, IOException -> 0x0092 }
                r6.<init>(r3);	 Catch:{ FileNotFoundException -> 0x00b3, IOException -> 0x0092 }
                r1.<init>(r6);	 Catch:{ FileNotFoundException -> 0x00b3, IOException -> 0x0092 }
            L_0x0062:
                r4 = r1.readLine();	 Catch:{ FileNotFoundException -> 0x006e, IOException -> 0x00b0, all -> 0x00ad }
                if (r4 == 0) goto L_0x0085;
            L_0x0068:
                r6 = "CameraApp";
                com.lge.camera.util.CamLog.m3d(r6, r4);	 Catch:{ FileNotFoundException -> 0x006e, IOException -> 0x00b0, all -> 0x00ad }
                goto L_0x0062;
            L_0x006e:
                r2 = move-exception;
                r0 = r1;
            L_0x0070:
                r6 = "CameraApp";
                r7 = "saving stats not exist, check if library built with PANORAMA_ENABLE_TIMER and PANORAMA_ENABLE_TIMER_APP defined";
                com.lge.camera.util.CamLog.m3d(r6, r7);	 Catch:{ all -> 0x00a1 }
                r2.printStackTrace();	 Catch:{ all -> 0x00a1 }
                if (r0 == 0) goto L_0x0056;
            L_0x007c:
                r0.close();	 Catch:{ IOException -> 0x0080 }
                goto L_0x0056;
            L_0x0080:
                r2 = move-exception;
                r2.printStackTrace();
                goto L_0x0056;
            L_0x0085:
                if (r1 == 0) goto L_0x00b5;
            L_0x0087:
                r1.close();	 Catch:{ IOException -> 0x008c }
                r0 = r1;
                goto L_0x0056;
            L_0x008c:
                r2 = move-exception;
                r2.printStackTrace();
                r0 = r1;
                goto L_0x0056;
            L_0x0092:
                r2 = move-exception;
            L_0x0093:
                r2.printStackTrace();	 Catch:{ all -> 0x00a1 }
                if (r0 == 0) goto L_0x0056;
            L_0x0098:
                r0.close();	 Catch:{ IOException -> 0x009c }
                goto L_0x0056;
            L_0x009c:
                r2 = move-exception;
                r2.printStackTrace();
                goto L_0x0056;
            L_0x00a1:
                r6 = move-exception;
            L_0x00a2:
                if (r0 == 0) goto L_0x00a7;
            L_0x00a4:
                r0.close();	 Catch:{ IOException -> 0x00a8 }
            L_0x00a7:
                throw r6;
            L_0x00a8:
                r2 = move-exception;
                r2.printStackTrace();
                goto L_0x00a7;
            L_0x00ad:
                r6 = move-exception;
                r0 = r1;
                goto L_0x00a2;
            L_0x00b0:
                r2 = move-exception;
                r0 = r1;
                goto L_0x0093;
            L_0x00b3:
                r2 = move-exception;
                goto L_0x0070;
            L_0x00b5:
                r0 = r1;
                goto L_0x0056;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.app.ext.PanoramaModuleCommonImpl.3.handleRun():void");
            }
        });
    }

    protected void initFrameDebug() {
    }

    protected void releaseFrameDebug() {
    }

    protected void addFrameDebug(byte[] data, boolean decideDirection, boolean needPoll) {
    }

    protected void saveAllFrameDebug() {
    }

    protected void initMiniPreviewFrameDebug() {
    }

    protected void startMiniPreviewFrameDebug(Image image) {
    }
}
