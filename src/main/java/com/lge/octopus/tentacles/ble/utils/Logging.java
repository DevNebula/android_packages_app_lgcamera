package com.lge.octopus.tentacles.ble.utils;

import android.util.Log;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Logging {
    private static String PREFIX = "";
    private static Set<String> sDoNonShowTagSet = null;
    private static final boolean sLogging = true;

    private Logging() {
    }

    static {
        Set<String> m = new HashSet();
        m.add("ParseAdvertiseData");
        m.add("TdsAdvInfo");
        sDoNonShowTagSet = Collections.unmodifiableSet(m);
    }

    private static boolean isLogging(String tag) {
        return sDoNonShowTagSet.isEmpty() || !sDoNonShowTagSet.contains(tag);
    }

    public static void setPrefix(String prefix) {
        PREFIX = prefix;
    }

    /* renamed from: i */
    public static void m46i(String tag, String body) {
        if (isLogging(tag)) {
            Log.i(PREFIX + tag, body);
        }
    }

    /* renamed from: d */
    public static void m44d(String tag, String body) {
        if (isLogging(tag)) {
            Log.d(PREFIX + tag, body);
        }
    }

    /* renamed from: e */
    public static void m45e(String tag, String body) {
        if (isLogging(tag)) {
            Log.e(PREFIX + tag, body);
        }
    }

    /* renamed from: w */
    public static void m48w(String tag, String body) {
        if (isLogging(tag)) {
            Log.w(PREFIX + tag, body);
        }
    }

    /* renamed from: v */
    public static void m47v(String tag, String body) {
        if (isLogging(tag)) {
            Log.v(PREFIX + tag, body);
        }
    }
}
