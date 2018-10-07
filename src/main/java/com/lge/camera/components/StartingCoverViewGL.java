package com.lge.camera.components;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class StartingCoverViewGL extends GLSurfaceView {
    public StartingCoverViewGL(Context context) {
        super(context);
        setRenderer(new StartingCoverViewRenderer());
    }

    public StartingCoverViewGL(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRenderer(new StartingCoverViewRenderer());
        setRenderMode(0);
    }
}
