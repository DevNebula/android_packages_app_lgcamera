package com.lge.camera.components;

import android.opengl.GLSurfaceView.Renderer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class StartingCoverViewRenderer implements Renderer {
    public void onDrawFrame(GL10 gl) {
        gl.glClear(16384);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }
}
