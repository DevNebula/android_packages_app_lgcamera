package com.lge.camera.device.api2;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera.Area;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.utils.SurfaceUtils;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.Utils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Camera2Util {
    static final double AVAILABLE_SEC_PER_FRAME = 0.1d;
    private static final int DEFAULT_THUMBNAIL_WIDTH = 320;
    private static final int[][] sFrontLiveSnapshotSize = new int[][]{new int[]{2268, CameraConstantsEx.FHD_SCREEN_RESOLUTION}};
    private static final int[][] sRearLiveSnapshotSize = new int[][]{new int[]{4656, 2218}, new int[]{2560, CameraConstantsEx.FHD_SCREEN_RESOLUTION}};
    private static Size[] sSupportedThumbnailList = null;

    public static Size getThumbnailSize(float pictureRatio) {
        if (sSupportedThumbnailList == null) {
            CamLog.m11w(CameraConstants.TAG, "supported thumbnail size is null, return");
            return null;
        }
        Size thumbnailSize = new Size(0, 0);
        for (int i = sSupportedThumbnailList.length - 1; i >= 0; i--) {
            Size size = sSupportedThumbnailList[i];
            if (size.getHeight() != 0) {
                float ratio = ((float) size.getWidth()) / ((float) size.getHeight());
                if (ratio == pictureRatio) {
                    return size;
                }
                if (Math.abs(ratio - pictureRatio) <= 0.001f && size.getWidth() > thumbnailSize.getWidth()) {
                    thumbnailSize = size;
                }
            }
        }
        if (thumbnailSize.getWidth() == 0 || thumbnailSize.getHeight() == 0) {
            int height = (int) (((float) 320) / pictureRatio);
            thumbnailSize = new Size(320, height);
            CamLog.m5e(CameraConstants.TAG, "Not supported in thumbnail supported list, set size to " + 320 + "x" + height);
        }
        return thumbnailSize;
    }

    public static void setSupportedThumbnailSizeList(CameraCharacteristics characteristics) {
        if (characteristics != null) {
            sSupportedThumbnailList = (Size[]) characteristics.get(CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES);
        }
    }

    public static void showParameterValues(Parameters2 param) {
        if (param != null && CamLog.getLogOn()) {
            Size previewSize = param.getPreviewSize();
            if (previewSize != null) {
                CamLog.m3d(CameraConstants.TAG, "setParameters() - previewSize = " + previewSize.getWidth() + "x" + previewSize.getHeight());
            }
            Size pictureSize = param.getPictureSize();
            if (pictureSize != null) {
                CamLog.m3d(CameraConstants.TAG, "setParameters() - pictureSize = " + pictureSize.getWidth() + "x" + pictureSize.getHeight());
            }
            CamLog.m3d(CameraConstants.TAG, "setParameters() - focusMode = " + param.getFocusMode());
            CamLog.m3d(CameraConstants.TAG, "setParameters() - zoom = " + param.getZoom());
        }
    }

    public static void initParametersZoom(CameraCharacteristics characteristics, Parameters2 params) {
        if (characteristics != null && params != null) {
            boolean isZoomSupported = isZoomSupported(characteristics);
            params.set(ParamConstants.KEY_ZOOM_SUPPORTED, isZoomSupported ? "true" : "false");
            if (isZoomSupported) {
                int[] zoomRatio = getZoomRatioList(characteristics);
                if (zoomRatio != null) {
                    String zoomRatios = Utils.getCommaSeparatedString(zoomRatio);
                    params.set(ParamConstants.KEY_MAX_ZOOM, zoomRatio.length - 1);
                    params.set(ParamConstants.KEY_ZOOM_RATIOS, zoomRatios);
                }
            }
        }
    }

    public static void initParametersEV(CameraCharacteristics characteristics, Parameters2 params) {
        if (characteristics != null && params != null) {
            Rational compensation_step = (Rational) characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP);
            Range<Integer> range = (Range) characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
            params.set(ParamConstants.KEY_EXPOSURE_COMPENSATION_STEP, compensation_step.floatValue());
            params.set(ParamConstants.KEY_MIN_EXPOSURE_COMPENSATION, ((Integer) range.getLower()).intValue());
            params.set(ParamConstants.KEY_MAX_EXPOSURE_COMPENSATION, ((Integer) range.getUpper()).intValue());
            CamLog.m3d(CameraConstants.TAG, "compensation step = " + compensation_step.floatValue());
        }
    }

    public static void initParametersLG(CameraCharacteristics characteristics, Parameters2 params) {
        if (characteristics != null && params != null) {
            params.set("lg-wb-supported-min", 2400);
            params.set("lg-wb-supported-max", 7500);
            params.set("lg-wb-supported-step", 100);
            Range<Integer> rangeISO = (Range) characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
            params.set("", clamp(((Integer) rangeISO.getLower()).intValue(), 0, 100));
            params.set("", clamp(((Integer) rangeISO.getLower()).intValue(), (int) CameraConstantsEx.GOOGLE_ASSISTANT_TAKE_CMD_DELAY, Integer.MAX_VALUE));
            params.set("", 100);
            Range<Long> rangeSS = (Range) characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
            params.set(ParamConstants.KEY_SHUTTER_SPEED_SUPPOPRTED_VALUES, rangeSS.getLower() + "," + rangeSS.getUpper());
            params.set(ParamConstants.KEY_MANUAL_MODE_RESET, 0);
            params.set("lg-wb", 0);
            params.set(ParamConstants.KEY_EXPOSURE_COMPENSATION, 0);
            params.set(ParamConstants.KEY_MANUAL_ISO, 0);
            params.set("shutter-speed", 0);
            params.set("hdr-mode", 0);
        }
    }

    public static boolean isReprocessSupported(CameraCharacteristics characteristics) {
        if (characteristics == null) {
            return false;
        }
        int[] capa = (int[]) characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
        if (capa == null) {
            return false;
        }
        for (int value : capa) {
            if (value == 7) {
                CamLog.m7i(CameraConstants.TAG, " isReprocessSupported : True ");
                return true;
            }
        }
        return false;
    }

    public static void initSupportedPictureSize(CameraCharacteristics characteristics, Parameters2 params) {
        if (characteristics != null && params != null) {
            String jpegSizes = getString(((StreamConfigurationMap) characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getOutputSizes(256));
            params.setSupportedPictureSizes(jpegSizes);
            initSupportedVideoSnapSize(characteristics, params);
            CamLog.m7i(CameraConstants.TAG, " picture size : " + jpegSizes);
            CamLog.m7i(CameraConstants.TAG, " preview size : " + getString(((StreamConfigurationMap) characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getOutputSizes(SurfaceHolder.class)));
        }
    }

    private static void initSupportedVideoSnapSize(CameraCharacteristics characteristics, Parameters2 params) {
        if (characteristics != null && params != null) {
            boolean isRear;
            if (((Integer) characteristics.get(CameraCharacteristics.LENS_FACING)).intValue() == 1) {
                isRear = true;
            } else {
                isRear = false;
            }
            int[][] addSizes = isRear ? sRearLiveSnapshotSize : sFrontLiveSnapshotSize;
            Size[] supportedSizeArray = ((StreamConfigurationMap) characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getOutputSizes(256);
            Size[] newSizeArray = (Size[]) Arrays.copyOf(supportedSizeArray, supportedSizeArray.length + addSizes.length);
            for (int i = 0; i < addSizes.length; i++) {
                newSizeArray[supportedSizeArray.length + i] = new Size(addSizes[i][0], addSizes[i][1]);
            }
            String snapShotStr = getString(newSizeArray);
            params.setSupportedVideoSnapSizes(snapShotStr);
            CamLog.m3d(CameraConstants.TAG, " LiveSnapShot size : " + snapShotStr);
        }
    }

    public static void initMaxFaceCount(CameraCharacteristics characteristics, Parameters2 params) {
        if (characteristics != null && params != null) {
            params.set(ParamConstants.KEY_MAX_NUM_DETECTED_FACES_HW, ((Integer) characteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT)).intValue());
        }
    }

    public static void initVideoSnapshotSupported(CameraCharacteristics characteristics, Parameters2 params) {
        if (characteristics != null && params != null) {
            if (((Integer) characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)).intValue() == 2) {
                params.set(ParamConstants.KEY_VIDEO_SNAPSHOT_SUPPORTED, "false");
            } else {
                params.set(ParamConstants.KEY_VIDEO_SNAPSHOT_SUPPORTED, "true");
            }
        }
    }

    public static void initFlashSupportedList(CameraCharacteristics characteristics, Parameters2 params) {
        if (characteristics != null && params != null) {
            params.set("flash-mode-values", ((Boolean) characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)).booleanValue() ? "off,on,auto,torch" : "off");
        }
    }

    public static void initAFSupportedList(CameraCharacteristics characteristics, Parameters2 params) {
        if (characteristics != null && params != null) {
            String ret = null;
            for (int mode : (int[]) characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)) {
                if (ret == null) {
                    ret = Camera2Converter.getFocusModeStr(mode);
                } else {
                    ret = ret + "," + Camera2Converter.getFocusModeStr(mode);
                }
            }
            if (ret != null && ret.contains(ParamConstants.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                ret = ret + ",mw_continuous-picture";
            }
            String str = "focus-mode-values";
            if (ret == null) {
                ret = ParamConstants.FOCUS_MODE_FIXED;
            }
            params.set(str, ret);
        }
    }

    public static boolean isAFSupported(CameraCharacteristics characteristics) {
        if (characteristics == null) {
            return false;
        }
        for (int mode : (int[]) characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)) {
            if (mode == 1) {
                return true;
            }
        }
        return false;
    }

    public static void initOutputsList(CameraCharacteristics characteristics, Parameters2 params) {
        if (characteristics != null && params != null) {
            params.set(ParamConstants.KEY_APP_OUTPUTS_TYPE, ParamConstants.OUTPUTS_DEFAULT_STR);
            params.set(ParamConstants.KEY_APP_FULL_FRAME_BUF_SIZE, ParamConstants.FULL_FRAME_DEFAULT);
            params.set(ParamConstants.KEY_APP_PREVIEW_CB_BUF_SIZE, "2");
            params.set(ParamConstants.KEY_ZSL, "on");
        }
    }

    public static Size getInputSize(CameraCharacteristics characteristics, Size pictureSizeParam) {
        int width = pictureSizeParam.getWidth();
        int height = pictureSizeParam.getHeight();
        Size[] inputSize = ((StreamConfigurationMap) characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getInputSizes(35);
        if (inputSize != null) {
            boolean isSupported = false;
            for (Size size : inputSize) {
                if (pictureSizeParam.equals(size)) {
                    isSupported = true;
                    break;
                }
            }
            if (!isSupported) {
                width = inputSize[0].getWidth();
                height = inputSize[0].getHeight();
                CamLog.m5e(CameraConstants.TAG, "Not supported input Size = " + pictureSizeParam.toString());
            }
        }
        return new Size(width, height);
    }

    public static boolean isSteadyCamSupported(CameraCharacteristics characteristics) {
        if (characteristics == null) {
            return false;
        }
        for (int value : (int[]) characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)) {
            if (value == 1) {
                CamLog.m7i(CameraConstants.TAG, " isSteadyCamSupported : True");
                return true;
            }
        }
        return false;
    }

    public static void initVideoHDRSupported(CameraCharacteristics characteristics, Parameters2 param, int cameraId) {
        int i = 0;
        if (characteristics != null && param != null) {
            try {
                String videoHDR = "";
                String[] videoHDRStrList = new String[(cameraId + 1)];
                int[] videoHDRList = (int[]) characteristics.get(ParamConstants.KEY_SUPPORTED_VIDEO_HDR);
                if (videoHDRList != null) {
                    for (int value : videoHDRList) {
                        if (value == 1) {
                            videoHDRStrList[cameraId] = "1";
                        }
                    }
                    while (i < videoHDRStrList.length) {
                        videoHDR = videoHDR + videoHDRStrList[i] + ",";
                        i++;
                    }
                    param.set(ParamConstants.KEY_VIDEO_HDR_SUPPORTED, videoHDR);
                }
            } catch (IllegalArgumentException e) {
                if (CamLog.isTagExceptionLogOn()) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void initAELockSupported(CameraCharacteristics characteristics, Parameters2 param) {
        if (characteristics != null && param != null) {
            try {
                Boolean isAELockSupported = (Boolean) characteristics.get(CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE);
                if (isAELockSupported == null || !isAELockSupported.booleanValue()) {
                    param.set(ParamConstants.KEY_AUTO_EXPOSURE_LOCK_SUPPORTED, "false");
                } else {
                    param.set(ParamConstants.KEY_AUTO_EXPOSURE_LOCK_SUPPORTED, "true");
                }
            } catch (IllegalArgumentException e) {
                if (CamLog.isTagExceptionLogOn()) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getString(Size[] sizeArray) {
        String ret = null;
        for (Size size : sizeArray) {
            if (ret == null) {
                ret = size.toString();
            } else {
                ret = ret + "," + size.toString();
            }
        }
        return ret;
    }

    public static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public static float clamp(float x, float min, float max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public static Size[] getAvailableSizesForFormatChecked(StreamConfigurationMap config, int format, int direction, boolean fastSizes, boolean slowSizes) {
        if (config == null) {
            return new Size[0];
        }
        Size[] sizes = null;
        switch (direction) {
            case 0:
                sizes = config.getInputSizes(format);
                CamLog.m3d(CameraConstants.TAG, "inputSize List : \n");
                break;
            case 1:
                Size[] fastSizeList = null;
                Size[] slowSizeList = null;
                if (fastSizes) {
                    fastSizeList = config.getOutputSizes(format);
                    CamLog.m3d(CameraConstants.TAG, "Output Size fast list : \n");
                }
                if (slowSizes) {
                    slowSizeList = config.getHighResolutionOutputSizes(format);
                    CamLog.m3d(CameraConstants.TAG, "Output Size slow List : \n");
                }
                if (fastSizeList == null || slowSizeList == null) {
                    if (fastSizeList == null) {
                        if (slowSizeList != null) {
                            sizes = slowSizeList;
                            break;
                        }
                    }
                    sizes = fastSizeList;
                    break;
                }
                sizes = new Size[(slowSizeList.length + fastSizeList.length)];
                System.arraycopy(fastSizeList, 0, sizes, 0, fastSizeList.length);
                System.arraycopy(slowSizeList, 0, sizes, fastSizeList.length, slowSizeList.length);
                break;
                break;
            default:
                throw new IllegalArgumentException("direction must be output or input");
        }
        if (sizes == null) {
            return new Size[0];
        }
        return sizes;
    }

    public static int[] getZoomRatioList(CameraCharacteristics characteristics) {
        if (characteristics == null) {
            return null;
        }
        try {
            return (int[]) characteristics.get(ParamConstants.KEY_ZOOM_RATIOS_VALUE);
        } catch (IllegalArgumentException e) {
            if (CamLog.isTagExceptionLogOn()) {
                e.printStackTrace();
            }
            return createZoomRatioList(characteristics);
        }
    }

    public static float getMaxZoom(CameraCharacteristics characteristics) {
        if (characteristics == null) {
            return 1.0f;
        }
        return ((Float) characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)).floatValue();
    }

    public static Size getRawSize(CameraCharacteristics characteristics) {
        Size[] rawSize = ((StreamConfigurationMap) characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getOutputSizes(32);
        for (int i = 0; i < rawSize.length; i++) {
            CamLog.m3d(CameraConstants.TAG, "rawSize w= " + rawSize[i].getWidth() + ", h= " + rawSize[i].getHeight());
        }
        return new Size(rawSize[0].getWidth(), rawSize[0].getHeight());
    }

    public static boolean isZoomSupported(CameraCharacteristics characteristics) {
        if (characteristics != null && getMaxZoom(characteristics) > 1.0f) {
            return true;
        }
        return false;
    }

    public static MeteringRectangle[] getMeteringRectFromArea(List<Area> area, Rect zoomCropRect) {
        if (area == null) {
            return null;
        }
        int size = area.size();
        MeteringRectangle[] rect = new MeteringRectangle[size];
        for (int i = 0; i < size; i++) {
            RectF meteringRegionF = new RectF((float) ((Area) area.get(i)).rect.left, (float) ((Area) area.get(i)).rect.top, (float) ((Area) area.get(i)).rect.right, (float) ((Area) area.get(i)).rect.bottom);
            Matrix matrix = new Matrix();
            matrix.preTranslate(((float) (-zoomCropRect.width())) / 2.0f, ((float) (-zoomCropRect.height())) / 2.0f);
            matrix.postScale(2000.0f / ((float) zoomCropRect.width()), 2000.0f / ((float) zoomCropRect.height()));
            matrix.invert(matrix);
            matrix.mapRect(meteringRegionF);
            Rect meteringRegion = new Rect(((int) meteringRegionF.left) + zoomCropRect.left, ((int) meteringRegionF.top) + zoomCropRect.top, ((int) meteringRegionF.right) + zoomCropRect.left, ((int) meteringRegionF.bottom) + zoomCropRect.top);
            meteringRegion.left = clamp(meteringRegion.left, zoomCropRect.left, zoomCropRect.right);
            meteringRegion.top = clamp(meteringRegion.top, zoomCropRect.top, zoomCropRect.bottom);
            meteringRegion.right = clamp(meteringRegion.right, zoomCropRect.left, zoomCropRect.right);
            meteringRegion.bottom = clamp(meteringRegion.bottom, zoomCropRect.top, zoomCropRect.bottom);
            rect[i] = new MeteringRectangle(meteringRegion, ((Area) area.get(i)).weight);
            CamLog.m3d(CameraConstants.TAG, "meteringRegion left : " + meteringRegion.left + ", top : " + meteringRegion.top + ", right : " + meteringRegion.right + ", bottom :  " + meteringRegion.bottom);
        }
        return rect;
    }

    public static void getWhiteBalancedColor(Integer[] color, int kelvin) {
        int red;
        int green;
        int blue;
        int temp = kelvin / 100;
        if (temp < 66) {
            red = 255;
            green = (int) ((99.4708025861d * Math.log((double) temp)) - 161.1195681661d);
            if (temp <= 19) {
                blue = 0;
            } else {
                blue = (int) ((138.5177312231d * Math.log((double) (temp - 10))) - 305.0447927307d);
            }
        } else {
            red = (int) (329.698727446d * Math.pow((double) (temp - 60), -0.1332047592d));
            green = (int) (288.1221695283d * Math.pow((double) (temp - 60), -0.0755148492d));
            blue = 255;
        }
        color[0] = Integer.valueOf(clamp(red, 0, 255));
        color[1] = Integer.valueOf(clamp(green, 0, 255));
        color[2] = Integer.valueOf(clamp(blue, 0, 255));
    }

    public static int[] createZoomRatioList(CameraCharacteristics characteristics) {
        int i;
        CamLog.m7i(CameraConstants.TAG, "createZoomRatioList");
        int[] baseZoomLevelList = new int[]{4096, 4191, 4289, 4389, 4492, 4597, 4705, 4815, 4927, 5042, 5160, 5281, 5404, 5531, 5660, 5792, 5928, 6066, 6208, 6353, 6501, 6653, 6809, 6968, 7131, 7298, 7468, 7643, 7822, 8004, 8192, 8383, 8579, 8779, 8985, 9195, 9410, 9630, 9855, 10085, 10321, 10562, 10809, 11062, 11320, 11585, 11856, 12133, 12416, 12706, 13003, 13307, 13619, 13937, 14263, 14596, 14937, 15286, 15644, 16009, 16384, 16766, 17158, 17559, 17970, 18390, 18820, 19260, 19710, 20171, 20642, 21125, 21618, 22124, 22641, 23170, 23712, 24266, 24833, 25413, 26007, 26615, 27238, 27874, 28526, 29192, 29875, 30573, 31288, 32019, 32768};
        ArrayList<Integer> zoomRatioList = new ArrayList();
        int maxZoom = (int) getMaxZoom(characteristics);
        int maxStep = maxZoom <= 4 ? 61 : 721;
        CamLog.m7i(CameraConstants.TAG, "maxZoom " + maxZoom + " / maxStep : " + maxStep);
        int baseListLen = baseZoomLevelList.length;
        int addStepNum = maxStep / (baseListLen - 1);
        if (maxStep > baseListLen) {
            for (i = 0; i < baseListLen - 1; i++) {
                float addNum = (float) ((baseZoomLevelList[i + 1] - baseZoomLevelList[i]) / addStepNum);
                for (int j = 0; j < addStepNum; j++) {
                    zoomRatioList.add(Integer.valueOf(((baseZoomLevelList[i] + (Math.round(addNum) * j)) * 100) / baseZoomLevelList[0]));
                }
            }
            zoomRatioList.add(Integer.valueOf((baseZoomLevelList[baseListLen - 1] * 100) / baseZoomLevelList[0]));
        } else {
            for (i = 0; i < maxStep; i++) {
                zoomRatioList.add(Integer.valueOf((baseZoomLevelList[i] * 100) / baseZoomLevelList[0]));
            }
        }
        int[] zoomList = new int[zoomRatioList.size()];
        for (i = 0; i < zoomRatioList.size(); i++) {
            zoomList[i] = ((Integer) zoomRatioList.get(i)).intValue();
        }
        return zoomList;
    }

    public static int getExifOrientaion(Parameters2 param) {
        switch (Integer.valueOf(param.get(ParamConstants.KEY_ROTATION)).intValue()) {
            case 90:
                return 6;
            case 180:
                return 3;
            case 270:
                return 8;
            default:
                return 1;
        }
    }

    public static void initParametersFromBuilder(CameraDevice camera, Parameters2 params) {
        if (camera != null && params != null) {
            try {
                Builder builder = camera.createCaptureRequest(5);
                if (builder == null) {
                    CamLog.m7i(CameraConstants.TAG, "builder create fail");
                    return;
                }
                CaptureRequest request = builder.build();
                if (request != null) {
                    params.set(ParamConstants.KEY_F_NUMBER, ((Float) request.get(CaptureRequest.LENS_APERTURE)).floatValue());
                    params.set(ParamConstants.KEY_FOCAL_LENGTH, ((Float) request.get(CaptureRequest.LENS_FOCAL_LENGTH)).floatValue());
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static void initFocusInfo(CameraCharacteristics characteristics, Parameters2 params) {
        if (characteristics != null && params != null) {
            try {
                int focusAreaMaxNum = ((Integer) characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)).intValue();
                int meteringAreaMaxNum = ((Integer) characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)).intValue();
                params.set(ParamConstants.KEY_MAX_NUM_FOCUS_AREAS, focusAreaMaxNum);
                params.set(ParamConstants.KEY_MAX_NUM_METERING_AREAS, meteringAreaMaxNum);
            } catch (IllegalArgumentException e) {
                if (CamLog.isTagExceptionLogOn()) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void initRawSize(CameraCharacteristics characteristics, Parameters2 params) {
        if (characteristics != null && params != null) {
            params.set(ParamConstants.KEY_RAW_SIZE, "32550804");
        }
    }

    public static int pixelFormatForCameraFormat(String format) {
        if (format == null) {
            return 0;
        }
        if (format.equals(Parameters2.PIXEL_FORMAT_YUV422SP)) {
            return 16;
        }
        if (format.equals("yuv420sp")) {
            return 17;
        }
        if (format.equals(Parameters2.PIXEL_FORMAT_YUV422I)) {
            return 20;
        }
        if (format.equals(Parameters2.PIXEL_FORMAT_YUV420P)) {
            return 842094169;
        }
        if (format.equals(Parameters2.PIXEL_FORMAT_RGB565)) {
            return 4;
        }
        if (format.equals(Parameters2.PIXEL_FORMAT_JPEG)) {
            return 256;
        }
        return 0;
    }

    public static boolean isSupportHighSpeedVideo(Surface recorderSurface, CameraCharacteristics cameraCharacteristics) {
        if (recorderSurface == null || cameraCharacteristics == null) {
            return false;
        }
        StreamConfigurationMap config = (StreamConfigurationMap) cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        List<Size> highSpeedSizes = null;
        if (config != null) {
            highSpeedSizes = Arrays.asList(config.getHighSpeedVideoSizes());
        }
        Size recordSurfaceSize = SurfaceUtils.getSurfaceSize(recorderSurface);
        CamLog.m5e(CameraConstants.TAG, "recordSurfaceSize : " + recordSurfaceSize);
        if (highSpeedSizes != null && highSpeedSizes.contains(recordSurfaceSize)) {
            return true;
        }
        CamLog.m5e(CameraConstants.TAG, "This recordSurfaceSize is not available");
        return false;
    }

    public static boolean isHighSpeedRecording(Parameters2 parameter) {
        int[] fps = new int[2];
        if (parameter == null) {
            return false;
        }
        parameter.getPreviewFpsRange(fps);
        if (fps[0] > 60) {
            return true;
        }
        return false;
    }

    public static boolean isHighSpeedCaptureSession(CameraCaptureSession session) {
        if (session == null) {
            return false;
        }
        return session instanceof CameraConstrainedHighSpeedCaptureSession;
    }

    public static double convertAvailableFPS(double curSecPerFrame) {
        CamLog.m3d(CameraConstants.TAG, "fake" + Math.min(curSecPerFrame, AVAILABLE_SEC_PER_FRAME));
        return Math.min(curSecPerFrame, AVAILABLE_SEC_PER_FRAME);
    }

    public static double log2(double d) {
        return Math.log(d) / Math.log(2.0d);
    }

    public static boolean isShutterSpeedLongerThan(int shotMode, String ss, float sec) {
        if (ss == null || shotMode != 16 || "not found".equals(ss)) {
            return false;
        }
        if (FunctionProperties.isSupportedManualZSL()) {
            try {
                if (Float.compare(Float.valueOf(ss).floatValue(), sec * 1000.0f) < 0) {
                    return false;
                }
                return true;
            } catch (NumberFormatException e) {
                CamLog.m3d(CameraConstants.TAG, "invalide shutter speed :" + e);
                return false;
            }
        } else if (Double.compare(ManualUtil.getShutterSpeedInDouble(ss), (double) sec) < 0) {
            return false;
        } else {
            return true;
        }
    }

    public static ByteBuffer cloneByteBuffer(ByteBuffer original) {
        ByteBuffer clone = ByteBuffer.allocate(original.capacity());
        original.rewind();
        clone.put(original);
        original.rewind();
        clone.flip();
        return clone;
    }
}
