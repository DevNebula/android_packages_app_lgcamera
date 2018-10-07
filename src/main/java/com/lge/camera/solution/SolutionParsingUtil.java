package com.lge.camera.solution;

import android.util.Log;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationUtilBase;
import com.lge.camera.util.CamLog;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;

public class SolutionParsingUtil extends ConfigurationUtilBase {
    public static final String SOLUTION_CFG_DIR = "/system/etc/";
    public static final String SOLUTION_CFG_EXT = ".xml";
    public static final String SOLUTION_CFG_FILE = "solution_config";
    public static final String SOLUTION_CFG_NOT_FOUND = "not found";
    public static final String SOLUTION_CFG_PATH_DEFAULT = "/system/etc/solution_config.xml";
    protected static final String SOLUTION_NAME_TAG = "solutionname";
    protected static final String SOLUTION_PARM_TAG = "solutionparam";
    protected static final String SOLUTION_SUPPORT_FALSE = "false";
    protected static final String SOLUTION_SUPPORT_TAG = "solutionsupport";
    protected static final String SOLUTION_SUPPORT_TRUE = "true";
    protected static final String SOLUTION_TAG = "solution";
    public static List<ArrayList<HashMap<String, String>>> sSolutionList = null;
    public static List<HashMap<String, String>> sSolutionParamList = null;

    protected static String getConfigFilePath() {
        if (new File(SOLUTION_CFG_PATH_DEFAULT).exists()) {
            return SOLUTION_CFG_PATH_DEFAULT;
        }
        return "not found";
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0072 A:{SYNTHETIC, Splitter: B:29:0x0072} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00b1 A:{SYNTHETIC, Splitter: B:46:0x00b1} */
    protected static synchronized void parseConfig() {
        /*
        r14 = 1;
        r11 = com.lge.camera.solution.SolutionParsingUtil.class;
        monitor-enter(r11);
        r10 = sSolutionList;	 Catch:{ all -> 0x007d }
        if (r10 != 0) goto L_0x000c;
    L_0x0008:
        r10 = sSolutionParamList;	 Catch:{ all -> 0x007d }
        if (r10 == 0) goto L_0x000e;
    L_0x000c:
        monitor-exit(r11);
        return;
    L_0x000e:
        r10 = "CameraApp";
        r12 = "parse config START";
        com.lge.camera.util.CamLog.m3d(r10, r12);	 Catch:{ all -> 0x007d }
        r6 = 0;
        r9 = 0;
        r0 = getConfigFilePath();	 Catch:{ Throwable -> 0x0068 }
        r10 = "not found";
        r10 = r10.equals(r0);	 Catch:{ Throwable -> 0x0068 }
        if (r10 != 0) goto L_0x0051;
    L_0x0023:
        r7 = new java.io.InputStreamReader;	 Catch:{ Throwable -> 0x0068 }
        r10 = new java.io.FileInputStream;	 Catch:{ Throwable -> 0x0068 }
        r10.<init>(r0);	 Catch:{ Throwable -> 0x0068 }
        r7.<init>(r10);	 Catch:{ Throwable -> 0x0068 }
        r4 = org.xmlpull.v1.XmlPullParserFactory.newInstance();	 Catch:{ Throwable -> 0x0147, all -> 0x0143 }
        r9 = r4.newPullParser();	 Catch:{ Throwable -> 0x0147, all -> 0x0143 }
        r9.setInput(r7);	 Catch:{ Throwable -> 0x0147, all -> 0x0143 }
        r6 = r7;
    L_0x0039:
        r10 = new java.util.ArrayList;	 Catch:{ Throwable -> 0x0068 }
        r10.<init>();	 Catch:{ Throwable -> 0x0068 }
        sSolutionList = r10;	 Catch:{ Throwable -> 0x0068 }
        r5 = 0;
    L_0x0041:
        r10 = 3;
        if (r5 >= r10) goto L_0x0080;
    L_0x0044:
        r10 = sSolutionList;	 Catch:{ Throwable -> 0x0068 }
        r12 = new java.util.ArrayList;	 Catch:{ Throwable -> 0x0068 }
        r12.<init>();	 Catch:{ Throwable -> 0x0068 }
        r10.add(r5, r12);	 Catch:{ Throwable -> 0x0068 }
        r5 = r5 + 1;
        goto L_0x0041;
    L_0x0051:
        r1 = 2131099658; // 0x7f06000a float:1.7811675E38 double:1.052903129E-314;
        r10 = android.security.KeyStore.getApplicationContext();	 Catch:{ Throwable -> 0x0068 }
        r10 = r10.getResources();	 Catch:{ Throwable -> 0x0068 }
        r9 = r10.getXml(r1);	 Catch:{ Throwable -> 0x0068 }
        r10 = "CameraApp";
        r12 = "ERROR";
        com.lge.camera.util.CamLog.m5e(r10, r12);	 Catch:{ Throwable -> 0x0068 }
        goto L_0x0039;
    L_0x0068:
        r8 = move-exception;
    L_0x0069:
        r10 = "CameraApp";
        r12 = "Config parsing error.";
        android.util.Log.e(r10, r12);	 Catch:{ all -> 0x00ae }
        if (r6 == 0) goto L_0x0075;
    L_0x0072:
        r6.close();	 Catch:{ Exception -> 0x012f }
    L_0x0075:
        r10 = "CameraApp";
        r12 = "parse config END";
        com.lge.camera.util.CamLog.m3d(r10, r12);	 Catch:{ all -> 0x007d }
        goto L_0x000c;
    L_0x007d:
        r10 = move-exception;
        monitor-exit(r11);
        throw r10;
    L_0x0080:
        r10 = new java.util.ArrayList;	 Catch:{ Throwable -> 0x0068 }
        r10.<init>();	 Catch:{ Throwable -> 0x0068 }
        sSolutionParamList = r10;	 Catch:{ Throwable -> 0x0068 }
        r3 = r9.getEventType();	 Catch:{ Throwable -> 0x0068 }
    L_0x008b:
        if (r3 == r14) goto L_0x00cb;
    L_0x008d:
        switch(r3) {
            case 2: goto L_0x0098;
            default: goto L_0x0090;
        };	 Catch:{ Throwable -> 0x0068 }
    L_0x0090:
        r9.next();	 Catch:{ Throwable -> 0x0068 }
        r3 = r9.getEventType();	 Catch:{ Throwable -> 0x0068 }
        goto L_0x008b;
    L_0x0098:
        r10 = "solution";
        r12 = r9.getName();	 Catch:{ Throwable -> 0x0068 }
        r13 = java.util.Locale.US;	 Catch:{ Throwable -> 0x0068 }
        r12 = r12.toLowerCase(r13);	 Catch:{ Throwable -> 0x0068 }
        r10 = r10.equals(r12);	 Catch:{ Throwable -> 0x0068 }
        if (r10 == 0) goto L_0x00b5;
    L_0x00aa:
        startParseSolution(r9);	 Catch:{ Throwable -> 0x0068 }
        goto L_0x0090;
    L_0x00ae:
        r10 = move-exception;
    L_0x00af:
        if (r6 == 0) goto L_0x00b4;
    L_0x00b1:
        r6.close();	 Catch:{ Exception -> 0x0139 }
    L_0x00b4:
        throw r10;	 Catch:{ all -> 0x007d }
    L_0x00b5:
        r10 = "solutionparam";
        r12 = r9.getName();	 Catch:{ Throwable -> 0x0068 }
        r13 = java.util.Locale.US;	 Catch:{ Throwable -> 0x0068 }
        r12 = r12.toLowerCase(r13);	 Catch:{ Throwable -> 0x0068 }
        r10 = r10.equals(r12);	 Catch:{ Throwable -> 0x0068 }
        if (r10 == 0) goto L_0x0090;
    L_0x00c7:
        startParseSolutionParam(r9);	 Catch:{ Throwable -> 0x0068 }
        goto L_0x0090;
    L_0x00cb:
        r12 = "CameraApp";
        r10 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x0068 }
        r10.<init>();	 Catch:{ Throwable -> 0x0068 }
        r13 = "solution snapshot ";
        r13 = r10.append(r13);	 Catch:{ Throwable -> 0x0068 }
        r10 = sSolutionList;	 Catch:{ Throwable -> 0x0068 }
        r14 = 0;
        r10 = r10.get(r14);	 Catch:{ Throwable -> 0x0068 }
        r10 = (java.util.ArrayList) r10;	 Catch:{ Throwable -> 0x0068 }
        r10 = r10.size();	 Catch:{ Throwable -> 0x0068 }
        r10 = r13.append(r10);	 Catch:{ Throwable -> 0x0068 }
        r13 = " preview ";
        r13 = r10.append(r13);	 Catch:{ Throwable -> 0x0068 }
        r10 = sSolutionList;	 Catch:{ Throwable -> 0x0068 }
        r14 = 1;
        r10 = r10.get(r14);	 Catch:{ Throwable -> 0x0068 }
        r10 = (java.util.ArrayList) r10;	 Catch:{ Throwable -> 0x0068 }
        r10 = r10.size();	 Catch:{ Throwable -> 0x0068 }
        r10 = r13.append(r10);	 Catch:{ Throwable -> 0x0068 }
        r13 = " video ";
        r13 = r10.append(r13);	 Catch:{ Throwable -> 0x0068 }
        r10 = sSolutionList;	 Catch:{ Throwable -> 0x0068 }
        r14 = 2;
        r10 = r10.get(r14);	 Catch:{ Throwable -> 0x0068 }
        r10 = (java.util.ArrayList) r10;	 Catch:{ Throwable -> 0x0068 }
        r10 = r10.size();	 Catch:{ Throwable -> 0x0068 }
        r10 = r13.append(r10);	 Catch:{ Throwable -> 0x0068 }
        r10 = r10.toString();	 Catch:{ Throwable -> 0x0068 }
        android.util.Log.e(r12, r10);	 Catch:{ Throwable -> 0x0068 }
        if (r6 == 0) goto L_0x0075;
    L_0x0120:
        r6.close();	 Catch:{ Exception -> 0x0125 }
        goto L_0x0075;
    L_0x0125:
        r2 = move-exception;
        r10 = "CameraApp";
        r12 = "Config parsing : reader close error.";
        android.util.Log.e(r10, r12);	 Catch:{ all -> 0x007d }
        goto L_0x0075;
    L_0x012f:
        r2 = move-exception;
        r10 = "CameraApp";
        r12 = "Config parsing : reader close error.";
        android.util.Log.e(r10, r12);	 Catch:{ all -> 0x007d }
        goto L_0x0075;
    L_0x0139:
        r2 = move-exception;
        r12 = "CameraApp";
        r13 = "Config parsing : reader close error.";
        android.util.Log.e(r12, r13);	 Catch:{ all -> 0x007d }
        goto L_0x00b4;
    L_0x0143:
        r10 = move-exception;
        r6 = r7;
        goto L_0x00af;
    L_0x0147:
        r8 = move-exception;
        r6 = r7;
        goto L_0x0069;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.solution.SolutionParsingUtil.parseConfig():void");
    }

    protected static void startParseSolution(XmlPullParser xpp) {
        LinkedHashMap<String, String> solutionMap = new LinkedHashMap();
        try {
            int eventType = xpp.getEventType();
            String key = null;
            int solutionType = 0;
            while (eventType != 1) {
                switch (eventType) {
                    case 2:
                        key = xpp.getName().toLowerCase(Locale.US);
                        break;
                    case 3:
                        if (!SOLUTION_TAG.equals(xpp.getName().toLowerCase(Locale.US))) {
                            break;
                        }
                        if ((solutionType & 1) != 0) {
                            ((ArrayList) sSolutionList.get(0)).add(solutionMap);
                        }
                        if ((solutionType & 2) != 0) {
                            ((ArrayList) sSolutionList.get(1)).add(solutionMap);
                        }
                        if ((solutionType & 4) != 0) {
                            ((ArrayList) sSolutionList.get(2)).add(solutionMap);
                            return;
                        }
                        return;
                    case 4:
                        if (!xpp.isWhitespace()) {
                            if (SOLUTION_SUPPORT_TAG.equals(key)) {
                                if (Integer.parseInt(xpp.getText()) != 0) {
                                    solutionType = Integer.parseInt(xpp.getText());
                                    break;
                                } else {
                                    CamLog.m5e(CameraConstants.TAG, "Solution  " + key + " is not supported!");
                                    return;
                                }
                            }
                            solutionMap.put(key, xpp.getText().toLowerCase(Locale.US));
                            CamLog.m3d(CameraConstants.TAG, "key : " + key + ", value : " + xpp.getText());
                            break;
                        }
                        continue;
                    default:
                        break;
                }
                xpp.next();
                eventType = xpp.getEventType();
            }
        } catch (Throwable t) {
            Log.d(CameraConstants.TAG, "getXmlItem " + t.toString());
        }
    }

    protected static void startParseSolutionParam(XmlPullParser xpp) {
        LinkedHashMap<String, String> solutionMap = new LinkedHashMap();
        try {
            int eventType = xpp.getEventType();
            String key = null;
            while (eventType != 1) {
                switch (eventType) {
                    case 2:
                        key = xpp.getName().toLowerCase(Locale.US);
                        break;
                    case 3:
                        if (!SOLUTION_PARM_TAG.equals(xpp.getName().toLowerCase(Locale.US))) {
                            break;
                        }
                        sSolutionParamList.add(solutionMap);
                        return;
                    case 4:
                        if (!xpp.isWhitespace()) {
                            solutionMap.put(key, xpp.getText().toLowerCase(Locale.US));
                            CamLog.m3d(CameraConstants.TAG, "key : " + key + ", value : " + xpp.getText());
                            break;
                        }
                        break;
                    default:
                        break;
                }
                xpp.next();
                eventType = xpp.getEventType();
            }
        } catch (Throwable t) {
            Log.d(CameraConstants.TAG, "getXmlItem " + t.toString());
        }
    }

    protected static boolean checkStringArray(String[] array) {
        return array != null && array.length > 0;
    }
}
