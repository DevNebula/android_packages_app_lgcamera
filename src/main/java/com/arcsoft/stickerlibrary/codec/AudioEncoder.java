package com.arcsoft.stickerlibrary.codec;

import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaFormat;
import com.arcsoft.stickerlibrary.api.StickerRecordingListener;
import com.arcsoft.stickerlibrary.sticker.StickerMessage;
import com.lge.camera.constants.LdbConstants;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioEncoder extends BaseEncoder {
    private static final String ENCODER_THREAD_NAME = "Arc_Audio_Encoder";
    private static final String TAG = "Arc_BaseEncoder";
    private final int DEFAULT_AUDIO_FORMAT;
    private final int DEFAULT_AUDIO_SOURCE;
    private final int DEFAULT_BIT_RATE;
    private final int DEFAULT_CHANNEL_CINFIG;
    private final String DEFAULT_MIME_TYPE;
    private final int DEFAULT_SAMPLE_RATE;
    private int mActualBufferSize;
    private Object mAudioEncoderMonitor;
    private Thread mAudioEncoderThread;
    private int mAudioFormat;
    private AudioRecord mAudioRecord;
    private int mAudioSource;
    private int mBitrate;
    private int mChannelConfig;
    private boolean mIsInited;
    private int mMinBufferSize;
    private int mSampleRate;
    private long mStartPausingTime;

    public AudioEncoder(MuxerWrapper muxer, Object pauseMonitor, StickerRecordingListener listener) {
        super(muxer, pauseMonitor, listener);
        this.DEFAULT_SAMPLE_RATE = 44100;
        this.DEFAULT_CHANNEL_CINFIG = 16;
        this.DEFAULT_AUDIO_FORMAT = 2;
        this.DEFAULT_AUDIO_SOURCE = 1;
        this.DEFAULT_MIME_TYPE = "audio/mp4a-latm";
        this.DEFAULT_BIT_RATE = 2000000;
        this.mSampleRate = 44100;
        this.mChannelConfig = 16;
        this.mAudioFormat = 2;
        this.mAudioSource = 1;
        this.mBitrate = 2000000;
        this.mAudioEncoderMonitor = new Object();
        this.mIsMuxerStarted = false;
        this.mStartPausingTime = 0;
    }

    public void prepare(boolean isUseSurface) {
        if (initAudioRecord()) {
            CodecLog.m41d(TAG, "Init AudioRecord success.");
            if (initAudioEncoder()) {
                CodecLog.m41d(TAG, "Init AudioEncoder success.");
                this.mIsInited = true;
            }
        }
    }

    private boolean initAudioRecord() {
        this.mMinBufferSize = AudioRecord.getMinBufferSize(this.mSampleRate, this.mChannelConfig, this.mAudioFormat);
        CodecLog.m41d(TAG, "initAudioRecord()->AudioRecord mini buffer size=" + this.mMinBufferSize);
        try {
            this.mAudioRecord = new AudioRecord(this.mAudioSource, this.mSampleRate, this.mChannelConfig, this.mAudioFormat, this.mMinBufferSize);
        } catch (Exception e) {
            if (this.mStickerRecordingListener != null) {
                this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_AUDIO_RECORD_CREATE, Integer.valueOf(0));
            }
            e.printStackTrace();
        }
        if (this.mAudioRecord == null || 1 != this.mAudioRecord.getState()) {
            CodecLog.m42e(TAG, "initAudioRecord()-> AudioRecord initialized failed.");
            this.mAudioRecord = null;
            return false;
        }
        this.mActualBufferSize = this.mMinBufferSize;
        return true;
    }

    private boolean initAudioEncoder() {
        MediaFormat audioFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", this.mSampleRate, this.mChannelConfig);
        audioFormat.setInteger("aac-profile", 2);
        audioFormat.setInteger("channel-count", getAudioChannelCount());
        audioFormat.setInteger(LdbConstants.LDB_FEAT_NAME_VIDEO_BITRATE, this.mBitrate);
        try {
            this.mEncoder = MediaCodec.createEncoderByType("audio/mp4a-latm");
        } catch (IOException e) {
            CodecLog.m42e(TAG, "initAudioEncoder()-> Crate audioEncoder is failed, may be caused by invalid params.");
            this.mEncoder = null;
            if (this.mStickerRecordingListener != null) {
                this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_AUDIO_CREATE, Integer.valueOf(0));
            }
            e.printStackTrace();
        }
        if (this.mEncoder == null) {
            return false;
        }
        try {
            this.mEncoder.configure(audioFormat, null, null, 1);
        } catch (Exception e2) {
            e2.printStackTrace();
            if (this.mStickerRecordingListener != null) {
                this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_AUDIO_CONFIGURE, Integer.valueOf(0));
            }
        }
        return true;
    }

    private int getAudioChannelCount() {
        if (12 == this.mAudioFormat) {
            return 2;
        }
        return 1;
    }

    public void notifyNewFrameAvailable() {
    }

    public void startRecording() {
        if (!this.mIsInited) {
            CodecLog.m42e(TAG, "AudioEncoder is not initialized.");
        } else if (this.mAudioEncoderThread != null) {
            CodecLog.m42e(TAG, "startRecording()-> Audio encoder thread has been started already, can not start twice.");
            throw new RuntimeException("Audio encoder thread has been started already, can not start twice.");
        } else {
            super.startRecording();
            this.mAudioEncoderThread = new Thread(ENCODER_THREAD_NAME) {
                public void run() {
                    super.run();
                    try {
                        AudioEncoder.this.mAudioRecord.startRecording();
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (AudioEncoder.this.mStickerRecordingListener != null) {
                            AudioEncoder.this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_AUDIO_RECORD_START_RECORDING, Integer.valueOf(0));
                        }
                    }
                    try {
                        AudioEncoder.this.mEncoder.start();
                    } catch (Exception e2) {
                        CodecLog.m42e(AudioEncoder.TAG, "audio encoder start_failed : " + e2.getMessage());
                        if (AudioEncoder.this.mStickerRecordingListener != null) {
                            AudioEncoder.this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_AUDIO_START, Integer.valueOf(0));
                        }
                    }
                    long startTime = System.nanoTime();
                    ByteBuffer rawBuffer = ByteBuffer.allocateDirect(AudioEncoder.this.mActualBufferSize);
                    while (!AudioEncoder.this.mIsRequestStop) {
                        CodecLog.m41d(AudioEncoder.TAG, "AudioRecord startRecording() next." + AudioEncoder.this.mIsRequestStop);
                        if (AudioEncoder.this.mIsRequestPause) {
                            long startPauseTime = System.nanoTime();
                            synchronized (AudioEncoder.this.mPauseMonitor) {
                                if (AudioEncoder.this.mIsRequestPause) {
                                    try {
                                        AudioEncoder.this.mAudioRecord.stop();
                                    } catch (Exception e22) {
                                        e22.printStackTrace();
                                        if (AudioEncoder.this.mStickerRecordingListener != null) {
                                            AudioEncoder.this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_AUDIO_RECORD_STOP, Integer.valueOf(0));
                                        }
                                    }
                                    try {
                                        AudioEncoder.this.mPauseMonitor.wait();
                                        try {
                                            AudioEncoder.this.mAudioRecord.startRecording();
                                        } catch (Exception e222) {
                                            e222.printStackTrace();
                                            if (AudioEncoder.this.mStickerRecordingListener != null) {
                                                AudioEncoder.this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_AUDIO_RECORD_START_RECORDING, Integer.valueOf(0));
                                            }
                                        }
                                        AudioEncoder audioEncoder = AudioEncoder.this;
                                        audioEncoder.mTotalPauseTimePassed += System.nanoTime() - startPauseTime;
                                        AudioEncoder.this.mPauseTimeQueue.add(Long.valueOf(AudioEncoder.this.mTotalPauseTimePassed));
                                    } catch (InterruptedException e3) {
                                        e3.printStackTrace();
                                    }
                                }
                            }
                        }
                        rawBuffer.clear();
                        int status = AudioEncoder.this.mAudioRecord.read(rawBuffer, AudioEncoder.this.mActualBufferSize);
                        CodecLog.m41d(AudioEncoder.TAG, "AudioRecord audio is paused. totalPassTime=" + AudioEncoder.this.mTotalPauseTimePassed);
                        if (status > 0) {
                            long timeUs = ((System.nanoTime() - startTime) - AudioEncoder.this.mTotalPauseTimePassed) / 1000;
                            CodecLog.m41d(AudioEncoder.TAG, "AudioRecord readvbuufer, size=" + rawBuffer.limit() + " timeUs = " + timeUs);
                            AudioEncoder.this.encode(rawBuffer, timeUs);
                            AudioEncoder.this.drain();
                        } else {
                            CodecLog.m42e(AudioEncoder.TAG, "AudioRecord read buffer may be meet error= " + status);
                        }
                    }
                    CodecLog.m41d(AudioEncoder.TAG, "startRecording()->AudioRecord will stop.");
                    try {
                        AudioEncoder.this.mAudioRecord.stop();
                    } catch (Exception e2222) {
                        e2222.printStackTrace();
                        if (AudioEncoder.this.mStickerRecordingListener != null) {
                            AudioEncoder.this.mStickerRecordingListener.onRecordingListener(StickerMessage.MSG_MEDIA_RECORDER_ERROR_AUDIO_RECORD_STOP, Integer.valueOf(0));
                        }
                    }
                    AudioEncoder.this.encode(null, 0);
                    AudioEncoder.this.drain();
                }
            };
            this.mAudioEncoderThread.start();
            CodecLog.m41d(TAG, "startRecording()->AudioRecord and AudioEncoder is started.");
        }
    }

    public void release(boolean isVideo) {
        String threadName = ENCODER_THREAD_NAME;
        if (this.mAudioEncoderThread != null) {
            try {
                this.mAudioEncoderThread.join();
            } catch (InterruptedException e) {
                CodecLog.m42e(TAG, "Wait AudioEncoderThread to be exit is interrupted.");
                e.printStackTrace();
            } finally {
                this.mAudioEncoderThread = null;
            }
        }
        CodecLog.m41d(TAG, "AudioEncoder release() encoder thread exit. threadName=" + threadName);
        this.mAudioRecord.release();
        this.mIsInited = false;
        super.release(isVideo);
    }
}
