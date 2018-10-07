package com.lge.camera.util;

import android.util.FloatMath;
import com.lge.camera.constants.CameraConstants;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MathUtil {
    public static float breakFloat(float ratio, int breakCount) {
        return (float) (((double) ((int) (((double) ratio) * Math.pow(10.0d, (double) breakCount)))) / Math.pow(10.0d, (double) breakCount));
    }

    public static float byte2Float(byte[] src, int startIndex, int itemSize) {
        return ByteBuffer.wrap(src, startIndex, itemSize).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    public static float byteToInt(byte[] src) {
        return (float) ByteBuffer.wrap(src).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static boolean check21To9Ratio(int width, int height) {
        return Float.compare(0.42f, breakFloat(((float) height) / ((float) width), 2)) == 0;
    }

    public static int distance(int start, int end) {
        return Math.abs(start - end);
    }

    public static float distance(float x, float y, float sx, float sy) {
        float dx = x - sx;
        float dy = y - sy;
        return FloatMath.sqrt((dx * dx) + (dy * dy));
    }

    public static float parseStringToFloat(String value) {
        float result = 0.0f;
        if (value == null) {
            return 0.0f;
        }
        try {
            if (value.contains("/")) {
                String[] values = value.split("/");
                if (values != null && values.length > 1) {
                    result = Float.valueOf(values[0]).floatValue() / Float.valueOf(values[1]).floatValue();
                }
            } else {
                result = Float.valueOf(value).floatValue();
            }
            return result;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            CamLog.m5e(CameraConstants.TAG, "NumberFormatException : " + e.getMessage());
            return 0.0f;
        }
    }

    public static double feetToMeter(double feet) {
        return 0.3048d * feet;
    }
}
