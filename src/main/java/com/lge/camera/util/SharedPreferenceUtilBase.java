package com.lge.camera.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.lge.camera.constants.CameraConstants;

public class SharedPreferenceUtilBase {
    public static final String DUAL_WINDOW_DEFAULT_INDEX = "1:1";
    public static final String SETTING_FRONT = "front";
    public static final String SETTING_PRIMARY = "Main_CameraAppConfig";
    public static final String SETTING_REAR = "rear";
    public static final String SETTING_SECONDARY = "Secondary_CameraAppConfig";

    public static void setCameraId(Context c, int cameraId) {
        if (c != null) {
            Editor editor = c.getSharedPreferences(SETTING_PRIMARY, 0).edit();
            editor.putInt("cameraId", cameraId);
            editor.apply();
            CamLog.m7i(CameraConstants.TAG, "setCameraId = " + cameraId);
        }
    }

    public static void saveAccumulatedDCFCount(Context c, long count) {
        if (c != null) {
            Editor editor = c.getSharedPreferences(SETTING_PRIMARY, 0).edit();
            editor.putLong(String.format("dcf_count", new Object[0]), count);
            editor.apply();
            CamLog.m7i(CameraConstants.TAG, "saved counter = " + count);
        }
    }

    public static void saveAccumulatedDCFFirstCount(Context c, int firstCount) {
        if (c != null) {
            Editor editor = c.getSharedPreferences(SETTING_PRIMARY, 0).edit();
            editor.putInt(String.format("dcf_first_number", new Object[0]), firstCount);
            editor.apply();
            CamLog.m7i(CameraConstants.TAG, "saved counter = " + firstCount);
        }
    }

    public static void saveAccumulatedDCFDigit(Context c, int digit) {
        if (c != null) {
            Editor editor = c.getSharedPreferences(SETTING_PRIMARY, 0).edit();
            editor.putInt(String.format("dcf_digit", new Object[0]), digit);
            editor.apply();
            CamLog.m7i(CameraConstants.TAG, "saved counter = " + digit);
        }
    }

    public static void saveInitialTagLocation(Context c, int index) {
        if (c != null) {
            Editor editor = c.getSharedPreferences(SETTING_PRIMARY, 0).edit();
            editor.putInt("initial_tag_location", index);
            editor.apply();
        }
    }

    public static void saveNeedShowStorageInitDialog(Context c, int index) {
        if (c != null) {
            Editor editor = c.getSharedPreferences(SETTING_PRIMARY, 0).edit();
            CamLog.m3d(CameraConstants.TAG, "saveNeedShowStorageInitDialog : " + index);
            editor.putInt("show_storage_init_guide", index);
            editor.apply();
        }
    }

    public static void savePastSDInsertionStatus(Context c, int status) {
        if (c != null) {
            Editor editor = c.getSharedPreferences(SETTING_PRIMARY, 0).edit();
            editor.putInt("sd_insertion", status);
            editor.apply();
            CamLog.m7i(CameraConstants.TAG, "sd_insertion = " + status);
        }
    }

    public static void saveLastCameraMode(Context c, int m) {
        if (c != null) {
            Editor editor = c.getSharedPreferences(SETTING_PRIMARY, 0).edit();
            editor.putInt("entermode", m);
            editor.apply();
        }
    }

    public static void saveCurrentCameraModeForStartingWindow(Context c, String layout) {
        if (c != null) {
            Editor editor = c.getSharedPreferences(SETTING_PRIMARY, 0).edit();
            editor.putString("starting_window", layout);
            editor.apply();
        }
    }

    public static void setModeList(Context context, String list, boolean rear) {
        if (context != null) {
            Editor editor = context.getSharedPreferences(SETTING_PRIMARY, 0).edit();
            editor.putString(rear ? "rear_shotmode_list" : "front_shotmode_list", list);
            editor.apply();
        }
    }

    public static String getModeList(Context context, boolean rear) {
        return context.getSharedPreferences(SETTING_PRIMARY, 0).getString(rear ? "rear_shotmode_list" : "front_shotmode_list", null);
    }

    public static void saveUspSetting(Context context, String usp) {
        if (context != null) {
            Editor editor = context.getSharedPreferences(SETTING_PRIMARY, 0).edit();
            editor.putString("usp_setting", usp);
            editor.apply();
        }
    }

    public static String getUspSetting(Context context) {
        return context.getSharedPreferences(SETTING_PRIMARY, 0).getString("usp_setting", null);
    }

    public static void saveLastSecondaryCameraMode(Context c, int m) {
        if (c != null) {
            Editor editor = c.getSharedPreferences(SETTING_SECONDARY, 0).edit();
            editor.putInt("entermode", m);
            editor.apply();
        }
    }

    public static void saveCameraPictureSizeList(Context c, int cameraId, String list) {
        if (c != null) {
            String key = "rear_picture_size";
            if (cameraId == 0) {
                key = "rear_picture_size";
            } else if (cameraId == 1) {
                key = "front_picture_size";
            } else if (cameraId == 2) {
                key = "rear_picture_size_sub";
            } else if (cameraId == 3) {
                key = "front_picture_size_sub";
            }
            Editor editor = c.getSharedPreferences(SETTING_PRIMARY, 0).edit();
            CamLog.m3d(CameraConstants.TAG, "saveRearCameraPictureSizeList : " + list);
            editor.putString(key, list);
            editor.apply();
        }
    }

    public static String getCameraPictureSizeList(Context c, int cameraId, String defaultValue) {
        SharedPreferences pref = c.getSharedPreferences(SETTING_PRIMARY, 0);
        String key = "rear_picture_size";
        if (cameraId == 0) {
            key = "rear_picture_size";
        } else if (cameraId == 1) {
            key = "front_picture_size";
        } else if (cameraId == 2) {
            key = "rear_picture_size_sub";
        } else if (cameraId == 3) {
            key = "front_picture_size_sub";
        }
        return pref.getString(key, defaultValue);
    }

    public static void saveCameraDefaultPictureSize(Context c, int cameraId, String size) {
        if (c != null) {
            String key = "rear_default_size";
            if (cameraId == 0) {
                key = "rear_default_size";
            } else if (cameraId == 1) {
                key = "front_default_size";
            } else if (cameraId == 2) {
                key = "rear_default_size_sub";
            } else if (cameraId == 3) {
                key = "front_default_size_sub";
            }
            Editor editor = c.getSharedPreferences(SETTING_PRIMARY, 0).edit();
            CamLog.m3d(CameraConstants.TAG, "saveCameraDefaultPictureSize : " + size);
            editor.putString(key, size);
            editor.apply();
        }
    }

    public static String getCameraDefaultSize(Context c, int cameraId, String defaultValue) {
        SharedPreferences pref = c.getSharedPreferences(SETTING_PRIMARY, 0);
        String key = "rear_default_size";
        if (cameraId == 0) {
            key = "rear_default_size";
        } else if (cameraId == 1) {
            key = "front_default_size";
        } else if (cameraId == 2) {
            key = "rear_default_size_sub";
        } else if (cameraId == 3) {
            key = "front_default_size_sub";
        }
        return pref.getString(key, defaultValue);
    }

    public static void saveGridShotFirstGuide(Context c, boolean isFirst) {
        Editor editor = c.getSharedPreferences(SETTING_PRIMARY, 0).edit();
        editor.putBoolean("grid_first_guide", isFirst);
        editor.apply();
    }

    public static boolean getGridShotFirstGuide(Context c) {
        return c.getSharedPreferences(SETTING_PRIMARY, 0).getBoolean("grid_first_guide", false);
    }

    public static void saveDualShotFirstGuide(Context c, boolean isFirst) {
        Editor editor = c.getSharedPreferences(SETTING_PRIMARY, 0).edit();
        editor.putBoolean("dual_first_guide", isFirst);
        editor.apply();
    }

    public static boolean getDualShotFirstGuide(Context c) {
        return c.getSharedPreferences(SETTING_PRIMARY, 0).getBoolean("dual_first_guide", false);
    }

    public static void saveOverlapShotFirstGuide(Context c, boolean isFirst) {
        Editor editor = c.getSharedPreferences(SETTING_PRIMARY, 0).edit();
        editor.putBoolean("overlap_first_guide", isFirst);
        editor.apply();
    }

    public static boolean getOverlapShotFirstGuide(Context c) {
        return c.getSharedPreferences(SETTING_PRIMARY, 0).getBoolean("overlap_first_guide", false);
    }

    public static void saveFilterMenuListOrder(Context c, String list) {
        if (c != null) {
            Editor editor = c.getSharedPreferences(SETTING_PRIMARY, 0).edit();
            CamLog.m3d(CameraConstants.TAG, "saveFilterMenuListOrder : " + list);
            editor.putString("filter_menu_list_order", list);
            editor.apply();
        }
    }

    public static String getFilterMenuListOrder(Context c) {
        return c.getSharedPreferences(SETTING_PRIMARY, 0).getString("filter_menu_list_order", "");
    }

    public static void saveLastSelectFilter(Context c, String value) {
        if (c != null) {
            Editor editor = c.getSharedPreferences(SETTING_PRIMARY, 0).edit();
            CamLog.m3d(CameraConstants.TAG, "saveLastSelectFilter : " + value);
            editor.putString("last_select_filter", value);
            editor.apply();
        }
    }

    public static String getLastSelectFilter(Context c) {
        return c.getSharedPreferences(SETTING_PRIMARY, 0).getString("last_select_filter", CameraConstants.FILM_NONE);
    }

    public static void saveRearFilterStrength(Context c, int strength) {
        if (c != null) {
            Editor editor = c.getSharedPreferences("rear", 0).edit();
            CamLog.m3d(CameraConstants.TAG, "saveRearFilterStrength : " + strength);
            editor.putInt("rear_filter_strength", strength);
            editor.apply();
        }
    }

    public static int getRearFilterStrength(Context c) {
        return c.getSharedPreferences("rear", 0).getInt("rear_filter_strength", 100);
    }

    public static void saveFrontFilterStrength(Context c, int strength) {
        if (c != null) {
            Editor editor = c.getSharedPreferences("front", 0).edit();
            CamLog.m3d(CameraConstants.TAG, "saveFrontFilterStrength : " + strength);
            editor.putInt("front_filter_strength", strength);
            editor.apply();
        }
    }

    public static int getFrontFilterStrength(Context c) {
        return c.getSharedPreferences("front", 0).getInt("front_filter_strength", 100);
    }

    public static void saveFrontFlashStep(Context c, int level) {
        if (c != null) {
            Editor editor = c.getSharedPreferences("front", 0).edit();
            CamLog.m3d(CameraConstants.TAG, "saveFrontFlashLevel : " + level);
            editor.putInt("front_flash_level", level);
            editor.apply();
        }
    }

    public static int getFrontFlashStep(Context c) {
        return c.getSharedPreferences("front", 0).getInt("front_flash_level", 1);
    }
}
