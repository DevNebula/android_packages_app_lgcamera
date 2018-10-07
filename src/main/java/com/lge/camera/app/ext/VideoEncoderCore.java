package com.lge.camera.app.ext;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.view.Surface;
import com.lge.camera.app.ext.MultiViewRecorder.EncoderConfig;
import com.lge.camera.components.AudioData;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.util.CamLog;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

public class VideoEncoderCore {
    private static final int TIMEOUT_USEC = 10000;
    private static final boolean VERBOSE = true;
    private LinkedList<AudioData> mAudioBuffer = new LinkedList();
    private BufferInfo mAudioBufferInfo = new BufferInfo();
    private MediaCodec mAudioEncoder;
    private int mAudioInputChunk = 0;
    private int mAudioTrackIndex;
    private Surface mInputSurface;
    private boolean mIsAudioTrackAdded;
    private boolean mIsVideoTrackAdded;
    private MediaMuxer mMuxer;
    private boolean mMuxerStarted;
    private long mPreTimeStamp = 0;
    private String mShotMode;
    private BufferInfo mVideoBufferInfo = new BufferInfo();
    private MediaCodec mVideoEncoder;
    private int mVideoTrackIndex;

    public VideoEncoderCore(EncoderConfig config, String shotMode) throws IOException {
        this.mShotMode = shotMode;
        MediaFormat videoFormat = MediaFormat.createVideoFormat(config.mVideoMimeType, config.mVideoWidth, config.mVideoHeight);
        videoFormat.setInteger("color-format", 2130708361);
        videoFormat.setInteger(LdbConstants.LDB_FEAT_NAME_VIDEO_BITRATE, config.mVideoBitRate);
        videoFormat.setInteger("frame-rate", config.mVideoFrameRate);
        videoFormat.setInteger("i-frame-interval", config.mVideoIFrameInterval);
        CamLog.m3d(CameraConstants.TAG, "video format: " + videoFormat);
        MediaFormat audioFormat = new MediaFormat();
        audioFormat.setString("mime", config.mAudioMimeType);
        audioFormat.setInteger("aac-profile", 2);
        audioFormat.setInteger("sample-rate", config.mAudioSampleRate);
        audioFormat.setInteger("channel-count", config.mAudioChannelCount);
        audioFormat.setInteger(LdbConstants.LDB_FEAT_NAME_VIDEO_BITRATE, config.mAudioBitRate);
        CamLog.m3d(CameraConstants.TAG, "audio format: " + audioFormat);
        this.mVideoEncoder = MediaCodec.createEncoderByType(config.mVideoMimeType);
        this.mVideoEncoder.configure(videoFormat, null, null, 1);
        this.mInputSurface = this.mVideoEncoder.createInputSurface();
        this.mVideoEncoder.start();
        CamLog.m3d(CameraConstants.TAG, "VideoEncoder.start()");
        this.mAudioEncoder = MediaCodec.createByCodecName("OMX.google.aac.encoder");
        this.mAudioEncoder.configure(audioFormat, null, null, 1);
        this.mAudioEncoder.start();
        CamLog.m3d(CameraConstants.TAG, "AudioEncoder.start()");
        this.mMuxer = new MediaMuxer(config.mOutputFile.toString(), 0);
        this.mMuxer.setOrientationHint(config.mVideoOrientationHint);
        this.mVideoTrackIndex = -1;
        this.mAudioTrackIndex = -1;
        this.mMuxerStarted = false;
        this.mIsAudioTrackAdded = false;
        this.mIsVideoTrackAdded = false;
    }

    public Surface getInputSurface() {
        return this.mInputSurface;
    }

    public void release() {
        CamLog.m3d(CameraConstants.TAG, "releasing encoder objects");
        if (this.mInputSurface != null) {
            this.mInputSurface.release();
            this.mInputSurface = null;
        }
        if (this.mVideoEncoder != null) {
            this.mVideoEncoder.stop();
            this.mVideoEncoder.release();
            this.mVideoEncoder = null;
        }
        if (this.mAudioEncoder != null) {
            this.mAudioEncoder.stop();
            this.mAudioEncoder.release();
            this.mAudioEncoder = null;
        }
        try {
            if (this.mMuxer != null) {
                this.mMuxer.stop();
                this.mMuxer.release();
                this.mMuxer = null;
            }
        } catch (IllegalStateException e) {
            CamLog.m3d(CameraConstants.TAG, "muxer is already stopped");
        }
        CamLog.m3d(CameraConstants.TAG, "mAudioBuffer remaining size = " + this.mAudioBuffer.size());
        this.mAudioBuffer.clear();
        this.mAudioBuffer = null;
    }

    private void setRecordingType(String type) {
        CamLog.m3d(CameraConstants.TAG, "-Multiview- setRecordingType");
        try {
            Class.forName("android.media.MediaMuxer").getDeclaredMethod("setRecordingType", new Class[]{String.class}).invoke(this.mMuxer, new Object[]{type});
        } catch (Exception e) {
            CamLog.m6e(CameraConstants.TAG, "-Multiview- setRecordingType invoke error : ", e);
        }
    }

    private void startMuxer() {
        if (this.mMuxerStarted) {
            throw new RuntimeException("format changed twice");
        } else if (this.mIsAudioTrackAdded && this.mIsVideoTrackAdded) {
            setRecordingType(getCurrentRecordingType());
            this.mMuxer.start();
            this.mMuxerStarted = true;
            CamLog.m3d(CameraConstants.TAG, "Muxer is started");
            CamLog.m3d(CameraConstants.TAG, "video track index = " + this.mVideoTrackIndex + ", audio track index = " + this.mAudioTrackIndex);
        } else {
            CamLog.m3d(CameraConstants.TAG, "Muxer is not started; later");
        }
    }

    private String getCurrentRecordingType() {
        if (CameraConstants.MODE_SQUARE_SPLICE.equals(this.mShotMode)) {
            return CameraConstants.VIDEO_SPLICE_TYPE;
        }
        if (CameraConstants.MODE_SQUARE_GRID.equals(this.mShotMode)) {
            return CameraConstants.VIDEO_GRID_TYPE;
        }
        return CameraConstants.VIDEO_MULTIVIEW_TYPE;
    }

    public void drainVideoEncoder(boolean endOfStream) {
        CamLog.m3d(CameraConstants.TAG, "drainVideoEncoder(" + endOfStream + ")");
        if (endOfStream) {
            CamLog.m3d(CameraConstants.TAG, "sending EOS to video encoder");
            this.mVideoEncoder.signalEndOfInputStream();
        }
        ByteBuffer[] videoEncoderOutputBuffers = this.mVideoEncoder.getOutputBuffers();
        while (true) {
            int videoOutputBufferIndex = this.mVideoEncoder.dequeueOutputBuffer(this.mVideoBufferInfo, 10000);
            if (videoOutputBufferIndex == -1) {
                if (endOfStream) {
                    CamLog.m3d(CameraConstants.TAG, "no output available, spinning to await EOS");
                } else {
                    return;
                }
            } else if (videoOutputBufferIndex == -3) {
                videoEncoderOutputBuffers = this.mVideoEncoder.getOutputBuffers();
            } else if (videoOutputBufferIndex == -2) {
                if (!this.mIsVideoTrackAdded) {
                    MediaFormat newFormat = this.mVideoEncoder.getOutputFormat();
                    CamLog.m3d(CameraConstants.TAG, "video encoder output format changed: " + newFormat);
                    this.mVideoTrackIndex = this.mMuxer.addTrack(newFormat);
                    this.mIsVideoTrackAdded = true;
                }
                startMuxer();
            } else if (videoOutputBufferIndex < 0) {
                CamLog.m11w(CameraConstants.TAG, "unexpected result from encoder.dequeueOutputBuffer: " + videoOutputBufferIndex);
            } else {
                if (writeData(endOfStream, videoEncoderOutputBuffers, videoOutputBufferIndex, this.mVideoBufferInfo, this.mVideoEncoder, this.mVideoTrackIndex)) {
                    return;
                }
            }
        }
    }

    public void feedAudioEncoder(ByteBuffer audioMixed, long timeStamp) {
        CamLog.m3d(CameraConstants.TAG, "feedAudioEncoder - timestamp = " + String.format("%,d", new Object[]{Long.valueOf(timeStamp)}));
        if (audioMixed != null) {
            this.mAudioBuffer.add(new AudioData(timeStamp, audioMixed));
        }
        if (!this.mMuxerStarted) {
            CamLog.m3d(CameraConstants.TAG, "muxer is not started; add audio data to queue, ts = " + timeStamp);
        } else if (audioMixed != null) {
            do {
            } while (queueAudioData());
        } else {
            queueAudioDataEOS();
        }
    }

    public void flushAudioBuffer() {
        CamLog.m3d(CameraConstants.TAG, "flush audio buffer --- START");
        drainAudioEncoder(false);
        while (!this.mAudioBuffer.isEmpty()) {
            queueAudioData();
            drainAudioEncoder(false);
        }
        CamLog.m3d(CameraConstants.TAG, "flush audio buffer --- END");
    }

    /* JADX WARNING: Missing block: B:22:0x00bb, code:
            if (checkInputBuffer(r1) == false) goto L_0x00aa;
     */
    private synchronized boolean queueAudioData() {
        /*
        r15 = this;
        r11 = 1;
        r0 = 0;
        monitor-enter(r15);
        r1 = -1;
        r2 = r15.mAudioBuffer;	 Catch:{ all -> 0x00b4 }
        r2 = r2.isEmpty();	 Catch:{ all -> 0x00b4 }
        if (r2 != 0) goto L_0x0014;
    L_0x000c:
        r2 = r15.mAudioEncoder;	 Catch:{ all -> 0x00b4 }
        r12 = 10000; // 0x2710 float:1.4013E-41 double:4.9407E-320;
        r1 = r2.dequeueInputBuffer(r12);	 Catch:{ all -> 0x00b4 }
    L_0x0014:
        r2 = "CameraApp";
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00b4 }
        r6.<init>();	 Catch:{ all -> 0x00b4 }
        r12 = "inputBufferIndex = ";
        r6 = r6.append(r12);	 Catch:{ all -> 0x00b4 }
        r6 = r6.append(r1);	 Catch:{ all -> 0x00b4 }
        r6 = r6.toString();	 Catch:{ all -> 0x00b4 }
        com.lge.camera.util.CamLog.m3d(r2, r6);	 Catch:{ all -> 0x00b4 }
        if (r1 < 0) goto L_0x00b7;
    L_0x002e:
        r2 = r15.mAudioBuffer;	 Catch:{ all -> 0x00b4 }
        r2 = r2.isEmpty();	 Catch:{ all -> 0x00b4 }
        if (r2 != 0) goto L_0x00ac;
    L_0x0036:
        r0 = r15.mAudioBuffer;	 Catch:{ all -> 0x00b4 }
        r7 = r0.remove();	 Catch:{ all -> 0x00b4 }
        r7 = (com.lge.camera.components.AudioData) r7;	 Catch:{ all -> 0x00b4 }
        r8 = r7.getData();	 Catch:{ all -> 0x00b4 }
        r4 = r7.getTimeStamp();	 Catch:{ all -> 0x00b4 }
        r0 = r15.mAudioEncoder;	 Catch:{ all -> 0x00b4 }
        r10 = r0.getInputBuffers();	 Catch:{ all -> 0x00b4 }
        r9 = r10[r1];	 Catch:{ all -> 0x00b4 }
        r9.clear();	 Catch:{ all -> 0x00b4 }
        r0 = 0;
        r8.position(r0);	 Catch:{ all -> 0x00b4 }
        r9.put(r8);	 Catch:{ all -> 0x00b4 }
        r3 = r9.limit();	 Catch:{ all -> 0x00b4 }
        r0 = r15.mAudioEncoder;	 Catch:{ all -> 0x00b4 }
        r2 = 0;
        r6 = 0;
        r0.queueInputBuffer(r1, r2, r3, r4, r6);	 Catch:{ all -> 0x00b4 }
        r15.mPreTimeStamp = r4;	 Catch:{ all -> 0x00b4 }
        r0 = "CameraApp";
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00b4 }
        r2.<init>();	 Catch:{ all -> 0x00b4 }
        r6 = "submitted audio frame ";
        r2 = r2.append(r6);	 Catch:{ all -> 0x00b4 }
        r6 = r15.mAudioInputChunk;	 Catch:{ all -> 0x00b4 }
        r2 = r2.append(r6);	 Catch:{ all -> 0x00b4 }
        r6 = " to audio enc, size=";
        r2 = r2.append(r6);	 Catch:{ all -> 0x00b4 }
        r2 = r2.append(r3);	 Catch:{ all -> 0x00b4 }
        r6 = ", timestamp = ";
        r2 = r2.append(r6);	 Catch:{ all -> 0x00b4 }
        r6 = "%,d";
        r12 = 1;
        r12 = new java.lang.Object[r12];	 Catch:{ all -> 0x00b4 }
        r13 = 0;
        r14 = java.lang.Long.valueOf(r4);	 Catch:{ all -> 0x00b4 }
        r12[r13] = r14;	 Catch:{ all -> 0x00b4 }
        r6 = java.lang.String.format(r6, r12);	 Catch:{ all -> 0x00b4 }
        r2 = r2.append(r6);	 Catch:{ all -> 0x00b4 }
        r2 = r2.toString();	 Catch:{ all -> 0x00b4 }
        com.lge.camera.util.CamLog.m3d(r0, r2);	 Catch:{ all -> 0x00b4 }
        r0 = r15.mAudioInputChunk;	 Catch:{ all -> 0x00b4 }
        r0 = r0 + 1;
        r15.mAudioInputChunk = r0;	 Catch:{ all -> 0x00b4 }
    L_0x00a9:
        r0 = r11;
    L_0x00aa:
        monitor-exit(r15);
        return r0;
    L_0x00ac:
        r2 = "CameraApp";
        r6 = "queue is empty; break";
        com.lge.camera.util.CamLog.m3d(r2, r6);	 Catch:{ all -> 0x00b4 }
        goto L_0x00aa;
    L_0x00b4:
        r0 = move-exception;
        monitor-exit(r15);
        throw r0;
    L_0x00b7:
        r2 = r15.checkInputBuffer(r1);	 Catch:{ all -> 0x00b4 }
        if (r2 != 0) goto L_0x00a9;
    L_0x00bd:
        goto L_0x00aa;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.app.ext.VideoEncoderCore.queueAudioData():boolean");
    }

    private synchronized boolean queueAudioDataEOS() {
        boolean z;
        int audioEncInputBufferIndex = this.mAudioEncoder.dequeueInputBuffer(10000);
        CamLog.m3d(CameraConstants.TAG, "inputBufferIndex = " + audioEncInputBufferIndex);
        if (audioEncInputBufferIndex >= 0) {
            this.mAudioEncoder.queueInputBuffer(audioEncInputBufferIndex, 0, 0, this.mPreTimeStamp, 4);
            CamLog.m3d(CameraConstants.TAG, "BUFFER_FLAG_END_OF_STREAM is sent to audio enc");
            z = false;
        } else if (checkInputBuffer(audioEncInputBufferIndex)) {
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    private boolean checkInputBuffer(int inputBufIndex) {
        if (inputBufIndex == -3) {
            CamLog.m3d(CameraConstants.TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "input buffer is not available");
        return false;
    }

    public synchronized void drainAudioEncoder(boolean endOfStream) {
        CamLog.m3d(CameraConstants.TAG, "drainAudioEncoder(" + endOfStream + ")");
        ByteBuffer[] audioEncoderOutputBuffers = this.mAudioEncoder.getOutputBuffers();
        while (true) {
            int audioOutputBufferIndex = this.mAudioEncoder.dequeueOutputBuffer(this.mAudioBufferInfo, 10000);
            if (audioOutputBufferIndex == -1) {
                if (!endOfStream) {
                    break;
                }
                CamLog.m3d(CameraConstants.TAG, "no output available, spinning to await EOS");
            } else if (audioOutputBufferIndex == -3) {
                audioEncoderOutputBuffers = this.mAudioEncoder.getOutputBuffers();
            } else if (audioOutputBufferIndex == -2) {
                if (!this.mIsAudioTrackAdded) {
                    MediaFormat newFormat = this.mAudioEncoder.getOutputFormat();
                    CamLog.m3d(CameraConstants.TAG, "audio encoder output format changed: " + newFormat);
                    this.mAudioTrackIndex = this.mMuxer.addTrack(newFormat);
                    this.mIsAudioTrackAdded = true;
                }
                startMuxer();
            } else if (audioOutputBufferIndex < 0) {
                CamLog.m11w(CameraConstants.TAG, "unexpected result from encoder.dequeueOutputBuffer: " + audioOutputBufferIndex);
            } else {
                if (writeData(endOfStream, audioEncoderOutputBuffers, audioOutputBufferIndex, this.mAudioBufferInfo, this.mAudioEncoder, this.mAudioTrackIndex)) {
                    break;
                }
            }
        }
    }

    private boolean writeData(boolean endOfStream, ByteBuffer[] encoderOutputBuffers, int outputBufferIndex, BufferInfo bufferInfo, MediaCodec encoder, int trackIndex) {
        ByteBuffer encodedData = encoderOutputBuffers[outputBufferIndex];
        if (encodedData == null) {
            throw new RuntimeException("encoderOutputBuffer " + outputBufferIndex + " was null");
        }
        if ((bufferInfo.flags & 2) != 0) {
            CamLog.m3d(CameraConstants.TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
            bufferInfo.size = 0;
        }
        if (bufferInfo.size != 0) {
            if (!this.mMuxerStarted) {
                return false;
            }
            encodedData.position(bufferInfo.offset);
            encodedData.limit(bufferInfo.offset + bufferInfo.size);
            this.mMuxer.writeSampleData(trackIndex, encodedData, bufferInfo);
            CamLog.m3d(CameraConstants.TAG, "track = " + trackIndex + ", sent " + bufferInfo.size + " bytes to muxer, ts=" + String.format("%,d", new Object[]{Long.valueOf(bufferInfo.presentationTimeUs)}));
        }
        encoder.releaseOutputBuffer(outputBufferIndex, false);
        if ((bufferInfo.flags & 4) == 0) {
            return false;
        }
        if (endOfStream) {
            CamLog.m3d(CameraConstants.TAG, "end of video stream reached");
        } else {
            CamLog.m11w(CameraConstants.TAG, "reached end of video stream unexpectedly");
        }
        return true;
    }

    public boolean isAudioBufferEmpty() {
        return this.mAudioBuffer.isEmpty();
    }
}
