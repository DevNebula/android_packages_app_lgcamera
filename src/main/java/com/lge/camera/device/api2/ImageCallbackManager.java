package com.lge.camera.device.api2;

import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.ICameraCallback.CameraImageCallback;
import com.lge.camera.util.CamLog;
import java.util.concurrent.locks.ReentrantLock;

public class ImageCallbackManager {
    private static final int PREVIEW_CB_HDR_ADD = 2;
    private static final int PREVIEW_CB_NORMAL_ADD = 1;
    private CameraImageCallback mFullImageCallback;
    private int mPreviewCallbackAdd = 0;
    private Image mPreviewCallbackImage = null;
    private CameraImageCallback mPreviewImageCallback;
    private final ReentrantLock mPreviewImageLock = new ReentrantLock();

    public void setPreviewImageCallback(final CameraImageCallback previewCb, Handler handler, boolean isWaitDone) {
        if (handler == null) {
            this.mPreviewImageCallback = previewCb;
        } else {
            handler.post(new Runnable() {
                public void run() {
                    ImageCallbackManager.this.mPreviewImageCallback = previewCb;
                }
            });
            if (isWaitDone) {
                CamLog.m9v(CameraConstants.TAG, "waiting");
                waitDone(handler);
            }
        }
        this.mPreviewCallbackAdd = previewCb != null ? this.mPreviewCallbackAdd | 1 : this.mPreviewCallbackAdd & -2;
    }

    public void enalbeHDRPreviewCallback(boolean isEnabled) {
        setPreviewImageLock(true);
        this.mPreviewCallbackAdd = isEnabled ? this.mPreviewCallbackAdd | 2 : this.mPreviewCallbackAdd & -3;
        if (!(isEnabled || this.mPreviewCallbackImage == null)) {
            this.mPreviewCallbackImage.close();
            this.mPreviewCallbackImage = null;
        }
        setPreviewImageLock(false);
    }

    public void setFullFrameImageCallback(final CameraImageCallback fullFrameCb, Handler handler, boolean isWaitDone) {
        if (handler == null) {
            this.mFullImageCallback = fullFrameCb;
            return;
        }
        handler.post(new Runnable() {
            public void run() {
                ImageCallbackManager.this.mFullImageCallback = fullFrameCb;
            }
        });
        if (isWaitDone) {
            CamLog.m9v(CameraConstants.TAG, "waiting");
            waitDone(handler);
        }
    }

    public boolean waitDone(Handler handler) {
        final Object waitDoneLock = new Object();
        Runnable unlockRunnable = new Runnable() {
            public void run() {
                synchronized (waitDoneLock) {
                    waitDoneLock.notifyAll();
                }
            }
        };
        synchronized (waitDoneLock) {
            handler.post(unlockRunnable);
            try {
                waitDoneLock.wait();
                CamLog.m9v(CameraConstants.TAG, "DONE");
            } catch (InterruptedException e) {
                CamLog.m9v(CameraConstants.TAG, "waitDone interrupted");
                return false;
            }
        }
        return true;
    }

    public boolean isAddPreviewCallback() {
        return this.mPreviewCallbackAdd != 0;
    }

    public boolean isAddFullFrameCallback() {
        return this.mFullImageCallback != null;
    }

    public boolean isHDRPreviewCallbackOn() {
        return (this.mPreviewCallbackAdd & 2) == 2;
    }

    public boolean onFullFrameAvailable(ImageReader reader) {
        if (this.mFullImageCallback == null) {
            return false;
        }
        this.mFullImageCallback.onImageData(reader.acquireNextImage());
        return true;
    }

    public boolean onPreviewFrameAvailable(ImageReader reader) {
        boolean needToClose = true;
        Image image = reader.acquireNextImage();
        if (isHDRPreviewCallbackOn()) {
            setPreviewImageLock(true);
            Image oldImage = this.mPreviewCallbackImage;
            this.mPreviewCallbackImage = image;
            setPreviewImageLock(false);
            if (oldImage != null) {
                image = oldImage;
            }
            return true;
        }
        if (this.mPreviewImageCallback != null) {
            this.mPreviewImageCallback.onImageData(image);
            needToClose = false;
        }
        if (needToClose && image != null) {
            image.close();
        }
        return true;
    }

    public void setPreviewImageLock(boolean isLock) {
        if (isLock) {
            this.mPreviewImageLock.lock();
        } else if (!isLock && this.mPreviewImageLock.isLocked()) {
            this.mPreviewImageLock.unlock();
        }
    }

    public void close() {
        setPreviewImageLock(true);
        if (this.mPreviewCallbackImage != null) {
            this.mPreviewCallbackImage.close();
            this.mPreviewCallbackImage = null;
        }
        setPreviewImageLock(false);
    }

    public Image getPreviewImage() {
        return this.mPreviewCallbackImage;
    }
}
