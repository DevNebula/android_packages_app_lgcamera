package com.lge.camera.managers;

import android.net.Uri;

public interface GalleryManagerInterface {
    boolean isDeleteAvailable();

    void onDeleteButtonClicked();

    void onGalleryBitmapLoaded(boolean z);

    void onGalleryImageViewClicked();

    void onGalleryPageChanged(Uri uri, boolean z, boolean z2);

    void onGalleryViewTouched(boolean z);

    void onProjectMove();

    void setStartGalleryLocation(float[] fArr);

    void startGallery(Uri uri, int i);
}
