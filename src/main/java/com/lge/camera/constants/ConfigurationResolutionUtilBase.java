package com.lge.camera.constants;

import android.content.Context;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;

public class ConfigurationResolutionUtilBase extends ConfigurationUtilBase {
    public static final String CAMERA_PICTURESIZE_BINNING_REAR_SUPPORTED = "camera_binning_pictureSize_rear_supported";
    public static final String CAMERA_PICTURESIZE_BINNING_SUB_REAR_SUPPORTED = "camera_binning_pictureSize_sub_rear_supported";
    public static final String CAMERA_PICTURESIZE_FRONT_DEFAULT = "camera_pictureSize_front_default";
    public static final String CAMERA_PICTURESIZE_FRONT_SUPPORTED = "camera_pictureSize_front_supported";
    public static final String CAMERA_PICTURESIZE_REAR_DEFAULT = "camera_pictureSize_rear_default";
    public static final String CAMERA_PICTURESIZE_REAR_SUPPORTED = "camera_pictureSize_rear_supported";
    public static final String CAMERA_PICTURESIZE_SUB_FRONT_DEFAULT = "camera_pictureSize_sub_front_default";
    public static final String CAMERA_PICTURESIZE_SUB_FRONT_SUPPORTED = "camera_pictureSize_sub_front_supported";
    public static final String CAMERA_PICTURESIZE_SUB_REAR_DEFAULT = "camera_pictureSize_sub_rear_default";
    public static final String CAMERA_PICTURESIZE_SUB_REAR_SUPPORTED = "camera_pictureSize_sub_rear_supported";
    public static final String CAMERA_PREVIEWSIZEONSCREEN_FRONT_SUPPORTED = "camera_previewSizeOnScreen_front_supported";
    public static final String CAMERA_PREVIEWSIZEONSCREEN_REAR_SUPPORTED = "camera_previewSizeOnScreen_rear_supported";
    public static final String CAMERA_PREVIEWSIZEONSCREEN_SUB_FRONT_SUPPORTED = "camera_previewSizeOnScreen_sub_front_supported";
    public static final String CAMERA_PREVIEWSIZEONSCREEN_SUB_REAR_SUPPORTED = "camera_previewSizeOnScreen_sub_rear_supported";
    public static final String CAMERA_PREVIEWSIZE_FRONT_SUPPORTED = "camera_previewSize_front_supported";
    public static final String CAMERA_PREVIEWSIZE_REAR_SUPPORTED = "camera_previewSize_rear_supported";
    public static final String CAMERA_PREVIEWSIZE_SUB_FRONT_SUPPORTED = "camera_previewSize_sub_front_supported";
    public static final String CAMERA_PREVIEWSIZE_SUB_REAR_SUPPORTED = "camera_previewSize_sub_rear_supported";
    public static final String VIDEO_PREVIEWSIZEONSCREEN_FRONT_SUPPORTED = "video_previewSizeOnScreen_front_supported";
    public static final String VIDEO_PREVIEWSIZEONSCREEN_REAR_SUPPORTED = "video_previewSizeOnScreen_rear_supported";
    public static final String VIDEO_PREVIEWSIZEONSCREEN_SUB_FRONT_SUPPORTED = "video_previewSizeOnScreen_sub_front_supported";
    public static final String VIDEO_PREVIEWSIZEONSCREEN_SUB_REAR_SUPPORTED = "video_previewSizeOnScreen_sub_rear_supported";
    public static final String VIDEO_PREVIEWSIZE_FRONT_SUPPORTED = "video_previewSize_front_supported";
    public static final String VIDEO_PREVIEWSIZE_REAR_SUPPORTED = "video_previewSize_rear_supported";
    public static final String VIDEO_PREVIEWSIZE_SUB_FRONT_SUPPORTED = "video_previewSize_sub_front_supported";
    public static final String VIDEO_PREVIEWSIZE_SUB_REAR_SUPPORTED = "video_previewSize_sub_rear_supported";
    public static final String VIDEO_SIZE_FRONT_DEFAULT = "video_size_front_default";
    public static final String VIDEO_SIZE_FRONT_SUPPORTED = "video_size_front_supported";
    public static final String VIDEO_SIZE_REAR_DEFAULT = "video_size_rear_default";
    public static final String VIDEO_SIZE_REAR_SUPPORTED = "video_size_rear_supported";
    public static final String VIDEO_SIZE_SUB_FRONT_DEFAULT = "video_size_sub_front_default";
    public static final String VIDEO_SIZE_SUB_FRONT_SUPPORTED = "video_size_sub_front_supported";
    public static final String VIDEO_SIZE_SUB_REAR_DEFAULT = "video_size_sub_rear_default";
    public static final String VIDEO_SIZE_SUB_REAR_SUPPORTED = "video_size_sub_rear_supported";
    public static String[] sCAMERA_PICTURESIZE_BINNING_REAR_SUPPORTED_ITEMS;
    public static String[] sCAMERA_PICTURESIZE_BINNING_SUB_REAR_SUPPORTED_ITEMS;
    public static String[] sCAMERA_PICTURESIZE_FRONT_DEFAULT_ITEMS;
    public static String[] sCAMERA_PICTURESIZE_FRONT_SUPPORTED_ITEMS;
    public static String[] sCAMERA_PICTURESIZE_REAR_DEFAULT_ITEMS;
    public static String[] sCAMERA_PICTURESIZE_REAR_SUPPORTED_ITEMS;
    public static String[] sCAMERA_PICTURESIZE_SUB_FRONT_DEFAULT_ITEMS;
    public static String[] sCAMERA_PICTURESIZE_SUB_FRONT_SUPPORTED_ITEMS;
    public static String[] sCAMERA_PICTURESIZE_SUB_REAR_DEFAULT_ITEMS;
    public static String[] sCAMERA_PICTURESIZE_SUB_REAR_SUPPORTED_ITEMS;
    public static String[] sCAMERA_PREVIEWSIZEONSCREEN_FRONT_SUPPORTED_ITEMS;
    public static String[] sCAMERA_PREVIEWSIZEONSCREEN_REAR_SUPPORTED_ITEMS;
    public static String[] sCAMERA_PREVIEWSIZEONSCREEN_SUB_FRONT_SUPPORTED_ITEMS;
    public static String[] sCAMERA_PREVIEWSIZEONSCREEN_SUB_REAR_SUPPORTED_ITEMS;
    public static String[] sCAMERA_PREVIEWSIZE_FRONT_SUPPORTED_ITEMS;
    public static String[] sCAMERA_PREVIEWSIZE_REAR_SUPPORTED_ITEMS;
    public static String[] sCAMERA_PREVIEWSIZE_SUB_FRONT_SUPPORTED_ITEMS;
    public static String[] sCAMERA_PREVIEWSIZE_SUB_REAR_SUPPORTED_ITEMS;
    private static final float[] sFrontDensity = new float[]{2.0f, 2.0f, 1.5f};
    protected static boolean sIsDpConfigFormat = false;
    private static final float[] sRearDensity = new float[]{3.0f, 3.0f, 1.5f};
    private static final float[] sScreenDensity = new float[]{4.0f, 3.0f, 2.0f};
    public static String[] sVIDEO_PREVIEWSIZEONSCREEN_FRONT_SUPPORTED_ITEMS;
    public static String[] sVIDEO_PREVIEWSIZEONSCREEN_REAR_SUPPORTED_ITEMS;
    public static String[] sVIDEO_PREVIEWSIZEONSCREEN_SUB_FRONT_SUPPORTED_ITEMS;
    public static String[] sVIDEO_PREVIEWSIZEONSCREEN_SUB_REAR_SUPPORTED_ITEMS;
    public static String[] sVIDEO_PREVIEWSIZE_FRONT_SUPPORTED_ITEMS;
    public static String[] sVIDEO_PREVIEWSIZE_REAR_SUPPORTED_ITEMS;
    public static String[] sVIDEO_PREVIEWSIZE_SUB_FRONT_SUPPORTED_ITEMS;
    public static String[] sVIDEO_PREVIEWSIZE_SUB_REAR_SUPPORTED_ITEMS;
    public static String[] sVIDEO_SIZE_FRONT_DEFAULT_ITEMS;
    public static String[] sVIDEO_SIZE_FRONT_SUPPORTED_ITEMS;
    public static String[] sVIDEO_SIZE_REAR_DEFAULT_ITEMS;
    public static String[] sVIDEO_SIZE_REAR_SUPPORTED_ITEMS;
    public static String[] sVIDEO_SIZE_SUB_FRONT_DEFAULT_ITEMS;
    public static String[] sVIDEO_SIZE_SUB_FRONT_SUPPORTED_ITEMS;
    public static String[] sVIDEO_SIZE_SUB_REAR_DEFAULT_ITEMS;
    public static String[] sVIDEO_SIZE_SUB_REAR_SUPPORTED_ITEMS;

    public static String[] getDpArrayToPxArray(Context context, String[] dpArray, boolean isRear, boolean isPreviewSize) {
        if (dpArray == null || dpArray[0] == null || !dpArray[0].contains("dp")) {
            return dpArray;
        }
        String[] pxArray = new String[dpArray.length];
        int index = getScreenResolutionIndex(context);
        float density = isPreviewSize ? isRear ? sRearDensity[index] : sFrontDensity[index] : sScreenDensity[index];
        for (int i = 0; i < dpArray.length; i++) {
            if (dpArray[i].contains("dp")) {
                dpArray[i] = dpArray[i].replace("dp", "");
                String[] split = dpArray[i].split("x");
                int[] size = new int[split.length];
                size[0] = (int) (((float) Integer.parseInt(split[0])) * density);
                size[1] = (int) (((float) Integer.parseInt(split[1])) * density);
                pxArray[i] = size[0] + "x" + size[1];
            } else {
                pxArray[i] = dpArray[i];
            }
        }
        return pxArray;
    }

    public static int getScreenResolutionIndex(Context context) {
        switch (Utils.getLCDsize(context, true)[1]) {
            case CameraConstantsEx.HD_SCREEN_RESOLUTION /*720*/:
                return 2;
            case CameraConstantsEx.FHD_SCREEN_RESOLUTION /*1080*/:
                return 1;
            case CameraConstantsEx.QHD_SCREEN_RESOLUTION /*1440*/:
                return 0;
            default:
                return -1;
        }
    }

    public static boolean isScreenResolutionChanged(Context c) {
        boolean z = true;
        int savedResolution = SharedPreferenceUtil.getScreenResolution(c);
        int[] lcdSize = Utils.getLCDsize(c, true);
        if (lcdSize == null) {
            return false;
        }
        if (savedResolution == lcdSize[1]) {
            z = false;
        }
        return z;
    }
}
