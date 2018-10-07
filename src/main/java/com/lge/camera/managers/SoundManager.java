package com.lge.camera.managers;

import android.media.AudioManager;
import android.media.SoundPool;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.SoundLoader.LOAD_TYPE;
import com.lge.camera.managers.SoundLoader.SoundLoaderCallback;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.TelephonyUtil;

public class SoundManager implements SoundLoaderCallback {
    public static final int MAX_CONTINUOUS_SHOT_SOUND = 5;
    private AudioManager mAudioManager = null;
    private int mAudioMode = 2;
    private int mContinuousSoundResultID = 0;
    private SoundManagerInterface mGet = null;
    private boolean mIsBurstRepeat = false;
    private Object mPlaySync = new Object();
    private int mSoundId_beforeLoaded = 0;
    private SoundLoader mSoundLoader = null;
    private SoundPool mSound_pool = null;
    private SoundPool mSound_pool_music = null;
    private SoundPool mSound_pool_system = null;
    private int mVoiceCommandStream = 0;

    public SoundManager(SoundManagerInterface iface) {
        this.mGet = iface;
    }

    public void init() {
        if (this.mSoundLoader == null) {
            this.mSoundLoader = SoundLoader.getSoundLoader(this.mGet.getAppContext(), this);
        } else {
            onSoundPoolLoadingDone();
        }
    }

    public void playSound(int type, boolean pBoolean, int pInt) {
        CamLog.m3d(CameraConstants.TAG, "playSound type=" + type + " bool=" + pBoolean + " int=" + pInt);
        if (this.mSoundLoader == null) {
            CamLog.m3d(CameraConstants.TAG, "exit playSound mSoundLoader = null");
            return;
        }
        switch (type) {
            case 1:
                playAFSound(pBoolean);
                return;
            case 2:
                soundPlay(this.mSoundLoader.mSound_shutter);
                return;
            case 3:
                playRecordingSound(pBoolean);
                return;
            case 4:
                playTimerSound(pInt);
                return;
            case 5:
                playBurstShotShutterSoundFront(pBoolean);
                return;
            case 6:
                playBurstShotShutterSound(pBoolean);
                return;
            case 7:
                CamLog.m3d(CameraConstants.TAG, "playPanoramaShutterSound");
                soundPlay(this.mSoundLoader.mSound_continuous_shutter_rear);
                return;
            case 8:
                playVoiceCommandSound(pInt);
                return;
            case 9:
                soundPlay(this.mSoundLoader.getShutterSound(pInt));
                return;
            case 10:
                playShutterMirrorSound(pBoolean);
                return;
            case 11:
                playWheelSoundEffect();
                return;
            case 12:
                playBurstShotShutterCloseSound();
                return;
            default:
                return;
        }
    }

    public void loadSound() {
        if (this.mSoundLoader != null) {
            this.mSoundLoader.loadSound(LOAD_TYPE.LOAD_ALL);
        }
    }

    private void playAFSound(boolean success) {
        CamLog.m3d(CameraConstants.TAG, "playAFSound : seccess=" + success);
        if (success) {
            if (this.mSoundLoader.checkSoundLoaded(this.mSoundLoader.mSound_afSuccess, 1)) {
                soundPlay(this.mSoundLoader.mSound_afSuccess);
            }
        } else if (this.mSoundLoader.checkSoundLoaded(this.mSoundLoader.mSound_afFail, 1)) {
            soundPlay(this.mSoundLoader.mSound_afFail);
        }
    }

    private void playBurstShotShutterSound(boolean repeat) {
        CamLog.m3d(CameraConstants.TAG, "playBurstShotShutterSound");
        soundPlayBurstShot(this.mSoundLoader.mSound_continuous_shutter_rear, repeat);
    }

    private void playBurstShotShutterSoundFront(boolean repeat) {
        CamLog.m3d(CameraConstants.TAG, "playBurstShotShutterSound");
        soundPlayBurstShot(this.mSoundLoader.mSound_continuous_shutter_front, repeat);
    }

    private void playShutterMirrorSound(boolean mirrorUp) {
        if (mirrorUp) {
            soundPlay(this.mSoundLoader.mSound_shutter_mirror_up);
        } else {
            soundPlay(this.mSoundLoader.mSound_shutter_mirror_down);
        }
    }

    private void playRecordingSound(final boolean start) {
        CamLog.m3d(CameraConstants.TAG, "playRecordingSound : start = " + start);
        new Thread() {
            public void run() {
                synchronized (SoundManager.this.mPlaySync) {
                    if (SoundManager.this.mSoundLoader == null) {
                        CamLog.m7i(CameraConstants.TAG, "return play recording sound. mSoundLoader is null");
                        return;
                    }
                    int i;
                    SoundManager soundManager = SoundManager.this;
                    if (start) {
                        i = SoundManager.this.mSoundLoader.mSound_startRecording;
                    } else {
                        i = SoundManager.this.mSoundLoader.mSound_stopRecording;
                    }
                    soundManager.soundPlay(i);
                }
            }
        }.start();
    }

    private void playTimerSound(int time) {
        CamLog.m3d(CameraConstants.TAG, "playTimerSound : time = " + time);
        if (!TelephonyUtil.phoneInCall(this.mGet.getAppContext())) {
            if (time <= 2) {
                soundPlay(this.mSoundLoader.mSound_TimerLast);
            } else {
                soundPlay(this.mSoundLoader.mSound_Timer1sec);
            }
        }
    }

    private void playVoiceCommandSound(int soundIndex) {
        CamLog.m3d(CameraConstants.TAG, "playVoiceCommandSound : soundIndex = " + soundIndex);
        if (this.mSound_pool_music != null) {
            if (this.mVoiceCommandStream != 0) {
                CamLog.m3d(CameraConstants.TAG, "Voice shutter sound stop.");
                this.mSound_pool_music.stop(this.mVoiceCommandStream);
            }
            int soundSource = this.mSoundLoader.getVoiceCommandSoundID(soundIndex);
            this.mVoiceCommandStream = this.mSound_pool_music.play(soundSource, 1.0f, 1.0f, 0, 0, 1.0f);
            CamLog.m7i(CameraConstants.TAG, "voiceCommandStream.play :" + soundSource + "result : " + this.mVoiceCommandStream);
            if (this.mVoiceCommandStream == 0) {
                this.mSoundId_beforeLoaded = soundSource;
            }
        }
    }

    private void playWheelSoundEffect() {
        if (this.mSound_pool_system != null) {
            this.mSound_pool_system.play(this.mSoundLoader.mSound_wheel_effect, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    private void soundPlay(int soundSource) {
        this.mAudioManager = (AudioManager) this.mGet.getAppContext().getSystemService("audio");
        this.mAudioMode = this.mAudioManager.getRingerModeInternal();
        if (((this.mAudioMode != 0 && this.mAudioMode != 1) || (soundSource != this.mSoundLoader.mSound_afSuccess && soundSource != this.mSoundLoader.mSound_afFail)) && this.mSound_pool != null) {
            int result = this.mSound_pool.play(soundSource, 1.0f, 1.0f, 0, 0, 1.0f);
            CamLog.m7i(CameraConstants.TAG, "mSound_pool.play :" + soundSource + "result :" + result);
            if (result == 0) {
                this.mSoundId_beforeLoaded = soundSource;
            }
        }
    }

    private void soundPlayBurstShot(int soundSource, boolean repeat) {
        if ((soundSource == this.mSoundLoader.mSound_continuous_shutter_front || soundSource == this.mSoundLoader.mSound_continuous_shutter_rear) && this.mSound_pool != null) {
            CamLog.m7i(CameraConstants.TAG, "mSound_pool.play soundSource : " + soundSource);
            if (this.mContinuousSoundResultID != 0) {
                stopSoundBurstShot();
            }
            this.mContinuousSoundResultID = this.mSound_pool.play(soundSource, 1.0f, 1.0f, 0, repeat ? -1 : 0, 1.0f);
            if (this.mContinuousSoundResultID == 0) {
                this.mSoundId_beforeLoaded = soundSource;
                this.mIsBurstRepeat = repeat;
            }
        }
    }

    public void stopSound(int type) {
        switch (type) {
            case 6:
                stopSoundBurstShot();
                break;
            case 8:
                stopVoiceCommandSound();
                break;
        }
        this.mSoundId_beforeLoaded = 0;
    }

    private void stopSoundBurstShot() {
        if (this.mSound_pool != null) {
            if (this.mContinuousSoundResultID != 0) {
                CamLog.m7i(CameraConstants.TAG, "mSound_pool.stop mContinuousSoundResultID : " + this.mContinuousSoundResultID);
                this.mSound_pool.stop(this.mContinuousSoundResultID);
            }
            if (this.mSoundId_beforeLoaded == this.mSoundLoader.mSound_continuous_shutter_front || this.mSoundId_beforeLoaded == this.mSoundLoader.mSound_continuous_shutter_rear) {
                this.mSoundId_beforeLoaded = 0;
            }
        }
        this.mContinuousSoundResultID = 0;
    }

    private void playBurstShotShutterCloseSound() {
        if (this.mSound_pool != null) {
            this.mSound_pool.play(this.mSoundLoader.mSound_continuous_shutter_last, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    private void stopVoiceCommandSound() {
        CamLog.m3d(CameraConstants.TAG, "stopVoiceCommandSound ");
        if (this.mSound_pool_music != null && this.mVoiceCommandStream != 0) {
            this.mSound_pool_music.stop(this.mVoiceCommandStream);
        }
    }

    public void onResumeAfter() {
        CamLog.m3d(CameraConstants.TAG, "onResume-start");
        if (this.mSoundLoader == null) {
            this.mSoundLoader = SoundLoader.getSoundLoader(this.mGet.getAppContext(), this);
        } else {
            onSoundPoolLoadingDone();
        }
        if (this.mSoundLoader.isInit()) {
            CamLog.m3d(CameraConstants.TAG, "onResume-end");
        }
    }

    public void onDestroy() {
        synchronized (this.mPlaySync) {
            if (this.mSoundLoader != null) {
                this.mSoundLoader.releaseSoundLoader(this);
                this.mSoundLoader = null;
            }
        }
    }

    public boolean isLoadingDone() {
        return (this.mSound_pool == null || this.mSound_pool_music == null || this.mSound_pool_system == null) ? false : true;
    }

    public void onSoundPoolLoadingDone() {
        if (this.mSoundLoader != null) {
            this.mSound_pool = this.mSoundLoader.getSoundPool();
            this.mSound_pool_music = this.mSoundLoader.getSoundMusicPool();
            this.mSound_pool_system = this.mSoundLoader.getSoundSystemPool();
        }
    }

    public void onSoundLoadingDone(int sampleId) {
        if (this.mSoundLoader != null && this.mSoundId_beforeLoaded == sampleId) {
            if (sampleId == this.mSoundLoader.mSound_continuous_shutter_rear || sampleId == this.mSoundLoader.mSound_continuous_shutter_front) {
                this.mContinuousSoundResultID = this.mSound_pool.play(sampleId, 1.0f, 1.0f, 10, this.mIsBurstRepeat ? -1 : 0, 1.0f);
            } else {
                this.mSound_pool.play(sampleId, 1.0f, 1.0f, 0, 0, 1.0f);
            }
            this.mSoundId_beforeLoaded = 0;
        }
    }
}
