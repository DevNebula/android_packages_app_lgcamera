package com.lge.camera.managers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutInfo.Builder;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShortcutListManager {
    private static ShortcutRunnable sAutoShortcutRunnable = new C11482();
    private static ShortcutRunnable sCinemaShortcutRunnable = new C11493();
    private static ShortcutRunnable sManualCameraShortcutRunnable = new C11504();
    private static ShortcutRunnable sManualVideoShortcutRunnable = new C11515();
    private static ShortcutRunnable sSelfieShortcutRunnable = new C11471();
    private static List<ShortcutRunnable> sShortcutList = null;

    public interface ShortcutRunnable {
        String getShortcutId();

        boolean isSupported();

        ShortcutInfo makeShortcut(Context context);
    }

    /* renamed from: com.lge.camera.managers.ShortcutListManager$1 */
    static class C11471 implements ShortcutRunnable {
        C11471() {
        }

        public ShortcutInfo makeShortcut(Context context) {
            CamLog.m3d(CameraConstants.TAG, "-Shortcut- make Selfie shortcut");
            return ShortcutListManager.makeShortcutInfo(context, "com.lge.camera.shortcut_selfie_camera", "com.lge.camera", CameraConstants.MODE_BEAUTY, (int) C0088R.string.shortcuts_title_front_camera, (int) C0088R.drawable.ic_sc_selfie);
        }

        public boolean isSupported() {
            return true;
        }

        public String getShortcutId() {
            return CameraConstants.MODE_BEAUTY;
        }
    }

    /* renamed from: com.lge.camera.managers.ShortcutListManager$2 */
    static class C11482 implements ShortcutRunnable {
        C11482() {
        }

        public ShortcutInfo makeShortcut(Context context) {
            CamLog.m3d(CameraConstants.TAG, "-Shortcut- make Auto shortcut");
            return ShortcutListManager.makeShortcutInfo(context, "com.lge.camera.shortcut_auto_camera", "com.lge.camera", "mode_normal", (int) C0088R.string.shortcuts_title_back_camera, (int) C0088R.drawable.ic_sc_auto);
        }

        public boolean isSupported() {
            return true;
        }

        public String getShortcutId() {
            return "mode_normal";
        }
    }

    /* renamed from: com.lge.camera.managers.ShortcutListManager$3 */
    static class C11493 implements ShortcutRunnable {
        C11493() {
        }

        public ShortcutInfo makeShortcut(Context context) {
            CamLog.m3d(CameraConstants.TAG, "-Shortcut- make Cinema shortcut");
            return ShortcutListManager.makeShortcutInfo(context, "com.lge.camera.shortcut_cine_camera", "com.lge.camera", CameraConstants.MODE_CINEMA, (int) C0088R.string.shot_mode_cine_video, (int) C0088R.drawable.ic_sc_cine);
        }

        public boolean isSupported() {
            List<String> modeList = Arrays.asList(ConfigurationUtil.sMODE_REAR_SUPPORTED_ITEMS);
            if (modeList == null || !modeList.contains(CameraConstants.MODE_CINEMA)) {
                return false;
            }
            return true;
        }

        public String getShortcutId() {
            return CameraConstants.MODE_CINEMA;
        }
    }

    /* renamed from: com.lge.camera.managers.ShortcutListManager$4 */
    static class C11504 implements ShortcutRunnable {
        C11504() {
        }

        public ShortcutInfo makeShortcut(Context context) {
            CamLog.m3d(CameraConstants.TAG, "-Shortcut- make Manual Camera shortcut");
            return ShortcutListManager.makeShortcutInfo(context, "com.lge.camera.shortcut_manual_camera", "com.lge.camera", CameraConstants.MODE_MANUAL_CAMERA, (int) C0088R.string.shortcuts_title_manual_camera, (int) C0088R.drawable.ic_sc_m_camera);
        }

        public boolean isSupported() {
            return FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA);
        }

        public String getShortcutId() {
            return CameraConstants.MODE_MANUAL_CAMERA;
        }
    }

    /* renamed from: com.lge.camera.managers.ShortcutListManager$5 */
    static class C11515 implements ShortcutRunnable {
        C11515() {
        }

        public ShortcutInfo makeShortcut(Context context) {
            CamLog.m3d(CameraConstants.TAG, "-Shortcut- make Manual Video shortcut");
            return ShortcutListManager.makeShortcutInfo(context, "com.lge.camera.shortcut_manual_video", "com.lge.camera", CameraConstants.MODE_MANUAL_VIDEO, (int) C0088R.string.shortcuts_title_manual_video, (int) C0088R.drawable.ic_sc_m_video);
        }

        public boolean isSupported() {
            return FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO);
        }

        public String getShortcutId() {
            return CameraConstants.MODE_MANUAL_VIDEO;
        }
    }

    public static void initShortcutList(Context context) {
        if (context != null && ConfigurationUtil.sMODE_REAR_SUPPORTED_ITEMS != null) {
            ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(ShortcutManager.class);
            if (shortcutManager != null) {
                if (sShortcutList == null) {
                    sShortcutList = makeShortcutList();
                }
                initShortcut(context, shortcutManager);
            }
        }
    }

    public static void refreshShortcuts(Context context) {
        ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(ShortcutManager.class);
        if (shortcutManager != null) {
            if (sShortcutList == null) {
                sShortcutList = makeShortcutList();
            }
            List<ShortcutInfo> list = new ArrayList();
            for (int i = 0; i < sShortcutList.size(); i++) {
                list.add(((ShortcutRunnable) sShortcutList.get(i)).makeShortcut(context));
            }
            shortcutManager.updateShortcuts(list);
        }
    }

    private static void initShortcut(Context context, ShortcutManager shortcutManager) {
        List<ShortcutInfo> shortcutList = new ArrayList();
        List<String> disableList = new ArrayList();
        if (shortcutManager != null && sShortcutList != null) {
            CamLog.m3d(CameraConstants.TAG, "-Shortcut- initShortcut");
            for (int i = 0; i < sShortcutList.size(); i++) {
                ShortcutRunnable runnable = (ShortcutRunnable) sShortcutList.get(i);
                if (runnable != null) {
                    if (runnable.isSupported()) {
                        shortcutList.add(runnable.makeShortcut(context));
                    } else {
                        disableList.add(runnable.getShortcutId());
                    }
                }
            }
            if (shortcutList.size() > 0) {
                shortcutManager.setDynamicShortcuts(shortcutList);
            }
            if (disableList.size() > 0) {
                shortcutManager.disableShortcuts(disableList);
            }
        }
    }

    private static List<ShortcutRunnable> makeShortcutList() {
        List<ShortcutRunnable> list = new ArrayList();
        list.add(sSelfieShortcutRunnable);
        list.add(sAutoShortcutRunnable);
        list.add(sCinemaShortcutRunnable);
        list.add(sManualCameraShortcutRunnable);
        list.add(sManualVideoShortcutRunnable);
        return list;
    }

    public static ShortcutInfo makeShortcutInfo(Context context, String intentAction, String intentPackage, String shortcutId, int labelResid, int iconResId) {
        return makeShortcutInfo(context, intentAction, intentPackage, shortcutId, context.getString(labelResid), iconResId);
    }

    public static ShortcutInfo makeShortcutInfo(Context context, String intentAction, String intentPackage, String shortcutId, String label, int iconResId) {
        Intent intent = new Intent();
        intent.setAction(intentAction);
        intent.setPackage(intentPackage);
        intent.setFlags(268468224);
        return new Builder(context, shortcutId).setShortLabel(label).setIcon(Icon.createWithResource(context, iconResId)).setIntent(intent).build();
    }
}
