package com.lge.camera.components;

import android.content.Context;
import android.graphics.Rect;
import android.support.p000v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import com.lge.camera.C0088R;
import com.lge.camera.components.WheelControllerBase.OnWheelControllerListener;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ManualModeItem;
import com.lge.camera.util.Utils;

public class WheelController extends WheelControllerBase {
    protected ManualModeItem mModeItem = null;

    public WheelController(Context context) {
        super(context);
    }

    public WheelController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WheelController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setWheelControllerListener(OnWheelControllerListener listener) {
        this.mListener = listener;
    }

    public boolean isInitialized() {
        return this.mInit;
    }

    private void initConfiguration() {
        this.mPressedStateDuration = ViewConfiguration.getPressedStateDuration();
        this.mDeceleration = (386.0878f * (getContext().getResources().getDisplayMetrics().density * 160.0f)) * ViewConfiguration.getScrollFriction();
        this.mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public void initDimensionValue() {
        this.mTextAreaMinWidth = Math.round(Utils.dpToPx(getContext(), 92.0f));
        this.mTextAreaMinClickWidth = this.mTextAreaMinWidth / 2;
        this.mTextAreaMinClickHeight = Math.round(Utils.dpToPx(getContext(), 30.0f));
        this.mTextPadding = Math.round(Utils.dpToPx(getContext(), 18.0f));
        this.mIconPadding = Math.max(1, Math.round(Utils.dpToPx(getContext(), 1.5f)));
        this.mCenterTextSize = Math.round(Utils.dpToPx(getContext(), 25.0f));
        this.mBaseTextSize = Math.round(Utils.dpToPx(getContext(), 14.0f));
        this.mShadowRadius = Math.max(1.0f, (float) Math.round(Utils.dpToPx(getContext(), 0.5f)));
        this.mTickWidthL = Utils.dpToPx(getContext(), 9.0f);
        this.mTickWidthS = Utils.dpToPx(getContext(), 4.5f);
        this.mTickThick = Math.max(1.0f, Utils.dpToPx(getContext(), 0.5f));
        this.mGaugeThick = Utils.dpToPx(getContext(), 2.7f);
        this.mRimThick = Utils.dpToPx(getContext(), 2.5f);
        this.mHideRadius = Utils.dpToPx(getContext(), 284.0f);
        this.mMinimumDistanceForWheelMovement = Utils.dpToPx(getContext(), 3.0f);
        this.mWheelTouchInnerPadding = Math.round(Utils.dpToPx(getContext(), 50.0f));
        this.mWheelTouchPadding = Math.round(Utils.dpToPx(getContext(), 40.0f));
    }

    private void initConstantValues() {
        float f;
        this.mITEM_DEG = this.mIsUseLargeRadius ? 15 : 20;
        this.mSHOWING_STEP = this.mIsUseLargeRadius ? 3 : 2;
        this.mRIM_DRAW_DEG = 80.0f;
        this.mBOUND_MAX_DEG = ((int) (this.mRIM_DRAW_DEG / 2.0f)) + 2;
        if (this.mIsUseLargeRadius) {
            f = 90.0f;
        } else {
            f = 80.0f;
        }
        this.mGAUGE_DRAW_DEG = f;
        this.mITEM_ANGLE = ((double) this.mITEM_DEG) / 57.29577951308232d;
        this.mMAX_SHOW_DEGREE = ((int) (this.mRIM_DRAW_DEG / 2.0f)) + 180;
        this.mMIN_SHOW_DEGREE = 180 - ((int) (this.mRIM_DRAW_DEG / 2.0f));
    }

    public void initResources(int wheelType, ManualModeItem modeItem) {
        initResources(wheelType, modeItem, false);
    }

    public void initResources(int wheelType, ManualModeItem modeItem, boolean useLargeFactor) {
        if (modeItem != null) {
            this.mModeItem = modeItem;
            this.mWheelType = wheelType;
            this.mIsUseLargeRadius = useLargeFactor;
            initDimensionValue();
            if (!this.mInit) {
                String defaultEntryValue;
                initConfiguration();
                initConstantValues();
                this.mLcdHeight = Utils.getLCDsize(getContext(), false)[1];
                this.mRadius = Math.round(((float) this.mLcdHeight) / (useLargeFactor ? 1.503f : 2.483f));
                this.mCenterX = this.mTextAreaMinWidth + ((int) this.mHideRadius);
                this.mCenterY = Math.round(((float) this.mLcdHeight) / 2.0f);
                String key = this.mModeItem.getKey();
                String[] entries = this.mModeItem.getEntries();
                String[] values = this.mModeItem.getValues();
                Integer[] icons = this.mModeItem.getIcons();
                int currentIndex = this.mModeItem.getSelectedIndex();
                if (currentIndex != -1) {
                    defaultEntryValue = entries[currentIndex];
                } else {
                    defaultEntryValue = this.mModeItem.getDefaultEntryValue();
                }
                CamLog.m3d(CameraConstants.TAG, "defaultEntryValue : " + defaultEntryValue);
                if (entries != null && entries.length != 0 && values != null && values.length != 0) {
                    makeDataList(key, entries, values, icons, defaultEntryValue);
                    if (this.mGestureDetector == null) {
                        this.mGestureDetector = new GestureDetectorCompat(getContext(), this);
                    }
                    setOnTouchListener(this);
                    this.mScrollAngle = this.mCurValueAngle;
                    this.mArrowIcon = BitmapManagingUtil.getBitmap(getContext(), C0088R.drawable.camera_main_arrow);
                    this.mInit = true;
                    postInvalidate();
                } else {
                    return;
                }
            }
            makeClickableAreaList();
        }
    }

    private void makeDataList(String key, String[] entries, String[] values, Integer[] icons, String defaultEntryValue) {
        this.mStartAngleRadius = (3.141592653589793d * ((double) (360.0f - ((float) ((entries.length - (entries.length % 2)) * this.mITEM_DEG))))) / 360.0d;
        for (int i = 0; i < entries.length; i++) {
            WheelItem item = new WheelItem();
            item.mTitle = entries[i];
            item.mValue = values[i];
            if (icons != null) {
                int icon = icons[i].intValue();
                if (icon > 0) {
                    item.mResId = icon;
                }
            }
            item.mKey = key;
            item.mAngle = this.mStartAngleRadius + (((double) i) * this.mITEM_ANGLE);
            if (entries[i].equals(defaultEntryValue)) {
                item.mSelected = true;
                this.mCurValueAngle = 3.141592653589793d - item.mAngle;
            }
            this.mDataList.add(item);
        }
    }

    private void makeClickableAreaList() {
        if (this.mClickRegionMap != null) {
            for (int i = -2; i < 3; i++) {
                int curDegree = (int) (((float) (this.mITEM_DEG * i)) + 180.0f);
                double clickableX = ((double) this.mCenterX) + (Math.cos(((double) curDegree) * 0.017453292519943295d) * ((double) (this.mRadius + this.mTextPadding)));
                double clickableY = ((double) this.mCenterY) + (Math.sin(((double) curDegree) * 0.017453292519943295d) * ((double) (this.mRadius + this.mTextPadding)));
                this.mClickRegionMap.put(i, new Rect((int) (clickableX - ((double) this.mTextAreaMinClickWidth)), (int) (clickableY - ((double) this.mTextAreaMinClickHeight)), (int) (((double) this.mTextAreaMinClickWidth) + clickableX), (int) (((double) this.mTextAreaMinClickHeight) + clickableY)));
            }
        }
    }

    public void refreshResource(boolean isSecondWheel) {
        this.mRadius = Math.round(((float) this.mLcdHeight) / (isSecondWheel ? 1.503f : 2.483f));
        this.mIsUseLargeRadius = isSecondWheel;
        initConstantValues();
        refreshAngleValues();
        makeClickableAreaList();
        postInvalidate();
    }

    public void unbind() {
        if (this.mDataList != null) {
            this.mDataList.clear();
        }
        if (this.mClickRegionMap != null) {
            this.mClickRegionMap.clear();
        }
        this.mGestureDetector = null;
        this.mArrowIcon = null;
        this.mInit = false;
    }

    public void updateCurrentRecommend(String[] updateList) {
        if (updateList != null && this.mDataList != null && this.mDataList.size() != 0) {
            int dataSize = this.mDataList.size();
            for (int i = 0; i < dataSize; i++) {
                WheelItem item = (WheelItem) this.mDataList.get(i);
                if (item != null) {
                    item.mRecommend = false;
                    int j = 0;
                    while (j < updateList.length) {
                        if (item.mValue.equals(updateList[j])) {
                            item.mRecommend = true;
                            if (j == 0) {
                                item.mRecommendType = 1;
                            } else if (j == updateList.length - 1) {
                                item.mRecommendType = 3;
                            } else {
                                item.mRecommendType = 2;
                            }
                        } else {
                            j++;
                        }
                    }
                }
            }
            postInvalidate();
        }
    }

    public void setViewDegree(int viewDegree) {
        if (this.mViewDegree != viewDegree) {
            postInvalidate();
        }
        if (this.mRotationInfo != null) {
            this.mRotationInfo.setDegree(viewDegree, true);
        }
        this.mViewDegree = viewDegree;
    }

    public void refreshAngleValues() {
        if (this.mDataList != null && this.mDataList.size() != 0) {
            int dataSize = this.mDataList.size();
            this.mStartAngleRadius = (((double) (360.0f - ((float) ((dataSize - (dataSize % 2)) * this.mITEM_DEG)))) * 3.141592653589793d) / 360.0d;
            for (int i = 0; i < dataSize; i++) {
                WheelItem item = (WheelItem) this.mDataList.get(i);
                if (item != null) {
                    double newAngle = this.mStartAngleRadius + (((double) i) * this.mITEM_ANGLE);
                    if (Double.compare(item.mAngle, newAngle) != 0) {
                        item.mAngle = newAngle;
                        if (item.mSelected) {
                            this.mCurValueAngle = 3.141592653589793d - item.mAngle;
                            this.mScrollAngle = this.mCurValueAngle;
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
            if (((WheelItem) this.mDataList.get(i)).mSelected) {
                return i;
            }
        }
        return -1;
    }

    public void setSelectedItem(String value, boolean isDefault) {
        if (this.mDataList != null && this.mDataList.size() != 0 && value != null) {
            int dataSize = this.mDataList.size();
            for (int i = 0; i < dataSize; i++) {
                WheelItem item = (WheelItem) this.mDataList.get(i);
                if (item != null) {
                    item.mSelected = false;
                    if (value.equals(item.mValue)) {
                        item.mSelected = true;
                        this.mCurValueAngle = 3.141592653589793d - item.mAngle;
                        this.mScrollAngle = this.mCurValueAngle;
                    }
                }
            }
            if (isDefault) {
                this.mScrollAngle = 0.0d;
                this.mCurValueAngle = 0.0d;
            }
        }
    }

    public WheelItem getCurSelectedItem() {
        if (!this.mInit || this.mDataList == null || this.mDataList.size() == 0) {
            return null;
        }
        int dataSize = this.mDataList.size();
        WheelItem selectedItem = null;
        for (int i = 0; i < dataSize; i++) {
            WheelItem item = (WheelItem) this.mDataList.get(i);
            double sweepDegree = (item.mAngle + this.mScrollAngle) * 57.29577951308232d;
            if (sweepDegree <= 176.0d || sweepDegree >= 184.0d) {
                item.mSelected = false;
            } else {
                item.mSelected = true;
                selectedItem = item;
            }
        }
        return selectedItem;
    }

    public WheelItem getWheelItem(int index) {
        if (!this.mInit || this.mDataList == null || this.mDataList.size() == 0 || index >= this.mDataList.size()) {
            return null;
        }
        return (WheelItem) this.mDataList.get(index);
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
    }

    public boolean isWheelMoving() {
        return (this.mScrollState == 0 && this.mTouchState == 0) ? false : true;
    }

    public boolean isSecondWheelMode() {
        return this.mIsUseLargeRadius;
    }

    public int getWheelType() {
        return this.mWheelType;
    }

    public String getTitle() {
        return this.mModeItem != null ? this.mModeItem.getTitle() : "";
    }

    public String getKey() {
        return this.mModeItem != null ? this.mModeItem.getKey() : "";
    }

    public void stopWheelMoving() {
        if (getVisibility() != 0) {
            CamLog.m3d(CameraConstants.TAG, "stopWheelMoving - return : wheelType = " + this.mWheelType);
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "stopWheelMoving - start.");
        releaseScrollAngle(false, true);
        setSelectedItemByAngle();
        removeConfirmSelection();
        this.mCurValueAngle = this.mScrollAngle;
        this.mStartX = 0.0f;
        this.mStartY = 0.0f;
        this.mTouchState = 0;
        this.mScrollState = 0;
        sendPerformClick(0);
    }
}
