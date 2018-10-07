package com.lge.camera.managers.ext;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.TalkBackUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.List;

public class PopoutCameraManager extends ManagerInterfaceImpl {
    private SparseArray<RotateImageButton> mArrayLayoutSelector = null;
    private RotateImageButton mBtnBlur = null;
    private RotateImageButton mBtnGrayScale = null;
    private View mBtnLayout = null;
    private List<RotateImageButton> mBtnList = null;
    private RotateImageButton mBtnPerspective = null;
    private RotateImageButton mBtnVignetting = null;
    protected View mChildView = null;
    private int mCurFrameValue = 0;
    private RelativeLayout mFrameButtonLayout = null;
    private View mFrameLayout = null;
    private RotateImageButton mHeartFarmeButton = null;
    private RotateImageButton mInstantPicButton = null;
    protected boolean mIsFrameOpen = false;
    private onPopoutBtnListener mListener;
    protected ImageView mParentArrow = null;
    protected RotateImageButton mParentButton = null;
    ArrayList<PopoutFrameChildItem> mPopoutFrameItemList = null;

    public interface onPopoutBtnListener {
        boolean onPopoutEffectBtnClick(int i);

        void onPopoutFrameBtnClick(int i);
    }

    /* renamed from: com.lge.camera.managers.ext.PopoutCameraManager$1 */
    class C12471 implements OnClickListener {
        C12471() {
        }

        public void onClick(View v) {
            PopoutCameraManager.this.onEffectBtnOnClick(v);
        }
    }

    /* renamed from: com.lge.camera.managers.ext.PopoutCameraManager$2 */
    class C12482 implements OnTouchListener {
        C12482() {
        }

        public boolean onTouch(View arg0, MotionEvent arg1) {
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.PopoutCameraManager$3 */
    class C12493 implements OnClickListener {
        C12493() {
        }

        public void onClick(View v) {
            PopoutCameraManager.this.toggleChildMenu();
        }
    }

    private class OnLayoutSelectorClickListener implements OnClickListener {
        private int mValue = 0;

        public OnLayoutSelectorClickListener(int value) {
            this.mValue = value;
        }

        public void onClick(View view) {
            if (this.mValue == PopoutCameraManager.this.mCurFrameValue) {
                PopoutCameraManager.this.setFrameSelection(this.mValue);
                PopoutCameraManager.this.setVisibilityFrameChildMenu(false, true);
                return;
            }
            PopoutCameraManager.this.mCurFrameValue = this.mValue;
            PopoutCameraManager.this.mListener.onPopoutFrameBtnClick(this.mValue);
            PopoutCameraManager.this.setFrameSelection(this.mValue);
            PopoutCameraManager.this.setVisibilityFrameChildMenu(false, true);
            PopoutCameraManager.this.setPopoutFrameLayoutButtonDescription();
        }
    }

    public class PopoutFrameChildItem {
        public int mBackgroundSrc;
        public int mContentDescription;
        public int mId;
        public int mImageSrc;

        public PopoutFrameChildItem(int id, int contentDescription, int imageSrc, int backgroundSrc) {
            this.mId = id;
            this.mContentDescription = contentDescription;
            this.mImageSrc = imageSrc;
            this.mBackgroundSrc = backgroundSrc;
        }
    }

    public void setOnPopoutBtnListener(onPopoutBtnListener listener) {
        this.mListener = listener;
    }

    public PopoutCameraManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
        if (ModelProperties.isMTKChipset()) {
            this.mCurFrameValue = 1;
        } else {
            this.mCurFrameValue = 0;
        }
    }

    public void initPopoutCameraLayout(int popoutEffect) {
        initEffectBtnLayout(popoutEffect);
        initFrameLayout();
        addFrameButton();
        setFrameSelection(this.mCurFrameValue);
    }

    private void initFrameLayout() {
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
        this.mFrameLayout = this.mGet.inflateView(C0088R.layout.popout_frame_selector);
        this.mFrameButtonLayout = (RelativeLayout) this.mFrameLayout.findViewById(C0088R.id.popout_frame_parent_item_view);
        if (vg != null && this.mFrameLayout != null && this.mFrameButtonLayout != null) {
            changeFrameLayoutMargin();
            makeFrameChildItem();
            LinearLayout mFrameChildView = (LinearLayout) this.mFrameLayout.findViewById(C0088R.id.popout_frame_child_view);
            if (mFrameChildView != null) {
                int quickBtnWidth = getAppContext().getResources().getDrawable(C0088R.drawable.btn_quickbutton_setting_expand_button, null).getIntrinsicWidth();
                int qflSpace = (((Utils.getLCDsize(getAppContext(), true)[1] - (Utils.getPx(getAppContext(), C0088R.dimen.quick_button_margin) * 2)) - quickBtnWidth) / 4) - quickBtnWidth;
                int space = (((qflSpace + quickBtnWidth) * 3) / this.mPopoutFrameItemList.size()) - getAppContext().getResources().getDrawable(C0088R.drawable.setting_popout_frame_circle, null).getIntrinsicWidth();
                this.mParentArrow = (ImageView) this.mFrameLayout.findViewById(C0088R.id.popout_frame_parent_item_arrow);
                this.mParentArrow.getLayoutParams().width = space;
                LayoutParams params = new LayoutParams(-2, -2);
                params.setMarginStart(space);
                for (int i = 0; i < this.mPopoutFrameItemList.size(); i++) {
                    PopoutFrameChildItem frameItem = (PopoutFrameChildItem) this.mPopoutFrameItemList.get(i);
                    RotateImageButton frameButton = new RotateImageButton(getAppContext());
                    frameButton.setId(frameItem.mId);
                    frameButton.setImageResource(frameItem.mImageSrc);
                    if (frameItem.mBackgroundSrc == -1) {
                        frameButton.setBackgroundColor(Color.parseColor("#00000000"));
                        frameButton.setRotateIconOnly(false);
                        frameButton.setDegree(0, false);
                    } else {
                        frameButton.setBackgroundResource(frameItem.mBackgroundSrc);
                        frameButton.setRotateIconOnly(true);
                        frameButton.setDegree(getOrientationDegree() + 90, false);
                    }
                    frameButton.setContentDescription(getAppContext().getString(frameItem.mContentDescription));
                    frameButton.setVisibility(4);
                    mFrameChildView.addView(frameButton, i, params);
                }
                View quickButtonView = this.mGet.findViewById(C0088R.id.quick_button_layout_root);
                int index = 0;
                if (quickButtonView != null) {
                    index = vg.indexOfChild(quickButtonView) + 1;
                }
                vg.addView(this.mFrameLayout, index, new RelativeLayout.LayoutParams(-1, -1));
                this.mFrameLayout.setVisibility(4);
            }
        }
    }

    private void makeFrameChildItem() {
        PopoutFrameChildItem rect = new PopoutFrameChildItem(C0088R.id.popout_frame_rect, C0088R.string.popout_frame_rectangle, C0088R.drawable.setting_popout_frame_rect, -1);
        PopoutFrameChildItem circle = new PopoutFrameChildItem(C0088R.id.popout_frame_circle, C0088R.string.popout_frame_circle, C0088R.drawable.setting_popout_frame_circle, -1);
        PopoutFrameChildItem hexagon = new PopoutFrameChildItem(C0088R.id.popout_frame_hexagon, C0088R.string.popout_frame_hexagon, C0088R.drawable.setting_popout_frame_hexagon, -1);
        PopoutFrameChildItem heart = new PopoutFrameChildItem(C0088R.id.popout_frame_heart, C0088R.string.popout_frame_heart, C0088R.drawable.setting_popout_frame_heart_only, C0088R.drawable.setting_popout_frame_base);
        PopoutFrameChildItem instantPic = new PopoutFrameChildItem(C0088R.id.popout_frame_instantpic, C0088R.string.popout_frame_instantpic, C0088R.drawable.setting_popout_frame_instantpic_only, C0088R.drawable.setting_popout_frame_base);
        PopoutFrameChildItem film = new PopoutFrameChildItem(C0088R.id.popout_frame_film, C0088R.string.popout_frame_film, C0088R.drawable.setting_popout_frame_film, -1);
        PopoutFrameChildItem horizontal = new PopoutFrameChildItem(C0088R.id.popout_frame_division_horizontal, C0088R.string.popout_frame_horizontal_division, C0088R.drawable.setting_popout_frame_division_horizontal, -1);
        PopoutFrameChildItem vertical = new PopoutFrameChildItem(C0088R.id.popout_frame_division_vertical, C0088R.string.popout_frame_vertical_division, C0088R.drawable.setting_popout_frame_division_vertical, -1);
        this.mPopoutFrameItemList = new ArrayList();
        if (ModelProperties.isMTKChipset()) {
            this.mPopoutFrameItemList.add(rect);
            this.mPopoutFrameItemList.add(horizontal);
            this.mPopoutFrameItemList.add(film);
            this.mPopoutFrameItemList.add(instantPic);
            this.mPopoutFrameItemList.add(heart);
            this.mPopoutFrameItemList.add(circle);
            return;
        }
        this.mPopoutFrameItemList.add(vertical);
        this.mPopoutFrameItemList.add(horizontal);
        this.mPopoutFrameItemList.add(hexagon);
        this.mPopoutFrameItemList.add(circle);
        this.mPopoutFrameItemList.add(rect);
    }

    private void initEffectBtnLayout(int popoutEffect) {
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
        if (vg != null) {
            this.mGet.inflateView(C0088R.layout.popout_button_layout, vg);
            this.mBtnLayout = vg.findViewById(C0088R.id.popout_button_layout);
            this.mBtnList = new ArrayList();
            if (this.mBtnLayout != null && this.mBtnList != null && this.mBtnLayout != null) {
                this.mBtnBlur = (RotateImageButton) this.mBtnLayout.findViewById(C0088R.id.btn_popout_effect_blur);
                this.mBtnGrayScale = (RotateImageButton) this.mBtnLayout.findViewById(C0088R.id.btn_popout_effect_grayscale);
                this.mBtnVignetting = (RotateImageButton) this.mBtnLayout.findViewById(C0088R.id.btn_popout_effect_vignetting);
                this.mBtnPerspective = (RotateImageButton) this.mBtnLayout.findViewById(C0088R.id.btn_popout_effect_perspective);
                this.mBtnBlur.setText(this.mGet.getActivity().getResources().getString(C0088R.string.popout_blur));
                this.mBtnGrayScale.setText(this.mGet.getActivity().getResources().getString(C0088R.string.popout_grayscale));
                this.mBtnVignetting.setText(this.mGet.getActivity().getResources().getString(C0088R.string.popout_vignetting));
                this.mBtnPerspective.setText(this.mGet.getActivity().getResources().getString(C0088R.string.popout_perspective));
                this.mBtnList.add(this.mBtnBlur);
                this.mBtnList.add(this.mBtnGrayScale);
                this.mBtnList.add(this.mBtnVignetting);
                this.mBtnList.add(this.mBtnPerspective);
                setBtnListeners();
                if ((popoutEffect & 1) != 0) {
                    this.mBtnBlur.setSelected(true);
                }
                if ((popoutEffect & 4) != 0) {
                    this.mBtnGrayScale.setSelected(true);
                }
                if ((popoutEffect & 2) != 0) {
                    this.mBtnVignetting.setSelected(true);
                }
                if ((popoutEffect & 8) != 0) {
                    this.mBtnPerspective.setSelected(true);
                }
            }
        }
    }

    private void setBtnListeners() {
        for (View mBtn : this.mBtnList) {
            mBtn.setOnClickListener(new C12471());
        }
    }

    private void onEffectBtnOnClick(View btn) {
        boolean z = true;
        if (!this.mGet.isModuleChanging() && !this.mGet.isTimerShotCountdown() && !this.mGet.isIntervalShotProgress() && this.mGet.checkModuleValidate(31) && !this.mGet.isActivatedQuickview()) {
            boolean mIsSelected = btn.isSelected();
            boolean result = true;
            if (btn.equals(this.mBtnBlur)) {
                result = this.mListener.onPopoutEffectBtnClick(1);
            } else if (btn.equals(this.mBtnGrayScale)) {
                result = this.mListener.onPopoutEffectBtnClick(4);
            } else if (btn.equals(this.mBtnVignetting)) {
                result = this.mListener.onPopoutEffectBtnClick(2);
            } else if (btn.equals(this.mBtnPerspective)) {
                result = this.mListener.onPopoutEffectBtnClick(8);
            }
            if (result) {
                boolean z2;
                if (mIsSelected) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                btn.setSelected(z2);
                if (mIsSelected) {
                    checkNormalEffect();
                }
                Context appContext = getAppContext();
                Context appContext2 = getAppContext();
                if (mIsSelected) {
                    z = false;
                }
                TalkBackUtil.setTalkbackDescOnDoubleTap(appContext, appContext2.getString(getPopoutEffectButtonDescription(btn, z)));
                btn.setContentDescription(getAppContext().getString(getPopoutEffectButtonDescription(btn, mIsSelected)));
            }
        }
    }

    private int getPopoutEffectButtonDescription(View btn, boolean mIsSelected) {
        if (btn.equals(this.mBtnBlur)) {
            return mIsSelected ? C0088R.string.popout_blur : C0088R.string.lens_blur_off;
        } else {
            if (btn.equals(this.mBtnGrayScale)) {
                return mIsSelected ? C0088R.string.black_and_white : C0088R.string.black_and_white_off;
            } else {
                if (btn.equals(this.mBtnVignetting)) {
                    return mIsSelected ? C0088R.string.popout_vignetting : C0088R.string.vignette_off;
                } else {
                    if (btn.equals(this.mBtnPerspective)) {
                        return mIsSelected ? C0088R.string.popout_perspective : C0088R.string.fisheye_off;
                    } else {
                        return C0088R.string.dummy_space;
                    }
                }
            }
        }
    }

    private void checkNormalEffect() {
        if (!this.mBtnBlur.isSelected() && !this.mBtnGrayScale.isSelected() && !this.mBtnVignetting.isSelected() && !this.mBtnPerspective.isSelected()) {
            this.mListener.onPopoutEffectBtnClick(0);
        }
    }

    public void setEffectButtonVisibility(boolean show) {
        if (this.mBtnLayout != null) {
            if (show) {
                this.mBtnLayout.setVisibility(0);
            } else {
                this.mBtnLayout.setVisibility(4);
            }
        }
    }

    public void setEffectButtonEnable(boolean enabled) {
        if (this.mBtnList != null) {
            ColorFilter colorFilter;
            if (enabled) {
                colorFilter = ColorUtil.getNormalColorByAlpha();
            } else {
                colorFilter = ColorUtil.getDimColorByAlpha();
            }
            for (RotateImageButton btn : this.mBtnList) {
                btn.setEnabled(enabled);
                btn.setColorFilter(colorFilter);
                btn.setTextColorFilter(colorFilter);
            }
        }
    }

    public void removeAllChildItems() {
        if (this.mFrameLayout != null) {
            LinearLayout mFrameChildView = (LinearLayout) this.mFrameLayout.findViewById(C0088R.id.popout_frame_child_view);
            if (mFrameChildView != null) {
                if (mFrameChildView != null && mFrameChildView.getChildCount() > 0) {
                    mFrameChildView.removeAllViews();
                }
                if (this.mPopoutFrameItemList != null) {
                    this.mPopoutFrameItemList.clear();
                    this.mPopoutFrameItemList = null;
                }
            }
        }
    }

    public void onDestroy() {
        removeAllChildItems();
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
        if (!(vg == null || this.mBtnLayout == null)) {
            vg.removeView(this.mBtnLayout);
        }
        this.mBtnBlur = null;
        this.mBtnGrayScale = null;
        this.mBtnVignetting = null;
        this.mBtnPerspective = null;
        this.mHeartFarmeButton = null;
        this.mInstantPicButton = null;
        this.mBtnLayout = null;
        if (this.mBtnList != null) {
            this.mBtnList.clear();
            this.mBtnList = null;
        }
        removeFrameSelectView();
        this.mListener = null;
        super.onDestroy();
    }

    public void setDegree(int degree, boolean animation) {
        if (this.mBtnList != null && this.mBtnLayout != null) {
            int convertDegree = degree % 360;
            int frameDegree = convertDegree + 90;
            if (Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
                convertDegree = (degree + 180) % 360;
            }
            for (RotateImageButton mBtn : this.mBtnList) {
                if (mBtn != null) {
                    mBtn.setDegree(convertDegree, animation);
                }
            }
            if ((this.mCurFrameValue == 5 || this.mCurFrameValue == 6) && this.mParentButton != null) {
                this.mParentButton.setDegree(frameDegree, animation);
            }
            if (this.mHeartFarmeButton != null) {
                this.mHeartFarmeButton.setDegree(frameDegree, animation);
            }
            if (this.mInstantPicButton != null) {
                this.mInstantPicButton.setDegree(frameDegree, animation);
            }
        }
    }

    public boolean isEffectButtonVisible() {
        if (this.mBtnLayout == null) {
            return false;
        }
        return this.mBtnLayout.isShown();
    }

    private void addFrameButton() {
        if (this.mFrameLayout != null) {
            this.mArrayLayoutSelector = new SparseArray();
            int[] layoutSelectorSettingValues = new int[]{0, 1, 2, 4, 3};
            if (ModelProperties.isMTKChipset()) {
                layoutSelectorSettingValues = new int[]{0, 1, 5, 6, 4, 7};
            }
            for (int frameValue : layoutSelectorSettingValues) {
                int viewId = getLayoutSeletorViewId(frameValue);
                RotateImageButton button = (RotateImageButton) this.mFrameLayout.findViewById(viewId);
                if (button != null) {
                    button.setOnClickListener(getSelectorClickListener(frameValue));
                    button.setVisibility(0);
                    this.mArrayLayoutSelector.put(viewId, button);
                }
            }
            this.mHeartFarmeButton = (RotateImageButton) this.mGet.findViewById(getLayoutSeletorViewId(5));
            this.mInstantPicButton = (RotateImageButton) this.mGet.findViewById(getLayoutSeletorViewId(6));
            this.mParentButton = (RotateImageButton) this.mFrameLayout.findViewById(C0088R.id.popout_frame_parent_item_selected);
            this.mChildView = this.mFrameLayout.findViewById(C0088R.id.popout_frame_child_view);
            if (this.mChildView != null) {
                this.mChildView.setOnTouchListener(new C12482());
            }
            if (this.mFrameButtonLayout != null) {
                setPopoutFrameLayoutButtonDescription();
                this.mFrameButtonLayout.setFocusable(true);
                this.mFrameButtonLayout.setOnClickListener(new C12493());
            }
        }
    }

    public void setPopoutFrameLayoutButtonDescription() {
        if (this.mFrameButtonLayout != null) {
            this.mFrameButtonLayout.setContentDescription(getAppContext().getString(C0088R.string.popout_layout_button) + getAppContext().getString(C0088R.string.button) + " " + getAppContext().getString(getLayoutSelctorDescriptionId(this.mCurFrameValue)));
        }
    }

    private OnClickListener getSelectorClickListener(int value) {
        return new OnLayoutSelectorClickListener(value);
    }

    private void setFrameSelection(int layoutSettingValue) {
        final int curId = getLayoutSeletorViewId(layoutSettingValue);
        int curResId = getLayoutSeletorResId(layoutSettingValue);
        if (layoutSettingValue == 5 || layoutSettingValue == 6) {
            this.mParentButton.setBackground(getAppContext().getDrawable(C0088R.drawable.setting_popout_frame_base));
            this.mParentButton.setRotateIconOnly(true);
            this.mParentButton.setDegree(getOrientationDegree() + 90, false);
        } else {
            this.mParentButton.setBackgroundColor(Color.parseColor("#00000000"));
            this.mParentButton.setRotateIconOnly(false);
            this.mParentButton.setDegree(0, false);
        }
        if (this.mParentButton != null) {
            this.mParentButton.setImageResource(curResId);
        }
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (PopoutCameraManager.this.mArrayLayoutSelector != null) {
                    for (int i = 0; i < PopoutCameraManager.this.mArrayLayoutSelector.size(); i++) {
                        ((RotateImageButton) PopoutCameraManager.this.mArrayLayoutSelector.valueAt(i)).setSelected(curId == PopoutCameraManager.this.mArrayLayoutSelector.keyAt(i));
                    }
                }
            }
        }, 0);
    }

    private int getLayoutSeletorViewId(int frameValue) {
        if (4 == frameValue) {
            return C0088R.id.popout_frame_division_horizontal;
        }
        if (3 == frameValue) {
            return C0088R.id.popout_frame_division_vertical;
        }
        if (1 == frameValue) {
            return C0088R.id.popout_frame_circle;
        }
        if (2 == frameValue) {
            return C0088R.id.popout_frame_hexagon;
        }
        if (5 == frameValue) {
            return C0088R.id.popout_frame_heart;
        }
        if (6 == frameValue) {
            return C0088R.id.popout_frame_instantpic;
        }
        if (7 == frameValue) {
            return C0088R.id.popout_frame_film;
        }
        return C0088R.id.popout_frame_rect;
    }

    private int getLayoutSeletorResId(int frameValue) {
        if (4 == frameValue) {
            return C0088R.drawable.setting_popout_frame_division_horizontal;
        }
        if (3 == frameValue) {
            return C0088R.drawable.setting_popout_frame_division_vertical;
        }
        if (1 == frameValue) {
            return C0088R.drawable.setting_popout_frame_circle;
        }
        if (2 == frameValue) {
            return C0088R.drawable.setting_popout_frame_hexagon;
        }
        if (5 == frameValue) {
            return C0088R.drawable.setting_popout_frame_heart_only;
        }
        if (6 == frameValue) {
            return C0088R.drawable.setting_popout_frame_instantpic_only;
        }
        if (7 == frameValue) {
            return C0088R.drawable.setting_popout_frame_film;
        }
        return C0088R.drawable.setting_popout_frame_rect;
    }

    private int getLayoutSelctorDescriptionId(int frameValue) {
        if (4 == frameValue) {
            return C0088R.string.popout_frame_horizontal_division;
        }
        if (3 == frameValue) {
            return C0088R.string.popout_frame_vertical_division;
        }
        if (1 == frameValue) {
            return C0088R.string.popout_frame_circle;
        }
        if (2 == frameValue) {
            return C0088R.string.popout_frame_hexagon;
        }
        if (5 == frameValue) {
            return C0088R.string.popout_frame_heart;
        }
        if (6 == frameValue) {
            return C0088R.string.popout_frame_instantpic;
        }
        if (7 == frameValue) {
            return C0088R.string.popout_frame_film;
        }
        return C0088R.string.popout_frame_rectangle;
    }

    public void toggleChildMenu() {
        if (this.mIsFrameOpen) {
            setVisibilityFrameChildMenu(false, true);
            this.mIsFrameOpen = false;
            return;
        }
        setVisibilityFrameChildMenu(true, true);
        this.mIsFrameOpen = true;
    }

    public void setVisibilityFrameChildMenu(boolean isShow, boolean useAni) {
        if (this.mIsFrameOpen != isShow) {
            if (useAni) {
                AnimationUtil.startTransAnimationForPullDownMenu(this.mChildView, isShow, null);
            } else {
                this.mChildView.setVisibility(isShow ? 0 : 4);
            }
            this.mIsFrameOpen = isShow;
            if (this.mParentArrow != null) {
                this.mParentArrow.setRotation(isShow ? 0.0f : 180.0f);
            }
        }
    }

    public void setFrameEnabled(boolean enabled) {
        if (this.mParentButton != null && this.mFrameButtonLayout != null && this.mParentArrow != null && this.mParentButton.getBackground() != null) {
            ColorMatrixColorFilter colorFilter;
            if (enabled) {
                colorFilter = ColorUtil.getNormalColorByAlpha();
            } else {
                colorFilter = ColorUtil.getDimColorByAlpha();
            }
            this.mFrameButtonLayout.setEnabled(enabled);
            this.mParentButton.setEnabled(enabled);
            this.mParentArrow.setEnabled(enabled);
            this.mParentButton.setColorFilter(colorFilter);
            this.mParentButton.getBackground().setColorFilter(colorFilter);
            this.mParentArrow.setColorFilter(colorFilter);
        }
    }

    public void setFrameVisibility(boolean isShow) {
        if (this.mFrameLayout != null) {
            this.mFrameLayout.setVisibility(isShow ? 0 : 4);
            if (!isShow) {
                setVisibilityFrameChildMenu(false, false);
            }
        }
    }

    public boolean isFrameLayoutOpen() {
        return this.mIsFrameOpen;
    }

    public boolean doBackKey() {
        if (!this.mIsFrameOpen) {
            return false;
        }
        toggleChildMenu();
        return true;
    }

    public void removeFrameSelectView() {
        if (this.mArrayLayoutSelector != null) {
            this.mArrayLayoutSelector.clear();
            this.mArrayLayoutSelector = null;
        }
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
        if (vg != null && this.mFrameLayout != null) {
            vg.removeView(this.mFrameLayout);
            this.mParentArrow = null;
            this.mParentButton = null;
            this.mFrameLayout = null;
            this.mFrameButtonLayout = null;
        }
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        setVisibilityFrameChildMenu(false, true);
        this.mIsFrameOpen = false;
    }

    public void shiftEffectButtonLayout(int previewHeight, int topMargin) {
        if (this.mBtnLayout != null) {
            FrameLayout.LayoutParams rlp = (FrameLayout.LayoutParams) this.mBtnLayout.getLayoutParams();
            if (rlp != null) {
                rlp.gravity = 48;
                int marginTop = topMargin + (previewHeight / 2);
                Drawable d = getAppContext().getResources().getDrawable(C0088R.drawable.btn_popout_effect_blur, null);
                int between = Utils.getPx(getAppContext(), C0088R.dimen.popout_effect_btn_between);
                if (d != null) {
                    int width = d.getIntrinsicWidth();
                    int size = this.mBtnList.size();
                    marginTop -= ((width * size) + ((size - 1) * between)) / 2;
                }
                rlp.setMarginsRelative(rlp.getMarginStart(), marginTop, rlp.getMarginEnd(), rlp.bottomMargin);
                this.mBtnLayout.setLayoutParams(rlp);
            }
        }
    }

    public String getPopoutFrameLDBString() {
        switch (this.mCurFrameValue) {
            case 1:
                return "Circle|";
            case 2:
                return "Hexagon|";
            case 3:
                return "Vertical_Division|";
            case 4:
                return "Horizontal_Division|";
            case 5:
                return "Heart|";
            case 6:
                return "Instant Picture|";
            case 7:
                return "Film|";
            default:
                return "Rect|";
        }
    }

    public void onChangePictureSize() {
        changeFrameLayoutMargin();
    }

    private void changeFrameLayoutMargin() {
        if (this.mFrameButtonLayout != null) {
            RelativeLayout.LayoutParams frameLp = (RelativeLayout.LayoutParams) this.mFrameButtonLayout.getLayoutParams();
            if (frameLp != null) {
                frameLp.topMargin = RatioCalcUtil.getQuickButtonWidth(getAppContext());
            }
        }
    }
}
