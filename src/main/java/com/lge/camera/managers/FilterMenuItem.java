package com.lge.camera.managers;

public class FilterMenuItem {
    public String mContentDesc;
    public int mDegree = 0;
    public int mEndMargin;
    public String mEntry;
    public String mEntryValue;
    public int mFilterMenuBottomMargin;
    public int mHeight;
    public int mHorizontalGap;
    public boolean mIsDownlodaded = false;
    public boolean mIsRTL = false;
    public int mItemCntPerRow = 5;
    public int mItemPosition = 0;
    public int mLUTNumber = 0;
    public int[] mLcdSize;
    public int mMoveX;
    public int mMoveY;
    public int mNextPosition = 0;
    public int mStartMargin;
    public int mTopMargin;
    public int mVerticalGap;
    public int mWidth;
    /* renamed from: mX */
    public int f29mX;
    /* renamed from: mY */
    public int f30mY;

    public FilterMenuItem(int[] lcdSize, int topMargin, int startMargin, int endMargin, int width, int height, int verticalGap, int horizontalGap, int filterMenuBottomMargin, boolean isRTL) {
        this.mLcdSize = lcdSize;
        this.mTopMargin = topMargin;
        this.mStartMargin = startMargin;
        this.mEndMargin = endMargin;
        this.mWidth = width;
        this.mHeight = height;
        this.mVerticalGap = verticalGap;
        this.mHorizontalGap = horizontalGap;
        this.mFilterMenuBottomMargin = filterMenuBottomMargin;
        this.mIsRTL = isRTL;
    }

    public void setDegree(int degree) {
        this.mDegree = degree;
        if (degree == 0 || degree == 180) {
            this.mItemCntPerRow = 5;
        } else {
            this.mItemCntPerRow = 1;
        }
    }

    public int[] getCoordinate(int position, int degree) {
        int[] coord = new int[2];
        int j = position;
        int i;
        if (this.mDegree == 0) {
            if (this.mIsRTL) {
                i = position;
                coord[0] = ((this.mLcdSize[1] - this.mEndMargin) - ((i + 1) * this.mWidth)) - (this.mHorizontalGap * i);
                coord[1] = this.mLcdSize[0] - (((this.mTopMargin + this.mVerticalGap) + (this.mHeight * 1)) + (this.mVerticalGap * 0));
            } else {
                coord[0] = this.mStartMargin + ((this.mWidth + this.mHorizontalGap) * position);
                coord[1] = this.mLcdSize[0] - (((this.mTopMargin + this.mVerticalGap) + (this.mHeight * 1)) + (this.mVerticalGap * 0));
            }
        } else if (this.mDegree == 270) {
            i = position;
            coord[0] = ((this.mLcdSize[1] - this.mEndMargin) - ((i + 1) * this.mWidth)) - (this.mHorizontalGap * i);
            coord[1] = this.mLcdSize[0] - (((this.mTopMargin + this.mVerticalGap) + (this.mHeight * 1)) + (this.mVerticalGap * 0));
        } else if (this.mDegree == 180) {
            j = position;
            coord[0] = ((this.mLcdSize[1] - this.mEndMargin) - ((j + 1) * this.mWidth)) - (this.mHorizontalGap * j);
            coord[1] = (this.mFilterMenuBottomMargin + (this.mHeight * 0)) + (this.mVerticalGap * 1);
        } else if (this.mDegree == 90) {
            coord[0] = this.mEndMargin + ((this.mWidth + this.mHorizontalGap) * position);
            coord[1] = (this.mFilterMenuBottomMargin + (this.mHeight * 0)) + (this.mVerticalGap * 1);
        }
        return coord;
    }

    public void setLUT(int lutNum) {
        this.mLUTNumber = lutNum;
    }

    public void setPosition(int position) {
        this.mItemPosition = position;
        updatePosition();
    }

    public void updatePosition() {
        int[] coord = getCoordinate(this.mItemPosition, this.mDegree);
        this.f29mX = coord[0];
        this.f30mY = coord[1];
    }

    public void setEntryValue(String entryValue) {
        this.mEntryValue = entryValue;
    }

    public void setEntry(String entry) {
        this.mEntry = entry;
    }

    public void setContentDesc(String contentDesc) {
        this.mContentDesc = contentDesc;
    }

    public int[] movePosition(int toPosition, float animationDuration, long timeDiff, int degree) {
        if (this.mItemPosition == toPosition) {
            return new int[]{0, 0};
        }
        int[] targetCoord = getCoordinate(toPosition, degree);
        int[] moveCoord = new int[2];
        float diffY;
        if (this.mItemPosition < toPosition) {
            if (toPosition % this.mItemCntPerRow == 0) {
                moveCoord[0] = (int) (((float) timeDiff) * (((float) (this.f29mX - targetCoord[0])) / animationDuration));
                moveCoord[1] = (int) (((float) timeDiff) * (((float) (this.f30mY - targetCoord[1])) / animationDuration));
                return moveCoord;
            } else if (this.mDegree == 0 || this.mDegree == 180) {
                moveCoord[0] = (int) (((float) timeDiff) * (((float) (this.f29mX - targetCoord[0])) / animationDuration));
                moveCoord[1] = 0;
                return moveCoord;
            } else {
                diffY = ((float) (this.f30mY - targetCoord[1])) / animationDuration;
                moveCoord[0] = 0;
                moveCoord[1] = (int) (((float) timeDiff) * diffY);
                return moveCoord;
            }
        } else if (toPosition % this.mItemCntPerRow == this.mItemCntPerRow - 1) {
            moveCoord[0] = (int) (((float) timeDiff) * (((float) (this.f29mX - targetCoord[0])) / animationDuration));
            moveCoord[1] = (int) (((float) timeDiff) * (((float) (this.f30mY - targetCoord[1])) / animationDuration));
            return moveCoord;
        } else if (this.mDegree == 0 || this.mDegree == 180) {
            moveCoord[0] = (int) (((float) timeDiff) * (((float) (this.f29mX - targetCoord[0])) / animationDuration));
            moveCoord[1] = 0;
            return moveCoord;
        } else {
            diffY = ((float) (this.f30mY - targetCoord[1])) / animationDuration;
            moveCoord[0] = 0;
            moveCoord[1] = (int) (((float) timeDiff) * diffY);
            return moveCoord;
        }
    }
}
