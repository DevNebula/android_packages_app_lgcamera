package com.lge.camera.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.LdbConstants;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitmapManagingUtil {
    public static final int UNCONSTRAINED = -1;

    public static Bitmap getThumbnailFromUri(Activity activity, Uri uri) {
        return getThumbnailFromUri(activity, uri, 3);
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x007e  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0079  */
    public static android.graphics.Bitmap getThumbnailFromUri(android.app.Activity r19, android.net.Uri r20, int r21) {
        /*
        r0 = "CameraApp";
        r1 = "getThumbnailFromUri START";
        com.lge.camera.util.CamLog.m3d(r0, r1);
        if (r19 == 0) goto L_0x000b;
    L_0x0009:
        if (r20 != 0) goto L_0x000e;
    L_0x000b:
        r18 = 0;
    L_0x000d:
        return r18;
    L_0x000e:
        r0 = 2;
        r2 = new java.lang.String[r0];
        r0 = 0;
        r1 = "_id";
        r2[r0] = r1;
        r0 = 1;
        r1 = "mime_type";
        r2[r0] = r1;
        r15 = 0;
        r13 = 0;
        r16 = 0;
        r0 = r19.getContentResolver();	 Catch:{ Exception -> 0x0063, all -> 0x0074 }
        r3 = 0;
        r4 = 0;
        r5 = 0;
        r1 = r20;
        r13 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0063, all -> 0x0074 }
        if (r13 == 0) goto L_0x0060;
    L_0x002e:
        r12 = r13.getCount();	 Catch:{ Exception -> 0x0063, all -> 0x0074 }
        if (r12 == 0) goto L_0x005d;
    L_0x0034:
        r0 = "_id";
        r10 = r13.getColumnIndexOrThrow(r0);	 Catch:{ Exception -> 0x0063, all -> 0x0074 }
        r0 = "mime_type";
        r11 = r13.getColumnIndexOrThrow(r0);	 Catch:{ Exception -> 0x0063, all -> 0x0074 }
        r13.moveToFirst();	 Catch:{ Exception -> 0x0063, all -> 0x0074 }
        r4 = r13.getLong(r10);	 Catch:{ Exception -> 0x0063, all -> 0x0074 }
        r15 = r13.getString(r11);	 Catch:{ Exception -> 0x00b2 }
    L_0x004b:
        if (r13 == 0) goto L_0x0051;
    L_0x004d:
        r13.close();
        r13 = 0;
    L_0x0051:
        if (r15 != 0) goto L_0x007e;
    L_0x0053:
        r18 = 0;
    L_0x0055:
        r0 = "CameraApp";
        r1 = "getThumbnailFromUri END";
        com.lge.camera.util.CamLog.m3d(r0, r1);
        goto L_0x000d;
    L_0x005d:
        r4 = -1;
        goto L_0x004b;
    L_0x0060:
        r4 = -1;
        goto L_0x004b;
    L_0x0063:
        r14 = move-exception;
        r4 = r16;
    L_0x0066:
        r0 = "CameraApp";
        r1 = "Could not ID from URI";
        com.lge.camera.util.CamLog.m6e(r0, r1, r14);	 Catch:{ all -> 0x00b0 }
        if (r13 == 0) goto L_0x0051;
    L_0x006f:
        r13.close();
        r13 = 0;
        goto L_0x0051;
    L_0x0074:
        r0 = move-exception;
        r4 = r16;
    L_0x0077:
        if (r13 == 0) goto L_0x007d;
    L_0x0079:
        r13.close();
        r13 = 0;
    L_0x007d:
        throw r0;
    L_0x007e:
        r0 = "image/";
        r0 = r15.startsWith(r0);
        if (r0 == 0) goto L_0x009b;
    L_0x0086:
        r0 = "CameraApp";
        r1 = "getThumbnailFromUri get image thumbnail";
        com.lge.camera.util.CamLog.m3d(r0, r1);
        r3 = r19.getContentResolver();
        r6 = 0;
        r9 = 0;
        r8 = r21;
        r18 = android.provider.MediaStore.Images.Thumbnails.getThumbnail(r3, r4, r6, r8, r9);
        goto L_0x0055;
    L_0x009b:
        r0 = "CameraApp";
        r1 = "getThumbnailFromUri get video thumbnail";
        com.lge.camera.util.CamLog.m3d(r0, r1);
        r3 = r19.getContentResolver();
        r6 = 0;
        r9 = 0;
        r8 = r21;
        r18 = android.provider.MediaStore.Video.Thumbnails.getThumbnail(r3, r4, r6, r8, r9);
        goto L_0x0055;
    L_0x00b0:
        r0 = move-exception;
        goto L_0x0077;
    L_0x00b2:
        r14 = move-exception;
        goto L_0x0066;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.util.BitmapManagingUtil.getThumbnailFromUri(android.app.Activity, android.net.Uri, int):android.graphics.Bitmap");
    }

    public static int[] getFitSizeOfBitmapForLCD(Activity activity, int imageWidth, int imageHeight, int degree) {
        int[] bitmapSize = new int[2];
        int dstWidth = 0;
        int dstHeight = 0;
        if (activity != null) {
            DisplayMetrics outMetrics = Utils.getWindowRealMatics(activity);
            int lcdWidth = outMetrics.widthPixels;
            int lcdHeight = outMetrics.heightPixels;
            if (degree == 0 || degree == 180) {
                lcdWidth = outMetrics.heightPixels;
                lcdHeight = outMetrics.widthPixels;
            }
            float imageRatio;
            if (imageWidth >= imageHeight) {
                imageRatio = ((float) imageWidth) / ((float) imageHeight);
                if (imageRatio > ((float) lcdWidth) / ((float) lcdHeight)) {
                    dstWidth = lcdWidth;
                    dstHeight = (int) (((float) lcdWidth) / imageRatio);
                } else {
                    dstHeight = lcdHeight;
                    dstWidth = (int) (((float) lcdHeight) * imageRatio);
                }
            } else {
                imageRatio = ((float) imageHeight) / ((float) imageWidth);
                if (imageRatio > ((float) lcdWidth) / ((float) lcdHeight)) {
                    dstHeight = lcdWidth;
                    dstWidth = (int) (((float) lcdWidth) / imageRatio);
                } else {
                    dstWidth = lcdHeight;
                    dstHeight = (int) (((float) lcdHeight) * imageRatio);
                }
            }
        }
        bitmapSize[0] = dstWidth;
        bitmapSize[1] = dstHeight;
        return bitmapSize;
    }

    public static int[] calcFitSizeOfImageForLCD(Activity activity, int imageWidth, int imageHeight, int degree) {
        int[] bitmapSize = new int[2];
        int dstWidth = 0;
        int dstHeight = 0;
        if (activity != null) {
            DisplayMetrics outMetrics = Utils.getWindowMatics(activity);
            int lcdWidth = outMetrics.widthPixels;
            int lcdHeight = outMetrics.heightPixels;
            if (degree == 0 || degree == 180) {
                lcdWidth = outMetrics.heightPixels;
                lcdHeight = outMetrics.widthPixels;
            }
            float imageRatio;
            if (imageWidth >= imageHeight) {
                imageRatio = ((float) imageWidth) / ((float) imageHeight);
                if (imageRatio > ((float) lcdWidth) / ((float) lcdHeight)) {
                    if (degree == 270 || degree == 90) {
                        dstWidth = lcdWidth;
                        dstHeight = (int) (((float) lcdWidth) / imageRatio);
                    } else {
                        dstWidth = lcdHeight;
                        dstHeight = (int) (((float) lcdHeight) / imageRatio);
                    }
                } else if (degree == 270 || degree == 90) {
                    dstHeight = lcdHeight;
                    dstWidth = (int) (((float) lcdHeight) * imageRatio);
                } else {
                    dstWidth = lcdHeight;
                    dstHeight = (int) (((float) lcdHeight) / imageRatio);
                }
            } else {
                imageRatio = ((float) imageHeight) / ((float) imageWidth);
                if (imageRatio > ((float) lcdWidth) / ((float) lcdHeight)) {
                    if (degree == 270 || degree == 90) {
                        dstHeight = lcdHeight;
                        dstWidth = (int) (((float) lcdHeight) / imageRatio);
                    } else {
                        dstHeight = lcdWidth;
                        dstWidth = (int) (((float) lcdWidth) / imageRatio);
                    }
                } else if (degree == 270 || degree == 90) {
                    dstHeight = lcdHeight;
                    dstWidth = (int) (((float) lcdHeight) / imageRatio);
                } else {
                    dstWidth = lcdHeight;
                    dstHeight = (int) (((float) lcdHeight) * imageRatio);
                }
            }
        }
        bitmapSize[0] = dstWidth;
        bitmapSize[1] = dstHeight;
        CamLog.m3d(CameraConstants.TAG, "dstWidth = " + dstWidth + ", dstHeight = " + dstHeight);
        return bitmapSize;
    }

    public static Bitmap loadScaledBitmap(ContentResolver cr, String strURI, int dstWidth, int dstHeight) {
        CamLog.m7i(CameraConstants.TAG, "loadBitmp uri = " + strURI);
        if (strURI == null) {
            return null;
        }
        Uri uri = Uri.parse(strURI);
        if (uri == null) {
            return null;
        }
        Bitmap resizeBmp;
        String strScheme = uri.getScheme();
        Bitmap bitmap;
        if (strScheme == null || strScheme.compareToIgnoreCase("file") == 0) {
            String filePath = uri.getPath();
            Options opts = new Options();
            opts.inDither = true;
            opts.inSampleSize = getSampleSize(null, null, filePath, opts, dstWidth, dstHeight);
            bitmap = BitmapFactory.decodeFile(filePath, opts);
            if (bitmap == null || (bitmap.getWidth() == dstWidth && bitmap.getHeight() == dstHeight)) {
                return bitmap;
            }
            resizeBmp = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
            bitmap.recycle();
        } else {
            bitmap = loadBitmapUsingFileDescriptor(cr, uri, dstWidth, dstHeight);
            if (bitmap == null || (bitmap.getWidth() == dstWidth && bitmap.getHeight() == dstHeight)) {
                return bitmap;
            }
            resizeBmp = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
            bitmap.recycle();
        }
        return resizeBmp;
    }

    public static Bitmap loadScaledandRotatedBitmap(ContentResolver cr, String strURI, int dstWidth, int dstHeight, int degree) {
        CamLog.m7i(CameraConstants.TAG, "loadBitmp uri = " + strURI);
        if (strURI != null) {
            Uri uri = Uri.parse(strURI);
            if (uri != null) {
                Bitmap bitmap;
                Bitmap resizeBmp;
                String strScheme = uri.getScheme();
                if (strScheme != null) {
                    if (strScheme.compareToIgnoreCase("file") != 0) {
                        bitmap = loadBitmapUsingFileDescriptor(cr, uri, dstWidth, dstHeight);
                        if (bitmap == null) {
                            return bitmap;
                        }
                        Matrix matrix = new Matrix();
                        if (!(bitmap.getWidth() == dstWidth && bitmap.getHeight() == dstHeight)) {
                            float s1;
                            float s1x = ((float) dstWidth) / ((float) bitmap.getWidth());
                            float s1y = ((float) dstHeight) / ((float) bitmap.getHeight());
                            if (s1x < s1y) {
                                s1 = s1x;
                            } else {
                                s1 = s1y;
                            }
                            matrix.postScale(s1, s1);
                        }
                        matrix.postRotate((float) degree);
                        resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        if (matrix != null) {
                        }
                        if (resizeBmp != bitmap) {
                            bitmap.recycle();
                        }
                        return resizeBmp;
                    }
                }
                String filePath = uri.getPath();
                Options opts = new Options();
                opts.inDither = true;
                opts.inSampleSize = getSampleSize(null, null, filePath, opts, dstWidth, dstHeight);
                bitmap = BitmapFactory.decodeFile(filePath, opts);
                if (bitmap == null) {
                    return bitmap;
                }
                resizeBmp = getRotatedImage(Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true), degree, false);
                bitmap.recycle();
                return resizeBmp;
            }
        }
        return null;
    }

    private static Bitmap loadBitmapUsingFileDescriptor(ContentResolver cr, Uri uri, int dstWidth, int dstHeight) {
        Bitmap bitmap = null;
        ParcelFileDescriptor pfd = null;
        try {
            pfd = cr.openFileDescriptor(uri, LdbConstants.LDB_CAMERA_ID_REAR_NORMAL);
            if (pfd == null) {
                CamLog.m11w(CameraConstants.TAG, "File description is null.");
                if (pfd == null) {
                    return null;
                }
                try {
                    pfd.close();
                    return null;
                } catch (IOException ex) {
                    CamLog.m5e(CameraConstants.TAG, "loadBitmapFromFile() IOException! " + ex);
                    return null;
                }
            }
            FileDescriptor fd = pfd.getFileDescriptor();
            Options opts = new Options();
            opts.inDither = true;
            opts.inSampleSize = getSampleSize(null, fd, null, opts, dstWidth, dstHeight);
            bitmap = BitmapFactory.decodeFileDescriptor(fd, null, opts);
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (IOException ex2) {
                    CamLog.m5e(CameraConstants.TAG, "loadBitmapFromFile() IOException! " + ex2);
                }
            }
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            CamLog.m5e(CameraConstants.TAG, "loadBitmapFromFile() FileNotFoundException! ");
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (IOException ex22) {
                    CamLog.m5e(CameraConstants.TAG, "loadBitmapFromFile() IOException! " + ex22);
                }
            }
        } catch (Throwable th) {
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (IOException ex222) {
                    CamLog.m5e(CameraConstants.TAG, "loadBitmapFromFile() IOException! " + ex222);
                }
            }
        }
    }

    public static int getSampleSize(byte[] jpegData, FileDescriptor fd, String filePath, Options opts, int targetWidth, int targetHeight) {
        if (opts == null) {
            return 4;
        }
        int imageLength;
        int targetLength;
        opts.inJustDecodeBounds = true;
        if (jpegData != null) {
            BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, opts);
        } else if (fd != null) {
            BitmapFactory.decodeFileDescriptor(fd, null, opts);
        } else if (filePath != null) {
            BitmapFactory.decodeFile(filePath, opts);
        }
        int imageWidth = opts.outWidth;
        int imageHeight = opts.outHeight;
        if (imageHeight >= imageWidth) {
            imageLength = imageHeight;
        } else {
            imageLength = imageWidth;
        }
        if (targetHeight >= targetWidth) {
            targetLength = targetHeight;
        } else {
            targetLength = targetWidth;
        }
        if (imageWidth <= 0 || imageHeight <= 0) {
            return 4;
        }
        int sampleSize = 1;
        while (imageLength / 2 >= targetLength) {
            imageLength /= 2;
            sampleSize *= 2;
            if (imageLength != 0) {
                if (targetLength == 0) {
                    break;
                }
            }
            break;
        }
        opts.inJustDecodeBounds = false;
        CamLog.m7i(CameraConstants.TAG, "getSampleSize is = " + sampleSize);
        return sampleSize;
    }

    public static Bitmap makeBitmap(byte[] jpegData, int minSideLength, int maxNumOfPixels) {
        try {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);
            if (options.mCancel || options.outWidth == -1 || options.outHeight == -1) {
                CamLog.m11w(CameraConstants.TAG, "makeBitmap decordByteArray fail");
                return null;
            }
            options.inSampleSize = computeSampleSize(options, -1, maxNumOfPixels);
            CamLog.m3d(CameraConstants.TAG, "maxNumOfPixels : " + maxNumOfPixels + "options.inSampleSize: " + options.inSampleSize);
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPreferredConfig = Config.ARGB_8888;
            return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);
        } catch (OutOfMemoryError ex) {
            CamLog.m6e(CameraConstants.TAG, "Got oom exception ", ex);
            return null;
        }
    }

    public static Bitmap makeScaledBitmap(byte[] jpegData, int dstWidth, int dstHeight) {
        Bitmap resizeBmp = null;
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);
        if (options.mCancel || options.outWidth == -1 || options.outHeight == -1) {
            return null;
        }
        options.inJustDecodeBounds = false;
        options.inDither = true;
        options.inPreferredConfig = Config.ARGB_8888;
        options.inMutable = true;
        options.inSampleSize = getSampleSize(jpegData, null, null, options, dstWidth, dstHeight);
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);
        if (bitmap != null) {
            if (bitmap.getWidth() == dstWidth && bitmap.getHeight() == dstHeight) {
                return bitmap;
            }
            resizeBmp = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
            bitmap.recycle();
        }
        return resizeBmp;
    }

    public static int computeSampleSize(Options options, int minSideLength, int maxNumOfPixels) {
        int roundedSize = 1;
        if (maxNumOfPixels != 1) {
            int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
            if (initialSize <= 8) {
                roundedSize = 1;
                while (roundedSize < initialSize) {
                    roundedSize <<= 1;
                }
            } else {
                roundedSize = ((initialSize + 7) / 8) * 8;
            }
            CamLog.m3d(CameraConstants.TAG, "computeSampleSize() return = " + roundedSize);
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(Options options, int minSideLength, int maxNumOfPixels) {
        int lowerBound;
        int upperBound;
        double w = (double) options.outWidth;
        double h = (double) options.outHeight;
        if (maxNumOfPixels == -1) {
            lowerBound = 1;
        } else {
            lowerBound = (int) Math.ceil(Math.sqrt((w * h) / ((double) maxNumOfPixels)));
        }
        if (minSideLength == -1) {
            upperBound = 128;
        } else {
            upperBound = (int) Math.min(Math.floor(w / ((double) minSideLength)), Math.floor(h / ((double) minSideLength)));
        }
        if (upperBound < lowerBound) {
            return lowerBound;
        }
        if (maxNumOfPixels == -1 && minSideLength == -1) {
            return 1;
        }
        if (minSideLength != -1) {
            return upperBound;
        }
        return lowerBound;
    }

    public static Bitmap getRotatedImage(Bitmap bmp, int degree, boolean mirror) {
        if (bmp == null) {
            return null;
        }
        if (degree == 0 && !mirror) {
            return bmp;
        }
        Matrix matrix = new Matrix();
        if (mirror) {
            matrix.setScale(-1.0f, 1.0f);
        }
        if (degree != 0) {
            matrix.postRotate((float) degree);
        }
        Bitmap rotated = null;
        try {
            rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        } catch (OutOfMemoryError err) {
            Bitmap tmp = Bitmap.createScaledBitmap(bmp, bmp.getHeight() / 4, bmp.getWidth() / 4, true);
            rotated = Bitmap.createBitmap(tmp, 0, 0, tmp.getWidth(), tmp.getHeight(), matrix, true);
            tmp.recycle();
            CamLog.m6e(CameraConstants.TAG, "error occurred rotating image because of OutOfMemory", err);
        } finally {
            if (bmp != null) {
                bmp.recycle();
            }
        }
        if (matrix != null) {
        }
        return rotated;
    }

    public static Bitmap getRoundedImage(Bitmap bmp, int width, int height, int radius) {
        if (bmp == null) {
            return null;
        }
        int padding = (width - radius) / 2;
        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Bitmap resize = Bitmap.createScaledBitmap(bmp, width, height, true);
        BitmapShader shader = new BitmapShader(resize, TileMode.CLAMP, TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);
        Canvas canvas = new Canvas(output);
        Rect rect = new Rect(padding, padding, padding + radius, padding + radius);
        RectF rectf = new RectF(rect);
        canvas.drawOval(rectf, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(resize, rect, rectf, paint);
        resize.recycle();
        bmp.recycle();
        return output;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int radius) {
        if (bitmap == null) {
            return null;
        }
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawRoundRect(rectF, (float) radius, (float) radius, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();
        return output;
    }

    public static byte[] makeFlipImage(byte[] jpegData, boolean isFlip, int exifOrientation) {
        Bitmap bitmap = makeFlipBitmap(jpegData, isFlip, exifOrientation);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        if (bitmap == null) {
            return jpegData;
        }
        bitmap.compress(CompressFormat.JPEG, 95, outStream);
        byte[] finalJpegData = outStream.toByteArray();
        bitmap.recycle();
        return finalJpegData;
    }

    public static Bitmap makeFlipBitmap(byte[] jpegData, boolean isFlip, int exifOrientation) {
        try {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);
            if (options.mCancel || options.outWidth == -1 || options.outHeight == -1) {
                return null;
            }
            options.inJustDecodeBounds = false;
            options.inDither = true;
            options.inPreferredConfig = Config.ARGB_8888;
            options.inMutable = true;
            if (isFlip) {
                return makeFlipBitmap(BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options), exifOrientation);
            }
            return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);
        } catch (OutOfMemoryError ex) {
            CamLog.m6e(CameraConstants.TAG, "Got oom exception ", ex);
            return null;
        }
    }

    public static Bitmap makeFlipBitmap(Bitmap bitmap, int orientation) {
        Matrix sideInversion = new Matrix();
        if (orientation == 90 || orientation == 270) {
            sideInversion.setScale(1.0f, -1.0f);
        } else {
            sideInversion.setScale(-1.0f, 1.0f);
        }
        Bitmap convertBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), sideInversion, false);
        bitmap.recycle();
        return convertBmp;
    }

    public static Bitmap resizeBitmapImage(Bitmap bitmap, int maxWidth, int maxHeight) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = width;
        int newHeight = height;
        if (width > height) {
            if (maxWidth < width) {
                newHeight = (int) (((float) height) * (((float) maxWidth) / ((float) width)));
                newWidth = maxWidth;
            }
        } else if (maxHeight < height) {
            newWidth = (int) (((float) width) * (((float) maxHeight) / ((float) height)));
            newHeight = maxHeight;
        }
        Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        if (bitmap == null) {
            return bitmapResized;
        }
        bitmap.recycle();
        return bitmapResized;
    }

    public static void recycleBitmapDrawable(Drawable drawable) {
        if (drawable != null) {
            try {
                if (drawable instanceof BitmapDrawable) {
                    Bitmap bm = ((BitmapDrawable) drawable).getBitmap();
                    if (bm != null) {
                        bm.recycle();
                    }
                }
                drawable.setCallback(null);
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "recycleBitmapDrawable Exception ", e);
            }
        }
    }

    public static void clearImageViewDrawable(ImageView imageView) {
        if (imageView != null) {
            try {
                Drawable drawable = imageView.getDrawable();
                if (drawable != null) {
                    imageView.setImageDrawable(null);
                    drawable.setCallback(null);
                    recycleBitmapDrawable(drawable);
                }
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "clearImageViewDrawable Exception ", e);
            }
        }
    }

    public static void recursiveRecycle(View rootView) {
        if (rootView != null) {
            rootView.setBackground(null);
            if (rootView instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) rootView;
                int count = group.getChildCount();
                for (int i = 0; i < count; i++) {
                    recursiveRecycle(group.getChildAt(i));
                }
                if (!(rootView instanceof AdapterView)) {
                    group.removeAllViews();
                }
            }
            if (rootView instanceof ImageView) {
                clearImageViewDrawable((ImageView) rootView);
            }
        }
    }

    public static Bitmap drawTextToBitmap(Context gContext, Bitmap gBitmap, int x, int y, String gText, int gFontSize, int gColor) {
        return drawTextToBitmap(gContext, gBitmap, x, y, gText, gFontSize, gColor, null);
    }

    public static Bitmap drawTextToBitmap(Context gContext, Bitmap gBitmap, int x, int y, String gText, int gFontSize, int gColor, String gTypeFace) {
        if (gBitmap == null) {
            return null;
        }
        Config bitmapConfig = gBitmap.getConfig();
        if (bitmapConfig == null) {
            bitmapConfig = Config.ARGB_8888;
        }
        Bitmap bitmap = gBitmap.copy(bitmapConfig, true);
        gBitmap.recycle();
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(1);
        paint.setColor(gColor);
        paint.setTextSize((float) gFontSize);
        paint.setShadowLayer(1.0f, 0.0f, 1.0f, -1);
        paint.getTextBounds(gText, 0, gText.length(), new Rect());
        Typeface typeFace = CameraTypeface.get(gContext, gTypeFace);
        if (gTypeFace != null) {
            paint.setTypeface(typeFace);
        }
        canvas.drawText(gText, (float) x, (float) y, paint);
        return bitmap;
    }

    public static boolean saveBitmapToFile(String path, Bitmap bmp) {
        OutputStream outputStream;
        File copyFile = new File(path);
        try {
            copyFile.createNewFile();
            try {
                OutputStream out = new FileOutputStream(copyFile);
                boolean result = bmp.compress(CompressFormat.JPEG, 100, out);
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        outputStream = out;
                        return false;
                    }
                }
                outputStream = out;
                return result;
            } catch (FileNotFoundException e2) {
                e2.printStackTrace();
                return false;
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            return false;
        }
    }

    public static Bitmap getBitmap(Context context, int id) {
        if (context == null) {
            CamLog.m3d(CameraConstants.TAG, "context is null");
            return null;
        } else if (id <= 0) {
            CamLog.m11w(CameraConstants.TAG, "invalid id");
            return null;
        } else {
            Drawable drawable = context.getDrawable(id);
            if (drawable == null) {
                CamLog.m11w(CameraConstants.TAG, "drawable is null");
                return null;
            } else if (!(drawable instanceof VectorDrawable)) {
                return BitmapFactory.decodeResource(context.getResources(), id);
            } else {
                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                return bitmap;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x002e A:{SYNTHETIC, Splitter: B:7:0x002e} */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0037  */
    /* JADX WARNING: Removed duplicated region for block: B:26:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0040  */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x002e A:{SYNTHETIC, Splitter: B:7:0x002e} */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0037  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0040  */
    /* JADX WARNING: Removed duplicated region for block: B:26:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x002e A:{SYNTHETIC, Splitter: B:7:0x002e} */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0037  */
    /* JADX WARNING: Removed duplicated region for block: B:26:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0040  */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x002e A:{SYNTHETIC, Splitter: B:7:0x002e} */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0037  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0040  */
    /* JADX WARNING: Removed duplicated region for block: B:26:? A:{SYNTHETIC, RETURN} */
    public static void saveCroppedImage(java.lang.String r10, java.lang.String r11, com.lge.camera.file.ExifInterface r12) {
        /*
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r6 = r6.append(r10);
        r6 = r6.append(r11);
        r5 = r6.toString();
        r0 = android.graphics.BitmapFactory.decodeFile(r5);
        r6 = 564; // 0x234 float:7.9E-43 double:2.787E-321;
        r7 = 0;
        r8 = 1432; // 0x598 float:2.007E-42 double:7.075E-321;
        r9 = r0.getHeight();
        r1 = android.graphics.Bitmap.createBitmap(r0, r6, r7, r8, r9);
        r3 = 0;
        r4 = new java.io.FileOutputStream;	 Catch:{ FileNotFoundException -> 0x0044, IOException -> 0x0049 }
        r4.<init>(r5);	 Catch:{ FileNotFoundException -> 0x0044, IOException -> 0x0049 }
        r12.writeExif(r1, r4);	 Catch:{ FileNotFoundException -> 0x0056, IOException -> 0x0053 }
        r3 = r4;
    L_0x002c:
        if (r3 == 0) goto L_0x0031;
    L_0x002e:
        r3.close();	 Catch:{ IOException -> 0x004e }
    L_0x0031:
        r6 = r0.isRecycled();
        if (r6 != 0) goto L_0x003a;
    L_0x0037:
        r0.recycle();
    L_0x003a:
        r6 = r1.isRecycled();
        if (r6 != 0) goto L_0x0043;
    L_0x0040:
        r1.recycle();
    L_0x0043:
        return;
    L_0x0044:
        r2 = move-exception;
    L_0x0045:
        r2.printStackTrace();
        goto L_0x002c;
    L_0x0049:
        r2 = move-exception;
    L_0x004a:
        r2.printStackTrace();
        goto L_0x002c;
    L_0x004e:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x0031;
    L_0x0053:
        r2 = move-exception;
        r3 = r4;
        goto L_0x004a;
    L_0x0056:
        r2 = move-exception;
        r3 = r4;
        goto L_0x0045;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.util.BitmapManagingUtil.saveCroppedImage(java.lang.String, java.lang.String, com.lge.camera.file.ExifInterface):void");
    }

    public static int[] getImageSize(Context context, Uri uri) {
        int imageWidth = 0;
        int imageHeight = 0;
        Options options = new Options();
        if (options != null) {
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(FileUtil.getRealPathFromURI(context, uri), options);
            imageWidth = options.outWidth;
            imageHeight = options.outHeight;
        }
        CamLog.m7i(CameraConstants.TAG, " imageWidth : " + imageWidth + " / imageHeight : " + imageHeight);
        return new int[]{imageWidth, imageHeight};
    }

    public static Bitmap cropBitmap(Context context, Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        if (bitmap.getHeight() <= 0 || bitmap.getWidth() <= 0) {
            CamLog.m5e(CameraConstants.TAG, "bitmap error  width : " + bitmap.getWidth() + " / height : " + bitmap.getHeight());
            return null;
        }
        int referenceWidth = RatioCalcUtil.getSizeCalculatedByPercentage(context, true, 0.11112f);
        int referenceHeight = referenceWidth;
        if (bitmap.getWidth() < referenceWidth) {
            bitmap = Bitmap.createScaledBitmap(bitmap, referenceWidth, (int) (((float) bitmap.getHeight()) * (((float) referenceWidth) / ((float) bitmap.getWidth()))), true);
        }
        if (bitmap.getHeight() < referenceHeight) {
            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (((float) bitmap.getWidth()) * (((float) referenceHeight) / ((float) bitmap.getHeight()))), referenceHeight, true);
        }
        int startX = (bitmap.getWidth() - referenceWidth) / 2;
        int startY = (bitmap.getHeight() - referenceHeight) / 2;
        try {
            if (bitmap.getHeight() <= 0 || bitmap.getWidth() <= 0) {
                return null;
            }
            return Bitmap.createBitmap(bitmap, startX, startY, referenceWidth, referenceHeight);
        } catch (OutOfMemoryError e) {
            CamLog.m6e(CameraConstants.TAG, "[Tile] error occurred rotating image because of OutOfMemory", e);
            return null;
        } catch (IllegalStateException e2) {
            CamLog.m5e(CameraConstants.TAG, "IllegalStateException");
            return null;
        }
    }

    public static BitmapDrawable convertSVGToBitmap(Context context, Resources res, int svgResId) {
        Drawable vectorDrawable = context.getDrawable(svgResId);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return new BitmapDrawable(res, bitmap);
    }

    public static Bitmap getBitmapFromByteArray(byte[] stream, int scrW, int srcH, Matrix matrix) {
        Bitmap bm = Bitmap.createBitmap(scrW, srcH, Config.ARGB_8888);
        ColorConverter.byteArrayToBitmap(bm, stream);
        if (matrix == null) {
            return bm;
        }
        Bitmap bmRotate = Bitmap.createBitmap(bm, 0, 0, scrW, srcH, matrix, true);
        if (bm.isRecycled()) {
            return bmRotate;
        }
        bm.recycle();
        return bmRotate;
    }

    public static Bitmap getScaledAndRotatedBitmap(Bitmap originalBitmap, int dstWidth, int dstHeight, int degree) {
        if (originalBitmap == null || originalBitmap.isRecycled()) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.preScale(((float) dstWidth) / ((float) originalBitmap.getWidth()), ((float) dstHeight) / ((float) originalBitmap.getHeight()));
        matrix.postRotate((float) degree);
        return Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
    }
}
