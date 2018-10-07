package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.util.Utils;

public class RatioGuideView extends View {
    protected int mGuideLineThickness = 8;
    protected int mHalfOfLineThickness = 4;
    protected final Paint mPaintBackground = new Paint(1);
    protected final Paint mPaintLine = new Paint(1);
    protected int mPreviewHeight = CameraConstantsEx.QHD_SCREEN_RESOLUTION;
    protected int mPreviewWidth = 2560;
    protected Rect mRectBackground = new Rect();
    protected Rect mRectLine = new Rect();
    protected int mScreenHeight = CameraConstantsEx.QHD_SCREEN_RESOLUTION;
    protected int mScreenWidth = 2560;

    public RatioGuideView(Context context) {
        super(context);
        initResources();
    }

    public RatioGuideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initResources();
    }

    public RatioGuideView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initResources();
    }

    private void initResources() {
        this.mGuideLineThickness = Utils.getPx(getContext(), C0088R.dimen.manual_ratio_guide_line_thickness);
        this.mHalfOfLineThickness = this.mGuideLineThickness / 2;
        initPaint();
        setScreenSize();
    }

    private void setScreenSize() {
        int[] lcdSize = Utils.getLCDsize(getContext(), true);
        if (lcdSize != null) {
            this.mScreenWidth = lcdSize[1];
            this.mScreenHeight = lcdSize[0];
        }
    }

    public void setPreviewSizeForGuideView(int width, int height) {
        this.mPreviewWidth = height;
        this.mPreviewHeight = width;
        invalidate();
    }

    private void initPaint() {
        this.mPaintBackground.setColor(Color.argb(125, 0, 0, 0));
        this.mPaintBackground.setStyle(Style.STROKE);
        this.mPaintLine.setColor(Color.argb(125, 255, 255, 255));
        this.mPaintLine.setStyle(Style.STROKE);
        this.mPaintLine.setStrokeWidth((float) this.mGuideLineThickness);
    }

    public void setBackgroundPaint(boolean transparent) {
        if (transparent) {
            this.mPaintBackground.setColor(Color.argb(0, 0, 0, 0));
        } else {
            this.mPaintBackground.setColor(Color.argb(125, 0, 0, 0));
        }
        invalidate();
    }

    private void setLineArea() {
        int startPointX = this.mHalfOfLineThickness;
        int startPointY = this.mHalfOfLineThickness;
        int endPointX = this.mScreenWidth - this.mHalfOfLineThickness;
        int endPointY = this.mScreenHeight - this.mHalfOfLineThickness;
        int bgHoritontalThickness = 0;
        int bgVerticalThickness = 0;
        if (this.mPreviewWidth < this.mScreenWidth) {
            startPointX = ((this.mScreenWidth - this.mPreviewWidth) / 2) - this.mHalfOfLineThickness;
            endPointX = (this.mPreviewWidth + startPointX) + (this.mHalfOfLineThickness * 2);
            bgHoritontalThickness = ((this.mScreenWidth - this.mPreviewWidth) / 2) - this.mHalfOfLineThickness;
        }
        if (this.mPreviewHeight < this.mScreenHeight) {
            startPointY = ((this.mScreenHeight - this.mPreviewHeight) / 2) - this.mHalfOfLineThickness;
            endPointY = (this.mPreviewHeight + startPointY) + (this.mHalfOfLineThickness * 2);
            bgVerticalThickness = ((this.mScreenHeight - this.mPreviewHeight) / 2) - this.mHalfOfLineThickness;
        }
        this.mRectLine.set(startPointX, startPointY, endPointX, endPointY);
        int bgThickness = Math.max(bgHoritontalThickness, bgVerticalThickness);
        this.mPaintBackground.setStrokeWidth((float) bgThickness);
        this.mRectBackground.set((startPointX - (bgThickness / 2)) - this.mHalfOfLineThickness, (startPointY - (bgThickness / 2)) - this.mHalfOfLineThickness, ((bgThickness / 2) + endPointX) + this.mHalfOfLineThickness, ((bgThickness / 2) + endPointY) + this.mHalfOfLineThickness);
    }

    public int getRatioGuideHeight() {
        return ((this.mScreenWidth - this.mPreviewWidth) / 2) - this.mHalfOfLineThickness;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        setLineArea();
        canvas.drawRect(this.mRectLine, this.mPaintLine);
        canvas.drawRect(this.mRectBackground, this.mPaintBackground);
    }
}
