package com.lge.camera.components;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class AudioData {
    private ByteBuffer mAudioData;
    private ShortBuffer mAudioDataShort;
    private long mTimeStamp;

    public AudioData(long timestamp, ByteBuffer audioData) {
        this.mTimeStamp = timestamp;
        this.mAudioData = audioData;
    }

    public AudioData(long timestamp, ShortBuffer audioData) {
        this.mTimeStamp = timestamp;
        this.mAudioDataShort = audioData;
    }

    public long getTimeStamp() {
        return this.mTimeStamp;
    }

    public ByteBuffer getData() {
        return this.mAudioData;
    }

    public ShortBuffer getShortData() {
        return this.mAudioDataShort;
    }
}
