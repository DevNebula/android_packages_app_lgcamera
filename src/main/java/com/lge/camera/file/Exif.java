package com.lge.camera.file;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.SystemProperties;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import java.io.IOException;
import java.util.List;
import java.util.TimeZone;

public class Exif {
    public static final String EXIF_STR_MAKE = "LG Electronics";
    private static final int EXIF_THUMB_LONG_SIZE = 512;
    private static final int EXIF_THUMB_NORMAL_SIZE = 320;
    private static final String EXIF_VERSION = "0220";

    public static ExifInterface createExif(byte[] jpegData, int jpegWidth, int jpegHeight, CameraParameters parameters, Location loc, int degree, int headingValue, SupportedExif supportedExif) {
        ExifInterface exif = readExif(jpegData);
        initExif(exif, jpegWidth, jpegHeight, parameters, loc, degree, headingValue, supportedExif, jpegData);
        return exif;
    }

    public static ExifInterface createExif(byte[] jpegData, int jpegWidth, int jpegHeight, CameraParameters parameters, Location loc, int degree, int headingValue, SupportedExif supportedExif, short sceneCaptureType) {
        ExifInterface exif = readExif(jpegData);
        initExif(exif, jpegWidth, jpegHeight, parameters, loc, degree, headingValue, supportedExif, jpegData, sceneCaptureType);
        return exif;
    }

    public static ExifInterface createExif(String filePath, int jpegWidth, int jpegHeight, CameraParameters parameters, Location loc, int degree, int headingValue, SupportedExif supportedExif, short sceneCaptureType) {
        ExifInterface exif = readExif(filePath);
        initExif(exif, jpegWidth, jpegHeight, parameters, loc, degree, headingValue, supportedExif, null, sceneCaptureType);
        return exif;
    }

    public static ExifInterface createExif(ExifInterface exif, String filePath, int jpegWidth, int jpegHeight, CameraParameters parameters, Location loc, int degree, int headingValue, SupportedExif supportedExif, short sceneCaptureType) {
        initExif(exif, jpegWidth, jpegHeight, parameters, loc, degree, headingValue, supportedExif, null, sceneCaptureType);
        return exif;
    }

    public static ExifInterface initExif(ExifInterface exif, int jpegWidth, int jpegHeight, CameraParameters parameters, Location loc, int degree, int headingValue, SupportedExif supportedExif, byte[] jpegData) {
        return initExif(exif, jpegWidth, jpegHeight, parameters, loc, degree, headingValue, supportedExif, jpegData, (short) 0);
    }

    public static ExifInterface initExif(ExifInterface exif, int jpegWidth, int jpegHeight, CameraParameters parameters, Location loc, int degree, int headingValue, SupportedExif supportedExif, byte[] jpegData, short sceneCaptureType) {
        long curTime = System.currentTimeMillis();
        if (!ModelProperties.isFakeExif()) {
            String modelName;
            if ("".equals(SystemProperties.get("ro.product.model"))) {
                modelName = Build.MODEL;
            } else {
                modelName = SystemProperties.get("ro.product.model");
            }
            exif.setTag(exif.buildTag(ExifInterface.TAG_MODEL, modelName));
        }
        exif.setTag(exif.buildTag(ExifInterface.TAG_MAKE, EXIF_STR_MAKE));
        exif.setTag(exif.buildTag(ExifInterface.TAG_EXIF_VERSION, EXIF_VERSION));
        exif.addDateTimeStampTag(ExifInterface.TAG_DATE_TIME, curTime, TimeZone.getDefault());
        exif.addDateTimeStampTag(ExifInterface.TAG_DATE_TIME_ORIGINAL, curTime, TimeZone.getDefault());
        exif.addDateTimeStampTag(ExifInterface.TAG_DATE_TIME_DIGITIZED, curTime, TimeZone.getDefault());
        if (degree != -1) {
            exif.setTag(exif.buildTag(ExifInterface.TAG_ORIENTATION, Short.valueOf(ExifInterface.getOrientationValueForRotation(degree))));
        } else {
            exif.setTag(exif.buildTag(ExifInterface.TAG_ORIENTATION, Short.valueOf(ExifInterface.getOrientationValueForRotation(getOrientation(exif)))));
        }
        exif.setTag(exif.buildTag(ExifInterface.TAG_IMAGE_WIDTH, Integer.valueOf(jpegWidth)));
        exif.setTag(exif.buildTag(ExifInterface.TAG_IMAGE_LENGTH, Integer.valueOf(jpegHeight)));
        exif.setTag(exif.buildTag(ExifInterface.TAG_PIXEL_X_DIMENSION, Integer.valueOf(jpegWidth)));
        exif.setTag(exif.buildTag(ExifInterface.TAG_PIXEL_Y_DIMENSION, Integer.valueOf(jpegHeight)));
        exif.setTag(exif.buildTag(ExifInterface.TAG_COLOR_SPACE, Short.valueOf((short) 1)));
        exif.setTag(exif.buildTag(ExifInterface.TAG_RESOLUTION_UNIT, Short.valueOf((short) 2)));
        exif.setTag(exif.buildTag(ExifInterface.TAG_X_RESOLUTION, new Rational(72, 1)));
        exif.setTag(exif.buildTag(ExifInterface.TAG_Y_RESOLUTION, new Rational(72, 1)));
        exif.setTag(exif.buildTag(ExifInterface.TAG_Y_CB_CR_POSITIONING, Short.valueOf((short) 1)));
        if (supportedExif == null || supportedExif.mFocalLengthTag) {
            setFocalLengthTag(exif, parameters);
        }
        if (supportedExif != null && supportedExif.mFlashTag) {
            setFlashTag(exif, parameters);
        }
        if (supportedExif != null && supportedExif.mWhiteBalanceTag) {
            setWhiteBalanceTag(exif, parameters);
        }
        if (supportedExif != null && supportedExif.mMeteringTag) {
            setMeteringModeTag(exif, parameters);
        }
        if (supportedExif != null && supportedExif.mZoomRatioTag) {
            setZoomRatioTag(exif, parameters);
        }
        if (supportedExif != null && supportedExif.mExposureBiasTag) {
            setExposureBiasValueTag(exif, parameters);
        }
        if (sceneCaptureType != (short) 0) {
            exif.setTag(exif.buildTag(ExifInterface.TAG_SCENE_CAPTURE_TYPE, Short.valueOf(sceneCaptureType)));
        }
        exif.setTag(exif.buildTag(ExifInterface.TAG_INTEROPERABILITY_INDEX, ExifInterface.INTEROP_INDEX_STR));
        exif.setTag(exif.buildTag(ExifInterface.TAG_INTEROPERABILITY_VERSION, ExifInterface.INTEROP_VER));
        if (loc != null) {
            exif.addGpsTags(loc.getLatitude(), loc.getLongitude());
            exif.addGpsDateTimeStampTag(curTime);
            setGpsImageHeading(exif, 1);
        }
        if (!(supportedExif == null || !supportedExif.mUpdateThumbnail || jpegData == null)) {
            exif.setTag(exif.buildTag(ExifInterface.TAG_X_RESOLUTION_IFD1, new Rational(72, 1)));
            exif.setTag(exif.buildTag(ExifInterface.TAG_Y_RESOLUTION_IFD1, new Rational(72, 1)));
            if (degree != -1) {
                exif.setTag(exif.buildTag(ExifInterface.TAG_ORIENTATION_IFD1, Short.valueOf(ExifInterface.getOrientationValueForRotation(degree))));
            }
            exif.setTag(exif.buildTag(ExifInterface.TAG_RESOLUTION_UNIT_IFD1, Short.valueOf((short) 2)));
            exif.setTag(exif.buildTag(ExifInterface.TAG_COMPRESSION_IFD1, Short.valueOf((short) 6)));
            updateThumbnail(exif, jpegData, jpegWidth, jpegHeight);
        }
        return exif;
    }

    public static ExifInterface readExif(byte[] jpegData) {
        ExifInterface exif = new ExifInterface();
        try {
            exif.readExif(jpegData);
        } catch (IOException e) {
            CamLog.m12w(CameraConstants.TAG, "Failed to read EXIF data", e);
        } catch (NullPointerException e2) {
            CamLog.m12w(CameraConstants.TAG, "NullPointerException on EXIF data", e2);
        }
        return exif;
    }

    public static ExifInterface readExif(String filePath) {
        ExifInterface exif = new ExifInterface();
        try {
            exif.readExif(filePath);
        } catch (IOException e) {
            CamLog.m11w(CameraConstants.TAG, "Failed to read EXIF data");
        }
        return exif;
    }

    public static int getOrientation(ExifInterface exif) {
        if (exif == null) {
            return 0;
        }
        Integer val = exif.getTagIntValue(ExifInterface.TAG_ORIENTATION);
        if (val != null) {
            return ExifInterface.getRotationForOrientationValue(val.shortValue());
        }
        return 0;
    }

    public static int getOrientation(byte[] jpegData) {
        if (jpegData == null) {
            return 0;
        }
        return getOrientation(readExif(jpegData));
    }

    public static int getOrientation(String filePath) {
        if (filePath == null) {
            return 0;
        }
        return getOrientation(readExif(filePath));
    }

    public static int getFlash(byte[] jpegData) {
        if (jpegData == null) {
            return 0;
        }
        ExifInterface exif = readExif(jpegData);
        if (exif == null) {
            return 0;
        }
        Integer flashIntVal = exif.getTagIntValue(ExifInterface.TAG_FLASH);
        if (flashIntVal != null) {
            return flashIntVal.intValue();
        }
        return 0;
    }

    public static void setGpsImageHeading(ExifInterface exif, int headingValue) {
        if (headingValue >= 0) {
            exif.setTag(exif.buildTag(ExifInterface.TAG_GPS_IMG_DIRECTION_REF, "M"));
            exif.setTag(exif.buildTag(ExifInterface.TAG_GPS_IMG_DIRECTION, new Rational((long) headingValue, 1)));
        }
    }

    public static void setFocalLengthTag(ExifInterface exif, CameraParameters parameters) {
        if (parameters != null) {
            String focalLength = parameters.get(ParamConstants.KEY_FOCAL_LENGTH);
            if (focalLength == null) {
                CamLog.m5e(CameraConstants.TAG, "The focal length value is null.");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "Parameters focalLength is : " + focalLength);
            exif.setTag(exif.buildTag(ExifInterface.TAG_FOCAL_LENGTH, new Rational((long) ((int) (Float.valueOf(focalLength).floatValue() * 1000.0f)), 1000)));
        }
    }

    public static void setFlashTag(ExifInterface exif, CameraParameters parameters) {
        if (parameters != null) {
            String flashMode = parameters.getFlashMode();
            short flashValue = (short) 0;
            if ("auto".equals(flashMode)) {
                flashValue = (short) 24;
            } else if ("on".equals(flashMode)) {
                flashValue = (short) 1;
            } else if (ParamConstants.FLASH_MODE_RED_EYE.equals(flashMode)) {
                flashValue = (short) 65;
            } else if (ParamConstants.FLASH_MODE_TORCH.equals(flashMode)) {
                flashValue = (short) 1;
            }
            exif.setTag(exif.buildTag(ExifInterface.TAG_FLASH, Short.valueOf(flashValue)));
        }
    }

    public static void setWhiteBalanceTag(ExifInterface exif, CameraParameters parameters) {
        if (parameters != null) {
            String wb = parameters.getWhiteBalance();
            if (wb == null) {
                CamLog.m5e(CameraConstants.TAG, "The white balance value is null. return.");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "Parameters getWhiteBalance is : " + wb);
            exif.setTag(exif.buildTag(ExifInterface.TAG_WHITE_BALANCE, Short.valueOf("auto".equals(wb) ? (short) 0 : (short) 1)));
        }
    }

    public static void setMeteringModeTag(ExifInterface exif, CameraParameters parameters) {
        if (parameters != null) {
            short meteringValue = (short) 2;
            String meterings = parameters.get(ParamConstants.KEY_METERING_MODE);
            CamLog.m3d(CameraConstants.TAG, "Parameters metering mode is : " + meterings);
            if (ParamConstants.METER_MODE_SPOT.equals(meterings)) {
                meteringValue = (short) 3;
            } else if (ParamConstants.METER_MODE_AVERAGE.equals(meterings)) {
                meteringValue = (short) 1;
            }
            exif.setTag(exif.buildTag(ExifInterface.TAG_METERING_MODE, Short.valueOf(meteringValue)));
        }
    }

    public static void setZoomRatioTag(ExifInterface exif, CameraParameters parameters) {
        if (parameters != null) {
            List<Integer> zoomRatios = parameters.getZoomRatios();
            if (zoomRatios == null) {
                CamLog.m5e(CameraConstants.TAG, "Parameter.getZoomRatios is null. return.");
                return;
            }
            int zoom = parameters.getZoom();
            if (zoomRatios != null) {
                Integer zoomRatioValue = (Integer) zoomRatios.get(zoom);
                CamLog.m3d(CameraConstants.TAG, "zoom ratio param is = " + zoomRatioValue.intValue());
                exif.setTag(exif.buildTag(ExifInterface.TAG_DIGITAL_ZOOM_RATIO, new Rational((long) zoomRatioValue.intValue(), 100)));
            }
        }
    }

    public static void setExposureBiasValueTag(ExifInterface exif, CameraParameters parameters) {
        if (parameters != null) {
            int exposure = parameters.getExposureCompensation();
            CamLog.m3d(CameraConstants.TAG, "Parameters getExposureCompensation is : " + exposure);
            exif.setTag(exif.buildTag(ExifInterface.TAG_EXPOSURE_BIAS_VALUE, new Rational((long) exposure, 1)));
        }
    }

    private static void updateThumbnail(ExifInterface exif, byte[] jpegData, int jpegWidth, int jpegHeight) {
        updateThumbnail(exif, jpegData, jpegWidth, jpegHeight, 70);
    }

    public static void updateThumbnail(ExifInterface exif, byte[] jpegData, int jpegWidth, int jpegHeight, int quality) {
        int[] thumbSize = calcThumbnailSize(jpegWidth, jpegHeight);
        Bitmap thumbnail = BitmapManagingUtil.makeScaledBitmap(jpegData, thumbSize[0], thumbSize[1]);
        if (thumbnail != null) {
            exif.setCompressedThumbnail(thumbnail, quality);
        }
    }

    public static void updateThumbnail(ExifInterface exif, byte[] jpegData) {
        if (exif != null) {
            Integer widthValue = exif.getTagIntValue(ExifInterface.TAG_PIXEL_X_DIMENSION);
            Integer heightValue = exif.getTagIntValue(ExifInterface.TAG_PIXEL_Y_DIMENSION);
            int[] thumbSize = calcThumbnailSize(widthValue == null ? 0 : widthValue.intValue(), heightValue == null ? 0 : heightValue.intValue());
            Bitmap thumbnail = BitmapManagingUtil.makeScaledBitmap(jpegData, thumbSize[0], thumbSize[1]);
            exif.removeCompressedThumbnail();
            if (thumbnail != null) {
                exif.setCompressedThumbnail(thumbnail, 70);
            }
        }
    }

    public static void updateThumbnailForSticker(ExifInterface exif, byte[] jpegData) {
        if (exif != null) {
            Integer widthValue = exif.getTagIntValue(ExifInterface.TAG_PIXEL_X_DIMENSION);
            Integer heightValue = exif.getTagIntValue(ExifInterface.TAG_PIXEL_Y_DIMENSION);
            int[] thumbSize = calcThumbnailSizeForSticker(widthValue == null ? 0 : widthValue.intValue(), heightValue == null ? 0 : heightValue.intValue());
            Bitmap thumbnail = BitmapManagingUtil.makeScaledBitmap(jpegData, thumbSize[0], thumbSize[1]);
            exif.removeCompressedThumbnail();
            if (thumbnail != null) {
                exif.setCompressedThumbnail(thumbnail, 70);
            }
        }
    }

    public static int[] calcThumbnailSizeForSticker(int jpegWidth, int jpegHeight) {
        if (jpegWidth >= jpegHeight) {
            return calcThumbnailSize(jpegWidth, jpegHeight);
        }
        float ratio = ((float) jpegHeight) / ((float) jpegWidth);
        int thumbHeight = 320;
        int thumbWidth = Math.round(320.0f / ratio);
        if (ratio > 1.0f) {
            thumbHeight = 512;
            thumbWidth = Math.round(512.0f / ratio);
        }
        CamLog.m3d(CameraConstants.TAG, "calcThumbnailSize : thumbWidth = " + thumbWidth + ", thumbHeight = " + thumbHeight);
        return new int[]{thumbWidth, thumbHeight};
    }

    public static int[] calcThumbnailSize(int jpegWidth, int jpegHeight) {
        int longer = jpegWidth;
        int shorter = jpegHeight;
        if (jpegWidth < jpegHeight) {
            longer = jpegHeight;
            shorter = jpegWidth;
        }
        float ratio = (longer == 0 || shorter == 0) ? 1.0f : ((float) longer) / ((float) shorter);
        int thumbWidth = 320;
        int thumbHeight = Math.round(320.0f / ratio);
        if (ratio > 1.0f) {
            thumbWidth = 512;
            thumbHeight = Math.round(512.0f / ratio);
        }
        CamLog.m3d(CameraConstants.TAG, "calcThumbnailSize : thumbWidth = " + thumbWidth + ", thumbHeight = " + thumbHeight);
        return new int[]{thumbWidth, thumbHeight};
    }

    public static int[] getExifSize(String filePath) {
        ExifInterface exif = readExif(filePath);
        Integer widthValue = exif.getTagIntValue(ExifInterface.TAG_PIXEL_X_DIMENSION);
        Integer heightValue = exif.getTagIntValue(ExifInterface.TAG_PIXEL_Y_DIMENSION);
        CamLog.m3d(CameraConstants.TAG, "getExifSize : width = " + (widthValue == null ? 0 : widthValue.intValue()) + ", height = " + (heightValue == null ? 0 : heightValue.intValue()));
        return new int[]{widthValue == null ? 0 : widthValue.intValue(), heightValue == null ? 0 : heightValue.intValue()};
    }

    public static int[] getExifSize(ExifInterface exif) {
        if (exif == null) {
            return new int[]{0, 0};
        }
        Integer widthValue = exif.getTagIntValue(ExifInterface.TAG_PIXEL_X_DIMENSION);
        Integer heightValue = exif.getTagIntValue(ExifInterface.TAG_PIXEL_Y_DIMENSION);
        CamLog.m3d(CameraConstants.TAG, "getExifSize : width = " + (widthValue == null ? 0 : widthValue.intValue()) + ", height = " + (heightValue == null ? 0 : heightValue.intValue()));
        return new int[]{widthValue == null ? 0 : widthValue.intValue(), heightValue == null ? 0 : heightValue.intValue()};
    }

    public static int[] getImageSize(String filePath) {
        ExifInterface exif = readExif(filePath);
        Integer widthValue = exif.getTagIntValue(ExifInterface.TAG_IMAGE_WIDTH);
        Integer heightValue = exif.getTagIntValue(ExifInterface.TAG_IMAGE_LENGTH);
        CamLog.m3d(CameraConstants.TAG, "getImageSize : width = " + (widthValue == null ? 0 : widthValue.intValue()) + ", height = " + (heightValue == null ? 0 : heightValue.intValue()));
        return new int[]{widthValue == null ? 0 : widthValue.intValue(), heightValue == null ? 0 : heightValue.intValue()};
    }

    public static int[] getImageSize(ExifInterface exif) {
        if (exif == null) {
            return new int[]{0, 0};
        }
        Integer widthValue = exif.getTagIntValue(ExifInterface.TAG_IMAGE_WIDTH);
        Integer heightValue = exif.getTagIntValue(ExifInterface.TAG_IMAGE_LENGTH);
        CamLog.m3d(CameraConstants.TAG, "getImageSize : width = " + (widthValue == null ? 0 : widthValue.intValue()) + ", height = " + (heightValue == null ? 0 : heightValue.intValue()));
        return new int[]{widthValue == null ? 0 : widthValue.intValue(), heightValue == null ? 0 : heightValue.intValue()};
    }

    public static void setSceneCaptureType(ExifInterface exif, short sceneCaptureType) {
        if (sceneCaptureType != (short) 0) {
            exif.setTag(exif.buildTag(ExifInterface.TAG_SCENE_CAPTURE_TYPE, Short.valueOf(sceneCaptureType)));
        }
    }
}
