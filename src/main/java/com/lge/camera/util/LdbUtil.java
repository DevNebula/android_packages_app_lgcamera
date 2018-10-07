package com.lge.camera.util;

import android.content.Context;
import android.content.Intent;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.settings.Setting;
import java.util.HashMap;

public class LdbUtil {
    private static HashMap<String, Integer> sCategoryMap = new HashMap();
    private static HashMap<String, String> sFeatureNameMap = new HashMap();
    private static HashMap<String, String> sFilterMap = new HashMap();
    private static int sMultiShotState = 0;
    private static boolean sMultiViewInterval = false;
    private static int sSceneMode = 0;
    private static String sShutterType = "";
    private static boolean sSkipNextLdbBroadcast = false;

    public static void makeLdbCategoryMap() {
        if (sCategoryMap == null || sCategoryMap.size() <= 0) {
            CamLog.m3d(CameraConstants.TAG, "makeCategoryMap [START]");
            if (sCategoryMap != null) {
                sCategoryMap.put("hdr-mode", Integer.valueOf(3));
                sCategoryMap.put(Setting.KEY_VIDEO_STEADY, Integer.valueOf(10));
                sCategoryMap.put(Setting.KEY_LIVE_PHOTO, Integer.valueOf(1));
                sCategoryMap.put(Setting.KEY_TILE_PREVIEW, Integer.valueOf(1));
                sCategoryMap.put("tracking-af", Integer.valueOf(15));
                sCategoryMap.put(Setting.KEY_FINGER_DETECTION, Integer.valueOf(3));
                sCategoryMap.put(Setting.KEY_FRAME_GRID, Integer.valueOf(11));
                sCategoryMap.put(Setting.KEY_SIGNATURE, Integer.valueOf(5));
                sCategoryMap.put(Setting.KEY_STORAGE, Integer.valueOf(15));
                sCategoryMap.put(Setting.KEY_SHUTTERLESS_SELFIE, Integer.valueOf(1));
                sCategoryMap.put(Setting.KEY_SAVE_DIRECTION, Integer.valueOf(1));
                sCategoryMap.put(Setting.KEY_MOTION_QUICKVIEWER, Integer.valueOf(1));
                sCategoryMap.put(Setting.KEY_SAVE_DIRECTION, Integer.valueOf(15));
                sCategoryMap.put(Setting.KEY_FILM_EMULATOR, Integer.valueOf(15));
                sCategoryMap.put("flash-mode", Integer.valueOf(15));
                sCategoryMap.put(Setting.KEY_LIGHTFRAME, Integer.valueOf(1));
                sCategoryMap.put(Setting.KEY_BEAUTYSHOT, Integer.valueOf(3));
                sCategoryMap.put(Setting.KEY_RELIGHTING, Integer.valueOf(3));
                sCategoryMap.put(Setting.KEY_RAW_PICTURE, Integer.valueOf(4));
                sCategoryMap.put(Setting.KEY_MANUAL_NOISE_REDUCTION, Integer.valueOf(4));
                sCategoryMap.put(Setting.KEY_INCLINOMETER, Integer.valueOf(4));
                sCategoryMap.put("lg-wb", Integer.valueOf(4));
                sCategoryMap.put(Setting.KEY_MANUAL_FOCUS_STEP, Integer.valueOf(4));
                sCategoryMap.put(Setting.KEY_LG_EV_CTRL, Integer.valueOf(4));
                sCategoryMap.put(Setting.KEY_LG_MANUAL_ISO, Integer.valueOf(4));
                sCategoryMap.put("shutter-speed", Integer.valueOf(4));
                sCategoryMap.put(Setting.KEY_MANUAL_VIDEO_FRAME_RATE, Integer.valueOf(8));
                sCategoryMap.put(Setting.KEY_MANUAL_VIDEO_BITRATE, Integer.valueOf(8));
                sCategoryMap.put(Setting.KEY_MANUAL_VIDEO_AUDIO, Integer.valueOf(8));
                sCategoryMap.put(Setting.KEY_MANUAL_VIDEO_LOG, Integer.valueOf(8));
                sCategoryMap.put(Setting.KEY_VIDEO_LG_WB, Integer.valueOf(8));
                sCategoryMap.put(Setting.KEY_VIDEO_MANUAL_FOCUS_STEP, Integer.valueOf(8));
                sCategoryMap.put(Setting.KEY_VIDEO_LG_EV_CTRL, Integer.valueOf(8));
                sCategoryMap.put(Setting.KEY_VIDEO_LG_MANUAL_ISO, Integer.valueOf(8));
                sCategoryMap.put(Setting.KEY_VIDEO_SHUTTER_SPEED, Integer.valueOf(8));
                sCategoryMap.put(Setting.KEY_HDR10, Integer.valueOf(8));
            }
            CamLog.m3d(CameraConstants.TAG, "makeCategoryMap [END]");
        }
    }

    public static void makeFeatureNameMap() {
        if (sFeatureNameMap == null || sFeatureNameMap.size() <= 0) {
            CamLog.m3d(CameraConstants.TAG, "makeFeatureNameMap [START]");
            if (sFeatureNameMap != null) {
                sFeatureNameMap.put("hdr-mode", "hdr");
                sFeatureNameMap.put(Setting.KEY_VIDEO_STEADY, LdbConstants.LDB_FEAT_NAME_VIDEO_STEADY);
                sFeatureNameMap.put(Setting.KEY_LIVE_PHOTO, LdbConstants.LDB_FEAT_NAME_LIVEPHOTO);
                sFeatureNameMap.put(Setting.KEY_TILE_PREVIEW, LdbConstants.LDB_FEAT_NAME_TILE_PREVIEW);
                sFeatureNameMap.put("tracking-af", "tracking-af");
                sFeatureNameMap.put(Setting.KEY_FINGER_DETECTION, LdbConstants.LDB_FEAT_NAME_FINGER_DETECTION);
                sFeatureNameMap.put(Setting.KEY_FRAME_GRID, "grid");
                sFeatureNameMap.put(Setting.KEY_SIGNATURE, LdbConstants.LDB_FEAT_NAME_SIGNATURE);
                sFeatureNameMap.put(Setting.KEY_STORAGE, LdbConstants.LDB_FEAT_NAME_STORAGE);
                sFeatureNameMap.put(Setting.KEY_SHUTTERLESS_SELFIE, LdbConstants.LDB_FEAT_NAME_SHUTTERLESS);
                sFeatureNameMap.put(Setting.KEY_SAVE_DIRECTION, LdbConstants.LDB_FEAT_NAME_SAVE_DIRECTION);
                sFeatureNameMap.put(Setting.KEY_MOTION_QUICKVIEWER, LdbConstants.LDB_FEAT_NAME_QUICKVIEW);
                sFeatureNameMap.put(Setting.KEY_FILM_EMULATOR, LdbConstants.LDB_FEAT_NAME_FILM_EMULATOR);
                sFeatureNameMap.put("flash-mode", LdbConstants.LDB_FEAT_NAME_FLASH);
                sFeatureNameMap.put(Setting.KEY_LIGHTFRAME, LdbConstants.LDB_FEAT_NAME_LIGHTFRAME);
                sFeatureNameMap.put(Setting.KEY_BEAUTYSHOT, "beautyshot");
                sFeatureNameMap.put(Setting.KEY_RELIGHTING, LdbConstants.LDB_FEAT_NAME_RELIGHTING);
                sFeatureNameMap.put(Setting.KEY_RAW_PICTURE, LdbConstants.LDB_FEAT_NAME_RAW_PICTURE);
                sFeatureNameMap.put(Setting.KEY_MANUAL_NOISE_REDUCTION, LdbConstants.LDB_FEAT_NAME_NOISE_REDUCTION);
                sFeatureNameMap.put(Setting.KEY_INCLINOMETER, LdbConstants.LDB_FEAT_NAME_INCLINOMETER);
                sFeatureNameMap.put("lg-wb", LdbConstants.LDB_FEAT_NAME_LG_WB);
                sFeatureNameMap.put(Setting.KEY_MANUAL_FOCUS_STEP, LdbConstants.LDB_FEAT_NAME_MANUAL_FOCUS_STEP);
                sFeatureNameMap.put(Setting.KEY_LG_EV_CTRL, LdbConstants.LDB_FEAT_NAME_LG_EV);
                sFeatureNameMap.put(Setting.KEY_LG_MANUAL_ISO, "iso");
                sFeatureNameMap.put("shutter-speed", LdbConstants.LDB_FEAT_NAME_SHUTTER_SPEED);
                sFeatureNameMap.put(Setting.KEY_MANUAL_VIDEO_FRAME_RATE, LdbConstants.LDB_FEAT_NAME_VIDEO_FRAME_RATE);
                sFeatureNameMap.put(Setting.KEY_MANUAL_VIDEO_BITRATE, LdbConstants.LDB_FEAT_NAME_VIDEO_BITRATE);
                sFeatureNameMap.put(Setting.KEY_MANUAL_VIDEO_AUDIO, LdbConstants.LDB_FEAT_NAME_VIDE_AUDIO);
                sFeatureNameMap.put(Setting.KEY_MANUAL_VIDEO_LOG, LdbConstants.LDB_FEAT_NAME_VIDEO_LOG);
                sFeatureNameMap.put(Setting.KEY_VIDEO_LG_WB, LdbConstants.LDB_FEAT_NAME_VIDEO_LG_WB);
                sFeatureNameMap.put(Setting.KEY_VIDEO_MANUAL_FOCUS_STEP, LdbConstants.LDB_FEAT_NAME_VIDEO_MANUAL_FOCUS_STEP);
                sFeatureNameMap.put(Setting.KEY_VIDEO_LG_EV_CTRL, LdbConstants.LDB_FEAT_NAME_VIDEO_LG_EV);
                sFeatureNameMap.put(Setting.KEY_VIDEO_LG_MANUAL_ISO, LdbConstants.LDB_FEAT_NAME_VIDEO_ISO);
                sFeatureNameMap.put(Setting.KEY_VIDEO_SHUTTER_SPEED, LdbConstants.LDB_FEAT_NAME_VIDEO_SHUTTER_SPEED);
                sFeatureNameMap.put(Setting.KEY_HDR10, "hdr10");
            }
            CamLog.m3d(CameraConstants.TAG, "makeFeatureNameMap [END]");
        }
    }

    public static String getLDBFeatureName(String key) {
        return sFeatureNameMap == null ? null : (String) sFeatureNameMap.get(key);
    }

    public static void makeFilterMap(Context c) {
        if (sFilterMap == null || sFilterMap.size() <= 0) {
            CamLog.m3d(CameraConstants.TAG, "makeFilterMap [START]");
            if (sFilterMap != null) {
                String[] key = c.getResources().getStringArray(C0088R.array.advanced_selfie_filter_entriyValues);
                String[] value = c.getResources().getStringArray(C0088R.array.ldb_filter_name);
                for (int i = 0; i < key.length; i++) {
                    sFilterMap.put(key[i], value[i]);
                }
            }
            CamLog.m3d(CameraConstants.TAG, "makeFilterMap [END]");
        }
    }

    public static String getLDBFilterName(String key) {
        if (sFilterMap != null) {
            return (String) sFilterMap.get(key);
        }
        return null;
    }

    public static int getCategory(String settingKey) {
        if (sCategoryMap != null) {
            Integer intValue = (Integer) sCategoryMap.get(settingKey);
            if (intValue != null) {
                return intValue.intValue();
            }
        }
        return 0;
    }

    public static void setShutterType(String shutterType) {
        sShutterType = shutterType;
    }

    public static String getShutterType() {
        return sShutterType;
    }

    public static void setSceneMode(int sceneMode) {
        sSceneMode = sceneMode;
    }

    public static int getSceneMode() {
        return sSceneMode;
    }

    public static void setMultiShotState(int state) {
        sMultiShotState = state;
    }

    public static void setSkipNextLdbBroadcast(boolean skip) {
        CamLog.m3d(CameraConstants.TAG, "setSkipNextLdbBroadcast : " + skip);
        sSkipNextLdbBroadcast = skip;
    }

    public static boolean skipNextLdbBroadcast() {
        return sSkipNextLdbBroadcast;
    }

    public static String getMultiShotState() {
        String multiShotState = LdbConstants.LDB_MULTIVIEW_INTERVAL_MODE_SINGLE;
        switch (sMultiShotState) {
            case 0:
                return LdbConstants.LDB_MULTIVIEW_INTERVAL_MODE_SINGLE;
            case 1:
                return "Burst";
            case 2:
                return LdbConstants.LDB_MULTIVIEW_INTERVAL_MODE_INTERVAL;
            case 4:
                return "Shot2Shot";
            default:
                return multiShotState;
        }
    }

    public static void setMultiViewIntervalMode(boolean intervalMode) {
        sMultiViewInterval = intervalMode;
    }

    public static boolean isMultiViewIntervalMode() {
        return sMultiViewInterval;
    }

    public static void unbind() {
        if (sCategoryMap != null) {
            sCategoryMap.clear();
        }
        sShutterType = "";
        sMultiShotState = 0;
        sSceneMode = 0;
        sSkipNextLdbBroadcast = false;
        sMultiViewInterval = false;
    }

    public static void sendLDBIntentForSwapCamera(Context context, boolean isRearCamera, String type) {
        int cameraId;
        if (FunctionProperties.getCameraTypeFront() == 1) {
            cameraId = isRearCamera ? SharedPreferenceUtil.getFrontCameraId(context) : 0;
        } else if (FunctionProperties.getCameraTypeRear() == 1) {
            cameraId = isRearCamera ? 1 : SharedPreferenceUtil.getRearCameraId(context);
        } else {
            cameraId = isRearCamera ? 1 : 0;
        }
        sendLDBIntent(context, LdbConstants.LDB_FEATURE_NAME_SWAP_CAMERA, cameraId, type);
    }

    public static void sendLDBIntent(Context context, String feaure_name) {
        sendLDBIntent(context, feaure_name, -1, null);
    }

    public static void sendLDBIntent(Context context, String feature_name, int extend_integer, String extend_text) {
        CamLog.m3d(CameraConstants.TAG, "[L-DB TASK] feat name : " + feature_name + " , extra int : " + extend_integer + " , extra txt : " + extend_text);
        Intent i = new Intent("com.lge.mlt.service.intent.action.APPEND_USER_LOG");
        i.setPackage("com.lge.mlt");
        i.putExtra("pkg_name", "com.lge.camera");
        i.putExtra("app_name", "camera");
        i.putExtra("feature_name", feature_name);
        if (extend_integer != -1) {
            i.putExtra("extend_integer", extend_integer);
        }
        if (extend_text != null) {
            i.putExtra("extend_text", extend_text);
        }
        context.startService(i);
    }

    public static String getLDBCameraId(Context c, boolean isRearCamera) {
        String ldbCameraId = "";
        if (FunctionProperties.getCameraTypeRear() == 1) {
            return getCameraIdForRearTwoCameraModel(c, isRearCamera);
        }
        if (FunctionProperties.getCameraTypeFront() == 1) {
            return getCameraIdForFrontTwoCameraModel(c, isRearCamera);
        }
        if (FunctionProperties.getCameraTypeFront() == 2) {
            return getCameraIdForFrontWideCameraModel(c, isRearCamera);
        }
        return isRearCamera ? LdbConstants.LDB_CAMERA_ID_REAR_NORMAL : LdbConstants.LDB_CAMERA_ID_FRONT_NORMAL;
    }

    private static String getCameraIdForRearTwoCameraModel(Context c, boolean isRearCamera) {
        String ldbCameraId = "";
        if (isRearCamera) {
            return SharedPreferenceUtil.getRearCameraId(c) == 0 ? LdbConstants.LDB_CAMERA_ID_REAR_NORMAL : LdbConstants.LDB_CAMERA_ID_REAR_WIDE;
        } else {
            if (SharedPreferenceUtil.getCropAngleButtonId(c) == 0) {
                return LdbConstants.LDB_CAMERA_ID_FRONT_NORMAL;
            }
            return LdbConstants.LDB_CAMERA_ID_FRONT_WIDE;
        }
    }

    private static String getCameraIdForFrontTwoCameraModel(Context c, boolean isRearCamera) {
        String ldbCameraId = "";
        if (isRearCamera) {
            return LdbConstants.LDB_CAMERA_ID_REAR_NORMAL;
        }
        if (SharedPreferenceUtil.getCropAngleButtonId(c) == 0) {
            return LdbConstants.LDB_CAMERA_ID_FRONT_NORMAL;
        }
        return LdbConstants.LDB_CAMERA_ID_FRONT_WIDE;
    }

    private static String getCameraIdForFrontWideCameraModel(Context c, boolean isRearCamera) {
        String ldbCameraId = "";
        if (isRearCamera) {
            return LdbConstants.LDB_CAMERA_ID_REAR_NORMAL;
        }
        if (SharedPreferenceUtil.getCropAngleButtonId(c) == 0) {
            return LdbConstants.LDB_CAMERA_ID_FRONT_NORMAL;
        }
        return LdbConstants.LDB_CAMERA_ID_FRONT_WIDE;
    }
}
