package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.lge.camera.C0088R;
import com.lge.camera.util.Utils;
import java.util.Stack;

public class SnapMovieSeekBar extends ImageView {
    public static final int MODE_ADD = 2;
    public static final int MODE_EDIT = 1;
    public static final int MODE_NORMAL = 0;
    public static final int SELECTED_NONE = -1;
    private int mAddStart = 0;
    private int mCur = 0;
    private int mCurWidth = 0;
    private int mDisabledSize = 0;
    private int mDisabledTime = 0;
    private int mDisabledWidth = 0;
    private int mHeight = 0;
    private int mMax = 100;
    private int mMode = 0;
    private int mOutlineWidth = 0;
    private Paint mPaintBg = new Paint();
    private Paint mPaintCurTime = new Paint();
    private Paint mPaintDisabled = new Paint();
    private Paint mPaintOutline = new Paint();
    private Paint mPaintOutline2 = new Paint();
    private Paint mPaintSelected = new Paint();
    private Paint mPaintSelectedOutline = new Paint();
    private Paint mPaintSeparator = new Paint();
    private int mSelectedIndex = -1;
    private int mSelectedOutlineWidth;
    private Stack<Integer> mSeparators = new Stack();
    private int mWidth = 0;

    public SnapMovieSeekBar(Context context) {
        super(context);
    }

    public SnapMovieSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(Context context) {
        this.mPaintBg.setColor(Color.argb(30, 41, 41, 41));
        this.mPaintCurTime.setColor(Color.rgb(250, 250, 250));
        int colorAccent = context.getResources().getColor(C0088R.color.camera_accent_txt);
        this.mPaintSelected.setColor(colorAccent);
        this.mSelectedOutlineWidth = Utils.getPx(context, C0088R.dimen.snap_movie_seek_bar_selected_outline_width);
        this.mPaintSelectedOutline.setColor(colorAccent);
        this.mPaintSelectedOutline.setStrokeWidth((float) this.mSelectedOutlineWidth);
        int separatorWidth = Utils.getPx(context, C0088R.dimen.snap_movie_seek_bar_separator_width);
        this.mPaintSeparator.setColor(Color.rgb(38, 38, 38));
        this.mPaintSeparator.setStrokeWidth((float) separatorWidth);
        int outlineWidth = Utils.getPx(context, C0088R.dimen.snap_movie_seek_bar_outline_width);
        this.mPaintOutline.setColor(Color.rgb(255, 255, 255));
        this.mPaintOutline.setStrokeWidth((float) outlineWidth);
        int outlineWidth2 = Utils.getPx(context, C0088R.dimen.snap_movie_seek_bar_outline2_width);
        if (outlineWidth2 < 1) {
            outlineWidth2 = 1;
        }
        this.mOutlineWidth = outlineWidth + outlineWidth2;
        this.mPaintOutline2.setColor(Color.rgb(0, 0, 0));
        this.mPaintOutline2.setStrokeWidth((float) outlineWidth2);
        this.mPaintDisabled.setColor(Color.rgb(0, 0, 0));
    }

    public int getOutlineWidth() {
        return this.mOutlineWidth;
    }

    public void setCurrentTime(int value) {
        this.mCur = value;
        this.mCurWidth = convertTimeToPos(this.mCur);
        if (value <= 0) {
            this.mSeparators.clear();
            this.mSelectedIndex = -1;
            this.mDisabledSize = 0;
            this.mDisabledWidth = 0;
        }
        invalidate();
    }

    public float getCurrentTime() {
        return (float) this.mCur;
    }

    public int getCurrentWidth() {
        if (this.mCur > 0 && this.mCurWidth <= 0) {
            this.mCurWidth = convertTimeToPos(this.mCur);
        }
        return this.mCurWidth;
    }

    public void setDisabledWidth(int size, int currentTime) {
        this.mDisabledSize = size;
        this.mDisabledTime = currentTime;
        this.mDisabledWidth = convertTimeToPos(currentTime);
    }

    public int getDisabledWidth() {
        return this.mDisabledWidth;
    }

    public int getDisabledSize() {
        return this.mDisabledSize;
    }

    public void setMaxTime(int value) {
        this.mMax = value;
        this.mCurWidth = convertTimeToPos(this.mCur);
        this.mDisabledWidth = convertTimeToPos(this.mDisabledTime);
        invalidate();
    }

    public int getMaxTime() {
        return this.mMax;
    }

    public void addSeparator(int value) {
        this.mSeparators.push(Integer.valueOf(value));
        invalidate();
    }

    public void removeSeparator(int index) {
        this.mCur -= ((Integer) this.mSeparators.remove(index)).intValue();
        this.mCurWidth = convertTimeToPos(this.mCur);
        setSelectedIndex(index - 1);
    }

    public void removeLastSeparator() {
        if (this.mSeparators.size() >= 1) {
            this.mSeparators.pop();
            invalidate();
        }
    }

    public int convertTimeToPos(int time) {
        return (int) (((float) getWidth()) * (((float) time) / ((float) this.mMax)));
    }

    public void setSelectedIndex(int index) {
        if (index < 0) {
            index = -1;
        }
        this.mSelectedIndex = index;
        invalidate();
    }

    public int setSelectedIndexByPos(int x) {
        this.mSelectedIndex = getIndexByPos(x);
        invalidate();
        return this.mSelectedIndex;
    }

    public int getSelectedIndex() {
        return this.mSelectedIndex;
    }

    public int getIndexByPos(int x) {
        int curTime = 0;
        for (int i = 0; i < this.mSeparators.size(); i++) {
            int duration = ((Integer) this.mSeparators.get(i)).intValue();
            curTime += duration;
            float curPos = (float) convertTimeToPos(curTime);
            if (x >= ((int) (curPos - ((float) convertTimeToPos(duration)))) && ((float) x) <= curPos) {
                return i;
            }
        }
        return -1;
    }

    public int getLastIndex() {
        int size = this.mSeparators.size();
        return size > 0 ? size - 1 : 0;
    }

    public float getSeparatorWidth() {
        return this.mPaintSeparator.getStrokeWidth();
    }

    public void setMode(int mode) {
        this.mMode = mode;
        this.mAddStart = this.mMode == 2 ? this.mCurWidth : 0;
        invalidate();
    }

    public int getMode() {
        return this.mMode;
    }

    private void drawRectSelected(Canvas canvas, int start, int end) {
        canvas.drawRect((float) start, 0.0f, (float) end, (float) this.mHeight, this.mPaintSelected);
        int wh = this.mSelectedOutlineWidth / 2;
        int left = start;
        int right = end;
        int top = wh;
        int bottom = this.mHeight - wh;
        canvas.drawLines(new float[]{(float) left, (float) top, (float) right, (float) top, (float) right, (float) top, (float) right, (float) bottom, (float) right, (float) bottom, (float) left, (float) bottom, (float) left, (float) bottom, (float) left, (float) top}, this.mPaintSelectedOutline);
    }

    protected void onDraw(Canvas canvas) {
        this.mWidth = getWidth();
        this.mHeight = getHeight();
        float curTime = ((float) this.mWidth) * (((float) this.mCur) / ((float) this.mMax));
        canvas.drawRect(0.0f, 0.0f, (float) this.mWidth, (float) this.mHeight, this.mPaintBg);
        canvas.drawLines(new float[]{0.0f, 0.0f, (float) this.mWidth, 0.0f, (float) this.mWidth, 0.0f, (float) this.mWidth, (float) this.mHeight, (float) this.mWidth, (float) this.mHeight, 0.0f, (float) this.mHeight, 0.0f, (float) this.mHeight, 0.0f, 0.0f}, this.mPaintOutline);
        canvas.drawRect(0.0f, 0.0f, curTime, (float) this.mHeight, this.mPaintCurTime);
        if (this.mMode == 2) {
            drawRectSelected(canvas, this.mAddStart, (int) curTime);
        }
        if (this.mDisabledWidth > 0) {
            int outlineWidth = this.mSelectedOutlineWidth / 2;
            canvas.drawRect((float) (outlineWidth + 0), (float) (outlineWidth + 0), (float) (this.mDisabledWidth - outlineWidth), (float) (this.mHeight - outlineWidth), this.mPaintDisabled);
        }
        int i = 0;
        float curPos = 0.0f;
        while (i < this.mSeparators.size()) {
            float durationPos = ((float) this.mWidth) * (((float) ((Integer) this.mSeparators.get(i)).intValue()) / ((float) this.mMax));
            curPos += durationPos;
            canvas.drawLine(curPos, 0.0f, curPos, (float) this.mHeight, this.mPaintSeparator);
            if (this.mMode == 1 && this.mSelectedIndex == i) {
                int start = (int) (curPos - durationPos);
                if (this.mSelectedIndex > 0) {
                    start = (int) (((float) start) + (this.mPaintSeparator.getStrokeWidth() / 2.0f));
                }
                drawRectSelected(canvas, start, (int) curPos);
            }
            i++;
        }
        canvas.drawLines(new float[]{0.0f, 0.0f, (float) this.mWidth, 0.0f, (float) this.mWidth, 0.0f, (float) this.mWidth, (float) this.mHeight, (float) this.mWidth, (float) this.mHeight, 0.0f, (float) this.mHeight, 0.0f, (float) this.mHeight, 0.0f, 0.0f}, this.mPaintOutline2);
        super.onDraw(canvas);
    }
}
