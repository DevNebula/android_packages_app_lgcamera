package com.lge.camera.constants;

import android.content.Context;
import com.lge.camera.constants.ConfigurationUtilBase.ItemRunnable;
import java.util.HashMap;

public class ConfigurationVideoSizeUtil extends ConfigurationResolutionUtilBase {

    /* renamed from: com.lge.camera.constants.ConfigurationVideoSizeUtil$10 */
    static class C064110 extends ItemRunnable {
        C064110() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sVIDEO_PREVIEWSIZE_FRONT_SUPPORTED_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationVideoSizeUtil$12 */
    static class C064312 extends ItemRunnable {
        C064312() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sVIDEO_SIZE_FRONT_DEFAULT_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationVideoSizeUtil$13 */
    static class C064413 extends ItemRunnable {
        C064413() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sVIDEO_SIZE_FRONT_SUPPORTED_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationVideoSizeUtil$14 */
    static class C064514 extends ItemRunnable {
        C064514() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sVIDEO_PREVIEWSIZE_FRONT_SUPPORTED_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationVideoSizeUtil$16 */
    static class C064716 extends ItemRunnable {
        C064716() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sVIDEO_SIZE_FRONT_DEFAULT_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationVideoSizeUtil$1 */
    static class C06481 extends ItemRunnable {
        C06481() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sVIDEO_SIZE_REAR_SUPPORTED_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationVideoSizeUtil$2 */
    static class C06492 extends ItemRunnable {
        C06492() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sVIDEO_PREVIEWSIZE_REAR_SUPPORTED_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationVideoSizeUtil$4 */
    static class C06514 extends ItemRunnable {
        C06514() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sVIDEO_SIZE_REAR_DEFAULT_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationVideoSizeUtil$5 */
    static class C06525 extends ItemRunnable {
        C06525() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sVIDEO_SIZE_SUB_REAR_SUPPORTED_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationVideoSizeUtil$6 */
    static class C06536 extends ItemRunnable {
        C06536() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sVIDEO_PREVIEWSIZE_SUB_REAR_SUPPORTED_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationVideoSizeUtil$8 */
    static class C06558 extends ItemRunnable {
        C06558() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sVIDEO_SIZE_SUB_REAR_DEFAULT_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    /* renamed from: com.lge.camera.constants.ConfigurationVideoSizeUtil$9 */
    static class C06569 extends ItemRunnable {
        C06569() {
        }

        public void run(String items) {
            ConfigurationResolutionUtilBase.sVIDEO_SIZE_FRONT_SUPPORTED_ITEMS = ConfigurationUtilBase.getStringArray(items);
        }
    }

    public static void addVideoSizeRearSupported(final Context context, HashMap<String, ItemRunnable> configMap) {
        configMap.put(ConfigurationResolutionUtilBase.VIDEO_SIZE_REAR_SUPPORTED, new C06481());
        configMap.put(ConfigurationResolutionUtilBase.VIDEO_PREVIEWSIZE_REAR_SUPPORTED, new C06492());
        configMap.put(ConfigurationResolutionUtilBase.VIDEO_PREVIEWSIZEONSCREEN_REAR_SUPPORTED, new ItemRunnable() {
            public void run(String items) {
                ConfigurationResolutionUtilBase.sVIDEO_PREVIEWSIZEONSCREEN_REAR_SUPPORTED_ITEMS = ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, ConfigurationUtilBase.getStringArray(items), true, false);
            }
        });
        configMap.put(ConfigurationResolutionUtilBase.VIDEO_SIZE_REAR_DEFAULT, new C06514());
        configMap.put(ConfigurationResolutionUtilBase.VIDEO_SIZE_SUB_REAR_SUPPORTED, new C06525());
        configMap.put(ConfigurationResolutionUtilBase.VIDEO_PREVIEWSIZE_SUB_REAR_SUPPORTED, new C06536());
        configMap.put(ConfigurationResolutionUtilBase.VIDEO_PREVIEWSIZEONSCREEN_SUB_REAR_SUPPORTED, new ItemRunnable() {
            public void run(String items) {
                ConfigurationResolutionUtilBase.sVIDEO_PREVIEWSIZEONSCREEN_SUB_REAR_SUPPORTED_ITEMS = ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, ConfigurationUtilBase.getStringArray(items), true, false);
            }
        });
        configMap.put(ConfigurationResolutionUtilBase.VIDEO_SIZE_SUB_REAR_DEFAULT, new C06558());
    }

    public static void addVideoSizeFrontSupported(final Context context, HashMap<String, ItemRunnable> configMap) {
        configMap.put(ConfigurationResolutionUtilBase.VIDEO_SIZE_FRONT_SUPPORTED, new C06569());
        configMap.put(ConfigurationResolutionUtilBase.VIDEO_PREVIEWSIZE_FRONT_SUPPORTED, new C064110());
        configMap.put(ConfigurationResolutionUtilBase.VIDEO_PREVIEWSIZEONSCREEN_FRONT_SUPPORTED, new ItemRunnable() {
            public void run(String items) {
                ConfigurationResolutionUtilBase.sVIDEO_PREVIEWSIZEONSCREEN_FRONT_SUPPORTED_ITEMS = ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, ConfigurationUtilBase.getStringArray(items), false, false);
            }
        });
        configMap.put(ConfigurationResolutionUtilBase.VIDEO_SIZE_FRONT_DEFAULT, new C064312());
        configMap.put(ConfigurationResolutionUtilBase.VIDEO_SIZE_SUB_FRONT_SUPPORTED, new C064413());
        configMap.put(ConfigurationResolutionUtilBase.VIDEO_PREVIEWSIZE_SUB_FRONT_SUPPORTED, new C064514());
        configMap.put(ConfigurationResolutionUtilBase.VIDEO_PREVIEWSIZEONSCREEN_SUB_FRONT_SUPPORTED, new ItemRunnable() {
            public void run(String items) {
                ConfigurationResolutionUtilBase.sVIDEO_PREVIEWSIZEONSCREEN_FRONT_SUPPORTED_ITEMS = ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, ConfigurationUtilBase.getStringArray(items), false, false);
            }
        });
        configMap.put(ConfigurationResolutionUtilBase.VIDEO_SIZE_SUB_FRONT_DEFAULT, new C064716());
    }
}
