package com.lge.camera.util;

public class AppControlUtilBase {
    private static boolean sIsLGLensLaunched = false;
    public static boolean sIsSharedActivityLaunched = false;
    private static boolean sIsVideoLaunched = false;

    public static boolean isVideoLaunched() {
        return sIsVideoLaunched;
    }

    public static void setLaunchingVideo(boolean launch) {
        sIsVideoLaunched = launch;
    }

    public static boolean isShareActivityLaunched() {
        return sIsSharedActivityLaunched;
    }

    public static void setLaunchingShareActivity(boolean launch) {
        sIsSharedActivityLaunched = launch;
    }

    public static boolean isLGLensLaunched() {
        return sIsLGLensLaunched;
    }

    public static void setLaunchingLGLens(boolean launch) {
        sIsLGLensLaunched = launch;
    }
}
