package com.lge.camera.app.ext;

import android.net.Uri;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.app.SquareSnapCameraModule;
import com.lge.camera.managers.ext.OverlapCameraInitGuideManager;
import com.lge.camera.managers.ext.OverlapPreviewManagerBase;
import com.lge.camera.managers.ext.OverlapPreviewManagerInterface;
import com.lge.camera.managers.ext.OverlapProjectManagerBase;
import com.lge.camera.managers.ext.OverlapProjectManagerInterface;
import com.lge.camera.managers.ext.SquareCameraInitGuideManagerBase.InitGuideListener;
import com.lge.camera.settings.Setting;

public abstract class SquareOverlapCameraModuleBase extends SquareSnapCameraModule implements OverlapProjectManagerInterface, OverlapPreviewManagerInterface, InitGuideListener {
    public static final int CAPTURE_MODE_PROJECT = 0;
    public static final int CAPTURE_MODE_SAMPLE = 1;
    protected static final int PROJECT_LIST = 0;
    protected static final int SNAP_VIEW = 1;
    protected int mCaptureMode = 0;
    protected int mCurrentState = 0;
    protected OverlapCameraInitGuideManager mIniGuideManager = new OverlapCameraInitGuideManager(this);
    protected boolean mIsFirstCaptured = false;
    boolean mOnShowMenuFilm = false;
    protected OverlapPreviewManagerBase mOverlapPreviewManager = new OverlapPreviewManagerBase(this);
    protected OverlapProjectManagerBase mOverlapProjectManager = new OverlapProjectManagerBase(this);

    public SquareOverlapCameraModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public boolean isSquareInitGuideShowing() {
        return this.mIniGuideManager == null ? false : this.mIniGuideManager.getGuideLayouVisiblity();
    }

    protected void doIntervalShot() {
        super.doIntervalShot();
        hidePreviewGuide();
    }

    protected void stopIntervalShot(int delay) {
        super.stopIntervalShot(delay);
        showPreviewGuide();
    }

    public boolean onShowMenu(int menuType) {
        if (!super.onShowMenu(menuType) || this.mOverlapPreviewManager == null) {
            return false;
        }
        this.mOverlapProjectManager.setVisible(true);
        this.mOverlapProjectManager.removeSlideShow();
        this.mOnShowMenuFilm = false;
        hidePreviewGuide();
        this.mOverlapPreviewManager.setVisible(false);
        return true;
    }

    public boolean onHideMenu(int menuType) {
        boolean retValue = super.onHideMenu(menuType);
        if (this.mCurrentState == 0) {
            this.mOverlapProjectManager.setVisible(true);
            this.mGalleryManager.showGalleryView(false);
        }
        if (this.mAdvancedFilmManager.getFilterViewVisibility() != 0) {
            this.mOverlapPreviewManager.setVisible(true);
        }
        showPreviewGuide();
        this.mOnShowMenuFilm = false;
        return retValue;
    }

    protected void doShowGestureGuide() {
        super.doShowGestureGuide();
        hidePreviewGuide();
    }

    protected void doHideGestureGuide() {
        super.doHideGestureGuide();
        showPreviewGuide();
    }

    public void showFaceViewVisible(boolean isShow) {
        boolean z = false;
        super.showFaceViewVisible(isShow);
        if (isShow || !isAvailableGuideBar()) {
            this.mOverlapPreviewManager.setGuideTextVisible(false);
            return;
        }
        OverlapPreviewManagerBase overlapPreviewManagerBase = this.mOverlapPreviewManager;
        if (!this.mIsFirstCaptured) {
            z = true;
        }
        overlapPreviewManagerBase.setGuideTextVisible(z);
    }

    private boolean isAvailableGuideBar() {
        if (getGestureVisibility() || isZoomBarVisible() || this.mBarManager.isBarVisible(1) || this.mBarManager.isBarVisible(4) || this.mAdvancedFilmManager.getFilterViewVisibility() == 0 || this.mIntervalShotTimer != null) {
            return false;
        }
        return true;
    }

    protected void showPreviewGuide() {
        boolean z = true;
        if (isAvailableGuideBar()) {
            this.mOverlapPreviewManager.setGuideSeekbarVisible(true);
            OverlapPreviewManagerBase overlapPreviewManagerBase = this.mOverlapPreviewManager;
            if (this.mIsFirstCaptured) {
                z = false;
            }
            overlapPreviewManagerBase.setGuideTextVisible(z);
        }
    }

    protected void hidePreviewGuide() {
        this.mOverlapPreviewManager.setGuideSeekbarVisible(false);
        this.mOverlapPreviewManager.setGuideTextVisible(false);
    }

    public void setBarVisible(int barType, boolean show, boolean enable) {
        super.setBarVisible(barType, show, enable);
        if (barType != 1 && barType != 4) {
            return;
        }
        if (show || this.mAdvancedFilmManager.isSelectedMenuType() != 0) {
            hidePreviewGuide();
            if (this.mAdvancedFilmManager.isSelectedMenuType() == 2) {
                this.mOverlapPreviewManager.setVisible(false);
                return;
            }
            return;
        }
        this.mOverlapPreviewManager.setVisible(true);
        showPreviewGuide();
    }

    protected void showZoomBar() {
        super.showZoomBar();
        hidePreviewGuide();
    }

    public void hideZoomBar() {
        super.hideZoomBar();
        showPreviewGuide();
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        super.doCleanView(doByAction, useAnimation, saveState);
        if (doByAction) {
            hidePreviewGuide();
        } else {
            showPreviewGuide();
        }
    }

    protected boolean isAvailableSteadyCam() {
        return false;
    }

    protected void initializeSettingMenus() {
        super.initializeSettingMenus();
        setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, "off", false);
        setSettingMenuEnable(Setting.KEY_SQUARE_VIDEO_SIZE, false);
    }

    protected void restoreSettingMenus() {
        super.restoreSettingMenus();
        restoreSettingValue(Setting.KEY_VIDEO_STEADY);
        setSettingMenuEnable(Setting.KEY_SQUARE_VIDEO_SIZE, true);
    }

    protected boolean checkUri(Uri changeUri) {
        return (changeUri.toString().contains("orig_id") && changeUri.toString().contains("group_id")) ? false : true;
    }

    protected boolean isAEAFLockSupportedMode() {
        return false;
    }

    protected boolean isAEControlSupportedMode() {
        return false;
    }
}
