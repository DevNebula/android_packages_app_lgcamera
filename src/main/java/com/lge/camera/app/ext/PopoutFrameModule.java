package com.lge.camera.app.ext;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.CameraSecondHolder;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.managers.ext.PopoutFrameManager;
import com.lge.camera.managers.ext.PopoutFrameManager.onPopoutFrameListener;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CameraTypeface;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.Utils;
import com.lge.effectEngine2.LGPopoutEffectEngineListener;
import com.lge.effectEngine2.ShaderParameterData;
import com.lge.lgpopouteffectengine2.LGPopoutEffectEngine;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PopoutFrameModule extends PopoutModule implements onPopoutFrameListener {
    protected static final int ANI_PICTURE_STATE_CAPTURE = 2;
    protected static final int ANI_PICTURE_STATE_IDLE = 1;
    protected static final int ANI_PICTURE_STATE_SAVING = 4;
    protected int mAnimatedPictureState;
    private int mBeforeDegree;
    protected RectF mBoundRect;
    protected RectF mBoundRectBackup;
    public Callback mCallback;
    protected Bitmap mFrameBmp;
    protected Map<Integer, FramePositionInfo> mFrameDefaultSizeMap;
    protected boolean mIsFrameMoved;
    protected Bitmap mMaskBmp;
    protected LGPopoutEffectEngineListener mPopoutEffectEnginListener;
    protected LGPopoutEffectEngine mPopoutFrameEngine;
    protected PopoutFrameManager mPopoutFrameManager;
    private HandlerRunnable mRotateBitmapHandler;
    protected ShaderParameterData mShaderParameterData;

    /* renamed from: com.lge.camera.app.ext.PopoutFrameModule$1 */
    class C04591 implements Callback {
        C04591() {
        }

        public void surfaceCreated(SurfaceHolder holder) {
            CamLog.m7i(CameraConstants.TAG, "[popout] surfaceCreated");
            if (PopoutFrameModule.this.isCameraDeviceAvailable()) {
                CamLog.m3d(CameraConstants.TAG, "[popout] Create popout camera engine");
                PopoutFrameModule.this.mGet.setPreviewCoverVisibility(0, true);
                if (PopoutFrameModule.this.mShaderParameterData != null) {
                    PopoutFrameModule.this.mShaderParameterData.setSingleModeRatio(1.0f);
                    PopoutFrameModule.this.mShaderParameterData.setNormToWideRatio(new float[]{0.625f, 0.72f, 0.75f, 0.55f});
                    PopoutFrameModule.this.setFrameBitmap(PopoutFrameModule.this.mCurrentFrameShape, PopoutFrameModule.this.mScreenSize, true);
                    PopoutFrameModule.this.setNormalPreviewRect(PopoutFrameModule.this.mBoundRect);
                    Camera camera = null;
                    if (FunctionProperties.getSupportedHal() != 2) {
                        camera = (Camera) PopoutFrameModule.this.mCameraDevice.getCamera();
                    }
                    PopoutFrameModule.this.mPopoutFrameEngine = new LGPopoutEffectEngine(camera, camera, PopoutFrameModule.this.mPopoutEffectEnginListener, holder, false);
                    PopoutFrameModule.this.setPopoutEffect();
                    PopoutFrameModule.this.mPopoutFrameEngine.setFrameType(PopoutFrameModule.this.mFrameBmp, PopoutFrameModule.this.mMaskBmp, PopoutFrameModule.this.mBoundRect, new RectF(0.0f, 0.0f, 1.0f, 1.0f));
                }
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            CamLog.m7i(CameraConstants.TAG, "[popout] surfaceChanged");
            if (PopoutFrameModule.this.mPopoutFrameEngine != null) {
                PopoutFrameModule.this.mPopoutFrameEngine.surfaceChanged(holder, width, height);
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            CamLog.m7i(CameraConstants.TAG, "[popout] surfaceDestroyed");
            if (PopoutFrameModule.this.mPopoutFrameEngine != null && !PopoutFrameModule.this.mIsSavingPicture) {
                CamLog.m3d(CameraConstants.TAG, "[popout] Release popout engine");
                PopoutFrameModule.this.mPopoutFrameEngine.release();
                PopoutFrameModule.this.mPopoutFrameEngine = null;
            }
        }
    }

    /* renamed from: com.lge.camera.app.ext.PopoutFrameModule$2 */
    class C04612 implements LGPopoutEffectEngineListener {
        C04612() {
        }

        public void onSendStillImage(Bitmap mResultBitmapImage) {
            PopoutFrameModule.this.onPopoutPictureTaken(mResultBitmapImage);
        }

        public void onErrorOccured(int errCode) {
            CamLog.m7i(CameraConstants.TAG, "[popout] onErrorOccured : " + errCode);
        }

        public void onEngineInitializeDone(SurfaceTexture normalTexture, SurfaceTexture wideTexture) {
            CamLog.m7i(CameraConstants.TAG, "[popout] onEngineInitializeDone");
            if (PopoutFrameModule.this.isCameraDeviceAvailable()) {
                SurfaceTexture mainTexture;
                if (PopoutFrameModule.this.mIsMultiPopoutMode) {
                    mainTexture = normalTexture;
                } else {
                    mainTexture = wideTexture;
                }
                if (ModelProperties.isMTKChipset()) {
                    PopoutFrameModule.this.mCameraDevice.setPreviewTexture(mainTexture);
                    PopoutFrameModule.this.startPreviewForNormalCamera(null, false);
                    if (PopoutFrameModule.this.mIsMultiPopoutMode) {
                        CameraSecondHolder.subinstance().setPreviewTexture(wideTexture);
                        PopoutFrameModule.this.startPreviewForWideCamera(null, false);
                    }
                } else {
                    PopoutFrameModule.this.mCameraDevice.setPreviewTexture(mainTexture);
                    if (PopoutFrameModule.this.mIsMultiPopoutMode) {
                        CameraSecondHolder.subinstance().setPreviewTexture(wideTexture);
                        PopoutFrameModule.this.startPreviewForWideCamera(null, false);
                    }
                    PopoutFrameModule.this.startPreviewForNormalCamera(null, false);
                }
                if (PopoutFrameModule.this.mPopoutFrameEngine != null) {
                    PopoutFrameModule.this.mPopoutFrameEngine.refreshCameraParameter();
                }
                ShaderParameterData.getInstance().setFPSMode(PopoutFrameModule.this.mIsShowFpsLog);
                PopoutFrameModule.this.mCameraStartUpThread = null;
                PopoutFrameModule.this.setCameraState(1);
                PopoutFrameModule.this.mGet.runOnUiThread(new HandlerRunnable(PopoutFrameModule.this) {
                    public void handleRun() {
                        PopoutFrameModule.this.access$500(true);
                        PopoutFrameModule.this.mPopoutFrameManager.setPreviewSizeOnScreen(PopoutFrameModule.this.mScreenSize);
                        PopoutFrameModule.this.setPictureSizeListAndSettingMenu(true);
                    }
                });
                CamLog.m3d(CameraConstants.TAG, "[popout] startPreview - end");
            }
        }

        public RectF onRequestBoundaryPosition() {
            return PopoutFrameModule.this.mBoundRect;
        }

        public void displayFPS(float arg0, boolean arg1) {
        }
    }

    public class FramePositionInfo {
        public boolean mCheckRatio;
        public int mHeight;
        public int mTopMargin;
        public int mWidth;

        public FramePositionInfo(int width, int height, int topMargin, boolean checkRatio) {
            this.mWidth = width;
            this.mHeight = height;
            this.mTopMargin = topMargin;
            this.mCheckRatio = checkRatio;
        }
    }

    public PopoutFrameModule(ActivityBridge activityBridge) {
        super(activityBridge);
        this.mPopoutFrameManager = new PopoutFrameManager(this);
        this.mShaderParameterData = ShaderParameterData.getInstance();
        this.mBoundRect = new RectF(0.25f, 0.25f, 0.75f, 0.75f);
        this.mBoundRectBackup = new RectF();
        this.mBeforeDegree = 270;
        this.mIsFrameMoved = false;
        this.mAnimatedPictureState = 1;
        this.mFrameDefaultSizeMap = new HashMap();
        this.mCallback = new C04591();
        this.mPopoutEffectEnginListener = new C04612();
        this.mRotateBitmapHandler = new HandlerRunnable(this) {
            public void handleRun() {
                if (!PopoutFrameModule.this.isPaused() && PopoutFrameModule.this.mPopoutFrameEngine != null) {
                    String screenSizeStr = PopoutFrameModule.this.mScreenSize;
                    if (!(PopoutFrameModule.this.checkModuleValidate(192) || PopoutFrameModule.this.mAnimatedPictureState == 2)) {
                        screenSizeStr = PopoutFrameModule.this.getListPreference(SettingKeyWrapper.getVideoSizeKey(PopoutFrameModule.this.getShotMode(), PopoutFrameModule.this.mCameraId)).getExtraInfo(2, PopoutFrameModule.this.mGet.getSettingIndex(SettingKeyWrapper.getVideoSizeKey(PopoutFrameModule.this.getShotMode(), PopoutFrameModule.this.mCameraId)));
                    }
                    int[] screenSize = Utils.sizeStringToArray(screenSizeStr);
                    float aspect = ((float) screenSize[0]) / ((float) screenSize[1]);
                    if (PopoutFrameModule.this.mCurrentFrameShape == 5 || PopoutFrameModule.this.mCurrentFrameShape == 1) {
                        PopoutFrameModule.this.adjustAndRotateBitmapForSquareType(false, true, aspect, PopoutFrameModule.this.getOrientationDegree());
                        PopoutFrameModule.this.mPopoutFrameEngine.setFrameType(PopoutFrameModule.this.mFrameBmp, PopoutFrameModule.this.mMaskBmp, PopoutFrameModule.this.mBoundRect, new RectF(0.0f, 0.0f, 1.0f, 1.0f));
                    } else if (PopoutFrameModule.this.mCurrentFrameShape == 6) {
                        PopoutFrameModule.this.setInstantPicBitmapByDegree(PopoutFrameModule.this.mCurrentFrameShape, aspect, PopoutFrameModule.this.getOrientationDegree(), (FramePositionInfo) PopoutFrameModule.this.mFrameDefaultSizeMap.get(Integer.valueOf(PopoutFrameModule.this.mCurrentFrameShape)));
                        PopoutFrameModule.this.mPopoutFrameEngine.setFrameType(PopoutFrameModule.this.mFrameBmp, PopoutFrameModule.this.mMaskBmp, PopoutFrameModule.this.mBoundRect, new RectF(0.0f, 0.0f, 1.0f, 1.0f));
                    }
                }
            }
        };
        this.mCurrentFrameShape = 1;
    }

    public void init() {
        super.init();
        if (this.mPopoutFrameManager != null) {
            this.mPopoutFrameManager.setOnPopoutFrameListener(this);
        }
        this.mFrameDefaultSizeMap.put(Integer.valueOf(0), new FramePositionInfo(264, 264, 1, true));
        this.mFrameDefaultSizeMap.put(Integer.valueOf(1), new FramePositionInfo(275, 275, 1, true));
        this.mFrameDefaultSizeMap.put(Integer.valueOf(5), new FramePositionInfo(275, 275, 1, true));
        this.mFrameDefaultSizeMap.put(Integer.valueOf(6), new FramePositionInfo(360, 360, 0, true));
        this.mFrameDefaultSizeMap.put(Integer.valueOf(7), new FramePositionInfo(330, 220, 1, false));
        this.mBoundRect.set(getDefaultPositionRect((FramePositionInfo) this.mFrameDefaultSizeMap.get(Integer.valueOf(this.mCurrentFrameShape))));
        this.mBoundRectBackup.set(this.mBoundRect);
    }

    protected void addModuleManager() {
        super.addModuleManager();
        this.mManagerList.add(this.mPopoutFrameManager);
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        restoreSquareFrameSize();
        if (this.mPopoutFrameEngine != null) {
            CamLog.m3d(CameraConstants.TAG, "[popout] Release popout engine");
            this.mPopoutFrameEngine.release();
            this.mPopoutFrameEngine = null;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mFrameDefaultSizeMap != null) {
            this.mFrameDefaultSizeMap.clear();
        }
        this.mIsFrameMoved = false;
    }

    protected RectF getDefaultPositionRect(FramePositionInfo info) {
        if (info == null) {
            CamLog.m3d(CameraConstants.TAG, "[popout] FramePositionInfo is null, so return defulat rect");
            return new RectF(0.25f, 0.25f, 0.75f, 0.75f);
        }
        float width = Utils.dpToPx(getAppContext(), (float) info.mWidth) * (info.mCheckRatio ? ((float) this.mLCDSize[0]) / ((float) this.mLCDSize[1]) : 1.0f);
        float height = Utils.dpToPx(getAppContext(), (float) info.mHeight);
        float topMargin = 0.0f;
        if (info.mTopMargin != 0) {
            topMargin = (((float) this.mLCDSize[1]) - height) / 2.0f;
        }
        float leftMargin = (((float) this.mLCDSize[0]) - width) / 2.0f;
        float left = leftMargin / ((float) this.mLCDSize[0]);
        float top = topMargin / ((float) this.mLCDSize[1]);
        float right = (leftMargin + width) / ((float) this.mLCDSize[0]);
        float bottom = (topMargin + height) / ((float) this.mLCDSize[1]);
        CamLog.m3d(CameraConstants.TAG, "[popout] Default position, left : " + left + ", top : " + top + ", right : " + right + ", bottom : " + bottom);
        return new RectF(left, top, right, bottom);
    }

    protected void setupPreview(CameraParameters params) {
        CamLog.m7i(CameraConstants.TAG, "[popout] setupPreview");
        if (this.mCameraDevice == null) {
            CamLog.m3d(CameraConstants.TAG, "[popout] Camera device is null!");
            return;
        }
        this.mIsAllPreviewAvailable = 0;
        CameraParameters param = this.mCameraDevice.getParameters();
        param.set(ParamConstants.KEY_DUAL_RECORDER, 1);
        param.set(ParamConstants.KEY_LGE_CAMERA, 1);
        CamLog.m3d(CameraConstants.TAG, "[popout] Normal camera pre setParameter");
        this.mCameraDevice.setParameters(param);
        if (this.mIsMultiPopoutMode) {
            int cameraId = 2;
            if (FunctionProperties.getCameraTypeFront() == 1) {
                cameraId = 1;
            } else if (FunctionProperties.getCameraTypeRear() == 0) {
                cameraId = 1;
            }
            CameraSecondHolder.subinstance().open(this.mHandler, cameraId, null, false, this.mGet.getActivity());
        }
        makePopoutPreviewAndPictureSizeList(param, CameraSecondHolder.subinstance().getParameters());
        CamLog.m3d(CameraConstants.TAG, "[popout] Create popout surfaceView");
        setNormalPreviewRect(this.mBoundRect);
        this.mPopoutSurfaceView = (SurfaceView) findViewById(C0088R.id.preview_surface_popout);
        this.mPopoutSurfaceView.getHolder().addCallback(this.mCallback);
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessage(86);
        }
    }

    protected void stopRecorder(boolean useDB) {
        CamLog.m3d(CameraConstants.TAG, "[popout] stopRecorder.");
        if (this.mPopoutFrameEngine != null) {
            this.mPopoutFrameEngine.stopRecording();
        }
        super.stopRecorder(useDB);
    }

    protected void setPopoutDrawMode(float[] perspectiveValue, float[] grayValue, float[] vignettingValue, float[] blurValue) {
        boolean z = true;
        this.mShaderParameterData.setPerspectiveValue(perspectiveValue);
        this.mShaderParameterData.setBlurValue(blurValue);
        this.mShaderParameterData.setGrayValue(grayValue);
        this.mShaderParameterData.setVignettingValue(vignettingValue);
        LGPopoutEffectEngine lGPopoutEffectEngine = this.mPopoutFrameEngine;
        boolean z2 = (this.mCurrentBackgroundEffect & 1) != 0;
        boolean z3 = (this.mCurrentBackgroundEffect & 8) != 0;
        boolean z4 = (this.mCurrentBackgroundEffect & 4) != 0;
        if ((this.mCurrentBackgroundEffect & 2) == 0) {
            z = false;
        }
        lGPopoutEffectEngine.setDrawMode(z2, z3, z4, z);
    }

    public boolean onPopoutEffectBtnClick(int type) {
        if (this.mAnimatedPictureState != 1) {
            return false;
        }
        if (this.mPopoutFrameEngine == null) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "[popout] set " + type + " effect");
        changePopoutBackgrondEffect(type);
        setPopoutEffect();
        return true;
    }

    public void onPopoutFrameBtnClick(int frame) {
        if (this.mPopoutFrameEngine != null && this.mPopoutFrameManager != null) {
            CamLog.m3d(CameraConstants.TAG, "[popout] frame is changed : " + frame);
            setFrameBitmap(frame, this.mScreenSize, false);
            setNormalPreviewRect(this.mBoundRect);
            checkRectBoundary(frame);
            this.mPopoutFrameEngine.setFrameType(this.mFrameBmp, this.mMaskBmp, this.mBoundRect, new RectF(0.0f, 0.0f, 1.0f, 1.0f));
            this.mPopoutFrameEngine.refreshCameraParameter();
            this.mCurrentFrameShape = frame;
            this.mPopoutFrameManager.setFrameType(frame);
            this.mPopoutFrameManager.setPreviewSizeOnScreen(this.mScreenSize);
            this.mPopoutFrameManager.onFrameTypeChanged(this.mBoundRect);
        }
    }

    private void checkRectBoundary(int frame) {
        float f = 0.0f;
        if (this.mBoundRect != null && frame == 0) {
            float f2;
            if (((double) this.mBoundRect.width()) > 0.75d || ((double) this.mBoundRect.height()) > 0.75d) {
                float leftMargin = (this.mBoundRect.width() - 0.75f) / 2.0f;
                float topMargin = (this.mBoundRect.height() - 0.75f) / 2.0f;
                this.mBoundRect.set(this.mBoundRect.left + leftMargin, this.mBoundRect.top + topMargin, this.mBoundRect.right - leftMargin, this.mBoundRect.bottom - topMargin);
            }
            if (this.mBoundRect.left < 0.0f) {
                f2 = -this.mBoundRect.left;
            } else {
                f2 = 0.0f;
            }
            float landMove = f2 + (this.mBoundRect.right > 1.0f ? 1.0f - this.mBoundRect.right : 0.0f);
            if (this.mBoundRect.top < 0.0f) {
                f2 = -this.mBoundRect.top;
            } else {
                f2 = 0.0f;
            }
            if (this.mBoundRect.bottom > 1.0f) {
                f = 1.0f - this.mBoundRect.bottom;
            }
            float portMove = f2 + f;
            this.mBoundRect.set(this.mBoundRect.left + landMove, this.mBoundRect.top + portMove, this.mBoundRect.right + landMove, this.mBoundRect.bottom + portMove);
            CamLog.m3d(CameraConstants.TAG, "[popout] mBoundRect.left : " + this.mBoundRect.left + ", mBoundRect.top : " + this.mBoundRect.top + ", mBoundRect.right : " + this.mBoundRect.right + ", mBoundRect.bottom : " + this.mBoundRect.bottom);
            this.mBoundRectBackup.set(this.mBoundRect);
            setNormalPreviewRect(this.mBoundRect);
        }
    }

    public String getShotMode() {
        return CameraConstants.MODE_POPOUT_CAMERA;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mPopoutFrameManager == null || !this.mPopoutFrameManager.onTouchEvent(event)) {
            return super.onTouchEvent(event);
        }
        return true;
    }

    protected void setFrameBitmap(int frame, String screenSize, boolean callBySetupPreview) {
        int[] size = Utils.sizeStringToArray(screenSize);
        float aspect = ((float) size[0]) / ((float) size[1]);
        int degree = getOrientationDegree();
        FramePositionInfo info = (FramePositionInfo) this.mFrameDefaultSizeMap.get(Integer.valueOf(frame));
        switch (frame) {
            case 0:
                setBitmapForMovableFrame(frame, C0088R.drawable.popout_frame_square, C0088R.drawable.popout_frame_mask_square, info, aspect, degree);
                return;
            case 1:
                setBitmapForMovableFrame(frame, C0088R.drawable.popout_frame_circle_1280x1280, C0088R.drawable.popout_frame_mask_circle_1280x1280, info, aspect, degree);
                return;
            case 4:
                setFrameAndMaskBitmap(C0088R.drawable.popout_frame_horizontal_divide, C0088R.drawable.popout_frame_mask_horizontal_divide);
                if (!(this.mCurrentFrameShape == 7 || callBySetupPreview)) {
                    this.mBoundRectBackup.set(this.mBoundRect);
                }
                this.mBoundRect.set(0.0f, 0.0f, 1.0f, 1.0f);
                return;
            case 5:
                setBitmapForMovableFrame(frame, C0088R.drawable.popout_frame_heart_1280x1280, C0088R.drawable.popout_frame_mask_heart_1280x1280, info, aspect, degree);
                return;
            case 6:
                setInstantPicBitmapByDegree(frame, aspect, degree, info);
                return;
            case 7:
                setFrameAndMaskBitmap(C0088R.drawable.popout_frame_film_2560x1440, C0088R.drawable.popout_frame_mask_film_2560x1440);
                if (!(this.mCurrentFrameShape == 4 || callBySetupPreview)) {
                    this.mBoundRectBackup.set(this.mBoundRect);
                }
                this.mBoundRect.set(0.0f, 0.0f, 1.0f, 1.0f);
                return;
            default:
                return;
        }
    }

    private void setInstantPicBitmapByDegree(int frame, float aspect, int degree, FramePositionInfo info) {
        if (degree == 0 || degree == 180) {
            setBitmapForMovableFrame(frame, C0088R.drawable.popout_frame_polaroid_ver_720x720, C0088R.drawable.popout_frame_mask_polaroid_ver_720x720, info, aspect, degree);
        } else {
            setBitmapForMovableFrame(frame, C0088R.drawable.popout_frame_polaroid_720x720, C0088R.drawable.popout_frame_mask_polaroid_720x720, info, aspect, degree);
        }
    }

    private void addDateInInstantPicture(int degree) {
        Bitmap tmp = this.mFrameBmp.copy(Config.ARGB_8888, true);
        this.mFrameBmp.recycle();
        this.mFrameBmp = null;
        Canvas c = new Canvas(tmp);
        Paint p = new Paint(1);
        p.setTextSize((float) ((int) Utils.dpToPx(getAppContext(), 12.0f)));
        p.setTypeface(CameraTypeface.get(getAppContext(), "HYRPostM_13_0624.ttf"));
        SimpleDateFormat dateFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyy.MM.dd"));
        int fontStartX = (int) Utils.dpToPx(getAppContext(), 163.0f);
        int fontStartY = (int) Utils.dpToPx(getAppContext(), 181.0f);
        if (degree == 0 || degree == 180) {
            fontStartX = (int) Utils.dpToPx(getAppContext(), 138.0f);
            fontStartY = (int) Utils.dpToPx(getAppContext(), 221.0f);
        }
        c.drawText(dateFormat.format(new Date(System.currentTimeMillis())), (float) fontStartX, (float) fontStartY, p);
        this.mFrameBmp = tmp.copy(Config.ARGB_8888, true);
        tmp.recycle();
    }

    private void setBitmapForMovableFrame(int frameIndex, int frameId, int mastId, FramePositionInfo info, float ratio, int degree) {
        this.mFrameBmp = BitmapFactory.decodeResource(this.mGet.getAppContext().getResources(), frameId);
        this.mMaskBmp = BitmapFactory.decodeResource(this.mGet.getAppContext().getResources(), mastId);
        if (frameIndex == 6) {
            addDateInInstantPicture(degree);
        }
        if (frameIndex != 0) {
            adjustAndRotateBitmapForSquareType(true, true, ratio, degree);
        }
        if (!this.mIsFrameMoved) {
            this.mBoundRectBackup.set(getDefaultPositionRect(info));
            if (this.mPopoutFrameManager != null) {
                this.mPopoutFrameManager.initDefaultResizeHandlerPosition(this.mBoundRectBackup);
            }
        }
        this.mBoundRect.set(this.mBoundRectBackup);
    }

    private void setFrameAndMaskBitmap(int frameId, int mastId) {
        this.mFrameBmp = BitmapFactory.decodeResource(this.mGet.getAppContext().getResources(), frameId);
        this.mMaskBmp = BitmapFactory.decodeResource(this.mGet.getAppContext().getResources(), mastId);
    }

    private void adjustAndRotateBitmapForSquareType(boolean isAdjust, boolean isRotate, float ratio, int degree) {
        if (isAdjust) {
            this.mFrameBmp = adjustFrame(this.mFrameBmp, ratio);
            this.mMaskBmp = adjustFrame(this.mMaskBmp, ratio);
            this.mBeforeDegree = 270;
        }
        if (isRotate) {
            this.mFrameBmp = rotateFrame(this.mFrameBmp, ratio, degree);
            this.mMaskBmp = rotateFrame(this.mMaskBmp, ratio, degree);
            this.mBeforeDegree = degree;
        }
    }

    public Bitmap adjustFrame(Bitmap src, float aspect) {
        int start = (int) ((((float) src.getHeight()) * (aspect - 1.0f)) / 2.0f);
        Bitmap adjust = Bitmap.createBitmap((int) (((float) src.getHeight()) * aspect), src.getHeight(), Config.ARGB_8888);
        Canvas c = new Canvas(adjust);
        c.save();
        c.drawBitmap(src, (float) start, 0.0f, null);
        c.restore();
        c.release();
        src.recycle();
        return adjust;
    }

    public Bitmap rotateFrame(Bitmap src, float aspect, int degree) {
        int rotateDegree = degree - this.mBeforeDegree;
        int start = (int) ((((float) src.getHeight()) * (aspect - 1.0f)) / 2.0f);
        int shorten = src.getHeight();
        int longer = (int) (((float) src.getHeight()) * aspect);
        Bitmap rotate = Bitmap.createBitmap(longer, shorten, Config.ARGB_8888);
        Canvas c = new Canvas(rotate);
        c.save();
        c.rotate((float) (-rotateDegree), ((float) longer) / 2.0f, ((float) shorten) / 2.0f);
        c.drawBitmap(src, new Rect(start, 0, start + shorten, shorten), new Rect(start, 0, start + shorten, shorten), null);
        c.restore();
        c.release();
        src.recycle();
        return rotate;
    }

    protected void setNormalPreviewRect(RectF rect) {
        if (this.mPopoutFrameManager != null) {
            CamLog.m3d(CameraConstants.TAG, "[popout] mBoundRect.left : " + this.mBoundRect.left + ", mBoundRect.top : " + this.mBoundRect.top + ", mBoundRect.right : " + this.mBoundRect.right + ", mBoundRect.bottom : " + this.mBoundRect.bottom);
            this.mPopoutFrameManager.setCurNormalPreviewPosition(rect);
        }
    }

    public void onFrameMovingDone(RectF rect, boolean isMoved) {
        this.mBoundRect.set(rect);
        this.mBoundRectBackup.set(this.mBoundRect);
        if (!this.mIsFrameMoved && isMoved) {
            this.mIsFrameMoved = isMoved;
        }
    }

    protected void sendPopoutStillImage() {
        if (this.mPopoutFrameEngine != null) {
            CamLog.m3d(CameraConstants.TAG, "[popout] All jpeg data ready");
            showSavingDialog(true, 0);
            if (this.mIsMultiPopoutMode) {
                this.mPopoutFrameEngine.setStillImage(this.mNormalViewCaptureData, this.mWideViewCaptureData);
            } else {
                this.mPopoutFrameEngine.setStillImage(this.mNormalViewCaptureData);
            }
        }
    }

    protected void prepareReordingOnEngine(Surface surface, int width, int height) {
        if (this.mPopoutFrameEngine != null) {
            this.mPopoutFrameEngine.prepareRecording(surface, width, height);
        }
    }

    protected void startRecordingOnEngine(boolean isFixedWidePreview) {
        if (this.mPopoutFrameEngine != null) {
            this.mPopoutFrameEngine.startRecording(isFixedWidePreview);
        }
    }

    protected void refreshCameraParamForEngine() {
        if (this.mPopoutFrameEngine != null) {
            this.mPopoutFrameEngine.refreshCameraParameter();
        }
    }

    protected void startPreview(CameraParameters params) {
        refreshCameraParamForEngine();
        super.startPreview(params);
    }

    protected void onChangePictureSize() {
        super.onChangePictureSize();
        changeFrameSize(true, this.mScreenSize);
    }

    private void changeFrameSize(boolean updateEngine, String screenSize) {
        CamLog.m3d(CameraConstants.TAG, "[popout] changeFrameSize, updateEngine : " + updateEngine + ", screenSize : " + screenSize);
        if (this.mPopoutFrameManager != null) {
            this.mPopoutFrameManager.setPreviewSizeOnScreen(screenSize);
            if (this.mPopoutFrameManager.isSquareFrameType()) {
                setFrameBitmap(this.mCurrentFrameShape, screenSize, false);
                this.mPopoutFrameManager.checkScreenBoundary();
                this.mPopoutFrameManager.updateNormalPreview(false);
                setNormalPreviewRect(this.mBoundRect);
                if (this.mPopoutFrameEngine != null && updateEngine) {
                    this.mPopoutFrameEngine.setFrameType(this.mFrameBmp, this.mMaskBmp, this.mBoundRect, new RectF(0.0f, 0.0f, 1.0f, 1.0f));
                }
            }
        }
    }

    private void restoreSquareFrameSize() {
        if (this.mPopoutFrameManager != null && this.mPopoutFrameManager.isSquareFrameType()) {
            this.mPopoutFrameManager.setPreviewSizeOnScreen(this.mScreenSize);
            this.mPopoutFrameManager.checkScreenBoundary();
            this.mPopoutFrameManager.updateNormalPreview(false);
            setNormalPreviewRect(this.mBoundRect);
        }
    }

    public void onOrientationChanged(int degree, boolean isFirst) {
        super.onOrientationChanged(degree, isFirst);
        if (this.mRotateBitmapHandler != null) {
            removePostRunnable(this.mRotateBitmapHandler);
            runOnUiThread(this.mRotateBitmapHandler);
        }
    }

    public void onVideoShutterClicked() {
        if (this.mPopoutFrameManager != null) {
            this.mPopoutFrameManager.hideSubWindowResizeHandler();
        }
        super.onVideoShutterClicked();
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                PopoutFrameModule.this.changeFrameSize(true, PopoutFrameModule.this.getListPreference(SettingKeyWrapper.getVideoSizeKey(PopoutFrameModule.this.getShotMode(), PopoutFrameModule.this.mCameraId)).getExtraInfo(2, PopoutFrameModule.this.mGet.getSettingIndex(SettingKeyWrapper.getVideoSizeKey(PopoutFrameModule.this.getShotMode(), PopoutFrameModule.this.mCameraId))));
            }
        });
    }

    protected void afterStopRecording() {
        super.afterStopRecording();
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (!PopoutFrameModule.this.isPaused()) {
                    PopoutFrameModule.this.changeFrameSize(true, PopoutFrameModule.this.mScreenSize);
                }
            }
        });
    }

    public void onFrameLongPressed() {
        hideZoomBar();
    }
}
