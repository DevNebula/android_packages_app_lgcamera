package com.lge.camera.managers;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.Image;
import android.media.Image.Plane;
import android.net.Uri;
import android.support.p000v4.view.ViewCompat;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.file.FileManager;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CameraTypeface;
import com.lge.camera.util.IntentBroadcastUtil;
import com.lge.camera.util.Utils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class SignatureManager {
    public static final int POST_DELAY = 1500;
    private final int ALPHA_THRESHOLD = 80;
    private final float FONT_RATIO = 0.05f;
    private int[] mAlphaImage;
    private Object mBitmapLock = new Object();
    protected SignatureManagerInterface mGet;
    private Object mInitLock = new Object();
    private boolean mIsSignatureInit = false;
    private float mPaddingRatio = 1.0f;
    private Bitmap mSignatureBitmap = null;
    private String mSignatureFont = null;
    private String mSignatureText = null;
    private byte[] mYuvImage;

    /* renamed from: com.lge.camera.managers.SignatureManager$1 */
    class C11571 implements Runnable {
        C11571() {
        }

        public void run() {
            SignatureManager.this.initSignatureContent();
        }
    }

    public SignatureManager(SignatureManagerInterface signatureManagerInterface) {
        this.mGet = signatureManagerInterface;
    }

    public void init() {
        this.mPaddingRatio = ((float) Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.signature_text_padding)) / ((float) Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.signature_text_size));
        initSignatureByThread();
    }

    public void initSignatureByThread() {
        if (!this.mIsSignatureInit) {
            Thread thread = new Thread(new C11571());
            if (thread != null) {
                thread.start();
            }
        }
    }

    public void initSignatureContent() {
        try {
            synchronized (this.mInitLock) {
                if (this.mIsSignatureInit) {
                    return;
                }
                CamLog.m3d(CameraConstants.TAG, "- Signature - initSignature [START]");
                this.mSignatureText = getSignatureContent(this.mGet.getAppContext(), CameraConstants.URI_SIGNATURE_TEXT);
                this.mSignatureFont = getSignatureContent(this.mGet.getAppContext(), CameraConstants.URI_SIGNATURE_FONT);
                onUpdateSignature();
                this.mIsSignatureInit = true;
                CamLog.m3d(CameraConstants.TAG, "- Signature - initSignature [END]");
            }
        } catch (Exception e) {
            CamLog.m5e(CameraConstants.TAG, e.getMessage());
        }
    }

    public void composeSignatureImage(Image yuvPictureImage, int degree) {
        boolean isNeedFlip = true;
        if (yuvPictureImage != null) {
            CamLog.m3d(CameraConstants.TAG, String.format("-Signature- yuv capture, composeSignatureImage, width = %d, height = %d, degree = %d", new Object[]{Integer.valueOf(yuvPictureImage.getWidth()), Integer.valueOf(yuvPictureImage.getHeight()), Integer.valueOf(degree)}));
            if (CameraDeviceUtils.isRearCamera(this.mGet.getCameraId()) || !"off".equals(this.mGet.getCurSettingValue(Setting.KEY_SAVE_DIRECTION))) {
                isNeedFlip = false;
            }
            Bitmap signatureBitmap = getSignatureBitmap(yuvPictureImage.getWidth(), yuvPictureImage.getHeight(), degree, isNeedFlip);
            if (signatureBitmap != null) {
                convertBitmapToYUVFormat(signatureBitmap);
                if (this.mYuvImage != null && this.mAlphaImage != null) {
                    addSignatureImage(signatureBitmap, yuvPictureImage, degree, isNeedFlip);
                    this.mYuvImage = null;
                    this.mAlphaImage = null;
                    if (signatureBitmap != null && !signatureBitmap.isRecycled()) {
                        signatureBitmap.recycle();
                    }
                }
            }
        }
    }

    private void addSignatureImage(Bitmap signatureBitmap, Image yuvPictureImage, int degree, boolean isNeedFlip) {
        if (signatureBitmap != null && yuvPictureImage != null && this.mYuvImage != null && this.mAlphaImage != null) {
            Plane yPlane = yuvPictureImage.getPlanes()[0];
            Plane vPlane = yuvPictureImage.getPlanes()[2];
            byte[] yBuffer = new byte[yPlane.getBuffer().capacity()];
            byte[] vBuffer = new byte[vPlane.getBuffer().capacity()];
            yPlane.getBuffer().get(yBuffer, 0, yBuffer.length);
            vPlane.getBuffer().get(vBuffer, 0, vBuffer.length);
            int pictureWidth = yuvPictureImage.getWidth();
            int pictureHeight = yuvPictureImage.getHeight();
            int signatureWidth = signatureBitmap.getWidth();
            int signatureHeight = signatureBitmap.getHeight();
            int yStride = yPlane.getRowStride();
            int uvStride = vPlane.getRowStride();
            Point signaturePoint = getSignatureStartPoint(pictureWidth, pictureHeight, signatureWidth, signatureHeight, degree, isNeedFlip);
            if (signaturePoint != null) {
                int uvStartIndex = signatureWidth * signatureHeight;
                int alphaIndex = 0;
                int r = 0;
                while (r < signatureHeight) {
                    int c = 0;
                    int alphaIndex2 = alphaIndex;
                    while (c < signatureWidth) {
                        alphaIndex = alphaIndex2 + 1;
                        if (this.mAlphaImage[alphaIndex2] >= 80) {
                            int y = r + signaturePoint.y;
                            int x = c + signaturePoint.x;
                            yBuffer[(y * yStride) + x] = this.mYuvImage[(r * signatureWidth) + c];
                            vBuffer[((y >> 1) * uvStride) + ((x >> 1) * 2)] = this.mYuvImage[((c >> 1) * 2) + uvStartIndex];
                            vBuffer[(((y >> 1) * uvStride) + ((x >> 1) * 2)) + 1] = this.mYuvImage[(((c >> 1) * 2) + uvStartIndex) + 1];
                        }
                        c++;
                        alphaIndex2 = alphaIndex;
                    }
                    if (r % 2 == 0) {
                        uvStartIndex += signatureWidth;
                    }
                    r++;
                    alphaIndex = alphaIndex2;
                }
                yPlane.getBuffer().clear();
                yPlane.getBuffer().put(yBuffer, 0, yBuffer.length);
                vPlane.getBuffer().clear();
                vPlane.getBuffer().put(vBuffer, 0, vBuffer.length);
                Object yBuffer2 = null;
                yPlane = null;
            } else if (signatureBitmap != null && !signatureBitmap.isRecycled()) {
                signatureBitmap.recycle();
            }
        }
    }

    private void convertBitmapToYUVFormat(Bitmap inputBitmap) {
        if (inputBitmap != null) {
            int width = inputBitmap.getWidth();
            int height = inputBitmap.getHeight();
            this.mYuvImage = new byte[((width * height) * 3)];
            this.mAlphaImage = new int[(width * height)];
            Arrays.fill(this.mYuvImage, (byte) 0);
            Arrays.fill(this.mAlphaImage, 0);
            int[] input = new int[(width * height)];
            inputBitmap.getPixels(input, 0, width, 0, 0, width, height);
            int index = 0;
            int yIndex = 0;
            int uvIndex = width * height;
            int alphaIndex = 0;
            int i = 0;
            while (i < height) {
                int alphaIndex2;
                int uvIndex2;
                int yIndex2;
                int j = 0;
                while (true) {
                    alphaIndex2 = alphaIndex;
                    uvIndex2 = uvIndex;
                    yIndex2 = yIndex;
                    if (j >= width) {
                        break;
                    }
                    int rValue = (input[index] & 16711680) >> 16;
                    int gValue = (input[index] & 65280) >> 8;
                    int bValue = (input[index] & 255) >> 0;
                    alphaIndex = alphaIndex2 + 1;
                    this.mAlphaImage[alphaIndex2] = ((input[index] & ViewCompat.MEASURED_STATE_MASK) >> 24) & 255;
                    int yValue = (((((rValue * 66) + (gValue * 129)) + (bValue * 25)) + 128) >> 8) + 16;
                    int uValue = (((((rValue * -38) - (gValue * 74)) + (bValue * 112)) + 128) >> 8) + 128;
                    int vValue = (((((rValue * 112) - (gValue * 94)) - (bValue * 18)) + 128) >> 8) + 128;
                    byte[] bArr = this.mYuvImage;
                    yIndex = yIndex2 + 1;
                    int i2 = yValue < 0 ? 0 : yValue > 255 ? 255 : yValue;
                    bArr[yIndex2] = (byte) i2;
                    if (i % 2 == 0 && index % 2 == 0) {
                        bArr = this.mYuvImage;
                        uvIndex = uvIndex2 + 1;
                        i2 = vValue < 0 ? 0 : vValue > 255 ? 255 : vValue;
                        bArr[uvIndex2] = (byte) i2;
                        bArr = this.mYuvImage;
                        uvIndex2 = uvIndex + 1;
                        i2 = uValue < 0 ? 0 : uValue > 255 ? 255 : uValue;
                        bArr[uvIndex] = (byte) i2;
                    }
                    uvIndex = uvIndex2;
                    index++;
                    j++;
                }
                i++;
                alphaIndex = alphaIndex2;
                uvIndex = uvIndex2;
                yIndex = yIndex2;
            }
        }
    }

    public Bitmap composeSignatureImage(Bitmap originalBitmap, int degree) {
        if (originalBitmap == null) {
            return null;
        }
        initSignatureContent();
        if (this.mSignatureText == null || this.mSignatureFont == null) {
            return originalBitmap;
        }
        CamLog.m3d(CameraConstants.TAG, String.format("- Signature - composeSignatureImage, width = %d, height = %d, degree = %d", new Object[]{Integer.valueOf(originalBitmap.getWidth()), Integer.valueOf(originalBitmap.getHeight()), Integer.valueOf(degree)}));
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();
        Bitmap composedBitmap = Bitmap.createBitmap(originalWidth, originalHeight, originalBitmap.getConfig());
        Canvas composeCanvas = new Canvas(composedBitmap);
        if (composedBitmap == null || composeCanvas == null) {
            return originalBitmap;
        }
        composeCanvas.drawBitmap(originalBitmap, 0.0f, 0.0f, null);
        Bitmap rotatedBitmap = getSignatureBitmap(originalWidth, originalHeight, degree, false);
        if (rotatedBitmap == null) {
            return originalBitmap;
        }
        Point point = getSignatureStartPoint(originalWidth, originalHeight, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), degree, false);
        if (point != null) {
            composeCanvas.drawBitmap(rotatedBitmap, (float) point.x, (float) point.y, null);
            rotatedBitmap.recycle();
            return composedBitmap;
        } else if (rotatedBitmap == null || rotatedBitmap.isRecycled()) {
            return originalBitmap;
        } else {
            rotatedBitmap.recycle();
            return originalBitmap;
        }
    }

    public Point getSignatureStartPoint(int originalWidth, int originalHeight, int signatureWidth, int signatureHeight, int degree, boolean flip) {
        int i = 0;
        Point point = new Point(0, 0);
        switch (degree) {
            case 0:
                if (!flip) {
                    i = originalWidth - signatureWidth;
                }
                point.x = i;
                point.y = originalHeight - signatureHeight;
                return point;
            case 90:
                point.x = originalWidth - signatureWidth;
                if (flip) {
                    i = originalHeight - signatureHeight;
                }
                point.y = i;
                return point;
            case 180:
                if (flip) {
                    i = originalWidth - signatureWidth;
                }
                point.x = i;
                return point;
            case 270:
                if (!flip) {
                    i = originalHeight - signatureHeight;
                }
                point.y = i;
                return point;
            default:
                return null;
        }
    }

    public Bitmap getSignatureBitmap(int oriImageWidth, int oriImageHeight, int degree, boolean flip) {
        Bitmap rotatedBitmap;
        synchronized (this.mBitmapLock) {
            if (this.mSignatureBitmap == null) {
                this.mSignatureBitmap = FileManager.readBitmapFromPath(CameraConstantsEx.SIGNATURE_PATH);
            }
            initSignatureContent();
            if (this.mSignatureBitmap == null || this.mSignatureText == null) {
                rotatedBitmap = null;
            } else {
                int pictureWidth;
                float textSize = getSignatureFontSize(oriImageWidth, oriImageHeight);
                Rect textBound = getTextBound(this.mSignatureText, textSize);
                int padding = getSignaturePadding(oriImageWidth, oriImageHeight);
                int scaledHeight = textBound.height() + (padding * 2);
                int scaledWidth = textBound.width() + (padding * 2);
                if (degree == 0 || degree == 180) {
                    pictureWidth = oriImageWidth;
                } else {
                    pictureWidth = oriImageHeight;
                }
                if (scaledWidth > pictureWidth) {
                    float ratio = ((float) scaledHeight) / ((float) scaledWidth);
                    scaledWidth = pictureWidth;
                    scaledHeight = (int) (((float) scaledWidth) * ratio);
                }
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(this.mSignatureBitmap, scaledWidth - (scaledWidth % 16), scaledHeight - (scaledHeight % 16), true);
                if (scaledBitmap == null) {
                    rotatedBitmap = null;
                } else {
                    rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), getRotatedMatrix(degree, flip), true);
                    scaledBitmap.recycle();
                }
            }
        }
        return rotatedBitmap;
    }

    public Bitmap getSignatureBitmapForSticker(int width, int height) {
        Bitmap scaledBitmap;
        synchronized (this.mBitmapLock) {
            if (this.mSignatureBitmap == null) {
                this.mSignatureBitmap = FileManager.readBitmapFromPath(CameraConstantsEx.SIGNATURE_PATH);
            }
            initSignatureContent();
            if (this.mSignatureBitmap == null || this.mSignatureText == null) {
                scaledBitmap = null;
            } else {
                Rect textBound = getTextBound(this.mSignatureText, getSignatureFontSize(width, height));
                int padding = getSignaturePadding(width, height);
                int scaledHeight = textBound.height() + (padding * 2);
                int scaledWidth = textBound.width() + (padding * 2);
                int pictureWidth = width;
                if (scaledWidth > pictureWidth) {
                    float ratio = ((float) scaledHeight) / ((float) scaledWidth);
                    scaledWidth = pictureWidth;
                    scaledHeight = (int) (((float) scaledWidth) * ratio);
                }
                scaledBitmap = Bitmap.createScaledBitmap(this.mSignatureBitmap, scaledWidth - (scaledWidth % 16), scaledHeight - (scaledHeight % 16), true);
            }
        }
        return scaledBitmap;
    }

    private Matrix getRotatedMatrix(int degree, boolean flip) {
        Matrix matrix = new Matrix();
        switch (degree) {
            case 90:
                matrix.preRotate(270.0f);
                break;
            case 180:
                matrix.preRotate(180.0f);
                break;
            case 270:
                matrix.preRotate(90.0f);
                break;
        }
        if (flip) {
            if (degree == 0 || degree == 180) {
                matrix.postScale(-1.0f, 1.0f);
            } else {
                matrix.postScale(1.0f, -1.0f);
            }
        }
        return matrix;
    }

    private Rect getTextBound(String text, float textSize) {
        Rect rect = new Rect(0, 0, 0, 0);
        Typeface fontType = CameraTypeface.get(this.mGet.getAppContext(), this.mSignatureFont);
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(fontType);
        textPaint.setAntiAlias(true);
        StaticLayout layout = new StaticLayout(text, textPaint, (int) textPaint.measureText(text), Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        rect.set(0, 0, layout.getWidth(), layout.getHeight());
        return rect;
    }

    private float getSignatureFontSize(int width, int height) {
        float size;
        if (width > height * 3) {
            size = ((float) (width - (height * 2))) * 0.05f;
        } else {
            size = ((float) Math.min(width, height)) * 0.05f;
        }
        return Math.min(size, ((float) height) * 0.05f);
    }

    private int getSignaturePadding(int width, int height) {
        return (int) (getSignatureFontSize(width, height) * this.mPaddingRatio);
    }

    public void onPause() {
        releaseSignature();
    }

    private void releaseSignature() {
        synchronized (this.mBitmapLock) {
            if (this.mSignatureBitmap != null) {
                this.mSignatureBitmap.recycle();
                this.mSignatureBitmap = null;
            }
            this.mSignatureText = null;
            this.mSignatureFont = null;
            this.mIsSignatureInit = false;
        }
    }

    public boolean isNeedToStartSignatureActivity(String value) {
        return "on".equals(value) && this.mSignatureText == null;
    }

    public void startSignatureActivity() {
        Intent sigIntent = new Intent();
        sigIntent.setAction(CameraConstants.ACTION_SIGNATURE);
        if (!IntentBroadcastUtil.isIntentAvailable(this.mGet.getActivity().getPackageManager(), sigIntent)) {
            CamLog.m3d(CameraConstants.TAG, "-Signature- signature activity is unavailable, return");
        }
        this.mGet.getActivity().startActivityForResult(sigIntent, 5);
    }

    public void onActivityResult(int resultCode, Intent data) {
        initSignatureContent();
        if (resultCode == -1) {
            CamLog.m3d(CameraConstants.TAG, "Signature -- return RESULT_OK");
            if (!(this.mSignatureText == null || "".equals(this.mSignatureText))) {
                this.mGet.setSetting(Setting.KEY_SIGNATURE, "on", true);
            }
            onUpdateSignature();
        }
    }

    public static String getSignatureContent(Context c, String uriType) {
        Uri uri;
        String content = null;
        if (CameraConstants.URI_SIGNATURE_TEXT.equals(uriType)) {
            uri = Uri.parse(CameraConstants.URI_SIGNATURE_TEXT);
        } else {
            if (CameraConstants.URI_SIGNATURE_FONT.equals(uriType)) {
                uri = Uri.parse(CameraConstants.URI_SIGNATURE_FONT);
            }
            return content;
        }
        Cursor cursor = null;
        content = null;
        try {
            cursor = c.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                content = cursor.getString(cursor.getColumnIndex(CameraConstants.SIGNATURE_VALUE));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            CamLog.m5e(CameraConstants.TAG, e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        CamLog.m3d(CameraConstants.TAG, "Signature content = " + content);
        return content;
    }

    private void onUpdateSignature() {
        String settingValue = this.mGet.getCurSettingValue(Setting.KEY_SIGNATURE);
        String text = this.mSignatureText;
        if (text == null) {
            text = "";
        }
        if ("".equals(text) && "on".equals(settingValue)) {
            this.mGet.setSetting(Setting.KEY_SIGNATURE, "off", true);
        }
        this.mGet.updateGuideTextSettingMenu(Setting.KEY_SIGNATURE, text);
    }

    public boolean isSignatureInitialized() {
        return this.mIsSignatureInit;
    }

    public String getSignatureText() {
        return this.mSignatureText;
    }

    public String getSignatureFont() {
        return this.mSignatureFont;
    }

    public byte[] composeSignatureImage(byte[] jpegData, int degree) {
        Bitmap originalImage = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
        if (originalImage == null) {
            return jpegData;
        }
        Bitmap composedImage = composeSignatureImage(originalImage, degree);
        if (composedImage == null) {
            originalImage.recycle();
            return jpegData;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        composedImage.compress(CompressFormat.JPEG, 100, stream);
        byte[] result = stream.toByteArray();
        originalImage.recycle();
        composedImage.recycle();
        try {
            stream.close();
        } catch (IOException e) {
            CamLog.m3d(CameraConstants.TAG, "ignore exception");
        }
        return result;
    }
}
