package com.lge.camera.managers;

import android.graphics.SurfaceTexture;

public interface FilmEmulatorEnginInterface {
    void onEnginReleased(boolean z, boolean z2);

    void onEngineInitializeDone(SurfaceTexture surfaceTexture);

    void onFilmEffectChanged(String str);

    void onFilmMenuHandleDone();

    void onFilterItemLongPressed();

    void updateFilterQuickButton(boolean z);
}
