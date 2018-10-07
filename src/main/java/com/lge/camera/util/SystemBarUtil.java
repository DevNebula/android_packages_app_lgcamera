package com.lge.camera.util;

import android.app.ActionBar;
import android.app.Activity;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings.System;
import android.util.TypedValue;
import android.view.WindowManager.LayoutParams;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.view.ViewUtil;

public class SystemBarUtil {
    private static final String ENABLE_HIDE_NAVIGATION = "enable_hide_navigation";
    public static final int SYSTEM_UI_FLAG_BLACK_NAVIGATION = 16;

    public static void setTranslucentNavigationBar(Activity activity) {
        LayoutParams winParams = activity.getWindow().getAttributes();
        if (winParams != null) {
            winParams.flags |= 67108864;
            activity.getWindow().setAttributes(winParams);
        }
        activity.getWindow().getDecorView().setSystemUiVisibility((((activity.getWindow().getDecorView().getSystemUiVisibility() | 256) | 512) | 1024) | 4);
    }

    public static boolean isEnableHideNavigation(Context context) {
        int result = System.getInt(context.getContentResolver(), ENABLE_HIDE_NAVIGATION, 0);
        CamLog.m3d(CameraConstants.TAG, "enable_hide_navigation = " + result);
        if (result == 1) {
            return true;
        }
        return false;
    }

    public static boolean isSystemUIVisible(Activity activity) {
        boolean z = true;
        if (ModelProperties.isNavigationBarShowingModel()) {
            return false;
        }
        boolean isHideNavi;
        if ((activity.getWindow().getDecorView().getSystemUiVisibility() & 2) != 0) {
            isHideNavi = true;
        } else {
            isHideNavi = false;
        }
        if (!ModelProperties.isSoftKeyNavigationBarModel(activity.getApplicationContext())) {
            return false;
        }
        if (isHideNavi) {
            z = false;
        }
        return z;
    }

    public static void hideSystemUI(Activity activity) {
        if (!ModelProperties.isNavigationBarShowingModel()) {
            setSystemUiVisibility(activity, false);
        }
    }

    public static void setSystemUiVisibility(Activity activity, boolean visible) {
        if (visible) {
            CamLog.m3d(CameraConstants.TAG, "show system UI");
            activity.getWindow().getDecorView().setSystemUiVisibility(1792);
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "hide system UI");
        removeLightStatusBarFlag(activity);
        activity.getWindow().getDecorView().setSystemUiVisibility((((((activity.getWindow().getDecorView().getSystemUiVisibility() | 256) | 512) | 1024) | 2) | 4) | 4096);
    }

    public static void removeLightStatusBarFlag(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(activity.getWindow().getDecorView().getSystemUiVisibility() & -8193);
    }

    public static void showSystemUI(Activity activity) {
        if (!ModelProperties.isNavigationBarShowingModel()) {
            setSystemUiVisibility(activity, true);
        }
    }

    public static void setImmersiveStickyForSystemUI(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(5894);
    }

    public static void showSystemUIonHelpActivity(Activity activity) {
        CamLog.m3d(CameraConstants.TAG, "show system UI");
        activity.getWindow().getDecorView().setSystemUiVisibility(activity.getWindow().getDecorView().getSystemUiVisibility() | 256);
    }

    public static void showSystemUIonPostview(Activity activity) {
        if (!ModelProperties.isNavigationBarShowingModel()) {
            CamLog.m3d(CameraConstants.TAG, "show system UI");
            activity.getWindow().getDecorView().setSystemUiVisibility(784);
        }
    }

    public static void hideSystemUIonPostview(Activity activity) {
        if (!ModelProperties.isNavigationBarShowingModel()) {
            CamLog.m3d(CameraConstants.TAG, "hide system UI");
            activity.getWindow().getDecorView().setSystemUiVisibility(((((((activity.getWindow().getDecorView().getSystemUiVisibility() | 256) | 512) | 1024) | 2) | 4) | 2048) | 16);
        }
    }

    public static void disableNavigationButton(Activity activity) {
        ViewUtil.setLGSystemUiVisibility(activity.getWindow().getDecorView(), 393216);
    }

    public static int getNavibarHeight(Context context) {
        if (context != null) {
            Resources resources = context.getResources();
            int resId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resId > 0) {
                return resources.getDimensionPixelSize(resId);
            }
        }
        return 0;
    }

    public static void setActionBarAnim(Activity activity, boolean enable) {
        if (activity != null) {
            try {
                ActionBar actionBar = activity.getActionBar();
                if (actionBar != null) {
                    actionBar.getClass().getDeclaredMethod("setShowHideAnimationEnabled", new Class[]{Boolean.TYPE}).invoke(actionBar, new Object[]{Boolean.valueOf(enable)});
                }
            } catch (Exception exception) {
                CamLog.m4d(CameraConstants.TAG, "Exception : ", exception);
            }
        }
    }

    public static int getActionBarSize(Activity activity) {
        if (activity == null) {
            return 0;
        }
        int actionBarHeight = activity.getActionBar().getHeight();
        TypedValue tv = new TypedValue();
        if (activity.getTheme().resolveAttribute(16843499, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    public static boolean setActionBarVisible(Activity activity, boolean show) {
        if (activity != null) {
            ActionBar actBar = activity.getActionBar();
            if (actBar != null) {
                if (show) {
                    if (!actBar.isShowing()) {
                        actBar.show();
                        return true;
                    }
                } else if (actBar.isShowing()) {
                    actBar.hide();
                    return true;
                }
            }
        }
        return false;
    }

    public static void disableStatusBarExpand(Context context, boolean disable) {
        if (context != null) {
            StatusBarManager statusBarManager = (StatusBarManager) context.getSystemService("statusbar");
            if (statusBarManager == null) {
                return;
            }
            if (disable) {
                try {
                    statusBarManager.disable(65536);
                    return;
                } catch (SecurityException se) {
                    CamLog.m5e(CameraConstants.TAG, "SecurityException " + se);
                    return;
                }
            }
            statusBarManager.disable(0);
        }
    }
}
