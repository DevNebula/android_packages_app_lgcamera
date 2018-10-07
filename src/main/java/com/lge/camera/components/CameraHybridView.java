package com.lge.camera.components;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.FrameLayout.LayoutParams;
import com.lge.camera.components.CameraSurfaceView.CameraSurfaceListener;
import com.lge.camera.components.CameraTextureView.CameraTextureListener;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class CameraHybridView implements CameraTextureListener, CameraSurfaceListener {
    private boolean mAcquiredListener = false;
    private CameraSurfaceView mCameraSurfaceView = null;
    private CameraTextureView mCameraTextureView = null;
    private String mCurrentViewType = null;
    private boolean mIsReadyPreviewBuffered = false;
    private CameraPreviewListener mListener = null;
    private Size mPreviewSize = null;

    public interface CameraPreviewListener {
        void onPreviewChanged(Object obj, int i, int i2);

        void onPreviewCreated(Object obj, int i, int i2);

        boolean onPreviewDestroyed(Object obj);

        void onPreviewUpdated(Object obj);
    }

    public CameraHybridView(Activity act) {
        this.mListener = (CameraPreviewListener) act;
        this.mCameraTextureView = new CameraTextureView(this, act);
        this.mCameraSurfaceView = new CameraSurfaceView(this, act);
        HybridViewConfig.makeHybridViewConfig();
    }

    public void acquireHybridView(Class<?> cls) {
        if (HybridViewConfig.SURFACE.equals(HybridViewConfig.getCurrentView(cls.getName()))) {
            this.mCameraSurfaceView.acquireSurfaceView();
            this.mCameraTextureView.setTextureVisibility(8);
            this.mCurrentViewType = HybridViewConfig.SURFACE;
        } else {
            this.mCameraTextureView.acquireTextureView();
            this.mCameraSurfaceView.setSurfaceVisibility(8);
            this.mCurrentViewType = HybridViewConfig.TEXTURE;
        }
        this.mAcquiredListener = true;
        CamLog.m3d(CameraConstants.TAG, "current view type = " + this.mCurrentViewType);
    }

    public void unbind() {
        CamLog.m3d(CameraConstants.TAG, "-mem- unbindPreview");
        if (this.mCameraTextureView != null) {
            this.mCameraTextureView.unbind();
            this.mCameraTextureView = null;
        }
        if (this.mCameraSurfaceView != null) {
            this.mCameraSurfaceView.unbind();
            this.mCameraSurfaceView = null;
        }
        this.mListener = null;
        this.mCurrentViewType = null;
        this.mAcquiredListener = false;
        this.mPreviewSize = null;
    }

    public boolean isAcquiredListener() {
        CamLog.m3d(CameraConstants.TAG, "isAcquiredListener() " + this.mAcquiredListener);
        return this.mAcquiredListener;
    }

    public void setAcquired(boolean set) {
        this.mAcquiredListener = set;
    }

    public String getCurrentViewType() {
        if (this.mCurrentViewType != null) {
            return this.mCurrentViewType;
        }
        return null;
    }

    public TextureView getCameraTextureView() {
        if (HybridViewConfig.TEXTURE.equals(this.mCurrentViewType)) {
            return this.mCameraTextureView.getTextureView();
        }
        return null;
    }

    public void setPreviewVisibility(int visibility) {
        if (HybridViewConfig.SURFACE.equals(this.mCurrentViewType)) {
            if (this.mCameraSurfaceView != null) {
                this.mCameraSurfaceView.setSurfaceVisibility(visibility);
            }
        } else if (this.mCameraTextureView != null) {
            this.mCameraTextureView.setTextureVisibility(visibility);
        }
    }

    public int getPreviewVisibility() {
        if (HybridViewConfig.SURFACE.equals(this.mCurrentViewType)) {
            if (this.mCameraSurfaceView != null) {
                return this.mCameraSurfaceView.getSurfaceVisibility();
            }
        } else if (this.mCameraTextureView != null) {
            return this.mCameraTextureView.getTextureVisibility();
        }
        return 8;
    }

    public SurfaceTexture getSurfaceTexture() {
        if (HybridViewConfig.TEXTURE.equals(this.mCurrentViewType) && this.mCameraTextureView != null) {
            return this.mCameraTextureView.getSurfaceTexture();
        }
        CamLog.m11w(CameraConstants.TAG, "TextureView has not been generated yet.");
        return null;
    }

    public SurfaceHolder getSurfaceHolder() {
        if (HybridViewConfig.SURFACE.equals(this.mCurrentViewType)) {
            return this.mCameraSurfaceView.getSurfaceHolder();
        }
        return null;
    }

    public SurfaceView getSurfaceView() {
        if (HybridViewConfig.SURFACE.equals(this.mCurrentViewType)) {
            return this.mCameraSurfaceView.getSurfaceView();
        }
        return null;
    }

    public void setLayoutParams(LayoutParams params) {
        if (HybridViewConfig.SURFACE.equals(this.mCurrentViewType)) {
            if (this.mCameraSurfaceView != null) {
                this.mCameraSurfaceView.setLayoutParams(params);
            }
        } else if (this.mCameraTextureView != null) {
            this.mCameraTextureView.setLayoutParams(params);
        }
    }

    public void setSurfaceViewTransparent(boolean transparent) {
        if (this.mCameraSurfaceView != null) {
            this.mCameraSurfaceView.setSurfaceViewTransparent(transparent);
        }
    }

    public void positionPreviewToY(int distance) {
        if (HybridViewConfig.SURFACE.equals(this.mCurrentViewType)) {
            if (this.mCameraSurfaceView != null) {
                this.mCameraSurfaceView.positionPreviewToY(distance);
            }
        } else if (this.mCameraTextureView != null) {
            this.mCameraTextureView.positionPreviewToY(distance);
        }
    }

    public void onSurfaceCreated(SurfaceHolder surfaceTexture) {
        if (this.mListener != null) {
            this.mListener.onPreviewCreated(surfaceTexture, 0, 0);
        }
    }

    public void onSurfaceChanged(SurfaceHolder surfaceTexture, int format, int width, int height) {
        if (this.mPreviewSize == null) {
            this.mPreviewSize = new Size(width, height);
            setReadyPreviewBufferFlag(true);
        } else if (this.mPreviewSize.getHeight() == height && this.mPreviewSize.getWidth() == width) {
            setReadyPreviewBufferFlag(true);
        }
        CamLog.m7i(CameraConstants.TAG, " Surface Changed width" + width + " height " + height + "  " + surfaceTexture);
        if (this.mListener != null) {
            this.mListener.onPreviewChanged(surfaceTexture, width, height);
        }
    }

    public void onSurfaceDestroyed(SurfaceHolder surfaceTexture) {
        if (this.mListener != null) {
            this.mListener.onPreviewDestroyed(surfaceTexture);
        }
        setReadyPreviewBufferFlag(false);
    }

    public void onTextureAvaliable(SurfaceTexture surfaceTexture, int width, int height) {
        if (this.mListener != null) {
            this.mListener.onPreviewCreated(surfaceTexture, width, height);
        }
    }

    public void onTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        if (this.mListener != null) {
            this.mListener.onPreviewChanged(surfaceTexture, width, height);
        }
    }

    public boolean onTextureDestroyed(SurfaceTexture surfaceTexture) {
        if (this.mListener != null) {
            this.mListener.onPreviewDestroyed(surfaceTexture);
        }
        return false;
    }

    public void onTextureUpdated(SurfaceTexture surfaceTexture) {
        if (this.mListener != null) {
            this.mListener.onPreviewUpdated(surfaceTexture);
        }
    }

    public boolean isNeedConfigCameraPreviewSize(int width, int height) {
        if (this.mCameraSurfaceView == null || width == 0 || height == 0) {
            return false;
        }
        if (this.mPreviewSize != null && this.mPreviewSize.getWidth() == width && this.mPreviewSize.getHeight() == height) {
            return false;
        }
        return true;
    }

    public void setCameraPreviewSize(int width, int height) {
        if (this.mPreviewSize == null || this.mPreviewSize.getWidth() != width || this.mPreviewSize.getHeight() != height) {
            CamLog.m7i(CameraConstants.TAG, "width : " + width + " height : " + height);
            this.mPreviewSize = new Size(width, height);
            if (HybridViewConfig.SURFACE.equals(this.mCurrentViewType)) {
                if (this.mCameraSurfaceView != null) {
                    if (!this.mCameraSurfaceView.setFixedSize(width, height)) {
                        this.mPreviewSize = null;
                    }
                    setReadyPreviewBufferFlag(false);
                }
            } else if (this.mCameraTextureView != null) {
                SurfaceTexture surfaceTexture = this.mCameraTextureView.getSurfaceTexture();
                if (surfaceTexture != null) {
                    surfaceTexture.setDefaultBufferSize(this.mPreviewSize.getWidth(), this.mPreviewSize.getHeight());
                    setReadyPreviewBufferFlag(true);
                }
            }
        }
    }

    public boolean isReadyPreviewBuffer() {
        CamLog.m7i(CameraConstants.TAG, "isReadyPreviewBuffer : " + this.mIsReadyPreviewBuffered);
        return HybridViewConfig.SURFACE.equals(this.mCurrentViewType) ? this.mIsReadyPreviewBuffered : true;
    }

    public synchronized void setReadyPreviewBufferFlag(boolean isSet) {
        this.mIsReadyPreviewBuffered = isSet;
    }
}
