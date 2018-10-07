package com.lge.camera.constants;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ViewConfiguration;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import com.lge.config.ConfigBuildFlags;
import com.lge.internal.R;
import com.lge.os.Build.VERSION;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;

public final class ModelProperties {
    public static final int CARRIER_CODE_ACG = 17;
    public static final int CARRIER_CODE_ATNT = 5;
    public static final int CARRIER_CODE_BELL = 14;
    public static final int CARRIER_CODE_CRK = 34;
    public static final int CARRIER_CODE_DCM = 4;
    public static final int CARRIER_CODE_IUSACELL = 16;
    public static final int CARRIER_CODE_JCM = 35;
    public static final int CARRIER_CODE_KDDI = 7;
    public static final int CARRIER_CODE_KT = 3;
    public static final int CARRIER_CODE_LGUPLUS = 1;
    public static final int CARRIER_CODE_M1 = 30;
    public static final int CARRIER_CODE_MPCS = 8;
    public static final int CARRIER_CODE_O2 = 22;
    public static final int CARRIER_CODE_OPEN_BRA = 13;
    public static final int CARRIER_CODE_OPEN_EUR = 9;
    public static final int CARRIER_CODE_OPEN_JPN = 32;
    public static final int CARRIER_CODE_OPEN_KOR = 31;
    public static final int CARRIER_CODE_ORG = 25;
    public static final int CARRIER_CODE_ROGERS = 20;
    public static final int CARRIER_CODE_SBM = 33;
    public static final int CARRIER_CODE_SKT = 2;
    public static final int CARRIER_CODE_SPCS = 10;
    public static final int CARRIER_CODE_STAR_HUB = 28;
    public static final int CARRIER_CODE_STL = 29;
    public static final int CARRIER_CODE_TELCEL = 12;
    public static final int CARRIER_CODE_TELSTRA = 21;
    public static final int CARRIER_CODE_TELUS = 15;
    public static final int CARRIER_CODE_TESCO = 23;
    public static final int CARRIER_CODE_TMUS = 19;
    public static final int CARRIER_CODE_UNKNOWN = 0;
    public static final int CARRIER_CODE_UQ = 36;
    public static final int CARRIER_CODE_USC = 18;
    public static final int CARRIER_CODE_VDF = 24;
    public static final int CARRIER_CODE_VIDEOTRON = 26;
    public static final int CARRIER_CODE_VIVO = 11;
    public static final int CARRIER_CODE_VZW = 6;
    public static final int CARRIER_CODE_WIND = 27;
    public static final int LCD_TYPE_FULL_VISION = 1;
    public static final int LCD_TYPE_FULL_VISION_LONG = 2;
    public static final int LCD_TYPE_NORMAL = 0;
    private static int sCarrierCode = setCarrierCode();
    private static HashMap<String, Integer> sCarrierCodeMap = null;
    private static int sCheckInstalledLGLensApp = -1;
    private static int sCheckInstalledQLensApp = -1;
    public static int sIsFHD60Supported = -1;
    private static boolean sIsFakeExif = checkFakeExif();
    private static boolean sIsFakeExifAtnt = checkFakeExifAtnt();
    private static boolean sIsFakeMode = checkFakeMode();
    public static int sIsJoanOriginal = -1;
    private static Boolean sIsRetailModeInstalled = null;
    private static int sIsSidePowerKeyModel = -1;
    private static Boolean sIsSupportedLguCnas = null;
    public static int sIsUHDSupported = -1;
    private static int sLCDType = 0;

    public static int getCarrierCode() {
        return sCarrierCode;
    }

    private static void initCarrierCodeMap() {
        sCarrierCodeMap = new HashMap();
        sCarrierCodeMap.put("LGU", Integer.valueOf(1));
        sCarrierCodeMap.put("SKT", Integer.valueOf(2));
        sCarrierCodeMap.put("KT", Integer.valueOf(3));
        sCarrierCodeMap.put("DCM", Integer.valueOf(4));
        sCarrierCodeMap.put("ATT", Integer.valueOf(5));
        sCarrierCodeMap.put("VZW", Integer.valueOf(6));
        sCarrierCodeMap.put("KDDI", Integer.valueOf(7));
        sCarrierCodeMap.put("MPCS", Integer.valueOf(8));
        sCarrierCodeMap.put("OPEN", Integer.valueOf(9));
        sCarrierCodeMap.put("SPR", Integer.valueOf(10));
        sCarrierCodeMap.put("VIV", Integer.valueOf(11));
        sCarrierCodeMap.put("TCL", Integer.valueOf(12));
        sCarrierCodeMap.put("VTR", Integer.valueOf(26));
        sCarrierCodeMap.put("WIN", Integer.valueOf(27));
        sCarrierCodeMap.put("VDF", Integer.valueOf(24));
        sCarrierCodeMap.put("ORG", Integer.valueOf(25));
        sCarrierCodeMap.put("RGS", Integer.valueOf(20));
        sCarrierCodeMap.put("TEL", Integer.valueOf(21));
        sCarrierCodeMap.put("ACG", Integer.valueOf(17));
        sCarrierCodeMap.put("USC", Integer.valueOf(18));
        sCarrierCodeMap.put("TMO", Integer.valueOf(19));
        sCarrierCodeMap.put("BELL", Integer.valueOf(14));
        sCarrierCodeMap.put("TLS", Integer.valueOf(15));
        sCarrierCodeMap.put("SHB", Integer.valueOf(28));
        sCarrierCodeMap.put("STL", Integer.valueOf(29));
        sCarrierCodeMap.put("MON", Integer.valueOf(30));
        sCarrierCodeMap.put("SBM", Integer.valueOf(33));
        sCarrierCodeMap.put("CRK", Integer.valueOf(34));
        sCarrierCodeMap.put("JCM", Integer.valueOf(35));
        sCarrierCodeMap.put("UQ", Integer.valueOf(36));
    }

    public static int setCarrierCode() {
        String strOperatorIso = SystemProperties.get("ro.build.target_operator");
        if (sCarrierCodeMap == null) {
            initCarrierCodeMap();
        }
        Integer carrierCode = (Integer) sCarrierCodeMap.get(strOperatorIso);
        if (carrierCode == null) {
            sCarrierCode = 0;
            if ("TRF_VZW".equals(SystemProperties.get("ro.build.target_operator_ext"))) {
                sCarrierCode = 6;
            }
            if ("TRF_ATT".equals(SystemProperties.get("ro.build.target_operator_ext"))) {
                sCarrierCode = 5;
            }
        } else {
            sCarrierCode = carrierCode.intValue();
            if (sCarrierCode == 9 && "KR".equals(SystemProperties.get("ro.build.target_country"))) {
                sCarrierCode = 31;
            }
            if (sCarrierCode == 9 && "JP".equals(SystemProperties.get("ro.build.target_country"))) {
                sCarrierCode = 32;
            }
        }
        Log.d(CameraConstants.TAG, "strOperatorIso : " + strOperatorIso + ", mCarrierCode = " + sCarrierCode);
        return sCarrierCode;
    }

    public static boolean isDomesticModel() {
        if (getCarrierCode() == 1 || getCarrierCode() == 2 || getCarrierCode() == 3 || getCarrierCode() == 31) {
            return true;
        }
        return false;
    }

    public static boolean isJapanModel() {
        if (getCarrierCode() == 4 || getCarrierCode() == 7 || getCarrierCode() == 35 || getCarrierCode() == 36 || getCarrierCode() == 32 || getCarrierCode() == 33) {
            return true;
        }
        return false;
    }

    public static boolean useDCFRule() {
        if (isUseNewNamingRule()) {
            return false;
        }
        switch (getCarrierCode()) {
            case 4:
            case 6:
                return false;
            default:
                return true;
        }
    }

    public static boolean isRenesasISP() {
        return ConfigurationUtil.sRENESAS_ISP_VALUE;
    }

    public static boolean isMTKChipset() {
        return ConfigurationUtil.sMTK_CHIPSET_VALUE;
    }

    public static String getPanoramaPreviewSize(boolean usePreview) {
        if (ConfigurationUtil.sPANORAMA_PREVIEW_SIZE_VALUE == null) {
            return CameraConstants.VGA_RESOLUTION;
        }
        String[] panoPrevSizeArray = ConfigurationUtil.sPANORAMA_PREVIEW_SIZE_VALUE;
        if (panoPrevSizeArray != null) {
            if (panoPrevSizeArray.length == 1) {
                CamLog.m7i(CameraConstants.TAG, "panoPrevSizeArray[0] : " + panoPrevSizeArray[0]);
                return panoPrevSizeArray[0];
            } else if (panoPrevSizeArray.length == 2) {
                if (usePreview) {
                    CamLog.m7i(CameraConstants.TAG, "2nd use preview panoPrevSizeArray[0] : " + panoPrevSizeArray[0]);
                    return panoPrevSizeArray[0];
                }
                CamLog.m7i(CameraConstants.TAG, "2nd use stitching panoPrevSizeArray[1] : " + panoPrevSizeArray[1]);
                return panoPrevSizeArray[1];
            }
        }
        return ConfigurationUtil.sPANORAMA_PREVIEW_SIZE_VALUE[0];
    }

    public static String[] get360PanoramaDistortionCorrection() {
        if (ConfigurationUtil.sPANORAMA_360_DIST_COR_VALUE != null) {
            return ConfigurationUtil.sPANORAMA_360_DIST_COR_VALUE;
        }
        return new String[]{"0", "0", "0", "0", "0.0", "0.0"};
    }

    public static boolean isSupportHDMI_MHL() {
        return ConfigurationUtil.sHDMI_MHL_SUPPORTED_VALUE;
    }

    public static boolean isSoftKeyNavigationBarModel(Context context) {
        return (context == null || ViewConfiguration.get(context).hasPermanentMenuKey()) ? false : true;
    }

    public static boolean isWifiOnlyModel(Context mContext) {
        if (mContext == null) {
            return false;
        }
        if (mContext.getPackageManager().hasSystemFeature("android.hardware.telephony")) {
            return false;
        }
        return true;
    }

    public static int getVoiceShutterKind() {
        if (isDomesticModel()) {
            return 0;
        }
        if (isJapanModel()) {
            return 2;
        }
        return 1;
    }

    public static int getVoiceShutterSensitivity() {
        if (isDomesticModel()) {
            return 6;
        }
        if (isJapanModel()) {
        }
        return 15;
    }

    public static boolean isRemoveOrgFile() {
        return getCarrierCode() == 4;
    }

    public static boolean useCheeseShutterTitle() {
        switch (getCarrierCode()) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 7:
            case 19:
            case 31:
            case 32:
            case 33:
            case 35:
            case 36:
                return false;
            default:
                return true;
        }
    }

    public static boolean isSupportVoiceShutterAME() {
        return "AME".equals(SystemProperties.get("ro.build.target_region")) && ("AME".equals(SystemProperties.get("ro.build.target_country")) || "TR".equals(SystemProperties.get("ro.build.target_country")));
    }

    public static boolean isSupportVoiceShutterJapanese() {
        switch (getCarrierCode()) {
            case 4:
            case 7:
            case 32:
            case 33:
            case 35:
            case 36:
                return true;
            default:
                return false;
        }
    }

    public static int getBeautyDefaultLevel() {
        int i = 0;
        String currentCountry = SystemProperties.get("ro.build.target_country");
        String[] defaultLevel4CountryList = new String[]{"KR", "CN", "JP"};
        int length = defaultLevel4CountryList.length;
        while (i < length) {
            if (defaultLevel4CountryList[i].compareToIgnoreCase(currentCountry) != 0) {
                i++;
            } else if (isOSU()) {
                return 3;
            } else {
                return 4;
            }
        }
        return "IDN-XX".equals(SystemProperties.get("ro.lge.opensw")) ? 6 : 1;
    }

    public static int getRelightingDefaultLevel() {
        String currentCountry = SystemProperties.get("ro.build.target_country");
        for (String checkCountry : new String[]{"US", "CA"}) {
            if (checkCountry.compareToIgnoreCase(currentCountry) == 0) {
                return 0;
            }
        }
        String currentRegion = SystemProperties.get("ro.build.target_region");
        for (String checkRegion : new String[]{"EU", "SCA"}) {
            if (checkRegion.compareToIgnoreCase(currentRegion) == 0) {
                return 0;
            }
        }
        return 4;
    }

    public static boolean isUseNewNamingRule() {
        switch (getCarrierCode()) {
            case 4:
            case 6:
                return false;
            default:
                return true;
        }
    }

    public static boolean isKeyPadSupported(Context context) {
        switch (context.getResources().getConfiguration().keyboard) {
            case 3:
                return true;
            default:
                return false;
        }
    }

    public static boolean isBatteryLevelConstraintNeeded() {
        return ConfigurationUtil.sBATTERY_LEVEL_CONSTRAINT_NEEDED_VALUE;
    }

    public static int getAppTier() {
        return ConfigurationUtil.sAPP_TIER;
    }

    public static int getRawAppTier() {
        return ConfigurationUtil.sRAW_APP_TIER;
    }

    public static String getDefaultFlashValue() {
        switch (getCarrierCode()) {
            case 34:
                return "auto";
            default:
                return "off";
        }
    }

    public static boolean checkFakeMode() {
        if ("1".equals(SystemProperties.get("ro.dev.fmode"))) {
            return true;
        }
        return false;
    }

    public static boolean isFakeMode() {
        return sIsFakeMode;
    }

    public static boolean checkFakeExifAtnt() {
        return "1".equals(SystemProperties.get("ro.dev.fmode_exif_atnt"));
    }

    public static boolean isFakeExifAtnt() {
        return sIsFakeExifAtnt;
    }

    public static boolean checkFakeExif() {
        return "1".equals(SystemProperties.get("ro.dev.fmode_exif"));
    }

    public static boolean isFakeExif() {
        return sIsFakeExif || sIsFakeExifAtnt;
    }

    public static boolean isQua() {
        try {
            return SystemProperties.getBoolean("ro.product.brand_qua", false);
        } catch (NoSuchFieldError e) {
            return false;
        }
    }

    public static boolean isJoanRenewal() {
        return ConfigurationUtil.sJOAN_RENEWAL_SUPPORTED;
    }

    public static boolean isJoanOriginal() {
        if (sIsJoanOriginal < 0) {
            int i;
            if (!ConfigurationUtil.sJOAN_RENEWAL_SUPPORTED || "Renewal256".equals(SystemProperties.get("ro.lge.hydra")) || "Renewal128".equals(SystemProperties.get("ro.lge.hydra"))) {
                i = 0;
            } else {
                i = 1;
            }
            sIsJoanOriginal = i;
        }
        if (sIsJoanOriginal == 1) {
            return true;
        }
        return false;
    }

    public static boolean isPersistentOn(Context context) {
        ApplicationInfo info = context.getApplicationInfo();
        if (info != null) {
            CamLog.m3d(CameraConstants.TAG, "Persist flags " + info.flags);
            if ((info.flags & 8) == 8) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPhone(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        return (telephonyManager == null || telephonyManager.getPhoneType() == 0) ? false : true;
    }

    public static boolean isEmmaNeo() {
        String deviceName = SystemProperties.get("ro.product.device");
        if (deviceName == null) {
            return false;
        }
        if ("judyln".equals(deviceName) || "judyp".equals(deviceName)) {
            return true;
        }
        return false;
    }

    public static boolean isTablet(Context context) {
        if (context == null) {
            return false;
        }
        String buildCharacter = SystemProperties.get("ro.build.characteristics");
        if (buildCharacter == null || !buildCharacter.contains("tablet")) {
            return false;
        }
        return true;
    }

    public static boolean isSupportedStylusPen(Context context) {
        String penSupportedPath = "/sys/devices/virtual/input/lge_touch/pen_support";
        if (new File("/sys/devices/virtual/input/lge_touch/pen_support").exists() && readPenSupport("/sys/devices/virtual/input/lge_touch/pen_support") == 1) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x011a A:{SYNTHETIC, Splitter: B:38:0x011a} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00b0 A:{SYNTHETIC, Splitter: B:24:0x00b0} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00f3 A:{SYNTHETIC, Splitter: B:32:0x00f3} */
    private static int readPenSupport(java.lang.String r9) {
        /*
        r1 = 0;
        r3 = "";
        r4 = 0;
        r2 = new java.io.BufferedReader;	 Catch:{ FileNotFoundException -> 0x003c, IOException -> 0x0087, Exception -> 0x00d4 }
        r5 = new java.io.FileReader;	 Catch:{ FileNotFoundException -> 0x003c, IOException -> 0x0087, Exception -> 0x00d4 }
        r5.<init>(r9);	 Catch:{ FileNotFoundException -> 0x003c, IOException -> 0x0087, Exception -> 0x00d4 }
        r2.<init>(r5);	 Catch:{ FileNotFoundException -> 0x003c, IOException -> 0x0087, Exception -> 0x00d4 }
        r3 = r2.readLine();	 Catch:{ FileNotFoundException -> 0x0146, IOException -> 0x0142, Exception -> 0x013f, all -> 0x013c }
        r4 = java.lang.Integer.parseInt(r3);	 Catch:{ FileNotFoundException -> 0x0146, IOException -> 0x0142, Exception -> 0x013f, all -> 0x013c }
        if (r2 == 0) goto L_0x001b;
    L_0x0018:
        r2.close();	 Catch:{ Exception -> 0x001d }
    L_0x001b:
        r1 = r2;
    L_0x001c:
        return r4;
    L_0x001d:
        r0 = move-exception;
        r5 = "CameraApp";
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r7 = "File Close exception : ";
        r6 = r6.append(r7);
        r7 = r0.getMessage();
        r6 = r6.append(r7);
        r6 = r6.toString();
        com.lge.camera.util.CamLog.m7i(r5, r6);
        r1 = r2;
        goto L_0x001c;
    L_0x003c:
        r0 = move-exception;
    L_0x003d:
        r5 = "CameraApp";
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0117 }
        r6.<init>();	 Catch:{ all -> 0x0117 }
        r7 = "PenSupport File Not Found : ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x0117 }
        r6 = r6.append(r9);	 Catch:{ all -> 0x0117 }
        r7 = " / ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x0117 }
        r7 = r0.getMessage();	 Catch:{ all -> 0x0117 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0117 }
        r6 = r6.toString();	 Catch:{ all -> 0x0117 }
        com.lge.camera.util.CamLog.m7i(r5, r6);	 Catch:{ all -> 0x0117 }
        if (r1 == 0) goto L_0x001c;
    L_0x0065:
        r1.close();	 Catch:{ Exception -> 0x0069 }
        goto L_0x001c;
    L_0x0069:
        r0 = move-exception;
        r5 = "CameraApp";
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r7 = "File Close exception : ";
        r6 = r6.append(r7);
        r7 = r0.getMessage();
        r6 = r6.append(r7);
        r6 = r6.toString();
        com.lge.camera.util.CamLog.m7i(r5, r6);
        goto L_0x001c;
    L_0x0087:
        r0 = move-exception;
    L_0x0088:
        r5 = "CameraApp";
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0117 }
        r6.<init>();	 Catch:{ all -> 0x0117 }
        r7 = "PenSupport File IOException : ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x0117 }
        r6 = r6.append(r9);	 Catch:{ all -> 0x0117 }
        r7 = " / ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x0117 }
        r7 = r0.getMessage();	 Catch:{ all -> 0x0117 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0117 }
        r6 = r6.toString();	 Catch:{ all -> 0x0117 }
        com.lge.camera.util.CamLog.m7i(r5, r6);	 Catch:{ all -> 0x0117 }
        if (r1 == 0) goto L_0x001c;
    L_0x00b0:
        r1.close();	 Catch:{ Exception -> 0x00b5 }
        goto L_0x001c;
    L_0x00b5:
        r0 = move-exception;
        r5 = "CameraApp";
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r7 = "File Close exception : ";
        r6 = r6.append(r7);
        r7 = r0.getMessage();
        r6 = r6.append(r7);
        r6 = r6.toString();
        com.lge.camera.util.CamLog.m7i(r5, r6);
        goto L_0x001c;
    L_0x00d4:
        r0 = move-exception;
    L_0x00d5:
        r5 = "CameraApp";
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0117 }
        r6.<init>();	 Catch:{ all -> 0x0117 }
        r7 = "";
        r6 = r6.append(r7);	 Catch:{ all -> 0x0117 }
        r7 = r0.getMessage();	 Catch:{ all -> 0x0117 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0117 }
        r6 = r6.toString();	 Catch:{ all -> 0x0117 }
        com.lge.camera.util.CamLog.m7i(r5, r6);	 Catch:{ all -> 0x0117 }
        if (r1 == 0) goto L_0x001c;
    L_0x00f3:
        r1.close();	 Catch:{ Exception -> 0x00f8 }
        goto L_0x001c;
    L_0x00f8:
        r0 = move-exception;
        r5 = "CameraApp";
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r7 = "File Close exception : ";
        r6 = r6.append(r7);
        r7 = r0.getMessage();
        r6 = r6.append(r7);
        r6 = r6.toString();
        com.lge.camera.util.CamLog.m7i(r5, r6);
        goto L_0x001c;
    L_0x0117:
        r5 = move-exception;
    L_0x0118:
        if (r1 == 0) goto L_0x011d;
    L_0x011a:
        r1.close();	 Catch:{ Exception -> 0x011e }
    L_0x011d:
        throw r5;
    L_0x011e:
        r0 = move-exception;
        r6 = "CameraApp";
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "File Close exception : ";
        r7 = r7.append(r8);
        r8 = r0.getMessage();
        r7 = r7.append(r8);
        r7 = r7.toString();
        com.lge.camera.util.CamLog.m7i(r6, r7);
        goto L_0x011d;
    L_0x013c:
        r5 = move-exception;
        r1 = r2;
        goto L_0x0118;
    L_0x013f:
        r0 = move-exception;
        r1 = r2;
        goto L_0x00d5;
    L_0x0142:
        r0 = move-exception;
        r1 = r2;
        goto L_0x0088;
    L_0x0146:
        r0 = move-exception;
        r1 = r2;
        goto L_0x003d;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.constants.ModelProperties.readPenSupport(java.lang.String):int");
    }

    public static boolean isNFCBlocked() {
        return SystemProperties.getInt("ro.camera.notify_nfc", 0) == 1;
    }

    public static boolean isUserDebugMode() {
        return "userdebug".equals(SystemProperties.get("ro.build.type"));
    }

    public static void setLongLCDModel(int[] lcdSize) {
        if (Float.compare(((float) lcdSize[0]) / ((float) lcdSize[1]), 2.0f) == 0) {
            sLCDType = 1;
        } else if (((float) lcdSize[0]) / ((float) lcdSize[1]) > 2.0f) {
            sLCDType = 2;
        } else {
            sLCDType = 0;
        }
    }

    public static int getLCDType() {
        return sLCDType;
    }

    public static boolean isLongLCDModel() {
        return sLCDType >= 1;
    }

    public static boolean isLguCloudServiceModel() {
        if (sIsSupportedLguCnas == null) {
            try {
                Field field = ConfigBuildFlags.class.getField("USE_LGU_CNAS");
                field.setAccessible(true);
                if (field.isAccessible()) {
                    sIsSupportedLguCnas = Boolean.valueOf(field.getBoolean(null));
                }
                CamLog.m7i(CameraConstants.TAG, "isSupported CNAS ? " + sIsSupportedLguCnas);
            } catch (NoSuchFieldException e) {
                CamLog.m3d(CameraConstants.TAG, "USE_LGU_CNAS Field not exist");
            } catch (IllegalAccessException e2) {
                CamLog.m6e(CameraConstants.TAG, "USE_LGU_CNAS IllegalAccessException : ", e2);
            }
            if (sIsSupportedLguCnas == null) {
                sIsSupportedLguCnas = Boolean.valueOf(false);
            }
        }
        return sIsSupportedLguCnas.booleanValue();
    }

    public static boolean isRetailModeInstalled() {
        if (sIsRetailModeInstalled == null) {
            sIsRetailModeInstalled = Boolean.valueOf("1".equals(SystemProperties.get("persist.sys.store_demo_enabled")));
        }
        return sIsRetailModeInstalled == null ? false : sIsRetailModeInstalled.booleanValue();
    }

    public static boolean isLDUModel() {
        return "1".equals(SystemProperties.get("persist.LiveDemoUnit", "0"));
    }

    public static boolean isNavigationBarShowingModel() {
        return true;
    }

    public static boolean isUHDSupportedModel() {
        if (sIsUHDSupported != -1) {
            if (sIsUHDSupported == 1) {
                return true;
            }
            return false;
        } else if (!checkValidVideoSize()) {
            return false;
        } else {
            for (String size : ConfigurationVideoSizeUtil.sVIDEO_SIZE_REAR_SUPPORTED_ITEMS) {
                if (ParamConstants.VIDEO_3840_BY_2160.equals(size)) {
                    sIsUHDSupported = 1;
                    return true;
                }
            }
            sIsUHDSupported = 0;
            return false;
        }
    }

    private static boolean checkValidVideoSize() {
        String[] videoSizes = ConfigurationVideoSizeUtil.sVIDEO_SIZE_REAR_SUPPORTED_ITEMS;
        if (videoSizes == null || videoSizes[0] == null) {
            return false;
        }
        return true;
    }

    public static boolean checkInstalledLGLensApp(Context context) {
        boolean z = true;
        if (context == null) {
            return false;
        }
        if (sCheckInstalledLGLensApp != -1) {
            if (sCheckInstalledLGLensApp != 1) {
                z = false;
            }
            return z;
        }
        int i;
        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo("com.lge.ellievision", 1);
        } catch (NameNotFoundException e) {
            CamLog.m3d(CameraConstants.TAG, e.getMessage() + " is not installed.");
        }
        CamLog.m3d(CameraConstants.TAG, "isInstalledLGLensApp : " + pi);
        if (pi == null) {
            i = 0;
        } else {
            i = 1;
        }
        sCheckInstalledLGLensApp = i;
        CamLog.m3d(CameraConstants.TAG, "sCheckInstalledLGLensApp : " + sCheckInstalledLGLensApp);
        if (sCheckInstalledLGLensApp != 0) {
            return true;
        }
        return false;
    }

    public static boolean checkInstalledQLensApp(Context context) {
        boolean z = true;
        if (context == null) {
            return false;
        }
        if (sCheckInstalledQLensApp != -1) {
            if (sCheckInstalledQLensApp != 1) {
                z = false;
            }
            return z;
        }
        int i;
        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo("com.lge.qlens", 1);
        } catch (NameNotFoundException e) {
            CamLog.m3d(CameraConstants.TAG, e.getMessage() + " is not installed.");
        }
        CamLog.m3d(CameraConstants.TAG, "isInstalledQLensApp : " + pi);
        if (pi == null) {
            i = 0;
        } else {
            i = 1;
        }
        sCheckInstalledQLensApp = i;
        CamLog.m3d(CameraConstants.TAG, "sCheckInstalledQLensApp : " + sCheckInstalledQLensApp);
        if (sCheckInstalledQLensApp != 0) {
            return true;
        }
        return false;
    }

    public static boolean isSidePowerKeyModel(Context context) {
        if (sIsSidePowerKeyModel == -1) {
            int i;
            boolean rearPowerKey = context.getResources().getBoolean(R.bool.config_rearside_power_key);
            if (rearPowerKey) {
                i = 0;
            } else {
                i = 1;
            }
            sIsSidePowerKeyModel = i;
            if (rearPowerKey) {
                return false;
            }
            return true;
        } else if (sIsSidePowerKeyModel == 1) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isOSU() {
        return VERSION.IS_OS_UPGRADED;
    }
}
