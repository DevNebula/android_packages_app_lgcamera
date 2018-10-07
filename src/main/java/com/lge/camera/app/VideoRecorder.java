package com.lge.camera.app;

import android.hardware.Camera;
import android.location.Location;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.ConditionVariable;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.constants.MultimediaProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;
import com.lge.media.MediaRecorderEx;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class VideoRecorder {
    private static final int AUDIO_SOURCE_TYPE = 5;
    public static final int CAMERA_DEFAULT = 0;
    private static final int CAMERA_MAX = 2;
    public static final int CAMERA_SECOND = 1;
    public static final int LOOP_STATE_LOOP_GAPLESS = 2;
    public static final int LOOP_STATE_LOOP_NORMAL = 1;
    public static final int LOOP_STATE_LOOP_SURFACE = 3;
    public static final int LOOP_STATE_NONE = 0;
    private static String sFileName = null;
    private static String sFilePath = null;
    private static String sFilePathForLoopRecording = null;
    private static boolean sInit = false;
    private static boolean sIsRecording = false;
    private static long sLoopRecordStartTime = 0;
    private static int sLoopState = 0;
    private static int sMaxDuration = 0;
    private static long sMaxFileSize = 0;
    private static MediaRecorderEx sMediaRecorder = null;
    private static int sOrientationHint = 0;
    private static Surface[] sPersistentSurface = new Surface[2];
    private static ConditionVariable sReadyStartRecording = new ConditionVariable();
    private static MediaRecorderEx sSecondMediaRecorder = null;
    private static int sStorageType = 0;
    public static Object sSynchRecordStop = new Object();
    public static String sUUID;

    public static void setWaitStartRecoding(boolean isWait) {
        CamLog.m3d(CameraConstants.TAG, "set wait : " + isWait);
        if (isWait) {
            sReadyStartRecording.block();
        } else {
            sReadyStartRecording.open();
        }
    }

    public static void releasePersistentSurface() {
        for (int i = 0; i < sPersistentSurface.length; i++) {
            if (sPersistentSurface[i] != null) {
                CamLog.m7i(CameraConstants.TAG, "[persist] release surface");
                CamLog.m7i(CameraConstants.TAG, "[persist] release surface - index : " + i + ", surface : " + sPersistentSurface[i]);
                sPersistentSurface[i].release();
                sPersistentSurface[i] = null;
            }
        }
    }

    public static void resetWaitStartRecoding() {
        CamLog.m3d(CameraConstants.TAG, "reset wait");
        sReadyStartRecording.close();
    }

    public static boolean isInitialized() {
        return sInit;
    }

    public static boolean isRecording() {
        return sIsRecording;
    }

    public static boolean setErrorListener(OnErrorListener l) {
        if (sMediaRecorder == null) {
            return false;
        }
        sMediaRecorder.setOnErrorListener(l);
        return true;
    }

    public static boolean setInfoListener(OnInfoListener l) {
        if (sMediaRecorder == null) {
            return false;
        }
        sMediaRecorder.setOnInfoListener(l);
        return true;
    }

    public static boolean switchCamera(int cameraId, String paramToSet) {
        if (sMediaRecorder == null) {
            return false;
        }
        try {
            Class.forName("com.lge.media.MediaRecorderEx").getDeclaredMethod("switchCamera", new Class[]{Integer.TYPE, String.class}).invoke(sMediaRecorder, new Object[]{Integer.valueOf(cameraId), paramToSet});
            CamLog.m3d(CameraConstants.TAG, "switchCamera invoke success");
            return true;
        } catch (NoSuchMethodError e) {
            CamLog.m3d(CameraConstants.TAG, "NoSuchMethodError! - switchCamera");
            e.printStackTrace();
            return false;
        } catch (Exception e2) {
            CamLog.m3d(CameraConstants.TAG, "Exception occured during switching the camera on recording");
            e2.printStackTrace();
            return false;
        }
    }

    public static boolean setCameraSwitch(CameraProxy cameraDevice) {
        return false;
    }

    public static boolean setMaxFileSize(long maxFileSize, long freeSpace, int storage) {
        sMaxFileSize = maxFileSize;
        if (sMaxFileSize == 0) {
            sMaxFileSize = Math.min(freeSpace - CameraConstants.VIDEO_LOW_STORAGE_THRESHOLD, CameraConstants.MEDIA_RECORDING_MAX_LIMIT);
            if (sMaxFileSize < 0) {
                sMaxFileSize = CameraConstants.VIDEO_LOW_STORAGE_THRESHOLD;
                CamLog.m3d(CameraConstants.TAG, "mMaxFileSize: " + sMaxFileSize + " freeSpace: " + freeSpace + " storage: " + storage);
                return false;
            }
        } else if (freeSpace <= sMaxFileSize) {
            sMaxFileSize = freeSpace;
        }
        sStorageType = storage;
        CamLog.m3d(CameraConstants.TAG, "mMaxFileSize: " + sMaxFileSize + " freeSpace: " + freeSpace + " storage: " + storage);
        return true;
    }

    public static void setMaxDuration(int maxDur) {
        if (maxDur <= 0) {
            maxDur = CameraConstants.DEFAULT_DURATION;
        }
        sMaxDuration = maxDur;
    }

    public static MediaRecorderEx init(Camera cameraDevice, int cameraId, String videoSize, Location loc, int purpose, double fps, int bitrate, String type, boolean inAndOutRecording, int videoFlipType) {
        return init(true, cameraDevice, cameraId, videoSize, loc, purpose, fps, bitrate, type, inAndOutRecording, 0, videoFlipType);
    }

    public static MediaRecorderEx init(boolean isFirstRecorder, Camera cameraDevice, int cameraId, String videoSize, Location loc, int purpose, double fps, int bitrate, String type, boolean inAndOutRecording, int videoFliptype) {
        return init(isFirstRecorder, cameraDevice, cameraId, videoSize, loc, purpose, fps, bitrate, type, inAndOutRecording, 0, videoFliptype);
    }

    public static MediaRecorderEx init(boolean isFirstRecorder, Camera cameraDevice, int cameraId, String videoSize, Location loc, int purpose, double fps, int bitrate, String type, boolean inAndOutRecording, int cameraIndex, int videoFlipType) {
        MediaRecorderEx mediaRecorder = isFirstRecorder ? sMediaRecorder : sSecondMediaRecorder;
        CamLog.m3d(CameraConstants.TAG, "-rec- Camcorder(MediaRecorder) init()-start");
        CamLog.m3d(CameraConstants.TAG, "-rec- id = " + cameraId + " videoSize = " + videoSize + " purpose = " + purpose + " fps = " + fps + " bitrate = " + bitrate + ", cameraIndex " + cameraIndex + ", videoFlipType : " + videoFlipType);
        if (!(sInit && isFirstRecorder)) {
            int iVideoBitrate;
            if (mediaRecorder == null) {
                CamLog.m7i(CameraConstants.TAG, "Camcorder new MediaRecorder()");
                mediaRecorder = new MediaRecorderEx();
            }
            if ((purpose & 2) == 0) {
                if (FunctionProperties.getSupportedHal() != 2) {
                    if (cameraDevice == null) {
                        return null;
                    }
                    try {
                        cameraDevice.unlock();
                    } catch (RuntimeException e) {
                        CamLog.m11w(CameraConstants.TAG, "exception : " + e);
                    }
                    mediaRecorder.setCamera(cameraDevice);
                }
                int videoSource = inAndOutRecording ? 3 : FunctionProperties.getSupportedHal() == 2 ? 2 : 1;
                mediaRecorder.setVideoSource(videoSource);
            } else {
                mediaRecorder.setVideoSource(2);
            }
            mediaRecorder.setAudioSource(5);
            mediaRecorder.setOutputFormat((purpose & 4) != 0 ? 1 : 2);
            if (loc != null) {
                mediaRecorder.setLocation((float) loc.getLatitude(), (float) loc.getLongitude());
            }
            int[] size = null;
            boolean isHFR = false;
            if (videoSize != null) {
                if (fps > 30.0d) {
                    isHFR = true;
                }
                size = Utils.sizeStringToArray(videoSize);
                mediaRecorder.setVideoSize(size[0], size[1]);
                CamLog.m7i(CameraConstants.TAG, String.format("setVideoSize width = %d , hegiht = %d", new Object[]{Integer.valueOf(size[0]), Integer.valueOf(size[1])}));
            } else {
                CamLog.m3d(CameraConstants.TAG, "error!! videoSize is null");
            }
            int quality = MultimediaProperties.getProfileQulity(cameraId, size, isHFR, type, fps == 240.0d);
            if (bitrate == 0) {
                bitrate = MultimediaProperties.getBitrate(cameraId, quality);
            }
            if ("1920x1080".equalsIgnoreCase(videoSize) && Double.compare(60.0d, fps) == 0) {
                bitrate = CameraConstants.VALUE_VIDEO_FHD_60F;
            }
            if ((purpose & 4) != 0) {
                iVideoBitrate = CameraConstants.VALUE_VIDEO_BITRATE_MMS;
            } else {
                iVideoBitrate = bitrate;
            }
            CamLog.m7i(CameraConstants.TAG, "VideoRecorder-Init : framerate = " + fps + ", Profile bitrate = " + iVideoBitrate);
            mediaRecorder.setVideoEncodingBitRate(iVideoBitrate);
            if (Double.compare(fps, 0.5d) == 0 || Double.compare(fps, 1.0d) == 0 || Double.compare(fps, 2.0d) == 0 || Double.compare(fps, 3.0d) == 0) {
                mediaRecorder.setCaptureRate(fps);
                mediaRecorder.setVideoFrameRate(30);
                CamLog.m3d(CameraConstants.TAG, "set videoframe rate 30");
            } else {
                mediaRecorder.setVideoFrameRate((int) fps);
            }
            if ((purpose & 4) != 0) {
                mediaRecorder.setAudioEncoder(MultimediaProperties.getMmsAudioEncodingType());
                if (MultimediaProperties.getMmsAudioEncodingType() == 3) {
                    mediaRecorder.setAudioEncodingBitRate(48000);
                    mediaRecorder.setAudioChannels(1);
                    mediaRecorder.setAudioSamplingRate(8000);
                }
            } else {
                boolean z;
                mediaRecorder.setAudioEncoder((purpose & 8) == 0 ? 3 : 12);
                mediaRecorder.setAudioChannels(2);
                mediaRecorder.setAudioSamplingRate(48000);
                mediaRecorder.setAudioEncodingBitRate(156000);
                if ((purpose & 8) != 0) {
                    z = true;
                } else {
                    z = false;
                }
                setAudioSetting(mediaRecorder, z);
            }
            int encoder = MultimediaProperties.getVideoEncodingType(purpose);
            CamLog.m3d(CameraConstants.TAG, "set encodingType as " + encoder);
            mediaRecorder.setVideoEncoder(encoder);
            mediaRecorder.setMaxFileSize(sMaxFileSize);
            mediaRecorder.setMaxDuration(sMaxDuration);
            mediaRecorder.setOrientationHint(sOrientationHint);
            if ((purpose & 16) != 0) {
                mediaRecorder.setVideoEncodingProfileLevel(4096, 1);
            }
            setVideoFlip(mediaRecorder, videoFlipType);
            String filePath = sFilePath;
            if (!(getLoopState() == 1 || isFirstRecorder)) {
                filePath = sFilePathForLoopRecording;
            }
            if (filePath == null) {
                CamLog.m3d(CameraConstants.TAG, "File path is null.");
                return null;
            }
            CamLog.m3d(CameraConstants.TAG, "file path : " + filePath);
            mediaRecorder.setOutputFile(filePath);
            setRecordingType(mediaRecorder, type);
            try {
                CamLog.m7i(CameraConstants.TAG, "Media recorder initializing start.");
                if (FunctionProperties.getSupportedHal() == 2 && (purpose & 2) == 0) {
                    if (sPersistentSurface[cameraIndex] == null) {
                        sPersistentSurface[cameraIndex] = MediaCodec.createPersistentInputSurface();
                        CamLog.m7i(CameraConstants.TAG, "[persist] create new persistent surface");
                    }
                    CamLog.m7i(CameraConstants.TAG, "[persist] camera index : " + cameraIndex + ", surface : " + sPersistentSurface[cameraIndex]);
                    mediaRecorder.setInputSurface(sPersistentSurface[cameraIndex]);
                }
                mediaRecorder.prepare();
                CamLog.m7i(CameraConstants.TAG, "Media recorder initialized.");
                sInit = true;
                CamLog.m3d(CameraConstants.TAG, "VideoRecorder sInit : " + sInit);
                CamLog.m7i(CameraConstants.TAG, "RECORDER_INIT_DONE");
            } catch (IllegalStateException e2) {
                CamLog.m6e(CameraConstants.TAG, "IllegalStateException in init recorder prepare : ", e2);
                release(mediaRecorder);
                mediaRecorder = null;
            } catch (IOException e3) {
                CamLog.m6e(CameraConstants.TAG, "recorder prepare error: ", e3);
                release(mediaRecorder);
                mediaRecorder = null;
            }
        }
        if (isFirstRecorder) {
            sMediaRecorder = mediaRecorder;
        } else {
            sSecondMediaRecorder = mediaRecorder;
        }
        CamLog.m3d(CameraConstants.TAG, "Camcorder(MediaRecorder) init()-end");
        return mediaRecorder;
    }

    public static void setNextOutputFile(File nextFile) {
        if (sMediaRecorder != null) {
            try {
                sMediaRecorder.setNextOutputFile(nextFile);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalStateException e2) {
                e2.printStackTrace();
            }
        }
    }

    public static void reInitMediaRecorder() {
        if (sMediaRecorder != null) {
            sInit = true;
            CamLog.m3d(CameraConstants.TAG, "VideoRecorder sInit : " + sInit);
        }
    }

    public static void changeMediaRecorder() {
        MediaRecorderEx temp = sMediaRecorder;
        sMediaRecorder = sSecondMediaRecorder;
        sSecondMediaRecorder = temp;
    }

    public static void releaseSecondMediaRecorder() {
        if (sSecondMediaRecorder != null) {
            sSecondMediaRecorder.release();
            sSecondMediaRecorder = null;
        }
    }

    public static Surface createPersistentSurface(String videoSize, int cameraIndex, String mimeType) {
        if (mimeType == null) {
            mimeType = "video/avc";
        }
        int[] size = Utils.sizeStringToArray(videoSize);
        CamLog.m7i(CameraConstants.TAG, "persistentSurface size : " + videoSize + ", surfaceIndex : " + cameraIndex + " , mimeType : " + mimeType);
        try {
            MediaFormat format = MediaFormat.createVideoFormat(mimeType, size[0], size[1]);
            format.setInteger(LdbConstants.LDB_FEAT_NAME_VIDEO_BITRATE, 125000);
            format.setInteger("frame-rate", 30);
            format.setInteger("max-input-size", 0);
            format.setInteger("i-frame-interval", 1);
            format.setInteger("color-format", 2130708361);
            if (mimeType == "video/hevc") {
                format.setInteger("profile", 4096);
                format.setInteger("level", 1);
            }
            MediaCodec codec = MediaCodec.createEncoderByType(mimeType);
            if (sPersistentSurface[cameraIndex] == null) {
                sPersistentSurface[cameraIndex] = MediaCodec.createPersistentInputSurface();
                CamLog.m7i(CameraConstants.TAG, "[persist] create new persistent surface");
                CamLog.m7i(CameraConstants.TAG, "[persist] camera index : " + cameraIndex + ", surface : " + sPersistentSurface[cameraIndex]);
            }
            codec.configure(format, null, null, 1);
            codec.setInputSurface(sPersistentSurface[cameraIndex]);
            codec.start();
            codec.stop();
            codec.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sPersistentSurface[cameraIndex];
    }

    public static Surface getSurface(boolean isOpenGLRecording, int cameraIndex) {
        if (FunctionProperties.getSupportedHal() == 2 && !isOpenGLRecording) {
            CamLog.m7i(CameraConstants.TAG, "[persist] recording camera index : " + cameraIndex + ", surface : " + sPersistentSurface[cameraIndex]);
            return sPersistentSurface[cameraIndex];
        } else if (sMediaRecorder == null || (!sInit && sSecondMediaRecorder == null)) {
            return null;
        } else {
            return sMediaRecorder.getSurface();
        }
    }

    public static void setFileName(String fileName) {
        sFileName = fileName;
    }

    public static String getFileName() {
        return sFileName;
    }

    public static void setFilePath(String filePath) {
        sFilePath = filePath;
    }

    public static String getFilePath() {
        return sFilePath;
    }

    public static boolean start() {
        CamLog.m3d(CameraConstants.TAG, "Camcorder start()-start");
        if (sMediaRecorder == null) {
            CamLog.m3d(CameraConstants.TAG, "sMediaRecorder is null");
            sInit = false;
            CamLog.m3d(CameraConstants.TAG, "VideoRecorder sInit : " + sInit);
            sIsRecording = false;
            return false;
        }
        if (sInit) {
            try {
                CamLog.m3d(CameraConstants.TAG, "##### video recording start - mMediaRecorder.start()");
                sMediaRecorder.start();
                sLoopRecordStartTime = SystemClock.uptimeMillis();
                sIsRecording = true;
            } catch (IllegalStateException e) {
                e.printStackTrace();
                CamLog.m5e(CameraConstants.TAG, "error recording start");
                clearEmptyVideoFile(sFilePath, true);
                sMediaRecorder.reset();
                sMediaRecorder.release();
                sMediaRecorder = null;
                sInit = false;
                CamLog.m3d(CameraConstants.TAG, "VideoRecorder sInit : " + sInit);
                sIsRecording = false;
            } catch (RuntimeException e2) {
                e2.printStackTrace();
                CamLog.m5e(CameraConstants.TAG, "RunTimeException recording start");
                clearEmptyVideoFile(sFilePath, true);
                sMediaRecorder.reset();
                sMediaRecorder.release();
                sMediaRecorder = null;
                sInit = false;
                CamLog.m3d(CameraConstants.TAG, "VideoRecorder sInit : " + sInit);
                sIsRecording = false;
            }
        }
        Log.d(CameraConstants.TAG, "TIME CHECK : Camcorder start()-end, return " + sIsRecording);
        return sIsRecording;
    }

    public static boolean stop() throws Exception {
        boolean z;
        CamLog.m3d(CameraConstants.TAG, "Camcorder stop()-start");
        synchronized (sSynchRecordStop) {
            if (sIsRecording) {
                CamLog.m3d(CameraConstants.TAG, "##### video recording stop - mMediaRecorder.stop()");
                try {
                    sMediaRecorder.stop();
                    sIsRecording = false;
                    sMediaRecorder.reset();
                    sInit = false;
                    CamLog.m3d(CameraConstants.TAG, "VideoRecorder sInit : " + sInit);
                    clearEmptyVideoFile(sFilePath, false);
                } catch (Exception e) {
                    CamLog.m3d(CameraConstants.TAG, "##### mediaRecorder stop Exception : " + e);
                    throw e;
                } catch (Throwable th) {
                    sIsRecording = false;
                    sMediaRecorder.reset();
                    sInit = false;
                    CamLog.m3d(CameraConstants.TAG, "VideoRecorder sInit : " + sInit);
                }
            }
        }
        String str = CameraConstants.TAG;
        StringBuilder append = new StringBuilder().append("Camcorder stop()-end, return ");
        if (sIsRecording) {
            z = false;
        } else {
            z = true;
        }
        CamLog.m3d(str, append.append(z).toString());
        if (sIsRecording) {
            return false;
        }
        return true;
    }

    public static void pause() {
        CamLog.m3d(CameraConstants.TAG, "Camcorder pause()-start");
        if (MultimediaProperties.isSupportedPauseAndResume()) {
            if (sInit) {
                try {
                    CamLog.m3d(CameraConstants.TAG, "##### video recording start - mMediaRecorder.pause()");
                    sMediaRecorder.pause();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    CamLog.m5e(CameraConstants.TAG, "error recording pause");
                    clearEmptyVideoFile(sFilePath, false);
                    clearEmptyVideoFile(sFilePathForLoopRecording, false);
                    sMediaRecorder.reset();
                    sMediaRecorder.release();
                }
            }
            CamLog.m3d(CameraConstants.TAG, "Camcorder pause()-end.");
            return;
        }
        CamLog.m7i(CameraConstants.TAG, "Model not supported pause and resume.");
    }

    public static void resume() {
        CamLog.m3d(CameraConstants.TAG, "Camcorder resume()-start");
        if (MultimediaProperties.isSupportedPauseAndResume()) {
            if (sInit) {
                try {
                    CamLog.m3d(CameraConstants.TAG, "##### video recording resume - mMediaRecorder.resume()");
                    sMediaRecorder.resume();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    CamLog.m5e(CameraConstants.TAG, "error recording pause");
                    clearEmptyVideoFile(sFilePath, false);
                    clearEmptyVideoFile(sFilePathForLoopRecording, false);
                    sMediaRecorder.reset();
                    sMediaRecorder.release();
                }
            }
            CamLog.m3d(CameraConstants.TAG, "Camcorder resume()-end.");
            return;
        }
        CamLog.m7i(CameraConstants.TAG, "Model not supported pause and resume.");
    }

    public static synchronized void release(MediaRecorderEx mediaRecorder) {
        synchronized (VideoRecorder.class) {
            CamLog.m3d(CameraConstants.TAG, "Camcorder release()-start");
            if (mediaRecorder == null) {
                mediaRecorder = sMediaRecorder;
            }
            if (mediaRecorder != null) {
                if (sIsRecording) {
                    try {
                        stop();
                    } catch (Exception e) {
                        CamLog.m3d(CameraConstants.TAG, "[VideoRecorder::release()] stop Exception !!");
                        e.printStackTrace();
                        clearEmptyVideoFile(sFilePath, false);
                        clearEmptyVideoFile(sFilePathForLoopRecording, false);
                    }
                } else {
                    clearEmptyVideoFile(sFilePath, false);
                }
                mediaRecorder.setOnInfoListener(null);
                mediaRecorder.setOnErrorListener(null);
                CamLog.m3d(CameraConstants.TAG, "Release MediaRecorder start");
                mediaRecorder.reset();
                mediaRecorder.release();
                sMediaRecorder = null;
                CamLog.m3d(CameraConstants.TAG, "Release MediaRecorder end");
                CamLog.m3d(CameraConstants.TAG, "UNLOCK CAMERA");
            }
            sInit = false;
            CamLog.m3d(CameraConstants.TAG, "VideoRecorder sInit : " + sInit);
            CamLog.m3d(CameraConstants.TAG, "Camcorder release()-end");
        }
        return;
    }

    public static void setOrientationHint(int degree) {
        CamLog.m7i(CameraConstants.TAG, "setOrientationHint : " + degree);
        if (degree < 0) {
            degree = 0;
        }
        sOrientationHint = degree;
    }

    public static long getMaxFileSize() {
        return sMaxFileSize;
    }

    public static int getMaxDuration() {
        return sMaxDuration;
    }

    public static int getStorageType() {
        return sStorageType;
    }

    public static void clearEmptyVideoFile(String filePath, boolean isForced) {
        CamLog.m3d(CameraConstants.TAG, "clearEmptyVideoFile() " + filePath);
        if (filePath != null) {
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    return;
                }
                if (file.length() != 0 && !isForced) {
                    CamLog.m3d(CameraConstants.TAG, "File is not empty: " + filePath);
                } else if (file.delete()) {
                    CamLog.m3d(CameraConstants.TAG, "Empty file deleted: " + filePath);
                } else {
                    CamLog.m3d(CameraConstants.TAG, "Empty file delete failed: " + filePath);
                }
            } catch (Exception e) {
                CamLog.m4d(CameraConstants.TAG, "clearEmptyVideoFile : ", e);
            }
        }
    }

    public static void setAudiozoomMetadata() {
        if (sMediaRecorder != null) {
            try {
                sMediaRecorder.setAudioZooming();
            } catch (IllegalStateException e) {
                CamLog.m3d(CameraConstants.TAG, "setAudioZooming error" + e);
            }
        }
    }

    public static void changeMaxFileSize(long size) {
        CamLog.m3d(CameraConstants.TAG, "changeMaxFileSize START");
        synchronized (sSynchRecordStop) {
            if (sMediaRecorder != null && sIsRecording) {
                try {
                    CamLog.m3d(CameraConstants.TAG, "changeMaxFileSize DO subSize : " + size);
                    sMediaRecorder.changeMaxFileSize(size);
                } catch (NoSuchMethodError e) {
                    CamLog.m3d(CameraConstants.TAG, "Catch Exception : " + e);
                } catch (IllegalStateException e2) {
                    CamLog.m3d(CameraConstants.TAG, "Catch IllegalStateException " + e2);
                }
            }
            CamLog.m3d(CameraConstants.TAG, "changeMaxFileSize END");
        }
        return;
    }

    public static int getMaxAudioAmplitude() {
        if (sMediaRecorder == null) {
            return 0;
        }
        return sMediaRecorder.getMaxAmplitude();
    }

    public static int[] getLevelMeter() {
        int gain = getMaxAudioAmplitude();
        int leftLevel = (gain >> 8) & 255;
        int rightLevel = gain & 255;
        if (leftLevel > 127) {
            leftLevel = 127;
        }
        if (rightLevel > 127) {
            rightLevel = 127;
        }
        return new int[]{leftLevel, rightLevel};
    }

    public static void createMediaRecorderEx() {
        if (sMediaRecorder == null) {
            CamLog.m3d(CameraConstants.TAG, "[Audio] create MediaRecorderEx for preview loopback : ");
            sMediaRecorder = new MediaRecorderEx();
        }
    }

    public static void setLoopback(int state) {
        if (sMediaRecorder != null) {
            try {
                CamLog.m3d(CameraConstants.TAG, "[Audio] setLoopback : " + state);
                sMediaRecorder.getClass().getMethod("setLoopback", new Class[]{Integer.TYPE}).invoke(sMediaRecorder, new Object[]{Integer.valueOf(state)});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setRecordingType(MediaRecorderEx mediaRecorder, String type) {
        if (!String.valueOf(0).equals(type) && mediaRecorder != null) {
            CamLog.m3d(CameraConstants.TAG, "setRecordingType : " + type);
            try {
                Class.forName("com.lge.media.MediaRecorderEx").getDeclaredMethod("setRecordingType", new Class[]{String.class}).invoke(mediaRecorder, new Object[]{type});
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "setRecordingType invoke error : ", e);
            }
        }
    }

    public static long getLoopRecordingDuration() {
        return SystemClock.uptimeMillis() - sLoopRecordStartTime;
    }

    public static void makeNewUUID() {
        sUUID = UUID.randomUUID().toString();
    }

    public static String getUUID() {
        return sUUID;
    }

    public static void setUUID() {
        if (sUUID == null) {
            CamLog.m3d(CameraConstants.TAG, "UUID is null");
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "setUUID START , UUID = " + sUUID);
        if (sMediaRecorder != null) {
            try {
                Class.forName("com.lge.media.MediaRecorderEx").getDeclaredMethod("setUUID", new Class[]{String.class}).invoke(sMediaRecorder, new Object[]{sUUID});
                CamLog.m3d(CameraConstants.TAG, "setUUID invoke sucess");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        CamLog.m3d(CameraConstants.TAG, "setUUID END");
    }

    public static void setLoopState(int state) {
        CamLog.m3d(CameraConstants.TAG, "setLoopState : " + state);
        sLoopState = state;
    }

    public static int getLoopState() {
        return sLoopState;
    }

    public static void setAudioSetting(MediaRecorderEx mediaRecorder, boolean isHifiOn) {
        if (FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO)) {
            if (mediaRecorder == null) {
                mediaRecorder = sMediaRecorder;
                if (mediaRecorder == null) {
                    return;
                }
            }
            try {
                CamLog.m3d(CameraConstants.TAG, "[Audio] setAudioSettingForHifi : " + isHifiOn);
                mediaRecorder.setAudioChannels(2);
                mediaRecorder.setAudioSamplingRate(48000);
                mediaRecorder.setAudioEncodingBitRate(156000);
                mediaRecorder.setParameter("audio-param-bit-per-sample=" + (isHifiOn ? CameraConstants.FPS_24 : "16"));
            } catch (RuntimeException e) {
                CamLog.m6e(CameraConstants.TAG, "setAudioSettingForHifi : RuntimeException : ", e);
            }
        }
    }

    public static void setVideoFlip(MediaRecorderEx mediaRecorder, int videoFlipType) {
        if (FunctionProperties.getSupportedHal() == 2) {
            if (mediaRecorder == null) {
                mediaRecorder = sMediaRecorder;
                if (mediaRecorder == null) {
                    return;
                }
            }
            try {
                CamLog.m7i(CameraConstants.TAG, "video flipType : " + videoFlipType);
                mediaRecorder.setParameter("video-record-mirror=" + videoFlipType);
            } catch (RuntimeException e) {
                CamLog.m6e(CameraConstants.TAG, "setVideoFlip : RuntimeException : ", e);
            }
        }
    }

    public static void setLoopRecordFilePath(String path) {
        CamLog.m3d(CameraConstants.TAG, "setLoopRecordFilePath : " + path);
        sFilePathForLoopRecording = path;
    }
}
