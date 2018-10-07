package com.lge.camera.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.lge.camera.constants.CameraConstants;
import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class FileUtil {
    public static void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable t) {
                Log.w(CameraConstants.TAG, "close fail", t);
            }
        }
    }

    public static void closeSilently(ParcelFileDescriptor fd) {
        if (fd != null) {
            try {
                fd.close();
            } catch (Throwable t) {
                Log.w(CameraConstants.TAG, "fail to close", t);
            }
        }
    }

    public static void closeSilently(Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Throwable t) {
                Log.w(CameraConstants.TAG, "fail to close", t);
            }
        }
    }

    public static String escapeXml(String s) {
        StringBuilder sb = new StringBuilder();
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\"':
                    sb.append("&quot;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '\'':
                    sb.append("&#039;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String path = null;
        if (!(context == null || contentUri == null)) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(contentUri, new String[]{"_data"}, null, null, null);
                if (cursor != null) {
                    int column_index = cursor.getColumnIndexOrThrow("_data");
                    if (cursor.moveToFirst()) {
                        path = cursor.getString(column_index);
                    }
                } else if ("file".equals(contentUri.getScheme())) {
                    path = contentUri.getPath();
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "failed to get path from uri: ", e);
                e.printStackTrace();
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        CamLog.m11w(CameraConstants.TAG, String.format("return path: %s", new Object[]{path}));
        return path;
    }

    public static String getFileNameFromURI(Context context, Uri contentUri) {
        String path = null;
        if (!(context == null || contentUri == null)) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(contentUri, new String[]{"_data"}, null, null, null);
                if (cursor != null) {
                    int column_index = cursor.getColumnIndexOrThrow("_data");
                    if (cursor.moveToFirst()) {
                        path = cursor.getString(column_index);
                    }
                } else if ("file".equals(contentUri.getScheme())) {
                    path = contentUri.getPath();
                }
                if (path != null) {
                    String substring;
                    int slashIdx = path.lastIndexOf(47);
                    int dotIdx = path.lastIndexOf(46);
                    if (dotIdx == -1) {
                        substring = path.substring(slashIdx + 1);
                    } else {
                        substring = path.substring(slashIdx + 1, dotIdx);
                    }
                    if (cursor == null) {
                        return substring;
                    }
                    cursor.close();
                    return substring;
                } else if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "failed to get path from uri: ", e);
                e.printStackTrace();
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        CamLog.m11w(CameraConstants.TAG, String.format("return path: %s", new Object[]{path}));
        return path;
    }

    public static int getModeColumnFromURI(Context context, Uri contentUri) {
        int modeColumn = -1;
        if (!(context == null || contentUri == null)) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(contentUri, new String[]{CameraConstants.MODE_COLUMN}, null, null, null);
                if (cursor != null) {
                    int column_index = cursor.getColumnIndexOrThrow(CameraConstants.MODE_COLUMN);
                    if (cursor.moveToFirst()) {
                        modeColumn = cursor.getInt(column_index);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "failed to get mode column from uri: ", e);
                e.printStackTrace();
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        CamLog.m11w(CameraConstants.TAG, String.format("return modeColumn: %s", new Object[]{Integer.valueOf(modeColumn)}) + ", mode column number : " + modeColumn);
        return modeColumn;
    }

    public static long getIdFromUri(Activity activity, Uri contentUri) {
        long id = -1;
        String[] proj = new String[]{"_id"};
        if (!(activity == null || contentUri == null)) {
            Cursor cursor = null;
            try {
                cursor = activity.getContentResolver().query(contentUri, proj, null, null, null);
                if (cursor != null) {
                    if (cursor.getCount() != 0) {
                        int column_index = cursor.getColumnIndexOrThrow("_id");
                        cursor.moveToFirst();
                        id = cursor.getLong(column_index);
                        if (cursor != null) {
                            cursor.close();
                        }
                    } else if (cursor != null) {
                        cursor.close();
                    }
                } else if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "Could not ID from URI", e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return id;
    }

    public static String getDirFromFullName(String fullName) {
        if (fullName == null) {
            return null;
        }
        String dirName = "";
        String[] sptString = fullName.split("/");
        if (sptString == null) {
            return null;
        }
        for (int i = 0; i < sptString.length - 1; i++) {
            dirName = dirName + sptString[i] + "/";
        }
        return dirName;
    }

    public static String getBurstIDFromFullName(String fullName) {
        if (fullName == null) {
            return null;
        }
        String[] sptString = fullName.split("/");
        if (sptString == null) {
            return null;
        }
        String name = sptString[sptString.length - 1].split("\\.")[0];
        if (name.length() > 2) {
            return name.substring(name.length() - 2);
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x008d  */
    public static android.net.Uri getUriFromPath(android.content.Context r18, java.lang.String r19, boolean r20) {
        /*
        r3 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        r13 = "_data";
        r14 = "_id";
        r15 = "mime_type";
        if (r20 != 0) goto L_0x0012;
    L_0x000a:
        r3 = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        r13 = "_data";
        r14 = "_id";
        r15 = "mime_type";
    L_0x0012:
        r11 = new java.io.File;
        r0 = r19;
        r11.<init>(r0);
        r9 = 0;
        r2 = r18.getContentResolver();	 Catch:{ SQLiteException -> 0x00c4 }
        r4 = 2;
        r4 = new java.lang.String[r4];	 Catch:{ SQLiteException -> 0x00c4 }
        r5 = 0;
        r4[r5] = r14;	 Catch:{ SQLiteException -> 0x00c4 }
        r5 = 1;
        r4[r5] = r15;	 Catch:{ SQLiteException -> 0x00c4 }
        r5 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x00c4 }
        r5.<init>();	 Catch:{ SQLiteException -> 0x00c4 }
        r5 = r5.append(r13);	 Catch:{ SQLiteException -> 0x00c4 }
        r6 = "=? ";
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x00c4 }
        r5 = r5.toString();	 Catch:{ SQLiteException -> 0x00c4 }
        r6 = 1;
        r6 = new java.lang.String[r6];	 Catch:{ SQLiteException -> 0x00c4 }
        r7 = 0;
        r6[r7] = r19;	 Catch:{ SQLiteException -> 0x00c4 }
        r7 = 0;
        r9 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ SQLiteException -> 0x00c4 }
        if (r9 == 0) goto L_0x0099;
    L_0x0047:
        r2 = r9.moveToFirst();	 Catch:{ SQLiteException -> 0x00c4 }
        if (r2 == 0) goto L_0x0099;
    L_0x004d:
        r2 = "_id";
        r2 = r9.getColumnIndex(r2);	 Catch:{ SQLiteException -> 0x00c4 }
        r12 = r9.getInt(r2);	 Catch:{ SQLiteException -> 0x00c4 }
        r2 = "mime_type";
        r2 = r9.getColumnIndex(r2);	 Catch:{ SQLiteException -> 0x00c4 }
        r16 = r9.getString(r2);	 Catch:{ SQLiteException -> 0x00c4 }
        r8 = 0;
        if (r16 == 0) goto L_0x0092;
    L_0x0064:
        r2 = "image";
        r0 = r16;
        r2 = r0.contains(r2);	 Catch:{ SQLiteException -> 0x00c4 }
        if (r2 == 0) goto L_0x0092;
    L_0x006e:
        r2 = "content://media/external/images/media";
        r8 = android.net.Uri.parse(r2);	 Catch:{ SQLiteException -> 0x00c4 }
    L_0x0074:
        r2 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x00c4 }
        r2.<init>();	 Catch:{ SQLiteException -> 0x00c4 }
        r4 = "";
        r2 = r2.append(r4);	 Catch:{ SQLiteException -> 0x00c4 }
        r2 = r2.append(r12);	 Catch:{ SQLiteException -> 0x00c4 }
        r2 = r2.toString();	 Catch:{ SQLiteException -> 0x00c4 }
        r2 = android.net.Uri.withAppendedPath(r8, r2);	 Catch:{ SQLiteException -> 0x00c4 }
        if (r9 == 0) goto L_0x0091;
    L_0x008d:
        r9.close();
        r9 = 0;
    L_0x0091:
        return r2;
    L_0x0092:
        r2 = "content://media/external/video/media";
        r8 = android.net.Uri.parse(r2);	 Catch:{ SQLiteException -> 0x00c4 }
        goto L_0x0074;
    L_0x0099:
        r2 = r11.exists();	 Catch:{ SQLiteException -> 0x00c4 }
        if (r2 == 0) goto L_0x00bc;
    L_0x009f:
        r17 = new android.content.ContentValues;	 Catch:{ SQLiteException -> 0x00c4 }
        r17.<init>();	 Catch:{ SQLiteException -> 0x00c4 }
        r0 = r17;
        r1 = r19;
        r0.put(r13, r1);	 Catch:{ SQLiteException -> 0x00c4 }
        r2 = r18.getContentResolver();	 Catch:{ SQLiteException -> 0x00c4 }
        r0 = r17;
        r2 = r2.insert(r3, r0);	 Catch:{ SQLiteException -> 0x00c4 }
        if (r9 == 0) goto L_0x0091;
    L_0x00b7:
        r9.close();
        r9 = 0;
        goto L_0x0091;
    L_0x00bc:
        r2 = 0;
        if (r9 == 0) goto L_0x0091;
    L_0x00bf:
        r9.close();
        r9 = 0;
        goto L_0x0091;
    L_0x00c4:
        r10 = move-exception;
        r2 = "CameraApp";
        r4 = "Could not load photo from database";
        com.lge.camera.util.CamLog.m6e(r2, r4, r10);	 Catch:{ all -> 0x00d4 }
        if (r9 == 0) goto L_0x00d2;
    L_0x00ce:
        r9.close();
        r9 = 0;
    L_0x00d2:
        r2 = 0;
        goto L_0x0091;
    L_0x00d4:
        r2 = move-exception;
        if (r9 == 0) goto L_0x00db;
    L_0x00d7:
        r9.close();
        r9 = 0;
    L_0x00db:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.util.FileUtil.getUriFromPath(android.content.Context, java.lang.String, boolean):android.net.Uri");
    }

    public static int getOrientationFromDB(ContentResolver resolver, Uri uri) {
        int degree = 0;
        Cursor cursor = resolver.query(uri, new String[]{CameraConstants.ORIENTATION}, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                degree = cursor.getInt(cursor.getColumnIndex(CameraConstants.ORIENTATION));
            }
            cursor.close();
        }
        return degree;
    }

    public static int getDurationFromFilePath(Context context, String filePath) {
        if (context == null) {
            return 0;
        }
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(filePath);
        String durStr = mediaMetadataRetriever.extractMetadata(9);
        mediaMetadataRetriever.release();
        if (durStr == null || "".equals(durStr)) {
            return 0;
        }
        return Integer.parseInt(durStr);
    }

    public static void deleteDNGFile(Context context, String filePath) {
        if (filePath != null && filePath.contains(".jpg")) {
            String dngPath = filePath.replace(".jpg", CameraConstants.CAM_RAW_EXTENSION);
            File dngFile = new File(dngPath);
            if (dngFile != null && dngFile.exists()) {
                dngFile.delete();
                context.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(dngFile)));
                CamLog.m3d(CameraConstants.TAG, "-raw- dngPath = " + dngPath + " is deleted.");
            }
        }
    }

    public static String getBucketIDStr(ArrayList<String> list) {
        if (list == null) {
            return null;
        }
        String bucketIdList = "";
        Iterator it = list.iterator();
        while (it.hasNext()) {
            bucketIdList = bucketIdList + "bucket_id=" + getBucketId((String) it.next()) + " OR ";
        }
        it = list.iterator();
        while (it.hasNext()) {
            String t = (String) it.next();
            if (!t.contains("cnas")) {
                bucketIdList = bucketIdList + "bucket_id=" + getBucketId(t, "tr") + " OR ";
            }
        }
        return bucketIdList.substring(0, bucketIdList.length() - 4);
    }

    public static String getBucketId(String currentDir) {
        CamLog.m7i(CameraConstants.TAG, "[Tile] currentDir : " + currentDir);
        return String.valueOf(currentDir.substring(0, currentDir.length() - 1).toLowerCase(Locale.US).hashCode());
    }

    public static String getBucketId(String currentDir, String langCode) {
        return String.valueOf(currentDir.substring(0, currentDir.length() - 1).toLowerCase(Locale.forLanguageTag(langCode)).hashCode());
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    public static boolean isCNasContents(android.net.Uri r11, android.content.Context r12) {
        /*
        r9 = 1;
        r10 = 0;
        r1 = com.lge.camera.constants.ModelProperties.isLguCloudServiceModel();
        if (r1 != 0) goto L_0x0009;
    L_0x0008:
        return r10;
    L_0x0009:
        if (r11 == 0) goto L_0x0008;
    L_0x000b:
        if (r12 == 0) goto L_0x0008;
    L_0x000d:
        r6 = 0;
        r0 = r12.getContentResolver();	 Catch:{ Exception -> 0x004a }
        r1 = 1;
        r2 = new java.lang.String[r1];	 Catch:{ Exception -> 0x004a }
        r1 = 0;
        r3 = "storage_type";
        r2[r1] = r3;	 Catch:{ Exception -> 0x004a }
        r3 = 0;
        r4 = 0;
        r5 = 0;
        r1 = r11;
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x004a }
        r1 = r6.getCount();	 Catch:{ Exception -> 0x004a }
        if (r1 <= 0) goto L_0x0044;
    L_0x0028:
        r6.moveToFirst();	 Catch:{ Exception -> 0x004a }
        r1 = "storage_type";
        r1 = r6.getColumnIndex(r1);	 Catch:{ Exception -> 0x004a }
        r8 = r6.getInt(r1);	 Catch:{ Exception -> 0x004a }
        r1 = 262145; // 0x40001 float:3.67343E-40 double:1.29517E-318;
        if (r8 != r1) goto L_0x0042;
    L_0x003a:
        r1 = r9;
    L_0x003b:
        if (r6 == 0) goto L_0x0040;
    L_0x003d:
        r6.close();
    L_0x0040:
        r10 = r1;
        goto L_0x0008;
    L_0x0042:
        r1 = r10;
        goto L_0x003b;
    L_0x0044:
        if (r6 == 0) goto L_0x0008;
    L_0x0046:
        r6.close();
        goto L_0x0008;
    L_0x004a:
        r7 = move-exception;
        r1 = "CameraApp";
        r2 = "cursor error ";
        com.lge.camera.util.CamLog.m6e(r1, r2, r7);	 Catch:{ all -> 0x0058 }
        if (r6 == 0) goto L_0x0008;
    L_0x0054:
        r6.close();
        goto L_0x0008;
    L_0x0058:
        r1 = move-exception;
        if (r6 == 0) goto L_0x005e;
    L_0x005b:
        r6.close();
    L_0x005e:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.util.FileUtil.isCNasContents(android.net.Uri, android.content.Context):boolean");
    }
}
