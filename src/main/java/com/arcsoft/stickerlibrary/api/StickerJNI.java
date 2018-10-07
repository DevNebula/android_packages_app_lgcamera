package com.arcsoft.stickerlibrary.api;

import android.content.Context;
import com.arcsoft.stickerlibrary.utils.ArcBuff;
import com.arcsoft.stickerlibrary.utils.ArcOffscreen;
import com.arcsoft.stickerlibrary.utils.LogUtil;
import com.arcsoft.stickerlibrary.utils.XMLParser.SuperParams;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class StickerJNI {
    private static final String TAG = StickerJNI.class.getSimpleName();
    private long mGlobalAddress;
    private StickerInfoListener mStickerInfoListener;

    public native int native_Init(int i, int i2, boolean z, int i3, boolean z2);

    public native int native_SetDefaultBeautyParam(long j, int i, int i2);

    public native int native_SetFunnyType(long j, int i);

    public native int native_SetTemplate(long j, ArrayList<SuperParams> arrayList);

    public native long native_getDirectBufferAddress(ByteBuffer byteBuffer);

    public native FaceStatus native_getFaceStatus(long j);

    public native int native_startProcess(long j, ArcBuff arcBuff, int i);

    public native int native_startProcess(long j, ArcOffscreen arcOffscreen, int i);

    public native int native_startRender(long j);

    public native int native_uninit(long j);

    static {
        System.loadLibrary("mpbase");
        System.loadLibrary("ArcSoftSpotlight");
        System.loadLibrary("ArcStickerEngine");
    }

    public StickerJNI(Context context) {
        this.mStickerInfoListener = null;
        this.mGlobalAddress = 0;
        this.mStickerInfoListener = null;
    }

    public int init(int width, int height, boolean bMirror, int degree, StickerInfoListener infoCallback) {
        this.mStickerInfoListener = infoCallback;
        int ret = native_Init(width, height, bMirror, degree, this.mStickerInfoListener != null);
        LogUtil.LogD(TAG, "init handle=" + this.mGlobalAddress);
        return ret;
    }

    public int setTemplate(ArrayList<SuperParams> xmlConfigList) {
        LogUtil.LogD(TAG, "setTemplate handle=" + this.mGlobalAddress);
        if (0 == this.mGlobalAddress) {
            return 5;
        }
        return native_SetTemplate(this.mGlobalAddress, xmlConfigList);
    }

    public int setFunnyType(int type) {
        return native_SetFunnyType(this.mGlobalAddress, type);
    }

    private void onSetTemplateProcessUpdate(int percent) {
        if (this.mStickerInfoListener != null) {
            this.mStickerInfoListener.OnInfoListener(33, Integer.valueOf(percent));
        }
    }

    public int startProcess(ArcBuff frameBuff, int orientationForFaceDetection) {
        return native_startProcess(this.mGlobalAddress, frameBuff, orientationForFaceDetection);
    }

    public int startProcess(ArcOffscreen frame, int orientationForFaceDetection) {
        if (0 == this.mGlobalAddress) {
            return 5;
        }
        return native_startProcess(this.mGlobalAddress, frame, orientationForFaceDetection);
    }

    public int startRender() {
        return native_startRender(this.mGlobalAddress);
    }

    public int setDefaultBeautyParameter(int beautySkinSoften, int beautySkinBright) {
        return native_SetDefaultBeautyParam(this.mGlobalAddress, beautySkinSoften, beautySkinBright);
    }

    public FaceStatus getFaceStatus() {
        return native_getFaceStatus(this.mGlobalAddress);
    }

    public int uninit() {
        int ret = native_uninit(this.mGlobalAddress);
        this.mGlobalAddress = 0;
        return ret;
    }

    public long getDirectBufferAddress(ByteBuffer directBuffer) {
        if (directBuffer.isDirect()) {
            return native_getDirectBufferAddress(directBuffer);
        }
        return 0;
    }
}
