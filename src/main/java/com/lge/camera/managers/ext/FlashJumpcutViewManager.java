package com.lge.camera.managers.ext;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ColorMatrixColorFilter;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.components.RotateView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.List;

public class FlashJumpcutViewManager extends ManagerInterfaceImpl {
    private final float JUMPCUT_ENTERING_COUNT_GUIDE_MARGIN_BOTTOM = 0.227f;
    private final float JUMPCUT_ENTERING_GUIDE_MARGIN_BOTTOM_LANDSCAPE = 0.04f;
    private final float JUMPCUT_INIT_GUIDE_MARGIN_BOTTOM = 0.369f;
    private final float JUMPCUT_INIT_GUIDE_MARGIN_BOTTOM_NO_EXTRA_PREVIEW_BUTTON = 0.285f;
    private ImageView mArrowBtn = null;
    private List<View> mBtnList = null;
    private int mCountTextGuideMarginTop_Landscape;
    private int mEnteringGuideMarginTop_Landscape;
    private int mInitGuideMarginTop_Landscape;
    private boolean mIsDrawerOpen = false;
    private ViewGroup mJumpcutBaseView = null;
    private View mJumpcutChildItemView = null;
    private View mJumpcutChildShot12Btn = null;
    private View mJumpcutChildShot16Btn = null;
    private View mJumpcutChildShot20Btn = null;
    private View mJumpcutChildShot4Btn = null;
    private View mJumpcutChildShot8Btn = null;
    private View mJumpcutChildView = null;
    private RotateLayout mJumpcutCountTextViewRotate = null;
    private LinearLayout mJumpcutCountWrapperView = null;
    private RotateTextView mJumpcutDivisionCountTextView = null;
    private TextView mJumpcutEnteringCountTextView = null;
    private RotateLayout mJumpcutEnteringCountTextViewRotate = null;
    private View mJumpcutGuideView = null;
    private RelativeLayout mJumpcutInitGuideViewLayout = null;
    private RotateLayout mJumpcutInitGuideViewRotate = null;
    private RotateImageButton mJumpcutParentShotBtn = null;
    private View mJumpcutParentView = null;
    private RotateTextView mJumpcutProgressCountTextView = null;
    private RotateTextView mJumpcutSelectedCountTextView = null;
    private View mJumpcutSelectorView = null;
    private int mSelectedIndex = 4;

    /* renamed from: com.lge.camera.managers.ext.FlashJumpcutViewManager$1 */
    class C11981 implements OnClickListener {
        C11981() {
        }

        public void onClick(View v) {
            FlashJumpcutViewManager.this.onJumpcutBtnOnClick(v);
        }
    }

    /* renamed from: com.lge.camera.managers.ext.FlashJumpcutViewManager$2 */
    class C11992 implements OnTouchListener {
        C11992() {
        }

        public boolean onTouch(View arg0, MotionEvent arg1) {
            return true;
        }
    }

    public FlashJumpcutViewManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        initJumpcutShotUI();
    }

    private void initJumpcutShotUI() {
        CamLog.m3d(CameraConstants.TAG, "[jumpcut] initJumpcutShotUI. mSelectedIndex : " + this.mSelectedIndex + ", preference : " + SharedPreferenceUtil.getJumpcutShotCount(getAppContext()));
        this.mBtnList = new ArrayList();
        if (this.mJumpcutBaseView == null) {
            this.mJumpcutBaseView = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        }
        if (this.mJumpcutSelectorView == null) {
            this.mJumpcutSelectorView = this.mGet.inflateView(C0088R.layout.flash_jumpcut_shot_selector);
        }
        if (this.mJumpcutGuideView == null) {
            this.mJumpcutGuideView = this.mGet.inflateView(C0088R.layout.flash_jumpcut_guide);
        }
        View quickButtonView = this.mGet.findViewById(C0088R.id.quick_button_layout_root);
        int index = 0;
        if (quickButtonView != null) {
            index = this.mJumpcutBaseView.indexOfChild(quickButtonView) + 1;
        }
        if (this.mJumpcutSelectorView == null || this.mJumpcutGuideView == null) {
            CamLog.m11w(CameraConstants.TAG, "[jumpcut] return init jumpcut view. mJumpcutView is NULL.");
            return;
        }
        this.mJumpcutBaseView.addView(this.mJumpcutSelectorView, index, new LayoutParams(-1, -1));
        this.mJumpcutBaseView.addView(this.mJumpcutGuideView, index);
        this.mJumpcutChildView = this.mJumpcutSelectorView.findViewById(C0088R.id.flash_jumpcut_child_view);
        this.mJumpcutParentView = this.mJumpcutSelectorView.findViewById(C0088R.id.flash_jumpcut_parent_view);
        this.mJumpcutChildItemView = this.mJumpcutChildView.findViewById(C0088R.id.flash_jumpcut_child_item_view);
        this.mJumpcutInitGuideViewLayout = (RelativeLayout) this.mJumpcutGuideView.findViewById(C0088R.id.flash_jumpcut_init_guide_text_inner_layout);
        this.mJumpcutInitGuideViewRotate = (RotateLayout) this.mJumpcutGuideView.findViewById(C0088R.id.flash_jumpcut_init_guide_text_rotateLayout);
        this.mJumpcutEnteringCountTextViewRotate = (RotateLayout) this.mJumpcutGuideView.findViewById(C0088R.id.flash_jumpcut_entering_text_layout);
        this.mJumpcutEnteringCountTextView = (TextView) this.mJumpcutGuideView.findViewById(C0088R.id.flash_jumpcut_entering_text);
        this.mJumpcutProgressCountTextView = (RotateTextView) this.mJumpcutGuideView.findViewById(C0088R.id.flash_jumpcut_progress_count);
        this.mJumpcutDivisionCountTextView = (RotateTextView) this.mJumpcutGuideView.findViewById(C0088R.id.flash_jumpcut_count_division);
        this.mJumpcutSelectedCountTextView = (RotateTextView) this.mJumpcutGuideView.findViewById(C0088R.id.flash_jumpcut_selected_count);
        this.mJumpcutCountWrapperView = (LinearLayout) this.mJumpcutGuideView.findViewById(C0088R.id.flash_jumpcut_shot_count_text_layout);
        this.mJumpcutCountTextViewRotate = (RotateLayout) this.mJumpcutGuideView.findViewById(C0088R.id.flash_jumpcut_shot_count_text_rotate);
        LayoutParams selectorParam = (LayoutParams) this.mJumpcutSelectorView.getLayoutParams();
        if (ModelProperties.getLCDType() == 0) {
            selectorParam.topMargin = RatioCalcUtil.getQuickButtonWidth(getAppContext());
        } else {
            selectorParam.topMargin = RatioCalcUtil.getLongLCDModelTopMargin(getAppContext(), 16, 9, 0);
        }
        this.mJumpcutParentShotBtn = (RotateImageButton) this.mJumpcutParentView.findViewById(C0088R.id.flash_jumpcut_parent_item_btn);
        this.mArrowBtn = (ImageView) this.mJumpcutParentView.findViewById(C0088R.id.flash_jumpcut_parent_item_arrow);
        this.mJumpcutChildShot4Btn = this.mJumpcutChildItemView.findViewById(C0088R.id.flash_jumpcut_shot4_btn);
        this.mJumpcutChildShot8Btn = this.mJumpcutChildItemView.findViewById(C0088R.id.flash_jumpcut_shot8_btn);
        this.mJumpcutChildShot12Btn = this.mJumpcutChildItemView.findViewById(C0088R.id.flash_jumpcut_shot12_btn);
        this.mJumpcutChildShot16Btn = this.mJumpcutChildItemView.findViewById(C0088R.id.flash_jumpcut_shot16_btn);
        this.mJumpcutChildShot20Btn = this.mJumpcutChildItemView.findViewById(C0088R.id.flash_jumpcut_shot20_btn);
        this.mBtnList.add(this.mJumpcutParentView);
        this.mBtnList.add(this.mJumpcutChildShot4Btn);
        this.mBtnList.add(this.mJumpcutChildShot8Btn);
        this.mBtnList.add(this.mJumpcutChildShot12Btn);
        this.mBtnList.add(this.mJumpcutChildShot16Btn);
        this.mBtnList.add(this.mJumpcutChildShot20Btn);
        setBtnSpacing();
        setRotateDegree(this.mGet.getOrientationDegree(), false);
        setGuideTextLayoutParam();
        setBtnListener();
        this.mSelectedIndex = SharedPreferenceUtil.getJumpcutShotCount(getAppContext());
        setJumpcutParentBtn();
        this.mJumpcutSelectorView.setVisibility(4);
        setJumpcutChildViewVisibility(true, false);
        setParentBtnTalkbackContentDescription();
    }

    public void showJumpCutInitGuideText(boolean show) {
        setGuideTextLayoutParam();
        if (this.mJumpcutInitGuideViewLayout != null) {
            this.mJumpcutInitGuideViewLayout.setVisibility(show ? 0 : 4);
        }
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        if (this.mJumpcutInitGuideViewLayout != null) {
            this.mJumpcutInitGuideViewLayout.setVisibility(4);
        }
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        setRotateDegree(this.mGet.getOrientationDegree(), false);
        CamLog.m3d(CameraConstants.TAG, "[jumpcut] onResumeAfter. mSelectedIndex : " + this.mSelectedIndex + ", preference : " + SharedPreferenceUtil.getJumpcutShotCount(getAppContext()));
        this.mSelectedIndex = SharedPreferenceUtil.getJumpcutShotCount(getAppContext());
        setJumpcutParentBtn();
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        removeLayout();
        initJumpcutShotUI();
    }

    private void setParentBtnTalkbackContentDescription() {
        String desc = String.format(this.mGet.getAppContext().getString(C0088R.string.flash_jumpcut_titleid), new Object[]{Integer.valueOf(this.mSelectedIndex)});
        if (this.mJumpcutParentView != null) {
            this.mJumpcutParentView.setContentDescription(desc);
        }
    }

    private void setBtnListener() {
        for (View mBtn : this.mBtnList) {
            mBtn.setOnClickListener(new C11981());
        }
        this.mJumpcutChildItemView.setOnTouchListener(new C11992());
    }

    private void onJumpcutBtnOnClick(View btn) {
        int selectedChildBtn = 0;
        if (btn != null && this.mJumpcutParentView != null && this.mJumpcutChildShot4Btn != null && this.mJumpcutChildShot8Btn != null && this.mJumpcutChildShot12Btn != null && this.mJumpcutChildShot16Btn != null && this.mJumpcutChildShot20Btn != null && !this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL)) {
            switch (btn.getId()) {
                case C0088R.id.flash_jumpcut_shot20_btn:
                    selectedChildBtn = 20;
                    break;
                case C0088R.id.flash_jumpcut_shot16_btn:
                    selectedChildBtn = 16;
                    break;
                case C0088R.id.flash_jumpcut_shot12_btn:
                    selectedChildBtn = 12;
                    break;
                case C0088R.id.flash_jumpcut_shot8_btn:
                    selectedChildBtn = 8;
                    break;
                case C0088R.id.flash_jumpcut_shot4_btn:
                    selectedChildBtn = 4;
                    break;
                case C0088R.id.flash_jumpcut_parent_view:
                    toggleChildMenu();
                    return;
            }
            SharedPreferenceUtil.saveJumpcutShotCount(this.mGet.getAppContext(), selectedChildBtn);
            this.mSelectedIndex = SharedPreferenceUtil.getJumpcutShotCount(this.mGet.getAppContext());
            CamLog.m3d(CameraConstants.TAG, "[jumpcut] onJumpcutBtnOnClick. mSelectedIndex : " + this.mSelectedIndex);
            setJumpcutParentBtn();
            setJumpcutChildViewVisibility(true, true);
            setParentBtnTalkbackContentDescription();
        }
    }

    private void setBtnSpacing() {
        int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
        int quickBtnWidth = getAppContext().getResources().getDrawable(C0088R.drawable.btn_quickbutton_setting_expand_button, null).getIntrinsicWidth();
        int qflSpace = (((lcdSize[1] - (Utils.getPx(getAppContext(), C0088R.dimen.quick_button_margin) * 2)) - quickBtnWidth) / 4) - quickBtnWidth;
        int speedBtnWidth = getAppContext().getResources().getDrawable(C0088R.drawable.camera_icon_setting_jumpcut_16_pressed, null).getIntrinsicWidth();
        int arrowSpace = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.025f);
        int space = (((qflSpace + quickBtnWidth) * 3) / (this.mBtnList.size() - 1)) - speedBtnWidth;
        for (View btn : this.mBtnList) {
            if (btn.equals(this.mJumpcutParentView)) {
                this.mArrowBtn.getLayoutParams().width = arrowSpace;
            } else {
                ((LinearLayout.LayoutParams) btn.getLayoutParams()).setMarginStart(space);
            }
        }
    }

    public void setJumpcutParentBtn() {
        int resourceId = 0;
        if (this.mSelectedIndex == 4) {
            resourceId = C0088R.drawable.setting_flash_jumpcut_shot_4;
        } else if (this.mSelectedIndex == 8) {
            resourceId = C0088R.drawable.setting_flash_jumpcut_shot_8;
        } else if (this.mSelectedIndex == 12) {
            resourceId = C0088R.drawable.setting_flash_jumpcut_shot_12;
        } else if (this.mSelectedIndex == 16) {
            resourceId = C0088R.drawable.setting_flash_jumpcut_shot_16;
        } else if (this.mSelectedIndex == 20) {
            resourceId = C0088R.drawable.setting_flash_jumpcut_shot_20;
        }
        this.mJumpcutParentShotBtn.setImageResource(resourceId);
        setPressedChildBtn();
    }

    public void setPressedChildBtn() {
        int selectedIndexId = 0;
        if (this.mSelectedIndex == 4) {
            selectedIndexId = C0088R.id.flash_jumpcut_shot4_btn;
        } else if (this.mSelectedIndex == 8) {
            selectedIndexId = C0088R.id.flash_jumpcut_shot8_btn;
        } else if (this.mSelectedIndex == 12) {
            selectedIndexId = C0088R.id.flash_jumpcut_shot12_btn;
        } else if (this.mSelectedIndex == 16) {
            selectedIndexId = C0088R.id.flash_jumpcut_shot16_btn;
        } else if (this.mSelectedIndex == 20) {
            selectedIndexId = C0088R.id.flash_jumpcut_shot20_btn;
        }
        for (int i = 0; i < this.mBtnList.size(); i++) {
            ((View) this.mBtnList.get(i)).setSelected(selectedIndexId == ((View) this.mBtnList.get(i)).getId());
        }
    }

    public void toggleChildMenu() {
        if (this.mIsDrawerOpen) {
            this.mIsDrawerOpen = false;
            this.mArrowBtn.setRotation(180.0f);
        } else {
            this.mIsDrawerOpen = true;
            this.mJumpcutChildView.setVisibility(0);
            this.mArrowBtn.setRotation(0.0f);
        }
        AnimationUtil.startTransAnimationForPullDownMenu(this.mJumpcutChildItemView, this.mIsDrawerOpen, null);
    }

    public void setJumpcutChildViewVisibility(boolean isShow, boolean isAni) {
        int i = 0;
        if (this.mJumpcutParentView != null && this.mJumpcutChildView != null && this.mArrowBtn != null) {
            this.mIsDrawerOpen = false;
            if (isShow) {
                this.mArrowBtn.setRotation(180.0f);
            }
            View view = this.mJumpcutParentView;
            if (!isShow) {
                i = 4;
            }
            view.setVisibility(i);
            if (isAni) {
                AnimationUtil.startTransAnimationForPullDownMenu(this.mJumpcutChildItemView, this.mIsDrawerOpen, null);
            } else {
                this.mJumpcutChildView.setVisibility(4);
            }
        }
    }

    public boolean isDrawerOpen() {
        return this.mIsDrawerOpen;
    }

    public void setJumpcutBtnEnable(boolean show) {
        if (this.mJumpcutParentView != null && this.mJumpcutParentShotBtn != null && this.mArrowBtn != null) {
            ColorMatrixColorFilter colorFilter = show ? ColorUtil.getNormalColorByAlpha() : ColorUtil.getDimColorByAlpha();
            this.mJumpcutParentView.setEnabled(show);
            this.mJumpcutParentShotBtn.setColorFilter(colorFilter);
            this.mArrowBtn.setColorFilter(colorFilter);
        }
    }

    private void removeLayout() {
        CamLog.m7i(CameraConstants.TAG, "[jumpcut] removeLayout");
        if (!(this.mJumpcutBaseView == null || this.mJumpcutSelectorView == null)) {
            this.mJumpcutBaseView.removeView(this.mJumpcutSelectorView);
        }
        if (!(this.mJumpcutBaseView == null || this.mJumpcutGuideView == null)) {
            this.mJumpcutBaseView.removeView(this.mJumpcutGuideView);
        }
        this.mJumpcutBaseView = null;
        this.mJumpcutChildShot20Btn = null;
        this.mJumpcutChildShot16Btn = null;
        this.mJumpcutChildShot12Btn = null;
        this.mJumpcutChildShot8Btn = null;
        this.mJumpcutChildShot4Btn = null;
        this.mArrowBtn = null;
        this.mJumpcutParentShotBtn = null;
        this.mJumpcutChildItemView = null;
        this.mJumpcutParentView = null;
        this.mJumpcutChildView = null;
        this.mJumpcutSelectorView = null;
        this.mJumpcutGuideView = null;
        this.mJumpcutEnteringCountTextView = null;
        this.mJumpcutEnteringCountTextViewRotate = null;
        this.mJumpcutProgressCountTextView = null;
        this.mJumpcutDivisionCountTextView = null;
        this.mJumpcutSelectedCountTextView = null;
        this.mJumpcutCountWrapperView = null;
        this.mJumpcutCountTextViewRotate = null;
        this.mJumpcutInitGuideViewLayout = null;
        this.mJumpcutInitGuideViewRotate = null;
        if (this.mBtnList != null) {
            this.mBtnList.clear();
            this.mBtnList = null;
        }
    }

    public void onDestroy() {
        removeLayout();
        super.onDestroy();
    }

    public void setRotateDegree(int degree, boolean animation) {
        super.setRotateDegree(degree, animation);
        if (this.mJumpcutParentShotBtn != null) {
            this.mJumpcutParentShotBtn.setDegree(degree, animation);
        }
        if (this.mJumpcutChildShot4Btn != null) {
            ((RotateView) this.mJumpcutChildShot4Btn).setDegree(degree, animation);
        }
        if (this.mJumpcutChildShot8Btn != null) {
            ((RotateView) this.mJumpcutChildShot8Btn).setDegree(degree, animation);
        }
        if (this.mJumpcutChildShot12Btn != null) {
            ((RotateView) this.mJumpcutChildShot12Btn).setDegree(degree, animation);
        }
        if (this.mJumpcutChildShot16Btn != null) {
            ((RotateView) this.mJumpcutChildShot16Btn).setDegree(degree, animation);
        }
        if (this.mJumpcutChildShot20Btn != null) {
            ((RotateView) this.mJumpcutChildShot20Btn).setDegree(degree, animation);
        }
        if (this.mJumpcutCountTextViewRotate != null) {
            this.mJumpcutCountTextViewRotate.setAngle(degree);
        }
        if (this.mJumpcutEnteringCountTextViewRotate != null) {
            this.mJumpcutEnteringCountTextViewRotate.setAngle(degree);
        }
        if (this.mJumpcutInitGuideViewRotate != null) {
            setGuideTextLayoutParam();
            this.mJumpcutInitGuideViewRotate.setAngle(degree);
        }
    }

    public void setShotSelectViewVisibility(boolean isShow, boolean useAnim) {
        if (this.mJumpcutSelectorView != null) {
            if (useAnim) {
                AnimationUtil.startShowingAnimation(this.mJumpcutSelectorView, isShow, 150, null);
            } else {
                this.mJumpcutSelectorView.setVisibility(isShow ? 0 : 4);
            }
        }
    }

    public void showFlashJumpCutEnteringGuide() {
        Animation animFadein = AnimationUtils.loadAnimation(this.mGet.getAppContext(), C0088R.anim.fade_in);
        animFadein.setDuration(380);
        setGuideTextLayoutParam();
        String initGuideText = String.format(getAppContext().getString(C0088R.string.flash_jump_cut_guide), new Object[]{Integer.valueOf(this.mSelectedIndex)});
        if (this.mJumpcutEnteringCountTextView != null) {
            this.mJumpcutEnteringCountTextView.setVisibility(0);
            this.mJumpcutEnteringCountTextView.setText(initGuideText);
            this.mJumpcutEnteringCountTextView.startAnimation(animFadein);
        }
    }

    public void setInitGuideTextLayout(int width, int height, int startMargin, int topMargin) {
        if (this.mJumpcutInitGuideViewRotate != null && this.mJumpcutEnteringCountTextViewRotate != null && this.mJumpcutCountTextViewRotate != null && ((LayoutParams) this.mJumpcutInitGuideViewRotate.getLayoutParams()) != null) {
            this.mJumpcutInitGuideViewRotate.measure(0, 0);
            this.mJumpcutEnteringCountTextViewRotate.measure(0, 0);
            this.mJumpcutCountTextViewRotate.measure(0, 0);
            int marginTop = startMargin + (height / 2);
            int degree = this.mGet.getOrientationDegree();
            int initGuideLength = (degree == 0 || degree == 180) ? this.mJumpcutInitGuideViewRotate.getMeasuredWidth() : this.mJumpcutInitGuideViewRotate.getMeasuredHeight();
            int enteringGuideLength = (degree == 0 || degree == 180) ? this.mJumpcutEnteringCountTextViewRotate.getMeasuredWidth() : this.mJumpcutEnteringCountTextViewRotate.getMeasuredHeight();
            int countTextLength = (degree == 0 || degree == 180) ? this.mJumpcutCountTextViewRotate.getMeasuredWidth() : this.mJumpcutCountTextViewRotate.getMeasuredHeight();
            this.mInitGuideMarginTop_Landscape = marginTop - (initGuideLength / 2);
            this.mEnteringGuideMarginTop_Landscape = marginTop - (enteringGuideLength / 2);
            this.mCountTextGuideMarginTop_Landscape = marginTop - (countTextLength / 2);
        }
    }

    public void setGuideTextLayoutParam() {
        if (this.mJumpcutInitGuideViewRotate != null && this.mJumpcutEnteringCountTextViewRotate != null && this.mJumpcutCountTextViewRotate != null) {
            LayoutParams lp = (LayoutParams) this.mJumpcutInitGuideViewRotate.getLayoutParams();
            LayoutParams enteringGuideLp = (LayoutParams) this.mJumpcutEnteringCountTextViewRotate.getLayoutParams();
            LayoutParams countLp = (LayoutParams) this.mJumpcutCountTextViewRotate.getLayoutParams();
            Utils.resetLayoutParameter(lp);
            Utils.resetLayoutParameter(enteringGuideLp);
            Utils.resetLayoutParameter(countLp);
            this.mJumpcutInitGuideViewRotate.measure(0, 0);
            switch (Utils.convertDegree(this.mGet.getAppContext().getResources(), this.mGet.getOrientationDegree())) {
                case 0:
                case 180:
                    lp.addRule(12);
                    lp.addRule(14);
                    Context appContext = getAppContext();
                    float f = (!this.mGet.isRearCamera() || FunctionProperties.isSupportedSticker()) ? 0.369f : 0.285f;
                    lp.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(appContext, true, f);
                    enteringGuideLp.addRule(12);
                    enteringGuideLp.addRule(14);
                    enteringGuideLp.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.227f);
                    countLp.addRule(12);
                    countLp.addRule(14);
                    countLp.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.227f);
                    break;
                case 90:
                case 270:
                    lp.addRule(20);
                    lp.addRule(10);
                    lp.topMargin = this.mInitGuideMarginTop_Landscape;
                    lp.setMarginStart(RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.04f));
                    enteringGuideLp.addRule(20);
                    enteringGuideLp.addRule(10);
                    enteringGuideLp.topMargin = this.mEnteringGuideMarginTop_Landscape;
                    enteringGuideLp.setMarginStart(RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.04f));
                    countLp.addRule(20);
                    countLp.addRule(10);
                    countLp.topMargin = this.mCountTextGuideMarginTop_Landscape;
                    countLp.setMarginStart(RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.04f));
                    break;
            }
            this.mJumpcutInitGuideViewRotate.setLayoutParams(lp);
            this.mJumpcutEnteringCountTextViewRotate.setLayoutParams(enteringGuideLp);
            this.mJumpcutCountTextViewRotate.setLayoutParams(countLp);
        }
    }

    public void showFlashJumpCutCountGuide(int count) {
        if (this.mJumpcutEnteringCountTextView != null && this.mJumpcutProgressCountTextView != null && this.mJumpcutDivisionCountTextView != null && this.mJumpcutSelectedCountTextView != null) {
            int shot = count + 1;
            Animation animFadein = AnimationUtils.loadAnimation(this.mGet.getAppContext(), C0088R.anim.fade_in);
            animFadein.setDuration(380);
            CamLog.m3d(CameraConstants.TAG, "[jumpcut][guide] mJumpcutProgressCountTextView : " + Integer.toString(count) + ", mJumpcutSelectedCountTextView : " + Integer.toString(this.mSelectedIndex));
            setGuideTextLayoutParam();
            if (shot <= 1) {
                String initGuideText = String.format(getAppContext().getString(C0088R.string.flash_jump_cut_guide), new Object[]{Integer.valueOf(this.mSelectedIndex)});
                if (this.mJumpcutEnteringCountTextView != null) {
                    this.mJumpcutEnteringCountTextView.setVisibility(0);
                    this.mJumpcutEnteringCountTextView.setText(initGuideText);
                    this.mJumpcutEnteringCountTextView.startAnimation(animFadein);
                    return;
                }
                return;
            }
            int size = Utils.getPx(getAppContext(), C0088R.dimen.gesture_guide_text.textSize);
            this.mJumpcutProgressCountTextView.setText(Integer.toString(count));
            this.mJumpcutDivisionCountTextView.setText("/");
            this.mJumpcutSelectedCountTextView.setText(Integer.toString(this.mSelectedIndex));
            this.mJumpcutDivisionCountTextView.setTextSize(size);
            this.mJumpcutProgressCountTextView.setTextSize(size);
            this.mJumpcutSelectedCountTextView.setTextSize(size);
            if (this.mJumpcutCountWrapperView != null) {
                this.mJumpcutCountWrapperView.setVisibility(0);
                this.mJumpcutCountWrapperView.startAnimation(animFadein);
            }
        }
    }

    public void hideFlashJumpCutCountGuide() {
        if (this.mJumpcutCountWrapperView != null && this.mJumpcutEnteringCountTextView != null) {
            this.mJumpcutCountWrapperView.setVisibility(4);
            this.mJumpcutEnteringCountTextView.setVisibility(4);
        }
    }
}
