package com.lge.camera.managers;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationResolutionUtilBase;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SnapShotChecker;
import com.lge.camera.util.Utils;
import java.util.HashMap;

public class BinningManager extends ManagerInterfaceImpl {
    private static String BINNING_CAPTURE_TYPE = ParamConstants.OUTPUTS_DEFAULT_STR;
    private static String BINNING_RECORD_TYPE = ParamConstants.OUTPUTS_DEFAULT_STR;
    public static float LOW_LIGHT_10_LUX_BINNING = 355.0f;
    public static float LOW_LIGHT_10_LUX_NORMAL = 425.0f;
    public static float LOW_LIGHT_3_LUX_NORMAL = 470.0f;
    protected static int sBinningButtonBottomMargin = 0;
    protected RotateImageButton mBinningIcon = null;
    protected boolean mBinningOffManually = false;
    protected String mBinningPictureSize = "";
    protected RelativeLayout mBinningRelatviewLayout = null;
    protected View mBinningRootView = null;
    protected String mCurrentPictureSize = "";
    protected RotateLayout mGuideLayout = null;
    protected TextView mGuideText = null;
    HandlerRunnable mHapticHandler1 = new HandlerRunnable(this) {
        public void handleRun() {
            AudioUtil.performHapticFeedback(BinningManager.this.mGuideLayout, 65593);
        }
    };
    HandlerRunnable mHapticHandler2 = new HandlerRunnable(this) {
        public void handleRun() {
            AudioUtil.performHapticFeedback(BinningManager.this.mGuideLayout, 65593);
        }
    };
    HandlerRunnable mHapticHandler3 = new HandlerRunnable(this) {
        public void handleRun() {
            AudioUtil.performHapticFeedback(BinningManager.this.mGuideLayout, 65593);
        }
    };
    HandlerRunnable mHideHandler = new HandlerRunnable(this) {
        public void handleRun() {
            BinningManager.this.mBinningRelatviewLayout.clearAnimation();
            BinningManager.this.mBinningRelatviewLayout.setVisibility(8);
            BinningManager.this.showGuideText(false);
        }
    };
    protected boolean mIsBinningIconShowingAvailable = false;
    protected boolean mIsBinningSettingProcessing = false;
    protected boolean mIsBinningState = false;
    protected boolean mNotNeedRestartPreview = false;
    protected HashMap<String, String> mRearCameraSizeMap = null;
    protected HashMap<String, String> mRearWideCameraSizeMap = null;
    HandlerRunnable mResetBinningProcessing = new HandlerRunnable(this) {
        public void handleRun() {
            BinningManager.this.mIsBinningSettingProcessing = false;
        }
    };
    HandlerRunnable mShowHandler = new HandlerRunnable(this) {
        public void handleRun() {
            BinningManager.this.mBinningRelatviewLayout.clearAnimation();
            BinningManager.this.mBinningRelatviewLayout.setVisibility(0);
            BinningManager.this.showGuideText(false);
        }
    };
    HandlerRunnable mStopAnimHandler = new HandlerRunnable(this) {
        public void handleRun() {
            BinningManager.this.mBinningRelatviewLayout.clearAnimation();
            BinningManager.this.showGuideText(false);
        }
    };

    /* renamed from: com.lge.camera.managers.BinningManager$1 */
    class C08351 implements ImageGetter {
        C08351() {
        }

        public Drawable getDrawable(String arg0) {
            Drawable d = BinningManager.this.mGet.getActivity().getResources().getDrawable(C0088R.drawable.camera_guide_spannable_night);
            if (d != null) {
                int textSize = (int) (((double) BinningManager.this.mGuideText.getTextSize()) * 1.3d);
                d.setBounds(0, 0, textSize, textSize);
            }
            return d;
        }
    }

    /* renamed from: com.lge.camera.managers.BinningManager$2 */
    class C08362 implements OnClickListener {
        C08362() {
        }

        public void onClick(View arg0) {
            if (!BinningManager.this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL) && !BinningManager.this.mIsBinningSettingProcessing && !BinningManager.this.checkSnapshotProcForcely() && BinningManager.this.mGet.checkModuleValidate(192) && BinningManager.this.checkAvailableChangeBinningMode()) {
                BinningManager.this.mIsBinningState = !BinningManager.this.mIsBinningState;
                CamLog.m3d(CameraConstants.TAG, "[NightVision] binning icon btn click");
                if (!SharedPreferenceUtil.getNightVisionManualToastState(BinningManager.this.mGet.getAppContext(), BinningManager.this.mIsBinningState)) {
                    BinningManager.this.mGet.showSetBinningToastManually(BinningManager.this.mIsBinningState);
                    SharedPreferenceUtil.setNightVisionManualToastState(BinningManager.this.mGet.getAppContext(), BinningManager.this.mIsBinningState);
                }
                BinningManager.this.removeHapticHandlers();
                if (BinningManager.this.checkBinningManualOff() && FunctionProperties.getSupportedHal() == 2 && (BinningManager.this.mGet.isFocusLock() || BinningManager.this.mGet.isAELock())) {
                    BinningManager.this.mGet.hideAndCancelAllFocusForBinningState(false);
                }
                BinningManager.this.setBinningPictureSizeDirect(0);
                if (BinningManager.this.checkBinningManualOff()) {
                    BinningManager.this.mGet.setBinningManualOff();
                    BinningManager.this.mBinningOffManually = true;
                }
            }
        }
    }

    public BinningManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        initializeBinningSize();
        initializeBinningSetting();
        initLayout();
    }

    public void initLayout() {
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        this.mBinningRootView = this.mGet.inflateView(C0088R.layout.binning_icon_view);
        if (vg != null && this.mBinningRootView != null) {
            vg.addView(this.mBinningRootView);
            this.mBinningRelatviewLayout = (RelativeLayout) this.mBinningRootView.findViewById(C0088R.id.binning_icon_view);
            this.mBinningIcon = (RotateImageButton) this.mBinningRootView.findViewById(C0088R.id.binning_icon_button);
            this.mBinningIcon.setContentDescription(getContentDescMsg());
            this.mGuideLayout = (RotateLayout) this.mBinningRootView.findViewById(C0088R.id.binning_guide_layout);
            this.mGuideText = (TextView) this.mBinningRootView.findViewById(C0088R.id.binning_suggestion_text);
            String htmlRes = "<img src=\"add_icon\"/>";
            int descResId = FunctionProperties.isUseSuperBright() ? C0088R.string.super_bright_camera_toast_suggestion_talkback : C0088R.string.bright_mode_toast_suggestion_talkback;
            this.mGuideText.setText(Html.fromHtml(this.mGet.getActivity().getResources().getString(FunctionProperties.isUseSuperBright() ? C0088R.string.super_bright_camera_toast_suggestion_rev2 : C0088R.string.bright_mode_toast_suggestion_rev2, new Object[]{"<img src=\"add_icon\"/>"}), new C08351(), null));
            this.mGuideText.setContentDescription(this.mGet.getActivity().getString(descResId));
            setLayoutLocation();
            updateBinningIcon();
            setButtonListener();
            setCurrentPictureSize();
        }
    }

    private void initializeBinningSetting() {
        if (FunctionProperties.checkBinningType() == 1) {
            BINNING_CAPTURE_TYPE = ParamConstants.OUTPUTS_DEFAULT_STR;
            BINNING_RECORD_TYPE = ParamConstants.OUTPUTS_DEFAULT_STR;
        } else if (FunctionProperties.checkBinningType() == 2) {
            BINNING_CAPTURE_TYPE = ParamConstants.OUTPUTS_PREVIEW_FULLFRAME_STR;
            BINNING_RECORD_TYPE = ParamConstants.OUTPUTS_PREVIEW_RECORD_FULLFRAME_STR;
        }
        if (FunctionProperties.getSupportedHal() == 2) {
            LOW_LIGHT_10_LUX_NORMAL = 365.0f;
            LOW_LIGHT_3_LUX_NORMAL = 405.0f;
            LOW_LIGHT_10_LUX_BINNING = 305.0f;
        }
    }

    private void initializeBinningSize() {
        this.mIsBinningState = false;
        initRearBinningSize();
        initRearWideBinningSize();
    }

    private void initRearBinningSize() {
        if (FunctionProperties.isSupportedBinning(0)) {
            if (this.mRearCameraSizeMap == null) {
                this.mRearCameraSizeMap = new HashMap();
            } else {
                this.mRearCameraSizeMap.clear();
            }
            putBinningSize(this.mRearCameraSizeMap, ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_BINNING_REAR_SUPPORTED_ITEMS);
        }
    }

    private void initRearWideBinningSize() {
        if (FunctionProperties.isSupportedBinning(2)) {
            if (this.mRearWideCameraSizeMap == null) {
                this.mRearWideCameraSizeMap = new HashMap();
            } else {
                this.mRearWideCameraSizeMap.clear();
            }
            putBinningSize(this.mRearWideCameraSizeMap, ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_BINNING_SUB_REAR_SUPPORTED_ITEMS);
        }
    }

    private void putBinningSize(HashMap<String, String> map, String[] size) {
        for (String contentSize : size) {
            map.put(getCalSize(RatioCalcUtil.getRatio(contentSize)), contentSize);
        }
    }

    private String getCalSize(float size) {
        float sizetmp = ((float) ((int) (size * 100.0f))) / 100.0f;
        if (sizetmp > 2.0f) {
            sizetmp = 2.0f;
        }
        return sizetmp + "";
    }

    private String getContentDescMsg() {
        return getActivity().getString(FunctionProperties.isUseSuperBright() ? C0088R.string.setting_super_bright_camera : C0088R.string.setting_bright_mode);
    }

    private void setButtonListener() {
        this.mBinningIcon.setOnClickListener(new C08362());
    }

    private boolean checkBinningManualOff() {
        return (this.mIsBinningState || this.mBinningOffManually) ? false : true;
    }

    public boolean checkAvailableChangeBinningMode() {
        switch (this.mGet.getFocusState()) {
            case 1:
            case 2:
                return false;
            default:
                return true;
        }
    }

    public void setBinningPictureSizeDirect(int whereFrom) {
        if (FunctionProperties.isSupportedBinning() && !this.mIsBinningSettingProcessing && checkAvailableChangeBinningMode()) {
            CamLog.m3d(CameraConstants.TAG, "[NightVision] setBinningPictureSizeDirect, whereFrom : " + whereFrom);
            this.mIsBinningSettingProcessing = true;
            this.mGet.setPreviewForBinning(true);
            this.mGet.runOnUiThread(this.mStopAnimHandler);
            setCurrentPictureSize();
            this.mBinningPictureSize = getBinningPictureSize(this.mGet.getCameraId());
            updateBinningIcon();
            if (this.mGet.checkModuleValidate(192)) {
                updateBinningSettings(this.mIsBinningState);
                updateBinningParameters();
            }
            if (whereFrom != 1) {
                this.mGet.setPreviewForBinning(false);
            }
            this.mGet.postOnUiThread(this.mResetBinningProcessing, 1000);
        }
    }

    public String getBinningPictureSize(int camId) {
        String picSize = this.mGet.getCameraState() == 5 ? this.mGet.getSettingValue(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), camId)) : this.mCurrentPictureSize;
        switch (camId) {
            case 0:
                if (FunctionProperties.isSupportedBinning(0)) {
                    return (String) this.mRearCameraSizeMap.get(getCalSize(RatioCalcUtil.getRatio(picSize)));
                }
                return picSize;
            case 2:
                if (FunctionProperties.isSupportedBinning(2)) {
                    return (String) this.mRearWideCameraSizeMap.get(getCalSize(RatioCalcUtil.getRatio(picSize)));
                }
                return picSize;
            default:
                return ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_BINNING_REAR_SUPPORTED_ITEMS[0];
        }
    }

    private void setLayoutLocation() {
        if (this.mGet.getShotMode().contains(CameraConstants.MODE_SMART_CAM)) {
            sBinningButtonBottomMargin = -500;
        } else {
            sBinningButtonBottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.37f);
        }
        LayoutParams param = (LayoutParams) this.mBinningIcon.getLayoutParams();
        param.bottomMargin = sBinningButtonBottomMargin;
        this.mBinningIcon.setLayoutParams(param);
    }

    public void setBinningIconVisibility(boolean visible, int whereFrom) {
        setBinningIconVisibility(visible, false, true, whereFrom);
    }

    public void setBinningIconVisibility(boolean visible, boolean isForced, boolean useAnim, int whereFrom) {
        if (this.mBinningRelatviewLayout != null && !this.mGet.isPaused() && getBinningSettingValue() && !checkSnapshotProcForcely()) {
            if (getBinningIconVisibility() == visible && !isForced && whereFrom == 0) {
                this.mGet.onBinningIconVisible(visible, whereFrom);
                return;
            }
            if (this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL) || !this.mGet.checkModuleValidate(192)) {
                if (this.mGet.getShotMode().contains(CameraConstants.MODE_SMART_CAM) && this.mGet.isMenuShowing(4)) {
                    CamLog.m11w(CameraConstants.TAG, "AI-Cam, visible : " + visible + ", keeping current visible.");
                } else {
                    visible = false;
                }
            }
            if (this.mIsBinningState && "manual".equals(this.mGet.getSettingValue(Setting.KEY_BINNING))) {
                useAnim = false;
            }
            if (!visible) {
                removeHandlers();
                this.mGet.runOnUiThread(this.mHideHandler);
            } else if (isForced) {
                removeHandlers();
                this.mGet.runOnUiThread(this.mShowHandler);
                this.mIsBinningState = true;
                if (this.mNotNeedRestartPreview) {
                    CamLog.m3d(CameraConstants.TAG, "[NightVision] skip restart preview, already turned on");
                    this.mNotNeedRestartPreview = false;
                } else {
                    CamLog.m3d(CameraConstants.TAG, "[NightVision] force binning on");
                    setBinningPictureSizeDirect(whereFrom);
                }
            } else if (useAnim) {
                AnimationUtil.startShowingBinningIconAnimation(this.mBinningRelatviewLayout, true, 600, null, true);
                this.mGet.postOnUiThread(this.mHapticHandler1, 0);
                this.mGet.postOnUiThread(this.mHapticHandler2, 1200);
                this.mGet.postOnUiThread(this.mHapticHandler3, 2400);
                showGuideText(true);
                this.mGet.postOnUiThread(this.mStopAnimHandler, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
            } else {
                removeHandlers();
                this.mGet.runOnUiThread(this.mShowHandler);
            }
            setCurrentPictureSize();
            updateBinningIcon();
            this.mGet.onBinningIconVisible(visible, whereFrom);
        }
    }

    private void removeHandlers() {
        this.mGet.removePostRunnable(this.mStopAnimHandler);
        this.mGet.removePostRunnable(this.mShowHandler);
        this.mGet.removePostRunnable(this.mHideHandler);
        removeHapticHandlers();
    }

    private void removeHapticHandlers() {
        this.mGet.removePostRunnable(this.mHapticHandler1);
        this.mGet.removePostRunnable(this.mHapticHandler2);
        this.mGet.removePostRunnable(this.mHapticHandler3);
    }

    public boolean getBinningIconVisibility() {
        if (this.mBinningRelatviewLayout == null || this.mBinningRelatviewLayout.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    private void updateBinningIcon() {
        this.mBinningIcon.setBackgroundResource(this.mIsBinningState ? C0088R.drawable.btn_quickbutton_lowlight_pressed : C0088R.drawable.btn_quickbutton_lowlight_normal);
    }

    private void updateBinningSettings(boolean isBinningOn) {
        if (this.mGet != null) {
            this.mGet.setBinningSettings(isBinningOn);
        }
    }

    private void updateBinningParameters() {
        if (!this.mGet.isModuleChanging() && !this.mGet.isPaused()) {
            CameraProxy cameraDevice = this.mGet.getCameraDevice();
            if (cameraDevice != null) {
                CameraParameters parameters = cameraDevice.getParameters();
                if (parameters != null) {
                    CamLog.m3d(CameraConstants.TAG, "[NightVision] updateBinningParameters");
                    parameters.set(ParamConstants.KEY_BINNING_PARAM, this.mIsBinningState ? ParamConstants.VALUE_BINNING_MODE : "normal");
                    parameters.set("picture-size", this.mIsBinningState ? this.mBinningPictureSize : this.mCurrentPictureSize);
                    cameraDevice.setParameters(parameters);
                }
            }
        }
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        showGuideText(false);
        removeHandlers();
        if (this.mBinningRelatviewLayout != null) {
            this.mBinningRelatviewLayout.clearAnimation();
            this.mBinningRelatviewLayout.setVisibility(8);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        removeLayout();
    }

    private void removeLayout() {
        if (this.mBinningRootView != null) {
            this.mBinningRootView.setVisibility(8);
        }
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (!(vg == null || this.mBinningRootView == null)) {
            vg.removeView(this.mBinningRootView);
        }
        this.mBinningRootView = null;
    }

    public void onConfigurationChanged(Configuration config) {
        CamLog.m3d(CameraConstants.TAG, "[NightVision] onConfigurationChanged");
        removeLayout();
        initLayout();
        super.onConfigurationChanged(config);
    }

    public void setDegree(int degree, boolean animation) {
        super.setDegree(degree, animation);
        if (this.mBinningIcon != null) {
            this.mBinningIcon.setDegree(degree, animation);
            setTextViewParam();
        }
    }

    public void setRotateDegree(int degree, boolean animation) {
        if (this.mGuideLayout != null && this.mGuideLayout.getVisibility() == 0) {
            this.mGuideLayout.rotateLayout(degree);
        }
    }

    public void showGuideText(boolean show) {
        if (this.mGuideLayout != null) {
            if (!show || this.mGet.isNightVisionGuideShown()) {
                this.mGuideLayout.setVisibility(8);
                return;
            }
            this.mGuideLayout.setVisibility(0);
            this.mGet.setNightVisionGuideShown();
            if (show) {
                setRotateDegree(this.mGet.getOrientationDegree(), false);
            }
        }
    }

    /* JADX WARNING: Missing block: B:5:0x0042, code:
            r9.mGuideText.setLayoutParams(r1);
            r9.mGuideLayout.setLayoutParams(r2);
     */
    /* JADX WARNING: Missing block: B:7:0x0073, code:
            r1.addRule(12);
            r2.width = com.lge.camera.util.RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 1.0f);
            r2.height = getPreivewHeightRatio();
            r2.topMargin = getPreivewMarginRatio();
            r1.bottomMargin = com.lge.camera.util.RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.16f);
     */
    /* JADX WARNING: Missing block: B:9:0x00da, code:
            r1.addRule(12);
            r2.width = com.lge.camera.util.RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 1.0f);
            r2.height = com.lge.camera.util.RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 1.0f);
            r1.bottomMargin = com.lge.camera.util.RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.36f);
            r1.rightMargin = (int) com.lge.camera.util.Utils.dpToPx(r9.mGet.getAppContext(), 50.0f);
            r1.leftMargin = (int) com.lge.camera.util.Utils.dpToPx(r9.mGet.getAppContext(), 50.0f);
     */
    /* JADX WARNING: Missing block: B:12:?, code:
            return;
     */
    protected void setTextViewParam() {
        /*
        r9 = this;
        r4 = 10;
        r8 = 1;
        r7 = 1112014848; // 0x42480000 float:50.0 double:5.49408334E-315;
        r6 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r5 = 0;
        r3 = r9.mGuideText;
        r1 = r3.getLayoutParams();
        r1 = (android.widget.RelativeLayout.LayoutParams) r1;
        r3 = r9.mGuideLayout;
        r2 = r3.getLayoutParams();
        r2 = (android.widget.RelativeLayout.LayoutParams) r2;
        if (r1 == 0) goto L_0x001c;
    L_0x001a:
        if (r2 != 0) goto L_0x001d;
    L_0x001c:
        return;
    L_0x001d:
        r3 = r9.mGet;
        r0 = r3.getOrientationDegree();
        r3 = 12;
        r1.removeRule(r3);
        r1.removeRule(r4);
        r2.bottomMargin = r5;
        r2.topMargin = r5;
        r2.rightMargin = r5;
        r2.leftMargin = r5;
        r2.height = r5;
        r2.width = r5;
        r1.rightMargin = r5;
        r1.leftMargin = r5;
        r1.bottomMargin = r5;
        r1.topMargin = r5;
        switch(r0) {
            case 0: goto L_0x00da;
            case 90: goto L_0x004d;
            case 180: goto L_0x009c;
            case 270: goto L_0x0073;
            default: goto L_0x0042;
        };
    L_0x0042:
        r3 = r9.mGuideText;
        r3.setLayoutParams(r1);
        r3 = r9.mGuideLayout;
        r3.setLayoutParams(r2);
        goto L_0x001c;
    L_0x004d:
        r1.addRule(r4);
        r3 = r9.getAppContext();
        r3 = com.lge.camera.util.RatioCalcUtil.getSizeCalculatedByPercentage(r3, r5, r6);
        r2.width = r3;
        r3 = r9.getPreivewHeightRatio();
        r2.height = r3;
        r3 = r9.getPreivewMarginRatio();
        r2.topMargin = r3;
        r3 = r9.getAppContext();
        r4 = 1042536202; // 0x3e23d70a float:0.16 double:5.15081322E-315;
        r3 = com.lge.camera.util.RatioCalcUtil.getSizeCalculatedByPercentage(r3, r5, r4);
        r1.topMargin = r3;
    L_0x0073:
        r3 = 12;
        r1.addRule(r3);
        r3 = r9.getAppContext();
        r3 = com.lge.camera.util.RatioCalcUtil.getSizeCalculatedByPercentage(r3, r5, r6);
        r2.width = r3;
        r3 = r9.getPreivewHeightRatio();
        r2.height = r3;
        r3 = r9.getPreivewMarginRatio();
        r2.topMargin = r3;
        r3 = r9.getAppContext();
        r4 = 1042536202; // 0x3e23d70a float:0.16 double:5.15081322E-315;
        r3 = com.lge.camera.util.RatioCalcUtil.getSizeCalculatedByPercentage(r3, r5, r4);
        r1.bottomMargin = r3;
        goto L_0x0042;
    L_0x009c:
        r1.addRule(r4);
        r3 = r9.getAppContext();
        r3 = com.lge.camera.util.RatioCalcUtil.getSizeCalculatedByPercentage(r3, r5, r6);
        r2.width = r3;
        r3 = r9.getAppContext();
        r3 = com.lge.camera.util.RatioCalcUtil.getSizeCalculatedByPercentage(r3, r8, r6);
        r2.height = r3;
        r3 = r9.getAppContext();
        r4 = 1052266988; // 0x3eb851ec float:0.36 double:5.19888969E-315;
        r3 = com.lge.camera.util.RatioCalcUtil.getSizeCalculatedByPercentage(r3, r8, r4);
        r1.topMargin = r3;
        r3 = r9.mGet;
        r3 = r3.getAppContext();
        r3 = com.lge.camera.util.Utils.dpToPx(r3, r7);
        r3 = (int) r3;
        r1.rightMargin = r3;
        r3 = r9.mGet;
        r3 = r3.getAppContext();
        r3 = com.lge.camera.util.Utils.dpToPx(r3, r7);
        r3 = (int) r3;
        r1.leftMargin = r3;
    L_0x00da:
        r3 = 12;
        r1.addRule(r3);
        r3 = r9.getAppContext();
        r3 = com.lge.camera.util.RatioCalcUtil.getSizeCalculatedByPercentage(r3, r5, r6);
        r2.width = r3;
        r3 = r9.getAppContext();
        r3 = com.lge.camera.util.RatioCalcUtil.getSizeCalculatedByPercentage(r3, r8, r6);
        r2.height = r3;
        r3 = r9.getAppContext();
        r4 = 1052266988; // 0x3eb851ec float:0.36 double:5.19888969E-315;
        r3 = com.lge.camera.util.RatioCalcUtil.getSizeCalculatedByPercentage(r3, r8, r4);
        r1.bottomMargin = r3;
        r3 = r9.mGet;
        r3 = r3.getAppContext();
        r3 = com.lge.camera.util.Utils.dpToPx(r3, r7);
        r3 = (int) r3;
        r1.rightMargin = r3;
        r3 = r9.mGet;
        r3 = r3.getAppContext();
        r3 = com.lge.camera.util.Utils.dpToPx(r3, r7);
        r3 = (int) r3;
        r1.leftMargin = r3;
        goto L_0x0042;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.BinningManager.setTextViewParam():void");
    }

    private int getPreivewHeightRatio() {
        return (int) (((float) Utils.getLCDsize(this.mGet.getAppContext(), true)[1]) * RatioCalcUtil.getRatio(this.mGet.getCurrentSelectedPictureSize()));
    }

    private int getPreivewMarginRatio() {
        int[] size = getPreviewScreenSize();
        if (size == null) {
            return 0;
        }
        int[] lcd_size = Utils.getLCDsize(this.mGet.getAppContext(), true);
        int startMargin = 0;
        float ratio = ((float) size[0]) / ((float) size[1]);
        if (ModelProperties.isLongLCDModel()) {
            startMargin = RatioCalcUtil.getLongLCDModelTopMargin(this.mGet.getAppContext(), size[0], size[1], 0);
        } else if (ratio < 1.5f && ratio > 1.1f) {
            startMargin = RatioCalcUtil.getQuickButtonWidth(getAppContext());
        } else if (ratio < 1.1f && ratio > 0.9f) {
            startMargin = (lcd_size[0] - size[0]) / 2;
        }
        return RatioCalcUtil.getTilePreviewMargin(getAppContext(), "on".equals(this.mGet.getSettingValue(Setting.KEY_TILE_PREVIEW)), ratio, startMargin);
    }

    private int[] getPreviewScreenSize() {
        ListPreference listPref;
        if (this.mGet.isVideoCaptureMode()) {
            listPref = (ListPreference) this.mGet.getListPreference(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()));
        } else {
            listPref = (ListPreference) this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()));
        }
        if (listPref == null) {
            return null;
        }
        String screenSize = listPref.getExtraInfo(2);
        if (screenSize != null) {
            return Utils.sizeStringToArray(screenSize);
        }
        return null;
    }

    private boolean getBinningSettingValue() {
        return !"off".equals(this.mGet.getSettingValue(Setting.KEY_BINNING));
    }

    public boolean isBinningEnabled() {
        return getBinningSettingValue() && this.mIsBinningState;
    }

    public void setBinningEnabled(boolean enable) {
        CamLog.m3d(CameraConstants.TAG, "[NightVision]###setBinningEnabled : " + enable);
        this.mIsBinningState = enable;
    }

    public void setCurrentPictureSize() {
        this.mCurrentPictureSize = this.mGet.getCurrentSelectedPictureSize();
    }

    public void showBinningIcon(int lowLightState, boolean isMultiShot, int whereFrom) {
        if (this.mGet.isPaused() || !this.mGet.checkModuleValidate(192)) {
            CamLog.m7i(CameraConstants.TAG, "[NightVision] showBinningIcon recording state return.");
            return;
        }
        final int i = lowLightState;
        final boolean z = isMultiShot;
        final int i2 = whereFrom;
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if ("off".equals(BinningManager.this.mGet.getSettingValue(Setting.KEY_BINNING))) {
                    BinningManager.this.setBinningIconVisibility(false, i2);
                    return;
                }
                if (!FunctionProperties.isSupportedBinning(BinningManager.this.mGet.getCameraId()) || i == 0) {
                    if (BinningManager.this.isBinningEnabled() && !z) {
                        BinningManager.this.setBinningEnabled(false);
                        CamLog.m3d(CameraConstants.TAG, "[NightVision] show binning icon binning off condition");
                        BinningManager.this.setBinningPictureSizeDirect(i2);
                    }
                    BinningManager.this.setBinningIconVisibility(false, i2);
                } else if (BinningManager.this.getBinningIconVisibility() || BinningManager.this.mGet.isAnimationShowing()) {
                    if (BinningManager.this.getBinningIconVisibility() && !BinningManager.this.isBinningEnabled() && i == 2) {
                        BinningManager.this.setBinningIconVisibility(true, true, false, i2);
                    } else {
                        BinningManager.this.mGet.onBinningIconVisible(BinningManager.this.getBinningIconVisibility(), i2);
                    }
                } else if (i == 1) {
                    BinningManager.this.setBinningIconVisibility(true, i2);
                } else if (i == 2) {
                    BinningManager.this.setBinningIconVisibility(true, true, false, i2);
                } else if (i == 3) {
                    BinningManager.this.setBinningIconVisibility(true, false, false, i2);
                }
                if (BinningManager.this.mGet.isTimerShotCountdown() || BinningManager.this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL) || z) {
                    boolean visible = false;
                    if (BinningManager.this.mGet.getShotMode().contains(CameraConstants.MODE_SMART_CAM) && BinningManager.this.mGet.isMenuShowing(4)) {
                        visible = i == 2;
                    }
                    BinningManager.this.setBinningIconVisibility(visible, true, false, i2);
                }
            }
        });
    }

    public boolean checkBinningOffManually() {
        return this.mBinningOffManually;
    }

    public void resetManuallyOffState() {
        this.mBinningOffManually = false;
    }

    public boolean isBinningSettingProcessing() {
        return this.mIsBinningSettingProcessing;
    }

    public boolean checkSnapshotProcForcely() {
        SnapShotChecker snapshotChecker = this.mGet.getSnapshotChecker();
        if (snapshotChecker == null) {
            return false;
        }
        if ((snapshotChecker.getSnapShotState() < 2 || snapshotChecker.getPictureCallbackState() >= 1) && !snapshotChecker.isSnapShotProcessing()) {
            return false;
        }
        return true;
    }

    public void setNotNeedRestartPreviewFlag() {
        if (FunctionProperties.getSupportedHal() == 2) {
            this.mNotNeedRestartPreview = true;
        }
    }

    public boolean checkChangeBinningSize() {
        if (!isBinningEnabled() || FunctionProperties.getSupportedHal() != 2) {
            return false;
        }
        setCurrentPictureSize();
        setNotNeedRestartPreviewFlag();
        return true;
    }

    public String getBinningCaptureOutputConfig() {
        return BINNING_CAPTURE_TYPE;
    }

    public String getBinningRecordingOutputConfig() {
        return BINNING_RECORD_TYPE;
    }

    public String getBinningType() {
        if (FunctionProperties.checkBinningType() == 0) {
            return "none";
        }
        if (FunctionProperties.checkBinningType() == 1) {
            return ParamConstants.VALUE_BINNING_SENSOR;
        }
        return ParamConstants.VALUE_BINNING_SWPIXEL;
    }
}
