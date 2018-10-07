package com.lge.camera.constants;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.SensorManager;
import android.os.Build.VERSION;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import com.lge.R.integer;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.PackageUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.media.CamcorderProfileEx;
import java.lang.reflect.Field;

public final class FunctionProperties {
    public static final int sBURST_SHOT_MAX_COUNT_VALUE = 101000030;
    public static final int sBURST_SHOT_MAX_COUNT_VALUE_NOT_SUPPORTED_FASTSHOT = 100300030;
    private static int sCheckAuClude = 0;
    private static boolean sCheckFuntionPropertyDone = false;
    public static int sDURATION = 0;
    public static int sIsFPSensorExists = -1;
    public static int sIsHotKeyRecordingSupported = -1;
    public static int sIsSupportedFingerprintMode = -1;
    public static int sMAX_COUNT = 0;
    public static boolean sSensorStatus = false;
    private static boolean sSupportedConeUI = false;

    public static boolean isSupportedZSL(int cameraId) {
        if (ModelProperties.isRenesasISP()) {
            return false;
        }
        switch (ConfigurationHALUtil.sZSL_SUPPORTED_VALUE) {
            case 0:
                return false;
            case 2:
                if (cameraId != 0) {
                    return false;
                }
                return true;
            case 3:
                if (cameraId != 1) {
                    return false;
                }
                return true;
            default:
                return true;
        }
    }

    public static boolean isSupportedBurstShot() {
        try {
            String configBuildFlagStr = "com.lge.config.ConfigBuildFlags";
            Field field = Class.forName("com.lge.config.ConfigBuildFlags").getField("CAPP_CAMERA_BURSTSHOT");
            field.setAccessible(true);
            if (field.isAccessible()) {
                return field.getBoolean(Class.forName("com.lge.config.ConfigBuildFlags"));
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isSupportedLightFrame() {
        return ConfigurationUtil.sLIGHT_FRAME_SUPPORTED_VALUE;
    }

    public static boolean isSupportedTimerHelper() {
        return ConfigurationUtil.sTIMER_HELPER_SUPPORTED_VALUE;
    }

    public static int isSupportedHDR(boolean isRearCamera) {
        if (!isRearCamera && ModelProperties.isMTKChipset()) {
            return 0;
        }
        if (ConfigurationHALUtil.sHDR_SUPPORTED_VALUE != 4) {
            return ConfigurationHALUtil.sHDR_SUPPORTED_VALUE;
        }
        if (isRearCamera) {
            return 3;
        }
        return 2;
    }

    public static boolean isSupportedMorphoNightShot() {
        return ConfigurationHALUtil.sMORHPO_NIGHT_SHOT_SUPPORTED_VALUE;
    }

    public static boolean isSupportedLongShot(boolean isRearCamera) {
        CamLog.m3d(CameraConstants.TAG, "isSupportedLongShot = " + ConfigurationHALUtil.sLONG_SHOT_SUPPORTED_VALUE);
        if (!ModelProperties.isMTKChipset() || isRearCamera || !isSupportedBeautyShot()) {
            return ConfigurationHALUtil.sLONG_SHOT_SUPPORTED_VALUE;
        }
        CamLog.m3d(CameraConstants.TAG, "not supported longshot with MTK front camera");
        return false;
    }

    public static boolean isSupportedGestureShot() {
        return CameraDeviceUtils.isFrontCameraSupported();
    }

    public static boolean isSupportedBeautyShot() {
        return ConfigurationUtil.sBEAUTYSHOT_SUPPORTED_VALUE == 2;
    }

    public static boolean isSupportedRelighting() {
        if ((!ModelProperties.isJoanRenewal() || ModelProperties.isJoanOriginal()) && isSupportedBeautyShot() && ConfigurationUtil.sRELIGHTING_SUPPORTED_VALUE) {
            return true;
        }
        return false;
    }

    public static boolean isSupportedRearOutfocus() {
        return ConfigurationUtil.sREAR_OUTFOCUS_TYPE > 0;
    }

    public static boolean isSupportedFrontOutfocus() {
        return ConfigurationUtil.sFRONT_OUTFOCUS_TYPE > 0;
    }

    public static boolean isSupportedSuperZoom() {
        return ConfigurationHALUtil.sSUPER_ZOOM_SUPPORTED_VALUE;
    }

    public static boolean isSupportedAEFocus() {
        return ConfigurationHALUtil.sAE_FOCUS_SUPPORTED_VALUE;
    }

    public static int getCropAngleZoomLevel() {
        return ConfigurationUtil.sCROP_ANGLE_ZOOM_LEVEL;
    }

    public static int getBeautyMax() {
        return ConfigurationUtil.sBEAUTY_MAX;
    }

    public static int getBeautyStep() {
        return ConfigurationUtil.sBEAUTY_STEP;
    }

    public static int getRelightingMax() {
        return ConfigurationUtil.sRELIGHTING_MAX;
    }

    public static int getRelightingStep() {
        return ConfigurationUtil.sRELIGHTING_STEP;
    }

    public static int getDisabledFlashBatteryLevel() {
        return ConfigurationUtil.sFLASH_DISABLE_BATTERY_LEVEL_VALUE;
    }

    public static boolean checkSensorStatus(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(ParamConstants.VALUE_BINNING_SENSOR);
        if (sensorManager == null || sensorManager.getDefaultSensor(1) == null || sensorManager.getDefaultSensor(9) == null) {
            return false;
        }
        sSensorStatus = true;
        return true;
    }

    public static boolean isSupportedMotionQuickView() {
        boolean enable;
        if (sSensorStatus && isSupportedGestureShot()) {
            enable = true;
        } else {
            enable = false;
        }
        if (enable && ConfigurationUtil.sMOTIONQUICKVIEWER_SUPPORTED_VALUE) {
            return true;
        }
        return false;
    }

    public static boolean isSupportedVideoHDR() {
        return ConfigurationHALUtil.sVIDEO_HDR_SUPPORTED;
    }

    public static boolean isSupportedLogProfile() {
        return isSupportedMode(CameraConstants.MODE_CINEMA) && !isSupportedHDR10();
    }

    public static boolean isSupportedRearCurtainFlash() {
        return ConfigurationManualUtil.sREAR_CURTAIN_FLASH_SUPPORTED;
    }

    public static int getSupportedHal() {
        return ConfigurationUtil.sHAL_SUPPORTED_VALUE;
    }

    public static boolean isSupportedFrontIntervalShot() {
        if (isSupportedZSL(1)) {
            return true;
        }
        return false;
    }

    public static boolean isSupportedRAWPictureSaving() {
        return ConfigurationManualUtil.sRAW_PICTURE_SAVING_SUPPORTED_VALUE;
    }

    public static boolean isSupportedVoiceShutter() {
        return ConfigurationUtil.sVOICE_SHUTTER_SUPPORTED;
    }

    public static boolean isSupportedSwitchingAnimation() {
        return ConfigurationUtil.sSWITCHING_ANIMTAION_SUPPORTED;
    }

    public static String getFrontFlipDefaultValue() {
        if (ModelProperties.getCarrierCode() == 4) {
            return "on";
        }
        return "on";
    }

    public static void checkConeUI(Context context) {
        int resId = context.getResources().getIdentifier("config_miniactivity_enabled", "bool", "com.lge");
        if (resId != 0 && context.getResources().getBoolean(resId)) {
            sSupportedConeUI = true;
        }
    }

    public static boolean isSupportedConeUI() {
        return sSupportedConeUI;
    }

    public static int getCameraTypeFront() {
        return ConfigurationUtil.sCameraTypeFront;
    }

    public static int getCameraTypeRear() {
        return ConfigurationUtil.sCameraTypeRear;
    }

    public static boolean isSupportedMode(String shotmode) {
        if (ConfigurationUtil.sMODE_REAR_SUPPORTED_ITEMS == null || shotmode == null) {
            return false;
        }
        for (String item : ConfigurationUtil.sMODE_REAR_SUPPORTED_ITEMS) {
            if (shotmode.equals(item) || item.contains(shotmode)) {
                return true;
            }
        }
        if (ConfigurationUtil.sMODE_FRONT_SUPPORTED_ITEMS == null) {
            return false;
        }
        for (String item2 : ConfigurationUtil.sMODE_FRONT_SUPPORTED_ITEMS) {
            if (shotmode.equals(item2) || item2.contains(shotmode)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUspZoneSupported() {
        return true;
    }

    public static boolean isSupportedSteadyCamera(boolean isRearCamera) {
        switch (ConfigurationHALUtil.sSTEADY_CAMERA_SUPPORTED) {
            case 0:
                return false;
            case 1:
                return isRearCamera;
            case 2:
                return true;
            default:
                return false;
        }
    }

    public static boolean useWideRearAsDefault() {
        if (ConfigurationUtil.sCameraTypeRear == 1) {
            return true;
        }
        return false;
    }

    public static boolean isSupportedInAndOutZoom() {
        if (getCameraTypeRear() == 1) {
            return true;
        }
        return false;
    }

    public static boolean isSupportedFingerDetection() {
        if (getCameraTypeRear() == 1) {
            return true;
        }
        return false;
    }

    public static boolean isSupportedGraphy() {
        return isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA) && ConfigurationManualUtil.sGRAPHY_SUPPORTED_VALUE;
    }

    public static boolean isSupportedCameraRoll() {
        return ConfigurationUtil.sCAMERA_ROLL_SUPPORTED;
    }

    public static boolean isSupportedFilmEmulator() {
        return ModelProperties.getAppTier() > 2 && ConfigurationUtil.sFILM_EMULATOR_SUPPORTED;
    }

    public static boolean isFilmStrengthSupported() {
        return ConfigurationUtil.sFILM_STRENGTH_SUPPORTED;
    }

    public static boolean isSupportedFilmRecording() {
        return ModelProperties.getAppTier() >= 5;
    }

    public static boolean isSupportedBeautyRecording() {
        return ModelProperties.getAppTier() >= 5;
    }

    public static boolean isSupportedHDRPreview() {
        return ModelProperties.getAppTier() >= 5 && getSupportedHal() == 2;
    }

    public static boolean isAppyingFilmLimitation() {
        return ModelProperties.getAppTier() < 5;
    }

    public static boolean isSupportedFilterDownload() {
        return ModelProperties.getCarrierCode() != 6 && ModelProperties.getRawAppTier() >= 5 && !ModelProperties.isJoanRenewal() && PackageUtil.isLGSmartWorldPreloaded();
    }

    public static boolean isShutterlessSupported(Context context) {
        boolean z = true;
        SensorManager sensorManager = (SensorManager) context.getSystemService(ParamConstants.VALUE_BINNING_SENSOR);
        if (sensorManager == null) {
            return false;
        }
        if (!(sensorManager.getDefaultSensor(1) != null && CameraDeviceUtils.isFrontCameraSupported() && ConfigurationUtil.sFRONT_SHUTTERLESS_SUPPORTED)) {
            z = false;
        }
        return z;
    }

    public static boolean isGaplessLoopRecordingSupported() {
        return ConfigurationUtil.sGAPLESS_LOOP_RECORDING_SUPPORTED;
    }

    public static void checkFuntionProperty(Context context) {
        if (!sCheckFuntionPropertyDone) {
            checkConeUI(context);
            checkSensorStatus(context);
            sCheckFuntionPropertyDone = true;
        }
    }

    public static boolean isSupportedFastShot() {
        return ConfigurationHALUtil.sFAST_SHOT_SUPPORTED > 0;
    }

    public static boolean isSupportedManualZSL() {
        return isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA) && ConfigurationHALUtil.sZSL_MANUAL_SUPPORTED;
    }

    public static boolean isSupportedManualFocus() {
        return ConfigurationManualUtil.sMANUAL_FOCUS_SUPPORTED;
    }

    public static int getSupportedBurstShotDuration(boolean isInternal, boolean invalidate, boolean isRearCamera) {
        if (invalidate || sDURATION == 0) {
            sDURATION = isRearCamera ? ConfigurationUtil.sBURST_SHOT_DURATION : ConfigurationUtil.sBURST_SHOT_DURATION_FRONT;
            CamLog.m3d(CameraConstants.TAG, "[burst] sDURATION = " + sDURATION);
            if (sDURATION >= CameraConstants.BURST_CHECK_BIT) {
                int i;
                int intTime = (int) Math.floor((double) ((sDURATION - CameraConstants.BURST_CHECK_BIT) / 10000));
                int extTime = sDURATION - ((intTime * 10000) + CameraConstants.BURST_CHECK_BIT);
                if (isInternal) {
                    i = intTime;
                } else {
                    i = extTime;
                }
                sDURATION = i;
                CamLog.m3d(CameraConstants.TAG, "[Burst] Internal duration = " + intTime + ", External duration = " + extTime);
            } else if (sDURATION < 0) {
                sDURATION = isSupportedFastShot() ? 67 : 100;
                CamLog.m3d(CameraConstants.TAG, "[Burst] default duration = " + sDURATION);
            }
        }
        return sDURATION;
    }

    public static int getSupportedBurstShotMaxCount(boolean isInternal, boolean invalidate, boolean isRearCamera) {
        if (!isRearCamera) {
            return 20;
        }
        if (invalidate || sMAX_COUNT == 0) {
            int i = (!isSupportedFastShot() || getSupportedHal() == 2) ? sBURST_SHOT_MAX_COUNT_VALUE_NOT_SUPPORTED_FASTSHOT : sBURST_SHOT_MAX_COUNT_VALUE;
            sMAX_COUNT = i;
            if (sMAX_COUNT >= CameraConstants.BURST_CHECK_BIT) {
                int intCount = (int) Math.floor((double) ((sMAX_COUNT - CameraConstants.BURST_CHECK_BIT) / 10000));
                int extCount = sMAX_COUNT - ((intCount * 10000) + CameraConstants.BURST_CHECK_BIT);
                if (isInternal) {
                    i = intCount;
                } else {
                    i = extCount;
                }
                sMAX_COUNT = i;
                CamLog.m3d(CameraConstants.TAG, "[Burst] Internal max count = " + intCount + ", External max count = " + extCount);
            }
        }
        return sMAX_COUNT;
    }

    public static boolean multiviewFrontRearWideSupported() {
        if (getCameraTypeFront() == 2 && getCameraTypeRear() == 1) {
            return true;
        }
        return false;
    }

    public static boolean isSupportedFingerPrintMode(Context c) {
        if (sIsSupportedFingerprintMode == -1) {
            int i;
            if ("key".equals(System.getProperty("ro.lge.fingerprint.mode"))) {
                i = 1;
            } else {
                i = 0;
            }
            sIsSupportedFingerprintMode = i;
        }
        if (sIsSupportedFingerprintMode == 1) {
            return true;
        }
        return false;
    }

    public static boolean isFingerPrintShotEnabled(Context c) {
        if (sIsFPSensorExists == -1) {
            int i;
            if (c.getPackageManager().hasSystemFeature("android.hardware.fingerprint")) {
                i = 1;
            } else {
                i = 0;
            }
            sIsFPSensorExists = i;
        }
        if (sIsFPSensorExists == -1 || sIsFPSensorExists == 0) {
            return false;
        }
        if (Global.getInt(c.getContentResolver(), "shortcutkey_take_photo_enabled", 0) == 1) {
            return true;
        }
        return false;
    }

    public static boolean isSignatureSupported(Context context) {
        return ModelProperties.getRawAppTier() >= 3 && !ModelProperties.isTablet(context) && ConfigurationUtil.sSIGNATURE_SUPPORTED && PackageUtil.isLGGalleryInstalled(context);
    }

    public static boolean isSupportedHotKeyRecording(Context c) {
        if (sIsHotKeyRecordingSupported != -1) {
            if (sIsHotKeyRecordingSupported != 0) {
                return true;
            }
            return false;
        } else if (ConfigurationUtil.sHOT_KEY_RECORDING_SUPPORTED) {
            Class<?> configClass = integer.class;
            if (configClass != null) {
                try {
                    Field field = configClass.getField("config_supportHotKeyMode");
                    if (field != null) {
                        sIsHotKeyRecordingSupported = c.getResources().getInteger(field.getInt(c));
                        if (sIsHotKeyRecordingSupported == 0) {
                            return false;
                        }
                        return true;
                    }
                } catch (Exception e) {
                    CamLog.m5e(CameraConstants.TAG, "cannot find hot key config, message = " + e.getMessage());
                }
            }
            return false;
        } else {
            sIsHotKeyRecordingSupported = 0;
            return false;
        }
    }

    public static boolean isLivePhotoSupported() {
        return (ModelProperties.isFakeMode() || ModelProperties.getRawAppTier() < 5 || ModelProperties.isJoanRenewal()) ? false : true;
    }

    public static boolean isSupportedOpticZoom() {
        if (isSupportedInAndOutZoom() && ConfigurationUtil.sOPTIC_ZOOM_SUPPORTED >= 1) {
            return true;
        }
        return false;
    }

    public static boolean isSameResolutionOpticZoom() {
        return ConfigurationUtil.sOPTIC_ZOOM_SUPPORTED != 2;
    }

    public static boolean isSupportedCollageRecording() {
        return ConfigurationUtil.sAPP_TIER == 5;
    }

    public static boolean isSupportedShortcut() {
        return VERSION.SDK_INT >= 25;
    }

    public static boolean isNeedToShowInitialManualModeDialog(Context c) {
        if (ModelProperties.getCarrierCode() == 4 && isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA) && !SharedPreferenceUtil.getManualModeInitialGuide(c)) {
            return true;
        }
        return false;
    }

    public static boolean isSupportedModedownload() {
        return !ModelProperties.isJoanRenewal() && ModelProperties.getRawAppTier() >= 5 && PackageUtil.isLGSmartWorldPreloaded();
    }

    public static boolean isSupportedSticker() {
        return !ModelProperties.isFakeMode() && ConfigurationUtil.sSTICKER_SUPPORTED;
    }

    public static boolean isSupportedShotSolution() {
        return ConfigurationUtil.sSHOT_SOLUTION_SUPPORTED != 0;
    }

    public static boolean isSupportedManualNoiseReduction() {
        return isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA) && getSupportedHal() != 2;
    }

    public static boolean isSupportedSmartCam(Context context) {
        return ConfigurationUtil.sSMART_CAM_SUPPORTED && ModelProperties.checkInstalledLGLensApp(context);
    }

    public static boolean isSupportedLGLens(Context context) {
        if (!ConfigurationUtil.sLG_LENS_SUPPORTED) {
            return false;
        }
        String support = SystemProperties.get("lge.support.lglens");
        CamLog.m3d(CameraConstants.TAG, "LGlens supported : " + support);
        if ("support".equals(support)) {
            return true;
        }
        return ModelProperties.checkInstalledQLensApp(context);
    }

    public static boolean isSupportedQrCode(Context c) {
        if (isSupportedLGLens(c) || isSupportedGoogleLens() || ModelProperties.getAppTier() < 2) {
            return false;
        }
        return true;
    }

    public static boolean isGraphyDefaultOn() {
        return ConfigurationUtil.sGRAPHY_DEFAULT_ON;
    }

    public static boolean isSupportedBinning(int cameraId) {
        switch (cameraId) {
            case 0:
                if ((ConfigurationUtil.sBINNING_SUPPORTED & 1) == 0) {
                    return false;
                }
                return true;
            case 2:
                if ((ConfigurationUtil.sBINNING_SUPPORTED & 2) == 0) {
                    return false;
                }
                return true;
            default:
                return false;
        }
    }

    public static boolean isSupportedBinning() {
        return ConfigurationUtil.sBINNING_SUPPORTED > 0 && ConfigurationUtil.sBINNING_SUPPORTED != 4;
    }

    public static boolean isBinningAllSupported() {
        return isSupportedBinning(0) && isSupportedBinning(2);
    }

    public static int checkBinningType() {
        if (ConfigurationUtil.sBINNING_SUPPORTED > 4) {
            return 2;
        }
        if (ConfigurationUtil.sBINNING_SUPPORTED > 0) {
            return 1;
        }
        return 0;
    }

    public static boolean isUseSuperBright() {
        return ConfigurationUtil.sUSE_SUPER_BRIGHT;
    }

    public static boolean isSupportedHDR10() {
        return ConfigurationUtil.sHDR10_SUPPORTED;
    }

    public static boolean enableSlowMotionVideoSizeMenu() {
        return CamcorderProfileEx.hasProfile(0, 10013);
    }

    public static boolean isQlensOCRSupported(Context context) {
        return ModelProperties.getRawAppTier() >= 5 && SystemProperties.get("ro.build.target_country").equalsIgnoreCase("KR");
    }

    public static boolean isSupportedARStickers() {
        return ConfigurationUtil.sGOOGLE_AR_STICKERS_SUPPORTED;
    }

    public static boolean isSupportedGoogleLens() {
        return ConfigurationUtil.sGOOGLE_LENS_SUPPORTED;
    }

    public static boolean isGoogleLensDefault() {
        return ConfigurationUtil.sGOOGLE_LENS_DEFAULT;
    }

    public static void checkAuClude(Context context) {
        if (ModelProperties.getCarrierCode() == 7 && sCheckAuClude == 0) {
            ApplicationInfo info = null;
            try {
                info = context.getPackageManager().getApplicationInfo("com.kddi.android.auclouduploader", 128);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            sCheckAuClude = 1;
            if (info != null) {
                sCheckAuClude = 2;
            }
        }
    }

    public static boolean isSupportedAuClude() {
        return sCheckAuClude == 2;
    }

    public static boolean hideNaviBarWhileRecording() {
        return ConfigurationUtil.sHIDE_NAVI_BAR_WHILE_RECORDING;
    }

    public static boolean isCineVideoAvailable(Context context) {
        if (!ConfigurationUtil.containsMode(CameraConstants.MODE_CINEMA)) {
            return false;
        }
        if (AppControlUtil.isPowerSaveMaximumModeOn(context) && isSupportedHDR10()) {
            return false;
        }
        return true;
    }
}
