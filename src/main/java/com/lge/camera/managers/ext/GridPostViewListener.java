package com.lge.camera.managers.ext;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.view.TextureView;
import com.lge.camera.managers.ext.GridCameraPostviewManagerBase.GridContentsInfo;

public interface GridPostViewListener {
    int getCurrentShotCount();

    int getRetakeCurrentIndex();

    boolean isCountDown();

    boolean isRetakeMode();

    boolean isSavingOnPause();

    void onCancel();

    void onSaveContents(Bitmap bitmap, boolean z);

    void onSaveContents(GridContentsInfo[] gridContentsInfoArr, boolean z);

    void setRetakeMode(int i, boolean z);

    void startRenderer(TextureView textureView, SurfaceTexture surfaceTexture);
}
