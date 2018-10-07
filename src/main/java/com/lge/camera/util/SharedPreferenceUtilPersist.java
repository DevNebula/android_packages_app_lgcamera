package com.lge.camera.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.PreferenceProperties;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.PreferenceGroup;
import com.lge.camera.settings.PreferenceInflater;
import com.lge.camera.settings.Setting;
import com.lge.camera.settings.SettingVariant;

public class SharedPreferenceUtilPersist extends SharedPreferenceUtil {
    public static void resetAllPreference(Context c, boolean isManualModeSupportetd) {
        SharedPreferences primaryPref = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0);
        resetPreference(c, primaryPref, PreferenceProperties.getRearPreference());
        resetRearOnlyPreference(c, primaryPref, isManualModeSupportetd);
        SharedPreferences secondPref = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_SECONDARY, 0);
        resetPreference(c, secondPref, PreferenceProperties.getFrontPreference());
        resetFrontOnlyPreference(c, secondPref, isManualModeSupportetd);
        SharedPreferenceUtilBase.setCameraId(c, 0);
        SharedPreferenceUtilBase.saveAccumulatedDCFCount(c, 0);
        SharedPreferenceUtilBase.saveAccumulatedDCFFirstCount(c, -1);
        SharedPreferenceUtilBase.saveAccumulatedDCFDigit(c, 0);
        SharedPreferenceUtilBase.saveInitialTagLocation(c, 0);
        SharedPreferenceUtilBase.saveNeedShowStorageInitDialog(c, 1);
        SharedPreferenceUtilBase.savePastSDInsertionStatus(c, 1);
        SharedPreferenceUtil.saveQuickClipUri(c, null);
        SharedPreferenceUtil.saveQuickClipBubble(c, true);
        SharedPreferenceUtil.saveFrontCameraId(c, 1);
        SharedPreferenceUtil.saveRearCameraId(c, 0);
        SharedPreferenceUtil.saveTimeLapseSpeedValue(c, 15);
        SharedPreferenceUtil.saveSnapMovieOrientation(c, -1);
        SharedPreferenceUtil.saveRearFlashMode(c, false);
        if (FunctionProperties.getCameraTypeFront() == 2) {
            SharedPreferenceUtil.saveCropAngleButtonId(c, 1);
        }
        SharedPreferenceUtil.saveFocusPeakingEnable(c, true);
        SharedPreferenceUtil.saveFocusPeakingGuide(c, false);
    }

    private static void resetPreference(Context c, SharedPreferences pref, int prefXml) {
        PreferenceGroup prefGroup = (PreferenceGroup) new PreferenceInflater(c).inflate(prefXml);
        if (prefGroup != null) {
            new SettingVariant().makePreferenceVariant(c, prefGroup);
            Editor editor = pref.edit();
            int size = prefGroup.size();
            String defaultValue = "";
            for (int i = 0; i < size; i++) {
                ListPreference listPref = prefGroup.getListPreference(i);
                if (listPref != null) {
                    defaultValue = listPref.getDefaultValue();
                    if (!"".equals(defaultValue)) {
                        listPref.setValue(defaultValue);
                        editor.putBoolean(listPref.getKey(), false);
                        editor.apply();
                    }
                }
            }
            resetModeHelpPreference(prefGroup, editor);
        }
    }

    private static void resetModeHelpPreference(PreferenceGroup prefGroup, Editor editor) {
        if (prefGroup != null && editor != null) {
            try {
                ListPreference listPref = prefGroup.findPreference(Setting.KEY_MODE);
                if (listPref != null) {
                    for (CharSequence mode : listPref.getEntryValues()) {
                        editor.putBoolean(mode.toString(), false);
                    }
                    editor.apply();
                }
            } catch (Exception e) {
            }
        }
    }

    private static void resetRearOnlyPreference(Context c, SharedPreferences pref, boolean isManualModeSupported) {
        Editor editor = pref.edit();
        editor.putBoolean(CameraConstants.MANUAL_MODE_ON, false);
        editor.apply();
        SharedPreferenceUtilBase.saveLastCameraMode(c, 1);
    }

    private static void resetFrontOnlyPreference(Context c, SharedPreferences pref, boolean isManualModeSupported) {
        Editor editor = pref.edit();
        editor.putString("dualwindow", SharedPreferenceUtilBase.DUAL_WINDOW_DEFAULT_INDEX);
        editor.apply();
        SharedPreferenceUtilBase.saveLastSecondaryCameraMode(c, 1);
    }

    public static void saveDataCleared(Context c, int isCleared) {
        Editor editor = c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putInt("data_cleared", isCleared);
        editor.apply();
    }

    public static int getDataCleared(Context c) {
        return c.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("data_cleared", 1);
    }
}
