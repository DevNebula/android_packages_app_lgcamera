package com.lge.camera.managers.ext;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.RemoteException;
import android.text.TextUtils.TruncateAt;
import android.util.Size;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.managers.ext.SmartCamManagerBase.SmartCamThread;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorConverter;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SmartcamUtil;
import com.lge.camera.util.Utils;
import com.lge.ellievision.MotionHandShakeReset;
import com.lge.ellievision.parceldata.ISceneCategory;

public class SmartCamManager extends SmartCamManagerBase {
    private View mAICamInitGuideLayout;

    /* renamed from: com.lge.camera.managers.ext.SmartCamManager$1 */
    class C12631 implements OnClickListener {
        C12631() {
        }

        public void onClick(View arg0) {
            CamLog.m3d(CameraConstants.TAG, "ai cam init guide OK button clicked");
            if (SmartCamManager.this.mAICamInitGuideLayout != null) {
                CheckBox checkBox = (CheckBox) SmartCamManager.this.mAICamInitGuideLayout.findViewById(C0088R.id.ai_cam_init_guide_checkBox);
                if (checkBox != null && checkBox.isChecked()) {
                    SharedPreferenceUtil.setAICamInitGuideShown(SmartCamManager.this.getAppContext(), true);
                }
                SmartCamManager.this.hideInitGuide(true);
            }
        }
    }

    public SmartCamManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void onDestroy() {
        super.onDestroy();
        onDestroyViews();
        this.mIEllieVision = null;
        if (this.mMotionHandShakeReset != null) {
            this.mMotionHandShakeReset.unbind();
            this.mMotionHandShakeReset = null;
        }
        if (this.mSmartCamTagCloudList != null) {
            this.mSmartCamTagCloudList.clear();
            this.mSmartCamTagCloudList = null;
        }
        synchronized (this.mSync) {
            this.mOutRGBArray = null;
        }
        if (this.mPoint != null) {
            this.mPoint.clear();
            this.mPoint = null;
        }
        this.mCurrentCategory = null;
        this.mCurrentDisplayName = null;
        this.mSmartCamInterface = null;
        this.mIsBinningState = 0;
    }

    public void onDestroyViews() {
        if (!(this.mBaseView == null || this.mSmartCamView == null)) {
            this.mBaseView.removeView(this.mSmartCamView);
            this.mBaseView = null;
        }
        this.mSmartCamView = null;
        this.mSmartCamLayout = null;
        if (this.mTagCloudLayout != null) {
            this.mTagCloudLayout.removeAllViews();
            this.mTagCloudLayout = null;
        }
        if (this.mFilterEffectCover != null) {
            this.mFilterEffectCover.clearAnimation();
            this.mFilterEffectCover = null;
        }
        if (this.mPeopleEffectLayout != null) {
            ((FrameLayout) this.mPeopleEffectLayout).removeAllViews();
            this.mPeopleEffectView1st = null;
            this.mPeopleEffectView2nd = null;
            this.mPeopleEffectLayout = null;
            this.mPeopleEffectText = null;
        }
        this.mContrastBarLayout = null;
        if (this.mContrastBar != null) {
            this.mContrastBar.unbind();
            this.mContrastBar = null;
        }
        this.mEVBarLayout = null;
        if (this.mEVBar != null) {
            this.mEVBar.unbind();
            this.mEVBar = null;
        }
        this.mSceneBtn = null;
        this.mSceneBtnRotate = null;
        this.mSceneBtnImage = null;
        this.mSceneBtnText = null;
        hideInitGuide(false);
    }

    public void onStartSmartcam(boolean startRecording) {
        CamLog.m3d(CameraConstants.TAG, "AI-onStartSmartcam, startRecording : " + startRecording);
        resetSmartCam(true, startRecording);
        if (this.mSmartCamLayout != null) {
            this.mSmartCamLayout.setVisibility(0);
        }
        if (this.mShowTextViewThread == null) {
            this.mShowTextViewThread = new SmartCamThread("smart_cam_tag_cloud", this.mTagCloudRunnable, 500);
            this.mShowTextViewThread.start();
        }
        if (this.mMotionHandShakeReset == null) {
            this.mMotionHandShakeReset = new MotionHandShakeReset(getAppContext(), new MotionRegHandShakeResetCallback());
            this.mMotionHandShakeReset.onResume();
        }
        this.mPreviewCallbackTime = 0;
    }

    public void onStopSmartcam(boolean startRecording) {
        CamLog.m3d(CameraConstants.TAG, "AI-onStopSmartcam : " + startRecording);
        if (this.mIEllieVision != null) {
            if (SmartcamUtil.isSmartcamBindService() && this.mIsRegisterCallback) {
                try {
                    CamLog.m3d(CameraConstants.TAG, "AI-smartcam unregisterCallback");
                    this.mIEllieVision.unregisterCallback(this.mEllieServiceCallback);
                    this.mIsRegisterCallback = false;
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e1) {
                    e1.printStackTrace();
                }
            }
            synchronized (this.mSync) {
                this.mOutRGBArray = null;
            }
            resetSmartCam(false, startRecording);
            if (this.mSmartCamLayout != null) {
                this.mSmartCamLayout.setVisibility(8);
            }
            if (this.mShowTextViewThread != null && this.mShowTextViewThread.isAlive()) {
                this.mShowTextViewThread.stopThread();
                this.mShowTextViewThread = null;
            }
            if (this.mRecognitionThread != null && this.mRecognitionThread.isAlive()) {
                this.mRecognitionThread.stopThread();
                this.mRecognitionThread = null;
            }
            if (this.mMotionHandShakeReset != null) {
                this.mMotionHandShakeReset.onPause();
                this.mMotionHandShakeReset = null;
            }
            this.mPreviewCallbackTime = 0;
            this.mIsHandShakingResetSending = false;
        }
    }

    public void onImageData(Image image, byte[] data, CameraProxy camera) {
        if (this.mGet.isPaused()) {
            CamLog.m7i(CameraConstants.TAG, "AI-Camera Activity is paused. return");
            return;
        }
        if (FunctionProperties.getSupportedHal() != 2) {
            if (data == null) {
                return;
            }
        } else if (image == null) {
            return;
        }
        if (this.mPreviewW == 0 || this.mPreviewH == 0 || this.mResizedPreviewWidth == 0 || this.mResizedPreviewHeight == 0) {
            CamLog.m5e(CameraConstants.TAG, "AI-preview size is zero");
            return;
        }
        if (this.mIEllieVision == null) {
            this.mIEllieVision = SmartcamUtil.getSmartcamEllieVisionService();
            if (!SmartcamUtil.isSmartcamBindService()) {
                SmartcamUtil.smartcamBindService(getAppContext());
            }
        }
        if (!SmartcamUtil.isSmartcamBindService()) {
            this.mIEllieVision = null;
        }
        registerSmartCamCallback();
        if (this.mIsBinningState <= 0) {
            synchronized (this.mSync) {
                if (this.mIsRegisterCallback && this.mPreviewCallbackTime > 2 && this.mOutRGBArray != null && SmartcamUtil.isSmartcamBindService() && this.mGet.checkModuleValidate(192)) {
                    int startX = (this.mResizedPreviewWidth - this.mTargetWidth) / 2;
                    int endX = startX + this.mTargetWidth;
                    if (FunctionProperties.getSupportedHal() != 2) {
                        ColorConverter.yuvToRgbArrayWithResizeAndCrop(this.mOutRGBArray, data, this.mPreviewW, this.mPreviewH, this.mResizedPreviewWidth, this.mResizedPreviewHeight, startX, endX, this.mPreviewW, 0);
                    } else {
                        ColorConverter.nv21ToRgbArrayWithResizeAndCrop(this.mOutRGBArray, image.getPlanes()[0].getBuffer(), image.getPlanes()[2].getBuffer(), this.mPreviewW, this.mPreviewH, this.mResizedPreviewWidth, this.mResizedPreviewHeight, startX, endX, image.getPlanes()[0].getRowStride(), 0);
                    }
                    this.mPreviewCallbackTime = 0;
                    if (this.mRecognitionThread == null) {
                        this.mRecognitionThread = new SmartCamThread("smart_cam_recognition", this.mRecognitionRunnable, 1000);
                        this.mRecognitionThread.start();
                    }
                }
            }
            this.mPreviewCallbackTime++;
        } else if (this.mRecognitionThread != null) {
            this.mRecognitionThread.stopThread();
            this.mRecognitionThread = null;
        }
    }

    public void setSmartCamLayoutVisibility(int visibility) {
        if (this.mSmartCamLayout != null) {
            this.mSmartCamLayout.setVisibility(visibility);
        }
    }

    public boolean isSceneBtnSelected() {
        if (this.mSceneBtn == null) {
            return false;
        }
        return this.mSceneBtn.isSelected();
    }

    public boolean isSmartCamBarVisibility() {
        if (this.mContrastBar == null || this.mContrastBarLayout == null || this.mEVBar == null || this.mEVBarLayout == null) {
            return false;
        }
        if (this.mContrastBarLayout.getVisibility() == 0 || this.mEVBarLayout.getVisibility() == 0) {
            return true;
        }
        return false;
    }

    public boolean isSmartSamBarShown() {
        if (this.mContrastBar == null || this.mContrastBarLayout == null || this.mEVBar == null || this.mEVBarLayout == null) {
            return false;
        }
        if (this.mContrastBarLayout.isShown() || this.mEVBarLayout.isShown()) {
            return true;
        }
        return false;
    }

    public void hideSmartCamBar() {
        if (this.mContrastBar != null && this.mContrastBarLayout != null && this.mEVBar != null && this.mEVBarLayout != null && this.mSceneBtn != null && this.mSceneBtnImage != null && this.mSceneBtnText != null) {
            if (this.mSceneBtn.getVisibility() != 8) {
                this.mSceneBtn.setSelected(false);
                this.mSceneBtnImage.setSelected(false);
                this.mSceneBtnText.setSelected(false);
                this.mSceneBtnText.setEllipsize(TruncateAt.END);
            } else {
                this.mSceneBtn.setVisibility(8);
            }
            showSmartCamBar(this.mContrastBarLayout, this.mContrastBar, false);
            showSmartCamBar(this.mEVBarLayout, this.mEVBar, false);
            startTimerForSmartCamScene(0);
        }
    }

    public void hideSmartCamTagCloud() {
        if (this.mTagCloudLayout != null && this.mSmartCamTagCloudList != null) {
            for (int i = 0; i < this.mTagCloudLayout.getChildCount(); i++) {
                RotateLayout rotate = (RotateLayout) this.mTagCloudLayout.getChildAt(i);
                if (rotate != null) {
                    rotate.clearAnimation();
                }
            }
            this.mTagCloudLayout.removeAllViews();
            this.mSmartCamTagCloudList.clear();
        }
    }

    public void getResizedPreviewSize() {
        CamLog.m3d(CameraConstants.TAG, "AI-getResizedPreviewSize");
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null) {
            CameraParameters param = cameraDevice.getParameters();
            if (param != null) {
                Size previeSize = param.getPreviewSize();
                if (previeSize != null) {
                    this.mPreviewW = previeSize.getWidth();
                    this.mPreviewH = previeSize.getHeight();
                    float ratio = ((float) this.mPreviewW) / ((float) this.mPreviewH);
                    if (ratio > 1.99f) {
                        int resizeW = (int) (224.0f * ratio);
                        this.mResizedPreviewWidth = resizeW - (resizeW % 8);
                        this.mResizedPreviewHeight = 224;
                        int cropH = 224;
                        this.mTargetWidth = 392;
                        this.mTargetHeight = 224;
                    } else {
                        this.mResizedPreviewWidth = 224;
                        this.mResizedPreviewHeight = 224;
                        this.mTargetWidth = this.mResizedPreviewWidth;
                        this.mTargetHeight = this.mResizedPreviewHeight;
                    }
                    CamLog.m5e(CameraConstants.TAG, "SIK test mResizedPreviewWidth : " + this.mResizedPreviewWidth + ", mResizedPreviewHeight : " + this.mResizedPreviewHeight);
                    CamLog.m5e(CameraConstants.TAG, "SIK test mTargetWidth : " + this.mTargetWidth + ", mTargetHeight : " + this.mTargetHeight);
                    this.mOutRGBArray = new byte[((this.mTargetWidth * this.mTargetHeight) * 4)];
                }
            }
        }
    }

    public void setFilterButtonEnabled(boolean enabled) {
        if (this.mSceneBtn != null) {
            this.mSceneBtn.setEnabled(enabled);
        }
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        init();
    }

    public byte[] getOutRGBarray() {
        return this.mOutRGBArray;
    }

    public int[] getPreviewSize() {
        return new int[]{this.mResizedPreviewWidth, this.mResizedPreviewHeight};
    }

    public int getEvParamValue() {
        return this.mEVBar != null ? this.mEVBar.getCurValue() - this.mMaxEvParamValue : 0;
    }

    public int getTextAutoContrastParamValue() {
        return this.mContrastBar != null ? this.mContrastBar.getCurValue() : 0;
    }

    public int getSilContrastParamValue() {
        return this.mContrastBar != null ? this.mContrastBar.getCurValue() : 0;
    }

    public String convertCurCategoryFromDisplayName(String curDisplayName) {
        String curCategory = this.mCurrentCategory;
        if (curDisplayName == null) {
            return curCategory;
        }
        if (ISceneCategory.CATEGORY_DISPLAY_TEXT.equals(curDisplayName)) {
            return ISceneCategory.CATEGORY_ID_TEXT;
        }
        if (ISceneCategory.CATEGORY_DISPLAY_STAR.equals(curDisplayName)) {
            return ISceneCategory.CATEGORY_ID_STAR;
        }
        if (ISceneCategory.CATEGORY_DISPLAY_SILHOUETTE.equals(curDisplayName)) {
            return ISceneCategory.CATEGORY_ID_SILHOUETTE;
        }
        return curCategory;
    }

    public CameraParameters updateDeviceParams(CameraParameters parameters, boolean isRecording, String curDisplayName) {
        if (this.mEVBar == null || this.mEVBarLayout == null || this.mContrastBar == null || this.mContrastBarLayout == null || this.mSmartCamInterface == null || parameters == null) {
            CamLog.m3d(CameraConstants.TAG, "AI-updateDeviceParams, return.");
            return parameters;
        }
        String curCategory = convertCurCategoryFromDisplayName(curDisplayName);
        if (ISceneCategory.CATEGORY_ID_STAR.equals(curCategory)) {
            parameters = updateSpecificDeviceParams(parameters, curCategory, this.mEVBar.getCurValue() - this.mMaxEvParamValue, 0, 0);
        } else if (ISceneCategory.CATEGORY_ID_TEXT.equals(curCategory)) {
            parameters = updateSpecificDeviceParams(parameters, curCategory, 0, this.mContrastBar.getCurValue(), 0);
        } else if (ISceneCategory.CATEGORY_ID_SILHOUETTE.equals(curCategory)) {
            parameters = updateSpecificDeviceParams(parameters, curCategory, 0, 0, this.mContrastBar.getCurValue());
        }
        return parameters;
    }

    public CameraParameters updateSpecificDeviceParams(CameraParameters parameters, String curCategory, int evValue, int textValue, int contValue) {
        CamLog.m3d(CameraConstants.TAG, "AI-updateSpecificDeviceParams, parameters : " + parameters + ", curCategory : " + curCategory + ", evValue : " + evValue + ", textValue : " + textValue + ", contValue : " + contValue);
        if (parameters == null) {
            CamLog.m3d(CameraConstants.TAG, "AI-updateSpecificDeviceParams, return.");
        } else {
            if (curCategory == null) {
                curCategory = this.mCurrentCategory;
            }
            if (ISceneCategory.CATEGORY_ID_STAR.equals(curCategory)) {
                int curValue = parameters.getExposureCompensation();
                CamLog.m3d(CameraConstants.TAG, "AI-updateDeviceParams-STAR, bar value : " + evValue + ", getEVParam : " + curValue);
                if (!(parameters == null || curValue == evValue)) {
                    parameters.setExposureCompensation(evValue);
                }
            } else if (ISceneCategory.CATEGORY_ID_TEXT.equals(curCategory)) {
                CamLog.m3d(CameraConstants.TAG, "AI-updateDeviceParams-TEXT, bar value : " + textValue);
                parameters.set(ParamConstants.KEY_APP_AUTO_CONTRAST, 1);
                parameters.set(ParamConstants.KEY_APP_AUTO_CONTRAST_LEVEL, textValue);
            } else if (ISceneCategory.CATEGORY_ID_SILHOUETTE.equals(curCategory)) {
                CamLog.m3d(CameraConstants.TAG, "AI-updateDeviceParams-SILHOUETTE, bar value : " + contValue);
                if (FunctionProperties.getSupportedHal() != 2) {
                    CamLog.m11w(CameraConstants.TAG, "AI-This function should be supported on HAL3 only.");
                }
            }
        }
        return parameters;
    }

    public void setLatestSceneAndBar(String displayName, int evBar, int textBar, int contBar) {
        if (displayName == null) {
            return;
        }
        if (ISceneCategory.CATEGORY_DISPLAY_STAR.equals(displayName) || ISceneCategory.CATEGORY_DISPLAY_TEXT.equals(displayName) || ISceneCategory.CATEGORY_DISPLAY_SILHOUETTE.equals(displayName)) {
            this.mLatestDisplayName = displayName;
            this.mLatestEVBarParamValue = evBar;
            this.mLatestTextBarParamValue = textBar;
            this.mLatestSilContrastBarParamValue = contBar;
            CamLog.m7i(CameraConstants.TAG, "AI-setLatestSceneAndBar displayName : " + this.mLatestDisplayName + ", evBar : " + evBar + ", textBar : " + textBar + ", contBar : " + contBar);
        }
    }

    public void setLatestSceneAndFilter(String displayName, String filterKindName, String filterName) {
        if (displayName == null || filterKindName == null || filterName == null) {
            this.mLatestDisplayName = null;
            this.mLatestFilterName = null;
            this.mLatestFilterIndex = -1;
            return;
        }
        this.mLatestDisplayName = displayName;
        this.mLatestFilterName = filterName;
        CamLog.m7i(CameraConstants.TAG, "AI-setLatestSceneAndFilter, displayName : " + displayName + ", filterKindName : " + filterKindName + ", mLatestFilterName : " + filterName);
        String[] entryValues = (String[]) ((ListPreference) this.mGet.getListPreference(Setting.KEY_SMART_CAM_FILTER)).getEntryValues();
        int findIndex = -1;
        int i = 0;
        while (i < entryValues.length) {
            if (entryValues[i] != null && entryValues[i].contains(filterKindName)) {
                findIndex++;
                if (entryValues[i].equals(filterName)) {
                    break;
                }
            }
            i++;
        }
        this.mLatestFilterIndex = findIndex;
        CamLog.m7i(CameraConstants.TAG, "AI-setLatestSceneAndFilter, findIndex : " + findIndex);
    }

    public String getCurFilterName() {
        return this.mSetFilterName;
    }

    public void showInitGuide() {
        if (ModelProperties.getCarrierCode() == 6 && !SharedPreferenceUtil.getAICamInitGuideShown(getAppContext()) && !isAICamInitGuideShowing()) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
            if (vg != null) {
                this.mGet.inflateView(C0088R.layout.ai_cam_init_guide, vg);
                this.mAICamInitGuideLayout = vg.findViewById(C0088R.id.ai_cam_init_guide_rotate_layout);
                if (this.mAICamInitGuideLayout != null) {
                    setInitGuideLayout();
                    rotateInitGuide(getOrientationDegree());
                    this.mAICamInitGuideLayout.setVisibility(0);
                    this.mGet.onShowMenu(512);
                    CamLog.m3d(CameraConstants.TAG, "show AI CAM init guide");
                }
            }
        }
    }

    public boolean hideInitGuide(boolean showInitDialog) {
        if (!isAICamInitGuideShowing()) {
            return false;
        }
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
        if (vg == null) {
            return false;
        }
        vg.removeView(this.mAICamInitGuideLayout);
        this.mGet.onHideMenu(512);
        this.mAICamInitGuideLayout = null;
        if (showInitDialog) {
            this.mGet.showInitDialog();
        }
        return true;
    }

    private void setInitGuideLayout() {
        if (this.mAICamInitGuideLayout != null) {
            Utils.addTabToNumberedDescription((ViewGroup) this.mAICamInitGuideLayout.findViewById(C0088R.id.ai_cam_init_guide_text_layout), this.mGet.getAppContext().getString(C0088R.string.ai_cam_init_guide), this.mGet.getAppContext(), false, C0088R.style.type_ag02_dp_white);
            View view = this.mAICamInitGuideLayout.findViewById(C0088R.id.ai_cam_init_guide_ok_btn);
            if (view != null) {
                view.setOnClickListener(new C12631());
            }
        }
    }

    public void setRotateDegree(int degree, boolean animation) {
        rotateInitGuide(degree);
        super.setRotateDegree(degree, animation);
    }

    private void rotateInitGuide(int degree) {
        if (this.mAICamInitGuideLayout != null && this.mAICamInitGuideLayout.isShown()) {
            boolean isLand;
            int sizeCalculatedByPercentage;
            int paddingStartEnd;
            ((RotateLayout) this.mAICamInitGuideLayout).rotateLayout(degree);
            if (degree == 90 || degree == 270) {
                isLand = true;
            } else {
                isLand = false;
            }
            View targetLayout = this.mAICamInitGuideLayout.findViewById(C0088R.id.ai_cam_init_guide_title);
            LayoutParams targetLp = targetLayout.getLayoutParams();
            if (isLand) {
                sizeCalculatedByPercentage = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.144f);
            } else {
                sizeCalculatedByPercentage = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.094f);
            }
            targetLp.height = sizeCalculatedByPercentage;
            targetLayout.setLayoutParams(targetLp);
            targetLayout = this.mAICamInitGuideLayout.findViewById(C0088R.id.ai_cam_init_guide_top_layout);
            if (isLand) {
                paddingStartEnd = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.132f);
            } else {
                paddingStartEnd = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.083f);
            }
            if (degree == 180) {
                sizeCalculatedByPercentage = RatioCalcUtil.getNavigationBarHeight(this.mGet.getAppContext());
            } else {
                sizeCalculatedByPercentage = 0;
            }
            targetLayout.setPaddingRelative(paddingStartEnd, sizeCalculatedByPercentage, paddingStartEnd, 0);
            rotateInitGuideItemLayout(this.mAICamInitGuideLayout.findViewById(C0088R.id.ai_cam_init_guide_contents_layout), isLand);
            targetLayout = this.mAICamInitGuideLayout.findViewById(C0088R.id.ai_cam_init_guide_bottom);
            if (isLand) {
                paddingStartEnd = RatioCalcUtil.getNavigationBarHeight(this.mGet.getAppContext());
            } else {
                paddingStartEnd = 0;
            }
            if (degree == 0) {
                sizeCalculatedByPercentage = RatioCalcUtil.getNavigationBarHeight(this.mGet.getAppContext());
            } else {
                sizeCalculatedByPercentage = 0;
            }
            targetLayout.setPaddingRelative(0, 0, paddingStartEnd, sizeCalculatedByPercentage);
            targetLayout = this.mAICamInitGuideLayout.findViewById(C0088R.id.ai_cam_init_guide_checkBox_wrapper);
            targetLp = targetLayout.getLayoutParams();
            int checkBoxPaddingStart = Utils.getPx(getAppContext(), C0088R.dimen.init_guide_checkbox_inner_padding);
            if (isLand) {
                Drawable imageDrawable = getAppContext().getDrawable(C0088R.drawable.camera_setting_help_image_21_ai_cam);
                if (imageDrawable != null) {
                    checkBoxPaddingStart = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.167f) + imageDrawable.getIntrinsicWidth();
                }
            }
            targetLayout.setPaddingRelative(checkBoxPaddingStart, 0, 0, 0);
            targetLayout.setLayoutParams(targetLp);
        }
    }

    private void rotateInitGuideItemLayout(View layout, boolean isLand) {
        if (layout != null) {
            ImageView imageView = (ImageView) layout.findViewById(C0088R.id.ai_cam_init_guide_imageView);
            View imageLayout = layout.findViewById(C0088R.id.ai_cam_init_guide_imageView_wrapper);
            LinearLayout textLayout = (LinearLayout) layout.findViewById(C0088R.id.ai_cam_init_guide_text_layout);
            if (imageView != null && textLayout != null && imageLayout != null) {
                RelativeLayout.LayoutParams imageLp = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                LinearLayout.LayoutParams imageLayoutLp = (LinearLayout.LayoutParams) imageLayout.getLayoutParams();
                LinearLayout.LayoutParams textLp = (LinearLayout.LayoutParams) textLayout.getLayoutParams();
                if (imageLp != null && textLp != null && imageLayoutLp != null) {
                    Drawable d = imageView.getDrawable();
                    if (d != null) {
                        if (isLand) {
                            imageLp.width = -2;
                            ((LinearLayout) layout).setOrientation(0);
                            imageLayoutLp.gravity = 16;
                            imageLayout.setPaddingRelative(0, 0, RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.035f), 0);
                            d.setLevel(1);
                            textLayout.setPaddingRelative(0, 0, 0, 0);
                            textLp.gravity = 16;
                        } else {
                            imageLp.width = -1;
                            ((LinearLayout) layout).setOrientation(1);
                            imageLayoutLp.gravity = 1;
                            imageLayout.setPaddingRelative(0, 0, 0, 0);
                            d.setLevel(0);
                            textLayout.setPaddingRelative(0, RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.021f), 0, 0);
                            textLp.gravity = 0;
                        }
                        imageView.setLayoutParams(imageLp);
                        imageLayout.setLayoutParams(imageLayoutLp);
                        textLayout.setLayoutParams(textLp);
                    }
                }
            }
        }
    }

    public boolean isAICamInitGuideShowing() {
        return this.mAICamInitGuideLayout != null && this.mAICamInitGuideLayout.isShown();
    }
}
