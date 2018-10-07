package com.lge.camera.constants;

import android.content.ContentResolver;
import android.provider.Settings.System;
import android.support.p000v4.media.session.PlaybackStateCompat;
import android.util.Log;
import com.lge.camera.device.ParamConstants;

public final class MmsProperties {
    private static final String[][] MMS_RESOLUTION_LIMITS;
    private static final String[] MMS_RESOLUTION_LIMITS_QCIF = new String[]{CameraConstants.QCIF_RESOLUTION};
    private static final String[] MMS_RESOLUTION_LIMITS_QVGA = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
    private static final String[] MMS_RESOLUTION_LIMITS_VGA = new String[]{CameraConstants.VGA_RESOLUTION, CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
    private static final String[] MMS_RESOLUTION_NOT_SUPPORT = new String[0];
    private static final long[] MMS_VIDEO_MINIMUM_SIZE = new long[]{30720, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 30720, 30720, 30720, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 30720, 51200, 51200, 51200, 51200, 51200, 51200, 30720, 30720, 30720, 51200, 51200, 51200};
    private static final long[] MMS_VIDEO_SIZE_LIMIT = new long[]{307200, 972800, 972800, 1000000, 307200, 1024000, 1228800, 0, 512000, 307200, 1024000, 307200, 1024000, 307200, 614400, 1024000, 307200, 1024000, 1024000, 1024000, 1024000, 614400, 307200, 307200, 307200, 1024000, 307200, 307200, 307200, 307200, 307200, 1024000, 307200, 307200};
    private static String[] sMmsResolutions = null;

    static {
        r0 = new String[37][];
        r0[0] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[1] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[2] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[3] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[4] = new String[0];
        r0[5] = new String[]{CameraConstants.QCIF_RESOLUTION};
        r0[6] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[7] = new String[0];
        r0[8] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[9] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[10] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[11] = new String[]{CameraConstants.QCIF_RESOLUTION};
        r0[12] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[13] = new String[]{CameraConstants.QCIF_RESOLUTION};
        r0[14] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[15] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[16] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[17] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[18] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[19] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[20] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[21] = new String[]{CameraConstants.QCIF_RESOLUTION};
        r0[22] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[23] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[24] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[25] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[26] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[27] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[28] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[29] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[30] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[31] = new String[]{CameraConstants.QVGA_RESOLUTION, CameraConstants.QCIF_RESOLUTION};
        r0[32] = new String[0];
        r0[33] = new String[0];
        r0[34] = new String[0];
        r0[35] = new String[0];
        r0[36] = new String[0];
        MMS_RESOLUTION_LIMITS = r0;
    }

    public static long getMmsVideoMinimumSize(ContentResolver cr) {
        long returnSize = getValidMMSVideoSizeFromList(MMS_VIDEO_MINIMUM_SIZE) * ((long) (MultimediaProperties.getMinRecordingTime() / 1000));
        if (returnSize >= getMmsVideoSizeLimit(cr)) {
            return getMmsVideoSizeLimit(cr) - PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
        }
        return returnSize;
    }

    public static long getAttachVideoMinimumSize(String resolution, ContentResolver cr) {
        if (resolution.equalsIgnoreCase(ParamConstants.SIZE_FHD_1088P) || resolution.equalsIgnoreCase("1920x1080")) {
            return 4710400;
        }
        if (resolution.equalsIgnoreCase("1440x1088")) {
            return 4505000;
        }
        if (resolution.equalsIgnoreCase("1280x720")) {
            return 3276800;
        }
        if (resolution.equalsIgnoreCase(CameraConstants.TV_RESOLUTION)) {
            return 1228800;
        }
        if (resolution.equalsIgnoreCase(CameraConstants.VGA_RESOLUTION)) {
            return 1024000;
        }
        return getMmsVideoMinimumSize(cr);
    }

    public static String[] getMmsResolutions(ContentResolver cr) {
        if (sMmsResolutions == null) {
            if (!(cr == null || ModelProperties.getCarrierCode() == 4 || ModelProperties.getCarrierCode() == 35 || ModelProperties.getCarrierCode() == 36 || ModelProperties.getCarrierCode() == 7 || ModelProperties.getCarrierCode() == 32 || ModelProperties.getCarrierCode() == 33 || ModelProperties.isWifiOnlyModel(null))) {
                String mmsResolution = System.getString(cr, "android.msg.camera.max.video.resolution");
                if (CameraConstants.VGA_RESOLUTION.equals(mmsResolution) || CameraConstants.QVGA_RESOLUTION.equals(mmsResolution) || CameraConstants.QCIF_RESOLUTION.equals(mmsResolution)) {
                    if (CameraConstants.VGA_RESOLUTION.equals(mmsResolution)) {
                        sMmsResolutions = MMS_RESOLUTION_LIMITS_VGA;
                    } else if (CameraConstants.QVGA_RESOLUTION.equals(mmsResolution)) {
                        sMmsResolutions = MMS_RESOLUTION_LIMITS_QVGA;
                    } else {
                        sMmsResolutions = MMS_RESOLUTION_LIMITS_QCIF;
                    }
                }
            }
            if (sMmsResolutions == null || ModelProperties.getCarrierCode() == 4 || ModelProperties.getCarrierCode() == 7 || ModelProperties.getCarrierCode() == 35 || ModelProperties.getCarrierCode() == 36 || ModelProperties.isWifiOnlyModel(null)) {
                try {
                    if (ModelProperties.isWifiOnlyModel(null)) {
                        sMmsResolutions = MMS_RESOLUTION_NOT_SUPPORT;
                    } else {
                        sMmsResolutions = MMS_RESOLUTION_LIMITS[ModelProperties.getCarrierCode()];
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    sMmsResolutions = MMS_RESOLUTION_LIMITS_QCIF;
                }
            }
        }
        return sMmsResolutions;
    }

    public static String getMaximumMmsResolutions(ContentResolver cr) {
        getMmsResolutions(cr);
        if (sMmsResolutions == null || sMmsResolutions.length <= 0) {
            return null;
        }
        return sMmsResolutions[0];
    }

    public static int getMmsResolutionsLength(ContentResolver cr) {
        return getMmsResolutions(cr).length;
    }

    public static boolean isAvailableMmsResolution(ContentResolver cr, String sizeValue) {
        boolean available = false;
        if (!(cr == null || sizeValue == null)) {
            String[] mmsResolutions = getMmsResolutions(cr);
            if (mmsResolutions.length != 0) {
                available = false;
                for (String equalsIgnoreCase : mmsResolutions) {
                    if (sizeValue.equalsIgnoreCase(equalsIgnoreCase)) {
                        available = true;
                    }
                }
            }
        }
        return available;
    }

    public static long getMmsVideoSizeLimit(ContentResolver cr) {
        String mmsLimit = null;
        if (cr != null) {
            mmsLimit = System.getString(cr, "android.msg.attachment.max.size");
        }
        if (mmsLimit != null) {
            try {
                return (long) Integer.parseInt(mmsLimit);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return getValidMMSVideoSizeFromList(MMS_VIDEO_SIZE_LIMIT);
    }

    public static long getValidMMSVideoSizeFromList(long[] list) {
        if (ModelProperties.getCarrierCode() < list.length) {
            return list[ModelProperties.getCarrierCode()];
        }
        Log.e(CameraConstants.TAG, "The MMS vjdeo size of this carrier code is not added in list. please add it");
        return list[0];
    }

    public static void resetMMSResolutionList() {
        sMmsResolutions = null;
    }
}
