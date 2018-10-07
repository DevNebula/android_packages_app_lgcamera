package com.lge.camera.managers;

import android.content.res.Configuration;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;

public class IndicatorManager extends ManagerInterfaceImpl {
    private static final int INDI_HIDE_DURATION = 3000;
    private final float INDICATOR_EXTRA_MARGIN_TOP = 0.036f;
    private final float INDICATOR_GAP = 0.018f;
    private final float INDICATOR_IMAGE_SIZE = 0.0306f;
    private final float INDICATOR_MARGIN_TOP = 0.146f;
    private final float INDICATOR_MARGIN_TOP_NOTCH = 0.142f;
    private final float INDICATOR_MARGIN_TOP_RECORDING = 0.2043f;
    private final float INDICATOR_MARGIN_TOP_SQUARE_MODE = 0.0647f;
    private final float INDICATOR_MARGIN_TOP_SQUARE_MODE_NOTCH = 0.111f;
    private final float INDICATOR_MARGIN_TOP_SQUARE_MODE_RECORDING = 0.0556f;
    private final float INDICATOR_MARGIN_TOP_SQUARE_MODE_RECORDING_ONE_ANGLE = 0.1116f;
    private final float INDICATOR_MARGIN_TOP_SQUARE_MODE_RECORDING_SINGLE_ZOOM = 0.192f;
    private final float INDICATOR_MARGIN_TOP_TIMELAPS = 0.2708f;
    private final float INDICATOR_MARGIN_TOP_TIMELAPS_RECORDING = 0.35f;
    private final float INDICATOR_PADDING_END = 0.115f;
    private HandlerRunnable mHideBatteryIndiRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (IndicatorManager.this.mGet.checkModuleValidate(9)) {
                CamLog.m3d(CameraConstants.TAG, "[indicator] mHideBatteryIndiRunnable");
                IndicatorManager.this.updateIndicator(8, 4, false);
                return;
            }
            CamLog.m11w(CameraConstants.TAG, "[indicator] mHideBatteryIndiRunnable return. VALIDATE_APP_FINISH | VALIDATE_ON_SWAPCAMERA");
        }
    };
    private HandlerRunnable mHideFlashIndiRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (IndicatorManager.this.mGet.checkModuleValidate(9)) {
                View iconView = IndicatorManager.this.mGet.findViewById(C0088R.id.indicator_item_hdr_or_flash);
                if (iconView != null && iconView.getVisibility() == 0) {
                    IndicatorManager.this.updateIndicator(3, 4, true);
                }
            }
        }
    };
    private HandlerRunnable mHideHDRIndiRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (IndicatorManager.this.mGet.checkModuleValidate(9)) {
                IndicatorManager.this.updateIndicator(2, 4, true);
            }
        }
    };
    private HandlerRunnable mHideLivePhotoIndicatorRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (IndicatorManager.this.mGet.checkModuleValidate(9)) {
                CamLog.m3d(CameraConstants.TAG, "[indicator] mHideLivePhotoIndicatorRunnable");
                IndicatorManager.this.updateIndicator(9, 4, true);
                return;
            }
            CamLog.m11w(CameraConstants.TAG, "[indicator] mHideLivePhotoIndicatorRunnable return. VALIDATE_APP_FINISH | VALIDATE_ON_SWAPCAMERA");
        }
    };
    private HandlerRunnable mHideRAWIndiRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (IndicatorManager.this.mGet.checkModuleValidate(9)) {
                IndicatorManager.this.updateIndicator(7, 4, true);
            }
        }
    };
    private HandlerRunnable mHideSteadyIndiRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (IndicatorManager.this.mGet.checkModuleValidate(9)) {
                IndicatorManager.this.updateIndicator(5, 4, true);
            }
        }
    };
    private HandlerRunnable mHideTimerIndicatorRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (IndicatorManager.this.mGet.checkModuleValidate(9)) {
                CamLog.m3d(CameraConstants.TAG, "[indicator] mHideTimerIndicatorRunnable");
                IndicatorManager.this.updateIndicator(10, 4, false);
                return;
            }
            CamLog.m11w(CameraConstants.TAG, "[indicator] mHideTimerIndicatorRunnable return. VALIDATE_APP_FINISH | VALIDATE_ON_SWAPCAMERA");
        }
    };
    private ArrayList<Integer> mIndicatorIdList;
    private LinearLayout mIndicatorLayout = null;
    private boolean mIsCheeseShutterShowing = false;
    private HandlerRunnable mShowBatteryIndiRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (IndicatorManager.this.mGet.checkModuleValidate(9)) {
                CamLog.m3d(CameraConstants.TAG, "[indicator] mShowBatteryIndiRunnable");
                IndicatorManager.this.updateIndicator(8, 0, true);
                return;
            }
            CamLog.m11w(CameraConstants.TAG, "[indicator] mShowBattery5IndiRunnable return. VALIDATE_APP_FINISH | VALIDATE_ON_SWAPCAMERA");
        }
    };
    private HandlerRunnable mShowFlashIndiRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (!IndicatorManager.this.mGet.checkModuleValidate(9)) {
            }
        }
    };
    private HandlerRunnable mShowHDRIndiRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (IndicatorManager.this.mGet.checkModuleValidate(9)) {
                IndicatorManager.this.updateIndicator(2, 0, false);
            }
        }
    };
    private HandlerRunnable mShowLivePhotoIndicatorRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (IndicatorManager.this.mGet.checkModuleValidate(9)) {
                CamLog.m3d(CameraConstants.TAG, "[indicator] mShowLivePhotoIndicatorRunnable");
                IndicatorManager.this.updateIndicator(9, 0, false);
                return;
            }
            CamLog.m11w(CameraConstants.TAG, "[indicator] mShowLivePhotoIndicatorRunnable return. VALIDATE_APP_FINISH | VALIDATE_ON_SWAPCAMERA");
        }
    };
    private HandlerRunnable mShowRAWIndiRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (IndicatorManager.this.mGet.checkModuleValidate(9)) {
                IndicatorManager.this.updateIndicator(7, 0, false);
            }
        }
    };
    private HandlerRunnable mShowSteadyIndiRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (IndicatorManager.this.mGet.checkModuleValidate(9)) {
                IndicatorManager.this.updateIndicator(5, 0, false);
            }
        }
    };
    private HandlerRunnable mShowTimerIndicatorRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (IndicatorManager.this.mGet.checkModuleValidate(9)) {
                CamLog.m3d(CameraConstants.TAG, "[indicator] mShowTimerIndicatorRunnable");
                IndicatorManager.this.updateIndicator(10, 0, false);
                return;
            }
            CamLog.m11w(CameraConstants.TAG, "[indicator] mShowTimerIndicatorRunnable return. VALIDATE_APP_FINISH | VALIDATE_ON_SWAPCAMERA");
        }
    };

    public IndicatorManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        CamLog.m3d(CameraConstants.TAG, "IndicatorManager init()!");
        initIndicatorListAndLayout();
    }

    public void initIndicatorListAndLayout() {
        CamLog.m3d(CameraConstants.TAG, "initIndicatorListAndLayout");
        makeIndicatorList();
        initIndicator();
        changeInidicatorLayout();
        setRotateDegree(this.mGet.getOrientationDegree(), false);
    }

    public void onPauseBefore() {
        hideAllIndicator();
        this.mGet.removePostRunnable(this.mShowFlashIndiRunnable);
        this.mGet.removePostRunnable(this.mHideFlashIndiRunnable);
        this.mGet.removePostRunnable(this.mShowHDRIndiRunnable);
        this.mGet.removePostRunnable(this.mHideHDRIndiRunnable);
        this.mGet.removePostRunnable(this.mShowSteadyIndiRunnable);
        this.mGet.removePostRunnable(this.mHideSteadyIndiRunnable);
        this.mGet.removePostRunnable(this.mShowRAWIndiRunnable);
        this.mGet.removePostRunnable(this.mHideRAWIndiRunnable);
        this.mGet.removePostRunnable(this.mShowLivePhotoIndicatorRunnable);
        this.mGet.removePostRunnable(this.mHideLivePhotoIndicatorRunnable);
        this.mGet.removePostRunnable(this.mShowTimerIndicatorRunnable);
        this.mGet.removePostRunnable(this.mHideTimerIndicatorRunnable);
        super.onPauseBefore();
    }

    public void onDestroy() {
        if (this.mIndicatorIdList != null) {
            this.mIndicatorIdList.clear();
            this.mIndicatorIdList = null;
        }
        removeAllChildItems();
        this.mIndicatorLayout = null;
        this.mGet.removePostRunnable(this.mShowBatteryIndiRunnable);
        this.mGet.removePostRunnable(this.mHideBatteryIndiRunnable);
    }

    public void onConfigurationChanged(Configuration config) {
        initIndicator();
        changeInidicatorLayout();
        setRotateDegree(getOrientationDegree(), false);
    }

    public void setRotateDegree(int degree, boolean animation) {
        if (this.mIndicatorLayout != null) {
            for (int i = 0; i < this.mIndicatorLayout.getChildCount(); i++) {
                RotateImageView indicator = (RotateImageView) this.mIndicatorLayout.getChildAt(i);
                if (indicator != null) {
                    indicator.setDegree(degree, animation);
                }
            }
        }
    }

    private void makeIndicatorList() {
        this.mIndicatorIdList = new ArrayList();
        this.mIndicatorIdList.add(Integer.valueOf(C0088R.id.indicator_item_battery));
        if (this.mGet.isIndicatorSupported(C0088R.id.indicator_item_cheese_shutter_or_timer)) {
            this.mIndicatorIdList.add(Integer.valueOf(C0088R.id.indicator_item_cheese_shutter_or_timer));
        }
        if (this.mGet.isIndicatorSupported(C0088R.id.indicator_item_hdr_or_flash)) {
            this.mIndicatorIdList.add(Integer.valueOf(C0088R.id.indicator_item_hdr_or_flash));
        }
        if (this.mGet.isIndicatorSupported(C0088R.id.indicator_item_livephoto)) {
            this.mIndicatorIdList.add(Integer.valueOf(C0088R.id.indicator_item_livephoto));
        }
        if (this.mGet.isIndicatorSupported(C0088R.id.indicator_item_steady)) {
            this.mIndicatorIdList.add(Integer.valueOf(C0088R.id.indicator_item_steady));
        }
        if (this.mGet.isIndicatorSupported(C0088R.id.indicator_item_raw)) {
            this.mIndicatorIdList.add(Integer.valueOf(C0088R.id.indicator_item_raw));
        }
    }

    public void changeInidicatorLayout() {
        if (this.mIndicatorLayout != null) {
            int marginTopPx;
            LayoutParams params = (LayoutParams) this.mIndicatorLayout.getLayoutParams();
            if (ModelProperties.getLCDType() == 1) {
                marginTopPx = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.146f);
            } else if (ModelProperties.getLCDType() == 2) {
                marginTopPx = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.142f);
            } else {
                marginTopPx = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.indicators_in_recording.paddingRight);
            }
            if ("on".equals(this.mGet.getSettingValue(Setting.KEY_TILE_PREVIEW)) && !this.mGet.isRecordingState()) {
                marginTopPx += RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.036f);
            }
            if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                marginTopPx = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0647f);
                if (ModelProperties.getLCDType() == 2) {
                    marginTopPx = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.111f);
                    if (this.mGet.isRecordingState()) {
                        if (this.mGet.isRecordingSingleZoom()) {
                            marginTopPx = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.192f);
                        } else {
                            marginTopPx = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0556f) + RatioCalcUtil.getSqureTopMargin(this.mGet.getAppContext());
                        }
                    }
                } else if (this.mGet.isRecordingState()) {
                    if (this.mGet.isRecordingSingleZoom()) {
                        marginTopPx = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.111f);
                    } else {
                        marginTopPx = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.1116f) + RatioCalcUtil.getSqureTopMargin(this.mGet.getAppContext());
                    }
                }
            } else if (this.mGet.getShotMode().equals(CameraConstants.MODE_TIME_LAPSE_VIDEO)) {
                marginTopPx = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, this.mGet.isRecordingState() ? 0.35f : 0.2708f);
            } else if (this.mGet.isRecordingState()) {
                marginTopPx = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.2043f);
            }
            params.setMargins(0, marginTopPx, 0, 0);
            this.mIndicatorLayout.setLayoutParams(params);
            int paddingEnd = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.indicators.paddingTop);
            if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(this.mGet.getShotMode()) && this.mGet.isRecordingState() && !this.mGet.isRecordingSingleZoom() && FunctionProperties.getCameraTypeRear() == 1) {
                paddingEnd = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.115f);
            }
            this.mIndicatorLayout.setPaddingRelative(0, 0, paddingEnd, 0);
        }
    }

    private void initIndicator() {
        this.mIndicatorLayout = (LinearLayout) this.mGet.findViewById(C0088R.id.indicator_layout);
        if (this.mIndicatorLayout == null) {
            CamLog.m3d(CameraConstants.TAG, "indicator layout is null, return");
            return;
        }
        removeAllChildItems();
        int indicatorGap = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.018f);
        int imageSize = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.0306f);
        for (int i = 0; i < this.mIndicatorIdList.size(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imageSize, imageSize);
            params.setLayoutDirection(0);
            if (i == 0) {
                params.setMargins(0, 0, 0, 0);
            } else {
                params.setMargins(0, indicatorGap, 0, 0);
            }
            RotateImageView indicatorItem = new RotateImageView(this.mGet.getAppContext());
            indicatorItem.setId(((Integer) this.mIndicatorIdList.get(i)).intValue());
            indicatorItem.setVisibility(4);
            indicatorItem.setScaleType(ScaleType.FIT_XY);
            this.mIndicatorLayout.addView(indicatorItem, i, params);
        }
    }

    public void removeAllChildItems() {
        if (this.mIndicatorLayout != null) {
            if (this.mIndicatorLayout.getChildCount() > 0) {
                this.mIndicatorLayout.removeAllViews();
            }
            this.mIsCheeseShutterShowing = false;
        }
    }

    public void updateIndicator(int id, int intParam, boolean booleanParam) {
        int intParameter = intParam;
        if (!this.mGet.isManualMode() || id != 2) {
            if (checkIndicatorVisibleCondition() || intParam != 0) {
                switch (id) {
                    case 1:
                        updateCheeseShutterIndicator(booleanParam);
                        return;
                    case 2:
                        updateIndicatorUsingAnim(C0088R.id.indicator_item_hdr_or_flash, C0088R.drawable.camera_indicator_icon_hdr_normal, intParameter, booleanParam);
                        return;
                    case 3:
                        updateIndicatorUsingAnim(C0088R.id.indicator_item_hdr_or_flash, C0088R.drawable.camera_indicator_icon_flash_normal, intParameter, booleanParam);
                        return;
                    case 5:
                        updateIndicatorUsingAnim(C0088R.id.indicator_item_steady, C0088R.drawable.camera_indicator_icon_steady_normal, intParameter, booleanParam);
                        return;
                    case 7:
                        updateIndicatorUsingAnim(C0088R.id.indicator_item_raw, C0088R.drawable.camera_indicator_icon_raw_normal, intParameter, booleanParam);
                        return;
                    case 8:
                        updateIndicatorUsingAnim(C0088R.id.indicator_item_battery, C0088R.drawable.camera_indicator_icon_battery, intParam, booleanParam);
                        return;
                    case 9:
                        updateIndicatorUsingAnim(C0088R.id.indicator_item_livephoto, C0088R.drawable.camera_indicator_icon_live_normal, intParameter, booleanParam);
                        return;
                    case 10:
                        updateIndicatorUsingAnim(C0088R.id.indicator_item_cheese_shutter_or_timer, C0088R.drawable.camera_indicator_icon_timer_normal, intParam, booleanParam);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public boolean isIndicatorVisible(int id) {
        int iconViewId = -1;
        switch (id) {
            case 0:
                return isIndicatorVisible();
            case 1:
                iconViewId = C0088R.id.indicator_item_cheese_shutter_or_timer;
                break;
            case 2:
            case 3:
                iconViewId = C0088R.id.indicator_item_hdr_or_flash;
                break;
            case 5:
                iconViewId = C0088R.id.indicator_item_steady;
                break;
            case 7:
                iconViewId = C0088R.id.indicator_item_raw;
                break;
            case 9:
                iconViewId = C0088R.id.indicator_item_livephoto;
                break;
        }
        RotateImageView iconView = (RotateImageView) this.mGet.findViewById(iconViewId);
        if (iconView != null && iconView.getVisibility() == 0) {
            return true;
        }
        return false;
    }

    public boolean isIndicatorVisible() {
        int[] idList = new int[]{C0088R.id.indicator_item_cheese_shutter_or_timer, C0088R.id.indicator_item_hdr_or_flash, C0088R.id.indicator_item_steady, C0088R.id.indicator_item_raw, C0088R.id.indicator_item_livephoto};
        for (int findViewById : idList) {
            RotateImageView iconView = (RotateImageView) this.mGet.findViewById(findViewById);
            if (iconView != null && iconView.getVisibility() == 0) {
                return true;
            }
        }
        return false;
    }

    private boolean checkIndicatorVisibleCondition() {
        return (this.mGet.isMenuShowing(1003) || this.mGet.isGridPostViesShowing()) ? false : true;
    }

    public void hideAllIndicator() {
        if (this.mIndicatorIdList != null) {
            for (int i = 0; i < this.mIndicatorIdList.size(); i++) {
                RotateImageView icon = (RotateImageView) this.mGet.findViewById(((Integer) this.mIndicatorIdList.get(i)).intValue());
                if (icon != null) {
                    icon.setVisibility(4);
                }
            }
        }
    }

    private void updateCheeseShutterIndicator(boolean recog) {
        final RotateImageView icon = (RotateImageView) this.mGet.findViewById(C0088R.id.indicator_item_cheese_shutter_or_timer);
        if (icon != null) {
            CamLog.m3d(CameraConstants.TAG, "[indicator] updateCheeseShutterIndicator, recog = " + recog);
            if (recog) {
                icon.setImageResource(C0088R.drawable.camera_indicator_icon_voice_shutter_normal);
                icon.clearAnimation();
                icon.setVisibility(0);
                this.mIsCheeseShutterShowing = true;
            } else if (icon.getVisibility() == 0) {
                AnimationUtil.startShowingAnimation(icon, false, 500, new AnimationListener() {
                    public void onAnimationStart(Animation animation) {
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }

                    public void onAnimationEnd(Animation animation) {
                        if (icon != null) {
                            icon.setVisibility(4);
                        }
                        IndicatorManager.this.mIsCheeseShutterShowing = false;
                    }
                });
            } else {
                icon.setVisibility(4);
            }
        }
    }

    private void updateIndicatorUsingAnim(int iconViewId, int iconResId, int visibility, boolean useAnimation) {
        RotateImageView iconView = (RotateImageView) this.mGet.findViewById(iconViewId);
        if (iconView != null) {
            iconView.setImageResource(iconResId);
            if (!useAnimation) {
                iconView.clearAnimation();
                iconView.setVisibility(visibility);
            } else if (visibility == 0) {
                if (iconView.getVisibility() != 0) {
                    AnimationUtil.startShowingAnimation(iconView, true, 200, null);
                }
            } else if (iconView.getVisibility() == 0) {
                AnimationUtil.startShowingAnimation(iconView, false, 200, null);
            } else {
                iconView.setVisibility(4);
            }
        }
    }

    public void onCameraSwitchingEnd() {
        if (AppControlUtil.getBatteryLevel() <= 5) {
            setBatteryIndicatorVisibility(true);
        }
        setTimerIndicatorVisibility(true);
    }

    public void showSceneIndicator(int id) {
        if (!this.mGet.isJogZoomMoving()) {
            switch (id) {
                case 2:
                    this.mGet.postOnUiThread(this.mShowHDRIndiRunnable, 0);
                    this.mGet.removePostRunnable(this.mHideHDRIndiRunnable);
                    this.mGet.postOnUiThread(this.mHideHDRIndiRunnable, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                    return;
                case 3:
                    this.mGet.postOnUiThread(this.mShowFlashIndiRunnable, 0);
                    this.mGet.removePostRunnable(this.mHideFlashIndiRunnable);
                    this.mGet.postOnUiThread(this.mHideFlashIndiRunnable, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                    return;
                case 5:
                    this.mGet.postOnUiThread(this.mShowSteadyIndiRunnable, 0);
                    this.mGet.removePostRunnable(this.mHideSteadyIndiRunnable);
                    this.mGet.postOnUiThread(this.mHideSteadyIndiRunnable, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                    return;
                case 7:
                    this.mGet.postOnUiThread(this.mShowRAWIndiRunnable, 0);
                    this.mGet.removePostRunnable(this.mHideRAWIndiRunnable);
                    this.mGet.postOnUiThread(this.mHideRAWIndiRunnable, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                    return;
                case 9:
                    this.mGet.postOnUiThread(this.mShowLivePhotoIndicatorRunnable, 0);
                    this.mGet.removePostRunnable(this.mHideLivePhotoIndicatorRunnable);
                    this.mGet.postOnUiThread(this.mHideLivePhotoIndicatorRunnable, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                    return;
                case 10:
                    this.mGet.postOnUiThread(this.mShowTimerIndicatorRunnable, 0);
                    return;
                default:
                    return;
            }
        }
    }

    public void setBatteryIndicatorVisibility(boolean visible) {
        if (this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL)) {
            updateIndicator(C0088R.id.indicator_item_battery, 4, false);
        } else if (!visible) {
            this.mGet.postOnUiThread(this.mHideBatteryIndiRunnable, 0);
        } else if (AppControlUtil.getBatteryLevel() <= 5) {
            this.mGet.postOnUiThread(this.mShowBatteryIndiRunnable, 0);
        }
    }

    public void setTimerIndicatorVisibility(boolean visible) {
        if (this.mIsCheeseShutterShowing) {
            CamLog.m3d(CameraConstants.TAG, "setTimerIndicatorVisibility, cheese shutter indicator is showing, return");
        } else if (!visible) {
            this.mGet.postOnUiThread(this.mHideTimerIndicatorRunnable, 0);
        } else if (Integer.parseInt(this.mGet.getSettingValue(Setting.KEY_TIMER)) > 0) {
            this.mGet.postOnUiThread(this.mShowTimerIndicatorRunnable, 0);
        }
    }

    public void slideIndicator(boolean show, boolean useAnimation) {
        setVisibleIndicatorView(C0088R.id.indicator_layout, show, useAnimation);
    }

    private void setVisibleIndicatorView(int resId, boolean show, boolean animation) {
        boolean z = true;
        int visible = 0;
        View view = this.mGet.findViewById(resId);
        if (view != null) {
            view.clearAnimation();
            if (animation) {
                boolean slidingFromRight;
                if (Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
                    slidingFromRight = true;
                } else {
                    slidingFromRight = false;
                }
                if (Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
                    z = false;
                }
                AnimationUtil.startTransAnimation(view, show, z, null, slidingFromRight);
                return;
            }
            if (!show) {
                visible = 4;
            }
            view.setVisibility(visible);
        }
    }
}
