package com.lge.camera.constants;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Log;
import com.lge.camera.C0088R;
import com.lge.camera.constants.ConfigurationUtilBase.ItemRunnable;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ConfigurationParsingUtil extends ConfigurationUtilBase {
    public static final String CAM_CFG_NOT_FOUND = "not found";
    public static final String CAM_CFG_PATH_DEFAULT = "/system/etc/camera_config.xml";
    public static final String CAM_JOAN_ONLY_CFG_PATH = "/system/etc/camera_config2.xml";
    protected static final String DEFAULT_TAG = "default";
    protected static final String ITEM_TAG = "item";
    protected static final String KEY_MODEL_NAME = "ro.model.name";
    protected static final String NAME_ATTR = "name";
    protected static final String PREFERENCE_TAG = "preference";
    protected static final String START_MODEL_NAME = "M";
    public static int sAPP_TIER = 6;
    protected static HashMap<String, Integer> sAppTierMap = null;
    public static boolean sLoadConfigDone = false;
    public static boolean sLoadHalConfigDone = false;
    public static int sRAW_APP_TIER = 6;
    public static String sRawAppTier = "NOT_DEF";
    protected static HashMap<String, ItemRunnable> sSupportedConfigMap = null;

    protected static void setItems(XmlPullParser xpp, String itemName) {
        ItemRunnable runnable = (ItemRunnable) sSupportedConfigMap.get(itemName);
        if (runnable != null) {
            try {
                runnable.run(xpp.getText());
                return;
            } catch (NumberFormatException e) {
                Log.d(CameraConstants.TAG, "parsing error But keep going=" + e);
                return;
            }
        }
        Log.w(CameraConstants.TAG, "Configuration setItems : itemName = [###" + itemName + "###] is not found.");
    }

    protected static void makeAppTierMap(Context context) {
        if (sAppTierMap == null) {
            sAppTierMap = new HashMap();
            sAppTierMap.put("LOW", Integer.valueOf(0));
            sAppTierMap.put("MID_LOW", Integer.valueOf(1));
            sAppTierMap.put("MID", Integer.valueOf(2));
            sAppTierMap.put("MID_HIGH", Integer.valueOf(3));
            sAppTierMap.put("HIGH", Integer.valueOf(4));
            sAppTierMap.put("PREMIUM", Integer.valueOf(5));
            sAppTierMap.put("NOT_DEF", Integer.valueOf(6));
        }
        sRawAppTier = "NOT_DEF";
        int appTierStringId = context.getResources().getIdentifier("config_app_tier", "string", "com.lge");
        if (appTierStringId > 0) {
            try {
                sRawAppTier = context.getResources().getString(appTierStringId);
            } catch (IllegalArgumentException e) {
                CamLog.m3d("MemoryTier", "-apptier- Tier is not defined");
            }
        }
        CamLog.m3d(CameraConstants.TAG, "-apptier- sRawAppTier : " + sRawAppTier);
        Integer tierIndex = (Integer) sAppTierMap.get(sRawAppTier);
        if (tierIndex != null) {
            sAPP_TIER = tierIndex.intValue();
            sRAW_APP_TIER = tierIndex.intValue();
        }
        String deviceName = SystemProperties.get("ro.product.device");
        if (deviceName.contains("joan") || deviceName.contains("lucye")) {
            sAPP_TIER = ((Integer) sAppTierMap.get("PREMIUM")).intValue();
            CamLog.m3d(CameraConstants.TAG, "-apptier- changed app tier to premium ");
        } else if (deviceName.equals("mh") || deviceName.equals("mhn")) {
            sAPP_TIER = ((Integer) sAppTierMap.get("MID_HIGH")).intValue();
            CamLog.m3d(CameraConstants.TAG, "-apptier- changed app tier to mid-high");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x008e A:{SYNTHETIC, Splitter: B:47:0x008e} */
    protected static synchronized void parseConfig(android.content.Context r15, boolean r16) {
        /*
        r12 = com.lge.camera.constants.ConfigurationParsingUtil.class;
        monitor-enter(r12);
        r11 = "CameraApp";
        r13 = "parse config START";
        com.lge.camera.util.CamLog.m3d(r11, r13);	 Catch:{ all -> 0x0081 }
        r7 = 0;
        r10 = 0;
        r2 = android.os.Build.DEVICE;	 Catch:{ all -> 0x0081 }
        r6 = readModelName();	 Catch:{ all -> 0x0081 }
        r0 = getConfigFilePath(r15);	 Catch:{ Throwable -> 0x0057 }
        r11 = "not found";
        r11 = r11.equals(r0);	 Catch:{ Throwable -> 0x0057 }
        if (r11 != 0) goto L_0x0046;
    L_0x001e:
        r8 = new java.io.InputStreamReader;	 Catch:{ Throwable -> 0x0057 }
        r11 = new java.io.FileInputStream;	 Catch:{ Throwable -> 0x0057 }
        r11.<init>(r0);	 Catch:{ Throwable -> 0x0057 }
        r8.<init>(r11);	 Catch:{ Throwable -> 0x0057 }
        r5 = org.xmlpull.v1.XmlPullParserFactory.newInstance();	 Catch:{ Throwable -> 0x00a7, all -> 0x00a4 }
        r10 = r5.newPullParser();	 Catch:{ Throwable -> 0x00a7, all -> 0x00a4 }
        r10.setInput(r8);	 Catch:{ Throwable -> 0x00a7, all -> 0x00a4 }
        r7 = r8;
    L_0x0034:
        r4 = r10.getEventType();	 Catch:{ Throwable -> 0x0057 }
    L_0x0038:
        r11 = 1;
        if (r4 == r11) goto L_0x006d;
    L_0x003b:
        switch(r4) {
            case 2: goto L_0x0053;
            default: goto L_0x003e;
        };	 Catch:{ Throwable -> 0x0057 }
    L_0x003e:
        r10.next();	 Catch:{ Throwable -> 0x0057 }
        r4 = r10.getEventType();	 Catch:{ Throwable -> 0x0057 }
        goto L_0x0038;
    L_0x0046:
        r1 = getConfigResourceId(r15);	 Catch:{ Throwable -> 0x0057 }
        r11 = r15.getResources();	 Catch:{ Throwable -> 0x0057 }
        r10 = r11.getXml(r1);	 Catch:{ Throwable -> 0x0057 }
        goto L_0x0034;
    L_0x0053:
        parseStartTag(r10, r2, r6);	 Catch:{ Throwable -> 0x0057 }
        goto L_0x003e;
    L_0x0057:
        r9 = move-exception;
    L_0x0058:
        r11 = "CameraApp";
        r13 = "Config parsing error.";
        android.util.Log.e(r11, r13);	 Catch:{ all -> 0x008b }
        if (r7 == 0) goto L_0x0064;
    L_0x0061:
        r7.close();	 Catch:{ Exception -> 0x0092 }
    L_0x0064:
        r11 = "CameraApp";
        r13 = "parse config END";
        com.lge.camera.util.CamLog.m3d(r11, r13);	 Catch:{ all -> 0x0081 }
        monitor-exit(r12);
        return;
    L_0x006d:
        if (r16 == 0) goto L_0x0084;
    L_0x006f:
        r11 = 1;
        sLoadHalConfigDone = r11;	 Catch:{ Throwable -> 0x0057 }
    L_0x0072:
        if (r7 == 0) goto L_0x0064;
    L_0x0074:
        r7.close();	 Catch:{ Exception -> 0x0078 }
        goto L_0x0064;
    L_0x0078:
        r3 = move-exception;
        r11 = "CameraApp";
        r13 = "Config parsing : reader close error.";
        android.util.Log.e(r11, r13);	 Catch:{ all -> 0x0081 }
        goto L_0x0064;
    L_0x0081:
        r11 = move-exception;
        monitor-exit(r12);
        throw r11;
    L_0x0084:
        r11 = 1;
        sLoadConfigDone = r11;	 Catch:{ Throwable -> 0x0057 }
        r11 = 1;
        sLoadHalConfigDone = r11;	 Catch:{ Throwable -> 0x0057 }
        goto L_0x0072;
    L_0x008b:
        r11 = move-exception;
    L_0x008c:
        if (r7 == 0) goto L_0x0091;
    L_0x008e:
        r7.close();	 Catch:{ Exception -> 0x009b }
    L_0x0091:
        throw r11;	 Catch:{ all -> 0x0081 }
    L_0x0092:
        r3 = move-exception;
        r11 = "CameraApp";
        r13 = "Config parsing : reader close error.";
        android.util.Log.e(r11, r13);	 Catch:{ all -> 0x0081 }
        goto L_0x0064;
    L_0x009b:
        r3 = move-exception;
        r13 = "CameraApp";
        r14 = "Config parsing : reader close error.";
        android.util.Log.e(r13, r14);	 Catch:{ all -> 0x0081 }
        goto L_0x0091;
    L_0x00a4:
        r11 = move-exception;
        r7 = r8;
        goto L_0x008c;
    L_0x00a7:
        r9 = move-exception;
        r7 = r8;
        goto L_0x0058;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.constants.ConfigurationParsingUtil.parseConfig(android.content.Context, boolean):void");
    }

    protected static void modifyConfiguration(Context context) {
        if (!"TMO".equals(SystemProperties.get("ro.build.target_operator"))) {
            return;
        }
        if (("TMA".equals(SystemProperties.get("ro.build.default_country")) || "TRA".equals(SystemProperties.get("ro.build.default_country"))) && "COM".equals(SystemProperties.get("ro.build.target_country"))) {
            if (checkStringArray(ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_REAR_SUPPORTED_ITEMS) && checkStringArray(ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_REAR_DEFAULT_ITEMS)) {
                Log.d(CameraConstants.TAG, "modify default picture size value in back camera settings");
                ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_REAR_DEFAULT_ITEMS[0] = ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_REAR_SUPPORTED_ITEMS[0];
            }
            if (checkStringArray(ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_FRONT_SUPPORTED_ITEMS) && checkStringArray(ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_FRONT_DEFAULT_ITEMS)) {
                Log.d(CameraConstants.TAG, "modify default picture size value in front camera settings");
                ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_FRONT_DEFAULT_ITEMS[0] = ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_FRONT_SUPPORTED_ITEMS[0];
            }
        }
    }

    protected static boolean checkStringArray(String[] array) {
        return array != null && array.length > 0;
    }

    protected static String getConfigFilePath(Context context) {
        File defaultConfigFile = new File(CAM_JOAN_ONLY_CFG_PATH);
        if (defaultConfigFile != null && defaultConfigFile.exists()) {
            return CAM_JOAN_ONLY_CFG_PATH;
        }
        defaultConfigFile = new File(CAM_CFG_PATH_DEFAULT);
        if (defaultConfigFile == null || !defaultConfigFile.exists()) {
            return "not found";
        }
        return CAM_CFG_PATH_DEFAULT;
    }

    protected static int getConfigResourceId(Context context) {
        String device = Build.DEVICE;
        String model = readModelName();
        DisplayMetrics metrics = Utils.getWindowRealMatics(context);
        int longWidth = Math.max(metrics.widthPixels, metrics.heightPixels);
        int shortHeight = Math.min(metrics.widthPixels, metrics.heightPixels);
        String prefConfigStr = "preference_supported_config_";
        String typeXml = "xml";
        String packageName = context.getPackageName();
        String configNameDevice = "preference_supported_config_" + device.replace("-", "_").replace(" ", "_").toLowerCase(Locale.US);
        int configXmlIdDevice = context.getResources().getIdentifier(configNameDevice, "xml", packageName);
        if (configXmlIdDevice > 0) {
            String configNameDeviceModel = configNameDevice + "_" + model.replace("-", "_").replace(" ", "_").toLowerCase(Locale.US);
            int configXmlIdDeviceModel = context.getResources().getIdentifier(configNameDeviceModel, "xml", packageName);
            if (configXmlIdDeviceModel > 0) {
                int configXmlIdDeviceModelSize = context.getResources().getIdentifier(configNameDeviceModel + "_" + longWidth + "_" + shortHeight, "xml", packageName);
                return configXmlIdDeviceModelSize > 0 ? configXmlIdDeviceModelSize : configXmlIdDeviceModel;
            } else {
                int configXmlIdDeviceSize = context.getResources().getIdentifier(configNameDevice + "_" + longWidth + "_" + shortHeight, "xml", packageName);
                if (configXmlIdDeviceSize > 0) {
                    return configXmlIdDeviceSize;
                }
                return configXmlIdDevice;
            }
        }
        String configNameModel = "preference_supported_config_" + model.replace("-", "_").replace(" ", "_").toLowerCase(Locale.US);
        int configXmlIdModel = context.getResources().getIdentifier(configNameModel, "xml", packageName);
        if (configXmlIdModel > 0) {
            int configXmlIdModelSize = context.getResources().getIdentifier(configNameModel + "_" + longWidth + "_" + shortHeight, "xml", packageName);
            if (configXmlIdModelSize > 0) {
                return configXmlIdModelSize;
            }
            return configXmlIdModel;
        }
        int configXmlIdSize = context.getResources().getIdentifier("preference_supported_config__" + longWidth + "_" + shortHeight, "xml", packageName);
        if (configXmlIdSize > 0) {
            return configXmlIdSize;
        }
        return C0088R.xml.preference_supported_config;
    }

    protected static String readModelName() {
        String modelName = SystemProperties.get(KEY_MODEL_NAME);
        if ("".equals(modelName)) {
            return Build.MODEL;
        }
        return modelName;
    }

    protected static void parseStartTag(XmlPullParser xpp, String device, String modelName) {
        String[] elementNames = xpp.getName().split("\\.");
        String hashCode = "M" + Integer.toString(modelName.hashCode());
        int i = 0;
        while (i < elementNames.length) {
            try {
                String name = elementNames[i];
                if (device.toLowerCase(Locale.US).equals(name.toLowerCase(Locale.US)) || hashCode.toLowerCase(Locale.US).equals(name.toLowerCase(Locale.US)) || "default".toLowerCase(Locale.US).equals(name.toLowerCase(Locale.US))) {
                    setXmlItem(xpp);
                } else if (i == elementNames.length - 1 && !PREFERENCE_TAG.equals(name.toLowerCase(Locale.US))) {
                    skip(xpp);
                }
                i++;
            } catch (Throwable t) {
                Log.d(CameraConstants.TAG, "parseStartTag " + t.toString());
                return;
            }
        }
    }

    protected static void setXmlItem(XmlPullParser xpp) {
        try {
            int eventType = xpp.getEventType();
            String itemName = "";
            while (eventType != 1) {
                switch (eventType) {
                    case 2:
                        if (!ITEM_TAG.equals(xpp.getName())) {
                            break;
                        }
                        itemName = xpp.getAttributeValue(null, "name");
                        xpp.next();
                        setItems(xpp, itemName);
                        break;
                    case 3:
                        if (ITEM_TAG.equals(xpp.getName())) {
                            break;
                        }
                        return;
                    default:
                        break;
                }
                xpp.next();
                eventType = xpp.getEventType();
            }
        } catch (Throwable t) {
            Log.d(CameraConstants.TAG, "getXmlItem " + t.toString());
        }
    }

    protected static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != 2) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case 2:
                    depth++;
                    break;
                case 3:
                    depth--;
                    break;
                default:
                    break;
            }
        }
    }
}
