package com.lge.camera.settings;

import android.content.Context;
import android.content.res.Resources;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationResolutionUtilBase;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.MmsProperties;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.Utils;

public class SizePrefMaker {
    public static final int IMAGE_SIZE_16BY9 = 5;
    public static final int IMAGE_SIZE_18BY9 = 6;
    public static final int IMAGE_SIZE_18_9BY9 = 7;
    public static final int IMAGE_SIZE_1BY1 = 1;
    public static final int IMAGE_SIZE_4BY3 = 2;
    private static final int IMAGE_SIZE_LOW = 0;
    private static final double[] PICTURE_RATIOS = new double[]{0.75d, 1.0d, 0.75d, 0.625d, 0.6d, 0.5625d, 0.5d, 0.47d};
    public static final String[] PICTURE_RATIO_STRINGS = new String[]{ParamConstants.LUMINANCE_LOW, SharedPreferenceUtilBase.DUAL_WINDOW_DEFAULT_INDEX, "4:3", "8:5", "5:3", "16:9", "18:9", "18.9:9"};
    public static final int UNDEFINED_VALUE = -1;

    public static ListPreference makeRearPictureSizePreference(Context context, PreferenceGroup prefGroup) {
        return makePictureSizePreference(context, prefGroup, "picture-size", C0088R.string.photo_size, ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_REAR_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sCAMERA_PREVIEWSIZE_REAR_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sCAMERA_PREVIEWSIZEONSCREEN_REAR_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_REAR_DEFAULT_ITEMS, 0);
    }

    public static ListPreference makeRearPictureSizePreferenceForSub(Context context, PreferenceGroup prefGroup) {
        if (FunctionProperties.getCameraTypeRear() != 1) {
            return null;
        }
        return makePictureSizePreference(context, prefGroup, Setting.KEY_CAMERA_PICTURESIZE_SUB, C0088R.string.photo_size, ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_SUB_REAR_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sCAMERA_PREVIEWSIZE_SUB_REAR_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sCAMERA_PREVIEWSIZEONSCREEN_SUB_REAR_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_SUB_REAR_DEFAULT_ITEMS, 2);
    }

    public static ListPreference makeFrontPictureSizePreference(Context context, PreferenceGroup prefGroup) {
        return makePictureSizePreference(context, prefGroup, "picture-size", C0088R.string.photo_size, ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_FRONT_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sCAMERA_PREVIEWSIZE_FRONT_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sCAMERA_PREVIEWSIZEONSCREEN_FRONT_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_FRONT_DEFAULT_ITEMS, 1);
    }

    public static ListPreference makeFrontPictureSizePreferenceForSub(Context context, PreferenceGroup prefGroup) {
        if (FunctionProperties.getCameraTypeFront() != 1) {
            return null;
        }
        return makePictureSizePreference(context, prefGroup, Setting.KEY_CAMERA_PICTURESIZE_SUB, C0088R.string.photo_size, ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_SUB_FRONT_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sCAMERA_PREVIEWSIZE_SUB_FRONT_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sCAMERA_PREVIEWSIZEONSCREEN_SUB_FRONT_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_SUB_FRONT_DEFAULT_ITEMS, 3);
    }

    public static ListPreference makeRearVideoSizePreference(Context context, PreferenceGroup prefGroup) {
        return makeVideoSizePreference(context, prefGroup, Setting.KEY_VIDEO_RECORDSIZE, C0088R.string.video_resolution, C0088R.array.video_size_entries, C0088R.array.video_size_entryValues, ConfigurationResolutionUtilBase.sVIDEO_SIZE_REAR_SUPPORTED_ITEMS, -1, -1, ConfigurationResolutionUtilBase.sVIDEO_PREVIEWSIZE_REAR_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sVIDEO_PREVIEWSIZEONSCREEN_REAR_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sVIDEO_SIZE_REAR_DEFAULT_ITEMS);
    }

    public static ListPreference makeRearVideoSizePreferenceForSub(Context context, PreferenceGroup prefGroup) {
        return makeVideoSizePreference(context, prefGroup, Setting.KEY_VIDEO_RECORDSIZE_SUB, C0088R.string.video_resolution, C0088R.array.video_size_entries, C0088R.array.video_size_entryValues, ConfigurationResolutionUtilBase.sVIDEO_SIZE_SUB_REAR_SUPPORTED_ITEMS, -1, -1, ConfigurationResolutionUtilBase.sVIDEO_PREVIEWSIZE_SUB_REAR_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sVIDEO_PREVIEWSIZEONSCREEN_SUB_REAR_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sVIDEO_SIZE_SUB_REAR_DEFAULT_ITEMS);
    }

    public static ListPreference makeFrontVideoSizePreference(Context context, PreferenceGroup prefGroup) {
        return makeVideoSizePreference(context, prefGroup, Setting.KEY_VIDEO_RECORDSIZE, C0088R.string.video_resolution, C0088R.array.video_size_entries, C0088R.array.video_size_entryValues, ConfigurationResolutionUtilBase.sVIDEO_SIZE_FRONT_SUPPORTED_ITEMS, -1, -1, ConfigurationResolutionUtilBase.sVIDEO_PREVIEWSIZE_FRONT_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sVIDEO_PREVIEWSIZEONSCREEN_FRONT_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sVIDEO_SIZE_FRONT_DEFAULT_ITEMS);
    }

    public static ListPreference makeFrontVideoSizePreferenceForSub(Context context, PreferenceGroup prefGroup) {
        return makeVideoSizePreference(context, prefGroup, Setting.KEY_VIDEO_RECORDSIZE_SUB, C0088R.string.video_resolution, C0088R.array.video_size_entries, C0088R.array.video_size_entryValues, ConfigurationResolutionUtilBase.sVIDEO_SIZE_SUB_FRONT_SUPPORTED_ITEMS, -1, -1, ConfigurationResolutionUtilBase.sVIDEO_PREVIEWSIZE_SUB_FRONT_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sVIDEO_PREVIEWSIZEONSCREEN_SUB_FRONT_SUPPORTED_ITEMS, ConfigurationResolutionUtilBase.sVIDEO_SIZE_SUB_FRONT_DEFAULT_ITEMS);
    }

    private static ListPreference makeVideoSizePreference(Context context, PreferenceGroup prefGroup, String key, int titleId, int entryId, int entryValueId, String[] sizeSupported, int menuIconId, int settingMenuIconId, String[] previewSizeSupported, String[] screenSizeSupported, String[] defaultValue) {
        if (context == null || prefGroup == null) {
            return null;
        }
        Resources resources = context.getResources();
        String title = resources.getString(titleId);
        String[] entries = resources.getStringArray(entryId);
        String[] entryValues = resources.getStringArray(entryValueId);
        int[] menuIcons = PrefMakerUtil.getIconList(context, menuIconId);
        int[] settingMenuIcons = PrefMakerUtil.getIconList(context, settingMenuIconId);
        if (title == null || entries == null || entryValues == null || defaultValue == null) {
            return null;
        }
        int supportedSize = sizeSupported.length;
        int entrySize = entryValues.length;
        int[] supportedMenuIcons = null;
        int[] supportedSettingMenuIcons = null;
        if (menuIcons != null && menuIcons.length > 0) {
            supportedMenuIcons = new int[supportedSize];
        }
        if (settingMenuIcons != null && settingMenuIcons.length > 0) {
            supportedSettingMenuIcons = new int[supportedSize];
        }
        String[] supportedEntries = new String[supportedSize];
        int i = 0;
        int j = 0;
        while (i < entrySize) {
            if (j < supportedSize && sizeSupported[j].equals(entryValues[i])) {
                if (supportedMenuIcons != null) {
                    supportedMenuIcons[j] = menuIcons[i];
                }
                if (supportedSettingMenuIcons != null) {
                    supportedSettingMenuIcons[j] = settingMenuIcons[i];
                }
                supportedEntries[j] = entries[i];
                String sizeString = sizeSupported[j].toString();
                if (MmsProperties.isAvailableMmsResolution(context.getContentResolver(), sizeString)) {
                    String mmsStr = " (MMS)";
                    supportedEntries[j] = sizeString + " (MMS)";
                } else if (entries[i].contains("High")) {
                    supportedEntries[j] = supportedEntries[j].replace("High", context.getString(C0088R.string.pref_video_quality_entry_high));
                } else if (entries[i].contains("Standard")) {
                    supportedEntries[j] = supportedEntries[j].replace("Standard", context.getString(C0088R.string.sp_resolution_standard));
                } else {
                    String videoSize = sizeSupported[j];
                    if (videoSize != null && videoSize.contains("@")) {
                        videoSize = videoSize.substring(0, videoSize.indexOf("@"));
                    }
                    supportedEntries[j] = supportedEntries[j] + " " + videoSize;
                }
                j++;
            }
            i++;
        }
        ListPreference listPref = new ListPreference(context, prefGroup.getSharedPreferenceName());
        listPref.setKey(key);
        listPref.setTitle(title);
        listPref.setMenuIconResources(supportedMenuIcons);
        listPref.setSettingMenuIconResources(supportedSettingMenuIcons);
        listPref.setEntries(supportedEntries);
        listPref.setEntryValues(sizeSupported);
        listPref.setExtraInfos((CharSequence[]) previewSizeSupported, 1);
        listPref.setExtraInfos((CharSequence[]) screenSizeSupported, 2);
        listPref.setDefaultValue(defaultValue[0]);
        listPref.setPersist(true);
        return listPref;
    }

    private static boolean isSquareVideoSize(String sizeString) {
        int[] size = Utils.sizeStringToArray(sizeString);
        if (Float.compare(((float) size[0]) / ((float) size[1]), 1.0f) == 0) {
            return true;
        }
        return false;
    }

    private static ListPreference makePictureSizePreference(Context context, PreferenceGroup prefGroup, String key, int titleId, String[] sizeSupported, String[] previewSizeSupported, String[] screenSizeSupported, String[] defaultValue, int cameraDeviceType) {
        if (context == null || prefGroup == null) {
            return null;
        }
        String title = context.getResources().getString(titleId);
        if (title == null) {
            return null;
        }
        if (sizeSupported != null) {
            return makePictureSizeFromCameraConfig(context, prefGroup, key, title, sizeSupported, previewSizeSupported, screenSizeSupported, defaultValue, cameraDeviceType);
        }
        return makePictureSizeFromSharedPreference(context, prefGroup, key, title, sizeSupported, previewSizeSupported, screenSizeSupported, defaultValue, cameraDeviceType);
    }

    private static String[] makePictureSizeEntry(Context context, String[] sizeSupported, int supportedSize) {
        String[] supportedEntries = new String[supportedSize];
        for (int i = 0; i < supportedSize; i++) {
            supportedEntries[i] = PICTURE_RATIO_STRINGS[calculateRatio(sizeSupported[i])];
            supportedEntries[i] = supportedEntries[i] + " (" + Utils.getMegaPixelOfPictureSize(sizeSupported[i], i) + ") " + sizeSupported[i];
        }
        return supportedEntries;
    }

    private static ListPreference makePictureSizeFromCameraConfig(Context context, PreferenceGroup prefGroup, String key, String title, String[] sizeSupported, String[] previewSizeSupported, String[] screenSizeSupported, String[] defaultValue, int cameraDeviceType) {
        String[] supportedEntries = makePictureSizeEntry(context, sizeSupported, sizeSupported.length);
        ListPreference listPref = new ListPreference(context, prefGroup.getSharedPreferenceName());
        listPref.setKey(key);
        listPref.setTitle(title);
        listPref.setMenuIconResources(null);
        listPref.setSettingMenuIconResources(null);
        listPref.setEntries(supportedEntries);
        listPref.setEntryValues(sizeSupported);
        listPref.setExtraInfos((CharSequence[]) previewSizeSupported, 1);
        listPref.setExtraInfos((CharSequence[]) screenSizeSupported, 2);
        listPref.setDefaultValue(defaultValue[0]);
        listPref.setPersist(true);
        return listPref;
    }

    private static ListPreference makePictureSizeFromSharedPreference(Context context, PreferenceGroup prefGroup, String key, String title, String[] pictureSizeSupported, String[] previewSizeSupported, String[] screenSizeSupported, String[] defaultValue, int cameraDeviceType) {
        int newIndex;
        int i;
        int i2;
        String prefSizeList = SharedPreferenceUtilBase.getCameraPictureSizeList(context, cameraDeviceType, "");
        if ("".equals(prefSizeList)) {
            pictureSizeSupported = previewSizeSupported;
        } else {
            pictureSizeSupported = prefSizeList.split(",");
        }
        String[] newPictureSizeSupported = pictureSizeSupported;
        CharSequence[] newPreviewSizeSupported = previewSizeSupported;
        CharSequence[] newScreenSizeSupported = screenSizeSupported;
        int pictureSizeSupportedLength = pictureSizeSupported.length;
        int mainPictureSizeSupportedLength = getMainPictureSizeLengthForSubCamera(context, cameraDeviceType, pictureSizeSupported.length);
        if (mainPictureSizeSupportedLength != pictureSizeSupported.length) {
            pictureSizeSupportedLength = mainPictureSizeSupportedLength;
            newPictureSizeSupported = new String[pictureSizeSupportedLength];
            newIndex = 0;
            for (i = 0; i < pictureSizeSupported.length; i++) {
                i2 = newIndex + 1;
                newPictureSizeSupported[newIndex] = pictureSizeSupported[i];
                newIndex = i2 + 1;
                newPictureSizeSupported[i2] = pictureSizeSupported[i];
            }
        }
        CamLog.m7i(CameraConstants.TAG, "[ConfigAuto], picture size list count : " + pictureSizeSupportedLength);
        if (pictureSizeSupportedLength > 5) {
            newPreviewSizeSupported = new String[pictureSizeSupportedLength];
            newScreenSizeSupported = new String[pictureSizeSupportedLength];
            newIndex = 0;
            for (i = 0; i < previewSizeSupported.length; i++) {
                newPreviewSizeSupported[newIndex] = previewSizeSupported[i];
                i2 = newIndex + 1;
                newScreenSizeSupported[newIndex] = screenSizeSupported[i];
                newPreviewSizeSupported[i2] = previewSizeSupported[i];
                newIndex = i2 + 1;
                newScreenSizeSupported[i2] = screenSizeSupported[i];
            }
        }
        if (!"".equals(SharedPreferenceUtilBase.getCameraDefaultSize(context, cameraDeviceType, ""))) {
            defaultValue = new String[]{SharedPreferenceUtilBase.getCameraDefaultSize(context, cameraDeviceType, "")};
        } else if (defaultValue == null) {
            defaultValue = new String[]{newPreviewSizeSupported[0]};
        }
        String[] supportedEntries = makePictureSizeEntry(context, newPictureSizeSupported, pictureSizeSupportedLength);
        ListPreference listPref = new ListPreference(context, prefGroup.getSharedPreferenceName());
        listPref.setKey(key);
        listPref.setTitle(title);
        listPref.setMenuIconResources(null);
        listPref.setSettingMenuIconResources(null);
        listPref.setEntries(supportedEntries);
        listPref.setEntryValues(newPictureSizeSupported);
        listPref.setExtraInfos(newPreviewSizeSupported, 1);
        listPref.setExtraInfos(newScreenSizeSupported, 2);
        listPref.setDefaultValue(defaultValue[0]);
        listPref.setPersist(true);
        return listPref;
    }

    private static int getMainPictureSizeLengthForSubCamera(Context context, int cameraDeviceType, int mainPictureSizeSupportedLength) {
        String mainPrefSizeList = "";
        if (cameraDeviceType == 2) {
            mainPrefSizeList = SharedPreferenceUtilBase.getCameraPictureSizeList(context, 0, "");
        } else if (cameraDeviceType == 3) {
            mainPrefSizeList = SharedPreferenceUtilBase.getCameraPictureSizeList(context, 1, "");
        } else if (cameraDeviceType == 0) {
            mainPrefSizeList = SharedPreferenceUtilBase.getCameraPictureSizeList(context, 2, "");
        } else if (cameraDeviceType == 1) {
            mainPrefSizeList = SharedPreferenceUtilBase.getCameraPictureSizeList(context, 3, "");
        }
        if ("".equals(mainPrefSizeList)) {
            return mainPictureSizeSupportedLength;
        }
        return mainPrefSizeList.split(",").length;
    }

    public static int calculateRatio(String strImageSize) {
        double max;
        double min;
        int i;
        int[] size = Utils.sizeStringToArray(strImageSize);
        double rwidth = (double) size[0];
        double rheight = (double) size[1];
        if (rwidth > rheight) {
            max = rwidth;
            min = rheight;
        } else {
            max = rheight;
            min = rwidth;
        }
        double ratio = ((double) ((int) ((min / max) * 100.0d))) / 100.0d;
        for (i = 1; i < PICTURE_RATIOS.length; i++) {
            if (Double.compare(ratio, PICTURE_RATIOS[i]) == 0) {
                return i;
            }
        }
        i = 1;
        while (i < PICTURE_RATIOS.length) {
            if (ratio > PICTURE_RATIOS[i]) {
                if (i == 1) {
                    return i;
                }
                if (Math.abs(PICTURE_RATIOS[i] - ratio) <= Math.abs(PICTURE_RATIOS[i - 1] - ratio)) {
                    return i;
                }
                return i - 1;
            } else if (i == PICTURE_RATIOS.length - 1) {
                return i;
            } else {
                i++;
            }
        }
        return 2;
    }

    public static String[] sortPreviewSizeList(String[] sizeSupportedList, String[] previewSupportedList) {
        int[] maxPictureSize = Utils.sizeStringToArray(sizeSupportedList[0]);
        CamLog.m3d(CameraConstants.TAG, "maxPictureSize : " + maxPictureSize);
        float pictureRatio = (float) (maxPictureSize[1] / maxPictureSize[0]);
        CamLog.m3d(CameraConstants.TAG, "ratio : " + pictureRatio);
        int[] maxPreviewSize = Utils.sizeStringToArray(previewSupportedList[0]);
        if (Float.compare(pictureRatio, (float) (maxPreviewSize[1] / maxPreviewSize[0])) == 0) {
            return previewSupportedList;
        }
        String[] newPreviewSizeSupported = new String[previewSupportedList.length];
        float[] ratioList = new float[previewSupportedList.length];
        for (int i = 0; i < sizeSupportedList.length; i++) {
            int[] size = Utils.sizeStringToArray(sizeSupportedList[i]);
            ratioList[i] = (float) (size[1] / size[0]);
        }
        return newPreviewSizeSupported;
    }

    public static ListPreference makeSquareRearPictureSizePreference(Context context, PreferenceGroup prefGroup) {
        int[] lcdSize = Utils.getLCDsize(context, true);
        String lcdSizeStr = lcdSize[1] + "x" + lcdSize[1];
        return makePictureSizePreference(context, prefGroup, Setting.KEY_SQUARE_PICTURE_SIZE, C0088R.string.photo_size, new String[]{"3120x3120", "1440x1440"}, ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, new String[]{"360x360dp", "360x360dp"}, true, true), new String[]{lcdSizeStr, lcdSizeStr}, new String[]{"3120x3120"}, 0);
    }

    public static ListPreference makeSquareFrontPictureSizePreference(Context context, PreferenceGroup prefGroup) {
        int[] lcdSize = Utils.getLCDsize(context, true);
        String lcdSizeStr = lcdSize[1] + "x" + lcdSize[1];
        return makePictureSizePreference(context, prefGroup, Setting.KEY_SQUARE_PICTURE_SIZE, C0088R.string.photo_size, new String[]{"1920x1920", "1440x1440"}, ConfigurationResolutionUtilBase.getDpArrayToPxArray(context, new String[]{"360x360dp", "360x360dp"}, false, true), new String[]{lcdSizeStr, lcdSizeStr}, new String[]{"1920x1920"}, 0);
    }

    public static ListPreference makeSquareRearVideoSizePreference(Context context, PreferenceGroup prefGroup) {
        int[] lcdSize = Utils.getLCDsize(context, true);
        String lcdSizeStr = lcdSize[1] + "x" + lcdSize[1];
        return makeVideoSizePreference(context, prefGroup, Setting.KEY_SQUARE_VIDEO_SIZE, C0088R.string.video_resolution, C0088R.array.video_size_entries, C0088R.array.video_size_entryValues, new String[]{"1080x1080", "720x720"}, -1, -1, null, new String[]{lcdSizeStr, lcdSizeStr}, new String[]{"1080x1080"});
    }

    public static ListPreference makeSquareFrontVideoSizePreference(Context context, PreferenceGroup prefGroup) {
        int[] lcdSize = Utils.getLCDsize(context, true);
        String lcdSizeStr = lcdSize[1] + "x" + lcdSize[1];
        return makeVideoSizePreference(context, prefGroup, Setting.KEY_SQUARE_VIDEO_SIZE, C0088R.string.video_resolution, C0088R.array.video_size_entries, C0088R.array.video_size_entryValues, new String[]{"1080x1080", "720x720"}, -1, -1, null, new String[]{lcdSizeStr, lcdSizeStr}, new String[]{"1080x1080"});
    }

    public static ListPreference makeSlowMotionVideoSizePreference(Context context, PreferenceGroup prefGroup) {
        return makeVideoSizePreference(context, prefGroup, Setting.KEY_SLOW_MOTION_VIDEO_SIZE, C0088R.string.video_resolution, C0088R.array.video_size_entries, C0088R.array.video_size_entryValues, new String[]{"1920x1080@120", CameraConstants.VIDEO_SLOW_MOTION_240, CameraConstants.VIDEO_SLOW_MOTION}, -1, -1, null, new String[]{"1920x1080", "1920x1080", "1920x1080"}, new String[]{CameraConstants.VIDEO_SLOW_MOTION_240});
    }
}
