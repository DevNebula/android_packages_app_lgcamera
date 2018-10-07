package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.util.Utils;

public class WaveView extends View {
    private final int COORDINATE_UNIT_COUNT = 4;
    private final float LMT_0_RATIO = 0.99f;
    private final float LMT_3_RATIO = 0.7109f;
    private final float LMT_6_RATIO = 0.5078f;
    private final float MAX_MONO = 32767.0f;
    private final float MAX_STEREO = 128.0f;
    protected CircularQueue mAmplitudeQueue = null;
    private Paint mBaseLinePaint;
    private boolean mIsRecording = false;
    private Paint mLmtLintPaint;
    private float mLmtRatio = 0.0f;
    protected float[] mWaveCoordinateArray = null;
    private float mWaveLineLimit = 0.0f;
    private Paint mWavePaint;
    public float mWaveXTranslation;

    public WaveView(Context context) {
        super(context);
        initWaveView(context);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWaveView(context);
    }

    private void initWaveView(Context context) {
        this.mWaveXTranslation = context.getResources().getDimension(C0088R.dimen.wave_space_dimen);
        this.mBaseLinePaint = new Paint(1);
        this.mBaseLinePaint.setStyle(Style.STROKE);
        this.mBaseLinePaint.setStrokeWidth((float) Utils.getPx(context, C0088R.dimen.wave_baseline_width));
        this.mBaseLinePaint.setColor(context.getResources().getColor(C0088R.color.wave_baseline_color, null));
        this.mWavePaint = new Paint(1);
        this.mWavePaint.setStyle(Style.STROKE);
        this.mWavePaint.setStrokeWidth(2.0f);
        this.mWavePaint.setColor(context.getResources().getColor(C0088R.color.manual_wave_line, null));
        this.mWavePaint.setAntiAlias(true);
        this.mLmtLintPaint = new Paint(1);
        this.mLmtLintPaint.setStyle(Style.STROKE);
        this.mLmtLintPaint.setStrokeWidth(3.0f);
        this.mLmtLintPaint.setColor(context.getResources().getColor(C0088R.color.wave_redline_color, null));
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawLine(0.0f, (float) (getHeight() / 2), (float) getWidth(), (float) (getHeight() / 2), this.mBaseLinePaint);
        if (this.mIsRecording) {
            canvas.drawLines(this.mWaveCoordinateArray, this.mWavePaint);
            drawLmtLine(canvas);
        }
        super.onDraw(canvas);
    }

    public void drawLmtLine(String dB) {
        if ("off".equals(dB)) {
            this.mLmtRatio = 0.0f;
            return;
        }
        switch (Math.abs(Integer.valueOf(dB).intValue())) {
            case 0:
                this.mLmtRatio = 0.00999999f;
                return;
            case 3:
                this.mLmtRatio = 0.2891f;
                return;
            case 6:
                this.mLmtRatio = 0.49220002f;
                return;
            default:
                return;
        }
    }

    private void drawLmtLine(Canvas canvas) {
        if (this.mLmtRatio != 0.0f) {
            this.mWaveLineLimit = ((float) (getHeight() / 2)) * this.mLmtRatio;
            canvas.drawLine(0.0f, this.mWaveLineLimit, (float) getWidth(), this.mWaveLineLimit, this.mLmtLintPaint);
        }
    }

    public void drawWave(int amplitude, int soundType) {
        this.mAmplitudeQueue.add(Float.valueOf(getMeteredWaveHeight(amplitude, soundType)));
        makeWaveArray();
    }

    private void makeWaveArray() {
        int qSize = this.mAmplitudeQueue.size() - 1;
        while (qSize > -1 && qSize < getWidth()) {
            Object queue = this.mAmplitudeQueue.get(qSize);
            float[] fArr = this.mWaveCoordinateArray;
            int i = qSize * 4;
            float f = ((float) qSize) * this.mWaveXTranslation;
            this.mWaveCoordinateArray[(qSize * 4) + 2] = f;
            fArr[i] = f;
            this.mWaveCoordinateArray[(qSize * 4) + 1] = (((float) getHeight()) - ((Float) queue).floatValue()) / 2.0f;
            this.mWaveCoordinateArray[(qSize * 4) + 3] = ((((float) getHeight()) - ((Float) queue).floatValue()) / 2.0f) + ((Float) queue).floatValue();
            qSize--;
        }
        invalidate();
    }

    public void startDrawingWave() {
        int width = (int) (((float) getWidth()) / this.mWaveXTranslation);
        if (!this.mIsRecording) {
            this.mIsRecording = true;
            if (this.mAmplitudeQueue == null) {
                this.mAmplitudeQueue = new CircularQueue(width);
            }
            if (this.mWaveCoordinateArray == null) {
                this.mWaveCoordinateArray = new float[(width * 4)];
            }
            for (int arrayindex = 0; arrayindex < this.mWaveCoordinateArray.length; arrayindex++) {
                this.mWaveCoordinateArray[arrayindex] = 0.0f;
            }
        }
    }

    public void stopDrawingWave() {
        this.mIsRecording = false;
        this.mWaveCoordinateArray = null;
        if (this.mAmplitudeQueue != null) {
            this.mAmplitudeQueue.clear();
        }
        this.mAmplitudeQueue = null;
        clearAnimation();
        invalidate();
    }

    private float getMeteredWaveHeight(int amplitude, int soundType) {
        return ((float) (getHeight() * amplitude)) / (soundType == 1 ? 32767.0f : 128.0f);
    }
}
