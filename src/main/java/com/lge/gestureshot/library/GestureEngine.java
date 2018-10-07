package com.lge.gestureshot.library;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class GestureEngine {
    public static final int GESTURE_ENGINE_CAPTURE_EVENT = 2;
    private static final int GESTURE_ENGINE_CAPTURE_MSG = 3;
    public static final int GESTURE_ENGINE_CLEAN_EVENT = 4;
    private static final int GESTURE_ENGINE_CLEAN_MSG = 5;
    public static final int GESTURE_ENGINE_DETMODE_MULTI = 2;
    public static final int GESTURE_ENGINE_DETMODE_NONE = 0;
    public static final int GESTURE_ENGINE_DETMODE_SINGLE = 1;
    private static final int GESTURE_ENGINE_DRAW_MSG = 2;
    private static final int GESTURE_ENGINE_ERROR_MSG = 1;
    public static final int GESTURE_ENGINE_INTERVALSHOT_EVENT = 3;
    private static final int GESTURE_ENGINE_INTERVALSHOT_MSG = 4;
    public static final int GESTURE_ENGINE_READY_EVENT = 1;
    public static final int GESTURE_ENGINE_STATUS_CREATED = 1;
    public static final int GESTURE_ENGINE_STATUS_INITIALIZED = 2;
    public static final int GESTURE_ENGINE_STATUS_NULL = 0;
    public static final int GESTURE_ENGINE_STATUS_STARTED = 3;
    public static final int GESTURE_ENGINE_STATUS_STOPPED = 4;
    private static List<GestureCallBack> sGestureCallBackList = new ArrayList();
    private static Handler sHandler = new C14431();

    public interface GestureCallBack {
        void onGestureEngineDrawCallback(HandInfo handInfo);

        void onGestureEngineErrorCallback(int i);

        void onGestureEngineEventCallback(int i);
    }

    /* renamed from: com.lge.gestureshot.library.GestureEngine$1 */
    static class C14431 extends Handler {
        C14431() {
        }

        public void handleMessage(Message msg) {
            HandInfo handInfo = msg.obj;
            switch (msg.what) {
                case 2:
                    for (GestureCallBack gestureCallBack : GestureEngine.sGestureCallBackList) {
                        gestureCallBack.onGestureEngineDrawCallback(handInfo);
                    }
                    return;
                case 3:
                    for (GestureCallBack gestureCallBack2 : GestureEngine.sGestureCallBackList) {
                        gestureCallBack2.onGestureEngineEventCallback(2);
                    }
                    return;
                case 4:
                    for (GestureCallBack gestureCallBack22 : GestureEngine.sGestureCallBackList) {
                        gestureCallBack22.onGestureEngineEventCallback(3);
                    }
                    return;
                case 5:
                    for (GestureCallBack gestureCallBack222 : GestureEngine.sGestureCallBackList) {
                        gestureCallBack222.onGestureEngineEventCallback(4);
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public static native int GestureCreate();

    public static native int GesturePutPreviewFrame(byte[] bArr, int i, int i2, int i3, int i4);

    public static native int GesturePutPreviewFrameByteBufferNV21(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3, int i4, int i5);

    public static native int GestureRelease();

    public static native int GestureStart();

    public static native int GestureStop();

    public static native int GetEngineStatus();

    public static native int SetGestureMode(int i, int i2, float f);

    public static native int create(int i, int i2);

    public static native int find(byte[] bArr, int i, int i2, int i3, int i4);

    public static native HandInfo[] getHandInfo();

    public static native int motionInitialize();

    public static native int motionRelease();

    public static native int motionReset();

    public static native int motionResumePush();

    public static native int motionUpdate(float[] fArr);

    public static native int release();

    public static native int resetHandInfo();

    static {
        try {
            System.loadLibrary("gesture-jni");
            sGestureCallBackList.clear();
        } catch (UnsatisfiedLinkError e) {
            Log.e("GestureEngine", "can't loadLibrary \r\n" + e.getMessage());
        }
    }

    protected void finalize() {
        try {
            GestureRelease();
            try {
                super.finalize();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } catch (Throwable e2) {
            e2.printStackTrace();
        }
    }

    public static void AddCallBack(GestureCallBack gestureCallBack) {
        sGestureCallBackList.add(gestureCallBack);
    }

    public static void ClearCallBack() {
        sGestureCallBackList.clear();
    }

    public static void GestureCallBack(HandInfo handInfo) {
        if (sHandler != null) {
            switch (handInfo.mEvent) {
                case 0:
                    Message msg0 = sHandler.obtainMessage();
                    msg0.obj = handInfo;
                    msg0.what = 2;
                    sHandler.sendMessage(msg0);
                    return;
                case 1:
                    Message msg1 = sHandler.obtainMessage();
                    msg1.obj = handInfo;
                    msg1.what = 3;
                    sHandler.sendMessage(msg1);
                    return;
                case 2:
                    Message msg2 = sHandler.obtainMessage();
                    msg2.obj = handInfo;
                    msg2.what = 5;
                    sHandler.sendMessage(msg2);
                    return;
                case 3:
                    Message msg3 = sHandler.obtainMessage();
                    msg3.obj = handInfo;
                    msg3.what = 4;
                    sHandler.sendMessage(msg3);
                    return;
                default:
                    return;
            }
        }
    }
}
