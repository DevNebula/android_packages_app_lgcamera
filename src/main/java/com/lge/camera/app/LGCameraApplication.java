package com.lge.camera.app;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class LGCameraApplication extends Application {
    private ThumbnailCache mCropCache;
    private ThumbnailCache mRotateCache;

    public static ThumbnailCache getCropCache(Context context) {
        return ((LGCameraApplication) context.getApplicationContext()).mCropCache;
    }

    public static ThumbnailCache getRotateCache(Context context) {
        return ((LGCameraApplication) context.getApplicationContext()).mRotateCache;
    }

    public void onCreate() {
        super.onCreate();
        int memoryClassBytes = (((ActivityManager) getSystemService("activity")).getMemoryClass() * 1024) * 1024;
        CamLog.m7i(CameraConstants.TAG, "[Tile] execute memoryClassBytes : " + (memoryClassBytes / 8));
        this.mCropCache = new ThumbnailCache(memoryClassBytes / 8);
        this.mRotateCache = new ThumbnailCache(memoryClassBytes / 8);
    }

    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= 60) {
            this.mCropCache.evictAll();
            this.mRotateCache.evictAll();
        }
    }
}
