package com.lge.camera.managers;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.graphics.ColorFilter;
import android.media.AudioManagerEx;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.app.VideoRecorder;
import com.lge.camera.components.AudioBalanceController;
import com.lge.camera.components.AudioControlBar;
import com.lge.camera.components.AudioControlBar.OnAudioControlBarListener;
import com.lge.camera.components.AudioControlStepBar;
import com.lge.camera.components.AudioControlStepBar.OnAudioControlStepBarListener;
import com.lge.camera.components.AudioLevelMeter;
import com.lge.camera.components.AudioOffStepBar;
import com.lge.camera.components.DrumController;
import com.lge.camera.components.ManualPanelButton;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.WaveView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.TalkBackUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class ManualAudioControlManagerBase extends ManualViewManager {
    public static final int AUDIOLOOPBACK_MONO_AMPLITUDE = 5;
    public static final int AUDIOLOOPBACK_STEREO_AMPLITUDE = 4;
    public static final int AUDIOLOPBACK_INIT = 0;
    public static final int AUDIOLOPBACK_RELEASE = 3;
    public static final int AUDIOLOPBACK_START = 1;
    public static final int AUDIOLOPBACK_STOP = 2;
    protected static final int DUAL_CONNECTION_DEVICE_TYPE_CHANGE = 10;
    public static final int FPS_TYPE_NORMAL = 0;
    public static final int FPS_TYPE_SLOW_MOTION = 2;
    public static final int FPS_TYPE_TIME_LAPSE = 1;
    public static final String MANUAL_AUDIO_MIC_TYPE_BLUTOOTH = "3";
    public static final String MANUAL_AUDIO_MIC_TYPE_HANDSET = "2";
    public static final String MANUAL_AUDIO_MIC_TYPE_HEADSET = "1";
    protected static final int SET_BLUETOOTH_LOOPBACK = 9;
    protected static final int UPDATE_AUDIO_LEVEL_METER = 8;
    private final float AUDIO_WIND_NOISE_FILTER_PADDING_BOTTOM = 0.035f;
    private final float AUDIO_WIND_NOISE_FILTER_PADDING_TOP = 0.013f;
    protected final int BAR_TYPE_GAIN = 0;
    protected final int BAR_TYPE_LCF = 1;
    protected final int BAR_TYPE_LMT = 2;
    protected View mAudioBTGainBtn;
    protected AudioBalanceController mAudioBalanceController = null;
    protected View mAudioButtonLayout;
    protected int mAudioConnectionType = 1;
    protected RotateLayout mAudioControlInnerView = null;
    protected View mAudioControlView = null;
    protected View mAudioDirectivityLayout;
    protected View mAudioEarphoneGainBtn;
    protected AudioControlBar mAudioGainController = null;
    protected AudioControlStepBar mAudioLCFController = null;
    protected AudioOffStepBar mAudioLMTController = null;
    protected AudioLevelMeter mAudioLevelMeterLeft = null;
    protected AudioLevelMeter mAudioLevelMeterRight = null;
    protected AudioManagerEx mAudioManager = null;
    protected View mAudioPhoneGainBtn;
    protected View mAudioWindNoiseFiliterLayout;
    protected ServiceListener mBTServiceListener = null;
    protected BluetoothA2dp mBluetoothA2dp = null;
    protected BluetoothAdapter mBluetoothAdapter = null;
    protected ManualPanelButton mBtnAudio = null;
    protected int mCurBTMicLv = 70;
    protected int mCurDirectivity = 0;
    protected int mCurMicType = 1;
    protected String mCurWindNoiseFilterValue = "0";
    protected int mFpsType = 0;
    protected int mGainValue_EarMic = 70;
    protected int mGainValue_PhoneMic = 70;
    protected boolean mIsBTScoConnecting = false;
    protected boolean mIsBluetoothScoOn = false;
    protected boolean mIsDirectivitySupported = true;
    protected boolean mIsEnableBTLoopback = false;
    protected boolean mIsEnableEarphoneLoopback = false;
    protected boolean mIsRecreatingMediaRecorder = false;
    protected int mLcfValue_EarMic = 0;
    protected int mLcfValue_PhoneMic = 0;
    protected String mLmtValue_EarMic = "0";
    protected String mLmtValue_PhoneMic = "0";
    protected boolean mUseBTRecording = false;
    protected WaveView mWaveView = null;
    protected TextView mWindNoiseFilterBtn;

    /* renamed from: com.lge.camera.managers.ManualAudioControlManagerBase$10 */
    class C107610 implements OnClickListener {

        /* renamed from: com.lge.camera.managers.ManualAudioControlManagerBase$10$1 */
        class C10771 implements Runnable {
            C10771() {
            }

            public void run() {
                if (ManualAudioControlManagerBase.this.mAudioEarphoneGainBtn != null && !ManualAudioControlManagerBase.this.mIsBTScoConnecting && ManualAudioControlManagerBase.this.mAudioPhoneGainBtn != null && AudioUtil.isWiredHeadsetHasMicOn() && !ManualAudioControlManagerBase.this.mAudioEarphoneGainBtn.isSelected()) {
                    ManualAudioControlManagerBase.this.onChangeMicType(4);
                    TalkBackUtil.setTalkbackDescOnDoubleTap(ManualAudioControlManagerBase.this.mGet.getAppContext(), ManualAudioControlManagerBase.this.mGet.getAppContext().getString(C0088R.string.on));
                    ManualAudioControlManagerBase.this.mGet.showToast(ManualAudioControlManagerBase.this.mGet.getAppContext().getString(C0088R.string.manual_audio_earphone_mic_select_limitation), CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                }
            }
        }

        C107610() {
        }

        public void onClick(View view) {
            ManualAudioControlManagerBase.this.sendItemClickRunnable(new C10771());
        }
    }

    /* renamed from: com.lge.camera.managers.ManualAudioControlManagerBase$11 */
    class C107811 implements OnClickListener {

        /* renamed from: com.lge.camera.managers.ManualAudioControlManagerBase$11$1 */
        class C10791 implements Runnable {
            C10791() {
            }

            public void run() {
                if (ManualAudioControlManagerBase.this.mAudioPhoneGainBtn != null && !ManualAudioControlManagerBase.this.mIsBTScoConnecting && ManualAudioControlManagerBase.this.mAudioEarphoneGainBtn != null && !ManualAudioControlManagerBase.this.mAudioPhoneGainBtn.isSelected()) {
                    ManualAudioControlManagerBase.this.onChangeMicType(1);
                    TalkBackUtil.setTalkbackDescOnDoubleTap(ManualAudioControlManagerBase.this.mGet.getAppContext(), ManualAudioControlManagerBase.this.mGet.getAppContext().getString(C0088R.string.on));
                }
            }
        }

        C107811() {
        }

        public void onClick(View view) {
            ManualAudioControlManagerBase.this.sendItemClickRunnable(new C10791());
        }
    }

    /* renamed from: com.lge.camera.managers.ManualAudioControlManagerBase$12 */
    class C108012 implements OnClickListener {

        /* renamed from: com.lge.camera.managers.ManualAudioControlManagerBase$12$1 */
        class C10811 implements Runnable {
            C10811() {
            }

            public void run() {
                if (ManualAudioControlManagerBase.this.mWindNoiseFilterBtn != null) {
                    ManualAudioControlManagerBase.this.mWindNoiseFilterBtn.setSelected(true);
                    boolean isFilterOn = "0".equals(ManualAudioControlManagerBase.this.mCurWindNoiseFilterValue);
                    CamLog.m3d(CameraConstants.TAG, isFilterOn ? "[Audio] wind noise filter on!" : "[Audio] wind noise filter off!");
                    ManualAudioControlManagerBase.this.mWindNoiseFilterBtn.setBackgroundResource(isFilterOn ? C0088R.drawable.btn_wind_noise_filter : C0088R.drawable.btn_wind_noise_filter_off);
                    ManualAudioControlManagerBase.this.mWindNoiseFilterBtn.setPadding(0, RatioCalcUtil.getSizeCalculatedByPercentage(ManualAudioControlManagerBase.this.getAppContext(), false, 0.013f), 0, RatioCalcUtil.getSizeCalculatedByPercentage(ManualAudioControlManagerBase.this.getAppContext(), false, 0.035f));
                    ManualAudioControlManagerBase.this.mCurWindNoiseFilterValue = isFilterOn ? "1" : "0";
                    TalkBackUtil.setTalkbackDescOnDoubleTap(ManualAudioControlManagerBase.this.mGet.getAppContext(), ManualAudioControlManagerBase.this.mGet.getAppContext().getString(C0088R.string.wind_noise_filter_switch) + ManualAudioControlManagerBase.this.mGet.getAppContext().getString(isFilterOn ? C0088R.string.on : C0088R.string.off));
                    ManualAudioControlManagerBase.this.setAudioParameter(ParamConstants.MANUAL_AUDIO_WIND, ManualAudioControlManagerBase.this.mCurWindNoiseFilterValue);
                }
            }
        }

        C108012() {
        }

        public void onClick(View view) {
            ManualAudioControlManagerBase.this.sendItemClickRunnable(new C10811());
        }
    }

    /* renamed from: com.lge.camera.managers.ManualAudioControlManagerBase$2 */
    class C10832 implements OnTouchListener {
        C10832() {
        }

        public boolean onTouch(View view, MotionEvent event) {
            view.performClick();
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.ManualAudioControlManagerBase$3 */
    class C10843 implements Runnable {
        C10843() {
        }

        public void run() {
            ManualAudioControlManagerBase.this.showAudioControlPanel(false);
            ManualAudioControlManagerBase.this.setItemViewSelected(ManualAudioControlManagerBase.this.mBtnAudio, false, 0);
        }
    }

    /* renamed from: com.lge.camera.managers.ManualAudioControlManagerBase$4 */
    class C10854 implements OnClickListener {

        /* renamed from: com.lge.camera.managers.ManualAudioControlManagerBase$4$1 */
        class C10861 implements Runnable {
            C10861() {
            }

            public void run() {
                ManualAudioControlManagerBase manualAudioControlManagerBase = ManualAudioControlManagerBase.this;
                boolean z = (ManualAudioControlManagerBase.this.mBtnAudio == null || ManualAudioControlManagerBase.this.mBtnAudio.isSelected()) ? false : true;
                manualAudioControlManagerBase.audioBtnClicked(z);
            }
        }

        C10854() {
        }

        public void onClick(View arg0) {
            if (ManualAudioControlManagerBase.this.isItemPanelWorkable(31)) {
                ManualAudioControlManagerBase.this.sendItemClickRunnable(new C10861());
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ManualAudioControlManagerBase$5 */
    class C10875 implements OnAudioControlStepBarListener {
        C10875() {
        }

        public void onBarTouchUp(String value) {
            int lcf = Integer.valueOf(value).intValue();
            switch (ManualAudioControlManagerBase.this.mCurMicType) {
                case 4:
                    if (ManualAudioControlManagerBase.this.mLcfValue_EarMic != lcf) {
                        ManualAudioControlManagerBase.this.mLcfValue_EarMic = Integer.valueOf(value).intValue();
                        ManualAudioControlManagerBase.this.setAudioParameter(ParamConstants.MANUAL_AUDIO_LCF, ManualAudioControlManagerBase.this.mLcfValue_EarMic + "");
                        break;
                    }
                    break;
                default:
                    if (ManualAudioControlManagerBase.this.mLcfValue_PhoneMic != lcf) {
                        ManualAudioControlManagerBase.this.mLcfValue_PhoneMic = Integer.valueOf(value).intValue();
                        ManualAudioControlManagerBase.this.setAudioParameter(ParamConstants.MANUAL_AUDIO_LCF, ManualAudioControlManagerBase.this.mLcfValue_PhoneMic + "");
                        break;
                    }
                    break;
            }
            ManualAudioControlManagerBase.this.talbackEventOnBarTouchUp(1, value, lcf == 0);
        }
    }

    /* renamed from: com.lge.camera.managers.ManualAudioControlManagerBase$6 */
    class C10886 implements OnAudioControlStepBarListener {
        C10886() {
        }

        public void onBarTouchUp(String value) {
            switch (ManualAudioControlManagerBase.this.mCurMicType) {
                case 4:
                    if (!ManualAudioControlManagerBase.this.mLmtValue_EarMic.equals(value)) {
                        ManualAudioControlManagerBase.this.mLmtValue_EarMic = value;
                        ManualAudioControlManagerBase.this.setAudioParameter(ParamConstants.MANUAL_AUDIO_LMT, "off".equals(value) ? "1" : ManualAudioControlManagerBase.this.mLmtValue_EarMic + "");
                        break;
                    }
                    break;
                default:
                    if (!ManualAudioControlManagerBase.this.mLmtValue_PhoneMic.equals(value)) {
                        String str;
                        ManualAudioControlManagerBase.this.mLmtValue_PhoneMic = value;
                        ManualAudioControlManagerBase manualAudioControlManagerBase = ManualAudioControlManagerBase.this;
                        String str2 = ParamConstants.MANUAL_AUDIO_LMT;
                        if ("off".equals(value)) {
                            str = "1";
                        } else {
                            str = ManualAudioControlManagerBase.this.mLmtValue_PhoneMic + "";
                        }
                        manualAudioControlManagerBase.setAudioParameter(str2, str);
                        break;
                    }
                    break;
            }
            ManualAudioControlManagerBase.this.drawLMTLine(value);
            ManualAudioControlManagerBase.this.talbackEventOnBarTouchUp(2, value, "off".equals(value));
        }
    }

    /* renamed from: com.lge.camera.managers.ManualAudioControlManagerBase$7 */
    class C10897 implements OnAudioControlBarListener {
        C10897() {
        }

        public void onBarValueChanged(int value) {
            int gain = ManualAudioControlManagerBase.this.changeBarValueToGainParamValue(value);
            switch (ManualAudioControlManagerBase.this.mCurMicType) {
                case 1:
                    if (gain != ManualAudioControlManagerBase.this.mGainValue_PhoneMic) {
                        ManualAudioControlManagerBase.this.mGainValue_PhoneMic = gain;
                        break;
                    }
                    break;
                case 4:
                    if (gain != ManualAudioControlManagerBase.this.mGainValue_EarMic) {
                        ManualAudioControlManagerBase.this.mGainValue_EarMic = gain;
                        break;
                    }
                    break;
            }
            ManualAudioControlManagerBase.this.setAudioParameter(ParamConstants.MANUAL_AUDIO_GAIN, gain + "");
            ManualAudioControlManagerBase.this.setEnableAudioComponents();
        }

        public void onBarTouchUp(int value) {
            boolean z;
            int gain = ManualAudioControlManagerBase.this.changeBarValueToGainParamValue(value);
            ManualAudioControlManagerBase manualAudioControlManagerBase = ManualAudioControlManagerBase.this;
            String valueOf = String.valueOf(value);
            if (gain == 0) {
                z = true;
            } else {
                z = false;
            }
            manualAudioControlManagerBase.talbackEventOnBarTouchUp(0, valueOf, z);
        }
    }

    /* renamed from: com.lge.camera.managers.ManualAudioControlManagerBase$8 */
    class C10908 implements OnAudioControlBarListener {
        C10908() {
        }

        public void onBarValueChanged(int value) {
            int resultValue = ManualAudioControlManagerBase.this.changeBarValueToDirectivityValue(value);
            if (resultValue != ManualAudioControlManagerBase.this.mCurDirectivity) {
                ManualAudioControlManagerBase.this.mCurDirectivity = resultValue;
                ManualAudioControlManagerBase.this.setAudioParameter(ParamConstants.MANUAL_AUDIO_DIRECTION, ManualAudioControlManagerBase.this.mCurDirectivity + "");
                CamLog.m3d(CameraConstants.TAG, "[Audio] audio directivity value " + value);
                if (resultValue > ManualAudioControlManagerBase.this.mCurDirectivity) {
                    TalkBackUtil.setTalkbackDescOnDoubleTap(ManualAudioControlManagerBase.this.mGet.getAppContext(), ManualAudioControlManagerBase.this.mGet.getAppContext().getString(C0088R.string.directivity_rear_larger));
                } else if (resultValue == 0) {
                    TalkBackUtil.setTalkbackDescOnDoubleTap(ManualAudioControlManagerBase.this.mGet.getAppContext(), ManualAudioControlManagerBase.this.mGet.getAppContext().getString(C0088R.string.directivity_center));
                } else if (resultValue < ManualAudioControlManagerBase.this.mCurDirectivity) {
                    TalkBackUtil.setTalkbackDescOnDoubleTap(ManualAudioControlManagerBase.this.mGet.getAppContext(), ManualAudioControlManagerBase.this.mGet.getAppContext().getString(C0088R.string.directivity_front_larger));
                }
            }
        }

        public void onBarTouchUp(int value) {
        }
    }

    /* renamed from: com.lge.camera.managers.ManualAudioControlManagerBase$9 */
    class C10919 implements OnClickListener {

        /* renamed from: com.lge.camera.managers.ManualAudioControlManagerBase$9$1 */
        class C10921 implements Runnable {
            C10921() {
            }

            public void run() {
                if (ManualAudioControlManagerBase.this.mAudioBTGainBtn != null && !ManualAudioControlManagerBase.this.mAudioBTGainBtn.isSelected()) {
                    AudioUtil.setAudioFocus(ManualAudioControlManagerBase.this.mGet.getAppContext(), true, false);
                    ManualAudioControlManagerBase.this.onChangeMicType(8);
                    TalkBackUtil.setTalkbackDescOnDoubleTap(ManualAudioControlManagerBase.this.mGet.getAppContext(), ManualAudioControlManagerBase.this.mGet.getAppContext().getString(C0088R.string.on));
                }
            }
        }

        C10919() {
        }

        public void onClick(View view) {
            ManualAudioControlManagerBase.this.sendItemClickRunnable(new C10921());
        }
    }

    public ManualAudioControlManagerBase(ManualModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setDegree(int degree, boolean animation) {
        super.setDegree(degree, animation);
        RotateLayout manualControlAudio = (RotateLayout) this.mGet.findViewById(C0088R.id.manual_audio_control_layout_rotate);
        if (manualControlAudio != null) {
            manualControlAudio.rotateLayout(270);
        }
        if (this.mAudioControlInnerView == null) {
            return;
        }
        if (degree == 270) {
            this.mAudioControlInnerView.rotateLayout(0);
        } else if (degree == 90) {
            this.mAudioControlInnerView.rotateLayout(180);
        }
    }

    protected void rotatePanelButtons(int degree) {
        if (degree == 90 || degree == 270) {
            int convertDegree = (degree + 90) % 360;
            super.rotatePanelButtons(degree);
            if (this.mBtnAudio != null) {
                this.mBtnAudio.setDegree(convertDegree, false);
            }
        }
    }

    protected void rotateInfoLayout(int degree) {
        super.rotateInfoLayout(degree);
        rotateLevelMeter(degree);
        if (degree == 90 || degree == 270) {
            RotateLayout rl = (RotateLayout) this.mGet.findViewById(C0088R.id.manual_audio_level_meter_layout_rotate_inner);
            if (rl != null) {
                rl.rotateLayout(degree);
            }
        }
    }

    private void rotateLevelMeter(int degree) {
        LinearLayout ll = (LinearLayout) this.mGet.findViewById(C0088R.id.manual_audio_level_meter_layout);
        if (ll != null) {
            LayoutParams params = (LayoutParams) ll.getLayoutParams();
            params.width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.10277f);
            params.height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.1166f);
            ll.setLayoutParams(params);
            ll = (LinearLayout) this.mGet.findViewById(C0088R.id.manual_audio_level_meter_layout_rotate_core);
            if (ll != null) {
                params = (LayoutParams) ll.getLayoutParams();
                int sizeCalculatedByPercentage = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.10277f);
                params.height = sizeCalculatedByPercentage;
                params.width = sizeCalculatedByPercentage;
                ll.setLayoutParams(params);
            }
        }
    }

    protected void setManualPanelbottomMargin(boolean cinemaSize) {
        super.setManualPanelbottomMargin(cinemaSize);
        LinearLayout ll = (LinearLayout) this.mGet.findViewById(C0088R.id.manual_audio_level_meter_layout);
        if (ll != null) {
            int ratioGuideHeight = getRatioGuideHeight();
            int manualPanelheight = Utils.getPx(getAppContext(), C0088R.dimen.manual_panel_button_height);
            int margin = Utils.getPx(getAppContext(), C0088R.dimen.manual_audio_level_meter_marginBottom);
            if (!ModelProperties.isLongLCDModel() && cinemaSize && ratioGuideHeight < manualPanelheight) {
                margin -= manualPanelheight(ratioGuideHeight, manualPanelheight);
            }
            ll.setPaddingRelative(ll.getPaddingStart(), ll.getPaddingTop(), ll.getPaddingEnd(), margin);
        }
    }

    protected int manualPanelheight(int ratioGuideHeight, int manualPanelHeight) {
        return Math.abs(ratioGuideHeight - manualPanelHeight) - 10;
    }

    public void setAudioButtonEanble(boolean enable) {
        if (this.mBtnAudio != null) {
            ColorFilter cf;
            if (enable) {
                cf = ColorUtil.getNormalColorByAlpha();
            } else {
                cf = ColorUtil.getDimColorByAlpha();
            }
            this.mBtnAudio.setEnabled(enable);
            this.mBtnAudio.setColorFilter(cf);
        }
    }

    public void showAudioControlPanel(boolean show) {
        if (this.mAudioControlView != null && this.mBtnAudio != null) {
            boolean z;
            if (show) {
                if (!isAudioControlPanelShowing()) {
                    this.mAudioControlView.setVisibility(0);
                    if ((this.mAudioConnectionType & 4) != 0) {
                        this.mAudioButtonLayout.setVisibility(0);
                    }
                    adjustAudioPopupSize();
                    checkAudioSettingAndState();
                }
            } else if (isAudioControlPanelShowing()) {
                this.mAudioControlView.setVisibility(8);
            }
            ModuleInterface moduleInterface = this.mGet;
            if (show) {
                z = false;
            } else {
                z = true;
            }
            moduleInterface.setFilmStrengthButtonVisibility(z, false);
            this.mBtnAudio.setSelected(show);
        }
    }

    protected void adjustAudioPopupSize() {
        boolean isDirectivityVisible;
        if (this.mIsDirectivitySupported && this.mCurMicType == 1) {
            isDirectivityVisible = true;
        } else {
            isDirectivityVisible = false;
        }
        int width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, isDirectivityVisible ? 0.544f : 0.43f);
        int height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.715f);
        int paddingTop = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.051f);
        int paddingStart = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.0257f);
        LayoutParams audioPopupLp = new LayoutParams(width, height);
        if (this.mAudioControlView != null) {
            this.mAudioControlView.setLayoutParams(audioPopupLp);
        }
        LayoutParams audioViewLp = new LayoutParams(width - (paddingStart * 2), height - (paddingTop * 2));
        audioViewLp.addRule(13);
        audioViewLp.setMarginStart(paddingStart);
        if (this.mAudioControlInnerView != null) {
            this.mAudioControlInnerView.setLayoutParams(audioViewLp);
        }
        adjustAudioComponentsSize();
    }

    protected void adjustAudioComponentsSize() {
        if (this.mGet != null) {
            int width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.10133333f);
            int height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.419f);
            for (int id : new int[]{C0088R.id.gain_bar_layout, C0088R.id.lcf_bar_layout, C0088R.id.lmt_bar_layout}) {
                View view = this.mGet.findViewById(id);
                if (view != null) {
                    LayoutParams audioBarLp = (LayoutParams) view.getLayoutParams();
                    audioBarLp.width = width;
                    view.setLayoutParams(audioBarLp);
                }
            }
            width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.304f);
            height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.184f);
            if (this.mWaveView != null) {
                LayoutParams waveLp = (LayoutParams) this.mWaveView.getLayoutParams();
                waveLp.width = width;
                waveLp.height = height;
                this.mWaveView.setLayoutParams(waveLp);
            }
            if (this.mCurMicType != 4 && this.mIsDirectivitySupported) {
                width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.094f);
                height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.377f);
                if (this.mAudioBalanceController != null) {
                    LayoutParams lp = (LayoutParams) this.mAudioBalanceController.getLayoutParams();
                    lp.width = width;
                    lp.height = height;
                    this.mAudioBalanceController.setLayoutParams(lp);
                }
            }
            if (this.mWindNoiseFilterBtn != null) {
                this.mWindNoiseFilterBtn.setTextSize(1, (float) RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.0063f));
                this.mWindNoiseFilterBtn.setLineSpacing((float) RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.027f), 0.0f);
            }
        }
    }

    public boolean isAudioControlPanelShowing() {
        if (this.mAudioControlView == null || this.mAudioControlView.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    protected void setItemViewSelected(final View view, final boolean show, long duration) {
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (view != null) {
                    view.setSelected(show);
                }
            }
        }, duration);
    }

    protected void checkAudioSettingAndState() {
        boolean z;
        boolean z2 = true;
        View view = this.mAudioBTGainBtn;
        if (this.mCurMicType == 8) {
            z = true;
        } else {
            z = false;
        }
        setItemViewSelected(view, z, 0);
        view = this.mAudioEarphoneGainBtn;
        if (this.mCurMicType == 4) {
            z = true;
        } else {
            z = false;
        }
        setItemViewSelected(view, z, 0);
        View view2 = this.mAudioPhoneGainBtn;
        if (this.mCurMicType != 1) {
            z2 = false;
        }
        setItemViewSelected(view2, z2, 0);
        boolean isFilterOn = "1".equals(this.mCurWindNoiseFilterValue);
        this.mWindNoiseFilterBtn.setSelected(isFilterOn);
        this.mWindNoiseFilterBtn.setBackgroundResource(isFilterOn ? C0088R.drawable.btn_wind_noise_filter : C0088R.drawable.btn_wind_noise_filter_off);
        this.mWindNoiseFilterBtn.setPadding(0, RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.013f), 0, RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.035f));
    }

    protected void setAudioParameter(String key, String value) {
    }

    protected void onChangeMicType(int type) {
    }

    public void setPanelVisibility(int visibility) {
        super.setPanelVisibility(visibility);
        RotateLayout manualAudioLevelMeter = (RotateLayout) this.mGet.findViewById(C0088R.id.manual_audio_level_meter_layout_rotate);
        if (manualAudioLevelMeter != null) {
            manualAudioLevelMeter.setVisibility(visibility);
        }
    }

    protected void setExtraControlVisibility(int visibility) {
        if (this.mAudioWindNoiseFiliterLayout != null) {
            this.mAudioWindNoiseFiliterLayout.setVisibility(visibility);
        }
        if (this.mAudioDirectivityLayout == null) {
            return;
        }
        if (visibility != 0 || this.mIsDirectivitySupported) {
            this.mAudioDirectivityLayout.setVisibility(visibility);
        }
    }

    protected void setAudioPanelBackgroundTouchListener() {
        if (this.mAudioControlView != null) {
            this.mAudioControlView.setOnTouchListener(new C10832());
        }
    }

    protected boolean onManualButtonClicked(int type, ManualData value) {
        if (!super.onManualButtonClicked(type, value)) {
            return false;
        }
        sendItemClickRunnable(new C10843());
        return true;
    }

    protected void initializePanelButtons() {
        super.initializePanelButtons();
        this.mBtnAudio = (ManualPanelButton) this.mGet.findViewById(C0088R.id.manual_panel_audio);
        if (this.mBtnAudio != null) {
            setAudioBtnDrawable("on".equals(this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_AUDIO)));
            this.mBtnAudio.setVisibility(0);
            this.mBtnAudio.setContentDescription(this.mGet.getAppContext().getString(C0088R.string.manual_audio_microphone));
            this.mBtnAudio.setOnClickListener(new C10854());
        }
    }

    public void audioBtnClicked(boolean clicked) {
        if (clicked) {
            for (Integer intValue : this.mPanelButtonMap.keySet()) {
                int type = intValue.intValue();
                DrumController drum = (DrumController) this.mDrumMap.get(Integer.valueOf(type));
                if (drum != null) {
                    drum.setVisibility(8);
                }
                RotateImageButton autoButton = (RotateImageButton) this.mAutoButtonMap.get(Integer.valueOf(type));
                if (autoButton != null) {
                    autoButton.setVisibility(8);
                }
                ManualPanelButton button = (ManualPanelButton) this.mPanelButtonMap.get(Integer.valueOf(type));
                if (button != null) {
                    button.setSelected(false);
                }
                if (this.mBtnPeaking != null) {
                    this.mBtnPeaking.setVisibility(8);
                }
            }
            if (FunctionProperties.isSupportedLogProfile()) {
                relocateLogDisplayLUTButton(this.mGet.getOrientationDegree());
            }
            if (this.mGet.checkModuleValidate(128)) {
                this.mGet.setZoomUiVisibility(false, 1);
                this.mGet.setZoomUiVisibility(false, 2);
            } else {
                this.mGet.setZoomUiVisibility(false);
            }
            showAudioControlPanel(true);
            if (!this.mGet.isSlowMotionMode()) {
                return;
            }
            if ((this.mAudioConnectionType & 4) != 0) {
                this.mGet.showToast(this.mGet.getAppContext().getString(C0088R.string.manual_audio_slowmotion_limitation_with_anothor_mic), CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                return;
            } else {
                this.mGet.showToast(this.mGet.getAppContext().getString(C0088R.string.manual_audio_slowmotion_limitation_only_phone_mic), CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                return;
            }
        }
        showAudioControlPanel(false);
        this.mGet.setZoomUiVisibility(true, 2);
        setItemViewSelected(this.mBtnAudio, false, 0);
    }

    protected void setAudioButtonLayout(int buttonWidth) {
        super.setAudioButtonLayout(buttonWidth);
        if (this.mBtnAudio != null) {
            LinearLayout.LayoutParams btnLp = (LinearLayout.LayoutParams) this.mBtnAudio.getLayoutParams();
            if (btnLp != null) {
                btnLp.width = buttonWidth;
                this.mBtnAudio.setLayoutParams(btnLp);
            }
        }
    }

    protected void createAudioCotroller() {
        this.mAudioGainController = (AudioControlBar) this.mGet.findViewById(C0088R.id.phone_audio_gain_control_bar);
        if (this.mAudioGainController != null) {
            this.mAudioGainController.init(-20, 20, 0, this.mGet.getAppContext().getResources().getString(C0088R.string.gain), this.mGet.getAppContext().getResources().getString(C0088R.string.db));
            setAudioPhoneGainControlListener();
        }
        this.mAudioLCFController = (AudioControlStepBar) this.mGet.findViewById(C0088R.id.audio_lcf_control_bar);
        if (this.mAudioLCFController != null) {
            this.mAudioLCFController.init(75, 3, this.mLcfValue_PhoneMic, this.mGet.getAppContext().getResources().getString(C0088R.string.lcf), this.mGet.getAppContext().getResources().getString(C0088R.string.hz));
            setAudioLCFControlListener();
        }
        this.mAudioLMTController = (AudioOffStepBar) this.mGet.findViewById(C0088R.id.audio_lmt_control_bar);
        if (this.mAudioLMTController != null) {
            this.mAudioLMTController.init(-3, 4, this.mLmtValue_PhoneMic, this.mGet.getAppContext().getResources().getString(C0088R.string.lmt), this.mGet.getAppContext().getResources().getString(C0088R.string.db));
            setAudioLMTControlListener();
        }
        if (this.mIsDirectivitySupported) {
            this.mAudioBalanceController = (AudioBalanceController) this.mGet.findViewById(C0088R.id.audio_balance_controller);
            if (this.mAudioBalanceController != null) {
                this.mAudioBalanceController.init(-10, 10, this.mCurDirectivity);
                setAudioBalanceControlListener();
            }
        }
        if (this.mWindNoiseFilterBtn != null) {
            this.mWindNoiseFilterBtn.setSelected(true);
        }
        setEnableAudioComponents();
    }

    protected void talbackEventOnBarTouchUp(int barType, String value, boolean isMinValue) {
    }

    protected void setAudioLCFControlListener() {
        this.mAudioLCFController.setListener(new C10875());
    }

    protected void setAudioLMTControlListener() {
        this.mAudioLMTController.setListener(new C10886());
        drawLMTLine(this.mCurMicType == 4 ? this.mLmtValue_EarMic + "" : this.mLmtValue_PhoneMic + "");
    }

    protected void drawLMTLine(String dB) {
        if (this.mWaveView != null) {
            CamLog.m3d(CameraConstants.TAG, "[Audio] drawLMTLine : " + dB);
            this.mWaveView.drawLmtLine(dB);
        }
    }

    protected int changeBarValueToGainParamValue(int value) {
        if (value == -20) {
            return 0;
        }
        return value + 70;
    }

    protected int changeGainParamValueToBarValue(int value) {
        if (value == 0) {
            return -20;
        }
        return value - 70;
    }

    protected void setAudioPhoneGainControlListener() {
        this.mAudioGainController.setOnAudioControlBarListener(new C10897());
    }

    protected void setEnableAudioComponents() {
        boolean isEnabled;
        boolean is120Fps = this.mGet.isSlowMotionMode();
        if (this.mAudioGainController != null) {
            boolean z;
            AudioControlBar audioControlBar = this.mAudioGainController;
            if (is120Fps) {
                z = false;
            } else {
                z = true;
            }
            audioControlBar.setEnabled(z);
        }
        boolean isMute = false;
        switch (this.mCurMicType) {
            case 1:
                if (this.mGainValue_PhoneMic == 0) {
                    isMute = true;
                } else {
                    isMute = false;
                }
                drawLMTLine(isMute ? "off" : this.mLmtValue_PhoneMic + "");
                break;
            case 4:
                if (this.mGainValue_EarMic == 0) {
                    isMute = true;
                } else {
                    isMute = false;
                }
                drawLMTLine(isMute ? "off" : this.mLmtValue_EarMic + "");
                break;
        }
        ArrayList<View> viewList = new ArrayList(Arrays.asList(new View[]{this.mAudioDirectivityLayout, this.mAudioBalanceController, this.mAudioLCFController, this.mAudioLMTController, this.mWindNoiseFilterBtn}));
        if (isMute || is120Fps) {
            isEnabled = false;
        } else {
            isEnabled = true;
        }
        Iterator it = viewList.iterator();
        while (it.hasNext()) {
            View view = (View) it.next();
            if (view != null) {
                view.setEnabled(isEnabled);
                view.setAlpha(isEnabled ? 1.0f : 0.35f);
            }
        }
    }

    public String getLDBString() {
        String res = super.getLDBString();
        if (this.mAudioGainController == null || this.mAudioLCFController == null || this.mAudioLMTController == null) {
            return res;
        }
        String micType = LdbConstants.LDB_MANUAL_AUDIO_MIC_PHONE_VOL;
        if (this.mCurMicType == 1) {
            micType = LdbConstants.LDB_MANUAL_AUDIO_MIC_PHONE_VOL;
        } else if (this.mCurMicType == 4) {
            micType = LdbConstants.LDB_MANUAL_AUDIO_MIC_EAR_VOL;
        } else if (this.mCurMicType == 8) {
            micType = LdbConstants.LDB_MANUAL_AUDIO_MIC_BT_VOL;
        }
        res = (((res + "micType=" + micType + ";") + "decibel=" + this.mAudioGainController.getCurrentValue() + ";") + "lcf=" + this.mAudioLCFController.getCurrentValue() + ";") + "lmt=" + this.mAudioLMTController.getCurrentValue() + ";";
        if (this.mAudioBalanceController != null) {
            res = res + "directivity=" + this.mAudioBalanceController.getCurrentValue() + ";";
        }
        return res + "windNoiseFilter=" + "1".equals(this.mCurWindNoiseFilterValue) + ";";
    }

    protected void initManualAudioControl() {
        boolean z;
        int i = 0;
        String frameRate = this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE);
        if ("120".equals(frameRate)) {
            this.mFpsType = 2;
        } else if ("1".equals(frameRate) || "2".equals(frameRate)) {
            this.mFpsType = 1;
        } else {
            this.mFpsType = 0;
        }
        this.mWaveView = (WaveView) this.mGet.findViewById(C0088R.id.manual_audio_waveview);
        if (this.mFpsType != 1) {
            z = true;
        } else {
            z = false;
        }
        setAudioButtonEanble(z);
        if (this.mCurMicType != 1) {
            i = 8;
        }
        setExtraControlVisibility(i);
    }

    protected void setupViews() {
        super.setupViews();
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.manual_base);
        RotateLayout audioPopupLayout = (RotateLayout) vg.findViewById(C0088R.id.manual_audio_control_layout_rotate);
        if (audioPopupLayout != null) {
            ((FrameLayout.LayoutParams) audioPopupLayout.getLayoutParams()).setMarginEnd(RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.11527f));
            audioPopupLayout.addView(this.mGet.inflateView(C0088R.layout.manual_audio_control));
            audioPopupLayout.setVisibility(0);
        }
        RotateLayout audioLevelMeterLayout = (RotateLayout) vg.findViewById(C0088R.id.manual_audio_level_meter_layout_rotate);
        if (audioLevelMeterLayout != null) {
            audioLevelMeterLayout.addView(this.mGet.inflateView(C0088R.layout.manual_audio_level_meter));
            audioLevelMeterLayout.setVisibility(4);
        }
        RotateLayout manual_audio = (RotateLayout) vg.findViewById(C0088R.id.manual_audio_level_meter_layout_rotate);
        if (manual_audio != null) {
            FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) manual_audio.getLayoutParams();
            param.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, ModelProperties.getLCDType() == 2 ? 0.07639f : 0.1055f) + RatioCalcUtil.getQuickButtonWidth(getAppContext());
            param.leftMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.03333f);
        }
        this.mAudioControlView = this.mGet.findViewById(C0088R.id.manual_audio_control_layout);
        this.mAudioControlInnerView = (RotateLayout) this.mGet.findViewById(C0088R.id.phone_audio_control_layout);
        this.mWaveView = (WaveView) this.mGet.findViewById(C0088R.id.manual_audio_waveview);
        this.mAudioButtonLayout = this.mGet.findViewById(C0088R.id.mic_button_layout);
        this.mAudioBTGainBtn = this.mGet.findViewById(C0088R.id.bt_audio_gain_button);
        this.mAudioEarphoneGainBtn = this.mGet.findViewById(C0088R.id.earphone_audio_gain_button);
        this.mAudioPhoneGainBtn = this.mGet.findViewById(C0088R.id.phone_audio_gain_button);
        if (this.mIsDirectivitySupported) {
            this.mAudioDirectivityLayout = this.mGet.findViewById(C0088R.id.audio_balance_control_layout);
            this.mAudioDirectivityLayout.setVisibility(0);
        }
        this.mAudioWindNoiseFiliterLayout = this.mGet.findViewById(C0088R.id.wind_noise_filter_checkbox_layout);
        this.mWindNoiseFilterBtn = (TextView) this.mGet.findViewById(C0088R.id.wind_noise_filter_text_button);
        this.mAudioLevelMeterLeft = (AudioLevelMeter) this.mGet.findViewById(C0088R.id.manual_audio_level_meter_left);
        if (this.mAudioLevelMeterLeft != null) {
            this.mAudioLevelMeterLeft.init();
        }
        this.mAudioLevelMeterRight = (AudioLevelMeter) this.mGet.findViewById(C0088R.id.manual_audio_level_meter_right);
        if (this.mAudioLevelMeterRight != null) {
            this.mAudioLevelMeterRight.init();
        }
        setDegree(270, false);
    }

    protected int changeBarValueToDirectivityValue(int srcValue) {
        return srcValue * 12;
    }

    protected int changeDirectivityValueToBarValue(int value) {
        return value / 12;
    }

    private void setAudioBalanceControlListener() {
        this.mAudioBalanceController.setOnAudioControlBarListener(new C10908());
    }

    protected void setBTGainBtnListener() {
        if (this.mAudioBTGainBtn != null) {
            this.mAudioBTGainBtn.setOnClickListener(new C10919());
        }
    }

    protected void setEarphoneGainBtnListener() {
        if (this.mAudioEarphoneGainBtn != null) {
            this.mAudioEarphoneGainBtn.setOnClickListener(new C107610());
        }
    }

    protected void setPhonGainBtnListener() {
        if (this.mAudioPhoneGainBtn != null) {
            this.mAudioPhoneGainBtn.setOnClickListener(new C107811());
        }
    }

    protected void setWindNoiseFilterBtnListener() {
        if (this.mWindNoiseFilterBtn != null) {
            this.mWindNoiseFilterBtn.setOnClickListener(new C108012());
        }
    }

    public void drawWave() {
        int amplitude;
        int soundType = (this.mCurMicType == 4 || this.mCurMicType == 8) ? 1 : 2;
        if (soundType == 1) {
            amplitude = VideoRecorder.getMaxAudioAmplitude();
        } else {
            int[] levelMeter = VideoRecorder.getLevelMeter();
            amplitude = (levelMeter[0] + levelMeter[1]) / 2;
        }
        if (this.mWaveView != null) {
            this.mWaveView.startDrawingWave();
            this.mWaveView.drawWave(amplitude, soundType);
        }
    }

    public void stopDrawingWave() {
        if (this.mWaveView != null) {
            this.mWaveView.stopDrawingWave();
        }
    }

    public void setAudioBtnDrawable(boolean isHifiOn) {
        if (this.mBtnAudio != null) {
            this.mBtnAudio.setImageResource(isHifiOn ? C0088R.drawable.btn_manual_panel_audio_hifi : C0088R.drawable.btn_manual_panel_audio);
        }
    }

    public boolean isAudioEarphoneGainBtnSelected() {
        if (this.mAudioEarphoneGainBtn == null) {
            return false;
        }
        return this.mAudioEarphoneGainBtn.isSelected();
    }
}
