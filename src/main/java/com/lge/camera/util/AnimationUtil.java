package com.lge.camera.util;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;

public class AnimationUtil {
    public static final int ANI_ALL = 3;
    public static final int DIRECTION_TO_DOWN = 3;
    public static final int DIRECTION_TO_LEFT = 0;
    public static final int DIRECTION_TO_RIGHT = 1;
    public static final int DIRECTION_TO_UP = 2;
    public static final long DUR_100 = 100;
    public static final long DUR_130 = 130;
    public static final long DUR_150 = 150;
    public static final long DUR_200 = 200;
    public static final long DUR_250 = 250;
    public static final long DUR_300 = 300;
    public static final long DUR_350 = 350;
    public static final long DUR_400 = 400;
    public static final long DUR_50 = 50;
    public static final long DUR_500 = 500;
    public static final long DUR_750 = 750;
    public static final int SNAPSHOT_ANI = 1;
    public static final int SWITCH_ANI = 2;
    private static float sSnapShotAnimationFactor = 1.5f;

    public static void startAlphaAnimation(View view, float start, float end, long duration, AnimationListener al) {
        if (view != null) {
            Animation anim = new AlphaAnimation(start, end);
            anim.setDuration(duration);
            anim.setAnimationListener(al);
            anim.setInterpolator(new DecelerateInterpolator());
            view.clearAnimation();
            view.startAnimation(anim);
        }
    }

    public static void startShowingAnimation(View aniView, boolean show, long duration, AnimationListener listener) {
        startShowingAnimation(aniView, show, duration, listener, true);
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

    public static void startAnimationList(ImageView view, boolean show) {
        AnimationDrawable ad = (AnimationDrawable) view.getBackground();
        if (show) {
            ad.start();
        } else {
            ad.stop();
        }
    }

    public static void startTransAnimationForSetting(View aniView, boolean show, AnimationListener listener, int degree) {
        if (aniView != null) {
            float fromX;
            float toX;
            float fromY;
            float toY;
            aniView.clearAnimation();
            aniView.setVisibility(show ? 0 : 4);
            if (degree == 0) {
                if (show) {
                    fromX = 1.0f;
                } else {
                    fromX = 0.0f;
                }
                if (show) {
                    toX = 0.0f;
                } else {
                    toX = 1.0f;
                }
                fromY = 0.0f;
                toY = 0.0f;
            } else if (degree == 180) {
                if (show) {
                    fromX = -1.0f;
                } else {
                    fromX = 0.0f;
                }
                if (show) {
                    toX = 0.0f;
                } else {
                    toX = -1.0f;
                }
                fromY = 0.0f;
                toY = 0.0f;
            } else if (degree == 90) {
                fromX = 0.0f;
                toX = 0.0f;
                if (show) {
                    fromY = -1.0f;
                } else {
                    fromY = 0.0f;
                }
                if (show) {
                    toY = 0.0f;
                } else {
                    toY = -1.0f;
                }
            } else {
                fromX = 0.0f;
                toX = 0.0f;
                if (show) {
                    fromY = 1.0f;
                } else {
                    fromY = 0.0f;
                }
                if (show) {
                    toY = 0.0f;
                } else {
                    toY = 1.0f;
                }
            }
            Animation transAni = new TranslateAnimation(1, fromX, 1, toX, 1, fromY, 1, toY);
            AnimationSet aniSet = new AnimationSet(true);
            aniSet.addAnimation(transAni);
            aniSet.setDuration(130);
            aniSet.setInterpolator(new DecelerateInterpolator());
            aniSet.setAnimationListener(listener);
            aniView.startAnimation(aniSet);
        }
    }

    public static void startTransAnimationForMode(View aniView, boolean show, AnimationListener listener) {
        float toY = -1.0f;
        if (aniView != null) {
            float fromY;
            aniView.clearAnimation();
            aniView.setVisibility(show ? 0 : 4);
            if (show) {
                fromY = -1.0f;
            } else {
                fromY = 0.0f;
            }
            if (show) {
                toY = 0.0f;
            }
            Animation transAni = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, fromY, 1, toY);
            AnimationSet aniSet = new AnimationSet(true);
            aniSet.addAnimation(transAni);
            aniSet.setDuration(200);
            aniSet.setInterpolator(new DecelerateInterpolator());
            aniSet.setAnimationListener(listener);
            aniView.startAnimation(aniSet);
        }
    }

    public static void startTransAnimationForPullDownMenu(View aniView, boolean show, AnimationListener listener) {
        float toX = 1.0f;
        if (aniView != null) {
            float fromX;
            aniView.clearAnimation();
            aniView.setVisibility(show ? 0 : 4);
            if (show) {
                fromX = 1.0f;
            } else {
                fromX = 0.0f;
            }
            if (show) {
                toX = 0.0f;
            }
            Animation transAni = new TranslateAnimation(1, fromX, 1, toX, 1, 0.0f, 1, 0.0f);
            AnimationSet aniSet = new AnimationSet(true);
            aniSet.addAnimation(transAni);
            aniSet.setDuration(show ? 750 : 400);
            aniSet.setInterpolator(show ? new DecelerateInterpolator(3.0f) : new AccelerateInterpolator(3.0f));
            aniSet.setAnimationListener(listener);
            aniView.startAnimation(aniSet);
        }
    }

    public static void startTransAnimation(View aniView, boolean show, boolean landscape, AnimationListener listener) {
        startTransAnimation(aniView, show, landscape, listener, true);
    }

    public static void startTransAnimation(View aniView, boolean show, boolean landscape, AnimationListener listener, boolean showFromLeft) {
        if (aniView != null) {
            aniView.clearAnimation();
            aniView.setVisibility(show ? 0 : 4);
            float fromAlpha = show ? 0.0f : 1.0f;
            float toAlpha = show ? 1.0f : 0.0f;
            float fromX = show ? -1.0f : 0.0f;
            float toX = show ? 0.0f : -1.0f;
            float fromY = 0.0f;
            float toY = 0.0f;
            if (!showFromLeft) {
                fromX = show ? 1.0f : 0.0f;
                toX = show ? 0.0f : 1.0f;
            }
            if (!landscape) {
                fromY = fromX;
                toY = toX;
                fromX = 0.0f;
                toX = 0.0f;
            }
            Animation transAni = new TranslateAnimation(1, fromX, 1, toX, 1, fromY, 1, toY);
            Animation alphaAni = new AlphaAnimation(fromAlpha, toAlpha);
            AnimationSet aniSet = new AnimationSet(true);
            aniSet.addAnimation(transAni);
            aniSet.addAnimation(alphaAni);
            aniSet.setDuration(200);
            aniSet.setAnimationListener(listener);
            aniSet.setInterpolator(new DecelerateInterpolator());
            aniView.startAnimation(aniSet);
        }
    }

    public static void startSnapShotAnimation(View aniView, boolean show, long duration, AnimationListener listener) {
        float end = 1.0f;
        if (aniView != null) {
            float start;
            aniView.clearAnimation();
            aniView.setVisibility(show ? 0 : 4);
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

    public static void startShutterTransAnimation(View aniView, int distance, long duration, boolean fromOrigin, AnimationListener listener) {
        float toX = 0.0f;
        if (aniView != null) {
            int i;
            aniView.clearAnimation();
            float fromX = fromOrigin ? 0.0f : (float) distance;
            if (fromOrigin) {
                toX = (float) distance;
            }
            if (fromOrigin) {
                i = 1;
            } else {
                i = 0;
            }
            if (fromOrigin) {
            }
            Animation transAni = new TranslateAnimation(i, fromX, 0, toX, 1, 0.0f, 1, 0.0f);
            AnimationSet aniSet = new AnimationSet(true);
            aniSet.addAnimation(transAni);
            aniSet.setDuration(duration);
            aniSet.setInterpolator(new DecelerateInterpolator());
            aniSet.setAnimationListener(listener);
            aniView.startAnimation(aniSet);
        }
    }

    public static void startShutterScaleAnimation(View aniView, float scale, int distance, long duration, AnimationListener listener) {
        if (aniView != null) {
            aniView.clearAnimation();
            Animation scaleAni = new ScaleAnimation(scale, 1.0f, scale, 1.0f, 1, 1.0f, 1, 0.5f);
            Animation transAni = new TranslateAnimation(0, (float) distance, 1, 0.0f, 1, 0.0f, 1, 0.0f);
            Animation animationSet = new AnimationSet(true);
            animationSet.addAnimation(scaleAni);
            animationSet.addAnimation(transAni);
            animationSet.setDuration(duration);
            animationSet.setInterpolator(new DecelerateInterpolator());
            animationSet.setAnimationListener(listener);
            aniView.startAnimation(animationSet);
        }
    }

    public static void startShutterTopAnimation(View aniView, int distance, long duration, boolean show, AnimationListener listener) {
        if (aniView != null) {
            aniView.clearAnimation();
            float fromAlpha = show ? 0.0f : 1.0f;
            float toAlpha = show ? 1.0f : 0.0f;
            Animation transAni = new TranslateAnimation(show ? 0 : 1, show ? (float) distance : 0.0f, show ? 1 : 0, show ? 0.0f : (float) distance, 1, 0.0f, 1, 0.0f);
            AnimationSet aniSet = new AnimationSet(true);
            Animation alphaAni = new AlphaAnimation(fromAlpha, toAlpha);
            aniSet.addAnimation(transAni);
            aniSet.addAnimation(alphaAni);
            aniSet.setDuration(duration);
            aniSet.setInterpolator(new DecelerateInterpolator());
            aniSet.setAnimationListener(listener);
            aniView.startAnimation(aniSet);
        }
    }

    public static void startRotateAnimationForRotateLayout(View aniView, int startDegree, int targetDegree, boolean isClockwise, int duration, AnimationListener listener) {
        int rotateDegree;
        aniView.clearAnimation();
        int diff = targetDegree - startDegree;
        if (diff == 270) {
            rotateDegree = -90;
        } else if (diff == -270) {
            rotateDegree = 90;
        } else {
            rotateDegree = diff;
        }
        Animation rotateAni = new RotateAnimation((float) rotateDegree, 0.0f, 1, 0.5f, 1, 0.5f);
        AnimationSet aniSet = new AnimationSet(true);
        aniSet.addAnimation(rotateAni);
        aniSet.setDuration(350);
        aniSet.setInterpolator(new DecelerateInterpolator());
        aniSet.setAnimationListener(listener);
        aniView.startAnimation(aniSet);
    }

    public static void startSnapshotCaptureThumbnailAnimation(View aniView, Context c, int degree, boolean isUpDown, AnimationListener listener) {
        float fromX = 0.0f;
        float toX = 0.0f;
        float fromY = 0.0f;
        float toY = 0.0f;
        float direction = Utils.isRTLLanguage() ? -1.0f : 1.0f;
        if (isUpDown) {
            fromY = -1.0f;
            toY = 0.0f;
        } else if (degree == 0) {
            fromX = -1.0f * direction;
            toX = 0.0f * direction;
        } else if (degree == 90) {
            fromY = 1.0f * direction;
            toY = 0.0f * direction;
        } else if (degree == 180) {
            fromX = 1.0f * direction;
            toX = 0.0f * direction;
        } else if (degree == 270) {
            fromY = -1.0f * direction;
            toY = 0.0f * direction;
        }
        Animation transAni = new TranslateAnimation(1, fromX, 1, toX, 1, fromY, 1, toY);
        AnimationSet aniSet = new AnimationSet(true);
        aniSet.addAnimation(transAni);
        aniSet.setDuration(300);
        aniSet.setInterpolator(new DecelerateInterpolator(sSnapShotAnimationFactor));
        aniSet.setAnimationListener(listener);
        aniView.startAnimation(aniSet);
    }

    public static void startSnapshotGalleryViewAnimation(View aniView, Context c, int degree, AnimationListener listener) {
        float fromX = 0.0f;
        float toX = 0.0f;
        float fromY = 0.0f;
        float toY = 0.0f;
        float direction = Utils.isRTLLanguage() ? -1.0f : 1.0f;
        if (degree == 0) {
            fromX = 0.0f * direction;
            toX = 1.0f * direction;
        } else if (degree == 90) {
            fromY = 0.0f * direction;
            toY = -1.0f * direction;
        } else if (degree == 180) {
            fromX = 0.0f * direction;
            toX = -1.0f * direction;
        } else if (degree == 270) {
            fromY = 0.0f * direction;
            toY = 1.0f * direction;
        }
        Animation transAni = new TranslateAnimation(1, fromX, 1, toX, 1, fromY, 1, toY);
        AnimationSet aniSet = new AnimationSet(true);
        aniSet.addAnimation(transAni);
        aniSet.setDuration(300);
        aniSet.setInterpolator(new DecelerateInterpolator(sSnapShotAnimationFactor));
        aniSet.setAnimationListener(listener);
        aniView.startAnimation(aniSet);
    }

    public static void startSnapshotDeleteOrUndoAnimation(View aniView, Context c, int degree, boolean isFirstView, float direction, AnimationListener listener) {
        if (aniView != null) {
            if (Utils.isRTLLanguage()) {
                direction *= -1.0f;
            }
            aniView.clearAnimation();
            float fromX = 0.0f;
            float toX = 0.0f;
            float fromY = 0.0f;
            float toY = 0.0f;
            if (isFirstView) {
                if (degree == 0) {
                    fromX = 0.0f * direction;
                    toX = -1.0f * direction;
                } else if (degree == 90) {
                    fromY = 0.0f * direction;
                    toY = 1.0f * direction;
                } else if (degree == 180) {
                    fromX = 0.0f * direction;
                    toX = 1.0f * direction;
                } else if (degree == 270) {
                    fromY = 0.0f * direction;
                    toY = -1.0f * direction;
                }
            } else if (degree == 0) {
                fromX = 1.0f * direction;
                toX = 0.0f * direction;
            } else if (degree == 90) {
                fromY = -1.0f * direction;
                toY = 0.0f * direction;
            } else if (degree == 180) {
                fromX = -1.0f * direction;
                toX = 0.0f * direction;
            } else if (degree == 270) {
                fromY = 1.0f * direction;
                toY = 0.0f * direction;
            }
            Animation transAni = new TranslateAnimation(1, fromX, 1, toX, 1, fromY, 1, toY);
            AnimationSet aniSet = new AnimationSet(true);
            aniSet.addAnimation(transAni);
            aniSet.setDuration(400);
            aniSet.setInterpolator(new DecelerateInterpolator());
            aniSet.setAnimationListener(listener);
            aniView.startAnimation(aniSet);
        }
    }

    public static void startCameraRollDeleteOrUndoAnimation(View aniView, Context c, int degree, boolean isFirstView, float direction, AnimationListener listener) {
        if (aniView != null) {
            aniView.clearAnimation();
            float fromX = 0.0f;
            float toX = 0.0f;
            float fromY = 0.0f;
            float toY = 0.0f;
            if (isFirstView) {
                if (degree == 0) {
                    fromX = 0.0f * direction;
                    toX = -1.0f * direction;
                } else if (degree == 90) {
                    fromY = 0.0f * direction;
                    toY = 2.0f * direction;
                } else if (degree == 180) {
                    fromX = 0.0f * direction;
                    toX = 1.0f * direction;
                } else if (degree == 270) {
                    fromY = 0.0f * direction;
                    toY = -2.0f * direction;
                }
            } else if (degree == 0) {
                fromX = 1.0f * direction;
                toX = 0.0f * direction;
            } else if (degree == 90) {
                fromY = -2.0f * direction;
                toY = 0.0f * direction;
            } else if (degree == 180) {
                fromX = -1.0f * direction;
                toX = 0.0f * direction;
            } else if (degree == 270) {
                fromY = 2.0f * direction;
                toY = 0.0f * direction;
            }
            Animation transAni = new TranslateAnimation(1, fromX, 1, toX, 1, fromY, 1, toY);
            AnimationSet aniSet = new AnimationSet(true);
            aniSet.addAnimation(transAni);
            aniSet.setDuration(400);
            aniSet.setInterpolator(new DecelerateInterpolator());
            aniSet.setAnimationListener(listener);
            aniView.startAnimation(aniSet);
        }
    }

    public static void startTimerCountDownAnimation(View aniView, float scale, int distance, long duration, AnimationListener listener) {
        if (aniView != null) {
            aniView.clearAnimation();
            Animation scaleAni = new ScaleAnimation(scale, 1.0f, scale, 1.0f, 1, 0.5f, 1, 0.5f);
            Animation transAni = new TranslateAnimation(0, (float) distance, 1, 0.0f, 1, 0.0f, 1, 0.0f);
            Animation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaAnimation = new AnimationSet(true);
            alphaAnimation.addAnimation(scaleAni);
            alphaAnimation.addAnimation(transAni);
            alphaAnimation.addAnimation(alphaAnimation);
            alphaAnimation.setDuration(duration);
            alphaAnimation.setInterpolator(new DecelerateInterpolator());
            alphaAnimation.setAnimationListener(listener);
            aniView.startAnimation(alphaAnimation);
        }
    }

    public static void startCinemaInitGuideAnim(View aniView, boolean isLand, AnimationListener listener) {
        if (aniView != null) {
            aniView.clearAnimation();
            Animation scaleAni = new ScaleAnimation(1.0f, 1.706f, 1.0f, 1.706f, 2, 0.85f, 2, 0.8f);
            scaleAni.setRepeatCount(-1);
            scaleAni.setRepeatMode(2);
            scaleAni.setAnimationListener(listener);
            AnimationSet aniSet = new AnimationSet(true);
            aniSet.addAnimation(scaleAni);
            aniSet.setDuration(CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
            aniView.startAnimation(aniSet);
        }
    }

    public static void startPreviewCoverAlphaAnimation(View aniView, boolean show, long duration, AnimationListener listener) {
        float end = 1.0f;
        if (aniView != null) {
            float start;
            aniView.clearAnimation();
            aniView.setVisibility(show ? 0 : 4);
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

    public static void startPreviewCoverScaleAnimation(ImageView coverView, float scale, long animDuration, int previewPivotY, int videoPivotY, AnimatorListener listener) {
        AnimatorSet scaleSet = new AnimatorSet();
        r2 = new Animator[3];
        r2[0] = ObjectAnimator.ofFloat(coverView, "translationY", new float[]{(float) (videoPivotY - previewPivotY)});
        r2[1] = ObjectAnimator.ofFloat(coverView, "scaleX", new float[]{1.0f, scale});
        r2[2] = ObjectAnimator.ofFloat(coverView, "scaleY", new float[]{1.0f, scale});
        scaleSet.playTogether(r2);
        scaleSet.setInterpolator(new DecelerateInterpolator());
        scaleSet.setDuration(animDuration);
        if (listener != null) {
            scaleSet.addListener(listener);
        }
        scaleSet.start();
    }

    public static void startDetailViewShowAnimation(View view, long duration, AnimationListener listener) {
        if (view != null) {
            view.clearAnimation();
            Animation scaleAni = new ScaleAnimation(0.6f, 1.0f, 0.6f, 1.0f, 1, 0.5f, 1, 0.5f);
            Animation alphaAni = new AlphaAnimation(0.0f, 1.0f);
            AnimationSet aniSet = new AnimationSet(true);
            aniSet.addAnimation(scaleAni);
            aniSet.addAnimation(alphaAni);
            aniSet.setDuration(duration);
            aniSet.setInterpolator(new OvershootInterpolator(1.5f));
            aniSet.setAnimationListener(listener);
            view.startAnimation(aniSet);
        }
    }

    public static void startDetailViewHideAnimation(View view, long duration, AnimationListener listener) {
        if (view != null) {
            view.clearAnimation();
            Animation scaleAni = new ScaleAnimation(1.0f, 0.6f, 1.0f, 0.6f, 1, 0.5f, 1, 0.5f);
            Animation alphaAni = new AlphaAnimation(1.0f, 0.0f);
            AnimationSet aniSet = new AnimationSet(true);
            aniSet.addAnimation(scaleAni);
            aniSet.addAnimation(alphaAni);
            aniSet.setDuration(duration);
            aniSet.setInterpolator(new DecelerateInterpolator());
            aniSet.setAnimationListener(listener);
            view.startAnimation(aniSet);
        }
    }

    public static void startGraphyButtonTransAnimation(Context context, View button, long duration, AnimationListener listener, boolean toDown) {
        if (button != null) {
            float toY;
            button.clearAnimation();
            if (toDown) {
                toY = (float) RatioCalcUtil.getSizeCalculatedByPercentage(context, true, 0.128f);
            } else {
                toY = (float) (RatioCalcUtil.getSizeCalculatedByPercentage(context, true, 0.128f) * -1);
            }
            Animation transAni = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, 0.0f, 0, toY);
            transAni.setDuration(duration);
            transAni.setInterpolator(new LinearInterpolator());
            transAni.setAnimationListener(listener);
            button.startAnimation(transAni);
        }
    }

    public static void startGraphyListAlphaAnimation(View view, boolean show, long duration, AnimationListener al) {
        float f = 1.0f;
        if (view != null) {
            float f2;
            if (show) {
                f2 = 0.0f;
            } else {
                f2 = 1.0f;
            }
            if (!show) {
                f = 0.0f;
            }
            Animation anim = new AlphaAnimation(f2, f);
            anim.setDuration(duration);
            anim.setAnimationListener(al);
            anim.setInterpolator(show ? new AccelerateInterpolator() : new DecelerateInterpolator());
            view.clearAnimation();
            view.startAnimation(anim);
        }
    }

    public static void startCineEffectAnimiation(View view) {
        if (view == null) {
            CamLog.m5e(CameraConstants.TAG, "view is null. return");
            return;
        }
        Animation alphaAni = new AlphaAnimation(1.0f, 0.0f);
        Animation scaleAni = new ScaleAnimation(1.0f, 20.0f, 1.0f, 20.0f, 1, 0.5f, 1, 0.5f);
        AnimationSet aniSet = new AnimationSet(true);
        aniSet.addAnimation(alphaAni);
        aniSet.addAnimation(scaleAni);
        aniSet.setDuration(550);
        aniSet.setInterpolator(new AccelerateInterpolator());
        aniSet.setFillAfter(true);
        view.startAnimation(aniSet);
    }

    public static void startSmartCamAnimation(View view, AnimationListener listener) {
        if (view == null || listener == null) {
            CamLog.m5e(CameraConstants.TAG, "view is null. return");
            return;
        }
        Animation alphaAni = new AlphaAnimation(1.0f, 0.0f);
        Animation scaleAni = new ScaleAnimation(1.0f, 20.0f, 1.0f, 20.0f, 1, 0.5f, 1, 0.5f);
        AnimationSet aniSet = new AnimationSet(true);
        aniSet.addAnimation(alphaAni);
        aniSet.addAnimation(scaleAni);
        aniSet.setDuration(550);
        aniSet.setInterpolator(new AccelerateInterpolator());
        aniSet.setFillAfter(true);
        aniSet.setAnimationListener(listener);
        view.startAnimation(aniSet);
    }

    public static void startSmartCamTagCloudAnimation(View aniView, long duration, AnimationListener listener) {
        if (aniView != null) {
            aniView.clearAnimation();
            Animation scaleAni = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f, 1, 0.5f, 1, 0.5f);
            Animation alphaAni = new AlphaAnimation(1.0f, 0.0f);
            AnimationSet aniSet = new AnimationSet(true);
            aniSet.addAnimation(scaleAni);
            aniSet.addAnimation(alphaAni);
            aniSet.setDuration(duration);
            aniSet.setInterpolator(new DecelerateInterpolator());
            aniSet.setAnimationListener(listener);
            aniView.startAnimation(aniSet);
        }
    }

    public static void startQRCodeAnimation(View aniView, boolean show, boolean hideToRight, AnimationListener listener) {
        if (aniView != null) {
            float fromX;
            float toX;
            aniView.clearAnimation();
            if (show) {
                fromX = 1.0f;
                toX = 0.0f;
            } else {
                fromX = 0.0f;
                toX = hideToRight ? 1.0f : -1.0f;
            }
            Animation transAni = new TranslateAnimation(1, fromX, 1, toX, 1, 0.0f, 1, 0.0f);
            Animation alphaAni = new AlphaAnimation(show ? 0.0f : 1.0f, show ? 1.0f : 0.0f);
            AnimationSet aniSet = new AnimationSet(true);
            aniSet.addAnimation(transAni);
            aniSet.addAnimation(alphaAni);
            aniSet.setDuration(show ? 750 : 400);
            aniSet.setInterpolator(show ? new DecelerateInterpolator(3.0f) : new AccelerateInterpolator(3.0f));
            aniSet.setAnimationListener(listener);
            aniView.startAnimation(aniSet);
        }
    }

    public static void startWideAngleAnimation(View aniView, AnimationListener listener) {
        if (aniView != null) {
            aniView.clearAnimation();
            Animation alphaAni = new AlphaAnimation(1.0f, 0.0f);
            alphaAni.setInterpolator(new LinearInterpolator());
            alphaAni.setRepeatCount(-1);
            alphaAni.setRepeatMode(2);
            alphaAni.setAnimationListener(listener);
            AnimationSet aniSet = new AnimationSet(true);
            aniSet.addAnimation(alphaAni);
            aniSet.setDuration(1000);
            aniView.startAnimation(aniSet);
        }
    }

    public static void startPanoPreviewAnimation(View aniView, boolean leftToRight, AnimationListener listener) {
        if (aniView != null) {
            float toXValue;
            aniView.clearAnimation();
            int[] location = new int[2];
            aniView.getLocationOnScreen(location);
            int margin = Utils.getPx(aniView.getContext(), C0088R.dimen.panorama_bar_side_margin);
            if (leftToRight) {
                toXValue = (float) (((aniView.getWidth() / 2) - location[0]) + margin);
            } else {
                toXValue = (float) ((location[0] - (aniView.getWidth() / 2)) - margin);
            }
            Animation transAni = new TranslateAnimation(1, 0.0f, 0, toXValue, 1, 0.0f, 1, 0.0f);
            transAni.setInterpolator(new DecelerateInterpolator());
            transAni.setAnimationListener(listener);
            AnimationSet aniSet = new AnimationSet(true);
            aniSet.addAnimation(transAni);
            aniSet.setDuration(200);
            aniView.startAnimation(aniSet);
        }
    }

    public static void startShowingBinningIconAnimation(View aniView, boolean show, long duration, AnimationListener listener, boolean showAgain) {
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
            anim.setRepeatMode(2);
            anim.setRepeatCount(-1);
            anim.setAnimationListener(listener);
            aniView.startAnimation(anim);
        }
    }
}
