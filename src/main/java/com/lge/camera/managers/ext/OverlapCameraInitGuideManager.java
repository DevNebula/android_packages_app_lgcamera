package com.lge.camera.managers.ext;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class OverlapCameraInitGuideManager extends SquareCameraInitGuideManagerBase {
    protected AnimationDrawable mFrameAniLand;
    protected AnimationDrawable mFrameAniPort;
    protected ImageView mImageLand = null;
    protected ImageView mImagePort = null;

    public OverlapCameraInitGuideManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    protected View inflateGuideViewLayout() {
        return this.mGet.inflateView(C0088R.layout.square_overlap_init_guide);
    }

    public void initLayout() {
        super.initLayout();
        setupLayout();
    }

    protected void setupLayout() {
        if (this.mGridInitView != null) {
            this.mImagePort = (ImageView) this.mGridInitView.findViewById(C0088R.id.overlap_init_guide_imageview_portrait);
            this.mImageLand = (ImageView) this.mGridInitView.findViewById(C0088R.id.overlap_init_guide_imageview_landscape);
            this.mImagePort.setBackgroundResource(C0088R.drawable.overlap_init_guide);
            this.mImageLand.setBackgroundResource(C0088R.drawable.overlap_init_guide_land);
            this.mFrameAniPort = (AnimationDrawable) this.mImagePort.getBackground();
            this.mFrameAniLand = (AnimationDrawable) this.mImageLand.getBackground();
            this.mFrameAniPort.start();
            this.mFrameAniLand.start();
        }
    }

    protected void setInitGuideLayoutForScreenZoom() {
        LayoutParams rlp = (LayoutParams) this.mPortTitleText.getLayoutParams();
        rlp.height = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.094f);
        this.mPortTitleText.setLayoutParams(rlp);
        rlp = (LayoutParams) this.mLandTitleText.getLayoutParams();
        rlp.height = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.236f);
        this.mLandTitleText.setLayoutParams(rlp);
        View view = this.mGridInitView.findViewById(C0088R.id.overlap_item_wrapper_land);
        rlp = (LayoutParams) view.getLayoutParams();
        rlp.setMargins(RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.167f), 0, RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.132f), 0);
        view.setLayoutParams(rlp);
    }

    protected void setupCheckBox() {
        super.setupCheckBox();
        Drawable drawable = getAppContext().getDrawable(C0088R.drawable.camera_cell_coach_image_guide_land_1);
        if (drawable != null) {
            this.mLandGuideLayout.findViewById(C0088R.id.square_mode_check_box_land_wrapper).setPaddingRelative((RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.167f) + drawable.getIntrinsicWidth()) + Utils.getPx(getAppContext(), C0088R.dimen.square_help_guide_startMargin.land), 0, 0, 0);
        }
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        if (this.mFrameAniPort != null) {
            this.mFrameAniPort.start();
        }
        if (this.mFrameAniLand != null) {
            this.mFrameAniLand.start();
        }
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        if (this.mFrameAniPort != null) {
            this.mFrameAniPort.stop();
        }
        if (this.mFrameAniLand != null) {
            this.mFrameAniLand.stop();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mFrameAniPort != null) {
            this.mFrameAniPort = null;
        }
        if (this.mFrameAniLand != null) {
            this.mFrameAniLand = null;
        }
    }
}
