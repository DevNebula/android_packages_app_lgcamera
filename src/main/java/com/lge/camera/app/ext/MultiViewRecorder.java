package com.lge.camera.app.ext;

import android.opengl.EGLContext;
import android.view.Surface;
import com.lge.camera.app.VideoRecorder;
import com.lge.camera.components.MVRecordOutputInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class MultiViewRecorder {
    protected EGLSurfaceBase mCodecInputSurface = null;
    protected EGLContext mEGLSharedContext;
    protected boolean mIsSingleViewScreenRecording;
    protected CountDownLatch mLatch;
    protected int mLayoutType;
    private MultiViewFrameBase mMultiViewFrameRecord;
    protected String mShotMode;
    protected Surface mSurface;
    protected VideoEncoderCore mVideoEncoder;

    public static class EncoderConfig {
        final int mAudioBitRate;
        final int mAudioChannelCount;
        final String mAudioMimeType;
        final int mAudioSampleRate;
        final EGLContext mEglContext;
        final int[] mInputFileType;
        final File mOutputFile;
        final ArrayList<Integer> mRecordingDegrees;
        final Surface mSurface;
        final int mVideoBitRate;
        final int mVideoFrameRate;
        final int mVideoHeight;
        final int mVideoIFrameInterval;
        final String mVideoMimeType;
        final int mVideoOrientationHint;
        final int mVideoWidth;

        public EncoderConfig(File outputFile, int[] inputFileType, ArrayList<Integer> recordingDegrees, MVRecordOutputInfo outputInfo, EGLContext sharedEglContext, Surface surface) {
            this.mOutputFile = outputFile;
            this.mInputFileType = inputFileType;
            this.mRecordingDegrees = recordingDegrees;
            this.mVideoMimeType = outputInfo.getVideoMimeType();
            this.mVideoWidth = outputInfo.getVideoWidth();
            this.mVideoHeight = outputInfo.getVideoHeight();
            this.mVideoBitRate = outputInfo.getVideoBitRate();
            this.mVideoFrameRate = outputInfo.getVideoFrameRate();
            this.mVideoIFrameInterval = outputInfo.getVideoIFrameInterval();
            this.mVideoOrientationHint = outputInfo.getVideoOrientationHint();
            this.mAudioMimeType = outputInfo.getAudioMimeType();
            this.mAudioSampleRate = outputInfo.getAudioSampleRate();
            this.mAudioChannelCount = outputInfo.getAudioChannelCount();
            this.mAudioBitRate = outputInfo.getAudioBitRate();
            this.mEglContext = sharedEglContext;
            this.mSurface = surface;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("EncoderConfig: ");
            builder.append(this.mVideoWidth + "x" + this.mVideoHeight + " @" + this.mVideoBitRate + " ");
            builder.append(" (" + this.mVideoOrientationHint + ") ");
            builder.append("to 'EGLContext = " + this.mEglContext + "' ");
            builder.append("FileName = ");
            if (this.mOutputFile != null) {
                builder.append(this.mOutputFile.toString());
            } else {
                builder.append(CameraConstants.NULL);
            }
            return builder.toString();
        }
    }

    public MultiViewRecorder(MultiViewFrameBase frameLayout, String shotMode) {
        this.mMultiViewFrameRecord = frameLayout;
        this.mShotMode = shotMode;
    }

    public synchronized void startRecording(EncoderConfig config, int degree) {
        CamLog.m3d(CameraConstants.TAG, "Encoder: startRecording()");
        prepareEncoder(config, degree);
    }

    public synchronized void stopRecording() {
        CamLog.m3d(CameraConstants.TAG, "handleStopRecording");
        if (this.mVideoEncoder != null) {
            this.mVideoEncoder.drainVideoEncoder(true);
            this.mVideoEncoder.flushAudioBuffer();
            this.mVideoEncoder.feedAudioEncoder(null, 0);
            this.mVideoEncoder.drainAudioEncoder(true);
        }
        releaseEncoder();
    }

    public synchronized void frameAvailable(int[] texture, float[][] texMatrix) {
        synchronized (VideoRecorder.sSynchRecordStop) {
            if (VideoRecorder.isRecording()) {
                try {
                    this.mCodecInputSurface.makeContextCurrent();
                    this.mMultiViewFrameRecord.drawFrame(texture, texMatrix);
                    this.mCodecInputSurface.swapBuffers();
                } catch (Exception e) {
                    CamLog.m11w(CameraConstants.TAG, "rendering fail");
                }
            }
        }
    }

    public synchronized void frameAvailableCollage(int[] texture, long timestamp) {
        if (timestamp == 0) {
            CamLog.m11w(CameraConstants.TAG, "HEY: got SurfaceTexture with timestamp of zero");
        } else {
            try {
                this.mCodecInputSurface.makeContextCurrent();
                this.mVideoEncoder.drainVideoEncoder(false);
                this.mVideoEncoder.drainAudioEncoder(false);
                drawCollageFrame(texture);
                CamLog.m3d(CameraConstants.TAG, "frameAvailableCollage timestamp = " + timestamp);
                this.mCodecInputSurface.setPresentationTime(timestamp);
                this.mCodecInputSurface.swapBuffers();
            } catch (Exception e) {
                CamLog.m11w(CameraConstants.TAG, "rendering fail");
            }
        }
        return;
    }

    protected void drawCollageFrame(int[] texture) {
        this.mMultiViewFrameRecord.drawFrameCollage(texture);
    }

    private void prepareEncoder(EncoderConfig config, int degree) {
        this.mEGLSharedContext = config.mEglContext;
        this.mSurface = config.mSurface;
        if (config.mOutputFile != null) {
            try {
                this.mVideoEncoder = new VideoEncoderCore(config, this.mShotMode);
                if (this.mLatch != null) {
                    this.mLatch.countDown();
                    CamLog.m3d(CameraConstants.TAG, "MultiView - mLatch countdown");
                }
                this.mSurface = this.mVideoEncoder.getInputSurface();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
        this.mVideoEncoder = null;
        this.mCodecInputSurface = new EGLSurfaceBase(this.mEGLSharedContext, this.mSurface, 1);
        this.mCodecInputSurface.makeContextCurrent();
        setupRecorder(config, degree);
    }

    protected void setupRecorder(EncoderConfig config, int degree) {
        if (this.mMultiViewFrameRecord == null) {
            return;
        }
        if (config.mOutputFile != null) {
            this.mMultiViewFrameRecord.init(this.mLayoutType, config.mInputFileType, config.mRecordingDegrees, degree, this.mIsSingleViewScreenRecording);
            return;
        }
        this.mMultiViewFrameRecord.init(this.mLayoutType, degree, this.mIsSingleViewScreenRecording);
    }

    private void releaseEncoder() {
        CamLog.m3d(CameraConstants.TAG, "releaseEncoder()");
        if (this.mVideoEncoder != null) {
            this.mVideoEncoder.release();
            this.mVideoEncoder = null;
        }
        if (this.mCodecInputSurface != null) {
            this.mCodecInputSurface.release();
            this.mCodecInputSurface = null;
        }
        this.mSurface = null;
        this.mEGLSharedContext = null;
    }

    public void updateLayoutForRecording(int viewType) {
        this.mLayoutType = viewType;
    }

    public void makeEncoderContextCurrent() {
        this.mCodecInputSurface.makeContextCurrent();
    }

    public void audioDataAvailalbe(ByteBuffer audioMixed, long timeStamp) {
        CamLog.m3d(CameraConstants.TAG, "audioDataAvailalbe");
        try {
            if (this.mLatch != null) {
                CamLog.m3d(CameraConstants.TAG, "waiting for mVideoEncoder - START");
                this.mLatch.await();
                CamLog.m3d(CameraConstants.TAG, "waiting for mVideoEncoder - END");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.mVideoEncoder.feedAudioEncoder(audioMixed, timeStamp);
        this.mLatch = null;
    }

    public void setLatchForAudioData() {
        this.mLatch = new CountDownLatch(1);
    }

    public void setSingleviewRecording(boolean singleViewRecording) {
        CamLog.m3d(CameraConstants.TAG, "-single- setSingleviewRecording = " + singleViewRecording);
        this.mIsSingleViewScreenRecording = singleViewRecording;
    }
}
