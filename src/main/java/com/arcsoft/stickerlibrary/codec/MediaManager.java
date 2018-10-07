package com.arcsoft.stickerlibrary.codec;

import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.support.annotation.NonNull;
import android.view.Surface;
import com.arcsoft.stickerlibrary.api.StickerRecordingListener;
import com.arcsoft.stickerlibrary.codec.p004gl.EGLWrapper;
import com.arcsoft.stickerlibrary.codec.p004gl.GLRender;
import com.arcsoft.stickerlibrary.sticker.StickerMessage;
import com.arcsoft.stickerlibrary.utils.LogUtil;

public class MediaManager implements StickerRecordingListener {
    public static final int MUXER_AUDIO_ENCODER = 1;
    public static final int MUXER_VIDEO_AND_AUDIO_ENCODER = 2;
    public static final int MUXER_VIDEO_ENCODER = 1;
    private static final String TAG = "Arc_VideoEncoder";
    private CodecLog CodecLog;
    private BaseEncoder mAudioEncoder;
    private EGLWrapper mEGLWrapper;
    private int mFrameHeight;
    private long mFrameIndex = 0;
    private int mFrameWidth;
    private GLRender mGLRender;
    private String mImageDirectory;
    private int mInitedEncoderCount;
    private boolean mIsInited;
    private boolean mIsMirror;
    private boolean mIsUseGL;
    private int mMaxEncoderCount;
    private MuxerWrapper mMuxer;
    private int mOrientation;
    private Object mPauseMonitor;
    private EGLContext mSharedContext;
    private StickerRecordingListener mStickerRecordingListener;
    private BaseEncoder mVideoEncoder;
    private String mVideoPath;

    public MediaManager(@NonNull String videopath, int frameWidth, int frameHeight, int oritentaion, boolean isMirror, int screentOrientation, StickerRecordingListener listener) {
        this.mStickerRecordingListener = listener;
        this.mFrameWidth = frameWidth;
        this.mFrameHeight = frameHeight;
        if (90 == oritentaion || 270 == oritentaion) {
            this.mFrameWidth ^= this.mFrameHeight;
            this.mFrameHeight = this.mFrameWidth ^ this.mFrameHeight;
            this.mFrameWidth ^= this.mFrameHeight;
        }
        this.mOrientation = oritentaion;
        this.mIsMirror = isMirror;
        this.mVideoPath = videopath;
        this.mSharedContext = EGL14.EGL_NO_CONTEXT;
        this.mMaxEncoderCount = 0;
        this.mInitedEncoderCount = 0;
        this.mMuxer = new MuxerWrapper(this.mVideoPath, screentOrientation, this);
        this.mPauseMonitor = new Object();
        LogUtil.LogD(TAG, "MediaManager constructor mFrameWidth = " + frameWidth + " ,mFrameHeight = " + frameHeight);
        CodecLog codecLog = this.CodecLog;
        CodecLog.m42e(TAG, "MediaManager constructor mFrameWidth = " + frameWidth + " ,mFrameHeight = " + frameHeight);
    }

    public void initVideoEncoder() {
        CodecLog codecLog = this.CodecLog;
        CodecLog.m42e(TAG, "MediaManager initVideoEncoder in");
        this.mVideoEncoder = new VideoEncoder(this.mMuxer, this.mFrameWidth, this.mFrameHeight, this.mPauseMonitor, this);
        this.mVideoEncoder.prepare(false);
        this.mIsInited = true;
        this.mInitedEncoderCount++;
        checkEncoderCount();
        codecLog = this.CodecLog;
        CodecLog.m42e(TAG, "MediaManager initVideoEncoder out mInitedEncoderCount = " + this.mInitedEncoderCount);
    }

    public void initVideoEncoderWithSharedContext(EGLContext sharedContext) {
        CodecLog codecLog = this.CodecLog;
        CodecLog.m41d(TAG, "MediaManager initVideoEncoderWithSharedContext in");
        this.mSharedContext = sharedContext;
        this.mVideoEncoder = new VideoEncoder(this.mMuxer, this.mFrameWidth, this.mFrameHeight, this.mPauseMonitor, this);
        this.mVideoEncoder.prepare(true);
        this.mIsUseGL = true;
        if (this.mIsUseGL) {
            Surface surf = this.mVideoEncoder.getInputSurface();
            if (surf != null) {
                this.mEGLWrapper = new EGLWrapper(surf, this.mSharedContext);
                this.mGLRender = new GLRender(this.mFrameWidth, this.mFrameHeight, this.mOrientation, this.mIsMirror);
                this.mGLRender.initRender();
            } else {
                codecLog = this.CodecLog;
                CodecLog.m42e(TAG, "initVideoEncoder()->getInputSurface null.");
                if (this.mStickerRecordingListener != null) {
                    this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO_CONFIGURE, Integer.valueOf(0));
                }
            }
        }
        this.mInitedEncoderCount++;
        checkEncoderCount();
        codecLog = this.CodecLog;
        CodecLog.m42e(TAG, "MediaManager initVideoEncoderWithSharedContext out mInitedEncoderCount = " + this.mInitedEncoderCount);
    }

    public void initAudioEncoder() {
        this.mAudioEncoder = new AudioEncoder(this.mMuxer, this.mPauseMonitor, this);
        this.mAudioEncoder.prepare(false);
        this.mInitedEncoderCount++;
        checkEncoderCount();
    }

    public void setEncoderCount(int count) {
        if (this.mMuxer != null) {
            this.mMuxer.setEncoderCount(count);
        }
        this.mMaxEncoderCount = count;
    }

    private void checkEncoderCount() {
        if (this.mMaxEncoderCount == this.mInitedEncoderCount) {
            this.mIsInited = true;
        } else if (this.mInitedEncoderCount >= 3) {
            throw new RuntimeException("Init encoder count great than need. need=" + this.mMaxEncoderCount + " ,but got=" + this.mInitedEncoderCount);
        }
    }

    public void startRecording() {
        if (!this.mIsInited || this.mMuxer == null) {
            throw new RuntimeException("Unit Encoder or Muxer is null.");
        }
        CodecLog codecLog;
        if (this.mVideoEncoder != null) {
            this.mVideoEncoder.startRecording();
        } else {
            codecLog = this.CodecLog;
            CodecLog.m43i(TAG, "startRecording()-> VideoEncoder is null. maxEncoderCount=" + this.mMaxEncoderCount);
        }
        if (this.mAudioEncoder != null) {
            this.mAudioEncoder.startRecording();
            return;
        }
        codecLog = this.CodecLog;
        CodecLog.m43i(TAG, "startRecording()-> AudioEncoder is null. maxEncoderCount=" + this.mMaxEncoderCount);
    }

    public int pauseRecording() {
        if (this.mAudioEncoder != null) {
            this.mAudioEncoder.pauseRecording();
        }
        if (this.mVideoEncoder != null) {
            this.mVideoEncoder.pauseRecording();
        }
        return 0;
    }

    public int resumeRecording() {
        synchronized (this.mPauseMonitor) {
            if (this.mAudioEncoder != null) {
                this.mAudioEncoder.resumeRecording();
            }
            if (this.mVideoEncoder != null) {
                this.mVideoEncoder.resumeRecording();
            }
            this.mPauseMonitor.notifyAll();
        }
        return 0;
    }

    public void stopRecording() {
        synchronized (this.mPauseMonitor) {
            this.mPauseMonitor.notifyAll();
        }
        if (this.mVideoEncoder != null) {
            this.mVideoEncoder.stopRecording();
            this.mVideoEncoder.release(true);
            this.mVideoEncoder = null;
        }
        if (this.mAudioEncoder != null) {
            this.mAudioEncoder.stopRecording();
            this.mAudioEncoder.release(false);
            this.mAudioEncoder = null;
        }
        if (this.mEGLWrapper != null) {
            this.mEGLWrapper.release();
            this.mEGLWrapper = null;
        }
        if (this.mGLRender != null) {
            this.mGLRender.unInitRender();
            this.mGLRender = null;
        }
        this.mMuxer = null;
        this.mPauseMonitor = null;
    }

    public void drawSurfaceWithTextureId(int textureId) {
        CodecLog codecLog;
        if (!this.mIsInited) {
            codecLog = this.CodecLog;
            CodecLog.m42e(TAG, "drawSurfaceWithTextureId()-> MediaManager has not been initialized.");
        } else if (textureId <= 0) {
            throw new IllegalArgumentException("textureId must >0 , your textureId=" + textureId);
        } else if (this.mGLRender == null) {
            throw new RuntimeException("Could not call drawSurfaceWithTextureId() in with a null GLRender.");
        } else {
            codecLog = this.CodecLog;
            CodecLog.m41d(TAG, "drawSurfaceWithTextureId()->A-");
            this.mGLRender.renderWithTextureId(textureId);
            codecLog = this.CodecLog;
            CodecLog.m41d(TAG, "drawSurfaceWithTextureId()->A");
            this.mFrameIndex++;
            swapBuffers();
            codecLog = this.CodecLog;
            CodecLog.m41d(TAG, "drawSurfaceWithTextureId()->B");
            this.mVideoEncoder.notifyNewFrameAvailable();
            codecLog = this.CodecLog;
            CodecLog.m41d(TAG, "drawSurfaceWithTextureId()->C");
        }
    }

    public boolean makeCurrent() {
        if (this.mEGLWrapper != null && this.mIsUseGL) {
            return this.mEGLWrapper.makeCurrent();
        }
        CodecLog codecLog = this.CodecLog;
        CodecLog.m42e(TAG, "You can not call makeCurrent() in a null mEGLWrapper");
        return false;
    }

    public long getMuxerTimeElapsed() {
        if (this.mMuxer == null) {
            return 0;
        }
        return this.mMuxer.getTimeElapse();
    }

    public long getMuxerSizeRecorded() {
        if (this.mMuxer == null) {
            return 0;
        }
        return this.mMuxer.getSizeRecordFile();
    }

    private boolean swapBuffers() {
        CodecLog codecLog = this.CodecLog;
        CodecLog.m41d(TAG, "swapBuffers()->in");
        if (this.mEGLWrapper == null || !this.mIsUseGL) {
            codecLog = this.CodecLog;
            CodecLog.m42e(TAG, "You can not call swapBuffers() in a null mEGLWrapper");
            return false;
        }
        codecLog = this.CodecLog;
        CodecLog.m41d(TAG, "swapBuffers()->out");
        return this.mEGLWrapper.swapBuffers();
    }

    public void onRecordingListener(int msg, Object value) {
        CodecLog codecLog = this.CodecLog;
        CodecLog.m41d(TAG, "onRecordingListener()->in msg = " + msg + " ,value = " + ((Integer) value));
        if (!false) {
            switch (msg) {
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_AUDIO_CREATE /*545*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_AUDIO;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_AUDIO_CONFIGURE /*546*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_AUDIO;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_AUDIO_START /*547*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_AUDIO;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_AUDIO_STOP /*548*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_AUDIO;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_AUDIO_RELEASE /*549*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_AUDIO;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO_CREATE /*561*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO_CONFIGURE /*562*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO_START /*563*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO_STOP /*564*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO_RELEASE /*565*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER_CREATE /*609*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER_ADD_TRACK /*610*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER_START /*611*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER_WRITE_SAMPLE_DATA /*612*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER_STOP /*613*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER_RELEASE /*614*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_AUDIO_RECORD_CREATE /*625*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_AUDIO_RECORD;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_AUDIO_RECORD_START_RECORDING /*626*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_AUDIO_RECORD;
                    break;
                case StickerMessage.MSG_MEDIA_RECORDER_ERROR_AUDIO_RECORD_STOP /*627*/:
                    msg = StickerMessage.MSG_MEDIA_RECORDER_ERROR_AUDIO_RECORD;
                    break;
            }
        }
        if (this.mStickerRecordingListener != null) {
            this.mStickerRecordingListener.onRecordingListener(msg, value);
        }
        codecLog = this.CodecLog;
        CodecLog.m41d(TAG, "onRecordingListener()->out");
    }
}
