package com.lge.camera.managers.ext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.database.OverlapProjectDb;
import com.lge.camera.database.OverlapProjectDefaults;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SquareUtil;
import com.lge.camera.util.Utils;

public class OverlapPreviewManagerBase extends ManagerInterfaceImpl {
    private static final int ANIMATION_TIMER = 800;
    private static final int MAX_LEVEL_SEEKBAR = 245;
    private static final int MIN_LEVEL_SEEKBAR = 10;
    protected String mCurrProjectID;
    protected TextView mGuideText;
    private boolean mIsSeekbarAni = false;
    protected OverlapPreviewManagerInterface mListener;
    protected RelativeLayout mOverlapPreviewLayout;
    protected LinearLayout mOverlapSeekBarLayout;
    protected ImageView mOverlapView;
    protected RelativeLayout mOverlapViewLayout;
    protected RotateLayout mOverlapViewRotateLayout;
    private SeekBar mSeekBar;

    /* renamed from: com.lge.camera.managers.ext.OverlapPreviewManagerBase$2 */
    class C12412 implements OnSeekBarChangeListener {
        C12412() {
        }

        public void onStopTrackingTouch(SeekBar arg0) {
        }

        public void onStartTrackingTouch(SeekBar arg0) {
        }

        public void onProgressChanged(SeekBar bar, int level, boolean arg2) {
            if (OverlapPreviewManagerBase.this.mOverlapSeekBarLayout == null || OverlapPreviewManagerBase.this.mOverlapSeekBarLayout.getVisibility() != 0) {
                CamLog.m3d(CameraConstants.TAG, "mOverlapSeekBarLayout = " + OverlapPreviewManagerBase.this.mOverlapSeekBarLayout);
            } else {
                OverlapPreviewManagerBase.this.mOverlapView.setImageAlpha(level + 10);
            }
        }
    }

    public OverlapPreviewManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setPreviewManagerInterface(OverlapPreviewManagerInterface listener) {
        this.mListener = listener;
    }

    public void init() {
        super.init();
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
        this.mOverlapPreviewLayout = (RelativeLayout) this.mGet.inflateView(C0088R.layout.overlap_preview_layout);
        if (vg != null && this.mOverlapPreviewLayout != null) {
            View preview = this.mGet.findViewById(C0088R.id.preview_layout);
            int previewIndex = 0;
            if (preview != null) {
                previewIndex = ((RelativeLayout) preview.getParent()).indexOfChild(preview);
            }
            vg.addView(this.mOverlapPreviewLayout, previewIndex + 1);
            this.mOverlapViewRotateLayout = (RotateLayout) this.mOverlapPreviewLayout.findViewById(C0088R.id.overlap_preview_rotate_layout);
            LayoutParams rlp = (LayoutParams) this.mOverlapViewRotateLayout.getLayoutParams();
            if (ModelProperties.getLCDType() == 2) {
                rlp.topMargin = RatioCalcUtil.getQuickButtonWidth(getAppContext());
            } else {
                rlp.topMargin = 0;
            }
            this.mOverlapViewRotateLayout.setLayoutParams(rlp);
            this.mOverlapViewLayout = (RelativeLayout) this.mOverlapPreviewLayout.findViewById(C0088R.id.overlap_imageview_layout);
            this.mOverlapView = (ImageView) this.mGet.findViewById(C0088R.id.overlap_imageview);
            this.mOverlapView.setScaleType(ScaleType.FIT_XY);
            this.mOverlapView.setImageAlpha(0);
            this.mGuideText = (TextView) this.mGet.findViewById(C0088R.id.alpha_guide_string);
            this.mGuideText.setVisibility(0);
            this.mOverlapSeekBarLayout = (LinearLayout) this.mGet.findViewById(C0088R.id.overlap_seekbar_layout);
            this.mSeekBar = (SeekBar) this.mGet.findViewById(C0088R.id.overlap_seekbar);
            this.mSeekBar.setProgress(0);
            setSeekBarListener();
            this.mIsSeekbarAni = false;
            setRotateDegree(getOrientationDegree(), false);
        }
    }

    public void onPauseBefore() {
        super.onPauseBefore();
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        setRotateDegree(getOrientationDegree(), false);
        if (!this.mIsSeekbarAni) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {

                /* renamed from: com.lge.camera.managers.ext.OverlapPreviewManagerBase$1$1 */
                class C12381 implements AnimatorUpdateListener {
                    C12381() {
                    }

                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (OverlapPreviewManagerBase.this.mSeekBar != null) {
                            OverlapPreviewManagerBase.this.mSeekBar.setProgress(((Integer) animation.getAnimatedValue()).intValue());
                        }
                    }
                }

                /* renamed from: com.lge.camera.managers.ext.OverlapPreviewManagerBase$1$2 */
                class C12392 extends AnimatorListenerAdapter {
                    C12392() {
                    }

                    public void onAnimationEnd(Animator animation) {
                        OverlapPreviewManagerBase.this.mListener.onSeekBarAnimationEnd();
                    }
                }

                public void handleRun() {
                    ValueAnimator anim = ValueAnimator.ofInt(new int[]{0, 122});
                    anim.setDuration(800);
                    anim.addUpdateListener(new C12381());
                    anim.addListener(new C12392());
                    anim.start();
                }
            }, 0);
        }
        this.mIsSeekbarAni = true;
    }

    public void setSeekBarListener() {
        this.mSeekBar.setOnSeekBarChangeListener(new C12412());
    }

    public void onDestroy() {
        super.onDestroy();
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
        if (!(vg == null || this.mOverlapPreviewLayout == null)) {
            vg.removeView(this.mOverlapPreviewLayout);
        }
        this.mOverlapPreviewLayout = null;
        if (this.mSeekBar != null) {
            this.mSeekBar.setOnSeekBarChangeListener(null);
        }
        this.mSeekBar = null;
    }

    public void setRotateDegree(int degree, boolean animation) {
        super.setRotateDegree(degree, animation);
        this.mOverlapViewRotateLayout.rotateLayout(degree);
        LayoutParams overlapViewRlp = (LayoutParams) this.mOverlapViewLayout.getLayoutParams();
        int height = Utils.getLCDsize(getAppContext(), true)[1];
        if (degree == 0 || degree == 180) {
            overlapViewRlp.height = height;
            overlapViewRlp.width = -1;
        } else {
            overlapViewRlp.height = -1;
            overlapViewRlp.width = height;
        }
        this.mOverlapViewLayout.setLayoutParams(overlapViewRlp);
        LayoutParams seekbarLp = (LayoutParams) this.mOverlapSeekBarLayout.getLayoutParams();
        int minus_width = (degree == 90 || degree == 270) ? Utils.getPx(getAppContext(), C0088R.dimen.overlap_seekbar_gap) : Utils.getPx(getAppContext(), C0088R.dimen.overlap_seekbar_gap_horizontal);
        int plus_margin = 0;
        if (degree == 0) {
            plus_margin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.0222f);
        } else if (degree == 90 || degree == 180) {
            plus_margin = Utils.getPx(getAppContext(), C0088R.dimen.setting_list_item_height);
        }
        seekbarLp.width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.75f) - minus_width;
        seekbarLp.bottomMargin = Utils.getPx(getAppContext(), C0088R.dimen.overlap_seekbar_bottom_margin) + plus_margin;
        if (this.mListener.isSelfie() && degree == 0) {
            seekbarLp.bottomMargin = this.mGet.getAppContext().getDrawable(C0088R.drawable.ic_camera_selfie_tone_normal).getIntrinsicHeight();
        }
        this.mOverlapSeekBarLayout.setLayoutParams(seekbarLp);
        LayoutParams textLp = (LayoutParams) this.mGuideText.getLayoutParams();
        textLp.width = seekbarLp.width;
        this.mGuideText.setLayoutParams(textLp);
    }

    public void setVisible(boolean show) {
        if (this.mOverlapPreviewLayout != null && this.mSeekBar != null) {
            if (!show || this.mListener.getOverlapCaptureMode() != 1) {
                if (show) {
                    this.mOverlapPreviewLayout.setVisibility(0);
                } else {
                    this.mOverlapPreviewLayout.setVisibility(8);
                }
                this.mSeekBar.setEnabled(show);
            }
        }
    }

    public void setGuideSeekbarVisible(boolean show) {
        if (this.mOverlapSeekBarLayout != null && this.mSeekBar != null) {
            if (show) {
                this.mOverlapSeekBarLayout.setVisibility(0);
            } else {
                this.mOverlapSeekBarLayout.setVisibility(8);
            }
            this.mSeekBar.setEnabled(show);
        }
    }

    public void setGuideTextVisible(boolean show) {
        if (this.mGuideText != null) {
            if (show) {
                this.mGuideText.setVisibility(0);
            } else {
                this.mGuideText.setVisibility(8);
            }
        }
    }

    public void setProjectView(OverlapProjectDb currProject, OverlapProjectAdapter projectAdapter) {
        if (currProject == null) {
            this.mCurrProjectID = null;
            return;
        }
        int preset = currProject.getPreset();
        String imagePath = currProject.getSamplePath();
        if (preset != -1) {
            projectAdapter.loadBitmap(String.valueOf(OverlapProjectDefaults.getDefaultSample(preset)), this.mOverlapView);
        } else {
            projectAdapter.loadBitmap(SquareUtil.getSampleFilesDir(getAppContext()) + imagePath, this.mOverlapView);
        }
        this.mOverlapView.setScaleType(ScaleType.FIT_XY);
        this.mCurrProjectID = currProject.getProjectId();
    }

    public String getCurrProjectID() {
        return this.mCurrProjectID;
    }
}
