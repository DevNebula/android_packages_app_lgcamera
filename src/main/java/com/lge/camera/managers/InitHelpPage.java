package com.lge.camera.managers;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class InitHelpPage {
    public static final float INIT_DESC_TOP_MARGIN = 0.0347f;
    public static final float INIT_HELP_DESC_TOP_MARGIN = 0.0167f;
    public static final float INIT_HELP_IMAGE_START_MARGIN = 0.0208f;
    public static final float INIT_HELP_LAYOUT_PADDING = 0.083f;
    public static final float INIT_HELP_LAYOUT_PADDING_LAND = 0.069f;
    public static final float INIT_HELP_TITLE_HEIGHT_LAND = 0.2889f;
    public static final float INIT_HELP_TITLE_LAYOUT_HEIGHT = 0.1153f;
    public static final float INIT_HELP_TITLE_TOP_MARGIN = 0.0528f;
    protected int mDegree = 0;
    protected ModuleInterface mGet = null;
    protected int mImageId = -1;
    protected View mView = null;

    public InitHelpPage(ModuleInterface moduleInterface) {
        this.mGet = moduleInterface;
    }

    protected void initLayout(int degree) {
        if (this.mView != null && this.mImageId >= 0) {
            boolean isPortrait;
            if (degree == 0 || degree == 180) {
                isPortrait = true;
            } else {
                isPortrait = false;
            }
            int padding;
            if (isPortrait) {
                View view = this.mView.findViewById(C0088R.id.title_text_view_layout);
                LayoutParams rlp = (LayoutParams) view.getLayoutParams();
                rlp.height = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.1153f);
                view.setLayoutParams(rlp);
                view = this.mView.findViewById(C0088R.id.title_text_view);
                rlp = (LayoutParams) view.getLayoutParams();
                rlp.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0528f);
                view.setLayoutParams(rlp);
                ((LinearLayout.LayoutParams) this.mView.findViewById(C0088R.id.desc_text_view).getLayoutParams()).topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0347f);
                padding = (Utils.getLCDsize(this.mGet.getAppContext(), true)[1] - this.mGet.getAppContext().getDrawable(this.mImageId).getIntrinsicWidth()) / 2;
                this.mView.findViewById(C0088R.id.init_help_layout).setPadding(padding, 0, padding, 0);
                return;
            }
            ((LayoutParams) this.mView.findViewById(C0088R.id.title_text_view).getLayoutParams()).height = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.2889f);
            ((LayoutParams) this.mView.findViewById(C0088R.id.desc_text_view).getLayoutParams()).setMarginStart(RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0208f));
            padding = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.083f);
            this.mView.findViewById(C0088R.id.init_help_layout).setPadding(padding, 0, padding, 0);
        }
    }

    public void initUI(View v, int degree) {
        if (v != null) {
            this.mDegree = degree;
            this.mView = v;
            initLayout(this.mDegree);
        }
    }

    public void init() {
    }

    public void onRemove() {
        this.mView = null;
    }

    public void oneShotPreviewCallbackDone() {
    }

    public void onPause() {
    }
}
