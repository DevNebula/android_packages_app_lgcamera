package com.lge.camera.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.util.Utils;

public class PIPResizeHandlerView extends View {
    private int mLeftTopX = 0;
    private int mLeftTopY = 0;
    private int mMovingEdges = 0;
    private Paint mPaint = new Paint();
    private Rect mRect = new Rect();
    private Bitmap mResizeHandler_Bottom_Left = null;
    private Bitmap mResizeHandler_Bottom_Right = null;
    private Bitmap mResizeHandler_Top_Left = null;
    private Bitmap mResizeHandler_Top_Right = null;
    private int mRightBottomX = 0;
    private int mRightBottomY = 0;

    public PIPResizeHandlerView(Context context) {
        super(context);
    }

    public PIPResizeHandlerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public PIPResizeHandlerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PIPResizeHandlerView(Context context, int x0, int y0, int x1, int y1) {
        super(context);
        setPosition(x0, y0, x1, y1);
    }

    public void unbind() {
        this.mRect = null;
        this.mPaint = null;
        if (this.mResizeHandler_Bottom_Left != null) {
            this.mResizeHandler_Bottom_Left.recycle();
            this.mResizeHandler_Bottom_Left = null;
        }
        if (this.mResizeHandler_Bottom_Right != null) {
            this.mResizeHandler_Bottom_Right.recycle();
            this.mResizeHandler_Bottom_Right = null;
        }
        if (this.mResizeHandler_Top_Left != null) {
            this.mResizeHandler_Top_Left.recycle();
            this.mResizeHandler_Top_Left = null;
        }
        if (this.mResizeHandler_Top_Right != null) {
            this.mResizeHandler_Top_Right.recycle();
            this.mResizeHandler_Top_Right = null;
        }
    }

    public void setPosition(int x0, int y0, int x1, int y1) {
        this.mLeftTopX = x0;
        this.mLeftTopY = y0;
        this.mRightBottomX = x1;
        this.mRightBottomY = y1;
        if (this.mResizeHandler_Bottom_Left == null) {
            this.mResizeHandler_Bottom_Left = BitmapFactory.decodeResource(getResources(), C0088R.drawable.sub_window_handler_left_bottom);
        }
        if (this.mResizeHandler_Bottom_Right == null) {
            this.mResizeHandler_Bottom_Right = BitmapFactory.decodeResource(getResources(), C0088R.drawable.sub_window_handler_right_bottom);
        }
        if (this.mResizeHandler_Top_Left == null) {
            this.mResizeHandler_Top_Left = BitmapFactory.decodeResource(getResources(), C0088R.drawable.sub_window_handler_left_top);
        }
        if (this.mResizeHandler_Top_Right == null) {
            this.mResizeHandler_Top_Right = BitmapFactory.decodeResource(getResources(), C0088R.drawable.sub_window_handler_right_top);
        }
    }

    public void updatePosition(int direction) {
        this.mMovingEdges = direction;
        this.mRect.set(this.mLeftTopX, this.mLeftTopY, this.mRightBottomX, this.mRightBottomY);
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!(this.mPaint == null || this.mRect == null)) {
            int subWindowHandlerThick = (int) Utils.dpToPx(getContext(), 3.0f);
            this.mPaint.setARGB(255, 75, 219, 190);
            this.mPaint.setStyle(Style.STROKE);
            this.mPaint.setStrokeWidth((float) subWindowHandlerThick);
            this.mPaint.setTextSize(22.0f);
            this.mPaint.setAntiAlias(true);
            this.mRect.set(this.mLeftTopX, this.mLeftTopY, this.mRightBottomX, this.mRightBottomY);
            canvas.drawRect(this.mRect, this.mPaint);
        }
        drawResizeHandlerCorner(canvas);
    }

    private void drawResizeHandlerCorner(Canvas canvas) {
        if (this.mResizeHandler_Bottom_Left != null && this.mResizeHandler_Bottom_Right != null && this.mResizeHandler_Top_Left != null && this.mResizeHandler_Top_Right != null && !this.mResizeHandler_Bottom_Left.isRecycled() && !this.mResizeHandler_Bottom_Right.isRecycled() && !this.mResizeHandler_Top_Left.isRecycled() && !this.mResizeHandler_Top_Right.isRecycled()) {
            int subWindowHandlerThick = Math.round(Utils.dpToPx(getContext(), 3.0f) / 2.0f);
            boolean notMoving = this.mMovingEdges == 0;
            if (!((this.mMovingEdges & 8) == 0 || (this.mMovingEdges & 1) == 0) || notMoving) {
                canvas.drawBitmap(this.mResizeHandler_Bottom_Left, (float) (this.mLeftTopX - subWindowHandlerThick), (float) ((this.mRightBottomY - this.mResizeHandler_Bottom_Left.getHeight()) + subWindowHandlerThick), null);
            }
            if (!((this.mMovingEdges & 8) == 0 || (this.mMovingEdges & 4) == 0) || notMoving) {
                canvas.drawBitmap(this.mResizeHandler_Bottom_Right, (float) ((this.mRightBottomX - this.mResizeHandler_Bottom_Right.getWidth()) + subWindowHandlerThick), (float) ((this.mRightBottomY - this.mResizeHandler_Bottom_Right.getHeight()) + subWindowHandlerThick), null);
            }
            if (!((this.mMovingEdges & 2) == 0 || (this.mMovingEdges & 1) == 0) || notMoving) {
                canvas.drawBitmap(this.mResizeHandler_Top_Left, (float) (this.mLeftTopX - subWindowHandlerThick), (float) (this.mLeftTopY - subWindowHandlerThick), null);
            }
            if (((this.mMovingEdges & 2) != 0 && (this.mMovingEdges & 4) != 0) || notMoving) {
                canvas.drawBitmap(this.mResizeHandler_Top_Right, (float) ((this.mRightBottomX - this.mResizeHandler_Top_Right.getWidth()) + subWindowHandlerThick), (float) (this.mLeftTopY - subWindowHandlerThick), null);
            }
        }
    }

    public Rect getCurrentPosition() {
        return this.mRect;
    }
}
