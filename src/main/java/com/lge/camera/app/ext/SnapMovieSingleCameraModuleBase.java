package com.lge.camera.app.ext;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.view.View;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.CaptureButtonManager;
import com.lge.camera.managers.QuickClipManager;
import com.lge.camera.managers.QuickclipManagerIF.onQuickClipListListener;
import com.lge.camera.managers.RecordingUIManager;
import com.lge.camera.managers.ReviewThumbnailManager;
import com.lge.camera.managers.ext.SnapMovieInterface;
import com.lge.camera.managers.ext.SnapMovieManagerTrigger;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;

public class SnapMovieSingleCameraModuleBase extends RecordingPriorityModule implements SnapMovieInterface {
    private String mSavePath = null;
    private int mSaveResult = 0;
    protected SnapMovieManagerTrigger mSnapManager = null;

    /* renamed from: com.lge.camera.app.ext.SnapMovieSingleCameraModuleBase$1 */
    class C04891 implements onQuickClipListListener {
        C04891() {
        }

        public void onListOpend() {
            SnapMovieSingleCameraModuleBase.this.showCommandArearUI(false);
            SnapMovieSingleCameraModuleBase.this.access$000(false);
            if (SnapMovieSingleCameraModuleBase.this.mSnapManager != null) {
                SnapMovieSingleCameraModuleBase.this.mSnapManager.setVisibleBar(false);
            }
        }

        public void onListClosed() {
            SnapMovieSingleCameraModuleBase.this.showCommandArearUI(true);
            SnapMovieSingleCameraModuleBase.this.access$100();
            if (SnapMovieSingleCameraModuleBase.this.mSnapManager != null) {
                SnapMovieSingleCameraModuleBase.this.mSnapManager.setVisibleBar(true);
            }
        }
    }

    public SnapMovieSingleCameraModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public String getShotMode() {
        return CameraConstants.MODE_SNAP;
    }

    public void init() {
        super.init();
        if (this.mSnapManager == null) {
            this.mSnapManager = new SnapMovieManagerTrigger(this);
        }
    }

    protected void addModuleManager() {
        super.addModuleManager();
        if (this.mSnapManager == null) {
            this.mSnapManager = new SnapMovieManagerTrigger(this);
        }
        this.mManagerList.add(this.mSnapManager);
    }

    public void initUI(View baseParent) {
        super.initUI(baseParent);
        this.mSnapManager.init(this.mGet, baseParent);
    }

    protected void changeRequester() {
        super.changeRequester();
        if (this.mSnapManager == null) {
            this.mSnapManager = new SnapMovieManagerTrigger(this);
        }
        this.mSnapManager.setInputSizeParam(this.mParamUpdater);
    }

    protected void afterStopRecording() {
        super.afterStopRecording();
        if (this.mSnapManager != null) {
            this.mSnapManager.afterStopRecording();
        }
    }

    public void onVideoStopClicked(boolean useThread, boolean stopByButton) {
        super.onVideoStopClicked(useThread, stopByButton);
        if (this.mSnapManager != null) {
            this.mSnapManager.onVideoStopClicked();
        }
    }

    public void enableConeMenuIcon(int coneMode, boolean enable) {
        if (this.mGet != null) {
            this.mGet.enableConeMenuIcon(coneMode, enable);
        }
    }

    public boolean onShutterLargeButtonLongClicked() {
        if (this.mSnapManager == null) {
            return false;
        }
        if (this.mSnapManager.onShutterLargeButtonLongClicked()) {
            return true;
        }
        super.onShutterTopButtonLongClickListener();
        return false;
    }

    public void onShutterLargeButtonClicked() {
        if (this.mSnapManager != null && !this.mSnapManager.onShutterLargeButtonClicked()) {
            super.onShutterLargeButtonClicked();
        }
    }

    public boolean isShutterKeyOptionTimerActivated() {
        if (this.mSnapManager != null && this.mSnapManager.isUseTimer()) {
            return super.isShutterKeyOptionTimerActivated();
        }
        return false;
    }

    protected void stopRecorder() {
        super.stopRecorder();
        if (this.mSnapManager != null) {
            this.mSnapManager.stopRecorder();
        }
    }

    public void sendLDBIntentOnAfterStopRecording() {
        if (!this.mSnapManager.isOrentationFixed()) {
            super.access$3700();
        }
    }

    protected String getLDBNonSettingString() {
        return super.getLDBNonSettingString() + "snap_mode=Single";
    }

    protected void restoreRecorderToIdle() {
        CamLog.m3d(CameraConstants.TAG, "restoreRecorderToIdle START");
        super.restoreRecorderToIdle();
        if (this.mSnapManager == null) {
            CamLog.m3d(CameraConstants.TAG, "Exit manager is null");
            return;
        }
        this.mSnapManager.decreaseShotCountAndTime(1);
        CamLog.m3d(CameraConstants.TAG, "restoreRecorderToIdle END");
    }

    protected void recorderOnError(MediaRecorder mr, int what, int extra) {
        super.recorderOnError(mr, what, extra);
        if (this.mSnapManager != null) {
            this.mSnapManager.recorderOnError(mr, what, extra);
        }
    }

    public String getCurTempDir() {
        if (this.mSnapManager == null) {
            return null;
        }
        return this.mSnapManager.getCurTempDir(this.mStorageManager);
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        if (this.mSnapManager != null) {
            this.mSnapManager.onResumeAfter();
        }
    }

    public void onPauseAfter() {
        setQuickButtonEnable(100, false, false);
        if (this.mSnapManager != null) {
            this.mSnapManager.onPauseAfter();
        }
        super.onPauseAfter();
    }

    protected void onChangeModuleBefore() {
        super.onChangeModuleBefore();
        if (!this.mGet.isCameraChangingOnSnap() && !CameraConstants.MODE_SNAP.equals(getSettingValue(Setting.KEY_MODE))) {
            ListPreference listPref = this.mGet.getListPreference(Setting.KEY_MODE);
            String defaultValue = listPref == null ? "mode_normal" : listPref.getDefaultValue();
            String modeRear = this.mGet.getSpecificSetting(true).getSettingValue(Setting.KEY_MODE);
            String modeFront = this.mGet.getSpecificSetting(false).getSettingValue(Setting.KEY_MODE);
            if (CameraConstants.MODE_SNAP.equals(modeRear)) {
                this.mGet.getSpecificSetting(true).setSetting(Setting.KEY_MODE, defaultValue, true);
            }
            if (CameraConstants.MODE_SNAP.equals(modeFront)) {
                this.mGet.getSpecificSetting(false).setSetting(Setting.KEY_MODE, defaultValue, true);
            }
        }
    }

    protected void updateRecordingTime() {
        super.access$1100();
        if (this.mSnapManager != null) {
            this.mSnapManager.updateRecordingTime();
        }
    }

    protected void resultVideoEditor(int resultCode, Intent data) {
        if (this.mSnapManager != null) {
            this.mSnapManager.resultVideoEditor(resultCode, data);
        }
    }

    protected boolean onReviewThumbnailClicked(int waitType) {
        if (this.mSnapManager == null) {
            return false;
        }
        this.mSnapManager.setStatus(0);
        return super.onReviewThumbnailClicked(waitType);
    }

    public void onOrientationChanged(int degree, boolean isFirst) {
        if (this.mSnapManager != null) {
            this.mSnapManager.rotateView(degree, isFirst);
            super.onOrientationChanged(degree, isFirst);
        }
    }

    public boolean isLiveSnapshotSupported() {
        return false;
    }

    public int getShutterButtonType() {
        return 4;
    }

    public boolean isZoomAvailable() {
        return true;
    }

    public Bitmap getPreviewBitmap() {
        return null;
    }

    protected void changeViewMode(int viewMode) {
    }

    public void onShutterStopButtonClicked() {
        if (this.mSnapManager != null) {
            this.mSnapManager.doShutterStopButtonClicked();
        }
    }

    public boolean isNeedFlip() {
        if (this.mFlipManager == null) {
            return false;
        }
        return this.mFlipManager.isNeedFlip(this.mCameraId);
    }

    public boolean isGestureShutterEnableCondition() {
        return false;
    }

    public QuickClipManager getQuickClipManager() {
        return this.mQuickClipManager;
    }

    public CaptureButtonManager getCaptureButtonManager() {
        return this.mCaptureButtonManager;
    }

    public RecordingUIManager getRecordingUIManager() {
        return this.mRecordingUIManager;
    }

    protected void setQuickClipListListener() {
        if (this.mQuickClipManager != null && isSupportedQuickClip()) {
            this.mQuickClipManager.setQuickClipListListener(new C04891());
        }
    }

    public void notifyNewMediaFromVideoTrim(Uri uri) {
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.updateThumbnail(uri, false, false, true, false);
        }
        this.mGet.onNewItemAdded(uri, 13, null);
        if (this.mGet == null || this.mQuickClipManager == null || ((this.mGet != null && getAppContext() == null) || ((this.mGet != null && this.mGet.getActivity() == null) || uri == null))) {
            updateSaveResult(2);
            return;
        }
        updateSaveResult(1);
        this.mQuickClipManager.setAfterShot();
        SharedPreferenceUtil.saveLastThumbnailPath(getAppContext(), this.mSavePath);
        SharedPreferenceUtil.saveLastThumbnailUri(getAppContext(), uri);
        SharedPreferences pref = this.mGet.getActivity().getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0);
        if (pref != null && !pref.getBoolean(CameraConstants.SNAP_DO_NOT_SHOW_SAVE_NOTE, false)) {
            showDialog(133);
        }
    }

    protected void enableControls(boolean enable) {
        super.access$600(enable);
        if (this.mSnapManager != null) {
            this.mSnapManager.doEnableContorls(enable);
        }
    }

    protected void doPhoneStateListenerAction(int state) {
        super.doPhoneStateListenerAction(state);
        if (this.mSnapManager != null) {
            this.mSnapManager.doPhoneStateAction(state);
        }
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (this.mSnapManager != null) {
            this.mSnapManager.doConfigurationChanged(this.mGet, config);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mSnapManager = null;
    }

    public ReviewThumbnailManager getReviewThumbnailManager() {
        return this.mReviewThumbnailManager;
    }

    public int getSaveResult() {
        return this.mSaveResult;
    }

    public void updateSaveResult(int result) {
        this.mSaveResult = result;
    }

    public void setSavePath(String path) {
        this.mSavePath = path;
    }

    protected int getLoopRecordingType() {
        return 0;
    }

    public boolean onShowMenu(int menuType) {
        if (!super.onShowMenu(menuType) || this.mSnapManager == null) {
            return false;
        }
        this.mSnapManager.onShowSpecificMenu();
        return true;
    }

    public boolean onHideMenu(int menuType) {
        if (!super.onHideMenu(menuType)) {
            return false;
        }
        if (!(isMenuShowing((menuType ^ -1) & CameraConstants.MENU_TYPE_ALL) || this.mSnapManager == null)) {
            this.mSnapManager.onHideSpecificMenu();
        }
        return true;
    }

    public void removeUIBeforeModeChange() {
        super.removeUIBeforeModeChange();
        if (this.mSnapManager != null) {
            this.mSnapManager.onShowSpecificMenu();
        }
    }

    public void setCaptureButtonEnableByNaviBar(boolean enable) {
        super.setCaptureButtonEnableByNaviBar(enable);
        if (!enable && this.mSnapManager != null && this.mSnapManager.getStatus() == 2) {
            this.mSnapManager.onReleaseShutterLongPress(null);
        }
    }

    protected boolean displayUIComponentAfterOneShot() {
        if (!super.displayUIComponentAfterOneShot() || this.mSnapManager == null) {
            return false;
        }
        this.mSnapManager.doCleanView(false, true, true);
        return true;
    }

    public void setTextGuideVisibilityForEachMode(boolean visible) {
        if (this.mSnapManager != null) {
            this.mSnapManager.setVisibleGuideText(visible);
        }
    }

    public void onShowAEBar() {
        if (this.mSnapManager != null) {
            this.mSnapManager.setVisibleBar(false);
        }
    }

    public void onHideAEBar() {
        if (this.mSnapManager != null) {
            this.mSnapManager.setVisibleBar(true);
        }
    }

    public int getSnapFixedDegree() {
        if (this.mSnapManager == null) {
            return -1;
        }
        return this.mSnapManager.getFixedDegree();
    }
}
