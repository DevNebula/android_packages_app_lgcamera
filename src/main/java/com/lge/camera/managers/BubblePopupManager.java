package com.lge.camera.managers;

import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class BubblePopupManager extends ManagerInterfaceImpl {
    private final Object mLock = new Object();
    protected float mMarginBottomRatio = 0.128f;
    private boolean mNotiComplete = false;
    private View mPopupView = null;

    public BubblePopupManager(ModuleInterface moduleInterface, float marginBottom) {
        super(moduleInterface);
        this.mMarginBottomRatio = marginBottom;
    }

    public void onResumeAfter() {
        setupView();
        super.onResumeAfter();
    }

    private void setupView() {
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        this.mPopupView = this.mGet.inflateView(C0088R.layout.bubble_popup);
        if (vg != null && this.mPopupView != null) {
            this.mPopupView.setVisibility(8);
            vg.addView(this.mPopupView);
        }
    }

    public void showBubblePopup() {
        if (!this.mNotiComplete) {
            if (this.mPopupView == null) {
                setupView();
            }
            View view = this.mGet.findViewById(C0088R.id.bubble_popup_layout);
            setRotateDegree(this.mGet.getOrientationDegree(), false);
            setBubblePopupAnimation(view, true);
            this.mNotiComplete = true;
        }
    }

    private void setLayout(RotateLayout rl, int degree) {
        if (rl != null) {
            int endMargin;
            LayoutParams params = (LayoutParams) rl.getLayoutParams();
            View bubbleView = this.mGet.findViewById(C0088R.id.bubble_popup);
            rl.measure(0, 0);
            RotateTextView rtv = (RotateTextView) this.mGet.findViewById(C0088R.id.bubble_popup_message);
            String message = getMessageTest();
            Utils.resetLayoutParameter(params);
            boolean isConfigureLandscape = Utils.isConfigureLandscape(this.mGet.getActivity().getResources());
            params.addRule(12, 1);
            params.addRule(21, 1);
            int lcd_width = Utils.getLCDsize(this.mGet.getActivity(), true)[0];
            int lcd_height = Utils.getLCDsize(this.mGet.getActivity(), true)[1];
            int topMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.bubble_popup.marginTop);
            int bottomMargin = Utils.getPx(getAppContext(), C0088R.dimen.quick_clip_init_bg_margin_bottom);
            this.mMarginBottomRatio = "on".equals(this.mGet.getSettingValue(Setting.KEY_TILE_PREVIEW)) ? 0.14f : 0.128f;
            int popupMargin = (RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, this.mMarginBottomRatio) + Utils.getPx(getAppContext(), C0088R.dimen.quick_clip_init_bg_width)) + RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.01f);
            int startMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.bubble_popup.marginStart);
            if (ModelProperties.isTablet(getAppContext())) {
                endMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.07f);
            } else {
                endMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.bubble_popup.marginEnd);
            }
            if (Utils.isEqualDegree(isConfigureLandscape, degree, 0) || Utils.isEqualDegree(isConfigureLandscape, degree, 180)) {
                rtv.setText(Utils.breakTextToMultiLine(rtv.getTextPaint(), message, (((lcd_width - topMargin) - bottomMargin) - bubbleView.getPaddingStart()) - bubbleView.getPaddingEnd()));
                startMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.bubble_popup_land.marginStart);
                if (ModelProperties.isTablet(getAppContext())) {
                    endMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.09f);
                } else {
                    endMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.bubble_popup_land.marginStart);
                }
                bubbleView.setBackgroundResource(Utils.isEqualDegree(isConfigureLandscape, degree, 0) ? C0088R.drawable.camera_bubble_bg : C0088R.drawable.camera_bubble_bg_opposite);
            } else {
                rtv.setText(Utils.breakTextToMultiLine(rtv.getTextPaint(), message, (((lcd_height - startMargin) - endMargin) - bubbleView.getPaddingStart()) - bubbleView.getPaddingEnd()));
                bubbleView.setBackgroundResource(Utils.isEqualDegree(isConfigureLandscape, degree, 90) ? C0088R.drawable.camera_bubble_bg_port : C0088R.drawable.camera_bubble_bg_port_opposite);
            }
            if (isConfigureLandscape) {
                params.setMarginEnd(popupMargin);
                params.topMargin = endMargin;
                params.setMarginStart(topMargin);
                params.bottomMargin = startMargin;
            } else {
                params.setMarginEnd(endMargin);
                params.topMargin = topMargin;
                params.setMarginStart(startMargin);
                params.bottomMargin = popupMargin;
            }
            rl.setLayoutParams(params);
        }
    }

    public void removeBubblePopup(boolean direct) {
        if (this.mPopupView != null) {
            if (!direct) {
                setBubblePopupAnimation(this.mGet.findViewById(C0088R.id.bubble_popup_layout), false);
            }
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    ViewGroup vg = (ViewGroup) BubblePopupManager.this.mGet.findViewById(C0088R.id.camera_controls);
                    if (vg != null) {
                        vg.removeView(BubblePopupManager.this.mPopupView);
                        BubblePopupManager.this.mPopupView = null;
                    }
                }
            }, 0);
        }
    }

    private void setBubblePopupAnimation(final View aniView, final boolean show) {
        CamLog.m3d(CameraConstants.TAG, "setBubblePopupAnimation-start");
        if (aniView != null) {
            Animation showAni = AnimationUtils.loadAnimation(this.mGet.getAppContext(), C0088R.anim.fade_in);
            Animation hideAni = AnimationUtils.loadAnimation(this.mGet.getAppContext(), C0088R.anim.fade_out);
            if (this.mGet.getActivity().isFinishing() || this.mGet.isPaused()) {
                aniView.clearAnimation();
                aniView.setVisibility(4);
                return;
            }
            Animation animation;
            aniView.clearAnimation();
            aniView.setVisibility(4);
            if (show) {
                animation = showAni;
            } else {
                animation = hideAni;
            }
            if (animation != null) {
                animation.setAnimationListener(new AnimationListener() {
                    public void onAnimationStart(Animation animation) {
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }

                    public void onAnimationEnd(Animation animation) {
                        BubblePopupManager.this.mGet.postOnUiThread(new HandlerRunnable(BubblePopupManager.this) {
                            public void handleRun() {
                                ViewGroup vg = (ViewGroup) BubblePopupManager.this.mGet.findViewById(C0088R.id.camera_controls);
                                if (aniView != null && vg != null && vg.indexOfChild(BubblePopupManager.this.mPopupView) >= 0) {
                                    if (show) {
                                        aniView.setVisibility(0);
                                    } else {
                                        aniView.setVisibility(8);
                                    }
                                }
                            }
                        }, 0);
                    }
                });
                aniView.startAnimation(animation);
            }
        }
    }

    public void setRotateDegree(int degree, boolean animation) {
        View view = this.mGet.findViewById(C0088R.id.bubble_popup_layout);
        if (view != null) {
            RotateLayout rl = (RotateLayout) view;
            setLayout(rl, degree);
            rl.rotateLayout(degree);
        }
    }

    public void setDegree(int degree, boolean animation) {
        super.setDegree(degree, animation);
    }

    public void onConfigurationChanged(Configuration config) {
        synchronized (this.mLock) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    BubblePopupManager.this.setupView();
                    BubblePopupManager.this.setDegree(BubblePopupManager.this.mGet.getOrientationDegree(), false);
                }
            }, 0);
        }
        super.onConfigurationChanged(config);
    }

    public void beforeConfigChanged() {
        removeBubblePopup(true);
    }

    public void initializeNotiComplete() {
        this.mNotiComplete = false;
    }

    private String getMessageTest() {
        return this.mGet.getActivity().getString(C0088R.string.quick_clip_guide_pop_up);
    }
}
