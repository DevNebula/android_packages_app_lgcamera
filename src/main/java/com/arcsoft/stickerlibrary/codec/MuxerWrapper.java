package com.arcsoft.stickerlibrary.codec;

import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.arcsoft.stickerlibrary.api.StickerRecordingListener;
import com.arcsoft.stickerlibrary.sticker.StickerMessage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MuxerWrapper {
    private static final String TAG = "Arc_MuxerWrapper";
    private static final String VIDEO_PREFFIX = "video";
    private static final String VIDEO_SUFFIX = ".mp4";
    private CodecLog CodecLog;
    private long mCurrentMuxerTime;
    private volatile int mEncoderCount;
    private volatile boolean mIsStarted;
    private int mMaxEncoderCount;
    private MediaMuxer mMuxer;
    private String mOutputVideoPath;
    private long mStartMuxerTime;
    private StickerRecordingListener mStickerRecordingListener;

    public MuxerWrapper(@Nullable String outputVideoPath, int screenOrientation, StickerRecordingListener listener) {
        this.mStartMuxerTime = 0;
        this.mCurrentMuxerTime = 0;
        this.mStickerRecordingListener = null;
        this.mStickerRecordingListener = listener;
        this.mOutputVideoPath = outputVideoPath;
        this.mMaxEncoderCount = 0;
        this.mEncoderCount = 0;
        this.mIsStarted = false;
        makeDirecoryValid();
        CodecLog codecLog = this.CodecLog;
        CodecLog.m41d(TAG, "MuxerWrapper()-> video name=" + this.mOutputVideoPath);
        try {
            this.mMuxer = new MediaMuxer(this.mOutputVideoPath, 0);
            this.mMuxer.setOrientationHint(screenOrientation);
            codecLog = this.CodecLog;
            CodecLog.m41d(TAG, "MuxerWrapper()-> screenOrientation=" + screenOrientation);
        } catch (IOException e) {
            codecLog = this.CodecLog;
            CodecLog.m42e(TAG, "MuxerWrapper()-> create MediaMuxer failed.");
            e.printStackTrace();
            this.mMuxer = null;
            if (this.mStickerRecordingListener != null) {
                this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER_CREATE, Integer.valueOf(0));
            }
        }
    }

    public MuxerWrapper(@Nullable String outputDirectory, int outputFormat, int screenOrientation, StickerRecordingListener listener) {
        this(outputDirectory, screenOrientation, listener);
    }

    public void setEncoderCount(int maxCount) {
        if (maxCount <= 0 || maxCount > 2) {
            throw new RuntimeException("The encoder count must between 1 and 2.");
        }
        this.mMaxEncoderCount = maxCount;
    }

    public synchronized void startMuxer() {
        CodecLog codecLog;
        if (this.mMuxer == null) {
            codecLog = this.CodecLog;
            CodecLog.m42e(TAG, "startMuxer()-> mMuxer must be created , but it's null until now");
        } else {
            this.mEncoderCount++;
            if (this.mEncoderCount == this.mMaxEncoderCount) {
                try {
                    codecLog = this.CodecLog;
                    CodecLog.m42e(TAG, "startMuxer()-> Muxerstart");
                    this.mMuxer.start();
                } catch (Exception e) {
                    codecLog = this.CodecLog;
                    CodecLog.m42e(TAG, "startMuxer()-> Muxer start failed");
                    if (this.mStickerRecordingListener != null) {
                        this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER_START, Integer.valueOf(0));
                    }
                    e.printStackTrace();
                }
                this.mIsStarted = true;
                notifyAll();
                codecLog = this.CodecLog;
                CodecLog.m41d(TAG, "startMuxer()-> mMuxer is started");
            }
        }
    }

    public boolean isStarted() {
        return this.mIsStarted;
    }

    public synchronized void writeSampleData(int trackIndex, @NonNull ByteBuffer byteBuf, @NonNull BufferInfo bufferInfo) {
        CodecLog codecLog;
        if (this.mMuxer == null) {
            codecLog = this.CodecLog;
            CodecLog.m42e(TAG, "writeSampleData()-> mMuxer must be created , but it's null until now.");
        } else {
            try {
                this.mMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
                codecLog = this.CodecLog;
                CodecLog.m42e(TAG, "writeSampleData()-> writeSampleData done");
            } catch (Exception e) {
                codecLog = this.CodecLog;
                CodecLog.m42e(TAG, "writeSampleData()-> writeSampleData failed");
                e.printStackTrace();
                if (this.mStickerRecordingListener != null) {
                    this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER_WRITE_SAMPLE_DATA, Integer.valueOf(0));
                }
            }
        }
    }

    public synchronized int addTrack(@NonNull MediaFormat format) {
        int result;
        if (this.mMuxer == null) {
            CodecLog codecLog = this.CodecLog;
            CodecLog.m42e(TAG, "writeSampleData()-> mMuxer must be created , but it's null until now.");
            result = -1;
        } else {
            result = 0;
            try {
                result = this.mMuxer.addTrack(format);
            } catch (Exception e) {
                if (this.mStickerRecordingListener != null) {
                    this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER_ADD_TRACK, Integer.valueOf(0));
                }
                e.printStackTrace();
            }
        }
        return result;
    }

    public synchronized void stopMuxer() {
        if (this.mMuxer != null) {
            this.mEncoderCount--;
            CodecLog codecLog = this.CodecLog;
            CodecLog.m41d(TAG, "stopMuxer()-> mEncoderCount=" + this.mEncoderCount + " ,maxCount=" + this.mMaxEncoderCount);
            if (this.mEncoderCount == 0) {
                try {
                    this.mMuxer.stop();
                } catch (Exception e) {
                    codecLog = this.CodecLog;
                    CodecLog.m42e(TAG, "stopMuxer()-> muxer.stop() error=" + e.getMessage());
                    if (this.mStickerRecordingListener != null) {
                        this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER_STOP, Integer.valueOf(0));
                    }
                }
                try {
                    this.mMuxer.release();
                } catch (Exception e2) {
                    codecLog = this.CodecLog;
                    CodecLog.m42e(TAG, "stopMuxer()-> muxer.release() error=" + e2.getMessage());
                    if (this.mStickerRecordingListener != null) {
                        this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER_RELEASE, Integer.valueOf(0));
                    }
                }
                this.mMuxer = null;
                File resultVideoPath = new File(this.mOutputVideoPath);
                if (resultVideoPath.exists()) {
                    codecLog = this.CodecLog;
                    CodecLog.m41d(TAG, "stopMuxer()->  video size = " + resultVideoPath.length());
                    if (0 == resultVideoPath.length() && resultVideoPath.isFile()) {
                        resultVideoPath.delete();
                        codecLog = this.CodecLog;
                        CodecLog.m41d(TAG, "stopMuxer()->  video size = 0 delete.");
                    }
                }
                codecLog = this.CodecLog;
                CodecLog.m41d(TAG, "stopMuxer()-> Muxer is released.");
            }
        }
    }

    private void makeDirecoryValid() {
        File file = new File(this.mOutputVideoPath);
        if (file.exists()) {
            file.delete();
            return;
        }
        File dir = new File(file.getParent());
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public void setStartTime(long time) {
        this.mStartMuxerTime = time;
    }

    public void setCurrentTime(long time) {
        this.mCurrentMuxerTime = time;
    }

    public long getTimeElapse() {
        return this.mCurrentMuxerTime - this.mStartMuxerTime;
    }

    public long getSizeRecordFile() {
        File file = new File(this.mOutputVideoPath);
        if (file != null && file.exists() && file.isFile()) {
            return file.length();
        }
        return 0;
    }
}
