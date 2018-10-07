package com.lge.camera.app.ext;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.view.Surface;
import android.view.TextureView;
import com.lge.camera.app.ext.MultiViewRecorder.EncoderConfig;
import com.lge.camera.components.MVRecordOutputInfo;
import com.lge.camera.components.MultiViewRecordInputInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class GridViewRenderer extends Thread {
    private static final int COLLAGE_RECORDING_OFF = 0;
    private static final int COLLAGE_RECORDING_ON = 1;
    private boolean isRequiredToFinishAfterSaving = false;
    private MultiViewRecordInputInfo mCollageInputInfo = null;
    private MVRecordOutputInfo mCollageOutputInfo = null;
    private int mCollageState = 0;
    private long mCollageVideoInitTime = 0;
    private int mFrameCount = 0;
    private int mFrameCountPrev = 0;
    private GridViewFrame mGridViewFrameRecord;
    public GridViewRecorder mGridViewRecorder = null;
    private boolean mInit = false;
    private boolean mInitCollage = false;
    private boolean mIsAllFirstFrameCollageReceived = false;
    private Boolean[] mIsSTUpdatedCollage;
    private volatile boolean mIsStopped;
    private EGLSurfaceBase mMVDisplaySurface;
    private long mPrevTimeStamp = 0;
    private int[] mPreviewTexture;
    private STAvailableListenerCollage mSTAvailableListenerCollage;
    private SurfaceTexture[] mSurfaceTexture;
    private SurfaceTexture[] mSurfaceTextureCollage;
    private int mSyncVideoId;
    private int[] mTextureCollage;
    private TextureView mTextureViewMV = null;
    private long mTimePrev = 0;
    private SurfaceTexture mWholeSurfaceTexture;

    private class FrameAvailableListener implements OnFrameAvailableListener {
        int stIdx;
        Boolean[] stUpdated;

        public FrameAvailableListener(Boolean[] updated, int idx) {
            this.stIdx = idx;
            this.stUpdated = updated;
        }

        public void onFrameAvailable(SurfaceTexture st) {
            this.stUpdated[this.stIdx] = Boolean.valueOf(true);
        }
    }

    public interface STAvailableListenerCollage {
        void onSurfaceTextureReady(SurfaceTexture[] surfaceTextureArr);
    }

    public GridViewRenderer(SurfaceTexture surface, String shotmode) {
        this.mWholeSurfaceTexture = surface;
    }

    public void init(TextureView textureView, STAvailableListenerCollage surfaceTextureListenerCollage) {
        this.mTextureViewMV = textureView;
        this.mSTAvailableListenerCollage = surfaceTextureListenerCollage;
        this.mInit = true;
        setRequiredToFinishAfterSaving(false);
        this.mIsStopped = false;
    }

    private boolean waitForNewFrameCollage() {
        int totalCount = this.mCollageInputInfo.getTotalCount();
        if (!this.mIsAllFirstFrameCollageReceived) {
            int imageCount = this.mCollageInputInfo.getImageCount();
            int receivedFirstFrameCount = 0;
            for (int i = 0; i < totalCount; i++) {
                if (this.mIsSTUpdatedCollage[i].booleanValue()) {
                    receivedFirstFrameCount++;
                }
            }
            if (receivedFirstFrameCount >= totalCount - imageCount) {
                this.mIsAllFirstFrameCollageReceived = true;
                return true;
            } else if (System.currentTimeMillis() - this.mCollageVideoInitTime <= CameraConstants.TOAST_LENGTH_MIDDLE_SHORT) {
                return false;
            } else {
                this.mIsAllFirstFrameCollageReceived = true;
                CamLog.m3d(CameraConstants.TAG, "totalCount = " + totalCount + " receivedFirstFrameCount = " + receivedFirstFrameCount);
                return true;
            }
        } else if (!this.mIsSTUpdatedCollage[this.mSyncVideoId].booleanValue()) {
            return false;
        } else {
            CamLog.m3d(CameraConstants.TAG, "mIsSTUpdatedCollage[" + this.mSyncVideoId + "] is updated");
            Arrays.fill(this.mIsSTUpdatedCollage, Boolean.valueOf(false));
            return true;
        }
    }

    public void run() {
        CamLog.m3d(CameraConstants.TAG, "Gridview - RenderThread run() - START");
        if (!this.mInit || this.mWholeSurfaceTexture == null) {
            CamLog.m3d(CameraConstants.TAG, "Gridview - RenderThread is not initialized; just return");
            return;
        }
        this.mMVDisplaySurface = new EGLSurfaceBase(EGL14.eglGetCurrentContext(), new Surface(this.mWholeSurfaceTexture), 0);
        this.mMVDisplaySurface.makeContextCurrent();
        setupGridviewRecorder();
        CamLog.m3d(CameraConstants.TAG, "mIsStopped = " + this.mIsStopped);
        while (!this.mIsStopped) {
            makeCollageVideo();
            try {
                sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!isRequiredToFinishAfterSaving()) {
            clearSurface();
        }
        release();
        CamLog.m3d(CameraConstants.TAG, "Gridview - RenderThread run() - END");
    }

    public void setSyncVideoId(int id) {
        CamLog.m3d(CameraConstants.TAG, "setSyncMovieId = " + id);
        this.mSyncVideoId = id;
    }

    public void setupGridviewRecorder() {
        this.mGridViewFrameRecord = new GridViewFrame(6);
        this.mGridViewRecorder = new GridViewRecorder(this.mGridViewFrameRecord, CameraConstants.MODE_SQUARE_GRID);
    }

    private void clearSurface() {
        CamLog.m3d(CameraConstants.TAG, "clearSurface");
        try {
            this.mMVDisplaySurface.makeContextCurrent();
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GLES20.glClear(16384);
            this.mMVDisplaySurface.swapBuffers();
        } catch (Exception e) {
            CamLog.m3d(CameraConstants.TAG, "Execption occurs on clearSurface");
            e.printStackTrace();
        }
    }

    private synchronized void makeCollageVideo() {
        if (this.mCollageState == 1) {
            int i;
            if (!this.mInitCollage) {
                this.mPrevTimeStamp = 0;
                int totalFile = this.mCollageInputInfo.getTotalCount();
                int[] fileType = this.mCollageInputInfo.getFileType();
                this.mTextureCollage = GridViewFrame.initTextureCollage(totalFile, fileType, this.mCollageInputInfo.getFilePath());
                this.mSurfaceTextureCollage = new SurfaceTexture[totalFile];
                this.mIsSTUpdatedCollage = new Boolean[totalFile];
                Arrays.fill(this.mIsSTUpdatedCollage, Boolean.valueOf(false));
                for (i = 0; i < totalFile; i++) {
                    if (fileType[i] == 2) {
                        this.mSurfaceTextureCollage[i] = new SurfaceTexture(this.mTextureCollage[i]);
                        this.mSurfaceTextureCollage[i].setOnFrameAvailableListener(new FrameAvailableListener(this.mIsSTUpdatedCollage, i));
                    } else if (this.mSurfaceTextureCollage[i] != null) {
                        this.mSurfaceTextureCollage[i].release();
                        this.mSurfaceTextureCollage[i] = null;
                    }
                }
                this.mSTAvailableListenerCollage.onSurfaceTextureReady(this.mSurfaceTextureCollage);
                File outputFile = new File(this.mCollageOutputInfo.getOutputDir() + this.mCollageOutputInfo.getOutputFileName() + this.mCollageOutputInfo.getOutputFileExt());
                this.mGridViewRecorder.startRecording(new EncoderConfig(outputFile, this.mCollageInputInfo.getFileType(), this.mCollageInputInfo.getRecordingDegrees(), this.mCollageOutputInfo, this.mMVDisplaySurface.getEGLContext(), null), 0);
                this.mInitCollage = true;
                this.mCollageVideoInitTime = System.currentTimeMillis();
            }
            if (waitForNewFrameCollage()) {
                this.mGridViewRecorder.makeEncoderContextCurrent();
                for (i = 0; i < this.mCollageInputInfo.getTotalCount(); i++) {
                    if (this.mSurfaceTextureCollage[i] != null) {
                        this.mSurfaceTextureCollage[i].updateTexImage();
                    }
                }
                long curTimeStamp = 0;
                if (this.mSurfaceTextureCollage[this.mSyncVideoId] != null) {
                    curTimeStamp = this.mSurfaceTextureCollage[this.mSyncVideoId].getTimestamp();
                }
                CamLog.m3d(CameraConstants.TAG, "timeDiff = " + (curTimeStamp - this.mPrevTimeStamp));
                recordMVFrameCollage(this.mTextureCollage, curTimeStamp);
                this.mPrevTimeStamp = curTimeStamp;
            }
        }
    }

    private void recordMVFrameCollage(int[] texture, long timeStamp) {
        if (this.mCollageState == 1) {
            this.mGridViewRecorder.frameAvailableCollage(texture, timeStamp);
        }
    }

    public void setCollageImageBitmap(Bitmap[] bm) {
        GridViewFrame gridViewFrame = this.mGridViewFrameRecord;
        GridViewFrame.setImageBitmap(bm);
    }

    public void setCameraId(int[] cameraId) {
        this.mGridViewRecorder.setCameraId(cameraId);
    }

    private void showFrameRate() {
        if (this.mTimePrev == 0) {
            this.mTimePrev = System.currentTimeMillis();
        }
        this.mFrameCount++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.mTimePrev > 1000) {
            float frameRate = (((float) (this.mFrameCount - this.mFrameCountPrev)) / ((float) (currentTime - this.mTimePrev))) * 1000.0f;
            this.mFrameCountPrev = this.mFrameCount;
            this.mTimePrev = currentTime;
            CamLog.m3d(CameraConstants.TAG, "draw frame rate = " + frameRate);
        }
    }

    public synchronized void stopRecorderCollage() {
        CamLog.m3d(CameraConstants.TAG, "stopRecorderCollage");
        this.mGridViewRecorder.stopRecording();
        this.mCollageState = 0;
        if (this.mTextureCollage != null) {
            CamLog.m3d(CameraConstants.TAG, "-null- deleteTexture mTextureCollage");
            GridViewFrame.deleteTexture(this.mTextureCollage, this.mTextureCollage.length);
            this.mTextureCollage = null;
        }
        if (this.mSurfaceTextureCollage != null) {
            for (int i = 0; i < this.mSurfaceTextureCollage.length; i++) {
                if (this.mSurfaceTextureCollage[i] != null) {
                    this.mSurfaceTextureCollage[i].release();
                    CamLog.m3d(CameraConstants.TAG, "-null- release mSurfaceTextureCollage : " + i);
                    this.mSurfaceTextureCollage[i] = null;
                }
            }
            this.mSurfaceTextureCollage = null;
            CamLog.m3d(CameraConstants.TAG, "-null- mSurfaceTextureCollage = null");
        }
        this.mIsSTUpdatedCollage = null;
        CamLog.m3d(CameraConstants.TAG, "-null- mIsSTUpdatedCollage = null");
        this.mCollageInputInfo = null;
        this.mCollageOutputInfo = null;
        this.mInitCollage = false;
    }

    public synchronized void startRecorderCollage(MultiViewRecordInputInfo inputInfo, MVRecordOutputInfo outputInfo) {
        CamLog.m3d(CameraConstants.TAG, "startRecorderCollage");
        this.mCollageInputInfo = inputInfo;
        this.mCollageOutputInfo = outputInfo;
        this.mIsAllFirstFrameCollageReceived = false;
        this.mGridViewRecorder.setLatchForAudioData();
        this.mCollageState = 1;
    }

    private void release() {
        CamLog.m3d(CameraConstants.TAG, "-Gridview- release()");
        if (this.mMVDisplaySurface != null) {
            this.mMVDisplaySurface.release();
            this.mMVDisplaySurface = null;
        }
        if (this.mPreviewTexture != null) {
            CamLog.m3d(CameraConstants.TAG, "-null- deleteTexture mPreviewTexture");
            GridViewFrame.deleteTexture(this.mPreviewTexture, this.mPreviewTexture.length);
            this.mPreviewTexture = null;
        }
        if (this.mTextureCollage != null) {
            CamLog.m3d(CameraConstants.TAG, "-null- deleteTexture mTextureCollage");
            GridViewFrame.deleteTexture(this.mTextureCollage, this.mTextureCollage.length);
            this.mTextureCollage = null;
        }
        if (this.mSurfaceTexture != null) {
            for (int i = 0; i < this.mSurfaceTexture.length; i++) {
                if (this.mSurfaceTexture[i] != null) {
                    this.mSurfaceTexture[i].release();
                    CamLog.m3d(CameraConstants.TAG, "-null- release mSurfaceTexture : " + i);
                    this.mSurfaceTexture[i] = null;
                }
            }
        }
        this.mSurfaceTexture = null;
        if (this.mTextureViewMV != null) {
            this.mTextureViewMV.removeCallbacks(this);
            this.mTextureViewMV = null;
        }
        this.mSTAvailableListenerCollage = null;
        this.mGridViewRecorder = null;
    }

    public void stopThread() {
        CamLog.m3d(CameraConstants.TAG, "-th- stopThread");
        this.mIsStopped = true;
    }

    public void setRequiredToFinishAfterSaving(boolean save) {
        CamLog.m3d(CameraConstants.TAG, "-th- setRequiredToFinishAfterSaving");
        this.isRequiredToFinishAfterSaving = save;
    }

    public boolean isRequiredToFinishAfterSaving() {
        return this.isRequiredToFinishAfterSaving;
    }

    public void setAudioData(ByteBuffer audioMixed, long timeStamp) {
        if (this.mGridViewRecorder == null || audioMixed == null) {
            CamLog.m3d(CameraConstants.TAG, "Exit it because of null object");
        } else {
            this.mGridViewRecorder.audioDataAvailalbe(audioMixed, timeStamp);
        }
    }
}
