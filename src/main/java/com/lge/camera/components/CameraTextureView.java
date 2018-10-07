package com.lge.camera.components;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.FrameLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class CameraTextureView implements SurfaceTextureListener {
    private CameraTextureListener mListener = null;
    private TextureView mTextureView = null;

    public interface CameraTextureListener {
        void onTextureAvaliable(SurfaceTexture surfaceTexture, int i, int i2);

        boolean onTextureDestroyed(SurfaceTexture surfaceTexture);

        void onTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2);

        void onTextureUpdated(SurfaceTexture surfaceTexture);
    }

    public CameraTextureView(CameraTextureListener listener, Activity activity) {
        this.mListener = listener;
        this.mTextureView = (TextureView) activity.findViewById(C0088R.id.preview_texture_view);
    }

    public void acquireTextureView() {
        CamLog.m3d(CameraConstants.TAG, "-hybrid- acquireTextureView");
        this.mTextureView.setSurfaceTextureListener(this);
        this.mTextureView.setBackground(null);
        setTextureVisibility(0);
    }

    public void unbind() {
        this.mListener = null;
        this.mTextureView = null;
    }

    public TextureView getTextureView() {
        return this.mTextureView;
    }

    public void setTextureVisibility(int visibility) {
        if (this.mTextureView != null) {
            this.mTextureView.setVisibility(visibility);
            CamLog.m3d(CameraConstants.TAG, "-hybrid- setTextureVisibility : " + visibility);
        }
    }

    public int getTextureVisibility() {
        if (this.mTextureView != null) {
            return this.mTextureView.getVisibility();
        }
        return 8;
    }

    public SurfaceTexture getSurfaceTexture() {
        if (this.mTextureView != null) {
            return this.mTextureView.getSurfaceTexture();
        }
        CamLog.m11w(CameraConstants.TAG, "TextureView has not been generated yet.");
        return null;
    }

    public void setLayoutParams(LayoutParams params) {
        if (this.mTextureView != null) {
            this.mTextureView.setLayoutParams(params);
        }
    }

    public void positionPreviewToY(int distance) {
        if (this.mTextureView != null) {
            this.mTextureView.setTranslationY((float) distance);
        }
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (this.mListener != null) {
            this.mListener.onTextureAvaliable(surfaceTexture, width, height);
        }
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        if (this.mListener != null) {
            this.mListener.onTextureSizeChanged(surfaceTexture, width, height);
        }
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        if (this.mListener != null) {
            return this.mListener.onTextureDestroyed(surfaceTexture);
        }
        return false;
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        if (this.mListener != null) {
            this.mListener.onTextureUpdated(surfaceTexture);
        }
    }
}
