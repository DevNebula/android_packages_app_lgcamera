package com.lge.camera.util;

import android.util.SparseArray;

public class TimeIntervalChecker {
    public SparseArray<CheckerItem> mCheckMap = new SparseArray();

    class CheckerItem {
        public long mBaseInterval = 0;
        public long mCheckTime = 0;
        public int mItemType = 0;

        public CheckerItem(int itemType, long startTime) {
            this.mItemType = itemType;
            this.mBaseInterval = startTime;
        }

        public void setCheckTime(long time) {
            this.mCheckTime = time;
        }
    }

    public void clearChecker() {
        if (this.mCheckMap != null) {
            this.mCheckMap.clear();
        }
    }

    public void addChecker(int checkerType, long baseInterval) {
        if (this.mCheckMap != null) {
            CheckerItem checkerItem = (CheckerItem) this.mCheckMap.get(checkerType);
            if (checkerItem != null) {
                checkerItem.mBaseInterval = baseInterval;
                return;
            }
            this.mCheckMap.put(checkerType, new CheckerItem(checkerType, baseInterval));
        }
    }

    public boolean checkTimeInterval(int checkerType) {
        if (this.mCheckMap == null) {
            return true;
        }
        CheckerItem checkerItem = (CheckerItem) this.mCheckMap.get(checkerType);
        if (checkerItem == null) {
            return true;
        }
        long curTime = System.currentTimeMillis();
        if (curTime - checkerItem.mCheckTime < 0) {
            checkerItem.mCheckTime = curTime;
        }
        if (curTime - checkerItem.mCheckTime < checkerItem.mBaseInterval) {
            return false;
        }
        checkerItem.mCheckTime = curTime;
        return true;
    }
}
