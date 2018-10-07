package com.lge.camera.managers.ext;

import android.content.res.Configuration;
import android.graphics.ColorMatrixColorFilter;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateView;
import com.lge.camera.constants.CameraConstants;
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

public class TimeLapseManager extends ManagerInterfaceImpl {
    private ImageView mArrowBtn = null;
    private List<View> mBtnList = null;
    private boolean mIsDrawerOpen = false;
    private OnTimeLapseFpsBtnListener mListener;
    private int mSelectedIndex = 15;
    private ViewGroup mTimeLapsBaseView = null;
    private View mTimeLapseChildItemView = null;
    private View mTimeLapseChildSpeed10Btn = null;
    private View mTimeLapseChildSpeed15Btn = null;
    private View mTimeLapseChildSpeed30Btn = null;
    private View mTimeLapseChildSpeed60Btn = null;
    private View mTimeLapseChildView = null;
    private RotateImageButton mTimeLapseParenSpeedtBtn = null;
    private View mTimeLapseParentView = null;
    private View mTimeLapseSelectorView = null;

    public interface OnTimeLapseFpsBtnListener {
        void onTimeLapseBtnClick(int i);
    }

    /* renamed from: com.lge.camera.managers.ext.TimeLapseManager$1 */
    class C13351 implements OnClickListener {
        C13351() {
        }

        public void onClick(View v) {
            TimeLapseManager.this.onTimeLapseBtnOnClick(v);
        }
    }

    /* renamed from: com.lge.camera.managers.ext.TimeLapseManager$2 */
    class C13362 implements OnTouchListener {
        C13362() {
        }

        public boolean onTouch(View arg0, MotionEvent arg1) {
            return true;
        }
    }

    public void setOnViewModeButtonListener(OnTimeLapseFpsBtnListener listener) {
        this.mListener = listener;
    }

    public TimeLapseManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        initTimeLapseSpeedBtnUI();
    }

    private void initTimeLapseSpeedBtnUI() {
        this.mBtnList = new ArrayList();
        this.mTimeLapsBaseView = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        this.mTimeLapseSelectorView = this.mGet.inflateView(C0088R.layout.time_lapse_speed_selector);
        View quickButtonView = this.mGet.findViewById(C0088R.id.quick_button_layout_root);
        int index = 0;
        if (quickButtonView != null) {
            index = this.mTimeLapsBaseView.indexOfChild(quickButtonView) + 1;
        }
        this.mTimeLapsBaseView.addView(this.mTimeLapseSelectorView, index, new LayoutParams(-1, -1));
        this.mTimeLapseChildView = this.mTimeLapseSelectorView.findViewById(C0088R.id.time_lapse_child_view);
        this.mTimeLapseParentView = this.mTimeLapseSelectorView.findViewById(C0088R.id.time_lapse_parent_view);
        this.mTimeLapseChildItemView = this.mTimeLapseChildView.findViewById(C0088R.id.time_lapse_child_item_view);
        LayoutParams selectorParam = (LayoutParams) this.mTimeLapseSelectorView.getLayoutParams();
        if (ModelProperties.getLCDType() == 0) {
            selectorParam.topMargin = RatioCalcUtil.getQuickButtonWidth(getAppContext());
        } else {
            selectorParam.topMargin = RatioCalcUtil.getLongLCDModelTopMargin(getAppContext(), 16, 9, 0);
        }
        this.mTimeLapseParenSpeedtBtn = (RotateImageButton) this.mTimeLapseParentView.findViewById(C0088R.id.time_lapse_parent_item_btn);
        if (ModelProperties.isTablet(getAppContext())) {
            LayoutParams lp = (LayoutParams) this.mTimeLapseParenSpeedtBtn.getLayoutParams();
            lp.setMarginEnd(RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.0625f));
            this.mTimeLapseParenSpeedtBtn.setLayoutParams(lp);
        }
        this.mArrowBtn = (ImageView) this.mTimeLapseParentView.findViewById(C0088R.id.time_lapse_parent_item_arrow);
        this.mTimeLapseChildSpeed10Btn = this.mTimeLapseChildItemView.findViewById(C0088R.id.time_lapse_speed10_btn);
        this.mTimeLapseChildSpeed15Btn = this.mTimeLapseChildItemView.findViewById(C0088R.id.time_lapse_speed15_btn);
        this.mTimeLapseChildSpeed30Btn = this.mTimeLapseChildItemView.findViewById(C0088R.id.time_lapse_speed30_btn);
        this.mTimeLapseChildSpeed60Btn = this.mTimeLapseChildItemView.findViewById(C0088R.id.time_lapse_speed60_btn);
        this.mBtnList.add(this.mTimeLapseParentView);
        this.mBtnList.add(this.mTimeLapseChildSpeed10Btn);
        this.mBtnList.add(this.mTimeLapseChildSpeed15Btn);
        this.mBtnList.add(this.mTimeLapseChildSpeed30Btn);
        this.mBtnList.add(this.mTimeLapseChildSpeed60Btn);
        setBtnSpacing();
        setRotateDegree(this.mGet.getOrientationDegree(), false);
        setBtnListener();
        setTimeLapseParentBtn();
        this.mTimeLapseSelectorView.setVisibility(4);
        setTimeLapseChildViewVisibility(true, false);
        setParentBtnTalkbackContentDescription();
    }

    private void setBtnSpacing() {
        int quickBtnWidth = getAppContext().getResources().getDrawable(C0088R.drawable.btn_quickbutton_setting_expand_button, null).getIntrinsicWidth();
        int qflSpace = (((Utils.getLCDsize(getAppContext(), true)[1] - (Utils.getPx(getAppContext(), C0088R.dimen.quick_button_margin) * 2)) - quickBtnWidth) / 4) - quickBtnWidth;
        int space = (((qflSpace + quickBtnWidth) * 3) / (this.mBtnList.size() - 1)) - getAppContext().getResources().getDrawable(C0088R.drawable.camera_icon_setting_timelapse_x10_normal, null).getIntrinsicWidth();
        for (View btn : this.mBtnList) {
            if (btn.equals(this.mTimeLapseParentView)) {
                this.mArrowBtn.getLayoutParams().width = space;
            } else {
                ((LinearLayout.LayoutParams) btn.getLayoutParams()).setMarginStart(space);
            }
        }
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        initTimeLapseSpeedBtnUI();
    }

    private void setParentBtnTalkbackContentDescription() {
        CamLog.m3d(CameraConstants.TAG, "setParentBtnTalkbackContentDescription");
        this.mSelectedIndex = SharedPreferenceUtil.getTimeLapseSpeedValue(this.mGet.getAppContext());
        String desc = String.format(this.mGet.getAppContext().getString(C0088R.string.time_lapse_speed_titleid), new Object[]{Integer.valueOf(this.mSelectedIndex)});
        if (this.mTimeLapseParentView != null) {
            this.mTimeLapseParentView.setContentDescription(desc);
        }
    }

    private void setBtnListener() {
        CamLog.m3d(CameraConstants.TAG, "setBtnListener");
        for (View mBtn : this.mBtnList) {
            mBtn.setOnClickListener(new C13351());
        }
        this.mTimeLapseChildItemView.setOnTouchListener(new C13362());
    }

    private void onTimeLapseBtnOnClick(View btn) {
        CamLog.m3d(CameraConstants.TAG, "onTimeLapseBtnOnClick");
        int selectedChildBtn = 0;
        if (btn != null && this.mTimeLapseParentView != null && this.mTimeLapseChildSpeed10Btn != null && this.mTimeLapseChildSpeed15Btn != null && this.mTimeLapseChildSpeed30Btn != null && this.mTimeLapseChildSpeed60Btn != null) {
            switch (btn.getId()) {
                case C0088R.id.time_lapse_speed60_btn:
                    selectedChildBtn = 60;
                    break;
                case C0088R.id.time_lapse_speed30_btn:
                    selectedChildBtn = 30;
                    break;
                case C0088R.id.time_lapse_speed15_btn:
                    selectedChildBtn = 15;
                    break;
                case C0088R.id.time_lapse_speed10_btn:
                    selectedChildBtn = 10;
                    break;
                case C0088R.id.time_lapse_parent_view:
                    toggleChildMenu();
                    return;
            }
            SharedPreferenceUtil.saveTimeLapseSpeedValue(this.mGet.getAppContext(), selectedChildBtn);
            this.mSelectedIndex = SharedPreferenceUtil.getTimeLapseSpeedValue(this.mGet.getAppContext());
            this.mListener.onTimeLapseBtnClick(this.mSelectedIndex);
            CamLog.m3d(CameraConstants.TAG, "mSelectedIndex : " + this.mSelectedIndex);
            setTimeLapseParentBtn();
            setTimeLapseChildViewVisibility(true, true);
            setParentBtnTalkbackContentDescription();
        }
    }

    public void setTimeLapseParentBtn() {
        CamLog.m3d(CameraConstants.TAG, "setTimeLapseParentBtn");
        int resourceId = 0;
        this.mSelectedIndex = SharedPreferenceUtil.getTimeLapseSpeedValue(this.mGet.getAppContext());
        CamLog.m3d(CameraConstants.TAG, "mSelectedIndex : " + this.mSelectedIndex);
        if (this.mSelectedIndex == 10) {
            resourceId = C0088R.drawable.setting_time_lapse_speed_10;
        } else if (this.mSelectedIndex == 15) {
            resourceId = C0088R.drawable.setting_time_lapse_speed_15;
        } else if (this.mSelectedIndex == 30) {
            resourceId = C0088R.drawable.setting_time_lapse_speed_30;
        } else if (this.mSelectedIndex == 60) {
            resourceId = C0088R.drawable.setting_time_lapse_speed_60;
        }
        this.mTimeLapseParenSpeedtBtn.setImageResource(resourceId);
        setPressedChildBtn();
    }

    public void setPressedChildBtn() {
        CamLog.m3d(CameraConstants.TAG, "setPressedChildBtn");
        int selectedIndexId = 0;
        this.mSelectedIndex = SharedPreferenceUtil.getTimeLapseSpeedValue(this.mGet.getAppContext());
        CamLog.m3d(CameraConstants.TAG, "mSelectedIndex : " + this.mSelectedIndex);
        if (this.mSelectedIndex == 10) {
            selectedIndexId = C0088R.id.time_lapse_speed10_btn;
        } else if (this.mSelectedIndex == 15) {
            selectedIndexId = C0088R.id.time_lapse_speed15_btn;
        } else if (this.mSelectedIndex == 30) {
            selectedIndexId = C0088R.id.time_lapse_speed30_btn;
        } else if (this.mSelectedIndex == 60) {
            selectedIndexId = C0088R.id.time_lapse_speed60_btn;
        }
        for (int i = 0; i < this.mBtnList.size(); i++) {
            int id = ((View) this.mBtnList.get(i)).getId();
            CamLog.m3d(CameraConstants.TAG, "id : " + id + ", selectedIndexId : " + selectedIndexId);
            ((View) this.mBtnList.get(i)).setSelected(selectedIndexId == id);
        }
    }

    public void toggleChildMenu() {
        if (this.mIsDrawerOpen) {
            this.mIsDrawerOpen = false;
            this.mArrowBtn.setRotation(180.0f);
        } else {
            this.mIsDrawerOpen = true;
            this.mTimeLapseChildView.setVisibility(0);
            this.mArrowBtn.setRotation(0.0f);
        }
        AnimationUtil.startTransAnimationForPullDownMenu(this.mTimeLapseChildItemView, this.mIsDrawerOpen, null);
    }

    public void setTimeLapseChildViewVisibility(boolean isShow, boolean isAni) {
        int i = 0;
        if (this.mTimeLapseParentView != null && this.mTimeLapseChildView != null && this.mArrowBtn != null) {
            this.mIsDrawerOpen = false;
            if (isShow) {
                this.mArrowBtn.setRotation(180.0f);
            }
            View view = this.mTimeLapseParentView;
            if (!isShow) {
                i = 4;
            }
            view.setVisibility(i);
            if (isAni) {
                AnimationUtil.startTransAnimationForPullDownMenu(this.mTimeLapseChildItemView, this.mIsDrawerOpen, null);
            } else {
                this.mTimeLapseChildView.setVisibility(4);
            }
        }
    }

    public boolean isDrawerOpen() {
        return this.mIsDrawerOpen;
    }

    public void setTimLapseBtnEnable(boolean show) {
        if (this.mTimeLapseParentView != null && this.mTimeLapseParenSpeedtBtn != null && this.mArrowBtn != null) {
            ColorMatrixColorFilter colorFilter = show ? ColorUtil.getNormalColorByAlpha() : ColorUtil.getDimColorByAlpha();
            this.mTimeLapseParentView.setEnabled(show);
            this.mTimeLapseParenSpeedtBtn.setColorFilter(colorFilter);
            this.mArrowBtn.setColorFilter(colorFilter);
        }
    }

    public void onDestroy() {
        if (!(this.mTimeLapsBaseView == null || this.mTimeLapseSelectorView == null)) {
            this.mTimeLapsBaseView.removeView(this.mTimeLapseSelectorView);
        }
        this.mTimeLapseChildSpeed60Btn = null;
        this.mTimeLapseChildSpeed30Btn = null;
        this.mTimeLapseChildSpeed15Btn = null;
        this.mTimeLapseChildSpeed10Btn = null;
        this.mArrowBtn = null;
        this.mTimeLapseParenSpeedtBtn = null;
        this.mTimeLapseChildItemView = null;
        this.mTimeLapseParentView = null;
        this.mTimeLapseChildView = null;
        this.mTimeLapseSelectorView = null;
        if (this.mBtnList != null) {
            this.mBtnList.clear();
            this.mBtnList = null;
        }
        this.mListener = null;
        super.onDestroy();
    }

    public void setRotateDegree(int degree, boolean animation) {
        super.setRotateDegree(degree, animation);
        if (this.mTimeLapseParenSpeedtBtn != null) {
            this.mTimeLapseParenSpeedtBtn.setDegree(degree, animation);
        }
        if (this.mTimeLapseChildSpeed10Btn != null) {
            ((RotateView) this.mTimeLapseChildSpeed10Btn).setDegree(degree, animation);
        }
        if (this.mTimeLapseChildSpeed15Btn != null) {
            ((RotateView) this.mTimeLapseChildSpeed15Btn).setDegree(degree, animation);
        }
        if (this.mTimeLapseChildSpeed30Btn != null) {
            ((RotateView) this.mTimeLapseChildSpeed30Btn).setDegree(degree, animation);
        }
        if (this.mTimeLapseChildSpeed60Btn != null) {
            ((RotateView) this.mTimeLapseChildSpeed60Btn).setDegree(degree, animation);
        }
    }

    public void setTimeSelectViewVisibility(boolean isShow, boolean useAnim) {
        if (this.mTimeLapseSelectorView != null) {
            if (useAnim) {
                AnimationUtil.startShowingAnimation(this.mTimeLapseSelectorView, isShow, 150, null);
            } else {
                this.mTimeLapseSelectorView.setVisibility(isShow ? 0 : 4);
            }
        }
    }
}
