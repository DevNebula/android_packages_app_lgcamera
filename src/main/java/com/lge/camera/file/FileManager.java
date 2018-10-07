package com.lge.camera.file;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.constants.MultimediaProperties;
import com.lge.camera.database.OverlapProjectDbAdapter;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FileManager {
    private static final int CONSECUTIVE_ID_MODE_BURST = 0;
    public static final String CROPPED_HEIGHT = "cropped_area_image_height_pixels";
    public static final String CROPPED_LEFT = "cropped_area_left_pixels";
    public static final String CROPPED_TOP = "cropped_area_top_pixels";
    public static final String CROPPED_WIDTH = "cropped_area_image_width_pixels";
    public static final byte[] EOFMarker = new byte[]{(byte) -98};
    public static final String FULL_PANO_HEIGHT = "full_pano_height_pixels";
    public static final String FULL_PANO_WIDTH = "full_pano_width_pixels";
    /* renamed from: ID */
    public static final String f26ID = "_id";
    public static final String IMAGE_ID = "image_id";
    public static final String INIT_HORI_FOV_DEGREE = "initial_horizontal_fov_degrees";
    public static final String PROJECTION_TYPE = "projection_type";
    public static final Uri URI = Uri.parse("content://media/external/images/xmp");
    public static final String USE_PANORAMA = "use_panorama_viewer";
    private static String sCurShotMode = "mode_normal";
    private static boolean sIsLivePhotoEnabled = false;
    private static String sOverlapProjectID = "";

    /* JADX WARNING: Removed duplicated region for block: B:32:0x0061 A:{SYNTHETIC, Splitter: B:32:0x0061} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x004f A:{SYNTHETIC, Splitter: B:23:0x004f} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0061 A:{SYNTHETIC, Splitter: B:32:0x0061} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x004f A:{SYNTHETIC, Splitter: B:23:0x004f} */
    public static void writeFile(byte[] r11, java.lang.String r12, java.lang.String r13) throws java.io.FileNotFoundException {
        /*
        r4 = 0;
        r2 = 0;
        r0 = new java.io.File;	 Catch:{ FileNotFoundException -> 0x0041, IOException -> 0x0055 }
        r0.<init>(r12);	 Catch:{ FileNotFoundException -> 0x0041, IOException -> 0x0055 }
        r7 = r0.exists();	 Catch:{ FileNotFoundException -> 0x0041, IOException -> 0x0055 }
        if (r7 != 0) goto L_0x0010;
    L_0x000d:
        r0.mkdirs();	 Catch:{ FileNotFoundException -> 0x0041, IOException -> 0x0055 }
    L_0x0010:
        r3 = new java.io.File;	 Catch:{ FileNotFoundException -> 0x0041, IOException -> 0x0055 }
        r3.<init>(r12, r13);	 Catch:{ FileNotFoundException -> 0x0041, IOException -> 0x0055 }
        r5 = new java.io.FileOutputStream;	 Catch:{ FileNotFoundException -> 0x00a9, IOException -> 0x00a2, all -> 0x009b }
        r5.<init>(r3);	 Catch:{ FileNotFoundException -> 0x00a9, IOException -> 0x00a2, all -> 0x009b }
        r5.write(r11);	 Catch:{ FileNotFoundException -> 0x00ac, IOException -> 0x00a5, all -> 0x009e }
        if (r5 == 0) goto L_0x00b0;
    L_0x001f:
        r5.close();	 Catch:{ Throwable -> 0x0025 }
        r4 = 0;
    L_0x0023:
        r2 = 0;
    L_0x0024:
        return;
    L_0x0025:
        r6 = move-exception;
        r7 = "CameraApp";
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r9 = "writeData : ";
        r8 = r8.append(r9);
        r8 = r8.append(r6);
        r8 = r8.toString();
        com.lge.camera.util.CamLog.m5e(r7, r8);
        r2 = r3;
        r4 = r5;
        goto L_0x0024;
    L_0x0041:
        r1 = move-exception;
    L_0x0042:
        r7 = "CameraApp";
        r8 = r1.toString();	 Catch:{ all -> 0x004c }
        com.lge.camera.util.CamLog.m12w(r7, r8, r1);	 Catch:{ all -> 0x004c }
        throw r1;	 Catch:{ all -> 0x004c }
    L_0x004c:
        r7 = move-exception;
    L_0x004d:
        if (r4 == 0) goto L_0x0053;
    L_0x004f:
        r4.close();	 Catch:{ Throwable -> 0x0081 }
        r4 = 0;
    L_0x0053:
        r2 = 0;
    L_0x0054:
        throw r7;
    L_0x0055:
        r1 = move-exception;
    L_0x0056:
        r7 = "CameraApp";
        r8 = r1.toString();	 Catch:{ all -> 0x004c }
        com.lge.camera.util.CamLog.m12w(r7, r8, r1);	 Catch:{ all -> 0x004c }
        if (r4 == 0) goto L_0x0065;
    L_0x0061:
        r4.close();	 Catch:{ Throwable -> 0x0067 }
        r4 = 0;
    L_0x0065:
        r2 = 0;
        goto L_0x0024;
    L_0x0067:
        r6 = move-exception;
        r7 = "CameraApp";
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r9 = "writeData : ";
        r8 = r8.append(r9);
        r8 = r8.append(r6);
        r8 = r8.toString();
        com.lge.camera.util.CamLog.m5e(r7, r8);
        goto L_0x0024;
    L_0x0081:
        r6 = move-exception;
        r8 = "CameraApp";
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "writeData : ";
        r9 = r9.append(r10);
        r9 = r9.append(r6);
        r9 = r9.toString();
        com.lge.camera.util.CamLog.m5e(r8, r9);
        goto L_0x0054;
    L_0x009b:
        r7 = move-exception;
        r2 = r3;
        goto L_0x004d;
    L_0x009e:
        r7 = move-exception;
        r2 = r3;
        r4 = r5;
        goto L_0x004d;
    L_0x00a2:
        r1 = move-exception;
        r2 = r3;
        goto L_0x0056;
    L_0x00a5:
        r1 = move-exception;
        r2 = r3;
        r4 = r5;
        goto L_0x0056;
    L_0x00a9:
        r1 = move-exception;
        r2 = r3;
        goto L_0x0042;
    L_0x00ac:
        r1 = move-exception;
        r2 = r3;
        r4 = r5;
        goto L_0x0042;
    L_0x00b0:
        r4 = r5;
        goto L_0x0023;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.file.FileManager.writeFile(byte[], java.lang.String, java.lang.String):void");
    }

    public static int deleteFile(Context context, Uri uri) {
        String filePath = FileUtil.getRealPathFromURI(context, uri);
        if (filePath != null) {
            try {
                File file = new File(filePath);
                if (!file.exists() || !file.delete()) {
                    return C0088R.string.sp_delete_fail_NORMAL;
                }
                context.getContentResolver().delete(uri, null, null);
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "File delete fail. : ", e);
                return C0088R.string.sp_delete_fail_NORMAL;
            }
        }
        return 0;
    }

    public static boolean deleteFile(String filePath) {
        if (filePath == null) {
            return false;
        }
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            CamLog.m6e(CameraConstants.TAG, "File delete fail. : ", e);
            return false;
        }
    }

    public static int deleteAllFileInPath(Context context, String path) {
        try {
            CamLog.m3d(CameraConstants.TAG, "all file delete. path : " + path);
            context.getContentResolver().delete(Files.getContentUri(CameraConstants.STORAGE_NAME_EXTERNAL), "_data LIKE '" + path + "%'", null);
        } catch (Exception e) {
            CamLog.m6e(CameraConstants.TAG, "File delete fail. : ", e);
        }
        return 0;
    }

    public static void deleteAllFiles(String dirPath) {
        if (dirPath != null) {
            try {
                File dir = new File(dirPath);
                if (dir.isDirectory()) {
                    String[] children = dir.list();
                    if (children != null) {
                        for (String child : children) {
                            new File(dir, child).delete();
                        }
                    }
                }
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "File delete fail. : ", e);
            }
        }
    }

    public static Uri addJpegImage(byte[] jpegData, byte[] extraExif, ContentResolver cr, String directory, String fileName, long dateTaken, Location location, int degree, ExifInterface exif) {
        return addJpegImage(jpegData, extraExif, cr, directory, fileName, dateTaken, location, degree, exif, false);
    }

    public static boolean writeJpegImageToFile(byte[] jpegData, byte[] extraExif, String directory, String fileName, ExifInterface exif) throws IOException {
        CamLog.m3d(CameraConstants.TAG, "addJpegImage : " + fileName);
        String path = directory + fileName;
        if (exif != null) {
            try {
                CamLog.m3d(CameraConstants.TAG, "exif is not null >  file write  === start ===");
                File dir = new File(directory);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                if (extraExif != null && extraExif.length > 0) {
                    exif.setTag(exif.buildTag(ExifInterface.TAG_USER_COMMENT, extraExif));
                }
                exif.writeExif(jpegData, path);
                CamLog.m3d(CameraConstants.TAG, "exif is not null >  file write === end ===");
            } catch (IOException e) {
                if (CameraConstants.EXIF_SIZE_EXCEPTION_MESSAGE.equals(e.getMessage())) {
                    throw new IOException(CameraConstants.EXIF_SIZE_EXCEPTION_MESSAGE);
                }
                CamLog.m6e(CameraConstants.TAG, "Failed to write data", e);
                return false;
            } catch (Exception e2) {
                CamLog.m6e(CameraConstants.TAG, "Failed to write data", e2);
                return false;
            }
        }
        CamLog.m3d(CameraConstants.TAG, "exif is null >  file write  === start ===");
        try {
            writeFile(jpegData, directory, fileName);
            CamLog.m3d(CameraConstants.TAG, "exif is null >  file write === end ===");
        } catch (FileNotFoundException e3) {
            CamLog.m6e(CameraConstants.TAG, "Failed to write data", e3);
            return false;
        }
        return true;
    }

    public static Uri addJpegImage(byte[] jpegData, byte[] extraExif, ContentResolver cr, String directory, String fileName, long dateTaken, Location location, int degree, ExifInterface exif, boolean isBurstshot) {
        return addJpegImage(jpegData, extraExif, cr, directory, fileName, dateTaken, location, degree, exif, isBurstshot, false);
    }

    public static Uri addJpegImage(byte[] jpegData, byte[] extraExif, ContentResolver cr, String directory, String fileName, long dateTaken, Location location, int degree, ExifInterface exif, boolean isBurstshot, boolean isLivePhotoEnabled) {
        int[] exifSize = Exif.getExifSize(exif);
        try {
            if (!writeJpegImageToFile(jpegData, extraExif, directory, fileName, exif)) {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isLivePhotoEnabled) {
            return null;
        }
        return registerImageUri(cr, directory, fileName, dateTaken, location, degree, exifSize, isBurstshot);
    }

    public static void registerFileUri(Context ctx, String directory, String fileName) {
        String filePath = directory + fileName;
        File file = new File(filePath);
        CamLog.m3d(CameraConstants.TAG, "registerFileUri filePath : " + filePath);
        ctx.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(file)));
    }

    public static Uri registerImageUri(ContentResolver cr, String directory, String fileName, long dateTaken, Location location, int degree, int[] exifSize, boolean isBurstshot) {
        return registerImageUri(cr, directory, fileName, dateTaken, location, degree, exifSize, isBurstshot, false);
    }

    public static Uri registerImageUri(ContentResolver cr, String directory, String fileName, long dateTaken, Location location, int degree, int[] exifSize, boolean isBurstshot, boolean isLivePhoto) {
        CamLog.m3d(CameraConstants.TAG, "registerImageUri : " + fileName);
        if (CameraConstants.MODE_SNAP.equals(sCurShotMode)) {
            CamLog.m3d(CameraConstants.TAG, "exit shot mode : " + sCurShotMode);
            return null;
        }
        ContentValues values = createCommonContentValue(directory, fileName, dateTaken, location, degree, exifSize);
        if (values == null) {
            return null;
        }
        Uri resultUri = null;
        values.put(CameraConstants.MODE_COLUMN, getCameraModeColumn(fileName, isLivePhoto));
        if (FunctionProperties.isSupportedBurstShot() && !CameraConstants.MODE_SQUARE_OVERLAP.equals(sCurShotMode)) {
            if (isBurstshot) {
                String burstID = getConsecutiveID(0, fileName);
                values.put("burst_id", burstID);
                CamLog.m3d(CameraConstants.TAG, "set burst_id = " + burstID);
            } else {
                values.put("burst_id", fileName);
                CamLog.m3d(CameraConstants.TAG, "set burst_id = " + fileName);
            }
        }
        if (CameraConstants.MODE_SQUARE_OVERLAP.equals(sCurShotMode) && getProjectId() != null) {
            values.put(OverlapProjectDbAdapter.KEY_PROJECT_ID, getProjectId());
            CamLog.m3d(CameraConstants.TAG, "put camera_mode project id : " + getProjectId());
        }
        if (cr != null) {
            try {
                CamLog.m3d(CameraConstants.TAG, "##DEV : pre insert jpeg");
                resultUri = cr.insert(Media.EXTERNAL_CONTENT_URI, values);
                CamLog.m3d(CameraConstants.TAG, "##DEV : after insert jpeg");
                return resultUri;
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "error insert database", e);
                return resultUri;
            }
        }
        CamLog.m11w(CameraConstants.TAG, "ContentResolver is null.");
        return null;
    }

    public static ContentValues createCommonContentValue(String directory, String fileName, long dateTaken, Location location, int degree, int[] exifSize) {
        String title;
        String filePath = directory + fileName;
        if (exifSize == null) {
            exifSize = new int[2];
            exifSize = new int[]{0, 0};
        }
        long size = new File(filePath).length();
        CamLog.m3d(CameraConstants.TAG, "directory : " + filePath);
        ContentValues values = new ContentValues();
        int dotIdx = fileName.lastIndexOf(46);
        if (dotIdx != -1) {
            title = fileName.substring(0, dotIdx);
        } else {
            title = fileName;
        }
        values.put("title", title);
        CamLog.m3d(CameraConstants.TAG, "set uri TITLE = " + title);
        values.put("_display_name", fileName);
        CamLog.m3d(CameraConstants.TAG, "set uri DISPLAY_NAME = " + fileName);
        values.put("datetaken", Long.valueOf(dateTaken));
        CamLog.m3d(CameraConstants.TAG, "set uri DATE_TAKEN = " + dateTaken);
        values.put("date_modified", Long.valueOf(dateTaken / 1000));
        values.put("mime_type", MultimediaProperties.IMAGE_MIME_TYPE);
        values.put(CameraConstants.ORIENTATION, Integer.valueOf(degree));
        values.put("_data", filePath);
        CamLog.m3d(CameraConstants.TAG, "set uri DATA = " + filePath);
        values.put("_size", Long.valueOf(size));
        CamLog.m3d(CameraConstants.TAG, "set uri SIZE = " + size);
        values.put("width", Integer.valueOf(exifSize[0]));
        values.put("height", Integer.valueOf(exifSize[1]));
        CamLog.m3d(CameraConstants.TAG, "set uri WIDTH = " + exifSize[0] + ", HEIGHT = " + exifSize[1]);
        if (location != null) {
            values.put("latitude", Double.valueOf(location.getLatitude()));
            values.put("longitude", Double.valueOf(location.getLongitude()));
        }
        return values;
    }

    public static Uri registerPanoramaUri(ContentResolver cr, String directory, String fileName, long dateTaken, Location location, int degree, int[] exifSize, String usePanoViewer, String projType, int crWidth, int crHeight, int crLeft, int crTop, int fullWidth, int fullHeight, float horzFovDegree) {
        CamLog.m3d(CameraConstants.TAG, "registerImageUri : " + fileName);
        ContentValues values = createCommonContentValue(directory, fileName, dateTaken, location, degree, exifSize);
        if (values == null) {
            return null;
        }
        values.put(CameraConstants.MODE_COLUMN, Integer.valueOf(100));
        values.put(CameraConstants.ORIENTATION, Integer.valueOf(0));
        values.put(USE_PANORAMA, usePanoViewer);
        values.put(PROJECTION_TYPE, projType);
        values.put(CROPPED_WIDTH, Integer.valueOf(crWidth));
        values.put(CROPPED_HEIGHT, Integer.valueOf(crHeight));
        values.put(CROPPED_LEFT, Integer.valueOf(crLeft));
        values.put(CROPPED_TOP, Integer.valueOf(crTop));
        values.put(FULL_PANO_WIDTH, Integer.valueOf(fullWidth));
        values.put(FULL_PANO_HEIGHT, Integer.valueOf(fullHeight));
        values.put(INIT_HORI_FOV_DEGREE, Float.valueOf(horzFovDegree));
        Uri resultUri = null;
        if (cr != null) {
            try {
                CamLog.m3d(CameraConstants.TAG, "##DEV : pre insert jpeg");
                resultUri = cr.insert(Media.EXTERNAL_CONTENT_URI, values);
                CamLog.m3d(CameraConstants.TAG, "##DEV : after insert jpeg");
                return resultUri;
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "error insert database", e);
                return resultUri;
            }
        }
        CamLog.m11w(CameraConstants.TAG, "ContentResolver is null.");
        return null;
    }

    /* JADX WARNING: Missing block: B:6:0x001d, code:
            if (r19.contains(com.lge.camera.managers.PhoneStorageManager.SNAP_DIRECTORY) != false) goto L_0x001f;
     */
    public static android.net.Uri registerVideoUri(android.content.Context r17, android.content.ContentResolver r18, java.lang.String r19, java.lang.String r20, java.lang.String r21, long r22, long r24, android.location.Location r26, int r27, int r28, java.lang.String r29) {
        /*
        r12 = "CameraApp";
        r13 = "registerVideoUri()";
        com.lge.camera.util.CamLog.m3d(r12, r13);
        if (r19 == 0) goto L_0x001f;
    L_0x0009:
        if (r20 == 0) goto L_0x001f;
    L_0x000b:
        r12 = "mode_snap";
        r13 = sCurShotMode;
        r12 = r12.equals(r13);
        if (r12 == 0) goto L_0x003b;
    L_0x0015:
        r12 = "/DCIM/.snap/";
        r0 = r19;
        r12 = r0.contains(r12);
        if (r12 == 0) goto L_0x003b;
    L_0x001f:
        r12 = "CameraApp";
        r13 = new java.lang.StringBuilder;
        r13.<init>();
        r14 = "exit shot mode : ";
        r13 = r13.append(r14);
        r14 = sCurShotMode;
        r13 = r13.append(r14);
        r13 = r13.toString();
        com.lge.camera.util.CamLog.m3d(r12, r13);
        r9 = 0;
    L_0x003a:
        return r9;
    L_0x003b:
        r2 = java.lang.System.currentTimeMillis();
        r9 = 0;
        r12 = 1;
        r0 = r27;
        if (r0 != r12) goto L_0x018a;
    L_0x0045:
        r5 = ".mp4";
    L_0x0047:
        r12 = new java.lang.StringBuilder;
        r12.<init>();
        r0 = r19;
        r12 = r12.append(r0);
        r0 = r20;
        r12 = r12.append(r0);
        r12 = r12.append(r5);
        r6 = r12.toString();
        r12 = "CameraApp";
        r13 = new java.lang.StringBuilder;
        r13.<init>();
        r14 = "filePath : ";
        r13 = r13.append(r14);
        r13 = r13.append(r6);
        r13 = r13.toString();
        com.lge.camera.util.CamLog.m3d(r12, r13);
        r11 = "video/mp4";
        r12 = 4;
        r0 = r27;
        if (r0 == r12) goto L_0x0086;
    L_0x007f:
        r12 = com.lge.camera.constants.ModelProperties.getCarrierCode();	 Catch:{ Exception -> 0x018e }
        r13 = 6;
        if (r12 != r13) goto L_0x008b;
    L_0x0086:
        r12 = 0;
        r11 = com.lge.camera.constants.MultimediaProperties.getVideoMimeType(r12);	 Catch:{ Exception -> 0x018e }
    L_0x008b:
        r12 = "CameraApp";
        r13 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x018e }
        r13.<init>();	 Catch:{ Exception -> 0x018e }
        r14 = "video mime type : ";
        r13 = r13.append(r14);	 Catch:{ Exception -> 0x018e }
        r13 = r13.append(r11);	 Catch:{ Exception -> 0x018e }
        r13 = r13.toString();	 Catch:{ Exception -> 0x018e }
        com.lge.camera.util.CamLog.m9v(r12, r13);	 Catch:{ Exception -> 0x018e }
        r10 = new android.content.ContentValues;	 Catch:{ Exception -> 0x018e }
        r10.<init>();	 Catch:{ Exception -> 0x018e }
        r12 = "title";
        r0 = r20;
        r10.put(r12, r0);	 Catch:{ Exception -> 0x018e }
        r12 = "_display_name";
        r13 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x018e }
        r13.<init>();	 Catch:{ Exception -> 0x018e }
        r0 = r20;
        r13 = r13.append(r0);	 Catch:{ Exception -> 0x018e }
        r13 = r13.append(r5);	 Catch:{ Exception -> 0x018e }
        r13 = r13.toString();	 Catch:{ Exception -> 0x018e }
        r10.put(r12, r13);	 Catch:{ Exception -> 0x018e }
        r12 = "mime_type";
        r10.put(r12, r11);	 Catch:{ Exception -> 0x018e }
        r12 = "datetaken";
        r13 = java.lang.Long.valueOf(r2);	 Catch:{ Exception -> 0x018e }
        r10.put(r12, r13);	 Catch:{ Exception -> 0x018e }
        r7 = 0;
        if (r17 == 0) goto L_0x00dc;
    L_0x00d8:
        r7 = r17.getPackageManager();	 Catch:{ Exception -> 0x018e }
    L_0x00dc:
        if (r7 == 0) goto L_0x00ef;
    L_0x00de:
        r12 = "com.lge.software.gallery_memories";
        r12 = r7.hasSystemFeature(r12);	 Catch:{ Exception -> 0x018e }
        if (r12 == 0) goto L_0x00ef;
    L_0x00e6:
        r12 = "video_datetaken";
        r13 = java.lang.Long.valueOf(r2);	 Catch:{ Exception -> 0x018e }
        r10.put(r12, r13);	 Catch:{ Exception -> 0x018e }
    L_0x00ef:
        r12 = "date_added";
        r13 = java.lang.Long.valueOf(r2);	 Catch:{ Exception -> 0x018e }
        r10.put(r12, r13);	 Catch:{ Exception -> 0x018e }
        r12 = "date_modified";
        r14 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r14 = r2 / r14;
        r13 = java.lang.Long.valueOf(r14);	 Catch:{ Exception -> 0x018e }
        r10.put(r12, r13);	 Catch:{ Exception -> 0x018e }
        r12 = "_data";
        r10.put(r12, r6);	 Catch:{ Exception -> 0x018e }
        r12 = "_size";
        r13 = java.lang.Long.valueOf(r24);	 Catch:{ Exception -> 0x018e }
        r10.put(r12, r13);	 Catch:{ Exception -> 0x018e }
        r12 = "duration";
        r13 = java.lang.Long.valueOf(r22);	 Catch:{ Exception -> 0x018e }
        r10.put(r12, r13);	 Catch:{ Exception -> 0x018e }
        if (r21 == 0) goto L_0x012e;
    L_0x011e:
        r12 = "@";
        r0 = r21;
        r8 = r0.split(r12);	 Catch:{ Exception -> 0x018e }
        r12 = "resolution";
        r13 = 0;
        r13 = r8[r13];	 Catch:{ Exception -> 0x018e }
        r10.put(r12, r13);	 Catch:{ Exception -> 0x018e }
    L_0x012e:
        if (r26 == 0) goto L_0x014a;
    L_0x0130:
        r12 = "latitude";
        r14 = r26.getLatitude();	 Catch:{ Exception -> 0x018e }
        r13 = java.lang.Double.valueOf(r14);	 Catch:{ Exception -> 0x018e }
        r10.put(r12, r13);	 Catch:{ Exception -> 0x018e }
        r12 = "longitude";
        r14 = r26.getLongitude();	 Catch:{ Exception -> 0x018e }
        r13 = java.lang.Double.valueOf(r14);	 Catch:{ Exception -> 0x018e }
        r10.put(r12, r13);	 Catch:{ Exception -> 0x018e }
    L_0x014a:
        r12 = "camera_mode";
        r0 = r28;
        r1 = r21;
        r13 = getVideoModeColumn(r0, r1);	 Catch:{ Exception -> 0x018e }
        r10.put(r12, r13);	 Catch:{ Exception -> 0x018e }
        r12 = "";
        r0 = r29;
        r12 = r12.equals(r0);	 Catch:{ Exception -> 0x018e }
        if (r12 != 0) goto L_0x0168;
    L_0x0161:
        r12 = "burst_id";
        r0 = r29;
        r10.put(r12, r0);	 Catch:{ Exception -> 0x018e }
    L_0x0168:
        r0 = r18;
        r1 = r17;
        r9 = insertToContentResolver(r0, r6, r10, r1);	 Catch:{ Exception -> 0x018e }
        r12 = "CameraApp";
        r13 = new java.lang.StringBuilder;
        r13.<init>();
        r14 = "Current video URI: ";
        r13 = r13.append(r14);
        r13 = r13.append(r9);
        r13 = r13.toString();
        com.lge.camera.util.CamLog.m9v(r12, r13);
        goto L_0x003a;
    L_0x018a:
        r5 = ".3gp";
        goto L_0x0047;
    L_0x018e:
        r4 = move-exception;
        r12 = "CameraApp";
        r13 = "Failed to register uri: %s";
        r14 = 1;
        r14 = new java.lang.Object[r14];	 Catch:{ all -> 0x01be }
        r15 = 0;
        r16 = r4.getMessage();	 Catch:{ all -> 0x01be }
        r14[r15] = r16;	 Catch:{ all -> 0x01be }
        r13 = java.lang.String.format(r13, r14);	 Catch:{ all -> 0x01be }
        com.lge.camera.util.CamLog.m5e(r12, r13);	 Catch:{ all -> 0x01be }
        r12 = "CameraApp";
        r13 = new java.lang.StringBuilder;
        r13.<init>();
        r14 = "Current video URI: ";
        r13 = r13.append(r14);
        r13 = r13.append(r9);
        r13 = r13.toString();
        com.lge.camera.util.CamLog.m9v(r12, r13);
        goto L_0x003a;
    L_0x01be:
        r12 = move-exception;
        r13 = "CameraApp";
        r14 = new java.lang.StringBuilder;
        r14.<init>();
        r15 = "Current video URI: ";
        r14 = r14.append(r15);
        r14 = r14.append(r9);
        r14 = r14.toString();
        com.lge.camera.util.CamLog.m9v(r13, r14);
        throw r12;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.file.FileManager.registerVideoUri(android.content.Context, android.content.ContentResolver, java.lang.String, java.lang.String, java.lang.String, long, long, android.location.Location, int, int, java.lang.String):android.net.Uri");
    }

    private static Uri insertToContentResolver(ContentResolver cr, String filePath, ContentValues values, Context context) {
        Uri uri = null;
        if (cr != null) {
            CamLog.m3d(CameraConstants.TAG, String.format("insert to DB:%s", new Object[]{filePath}));
            uri = cr.insert(Video.Media.EXTERNAL_CONTENT_URI, values);
            CamLog.m3d(CameraConstants.TAG, String.format("insert to DB done.", new Object[0]));
            if (uri == null && context != null) {
                uri = FileUtil.getUriFromPath(context, filePath, false);
                CamLog.m3d(CameraConstants.TAG, "insert DB failed. so get uri from file path : " + uri);
                if (uri != null) {
                    cr.update(uri, values, null, null);
                }
            }
        } else {
            CamLog.m11w(CameraConstants.TAG, "ContentResolver is null.");
        }
        return uri;
    }

    private static String getCameraModeColumn(String fileName, boolean isLivePhoto) {
        int modeColumn = 0;
        if (sCurShotMode.contains(CameraConstants.MODE_PANORAMA)) {
            modeColumn = 1;
        } else if (CameraConstants.MODE_MULTIVIEW.equals(sCurShotMode)) {
            modeColumn = 2;
        } else if (CameraConstants.MODE_POPOUT_CAMERA.equals(sCurShotMode)) {
            modeColumn = 3;
        } else if (CameraConstants.MODE_SQUARE_OVERLAP.equals(sCurShotMode) && getProjectId() != null) {
            modeColumn = 4;
        } else if ("mode_food".equals(sCurShotMode)) {
            modeColumn = 7;
        } else if (CameraConstants.MODE_SQUARE_GRID.equals(sCurShotMode)) {
            modeColumn = 6;
        } else if (CameraConstants.MODE_SQUARE_SPLICE.equals(sCurShotMode)) {
            modeColumn = 5;
        } else if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(sCurShotMode)) {
            modeColumn = 8;
        } else if (CameraConstants.MODE_DUAL_POP_CAMERA.equals(sCurShotMode)) {
            modeColumn = 203;
        } else if (isLivePhoto) {
            modeColumn = 201;
        }
        CamLog.m3d(CameraConstants.TAG, "put camera_mode column : " + String.valueOf(modeColumn));
        return String.valueOf(modeColumn);
    }

    private static String getVideoModeColumn(int specialModeColumn, String resolution) {
        int modeColumn;
        if (specialModeColumn != 0) {
            modeColumn = specialModeColumn;
        } else if (CameraConstants.MODE_SNAP.equals(sCurShotMode)) {
            modeColumn = 10;
        } else if (CameraConstants.MODE_TIME_LAPSE_VIDEO.equals(sCurShotMode)) {
            modeColumn = 11;
        } else if (CameraConstants.MODE_SLOW_MOTION.equals(sCurShotMode)) {
            modeColumn = 12;
        } else if (CameraConstants.MODE_MULTIVIEW.equals(sCurShotMode)) {
            modeColumn = 13;
        } else if (CameraConstants.MODE_POPOUT_CAMERA.equals(sCurShotMode)) {
            modeColumn = 14;
        } else if ("mode_food".equals(sCurShotMode)) {
            modeColumn = 20;
        } else if (CameraConstants.MODE_SQUARE_GRID.equals(sCurShotMode)) {
            modeColumn = 19;
        } else if (CameraConstants.MODE_SQUARE_SPLICE.equals(sCurShotMode)) {
            modeColumn = 18;
        } else if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(sCurShotMode)) {
            modeColumn = 21;
        } else {
            modeColumn = check2to1ratio(resolution, 0);
        }
        CamLog.m3d(CameraConstants.TAG, "put video_mode column : " + String.valueOf(modeColumn));
        return String.valueOf(modeColumn);
    }

    private static int check2to1ratio(String resolution, int modeColumn) {
        if (resolution == null) {
            return modeColumn;
        }
        int[] size = Utils.sizeStringToArray(resolution.split("@")[0]);
        if (size[0] == 0 || Float.compare(((float) size[1]) / ((float) size[0]), 0.5f) != 0) {
            return modeColumn;
        }
        return 17;
    }

    public static void broadcastNewMedia(Context context, Uri uri) {
        if (context == null || uri == null) {
            CamLog.m3d(CameraConstants.TAG, "exit because context=" + context + ", uri=" + uri);
            return;
        }
        ContentResolver resolver = context.getContentResolver();
        String mediaType = resolver.getType(uri);
        if (mediaType != null) {
            if (mediaType.startsWith(CameraConstants.MIME_TYPE_VIDEO)) {
                broadcastNewVideo(context, uri);
            }
            if (mediaType.startsWith(CameraConstants.MIME_TYPE_IMAGE)) {
                broadcastNewPicture(context, uri);
            }
            if (!AppControlUtil.isGuestMode()) {
                SharedPreferenceUtil.saveLastThumbnail(context, uri);
            }
            if ((SecureImageUtil.useSecureLockImage() || (AppControlUtil.isGuestMode() && !AppControlUtil.checkGalleryEnabledOnGuestMode(resolver))) && FileNamer.get().isNewBurstShotInSecure()) {
                if (SecureImageUtil.get().getSecureLockUriListSize() >= 500) {
                    SecureImageUtil.get().removeSecureLockIndex(0);
                }
                SecureImageUtil.get().addSecureLockImageUri(uri);
            }
        }
    }

    public static void broadcastNewPicture(Context context, Uri uri) {
        if (context != null && uri != null) {
            Intent intent = new Intent(CameraConstants.ACTION_NEW_PICTURE, uri);
            intent.addFlags(16777216);
            context.sendBroadcast(intent, CameraConstants.PERMISSION_CAMERA_ACTION);
        }
    }

    public static void broadcastNewVideo(Context context, Uri uri) {
        if (context != null && uri != null) {
            Intent intent = new Intent(CameraConstants.ACTION_NEW_VIDEO, uri);
            intent.addFlags(16777216);
            context.sendBroadcast(intent, CameraConstants.PERMISSION_CAMERA_ACTION);
        }
    }

    public static void requestUpBoxBackupPhoto(Context context, String filepath, boolean condition) {
        if (ModelProperties.getCarrierCode() == 1 || !condition) {
            CamLog.m7i(CameraConstants.TAG, "SetUplusBoxMode : model is not supported.");
            return;
        }
        ArrayList<String> pathList = new ArrayList();
        pathList.add(filepath);
        Intent intent = new Intent();
        intent.setAction("lg.uplusbox.intent.action.CLOUD_BACKUP_PHOTO");
        intent.putStringArrayListExtra("extra_file_path", pathList);
        context.sendBroadcast(intent);
    }

    public static boolean isFileExist(String fileName) {
        if (fileName == null) {
            return false;
        }
        File file = new File(fileName);
        if (file == null || !file.exists()) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0067 A:{SYNTHETIC, Splitter: B:19:0x0067} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x007e A:{SYNTHETIC, Splitter: B:29:0x007e} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x008c A:{SYNTHETIC, Splitter: B:37:0x008c} */
    public static boolean saveTempFile(byte[] r10, java.lang.String r11, java.lang.String r12, java.lang.String r13) {
        /*
        r6 = 0;
        r7 = "CameraApp";
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r9 = "saveTempFile-start:";
        r8 = r8.append(r9);
        r8 = r8.append(r12);
        r8 = r8.toString();
        com.lge.camera.util.CamLog.m7i(r7, r8);
        r4 = 0;
        r0 = new java.io.File;	 Catch:{ FileNotFoundException -> 0x005b, IOException -> 0x0072 }
        r0.<init>(r11);	 Catch:{ FileNotFoundException -> 0x005b, IOException -> 0x0072 }
        r7 = r0.exists();	 Catch:{ FileNotFoundException -> 0x005b, IOException -> 0x0072 }
        if (r7 != 0) goto L_0x0028;
    L_0x0025:
        r0.mkdirs();	 Catch:{ FileNotFoundException -> 0x005b, IOException -> 0x0072 }
    L_0x0028:
        r3 = new java.io.File;	 Catch:{ FileNotFoundException -> 0x005b, IOException -> 0x0072 }
        r7 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x005b, IOException -> 0x0072 }
        r7.<init>();	 Catch:{ FileNotFoundException -> 0x005b, IOException -> 0x0072 }
        r7 = r7.append(r12);	 Catch:{ FileNotFoundException -> 0x005b, IOException -> 0x0072 }
        r7 = r7.append(r13);	 Catch:{ FileNotFoundException -> 0x005b, IOException -> 0x0072 }
        r7 = r7.toString();	 Catch:{ FileNotFoundException -> 0x005b, IOException -> 0x0072 }
        r3.<init>(r11, r7);	 Catch:{ FileNotFoundException -> 0x005b, IOException -> 0x0072 }
        r5 = new java.io.FileOutputStream;	 Catch:{ FileNotFoundException -> 0x005b, IOException -> 0x0072 }
        r5.<init>(r3);	 Catch:{ FileNotFoundException -> 0x005b, IOException -> 0x0072 }
        r5.write(r10);	 Catch:{ FileNotFoundException -> 0x009d, IOException -> 0x009a, all -> 0x0097 }
        if (r5 == 0) goto L_0x00a0;
    L_0x0048:
        r5.close();	 Catch:{ IOException -> 0x0056 }
    L_0x004b:
        r4 = 0;
    L_0x004c:
        r3 = 0;
        r6 = "CameraApp";
        r7 = "saveTempFile-end";
        com.lge.camera.util.CamLog.m3d(r6, r7);
        r6 = 1;
    L_0x0055:
        return r6;
    L_0x0056:
        r1 = move-exception;
        r1.printStackTrace();
        goto L_0x004b;
    L_0x005b:
        r2 = move-exception;
    L_0x005c:
        r7 = "CameraApp";
        r8 = r2.toString();	 Catch:{ all -> 0x0089 }
        com.lge.camera.util.CamLog.m12w(r7, r8, r2);	 Catch:{ all -> 0x0089 }
        if (r4 == 0) goto L_0x006b;
    L_0x0067:
        r4.close();	 Catch:{ IOException -> 0x006d }
    L_0x006a:
        r4 = 0;
    L_0x006b:
        r3 = 0;
        goto L_0x0055;
    L_0x006d:
        r1 = move-exception;
        r1.printStackTrace();
        goto L_0x006a;
    L_0x0072:
        r2 = move-exception;
    L_0x0073:
        r7 = "CameraApp";
        r8 = r2.toString();	 Catch:{ all -> 0x0089 }
        com.lge.camera.util.CamLog.m12w(r7, r8, r2);	 Catch:{ all -> 0x0089 }
        if (r4 == 0) goto L_0x0082;
    L_0x007e:
        r4.close();	 Catch:{ IOException -> 0x0084 }
    L_0x0081:
        r4 = 0;
    L_0x0082:
        r3 = 0;
        goto L_0x0055;
    L_0x0084:
        r1 = move-exception;
        r1.printStackTrace();
        goto L_0x0081;
    L_0x0089:
        r6 = move-exception;
    L_0x008a:
        if (r4 == 0) goto L_0x0090;
    L_0x008c:
        r4.close();	 Catch:{ IOException -> 0x0092 }
    L_0x008f:
        r4 = 0;
    L_0x0090:
        r3 = 0;
        throw r6;
    L_0x0092:
        r1 = move-exception;
        r1.printStackTrace();
        goto L_0x008f;
    L_0x0097:
        r6 = move-exception;
        r4 = r5;
        goto L_0x008a;
    L_0x009a:
        r2 = move-exception;
        r4 = r5;
        goto L_0x0073;
    L_0x009d:
        r2 = move-exception;
        r4 = r5;
        goto L_0x005c;
    L_0x00a0:
        r4 = r5;
        goto L_0x004c;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.file.FileManager.saveTempFile(byte[], java.lang.String, java.lang.String, java.lang.String):boolean");
    }

    public static String getConsecutiveID(int mode, String fileName) {
        StringBuffer consecutiveId = new StringBuffer();
        String str = "Burst";
        str = "Burst";
        String[] arr = fileName.split("_");
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                if (!arr[i].contains(str)) {
                    if (i > 0) {
                        consecutiveId.append("_");
                    }
                    consecutiveId.append(arr[i]);
                }
            }
        }
        CamLog.m3d(CameraConstants.TAG, "consecutiveId.toString() = " + consecutiveId.toString());
        return consecutiveId.toString();
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0054 A:{SYNTHETIC, Splitter: B:22:0x0054} */
    public static android.graphics.Bitmap readBitmapFromPath(java.lang.String r8) {
        /*
        r0 = 0;
        r2 = new java.io.File;
        r2.<init>(r8);
        r3 = 0;
        r4 = new java.io.FileInputStream;	 Catch:{ Exception -> 0x0023 }
        r4.<init>(r2);	 Catch:{ Exception -> 0x0023 }
        r0 = android.graphics.BitmapFactory.decodeStream(r4);	 Catch:{ Exception -> 0x0066, all -> 0x0063 }
        if (r4 == 0) goto L_0x0015;
    L_0x0012:
        r4.close();	 Catch:{ Exception -> 0x0017 }
    L_0x0015:
        r3 = r4;
    L_0x0016:
        return r0;
    L_0x0017:
        r1 = move-exception;
        r5 = "CameraApp";
        r6 = r1.getMessage();
        com.lge.camera.util.CamLog.m5e(r5, r6);
        r3 = r4;
        goto L_0x0016;
    L_0x0023:
        r1 = move-exception;
    L_0x0024:
        r5 = "CameraApp";
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0051 }
        r6.<init>();	 Catch:{ all -> 0x0051 }
        r7 = "Cannot read Bitmap, path = /data/LGCamera/signature/signature.png, message = ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x0051 }
        r7 = r1.getMessage();	 Catch:{ all -> 0x0051 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0051 }
        r6 = r6.toString();	 Catch:{ all -> 0x0051 }
        com.lge.camera.util.CamLog.m5e(r5, r6);	 Catch:{ all -> 0x0051 }
        if (r3 == 0) goto L_0x0016;
    L_0x0042:
        r3.close();	 Catch:{ Exception -> 0x0046 }
        goto L_0x0016;
    L_0x0046:
        r1 = move-exception;
        r5 = "CameraApp";
        r6 = r1.getMessage();
        com.lge.camera.util.CamLog.m5e(r5, r6);
        goto L_0x0016;
    L_0x0051:
        r5 = move-exception;
    L_0x0052:
        if (r3 == 0) goto L_0x0057;
    L_0x0054:
        r3.close();	 Catch:{ Exception -> 0x0058 }
    L_0x0057:
        throw r5;
    L_0x0058:
        r1 = move-exception;
        r6 = "CameraApp";
        r7 = r1.getMessage();
        com.lge.camera.util.CamLog.m5e(r6, r7);
        goto L_0x0057;
    L_0x0063:
        r5 = move-exception;
        r3 = r4;
        goto L_0x0052;
    L_0x0066:
        r1 = move-exception;
        r3 = r4;
        goto L_0x0024;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.file.FileManager.readBitmapFromPath(java.lang.String):android.graphics.Bitmap");
    }

    public static void setShotMode(String shotMode) {
        sCurShotMode = shotMode;
    }

    public static void setProjectId(String projectId) {
        sOverlapProjectID = projectId;
    }

    public static String getProjectId() {
        return sOverlapProjectID;
    }

    public static void copyFileIntoStream(byte[] input, FileOutputStream fos) {
        if (input == null || fos == null) {
            CamLog.m11w(CameraConstants.TAG, "Cannot copy file into Stream");
            return;
        }
        try {
            fos.write(input);
        } catch (IOException e) {
            e.printStackTrace();
            CamLog.m5e(CameraConstants.TAG, e.getMessage());
        }
    }

    public static void copyFileIntoStream(FileInputStream fis, FileOutputStream fos) {
        if (fis != null && fos != null) {
            byte[] buffer = new byte[2048];
            while (true) {
                try {
                    int len = fis.read(buffer);
                    if (len > 0) {
                        fos.write(buffer, 0, len);
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    CamLog.m5e(CameraConstants.TAG, e.getMessage());
                    return;
                }
            }
        }
    }

    public static void insertLivePicEOFMarker(FileOutputStream fos) {
        if (fos != null) {
            try {
                fos.write(EOFMarker);
            } catch (IOException e) {
                e.printStackTrace();
                CamLog.m5e(CameraConstants.TAG, e.getMessage());
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x012d  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00ab  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00ea A:{SYNTHETIC, Splitter: B:30:0x00ea} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00ab  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x012d  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x010d A:{SYNTHETIC, Splitter: B:36:0x010d} */
    public static android.net.Uri addOutFocusJpegImage(byte[] r21, byte[] r22, android.content.ContentResolver r23, java.lang.String r24, java.lang.String r25, long r26, android.location.Location r28, int r29, com.lge.camera.file.ExifInterface r30, byte[] r31, byte[] r32) {
        /*
        r12 = com.lge.camera.file.Exif.getExifSize(r30);
        r0 = r21;
        r1 = r22;
        r2 = r24;
        r3 = r25;
        r4 = r30;
        r6 = writeJpegImageToFile(r0, r1, r2, r3, r4);	 Catch:{ IOException -> 0x0017 }
        if (r6 != 0) goto L_0x001b;
    L_0x0014:
        r18 = 0;
    L_0x0016:
        return r18;
    L_0x0017:
        r14 = move-exception;
        r14.printStackTrace();
    L_0x001b:
        r6 = "CameraApp";
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "outfocus File size  ";
        r7 = r7.append(r8);
        r0 = r21;
        r8 = r0.length;
        r7 = r7.append(r8);
        r7 = r7.toString();
        com.lge.camera.util.CamLog.m5e(r6, r7);
        if (r31 != 0) goto L_0x003a;
    L_0x0038:
        if (r32 == 0) goto L_0x009b;
    L_0x003a:
        r15 = 0;
        r16 = 0;
        r15 = new java.io.File;
        r0 = r24;
        r1 = r25;
        r15.<init>(r0, r1);
        r17 = new java.io.FileOutputStream;	 Catch:{ Exception -> 0x00cb }
        r6 = 1;
        r0 = r17;
        r0.<init>(r15, r6);	 Catch:{ Exception -> 0x00cb }
        if (r31 == 0) goto L_0x0072;
    L_0x0050:
        r0 = r17;
        r1 = r31;
        r0.write(r1);	 Catch:{ Exception -> 0x01a6, all -> 0x01a1 }
        r6 = "CameraApp";
        r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x01a6, all -> 0x01a1 }
        r7.<init>();	 Catch:{ Exception -> 0x01a6, all -> 0x01a1 }
        r8 = "outfocus origin File size  ";
        r7 = r7.append(r8);	 Catch:{ Exception -> 0x01a6, all -> 0x01a1 }
        r0 = r31;
        r8 = r0.length;	 Catch:{ Exception -> 0x01a6, all -> 0x01a1 }
        r7 = r7.append(r8);	 Catch:{ Exception -> 0x01a6, all -> 0x01a1 }
        r7 = r7.toString();	 Catch:{ Exception -> 0x01a6, all -> 0x01a1 }
        com.lge.camera.util.CamLog.m5e(r6, r7);	 Catch:{ Exception -> 0x01a6, all -> 0x01a1 }
    L_0x0072:
        if (r32 == 0) goto L_0x0096;
    L_0x0074:
        r0 = r17;
        r1 = r32;
        r0.write(r1);	 Catch:{ Exception -> 0x01a6, all -> 0x01a1 }
        r6 = "CameraApp";
        r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x01a6, all -> 0x01a1 }
        r7.<init>();	 Catch:{ Exception -> 0x01a6, all -> 0x01a1 }
        r8 = "outfocus meta File size  ";
        r7 = r7.append(r8);	 Catch:{ Exception -> 0x01a6, all -> 0x01a1 }
        r0 = r32;
        r8 = r0.length;	 Catch:{ Exception -> 0x01a6, all -> 0x01a1 }
        r7 = r7.append(r8);	 Catch:{ Exception -> 0x01a6, all -> 0x01a1 }
        r7 = r7.toString();	 Catch:{ Exception -> 0x01a6, all -> 0x01a1 }
        com.lge.camera.util.CamLog.m5e(r6, r7);	 Catch:{ Exception -> 0x01a6, all -> 0x01a1 }
    L_0x0096:
        if (r17 == 0) goto L_0x009b;
    L_0x0098:
        r17.close();	 Catch:{ Throwable -> 0x00af }
    L_0x009b:
        r6 = r24;
        r7 = r25;
        r8 = r26;
        r10 = r28;
        r11 = r29;
        r20 = createCommonContentValue(r6, r7, r8, r10, r11, r12);
        if (r20 != 0) goto L_0x012d;
    L_0x00ab:
        r18 = 0;
        goto L_0x0016;
    L_0x00af:
        r19 = move-exception;
        r6 = "CameraApp";
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "outputStream.close() : ";
        r7 = r7.append(r8);
        r0 = r19;
        r7 = r7.append(r0);
        r7 = r7.toString();
        com.lge.camera.util.CamLog.m5e(r6, r7);
        goto L_0x009b;
    L_0x00cb:
        r14 = move-exception;
    L_0x00cc:
        r6 = "CameraApp";
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x010a }
        r7.<init>();	 Catch:{ all -> 0x010a }
        r8 = " File IO Exception ";
        r7 = r7.append(r8);	 Catch:{ all -> 0x010a }
        r8 = r14.toString();	 Catch:{ all -> 0x010a }
        r7 = r7.append(r8);	 Catch:{ all -> 0x010a }
        r7 = r7.toString();	 Catch:{ all -> 0x010a }
        com.lge.camera.util.CamLog.m5e(r6, r7);	 Catch:{ all -> 0x010a }
        if (r16 == 0) goto L_0x009b;
    L_0x00ea:
        r16.close();	 Catch:{ Throwable -> 0x00ee }
        goto L_0x009b;
    L_0x00ee:
        r19 = move-exception;
        r6 = "CameraApp";
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "outputStream.close() : ";
        r7 = r7.append(r8);
        r0 = r19;
        r7 = r7.append(r0);
        r7 = r7.toString();
        com.lge.camera.util.CamLog.m5e(r6, r7);
        goto L_0x009b;
    L_0x010a:
        r6 = move-exception;
    L_0x010b:
        if (r16 == 0) goto L_0x0110;
    L_0x010d:
        r16.close();	 Catch:{ Throwable -> 0x0111 }
    L_0x0110:
        throw r6;
    L_0x0111:
        r19 = move-exception;
        r7 = "CameraApp";
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r9 = "outputStream.close() : ";
        r8 = r8.append(r9);
        r0 = r19;
        r8 = r8.append(r0);
        r8 = r8.toString();
        com.lge.camera.util.CamLog.m5e(r7, r8);
        goto L_0x0110;
    L_0x012d:
        r18 = 0;
        r6 = "mode_rear_outfocus";
        r7 = sCurShotMode;
        r6 = r6.equals(r7);
        if (r6 == 0) goto L_0x0195;
    L_0x0139:
        r13 = 205; // 0xcd float:2.87E-43 double:1.013E-321;
    L_0x013b:
        r6 = "camera_mode";
        r7 = java.lang.String.valueOf(r13);
        r0 = r20;
        r0.put(r6, r7);
        r6 = com.lge.camera.constants.FunctionProperties.isSupportedBurstShot();
        if (r6 == 0) goto L_0x016f;
    L_0x014c:
        r6 = "burst_id";
        r0 = r20;
        r1 = r25;
        r0.put(r6, r1);
        r6 = "CameraApp";
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "set burst_id = ";
        r7 = r7.append(r8);
        r0 = r25;
        r7 = r7.append(r0);
        r7 = r7.toString();
        com.lge.camera.util.CamLog.m3d(r6, r7);
    L_0x016f:
        if (r23 == 0) goto L_0x0198;
    L_0x0171:
        r6 = "CameraApp";
        r7 = "##DEV : pre insert jpeg";
        com.lge.camera.util.CamLog.m3d(r6, r7);	 Catch:{ Exception -> 0x018b }
        r6 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;	 Catch:{ Exception -> 0x018b }
        r0 = r23;
        r1 = r20;
        r18 = r0.insert(r6, r1);	 Catch:{ Exception -> 0x018b }
        r6 = "CameraApp";
        r7 = "##DEV : after insert jpeg";
        com.lge.camera.util.CamLog.m3d(r6, r7);	 Catch:{ Exception -> 0x018b }
        goto L_0x0016;
    L_0x018b:
        r14 = move-exception;
        r6 = "CameraApp";
        r7 = "error insert database";
        com.lge.camera.util.CamLog.m6e(r6, r7, r14);
        goto L_0x0016;
    L_0x0195:
        r13 = 202; // 0xca float:2.83E-43 double:1.0E-321;
        goto L_0x013b;
    L_0x0198:
        r6 = "CameraApp";
        r7 = "ContentResolver is null.";
        com.lge.camera.util.CamLog.m11w(r6, r7);	 Catch:{ Exception -> 0x018b }
        goto L_0x0016;
    L_0x01a1:
        r6 = move-exception;
        r16 = r17;
        goto L_0x010b;
    L_0x01a6:
        r14 = move-exception;
        r16 = r17;
        goto L_0x00cc;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.file.FileManager.addOutFocusJpegImage(byte[], byte[], android.content.ContentResolver, java.lang.String, java.lang.String, long, android.location.Location, int, com.lge.camera.file.ExifInterface, byte[], byte[]):android.net.Uri");
    }
}
