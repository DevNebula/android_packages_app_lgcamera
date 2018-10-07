package com.lge.camera.managers.ext;

import android.os.SystemClock;
import android.os.SystemProperties;
import com.lge.camera.components.StartRecorderInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.MmsProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.IntentBroadcastUtil;
import com.lge.camera.util.SettingKeyWrapper;
import java.io.File;

public class RecordingPriorityManager extends ManagerInterfaceImpl {
    public RecordingPriorityInterface mGet = null;

    public RecordingPriorityManager(RecordingPriorityInterface moduleInterface) {
        super(moduleInterface);
        this.mGet = moduleInterface;
    }

    public double getVideoFPS() {
        String[] removeAt = this.mGet.getSettingValue(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId())).split("@");
        String videoSize = removeAt[0];
        String prefFps = removeAt.length > 1 ? removeAt[1] : null;
        double fps = 30.0d;
        if (MmsProperties.isAvailableMmsResolution(this.mGet.getAppContext().getContentResolver(), videoSize)) {
            fps = 15.0d;
        }
        if (prefFps != null) {
            return (double) Integer.valueOf(prefFps).intValue();
        }
        return fps;
    }

    public boolean doStartRecorder() {
        CamLog.m3d(CameraConstants.TAG, "doStartRecorder");
        if (this.mGet.getRecordingUIManager() != null) {
            this.mGet.getRecordingUIManager().initVideoTime();
        }
        IntentBroadcastUtil.stopVoiceRec(this.mGet.getActivity(), true);
        if (this.mGet.getToastManager().isShowing()) {
            this.mGet.getToastManager().hideAndResetDisturb(0);
        }
        ListPreference listPref = (ListPreference) this.mGet.getListPreference(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()));
        if (listPref == null) {
            CamLog.m5e(CameraConstants.TAG, "KEY_VIDEO_RECORDSIZE listPref is null in startRecorder");
            return false;
        }
        this.mGet.setCameraState(5);
        this.mGet.setParamForVideoRecord(true, listPref);
        this.mGet.setTextureLayoutParams(this.mGet.getVideoSize()[0], this.mGet.getVideoSize()[1], -1);
        this.mGet.runStartRecorder(true);
        return true;
    }

    public StartRecorderInfo makeRecorderInfo() {
        String videoSize = this.mGet.getSettingValue(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId())).split("@")[0];
        int purpose = 1;
        if (CameraConstants.MODE_CINEMA.equals(this.mGet.getShotMode()) && FunctionProperties.isSupportedHDR10()) {
            purpose = 1 | 16;
        }
        String extend = ".mp4";
        int storage = this.mGet.getCurStorage();
        boolean isCNasLimitation = this.mGet.isCnasRecordingLimitation(storage);
        if (isCNasLimitation) {
            storage = 0;
        }
        String dir = isCNasLimitation ? this.mGet.getStorageSaveDir(storage) : this.mGet.getCurDir();
        String fileName = this.mGet.makeFileName(1, storage, dir, false, this.mGet.getSettingValue(Setting.KEY_MODE));
        String outFilePath = dir + fileName + extend;
        CamLog.m3d(CameraConstants.TAG, "output file is : " + outFilePath);
        return new StartRecorderInfo(storage, fileName, outFilePath, videoSize, purpose, this.mGet.getVideoFPS(), this.mGet.getVideoBitrate(), CameraDeviceUtils.getVideoFlipType(this.mGet.getVideoOrientation(), this.mGet.getCameraId(), "off".equals(this.mGet.getSettingValue(Setting.KEY_SAVE_DIRECTION)), this.mGet.getShotMode().contains(CameraConstants.MODE_SNAP)));
    }

    public void setFpsRange() {
        String fps = SystemProperties.get(ParamConstants.FPS_RANGE_SYSTEM_PROPERTY_CAMCORDER, "15000,30000");
        if (CameraConstants.MODE_CINEMA.equals(this.mGet.getShotMode())) {
            fps = "30000,30000";
        } else {
            this.mGet.getParamUpdater().setParamValue(ParamConstants.KEY_HFR, "off");
        }
        this.mGet.getParamUpdater().setParamValue(ParamConstants.KEY_PREVIEW_FPS_RANGE, fps);
    }

    public boolean doStopRecorder(String fileName, String filePath) {
        return doStopRecorder(fileName, filePath, "");
    }

    public boolean doStopRecorder(String fileName, String filePath, String uuid) {
        CamLog.m3d(CameraConstants.TAG, "doStopRecorder");
        if (this.mGet.checkModuleValidate(192)) {
            CamLog.m3d(CameraConstants.TAG, "EXIT doStopRecorder");
            return false;
        }
        IntentBroadcastUtil.unblockAlarmInRecording(this.mGet.getActivity());
        hideRecordingUI();
        boolean deleteFile = false;
        if (this.mGet.getRecordingUIManager() != null) {
            this.mGet.getRecordingUIManager().updateVideoTime(3, SystemClock.uptimeMillis());
            this.mGet.getRecordingUIManager().setRecDurationTime((long) this.mGet.getRecCompensationTime());
            if (!this.mGet.getRecordingUIManager().checkMinRecTime()) {
                deleteFile = true;
            }
        }
        this.mGet.getCameraDevice().setRecordSurfaceToTarget(false);
        this.mGet.videoRecorderRelease();
        AudioUtil.setAllSoundCaseMute(getAppContext(), false);
        this.mGet.playRecordingSound(false);
        AudioUtil.enableRaM(getAppContext(), true);
        showCoverForRecording();
        String videoSize = this.mGet.getSettingValue(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId())).split("@")[0];
        String dir = FileUtil.getDirFromFullName(filePath);
        this.mGet.startHeatingWarning(false);
        if (this.mGet.getQuickClipManager() != null) {
            this.mGet.getQuickClipManager().setAfterShot();
        }
        if (filePath != null) {
            File file = new File(filePath);
            if (file != null) {
                if (deleteFile) {
                    CamLog.m3d(CameraConstants.TAG, "The recording time is too short, delete the file.");
                    file.delete();
                } else {
                    CamLog.m3d(CameraConstants.TAG, "save file path : " + filePath);
                    this.mGet.getMediaSaveService().addVideo(this.mGet.getAppContext(), this.mGet.getAppContext().getContentResolver(), dir, fileName, videoSize, this.mGet.getRecDurationTime(filePath), file.length(), this.mGet.getCurrentLocation(), 1, this.mGet.getOnMediaSavedListener(), this.mGet.getModeColumn(), uuid);
                }
            }
        }
        this.mGet.clearEmptyVideoFile();
        this.mGet.checkStorage();
        return true;
    }

    private void hideRecordingUI() {
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (RecordingPriorityManager.this.mGet != null) {
                    RecordingPriorityManager.this.mGet.keepScreenOnAwhile();
                    if (RecordingPriorityManager.this.mGet.getRecordingUIManager() != null) {
                        RecordingPriorityManager.this.mGet.getRecordingUIManager().hide();
                        RecordingPriorityManager.this.mGet.getRecordingUIManager().destroyLayout();
                    }
                }
            }
        }, 0);
    }

    private void showCoverForRecording() {
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (RecordingPriorityManager.this.mGet != null) {
                    RecordingPriorityManager.this.mGet.showPreviewCoverForRecording(false);
                }
            }
        }, 0);
    }
}
