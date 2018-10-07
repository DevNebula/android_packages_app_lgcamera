package com.lge.camera.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.Setting;

public class SharedPreferenceUtil extends SharedPreferenceUtilBase {
    private static final int ERROR_VALUE = -1;

    public static void saveAccumulatedMediaCount(Context c, int storage, long count) {
        String storageString = StorageUtil.convertStorageTypeToName(storage);
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putLong(String.format("media_number_%s", new Object[]{storageString}), count);
        editor.apply();
        CamLog.m7i(CameraConstants.TAG, "saved media counter = " + count);
    }

    public static long getAccumulatedMediaCount(Context c, int storage) {
        String storageString = StorageUtil.convertStorageTypeToName(storage);
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getLong(String.format("media_number_%s", new Object[]{storageString}), 0);
    }

    public static long getAccumulatedDCFCount(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getLong(String.format("dcf_count", new Object[0]), 0);
    }

    public static int getAccumulatedDCFFirstCount(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt(String.format("dcf_first_number", new Object[0]), -1);
    }

    public static int getAccumulatedDCFDigit(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt(String.format("dcf_digit", new Object[0]), 0);
    }

    public static int getLastCameraMode(Context c, int defaultViewMode) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("entermode", defaultViewMode);
    }

    public static String getCurrentCameraModeForStartingWindow(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getString("starting_window", CameraConstants.START_CAMERA_NORMAL_VIEW_DUMMY);
    }

    public static int getLastSecondaryCameraMode(Context c, int defaultViewMode) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_SECONDARY, 0).getInt("entermode", defaultViewMode);
    }

    public static void saveLastSecondaryDualWindow(Context c, String window) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_SECONDARY, 0).edit();
        editor.putString("dualwindow", window);
        editor.apply();
    }

    public static String getLastSecondaryDualWindow(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_SECONDARY, 0).getString("dualwindow", SharedPreferenceUtilBase.DUAL_WINDOW_DEFAULT_INDEX);
    }

    public static void saveLastThumbnail(Context context, Uri uri) {
        if (context != null) {
            saveLastThumbnailUri(context, uri);
            saveLastThumbnailPath(context, FileUtil.getRealPathFromURI(context, uri));
        }
    }

    public static void saveLastThumbnail(Context context, Uri uri, String path) {
        if (context != null) {
            saveLastThumbnailUri(context, uri);
            saveLastThumbnailPath(context, path);
        }
    }

    public static void saveLastPicture(Context context, Uri uri) {
        if (context != null) {
            saveLastPictureUri(context, uri);
            saveLastPicturePath(context, FileUtil.getRealPathFromURI(context, uri));
        }
    }

    public static void saveLastVideo(Context context, Uri uri) {
        if (context != null) {
            saveLastVideoUri(context, uri);
            saveLastVideoPath(context, FileUtil.getRealPathFromURI(context, uri));
        }
    }

    public static void saveLastThumbnailUri(Context c, Uri uri) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        if (uri != null) {
            editor.putString("thumbnail_uri", uri.toString());
        } else {
            editor.remove("thumbnail_uri");
        }
        editor.apply();
        saveQuickClipUri(c, uri);
    }

    public static void saveLastPictureUri(Context c, Uri uri) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        if (uri != null) {
            editor.putString("thumbnail_uri_camera", uri.toString());
        } else {
            editor.remove("thumbnail_uri_camera");
        }
        editor.apply();
    }

    public static void saveLastVideoUri(Context c, Uri uri) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        if (uri != null) {
            editor.putString("thumbnail_uri_camcorder", uri.toString());
        } else {
            editor.remove("thumbnail_uri_camcorder");
        }
        editor.apply();
    }

    public static void saveLastThumbnailPath(Context c, String path) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        if (path != null) {
            editor.putString("thumbnail_path", path);
        } else {
            editor.remove("thumbnail_path");
        }
        editor.apply();
    }

    public static void saveLastPicturePath(Context c, String path) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        if (path != null) {
            editor.putString("thumbnail_path_camera", path);
        } else {
            editor.remove("thumbnail_path_camera");
        }
        editor.apply();
    }

    public static void saveLastVideoPath(Context c, String path) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        if (path != null) {
            editor.putString("thumbnail_path_camcorder", path);
        } else {
            editor.remove("thumbnail_path_camcorder");
        }
        editor.apply();
    }

    public static String getLastThumbnailUri(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getString("thumbnail_uri", null);
    }

    public static String getLastThumbnailPath(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getString("thumbnail_path", null);
    }

    public static String getLastPictureUri(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getString("thumbnail_uri_camera", null);
    }

    public static String getLastVideoUri(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getString("thumbnail_uri_camcorder", null);
    }

    public static String getLastPicturePath(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getString("thumbnail_path_camera", null);
    }

    public static String getLastVideoPath(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getString("thumbnail_path_camcorder", null);
    }

    public static int getShutterSoundIndex(Context c) {
        if (c != null) {
            return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("shutter_sound_index", 0);
        }
        return -1;
    }

    public static void saveShutterSoundIndex(Context c, int index) {
        if (c != null) {
            Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
            editor.putInt("shutter_sound_index", index);
            editor.apply();
        }
    }

    public static void saveVideoSizeIndexAtPrimaryNormalMode(Context c, int index) {
        if (c != null) {
            Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
            editor.putInt("video_size_at_normal", index);
            editor.apply();
        }
    }

    public static int getVideoSizeIndexAtPrimaryNormalMode(Context c) {
        if (c != null) {
            return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("video_size_at_normal", 0);
        }
        return -1;
    }

    public static void saveVideoSizeIndexAtSecondaryNormalMode(Context c, int index) {
        if (c != null) {
            Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_SECONDARY, 0).edit();
            editor.putInt("video_size_at_normal", index);
            editor.apply();
        }
    }

    public static int getVideoSizeIndexAtSecondaryNormalMode(Context c) {
        if (c != null) {
            return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_SECONDARY, 0).getInt("video_size_at_normal", 0);
        }
        return -1;
    }

    public static int getShutterKeyIndex(Context c) {
        if (c != null) {
            return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("shutter_key_index", 0);
        }
        return -1;
    }

    public static void saveShutterKeyIndex(Context c, int index) {
        if (c != null) {
            Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
            editor.putInt("shutter_key_index", index);
            editor.apply();
        }
    }

    public static int getInitialGuideStep(Context c) {
        if (c != null) {
            return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("initial_guide_step", 0);
        }
        return -1;
    }

    public static void saveInitialGuideStep(Context c, int index) {
        if (c != null) {
            Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
            editor.putInt("initial_guide_step", index);
            editor.apply();
        }
    }

    public static int getInitialTagLocation(Context c) {
        if (c != null) {
            return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("initial_tag_location", 0);
        }
        return -1;
    }

    public static int getNeedShowStorageInitDialog(Context c) {
        if (c != null) {
            return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("show_storage_init_guide", 1);
        }
        return -1;
    }

    public static int getCameraId(Context c) {
        int defaultCameraId = 0;
        SharedPreferences pref = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0);
        if (ConfigurationUtil.sIS_WIDE_CAMERA_DEFAULT) {
            defaultCameraId = 2;
        }
        return pref.getInt("cameraId", defaultCameraId);
    }

    public static int getCropAngleButtonId(Context c) {
        SharedPreferences pref = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0);
        if (ModelProperties.getCarrierCode() == 6) {
            return pref.getInt("cropAngleButtonId", 1);
        }
        return pref.getInt("cropAngleButtonId", 0);
    }

    public static void saveCropAngleButtonId(Context c, int id) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putInt("cropAngleButtonId", id);
        editor.apply();
        CamLog.m7i(CameraConstants.TAG, "saveCropAngleButtonId = " + id);
    }

    public static int getPastSDInsertionStatus(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("sd_insertion", 0);
    }

    public static String getBeautyLevel(Context c, String prefName) {
        return c.getSharedPreferences(prefName, 0).getString(Setting.KEY_BEAUTYSHOT, "4");
    }

    public static String getLightFrameStatus(Context c) {
        return c.getSharedPreferences("front", 0).getString(Setting.KEY_LIGHTFRAME, "off");
    }

    public static void saveQuickClipUri(Context c, Uri uri) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        if (uri != null) {
            editor.putString("quickclip_uri", uri.toString());
        } else {
            editor.remove("quickclip_uri");
        }
        editor.apply();
    }

    public static void saveQuickClipBubble(Context c, boolean show) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putBoolean("quickclip_bubble", show);
        editor.apply();
    }

    public static boolean getQuickClipBubble(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getBoolean("quickclip_bubble", true);
    }

    public static String getQuickClipUri(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getString("quickclip_uri", null);
    }

    public static void saveSnapMovieOrientation(Context c, int orientation) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putInt("snap_movie_orientation", orientation);
        editor.apply();
    }

    public static int getSnapMovieOrientation(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("snap_movie_orientation", -1);
    }

    public static void saveDualViewType(Context c, int type) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putInt("dualview_type", type);
        editor.apply();
    }

    public static int getDualViewType(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("dualview_type", 2);
    }

    public static void saveRearFlashMode(Context c, boolean isRearFlash) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putBoolean("rear_flash_mode", isRearFlash);
        editor.apply();
    }

    public static boolean getRearFlashMode(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getBoolean("rear_flash_mode", false);
    }

    public static void saveManualAudioSettingValues(Context c, String settings) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putString("manual_audio_settings", settings);
        editor.apply();
    }

    public static String getManualAudioSettingValues(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getString("manual_audio_settings", "1:70:0:0:70:70");
    }

    public static int getFrontCameraId(Context c) {
        return c.getSharedPreferences("front", 0).getInt("front_camera_id", 1);
    }

    public static void saveFrontCameraId(Context c, int id) {
        Editor editor = c.getSharedPreferences("front", 0).edit();
        editor.putInt("front_camera_id", id);
        editor.apply();
    }

    public static int getRearCameraId(Context c) {
        int defaultCameraId = 0;
        SharedPreferences pref = c.getSharedPreferences("rear", 0);
        if (ConfigurationUtil.sIS_WIDE_CAMERA_DEFAULT) {
            defaultCameraId = 2;
        }
        return pref.getInt("rear_camera_id", defaultCameraId);
    }

    public static void saveRearCameraId(Context c, int id) {
        Editor editor = c.getSharedPreferences("rear", 0).edit();
        editor.putInt("rear_camera_id", id);
        editor.apply();
    }

    public static int getTimeLapseSpeedValue(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("time_lapse_speed_value", 15);
    }

    public static void saveTimeLapseSpeedValue(Context c, int speedValue) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putInt("time_lapse_speed_value", speedValue);
        editor.apply();
    }

    public static int getJumpcutShotCount(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("flash_jumpcut_shot_count", 4);
    }

    public static void saveJumpcutShotCount(Context c, int speedValue) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putInt("flash_jumpcut_shot_count", speedValue);
        editor.apply();
    }

    public static String getPenSupportedValue(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getString("pen_supported_model", CameraConstants.PEN_SUPPORTED_NOT_DEFINED);
    }

    public static void savePenSupportedValue(Context c, String penSupported) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putString("pen_supported_model", penSupported);
        editor.apply();
    }

    public static boolean getFocusPeakingEnable(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getBoolean("focus_peaking_enable", true);
    }

    public static void saveFocusPeakingEnable(Context c, boolean isPeaking) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putBoolean("focus_peaking_enable", isPeaking);
        editor.apply();
    }

    public static void saveFocusPeakingGuide(Context c, boolean show) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putBoolean("focus_peaking_toast_guide", show);
        editor.apply();
    }

    public static boolean getFocusPeakingGuide(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getBoolean("focus_peaking_toast_guide", false);
    }

    public static void saveFingerprintShotGuide(Context c, boolean show) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putBoolean("fingerprint_shot_guide", show);
        editor.apply();
    }

    public static boolean getFingerprintShotGuide(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getBoolean("fingerprint_shot_guide", false);
    }

    public static void saveSquareModeInitGuide(Context c, boolean show) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putBoolean("square_mode_init_guide", show);
        editor.apply();
    }

    public static boolean getSquareModeInitGuide(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getBoolean("square_mode_init_guide", true);
    }

    public static void saveHotKeyMessageShown(Context c, boolean show) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putBoolean("hotkey_toast_msg", show);
        editor.apply();
    }

    public static boolean getHotKeyMessageShown(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getBoolean("hotkey_toast_msg", false);
    }

    public static int getSquareOverlapProject(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("overlap_project_index", 0);
    }

    public static void saveSquareOverlapProject(Context c, int index) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putInt("overlap_project_index", index);
        editor.apply();
    }

    public static boolean getSquareOverlapDefaultAdded(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getBoolean("overlap_default_added", false);
    }

    public static void saveSquareOverlapDefaultAdded(Context c, boolean added) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putBoolean("overlap_default_added", added);
        editor.apply();
    }

    public static boolean getInitialHelpShown(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getBoolean("photo_size_help", false);
    }

    public static void saveInitialHelpShown(Context c, boolean shown) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putBoolean("photo_size_help", shown);
        editor.apply();
    }

    public static boolean getCineEffectBubbleShown(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getBoolean("cine_effect_bubble", false);
    }

    public static void setCineEffectBubbleShown(Context c, boolean shown) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putBoolean("cine_effect_bubble", shown);
        editor.apply();
    }

    public static boolean getCinemaInitGuideShown(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getBoolean("cinema_mode_init_guide", false);
    }

    public static void setCinemaInitGuideShown(Context c, boolean shown) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putBoolean("cinema_mode_init_guide", shown);
        editor.apply();
    }

    public static boolean getAICamInitGuideShown(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getBoolean("AI_CAM_init_guide", false);
    }

    public static void setAICamInitGuideShown(Context c, boolean shown) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putBoolean("AI_CAM_init_guide", shown);
        editor.apply();
    }

    public static boolean getGraphyInitGuideShown(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getBoolean("graphy_init_guide", false);
    }

    public static void setGraphyInitGuideShown(Context c, boolean shown) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putBoolean("graphy_init_guide", shown);
        editor.apply();
    }

    public static int getCinemaLUTIndex(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("cinema_mode_LUT_index", 0);
    }

    public static void setCinemaLUTIndex(Context c, int index) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putInt("cinema_mode_LUT_index", index);
        editor.apply();
    }

    public static int getScreenResolution(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("screen_resolution", -1);
    }

    public static void setScreenResolution(Context c, int screenResolution) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putInt("screen_resolution", screenResolution);
        editor.apply();
    }

    public static void saveManualModeInitialGuide(Context c, boolean show) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putBoolean("manual_mode_initial_guide", show);
        editor.apply();
    }

    public static boolean getManualModeInitialGuide(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getBoolean("manual_mode_initial_guide", false);
    }

    public static void setPictureSizeBackupIndex(Context c, String key, int index) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putInt(key, index);
        editor.apply();
    }

    public static int getPictureSizeBackupIndex(Context c, String key) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt(key, 0);
    }

    public static boolean getNightVisionManualToastState(Context c, boolean isOn) {
        SharedPreferences pref = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0);
        return isOn ? pref.getBoolean("night_vision_manual_on", false) : pref.getBoolean("night_vision_manual_off", false);
    }

    public static void setNightVisionManualToastState(Context c, boolean isOn) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        if (isOn) {
            editor.putBoolean("night_vision_manual_on", true);
        } else {
            editor.putBoolean("night_vision_manual_off", true);
        }
        editor.apply();
    }

    public static int getSensorModuleType(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("module_type", -1);
    }

    public static void setSensorModuleType(Context c, int type) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putInt("module_type", type);
        editor.apply();
    }

    public static boolean getAICamPeopleEffectShown(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getBoolean("AI_CAM_people_effect", false);
    }

    public static void setAICamPeopleEffectShown(Context c, boolean shown) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putBoolean("AI_CAM_people_effect", shown);
        editor.apply();
    }
}
