package com.lge.camera.app.ext;

import android.graphics.ColorFilter;
import android.net.Uri;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.components.DrawingPanel;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.file.FileManager;
import com.lge.camera.managers.AudioRecorderManager;
import com.lge.camera.managers.AudioRecorderManager.AudioRecorderManagerListener;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.IntentBroadcastUtil;
import com.lge.camera.util.MDMUtil;
import com.lge.camera.util.TalkBackUtil;
import com.lge.camera.util.TelephonyUtil;
import com.lge.panorama.Panorama.XmpAvailableListener;
import com.lge.panorama.Panorama.XmpInfo;
import java.io.File;

public class PanoramaModuleLG360Proj extends PanoramaModuleLGRaw implements AudioRecorderManagerListener {
    protected static final int BTN_PANO_ALL = 1;
    protected static final int BTN_PANO_SND_REC = 1;
    public static final String EQUI_RECTANGULAR = "equirectangular";
    public static final String PANO_TRUE = "True";
    private final String DEFAULT_TEMP_PATH = "/storage/emulated/0/DCIM/.thumbnails/";
    private final String PANO_TEMP_FILE_NAME = "PanoJpegTemp";
    protected AudioRecorderManager mAudioRecMgr = new AudioRecorderManager(this);
    protected int mMakePanorama360 = 1;
    protected RotateImageButton mSndRecBtn = null;
    protected XmpInfo mXmpInfo = new XmpInfo();

    /* renamed from: com.lge.camera.app.ext.PanoramaModuleLG360Proj$1 */
    class C04251 implements OnClickListener {
        C04251() {
        }

        public void onClick(View arg0) {
            if (PanoramaModuleLG360Proj.this.mSndRecBtn != null) {
                if (PanoramaModuleLG360Proj.this.mSndRecBtn.isEnabled()) {
                    String updateValue = "on".equals(PanoramaModuleLG360Proj.this.mGet.getCurSettingValue(Setting.KEY_PANO_SOUND_REC)) ? "off" : "on";
                    PanoramaModuleLG360Proj.this.mGet.setSetting(Setting.KEY_PANO_SOUND_REC, updateValue, true);
                    PanoramaModuleLG360Proj.this.mSndRecBtn.setBackgroundResource("on".equals(updateValue) ? C0088R.drawable.btn_pano_snd_rec_on : C0088R.drawable.btn_pano_snd_rec_off);
                    ColorFilter cf = ColorUtil.getNormalColorByAlpha();
                    PanoramaModuleLG360Proj.this.mSndRecBtn.setColorFilter(cf);
                    PanoramaModuleLG360Proj.this.mSndRecBtn.getBackground().setColorFilter(cf);
                    TalkBackUtil.setTalkbackDescOnDoubleTap(PanoramaModuleLG360Proj.this.getAppContext(), PanoramaModuleLG360Proj.this.mGet.getAppContext().getString("on".equals(updateValue) ? C0088R.string.talkback_btn_selected : C0088R.string.talkback_btn_not_selected));
                    return;
                }
                PanoramaModuleLG360Proj.this.setSndBtnEnable(false);
            }
        }
    }

    /* renamed from: com.lge.camera.app.ext.PanoramaModuleLG360Proj$2 */
    class C04262 implements XmpAvailableListener {
        C04262() {
        }

        public void onXmpAvailable(int crop_left, int crop_top, int crop_width, int crop_height, int whole_width, int whole_height, float hori_fov, float vert_fov) {
            if (PanoramaModuleLG360Proj.this.mPanorama != null) {
                if (PanoramaModuleLG360Proj.this.mXmpInfo == null) {
                    PanoramaModuleLG360Proj.this.mXmpInfo = new XmpInfo();
                }
                PanoramaModuleLG360Proj.this.mXmpInfo.mCropLeft = crop_left;
                PanoramaModuleLG360Proj.this.mXmpInfo.mCropTop = crop_top;
                PanoramaModuleLG360Proj.this.mXmpInfo.mCropWidth = crop_width;
                PanoramaModuleLG360Proj.this.mXmpInfo.mCropHeight = crop_height;
                PanoramaModuleLG360Proj.this.mXmpInfo.mWholeWidth = whole_width;
                PanoramaModuleLG360Proj.this.mXmpInfo.mWholeHeight = whole_height;
                PanoramaModuleLG360Proj.this.mXmpInfo.mHoriFov = hori_fov;
                PanoramaModuleLG360Proj.this.mXmpInfo.mVertFov = vert_fov;
                CamLog.m7i(CameraConstants.TAG, "XMP info - crop left : " + PanoramaModuleLG360Proj.this.mXmpInfo.mCropLeft + ", crop top : " + PanoramaModuleLG360Proj.this.mXmpInfo.mCropTop + ", crop width : " + PanoramaModuleLG360Proj.this.mXmpInfo.mCropWidth + ", crop height : " + PanoramaModuleLG360Proj.this.mXmpInfo.mCropHeight + ", whole width : " + PanoramaModuleLG360Proj.this.mXmpInfo.mWholeWidth + ", whole height : " + PanoramaModuleLG360Proj.this.mXmpInfo.mWholeHeight + ", horizontal fov : " + PanoramaModuleLG360Proj.this.mXmpInfo.mHoriFov + ", vertical fov : " + PanoramaModuleLG360Proj.this.mXmpInfo.mVertFov);
            }
        }
    }

    public PanoramaModuleLG360Proj(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public int getCameraIdFromPref() {
        return 2;
    }

    protected int getPanoramaCameraId() {
        return getCameraIdFromPref();
    }

    public void createViews(boolean visible) {
        super.createViews(visible);
        if (!(this.mBaseView == null || this.mButtonLayout == null)) {
            this.mSndRecBtn = (RotateImageButton) this.mBaseView.findViewById(C0088R.id.btn_pano_snd_record);
            this.mButtonLayout.setVisibility(visible ? 0 : 4);
        }
        updateBtnDegree(this.mGet.getOrientationDegree(), false);
        setBtnListener();
        if (this.mAudioRecMgr != null) {
            this.mAudioRecMgr.setListener(this);
        }
    }

    public void init() {
        super.init();
        if (this.mAudioRecMgr == null) {
            this.mAudioRecMgr = new AudioRecorderManager(this);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mSndRecBtn = null;
        this.mButtonLayout = null;
        if (this.mAudioRecMgr != null) {
            this.mAudioRecMgr.release();
        }
    }

    protected void doPhoneStateListenerAction(int state) {
        super.doPhoneStateListenerAction(state);
        if (state == 0) {
            if (MDMUtil.allowMicrophone()) {
                setSndBtnEnable(true);
                return;
            }
            stopAudioRecord();
            setSndBtnEnable(false);
        } else if (state == 2) {
            stopAudioRecord();
            setSndBtnEnable(false);
        } else if (state == 1) {
            setSndBtnEnable(false);
        }
    }

    public void setDoubleCameraEnable(boolean enable) {
        if (this.mDoubleCameraManager != null) {
            this.mDoubleCameraManager.setDualViewControlEnabled(false);
        }
    }

    public void showDoubleCamera(boolean show) {
        if (this.mDoubleCameraManager != null) {
            this.mDoubleCameraManager.showDualViewControl(false);
        }
    }

    protected void updateBtnDegree(int degree, boolean animation) {
        if (this.mSndRecBtn != null) {
            this.mSndRecBtn.setDegree(degree, animation);
        }
    }

    public boolean onShowMenu(int menuType) {
        if (!super.onShowMenu(menuType)) {
            return false;
        }
        showPanoramaBtns(1, 4);
        return true;
    }

    public boolean onHideMenu(int menuType) {
        if (!super.onHideMenu(menuType)) {
            return false;
        }
        showPanoramaBtns(1, 0);
        return true;
    }

    protected boolean mainHandlerHandleMessage(Message msg) {
        switch (msg.what) {
            case 31:
                if (this.mBaseView != null) {
                    this.mBaseView.setVisibility(4);
                }
                return super.mainHandlerHandleMessage(msg);
            default:
                return super.mainHandlerHandleMessage(msg);
        }
    }

    protected void showPanoramaBtns(int type, int visible) {
        if (type == 1) {
            if (this.mButtonLayout != null) {
                this.mButtonLayout.setVisibility(visible);
            }
        } else if ((type & 1) != 0 && this.mSndRecBtn != null) {
            this.mSndRecBtn.setVisibility(visible);
        }
    }

    protected void setBtnListener() {
        if (this.mSndRecBtn != null) {
            if (checkAudioState()) {
                this.mSndRecBtn.setBackgroundResource("on".equals(this.mGet.getCurSettingValue(Setting.KEY_PANO_SOUND_REC)) ? C0088R.drawable.btn_pano_snd_rec_on : C0088R.drawable.btn_pano_snd_rec_off);
            } else {
                setSndBtnEnable(false);
            }
            this.mSndRecBtn.setOnClickListener(new C04251());
        }
    }

    protected String getLDBNonSettingString() {
        return super.getLDBNonSettingString() + "pano_360_rec=" + this.mGet.getCurSettingValue(Setting.KEY_PANO_SOUND_REC);
    }

    protected void setSndBtnEnable(boolean enable) {
        if (this.mSndRecBtn == null) {
            CamLog.m11w(CameraConstants.TAG, "SndRecBtn is null!!");
            return;
        }
        this.mSndRecBtn.setBackgroundResource("on".equals(enable ? this.mGet.getCurSettingValue(Setting.KEY_PANO_SOUND_REC) : "off") ? C0088R.drawable.btn_pano_snd_rec_on : C0088R.drawable.btn_pano_snd_rec_off);
        ColorFilter cf = enable ? ColorUtil.getNormalColorByAlpha() : ColorUtil.getDimColorByAlpha();
        this.mSndRecBtn.setColorFilter(cf);
        this.mSndRecBtn.getBackground().setColorFilter(cf);
        this.mSndRecBtn.setEnabled(enable);
    }

    protected void setDrawingPanelView() {
        this.mDrawingPanel = (DrawingPanel) this.mBaseView.findViewById(C0088R.id.preview_drawingpanel);
        this.mDrawingPanel.setArrowRes(C0088R.drawable.panorama_guide_arrow_right, C0088R.drawable.panorama_guide_arrow_left, C0088R.drawable.panorama_guide_arrow_up, C0088R.drawable.panorama_guide_arrow_down);
    }

    protected int[] setExifSize(ExifInterface exif) {
        return new int[]{this.mXmpInfo.mCropWidth, this.mXmpInfo.mCropHeight};
    }

    protected void setPanoramaMode360() {
        if (this.mPanorama != null) {
            this.mPanorama.setMakePanorama360(this.mMakePanorama360);
            this.mPanorama.setMinimumAnglePanorama360(270.0f);
            this.mPanorama.setEnableCylindricalStripWarping(true);
            this.mPanorama.setXmpAvailableListener(new C04262());
        }
    }

    protected boolean backKeyProcessing() {
        if (this.mState < 3 || this.mState > 5) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "Panorama doBackKey - mState : " + this.mState);
        if (this.mPanorama != null) {
            this.mPanorama.setMinimumAnglePanorama360(360.0f);
        }
        return true;
    }

    protected void calcPanoramaBuffer(float focalLength, float focalDistance, float sensorWidth, float sensorHeight, float armDistance) {
        if (this.mTrackingMode == 1) {
            this.mBufferCalculator.setBufferCalculator(this.mViewAngleH, this.mViewAngleV, this.mFrameSize, sensorWidth, sensorHeight, focalLength, focalDistance, 360, 0.0f, true);
        } else {
            this.mBufferCalculator.setBufferCalculator(this.mViewAngleH, this.mViewAngleV, this.mFrameSize, sensorWidth, sensorHeight, focalLength, focalDistance, 360, armDistance, true);
        }
    }

    protected Uri addXMPMetaData(String file_name, long dateTaken, String directory, String output_img_path, int degree, int[] exifSize) {
        CamLog.m3d(CameraConstants.TAG, "addXmpAudioMetaData - start");
        String sndRec = this.mGet.getCurSettingValue(Setting.KEY_PANO_SOUND_REC);
        String panoFileName = file_name + ".jpg";
        CamLog.m3d(CameraConstants.TAG, "panoFileName : " + panoFileName);
        if ("on".equals(sndRec) && this.mAudioRecMgr != null && this.mAudioRecMgr.waitReleaseRecording()) {
            String audFilePath;
            if (isCNASStorage()) {
                audFilePath = getPanoramaFileDir(true) + getPanoramaFileName(true) + ".jpg";
                CamLog.m3d(CameraConstants.TAG, "Saving audFilePath : " + audFilePath);
                File tempFile;
                if (this.mAudioRecMgr.addXmpAudioMetaData(output_img_path, audFilePath)) {
                    String tempFilePath = directory + panoFileName;
                    CamLog.m3d(CameraConstants.TAG, "Delete tempFilePath : " + tempFilePath);
                    tempFile = new File(tempFilePath);
                    if (tempFile != null && tempFile.exists()) {
                        tempFile.delete();
                        CamLog.m3d(CameraConstants.TAG, "origin temp file is deleted.");
                    }
                } else {
                    CamLog.m11w(CameraConstants.TAG, "addXmpAudioMetaData has an error. It should be renamed.");
                    tempFile = new File(directory + panoFileName);
                    File file = new File(audFilePath);
                    if (tempFile != null && tempFile.exists()) {
                        tempFile.renameTo(file);
                    }
                }
                directory = getPanoramaFileDir(true);
                panoFileName = getPanoramaFileName(true) + ".jpg";
            } else {
                audFilePath = directory + file_name + "_aud" + ".jpg";
                if (this.mAudioRecMgr.addXmpAudioMetaData(output_img_path, audFilePath)) {
                    File targetFile = new File(directory + panoFileName);
                    if (targetFile != null && targetFile.exists()) {
                        targetFile.delete();
                        CamLog.m3d(CameraConstants.TAG, "origin file is deleted.");
                    }
                    File audFile = new File(audFilePath);
                    if (audFile != null && audFile.exists()) {
                        audFile.renameTo(targetFile);
                        CamLog.m3d(CameraConstants.TAG, "audiofile rename to target file.");
                    }
                } else {
                    CamLog.m5e(CameraConstants.TAG, "addXmpAudioMetaData has an error.");
                }
            }
        }
        Uri resultUri = FileManager.registerPanoramaUri(this.mGet.getActivity().getContentResolver(), directory, panoFileName, dateTaken, getCurrentLocation(), degree, exifSize, "True", EQUI_RECTANGULAR, this.mXmpInfo.mCropWidth, this.mXmpInfo.mCropHeight, this.mXmpInfo.mCropLeft, this.mXmpInfo.mCropTop, this.mXmpInfo.mWholeWidth, this.mXmpInfo.mWholeHeight, this.mXmpInfo.mHoriFov);
        CamLog.m3d(CameraConstants.TAG, "addXmpAudioMetaData - end");
        return resultUri;
    }

    protected void resetToPreviewState() {
        super.resetToPreviewState();
        showPanoramaBtns(1, 0);
    }

    public void onOrientationChanged(int degree, boolean isFirst) {
        super.onOrientationChanged(degree, isFirst);
        if (this.mState != 5) {
            updateBtnDegree(degree, true);
        }
    }

    protected void doPanoramaJobStartAfter() {
        showPanoramaBtns(1, 4);
        startAudioRecordWithChecking();
    }

    protected void stopPanoramaBefore(boolean isNeedSaving) {
        stopAudioRecord();
    }

    protected boolean startAudioRecordWithChecking() {
        if ("off".equals(this.mGet.getCurSettingValue(Setting.KEY_PANO_SOUND_REC)) || !checkAudioState()) {
            return false;
        }
        IntentBroadcastUtil.stopVoiceRec(this.mGet.getActivity(), true);
        IntentBroadcastUtil.blockAlarmInRecording(this.mGet.getActivity(), true);
        access$300(true);
        AudioUtil.setAudioFocus(getAppContext(), true, false);
        if (AudioUtil.isAudioRecording(getAppContext())) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (AudioUtil.isAudioRecording(PanoramaModuleLG360Proj.this.getAppContext())) {
                        AudioUtil.setAudioAvailability(false);
                        PanoramaModuleLG360Proj.this.access$300(false);
                        CamLog.m11w(CameraConstants.TAG, "Audio is recording now.");
                        return;
                    }
                    AudioUtil.setAudioAvailability(true);
                    PanoramaModuleLG360Proj.this.startAudioRecord();
                }
            }, 300);
            return false;
        }
        startAudioRecord();
        return true;
    }

    protected void startAudioRecord() {
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (PanoramaModuleLG360Proj.this.mState < 4) {
                    CamLog.m3d(CameraConstants.TAG, "Panorama is not taking. state = " + PanoramaModuleLG360Proj.this.mState + ". don't startAudioRecord()");
                    PanoramaModuleLG360Proj.this.afterAudioRecordingStopped(false);
                } else if (PanoramaModuleLG360Proj.this.mAudioRecMgr != null) {
                    AudioUtil.setAllSoundCaseMute(PanoramaModuleLG360Proj.this.getAppContext(), true);
                    String tempFilePath = PanoramaModuleLG360Proj.this.mStorageManager.getTempDir(0);
                    File tempDir = new File(tempFilePath);
                    if (!(tempDir == null || tempDir.exists())) {
                        tempDir.mkdir();
                    }
                    PanoramaModuleLG360Proj.this.mAudioRecMgr.initAudioRecorder(tempFilePath + "panoAudioDataTemp" + ".mp3");
                    PanoramaModuleLG360Proj.this.mAudioRecMgr.startAudioRecord();
                }
            }
        }, 500);
    }

    public boolean afterAudioRecordingStopped(final boolean audRec) {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (audRec) {
                    IntentBroadcastUtil.unblockAlarmInRecording(PanoramaModuleLG360Proj.this.mGet.getActivity());
                    AudioUtil.setAllSoundCaseMute(PanoramaModuleLG360Proj.this.getAppContext(), false);
                    AudioUtil.setAudioFocus(PanoramaModuleLG360Proj.this.getAppContext(), false);
                    if (PanoramaModuleLG360Proj.this.mCheeseShutterManager != null) {
                        PanoramaModuleLG360Proj.this.mCheeseShutterManager.setCheeseShutterSetting("on".equals(PanoramaModuleLG360Proj.this.mGet.getCurSettingValue(Setting.KEY_VOICESHUTTER)), true);
                    }
                }
                if (PanoramaModuleLG360Proj.this.mState > 4 && !PanoramaModuleLG360Proj.this.mIsFeeding && !PanoramaModuleLG360Proj.this.isPaused()) {
                    PanoramaModuleLG360Proj.this.mGet.playSound(3, false, 0);
                }
            }
        });
        return true;
    }

    protected void stopAudioRecord() {
        if (this.mState < 4) {
            CamLog.m3d(CameraConstants.TAG, "stopAudioRecord exit by mState=" + this.mState);
            return;
        }
        if ("off".equals(this.mGet.getCurSettingValue(Setting.KEY_PANO_SOUND_REC))) {
            if (!this.mIsFeeding && !isPaused() && this.mState > 4) {
                this.mGet.playSound(3, false, 0);
            }
        } else if (this.mAudioRecMgr != null) {
            this.mAudioRecMgr.stopAudioRecord(true);
        }
    }

    protected boolean checkAudioState() {
        if (TelephonyUtil.phoneInCall(this.mGet.getAppContext()) || AudioUtil.isInP2PCallMode(this.mGet.getAppContext()) || !MDMUtil.allowMicrophone()) {
            return false;
        }
        return true;
    }

    protected boolean isCNASStorage() {
        return getCurStorage() == 2;
    }

    protected void setSignatureSetting() {
        setSpecificSettingValueAndDisable(Setting.KEY_SIGNATURE, "off", false);
    }

    protected void restoreSignatureSetting() {
        restoreSettingValue(Setting.KEY_SIGNATURE);
    }

    protected void notifyChangeChildSetting(String key) {
        if (ModelProperties.isLguCloudServiceModel() && Setting.KEY_STORAGE.equals(key) && this.mSndRecBtn != null && checkAudioState()) {
            setSndBtnEnable(true);
        }
    }

    protected String makePanoTempFileDir() {
        if (this.mStorageManager == null) {
            return "/storage/emulated/0/DCIM/.thumbnails/";
        }
        String tempFilePath = this.mStorageManager.getTempDir(0);
        File tempDir = new File(tempFilePath);
        if (tempDir == null || tempDir.exists()) {
            return tempFilePath;
        }
        tempDir.mkdir();
        return tempFilePath;
    }

    protected String getPanoramaFileName(boolean needOriginalPath) {
        String sndRec = this.mGet.getCurSettingValue(Setting.KEY_PANO_SOUND_REC);
        if (needOriginalPath || "off".equals(sndRec) || !isCNASStorage()) {
            return this.mFileName;
        }
        return "PanoJpegTemp";
    }

    protected String getPanoramaFileDir(boolean needOriginalPath) {
        String sndRec = this.mGet.getCurSettingValue(Setting.KEY_PANO_SOUND_REC);
        if (needOriginalPath || "off".equals(sndRec) || !isCNASStorage()) {
            return this.mDirectory;
        }
        return makePanoTempFileDir();
    }

    protected boolean isEnableStop360() {
        return false;
    }

    public String getShotMode() {
        return CameraConstants.MODE_PANORAMA_LG_360_PROJ;
    }
}
