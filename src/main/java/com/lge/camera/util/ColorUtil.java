package com.lge.camera.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.renderscript.Allocation;
import android.renderscript.Allocation.MipmapControl;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.p000v4.view.ViewCompat;
import com.lge.camera.constants.CameraConstants;

public class ColorUtil {
    private static RenderScript sRS = null;

    public static ColorMatrixColorFilter getBrightnessAndContrast(float brightness, float contrast) {
        ColorMatrix cm = new ColorMatrix();
        cm.set(new float[]{contrast, 0.0f, 0.0f, 0.0f, brightness, 0.0f, contrast, 0.0f, 0.0f, brightness, 0.0f, 0.0f, contrast, 0.0f, brightness, 0.0f, 0.0f, 0.0f, contrast, 0.0f});
        return new ColorMatrixColorFilter(cm);
    }

    public static ColorMatrixColorFilter getAlphaMatrix(float alpha) {
        ColorMatrix cm = new ColorMatrix();
        cm.set(new float[]{1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, alpha, 0.0f});
        return new ColorMatrixColorFilter(cm);
    }

    public static ColorMatrixColorFilter getColorMatrix(float alpha, float r, float g, float b) {
        ColorMatrix cm = new ColorMatrix();
        g /= 255.0f;
        b /= 255.0f;
        alpha /= 255.0f;
        cm.set(new float[]{r / 255.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, g, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, b, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, alpha, 0.0f});
        return new ColorMatrixColorFilter(cm);
    }

    public static ColorMatrixColorFilter getDimColor() {
        return getBrightnessAndContrast(50.0f, 0.6f);
    }

    public static ColorMatrixColorFilter getQuickButtonDimColor() {
        return getBrightnessAndContrast(-40.0f, 0.5f);
    }

    public static ColorMatrixColorFilter getNormalColorByAlpha() {
        return getAlphaMatrix(1.0f);
    }

    public static ColorMatrixColorFilter getDimColorByAlpha() {
        return getAlphaMatrix(0.35f);
    }

    public static ColorMatrixColorFilter getDimColorExpand() {
        return getBrightnessAndContrast(70.0f, 0.2f);
    }

    public static ColorMatrixColorFilter getEnteringQFLColor() {
        return getBrightnessAndContrast(150.0f, 0.6f);
    }

    public static ColorMatrixColorFilter getSeletedColor() {
        return getColorMatrix(255.0f, 75.0f, 219.0f, 190.0f);
    }

    public static ColorMatrixColorFilter getSeletedColor(int colorValue) {
        return getColorMatrix(255.0f, (float) Color.red(colorValue), (float) Color.green(colorValue), (float) Color.blue(colorValue));
    }

    public int getSettingMenuDimColor() {
        return Color.argb(255, 51, 56, 59);
    }

    public static synchronized void createRS(Context context) {
        synchronized (ColorUtil.class) {
            if (sRS == null) {
                CamLog.m3d(CameraConstants.TAG, "createRS Start");
                sRS = RenderScript.create(context);
                CamLog.m3d(CameraConstants.TAG, "createRS End");
            }
        }
    }

    public static synchronized void destroyRS() {
        synchronized (ColorUtil.class) {
            if (sRS != null) {
                CamLog.m3d(CameraConstants.TAG, "destroyRS");
                sRS.destroy();
                sRS = null;
            }
        }
    }

    public static Bitmap getBlurImage(Context context, Bitmap bitmapIn, int blurRadius) {
        if (sRS == null) {
            createRS(context);
        }
        if (context == null || bitmapIn == null || sRS == null) {
            return null;
        }
        try {
            Bitmap mBitmapOut = Bitmap.createBitmap(bitmapIn);
            Allocation mInputAlloc = Allocation.createFromBitmap(sRS, bitmapIn, MipmapControl.MIPMAP_FULL, 1);
            Allocation mOutputAlloc = Allocation.createTyped(sRS, mInputAlloc.getType());
            ScriptIntrinsicBlur mScript = ScriptIntrinsicBlur.create(sRS, Element.U8_4(sRS));
            mScript.setInput(mInputAlloc);
            mScript.setRadius((float) blurRadius);
            mScript.forEach(mOutputAlloc);
            mOutputAlloc.copyTo(mBitmapOut);
            sRS.finish();
            return mBitmapOut;
        } catch (Exception e) {
            CamLog.m6e(CameraConstants.TAG, "getBlurImage Exception : ", e);
            return bitmapIn;
        }
    }

    public static int getColorAccentFromTheme(Context context) {
        if (context == null) {
            return 0;
        }
        TypedArray style = context.getTheme().obtainStyledAttributes(new int[]{16843829});
        int color = style.getColor(0, 0);
        style.recycle();
        return color;
    }

    public static int getTextColorPrimaryFromTheme(Context context) {
        if (context == null) {
            return ViewCompat.MEASURED_STATE_MASK;
        }
        TypedArray style = context.getTheme().obtainStyledAttributes(new int[]{16842806});
        int color = style.getColor(0, 0);
        style.recycle();
        return color;
    }
}
