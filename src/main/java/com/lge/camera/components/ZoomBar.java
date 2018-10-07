package com.lge.camera.components;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import java.util.Locale;

public class ZoomBar extends BarView {
    private BarProgress mProgressBar = null;
    private int mZOOM_MAX_STEP = 90;
    private int prevDegree;

    public ZoomBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ZoomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZoomBar(Context context) {
        super(context);
    }

    public void initBar(BarAction barAction, int barType) {
        setBarValueInitialization(this.mZOOM_MAX_STEP, 0, 1, 1, "zoom");
        setBarResources(C0088R.id.zoom_cursor, C0088R.id.zoom_cursor_bg_view, C0088R.id.zoom_minus_button, C0088R.id.zoom_minus_button_view, C0088R.id.zoom_plus_button, C0088R.id.zoom_plus_button_view, C0088R.id.zoom_text_layout);
        super.initBar(barAction, barType);
        setListener(true);
        getBarSettingValue();
        this.mProgressBar = (BarProgress) findViewById(C0088R.id.zoom_cursor_bg);
    }

    protected RotateLayout getBarLayout() {
        if (this.mBarAction == null) {
            return null;
        }
        return (RotateLayout) this.mBarAction.findViewById(C0088R.id.zoombar_rotate);
    }

    protected View getBarParentLayout() {
        if (this.mBarAction == null) {
            return null;
        }
        return this.mBarAction.findViewById(C0088R.id.zoom_bar_layout);
    }

    protected View getBarView() {
        if (this.mBarAction == null) {
            return null;
        }
        return this.mBarAction.findViewById(C0088R.id.zoom_bar);
    }

    public void getBarSettingValue() {
        int lValue = getCursorValue();
        if (lValue > this.mZOOM_MAX_STEP) {
            lValue = this.mZOOM_MAX_STEP;
        } else if (lValue < 0) {
            lValue = 0;
        }
        setCursorValue(lValue);
        setCursor(lValue);
    }

    public void setLayoutDimension() {
        float sizeCalculatedByPercentage = (float) RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, false, 0.09027f);
        this.CURSOR_HEIGHT_PORT = sizeCalculatedByPercentage;
        this.CURSOR_HEIGHT = sizeCalculatedByPercentage;
        this.MIN_CURSOR_POS = (int) (Utils.isConfigureLandscape(getResources()) ? this.CURSOR_HEIGHT : this.CURSOR_HEIGHT_PORT);
        this.MAX_CURSOR_POS = RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, false, 0.73611f);
        this.MAX_CURSOR_POS_PORT = RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, false, 0.73611f);
        this.CURSOR_POS_HEIGHT = (int) (((float) this.MAX_CURSOR_POS) - (this.CURSOR_HEIGHT * 2.0f));
        this.CURSOR_POS_HEIGHT_PORT = (int) (((float) this.MAX_CURSOR_POS_PORT) - (this.CURSOR_HEIGHT_PORT * 2.0f));
        this.RELEASE_EXPAND_LEFT = Utils.getPx(getContext(), C0088R.dimen.adj_plus_button.releaseLeft);
        this.RELEASE_EXPAND_TOP = Utils.getPx(getContext(), C0088R.dimen.adj_plus_button.releaseTop);
        this.RELEASE_EXPAND_RIGHT = Utils.getPx(getContext(), C0088R.dimen.adj_plus_button.releaseRight);
        this.RELEASE_EXPAND_BOTTOM = Utils.getPx(getContext(), C0088R.dimen.adj_plus_button.releaseBottom);
        this.mProgressBar = (BarProgress) findViewById(C0088R.id.zoom_cursor_bg);
        int cursorHeight = (int) (Utils.isConfigureLandscape(getResources()) ? this.CURSOR_HEIGHT : this.CURSOR_HEIGHT_PORT);
        int cursorPosHeight = Utils.isConfigureLandscape(getResources()) ? this.CURSOR_POS_HEIGHT : this.CURSOR_POS_HEIGHT_PORT;
        int maxCursorPos = Utils.isConfigureLandscape(getResources()) ? this.MAX_CURSOR_POS : this.MAX_CURSOR_POS_PORT;
        ImageView tmpImageView = (ImageView) findViewById(C0088R.id.zoom_cursor);
        LayoutParams tmpParam = (LayoutParams) tmpImageView.getLayoutParams();
        tmpParam.height = cursorHeight;
        tmpParam.width = cursorHeight;
        tmpImageView.setLayoutParams(tmpParam);
        RelativeLayout tmpRelativeLayout = (RelativeLayout) findViewById(C0088R.id.zoom_bar);
        tmpParam = (LayoutParams) tmpRelativeLayout.getLayoutParams();
        tmpParam.width = cursorHeight;
        tmpParam.height = maxCursorPos;
        tmpRelativeLayout.setLayoutParams(tmpParam);
        tmpRelativeLayout = (RelativeLayout) findViewById(C0088R.id.zoom_cursor_bg_view);
        tmpParam = (LayoutParams) tmpRelativeLayout.getLayoutParams();
        tmpParam.width = cursorHeight;
        tmpParam.height = cursorPosHeight;
        tmpRelativeLayout.setLayoutParams(tmpParam);
        tmpImageView = (ImageView) findViewById(C0088R.id.zoom_cursor_bg);
        tmpParam = (LayoutParams) tmpImageView.getLayoutParams();
        tmpParam.height = cursorPosHeight - cursorHeight;
        tmpImageView.setLayoutParams(tmpParam);
    }

    public void releaseBar() {
        if (this.mInitial) {
            int value = getValue();
            CamLog.m3d(CameraConstants.TAG, "mValue = " + value);
            if (this.mBarAction != null) {
                this.mBarAction.setBarSetting(this.mBarSettingKey, Integer.toString(value), true);
            }
            setBarValue(value);
            setCursor(value);
        }
    }

    public void startRotation(int degree, boolean animation) {
        RotateLayout rl = getBarLayout();
        if (rl != null && this.mBarAction != null) {
            LayoutParams barParams = (LayoutParams) getLayoutParams();
            RotateLayout tvRl = (RotateLayout) this.mBarAction.findViewById(C0088R.id.zoom_text_layout);
            LayoutParams layoutParam = (LayoutParams) tvRl.getLayoutParams();
            TextView tv = (TextView) this.mBarAction.findViewById(C0088R.id.zoom_text);
            LayoutParams tvLayoutParam = (LayoutParams) tv.getLayoutParams();
            Utils.resetLayoutParameter(barParams);
            Utils.resetLayoutParameter(layoutParam);
            Utils.resetLayoutParameter(tvLayoutParam);
            int convertDegree = changeDegreeFixedPortrait(degree);
            int buttonDegree = (degree + 270) % 180;
            int marginEnd_land = RatioCalcUtil.getSizeCalculatedByPercentage(getContext(), true, 0.232f);
            int marginEnd_port = Utils.getPx(getContext(), C0088R.dimen.zoom_view_port.marginEnd);
            if (ModelProperties.isLongLCDModel()) {
                Drawable icon = getContext().getDrawable(C0088R.drawable.camera_preview_bar_plus);
                if (icon != null) {
                    marginEnd_land = RatioCalcUtil.getSizeCalculatedByPercentage(getContext(), true, ModelProperties.getLCDType() > 1 ? 0.23f : 0.289f) - ((Utils.getPx(getContext(), C0088R.dimen.zoom_view_width) - icon.getIntrinsicWidth()) / 2);
                }
            }
            if (this.mBarAction.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                marginEnd_land = Utils.getLCDsize(getContext(), true)[1] + Utils.getPx(getContext(), C0088R.dimen.ae_control_bar_square_botton_margin);
            }
            if (this.minusButtonResId != 0) {
                ((RotateImageView) findViewById(this.minusButtonResId)).setDegree(buttonDegree, animation);
            }
            if (this.plusButtonResId != 0) {
                ((RotateImageView) findViewById(this.plusButtonResId)).setDegree(buttonDegree, animation);
            }
            if (this.cursorResId != 0) {
                ((RotateImageView) findViewById(this.cursorResId)).setDegree(buttonDegree, animation);
            }
            if (!Utils.isConfigureLandscape(getResources())) {
                switch (convertDegree) {
                    case 0:
                        barParams.addRule(21, 1);
                        barParams.addRule(15, 1);
                        barParams.setMarginStart(0);
                        barParams.setMarginEnd(marginEnd_port);
                        break;
                    case 90:
                        barParams.addRule(20, 1);
                        barParams.addRule(15, 1);
                        barParams.setMarginStart(marginEnd_land);
                        barParams.setMarginEnd(0);
                        layoutParam.addRule(17, C0088R.id.zoom_bar);
                        layoutParam.addRule(6, C0088R.id.zoom_bar);
                        break;
                    case 180:
                        barParams.addRule(20, 1);
                        barParams.addRule(15, 1);
                        barParams.setMarginStart(marginEnd_port);
                        barParams.setMarginEnd(0);
                        break;
                    case 270:
                        barParams.addRule(21, 1);
                        barParams.addRule(15, 1);
                        barParams.setMarginStart(0);
                        barParams.setMarginEnd(marginEnd_land);
                        layoutParam.addRule(16, C0088R.id.zoom_bar);
                        layoutParam.addRule(6, C0088R.id.zoom_bar);
                        break;
                }
            }
            switch (convertDegree) {
                case 0:
                    barParams.addRule(21, 1);
                    barParams.addRule(15, 1);
                    barParams.setMarginStart(0);
                    barParams.setMarginEnd(marginEnd_land);
                    break;
                case 90:
                    barParams.addRule(21, 1);
                    barParams.addRule(15, 1);
                    barParams.setMarginStart(0);
                    barParams.setMarginEnd(marginEnd_port);
                    break;
                case 180:
                    barParams.addRule(20, 1);
                    barParams.addRule(15, 1);
                    barParams.setMarginStart(marginEnd_land);
                    barParams.setMarginEnd(0);
                    break;
                case 270:
                    barParams.addRule(20, 1);
                    barParams.addRule(15, 1);
                    barParams.setMarginStart(marginEnd_port);
                    barParams.setMarginEnd(0);
                    break;
            }
            if (!(this.prevDegree == degree || (this.prevDegree - degree) % 180 == 0)) {
                int i;
                if (degree == 90) {
                    i = 0;
                } else {
                    i = Utils.getPx(this.mContext, C0088R.dimen.bar_cursor_text_margin_startEnd);
                }
                layoutParam.setMarginEnd(i);
                if (degree == 90 || degree == 270) {
                    layoutParam.topMargin += (tv.getWidth() - tv.getHeight()) / 2;
                } else {
                    layoutParam.topMargin -= (tv.getWidth() - tv.getHeight()) / 2;
                }
                this.prevDegree = degree;
            }
            tv.setLayoutParams(tvLayoutParam);
            tvRl.setLayoutParams(layoutParam);
            tvRl.rotateLayout(buttonDegree);
            setLayoutParams(barParams);
            rl.rotateLayout(convertDegree);
        }
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (this.mBarAction != null) {
            TextView tv = (TextView) this.mBarAction.findViewById(C0088R.id.zoom_text);
            if (tv != null) {
                if (FunctionProperties.isSupportedInAndOutZoom()) {
                    visibility = 8;
                }
                tv.setVisibility(visibility);
            }
        }
    }

    public void setMaxValue(int maxValue) {
        this.mZOOM_MAX_STEP = maxValue;
        setCursorMaxStep(this.mZOOM_MAX_STEP);
        if (this.mProgressBar != null) {
            this.mProgressBar.setMaxValue(this.mZOOM_MAX_STEP);
        }
        super.setMaxValue(maxValue);
    }

    public void updateExtraInfo(String info) {
        if (info != null && this.mBarAction != null) {
            TextView tv = (TextView) this.mBarAction.findViewById(C0088R.id.zoom_text);
            if (tv != null) {
                tv.setText(String.format(Locale.US, "X %.1f", new Object[]{Float.valueOf(Float.parseFloat(info) / 100.0f)}));
            }
        }
    }

    public void setProgress(int value) {
        if (this.mProgressBar != null) {
            this.mProgressBar.setProgress(value);
        }
    }

    public void unbind() {
        this.mProgressBar = null;
        super.unbind();
    }

    public void setBarEnable(boolean enabled) {
    }

    protected void sendLDBIntentOnTouchUp() {
    }
}
