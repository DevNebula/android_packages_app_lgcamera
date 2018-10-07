package com.lge.camera.app;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.LruCache;

public class ThumbnailCache extends LruCache<Uri, Bitmap> {
    public ThumbnailCache(int maxSize) {
        super(maxSize);
    }

    protected int sizeOf(Uri key, Bitmap value) {
        return value.getByteCount();
    }
}
