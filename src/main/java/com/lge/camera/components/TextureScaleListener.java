package com.lge.camera.components;

import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import com.lge.camera.app.ProjectionMatrix;

public class TextureScaleListener extends SimpleOnScaleGestureListener {
    private int mHalfLCDDistance = 0;
    private float mPrevSpan = 0.0f;

    public TextureScaleListener(int halfLCDDistance) {
        this.mHalfLCDDistance = halfLCDDistance;
    }

    public boolean onScale(ScaleGestureDetector detector) {
        float mCurSpan = detector.getCurrentSpan();
        float distance = (mCurSpan - this.mPrevSpan) / ((float) this.mHalfLCDDistance);
        this.mPrevSpan = mCurSpan;
        ProjectionMatrix.setScale(distance, distance);
        return super.onScale(detector);
    }

    public boolean onScaleBegin(ScaleGestureDetector detector) {
        this.mPrevSpan = detector.getCurrentSpan();
        return super.onScaleBegin(detector);
    }

    public void onScaleEnd(ScaleGestureDetector detector) {
        super.onScaleEnd(detector);
    }
}
