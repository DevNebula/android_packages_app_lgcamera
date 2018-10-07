package com.lge.gallery.xmp.encoder.util;

import android.util.Log;
import java.io.Closeable;

public class Utils {
    private static final String TAG = "Utils";

    public static void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable t) {
                Log.w(TAG, "close fail", t);
            }
        }
    }
}
