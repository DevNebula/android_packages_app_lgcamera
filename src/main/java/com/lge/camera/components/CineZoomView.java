package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.util.Utils;

public class CineZoomView extends View {
    protected final int LEFT_ROUND = 1;
    protected final int NONE = 0;
    protected final int RIGHT_ROUND = 2;
    protected final int TOP_ROUND = 3;
    protected int mBOTTOM_MAX_BOUND = 2560;
    protected NinePatchDrawable mCineZoomPauseFullView;
    protected NinePatchDrawable mCineZoomPauseLeftView;
    protected NinePatchDrawable mCineZoomPauseRightView;
    protected NinePatchDrawable mCineZoomPauseView;
    protected NinePatchDrawable mCineZoomPlayFullView;
    protected NinePatchDrawable mCineZoomPlayLeftView;
    protected NinePatchDrawable mCineZoomPlayRightView;
    protected NinePatchDrawable mCineZoomPlayView;
    protected Rect mDst = new Rect();
    private boolean mIsPlaying = true;
    protected Rect mLeftEdge;
    protected int mMARGIN = 30;
    protected Rect mRightEdge;
    protected int mSCREEN_WIDTH = CameraConstantsEx.QHD_SCREEN_RESOLUTION;
    protected Rect mSrc = new Rect();
    private int mStatus = 0;

    public CineZoomView(Context context) {
        super(context);
        this.mContext = context;
        this.mCineZoomPlayView = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.camera_cine_zoom_play);
        this.mCineZoomPlayLeftView = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.camera_cine_zoom_left_play);
        this.mCineZoomPlayRightView = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.camera_cine_zoom_right_play);
        this.mCineZoomPlayFullView = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.camera_cine_zoom_full_play);
        this.mCineZoomPauseView = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.camera_cine_zoom_pause);
        this.mCineZoomPauseLeftView = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.camera_cine_zoom_left_pause);
        this.mCineZoomPauseRightView = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.camera_cine_zoom_right_pause);
        this.mCineZoomPauseFullView = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.camera_cine_zoom_full_pause);
        initConstants(context);
    }

    private void initConstants(Context context) {
        int[] lcdSize = Utils.getLCDsize(context, true);
        this.mSCREEN_WIDTH = lcdSize[1];
        this.mBOTTOM_MAX_BOUND = (lcdSize[1] * 16) / 9;
        this.mMARGIN = this.mSCREEN_WIDTH / 48;
        this.mLeftEdge = new Rect(-this.mMARGIN, -this.mMARGIN, this.mMARGIN, this.mMARGIN);
        this.mRightEdge = new Rect(this.mSCREEN_WIDTH - this.mMARGIN, -this.mMARGIN, this.mSCREEN_WIDTH + this.mMARGIN, this.mMARGIN);
    }

    public void setIsPlaying(boolean isPlaying) {
        this.mIsPlaying = isPlaying;
    }

    public void setTarget(Rect src, Rect dst) {
        if (this.mSrc != null && this.mDst != null) {
            this.mSrc.set(src);
            this.mDst.set(dst);
        }
    }

    public void setBounds(int l, int t, int r, int b) {
        if (this.mSrc != null && this.mDst != null && this.mLeftEdge != null && this.mRightEdge != null && this.mCineZoomPlayView != null && this.mCineZoomPlayLeftView != null && this.mCineZoomPlayRightView != null && this.mCineZoomPlayFullView != null && this.mCineZoomPauseView != null && this.mCineZoomPauseLeftView != null && this.mCineZoomPauseRightView != null && this.mCineZoomPauseFullView != null) {
            int left = (this.mSrc.left + this.mDst.left) - l;
            int top = (this.mSrc.top + this.mDst.top) - t;
            int right = (this.mSrc.right + this.mDst.right) - r;
            int bottom = (this.mSrc.bottom + this.mDst.bottom) - b;
            if (bottom > this.mBOTTOM_MAX_BOUND) {
                bottom = this.mBOTTOM_MAX_BOUND;
            }
            if (this.mLeftEdge.contains(left, top) && this.mRightEdge.contains(right, top)) {
                this.mStatus = 3;
                this.mCineZoomPlayFullView.setBounds(left, top, right, bottom);
                this.mCineZoomPlayView.setVisible(false, false);
                this.mCineZoomPlayLeftView.setVisible(false, false);
                this.mCineZoomPlayRightView.setVisible(false, false);
                this.mCineZoomPlayFullView.setVisible(true, false);
                this.mCineZoomPauseFullView.setBounds(left, top, right, bottom);
                this.mCineZoomPauseView.setVisible(false, false);
                this.mCineZoomPauseLeftView.setVisible(false, false);
                this.mCineZoomPauseRightView.setVisible(false, false);
                this.mCineZoomPauseFullView.setVisible(true, false);
            } else if (this.mLeftEdge.contains(left, top)) {
                this.mStatus = 1;
                this.mCineZoomPlayLeftView.setBounds(left, top, right, bottom);
                this.mCineZoomPlayView.setVisible(false, false);
                this.mCineZoomPlayLeftView.setVisible(true, false);
                this.mCineZoomPlayRightView.setVisible(false, false);
                this.mCineZoomPlayFullView.setVisible(false, false);
                this.mCineZoomPauseLeftView.setBounds(left, top, right, bottom);
                this.mCineZoomPauseView.setVisible(false, false);
                this.mCineZoomPauseLeftView.setVisible(true, false);
                this.mCineZoomPauseRightView.setVisible(false, false);
                this.mCineZoomPauseFullView.setVisible(false, false);
            } else if (this.mRightEdge.contains(right, top)) {
                this.mStatus = 2;
                this.mCineZoomPlayRightView.setBounds(left, top, right, bottom);
                this.mCineZoomPlayView.setVisible(false, false);
                this.mCineZoomPlayLeftView.setVisible(false, false);
                this.mCineZoomPlayRightView.setVisible(true, false);
                this.mCineZoomPlayFullView.setVisible(false, false);
                this.mCineZoomPauseRightView.setBounds(left, top, right, bottom);
                this.mCineZoomPauseView.setVisible(false, false);
                this.mCineZoomPauseLeftView.setVisible(false, false);
                this.mCineZoomPauseRightView.setVisible(true, false);
                this.mCineZoomPauseFullView.setVisible(false, false);
            } else {
                this.mStatus = 0;
                this.mCineZoomPlayView.setBounds(left, top, right, bottom);
                this.mCineZoomPlayView.setVisible(true, false);
                this.mCineZoomPlayLeftView.setVisible(false, false);
                this.mCineZoomPlayRightView.setVisible(false, false);
                this.mCineZoomPlayFullView.setVisible(false, false);
                this.mCineZoomPauseView.setBounds(left, top, right, bottom);
                this.mCineZoomPauseView.setVisible(true, false);
                this.mCineZoomPauseLeftView.setVisible(false, false);
                this.mCineZoomPauseRightView.setVisible(false, false);
                this.mCineZoomPauseFullView.setVisible(false, false);
            }
        }
    }

    protected void onDraw(Canvas canvas) {
        if (this.mCineZoomPlayView == null || this.mCineZoomPlayLeftView == null || this.mCineZoomPlayRightView == null || this.mCineZoomPlayFullView == null || this.mCineZoomPauseView == null || this.mCineZoomPauseLeftView == null || this.mCineZoomPauseRightView == null || this.mCineZoomPauseFullView != null) {
        }
    }
}
