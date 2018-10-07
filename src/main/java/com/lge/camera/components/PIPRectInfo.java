package com.lge.camera.components;

public class PIPRectInfo {
    private int getRextX0;
    private int mHeight;
    private int mPrevRectX0;
    private int mPrevRectX1;
    private int mPrevRectY0;
    private int mPrevRectY1;
    private int mRectX1;
    private int mRectY0;
    private int mRectY1;
    private int mWidth;

    public PIPRectInfo() {
        setPosition(0, 0, 0, 0);
        setPrevPositionUpdate();
    }

    public void setPosition(int x0, int y0, int x1, int y1) {
        this.getRextX0 = x0;
        this.mRectY0 = y0;
        this.mRectX1 = x1;
        this.mRectY1 = y1;
        this.mWidth = this.mRectX1 - this.getRextX0;
        this.mHeight = this.mRectY1 - this.mRectY0;
    }

    public void setPosition(PIPRectInfo rect) {
        setPosition(rect.getRextX0, rect.mRectY0, rect.mRectX1, rect.mRectY1);
    }

    public void setPrevPositionUpdate() {
        this.mPrevRectX0 = this.getRextX0;
        this.mPrevRectY0 = this.mRectY0;
        this.mPrevRectX1 = this.mRectX1;
        this.mPrevRectY1 = this.mRectY1;
    }

    public void restorePosition() {
        setPosition(this.mPrevRectX0, this.mPrevRectY0, this.mPrevRectX1, this.mPrevRectY1);
    }

    public int getRectX0() {
        return this.getRextX0;
    }

    public void setRectX0(int mRectX0) {
        this.getRextX0 = mRectX0;
    }

    public int getRectY0() {
        return this.mRectY0;
    }

    public void setRectY0(int mRectY0) {
        this.mRectY0 = mRectY0;
    }

    public int getRectX1() {
        return this.mRectX1;
    }

    public void setRectX1(int mRectX1) {
        this.mRectX1 = mRectX1;
    }

    public int getRectY1() {
        return this.mRectY1;
    }

    public void setRectY1(int mRectY1) {
        this.mRectY1 = mRectY1;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public void setWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public void setHeight(int mHeight) {
        this.mHeight = mHeight;
    }

    public int getPrevRectX0() {
        return this.mPrevRectX0;
    }

    public void setPrevRectX0(int mPrevRectX0) {
        this.mPrevRectX0 = mPrevRectX0;
    }

    public int getPrevRectY0() {
        return this.mPrevRectY0;
    }

    public void setPrevRectY0(int mPrevRectY0) {
        this.mPrevRectY0 = mPrevRectY0;
    }

    public int getPrevRectX1() {
        return this.mPrevRectX1;
    }

    public void setPrevRectX1(int mPrevRectX1) {
        this.mPrevRectX1 = mPrevRectX1;
    }

    public int getPrevRectY1() {
        return this.mPrevRectY1;
    }

    public void setPrevRectY1(int mPrevRectY1) {
        this.mPrevRectY1 = mPrevRectY1;
    }
}
