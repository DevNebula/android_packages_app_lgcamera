package com.lge.camera.app.ext;

import android.renderscript.Matrix3f;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.MDMUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.TelephonyUtil;
import com.lge.camera.util.Utils;
import com.lge.panorama.Panorama;
import com.lge.panorama.Panorama.PanoramaListener;
import com.lge.panorama.RotationListener;
import com.lge.panorama.RotationListener.PanoramaRotaionSensorListener;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class PanoramaModuleLGImpl extends PanoramaModuleCommonImpl {
    protected static final int FIRST_SKIP_CNT_LG = 0;
    protected static final double GOAL_ANGLE_DEFAULT = 360.0d;
    protected static final int JPEG_QUALITY_FACTOR = 92;
    protected static final Object STOP_ENGINE_SYNC = new Object();
    protected static final boolean USE_AE_LOCK = true;
    protected static final boolean USE_AWB_LOCK = true;
    protected static final boolean USE_GAIN_COMPENSATION = false;
    protected static final int USE_MIN_PATH_SEAM_FINDER = 1;
    protected static final boolean ZERO_COPY = true;
    protected double mAngleX = 0.0d;
    protected double mAngleY = 0.0d;
    protected ByteBuffer mBackupFrameByteBuffer = null;
    protected ByteBuffer[] mByteBufferArray = null;
    protected float[] mCurrentGravityData = new float[3];
    protected int mDisplayRotation = 0;
    protected int mEdgeEnhancementMode = 0;
    protected long mFirstCamTimestampOffset = -1;
    protected Matrix3f mFirstRotationMatrix = new Matrix3f();
    protected long mFirstSysTimestampOffset = -1;
    protected long mFrameDelay = 0;
    protected HashMap<Integer, Integer> mFrameHashMap = null;
    protected int mLensCorrectionMode = 0;
    protected double mMaxAngleRotationX = 0.0d;
    protected double mMaxAngleRotationY = 0.0d;
    protected int mNRmode = 0;
    protected Panorama mPanorama = null;
    protected PanoramaListener mPanoramaListener = new C04301();
    protected ByteBuffer mPreviewImageRGBBuffer = null;
    protected float[] mPrincipal_point = new float[2];
    protected float[] mQuaternion = new float[4];
    protected int mRotationDirection = -1;
    protected float[] mRotationMatrix = new float[9];
    protected RotationListener mSensorEventListener = null;
    protected int mTrackingMode = 1;
    protected float[] mUndistort_matrix = new float[4];
    protected boolean mValidRotation = false;
    protected boolean mWaitSavingCompleted = false;

    /* renamed from: com.lge.camera.app.ext.PanoramaModuleLGImpl$1 */
    class C04301 implements PanoramaListener {
        C04301() {
        }

        public void onNotifyStatus(int statusCode) {
            if (statusCode != 0) {
                CamLog.m3d(CameraConstants.TAG, "mPanoramaListener: received statusCode = " + statusCode);
            }
            PanoramaModuleLGImpl.this.checkStatusFlow(statusCode);
            PanoramaModuleLGImpl.this.checkStatusMovement(statusCode);
            PanoramaModuleLGImpl.this.checkSpeedWarningStatusFlow(statusCode);
            PanoramaModuleLGImpl.this.checkStatusWarningError(statusCode);
        }
    }

    /* renamed from: com.lge.camera.app.ext.PanoramaModuleLGImpl$2 */
    class C04312 implements PanoramaRotaionSensorListener {
        C04312() {
        }

        public void feedInertialSensorData(float[] quaternion, long timeStamp) {
            if (PanoramaModuleLGImpl.this.mPanorama != null && PanoramaModuleLGImpl.this.mIsFeeding) {
                PanoramaModuleLGImpl.this.mPanorama.feedInertialSensorData(quaternion, timeStamp);
            }
        }
    }

    public PanoramaModuleLGImpl(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected boolean isZeroCopy() {
        return this.mInputType == 0;
    }

    protected byte[] getRawBufferById(int bufferId, int buffer_size) {
        if (!isZeroCopy()) {
            return null;
        }
        if (this.mFrameHashMap == null) {
            CamLog.m5e(CameraConstants.TAG, "getPreviewBuffer mPreviewHashMap is NULL");
            return null;
        } else if (this.mByteBufferArray == null) {
            CamLog.m5e(CameraConstants.TAG, "getPreviewBuffer mByteBufferArray is NULL");
            return null;
        } else if (this.mBackupFrameByteBuffer == null) {
            CamLog.m5e(CameraConstants.TAG, "getPreviewBuffer mBackupPreviewByteBuffer is NULL");
            return null;
        } else if (bufferId < 0 || bufferId >= buffer_size) {
            CamLog.m5e(CameraConstants.TAG, "getPreviewBuffer Check bufferId : " + bufferId + " so mBackup buffer");
            return this.mBackupFrameByteBuffer.array();
        } else {
            byte[] returnValue = this.mByteBufferArray[bufferId].array();
            CamLog.m3d(CameraConstants.TAG, "getPreviewBuffer mPanorama.dequeueBufferd : " + bufferId + ", hasCode : " + returnValue.hashCode());
            return returnValue;
        }
    }

    protected void createRawBuffer(int size, int buffer_size) {
        int i;
        if (isZeroCopy()) {
            this.mBackupFrameByteBuffer = ByteBuffer.allocateDirect(size);
            this.mFrameHashMap = new HashMap();
            this.mByteBufferArray = new ByteBuffer[buffer_size];
            for (i = 0; i < buffer_size; i++) {
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(size);
                int hashCode = byteBuffer.array().hashCode();
                this.mByteBufferArray[i] = byteBuffer;
                this.mFrameHashMap.put(Integer.valueOf(hashCode), Integer.valueOf(i));
                CamLog.m3d(CameraConstants.TAG, "call byteBuffer " + i + ", hashCode " + hashCode);
            }
            CamLog.m3d(CameraConstants.TAG, "Create preview buf size : " + size);
            return;
        }
        this.mRawByteBuffer = new ByteBuffer[buffer_size];
        for (i = 0; i < buffer_size; i++) {
            this.mRawByteBuffer[i] = ByteBuffer.allocateDirect(size);
        }
    }

    protected void releaseFrameHashMap() {
        if (this.mFrameHashMap != null) {
            this.mFrameHashMap.clear();
            this.mFrameHashMap = null;
        }
    }

    protected void releaseBackupFrame() {
        if (this.mBackupFrameByteBuffer != null) {
            this.mBackupFrameByteBuffer.clear();
            this.mBackupFrameByteBuffer = null;
        }
    }

    protected void checkStatusFlow(int statusCode) {
    }

    protected void checkStatusWarningError(int statusCode) {
    }

    protected void checkSpeedWarningStatusFlow(int statusCode) {
    }

    protected void checkStatusMovement(int statusCode) {
        switch (statusCode) {
            case 10:
                this.mCaptureDirection = 1;
                return;
            case 11:
                this.mCaptureDirection = 2;
                return;
            case 12:
                this.mCaptureDirection = 3;
                return;
            case 13:
                this.mCaptureDirection = 4;
                return;
            default:
                return;
        }
    }

    protected int convertDirectionLG(int direction) {
        switch (direction) {
            case 1:
                return 21;
            case 2:
                return 22;
            case 3:
                return 24;
            case 4:
                return 23;
            default:
                return 0;
        }
    }

    public void init() {
        super.init();
        this.mSensorEventListener = new RotationListener(this.mGet.getAppContext());
        this.mSensorEventListener.setListener(new C04312());
        this.mDisplayRotation = CameraDeviceUtils.getDisplayRotation(this.mGet.getActivity());
    }

    public void onResumeBefore() {
        if (this.mSensorEventListener != null) {
            this.mSensorEventListener.register();
        }
        super.onResumeBefore();
    }

    public void onResumeAfter() {
        if (isProcessingFinishTask()) {
            this.mState = 6;
            showProcessingDialog(true, 0);
        } else {
            this.mIsOnRecording = false;
            this.mState = 0;
        }
        super.onResumeAfter();
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.setShutterButtonVisibility(4, getShutterButtonType());
            this.mCaptureButtonManager.changeButtonByMode(12);
        }
        if (!MDMUtil.allowMicrophone() || this.mGet.isLGUOEMCameraIntent() || TelephonyUtil.phoneInCall(this.mGet.getAppContext())) {
            setCaptureButtonEnable(false, getShutterButtonType());
        }
    }

    public void onPauseBefore() {
        this.mIsFeeding = false;
        CamLog.m3d(CameraConstants.TAG, "onPauseBefore mIsFeeding : " + this.mIsFeeding);
        stopEngine();
        super.onPauseBefore();
        if (this.mSensorEventListener != null) {
            this.mSensorEventListener.unregister();
        }
    }

    public void onPauseAfter() {
        CamLog.m3d(CameraConstants.TAG, "Panorama module onPause - start");
        resetByPausing();
        if (this.mCameraDevice != null) {
            CameraParameters params = this.mCameraDevice.getParameters();
            if (this.mInputType == 2 || this.mInputType == 1) {
                params.set("raw_ppmask_enable", "0");
            }
            if (params != null) {
                setParameterByLGSF(params, "mode_normal", false);
            }
        }
        if (this.mDrawingPanel != null) {
            this.mDrawingPanel.unbind();
        }
        if (this.mHandlerTaking != null) {
            this.mHandlerTaking.unbind();
        }
        CamLog.m3d(CameraConstants.TAG, "Panorama module onPause -end");
        super.onPauseAfter();
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mSensorEventListener != null) {
            this.mSensorEventListener.unbind();
            this.mSensorEventListener = null;
        }
        this.mPreviewMini = null;
        if (!(this.mPreviewImageMini == null || this.mPreviewImageMini.isRecycled())) {
            this.mPreviewImageMini.recycle();
            this.mPreviewImageMini = null;
        }
        if (this.mPreviewImageRGBBuffer != null) {
            this.mPreviewImageRGBBuffer.clear();
            this.mPreviewImageRGBBuffer = null;
        }
        if (!(this.mDisplayPreviewImageMini == null || this.mDisplayPreviewImageMini.isRecycled())) {
            this.mDisplayPreviewImageMini.recycle();
            this.mDisplayPreviewImageMini = null;
        }
        if (this.mDrawingPanel != null) {
            this.mDrawingPanel.unbind();
            this.mDrawingPanel = null;
        }
        if (this.mHandlerTaking != null) {
            this.mHandlerTaking.unbind();
            this.mHandlerTaking = null;
        }
        this.mTempParams = null;
        this.mSaveThread = null;
        if (this.mExecutor != null) {
            this.mExecutor.shutdown();
            try {
                this.mExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                this.mExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            this.mExecutor = null;
        }
        releaseFrameHashMap();
        releaseBackupFrame();
        releaseFrameDebug();
    }

    protected void initFirstRotationMatrix() {
        if (this.mSensorEventListener != null) {
            this.mSensorEventListener.getRotationMatrix(this.mRotationMatrix);
        }
        this.mFirstRotationMatrix = new Matrix3f(this.mRotationMatrix);
        this.mValidRotation = false;
    }

    protected void readSensorData() {
        if (this.mSensorEventListener != null) {
            this.mSensorEventListener.getRotationMatrix(this.mRotationMatrix);
            this.mSensorEventListener.getQuaternion(this.mQuaternion);
        }
        if (isEnableStop360()) {
            calculateRotationAngles();
            verify180Degrees();
            verify360Degrees();
        }
    }

    protected void calculateRotationAngles() {
        if (this.mFirstRotationMatrix == null) {
            CamLog.m7i(CameraConstants.TAG, "calculateRotationAngles mFirstRotationMatrix is null. return.");
            return;
        }
        Matrix3f firstMatrix = new Matrix3f(this.mFirstRotationMatrix.getArray());
        Matrix3f currentRotation = new Matrix3f(this.mRotationMatrix);
        currentRotation.transpose();
        firstMatrix.multiply(currentRotation);
        firstMatrix.transpose();
        float[] delta = firstMatrix.getArray();
        double normZ = Math.sqrt((double) (((delta[2] * delta[2]) + (delta[5] * delta[5])) + (delta[8] * delta[8])));
        double[] z = new double[]{((double) delta[2]) / normZ, ((double) delta[5]) / normZ, ((double) delta[8]) / normZ};
        double normHoriz = Math.sqrt((z[0] * z[0]) + (z[2] * z[2]));
        this.mAngleX = Math.IEEEremainder(Math.atan2(z[0] / normHoriz, z[2] / normHoriz) + 6.283185307179586d, 6.283185307179586d);
        double normVert = Math.sqrt((z[1] * z[1]) + (z[2] * z[2]));
        this.mAngleY = Math.IEEEremainder(Math.atan2(z[1] / normVert, z[2] / normVert) + 6.283185307179586d, 6.283185307179586d);
        this.mAngleX = Math.toDegrees(this.mAngleX);
        this.mAngleY = Math.toDegrees(this.mAngleY);
        this.mAngleX = (this.mAngleX + GOAL_ANGLE_DEFAULT) % GOAL_ANGLE_DEFAULT;
        this.mAngleY = (this.mAngleY + GOAL_ANGLE_DEFAULT) % GOAL_ANGLE_DEFAULT;
    }

    protected void verify360Degrees() {
        this.mRotationDirection = getRotationDirection(this.mDisplayRotation);
        if (this.mValidRotation) {
            CamLog.m3d(CameraConstants.TAG, "verify360Degrees mAngleX : " + this.mAngleX + ", mMaxAngleRotationX : " + this.mMaxAngleRotationX);
            switch (this.mRotationDirection) {
                case 0:
                    if (this.mAngleX < GOAL_ANGLE_DEFAULT - this.mMaxAngleRotationX && this.mAngleX > 5.0d) {
                        stopPanorama360();
                        return;
                    }
                    return;
                case 1:
                    if (this.mAngleX > this.mMaxAngleRotationX && this.mAngleX < 355.0d) {
                        stopPanorama360();
                        return;
                    }
                    return;
                case 2:
                    if (this.mAngleY > this.mMaxAngleRotationY && this.mAngleY < 355.0d) {
                        stopPanorama360();
                        return;
                    }
                    return;
                case 3:
                    if (this.mAngleY < GOAL_ANGLE_DEFAULT - this.mMaxAngleRotationY && this.mAngleY > 5.0d) {
                        stopPanorama360();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    protected void verify180Degrees() {
        switch (this.mRotationDirection) {
            case 0:
            case 1:
                if (this.mAngleX < 180.0d && this.mAngleX > 100.0d) {
                    this.mValidRotation = true;
                    return;
                }
                return;
            case 2:
            case 3:
                if (this.mAngleY < 180.0d && this.mAngleY > 100.0d) {
                    this.mValidRotation = true;
                    return;
                }
                return;
            default:
                return;
        }
    }

    protected int getRotationDirection(int displayOrientation) {
        if (displayOrientation == 0) {
            return setRotateDirection0(displayOrientation);
        }
        if (displayOrientation == 90) {
            return setRotateDirection90(displayOrientation);
        }
        if (displayOrientation == 180) {
            return setRotateDirection180(displayOrientation);
        }
        if (displayOrientation == 270) {
            return setRotateDirection270(displayOrientation);
        }
        return 0;
    }

    protected int setRotateDirection0(int displayOrientation) {
        if (this.mCaptureDirection == 1) {
            return 0;
        }
        if (this.mCaptureDirection == 2) {
            return 1;
        }
        if (this.mCaptureDirection == 4) {
            return 2;
        }
        if (this.mCaptureDirection == 3) {
            return 3;
        }
        return -1;
    }

    protected int setRotateDirection90(int displayOrientation) {
        if (this.mCaptureDirection == 1) {
            return 2;
        }
        if (this.mCaptureDirection == 2) {
            return 3;
        }
        if (this.mCaptureDirection == 4) {
            return 1;
        }
        if (this.mCaptureDirection == 3) {
            return 0;
        }
        return -1;
    }

    protected int setRotateDirection180(int displayOrientation) {
        if (this.mCaptureDirection == 1) {
            return 1;
        }
        if (this.mCaptureDirection == 2) {
            return 0;
        }
        if (this.mCaptureDirection == 4) {
            return 3;
        }
        if (this.mCaptureDirection == 3) {
            return 2;
        }
        return -1;
    }

    protected int setRotateDirection270(int displayOrientation) {
        if (this.mCaptureDirection == 1) {
            return 3;
        }
        if (this.mCaptureDirection == 2) {
            return 2;
        }
        if (this.mCaptureDirection == 4) {
            return 0;
        }
        if (this.mCaptureDirection == 3) {
            return 1;
        }
        return -1;
    }

    protected void stopPanorama360() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                CamLog.m3d(CameraConstants.TAG, "stopPanorama360() : stopPanorama on UI thread.");
                PanoramaModuleLGImpl.this.stopPanorama(true, true);
            }
        });
    }

    protected boolean isEnableStop360() {
        return false;
    }

    /* JADX WARNING: Missing block: B:48:?, code:
            return;
     */
    protected void stopPanoramabyEngine(boolean r10, boolean r11) {
        /*
        r9 = this;
        r8 = 1;
        r1 = "CameraApp";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "stopPanoramabyEngine - aborting : ";
        r2 = r2.append(r3);
        r2 = r2.append(r10);
        r3 = ", stop360 : ";
        r2 = r2.append(r3);
        r2 = r2.append(r11);
        r2 = r2.toString();
        com.lge.camera.util.CamLog.m3d(r1, r2);
        r1 = r9.mPanorama;
        if (r1 == 0) goto L_0x002f;
    L_0x0027:
        r1 = r9.mPanorama;
        r1 = r1.isBusy();
        if (r1 != 0) goto L_0x0037;
    L_0x002f:
        r1 = "CameraApp";
        r2 = "SaveThreadLG mPanorama is null or Engine is not busy. return.";
        com.lge.camera.util.CamLog.m11w(r1, r2);
    L_0x0036:
        return;
    L_0x0037:
        r0 = 0;
        r1 = r9.isEnableStop360();
        if (r1 == 0) goto L_0x004c;
    L_0x003e:
        r1 = r9.mRotationDirection;
        if (r1 != 0) goto L_0x0065;
    L_0x0042:
        r2 = r9.mAngleX;
        r1 = r9.mViewAngleH;
        r4 = (double) r1;
        r1 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));
        if (r1 >= 0) goto L_0x0065;
    L_0x004b:
        r0 = 1;
    L_0x004c:
        r2 = STOP_ENGINE_SYNC;
        monitor-enter(r2);
        if (r10 == 0) goto L_0x009a;
    L_0x0051:
        r1 = r9.mState;	 Catch:{ all -> 0x0062 }
        r3 = 5;
        if (r1 == r3) goto L_0x005b;
    L_0x0056:
        r1 = r9.mState;	 Catch:{ all -> 0x0062 }
        r3 = 4;
        if (r1 != r3) goto L_0x009a;
    L_0x005b:
        r1 = r9.mPanorama;	 Catch:{ all -> 0x0062 }
        r1.abort();	 Catch:{ all -> 0x0062 }
        monitor-exit(r2);	 Catch:{ all -> 0x0062 }
        goto L_0x0036;
    L_0x0062:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x0062 }
        throw r1;
    L_0x0065:
        r1 = r9.mRotationDirection;
        if (r1 != r8) goto L_0x0077;
    L_0x0069:
        r2 = r9.mAngleX;
        r4 = r9.mMaxAngleRotationX;
        r1 = r9.mViewAngleH;
        r6 = (double) r1;
        r4 = r4 - r6;
        r1 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));
        if (r1 <= 0) goto L_0x0077;
    L_0x0075:
        r0 = 1;
        goto L_0x004c;
    L_0x0077:
        r1 = r9.mRotationDirection;
        r2 = 2;
        if (r1 != r2) goto L_0x008a;
    L_0x007c:
        r2 = r9.mAngleY;
        r4 = r9.mMaxAngleRotationY;
        r1 = r9.mViewAngleV;
        r6 = (double) r1;
        r4 = r4 - r6;
        r1 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));
        if (r1 <= 0) goto L_0x008a;
    L_0x0088:
        r0 = 1;
        goto L_0x004c;
    L_0x008a:
        r1 = r9.mRotationDirection;
        r2 = 3;
        if (r1 != r2) goto L_0x004c;
    L_0x008f:
        r2 = r9.mAngleY;
        r1 = r9.mViewAngleV;
        r4 = (double) r1;
        r1 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));
        if (r1 >= 0) goto L_0x004c;
    L_0x0098:
        r0 = 1;
        goto L_0x004c;
    L_0x009a:
        if (r0 == r8) goto L_0x009e;
    L_0x009c:
        if (r11 == 0) goto L_0x00a5;
    L_0x009e:
        r1 = r9.mPanorama;	 Catch:{ all -> 0x0062 }
        r1.stop360();	 Catch:{ all -> 0x0062 }
    L_0x00a3:
        monitor-exit(r2);	 Catch:{ all -> 0x0062 }
        goto L_0x0036;
    L_0x00a5:
        r1 = r9.mPanorama;	 Catch:{ all -> 0x0062 }
        r1.stop();	 Catch:{ all -> 0x0062 }
        goto L_0x00a3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.app.ext.PanoramaModuleLGImpl.stopPanoramabyEngine(boolean, boolean):void");
    }

    protected synchronized String getPanoramaPictureSize(int cameraId) {
        String str;
        if (this.mInputType == 0) {
            ListPreference listPref = this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), cameraId));
            if (listPref != null) {
                CharSequence[] entryValues = listPref.getEntryValues();
                for (String value : entryValues) {
                    CamLog.m3d(CameraConstants.TAG, "LG pano picture size setting entry value : " + value);
                    int[] size = Utils.sizeStringToArray(value);
                    if (ModelProperties.isLongLCDModel()) {
                        if (Utils.isSquarePictureSize(size)) {
                            this.mPanoramaPictureSize = value;
                        }
                    } else if (Utils.isWidePictureSize(size)) {
                        this.mPanoramaPictureSize = value;
                    }
                }
                CamLog.m3d(CameraConstants.TAG, "LG pano getPanoramaPictureSize value : " + this.mPanoramaPictureSize);
                str = this.mPanoramaPictureSize;
            }
        }
        str = super.getPanoramaPictureSize(cameraId);
        return str;
    }

    protected void createPreviewImageMiniBitmap(int width, int height, int previewW, int previewH) {
        this.mPreviewImageRGBBuffer = ByteBuffer.allocateDirect((width * height) * 4);
        super.createPreviewImageMiniBitmap(width, height, previewW, previewH);
    }

    protected void setSignatureSetting() {
    }

    protected void restoreSignatureSetting() {
    }

    public String getShotMode() {
        return CameraConstants.MODE_PANORAMA_LG;
    }

    protected boolean isProcessingFinishTask() {
        return this.mWaitSavingCompleted && this.mState == 6;
    }

    protected String getPanoramaFileName(boolean needOriginalPath) {
        return this.mFileName;
    }

    protected String getPanoramaFileDir(boolean needOriginalPath) {
        return this.mDirectory;
    }

    protected void needSaveInStopPanorama() {
        this.mWaitSavingCompleted = true;
    }

    protected void setWideDistCorrectionValue(int sensorActiveWidth, int sensorActiveHeight) {
        if (this.mLensCorrectionMode == 1) {
            this.mPrincipal_point[0] = ((float) sensorActiveWidth) / 2.0f;
            this.mPrincipal_point[1] = ((float) sensorActiveHeight) / 2.0f;
        } else if (this.mLensCorrectionMode == 2) {
            String[] distValues = ModelProperties.get360PanoramaDistortionCorrection();
            if (distValues != null && getCameraId() == 2) {
                this.mUndistort_matrix[0] = Float.valueOf(distValues[0]).floatValue();
                this.mUndistort_matrix[1] = Float.valueOf(distValues[1]).floatValue();
                this.mUndistort_matrix[2] = Float.valueOf(distValues[2]).floatValue();
                this.mUndistort_matrix[3] = Float.valueOf(distValues[3]).floatValue();
            }
        }
    }

    protected long getFrameDelay() {
        String[] distValues = ModelProperties.get360PanoramaDistortionCorrection();
        int value_idx = getCameraId() == 2 ? 5 : 4;
        if (distValues != null && distValues.length > value_idx) {
            this.mFrameDelay = (long) ((Float.valueOf(distValues[value_idx]).floatValue() / 30.0f) * 1000.0f);
            CamLog.m7i(CameraConstants.TAG, "frameDelay : " + this.mFrameDelay);
        }
        return this.mFrameDelay;
    }

    protected void resetTimestamps() {
        this.mFirstSysTimestampOffset = -1;
        this.mFirstCamTimestampOffset = -1;
    }

    protected void setFisrtTimestamp(long curTime, long camTime) {
        if (this.mFirstSysTimestampOffset == -1) {
            this.mFirstSysTimestampOffset = curTime;
            this.mFirstCamTimestampOffset = camTime;
        }
    }

    protected long getTimeStamp(long imageTimeStamp) {
        return this.mFirstSysTimestampOffset + (imageTimeStamp - this.mFirstCamTimestampOffset);
    }
}
