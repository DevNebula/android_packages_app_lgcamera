package com.lge.camera.device;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.text.TextUtils.SimpleStringSplitter;
import android.text.TextUtils.StringSplitter;
import android.util.Log;
import android.util.TypedValue;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.List;

public class CameraDeviceUtils {
    private static final int DEVICE_COMPONENT_NOT_SUPPORTED = 0;
    private static final int DEVICE_COMPONENT_SUPPORTED = 1;
    private static final int DEVICE_DETECTED_SUPPORTED = 1;
    private static final int DEVICE_NOT_DETECTED_SUPPORTED = 0;
    public static final int ORIENTATION_HYSTERESIS = 5;
    public static final String RECORDING_HINT = "recording-hint";
    public static int sCameraModuleType = -1;
    private static AlertDialog sErrorDialog = null;
    public static int sNumbersOfCamera = -1;

    public static boolean isSupported(String value, List<String> supported) {
        return supported != null && supported.indexOf(value) >= 0;
    }

    public static boolean isAutoExposureLockSupported(CameraParameters params) {
        return "true".equals(params.get(ParamConstants.KEY_AUTO_EXPOSURE_LOCK_SUPPORTED));
    }

    public static boolean isAutoWhiteBalanceLockSupported(CameraParameters params) {
        return "true".equals(params.get(ParamConstants.KEY_AUTO_WHITEBALANCE_LOCK_SUPPORTED));
    }

    public static boolean isVideoSnapshotSupported(CameraParameters params) {
        return "true".equals(params.get(ParamConstants.KEY_VIDEO_SNAPSHOT_SUPPORTED));
    }

    public static boolean isVideoStabilizationSupported(CameraParameters params) {
        return "true".equals(params.get(ParamConstants.KEY_VIDEO_STABILIZATION_SUPPORTED));
    }

    public static boolean[] isVideoHDRSupported(CameraParameters params) {
        boolean[] hdrSuupported = null;
        String videoHDRSupported = params.get(ParamConstants.KEY_VIDEO_HDR_SUPPORTED);
        if (videoHDRSupported != null) {
            String[] parsedValue = videoHDRSupported.split(",");
            if (parsedValue != null) {
                int cnt = parsedValue.length;
                if (cnt > 0) {
                    hdrSuupported = new boolean[cnt];
                    for (int i = 0; i < cnt; i++) {
                        hdrSuupported[i] = "1".equals(parsedValue[i]);
                    }
                }
            }
        }
        return hdrSuupported;
    }

    public static boolean isMeteringAreaSupported(CameraParameters params) {
        return params.getMaxNumMeteringAreas() > 0;
    }

    public static boolean isFocusAreaSupported(CameraParameters params) {
        return params.getMaxNumFocusAreas() > 0 && isSupported("auto", params.getSupportedFocusModes());
    }

    public static boolean isWhiteBalanceSupported(CameraParameters params) {
        List<String> supported = params.getSupportedWhiteBalance();
        if (supported != null && supported.size() > 0 && supported.contains("auto")) {
            return true;
        }
        return false;
    }

    public static boolean isZoomSupported(CameraParameters params) {
        return params.isZoomSupported();
    }

    public static int getMaxZoom(CameraParameters params) {
        return params.getMaxZoom();
    }

    public static boolean isFocusPeakingSupported(CameraParameters params) {
        String value = params.get(ParamConstants.KEY_FOCUS_PEAKING_SUPPORTED);
        CamLog.m3d(CameraConstants.TAG, "value = " + value);
        if ("true".equals(value)) {
            return true;
        }
        return false;
    }

    public static boolean isFocusTrackingSupported(CameraParameters params) {
        String value = params.get(ParamConstants.KEY_FOCUS_MODE_OBJECT_TRACKING_AREA_SUPPORTED);
        CamLog.m3d(CameraConstants.TAG, "value = " + value);
        if ("true".equals(value)) {
            return true;
        }
        return false;
    }

    public static boolean isFaceDetectionSupported(CameraParameters params) {
        int getMaxNumDetectedFaces = params.getMaxNumDetectedFaces();
        CamLog.m3d(CameraConstants.TAG, "[HAL FACE] getMaxNumDetectedFaces : " + getMaxNumDetectedFaces);
        return getMaxNumDetectedFaces > 0;
    }

    public static boolean isFingerDetectionSupported(CameraParameters params) {
        String value = params.get(ParamConstants.KEY_FINGER_DETECTION_SUPPORTED);
        CamLog.m3d(CameraConstants.TAG, "finger-detection-supported = " + value);
        if ("true".equals(value)) {
            return true;
        }
        return false;
    }

    public static boolean isFrontCameraSupported() {
        if (sNumbersOfCamera == -1) {
            sNumbersOfCamera = Camera.getNumberOfCameras();
            CamLog.m3d(CameraConstants.TAG, "numberOfCamera : " + sNumbersOfCamera);
        }
        if (sNumbersOfCamera > 1) {
            return true;
        }
        return false;
    }

    public static boolean isManualAntiBandingSupported(CameraParameters params) {
        List<String> supported = params.getSupportedAntibanding();
        if (supported == null || supported.size() <= 0) {
            return false;
        }
        if ((supported.contains(ParamConstants.ANTIBANDING_50HZ) || supported.contains(ParamConstants.ANTIBANDING_60HZ)) && !supported.contains("auto")) {
            return true;
        }
        return false;
    }

    public static boolean isAFSupported(CameraParameters params) {
        List<String> supported = params.getSupportedFocusModes();
        if (supported == null) {
            return false;
        }
        CamLog.m7i(CameraConstants.TAG, " AF suported " + supported);
        if (supported.size() <= 0 || !supported.contains("auto")) {
            return false;
        }
        return true;
    }

    public static boolean isSnapshotPictureFlipSupported(CameraParameters params) {
        if (ConfigurationUtil.sHAL_SUPPORTED_VALUE == 2) {
            return true;
        }
        if (params == null) {
            return false;
        }
        if (params.get(ParamConstants.KEY_QC_SNAPSHOT_PICTURE_FLIP) == null) {
            return false;
        }
        return true;
    }

    public static boolean isVideoFlipSupported(CameraParameters params) {
        if (ConfigurationUtil.sHAL_SUPPORTED_VALUE == 2) {
            return true;
        }
        if (params == null) {
            return false;
        }
        if (params.get(ParamConstants.KEY_QC_VIDEO_FLIP) == null) {
            return false;
        }
        return true;
    }

    public static boolean isDeviceComponentSupported(CameraParameters params, String key) {
        int value = 0;
        try {
            value = params.getInt(key);
        } catch (NumberFormatException e) {
            Log.w(CameraConstants.TAG, "NumberFormatException", e);
        }
        if (value == 0 || value == 1 || value == 3 || value != 2) {
            return true;
        }
        return false;
    }

    public static int getVideoFlipType(int degree, int cameraId, boolean isEnable, boolean isSnap) {
        CamLog.m7i(CameraConstants.TAG, "degree : " + degree + " / cameraId : " + cameraId + " / isEnable  : " + isEnable + " / isSnap : " + isSnap);
        if (cameraId == 0 || cameraId == 2) {
            return 0;
        }
        int flipType = isEnable ? (degree == 90 || degree == 270) ? isSnap ? 3 : 2 : isSnap ? 0 : 1 : 0;
        return flipType;
    }

    public static boolean isFlashSupported(CameraParameters parameters, String flashMode) {
        List<String> supportedModes = parameters.getSupportedFlashModes();
        boolean supported = false;
        if (supportedModes != null) {
            for (String mode : supportedModes) {
                if (mode.equals(flashMode)) {
                    supported = true;
                }
            }
        }
        if (!supported) {
            CamLog.m11w(CameraConstants.TAG, String.format("Flash mode [%s] not supported.", new Object[]{flashMode}));
        }
        return supported;
    }

    public static boolean isBeautyRecordingSupported(CameraParameters params) {
        if (params != null && "false".equals(params.get(ParamConstants.KEY_BEAUTY_RECORDING_SUPPORTED))) {
            return false;
        }
        return true;
    }

    public static String checkDeviceComponentSupported(CameraParameters params, Activity activity) {
        CamLog.m3d(CameraConstants.TAG, "checkDeviceComponentSupported : start");
        StringBuffer checkDeviceErrorTex = new StringBuffer();
        if (!(checkDeviceErrorTex == null || !AppControlUtil.isStartFromOnCreate() || ModelProperties.isFakeMode() || !ModelProperties.isFakeExif() || ModelProperties.isFakeExifAtnt())) {
            CamLog.m3d(CameraConstants.TAG, "Check Device Component and Show Popup");
            if (!isDeviceComponentSupported(params, ParamConstants.KEY_DEVICE_ACTUATOR_SUPPORTED)) {
                checkDeviceErrorTex.append(activity.getString(C0088R.string.camera_device_error_acturator) + "\n");
            }
            if (!isDeviceComponentSupported(params, ParamConstants.KEY_DEVICE_LASER_SUPPORTED)) {
                checkDeviceErrorTex.append(activity.getString(C0088R.string.camera_device_error_laser_proxy) + "\n");
            }
            if (!isDeviceComponentSupported(params, ParamConstants.KEY_DEVICE_EEPROM_SUPPORTED)) {
                checkDeviceErrorTex.append(activity.getString(C0088R.string.camera_device_error_eeprom) + "\n");
            }
        }
        CamLog.m3d(CameraConstants.TAG, "checkDeviceComponentSupported : end");
        return checkDeviceErrorTex.toString();
    }

    public static int getDisplayRotation(Activity activity) {
        switch (activity.getWindowManager().getDefaultDisplay().getRotation()) {
            case 1:
                return 90;
            case 2:
                return 180;
            case 3:
                return 270;
            default:
                return 0;
        }
    }

    public static int getDisplayOrientation(int degrees, int cameraId) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        if (info.facing != 0) {
            return (360 - ((info.orientation + degrees) % 360)) % 360;
        }
        return ((info.orientation - degrees) + 360) % 360;
    }

    public static int getOrientationHint(int degree, int cameraId) {
        return isRearCamera(cameraId) ? degree % 360 : (360 - degree) % 360;
    }

    public static int getMultiviewOrientationHint(int degree) {
        return degree % 360;
    }

    public static int getJpegOrientation(Activity activity, int orientationDegree, int cameraId) {
        int jpegDegree;
        if (isRearCamera(cameraId)) {
            if (Utils.isLandscapeOrientaionModel(activity)) {
                jpegDegree = (270 - orientationDegree) % 360;
            } else {
                jpegDegree = (360 - orientationDegree) % 360;
            }
        } else if (Utils.isLandscapeOrientaionModel(activity)) {
            jpegDegree = (orientationDegree + 90) % 360;
        } else {
            jpegDegree = (orientationDegree + 180) % 360;
        }
        return getDisplayOrientation(jpegDegree, cameraId);
    }

    public static void showErrorAndFinish(Activity activity, int titleId, int msgId) {
        showErrorAndFinish(activity, titleId, msgId, "");
    }

    public static void showErrorAndFinish(final Activity activity, int titleId, int msgId, String addMsg) {
        if (activity != null && !activity.isFinishing()) {
            CamLog.m11w(CameraConstants.TAG, "showErrorAndFinish");
            OnClickListener buttonListener = new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    activity.finish();
                }
            };
            TypedValue out = new TypedValue();
            activity.getTheme().resolveAttribute(16843605, out, true);
            dismissErrorAndFinish();
            sErrorDialog = new Builder(activity).setCancelable(false).setTitle(activity.getString(titleId)).setMessage(activity.getString(msgId) + addMsg).setPositiveButton(C0088R.string.sp_ok_NORMAL, buttonListener).setIcon(out.resourceId).show();
        }
    }

    public static void dismissErrorAndFinish() {
        try {
            if (sErrorDialog != null) {
                sErrorDialog.dismiss();
                sErrorDialog = null;
            }
        } catch (Exception e) {
            Log.w(CameraConstants.TAG, "dismissErrorAndFinish error");
        }
    }

    public static boolean isLowLuminance(CameraParameters parameters, boolean isForCamera) {
        if (parameters == null) {
            return false;
        }
        String luminanceCondition;
        if (isForCamera) {
            luminanceCondition = parameters.get(ParamConstants.LUMINANCE_CONDITION);
        } else {
            luminanceCondition = parameters.get(ParamConstants.LUMINANCE_CONDITION_FOR_VIDEO);
        }
        CamLog.m3d(CameraConstants.TAG, "Current luminanceCondition = " + luminanceCondition + ", camera mode ? : " + isForCamera);
        return ParamConstants.LUMINANCE_LOW.equals(luminanceCondition);
    }

    public static void setEnable3ALocks(CameraProxy cameraDevice, boolean lockAE, boolean lockAWB) {
        CamLog.m3d(CameraConstants.TAG, "#### setEnable3ALocks : lockAE=" + lockAE + " lockAWB=" + lockAWB);
        if (cameraDevice != null) {
            CameraParameters parameters = cameraDevice.getParameters();
            if (parameters != null) {
                try {
                    if (parameters.isAutoExposureLockSupported()) {
                        parameters.setAutoExposureLock(lockAE);
                    }
                    if (parameters.isAutoWhiteBalanceLockSupported()) {
                        parameters.setAutoWhiteBalanceLock(lockAWB);
                    }
                } catch (RuntimeException e) {
                    CamLog.m6e(CameraConstants.TAG, "RuntimeException-setEnable3ALocks: ", e);
                }
            }
        }
    }

    public static ArrayList<String> paramSplit(String str) {
        if (str == null) {
            return null;
        }
        StringSplitter<String> splitter = new SimpleStringSplitter(',');
        splitter.setString(str);
        ArrayList<String> substrings = new ArrayList();
        for (String s : splitter) {
            substrings.add(s);
        }
        return substrings;
    }

    public static String getPreviewSizeforMMSVideo(Context context, String videoSize) {
        int[] lcdSize = Utils.getLCDsize(context, true);
        String mPreviewSize = videoSize;
        if (ModelProperties.getCarrierCode() == 5) {
            if (CameraConstants.QVGA_RESOLUTION.equals(videoSize) || CameraConstants.QCIF_RESOLUTION.equals(videoSize)) {
                mPreviewSize = CameraConstants.VGA_RESOLUTION;
            } else {
                mPreviewSize = videoSize;
            }
            return mPreviewSize;
        }
        if (lcdSize[0] >= CameraConstants.MENU_TYPE_FULL_SCREEN_GUIDE && lcdSize[1] >= 540) {
            mPreviewSize = CameraConstants.QVGA_RESOLUTION.equals(videoSize) ? CameraConstants.VGA_RESOLUTION : CameraConstants.QCIF_RESOLUTION.equals(videoSize) ? "352x288" : videoSize;
        }
        return mPreviewSize;
    }

    public static void setSnapshotPictureFlip(CameraParameters params, int degree, boolean isEnable) {
        if (!isEnable) {
            params.set(ParamConstants.KEY_QC_SNAPSHOT_PICTURE_FLIP, "off");
        } else if (degree == 90 || degree == 270) {
            params.set(ParamConstants.KEY_QC_SNAPSHOT_PICTURE_FLIP, ParamConstants.FLIP_MODE_V);
        } else {
            params.set(ParamConstants.KEY_QC_SNAPSHOT_PICTURE_FLIP, ParamConstants.FLIP_MODE_H);
        }
    }

    public static void setVideoFlip(CameraParameters params, int degree, boolean isEnable, boolean isSnap) {
        String flipType = "off";
        if (isEnable) {
            flipType = (degree == 90 || degree == 270) ? isSnap ? ParamConstants.FLIP_MODE_VH : ParamConstants.FLIP_MODE_V : isSnap ? "off" : ParamConstants.FLIP_MODE_H;
        }
        CamLog.m3d(CameraConstants.TAG, "flipType =  " + flipType);
        params.set(ParamConstants.KEY_QC_VIDEO_FLIP, flipType);
    }

    public static void setBeautyLevel(CameraParameters params, int level) {
        if (params != null && level >= 0 && level <= 100) {
            params.set(ParamConstants.KEY_BEAUTY_LEVEL, "" + level);
        }
    }

    public static int getBeautyLevel(CameraParameters params) {
        if (params == null) {
            return -1;
        }
        String value = params.get(ParamConstants.KEY_BEAUTY_LEVEL);
        if (value != null) {
            return Integer.valueOf(value).intValue();
        }
        return -1;
    }

    public static void setRelightingLevel(CameraParameters params, int level) {
        if (params != null && level >= 0 && level <= 100) {
            params.set(ParamConstants.KEY_RELIGHTING_LEVEL, "" + level);
        }
    }

    public static int getRelightingLevel(CameraParameters params) {
        if (params == null) {
            return -1;
        }
        String value = params.get(ParamConstants.KEY_RELIGHTING_LEVEL);
        if (value != null) {
            return Integer.valueOf(value).intValue();
        }
        return -1;
    }

    public static boolean isRearCamera(int cameraId) {
        if (cameraId == 0) {
            return true;
        }
        if (ConfigurationUtil.sCameraTypeRear == 1 && cameraId == 2) {
            return true;
        }
        return false;
    }

    public static ArrayList<Integer> getCameraIdArray(int facing) {
        ArrayList<Integer> cameraArr = new ArrayList();
        int numOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info != null && info.facing == facing) {
                cameraArr.add(Integer.valueOf(i));
            }
        }
        return cameraArr;
    }
}
