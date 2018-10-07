package com.lge.effectEngine;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;

public interface LGPopoutEffectEngineListener {
    void onEngineInitializeDone(SurfaceTexture surfaceTexture, SurfaceTexture surfaceTexture2);

    void onErrorOccured(int i);

    void onSendStillImage(Bitmap bitmap);
}
