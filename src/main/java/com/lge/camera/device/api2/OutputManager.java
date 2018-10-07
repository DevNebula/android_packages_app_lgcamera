package com.lge.camera.device.api2;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.params.OutputConfiguration;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OutputManager {
    private static final int CONCURRENT_FULL_FRAME_STREAM_DEFAULT = 12;
    private static final int CONCURRENT_FULL_FRAME_STREAM_MAX = 15;
    private static final int CONCURRENT_FULL_FRAME_STREAM_MIN = 2;
    private static final int CONCURRENT_JPEG_DEFAULT = 2;
    private static final int CONCURRENT_JPEG_MAX = 4;
    private static final int CONCURRENT_JPEG_MIN = 2;
    private static final int CONCURRENT_PREVIEW_MAX = 12;
    private static final int CONCURRENT_PREVIEW_MIN = 2;
    private static final int IMAGE_READER_FULL_FRAME = 1;
    private static final int IMAGE_READER_JPEG = 0;
    private static final int IMAGE_READER_MAX = 4;
    private static final int IMAGE_READER_PREVIEW_CALLBACK = 2;
    private static final int IMAGE_READER_RAW = 3;
    private static final int OUTPUTS_DEFAULT_RECORD = 7;
    private static final int OUTPUTS_DEFAULT_SHOT = 29;
    private static final int OUTPUTS_NO_SET = 0;
    public static final int SESSION_RECORD = 1;
    public static final int SESSION_SHOT = 0;
    public static final int SURFACE_FULL_FRAME = 3;
    public static final int SURFACE_JPEG = 2;
    public static final int SURFACE_MAX = 6;
    public static final int SURFACE_PREVIEW = 0;
    public static final int SURFACE_PREVIEW_CALLBACK = 4;
    public static final int SURFACE_RAW = 5;
    public static final int SURFACE_RECORD = 1;
    private static HashMap<String, Integer> sOutputsMap = null;
    private ImageReader[] mAllImageReader = new ImageReader[4];
    private ImageReader[] mAllImageReaderBackup = new ImageReader[4];
    private Surface[] mAllSurface = new Surface[6];
    private CameraCharacteristics mCameraCharacteristics;
    private ICameraOps mCameraOps = null;
    private Handler mCaptureHandler;
    List<OutputConfiguration> mCurrOutputConfigs;
    private List<Surface> mCurrOutputSurfaces;
    private int mFullFrameBufferSize = 12;
    private Handler mFullFrameImageHandler;
    private Size mFullFrameSize;
    private boolean mIsReprocess = false;
    private int mOutputType = 0;
    private int mPreviewCallbackBufferSize = 2;
    private Handler mPreviewImageHandler;
    private OutputConfiguration mPreviewOutputConfig;

    public OutputManager(ICameraOps cameraOps, CameraCharacteristics cameraCharacteristics, Handler previewImageHandler, Handler fullFrameImageHandler, Handler captureHandler) {
        if (sOutputsMap == null) {
            sOutputsMap = new HashMap();
            sOutputsMap.put(ParamConstants.OUTPUTS_PREVIEW_JPEG_PCALLBACK_STR, Integer.valueOf(21));
            sOutputsMap.put(ParamConstants.OUTPUTS_PREVIEW_JPEG_RAW_STR, Integer.valueOf(37));
            sOutputsMap.put(ParamConstants.OUTPUTS_PREVIEW_FULLFRAME_PCALLBACK_STR, Integer.valueOf(25));
            sOutputsMap.put(ParamConstants.OUTPUTS_PREVIEW_FULLFRAME_STR, Integer.valueOf(9));
            sOutputsMap.put(ParamConstants.OUTPUTS_PREVIEW_JPEG_STR, Integer.valueOf(5));
            sOutputsMap.put(ParamConstants.OUTPUTS_PREVIEW_RECORD_STR, Integer.valueOf(3));
            sOutputsMap.put(ParamConstants.OUTPUTS_PREVIEW_STR, Integer.valueOf(1));
            sOutputsMap.put(ParamConstants.OUTPUTS_PREVIEW_RECORD_FULLFRAME_STR, Integer.valueOf(11));
        }
        this.mCameraOps = cameraOps;
        this.mCameraCharacteristics = cameraCharacteristics;
        this.mPreviewImageHandler = previewImageHandler;
        this.mFullFrameImageHandler = fullFrameImageHandler;
        this.mCaptureHandler = captureHandler;
        this.mCurrOutputSurfaces = new ArrayList();
    }

    public List<Surface> getCurrOutputSurfaces() {
        return this.mCurrOutputSurfaces;
    }

    public List<OutputConfiguration> getCurrOutputConfigs() {
        return this.mCurrOutputConfigs;
    }

    public void setSurface(int surfaceType, Surface surface) {
        this.mAllSurface[surfaceType] = surface;
    }

    public Surface getSurface(int surfaceType) {
        return this.mAllSurface[surfaceType];
    }

    public boolean configureOutputSurfaces(int sessionType, Parameters2 parametersDevice, boolean isReprocessSupported) {
        if (this.mCurrOutputSurfaces == null) {
            CamLog.m3d(CameraConstants.TAG, "mCurrOutputSurfaces null");
            return false;
        } else if (this.mCurrOutputSurfaces.size() > 0) {
            CamLog.m5e(CameraConstants.TAG, "mCurrOutputSurfaces already configured, need to stoppreview to be reset " + this.mCurrOutputSurfaces.size());
            return false;
        } else {
            setOutputsType(sessionType, parametersDevice, isReprocessSupported);
            setConcurrentBufferSize(parametersDevice);
            List<Surface> outputSurfaces = new ArrayList();
            List<OutputConfiguration> outputConfigs = new ArrayList();
            if (isAddableToOutputs(0) && this.mAllSurface[0] == null && setPreviewOutputConfig(parametersDevice)) {
                outputConfigs.add(this.mPreviewOutputConfig);
            }
            for (int surfaceType = 0; surfaceType < this.mAllSurface.length; surfaceType++) {
                if (isAddableToOutputs(surfaceType)) {
                    CamLog.m3d(CameraConstants.TAG, "setOutputSurfaceType " + surfaceType);
                    if (isSurfaceOfImageReader(surfaceType)) {
                        setSurfaceOfImageReader(surfaceType, parametersDevice);
                    }
                    if (this.mAllSurface[surfaceType] != null) {
                        outputSurfaces.add(this.mAllSurface[surfaceType]);
                        outputConfigs.add(new OutputConfiguration(this.mAllSurface[surfaceType]));
                    }
                }
            }
            this.mCurrOutputSurfaces = outputSurfaces;
            this.mCurrOutputConfigs = outputConfigs;
            return true;
        }
    }

    public boolean finalizeOutputConfigurations() {
        if (this.mPreviewOutputConfig == null || this.mAllSurface[0] == null) {
            return false;
        }
        this.mPreviewOutputConfig.addSurface(this.mAllSurface[0]);
        this.mCurrOutputSurfaces.add(this.mAllSurface[0]);
        return true;
    }

    private boolean setPreviewOutputConfig(Parameters2 parametersDevice) {
        Size previewSizeParam = parametersDevice.getPreviewSize();
        if (previewSizeParam == null) {
            CamLog.m5e(CameraConstants.TAG, " Error no Preview Surface Infomation");
            return false;
        }
        String surfaceTypeValue = parametersDevice.get(ParamConstants.KEY_PREVIEW_SURFACE_TYPE);
        if (surfaceTypeValue == null) {
            CamLog.m5e(CameraConstants.TAG, "KEY_PREVIEW_SURFACE_TYPE null");
            return false;
        }
        if (Integer.valueOf(surfaceTypeValue).intValue() == 1) {
            this.mPreviewOutputConfig = new OutputConfiguration(previewSizeParam, SurfaceHolder.class);
        } else {
            this.mPreviewOutputConfig = new OutputConfiguration(previewSizeParam, SurfaceTexture.class);
        }
        return true;
    }

    private void setOutputsType(int sessionType, Parameters2 parametersDevice, boolean isReprocessSupported) {
        String outputType = parametersDevice.get(ParamConstants.KEY_APP_OUTPUTS_TYPE);
        String zsl = parametersDevice.get(ParamConstants.KEY_ZSL);
        boolean isReprocess = false;
        Integer outputInteger = (Integer) sOutputsMap.get(outputType);
        if (sessionType == 0) {
            int snapShotType = 29;
            if (outputInteger == null) {
                isReprocess = true;
                if (!(isReprocessSupported || 1 == null) || "off".equals(zsl)) {
                    snapShotType = ((Integer) sOutputsMap.get(ParamConstants.OUTPUTS_PREVIEW_JPEG_PCALLBACK_STR)).intValue();
                    isReprocess = false;
                }
            } else {
                snapShotType = outputInteger.intValue();
                isReprocess = false;
            }
            this.mOutputType = snapShotType;
        } else {
            int recordType = 7;
            if (!(outputInteger == null || (outputInteger.intValue() & 2) == 0)) {
                recordType = outputInteger.intValue();
                CamLog.m7i(CameraConstants.TAG, "Vaild record output type");
            }
            if (!isSupportLiveSnapShot(parametersDevice)) {
                recordType = ((Integer) sOutputsMap.get(ParamConstants.OUTPUTS_PREVIEW_RECORD_STR)).intValue();
            }
            this.mOutputType = recordType;
        }
        this.mIsReprocess = isReprocess;
        CamLog.m3d(CameraConstants.TAG, "setOutputSurfaceType outputTypeStr = " + outputType + ", mOutputType = " + this.mOutputType + ", mIsReprocess = " + this.mIsReprocess);
    }

    private boolean isSupportLiveSnapShot(Parameters2 parametersDevice) {
        if (parametersDevice == null || "on".equals(parametersDevice.get("hdr10")) || parametersDevice.getLiveSanpShotSize() == null) {
            return false;
        }
        return true;
    }

    private void setConcurrentBufferSize(Parameters2 parametersDevice) {
        int tempSize;
        this.mFullFrameBufferSize = 12;
        String fullFrameSize = parametersDevice.get(ParamConstants.KEY_APP_FULL_FRAME_BUF_SIZE);
        if (fullFrameSize != null) {
            tempSize = Integer.parseInt(fullFrameSize);
            if (tempSize < 2 || tempSize > 15) {
                CamLog.m5e(CameraConstants.TAG, "Requested Full frame buffer size is too big or small " + tempSize);
            } else {
                this.mFullFrameBufferSize = tempSize;
            }
        }
        this.mPreviewCallbackBufferSize = 2;
        String previewSize = parametersDevice.get(ParamConstants.KEY_APP_PREVIEW_CB_BUF_SIZE);
        if (previewSize != null) {
            tempSize = Integer.parseInt(previewSize);
            if (tempSize < 2 || tempSize > 12) {
                CamLog.m5e(CameraConstants.TAG, "Requested preview callback buffer size is too big or small " + tempSize);
            } else {
                this.mPreviewCallbackBufferSize = tempSize;
            }
        }
        CamLog.m3d(CameraConstants.TAG, "fullFrameSize " + this.mFullFrameBufferSize + ", previewSize " + this.mPreviewCallbackBufferSize);
    }

    private void setJpegReaderSurface(Parameters2 parametersDevice) {
        CamLog.m3d(CameraConstants.TAG, "setJpegReaderSurface " + isAddableToOutputs(1));
        Size pictureSize = isAddableToOutputs(1) ? parametersDevice.getLiveSanpShotSize() : parametersDevice.getPictureSize();
        if (pictureSize != null) {
            if (this.mAllImageReader[0] != null) {
                this.mAllImageReader[0].close();
            }
            this.mAllImageReader[0] = ImageReader.newInstance(pictureSize.getWidth(), pictureSize.getHeight(), 256, 2);
            this.mAllImageReader[0].setOnImageAvailableListener(this.mCameraOps.getOnJpegImageAvailableListener(), this.mCaptureHandler);
            this.mAllSurface[2] = this.mAllImageReader[0].getSurface();
        }
    }

    private void setRawReaderSurface() {
        CamLog.m3d(CameraConstants.TAG, "setRawReaderSurface ");
        if (this.mAllImageReader[3] != null) {
            this.mAllImageReader[3].close();
        }
        Size rawSize = Camera2Util.getRawSize(this.mCameraCharacteristics);
        this.mAllImageReader[3] = ImageReader.newInstance(rawSize.getWidth(), rawSize.getHeight(), 32, 2);
        this.mAllImageReader[3].setOnImageAvailableListener(this.mCameraOps.getOnRawImageAvailableListener(), this.mCaptureHandler);
        this.mAllSurface[5] = this.mAllImageReader[3].getSurface();
    }

    private void setFullFrameReaderSurface(Parameters2 parametersDevice) {
        CamLog.m3d(CameraConstants.TAG, "setFullFrameReaderSurface");
        Size pictureSizeParam = parametersDevice.getPictureSize();
        if (pictureSizeParam != null) {
            if (isReprocess()) {
                this.mFullFrameSize = Camera2Util.getInputSize(this.mCameraCharacteristics, pictureSizeParam);
            } else {
                this.mFullFrameSize = pictureSizeParam;
            }
            int width = this.mFullFrameSize.getWidth();
            int height = this.mFullFrameSize.getHeight();
            if (this.mAllImageReader[1] != null) {
                this.mAllImageReader[1].close();
            }
            CamLog.m3d(CameraConstants.TAG, "frame buffer size : " + width + "x" + height);
            this.mAllImageReader[1] = ImageReader.newInstance(width, height, 35, this.mFullFrameBufferSize);
            this.mAllImageReader[1].setOnImageAvailableListener(this.mCameraOps.getOnFullFrameImageAvailableListener(), this.mFullFrameImageHandler);
            this.mAllSurface[3] = this.mAllImageReader[1].getSurface();
        }
    }

    public Size getFullFrameSize() {
        return this.mFullFrameSize;
    }

    private void setPreviewCallbackReaderSurface(Parameters2 parametersDevice) {
        CamLog.m3d(CameraConstants.TAG, "setPreviewCallbackReaderSurface");
        Size previewSizeParam = parametersDevice.getPreviewSize();
        Size picSizeParam = parametersDevice.getPictureSize();
        if (previewSizeParam != null) {
            if (this.mAllImageReader[2] != null) {
                this.mAllImageReader[2].close();
            }
            CamLog.m3d(CameraConstants.TAG, "preview callback size : " + previewSizeParam.getWidth() + "x" + previewSizeParam.getHeight());
            this.mAllImageReader[2] = ImageReader.newInstance(previewSizeParam.getWidth(), previewSizeParam.getHeight(), 35, this.mPreviewCallbackBufferSize);
            this.mAllImageReader[2].setOnImageAvailableListener(this.mCameraOps.getOnPreviewImageAvailableListener(), this.mPreviewImageHandler);
            this.mAllSurface[4] = this.mAllImageReader[2].getSurface();
        }
    }

    private boolean isSurfaceOfImageReader(int surfaceType) {
        return surfaceType == 2 || surfaceType == 3 || surfaceType == 4 || surfaceType == 5;
    }

    private void setSurfaceOfImageReader(int surfaceType, Parameters2 parametersDevice) {
        if (surfaceType == 2) {
            setJpegReaderSurface(parametersDevice);
        } else if (surfaceType == 3) {
            setFullFrameReaderSurface(parametersDevice);
        } else if (surfaceType == 4) {
            setPreviewCallbackReaderSurface(parametersDevice);
        } else if (surfaceType == 5) {
            setRawReaderSurface();
        }
    }

    private boolean isAddableToOutputs(int surfaceType) {
        return (this.mOutputType & (1 << surfaceType)) == (1 << surfaceType);
    }

    public boolean isReprocess() {
        return this.mIsReprocess;
    }

    public boolean isValid(int surfaceType) {
        return this.mAllSurface[surfaceType] != null;
    }

    public boolean addTargetToBuilder(Builder builder, Surface surface) {
        if (this.mCurrOutputSurfaces == null || this.mCurrOutputSurfaces.size() == 0) {
            return false;
        }
        for (Surface s : this.mCurrOutputSurfaces) {
            if (surface.equals(s)) {
                builder.addTarget(s);
                return true;
            }
        }
        CamLog.m7i(CameraConstants.TAG, "passing invalid surface");
        return false;
    }

    public void closeImageStream() {
        int i;
        for (i = 0; i < this.mAllSurface.length; i++) {
            if (this.mAllSurface[i] != null) {
                this.mAllSurface[i] = null;
            }
        }
        for (i = 0; i < this.mAllImageReader.length; i++) {
            if (this.mAllImageReader[i] != null) {
                this.mAllImageReader[i].close();
                this.mAllImageReader[i] = null;
            }
        }
    }

    public void backupImageStream() {
        closeBackupImageStream();
        int i = 0;
        while (i < this.mAllImageReader.length && i < this.mAllImageReaderBackup.length) {
            this.mAllImageReaderBackup[i] = this.mAllImageReader[i];
            this.mAllImageReader[i] = null;
            i++;
        }
    }

    public void closeBackupImageStream() {
        for (int i = 0; i < this.mAllImageReaderBackup.length; i++) {
            if (this.mAllImageReaderBackup[i] != null) {
                this.mAllImageReaderBackup[i].close();
                this.mAllImageReaderBackup[i] = null;
                CamLog.m7i(CameraConstants.TAG, " clear image reader " + i);
            }
        }
    }

    public void clear() {
        if (this.mCurrOutputSurfaces != null) {
            this.mCurrOutputSurfaces.clear();
        }
        this.mOutputType = 0;
    }

    public boolean isWaitingPreviewSurface() {
        return this.mPreviewOutputConfig != null && this.mPreviewOutputConfig.getSurface() == null;
    }
}
