package com.lge.camera.app;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.BarView;
import com.lge.camera.components.BeautyshotBar;
import com.lge.camera.components.ComponentInterface;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.AdvancedSelfieManager;
import com.lge.camera.managers.BeautyShotManager;
import com.lge.camera.managers.QuickclipManagerIF.onQuickClipListListener;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.TalkBackUtil;

public class BeautyShotCameraModuleBase extends GestureShotCameraModule implements ComponentInterface {
    protected static final int BEAUTY_PIC_STATE = 3;
    protected static final int BEAUTY_REC_STATE_IDLE = 0;
    protected static final int BEAUTY_REC_STATE_REC = 1;
    protected static final int BEAUTY_REC_STATE_REC_AFTER = 2;
    protected View mAdvancedSelfieView = null;
    protected BeautyShotManager mBeautyManager = BeautyShotManager.NULL;
    protected View mBeautyShotView = null;
    protected boolean mIsOneShotPreviewDone = false;

    /* renamed from: com.lge.camera.app.BeautyShotCameraModuleBase$1 */
    class C02601 implements onQuickClipListListener {
        C02601() {
        }

        public void onListOpend() {
            BeautyShotCameraModuleBase.this.pauseShutterless();
            BeautyShotCameraModuleBase.this.showCommandArearUI(false);
            if (!BeautyShotCameraModuleBase.this.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                if (BeautyShotCameraModuleBase.this.mColorEffectManager != null) {
                    BeautyShotCameraModuleBase.this.mColorEffectManager.hideMenu(false);
                }
                BeautyShotCameraModuleBase.this.mAdvancedFilmManager.setSelfieOptionVisibility(false, true);
                BeautyShotCameraModuleBase.this.mAdvancedFilmManager.hideSelfieMenuTransient();
                BeautyShotCameraModuleBase.this.hideFocusOnShowOtherBars(false);
            }
            if (BeautyShotCameraModuleBase.this.isActivatedQuickdetailView()) {
                BeautyShotCameraModuleBase.this.setDeleteButtonVisibility(false);
            }
        }

        public void onListClosed() {
            if (!BeautyShotCameraModuleBase.this.isSquareGalleryBtn() || BeautyShotCameraModuleBase.this.mHandler == null) {
                BeautyShotCameraModuleBase.this.resumeShutterless();
            } else {
                BeautyShotCameraModuleBase.this.mHandler.sendEmptyMessageDelayed(93, CameraConstants.TOAST_LENGTH_SHORT);
            }
            BeautyShotCameraModuleBase.this.showCommandArearUI(true);
            if (!BeautyShotCameraModuleBase.this.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                BeautyShotCameraModuleBase.this.mAdvancedFilmManager.restoreSelfieMenuVisibility();
                if (!(BeautyShotCameraModuleBase.this.isBarVisible(1) || BeautyShotCameraModuleBase.this.isMenuShowing(CameraConstants.MENU_TYPE_ALL))) {
                    BeautyShotCameraModuleBase.this.showFocusOnHideOtherBars();
                }
            }
            if (BeautyShotCameraModuleBase.this.isActivatedQuickdetailView()) {
                BeautyShotCameraModuleBase.this.setDeleteButtonVisibility(true);
            }
        }
    }

    public BeautyShotCameraModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void doShowGestureGuide() {
        setBarVisible(false);
        super.doShowGestureGuide();
    }

    public boolean isColorEffectSupported() {
        return isRearCamera() ? super.isColorEffectSupported() : false;
    }

    protected void initializeControls(boolean enable) {
        super.initializeControls(enable);
        if (this.mBeautyShotView == null) {
            this.mBeautyShotView = inflateView(C0088R.layout.beautyshot);
            ViewGroup vg = (ViewGroup) findViewById(C0088R.id.camera_controls);
            if (!(vg == null || this.mBeautyShotView == null)) {
                vg.addView(this.mBeautyShotView, 0, new LayoutParams(-1, -1));
            }
        }
        if (this.mBeautyManager.isBeautyOn()) {
            initBeautyshotBar();
            return;
        }
        this.mBarManager.initBar(1);
        this.mBarManager.initBar(4);
        this.mBarManager.setBarListener(1, this);
        this.mBarManager.setVisible(1, false, true);
    }

    protected void setQuickClipListListener() {
        if (isSupportedQuickClip()) {
            this.mQuickClipManager.setQuickClipListListener(new C02601());
        }
    }

    public boolean setBarSetting(String key, String value, boolean save) {
        if (Setting.KEY_BEAUTYSHOT.equals(key) || Setting.KEY_RELIGHTING.equals(key)) {
            setBeautyBarSetting(key, value, save);
        } else {
            super.setBarSetting(key, value, save);
        }
        return true;
    }

    protected boolean setBeautyBarSetting(String key, String value, boolean save) {
        if (!checkModuleValidate(1) || !this.mBeautyManager.isBeautyOn()) {
            return false;
        }
        int beautyLevel = checkEffectLevel(value);
        sendBeautyTalkbackEvent(key, beautyLevel);
        setBeautyLevel(key, beautyLevel);
        if (save) {
            this.mGet.setForcedSetting(key, value);
        } else {
            this.mGet.setSetting(key, String.valueOf(beautyLevel), save);
            updateBeautyParam(key);
        }
        return true;
    }

    private void sendBeautyTalkbackEvent(String key, int beautyLevel) {
        if (beautyLevel != this.mBeautyManager.getBeautyLevel()) {
            int resId = C0088R.string.tone_level;
            if (Setting.KEY_BEAUTYSHOT.equals(key)) {
                resId = C0088R.string.tone_level;
            } else if (Setting.KEY_RELIGHTING.equals(key)) {
                resId = C0088R.string.relighting_level;
            }
            TalkBackUtil.sendAccessibilityEvent(getAppContext(), getClass().getName(), String.format(getAppContext().getString(resId), new Object[]{Integer.valueOf(beautyLevel)}));
        }
    }

    private void setBeautyLevel(String key, int beautyLevel) {
        if (Setting.KEY_BEAUTYSHOT.equals(key)) {
            this.mBeautyManager.setBeautyLevel(beautyLevel);
        } else if (Setting.KEY_RELIGHTING.equals(key)) {
            this.mBeautyManager.setBeautyRelightingLevel(beautyLevel);
        }
    }

    private void updateBeautyParam(String key) {
        if (this.mBeautyManager == null) {
            return;
        }
        if (Setting.KEY_BEAUTYSHOT.equals(key)) {
            this.mParamUpdater = this.mBeautyManager.setBeautyParam(this.mParamUpdater);
        } else if (Setting.KEY_RELIGHTING.equals(key)) {
            this.mParamUpdater = this.mBeautyManager.setBeautyRelightingParam(this.mParamUpdater);
        }
    }

    protected int checkEffectLevel(String value) {
        int retValue = Integer.valueOf(value).intValue();
        CamLog.m3d(CameraConstants.TAG, "setEffectLevel value" + retValue);
        if (retValue < 0) {
            return 0;
        }
        if (retValue > 100) {
            return 100;
        }
        return retValue;
    }

    public void initBeautyshotBar() {
        if (this.mBeautyShotView != null && this.mBarManager != null && this.mBeautyManager.isBeautyOn()) {
            this.mBarManager.initBar(1);
            this.mBarManager.setBarListener(1, this);
            this.mBarManager.setBarMaxValue(1, 100);
            BarView barView = this.mBarManager.getBar(1);
            if (barView != null) {
                barView.refreshBar();
            }
        }
    }

    public void setBarVisible(final boolean bVisible) {
        if (this.mBeautyManager.isBeautyOn() && this.mBarManager != null && this.mBeautyShotView != null) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    boolean bSet = BeautyShotCameraModuleBase.this.mIsOneShotPreviewDone ? bVisible : false;
                    if (bSet) {
                        AdvancedSelfieManager advancedSelfieManager = BeautyShotCameraModuleBase.this.mAdvancedFilmManager;
                        if (!AdvancedSelfieManager.sFilterOptionVisible) {
                            BeautyShotCameraModuleBase.this.mBarManager.setVisible(1, bSet, true);
                        }
                    }
                    BeautyshotBar beautyBarView = (BeautyshotBar) BeautyShotCameraModuleBase.this.mBarManager.getBar(1);
                    if (!bSet && beautyBarView != null) {
                        beautyBarView.setTextLevelVisibility(bSet);
                    }
                }
            });
        }
    }

    public void setBarEnable(boolean bEnable) {
        if (!isRearCamera() && this.mBeautyManager.isBeautyOn()) {
            this.mBarManager.setEnable(1, bEnable);
        }
    }

    public void setBarEnable(boolean bEnable, boolean changedColor) {
        if (changedColor) {
            setBarEnable(bEnable);
        } else if (!isRearCamera() && this.mBeautyManager.isBeautyOn()) {
            BeautyshotBar beautyBarView = (BeautyshotBar) this.mBarManager.getBar(1);
            if (beautyBarView != null) {
                beautyBarView.setBarEnable(bEnable, false);
            }
        }
    }

    public void onZoomShow() {
        super.onZoomShow();
        if (getCameraState() != 6) {
            setBarVisible(false);
        }
    }

    protected void hideManagerForSelfTimer(boolean keepState) {
        boolean z;
        if (isFastShotAvailable(3)) {
            z = false;
        } else {
            z = true;
        }
        setBarEnable(false, z);
        super.hideManagerForSelfTimer(keepState);
    }

    protected boolean isHandlerSwitchingModule() {
        return true;
    }

    public String getShotMode() {
        return "mode_beauty=" + this.mBeautyManager.getBeautyStrength();
    }

    protected void onCameraSwitchingStart() {
        if (!isRearCamera()) {
            setPreviewCallbackAll(false);
        }
        setBarVisible(false);
        super.onCameraSwitchingStart();
    }

    public void removeUIBeforeModeChange() {
        super.removeUIBeforeModeChange();
        setBarVisible(false);
    }
}
