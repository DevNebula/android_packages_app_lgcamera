package com.lge.camera.managers;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.GridView;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class ColorEffectRear extends ColorEffectManager {
    private final int COLUMN_NUM = 3;
    private final float GRIDVIEW_HORIZONTAL_SPACING = 0.0146f;
    private GridView mGridView = null;

    /* renamed from: com.lge.camera.managers.ColorEffectRear$1 */
    class C08771 implements AnimationListener {
        C08771() {
        }

        public void onAnimationStart(Animation arg0) {
        }

        public void onAnimationRepeat(Animation arg0) {
        }

        public void onAnimationEnd(Animation arg0) {
            ColorEffectRear.this.setGridView();
            AnimationUtil.startAlphaAnimation(ColorEffectRear.this.mGridView, 0.0f, 1.0f, 200, null);
        }
    }

    public ColorEffectRear(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    private void setGridView() {
        if (this.mMenuView != null) {
            this.mGridView = (GridView) this.mMenuView.findViewById(C0088R.id.color_effect_gridview);
            if (this.mGridView != null) {
                if (this.mColorAdapter == null) {
                    this.mColorAdapter = new ColorEffectAdapter(this.mGet.getAppContext(), this.mColorList, this.mDegree);
                }
                this.mColorAdapter.setDegree(this.mDegree);
                this.mGridView.setColumnWidth(RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.311f));
                this.mGridView.setHorizontalSpacing(RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.0146f));
                this.mGridView.setNumColumns(3);
                this.mGridView.setGravity(17);
                this.mGridView.setFocusable(false);
                this.mGridView.setSelected(false);
                this.mGridView.setAdapter(this.mColorAdapter);
                this.mGridView.setOnItemClickListener(this.mColorItemClickListener);
            }
        }
    }

    protected void show() {
        this.mMenuView = this.mGet.inflateView(C0088R.layout.color_effect_rear_menu);
        if (this.mMenuView != null && !isMenuVisible() && this.mGet != null && !this.mGet.isPaused()) {
            this.mGet.inflateStub(C0088R.id.stub_color_effect_view);
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.color_effect_layout);
            if (vg != null) {
                vg.addView(this.mMenuView);
            }
            View view = this.mGet.findViewById(C0088R.id.color_effect_layout);
            if (view != null) {
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                if ("on".equals(this.mGet.getSettingValue(Setting.KEY_TILE_PREVIEW))) {
                    lp.topMargin = RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext());
                } else {
                    lp.topMargin = (int) Utils.dpToPx(this.mGet.getAppContext(), 0.0f);
                }
                view.setLayoutParams(lp);
            }
            setGridView();
            CamLog.m3d(CameraConstants.TAG, "[color] show menu");
            this.mMenuView.setVisibility(0);
        }
    }

    protected void hide() {
        if (this.mMenuView != null && isMenuVisible()) {
            this.mGet.setQuickButtonIndex(C0088R.id.quick_button_color_effect, 0);
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.color_effect_layout);
            CamLog.m3d(CameraConstants.TAG, "[color] hide menu");
            this.mMenuView.setVisibility(8);
            if (vg != null) {
                vg.removeView(this.mMenuView);
            }
            this.mMenuView = null;
        }
    }

    protected void onColorItemClick(int position) {
        if (isMenuVisible() && this.mGet != null) {
            Handler handler = this.mGet.getHandler();
            if (this.mSelectedPosition == position) {
                hideMenu(true);
                if (handler != null) {
                    handler.removeMessages(101);
                    handler.sendEmptyMessage(101);
                    return;
                }
                return;
            }
            super.onColorItemClick(position);
            hideMenu(true);
            if (handler != null) {
                handler.removeMessages(101);
                handler.sendEmptyMessage(101);
            }
            if (this.mColorInterface != null) {
                this.mColorInterface.updateColorEffectQuickButton();
            }
        }
    }

    public void setRotateDegree(int degree, boolean animation) {
        super.setRotateDegree(degree, animation);
        AnimationUtil.startAlphaAnimation(this.mGridView, 1.0f, 0.0f, 200, new C08771());
    }

    public void initializeAfterStartPreviewDone() {
        super.initializeAfterStartPreviewDone();
        this.mColorInterface.updateColorEffectQuickButton();
    }

    public void onDestroy() {
        CamLog.m3d(CameraConstants.TAG, "[color] rear - destroy");
        super.onDestroy();
        if (this.mGridView != null) {
            this.mGridView.setVisibility(8);
            this.mGridView = null;
        }
    }
}
