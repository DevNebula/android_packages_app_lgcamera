package com.lge.camera.components;

public class AnimateItem {
    private int mAniTypeX = -1;
    private int mAniTypeY = -1;
    private long mId = 0;
    private int mIndex = 0;

    public AnimateItem(long id, int index, int typeX, int typeY) {
        this.mId = id;
        this.mIndex = index;
        this.mAniTypeX = typeX;
        this.mAniTypeY = typeY;
    }

    public long getId() {
        return this.mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public int getIndex() {
        return this.mIndex;
    }

    public void setIndex(int index) {
        this.mIndex = index;
    }

    public int getAniTypeX() {
        return this.mAniTypeX;
    }

    public void setAniTypeX(int typeX) {
        this.mAniTypeX = typeX;
    }

    public int getAniTypeY() {
        return this.mAniTypeY;
    }

    public void setAniTypeY(int typeY) {
        this.mAniTypeY = typeY;
    }
}
