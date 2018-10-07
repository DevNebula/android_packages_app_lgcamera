package com.lge.camera.components;

import android.app.Activity;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.FrameLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class CameraSurfaceView implements Callback {
    private SurfaceHolder mHolder = null;
    private CameraSurfaceListener mListener = null;
    private SurfaceView mSurfaceView = null;

    public interface CameraSurfaceListener {
        void onSurfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3);

        void onSurfaceCreated(SurfaceHolder surfaceHolder);

        void onSurfaceDestroyed(SurfaceHolder surfaceHolder);
    }

    public CameraSurfaceView(CameraSurfaceListener listener, Activity activity) {
        this.mListener = listener;
        this.mSurfaceView = (SurfaceView) activity.findViewById(C0088R.id.preview_surface_view);
    }

    public void acquireSurfaceView() {
        this.mHolder = this.mSurfaceView.getHolder();
        this.mHolder.addCallback(this);
        setSurfaceVisibility(0);
        CamLog.m3d(CameraConstants.TAG, "-acq- acquireSurfaceView mHolder = " + this.mHolder);
    }

    public void unbind() {
        if (!(this.mSurfaceView == null || this.mSurfaceView.getBackground() == null)) {
            this.mSurfaceView.getBackground().setCallback(null);
            this.mSurfaceView.setBackgroundDrawable(null);
        }
        this.mListener = null;
        this.mSurfaceView = null;
        this.mHolder = null;
    }

    public void setSurfaceVisibility(int visibility) {
        if (this.mSurfaceView != null) {
            CamLog.m3d(CameraConstants.TAG, "-tex- setSurfaceVisibility : " + visibility);
            this.mSurfaceView.setVisibility(visibility);
        }
    }

    public int getSurfaceVisibility() {
        if (this.mSurfaceView != null) {
            return this.mSurfaceView.getVisibility();
        }
        return 8;
    }

    public SurfaceView getSurfaceView() {
        if (this.mSurfaceView != null) {
            return this.mSurfaceView;
        }
        CamLog.m11w(CameraConstants.TAG, "SurfaceView has not been generated yet.");
        return null;
    }

    public SurfaceHolder getSurfaceHolder() {
        if (this.mHolder != null) {
            return this.mHolder;
        }
        CamLog.m11w(CameraConstants.TAG, "-hybrid- Surface holder has not been generated yet.");
        return null;
    }

    public void setLayoutParams(LayoutParams params) {
        if (this.mSurfaceView != null && params != null) {
            CamLog.m3d(CameraConstants.TAG, "-hybrid- param width x height = " + params.width + " X " + params.height);
            this.mSurfaceView.setLayoutParams(params);
        }
    }

    public void setSurfaceViewTransparent(boolean transparent) {
        if (this.mSurfaceView != null) {
            CamLog.m3d(CameraConstants.TAG, "-sol- setSurfaceViewTransparent = " + transparent);
            if (transparent) {
                this.mSurfaceView.setAlpha(0.0f);
            } else {
                this.mSurfaceView.setAlpha(1.0f);
            }
        }
    }

    public void positionPreviewToY(int distance) {
        if (this.mSurfaceView != null) {
            this.mSurfaceView.setTranslationY((float) distance);
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.mHolder = holder;
        CamLog.m3d(CameraConstants.TAG, "-hybrid- surfaceChanged " + width + " x " + height);
        if (this.mListener != null) {
            this.mListener.onSurfaceChanged(holder, format, width, height);
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        this.mHolder = holder;
        CamLog.m3d(CameraConstants.TAG, "-hybrid- surfaceCreated");
        if (this.mListener != null) {
            this.mListener.onSurfaceCreated(holder);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        CamLog.m3d(CameraConstants.TAG, "-hybrid- surfaceDestroyed");
        if (this.mListener != null) {
            this.mListener.onSurfaceDestroyed(holder);
        }
        this.mHolder = null;
    }

    public boolean setFixedSize(int width, int height) {
        if (this.mHolder == null) {
            return false;
        }
        this.mHolder.setFixedSize(width, height);
        return true;
    }
}
