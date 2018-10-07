package com.lge.camera.managers.ext.sticker.solutions;

import android.graphics.Bitmap;
import android.media.Image;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import com.lge.camera.managers.ext.sticker.StickerInformationDataClass;

public abstract class SolutionBase implements Renderer {
    public static final int INFO_FACE_ACTION_DONE = 101;
    public static final int INFO_FACE_COUNT_CHANGED = 100;
    public static final int INFO_SET_FACE_COUNT = 103;
    public static final int INFO_STICKER_CHANGE = 102;
    public static final int INFO_STICKER_CHANGE_COMPLETED = 104;
    public static final long MAX_FILE_SIZE = -1;
    private static final int MOTION_AVAILABLE_COUNT = 5;
    public static final int MOTION_EYE_BLINK = 1;
    public static final int MOTION_EYE_BROW_RAISE = 2;
    public static final int MOTION_HEAD_NOD = 3;
    public static final int MOTION_HEAD_SHAKE = 4;
    public static final int MOTION_OPEN_MOUTH = 0;
    public static final int SOLUTION_ARC = 2;
    public static final int SOLUTION_LOADER_RECENT = 4;
    public static final int SOLUTION_LOLLICAM = 1;
    public static final int SOLUTION_OTHER = 3;
    public static final String VIDEO_EXTENTION = ".mp4";
    public String[] mActionStringList;
    protected ContentsTakenCallback mContentsTakenCallback;
    protected InfoListener mInfoListener;
    protected int mPhoneOrientationDegree;
    protected SolutionStateManager mStateManager;

    public interface ContentsTakenCallback {
        void onContentTaken(ContentsInformation contentsInformation);
    }

    public interface InfoListener {
        void onInfoListener(int i, Object obj);
    }

    public abstract void init();

    public abstract boolean isInited();

    public abstract boolean isRecording();

    public abstract boolean isStickerDrawing();

    public abstract boolean isStickerLoadCompleted();

    public abstract void pauseRecording();

    public abstract void process(Image image, int i);

    public abstract void process(byte[] bArr, int i);

    public abstract void resumeRecording();

    protected abstract void setActionStringList();

    public abstract void setDrawingInformation(int[] iArr, int[] iArr2, int i, int i2, boolean z);

    public abstract void setSticker(StickerInformationDataClass stickerInformationDataClass);

    public abstract void startRecording(String str, String str2, long j, long j2);

    public abstract ContentsInformation stopRecording(GLSurfaceView gLSurfaceView);

    public abstract void takePicture(int i, int i2, boolean z, Bitmap bitmap);

    public abstract int type();

    public abstract void uninit();

    public SolutionBase() {
        this.mStateManager = null;
        this.mActionStringList = new String[5];
        this.mPhoneOrientationDegree = 0;
        this.mStateManager = new SolutionStateManager();
    }

    public void setContentsTakenCallback(ContentsTakenCallback callback) {
        this.mContentsTakenCallback = callback;
    }

    public void setInfoListener(InfoListener listener) {
        this.mInfoListener = listener;
    }

    public String[] getActionStringList() {
        return this.mActionStringList;
    }

    public void clearActionStringList() {
        for (int i = 0; i < 5; i++) {
            this.mActionStringList[i] = null;
        }
    }

    public void phoneOrientationDegree(int degree) {
        this.mPhoneOrientationDegree = degree;
    }
}
