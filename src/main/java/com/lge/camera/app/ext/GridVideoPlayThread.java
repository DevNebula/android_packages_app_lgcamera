package com.lge.camera.app.ext;

import android.view.Surface;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.io.File;

public class GridVideoPlayThread extends Thread {
    private final File mFile;
    private final Surface mSurface;
    private VideoPlayer mVideoPlayer;
    private final VideoPlayerCallback mVideoPlayerCallback;

    public GridVideoPlayThread(File file, Surface surface, VideoPlayerCallback playerCallback) {
        this.mFile = file;
        this.mSurface = surface;
        this.mVideoPlayerCallback = playerCallback;
        start();
    }

    public void run() {
        try {
            this.mVideoPlayer = new VideoPlayer(this.mFile, this.mSurface, this.mVideoPlayerCallback);
            CamLog.m3d(CameraConstants.TAG, "starting VideoPlayer: " + this.mFile);
            this.mVideoPlayer.play();
            CamLog.m3d(CameraConstants.TAG, "stopping VideoPlayer: " + this.mFile);
        } catch (Exception e) {
            CamLog.m6e(CameraConstants.TAG, "video playback failed", e);
        } finally {
            this.mSurface.release();
            CamLog.m3d(CameraConstants.TAG, "VideoPlayThread stopping");
        }
    }
}
