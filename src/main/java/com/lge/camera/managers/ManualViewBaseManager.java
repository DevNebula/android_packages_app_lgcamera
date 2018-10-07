package com.lge.camera.managers;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.DrumBarController;
import com.lge.camera.components.DrumController;
import com.lge.camera.components.ManualPanelButton;
import com.lge.camera.components.RatioGuideView;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.file.ExifInterface.GpsSpeedRef;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.ManualModeItem;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class ManualViewBaseManager extends ManagerInterfaceImpl {
    protected static final float DRUM_MARGIN_END_PERCENTAGE = 0.164f;
    protected static final float DRUM_MARGIN_END_PERCENTAGE_FULL_VISION_LCD = 0.217f;
    protected static final float DRUM_MARGIN_END_PERCENTAGE_LONG_LCD = 0.185f;
    protected static final float MANUAL_CAMERA_EMPTY_AREA_WIDTH_PERCENTAGE_LONG_LCD = 0.1166f;
    protected static final float MANUAL_CAMERA_PANEL_ADDITIONAL_MARGIN_LONG_LCD = 0.07639f;
    protected static final float MANUAL_PANEL_BUTTONS_HEIGHT = 0.09f;
    protected static final float MANUAL_PANEL_HEIGHT_LONG_LCD = 0.10277f;
    protected static final float MANUAL_VIDEO_PANEL_ADDITIONAL_MARGIN_LONG_LCD = 0.1055f;
    protected static final float MANUAL_VIDEO_TOTAL_PANEL_WIDTH_FULL_VISION_LCD = 0.74f;
    protected static final float MANUAL_VIDEO_TOTAL_PANEL_WIDTH_LONG_LCD = 0.757f;
    public static final String MODEITEM_EXPOSURE_VALUE = "Exposure value";
    public static final String MODEITEM_ISO = "ISO";
    public static final String MODEITEM_MANUAL_FOCUS = "Manual focus";
    public static final String MODEITEM_SHUTTER_SPEED = "Shutter speed";
    public static final String MODEITEM_WHITE_BALANCE = "White balance";
    protected static final int MSG_DELAY = 1000;
    protected static final int MSG_RESET_BTN_COLOR = 2;
    protected static final int MSG_START_SPIN_EFFECT = 0;
    protected static final int MSG_STOP_SPIN_EFFECT = 1;
    protected static final float PEAKING_MARGIN_END_PERCENTAGE = 0.1651f;
    protected static final float PEAKING_MARGIN_END_PERCENTAGE_LONG_LCD = 0.1491f;
    protected HashMap<Integer, RotateImageButton> mAutoButtonMap = new HashMap();
    protected ManualPanelButton mBtnAELock = null;
    protected ManualPanelButton mBtnAllAuto = null;
    protected ManualPanelButton mBtnEV = null;
    protected ManualPanelButton mBtnFocus = null;
    protected ManualPanelButton mBtnISO = null;
    protected RotateImageButton mBtnLogDisplayLUT = null;
    protected RotateImageButton mBtnPeaking = null;
    protected ManualPanelButton mBtnSS = null;
    protected ManualPanelButton mBtnWB = null;
    protected RotateImageButton mDrumBtnAF = null;
    protected RotateImageButton mDrumBtnAWB = null;
    protected RotateImageButton mDrumBtnISO = null;
    protected RotateImageButton mDrumBtnSS = null;
    protected HashMap<Integer, DrumController> mDrumMap = new HashMap();
    protected DrumController mExposureValue = null;
    protected DrumController mISO = null;
    protected boolean mInit = false;
    protected boolean mIsFirstDisplayLUT;
    protected boolean mIsLogDisplayLUT = true;
    protected RotateLayout mManualBasePanelView;
    protected View mManualBaseView;
    protected DrumBarController mManualFocus = null;
    protected ManualModuleInterface mManualGet = null;
    protected View mManualPanelView;
    protected List<ManualModeItem> mModeItemList = new ArrayList();
    protected HashMap<Integer, ManualPanelButton> mPanelButtonMap = new HashMap();
    protected View mRatioGuide = null;
    protected HashMap<Integer, String> mSettingKeyMap = new HashMap();
    protected DrumController mShutterSpeed = null;
    protected DrumController mWhiteBalance = null;

    public ManualViewBaseManager(ManualModuleInterface moduleInterface) {
        super(moduleInterface);
        this.mManualGet = moduleInterface;
    }

    public void setVideoSettingInfo() {
        if (this.mGet != null && !ManualUtil.isManualCameraMode(this.mGet.getShotMode())) {
            LinearLayout videoSettingInfoLayout = (LinearLayout) this.mGet.findViewById(C0088R.id.manual_video_setting_info);
            if (videoSettingInfoLayout != null) {
                String desc = this.mManualGet.getSettingDesc(this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE));
                if (desc != null) {
                    desc = desc.replaceAll("\\s", "");
                    TextView tv1 = (TextView) videoSettingInfoLayout.findViewById(C0088R.id.manual_video_setting_resolution);
                    if (tv1 != null) {
                        tv1.setText(desc);
                    }
                    TextView tv2 = (TextView) videoSettingInfoLayout.findViewById(C0088R.id.manual_video_setting_frame_rate);
                    String fps = this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE);
                    if (tv2 != null) {
                        tv2.setText(fps);
                    }
                    String bitrate = this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_BITRATE);
                    int valueInt = 0;
                    try {
                        valueInt = Integer.parseInt(bitrate);
                    } catch (NumberFormatException e) {
                        CamLog.m3d(CameraConstants.TAG, "NumberFormatException occured, value = " + bitrate);
                    }
                    bitrate = String.valueOf((int) (((double) valueInt) / Math.pow(10.0d, 6.0d))) + " Mbps";
                    TextView tv3 = (TextView) videoSettingInfoLayout.findViewById(C0088R.id.manual_video_setting_bitrate);
                    if (tv3 != null) {
                        tv3.setText(bitrate);
                    }
                    if (videoSettingInfoLayout.getVisibility() != 0) {
                        videoSettingInfoLayout.setVisibility(0);
                    }
                    if (FunctionProperties.isSupportedLogProfile() || FunctionProperties.isSupportedHDR10()) {
                        TextView videoType = (TextView) this.mGet.findViewById(C0088R.id.manual_video_setting_type);
                        if (videoType == null) {
                            return;
                        }
                        if ((this.mManualGet.hasLogProfileLimitation() || !"on".equals(this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG))) && (this.mManualGet.hasHDR10Limitation() || !"on".equals(this.mGet.getSettingValue(Setting.KEY_HDR10)))) {
                            videoType.setVisibility(8);
                            tv1.setIncludeFontPadding(true);
                            tv2.setIncludeFontPadding(true);
                            tv3.setIncludeFontPadding(true);
                            videoType.setIncludeFontPadding(true);
                            return;
                        }
                        if (FunctionProperties.isSupportedHDR10()) {
                            videoType.setText(C0088R.string.hdr10);
                        }
                        videoType.setVisibility(0);
                        tv1.setIncludeFontPadding(false);
                        tv2.setIncludeFontPadding(false);
                        tv3.setIncludeFontPadding(false);
                        videoType.setIncludeFontPadding(false);
                    }
                }
            }
        }
    }

    protected void rotatePanelButtons(int degree) {
        if (degree == 90 || degree == 270) {
            int convertDegree = (degree + 90) % 360;
            for (Integer intValue : this.mPanelButtonMap.keySet()) {
                ManualPanelButton manualButton = (ManualPanelButton) this.mPanelButtonMap.get(Integer.valueOf(intValue.intValue()));
                if (manualButton != null) {
                    manualButton.setDegree(convertDegree, false);
                }
            }
            if (this.mBtnAELock != null) {
                this.mBtnAELock.setDegree(convertDegree, false);
            }
            if (this.mBtnAllAuto != null) {
                this.mBtnAllAuto.setDegree(convertDegree, false);
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        destroyViews();
        this.mInit = false;
    }

    public void destroyViews() {
        if (this.mManualBasePanelView != null) {
            this.mManualBasePanelView.removeAllViews();
        }
        if (this.mManualBaseView != null) {
            ((ViewGroup) this.mManualBaseView).removeAllViews();
            ViewGroup contentBase = (ViewGroup) this.mManualGet.findViewById(C0088R.id.contents_base);
            if (contentBase != null) {
                contentBase.removeView(this.mManualBaseView);
            }
        }
    }

    protected int getPannelButtonCnt() {
        int cnt = 1;
        for (int features = this.mManualGet.getSupportedFeature(); features != 0; features >>= 1) {
            if ((features & 1) == 1) {
                cnt++;
            }
        }
        return cnt;
    }

    protected void makePanelButtonMap() {
        if (this.mPanelButtonMap != null) {
            this.mPanelButtonMap.put(Integer.valueOf(4), this.mBtnWB);
            this.mPanelButtonMap.put(Integer.valueOf(16), this.mBtnFocus);
            this.mPanelButtonMap.put(Integer.valueOf(2), this.mBtnEV);
            this.mPanelButtonMap.put(Integer.valueOf(8), this.mBtnISO);
            this.mPanelButtonMap.put(Integer.valueOf(1), this.mBtnSS);
        }
    }

    protected void makeAutoButtonMap() {
        if (this.mAutoButtonMap != null) {
            this.mAutoButtonMap.put(Integer.valueOf(4), this.mDrumBtnAWB);
            this.mAutoButtonMap.put(Integer.valueOf(16), this.mDrumBtnAF);
            this.mAutoButtonMap.put(Integer.valueOf(8), this.mDrumBtnISO);
            this.mAutoButtonMap.put(Integer.valueOf(1), this.mDrumBtnSS);
        }
    }

    protected void makeDrumMap() {
        if (this.mDrumMap != null) {
            this.mDrumMap.put(Integer.valueOf(4), this.mWhiteBalance);
            this.mDrumMap.put(Integer.valueOf(16), this.mManualFocus);
            this.mDrumMap.put(Integer.valueOf(2), this.mExposureValue);
            this.mDrumMap.put(Integer.valueOf(8), this.mISO);
            this.mDrumMap.put(Integer.valueOf(1), this.mShutterSpeed);
        }
    }

    protected void makeSettingKeyList() {
        if (this.mManualGet != null) {
            String shotMode = this.mManualGet.getShotMode();
            if (this.mSettingKeyMap != null && shotMode != null) {
                this.mSettingKeyMap.put(Integer.valueOf(4), SettingKeyWrapper.getManualSettingKey(shotMode, "lg-wb"));
                this.mSettingKeyMap.put(Integer.valueOf(16), SettingKeyWrapper.getManualSettingKey(shotMode, Setting.KEY_MANUAL_FOCUS_STEP));
                this.mSettingKeyMap.put(Integer.valueOf(2), SettingKeyWrapper.getManualSettingKey(shotMode, Setting.KEY_LG_EV_CTRL));
                this.mSettingKeyMap.put(Integer.valueOf(8), SettingKeyWrapper.getManualSettingKey(shotMode, Setting.KEY_LG_MANUAL_ISO));
                this.mSettingKeyMap.put(Integer.valueOf(1), SettingKeyWrapper.getManualSettingKey(shotMode, "shutter-speed"));
            }
        }
    }

    protected String getModeKey(int type) {
        switch (type) {
            case 1:
                return MODEITEM_SHUTTER_SPEED;
            case 2:
                return MODEITEM_EXPOSURE_VALUE;
            case 4:
                return MODEITEM_WHITE_BALANCE;
            case 8:
                return "ISO";
            case 16:
                return "Manual focus";
            default:
                return MODEITEM_WHITE_BALANCE;
        }
    }

    protected void buildManualItem(CameraParameters parameter, int type) {
        if (parameter != null) {
            if ((type & 4) != 0) {
                buildLgWbItem(parameter);
            }
            if ((type & 16) != 0) {
                buildManualFocusItem(parameter);
            }
            if ((type & 2) != 0) {
                buildExposureValueItem(parameter);
            }
            if ((type & 8) != 0) {
                buildISOItem(parameter);
            }
            if ((type & 1) != 0) {
                buildShutterSpeedItem(parameter, true);
            }
        }
    }

    protected void buildLgWbItem(CameraParameters parameter) {
        ManualData wbData = this.mManualGet.getManualData(4);
        if (parameter != null && wbData != null && wbData.getEntryArray() != null) {
            Resources res = this.mManualGet.getAppContext().getResources();
            String[] entries = res.getStringArray(C0088R.array.white_balance_entries);
            TypedArray typedIcons = res.obtainTypedArray(C0088R.array.white_balance_wheel_icon);
            ManualModeItem modeItem = new ManualModeItem();
            modeItem.setTitle(MODEITEM_WHITE_BALANCE);
            modeItem.setKey("lg-wb");
            modeItem.setSettingKey(wbData.getSettingKey());
            int length = wbData.getValueArray().length;
            Integer[] supportedIcons = new Integer[length];
            int[] barColorArray = new int[length];
            boolean[] showEntryValue = new boolean[length];
            int iconIndex = 0;
            int currentIndex = 0;
            for (String value : wbData.getValueArray()) {
                if (entries[iconIndex].equals(value)) {
                    supportedIcons[currentIndex] = Integer.valueOf(typedIcons.getResourceId(iconIndex, 0));
                    iconIndex++;
                } else {
                    supportedIcons[currentIndex] = Integer.valueOf(-1);
                }
                int intValue = 0;
                try {
                    intValue = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    CamLog.m3d(CameraConstants.TAG, "NumberFormatException occured, value = " + value);
                }
                if (intValue % 200 == 100) {
                    showEntryValue[currentIndex] = false;
                } else {
                    showEntryValue[currentIndex] = true;
                }
                barColorArray[currentIndex] = -1;
                currentIndex++;
            }
            typedIcons.recycle();
            modeItem.setEntries(wbData.getEntryArray());
            modeItem.setValues(wbData.getValueArray());
            modeItem.setIcons(supportedIcons);
            modeItem.setBarColors(barColorArray);
            modeItem.setShowEntryValue(showEntryValue);
            modeItem.setPrefDefaultValue(wbData.getDefaultValue());
            modeItem.setSelectedIndex(-1);
            modeItem.setDefaultValue("0");
            modeItem.setDefaultEntryValue("4500K");
            this.mModeItemList.add(modeItem);
        }
    }

    protected void buildManualFocusItem(CameraParameters parameter) {
        ManualData mfData = this.mManualGet.getManualData(16);
        if (parameter != null && mfData != null && mfData.getEntryArray() != null) {
            String[] entryArray = mfData.getEntryArray();
            int length = entryArray.length;
            ManualModeItem modeItem = new ManualModeItem();
            modeItem.setTitle("Manual focus");
            modeItem.setKey(ParamConstants.MANUAL_FOCUS_STEP);
            modeItem.setSettingKey(mfData.getSettingKey());
            boolean[] showEntryValue = new boolean[length];
            for (int i = 0; i < length; i++) {
                if ("".equals(entryArray[i])) {
                    showEntryValue[i] = false;
                } else {
                    showEntryValue[i] = true;
                }
            }
            modeItem.setEntries(entryArray);
            modeItem.setValues(mfData.getValueArray());
            modeItem.setShowEntryValue(showEntryValue);
            modeItem.setSelectedIndex(0);
            modeItem.setPrefDefaultValue(mfData.getDefaultValue());
            modeItem.setDefaultValue("-1");
            modeItem.setDefaultEntryValue("-1");
            modeItem.setPrefDefaultValue("-1");
            this.mModeItemList.add(modeItem);
        }
    }

    protected void buildISOItem(CameraParameters parameter) {
        ManualData isoData = this.mManualGet.getManualData(8);
        if (parameter != null && isoData != null && isoData.getEntryArray() != null) {
            ManualModeItem modeItem = new ManualModeItem();
            modeItem.setTitle("ISO");
            modeItem.setKey(this.mManualGet.getISOParamKey());
            modeItem.setSettingKey(isoData.getSettingKey());
            int selectedIndex = 0;
            String settingValue = isoData.getValue();
            for (String value : isoData.getValueArray()) {
                if (value != null && value.equals(settingValue)) {
                    break;
                }
                selectedIndex++;
            }
            if (selectedIndex == isoData.getValueArray().length) {
                selectedIndex = -1;
            }
            modeItem.setEntries(isoData.getEntryArray());
            modeItem.setValues(isoData.getValueArray());
            modeItem.setSelectedIndex(selectedIndex);
            modeItem.setDefaultValue(isoData.getDefaultValue());
            modeItem.setDefaultEntryValue(CameraConstants.FPS_50);
            modeItem.setPrefDefaultValue("auto");
            this.mModeItemList.add(modeItem);
        }
    }

    protected void buildExposureValueItem(CameraParameters parameter) {
        ManualData evData = this.mManualGet.getManualData(2);
        if (parameter != null && evData != null && evData.getEntryArray() != null && evData.getValueArray() != null) {
            ManualModeItem modeItem = new ManualModeItem();
            modeItem.setTitle(MODEITEM_EXPOSURE_VALUE);
            modeItem.setKey(ParamConstants.KEY_EXPOSURE_COMPENSATION);
            modeItem.setSettingKey(evData.getSettingKey());
            String settingValue = evData.getValue();
            CamLog.m3d(CameraConstants.TAG, "current EV setting value : " + settingValue);
            if (settingValue == null) {
                settingValue = "0";
            }
            int curIndex = 0;
            int selectedIndex = -1;
            boolean[] showEntryValue = new boolean[evData.getEntryArray().length];
            for (String value : evData.getValueArray()) {
                int valueInt = 0;
                try {
                    valueInt = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    CamLog.m3d(CameraConstants.TAG, "NumberFormatException occured, value = " + value);
                }
                if (valueInt % 3 == 0) {
                    showEntryValue[curIndex] = true;
                } else {
                    showEntryValue[curIndex] = false;
                }
                if (settingValue.equals(value)) {
                    selectedIndex = curIndex;
                }
                curIndex++;
            }
            modeItem.setEntries(evData.getEntryArray());
            modeItem.setValues(evData.getValueArray());
            modeItem.setShowEntryValue(showEntryValue);
            modeItem.setSelectedIndex(selectedIndex);
            modeItem.setPrefDefaultValue(evData.getDefaultValue());
            modeItem.setDefaultValue("0");
            modeItem.setDefaultEntryValue("0.0");
            this.mModeItemList.add(modeItem);
        }
    }

    protected void buildShutterSpeedItem(CameraParameters parameters, boolean isInitializing) {
        ManualData shutterSpeed = this.mManualGet.getManualData(1);
        if (parameters != null && shutterSpeed != null && shutterSpeed.getEntryArray() != null) {
            boolean[] showEntryValue = new boolean[shutterSpeed.getEntryArray().length];
            for (int i = 0; i < shutterSpeed.getEntryArray().length; i++) {
                if (i % 2 == 0) {
                    showEntryValue[i] = true;
                } else {
                    showEntryValue[i] = false;
                }
            }
            ManualModeItem modeItem = new ManualModeItem();
            modeItem.setTitle(MODEITEM_SHUTTER_SPEED);
            modeItem.setKey("shutter-speed");
            modeItem.setSettingKey(shutterSpeed.getSettingKey());
            modeItem.setEntries(shutterSpeed.getEntryArray());
            modeItem.setValues(shutterSpeed.getValueArray());
            modeItem.setSelectedIndex(-1);
            modeItem.setDefaultValue("0");
            modeItem.setDefaultEntryValue("1/30");
            modeItem.setPrefDefaultValue(shutterSpeed.getDefaultValue());
            modeItem.setShowEntryValue(showEntryValue);
            if (isInitializing) {
                this.mModeItemList.add(modeItem);
            }
        }
    }

    protected int getPanelAreaMarginTop() {
        return Utils.getPx(this.mManualGet.getAppContext(), C0088R.dimen.manual_lower_panel_marginEnd);
    }

    public String getLDBString() {
        return "";
    }

    protected boolean isItemPanelWorkable(int manualType) {
        if (this.mManualGet.checkModuleValidate(127) && this.mManualGet.isDrumMovingAvailable()) {
            return true;
        }
        return false;
    }

    protected boolean isDrumControlUnavailable() {
        return this.mManualGet.isSettingMenuVisible() || this.mManualGet.isModeMenuVisible() || this.mManualGet.isHelpListVisible();
    }

    public void setDrumVisiblity(int type, int visibility) {
        for (Integer intValue : this.mDrumMap.keySet()) {
            int key = intValue.intValue();
            if (type != key) {
                DrumController otherDrum = (DrumController) this.mDrumMap.get(Integer.valueOf(key));
                if (otherDrum != null) {
                    otherDrum.setVisibility(visibility);
                }
            }
        }
        this.mManualGet.onDrumVisibilityChanged(type, visibility == 0);
    }

    protected void sendItemClickRunnable(final Runnable runnable) {
        this.mManualGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (runnable != null) {
                    runnable.run();
                }
            }
        }, 0);
    }

    public boolean isDrumShowing(int type) {
        for (Integer intValue : this.mDrumMap.keySet()) {
            int key = intValue.intValue();
            if ((type & key) == key) {
                DrumController otherDrum = (DrumController) this.mDrumMap.get(Integer.valueOf(key));
                if (otherDrum != null && otherDrum.getVisibility() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isDrumMoving(int type) {
        for (Integer intValue : this.mDrumMap.keySet()) {
            int key = intValue.intValue();
            if ((type & key) == key) {
                DrumController drum = (DrumController) this.mDrumMap.get(Integer.valueOf(key));
                if (drum != null && drum.isDrumMoving()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setDrumsEnabled(int type, boolean enable) {
        for (Integer intValue : this.mDrumMap.keySet()) {
            int key = intValue.intValue();
            if ((type & key) == key) {
                DrumController drum = (DrumController) this.mDrumMap.get(Integer.valueOf(key));
                if (drum != null) {
                    drum.setEnabled(enable);
                }
            }
        }
    }

    public void setAutoButtonsEnabled(int type, boolean enable) {
        ColorFilter cf;
        if (enable) {
            cf = ColorUtil.getNormalColorByAlpha();
        } else {
            cf = ColorUtil.getDimColorByAlpha();
        }
        for (Integer intValue : this.mAutoButtonMap.keySet()) {
            int key = intValue.intValue();
            if ((type & key) == key) {
                RotateImageButton autoButton = (RotateImageButton) this.mAutoButtonMap.get(Integer.valueOf(key));
                if (autoButton != null) {
                    autoButton.setEnabled(enable);
                    autoButton.setColorFilter(cf);
                }
            }
        }
        if (this.mBtnPeaking != null) {
            boolean enableFocusPeaking;
            ColorFilter normalColorByAlpha;
            boolean isHDR10Mode = ManualUtil.isManualVideoMode(this.mGet.getShotMode()) && "on".equals(this.mGet.getSettingValue(Setting.KEY_HDR10));
            if (isHDR10Mode) {
                enableFocusPeaking = false;
            } else {
                enableFocusPeaking = enable;
            }
            this.mBtnPeaking.setEnabled(enableFocusPeaking);
            if (isHDR10Mode) {
                this.mBtnPeaking.setSelected(false);
            }
            RotateImageButton rotateImageButton = this.mBtnPeaking;
            if (enableFocusPeaking) {
                normalColorByAlpha = ColorUtil.getNormalColorByAlpha();
            } else {
                normalColorByAlpha = ColorUtil.getDimColorByAlpha();
            }
            rotateImageButton.setColorFilter(normalColorByAlpha);
        }
    }

    public void updateRatioGuideVisibility(int visibility) {
        if (this.mManualGet.isRatioGuideNeeded() && this.mRatioGuide != null) {
            if (!updateGuideViewRatio()) {
                visibility = 8;
            }
            this.mRatioGuide.setVisibility(visibility);
        }
    }

    public int getRatioGuideHeight() {
        if (this.mRatioGuide == null) {
            return 0;
        }
        return ((RatioGuideView) this.mRatioGuide).getRatioGuideHeight();
    }

    public boolean updateGuideViewRatio() {
        if (!this.mManualGet.isRatioGuideNeeded()) {
            return false;
        }
        boolean showRatioGuide = false;
        if (this.mRatioGuide == null) {
            return false;
        }
        String contentSize = this.mManualGet.getContentSize();
        if (contentSize != null) {
            int[] contentSizeArr = Utils.sizeStringToArray(contentSize);
            if (contentSizeArr == null || contentSizeArr.length <= 1) {
                this.mRatioGuide.setVisibility(8);
                return false;
            }
            showRatioGuide = ManualUtil.isCinemaSize(this.mGet.getAppContext(), contentSizeArr[0], contentSizeArr[1]);
            int[] lcdSizeArr = Utils.getLCDsize(this.mGet.getAppContext(), true);
            int previewWidth = lcdSizeArr[0];
            int previewHeight = lcdSizeArr[1];
            if (showRatioGuide) {
                float contentRatio = ((float) contentSizeArr[0]) / ((float) contentSizeArr[1]);
                float lcdSizeRatio = ((float) lcdSizeArr[0]) / ((float) lcdSizeArr[1]);
                if (Float.compare(contentRatio, lcdSizeRatio) > 0) {
                    previewHeight = (int) (((float) lcdSizeArr[0]) * (((float) contentSizeArr[1]) / ((float) contentSizeArr[0])));
                } else if (Float.compare(contentRatio, lcdSizeRatio) < 0) {
                    previewWidth = (int) (((float) lcdSizeArr[1]) * (((float) contentSizeArr[0]) / ((float) contentSizeArr[1])));
                }
                this.mRatioGuide.setVisibility(0);
                ((RatioGuideView) this.mRatioGuide).setPreviewSizeForGuideView(previewWidth, previewHeight);
            }
        }
        if (showRatioGuide) {
            return showRatioGuide;
        }
        this.mRatioGuide.setVisibility(8);
        return showRatioGuide;
    }

    protected boolean setManualData(int type, String key, String value, Boolean isSave) {
        return this.mManualGet.setManualData(type, key, value, isSave.booleanValue());
    }

    public void setFocusPeakingParam(boolean on, boolean save) {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null) {
            CameraParameters parameter = cameraDevice.getParameters();
            if (parameter != null) {
                if (on) {
                    if (!(SharedPreferenceUtil.getFocusPeakingGuide(getAppContext()) || "on".equals(this.mGet.getSettingValue(Setting.KEY_HDR10)))) {
                        this.mGet.showToast(this.mGet.getAppContext().getString(C0088R.string.focus_peaking_toast), CameraConstants.TOAST_LENGTH_LONG);
                        SharedPreferenceUtil.saveFocusPeakingGuide(getAppContext(), true);
                    }
                    this.mGet.setParamUpdater(parameter, ParamConstants.KEY_FOCUS_PEAKING, "on");
                    this.mBtnPeaking.setSelected(true);
                } else {
                    this.mGet.setParamUpdater(parameter, ParamConstants.KEY_FOCUS_PEAKING, "off");
                    this.mBtnPeaking.setSelected(false);
                }
                if (save) {
                    SharedPreferenceUtil.saveFocusPeakingEnable(getAppContext(), on);
                }
                CamLog.m3d(CameraConstants.TAG, "ParamConstants.KEY_FOCUS_PEAKING : " + on);
                this.mGet.setParameters(parameter);
            }
        }
    }

    public void setLogDisplayLUTParam(boolean on) {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null) {
            CameraParameters parameter = cameraDevice.getParameters();
            if (parameter != null) {
                this.mGet.setParamUpdater(parameter, ParamConstants.KEY_CINEMA_MODE, ParamConstants.CINEMA_PREVIEW_ONLY);
                this.mGet.setParamUpdater(parameter, ParamConstants.KEY_CINEMA_VIGNETTE, ParamConstants.CINEMA_MANUAL_VIGNETTE);
                if (on) {
                    this.mGet.setParamUpdater(parameter, ParamConstants.KEY_CINEMA_LUT, ParamConstants.CINEMA_MANUAL_DISPLAY_LUT);
                } else {
                    this.mGet.setParamUpdater(parameter, ParamConstants.KEY_CINEMA_LUT, ParamConstants.CINEMA_MANUAL_VIDEO_VALUE);
                }
                this.mGet.setParameters(parameter);
            }
        }
    }

    protected String getContentDescValue(int type, String value) {
        if (value == null) {
            return "";
        }
        ManualData mb;
        switch (type) {
            case 1:
                value = value + this.mGet.getAppContext().getString(C0088R.string.seconds);
                break;
            case 4:
                if (!value.contains(GpsSpeedRef.KILOMETERS)) {
                    mb = this.mManualGet.getManualData(4);
                    if (mb != null && value.equals(mb.getAutoInfoValue())) {
                        value = this.mGet.getAppContext().getString(C0088R.string.auto);
                        break;
                    }
                }
                value = value.replace(GpsSpeedRef.KILOMETERS, " K");
                break;
            case 16:
                mb = this.mManualGet.getManualData(16);
                if (mb != null) {
                    if (!value.equals(mb.getAutoInfoValue())) {
                        value = this.mGet.getAppContext().getString(C0088R.string.manual_for_camera);
                        break;
                    }
                    value = this.mGet.getAppContext().getString(C0088R.string.auto);
                    break;
                }
                break;
        }
        return value;
    }

    protected void updateButtonValue(int type, String value) {
        if (value != null) {
            ManualPanelButton button = (ManualPanelButton) this.mPanelButtonMap.get(Integer.valueOf(type));
            if (button != null) {
                if ((this.mManualGet.getLockedFeature() & type) == type) {
                    button.setImageResource(C0088R.drawable.bg_manual_control_lock);
                } else {
                    button.setImageResource(C0088R.drawable.btn_manual_panel);
                }
                button.setDrawablesColorFilter(button.isEnabled());
                String prevText = button.getText();
                if (prevText == null || !prevText.equals(value)) {
                    button.setText(value, false);
                    button.setContentDescription(button.getContentDescriptionString() + ", " + getContentDescValue(type, value));
                }
            }
        }
    }

    protected void setAudioButtonLayout(int buttonWidth) {
    }

    public void drawWave() {
    }

    public void stopDrawingWave() {
    }

    public void setSSRSetting(boolean enable) {
    }

    public void setAudioLoopbackInRecording(boolean enable) {
    }

    public void setAudioLoopbackOnPreview(boolean enable) {
    }

    public boolean isLoopbackAvailable() {
        return false;
    }

    public void onHeadsetStateChanged(boolean isConnected) {
    }

    public void onBTConnectionStateChanged(boolean isConnected) {
    }

    public void onBTStateChanged(boolean isOn) {
    }

    public void onBTAudioStateChanged(boolean isConnected) {
    }

    public void setAudioBtnDrawable(boolean isHifiOn) {
    }

    public void setAudioParamOnRecordingStart() {
    }

    public void onFrameRateChanged(String previousFps, String nextFPS) {
    }

    public void audioBtnClicked(boolean clicked) {
    }

    public boolean isAudioControlPanelShowing() {
        return false;
    }

    public boolean isAudioEarphoneGainBtnSelected() {
        return false;
    }
}
