package com.arcsoft.stickerlibrary.utils;

import java.util.LinkedHashMap;

public class BenchmarkUtil {
    private static final String TAG = BenchmarkUtil.class.getSimpleName();
    private static final int TOP_METHOD_LEVEL = 1;
    private static LinkedHashMap<String, Long> mStartTimeMap = new LinkedHashMap();

    public static synchronized void start() {
        synchronized (BenchmarkUtil.class) {
            StackTraceElement[] stacks = new Throwable().getStackTrace();
            if (stacks != null && stacks.length > 0) {
                String className = stacks[1].getClassName();
                mStartTimeMap.put(className + stacks[1].getMethodName(), Long.valueOf(System.currentTimeMillis()));
            }
        }
    }

    public static synchronized void start(String label) {
        synchronized (BenchmarkUtil.class) {
            if (label == null) {
                LogUtil.LogE(TAG, "label cannot be NULL");
            } else {
                mStartTimeMap.put(label, Long.valueOf(System.currentTimeMillis()));
            }
        }
    }

    public static synchronized void stop() {
        synchronized (BenchmarkUtil.class) {
            long endTime = System.currentTimeMillis();
            StackTraceElement[] stacks = new Throwable().getStackTrace();
            if (stacks != null && stacks.length > 0) {
                String className = stacks[1].getClassName();
                String methodName = stacks[1].getMethodName();
                String key = className + methodName;
                Long startTime = (Long) mStartTimeMap.get(key);
                if (startTime != null) {
                    printLog(className, methodName, endTime - startTime.longValue());
                    mStartTimeMap.remove(key);
                } else {
                    LogUtil.LogE(TAG, "There is no match class and method, do you forgot to call start() ?");
                }
            }
        }
    }

    public static synchronized void stop(String label) {
        synchronized (BenchmarkUtil.class) {
            if (label == null) {
                LogUtil.LogE(TAG, "label cannot be NULL");
            } else {
                long endTime = System.currentTimeMillis();
                Long startTime = (Long) mStartTimeMap.get(label);
                if (startTime != null) {
                    String className = "";
                    StackTraceElement[] stacks = new Throwable().getStackTrace();
                    if (stacks != null && stacks.length > 0) {
                        int level = 1;
                        className = stacks[1].getClassName();
                        while (className.contains(BenchmarkUtil.class.getName())) {
                            level++;
                            className = stacks[level].getClassName();
                        }
                    }
                    printLog(className + "," + label, endTime - startTime.longValue());
                    mStartTimeMap.remove(label);
                } else {
                    LogUtil.LogE(TAG, "There is no match class and method, do you forgot to call start(label) ?");
                }
            }
        }
    }

    public static void reset() {
        if (mStartTimeMap != null) {
            mStartTimeMap.clear();
        }
    }

    private static void printLog(String className, String methodName, long time) {
        LogUtil.LogI(TAG, new StringBuffer().append("\t").append(className).append(".").append("[" + methodName + "]").append("\tcost\t").append(time).append("\tms").toString());
    }

    private static void printLog(String str, long time) {
        LogUtil.LogI(TAG, new StringBuffer().append("\t").append(str).append("\tcost\t").append(time).append("\tms").toString());
    }
}
