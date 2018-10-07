package com.lge.camera.constants;

import android.content.Context;
import com.lge.camera.constants.ConfigurationUtilBase.ItemRunnable;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CheckStatusManager;
import com.lge.camera.util.PackageUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConfigurationUtil extends ConfigurationParsingUtil {
    public static final String BATTERY_LEVEL_CONSTRAINT_NEEDED = "battery_level_constraint_needed";
    public static final String BEAUTYSHOT_SUPPORTED = "beautyshot_supported";
    public static final String BEAUTY_VALUES = "beauty_values";
    public static final String BINNING_SUPPORTED = "binning_supported";
    public static final String BURST_SHOT_PER_SECOND = "burst_shot_per_second";
    public static final String BURST_SHOT_PER_SECOND_FRONT = "burst_shot_per_second_front";
    public static final String CAMERA_ROLL_SUPPORTED = "camera_roll_supported";
    public static final String CAMERA_TYPE_FRONT = "camera_type_front";
    public static final int CAMERA_TYPE_FRONT_ONE_NORMAL = 0;
    public static final int CAMERA_TYPE_FRONT_ONE_WIDE = 2;
    public static final int CAMERA_TYPE_FRONT_TWO_NORMAL_WIDE = 1;
    public static final String CAMERA_TYPE_REAR = "camera_type_rear";
    public static final int CAMERA_TYPE_REAR_ONE_NORMAL = 0;
    public static final int CAMERA_TYPE_REAR_TWO_NORMAL_WIDE = 1;
    public static final String CROP_ANGLE_ZOOM_LEVEL = "crop_zoom_level";
    public static final String FILM_EMULATOR_SUPPORTED = "film_emulator_supported";
    public static final String FILM_STRENGTH_SUPPORTED = "film_strength_supported";
    public static final String FLASH_DISABLE_BATTERY_LEVEL = "flash_disable_battery_level";
    public static final String FRONT_OUTFOCUS_PREVIEW_SIZE = "front_outfocus_preview_size";
    public static final String FRONT_OUTFOCUS_SUPPORTED = "front_outfocus_supported";
    public static final String FRONT_SHUTTERLESS_SUPPORTED = "front_shutterless_supported";
    public static final String GAPLESS_LOOP_RECORDING_SUPPORTED = "gapless_loop_recording_supported";
    public static final String GOOGLE_AR_STICKERS_SUPPORTED = "google_ar_stickers_supported";
    public static final String GOOGLE_LENS_DEFAULT = "google_lens_default";
    public static final String GOOGLE_LENS_SUPPORTED = "google_lens_supported";
    public static final String GRAPHY_DEFAULT_ON = "graphy_default_on";
    public static final String HAL_SUPPORTED = "hal_supported";
    public static final String HDMI_MHL_SUPPORTED = "hdmi_mhl_supported";
    public static final String HDR10_SUPPORTED = "hdr10_supported";
    public static final String HIDE_NAVI_BAR_WHILE_RECORDING = "hide_navibar_while_recording";
    public static final String HOT_KEY_RECORDING_SUPPORTED = "hot_key_recording_supported";
    public static final String IS_WIDE_CAMERA_DEFAULT = "wide_camera_default";
    public static final String JOAN_RENEWAL_SUPPORTED = "joan_renewal_supported";
    public static final String LG_LENS_SUPPORTED = "lg_lens_supported";
    public static final String LIGHT_FRAME_SUPPORTED = "light_frame_supported";
    public static final String MODE_DOWNLOAD_SUPPORTED = "mode_download_supported";
    public static final String MODE_FRONT_SUPPORTED = "mode_front_supported";
    public static final String MODE_REAR_SUPPORTED = "mode_rear_supported";
    public static final String MOTIONQUICKVIEWER_SUPPORTED = "motionquickviewer_supported";
    public static final String MTK_CHIPSET = "MTK_chipset";
    public static final String OPTIC_ZOOM_SUPPORTED = "optic_zoom_supported";
    public static final int OUTFOCUS_ARC = 1;
    public static final int OUTFOCUS_NOT_SUPPORTED = 0;
    public static final String PANORAMA_360_DIST_COR_VALUE = "panorama_360_dist_cor_value";
    public static final String PANORAMA_PREVIEW_SIZE = "panorama_preview_size";
    public static final String REAR_OUTFOCUS_SUPPORTED = "rear_outfocus_supported";
    public static final String RELIGHTING_SUPPORTED = "relighting_supported";
    public static final String RENESAS_ISP = "use_renesas_ISP";
    public static final int SHOT_SOLUTION_FULL = 1;
    public static final int SHOT_SOLUTION_NONE = 0;
    public static final int SHOT_SOLUTION_NR_ONLY = 2;
    public static final String SHOT_SOLUTION_SUPPORTED = "shot_solution_supported";
    public static final String SIGNATURE_SUPPORTED = "signature_supported";
    public static final String SMART_CAM_SUPPORTED = "smart_cam_supported";
    public static final String STICKER_SUPPORTED = "sticker_supported";
    public static final String SWITCHING_ANIMTAION_SUPPORTED = "switching_animation_supported";
    public static final String TIMER_HELPER_SUPPORTED = "timer_helper_supported";
    public static final String USE_SUPER_BRIGHT = "use_super_bright";
    public static final String VOICE_SHUTTER_SUPPORTED = "voice_shutter_supported";
    public static boolean sBATTERY_LEVEL_CONSTRAINT_NEEDED_VALUE = false;
    public static int sBEAUTYSHOT_SUPPORTED_VALUE = 1;
    public static int sBEAUTY_MAX = 0;
    public static int sBEAUTY_STEP = 0;
    public static int sBINNING_SUPPORTED = 0;
    public static int sBURST_SHOT_DURATION = -1;
    public static int sBURST_SHOT_DURATION_FRONT = 101800180;
    public static boolean sCAMERA_ROLL_SUPPORTED = false;
    public static int sCROP_ANGLE_ZOOM_LEVEL = 0;
    public static int sCameraTypeFront = 0;
    public static int sCameraTypeRear = 0;
    public static boolean sFILM_EMULATOR_SUPPORTED = false;
    public static boolean sFILM_STRENGTH_SUPPORTED = true;
    public static int sFLASH_DISABLE_BATTERY_LEVEL_VALUE = 15;
    public static String[] sFRONT_OUTFOCUS_PREVIEW_SIZE = null;
    public static int sFRONT_OUTFOCUS_TYPE = 0;
    public static boolean sFRONT_SHUTTERLESS_SUPPORTED = true;
    public static boolean sGAPLESS_LOOP_RECORDING_SUPPORTED = true;
    public static boolean sGOOGLE_AR_STICKERS_SUPPORTED = false;
    public static boolean sGOOGLE_LENS_DEFAULT = true;
    public static boolean sGOOGLE_LENS_SUPPORTED = true;
    public static boolean sGRAPHY_DEFAULT_ON = true;
    public static int sHAL_SUPPORTED_VALUE = 0;
    public static boolean sHDMI_MHL_SUPPORTED_VALUE = true;
    public static boolean sHDR10_SUPPORTED = false;
    public static boolean sHIDE_NAVI_BAR_WHILE_RECORDING = false;
    public static boolean sHOT_KEY_RECORDING_SUPPORTED = false;
    public static boolean sIS_WIDE_CAMERA_DEFAULT = false;
    public static boolean sJOAN_RENEWAL_SUPPORTED = false;
    public static boolean sLG_LENS_SUPPORTED = true;
    public static boolean sLIGHT_FRAME_SUPPORTED_VALUE = true;
    public static String[] sMODE_DOWNLOAD_SUPPORTED;
    public static String[] sMODE_FRONT_SUPPORTED_ITEMS;
    public static String[] sMODE_REAR_SUPPORTED_ITEMS;
    public static boolean sMOTIONQUICKVIEWER_SUPPORTED_VALUE = false;
    public static boolean sMTK_CHIPSET_VALUE = false;
    private static List<String> sModeList = null;
    public static int sOPTIC_ZOOM_SUPPORTED = 0;
    public static String[] sPANORAMA_360_DIST_COR_VALUE;
    public static String[] sPANORAMA_PREVIEW_SIZE_VALUE;
    public static int sREAR_OUTFOCUS_TYPE = 0;
    public static int sRELIGHTING_MAX = 0;
    public static int sRELIGHTING_STEP = 0;
    public static boolean sRELIGHTING_SUPPORTED_VALUE = false;
    public static boolean sRENESAS_ISP_VALUE = false;
    public static int sSHOT_SOLUTION_SUPPORTED = 0;
    public static boolean sSIGNATURE_SUPPORTED = true;
    public static boolean sSMART_CAM_SUPPORTED = false;
    public static boolean sSTICKER_SUPPORTED = false;
    public static boolean sSWITCHING_ANIMTAION_SUPPORTED = true;
    public static boolean sTIMER_HELPER_SUPPORTED_VALUE = true;
    public static boolean sUSE_SUPER_BRIGHT = false;
    public static boolean sVOICE_SHUTTER_SUPPORTED = true;

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$10 */
    static class C059210 extends ItemRunnable {
        C059210() {
        }

        public void run(String items) {
            ConfigurationUtil.sCameraTypeRear = ConfigurationUtilBase.getItemSupportedType(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$11 */
    static class C059311 extends ItemRunnable {
        C059311() {
        }

        public void run(String items) {
            ConfigurationUtil.sJOAN_RENEWAL_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$12 */
    static class C059412 extends ItemRunnable {
        C059412() {
        }

        public void run(String items) {
            ConfigurationUtil.sBEAUTYSHOT_SUPPORTED_VALUE = ConfigurationUtilBase.getItemSupportedType(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$13 */
    static class C059513 extends ItemRunnable {
        C059513() {
        }

        public void run(String items) {
            ConfigurationUtil.sRELIGHTING_SUPPORTED_VALUE = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$14 */
    static class C059614 extends ItemRunnable {
        C059614() {
        }

        public void run(String items) {
            ConfigurationUtil.sLIGHT_FRAME_SUPPORTED_VALUE = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$15 */
    static class C059715 extends ItemRunnable {
        C059715() {
        }

        public void run(String items) {
            ConfigurationUtil.sTIMER_HELPER_SUPPORTED_VALUE = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$16 */
    static class C059816 extends ItemRunnable {
        C059816() {
        }

        public void run(String items) {
            ConfigurationUtil.sVOICE_SHUTTER_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$17 */
    static class C059917 extends ItemRunnable {
        C059917() {
        }

        public void run(String items) {
            ConfigurationUtil.sFILM_EMULATOR_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$18 */
    static class C060018 extends ItemRunnable {
        C060018() {
        }

        public void run(String items) {
            ConfigurationUtil.sFILM_STRENGTH_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$19 */
    static class C060119 extends ItemRunnable {
        C060119() {
        }

        public void run(String items) {
            ConfigurationUtil.sFRONT_SHUTTERLESS_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$1 */
    static class C06021 extends ItemRunnable {
        C06021() {
        }

        public void run(String items) {
            ConfigurationUtil.sMODE_REAR_SUPPORTED_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$20 */
    static class C060320 extends ItemRunnable {
        C060320() {
        }

        public void run(String items) {
            ConfigurationUtil.sGAPLESS_LOOP_RECORDING_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$21 */
    static class C060421 extends ItemRunnable {
        C060421() {
        }

        public void run(String items) {
            ConfigurationUtil.sMOTIONQUICKVIEWER_SUPPORTED_VALUE = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$22 */
    static class C060522 extends ItemRunnable {
        C060522() {
        }

        public void run(String items) {
            ConfigurationUtil.sBURST_SHOT_DURATION_FRONT = ConfigurationUtilBase.getItemSupportedType(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$23 */
    static class C060623 extends ItemRunnable {
        C060623() {
        }

        public void run(String items) {
            ConfigurationUtil.sBURST_SHOT_DURATION = ConfigurationUtilBase.getItemSupportedType(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$24 */
    static class C060724 extends ItemRunnable {
        C060724() {
        }

        public void run(String items) {
            ConfigurationUtil.sCROP_ANGLE_ZOOM_LEVEL = ConfigurationUtilBase.getItemSupportedType(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$25 */
    static class C060825 extends ItemRunnable {
        C060825() {
        }

        public void run(String items) {
            ConfigurationUtil.sOPTIC_ZOOM_SUPPORTED = ConfigurationUtilBase.getItemSupportedType(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$26 */
    static class C060926 extends ItemRunnable {
        C060926() {
        }

        public void run(String items) {
            String[] values = ConfigurationUtilBase.getStringArray(items);
            if (values.length == 4) {
                ConfigurationUtil.sBEAUTY_MAX = Integer.parseInt(values[0]);
                ConfigurationUtil.sBEAUTY_STEP = Integer.parseInt(values[1]);
                ConfigurationUtil.sRELIGHTING_MAX = Integer.parseInt(values[2]);
                ConfigurationUtil.sRELIGHTING_STEP = Integer.parseInt(values[3]);
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "BEAUTY_VALUES load failed!");
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$27 */
    static class C061027 extends ItemRunnable {
        C061027() {
        }

        public void run(String items) {
            ConfigurationUtil.sHOT_KEY_RECORDING_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$28 */
    static class C061128 extends ItemRunnable {
        C061128() {
        }

        public void run(String items) {
            ConfigurationUtil.sSWITCHING_ANIMTAION_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$29 */
    static class C061229 extends ItemRunnable {
        C061229() {
        }

        public void run(String items) {
            ConfigurationUtil.sGRAPHY_DEFAULT_ON = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$2 */
    static class C06132 extends ItemRunnable {
        C06132() {
        }

        public void run(String items) {
            ConfigurationUtil.sMODE_FRONT_SUPPORTED_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$30 */
    static class C061430 extends ItemRunnable {
        C061430() {
        }

        public void run(String items) {
            ConfigurationUtil.sSTICKER_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$31 */
    static class C061531 extends ItemRunnable {
        C061531() {
        }

        public void run(String items) {
            ConfigurationUtil.sSHOT_SOLUTION_SUPPORTED = ConfigurationUtilBase.getItemSupportedType(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$32 */
    static class C061632 extends ItemRunnable {
        C061632() {
        }

        public void run(String items) {
            ConfigurationUtil.sSMART_CAM_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$33 */
    static class C061733 extends ItemRunnable {
        C061733() {
        }

        public void run(String items) {
            ConfigurationUtil.sSIGNATURE_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$34 */
    static class C061834 extends ItemRunnable {
        C061834() {
        }

        public void run(String items) {
            ConfigurationUtil.sLG_LENS_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$35 */
    static class C061935 extends ItemRunnable {
        C061935() {
        }

        public void run(String items) {
            ConfigurationUtil.sGOOGLE_LENS_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$36 */
    static class C062036 extends ItemRunnable {
        C062036() {
        }

        public void run(String items) {
            ConfigurationUtil.sGOOGLE_LENS_DEFAULT = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$37 */
    static class C062137 extends ItemRunnable {
        C062137() {
        }

        public void run(String items) {
            ConfigurationUtil.sGOOGLE_AR_STICKERS_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$38 */
    static class C062238 extends ItemRunnable {
        C062238() {
        }

        public void run(String items) {
            ConfigurationUtil.sBINNING_SUPPORTED = ConfigurationUtilBase.getItemSupportedType(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$39 */
    static class C062339 extends ItemRunnable {
        C062339() {
        }

        public void run(String items) {
            ConfigurationUtil.sUSE_SUPER_BRIGHT = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$40 */
    static class C062540 extends ItemRunnable {
        C062540() {
        }

        public void run(String items) {
            ConfigurationUtil.sFRONT_OUTFOCUS_TYPE = ConfigurationUtilBase.getItemSupportedType(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$41 */
    static class C062641 extends ItemRunnable {
        C062641() {
        }

        public void run(String items) {
            ConfigurationUtil.sREAR_OUTFOCUS_TYPE = ConfigurationUtilBase.getItemSupportedType(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$42 */
    static class C062742 extends ItemRunnable {
        C062742() {
        }

        public void run(String items) {
            ConfigurationUtil.sFRONT_OUTFOCUS_PREVIEW_SIZE = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$43 */
    static class C062843 extends ItemRunnable {
        C062843() {
        }

        public void run(String items) {
            ConfigurationUtil.sHDR10_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$44 */
    static class C062944 extends ItemRunnable {
        C062944() {
        }

        public void run(String items) {
            ConfigurationUtil.sHIDE_NAVI_BAR_WHILE_RECORDING = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$45 */
    static class C063045 extends ItemRunnable {
        C063045() {
        }

        public void run(String items) {
            ConfigurationUtil.sCAMERA_ROLL_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$46 */
    static class C063146 extends ItemRunnable {
        C063146() {
        }

        public void run(String items) {
            ConfigurationUtil.sHAL_SUPPORTED_VALUE = ConfigurationUtilBase.getItemSupportedType(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$47 */
    static class C063247 extends ItemRunnable {
        C063247() {
        }

        public void run(String items) {
            ConfigurationUtil.sBATTERY_LEVEL_CONSTRAINT_NEEDED_VALUE = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$48 */
    static class C063348 extends ItemRunnable {
        C063348() {
        }

        public void run(String items) {
            ConfigurationUtil.sFLASH_DISABLE_BATTERY_LEVEL_VALUE = ConfigurationUtilBase.getItemSupportedType(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$49 */
    static class C063449 extends ItemRunnable {
        C063449() {
        }

        public void run(String items) {
            ConfigurationUtil.sIS_WIDE_CAMERA_DEFAULT = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$4 */
    static class C06354 extends ItemRunnable {
        C06354() {
        }

        public void run(String items) {
            ConfigurationUtil.sPANORAMA_360_DIST_COR_VALUE = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$5 */
    static class C06365 extends ItemRunnable {
        C06365() {
        }

        public void run(String items) {
            ConfigurationUtil.sMODE_DOWNLOAD_SUPPORTED = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$6 */
    static class C06376 extends ItemRunnable {
        C06376() {
        }

        public void run(String items) {
            ConfigurationUtil.sHDMI_MHL_SUPPORTED_VALUE = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$7 */
    static class C06387 extends ItemRunnable {
        C06387() {
        }

        public void run(String items) {
            ConfigurationUtil.sMTK_CHIPSET_VALUE = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$8 */
    static class C06398 extends ItemRunnable {
        C06398() {
        }

        public void run(String items) {
            ConfigurationUtil.sRENESAS_ISP_VALUE = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationUtil$9 */
    static class C06409 extends ItemRunnable {
        C06409() {
        }

        public void run(String items) {
            ConfigurationUtil.sCameraTypeFront = ConfigurationUtilBase.getItemSupportedType(items);
        }
    }

    private static void addModeSupported(final Context context) {
        sSupportedConfigMap.put(MODE_REAR_SUPPORTED, new C06021());
        sSupportedConfigMap.put(MODE_FRONT_SUPPORTED, new C06132());
        sSupportedConfigMap.put(PANORAMA_PREVIEW_SIZE, new ItemRunnable() {
            public void run(String items) {
                ConfigurationUtil.sPANORAMA_PREVIEW_SIZE_VALUE = ConfigurationUtil.parsePanoramaPreviewSize(context, ConfigurationUtilBase.getStringArray(items));
            }
        });
        sSupportedConfigMap.put(PANORAMA_360_DIST_COR_VALUE, new C06354());
        sSupportedConfigMap.put(MODE_DOWNLOAD_SUPPORTED, new C06365());
    }

    private static String[] parsePanoramaPreviewSize(Context context, String[] previewSizes) {
        if (previewSizes == null || previewSizes.length == 0) {
            return null;
        }
        if (previewSizes.length == 1) {
            return ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, previewSizes, false, true);
        }
        return ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, previewSizes, false, true);
    }

    private static void addDeviceSupported() {
        sSupportedConfigMap.put(HDMI_MHL_SUPPORTED, new C06376());
        sSupportedConfigMap.put(MTK_CHIPSET, new C06387());
        sSupportedConfigMap.put(RENESAS_ISP, new C06398());
        sSupportedConfigMap.put(CAMERA_TYPE_FRONT, new C06409());
        sSupportedConfigMap.put(CAMERA_TYPE_REAR, new C059210());
    }

    private static void addFeatureSupported() {
        sSupportedConfigMap.put(JOAN_RENEWAL_SUPPORTED, new C059311());
        sSupportedConfigMap.put(BEAUTYSHOT_SUPPORTED, new C059412());
        sSupportedConfigMap.put(RELIGHTING_SUPPORTED, new C059513());
        sSupportedConfigMap.put(LIGHT_FRAME_SUPPORTED, new C059614());
        sSupportedConfigMap.put(TIMER_HELPER_SUPPORTED, new C059715());
        sSupportedConfigMap.put(VOICE_SHUTTER_SUPPORTED, new C059816());
        sSupportedConfigMap.put(FILM_EMULATOR_SUPPORTED, new C059917());
        sSupportedConfigMap.put(FILM_STRENGTH_SUPPORTED, new C060018());
        sSupportedConfigMap.put(FRONT_SHUTTERLESS_SUPPORTED, new C060119());
        sSupportedConfigMap.put(GAPLESS_LOOP_RECORDING_SUPPORTED, new C060320());
        sSupportedConfigMap.put(MOTIONQUICKVIEWER_SUPPORTED, new C060421());
        sSupportedConfigMap.put(BURST_SHOT_PER_SECOND_FRONT, new C060522());
        sSupportedConfigMap.put(BURST_SHOT_PER_SECOND, new C060623());
        sSupportedConfigMap.put(CROP_ANGLE_ZOOM_LEVEL, new C060724());
        sSupportedConfigMap.put(OPTIC_ZOOM_SUPPORTED, new C060825());
        sSupportedConfigMap.put(BEAUTY_VALUES, new C060926());
        sSupportedConfigMap.put(HOT_KEY_RECORDING_SUPPORTED, new C061027());
        sSupportedConfigMap.put(SWITCHING_ANIMTAION_SUPPORTED, new C061128());
        sSupportedConfigMap.put(GRAPHY_DEFAULT_ON, new C061229());
        sSupportedConfigMap.put(STICKER_SUPPORTED, new C061430());
        sSupportedConfigMap.put(SHOT_SOLUTION_SUPPORTED, new C061531());
        sSupportedConfigMap.put(SMART_CAM_SUPPORTED, new C061632());
        sSupportedConfigMap.put(SIGNATURE_SUPPORTED, new C061733());
        sSupportedConfigMap.put(LG_LENS_SUPPORTED, new C061834());
        sSupportedConfigMap.put(GOOGLE_LENS_SUPPORTED, new C061935());
        sSupportedConfigMap.put(GOOGLE_LENS_DEFAULT, new C062036());
        sSupportedConfigMap.put(GOOGLE_AR_STICKERS_SUPPORTED, new C062137());
        sSupportedConfigMap.put(BINNING_SUPPORTED, new C062238());
        sSupportedConfigMap.put(USE_SUPER_BRIGHT, new C062339());
        sSupportedConfigMap.put(FRONT_OUTFOCUS_SUPPORTED, new C062540());
        sSupportedConfigMap.put(REAR_OUTFOCUS_SUPPORTED, new C062641());
        sSupportedConfigMap.put(FRONT_OUTFOCUS_PREVIEW_SIZE, new C062742());
        sSupportedConfigMap.put(HDR10_SUPPORTED, new C062843());
        sSupportedConfigMap.put(HIDE_NAVI_BAR_WHILE_RECORDING, new C062944());
        sSupportedConfigMap.put(CAMERA_ROLL_SUPPORTED, new C063045());
    }

    private static void addHalSupported() {
        sSupportedConfigMap.put(HAL_SUPPORTED, new C063146());
    }

    private static void addExceptionScenarioSupported() {
        sSupportedConfigMap.put(BATTERY_LEVEL_CONSTRAINT_NEEDED, new C063247());
        sSupportedConfigMap.put(FLASH_DISABLE_BATTERY_LEVEL, new C063348());
    }

    private static void addGoogleLensSupported(Context context) {
        boolean z = sGOOGLE_LENS_SUPPORTED && PackageUtil.isGoogleLensInstalled(context);
        sGOOGLE_LENS_SUPPORTED = z;
        CamLog.m3d(CameraConstants.TAG, "Google lens supported : " + sGOOGLE_LENS_SUPPORTED);
        CamLog.m3d(CameraConstants.TAG, "Google lens default : " + sGOOGLE_LENS_DEFAULT);
        if (sLG_LENS_SUPPORTED && sGOOGLE_LENS_SUPPORTED) {
            String type = CheckStatusManager.getSystemSettingLensType(context.getContentResolver());
            if (type == null || "".equals(type)) {
                CheckStatusManager.setSystemSettingLensType(context.getContentResolver(), sGOOGLE_LENS_DEFAULT ? CameraConstants.GOOGLELENS : CameraConstants.QLENS);
            }
        }
    }

    private static void addGoogleARStickersSupported(Context context) {
        boolean z = sGOOGLE_AR_STICKERS_SUPPORTED && PackageUtil.isGoogleLensInstalled(context);
        sGOOGLE_AR_STICKERS_SUPPORTED = z;
        CamLog.m3d(CameraConstants.TAG, "Google AR Stickers supported : " + sGOOGLE_AR_STICKERS_SUPPORTED);
    }

    private static void addSmartworldSupported(Context context) {
        PackageUtil.checkLGSmartWorldPreloaded(context);
    }

    private static void addWideCameraDefault() {
        sSupportedConfigMap.put(IS_WIDE_CAMERA_DEFAULT, new C063449());
    }

    protected static synchronized void makeConfigMap(Context context, boolean isOnlyHal) {
        synchronized (ConfigurationUtil.class) {
            CamLog.m3d(CameraConstants.TAG, "make config map START");
            if (isOnlyHal) {
                if (sLoadHalConfigDone) {
                    CamLog.m3d(CameraConstants.TAG, "make config map EXIT");
                } else {
                    if (sSupportedConfigMap == null) {
                        sSupportedConfigMap = new HashMap();
                        addHalSupported();
                    } else if (((ItemRunnable) sSupportedConfigMap.get(HAL_SUPPORTED)) == null) {
                        addHalSupported();
                    }
                    addWideCameraDefault();
                    CamLog.m3d(CameraConstants.TAG, "make config map END");
                }
            } else if (sLoadConfigDone) {
                CamLog.m3d(CameraConstants.TAG, "make config map EXIT");
            } else {
                if (sSupportedConfigMap == null) {
                    sSupportedConfigMap = new HashMap();
                }
                ConfigurationPictureSizeUtil.addPictureSizeRearSupported(context, sSupportedConfigMap);
                ConfigurationPictureSizeUtil.addPictureSizeFrontSupported(context, sSupportedConfigMap);
                ConfigurationVideoSizeUtil.addVideoSizeRearSupported(context, sSupportedConfigMap);
                ConfigurationVideoSizeUtil.addVideoSizeFrontSupported(context, sSupportedConfigMap);
                ConfigurationManualUtil.addManualSupported(sSupportedConfigMap);
                ConfigurationHALUtil.addHALSupported(sSupportedConfigMap);
                addModeSupported(context);
                addDeviceSupported();
                addFeatureSupported();
                addSmartworldSupported(context);
                if (!sLoadHalConfigDone) {
                    addHalSupported();
                    addWideCameraDefault();
                }
                addExceptionScenarioSupported();
                CamLog.m3d(CameraConstants.TAG, "make config map END");
            }
        }
    }

    public static void setScreenResolution(Context context) {
        int[] lcdSize = Utils.getLCDsize(context, true);
        if (lcdSize != null && ConfigurationResolutionUtilBase.isScreenResolutionChanged(context)) {
            CamLog.m3d(CameraConstants.TAG, "Screen resolution has changed, resolution = " + lcdSize[1]);
            SharedPreferenceUtil.setScreenResolution(context, lcdSize[1]);
            initializeStaticValues();
        }
    }

    protected static void initializeStaticValues() {
        RatioCalcUtil.sQuickButtonWidth = 1;
        RatioCalcUtil.sLongLCDModelTopMargin_1x1 = -1;
        RatioCalcUtil.sLongLCDModelTopMargin_4x3 = -1;
    }

    public static void setConfiguration(Context context) {
        boolean isScreenResolutionChanged = ConfigurationResolutionUtilBase.isScreenResolutionChanged(context);
        if (!sLoadConfigDone || isScreenResolutionChanged) {
            setScreenResolution(context);
            ConfigurationParsingUtil.makeAppTierMap(context);
            makeConfigMap(context, false);
            ConfigurationParsingUtil.parseConfig(context, false);
            addGoogleLensSupported(context);
            addGoogleARStickersSupported(context);
            ConfigurationParsingUtil.modifyConfiguration(context);
        }
    }

    public static void setConfigurationHalOnly(Context context) {
        if (!sLoadHalConfigDone) {
            makeConfigMap(context, true);
            ConfigurationParsingUtil.parseConfig(context, true);
        }
    }

    public static boolean containsMode(String shotMode) {
        if (sModeList == null) {
            sModeList = Arrays.asList(sMODE_REAR_SUPPORTED_ITEMS);
        }
        if (sModeList == null || sModeList.size() == 0) {
            return false;
        }
        return sModeList.contains(shotMode);
    }
}
