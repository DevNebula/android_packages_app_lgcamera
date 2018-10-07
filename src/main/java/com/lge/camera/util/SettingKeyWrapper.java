package com.lge.camera.util;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.settings.Setting;
import java.util.HashMap;

public class SettingKeyWrapper {
    static final HashMap<String, String> sManualVideoMap = new HashMap();

    static {
        sManualVideoMap.put("lg-wb", Setting.KEY_VIDEO_LG_WB);
        sManualVideoMap.put(Setting.KEY_MANUAL_FOCUS_STEP, Setting.KEY_VIDEO_MANUAL_FOCUS_STEP);
        sManualVideoMap.put(Setting.KEY_LG_EV_CTRL, Setting.KEY_VIDEO_LG_EV_CTRL);
        sManualVideoMap.put(Setting.KEY_LG_MANUAL_ISO, Setting.KEY_VIDEO_LG_MANUAL_ISO);
        sManualVideoMap.put("shutter-speed", Setting.KEY_VIDEO_SHUTTER_SPEED);
    }

    public static String getPictureSizeKey(String shotMode, int cameraId) {
        if (shotMode.contains(CameraConstants.MODE_SQUARE) && !shotMode.equals(CameraConstants.MODE_SQUARE_SPLICE)) {
            return Setting.KEY_SQUARE_PICTURE_SIZE;
        }
        String settingKey = "picture-size";
        if (FunctionProperties.getCameraTypeRear() == 1 && cameraId == 2) {
            return Setting.KEY_CAMERA_PICTURESIZE_SUB;
        }
        return settingKey;
    }

    public static String getVideoSizeKey(String shotMode, int cameraId) {
        if (shotMode.contains(CameraConstants.MODE_SQUARE) && !shotMode.equals(CameraConstants.MODE_SQUARE_SPLICE)) {
            return Setting.KEY_SQUARE_VIDEO_SIZE;
        }
        if (CameraConstants.MODE_CINEMA.equals(shotMode)) {
            return Setting.KEY_VIDEO_RECORDSIZE;
        }
        if (CameraConstants.MODE_SLOW_MOTION.equals(shotMode) && FunctionProperties.enableSlowMotionVideoSizeMenu()) {
            return Setting.KEY_SLOW_MOTION_VIDEO_SIZE;
        }
        String settingKey = Setting.KEY_VIDEO_RECORDSIZE;
        if (FunctionProperties.getCameraTypeRear() == 1 && cameraId == 2) {
            return Setting.KEY_VIDEO_RECORDSIZE_SUB;
        }
        return settingKey;
    }

    public static String getManualSettingKey(String shotwMode, String key) {
        if (CameraConstants.MODE_MANUAL_CAMERA.equals(shotwMode)) {
            return key;
        }
        if (ManualUtil.isManualVideoMode(shotwMode)) {
            return (String) sManualVideoMap.get(key);
        }
        return null;
    }

    public static String getPictureSizeBackupKey(int cameraId) {
        switch (cameraId) {
            case 0:
            case 2:
                return CameraConstantsEx.KEY_PICTURESIZE_BACKUP_REAR;
            case 1:
                return CameraConstantsEx.KEY_PICTURESIZE_BACKUP_FRONT;
            default:
                return CameraConstantsEx.KEY_PICTURESIZE_BACKUP_REAR;
        }
    }
}
