package com.lge.camera.components;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.support.p000v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.file.ExifInterface.GpsSpeedRef;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ManualModeItem;
import com.lge.camera.util.Utils;

public class DrumController extends DrumControllerBase {
    protected ManualModeItem mModeItem = null;

    public DrumController(Context context) {
        super(context);
    }

    public DrumController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrumController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDrumControllerListener(DrumControllerListener listener) {
        this.mListener = listener;
    }

    public boolean isInitialized() {
        return this.mInit;
    }

    protected void initConfiguration() {
        this.mPressedStateDuration = ViewConfiguration.getPressedStateDuration();
        this.mDeceleration = (386.0878f * (getContext().getResources().getDisplayMetrics().density * 160.0f)) * ViewConfiguration.getScrollFriction();
        this.mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public void initDimensionValue() {
        this.mTextAreaMinWidth = Math.round(Utils.dpToPx(getContext(), 43.0f));
        this.mTextAreaMinClickWidth = this.mTextAreaMinWidth / 2;
        this.mTextPadding = Math.max(1, Math.round(Utils.dpToPx(getContext(), 1.5f)));
        this.mTextPaddingSelected = Math.max(1, Math.round(Utils.dpToPx(getContext(), 4.0f)));
        this.mIconSize = Math.round(Utils.dpToPx(getContext(), 18.0f));
        this.mCenterTextSize = Math.round(Utils.dpToPx(getContext(), 20.0f));
        this.mBaseTextSize = Math.round(Utils.dpToPx(getContext(), 13.0f));
        this.mShadowRadius = Math.max(1.0f, (float) Math.round(Utils.dpToPx(getContext(), 0.5f)));
        this.mTickThick = Math.max(1.0f, Utils.dpToPx(getContext(), 0.5f));
        this.mMinimumDistanceForDrumMovement = Utils.dpToPx(getContext(), 3.0f);
        this.mDrumTouchInnerPadding = Math.round(Utils.dpToPx(getContext(), 50.0f));
        this.mDrumTouchPadding = Math.round(Utils.dpToPx(getContext(), 40.0f));
        initDrumBgDimension();
    }

    protected void initConstantValues() {
        this.mDEFAULT_FLING_DURATION = 150;
        int i = (this.mDrumType == 2 || this.mDrumType == 4 || this.mDrumType == 1) ? 12 : 6;
        this.mSHOWING_STEP = i;
        this.mDRUM_WIDTH = (int) Utils.dpToPx(getContext(), 28.5f);
        this.mDRUM_HEIGHT = Utils.getPx(getContext(), C0088R.dimen.manual_drum_height);
        this.mITEM_GAP = this.mDRUM_HEIGHT / this.mSHOWING_STEP;
        this.mWB_BAR_WIDTH = (int) Utils.dpToPx(getContext(), 5.0f);
        if (this.mDrumType == 16) {
            i = (int) Utils.dpToPx(getContext(), 14.5f);
        } else {
            i = ((int) Utils.dpToPx(getContext(), 32.5f)) / 2;
        }
        this.mCENTER_SELECTED_WIDTH = i;
        this.mSELECT_BOUND_POSITION = this.mCENTER_SELECTED_WIDTH / 2;
        i = (this.mDrumType == 2 || this.mDrumType == 1) ? this.mSELECT_BOUND_POSITION + this.mITEM_GAP : this.mSELECT_BOUND_POSITION + (this.mITEM_GAP * 2);
        this.mSECOND_LEVEL_GAP = i;
        i = (this.mDrumType == 2 || this.mDrumType == 1) ? this.mSELECT_BOUND_POSITION + (this.mITEM_GAP * 2) : this.mSELECT_BOUND_POSITION + (this.mITEM_GAP * 4);
        this.mTHIRD_LEVEL_GAP = i;
    }

    public void initResources(int drumType, ManualModeItem modeItem, boolean byForce, String currentValue) {
        if (modeItem != null) {
            if (byForce) {
                this.mInit = false;
                if (this.mDataList != null) {
                    this.mDataList.clear();
                }
                if (this.mClickRegionMap != null) {
                    this.mClickRegionMap.clear();
                }
            }
            this.mModeItem = modeItem;
            this.mDrumType = drumType;
            initDimensionValue();
            if (!this.mInit) {
                String defaultEntryValue;
                initConfiguration();
                initConstantValues();
                calculateCoordinates();
                String key = this.mModeItem.getKey();
                String[] entries = this.mModeItem.getEntries();
                String[] values = this.mModeItem.getValues();
                Integer[] icons = this.mModeItem.getIcons();
                int currentIndex = this.mModeItem.getSelectedIndex();
                int[] barColors = this.mModeItem.getBarColors();
                boolean[] showTitle = this.mModeItem.getShowEntryValue();
                if (currentIndex != -1) {
                    defaultEntryValue = entries[currentIndex];
                } else {
                    defaultEntryValue = this.mModeItem.getDefaultEntryValue();
                }
                CamLog.m3d(CameraConstants.TAG, "defaultEntryValue : " + defaultEntryValue);
                if (entries != null && entries.length != 0 && values != null && values.length != 0) {
                    makeDataList(key, entries, values, icons, barColors, showTitle, defaultEntryValue, currentValue);
                    if (this.mGestureDetector == null) {
                        this.mGestureDetector = new GestureDetectorCompat(getContext(), this);
                    }
                    setOnTouchListener(this);
                    this.mScrollPosition = this.mCurValuePosition;
                    this.mGradiantBg = (GradientDrawable) getContext().getDrawable(C0088R.drawable.drum_background);
                    if (this.mDrumType == 4) {
                        this.mWBColorBg = new GradientDrawable();
                    }
                    this.mInit = true;
                    postInvalidate();
                } else {
                    return;
                }
            }
            makeClickableAreaList();
        }
    }

    public void initResources(int drumType, ManualModeItem modeItem) {
        initResources(drumType, modeItem, false, null);
    }

    protected void calculateCoordinates() {
        this.mLcdHeight = Utils.getLCDsize(getContext(), true)[1];
        if (this.mViewDegree == 90 || this.mViewDegree == 270) {
        }
        this.mCenterY = Math.round(((float) this.mLcdHeight) / 2.0f);
        this.mHalfHeight = this.mDRUM_HEIGHT / 2;
        this.mDrumEndPosX = Utils.getPx(getContext(), C0088R.dimen.manual_wheel_width) - getPaddingEnd();
        this.mDrumEndPosY = this.mCenterY + this.mHalfHeight;
        this.mDrumStartPosX = this.mDrumEndPosX - this.mDRUM_WIDTH;
        this.mDrumStartPosY = this.mCenterY - this.mHalfHeight;
        this.mDrumStartMargin = (int) Utils.dpToPx(getContext(), 5.75f);
        this.mDrumEndMargin = (int) Utils.dpToPx(getContext(), 8.25f);
        this.mTickWidthL = (float) (((this.mDrumEndPosX - this.mDrumStartPosX) - this.mDrumStartMargin) - this.mDrumEndMargin);
        this.mTickWidthS = this.mTickWidthL / 2.0f;
    }

    protected void makeDataList(String key, String[] entries, String[] values, Integer[] icons, int[] barColors, boolean[] showTitle, String defaultEntryValue, String currentValue) {
        String valueToBeSet;
        this.mStartPosition = this.mCenterY;
        if (currentValue == null) {
            valueToBeSet = defaultEntryValue;
        } else {
            valueToBeSet = currentValue;
        }
        int i = 0;
        while (i < entries.length) {
            DrumItem item = new DrumItem();
            item.mTitle = entries[i];
            item.mValue = values[i];
            if (icons != null) {
                int icon = icons[i].intValue();
                if (icon > 0) {
                    item.mResId = icon;
                }
            }
            item.mKey = key;
            item.mPosition = this.mStartPosition - (this.mITEM_GAP * i);
            if (entries[i].equals(valueToBeSet)) {
                item.mSelected = true;
                this.mCurValuePosition = this.mITEM_GAP * i;
            }
            if (this.mDrumType == 4 && barColors != null && i + 1 < entries.length) {
                item.mStartBarColor = barColors[i];
                item.mEndBarColor = barColors[i + 1];
            }
            if (showTitle != null) {
                item.mIsShowTitle = showTitle[i];
            }
            if (this.mDataList != null) {
                this.mDataList.add(item);
            }
            i++;
        }
    }

    protected void makeClickableAreaList() {
        if (this.mClickRegionMap != null) {
            int halfStep = Math.round((float) (this.mSHOWING_STEP / 2)) + 1;
            for (int i = -halfStep; i < halfStep; i++) {
                int clickableY = this.mCenterY + (i * this.mITEM_GAP);
                this.mClickRegionMap.put(i, new Rect((this.mDrumStartPosX + this.mDrumStartMargin) - this.mTextAreaMinWidth, clickableY - (this.mITEM_GAP / 2), this.mDrumEndPosX - this.mDrumEndMargin, (this.mITEM_GAP / 2) + clickableY));
            }
        }
    }

    public void unbind() {
        if (this.mDataList != null) {
            this.mDataList.clear();
        }
        if (this.mClickRegionMap != null) {
            this.mClickRegionMap.clear();
        }
        this.mGestureDetector = null;
        this.mGradiantBg = null;
        this.mWBColorBg = null;
        this.mInit = false;
        this.mListener = null;
    }

    public void setDegree(int viewDegree) {
        if (this.mViewDegree != viewDegree) {
            postInvalidate();
        }
        if (this.mRotationInfo != null) {
            this.mRotationInfo.setDegree(viewDegree, false);
        }
        this.mViewDegree = viewDegree;
        calculateCoordinates();
        refreshPositionValues();
    }

    public void refreshPositionValues() {
        if (this.mDataList != null && this.mDataList.size() != 0) {
            int dataSize = this.mDataList.size();
            this.mStartPosition = this.mCenterY;
            for (int i = 0; i < dataSize; i++) {
                DrumItem item = (DrumItem) this.mDataList.get(i);
                if (item != null) {
                    int newPosition = this.mStartPosition - (this.mITEM_GAP * i);
                    if (item.mPosition != newPosition) {
                        item.mPosition = newPosition;
                        if (item.mSelected) {
                            this.mCurValuePosition = this.mITEM_GAP * i;
                            this.mScrollPosition = this.mCurValuePosition;
                        }
                    }
                }
            }
        }
    }

    public int getCurSelectedIndex() {
        if (!this.mInit || this.mDataList == null || this.mDataList.size() == 0) {
            return -1;
        }
        int dataSize = this.mDataList.size();
        for (int i = 0; i < dataSize; i++) {
            if (((DrumItem) this.mDataList.get(i)).mSelected) {
                return i;
            }
        }
        return -1;
    }

    public void setSelectedItem(String value, boolean isDefault) {
        if (this.mDataList != null && this.mDataList.size() != 0 && value != null) {
            int dataSize = this.mDataList.size();
            for (int i = 0; i < dataSize; i++) {
                DrumItem item = (DrumItem) this.mDataList.get(i);
                if (item != null) {
                    item.mSelected = false;
                    if (value.equals(item.mValue)) {
                        item.mSelected = true;
                        this.mCurValuePosition = this.mITEM_GAP * i;
                        this.mScrollPosition = this.mCurValuePosition;
                    }
                }
            }
            if (isDefault) {
                this.mCurValuePosition = 0;
                this.mScrollPosition = 0;
            }
        }
    }

    public void setSelectedRealWBItem(String value, boolean isDefault) {
        if (this.mDataList != null && this.mDataList.size() != 0 && value != null) {
            int dataSize = this.mDataList.size();
            value = value.replace(GpsSpeedRef.KILOMETERS, "");
            float targetValue = Utils.parseStringToFloat(value);
            for (int i = 0; i < dataSize; i++) {
                DrumItem nextItem;
                DrumItem curItem = (DrumItem) this.mDataList.get(i);
                if (i < dataSize - 1) {
                    nextItem = (DrumItem) this.mDataList.get(i + 1);
                } else {
                    nextItem = curItem;
                }
                if (!(curItem == null || nextItem == null)) {
                    curItem.mSelected = false;
                    float curValue = Utils.parseStringToFloat(curItem.mValue);
                    float nextValue = Utils.parseStringToFloat(nextItem.mValue);
                    if (i != 0 || Float.compare(targetValue, curValue) >= 0) {
                        if (!value.equals(curItem.mValue)) {
                            if (Float.compare(targetValue, curValue) > 0 && Float.compare(targetValue, nextValue) < 0) {
                                curItem.mSelected = true;
                                this.mCurValuePosition = (this.mITEM_GAP * i) - (((this.mITEM_GAP * i) - ((i + 1) * this.mITEM_GAP)) / 2);
                                this.mScrollPosition = this.mCurValuePosition;
                                this.mIsNotNeedToCorrectPosition = true;
                                break;
                            }
                        }
                        curItem.mSelected = true;
                        this.mCurValuePosition = this.mITEM_GAP * i;
                        this.mScrollPosition = this.mCurValuePosition;
                        break;
                    }
                    curItem.mSelected = true;
                    this.mCurValuePosition = (this.mITEM_GAP * i) + (((this.mITEM_GAP * i) - ((i + 1) * this.mITEM_GAP)) / 2);
                    this.mScrollPosition = this.mCurValuePosition;
                    break;
                }
            }
            if (isDefault) {
                this.mCurValuePosition = 0;
                this.mScrollPosition = 0;
            }
        }
    }

    public void setSelectedRealSSItem(String value, boolean isDefault) {
        if (this.mDataList != null && this.mDataList.size() != 0 && value != null) {
            int dataSize = this.mDataList.size();
            float targetValue = calculateShutterSpeedValue(value.split("/"));
            for (int i = 0; i < dataSize; i++) {
                DrumItem nextItem;
                DrumItem curItem = (DrumItem) this.mDataList.get(i);
                if (i < dataSize - 1) {
                    nextItem = (DrumItem) this.mDataList.get(i + 1);
                } else {
                    nextItem = curItem;
                }
                if (!(curItem == null || nextItem == null)) {
                    curItem.mSelected = false;
                    float curValue = calculateShutterSpeedValue(curItem.mValue.split("/"));
                    float nextValue = calculateShutterSpeedValue(nextItem.mValue.split("/"));
                    if (i != 0 || Float.compare(targetValue, curValue) <= 0) {
                        if (!value.equals(curItem.mValue)) {
                            if (Float.compare(targetValue, curValue) < 0 && Float.compare(targetValue, nextValue) > 0) {
                                curItem.mSelected = true;
                                this.mCurValuePosition = (this.mITEM_GAP * i) - (((this.mITEM_GAP * i) - ((i + 1) * this.mITEM_GAP)) / 2);
                                this.mScrollPosition = this.mCurValuePosition;
                                this.mIsNotNeedToCorrectPosition = true;
                                break;
                            }
                        }
                        curItem.mSelected = true;
                        this.mCurValuePosition = this.mITEM_GAP * i;
                        this.mScrollPosition = this.mCurValuePosition;
                        break;
                    }
                    curItem.mSelected = true;
                    this.mCurValuePosition = (this.mITEM_GAP * i) + (((this.mITEM_GAP * i) - ((i + 1) * this.mITEM_GAP)) / 2);
                    this.mScrollPosition = this.mCurValuePosition;
                    break;
                }
            }
            if (isDefault) {
                this.mCurValuePosition = 0;
                this.mScrollPosition = 0;
            }
        }
    }

    private float calculateShutterSpeedValue(String[] parseValues) {
        if (parseValues.length == 1) {
            return Float.parseFloat(parseValues[0]);
        }
        return Float.parseFloat(parseValues[0]) / Float.parseFloat(parseValues[1]);
    }

    public void setSelectedRealISOItem(String value, boolean isDefault) {
        if (this.mDataList != null && this.mDataList.size() != 0 && value != null) {
            int dataSize = this.mDataList.size();
            int targetValue = Integer.parseInt(value);
            for (int i = 0; i < dataSize; i++) {
                DrumItem nextItem;
                DrumItem curItem = (DrumItem) this.mDataList.get(i);
                if (i < dataSize - 1) {
                    nextItem = (DrumItem) this.mDataList.get(i + 1);
                } else {
                    nextItem = curItem;
                }
                if (!(curItem == null || nextItem == null)) {
                    curItem.mSelected = false;
                    int curValue = Integer.parseInt(curItem.mValue);
                    int nextValue = Integer.parseInt(nextItem.mValue);
                    if (value.equals(curItem.mValue)) {
                        curItem.mSelected = true;
                        this.mCurValuePosition = this.mITEM_GAP * i;
                        this.mScrollPosition = this.mCurValuePosition;
                    } else if (targetValue > curValue && targetValue < nextValue) {
                        curItem.mSelected = true;
                        this.mCurValuePosition = (this.mITEM_GAP * i) - (((this.mITEM_GAP * i) - ((i + 1) * this.mITEM_GAP)) / 2);
                        this.mScrollPosition = this.mCurValuePosition;
                        this.mIsNotNeedToCorrectPosition = true;
                    }
                }
            }
            if (isDefault) {
                this.mCurValuePosition = 0;
                this.mScrollPosition = 0;
            }
        }
    }

    public DrumItem getCurSelectedItem() {
        if (!this.mInit || this.mDataList == null || this.mDataList.size() == 0) {
            return null;
        }
        int dataSize = this.mDataList.size();
        DrumItem selectedItem = null;
        for (int i = 0; i < dataSize; i++) {
            DrumItem item = (DrumItem) this.mDataList.get(i);
            int scrollDiff = item.mPosition + this.mScrollPosition;
            if (scrollDiff <= this.mCenterY - this.mSELECT_BOUND_POSITION || scrollDiff >= this.mCenterY + this.mSELECT_BOUND_POSITION) {
                item.mSelected = false;
            } else {
                item.mSelected = true;
                selectedItem = item;
            }
        }
        return selectedItem;
    }

    public DrumItem getDrumItem(int index) {
        if (!this.mInit || this.mDataList == null) {
            return null;
        }
        int dataSize = this.mDataList.size();
        if (dataSize == 0 || index >= dataSize) {
            return null;
        }
        return (DrumItem) this.mDataList.get(index);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            this.mTPaintColor = -1;
            this.mTickPaintColor = -1;
            return;
        }
        this.mTPaintColor = DISABLED_COLOR;
        this.mTickPaintColor = DISABLED_COLOR;
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        this.mScrollState = 0;
        this.mTouchState = 0;
        this.mParamWaitingCount = 0;
    }

    public boolean isDrumMoving() {
        return (this.mScrollState == 0 && this.mTouchState == 0 && this.mParamWaitingCount <= 0) ? false : true;
    }

    public int getDrumType() {
        return this.mDrumType;
    }

    public String getTitle() {
        return this.mModeItem != null ? this.mModeItem.getTitle() : "";
    }

    public String getKey() {
        return this.mModeItem != null ? this.mModeItem.getKey() : "";
    }

    public int getDrumMarginTop() {
        return (this.mLcdHeight - this.mDRUM_HEIGHT) / 2;
    }

    public void stopDrumMoving() {
        if (getVisibility() != 0) {
            CamLog.m3d(CameraConstants.TAG, "stopDrumMoving - return : wheelType = " + this.mDrumType);
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "stopDrumMoving - start.");
        releaseScrollPosition(false, true);
        setSelectedItemByPosition();
        removeConfirmSelection();
        this.mCurValuePosition = this.mScrollPosition;
        this.mStartX = 0.0f;
        this.mStartY = 0.0f;
        this.mTouchState = 0;
        this.mScrollState = 0;
        sendPerformClick(0);
    }

    public void refreshBarColors(int[] barColors) {
        if (this.mDataList != null && barColors != null) {
            int i = 0;
            while (i < this.mDataList.size()) {
                DrumItem item = (DrumItem) this.mDataList.get(i);
                if (item != null && i + 1 < barColors.length) {
                    item.mStartBarColor = barColors[i];
                    item.mEndBarColor = barColors[i + 1];
                }
                i++;
            }
        }
    }

    public void setData(ManualModeItem modeItem) {
        if (modeItem != null) {
            String defaultEntryValue;
            this.mModeItem = modeItem;
            String key = this.mModeItem.getKey();
            String[] entries = this.mModeItem.getEntries();
            String[] values = this.mModeItem.getValues();
            Integer[] icons = this.mModeItem.getIcons();
            int currentIndex = this.mModeItem.getSelectedIndex();
            int[] barColors = this.mModeItem.getBarColors();
            boolean[] showTitle = this.mModeItem.getShowEntryValue();
            if (currentIndex != -1) {
                defaultEntryValue = entries[currentIndex];
            } else {
                defaultEntryValue = this.mModeItem.getDefaultEntryValue();
            }
            if (this.mDataList != null) {
                this.mDataList.clear();
            }
            makeDataList(key, entries, values, icons, barColors, showTitle, defaultEntryValue, null);
            refreshPositionValues();
        }
    }

    public boolean isVisibilityChanging() {
        return this.mIsVisibilityChanging;
    }

    public void setIsVisibilityChanging(boolean isVisibilityChanging) {
        this.mIsVisibilityChanging = isVisibilityChanging;
    }
}
