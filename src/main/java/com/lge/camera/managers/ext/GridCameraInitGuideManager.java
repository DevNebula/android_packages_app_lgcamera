package com.lge.camera.managers.ext;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class GridCameraInitGuideManager extends OverlapCameraInitGuideManager {
    public GridCameraInitGuideManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    protected View inflateGuideViewLayout() {
        return this.mGet.inflateView(C0088R.layout.square_grid_init_guide);
    }

    protected void setupLayout() {
        if (this.mGridInitView != null) {
            this.mImagePort = (ImageView) this.mGridInitView.findViewById(C0088R.id.square_grid_init_guide_portrait_img_view);
            this.mImageLand = (ImageView) this.mGridInitView.findViewById(C0088R.id.square_grid_init_guide_land_img_view);
            if (Utils.isRTLLanguage()) {
                this.mImagePort.setBackgroundResource(C0088R.drawable.grid_init_guide_anim_rtl);
                this.mImageLand.setBackgroundResource(C0088R.drawable.grid_init_guide_anim_land_rtl);
            } else {
                this.mImagePort.setBackgroundResource(C0088R.drawable.grid_init_guide_anim);
                this.mImageLand.setBackgroundResource(C0088R.drawable.grid_init_guide_anim_land);
            }
            this.mFrameAniPort = (AnimationDrawable) this.mImagePort.getBackground();
            this.mFrameAniLand = (AnimationDrawable) this.mImageLand.getBackground();
            this.mFrameAniPort.start();
            this.mFrameAniLand.start();
            int descId = FunctionProperties.isSupportedCollageRecording() ? C0088R.string.cell_square_grid_coach_guide_text : C0088R.string.cell_square_grid_coach_guide_text_only_photo;
            ((TextView) this.mGridInitView.findViewById(C0088R.id.square_grid_init_guide_portrait_description)).setText(this.mGet.getAppContext().getString(descId));
            ((TextView) this.mGridInitView.findViewById(C0088R.id.square_grid_init_guide_land_description)).setText(this.mGet.getAppContext().getString(descId));
        }
    }

    protected void setInitGuideLayoutForScreenZoom() {
        LayoutParams rlp = (LayoutParams) this.mPortTitleText.getLayoutParams();
        rlp.height = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.094f);
        this.mPortTitleText.setLayoutParams(rlp);
        rlp = (LayoutParams) this.mLandTitleText.getLayoutParams();
        rlp.height = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.236f);
        this.mLandTitleText.setLayoutParams(rlp);
        View view = this.mGridInitView.findViewById(C0088R.id.grid_item_wrapper_landscape);
        rlp = (LayoutParams) view.getLayoutParams();
        rlp.setMargins(RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.167f), 0, RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.132f), 0);
        view.setLayoutParams(rlp);
    }

    protected void setupCheckBox() {
        super.setupCheckBox();
        Drawable drawable = getAppContext().getDrawable(C0088R.drawable.camera_cell_coach_image_grid_land_1);
        if (drawable != null) {
            this.mLandGuideLayout.findViewById(C0088R.id.square_mode_check_box_land_wrapper).setPaddingRelative((RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.167f) + drawable.getIntrinsicWidth()) + Utils.getPx(getAppContext(), C0088R.dimen.square_help_guide_startMargin.land), 0, 0, 0);
        }
    }
}
