package com.lge.camera.constants;

import android.os.SystemProperties;
import android.support.p000v4.view.PointerIconCompat;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.managers.ext.SnapMovieInterfaceImpl;
import com.lge.camera.util.CamLog;
import com.lge.media.CamcorderProfileEx;

public final class MultimediaProperties {
    public static final String CAMCORDER_PROFILE_NOTSUPPORT = "NotSupport";
    public static final int FUNCTION_ELAPSED_TICK_TIMER = 1;
    public static final int FUNCTION_ELAPSED_UI_TIMER = 0;
    public static final String IMAGE_MIME_TYPE = "image/jpeg";
    public static final String MEDIA_EJECT = "eject";
    public static final int MEDIA_RECORDER_ERROR_FULL_STORAGE = 300;
    public static final int MEDIA_RECORDER_ERROR_RESOURCE = 2;
    public static final int MEDIA_RECORDER_INFO_PROGRESS_TIME_DURATION = 1003;
    public static final int MEDIA_RECORDER_INFO_PROGRESS_TIME_STATUS = 804;
    public static final int MEDIA_RECORDER_INFO_TOTAL_DURATION = 805;
    public static final int REC_START_DELAY = 0;
    public static final String VIDEO_EXTENSION_3GP = ".3gp";
    public static final String VIDEO_EXTENSION_MP4 = ".mp4";
    public static final String VIDEO_MIME_TYPE = "video/mp4";

    public static int getMinRecordingTime() {
        return 2000;
    }

    public static String getVideoMimeType(String postfix) {
        switch (ModelProperties.getCarrierCode()) {
            case 1:
            case 2:
            case 3:
            case 31:
                return "video/3gpp";
            default:
                if ("AU".equals(SystemProperties.get("ro.build.target_country"))) {
                    return "video/3gpp";
                }
                return VIDEO_MIME_TYPE;
        }
    }

    public static int getVideoEncodingType(int purpose) {
        if ((purpose & 16) != 0) {
            return 5;
        }
        if ((purpose & 4) == 0) {
            return 2;
        }
        switch (ModelProperties.getCarrierCode()) {
            case 6:
            case 10:
                return 2;
            default:
                return 3;
        }
    }

    public static int getMmsAudioEncodingType() {
        ModelProperties.getCarrierCode();
        return 1;
    }

    public static long getMediaRecoderLimitSize() {
        return CameraConstants.MEDIA_RECORDING_MAX_LIMIT;
    }

    public static int getMMSMaxDuration() {
        switch (ModelProperties.getCarrierCode()) {
            case 6:
                return SnapMovieInterfaceImpl.SHOT_TIME_MAX_NO_DAMPER;
            default:
                return -1;
        }
    }

    public static int getProfileQulity(int cameraId, int[] size, boolean isHFR, String modeType, boolean is240fps) {
        if (size == null) {
            return 6;
        }
        if (size[1] == 2160 && CamcorderProfileEx.hasProfile(cameraId, 8)) {
            return 8;
        }
        if ((size[1] == CameraConstantsEx.FHD_SCREEN_RESOLUTION || size[1] == 1088) && CamcorderProfileEx.hasProfile(cameraId, 6)) {
            if (modeType != null && CameraConstants.VIDEO_VIDEO_TIMELAPSE_TYPE.equals(modeType)) {
                return PointerIconCompat.TYPE_CELL;
            }
            if (modeType == null || !CameraConstants.VIDEO_SLOMO_TYPE.equals(modeType)) {
                return 6;
            }
            return 10013;
        } else if (size[1] == CameraConstantsEx.HD_SCREEN_RESOLUTION && CamcorderProfileEx.hasProfile(cameraId, 5)) {
            if (isHFR) {
                if (modeType != null && CameraConstants.VIDEO_SLOMO_TYPE.equals(modeType) && is240fps) {
                    return 10017;
                }
                return 2003;
            } else if (modeType == null || !CameraConstants.VIDEO_VIDEO_TIMELAPSE_TYPE.equals(modeType)) {
                return 5;
            } else {
                return 1005;
            }
        } else if (size[1] == CameraConstants.SHUTTER_ZOOM_SUPPORTED_MIN_ZOOM_LEVEL && CamcorderProfileEx.hasProfile(cameraId, 4)) {
            return 4;
        } else {
            if (size[1] == 240 && CamcorderProfileEx.hasProfile(cameraId, 7)) {
                return 7;
            }
            if (size[1] == 144 && CamcorderProfileEx.hasProfile(cameraId, 2)) {
                return 2;
            }
            return 6;
        }
    }

    public static int getBitrate(int cameraId, int quality) {
        CamcorderProfileEx profile = null;
        try {
            profile = CamcorderProfileEx.get(cameraId, quality);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        if (profile != null) {
            return profile.videoBitRate;
        }
        return 30000000;
    }

    public static boolean isSupportedPauseAndResume() {
        return true;
    }

    public static String getCameraFPSRange(boolean isMainCamera) {
        String[] fpsValueArray;
        String fpsRange = SystemProperties.get(isMainCamera ? ParamConstants.FPS_RANGE_SYSTEM_PROPERTY_CAMERA_REAR : ParamConstants.FPS_RANGE_SYSTEM_PROPERTY_CAMERA_FRONT);
        if (fpsRange == null || "".equals(fpsRange)) {
            fpsValueArray = isMainCamera ? new String[]{"7000", "6000"} : new String[]{"10000", "5000"};
        } else {
            fpsValueArray = fpsRange.split(",");
        }
        return fpsValueArray[0] + ",30000";
    }

    public static double getPictureSizeScale(int cameraID, String shotMode, int projectCode, String pictureSize) {
        if (shotMode.equals(CameraConstants.MODE_PANORAMA)) {
            return 1.111111d;
        }
        return 0.8d;
    }

    public static String getManualVideoSupportedListStr(int cameraId, int quality, float ratio) {
        try {
            String supportedList = CamcorderProfileEx.getManualSupportedList(cameraId, quality, ratio);
            if (!CAMCORDER_PROFILE_NOTSUPPORT.equals(supportedList)) {
                return supportedList;
            }
            throw new Exception(CAMCORDER_PROFILE_NOTSUPPORT);
        } catch (Exception e) {
            CamLog.m7i(CameraConstants.TAG, "[camcorder profile not support] cameraId : " + cameraId + ", quality : " + quality + ", ratio : " + ratio);
            return readManualVideoList(cameraId, quality, ratio);
        }
    }

    private static String readManualVideoList(int cameraId, int quality, float ratio) {
        if (quality == 8) {
            if (Float.compare(ratio, 1.7777778f) == 0) {
                return "1920x1080:3840x2160:1,2,24,30:64000000,48000000,30000000";
            }
            return CAMCORDER_PROFILE_NOTSUPPORT;
        } else if (quality == 6) {
            if (Float.compare(ratio, 1.7777778f) == 0) {
                return "1920x1080:1920x1080:1,2,24,30,60:24000000,17000000,10000000:36000000,24000000,12000000";
            }
            return "1920x1080:2560x1080:1,2,24,30,60:24000000,17000000,10000000:36000000,24000000,12000000";
        } else if (quality != 5) {
            return null;
        } else {
            if (Float.compare(ratio, 1.7777778f) == 0) {
                return "1280x720:1280x720:1,2,24,30,60,120:36000000,24000000,12000000:18000000,12000000,6000000:36000000,24000000,12000000:48000000,36000000,24000000";
            }
            return "1280x720:1680x720:1,2,24,30,60:18000000,12000000,6000000:24000000,17000000,10000000";
        }
    }
}
