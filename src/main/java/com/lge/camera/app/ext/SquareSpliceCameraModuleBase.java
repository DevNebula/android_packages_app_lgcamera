package com.lge.camera.app.ext;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.components.MultiViewLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.managers.ext.SplicePostViewListener;
import com.lge.camera.managers.ext.SpliceViewListener;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.Utils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class SquareSpliceCameraModuleBase extends MultiViewCameraModuleExpand implements SpliceViewListener, SplicePostViewListener, SpliceManagersInterface {
    protected int mBottomPreviewTargetCamId = 1;
    protected boolean mIsImageLoadCanceled = false;
    protected boolean mIsSaveBtnClicked = false;
    protected boolean mIsSettingImgPrePostView = false;
    protected boolean mPrePostViewImage = false;
    HandlerRunnable mRemovePostview = new HandlerRunnable(this) {
        public void handleRun() {
            CamLog.m3d(CameraConstants.TAG, "cancel button clicked");
            if (SquareSpliceCameraModuleBase.this.mSplicePostViewManager != null) {
                SquareSpliceCameraModuleBase.this.mSplicePostViewManager.removeViews();
            }
        }
    };
    HandlerRunnable mSetBitmapPrePostView = new HandlerRunnable(this) {
        public void handleRun() {
            CamLog.m3d(CameraConstants.TAG, "setBitmapToPrePostView");
            if (SquareSpliceCameraModuleBase.this.mIsImportedImage && !SquareSpliceCameraModuleBase.this.mIsImageLoadCanceled && SquareSpliceCameraModuleBase.this.mSpliceViewImageImportManager.getGalleryButtonClicked()) {
                int setForRotaionAngle = 0;
                switch (SquareSpliceCameraModuleBase.this.mSpliceViewImageImportManager.getRotationBeforeStartingGallery()) {
                    case 90:
                        setForRotaionAngle = 270;
                        break;
                    case 270:
                        setForRotaionAngle = 90;
                        break;
                }
                SquareSpliceCameraModuleBase.this.mSpliceViewImageImportManager.resetPrePostViewDegree((float) setForRotaionAngle);
            }
            if (MultiViewFrame.sPreviewBitmap[0] != null) {
                SquareSpliceCameraModuleBase.this.mSpliceViewImageImportManager.setPrePostBitmap(SquareSpliceCameraModuleBase.this.createCopiedBitmap(MultiViewFrame.sPreviewBitmap[0]));
                SquareSpliceCameraModuleBase.this.mSpliceViewImageImportManager.setImageToPrePostView(SquareSpliceCameraModuleBase.this.mPrePostViewImage);
            }
            SquareSpliceCameraModuleBase.this.mIsSettingImgPrePostView = false;
            SquareSpliceCameraModuleBase.this.mIsImageLoadCanceled = false;
            SquareSpliceCameraModuleBase.this.mSpliceViewImageImportManager.resetGalleryButtonClicked();
        }
    };
    protected int mTopPreviewTargetCamId = 0;
    protected boolean mUseAnim = false;
    protected String mfilePath = null;

    public SquareSpliceCameraModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void changeRequester() {
        super.changeRequester();
        this.mParamUpdater.setParamValue(ParamConstants.KEY_APP_OUTPUTS_TYPE, ParamConstants.OUTPUTS_PREVIEW_RECORD_STR);
    }

    public boolean isFrameShot() {
        return isMultiviewFrameShot();
    }

    public int getFrameshotCount() {
        return this.mFrameShotProgressCount;
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        this.mSplicePostViewManager.onPauseAfter();
    }

    protected int getLayoutIndex() {
        return 1;
    }

    protected void resultFromCropImage(Intent data) {
        int countOfRotate = 0;
        switch (this.mSpliceViewImageImportManager.getRotationBeforeStartingGallery()) {
            case 90:
                countOfRotate = 1;
                break;
            case 270:
                countOfRotate = 3;
                break;
        }
        this.mMultiviewFrame.setRotateState(countOfRotate);
        if (this.mImportedUri != null) {
            this.mfilePath = this.mImportedUri.toSafeString();
        } else {
            CamLog.m5e(CameraConstants.TAG, "mImportedUri is null");
        }
        this.mIsImageLoadCanceled = false;
        this.mIsImportedImage = true;
    }

    protected void resultFromPickImage(Intent data) {
        this.mImportedUri = data.getData();
        startCameraCrop();
    }

    private void startCameraCrop() {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(this.mImportedUri, "image/*");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("scale", true);
        intent.putExtra("lge-crop-max-cue", true);
        this.mImportedUri = getTempFile();
        intent.putExtra("output", this.mImportedUri);
        intent.addFlags(1);
        this.mGet.getActivity().startActivityForResult(intent, 7);
    }

    private Uri getTempFile() {
        String filePath = "multiview_0.jpg";
        File f = new File(this.mGet.getAppContext().getExternalFilesDir(".Camera").getAbsolutePath(), "multiview_0.jpg");
        try {
            f.createNewFile();
            return Uri.fromFile(f);
        } catch (IOException e) {
            return this.mImportedUri;
        }
    }

    public Bitmap getBitmapForImport() {
        try {
            this.mImportedBitmap = BitmapManagingUtil.loadScaledBitmap(this.mGet.getAppContext().getContentResolver(), this.mImportedUri.toString(), CameraConstantsEx.QHD_SCREEN_RESOLUTION, CameraConstantsEx.QHD_SCREEN_RESOLUTION);
        } catch (Exception e) {
            CamLog.m6e(CameraConstants.TAG, "Bitmap Exception ", e);
        }
        return this.mImportedBitmap;
    }

    public void onSwap() {
        if (this.mMultiviewFrame != null) {
            this.mMultiviewFrame.swapView(this.mGet.getOrientationDegree());
        }
        int temp = this.mTopPreviewTargetCamId;
        this.mTopPreviewTargetCamId = this.mBottomPreviewTargetCamId;
        this.mBottomPreviewTargetCamId = temp;
        if (this.mSpliceViewImageImportManager.getReverseState()) {
            this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(0, true);
            this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(1, false);
        } else {
            this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(0, false);
            this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(1, true);
        }
        this.mSpliceViewImageImportManager.updateDualCameraIcon();
    }

    public void onRotate() {
        if (this.mMultiviewFrame != null && this.mSpliceViewImageImportManager != null) {
            this.mMultiviewFrame.setRotateStateIncremental();
            this.mMultiviewFrame.rotateView(this.mGet.getOrientationDegree());
            this.mSpliceViewImageImportManager.showSwapAndRotateButton(true);
        }
    }

    public int getCurrentDegreeForImportImg() {
        if (this.mMultiviewFrame != null) {
            return ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).getRotateState();
        }
        return 0;
    }

    public void savePostViewContens(int type) {
        switch (type) {
            case 1:
            case 2:
            case 3:
                setMVState(4);
                startMakingCollageVideo(this.mInputInfo);
                return;
            default:
                return;
        }
    }

    public Bitmap[] getTransformedImage() {
        return this.mSplicePostViewManager.getTransformedImage();
    }

    public void onCancel() {
        CamLog.m3d(CameraConstants.TAG, "in");
        if (this.mPostViewBitmap != null) {
            int i = 0;
            while (i < this.mPostViewBitmap.length) {
                if (!(this.mPostViewBitmap[i] == null || this.mPostViewBitmap[i].isRecycled())) {
                    this.mPostViewBitmap[i].recycle();
                    this.mPostViewBitmap[i] = null;
                }
                i++;
            }
        }
        ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).releaseGuidePhotoView();
        resetImportedImage();
        cancelFrameShot();
        MultiViewFrame.resetCapturedTexture();
        this.mMultiviewFrame.restoreVertex(this.mGet.getOrientationDegree());
        this.mSplicePostViewManager.resetSaveButtonClickedFlag();
        this.mSpliceViewImageImportManager.initLayout();
        this.mSpliceViewImageImportManager.showImportLayout(true);
        this.mIsSaveBtnClicked = false;
        if (this.mQuickClipManager != null) {
            this.mQuickClipManager.setSharedUri(getUri());
        }
        this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(0, true);
        this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(1, false);
        AudioUtil.setAudioFocus(this.mGet.getActivity(), false);
        this.mQuickButtonManager.setEnable(C0088R.id.quick_button_flash, false);
        this.mQuickButtonManager.setEnable(C0088R.id.quick_button_film_emulator, false);
        this.mSpliceViewImageImportManager.setPrePostBitmap(null);
        setButtonsVisibilityForPostView(true);
        CamLog.m3d(CameraConstants.TAG, "out");
    }

    public void onSaveContents(boolean isSaveBtnClicked) {
        if (getCurrentPostViewType() != 0) {
            setUIOnCollageSaving();
        }
        this.mIsSaveBtnClicked = isSaveBtnClicked;
    }

    public void onSaveContents(Bitmap bm, boolean isSaveBtnClicked) {
        this.mIsSaveBtnClicked = isSaveBtnClicked;
        Bitmap previewBitmap = BitmapManagingUtil.getRotatedImage(bm, Utils.isConfigureLandscape(this.mGet.getAppContext().getResources()) ? 0 : 270, false);
        if (isSignatureEnableCondition()) {
            previewBitmap = this.mGet.composeSignatureImage(previewBitmap, this.mCameraDegree);
        }
        if (previewBitmap != null) {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            previewBitmap.compress(CompressFormat.JPEG, 100, byteArray);
            saveImage(byteArray.toByteArray(), null);
            onSaveContents(isSaveBtnClicked);
            try {
                byteArray.close();
            } catch (Exception e) {
                CamLog.m3d(CameraConstants.TAG, "byteArray close failed");
            }
            previewBitmap.recycle();
            bm.recycle();
        }
        removePostView();
        this.mToastManager.showShortToast(this.mGet.getActivity().getString(C0088R.string.sp_saved_NORMAL));
        onCancel();
    }

    public void removePostView() {
        this.mGet.postOnUiThread(this.mRemovePostview, 50);
    }

    public void onImageTransformed(int type) {
        if (getCurrentPostViewType() != 0) {
            if (type != 3) {
                MultiViewFrame.setTransformedImage(type);
            } else {
                savePostViewContens(type);
            }
        }
    }

    protected void launchQuickClipSharedApp() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                SquareSpliceCameraModuleBase.this.mQuickClipManager.doClickQuickClip();
                SquareSpliceCameraModuleBase.this.mIsSaveBtnClicked = false;
                SquareSpliceCameraModuleBase.this.mSplicePostViewManager.resetSaveButtonClickedFlag();
            }
        });
    }

    public void setCollageContentsSharedFlag() {
        this.mIsCollageShared = true;
        this.mSplicePostViewManager.saveContents(false);
    }

    public boolean checkCollageContentsShareAvailable() {
        return this.mFrameShotProgressCount == this.mFrameShotMaxCount && !this.mIsSaveBtnClicked;
    }

    public void onRotateDegree(int degree) {
        this.mSplicePostViewManager.setDegree(degree, true);
    }

    public boolean isPostviewVisible() {
        return this.mSplicePostViewManager.isPostviewVisible();
    }

    protected void changeToAutoView() {
        this.mGet.setCurrentConeMode(1, true);
        setSetting(Setting.KEY_MODE, "mode_normal", true);
        this.mGet.modeMenuClicked("mode_normal");
    }

    public boolean onShowMenu(int menuType) {
        if (!super.onShowMenu(menuType)) {
            return false;
        }
        if (this.mSpliceViewImageImportManager != null) {
            this.mSpliceViewImageImportManager.showImportLayout(false);
        }
        return true;
    }

    public boolean onHideMenu(int menuType) {
        if (!super.onHideMenu(menuType)) {
            return false;
        }
        if (isMenuShowing((menuType ^ -1) & CameraConstants.MENU_TYPE_ALL) || this.mSpliceViewImageImportManager == null) {
            return true;
        }
        this.mSpliceViewImageImportManager.showImportLayout(true);
        return true;
    }

    public boolean doBackKey() {
        if (isProgressDialogVisible() || this.mIsSettingImgPrePostView) {
            return true;
        }
        if (this.mSplicePostViewManager == null || !this.mSplicePostViewManager.isPostviewVisible()) {
            return super.doBackKey();
        }
        this.mSplicePostViewManager.releaseMediaPlayer();
        removePostView();
        onCancel();
        return true;
    }

    public boolean getPostviewVisibility() {
        return this.mSplicePostViewManager.isPostviewVisible();
    }

    public void setCmdButtonsVisibility(boolean visiblity) {
        setButtonsVisibilityForPostView(visiblity);
    }

    public int getCurrentPostViewType() {
        return this.mSplicePostViewManager.getPostviewType();
    }

    protected void cancelImportedImage() {
        onCancel();
    }

    protected String getSpliceSurfaceRecordingVideoSize() {
        return isMultiviewFrameShot() ? null : "2880x1440";
    }

    public void setBitmapToPrePostView(boolean isImage) {
        if (MultiViewFrame.sPreviewBitmap[0] == null) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    SquareSpliceCameraModuleBase.this.onCancel();
                }
            });
            return;
        }
        this.mIsSettingImgPrePostView = true;
        this.mPrePostViewImage = isImage;
        this.mGet.removePostRunnable(this.mSetBitmapPrePostView);
        this.mGet.postOnUiThread(this.mSetBitmapPrePostView, 150);
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        if (this.mSplicePostViewManager == null || !isPostviewVisible()) {
            super.doCleanView(doByAction, useAnimation, saveState);
            return;
        }
        super.doCleanView(doByAction, false, saveState);
        setButtonsVisibilityForPostView(false);
    }

    protected void doShowCmdButtons(boolean useAnimation) {
        this.mUseAnim = useAnimation;
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                SquareSpliceCameraModuleBase.this.showCaptureButtonsProc(SquareSpliceCameraModuleBase.this.mUseAnim);
            }
        });
    }

    protected void showCaptureButtonsProc(boolean useAnim) {
        if (this.mSplicePostViewManager == null || !(this.mFrameShotProgressCount == 2 || isPostviewVisible())) {
            super.doShowCmdButtons(this.mUseAnim);
            return;
        }
        this.mMultiViewManager.clearCapturedImageView();
        setButtonsVisibilityForPostView(false);
    }

    public int getFrameshotCountForPostView() {
        return getFrameshotCount();
    }

    public int[] getCameraArray() {
        return ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).getCurCameraIdArray();
    }

    protected boolean isFingerDetectionSupportedMode() {
        return false;
    }

    protected boolean isPauseWaitDuringShot() {
        return false;
    }

    protected void resultFromCancel() {
        CamLog.m3d(CameraConstants.TAG, "resultFromCancel");
        this.mIsImageLoadCanceled = true;
    }
}
