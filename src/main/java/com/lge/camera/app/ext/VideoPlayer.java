package com.lge.camera.app.ext;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.view.Surface;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.io.File;

public class VideoPlayer {
    private static final int TIMEOUT_USEC = 100000;
    private BufferInfo mAudioBufferInfo = new BufferInfo();
    private long mAudioFirstInputTimeNsec;
    private int mAudioInputChunk;
    private boolean mAudioInputDone = false;
    private boolean mAudioOutputDone = false;
    private volatile boolean mIsStopRequested;
    PlayerCallback mPlayerCallback;
    private File mSourceFile;
    private BufferInfo mVideoBufferInfo = new BufferInfo();
    private long mVideoFirstInputTimeNsec;
    private int mVideoHeight;
    private int mVideoInputChunk;
    private boolean mVideoInputDone = false;
    private boolean mVideoOutputDone = false;
    private Surface mVideoOutputSurface;
    private int mVideoWidth;

    public interface PlayerCallback {
        void audioDataAvailable(MediaCodec mediaCodec, int i, long j);

        void loopReset();

        void playDone();

        void playStarted(long j);

        void postRender();

        void preRender(long j);
    }

    public interface PlayerFeedback {
        void playbackStopped();
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0052  */
    public VideoPlayer(java.io.File r8, android.view.Surface r9, com.lge.camera.app.ext.VideoPlayer.PlayerCallback r10) throws java.io.IOException {
        /*
        r7 = this;
        r5 = 0;
        r7.<init>();
        r4 = new android.media.MediaCodec$BufferInfo;
        r4.<init>();
        r7.mVideoBufferInfo = r4;
        r4 = new android.media.MediaCodec$BufferInfo;
        r4.<init>();
        r7.mAudioBufferInfo = r4;
        r7.mVideoOutputDone = r5;
        r7.mVideoInputDone = r5;
        r7.mAudioOutputDone = r5;
        r7.mAudioInputDone = r5;
        r7.mSourceFile = r8;
        r7.mVideoOutputSurface = r9;
        r7.mPlayerCallback = r10;
        r0 = 0;
        r1 = new android.media.MediaExtractor;	 Catch:{ all -> 0x0099 }
        r1.<init>();	 Catch:{ all -> 0x0099 }
        r4 = r8.toString();	 Catch:{ all -> 0x004e }
        r1.setDataSource(r4);	 Catch:{ all -> 0x004e }
        r3 = selectTrack(r1);	 Catch:{ all -> 0x004e }
        if (r3 >= 0) goto L_0x0056;
    L_0x0033:
        r4 = new java.lang.RuntimeException;	 Catch:{ all -> 0x004e }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x004e }
        r5.<init>();	 Catch:{ all -> 0x004e }
        r6 = "No video track found in ";
        r5 = r5.append(r6);	 Catch:{ all -> 0x004e }
        r6 = r7.mSourceFile;	 Catch:{ all -> 0x004e }
        r5 = r5.append(r6);	 Catch:{ all -> 0x004e }
        r5 = r5.toString();	 Catch:{ all -> 0x004e }
        r4.<init>(r5);	 Catch:{ all -> 0x004e }
        throw r4;	 Catch:{ all -> 0x004e }
    L_0x004e:
        r4 = move-exception;
        r0 = r1;
    L_0x0050:
        if (r0 == 0) goto L_0x0055;
    L_0x0052:
        r0.release();
    L_0x0055:
        throw r4;
    L_0x0056:
        r1.selectTrack(r3);	 Catch:{ all -> 0x004e }
        r2 = r1.getTrackFormat(r3);	 Catch:{ all -> 0x004e }
        r4 = "width";
        r4 = r2.getInteger(r4);	 Catch:{ all -> 0x004e }
        r7.mVideoWidth = r4;	 Catch:{ all -> 0x004e }
        r4 = "height";
        r4 = r2.getInteger(r4);	 Catch:{ all -> 0x004e }
        r7.mVideoHeight = r4;	 Catch:{ all -> 0x004e }
        r4 = "CameraApp";
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x004e }
        r5.<init>();	 Catch:{ all -> 0x004e }
        r6 = "Video size is ";
        r5 = r5.append(r6);	 Catch:{ all -> 0x004e }
        r6 = r7.mVideoWidth;	 Catch:{ all -> 0x004e }
        r5 = r5.append(r6);	 Catch:{ all -> 0x004e }
        r6 = "x";
        r5 = r5.append(r6);	 Catch:{ all -> 0x004e }
        r6 = r7.mVideoHeight;	 Catch:{ all -> 0x004e }
        r5 = r5.append(r6);	 Catch:{ all -> 0x004e }
        r5 = r5.toString();	 Catch:{ all -> 0x004e }
        com.lge.camera.util.CamLog.m3d(r4, r5);	 Catch:{ all -> 0x004e }
        if (r1 == 0) goto L_0x0098;
    L_0x0095:
        r1.release();
    L_0x0098:
        return;
    L_0x0099:
        r4 = move-exception;
        goto L_0x0050;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.app.ext.VideoPlayer.<init>(java.io.File, android.view.Surface, com.lge.camera.app.ext.VideoPlayer$PlayerCallback):void");
    }

    public int getVideoWidth() {
        return this.mVideoWidth;
    }

    public int getVideoHeight() {
        return this.mVideoHeight;
    }

    public void requestStop() {
        this.mIsStopRequested = true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x005a  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0063  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x006c  */
    public void play() throws java.io.IOException {
        /*
        r15 = this;
        r7 = 0;
        r2 = 0;
        r4 = 0;
        r3 = 0;
        r5 = 0;
        r0 = r15.mSourceFile;
        r0 = r0.canRead();
        if (r0 != 0) goto L_0x0028;
    L_0x000d:
        r0 = new java.io.FileNotFoundException;
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r14 = "Unable to read ";
        r6 = r6.append(r14);
        r14 = r15.mSourceFile;
        r6 = r6.append(r14);
        r6 = r6.toString();
        r0.<init>(r6);
        throw r0;
    L_0x0028:
        r1 = new android.media.MediaExtractor;	 Catch:{ all -> 0x0145 }
        r1.<init>();	 Catch:{ all -> 0x0145 }
        r0 = r15.mSourceFile;	 Catch:{ all -> 0x0057 }
        r0 = r0.toString();	 Catch:{ all -> 0x0057 }
        r1.setDataSource(r0);	 Catch:{ all -> 0x0057 }
        r11 = r1.getTrackCount();	 Catch:{ all -> 0x0057 }
        if (r11 >= 0) goto L_0x0071;
    L_0x003c:
        r0 = new java.lang.RuntimeException;	 Catch:{ all -> 0x0057 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0057 }
        r6.<init>();	 Catch:{ all -> 0x0057 }
        r14 = "No track found in ";
        r6 = r6.append(r14);	 Catch:{ all -> 0x0057 }
        r14 = r15.mSourceFile;	 Catch:{ all -> 0x0057 }
        r6 = r6.append(r14);	 Catch:{ all -> 0x0057 }
        r6 = r6.toString();	 Catch:{ all -> 0x0057 }
        r0.<init>(r6);	 Catch:{ all -> 0x0057 }
        throw r0;	 Catch:{ all -> 0x0057 }
    L_0x0057:
        r0 = move-exception;
    L_0x0058:
        if (r2 == 0) goto L_0x0061;
    L_0x005a:
        r2.stop();
        r2.release();
        r2 = 0;
    L_0x0061:
        if (r4 == 0) goto L_0x006a;
    L_0x0063:
        r4.stop();
        r4.release();
        r4 = 0;
    L_0x006a:
        if (r1 == 0) goto L_0x0070;
    L_0x006c:
        r1.release();
        r1 = 0;
    L_0x0070:
        throw r0;
    L_0x0071:
        r12 = 0;
        r9 = 0;
    L_0x0074:
        if (r9 >= r11) goto L_0x0117;
    L_0x0076:
        r8 = r1.getTrackFormat(r9);	 Catch:{ all -> 0x0057 }
        r0 = "mime";
        r10 = r8.getString(r0);	 Catch:{ all -> 0x0057 }
        r0 = "video/";
        r0 = r10.startsWith(r0);	 Catch:{ all -> 0x0057 }
        if (r0 == 0) goto L_0x00cc;
    L_0x0088:
        r0 = "CameraApp";
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0057 }
        r6.<init>();	 Catch:{ all -> 0x0057 }
        r14 = "Extractor selected track ";
        r6 = r6.append(r14);	 Catch:{ all -> 0x0057 }
        r6 = r6.append(r9);	 Catch:{ all -> 0x0057 }
        r14 = " (";
        r6 = r6.append(r14);	 Catch:{ all -> 0x0057 }
        r6 = r6.append(r10);	 Catch:{ all -> 0x0057 }
        r14 = "): ";
        r6 = r6.append(r14);	 Catch:{ all -> 0x0057 }
        r6 = r6.append(r8);	 Catch:{ all -> 0x0057 }
        r6 = r6.toString();	 Catch:{ all -> 0x0057 }
        com.lge.camera.util.CamLog.m3d(r0, r6);	 Catch:{ all -> 0x0057 }
        r0 = "durationUs";
        r12 = r8.getLong(r0);	 Catch:{ all -> 0x0057 }
        r3 = r9;
        r2 = android.media.MediaCodec.createDecoderByType(r10);	 Catch:{ all -> 0x0057 }
        r0 = r15.mVideoOutputSurface;	 Catch:{ all -> 0x0057 }
        r6 = 0;
        r14 = 0;
        r2.configure(r8, r0, r6, r14);	 Catch:{ all -> 0x0057 }
        r2.start();	 Catch:{ all -> 0x0057 }
    L_0x00c9:
        r9 = r9 + 1;
        goto L_0x0074;
    L_0x00cc:
        r0 = "audio/";
        r0 = r10.startsWith(r0);	 Catch:{ all -> 0x0057 }
        if (r0 == 0) goto L_0x010f;
    L_0x00d4:
        r0 = "CameraApp";
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0057 }
        r6.<init>();	 Catch:{ all -> 0x0057 }
        r14 = "Extractor selected track ";
        r6 = r6.append(r14);	 Catch:{ all -> 0x0057 }
        r6 = r6.append(r9);	 Catch:{ all -> 0x0057 }
        r14 = " (";
        r6 = r6.append(r14);	 Catch:{ all -> 0x0057 }
        r6 = r6.append(r10);	 Catch:{ all -> 0x0057 }
        r14 = "): ";
        r6 = r6.append(r14);	 Catch:{ all -> 0x0057 }
        r6 = r6.append(r8);	 Catch:{ all -> 0x0057 }
        r6 = r6.toString();	 Catch:{ all -> 0x0057 }
        com.lge.camera.util.CamLog.m3d(r0, r6);	 Catch:{ all -> 0x0057 }
        r5 = r9;
        r4 = android.media.MediaCodec.createDecoderByType(r10);	 Catch:{ all -> 0x0057 }
        r0 = 0;
        r6 = 0;
        r14 = 0;
        r4.configure(r8, r0, r6, r14);	 Catch:{ all -> 0x0057 }
        r4.start();	 Catch:{ all -> 0x0057 }
        goto L_0x00c9;
    L_0x010f:
        r0 = "CameraApp";
        r6 = "unknown mime type";
        com.lge.camera.util.CamLog.m5e(r0, r6);	 Catch:{ all -> 0x0057 }
        goto L_0x00c9;
    L_0x0117:
        r0 = r15.mPlayerCallback;	 Catch:{ all -> 0x0057 }
        if (r0 == 0) goto L_0x012c;
    L_0x011b:
        if (r2 == 0) goto L_0x012c;
    L_0x011d:
        if (r1 == 0) goto L_0x012c;
    L_0x011f:
        if (r4 == 0) goto L_0x012c;
    L_0x0121:
        r0 = r15.mPlayerCallback;	 Catch:{ all -> 0x0057 }
        r0.playStarted(r12);	 Catch:{ all -> 0x0057 }
        r6 = r15.mPlayerCallback;	 Catch:{ all -> 0x0057 }
        r0 = r15;
        r0.doExtract(r1, r2, r3, r4, r5, r6);	 Catch:{ all -> 0x0057 }
    L_0x012c:
        if (r2 == 0) goto L_0x0135;
    L_0x012e:
        r2.stop();
        r2.release();
        r2 = 0;
    L_0x0135:
        if (r4 == 0) goto L_0x013e;
    L_0x0137:
        r4.stop();
        r4.release();
        r4 = 0;
    L_0x013e:
        if (r1 == 0) goto L_0x0144;
    L_0x0140:
        r1.release();
        r1 = 0;
    L_0x0144:
        return;
    L_0x0145:
        r0 = move-exception;
        r1 = r7;
        goto L_0x0058;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.app.ext.VideoPlayer.play():void");
    }

    private static int selectTrack(MediaExtractor extractor) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString("mime");
            if (mime.startsWith(CameraConstants.MIME_TYPE_VIDEO)) {
                CamLog.m3d(CameraConstants.TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                return i;
            }
        }
        return -1;
    }

    private void doExtract(MediaExtractor extractor, MediaCodec videoDecoder, int videoTrackIndex, MediaCodec audioDecoder, int audioTrackIndex, PlayerCallback playerCallback) {
        if (extractor == null || videoDecoder == null || audioDecoder == null || playerCallback == null) {
            CamLog.m3d(CameraConstants.TAG, "Exit it because of null object");
            return;
        }
        this.mVideoInputChunk = 0;
        this.mAudioInputChunk = 0;
        this.mVideoFirstInputTimeNsec = -1;
        this.mVideoOutputDone = false;
        this.mVideoInputDone = false;
        extractor.selectTrack(videoTrackIndex);
        extractor.selectTrack(audioTrackIndex);
        while (!this.mVideoOutputDone) {
            if (this.mIsStopRequested) {
                CamLog.m3d(CameraConstants.TAG, "Stop requested");
                return;
            }
            int trackIndex = extractor.getSampleTrackIndex();
            if (trackIndex == videoTrackIndex) {
                queueVideoInputBuffer(extractor, videoDecoder, videoTrackIndex);
                releaseVideoOutputBuffer(extractor, videoDecoder, playerCallback);
            } else if (trackIndex == audioTrackIndex) {
                queueAudioInputBuffer(extractor, audioDecoder, audioTrackIndex);
                releaseAudioOutputBuffer(extractor, audioDecoder, playerCallback);
            } else if (trackIndex == -1) {
                CamLog.m5e(CameraConstants.TAG, "No more samples are available");
                this.mVideoOutputDone = true;
                this.mAudioOutputDone = true;
            } else {
                CamLog.m5e(CameraConstants.TAG, "unknown track index");
            }
            if (this.mVideoOutputDone) {
                playerCallback.playDone();
            }
        }
    }

    private void queueVideoInputBuffer(MediaExtractor extractor, MediaCodec videoDecoder, int videoTrackIndex) {
        if (!this.mVideoInputDone) {
            int inputBufIndex = videoDecoder.dequeueInputBuffer(100000);
            if (inputBufIndex >= 0) {
                if (this.mVideoFirstInputTimeNsec == -1) {
                    this.mVideoFirstInputTimeNsec = System.nanoTime();
                }
                int chunkSize = readSampleData(extractor, videoDecoder, videoTrackIndex, inputBufIndex);
                if (chunkSize == 0) {
                    this.mVideoInputDone = true;
                    return;
                } else if (chunkSize > 0) {
                    CamLog.m3d(CameraConstants.TAG, "submitted video frame " + this.mVideoInputChunk + " to video dec, size=" + chunkSize);
                    this.mVideoInputChunk++;
                    return;
                } else {
                    return;
                }
            }
            CamLog.m3d(CameraConstants.TAG, "video input buffer not available");
        }
    }

    private void queueAudioInputBuffer(MediaExtractor extractor, MediaCodec audioDecoder, int audioTrackIndex) {
        if (!this.mAudioInputDone) {
            int inputBufIndex = audioDecoder.dequeueInputBuffer(100000);
            if (inputBufIndex >= 0) {
                if (this.mAudioFirstInputTimeNsec == -1) {
                    this.mAudioFirstInputTimeNsec = System.nanoTime();
                }
                int chunkSize = readSampleData(extractor, audioDecoder, audioTrackIndex, inputBufIndex);
                if (chunkSize == 0) {
                    this.mAudioInputDone = true;
                    return;
                } else if (chunkSize > 0) {
                    CamLog.m3d(CameraConstants.TAG, "submitted audio frame " + this.mAudioInputChunk + " to audio dec, size=" + chunkSize);
                    this.mAudioInputChunk++;
                    return;
                } else {
                    return;
                }
            }
            CamLog.m3d(CameraConstants.TAG, "audio input buffer not available");
        }
    }

    private int readSampleData(MediaExtractor extractor, MediaCodec decoder, int inputTrackIndex, int inputBufIndex) {
        int chunkSize = extractor.readSampleData(decoder.getInputBuffers()[inputBufIndex], 0);
        if (chunkSize < 0) {
            decoder.queueInputBuffer(inputBufIndex, 0, 0, 0, 4);
            CamLog.m3d(CameraConstants.TAG, "sent video input EOS");
            return 0;
        }
        int trackIndex = extractor.getSampleTrackIndex();
        if (trackIndex == inputTrackIndex || trackIndex < 0) {
            long presentationTimeUs = extractor.getSampleTime();
            CamLog.m3d(CameraConstants.TAG, "extract from file: track index = " + inputTrackIndex + ", timestamp = " + String.format("%,d", new Object[]{Long.valueOf(presentationTimeUs)}));
            decoder.queueInputBuffer(inputBufIndex, 0, chunkSize, presentationTimeUs, 0);
            extractor.advance();
            return chunkSize;
        }
        CamLog.m11w(CameraConstants.TAG, "WEIRD: got sample from track " + extractor.getSampleTrackIndex() + ", expected " + inputTrackIndex);
        return -1;
    }

    private void releaseVideoOutputBuffer(MediaExtractor extractor, MediaCodec videoDecoder, PlayerCallback playerCallback) {
        if (!this.mVideoOutputDone) {
            int outputBufIndex = videoDecoder.dequeueOutputBuffer(this.mVideoBufferInfo, 100000);
            if (outputBufIndex >= 0) {
                doVideoRendering(extractor, videoDecoder, playerCallback, outputBufIndex);
            } else {
                checkOutputBuffer(outputBufIndex, videoDecoder);
            }
        }
    }

    private void releaseAudioOutputBuffer(MediaExtractor extractor, MediaCodec audioDecoder, PlayerCallback playerCallback) {
        if (extractor == null || audioDecoder == null || playerCallback == null) {
            CamLog.m3d(CameraConstants.TAG, "Exit it because of null object");
        } else if (!this.mAudioOutputDone) {
            int outputBufIndex = audioDecoder.dequeueOutputBuffer(this.mAudioBufferInfo, 100000);
            if (outputBufIndex >= 0) {
                doAudioRendering(extractor, audioDecoder, playerCallback, outputBufIndex);
            } else {
                checkOutputBuffer(outputBufIndex, audioDecoder);
            }
        }
    }

    private void checkOutputBuffer(int outputBufIndex, MediaCodec decoder) {
        if (outputBufIndex == -1) {
            CamLog.m3d(CameraConstants.TAG, "no output from decoder available");
        } else if (outputBufIndex == -3) {
            CamLog.m3d(CameraConstants.TAG, "decoder output buffers changed");
        } else if (outputBufIndex == -2) {
            CamLog.m3d(CameraConstants.TAG, "decoder output format changed: " + decoder.getOutputFormat());
        } else {
            throw new RuntimeException("unexpected result from decoder.dequeueOutputBuffer: " + outputBufIndex);
        }
    }

    private void doVideoRendering(MediaExtractor extractor, MediaCodec videoDecoder, PlayerCallback playerCallback, int videoOutputBufIndex) {
        if (this.mVideoFirstInputTimeNsec != 0) {
            CamLog.m3d(CameraConstants.TAG, "video startup lag " + (((double) (System.nanoTime() - this.mVideoFirstInputTimeNsec)) / 1000000.0d) + " ms");
            this.mVideoFirstInputTimeNsec = 0;
        }
        CamLog.m3d(CameraConstants.TAG, "video decoder given buffer " + videoOutputBufIndex + " (size=" + this.mVideoBufferInfo.size + ", timestamp = " + String.format("%,d", new Object[]{Long.valueOf(this.mVideoBufferInfo.presentationTimeUs)}) + ")");
        if ((this.mVideoBufferInfo.flags & 4) != 0) {
            CamLog.m3d(CameraConstants.TAG, "video output EOS");
            this.mVideoOutputDone = true;
        }
        boolean doRender = this.mVideoBufferInfo.size != 0;
        if (doRender && playerCallback != null) {
            playerCallback.preRender(this.mVideoBufferInfo.presentationTimeUs);
        }
        if (doRender) {
            videoDecoder.releaseOutputBuffer(videoOutputBufIndex, this.mVideoBufferInfo.presentationTimeUs * 1000);
        } else {
            videoDecoder.releaseOutputBuffer(videoOutputBufIndex, false);
        }
        if (doRender && playerCallback != null) {
            playerCallback.postRender();
        }
    }

    private void doAudioRendering(MediaExtractor extractor, MediaCodec audioDecoder, PlayerCallback playerCallback, int audioOutputBufIndex) {
        if (extractor == null || audioDecoder == null || playerCallback == null) {
            CamLog.m3d(CameraConstants.TAG, "Exit it because of null object");
            return;
        }
        if (this.mAudioFirstInputTimeNsec != 0) {
            CamLog.m3d(CameraConstants.TAG, "audio startup lag " + (((double) (System.nanoTime() - this.mAudioFirstInputTimeNsec)) / 1000000.0d) + " ms");
            this.mAudioFirstInputTimeNsec = 0;
        }
        CamLog.m3d(CameraConstants.TAG, "audio decoder given buffer " + audioOutputBufIndex + " (size=" + this.mAudioBufferInfo.size + ", timestamp = " + String.format("%,d", new Object[]{Long.valueOf(this.mAudioBufferInfo.presentationTimeUs)}) + ")");
        if ((this.mAudioBufferInfo.flags & 4) != 0) {
            CamLog.m3d(CameraConstants.TAG, "audio output EOS");
            this.mAudioOutputDone = true;
        }
        playerCallback.audioDataAvailable(audioDecoder, audioOutputBufIndex, this.mAudioBufferInfo.presentationTimeUs);
        audioDecoder.releaseOutputBuffer(audioOutputBufIndex, false);
    }
}
