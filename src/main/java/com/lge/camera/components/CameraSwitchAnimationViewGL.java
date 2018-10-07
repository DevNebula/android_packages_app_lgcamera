package com.lge.camera.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.support.p000v4.view.PointerIconCompat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import com.lge.camera.components.CameraSwitchingAnimationRenderer.CameraSwitchingAnimationCallback;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CamLog.TraceTag;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.Utils;

public class CameraSwitchAnimationViewGL extends GLSurfaceView implements OnRemoveHandler, CameraSwitchingAnimationCallback {
    private ComponentInterface mGet = null;
    private CameraSwitchingAnimationRenderer mRenderer = null;
    private GLSurfaceView mView = null;
    private boolean mViewReadyToShow = false;

    public CameraSwitchAnimationViewGL(Context context) {
        super(context);
        if (FunctionProperties.isSupportedSwitchingAnimation()) {
            this.mRenderer = new CameraSwitchingAnimationRenderer();
            setRenderer(this.mRenderer);
            setRenderMode(0);
            this.mRenderer.setAnimtationCallback(this);
            init();
        }
    }

    public CameraSwitchAnimationViewGL(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (FunctionProperties.isSupportedSwitchingAnimation()) {
            CamLog.m3d(CameraConstants.TAG, "CameraSwitchAnimationViewGL");
            this.mRenderer = new CameraSwitchingAnimationRenderer();
            setRenderer(this.mRenderer);
            setRenderMode(0);
            this.mRenderer.setAnimtationCallback(this);
            init();
        }
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        CamLog.m3d(CameraConstants.TAG, "CameraSwitchAnimationViewGL - finalize()");
    }

    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        CamLog.m3d(CameraConstants.TAG, "CameraSwitchAnimationViewGL - surfaceCreated()");
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
        CamLog.m3d(CameraConstants.TAG, "CameraSwitchAnimationViewGL - surfaceChanged()");
        if (this.mRenderer != null && this.mRenderer.isAvailabeDrawFrame()) {
            requestRender();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        CamLog.m3d(CameraConstants.TAG, "CameraSwitchAnimationViewGL - surfaceDestroyed()");
    }

    public void unbind() {
        if (this.mRenderer != null) {
            this.mRenderer.destroy();
            this.mRenderer = null;
        }
        this.mView = null;
        this.mGet = null;
        this.mViewReadyToShow = false;
    }

    public void setComponentInterface(ComponentInterface get) {
        this.mGet = get;
    }

    public void readyToShow() {
        if (!this.mViewReadyToShow) {
            this.mViewReadyToShow = true;
            this.mView.setTranslationY((float) (Utils.getDefaultDisplayHeight(this.mGet.getActivity()) + 300));
            CamLog.m3d(CameraConstants.TAG, "readyToShow");
            this.mView.setVisibility(0);
            CamLog.m3d(CameraConstants.TAG, "-ani- set animation view visible");
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (CameraSwitchAnimationViewGL.this.mView != null) {
                        CameraSwitchAnimationViewGL.this.mView.setVisibility(4);
                        CamLog.m3d(CameraConstants.TAG, "-ani- set animation view invisible");
                    }
                }
            }, 50);
        }
    }

    public void init() {
        this.mViewReadyToShow = false;
        this.mView = this;
    }

    public void startGLAnimation(int animationType) {
        CamLog.traceBegin(TraceTag.OPTIONAL, "SwitchAnimation", PointerIconCompat.TYPE_HAND);
        this.mRenderer.startAnimation(animationType);
    }

    public void setTextureBitmap(Bitmap bmp) {
        this.mRenderer.setTextureBitmap(bmp);
    }

    public void setFlipDirection(int direction) {
        this.mRenderer.setFlipDirection(direction);
    }

    public void drawFrame() {
        requestRender();
    }

    public void stopGLAnimation() {
        this.mRenderer.stopAnimation();
    }

    public int getAnimationType() {
        return this.mRenderer.getAnimationType();
    }

    public void resetAnimType() {
        this.mRenderer.resetAnimType();
    }

    public boolean checkCameraSwitchingAnim(boolean isOpticZoomSupported) {
        return this.mRenderer.checkCameraSwitchingAnim(isOpticZoomSupported);
    }
}
