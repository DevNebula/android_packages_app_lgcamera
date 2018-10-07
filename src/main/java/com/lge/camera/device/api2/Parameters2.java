package com.lge.camera.device.api2;

import android.graphics.Rect;
import android.hardware.Camera.Area;
import android.hardware.camera2.params.RggbChannelVector;
import android.text.TextUtils.SimpleStringSplitter;
import android.text.TextUtils.StringSplitter;
import android.util.Size;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamUtils;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Parameters2 implements Cloneable, CameraParameters {
    private static final String FALSE = "false";
    public static final String PIXEL_FORMAT_JPEG = "jpeg";
    public static final String PIXEL_FORMAT_RGB565 = "rgb565";
    public static final String PIXEL_FORMAT_YUV420P = "yuv420p";
    public static final String PIXEL_FORMAT_YUV420SP = "yuv420sp";
    public static final String PIXEL_FORMAT_YUV422I = "yuv422i-yuyv";
    public static final String PIXEL_FORMAT_YUV422SP = "yuv422sp";
    public static final String SUPPORTED_VALUES_SUFFIX = "-values";
    private static final String TRUE = "true";
    private HashMap<String, String> mMap = new HashMap(64);

    public Parameters2(HashMap<String, String> map) {
        this.mMap = map;
    }

    public void setMap(HashMap<String, String> map) {
        this.mMap = map;
    }

    public HashMap<String, String> getMap() {
        return this.mMap;
    }

    protected Parameters2 clone() {
        try {
            Parameters2 retParam = (Parameters2) super.clone();
            retParam.setMap((HashMap) retParam.getMap().clone());
            return retParam;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void unflatten(String flattened) {
        this.mMap.clear();
        StringSplitter<String> splitter = new SimpleStringSplitter(';');
        splitter.setString(flattened);
        for (String kv : splitter) {
            int pos = kv.indexOf(61);
            if (pos != -1) {
                this.mMap.put(kv.substring(0, pos), kv.substring(pos + 1));
            }
        }
    }

    public String flatten() {
        StringBuilder flattened = new StringBuilder(128);
        for (String k : this.mMap.keySet()) {
            flattened.append(k);
            flattened.append("=");
            flattened.append((String) this.mMap.get(k));
            flattened.append(";");
        }
        flattened.deleteCharAt(flattened.length() - 1);
        return flattened.toString();
    }

    public void remove(String key) {
        this.mMap.remove(key);
    }

    public void removeAll() {
        this.mMap.clear();
    }

    public void set(String key, String value) {
        if (key.indexOf(61) != -1 || key.indexOf(59) != -1 || key.indexOf(0) != -1) {
            CamLog.m5e(CameraConstants.TAG, "Key \"" + key + "\" contains invalid character (= or ; or \\0)");
        } else if (value.indexOf(61) == -1 && value.indexOf(59) == -1 && value.indexOf(0) == -1) {
            this.mMap.put(key, value);
        } else {
            CamLog.m5e(CameraConstants.TAG, "Value \"" + value + "\" contains invalid character (= or ; or \\0)");
        }
    }

    public void set(String key, int value) {
        this.mMap.put(key, Integer.toString(value));
    }

    public void set(String key, float value) {
        this.mMap.put(key, Float.toString(value));
    }

    private void set(String key, List<Area> areas) {
        if (areas == null) {
            set(key, "(0,0,0,0,0)");
            return;
        }
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < areas.size(); i++) {
            Area area = (Area) areas.get(i);
            Rect rect = area.rect;
            buffer.append('(');
            buffer.append(rect.left);
            buffer.append(',');
            buffer.append(rect.top);
            buffer.append(',');
            buffer.append(rect.right);
            buffer.append(',');
            buffer.append(rect.bottom);
            buffer.append(',');
            buffer.append(area.weight);
            buffer.append(')');
            if (i != areas.size() - 1) {
                buffer.append(',');
            }
        }
        set(key, buffer.toString());
    }

    public String get(String key) {
        return (String) this.mMap.get(key);
    }

    public int getInt(String key) {
        return Integer.parseInt((String) this.mMap.get(key));
    }

    public void setPreviewSize(int width, int height) {
        set(ParamConstants.KEY_PREVIEW_SIZE, Integer.toString(width) + "x" + Integer.toString(height));
    }

    public Size getPreviewSize() {
        return strToSize(get(ParamConstants.KEY_PREVIEW_SIZE));
    }

    public Size getPictureSize() {
        return strToSize(get("picture-size"));
    }

    public Size getVideoSize() {
        String videoSizeStr = get(ParamConstants.KEY_VIDEO_SIZE);
        if (videoSizeStr == null || videoSizeStr.length() == 0) {
            return null;
        }
        return strToSize(videoSizeStr.split("@")[0]);
    }

    public Size getLiveSanpShotSize() {
        if ("false".equals(get(ParamConstants.KEY_VIDEO_SNAPSHOT_SUPPORTED)) || "off".equals(get(ParamConstants.KEY_APP_VIDEO_SNAPSHOT))) {
            CamLog.m7i(CameraConstants.TAG, "LiveSnapShot not support mode");
            return null;
        }
        int[] fps = new int[2];
        getPreviewFpsRange(fps);
        String videoSize = get(ParamConstants.KEY_VIDEO_SIZE).split("@")[0];
        if (ParamConstants.VIDEO_3840_BY_2160.equalsIgnoreCase(videoSize) || fps[0] >= 120) {
            return null;
        }
        if (fps[0] > 30) {
            return strToSize(videoSize);
        }
        return getPictureSize();
    }

    public List<Size> getSupportedPictureSizes() {
        return splitSize(get("picture-size-values"));
    }

    public void setSupportedPictureSizes(String pictureSizes) {
        if (pictureSizes != null) {
            set("picture-size-values", pictureSizes);
        }
    }

    public void setSupportedVideoSnapSizes(String pictureSizes) {
        if (pictureSizes != null) {
            set(ParamConstants.KEY_VIDEO_SNAPSHOT_SIZE_SUPPORTED, pictureSizes);
        }
    }

    public void setRotation(int rotation) {
        if (rotation == 0 || rotation == 90 || rotation == 180 || rotation == 270) {
            set(ParamConstants.KEY_ROTATION, Integer.toString(rotation));
            return;
        }
        throw new IllegalArgumentException("Invalid rotation=" + rotation);
    }

    public void setGpsLatitude(double latitude) {
        set(ParamConstants.KEY_GPS_LATITUDE, Double.toString(latitude));
    }

    public void setGpsLongitude(double longitude) {
        set(ParamConstants.KEY_GPS_LONGITUDE, Double.toString(longitude));
    }

    public void setGpsAltitude(double altitude) {
        set(ParamConstants.KEY_GPS_ALTITUDE, Double.toString(altitude));
    }

    public void setGpsTimestamp(long timestamp) {
        set(ParamConstants.KEY_GPS_TIMESTAMP, Long.toString(timestamp));
    }

    public void setGpsProcessingMethod(String processing_method) {
        set(ParamConstants.KEY_GPS_PROCESSING_METHOD, processing_method);
    }

    public void removeGpsData() {
        remove(ParamConstants.KEY_GPS_LATITUDE);
        remove(ParamConstants.KEY_GPS_LONGITUDE);
        remove(ParamConstants.KEY_GPS_ALTITUDE);
        remove(ParamConstants.KEY_GPS_TIMESTAMP);
        remove(ParamConstants.KEY_GPS_PROCESSING_METHOD);
    }

    public String getWhiteBalance() {
        return get(ParamConstants.KEY_WHITE_BALANCE);
    }

    public void setWhiteBalance(String value) {
        if (!ParamUtils.isSame(value, get(ParamConstants.KEY_WHITE_BALANCE))) {
            set(ParamConstants.KEY_WHITE_BALANCE, value);
            set(ParamConstants.KEY_AUTO_WHITEBALANCE_LOCK, "false");
        }
    }

    public List<String> getSupportedWhiteBalance() {
        return ParamUtils.split(get("whitebalance-values"));
    }

    public String getAntibanding() {
        return get(ParamConstants.KEY_ANTIBANDING);
    }

    public void setAntibanding(String antibanding) {
        set(ParamConstants.KEY_ANTIBANDING, antibanding);
    }

    public List<String> getSupportedAntibanding() {
        return ParamUtils.split(get("antibanding-values"));
    }

    public String getSceneMode() {
        return get(ParamConstants.KEY_SCENE_MODE);
    }

    public void setSceneMode(String value) {
        set(ParamConstants.KEY_SCENE_MODE, value);
    }

    public List<String> getSupportedSceneModes() {
        return ParamUtils.split(get("scene-mode-values"));
    }

    public String getFlashMode() {
        return get("flash-mode");
    }

    public void setFlashMode(String value) {
        set("flash-mode", value);
    }

    public List<String> getSupportedFlashModes() {
        return ParamUtils.split(get("flash-mode-values"));
    }

    public String getFocusMode() {
        return get("focus-mode");
    }

    public void setFocusMode(String value) {
        set("focus-mode", value);
    }

    public List<String> getSupportedFocusModes() {
        return ParamUtils.split(get("focus-mode-values"));
    }

    public float getFocalLength() {
        return Float.parseFloat(get(ParamConstants.KEY_FOCAL_LENGTH));
    }

    public float getHorizontalViewAngle() {
        return Float.parseFloat(get(ParamConstants.KEY_HORIZONTAL_VIEW_ANGLE));
    }

    public float getVerticalViewAngle() {
        return Float.parseFloat(get(ParamConstants.KEY_VERTICAL_VIEW_ANGLE));
    }

    public void setManualFocusStep(String value) {
        set(ParamConstants.MANUAL_FOCUS_STEP, value);
    }

    public int getExposureCompensation() {
        return getInt(ParamConstants.KEY_EXPOSURE_COMPENSATION, 0);
    }

    public void setExposureCompensation(int value) {
        set(ParamConstants.KEY_EXPOSURE_COMPENSATION, value);
    }

    public int getContrast() {
        return getInt(ParamConstants.KEY_CONTRAST, -1);
    }

    public int getFilterStrength() {
        return getInt(ParamConstants.KEY_FILM_STRENGTH, -1);
    }

    public int getLightFrameTime() {
        return getInt(ParamConstants.KEY_LIGHTFRMAE_TIME, 0);
    }

    public void setContrast(int value) {
        set(ParamConstants.KEY_CONTRAST, value);
    }

    public int getMaxExposureCompensation() {
        return getInt(ParamConstants.KEY_MAX_EXPOSURE_COMPENSATION, 0);
    }

    public int getMinExposureCompensation() {
        return getInt(ParamConstants.KEY_MIN_EXPOSURE_COMPENSATION, 0);
    }

    public void setAutoExposureLock(boolean toggle) {
        set(ParamConstants.KEY_AUTO_EXPOSURE_LOCK, toggle ? "true" : "false");
    }

    public boolean getAutoExposureLock() {
        return "true".equals(get(ParamConstants.KEY_AUTO_EXPOSURE_LOCK));
    }

    public boolean isAutoExposureLockSupported() {
        return "true".equals(get(ParamConstants.KEY_AUTO_EXPOSURE_LOCK_SUPPORTED));
    }

    public void setAutoWhiteBalanceLock(boolean toggle) {
        set(ParamConstants.KEY_AUTO_WHITEBALANCE_LOCK, toggle ? "true" : "false");
    }

    public boolean getAutoWhiteBalanceLock() {
        return "true".equals(get(ParamConstants.KEY_AUTO_WHITEBALANCE_LOCK));
    }

    public boolean isAutoWhiteBalanceLockSupported() {
        return "true".equals(get(ParamConstants.KEY_AUTO_WHITEBALANCE_LOCK_SUPPORTED));
    }

    public int getZoom() {
        return getInt("zoom", 0);
    }

    public void setZoom(int value) {
        set("zoom", value);
    }

    public boolean isZoomSupported() {
        return "true".equals(get(ParamConstants.KEY_ZOOM_SUPPORTED));
    }

    public int getMaxZoom() {
        return getInt(ParamConstants.KEY_MAX_ZOOM, 0);
    }

    public List<Integer> getZoomRatios() {
        return ParamUtils.splitInt(get(ParamConstants.KEY_ZOOM_RATIOS));
    }

    public int getMaxNumFocusAreas() {
        return getInt(ParamConstants.KEY_MAX_NUM_FOCUS_AREAS, 0);
    }

    public List<Area> getFocusAreas() {
        return splitArea(get(ParamConstants.KEY_FOCUS_AREAS));
    }

    public void setFocusAreas(List<Area> focusAreas) {
        set(ParamConstants.KEY_FOCUS_AREAS, (List) focusAreas);
    }

    public int getMaxNumMeteringAreas() {
        return getInt(ParamConstants.KEY_MAX_NUM_METERING_AREAS, 0);
    }

    public List<Area> getMeteringAreas() {
        return splitArea(get(ParamConstants.KEY_METERING_AREAS));
    }

    public void setMeteringAreas(List<Area> meteringAreas) {
        set(ParamConstants.KEY_METERING_AREAS, (List) meteringAreas);
    }

    public int getMaxNumDetectedFaces() {
        return getInt(ParamConstants.KEY_MAX_NUM_DETECTED_FACES_HW, 0);
    }

    public void setRecordingHint(boolean hint) {
        set("recording-hint", hint ? "true" : "false");
    }

    public int isBeautySupported() {
        return "on".equals(get("beautyshot")) ? 1 : 0;
    }

    public int isOutfocusSupported() {
        return "on".equals(get(ParamConstants.KEY_OUTFOCUS)) ? 1 : 0;
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt((String) this.mMap.get(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private ArrayList<Size> splitSize(String str) {
        if (str == null) {
            return null;
        }
        StringSplitter<String> splitter = new SimpleStringSplitter(',');
        splitter.setString(str);
        ArrayList<Size> sizeList = new ArrayList();
        for (String s : splitter) {
            Size size = strToSize(s);
            if (size != null) {
                sizeList.add(size);
            }
        }
        if (sizeList.size() == 0) {
            return null;
        }
        return sizeList;
    }

    private Size strToSize(String str) {
        if (str == null) {
            return null;
        }
        int pos = str.indexOf(120);
        if (pos != -1) {
            return new Size(Integer.parseInt(str.substring(0, pos)), Integer.parseInt(str.substring(pos + 1)));
        }
        CamLog.m5e(CameraConstants.TAG, "Invalid size parameter string=" + str);
        return null;
    }

    private ArrayList<Area> splitArea(String str) {
        if (str != null && str.charAt(0) == '(' && str.charAt(str.length() - 1) == ')') {
            ArrayList<Area> result = new ArrayList();
            int fromIndex = 1;
            int[] array = new int[5];
            int endIndex;
            do {
                endIndex = str.indexOf("),(", fromIndex);
                if (endIndex == -1) {
                    endIndex = str.length() - 1;
                }
                ParamUtils.splitInt(str.substring(fromIndex, endIndex), array);
                result.add(new Area(new Rect(array[0], array[1], array[2], array[3]), array[4]));
                fromIndex = endIndex + 3;
            } while (endIndex != str.length() - 1);
            if (result.size() == 0) {
                return null;
            }
            if (result.size() != 1) {
                return result;
            }
            Area area = (Area) result.get(0);
            Rect rect = area.rect;
            if (rect.left == 0 && rect.top == 0 && rect.right == 0 && rect.bottom == 0 && area.weight == 0) {
                return null;
            }
            return result;
        }
        CamLog.m3d(CameraConstants.TAG, "Invalid area string=" + str);
        return null;
    }

    public Object getParameters() {
        return clone();
    }

    public void setParameters(Object parameters) {
    }

    public int getPreviewFormat() {
        return Camera2Util.pixelFormatForCameraFormat(get(ParamConstants.KEY_PREVIEW_FORMAT));
    }

    public void setColorCorrectionGains(RggbChannelVector rggbChannelVector) {
        if (rggbChannelVector != null) {
            set(ParamConstants.KEY_COLOR_CORRECTION_GAINS, getStrValue(rggbChannelVector));
        } else {
            set(ParamConstants.KEY_COLOR_CORRECTION_GAINS, "");
        }
    }

    public String getStrValue(Object paramValue) {
        String value = "";
        if (!(paramValue instanceof RggbChannelVector)) {
            return value;
        }
        StringBuilder buffer = new StringBuilder();
        RggbChannelVector rggbChannelVector = (RggbChannelVector) paramValue;
        buffer.append(rggbChannelVector.getRed());
        buffer.append(',');
        buffer.append(rggbChannelVector.getGreenEven());
        buffer.append(',');
        buffer.append(rggbChannelVector.getGreenOdd());
        buffer.append(',');
        buffer.append(rggbChannelVector.getBlue());
        return buffer.toString();
    }

    public RggbChannelVector getColorCorrectionGains() {
        String gainsStr = get(ParamConstants.KEY_COLOR_CORRECTION_GAINS);
        if (gainsStr != null) {
            String[] gainsStrArr = gainsStr.split(",");
            if (gainsStrArr != null && gainsStrArr.length == 4) {
                return new RggbChannelVector(Float.valueOf(gainsStrArr[0]).floatValue(), Float.valueOf(gainsStrArr[1]).floatValue(), Float.valueOf(gainsStrArr[2]).floatValue(), Float.valueOf(gainsStrArr[3]).floatValue());
            }
        }
        return null;
    }

    public void getPreviewFpsRange(int[] range) {
        if (range == null || range.length != 2) {
            throw new IllegalArgumentException("range must be an array with two elements.");
        }
        String fpsRange = get(ParamConstants.KEY_PREVIEW_FPS_RANGE);
        if (fpsRange != null) {
            StringSplitter<String> splitter = new SimpleStringSplitter(',');
            splitter.setString(fpsRange);
            int index = 0;
            for (String s : splitter) {
                int index2 = index + 1;
                range[index] = Integer.parseInt(s) / 1000;
                index = index2;
            }
        }
    }

    public List<String> getSupportedColorEffects() {
        return null;
    }

    public String getColorEffect() {
        return null;
    }
}
