package com.arcsoft.stickerlibrary.codec;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.os.Bundle;
import android.view.Surface;
import com.arcsoft.stickerlibrary.api.StickerRecordingListener;
import com.arcsoft.stickerlibrary.sticker.StickerMessage;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

public abstract class BaseEncoder {
    private static final int DEFAULT_I_FRAME_INTERVAL = 1000000;
    private static final int RESUME_FIRST_FRAME_DELTA = 50564;
    private static final String TAG = "Arc_BaseEncoder";
    private static final int THREAD_HOLD_INTERVAL_AS_PAUSE = 1000000;
    private long mEncodeTime = 0;
    private long mEncodeTimelineDelta = 0;
    protected MediaCodec mEncoder;
    protected int mFrameIndex;
    protected boolean mIsEOS;
    protected boolean mIsMuxerStarted;
    protected boolean mIsRecording;
    protected volatile boolean mIsRequestPause;
    protected boolean mIsRequestStop;
    protected boolean mIsVideoFirstResume;
    private long mLastEncodeBufferInfoTimestamep = 0;
    private long mLastKeyFrameTimestamp = 0;
    protected long mLastRecordTime;
    protected MuxerWrapper mMuxer;
    protected Object mPauseMonitor;
    protected Queue<Long> mPauseTimeQueue;
    protected StickerRecordingListener mStickerRecordingListener;
    protected volatile long mTotalPauseTimePassed;
    protected int mTrackIndex;
    private long prevOutputPTSUs = 0;

    public abstract void notifyNewFrameAvailable();

    public abstract void prepare(boolean z);

    public BaseEncoder(MuxerWrapper muxer, Object pauseMonitor, StickerRecordingListener listener) {
        this.mStickerRecordingListener = listener;
        this.mMuxer = muxer;
        this.mIsMuxerStarted = false;
        this.mIsRecording = false;
        this.mIsEOS = true;
        this.mIsRequestPause = false;
        this.mIsRequestStop = false;
        this.mFrameIndex = -1;
        this.mTrackIndex = -1;
        this.mPauseMonitor = pauseMonitor;
        this.mTotalPauseTimePassed = 0;
        this.mIsVideoFirstResume = false;
        this.mLastRecordTime = 0;
        this.mPauseTimeQueue = new LinkedList();
        CodecLog.m41d(TAG, "BaseEncoder constructor out");
    }

    public void encode(ByteBuffer buffer, long timeUsecs) {
        if (this.mEncoder == null) {
            CodecLog.m42e(TAG, "encode()->Encoder is not ready.");
            return;
        }
        CodecLog.m41d(TAG, "encode()->Encoder one frame. threadName in=" + Thread.currentThread().getName());
        int waitCount = 0;
        if (this.mIsRecording) {
            while (this.mIsRecording && !this.mIsEOS) {
                int bufferIndex = this.mEncoder.dequeueInputBuffer(500);
                if (-1 == bufferIndex) {
                    if (waitCount < 3) {
                        waitCount++;
                        CodecLog.m41d(TAG, "encode()->Encoder is busy, wait ... waitCount = " + waitCount);
                    } else {
                        CodecLog.m41d(TAG, "encode()->Encoder is busy, wait time out.");
                        return;
                    }
                } else if (bufferIndex >= 0) {
                    ByteBuffer frameBuffer = this.mEncoder.getInputBuffer(bufferIndex);
                    if (buffer == null) {
                        this.mIsEOS = true;
                        this.mEncoder.queueInputBuffer(bufferIndex, 0, 0, timeUsecs, 4);
                        CodecLog.m41d(TAG, "encode()->Encoder meets end of stream.");
                        return;
                    }
                    frameBuffer.clear();
                    frameBuffer.put(buffer);
                    frameBuffer.flip();
                    this.mEncoder.queueInputBuffer(bufferIndex, 0, frameBuffer.remaining(), timeUsecs, 0);
                    CodecLog.m41d(TAG, "encode()->Encoder is fed a new frame.");
                    return;
                }
            }
        }
    }

    public void drain() {
        if (this.mEncoder == null) {
            CodecLog.m42e(TAG, "drain()->Encoder is not ready.");
            return;
        }
        CodecLog.m41d(TAG, "drain()->Encoder one frame. threadName in=" + Thread.currentThread().getName());
        if (this.mMuxer == null) {
            CodecLog.m42e(TAG, "drain()->Muxer is not ready.");
            return;
        }
        int waitCount = 0;
        BufferInfo bufferInfo = new BufferInfo();
        while (this.mIsRecording) {
            int bufferIndex = this.mEncoder.dequeueOutputBuffer(bufferInfo, 500);
            if (-1 == bufferIndex) {
                if (waitCount >= 3) {
                    CodecLog.m41d(TAG, "drain()->Encoded frame is preparing, wait time out.");
                    break;
                } else {
                    waitCount++;
                    CodecLog.m41d(TAG, "drain()->Encoded frame is preparing, wait ... waitCount = " + waitCount);
                }
            } else if (-2 == bufferIndex) {
                if (this.mIsMuxerStarted) {
                    CodecLog.m42e(TAG, "drain()->Encoder format change twice.");
                    throw new RuntimeException("Format only allow change once, but Encoder meet twice!");
                }
                this.mTrackIndex = this.mMuxer.addTrack(this.mEncoder.getOutputFormat());
                this.mIsMuxerStarted = true;
                if (this.mMuxer.isStarted()) {
                    continue;
                } else {
                    this.mMuxer.startMuxer();
                    CodecLog.m41d(TAG, "Muxer started: threadName =" + Thread.currentThread().getName());
                    synchronized (this.mMuxer) {
                        while (!this.mMuxer.isStarted() && !this.mIsRequestStop) {
                            try {
                                this.mMuxer.wait(100);
                            } catch (InterruptedException e) {
                                CodecLog.m42e(TAG, "drain()->Wait for muxer started, but be interrupted : " + e.getMessage());
                                this.mIsMuxerStarted = false;
                            }
                        }
                        long startMuxterTime = getPTSUs();
                        this.mEncodeTime = startMuxterTime;
                        this.mMuxer.setStartTime(startMuxterTime);
                        CodecLog.m43i(TAG, "Muxer start time =" + startMuxterTime);
                    }
                }
            } else if (bufferIndex < 0) {
                CodecLog.m43i(TAG, "drain()->Encoder meet bufferStatus =" + bufferIndex);
            } else {
                ByteBuffer encodedBuffer = this.mEncoder.getOutputBuffer(bufferIndex);
                if ((bufferInfo.flags & 2) != 0) {
                    bufferInfo.size = 0;
                    CodecLog.m43i(TAG, "drain()->Encoder meet bufferStatus : BUFFER_FLAG_CODEC_CONFIG ");
                }
                if (!this.mIsMuxerStarted) {
                    CodecLog.m42e(TAG, "drain()->Encoder muxer has not started ");
                }
                if (bufferInfo.size != 0) {
                    long j;
                    encodedBuffer.position(bufferInfo.offset);
                    encodedBuffer.limit(encodedBuffer.position() + bufferInfo.size);
                    CodecLog.m41d(TAG, "drain()->Encoder one frame. threadName=" + Thread.currentThread().getName() + " timestamp original buffer info =" + bufferInfo.presentationTimeUs);
                    if (this.mLastEncodeBufferInfoTimestamep <= 0) {
                        this.mEncodeTimelineDelta = 0;
                    } else if (bufferInfo.presentationTimeUs - this.mLastEncodeBufferInfoTimestamep > 1000000) {
                        this.mEncodeTimelineDelta = 50564;
                    } else {
                        this.mEncodeTimelineDelta = bufferInfo.presentationTimeUs - this.mLastEncodeBufferInfoTimestamep;
                    }
                    if (this.mEncodeTimelineDelta < 0) {
                        j = 0;
                    } else {
                        j = this.mEncodeTimelineDelta;
                    }
                    this.mEncodeTimelineDelta = j;
                    this.mLastEncodeBufferInfoTimestamep = bufferInfo.presentationTimeUs;
                    this.mEncodeTime += this.mEncodeTimelineDelta;
                    bufferInfo.presentationTimeUs = this.mEncodeTime;
                    if (bufferInfo.presentationTimeUs < this.prevOutputPTSUs) {
                        CodecLog.m41d(TAG, "drain()->Encoder one frame. threadName=" + Thread.currentThread().getName() + " timestamp delta   bufferInfo.presentationTimeUs =" + bufferInfo.presentationTimeUs + " ,prevOutputPTSUs = " + this.prevOutputPTSUs + " ,delta = " + (this.prevOutputPTSUs - bufferInfo.presentationTimeUs) + " , mTotalPauseTimePassed = " + this.mTotalPauseTimePassed);
                        bufferInfo.presentationTimeUs = this.prevOutputPTSUs;
                    }
                    if (bufferInfo.presentationTimeUs - this.mLastKeyFrameTimestamp >= 1000000) {
                        this.mLastKeyFrameTimestamp = bufferInfo.presentationTimeUs;
                        Bundle params = new Bundle();
                        params.putInt("request-sync", 1);
                        this.mEncoder.setParameters(params);
                    }
                    this.mMuxer.writeSampleData(this.mTrackIndex, encodedBuffer, bufferInfo);
                    this.prevOutputPTSUs = bufferInfo.presentationTimeUs;
                    this.mMuxer.setCurrentTime(bufferInfo.presentationTimeUs);
                    CodecLog.m41d(TAG, "drain()->Encoder one frame. threadName=" + Thread.currentThread().getName() + " timestamp=" + bufferInfo.presentationTimeUs);
                }
                this.mEncoder.releaseOutputBuffer(bufferIndex, false);
                if ((bufferInfo.flags & 4) != 0) {
                    if (this.mIsEOS) {
                        CodecLog.m41d(TAG, "drain()->Encoder meet end of stream.");
                    } else {
                        CodecLog.m42e(TAG, "drain()->Encoder meet unexpected end of stream.");
                    }
                    this.mIsRecording = false;
                }
            }
        }
        CodecLog.m41d(TAG, "drain()->Encoder one frame. threadName out=" + Thread.currentThread().getName());
    }

    public Surface getInputSurface() {
        return null;
    }

    public void startRecording() {
        if (this.mIsRecording) {
            CodecLog.m43i(TAG, "startRecording()-> encoder is started, you can not start it again");
            return;
        }
        this.mIsRecording = true;
        this.mIsRequestStop = false;
        this.mIsEOS = false;
        CodecLog.m41d(TAG, "startRecording()-> encoder is started.");
    }

    public void pauseRecording() {
        this.mIsRequestPause = true;
        CodecLog.m41d(TAG, "Log_mIsRequestPause_Vaule_pauseRecording ->mIsRequestPause=" + this.mIsRequestPause);
    }

    public void resumeRecording() {
        this.mIsRequestPause = false;
        CodecLog.m41d(TAG, "Log_mIsRequestPause_Vaule_resumeRecording ->mIsRequestPause=" + this.mIsRequestPause);
    }

    public void stopRecording() {
        if (this.mIsRequestStop) {
            CodecLog.m43i(TAG, "stopRecording()-> stop encoder request command is received,you can not send stop command again.");
        } else {
            this.mIsRequestStop = true;
        }
    }

    public void release(boolean isVideo) {
        if (this.mEncoder != null) {
            try {
                this.mEncoder.stop();
            } catch (Exception e) {
                e.printStackTrace();
                if (isVideo) {
                    if (this.mStickerRecordingListener != null) {
                        this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO_STOP, Integer.valueOf(0));
                    }
                } else if (this.mStickerRecordingListener != null) {
                    this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_AUDIO_STOP, Integer.valueOf(0));
                }
            }
            try {
                this.mEncoder.release();
            } catch (Exception e2) {
                e2.printStackTrace();
                if (isVideo) {
                    if (this.mStickerRecordingListener != null) {
                        this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO_RELEASE, Integer.valueOf(0));
                    }
                } else if (this.mStickerRecordingListener != null) {
                    this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_AUDIO_RELEASE, Integer.valueOf(0));
                }
            }
            this.mEncoder = null;
        }
        if (this.mMuxer != null) {
            this.mMuxer.stopMuxer();
            this.mMuxer = null;
        }
        this.mIsRecording = false;
        this.mIsRequestPause = false;
        this.mIsRequestStop = false;
        this.mIsEOS = true;
        this.mPauseMonitor = null;
    }

    protected long getPTSUs() {
        long currentTimeInNano = System.nanoTime();
        long pauseTimeDuration = this.mTotalPauseTimePassed;
        if (this.mPauseTimeQueue.size() != 0) {
            pauseTimeDuration = ((Long) this.mPauseTimeQueue.poll()).longValue();
        }
        long result = (currentTimeInNano - pauseTimeDuration) / 1000;
        CodecLog.m41d(TAG, "getPTSUs TotalPauseTime=" + (this.mTotalPauseTimePassed / 1000));
        CodecLog.m41d(TAG, "getPTSUs preTime=" + this.prevOutputPTSUs + " ,currentTime=" + (currentTimeInNano / 1000) + " , result=" + result);
        if (result < this.prevOutputPTSUs) {
            return this.prevOutputPTSUs;
        }
        return result;
    }
}
