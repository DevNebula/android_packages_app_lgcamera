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
import com.lge.camera.device.CameraParameters;
import com.lge.camera.managers.CaptureButtonManager;
import com.lge.camera.managers.QuickClipManager;
import com.lge.camera.managers.QuickclipManagerIF.onQuickClipListListener;
import com.lge.camera.managers.RecordingUIManager;
import com.lge.camera.managers.ReviewThumbnailManager;
import com.lge.camera.managers.ext.SnapMovieInterface;
import com.lge.camera.managers.ext.SnapMovieManagerTrigger;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;

public class SnapMovieFrontCameraModuleBase extends RecordingPriorityBeautyModule implements SnapMovieInterface {
    private String mSavePath = null;
    private int mSaveResult = 0;
    protected SnapMovieManagerTrigger mSnapManager = null;

    /* renamed from: com.lge.camera.app.ext.SnapMovieFrontCameraModuleBase$1 */
    class C04881 implements onQuickClipListListener {
        C04881() {
        }

        public void onListOpend() {
            SnapMovieFrontCameraModuleBase.this.showCommandArearUI(false);
            if (SnapMovieFrontCameraModuleBase.this.mSnapManager != null) {
                SnapMovieFrontCameraModuleBase.this.mSnapManager.setVisibleBar(false);
            }
        }

        public void onListClosed() {
            SnapMovieFrontCameraModuleBase.this.showCommandArearUI(true);
            if (SnapMovieFrontCameraModuleBase.this.mSnapManager != null) {
                SnapMovieFrontCameraModuleBase.this.mSnapManager.setVisibleBar(true);
            }
        }
    }

    public SnapMovieFrontCameraModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public synchronized void setBeautyEngineOn(boolean bIsOn) {
        super.setBeautyEngineOn(false);
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

    public boolean setBarSetting(String key, String value, boolean save) {
        return super.setBarSetting(key, value, false);
    }

    protected void setParameterByLGSF(CameraParameters parameters, String shotMode, boolean isRecording) {
        super.setParameterByLGSF(parameters, super.getShotMode(), isRecording);
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

    public void onShutterTopButtonClickListener() {
        if (this.mSnapManager != null && !this.mSnapManager.onShutterTopButtonClickListener(this.mPostviewManager, this.mReviewThumbnailManager)) {
            super.onShutterTopButtonClickListener();
        }
    }

    public boolean onShutterBottomButtonLongClickListener() {
        CamLog.m3d(CameraConstants.TAG, "do nothing on snap mode");
        return true;
    }

    protected void stopRecorder() {
        super.stopRecorder();
        if (this.mSnapManager != null) {
            this.mSnapManager.stopRecorder();
        }
    }

    public void sendLDBIntentOnAfterStopRecording() {
        if (this.mSnapManager == null || !this.mSnapManager.isOrentationFixed()) {
            super.access$3700();
        }
    }

    protected String getLDBNonSettingString() {
        return super.getLDBNonSettingString() + "snap_mode=Single";
    }

    protected void restoreRecorderToIdle() {
        super.restoreRecorderToIdle();
        if (this.mSnapManager != null) {
            this.mSnapManager.decreaseShotCountAndTime(1);
        }
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

    public String getCurDir() {
        if (this.mSnapManager == null) {
            return "";
        }
        return this.mSnapManager.getCurDir(this.mStorageManager, this.mNeedProgressDuringCapture);
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

    public boolean isLiveSnapshotSupported() {
        return false;
    }

    protected void changeViewMode() {
    }

    public int getShutterButtonType() {
        return 4;
    }

    public Bitmap getPreviewBitmap() {
        return null;
    }

    public boolean isIntervalShotEnableCondition() {
        return false;
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
            this.mQuickClipManager.setQuickClipListListener(new C04881());
        }
    }

    public void notifyNewMediaFromVideoTrim(Uri uri) {
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.updateThumbnail(uri, false, false, true, false);
        }
        if (this.mGet == null || this.mQuickClipManager == null || ((this.mGet != null && getAppContext() == null) || ((this.mGet != null && this.mGet.getActivity() == null) || uri == null))) {
            updateSaveResult(2);
            return;
        }
        this.mGet.onNewItemAdded(uri, 13, null);
        updateSaveResult(1);
        this.mQuickClipManager.setAfterShot();
        SharedPreferenceUtil.saveLastThumbnailPath(getAppContext(), this.mSavePath);
        SharedPreferenceUtil.saveLastThumbnailUri(getAppContext(), uri);
        SharedPreferences pref = this.mGet.getActivity().getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0);
        if (pref != null && !pref.getBoolean(CameraConstants.SNAP_DO_NOT_SHOW_SAVE_NOTE, false)) {
            showDialog(133);
        }
    }

    public int getSnapFixedDegree() {
        if (this.mSnapManager == null) {
            return -1;
        }
        return this.mSnapManager.getFixedDegree();
    }

    protected void enableControls(boolean enable, boolean changeColor) {
        super.enableControls(enable, changeColor);
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
        if (this.mSnapManager != null) {
            this.mSnapManager.onDestroy();
            this.mSnapManager = null;
        }
    }

    public ReviewThumbnailManager getReviewThumbnailManager() {
        return this.mReviewThumbnailManager;
    }

    protected boolean checkToUseFilmEffect() {
        return false;
    }

    public void hideZoomBar() {
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

    public void setCaptureButtonEnableByNaviBar(boolean enable) {
        super.setCaptureButtonEnableByNaviBar(enable);
        if (!enable && this.mSnapManager != null && this.mSnapManager.getStatus() == 2) {
            this.mSnapManager.onReleaseShutterLongPress(null);
        }
    }
}
