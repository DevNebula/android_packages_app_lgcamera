package com.lge.camera.app;

import android.content.ContentProviderClient;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.managers.ConeUIManagerBase;
import com.lge.camera.managers.ConeUIManagerInterface;
import com.lge.camera.managers.ConeUIManagerInterface.OnConeViewModeButtonListener;
import com.lge.camera.managers.HelpManager;
import com.lge.camera.managers.SignatureManager;
import com.lge.camera.managers.SignatureManagerInterface;
import com.lge.camera.managers.SoundManager;
import com.lge.camera.managers.SoundManagerInterface;
import com.lge.camera.managers.ThumbnailListManager;
import com.lge.camera.managers.TilePreviewInterface;
import com.lge.camera.managers.UspZoneInterface;
import com.lge.camera.managers.UspZoneManager;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.ModeItem;
import com.lge.camera.settings.ModeMenuInterface;
import com.lge.camera.settings.ModeMenuManager;
import com.lge.camera.settings.PreferenceGroup;
import com.lge.camera.settings.Setting;
import com.lge.camera.settings.SettingIntegration;
import com.lge.camera.settings.SettingIntegrationManual;
import com.lge.camera.settings.SettingInterface;
import com.lge.camera.settings.SettingManager;
import com.lge.camera.systeminput.OrientationManager;
import com.lge.camera.systeminput.OrientationManager.OrientationChangedListener;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.QuickWindowUtils;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.SmartcamUtil;

public abstract class ExpandActivity extends BaseActivity implements SettingInterface, ModeMenuInterface, OrientationChangedListener, SoundManagerInterface, ConeUIManagerInterface, TilePreviewInterface, UspZoneInterface, SignatureManagerInterface {
    protected ConeUIManagerBase mConeUIManager = null;
    protected HelpManager mHelpManager = new HelpManager(this);
    private ContentProviderClient mMediaProviderClient;
    protected ModeMenuManager mModeMenuManager = new ModeMenuManager(this);
    protected OrientationManager mOrientationInfo = null;
    protected SettingIntegration mSettingManager = new SettingIntegration(this);
    protected SignatureManager mSignatureManager = null;
    protected SoundManager mSoundManager = new SoundManager(this);
    protected ThumbnailListManager mThumbnailListManager = new ThumbnailListManager(this);
    protected UspZoneManager mUspZoneManager = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!this.mIsCheckingPermission) {
            if (this.mSoundManager != null) {
                this.mSoundManager.init();
            }
            setOrientationLock(true);
            keepMediaProviderInstance();
            if (FunctionProperties.isSignatureSupported(this)) {
                this.mSignatureManager = new SignatureManager(this);
                this.mSignatureManager.init();
            }
            if (FunctionProperties.isUspZoneSupported() && !isAttachIntent() && !isMMSIntent()) {
                this.mUspZoneManager = new UspZoneManager(this);
                this.mUspZoneManager.init();
            }
        }
    }

    protected int checkCameraId(int cameraId) {
        if (this.mVoiceAssistantManager != null) {
            cameraId = this.mVoiceAssistantManager.checkCameraId(cameraId);
        }
        return super.checkCameraId(cameraId);
    }

    public boolean isActivityPaused() {
        return isPaused();
    }

    protected void initActivityManager() {
        this.mOrientationInfo = new OrientationManager(this);
        this.mOrientationInfo.resume(this);
    }

    protected void createSettingManagers() {
        if (this.mSettingManager != null) {
            if (FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO) || FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA)) {
                this.mSettingManager.closeSetting();
                this.mSettingManager.onDestroy();
                this.mSettingManager = new SettingIntegrationManual(this);
            }
            this.mSettingManager.inflateSetting();
            this.mSettingManager.createSetting();
            this.mSettingManager.initSettingOrder();
        }
    }

    public void onResume() {
        if (this.mIgnoreRequestPermissionsIfNeededCall || this.mIsCheckingPermission) {
            super.onResume();
            return;
        }
        if (this.mOrientationInfo != null) {
            this.mOrientationInfo.resume(this);
        }
        super.onResume();
        if (this.mModeMenuManager != null) {
            this.mModeMenuManager.onResume();
        }
        if (this.mUspZoneManager != null) {
            this.mUspZoneManager.onResume();
        }
        if (this.mSoundManager != null) {
            this.mSoundManager.onResumeAfter();
        }
        keepMediaProviderInstance();
        if (this.mThumbnailListManager != null) {
            this.mThumbnailListManager.onResume();
        }
        if (this.mHelpManager != null) {
            this.mHelpManager.onResume();
        }
        if (this.mSignatureManager != null && !AppControlUtil.isStartFromOnCreate()) {
            this.mSignatureManager.init();
        }
    }

    public void onPause() {
        if (this.mIgnoreRequestPermissionsIfNeededCall || this.mIsCheckingPermission) {
            super.onPause();
            return;
        }
        if (this.mOrientationInfo != null) {
            this.mOrientationInfo.pause();
        }
        super.onPause();
        if (this.mSettingManager != null) {
            this.mSettingManager.onPause();
        }
        if (this.mModeMenuManager != null) {
            this.mModeMenuManager.onPause();
        }
        if (this.mUspZoneManager != null) {
            this.mUspZoneManager.onPause();
        }
        if (this.mHelpManager != null) {
            this.mHelpManager.onPause();
        }
        if (this.mConeUIManager != null) {
            this.mConeUIManager.onPause();
        }
        if (this.mThumbnailListManager != null) {
            this.mThumbnailListManager.onPause();
        }
        if (this.mSignatureManager != null) {
            this.mSignatureManager.onPause();
        }
    }

    public void onStop() {
        super.onStop();
        if (!this.mIgnoreRequestPermissionsIfNeededCall && !this.mIsCheckingPermission) {
            releaseMediaProviderInstance();
            if (this.mThumbnailListManager != null) {
                this.mThumbnailListManager.onStop();
            }
            if (this.mVoiceAssistantManager != null) {
                this.mVoiceAssistantManager.onStop();
            }
        }
    }

    public void onDestroy() {
        if (this.mIgnoreRequestPermissionsIfNeededCall || this.mIsCheckingPermission) {
            super.onDestroy();
            return;
        }
        if (this.mSettingManager != null) {
            this.mSettingManager.closeSetting();
            this.mSettingManager.onDestroy();
        }
        if (this.mModeMenuManager != null) {
            this.mModeMenuManager.onDestroy();
        }
        if (this.mUspZoneManager != null) {
            this.mUspZoneManager.onDestroy();
        }
        if (this.mHelpManager != null) {
            this.mHelpManager.onDestroy();
        }
        if (this.mThumbnailListManager != null) {
            this.mThumbnailListManager.onDestroy();
        }
        super.onDestroy();
        if (this.mSoundManager != null) {
            this.mSoundManager.onDestroy();
        }
        if (this.mOrientationInfo != null) {
            this.mOrientationInfo.unbind();
            this.mOrientationInfo = null;
        }
        this.mConeUIManager = null;
        if (FunctionProperties.isSupportedSmartCam(getAppContext()) && SmartcamUtil.isSmartcamBindService() && !isAttachIntent()) {
            SmartcamUtil.smartcamUnBindService(getAppContext());
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 79) {
            if (this.mSettingManager != null && this.mSettingManager.onKeyDown(keyCode, event)) {
                return true;
            }
            if (this.mModeMenuManager != null && this.mModeMenuManager.isVisible()) {
                this.mModeMenuManager.hide(true, true);
                return true;
            } else if (this.mHelpManager != null && this.mHelpManager.isVisible()) {
                this.mHelpManager.hide(true);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (!this.mPaused) {
            if (this.mSettingManager != null) {
                this.mSettingManager.onConfigurationChanged(config);
            }
            if (this.mOrientationInfo != null) {
                this.mOrientationInfo.onConfigurationChanged(config);
            }
            if (this.mModeMenuManager != null) {
                this.mModeMenuManager.onConfigurationChanged(config);
            }
            if (this.mHelpManager != null) {
                this.mHelpManager.onConfigurationChanged(config);
            }
        }
    }

    public void onOrientationChanged(int degree, boolean isFirst) {
        if (this.mSettingManager != null) {
            this.mSettingManager.onOrientationChanged(degree);
        }
        if (this.mModeMenuManager != null) {
            this.mModeMenuManager.setDegree(degree);
        }
        if (this.mHelpManager != null) {
            this.mHelpManager.onOrientationChanged(degree);
        }
        if (this.mConeUIManager != null) {
            this.mConeUIManager.setRotateDegree(degree, !isFirst);
        }
        if (this.mThumbnailListManager != null) {
            this.mThumbnailListManager.setDegree(degree, false);
        }
    }

    public void setUspVisibility(int visibility) {
        if (this.mUspZoneManager != null) {
            this.mUspZoneManager.setVisibility(visibility);
        }
    }

    public boolean isUspVisible() {
        if (this.mUspZoneManager != null) {
            return this.mUspZoneManager.isVisible();
        }
        return false;
    }

    public boolean isUspZoneSupportedMode(String shotMode) {
        return this.mUspZoneManager == null ? false : this.mUspZoneManager.isUspZoneSupportedMode(shotMode);
    }

    public int getUspBottomMargin() {
        if (this.mUspZoneManager == null) {
            return -1;
        }
        return this.mUspZoneManager.getUspBottomMargin();
    }

    public boolean unselectUspOnBackKey() {
        if (this.mUspZoneManager != null) {
            return this.mUspZoneManager.doBackKey();
        }
        return false;
    }

    public boolean isModeMenuVisible() {
        if (this.mModeMenuManager != null) {
            return this.mModeMenuManager.isVisible();
        }
        return false;
    }

    public void deleteMode(ModeItem item) {
        if (this.mModeMenuManager != null) {
            this.mModeMenuManager.deleteMode(item);
        }
    }

    public boolean isModeEditable() {
        if (this.mModeMenuManager == null) {
            return false;
        }
        return this.mModeMenuManager.isEditable();
    }

    public void switchModeList() {
        if (this.mModeMenuManager != null) {
            setModeEditMode(false);
            this.mModeMenuManager.saveModePreference();
            this.mModeMenuManager.makeModeList();
        }
    }

    public void setModeEditMode(boolean selected) {
        if (this.mModeMenuManager != null) {
            this.mModeMenuManager.onEditButtonClicked(selected);
        }
    }

    public void onModeItemClick(String shotMode) {
        if (this.mModeMenuManager != null) {
            this.mModeMenuManager.onModeItemClick(shotMode);
        }
    }

    public void showModeMenu(boolean useAnim) {
        if (this.mModeMenuManager != null) {
            this.mModeMenuManager.show(useAnim);
        }
    }

    public void hideModeMenu(boolean showAnimation, boolean onlyHideMenu) {
        if (this.mModeMenuManager != null) {
            this.mModeMenuManager.hide(showAnimation, !onlyHideMenu);
        }
        if (!onlyHideMenu) {
            onHideMenu(2);
        }
    }

    public void showHelpList(boolean useAnim) {
        if (this.mHelpManager != null) {
            this.mHelpManager.showHelpList(useAnim);
        }
    }

    public void hideHelpList(boolean showAnimation, boolean onlyHideMenu) {
        if (this.mHelpManager != null) {
            this.mHelpManager.hide(showAnimation);
        }
        if (!onlyHideMenu) {
            onHideMenu(8);
        }
    }

    public void refreshSettingByCameraId() {
        if (this.mSettingManager != null) {
            this.mSettingManager.refreshSettingByCameraId();
        }
    }

    public void setSetting(String key, String value, boolean save) {
        if (this.mSettingManager != null) {
            this.mSettingManager.setSetting(key, value, save);
        }
    }

    public boolean setForcedSetting(String key, String value) {
        if (this.mSettingManager != null) {
            return this.mSettingManager.setForcedSetting(key, value);
        }
        return false;
    }

    public String getCurSettingValue(String key) {
        if (this.mSettingManager != null) {
            return this.mSettingManager.getSettingValue(key);
        }
        return "not found";
    }

    public int getSettingIndex(String key) {
        if (this.mSettingManager != null) {
            return this.mSettingManager.getSettingIndex(key);
        }
        return -1;
    }

    public SettingManager getSettingManager() {
        return this.mSettingManager;
    }

    public void setupSetting() {
        if (this.mSettingManager != null) {
            this.mSettingManager.setupSetting();
        }
    }

    public void updateGuideTextSettingMenu(String key, String guideText) {
        if (this.mSettingManager != null) {
            this.mSettingManager.updateGuideTextSettingMenu(key, guideText);
        }
    }

    public ListPreference getListPreference(String key) {
        if (this.mSettingManager != null) {
            return this.mSettingManager.getSetting().getListPreference(key);
        }
        return new ListPreference(getAppContext(), "not found");
    }

    public ListPreference getListPreference(String key, boolean isRear) {
        if (this.mSettingManager != null) {
            return this.mSettingManager.getSpecificSetting(isRear).getListPreference(key);
        }
        return new ListPreference(getAppContext(), "not found");
    }

    public PreferenceGroup getPreferenceGroup() {
        if (this.mSettingManager != null) {
            return this.mSettingManager.getSetting().getPreferenceGroup();
        }
        return null;
    }

    public void setAllSettingMenuEnable(boolean enable) {
        if (this.mSettingManager != null) {
            this.mSettingManager.setAllSettingMenuEnable(enable);
        }
    }

    public void setSettingMenuEnable(String key, boolean enable) {
        if (this.mSettingManager != null) {
            this.mSettingManager.setSettingMenuEnable(key, enable);
        }
    }

    public boolean getSettingMenuEnable(String key) {
        if (this.mSettingManager != null) {
            return this.mSettingManager.getSettingMenuEnable(key);
        }
        return false;
    }

    public void setSettingChildMenuEnable(String key, String value, boolean enable) {
        if (this.mSettingManager != null) {
            this.mSettingManager.setSettingChildMenuEnable(key, value, enable);
        }
    }

    public void setAllSettingChildMenuEnable(String key, boolean enable) {
        if (this.mSettingManager != null) {
            this.mSettingManager.setAllSettingChildMenuEnable(key, enable);
        }
    }

    public void backupSetting(String key, String value) {
        if (this.mSettingManager != null) {
            this.mSettingManager.backupSetting(key, value);
        }
    }

    public void restoreBackupSetting(String key, boolean saveSetting) {
        if (this.mSettingManager != null) {
            this.mSettingManager.restoreBackupSetting(key, saveSetting);
        }
    }

    public void showSettingMenu(boolean direct) {
        if (this.mSettingManager != null) {
            this.mSettingManager.displaySettingView(direct);
        }
    }

    public void removeSettingMenu(boolean direct, boolean onlyHideMenu) {
        CamLog.m3d(CameraConstants.TAG, "removeSettingMenu");
        if (this.mSettingManager != null) {
            if (direct) {
                this.mSettingManager.removeSettingViewAll();
            } else {
                this.mSettingManager.removeSettingView();
            }
        }
        if (!onlyHideMenu) {
            onHideMenu(1);
        }
    }

    public boolean isSettingMenuVisible() {
        if (this.mSettingManager != null) {
            return this.mSettingManager.isVisible();
        }
        return false;
    }

    public boolean isOpeningSettingMenu() {
        if (this.mSettingManager != null) {
            return this.mSettingManager.isOpeningSettingMenu();
        }
        return false;
    }

    public boolean isHelpListVisible() {
        if (this.mHelpManager != null) {
            return this.mHelpManager.isVisible();
        }
        return false;
    }

    public boolean isSettingChildMenuVisible() {
        if (this.mSettingManager != null) {
            return this.mSettingManager.isChildViewVisible();
        }
        return false;
    }

    public void removeChildSettingView(boolean isShowAnim) {
        if (this.mSettingManager != null) {
            this.mSettingManager.removeChildSettingView(isShowAnim);
        }
    }

    public Setting getSpecificSetting(boolean rear) {
        if (this.mSettingManager != null) {
            return this.mSettingManager.getSpecificSetting(rear);
        }
        return null;
    }

    protected void setDefaultPreferenceValue() {
        postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (ExpandActivity.this.mSettingManager != null) {
                    PreferenceGroup prefGroup = ExpandActivity.this.mSettingManager.getPrefGroup();
                    int size = prefGroup.size();
                    String defaultValue = "";
                    for (int i = 0; i < size; i++) {
                        ListPreference listPref = prefGroup.getListPreference(i);
                        if (listPref != null) {
                            defaultValue = listPref.getDefaultValue();
                            if (!"".equals(defaultValue)) {
                                ExpandActivity.this.mSettingManager.setSetting(listPref.getKey(), defaultValue, true);
                            }
                        }
                    }
                }
            }
        });
    }

    public int getOrientationDegree() {
        if (this.mOrientationInfo == null || !this.mOrientationInfo.isOrientationLocked()) {
            return 0;
        }
        return this.mOrientationInfo.getOrientationManagerDegree();
    }

    public void setOrientationLock(boolean lock) {
        if (this.mOrientationInfo == null) {
            return;
        }
        if (lock) {
            this.mOrientationInfo.lockOrientationPortrait(getActivity());
        } else {
            this.mOrientationInfo.unlockOrientation(getActivity());
        }
    }

    public void playSound(int id, boolean pBoolean, int pInt) {
        if (this.mSoundManager != null) {
            this.mSoundManager.playSound(id, pBoolean, pInt);
        }
    }

    public void playSound(int type, boolean pBoolean, int pInt, boolean requestAudioFocus) {
        if (requestAudioFocus) {
            AudioUtil.setAudioFocus(getAppContext(), true);
        }
        playSound(type, pBoolean, pInt);
        if (requestAudioFocus) {
            AudioUtil.setAudioFocus(getAppContext(), false);
        }
    }

    public void stopSound(int type) {
        if (this.mSoundManager != null) {
            this.mSoundManager.stopSound(type);
        }
    }

    public void loadSound() {
        if (this.mSoundManager != null) {
            this.mSoundManager.loadSound();
        }
    }

    public boolean isOrientationLocked() {
        if (this.mOrientationInfo != null) {
            return this.mOrientationInfo.isOrientationLocked();
        }
        return false;
    }

    private void keepMediaProviderInstance() {
        if (this.mMediaProviderClient == null) {
            this.mMediaProviderClient = getContentResolver().acquireContentProviderClient("media");
        }
    }

    private void releaseMediaProviderInstance() {
        if (this.mMediaProviderClient != null) {
            this.mMediaProviderClient.release();
            this.mMediaProviderClient = null;
        }
    }

    public int getCameraId() {
        return 0;
    }

    public void setCurrentConeMode(int mode, boolean save) {
        if (this.mConeUIManager != null) {
            this.mConeUIManager.setCurrentViewMode(mode, save);
        }
    }

    public int getCurrentConeMode() {
        if (QuickWindowUtils.isQuickWindowCameraMode()) {
            return 1;
        }
        if (this.mConeUIManager != null) {
            return this.mConeUIManager.getCurrentViewMode();
        }
        return 0;
    }

    public void setOnConeViewModeButtonListener(OnConeViewModeButtonListener listener) {
        if (this.mConeUIManager != null) {
            this.mConeUIManager.setOnConeViewModeButtonListener(listener);
        }
    }

    public void showConeViewMode(boolean show) {
        if (this.mConeUIManager != null) {
            this.mConeUIManager.showConeViewMode(show);
        }
    }

    public void setConeModeChanged() {
        if (this.mConeUIManager != null) {
            this.mConeUIManager.setConeModeChanged();
        }
    }

    public void enableConeMenuIcon(int coneType, boolean enable) {
        if (this.mConeUIManager != null) {
            this.mConeUIManager.enableConeMenuIcon(coneType, enable);
        }
    }

    public void setConeClickable(boolean bClickable) {
        if (this.mConeUIManager != null) {
            this.mConeUIManager.setClickable(bClickable);
        }
    }

    public String getSettingValue(String keyMode) {
        if (this.mSettingManager != null) {
            return this.mSettingManager.getSettingValue(keyMode);
        }
        return null;
    }

    public void onHelpButtonClicked(int quickButtonId) {
        if (this.mHelpManager != null) {
            this.mHelpManager.onHelpButtonClicked(quickButtonId);
        }
    }

    public boolean isNeedHelpItem() {
        if (this.mHelpManager != null) {
            return this.mHelpManager.isNeedHelpItem();
        }
        return false;
    }

    public void setFrontFlashOff() {
        this.mSettingManager.setFrontFlashOff();
    }

    public void onNewItemAdded(Uri uri, int mode, String burstId) {
        if (this.mThumbnailListManager != null) {
            this.mThumbnailListManager.onNewItemAdded(uri, mode, burstId);
        }
    }

    public boolean isActivatedTilePreview() {
        if (this.mThumbnailListManager != null) {
            return this.mThumbnailListManager.isActivatedTilePreview();
        }
        return false;
    }

    public boolean isActivatedQuickdetailView() {
        if (this.mThumbnailListManager != null) {
            return this.mThumbnailListManager.isActivatedQuickdetailView();
        }
        return false;
    }

    public void setDeleteButtonVisibility(boolean visibility) {
        if (this.mThumbnailListManager != null) {
            this.mThumbnailListManager.setDeleteButtonVisibility(visibility);
        }
    }

    public void closeDetailView() {
        if (this.mThumbnailListManager != null) {
            this.mThumbnailListManager.closeDetailView();
        }
    }

    public void thumbnailListInit() {
        if (this.mThumbnailListManager != null) {
            this.mThumbnailListManager.thumbnailListInit();
        }
    }

    public void delayCursorUpdate() {
        if (this.mThumbnailListManager != null) {
            this.mThumbnailListManager.delayCursorUpdate();
        }
    }

    public void refreshTilePreviewCursor() {
        if (this.mThumbnailListManager != null) {
            this.mThumbnailListManager.refreshAdaptersByQuickView();
        }
    }

    public void initSignatureContent() {
        if (this.mSignatureManager != null) {
            this.mSignatureManager.initSignatureContent();
        }
    }

    public String getSignatureText() {
        return this.mSignatureManager == null ? "" : this.mSignatureManager.getSignatureText();
    }

    protected void resultSignature(int resultCode, Intent data) {
        if (this.mSignatureManager != null) {
            this.mSignatureManager.onActivityResult(resultCode, data);
        }
    }

    public void startSignatureActivity() {
        if (this.mSignatureManager != null) {
            this.mSignatureManager.startSignatureActivity();
        }
    }

    public boolean isNeedToStartSignatureActivity(String value) {
        return this.mSignatureManager != null ? this.mSignatureManager.isNeedToStartSignatureActivity(value) : false;
    }

    public byte[] composeSignatureImage(byte[] jpegData, int degree) {
        if (this.mSignatureManager == null) {
            return jpegData;
        }
        return this.mSignatureManager.composeSignatureImage(jpegData, degree);
    }

    public Bitmap composeSignatureImage(Bitmap originalBitmap, int degree) {
        return this.mSignatureManager != null ? this.mSignatureManager.composeSignatureImage(originalBitmap, degree) : null;
    }

    public void composeSignatureImage(Image yuvImage, int degree) {
        if (this.mSignatureManager != null) {
            this.mSignatureManager.composeSignatureImage(yuvImage, degree);
        }
    }

    public Bitmap getSignatureBitmap(int oriImageWidth, int oriImageHeight, int degree) {
        if (this.mSignatureManager == null) {
            return null;
        }
        if (degree == Integer.MIN_VALUE) {
            return this.mSignatureManager.getSignatureBitmapForSticker(oriImageWidth, oriImageHeight);
        }
        return this.mSignatureManager.getSignatureBitmap(oriImageWidth, oriImageHeight, degree, false);
    }

    protected void setFlagsByIntentAction() {
        super.setFlagsByIntentAction();
        if (isGraphyIntent()) {
            setFromGraphyFlag(true);
            SharedPreferenceUtilBase.setCameraId(getAppContext(), SharedPreferenceUtil.getRearCameraId(getAppContext()));
        }
        if (this.mVoiceAssistantManager != null) {
            this.mVoiceAssistantManager.setFlagsByIntent();
        }
    }

    public void updateUspLayout(String newUsp, boolean selected) {
        if (this.mUspZoneManager != null) {
            this.mUspZoneManager.updateLayout(newUsp, selected);
        }
    }

    public String getCurrentUSPZone() {
        if (this.mUspZoneManager != null) {
            return this.mUspZoneManager.getCurValue();
        }
        return CameraConstantsEx.USP_NORMAL;
    }
}
