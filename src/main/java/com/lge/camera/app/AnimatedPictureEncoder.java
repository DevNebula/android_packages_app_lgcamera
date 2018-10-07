package com.lge.camera.app;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import com.lge.camera.components.AnimatedPictureOutputInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorConverter;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class AnimatedPictureEncoder {
    private static final boolean CHECK_CODEC_NAME = false;
    private static final String MIME_TYPE = "video/avc";
    private static final String TAG = "CameraApp";
    private static final int TIMEOUT_USEC = 10000;
    private int mBitRate = 4000000;
    private byte[][] mEncodedDataArr;
    private MediaCodec mEncoder;
    private int mFrameRate = 30;
    private int mHeight = CameraConstantsEx.HD_SCREEN_RESOLUTION;
    private int mIFrameInterval = 1;
    private MediaMuxer mMuxer;
    private boolean mMuxerStarted;
    private File mOutputFile = null;
    private int mTimeLength = 2;
    private int mTrackIndex;
    private int mVideoOrientation = 0;
    private int mWidth = 1280;

    public AnimatedPictureEncoder(ArrayList<Bitmap> frames, File outputFile, AnimatedPictureOutputInfo info) {
        CamLog.m3d("CameraApp", "init AnimatedPictureEncoder - start");
        this.mWidth = info.getVideoWidth();
        this.mHeight = info.getVideoHeight();
        this.mFrameRate = info.getVideoFrameRate();
        this.mBitRate = info.getVideoBitrate();
        this.mIFrameInterval = info.getVideoIFrameInterval();
        this.mTimeLength = info.getVideoTimeLength();
        this.mVideoOrientation = info.getVideoOrientationHint();
        if (frames == null) {
            CamLog.m3d("CameraApp", "Input frames are null, so return");
            return;
        }
        this.mOutputFile = outputFile;
        this.mEncodedDataArr = new byte[frames.size()][];
        for (int i = 0; i < frames.size(); i++) {
            this.mEncodedDataArr[i] = new byte[(((this.mWidth * this.mHeight) * 3) / 2)];
            getNV21(this.mEncodedDataArr[i], this.mWidth, this.mHeight, (Bitmap) frames.get(i));
            ((Bitmap) frames.get(i)).recycle();
        }
        CamLog.m3d("CameraApp", "init AnimatedPictureEncoder - end");
    }

    public AnimatedPictureEncoder(byte[][] data, File outputFile, AnimatedPictureOutputInfo info) {
        this.mWidth = info.getVideoWidth();
        this.mHeight = info.getVideoHeight();
        this.mFrameRate = info.getVideoFrameRate();
        this.mBitRate = info.getVideoBitrate();
        this.mIFrameInterval = info.getVideoIFrameInterval();
        this.mTimeLength = info.getVideoTimeLength();
        this.mVideoOrientation = info.getVideoOrientationHint();
        this.mOutputFile = outputFile;
        this.mEncodedDataArr = data;
    }

    public boolean encodeVideoFromBuffer() throws Exception {
        CamLog.m3d("CameraApp", "encodeVideoFromBuffer - start");
        try {
            CamLog.m3d("CameraApp", "found colorFormat: " + 21);
            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, this.mWidth, this.mHeight);
            format.setInteger("color-format", 21);
            format.setInteger(LdbConstants.LDB_FEAT_NAME_VIDEO_BITRATE, this.mBitRate);
            format.setInteger("frame-rate", this.mFrameRate);
            format.setInteger("i-frame-interval", this.mIFrameInterval);
            if (ModelProperties.isMTKChipset()) {
                this.mEncoder = MediaCodec.createByCodecName("OMX.MTK.VIDEO.ENCODER.AVC");
            } else {
                this.mEncoder = MediaCodec.createByCodecName("OMX.qcom.video.encoder.avc");
            }
            this.mEncoder.configure(format, null, null, 1);
            CamLog.m5e("CameraApp", "MediaCodec start");
            this.mEncoder.start();
            if (this.mOutputFile != null) {
                this.mMuxer = new MediaMuxer(this.mOutputFile.getAbsolutePath(), 0);
                this.mMuxer.setOrientationHint(this.mVideoOrientation);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            CamLog.m5e("CameraApp", "fail!!");
        } catch (Throwable th) {
            if (this.mEncoder != null) {
                this.mEncoder.stop();
                this.mEncoder.release();
            }
            if (this.mMuxer != null) {
                this.mMuxer.stop();
                this.mMuxer.release();
            }
            CamLog.m5e("CameraApp", "MediaCodec end");
        }
        boolean result = doEncodeVideoFromBuffer(this.mEncoder, 21);
        if (this.mEncoder != null) {
            this.mEncoder.stop();
            this.mEncoder.release();
        }
        if (this.mMuxer != null) {
            this.mMuxer.stop();
            this.mMuxer.release();
        }
        CamLog.m5e("CameraApp", "MediaCodec end");
        return result;
    }

    private void setRecordingType(String type) {
        CamLog.m3d("CameraApp", "-Popout- setRecordingType");
        try {
            Class.forName("android.media.MediaMuxer").getDeclaredMethod("setRecordingType", new Class[]{String.class}).invoke(this.mMuxer, new Object[]{type});
        } catch (Exception e) {
            CamLog.m6e("CameraApp", "-Multiview- setRecordingType invoke error : ", e);
        }
    }

    private MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (codecInfo.isEncoder()) {
                String[] types = codecInfo.getSupportedTypes();
                for (String equalsIgnoreCase : types) {
                    if (equalsIgnoreCase.equalsIgnoreCase(mimeType)) {
                        return codecInfo;
                    }
                }
                continue;
            }
        }
        return null;
    }

    private boolean doEncodeVideoFromBuffer(MediaCodec encoder, int encoderColorFormat) {
        if (this.mEncodedDataArr == null) {
            return false;
        }
        ByteBuffer[] encoderInputBuffers = encoder.getInputBuffers();
        BufferInfo info = new BufferInfo();
        int generateIndex = 0;
        byte[] frameData = new byte[(((this.mWidth * this.mHeight) * 3) / 2)];
        boolean inputDone = false;
        while (!inputDone) {
            int inputBufIndex = encoder.dequeueInputBuffer(10000);
            if (inputBufIndex >= 0) {
                long ptsUsec = computePresentationTime(generateIndex);
                if (generateIndex >= this.mEncodedDataArr.length * this.mTimeLength) {
                    encoder.queueInputBuffer(inputBufIndex, 0, 0, ptsUsec, 4);
                    inputDone = true;
                    drainEncoder(true, info);
                } else {
                    try {
                        generateFrame(generateIndex, encoderColorFormat, frameData);
                    } catch (Exception e) {
                        CamLog.m7i("CameraApp", "meet a different type of image");
                        Arrays.fill(frameData, (byte) 0);
                    }
                    CamLog.m7i("CameraApp", "generateIndex: " + generateIndex + ", size: " + this.mEncodedDataArr.length);
                    ByteBuffer inputBuf = encoderInputBuffers[inputBufIndex];
                    inputBuf.clear();
                    inputBuf.put(frameData);
                    encoder.queueInputBuffer(inputBufIndex, 0, frameData.length, ptsUsec, 0);
                    drainEncoder(false, info);
                }
                generateIndex++;
            } else {
                CamLog.m7i("CameraApp", "input buffer not available");
            }
        }
        return true;
    }

    private void drainEncoder(boolean endOfStream, BufferInfo mBufferInfo) {
        if (endOfStream) {
            try {
                this.mEncoder.signalEndOfInputStream();
            } catch (Exception e) {
                CamLog.m11w("CameraApp", "signalEndOfInputStreem fail");
            }
        }
        ByteBuffer[] encoderOutputBuffers = this.mEncoder.getOutputBuffers();
        while (true) {
            int encoderStatus = this.mEncoder.dequeueOutputBuffer(mBufferInfo, 10000);
            if (encoderStatus == -1) {
                CamLog.m7i("CameraApp", "INFO_TRY_AGAIN_LATER");
                if (endOfStream) {
                    CamLog.m7i("CameraApp", "no output available, spinning to await EOS");
                } else {
                    return;
                }
            } else if (encoderStatus == -3) {
                CamLog.m7i("CameraApp", "INFO_OUTPUT_BUFFERS_CHANGED");
                encoderOutputBuffers = this.mEncoder.getOutputBuffers();
            } else if (encoderStatus == -2) {
                CamLog.m7i("CameraApp", "INFO_OUTPUT_FORMAT_CHANGED");
                if (this.mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = this.mEncoder.getOutputFormat();
                CamLog.m7i("CameraApp", "encoder output format changed: " + newFormat);
                if (this.mMuxer != null) {
                    this.mTrackIndex = this.mMuxer.addTrack(newFormat);
                    setRecordingType(CameraConstants.VIDEO_ANIMATED_POOPUT_TYPE);
                    this.mMuxer.start();
                    this.mMuxerStarted = true;
                }
            } else if (encoderStatus < 0) {
                CamLog.m7i("CameraApp", "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
            } else if (doDrainEncoder(endOfStream, mBufferInfo, encoderOutputBuffers, encoderStatus)) {
                return;
            }
        }
    }

    private boolean doDrainEncoder(boolean endOfStream, BufferInfo mBufferInfo, ByteBuffer[] encoderOutputBuffers, int encoderStatus) {
        CamLog.m7i("CameraApp", "else..");
        ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
        if (encodedData == null) {
            throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
        }
        if ((mBufferInfo.flags & 2) != 0) {
            CamLog.m3d("CameraApp", "ignoring BUFFER_FLAG_CODEC_CONFIG");
            mBufferInfo.size = 0;
        }
        if (mBufferInfo.size != 0) {
            if (this.mMuxerStarted) {
                encodedData.position(mBufferInfo.offset);
                encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                CamLog.m3d("CameraApp", "BufferInfo: " + mBufferInfo.offset + "," + mBufferInfo.size + "," + mBufferInfo.presentationTimeUs);
                try {
                    if (this.mMuxer != null) {
                        this.mMuxer.writeSampleData(this.mTrackIndex, encodedData, mBufferInfo);
                    }
                } catch (Exception e) {
                    CamLog.m7i("CameraApp", "Too many frames");
                }
            } else {
                throw new RuntimeException("muxer hasn't started");
            }
        }
        this.mEncoder.releaseOutputBuffer(encoderStatus, false);
        if ((mBufferInfo.flags & 4) == 0) {
            return false;
        }
        if (endOfStream) {
            CamLog.m7i("CameraApp", "end of stream reached");
        } else {
            CamLog.m7i("CameraApp", "reached end of stream unexpectedly");
        }
        return true;
    }

    private void generateFrame(int frameIndex, int colorFormat, byte[] frameData) {
        if (this.mEncodedDataArr != null) {
            int maxBaseFrameCount = this.mEncodedDataArr.length;
            int normalizedIndex = frameIndex % (maxBaseFrameCount * 2);
            int index = normalizedIndex;
            if (normalizedIndex >= maxBaseFrameCount) {
                index = (this.mEncodedDataArr.length - (normalizedIndex % maxBaseFrameCount)) - 1;
            }
            System.arraycopy(this.mEncodedDataArr[index], 0, frameData, 0, this.mEncodedDataArr[index].length);
        }
    }

    private void getNV21(byte[] frameData, int inputWidth, int inputHeight, Bitmap scaled) {
        int[] argb = new int[(inputWidth * inputHeight)];
        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);
        ColorConverter.RGBToYuv420sp(argb, frameData, inputWidth, inputHeight);
    }

    private long computePresentationTime(int frameIndex) {
        return 132 + ((1000000 * ((long) frameIndex)) / ((long) this.mFrameRate));
    }

    public void unbind() {
    }
}
