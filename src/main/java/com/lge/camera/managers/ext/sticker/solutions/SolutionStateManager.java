package com.lge.camera.managers.ext.sticker.solutions;

import com.lge.camera.util.CamLog;

public class SolutionStateManager {
    public static final int STATE_CREATED = 2;
    public static final int STATE_DESTROYED = 6;
    public static final int STATE_END = 7;
    public static final int STATE_INITED = 4;
    public static final int STATE_NONE = 0;
    public static final int STATE_RECORDING_IDLE = 100;
    public static final int STATE_RECORDING_STARTED = 101;
    public static final int STATE_RECORIDNG_PAUSED = 102;
    public static final int STATE_STICKER_DRAWING = 1001;
    public static final int STATE_STICKER_LOADED = 2001;
    public static final int STATE_STICKER_NOT_DRAWING = 1000;
    public static final int STATE_STICKER_NOT_LOADED = 2000;
    public static final int STATE_UNINITED = 3;
    private static final String TAG = "SolutionStateManager";
    private int mCurrentGlThreadState;
    private int mNowRecording;
    private int mStickerDrawingState;
    public int mStickerLoadState;

    public SolutionStateManager() {
        this.mCurrentGlThreadState = 0;
        this.mNowRecording = 100;
        this.mStickerLoadState = 2000;
        this.mStickerDrawingState = 1000;
        this.mCurrentGlThreadState = 0;
        this.mStickerLoadState = 2000;
    }

    public synchronized void setState(int state) {
        if (state < 0 || state > 7) {
            CamLog.m3d(TAG, "");
        }
        this.mStickerDrawingState = 1000;
        this.mCurrentGlThreadState = state;
        CamLog.m3d(TAG, "mStickerDrawingState = " + this.mStickerDrawingState);
    }

    public synchronized int getState() {
        return this.mCurrentGlThreadState;
    }

    public synchronized boolean isCreated() {
        boolean z;
        z = this.mCurrentGlThreadState >= 2 && this.mCurrentGlThreadState <= 4;
        return z;
    }

    public synchronized boolean isCanInit() {
        boolean z;
        z = this.mCurrentGlThreadState >= 2 && this.mCurrentGlThreadState < 4;
        return z;
    }

    public synchronized boolean isInited() {
        return this.mCurrentGlThreadState == 4;
    }

    public synchronized boolean isStickerDrawing() {
        return this.mStickerDrawingState == 1001;
    }

    public synchronized void setStickerDrawing(boolean drawing) {
        if (!drawing) {
            this.mStickerDrawingState = 1000;
        } else if (isInited()) {
            this.mStickerDrawingState = 1001;
        } else {
            throw new RuntimeException("Wrong state change");
        }
        CamLog.m3d(TAG, "setStickerDrawing mStickerDrawingState = " + this.mStickerDrawingState);
    }

    public synchronized void setRecordingStarted() {
        if (isInited()) {
            this.mNowRecording = 101;
        } else {
            this.mNowRecording = 100;
        }
    }

    public synchronized void setRecordingPauseResume(int state) {
        if (isInited() && this.mNowRecording == 101 && state == 102) {
            this.mNowRecording = 102;
        } else if (isInited() && this.mNowRecording == 102 && state == 101) {
            this.mNowRecording = 101;
        }
    }

    public synchronized void setRecordingFinished() {
        this.mNowRecording = 100;
    }

    public synchronized boolean isNowRecording() {
        return this.mNowRecording != 100;
    }

    public synchronized boolean isPauseRecording() {
        return this.mNowRecording == 102;
    }

    public synchronized void setStickerLoadState(boolean loaded) {
        if (loaded) {
            this.mStickerLoadState = STATE_STICKER_LOADED;
        } else {
            this.mStickerLoadState = 2000;
        }
    }

    public synchronized boolean isStickerLoaded() {
        return this.mStickerLoadState == STATE_STICKER_LOADED;
    }
}
