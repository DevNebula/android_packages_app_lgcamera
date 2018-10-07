package com.lge.camera.managers;

import android.os.SystemClock;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.MDMUtil;
import com.lge.camera.util.TelephonyUtil;
import com.lge.voiceshutter.library.AudioRecogEngine;
import com.lge.voiceshutter.library.AudioRecogEngine.Callback;

public class CheeseShutterManager extends ManagerInterfaceImpl {
    private static final int CHECK_CALL_LOOP_DELAY = 100;
    private static final int GET_AUDIO_DELAY = 500;
    private static final int RESTART_ENGINE_DURATION = 500;
    private static final int TAKE_PICTURE_DELAY = 400;
    private static int sCheckCount = 30;
    private AudioRecogEngine mAudioRecogEngine = null;
    public HandlerRunnable mStartRecogEngineRunnable = null;
    private OnVoiceRecogListener mVoiceListener = null;

    public interface OnVoiceRecogListener {
        void voiceRecogSuccess(int i);
    }

    private class AudioRecogEngineCallback implements Callback {
        private AudioRecogEngineCallback() {
        }

        /* synthetic */ AudioRecogEngineCallback(CheeseShutterManager x0, C08531 x1) {
            this();
        }

        public void onAudioEngineStartCallback(int mode) {
            CamLog.m3d(CameraConstants.TAG, "onAudioEngineStartCallback(), " + mode);
            switch (mode) {
            }
        }

        public void onAudioEngineStopCallback(int mode) {
            CamLog.m3d(CameraConstants.TAG, "onAudioEngineStopCallback(), " + mode);
            switch (mode) {
            }
        }

        public void onAudioRecogErrorCallback(int error_type) {
            CamLog.m3d(CameraConstants.TAG, "onAudioRecogStateCallback(), ERROR:" + error_type);
            switch (error_type) {
                case 1:
                    SystemClock.sleep(12);
                    CamLog.m3d(CameraConstants.TAG, "restartEngine");
                    CheeseShutterManager.this.audioCallbackRestartEngine();
                    return;
                default:
                    return;
            }
        }

        public void onAudioRecogResultCallback(int type) {
            CamLog.m3d(CameraConstants.TAG, "onAudioRecogResultCallback() " + type);
            switch (type) {
                case 1:
                    CheeseShutterManager.this.audioCallbackRecogSuccess();
                    return;
                default:
                    return;
            }
        }
    }

    public CheeseShutterManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setVoiceRecogListener(OnVoiceRecogListener listener) {
        this.mVoiceListener = listener;
    }

    public void onResumeAfter() {
        initAudioShutterEngine();
    }

    public void onPauseBefore() {
        CamLog.m3d(CameraConstants.TAG, "onPauseBefore");
        setAudioRecogEngineStop();
        this.mAudioRecogEngine = null;
        if (!this.mGet.isModuleChanging()) {
            AudioUtil.setAudioFocus(this.mGet.getAppContext(), false);
        }
    }

    public void setCheeseShutterSetting(final boolean enable, final boolean requestAudioFocus, int waitingTime) {
        if (waitingTime < 0) {
            waitingTime = 0;
        }
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                CheeseShutterManager.this.setCheeseShutterSetting(enable, requestAudioFocus);
            }
        }, (long) waitingTime);
    }

    public void setCheeseShutterSetting(final boolean enable, final boolean requestAudioFocus) {
        int delay = 0;
        if (!enable || TelephonyUtil.phoneInCall(this.mGet.getAppContext()) || !MDMUtil.allowMicrophone()) {
            if (requestAudioFocus && AudioUtil.sRequestAudioFocusCount != 0) {
                AudioUtil.setAudioFocus(this.mGet.getAppContext(), false);
            }
            if (this.mStartRecogEngineRunnable != null) {
                this.mGet.removePostRunnable(this.mStartRecogEngineRunnable);
                this.mStartRecogEngineRunnable = null;
            }
            setAudioRecogEngineStop();
        } else if (checkAudioManagerCallStatus(true)) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    CheeseShutterManager.this.setCheeseShutterSetting(enable, requestAudioFocus);
                }
            }, 100);
        } else {
            if (requestAudioFocus) {
                int preAudiofocusCount = AudioUtil.sRequestAudioFocusCount;
                AudioUtil.setAudioFocus(this.mGet.getAppContext(), true);
                if (preAudiofocusCount == AudioUtil.sRequestAudioFocusCount) {
                    showCheeseShutterFailPopup();
                    this.mGet.setSetting(Setting.KEY_VOICESHUTTER, "off", true);
                    return;
                }
            }
            checkAudioManagerCallStatus(false);
            if (AudioUtil.isAudioRecording(getAppContext())) {
                delay = 500;
            }
            this.mStartRecogEngineRunnable = new HandlerRunnable(this) {
                public void handleRun() {
                    if (CheeseShutterManager.this.mAudioRecogEngine == null || CheeseShutterManager.this.mAudioRecogEngine.isAudioRecogEngineStarted() || delay == 0 || !AudioUtil.isAudioRecording(CheeseShutterManager.this.getAppContext())) {
                        CheeseShutterManager.this.setAudioRecogEngineStart();
                        return;
                    }
                    CheeseShutterManager.this.showCheeseShutterFailPopup();
                    CheeseShutterManager.this.mGet.setSetting(Setting.KEY_VOICESHUTTER, "off", true);
                }
            };
            this.mGet.postOnUiThread(this.mStartRecogEngineRunnable, (long) delay);
        }
    }

    private void showCheeseShutterFailPopup() {
        String shutterTitle;
        if (ModelProperties.useCheeseShutterTitle()) {
            shutterTitle = getString(C0088R.string.sp_cheeseshutter_NORMAL);
        } else {
            shutterTitle = getString(C0088R.string.sp_voiceshutter_NORMAL);
        }
        this.mGet.showToast(String.format(getString(C0088R.string.voiceshutter_error_occurred_try_again), new Object[]{shutterTitle}), CameraConstants.TOAST_LENGTH_SHORT);
    }

    private String getString(int stringId) {
        return this.mGet.getAppContext().getString(stringId);
    }

    private void updateCheeseShutterIndicator(boolean recog) {
        this.mGet.updateIndicator(1, 0, recog);
    }

    private void initAudioShutterEngine() {
        CamLog.m3d(CameraConstants.TAG, "initAudioShutterEngine");
        if (this.mAudioRecogEngine == null) {
            this.mAudioRecogEngine = new AudioRecogEngine(new AudioRecogEngineCallback(this, null), ModelProperties.getVoiceShutterKind());
        }
    }

    private void setAudioRecogEngineStop() {
        CamLog.m3d(CameraConstants.TAG, "setAudioRecogEngineStop");
        if (this.mAudioRecogEngine != null) {
            this.mAudioRecogEngine.stop();
        }
    }

    private void setAudioRecogEngineStart() {
        CamLog.m3d(CameraConstants.TAG, "setAudioRecogEngineStart");
        if (this.mGet.checkModuleValidate(192) && this.mAudioRecogEngine != null) {
            this.mAudioRecogEngine.start(ModelProperties.getVoiceShutterSensitivity());
        }
    }

    private boolean checkAudioManagerCallStatus(boolean check) {
        if (check && sCheckCount > 0 && AudioUtil.isAudioManagerCallStatus(this.mGet.getAppContext())) {
            sCheckCount--;
            CamLog.m3d(CameraConstants.TAG, "isAudioManagerCallStatus() == true, checkAudioManagerCallStatusCount = " + sCheckCount);
            return true;
        }
        sCheckCount = 30;
        return false;
    }

    private void audioCallbackRestartEngine() {
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                CheeseShutterManager.this.updateCheeseShutterIndicator(false);
                if (!MDMUtil.allowMicrophone()) {
                    CamLog.m3d(CameraConstants.TAG, "Can not start AudoRecogEngine because a microphone is not allowed");
                } else if ("on".equals(CheeseShutterManager.this.mGet.getSettingValue(Setting.KEY_PANO_SOUND_REC)) && CameraConstants.MODE_PANORAMA_LG_360_PROJ.equals(CheeseShutterManager.this.mGet.getShotMode()) && CheeseShutterManager.this.mGet.getCameraState() == 3) {
                    CamLog.m3d(CameraConstants.TAG, "360 panorama sound is recording, don't restart AudoRecogEngine");
                } else {
                    CheeseShutterManager.this.setAudioRecogEngineStart();
                }
            }
        }, 500);
    }

    private void audioCallbackRecogSuccess() {
        CamLog.m3d(CameraConstants.TAG, "sound recognize : take a picture!!!");
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (CheeseShutterManager.this.mVoiceListener != null) {
                    CheeseShutterManager.this.mVoiceListener.voiceRecogSuccess(0);
                }
                CheeseShutterManager.this.audioCallbackRestartEngine();
            }
        }, 400);
    }

    public void setDegree(int degree, boolean animation) {
    }
}
