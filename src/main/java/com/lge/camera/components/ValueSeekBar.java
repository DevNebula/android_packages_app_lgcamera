package com.lge.camera.components;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class ValueSeekBar {
    private SeekBar mBar;
    private LinearLayout mBarWrapper;
    private final Context mContext;
    private int mCursorHeight;
    private int mDivideValue = 1;
    private Handler mHandler = new Handler();
    private final LayoutInflater mInflater;
    private TextView mLabelLand;
    private TextView mLabelPort;
    private int mLabelPortReverseTopMargin;
    private int mLabelPortTopMargin;
    private Runnable mRemoveText = new C05562();
    private RelativeLayout mRoot;
    private int mShowingValue;
    private TextView mText;
    private LayoutParams mTextLp;
    private int mType;

    /* renamed from: com.lge.camera.components.ValueSeekBar$2 */
    class C05562 implements Runnable {
        C05562() {
        }

        public void run() {
            ValueSeekBar.this.setTextVisibility(8);
        }
    }

    public interface OnValueSeekBarChangeListener {
        void onProgressChanged(ValueSeekBar valueSeekBar, int i, boolean z, boolean z2);

        void onStartTrackingTouch(ValueSeekBar valueSeekBar);

        void onStopTrackingTouch(ValueSeekBar valueSeekBar);
    }

    public ValueSeekBar(Context context, int type, ViewGroup vg) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mType = type;
        this.mInflater.inflate(C0088R.layout.value_seekbar, vg);
        this.mRoot = (RelativeLayout) vg.findViewById(C0088R.id.value_seekBar_root);
        this.mRoot.setId(View.generateViewId());
        this.mBarWrapper = (LinearLayout) this.mRoot.findViewById(C0088R.id.value_seekBar_wrapper);
        this.mBarWrapper.setId(View.generateViewId());
        this.mBar = (SeekBar) this.mRoot.findViewById(C0088R.id.value_seekBar);
        this.mText = (TextView) this.mRoot.findViewById(C0088R.id.value_seekBar_level);
        LinearLayout.LayoutParams barLp = (LinearLayout.LayoutParams) this.mBar.getLayoutParams();
        barLp.width = (Utils.getPx(this.mContext, C0088R.dimen.value_seekBar_width) + Utils.getPx(this.mContext, C0088R.dimen.value_seekBar_paddingStart)) + Utils.getPx(this.mContext, C0088R.dimen.value_seekBar_paddingEnd);
        this.mBar.setLayoutParams(barLp);
        Drawable cursor = this.mContext.getDrawable(C0088R.drawable.ic_camera_cursor);
        if (cursor != null) {
            this.mCursorHeight = cursor.getIntrinsicHeight();
        }
        this.mTextLp = (LayoutParams) this.mText.getLayoutParams();
        this.mTextLp.height = this.mCursorHeight;
        this.mTextLp.width = this.mCursorHeight;
        this.mTextLp.setMarginStart(Utils.getPx(this.mContext, C0088R.dimen.value_seekBar_level_marginStart));
        this.mTextLp.addRule(17, this.mBarWrapper.getId());
        this.mTextLp.addRule(6, this.mBarWrapper.getId());
        this.mText.setLayoutParams(this.mTextLp);
    }

    public void setProgress(int progress) {
        if (this.mBar != null) {
            this.mBar.setProgress(progress);
            this.mShowingValue = getShowingValueByProgress(progress);
        }
    }

    public int getProgress() {
        if (this.mBar != null) {
            return this.mBar.getProgress();
        }
        return -1;
    }

    public int getType() {
        return this.mType;
    }

    public int getShowingValueByProgress(int progress) {
        if (this.mDivideValue != 1) {
            return (int) Math.floor(((double) progress) / ((double) this.mDivideValue));
        }
        return progress;
    }

    public int getValue() {
        return this.mShowingValue * this.mDivideValue;
    }

    public void setSeekBarDescription(String description) {
        if (this.mBar != null) {
            this.mBar.setContentDescription(description);
        }
    }

    public void setLabel(String label) {
        if (this.mRoot != null && this.mContext != null) {
            this.mLabelPort = (TextView) this.mRoot.findViewById(C0088R.id.value_seekBar_label_port);
            this.mLabelPort.setText(label);
            this.mLabelPort.measure(0, 0);
            LayoutParams labelLp = (LayoutParams) this.mLabelPort.getLayoutParams();
            this.mLabelPortTopMargin = ((this.mCursorHeight / 2) - Utils.getPx(this.mContext, C0088R.dimen.value_seekBar_label_marginBottom_port)) - this.mLabelPort.getMeasuredHeight();
            this.mLabelPortReverseTopMargin = Utils.getPx(this.mContext, C0088R.dimen.value_seekBar_label_marginBottom_port) + (this.mCursorHeight / 2);
            labelLp.topMargin = this.mLabelPortTopMargin;
            labelLp.addRule(6, this.mBarWrapper.getId());
            labelLp.addRule(18, this.mBarWrapper.getId());
            this.mLabelPort.setLayoutParams(labelLp);
            this.mLabelLand = (TextView) this.mRoot.findViewById(C0088R.id.value_seekBar_label_land);
            this.mLabelLand.setText(label);
            this.mLabelLand.measure(0, 0);
            int labelMarginLandCompensationValue = (this.mLabelLand.getMeasuredHeight() - this.mLabelLand.getMeasuredWidth()) / 2;
            labelLp = (LayoutParams) this.mLabelLand.getLayoutParams();
            labelLp.topMargin = (this.mCursorHeight - this.mLabelLand.getMeasuredHeight()) / 2;
            labelLp.setMarginEnd(Utils.getPx(this.mContext, C0088R.dimen.value_seekBar_label_marginEnd_land) + labelMarginLandCompensationValue);
            labelLp.addRule(16, this.mBarWrapper.getId());
            labelLp.addRule(6, this.mBarWrapper.getId());
            this.mLabelLand.setLayoutParams(labelLp);
        }
    }

    public void setOnValueSeekBarChangeListener(final OnValueSeekBarChangeListener listener) {
        if (this.mBar != null) {
            this.mBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    boolean valueChanged = ValueSeekBar.this.mShowingValue != ValueSeekBar.this.getShowingValueByProgress(progress);
                    if (valueChanged) {
                        ValueSeekBar.this.mShowingValue = ValueSeekBar.this.getShowingValueByProgress(progress);
                    }
                    ValueSeekBar.this.updateText();
                    if (listener != null) {
                        listener.onProgressChanged(ValueSeekBar.this, progress, fromUser, valueChanged);
                    }
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                    if (listener != null) {
                        listener.onStartTrackingTouch(ValueSeekBar.this);
                    }
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (listener != null) {
                        listener.onStopTrackingTouch(ValueSeekBar.this);
                    }
                }
            });
        }
    }

    public void setDivideValue(int value) {
        this.mDivideValue = value;
    }

    public void setRotation(int degree) {
        int i;
        int textDegree = degree % 180 == 0 ? degree : degree + 180;
        if (this.mText != null) {
            TextView textView = this.mText;
            i = (degree == 90 || degree == 270) ? 81 : 16;
            textView.setGravity(i);
            this.mText.setRotation((float) textDegree);
            if (this.mText.getVisibility() == 0) {
                updateText();
            }
        }
        if (this.mRoot != null) {
            RelativeLayout relativeLayout = this.mRoot;
            if (degree == 90 || degree == 180) {
                i = 1;
            } else {
                i = 0;
            }
            relativeLayout.setLayoutDirection(i);
        }
        if (this.mLabelPort != null && this.mLabelLand != null) {
            if (degree == 90 || degree == 270) {
                this.mLabelLand.setVisibility(0);
                this.mLabelPort.setVisibility(8);
                this.mLabelLand.setRotation((float) textDegree);
                return;
            }
            this.mLabelLand.setVisibility(8);
            this.mLabelPort.setVisibility(0);
            this.mLabelPort.setRotation((float) textDegree);
            LayoutParams labelLp = (LayoutParams) this.mLabelPort.getLayoutParams();
            labelLp.topMargin = degree == 180 ? this.mLabelPortReverseTopMargin : this.mLabelPortTopMargin;
            this.mLabelPort.setLayoutParams(labelLp);
        }
    }

    public void setBarMargins(int left, int top, int right, int bottom) {
        if (this.mBar != null) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.mBar.getLayoutParams();
            if (lp != null) {
                lp.setMargins(left, top, right, bottom);
                this.mBar.setLayoutParams(lp);
            }
        }
    }

    public void setFilterStrengthBarLayout(int degree, Context context, String shotMode, boolean supportSticker) {
        if (this.mBarWrapper != null && this.mRoot != null) {
            LayoutParams barWrapperParams = (LayoutParams) this.mBarWrapper.getLayoutParams();
            LayoutParams rootParam = (LayoutParams) this.mRoot.getLayoutParams();
            if (rootParam != null && barWrapperParams != null) {
                Utils.resetLayoutParameter(rootParam);
                Utils.resetLayoutParameter(barWrapperParams);
                barWrapperParams.addRule(14);
                if (null == null || 0 == 90) {
                    int bottomMargin;
                    if (ModelProperties.getLCDType() == 2) {
                        bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(context, true, 0.435f);
                    } else if (supportSticker) {
                        bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(context, true, 0.41f);
                    } else {
                        bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(context, true, 0.353f);
                    }
                    if (shotMode.contains(CameraConstants.MODE_SQUARE)) {
                        if (ModelProperties.getLCDType() == 2) {
                            bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(context, true, 0.628f);
                        } else if (supportSticker) {
                            bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(context, true, 0.6944f);
                        } else {
                            bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(context, true, 0.6416f);
                        }
                    }
                    rootParam.bottomMargin = bottomMargin;
                    barWrapperParams.addRule(12);
                    return;
                }
                rootParam.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(context, true, 0.324f);
                barWrapperParams.addRule(10);
            }
        }
    }

    public void setVisibility(int visibility) {
        if (this.mRoot != null) {
            this.mRoot.setVisibility(visibility);
            updateText();
        }
    }

    public void setTextVisibility(int visibility) {
        if (this.mText != null) {
            this.mText.setVisibility(visibility);
        }
    }

    public void removeTextDelayed(long delay) {
        this.mHandler.removeCallbacks(this.mRemoveText);
        this.mHandler.postDelayed(this.mRemoveText, delay);
    }

    private void updateText() {
        if (this.mText != null) {
            this.mText.setText(Integer.toString(this.mShowingValue));
        }
    }
}
