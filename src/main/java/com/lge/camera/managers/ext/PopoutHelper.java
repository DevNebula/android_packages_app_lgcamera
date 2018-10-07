package com.lge.camera.managers.ext;

import android.content.Context;
import android.util.Size;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.util.Utils;
import java.lang.reflect.Array;
import java.util.List;

public class PopoutHelper {
    public static final int INDEX_16BY9 = 0;
    public static final int INDEX_18BY9 = 3;
    public static final int INDEX_1BY1 = 2;
    public static final int INDEX_4BY3 = 1;
    private final int MAX_PICTURE_SIZE = 8000000;

    public String[] getPictureSizeList(Context c, CameraParameters normalParam, CameraParameters wideParam, ListPreference listPref) {
        String[] firstRatioList;
        String[] secondRatioList;
        String[] thirdRatioList;
        List<Size> normalSupportedList = normalParam.getSupportedPictureSizes();
        List<Size> wideSupportedList = wideParam.getSupportedPictureSizes();
        int[] lcdSize = Utils.getLCDsize(c, true);
        int sizeListLength = 3;
        if (Float.compare(((float) lcdSize[1]) / ((float) lcdSize[0]), 0.56f) != 0) {
            sizeListLength = 4;
        }
        String[][] sizeList = (String[][]) Array.newInstance(String.class, new int[]{sizeListLength, 2});
        int listLength = listPref.getEntries().length;
        if (Float.compare(selectPictureFromSupportedList(normalSupportedList, wideSupportedList, sizeList, 0.56f), 0.56f) == 0) {
            firstRatioList = sizeList[0];
            if (sizeListLength < 4) {
                secondRatioList = sizeList[1];
                thirdRatioList = null;
            } else {
                secondRatioList = sizeList[3];
                thirdRatioList = sizeList[1];
            }
        } else {
            firstRatioList = sizeList[1];
            secondRatioList = sizeList[0];
            if (sizeListLength < 4) {
                secondRatioList = sizeList[0];
                thirdRatioList = null;
            } else {
                secondRatioList = sizeList[0];
                thirdRatioList = sizeList[3];
            }
        }
        String heightOfFirstSize = firstRatioList[0].split("x")[1];
        String heightOfSecondSize = firstRatioList[1].split("x")[1];
        sizeList[2][0] = heightOfFirstSize + "x" + heightOfFirstSize;
        sizeList[2][1] = heightOfSecondSize + "x" + heightOfSecondSize;
        int maxCountPerRatio = listLength > 5 ? 2 : 1;
        String[] pictureSizeList = new String[listLength];
        for (int i = 0; i < maxCountPerRatio; i++) {
            pictureSizeList[i] = firstRatioList[i];
            pictureSizeList[i + maxCountPerRatio] = secondRatioList[i];
            if (thirdRatioList != null) {
                pictureSizeList[(maxCountPerRatio * 2) + i] = thirdRatioList[i];
                pictureSizeList[(maxCountPerRatio * 3) + i] = sizeList[2][i];
            } else {
                pictureSizeList[(maxCountPerRatio * 2) + i] = sizeList[2][i];
            }
        }
        return pictureSizeList;
    }

    public String[] getReducedPreviewSize(String[] previewSizeList) {
        if (previewSizeList == null) {
            return null;
        }
        String[] sizeList = new String[previewSizeList.length];
        for (int i = 0; i < previewSizeList.length; i++) {
            String originSize = previewSizeList[i];
            int[] size = Utils.sizeStringToArray(originSize);
            float ratio = ((float) size[0]) / ((float) size[1]);
            if (ratio > 1.9f) {
                sizeList[i] = ParamConstants.VIDEO_1440_BY_720;
            } else if (ratio > 1.7f) {
                sizeList[i] = "1280x720";
            } else if (ratio > 1.3f) {
                sizeList[i] = "1280x960";
            } else {
                sizeList[i] = originSize;
            }
        }
        return sizeList;
    }

    public float selectPictureFromSupportedList(List<Size> normalSupportedList, List<Size> wideSupportedList, String[][] sizeList, float maxPictureSizeRatio) {
        if (ModelProperties.isMTKChipset()) {
            int maxIndexInSupporatedList = normalSupportedList.size() - 1;
            maxPictureSizeRatio = ((float) ((Size) normalSupportedList.get(maxIndexInSupporatedList)).getHeight()) / ((float) ((Size) normalSupportedList.get(maxIndexInSupporatedList)).getWidth());
            selectPictureSizeForMTK(normalSupportedList, wideSupportedList, sizeList, maxIndexInSupporatedList);
            return maxPictureSizeRatio;
        }
        maxPictureSizeRatio = ((float) ((Size) normalSupportedList.get(0)).getHeight()) / ((float) ((Size) normalSupportedList.get(0)).getWidth());
        selectPictureSizeForQCT(normalSupportedList, wideSupportedList, sizeList, maxPictureSizeRatio);
        return maxPictureSizeRatio;
    }

    private void selectPictureSizeForQCT(List<Size> normalSupportedList, List<Size> wideSupportedList, String[][] sizeList, float maxPictureSizeRatio) {
        int index16by9 = 0;
        int index4by3 = 0;
        int index18by9 = 0;
        int startPosition = 0;
        for (int i = 0; i < wideSupportedList.size(); i++) {
            Size wideSize = (Size) wideSupportedList.get(i);
            if (wideSize.getWidth() * wideSize.getHeight() <= 8000000) {
                for (int j = startPosition; j < normalSupportedList.size(); j++) {
                    Size normalSize = (Size) normalSupportedList.get(j);
                    if (wideSize.getWidth() == normalSize.getWidth() && wideSize.getHeight() == normalSize.getHeight()) {
                        Float ratio = Float.valueOf(((float) ((int) (Float.valueOf(((float) wideSize.getHeight()) / ((float) wideSize.getWidth())).floatValue() * 100.0f))) / 100.0f);
                        if (index16by9 >= 2 || Float.compare(ratio.floatValue(), 0.56f) != 0) {
                            if (index4by3 >= 2 || Float.compare(ratio.floatValue(), 0.75f) != 0) {
                                if (sizeList.length > 3 && index18by9 < 2 && Float.compare(ratio.floatValue(), 0.5f) == 0) {
                                    int index18by92 = index18by9 + 1;
                                    sizeList[3][index18by9] = wideSize.getWidth() + "x" + wideSize.getHeight();
                                    startPosition = j + 1;
                                    index18by9 = index18by92;
                                    break;
                                }
                            }
                            int index4by32 = index4by3 + 1;
                            sizeList[1][index4by3] = wideSize.getWidth() + "x" + wideSize.getHeight();
                            startPosition = j + 1;
                            index4by3 = index4by32;
                            break;
                        }
                        int index16by92 = index16by9 + 1;
                        sizeList[0][index16by9] = wideSize.getWidth() + "x" + wideSize.getHeight();
                        startPosition = j + 1;
                        index16by9 = index16by92;
                        break;
                    }
                }
                if (index16by9 == 2 && index4by3 == 2 && (sizeList.length < 4 || index18by9 == 2)) {
                    return;
                }
            }
        }
    }

    private void selectPictureSizeForMTK(List<Size> normalSupportedList, List<Size> wideSupportedList, String[][] sizeList, int maxIndexInSupporatedList) {
        int index4by3;
        int index16by9;
        int startPosition = maxIndexInSupporatedList;
        int i = wideSupportedList.size() - 1;
        int index18by9 = 0;
        int index4by32 = 0;
        int index16by92 = 0;
        while (i >= 0) {
            int index18by92;
            Size wideSize = (Size) wideSupportedList.get(i);
            for (int j = startPosition; j >= 0; j--) {
                Size normalSize = (Size) normalSupportedList.get(j);
                if (wideSize.getWidth() == normalSize.getWidth() && wideSize.getHeight() == normalSize.getHeight()) {
                    if (index16by92 >= 2 || Float.compare(((float) wideSize.getHeight()) / ((float) wideSize.getWidth()), 0.56f) != 0) {
                        if (index4by32 >= 2 || Float.compare(((float) wideSize.getHeight()) / ((float) wideSize.getWidth()), 0.75f) != 0) {
                            if (sizeList.length > 3 && index18by9 < 2 && Float.compare(((float) wideSize.getHeight()) / ((float) wideSize.getWidth()), 0.5f) == 0) {
                                index18by92 = index18by9 + 1;
                                sizeList[3][index18by9] = wideSize.getWidth() + "x" + wideSize.getHeight();
                                startPosition = j - 1;
                                index4by3 = index4by32;
                                index16by9 = index16by92;
                                break;
                            }
                        }
                        index4by3 = index4by32 + 1;
                        sizeList[1][index4by32] = wideSize.getWidth() + "x" + wideSize.getHeight();
                        startPosition = j - 1;
                        index18by92 = index18by9;
                        index16by9 = index16by92;
                        break;
                    }
                    index16by9 = index16by92 + 1;
                    sizeList[0][index16by92] = wideSize.getWidth() + "x" + wideSize.getHeight();
                    startPosition = j - 1;
                    index18by92 = index18by9;
                    index4by3 = index4by32;
                    break;
                }
            }
            index18by92 = index18by9;
            index4by3 = index4by32;
            index16by9 = index16by92;
            if (index16by9 != 2 || index4by3 != 2 || (sizeList.length >= 4 && index18by92 != 2)) {
                i--;
                index18by9 = index18by92;
                index4by32 = index4by3;
                index16by92 = index16by9;
            } else {
                return;
            }
        }
        index4by3 = index4by32;
        index16by9 = index16by92;
    }

    public String getPopoutLDBIntentString(int currentBackgroundEffect, String ldbString) {
        StringBuilder popoutLDB = new StringBuilder(ldbString);
        if (currentBackgroundEffect == 0) {
            popoutLDB.append("None,");
        }
        if ((currentBackgroundEffect & 8) != 0) {
            popoutLDB.append("Fisheye,");
        }
        if ((currentBackgroundEffect & 4) != 0) {
            popoutLDB.append("B&W,");
        }
        if ((currentBackgroundEffect & 2) != 0) {
            popoutLDB.append("Vignette,");
        }
        if ((currentBackgroundEffect & 1) != 0) {
            popoutLDB.append("Lens_Blur,");
        }
        return popoutLDB.deleteCharAt(popoutLDB.length() - 1).toString();
    }
}
