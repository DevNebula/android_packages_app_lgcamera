package com.lge.camera.managers;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.support.p000v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.util.Utils;

public class AnimationManager extends ManagerInterfaceImpl {
    public static final String ANI_ALPHA = "alpha";
    public static final float FLASH_ALPHA_END = 0.0f;
    public static final float FLASH_ALPHA_START = 0.3f;
    public static final int FLASH_DURATION = 300;
    public static final int HOLD_DURATION = 2500;
    public static final int SHRINK_DURATION = 400;
    public static final int SLIDE_DURATION = 1100;
    private ObjectAnimator mAnimator = null;
    private AnimatorSet mAnimatorSet = null;
    private Bitmap mBitmap = null;
    private AnimatorSet mCaptureAnimator = null;
    private ImageView mSnapshotAniView = null;
    private ImageView mSwitchAniView = null;

    /* renamed from: com.lge.camera.managers.AnimationManager$4 */
    class C08174 implements AnimatorListener {
        C08174() {
        }

        public void onAnimationStart(Animator animation) {
            if (AnimationManager.this.mSwitchAniView != null) {
                AnimationManager.this.mSwitchAniView.setVisibility(0);
            }
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            if (AnimationManager.this.mAnimator != null) {
                AnimationManager.this.mAnimator.removeAllListeners();
                AnimationManager.this.mAnimator = null;
            }
            if (AnimationManager.this.mSwitchAniView != null) {
                AnimationManager.this.mSwitchAniView.setVisibility(8);
                AnimationManager.this.mSwitchAniView.setImageBitmap(null);
            }
        }

        public void onAnimationCancel(Animator animation) {
        }
    }

    private class DecodeTask extends AsyncTask<Void, Void, Bitmap> {
        private final byte[] mData;
        private int mDegree = 0;
        private boolean mMirror;

        public DecodeTask(byte[] data, int degree, boolean mirror) {
            this.mData = data;
            this.mDegree = degree;
            this.mMirror = mirror;
        }

        protected Bitmap doInBackground(Void... arg0) {
            Options option = new Options();
            option.inSampleSize = 4;
            Bitmap bitmap = BitmapFactory.decodeByteArray(this.mData, 0, this.mData.length, option);
            if (this.mDegree == 0 && !this.mMirror) {
                return bitmap;
            }
            Matrix matrix = new Matrix();
            matrix.preRotate((float) this.mDegree);
            if (this.mMirror) {
                matrix.setScale(-1.0f, 1.0f);
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        }

        protected void onPostExecute(Bitmap bitmap) {
            AnimationManager.this.mBitmap = bitmap;
        }
    }

    public interface OnAnimationEndListener {
        void onAnimationEnd();
    }

    public AnimationManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        this.mSwitchAniView = (ImageView) this.mGet.getActivity().findViewById(C0088R.id.preivew_anim_view);
        this.mSnapshotAniView = (ImageView) this.mGet.getActivity().findViewById(C0088R.id.preview_snapshot_anim_view);
    }

    public void setSwitchAniLayout(int width, int height, int startMargin) {
        setAnimationLayout(this.mSwitchAniView, width, height, startMargin);
    }

    public void setSnapshotAniLayout(int width, int height, int startMargin) {
        setAnimationLayout(this.mSnapshotAniView, width, height, startMargin);
    }

    private void setAnimationLayout(View view, int width, int height, int startMargin) {
        if (view != null) {
            LayoutParams params = Utils.getRelativeLayoutParams(this.mGet.getAppContext(), width, height);
            if (params != null) {
                if (startMargin != -1) {
                    if (Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
                        params.setMarginStart(startMargin);
                    } else {
                        params.topMargin = startMargin;
                    }
                }
                view.setLayoutParams(params);
            }
        }
    }

    public void readyForSwitchAniView(Bitmap bitmap) {
        if (this.mSwitchAniView != null) {
            this.mBitmap = bitmap;
            this.mSwitchAniView.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        }
    }

    public void startAlphaAnimator(final ImageView aniView, float[] value, long duration) {
        if (aniView != null) {
            if (this.mAnimator != null && this.mAnimator.isStarted()) {
                this.mAnimator.cancel();
            }
            this.mAnimator = ObjectAnimator.ofFloat(aniView, ANI_ALPHA, value);
            this.mAnimator.setDuration(duration);
            this.mAnimator.addListener(new AnimatorListener() {
                public void onAnimationStart(Animator animation) {
                    if (aniView != null) {
                        aniView.setVisibility(0);
                    }
                }

                public void onAnimationRepeat(Animator animation) {
                }

                public void onAnimationEnd(Animator animation) {
                    if (AnimationManager.this.mAnimator != null) {
                        AnimationManager.this.mAnimator.removeAllListeners();
                        AnimationManager.this.mAnimator = null;
                    }
                    if (aniView != null) {
                        aniView.setVisibility(8);
                        aniView.setImageBitmap(null);
                    }
                }

                public void onAnimationCancel(Animator animation) {
                }
            });
            this.mAnimator.start();
        }
    }

    public void startManualAnimator(final boolean isStart, final ImageView aniView) {
        if (aniView != null) {
            if (this.mAnimator != null && this.mAnimator.isStarted()) {
                this.mAnimator.cancel();
            }
            float value = isStart ? 0.3f : 0.0f;
            this.mAnimator = ObjectAnimator.ofFloat(aniView, ANI_ALPHA, new float[]{value});
            this.mAnimator.setDuration(100);
            this.mAnimator.addListener(new AnimatorListener() {
                public void onAnimationStart(Animator animation) {
                    if (aniView != null) {
                        aniView.setVisibility(0);
                    }
                }

                public void onAnimationRepeat(Animator animation) {
                }

                public void onAnimationEnd(Animator animation) {
                    if (AnimationManager.this.mAnimator != null) {
                        AnimationManager.this.mAnimator.removeAllListeners();
                        AnimationManager.this.mAnimator = null;
                    }
                    if (!isStart) {
                        AnimationManager.this.removeManualAnimationView();
                    }
                }

                public void onAnimationCancel(Animator animation) {
                }
            });
            this.mAnimator.start();
        }
    }

    public void removeManualAnimationView() {
        if (this.mSnapshotAniView != null) {
            this.mSnapshotAniView.setVisibility(8);
            this.mSnapshotAniView.setImageBitmap(null);
        }
    }

    public void startSwitchFadeIn(View aniview, final OnAnimationEndListener listener, long wait, long duration) {
        if (aniview != null) {
            if (this.mAnimatorSet != null && this.mAnimatorSet.isStarted()) {
                this.mAnimatorSet.cancel();
            }
            this.mAnimatorSet = new AnimatorSet();
            ObjectAnimator.ofFloat(aniview, ANI_ALPHA, new float[]{0.0f, 0.0f}).setDuration(wait);
            ObjectAnimator animator2 = ObjectAnimator.ofFloat(aniview, ANI_ALPHA, new float[]{0.0f, 1.0f});
            animator2.setStartDelay(wait);
            animator2.setDuration(duration);
            this.mAnimatorSet.playTogether(new Animator[]{animator1, animator2});
            this.mAnimatorSet.addListener(new AnimatorListener() {
                public void onAnimationStart(Animator animation) {
                }

                public void onAnimationRepeat(Animator animation) {
                }

                public void onAnimationEnd(Animator animation) {
                    if (AnimationManager.this.mAnimatorSet != null) {
                        AnimationManager.this.mAnimatorSet.removeAllListeners();
                        AnimationManager.this.mAnimatorSet = null;
                    }
                    if (listener != null) {
                        listener.onAnimationEnd();
                    }
                }

                public void onAnimationCancel(Animator animation) {
                }
            });
            this.mAnimatorSet.start();
        }
    }

    public void startSwitchFadeOut(long duration) {
        if (this.mSwitchAniView != null && this.mBitmap != null) {
            if (this.mAnimator != null && this.mAnimator.isStarted()) {
                this.mAnimator.cancel();
            }
            this.mSwitchAniView.setVisibility(4);
            this.mSwitchAniView.setImageBitmap(this.mBitmap);
            this.mAnimator = ObjectAnimator.ofFloat(this.mSwitchAniView, ANI_ALPHA, new float[]{1.0f, 0.0f});
            this.mAnimator.setDuration(duration);
            this.mAnimator.addListener(new C08174());
            this.mAnimator.start();
        }
    }

    public void startSnapShotEffect(float fromAlpha, long duration) {
        if (this.mSnapshotAniView != null) {
            this.mSnapshotAniView.setImageBitmap(null);
            this.mSnapshotAniView.setBackgroundColor(-1);
            startAlphaAnimator(this.mSnapshotAniView, new float[]{fromAlpha, 0.0f}, duration);
        }
    }

    public void startManualSnapShotEffect() {
        if (this.mSnapshotAniView != null) {
            this.mSnapshotAniView.setImageBitmap(null);
            this.mSnapshotAniView.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
            startManualAnimator(true, this.mSnapshotAniView);
        }
    }

    public void stopManualSnapShotEffect() {
        startManualAnimator(false, this.mSnapshotAniView);
    }

    public void startCaptureAnimation(View view) {
        float scale;
        if (this.mCaptureAnimator != null && this.mCaptureAnimator.isStarted()) {
            this.mCaptureAnimator.cancel();
        }
        View parentView = (View) view.getParent();
        float slideDistance = (float) (parentView.getWidth() - view.getLeft());
        float scaleX = ((float) parentView.getWidth()) / ((float) view.getWidth());
        float scaleY = ((float) parentView.getHeight()) / ((float) view.getHeight());
        if (scaleX > scaleY) {
            scale = scaleX;
        } else {
            scale = scaleY;
        }
        int centerX = view.getLeft() + (view.getWidth() / 2);
        int centerY = view.getTop() + (view.getHeight() / 2);
        View view2 = view;
        ObjectAnimator slide = ObjectAnimator.ofFloat(view2, "translationX", new float[]{0.0f, slideDistance}).setDuration(1100);
        slide.setStartDelay(2900);
        view2 = view;
        ObjectAnimator translateY = ObjectAnimator.ofFloat(view2, "translationY", new float[]{(float) ((parentView.getHeight() / 2) - centerY), 0.0f}).setDuration(400);
        final View view3 = view;
        translateY.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                view3.setClickable(true);
            }

            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }
        });
        this.mCaptureAnimator = new AnimatorSet();
        AnimatorSet animatorSet = this.mCaptureAnimator;
        r12 = new Animator[5];
        view2 = view;
        r12[0] = ObjectAnimator.ofFloat(view2, "scaleX", new float[]{scale, 1.0f}).setDuration(400);
        view2 = view;
        r12[1] = ObjectAnimator.ofFloat(view2, "scaleY", new float[]{scale, 1.0f}).setDuration(400);
        view2 = view;
        r12[2] = ObjectAnimator.ofFloat(view2, "translationX", new float[]{(float) ((parentView.getWidth() / 2) - centerX), 0.0f}).setDuration(400);
        r12[3] = translateY;
        r12[4] = slide;
        animatorSet.playTogether(r12);
        view3 = view;
        this.mCaptureAnimator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animator) {
                view3.setClickable(false);
                view3.setVisibility(0);
            }

            public void onAnimationEnd(Animator animator) {
                view3.setScaleX(1.0f);
                view3.setScaleX(1.0f);
                view3.setTranslationX(0.0f);
                view3.setTranslationY(0.0f);
                view3.setVisibility(4);
                AnimationManager.this.mCaptureAnimator.removeAllListeners();
                AnimationManager.this.mCaptureAnimator = null;
            }

            public void onAnimationCancel(Animator animator) {
                view3.setVisibility(4);
            }

            public void onAnimationRepeat(Animator animator) {
            }
        });
        this.mCaptureAnimator.start();
    }

    public void cancelCaptureAnimations() {
        if (this.mCaptureAnimator != null && this.mCaptureAnimator.isStarted()) {
            this.mCaptureAnimator.cancel();
        }
    }

    public void decodeCapture(byte[] data, int degree, boolean mirror) {
        new DecodeTask(data, degree, mirror).execute(new Void[0]);
    }

    public void onPauseAfter() {
        removeManualAnimationView();
    }

    public void onDestroy() {
        this.mSwitchAniView = null;
        this.mSnapshotAniView = null;
        this.mAnimatorSet = null;
        this.mAnimator = null;
        this.mBitmap = null;
        this.mCaptureAnimator = null;
    }
}
