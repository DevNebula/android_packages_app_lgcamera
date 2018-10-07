package com.lge.camera.components;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import com.lge.camera.C0088R;

public class SpeedWarningHandler extends Handler {
    public static final int DURATION_SPEED_WARNING = 1000;
    public static final int MSG_SPEED_WARNING_HIDE = 2;
    public static final int MSG_SPEED_WARNING_SHOW = 1;
    private boolean mIsTextSpeedShowing = false;
    private View mTextLayout = null;
    private TextView mTextSpeed = null;

    /* renamed from: com.lge.camera.components.SpeedWarningHandler$1 */
    class C05491 implements AnimationListener {
        C05491() {
        }

        public void onAnimationStart(Animation animation) {
            SpeedWarningHandler.this.mIsTextSpeedShowing = true;
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            if (SpeedWarningHandler.this.mTextLayout != null) {
                SpeedWarningHandler.this.mTextLayout.setVisibility(0);
            }
        }
    }

    /* renamed from: com.lge.camera.components.SpeedWarningHandler$2 */
    class C05502 implements AnimationListener {
        C05502() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            if (SpeedWarningHandler.this.mTextLayout != null) {
                SpeedWarningHandler.this.mTextLayout.setVisibility(4);
            }
            SpeedWarningHandler.this.mIsTextSpeedShowing = false;
        }
    }

    public SpeedWarningHandler(View baseView) {
        if (baseView != null) {
            this.mTextLayout = baseView.findViewById(C0088R.id.panorama_warning_text_layout);
            this.mTextSpeed = (TextView) baseView.findViewById(C0088R.id.panorama_warning_text);
        }
    }

    public void unbind() {
        if (this.mTextLayout != null) {
            this.mTextLayout.clearAnimation();
            this.mTextLayout.setVisibility(4);
            this.mTextLayout = null;
        }
        this.mTextSpeed = null;
    }

    public void handleMessage(Message msg) {
        if (this.mTextLayout != null && this.mTextSpeed != null) {
            if (msg.what == 1) {
                if (!this.mIsTextSpeedShowing) {
                    if (msg.arg1 > 0) {
                        this.mTextSpeed.setText(msg.arg1);
                    }
                    showAnimation();
                }
            } else if (msg.what == 2 && this.mIsTextSpeedShowing) {
                hideAnimation();
            }
        }
    }

    public void showAnimation() {
        startShowingAnimation(this.mTextLayout, true, 100, new C05491(), true);
    }

    public void hideAnimation() {
        if (this.mIsTextSpeedShowing) {
            startShowingAnimation(this.mTextLayout, false, 100, new C05502(), true);
        }
    }

    public static void startShowingAnimation(View aniView, boolean show, long duration, AnimationListener listener, boolean showAgain) {
        int i = 0;
        float end = 1.0f;
        if (aniView != null) {
            float start;
            aniView.clearAnimation();
            if (!showAgain) {
                int i2;
                int visibility = aniView.getVisibility();
                if (show) {
                    i2 = 0;
                } else {
                    i2 = 4;
                }
                if (visibility == i2) {
                    return;
                }
            }
            if (!show) {
                i = 4;
            }
            aniView.setVisibility(i);
            if (show) {
                start = 0.0f;
            } else {
                start = 1.0f;
            }
            if (!show) {
                end = 0.0f;
            }
            Animation anim = new AlphaAnimation(start, end);
            anim.setDuration(duration);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setAnimationListener(listener);
            aniView.startAnimation(anim);
        }
    }
}
