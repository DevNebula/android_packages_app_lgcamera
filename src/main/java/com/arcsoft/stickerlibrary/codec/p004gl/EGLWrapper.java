package com.arcsoft.stickerlibrary.codec.p004gl;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;
import com.arcsoft.stickerlibrary.codec.CodecLog;

/* renamed from: com.arcsoft.stickerlibrary.codec.gl.EGLWrapper */
public class EGLWrapper {
    private static final int EGL_RECORDABLE_ANDROID = 12610;
    private static final String TAG = "Arc_EGLWrapper";
    private EGLContext EGL_SHARE_CONTEXT;
    private EGLConfig[] mEGLConfigs;
    private EGLContext mEGLContext;
    private EGLDisplay mEGLDisplay;
    private EGLSurface mEGLSurface;
    private int mHeight;
    private Surface mSurface;
    private int mWidth;

    public EGLWrapper(Surface surface) {
        this.mEGLContext = EGL14.EGL_NO_CONTEXT;
        this.mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        this.mEGLSurface = EGL14.EGL_NO_SURFACE;
        this.mEGLConfigs = new EGLConfig[1];
        this.EGL_SHARE_CONTEXT = EGL14.EGL_NO_CONTEXT;
        if (surface == null) {
            throw new NullPointerException();
        }
        this.mSurface = surface;
        egl_Setup();
    }

    public EGLWrapper(Surface surface, EGLContext sharedContext) {
        this.mEGLContext = EGL14.EGL_NO_CONTEXT;
        this.mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        this.mEGLSurface = EGL14.EGL_NO_SURFACE;
        this.mEGLConfigs = new EGLConfig[1];
        this.EGL_SHARE_CONTEXT = EGL14.EGL_NO_CONTEXT;
        if (surface == null) {
            throw new NullPointerException();
        }
        this.mSurface = surface;
        this.EGL_SHARE_CONTEXT = sharedContext;
        egl_Setup();
    }

    public int getWidth() {
        int[] val = new int[1];
        EGL14.eglQuerySurface(this.mEGLDisplay, this.mEGLSurface, 12375, val, 0);
        return val[0];
    }

    public int getHeight() {
        int[] val = new int[1];
        EGL14.eglQuerySurface(this.mEGLDisplay, this.mEGLSurface, 12374, val, 0);
        return val[0];
    }

    private void createEGLSurface() {
        this.mEGLSurface = EGL14.eglCreateWindowSurface(this.mEGLDisplay, this.mEGLConfigs[0], this.mSurface, new int[]{12344}, 0);
        checkEglError("eglCreateWindowSurface");
        if (this.mEGLSurface == null) {
            throw new RuntimeException("surface == null");
        }
    }

    private void egl_Setup() {
        this.mEGLDisplay = EGL14.eglGetDisplay(0);
        if (this.mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("EGL14.eglGetDisplay fail...");
        }
        int[] major_version = new int[2];
        if (EGL14.eglInitialize(this.mEGLDisplay, major_version, 0, major_version, 1)) {
            int[] attribList = new int[]{12339, 4, 12352, 4, 12324, 8, 12323, 8, 12322, 8, 12321, 8, 12325, 16, EGL_RECORDABLE_ANDROID, 1, 12344};
            int[] numConfigs = new int[1];
            if (EGL14.eglChooseConfig(this.mEGLDisplay, attribList, 0, this.mEGLConfigs, 0, this.mEGLConfigs.length, numConfigs, 0)) {
                this.mEGLContext = EGL14.eglCreateContext(this.mEGLDisplay, this.mEGLConfigs[0], this.EGL_SHARE_CONTEXT, new int[]{12440, 2, 12344}, 0);
                checkEglError("eglCreateContext");
                if (this.mEGLContext == null) {
                    throw new RuntimeException("eglCreateContext == null");
                }
                createEGLSurface();
                this.mWidth = getWidth();
                this.mHeight = getHeight();
                CodecLog.m41d(TAG, "egl_Setup , display=" + this.mEGLDisplay + " ,context=" + this.mEGLContext + " ,sharedContext= " + this.EGL_SHARE_CONTEXT + ", surface=" + this.mEGLSurface + ", w=" + this.mWidth + " ,h=" + this.mHeight);
                return;
            }
            throw new RuntimeException("eglChooseConfig [RGBA888 + recordable] ES2 EGL_config_fail...");
        }
        this.mEGLDisplay = null;
        throw new RuntimeException("EGL14.eglInitialize fail...");
    }

    private void releaseEGLSurface() {
        if (this.mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglDestroySurface(this.mEGLDisplay, this.mEGLSurface);
            this.mEGLSurface = EGL14.EGL_NO_SURFACE;
        }
    }

    public void makeUnCurrent() {
        if (!EGL14.eglMakeCurrent(this.mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)) {
            checkEglError("makeUnCurrent");
        }
    }

    public boolean makeCurrent() {
        if (this.mEGLDisplay == null || this.mEGLSurface == null) {
            CodecLog.m41d(TAG, "makeCurrent()-> failed");
            return false;
        }
        boolean success = EGL14.eglMakeCurrent(this.mEGLDisplay, this.mEGLSurface, this.mEGLSurface, this.mEGLContext);
        if (!success) {
            checkEglError("makeCurrent");
        }
        CodecLog.m41d(TAG, "makeCurrent()-> " + success);
        return success;
    }

    public boolean swapBuffers() {
        if (this.mEGLDisplay == null || this.mEGLSurface == null) {
            return false;
        }
        boolean success = EGL14.eglSwapBuffers(this.mEGLDisplay, this.mEGLSurface);
        if (success) {
            return success;
        }
        checkEglError("makeCurrent");
        return success;
    }

    public Surface getSurface() {
        return this.mSurface;
    }

    public void setPresentationTime(long nsecs) {
        EGLExt.eglPresentationTimeANDROID(this.mEGLDisplay, this.mEGLSurface, nsecs);
        checkEglError("eglPresentationTimeANDROID");
    }

    public void updateSize(int width, int height) {
        if (width != this.mWidth || height != this.mHeight) {
            CodecLog.m41d(TAG, "re-create EGLSurface");
            releaseEGLSurface();
            createEGLSurface();
            this.mWidth = getWidth();
            this.mHeight = getHeight();
        }
    }

    public void release() {
        if (this.mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglDestroySurface(this.mEGLDisplay, this.mEGLSurface);
            EGL14.eglDestroyContext(this.mEGLDisplay, this.mEGLContext);
            EGL14.eglTerminate(this.mEGLDisplay);
        }
        this.mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        this.mEGLContext = EGL14.EGL_NO_CONTEXT;
        this.mEGLSurface = EGL14.EGL_NO_SURFACE;
        this.mSurface = null;
    }

    private void checkEglError(String message) {
        int error = EGL14.eglGetError();
        if (error != 12288) {
            new Exception("NOT_ERROR_JUST_SEE_CALL_STACK").printStackTrace();
            throw new RuntimeException(message + ": EGL_ERROR_CODE: 0x" + Integer.toHexString(error));
        }
    }
}
