package com.lge.camera.managers;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodec.Callback;
import android.media.MediaCodec.CodecException;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.util.CamLog;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

public class LivePhotoEncoder {
    private int FPS = -1;
    private int KEY_FRAME_PER_SEC = -1;
    private boolean endOfStream = false;
    private int mColorFormat;
    private int mDegree = -1;
    private String mDirPath = null;
    private String mFileName = null;
    private int mFrameCount = 0;
    private LivePhotoEncoderListener mListener;
    private MediaCodec mMediaCodec;
    private MediaFormat mMediaFormat;
    private MediaMuxer mMuxer;
    private MediaFormat mOutputFormat;
    private LinkedList<byte[]> mSaveBuffer = null;
    private String mSavedFilePath = null;
    private int[] mSize = null;
    private Handler mThreadHandler;
    private Handler mThreadQuitHandler = new C10431();
    private long ptrusec = 0;
    private int trackIndex;

    /* renamed from: com.lge.camera.managers.LivePhotoEncoder$1 */
    class C10431 extends Handler {
        C10431() {
        }

        public void handleMessage(Message msg) {
            if (msg != null) {
                msg.obj.getLooper().quitSafely();
            }
        }
    }

    /* renamed from: com.lge.camera.managers.LivePhotoEncoder$2 */
    class C10442 extends Callback {
        C10442() {
        }

        public void onInputBufferAvailable(MediaCodec mediaCodec, int i) {
            if (i >= 0 && !LivePhotoEncoder.this.endOfStream && LivePhotoEncoder.this.mSaveBuffer.size() > 0) {
                ByteBuffer buffer = LivePhotoEncoder.this.mMediaCodec.getInputBuffer(i);
                byte[] dt = (byte[]) LivePhotoEncoder.this.mSaveBuffer.removeFirst();
                if (dt != null) {
                    buffer.put(dt);
                    if (LivePhotoEncoder.this.mSaveBuffer.size() == 0) {
                        LivePhotoEncoder.this.endOfStream = true;
                        LivePhotoEncoder.this.mMediaCodec.queueInputBuffer(i, 0, dt.length, LivePhotoEncoder.this.ptrusec, 5);
                        CamLog.m3d(CameraConstants.TAG, "input endOfStream");
                    } else {
                        LivePhotoEncoder.this.mMediaCodec.queueInputBuffer(i, 0, dt.length, LivePhotoEncoder.this.ptrusec, 1);
                    }
                    LivePhotoEncoder.this.ptrusec = LivePhotoEncoder.this.ptrusec + ((long) ((1.0f / ((float) LivePhotoEncoder.this.FPS)) * 1000000.0f));
                    LivePhotoEncoder.this.mFrameCount = LivePhotoEncoder.this.mFrameCount + 1;
                }
            }
        }

        public void onOutputBufferAvailable(MediaCodec mediaCodec, int i, BufferInfo bufferInfo) {
            if (i >= 0) {
                LivePhotoEncoder.this.mMuxer.writeSampleData(LivePhotoEncoder.this.trackIndex, LivePhotoEncoder.this.mMediaCodec.getOutputBuffer(i), bufferInfo);
                LivePhotoEncoder.this.mMediaCodec.releaseOutputBuffer(i, false);
                if (LivePhotoEncoder.this.endOfStream && (bufferInfo.flags & 4) != 0) {
                    LivePhotoEncoder.this.mMuxer.stop();
                    LivePhotoEncoder.this.mMuxer.release();
                    LivePhotoEncoder.this.mMediaCodec.stop();
                    LivePhotoEncoder.this.mMediaCodec.release();
                    CamLog.m3d(CameraConstants.TAG, "finish writing video file. mSavedFilePath = " + LivePhotoEncoder.this.mSavedFilePath);
                    if (LivePhotoEncoder.this.mListener != null) {
                        LivePhotoEncoder.this.mListener.onEncodeAfter(LivePhotoEncoder.this.mSavedFilePath);
                    }
                    CamLog.m3d(CameraConstants.TAG, "-Live photo- output frameCount = " + LivePhotoEncoder.this.mFrameCount);
                    LivePhotoEncoder.this.sendQuitMessage(LivePhotoEncoder.this.mThreadHandler);
                }
            }
        }

        public void onError(MediaCodec mediaCodec, CodecException e) {
            CamLog.m5e(CameraConstants.TAG, "onError = " + e.getMessage());
            if (LivePhotoEncoder.this.mListener != null) {
                LivePhotoEncoder.this.mListener.onError(e);
            }
            LivePhotoEncoder.this.sendQuitMessage(LivePhotoEncoder.this.mThreadHandler);
        }

        public void onOutputFormatChanged(MediaCodec mediaCodec, MediaFormat mediaFormat) {
            CamLog.m7i(CameraConstants.TAG, "onOutputFormatChanged = " + mediaFormat.toString());
            LivePhotoEncoder.this.mOutputFormat = mediaFormat;
            if (LivePhotoEncoder.this.mMuxer != null) {
                LivePhotoEncoder.this.trackIndex = LivePhotoEncoder.this.mMuxer.addTrack(LivePhotoEncoder.this.mOutputFormat);
                LivePhotoEncoder.this.mMuxer.start();
            }
        }
    }

    public interface LivePhotoEncoderListener {
        void onEncodeAfter(String str);

        void onError(CodecException codecException);
    }

    private class LivePhotoSaverThread extends Thread {
        private LivePhotoSaverThread() {
        }

        /* synthetic */ LivePhotoSaverThread(LivePhotoEncoder x0, C10431 x1) {
            this();
        }

        public void run() {
            File dirs = new File(LivePhotoEncoder.this.mDirPath);
            if (!dirs.exists()) {
                dirs.mkdir();
            }
            try {
                Looper.prepare();
                LivePhotoEncoder.this.mThreadHandler = new Handler();
                LivePhotoEncoder.this.initialize();
                LivePhotoEncoder.this.encode();
                Looper.loop();
            } catch (IOException e) {
                e.printStackTrace();
                CamLog.m5e(CameraConstants.TAG, e.getMessage());
            }
        }
    }

    public void setDirPath(String dirPath) {
        this.mDirPath = dirPath;
    }

    public void setFileName(String fileName) {
        this.mFileName = fileName;
    }

    public void setBuffer(LinkedList<byte[]> buffer) {
        this.mSaveBuffer = buffer;
    }

    public void setSize(int[] size) {
        this.mSize = size;
    }

    public void setKeyFramePerSec(int keyFramePerSec) {
        this.KEY_FRAME_PER_SEC = keyFramePerSec;
    }

    public void setFPS(int FPS) {
        this.FPS = FPS;
    }

    public void setOrientationHint(int orientation) {
        this.mDegree = orientation;
    }

    public void setColorFormat(int colorFormat) {
        this.mColorFormat = colorFormat;
    }

    public void start() {
        new LivePhotoSaverThread(this, null).start();
    }

    public void initialize() throws IOException {
        this.mSavedFilePath = this.mDirPath + this.mFileName;
        this.mMuxer = new MediaMuxer(this.mSavedFilePath + ".mp4", 0);
        this.mMuxer.setOrientationHint(this.mDegree);
        this.mMediaCodec = MediaCodec.createByCodecName("OMX.qcom.video.encoder.avc");
        CamLog.m7i(CameraConstants.TAG, "Codec Name = " + this.mMediaCodec.getCodecInfo().getName());
        this.mMediaFormat = MediaFormat.createVideoFormat("video/avc", this.mSize[0], this.mSize[1]);
        CamLog.m3d(CameraConstants.TAG, "size : " + this.mMediaFormat.getInteger("width") + "x" + this.mMediaFormat.getInteger("height"));
        this.mMediaFormat.setInteger(LdbConstants.LDB_FEAT_NAME_VIDEO_BITRATE, 9000000);
        this.mMediaFormat.setInteger("frame-rate", this.FPS);
        this.mMediaFormat.setInteger("color-format", this.mColorFormat);
        float iFrameInterval = 1.0f / ((float) this.KEY_FRAME_PER_SEC);
        CamLog.m3d(CameraConstants.TAG, "-Live Photo Encoder- set KEY_I_FRAME_INTERVAL = " + iFrameInterval);
        this.mMediaFormat.setFloat("i-frame-interval", iFrameInterval);
    }

    public void setListener(LivePhotoEncoderListener listener) {
        this.mListener = listener;
    }

    public void encode() {
        CamLog.m3d(CameraConstants.TAG, "encode");
        this.mFrameCount = 0;
        if (this.mMediaCodec == null) {
            CamLog.m3d(CameraConstants.TAG, "-Live photo- encode(), mMediaCodec is null, return");
            return;
        }
        this.mMediaCodec.setCallback(new C10442());
        this.mMediaCodec.configure(this.mMediaFormat, null, null, 1);
        this.mMediaCodec.start();
    }

    private void sendQuitMessage(Handler handler) {
        if (this.mThreadQuitHandler != null) {
            Message msg = Message.obtain();
            msg.obj = handler;
            this.mThreadQuitHandler.sendMessage(msg);
        }
    }

    public void close() {
        if (this.mMediaCodec != null) {
            this.mMediaCodec.stop();
            this.mMediaCodec.release();
            this.mMediaCodec = null;
        }
        if (this.mMuxer != null) {
            this.mMuxer.stop();
            this.mMuxer.release();
            this.mMuxer = null;
        }
        if (this.mSaveBuffer != null) {
            this.mSaveBuffer.clear();
            this.mSaveBuffer = null;
        }
    }
}
