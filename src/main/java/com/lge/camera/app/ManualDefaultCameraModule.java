package com.lge.camera.app;

import android.view.MotionEvent;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.ManualDefaultControlManager;
import com.lge.camera.managers.ManualViewManager;

public class ManualDefaultCameraModule extends ManualCameraGraphyModule {
    private boolean mIsMenuShowing = false;

    public ManualDefaultCameraModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void initManualManager() {
        this.mManualViewManager = new ManualViewManager(this);
        this.mManualControlManager = new ManualDefaultControlManager(this);
        super.initManualManager();
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        this.mIsMenuShowing = false;
    }

    public String getShotMode() {
        return CameraConstants.MODE_MANUAL_CAMERA;
    }

    public void onDrumVisibilityChanged(int type, boolean isVisible) {
        boolean z = true;
        super.onDrumVisibilityChanged(type, isVisible);
        if (!(this.mIsMenuShowing || isZoomBarVisible())) {
            setGraphyButtonVisiblity(!isVisible);
        }
        if (isVisible) {
            if (isVisible) {
                z = false;
            }
            setGraphyListVisibility(z, false);
            setEVGuideLayoutVisibility(false, false);
        }
    }

    public boolean onShowMenu(int menuType) {
        this.mIsMenuShowing = true;
        setGraphyListVisibility(false, false);
        setGraphyButtonVisiblity(false);
        return super.onShowMenu(menuType);
    }

    public boolean onHideMenu(int menuType) {
        this.mIsMenuShowing = false;
        setGraphyButtonVisiblity(true);
        if (menuType == 1 && this.mGraphyListOn) {
            setGraphyListVisibility(true, false);
            this.mGraphyListOn = false;
        }
        return super.onHideMenu(menuType);
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        super.doCleanView(doByAction, useAnimation, saveState);
        if (this.mGraphyViewManager != null && !this.mGet.isModuleChanging()) {
            if (!isEnableManualView()) {
                this.mGraphyViewManager.hideGraphyViewTransiently();
            } else if (this.mZoomManager != null && !this.mZoomManager.isInAndOutSwithing()) {
                this.mGraphyViewManager.restoreGraphyView();
            }
        }
    }

    public void onChangeZoomMinimapVisibility(boolean show) {
        super.onChangeZoomMinimapVisibility(show);
        if (this.mGraphyViewManager != null) {
            if (show) {
                this.mGraphyViewManager.hideGraphyViewTransiently();
            } else if (isEnableManualView()) {
                this.mGraphyViewManager.restoreGraphyView();
            }
        }
    }

    public String getISOParamKey() {
        return super.getISOParamKey();
    }

    public String getShutterSpeedParamKey() {
        return super.getShutterSpeedParamKey();
    }

    public String convertISOParamValue(String iso) {
        return super.convertISOParamValue(iso);
    }

    public void onZoomShow() {
        super.onZoomShow();
        setGraphyButtonVisiblity(false);
        setGraphyListVisibility(false, false);
        setEVGuideLayoutVisibility(false, false);
    }

    public void onZoomHide() {
        super.onZoomHide();
        if (!this.mIsMenuShowing) {
            setGraphyButtonVisiblity(true);
        }
        setGraphyListVisibility(false, false);
        setEVGuideLayoutVisibility(false, false);
    }

    protected boolean displayUIComponentAfterOneShot() {
        if (!super.displayUIComponentAfterOneShot()) {
            return false;
        }
        setGraphyButtonVisiblity(true);
        return true;
    }

    public void onWideAngleButtonClicked() {
        super.onWideAngleButtonClicked();
        if (!this.mIsAngleChangedByGraphy) {
            if (this.mGraphyViewManager != null && this.mGraphyViewManager.isGraphyListVisible()) {
                setGraphyListVisibility(false, true);
            }
            setEVGuideLayoutVisibility(false, false);
        }
        this.mIsAngleChangedByGraphy = false;
    }

    public void onNormalAngleButtonClicked() {
        super.onNormalAngleButtonClicked();
        if (!this.mIsAngleChangedByGraphy) {
            if (this.mGraphyViewManager != null && this.mGraphyViewManager.isGraphyListVisible()) {
                setGraphyListVisibility(false, true);
            }
            setEVGuideLayoutVisibility(false, false);
        }
        this.mIsAngleChangedByGraphy = false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mGraphyViewManager == null || !this.mGraphyViewManager.isGraphyListVisible() || event.getActionMasked() != 1) {
            return super.onTouchEvent(event);
        }
        setGraphyListVisibility(false, true);
        setEVGuideLayoutVisibility(false, false);
        return true;
    }

    public void onShutterBottomButtonClickListener() {
        if (this.mGraphyViewManager != null && this.mGraphyViewManager.isGraphyListVisible()) {
            setGraphyListVisibility(false, false);
            setEVGuideLayoutVisibility(false, false);
        }
        super.onShutterBottomButtonClickListener();
    }

    protected void doOnQuickViewShown(boolean isAutoReview) {
        if (this.mGraphyViewManager != null && this.mGraphyViewManager.isGraphyListVisible()) {
            setGraphyListVisibility(false, false);
            setEVGuideLayoutVisibility(false, false);
        }
        super.doOnQuickViewShown(isAutoReview);
    }

    public boolean doBackKey() {
        boolean result = false;
        if (this.mGraphyViewManager != null) {
            if (this.mGraphyViewManager.isGraphyListVisible() && !this.mGraphyViewManager.isGraphyListAnimaitonShowing()) {
                this.mGraphyViewManager.setGraphyListVisibility(false, true);
                result = true;
            }
            if (this.mGraphyViewManager.isDetailviewVisible()) {
                this.mGraphyViewManager.hideDetailView();
                result = true;
            }
        }
        if (result) {
            return true;
        }
        return super.doBackKey();
    }

    public boolean isIndicatorSupported(int indicatorId) {
        switch (indicatorId) {
            case C0088R.id.indicator_item_cheese_shutter_or_timer:
            case C0088R.id.indicator_item_hdr_or_flash:
            case C0088R.id.indicator_item_raw:
                return true;
            default:
                return false;
        }
    }
}
