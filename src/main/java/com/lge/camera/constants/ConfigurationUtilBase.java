package com.lge.camera.constants;

public class ConfigurationUtilBase {

    public static abstract class ItemRunnable {
        public abstract void run(String str);
    }

    public static String[] getStringArray(String itemName) {
        return itemName.replaceAll("\\s+", "").split(",");
    }

    public static boolean isItemSupported(String itemName) {
        return "true".equals(itemName.replaceAll("\\s+", ""));
    }

    public static int getItemSupportedType(String itemName) {
        return Integer.valueOf(itemName.replaceAll("\\s+", "")).intValue();
    }
}
