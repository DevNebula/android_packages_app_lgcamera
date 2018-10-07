package com.lge.camera.constants;

import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import com.android.internal.telephony.SmsApplication;
import com.lge.camera.util.CamLog;
import java.io.File;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;

public class QuickClipConfig {
    private static final String DEFALT_MESSAGE = "DEF.MSG";
    private static final String ITEM_DEFAULT = "default";
    private static final String ITEM_PHOTO = "photo";
    private static final String ITEM_TAG = "item";
    private static final String ITEM_VIDEO = "video";
    private static final String NAME_ATTR = "name";
    private static final String PACKAGE_ATTR = "package";
    public static final String SNS_LIST_CONFIG_DLOAD_PATH = "/mpt/com.lge.camera_quickclip_config.xml";
    public static final String SNS_LIST_CONFIG_PATH = "/system/etc/com.lge.camera_quickclip_config.xml";
    public static boolean sLoadSNSListDone = false;
    public static ArrayList<String> sSNSPhotoList = new ArrayList();
    public static ArrayList<String> sSNSVideoList = new ArrayList();

    public static void loadDefinedSNSList(Context context) {
        if (!sLoadSNSListDone) {
            parseSNSList(context);
            if (sSNSPhotoList != null && sSNSPhotoList.size() > 0 && sSNSVideoList != null && sSNSVideoList.size() > 0) {
                sLoadSNSListDone = true;
            }
        }
    }

    public static ArrayList<String> getSNSPackageList(String type) {
        CamLog.m3d(CameraConstants.TAG, "loading type: " + type);
        if (type == null) {
            CamLog.m11w(CameraConstants.TAG, "getSNSPackageList return null");
            return null;
        } else if (type.equals("image/*")) {
            return sSNSPhotoList;
        } else {
            return sSNSVideoList;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x00a8 A:{SYNTHETIC, Splitter: B:38:0x00a8} */
    /* JADX WARNING: Removed duplicated region for block: B:51:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0086 A:{SYNTHETIC, Splitter: B:26:0x0086} */
    private static void parseSNSList(android.content.Context r13) {
        /*
        r6 = 0;
        r9 = 0;
        r4 = 0;
        r3 = getQuickClipConfigPath();
        r10 = sSNSPhotoList;
        r10.clear();
        r10 = sSNSVideoList;
        r10.clear();
        r10 = "not found";
        r10 = r3.equals(r10);
        if (r10 == 0) goto L_0x001d;
    L_0x0019:
        loadDefaultSNSLists();
    L_0x001c:
        return;
    L_0x001d:
        r7 = new java.io.InputStreamReader;	 Catch:{ Throwable -> 0x00b8 }
        r10 = new java.io.FileInputStream;	 Catch:{ Throwable -> 0x00b8 }
        r10.<init>(r3);	 Catch:{ Throwable -> 0x00b8 }
        r7.<init>(r10);	 Catch:{ Throwable -> 0x00b8 }
        r2 = org.xmlpull.v1.XmlPullParserFactory.newInstance();	 Catch:{ Throwable -> 0x0078, all -> 0x00b5 }
        r9 = r2.newPullParser();	 Catch:{ Throwable -> 0x0078, all -> 0x00b5 }
        r9.setInput(r7);	 Catch:{ Throwable -> 0x0078, all -> 0x00b5 }
        r1 = r9.getEventType();	 Catch:{ Throwable -> 0x0078, all -> 0x00b5 }
    L_0x0036:
        r10 = 1;
        if (r1 == r10) goto L_0x0093;
    L_0x0039:
        switch(r1) {
            case 2: goto L_0x0044;
            default: goto L_0x003c;
        };	 Catch:{ Throwable -> 0x0078, all -> 0x00b5 }
    L_0x003c:
        r9.next();	 Catch:{ Throwable -> 0x0078, all -> 0x00b5 }
        r1 = r9.getEventType();	 Catch:{ Throwable -> 0x0078, all -> 0x00b5 }
        goto L_0x0036;
    L_0x0044:
        r10 = "item";
        r11 = r9.getName();	 Catch:{ Throwable -> 0x0078, all -> 0x00b5 }
        r10 = r10.equals(r11);	 Catch:{ Throwable -> 0x0078, all -> 0x00b5 }
        if (r10 == 0) goto L_0x0057;
    L_0x0050:
        r10 = 0;
        r11 = "name";
        r4 = r9.getAttributeValue(r10, r11);	 Catch:{ Throwable -> 0x0078, all -> 0x00b5 }
    L_0x0057:
        r10 = "package";
        r11 = r9.getName();	 Catch:{ Throwable -> 0x0078, all -> 0x00b5 }
        r10 = r10.equals(r11);	 Catch:{ Throwable -> 0x0078, all -> 0x00b5 }
        if (r10 == 0) goto L_0x0074;
    L_0x0063:
        r10 = 0;
        r11 = "name";
        r5 = r9.getAttributeValue(r10, r11);	 Catch:{ Throwable -> 0x0078, all -> 0x00b5 }
        if (r5 == 0) goto L_0x0074;
    L_0x006c:
        if (r4 == 0) goto L_0x0074;
    L_0x006e:
        setSNSList(r4, r5);	 Catch:{ Throwable -> 0x0078, all -> 0x00b5 }
        setItem(r9, r5);	 Catch:{ Throwable -> 0x0078, all -> 0x00b5 }
    L_0x0074:
        r9.next();	 Catch:{ Throwable -> 0x0078, all -> 0x00b5 }
        goto L_0x003c;
    L_0x0078:
        r8 = move-exception;
        r6 = r7;
    L_0x007a:
        r10 = "CameraApp";
        r11 = "Config parsing error.";
        android.util.Log.e(r10, r11);	 Catch:{ all -> 0x00a5 }
        loadDefaultSNSLists();	 Catch:{ all -> 0x00a5 }
        if (r6 == 0) goto L_0x001c;
    L_0x0086:
        r6.close();	 Catch:{ Exception -> 0x008a }
        goto L_0x001c;
    L_0x008a:
        r0 = move-exception;
        r10 = "CameraApp";
        r11 = "Config parsing : reader close error.";
        android.util.Log.e(r10, r11);
        goto L_0x001c;
    L_0x0093:
        if (r7 == 0) goto L_0x00ba;
    L_0x0095:
        r7.close();	 Catch:{ Exception -> 0x009a }
        r6 = r7;
        goto L_0x001c;
    L_0x009a:
        r0 = move-exception;
        r10 = "CameraApp";
        r11 = "Config parsing : reader close error.";
        android.util.Log.e(r10, r11);
        r6 = r7;
        goto L_0x001c;
    L_0x00a5:
        r10 = move-exception;
    L_0x00a6:
        if (r6 == 0) goto L_0x00ab;
    L_0x00a8:
        r6.close();	 Catch:{ Exception -> 0x00ac }
    L_0x00ab:
        throw r10;
    L_0x00ac:
        r0 = move-exception;
        r11 = "CameraApp";
        r12 = "Config parsing : reader close error.";
        android.util.Log.e(r11, r12);
        goto L_0x00ab;
    L_0x00b5:
        r10 = move-exception;
        r6 = r7;
        goto L_0x00a6;
    L_0x00b8:
        r8 = move-exception;
        goto L_0x007a;
    L_0x00ba:
        r6 = r7;
        goto L_0x001c;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.constants.QuickClipConfig.parseSNSList(android.content.Context):void");
    }

    private static String getQuickClipConfigPath() {
        if (new File(SNS_LIST_CONFIG_DLOAD_PATH).exists()) {
            return SNS_LIST_CONFIG_DLOAD_PATH;
        }
        if (new File(SNS_LIST_CONFIG_PATH).exists()) {
            return SNS_LIST_CONFIG_PATH;
        }
        return "not found";
    }

    private static void setItem(XmlPullParser xpp, String itemName) {
        CamLog.m3d(CameraConstants.TAG, "limte : " + xpp.getText());
    }

    private static void setSNSList(String itemName, String packageName) {
        CamLog.m3d(CameraConstants.TAG, "loadXMLSNSLists");
        if (itemName.equals("default")) {
            sSNSPhotoList.add(packageName);
            sSNSVideoList.add(packageName);
        } else if (itemName.equals(ITEM_PHOTO)) {
            sSNSPhotoList.add(packageName);
        } else if (itemName.equals(ITEM_VIDEO)) {
            sSNSVideoList.add(packageName);
        }
    }

    private static void loadDefaultSNSLists() {
        int i = 0;
        CamLog.m3d(CameraConstants.TAG, "loadDefaultSNSLists");
        String[] defaultNativeAppForPhoto = new String[]{DEFALT_MESSAGE, "com.google.android.gm", "com.lge.qmemoplus", "com.android.bluetooth", "com.android.calendar", "com.google.android.apps.docs", "com.google.android.apps.plus"};
        String[] defaultNativeAppForVideo = new String[]{CameraConstants.PACKAGE_YOUTUBE, DEFALT_MESSAGE, "com.google.android.gm", "com.lge.qmemoplus", "com.android.bluetooth", "com.google.android.apps.docs", "com.google.android.apps.plus"};
        for (String item : new String[]{"com.facebook.katana", "com.twitter.android", "com.tumblr", "com.instagram.android", "com.whatsapp", "com.skype.raider", "co.vine.android", "com.kakao.talk", "jp.naver.line.android"}) {
            sSNSPhotoList.add(item);
            sSNSVideoList.add(item);
        }
        for (String item2 : defaultNativeAppForPhoto) {
            sSNSPhotoList.add(item2);
        }
        int length = defaultNativeAppForVideo.length;
        while (i < length) {
            sSNSVideoList.add(defaultNativeAppForVideo[i]);
            i++;
        }
    }

    public static String getDefaultMessageApp(Context context) {
        ComponentName appName = SmsApplication.getDefaultSmsApplication(context, false);
        String packageName = CameraConstants.NULL;
        if (appName != null) {
            packageName = appName.getPackageName();
        }
        Log.d(CameraConstants.TAG, "getDefaultSMSApp = " + packageName);
        return packageName;
    }

    public static boolean isMessageApp(String packageName) {
        if (DEFALT_MESSAGE.equals(packageName)) {
            return true;
        }
        return false;
    }
}
