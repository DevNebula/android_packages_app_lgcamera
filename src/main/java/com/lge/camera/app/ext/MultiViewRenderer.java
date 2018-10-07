package com.lge.camera.app.ext;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import com.lge.camera.app.ext.MultiViewRecorder.EncoderConfig;
import com.lge.camera.components.MVRecordOutputInfo;
import com.lge.camera.components.MultiViewRecordInputInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.DebugUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MultiViewRenderer extends Thread {
    private static final int COLLAGE_IMAGE_ON = 2;
    private static final int COLLAGE_RECORDING_OFF = 0;
    private static final int COLLAGE_RECORDING_ON = 1;
    private static final int MAX_NOT_RECEIVED_FRAME_COUNT = 80;
    private boolean isRequiredToFinishAfterSaving = false;
    private int mCaptureRotationDegree;
    private MultiViewRecordInputInfo mCollageInputInfo = null;
    private MVRecordOutputInfo mCollageOutputInfo = null;
    private int mCollageState = 0;
    private long mCollageVideoInitTime = 0;
    private FrameAvailableListener[] mFrameAvailableListener;
    private GLFrameCaptureListener mFrameCaptureListener;
    private int mFrameCount = 0;
    private int mFrameCountPrev = 0;
    private long mFrontTimeDiff = 0;
    private IMultiViewModule mGet = null;
    private boolean mInit = false;
    private boolean mInitCollage = false;
    private Surface mInputRecordSurface = null;
    private boolean mIsAllFirstFrameCollageReceived = false;
    private boolean mIsAllFirstFrameReceived = false;
    private boolean mIsCollageImgOn = false;
    private boolean mIsInitRecording = false;
    private boolean mIsRecordStarted = false;
    private boolean mIsRecordStopped = false;
    private Boolean[] mIsSTUpdated;
    private Boolean[] mIsSTUpdatedCollage;
    private boolean mIsSingleViewScreenRecording;
    private volatile boolean mIsStopped;
    private EGLSurfaceBase mMVDisplaySurface;
    private MultiViewFrameBase mMultiViewFrame;
    private MultiViewFrameBase mMultiViewFrameRecord;
    public MultiViewRecorder mMultiViewRecorder = null;
    private int mNotReceivedFrameCount = 0;
    private int mOrientationHint = 0;
    private long mPrevCamUpdateTime = 0;
    private long mPrevTimeStamp = 0;
    private long mPreviewReceived = 0;
    private int[] mPreviewTexture;
    private STAvailableListener mSTAvailableListener;
    private STAvailableListenerCollage mSTAvailableListenerCollage;
    private String mShotMode;
    private SurfaceTexture[] mSurfaceTexture;
    private SurfaceTexture[] mSurfaceTextureCollage;
    private int mSyncVideoId;
    private boolean mTakeGLViewRequested = false;
    private int[] mTextureCollage;
    private TextureView mTextureViewMV = null;
    private long mTimePrev = 0;
    private float[][] mTmpMatrix = ((float[][]) Array.newInstance(Float.TYPE, new int[]{3, 16}));
    private SurfaceTexture mWholeSurfaceTexture;

    public interface GLFrameCaptureListener {
        void onGLFrameCaptured(byte[] bArr);

        Bitmap onPreGLFrameCapture(Bitmap bitmap);
    }

    public interface STAvailableListener {
        void onSurfaceTextureReady(SurfaceTexture[] surfaceTextureArr);
    }

    public interface STAvailableListenerCollage {
        void onSurfaceTextureReady(SurfaceTexture[] surfaceTextureArr);
    }

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

    public MultiViewRenderer(SurfaceTexture surface, String shotmode) {
        this.mWholeSurfaceTexture = surface;
        this.mShotMode = shotmode;
    }

    public void init(TextureView textureView, GLFrameCaptureListener captureListener, STAvailableListener surfaceTextureListener, STAvailableListenerCollage surfaceTextureListenerCollage, int captureRotationDegree, int cameraId, int layoutIndex, IMultiViewModule multiviewModule) {
        this.mTextureViewMV = textureView;
        this.mFrameCaptureListener = captureListener;
        this.mSTAvailableListener = surfaceTextureListener;
        this.mSTAvailableListenerCollage = surfaceTextureListenerCollage;
        this.mCaptureRotationDegree = captureRotationDegree;
        this.mInit = true;
        this.mGet = multiviewModule;
        if (CameraConstants.MODE_MULTIVIEW.equals(this.mShotMode) || CameraConstants.MODE_SQUARE_SPLICE.equals(this.mShotMode)) {
            this.mMultiViewFrame = new MultiViewFrame(1, layoutIndex, this.mGet);
        } else if (CameraConstants.MODE_SNAP.equals(this.mShotMode)) {
            this.mMultiViewFrame = new SnapMovieFrame(1, layoutIndex, this.mGet);
        }
        setRequiredToFinishAfterSaving(false);
        this.mIsStopped = false;
    }

    private boolean waitForNewFrame() {
        if (this.mCollageState != 1 && isSurfaceTextureUpdated()) {
            return true;
        }
        return false;
    }

    private boolean isSurfaceTextureUpdated() {
        if (this.mIsStopped) {
            return true;
        }
        if (!isAllFirstFrameReceived()) {
            return false;
        }
        if (multiviewFrameUpdate()) {
            if (this.mPreviewReceived == 0) {
                this.mPreviewReceived = System.currentTimeMillis();
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.mPreviewReceived > 1000) {
                this.mPreviewReceived = currentTime;
                CamLog.m3d(CameraConstants.TAG, "frame received ");
            }
            this.mNotReceivedFrameCount = 0;
            return true;
        }
        this.mNotReceivedFrameCount++;
        return false;
    }

    private boolean multiviewFrameUpdate() {
        long time = System.currentTimeMillis();
        if (this.mIsSTUpdated[0].booleanValue()) {
            this.mPrevCamUpdateTime = time;
            return true;
        }
        if (this.mIsSTUpdated[1].booleanValue()) {
            this.mFrontTimeDiff = time - this.mPrevCamUpdateTime;
            if (this.mFrontTimeDiff > 70) {
                this.mPrevCamUpdateTime = time;
                CamLog.m3d(CameraConstants.TAG, "-draw- updated by front preview mFrontTimeDiff = " + this.mFrontTimeDiff);
                return true;
            }
        }
        return false;
    }

    private boolean isAllFirstFrameReceived() {
        if (this.mIsAllFirstFrameReceived) {
            return true;
        }
        int recivedFirstFrameCount = 0;
        for (int i = 0; i < this.mPreviewTexture.length; i++) {
            if (this.mIsSTUpdated[i].booleanValue()) {
                recivedFirstFrameCount++;
            }
        }
        if (recivedFirstFrameCount < 2) {
            return false;
        }
        this.mIsAllFirstFrameReceived = true;
        this.mGet.multiviewFrameReady();
        CamLog.m3d(CameraConstants.TAG, "-c1- return true recivedFirstFrameCount = " + recivedFirstFrameCount);
        return true;
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
                CamLog.m3d(CameraConstants.TAG, "timeout(3sec) for waiting all video play - start collage video");
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

    public void setSyncVideoId(int id) {
        CamLog.m3d(CameraConstants.TAG, "setSyncMovieId = " + id);
        this.mSyncVideoId = id;
    }

    private void createFrameAvailableListener() {
        this.mFrameAvailableListener = new FrameAvailableListener[this.mPreviewTexture.length];
        for (int i = 0; i < this.mPreviewTexture.length; i++) {
            this.mFrameAvailableListener[i] = new FrameAvailableListener(this.mIsSTUpdated, i);
        }
    }

    public void run() {
        CamLog.m3d(CameraConstants.TAG, "MultiView - RenderThread run() - START");
        if (!this.mInit || this.mWholeSurfaceTexture == null) {
            CamLog.m3d(CameraConstants.TAG, "MultiView - RenderThread is not initialized; just return");
            return;
        }
        int i;
        this.mMVDisplaySurface = new EGLSurfaceBase(EGL14.eglGetCurrentContext(), new Surface(this.mWholeSurfaceTexture), 0);
        this.mMVDisplaySurface.makeContextCurrent();
        this.mPreviewTexture = MultiViewFrameBase.initPreviewTexture(3);
        this.mSurfaceTexture = new SurfaceTexture[this.mPreviewTexture.length];
        this.mIsSTUpdated = new Boolean[this.mPreviewTexture.length];
        Arrays.fill(this.mIsSTUpdated, Boolean.valueOf(false));
        createFrameAvailableListener();
        for (i = 0; i < this.mPreviewTexture.length; i++) {
            this.mSurfaceTexture[i] = new SurfaceTexture(this.mPreviewTexture[i]);
            this.mSurfaceTexture[i].setOnFrameAvailableListener(this.mFrameAvailableListener[i]);
        }
        this.mSTAvailableListener.onSurfaceTextureReady(this.mSurfaceTexture);
        this.mMultiViewFrame.init(this.mGet.getOrientationDegree());
        this.mIsAllFirstFrameReceived = false;
        setupMultiViewRecorder();
        CamLog.m3d(CameraConstants.TAG, "mIsStopped = " + this.mIsStopped);
        while (!this.mIsStopped) {
            if ((waitForNewFrame() && !this.mGet.isPaused()) || checkPreviewStoppedAndResetFrameListener()) {
                this.mMVDisplaySurface.makeContextCurrent();
                for (i = 0; i < 2; i++) {
                    if (this.mSurfaceTexture[i] != null) {
                        this.mSurfaceTexture[i].updateTexImage();
                        this.mIsSTUpdated[i] = Boolean.valueOf(false);
                        this.mSurfaceTexture[i].getTransformMatrix(this.mTmpMatrix[i]);
                    }
                }
                recordMVFrame();
                displayMVFrame();
                takeMVFrame();
            }
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
        CamLog.m3d(CameraConstants.TAG, "MultiView - RenderThread run() - END");
    }

    public void setupMultiViewRecorder() {
        if (CameraConstants.MODE_MULTIVIEW.equals(this.mShotMode) || CameraConstants.MODE_SQUARE_SPLICE.equals(this.mShotMode)) {
            if (this.mGet.isMultiviewFrameShot()) {
                CamLog.m3d(CameraConstants.TAG, "-rec- setupMultiViewRecorder multiview frameshot");
                this.mMultiViewFrameRecord = new MultiViewFrame(6, this.mGet);
            } else {
                CamLog.m3d(CameraConstants.TAG, "-rec- setupMultiViewRecorder multiview wholeshot");
                this.mMultiViewFrameRecord = new MultiViewFrame(2, this.mGet);
            }
        } else if (CameraConstants.MODE_SNAP.equals(this.mShotMode)) {
            this.mMultiViewFrameRecord = new SnapMovieFrame(2, this.mGet);
        }
        this.mMultiViewRecorder = new MultiViewRecorder(this.mMultiViewFrameRecord, this.mShotMode);
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
                this.mTextureCollage = MultiViewFrame.initTextureCollage(totalFile, fileType, this.mCollageInputInfo.getFilePath());
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
                this.mMultiViewRecorder.startRecording(new EncoderConfig(outputFile, this.mCollageInputInfo.getFileType(), this.mCollageInputInfo.getRecordingDegrees(), this.mCollageOutputInfo, this.mMVDisplaySurface.getEGLContext(), null), this.mGet.getOrientationDegree());
                this.mInitCollage = true;
                this.mCollageVideoInitTime = System.currentTimeMillis();
            }
            if (waitForNewFrameCollage()) {
                this.mMultiViewRecorder.makeEncoderContextCurrent();
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

    public void takeCollageImage(MultiViewRecordInputInfo inputInfo, CollagePictureTakenListener listener) {
        this.mCollageInputInfo = inputInfo;
        this.mCollageState = 2;
    }

    private synchronized void displayMVFrame() {
        if (this.isRequiredToFinishAfterSaving) {
            CamLog.m3d(CameraConstants.TAG, "isRequiredToFinishAfterSaving is true return");
        } else {
            try {
                this.mMVDisplaySurface.makeContextCurrent();
                this.mMultiViewFrame.drawFrame(this.mPreviewTexture, this.mTmpMatrix);
                this.mMVDisplaySurface.swapBuffers();
            } catch (Exception e) {
                CamLog.m11w(CameraConstants.TAG, "rendering fail");
            }
            showFrameRate();
        }
        return;
    }

    private synchronized void recordMVFrame() {
        if (this.mIsRecordStarted) {
            if (!this.mIsInitRecording) {
                this.mIsInitRecording = true;
                if (this.mInputRecordSurface != null) {
                    this.mMultiViewRecorder.startRecording(new EncoderConfig(null, null, null, new MVRecordOutputInfo(this.mOrientationHint), this.mMVDisplaySurface.getEGLContext(), this.mInputRecordSurface), this.mGet.getOrientationDegree());
                } else {
                    CamLog.m5e(CameraConstants.TAG, "MultiView -  mInputRecordSurface is null");
                }
            }
            this.mMultiViewRecorder.frameAvailable(this.mPreviewTexture, this.mTmpMatrix);
        } else if (this.mIsRecordStopped) {
            this.mIsRecordStopped = false;
            this.mMultiViewRecorder.stopRecording();
        }
    }

    private boolean checkPreviewStoppedAndResetFrameListener() {
        if (this.mGet.isPaused() || this.mIsStopped || this.mNotReceivedFrameCount <= 80) {
            return false;
        }
        int i;
        CamLog.m5e(CameraConstants.TAG, "MultiView -  reset frame listener" + this.mNotReceivedFrameCount);
        for (i = 0; i < this.mPreviewTexture.length; i++) {
            this.mSurfaceTexture[i].setOnFrameAvailableListener(null);
        }
        for (i = 0; i < this.mPreviewTexture.length; i++) {
            this.mSurfaceTexture[i].setOnFrameAvailableListener(this.mFrameAvailableListener[i]);
        }
        return true;
    }

    private void recordMVFrameCollage(int[] texture, long timeStamp) {
        if (this.mCollageState == 1) {
            this.mMultiViewRecorder.frameAvailableCollage(texture, timeStamp);
        }
    }

    private synchronized void takeMVFrame() {
        if (this.mTakeGLViewRequested || this.mIsCollageImgOn) {
            DebugUtil.setEndTime("[2] MV image: Wait time for starting takeMVFrame");
            this.mTakeGLViewRequested = false;
            long startTime = System.currentTimeMillis();
            CamLog.m3d(CameraConstants.TAG, "MultiView - get bitmap from textureview - START");
            DebugUtil.setStartTime("[3] MV image: mTextureViewMV.getBitmap()");
            Bitmap mPreviewBitmap_origin = this.mTextureViewMV.getBitmap();
            DebugUtil.setEndTime("[3] MV image: mTextureViewMV.getBitmap()");
            CamLog.m3d(CameraConstants.TAG, "MultiView - get bitmap from textureview - DONE");
            Bitmap mPreviewBitmap = null;
            if (this.mCaptureRotationDegree != 0) {
                CamLog.m3d(CameraConstants.TAG, "MultiView - getRotatedImage - START");
                DebugUtil.setStartTime("[4] MV image: getRotatedImage");
                mPreviewBitmap = BitmapManagingUtil.getRotatedImage(mPreviewBitmap_origin, this.mCaptureRotationDegree, false);
                DebugUtil.setEndTime("[4] MV image: getRotatedImage");
                CamLog.m3d(CameraConstants.TAG, "MultiView - get bitmap from textureview - DONE");
            }
            if (this.mFrameCaptureListener != null) {
                mPreviewBitmap = this.mFrameCaptureListener.onPreGLFrameCapture(mPreviewBitmap);
            }
            ByteArrayOutputStream mPreviewStream = new ByteArrayOutputStream();
            if (mPreviewBitmap != null) {
                DebugUtil.setStartTime("[5] MV image: jpeg compression");
                mPreviewBitmap.compress(CompressFormat.JPEG, 100, mPreviewStream);
                DebugUtil.setEndTime("[5] MV image: jpeg compression");
                CamLog.m3d(CameraConstants.TAG, "MultiView - jpeg compression - DONE");
                if (this.mFrameCaptureListener != null) {
                    this.mFrameCaptureListener.onGLFrameCaptured(mPreviewStream.toByteArray());
                }
                try {
                    mPreviewStream.close();
                } catch (Exception e) {
                    Log.e(CameraConstants.TAG, "MultiView - mPreviewStream.close() failed");
                }
                mPreviewBitmap.recycle();
            }
            CamLog.m3d(CameraConstants.TAG, "MultiView - take picture thread - END");
            DebugUtil.showElapsedTime("takeMVFrame", startTime);
            MultiViewFrame.deleteCapturedTexture();
            setCollageImageOnState(false);
        }
        return;
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

    public void takeGLView() {
        CamLog.m3d(CameraConstants.TAG, "MultiView - takeGLView");
        this.mTakeGLViewRequested = true;
    }

    public void setInputRecordSurface(Surface surface) {
        this.mInputRecordSurface = surface;
    }

    public void startRecorder(boolean singleViewRecording) {
        this.mIsRecordStarted = true;
        CamLog.m3d(CameraConstants.TAG, "-single- startRecorder = " + singleViewRecording);
        this.mIsSingleViewScreenRecording = singleViewRecording;
        this.mMultiViewRecorder.setSingleviewRecording(this.mIsSingleViewScreenRecording);
    }

    public void stopRecorder() {
        this.mIsRecordStopped = true;
        this.mIsRecordStarted = false;
        this.mIsInitRecording = false;
    }

    public synchronized void startRecorderCollage(MultiViewRecordInputInfo inputInfo, MVRecordOutputInfo outputInfo) {
        CamLog.m3d(CameraConstants.TAG, "startRecorderCollage");
        this.mIsSingleViewScreenRecording = false;
        this.mMultiViewRecorder.setSingleviewRecording(this.mIsSingleViewScreenRecording);
        this.mCollageInputInfo = inputInfo;
        this.mCollageOutputInfo = outputInfo;
        this.mIsAllFirstFrameCollageReceived = false;
        this.mMultiViewRecorder.setLatchForAudioData();
        this.mCollageState = 1;
    }

    public void setOrientationHint(int orientationHint) {
        this.mOrientationHint = orientationHint;
    }

    public synchronized void stopRecorderCollage() {
        CamLog.m3d(CameraConstants.TAG, "stopRecorderCollage");
        this.mMultiViewRecorder.stopRecording();
        this.mCollageState = 0;
        if (this.mTextureCollage != null) {
            CamLog.m3d(CameraConstants.TAG, "-null- deleteTexture mTextureCollage");
            MultiViewFrameBase.deleteTexture(this.mTextureCollage, this.mTextureCollage.length);
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

    private void release() {
        CamLog.m3d(CameraConstants.TAG, "-multiview- release()");
        if (this.mMVDisplaySurface != null) {
            this.mMVDisplaySurface.release();
            this.mMVDisplaySurface = null;
        }
        if (this.mPreviewTexture != null) {
            CamLog.m3d(CameraConstants.TAG, "-null- deleteTexture mPreviewTexture");
            MultiViewFrameBase.deleteTexture(this.mPreviewTexture, this.mPreviewTexture.length);
            this.mPreviewTexture = null;
        }
        if (this.mTextureCollage != null) {
            CamLog.m3d(CameraConstants.TAG, "-null- deleteTexture mTextureCollage");
            MultiViewFrameBase.deleteTexture(this.mTextureCollage, this.mTextureCollage.length);
            this.mTextureCollage = null;
        }
        for (int i = 0; i < this.mSurfaceTexture.length; i++) {
            if (this.mSurfaceTexture[i] != null) {
                this.mSurfaceTexture[i].release();
                CamLog.m3d(CameraConstants.TAG, "-null- release mSurfaceTexture : " + i);
                this.mSurfaceTexture[i] = null;
            }
        }
        this.mSurfaceTexture = null;
        if (this.mTextureViewMV != null) {
            this.mTextureViewMV.removeCallbacks(this);
            this.mTextureViewMV = null;
        }
        this.mFrameCaptureListener = null;
        this.mSTAvailableListener = null;
        this.mSTAvailableListenerCollage = null;
        this.mMultiViewRecorder = null;
    }

    public void stopThread() {
        CamLog.m3d(CameraConstants.TAG, "-th- stopThread");
        this.mIsStopped = true;
    }

    public MultiViewFrameBase getMultiviewFrame() {
        return this.mMultiViewFrame;
    }

    public void setAudioData(ByteBuffer audioMixed, long timeStamp) {
        if (this.mMultiViewRecorder == null || audioMixed == null) {
            CamLog.m3d(CameraConstants.TAG, "Exit it because of null object");
        } else {
            this.mMultiViewRecorder.audioDataAvailalbe(audioMixed, timeStamp);
        }
    }

    public void setRequiredToFinishAfterSaving(boolean save) {
        CamLog.m3d(CameraConstants.TAG, "-th- setRequiredToFinishAfterSaving");
        this.isRequiredToFinishAfterSaving = save;
    }

    public boolean isRequiredToFinishAfterSaving() {
        return this.isRequiredToFinishAfterSaving;
    }

    public void setCollageImageOnState(boolean isCollageImage) {
        this.mIsCollageImgOn = isCollageImage;
    }
}
