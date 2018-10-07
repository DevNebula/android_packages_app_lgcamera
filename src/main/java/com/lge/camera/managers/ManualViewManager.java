package com.lge.camera.managers;

import android.content.Context;
import android.graphics.ColorFilter;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.DrumBarController;
import com.lge.camera.components.DrumController;
import com.lge.camera.components.DrumControllerListener;
import com.lge.camera.components.DrumItem;
import com.lge.camera.components.ManualPanelButton;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.file.ExifInterface.GpsLatitudeRef;
import com.lge.camera.managers.ManualControlManager.ManualControlCallback;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.ManualModeItem;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;

public class ManualViewManager extends ManualViewBaseManager implements ManualControlCallback {
    protected Handler mMainHandler = new MainHandler();
    private int mSpinEffectIdx = 0;

    /* renamed from: com.lge.camera.managers.ManualViewManager$10 */
    class C105310 implements OnClickListener {
        C105310() {
        }

        public void onClick(View arg0) {
            if (ManualViewManager.this.mGet.checkModuleValidate(80)) {
                ManualViewManager.this.mIsLogDisplayLUT = !ManualViewManager.this.mIsLogDisplayLUT;
                ManualViewManager.this.setLogDisplayLUT(ManualViewManager.this.mIsLogDisplayLUT);
                LdbUtil.sendLDBIntent(ManualViewManager.this.getAppContext(), LdbConstants.LDB_FEATURE_NAME_DEFAULT_LUT, -1, Boolean.toString(ManualViewManager.this.mIsLogDisplayLUT));
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "-log profile- ignored DisplayLUT");
        }
    }

    /* renamed from: com.lge.camera.managers.ManualViewManager$11 */
    class C105411 implements OnClickListener {
        C105411() {
        }

        public void onClick(View arg0) {
            ManualViewManager.this.mDrumBtnAWB.setVisibility(8);
            ManualViewManager.this.onAutoButtonClicked(4);
            ManualViewManager.this.mManualGet.setManualData(4, "lg-wb", "0", true);
            ManualViewManager.this.mManualGet.setLockedFeature();
        }
    }

    /* renamed from: com.lge.camera.managers.ManualViewManager$12 */
    class C105512 implements OnClickListener {
        C105512() {
        }

        public void onClick(View arg0) {
            ManualViewManager.this.mDrumBtnAF.setVisibility(8);
            ManualViewManager.this.onAutoButtonClicked(16);
            ManualViewManager.this.mManualGet.setManualData(16, ParamConstants.MANUAL_FOCUS_STEP, "-1", true);
            ManualViewManager.this.mManualGet.setLockedFeature();
        }
    }

    /* renamed from: com.lge.camera.managers.ManualViewManager$13 */
    class C105613 implements OnClickListener {
        C105613() {
        }

        public void onClick(View arg0) {
            boolean peakingOn = !SharedPreferenceUtil.getFocusPeakingEnable(ManualViewManager.this.getAppContext());
            ManualViewManager.this.setFocusPeaking(16, peakingOn);
            LdbUtil.sendLDBIntent(ManualViewManager.this.getAppContext(), LdbConstants.LDB_FEATURE_NAME_FOCUS_PEAKING, -1, Boolean.toString(peakingOn));
        }
    }

    /* renamed from: com.lge.camera.managers.ManualViewManager$14 */
    class C105714 implements OnClickListener {
        C105714() {
        }

        public void onClick(View arg0) {
            ManualViewManager.this.mDrumBtnISO.setVisibility(8);
            ManualViewManager.this.onAutoButtonClicked(8);
            ManualViewManager.this.setButtonLocked(false, 9);
            ManualViewManager.this.mManualGet.setAELock(Boolean.valueOf(false));
            ManualViewManager.this.mManualGet.setManualData(8, ManualViewManager.this.mManualGet.getISOParamKey(), ManualViewManager.this.mManualGet.convertISOParamValue("auto"), true);
            ManualViewManager.this.mManualGet.setLockedFeature();
        }
    }

    /* renamed from: com.lge.camera.managers.ManualViewManager$15 */
    class C105815 implements OnClickListener {
        C105815() {
        }

        public void onClick(View arg0) {
            ManualViewManager.this.mDrumBtnSS.setVisibility(8);
            ManualViewManager.this.onAutoButtonClicked(1);
            ManualViewManager.this.setButtonLocked(false, 9);
            ManualViewManager.this.mManualGet.setAELock(Boolean.valueOf(false));
            ManualViewManager.this.mManualGet.setManualData(1, ManualViewManager.this.mManualGet.getShutterSpeedParamKey(), ManualViewManager.this.mManualGet.convertShutterSpeedParamValue("0"), true);
            ManualViewManager.this.mManualGet.setLockedFeature();
        }
    }

    /* renamed from: com.lge.camera.managers.ManualViewManager$16 */
    class C105916 implements DrumControllerListener {
        C105916() {
        }

        public void onItemSelected(DrumItem item, boolean updateDrum) {
            if (item != null && !ManualViewManager.this.isDrumControlUnavailable() && ManualViewManager.this.mWhiteBalance.getVisibility() == 0) {
                ManualViewManager.this.mManualGet.setManualData(4, item.getKey(), item.getValue(), false);
            }
        }

        public void playDrumEffectSound() {
            ManualViewManager.this.mManualGet.playSound(11, false, 0);
        }

        public void onDrumScrollReleased(DrumItem item) {
            ManualViewManager.this.onScrollReleased(4, item.getValue());
        }

        public boolean isAvailableToMoveDrum() {
            return ManualViewManager.this.mManualGet.isDrumMovingAvailable();
        }
    }

    /* renamed from: com.lge.camera.managers.ManualViewManager$17 */
    class C106017 implements DrumControllerListener {
        C106017() {
        }

        public void onItemSelected(DrumItem item, boolean updateWheel) {
            if (item != null && !ManualViewManager.this.isDrumControlUnavailable() && ManualViewManager.this.mManualFocus.getVisibility() == 0) {
                ManualViewManager.this.mManualGet.setManualData(16, item.getKey(), item.getValue(), false);
            }
        }

        public void playDrumEffectSound() {
            ManualViewManager.this.mManualGet.playSound(11, false, 0);
        }

        public void onDrumScrollReleased(DrumItem item) {
            ManualViewManager.this.onScrollReleased(16, item.getValue());
        }

        public boolean isAvailableToMoveDrum() {
            return ManualViewManager.this.mManualGet.isDrumMovingAvailable();
        }
    }

    /* renamed from: com.lge.camera.managers.ManualViewManager$18 */
    class C106118 implements DrumControllerListener {
        C106118() {
        }

        public void onItemSelected(DrumItem item, boolean updateWheel) {
            if (item != null && !ManualViewManager.this.isDrumControlUnavailable() && ManualViewManager.this.mExposureValue.getVisibility() == 0) {
                ManualViewManager.this.mManualGet.setManualData(2, item.getKey(), item.getValue(), false);
            }
        }

        public void playDrumEffectSound() {
            ManualViewManager.this.mManualGet.playSound(11, false, 0);
        }

        public void onDrumScrollReleased(DrumItem item) {
            ManualViewManager.this.onScrollReleased(2, item.getValue());
        }

        public boolean isAvailableToMoveDrum() {
            return ManualViewManager.this.mManualGet.isDrumMovingAvailable();
        }
    }

    /* renamed from: com.lge.camera.managers.ManualViewManager$19 */
    class C106219 implements DrumControllerListener {
        C106219() {
        }

        public void onItemSelected(DrumItem item, boolean updateWheel) {
            if (item != null && !ManualViewManager.this.isDrumControlUnavailable() && ManualViewManager.this.mISO.getVisibility() == 0) {
                ManualViewManager.this.mManualGet.setManualData(8, item.getKey(), item.getValue(), false);
            }
        }

        public void playDrumEffectSound() {
            ManualViewManager.this.mManualGet.playSound(11, false, 0);
        }

        public void onDrumScrollReleased(DrumItem item) {
            ManualViewManager.this.onScrollReleased(8, item.getValue());
        }

        public boolean isAvailableToMoveDrum() {
            return ManualViewManager.this.mManualGet.isDrumMovingAvailable();
        }
    }

    /* renamed from: com.lge.camera.managers.ManualViewManager$20 */
    class C106420 implements DrumControllerListener {
        C106420() {
        }

        public void onItemSelected(DrumItem item, boolean updateWheel) {
            if (item != null && !ManualViewManager.this.isDrumControlUnavailable() && ManualViewManager.this.mShutterSpeed.getVisibility() == 0) {
                ManualViewManager.this.mManualGet.setManualData(1, item.getKey(), item.getValue(), false);
            }
        }

        public void playDrumEffectSound() {
            ManualViewManager.this.mManualGet.playSound(11, false, 0);
        }

        public void onDrumScrollReleased(DrumItem item) {
            ManualViewManager.this.onScrollReleased(1, item.getValue());
        }

        public boolean isAvailableToMoveDrum() {
            return ManualViewManager.this.mManualGet.isDrumMovingAvailable();
        }
    }

    /* renamed from: com.lge.camera.managers.ManualViewManager$3 */
    class C10683 implements OnClickListener {
        C10683() {
        }

        public void onClick(View arg0) {
            final ManualDataWB data = (ManualDataWB) ManualViewManager.this.mManualGet.getManualData(4);
            if (data != null && ManualViewManager.this.onManualButtonClicked(4, data)) {
                ManualViewManager.this.sendItemClickRunnable(new Runnable() {
                    public void run() {
                        ManualModeItem wbItem = ManualViewManager.this.getManualModeItem(4);
                        if (wbItem != null) {
                            data.makeColorValues(wbItem.getBarColors());
                            ManualViewManager.this.mWhiteBalance.refreshBarColors(wbItem.getBarColors());
                        }
                    }
                });
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ManualViewManager$4 */
    class C10704 implements OnClickListener {
        C10704() {
        }

        public void onClick(View arg0) {
            ManualViewManager.this.onManualButtonClicked(16, ManualViewManager.this.mManualGet.getManualData(16));
        }
    }

    /* renamed from: com.lge.camera.managers.ManualViewManager$5 */
    class C10715 implements OnClickListener {
        C10715() {
        }

        public void onClick(View arg0) {
            ManualViewManager.this.onManualButtonClicked(2, ManualViewManager.this.mManualGet.getManualData(2));
        }
    }

    /* renamed from: com.lge.camera.managers.ManualViewManager$6 */
    class C10726 implements OnClickListener {
        C10726() {
        }

        public void onClick(View arg0) {
            ManualViewManager.this.onManualButtonClicked(8, ManualViewManager.this.mManualGet.getManualData(8));
        }
    }

    /* renamed from: com.lge.camera.managers.ManualViewManager$7 */
    class C10737 implements OnClickListener {
        C10737() {
        }

        public void onClick(View arg0) {
            ManualViewManager.this.onManualButtonClicked(1, ManualViewManager.this.mManualGet.getManualData(1));
        }
    }

    /* renamed from: com.lge.camera.managers.ManualViewManager$8 */
    class C10748 implements OnClickListener {
        C10748() {
        }

        public void onClick(View arg0) {
            if (ManualViewManager.this.isItemPanelWorkable(31)) {
                ManualViewManager.this.setButtonAndDrumUnselected(0);
                ManualViewManager.this.setButtonLocked(false, 31);
                if (ManualViewManager.this.mGet != null) {
                    ManualViewManager.this.mGet.setFilmStrengthButtonVisibility(true, false);
                }
                ManualViewManager.this.mManualGet.setAELock(Boolean.valueOf(false));
                ManualViewManager.this.mManualGet.setManualData(4, "lg-wb", "0", true);
                ManualViewManager.this.mManualGet.setManualData(16, ParamConstants.MANUAL_FOCUS_STEP, "-1", true);
                ManualViewManager.this.mManualGet.setLockedFeature();
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ManualViewManager$9 */
    class C10759 implements OnClickListener {
        C10759() {
        }

        public void onClick(View arg0) {
            if (ManualViewManager.this.isItemPanelWorkable(31)) {
                ManualViewManager.this.setButtonAndDrumUnselected(0);
                boolean isAELocked = ManualViewManager.this.mManualGet.getAELock();
                if (isAELocked) {
                    ManualViewManager.this.setButtonLocked(false, 9);
                    ManualViewManager.this.mBtnAELock.setContentDescription(ManualViewManager.this.mGet.getAppContext().getString(C0088R.string.ae_lock_off));
                } else {
                    ManualViewManager.this.setButtonLocked(true, 11);
                    ManualViewManager.this.mBtnAELock.setContentDescription(ManualViewManager.this.mGet.getAppContext().getString(C0088R.string.ae_lock_on));
                }
                if (isAELocked) {
                    isAELocked = false;
                } else {
                    isAELocked = true;
                }
                ManualViewManager.this.mManualGet.setAELock(Boolean.valueOf(isAELocked));
                ManualViewManager.this.mBtnAELock.setSelected(isAELocked);
                if (!isAELocked) {
                    ManualViewManager.this.mBtnAELock.sendAccessibilityEvent(4);
                }
            }
        }
    }

    protected class MainHandler extends Handler {
        protected MainHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg != null) {
                ManualViewManager.this.mainHandleMessage(msg.what, msg.arg1);
            }
        }
    }

    public ManualViewManager(ManualModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    protected void mainHandleMessage(int message, int key) {
        ManualPanelButton panelButton;
        switch (message) {
            case 0:
                panelButton = (ManualPanelButton) this.mPanelButtonMap.get(Integer.valueOf(key));
                ManualModeItem item = getManualModeItem(key);
                if (item != null && panelButton != null) {
                    if (key == 1 || (FunctionProperties.getSupportedHal() != 2 && key == 8)) {
                        if (this.mSpinEffectIdx >= item.mEntries.length - 1) {
                            this.mSpinEffectIdx = 0;
                        } else {
                            this.mSpinEffectIdx++;
                        }
                        updateButtonValue(key, item.mEntries[this.mSpinEffectIdx]);
                        Message msg = new Message();
                        msg.what = 0;
                        msg.arg1 = key;
                        this.mMainHandler.sendMessageDelayed(msg, 30);
                        return;
                    }
                    return;
                }
                return;
            case 1:
                this.mMainHandler.removeMessages(0);
                for (Integer intValue : this.mPanelButtonMap.keySet()) {
                    updateButtonValue(intValue.intValue());
                }
                return;
            case 2:
                panelButton = (ManualPanelButton) this.mPanelButtonMap.get(Integer.valueOf(key));
                Context context = this.mGet.getAppContext();
                if (panelButton != null && context != null) {
                    panelButton.setTextColor(context.getColor(C0088R.color.camera_white_txt));
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        if (!this.mInit) {
            initAllViews();
            this.mInit = true;
        }
        updateViews();
    }

    public void initAllViews() {
        setupViews();
        CameraProxy camera = this.mManualGet.getCameraDevice();
        if (camera == null) {
            initViewsInternal();
            return;
        }
        CameraParameters parameter = camera.getParameters();
        if (parameter != null) {
            this.mManualGet.loadManualData(parameter);
            buildManualItem(parameter, this.mManualGet.getSupportedFeature());
            makeSettingKeyList();
            initViewsInternal();
        }
    }

    private void initViewsInternal() {
        createManualDrums();
        makePanelButtonMap();
        makeDrumMap();
        makeAutoButtonMap();
    }

    public void updateViews() {
        if (updateGuideViewRatio()) {
            setManualPanelbottomMargin(true);
        } else {
            setManualPanelbottomMargin(false);
        }
        setButtonsEnabled(true);
        setDegree(this.mGet.getOrientationDegree(), false);
        updateManualPannel();
    }

    public void updateManualPannel() {
        setApertureNumber();
        setButtonAndDrumUnselected(31);
        updateAllButtonValue();
    }

    public void changeLocalModule() {
        makeSettingKeyList();
        setPanelVisibility(0);
        updateRatioGuideVisibility(0);
    }

    public void updateButtonValue(int type) {
        ManualData value = this.mManualGet.getManualData(type);
        if (value != null) {
            if ((this.mManualGet.getEnabledFeature() & type) == 0) {
                updateButtonValue(type, value.getDisabledInfoValue());
            } else if (value.isDefaultValue()) {
                updateButtonValue(type, value.getAutoInfoValue());
            } else {
                updateButtonValue(type, value.getUserInfoValue());
            }
        }
    }

    protected void setupViews() {
        LayoutInflater mInflater = LayoutInflater.from(this.mGet.getAppContext());
        this.mManualBaseView = this.mManualGet.inflateView(C0088R.layout.manual_base);
        ViewGroup vg = (ViewGroup) this.mGet.getActivity().findViewById(C0088R.id.contents_base);
        if (vg != null && this.mManualBaseView != null) {
            vg.addView(this.mManualBaseView);
            this.mManualBasePanelView = (RotateLayout) vg.findViewById(C0088R.id.manual_base_panel);
            this.mManualPanelView = this.mManualGet.inflateView(C0088R.layout.manual_panel);
            if (this.mManualBasePanelView != null && this.mManualPanelView != null) {
                this.mManualBasePanelView.addView(this.mManualPanelView);
                this.mManualBasePanelView.rotateLayout(270);
                LinearLayout ll = (LinearLayout) this.mManualBasePanelView.findViewById(C0088R.id.manual_panel_left);
                if (ll != null) {
                    LayoutParams lp = (LayoutParams) ll.getLayoutParams();
                    if (lp != null) {
                        int additionalMargin = 0;
                        if (ModelProperties.isLongLCDModel()) {
                            if (ManualUtil.isManualCameraMode(this.mGet.getShotMode()) || ModelProperties.getLCDType() == 2) {
                                additionalMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.07639f);
                            } else if (ManualUtil.isManualVideoMode(this.mGet.getShotMode())) {
                                additionalMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.1055f);
                            }
                        }
                        lp.setMarginStart(RatioCalcUtil.getQuickButtonWidth(getAppContext()) + additionalMargin);
                        if (ModelProperties.isLongLCDModel()) {
                            lp.height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.10277f);
                        }
                        ll.setLayoutParams(lp);
                        RotateLayout tempVg = (RotateLayout) vg.findViewById(C0088R.id.manual_base_drum_layout_rotate);
                        tempVg.addView(mInflater.inflate(C0088R.layout.manual_drum_layout, null));
                        tempVg.rotateLayout(270);
                        setVideoSettingInfo();
                        initializePanelButtons();
                        organizePanelLayout();
                        setAutoButtonsListener();
                        this.mRatioGuide = this.mGet.findViewById(C0088R.id.manual_base_ratio_guide_view);
                        if (this.mGet.isModuleChanging()) {
                            ((RelativeLayout) this.mManualGet.findViewById(C0088R.id.manual_panel_layout)).setVisibility(4);
                        }
                        if (FunctionProperties.isSupportedLogProfile()) {
                            initializeLogDisplayLUTButton();
                        }
                    }
                }
            }
        }
    }

    public void setDegree(int degree, boolean animation) {
        super.setDegree(degree, animation);
        rotatePanelButtons(degree);
        rotateInfoLayout(degree);
        rotateDrumLayout(270, degree);
        rotateDrumComponents(degree);
        relocateDrumAutoButton(degree);
        relocatePeakingButton(degree);
        if (FunctionProperties.isSupportedLogProfile()) {
            relocateLogDisplayLUTButton(degree);
        }
        setApertureTextLayoutGravity(degree);
    }

    protected void rotateInfoLayout(int degree) {
        if (degree == 90 || degree == 270) {
            int convertDegree = (degree + 90) % 360;
            RotateLayout rl = (RotateLayout) this.mGet.findViewById(C0088R.id.manual_info_layout_rotate);
            if (rl != null) {
                rl.rotateLayout(convertDegree);
            }
        }
    }

    protected void rotateDrumLayout(int layoutDegree, int degree) {
        int gravity = 81;
        int drumPaddingEnd = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, ManualUtil.selectValueCheckingLCDSize(0.164f, ModelProperties.getLCDType() == 2 ? 0.217f : 0.185f));
        RotateLayout manualDrumRotate = (RotateLayout) this.mManualGet.findViewById(C0088R.id.manual_base_drum_layout_rotate);
        if (manualDrumRotate != null) {
            FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) manualDrumRotate.getLayoutParams();
            if (flp != null) {
                switch (layoutDegree) {
                    case 0:
                        gravity = 8388629;
                        drumPaddingEnd *= -1;
                        flp.setMarginStart(0);
                        flp.setMarginEnd(drumPaddingEnd);
                        break;
                    case 90:
                        gravity = 49;
                        flp.setMarginStart(0);
                        flp.setMarginEnd(0);
                        break;
                    case 180:
                        gravity = 8388627;
                        flp.setMarginStart(drumPaddingEnd * -1);
                        flp.setMarginEnd(0);
                        break;
                    case 270:
                        gravity = 81;
                        flp.setMarginStart(0);
                        flp.setMarginEnd(0);
                        break;
                }
                flp.gravity = gravity;
                manualDrumRotate.setLayoutParams(flp);
            }
            manualDrumRotate.rotateLayout(layoutDegree);
        }
    }

    private void rotateDrumComponents(int degree) {
        if (degree == 90 || degree == 270) {
            int convertDegree = (degree + 90) % 360;
            if (Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
                convertDegree = (degree + 180) % 360;
            }
            for (Integer intValue : this.mDrumMap.keySet()) {
                DrumController drum = (DrumController) this.mDrumMap.get(Integer.valueOf(intValue.intValue()));
                if (drum != null) {
                    drum.setDegree(convertDegree);
                }
            }
        }
    }

    protected boolean onManualButtonClicked(final int type, final ManualData value) {
        if (isItemPanelWorkable(type)) {
            sendItemClickRunnable(new Runnable() {
                public void run() {
                    ManualViewManager.this.setButtonAndDrumUnselected(type);
                    ManualPanelButton button = (ManualPanelButton) ManualViewManager.this.mPanelButtonMap.get(Integer.valueOf(type));
                    DrumController drum = (DrumController) ManualViewManager.this.mDrumMap.get(Integer.valueOf(type));
                    RotateImageButton autoButton = (RotateImageButton) ManualViewManager.this.mAutoButtonMap.get(Integer.valueOf(type));
                    if (button != null && drum != null) {
                        int visiblity = button.isSelected() ? 8 : 0;
                        boolean toBeSelected = !button.isSelected();
                        drum.setVisibility(visiblity);
                        if (autoButton != null) {
                            autoButton.setVisibility(visiblity);
                            boolean isEnabled = ManualViewManager.this.mManualGet.isEnableAutoFuntion(type);
                            ColorFilter cf = isEnabled ? ColorUtil.getNormalColorByAlpha() : ColorUtil.getDimColorByAlpha();
                            autoButton.setEnabled(isEnabled);
                            autoButton.setColorFilter(cf);
                        }
                        ManualModeItem manualItem = ManualViewManager.this.getManualModeItem(type);
                        if (manualItem != null && value != null) {
                            if (toBeSelected && value.isDefaultValue()) {
                                ManualViewManager.this.setManualData(type, manualItem.getKey(), ManualControlManager.LOCK, Boolean.valueOf(true));
                            }
                            button.setSelected(toBeSelected);
                            if (type == 16 && ManualViewManager.this.mBtnPeaking != null) {
                                ManualViewManager.this.mBtnPeaking.setEnabled(true);
                                ManualViewManager.this.mBtnPeaking.setColorFilter(ColorUtil.getNormalColorByAlpha());
                                if (visiblity != 0) {
                                    ManualViewManager.this.mBtnPeaking.setVisibility(visiblity);
                                } else if (ManualUtil.isManualVideoMode(ManualViewManager.this.mGet.getShotMode()) && "on".equals(ManualViewManager.this.mGet.getSettingValue(Setting.KEY_HDR10))) {
                                    if (ManualViewManager.this.mGet.checkModuleValidate(192)) {
                                        ManualViewManager.this.mBtnPeaking.setVisibility(0);
                                    } else {
                                        ManualViewManager.this.mBtnPeaking.setVisibility(8);
                                    }
                                    ManualViewManager.this.mBtnPeaking.setSelected(false);
                                    ManualViewManager.this.mBtnPeaking.setEnabled(false);
                                    ManualViewManager.this.mBtnPeaking.setColorFilter(ColorUtil.getDimColorByAlpha());
                                } else {
                                    ManualViewManager.this.setFocusPeaking(type, SharedPreferenceUtil.getFocusPeakingEnable(ManualViewManager.this.getAppContext()));
                                }
                            }
                            if (!(visiblity == 0 || ManualViewManager.this.mGet == null)) {
                                ManualViewManager.this.mGet.setFilmStrengthButtonVisibility(true, false);
                            }
                            ManualViewManager.this.mManualGet.setLockedFeature();
                            ManualViewManager.this.mManualGet.onDrumVisibilityChanged(type, visiblity == 0);
                            ManualViewManager.this.mManualGet.onManualButtonClicked(type);
                            if (!value.mIsFakeMode && !ManualViewManager.this.mManualGet.IsDataChangedByGraphy()) {
                                if (type == 8 && FunctionProperties.getSupportedHal() == 2) {
                                    drum.setSelectedRealISOItem(value.getUserInfoValue(), false);
                                }
                                drum.setSelectedItem(value.getValue(), false);
                            } else if (type == 1) {
                                drum.setSelectedRealSSItem(value.getUserInfoValue(), false);
                            } else if (type == 8) {
                                drum.setSelectedRealISOItem(value.getUserInfoValue(), false);
                            } else if (type == 4) {
                                drum.setSelectedRealWBItem(value.getUserInfoValue(), false);
                            } else {
                                drum.setSelectedItem(value.getValue(), false);
                            }
                            if (FunctionProperties.isSupportedLogProfile() && ManualViewManager.this.mGet != null) {
                                ManualViewManager.this.relocateLogDisplayLUTButton(ManualViewManager.this.mGet.getOrientationDegree());
                            }
                        }
                    }
                }
            });
            return true;
        }
        setButtonAndDrumUnselected(type);
        return false;
    }

    public void onInAndOutRecording() {
        this.mManualGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                ManualViewManager.this.setButtonAndDrumUnselected(31);
            }
        });
    }

    public void setFocusPeaking(int type, boolean isFocusPeaking) {
        if (type == 16 && this.mBtnPeaking != null) {
            if (this.mGet.checkModuleValidate(192)) {
                this.mBtnPeaking.setVisibility(0);
                setFocusPeakingParam(isFocusPeaking, true);
                return;
            }
            this.mBtnPeaking.setVisibility(8);
            setFocusPeakingParam(false, false);
        }
    }

    public void setLogDisplayLUT(boolean on) {
        if (this.mBtnLogDisplayLUT != null) {
            this.mBtnLogDisplayLUT.setImageResource(on ? C0088R.drawable.btn_manual_lut_pressed : C0088R.drawable.btn_manual_lut_normal);
            setLogDisplayLUTParam(on);
        }
    }

    public void setButtonAndDrumUnselected(int type) {
        for (Integer intValue : this.mDrumMap.keySet()) {
            int key = intValue.intValue();
            if (type != key) {
                DrumController otherDrum = (DrumController) this.mDrumMap.get(Integer.valueOf(key));
                if (otherDrum != null) {
                    otherDrum.setVisibility(8);
                }
                this.mManualGet.onDrumVisibilityChanged(type, false);
                ManualPanelButton panelButton = (ManualPanelButton) this.mPanelButtonMap.get(Integer.valueOf(key));
                if (panelButton != null) {
                    panelButton.setSelected(false);
                }
                RotateImageButton autoButton = (RotateImageButton) this.mAutoButtonMap.get(Integer.valueOf(key));
                if (autoButton != null) {
                    autoButton.setVisibility(8);
                }
                if (this.mBtnPeaking != null) {
                    this.mBtnPeaking.setVisibility(8);
                }
            }
        }
    }

    protected void initializePanelButtons() {
        this.mBtnWB = (ManualPanelButton) this.mManualGet.findViewById(C0088R.id.manual_panel_wb);
        if (this.mBtnWB != null) {
            this.mBtnWB.setTitle(GraphyDataManager.COLUMN_WB);
            this.mBtnWB.setContentDescriptionString(this.mGet.getAppContext().getString(C0088R.string.white_balance));
            this.mBtnWB.setOnClickListener(new C10683());
        }
        this.mBtnFocus = (ManualPanelButton) this.mManualGet.findViewById(C0088R.id.manual_panel_mf);
        if (this.mBtnFocus != null) {
            this.mBtnFocus.setTitle("Focus");
            this.mBtnFocus.setContentDescriptionString(this.mGet.getAppContext().getString(C0088R.string.focus));
            this.mBtnFocus.setOnClickListener(new C10704());
        }
        this.mBtnEV = (ManualPanelButton) this.mManualGet.findViewById(C0088R.id.manual_panel_ev);
        if (this.mBtnEV != null) {
            this.mBtnEV.setTitle("EV");
            this.mBtnEV.setContentDescriptionString(this.mGet.getAppContext().getString(C0088R.string.brightness));
            this.mBtnEV.setOnClickListener(new C10715());
        }
        this.mBtnISO = (ManualPanelButton) this.mManualGet.findViewById(C0088R.id.manual_panel_iso);
        if (this.mBtnISO != null) {
            this.mBtnISO.setTitle("ISO");
            this.mBtnISO.setContentDescriptionString(this.mGet.getAppContext().getString(C0088R.string.iso_accessibility));
            this.mBtnISO.setOnClickListener(new C10726());
        }
        this.mBtnSS = (ManualPanelButton) this.mManualGet.findViewById(C0088R.id.manual_panel_ss);
        if (this.mBtnSS != null) {
            this.mBtnSS.setTitle(GpsLatitudeRef.SOUTH);
            this.mBtnSS.setContentDescriptionString(this.mGet.getAppContext().getString(C0088R.string.shutter_speed));
            this.mBtnSS.setOnClickListener(new C10737());
        }
        this.mBtnAllAuto = (ManualPanelButton) this.mManualGet.findViewById(C0088R.id.manual_panel_all_auto);
        if (this.mBtnAllAuto != null) {
            this.mBtnAllAuto.setOnClickListener(new C10748());
        }
        this.mBtnAELock = (ManualPanelButton) this.mManualGet.findViewById(C0088R.id.manual_panel_ae_lock);
        if (this.mBtnAELock != null) {
            this.mBtnAELock.setText("AE-L");
            this.mBtnAELock.setContentDescription(this.mGet.getAppContext().getString(C0088R.string.ae_lock_off));
            this.mBtnAELock.setOnClickListener(new C10759());
            setAElockButtnSeletced();
        }
    }

    protected void initializeLogDisplayLUTButton() {
        ViewGroup tmpVg = (ViewGroup) this.mGet.findViewById(C0088R.id.manual_base);
        RelativeLayout tmpLayout = (RelativeLayout) this.mGet.inflateView(C0088R.layout.display_lut_button);
        if (!(tmpVg == null || tmpLayout == null)) {
            tmpVg.addView(tmpLayout);
        }
        this.mBtnLogDisplayLUT = (RotateImageButton) this.mManualGet.findViewById(C0088R.id.manual_video_log_profile_display_lut_btn);
        if (this.mBtnLogDisplayLUT != null) {
            this.mBtnLogDisplayLUT.setContentDescription(getAppContext().getString(C0088R.string.manual_video_log_profile_display_lut_3));
            this.mBtnLogDisplayLUT.setOnClickListener(new C105310());
        }
        rotateLogDefaultLUTButton(this.mGet.getOrientationDegree());
    }

    public void setLogDisplayLUTValue(boolean on) {
        this.mIsLogDisplayLUT = on;
        this.mBtnLogDisplayLUT.setImageResource(on ? C0088R.drawable.btn_manual_lut_pressed : C0088R.drawable.btn_manual_lut_normal);
    }

    public boolean getLogDisplayLUTValue() {
        return this.mIsLogDisplayLUT;
    }

    public void needDisplayLUTToast(boolean need) {
        this.mIsFirstDisplayLUT = need;
    }

    public void setButtonLocked(boolean locked, int key) {
        if ((key & 1) != 0) {
            setButtonLockedInternal(locked, 1);
        }
        if ((key & 8) != 0) {
            setButtonLockedInternal(locked, 8);
        }
        if ((key & 2) != 0) {
            setButtonLockedInternal(locked, 2);
        }
        if ((key & 16) != 0) {
            setButtonLockedInternal(locked, 16);
        }
        if ((key & 4) != 0) {
            setButtonLockedInternal(locked, 4);
        }
    }

    private void setButtonLockedInternal(boolean locked, int key) {
        ManualPanelButton panelButton = (ManualPanelButton) this.mPanelButtonMap.get(Integer.valueOf(key));
        Context context = this.mGet.getAppContext();
        if (panelButton != null && panelButton.isEnabled()) {
            int color = 0;
            Message msg = new Message();
            int delay = 1000;
            if (locked) {
                if (context != null) {
                    color = context.getColor(C0088R.color.camera_pressed_txt);
                }
                msg.what = 2;
                msg.arg1 = key;
            } else {
                Message msg3 = new Message();
                if (context != null) {
                    color = context.getColor(C0088R.color.camera_pressed_txt);
                }
                msg3.what = 2;
                msg3.arg1 = key;
                this.mMainHandler.sendMessageDelayed(msg3, (long) 300);
                Message msg2 = new Message();
                msg2.what = 0;
                msg2.arg1 = key;
                this.mMainHandler.sendMessageDelayed(msg2, (long) 0);
                msg.what = 1;
                msg.arg1 = key;
                delay = 300;
            }
            panelButton.setTextColor(color);
            this.mMainHandler.sendMessageDelayed(msg, (long) delay);
        }
    }

    protected void organizePanelLayout() {
        int previewWidth = (int) (((float) Utils.getLCDsize(getAppContext(), true)[1]) * 1.3333334f);
        boolean isManualCameraMode = ManualUtil.isManualCameraMode(this.mGet.getShotMode());
        ViewGroup emptySpace = (ViewGroup) this.mGet.findViewById(C0088R.id.manual_panel_empty_area);
        if (emptySpace != null) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) emptySpace.getLayoutParams();
            if (lp != null) {
                int btnCnt = getPannelButtonCnt();
                if (!isManualCameraMode) {
                    btnCnt++;
                }
                float widthRatio = 0.1166f;
                if (ModelProperties.isLongLCDModel()) {
                    if (isManualCameraMode) {
                        widthRatio = 0.1166f;
                    } else {
                        previewWidth = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, ModelProperties.getLCDType() == 2 ? 0.74f : 0.757f);
                        widthRatio = 0.1166f;
                    }
                }
                lp.width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, widthRatio);
                emptySpace.setLayoutParams(lp);
                emptySpace.setOnTouchListener(null);
                emptySpace.setOnClickListener(null);
                int buttonWidth = ((previewWidth - lp.width) - RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.09375f)) / btnCnt;
                setManualPanelButtonLayout(buttonWidth);
                setAudioButtonLayout(buttonWidth);
            }
        }
    }

    private void setManualPanelButtonLayout(int buttonWidth) {
        ViewGroup buttonGroup = (ViewGroup) this.mGet.findViewById(C0088R.id.manual_panel_button_layout);
        if (ModelProperties.isLongLCDModel()) {
            LayoutParams buttonsLayoutParam = (LayoutParams) buttonGroup.getLayoutParams();
            buttonsLayoutParam.height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.10277f);
            buttonGroup.setLayoutParams(buttonsLayoutParam);
        }
        if (buttonGroup != null) {
            for (int i = 0; i < buttonGroup.getChildCount(); i++) {
                View button = buttonGroup.getChildAt(i);
                if (button != null && (button instanceof ManualPanelButton)) {
                    LinearLayout.LayoutParams buttonLayoutParam = (LinearLayout.LayoutParams) button.getLayoutParams();
                    if (buttonLayoutParam != null) {
                        buttonLayoutParam.width = buttonWidth;
                        if (ModelProperties.isLongLCDModel()) {
                            buttonLayoutParam.height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.09f);
                        }
                        button.setLayoutParams(buttonLayoutParam);
                    }
                }
            }
        }
    }

    protected void setAutoButtonsListener() {
        this.mDrumBtnAWB = (RotateImageButton) this.mManualGet.findViewById(C0088R.id.manual_control_button_awb);
        if (this.mDrumBtnAWB != null) {
            this.mDrumBtnAWB.setOnClickListener(new C105411());
        }
        this.mDrumBtnAF = (RotateImageButton) this.mManualGet.findViewById(C0088R.id.manual_control_button_af);
        if (this.mDrumBtnAF != null) {
            this.mDrumBtnAF.setOnClickListener(new C105512());
        }
        this.mBtnPeaking = (RotateImageButton) this.mManualGet.findViewById(C0088R.id.manual_control_button_peaking);
        if (this.mBtnPeaking != null) {
            this.mBtnPeaking.setText(this.mGet.getAppContext().getString(C0088R.string.manual_peaking_title));
            this.mBtnPeaking.setOnClickListener(new C105613());
        }
        this.mDrumBtnISO = (RotateImageButton) this.mManualGet.findViewById(C0088R.id.manual_control_button_iso);
        if (this.mDrumBtnISO != null) {
            this.mDrumBtnISO.setOnClickListener(new C105714());
        }
        this.mDrumBtnSS = (RotateImageButton) this.mManualGet.findViewById(C0088R.id.manual_control_button_ss);
        if (this.mDrumBtnSS != null) {
            this.mDrumBtnSS.setOnClickListener(new C105815());
        }
    }

    private void onAutoButtonClicked(int type) {
        DrumController drum = (DrumController) this.mDrumMap.get(Integer.valueOf(type));
        if (drum != null) {
            drum.setVisibility(8);
        }
        this.mManualGet.onDrumVisibilityChanged(type, false);
        ManualPanelButton button = (ManualPanelButton) this.mPanelButtonMap.get(Integer.valueOf(type));
        if (button != null) {
            button.setSelected(false);
        }
        if (this.mGet != null) {
            this.mGet.setFilmStrengthButtonVisibility(true, false);
        }
        if (this.mBtnPeaking != null) {
            this.mBtnPeaking.setVisibility(8);
        }
    }

    public void setApertureNumber() {
        String shotMode = this.mGet.getShotMode();
        TextView view = (TextView) this.mGet.findViewById(C0088R.id.manual_info_text_aperture);
        if (view != null) {
            if (ManualUtil.isManualCameraMode(shotMode)) {
                CameraProxy cameraDevice = this.mGet.getCameraDevice();
                if (cameraDevice != null) {
                    CameraParameters param = cameraDevice.getParameters();
                    if (param != null) {
                        String aperture = param.get(ParamConstants.KEY_F_NUMBER);
                        if (aperture != null) {
                            try {
                                if (!(aperture.equals("0") || ModelProperties.isFakeExif())) {
                                    aperture = String.valueOf(Math.floor(Double.parseDouble(aperture) * 10.0d) / 10.0d);
                                    String value = "F" + aperture;
                                    view.setContentDescription(value);
                                    view.setText(value);
                                    return;
                                }
                            } catch (Exception e) {
                                aperture = "2.0";
                            }
                        }
                        throw new Exception();
                    }
                    return;
                }
                return;
            }
            view.setVisibility(8);
        }
    }

    protected void setApertureTextLayoutGravity(int degree) {
        RelativeLayout emptySpace = (RelativeLayout) this.mGet.findViewById(C0088R.id.manual_info_layout);
        if (emptySpace != null) {
            int gravity = emptySpace.getGravity();
            switch (degree) {
                case 90:
                    gravity = 8388627;
                    break;
                case 270:
                    gravity = 8388629;
                    break;
            }
            if (ManualUtil.isManualCameraMode(this.mGet.getShotMode())) {
                String histogramSetting = this.mGet.getSettingValue(Setting.KEY_HISTOGRAM);
                TextView view = (TextView) this.mGet.findViewById(C0088R.id.manual_info_text_aperture);
                if (view != null) {
                    LayoutParams rlp = (LayoutParams) view.getLayoutParams();
                    if (rlp != null) {
                        int marginEnd = rlp.getMarginEnd();
                        int marginStart = rlp.getMarginStart();
                        if ("off".equals(histogramSetting)) {
                            gravity = 17;
                            marginEnd = 0;
                            marginStart = 0;
                        } else {
                            gravity |= 16;
                            int marginValue = Utils.getPx(getAppContext(), C0088R.dimen.manual_panel_aperture_text_margin_end);
                            if (degree == 90) {
                                marginEnd = 0;
                                marginStart = marginValue;
                            } else if (degree == 270) {
                                marginEnd = marginValue;
                                marginStart = 0;
                            }
                        }
                        rlp.setMarginStart(marginStart);
                        rlp.setMarginEnd(marginEnd);
                        view.setLayoutParams(rlp);
                    } else {
                        return;
                    }
                }
                return;
            }
            emptySpace.setGravity(gravity);
        }
    }

    private void relocateDrumAutoButton(int degree) {
        int drumPaddingEnd = Utils.getPx(this.mManualGet.getAppContext(), C0088R.dimen.manual_wheel_marginEnd);
        if (this.mDrumBtnAWB != null && this.mDrumBtnAF != null && this.mDrumBtnISO != null && this.mDrumBtnSS != null) {
            LayoutParams rl = (LayoutParams) this.mDrumBtnAWB.getLayoutParams();
            if (rl != null) {
                rl.width = Utils.getPx(getAppContext(), C0088R.dimen.manual_auto_button_width);
                rl.height = Utils.getPx(getAppContext(), C0088R.dimen.manual_auto_button_height);
                if (this.mWhiteBalance != null) {
                    int marginTop = Utils.getPx(getAppContext(), C0088R.dimen.manual_drum_auto_btn_marginTop);
                    int marginEnd = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, ManualUtil.selectValueCheckingLCDSize(0.164f, ModelProperties.getLCDType() == 2 ? 0.217f : 0.185f));
                    switch (degree) {
                        case 0:
                        case 180:
                            rl.setMarginsRelative(0, marginTop, marginEnd, 0);
                            break;
                        case 90:
                        case 270:
                            rl.setMarginsRelative(0, marginTop, marginEnd, 0);
                            break;
                    }
                    this.mDrumBtnAWB.setLayoutParams(rl);
                    this.mDrumBtnAF.setLayoutParams(rl);
                    this.mDrumBtnISO.setLayoutParams(rl);
                    this.mDrumBtnSS.setLayoutParams(rl);
                    rotateAutoButton(degree);
                }
            }
        }
    }

    protected void rotateAutoButton(int degree) {
        if (degree == 90 || degree == 270) {
            int convertDegree = (degree + 90) % 360;
            this.mDrumBtnAWB.setDegree(convertDegree, false);
            this.mDrumBtnAF.setDegree(convertDegree, false);
            this.mDrumBtnISO.setDegree(convertDegree, false);
            this.mDrumBtnSS.setDegree(convertDegree, false);
        }
    }

    private void relocatePeakingButton(int degree) {
        int drumPaddingEnd = Utils.getPx(getAppContext(), C0088R.dimen.manual_wheel_marginEnd);
        if (this.mDrumBtnAWB != null && this.mBtnPeaking != null) {
            LayoutParams rl = (LayoutParams) this.mBtnPeaking.getLayoutParams();
            if (rl != null) {
                rl.width = Utils.getPx(getAppContext(), C0088R.dimen.manual_peaking_button_width);
                rl.height = Utils.getPx(getAppContext(), C0088R.dimen.manual_peaking_button_height);
                if (this.mWhiteBalance != null) {
                    int marginTop = Utils.getPx(getAppContext(), C0088R.dimen.manual_drum_auto_btn_marginTop);
                    int marginEnd = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, ManualUtil.selectValueCheckingLCDSize(0.164f, ModelProperties.getLCDType() == 2 ? 0.217f : 0.185f) + 0.017f) + Utils.getPx(getAppContext(), C0088R.dimen.manual_auto_button_width);
                    switch (degree) {
                        case 0:
                        case 180:
                            rl.setMarginsRelative(0, marginTop, marginEnd, 0);
                            break;
                        case 90:
                        case 270:
                            rl.setMarginsRelative(0, marginTop, marginEnd, 0);
                            break;
                    }
                    this.mBtnPeaking.setLayoutParams(rl);
                    rotatePeakingButton(degree);
                }
            }
        }
    }

    public void relocateLogDisplayLUTButton(int degree) {
        if (this.mBtnLogDisplayLUT != null) {
            LayoutParams rl = (LayoutParams) this.mBtnLogDisplayLUT.getLayoutParams();
            if (rl != null) {
                rl.width = Utils.getPx(getAppContext(), C0088R.dimen.manual_lut_button_width);
                rl.height = Utils.getPx(getAppContext(), C0088R.dimen.manual_lut_button_height);
                int end = Utils.getPx(getAppContext(), C0088R.dimen.manual_drum_auto_btn_marginTop);
                int bottom = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, ManualUtil.selectValueCheckingLCDSize(0.164f, ModelProperties.getLCDType() == 2 ? 0.217f : 0.185f));
                int buttonGap = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.017f);
                int autoBtnWidth = Utils.getPx(getAppContext(), C0088R.dimen.manual_auto_button_width);
                int focusPeakingBtnWidth = Utils.getPx(getAppContext(), C0088R.dimen.manual_peaking_button_width);
                if (isDrumShowing(16)) {
                    bottom = ((autoBtnWidth + buttonGap) + bottom) + (focusPeakingBtnWidth + buttonGap);
                } else if (isDrumShowing(31)) {
                    bottom += autoBtnWidth + buttonGap;
                }
                rl.addRule(12, 1);
                rl.addRule(21, 1);
                rl.setMarginEnd(end);
                rl.bottomMargin = bottom;
                this.mBtnLogDisplayLUT.setLayoutParams(rl);
                rotateLogDefaultLUTButton(degree);
            }
        }
    }

    protected void rotatePeakingButton(int degree) {
        if (this.mBtnPeaking != null) {
            if (degree == 90 || degree == 270) {
                this.mBtnPeaking.setDegree((degree + 90) % 360, false);
            }
        }
    }

    protected void rotateLogDefaultLUTButton(int degree) {
        if (this.mBtnLogDisplayLUT == null) {
            return;
        }
        if (degree == 90 || degree == 270) {
            this.mBtnLogDisplayLUT.setDegree(degree, false);
        }
    }

    protected void onScrollReleased(int type, String value) {
        this.mManualGet.setSetting((String) this.mSettingKeyMap.get(Integer.valueOf(type)), value, true);
    }

    public void onMenuVisibilityChanged(boolean menuVisible) {
        if (menuVisible) {
            setButtonAndDrumUnselected(31);
            setPanelVisibility(8);
            updateRatioGuideVisibility(8);
            return;
        }
        setPanelVisibility(0);
        updateRatioGuideVisibility(0);
        setApertureTextLayoutGravity(this.mGet.getOrientationDegree());
        setDrumsEnabled(31, true);
    }

    public void setPanelVisibility(int visibility) {
        if (visibility != 0) {
            setButtonAndDrumUnselected(0);
        }
        RelativeLayout rl = (RelativeLayout) this.mManualGet.findViewById(C0088R.id.manual_panel_layout);
        if (rl != null) {
            rl.setVisibility(visibility);
        }
        if (FunctionProperties.isSupportedLogProfile() && this.mBtnLogDisplayLUT != null) {
            if ("on".equals(this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG)) && ManualUtil.isManualVideoMode(this.mGet.getShotMode())) {
                this.mBtnLogDisplayLUT.setVisibility(visibility);
                if (this.mIsFirstDisplayLUT && visibility == 0) {
                    this.mIsFirstDisplayLUT = false;
                    this.mGet.showToast(getAppContext().getString(C0088R.string.manual_video_log_profile_display_lut_toast_4), CameraConstants.TOAST_LENGTH_LONG);
                    return;
                }
                return;
            }
            this.mBtnLogDisplayLUT.setVisibility(4);
        }
    }

    protected void setWhiteBalanceListener() {
        if (this.mWhiteBalance != null) {
            this.mWhiteBalance.setDrumControllerListener(new C105916());
        }
    }

    protected String getSelectedPanelItem(int itemType) {
        ManualPanelButton itemView = null;
        switch (itemType) {
            case 1:
                itemView = this.mBtnSS;
                break;
            case 2:
                itemView = this.mBtnEV;
                break;
            case 4:
                itemView = this.mBtnWB;
                break;
            case 8:
                itemView = this.mBtnISO;
                break;
            case 16:
                itemView = this.mBtnFocus;
                break;
        }
        if (itemView != null) {
            return itemView.getText();
        }
        return "none";
    }

    protected void setManualFocusListener() {
        if (this.mManualFocus != null) {
            this.mManualFocus.setDrumControllerListener(new C106017());
        }
    }

    protected void setExposureValueListener() {
        if (this.mExposureValue != null) {
            this.mExposureValue.setDrumControllerListener(new C106118());
        }
    }

    protected void setISOListener() {
        if (this.mISO != null) {
            this.mISO.setDrumControllerListener(new C106219());
        }
    }

    protected void setShutterSpeedListener() {
        if (this.mShutterSpeed != null) {
            this.mShutterSpeed.setDrumControllerListener(new C106420());
        }
    }

    protected ManualModeItem getManualModeItem(int type) {
        if (this.mModeItemList == null || this.mModeItemList.size() == 0) {
            return null;
        }
        for (ManualModeItem item : this.mModeItemList) {
            if (item != null) {
                if (getModeKey(type).equals(item.getTitle())) {
                    return item;
                }
            }
        }
        return null;
    }

    protected void createManualDrums() {
        int drumPaddingEnd = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, ManualUtil.selectValueCheckingLCDSize(0.164f, ModelProperties.getLCDType() == 2 ? 0.217f : 0.185f));
        this.mWhiteBalance = (DrumController) this.mManualGet.findViewById(C0088R.id.drum_white_balance);
        if (this.mWhiteBalance != null) {
            this.mWhiteBalance.setPaddingRelative(this.mWhiteBalance.getPaddingStart(), this.mWhiteBalance.getPaddingTop(), drumPaddingEnd, this.mWhiteBalance.getPaddingBottom());
            this.mWhiteBalance.initResources(4, getManualModeItem(4));
            setWhiteBalanceListener();
        }
        this.mManualFocus = (DrumBarController) this.mManualGet.findViewById(C0088R.id.drum_manual_focus);
        if (this.mManualFocus != null) {
            this.mManualFocus.setPaddingRelative(this.mManualFocus.getPaddingStart(), this.mManualFocus.getPaddingTop(), drumPaddingEnd, this.mManualFocus.getPaddingBottom());
            this.mManualFocus.initResources(16, getManualModeItem(16));
            setManualFocusListener();
        }
        this.mExposureValue = (DrumController) this.mManualGet.findViewById(C0088R.id.drum_exposure_value);
        if (this.mExposureValue != null) {
            this.mExposureValue.setPaddingRelative(this.mExposureValue.getPaddingStart(), this.mExposureValue.getPaddingTop(), drumPaddingEnd, this.mExposureValue.getPaddingBottom());
            this.mExposureValue.initResources(2, getManualModeItem(2));
            setExposureValueListener();
        }
        this.mISO = (DrumController) this.mManualGet.findViewById(C0088R.id.drum_iso);
        if (this.mISO != null) {
            this.mISO.setPaddingRelative(this.mISO.getPaddingStart(), this.mISO.getPaddingTop(), drumPaddingEnd, this.mISO.getPaddingBottom());
            this.mISO.initResources(8, getManualModeItem(8));
            setISOListener();
        }
        this.mShutterSpeed = (DrumController) this.mManualGet.findViewById(C0088R.id.drum_shutter_speed);
        if (this.mShutterSpeed != null) {
            this.mShutterSpeed.setPaddingRelative(this.mShutterSpeed.getPaddingStart(), this.mShutterSpeed.getPaddingTop(), drumPaddingEnd, this.mShutterSpeed.getPaddingBottom());
            this.mShutterSpeed.initResources(1, getManualModeItem(1));
            setShutterSpeedListener();
        }
    }

    public void refreshShutterSpeedData() {
        ManualModeItem shutterSpeedItem = getManualModeItem(1);
        if (shutterSpeedItem != null) {
            ManualData shutterSpeedData = this.mManualGet.getManualData(1);
            if (shutterSpeedData != null && shutterSpeedData.getEntryArray() != null) {
                this.mModeItemList.remove(shutterSpeedItem);
                boolean[] showEntryValue = new boolean[shutterSpeedData.getEntryArray().length];
                for (int i = 0; i < shutterSpeedData.getEntryArray().length; i++) {
                    if (i % 2 == 0) {
                        showEntryValue[i] = true;
                    } else {
                        showEntryValue[i] = false;
                    }
                }
                shutterSpeedItem.setEntries(shutterSpeedData.getEntryArray());
                shutterSpeedItem.setValues(shutterSpeedData.getValueArray());
                shutterSpeedItem.setSelectedIndex(-1);
                shutterSpeedItem.setPrefDefaultValue(shutterSpeedData.getDefaultValue());
                shutterSpeedItem.setShowEntryValue(showEntryValue);
                this.mModeItemList.add(shutterSpeedItem);
                ManualData data = this.mManualGet.getManualData(1);
                String currentValue = null;
                if (data != null) {
                    currentValue = data.getValue();
                }
                if (this.mShutterSpeed != null) {
                    this.mShutterSpeed.initResources(1, shutterSpeedItem, true, currentValue);
                }
            }
        }
    }

    public void notifyLockStatus() {
        this.mManualGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (ManualViewManager.this.mManualGet != null) {
                    ManualViewManager.this.setButtonsEnabled(true);
                    ManualViewManager.this.setAElockButtnSeletced();
                }
            }
        });
    }

    public void setAElockButtnSeletced() {
        if (this.mBtnAELock == null) {
            return;
        }
        if (this.mManualGet.getAELock()) {
            this.mBtnAELock.setSelected(true);
        } else {
            this.mBtnAELock.setSelected(false);
        }
    }

    public void enableManualControls(boolean enable) {
        setButtonsEnabled(enable);
        setDrumsEnabled(31, enable);
        setAutoButtonsEnabled(31, enable);
    }

    protected void setButtonsEnabled(boolean enable) {
        int enabledFeature = this.mManualGet.getEnabledFeature();
        int controllableFeature = this.mManualGet.getControllableFeature();
        for (Integer intValue : this.mPanelButtonMap.keySet()) {
            int key = intValue.intValue();
            ManualPanelButton manualButton = (ManualPanelButton) this.mPanelButtonMap.get(Integer.valueOf(key));
            if (manualButton != null) {
                if (enable && (enabledFeature & key) == key && (controllableFeature & key) == key) {
                    manualButton.setEnabled(true);
                } else {
                    manualButton.setEnabled(false);
                }
            }
        }
        if (this.mBtnAELock != null) {
            ManualPanelButton manualPanelButton = this.mBtnAELock;
            boolean z = enable && this.mManualGet.isSupportedAEUnlock();
            manualPanelButton.setEnabled(z);
        }
        if (this.mBtnAllAuto != null) {
            this.mBtnAllAuto.setEnabled(enable);
        }
        if (this.mBtnFocus != null) {
            ManualDataMF value = (ManualDataMF) this.mManualGet.getManualData(16);
            if (value != null) {
                if ((enabledFeature & 16) == 0) {
                    updateButtonValue(16, value.getDisabledInfoValue());
                } else if (value.isDefaultValue()) {
                    updateButtonValue(16, value.getAutoInfoValue());
                } else {
                    updateButtonValue(16, value.getUserInfoValue());
                }
            }
        }
    }

    public void updateAllButtonValue() {
        if (!this.mMainHandler.hasMessages(0)) {
            this.mManualGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (ManualViewManager.this.mManualGet.checkModuleValidate(1) && !ManualViewManager.this.mGet.isCameraChanging() && !ManualViewManager.this.mGet.isModuleChanging() && !ManualViewManager.this.mGet.isSwithingCameraDuringTheRecording()) {
                        int supportedFeature = ManualViewManager.this.mManualGet.getSupportedFeature();
                        for (int i = 1; i < 31; i <<= 1) {
                            if ((supportedFeature & i) == i) {
                                ManualViewManager.this.updateButtonValue(i);
                            }
                        }
                    }
                }
            });
        }
    }

    public void updateAllButtonValueByForce() {
        int supportedFeature = this.mManualGet.getSupportedFeature();
        for (int i = 1; i < 31; i <<= 1) {
            if ((supportedFeature & i) == i) {
                updateButtonValue(i);
            }
        }
    }

    public void onContentSizeChanged() {
        if (updateGuideViewRatio()) {
            setManualPanelbottomMargin(true);
        } else {
            setManualPanelbottomMargin(false);
        }
    }

    protected void setManualPanelbottomMargin(boolean cinemaSize) {
        int ratioGuideHeight = getRatioGuideHeight();
        int manualPanelheight = Utils.getPx(getAppContext(), C0088R.dimen.manual_panel_button_height);
        View manualPanel = this.mGet.findViewById(C0088R.id.manual_panel_layout);
        if (manualPanel != null) {
            int margin = 0;
            if (!cinemaSize || ModelProperties.isLongLCDModel()) {
                margin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.03333f);
            } else if (ratioGuideHeight < manualPanelheight) {
                margin = (ratioGuideHeight - manualPanelheight) + 10;
            }
            manualPanel.setPaddingRelative(manualPanel.getPaddingStart(), manualPanel.getPaddingTop(), manualPanel.getPaddingEnd(), margin);
        }
    }
}
