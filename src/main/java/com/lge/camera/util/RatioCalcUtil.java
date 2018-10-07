package com.lge.camera.util;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;

public class RatioCalcUtil {
    private static final float COMMAND_BOTTOM_HEIGHT_FULLVISION_LONG_RATIO = 0.2794f;
    private static final float COMMAND_BUTTOM_HEIGHT_RATIO = 0.2634f;
    public static int sLongLCDModelTopMargin_1x1 = -1;
    public static int sLongLCDModelTopMargin_4x3 = -1;
    private static Rect sPreviewTextureRect = new Rect();
    public static int sQuickButtonWidth = 1;

    public static int getSizeCalculatedByPercentage(Context context, boolean isLongSide, float percentage) {
        if (Float.compare(percentage, 0.0f) <= 0) {
            CamLog.m3d(CameraConstants.TAG, "Percentage is out of boundary, should be bigger than 0");
            return 1;
        } else if (context == null || context.getResources() == null) {
            return 1;
        } else {
            float calculateWith;
            DisplayMetrics metrics = Utils.getWindowRealMatics(context);
            int lcd_width = metrics.widthPixels;
            int lcd_height = metrics.heightPixels;
            if (lcd_width < lcd_height) {
                int temp = lcd_width;
                lcd_width = lcd_height;
                lcd_height = temp;
            }
            if (isLongSide) {
                calculateWith = ((float) lcd_width) * percentage;
            } else {
                calculateWith = ((float) lcd_height) * percentage;
            }
            return Math.round(calculateWith);
        }
    }

    public static int getQuickButtonWidth(Context context) {
        if (sQuickButtonWidth == 1 || context == null) {
            if (ModelProperties.isTablet(context)) {
                sQuickButtonWidth = getSizeCalculatedByPercentage(context, true, 0.047f);
            } else if (ModelProperties.isLongLCDModel()) {
                sQuickButtonWidth = getLongLCDModelTopMargin(context, 4, 3, 0);
            } else {
                sQuickButtonWidth = getNormalLCDModelTopMargin(context, 4, 3, 0);
            }
        }
        return sQuickButtonWidth;
    }

    public static void setLengthSameWithQuickButtonWidth(Context context, View v, boolean setWidth, boolean isTilePreviewOn) {
        if (context == null || v == null) {
            CamLog.m3d(CameraConstants.TAG, "context or view is null");
            return;
        }
        LayoutParams lp = v.getLayoutParams();
        if (lp == null) {
            CamLog.m3d(CameraConstants.TAG, "LayoutParams is null");
            return;
        }
        if (setWidth) {
            lp.width = getQuickButtonWidth(context);
        } else {
            lp.height = getQuickButtonWidth(context);
        }
        if (isTilePreviewOn) {
            lp.height = getQuickButtonWidth(context) + getSizeCalculatedByPercentage(context, true, 0.11112f);
        }
        v.setLayoutParams(lp);
    }

    public static int getLongLCDModelTopMargin(Context c, int width, int height, int offset) {
        int topMargin;
        float previewRatio = ((float) Math.max(width, height)) / ((float) Math.min(width, height));
        int[] size;
        if (previewRatio > 2.3f) {
            topMargin = getNotchDisplayHeight(c);
        } else if (previewRatio > 2.05f) {
            topMargin = getNotchDisplayHeight(c);
        } else if (previewRatio >= 2.0f) {
            if (ModelProperties.getLCDType() == 2) {
                if (sLongLCDModelTopMargin_4x3 == -1) {
                    sLongLCDModelTopMargin_4x3 = getSizeCalculatedByPercentage(c, true, 0.0468f) + getNotchDisplayHeight(c);
                }
                topMargin = sLongLCDModelTopMargin_4x3;
            } else {
                topMargin = 0;
            }
        } else if (previewRatio >= 1.7f) {
            if (ModelProperties.getLCDType() == 2) {
                size = Utils.getLCDsize(c, true);
                topMargin = (size[0] - ((int) ((((float) size[1]) * 16.0f) / 9.0f))) / 2;
            } else {
                if (sLongLCDModelTopMargin_4x3 == -1) {
                    sLongLCDModelTopMargin_4x3 = getSizeCalculatedByPercentage(c, true, 0.056f);
                }
                topMargin = sLongLCDModelTopMargin_4x3;
            }
        } else if (previewRatio >= 1.2f) {
            if (sLongLCDModelTopMargin_4x3 == -1) {
                if (ModelProperties.getLCDType() == 2) {
                    sLongLCDModelTopMargin_4x3 = getSizeCalculatedByPercentage(c, true, 0.0468f) + getNotchDisplayHeight(c);
                } else {
                    sLongLCDModelTopMargin_4x3 = getSizeCalculatedByPercentage(c, true, 0.056f);
                }
            }
            topMargin = sLongLCDModelTopMargin_4x3;
        } else {
            if (sLongLCDModelTopMargin_1x1 == -1) {
                if (ModelProperties.getLCDType() == 2) {
                    sLongLCDModelTopMargin_1x1 = getSizeCalculatedByPercentage(c, true, 0.1756f);
                } else {
                    size = Utils.getLCDsize(c, true);
                    int[] size_4by3 = new int[]{(int) ((((float) size[1]) * 4.0f) / 3.0f), size[1]};
                    if (sLongLCDModelTopMargin_4x3 == -1) {
                        sLongLCDModelTopMargin_4x3 = getSizeCalculatedByPercentage(c, true, 0.056f);
                    }
                    sLongLCDModelTopMargin_1x1 = sLongLCDModelTopMargin_4x3 + ((size_4by3[0] - size_4by3[1]) / 2);
                }
            }
            topMargin = sLongLCDModelTopMargin_1x1;
        }
        return topMargin + offset;
    }

    public static int getNormalLCDModelTopMargin(Context c, int width, int height, int offset) {
        float previewRatio = ((float) Math.max(width, height)) / ((float) Math.min(width, height));
        if (previewRatio < 1.5f && previewRatio > 1.1f) {
            return getSizeCalculatedByPercentage(c, true, 0.056f);
        }
        if (Float.compare(previewRatio, 1.0f) != 0) {
            return 0;
        }
        int[] size = Utils.getLCDsize(c, true);
        int[] size_4by3 = new int[]{(int) ((((float) size[1]) * 4.0f) / 3.0f), size[1]};
        return getQuickButtonWidth(c) + ((size_4by3[0] - size_4by3[1]) / 2);
    }

    public static void setPreviewTextureRect(Rect rect) {
        sPreviewTextureRect.set(rect);
    }

    public static Rect getPreviewTextureRect() {
        return sPreviewTextureRect;
    }

    public static int getCommandButtonHeight(Context context) {
        if (ModelProperties.getLCDType() == 2) {
            return getSizeCalculatedByPercentage(context, true, COMMAND_BOTTOM_HEIGHT_FULLVISION_LONG_RATIO);
        }
        return getSizeCalculatedByPercentage(context, true, COMMAND_BUTTOM_HEIGHT_RATIO);
    }

    public static int getCommandBottomMargin(Context context) {
        return (getCommandButtonHeight(context) - context.getDrawable(C0088R.drawable.shutter_gallery_normal).getIntrinsicHeight()) / 2;
    }

    public static int getTilePreviewMargin(Context context, boolean isTilePreviewOn, float ratio, int currnetMargin) {
        int margin = currnetMargin;
        if (!isTilePreviewOn) {
            return margin;
        }
        if (Float.compare(ratio, 1.0f) == 0) {
            margin = (getSizeCalculatedByPercentage(context, true, 0.11112f) + currnetMargin) - sLongLCDModelTopMargin_4x3;
        } else if (ratio <= 1.3f || ratio >= 1.4f) {
            if (ratio <= 1.7f || ratio >= 1.8f) {
                margin = getSizeCalculatedByPercentage(context, true, 0.11112f) + getQuickButtonWidth(context);
            } else if (ModelProperties.getLCDType() == 2) {
                margin = getSizeCalculatedByPercentage(context, true, 0.11112f) + getSizeCalculatedByPercentage(context, true, 0.0468f);
            } else {
                margin = getSizeCalculatedByPercentage(context, true, 0.11112f);
            }
        } else if (ModelProperties.getLCDType() == 2) {
            margin = getSizeCalculatedByPercentage(context, true, 0.11112f) + getSizeCalculatedByPercentage(context, true, 0.0468f);
        } else {
            margin = getSizeCalculatedByPercentage(context, true, 0.11112f);
        }
        return getNotchDisplayHeight(context) + margin;
    }

    public static int getSizeRatioInPano(Context context, float value) {
        if (context == null) {
            return 0;
        }
        return getSizeCalculatedByPercentage(context, true, ModelProperties.isLongLCDModel() ? value / 2880.0f : value / 2560.0f);
    }

    public static float getRatio(String previewSize) {
        if (previewSize == null) {
            return 0.0f;
        }
        int[] size = Utils.sizeStringToArray(previewSize);
        return ((float) size[0]) / ((float) size[1]);
    }

    public static int getNotchDisplayHeight(Context context) {
        if (ModelProperties.getLCDType() == 2) {
            return getSizeCalculatedByPercentage(context, true, 0.0307f);
        }
        return 0;
    }

    public static int getSqureTopMargin(Context context) {
        if (ModelProperties.getLCDType() == 2) {
            return getQuickButtonWidth(context);
        }
        return 0;
    }

    public static int getNavigationBarHeight(Context context) {
        if (context != null) {
            int resId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resId > 0) {
                return Utils.getPx(context, resId);
            }
        }
        return 0;
    }
}
