package com.lge.camera.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.ModelProperties;

public class PackageUtil {
    private static final String AUTHORITY = "com.lge.appbox.contentprovider";
    private static final String PACKAGE_NAME_FIELD = "package_name";
    private static final String[] TABLE_UPDATEAPP_PROJ = new String[]{PACKAGE_NAME_FIELD};
    private static final Uri UPDATE_APP_INFO_CONTENT_URI = Uri.parse("content://com.lge.appbox.contentprovider/update_app_info");
    private static int sIsGalleryInstalled = -1;
    private static boolean sIsLGSmartworldPreloaded = true;

    public static String getPackageName(String className) {
        String packageName = "";
        if (className == null) {
            return null;
        }
        String[] sptPackageName = className.split("\\.");
        if (sptPackageName == null || sptPackageName.length < 3) {
            return null;
        }
        for (int i = 0; i < 3; i++) {
            packageName = packageName + sptPackageName[i] + ".";
        }
        return packageName.substring(0, packageName.length() - 1);
    }

    public static boolean isAppInstalled(Activity activity, String packageName) {
        PackageManager pm = activity.getPackageManager();
        String url = "";
        try {
            if (pm.getApplicationInfo(packageName, 128) != null) {
                return true;
            }
            return false;
        } catch (NameNotFoundException e) {
            try {
                pm.getPackageInfo("com.android.vending", 0);
                url = "market://details?id=" + packageName;
            } catch (Exception e2) {
                url = "https://play.google.com/store/apps/details?id=" + packageName;
            }
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
            intent.addFlags(268959744);
            activity.startActivity(intent);
            return false;
        }
    }

    public static void launchApp(Activity activity, String packageName, String className) {
        ApplicationInfo info = null;
        try {
            info = activity.getPackageManager().getApplicationInfo(packageName, 128);
        } catch (NameNotFoundException e) {
            Toast.makeText(activity.getApplicationContext(), C0088R.string.error_not_exist_app, 0).show();
        }
        if (info == null || info.enabled) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, className));
            intent.addFlags(67108864);
            try {
                activity.startActivity(intent);
                return;
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(activity.getApplicationContext(), C0088R.string.error_not_exist_app, 0).show();
                return;
            }
        }
        Toast.makeText(activity.getApplicationContext(), C0088R.string.error_not_exist_app, 0).show();
    }

    public static void checkAppUpdated(Context context) {
        if (context != null) {
            CamLog.m7i(CameraConstants.TAG, " check App updte");
            Intent svcIntent = new Intent("com.lge.appbox.commonservice.update");
            svcIntent.setComponent(new ComponentName("com.lge.appbox.client", "com.lge.appbox.service.AppBoxCommonService"));
            svcIntent.putExtra("packagename", context.getPackageName());
            svcIntent.putExtra("type", "update");
            context.startService(svcIntent);
        }
    }

    public static void checkLGSmartWorldPreloaded(Context context) {
        if (ModelProperties.getRawAppTier() < 5) {
            sIsLGSmartworldPreloaded = false;
            return;
        }
        boolean isExist = isExistsApkInUpdateCenter(context, CameraConstantsEx.LG_SMART_WORLD).booleanValue();
        CamLog.m3d(CameraConstants.TAG, "isExist smart world in download provider : " + isExist);
        if (isExist || isLGSmartWorldInstalled(context)) {
            sIsLGSmartworldPreloaded = true;
        } else {
            sIsLGSmartworldPreloaded = false;
        }
    }

    private static Boolean isExistsApkInUpdateCenter(Context context, String packageName) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(UPDATE_APP_INFO_CONTENT_URI, TABLE_UPDATEAPP_PROJ, "package_name = ?", new String[]{packageName}, null);
            Boolean valueOf;
            if (cursor == null) {
                valueOf = Boolean.valueOf(false);
                if (cursor == null) {
                    return valueOf;
                }
                cursor.close();
                return valueOf;
            } else if (cursor.moveToFirst()) {
                valueOf = Boolean.valueOf(true);
                if (cursor == null) {
                    return valueOf;
                }
                cursor.close();
                return valueOf;
            } else {
                if (cursor != null) {
                    cursor.close();
                }
                return Boolean.valueOf(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean isLGSmartWorldPreloaded() {
        return sIsLGSmartworldPreloaded;
    }

    public static boolean isLGSmartWorldInstalled(Context context) {
        try {
            if (context.getPackageManager().getApplicationInfo(CameraConstantsEx.LG_SMART_WORLD, 0) != null) {
                return true;
            }
            return false;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isLGSmartWorldEnabled(Context context) {
        boolean z = false;
        try {
            return context.getPackageManager().getApplicationInfo(CameraConstantsEx.LG_SMART_WORLD, 0).enabled;
        } catch (NameNotFoundException e) {
            return z;
        }
    }

    public static void checkLGSmartWorldUpdated(Context context) {
        if (context != null) {
            Intent intent = new Intent("com.lge.appbox.commonservice.update");
            intent.setComponent(new ComponentName("com.lge.appbox.client", "com.lge.appbox.service.AppBoxCommonService"));
            intent.putExtra("packagename", CameraConstantsEx.LG_SMART_WORLD);
            intent.putExtra("type", "download");
            context.startService(intent);
        }
    }

    public static boolean isGoogleLensInstalled(Context context) {
        try {
            if (context.getPackageManager().getApplicationInfo("com.google.ar.lens", 0) != null) {
                return true;
            }
            return false;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static void setLGGalleryInstalled(Context context) {
        if (sIsGalleryInstalled == -1 && context != null) {
            PackageManager pm = context.getPackageManager();
            CamLog.m3d(CameraConstants.TAG, "getApplicationInfo START");
            try {
                int i;
                if (pm.getApplicationInfo(CameraConstants.PACKAGE_GALLERY, 0) != null) {
                    i = 1;
                } else {
                    i = 0;
                }
                sIsGalleryInstalled = i;
            } catch (NameNotFoundException e) {
                sIsGalleryInstalled = 0;
            }
            CamLog.m3d(CameraConstants.TAG, "getApplicationInfo END, sIsGalleryInstalled = " + sIsGalleryInstalled);
        }
    }

    public static boolean isLGGalleryInstalled(Context context) {
        if (sIsGalleryInstalled == -1) {
            setLGGalleryInstalled(context);
            if (sIsGalleryInstalled != 1) {
                return false;
            }
            return true;
        } else if (sIsGalleryInstalled == 1) {
            return true;
        } else {
            return false;
        }
    }
}
