package com.lge.camera.app.ext;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
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
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import com.lge.effectEngine.LGPopoutEffectEngineListener;
import com.lge.effectEngine.ShaderParameterData;
import com.lge.lgpopouteffectengine.LGPopoutEffectEngine;

public class PopoutShapeModule extends PopoutModule {
    public Callback mCallback = new C04811();
    protected LGPopoutEffectEngineListener mPopoutEffectEnginListener = new C04832();
    protected LGPopoutEffectEngine mPopoutEffectEngine;
    protected ShaderParameterData mShaderParameterData = ShaderParameterData.getInstance();

    /* renamed from: com.lge.camera.app.ext.PopoutShapeModule$1 */
    class C04811 implements Callback {
        C04811() {
        }

        public void surfaceCreated(SurfaceHolder holder) {
            CamLog.m7i(CameraConstants.TAG, "[popout] surfaceCreated");
            if (PopoutShapeModule.this.isCameraDeviceAvailable()) {
                CamLog.m3d(CameraConstants.TAG, "[popout] Create popout camera engine");
                if (PopoutShapeModule.this.mShaderParameterData != null) {
                    PopoutShapeModule.this.setFrameShadowEffect(PopoutShapeModule.this.mCurrentFrameShape);
                    PopoutShapeModule.this.mShaderParameterData.setSingleModeRatio(1.0f);
                    PopoutShapeModule.this.mShaderParameterData.setNormToWideRatio(new float[]{0.625f, 0.72f, 0.75f, 0.55f});
                    int[] lcdSize = Utils.getLCDsize(PopoutShapeModule.this.getAppContext(), true);
                    int previewMargin1by1 = (lcdSize[0] - lcdSize[1]) / 2;
                    if (ModelProperties.isLongLCDModel()) {
                        PopoutShapeModule.this.mShaderParameterData.setMarginLeft(new int[]{lcdSize[0], lcdSize[1], RatioCalcUtil.getLongLCDModelTopMargin(PopoutShapeModule.this.getAppContext(), 16, 9, 0), RatioCalcUtil.getQuickButtonWidth(PopoutShapeModule.this.mGet.getAppContext()), RatioCalcUtil.getLongLCDModelTopMargin(PopoutShapeModule.this.getAppContext(), 1, 1, 0), 0});
                    } else {
                        PopoutShapeModule.this.mShaderParameterData.setMarginLeft(new int[]{lcdSize[0], lcdSize[1], 0, RatioCalcUtil.getQuickButtonWidth(PopoutShapeModule.this.mGet.getAppContext()), previewMargin1by1, 0});
                    }
                    Camera camera = null;
                    if (FunctionProperties.getSupportedHal() != 2) {
                        camera = (Camera) PopoutShapeModule.this.mCameraDevice.getCamera();
                    }
                    if (PopoutShapeModule.this.mIsMultiPopoutMode) {
                        String[] sizenNormal = PopoutShapeModule.this.mNormalCameraPreviewSize.split("x");
                        String[] sizeWide = PopoutShapeModule.this.mWideCameraPreviewSize.split("x");
                        PopoutShapeModule.this.mPopoutEffectEngine = new LGPopoutEffectEngine(holder, PopoutShapeModule.this.mPopoutEffectEnginListener, false, Integer.parseInt(sizenNormal[0]), Integer.parseInt(sizenNormal[1]), Integer.parseInt(sizeWide[0]), Integer.parseInt(sizeWide[1]), FunctionProperties.getSupportedHal() == 2);
                    } else {
                        PopoutShapeModule.this.mPopoutEffectEngine = new LGPopoutEffectEngine(camera, holder, PopoutShapeModule.this.mPopoutEffectEnginListener, false);
                    }
                    PopoutShapeModule.this.setPopoutEffect();
                    PopoutShapeModule.this.mPopoutEffectEngine.setFrameType(PopoutShapeModule.this.mCurrentFrameShape);
                }
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            CamLog.m7i(CameraConstants.TAG, "[popout] surfaceChanged");
            if (PopoutShapeModule.this.mPopoutEffectEngine != null) {
                PopoutShapeModule.this.mPopoutEffectEngine.surfaceChanged(holder, width, height);
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            CamLog.m7i(CameraConstants.TAG, "[popout] surfaceDestroyed");
            if (PopoutShapeModule.this.mPopoutEffectEngine != null && !PopoutShapeModule.this.mIsSavingPicture) {
                CamLog.m3d(CameraConstants.TAG, "[popout] Release popout engine");
                PopoutShapeModule.this.mPopoutEffectEngine.release();
                PopoutShapeModule.this.mPopoutEffectEngine = null;
            }
        }
    }

    /* renamed from: com.lge.camera.app.ext.PopoutShapeModule$2 */
    class C04832 implements LGPopoutEffectEngineListener {
        C04832() {
        }

        public void onSendStillImage(Bitmap mResultBitmapImage) {
            PopoutShapeModule.this.onPopoutPictureTaken(mResultBitmapImage);
        }

        public void onErrorOccured(int errCode) {
            CamLog.m7i(CameraConstants.TAG, "[popout] onErrorOccured");
        }

        public void onEngineInitializeDone(SurfaceTexture normalTexture, SurfaceTexture wideTexture) {
            CamLog.m7i(CameraConstants.TAG, "[popout] onEngineInitializeDone");
            if (PopoutShapeModule.this.isCameraDeviceAvailable()) {
                SurfaceTexture mainTexture;
                if (PopoutShapeModule.this.mIsMultiPopoutMode) {
                    mainTexture = normalTexture;
                } else {
                    mainTexture = wideTexture;
                }
                if (ModelProperties.isMTKChipset()) {
                    PopoutShapeModule.this.mCameraDevice.setPreviewTexture(mainTexture);
                    PopoutShapeModule.this.startPreviewForNormalCamera(null, false);
                    if (PopoutShapeModule.this.mIsMultiPopoutMode) {
                        CameraSecondHolder.subinstance().setPreviewTexture(wideTexture);
                        PopoutShapeModule.this.startPreviewForWideCamera(null, false);
                    }
                } else {
                    PopoutShapeModule.this.mCameraDevice.setPreviewTexture(mainTexture);
                    if (PopoutShapeModule.this.mIsMultiPopoutMode) {
                        CameraSecondHolder.subinstance().setPreviewTexture(wideTexture);
                        PopoutShapeModule.this.startPreviewForWideCamera(null, false);
                    }
                    PopoutShapeModule.this.startPreviewForNormalCamera(null, false);
                }
                if (PopoutShapeModule.this.mPopoutEffectEngine != null) {
                    PopoutShapeModule.this.mPopoutEffectEngine.refreshCameraParameter();
                }
                ShaderParameterData.getInstance().setFPSMode(PopoutShapeModule.this.mIsShowFpsLog);
                PopoutShapeModule.this.mCameraStartUpThread = null;
                if (PopoutShapeModule.this.getCameraState() != 5) {
                    PopoutShapeModule.this.setCameraState(1);
                }
                PopoutShapeModule.this.mGet.runOnUiThread(new HandlerRunnable(PopoutShapeModule.this) {
                    public void handleRun() {
                        PopoutShapeModule.this.access$600(true);
                        PopoutShapeModule.this.setPictureSizeListAndSettingMenu(true);
                    }
                });
                CamLog.m3d(CameraConstants.TAG, "[popout] startPreview - end");
            }
        }
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        if (this.mPopoutEffectEngine != null) {
            CamLog.m3d(CameraConstants.TAG, "[popout] Release popout engine");
            this.mPopoutEffectEngine.release();
            this.mPopoutEffectEngine = null;
        }
    }

    protected void closeCamera() {
        if (!(this.mPopoutSurfaceView == null || this.mPopoutSurfaceView.getHolder() == null)) {
            this.mPopoutSurfaceView.getHolder().removeCallback(this.mCallback);
        }
        super.closeCamera();
    }

    public PopoutShapeModule(ActivityBridge activityBridge) {
        super(activityBridge);
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
            CameraSecondHolder.subinstance().open(this.mHolderHandler, cameraId, null, false, this.mGet.getActivity());
        }
        makePopoutPreviewAndPictureSizeList(param, CameraSecondHolder.subinstance().getParameters());
        CamLog.m3d(CameraConstants.TAG, "[popout] Create popout surfaceView");
        this.mPopoutSurfaceView = (SurfaceView) findViewById(C0088R.id.preview_surface_popout);
        this.mPopoutSurfaceView.getHolder().addCallback(this.mCallback);
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessage(86);
        }
    }

    protected void stopRecorder(boolean useDB) {
        CamLog.m3d(CameraConstants.TAG, "[popout] stopRecorder.");
        if (this.mPopoutEffectEngine != null) {
            this.mPopoutEffectEngine.stopRecording();
        }
        super.stopRecorder(useDB);
    }

    protected void setPopoutDrawMode(float[] perspectiveValue, float[] grayValue, float[] vignettingValue, float[] blurValue) {
        boolean z = true;
        this.mShaderParameterData.setPerspectiveValue(perspectiveValue);
        this.mShaderParameterData.setBlurValue(blurValue);
        this.mShaderParameterData.setGrayValue(grayValue);
        this.mShaderParameterData.setVignettingValue(vignettingValue);
        LGPopoutEffectEngine lGPopoutEffectEngine = this.mPopoutEffectEngine;
        boolean z2 = (this.mCurrentBackgroundEffect & 1) != 0;
        boolean z3 = (this.mCurrentBackgroundEffect & 8) != 0;
        boolean z4 = (this.mCurrentBackgroundEffect & 4) != 0;
        if ((this.mCurrentBackgroundEffect & 2) == 0) {
            z = false;
        }
        lGPopoutEffectEngine.setDrawMode(false, z2, z3, z4, z);
    }

    protected void setFrameShadowEffect(int frameType) {
        if (this.mShaderParameterData != null) {
            this.mShaderParameterData.setFrameThickness(2.0f);
            this.mShaderParameterData.setFrameShadowDX(0.0f);
            this.mShaderParameterData.setFrameShadowDY(0.0f);
            switch (frameType) {
                case 0:
                    this.mShaderParameterData.setFrameShadowsScale(1.015f);
                    this.mShaderParameterData.setFrameShadowsSize(14.0f);
                    this.mShaderParameterData.setFrameShadowColor(Color.argb(80, 0, 0, 0));
                    return;
                case 1:
                case 2:
                    this.mShaderParameterData.setFrameShadowsScale(1.007f);
                    this.mShaderParameterData.setFrameShadowsSize(9.0f);
                    this.mShaderParameterData.setFrameShadowColor(Color.argb(65, 0, 0, 0));
                    return;
                case 3:
                    this.mShaderParameterData.setFrameShadowsScale(1.015f);
                    this.mShaderParameterData.setFrameShadowsSize(15.0f);
                    this.mShaderParameterData.setFrameShadowColor(Color.argb(97, 0, 0, 0));
                    return;
                case 4:
                    this.mShaderParameterData.setFrameShadowsScale(1.011f);
                    this.mShaderParameterData.setFrameShadowsSize(11.0f);
                    this.mShaderParameterData.setFrameShadowColor(Color.argb(72, 0, 0, 0));
                    return;
                default:
                    return;
            }
        }
    }

    public boolean onPopoutEffectBtnClick(int type) {
        if (this.mPopoutEffectEngine != null) {
            CamLog.m3d(CameraConstants.TAG, "[popout] set " + type + " effect");
            changePopoutBackgrondEffect(type);
            setPopoutEffect();
        }
        return true;
    }

    public void onPopoutFrameBtnClick(int frame) {
        CamLog.m3d(CameraConstants.TAG, "[popout] frame is changed : " + frame);
        this.mCurrentFrameShape = frame;
        if (this.mPopoutEffectEngine != null) {
            this.mPopoutEffectEngine.setFrameType(frame);
            setFrameShadowEffect(frame);
            this.mPopoutEffectEngine.refreshCameraParameter();
        }
    }

    public String getShotMode() {
        return CameraConstants.MODE_POPOUT_CAMERA;
    }

    protected void sendPopoutStillImage() {
        if (this.mPopoutEffectEngine != null) {
            CamLog.m3d(CameraConstants.TAG, "[popout] All jpeg data ready");
            showSavingDialog(true, 0);
            if (this.mIsMultiPopoutMode) {
                this.mPopoutEffectEngine.setStillImage(this.mNormalViewCaptureData, this.mWideViewCaptureData);
            } else {
                this.mPopoutEffectEngine.setStillImage(this.mNormalViewCaptureData);
            }
        }
    }

    protected void prepareReordingOnEngine(Surface surface, int width, int height) {
        if (this.mPopoutEffectEngine != null) {
            this.mPopoutEffectEngine.prepareRecording(surface, width, height);
        }
    }

    protected void startRecordingOnEngine(boolean isFixedWidePreview) {
        if (this.mPopoutEffectEngine != null) {
            this.mPopoutEffectEngine.startRecording(isFixedWidePreview);
        }
    }

    protected void refreshCameraParamForEngine() {
        if (this.mPopoutEffectEngine != null) {
            this.mPopoutEffectEngine.refreshCameraParameter();
        }
    }

    protected boolean displayUIComponentAfterOneShot() {
        if (!super.displayUIComponentAfterOneShot()) {
            return false;
        }
        setPopoutLayoutVisibility(true);
        return true;
    }

    public void removeUIBeforeModeChange() {
        super.removeUIBeforeModeChange();
        setPopoutLayoutVisibility(false);
    }

    protected void releasePopoutEngine() {
        if (this.mPopoutEffectEngine != null && !this.mIsSavingPicture) {
            CamLog.m3d(CameraConstants.TAG, "[popout] Release popout engine");
            this.mPopoutEffectEngine.release();
            this.mPopoutEffectEngine = null;
        }
    }
}
