package com.lge.camera.util;

import android.content.Context;
import android.graphics.Typeface;
import com.lge.camera.constants.CameraConstants;
import java.util.Hashtable;

public class CameraTypeface {
    private static final Hashtable<String, Typeface> CACHE = new Hashtable();

    public static Typeface get(Context c, String assetPath) {
        Typeface typeface;
        synchronized (CACHE) {
            if (!CACHE.containsKey(assetPath)) {
                try {
                    CACHE.put(assetPath, Typeface.createFromAsset(c.getAssets(), assetPath));
                } catch (Exception e) {
                    CamLog.m3d(CameraConstants.TAG, "Could not get typeface '" + assetPath + "' because " + e.getMessage());
                    typeface = null;
                }
            }
            typeface = (Typeface) CACHE.get(assetPath);
        }
        return typeface;
    }
}
