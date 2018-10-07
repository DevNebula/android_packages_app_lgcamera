package com.lge.panorama;

import android.graphics.Bitmap;
import android.support.p000v4.view.ViewCompat;
import android.util.Size;
import java.nio.ByteBuffer;

public class Panorama {
    public static final long CAMERA_FRAME_DELAY = 0;
    public static final int CAPTURE_DIRECTION_BOTTOM_TO_TOP = 3;
    public static final int CAPTURE_DIRECTION_LEFT_TO_RIGHT = 1;
    public static final int CAPTURE_DIRECTION_RIGHT_TO_LEFT = 2;
    public static final int CAPTURE_DIRECTION_TOP_TO_BOTTOM = 4;
    public static final int CAPTURE_DIRECTION_UNKNOWN = 0;
    public static final float COARSE_ALIGN_DOWNSCALE_FACTOR = 16.0f;
    public static final float COARSE_ALIGN_MIN_DOWNSCALE_MP = 10.0f;
    public static final int DEF_ROTATION_ANGLE = 360;
    public static final int EDGE_MODE_DISABLE = 0;
    public static final int EDGE_SHARPEN_MEDIUM = 2;
    public static final int EDGE_SHARPEN_STRONG = 3;
    public static final int EDGE_SHARPEN_WEAK = 1;
    public static final int LENS_CORR_CYLINDRICAL = 1;
    public static final int LENS_CORR_CYLINDRICAL_AND_UNDIST = 2;
    public static final int LENS_CORR_DIABLE = 0;
    public static final float MAX_PANO_ANGLE = 360.0f;
    public static final int MAX_ROTATION_ANGLE = 360;
    public static final float MIN_PANO_ANGLE = 270.0f;
    public static final int NOT_STARTED = 3;
    public static final int NR_BILATERAL_FILTER = 3;
    public static final int NR_MEDIAN_3X3 = 1;
    public static final int NR_MEDIAN_5X5 = 2;
    public static final int NR_MEDIAN_DISABLE = 0;
    public static final int PANORAMA_360_DISABLE = 0;
    public static final int PANORAMA_360_MAKING = 1;
    public static final String PANO_LIB_AMRV7 = "ARMV7";
    public static final String PANO_LIB_AMRV8 = "ARMV8";
    public static final int REACHED_LOWER_BOUND = 0;
    public static final int REACHED_UPPER_BOUND = 1;
    public static final int SENSOR_OBSERVATION_WINDOW_TIME = 2;
    public static final float SENSOR_SAMPLE_RATE = 90.0f;
    public static final int STATUS_ERROR_ABORT_PANORAMA = 440;
    public static final int STATUS_ERROR_ALIGNMENT_FAILURE = 402;
    public static final int STATUS_ERROR_MOVEMENT_DETECTION_INCOMPLETE = 405;
    public static final int STATUS_ERROR_OUT_OF_MEMORY = 401;
    public static final int STATUS_ERROR_SAVING_FAILURE = 430;
    public static final int STATUS_ERROR_TOO_FAR_DOWN = 421;
    public static final int STATUS_ERROR_TOO_FAR_LEFT = 422;
    public static final int STATUS_ERROR_TOO_FAR_RIGHT = 423;
    public static final int STATUS_ERROR_TOO_FAR_UP = 420;
    public static final int STATUS_ERROR_TOO_FAST = 450;
    public static final int STATUS_ERROR_UNKNOWN = 500;
    public static final int STATUS_ERROR_WRONG_DIRECTION_DOWN = 411;
    public static final int STATUS_ERROR_WRONG_DIRECTION_LEFT = 412;
    public static final int STATUS_ERROR_WRONG_DIRECTION_RIGHT = 413;
    public static final int STATUS_ERROR_WRONG_DIRECTION_UP = 410;
    public static final int STATUS_MOVEMENT_DETECTION_COMPLETE_BOTTOM_TO_TOP = 12;
    public static final int STATUS_MOVEMENT_DETECTION_COMPLETE_LEFT_TO_RIGHT = 10;
    public static final int STATUS_MOVEMENT_DETECTION_COMPLETE_RIGHT_TO_LEFT = 11;
    public static final int STATUS_MOVEMENT_DETECTION_COMPLETE_TOP_TO_BOTTOM = 13;
    public static final int STATUS_MOVEMENT_DETECTION_STARTED = 3;
    public static final int STATUS_MOVEMENT_DETECTION_STOPPED = 4;
    public static final int STATUS_OK = 0;
    public static final int STATUS_SAVED_OUTPUT_FILE = 2;
    public static final int STATUS_STARTED_PANORAMA = 5;
    public static final int STATUS_STOP_PANORAMA_360 = 6;
    public static final int STATUS_UPDATED_PANORAMA_FRAME = 202;
    public static final int STATUS_UPDATED_PREVIEW_FRAME = 203;
    public static final int STATUS_WARNING_ALIGNMENT_FAILURE = 140;
    public static final int STATUS_WARNING_TOO_FAR_DOWN = 121;
    public static final int STATUS_WARNING_TOO_FAR_LEFT = 122;
    public static final int STATUS_WARNING_TOO_FAR_RIGHT = 123;
    public static final int STATUS_WARNING_TOO_FAR_UP = 120;
    public static final int STATUS_WARNING_TOO_FAST_DOWN = 131;
    public static final int STATUS_WARNING_TOO_FAST_LEFT = 132;
    public static final int STATUS_WARNING_TOO_FAST_RIGHT = 133;
    public static final int STATUS_WARNING_TOO_FAST_UP = 130;
    public static final int STATUS_WARNING_WRONG_DIRECTION_DOWN = 111;
    public static final int STATUS_WARNING_WRONG_DIRECTION_LEFT = 112;
    public static final int STATUS_WARNING_WRONG_DIRECTION_RIGHT = 113;
    public static final int STATUS_WARNING_WRONG_DIRECTION_UP = 110;
    public static final int STATUS_WHOLE_AREA_COMPLETE = 1;
    public static final int THUMB_JPEG_Q_FACTOR = 80;
    public static final int THUMB_MAX_WIDTH = 512;
    public static final float TOO_FAST_PROPORTION = 0.023f;
    public static final float TOO_FAST_PROPORTION_WIDE = 0.015333333f;
    public static final int TRACKING_MODE_IMAGE_AND_INERTIAL_SENSOR = 1;
    public static final int TRACKING_MODE_IMAGE_ONLY = 0;
    public static final boolean USE_DEFAULT_STRIP_WARPING = true;
    public static final int WITHIN_BOUNDS = 2;
    public SignatureCallbackInJava mCallbackJava = null;
    private long mHandle = 0;
    private boolean mIsBusy = false;
    private PanoramaListener mListener = null;
    public XmpAvailableListener mXmpListener = null;
    public long mXmpListenerObj = 0;

    public interface XmpAvailableListener {
        void onXmpAvailable(int i, int i2, int i3, int i4, int i5, int i6, float f, float f2);
    }

    public interface PanoramaListener {
        void onNotifyStatus(int i);
    }

    public interface SignatureCallbackInJava {
        Bitmap setSignatureParam(int i, int i2, int i3);
    }

    public static class XmpInfo {
        public int mCropHeight;
        public int mCropLeft;
        public int mCropTop;
        public int mCropWidth;
        public float mHoriFov;
        public float mVertFov;
        public int mWholeHeight;
        public int mWholeWidth;
    }

    private native void abort(long j);

    private native void begin(long j, String str, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, String str2, String str3, float f, float f2, float f3, int i12, long j2, int i13, float f4, float f5, int i14, int i15, float f6, float f7, float f8, boolean z, int i16, float[] fArr, float[] fArr2, int i17, int i18, int i19, ByteBuffer[] byteBufferArr, int i20, int i21);

    private native int dequeueBuffer(long j);

    private native boolean feedFrame(long j, ByteBuffer byteBuffer, ByteBuffer byteBuffer2, long j2);

    private native boolean feedFrameSingle(long j, byte[] bArr, long j2);

    private native boolean feedFrameSingleStrided(long j, byte[] bArr, int i, int i2, long j2);

    private native boolean feedFrameStrided(long j, ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, long j2);

    private native boolean feedInertialSensorData(long j, float[] fArr, long j2);

    private native void fillBuffer(long j, Bitmap bitmap, int[] iArr);

    private native double getFeedAvgFps(long j);

    private native float getGainCompensationLastAdjustedLuma(long j);

    private native float getGainCompensationLastAverageLuma(long j);

    private native float getGainCompensationLowerLimit(long j);

    private native int getGainCompensationStatus(long j);

    private native float getGainCompensationTarget(long j);

    private native float getGainCompensationUpperLimit(long j);

    private native long getHandle();

    public static native String getLibArchitecture();

    public static native String getLibVersion();

    private native double getPanoramaAvgFps(long j);

    private native double getPanoramaFps(long j);

    private native double getPreviewFps(long j);

    private native void queueBuffer(long j, int i, long j2);

    private native void releaseHandle(long j);

    private native void setCameraFrameDelay(long j, long j2);

    private native void setCoarseAlignmentDownscaleFactor(long j, float f);

    private native void setCoarseAlignmentMinimalDownscaledMP(long j, float f);

    private native void setEnableCylindricalStripWarping(long j, boolean z);

    private native void setEnableMinPathSeamFinder(long j, boolean z);

    private native void setGainCompensationEnabled(long j, boolean z);

    private native void setGainCompensationLimits(long j, float f, float f2);

    private native void setGainCompensationTarget(long j, float f);

    private native void setJpegQualityFactor(long j, int i);

    private native void setMakePanorama360(long j, boolean z);

    private native void setMinimumAnglePanorama360(long j, float f);

    private native void setSensorObservationWindowTime(long j, int i);

    private native void setSensorSampleRate(long j, float f);

    private native void setSignatureData(long j, int i, int i2, byte[] bArr, int i3);

    private native void setSignatureUse(long j, boolean z);

    private native void setThumbnailJpegQualityFactor(long j, int i);

    private native void setThumbnailMaximumWidth(long j, int i);

    private native void setTooFastProportion(long j, float f);

    private native long setXmpAvailableListener(long j);

    private native void stop(long j);

    private native void stop360(long j);

    static {
        System.loadLibrary("panorama");
        System.loadLibrary("panorama_jni");
    }

    public Panorama(PanoramaListener listener) {
        this.mListener = listener;
    }

    public void init() {
        if (this.mHandle == 0) {
            this.mHandle = getHandle();
        }
    }

    public void release() {
        if (this.mHandle != 0) {
            releaseHandle(this.mHandle);
            this.mHandle = 0;
            this.mListener = null;
            this.mCallbackJava = null;
            this.mXmpListener = null;
        }
        if (this.mXmpListenerObj != 0) {
            this.mXmpListenerObj = 0;
        }
    }

    public void begin(String outputFileName, Size frameSize, int horizontalPanoramaMaxLength, int verticalPanoramaMaxLength, int maximumRotationAngleCoverage, int frameRotation, int bufferToInertialSensorRotation, Size horizontalPanoramaPreviewSize, Size verticalPanoramaPreviewSize, String make, String model, float focalLengthMm, float fNumber, float maxAperture, int iso, long exposureTimeNs, int appExposureMode, float sensorWidthMm, float sensorHeightMm, int sensorActiveArrayWidth, int sensorActiveArrayHeight, float gpsLatitude, float gpsLongitude, float gpsAltitude, boolean enableGps, int lensCorrectionMode, float[] principal_point, float[] undistort_matrix, int noiseReductionMode, int edgeEnhancementMode, int trackMode, ByteBuffer[] bufferTableData, int bufferTableLength, int byteBufferOffset) {
        if (this.mHandle != 0) {
            begin(this.mHandle, outputFileName, frameSize.getWidth(), frameSize.getHeight(), horizontalPanoramaMaxLength, verticalPanoramaMaxLength, maximumRotationAngleCoverage, frameRotation, bufferToInertialSensorRotation, horizontalPanoramaPreviewSize.getWidth(), horizontalPanoramaPreviewSize.getHeight(), verticalPanoramaPreviewSize.getWidth(), verticalPanoramaPreviewSize.getHeight(), make, model, focalLengthMm, fNumber, maxAperture, iso, exposureTimeNs, appExposureMode, sensorWidthMm, sensorHeightMm, sensorActiveArrayWidth, sensorActiveArrayHeight, gpsLatitude, gpsLongitude, gpsAltitude, enableGps, lensCorrectionMode, principal_point, undistort_matrix, noiseReductionMode, edgeEnhancementMode, trackMode, bufferTableData, bufferTableLength, byteBufferOffset);
            this.mIsBusy = true;
        }
    }

    public int getGainCompensationStatus() {
        if (this.mHandle != 0) {
            return getGainCompensationStatus(this.mHandle);
        }
        return 3;
    }

    public void setGainCompensationEnabled(boolean enable) {
        if (this.mHandle != 0) {
            setGainCompensationEnabled(this.mHandle, enable);
        }
    }

    public void setJpegQualityFactor(int factor) {
        if (this.mHandle != 0) {
            setJpegQualityFactor(this.mHandle, factor);
        }
    }

    public void setThumbnailJpegQualityFactor(int factor) {
        if (this.mHandle != 0) {
            setThumbnailJpegQualityFactor(this.mHandle, factor);
        }
    }

    public void setThumbnailMaximumWidth(int width) {
        if (this.mHandle != 0) {
            setThumbnailMaximumWidth(this.mHandle, width);
        }
    }

    public void setTooFastProportion(float proportion) {
        if (this.mHandle != 0) {
            setTooFastProportion(this.mHandle, proportion);
        }
    }

    public void setCoarseAlignmentDownscaleFactor(float proportion) {
        if (this.mHandle != 0) {
            setCoarseAlignmentDownscaleFactor(this.mHandle, proportion);
        }
    }

    public void setCameraFrameDelay(long value) {
        if (this.mHandle != 0) {
            setCameraFrameDelay(this.mHandle, value);
        }
    }

    public void setSensorSampleRate(float rate) {
        if (this.mHandle != 0) {
            setSensorSampleRate(this.mHandle, rate);
        }
    }

    public void setSensorObservationWindowTime(int seconds) {
        if (this.mHandle != 0) {
            setSensorObservationWindowTime(this.mHandle, seconds);
        }
    }

    public void setMakePanorama360(int value) {
        if (this.mHandle == 0) {
            return;
        }
        if (value == 0) {
            setMakePanorama360(this.mHandle, false);
        } else {
            setMakePanorama360(this.mHandle, true);
        }
    }

    public void setMinimumAnglePanorama360(float value) {
        if (this.mHandle != 0) {
            setMinimumAnglePanorama360(this.mHandle, value);
        }
    }

    public void setEnableMinPathSeamFinder360(int value) {
        if (this.mHandle == 0) {
            return;
        }
        if (value == 0) {
            setEnableMinPathSeamFinder(this.mHandle, false);
        } else {
            setEnableMinPathSeamFinder(this.mHandle, true);
        }
    }

    public void setEnableCylindricalStripWarping(boolean enable) {
        if (this.mHandle != 0) {
            setEnableCylindricalStripWarping(this.mHandle, enable);
        }
    }

    public boolean feedFrame(ByteBuffer yFrame, ByteBuffer vuFrame, long timestamp) {
        if (this.mHandle == 0) {
            return false;
        }
        return feedFrame(this.mHandle, yFrame, vuFrame, timestamp);
    }

    public boolean feedFrame(byte[] yuvChannels, long timestamp) {
        if (this.mHandle == 0) {
            return false;
        }
        return feedFrameSingle(this.mHandle, yuvChannels, timestamp);
    }

    public boolean feedFrame(ByteBuffer yFrame, ByteBuffer vuFrame, int stride, long timestamp) {
        if (this.mHandle == 0) {
            return false;
        }
        return feedFrameStrided(this.mHandle, yFrame, vuFrame, stride, timestamp);
    }

    public boolean feedFrame(byte[] yuvChannels, int stride, int scanline, long timestamp) {
        if (this.mHandle == 0) {
            return false;
        }
        return feedFrameSingleStrided(this.mHandle, yuvChannels, stride, scanline, timestamp);
    }

    public boolean feedInertialSensorData(float[] quaternion, long timeStamp) {
        if (this.mHandle == 0) {
            return false;
        }
        return feedInertialSensorData(this.mHandle, quaternion, timeStamp);
    }

    public void stop() {
        if (this.mHandle != 0) {
            stop(this.mHandle);
        }
        this.mIsBusy = false;
    }

    public void stop360() {
        if (this.mHandle != 0) {
            stop360(this.mHandle);
        }
        this.mIsBusy = false;
    }

    public void abort() {
        if (this.mHandle != 0) {
            abort(this.mHandle);
        }
        this.mIsBusy = false;
    }

    public int fillBuffer(Bitmap bitmap, int[] lastPos) {
        if (this.mHandle == 0) {
            return 0;
        }
        fillBuffer(this.mHandle, bitmap, lastPos);
        return 1;
    }

    public double getAverageFps() {
        if (this.mHandle != 0) {
            return getPanoramaAvgFps(this.mHandle);
        }
        return 0.0d;
    }

    public double getPanoramaFps() {
        if (this.mHandle != 0) {
            return getPanoramaFps(this.mHandle);
        }
        return 0.0d;
    }

    public double getPreviewFps() {
        if (this.mHandle != 0) {
            return getPreviewFps(this.mHandle);
        }
        return 0.0d;
    }

    public double getFeedAvgFps() {
        if (this.mHandle != 0) {
            return getFeedAvgFps(this.mHandle);
        }
        return 0.0d;
    }

    public boolean isBusy() {
        return this.mIsBusy;
    }

    public void callbackHandler(String text) {
        int statusCode = -1;
        try {
            statusCode = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (this.mListener != null) {
            this.mListener.onNotifyStatus(statusCode);
        }
    }

    public int dequeueBuffer() {
        if (this.mHandle != 0) {
            return dequeueBuffer(this.mHandle);
        }
        return -1;
    }

    public void queueBuffer(int bufferId, long timestamp) {
        if (this.mHandle != 0) {
            queueBuffer(this.mHandle, bufferId, timestamp);
        }
    }

    public void setSignatureCallback(SignatureCallbackInJava callback) {
        if (this.mHandle == 0 || callback == null) {
            this.mCallbackJava = null;
            setSignatureUse(this.mHandle, false);
            return;
        }
        this.mCallbackJava = callback;
        setSignatureUse(this.mHandle, true);
    }

    public void callback_prepareSignature(int panowidth, int panoheight, int degree) {
        if (!(this.mCallbackJava == null || this.mHandle == 0)) {
            Bitmap sigBmp = this.mCallbackJava.setSignatureParam(panowidth, panoheight, degree);
            if (sigBmp != null) {
                int sig_width = sigBmp.getWidth();
                int sig_height = sigBmp.getHeight();
                int[] argb = new int[(sig_width * sig_height)];
                sigBmp.getPixels(argb, 0, sig_width, 0, 0, sig_width, sig_height);
                int srcLength = argb.length;
                byte[] data = new byte[(srcLength << 2)];
                for (int i = 0; i < srcLength; i++) {
                    int x = argb[i];
                    int j = i << 2;
                    byte r = (byte) ((16711680 & x) >> 16);
                    byte g = (byte) ((65280 & x) >> 8);
                    byte b = (byte) ((x & 255) >> 0);
                    int j2 = j + 1;
                    data[j] = (byte) ((ViewCompat.MEASURED_STATE_MASK & x) >> 24);
                    j = j2 + 1;
                    data[j2] = r;
                    j2 = j + 1;
                    data[j] = g;
                    j = j2 + 1;
                    data[j2] = b;
                }
                sigBmp.recycle();
                setSignatureData(this.mHandle, sig_width, sig_height, data, data.length);
                return;
            }
        }
        setSignatureData(this.mHandle, -1, -1, null, -1);
    }

    public void onXmpAvailableListener(int crop_left, int crop_top, int crop_width, int crop_height, int whole_width, int whole_height, float hori_fov, float vert_fov) {
        if (this.mXmpListener != null) {
            this.mXmpListener.onXmpAvailable(crop_left, crop_top, crop_width, crop_height, whole_width, whole_height, hori_fov, vert_fov);
        }
    }

    public void setXmpAvailableListener(XmpAvailableListener listener) {
        if (this.mHandle != 0) {
            this.mXmpListenerObj = setXmpAvailableListener(this.mHandle);
            this.mXmpListener = listener;
        }
    }
}
