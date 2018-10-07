package com.lge.camera.managers;

import android.graphics.ColorFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class ColorEffectFront extends ColorEffectManager {
    private final float LISTVIEW_BOTTOM_MARGIN = 0.2f;
    private RotateImageButton mColorEffectButton = null;
    private int mListOffset = 0;
    private ListView mListView = null;

    /* renamed from: com.lge.camera.managers.ColorEffectFront$1 */
    class C08711 implements OnClickListener {
        C08711() {
        }

        public void onClick(View v) {
            if (ColorEffectFront.this.isMenuVisible()) {
                ColorEffectFront.this.onColorButtonUnselected();
            } else {
                ColorEffectFront.this.onColorButtonSelected();
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ColorEffectFront$2 */
    class C08722 implements AnimationListener {
        C08722() {
        }

        public void onAnimationStart(Animation arg0) {
        }

        public void onAnimationRepeat(Animation arg0) {
        }

        public void onAnimationEnd(Animation arg0) {
            ColorEffectFront.this.setListView();
            AnimationUtil.startAlphaAnimation(ColorEffectFront.this.mListView, 0.0f, 1.0f, 200, null);
        }
    }

    public ColorEffectFront(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        this.mColorEffectButton = (RotateImageButton) this.mGet.findViewById(C0088R.id.beauty_color_effect_button);
        if (this.mColorEffectButton != null) {
            this.mColorEffectButton.initButtonText(this.mGet.getActivity().getResources().getString(C0088R.string.Advanced_beauty_filter));
            setButtonImage();
            this.mColorEffectButton.setVisibility(0);
            this.mColorEffectButton.setOnClickListener(new C08711());
        }
        this.mListOffset = (Utils.getLCDsize(this.mGet.getAppContext(), true)[1] - this.mGet.getAppContext().getDrawable(C0088R.drawable.camera_setting_filter_front_mid_normal).getIntrinsicWidth()) / 2;
    }

    private void onColorButtonUnselected() {
        hideMenu(true);
        this.mGet.onHideBeautyMenu();
    }

    private void onColorButtonSelected() {
        this.mGet.pauseShutterless();
        this.mColorInterface.setMenuType(4);
        this.mColorInterface.setBeautyButtonSelected(1, false);
        this.mColorInterface.setBeautyButtonSelected(3, false);
        this.mGet.setBarVisible(1, false, false);
        this.mGet.onShowBeautyMenu(4);
        showMenu(true);
    }

    protected void show() {
        this.mMenuView = this.mGet.inflateView(C0088R.layout.color_effect_front_menu);
        if (this.mMenuView != null && !isMenuVisible() && this.mGet != null && !this.mGet.isPaused()) {
            setButtonSelected(true);
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            if (vg != null) {
                vg.addView(this.mMenuView);
            }
            setListView();
            LayoutParams params = (LayoutParams) this.mMenuView.getLayoutParams();
            int padding = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.color_effect_list_view_spacing);
            params.height = this.mColorAdapter.getImageSize()[1] + (padding * 2);
            params.addRule(12);
            params.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.2f);
            this.mMenuView.setPadding(padding, padding, padding, padding);
            this.mMenuView.setLayoutParams(params);
            ((RotateLayout) this.mMenuView.findViewById(C0088R.id.color_effect_listview_rotate)).setAngle(90);
            CamLog.m3d(CameraConstants.TAG, "[color] show menu");
            this.mMenuView.setVisibility(0);
        }
    }

    private void setListView() {
        if (this.mMenuView != null) {
            this.mListView = (ListView) this.mMenuView.findViewById(C0088R.id.color_effect_listview);
            if (this.mListView != null) {
                if (this.mColorAdapter == null) {
                    this.mColorAdapter = new ColorEffectFrontAdapter(this.mGet.getAppContext(), this.mColorList, this.mDegree);
                }
                this.mColorAdapter.setDegree(this.mDegree);
                this.mListView.setAdapter(this.mColorAdapter);
                this.mListView.setOnItemClickListener(this.mColorItemClickListener);
                this.mListView.setSelectionFromTop(this.mSelectedPosition, this.mListOffset);
            }
        }
    }

    protected void hide() {
        if (this.mMenuView != null && isMenuVisible()) {
            setButtonImage();
            setButtonSelected(false);
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            CamLog.m3d(CameraConstants.TAG, "[color] hide menu");
            this.mMenuView.setVisibility(8);
            if (vg != null) {
                vg.removeView(this.mMenuView);
            }
            this.mMenuView = null;
        }
    }

    public void hideMenu(boolean showAni) {
        if (showAni) {
            setButtonImage();
            setButtonSelected(false);
        }
        super.hideMenu(showAni);
    }

    protected void onColorItemClick(int position) {
        if (this.mSelectedPosition != position) {
            super.onColorItemClick(position);
            setButtonImage();
            if (this.mListView != null) {
                this.mListView.setSelectionFromTop(this.mSelectedPosition, this.mListOffset);
            }
        }
    }

    public void setRotateDegree(int degree, boolean animation) {
        super.setRotateDegree(degree, animation);
        if (this.mColorEffectButton != null) {
            this.mColorEffectButton.setDegree(degree, animation);
        }
        AnimationUtil.startAlphaAnimation(this.mListView, 1.0f, 0.0f, 200, new C08722());
    }

    public void onDestroy() {
        super.onDestroy();
        CamLog.m3d(CameraConstants.TAG, "[color] front - destroy");
        if (this.mListView != null) {
            this.mListView.setVisibility(8);
            this.mListView = null;
        }
    }

    private void setButtonImage() {
        if (this.mColorEffectButton != null) {
            this.mColorEffectButton.setImageResource(!"none".equals(this.mGet.getSettingValue(Setting.KEY_COLOR_EFFECT)) ? C0088R.drawable.btn_front_filter_on : C0088R.drawable.btn_front_filter_off);
        }
    }

    private void setButtonSelected(boolean selected) {
        if (this.mColorEffectButton != null) {
            this.mColorEffectButton.setSelected(selected);
        }
    }

    public void setButtonEnabled(boolean enabled) {
        if (this.mColorEffectButton != null) {
            ColorFilter color = enabled ? ColorUtil.getNormalColorByAlpha() : ColorUtil.getDimColorByAlpha();
            this.mColorEffectButton.setEnabled(enabled);
            this.mColorEffectButton.setColorFilter(color);
            this.mColorEffectButton.getBackground().setColorFilter(color);
        }
    }
}
