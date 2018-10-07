package com.lge.camera.device;

import android.text.TextUtils.SimpleStringSplitter;
import android.text.TextUtils.StringSplitter;
import java.util.ArrayList;

public final class ParamUtils {
    public static int getFlashMode(String value) {
        if ("auto".equals(value)) {
            return 52;
        }
        if ("on".equals(value)) {
            return 51;
        }
        if (ParamConstants.FLASH_MODE_REAR_ON.equals(value)) {
            return 53;
        }
        return 50;
    }

    public static String getFlashMode(int value) {
        String flashMode = "off";
        switch (value) {
            case 51:
                return "on";
            case 52:
                return "auto";
            case 53:
                return ParamConstants.FLASH_MODE_REAR_ON;
            default:
                return "off";
        }
    }

    public static boolean isSame(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null || !s1.equals(s2)) {
            return false;
        }
        return true;
    }

    public static ArrayList<String> split(String str) {
        if (str == null) {
            return null;
        }
        StringSplitter<String> splitter = new SimpleStringSplitter(',');
        splitter.setString(str);
        ArrayList<String> substrings = new ArrayList();
        for (String s : splitter) {
            substrings.add(s);
        }
        return substrings;
    }

    public static ArrayList<Integer> splitInt(String str) {
        if (str == null) {
            return null;
        }
        StringSplitter<String> splitter = new SimpleStringSplitter(',');
        splitter.setString(str);
        ArrayList<Integer> substrings = new ArrayList();
        for (String s : splitter) {
            substrings.add(Integer.valueOf(Integer.parseInt(s)));
        }
        if (substrings.size() == 0) {
            return null;
        }
        return substrings;
    }

    public static void splitInt(String str, int[] output) {
        if (str != null) {
            StringSplitter<String> splitter = new SimpleStringSplitter(',');
            splitter.setString(str);
            int index = 0;
            for (String s : splitter) {
                int index2 = index + 1;
                output[index] = Integer.parseInt(s);
                index = index2;
            }
        }
    }

    public static String convertShotMode(String str) {
        if (str == null) {
            return str;
        }
        String[] mode = str.split("=");
        if (mode[0] == null || !mode[0].startsWith("mode_")) {
            return mode[0];
        }
        return mode[0].substring(5);
    }
}
