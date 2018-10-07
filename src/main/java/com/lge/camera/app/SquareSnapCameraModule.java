package com.lge.camera.app;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore.Images.Media;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.database.OverlapProjectDbAdapter;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.file.FileManager;
import com.lge.camera.managers.GalleryManagerInterface;
import com.lge.camera.managers.SquareSnapGalleryItem;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.Utils;

public class SquareSnapCameraModule extends SquareCameraModuleBase implements GalleryManagerInterface {
    public ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri changeUri) {
            super.onChange(selfChange, changeUri);
            SquareSnapCameraModule.this.onContentsChanged(changeUri);
        }
    };
    private boolean mIsFirstShot = false;
    private boolean mIsGalleryViewTouched = false;
    public boolean mIsNeedToReloadList = false;
    protected boolean mIsRegisterContentObserver = false;
    public HandlerRunnable mReloadListRunable = new HandlerRunnable(this) {
        public void handleRun() {
            SquareSnapCameraModule.this.reloadList();
        }
    };
    private HandlerRunnable mSquareBurstShotItemAdded = new HandlerRunnable(this) {
        public void handleRun() {
            if (SquareSnapCameraModule.this.mGalleryManager != null) {
                int type = 30;
                int snapshotType = 2;
                if (DefaultCameraModule.sBurstShotCount <= 1) {
                    type = 0;
                    snapshotType = 0;
                }
                SquareSnapCameraModule.this.mGalleryManager.onNewItemAdded(SquareSnapCameraModule.this.mFirstBurstUri, null, snapshotType);
                SquareSnapCameraModule.this.mGet.onNewItemAdded(SquareSnapCameraModule.this.mFirstBurstUri, type, FileManager.getConsecutiveID(0, SquareSnapCameraModule.this.mBurstShotFileName));
                SquareSnapCameraModule.this.mFirstBurstUri = null;
                SquareSnapCameraModule.this.setContentObserver(true, Media.EXTERNAL_CONTENT_URI);
            }
        }
    };

    public SquareSnapCameraModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void init() {
        super.init();
        setContentObserver(true, Media.EXTERNAL_CONTENT_URI);
        this.mQuickClipManager.setForceStatusForSquareMode(true);
        if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(getShotMode())) {
            this.mGet.setReviewThumbBmp(null);
        }
    }

    protected void setManagersListener() {
        super.setManagersListener();
        this.mGalleryManager.setGalleryManagerInterface(this);
    }

    public void onDestroy() {
        super.onDestroy();
        setContentObserver(false, Media.EXTERNAL_CONTENT_URI);
    }

    public String getShotMode() {
        return CameraConstants.MODE_SQUARE_SNAPSHOT;
    }

    protected void changeToAutoView() {
        this.mGet.setCurrentConeMode(1, true);
        setSetting(Setting.KEY_MODE, "mode_normal", true);
        this.mGet.modeMenuClicked("mode_normal");
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(getShotMode()) && !AppControlUtil.isStartFromOnCreate()) {
            this.mGalleryManager.reloadList(true);
        }
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        this.mCaptureButtonManager.changeButtonByMode(1);
        if (this.mIsNeedToReloadList) {
            this.mIsNeedToReloadList = false;
            this.mGet.removePostRunnable(this.mReloadListRunable);
            this.mGet.runOnUiThread(this.mReloadListRunable);
        }
        this.mIsGalleryViewTouched = false;
    }

    protected void doShowGestureGuide() {
        super.doShowGestureGuide();
        showDoubleCamera(true);
    }

    protected Uri doSaveImage(byte[] data, byte[] extraExif, String dir, String filename) {
        Uri uri = super.doSaveImage(data, extraExif, dir, filename);
        if (this.mGalleryManager != null) {
            this.mGalleryManager.onNewItemAdded(uri, null, 0);
        }
        return uri;
    }

    public void notifyNewMedia(final Uri uri, boolean updateThumbnail) {
        String mediaType = getAppContext().getContentResolver().getType(uri);
        if (mediaType != null) {
            if (mediaType.startsWith(CameraConstants.MIME_TYPE_VIDEO)) {
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (SquareSnapCameraModule.this.mGalleryManager != null) {
                            SquareSnapCameraModule.this.mIsFirstShot = false;
                            SquareSnapCameraModule.this.mGalleryManager.onNewItemAdded(uri, null, 1);
                        }
                    }
                }, 0);
            } else if (mediaType.startsWith("image/gif")) {
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (SquareSnapCameraModule.this.mGalleryManager != null) {
                            SquareSnapCameraModule.this.mIsFirstShot = false;
                            SquareSnapCameraModule.this.mGalleryManager.readyCaptureAnimation(SquareSnapCameraModule.this.mGalleryManager.getThumbnailBitmap(uri, 3, true), true);
                            SquareSnapCameraModule.this.mGalleryManager.onNewItemAdded(uri, null, 3);
                        }
                    }
                }, 0);
            }
            super.notifyNewMedia(uri, updateThumbnail);
        }
    }

    protected boolean stopBurstShotTakingForLongshot() {
        if (this.mSnapShotChecker.checkMultiShotState(1)) {
            postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (SquareSnapCameraModule.this.mGalleryManager == null) {
                        return;
                    }
                    if (SquareSnapCameraModule.this.mFirstBurstUri == null) {
                        CamLog.m3d(CameraConstants.TAG, "[Cell] mFirstBurstUri is null");
                        return;
                    }
                    SquareSnapGalleryItem item = SquareSnapCameraModule.this.getCurSquareSnapItem();
                    if (item == null || item.mUri != SquareSnapCameraModule.this.mFirstBurstUri) {
                        CamLog.m3d(CameraConstants.TAG, "[Cell] Make burst new item");
                        String filePath = FileUtil.getRealPathFromURI(SquareSnapCameraModule.this.getAppContext(), SquareSnapCameraModule.this.mFirstBurstUri);
                        if (filePath == null) {
                            CamLog.m3d(CameraConstants.TAG, "filePath is null");
                            return;
                        }
                        SquareSnapCameraModule.this.setBtimapImageToAnimManager(Exif.readExif(filePath));
                        if (SquareSnapCameraModule.this.mGalleryManager != null) {
                            SquareSnapCameraModule.this.mGalleryManager.cancelOriginalImageMaking();
                        }
                        SquareSnapCameraModule.this.mGalleryManager.showTouchBlockCoverView(false);
                        SquareSnapCameraModule.this.postOnUiThread(SquareSnapCameraModule.this.mSquareBurstShotItemAdded, 250);
                    }
                }
            }, 0);
        }
        return super.stopBurstShotTakingForLongshot();
    }

    protected boolean stopBurstShotTakingForShot2Shot() {
        if (this.mSnapShotChecker.checkMultiShotState(4)) {
            postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (SquareSnapCameraModule.this.mGalleryManager == null) {
                        return;
                    }
                    if (SquareSnapCameraModule.this.mFirstBurstUri == null) {
                        CamLog.m3d(CameraConstants.TAG, "[Cell] mFirstBurstUri is null");
                        return;
                    }
                    SquareSnapGalleryItem item = SquareSnapCameraModule.this.getCurSquareSnapItem();
                    if (item == null || item.mUri != SquareSnapCameraModule.this.mFirstBurstUri) {
                        CamLog.m3d(CameraConstants.TAG, "[Cell] Make burst new item : " + SquareSnapCameraModule.this.mFirstBurstUri);
                        String filePath = FileUtil.getRealPathFromURI(SquareSnapCameraModule.this.getAppContext(), SquareSnapCameraModule.this.mFirstBurstUri);
                        if (filePath == null) {
                            CamLog.m3d(CameraConstants.TAG, "filePath is null");
                            return;
                        }
                        SquareSnapCameraModule.this.setBtimapImageToAnimManager(Exif.readExif(filePath));
                        if (SquareSnapCameraModule.this.mGalleryManager != null) {
                            SquareSnapCameraModule.this.mGalleryManager.cancelOriginalImageMaking();
                        }
                        SquareSnapCameraModule.this.mGalleryManager.showTouchBlockCoverView(false);
                        SquareSnapCameraModule.this.postOnUiThread(SquareSnapCameraModule.this.mSquareBurstShotItemAdded, 250);
                    }
                }
            }, 0);
        }
        return super.stopBurstShotTakingForShot2Shot();
    }

    protected void onPictureTakenCallback(byte[] data, byte[] extraExif, CameraProxy camera) {
        super.onPictureTakenCallback(data, extraExif, camera);
        ExifInterface exif = Exif.readExif(data);
        if (!this.mSnapShotChecker.checkMultiShotState(12)) {
            setBtimapImageToAnimManager(exif);
        }
    }

    protected void setBtimapImageToAnimManager(ExifInterface exif) {
        int degree = Exif.getOrientation(exif) + getOrientationDegree();
        if (getOrientationDegree() == 90 || getOrientationDegree() == 270) {
            degree = (degree + 180) % 360;
        }
        Bitmap bmp = BitmapManagingUtil.getRotatedImage(exif.getThumbnailBitmap(), degree, false);
        SquareSnapGalleryItem item = getCurSquareSnapItem();
        this.mIsFirstShot = false;
        if (item == null || item.mType == -1) {
            this.mIsFirstShot = true;
        }
        this.mGalleryManager.readyCaptureAnimation(bmp, false);
    }

    public SquareSnapGalleryItem getCurSquareSnapItem() {
        return this.mGalleryManager.getCurrentItem();
    }

    protected void runSwitchCamera() {
        CamLog.m3d(CameraConstants.TAG, "[Cell] runSwitchCamera");
        this.mSnapShotChecker.removeBurstState(96);
        if (this.mZoomManager != null) {
            this.mZoomManager.setZoomBarVisibility(8);
            this.mZoomManager.stopDrawingExceedsLevel();
        }
        initializeSettingMenus();
        if (isRearCamera()) {
            releaseEngine();
        }
        this.mGet.setSetting(Setting.KEY_MODE, getShotMode(), false);
        setHDRMetaDataCallback(null);
        setFlashMetaDataCallback(null);
        if (this.mIndicatorManager != null) {
            this.mIndicatorManager.hideAllIndicator();
        }
        stopPreview();
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.release();
        }
        closeCamera();
        this.mGet.setPreviewVisibility(0);
        this.mCameraStartUpThread = new CameraStartUpThread(0);
        this.mCameraStartUpThread.start();
        changeInitParameters();
        if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.onDestroy();
        }
        if (this.mDoubleCameraManager != null) {
            this.mDoubleCameraManager.onDestroy();
            this.mDoubleCameraManager.init();
            this.mDoubleCameraManager.setListenerAfterOneShotCallback();
        }
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.onPauseBefore();
            this.mAdvancedFilmManager.onPauseAfter();
            this.mAdvancedFilmManager.onDestroy();
            this.mAdvancedFilmManager.init();
            this.mAdvancedFilmManager.createFilmList();
            this.mAdvancedFilmManager.onResumeBefore();
            this.mAdvancedFilmManager.onResumeAfter();
        }
        if (this.mZoomManager != null) {
            this.mZoomManager.onPauseBefore();
            this.mZoomManager.onDestroy();
            this.mZoomManager.setZoomInterface(this);
            this.mZoomManager.onResumeBefore();
        }
        if (this.mExtraPrevewUIManager != null) {
            this.mExtraPrevewUIManager.onPauseBefore();
            this.mExtraPrevewUIManager.init();
            this.mExtraPrevewUIManager.onResumeBefore();
        }
        if (FunctionProperties.getSupportedHal() == 2) {
            ListPreference listPref = getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
            if (listPref != null) {
                String previewSizeStr = listPref.getExtraInfo(1);
                if (previewSizeStr != null) {
                    int[] previewSize = Utils.sizeStringToArray(previewSizeStr);
                    if (previewSize != null && previewSize.length == 2) {
                        this.mGet.setCameraPreviewSize(previewSize[0], previewSize[1]);
                    }
                }
            }
        }
    }

    public boolean onShowMenu(int menuType) {
        if (super.onShowMenu(menuType)) {
            return true;
        }
        deleteImmediatelyNotUndo();
        return false;
    }

    public boolean onHideMenu(int menuType) {
        boolean retValue = super.onHideMenu(menuType);
        this.mGalleryManager.showGalleryView(true);
        return retValue;
    }

    protected void afterStopRecording() {
        super.afterStopRecording();
        if (this.mGalleryManager != null) {
            this.mGalleryManager.showTouchBlockCoverView(false);
        }
    }

    public boolean isSquareGalleryBtn() {
        return true;
    }

    public void setContentObserver(boolean register, Uri uri) {
        CamLog.m3d(CameraConstants.TAG, "[Cell] setContentObserver");
        Context context = this.mGet.getAppContext();
        if (context != null) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver != null) {
                if (!register) {
                    CamLog.m3d(CameraConstants.TAG, "[Cell] setContentObserver : unregister");
                    resolver.unregisterContentObserver(this.mContentObserver);
                } else if (uri != null) {
                    CamLog.m3d(CameraConstants.TAG, "[Cell] setContentObserver : register");
                    resolver.unregisterContentObserver(this.mContentObserver);
                    resolver.registerContentObserver(uri, true, this.mContentObserver);
                } else {
                    return;
                }
                this.mIsRegisterContentObserver = register;
            }
        }
    }

    protected boolean checkUri(Uri uri) {
        return true;
    }

    public void onContentsChanged(Uri changeUri) {
        CamLog.m3d(CameraConstants.TAG, "[Cell] changeUri : " + changeUri.toString() + " paused " + this.mGet.isPaused());
        if (!this.mIsRegisterContentObserver || !checkUri(changeUri)) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] onContentsChanged - return : " + this.mIsRegisterContentObserver);
        } else if (this.mGet.isPaused()) {
            this.mIsNeedToReloadList = true;
        } else {
            this.mGet.removePostRunnable(this.mReloadListRunable);
            this.mGet.postOnUiThread(this.mReloadListRunable, 500);
        }
    }

    public void reloadList() {
        if (this.mGalleryManager.reloadList(false) == 0) {
            setQuickClipSharedUri(null);
            this.mReviewThumbnailManager.setThumbnailDefault(false, false);
            this.mReviewThumbnailManager.setEnabled(false);
        }
    }

    public boolean onCameraShutterButtonClicked() {
        if (this.mGalleryManager.isDeleteProcessing()) {
            return false;
        }
        boolean retValue = super.onCameraShutterButtonClicked();
        if (!retValue) {
            return retValue;
        }
        deleteImmediatelyNotUndo();
        return retValue;
    }

    protected void doTakePicture() {
        this.mGalleryManager.cancelOriginalImageMaking();
        if (this.mGalleryManager.getSquareSnapshotState() != 0) {
            this.mGalleryManager.stopVideoPlay();
        }
        super.doTakePicture();
    }

    public boolean onShutterBottomButtonLongClickListener() {
        if (!super.onShutterBottomButtonLongClickListener()) {
            return false;
        }
        if (isShutterButtonStateAvailableForBurstShot() && this.mSnapShotChecker.checkMultiShotState(1)) {
            this.mGalleryManager.showTouchBlockCoverView(true);
        }
        if (this.mGalleryManager.getSquareSnapshotState() != 0) {
            this.mGalleryManager.stopVideoPlay();
        }
        if (this.mAdvancedFilmManager == null || !this.mAdvancedFilmManager.isRunningFilmEmulator() || CameraConstants.FILM_NONE.equals(getSettingValue(Setting.KEY_FILM_EMULATOR))) {
            setContentObserver(false, Media.EXTERNAL_CONTENT_URI);
        }
        return true;
    }

    protected void restoreRecorderToIdle() {
        super.restoreRecorderToIdle();
        this.mGalleryManager.showTouchBlockCoverView(false);
    }

    protected void oneShotPreviewCallbackDone() {
        super.oneShotPreviewCallbackDone();
        if (getCurSquareSnapItem() == null && CameraConstants.MODE_SQUARE_SNAPSHOT.equals(getShotMode())) {
            setQuickClipSharedUri(null);
        }
    }

    public void onGalleryBitmapLoaded(boolean useAnimation) {
        setThumbnailButtonImage(false, true);
        if (useAnimation && isSquareGalleryBtn()) {
            this.mGalleryManager.doCaptureEffect(this.mIsFirstShot);
        }
    }

    protected void setThumbnailButtonImage(boolean isEmptyImg, boolean notUseSecureImg) {
        View burstCountView = this.mGet.findViewById(C0088R.id.thumbnail_burst_count);
        boolean isBurstCountVisible = false;
        if (burstCountView != null && burstCountView.getVisibility() == 0) {
            isBurstCountVisible = true;
        }
        if (!this.mSnapShotChecker.checkMultiShotState(1) && !isBurstCountVisible) {
            this.mReviewThumbnailManager.setThumbnailDefault(isEmptyImg, notUseSecureImg);
        }
    }

    public void onProjectMove() {
    }

    public void onGalleryPageChanged(Uri uri, boolean byCaptured, boolean byDeleting) {
        CamLog.m3d(CameraConstants.TAG, "[Cell] onGalleryPageChanged : " + uri);
        if (!(this.mGifManager == null || !this.mGifManager.getGifVisibleStatus() || byCaptured)) {
            this.mGifManager.setGifVisibleStatus(false);
            this.mGifManager.setGifVisibility(false);
        }
        if (uri == null) {
            setQuickClipSharedUri(null);
            this.mReviewThumbnailManager.setThumbnailDefault(false, false);
            this.mReviewThumbnailManager.setEnabled(false);
            return;
        }
        if (uri.toString().contains(OverlapProjectDbAdapter.URI_OVERLAP)) {
            this.mReviewThumbnailManager.setEnabled(false);
            this.mQuickClipManager.setQuickClipIcon(false);
        } else if (checkModuleValidate(192) && !byCaptured) {
            setQuickClipSharedUri(uri);
            setThumbnailButtonImage(false, true);
            this.mReviewThumbnailManager.setEnabled(true);
            if (!byDeleting) {
                this.mQuickClipManager.setQuickClipIcon(true);
            }
        } else {
            return;
        }
        this.mQuickClipManager.setForceStatusForSquareMode(false);
        if (!byDeleting) {
            deleteImmediatelyNotUndo();
        }
    }

    public void onDeleteButtonClicked() {
        if (this.mQuickClipManager != null) {
            this.mQuickClipManager.drawerClose(false);
        }
        pauseShutterless();
    }

    public void onGalleryImageViewClicked() {
        if (this.mQuickClipManager != null) {
            this.mQuickClipManager.setMiniView();
        }
    }

    public boolean isDeleteAvailable() {
        if (this.mGifManager != null && this.mGifManager.isGIFEncoding()) {
            CamLog.m7i(CameraConstants.TAG, "[Cell] gif encoding");
            return false;
        } else if (this.mGalleryManager != null && this.mGalleryManager.isAnimationViewShowing()) {
            CamLog.m7i(CameraConstants.TAG, "[Cell] animation view showing");
            return false;
        } else if (this.mIsRegisterContentObserver) {
            return true;
        } else {
            CamLog.m7i(CameraConstants.TAG, "[Cell] mIsRegisterContentObserver is false");
            return false;
        }
    }

    public void startGallery(Uri uri, int galleryPlayType) {
        super.launchGallery(uri, galleryPlayType);
    }

    public void setStartGalleryLocation(float[] loc) {
        super.setLaunchGalleryLocation(loc);
    }

    public void onGalleryViewTouched(boolean touched) {
        if (this.mHandler != null && !isRearCamera()) {
            if (touched) {
                if (!this.mIsGalleryViewTouched) {
                    if (this.mShutterlessSelfieManager != null) {
                        this.mHandler.removeMessages(93);
                        this.mShutterlessSelfieManager.cancelTimerTask();
                        pauseShutterless();
                    }
                } else {
                    return;
                }
            } else if (this.mIsGalleryViewTouched) {
                this.mHandler.removeMessages(94);
                this.mHandler.sendEmptyMessageDelayed(93, CameraConstants.TOAST_LENGTH_SHORT);
            } else {
                return;
            }
            this.mIsGalleryViewTouched = touched;
        }
    }

    protected void updateRecordingUi() {
        super.updateRecordingUi();
        if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(getShotMode())) {
            this.mGalleryManager.showGalleryView(true);
        }
    }

    public boolean onRecordStartButtonClicked() {
        if (this.mGalleryManager.isDeleteProcessing()) {
            return false;
        }
        boolean retValue = super.onRecordStartButtonClicked();
        if (!retValue) {
            return retValue;
        }
        deleteImmediatelyNotUndo();
        return retValue;
    }

    public void onVideoShutterClicked() {
        this.mGalleryManager.showTouchBlockCoverView(true);
        this.mGalleryManager.removeAnimationViews();
        if (this.mGalleryManager.getSquareSnapshotState() == 1 || this.mGalleryManager.getSquareSnapshotState() == 3) {
            this.mGalleryManager.stopVideoPlay();
        }
        super.onVideoShutterClicked();
    }

    public boolean isSquareSnapAccessView() {
        return this.mGalleryManager.isMainAccessView();
    }

    protected boolean isSettingOpen() {
        if (this.mGalleryManager.getSquareSnapshotState() == 1 || this.mIsGalleryViewTouched) {
            return true;
        }
        return super.isSettingOpen();
    }
}
