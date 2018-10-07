package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.util.Utils;

public class SettingVerticalDivider extends View {
    private Context mContext;
    private boolean mIsInitialized = false;
    private Paint mPaint = new Paint();

    public SettingVerticalDivider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }

    public SettingVerticalDivider(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public SettingVerticalDivider(Context context) {
        super(context);
        this.mContext = context;
    }

    public void onDraw(Canvas canvas) {
        if (canvas != null) {
            int color;
            if (!this.mIsInitialized) {
                init();
                this.mIsInitialized = true;
            }
            if (isEnabled()) {
                color = this.mContext.getResources().getColor(C0088R.color.list_divider_color);
            } else {
                color = this.mContext.getResources().getColor(C0088R.color.list_divider_color_disabled);
            }
            this.mPaint.setColor(color);
            canvas.drawLine(0.0f, 0.0f, 0.0f, (float) getHeight(), this.mPaint);
        }
    }

    private void init() {
        if (this.mPaint == null) {
            this.mPaint = new Paint();
        }
        this.mPaint.setStrokeWidth((float) Utils.getPx(this.mContext, C0088R.dimen.setting_vertical_divider.width));
    }
}
