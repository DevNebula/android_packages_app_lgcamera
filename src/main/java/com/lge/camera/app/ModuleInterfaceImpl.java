package com.lge.camera.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.lge.app.MiniActivity;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.MmsProperties;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamRequester;
import com.lge.camera.file.FileManager;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.StorageUtil;

public abstract class ModuleInterfaceImpl extends Module {
    public ModuleInterfaceImpl(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public Activity getActivity() {
        return this.mGet.getActivity();
    }

    public MiniActivity getMiniActivity() {
        return this.mGet.getMiniActivity();
    }

    public Context getAppContext() {
        return this.mGet.getAppContext();
    }

    public int getCameraState() {
        return this.mCameraState;
    }

    public View inflateStub(int id) {
        return this.mGet.inflateStub(id);
    }

    public View inflateView(int resource) {
        return this.mGet.layoutInflate(resource, null);
    }

    public View inflateView(int resource, ViewGroup vg) {
        return this.mGet.layoutInflate(resource, vg);
    }

    public View findViewById(int resource) {
        return this.mGet.findViewById(resource);
    }

    public void setupOptionMenu(int postView) {
        this.mGet.setupOptionMenu(postView);
    }

    public void setSystemUiVisibilityListener(boolean register) {
        this.mGet.setSystemUiVisibilityListener(register);
    }

    public boolean isAttachIntent() {
        return this.mGet.isAttachIntent();
    }

    public boolean isVideoCameraIntent() {
        return this.mGet.isVideoCameraIntent();
    }

    public boolean isMMSIntent() {
        return this.mGet.isMMSIntent();
    }

    public boolean isLGUOEMCameraIntent() {
        return this.mGet.isLGUOEMCameraIntent();
    }

    public boolean isVideoCaptureMode() {
        return this.mGet.isVideoCaptureMode();
    }

    public boolean isAttachResol() {
        ListPreference listPref = getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
        if (listPref == null) {
            return false;
        }
        String videoSize = listPref.getValue();
        if (MmsProperties.getMmsResolutionsLength(this.mGet.getAppContext().getContentResolver()) == 0) {
            return false;
        }
        return MmsProperties.isAvailableMmsResolution(this.mGet.getAppContext().getContentResolver(), videoSize);
    }

    public boolean isPaused() {
        return this.mGet.isPaused();
    }

    public int getOrientationDegree() {
        return this.mGet.getOrientationDegree();
    }

    public void setOrientationLock(boolean lock) {
        this.mGet.setOrientationLock(lock);
    }

    public boolean isConfigChanging() {
        return this.mConfigChanging;
    }

    public void runOnUiThread(Object action) {
        this.mGet.runOnUiThread(action);
    }

    public void postOnUiThread(Object action, long delay) {
        this.mGet.postOnUiThread(action, delay);
    }

    public void removePostRunnable(Object object) {
        this.mGet.removePostRunnable(object);
    }

    public void showToast(String message, long hideDelayMillis) {
        if (this.mToastManager != null) {
            this.mToastManager.showToast(message, hideDelayMillis);
        }
    }

    public void showToastConstant(String message) {
        if (this.mToastManager != null) {
            this.mToastManager.showShortToast(message, false);
        }
    }

    public void setSetting(String key, String value, boolean save) {
        this.mGet.setSetting(key, value, save);
    }

    public String getSettingValue(String key) {
        return this.mGet.getCurSettingValue(key);
    }

    public boolean isModeMenuVisible() {
        return this.mGet.isModeMenuVisible();
    }

    public boolean isHelpListVisible() {
        return this.mGet.isHelpListVisible();
    }

    public boolean isSettingMenuVisible() {
        return this.mGet.isSettingMenuVisible();
    }

    public boolean isOpeningSettingMenu() {
        return this.mGet.isOpeningSettingMenu();
    }

    public void removeSettingMenu(boolean direct, boolean onlyHideMenu) {
        this.mGet.removeSettingMenu(direct, onlyHideMenu);
    }

    public int getCurStorage() {
        return StorageUtil.convertStorageNameToType(this.mGet.getCurSettingValue(Setting.KEY_STORAGE));
    }

    public String getCurDir() {
        if (this.mStorageManager == null) {
            return "";
        }
        return this.mStorageManager.getDir(getCurStorage());
    }

    public String getTempDir() {
        if (this.mStorageManager == null) {
            return "";
        }
        return this.mStorageManager.getTempDir(getCurStorage());
    }

    public String getStorageDir(int storageType) {
        if (this.mStorageManager != null) {
            return this.mStorageManager.getDir(storageType);
        }
        return "";
    }

    public boolean checkStorage(int checkFor, int storageType) {
        if (this.mStorageManager != null) {
            return this.mStorageManager.checkStorage(checkFor, storageType);
        }
        return true;
    }

    public boolean checkStorage(int checkFor, int storageType, boolean isNeedCheckCacheSize) {
        boolean isAvailable = checkStorage(checkFor, storageType);
        if (storageType == 2 && this.mStorageManager != null && isAvailable && isNeedCheckCacheSize) {
            return this.mStorageManager.checkCNasCacheStorage(checkFor, true);
        }
        return isAvailable;
    }

    public boolean easyCheckStorage(int checkFor, int storageType, boolean isNeedCheckCacheSize) {
        if (this.mStorageManager == null) {
            return false;
        }
        if (this.mGet.getMediaSaveService() == null) {
            return checkStorage(checkFor, storageType, isNeedCheckCacheSize);
        }
        boolean isAvailable = this.mStorageManager.easyCheckStorage(checkFor, storageType, this.mGet.getMediaSaveService().getQueueCount());
        if (storageType == 2 && isAvailable && isNeedCheckCacheSize) {
            return this.mStorageManager.checkCNasCacheStorage(checkFor, true);
        }
        return isAvailable;
    }

    public boolean checkStorage() {
        return checkStorage(false);
    }

    public boolean checkStorage(boolean isNeedCheckCacheSize) {
        if (this.mStorageManager == null) {
            return true;
        }
        return checkStorage(0, this.mStorageManager.getCurrentStorage(), isNeedCheckCacheSize);
    }

    public boolean checkStorage(int checkFor, int storageType, int durationMillis, int bitrate, boolean isNeedCheckCacheSize) {
        if (this.mStorageManager == null) {
            return true;
        }
        boolean isAvailable = this.mStorageManager.checkStorage(checkFor, storageType, durationMillis, bitrate);
        if (storageType == 2 && isAvailable && isNeedCheckCacheSize) {
            return this.mStorageManager.checkCNasCacheStorage(checkFor, true);
        }
        return isAvailable;
    }

    public void notifyNewMediaFromPostview(Uri uri) {
        FileManager.broadcastNewMedia(this.mGet.getAppContext(), uri);
        this.mReviewThumbnailManager.setLaunchGalleryAvailable(true);
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.doAfterCaptureProcess(uri, false);
        }
    }

    public int getSettingIndex(String key) {
        return this.mGet.getSettingIndex(key);
    }

    public void updateUi(ParamRequester requester) {
        if (requester != null && ParamConstants.KEY_PREVIEW_SIZE.equals(requester.getKey())) {
            setPreviewLayoutParam();
        }
    }

    public void setSettingMenuEnable(String key, boolean enable) {
        this.mGet.setSettingMenuEnable(key, enable);
    }

    public boolean getSettingMenuEnable(String key) {
        return this.mGet.getSettingMenuEnable(key);
    }

    public void onGestureFlicking(MotionEvent e1, MotionEvent e2, int gestureType) {
    }

    public void onSizeChanged(int width, int height) {
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    public ListPreference getListPreference(String key) {
        return this.mGet.getListPreference(key);
    }

    public Object getListPreference(String key, boolean isRear) {
        return this.mGet.getListPreference(key, isRear);
    }

    public Location getCurrentLocation() {
        return this.mLocationServiceManager == null ? null : this.mLocationServiceManager.getCurrentLocation();
    }

    public boolean attatchMediaOnPostview(Uri uri, int mediaType) {
        return false;
    }

    public boolean isOnGpsSetting() {
        return "on".equals(getSettingValue(Setting.KEY_TAG_LOCATION));
    }

    public void showDialog(int id) {
        if (this.mDialogManager != null) {
            if (this.mDialogManager.isProgressDialogVisible()) {
                if (id == 133) {
                    this.mDialogManager.onDismissRotateDialog(true);
                } else {
                    this.mDialogManager.onDismissRotateDialog();
                }
            }
            int previousId = this.mDialogManager.getDialogID();
            if (id != previousId && previousId == 144) {
                this.mDialogManager.onDismissRotateDialog();
            }
            this.mDialogManager.showDialogPopup(id);
            if (id == 8) {
                this.mGet.setSetting(Setting.KEY_STORAGE, CameraConstants.STORAGE_NAME_INTERNAL, true);
            }
        }
    }

    public void showDialog(int id, boolean showAnim) {
        if (this.mDialogManager != null) {
            if (this.mDialogManager.isProgressDialogVisible()) {
                this.mDialogManager.onDismissRotateDialog();
            }
            this.mDialogManager.showDialogPopup(id, showAnim);
            if (id == 8) {
                this.mGet.setSetting(Setting.KEY_STORAGE, CameraConstants.STORAGE_NAME_INTERNAL, true);
            }
        }
    }

    public void showDialog(int id, String setting, boolean isCheckBoxNeeded) {
        this.mDialogManager.showDialogPopup(id, setting, isCheckBoxNeeded);
    }

    private void showProgressDialog(final boolean show, int delay, final int type) {
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                CamLog.m3d(CameraConstants.TAG, "showProgressDialog. show : " + show + " type = " + type);
                if (ModuleInterfaceImpl.this.mDialogManager != null) {
                    if (!show) {
                        if (ModuleInterfaceImpl.this.mDialogManager.isRotateDialogVisible(type)) {
                            if (ModuleInterfaceImpl.this.mGifManager.getWaitSavingDialogType() == 4 || ModuleInterfaceImpl.this.mGifManager.getWaitSavingDialogType() == 5) {
                                ModuleInterfaceImpl.this.mDialogManager.onDismissRotateDialog(true);
                            } else {
                                ModuleInterfaceImpl.this.mDialogManager.onDismissRotateDialog();
                            }
                        }
                        if (!AppControlUtil.isNeedQuickShotTaking()) {
                            ModuleInterfaceImpl.this.mNeedProgressDuringCapture = 0;
                        }
                    } else if (!ModuleInterfaceImpl.this.isPaused() && !ModuleInterfaceImpl.this.mDialogManager.isProgressDialogVisible() && !ModuleInterfaceImpl.this.getActivity().isFinishing()) {
                        ModuleInterfaceImpl.this.showDialog(type);
                    }
                }
            }
        }, (long) delay);
    }

    public void showProgressBarDialog(boolean show, int delay) {
        CamLog.m3d(CameraConstants.TAG, "[dialog] showProgressBarDialog = " + show);
        showProgressDialog(show, delay, 13);
    }

    public void showSavingDialog(boolean show, int delay) {
        showProgressDialog(show, delay, 5);
    }

    public void showProcessingDialog(boolean show, int delay) {
        CamLog.m3d(CameraConstants.TAG, "-c1- showProcessingDialog = " + show);
        showProgressDialog(show, delay, 3);
    }

    public boolean isProgressDialogVisible() {
        return this.mDialogManager == null ? true : this.mDialogManager.isProgressDialogVisible();
    }

    public void setReviewThumbBmp(Bitmap bmp) {
        this.mGet.setReviewThumbBmp(bmp);
    }

    public Bitmap getReviewThumbBmp() {
        return this.mGet.getReviewThumbBmp();
    }

    public boolean isAFSupported() {
        return this.mCameraCapabilities != null ? this.mCameraCapabilities.isAFSupported() : true;
    }

    public boolean isFlashSupported() {
        return this.mCameraCapabilities != null ? this.mCameraCapabilities.isFlashSupported() : true;
    }

    public boolean isMWAFSupported() {
        if (this.mCameraCapabilities != null) {
            return this.mCameraCapabilities.isMWContinousFocusSupported();
        }
        return false;
    }

    public boolean isTimerShotCountdown() {
        if (this.mTimerManager != null) {
            return this.mTimerManager.isTimerShotCountdown();
        }
        return false;
    }

    public boolean doBackKey() {
        return doBackKey();
    }

    public View getPreviewFrameLayout() {
        return this.mPreviewFrameLayout;
    }

    public int getDialogID() {
        return this.mDialogManager == null ? 0 : this.mDialogManager.getDialogID();
    }

    public boolean isRotateDialogVisible() {
        return this.mDialogManager == null ? false : this.mDialogManager.isRotateDialogVisible();
    }

    public void removeRotateDialog() {
        if (this.mDialogManager != null) {
            this.mDialogManager.onDismissRotateDialog();
        }
    }

    public void playSound(int id, boolean pBoolean, int pInt) {
        this.mGet.playSound(id, pBoolean, pInt);
    }

    public void playSound(int id, boolean pBoolean, int pInt, boolean requestAudioFocus) {
        this.mGet.playSound(id, pBoolean, pInt, requestAudioFocus);
    }

    public boolean isCenterKeyPressed() {
        if (this.mKeyManager != null) {
            return this.mKeyManager.isCenterKeyPressed();
        }
        return false;
    }

    public int getFocusState() {
        if (this.mFocusManager != null) {
            return this.mFocusManager.getFocusState();
        }
        return 0;
    }

    public boolean isFocusLock() {
        if (this.mFocusManager != null) {
            return this.mFocusManager.isFocusLock();
        }
        return false;
    }

    public boolean isAELock() {
        if (this.mFocusManager != null) {
            return this.mFocusManager.isAELock();
        }
        return false;
    }

    public boolean isAEAFJustLocked() {
        return this.mFocusManager == null ? false : this.mFocusManager.isAEAFJustLocked();
    }

    public void setFocusState(int focusState) {
        if (this.mFocusManager != null) {
            this.mFocusManager.setFocusState(focusState);
        }
    }

    public boolean checkFocusStateForChangingSetting() {
        if (this.mFocusManager != null) {
            return this.mFocusManager.checkFocusStateForChangingSetting();
        }
        return true;
    }

    public boolean isModuleChanging() {
        return this.mGet.isModuleChanging();
    }

    public void doInitSettingOrder() {
        this.mGet.doInitSettingOrder();
    }

    public void movePreviewOutOfWindow(boolean moveOut) {
        this.mGet.movePreviewOutOfWindow(moveOut);
    }

    public void setLocationOnByCamera(boolean isOn) {
        this.mGet.setLocationOnByCamera(isOn);
    }

    public void releaseAeControlBar() {
        if (this.mFocusManager != null) {
            this.mFocusManager.releaseAeControlBar();
        }
    }

    public boolean isAeControlBarTouched() {
        if (this.mFocusManager != null) {
            return this.mFocusManager.isAeControlBarTouched();
        }
        return false;
    }

    public boolean isAeControlBarShowing() {
        if (this.mFocusManager != null) {
            return this.mFocusManager.isAeControlBarShowing();
        }
        return false;
    }

    public int getAEControlLayoutButtomMargin() {
        if (this.mFocusManager != null) {
            return this.mFocusManager.getAEControlLayoutButtomMargin();
        }
        return 0;
    }

    public int getUspBottomMargin() {
        return this.mGet.getUspBottomMargin();
    }
}
