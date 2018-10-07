package com.lge.camera.managers;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorConverter;
import com.lge.camera.util.MiniViewUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import java.util.Timer;
import java.util.TimerTask;

public class LivePhotoScreenFrameManager extends LivePhotoManagerBase {
    private int mFrameWidth = CameraConstantsEx.HD_SCREEN_RESOLUTION;
    private boolean mLivePhotoPaused = false;
    private Handler mScreenFrameHandler;
    private Timer mTimer;

    /* renamed from: com.lge.camera.managers.LivePhotoScreenFrameManager$1 */
    class C10481 extends TimerTask {

        /* renamed from: com.lge.camera.managers.LivePhotoScreenFrameManager$1$1 */
        class C10471 implements Runnable {
            C10471() {
            }

            public void run() {
                if (LivePhotoScreenFrameManager.this.checkFrameCondition()) {
                    LivePhotoScreenFrameManager.this.mLastPrevCallbackTime = System.currentTimeMillis();
                    Bitmap screenShot = LivePhotoScreenFrameManager.this.getScreenShot();
                    if (screenShot != null) {
                        LivePhotoScreenFrameManager.this.onScreenFrame(screenShot);
                    }
                }
            }
        }

        C10481() {
        }

        public void run() {
            if (LivePhotoScreenFrameManager.this.mScreenFrameHandler != null) {
                LivePhotoScreenFrameManager.this.mScreenFrameHandler.post(new C10471());
            }
        }
    }

    public LivePhotoScreenFrameManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public boolean enableLivePhoto() {
        if (!super.enableLivePhoto()) {
            return false;
        }
        HandlerThread thread = new HandlerThread("LivePhotoScreenFrameHandler");
        thread.start();
        this.mScreenFrameHandler = new Handler(thread.getLooper());
        startScreenCaptureTimer();
        return true;
    }

    protected void initFrameSize() {
        this.mFrameSize = getLivePhotoFrameSize();
        CamLog.m3d(CameraConstants.TAG, "frameSize size[0] = " + this.mFrameSize[0] + ", size[1] = " + this.mFrameSize[1]);
    }

    public void disableLivePhoto() {
        super.disableLivePhoto();
        stopScreenCaptureTimer();
        if (this.mScreenFrameHandler != null) {
            this.mScreenFrameHandler.getLooper().quitSafely();
        }
    }

    private void startScreenCaptureTimer() {
        TimerTask timerTask = new C10481();
        if (this.mTimer != null) {
            stopScreenCaptureTimer();
        }
        this.mTimer = new Timer();
        this.mTimer.scheduleAtFixedRate(timerTask, 1000, (long) 22);
    }

    private Bitmap getScreenShot() {
        Rect previewRect = getPreviewRect();
        if (previewRect == null) {
            return null;
        }
        Bitmap screenShot;
        if (MiniViewUtil.isMiniViewState()) {
            int[] resizedLcdSize = new int[]{(int) ((((float) this.mFrameWidth) * ((float) lcdSize[0])) / ((float) Utils.getLCDsize(getAppContext(), true)[1])), this.mFrameWidth};
            int resizedTopMargin = (int) ((((float) previewRect.top) * ((float) resizedLcdSize[0])) / ((float) Utils.getLCDsize(getAppContext(), true)[0]));
            screenShot = Utils.getScreenShot(resizedLcdSize[1], resizedLcdSize[0], true, new Rect(0, resizedTopMargin, this.mFrameSize[0], this.mFrameSize[1] + resizedTopMargin), true);
        } else {
            screenShot = Utils.getScreenShot(this.mFrameSize[0], this.mFrameSize[1], true, previewRect, true);
        }
        return screenShot;
    }

    private boolean checkFrameCondition() {
        if (System.currentTimeMillis() - this.mLastPrevCallbackTime >= 50 && !this.mGet.isPaused() && !this.mLivePhotoPaused && this.mIsLivePhotoEnabled) {
            return true;
        }
        return false;
    }

    private void stopScreenCaptureTimer() {
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer = null;
        }
    }

    public void onScreenFrame() {
        if (this.mIsLivePhotoEnabled && !this.mGet.isPaused() && !this.mLivePhotoPaused && System.currentTimeMillis() - this.mLastPrevCallbackTime >= 66) {
            Rect rect = getPreviewRect();
            if (rect != null) {
                Bitmap screenShot = Utils.getScreenShot(this.mFrameSize[0], this.mFrameSize[1], true, rect);
                if (screenShot != null) {
                    int width = screenShot.getWidth();
                    int height = screenShot.getHeight();
                    int[] rgb = new int[(width * height)];
                    byte[] yuv = new byte[(((width * height) * 3) / 2)];
                    screenShot.getPixels(rgb, 0, width, 0, 0, width, height);
                    ColorConverter.RGBToYuv420sp(rgb, yuv, width, height);
                    if ("on".equals(this.mGet.getSettingValue(Setting.KEY_SAVE_DIRECTION))) {
                        byte[] flipYuv = new byte[yuv.length];
                        ColorConverter.flipYuvImage(yuv, flipYuv, width, height);
                        yuv = flipYuv;
                    }
                    if (this.mPrev2SecBuffer != null) {
                        synchronized (this.mLinkedListLock) {
                            if (this.mPrev2SecBuffer.size() >= 30) {
                                this.mPrev2SecBuffer.removeFirst();
                            }
                            this.mPrev2SecBuffer.add(yuv);
                        }
                        if (this.mLivePhotoList != null) {
                            for (int i = 0; i < this.mLivePhotoList.size(); i++) {
                                LivePhoto livePhoto = (LivePhoto) this.mLivePhotoList.get(i);
                                if (livePhoto.put(yuv)) {
                                    livePhoto.save();
                                    this.mLivePhotoList.remove(i);
                                }
                            }
                            this.mLastPrevCallbackTime = System.currentTimeMillis();
                        }
                    }
                }
            }
        }
    }

    public void onScreenFrame(Bitmap screenShot) {
        if (screenShot != null && !screenShot.isRecycled()) {
            int width = screenShot.getWidth();
            int height = screenShot.getHeight();
            int[] rgb = new int[(width * height)];
            byte[] yuv = new byte[(((width * height) * 3) / 2)];
            screenShot.getPixels(rgb, 0, width, 0, 0, width, height);
            ColorConverter.RGBToYuv420sp(rgb, yuv, width, height);
            if ("on".equals(this.mGet.getSettingValue(Setting.KEY_SAVE_DIRECTION))) {
                byte[] flip = new byte[yuv.length];
                ColorConverter.flipYuvImage(yuv, flip, width, height);
                yuv = flip;
            }
            if (this.mPrev2SecBuffer != null) {
                synchronized (this.mLinkedListLock) {
                    if (this.mPrev2SecBuffer.size() >= 30) {
                        this.mPrev2SecBuffer.removeFirst();
                    }
                    this.mPrev2SecBuffer.add(yuv);
                }
                if (this.mLivePhotoList != null) {
                    for (int i = 0; i < this.mLivePhotoList.size(); i++) {
                        LivePhoto livePhoto = (LivePhoto) this.mLivePhotoList.get(i);
                        if (livePhoto.put(yuv)) {
                            livePhoto.save();
                            this.mLivePhotoList.remove(i);
                        }
                    }
                }
            }
        }
    }

    private int[] getLivePhotoFrameSize() {
        int[] screenSize = getPreviewScreenSize();
        if (screenSize == null) {
            return new int[]{0, 0};
        }
        if (screenSize[0] <= this.mFrameWidth) {
            return screenSize;
        }
        int height = (int) (((float) (screenSize[1] * this.mFrameWidth)) / ((float) screenSize[0]));
        return new int[]{this.mFrameWidth, height};
    }

    private Rect getPreviewRect() {
        int[] screenSize = getPreviewScreenSize();
        if (screenSize == null) {
            return null;
        }
        int topMargin;
        Rect previewRect = new Rect();
        if (ModelProperties.isLongLCDModel()) {
            topMargin = RatioCalcUtil.getLongLCDModelTopMargin(this.mGet.getAppContext(), screenSize[0], screenSize[1], 0);
            if ("on".equals(this.mGet.getSettingValue(Setting.KEY_TILE_PREVIEW))) {
                topMargin = RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext());
            }
        } else {
            topMargin = RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext());
        }
        previewRect.set(0, topMargin, Math.min(screenSize[0], screenSize[1]), topMargin + Math.max(screenSize[0], screenSize[1]));
        return previewRect;
    }

    private int[] getPreviewScreenSize() {
        ListPreference listPref = (ListPreference) this.mGet.getListPreference("picture-size");
        if (listPref == null) {
            return null;
        }
        int[] screenSize = Utils.sizeStringToArray(listPref.getExtraInfo(2));
        int shortWidth = Math.min(screenSize[0], screenSize[1]);
        int longHeight = Math.max(screenSize[0], screenSize[1]);
        return new int[]{shortWidth, longHeight};
    }

    protected int getVideoDegree(int exifDegree, int deviceDegree) {
        int degree = deviceDegree;
        if ("on".equals(this.mGet.getSettingValue(Setting.KEY_SAVE_DIRECTION))) {
            degree = (360 - degree) % 360;
        }
        CamLog.m3d(CameraConstants.TAG, "-Live photo- video degree = " + degree);
        return degree;
    }

    public void pause() {
        this.mLivePhotoPaused = true;
    }

    public void resume() {
        this.mLivePhotoPaused = false;
    }
}
