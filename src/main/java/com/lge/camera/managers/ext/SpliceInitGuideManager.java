package com.lge.camera.managers.ext;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.HelpUtils;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.List;

public class SpliceInitGuideManager extends SquareCameraInitGuideManagerBase {
    protected final float DUAL_TEXT_TOP_MARGIN = 0.0236f;
    protected final float DUAL_TIEM_IMAGE_TOP_MARGIN = 0.0569f;
    private int[] mGuideTextForPhotoIds = new int[]{C0088R.string.cell_square_dual_coach_guide_text1_1_only_photo, C0088R.string.cell_square_dual_coach_guide_text1_2, C0088R.string.cell_square_dual_coach_guide_text2_1_only_photo};
    private int[] mGuideTextIds = new int[]{C0088R.string.cell_square_dual_coach_guide_text1_1, C0088R.string.cell_square_dual_coach_guide_text1_2, C0088R.string.cell_square_dual_coach_guide_text2_1};
    private int[] mGuideTextViewIds = new int[]{C0088R.id.dual_guide_text1_1, C0088R.id.dual_guide_text1_2, C0088R.id.dual_guide_text2_1};
    private int[] mGuideTextViewIdsLand = new int[]{C0088R.id.dual_guide_text1_1_land, C0088R.id.dual_guide_text1_2_land, C0088R.id.dual_guide_text2_1_land};
    private List<SpannableItem> mSpannableArray;

    public static class SpannableItem {
        public int descId;
        public int drawableId;
        public String toBeReplace;

        public SpannableItem(int drawableId, String toBeReplace, int descId) {
            this.drawableId = drawableId;
            this.toBeReplace = toBeReplace;
            this.descId = descId;
        }
    }

    public SpliceInitGuideManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    protected View inflateGuideViewLayout() {
        return this.mGet.inflateView(C0088R.layout.square_dual_init_guide);
    }

    public void initLayout() {
        int i;
        TextView tv;
        super.initLayout();
        this.mSpannableArray = new ArrayList();
        this.mSpannableArray.add(new SpannableItem(C0088R.drawable.ic_camera_cell_coach_dual_sequential, "#01#", C0088R.string.multiview_frameshot_on_talkback));
        this.mSpannableArray.add(new SpannableItem(C0088R.drawable.ic_camera_cell_coach_dual_gallery_plus, "#02#", C0088R.string.add_images));
        this.mSpannableArray.add(new SpannableItem(C0088R.drawable.ic_camera_cell_coach_dual_same_time, "#03#", C0088R.string.multiview_frameshot_off_talkback));
        String button = this.mGet.getAppContext().getString(C0088R.string.button);
        int[] guideTextIds = FunctionProperties.isSupportedCollageRecording() ? this.mGuideTextIds : this.mGuideTextForPhotoIds;
        for (i = 0; i < this.mGuideTextViewIds.length; i++) {
            tv = (TextView) this.mPortGuideLayout.findViewById(this.mGuideTextViewIds[i]);
            if (tv != null) {
                tv.setText(HelpUtils.makeSpannableString(this.mGet.getAppContext(), this.mSpannableArray, this.mGet.getAppContext().getString(guideTextIds[i])));
                tv.setContentDescription(HelpUtils.getSpannableDescription(this.mGet.getAppContext(), this.mSpannableArray, this.mGet.getAppContext().getString(guideTextIds[i]), button));
            }
        }
        for (i = 0; i < this.mGuideTextViewIdsLand.length; i++) {
            tv = (TextView) this.mLandGuideLayout.findViewById(this.mGuideTextViewIdsLand[i]);
            if (tv != null) {
                tv.setText(HelpUtils.makeSpannableString(this.mGet.getAppContext(), this.mSpannableArray, this.mGet.getAppContext().getString(guideTextIds[i])));
                tv.setContentDescription(HelpUtils.getSpannableDescription(this.mGet.getAppContext(), this.mSpannableArray, this.mGet.getAppContext().getString(guideTextIds[i]), button));
            }
        }
    }

    protected void setupCheckBox() {
        super.setupCheckBox();
        Drawable drawable = getAppContext().getDrawable(C0088R.drawable.camera_cell_coach_image_dual_land_2);
        if (drawable != null) {
            this.mLandGuideLayout.findViewById(C0088R.id.grid_check_box_wrapper_land).setPaddingRelative((RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.132f) + drawable.getIntrinsicWidth()) + Utils.getPx(getAppContext(), C0088R.dimen.square_help_guide_startMargin.land), 0, 0, 0);
        }
    }

    protected void setInitGuideLayoutForScreenZoom() {
        LayoutParams rlp = (LayoutParams) this.mPortTitleText.getLayoutParams();
        rlp.height = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.094f);
        this.mPortTitleText.setLayoutParams(rlp);
        rlp = (LayoutParams) this.mLandTitleText.getLayoutParams();
        rlp.height = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.144f);
        this.mLandTitleText.setLayoutParams(rlp);
        rlp = (LayoutParams) this.mGridInitView.findViewById(C0088R.id.dual_items_wrapper_port).getLayoutParams();
        rlp.setMarginStart(RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.083f));
        rlp.setMarginEnd(RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.083f));
        View view = this.mGridInitView.findViewById(C0088R.id.dual_items_wrapper_land);
        rlp = (LayoutParams) view.getLayoutParams();
        rlp.setMarginStart(RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.132f));
        rlp.setMarginEnd(RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.132f));
        view.setLayoutParams(rlp);
        view = this.mGridInitView.findViewById(C0088R.id.dual_second_item_wrapper);
        rlp = (LayoutParams) view.getLayoutParams();
        rlp.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.0569f);
        view.setLayoutParams(rlp);
        int textTopMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.0236f);
        view = this.mGridInitView.findViewById(C0088R.id.dual_guide_text1_1);
        rlp = (LayoutParams) view.getLayoutParams();
        rlp.topMargin = textTopMargin;
        view.setLayoutParams(rlp);
        view = this.mGridInitView.findViewById(C0088R.id.dual_guide_text1_2);
        rlp = (LayoutParams) view.getLayoutParams();
        rlp.topMargin = textTopMargin;
        view.setLayoutParams(rlp);
        view = this.mGridInitView.findViewById(C0088R.id.dual_guide_text2_1_wrapper);
        rlp = (LayoutParams) view.getLayoutParams();
        rlp.topMargin = textTopMargin;
        view.setLayoutParams(rlp);
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        if (this.mSpannableArray != null) {
            this.mSpannableArray.clear();
            this.mSpannableArray = null;
        }
    }
}
