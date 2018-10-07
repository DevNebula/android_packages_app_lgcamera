package com.lge.effectEngine2;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;

public interface LGPopoutEffectEngineListener {
    void displayFPS(float f, boolean z);

    void onEngineInitializeDone(SurfaceTexture surfaceTexture, SurfaceTexture surfaceTexture2);

    void onErrorOccured(int i);

    RectF onRequestBoundaryPosition();

    void onSendStillImage(Bitmap bitmap);
}
