package com.lge.camera.app.ext;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class EGLSurfaceBase {
    private static final int EGL_RECORDABLE_ANDROID = 12610;
    public static final int FLAG_RECORDABLE = 1;
    int[] alWindowSurface = new int[]{12344};
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
    private Surface mSurface;

    public EGLSurfaceBase(EGLContext sharedContext, Surface surface, int flag) {
        if (surface == null) {
            throw new NullPointerException();
        }
        this.mSurface = surface;
        eglSetup(sharedContext, flag);
    }

    private void eglSetup(EGLContext sharedContext, int flag) {
        CamLog.m7i(CameraConstants.TAG, "Creating EGL14 Surface");
        if (this.mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("EGL already set up");
        }
        if (sharedContext == null) {
            sharedContext = EGL14.EGL_NO_CONTEXT;
        }
        this.mEGLDisplay = EGL14.eglGetDisplay(0);
        if (this.mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }
        int[] version = new int[2];
        if (EGL14.eglInitialize(this.mEGLDisplay, version, 0, version, 1)) {
            EGLConfig config = getConfig(flag, 2);
            if (config == null) {
                throw new RuntimeException("Unable to find a suitable EGLConfig");
            }
            this.mEGLContext = EGL14.eglCreateContext(this.mEGLDisplay, config, sharedContext, new int[]{12440, 2, 12344}, 0);
            checkEglError("eglCreateContext");
            this.mEGLSurface = EGL14.eglCreateWindowSurface(this.mEGLDisplay, config, this.mSurface, this.alWindowSurface, 0);
            checkEglError("eglCreateWindowSurface");
            if (this.mEGLSurface == null) {
                throw new RuntimeException("surface was null");
            }
            return;
        }
        this.mEGLDisplay = null;
        throw new RuntimeException("unable to initialize EGL14");
    }

    private EGLConfig getConfig(int flags, int version) {
        int renderableType = 4;
        if (version >= 3) {
            renderableType = 4 | 64;
        }
        int[] attribList = new int[]{12324, 8, 12323, 8, 12322, 8, 12321, 8, 12325, 0, 12326, 0, 12352, renderableType, EGL_RECORDABLE_ANDROID, 1, 12344, 0, 12344};
        if ((flags & 1) != 0) {
            attribList[attribList.length - 3] = EGL_RECORDABLE_ANDROID;
            attribList[attribList.length - 2] = 1;
        }
        EGLConfig[] configs = new EGLConfig[1];
        if (EGL14.eglChooseConfig(this.mEGLDisplay, attribList, 0, configs, 0, configs.length, new int[1], 0)) {
            return configs[0];
        }
        CamLog.m11w(CameraConstants.TAG, "unable to find RGB8888 / " + version + " EGLConfig");
        return null;
    }

    public void release() {
        CamLog.m3d(CameraConstants.TAG, "release()");
        if (this.mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(this.mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(this.mEGLDisplay, this.mEGLSurface);
            EGL14.eglDestroyContext(this.mEGLDisplay, this.mEGLContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(this.mEGLDisplay);
        }
        this.mSurface.release();
        this.mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        this.mEGLContext = EGL14.EGL_NO_CONTEXT;
        this.mEGLSurface = EGL14.EGL_NO_SURFACE;
        this.mSurface = null;
    }

    public void makeContextCurrent() {
        makeCurrent(this.mEGLContext);
    }

    private void makeCurrent(EGLContext context) {
        if (this.mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            CamLog.m11w(CameraConstants.TAG, "NOTE: makeCurrent w/o display");
        }
        if (!context.equals(EGL14.eglGetCurrentContext())) {
            EGL14.eglMakeCurrent(this.mEGLDisplay, this.mEGLSurface, this.mEGLSurface, context);
            checkEglError("eglMakeCurrent");
        }
    }

    public boolean swapBuffers() {
        boolean result = EGL14.eglSwapBuffers(this.mEGLDisplay, this.mEGLSurface);
        checkEglError("eglSwapBuffers");
        return result;
    }

    public void setPresentationTime(long nsecs) {
        EGLExt.eglPresentationTimeANDROID(this.mEGLDisplay, this.mEGLSurface, nsecs);
    }

    private void checkEglError(String msg) {
        int error = EGL14.eglGetError();
        if (error != 12288) {
            CamLog.m5e(CameraConstants.TAG, msg + ": EGL error: 0x" + Integer.toHexString(error));
            throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }

    public EGLContext getEGLContext() {
        return this.mEGLContext;
    }
}
