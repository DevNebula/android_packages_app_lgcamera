package com.lge.gestureshot.library;

public class HandInfo {
    public int mDetID;
    public int mEvent;
    public int mGestureType;
    public int mHeight;
    public int mMinX;
    public int mMinY;
    public int mWidth;

    public HandInfo() {
        this.mDetID = 0;
        this.mMinX = 0;
        this.mMinY = 0;
        this.mWidth = 0;
        this.mHeight = 0;
        this.mEvent = 0;
        this.mGestureType = 0;
    }

    public HandInfo(int detID, int minX, int minY, int width, int height, int event, int gestureType) {
        this.mDetID = detID;
        this.mMinX = minX;
        this.mMinY = minY;
        this.mWidth = width;
        this.mHeight = height;
        this.mEvent = event;
        this.mGestureType = gestureType;
    }

    public void setHandInfo(HandInfo handInfo) {
        if (handInfo != null) {
            this.mDetID = handInfo.mDetID;
            this.mMinX = handInfo.mMinX;
            this.mMinY = handInfo.mMinY;
            this.mWidth = handInfo.mWidth;
            this.mHeight = handInfo.mHeight;
            this.mEvent = handInfo.mEvent;
            this.mGestureType = handInfo.mGestureType;
        }
    }

    public boolean compareHandInfo(HandInfo handInfo) {
        if (handInfo.mHeight == this.mHeight && handInfo.mMinX == this.mMinX && handInfo.mMinY == this.mMinY && handInfo.mWidth == this.mWidth && handInfo.mEvent == this.mEvent && handInfo.mGestureType == this.mGestureType) {
            return true;
        }
        return false;
    }
}
