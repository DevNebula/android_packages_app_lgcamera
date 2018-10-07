package com.lge.camera.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.net.Uri.Builder;
import com.lge.camera.database.OverlapProjectDbAdapter;
import com.lge.camera.database.OverlapProjectDefaults;

public class SquareUtil {
    public static String getSampleFilesDir(Context context) {
        return context.getFilesDir().toString() + "/";
    }

    public static Uri getOverlapSampleUri(int preset, int subIndex, String imagePath) {
        Builder builder = Uri.parse(OverlapProjectDbAdapter.URI_OVERLAP).buildUpon();
        builder.appendQueryParameter("preset", String.valueOf(preset));
        builder.appendQueryParameter(OverlapProjectDbAdapter.URI_PARAM_PRESET_SUB, String.valueOf(subIndex));
        builder.appendQueryParameter("sample_path", imagePath);
        return builder.build();
    }

    public static Bitmap getOverlapSampleBitmap(Context context, Uri uri, int reqWidth, int reqHeight) {
        int preset = Integer.parseInt(uri.getQueryParameter("preset"));
        String imagePath = uri.getQueryParameter("sample_path");
        if (preset == -1) {
            return decodeSampledBitmap(context, getSampleFilesDir(context) + imagePath + "", reqWidth, reqWidth);
        }
        return decodeSampledBitmap(context, OverlapProjectDefaults.getSubOfDefaultOne(preset, Integer.parseInt(uri.getQueryParameter(OverlapProjectDbAdapter.URI_PARAM_PRESET_SUB))) + "", reqWidth, reqHeight);
    }

    public static Bitmap decodeSampledBitmap(Context context, String resId, int reqWidth, int reqHeight) {
        Resources res = context.getResources();
        boolean isUserSample = resId.contains(getSampleFilesDir(context));
        int resInt = isUserSample ? 0 : Integer.parseInt(resId);
        Options options = new Options();
        options.inJustDecodeBounds = true;
        if (isUserSample) {
            reqWidth /= 2;
            reqHeight /= 2;
            BitmapFactory.decodeFile(resId, options);
        } else {
            BitmapFactory.decodeResource(res, resInt, options);
        }
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        if (isUserSample) {
            return BitmapFactory.decodeFile(resId, options);
        }
        return BitmapFactory.decodeResource(res, resInt, options);
    }

    public static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static int getHeight(Context context) {
        return Utils.getLCDsize(context, true)[1];
    }

    public static int checkIndexBoundary(int index, int listSize) {
        int result = index;
        if (result >= listSize) {
            result = listSize - 1;
        }
        if (result < 0) {
            return 0;
        }
        return result;
    }

    public static int getThumbnailDegree(int degree, int orientationDegree, boolean forAnimationView) {
        if (!forAnimationView) {
            return degree;
        }
        int result = degree + orientationDegree;
        if (orientationDegree == 90 || orientationDegree == 270) {
            return (result + 180) % 360;
        }
        return result;
    }
}
