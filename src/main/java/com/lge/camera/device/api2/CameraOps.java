package com.lge.camera.device.api2;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera.Area;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.DngCreator;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.InputConfiguration;
import android.location.Location;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.ImageWriter;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.Toast;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.device.CameraCallbackForwards.AFCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.AFMoveCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.CameraErrorCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.FaceDetectionCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.OneShotCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.ShutterCallbackForward;
import com.lge.camera.device.CameraOpsModuleBridge;
import com.lge.camera.device.ICameraCallback.CameraBacklightDetectionCallback;
import com.lge.camera.device.ICameraCallback.CameraHistogramDataCallback;
import com.lge.camera.device.ICameraCallback.CameraImageCallback;
import com.lge.camera.device.ICameraCallback.CameraImageMetaCallback;
import com.lge.camera.device.ICameraCallback.CameraLowlightDetectionCallback;
import com.lge.camera.device.ICameraCallback.CineZoomCallback;
import com.lge.camera.device.ICameraCallback.OutFocusCallback;
import com.lge.camera.device.ICameraCallback.ZoomChangeCallback;
import com.lge.camera.device.LGCameraMetaDataCallback;
import com.lge.camera.device.OutfocusCaptureResult;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.api2.BlockingCameraManager.BlockingOpenException;
import com.lge.camera.device.api2.OutFocusImageManager.OnOutFocusImageListener;
import com.lge.camera.solution.SolutionManager;
import com.lge.camera.solution.SolutionPickResult;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CamLog.TraceTag;
import com.lge.hardware.LGCamera.EVCallbackListener;
import com.lge.panorama.Panorama;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CameraOps implements ICameraOps, OnOutFocusImageListener {
    private static final int GATHERING_CNT_RAW = 1;
    private static final int GATHERING_TIME_FLASH_NR = 2000;
    private static final int GATHERING_TIME_RAW = 5000;
    private static final int STATUS_OK = 2;
    private static final int STATUS_UNINITIALIZED = 1;
    private ActionStateMachine mActionStateMachine = null;
    private AdvancedZoomManager mAdvancedZoomManager = null;
    private final BlockingCameraManager mBlockingCameraManager;
    private BuilderSet mBuilderSet = null;
    private CameraDevice mCamera;
    private CameraCharacteristics mCameraCharacteristics;
    private CameraErrorCallbackForward mCameraErrorCallback;
    private int mCameraId = 0;
    private final CameraManager mCameraManager;
    private Handler mCameraOpsHandler;
    private Handler mCaptureHandler;
    private CameraImageMetaCallback mCaptureMetaCallback;
    private Context mContext;
    private final BlockingStateListener mDeviceListener = new BlockingStateListener();
    private Runnable mDoBinningBurst = new C069511();
    private Runnable mDoSolution = new C069410();
    private Handler mFullFrameImageHandler;
    private CaptureCallback mGeneralCaptureListener = new C06981();
    private CameraOpsModuleBridge mGet;
    private final ImageCallbackManager mImageCallbackManager = new ImageCallbackManager();
    private boolean mIsRecording = false;
    ImageWriter mJpegImageWriter;
    private CameraCaptureSession mMainSession = null;
    private MultiFrameBufferManager mMultiFrameBufferManager;
    private OutFocusImageManager mOutFocusImageManager;
    private OutputManager mOutputManager;
    private Parameters2 mParametersDevice = null;
    private Handler mPreviewHandler;
    private Handler mPreviewImageHandler;
    private Builder mPreviewRequestBuilder;
    private SurfaceTexture mPreviewSurfaceTexture;
    private SurfaceHolder mPreviewSurfaceView;
    private MultiFrameBufferManager mRawFrameBuffer;
    SolutionPickResult mShotSolutionPicked = null;
    private SnapShotManager mSnapShotManager = new SnapShotManager();
    private SolutionManager mSolutionManager = SolutionManager.getInstance();
    private StatManager mStatManager;
    private int mStatus = 1;

    /* renamed from: com.lge.camera.device.api2.CameraOps$10 */
    class C069410 implements Runnable {
        C069410() {
        }

        public void run() {
            if (CameraOps.this.mSnapShotManager.getState() != 0 && CameraOps.this.mShotSolutionPicked != null) {
                ArrayList<ImageItem> items;
                CameraOps.this.mSnapShotManager.setProcessingShot();
                CamLog.m7i(CameraConstants.TAG, "Enabled Solution " + CameraOps.this.mShotSolutionPicked.getEnabledSolutions());
                if (CameraOps.this.mShotSolutionPicked.isEnabledSolution(4)) {
                    items = CameraOps.this.getHDRImageItems();
                } else {
                    items = CameraOps.this.mMultiFrameBufferManager.poll(CameraOps.this.mShotSolutionPicked.getFrameCount(), true);
                }
                if (CameraOps.this.mShotSolutionPicked.getFrameCount() > 1) {
                    CameraOps.this.mMultiFrameBufferManager.stopBuffering();
                }
                if (items == null || items.size() == 0) {
                    CameraOps.this.mSnapShotManager.sendDropCallback(CameraOps.this.mCameraErrorCallback, CameraOps.this.mShotSolutionPicked);
                    CameraOps.this.mSnapShotManager.doneShot(false);
                    CameraOps.this.mMultiFrameBufferManager.startBuffering();
                    return;
                }
                CameraOps.this.mSnapShotManager.sendShotStateCallback(CameraOps.this.mCameraErrorCallback, CameraOps.this.mShotSolutionPicked);
                CamLog.m7i(CameraConstants.TAG, " Needed Frame Count " + CameraOps.this.mShotSolutionPicked.getFrameCount() + " Get Frame Count = " + items.size());
                Image[] imageArray = new Image[items.size()];
                TotalCaptureResult[] resultArray = new TotalCaptureResult[items.size()];
                int i = 0;
                TotalCaptureResult pickMeta = CameraOps.this.mShotSolutionPicked.getMeta();
                CamLog.m7i(CameraConstants.TAG, "pickMeta " + pickMeta);
                if (((ImageItem) items.get(0)).getMetadata() == null) {
                    ((ImageItem) items.get(0)).setMetadata(pickMeta, false);
                }
                Iterator it = items.iterator();
                while (it.hasNext()) {
                    ImageItem tmp = (ImageItem) it.next();
                    imageArray[i] = tmp.getImage();
                    int i2 = i + 1;
                    resultArray[i] = tmp.getMetadata() == null ? pickMeta : tmp.getMetadata();
                    i = i2;
                }
                if (!Camera2Util.isShutterSpeedLongerThan(CameraOps.this.mBuilderSet.getShootMode(), CameraOps.this.mParametersDevice.get(ParamConstants.KEY_MANUAL_SHUTTER_SPEED_ZSL), 0.5f)) {
                    CameraOps.this.mSnapShotManager.sendShutterCallback();
                }
                byte[] hiddenExif = CameraOps.this.mSolutionManager.processSolutions(resultArray, imageArray, CameraOps.this.mShotSolutionPicked);
                CamLog.m7i(CameraConstants.TAG, "Done solution processing");
                if (CameraOps.this.mSnapShotManager.getState() == 0) {
                    Iterator it2 = items.iterator();
                    while (it2.hasNext()) {
                        ((ImageItem) it2.next()).close();
                    }
                    CameraOps.this.mSnapShotManager.doneShot(false);
                    CameraOps.this.mMultiFrameBufferManager.startBuffering();
                    return;
                }
                CameraOps.this.processAfterSolutions(items, hiddenExif);
            }
        }
    }

    /* renamed from: com.lge.camera.device.api2.CameraOps$11 */
    class C069511 implements Runnable {
        C069511() {
        }

        public void run() {
            if (ParamConstants.VALUE_BINNING_MODE.equals(CameraOps.this.mParametersDevice.get(ParamConstants.KEY_BINNING_PARAM))) {
                CameraOps.this.mSnapShotManager.setProcessingShot();
                if (CameraOps.this.mMultiFrameBufferManager.getState() == 4) {
                    CameraOps.this.mMultiFrameBufferManager.startBuffering();
                }
                ArrayList<ImageItem> items = CameraOps.this.mMultiFrameBufferManager.poll(1, true);
                CameraOps.this.mMultiFrameBufferManager.stopBuffering();
                if (items == null || items.size() == 0) {
                    CameraOps.this.mSnapShotManager.sendDropCallback(CameraOps.this.mCameraErrorCallback, CameraOps.this.mShotSolutionPicked);
                    CameraOps.this.mSnapShotManager.doneShot(false);
                    CameraOps.this.mMultiFrameBufferManager.startBuffering();
                    return;
                }
                CameraOps.this.mSnapShotManager.sendShutterCallback();
                CamLog.m7i(CameraConstants.TAG, "Done binning burst processing");
                CameraOps.this.mSnapShotManager.addHiddenExif(null);
                CameraOps.this.doYUVtoJpeg(((ImageItem) items.get(0)).getImage());
                CameraOps.this.mSnapShotManager.doneShot(false);
                CameraOps.this.mMultiFrameBufferManager.startBuffering();
            }
        }
    }

    /* renamed from: com.lge.camera.device.api2.CameraOps$1 */
    class C06981 extends CaptureCallback {
        C06981() {
        }

        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            CamLog.m3d(CameraConstants.TAG, "onCaptureCompleted frameNumber : " + result.getFrameNumber());
            if (CameraOps.this.mSnapShotManager.isRawCapture() && CameraOps.this.mRawFrameBuffer != null) {
                CameraOps.this.mRawFrameBuffer.add(result, true);
            }
            if (CameraOps.this.mOutFocusImageManager != null && CameraOps.this.mOutFocusImageManager.isEnabled()) {
                CameraOps.this.mOutFocusImageManager.add(result, true);
            }
        }

        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            CamLog.m3d(CameraConstants.TAG, "onCaptureStarted timestamp : " + timestamp + " frameNumber : " + frameNumber);
            if (!(CameraOps.this.mOutFocusImageManager == null ? false : CameraOps.this.mOutFocusImageManager.isSecondShot(request)) && CameraOps.this.mSnapShotManager != null && CameraOps.this.mSnapShotManager.checkVaildRequest(request, timestamp)) {
                CameraOps.this.mSnapShotManager.sendShutterCallback();
            }
        }

        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure result) {
            CamLog.m3d(CameraConstants.TAG, "onCaptureFailed: " + result.getFrameNumber());
            if (CameraOps.this.mOutFocusImageManager == null || !CameraOps.this.mOutFocusImageManager.isEnabled()) {
                CameraOps.this.mSnapShotManager.sendDropCallback(CameraOps.this.mCameraErrorCallback, CameraOps.this.mShotSolutionPicked);
                CameraOps.this.mSnapShotManager.doneShot(request.getTag() != null);
                CameraOps.this.mSnapShotManager.doneRawShot();
                return;
            }
            CameraOps.this.mOutFocusImageManager.drop(request);
        }
    }

    /* renamed from: com.lge.camera.device.api2.CameraOps$2 */
    class C07002 extends CaptureCallback {

        /* renamed from: com.lge.camera.device.api2.CameraOps$2$1 */
        class C06991 implements Runnable {
            C06991() {
            }

            public void run() {
                if (CameraOps.this.mPreviewRequestBuilder != null) {
                    CameraOps.this.addTargetToBuilder(CameraOps.this.mPreviewRequestBuilder, CameraOps.this.mOutputManager.getSurface(3));
                    CameraOps.this.setRepeatingRequestToMainSession(CameraOps.this.mPreviewRequestBuilder);
                }
            }
        }

        C07002() {
        }

        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            CamLog.m7i(CameraConstants.TAG, "onCaptureStarted");
        }

        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            CamLog.m7i(CameraConstants.TAG, "Reprocess onCaptureCompleted, Focus:" + Camera2Converter.getStringFocusState(((Integer) result.get(CaptureResult.CONTROL_AF_STATE)).intValue()) + ", Exposure:" + Camera2Converter.getStringExposureState(((Integer) result.get(CaptureResult.CONTROL_AE_STATE)).intValue()));
            if (CameraOps.this.mBuilderSet.getShootMode() != 2) {
                CameraOps.this.mMultiFrameBufferManager.startBuffering();
                CameraOps.this.mCameraOpsHandler.post(new C06991());
            }
        }

        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            CamLog.m7i(CameraConstants.TAG, "Reprocess onCaptureFailed");
            CameraOps.this.mSnapShotManager.sendPictureCallback(null);
        }
    }

    /* renamed from: com.lge.camera.device.api2.CameraOps$3 */
    class C07013 implements Runnable {
        C07013() {
        }

        public void run() {
            CamLog.m7i(CameraConstants.TAG, "getRawImage ");
            if (CameraOps.this.mRawFrameBuffer == null || CameraOps.this.mParametersDevice == null || CameraOps.this.mParametersDevice.get(ParamConstants.KEY_MANUAL_SHUTTER_SPEED_ZSL) == null) {
                CamLog.m7i(CameraConstants.TAG, "getRawImage return b " + CameraOps.this.mRawFrameBuffer + "  param " + CameraOps.this.mParametersDevice + " ss " + CameraOps.this.mParametersDevice.get(ParamConstants.KEY_MANUAL_SHUTTER_SPEED_ZSL));
                CameraOps.this.mSnapShotManager.doneRawShot();
                return;
            }
            CameraOps.this.mRawFrameBuffer.startBuffering();
            if (CameraOps.this.mRawFrameBuffer.gatherImage(1, null, (long) ((int) (5000.0f + Float.valueOf(CameraOps.this.mParametersDevice.get(ParamConstants.KEY_MANUAL_SHUTTER_SPEED_ZSL)).floatValue())))) {
                ImageItem imageItem = CameraOps.this.mRawFrameBuffer.poll();
                if (imageItem != null) {
                    CameraOps.this.mSnapShotManager.setProcessingRawShot();
                    DngCreator dngCreator = new DngCreator(CameraOps.this.mCameraCharacteristics, imageItem.getMetadata());
                    dngCreator.setOrientation(Camera2Util.getExifOrientaion(CameraOps.this.mParametersDevice));
                    Size size = new Size(imageItem.getImage().getWidth(), imageItem.getImage().getHeight());
                    ByteBuffer copiedBuffer = Camera2Util.cloneByteBuffer(imageItem.getImage().getPlanes()[0].getBuffer());
                    imageItem.getImage().close();
                    CameraOps.this.mSnapShotManager.sendPictureCallbackForDng(dngCreator, copiedBuffer, size);
                }
            }
            CameraOps.this.mRawFrameBuffer.stopBuffering();
            CameraOps.this.mRawFrameBuffer.clearBuffer();
            CameraOps.this.mSnapShotManager.doneRawShot();
        }
    }

    /* renamed from: com.lge.camera.device.api2.CameraOps$4 */
    class C07024 implements OnImageAvailableListener {
        C07024() {
        }

        public void onImageAvailable(ImageReader reader) {
            if (!CameraOps.this.mImageCallbackManager.onFullFrameAvailable(reader)) {
                Image image = reader.acquireNextImage();
                if (image == null) {
                    return;
                }
                if (CameraOps.this.mMultiFrameBufferManager == null || !CameraOps.this.mMultiFrameBufferManager.add(image)) {
                    image.close();
                }
            }
        }
    }

    /* renamed from: com.lge.camera.device.api2.CameraOps$5 */
    class C07035 implements OnImageAvailableListener {
        C07035() {
        }

        public void onImageAvailable(ImageReader reader) {
            if (CameraOps.this.mMainSession == null) {
                CamLog.m7i(CameraConstants.TAG, " Main Session is null");
                Image image = reader.acquireNextImage();
                if (image != null) {
                    image.close();
                    return;
                }
                return;
            }
            CameraOps.this.mImageCallbackManager.onPreviewFrameAvailable(reader);
        }
    }

    /* renamed from: com.lge.camera.device.api2.CameraOps$6 */
    class C07046 implements OnImageAvailableListener {
        C07046() {
        }

        public void onImageAvailable(ImageReader reader) {
            CamLog.m3d(CameraConstants.TAG, "jpeg onImageAvailable");
            Image image = null;
            try {
                CameraOps.this.mSnapShotManager.setProcessingShot();
                image = reader.acquireNextImage();
                boolean isBurstShotImage = CameraOps.this.mSnapShotManager.isBurstShot(image.getTimestamp());
                if (image.getFormat() != 256 || !CameraOps.this.mSnapShotManager.checkVaildImage(image)) {
                    CamLog.m5e(CameraConstants.TAG, "Unexpected format: " + image.getFormat() + " Or Invaid image shot state " + CameraOps.this.mSnapShotManager.getState());
                    CameraOps.this.mSnapShotManager.doneShot(isBurstShotImage);
                    if (image != null && 1 != null) {
                        image.close();
                    }
                } else if (CameraOps.this.mOutFocusImageManager == null || !CameraOps.this.mOutFocusImageManager.isEnabled()) {
                    if (CameraOps.this.mBuilderSet != null) {
                        CameraOps.this.mBuilderSet.applyTakePictureState(CameraOps.this.mPreviewRequestBuilder, false);
                    }
                    CameraOps.this.setRepeatingRequestToMainSession(CameraOps.this.mPreviewRequestBuilder);
                    ByteBuffer jpegBuffer = image.getPlanes()[0].getBuffer();
                    byte[] jpegData = new byte[jpegBuffer.capacity()];
                    jpegBuffer.get(jpegData);
                    CameraOps.this.mSnapShotManager.sendPictureCallback(jpegData);
                    CameraOps.this.mSnapShotManager.doneShot(isBurstShotImage);
                    if (image != null && 1 != null) {
                        image.close();
                    }
                } else {
                    CameraOps.this.mOutFocusImageManager.add(image);
                    if (image != null && false) {
                        image.close();
                    }
                }
            } catch (IllegalArgumentException e) {
                if (CamLog.isTagExceptionLogOn()) {
                    e.printStackTrace();
                }
                if (image != null && 1 != null) {
                    image.close();
                }
            } catch (Throwable th) {
                if (!(image == null || 1 == null)) {
                    image.close();
                }
                throw th;
            }
        }
    }

    /* renamed from: com.lge.camera.device.api2.CameraOps$7 */
    class C07057 implements OnImageAvailableListener {
        C07057() {
        }

        public void onImageAvailable(ImageReader reader) {
            CamLog.m3d(CameraConstants.TAG, "Raw onImageAvailable");
            Image image = reader.acquireNextImage();
            if (image == null) {
                CamLog.m3d(CameraConstants.TAG, "Raw image null");
            } else if (CameraOps.this.mRawFrameBuffer == null) {
                image.close();
            } else if (!CameraOps.this.mRawFrameBuffer.add(image)) {
                image.close();
            }
        }
    }

    private void checkOk() {
        if (this.mStatus < 2) {
            throw new IllegalStateException(String.format("Device not OK: %d", new Object[]{Integer.valueOf(this.mStatus)}));
        }
    }

    public CameraOps(Context context, Handler handler, int cameraId) throws ApiFailureException {
        this.mContext = context;
        this.mCameraManager = (CameraManager) this.mContext.getSystemService("camera");
        if (this.mCameraManager == null) {
            throw new ApiFailureException("Can't connect to camera manager!");
        }
        this.mBlockingCameraManager = new BlockingCameraManager(this.mCameraManager);
        this.mStatus = 2;
        this.mCameraOpsHandler = handler;
        HandlerThread ht = new HandlerThread("CameraPreview");
        ht.start();
        this.mPreviewHandler = new Handler(ht.getLooper());
        ht = new HandlerThread("CameraCapture");
        ht.start();
        this.mCaptureHandler = new Handler(ht.getLooper());
        ht = new HandlerThread("PreviewImage");
        ht.start();
        this.mPreviewImageHandler = new Handler(ht.getLooper());
        ht = new HandlerThread("FullImage");
        ht.start();
        this.mFullFrameImageHandler = new Handler(ht.getLooper());
        this.mCameraId = cameraId;
        openCamera();
    }

    public CameraCharacteristics getCameraCharacteristics() {
        checkOk();
        if (this.mCameraCharacteristics != null) {
            return this.mCameraCharacteristics;
        }
        throw new IllegalStateException("CameraCharacteristics is not available");
    }

    public void closeDevice() throws ApiFailureException {
        checkOk();
        this.mCameraCharacteristics = null;
        this.mBuilderSet = null;
        this.mAdvancedZoomManager = null;
        this.mSolutionManager.closeSolutions();
        this.mActionStateMachine.resetActionState();
        this.mMultiFrameBufferManager.clearBuffer();
        this.mPreviewRequestBuilder = null;
        if (this.mRawFrameBuffer != null) {
            this.mRawFrameBuffer.clearBuffer();
        }
        if (this.mCamera != null) {
            CamLog.traceBegin(TraceTag.OPTIONAL, "CameraClosed", 1000);
            try {
                CamLog.m7i(CameraConstants.TAG, "CloseCamera-start");
                this.mCamera.close();
                CamLog.m7i(CameraConstants.TAG, "CloseCamera-end");
                CamLog.traceEnd(TraceTag.OPTIONAL, "CameraClosed", 1000);
                this.mCamera = null;
                this.mMultiFrameBufferManager = null;
                this.mRawFrameBuffer = null;
                this.mStatManager = null;
                closeImageStream();
                if (this.mOutputManager != null) {
                    this.mOutputManager.closeBackupImageStream();
                }
                this.mOutputManager = null;
                quitHandlers();
            } catch (Exception e) {
                throw new ApiFailureException("can't close device!", e);
            }
        }
    }

    private void quitHandlers() {
        if (this.mPreviewHandler != null) {
            this.mPreviewHandler.getLooper().quit();
            this.mPreviewHandler = null;
        }
        if (this.mCaptureHandler != null) {
            this.mCaptureHandler.getLooper().quit();
            this.mCaptureHandler = null;
        }
        if (this.mFullFrameImageHandler != null) {
            this.mFullFrameImageHandler.getLooper().quit();
            this.mFullFrameImageHandler = null;
        }
        if (this.mPreviewImageHandler != null) {
            this.mPreviewImageHandler.getLooper().quit();
            this.mPreviewImageHandler = null;
        }
    }

    private void openCamera() throws ApiFailureException {
        if (this.mCamera == null) {
            try {
                CamLog.m7i(CameraConstants.TAG, "Camera id = " + this.mCameraId);
                if (this.mParametersDevice == null) {
                    this.mParametersDevice = new Parameters2();
                }
                this.mMultiFrameBufferManager = new MultiFrameBufferManager();
                this.mStatManager = new StatManager(this.mPreviewHandler, this);
                this.mActionStateMachine = new ActionStateMachine(this.mCameraOpsHandler, this);
                this.mCamera = this.mBlockingCameraManager.openCamera(Integer.toString(this.mCameraId), this.mDeviceListener, this.mPreviewHandler);
                this.mCameraCharacteristics = this.mCameraManager.getCameraCharacteristics(this.mCamera.getId());
                this.mSolutionManager.openSolutions(this.mCameraId);
                initParametersDevice();
                this.mBuilderSet = new BuilderSet(this.mParametersDevice, this.mCameraCharacteristics);
                this.mAdvancedZoomManager = new AdvancedZoomManager(this.mCameraOpsHandler, this);
                this.mStatManager.setBuilder(this.mBuilderSet);
                this.mOutputManager = new OutputManager(this, this.mCameraCharacteristics, this.mPreviewImageHandler, this.mFullFrameImageHandler, this.mCaptureHandler);
            } catch (CameraAccessException e) {
                throw new ApiFailureException("open failure", e);
            } catch (BlockingOpenException e2) {
                throw new ApiFailureException("open async failure", e2);
            }
        }
        this.mStatus = 2;
    }

    private void setPreviewRequestToMainSession() {
        CamLog.m3d(CameraConstants.TAG, "setRepeatingRequestForPreview");
        if (this.mCamera != null && this.mBuilderSet != null) {
            CamLog.m3d(CameraConstants.TAG, "setRepeatingRequestForPreview");
            try {
                this.mPreviewRequestBuilder = this.mCamera.createCaptureRequest(5);
                CamLog.m7i(CameraConstants.TAG, " mShotMode = " + this.mBuilderSet.mShotMode);
                addTargetToBuilder(this.mPreviewRequestBuilder, this.mOutputManager.getSurface(0));
                updateTargetList(false);
            } catch (CameraAccessException e) {
                CamLog.m5e(CameraConstants.TAG, "Error: createCaptureRequest");
            }
            this.mBuilderSet.applyCommonSettings(this.mPreviewRequestBuilder);
            this.mBuilderSet.applyFlash(this.mPreviewRequestBuilder);
            this.mBuilderSet.applyColorCorrectionGains(this.mPreviewRequestBuilder);
            setRepeatingRequestToMainSession(this.mPreviewRequestBuilder);
        }
    }

    private void setPreviewRequestToRecordingSession() {
        CamLog.m3d(CameraConstants.TAG, "setPreviewRequestToRecordingSession");
        if (this.mCamera != null && this.mBuilderSet != null) {
            try {
                this.mPreviewRequestBuilder = this.mCamera.createCaptureRequest(3);
                CamLog.m7i(CameraConstants.TAG, " mShotMode = " + this.mBuilderSet.mShotMode);
                addTargetToBuilder(this.mPreviewRequestBuilder, this.mOutputManager.getSurface(0));
                updateTargetList(false);
                this.mBuilderSet.applyCommonSettings(this.mPreviewRequestBuilder);
                this.mBuilderSet.applyFlash(this.mPreviewRequestBuilder);
                this.mBuilderSet.applySteadyCamMode(this.mPreviewRequestBuilder);
                this.mBuilderSet.applyVideoHDR(this.mPreviewRequestBuilder);
                this.mBuilderSet.applyHDR10(this.mPreviewRequestBuilder);
                this.mBuilderSet.applyCineEffect(this.mPreviewRequestBuilder);
            } catch (CameraAccessException e) {
                CamLog.m5e(CameraConstants.TAG, "Error: createCaptureRequest");
            }
            setRepeatingRequestToMainSession(this.mPreviewRequestBuilder);
        }
    }

    private void setRepeatingRequestToMainSession(Builder builder) {
        if (builder != null && this.mMainSession != null && !this.mAdvancedZoomManager.isJogZoomRunning()) {
            if (Camera2Util.isHighSpeedCaptureSession(this.mMainSession)) {
                setRepeatingRequestToHFRRecording(this.mPreviewRequestBuilder);
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "setPreviewRequestToMainSession");
            try {
                this.mMainSession.setRepeatingRequest(builder.build(), this.mStatManager, this.mPreviewHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (IllegalStateException e2) {
                e2.printStackTrace();
                CamLog.m5e(CameraConstants.TAG, "mMainSession.setRepeatingRequest IllegalStateException");
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
                CamLog.m5e(CameraConstants.TAG, "mMainSession.setRepeatingRequest IllegalArgumentException");
                CamLog.m5e(CameraConstants.TAG, "builder : " + builder.toString());
            }
            CamLog.m3d(CameraConstants.TAG, "setPreviewRequestToMainSession - e");
        }
    }

    private void setRepeatingRequestToHFRRecording(Builder builder) {
        if (builder != null && this.mMainSession != null && Camera2Util.isHighSpeedCaptureSession(this.mMainSession)) {
            CamLog.m3d(CameraConstants.TAG, "setRepeatingRequestToHFRRecording");
            try {
                CameraConstrainedHighSpeedCaptureSession highSpeedCaptureSession = this.mMainSession;
                highSpeedCaptureSession.setRepeatingBurst(highSpeedCaptureSession.createHighSpeedRequestList(builder.build()), this.mStatManager, this.mPreviewHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (IllegalStateException e2) {
                e2.printStackTrace();
                CamLog.m5e(CameraConstants.TAG, "mHighSpeedCaptureSession.setRepeatingRequest IllegalStateException");
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
                CamLog.m5e(CameraConstants.TAG, "mHighSpeedCaptureSession.setRepeatingRequest IllegalArgumentException");
                CamLog.m5e(CameraConstants.TAG, "builder : " + builder.toString());
            }
            CamLog.m3d(CameraConstants.TAG, "setRepeatingRequestToHFRRecording - e");
        }
    }

    public void startMainSession() throws ApiFailureException {
        if (this.mCamera == null) {
            CamLog.m5e(CameraConstants.TAG, "camera is not created");
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "startMainSession");
        if (this.mOutputManager.isValid(0)) {
            InputConfiguration inputConfig = null;
            try {
                if (this.mOutputManager.isReprocess()) {
                    inputConfig = new InputConfiguration(this.mMultiFrameBufferManager.getFrameWidth(), this.mMultiFrameBufferManager.getFrameHeight(), 35);
                }
                this.mMainSession = this.mBlockingCameraManager.createSession(Boolean.valueOf(this.mOutputManager.isReprocess()), this.mCamera, inputConfig, (ArrayList) this.mOutputManager.getCurrOutputSurfaces(), this.mPreviewHandler, CameraOperationMode.getOperationMode(this.mParametersDevice, false, false, this.mOutputManager.isReprocess(), this.mCameraId));
                if (!(this.mMainSession == null || this.mBuilderSet == null)) {
                    this.mBuilderSet.setHFRPreviewMode(false);
                }
                configuredMainSession();
                return;
            } catch (CameraAccessException e) {
                throw new ApiFailureException("Error setting up minimal preview", e);
            }
        }
        throw new ApiFailureException("surface is not created");
    }

    private void configuredMainSession() {
        this.mOutputManager.closeBackupImageStream();
        if (this.mMainSession == null) {
            CamLog.m5e(CameraConstants.TAG, " Session confiure Failed!");
            return;
        }
        this.mStatManager.setEnableMeta(true);
        CamLog.m3d(CameraConstants.TAG, "camera isReprocessable : " + this.mMainSession.isReprocessable());
        setPreviewRequestToMainSession();
        this.mMultiFrameBufferManager.startBuffering();
        if (this.mMainSession.isReprocessable()) {
            this.mJpegImageWriter = ImageWriter.newInstance(this.mMainSession.getInputSurface(), 1);
            this.mJpegImageWriter.setOnImageReleasedListener(null, this.mPreviewHandler);
        }
    }

    private void doReprocessYUVtoJpeg(Image inputImage, TotalCaptureResult captureResult) throws CameraAccessException {
        if (!(inputImage == null || this.mGet == null)) {
            this.mGet.composeSignatureImage(inputImage);
        }
        CamLog.m7i(CameraConstants.TAG, "Make jpeg");
        this.mJpegImageWriter.queueInputImage(inputImage);
        Builder reprocessReqs = this.mCamera.createReprocessCaptureRequest(captureResult);
        addTargetToBuilder(reprocessReqs, this.mOutputManager.getSurface(2));
        boolean enableCpp = this.mShotSolutionPicked == null || !this.mShotSolutionPicked.isEnabledSolution(4);
        this.mBuilderSet.applySettingForJpeg(reprocessReqs);
        this.mBuilderSet.applySettingForCpp(reprocessReqs, enableCpp);
        this.mMainSession.capture(reprocessReqs.build(), new C07002(), this.mCaptureHandler);
        CamLog.m7i(CameraConstants.TAG, "Make jpeg - e");
    }

    private boolean beforeStopPreview() {
        if (this.mBuilderSet == null || !this.mBuilderSet.applyBuilderBeforeStopPreview(this.mPreviewRequestBuilder)) {
            return true;
        }
        try {
            this.mMainSession.stopRepeating();
            doCameraSessionCapture(this.mPreviewRequestBuilder, this.mStatManager, this.mPreviewHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (IllegalStateException e2) {
            e2.printStackTrace();
        }
        return false;
    }

    public void stopPreview() {
        if (this.mMainSession != null) {
            boolean needStopCaptures = beforeStopPreview();
            CamLog.m3d(CameraConstants.TAG, "stopPreview needStopCaptures : " + needStopCaptures);
            this.mCaptureHandler.removeCallbacks(this.mDoSolution);
            this.mCaptureHandler.removeCallbacks(this.mDoBinningBurst);
            this.mSnapShotManager.waitShot();
            this.mSnapShotManager.waitRawShot();
            this.mSnapShotManager.reset();
            this.mSnapShotManager.stopPreview();
            this.mShotSolutionPicked = null;
            this.mIsRecording = false;
            this.mAdvancedZoomManager.reset();
            this.mSolutionManager.reset();
            initParametersDevice();
            if (this.mStatManager != null) {
                this.mStatManager.reset();
            }
            if (this.mBuilderSet != null) {
                this.mBuilderSet.reset();
            }
            if (this.mActionStateMachine != null) {
                this.mActionStateMachine.resetActionState();
            }
            if (this.mMultiFrameBufferManager != null) {
                this.mMultiFrameBufferManager.release();
            }
            if (this.mRawFrameBuffer != null) {
                this.mRawFrameBuffer.release();
            }
            if (this.mOutFocusImageManager != null) {
                this.mOutFocusImageManager.release();
                this.mOutFocusImageManager = null;
            }
            this.mImageCallbackManager.setPreviewImageCallback(null, this.mPreviewImageHandler, true);
            this.mImageCallbackManager.setFullFrameImageCallback(null, this.mFullFrameImageHandler, true);
            try {
                this.mPreviewRequestBuilder = null;
                if (!(this.mCamera == null || this.mMainSession == null)) {
                    if (needStopCaptures) {
                        CamLog.m7i(CameraConstants.TAG, " stopRepeating start");
                        CamLog.traceBegin(TraceTag.OPTIONAL, "stopRepeating", 1000);
                        this.mMainSession.stopRepeating();
                        CamLog.traceEnd(TraceTag.OPTIONAL, "stopRepeating", 1000);
                        CamLog.m7i(CameraConstants.TAG, " stopRepeating end");
                        CamLog.m7i(CameraConstants.TAG, " abortCaptures start ");
                        CamLog.traceBegin(TraceTag.OPTIONAL, "abortCaptures", 1000);
                        this.mMainSession.abortCaptures();
                        CamLog.traceEnd(TraceTag.OPTIONAL, "abortCaptures", 1000);
                        CamLog.m7i(CameraConstants.TAG, " abortCaptures end ");
                    }
                    CamLog.traceBegin(TraceTag.OPTIONAL, "SessionClose", 1000);
                    CamLog.m7i(CameraConstants.TAG, " close ");
                    this.mMainSession.close();
                    CamLog.traceEnd(TraceTag.OPTIONAL, "SessionClose", 1000);
                    this.mMainSession = null;
                    CamLog.m7i(CameraConstants.TAG, " mMainSession = null ");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (this.mOutputManager != null) {
                this.mOutputManager.clear();
                this.mOutputManager.backupImageStream();
            }
            closeImageStream();
        }
    }

    public void setRecordingStream(boolean isStart) {
        if (this.mBuilderSet == null || this.mPreviewRequestBuilder == null || this.mParametersDevice == null) {
            CamLog.m11w(CameraConstants.TAG, "mBuilderSet or mPreviewRequestBuilder or mParametersDevice is null. return");
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "apply recording startOfStream. isStart ? " + isStart);
        this.mBuilderSet.applyBuilder(this.mPreviewRequestBuilder, ParamConstants.KEY_RECORDING_START_OF_STREAM, Byte.valueOf(isStart ? (byte) 1 : (byte) 0));
    }

    public void setForStopRecording() {
        if (this.mBuilderSet == null || this.mPreviewRequestBuilder == null || this.mParametersDevice == null) {
            CamLog.m11w(CameraConstants.TAG, "mBuilderSet or mPreviewRequestBuilder or mParametersDevice is null. return");
        } else if (Camera2Util.isHighSpeedRecording(this.mParametersDevice)) {
            try {
                CamLog.m3d(CameraConstants.TAG, "stopRepeating for HFR stop - start");
                this.mMainSession.stopRepeating();
                CamLog.m3d(CameraConstants.TAG, "stopRepeating for HFR stop - end");
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeImageStream() {
        if (this.mOutputManager != null) {
            this.mOutputManager.closeImageStream();
        }
        if (this.mJpegImageWriter != null) {
            this.mJpegImageWriter.close();
            this.mJpegImageWriter = null;
        }
        this.mImageCallbackManager.close();
        CamLog.m7i(CameraConstants.TAG, "Close streams");
    }

    private void doYUVtoJpeg(Image image) {
        if (image != null) {
            if (this.mSnapShotManager.getCaptureCallback() == null) {
                image.close();
                return;
            }
            if (this.mGet != null) {
                this.mGet.composeSignatureImage(image);
            }
            ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
            yBuffer.rewind();
            ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();
            vBuffer.rewind();
            int[] strides = new int[]{image.getPlanes()[0].getRowStride(), image.getPlanes()[1].getRowStride(), image.getPlanes()[2].getRowStride()};
            int ySize = strides[0] * image.getHeight();
            byte[] yuvBuffer = new byte[((int) (((float) ySize) * 1.5f))];
            yBuffer.get(yuvBuffer, 0, yBuffer.remaining());
            vBuffer.get(yuvBuffer, ySize, vBuffer.remaining());
            YuvImage yuvImage = new YuvImage(yuvBuffer, 17, image.getWidth(), image.getHeight(), strides);
            ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 95, output_stream);
            image.close();
            this.mSnapShotManager.sendPictureCallback(output_stream.toByteArray());
        }
    }

    private void doReprocessCapture() throws CameraAccessException {
        ImageItem item;
        if (this.mBuilderSet.getShootMode() == 2) {
            item = this.mMultiFrameBufferManager.poll();
        } else {
            item = this.mMultiFrameBufferManager.getMatchedImage(true, this.mPreviewRequestBuilder);
        }
        if (item == null || !item.isValid()) {
            this.mMultiFrameBufferManager.startBuffering();
            this.mSnapShotManager.sendDropCallback(this.mCameraErrorCallback, null);
            return;
        }
        if (!Camera2Util.isShutterSpeedLongerThan(this.mBuilderSet.getShootMode(), this.mParametersDevice.get(ParamConstants.KEY_MANUAL_SHUTTER_SPEED_ZSL), 0.5f)) {
            this.mSnapShotManager.sendShutterCallback();
        }
        doReprocessYUVtoJpeg(item.getImage(), item.getMetadata());
    }

    public void doJpegCapture(boolean isFlash) throws ApiFailureException {
        int template = 2;
        if (this.mBuilderSet.isRecordMode()) {
            template = 4;
        } else if (this.mSnapShotManager.getState() == 4) {
            template = 5;
        } else if (!(this.mBuilderSet.getShootMode() == 16 || this.mSnapShotManager.getState() == 2 || (this.mShotSolutionPicked != null && this.mShotSolutionPicked.isEnabledSolution()))) {
            template = 5;
        }
        CamLog.m7i(CameraConstants.TAG, "Jpeg capture -s  template = " + template);
        try {
            Builder captureBuilder = this.mCamera.createCaptureRequest(template);
            this.mBuilderSet.applyTakePictureState(captureBuilder, true);
            addTargetToBuilder(captureBuilder, this.mOutputManager.getSurface(0));
            addTargetToBuilder(captureBuilder, this.mOutputManager.getSurface(2));
            if (this.mBuilderSet.isRecordMode()) {
                addTargetToBuilder(captureBuilder, this.mOutputManager.getSurface(1));
            } else if (this.mSnapShotManager.isRawCapture()) {
                addTargetToBuilder(captureBuilder, this.mOutputManager.getSurface(5));
                if (this.mRawFrameBuffer == null) {
                    this.mRawFrameBuffer = new MultiFrameBufferManager();
                }
                if (!this.mRawFrameBuffer.isConfigured()) {
                    this.mRawFrameBuffer.configureBuffer(Camera2Util.getRawSize(this.mCameraCharacteristics));
                }
            }
            this.mBuilderSet.applyCommonSettings(captureBuilder);
            this.mBuilderSet.applySettingForJpeg(captureBuilder);
            this.mBuilderSet.applySettingForCpp(captureBuilder, true);
            if (this.mShotSolutionPicked != null) {
                CamLog.m7i(CameraConstants.TAG, " shot solution " + this.mShotSolutionPicked.getEnabledSolutionName());
                this.mBuilderSet.applySolution(captureBuilder, this.mShotSolutionPicked.getEnabledSolutions(), this.mShotSolutionPicked.getFrameCount());
            }
            if (isFlash) {
                this.mBuilderSet.applyFlashForcedOn(captureBuilder, true);
            } else {
                this.mBuilderSet.applyFlash(captureBuilder);
            }
            this.mBuilderSet.applyBuilder(captureBuilder, ParamConstants.KEY_FLASH_SHOT, Boolean.valueOf(isFlash));
            this.mBuilderSet.applyManualVirtualExposureValue(captureBuilder, true);
            this.mSnapShotManager.setBurstTag(captureBuilder);
            try {
                if (this.mOutFocusImageManager == null || !this.mOutFocusImageManager.isEnabled()) {
                    this.mMainSession.capture(captureBuilder.build(), this.mGeneralCaptureListener, this.mCaptureHandler);
                } else {
                    CamLog.m7i(CameraConstants.TAG, " OutFocus Capture!");
                    List<CaptureRequest> requests = new ArrayList(2);
                    this.mBuilderSet.applyBuilder(captureBuilder, ParamConstants.KEY_OUTFOCUS_ORIGINAL_IMAGE, Boolean.valueOf(false));
                    requests.add(captureBuilder.build());
                    this.mBuilderSet.applyBuilder(captureBuilder, ParamConstants.KEY_OUTFOCUS_ORIGINAL_IMAGE, Boolean.valueOf(true));
                    requests.add(captureBuilder.build());
                    this.mMainSession.captureBurst(requests, this.mGeneralCaptureListener, this.mCaptureHandler);
                    this.mOutFocusImageManager.captureStart(this, requests);
                }
                this.mSnapShotManager.startRequestShot();
                if (this.mSnapShotManager.isRawCapture()) {
                    getRawImage();
                }
                CamLog.m7i(CameraConstants.TAG, "Jpeg capture -e ");
            } catch (CameraAccessException e) {
                throw new ApiFailureException("Error in createCaptureSession", e);
            }
        } catch (CameraAccessException e2) {
            throw new ApiFailureException("Error in createCaptureRequest", e2);
        }
    }

    private void getRawImage() {
        new Thread(new C07013()).start();
    }

    public OnImageAvailableListener getOnFullFrameImageAvailableListener() {
        return new C07024();
    }

    public OnImageAvailableListener getOnPreviewImageAvailableListener() {
        return new C07035();
    }

    public OnImageAvailableListener getOnJpegImageAvailableListener() {
        return new C07046();
    }

    public OnImageAvailableListener getOnRawImageAvailableListener() {
        return new C07057();
    }

    public void setPreviewTexture(SurfaceTexture surfaceTexture) {
        CamLog.m3d(CameraConstants.TAG, "setPreviewTexture surfaceTexture : " + surfaceTexture);
        this.mPreviewSurfaceTexture = surfaceTexture;
        this.mPreviewSurfaceView = null;
    }

    public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
        CamLog.m3d(CameraConstants.TAG, "setPreviewDisplay holder : " + surfaceHolder);
        this.mPreviewSurfaceView = surfaceHolder;
        this.mPreviewSurfaceTexture = null;
    }

    public void startPreviewForRecording(Surface recorderSurface) {
        CamLog.m3d(CameraConstants.TAG, "startPreviewForRecording");
        if (recorderSurface == null && this.mPreviewSurfaceTexture == null && this.mPreviewSurfaceView == null) {
            CamLog.m5e(CameraConstants.TAG, "Surface is not set; just return");
            return;
        }
        boolean isHighSpeedRecording = Camera2Util.isHighSpeedRecording(this.mParametersDevice);
        if (this.mMainSession == null) {
            if (isHighSpeedRecording && !Camera2Util.isSupportHighSpeedVideo(recorderSurface, this.mCameraCharacteristics)) {
                isHighSpeedRecording = false;
                Toast.makeText(this.mContext, "recordSurface size is not supported HighSpeedCaptureSession", 0).show();
            }
            setupPreviewSurface();
            this.mOutputManager.setSurface(1, recorderSurface);
            this.mOutputManager.configureOutputSurfaces(1, this.mParametersDevice, false);
            if (ParamConstants.VALUE_BINNING_MODE.equals(this.mParametersDevice.get(ParamConstants.KEY_BINNING_PARAM))) {
                this.mSolutionManager.determineAppShotSolution(this.mParametersDevice);
                if (this.mSolutionManager.IsEnableAppShotSolution()) {
                    this.mMultiFrameBufferManager.configureBuffer(this.mOutputManager.getFullFrameSize());
                } else {
                    this.mMultiFrameBufferManager.release();
                }
            }
            if (this.mOutputManager.isValid(0) && this.mOutputManager.isValid(1)) {
                try {
                    this.mMainSession = this.mBlockingCameraManager.createRecordingSession(Boolean.valueOf(isHighSpeedRecording), this.mCamera, (ArrayList) this.mOutputManager.getCurrOutputSurfaces(), this.mPreviewHandler, CameraOperationMode.getOperationMode(this.mParametersDevice, true, isHighSpeedRecording, this.mOutputManager.isReprocess(), this.mCameraId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                return;
            }
        }
        this.mOutputManager.closeBackupImageStream();
        if (this.mMainSession != null) {
            this.mStatManager.setEnableMeta(true);
            if (this.mBuilderSet != null) {
                this.mBuilderSet.setHFRPreviewMode(isHighSpeedRecording);
                this.mBuilderSet.setRecordMode(1);
            }
            setPreviewRequestToRecordingSession();
        }
    }

    public void preparePreview(Parameters2 params) {
        CamLog.m3d(CameraConstants.TAG, "preparePreview");
        if (this.mMainSession == null && this.mParametersDevice != null) {
            setParameters(params);
            if (this.mParametersDevice.getPreviewSize() != null) {
                this.mOutFocusImageManager = OutFocusImageManager.getInstance(this.mCameraId, this.mParametersDevice, this.mCaptureHandler);
                this.mOutputManager.configureOutputSurfaces(0, this.mParametersDevice, isReprocessSupported());
                if (this.mSolutionManager.IsEnableAppShotSolution()) {
                    this.mMultiFrameBufferManager.configureBuffer(this.mOutputManager.getFullFrameSize());
                } else {
                    this.mMultiFrameBufferManager.release();
                }
                InputConfiguration inputConfig = null;
                try {
                    if (this.mOutputManager.isReprocess()) {
                        inputConfig = new InputConfiguration(this.mMultiFrameBufferManager.getFrameWidth(), this.mMultiFrameBufferManager.getFrameHeight(), 35);
                    }
                    this.mMainSession = this.mBlockingCameraManager.createSession(Boolean.valueOf(this.mOutputManager.isReprocess()), this.mCamera, inputConfig, (ArrayList) this.mOutputManager.getCurrOutputConfigs(), this.mPreviewHandler, CameraOperationMode.getOperationMode(this.mParametersDevice, false, false, this.mOutputManager.isReprocess(), this.mCameraId));
                    CamLog.m7i(CameraConstants.TAG, "Session is created");
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                configuredMainSession();
            }
        }
    }

    public void startPreview() {
        try {
            CamLog.m3d(CameraConstants.TAG, "startPreview");
            if (this.mMainSession == null) {
                setupPreviewSurface();
                this.mOutFocusImageManager = OutFocusImageManager.getInstance(this.mCameraId, this.mParametersDevice, this.mCaptureHandler);
                this.mOutputManager.configureOutputSurfaces(0, this.mParametersDevice, isReprocessSupported());
                this.mSolutionManager.determineAppShotSolution(this.mParametersDevice);
                if (this.mSolutionManager.IsEnableAppShotSolution()) {
                    this.mMultiFrameBufferManager.configureBuffer(this.mOutputManager.getFullFrameSize());
                } else {
                    this.mMultiFrameBufferManager.release();
                }
                startMainSession();
                return;
            }
            if (this.mOutputManager.isWaitingPreviewSurface()) {
                CamLog.m7i(CameraConstants.TAG, " main session config done");
                setupPreviewSurface();
                finalizeOutputConfigurations();
            }
            setPreviewRequestToMainSession();
        } catch (ApiFailureException e) {
            e.printStackTrace();
        }
    }

    private void finalizeOutputConfigurations() {
        if (this.mMainSession == null) {
            CamLog.m5e(CameraConstants.TAG, "Can not config preview output surface");
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "finalizeOutputConfigurations");
        if (this.mOutputManager.finalizeOutputConfigurations()) {
            try {
                this.mMainSession.finalizeOutputConfigurations(this.mOutputManager.getCurrOutputConfigs());
                return;
            } catch (CameraAccessException e) {
                e.printStackTrace();
                return;
            }
        }
        CamLog.m5e(CameraConstants.TAG, "Can not config preview output surface");
    }

    public void setPreviewSurface(boolean set) {
        CamLog.m7i(CameraConstants.TAG, "setPreviewSurface set : " + set);
        if (this.mOutputManager.isValid(0) && this.mPreviewRequestBuilder != null) {
            if (set) {
                addTargetToBuilder(this.mPreviewRequestBuilder, this.mOutputManager.getSurface(0));
            } else {
                this.mPreviewRequestBuilder.removeTarget(this.mOutputManager.getSurface(0));
            }
            setRepeatingRequestToMainSession(this.mPreviewRequestBuilder);
        }
    }

    private void updateTargetList(boolean needRequest) {
        if (this.mMainSession == null) {
            CamLog.m3d(CameraConstants.TAG, "setPreviewCallback() fails; session is not created");
        } else if (this.mPreviewRequestBuilder != null) {
            if (this.mImageCallbackManager.isAddPreviewCallback()) {
                addTargetToBuilder(this.mPreviewRequestBuilder, this.mOutputManager.getSurface(4));
            } else {
                this.mPreviewRequestBuilder.removeTarget(this.mOutputManager.getSurface(4));
            }
            if (this.mImageCallbackManager.isAddFullFrameCallback() || this.mOutputManager.isValid(3)) {
                addTargetToBuilder(this.mPreviewRequestBuilder, this.mOutputManager.getSurface(3));
            } else {
                this.mPreviewRequestBuilder.removeTarget(this.mOutputManager.getSurface(3));
            }
            if (needRequest) {
                setRepeatingRequestToMainSession(this.mPreviewRequestBuilder);
            }
        }
    }

    public boolean checkPreviewCallbackHDR(int value) {
        if (ConfigurationUtil.sSHOT_SOLUTION_SUPPORTED != 1 || this.mPreviewRequestBuilder == null || !this.mOutputManager.isValid(4) || this.mParametersDevice == null || this.mCameraOpsHandler == null) {
            return false;
        }
        if (value == 1 && this.mImageCallbackManager.isHDRPreviewCallbackOn()) {
            return false;
        }
        if (value != 0 || this.mImageCallbackManager.isHDRPreviewCallbackOn()) {
            return true;
        }
        return false;
    }

    public void addPreviewCallbackTargetForHDR(final int value, final boolean needRequest) {
        if (checkPreviewCallbackHDR(value)) {
            this.mCameraOpsHandler.post(new Runnable() {
                public void run() {
                    boolean z = true;
                    if (CameraOps.this.checkPreviewCallbackHDR(value) && CameraOps.this.mPreviewRequestBuilder != null) {
                        ImageCallbackManager access$1300 = CameraOps.this.mImageCallbackManager;
                        if (value != 1) {
                            z = false;
                        }
                        access$1300.enalbeHDRPreviewCallback(z);
                        CameraOps.this.updateTargetList(needRequest);
                        CamLog.m3d(CameraConstants.TAG, "mPreviewCallbackAdd " + value);
                    }
                }
            });
        }
    }

    private int doCameraSessionCapture(Builder builder, CaptureCallback callback, Handler handler) throws CameraAccessException {
        if (Camera2Util.isHighSpeedCaptureSession(this.mMainSession)) {
            CameraConstrainedHighSpeedCaptureSession highSpeedCaptureSession = this.mMainSession;
            this.mBuilderSet.applyPreviewFpsRange(builder);
            List<CaptureRequest> builderBurst = highSpeedCaptureSession.createHighSpeedRequestList(builder.build());
            if (builderBurst == null) {
                return 0;
            }
            highSpeedCaptureSession.captureBurst(builderBurst, callback, handler);
            return ((CaptureRequest) builderBurst.get(builderBurst.size() - 1)).hashCode();
        } else if (builder == null) {
            return 0;
        } else {
            CaptureRequest request = builder.build();
            int requestCode = request.hashCode();
            try {
                this.mMainSession.capture(request, callback, handler);
                return requestCode;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                CamLog.m5e(CameraConstants.TAG, "mMainSession.capture IllegalArgumentException");
                CamLog.m5e(CameraConstants.TAG, "builder : " + builder.toString());
                return requestCode;
            }
        }
    }

    public void setOneShotPreviewCallback(OneShotCallbackForward cb) {
        if (this.mStatManager != null) {
            this.mStatManager.setOneShotPreviewCallback(cb);
        }
    }

    public void autoFocus(AFCallbackForward cb) {
        if (this.mMainSession != null && this.mBuilderSet != null) {
            boolean isPreFlash = false;
            if (isFlashRequired()) {
                isPreFlash = !this.mBuilderSet.isRecordMode();
            }
            this.mActionStateMachine.doActionForAutoFocus(isPreFlash, this.mMainSession.isReprocessable());
            if (this.mStatManager != null) {
                this.mStatManager.setAutoFocusCallback(cb);
            }
        }
    }

    public void setAutoFocusMoveCallback(AFMoveCallbackForward cb) {
        if (this.mStatManager != null) {
            this.mStatManager.setAutoFocusMoveCallback(cb);
        }
    }

    public void setFaceDetectionCallback(FaceDetectionCallbackForward cb) {
        if (this.mStatManager != null) {
            this.mStatManager.setFaceDetectionCallback(cb);
        }
    }

    public void setZoomChangeCallback(ZoomChangeCallback zoomChangeCallback) {
        if (this.mAdvancedZoomManager != null) {
            this.mAdvancedZoomManager.setZoomChangeCallback(zoomChangeCallback);
        }
    }

    public void setCineZoomCallback(CineZoomCallback cineZoomCallback) {
        if (this.mAdvancedZoomManager != null) {
            this.mAdvancedZoomManager.setCineZoomCallback(cineZoomCallback);
        }
    }

    public void setBacklightDetectionCallback(CameraBacklightDetectionCallback cb) {
        if (this.mStatManager != null) {
            this.mStatManager.setBacklightDetectionCallback(cb);
        }
    }

    public void setLowlightDetectionCallback(CameraLowlightDetectionCallback cb) {
        if (this.mStatManager != null) {
            this.mStatManager.setLowlightDetectionCallback(cb);
        }
    }

    public void setOutFocusCallback(OutFocusCallback cb) {
        if (this.mStatManager != null) {
            this.mStatManager.setOutFocusCallback(cb);
        }
        this.mSnapShotManager.setOutFocusCallback(cb);
    }

    public void setFaceDetection(boolean on) {
        CamLog.m3d(CameraConstants.TAG, "setFaceDetection " + on);
        if (this.mCamera != null && this.mPreviewRequestBuilder != null) {
            this.mBuilderSet.applyFaceDetection(this.mPreviewRequestBuilder, on);
            setRepeatingRequestToMainSession(this.mPreviewRequestBuilder);
        }
    }

    public void setErrorCallback(CameraErrorCallbackForward cb) {
        this.mCameraErrorCallback = cb;
    }

    public void setParameters(Parameters2 paramsNew) {
        if (this.mBuilderSet != null && paramsNew != null && this.mStatManager != null) {
            boolean previewUpdate = compareParameters(this.mParametersDevice, paramsNew);
            this.mParametersDevice = paramsNew.clone();
            if (this.mParametersDevice != null) {
                this.mStatManager.setDisableHDRMode(this.mParametersDevice.getInt("hdr-mode") == 0);
                if (this.mSnapShotManager.getState() != 4) {
                    this.mBuilderSet.setShootMode(this.mParametersDevice.get(ParamConstants.KEY_APP_SHOT_MODE));
                }
                this.mSolutionManager.setShootMode(this.mBuilderSet.getShootMode());
                SolutionPickResult pickResult = this.mSolutionManager.checkPreviewSolutions(null, this.mParametersDevice);
                this.mBuilderSet.setPreviewSolution(pickResult.getEnabledSolutions());
                CamLog.m7i(CameraConstants.TAG, " preview solution " + pickResult.getEnabledSolutionName());
                this.mBuilderSet.setInfoFromParams(this.mPreviewRequestBuilder, this.mParametersDevice);
                if (previewUpdate) {
                    setRepeatingRequestToMainSession(this.mPreviewRequestBuilder);
                }
                Camera2Util.showParameterValues(this.mParametersDevice);
                this.mAdvancedZoomManager.checkAdvancedZoom(this.mParametersDevice);
            }
        }
    }

    public void startZoomAction() {
        CamLog.m3d(CameraConstants.TAG, "[jog] startZoomAction");
        try {
            if (this.mMainSession != null) {
                this.mMainSession.stopRepeating();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void stopZoomAction() {
        CamLog.m3d(CameraConstants.TAG, "[jog] stopZoomAction");
        setRepeatingRequestToMainSession(this.mPreviewRequestBuilder);
    }

    public void setPointZoom(Rect rect) {
        if (this.mBuilderSet != null && this.mPreviewRequestBuilder != null && this.mStatManager != null && this.mPreviewHandler != null && this.mAdvancedZoomManager.isJogZoomRunning()) {
            if (this.mBuilderSet.setPointZoom(rect)) {
                this.mBuilderSet.applyZoom(this.mPreviewRequestBuilder);
                try {
                    this.mAdvancedZoomManager.addJogZoomRequest(doCameraSessionCapture(this.mPreviewRequestBuilder, this.mStatManager, this.mPreviewHandler));
                    return;
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                    return;
                }
            }
            this.mBuilderSet.applyZoom(this.mPreviewRequestBuilder);
            setRepeatingRequestToMainSession(this.mPreviewRequestBuilder);
        }
    }

    public void setJogZoom(int zoomValue) {
        if (this.mParametersDevice != null && this.mBuilderSet != null && this.mAdvancedZoomManager.isJogZoomRunning()) {
            this.mParametersDevice.setZoom(zoomValue);
            this.mBuilderSet.setZoom(zoomValue);
            this.mBuilderSet.applyZoom(this.mPreviewRequestBuilder);
            try {
                this.mAdvancedZoomManager.addJogZoomRequest(doCameraSessionCapture(this.mPreviewRequestBuilder, this.mStatManager, this.mPreviewHandler));
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean compareParameters(Parameters2 oldParam, Parameters2 newParam) {
        boolean retValue = true;
        if (oldParam == null || newParam == null) {
            return 1;
        }
        if (!(this.mActionStateMachine == null || !this.mActionStateMachine.isFlashPreCapturing() || oldParam.getExposureCompensation() == newParam.getExposureCompensation())) {
            retValue = false;
        }
        if (this.mActionStateMachine != null && this.mActionStateMachine.isFlashAction()) {
            String oldFlashMode = oldParam.getFlashMode();
            String newFlashMode = newParam.getFlashMode();
            if (!(oldFlashMode == null || newFlashMode == null || oldFlashMode.equals(newFlashMode) || (!"auto".equals(newFlashMode) && !"off".equals(newFlashMode)))) {
                CamLog.m3d(CameraConstants.TAG, "newFlashMode " + newFlashMode);
                this.mBuilderSet.applyFlashForcedOn(this.mPreviewRequestBuilder, false);
                this.mActionStateMachine.resetActionState();
            }
        }
        return retValue;
    }

    public void setSuperZoom(int zoomStep) {
        if (this.mBuilderSet != null) {
            this.mParametersDevice.setZoom(zoomStep);
            this.mBuilderSet.setZoom(zoomStep);
            this.mBuilderSet.setSuperZoomMode(true);
            this.mBuilderSet.applyZoom(this.mPreviewRequestBuilder);
            setRepeatingRequestToMainSession(this.mPreviewRequestBuilder);
        }
    }

    private void initParametersDevice() {
        if (this.mParametersDevice != null && this.mCameraCharacteristics != null) {
            CamLog.m7i(CameraConstants.TAG, "INFO_SUPPORTED_HARDWARE_LEVEL = " + this.mCameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL));
            this.mParametersDevice.removeAll();
            Camera2Util.initParametersZoom(this.mCameraCharacteristics, this.mParametersDevice);
            Camera2Util.initParametersEV(this.mCameraCharacteristics, this.mParametersDevice);
            Camera2Util.initParametersLG(this.mCameraCharacteristics, this.mParametersDevice);
            Camera2Util.initSupportedPictureSize(this.mCameraCharacteristics, this.mParametersDevice);
            Camera2Util.initMaxFaceCount(this.mCameraCharacteristics, this.mParametersDevice);
            Camera2Util.initVideoSnapshotSupported(this.mCameraCharacteristics, this.mParametersDevice);
            Camera2Util.initAFSupportedList(this.mCameraCharacteristics, this.mParametersDevice);
            Camera2Util.initFlashSupportedList(this.mCameraCharacteristics, this.mParametersDevice);
            Camera2Util.initOutputsList(this.mCameraCharacteristics, this.mParametersDevice);
            Camera2Util.initFocusInfo(this.mCameraCharacteristics, this.mParametersDevice);
            Camera2Util.initVideoHDRSupported(this.mCameraCharacteristics, this.mParametersDevice, this.mCameraId);
            Camera2Util.initAELockSupported(this.mCameraCharacteristics, this.mParametersDevice);
            Camera2Util.initParametersFromBuilder(this.mCamera, this.mParametersDevice);
            Camera2Util.initRawSize(this.mCameraCharacteristics, this.mParametersDevice);
            this.mParametersDevice.set("picture-size", "1920x1080");
            this.mParametersDevice.set(ParamConstants.KEY_PREVIEW_SIZE, "1920x1080");
        }
    }

    public Parameters2 getParameters() {
        if (this.mParametersDevice != null) {
            return this.mParametersDevice.clone();
        }
        CamLog.m5e(CameraConstants.TAG, "mParametersDevice is NULL");
        return null;
    }

    public void setLGManualModedataCb(LGCameraMetaDataCallback cb) {
        if (this.mStatManager != null) {
            this.mStatManager.setLGManualModedataCb(cb);
        }
    }

    public void setCameraHistogramDataCallback(CameraHistogramDataCallback cb) {
        if (this.mStatManager != null) {
            this.mStatManager.setCameraHistogramDataCallback(cb);
        }
    }

    public void setGPSlocation(Location loc) {
        this.mBuilderSet.setLocation(loc);
    }

    public void setEVCallbackDataListener(EVCallbackListener cb) {
        if (this.mStatManager != null) {
            this.mStatManager.setEVCallbackDataListener(cb);
        }
    }

    public List<Area> getMultiWindowFocusAreas() {
        List<Area> areaList = null;
        if (this.mStatManager != null) {
            areaList = this.mStatManager.getMWFocusAreas();
        }
        if (areaList != null) {
            return areaList;
        }
        areaList = new ArrayList();
        areaList.add(new Area(new Rect(-280, -280, 0, 0), 1));
        areaList.add(new Area(new Rect(-140, -420, 140, -140), 1));
        areaList.add(new Area(new Rect(0, -280, 280, 0), 1));
        areaList.add(new Area(new Rect(-420, -140, -140, 140), 1));
        areaList.add(new Area(new Rect(-140, -140, 140, 140), 1));
        areaList.add(new Area(new Rect(140, -140, Panorama.STATUS_ERROR_TOO_FAR_UP, 140), 1));
        areaList.add(new Area(new Rect(-280, 0, 0, 280), 1));
        areaList.add(new Area(new Rect(-140, 140, 140, Panorama.STATUS_ERROR_TOO_FAR_UP), 1));
        areaList.add(new Area(new Rect(0, 0, 280, 280), 1));
        return areaList;
    }

    public void cancelAutoFocus() {
        this.mActionStateMachine.doActionForCancelFocus();
    }

    public void setRecordSurface(boolean set) {
        boolean z = true;
        if (this.mOutputManager.isValid(1) && this.mPreviewRequestBuilder != null) {
            CamLog.m7i(CameraConstants.TAG, "set : " + set + " / cameraId : " + this.mCameraId);
            this.mSolutionManager.setRecordingState(set);
            this.mIsRecording = set;
            if (set) {
                addTargetToBuilder(this.mPreviewRequestBuilder, this.mOutputManager.getSurface(1));
            } else {
                this.mPreviewRequestBuilder.removeTarget(this.mOutputManager.getSurface(1));
            }
            if (Camera2Util.isHighSpeedCaptureSession(this.mMainSession) && this.mBuilderSet != null) {
                BuilderSet builderSet = this.mBuilderSet;
                if (set) {
                    z = false;
                }
                builderSet.setHFRPreviewMode(z);
                this.mBuilderSet.applyPreviewFpsRange(this.mPreviewRequestBuilder);
            }
            setRepeatingRequestToMainSession(this.mPreviewRequestBuilder);
        }
    }

    private boolean addTargetToBuilder(Builder builder, Surface surface) {
        if (builder == null || surface == null) {
            return false;
        }
        return this.mOutputManager.addTargetToBuilder(builder, surface);
    }

    public void onPreviewCaptureFailed(CaptureRequest request, CaptureFailure failure) {
        if (this.mMainSession == null) {
            CamLog.m7i(CameraConstants.TAG, " Main Session is null");
        } else {
            this.mActionStateMachine.onCaptureFailed(request);
        }
    }

    public void onMetaAvailable(TotalCaptureResult result, boolean isFocused) {
        if (this.mMainSession == null) {
            CamLog.m7i(CameraConstants.TAG, " Main Session is null");
            return;
        }
        int i;
        int prevEnabledSolution;
        if (this.mAdvancedZoomManager != null) {
            this.mAdvancedZoomManager.checkJogZoomRequest(result);
        }
        if (this.mActionStateMachine != null) {
            this.mActionStateMachine.onCaptureResult(result);
        }
        if (this.mStatManager.isHDRDetected()) {
            i = 1;
        } else {
            i = 0;
        }
        addPreviewCallbackTargetForHDR(i, true);
        if (this.mCaptureMetaCallback != null) {
            this.mCaptureMetaCallback.onImageMetaData(result);
        }
        if (!(this.mCameraOpsHandler == null || this.mBuilderSet == null)) {
            this.mBuilderSet.getCurrentContrast(result, this.mCameraOpsHandler);
        }
        final SolutionPickResult pickResult = this.mSolutionManager.checkPreviewSolutions(result, null);
        if (this.mBuilderSet != null) {
            prevEnabledSolution = this.mBuilderSet.getPreviewSolutionList();
        } else {
            prevEnabledSolution = 0;
        }
        if (!(pickResult.getEnabledSolutions() == prevEnabledSolution || this.mCameraOpsHandler == null || Camera2Util.isHighSpeedCaptureSession(this.mMainSession))) {
            CamLog.m7i(CameraConstants.TAG, " solution prev : " + prevEnabledSolution + " New " + pickResult.getEnabledSolutions());
            this.mCameraOpsHandler.post(new Runnable() {
                public void run() {
                    if (CameraOps.this.mBuilderSet != null && CameraOps.this.mPreviewRequestBuilder != null && CameraOps.this.mBuilderSet.getPreviewSolutionList() != pickResult.getEnabledSolutions()) {
                        CameraOps.this.mBuilderSet.setPreviewSolution(pickResult.getEnabledSolutions());
                        CameraOps.this.mBuilderSet.applySolution(CameraOps.this.mPreviewRequestBuilder, pickResult.getEnabledSolutions(), 1);
                        CameraOps.this.setRepeatingRequestToMainSession(CameraOps.this.mPreviewRequestBuilder);
                    }
                }
            });
        }
        if (!this.mSolutionManager.IsEnableAppShotSolution()) {
            this.mSolutionManager.setCaptureResult(result);
        } else if (this.mMultiFrameBufferManager != null && this.mMultiFrameBufferManager.isConfigured()) {
            if (this.mSnapShotManager.getState() == 0 && this.mBuilderSet != null) {
                this.mBuilderSet.setSuperZoomParam(this.mParametersDevice);
                this.mMultiFrameBufferManager.setBufferSize(this.mSolutionManager.getMultiFrameCount(result, this.mParametersDevice));
            }
            this.mMultiFrameBufferManager.add(result, isFocused);
        }
    }

    public Parameters2 prepareTakePicture(Parameters2 params, String shotMode) {
        boolean z = false;
        if (this.mSnapShotManager.isAvailableShot(this.mShotSolutionPicked)) {
            this.mBuilderSet.setShootMode(shotMode);
            if (shotMode == null || !shotMode.contains(CameraConstants.MODE_PANORAMA)) {
                TotalCaptureResult meta;
                boolean isFlashShot;
                CamLog.m7i(CameraConstants.TAG, " prepareTakePicture ");
                if (this.mBuilderSet.getShootMode() == 2) {
                    if (this.mSnapShotManager.getState() != 4) {
                        this.mSnapShotManager.setState(4);
                    }
                }
                if (!this.mSolutionManager.IsEnableAppShotSolution()) {
                    meta = this.mSolutionManager.getLastCaptureResult();
                } else if (this.mMultiFrameBufferManager == null || !this.mMultiFrameBufferManager.isConfigured()) {
                    CamLog.m7i(CameraConstants.TAG, "Current shot mode is : " + shotMode);
                } else {
                    ImageItem item = this.mMultiFrameBufferManager.getMatchedImage(false, this.mPreviewRequestBuilder);
                    if (item != null) {
                        meta = item.getMetadata();
                        this.mMultiFrameBufferManager.startBuffering();
                    }
                }
                this.mBuilderSet.setSuperZoomParam(params);
                if (isFlashRequired() || this.mActionStateMachine.isAction(3)) {
                    isFlashShot = true;
                } else {
                    isFlashShot = false;
                }
                params.set(ParamConstants.KEY_FLASH_FIRE, isFlashShot ? "on" : "off");
                this.mShotSolutionPicked = this.mSolutionManager.checkSnapShotSolutions(meta, this.mBuilderSet.getShootMode(), params);
                if (this.mOutFocusImageManager != null) {
                    OutFocusImageManager outFocusImageManager = this.mOutFocusImageManager;
                    if (this.mShotSolutionPicked != null && this.mShotSolutionPicked.isEnabledSolution(256)) {
                        z = true;
                    }
                    outFocusImageManager.setEnable(z);
                }
                if (this.mShotSolutionPicked != null) {
                    this.mShotSolutionPicked.setMeta(meta);
                    if (this.mShotSolutionPicked.isEnabledSolution(1)) {
                        params.set(ParamConstants.KEY_SUPERZOOM, "on");
                    } else if (this.mShotSolutionPicked.isEnabledHDRSolution()) {
                        params.set("hdr-mode", 1);
                    } else if (this.mShotSolutionPicked.isEnabledNightSolution()) {
                        params.setSceneMode(ParamConstants.SCENE_MODE_NIGHT);
                    }
                }
            } else {
                CamLog.m7i(CameraConstants.TAG, "Current shot mode is : " + shotMode);
            }
        }
        return params;
    }

    public void restoreParameters() {
        this.mBuilderSet.setShootMode(null);
        this.mSolutionManager.setShootMode(this.mBuilderSet.getShootMode());
    }

    private boolean applySolution() {
        if (this.mShotSolutionPicked == null || !this.mShotSolutionPicked.isEnabledSolution() || this.mBuilderSet.getShootMode() == 2) {
            return false;
        }
        this.mSnapShotManager.setState(3);
        if (!this.mSolutionManager.IsEnableAppShotSolution()) {
            return false;
        }
        CamLog.m7i(CameraConstants.TAG, " applySolution");
        this.mCaptureHandler.removeCallbacks(this.mDoSolution);
        this.mCaptureHandler.post(this.mDoSolution);
        return true;
    }

    private void processAfterSolutions(ArrayList<ImageItem> items, byte[] hiddenExif) {
        if (items == null || this.mMainSession == null) {
            this.mSnapShotManager.sendDropCallback(this.mCameraErrorCallback, this.mShotSolutionPicked);
            return;
        }
        this.mSnapShotManager.addHiddenExif(hiddenExif);
        for (int i = 1; i < items.size(); i++) {
            ((ImageItem) items.get(i)).close();
        }
        try {
            if (this.mMainSession.isReprocessable()) {
                doReprocessYUVtoJpeg(((ImageItem) items.get(0)).getImage(), ((ImageItem) items.get(0)).getMetadata());
                return;
            }
            doYUVtoJpeg(((ImageItem) items.get(0)).getImage());
            this.mSnapShotManager.doneShot(false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            ((ImageItem) items.get(0)).close();
            this.mSnapShotManager.sendDropCallback(this.mCameraErrorCallback, this.mShotSolutionPicked);
            this.mSnapShotManager.reset();
        }
    }

    private ArrayList<ImageItem> getHDRImageItems() {
        CamLog.m3d(CameraConstants.TAG, "getHDRImageItems");
        if (this.mPreviewRequestBuilder == null) {
            return null;
        }
        ArrayList<ImageItem> itemList = new ArrayList();
        this.mImageCallbackManager.setPreviewImageLock(true);
        int[] captureCnt = evalueHDRChecker(this.mImageCallbackManager.getPreviewImage());
        this.mImageCallbackManager.setPreviewImageLock(false);
        if (captureCnt == null || captureCnt.length == 0) {
            return itemList;
        }
        this.mPreviewRequestBuilder.removeTarget(this.mOutputManager.getSurface(3));
        setRepeatingRequestToMainSession(this.mPreviewRequestBuilder);
        this.mMultiFrameBufferManager.clearBuffer();
        this.mMultiFrameBufferManager.startBuffering();
        ResultChecker metaChecker = new ResultChecker();
        try {
            Builder builder = this.mCamera.createCaptureRequest(5);
            addTargetToBuilder(builder, this.mOutputManager.getSurface(3));
            this.mBuilderSet.applyCommonSettings(builder);
            this.mBuilderSet.applySettingForCpp(builder, true);
            for (int applyEV : captureCnt) {
                this.mBuilderSet.applyEV(builder, applyEV, true);
                doCameraSessionCapture(builder, null, null);
                metaChecker.addCheckCondition(doCameraSessionCapture(builder, this.mStatManager, this.mPreviewHandler));
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        this.mMultiFrameBufferManager.gatherImage(captureCnt.length, metaChecker, 1000);
        return this.mMultiFrameBufferManager.getImageItems(captureCnt.length);
    }

    public int[] evalueHDRChecker(Image input) {
        if (input == null) {
            return null;
        }
        return this.mSolutionManager.configureSolutions(input);
    }

    public void takePicture(ShutterCallbackForward shutterCallback, CaptureListener listener) throws ApiFailureException {
        if (this.mCamera == null || this.mMainSession == null) {
            CamLog.m5e(CameraConstants.TAG, "camera is not ready");
        } else if (this.mSnapShotManager.isAvailableShot(this.mShotSolutionPicked) && this.mSnapShotManager.isAvailableTimeToShot()) {
            this.mSnapShotManager.setCallback(shutterCallback, listener);
            this.mSnapShotManager.setLastShotTime(System.currentTimeMillis());
            if (this.mBuilderSet.getShootMode() == 2 || !((this.mShotSolutionPicked != null && this.mShotSolutionPicked.isEnabledSolution(2)) || isFlashRequired() || this.mActionStateMachine.isAction(3))) {
                takePicture();
                return;
            }
            this.mSnapShotManager.setState(2);
            this.mActionStateMachine.doActionForFlash(this.mMainSession.isReprocessable());
        } else {
            this.mSnapShotManager.sendDropCallback(this.mCameraErrorCallback, this.mShotSolutionPicked);
        }
    }

    public void takePicture() throws ApiFailureException {
        if (this.mMainSession != null) {
            CamLog.m3d(CameraConstants.TAG, "takePicture");
            try {
                if (!applySolution()) {
                    if (ParamConstants.VALUE_BINNING_MODE.equals(this.mParametersDevice.get(ParamConstants.KEY_BINNING_PARAM)) && ParamConstants.VALUE_BINNING_SWPIXEL.equals(this.mParametersDevice.get(ParamConstants.KEY_APP_BINNING_TYPE)) && this.mBuilderSet.getShootMode() == 2) {
                        CamLog.m7i(CameraConstants.TAG, "applyBinningBurst");
                        this.mSnapShotManager.setState(4);
                        this.mCaptureHandler.removeCallbacks(this.mDoBinningBurst);
                        this.mCaptureHandler.post(this.mDoBinningBurst);
                        return;
                    }
                    this.mSnapShotManager.setState(1);
                    this.mSnapShotManager.addHiddenExif(null);
                    if (this.mMainSession.isReprocessable()) {
                        doReprocessCapture();
                    } else {
                        doJpegCapture(false);
                    }
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
                throw new ApiFailureException("Error in takePicture", e);
            }
        }
    }

    public int doAutoFocus(boolean isForceFlash) {
        CamLog.m3d(CameraConstants.TAG, "[CaptureRequest] doAutoFocus");
        if (this.mBuilderSet == null || this.mMainSession == null) {
            return 0;
        }
        try {
            Builder builder = this.mCamera.createCaptureRequest(5);
            addTargetToBuilder(builder, this.mOutputManager.getSurface(0));
            if (this.mIsRecording) {
                addTargetToBuilder(builder, this.mOutputManager.getSurface(1));
            }
            this.mBuilderSet.applyCommonSettings(builder);
            if (isAFSupported()) {
                this.mBuilderSet.applySettingsForAutoFocus(builder);
                if (isForceFlash) {
                    this.mBuilderSet.applyFlashForcedOn(builder, true);
                    this.mBuilderSet.applyFlashForcedOn(this.mPreviewRequestBuilder, true);
                    setRepeatingRequestToMainSession(this.mPreviewRequestBuilder);
                } else {
                    this.mBuilderSet.applyFlash(builder);
                }
            } else {
                this.mBuilderSet.applyFlash(builder);
            }
            return doCameraSessionCapture(builder, this.mStatManager, this.mPreviewHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int doCancelAutoFocus() {
        CamLog.m3d(CameraConstants.TAG, "[CaptureRequest] doCancelAutoFocus");
        if (this.mMainSession == null || this.mBuilderSet == null || this.mMultiFrameBufferManager == null) {
            return 0;
        }
        if (!this.mBuilderSet.isRecordMode()) {
            this.mBuilderSet.applySettingsForUnlockExposure(this.mPreviewRequestBuilder);
            this.mBuilderSet.applyFlashForcedOn(this.mPreviewRequestBuilder, false);
            setRepeatingRequestToMainSession(this.mPreviewRequestBuilder);
        }
        try {
            Builder builder = this.mCamera.createCaptureRequest(5);
            addTargetToBuilder(builder, this.mOutputManager.getSurface(0));
            if (this.mIsRecording) {
                addTargetToBuilder(builder, this.mOutputManager.getSurface(1));
            }
            this.mBuilderSet.applyCommonSettings(builder);
            this.mBuilderSet.applySettingsForUnlockFocus(builder);
            this.mBuilderSet.applyFlashForcedOn(builder, false);
            return doCameraSessionCapture(builder, this.mStatManager, this.mPreviewHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int preCapture() {
        CamLog.m3d(CameraConstants.TAG, "[CaptureRequest] preCapture");
        if (this.mBuilderSet == null || this.mMainSession == null) {
            return 0;
        }
        if (((Integer) this.mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AE_MODE)).intValue() != 0) {
            this.mBuilderSet.applyFlashForcedOn(this.mPreviewRequestBuilder, true);
            setRepeatingRequestToMainSession(this.mPreviewRequestBuilder);
        }
        try {
            Builder builder = this.mCamera.createCaptureRequest(5);
            addTargetToBuilder(builder, this.mOutputManager.getSurface(0));
            this.mBuilderSet.applyCommonSettings(builder);
            this.mBuilderSet.applySettingsForPrecapture(builder);
            if (isAFSupported()) {
                this.mBuilderSet.applySettingsForAutoFocus(builder);
            }
            this.mBuilderSet.applyFlashForcedOn(builder, true);
            return doCameraSessionCapture(builder, this.mStatManager, this.mPreviewHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int lockExposure(boolean isForFlash) {
        CamLog.m3d(CameraConstants.TAG, "[CaptureRequest] lockExposure");
        if (this.mMainSession == null || this.mBuilderSet == null || this.mMultiFrameBufferManager == null) {
            return 0;
        }
        this.mBuilderSet.applySettingsForLockExposure(this.mPreviewRequestBuilder);
        setRepeatingRequestToMainSession(this.mPreviewRequestBuilder);
        return 0;
    }

    public void captureYuvFlash() {
        int gatherCnt = 1;
        CamLog.m3d(CameraConstants.TAG, "[CaptureRequest] captureYuvFlash");
        if (this.mMainSession != null && this.mBuilderSet != null && this.mMultiFrameBufferManager != null) {
            this.mMultiFrameBufferManager.clearBuffer();
            this.mMultiFrameBufferManager.startBuffering();
            ResultChecker metaChecker = new ResultChecker();
            try {
                Builder builder = this.mCamera.createCaptureRequest(2);
                addTargetToBuilder(builder, this.mOutputManager.getSurface(0));
                addTargetToBuilder(builder, this.mOutputManager.getSurface(3));
                this.mBuilderSet.applyCommonSettings(builder);
                this.mBuilderSet.applySettingsForLockExposure(builder);
                this.mBuilderSet.applyFlashForcedOn(builder, true);
                metaChecker.addCheckCondition(CaptureResult.FLASH_STATE, Integer.valueOf(3), true);
                doCameraSessionCapture(builder, this.mStatManager, this.mPreviewHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            if (this.mShotSolutionPicked != null && this.mShotSolutionPicked.isEnabledSolution(2)) {
                gatherCnt = this.mSolutionManager.getSolutionFrameCount(2);
            }
            this.mMultiFrameBufferManager.gatherImage(gatherCnt, metaChecker, CameraConstants.TOAST_LENGTH_SHORT);
        }
    }

    public void onFaceDetionOnOff(final boolean on) {
        this.mCameraOpsHandler.post(new Runnable() {
            public void run() {
                CameraOps.this.setFaceDetection(on);
            }
        });
    }

    public boolean isAFSupported() {
        return Camera2Util.isAFSupported(this.mCameraCharacteristics);
    }

    public boolean isFlashRequired() {
        if (this.mParametersDevice == null || this.mBuilderSet == null) {
            return false;
        }
        String flashMode = this.mParametersDevice.getFlashMode();
        if ("on".equals(flashMode)) {
            return true;
        }
        if (!"auto".equals(flashMode) || !this.mStatManager.isLowLightDetected()) {
            return false;
        }
        if (this.mBuilderSet.getShootMode() == 16) {
            return this.mBuilderSet.isAutoInManualMode(this.mParametersDevice);
        }
        return true;
    }

    public boolean isLowLightDetected() {
        return this.mStatManager.isLowLightDetected();
    }

    public boolean isLowFps() {
        return this.mStatManager.isLowFps();
    }

    public void setImageCallbackListener(int imageType, CameraImageCallback imageCb) {
        if (imageType == 1) {
            if (this.mOutputManager.isValid(3)) {
                if (imageCb != null) {
                    this.mMultiFrameBufferManager.stopBuffering();
                    this.mMultiFrameBufferManager.clearBuffer();
                }
                this.mImageCallbackManager.setFullFrameImageCallback(imageCb, this.mFullFrameImageHandler, false);
            } else {
                CamLog.m5e(CameraConstants.TAG, "There is no FULL FRAME Surface in Output surfaces");
                return;
            }
        } else if (imageType != 0) {
            return;
        } else {
            if (this.mOutputManager.isValid(4)) {
                this.mImageCallbackManager.setPreviewImageCallback(imageCb, this.mPreviewImageHandler, false);
            } else {
                CamLog.m5e(CameraConstants.TAG, "There is no PREVIEW_CALLBACK Surface in Output surfaces");
                return;
            }
        }
        updateTargetList(true);
    }

    public void setImageCaptureCallback(final CameraImageMetaCallback captureCb) {
        this.mPreviewHandler.post(new Runnable() {
            public void run() {
                CameraOps.this.mCaptureMetaCallback = captureCb;
            }
        });
    }

    private void setupPreviewSurface() {
        if (this.mPreviewSurfaceView != null) {
            this.mOutputManager.setSurface(0, this.mPreviewSurfaceView.getSurface());
        } else if (this.mPreviewSurfaceTexture != null) {
            this.mOutputManager.setSurface(0, new Surface(this.mPreviewSurfaceTexture));
        } else {
            CamLog.m5e(CameraConstants.TAG, "output display is not set; just return");
        }
    }

    public void setCameraOpsModuleBridge(CameraOpsModuleBridge bridge) {
        CamLog.m3d(CameraConstants.TAG, "setCameraOpsCallbacks, callbacks = " + bridge);
        this.mGet = bridge;
    }

    public void updateRequestParameter(long exposureTime, int sensorSensitivity) {
        if (this.mBuilderSet == null) {
            CamLog.m5e(CameraConstants.TAG, "updateRequestParameter : mBuilderSet is null. return.");
            return;
        }
        this.mBuilderSet.applyPanoramaCapture(this.mPreviewRequestBuilder, exposureTime, sensorSensitivity);
        setRepeatingRequestToMainSession(this.mPreviewRequestBuilder);
    }

    public void setLuxIndexMetadata(LGCameraMetaDataCallback cb) {
        if (this.mStatManager != null) {
            this.mStatManager.setLuxIndexMetadata(cb);
        }
    }

    private boolean isReprocessSupported() {
        if (Camera2Util.isReprocessSupported(this.mCameraCharacteristics) && ConfigurationUtil.sSHOT_SOLUTION_SUPPORTED == 1) {
            return true;
        }
        return false;
    }

    public void stopBurstShot() {
        CamLog.m7i(CameraConstants.TAG, "stopBurstShot");
        this.mCaptureHandler.removeCallbacks(this.mDoBinningBurst);
        this.mSnapShotManager.stopBurstState();
    }

    public void onOutFocusImage(OutfocusCaptureResult outfocusResult) {
        if (outfocusResult == null || !outfocusResult.hasVaildImage()) {
            this.mSnapShotManager.sendDropCallback(this.mCameraErrorCallback, null);
        } else {
            outfocusResult.printDebugLog();
            if (this.mBuilderSet != null) {
                outfocusResult.setSensorActiveSize(this.mBuilderSet.getActiveArraySize());
            }
            this.mSnapShotManager.sendOutFocusCapturCallback(outfocusResult);
            this.mSnapShotManager.sendPictureCallback(outfocusResult.getErrorType() == 0 ? outfocusResult.getBluredImage() : outfocusResult.getOriginImage());
        }
        this.mSnapShotManager.doneShot(false);
    }
}
