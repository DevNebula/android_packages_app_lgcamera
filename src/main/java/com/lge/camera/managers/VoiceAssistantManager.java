package com.lge.camera.managers;

import android.content.Intent;
import android.os.Bundle;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.settings.ModeMenuManagerBase;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import java.io.File;
import java.util.HashMap;
import java.util.Set;

public class VoiceAssistantManager implements OnRemoveHandler {
    protected FlagState mFlagState = new FlagState(this, null);
    protected HelpInterface mGet;
    protected String mSwapString = null;

    private class FlagState {
        private boolean mHasAnyFlags;
        private HashMap<String, Object> map;

        private FlagState() {
            this.map = new HashMap();
            this.mHasAnyFlags = false;
        }

        /* synthetic */ FlagState(VoiceAssistantManager x0, C11871 x1) {
            this();
        }

        public void setFlag(String key, Object value) {
            if (this.map != null) {
                this.map.put(key, value);
            }
        }

        public Object getFlag(String key) {
            return this.map == null ? null : this.map.get(key);
        }

        public void setHasAnyFlags(boolean set) {
            this.mHasAnyFlags = set;
        }

        public boolean hasAnyFlags() {
            Intent intent = VoiceAssistantManager.this.mGet.getActivityIntent();
            if (intent == null || (1048576 & intent.getFlags()) == 0) {
                return this.mHasAnyFlags;
            }
            return false;
        }

        public void clear() {
            if (this.map != null) {
                this.map.clear();
            }
            this.mHasAnyFlags = false;
        }
    }

    public VoiceAssistantManager(HelpInterface helpInterface) {
        this.mGet = helpInterface;
    }

    public void setFlagsByIntent() {
        Intent intent = this.mGet.getActivityIntent();
        if (intent != null) {
            this.mFlagState = new FlagState(this, null);
            if (this.mFlagState != null) {
                printAllExtras(intent);
                this.mFlagState.setFlag(CameraConstantsEx.FLAG_USE_FRONT_CAMERA, Boolean.valueOf(intent.getBooleanExtra(CameraConstantsEx.FLAG_USE_FRONT_CAMERA, false)));
                if (intent.hasExtra(CameraConstantsEx.FLAG_USE_FRONT_CAMERA)) {
                    this.mFlagState.setHasAnyFlags(true);
                }
                this.mFlagState.setFlag(CameraConstantsEx.FLAG_TIMER_DURATION_SECONDS, Integer.valueOf(intent.getIntExtra(CameraConstantsEx.FLAG_TIMER_DURATION_SECONDS, -1)));
                if (intent.hasExtra(CameraConstantsEx.FLAG_TIMER_DURATION_SECONDS)) {
                    this.mFlagState.setHasAnyFlags(true);
                }
                this.mFlagState.setFlag(CameraConstantsEx.FLAG_CAMERA_MODE, intent.getStringExtra(CameraConstantsEx.FLAG_CAMERA_MODE));
                if (intent.hasExtra(CameraConstantsEx.FLAG_CAMERA_MODE)) {
                    this.mFlagState.setHasAnyFlags(true);
                }
                this.mFlagState.setFlag(CameraConstantsEx.FLAG_CAMERA_SUB_MODE, intent.getStringExtra(CameraConstantsEx.FLAG_CAMERA_SUB_MODE));
                if (intent.hasExtra(CameraConstantsEx.FLAG_CAMERA_SUB_MODE)) {
                    this.mFlagState.setHasAnyFlags(true);
                }
                if (CameraConstantsEx.FLAG_VALUE_MODE_CINE_BEAUTY.equalsIgnoreCase(intent.getStringExtra(CameraConstantsEx.FLAG_CAMERA_MODE))) {
                    this.mFlagState.setFlag(CameraConstantsEx.FLAG_CAMERA_MODE, CameraConstantsEx.FLAG_VALUE_MODE_CINEMA);
                    this.mFlagState.setFlag(CameraConstantsEx.FLAG_CAMERA_SUB_MODE, "beauty");
                    this.mFlagState.setHasAnyFlags(true);
                }
                this.mFlagState.setFlag(CameraConstantsEx.FLAG_CAMERA_OPEN_ONLY, Boolean.valueOf(intent.getBooleanExtra(CameraConstantsEx.FLAG_CAMERA_OPEN_ONLY, false)));
                if (intent.hasExtra(CameraConstantsEx.FLAG_CAMERA_OPEN_ONLY)) {
                    this.mFlagState.setHasAnyFlags(true);
                }
                if (CameraConstantsEx.FLAG_VALUE_LDU_WIDE_CAMERA.equalsIgnoreCase(getStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null))) {
                    this.mFlagState.setFlag(CameraConstantsEx.FLAG_CAMERA_OPEN_ONLY, Boolean.valueOf(true));
                }
                boolean boolValue = intent.getBooleanExtra(CameraConstantsEx.FLAG_LAUNCH_GALLERY, false);
                this.mFlagState.setFlag(CameraConstantsEx.FLAG_LAUNCH_GALLERY, Boolean.valueOf(boolValue));
                if (boolValue) {
                    this.mFlagState.setHasAnyFlags(true);
                }
                String stringValue = intent.getStringExtra(CameraConstantsEx.FLAG_PICTURE_SIZE);
                if (ModelProperties.getLCDType() == 1) {
                    if (CameraConstantsEx.FLAG_VALUE_PIC_SIZE_18_9_9.equals(stringValue) || CameraConstantsEx.FLAG_VALUE_PIC_SIZE_FULL_VISION.equalsIgnoreCase(stringValue)) {
                        stringValue = CameraConstantsEx.FLAG_VALUE_PIC_SIZE_18_9;
                    }
                } else if (ModelProperties.getLCDType() == 2 && (CameraConstantsEx.FLAG_VALUE_PIC_SIZE_18_9.equals(stringValue) || CameraConstantsEx.FLAG_VALUE_PIC_SIZE_FULL_VISION.equalsIgnoreCase(stringValue))) {
                    stringValue = CameraConstantsEx.FLAG_VALUE_PIC_SIZE_18_9_9;
                }
                this.mFlagState.setFlag(CameraConstantsEx.FLAG_PICTURE_SIZE, stringValue);
                if (intent.hasExtra(CameraConstantsEx.FLAG_PICTURE_SIZE)) {
                    this.mFlagState.setHasAnyFlags(true);
                }
                boolValue = intent.getBooleanExtra(CameraConstantsEx.FLAG_USE_REAR_CAMERA, false);
                this.mFlagState.setFlag(CameraConstantsEx.FLAG_USE_REAR_CAMERA, Boolean.valueOf(boolValue));
                if (boolValue) {
                    this.mFlagState.setHasAnyFlags(true);
                }
            }
        }
    }

    private void printAllExtras(Intent intent) {
        if (intent != null) {
            CamLog.m3d(CameraConstants.TAG, "-Voice Assistant- action = " + intent.getAction());
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Set<String> keySet = bundle.keySet();
                if (keySet != null) {
                    for (String key : keySet) {
                        CamLog.m3d(CameraConstants.TAG, "-Voice Assistant- key = " + key + ", value = " + bundle.get(key));
                    }
                }
            }
        }
    }

    public boolean isAssistantSpecified() {
        return this.mFlagState == null ? false : this.mFlagState.hasAnyFlags();
    }

    public void setFlag(String key, Object value) {
        if (this.mFlagState != null) {
            this.mFlagState.setFlag(key, value);
        }
    }

    private Object getFlag(String key) {
        return this.mFlagState == null ? null : this.mFlagState.getFlag(key);
    }

    public boolean getBooleanFlag(String key, boolean defaultValue) {
        if (this.mFlagState == null) {
            return defaultValue;
        }
        Boolean obj = getFlag(key);
        if (obj == null) {
            return defaultValue;
        }
        Boolean flag = obj;
        if (flag != null) {
            return flag.booleanValue();
        }
        return defaultValue;
    }

    public int getIntFlag(String key, int defaultValue) {
        if (this.mFlagState == null) {
            return defaultValue;
        }
        Integer obj = getFlag(key);
        if (obj == null) {
            return defaultValue;
        }
        Integer flag = obj;
        if (flag != null) {
            return flag.intValue();
        }
        return defaultValue;
    }

    public String getStringFlag(String key, String defaultValue) {
        if (this.mFlagState == null) {
            return defaultValue;
        }
        String obj = getFlag(key);
        if (obj == null) {
            return defaultValue;
        }
        String flag = obj;
        if (flag != null) {
            return flag;
        }
        return defaultValue;
    }

    public String getAssistantInitMode() {
        String assistantShotMode = getStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null);
        String initMode = "mode_normal";
        int cameraId = SharedPreferenceUtil.getCameraId(this.mGet.getAppContext());
        this.mSwapString = "rear";
        if (!CameraDeviceUtils.isRearCamera(cameraId)) {
            if (FunctionProperties.isSupportedBeautyShot()) {
                initMode = CameraConstants.MODE_BEAUTY;
                this.mSwapString = "front";
            } else if (FunctionProperties.isSupportedGestureShot()) {
                initMode = CameraConstants.MODE_GESTURESHOT;
                this.mSwapString = "front";
            }
        }
        if (CameraConstantsEx.FLAG_VALUE_MODE_CINEMA.equals(assistantShotMode) && FunctionProperties.isCineVideoAvailable(this.mGet.getAppContext())) {
            initMode = CameraConstants.MODE_CINEMA;
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_MANUAL.equals(assistantShotMode)) {
            if (this.mGet.isAssistantImageIntent() && ConfigurationUtil.containsMode(CameraConstants.MODE_MANUAL_CAMERA)) {
                initMode = CameraConstants.MODE_MANUAL_CAMERA;
            } else if (this.mGet.isAssistantVideoIntent() && ConfigurationUtil.containsMode(CameraConstants.MODE_MANUAL_VIDEO)) {
                initMode = CameraConstants.MODE_MANUAL_VIDEO;
            }
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_PANORAMA.equals(assistantShotMode)) {
            if (ConfigurationUtil.containsMode(CameraConstants.MODE_PANORAMA_LG)) {
                initMode = CameraConstants.MODE_PANORAMA_LG;
            } else if (ConfigurationUtil.containsMode(CameraConstants.MODE_PANORAMA_LG_RAW) || ConfigurationUtil.containsMode("mode_panorama_360_normal")) {
                initMode = CameraConstants.MODE_PANORAMA_LG_RAW;
            }
        } else if (CameraConstantsEx.FLAG_VALUE_GRAPHY.equalsIgnoreCase(assistantShotMode) && ConfigurationUtil.containsMode(CameraConstants.MODE_MANUAL_CAMERA)) {
            initMode = CameraConstants.MODE_MANUAL_CAMERA;
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_FOOD.equalsIgnoreCase(assistantShotMode) && ConfigurationUtil.containsMode("mode_food")) {
            initMode = "mode_food";
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_SMARTCAM.equals(assistantShotMode) && FunctionProperties.isSupportedSmartCam(this.mGet.getAppContext())) {
            if (CameraDeviceUtils.isRearCamera(cameraId)) {
                initMode = CameraConstants.MODE_SMART_CAM;
                this.mSwapString = "rear";
            } else {
                initMode = CameraConstants.MODE_SMART_CAM_FRONT;
                this.mSwapString = "front";
            }
            initMode = CameraDeviceUtils.isRearCamera(cameraId) ? CameraConstants.MODE_SMART_CAM : CameraConstants.MODE_SMART_CAM_FRONT;
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_OUTFOCUS.equals(assistantShotMode)) {
            initMode = "mode_normal";
            if (CameraDeviceUtils.isRearCamera(cameraId)) {
                if (ConfigurationUtil.containsMode(CameraConstants.MODE_REAR_OUTFOCUS) || FunctionProperties.isSupportedRearOutfocus()) {
                    initMode = CameraConstants.MODE_REAR_OUTFOCUS;
                    this.mSwapString = "rear";
                } else if (ConfigurationUtil.containsMode(CameraConstants.MODE_FRONT_OUTFOCUS) || FunctionProperties.isSupportedFrontOutfocus()) {
                    initMode = CameraConstants.MODE_FRONT_OUTFOCUS;
                    this.mSwapString = "front";
                }
            } else if (ConfigurationUtil.containsMode(CameraConstants.MODE_FRONT_OUTFOCUS) || FunctionProperties.isSupportedFrontOutfocus()) {
                initMode = CameraConstants.MODE_FRONT_OUTFOCUS;
                this.mSwapString = "front";
            } else if (ConfigurationUtil.containsMode(CameraConstants.MODE_REAR_OUTFOCUS) || FunctionProperties.isSupportedRearOutfocus()) {
                initMode = CameraConstants.MODE_REAR_OUTFOCUS;
                this.mSwapString = "rear";
            }
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_SLOWMOTION.equals(assistantShotMode) && ConfigurationUtil.containsMode(CameraConstants.MODE_SLOW_MOTION)) {
            initMode = CameraConstants.MODE_SLOW_MOTION;
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_TIMELAPS.equals(assistantShotMode) && ConfigurationUtil.containsMode(CameraConstants.MODE_TIME_LAPSE_VIDEO)) {
            if (!FunctionProperties.isSupportedModedownload()) {
                initMode = CameraConstants.MODE_TIME_LAPSE_VIDEO;
            } else if (checkDownloadedMode(ModeMenuManagerBase.MODE_DOWNLOAD_TIMELAPSE)) {
                initMode = CameraConstants.MODE_TIME_LAPSE_VIDEO;
            } else {
                showModeNotDownloadedToastMsg(CameraConstants.MODE_TIME_LAPSE_VIDEO);
            }
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_NIGHTVISION.equals(assistantShotMode) && FunctionProperties.isSupportedBinning(0)) {
            initMode = "mode_normal";
        }
        if (getBooleanFlag(CameraConstantsEx.FLAG_USE_REAR_CAMERA, false) && !CameraDeviceUtils.isRearCamera(cameraId)) {
            initMode = "mode_normal";
        } else if (getBooleanFlag(CameraConstantsEx.FLAG_USE_FRONT_CAMERA, false)) {
            if (FunctionProperties.isSupportedBeautyShot()) {
                initMode = CameraConstants.MODE_BEAUTY;
                this.mSwapString = "front";
            } else if (FunctionProperties.isSupportedGestureShot()) {
                initMode = CameraConstants.MODE_GESTURESHOT;
                this.mSwapString = "front";
            } else {
                initMode = "mode_normal";
            }
        }
        CamLog.m3d(CameraConstants.TAG, "-Voice Assistant- getAssistantInitMode, initMode = " + initMode);
        return initMode;
    }

    private void showModeNotDownloadedToastMsg(String mode) {
        String tmpToastMsg = null;
        if (CameraConstants.MODE_TIME_LAPSE_VIDEO.equals(mode)) {
            tmpToastMsg = this.mGet.getAppContext().getString(C0088R.string.assistant_mode_not_downloaded);
        }
        if (tmpToastMsg != null) {
            final String toastMsg = tmpToastMsg;
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (!VoiceAssistantManager.this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_FULL_SCREEN_GUIDE)) {
                        VoiceAssistantManager.this.mGet.showToast(toastMsg, CameraConstants.TOAST_LENGTH_SHORT);
                    }
                }
            }, 1000);
        }
    }

    public String getAssistantDummyCmdButtonLayout() {
        String layoutId = CameraConstants.START_CAMERA_NORMAL_SHUTTER_ZOOM_VIEW_DUMMY;
        String assistantShotMode = getStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null);
        if (CameraConstantsEx.FLAG_VALUE_MODE_CINEMA.equals(assistantShotMode) && FunctionProperties.isCineVideoAvailable(this.mGet.getAppContext())) {
            layoutId = CameraConstants.START_CAMERA_MANUAL_VIDEO_VIEW_DUMMY;
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_MANUAL.equals(assistantShotMode)) {
            if (this.mGet.isAssistantImageIntent() && ConfigurationUtil.containsMode(CameraConstants.MODE_MANUAL_CAMERA)) {
                layoutId = CameraConstants.START_CAMERA_MANUAL_CAMERA_VIEW_DUMMY;
            } else if (this.mGet.isAssistantVideoIntent() && ConfigurationUtil.containsMode(CameraConstants.MODE_MANUAL_VIDEO)) {
                layoutId = ManualUtil.isCinemaSize(this.mGet.getAppContext(), this.mGet.getCurSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE)) ? CameraConstants.START_CAMERA_MANUAL_VIDEO_CINEMA_VIEW_DUMMY : CameraConstants.START_CAMERA_MANUAL_VIDEO_VIEW_DUMMY;
            }
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_PANORAMA.equals(assistantShotMode) && (ConfigurationUtil.containsMode(CameraConstants.MODE_PANORAMA_LG) || ConfigurationUtil.containsMode(CameraConstants.MODE_PANORAMA_LG_RAW))) {
            layoutId = CameraConstants.START_CAMERA_MANUAL_CAMERA_VIEW_DUMMY;
        } else if (CameraConstantsEx.FLAG_VALUE_GRAPHY.equalsIgnoreCase(assistantShotMode) && ConfigurationUtil.containsMode(CameraConstants.MODE_MANUAL_CAMERA)) {
            layoutId = CameraConstants.START_CAMERA_MANUAL_CAMERA_VIEW_DUMMY;
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_FOOD.equalsIgnoreCase(assistantShotMode) && ConfigurationUtil.containsMode(assistantShotMode)) {
            layoutId = CameraConstants.START_CAMERA_NORMAL_SHUTTER_ZOOM_VIEW_DUMMY;
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_SLOWMOTION.equalsIgnoreCase(assistantShotMode) && ConfigurationUtil.containsMode(CameraConstants.MODE_SLOW_MOTION)) {
            layoutId = CameraConstants.START_CAMERA_MANUAL_VIDEO_VIEW_DUMMY;
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_TIMELAPS.equals(assistantShotMode) && ConfigurationUtil.containsMode(CameraConstants.MODE_TIME_LAPSE_VIDEO)) {
            if (!FunctionProperties.isSupportedModedownload()) {
                layoutId = CameraConstants.START_CAMERA_MANUAL_VIDEO_VIEW_DUMMY;
            } else if (checkDownloadedMode(ModeMenuManagerBase.MODE_DOWNLOAD_TIMELAPSE)) {
                layoutId = CameraConstants.START_CAMERA_MANUAL_VIDEO_VIEW_DUMMY;
            }
        }
        if (getBooleanFlag(CameraConstantsEx.FLAG_USE_FRONT_CAMERA, false)) {
            layoutId = CameraConstants.START_CAMERA_NORMAL_SHUTTER_ZOOM_VIEW_DUMMY;
        }
        CamLog.m3d(CameraConstants.TAG, "-Voice Assistant- getAssistantDummyCmdButtonLayout, layoutId = " + layoutId);
        return layoutId;
    }

    public String getAssistantShotMode() {
        String assistantMode = getStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null);
        String shotMode = "mode_normal";
        if (CameraConstantsEx.FLAG_VALUE_MODE_CINEMA.equals(assistantMode) && FunctionProperties.isCineVideoAvailable(this.mGet.getAppContext())) {
            shotMode = CameraConstants.MODE_CINEMA;
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_MANUAL.equals(assistantMode)) {
            if (this.mGet.isAssistantImageIntent() && ConfigurationUtil.containsMode(CameraConstants.MODE_MANUAL_CAMERA)) {
                shotMode = CameraConstants.MODE_MANUAL_CAMERA;
            } else if (this.mGet.isAssistantVideoIntent() && ConfigurationUtil.containsMode(CameraConstants.MODE_MANUAL_VIDEO)) {
                shotMode = CameraConstants.MODE_MANUAL_VIDEO;
            }
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_PANORAMA.equals(assistantMode)) {
            if (ConfigurationUtil.containsMode(CameraConstants.MODE_PANORAMA_LG)) {
                shotMode = CameraConstants.MODE_PANORAMA_LG;
            } else if (ConfigurationUtil.containsMode(CameraConstants.MODE_PANORAMA_LG_RAW)) {
                shotMode = CameraConstants.MODE_PANORAMA_LG_RAW;
            }
        } else if (CameraConstantsEx.FLAG_VALUE_GRAPHY.equalsIgnoreCase(assistantMode) && ConfigurationUtil.containsMode(CameraConstants.MODE_MANUAL_CAMERA)) {
            shotMode = CameraConstants.MODE_MANUAL_CAMERA;
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_FOOD.equals(assistantMode) && ConfigurationUtil.containsMode("mode_food")) {
            shotMode = "mode_food";
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_SLOWMOTION.equalsIgnoreCase(assistantMode) && ConfigurationUtil.containsMode(CameraConstants.MODE_SLOW_MOTION)) {
            shotMode = CameraConstants.MODE_SLOW_MOTION;
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_TIMELAPS.equals(assistantMode) && ConfigurationUtil.containsMode(CameraConstants.MODE_TIME_LAPSE_VIDEO)) {
            if (!FunctionProperties.isSupportedModedownload()) {
                shotMode = CameraConstants.MODE_TIME_LAPSE_VIDEO;
            } else if (checkDownloadedMode(ModeMenuManagerBase.MODE_DOWNLOAD_TIMELAPSE)) {
                shotMode = CameraConstants.MODE_TIME_LAPSE_VIDEO;
            }
        }
        if (getBooleanFlag(CameraConstantsEx.FLAG_USE_FRONT_CAMERA, false)) {
            shotMode = "mode_normal";
        } else if (getBooleanFlag(CameraConstantsEx.FLAG_USE_REAR_CAMERA, false)) {
            shotMode = "mode_normal";
        }
        CamLog.m3d(CameraConstants.TAG, "-Voice Assistant- getAssistantShotMode, shotMode = " + shotMode);
        return shotMode;
    }

    public String getAssistantSwapString() {
        CamLog.m3d(CameraConstants.TAG, "-Voice Assistant- getAssistantSwapString, mSwapString = " + this.mSwapString);
        return this.mSwapString;
    }

    public void handleVoiceAssistantOnNewIntent() {
        if (this.mFlagState != null) {
            CamLog.m3d(CameraConstants.TAG, "-Voice Assistant- handleVoiceAssistantOnNewIntent");
            boolean isFrontWideSpecified = false;
            boolean isRearWideSpecified = false;
            String assistantMode = getStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, "");
            if (assistantMode.contains(CameraConstantsEx.FLAG_VALUE_WIDE_CAMERA)) {
                if (FunctionProperties.getCameraTypeRear() == 1) {
                    isRearWideSpecified = true;
                }
                if (FunctionProperties.getCameraTypeFront() == 2) {
                    isFrontWideSpecified = true;
                }
            }
            int savedCameraId = SharedPreferenceUtil.getCameraId(this.mGet.getAppContext());
            int cameraId = savedCameraId;
            String mode = "mode_normal";
            String swapString = "rear";
            if (!CameraDeviceUtils.isRearCamera(cameraId)) {
                if (FunctionProperties.isSupportedBeautyShot()) {
                    swapString = "front";
                } else if (FunctionProperties.isSupportedGestureShot()) {
                    swapString = "front";
                }
            }
            if (CameraConstantsEx.FLAG_VALUE_MODE_CINEMA.equals(assistantMode) && FunctionProperties.isCineVideoAvailable(this.mGet.getAppContext())) {
                cameraId = 0;
                mode = CameraConstants.MODE_CINEMA;
                swapString = "rear";
            } else if (CameraConstantsEx.FLAG_VALUE_MODE_MANUAL.equals(assistantMode) && ConfigurationUtil.containsMode(CameraConstants.MODE_MANUAL_CAMERA)) {
                if (this.mGet.isAssistantImageIntent()) {
                    cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
                    mode = CameraConstants.MODE_MANUAL_CAMERA;
                    swapString = "rear";
                } else {
                    cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
                    mode = CameraConstants.MODE_MANUAL_VIDEO;
                    swapString = "rear";
                }
            } else if (CameraConstantsEx.FLAG_VALUE_MODE_PANORAMA.equals(assistantMode)) {
                cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
                if (ConfigurationUtil.containsMode(CameraConstants.MODE_PANORAMA_LG)) {
                    mode = CameraConstants.MODE_PANORAMA_LG;
                } else {
                    mode = CameraConstants.MODE_PANORAMA_LG_RAW;
                }
                swapString = "rear";
            } else if (assistantMode.contains(CameraConstantsEx.FLAG_VALUE_WIDE_CAMERA)) {
                if (CameraDeviceUtils.isRearCamera(cameraId) && isRearWideSpecified) {
                    cameraId = 2;
                    SharedPreferenceUtil.saveRearCameraId(this.mGet.getAppContext(), 2);
                } else if (!CameraDeviceUtils.isRearCamera(cameraId) && isFrontWideSpecified) {
                    cameraId = 1;
                    SharedPreferenceUtil.saveCropAngleButtonId(this.mGet.getAppContext(), 1);
                }
            } else if (CameraConstantsEx.FLAG_VALUE_GRAPHY.equalsIgnoreCase(assistantMode) && ConfigurationUtil.containsMode(CameraConstants.MODE_MANUAL_CAMERA)) {
                cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
                mode = CameraConstants.MODE_MANUAL_CAMERA;
                swapString = "rear";
            } else if (CameraConstantsEx.FLAG_VALUE_MODE_FOOD.equalsIgnoreCase(assistantMode) && ConfigurationUtil.containsMode("mode_food")) {
                cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
                mode = "mode_food";
                swapString = "rear";
            } else if (CameraConstantsEx.FLAG_VALUE_MODE_SLOWMOTION.equalsIgnoreCase(assistantMode) && ConfigurationUtil.containsMode(CameraConstants.MODE_SLOW_MOTION)) {
                cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
                mode = CameraConstants.MODE_SLOW_MOTION;
                swapString = "rear";
            } else if (CameraConstantsEx.FLAG_VALUE_MODE_NIGHTVISION.equalsIgnoreCase(assistantMode) && FunctionProperties.isSupportedBinning(0)) {
                cameraId = 0;
                mode = "mode_normal";
                swapString = "rear";
            } else if (CameraConstantsEx.FLAG_VALUE_MODE_TIMELAPS.equals(assistantMode) && ConfigurationUtil.containsMode(CameraConstants.MODE_TIME_LAPSE_VIDEO)) {
                if (!FunctionProperties.isSupportedModedownload()) {
                    cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
                    mode = CameraConstants.MODE_TIME_LAPSE_VIDEO;
                    swapString = "rear";
                } else if (checkDownloadedMode(ModeMenuManagerBase.MODE_DOWNLOAD_TIMELAPSE)) {
                    cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
                    mode = CameraConstants.MODE_TIME_LAPSE_VIDEO;
                    swapString = "rear";
                }
            } else if (CameraConstantsEx.FLAG_VALUE_MODE_OUTFOCUS.equalsIgnoreCase(assistantMode)) {
                String shotMode = this.mGet.getShotMode();
                if (CameraConstants.MODE_REAR_OUTFOCUS.equals(shotMode)) {
                    cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
                    mode = CameraConstants.MODE_REAR_OUTFOCUS;
                    swapString = "rear";
                } else if (CameraConstants.MODE_FRONT_OUTFOCUS.equals(shotMode)) {
                    cameraId = 1;
                    mode = CameraConstants.MODE_FRONT_OUTFOCUS;
                    swapString = "front";
                }
            }
            if (getBooleanFlag(CameraConstantsEx.FLAG_USE_FRONT_CAMERA, false)) {
                mode = "mode_normal";
                swapString = "front";
                cameraId = SharedPreferenceUtil.getFrontCameraId(this.mGet.getAppContext());
                if (isFrontWideSpecified) {
                    SharedPreferenceUtil.saveCropAngleButtonId(this.mGet.getAppContext(), 1);
                }
            } else if (getBooleanFlag(CameraConstantsEx.FLAG_USE_REAR_CAMERA, false)) {
                mode = "mode_normal";
                swapString = "rear";
                if (isRearWideSpecified) {
                    cameraId = 2;
                } else {
                    cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
                }
                SharedPreferenceUtil.saveRearCameraId(this.mGet.getAppContext(), cameraId);
            }
            SharedPreferenceUtilBase.setCameraId(this.mGet.getAppContext(), cameraId);
            if (isFrontRearChanged(savedCameraId, cameraId)) {
                this.mGet.setupSetting();
            }
            this.mGet.setSetting(Setting.KEY_MODE, mode, true);
            this.mGet.setSetting(Setting.KEY_SWAP_CAMERA, swapString, true);
        }
    }

    public void setSettingByAssistant() {
        if (isAssistantSpecified()) {
            String shotMode = getAssistantShotMode();
            if (!(shotMode == null || shotMode.equals(this.mGet.getCurSettingValue(Setting.KEY_MODE)))) {
                this.mGet.setSetting(Setting.KEY_MODE, shotMode, true);
            }
            String swapString = getAssistantSwapString();
            if (swapString != null && !swapString.equals(this.mGet.getCurSettingValue(Setting.KEY_SWAP_CAMERA))) {
                this.mGet.setSetting(Setting.KEY_SWAP_CAMERA, swapString, true);
            }
        }
    }

    public boolean isFrontRearChanged(int savedCameraId, int currentCameraId) {
        if ((!CameraDeviceUtils.isRearCamera(savedCameraId) || CameraDeviceUtils.isRearCamera(currentCameraId)) && (CameraDeviceUtils.isRearCamera(savedCameraId) || !CameraDeviceUtils.isRearCamera(currentCameraId))) {
            return false;
        }
        return true;
    }

    public int checkCameraId(int cameraId) {
        if (!isAssistantSpecified()) {
            return cameraId;
        }
        int savedCameraId = SharedPreferenceUtil.getCameraId(this.mGet.getAppContext());
        String cameraMode = getStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null);
        if (cameraMode == null) {
            return cameraId;
        }
        boolean isFrontWideSpecified = false;
        boolean isRearWideSpecified = false;
        if (cameraMode.contains(CameraConstantsEx.FLAG_VALUE_WIDE_CAMERA)) {
            if (FunctionProperties.getCameraTypeRear() == 1) {
                isRearWideSpecified = true;
            }
            if (FunctionProperties.getCameraTypeFront() == 2) {
                isFrontWideSpecified = true;
            }
        }
        if (getBooleanFlag(CameraConstantsEx.FLAG_USE_FRONT_CAMERA, false)) {
            cameraId = SharedPreferenceUtil.getFrontCameraId(this.mGet.getAppContext());
            SharedPreferenceUtilBase.setCameraId(this.mGet.getAppContext(), cameraId);
            if (isFrontWideSpecified) {
                SharedPreferenceUtil.saveCropAngleButtonId(this.mGet.getAppContext(), 1);
            }
        } else if (getBooleanFlag(CameraConstantsEx.FLAG_USE_REAR_CAMERA, false)) {
            if (isRearWideSpecified) {
                cameraId = 2;
            } else {
                cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
            }
            SharedPreferenceUtilBase.setCameraId(this.mGet.getAppContext(), cameraId);
            SharedPreferenceUtil.saveRearCameraId(this.mGet.getAppContext(), cameraId);
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_CINEMA.equals(cameraMode) && FunctionProperties.isCineVideoAvailable(this.mGet.getAppContext())) {
            SharedPreferenceUtilBase.setCameraId(this.mGet.getAppContext(), 0);
            SharedPreferenceUtil.saveRearCameraId(this.mGet.getAppContext(), 0);
            return 0;
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_MANUAL.equals(cameraMode) && (ConfigurationUtil.containsMode(CameraConstants.MODE_MANUAL_CAMERA) || ConfigurationUtil.containsMode(CameraConstants.MODE_MANUAL_VIDEO))) {
            if (isRearWideSpecified) {
                cameraId = 2;
            } else {
                cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
            }
            SharedPreferenceUtilBase.setCameraId(this.mGet.getAppContext(), cameraId);
            if (this.mGet.isAssistantVideoIntent()) {
                return -1;
            }
        } else if (CameraConstantsEx.FLAG_VALUE_GRAPHY.equals(cameraMode) && FunctionProperties.isSupportedGraphy()) {
            cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
            SharedPreferenceUtilBase.setCameraId(this.mGet.getAppContext(), cameraId);
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_PANORAMA.equals(cameraMode) && (ConfigurationUtil.containsMode(CameraConstants.MODE_PANORAMA_LG) || ConfigurationUtil.containsMode(CameraConstants.MODE_PANORAMA_LG_RAW))) {
            if (isRearWideSpecified) {
                cameraId = 2;
            } else {
                cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
            }
            SharedPreferenceUtilBase.setCameraId(this.mGet.getAppContext(), cameraId);
            return cameraId;
        } else if (cameraMode.contains(CameraConstantsEx.FLAG_VALUE_WIDE_CAMERA)) {
            if (CameraDeviceUtils.isRearCamera(cameraId) && isRearWideSpecified) {
                cameraId = 2;
                SharedPreferenceUtil.saveRearCameraId(this.mGet.getAppContext(), 2);
                SharedPreferenceUtilBase.setCameraId(this.mGet.getAppContext(), 2);
            } else if (!CameraDeviceUtils.isRearCamera(cameraId) && isFrontWideSpecified) {
                SharedPreferenceUtil.saveCropAngleButtonId(this.mGet.getAppContext(), 1);
            }
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_FOOD.equals(cameraMode) && ConfigurationUtil.containsMode("mode_food")) {
            if (isRearWideSpecified) {
                cameraId = 2;
            } else {
                cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
            }
            SharedPreferenceUtilBase.setCameraId(this.mGet.getAppContext(), cameraId);
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_SLOWMOTION.equals(cameraMode) && ConfigurationUtil.containsMode(CameraConstants.MODE_SLOW_MOTION)) {
            if (isRearWideSpecified) {
                cameraId = 2;
            } else {
                cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
            }
            SharedPreferenceUtilBase.setCameraId(this.mGet.getAppContext(), cameraId);
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_TIMELAPS.equals(cameraMode) && ConfigurationUtil.containsMode(CameraConstants.MODE_TIME_LAPSE_VIDEO)) {
            if (!FunctionProperties.isSupportedModedownload()) {
                if (isRearWideSpecified) {
                    cameraId = 2;
                } else {
                    cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
                }
                SharedPreferenceUtilBase.setCameraId(this.mGet.getAppContext(), cameraId);
            } else if (checkDownloadedMode(ModeMenuManagerBase.MODE_DOWNLOAD_TIMELAPSE)) {
                if (isRearWideSpecified) {
                    cameraId = 2;
                } else {
                    cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
                }
                SharedPreferenceUtilBase.setCameraId(this.mGet.getAppContext(), cameraId);
            }
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_NIGHTVISION.equalsIgnoreCase(cameraMode)) {
            if (FunctionProperties.isSupportedBinning()) {
                cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
            } else if (ConfigurationUtil.sBINNING_SUPPORTED == 2 || ConfigurationUtil.sBINNING_SUPPORTED == 6) {
                cameraId = 2;
            } else if (ConfigurationUtil.sBINNING_SUPPORTED == 1 || ConfigurationUtil.sBINNING_SUPPORTED == 5) {
                cameraId = 0;
            }
            SharedPreferenceUtilBase.setCameraId(this.mGet.getAppContext(), cameraId);
        } else if (CameraConstantsEx.FLAG_VALUE_MODE_OUTFOCUS.equals(cameraMode)) {
            boolean isRear = CameraDeviceUtils.isRearCamera(cameraId);
            if (isRear && FunctionProperties.isSupportedFrontOutfocus() && !FunctionProperties.isSupportedRearOutfocus()) {
                cameraId = 1;
                SharedPreferenceUtilBase.setCameraId(this.mGet.getAppContext(), 1);
                if (isFrontWideSpecified) {
                    SharedPreferenceUtil.saveCropAngleButtonId(this.mGet.getAppContext(), 1);
                }
            } else if (!(isRear || FunctionProperties.isSupportedFrontOutfocus() || !FunctionProperties.isSupportedRearOutfocus())) {
                if (isRearWideSpecified) {
                    cameraId = 2;
                } else {
                    cameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
                }
                SharedPreferenceUtilBase.setCameraId(this.mGet.getAppContext(), cameraId);
            }
        }
        if (isFrontRearChanged(savedCameraId, cameraId)) {
            this.mGet.setupSetting();
        }
        CamLog.m3d(CameraConstants.TAG, String.format("-Voice Assistant- checkCameraId, savedCameraId = %d, newCameraId = %d", new Object[]{Integer.valueOf(savedCameraId), Integer.valueOf(cameraId)}));
        return cameraId;
    }

    public void onStop() {
        clearAllFlags();
    }

    public void clearAllFlags() {
        if (this.mFlagState != null) {
            this.mFlagState.clear();
            this.mFlagState = null;
        }
    }

    private boolean checkDownloadedMode(String modeName) {
        File dir = new File(ModeMenuManagerBase.MODE_DOWNLOAD_PATH);
        if (dir == null || !dir.exists()) {
            return false;
        }
        File[] fileList = dir.listFiles();
        if (fileList == null) {
            return false;
        }
        for (File file : fileList) {
            if (file != null) {
                String fileName = file.getName();
                if (fileName != null && fileName.contains(modeName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }
}
