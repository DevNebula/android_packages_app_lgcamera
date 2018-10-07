package com.lge.camera.managers;

import android.content.Context;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Handler;
import com.lge.camera.C0088R;
import com.lge.camera.app.BoostService;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.CamLog;

public class SoundLoader {
    private static final String AF_FAILURE = "system/media/audio/ui/af_failure.ogg";
    private static final String AF_SUCCESS = "system/media/audio/ui/af_success.ogg";
    private static final String CONTI_SOUND = "system/media/audio/ui/continuous_shot.ogg";
    private static final String CONTI_SOUND_FRONT = "system/media/audio/ui/continuous_shot_front.ogg";
    private static final String CONTI_SOUND_LAST = "system/media/audio/ui/continuous_shot_last.ogg";
    private static final String REC_START = "system/media/audio/ui/camstart.ogg";
    private static final String REC_STOP = "system/media/audio/ui/camstop.ogg";
    private static final String SHUTTER_MIRROR_DOWN = "system/media/audio/ui/shutter_mirror_down.ogg";
    private static final String SHUTTER_MIRROR_UP = "system/media/audio/ui/shutter_mirror_up.ogg";
    private static final String SHUTTER_SOUND = "system/media/audio/ui/cam_snap_0.ogg";
    public static final int SHUTTER_SOUND_COUNT = 1;
    private static final String SOUND_RESOURCE_PATH = "system/media/audio/ui/";
    private static final String TIMER_COUNT = "system/media/audio/ui/cam_timer_1sec.ogg";
    private static final String TIMER_LAST = "system/media/audio/ui/cam_timer_last.ogg";
    public static final int TYPE_STREAM_ENFORCED_OTHER = 1;
    public static final int TYPE_STREAM_ENFORCED_SHUTTER = 0;
    public static final int TYPE_STREAM_MUSIC = 2;
    public static final int TYPE_STREAM_SYSTEM_EFFECT = 3;
    private static final int USE_PATH = -1;
    private static final String VOICE_CHEESE = "system/media/audio/ui/voicesound_cheese.ogg";
    private static final String VOICE_KIMCHI = "system/media/audio/ui/voicesound_kimchi.ogg";
    private static final String VOICE_LG = "system/media/audio/ui/voicesound_lg.ogg";
    private static final String VOICE_SMILE = "system/media/audio/ui/voicesound_smile.ogg";
    private static final String VOICE_TORIMASU = "system/media/audio/ui/voicesound_torimasu.ogg";
    private static final String VOICE_WHISKY = "system/media/audio/ui/voicesound_whisky.ogg";
    private static final String WHEEL_EFFECT = "system/media/audio/ui/wheel_effect.ogg";
    static SoundLoaderCallback sCb = null;
    private static int sReferenceCount = 0;
    private static SoundLoader sSoundLoader = null;
    private final OnLoadCompleteListener completeListener_music = new C11603();
    private final OnLoadCompleteListener completeListener_system = new C11614();
    private final OnLoadCompleteListener loadCompleteListener = new C11592();
    Handler mCbHandler = null;
    Context mContext = null;
    private boolean mInit = false;
    private boolean mIsLocked = false;
    private boolean mIsPoolLoadingDone = false;
    private Object mLockObject = null;
    private int mRequestedSound_loadedMask = 0;
    private int[] mShutter = new int[1];
    public boolean mShutterSoundLoaded = false;
    private Thread mSoundBuildThread = null;
    private int mSoundId_loadedMask = 0;
    private int mSoundId_loadedMask_music = 0;
    private int mSoundId_loadedMask_systemEffect = 0;
    public int mSoundId_registeredMask = -1;
    private Thread mSoundLoadThread = null;
    public int mSound_Timer1sec = 0;
    public int mSound_TimerLast = 0;
    public int mSound_afFail = 0;
    public int mSound_afSuccess = 0;
    public int mSound_continuous_shutter_front = 0;
    public int mSound_continuous_shutter_last = 0;
    public int mSound_continuous_shutter_rear = 0;
    private SoundPool mSound_pool = null;
    private SoundPool mSound_pool_music = null;
    private SoundPool mSound_pool_system = null;
    public int mSound_shutter = 0;
    public int mSound_shutter_mirror_down = 0;
    public int mSound_shutter_mirror_up = 0;
    public int mSound_startRecording = 0;
    public int mSound_stopRecording = 0;
    public int mSound_voiceShutter_LG = 0;
    public int mSound_voiceShutter_cheese = 0;
    public int mSound_voiceShutter_kimchi = 0;
    public int mSound_voiceShutter_smile = 0;
    public int mSound_voiceShutter_torimasu = 0;
    public int mSound_voiceShutter_whisky = 0;
    public int mSound_wheel_effect = 0;

    /* renamed from: com.lge.camera.managers.SoundLoader$1 */
    class C11581 implements Runnable {
        C11581() {
        }

        public void run() {
            SoundLoader.this.mSound_pool = new SoundPool(4, 7, 0);
            SoundLoader.this.mSound_pool.setOnLoadCompleteListener(SoundLoader.this.loadCompleteListener);
            SoundLoader.this.mSound_pool_music = new SoundPool(4, 3, 0);
            SoundLoader.this.mSound_pool_music.setOnLoadCompleteListener(SoundLoader.this.completeListener_music);
            SoundLoader.this.mSound_pool_system = new SoundPool(2, 1, 0);
            SoundLoader.this.mSound_pool_system.setOnLoadCompleteListener(SoundLoader.this.completeListener_system);
            SoundLoader.this.mSoundId_registeredMask = -1;
            SoundLoader.this.mSoundId_loadedMask = 0;
            SoundLoader.this.mSoundId_loadedMask_music = 0;
            SoundLoader.this.mSoundId_loadedMask_systemEffect = 0;
            SoundLoader.this.mRequestedSound_loadedMask = 0;
            SoundLoader.this.mShutterSoundLoaded = false;
            SoundLoader.this.loadSound(LOAD_TYPE.PRIMARY);
            SoundLoader.this.mIsPoolLoadingDone = true;
            SoundLoader.this.soundPoolLoadingDone();
        }
    }

    /* renamed from: com.lge.camera.managers.SoundLoader$2 */
    class C11592 implements OnLoadCompleteListener {
        C11592() {
        }

        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            CamLog.m9v(CameraConstants.TAG, "onLoadComplete() id:" + sampleId + ",status:" + status);
            SoundLoader.this.setLoadedSoundMaskID(0, sampleId);
            if (status != 0 && sampleId == 1) {
                SoundLoader.this.mSound_shutter = SoundLoader.this.loadSound(0, -1, SoundLoader.SHUTTER_SOUND, true);
                CamLog.m9v(CameraConstants.TAG, "onLoadComplete() reload path : system/media/audio/ui/cam_snap_0.ogg");
            }
            SoundLoader.this.soundLoadingDone(sampleId);
            if (!SoundLoader.this.mShutterSoundLoaded && SoundLoader.this.checkShutterSoundLoaded("")) {
                SoundLoader.this.mShutterSoundLoaded = true;
            }
            if (SoundLoader.this.checkAllSoundLoaded()) {
                BoostService.sSoundLoadWait.open();
                CamLog.m11w(CameraConstants.TAG, "All Sound Loaded");
            }
        }
    }

    /* renamed from: com.lge.camera.managers.SoundLoader$3 */
    class C11603 implements OnLoadCompleteListener {
        C11603() {
        }

        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            CamLog.m9v(CameraConstants.TAG, "completeListener_music() id:" + sampleId + ",status:" + status);
            SoundLoader.this.setLoadedSoundMaskID(2, sampleId);
        }
    }

    /* renamed from: com.lge.camera.managers.SoundLoader$4 */
    class C11614 implements OnLoadCompleteListener {
        C11614() {
        }

        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            CamLog.m9v(CameraConstants.TAG, "completeListener_system() id:" + sampleId + ",status:" + status);
            SoundLoader.this.setLoadedSoundMaskID(3, sampleId);
        }
    }

    /* renamed from: com.lge.camera.managers.SoundLoader$6 */
    class C11636 implements Runnable {
        C11636() {
        }

        public void run() {
            if (SoundLoader.sCb != null) {
                SoundLoader.sCb.onSoundPoolLoadingDone();
            }
        }
    }

    public enum LOAD_TYPE {
        PRIMARY(1),
        SECONDARY(2),
        LOAD_ALL(3);
        
        int mMask;

        private LOAD_TYPE(int value) {
            this.mMask = value;
        }

        boolean isContained(LOAD_TYPE compareType) {
            if ((this.mMask & compareType.mMask) == compareType.mMask) {
                return true;
            }
            return false;
        }
    }

    public interface SoundLoaderCallback {
        void onSoundLoadingDone(int i);

        void onSoundPoolLoadingDone();
    }

    private SoundLoader(Context context) {
        this.mContext = context;
        this.mCbHandler = new Handler(context.getMainLooper());
    }

    public static synchronized SoundLoader getSoundLoader(Context context, SoundLoaderCallback cb) {
        SoundLoader soundLoader;
        synchronized (SoundLoader.class) {
            if (sSoundLoader == null) {
                sSoundLoader = new SoundLoader(context);
                sSoundLoader.startLoadingSound();
                sSoundLoader.mInit = true;
                sReferenceCount = 0;
            }
            if (cb != null) {
                sCb = cb;
            }
            if (sSoundLoader.mIsPoolLoadingDone) {
                sSoundLoader.soundPoolLoadingDone();
            }
            sReferenceCount++;
            CamLog.m7i(CameraConstants.TAG, "getSoundLoader Ref : " + sReferenceCount);
            soundLoader = sSoundLoader;
        }
        return soundLoader;
    }

    public void resetSoundFile() {
        if (sReferenceCount == 1 && sSoundLoader != null && sSoundLoader.mIsLocked) {
            sSoundLoader.forceResetSoundFile();
        }
    }

    public synchronized void setLockedSoundLoader(Object lockObject, boolean set) {
        if (!this.mIsLocked || set || this.mLockObject == lockObject) {
            this.mIsLocked = set;
            this.mLockObject = lockObject;
            CamLog.m3d(CameraConstants.TAG, "setLockedSoundLoader " + set);
        }
    }

    public synchronized void releaseSoundLoader(SoundLoaderCallback cb) {
        sReferenceCount--;
        if (sReferenceCount > 0 || sSoundLoader == null) {
            resetSoundFile();
        } else {
            sCb = null;
            sSoundLoader.unloadSoundFile();
            sSoundLoader = null;
            sReferenceCount = 0;
        }
        if (sCb == cb) {
            sCb = null;
        }
        CamLog.m7i(CameraConstants.TAG, "releaseSoundLoader Ref : " + sReferenceCount);
    }

    public int getShutterSound(int index) {
        if (!this.mInit || this.mShutter == null) {
            return -1;
        }
        return this.mShutter[index];
    }

    public boolean isInit() {
        return this.mInit;
    }

    private void startLoadingSound() {
        waitSoundBuildThreadDone();
        if (this.mSound_pool != null) {
            soundPoolLoadingDone();
            return;
        }
        this.mSoundBuildThread = new Thread(new C11581());
        this.mSoundBuildThread.start();
    }

    private boolean checkShutterSoundLoaded(String mode) {
        if (!checkSoundLoaded(this.mSound_startRecording, 0)) {
            return false;
        }
        int shutterSoundID = this.mSound_shutter;
        if (CameraConstants.MODE_FULL_CONTINUOUS.equals(mode)) {
            shutterSoundID = this.mSound_continuous_shutter_rear;
        } else if (mode.contains(CameraConstants.MODE_PANORAMA)) {
            shutterSoundID = this.mSound_startRecording;
        } else {
            shutterSoundID = this.mSound_shutter;
        }
        if (checkSoundLoaded(shutterSoundID, 0)) {
            return true;
        }
        return false;
    }

    public boolean checkSoundLoaded(int soundID, int streamType) {
        int loadMaskID;
        switch (streamType) {
            case 2:
                loadMaskID = this.mSoundId_loadedMask_music;
                break;
            case 3:
                loadMaskID = this.mSoundId_loadedMask_systemEffect;
                break;
            default:
                loadMaskID = this.mSoundId_loadedMask;
                break;
        }
        int soundMaskID = 1 << soundID;
        if ((loadMaskID & soundMaskID) == soundMaskID) {
            return true;
        }
        return false;
    }

    private boolean checkAllSoundLoaded() {
        return this.mSoundId_registeredMask == this.mSoundId_loadedMask;
    }

    private void setLoadedSoundMaskID(int streamType, int sampleID) {
        switch (streamType) {
            case 2:
                this.mSoundId_loadedMask_music |= 1 << sampleID;
                return;
            case 3:
                this.mSoundId_loadedMask_systemEffect |= 1 << sampleID;
                return;
            default:
                this.mSoundId_loadedMask |= 1 << sampleID;
                return;
        }
    }

    public void setRegisteredSoundMaskID(int sampleID) {
        if (this.mSoundId_registeredMask == -1) {
            this.mSoundId_registeredMask = 1 << sampleID;
        } else {
            this.mSoundId_registeredMask |= 1 << sampleID;
        }
    }

    private boolean isNeedLoad(LOAD_TYPE requestType, LOAD_TYPE checkType) {
        if (checkType == LOAD_TYPE.LOAD_ALL) {
            CamLog.m3d(CameraConstants.TAG, " IllegalArgumentException ");
            return false;
        } else if (!requestType.isContained(checkType)) {
            return false;
        } else {
            int mask = checkType.mMask;
            if ((this.mRequestedSound_loadedMask & mask) != mask) {
                return true;
            }
            return false;
        }
    }

    private void loadingPrimarySource() {
        CamLog.m3d(CameraConstants.TAG, "loadingPrimarySource");
        this.mSound_shutter = loadSound(0, C0088R.raw.cam_snap_0, null, true);
        if (this.mSound_shutter == 0) {
            CamLog.m3d(CameraConstants.TAG, "Shutter Sound Load Failed");
        }
        this.mSound_startRecording = loadSound(1, C0088R.raw.camstart, null, true);
        this.mSound_Timer1sec = loadSound(1, C0088R.raw.cam_timer_1sec, null, true);
        this.mSound_TimerLast = loadSound(1, C0088R.raw.cam_timer_last, null, true);
    }

    private void loadingSecondarySource() {
        CamLog.m3d(CameraConstants.TAG, "loadingSecondarySource");
        this.mSound_afSuccess = loadSound(1, C0088R.raw.af_success, null, true);
        this.mSound_afFail = loadSound(1, C0088R.raw.af_failure, null, true);
        if (FunctionProperties.isSupportedLongShot(true)) {
            this.mSound_continuous_shutter_rear = loadSound(1, C0088R.raw.continuous_shot, null, true);
            this.mSound_continuous_shutter_front = loadSound(1, C0088R.raw.continuous_shot_front, null, true);
            this.mSound_continuous_shutter_last = loadSound(1, C0088R.raw.continuous_shot_last, null, true);
        }
        loadingShutterMirrorSound();
        this.mSound_stopRecording = loadSound(1, C0088R.raw.camstop, null, true);
        loadingWheelSoundEffect();
        loadingVoiceShutterSound();
    }

    private void loadingVoiceShutterSound() {
        if (FunctionProperties.isSupportedVoiceShutter()) {
            this.mSound_voiceShutter_cheese = loadSound(2, -1, VOICE_CHEESE, false);
            if (this.mSound_voiceShutter_cheese != 0) {
                this.mSound_voiceShutter_smile = loadSound(2, -1, VOICE_SMILE, false);
                this.mSound_voiceShutter_whisky = loadSound(2, -1, VOICE_WHISKY, false);
                this.mSound_voiceShutter_kimchi = loadSound(2, -1, VOICE_KIMCHI, false);
                this.mSound_voiceShutter_LG = loadSound(2, -1, VOICE_LG, false);
                if (ModelProperties.isSupportVoiceShutterJapanese()) {
                    this.mSound_voiceShutter_torimasu = loadSound(2, -1, VOICE_TORIMASU, false);
                    return;
                }
                return;
            }
            this.mSound_voiceShutter_cheese = loadSound(2, C0088R.raw.voicesound_cheese, null, false);
            this.mSound_voiceShutter_smile = loadSound(2, C0088R.raw.voicesound_smile, null, false);
            this.mSound_voiceShutter_whisky = loadSound(2, C0088R.raw.voicesound_whisky, null, false);
            this.mSound_voiceShutter_kimchi = loadSound(2, C0088R.raw.voicesound_kimchi, null, false);
            this.mSound_voiceShutter_LG = loadSound(2, C0088R.raw.voicesound_lg, null, false);
            if (ModelProperties.isSupportVoiceShutterJapanese()) {
                this.mSound_voiceShutter_torimasu = loadSound(2, C0088R.raw.voicesound_torimasu, null, false);
            }
        }
    }

    private void loadingCameraSoundSource(LOAD_TYPE type) {
        CamLog.m3d(CameraConstants.TAG, "Load camera sound sources from system");
        if (isNeedLoad(type, LOAD_TYPE.PRIMARY)) {
            if (!loadingPrimarySourceFromSystem()) {
                loadingPrimarySource();
            }
            this.mRequestedSound_loadedMask |= LOAD_TYPE.PRIMARY.mMask;
        }
        if (isNeedLoad(type, LOAD_TYPE.SECONDARY)) {
            if (!loadingSecondarySourceFromSystem()) {
                loadingSecondarySource();
            }
            this.mRequestedSound_loadedMask |= LOAD_TYPE.SECONDARY.mMask;
        }
        CamLog.m3d(CameraConstants.TAG, "Load camera sound sources from system - e");
    }

    private boolean loadingPrimarySourceFromSystem() {
        CamLog.m3d(CameraConstants.TAG, "loadingPrimarySource ****************** ");
        this.mSound_shutter = loadSound(0, -1, SHUTTER_SOUND, true);
        if (this.mSound_shutter == 0) {
            CamLog.m3d(CameraConstants.TAG, "Shutter Sound Load Failed");
            return false;
        }
        this.mSound_startRecording = loadSound(1, -1, REC_START, true);
        this.mSound_Timer1sec = loadSound(1, -1, TIMER_COUNT, true);
        this.mSound_TimerLast = loadSound(1, -1, TIMER_LAST, true);
        return true;
    }

    private boolean loadingSecondarySourceFromSystem() {
        CamLog.m3d(CameraConstants.TAG, "loadingSecondarySourceFromSystem ##################");
        this.mSound_afSuccess = loadSound(1, -1, AF_SUCCESS, true);
        if (this.mSound_afSuccess == 0) {
            CamLog.m3d(CameraConstants.TAG, "af Sound Load Failed");
            return false;
        }
        this.mSound_afFail = loadSound(1, -1, AF_FAILURE, true);
        if (FunctionProperties.isSupportedLongShot(true)) {
            this.mSound_continuous_shutter_rear = loadSound(1, -1, CONTI_SOUND, true);
            this.mSound_continuous_shutter_front = loadSound(1, -1, CONTI_SOUND_FRONT, true);
            this.mSound_continuous_shutter_last = loadSound(1, -1, CONTI_SOUND_LAST, true);
        }
        loadingShutterMirrorSound();
        this.mSound_stopRecording = loadSound(1, -1, REC_STOP, true);
        loadingWheelSoundEffect();
        loadingVoiceShutterSound();
        return true;
    }

    private void loadingShutterMirrorSound() {
        if (FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA)) {
            this.mSound_shutter_mirror_up = loadSound(1, -1, SHUTTER_MIRROR_UP, true);
            if (this.mSound_shutter_mirror_up == 0) {
                this.mSound_shutter_mirror_up = loadSound(1, C0088R.raw.shutter_mirror_up, null, true);
            }
            this.mSound_shutter_mirror_down = loadSound(1, -1, SHUTTER_MIRROR_DOWN, true);
            if (this.mSound_shutter_mirror_down == 0) {
                this.mSound_shutter_mirror_down = loadSound(1, C0088R.raw.shutter_mirror_down, null, true);
            }
        }
    }

    private void loadingWheelSoundEffect() {
        if (FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA)) {
            this.mSound_wheel_effect = loadSound(3, -1, WHEEL_EFFECT, false);
            if (this.mSound_wheel_effect == 0) {
                this.mSound_wheel_effect = loadSound(3, C0088R.raw.wheel_effect, null, false);
            }
        }
    }

    private void waitSoundBuildThreadDone() {
        try {
            if (this.mSoundBuildThread != null && this.mSoundBuildThread.isAlive()) {
                CamLog.m3d(CameraConstants.TAG, String.format("Wait for sound_pool load..", new Object[0]));
                this.mSoundBuildThread.join();
                this.mSoundBuildThread = null;
                CamLog.m3d(CameraConstants.TAG, String.format("sound_pool loaded..", new Object[0]));
            }
            if (this.mSoundLoadThread != null && this.mSoundLoadThread.isAlive()) {
                CamLog.m3d(CameraConstants.TAG, String.format("Wait for sound loader.", new Object[0]));
                this.mSoundLoadThread.join();
                this.mSoundLoadThread = null;
                CamLog.m3d(CameraConstants.TAG, String.format("Wait for sound loader.", new Object[0]));
            }
        } catch (InterruptedException e) {
            CamLog.m5e(CameraConstants.TAG, String.format("Failed to join sound_pool load thread!", new Object[0]));
            e.printStackTrace();
        }
    }

    private void waitSoundLoadThreadDone() {
        try {
            if (this.mSoundLoadThread != null && this.mSoundLoadThread.isAlive()) {
                CamLog.m3d(CameraConstants.TAG, String.format("Wait for sound loader.", new Object[0]));
                this.mSoundLoadThread.join();
                this.mSoundLoadThread = null;
                CamLog.m3d(CameraConstants.TAG, String.format("Wait for sound loader.", new Object[0]));
            }
        } catch (InterruptedException e) {
            CamLog.m5e(CameraConstants.TAG, String.format("Failed to join sound_pool load thread!", new Object[0]));
            e.printStackTrace();
        }
    }

    private void forceResetSoundFile() {
        CamLog.m3d(CameraConstants.TAG, "forceResetSoundFile : isLocked ? " + this.mIsLocked);
        unloadSoundFile();
        startLoadingSound();
    }

    private void unloadSoundFile() {
        waitSoundBuildThreadDone();
        CamLog.m3d(CameraConstants.TAG, "unloadSoundFile-start, sound_pool release 1/2");
        if (this.mSound_pool != null) {
            unloadSoundPool(0, 0);
            unloadSoundPool(this.mSound_afSuccess, 1);
            unloadSoundPool(this.mSound_afFail, 1);
            unloadSoundPool(this.mSound_startRecording, 1);
            unloadSoundPool(this.mSound_stopRecording, 1);
            unloadSoundPool(this.mSound_continuous_shutter_front, 1);
            unloadSoundPool(this.mSound_continuous_shutter_rear, 1);
            unloadSoundPool(this.mSound_continuous_shutter_last, 1);
            unloadSoundPool(this.mSound_shutter_mirror_up, 1);
            unloadSoundPool(this.mSound_shutter_mirror_down, 1);
            unloadSoundPool(this.mSound_Timer1sec, 1);
            unloadSoundPool(this.mSound_TimerLast, 1);
            unloadSoundPool(this.mSound_voiceShutter_cheese, 2);
            unloadSoundPool(this.mSound_voiceShutter_smile, 2);
            unloadSoundPool(this.mSound_voiceShutter_whisky, 2);
            unloadSoundPool(this.mSound_voiceShutter_kimchi, 2);
            unloadSoundPool(this.mSound_voiceShutter_LG, 2);
            if (ModelProperties.isSupportVoiceShutterJapanese()) {
                unloadSoundPool(this.mSound_voiceShutter_torimasu, 2);
            }
            unloadSoundPool(this.mSound_wheel_effect, 3);
            this.mSound_pool.setOnLoadCompleteListener(null);
            this.mSound_pool.release();
            this.mSound_pool = null;
            this.mSound_pool_music.setOnLoadCompleteListener(null);
            this.mSound_pool_music.release();
            this.mSound_pool_music = null;
            this.mSound_pool_system.setOnLoadCompleteListener(null);
            this.mSound_pool_system.release();
            this.mSound_pool_system = null;
        }
        CamLog.m3d(CameraConstants.TAG, "unloadSoundFile-end, sound_pool release 2/2");
        this.mIsPoolLoadingDone = false;
    }

    public synchronized void loadSound(final LOAD_TYPE type) {
        int mask = type.mMask;
        if ((this.mRequestedSound_loadedMask & mask) != mask) {
            waitSoundLoadThreadDone();
            this.mSoundLoadThread = new Thread(new Runnable() {
                public void run() {
                    CamLog.m3d(CameraConstants.TAG, "Sound Load - START : [loadingCameraSoundSource]");
                    SoundLoader.this.loadingCameraSoundSource(type);
                    CamLog.m3d(CameraConstants.TAG, "Sound Load - END : [loadingCameraSoundSource]");
                }
            });
            this.mSoundLoadThread.start();
        }
    }

    private int loadSound(int streamType, int resId, String path, boolean registerMaskId) {
        switch (streamType) {
            case 2:
                return loadSoundToMusicPool(resId, path);
            case 3:
                return loadSoundToSystemEffectPool(resId, path);
            default:
                return loadSoundToEnforcedPool(resId, path, registerMaskId);
        }
    }

    private int loadSoundToEnforcedPool(int resId, String path, boolean registerMaskId) {
        int soundId = 0;
        if (this.mSound_pool != null) {
            if (path == null) {
                soundId = this.mSound_pool.load(this.mContext, resId, 1);
            } else {
                soundId = this.mSound_pool.load(path, 1);
            }
            if (registerMaskId) {
                setRegisteredSoundMaskID(soundId);
            }
        }
        return soundId;
    }

    private int loadSoundToMusicPool(int resId, String path) {
        if (this.mSound_pool_music == null) {
            return 0;
        }
        if (path == null) {
            return this.mSound_pool_music.load(this.mContext, resId, 1);
        }
        return this.mSound_pool_music.load(path, 1);
    }

    private int loadSoundToSystemEffectPool(int resId, String path) {
        if (this.mSound_pool_system == null) {
            return 0;
        }
        if (path == null) {
            return this.mSound_pool_system.load(this.mContext, resId, 1);
        }
        return this.mSound_pool_system.load(path, 1);
    }

    private void unloadSoundPool(int index, int streamType) {
        CamLog.m3d(CameraConstants.TAG, "unloadSoundPool, start - Index = " + index + ", stream type = " + streamType);
        switch (streamType) {
            case 0:
                if (this.mShutter != null && this.mShutter[index] > 0 && checkSoundLoaded(this.mShutter[index], 0)) {
                    CamLog.m3d(CameraConstants.TAG, "unloadSoundPool, mShutter[index] = " + this.mShutter[index]);
                    this.mSound_pool.unload(this.mShutter[index]);
                    this.mShutter[index] = 0;
                    break;
                }
            case 2:
                if (index > 0 && checkSoundLoaded(index, 2)) {
                    this.mSound_pool_music.unload(index);
                    break;
                }
            case 3:
                if (index > 0 && checkSoundLoaded(index, 3)) {
                    this.mSound_pool_system.unload(index);
                    break;
                }
            default:
                if (index > 0 && checkSoundLoaded(index, 1)) {
                    this.mSound_pool.unload(index);
                    break;
                }
        }
        CamLog.m3d(CameraConstants.TAG, "unloadSoundPool, end ");
    }

    public SoundPool getSoundPool() {
        return this.mSound_pool;
    }

    public SoundPool getSoundMusicPool() {
        return this.mSound_pool_music;
    }

    public SoundPool getSoundSystemPool() {
        return this.mSound_pool_system;
    }

    public int getVoiceCommandSoundID(int soundIndex) {
        switch (soundIndex) {
            case 0:
                return this.mSound_voiceShutter_cheese;
            case 1:
                return this.mSound_voiceShutter_smile;
            case 2:
                return this.mSound_voiceShutter_whisky;
            case 3:
                return this.mSound_voiceShutter_kimchi;
            case 4:
                return this.mSound_voiceShutter_LG;
            case 5:
                return this.mSound_voiceShutter_torimasu;
            default:
                return 0;
        }
    }

    private void soundPoolLoadingDone() {
        if (sCb != null && this.mCbHandler != null) {
            this.mCbHandler.post(new C11636());
        }
    }

    private void soundLoadingDone(final int sampleId) {
        if (sCb != null && this.mCbHandler != null) {
            this.mCbHandler.post(new Runnable() {
                public void run() {
                    if (SoundLoader.sCb != null) {
                        SoundLoader.sCb.onSoundLoadingDone(sampleId);
                    }
                }
            });
        }
    }
}
