package com.lge.camera.util;

import android.content.Context;
import android.util.Rational;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.api2.Camera2Util;

public class ManualUtil {
    private static final int LG_ISO_SENSITIVITY = 50;

    public static boolean isManualCameraMode(String shotMode) {
        return CameraConstants.MODE_MANUAL_CAMERA.equals(shotMode);
    }

    public static boolean isManualVideoMode(String shotMode) {
        return CameraConstants.MODE_MANUAL_VIDEO.equals(shotMode);
    }

    public static boolean isManualCameraMode(int viewMode) {
        return viewMode == 2;
    }

    public static boolean isManualVideoMode(int viewMode) {
        return viewMode == 3;
    }

    public static boolean isCinemaSize(Context context, int contentWidth, int contentHeight) {
        if (((float) contentWidth) / ((float) contentHeight) > 2.3f) {
            return true;
        }
        return false;
    }

    public static boolean isCinemaSize(Context context, String contentSize) {
        int[] contentSizeArr = null;
        if (contentSize != null) {
            contentSizeArr = Utils.sizeStringToArray(contentSize);
            if (contentSizeArr == null || contentSizeArr.length <= 1) {
                return false;
            }
        }
        if (contentSizeArr != null) {
            return isCinemaSize(context, contentSizeArr[0], contentSizeArr[1]);
        }
        return false;
    }

    public static double getShutterSpeedInDouble(String ss) {
        int divideIndex = ss.indexOf("/");
        if (divideIndex != -1) {
            String denominator = ss.substring(divideIndex + 1);
            if ("10".equals(denominator)) {
                return Double.parseDouble(ss.substring(0, divideIndex)) / Double.parseDouble(denominator);
            }
            return (double) (1.0f / Float.parseFloat(denominator));
        } else if ("0".equals(ss)) {
            return 0.0d;
        } else {
            return Double.parseDouble(ss);
        }
    }

    public static float selectValueCheckingLCDSize(float normalValue, float longLCDValue) {
        return ModelProperties.isLongLCDModel() ? longLCDValue : normalValue;
    }

    public static long convertShutterSpeedMilliToNano(String value) {
        Double milliSec = Double.valueOf(0.0d);
        if (value == null || "0".equals(value)) {
            return 333333333;
        }
        if (value.indexOf("/") == -1) {
            milliSec = Double.valueOf(value);
        } else {
            milliSec = Double.valueOf(Rational.parseRational(value).doubleValue());
        }
        return (long) (milliSec.doubleValue() * 1000000.0d);
    }

    public static float convertShutterSpeedNanoToSec(String value) {
        if (value == null || "0".equals(value)) {
            return 33.0f;
        }
        return (float) (Double.parseDouble(value) / 1.0E9d);
    }

    public static void convertKelvinToRGB(Integer[] color, int kelvin) {
        int red;
        int green;
        int blue;
        int temp = kelvin / 100;
        if (temp < 66) {
            red = 255;
            green = (int) ((99.4708025861d * Math.log((double) temp)) - 161.1195681661d);
            if (temp <= 19) {
                blue = 0;
            } else {
                blue = (int) ((138.5177312231d * Math.log((double) (temp - 10))) - 305.0447927307d);
            }
        } else {
            red = (int) (329.698727446d * Math.pow((double) (temp - 60), -0.1332047592d));
            green = (int) (288.1221695283d * Math.pow((double) (temp - 60), -0.0755148492d));
            blue = 255;
        }
        color[0] = Integer.valueOf(Camera2Util.clamp(red, 0, 255));
        color[1] = Integer.valueOf(Camera2Util.clamp(green, 0, 255));
        color[2] = Integer.valueOf(Camera2Util.clamp(blue, 0, 255));
    }

    public static int convertRGBtoKenlvin(Integer[] color, int kelvin) {
        return 3800;
    }

    public static int convertISO(String value) {
        return (int) (Math.floor(Double.parseDouble(value) / 50.0d) * 50.0d);
    }

    public static int convertISO(float value) {
        return (int) (Math.floor((double) (value / 50.0f)) * 50.0d);
    }

    public static double calculateIlluminance(float currentISO, float currentShutterSpeed, float currentAperture) {
        return (Math.pow((double) currentAperture, 2.0d) / ((double) currentShutterSpeed)) / ((double) currentISO);
    }
}
