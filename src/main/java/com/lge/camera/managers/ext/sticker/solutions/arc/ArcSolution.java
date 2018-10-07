package com.lge.camera.managers.ext.sticker.solutions.arc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.Image;
import android.opengl.GLSurfaceView;
import android.support.p000v4.media.session.PlaybackStateCompat;
import com.arcsoft.stickerlibrary.api.FaceResult;
import com.arcsoft.stickerlibrary.api.StickerApi;
import com.arcsoft.stickerlibrary.api.StickerCaptureCallback;
import com.arcsoft.stickerlibrary.api.StickerInfoListener;
import com.arcsoft.stickerlibrary.api.StickerRecordingListener;
import com.arcsoft.stickerlibrary.sticker.StickerAction;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.MmsProperties;
import com.lge.camera.managers.ext.sticker.StickerInformationDataClass;
import com.lge.camera.managers.ext.sticker.solutions.ContentsInformation;
import com.lge.camera.managers.ext.sticker.solutions.SolutionBase;
import com.lge.camera.managers.ext.sticker.solutions.StickerDrawingInformation;
import com.lge.camera.managers.ext.sticker.utils.StickerUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ArcSolution extends SolutionBase implements StickerCaptureCallback, StickerRecordingListener {
    public static final int BEAUTY_WITHOUT_STICKER_BRIGHTEN_LEVEL = 30;
    public static final int BEAUTY_WITHOUT_STICKER_SOFTEN_LEVEL = 65;
    private static final String TAG = "ArcSolution";
    private Object MonitorStopRecording = null;
    private long duration = 0;
    private Context mContext;
    private int mDisplayOrientation = 90;
    private String mFaceDataPath;
    private boolean mInitWithVideoSize = false;
    private ContentsInformation mLastVideoContent;
    private String mLastVideoDir;
    private String mLastVideoName;
    private boolean mNeedFlip;
    private Bitmap mSignatureBitmap;
    private StickerApi mStickerApi;
    private StickerDrawingInformation mStickerDI = null;
    private StickerInfoListener mStickerInfoListener = new C13601();
    private String mTrackDataPath;
    private int mViewHeight;
    private int mViewWidth;
    private long videoFileSize;
    private int waitRenderCount = -1;

    /* renamed from: com.lge.camera.managers.ext.sticker.solutions.arc.ArcSolution$1 */
    class C13601 implements StickerInfoListener {
        C13601() {
        }

        public void OnInfoListener(int msg, Object value) {
            CamLog.m3d(ArcSolution.TAG, "msg : " + msg);
            boolean actionDone = false;
            int actionInfo = -1;
            switch (msg) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    actionInfo = ArcSolution.this.getActionInfo(msg);
                    if (actionInfo != -1) {
                        actionDone = true;
                        break;
                    }
                    break;
                case 17:
                    ArcSolution.this.mInfoListener.onInfoListener(100, value);
                    break;
                case 33:
                    int percent = ((Integer) value).intValue();
                    CamLog.m3d(ArcSolution.TAG, "percent : " + percent);
                    if (percent != 0) {
                        if (100 == percent) {
                            int faceCount = 0;
                            FaceResult fr = ArcSolution.this.mStickerApi.getFaceResult();
                            if (fr != null) {
                                faceCount = fr.getFaceCount();
                            }
                            ArcSolution.this.mInfoListener.onInfoListener(103, Integer.valueOf(faceCount));
                            ArcSolution.this.mInfoListener.onInfoListener(102, value);
                            ArcSolution.this.waitRenderCount = 0;
                            break;
                        }
                    }
                    ArcSolution.this.mInfoListener.onInfoListener(102, value);
                    ArcSolution.this.mStateManager.setStickerLoadState(false);
                    ArcSolution.this.waitRenderCount = -1;
                    break;
                    break;
            }
            if (actionDone) {
                ArcSolution.this.mInfoListener.onInfoListener(101, Integer.valueOf(actionInfo));
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.sticker.solutions.arc.ArcSolution$2 */
    class C13612 implements Runnable {
        C13612() {
        }

        public void run() {
            synchronized (ArcSolution.this.MonitorStopRecording) {
                ArcSolution.this.mStickerApi.stopRecording();
                ArcSolution.this.MonitorStopRecording.notifyAll();
            }
        }
    }

    public ArcSolution(Context ctx) {
        this.mContext = ctx;
        this.mTrackDataPath = this.mContext.getFilesDir() + "/" + StickerUtil.TRACKING_DATA_DIRECTORY_NAME + "/" + StickerUtil.TRACKING_DATA_FILE;
        this.mFaceDataPath = this.mContext.getFilesDir() + "/" + StickerUtil.TRACKING_DATA_DIRECTORY_NAME + "/" + StickerUtil.TRACKING_FACE_FILE;
    }

    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        if (this.mStickerApi == null) {
            this.mStickerApi = new StickerApi();
            this.mStickerApi.enableLog(CamLog.getLogOn());
            this.mStateManager.setState(2);
        }
    }

    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        if (this.mStickerDI.isInfoChanged()) {
            uninit();
        }
        init();
    }

    public void onDrawFrame(GL10 gl10) {
        if (this.mStateManager.isInited()) {
            this.mStickerApi.startRender();
        }
        if (this.waitRenderCount >= 0) {
            this.waitRenderCount++;
            if (this.waitRenderCount == 3) {
                this.mStateManager.setStickerLoadState(true);
                this.mInfoListener.onInfoListener(104, null);
                this.waitRenderCount = -1;
            }
        }
    }

    private int getActionInfo(int currentAction) {
        if (this.mStickerApi == null) {
            return -1;
        }
        StickerAction sa = this.mStickerApi.getActionList();
        if (sa == null) {
            return -1;
        }
        switch (currentAction) {
            case 1:
                if (sa.mMouthOpen == 1) {
                    return 0;
                }
                return -1;
            case 2:
                if (sa.mEyeBlink == 1) {
                    return 1;
                }
                return -1;
            case 3:
                if (sa.mEyebrowRaise == 1) {
                    return 2;
                }
                return -1;
            case 4:
                if (sa.mHeadNod == 1) {
                    return 3;
                }
                return -1;
            case 5:
                if (sa.mHeadShake == 1) {
                    return 4;
                }
                return -1;
            default:
                return -1;
        }
    }

    public void init() {
        if (this.mStateManager.isCanInit()) {
            this.MonitorStopRecording = new Object();
            CamLog.m5e(TAG, "init = " + this.mStickerDI.toString());
            if (this.mStickerDI.isValid()) {
                this.mStickerDI.setIsInfoChangedFalse();
                this.mStickerApi = new StickerApi();
                this.mStickerApi.enableLog(CamLog.getLogOn());
                if (this.mInitWithVideoSize) {
                    this.mStickerApi.init(this.mContext, this.mStickerDI.getPreviewWidth(), this.mStickerDI.getPreviewHeight(), this.mStickerDI.isFrontCamera(), this.mDisplayOrientation, this.mStickerDI.getOrientation(), this.mTrackDataPath, this.mFaceDataPath, this.mStickerInfoListener);
                } else {
                    this.mStickerApi.init(this.mContext, this.mStickerDI.getPreviewWidth(), this.mStickerDI.getPreviewHeight(), this.mStickerDI.isFrontCamera(), this.mDisplayOrientation, this.mStickerDI.getOrientation(), this.mTrackDataPath, this.mFaceDataPath, this.mStickerInfoListener);
                }
                this.mStickerApi.setDefaultBeautyValue(65, 30);
                this.mStateManager.setState(4);
            }
        }
    }

    public void uninit() {
        if (this.mStateManager.isInited()) {
            CamLog.m5e(TAG, "uninit = ");
            if (this.mStateManager.isNowRecording()) {
                stopRecordingInner();
            }
            this.mStickerApi.uninit();
            this.mStateManager.setState(3);
            clearActionStringList();
        }
    }

    public void setDrawingInformation(int[] previewSize, int[] videoSize, int orientation, int cameraID, boolean initWithVideoSize) {
        this.mInitWithVideoSize = initWithVideoSize;
        if (this.mInitWithVideoSize) {
            String mVideoSize = MmsProperties.getMaximumMmsResolutions(this.mContext.getContentResolver());
            if (mVideoSize == null) {
                mVideoSize = CameraConstants.QCIF_RESOLUTION;
            }
            videoSize = Utils.sizeStringToArray(mVideoSize);
        }
        if (this.mStickerDI == null) {
            this.mStickerDI = new StickerDrawingInformation(previewSize, videoSize, orientation, cameraID);
        } else {
            this.mStickerDI.setData(previewSize, videoSize, orientation, cameraID);
        }
        CamLog.m5e(TAG, "init With video? = " + this.mInitWithVideoSize);
    }

    public void setSticker(StickerInformationDataClass data) {
        if (this.mStateManager.isInited()) {
            if (data == null) {
                this.mStickerApi.setDefaultBeautyValue(65, 30);
                this.mStickerApi.setTemplate(null);
                this.mStateManager.setStickerDrawing(false);
                clearActionStringList();
            } else {
                this.mStickerApi.setTemplate(data.configFile);
                this.mStateManager.setStickerDrawing(true);
                setActionStringList();
            }
            this.mStateManager.setStickerLoadState(false);
            this.waitRenderCount = -1;
            return;
        }
        CamLog.m5e(TAG, "mStateManager is not STATE_INITED");
    }

    public void process(Image image, int degree) {
        if (this.mStateManager.isInited()) {
            this.mStickerApi.startProcess(image, degree);
        }
    }

    public void process(byte[] data, int degree) {
        if (this.mStateManager.isInited()) {
            this.mStickerApi.startProcess(data, degree);
        }
    }

    public int type() {
        return 2;
    }

    public boolean isInited() {
        return this.mStateManager.isInited();
    }

    public boolean isStickerDrawing() {
        return this.mStateManager.isStickerDrawing();
    }

    public void takePicture(int viewWidth, int viewHeight, boolean needFlip, Bitmap signature) {
        this.mViewWidth = viewWidth;
        this.mViewHeight = viewHeight;
        this.mNeedFlip = needFlip;
        this.mSignatureBitmap = signature;
        if (this.mStateManager.isInited()) {
            CamLog.m3d(TAG, "takePicture = ");
            this.mStickerApi.capture(this);
        }
    }

    public void startRecording(String dir, String filename, long maxFileSize, long maxDuration) {
        if (this.mStateManager.isInited() && !this.mStateManager.isNowRecording()) {
            int result;
            this.mLastVideoContent = null;
            this.mStateManager.setRecordingStarted();
            this.mLastVideoDir = dir;
            this.mLastVideoName = filename;
            this.videoFileSize = 0;
            CamLog.m3d(TAG, "startRecording = orientaion = " + this.mPhoneOrientationDegree);
            this.mViewHeight = this.mStickerDI.getPreviewHeight();
            this.mViewWidth = this.mStickerDI.getPreviewWidth();
            this.duration = System.currentTimeMillis();
            if (maxDuration != -1) {
                CamLog.m5e(TAG, "setDuration limit = " + maxDuration);
                this.mStickerApi.setMaxDuration(maxDuration);
            } else {
                CamLog.m5e(TAG, "setDuration limit = " + maxDuration + " so no limit duration");
            }
            if (!this.mInitWithVideoSize || maxFileSize == 0) {
                this.mStickerApi.setMaxFileSize(-1);
                CamLog.m5e(TAG, "maxFileSize full with = -1");
                result = this.mStickerApi.startRecording(this.mLastVideoDir + this.mLastVideoName + ".mp4", this, this.mPhoneOrientationDegree, this.mViewWidth, this.mViewHeight);
            } else {
                this.mStickerApi.setMaxFileSize(-1);
                CamLog.m5e(TAG, "maxFileSize full with = -1");
                result = this.mStickerApi.startRecording(this.mLastVideoDir + this.mLastVideoName + ".mp4", this, this.mPhoneOrientationDegree, this.mViewWidth, this.mViewHeight);
            }
            if (result != 0) {
                CamLog.m5e(TAG, "result : " + result);
                stopRecordingInner();
            }
        }
    }

    private void stopRecordingInner() {
        CamLog.m5e(TAG, "stopRecordingInner");
        if (!this.mStateManager.isInited()) {
            return;
        }
        if (this.mStateManager.isNowRecording() || this.mStateManager.isPauseRecording()) {
            this.mStateManager.setRecordingFinished();
            int result = this.mStickerApi.stopRecording();
            File video = new File(this.mLastVideoDir + this.mLastVideoName + ".mp4");
            if (result == 0 && video != null && video.exists()) {
                this.duration = System.currentTimeMillis() - this.duration;
                this.mLastVideoContent = new ContentsInformation(this.mLastVideoDir, this.mLastVideoName, ".mp4", this.videoFileSize, this.mViewWidth, this.mViewHeight, this.duration);
                return;
            }
            this.mLastVideoContent = null;
        }
    }

    public ContentsInformation stopRecording(GLSurfaceView glview) {
        ContentsInformation ci;
        if (this.mStateManager.isInited() && (this.mStateManager.isNowRecording() || this.mStateManager.isPauseRecording())) {
            this.mStateManager.setRecordingFinished();
            if (glview != null) {
                glview.queueEvent(new C13612());
                synchronized (this.MonitorStopRecording) {
                    try {
                        this.MonitorStopRecording.wait(300);
                    } catch (InterruptedException e) {
                        CamLog.m3d(TAG, "Interrupted wait 300 millis end");
                    }
                }
            } else {
                this.mStickerApi.stopRecording();
            }
            if (this.mContentsTakenCallback != null) {
                File video = new File(this.mLastVideoDir + this.mLastVideoName + ".mp4");
                if (video == null || !video.exists()) {
                    ci = new ContentsInformation();
                } else {
                    this.duration = System.currentTimeMillis() - this.duration;
                    ci = new ContentsInformation(this.mLastVideoDir, this.mLastVideoName, ".mp4", this.videoFileSize, this.mViewWidth, this.mViewHeight, this.duration);
                }
                CamLog.m3d(TAG, "saved file : " + ci.toString());
                return ci;
            }
        } else if (this.mLastVideoContent != null) {
            ci = this.mLastVideoContent.clone();
            CamLog.m3d(TAG, "saved file : " + ci.toString());
            this.mLastVideoContent = null;
            return ci;
        }
        return new ContentsInformation();
    }

    public void resumeRecording() {
        if (this.mStateManager.isInited() && this.mStateManager.isPauseRecording()) {
            this.mStateManager.setRecordingPauseResume(101);
            this.mStickerApi.resumeRecording();
        }
    }

    public void pauseRecording() {
        if (this.mStateManager.isInited() && this.mStateManager.isNowRecording()) {
            this.mStateManager.setRecordingPauseResume(102);
            this.mStickerApi.pauseRecording();
        }
    }

    public boolean isRecording() {
        CamLog.m3d(TAG, "mStateManager.isInited() : " + this.mStateManager.isInited());
        CamLog.m3d(TAG, "mStateManager.isNowRecording() : " + this.mStateManager.isNowRecording());
        CamLog.m3d(TAG, "mStateManager.isPauseRecording() : " + this.mStateManager.isPauseRecording());
        return this.mStateManager.isInited() && (this.mStateManager.isNowRecording() || this.mStateManager.isPauseRecording());
    }

    public boolean isStickerLoadCompleted() {
        CamLog.m3d(TAG, "mStateManager.isStickerLoaded() : " + this.mStateManager.isStickerLoaded());
        return this.mStateManager.isStickerLoaded();
    }

    public int GetCaptureResult(ByteBuffer byteBuffer) {
        CamLog.m5e(TAG, "GetCaptureResult = ");
        if (!(byteBuffer == null || this.mContentsTakenCallback == null)) {
            int width = this.mViewWidth;
            int height = this.mViewHeight;
            if (width == 0 || height == 0) {
                width = this.mStickerDI.getPreviewWidth();
                height = this.mStickerDI.getPreviewHeight();
            }
            if (width == 0 || height == 0) {
                CamLog.m5e(TAG, "GetCaptureResult = error!!!");
                return 0;
            }
            Bitmap bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            if (this.mSignatureBitmap != null) {
                Canvas canvas = new Canvas(bm);
                Matrix sigMatrix = getSignatureMatrix(this.mSignatureBitmap.getWidth(), this.mSignatureBitmap.getHeight());
                Bitmap original = Bitmap.createBitmap(width, height, Config.ARGB_8888);
                Bitmap rotated = Bitmap.createBitmap(this.mSignatureBitmap, 0, 0, this.mSignatureBitmap.getWidth(), this.mSignatureBitmap.getHeight(), sigMatrix, false);
                original.copyPixelsFromBuffer(byteBuffer);
                canvas.drawBitmap(original, 0.0f, 0.0f, null);
                int[] dst = getSignatureDestRect(rotated.getWidth(), rotated.getHeight());
                canvas.drawBitmap(rotated, (float) dst[0], (float) dst[1], null);
                original.recycle();
                rotated.recycle();
                this.mSignatureBitmap.recycle();
                this.mSignatureBitmap = null;
                CamLog.m5e(TAG, "compose signature end");
            } else {
                bm.copyPixelsFromBuffer(byteBuffer);
            }
            Bitmap jpeg = Bitmap.createBitmap(bm, 0, 0, width, height, calculateMatrix(), false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            jpeg.compress(CompressFormat.JPEG, 100, baos);
            byte[] jpegdata = baos.toByteArray();
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ContentsInformation contentsInformation = new ContentsInformation(jpegdata, jpeg.getWidth(), jpeg.getHeight(), getJpegOrientation());
            bm.recycle();
            jpeg.recycle();
            this.mContentsTakenCallback.onContentTaken(contentsInformation);
        }
        return 0;
    }

    protected void setActionStringList() {
        String str = null;
        clearActionStringList();
        if (this.mStickerApi != null && this.mStateManager.isInited()) {
            StickerAction action = this.mStickerApi.getActionList();
            CamLog.m3d(TAG, "setActionStringList : " + action);
            if (action != null) {
                String string;
                String[] strArr = this.mActionStringList;
                if (action.mEyeBlink == 1) {
                    string = this.mContext.getString(C0088R.string.sticker_blink_eyes);
                } else {
                    string = null;
                }
                strArr[1] = string;
                strArr = this.mActionStringList;
                if (action.mEyebrowRaise == 1) {
                    string = this.mContext.getString(C0088R.string.sticker_raise_eyebrows);
                } else {
                    string = null;
                }
                strArr[2] = string;
                strArr = this.mActionStringList;
                if (action.mHeadNod == 1) {
                    string = this.mContext.getString(C0088R.string.sticker_nod_head);
                } else {
                    string = null;
                }
                strArr[3] = string;
                strArr = this.mActionStringList;
                if (action.mHeadShake == 1) {
                    string = this.mContext.getString(C0088R.string.sticker_turn_head_left_and_right);
                } else {
                    string = null;
                }
                strArr[4] = string;
                String[] strArr2 = this.mActionStringList;
                if (action.mMouthOpen == 1) {
                    str = this.mContext.getString(C0088R.string.sticker_open_your_mouth);
                }
                strArr2[0] = str;
            }
        }
    }

    private Matrix calculateMatrix() {
        Matrix matrix = new Matrix();
        matrix.postScale(((float) this.mStickerDI.getPreviewHeight()) / ((float) this.mViewWidth), ((float) this.mStickerDI.getPreviewWidth()) / ((float) this.mViewHeight));
        if (!this.mStickerDI.isFrontCamera()) {
            matrix.preScale(1.0f, -1.0f, (float) (this.mViewWidth / 2), (float) (this.mViewHeight / 2));
            matrix.postRotate(-90.0f, (float) (this.mViewWidth / 2), (float) (this.mViewHeight / 2));
        } else if (this.mNeedFlip) {
            matrix.postRotate(-90.0f, (float) (this.mViewWidth / 2), (float) (this.mViewHeight / 2));
        } else {
            matrix.preScale(1.0f, -1.0f);
            if (this.mPhoneOrientationDegree == 0 || this.mPhoneOrientationDegree == 180) {
                matrix.postRotate(90.0f, (float) (this.mViewWidth / 2), (float) (this.mViewHeight / 2));
            } else {
                matrix.postRotate(-90.0f, (float) (this.mViewWidth / 2), (float) (this.mViewHeight / 2));
            }
        }
        return matrix;
    }

    private int getJpegOrientation() {
        if (this.mStickerDI.isFrontCamera()) {
            if (this.mPhoneOrientationDegree == 0) {
                return 270;
            }
            if (this.mPhoneOrientationDegree == 90) {
                return 180;
            }
            if (this.mPhoneOrientationDegree == 180) {
                return 90;
            }
            return 0;
        } else if (this.mPhoneOrientationDegree == 0) {
            return 90;
        } else {
            if (this.mPhoneOrientationDegree == 90) {
                return 180;
            }
            if (this.mPhoneOrientationDegree != 180) {
                return 0;
            }
            return 270;
        }
    }

    private Matrix getSignatureMatrix(int sigWidth, int sigHeight) {
        Matrix matrix = new Matrix();
        int degree = this.mPhoneOrientationDegree;
        if (!this.mStickerDI.isFrontCamera()) {
            matrix.preScale(1.0f, -1.0f, (float) (sigWidth / 2), (float) (sigHeight / 2));
        } else if (this.mNeedFlip) {
            degree += 180;
        } else {
            matrix.preScale(1.0f, -1.0f);
        }
        matrix.postRotate((float) degree, (float) (sigWidth / 2), (float) (sigWidth / 2));
        return matrix;
    }

    private int[] getSignatureDestRect(int rWidth, int rHeight) {
        int[] dst = new int[2];
        if (this.mStickerDI.isFrontCamera() && this.mNeedFlip) {
            if (this.mPhoneOrientationDegree == 0) {
                dst[0] = 0;
                dst[1] = 0;
            } else if (this.mPhoneOrientationDegree == 90) {
                dst[0] = this.mViewWidth - rWidth;
                dst[1] = 0;
            } else if (this.mPhoneOrientationDegree == 270) {
                dst[0] = 0;
                dst[1] = this.mViewHeight - rHeight;
            } else {
                dst[0] = this.mViewWidth - rWidth;
                dst[1] = this.mViewHeight - rHeight;
            }
        } else if (this.mPhoneOrientationDegree == 0) {
            dst[0] = this.mViewWidth - rWidth;
            dst[1] = 0;
        } else if (this.mPhoneOrientationDegree == 90) {
            dst[0] = this.mViewWidth - rWidth;
            dst[1] = this.mViewHeight - rHeight;
        } else if (this.mPhoneOrientationDegree == 270) {
            dst[0] = 0;
            dst[1] = 0;
        } else {
            dst[0] = 0;
            dst[1] = this.mViewHeight - rHeight;
        }
        return dst;
    }

    public void onRecordingListener(int msg, Object value) {
        CamLog.m3d(TAG, "onRecordingListener msg = " + msg + " value = " + value);
        switch (msg) {
            case 257:
                CamLog.m3d(TAG, "RECORDER_INFO__MAX_DURATION_REACHED = " + ((((Long) value).longValue() / 1000) / 1000));
                pauseRecording();
                if (this.mContentsTakenCallback != null) {
                    this.mContentsTakenCallback.onContentTaken(new ContentsInformation(true));
                    return;
                }
                return;
            case 258:
                CamLog.m3d(TAG, "RECORDER_INFO_CURRENT_DURATION = " + ((((Long) value).longValue() / 1000) / 1000));
                return;
            case 259:
                CamLog.m3d(TAG, "RECORDER_INFO_MAX_FILESIZE_REACHED= " + (((Long) value).longValue() / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID));
                stopRecordingInner();
                return;
            case 260:
                long size = ((Long) value).longValue() / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
                CamLog.m3d(TAG, "RECORDER_INFO_CURRENT_FILESIZE = " + size);
                this.videoFileSize = size;
                return;
            default:
                return;
        }
    }
}
