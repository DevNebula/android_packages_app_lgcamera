package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;
import com.lge.camera.C0088R;
import com.lge.camera.util.Utils;

public class ShiftImageSpan extends ImageSpan {
    private float mShiftUp;

    public ShiftImageSpan(Context mContext, Drawable b) {
        this(mContext, b, 1);
    }

    public ShiftImageSpan(Context mContext, Drawable b, int verticalAlignment) {
        super(b, verticalAlignment);
        this.mShiftUp = (float) Utils.getPx(mContext, C0088R.dimen.default_image_span_shift_up);
    }

    public ShiftImageSpan(Drawable b, float shiftUp) {
        this(b, 1, shiftUp);
    }

    public ShiftImageSpan(Drawable b, int verticalAlignment, float shiftUp) {
        super(b, verticalAlignment);
        this.mShiftUp = shiftUp;
    }

    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        Drawable b = getDrawable();
        if (b != null) {
            canvas.save();
            canvas.translate(x, (float) ((int) (((float) (bottom - b.getBounds().bottom)) - this.mShiftUp)));
            b.draw(canvas);
            canvas.restore();
        }
    }
}
