package com.lge.camera.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class BeautyshotBar extends BarView {
    public static final int DEFAULT_EFFECT_LEVEL_MAX = 100;
    public static final int DEFAULT_EFFECT_LEVEL_MIN = 0;
    private int mBeautyMax = 0;
    private int mBeautyMin = 0;
    private int mBeautyStep = 0;
    private HandlerRunnable mHideTextRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            BeautyshotBar.this.setTextLevelVisibility(false);
        }
    };
    private int mPreviousLevelValue = 0;
    private BarProgress mProgressBar = null;
    private int mRelightingMax = 0;
    private int mRelightingMin = 0;
    private int mRelightingStep = 0;

    public BeautyshotBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BeautyshotBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BeautyshotBar(Context context) {
        super(context);
    }

    public void initBar(BarAction barAction, int type) {
        super.initBar(barAction, type);
        initValue();
        setBarValueInitialization(100, 0, 1, 1, type == 4 ? Setting.KEY_RELIGHTING : Setting.KEY_BEAUTYSHOT);
        setBarResources(C0088R.id.face_cursor, C0088R.id.face_cursor_bg_view, C0088R.id.face_minus_button, C0088R.id.face_minus_button_view, C0088R.id.face_plus_button, C0088R.id.face_plus_button_view, C0088R.id.beauty_text_layout);
        setListener(true);
        this.mProgressBar = (BarProgress) findViewById(C0088R.id.face_cursor_bg);
        LayoutParams layoutParam = (RelativeLayout.LayoutParams) this.mProgressBar.getLayoutParams();
        layoutParam.height = Utils.getPx(this.mContext, C0088R.dimen.beauty_bar_height);
        this.mProgressBar.setLayoutParams(layoutParam);
        this.mProgressBar.setContentDescription(this.mContext.getText(type == 4 ? C0088R.string.relighting_control_bar : C0088R.string.tone_control_bar));
        getBarSettingValue();
    }

    private void initValue() {
        int i = 80;
        int i2 = 10;
        this.mBeautyMax = FunctionProperties.getBeautyMax();
        this.mBeautyStep = FunctionProperties.getBeautyStep();
        this.mRelightingMax = FunctionProperties.getRelightingMax();
        this.mRelightingStep = FunctionProperties.getRelightingStep();
        this.mBeautyMax = this.mBeautyMax == 0 ? 80 : this.mBeautyMax;
        this.mBeautyStep = this.mBeautyStep == 0 ? 10 : this.mBeautyStep;
        if (this.mRelightingMax != 0) {
            i = this.mRelightingMax;
        }
        this.mRelightingMax = i;
        if (this.mRelightingStep != 0) {
            i2 = this.mRelightingStep;
        }
        this.mRelightingStep = i2;
    }

    protected RotateLayout getBarLayout() {
        if (this.mBarAction == null) {
            return null;
        }
        return (RotateLayout) this.mBarAction.findViewById(C0088R.id.beautyshot_rotate);
    }

    protected View getBarParentLayout() {
        if (this.mBarAction == null) {
            return null;
        }
        return this.mBarAction.findViewById(C0088R.id.beautyshot);
    }

    protected View getBarView() {
        if (this.mBarAction == null) {
            return null;
        }
        return this.mBarAction.findViewById(C0088R.id.beautyshot_bar);
    }

    public void getBarSettingValue() {
        int lValue = getCursorValue();
        if (this.mBarAction != null) {
            String settingValue = this.mBarAction.getSettingValue(this.mBarSettingKey);
            CamLog.m3d(CameraConstants.TAG, "[relighting] mBarSettingKey = " + this.mBarSettingKey + ", settingValue = " + settingValue);
            if ("".equals(settingValue) || "not found".equals(settingValue)) {
                lValue = ModelProperties.getBeautyDefaultLevel();
            } else {
                lValue = Integer.parseInt(settingValue);
            }
            int value = transformSettingValueToCursorValue(lValue);
            setCursorValue(value);
            setCursor(value);
            this.mProgressBar.setProgress(value);
        }
    }

    public void setLayoutDimension() {
        this.CURSOR_HEIGHT = (float) RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, false, 0.098f);
        this.CURSOR_HEIGHT_PORT = (float) RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, false, 0.098f);
        this.ADJUST_BEAUTY_CURSOR = RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, false, 0.028f);
        this.MIN_CURSOR_POS = RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, false, 0.07f);
        this.MAX_CURSOR_POS = RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, false, 0.826f);
        this.MAX_CURSOR_POS_PORT = RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, false, 0.826f);
        this.CURSOR_POS_HEIGHT = (int) ((((float) this.MAX_CURSOR_POS) - this.CURSOR_HEIGHT) - ((float) (this.MIN_CURSOR_POS * 2)));
        this.CURSOR_POS_HEIGHT_PORT = (int) ((((float) this.MAX_CURSOR_POS_PORT) - this.CURSOR_HEIGHT_PORT) - ((float) (this.MIN_CURSOR_POS * 2)));
        this.RELEASE_EXPAND_LEFT = Utils.getPx(getContext(), C0088R.dimen.adj_plus_button.releaseLeft);
        this.RELEASE_EXPAND_TOP = Utils.getPx(getContext(), C0088R.dimen.adj_plus_button.releaseTop);
        this.RELEASE_EXPAND_RIGHT = Utils.getPx(getContext(), C0088R.dimen.adj_plus_button.releaseRight);
        this.RELEASE_EXPAND_BOTTOM = Utils.getPx(getContext(), C0088R.dimen.adj_plus_button.releaseBottom);
    }

    public void releaseBar() {
        if (this.mInitial && this.mBarAction != null) {
            int step = this.mBarType == 1 ? this.mBeautyStep : this.mRelightingStep;
            int value = getValue();
            CamLog.m3d(CameraConstants.TAG, "mValue = " + value);
            this.mBarAction.setBarSetting(this.mBarSettingKey, Integer.toString(transformCursorValueToLevelValue(value) / step), true);
            this.mBarAction.updateAllBars(this.mBarType, getCursorValue());
        }
    }

    public void startRotation(int degree, boolean animation) {
        RotateLayout rl = getBarLayout();
        if (rl != null && this.mBarAction != null) {
            int marginEnd_land;
            int i;
            RelativeLayout.LayoutParams barParams = (RelativeLayout.LayoutParams) getLayoutParams();
            barParams.height = RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, false, 0.82f);
            RelativeLayout.LayoutParams cursor_bg_param = (RelativeLayout.LayoutParams) findViewById(C0088R.id.face_cursor_bg_view).getLayoutParams();
            cursor_bg_param.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getContext(), false, 0.015f);
            cursor_bg_param.height = RatioCalcUtil.getSizeCalculatedByPercentage(getContext(), false, 0.635f);
            setLayoutParams(cursor_bg_param);
            RotateLayout tvRl = (RotateLayout) this.mBarAction.findViewById(C0088R.id.beauty_text_layout);
            RelativeLayout.LayoutParams layoutParam = (RelativeLayout.LayoutParams) tvRl.getLayoutParams();
            TextView tv = (TextView) this.mBarAction.findViewById(C0088R.id.beautyshot_text);
            RelativeLayout.LayoutParams tvLayoutParam = (RelativeLayout.LayoutParams) tv.getLayoutParams();
            Utils.resetLayoutParameter(layoutParam);
            Utils.resetLayoutParameter(tvLayoutParam);
            Utils.resetLayoutParameter(barParams);
            int convertDegree = changeDegreeFixedPortrait(degree);
            int buttonDegree = changeButton(degree);
            if (ModelProperties.getLCDType() == 1) {
                marginEnd_land = RatioCalcUtil.getSizeCalculatedByPercentage(getContext(), true, 0.362f);
            } else if (ModelProperties.getLCDType() == 2) {
                marginEnd_land = RatioCalcUtil.getSizeCalculatedByPercentage(getContext(), true, 0.374f);
            } else {
                marginEnd_land = RatioCalcUtil.getSizeCalculatedByPercentage(getContext(), true, 0.362f);
            }
            if (this.mBarAction.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                marginEnd_land = (getContext().getDrawable(C0088R.drawable.btn_selfie_tone_normal).getIntrinsicHeight() * 2) + Utils.getLCDsize(getContext(), true)[1];
            }
            int marginEnd_port = Utils.getPx(getContext(), C0088R.dimen.beautyshot_view_port.marginEnd);
            if (this.minusButtonResId != 0) {
                RotateImageView rotateMinus = (RotateImageView) findViewById(this.minusButtonResId);
                if (this.mBarSettingKey.equals(Setting.KEY_BEAUTYSHOT)) {
                    rotateMinus.setImageResource(C0088R.drawable.camera_preview_bar_minus);
                } else if (this.mBarSettingKey.equals(Setting.KEY_RELIGHTING)) {
                    rotateMinus.setImageResource(C0088R.drawable.camera_preview_bar_relighting_minus);
                }
                rotateMinus.setDegree(buttonDegree, animation);
            }
            if (this.plusButtonResId != 0) {
                RotateImageView rotatePlus = (RotateImageView) findViewById(this.plusButtonResId);
                if (this.mBarSettingKey.equals(Setting.KEY_BEAUTYSHOT)) {
                    rotatePlus.setImageResource(C0088R.drawable.camera_preview_bar_plus);
                } else if (this.mBarSettingKey.equals(Setting.KEY_RELIGHTING)) {
                    rotatePlus.setImageResource(C0088R.drawable.camera_preview_bar_relighting_plus);
                }
                rotatePlus.setDegree(buttonDegree, animation);
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
                        layoutParam.addRule(17, C0088R.id.beautyshot_bar);
                        layoutParam.addRule(6, C0088R.id.beautyshot_bar);
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
                        layoutParam.addRule(16, C0088R.id.beautyshot_bar);
                        layoutParam.addRule(6, C0088R.id.beautyshot_bar);
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
            tv.setLayoutParams(tvLayoutParam);
            tvRl.setLayoutParams(layoutParam);
            tvRl.rotateLayout(buttonDegree);
            setLayoutParams(barParams);
            rl.rotateLayout(convertDegree);
        }
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == 8) {
            setTextLevelVisibility(false);
        }
    }

    public void setTextLevelVisibility(boolean visible) {
        if (this.mBarAction != null) {
            TextView tv = (TextView) this.mBarAction.findViewById(C0088R.id.beautyshot_text);
            if (tv != null) {
                tv.setVisibility(visible ? 0 : 8);
            }
        }
    }

    public void setMaxValue(int maxValue) {
        if (this.mProgressBar != null) {
            this.mProgressBar.setMaxValue(100);
        }
        super.setMaxValue(100);
    }

    public int changeButton(int degree) {
        int changeDegree = degree;
        if (degree == 0 || degree == 180) {
            return 90;
        }
        if (degree == 90 || degree == 270) {
            return 0;
        }
        return changeDegree;
    }

    public void setProgress(final int value) {
        if (this.mProgressBar != null && this.mBarAction != null) {
            this.mBarAction.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    BeautyshotBar.this.mProgressBar.setProgress(value);
                }
            });
        }
    }

    public void unbind() {
        this.mProgressBar = null;
        super.unbind();
    }

    public void updateBarWithValue(int value, boolean actionEnd) {
        if (this.mBarAction != null) {
            int lValue = this.mValue;
            int step = this.mBarType == 1 ? this.mBeautyStep : this.mRelightingStep;
            if (step != 0) {
                if (this.mInitial && actionEnd) {
                    setCursor(this.mValue);
                    lValue = transformCursorValueToLevelValue(lValue) / step;
                    CamLog.m3d(CameraConstants.TAG, "beauty setting value : " + lValue + " actionEnd : " + actionEnd);
                    setBarSettingValue(this.mBarSettingKey, lValue, actionEnd);
                }
                lValue = value;
                if (actionEnd) {
                    this.mBarAction.removePostRunnable(this.mHideTextRunnable);
                    this.mBarAction.postOnUiThread(this.mHideTextRunnable, 1500);
                    return;
                }
                if (lValue > this.mCursorMaxStep) {
                    lValue = this.mCursorMaxStep;
                }
                if (lValue < sCURSOR_MIN_STEP) {
                    lValue = sCURSOR_MIN_STEP;
                }
                this.mValue = lValue;
                setCursor(lValue);
                updateAllBars();
                lValue = transformCursorValueToLevelValue(lValue) / step;
                if (lValue != this.mPreviousLevelValue) {
                    CamLog.m3d(CameraConstants.TAG, "beauty setting value : " + lValue);
                    setBarSettingValue(this.mBarSettingKey, lValue, false);
                    updateExtraInfo(Integer.toString(lValue));
                }
                this.mPreviousLevelValue = lValue;
                this.mBarAction.removePostRunnable(this.mHideTextRunnable);
            }
        }
    }

    private int transformCursorValueToLevelValue(int value) {
        int max = 0;
        int min = 0;
        int step = 0;
        if (this.mBarType == 1) {
            max = this.mBeautyMax;
            min = this.mBeautyMin;
            step = this.mBeautyStep;
        } else if (this.mBarType == 4) {
            max = this.mRelightingMax;
            min = this.mRelightingMin;
            step = this.mRelightingStep;
        }
        if (step == 0) {
            return 0;
        }
        float fValue = (((float) value) * ((float) (max - min))) / 100.0f;
        if (((float) (((int) fValue) % step)) >= ((float) step) / 2.0f) {
            return ((((int) fValue) / step) * step) + step;
        }
        return (((int) fValue) / step) * step;
    }

    private int transformSettingValueToCursorValue(int value) {
        int max = 0;
        int min = 0;
        int step = 0;
        if (this.mBarType == 1) {
            max = this.mBeautyMax;
            min = this.mBeautyMin;
            step = this.mBeautyStep;
        } else if (this.mBarType == 4) {
            max = this.mRelightingMax;
            min = this.mRelightingMin;
            step = this.mRelightingStep;
        }
        if (max == min) {
            return 0;
        }
        return (int) (((((float) value) * ((float) step)) * 100.0f) / ((float) (max - min)));
    }

    public void updateExtraInfo(String info) {
        if (info != null && this.mBarAction != null) {
            TextView tv = (TextView) this.mBarAction.findViewById(C0088R.id.beautyshot_text);
            tv.setVisibility(0);
            tv.setText(info);
        }
    }

    public void setBarEnable(boolean enable) {
        if (enable) {
            setAlpha(1.0f);
            ((RelativeLayout) findViewById(C0088R.id.face_cursor_bg_view)).setEnabled(enable);
        } else {
            this.mBarTouchState = false;
            setAlpha(0.4f);
            ((RelativeLayout) findViewById(C0088R.id.face_cursor_bg_view)).setEnabled(enable);
            setTextLevelVisibility(false);
        }
        super.setEnabled(enable);
    }

    public void setBarEnable(boolean enable, boolean changedColor) {
        if (changedColor) {
            setBarEnable(enable);
            return;
        }
        this.mBarTouchState = false;
        ((RelativeLayout) findViewById(C0088R.id.face_cursor_bg_view)).setEnabled(enable);
        super.setEnabled(enable);
    }
}
