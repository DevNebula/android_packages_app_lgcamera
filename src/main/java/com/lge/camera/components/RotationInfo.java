package com.lge.camera.components;

import android.view.animation.AnimationUtils;

public class RotationInfo {
    protected static final int ANIMATION_SPEED = 240;
    protected long mAnimationEndTime = 0;
    protected long mAnimationStartTime = 0;
    protected boolean mClockwise = false;
    protected int mCurrentDegree = 0;
    protected int mStartDegree = 0;
    protected int mTargetDegree = 0;

    public int getCurrentDegree() {
        return this.mCurrentDegree;
    }

    public void setCurrentDegree(int mCurrentDegree) {
        this.mCurrentDegree = mCurrentDegree;
    }

    public int getStartDegree() {
        return this.mStartDegree;
    }

    public void setStartDegree(int mStartDegree) {
        this.mStartDegree = mStartDegree;
    }

    public int getTargetDegree() {
        return this.mTargetDegree;
    }

    public void setTargetDegree(int mTargetDegree) {
        this.mTargetDegree = mTargetDegree;
    }

    public long getAnimationStartTime() {
        return this.mAnimationStartTime;
    }

    public void setAnimationStartTime(long mAnimationStartTime) {
        this.mAnimationStartTime = mAnimationStartTime;
    }

    public long getAnimationEndTime() {
        return this.mAnimationEndTime;
    }

    public void setAnimationEndTime(long mAnimationEndTime) {
        this.mAnimationEndTime = mAnimationEndTime;
    }

    public boolean isClockwise() {
        return this.mClockwise;
    }

    public void setClockwise(boolean mClockwise) {
        this.mClockwise = mClockwise;
    }

    public void setDegree(int degree) {
        setDegree(degree, true);
    }

    public void setDegree(int degree, boolean animation) {
        degree = degree >= 0 ? degree % 360 : (degree % 360) + 360;
        if (degree != this.mTargetDegree) {
            if (animation) {
                this.mTargetDegree = degree;
                this.mStartDegree = this.mCurrentDegree;
            } else {
                this.mCurrentDegree = degree;
                this.mStartDegree = degree;
                this.mTargetDegree = degree;
            }
            this.mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
            int diff = this.mTargetDegree - this.mCurrentDegree;
            if (diff < 0) {
                diff += 360;
            }
            if (diff > 180) {
                diff -= 360;
            }
            this.mClockwise = diff >= 0;
            this.mAnimationEndTime = this.mAnimationStartTime + ((long) ((Math.abs(diff) * 1000) / 240));
        }
    }

    public boolean calcCurrentDegree() {
        long time = AnimationUtils.currentAnimationTimeMillis();
        if (time < this.mAnimationEndTime) {
            int deltaTime = (int) (time - this.mAnimationStartTime);
            int i = this.mStartDegree;
            if (!this.mClockwise) {
                deltaTime = -deltaTime;
            }
            int degree = i + ((deltaTime * 240) / 1000);
            this.mCurrentDegree = degree >= 0 ? degree % 360 : (degree % 360) + 360;
            return true;
        }
        this.mCurrentDegree = this.mTargetDegree;
        return false;
    }
}
