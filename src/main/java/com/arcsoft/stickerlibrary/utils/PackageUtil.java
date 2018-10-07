package com.arcsoft.stickerlibrary.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

public class PackageUtil {
    public static String getPackageName(Context context) {
        if (context == null) {
            return null;
        }
        try {
            return context.getApplicationContext().getPackageManager().getPackageInfo(context.getPackageName(), 0).packageName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getVersionName(Context context) {
        if (context == null) {
            return null;
        }
        try {
            return context.getApplicationContext().getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int getVersionCode(Context context) {
        int i = 0;
        if (context == null) {
            return i;
        }
        try {
            return context.getApplicationContext().getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return i;
        }
    }

    public static String getApplicationLabel(Context context) {
        if (context == null) {
            return null;
        }
        ApplicationInfo applicationInfo;
        PackageManager packageManager = null;
        try {
            packageManager = context.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            applicationInfo = null;
        }
        return (String) packageManager.getApplicationLabel(applicationInfo);
    }

    public static void restartApplication(Activity activity) {
        if (activity != null) {
            Context baseContext = activity.getBaseContext();
            Intent intent = baseContext.getPackageManager().getLaunchIntentForPackage(baseContext.getPackageName());
            intent.addFlags(67108864);
            activity.startActivity(intent);
        }
    }

    public static boolean gotoMarket(Activity activity) {
        try {
            activity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + activity.getPackageName())));
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }
}
