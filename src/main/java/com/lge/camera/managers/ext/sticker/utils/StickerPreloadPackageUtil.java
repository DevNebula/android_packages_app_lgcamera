package com.lge.camera.managers.ext.sticker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import com.lge.camera.util.CamLog;

public class StickerPreloadPackageUtil {
    public static final String KEY_PRELOAD_VERSION_CODE = "version_code";
    public static final String PREF_NAME = "preload_package_version_code";
    private static String PRELOAD_STICKER_PACKAGE_NAME = StickerUtil.PRELOAD_STICKER_PACKAGE_NAME;
    private static final String TAG = "StickerPreloadPackageUtil";

    public static int getPreloadPackageVersion(Context ctx) {
        if (ctx != null) {
            return ctx.getSharedPreferences(PREF_NAME, 0).getInt(KEY_PRELOAD_VERSION_CODE, -1);
        }
        return -1;
    }

    public static int getRemotePreloadPackageVersion(Context ctx) {
        if (ctx == null) {
            return -1;
        }
        try {
            PackageInfo pi = ctx.getPackageManager().getPackageInfo(PRELOAD_STICKER_PACKAGE_NAME, 0);
            CamLog.m5e(TAG, "piversion = " + pi.versionCode);
            return pi.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public static boolean isPreloadPackageUpdated(Context ctx) {
        boolean retval = false;
        if (ctx != null) {
            int savedVersion = ctx.getSharedPreferences(PREF_NAME, 0).getInt(KEY_PRELOAD_VERSION_CODE, Integer.MIN_VALUE);
            try {
                PackageInfo pi = ctx.getPackageManager().getPackageInfo(PRELOAD_STICKER_PACKAGE_NAME, 0);
                CamLog.m5e(TAG, "piversion = " + pi.versionCode);
                CamLog.m5e(TAG, "savedVersion = " + savedVersion);
                if (pi.versionCode > savedVersion) {
                    return true;
                }
                return false;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
                retval = false;
            } catch (Exception ex) {
                ex.printStackTrace();
                retval = false;
            }
        }
        return retval;
    }

    public static void saveCurrentPreloadPackageVersion(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, 0);
        try {
            PackageInfo pi = ctx.getPackageManager().getPackageInfo(PRELOAD_STICKER_PACKAGE_NAME, 0);
            Editor edit = sp.edit();
            edit.putInt(KEY_PRELOAD_VERSION_CODE, pi.versionCode);
            edit.commit();
            CamLog.m5e(TAG, "version save = " + pi.versionCode);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
            CamLog.m5e(TAG, "exception = " + ex.getMessage());
        }
    }
}
