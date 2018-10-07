package com.lge.camera.managers.ext;

import android.graphics.Bitmap;

public interface SplicePostViewListener {
    int getFrameshotCountForPostView();

    void onCancel();

    void onImageTransformed(int i);

    void onSaveContents(Bitmap bitmap, boolean z);

    void onSaveContents(boolean z);

    void removePostView();
}
