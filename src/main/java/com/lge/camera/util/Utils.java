package com.lge.camera.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceControl;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.view.SurfaceControlEx;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class Utils {
    private static final int START_MARGIN_IDX = 0;
    private static final int TOP_MARGIN_IDX = 1;
    private static volatile int sDefaultDisplayHeight = -1;
    private static volatile boolean sDisplayValuesLoaded = false;
    public static int sSizeOfTafDp = 0;
    private static volatile DisplayMetrics sWindowMatrics = null;
    private static volatile DisplayMetrics sWindowRealMatrics = null;

    public static void fail(String message, Object... args) {
        if (args.length != 0) {
            message = String.format(message, args);
        }
        throw new AssertionError(message);
    }

    public static <T> T checkNotNull(T object) {
        if (object != null) {
            return object;
        }
        throw new NullPointerException();
    }

    public static boolean equals(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    public static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public static float clamp(float x, float min, float max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public static long clamp(long x, long min, long max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public static String getUserAgent(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return String.format("%s/%s; %s/%s/%s/%s; %s/%s/%s/%s", new Object[]{packageInfo.packageName, packageInfo.versionName, Build.BRAND, Build.DEVICE, Build.MODEL, Build.ID, Integer.valueOf(VERSION.SDK_INT), VERSION.RELEASE, VERSION.INCREMENTAL, Build.TYPE});
        } catch (NameNotFoundException e) {
            throw new IllegalStateException("getPackageInfo failed");
        }
    }

    public static float dpToPx(Context context, float dp) {
        return getWindowMatics(context).density * dp;
    }

    public static int[] sizeStringToArray(String sizeString) {
        int[] sizeArray = new int[2];
        if (sizeString == null || "not found".equals(sizeString)) {
            CamLog.m3d(CameraConstants.TAG, "sizeStringToArray return : sizeString = " + sizeString);
        } else {
            String[] sizeStringArray = sizeString.split("@")[0].split("x");
            if (sizeStringArray.length == 2) {
                sizeArray[0] = Integer.parseInt(sizeStringArray[0]);
                sizeArray[1] = Integer.parseInt(sizeStringArray[1]);
            }
        }
        return sizeArray;
    }

    public static float[] sizeStringToFloatArray(String sizeString) {
        float[] sizeArray = new float[2];
        if (sizeString == null || "not found".equals(sizeString)) {
            CamLog.m3d(CameraConstants.TAG, "sizeStringToArray return : sizeString = " + sizeString);
        } else {
            String[] sizeStringArray = sizeString.split("@")[0].split("x");
            if (sizeStringArray.length == 2) {
                sizeArray[0] = Float.parseFloat(sizeStringArray[0]);
                sizeArray[1] = Float.parseFloat(sizeStringArray[1]);
            }
        }
        return sizeArray;
    }

    public static boolean isConfigureLandscape(Resources resource) {
        return false;
    }

    public static int convertDegree(Resources resource, int current) {
        if (isConfigureLandscape(resource)) {
            return (current + 90) % 360;
        }
        return current;
    }

    public static int restoreDegree(Resources resource, int current) {
        if (isConfigureLandscape(resource)) {
            return (current + 270) % 360;
        }
        return current;
    }

    public static boolean isEqualDegree(boolean landscape, int current, int input) {
        if (landscape) {
            if (current == input) {
                return true;
            }
            return false;
        } else if ((current + 90) % 360 != input) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isEqualDegree(Resources resource, int current, int input) {
        if (isConfigureLandscape(resource)) {
            if (current == input) {
                return true;
            }
            return false;
        } else if ((current + 90) % 360 != input) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isLandscapeOrientaionModel(Activity activity) {
        switch (activity.getWindowManager().getDefaultDisplay().getRotation()) {
            case 0:
                if (isConfigureLandscape(activity.getResources())) {
                    return true;
                }
                break;
            case 1:
                if (!isConfigureLandscape(activity.getResources())) {
                    return true;
                }
                break;
            case 2:
                if (isConfigureLandscape(activity.getResources())) {
                    return true;
                }
                break;
            case 3:
                if (!isConfigureLandscape(activity.getResources())) {
                    return true;
                }
                break;
            default:
                return false;
        }
        return false;
    }

    public static int[] getLCDsize(Context context, boolean isContainNaviArea) {
        if (context == null || context.getResources() == null) {
            return new int[]{CameraConstants.SHUTTER_ZOOM_SUPPORTED_MIN_ZOOM_LEVEL, 320};
        }
        DisplayMetrics metrics;
        if (isContainNaviArea) {
            metrics = getWindowRealMatics(context);
        } else {
            metrics = getWindowMatics(context);
        }
        if (metrics.widthPixels > metrics.heightPixels) {
            return new int[]{metrics.widthPixels, metrics.heightPixels};
        }
        return new int[]{metrics.heightPixels, metrics.widthPixels};
    }

    public static void resetLayoutParameter(LayoutParams lp) {
        if (lp != null) {
            try {
                int ruleSize = lp.getRules().length;
                for (int i = 0; i < ruleSize; i++) {
                    lp.addRule(i, 0);
                }
            } catch (NullPointerException e) {
                CamLog.m5e(CameraConstants.TAG, "NullPointerException : " + e);
            }
        }
    }

    public static int getPx(Context context, int id) {
        if (context == null) {
            return 0;
        }
        return Math.round(context.getResources().getDimension(id));
    }

    public static String breakTextToMultiLine(Paint textPaint, String message, int maxWidth) {
        if (message == null || "".equals(message)) {
            return "";
        }
        if (maxWidth == 0 || textPaint == null) {
            return message;
        }
        StringBuffer messageBuffer = new StringBuffer(message);
        StringBuffer breakStringBuffer = new StringBuffer();
        while (messageBuffer.length() > 0) {
            while (messageBuffer.length() > 0 && messageBuffer.charAt(0) == ' ') {
                messageBuffer.deleteCharAt(0);
            }
            String remainString = messageBuffer.toString();
            int breakCount = textPaint.breakText(remainString, true, (float) maxWidth, null);
            if (breakCount > remainString.length()) {
                return message;
            }
            String breakString = remainString.substring(0, breakCount);
            if (breakString != null && breakCount < remainString.length()) {
                breakCount = breakString.lastIndexOf(32);
                if (breakCount < 0) {
                    breakCount = breakString.length();
                }
            }
            breakStringBuffer.append(remainString.substring(0, breakCount));
            messageBuffer.delete(0, breakCount);
            if (messageBuffer.length() > 0) {
                breakStringBuffer.append("\n");
            }
        }
        return breakStringBuffer.toString();
    }

    public static boolean isBurstshotFile(String fullName) {
        if (fullName == null) {
            return false;
        }
        String[] sptString = fullName.split("/");
        if (sptString == null || !sptString[sptString.length - 1].contains("Burst")) {
            return false;
        }
        return true;
    }

    public static String getCurrentDateTime(long dateTime) {
        String stringDateTime = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).format(new Date(dateTime));
        CamLog.m7i(CameraConstants.TAG, "dateTime = " + stringDateTime);
        return stringDateTime;
    }

    public static LayoutParams getRelativeLayoutParams(Context context, int width, int height) {
        if (context == null) {
            return null;
        }
        int windowWidth;
        int windowHeight;
        int[] paramSize = getLayoutParamSize(context, width, height);
        boolean isLand = isConfigureLandscape(context.getResources());
        if (isLand) {
            windowWidth = width;
        } else {
            windowWidth = height;
        }
        if (isLand) {
            windowHeight = height;
        } else {
            windowHeight = width;
        }
        LayoutParams params = new LayoutParams(windowWidth, windowHeight);
        params.setMarginStart(paramSize[0]);
        params.topMargin = paramSize[1];
        return params;
    }

    public static FrameLayout.LayoutParams getFrameLayoutParams(Context context, int width, int height) {
        if (context == null) {
            return null;
        }
        int windowWidth;
        int windowHeight;
        int[] paramSize = getLayoutParamSize(context, width, height);
        boolean isLand = isConfigureLandscape(context.getResources());
        if (isLand) {
            windowWidth = width;
        } else {
            windowWidth = height;
        }
        if (isLand) {
            windowHeight = height;
        } else {
            windowHeight = width;
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(windowWidth, windowHeight);
        params.setMarginStart(paramSize[0]);
        params.topMargin = paramSize[1];
        return params;
    }

    public static int[] getLayoutParamSize(Context context, int width, int height) {
        int[] returnSize = new int[]{640, CameraConstants.SHUTTER_ZOOM_SUPPORTED_MIN_ZOOM_LEVEL};
        if (context != null) {
            DisplayMetrics metrics = getWindowRealMatics(context);
            int longWidth = Math.max(metrics.widthPixels, metrics.heightPixels);
            int shortHeight = Math.min(metrics.widthPixels, metrics.heightPixels);
            float screenRatio = shortHeight == 0 ? 0.0f : ((float) longWidth) / ((float) shortHeight);
            float previewRatio = height == 0 ? 0.0f : ((float) width) / ((float) height);
            if (isConfigureLandscape(context.getResources())) {
                int i;
                returnSize[0] = longWidth - width > 0 ? (longWidth - width) / 2 : 0;
                if (shortHeight - height > 0) {
                    i = (shortHeight - height) / 2;
                } else {
                    i = 0;
                }
                returnSize[1] = i;
            } else {
                returnSize[1] = longWidth - width > 0 ? (longWidth - width) / 2 : 0;
                if (!ModelProperties.isLongLCDModel() || ((float) width) / ((float) height) <= 2.3f) {
                    returnSize[0] = shortHeight - height > 0 ? (shortHeight - height) / 2 : 0;
                } else {
                    returnSize[0] = shortHeight - height > 0 ? shortHeight - height : 0;
                }
            }
            int quickBtnWidth = RatioCalcUtil.getQuickButtonWidth(context);
            int panelWidth = getPx(context, C0088R.dimen.panel.width);
            int panelEndMargin = RatioCalcUtil.getCommandBottomMargin(context);
            if (Float.compare(screenRatio, 1.5f) == 0 && Float.compare(previewRatio, 1.0f) != 0 && Float.compare(previewRatio, 1.5f) < 0 && width >= longWidth - ((quickBtnWidth + panelEndMargin) + panelWidth)) {
                returnSize[1] = getPx(context, C0088R.dimen.preview_4_3_topmargin);
            }
        }
        return returnSize;
    }

    public static void setIgnoreTouchArea(int tafDp) {
        sSizeOfTafDp = Math.round(((float) tafDp) * 0.4f);
    }

    public static boolean isIgnoreTouchEvent(Context context, int x, int y) {
        if (context == null) {
            return true;
        }
        int coordinate;
        int margin = (int) (((float) sSizeOfTafDp) * 0.4f);
        if (isConfigureLandscape(context.getResources())) {
            coordinate = y;
        } else {
            coordinate = x;
        }
        if (coordinate < margin || coordinate > getLCDsize(context, true)[1] - margin) {
            return true;
        }
        return false;
    }

    public static void addTabToNumberedDescription(ViewGroup parentLayout, String msg, Context context, boolean isPrefixNumber, int textStyleId) {
        String message = msg;
        if (message != null) {
            String[] splitTokens = message.split("\n");
            int descriptionIndex = 1;
            String pattern = isPrefixNumber ? "^\\d\\. " : "^\\- ";
            for (int i = 0; i < splitTokens.length; i++) {
                LinearLayout dividedDescriptionRow = new LinearLayout(context);
                dividedDescriptionRow.setLayoutDirection(3);
                String[] subToken = Pattern.compile(pattern).split(splitTokens[i]);
                TextView tvMessage = new TextView(context);
                tvMessage.setTextAppearance(context, textStyleId);
                tvMessage.setTextDirection(5);
                if (subToken.length > 1) {
                    String prefix;
                    if (isPrefixNumber) {
                        Object[] objArr = new Object[1];
                        int descriptionIndex2 = descriptionIndex + 1;
                        objArr[0] = Integer.valueOf(descriptionIndex);
                        prefix = String.format(Locale.getDefault(), "%d. ", objArr);
                        descriptionIndex = descriptionIndex2;
                    } else {
                        prefix = "  •";
                    }
                    TextView tvPrefix = new TextView(context);
                    tvPrefix.setTextAppearance(context, textStyleId);
                    tvPrefix.setTextDirection(5);
                    tvPrefix.setText(prefix);
                    tvMessage.setText(subToken[1]);
                    dividedDescriptionRow.addView(tvPrefix);
                } else {
                    tvMessage.setText(subToken[0]);
                }
                dividedDescriptionRow.addView(tvMessage);
                parentLayout.addView(dividedDescriptionRow, i);
            }
        }
    }

    public static boolean checkSystemUIArea(Context context, int startX, int startY) {
        boolean isLand = isConfigureLandscape(context.getResources());
        int[] lcdSize = getLCDsize(context, false);
        int indicatorSize = getPx(context, C0088R.dimen.indicators.height);
        if (isLand) {
            if (startX > lcdSize[0] || startX < indicatorSize) {
                return true;
            }
        } else if (startY > lcdSize[0] || startY < indicatorSize) {
            return true;
        }
        return false;
    }

    public static int parseStringToInteger(String valueToParse) {
        int result = 0;
        try {
            return Integer.parseInt(valueToParse);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return result;
        }
    }

    public static float parseStringToFloat(String valueToParse) {
        float result = 0.0f;
        try {
            return Float.parseFloat(valueToParse);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return result;
        }
    }

    public static Bitmap getScreenShot(int width, int height, boolean isSurfaceOnly, Rect crop) {
        return getScreenShot(width, height, isSurfaceOnly, crop, false);
    }

    public static Bitmap getScreenShot(int width, int height, boolean isSurfaceOnly, Rect crop, boolean logOff) {
        try {
            Class.forName("com.lge.view.SurfaceControlEx").getDeclaredMethod("screenshotPreviewOnly", new Class[]{Boolean.TYPE}).invoke(new SurfaceControlEx(), new Object[]{Boolean.valueOf(isSurfaceOnly)});
            if (!logOff) {
                CamLog.m3d(CameraConstants.TAG, "screenshotPreviewOnly invoke success.");
            }
        } catch (Exception e) {
            CamLog.m4d(CameraConstants.TAG, "screenshotPreviewOnly error : ", e);
        }
        Bitmap originalBitmap;
        if (crop == null) {
            if (!MiniViewUtil.isMiniViewState()) {
                return SurfaceControl.screenshot(width, height);
            }
            originalBitmap = SurfaceControl.screenshot(width, height, MiniViewUtil.getMiniViewRect());
            if (originalBitmap != null) {
                return Bitmap.createScaledBitmap(originalBitmap, width, height, true);
            }
            return null;
        } else if (!MiniViewUtil.isMiniViewState()) {
            return SurfaceControl.screenshot(width, height, crop);
        } else {
            originalBitmap = SurfaceControl.screenshot(width, height, MiniViewUtil.getMiniViewRect());
            if (originalBitmap == null) {
                return null;
            }
            if (crop.right > originalBitmap.getWidth()) {
                crop.right = originalBitmap.getWidth();
            }
            if (crop.bottom > originalBitmap.getHeight()) {
                crop.bottom = originalBitmap.getHeight();
            }
            return Bitmap.createBitmap(originalBitmap, crop.left, crop.top, crop.width(), crop.height());
        }
    }

    public static Bitmap getScreenShot(int width, int height, boolean isSurfaceOnly, int degree) {
        try {
            Class.forName("com.lge.view.SurfaceControlEx").getDeclaredMethod("screenshotPreviewOnly", new Class[]{Boolean.TYPE}).invoke(new SurfaceControlEx(), new Object[]{Boolean.valueOf(isSurfaceOnly)});
            CamLog.m3d(CameraConstants.TAG, "screenshotPreviewOnly invoke success.");
        } catch (Exception e) {
            CamLog.m4d(CameraConstants.TAG, "screenshotPreviewOnly error : ", e);
        }
        if (!MiniViewUtil.isMiniViewState()) {
            return SurfaceControl.screenshot(new Rect(), width, height, 0, Integer.MAX_VALUE, false, 3);
        }
        Bitmap bitmap = SurfaceControl.screenshot(MiniViewUtil.getMiniViewRect(), width, height, 0, Integer.MAX_VALUE, false, 3);
        if (bitmap != null) {
            return Bitmap.createScaledBitmap(bitmap, width, height, true);
        }
        return null;
    }

    public static boolean isRTLLanguage() {
        return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 1;
    }

    public static boolean calculate4by3Preview(String previewSize) {
        int[] size = sizeStringToArray(previewSize);
        float mPreviewAspect = ((float) size[0]) / ((float) size[1]);
        if (Float.compare(mPreviewAspect, 1.5f) >= 0 || Float.compare(mPreviewAspect, 1.3f) <= 0) {
            return false;
        }
        return true;
    }

    public static boolean isWidePictureSize(int[] size) {
        if (size == null) {
            return false;
        }
        double max;
        double min;
        double width = (double) size[0];
        double height = (double) size[1];
        if (width > height) {
            max = width;
            min = height;
        } else {
            max = height;
            min = width;
        }
        double ratio = min / max;
        if ((Double.compare(0.559375d, ratio) >= 0 || Double.compare(0.565625d, ratio) <= 0) && Double.compare(0.625d, ratio) != 0) {
            return false;
        }
        return true;
    }

    public static boolean isSquarePictureSize(int[] size) {
        if (size == null) {
            return false;
        }
        double max;
        double min;
        double width = (double) size[0];
        double height = (double) size[1];
        if (width > height) {
            max = width;
            min = height;
        } else {
            max = height;
            min = width;
        }
        return Double.compare(0.5d, min / max) == 0;
    }

    public static Context getFixedDensityContext(Context context) {
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.densityDpi = DisplayMetrics.DENSITY_DEVICE_STABLE;
        return context.createConfigurationContext(config);
    }

    public static String getMegaPixelOfPictureSize(String pictureSize, int index) {
        int[] size = sizeStringToArray(pictureSize);
        int totalSize = size[0] * size[1];
        float ratio = ((float) size[0]) / ((float) size[1]);
        float megaPixel = ((float) totalSize) / 1000000.0f;
        if (totalSize >= 10000000) {
            return Math.round(megaPixel) + "MP";
        }
        if (index == 0 && Float.compare(ratio, 1.0f) != 0) {
            megaPixel = (float) Math.round(megaPixel);
        }
        return (((double) Math.round(((double) megaPixel) * 10.0d)) / 10.0d) + "MP";
    }

    public static DisplayMetrics getWindowRealMatics(Context context) {
        if (sDisplayValuesLoaded) {
            return sWindowRealMatrics;
        }
        DisplayMetrics ret = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRealMetrics(ret);
        return ret;
    }

    public static DisplayMetrics getWindowMatics(Context context) {
        if (sDisplayValuesLoaded) {
            return sWindowMatrics;
        }
        DisplayMetrics ret = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(ret);
        return ret;
    }

    public static int getDefaultDisplayHeight(Context context) {
        if (sDisplayValuesLoaded) {
            return sDefaultDisplayHeight;
        }
        return ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getHeight();
    }

    public static void setDisplayValues(Context context) {
        CamLog.m7i(CameraConstants.TAG, "setDisplayValues - s");
        sDisplayValuesLoaded = false;
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics windowMatrics = new DisplayMetrics();
        DisplayMetrics windowRealMatrics = new DisplayMetrics();
        display.getMetrics(windowMatrics);
        display.getRealMetrics(windowRealMatrics);
        sDefaultDisplayHeight = display.getHeight();
        sWindowRealMatrics = windowRealMatrics;
        sWindowMatrics = windowMatrics;
        sDisplayValuesLoaded = true;
        CamLog.m7i(CameraConstants.TAG, "setDisplayValues - e");
    }

    public static Drawable getTypeDrawable(Context c, int type) {
        switch (type) {
            case 1:
                return c.getDrawable(C0088R.drawable.ic_panorama);
            case 2:
            case 5:
            case 18:
                return c.getDrawable(C0088R.drawable.ic_doubleshot);
            case 3:
            case 14:
                return c.getDrawable(C0088R.drawable.ic_popout);
            case 4:
                return c.getDrawable(C0088R.drawable.ic_guideshot);
            case 6:
            case 19:
                return c.getDrawable(C0088R.drawable.ic_gridshot);
            case 7:
            case 20:
                return c.getDrawable(C0088R.drawable.ic_food);
            case 10:
            case 13:
                return c.getDrawable(C0088R.drawable.ic_snapmovie);
            case 11:
                return c.getDrawable(C0088R.drawable.ic_timelapse);
            case 12:
                return c.getDrawable(C0088R.drawable.ic_slomo);
            case 23:
                return c.getDrawable(C0088R.drawable.ic_cinema);
            case 24:
                return c.getDrawable(C0088R.drawable.ic_log);
            case 30:
                return c.getDrawable(C0088R.drawable.ic_burstshot);
            case 100:
                return c.getDrawable(C0088R.drawable.ic_vr_panorama);
            case 201:
                return c.getDrawable(C0088R.drawable.ic_live);
            case 202:
            case 205:
                return c.getDrawable(C0088R.drawable.ic_outfocus);
            case 203:
            case 204:
                return c.getDrawable(C0088R.drawable.ic_dualphoto);
            default:
                return null;
        }
    }

    public static int getVideoModeColumn(boolean isAnimatedPicture, String currentMode) {
        int modeColumn = 0;
        if (CameraConstants.MODE_SNAP.equals(currentMode)) {
            modeColumn = 10;
        } else if (CameraConstants.MODE_TIME_LAPSE_VIDEO.equals(currentMode)) {
            modeColumn = 11;
        } else if (CameraConstants.MODE_SLOW_MOTION.equals(currentMode)) {
            modeColumn = 12;
        } else if (CameraConstants.MODE_MULTIVIEW.equals(currentMode)) {
            modeColumn = 13;
        } else if (CameraConstants.MODE_SQUARE_SPLICE.equals(currentMode)) {
            modeColumn = 5;
        } else if (CameraConstants.MODE_SQUARE_GRID.equals(currentMode)) {
            modeColumn = 6;
        } else if (CameraConstants.MODE_CINEMA.equals(currentMode)) {
            modeColumn = 23;
        } else if ("mode_food".equals(currentMode)) {
            modeColumn = 7;
        } else if (CameraConstants.MODE_POPOUT_CAMERA.equals(currentMode)) {
            if (isAnimatedPicture) {
                modeColumn = 15;
            } else {
                modeColumn = 14;
            }
        }
        CamLog.m3d(CameraConstants.TAG, "[Tile] return video_mode column : " + String.valueOf(modeColumn));
        return modeColumn;
    }

    public static int getCameraModeColumn(String fileName, String currentMode) {
        int modeColumn;
        if (currentMode != null && currentMode.contains(CameraConstants.MODE_PANORAMA)) {
            modeColumn = 1;
        } else if (CameraConstants.MODE_MULTIVIEW.equals(currentMode)) {
            modeColumn = 2;
        } else if (CameraConstants.MODE_POPOUT_CAMERA.equals(currentMode)) {
            modeColumn = 3;
        } else if ("mode_food".equals(currentMode)) {
            modeColumn = 7;
        } else if (CameraConstants.MODE_SQUARE_SPLICE.equals(currentMode)) {
            modeColumn = 0;
        } else if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(currentMode)) {
            modeColumn = 8;
        } else if (CameraConstants.MODE_SQUARE_OVERLAP.equals(currentMode)) {
            modeColumn = 4;
        } else if (CameraConstants.MODE_DUAL_POP_CAMERA.equals(currentMode)) {
            modeColumn = 203;
        } else {
            modeColumn = 0;
        }
        CamLog.m3d(CameraConstants.TAG, "get camera_mode column : " + String.valueOf(modeColumn));
        return modeColumn;
    }

    public static String getNormalNumeric(int num) {
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        if (nf == null) {
            return null;
        }
        nf.setMaximumFractionDigits(10);
        return nf.format((long) num);
    }

    public static String getCommaSeparatedString(int[] list) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.length; i++) {
            builder.append(list[i]);
            if (i != list.length - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    public static boolean checkOOS() {
        return VERSION.SDK_INT > 25;
    }
}
