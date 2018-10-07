package com.lge.camera.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.RelativeLayout;
import com.lge.camera.components.LayoutChangeNotifier.Listener;

public class PreviewFrameLayout extends RelativeLayout implements LayoutChangeNotifier {
    private double mAspectRatio;
    private LayoutChangeHelper mLayoutChangeHelper = new LayoutChangeHelper(this);
    private OnSizeChangedListener mListener;

    public interface OnSizeChangedListener {
        void onSizeChanged(int i, int i2);
    }

    public PreviewFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAspectRatio(1.3333333333333333d);
    }

    protected void onFinishInflate() {
    }

    public void setAspectRatio(double ratio) {
        if (ratio <= 0.0d) {
            throw new IllegalArgumentException();
        }
        if (getResources().getConfiguration().orientation == 1) {
            ratio = 1.0d / ratio;
        }
        if (Double.compare(this.mAspectRatio, ratio) != 0) {
            this.mAspectRatio = ratio;
            requestLayout();
        }
    }

    protected void onMeasure(int widthSpec, int heightSpec) {
        int hPadding = getPaddingStart() + getPaddingEnd();
        int vPadding = getPaddingTop() + getPaddingBottom();
        super.onMeasure(MeasureSpec.makeMeasureSpec((MeasureSpec.getSize(widthSpec) - hPadding) + hPadding, 1073741824), MeasureSpec.makeMeasureSpec((MeasureSpec.getSize(heightSpec) - vPadding) + vPadding, 1073741824));
    }

    public void setOnSizeChangedListener(OnSizeChangedListener listener) {
        this.mListener = listener;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (this.mListener != null) {
            this.mListener.onSizeChanged(w, h);
        }
    }

    public void setOnLayoutChangeListener(Listener listener) {
        this.mLayoutChangeHelper.setOnLayoutChangeListener(listener);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mLayoutChangeHelper.layout(changed, l, t, r, b);
    }

    public void unbind() {
        this.mListener = null;
        this.mLayoutChangeHelper = null;
    }
}
