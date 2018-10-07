package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera.Area;
import android.util.AttributeSet;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.List;

public class CameraManualModeFocusMultiWindowView extends CameraFocusView {
    private List<Area> mAreaList = new ArrayList();
    private BitmapDrawable mCenterDrawWhite = null;
    private int mCenterX = 0;
    private int mCenterY = 0;
    private Rect mDrawRect = new Rect();
    private int mHeight = 0;
    private boolean mIsLevelControlFocusMode = false;
    private BitmapDrawable mManualCenterFailHorizon = null;
    private BitmapDrawable mManualCenterFailVertical = null;
    private BitmapDrawable mMultifocusSuceedHorizon = null;
    private BitmapDrawable mMultifocusSuceedVertical = null;
    private int mPointerHeight = 0;
    private int mPointerWidth = 0;
    private int mPreviewHeight = 0;
    private int mPreviewWidth = 0;
    private List<Rect> mRectList = new ArrayList();
    private int mUnusedAreaCount = 0;
    private int mWidth = 0;

    public CameraManualModeFocusMultiWindowView(Context context) {
        super(context);
    }

    public CameraManualModeFocusMultiWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraManualModeFocusMultiWindowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init(List<Area> list, int[] previewSizeOnScreen, int unUsedAreaCount) {
        CamLog.m3d(CameraConstants.TAG, "multi window af previewSizeOnScreen = " + previewSizeOnScreen[0] + ", x " + previewSizeOnScreen[1]);
        this.mUnusedAreaCount = unUsedAreaCount;
        this.mAreaList = list;
        manualModeFocusViewInit();
        refresh(previewSizeOnScreen);
        if (!this.mInit) {
            initResources();
        }
        CamLog.m3d(CameraConstants.TAG, "initMultiWindowAFView previewSizeOnScreen = " + list.size());
        this.mInit = true;
    }

    public void initResources() {
        this.mManualCenterFailHorizon = BitmapManagingUtil.convertSVGToBitmap(getContext(), getResources(), C0088R.drawable.focus_guide_multiwindow_af);
        this.mManualCenterFailVertical = BitmapManagingUtil.convertSVGToBitmap(getContext(), getResources(), C0088R.drawable.focus_guide_multiwindow_af);
        this.mMultifocusSuceedHorizon = BitmapManagingUtil.convertSVGToBitmap(getContext(), getResources(), C0088R.drawable.focus_guide_multiwindow_af_success);
        this.mMultifocusSuceedVertical = BitmapManagingUtil.convertSVGToBitmap(getContext(), getResources(), C0088R.drawable.focus_guide_multiwindow_af_success);
    }

    public void unbind() {
        this.mInit = false;
        if (this.mAreaList != null) {
            this.mAreaList.clear();
        }
        if (this.mRectList != null) {
            this.mRectList.clear();
        }
    }

    public void refresh(int[] previewSizeOnScreen) {
        this.mPreviewWidth = previewSizeOnScreen[0];
        this.mPreviewHeight = previewSizeOnScreen[1];
        makeMultiFocusView();
    }

    public void setList(List<Area> list) {
        this.mAreaList = list;
    }

    public void manualModeFocusViewInit() {
        if (this.mRectList != null) {
            int size = this.mRectList.size() - 1;
            for (int i = 0; i < size; i++) {
                ((Area) this.mAreaList.get(i)).weight = 0;
            }
            invalidate();
        }
    }

    private void makeMultiFocusView() {
        if (this.mAreaList == null) {
            CamLog.m3d(CameraConstants.TAG, "Cannot make multi focus window because areaList null.");
        } else if (this.mRectList != null) {
            this.mRectList.clear();
            boolean isLand = Utils.isConfigureLandscape(getResources());
            float convertX = ((float) this.mPreviewWidth) / 2000.0f;
            float convertY = ((float) this.mPreviewHeight) / 2000.0f;
            int i = 0;
            while (i < this.mAreaList.size() && this.mAreaList.get(i) != null) {
                int l = (int) (((float) (((Area) this.mAreaList.get(i)).rect.left + 1000)) * convertX);
                int r = (int) (((float) (((Area) this.mAreaList.get(i)).rect.right + 1000)) * convertX);
                int t = (int) (((float) (((Area) this.mAreaList.get(i)).rect.top + 1000)) * convertY);
                int b = (int) (((float) (((Area) this.mAreaList.get(i)).rect.bottom + 1000)) * convertY);
                if (isLand) {
                    this.mRectList.add(new Rect(l, t, r, b));
                } else {
                    this.mRectList.add(new Rect(this.mPreviewHeight - b, l, this.mPreviewHeight - t, r));
                }
                i++;
            }
        }
    }

    public void setDegree(int degree) {
        super.setDegree(degree);
        setMenuMode();
        invalidate();
    }

    public void setCenterWindowVisibility(boolean centerShowing) {
    }

    public void setMenuMode() {
        setImageDrawable(isHorizontal() ? this.mMultifocusSuceedHorizon : this.mMultifocusSuceedVertical);
    }

    public void setLevelControlFocusMode(boolean enable) {
        this.mIsLevelControlFocusMode = enable;
        manualModeFocusViewInit();
    }

    public boolean isLevelControlFocusModeEnable() {
        return this.mIsLevelControlFocusMode;
    }

    public void setState(int state) {
        if (this.mInit) {
            super.setState(state);
            invalidate();
        }
    }

    protected void onDraw(Canvas canvas) {
        if (this.mInit && getVisibility() == 0 && getDrawable() != null && this.mRectList != null) {
            int size = this.mRectList.size() - this.mUnusedAreaCount;
            Drawable drawable = isHorizontal() ? this.mMultifocusSuceedHorizon : this.mMultifocusSuceedVertical;
            this.mCenterDrawWhite = isHorizontal() ? this.mManualCenterFailHorizon : this.mManualCenterFailVertical;
            for (int i = 0; i < size; i++) {
                Area area = (Area) this.mAreaList.get(i);
                Rect rect = (Rect) this.mRectList.get(i);
                if (!(area == null || rect == null)) {
                    this.mDrawRect.set(rect);
                    this.mWidth = Math.abs(rect.bottom - rect.top);
                    this.mHeight = Math.abs(rect.right - rect.left);
                    this.mCenterX = rect.left + (this.mHeight / 2);
                    this.mCenterY = rect.top + (this.mWidth / 2);
                    this.mPointerWidth = drawable.getIntrinsicWidth();
                    this.mPointerHeight = drawable.getIntrinsicHeight();
                    this.mDrawRect.left = this.mCenterX - (this.mPointerWidth / 2);
                    this.mDrawRect.top = this.mCenterY - (this.mPointerHeight / 2);
                    this.mDrawRect.right = this.mDrawRect.left + this.mPointerWidth;
                    this.mDrawRect.bottom = this.mDrawRect.top + this.mPointerHeight;
                    drawRect(canvas, drawable, area);
                }
            }
        }
    }

    private void drawRect(Canvas canvas, Drawable drawable, Area area) {
        if (area.weight == 1) {
            drawable.setBounds(this.mDrawRect);
            drawable.draw(canvas);
        } else if (this.mIsLevelControlFocusMode) {
            this.mCenterDrawWhite.setBounds(this.mDrawRect);
            this.mCenterDrawWhite.draw(canvas);
        }
    }
}
