package com.lge.camera.util;

import android.app.Activity;
import com.lge.camera.constants.CameraConstants;
import com.lge.mdm.LGMDMManager;
import com.lge.mdm.config.LGMDMApplicationState;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MDMUtil {
    public static final int DISABLED = 2;
    public static boolean sIsMdmBuildFlag = confirmMdmBuildFlag();

    public static void loadMDMInstance() {
        LGMDMManager.getInstance();
    }

    public static boolean allowCamera(Activity activity) {
        if (!sIsMdmBuildFlag) {
            return true;
        }
        LGMDMManager lgMdmManager = LGMDMManager.getInstance();
        if (lgMdmManager == null || lgMdmManager.getAllowCameraWithWhitelist(null)) {
            return true;
        }
        List<String> list = lgMdmManager.getCameraWhitelist(null);
        if (list == null || !list.contains("com.lge.camera")) {
            return false;
        }
        return true;
    }

    public static boolean allowMicrophone() {
        try {
            if (!sIsMdmBuildFlag || LGMDMManager.getInstance().getAllowMicrophone(null)) {
                return true;
            }
            List<String> list = LGMDMManager.getInstance().getMicrophoneWhitelist(null);
            if (list != null) {
                for (String str : list) {
                    if ("com.lge.camera".equals(str)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (NoClassDefFoundError e) {
            return true;
        }
    }

    public static boolean confirmMdmBuildFlag() {
        try {
            String configBuildFlagStr = "com.lge.config.ConfigBuildFlags";
            Field field = Class.forName("com.lge.config.ConfigBuildFlags").getField("CAPP_MDM");
            field.setAccessible(true);
            if (field.isAccessible()) {
                return field.getBoolean(Class.forName("com.lge.config.ConfigBuildFlags"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isAppDisabledByMDM(String packageName) {
        Iterator<LGMDMApplicationState> iter = ((ArrayList) LGMDMManager.getInstance().getApplicationState(null)).iterator();
        while (iter.hasNext()) {
            LGMDMApplicationState appState = (LGMDMApplicationState) iter.next();
            if (packageName.equals(appState.getPackageName())) {
                int result = appState.getEnable();
                CamLog.m3d(CameraConstants.TAG, "MDM packageName = " + packageName + " result = " + result);
                if (result == 2) {
                    return true;
                }
            }
        }
        return false;
    }
}
