package com.lge.camera.util;

import android.os.Debug;
import android.util.Log;
import com.lge.camera.constants.CameraConstants;

public class DebugUtil {
    public static boolean sEnableTimeCheck = false;
    private static long sInterim_startTime = 0;
    private static long sStartTime = 0;
    public static long sTimeCheckStartTime;
    public static String sTimeCheckTag = null;

    public static void checkTimeLog(String comment, boolean start) {
        if (start) {
            sStartTime = System.nanoTime();
            return;
        }
        Log.d(CameraConstants.TAG, "CHECK TIME : " + comment + " time is = " + (System.nanoTime() - sStartTime));
        sStartTime = System.nanoTime();
    }

    public static void showElapsedTime(String tag, long startTime) {
        Log.d(CameraConstants.TAG, tag + " - elapsed time = " + (System.currentTimeMillis() - startTime));
    }

    public static void setStartTime(String tag) {
        if (sEnableTimeCheck) {
            sTimeCheckStartTime = System.currentTimeMillis();
            sTimeCheckTag = tag;
        }
    }

    public static void setEndTime(String tag) {
        if (sEnableTimeCheck) {
            long elapsedTime = System.currentTimeMillis() - sTimeCheckStartTime;
            if (tag == null || sTimeCheckTag == null) {
                Log.e(CameraConstants.TAG, "Tag is null. Make sure that setStartTime() is called before setEndTime()");
                return;
            }
            if (sTimeCheckTag.equals(tag)) {
                Log.d(CameraConstants.TAG, tag + " - time elapsed = " + elapsedTime);
            } else {
                Log.e(CameraConstants.TAG, "Check tag! setStartTime() and setEndTime() should have the same tag");
            }
            sTimeCheckTag = null;
        }
    }

    public static void debugStackTrace(String from) {
        Log.e(CameraConstants.TAG, "[Debug] Printing stack trace : from - " + from);
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i = 3; i < elements.length; i++) {
            StackTraceElement s = elements[i];
            Log.d(CameraConstants.TAG, "[Debug] \tat " + from + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
        }
    }

    public static long interimCheckTime(boolean end) {
        if (end) {
            long interim_duration = System.currentTimeMillis() - sInterim_startTime;
            sInterim_startTime = 0;
            return interim_duration;
        }
        sInterim_startTime = System.currentTimeMillis();
        return 0;
    }

    public static void printMemory() {
        long maxMem = Runtime.getRuntime().maxMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();
        long nativeHeap = Debug.getNativeHeapSize();
        long nativeFreeHeap = Debug.getNativeHeapFreeSize();
        long nativeAllocHeap = Debug.getNativeHeapAllocatedSize();
        CamLog.m7i(CameraConstants.TAG, "Max memory : " + (((float) maxMem) / 1048576.0f) + "MB");
        CamLog.m7i(CameraConstants.TAG, "Total memory : " + (((float) totalMem) / 1048576.0f) + "MB");
        CamLog.m7i(CameraConstants.TAG, "Free memory : " + (((float) freeMem) / 1048576.0f) + "MB");
        CamLog.m7i(CameraConstants.TAG, "Allocation memory : " + (((float) (totalMem - freeMem)) / 1048576.0f) + "MB");
        CamLog.m7i(CameraConstants.TAG, "Native heap memory : " + (((float) nativeHeap) / 1048576.0f) + "MB");
        CamLog.m7i(CameraConstants.TAG, "Native free heap memory : " + (((float) nativeFreeHeap) / 1048576.0f) + "MB");
        CamLog.m7i(CameraConstants.TAG, "Native used heap memory : " + (((float) nativeAllocHeap) / 1048576.0f) + "MB");
    }
}
