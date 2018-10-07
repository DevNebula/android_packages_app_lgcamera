package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.support.p000v4.internal.view.SupportMenu;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.lge.camera.C0088R;
import com.lge.camera.constants.FunctionProperties;
import java.util.ArrayList;

public class Histogram extends FrameLayout {
    private static int sSupportedHal;
    private final int MAX_B_COLOR_SCALE_SIZE = 1028;
    private final int MAX_G_COLOR_SCALE_SIZE = 771;
    private final int MAX_RGB_SCLAE_SIZE = 768;
    private final int MAX_R_COLOR_SCALE_SIZE = 514;
    private final int MAX_YRGB_SCLAE_SIZE = 1024;
    private final int MAX_Y_COLOR_SCALE_SIZE = 257;
    private final int MAX_Y_COLOR_SCALE_SIZE_1024 = 1024;
    private final int RGB_MODE = 1;
    private final int RGB_SCALE_BUCKET_SIZE = 256;
    private final int YRGB_MODE = 2;
    private final int Y_MODE = 0;
    private boolean isPaused = false;
    private ArrayList<Float> mBColorScaleLineList = new ArrayList();
    private int mCameraMaxScaleSize = 0;
    private ArrayList<Float> mGColorScaleLineList = new ArrayList();
    private int mHistogramType = 0;
    private Paint mLineBluePaint = new Paint();
    private Paint mLineGreenPaint = new Paint();
    private Paint mLineRedPaint = new Paint();
    private Paint mLineWhitePaint = new Paint();
    private ArrayList<Float> mRColorScaleLineList = new ArrayList();
    private ArrayList<Float> mYColorScaleLineList = new ArrayList();

    public Histogram(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVariable();
    }

    public void initVariable() {
        sSupportedHal = FunctionProperties.getSupportedHal();
        this.mCameraMaxScaleSize = getResources().getDimensionPixelSize(C0088R.dimen.histogram_panel_height);
        this.mLineWhitePaint.setColor(-1);
        this.mLineWhitePaint.setAlpha(255);
        this.mLineWhitePaint.setStyle(Style.FILL);
        this.mLineWhitePaint.setStrokeJoin(Join.BEVEL);
        this.mLineWhitePaint.setStrokeCap(Cap.SQUARE);
        this.mLineGreenPaint.setColor(-16711936);
        this.mLineGreenPaint.setAlpha(127);
        this.mLineGreenPaint.setStyle(Style.FILL);
        this.mLineGreenPaint.setStrokeJoin(Join.BEVEL);
        this.mLineGreenPaint.setStrokeCap(Cap.SQUARE);
        this.mLineRedPaint.setColor(SupportMenu.CATEGORY_MASK);
        this.mLineRedPaint.setAlpha(127);
        this.mLineRedPaint.setStyle(Style.FILL);
        this.mLineRedPaint.setStrokeJoin(Join.BEVEL);
        this.mLineRedPaint.setStrokeCap(Cap.SQUARE);
        this.mLineBluePaint.setColor(-16776961);
        this.mLineBluePaint.setAlpha(127);
        this.mLineBluePaint.setStyle(Style.FILL);
        this.mLineBluePaint.setStrokeJoin(Join.BEVEL);
        this.mLineBluePaint.setStrokeCap(Cap.SQUARE);
    }

    public void setHistogram(int[] histogram) {
        int length = histogram.length;
        if (sSupportedHal == 2) {
            if (length == 1024) {
                this.mHistogramType = 0;
                convertYHistogram1024(histogram, 0);
            }
        } else if (length == 1029) {
            this.mHistogramType = 0;
            convertYHistogram(histogram, 1);
        } else if (length == 258) {
            convertYRGBHistogram(histogram, 1);
        } else if (length == 1028) {
            this.mHistogramType = 2;
            convertYRGBHistogram(histogram, 0);
        } else if (length == 257) {
            this.mHistogramType = 0;
            convertYHistogram(histogram, 0);
        }
    }

    public void releaseHistogram() {
        this.mYColorScaleLineList.clear();
        this.mRColorScaleLineList.clear();
        this.mGColorScaleLineList.clear();
        this.mBColorScaleLineList.clear();
        invalidate();
    }

    private void convertYHistogram(int[] histogram, int dataGap) {
        if (histogram != null) {
            int maxValue = Integer.MIN_VALUE;
            if (histogram[dataGap + 0] == 0) {
                for (int i = dataGap + 1; i < dataGap + 257; i++) {
                    if (maxValue < histogram[i]) {
                        maxValue = histogram[i];
                    }
                }
            } else {
                maxValue = histogram[dataGap + 0];
            }
            int scale = maxValue / this.mCameraMaxScaleSize;
            if (scale == 0) {
                scale = 1;
            }
            makeHitogramLineList(this.mYColorScaleLineList, histogram, dataGap + 1, scale);
            invalidate();
        }
    }

    private void convertYHistogram1024(int[] histogram, int dataGap) {
        if (histogram != null) {
            int maxValue = Integer.MIN_VALUE;
            for (int i = 0; i < 1024; i++) {
                if (maxValue < histogram[i]) {
                    maxValue = histogram[i];
                }
            }
            int scale = maxValue / this.mCameraMaxScaleSize;
            if (scale == 0) {
                scale = 1;
            }
            makeHitogramLineList1024(this.mYColorScaleLineList, histogram, 0, scale);
            invalidate();
        }
    }

    private void convertYRGBHistogram(int[] histogram, int dataGap) {
        if (histogram != null) {
            int maxValue = Integer.MIN_VALUE;
            int maxYRGB = histogram[dataGap + 0] > histogram[dataGap + 257] ? histogram[dataGap + 0] : histogram[dataGap + 257];
            if (maxYRGB <= histogram[dataGap + 514]) {
                maxYRGB = histogram[dataGap + 514];
            }
            if (maxYRGB <= histogram[dataGap + 771]) {
                maxYRGB = histogram[dataGap + 771];
            }
            if (maxYRGB == 0) {
                for (int i = dataGap + 1; i < dataGap + 1028; i++) {
                    if (maxValue < histogram[i]) {
                        maxValue = histogram[i];
                    }
                }
            } else {
                maxValue = maxYRGB;
            }
            int scale = maxValue / this.mCameraMaxScaleSize;
            if (scale == 0) {
                scale = 1;
            }
            makeHitogramLineList(this.mYColorScaleLineList, histogram, dataGap + 1, scale);
            makeHitogramLineList(this.mRColorScaleLineList, histogram, dataGap + 257, scale);
            makeHitogramLineList(this.mGColorScaleLineList, histogram, dataGap + 514, scale);
            makeHitogramLineList(this.mBColorScaleLineList, histogram, dataGap + 771, scale);
            invalidate();
        }
    }

    private void makeHitogramLineList(ArrayList<Float> array, int[] histogram, int position, int scale) {
        array.clear();
        int maxPosition = (position + 257) - 2;
        int i = 0;
        while (i < 256) {
            if (!(i % 5 == 1 || i % 5 == 4)) {
                array.add(Float.valueOf((float) (histogram[i + position] / scale)));
            }
            i++;
        }
        int scaled = histogram[maxPosition] / scale;
        array.add(Float.valueOf((float) scaled));
        array.add(Float.valueOf((float) scaled));
        array.add(Float.valueOf(0.0f));
        array.add(Float.valueOf(0.0f));
    }

    private void makeHitogramLineList1024(ArrayList<Float> array, int[] histogram, int position, int scale) {
        array.clear();
        int maxPosition = (position + 1024) - 1;
        for (int i = 0; i < 1024; i++) {
            if (i % 5 == 0) {
                array.add(Float.valueOf((float) (histogram[i + position] / scale)));
            }
        }
        int scaled = histogram[maxPosition] / scale;
        array.add(Float.valueOf((float) scaled));
        array.add(Float.valueOf((float) scaled));
        array.add(Float.valueOf(0.0f));
        array.add(Float.valueOf(0.0f));
    }

    protected void dispatchDraw(Canvas canvas) {
        if (canvas != null && !this.isPaused) {
            if (this.mHistogramType == 1) {
                canvas = getRGBHistogramCanvas(canvas);
            } else if (this.mHistogramType == 2) {
                canvas = getYRGBHistogramCanvas(canvas);
            } else {
                canvas = getNormalHistogramCanvas(canvas);
            }
            super.dispatchDraw(canvas);
        }
    }

    public void setPause(boolean pause) {
        this.isPaused = pause;
    }

    private Canvas getNormalHistogramCanvas(Canvas canvas) {
        int scaleSize = this.mYColorScaleLineList.size();
        for (int i = 0; i < scaleSize; i++) {
            canvas.drawLine(0.0f, (float) i, ((Float) this.mYColorScaleLineList.get(i)).floatValue(), (float) i, this.mLineWhitePaint);
        }
        return canvas;
    }

    private Canvas getRGBHistogramCanvas(Canvas canvas) {
        int scaleSize = this.mRColorScaleLineList.size();
        for (int i = 0; i < scaleSize; i++) {
            canvas.drawLine(0.0f, (float) i, ((Float) this.mRColorScaleLineList.get(i)).floatValue(), (float) i, this.mLineRedPaint);
            canvas.drawLine(0.0f, (float) i, ((Float) this.mGColorScaleLineList.get(i)).floatValue(), (float) i, this.mLineGreenPaint);
            canvas.drawLine(0.0f, (float) i, ((Float) this.mBColorScaleLineList.get(i)).floatValue(), (float) i, this.mLineBluePaint);
        }
        return canvas;
    }

    private Canvas getYRGBHistogramCanvas(Canvas canvas) {
        int scaleSize = this.mRColorScaleLineList.size();
        for (int i = 0; i < scaleSize; i++) {
            canvas.drawLine(0.0f, (float) i, ((Float) this.mYColorScaleLineList.get(i)).floatValue(), (float) i, this.mLineWhitePaint);
            canvas.drawLine(0.0f, (float) i, ((Float) this.mRColorScaleLineList.get(i)).floatValue(), (float) i, this.mLineRedPaint);
            canvas.drawLine(0.0f, (float) i, ((Float) this.mGColorScaleLineList.get(i)).floatValue(), (float) i, this.mLineGreenPaint);
            canvas.drawLine(0.0f, (float) i, ((Float) this.mBColorScaleLineList.get(i)).floatValue(), (float) i, this.mLineBluePaint);
        }
        return canvas;
    }

    public void unbind() {
        if (this.mYColorScaleLineList != null) {
            this.mYColorScaleLineList.clear();
        }
        if (this.mRColorScaleLineList != null) {
            this.mRColorScaleLineList.clear();
        }
        if (this.mGColorScaleLineList != null) {
            this.mGColorScaleLineList.clear();
        }
        if (this.mBColorScaleLineList != null) {
            this.mBColorScaleLineList.clear();
        }
    }
}
