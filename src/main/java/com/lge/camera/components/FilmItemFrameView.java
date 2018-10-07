package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import com.lge.camera.C0088R;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.Utils;

public class FilmItemFrameView extends View {
    private Context mContext;
    private String mFilmName;
    private String mFilmValue;
    private int mFontSize;
    private Paint mFramePaint = new Paint();
    private Rect mFrameRect = new Rect();
    private int mNormalRectStrokeWidth;
    private int mSelectRectStrokeWidth;
    private int mTextMarginBottom;
    private int mTextMarginEnd;
    private Paint mTextPaint = new Paint();
    protected int mViewDegree = 0;

    public FilmItemFrameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FilmItemFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FilmItemFrameView(Context context) {
        super(context);
    }

    public void initFilmItemFrame(Context c, String filmName, String entryValue, int degree) {
        this.mContext = c;
        this.mTextMarginEnd = (int) Utils.dpToPx(c, 5.0f);
        this.mTextMarginBottom = (int) Utils.dpToPx(c, 5.0f);
        this.mNormalRectStrokeWidth = (int) Utils.dpToPx(this.mContext, 0.7f);
        this.mSelectRectStrokeWidth = (int) Utils.dpToPx(this.mContext, 1.5f);
        this.mFilmName = filmName;
        this.mFilmValue = entryValue;
        this.mFontSize = Utils.getPx(c, ModelProperties.isTablet(c) ? C0088R.dimen.film_menu_font_size_tablet : C0088R.dimen.film_menu_font_size);
        this.mViewDegree = degree;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec));
    }

    private int measure(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == 1073741824) {
            return size;
        }
        return Math.min(0, size);
    }

    protected void onDraw(Canvas canvas) {
        int rectStrokeMargin;
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setTextSize((float) this.mFontSize);
        this.mTextPaint.setTypeface(Typeface.create((String) null, 1));
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int startX = 0;
        int startY = 0;
        int textWidth = (int) this.mTextPaint.measureText(this.mFilmName);
        canvas.save();
        if (this.mViewDegree == 270) {
            canvas.rotate(90.0f, ((float) width) / 2.0f, ((float) height) / 2.0f);
            startX = (width - height) / 2;
            startY = (height - width) / 2;
            width = getMeasuredHeight();
            height = getMeasuredWidth();
        } else if (this.mViewDegree == 90) {
            canvas.rotate(270.0f, (float) (width / 2), (float) (height / 2));
            startX = (width - height) / 2;
            startY = (height - width) / 2;
            width = getMeasuredHeight();
            height = getMeasuredWidth();
        } else if (this.mViewDegree == 180) {
            canvas.rotate(180.0f, (float) (width / 2), (float) (height / 2));
        }
        this.mTextPaint.setStyle(Style.STROKE);
        this.mTextPaint.setStrokeWidth(Utils.dpToPx(this.mContext, 0.5f));
        this.mTextPaint.setColor(Color.parseColor("#cc000000"));
        canvas.drawText(this.mFilmName, (float) (((startX + width) - textWidth) - this.mTextMarginEnd), (float) ((startY + height) - this.mTextMarginBottom), this.mTextPaint);
        this.mTextPaint.setStyle(Style.FILL);
        this.mFramePaint.setStyle(Style.STROKE);
        if (isSelected()) {
            this.mFramePaint.setColor(Color.parseColor("#4bdbbe"));
            this.mFramePaint.setStrokeWidth((float) this.mSelectRectStrokeWidth);
            this.mTextPaint.setColor(Color.parseColor("#4bdbbe"));
            rectStrokeMargin = this.mSelectRectStrokeWidth;
        } else {
            this.mFramePaint.setColor(Color.parseColor("#000000"));
            this.mFramePaint.setStrokeWidth((float) this.mNormalRectStrokeWidth);
            this.mTextPaint.setColor(Color.parseColor("#ffffff"));
            rectStrokeMargin = this.mNormalRectStrokeWidth;
        }
        rectStrokeMargin = (int) (((float) rectStrokeMargin) / 2.0f);
        this.mFrameRect.set(startX + rectStrokeMargin, startY + rectStrokeMargin, (startX + width) - rectStrokeMargin, (startY + height) - rectStrokeMargin);
        canvas.drawRect(this.mFrameRect, this.mFramePaint);
        canvas.drawText(this.mFilmName, (float) (((startX + width) - textWidth) - this.mTextMarginEnd), (float) ((startY + height) - this.mTextMarginBottom), this.mTextPaint);
        canvas.restore();
    }

    public String getFilmValue() {
        return this.mFilmValue;
    }

    public void setViewDegree(int viewDegree) {
        if (this.mViewDegree != viewDegree) {
            postInvalidate();
        }
        this.mViewDegree = viewDegree;
        invalidate();
    }

    public void unbind() {
        this.mFrameRect = null;
        this.mTextPaint = null;
    }
}
