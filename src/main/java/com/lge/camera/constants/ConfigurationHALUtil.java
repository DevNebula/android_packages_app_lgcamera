package com.lge.camera.constants;

import com.lge.camera.constants.ConfigurationUtilBase.ItemRunnable;
import java.util.HashMap;

public class ConfigurationHALUtil extends ConfigurationUtilBase {
    public static final String AE_FOCUS_SUPPORTED = "AE_focus_supported";
    public static final String FAST_SHOT_SUPPORTED = "fast_shot_supported";
    public static final String HDR_SUPPORTED = "hdr_supported";
    public static final String LONG_SHOT_SUPPORTED = "long_shot_supported";
    public static final String MORHPO_NIGHT_SHOT_SUPPORTED = "morpho_night_shot_supported";
    public static final String SECOND_CAMERA_MAX_ZOOM_LEVEL = "second_camera_max_zoom_level";
    public static final String STEADY_CAMERA_SUPPORTED = "steady_camera_supported";
    public static final String SUPER_ZOOM_SUPPORTED = "super_zoom_supported";
    public static final String VIDEO_HDR_SUPPORTED = "video_hdr_supported";
    public static final String ZSL_MANUAL_SUPPORTED = "zsl_manual_supported";
    public static final String ZSL_SUPPORTED = "zsl_supported";
    public static boolean sAE_FOCUS_SUPPORTED_VALUE = true;
    public static int sFAST_SHOT_SUPPORTED = 0;
    public static int sHDR_SUPPORTED_VALUE = 1;
    public static boolean sLONG_SHOT_SUPPORTED_VALUE = false;
    public static boolean sMORHPO_NIGHT_SHOT_SUPPORTED_VALUE = true;
    public static int sSECOND_CAMERA_MAX_ZOOM_LEVEL = 0;
    public static int sSTEADY_CAMERA_SUPPORTED = 0;
    public static boolean sSUPER_ZOOM_SUPPORTED_VALUE = false;
    public static boolean sVIDEO_HDR_SUPPORTED = false;
    public static boolean sZSL_MANUAL_SUPPORTED = true;
    public static int sZSL_SUPPORTED_VALUE = 1;

    /* renamed from: com.lge.camera.constants.ConfigurationHALUtil$10 */
    static class C055910 extends ItemRunnable {
        C055910() {
        }

        public void run(String items) {
            ConfigurationHALUtil.sFAST_SHOT_SUPPORTED = ConfigurationUtilBase.getItemSupportedType(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationHALUtil$11 */
    static class C056011 extends ItemRunnable {
        C056011() {
        }

        public void run(String items) {
            ConfigurationHALUtil.sZSL_MANUAL_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationHALUtil$1 */
    static class C05611 extends ItemRunnable {
        C05611() {
        }

        public void run(String items) {
            ConfigurationHALUtil.sZSL_SUPPORTED_VALUE = ConfigurationUtilBase.getItemSupportedType(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationHALUtil$2 */
    static class C05622 extends ItemRunnable {
        C05622() {
        }

        public void run(String items) {
            ConfigurationHALUtil.sAE_FOCUS_SUPPORTED_VALUE = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationHALUtil$3 */
    static class C05633 extends ItemRunnable {
        C05633() {
        }

        public void run(String items) {
            ConfigurationHALUtil.sLONG_SHOT_SUPPORTED_VALUE = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationHALUtil$4 */
    static class C05644 extends ItemRunnable {
        C05644() {
        }

        public void run(String items) {
            ConfigurationHALUtil.sMORHPO_NIGHT_SHOT_SUPPORTED_VALUE = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationHALUtil$5 */
    static class C05655 extends ItemRunnable {
        C05655() {
        }

        public void run(String items) {
            ConfigurationHALUtil.sHDR_SUPPORTED_VALUE = ConfigurationUtilBase.getItemSupportedType(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationHALUtil$6 */
    static class C05666 extends ItemRunnable {
        C05666() {
        }

        public void run(String items) {
            ConfigurationHALUtil.sVIDEO_HDR_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationHALUtil$7 */
    static class C05677 extends ItemRunnable {
        C05677() {
        }

        public void run(String items) {
            ConfigurationHALUtil.sSECOND_CAMERA_MAX_ZOOM_LEVEL = ConfigurationUtilBase.getItemSupportedType(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationHALUtil$8 */
    static class C05688 extends ItemRunnable {
        C05688() {
        }

        public void run(String items) {
            try {
                ConfigurationHALUtil.sSTEADY_CAMERA_SUPPORTED = ConfigurationUtilBase.getItemSupportedType(items);
            } catch (NumberFormatException e) {
                ConfigurationHALUtil.sSTEADY_CAMERA_SUPPORTED = ConfigurationUtilBase.isItemSupported(items) ? 2 : 0;
            }
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationHALUtil$9 */
    static class C05699 extends ItemRunnable {
        C05699() {
        }

        public void run(String items) {
            ConfigurationHALUtil.sSUPER_ZOOM_SUPPORTED_VALUE = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    public static void addHALSupported(HashMap<String, ItemRunnable> configMap) {
        configMap.put(ZSL_SUPPORTED, new C05611());
        configMap.put(AE_FOCUS_SUPPORTED, new C05622());
        configMap.put(LONG_SHOT_SUPPORTED, new C05633());
        configMap.put(MORHPO_NIGHT_SHOT_SUPPORTED, new C05644());
        configMap.put(HDR_SUPPORTED, new C05655());
        configMap.put(VIDEO_HDR_SUPPORTED, new C05666());
        configMap.put(SECOND_CAMERA_MAX_ZOOM_LEVEL, new C05677());
        configMap.put(STEADY_CAMERA_SUPPORTED, new C05688());
        configMap.put(SUPER_ZOOM_SUPPORTED, new C05699());
        configMap.put(FAST_SHOT_SUPPORTED, new C055910());
        configMap.put(ZSL_MANUAL_SUPPORTED, new C056011());
    }
}
