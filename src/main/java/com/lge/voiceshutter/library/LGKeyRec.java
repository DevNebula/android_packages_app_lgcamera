package com.lge.voiceshutter.library;

import android.util.Log;

public final class LGKeyRec {
    public static final int EVENT_INCOMPLETE = 2;
    public static final int EVENT_INVALID = 0;
    public static final int EVENT_MAX_SPEECH = 9;
    public static final int EVENT_NEED_MORE_AUDIO = 8;
    public static final int EVENT_NO_MATCH = 1;
    public static final int EVENT_RECOGNITION_RESULT = 6;
    public static final int EVENT_RECOGNITION_TIMEOUT = 7;
    public static final int EVENT_STARTED = 3;
    public static final int EVENT_START_OF_VOICING = 5;
    public static final int EVENT_STOPPED = 4;
    private static final int STATUS_CREATED = 1;
    private static final int STATUS_INITIALIZED = 2;
    private static final int STATUS_NULL = 0;
    private static final int STATUS_STARTED = 3;
    private static final int STATUS_STOPPED = 4;
    public static final String TAG = "LGKeyRec";
    private static long sLGKhandle = 0;
    private int LGK_status = 0;

    private native int LGKAdvance(long j);

    private native long LGKCreate(int i, int i2);

    private native void LGKDestroy(long j);

    private native byte[][] LGKGetEnrolledKeywords(long j);

    private native int LGKGetLength(long j);

    private native byte[] LGKGetResult(long j);

    private native void LGKInitialize(long j);

    private native String LGKLibraryVersion();

    private native void LGKPutAudio(long j, byte[] bArr, int i, int i2, boolean z);

    private native void LGKStart(long j);

    private native void LGKStop(long j);

    static {
        System.loadLibrary("kwr_mvoice-jni_4");
        Log.d(TAG, "loading: libkwr_mvoice-jni.so");
    }

    public static String eventToString(int event) {
        switch (event) {
            case 0:
                return "EVENT_INVALID";
            case 1:
                return "EVENT_NO_MATCH";
            case 2:
                return "EVENT_INCOMPLETE";
            case 3:
                return "EVENT_STARTED";
            case 4:
                return "EVENT_STOPPED";
            case 5:
                return "EVENT_START_OF_VOICING";
            case 6:
                return "EVENT_RECOGNITION_RESULT";
            case 7:
                return "EVENT_RECOGNITION_TIMEOUT";
            case 8:
                return "EVENT_NEED_MORE_AUDIO";
            case 9:
                return "EVENT_MAX_SPEECH";
            default:
                return "EVENT_" + event;
        }
    }

    public LGKeyRec(int language, int sensitivity) {
        sLGKhandle = LGKCreate(language, sensitivity);
        this.LGK_status = 1;
    }

    public void Initialize() {
        if (this.LGK_status != 0) {
            LGKInitialize(sLGKhandle);
            this.LGK_status = 2;
        }
    }

    public void Start() {
        if (this.LGK_status == 4) {
            Initialize();
        }
        if (this.LGK_status == 2) {
            LGKStart(sLGKhandle);
            this.LGK_status = 3;
        }
    }

    public int Advance() {
        if (this.LGK_status == 3) {
            return LGKAdvance(sLGKhandle);
        }
        return 0;
    }

    public void PutAudio(byte[] buf, int offset, int length, boolean isLast) {
        if (this.LGK_status == 3) {
            LGKPutAudio(sLGKhandle, buf, offset, length, isLast);
        }
    }

    public void Stop() {
        if (sLGKhandle != 0 && this.LGK_status == 3) {
            LGKStop(sLGKhandle);
        }
        this.LGK_status = 4;
    }

    public void DestroyRecognizer() {
        Stop();
        if (sLGKhandle != 0) {
            LGKDestroy(sLGKhandle);
            sLGKhandle = 0;
            Log.d(TAG, "LGKDestroy is called !");
        }
        this.LGK_status = 0;
    }

    public int GetKeywordLength() {
        return LGKGetLength(sLGKhandle);
    }

    public String GetKeywordResult() {
        return new String(LGKGetResult(sLGKhandle));
    }

    public String LibraryVersion() {
        return LGKLibraryVersion();
    }

    public String GetEnrolledKeywords() {
        StringBuilder keywords = new StringBuilder();
        for (byte[] str : LGKGetEnrolledKeywords(sLGKhandle)) {
            keywords.append(new String(str));
            keywords.append("\n");
        }
        return keywords.toString();
    }
}
