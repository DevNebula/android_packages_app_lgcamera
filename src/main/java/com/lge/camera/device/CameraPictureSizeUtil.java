package com.lge.camera.device;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Size;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.List;

public class CameraPictureSizeUtil {
    private static final int STANDARD_INDEX_16BY9 = 1;
    private static final int STANDARD_INDEX_4BY3 = 0;
    private static final int STANDARD_INDEX_EXTRA = 2;
    public static String sDefaultPictureSize;
    public static String sErrorPictureSize;
    public static int sErrorStringId = C0088R.string.max_cal_resolution_check_error;
    private static int sExtraSizeIndex = 0;

    public static class PictureRatio {
        float height;
        float ratio;
        float width;

        public PictureRatio(float width, float height) {
            this.width = width;
            this.height = height;
            this.ratio = ((float) ((int) ((height / width) * 100.0f))) / 100.0f;
        }
    }

    public static String[] getPictureSizeList(Context context, CameraParameters params, int listLength, int cameraId, float defaultRatio) {
        if (params == null) {
            CamLog.m3d(CameraConstants.TAG, "[ConfigAuto] params is null");
            return null;
        }
        List<Size> supported = params.getSupportedPictureSizes();
        if (supported == null) {
            CamLog.m3d(CameraConstants.TAG, "[ConfigAuto] supported is null");
            return null;
        }
        Size maxPictureSizeFromSensor = (Size) supported.get(ModelProperties.isMTKChipset() ? supported.size() - 1 : 0);
        float maxPictureWidth = (float) maxPictureSizeFromSensor.getWidth();
        float maxPictureHeight = (float) maxPictureSizeFromSensor.getHeight();
        float maxpictureRatio = 0.56f;
        if (maxPictureWidth != 0.0f) {
            maxpictureRatio = ((float) ((int) (100.0f * (maxPictureHeight / maxPictureWidth)))) / 100.0f;
        }
        boolean isSupportStandardSize = false;
        if (maxPictureWidth * maxPictureHeight >= 8000000.0f || (CameraDeviceUtils.isRearCamera(cameraId) && ConfigurationUtil.sIS_WIDE_CAMERA_DEFAULT)) {
            isSupportStandardSize = true;
            listLength *= 2;
        } else if (listLength >= 6) {
            isSupportStandardSize = true;
        }
        sDefaultPictureSize = String.valueOf((int) maxPictureWidth) + "x" + String.valueOf((int) maxPictureHeight);
        CamLog.m3d(CameraConstants.TAG, "[ConfigAuto] Maximum and default picture size : " + maxPictureWidth + ", ratio : " + maxpictureRatio);
        int[] lcdSize = Utils.getLCDsize(context, true);
        float lcdWidth = (float) lcdSize[0];
        float lcdHeight = (float) lcdSize[1];
        float lcdRatio = ((float) ((int) (100.0f * (lcdHeight / lcdWidth)))) / 100.0f;
        CamLog.m3d(CameraConstants.TAG, "[ConfigAuto] lcdWidth : " + lcdWidth + ", lcdHeight : " + lcdHeight + ", LCD ratio : " + lcdRatio);
        ArrayList<PictureRatio> pictureRatioList = makePictureRatioList(maxpictureRatio, lcdRatio, listLength);
        if (pictureRatioList == null || pictureRatioList.size() == 0) {
            CamLog.m3d(CameraConstants.TAG, "[ConfigAuto] The ratio of maximum size is error");
            sErrorStringId = C0088R.string.max_cal_ratio_check_error;
            sErrorPictureSize = sDefaultPictureSize;
            return null;
        }
        String[] sizeSupported = makePictureSizeSupported(supported, listLength, maxPictureWidth, maxPictureHeight, maxpictureRatio, lcdRatio, pictureRatioList, isSupportStandardSize);
        checkDefaultSize(defaultRatio, sizeSupported);
        return sizeSupported;
    }

    private static void checkDefaultSize(float defaultRatio, String[] sizeSupported) {
        boolean checkDefaultSize = false;
        if (sizeSupported != null) {
            for (int i = 0; i < sizeSupported.length; i++) {
                CamLog.m3d(CameraConstants.TAG, "[ConfigAuto] pictureSizeList[" + i + "] = " + sizeSupported[i]);
                if (!checkDefaultSize) {
                    int[] size = Utils.sizeStringToArray(sizeSupported[i]);
                    float ratio = ((float) size[0]) / ((float) size[1]);
                    if (ratio <= defaultRatio + 0.01f && ratio >= defaultRatio - 0.01f) {
                        sDefaultPictureSize = sizeSupported[i];
                        checkDefaultSize = true;
                        CamLog.m3d(CameraConstants.TAG, "[ConfigAuto] Default size = " + sDefaultPictureSize);
                    }
                }
            }
        }
    }

    public static float getDefaultPictureSizeRatio(ListPreference listPref) {
        if (listPref == null) {
            return 0.0f;
        }
        int[] size = Utils.sizeStringToArray(listPref.getDefaultValue());
        return ((float) size[1]) / ((float) size[0]);
    }

    private static String[] makePictureSizeSupported(List<Size> supported, int listLength, float maxPictureWidth, float maxPictureHeight, float maxpictureRatio, float lcdRatio, ArrayList<PictureRatio> pictureRatioList, boolean isSupportStandardSize) {
        int listSize = pictureRatioList.size() == 1 ? 1 : pictureRatioList.size() + 1;
        if (isSupportStandardSize) {
            listSize *= 2;
        }
        String[] sizeSupported = new String[listSize];
        int remainder = ModelProperties.isMTKChipset() ? 16 : 2;
        float[] pictureSize = new float[2];
        int[] standardSizeIndex = getStandardSizeIndexFromSupportedList(supported, (int) (((double) maxPictureWidth) / Math.sqrt(2.0d)), pictureRatioList);
        int newIndex = 0;
        float extraRatio = 0.0f;
        if (pictureRatioList.size() > 2) {
            extraRatio = ((PictureRatio) pictureRatioList.get(sExtraSizeIndex)).ratio;
        }
        for (int i = 0; i < pictureRatioList.size(); i++) {
            int newIndex2;
            if (Float.compare(maxpictureRatio, 0.75f) == 0) {
                pictureSize[0] = maxPictureWidth;
                pictureSize[1] = removeRemainder((pictureSize[0] * ((PictureRatio) pictureRatioList.get(i)).height) / ((PictureRatio) pictureRatioList.get(i)).width, remainder);
                newIndex2 = newIndex + 1;
                sizeSupported[newIndex] = String.valueOf((int) pictureSize[0]) + "x" + String.valueOf((int) pictureSize[1]);
            } else {
                pictureSize[0] = removeRemainder((maxPictureHeight * ((PictureRatio) pictureRatioList.get(i)).width) / ((PictureRatio) pictureRatioList.get(i)).height, remainder);
                pictureSize[1] = maxPictureHeight;
                if (pictureSize[0] > maxPictureWidth) {
                    pictureSize[0] = maxPictureWidth;
                    pictureSize[1] = removeRemainder((pictureSize[0] * ((PictureRatio) pictureRatioList.get(i)).height) / ((PictureRatio) pictureRatioList.get(i)).width, remainder);
                }
                newIndex2 = newIndex + 1;
                sizeSupported[newIndex] = String.valueOf((int) pictureSize[0]) + "x" + String.valueOf((int) pictureSize[1]);
            }
            if (isSupportStandardSize) {
                getStandardPictureSize(supported, pictureRatioList, pictureSize, standardSizeIndex, extraRatio, i);
                newIndex = newIndex2 + 1;
                sizeSupported[newIndex2] = String.valueOf((int) pictureSize[0]) + "x" + String.valueOf((int) pictureSize[1]);
            } else {
                newIndex = newIndex2;
            }
        }
        return checkValidationAndMakeSquarePictureSize(supported, listLength, maxPictureHeight, pictureRatioList, isSupportStandardSize, sizeSupported, remainder, standardSizeIndex);
    }

    private static void getStandardPictureSize(List<Size> supported, ArrayList<PictureRatio> pictureRatioList, float[] pictureSize, int[] standardSizeIndex, float extraRatio, int i) {
        if (Float.compare(0.56f, ((PictureRatio) pictureRatioList.get(i)).ratio) == 0) {
            pictureSize[0] = (float) ((Size) supported.get(standardSizeIndex[1])).getWidth();
            pictureSize[1] = (float) ((Size) supported.get(standardSizeIndex[1])).getHeight();
        }
        if (Float.compare(0.75f, ((PictureRatio) pictureRatioList.get(i)).ratio) == 0) {
            pictureSize[0] = (float) ((Size) supported.get(standardSizeIndex[0])).getWidth();
            pictureSize[1] = (float) ((Size) supported.get(standardSizeIndex[0])).getHeight();
        }
        if (Float.compare(extraRatio, ((PictureRatio) pictureRatioList.get(i)).ratio) == 0) {
            pictureSize[0] = (float) ((Size) supported.get(standardSizeIndex[2])).getWidth();
            pictureSize[1] = (float) ((Size) supported.get(standardSizeIndex[2])).getHeight();
        }
    }

    private static int[] getStandardSizeIndexFromSupportedList(List<Size> supported, int standardSizeWidth, ArrayList<PictureRatio> pictureRatioList) {
        int[] standardSizeIndex = new int[pictureRatioList.size()];
        int standardSizeCheckCount = pictureRatioList.size();
        float extraRatio = 0.0f;
        if (pictureRatioList.size() > 2) {
            extraRatio = ((float) ((int) ((((PictureRatio) pictureRatioList.get(sExtraSizeIndex)).height / ((PictureRatio) pictureRatioList.get(sExtraSizeIndex)).width) * 100.0f))) / 100.0f;
        }
        int i;
        if (!ModelProperties.isMTKChipset()) {
            for (i = 0; i < supported.size(); i++) {
                if (checkStandardSizeIndex(supported, standardSizeWidth, pictureRatioList, standardSizeIndex, standardSizeCheckCount, ((float) ((int) ((((float) ((Size) supported.get(i)).getHeight()) / ((float) ((Size) supported.get(i)).getWidth())) * 100.0f))) / 100.0f, extraRatio, i)) {
                    break;
                }
            }
        } else {
            for (i = supported.size() - 1; i >= 0; i--) {
                if (checkStandardSizeIndex(supported, standardSizeWidth, pictureRatioList, standardSizeIndex, standardSizeCheckCount, ((float) ((int) ((((float) ((Size) supported.get(i)).getHeight()) / ((float) ((Size) supported.get(i)).getWidth())) * 100.0f))) / 100.0f, extraRatio, i)) {
                    break;
                }
            }
        }
        return standardSizeIndex;
    }

    private static boolean checkStandardSizeIndex(List<Size> supported, int standardSizeWidth, ArrayList<PictureRatio> pictureRatioList, int[] standardSizeIndex, int standardSizeCheckCount, float deviceRatio, float extraRatio, int i) {
        if (((Size) supported.get(i)).getWidth() <= standardSizeWidth) {
            if (standardSizeIndex[1] == 0 && Float.compare(0.56f, deviceRatio) == 0) {
                standardSizeIndex[1] = i;
                standardSizeCheckCount--;
            }
            if (standardSizeIndex[0] == 0 && Float.compare(0.75f, deviceRatio) == 0) {
                standardSizeIndex[0] = i;
                standardSizeCheckCount--;
            }
            if (pictureRatioList.size() > 2 && standardSizeIndex[2] == 0 && Float.compare(extraRatio, deviceRatio) == 0) {
                standardSizeIndex[2] = i;
                standardSizeCheckCount--;
            }
            if (standardSizeCheckCount == 0) {
                CamLog.m3d(CameraConstants.TAG, "[ConfigAuto] get index for standard picture size, 4:3 - " + standardSizeIndex[0] + ", 16:9 - " + standardSizeIndex[1]);
                return true;
            }
        }
        return false;
    }

    private static String[] checkValidationAndMakeSquarePictureSize(List<Size> supported, int listLength, float maxPictureHeight, ArrayList<PictureRatio> pictureRatioList, boolean isSupportStandardSize, String[] sizeSupported, int remainder, int[] standardSizeIndex) {
        if (listLength <= 1) {
            return sizeSupported;
        }
        if (isCustomResolutionInParameter(supported, sizeSupported, isSupportStandardSize)) {
            int lastIndex = !isSupportStandardSize ? pictureRatioList.size() : pictureRatioList.size() * 2;
            float pictureWidth = removeRemainder(maxPictureHeight, remainder);
            sizeSupported[lastIndex] = String.valueOf((int) pictureWidth) + "x" + String.valueOf((int) pictureWidth);
            if (!isSupportStandardSize) {
                return sizeSupported;
            }
            float pictureHeight = removeRemainder((float) ((Size) supported.get(standardSizeIndex[1])).getHeight(), remainder);
            sizeSupported[lastIndex + 1] = String.valueOf((int) pictureHeight) + "x" + String.valueOf((int) pictureHeight);
            return sizeSupported;
        }
        sErrorStringId = C0088R.string.max_cal_resolution_check_error;
        return null;
    }

    private static ArrayList<PictureRatio> makePictureRatioList(float mMaxpictureRatio, float lcdRatio, int listLength) {
        ArrayList<PictureRatio> pictureRatioList = new ArrayList();
        sExtraSizeIndex = 0;
        if (Float.compare(mMaxpictureRatio, 0.75f) == 0 || mMaxpictureRatio > 0.75f) {
            pictureRatioList.add(new PictureRatio(4.0f, 3.0f));
            if (listLength != 1) {
                if (Float.compare(lcdRatio, 0.62f) == 0) {
                    pictureRatioList.add(new PictureRatio(8.0f, 5.0f));
                    sExtraSizeIndex = 1;
                } else if (Float.compare(lcdRatio, 0.6f) == 0) {
                    pictureRatioList.add(new PictureRatio(5.0f, 3.0f));
                    sExtraSizeIndex = 1;
                }
                pictureRatioList.add(new PictureRatio(16.0f, 9.0f));
                if (Float.compare(lcdRatio, 0.5f) == 0 && !"LG-F700L".equals(SystemProperties.get("ro.product.model"))) {
                    pictureRatioList.add(new PictureRatio(18.0f, 9.0f));
                    sExtraSizeIndex = 2;
                }
                if (Float.compare(lcdRatio, 0.46f) == 0) {
                    pictureRatioList.add(new PictureRatio(18.9f, 9.0f));
                    sExtraSizeIndex = 2;
                }
            }
        }
        if (Float.compare(mMaxpictureRatio, 0.56f) == 0) {
            pictureRatioList.add(new PictureRatio(16.0f, 9.0f));
            if (Float.compare(lcdRatio, 0.6f) == 0) {
                pictureRatioList.add(new PictureRatio(5.0f, 3.0f));
                sExtraSizeIndex = 1;
            } else if (Float.compare(lcdRatio, 0.62f) == 0) {
                pictureRatioList.add(new PictureRatio(8.0f, 5.0f));
                sExtraSizeIndex = 1;
            } else if (Float.compare(lcdRatio, 0.5f) == 0 && !"LG-F700L".equals(SystemProperties.get("ro.product.model"))) {
                pictureRatioList.add(new PictureRatio(18.0f, 9.0f));
                sExtraSizeIndex = 1;
            }
            pictureRatioList.add(new PictureRatio(4.0f, 3.0f));
        }
        return pictureRatioList;
    }

    private static float removeRemainder(float size, int reste) {
        if (size % ((float) reste) <= 0.0f) {
            return size;
        }
        if (size % ((float) reste) >= ((float) (reste / 2))) {
            return (((float) reste) + size) - (size % ((float) reste));
        }
        return size - (size % ((float) reste));
    }

    private static boolean isCustomResolutionInParameter(List<Size> supported, String[] sizeSupported, boolean isSupportStandardSize) {
        int matchCount = 0;
        int sizeSupportedLength = isSupportStandardSize ? sizeSupported.length - 2 : sizeSupported.length - 1;
        for (int i = 0; i < sizeSupportedLength; i++) {
            for (int j = 0; j < supported.size(); j++) {
                if (sizeSupported[i].equals(((Size) supported.get(j)).getWidth() + "x" + ((Size) supported.get(j)).getHeight())) {
                    matchCount++;
                    break;
                }
            }
            if (i + 1 != matchCount) {
                sErrorPictureSize = sizeSupported[i];
                break;
            }
        }
        if (sizeSupportedLength == matchCount) {
            return true;
        }
        CamLog.m7i(CameraConstants.TAG, "[ConfigAuto] " + sErrorPictureSize + " resolution is not existed in parameter table");
        return false;
    }
}
