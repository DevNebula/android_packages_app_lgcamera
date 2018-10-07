package com.lge.camera.managers;

import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.MmsProperties;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SettingKeyWrapper;

public class AudioZoomManager extends ManagerInterfaceImpl {
    private static final int ANGLE_MAX = 100;
    public static final int ANGLE_MIN = 0;
    private static final String KEY_ANGLE = "AUDIO_ZOOMING_LEVEL=";
    private static final String KEY_ORIENTATION = "AUDIO_ZOOMING_MODE=";
    private static final String ORIENTATION_0 = "0";
    private static final String ORIENTATION_180 = "180";
    private static final String ORIENTATION_270 = "270";
    private static final String ORIENTATION_90 = "90";
    private static final String ORIENTATION_INIT = "INIT";
    private static final String ORIENTATION_NONE = "-1";
    private final int SET_ANGLE = 2;
    private final int SET_ORIENTATION = 1;
    private int mAngle = -1;
    private AudioManager mAudioManager = null;
    private AudioZoomHandler mAudioZoomHandler = null;
    private HandlerThread mAudioZoomThread = null;
    private boolean mIsMMSsize = false;
    private RecorderListener mListener = null;
    private String mOrientaion = ORIENTATION_INIT;
    private float mZoomMaxValue = 0.0f;

    public interface RecorderListener {
        boolean isVideoRecording();

        void setAudiozoomMetadata();
    }

    private class AudioZoomHandler extends Handler {
        AudioZoomHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg != null) {
                try {
                    switch (msg.what) {
                        case 1:
                            String direction = AudioZoomManager.this.setOrientation(msg);
                            if (!AudioZoomManager.this.mOrientaion.equals(direction)) {
                                AudioZoomManager.this.mOrientaion = direction;
                                String orientation = AudioZoomManager.KEY_ORIENTATION + direction;
                                AudioZoomManager.this.setAudioParameters(orientation);
                                CamLog.m3d(CameraConstants.TAG, "[audio zoom] ===>" + orientation);
                                if (msg.arg1 == 5) {
                                    AudioZoomManager.this.setAudioZoomHandler(false);
                                    return;
                                }
                                return;
                            }
                            return;
                        case 2:
                            int currentZoom = msg.arg1;
                            if (AudioZoomManager.this.mListener != null && currentZoom > 0 && AudioZoomManager.this.mGet.getCameraState() == 6) {
                                AudioZoomManager.this.mListener.setAudiozoomMetadata();
                            }
                            int angle = AudioZoomManager.this.calculateAngle(currentZoom);
                            if (AudioZoomManager.this.mAngle != angle) {
                                AudioZoomManager.this.mAngle = angle;
                                String angleKey = AudioZoomManager.KEY_ANGLE + angle;
                                AudioZoomManager.this.setAudioParameters(angleKey);
                                CamLog.m3d(CameraConstants.TAG, "[audio zoom] ===>" + angleKey);
                                return;
                            }
                            return;
                        default:
                            throw new RuntimeException("Invalid AudioZoom message=" + msg.what);
                    }
                } catch (RuntimeException e) {
                    CamLog.m6e(CameraConstants.TAG, "AudioZoom Exception : ", e);
                }
                CamLog.m6e(CameraConstants.TAG, "AudioZoom Exception : ", e);
            }
        }
    }

    public AudioZoomManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setRecorderListener(RecorderListener listener) {
        this.mListener = listener;
    }

    public void setRotateDegree(int degree, boolean animation) {
        if (this.mListener == null || !this.mListener.isVideoRecording() || !this.mIsMMSsize) {
        }
    }

    private void setAudioParameters(String keyValuePairs) {
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) this.mGet.getAppContext().getSystemService("audio");
        }
        if (this.mAudioManager != null) {
            this.mAudioManager.setParameters(keyValuePairs);
        }
    }

    public void setAudioZoomHandler(boolean init) {
        CamLog.m3d(CameraConstants.TAG, "[audio zoom] setAudioZoomHandler : " + init);
        if (init) {
            CamLog.m3d(CameraConstants.TAG, "[audio zoom] init AudioZoomHandler - getLooper()");
            if (this.mAudioZoomThread == null) {
                this.mAudioZoomThread = new HandlerThread("[audio zoom] AudioZoom Handler Thread");
                this.mAudioZoomThread.start();
            }
            if (this.mAudioZoomHandler == null) {
                this.mAudioZoomHandler = new AudioZoomHandler(this.mAudioZoomThread.getLooper());
            }
        } else if (this.mAudioZoomHandler != null) {
            this.mAudioZoomHandler.removeCallbacksAndMessages(null);
            CamLog.m3d(CameraConstants.TAG, "[audio zoom] remove AudioZoomHandler - quit looper()");
            this.mAudioZoomHandler.getLooper().quit();
            this.mAudioZoomHandler = null;
            if (this.mAudioZoomThread != null) {
                this.mAudioZoomThread = null;
            }
        }
    }

    public void start(int orientation, int currentZoom, int zoomMax) {
        CamLog.m3d(CameraConstants.TAG, "AudioZoom start = " + orientation + " / " + currentZoom + " / " + zoomMax);
        if (this.mAudioZoomHandler == null) {
            setAudioZoomHandler(true);
        }
        this.mZoomMaxValue = (float) zoomMax;
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) this.mGet.getAppContext().getSystemService("audio");
        }
        this.mIsMMSsize = MmsProperties.isAvailableMmsResolution(this.mGet.getAppContext().getContentResolver(), this.mGet.getSettingValue(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId())).split("@")[0]);
        setOrientation(orientation);
        setAngle(currentZoom);
    }

    public void stop() {
        CamLog.m3d(CameraConstants.TAG, "AudioZoom stop");
        if (this.mGet == null) {
            CamLog.m3d(CameraConstants.TAG, "AudioZoom stop EXIT");
            reset();
            return;
        }
        setOrientation(5);
        setAngle(0);
        this.mIsMMSsize = false;
        this.mAngle = -1;
        this.mOrientaion = ORIENTATION_INIT;
    }

    private void reset() {
        this.mAudioManager = null;
        this.mIsMMSsize = false;
    }

    public void setOrientation(int orientation) {
        if (!isAudioZoomAvailable()) {
            CamLog.m3d(CameraConstants.TAG, "AudioZoom is not available");
        } else if (this.mAudioZoomHandler != null) {
            Message msg = new Message();
            msg.what = 1;
            msg.arg1 = orientation;
            this.mAudioZoomHandler.sendMessage(msg);
        }
    }

    private String setOrientation(Message msg) {
        int orientation = msg.arg1;
        String direction = ORIENTATION_NONE;
        switch (orientation) {
            case 0:
                return ORIENTATION_90;
            case 4:
            case 270:
                return "0";
            case 90:
                return ORIENTATION_180;
            case 180:
                return ORIENTATION_270;
            default:
                return ORIENTATION_NONE;
        }
    }

    public void setAngle(int currentZoom) {
        if (isAudioZoomAvailable()) {
            if (this.mGet.isOpticZoomSupported(null)) {
                currentZoom -= this.mGet.getInitZoomStep(this.mGet.getCameraId());
                if (currentZoom < 0) {
                    currentZoom = 0;
                }
            }
            if (this.mAudioZoomHandler != null) {
                Message msg = new Message();
                msg.what = 2;
                msg.arg1 = currentZoom;
                this.mAudioZoomHandler.sendMessage(msg);
                return;
            }
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "AudioZoom is not available");
    }

    private int calculateAngle(int zoomValue) {
        return (int) Math.max(Math.min((((float) zoomValue) / this.mZoomMaxValue) * 100.0f, 100.0f), 0.0f);
    }

    public void onPauseBefore() {
        setAudioZoomHandler(false);
        super.onPauseBefore();
    }

    public void onDestroy() {
        this.mListener = null;
        this.mAudioManager = null;
        super.onDestroy();
    }
}
