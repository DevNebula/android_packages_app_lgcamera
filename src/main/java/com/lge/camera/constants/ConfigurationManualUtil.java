package com.lge.camera.constants;

import com.lge.camera.constants.ConfigurationUtilBase.ItemRunnable;
import java.util.HashMap;

public class ConfigurationManualUtil extends ConfigurationUtilBase {
    public static final String GRAPHY_SUPPORTED = "graphy_supported";
    public static final String MANUAL_FOCUS_SUPPORTED = "manual_focus_supported";
    public static final String RAW_PICTURE_SAVING_SUPPORTED = "raw_picture_saving_supported";
    public static final String REAR_CURTAIN_FLASH_SUPPORTED = "rear_curtain_sync_supported";
    public static boolean sGRAPHY_SUPPORTED_VALUE = true;
    public static boolean sMANUAL_FOCUS_SUPPORTED = true;
    public static boolean sRAW_PICTURE_SAVING_SUPPORTED_VALUE = false;
    public static boolean sREAR_CURTAIN_FLASH_SUPPORTED = false;

    /* renamed from: com.lge.camera.constants.ConfigurationManualUtil$1 */
    static class C05701 extends ItemRunnable {
        C05701() {
        }

        public void run(String items) {
            ConfigurationManualUtil.sRAW_PICTURE_SAVING_SUPPORTED_VALUE = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationManualUtil$2 */
    static class C05712 extends ItemRunnable {
        C05712() {
        }

        public void run(String items) {
            ConfigurationManualUtil.sREAR_CURTAIN_FLASH_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationManualUtil$3 */
    static class C05723 extends ItemRunnable {
        C05723() {
        }

        public void run(String items) {
            ConfigurationManualUtil.sMANUAL_FOCUS_SUPPORTED = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationManualUtil$4 */
    static class C05734 extends ItemRunnable {
        C05734() {
        }

        public void run(String items) {
            ConfigurationManualUtil.sGRAPHY_SUPPORTED_VALUE = ConfigurationUtilBase.isItemSupported(items);
        }
    }

    public static void addManualSupported(HashMap<String, ItemRunnable> configMap) {
        configMap.put(RAW_PICTURE_SAVING_SUPPORTED, new C05701());
        configMap.put(REAR_CURTAIN_FLASH_SUPPORTED, new C05712());
        configMap.put(MANUAL_FOCUS_SUPPORTED, new C05723());
        configMap.put(GRAPHY_SUPPORTED, new C05734());
    }
}
