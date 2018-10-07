package com.lge.camera.managers.ext;

import android.graphics.ColorMatrixColorFilter;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.MemoryUtils;

public class MultiViewLayoutSelectionManager extends ManagerInterfaceImpl {
    protected SparseArray<RotateImageButton> mArrayLayoutSelector = null;
    protected View mBaseView = null;
    protected View mChildView = null;
    protected MultiViewModuleInterface mGet = null;
    protected boolean mIsOpen = false;
    protected boolean mIsViewCreated = false;
    protected ImageView mParentArrow = null;
    protected RotateImageButton mParentButton = null;

    /* renamed from: com.lge.camera.managers.ext.MultiViewLayoutSelectionManager$1 */
    class C12281 implements OnTouchListener {
        C12281() {
        }

        public boolean onTouch(View arg0, MotionEvent arg1) {
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.MultiViewLayoutSelectionManager$2 */
    class C12292 implements OnClickListener {
        C12292() {
        }

        public void onClick(View v) {
            MultiViewLayoutSelectionManager.this.toggleChildMenu();
        }
    }

    private class OnLayoutSelectorClickListener implements OnClickListener {
        private String mValue = null;

        public OnLayoutSelectorClickListener(String value) {
            this.mValue = value;
        }

        public void onClick(View view) {
            if (this.mValue.equals(MultiViewLayoutSelectionManager.this.mGet.getSettingValue(Setting.KEY_MULTIVIEW_LAYOUT))) {
                MultiViewLayoutSelectionManager.this.setLayoutSelection(this.mValue);
                MultiViewLayoutSelectionManager.this.setVisibilityChildMenu(false, true);
                return;
            }
            MultiViewLayoutSelectionManager.this.mGet.setSetting(Setting.KEY_MULTIVIEW_LAYOUT, this.mValue, true);
            MultiViewLayoutSelectionManager.this.mGet.changeLayoutOnMultiview(this.mValue);
            MultiViewLayoutSelectionManager.this.setLayoutSelection(this.mValue);
            MultiViewLayoutSelectionManager.this.setVisibilityChildMenu(false, true);
        }
    }

    protected OnClickListener getSelectorClickListener(String value) {
        return new OnLayoutSelectorClickListener(value);
    }

    public MultiViewLayoutSelectionManager(MultiViewModuleInterface moduleInterface) {
        super(moduleInterface);
        this.mGet = moduleInterface;
    }

    protected void createView() {
        if (!this.mIsViewCreated) {
            CamLog.m3d(CameraConstants.TAG, "-multiview- createView");
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            this.mBaseView = this.mGet.inflateView(C0088R.layout.multiview_layout_selector);
            if (vg != null && this.mBaseView != null) {
                View quickButtonView = this.mGet.findViewById(C0088R.id.quick_button_layout_root);
                int index = 0;
                if (quickButtonView != null) {
                    index = vg.indexOfChild(quickButtonView) + 1;
                }
                vg.addView(this.mBaseView, index, new LayoutParams(-1, -1));
                this.mBaseView.setVisibility(4);
                this.mIsViewCreated = true;
            }
        }
    }

    protected void hideUselessLayout() {
        if (this.mBaseView != null) {
            ImageView singleView = (ImageView) this.mBaseView.findViewById(C0088R.id.multi_view_layout_single);
            if (singleView != null) {
                singleView.setVisibility(8);
            }
        }
    }

    protected void addLayout() {
        int i = 0;
        if (this.mBaseView != null) {
            this.mArrayLayoutSelector = new SparseArray();
            String[] layoutSelectorSettingValues = new String[]{CameraConstants.MULTIVIEW_LAYOUT_SINGLE, CameraConstants.MULTIVIEW_LAYOUT_SPLIT, CameraConstants.MULTIVIEW_LAYOUT_TRIPLE01, CameraConstants.MULTIVIEW_LAYOUT_TRIPLE02, CameraConstants.MULTIVIEW_LAYOUT_QUAD};
            int length = layoutSelectorSettingValues.length;
            while (i < length) {
                String settingValue = layoutSelectorSettingValues[i];
                int viewId = getLayoutSeletorViewId(settingValue);
                RotateImageButton button = (RotateImageButton) this.mBaseView.findViewById(viewId);
                if (button != null) {
                    button.setOnClickListener(getSelectorClickListener(settingValue));
                    this.mArrayLayoutSelector.put(viewId, button);
                }
                i++;
            }
            this.mParentButton = (RotateImageButton) this.mBaseView.findViewById(C0088R.id.multi_view_layout_parent_item_selected);
            CamLog.m3d(CameraConstants.TAG, "-sd- orientation = " + this.mGet.getOrientationDegree());
            this.mChildView = this.mBaseView.findViewById(C0088R.id.multi_view_layout_child_view);
            if (!(this.mChildView == null || this.mParentButton == null)) {
                this.mChildView.setOnTouchListener(new C12281());
                this.mParentButton.setOnClickListener(new C12292());
            }
            this.mParentArrow = (ImageView) this.mBaseView.findViewById(C0088R.id.multi_view_layout_parent_item_arrow);
        }
    }

    protected void initLayoutSelector() {
        this.mGet.restoreSettingValue(Setting.KEY_MULTIVIEW_LAYOUT);
        setLayoutSelection(this.mGet.getSettingValue(Setting.KEY_MULTIVIEW_LAYOUT));
    }

    public void toggleChildMenu() {
        if (this.mIsOpen) {
            setVisibilityChildMenu(false, true);
            this.mIsOpen = false;
            return;
        }
        setVisibilityChildMenu(true, true);
        this.mIsOpen = true;
    }

    public void setVisibilityChildMenu(boolean isShow, boolean useAni) {
        if (this.mIsOpen != isShow) {
            if (useAni) {
                AnimationUtil.startTransAnimationForPullDownMenu(this.mChildView, isShow, null);
            } else {
                this.mChildView.setVisibility(isShow ? 0 : 4);
            }
            this.mIsOpen = isShow;
            if (this.mParentArrow != null) {
                this.mParentArrow.setImageResource(isShow ? C0088R.drawable.camera_icon_setting_multiview_arrow_up : C0088R.drawable.camera_icon_setting_multiview_arrow_down);
            }
        }
    }

    public void setEnabled(boolean enabled) {
        if (this.mParentButton != null && this.mParentArrow != null) {
            if (this.mGet.isMultiviewIntervalShot()) {
                enabled = false;
                CamLog.m3d(CameraConstants.TAG, "-ml- setEnabled changed to false during multiview intervalshot.");
            }
            ColorMatrixColorFilter colorFilter = enabled ? ColorUtil.getNormalColorByAlpha() : ColorUtil.getDimColorByAlpha();
            this.mParentButton.setEnabled(enabled);
            this.mParentArrow.setEnabled(enabled);
            this.mParentButton.setColorFilter(colorFilter);
            this.mParentArrow.setColorFilter(colorFilter);
        }
    }

    public void setVisibility(boolean isShow) {
        if (this.mBaseView != null) {
            if (this.mGet.isMultiviewIntervalShot()) {
                isShow = false;
                CamLog.m3d(CameraConstants.TAG, "-ml- setVisibility changed to false during multiview intervalshot.");
            }
            this.mBaseView.setVisibility(isShow ? 0 : 4);
            if (!isShow) {
                setVisibilityChildMenu(false, false);
            }
        }
    }

    protected void setLayoutSelection(String layoutSettingValue) {
        if (this.mGet != null && this.mGet.getActivity() != null) {
            final int curId = getLayoutSeletorViewId(layoutSettingValue);
            int curResId = getLayoutSeletorResId(layoutSettingValue);
            if (this.mParentButton != null) {
                this.mParentButton.setImageResource(curResId);
                this.mParentButton.setContentDescription(this.mGet.getActivity().getString(C0088R.string.multiview_layout_selection) + this.mGet.getActivity().findViewById(getLayoutSeletorViewId(layoutSettingValue)).getContentDescription());
            }
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (MultiViewLayoutSelectionManager.this.mArrayLayoutSelector != null) {
                        for (int i = 0; i < MultiViewLayoutSelectionManager.this.mArrayLayoutSelector.size(); i++) {
                            ((RotateImageButton) MultiViewLayoutSelectionManager.this.mArrayLayoutSelector.valueAt(i)).setSelected(curId == MultiViewLayoutSelectionManager.this.mArrayLayoutSelector.keyAt(i));
                        }
                    }
                }
            }, 0);
        }
    }

    protected int getLayoutSeletorViewId(String settingValue) {
        if (CameraConstants.MULTIVIEW_LAYOUT_SPLIT.equals(settingValue)) {
            return C0088R.id.multi_view_layout_split;
        }
        if (CameraConstants.MULTIVIEW_LAYOUT_TRIPLE01.equals(settingValue)) {
            return C0088R.id.multi_view_layout_triple1;
        }
        if (CameraConstants.MULTIVIEW_LAYOUT_TRIPLE02.equals(settingValue)) {
            return C0088R.id.multi_view_layout_triple2;
        }
        if (CameraConstants.MULTIVIEW_LAYOUT_QUAD.equals(settingValue)) {
            return C0088R.id.multi_view_layout_quad;
        }
        return C0088R.id.multi_view_layout_single;
    }

    protected int getLayoutSeletorResId(String settingValue) {
        if (CameraConstants.MULTIVIEW_LAYOUT_SPLIT.equals(settingValue)) {
            return C0088R.drawable.setting_multiview_layout_split;
        }
        if (CameraConstants.MULTIVIEW_LAYOUT_TRIPLE01.equals(settingValue)) {
            return C0088R.drawable.setting_multiview_layout_triple1;
        }
        if (CameraConstants.MULTIVIEW_LAYOUT_TRIPLE02.equals(settingValue)) {
            return C0088R.drawable.setting_multiview_layout_triple2;
        }
        if (CameraConstants.MULTIVIEW_LAYOUT_QUAD.equals(settingValue)) {
            return C0088R.drawable.setting_multiview_layout_quad;
        }
        return C0088R.drawable.setting_multiview_layout_single;
    }

    public boolean isOpen() {
        return this.mIsOpen;
    }

    public boolean doBackKey() {
        if (!this.mIsOpen) {
            return false;
        }
        toggleChildMenu();
        return true;
    }

    public void removeView() {
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (vg != null && this.mBaseView != null) {
            vg.removeView(this.mBaseView);
            MemoryUtils.releaseViews(this.mBaseView);
            this.mIsViewCreated = false;
        }
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        createView();
        hideUselessLayout();
        addLayout();
        initLayoutSelector();
    }

    public void onPauseBefore() {
        this.mIsOpen = false;
        if (!this.mGet.isCameraChanging()) {
            this.mGet.restoreSettingValue(Setting.KEY_MULTIVIEW_LAYOUT);
        }
        removeView();
        super.onPauseBefore();
    }
}
