package com.lge.camera.managers;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManagerEx;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.app.VideoRecorder;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.IntentBroadcastUtil;
import com.lge.camera.util.TalkBackUtil;
import com.lge.camera.util.TelephonyUtil;
import java.lang.ref.WeakReference;

public class ManualAudioControlManager extends ManualAudioControlManagerBase {
    static final int MEDIASERVER_LOCKING_TIME_MILLISECOND = 800;
    protected final ManualModeHandler mManualModeUpdateHandler = new ManualModeHandler(this);

    /* renamed from: com.lge.camera.managers.ManualAudioControlManager$1 */
    class C10501 implements ServiceListener {
        C10501() {
        }

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            CamLog.m3d(CameraConstants.TAG, "[audio] BTServiceListener - Connected");
            if (profile == 2) {
                ManualAudioControlManager.this.mBluetoothA2dp = (BluetoothA2dp) proxy;
                if (ManualAudioControlManager.this.mGet.isPaused()) {
                    ManualAudioControlManager.this.mGet.postOnUiThread(new HandlerRunnable(ManualAudioControlManager.this) {
                        public void handleRun() {
                            ManualAudioControlManager.this.releaseBTServiceListener();
                        }
                    }, 0);
                }
            }
        }

        public void onServiceDisconnected(int profile) {
            CamLog.m3d(CameraConstants.TAG, "[audio] BTServiceListener - Disconnected");
        }
    }

    /* renamed from: com.lge.camera.managers.ManualAudioControlManager$2 */
    class C10512 extends BroadcastReceiver {
        C10512() {
        }

        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("android.media.extra.SCO_AUDIO_STATE", -1);
            CamLog.m3d(CameraConstants.TAG, "[Audio] Audio SCO state: " + state);
            if (1 == state) {
                CamLog.m3d(CameraConstants.TAG, "[Audio] Blutooth mic connected");
                ManualAudioControlManager.this.mIsBluetoothScoOn = true;
                ManualAudioControlManager.this.mIsBTScoConnecting = false;
                ManualAudioControlManager.this.setItemViewSelected(ManualAudioControlManager.this.mAudioBTGainBtn, true, 0);
                ManualAudioControlManager.this.mGet.getActivity().unregisterReceiver(this);
            }
        }
    }

    class HeadsetStateChangeRunnable extends HandlerRunnable {
        boolean mIsConnected;

        public HeadsetStateChangeRunnable(OnRemoveHandler removeFunc, boolean isConnected) {
            super(removeFunc);
            this.mIsConnected = isConnected;
        }

        public void handleRun() {
            CamLog.m3d(CameraConstants.TAG, "[Audio] onHeadsetStateChanged : " + this.mIsConnected);
            if (this.mIsConnected) {
                ManualAudioControlManager.this.doHeadsetConnectAction();
                return;
            }
            ManualAudioControlManager manualAudioControlManager = ManualAudioControlManager.this;
            manualAudioControlManager.mAudioConnectionType &= -7;
            if ((ManualAudioControlManager.this.mAudioConnectionType & 16) == 0 || !ManualAudioControlManager.this.isBTA2dpConnected()) {
                VideoRecorder.setLoopback(2);
            }
            ManualAudioControlManager.this.mIsEnableEarphoneLoopback = false;
            ManualAudioControlManager.this.onChangeMicType(ManualAudioControlManager.this.mCurMicType == 8 ? 8 : 1);
            if (ManualAudioControlManager.this.mAudioButtonLayout != null) {
                ManualAudioControlManager.this.mAudioButtonLayout.setVisibility(8);
            }
        }
    }

    static class ManualModeHandler extends Handler {
        private final WeakReference<ManualAudioControlManager> mManagerRef;

        ManualModeHandler(ManualAudioControlManager manager) {
            this.mManagerRef = new WeakReference(manager);
        }

        public void handleMessage(Message msg) {
            if (this.mManagerRef != null) {
                ManualAudioControlManager manager = (ManualAudioControlManager) this.mManagerRef.get();
                if (manager != null) {
                    manager.manualModeHandleMessage(msg);
                }
            }
        }
    }

    public ManualAudioControlManager(ManualModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    protected void manualModeHandleMessage(Message msg) {
        switch (msg.what) {
            case 8:
                updateAudioLevelMeter();
                return;
            case 9:
                setBluetoothLoopback();
                return;
            case 10:
                if (!this.mIsEnableBTLoopback && !this.mIsEnableEarphoneLoopback) {
                    startLoopbackStart();
                    this.mIsEnableBTLoopback = true;
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void updateAudioLevelMeter() {
        if (this.mAudioLevelMeterLeft != null && this.mAudioLevelMeterRight != null && !this.mIsRecreatingMediaRecorder) {
            int[] levelMeter = VideoRecorder.getLevelMeter();
            int leftLevel = levelMeter[0];
            int rightLevel = levelMeter[1];
            if (this.mCurMicType != 1) {
                rightLevel = leftLevel;
            }
            if (this.mFpsType == 1) {
                leftLevel = 0;
                rightLevel = 0;
            }
            this.mAudioLevelMeterLeft.updateAudioLevel(leftLevel);
            this.mAudioLevelMeterRight.updateAudioLevel(rightLevel);
            this.mManualModeUpdateHandler.sendEmptyMessageDelayed(8, 200);
        }
    }

    private void setBluetoothLoopback() {
        if (isBTA2dpConnected()) {
            this.mIsEnableBTLoopback = true;
            startLoopbackStart();
        }
        if (this.mCurMicType == 1) {
            setAudioParameter(ParamConstants.MANUAL_AUDIO_MIC_TYPE, "2");
            VideoRecorder.setLoopback(4);
            setAudioExtraParameters(true, true);
        } else if (this.mCurMicType == 4) {
            setAudioParameter(ParamConstants.MANUAL_AUDIO_MIC_TYPE, "1");
            VideoRecorder.setLoopback(5);
            setAudioExtraParameters(true, false);
        }
        if (this.mUseBTRecording && isAudioControlPanelShowing()) {
            this.mAudioBTGainBtn.setVisibility(0);
        }
    }

    private void setBTServiceListener() {
        if (this.mBTServiceListener == null && this.mBluetoothAdapter != null && this.mBluetoothAdapter.isEnabled()) {
            this.mBTServiceListener = new C10501();
            this.mBluetoothAdapter.getProfileProxy(getAppContext(), this.mBTServiceListener, 2);
        }
    }

    private void releaseBTServiceListener() {
        if (this.mBluetoothAdapter != null) {
            if (this.mBluetoothA2dp != null) {
                this.mBluetoothAdapter.closeProfileProxy(2, this.mBluetoothA2dp);
                this.mBluetoothA2dp = null;
            }
            this.mBTServiceListener = null;
        }
    }

    protected void doBTStateChangedToDisconnect() {
        int i = 4;
        if (this.mBluetoothAdapter != null) {
            if (this.mBluetoothA2dp == null || this.mBluetoothA2dp.getConnectedDevices().size() <= 0) {
                this.mAudioConnectionType &= -25;
                if ((this.mAudioConnectionType & 2) == 0 && (this.mAudioConnectionType & 4) == 0) {
                    VideoRecorder.setLoopback(2);
                }
                this.mIsEnableBTLoopback = false;
                if (this.mCurMicType != 4) {
                    i = 1;
                }
                onChangeMicType(i);
                if (isAudioControlPanelShowing()) {
                    this.mAudioBTGainBtn.setVisibility(8);
                    return;
                }
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "[audio] BT disconnect - another devices is connected");
        }
    }

    protected void startLoopbackStart() {
        if (this.mFpsType != 1) {
            VideoRecorder.setLoopback(1);
        } else {
            VideoRecorder.setLoopback(2);
        }
    }

    protected void setAudioParameter(String key, String value) {
        if (this.mAudioManager != null) {
            this.mAudioManager.setParameters(key + "=" + value);
            CamLog.m3d(CameraConstants.TAG, "[Audio] setAudioParameter, key : " + key + ", value : " + value);
        }
    }

    protected void onChangeMicType(int type) {
        CamLog.m3d(CameraConstants.TAG, "[Audio] MIC type : " + type);
        setAudioMicType(type);
        switch (type) {
            case 1:
                if (this.mCurMicType == 8) {
                    setBluetoothSco(false);
                }
                if (this.mIsEnableEarphoneLoopback || this.mIsEnableBTLoopback) {
                    startLoopbackStart();
                    break;
                }
            case 4:
                VideoRecorder.setLoopback(2);
                if (this.mCurMicType == 8) {
                    setBluetoothSco(false);
                    break;
                }
                break;
            case 8:
                if (this.mCurMicType != 8) {
                    setBluetoothSco(true);
                }
                if (this.mCurMicType == 4) {
                    startLoopbackStart();
                    break;
                }
                break;
        }
        afterMicChanged(type);
    }

    private void afterMicChanged(int type) {
        boolean isPhoneMic;
        int i;
        boolean z;
        boolean z2 = true;
        this.mCurMicType = type;
        updateAudioBar();
        if (type == 1) {
            isPhoneMic = true;
        } else {
            isPhoneMic = false;
        }
        if (isPhoneMic) {
            i = 0;
        } else {
            i = 8;
        }
        setExtraControlVisibility(i);
        if (isPhoneMic) {
            z = true;
        } else {
            z = false;
        }
        setAudioExtraParameters(true, z);
        adjustAudioPopupSize();
        setEnableAudioComponents();
        if (this.mAudioBTGainBtn != null && this.mAudioEarphoneGainBtn != null && this.mAudioPhoneGainBtn != null) {
            if (!this.mIsBTScoConnecting) {
                View view = this.mAudioBTGainBtn;
                if (this.mCurMicType == 8) {
                    z = true;
                } else {
                    z = false;
                }
                view.setSelected(z);
            }
            View view2 = this.mAudioEarphoneGainBtn;
            if (this.mCurMicType == 4) {
                z = true;
            } else {
                z = false;
            }
            view2.setSelected(z);
            View view3 = this.mAudioPhoneGainBtn;
            if (this.mCurMicType != 1) {
                z2 = false;
            }
            view3.setSelected(z2);
        }
    }

    private void updateAudioBar() {
        switch (this.mCurMicType) {
            case 1:
                if (this.mAudioGainController != null) {
                    this.mAudioGainController.updateCursorPositon(changeGainParamValueToBarValue(this.mGainValue_PhoneMic));
                }
                if (this.mAudioLCFController != null) {
                    this.mAudioLCFController.updateCursorPositon(this.mLcfValue_PhoneMic);
                }
                if (this.mAudioLMTController != null) {
                    this.mAudioLMTController.updateCursorPositon(this.mLmtValue_PhoneMic);
                }
                if (this.mAudioBalanceController != null) {
                    this.mAudioBalanceController.updateCursorPositon(changeDirectivityValueToBarValue(this.mCurDirectivity));
                    return;
                }
                return;
            case 4:
                if (this.mAudioGainController != null) {
                    this.mAudioGainController.updateCursorPositon(changeGainParamValueToBarValue(this.mGainValue_EarMic));
                }
                if (this.mAudioLCFController != null) {
                    this.mAudioLCFController.updateCursorPositon(this.mLcfValue_EarMic);
                }
                if (this.mAudioLMTController != null) {
                    this.mAudioLMTController.updateCursorPositon(this.mLmtValue_EarMic);
                    return;
                }
                return;
            default:
                return;
        }
    }

    protected void setBluetoothSco(boolean set) {
        if (this.mAudioManager != null) {
            if (set) {
                this.mGet.getActivity().registerReceiver(new C10512(), new IntentFilter("android.media.ACTION_SCO_AUDIO_STATE_UPDATED"));
                this.mIsBTScoConnecting = true;
                CamLog.m3d(CameraConstants.TAG, "[Audio] Start Blutooth Sco!!");
                this.mAudioManager.startBluetoothSco();
            } else if (this.mIsBluetoothScoOn) {
                CamLog.m3d(CameraConstants.TAG, "[Audio] Stop Blutooth Sco!!");
                this.mAudioManager.stopBluetoothSco();
                this.mIsBluetoothScoOn = false;
            }
        }
    }

    public void setSSRSetting(boolean enable) {
        setAudioParameter(ParamConstants.MANUAL_AUDIO_USING_MIC, enable + "");
        if (enable || this.mFpsType == 2) {
            setAudioMicType(this.mCurMicType);
        }
    }

    public void setAudioLoopbackOnPreview(boolean enable) {
        CamLog.m3d(CameraConstants.TAG, "[Audio] setAudioLoopbackOnPreview enable = " + enable);
        if (!enable) {
            if (this.mManualModeUpdateHandler != null) {
                this.mManualModeUpdateHandler.removeMessages(8);
            }
            this.mIsRecreatingMediaRecorder = true;
            setSSRSetting(false);
            VideoRecorder.setLoopback(3);
        } else if (this.mGet.checkModuleValidate(192)) {
            setSSRSetting(true);
            VideoRecorder.createMediaRecorderEx();
            VideoRecorder.setAudioSetting(null, "on".equals(this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_AUDIO)));
            VideoRecorder.setLoopback(0);
            if (this.mCurMicType == 1) {
                VideoRecorder.setLoopback(4);
            } else {
                VideoRecorder.setLoopback(5);
            }
            if (!this.mIsEnableEarphoneLoopback && !this.mIsEnableBTLoopback) {
                VideoRecorder.setLoopback(2);
            } else if (this.mCurMicType != 4) {
                startLoopbackStart();
            }
            setAudioExtraParameters(true, true);
            this.mIsRecreatingMediaRecorder = false;
            if (this.mManualModeUpdateHandler != null) {
                this.mManualModeUpdateHandler.sendEmptyMessage(8);
            }
        } else {
            CamLog.m3d(CameraConstants.TAG, "[Audio] can not set preview audio param during recoridng started ");
        }
    }

    public void setAudioLoopbackInRecording(boolean enable) {
        CamLog.m3d(CameraConstants.TAG, "[Audio] setAudioLoopbackInRecording enable = " + enable);
        if (enable) {
            setAudioExtraParameters(true, true);
            VideoRecorder.setLoopback(this.mCurMicType == 1 ? 4 : 5);
            if (!this.mIsEnableEarphoneLoopback && !this.mIsEnableBTLoopback) {
                VideoRecorder.setLoopback(2);
            } else if (this.mCurMicType != 4) {
                startLoopbackStart();
            }
            this.mIsRecreatingMediaRecorder = false;
            if (this.mManualModeUpdateHandler != null) {
                this.mManualModeUpdateHandler.sendEmptyMessage(8);
                return;
            }
            return;
        }
        if (this.mManualModeUpdateHandler != null) {
            this.mManualModeUpdateHandler.removeMessages(8);
        }
        this.mIsRecreatingMediaRecorder = true;
        setSSRSetting(false);
        VideoRecorder.setLoopback(2);
    }

    private void setAudioMicType(int type) {
        switch (type) {
            case 1:
                setAudioParameter(ParamConstants.MANUAL_AUDIO_MIC_TYPE, "2");
                VideoRecorder.setLoopback(4);
                return;
            case 4:
                setAudioParameter(ParamConstants.MANUAL_AUDIO_MIC_TYPE, "1");
                VideoRecorder.setLoopback(5);
                return;
            case 8:
                setAudioParameter(ParamConstants.MANUAL_AUDIO_MIC_TYPE, "3");
                VideoRecorder.setLoopback(5);
                return;
            default:
                return;
        }
    }

    protected void setAudioExtraParameters(boolean setMicParam, boolean setExtraParam) {
        if (this.mFpsType != 2) {
            if (setMicParam) {
                switch (this.mCurMicType) {
                    case 1:
                        setAudioParameter(ParamConstants.MANUAL_AUDIO_GAIN, this.mGainValue_PhoneMic + "");
                        setAudioParameter(ParamConstants.MANUAL_AUDIO_LCF, this.mLcfValue_PhoneMic + "");
                        setAudioParameter(ParamConstants.MANUAL_AUDIO_LMT, "off".equals(this.mLmtValue_PhoneMic) ? "1" : this.mLmtValue_PhoneMic + "");
                        break;
                    case 4:
                        setAudioParameter(ParamConstants.MANUAL_AUDIO_GAIN, this.mGainValue_EarMic + "");
                        setAudioParameter(ParamConstants.MANUAL_AUDIO_LCF, this.mLcfValue_EarMic + "");
                        setAudioParameter(ParamConstants.MANUAL_AUDIO_LMT, "off".equals(this.mLmtValue_EarMic) ? "1" : this.mLmtValue_EarMic + "");
                        break;
                }
            }
            if (setExtraParam) {
                setAudioParameter(ParamConstants.MANUAL_AUDIO_WIND, this.mCurWindNoiseFilterValue);
                if (this.mIsDirectivitySupported) {
                    setAudioParameter(ParamConstants.MANUAL_AUDIO_DIRECTION, this.mCurDirectivity + "");
                }
            }
        }
    }

    public boolean isLoopbackAvailable() {
        return this.mIsEnableEarphoneLoopback || this.mIsEnableBTLoopback;
    }

    protected void initializeAudioParameters() {
        this.mGainValue_PhoneMic = 70;
        this.mGainValue_EarMic = 70;
        this.mCurBTMicLv = 70;
        this.mLcfValue_PhoneMic = 0;
        this.mLcfValue_EarMic = 0;
        this.mLmtValue_PhoneMic = "0";
        this.mLmtValue_EarMic = "0";
        this.mCurDirectivity = 0;
        this.mCurWindNoiseFilterValue = "0";
    }

    protected boolean isBTA2dpConnected() {
        boolean isBluetoothConnected = false;
        if (this.mAudioManager == null) {
            return false;
        }
        if ((this.mAudioManager.getDevicesForStream(3) & 896) != 0) {
            isBluetoothConnected = true;
        }
        CamLog.m3d(CameraConstants.TAG, "[Audio] isBTA2dpConnected : " + isBluetoothConnected);
        return isBluetoothConnected;
    }

    protected void checkAudioDeviceConnectionType() {
        this.mAudioConnectionType = 1;
        if (AudioUtil.isWiredHeadsetWithoutMicOn()) {
            this.mAudioConnectionType |= 2;
            this.mIsEnableEarphoneLoopback = true;
            this.mCurMicType = 1;
        } else if (AudioUtil.isWiredHeadsetHasMicOn()) {
            this.mAudioConnectionType |= 4;
            this.mIsEnableEarphoneLoopback = true;
        } else {
            this.mCurMicType = 1;
        }
        if (this.mBluetoothAdapter.getProfileConnectionState(2) == 2) {
            this.mAudioConnectionType |= 24;
            if (isBTA2dpConnected()) {
                this.mIsEnableBTLoopback = true;
            }
        }
        CamLog.m3d(CameraConstants.TAG, "[Audio] mAudioConnectionType = " + this.mAudioConnectionType + ", mCurMicType = " + this.mCurMicType);
    }

    protected void initManualAudioControl() {
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        super.initManualAudioControl();
        setBTServiceListener();
        this.mAudioManager = (AudioManagerEx) this.mGet.getAppContext().getSystemService("audio");
        if (!this.mInit) {
            initializeAudioParameters();
        }
        checkAudioDeviceConnectionType();
        if (this.mCurMicType == 8) {
            setBluetoothSco(true);
        }
        setAudioLoopbackOnPreview(true);
    }

    public void onResumeBefore() {
        boolean z = true;
        IntentBroadcastUtil.stopVoiceRec(this.mGet.getActivity(), true);
        super.onResumeBefore();
        if (AudioUtil.getNumOfMic(getAppContext()) < 3) {
            z = false;
        }
        this.mIsDirectivitySupported = z;
        if (!(this.mIsDirectivitySupported || this.mAudioDirectivityLayout == null)) {
            this.mAudioDirectivityLayout.setVisibility(8);
        }
        initManualAudioControl();
        createAudioCotroller();
        setAudioButtonListeners();
        adjustAudioPopupSize();
    }

    protected void setAudioButtonListeners() {
        setEarphoneGainBtnListener();
        setPhonGainBtnListener();
        setAudioPanelBackgroundTouchListener();
        setWindNoiseFilterBtnListener();
    }

    public void onPauseBefore() {
        showAudioControlPanel(false);
        super.onPauseBefore();
        if (this.mManualModeUpdateHandler != null) {
            this.mManualModeUpdateHandler.removeMessages(8);
        }
        setSSRSetting(false);
        VideoRecorder.setLoopback(2);
        VideoRecorder.release(null);
        this.mIsEnableEarphoneLoopback = false;
        this.mIsEnableBTLoopback = false;
        this.mIsRecreatingMediaRecorder = false;
        setBluetoothSco(false);
        this.mAudioManager = null;
        releaseBTServiceListener();
        this.mCurMicType = 1;
        initializeAudioParameters();
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mAudioLevelMeterLeft != null) {
            this.mAudioLevelMeterLeft.unbind();
            this.mAudioLevelMeterLeft = null;
        }
        if (this.mAudioLevelMeterRight != null) {
            this.mAudioLevelMeterRight.unbind();
            this.mAudioLevelMeterRight = null;
        }
        this.mBtnAudio = null;
        if (this.mAudioGainController != null) {
            this.mAudioGainController.unbind();
            this.mAudioGainController = null;
        }
        if (this.mAudioLCFController != null) {
            this.mAudioLCFController.unbind();
            this.mAudioLCFController = null;
        }
        if (this.mAudioLMTController != null) {
            this.mAudioLMTController.unbind();
            this.mAudioLMTController = null;
        }
        this.mAudioButtonLayout = null;
        this.mWaveView = null;
        this.mAudioBTGainBtn = null;
        this.mAudioEarphoneGainBtn = null;
        this.mAudioPhoneGainBtn = null;
        this.mWindNoiseFilterBtn = null;
        this.mAudioControlInnerView = null;
        this.mAudioControlView = null;
        this.mAudioDirectivityLayout = null;
        this.mAudioWindNoiseFiliterLayout = null;
        this.mAudioLevelMeterLeft = null;
        this.mAudioLevelMeterRight = null;
        this.mBluetoothAdapter = null;
    }

    public void onFrameRateChanged(String previousFps, String nextFPS) {
        boolean z = true;
        if (previousFps != null && nextFPS != null && !"not found".equals(nextFPS)) {
            if ("120".equals(nextFPS)) {
                this.mFpsType = 2;
                initializeAudioParameters();
            } else if ("1".equals(nextFPS) || "2".equals(nextFPS)) {
                this.mFpsType = 1;
            } else {
                this.mFpsType = 0;
                if (this.mAudioManager != null) {
                    if ("true".equalsIgnoreCase(this.mAudioManager.getParameters(ParamConstants.MANUAL_AUDIO_USING_MIC))) {
                        return;
                    }
                }
            }
            VideoRecorder.setLoopback(3);
            VideoRecorder.release(null);
            setAudioLoopbackOnPreview(true);
            updateAudioBar();
            setEnableAudioComponents();
            if (this.mFpsType == 1) {
                z = false;
            }
            setAudioButtonEanble(z);
        }
    }

    public void onHeadsetStateChanged(boolean isConnected) {
        CamLog.m3d(CameraConstants.TAG, "camera state" + this.mGet.getCameraState());
        this.mGet.postOnUiThread(new HeadsetStateChangeRunnable(this, isConnected), this.mGet.getCameraState() == 5 ? 800 : 0);
    }

    protected void doHeadsetConnectAction() {
        boolean isShowMicBtn = true;
        if (AudioUtil.isWiredHeadsetWithoutMicOn()) {
            this.mAudioConnectionType |= 2;
            isShowMicBtn = false;
        } else {
            this.mAudioConnectionType |= 4;
        }
        startLoopbackStart();
        this.mIsEnableEarphoneLoopback = true;
        if (this.mCurMicType == 1) {
            setAudioParameter(ParamConstants.MANUAL_AUDIO_MIC_TYPE, "2");
            VideoRecorder.setLoopback(4);
            setAudioExtraParameters(true, true);
        } else if (this.mCurMicType == 8) {
            setAudioParameter(ParamConstants.MANUAL_AUDIO_MIC_TYPE, "3");
            VideoRecorder.setLoopback(5);
            setAudioExtraParameters(true, false);
        }
        if (isAudioControlPanelShowing() && isShowMicBtn) {
            this.mAudioButtonLayout.setVisibility(0);
        }
    }

    public void onBTConnectionStateChanged(boolean isConnected) {
        CamLog.m3d(CameraConstants.TAG, "[Audio] onBTConnectionStateChanged : " + isConnected);
        if (isConnected) {
            this.mAudioConnectionType |= 24;
            this.mManualModeUpdateHandler.sendEmptyMessageDelayed(9, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
            return;
        }
        doBTStateChangedToDisconnect();
    }

    public void onBTStateChanged(boolean isOn) {
        CamLog.m3d(CameraConstants.TAG, "[Audio] onBTStateChanged : " + isOn);
        if (isOn) {
            setBTServiceListener();
        } else {
            releaseBTServiceListener();
        }
    }

    public void onBTAudioStateChanged(boolean isConnected) {
        if (this.mUseBTRecording) {
            CamLog.m3d(CameraConstants.TAG, "[Audio] onBTAudioStateChanged : " + isConnected);
            if (!isConnected && TelephonyUtil.phoneInCall(this.mGet.getAppContext())) {
                CamLog.m3d(CameraConstants.TAG, "[Audio] Change to phone mic becuase of call state");
                onChangeMicType(1);
            }
        }
    }

    public void onDualConnectionDeviceTypeChanged(int selectedBtn) {
        CamLog.m3d(CameraConstants.TAG, "[Audio] onDualConnectionDeviceTypeChanged, selectedBtn : " + selectedBtn);
        if (selectedBtn != 0) {
            this.mManualModeUpdateHandler.removeMessages(10);
            this.mManualModeUpdateHandler.sendEmptyMessageDelayed(10, 500);
        } else if (!this.mIsEnableEarphoneLoopback) {
            VideoRecorder.setLoopback(2);
            this.mIsEnableBTLoopback = false;
        }
    }

    public void setAudioParamOnRecordingStart() {
        CamLog.m3d(CameraConstants.TAG, "[Audio] setAudioParamOnRecordingStart");
        setSSRSetting(true);
        switch (this.mCurMicType) {
            case 1:
                setAudioParameter(ParamConstants.MANUAL_AUDIO_MIC_TYPE, "2");
                return;
            case 4:
                setAudioParameter(ParamConstants.MANUAL_AUDIO_MIC_TYPE, "1");
                return;
            case 8:
                setAudioParameter(ParamConstants.MANUAL_AUDIO_MIC_TYPE, "3");
                return;
            default:
                return;
        }
    }

    protected void talbackEventOnBarTouchUp(int barType, String value, boolean isMinValue) {
        if (this.mGet != null) {
            String talkback = "";
            switch (barType) {
                case 0:
                    if (!isMinValue) {
                        talkback = this.mGet.getAppContext().getString(C0088R.string.gain_level) + value + this.mGet.getAppContext().getString(C0088R.string.decibel);
                        break;
                    } else {
                        talkback = this.mGet.getAppContext().getString(C0088R.string.ids_setting_video_voice_mute);
                        break;
                    }
                case 1:
                    talkback = this.mGet.getAppContext().getString(C0088R.string.lcf_level);
                    if (!isMinValue) {
                        talkback = talkback + value + this.mGet.getAppContext().getString(C0088R.string.hertz);
                        break;
                    } else {
                        talkback = talkback + this.mGet.getAppContext().getString(C0088R.string.off);
                        break;
                    }
                case 2:
                    talkback = this.mGet.getAppContext().getString(C0088R.string.lmt_level);
                    if (!"off".equals(value)) {
                        talkback = talkback + value + this.mGet.getAppContext().getString(C0088R.string.decibel);
                        break;
                    } else {
                        talkback = talkback + this.mGet.getAppContext().getString(C0088R.string.off);
                        break;
                    }
            }
            TalkBackUtil.setTalkbackDescOnDoubleTap(this.mGet.getAppContext(), talkback);
        }
    }
}
