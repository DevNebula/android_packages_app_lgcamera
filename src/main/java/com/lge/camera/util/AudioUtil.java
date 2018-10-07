package com.lge.camera.util;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.AudioManagerEx;
import android.media.AudioSystem;
import android.media.IAudioService.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.View;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.VolumeVibratorManager;

public class AudioUtil {
    public static OnAudioFocusChangeListener sAudioFocusChangeLister;
    public static int sHeadsetState = 0;
    private static boolean sIsAudioAvailability = true;
    private static Boolean sIsCameraSoundForced = null;
    public static boolean sIsMuteNotificationStream = false;
    public static boolean sIsMuteSystemStream = false;
    private static boolean sIsRingerSteamVibrated = false;
    private static boolean sIsStreamMuted = false;
    private static int sNumOfMic = -1;
    public static int sRequestAudioFocusCount = 0;

    public static void pauseAudioPlayback(Context context) {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        context.sendBroadcast(i);
        if (ModelProperties.getCarrierCode() == 2) {
            Intent m = new Intent("com.iloen.melon.musicservicecommand");
            m.putExtra("command", "pause");
            context.sendBroadcast(m);
        }
        setAudioFocus(context, true);
    }

    public static void resumeAudioPlayback(Context context) {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "togglepause");
        context.sendBroadcast(i);
        setAudioFocus(context, false);
    }

    public static void setAudioFocusChangeListener(OnAudioFocusChangeListener listener) {
        CamLog.m3d(CameraConstants.TAG, "setAudioFocusChangeListener, listener = " + listener);
        sAudioFocusChangeLister = listener;
    }

    public static void setAudioFocus(Context context, boolean requestAudioFocus) {
        AudioManager am = (AudioManager) context.getSystemService("audio");
        if (requestAudioFocus) {
            CamLog.m3d(CameraConstants.TAG, "++ Get audiofocus");
            if (sAudioFocusChangeLister != null) {
                if (Utils.checkOOS()) {
                    am.unregisterAudioFocusRequest(sAudioFocusChangeLister);
                } else {
                    unregisterAudioFocusListener(am, sAudioFocusChangeLister);
                }
            }
            am.requestAudioFocus(sAudioFocusChangeLister, 3, 2);
            sRequestAudioFocusCount++;
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "-- Abandon audioFocus");
        if (Utils.checkOOS()) {
            am.unregisterAudioFocusRequest(sAudioFocusChangeLister);
        } else {
            unregisterAudioFocusListener(am, sAudioFocusChangeLister);
        }
        am.abandonAudioFocus(sAudioFocusChangeLister);
        sRequestAudioFocusCount--;
        if (sRequestAudioFocusCount < 0) {
            sRequestAudioFocusCount = 0;
        }
    }

    public static void setAudioFocus(Context context, boolean requestAudioFocus, boolean isTransient) {
        if (isTransient) {
            setAudioFocus(context, requestAudioFocus);
            return;
        }
        AudioManager am = (AudioManager) context.getSystemService("audio");
        if (am == null) {
            return;
        }
        if (requestAudioFocus) {
            if (sAudioFocusChangeLister != null) {
                if (Utils.checkOOS()) {
                    am.unregisterAudioFocusRequest(sAudioFocusChangeLister);
                } else {
                    unregisterAudioFocusListener(am, sAudioFocusChangeLister);
                }
            }
            CamLog.m3d(CameraConstants.TAG, "++ Get audiofocus-stopAudioPlayback by get audiofocus");
            if (1 == am.requestAudioFocus(sAudioFocusChangeLister, 3, 1)) {
                sRequestAudioFocusCount++;
                return;
            }
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "-- Loose audioFocus");
        if (Utils.checkOOS()) {
            am.unregisterAudioFocusRequest(sAudioFocusChangeLister);
        } else {
            unregisterAudioFocusListener(am, sAudioFocusChangeLister);
        }
        am.abandonAudioFocus(null);
        sRequestAudioFocusCount--;
        if (sRequestAudioFocusCount < 0) {
            sRequestAudioFocusCount = 0;
        }
    }

    public static void checkAudioFocus(Context context) {
        if (sRequestAudioFocusCount != 0) {
            CamLog.m11w(CameraConstants.TAG, "Check requestAudioFocusCount : current count is = " + sRequestAudioFocusCount);
            CamLog.m11w(CameraConstants.TAG, "Check requestAudioFocusCount : doing abandonAudioFocus");
            ((AudioManager) context.getSystemService("audio")).abandonAudioFocus(null);
            sRequestAudioFocusCount = 0;
        }
    }

    public static void setStopNotificationStream(Context context) {
        context.sendBroadcast(new Intent("com.lge.media.STOP_NOTIFICATION"));
    }

    public static void setMuteNotificationStream(Context context, boolean set) {
        AudioManager am = (AudioManager) context.getSystemService("audio");
        if (am == null) {
            return;
        }
        if (set) {
            if (am.getStreamVolume(5) != 0) {
                CamLog.m3d(CameraConstants.TAG, "set mute to notification stream : ON");
                am.setStreamMute(5, true);
                sIsMuteNotificationStream = true;
            }
        } else if (sIsMuteNotificationStream) {
            CamLog.m3d(CameraConstants.TAG, "set mute to notification stream : OFF");
            am.setStreamMute(5, false);
            sIsMuteNotificationStream = false;
        }
    }

    public static void setMuteSystemStream(Context context, boolean set) {
        AudioManager am = (AudioManager) context.getSystemService("audio");
        if (am == null) {
            return;
        }
        if (set) {
            if (am.getStreamVolume(1) != 0) {
                CamLog.m3d(CameraConstants.TAG, "set mute to notification stream : ON");
                am.setStreamMute(1, true);
                sIsMuteSystemStream = true;
            }
        } else if (sIsMuteSystemStream) {
            CamLog.m3d(CameraConstants.TAG, "set mute to notification stream : OFF");
            am.setStreamMute(1, false);
            sIsMuteSystemStream = false;
        }
    }

    public static int getNumOfMic(Context context) {
        if (sNumOfMic != -1) {
            return sNumOfMic;
        }
        String keys = "number_of_mic";
        String result = ((AudioManager) context.getSystemService("audio")).getParameters(keys);
        CamLog.m3d(CameraConstants.TAG, "[mic] num : " + result);
        if (result.contains(keys + "=")) {
            sNumOfMic = Integer.valueOf(result.replace(keys + "=", "")).intValue();
        }
        return sNumOfMic;
    }

    public static boolean isWiredHeadsetHasMicOn() {
        return AudioSystem.getDeviceConnectionState(4, "") != 0;
    }

    public static boolean isWiredHeadsetWithoutMicOn() {
        return AudioSystem.getDeviceConnectionState(8, "") != 0;
    }

    public static boolean isBluetoothA2dpOn() {
        return AudioSystem.getDeviceConnectionState(128, "") != 0;
    }

    public static boolean isWiredHeadsetOn() {
        return isWiredHeadsetWithoutMicOn() || isWiredHeadsetHasMicOn();
    }

    public static boolean isAudioManagerCallStatus(Context context) {
        AudioManager am = (AudioManager) context.getSystemService("audio");
        if (am != null) {
            int mode = am.getMode();
            if (mode == 2 || mode == 3 || mode == 1) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAudioRecording(Context context) {
        AudioManagerEx manager = new AudioManagerEx(context);
        if (manager != null && !manager.isRecording()) {
            return false;
        }
        CamLog.m7i(CameraConstants.TAG, "other application is using audio");
        return true;
    }

    public static void setAudioAvailability(boolean isAvailable) {
        CamLog.m3d(CameraConstants.TAG, "setAudioAvailability = " + isAvailable);
        sIsAudioAvailability = isAvailable;
    }

    public static boolean checkAudioAvailabilityBeforeRecording() {
        return sIsAudioAvailability;
    }

    public static void setStreamMute(Context context, int[] stream, boolean state) {
        CamLog.m3d(CameraConstants.TAG, "setStreamMute = " + state);
        if (state == sIsStreamMuted) {
            CamLog.m3d(CameraConstants.TAG, "exit because try same state!");
            return;
        }
        AudioManager am = (AudioManager) context.getSystemService("audio");
        if (am == null) {
            CamLog.m3d(CameraConstants.TAG, "exit because service is null!");
            return;
        }
        int audioMode = am.getRingerModeInternal();
        if (state || !(audioMode == 0 || audioMode == 1)) {
            int i = 0;
            while (i < stream.length) {
                if (stream[i] == 2 || (!ModelProperties.isPhone(context) && stream[i] == 5)) {
                    setRingerModeMute(am, state, stream[i]);
                } else if (state) {
                    am.adjustStreamVolume(stream[i], -100, 0);
                } else {
                    am.adjustStreamVolume(stream[i], 100, 0);
                }
                i++;
            }
            sIsStreamMuted = state;
            return;
        }
        sIsStreamMuted = state;
        CamLog.m3d(CameraConstants.TAG, "exit because of silent or vibrate mode");
    }

    private static void setRingerModeMute(AudioManager am, boolean state, int stream) {
        if (am != null) {
            CamLog.m3d(CameraConstants.TAG, "set mute to ringer mode stream : " + am.getRingerModeInternal() + " , state : " + state + ", stream : " + stream);
            if (state) {
                if (am.getRingerModeInternal() == 1 || am.getRingerModeInternal() == 0) {
                    sIsRingerSteamVibrated = true;
                    return;
                }
                am.adjustStreamVolume(stream, -100, 0);
                sIsRingerSteamVibrated = false;
            } else if (!sIsRingerSteamVibrated) {
                am.adjustStreamVolume(stream, 100, 0);
            }
        }
    }

    private static void setVibrationMute(Context context, boolean state) {
        VolumeVibratorManager mVibrationManager = (VolumeVibratorManager) new LGContext(context).getLGSystemService("volumevibrator");
        if (mVibrationManager != null) {
            mVibrationManager.setVibrateMute(state);
        }
    }

    public static void setAllSoundCaseMute(Context context, boolean state) {
        setStreamMute(context, new int[]{1, 5, 2}, state);
        setVibrationMute(context, state);
    }

    public static boolean isInP2PCallMode(Context context) {
        if (((AudioManager) context.getSystemService("audio")).getMode() == 2) {
            return true;
        }
        return false;
    }

    public static void setHeadsetState(int state) {
        CamLog.m3d(CameraConstants.TAG, "[audio] setHeadsetState = " + state);
        sHeadsetState = state;
    }

    public static int getHeadsetState() {
        return sHeadsetState;
    }

    public static void checkHeadsetState() {
        if (isWiredHeadsetHasMicOn()) {
            setHeadsetState(2);
        } else if (isWiredHeadsetWithoutMicOn()) {
            setHeadsetState(1);
        } else {
            setHeadsetState(0);
        }
    }

    public static void setUseBuiltInMicForRecording(Context context, boolean useBuiltIn) {
        AudioManager am = (AudioManager) context.getSystemService("audio");
        CamLog.m3d(CameraConstants.TAG, "setUseBuiltInMicForRecording = " + useBuiltIn);
        if (am == null) {
            return;
        }
        if (useBuiltIn) {
            am.setParameters("use_builtin_mic=1");
        } else {
            am.setParameters("use_builtin_mic=0");
        }
    }

    public static void enableRaM(Context context, boolean enable) {
        AudioManager am = (AudioManager) context.getSystemService("audio");
        CamLog.m3d(CameraConstants.TAG, "enableRaM = " + enable);
        if (am == null) {
            return;
        }
        if (enable) {
            am.setParameters("high_spl=on");
        } else {
            am.setParameters("high_spl=off");
        }
    }

    public static void camcorderEndSound(Context context) {
        AudioManager am = (AudioManager) context.getSystemService("audio");
        CamLog.m3d(CameraConstants.TAG, "camcorderEndSound");
        if (am != null) {
            am.setParameters("camcorder_end_sound=nxp");
        }
    }

    private static boolean isRingtoneMuted(Context context) {
        AudioManager am = (AudioManager) context.getSystemService("audio");
        if (am != null) {
            int audioMode = am.getRingerModeInternal();
            if (audioMode == 0 || audioMode == 1) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasHapticLimitation(View view, int feedbackConstant) {
        if (feedbackConstant == 65573) {
            if (sIsCameraSoundForced == null) {
                try {
                    sIsCameraSoundForced = Boolean.valueOf(Stub.asInterface(ServiceManager.getService("audio")).isCameraSoundForced());
                } catch (RemoteException e) {
                    CamLog.m5e(CameraConstants.TAG, "Audio service is unavailable for queries");
                    sIsCameraSoundForced = Boolean.FALSE;
                }
            }
            if (!sIsCameraSoundForced.booleanValue()) {
                return isRingtoneMuted(view.getContext());
            }
        }
        return false;
    }

    public static boolean performHapticFeedback(View view, int feedbackConstant) {
        return performHapticFeedback(view, feedbackConstant, 0);
    }

    public static boolean performHapticFeedback(View view, int feedbackConstant, int flags) {
        if (view == null || hasHapticLimitation(view, feedbackConstant)) {
            return false;
        }
        return view.performHapticFeedback(feedbackConstant, flags);
    }

    private static void unregisterAudioFocusListener(AudioManager am, OnAudioFocusChangeListener listener) {
        try {
            Class.forName("android.media.AudioManager").getDeclaredMethod("unregisterAudioFocusListener", new Class[]{OnAudioFocusChangeListener.class}).invoke(am, new Object[]{listener});
            CamLog.m3d(CameraConstants.TAG, "#### unregisterAudioFocusListener invoke success - ");
        } catch (Exception e) {
            CamLog.m4d(CameraConstants.TAG, "unregisterAudioFocusListener error : ", e);
        }
    }

    public static void setSleepForRecordSound() {
        try {
            Thread.sleep((long) getDelayTimeForRecordSound(true));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int getDelayTimeForRecordSound(boolean isBeforeRecorderInit) {
        return isBeforeRecorderInit ? 200 : 400;
    }
}
