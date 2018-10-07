package android.support.p000v4.app;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.annotation.RequiresApi;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@TargetApi(19)
@RequiresApi(19)
/* renamed from: android.support.v4.app.NotificationManagerCompatKitKat */
class NotificationManagerCompatKitKat {
    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

    NotificationManagerCompatKitKat() {
    }

    public static boolean areNotificationsEnabled(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService("appops");
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        try {
            Class<?> appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, new Class[]{Integer.TYPE, Integer.TYPE, String.class});
            int value = ((Integer) appOpsClass.getDeclaredField(OP_POST_NOTIFICATION).get(Integer.class)).intValue();
            if (((Integer) checkOpNoThrowMethod.invoke(appOps, new Object[]{Integer.valueOf(value), Integer.valueOf(uid), pkg})).intValue() == 0) {
                return true;
            }
            return false;
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e2) {
        } catch (NoSuchFieldException e3) {
        } catch (InvocationTargetException e4) {
        } catch (IllegalAccessException e5) {
        } catch (RuntimeException e6) {
        }
        return true;
    }
}
