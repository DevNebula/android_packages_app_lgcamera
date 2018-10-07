package com.lge.filmemulatorengine;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;

public interface OnFilmEmulationViewListener {
    void onDraw(FilmEmulatorRendererPreview filmEmulatorRendererPreview);

    void onEngineInitializeDone(SurfaceTexture surfaceTexture, SurfaceTexture surfaceTexture2);

    void onErrorOccured(int i);

    void onSendStillImage(Bitmap bitmap);
}
