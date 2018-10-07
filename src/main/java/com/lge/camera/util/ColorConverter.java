package com.lge.camera.util;

import android.graphics.Bitmap;
import android.support.p000v4.view.ViewCompat;
import java.nio.ByteBuffer;

public class ColorConverter {
    public static final native void RGBToYuv420sp(int[] iArr, byte[] bArr, int i, int i2);

    public static final native void byteArrayToBitmap(Bitmap bitmap, byte[] bArr);

    public static final native void flipYuvImage(byte[] bArr, byte[] bArr2, int i, int i2);

    public static final native void mergeYuvChannels(byte[] bArr, ByteBuffer byteBuffer, ByteBuffer byteBuffer2, ByteBuffer byteBuffer3, int i, int i2, int i3);

    public static final native void nv12ToBitmap(Bitmap bitmap, byte[] bArr, int i, int i2);

    public static final native void nv12VenusToBitmap(Bitmap bitmap, byte[] bArr, int i, int i2);

    public static final native void nv21ToRgbArrayWithResize(byte[] bArr, ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3, int i4, int i5, int i6);

    public static final native void nv21ToRgbArrayWithResizeAndCrop(byte[] bArr, ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8);

    public static final native void yuv420spToBitmap(Bitmap bitmap, byte[] bArr, int i, int i2);

    public static final native void yuvToRgb(ByteBuffer byteBuffer, byte[] bArr, int i, int i2);

    public static final native void yuvToRgbArrayWithResize(byte[] bArr, byte[] bArr2, int i, int i2, int i3, int i4, int i5, int i6);

    public static final native void yuvToRgbArrayWithResizeAndCrop(byte[] bArr, byte[] bArr2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8);

    public static final native void yuvToRgbWithResize(ByteBuffer byteBuffer, byte[] bArr, int i, int i2, int i3, int i4, int i5, int i6);

    public static final native void yuvToRgbWithResizeForYuv420SP(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, ByteBuffer byteBuffer3, int i, int i2, int i3, int i4, int i5, int i6);

    static {
        System.loadLibrary("ColorConverter");
    }

    public static void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
        int frameSize = width * height;
        int yp = 0;
        for (int j = 0; j < height; j++) {
            int uvp = frameSize + ((j >> 1) * width);
            int u = 0;
            int v = 0;
            int i = 0;
            while (true) {
                int uvp2 = uvp;
                if (i >= width) {
                    break;
                }
                int y = (yuv420sp[yp] & 255) - 16;
                Math.max(y, 0);
                if ((i & 1) == 0) {
                    uvp = uvp2 + 1;
                    v = (yuv420sp[uvp2] & 255) - 128;
                    uvp2 = uvp + 1;
                    u = (yuv420sp[uvp] & 255) - 128;
                }
                uvp = uvp2;
                int y1192 = y * 1192;
                int g = (y1192 - (v * 833)) - (u * 400);
                int b = y1192 + (u * 2066);
                rgb[yp] = ((ViewCompat.MEASURED_STATE_MASK | ((Math.min(Math.max(y1192 + (v * 1634), 0), 262143) << 6) & 16711680)) | ((Math.min(Math.max(g, 0), 262143) >> 2) & 65280)) | ((Math.min(Math.max(b, 0), 262143) >> 10) & 255);
                i++;
                yp++;
            }
        }
    }
}
