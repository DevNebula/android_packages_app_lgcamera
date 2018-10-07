package com.lge.camera.constants;

import android.content.Context;
import com.lge.camera.constants.ConfigurationUtilBase.ItemRunnable;
import java.util.HashMap;

public class ConfigurationPictureSizeUtil extends ConfigurationResolutionUtilBase {

    /* renamed from: com.lge.camera.constants.ConfigurationPictureSizeUtil$10 */
    static class C057410 extends ItemRunnable {
        C057410() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_BINNING_SUB_REAR_SUPPORTED_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationPictureSizeUtil$11 */
    static class C057511 extends ItemRunnable {
        C057511() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_FRONT_SUPPORTED_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationPictureSizeUtil$15 */
    static class C057915 extends ItemRunnable {
        C057915() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_SUB_FRONT_SUPPORTED_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationPictureSizeUtil$1 */
    static class C05831 extends ItemRunnable {
        C05831() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_REAR_SUPPORTED_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationPictureSizeUtil$5 */
    static class C05875 extends ItemRunnable {
        C05875() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_BINNING_REAR_SUPPORTED_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationPictureSizeUtil$6 */
    static class C05886 extends ItemRunnable {
        C05886() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_SUB_REAR_SUPPORTED_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    public static void addPictureSizeRearSupported(Context context, HashMap<String, ItemRunnable> configMap) {
        addPictureSizeRearNormalSupported(context, configMap);
        addPictureSizeRearWideSupported(context, configMap);
    }

    private static void addPictureSizeRearNormalSupported(final Context context, HashMap<String, ItemRunnable> configMap) {
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PICTURESIZE_REAR_SUPPORTED, new C05831());
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PREVIEWSIZE_REAR_SUPPORTED, new ItemRunnable() {
            public void run(String items) {
                ConfigurationResolutionUtilBase.sCAMERA_PREVIEWSIZE_REAR_SUPPORTED_ITEMS = ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, ConfigurationUtilBase.getStringArray(items), true, true);
            }
        });
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PREVIEWSIZEONSCREEN_REAR_SUPPORTED, new ItemRunnable() {
            public void run(String items) {
                ConfigurationResolutionUtilBase.sCAMERA_PREVIEWSIZEONSCREEN_REAR_SUPPORTED_ITEMS = ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, ConfigurationUtilBase.getStringArray(items), true, false);
            }
        });
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PICTURESIZE_REAR_DEFAULT, new ItemRunnable() {
            public void run(String items) {
                ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_REAR_DEFAULT_ITEMS = ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, ConfigurationUtilBase.getStringArray(items), true, true);
            }
        });
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PICTURESIZE_BINNING_REAR_SUPPORTED, new C05875());
    }

    private static void addPictureSizeRearWideSupported(final Context context, HashMap<String, ItemRunnable> configMap) {
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PICTURESIZE_SUB_REAR_SUPPORTED, new C05886());
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PREVIEWSIZE_SUB_REAR_SUPPORTED, new ItemRunnable() {
            public void run(String items) {
                ConfigurationResolutionUtilBase.sCAMERA_PREVIEWSIZE_SUB_REAR_SUPPORTED_ITEMS = ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, ConfigurationUtilBase.getStringArray(items), true, true);
            }
        });
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PREVIEWSIZEONSCREEN_SUB_REAR_SUPPORTED, new ItemRunnable() {
            public void run(String items) {
                ConfigurationResolutionUtilBase.sCAMERA_PREVIEWSIZEONSCREEN_SUB_REAR_SUPPORTED_ITEMS = ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, ConfigurationUtilBase.getStringArray(items), true, false);
            }
        });
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PICTURESIZE_SUB_REAR_DEFAULT, new ItemRunnable() {
            public void run(String items) {
                ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_SUB_REAR_DEFAULT_ITEMS = ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, ConfigurationUtilBase.getStringArray(items), true, true);
            }
        });
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PICTURESIZE_BINNING_SUB_REAR_SUPPORTED, new C057410());
    }

    public static void addPictureSizeFrontSupported(Context context, HashMap<String, ItemRunnable> configMap) {
        addPictureSizeFrontNormalSupported(context, configMap);
        addPictureSizeFrontWideSupported(context, configMap);
    }

    private static void addPictureSizeFrontNormalSupported(final Context context, HashMap<String, ItemRunnable> configMap) {
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PICTURESIZE_FRONT_SUPPORTED, new C057511());
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PREVIEWSIZE_FRONT_SUPPORTED, new ItemRunnable() {
            public void run(String items) {
                ConfigurationResolutionUtilBase.sCAMERA_PREVIEWSIZE_FRONT_SUPPORTED_ITEMS = ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, ConfigurationUtilBase.getStringArray(items), false, true);
            }
        });
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PREVIEWSIZEONSCREEN_FRONT_SUPPORTED, new ItemRunnable() {
            public void run(String items) {
                ConfigurationResolutionUtilBase.sCAMERA_PREVIEWSIZEONSCREEN_FRONT_SUPPORTED_ITEMS = ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, ConfigurationUtilBase.getStringArray(items), false, false);
            }
        });
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PICTURESIZE_FRONT_DEFAULT, new ItemRunnable() {
            public void run(String items) {
                ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_FRONT_DEFAULT_ITEMS = ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, ConfigurationUtilBase.getStringArray(items), false, true);
            }
        });
    }

    private static void addPictureSizeFrontWideSupported(final Context context, HashMap<String, ItemRunnable> configMap) {
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PICTURESIZE_SUB_FRONT_SUPPORTED, new C057915());
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PREVIEWSIZE_SUB_FRONT_SUPPORTED, new ItemRunnable() {
            public void run(String items) {
                ConfigurationResolutionUtilBase.sCAMERA_PREVIEWSIZE_SUB_FRONT_SUPPORTED_ITEMS = ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, ConfigurationUtilBase.getStringArray(items), false, true);
            }
        });
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PREVIEWSIZEONSCREEN_SUB_FRONT_SUPPORTED, new ItemRunnable() {
            public void run(String items) {
                ConfigurationResolutionUtilBase.sCAMERA_PREVIEWSIZEONSCREEN_SUB_FRONT_SUPPORTED_ITEMS = ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, ConfigurationUtilBase.getStringArray(items), false, false);
            }
        });
        configMap.put(ConfigurationResolutionUtilBase.CAMERA_PICTURESIZE_SUB_FRONT_DEFAULT, new ItemRunnable() {
            public void run(String items) {
                ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_SUB_FRONT_DEFAULT_ITEMS = ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, ConfigurationUtilBase.getStringArray(items), false, true);
            }
        });
    }
}
