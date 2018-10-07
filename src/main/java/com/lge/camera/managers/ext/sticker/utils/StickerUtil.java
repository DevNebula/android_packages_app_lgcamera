package com.lge.camera.managers.ext.sticker.utils;

import android.content.Context;
import com.lge.camera.C0088R;
import com.lge.camera.managers.ext.sticker.solutions.StickerDrawingInformation;
import com.lge.camera.util.CamLog;
import java.io.File;

public class StickerUtil {
    private static final int MAX_SIZE = 10485760;
    public static final String PRELOAD_STICKER_PACKAGE_NAME = "com.lge.camera.sticker.res";
    public static final String PRELOAD_STICKER_RESOURCE_NAME = "sticker";
    private static final String TAG = "StickerUtil";
    public static final String TRACKING_DATA_DIRECTORY_NAME = "sticker_tracking_data";
    public static final String TRACKING_DATA_FILE = "track_data.dat";
    public static final String TRACKING_DATA_RESOURCE_NAME = "track_data";
    public static final String TRACKING_FACE_FILE = "i16facemodel.data";
    public static final String TRACKING_FACE_RESOURCE_NAME = "i16facemodel";

    public static void dump(final StickerDrawingInformation di, final byte[] buffers) {
        new Thread(new Runnable() {
            /* JADX WARNING: Removed duplicated region for block: B:51:? A:{SYNTHETIC, RETURN} */
            /* JADX WARNING: Removed duplicated region for block: B:28:0x0140 A:{SYNTHETIC, Splitter: B:28:0x0140} */
            /* JADX WARNING: Removed duplicated region for block: B:34:0x0150 A:{SYNTHETIC, Splitter: B:34:0x0150} */
            public void run() {
                /*
                r17 = this;
                r2 = "StickerUtil";
                r3 = new java.lang.StringBuilder;
                r3.<init>();
                r16 = "di = ";
                r0 = r16;
                r3 = r3.append(r0);
                r0 = r17;
                r0 = r2;
                r16 = r0;
                r0 = r16;
                r3 = r3.append(r0);
                r3 = r3.toString();
                com.lge.camera.util.CamLog.m5e(r2, r3);
                r2 = "StickerUtil";
                r3 = new java.lang.StringBuilder;
                r3.<init>();
                r16 = "buffers = ";
                r0 = r16;
                r3 = r3.append(r0);
                r0 = r17;
                r0 = r3;
                r16 = r0;
                r0 = r16;
                r0 = r0.length;
                r16 = r0;
                r0 = r16;
                r3 = r3.append(r0);
                r3 = r3.toString();
                com.lge.camera.util.CamLog.m5e(r2, r3);
                r0 = r17;
                r2 = r2;
                if (r2 == 0) goto L_0x0129;
            L_0x004f:
                r0 = r17;
                r2 = r2;
                r2 = r2.getPreviewWidth();
                if (r2 <= 0) goto L_0x0129;
            L_0x0059:
                r0 = r17;
                r2 = r2;
                r2 = r2.getPreviewHeight();
                if (r2 <= 0) goto L_0x0129;
            L_0x0063:
                r0 = r17;
                r2 = r3;
                if (r2 == 0) goto L_0x0129;
            L_0x0069:
                r0 = r17;
                r2 = r3;
                r2 = r2.length;
                if (r2 <= 0) goto L_0x0129;
            L_0x0070:
                r0 = r17;
                r2 = r2;
                r4 = r2.getPreviewWidth();
                r0 = r17;
                r2 = r2;
                r5 = r2.getPreviewHeight();
                r2 = 3;
                r6 = new int[r2];
                r2 = 0;
                r6[r2] = r4;
                r2 = 1;
                r3 = r4 / 2;
                r6[r2] = r3;
                r2 = 2;
                r3 = r4 / 2;
                r6[r2] = r3;
                r2 = new java.lang.StringBuilder;
                r2.<init>();
                r3 = android.os.Environment.getExternalStorageDirectory();
                r3 = r3.getAbsolutePath();
                r2 = r2.append(r3);
                r3 = "/sticker_dump";
                r2 = r2.append(r3);
                r8 = r2.toString();
                r11 = new java.io.File;
                r11.<init>(r8);
                r2 = r11.exists();
                if (r2 != 0) goto L_0x00b9;
            L_0x00b6:
                r11.mkdir();
            L_0x00b9:
                r2 = "StickerUtil";
                r3 = "dump start";
                com.lge.camera.util.CamLog.m5e(r2, r3);
                r1 = new android.graphics.YuvImage;
                r0 = r17;
                r2 = r3;
                r3 = 17;
                r1.<init>(r2, r3, r4, r5, r6);
                r14 = new android.graphics.Rect;
                r2 = 0;
                r3 = 0;
                r14.<init>(r2, r3, r4, r5);
                r12 = 0;
                r15 = new android.icu.text.SimpleDateFormat;	 Catch:{ IOException -> 0x0133 }
                r2 = "yyyy_mm_dd_kk_mm_ss";
                r15.<init>(r2);	 Catch:{ IOException -> 0x0133 }
                r7 = new java.util.Date;	 Catch:{ IOException -> 0x0133 }
                r7.<init>();	 Catch:{ IOException -> 0x0133 }
                r2 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0133 }
                r2.<init>();	 Catch:{ IOException -> 0x0133 }
                r2 = r2.append(r8);	 Catch:{ IOException -> 0x0133 }
                r3 = "/dump_";
                r2 = r2.append(r3);	 Catch:{ IOException -> 0x0133 }
                r3 = r15.format(r7);	 Catch:{ IOException -> 0x0133 }
                r2 = r2.append(r3);	 Catch:{ IOException -> 0x0133 }
                r3 = ".jpg";
                r2 = r2.append(r3);	 Catch:{ IOException -> 0x0133 }
                r10 = r2.toString();	 Catch:{ IOException -> 0x0133 }
                r13 = new java.io.FileOutputStream;	 Catch:{ IOException -> 0x0133 }
                r13.<init>(r10);	 Catch:{ IOException -> 0x0133 }
                r2 = 100;
                r1.compressToJpeg(r14, r2, r13);	 Catch:{ IOException -> 0x0162, all -> 0x015f }
                r2 = "StickerUtil";
                r3 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0162, all -> 0x015f }
                r3.<init>();	 Catch:{ IOException -> 0x0162, all -> 0x015f }
                r16 = "dump Success on : ";
                r0 = r16;
                r3 = r3.append(r0);	 Catch:{ IOException -> 0x0162, all -> 0x015f }
                r3 = r3.append(r10);	 Catch:{ IOException -> 0x0162, all -> 0x015f }
                r3 = r3.toString();	 Catch:{ IOException -> 0x0162, all -> 0x015f }
                com.lge.camera.util.CamLog.m5e(r2, r3);	 Catch:{ IOException -> 0x0162, all -> 0x015f }
                if (r13 == 0) goto L_0x0129;
            L_0x0126:
                r13.close();	 Catch:{ IOException -> 0x012a }
            L_0x0129:
                return;
            L_0x012a:
                r9 = move-exception;
                r2 = "StickerUtil";
                r3 = "fos close IOException";
                com.lge.camera.util.CamLog.m5e(r2, r3);
                goto L_0x0129;
            L_0x0133:
                r9 = move-exception;
            L_0x0134:
                r2 = "StickerUtil";
                r3 = "FileSave IOException";
                com.lge.camera.util.CamLog.m5e(r2, r3);	 Catch:{ all -> 0x014d }
                r9.printStackTrace();	 Catch:{ all -> 0x014d }
                if (r12 == 0) goto L_0x0129;
            L_0x0140:
                r12.close();	 Catch:{ IOException -> 0x0144 }
                goto L_0x0129;
            L_0x0144:
                r9 = move-exception;
                r2 = "StickerUtil";
                r3 = "fos close IOException";
                com.lge.camera.util.CamLog.m5e(r2, r3);
                goto L_0x0129;
            L_0x014d:
                r2 = move-exception;
            L_0x014e:
                if (r12 == 0) goto L_0x0153;
            L_0x0150:
                r12.close();	 Catch:{ IOException -> 0x0154 }
            L_0x0153:
                throw r2;
            L_0x0154:
                r9 = move-exception;
                r3 = "StickerUtil";
                r16 = "fos close IOException";
                r0 = r16;
                com.lge.camera.util.CamLog.m5e(r3, r0);
                goto L_0x0153;
            L_0x015f:
                r2 = move-exception;
                r12 = r13;
                goto L_0x014e;
            L_0x0162:
                r9 = move-exception;
                r12 = r13;
                goto L_0x0134;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.ext.sticker.utils.StickerUtil.1.run():void");
            }
        }).start();
    }

    public static void copy(String srcPath, String destPath) {
        copy(new File(srcPath), new File(destPath));
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x004a A:{SYNTHETIC, Splitter: B:22:0x004a} */
    /* JADX WARNING: Removed duplicated region for block: B:54:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004f A:{SYNTHETIC, Splitter: B:25:0x004f} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0060 A:{SYNTHETIC, Splitter: B:33:0x0060} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0065 A:{SYNTHETIC, Splitter: B:36:0x0065} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x004a A:{SYNTHETIC, Splitter: B:22:0x004a} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004f A:{SYNTHETIC, Splitter: B:25:0x004f} */
    /* JADX WARNING: Removed duplicated region for block: B:54:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0060 A:{SYNTHETIC, Splitter: B:33:0x0060} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0065 A:{SYNTHETIC, Splitter: B:36:0x0065} */
    public static void copy(java.io.File r12, java.io.File r13) {
        /*
        r2 = "StickerUtil";
        r3 = "file copy start";
        com.lge.camera.util.CamLog.m5e(r2, r3);
        r8 = 0;
        r10 = 0;
        r9 = new java.io.FileInputStream;	 Catch:{ IOException -> 0x0044 }
        r9.<init>(r12);	 Catch:{ IOException -> 0x0044 }
        r11 = new java.io.FileOutputStream;	 Catch:{ IOException -> 0x007a, all -> 0x0073 }
        r11.<init>(r13);	 Catch:{ IOException -> 0x007a, all -> 0x0073 }
        r1 = r9.getChannel();	 Catch:{ IOException -> 0x007d, all -> 0x0076 }
        r6 = r11.getChannel();	 Catch:{ IOException -> 0x007d, all -> 0x0076 }
        r2 = 0;
        r4 = r1.size();	 Catch:{ IOException -> 0x007d, all -> 0x0076 }
        r1.transferTo(r2, r4, r6);	 Catch:{ IOException -> 0x007d, all -> 0x0076 }
        r2 = "StickerUtil";
        r3 = "file copy end";
        com.lge.camera.util.CamLog.m5e(r2, r3);	 Catch:{ IOException -> 0x007d, all -> 0x0076 }
        if (r9 == 0) goto L_0x0030;
    L_0x002d:
        r9.close();	 Catch:{ IOException -> 0x0038 }
    L_0x0030:
        if (r11 == 0) goto L_0x0081;
    L_0x0032:
        r11.close();	 Catch:{ IOException -> 0x003d }
        r10 = r11;
        r8 = r9;
    L_0x0037:
        return;
    L_0x0038:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0030;
    L_0x003d:
        r0 = move-exception;
        r0.printStackTrace();
        r10 = r11;
        r8 = r9;
        goto L_0x0037;
    L_0x0044:
        r7 = move-exception;
    L_0x0045:
        r7.printStackTrace();	 Catch:{ all -> 0x005d }
        if (r8 == 0) goto L_0x004d;
    L_0x004a:
        r8.close();	 Catch:{ IOException -> 0x0058 }
    L_0x004d:
        if (r10 == 0) goto L_0x0037;
    L_0x004f:
        r10.close();	 Catch:{ IOException -> 0x0053 }
        goto L_0x0037;
    L_0x0053:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0037;
    L_0x0058:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x004d;
    L_0x005d:
        r2 = move-exception;
    L_0x005e:
        if (r8 == 0) goto L_0x0063;
    L_0x0060:
        r8.close();	 Catch:{ IOException -> 0x0069 }
    L_0x0063:
        if (r10 == 0) goto L_0x0068;
    L_0x0065:
        r10.close();	 Catch:{ IOException -> 0x006e }
    L_0x0068:
        throw r2;
    L_0x0069:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0063;
    L_0x006e:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0068;
    L_0x0073:
        r2 = move-exception;
        r8 = r9;
        goto L_0x005e;
    L_0x0076:
        r2 = move-exception;
        r10 = r11;
        r8 = r9;
        goto L_0x005e;
    L_0x007a:
        r7 = move-exception;
        r8 = r9;
        goto L_0x0045;
    L_0x007d:
        r7 = move-exception;
        r10 = r11;
        r8 = r9;
        goto L_0x0045;
    L_0x0081:
        r10 = r11;
        r8 = r9;
        goto L_0x0037;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.ext.sticker.utils.StickerUtil.copy(java.io.File, java.io.File):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x005a A:{SYNTHETIC, Splitter: B:38:0x005a} */
    /* JADX WARNING: Removed duplicated region for block: B:83:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x005f A:{SYNTHETIC, Splitter: B:41:0x005f} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0073 A:{SYNTHETIC, Splitter: B:51:0x0073} */
    /* JADX WARNING: Removed duplicated region for block: B:85:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0078 A:{SYNTHETIC, Splitter: B:54:0x0078} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0089 A:{SYNTHETIC, Splitter: B:62:0x0089} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x008e A:{SYNTHETIC, Splitter: B:65:0x008e} */
    public static void copyOrThrow(java.io.InputStream r9, java.io.File r10) throws java.io.IOException {
        /*
        r2 = 0;
        r6 = r9.available();	 Catch:{ IOException -> 0x0054, OutOfMemoryError -> 0x006d }
        if (r6 == 0) goto L_0x000b;
    L_0x0007:
        r7 = 10485760; // 0xa00000 float:1.469368E-38 double:5.180654E-317;
        if (r6 <= r7) goto L_0x0027;
    L_0x000b:
        r7 = "StickerUtil";
        r8 = "check InputStream size";
        com.lge.camera.util.CamLog.m5e(r7, r8);	 Catch:{ IOException -> 0x0054, OutOfMemoryError -> 0x006d }
        if (r9 == 0) goto L_0x0017;
    L_0x0014:
        r9.close();	 Catch:{ IOException -> 0x001d }
    L_0x0017:
        if (r2 == 0) goto L_0x001c;
    L_0x0019:
        r2.close();	 Catch:{ IOException -> 0x0022 }
    L_0x001c:
        return;
    L_0x001d:
        r4 = move-exception;
        r4.printStackTrace();
        goto L_0x0017;
    L_0x0022:
        r4 = move-exception;
        r4.printStackTrace();
        goto L_0x001c;
    L_0x0027:
        r7 = r9.available();	 Catch:{ IOException -> 0x0054, OutOfMemoryError -> 0x006d }
        r0 = new byte[r7];	 Catch:{ IOException -> 0x0054, OutOfMemoryError -> 0x006d }
        r7 = r9.read(r0);	 Catch:{ IOException -> 0x0054, OutOfMemoryError -> 0x006d }
        if (r7 <= 0) goto L_0x003f;
    L_0x0033:
        r3 = new java.io.FileOutputStream;	 Catch:{ IOException -> 0x0054, OutOfMemoryError -> 0x006d }
        r3.<init>(r10);	 Catch:{ IOException -> 0x0054, OutOfMemoryError -> 0x006d }
        r3.write(r0);	 Catch:{ IOException -> 0x00a2, OutOfMemoryError -> 0x009f, all -> 0x009c }
        r3.flush();	 Catch:{ IOException -> 0x00a2, OutOfMemoryError -> 0x009f, all -> 0x009c }
        r2 = r3;
    L_0x003f:
        if (r9 == 0) goto L_0x0044;
    L_0x0041:
        r9.close();	 Catch:{ IOException -> 0x004f }
    L_0x0044:
        if (r2 == 0) goto L_0x001c;
    L_0x0046:
        r2.close();	 Catch:{ IOException -> 0x004a }
        goto L_0x001c;
    L_0x004a:
        r4 = move-exception;
        r4.printStackTrace();
        goto L_0x001c;
    L_0x004f:
        r4 = move-exception;
        r4.printStackTrace();
        goto L_0x0044;
    L_0x0054:
        r1 = move-exception;
    L_0x0055:
        r1.printStackTrace();	 Catch:{ all -> 0x0086 }
        if (r9 == 0) goto L_0x005d;
    L_0x005a:
        r9.close();	 Catch:{ IOException -> 0x0068 }
    L_0x005d:
        if (r2 == 0) goto L_0x001c;
    L_0x005f:
        r2.close();	 Catch:{ IOException -> 0x0063 }
        goto L_0x001c;
    L_0x0063:
        r4 = move-exception;
        r4.printStackTrace();
        goto L_0x001c;
    L_0x0068:
        r4 = move-exception;
        r4.printStackTrace();
        goto L_0x005d;
    L_0x006d:
        r5 = move-exception;
    L_0x006e:
        r5.printStackTrace();	 Catch:{ all -> 0x0086 }
        if (r9 == 0) goto L_0x0076;
    L_0x0073:
        r9.close();	 Catch:{ IOException -> 0x0081 }
    L_0x0076:
        if (r2 == 0) goto L_0x001c;
    L_0x0078:
        r2.close();	 Catch:{ IOException -> 0x007c }
        goto L_0x001c;
    L_0x007c:
        r4 = move-exception;
        r4.printStackTrace();
        goto L_0x001c;
    L_0x0081:
        r4 = move-exception;
        r4.printStackTrace();
        goto L_0x0076;
    L_0x0086:
        r7 = move-exception;
    L_0x0087:
        if (r9 == 0) goto L_0x008c;
    L_0x0089:
        r9.close();	 Catch:{ IOException -> 0x0092 }
    L_0x008c:
        if (r2 == 0) goto L_0x0091;
    L_0x008e:
        r2.close();	 Catch:{ IOException -> 0x0097 }
    L_0x0091:
        throw r7;
    L_0x0092:
        r4 = move-exception;
        r4.printStackTrace();
        goto L_0x008c;
    L_0x0097:
        r4 = move-exception;
        r4.printStackTrace();
        goto L_0x0091;
    L_0x009c:
        r7 = move-exception;
        r2 = r3;
        goto L_0x0087;
    L_0x009f:
        r5 = move-exception;
        r2 = r3;
        goto L_0x006e;
    L_0x00a2:
        r1 = move-exception;
        r2 = r3;
        goto L_0x0055;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.ext.sticker.utils.StickerUtil.copyOrThrow(java.io.InputStream, java.io.File):void");
    }

    public static void copyTrackingData(Context ctx) {
        File dataFolder = new File(ctx.getFilesDir().getAbsolutePath() + "/" + TRACKING_DATA_DIRECTORY_NAME);
        if (dataFolder != null) {
            if (!dataFolder.exists()) {
                dataFolder.mkdir();
            }
            if (dataFolder.exists() && dataFolder.isDirectory()) {
                File track = new File(dataFolder, TRACKING_DATA_FILE);
                File face = new File(dataFolder, TRACKING_FACE_FILE);
                if (track.exists() && face.exists()) {
                    CamLog.m5e(TAG, "Do Not Need Data File Copy!!!!");
                    return;
                }
                try {
                    copyOrThrow(ctx.getResources().openRawResource(C0088R.raw.track_data), track);
                    copyOrThrow(ctx.getResources().openRawResource(C0088R.raw.i16facemodel), face);
                    CamLog.m5e(TAG, "Copy success!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    CamLog.m5e(TAG, "tracking and facing File Copy fail");
                }
            }
        }
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory != null) {
            if (fileOrDirectory.isDirectory()) {
                File[] all = fileOrDirectory.listFiles();
                if (all != null) {
                    for (File child : all) {
                        deleteRecursive(child);
                    }
                }
            }
            fileOrDirectory.delete();
        }
    }
}
