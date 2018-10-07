package com.lge.camera.settings;

import android.content.Context;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.util.CamLog;

public class SettingVariant extends SettingVariantBase {
    public void makePreferenceVariant(Context context, PreferenceGroup prefGroup) {
        super.makePreferenceVariant(context, prefGroup);
        if (context != null && prefGroup != null) {
            try {
                addManualVideoHDR10Menu(context, prefGroup);
                addManualVideoLogMenu(context, prefGroup);
                addGraphyMenu(context, prefGroup);
                addManualNoiseReductionMenu(context, prefGroup);
                addFingerDetectionMenu(context, prefGroup);
                addColorEffetMenu(context, prefGroup);
                addDualPopTypeMenu(context, prefGroup);
                addLivePhotoMenu(context, prefGroup);
                addSmartCamMenu(context, prefGroup);
                addQRMenu(context, prefGroup);
                addFullVisionMenu(context, prefGroup);
                addBinningMenu(context, prefGroup);
                addLensSelectionMenu(context, prefGroup);
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "Preference add exception : ", e);
            }
        }
    }

    public void addManualVideoLogMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO) && FunctionProperties.isSupportedLogProfile() && prefGroup.findPreference(Setting.KEY_MANUAL_VIDEO_LOG) == null) {
            prefGroup.addChild(PrefMaker.makeManualVideoLogPreference(context, prefGroup));
        }
    }

    public void addManualVideoHDR10Menu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO) && FunctionProperties.isSupportedHDR10() && prefGroup.findPreference(Setting.KEY_HDR10) == null) {
            prefGroup.addChild(PrefMaker.makeManualVideoHDR10Preference(context, prefGroup));
        }
    }

    public void addManualNoiseReductionMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA) && prefGroup.findPreference(Setting.KEY_MANUAL_NOISE_REDUCTION) == null) {
            prefGroup.addChild(PrefMaker.makeManualNoiseReductionPreference(context, prefGroup));
        }
    }

    public void addGraphyMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA) && FunctionProperties.isSupportedGraphy() && prefGroup.findPreference(Setting.KEY_GRAPHY) == null) {
            prefGroup.addChild(PrefMaker.makeGraphyPreference(context, prefGroup));
        }
    }

    public void addFingerDetectionMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedFingerDetection() && prefGroup.findPreference(Setting.KEY_FINGER_DETECTION) == null) {
            prefGroup.addChild(PrefMaker.makeFingerDetectionPreference(context, prefGroup));
        }
    }

    public void addColorEffetMenu(Context context, PreferenceGroup prefGroup) {
        if (!FunctionProperties.isSupportedFilmEmulator()) {
            if ("rear".equals(prefGroup.getSharedPreferenceName()) && prefGroup.findPreference(Setting.KEY_COLOR_EFFECT) == null) {
                prefGroup.addChild(PrefMaker.makeColorEffectPreference(context, prefGroup, true));
            }
            if ("front".equals(prefGroup.getSharedPreferenceName()) && prefGroup.findPreference(Setting.KEY_COLOR_EFFECT) == null) {
                prefGroup.addChild(PrefMaker.makeColorEffectPreference(context, prefGroup, false));
            }
        }
    }

    public void addLivePhotoMenu(Context context, PreferenceGroup prefGroup) {
        if (("rear".equals(prefGroup.getSharedPreferenceName()) || "front".equals(prefGroup.getSharedPreferenceName())) && FunctionProperties.isLivePhotoSupported() && prefGroup.findPreference(Setting.KEY_LIVE_PHOTO) == null) {
            prefGroup.addChild(PrefMaker.makeLivePhotoPreference(context, prefGroup));
        }
    }

    public void addDualPopTypeMenu(Context context, PreferenceGroup prefGroup) {
        if (FunctionProperties.isSupportedMode(CameraConstants.MODE_DUAL_POP_CAMERA) && prefGroup.findPreference(Setting.KEY_DUAL_POP_TYPE) == null) {
            prefGroup.addChild(PrefMaker.makeDualPopTypePreference(context, prefGroup));
        }
    }

    public void addSmartCamMenu(Context context, PreferenceGroup prefGroup) {
        if (("rear".equals(prefGroup.getSharedPreferenceName()) || "front".equals(prefGroup.getSharedPreferenceName())) && FunctionProperties.isSupportedSmartCam(context) && prefGroup.findPreference(Setting.KEY_SMART_CAM_FILTER) == null) {
            prefGroup.addChild(PrefMaker.makeSmartCamPreference(context, prefGroup));
        }
    }

    public void addQRMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedQrCode(context) && prefGroup.findPreference(Setting.KEY_QR) == null) {
            prefGroup.addChild(PrefMaker.makeQRPreference(context, prefGroup));
        }
    }

    public void addBinningMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedBinning() && prefGroup.findPreference(Setting.KEY_BINNING) == null) {
            prefGroup.addChild(PrefMaker.makeBinningPreference(context, prefGroup));
        }
    }

    public void addLensSelectionMenu(Context context, PreferenceGroup prefGroup) {
        if (!FunctionProperties.isSupportedLGLens(context) || !FunctionProperties.isSupportedGoogleLens()) {
            return;
        }
        if (("rear".equals(prefGroup.getSharedPreferenceName()) || "front".equals(prefGroup.getSharedPreferenceName())) && prefGroup.findPreference(Setting.KEY_LENS_SELECTION) == null) {
            prefGroup.addChild(PrefMaker.makeLensSelectionMenu(context, prefGroup));
        }
    }
}
