package com.arcsoft.stickerlibrary.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;
import com.arcsoft.stickerlibrary.api.StickerRecordingListener;
import com.arcsoft.stickerlibrary.sticker.StickerMessage;
import com.arcsoft.stickerlibrary.utils.LogUtil;
import com.lge.camera.constants.LdbConstants;
import java.io.IOException;

public class VideoEncoder extends BaseEncoder {
    private static final long A_SECOND_IN_NANO_UNITS = 1000000000;
    private static final int DEFAULT_BIT_RATE = 10000000;
    private static final int DEFAULT_FRAME_RATE = 30;
    private static final int DEFAULT_I_FRAME_INTERVAL = 10;
    private static String DEFAULT_MIME_TYPE = "video/avc";
    public static final String ENCODER_THREAD_NAME = "Arc_Video_Encoder";
    private static final String TAG = "Arc_VideoEncoder";
    private Surface mEncodeInputSurface;
    private Thread mEncoderVideoThread = null;
    private int mHeight;
    private MediaFormat mMediaFormat;
    private boolean mMirror;
    private int mOrientation;
    protected long mStartPauseTime;
    private Object mVideoEncoderMonitor = new Object();
    private int mWidth;

    public VideoEncoder(MuxerWrapper muxer, int width, int height, Object pauseMonitor, StickerRecordingListener listener) {
        super(muxer, pauseMonitor, listener);
        this.mWidth = width;
        this.mHeight = height;
        LogUtil.LogD(TAG, "VideoEncoder constructor mWidth = " + width + " ,mHeight = " + height);
    }

    public void prepare(boolean isUseSurface) {
        initVideoEncoder(isUseSurface);
        if (this.mEncoder == null) {
            throw new RuntimeException("Init video encoder is failed.");
        }
    }

    public Surface getInputSurface() {
        if (this.mEncoder != null) {
            return this.mEncodeInputSurface;
        }
        return super.getInputSurface();
    }

    public void startRecording() {
        if (this.mEncoderVideoThread != null) {
            CodecLog.m42e(TAG, "startRecording()-> Video encoder thread has been started already, can not start twice.");
            throw new RuntimeException("Video encoder thread has been started already, can not start twice.");
        }
        super.startRecording();
        this.mEncoderVideoThread = new Thread(ENCODER_THREAD_NAME) {
            public void run() {
                super.run();
                try {
                    VideoEncoder.this.mEncoder.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (VideoEncoder.this.mStickerRecordingListener != null) {
                        VideoEncoder.this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO_START, Integer.valueOf(0));
                    }
                }
                while (!VideoEncoder.this.mIsRequestStop) {
                    synchronized (VideoEncoder.this.mVideoEncoderMonitor) {
                        try {
                            CodecLog.m41d(VideoEncoder.TAG, "Video encoder waiting ...");
                            VideoEncoder.this.mVideoEncoderMonitor.wait();
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                    }
                    CodecLog.m41d(VideoEncoder.TAG, "Video new frame come , to drain()");
                    VideoEncoder.this.drain();
                }
                VideoEncoder.this.mIsEOS = true;
                VideoEncoder.this.mEncoder.signalEndOfInputStream();
                VideoEncoder.this.drain();
            }
        };
        this.mEncoderVideoThread.start();
        CodecLog.m41d(TAG, "VideoEncoder is started.");
    }

    public void notifyNewFrameAvailable() {
        CodecLog.m41d(TAG, "notifyNewFrameAvailable()-> in.");
        synchronized (this.mVideoEncoderMonitor) {
            this.mVideoEncoderMonitor.notifyAll();
        }
        CodecLog.m41d(TAG, "notifyNewFrameAvailable()-> out.");
    }

    public void stopRecording() {
        super.stopRecording();
        synchronized (this.mVideoEncoderMonitor) {
            this.mVideoEncoderMonitor.notifyAll();
        }
    }

    public void pauseRecording() {
        if (!this.mIsRequestPause) {
            this.mIsRequestPause = true;
            this.mStartPauseTime = System.nanoTime();
        }
    }

    public void resumeRecording() {
        if (this.mIsRequestPause) {
            this.mIsRequestPause = false;
            this.mTotalPauseTimePassed += System.nanoTime() - this.mStartPauseTime;
            this.mPauseTimeQueue.add(Long.valueOf(this.mTotalPauseTimePassed));
        }
    }

    public void release(boolean isVideo) {
        String threadName = ENCODER_THREAD_NAME;
        if (this.mEncoderVideoThread != null) {
            try {
                synchronized (this.mVideoEncoderMonitor) {
                    this.mVideoEncoderMonitor.notifyAll();
                }
                this.mEncoderVideoThread.join();
                this.mEncoderVideoThread = null;
            } catch (InterruptedException e) {
                try {
                    CodecLog.m41d(TAG, "Encoder Thread has been Interrupted, errors may be occurred.");
                    e.printStackTrace();
                } finally {
                    this.mEncoderVideoThread = null;
                }
            }
        }
        CodecLog.m41d(TAG, "VideoEncoder release() encoder thread exit. threadName =" + threadName);
        this.mVideoEncoderMonitor = null;
        this.mEncodeInputSurface = null;
        super.release(isVideo);
    }

    private void initVideoEncoder(boolean isUseSurface) {
        CodecLog.m41d(TAG, "initVideoEncoder()->in");
        this.mMediaFormat = MediaFormat.createVideoFormat(DEFAULT_MIME_TYPE, this.mWidth, this.mHeight);
        this.mMediaFormat.setInteger("color-format", 2130708361);
        this.mMediaFormat.setInteger(LdbConstants.LDB_FEAT_NAME_VIDEO_BITRATE, DEFAULT_BIT_RATE);
        this.mMediaFormat.setInteger("frame-rate", 30);
        this.mMediaFormat.setInteger("i-frame-interval", 10);
        try {
            this.mEncoder = MediaCodec.createEncoderByType(DEFAULT_MIME_TYPE);
        } catch (IOException e) {
            CodecLog.m42e(TAG, "initVideoEncoder()->createEncoderByType failed.");
            e.printStackTrace();
            if (this.mStickerRecordingListener != null) {
                this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO_CREATE, Integer.valueOf(0));
            }
        }
        try {
            this.mEncoder.configure(this.mMediaFormat, null, null, 1);
        } catch (Exception e2) {
            CodecLog.m42e(TAG, "initVideoEncoder()->configure failed.");
            e2.printStackTrace();
            if (this.mStickerRecordingListener != null) {
                this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO_CONFIGURE, Integer.valueOf(0));
            }
        }
        if (isUseSurface) {
            try {
                this.mEncodeInputSurface = this.mEncoder.createInputSurface();
            } catch (Exception e22) {
                CodecLog.m42e(TAG, "initVideoEncoder()->createInputSurface failed.");
                e22.printStackTrace();
                if (this.mStickerRecordingListener != null) {
                    this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO_CONFIGURE, Integer.valueOf(0));
                }
            }
        } else {
            this.mEncodeInputSurface = null;
        }
        CodecLog.m41d(TAG, "initVideoEncoder()->out");
    }
}
