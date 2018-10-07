package com.lge.camera.managers;

import android.graphics.ColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.managers.ConeUIManagerInterface.OnConeViewModeButtonListener;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.TelephonyUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class ConeUIManagerBase implements OnRemoveHandler {
    private List<RotateImageButton> mBtnList = null;
    private LinearLayout mConeViewModeItemLayout = null;
    private int mCurrentConeMode = 1;
    private ConeUIManagerInterface mGet = null;
    private LayoutInflater mInflater = null;
    private boolean mIsViewModeChangedAtQuickShotMode = false;
    private ArrayList<LinearLayout> mListItems = null;
    private OnConeViewModeButtonListener mListener;
    private ArrayList<ConeModeItem> mModeListItems = null;
    private RotateImageButton mNormalAutoModeBtn = null;
    private RotateImageButton mNormalManualModeBtn = null;
    private RotateImageButton mNormalManualVideoModeBtn = null;
    private RotateImageButton mSquareModeBtn = null;

    /* renamed from: com.lge.camera.managers.ConeUIManagerBase$1 */
    class C08781 implements OnClickListener {
        C08781() {
        }

        public void onClick(View v) {
            ConeUIManagerBase.this.onModeBtnOnClick(v);
        }
    }

    private class ConeModeItem {
        public int mDescriptionId;
        public int mId;
        public int mResId;
        public int mTextId;

        public ConeModeItem(int id, int textId, int resId, int desId) {
            this.mId = id;
            this.mTextId = textId;
            this.mResId = resId;
            this.mDescriptionId = desId;
        }
    }

    public ConeUIManagerBase(ConeUIManagerInterface activityInterface) {
        this.mGet = activityInterface;
        inflateLayout();
        makeListItems();
        this.mIsViewModeChangedAtQuickShotMode = false;
    }

    private void inflateLayout() {
        this.mInflater = LayoutInflater.from(Utils.getFixedDensityContext(this.mGet.getAppContext()));
    }

    private void makeListItems() {
        this.mModeListItems = new ArrayList();
        if (this.mModeListItems != null) {
            if (FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO)) {
                this.mModeListItems.add(new ConeModeItem(C0088R.id.cone_view_mode_manual_video_view, C0088R.string.cone_manual_view_button, C0088R.drawable.btn_second_screen_video, C0088R.string.camera_help_manual_video_title));
            }
            if (FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA)) {
                this.mModeListItems.add(new ConeModeItem(C0088R.id.cone_view_mode_manual_camera_view, C0088R.string.cone_manual_view_button, C0088R.drawable.btn_second_screen_camera, C0088R.string.camera_help_manual_camera_title));
            }
            if (FunctionProperties.isSupportedMode(CameraConstants.MODE_SQUARE)) {
                this.mModeListItems.add(new ConeModeItem(C0088R.id.cone_view_square_view, C0088R.string.view_mode_square, C0088R.drawable.btn_second_screen_camera_auto, C0088R.string.view_mode_square));
            }
            this.mModeListItems.add(new ConeModeItem(C0088R.id.cone_view_mode_auto_view, C0088R.string.cone_auto_view_button, C0088R.drawable.btn_second_screen_camera_auto, C0088R.string.auto_mode_button));
        }
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }

    public void setOnConeViewModeButtonListener(OnConeViewModeButtonListener listener) {
        this.mListener = listener;
    }

    public void init() {
        this.mConeViewModeItemLayout = (LinearLayout) this.mGet.getMiniActivity().findViewById(C0088R.id.cone_view_item_layout);
        if (this.mConeViewModeItemLayout != null && this.mConeViewModeItemLayout.getChildCount() <= 0) {
            addModeList();
            addModeButton();
            addModeButtonList();
            setBtnListeners();
            this.mListItems = new ArrayList();
            this.mListItems.add(this.mConeViewModeItemLayout);
            setItemsMargin();
            enableConeMenuIcon(false);
            setConeModeChanged();
            if (!this.mGet.isAttachIntent()) {
                enableConeMenuIcon(true);
            }
        }
    }

    public void initConeMode() {
        this.mCurrentConeMode = SharedPreferenceUtil.getLastCameraMode(this.mGet.getAppContext(), 1);
        if (this.mGet.isAttachIntent() || this.mGet.isVideoCameraMode()) {
            CamLog.m3d(CameraConstants.TAG, "set normal view mode, because this is attach mode");
            this.mCurrentConeMode = 1;
        }
        if ((AppControlUtil.isQuickShotMode() && (AppControlUtil.isNeedQuickShotTaking() || !this.mIsViewModeChangedAtQuickShotMode)) || TelephonyUtil.phoneInCall(this.mGet.getAppContext())) {
            CamLog.m3d(CameraConstants.TAG, "set normal view mode, because this is quick shot mode or support cone ui and phone in call");
            this.mCurrentConeMode = 1;
        }
        AppControlUtil.setViewModeChangedForConeUIByPhoneState(TelephonyUtil.phoneInCall(this.mGet.getAppContext()), this.mGet.getAppContext());
    }

    private void addModeList() {
        if (this.mInflater != null) {
            Iterator it = this.mModeListItems.iterator();
            while (it.hasNext()) {
                ConeModeItem item = (ConeModeItem) it.next();
                RotateImageButton button = (RotateImageButton) this.mInflater.inflate(C0088R.layout.cone_item_text, null);
                if (button != null) {
                    button.setId(item.mId);
                    button.setContentDescription(this.mGet.getAppContext().getString(item.mDescriptionId));
                    if (item.mTextId != 0) {
                        button.setText(this.mGet.getAppContext().getString(item.mTextId).toUpperCase(Locale.US));
                    }
                    if (item.mResId != 0) {
                        button.setImageResource(item.mResId);
                    }
                    button.setRotateIconOnly(true);
                    this.mConeViewModeItemLayout.addView(button);
                }
            }
            this.mConeViewModeItemLayout.requestLayout();
        }
    }

    public void setCurrentViewMode(int mode, boolean save) {
        this.mCurrentConeMode = mode;
        if (!AppControlUtil.isNeedQuickShotTaking()) {
            this.mIsViewModeChangedAtQuickShotMode = true;
        }
        if (save) {
            SharedPreferenceUtilBase.saveLastCameraMode(this.mGet.getAppContext(), this.mCurrentConeMode);
            this.mGet.saveStartingWindowLayout(this.mCurrentConeMode);
        }
    }

    public int getCurrentViewMode() {
        int currentMode = this.mCurrentConeMode;
        if (this.mGet == null || this.mGet.isRearCamera()) {
            return currentMode;
        }
        if (ManualUtil.isManualCameraMode(this.mCurrentConeMode) || ManualUtil.isManualVideoMode(this.mCurrentConeMode)) {
            return 1;
        }
        return currentMode;
    }

    private void setItemsMargin() {
        Iterator it = this.mListItems.iterator();
        while (it.hasNext()) {
            LinearLayout layoutItem = (LinearLayout) it.next();
            for (int i = 0; i < layoutItem.getChildCount(); i++) {
                RotateImageButton button = (RotateImageButton) layoutItem.getChildAt(i);
                if (button != null) {
                    LayoutParams param = (LayoutParams) button.getLayoutParams();
                    if (param != null) {
                        if (button.getId() == C0088R.id.cone_view_mode_auto_view) {
                            param.setMargins(0, 0, Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.mini_activity_auto_icon_margin), 0);
                        } else if (button.getId() == C0088R.id.cone_view_mode_manual_camera_view) {
                            param.setMargins(0, 0, Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.mini_activity_manual_icon_margin), 0);
                        } else if (button.getId() == C0088R.id.cone_view_mode_manual_video_view) {
                            param.setMargins(0, 0, Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.mini_activity_manual_video_icon_margin), 0);
                        } else if (button.getId() == C0088R.id.cone_view_square_view) {
                            param.setMargins(0, 0, Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.mini_activity_manual_video_icon_margin), 0);
                        }
                        button.setLayoutParams(param);
                    }
                }
            }
        }
    }

    public void setConeModeChanged() {
        CamLog.m3d(CameraConstants.TAG, "setConeModeChanged : " + this.mGet.getCurrentConeMode());
        setSelectedViewButton(this.mGet.getCurrentConeMode(), false);
    }

    private void setEnableButton(View btn, boolean enabled) {
        ColorFilter colorFilter = enabled ? ColorUtil.getNormalColorByAlpha() : ColorUtil.getDimColorByAlpha();
        if (btn != null) {
            ((RotateImageButton) btn).setEnabled(enabled);
            ((RotateImageButton) btn).setColorFilter(colorFilter);
        }
    }

    public void enableConeMenuIcon(boolean enable) {
        enableConeMenuIcon(31, enable);
    }

    public void enableConeMenuIcon(int coneType, boolean enable) {
        if (!enable || !this.mGet.isAttachIntent()) {
            setClickable(enable);
            if ((coneType & 2) != 0) {
                setEnableButton(this.mNormalAutoModeBtn, enable);
            }
            if ((coneType & 4) != 0) {
                setEnableButton(this.mNormalManualModeBtn, enable);
            }
            if ((coneType & 8) != 0) {
                setEnableButton(this.mNormalManualVideoModeBtn, enable);
            }
            if ((coneType & 16) != 0) {
                setEnableButton(this.mSquareModeBtn, enable);
            }
        }
    }

    public void setClickable(boolean clickable) {
        CamLog.m3d(CameraConstants.TAG, "setClickable : " + clickable);
        Iterator it = this.mListItems.iterator();
        while (it.hasNext()) {
            LinearLayout layoutItem = (LinearLayout) it.next();
            for (int i = 0; i < layoutItem.getChildCount(); i++) {
                RotateImageButton button = (RotateImageButton) layoutItem.getChildAt(i);
                if (button != null) {
                    button.setClickable(clickable);
                }
            }
        }
    }

    private void setButtonSelected(View btn, boolean selected, boolean isClick) {
        if (btn != null) {
            CamLog.m3d(CameraConstants.TAG, "setButtonSelected : " + btn.getId());
            setButtonsUnselected();
            btn.setSelected(selected);
            changeModule(btn);
        }
    }

    private void setSelectedViewButton(int mode, boolean isClick) {
        CamLog.m3d(CameraConstants.TAG, "setSelectedViewButton : " + mode);
        switch (mode) {
            case 1:
                setButtonSelected(this.mNormalAutoModeBtn, true, isClick);
                return;
            case 2:
                setButtonSelected(this.mNormalManualModeBtn, true, isClick);
                return;
            case 3:
                setButtonSelected(this.mNormalManualVideoModeBtn, true, isClick);
                return;
            default:
                return;
        }
    }

    private void addModeButton() {
        this.mNormalAutoModeBtn = (RotateImageButton) this.mConeViewModeItemLayout.findViewById(C0088R.id.cone_view_mode_auto_view);
        this.mNormalManualModeBtn = (RotateImageButton) this.mConeViewModeItemLayout.findViewById(C0088R.id.cone_view_mode_manual_camera_view);
        this.mNormalManualVideoModeBtn = (RotateImageButton) this.mConeViewModeItemLayout.findViewById(C0088R.id.cone_view_mode_manual_video_view);
        this.mSquareModeBtn = (RotateImageButton) this.mConeViewModeItemLayout.findViewById(C0088R.id.cone_view_square_view);
    }

    private void addModeButtonList() {
        this.mBtnList = new ArrayList();
        if (this.mBtnList != null) {
            if (FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO)) {
                this.mBtnList.add(this.mNormalManualVideoModeBtn);
            }
            if (FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA)) {
                this.mBtnList.add(this.mNormalManualModeBtn);
            }
            this.mBtnList.add(this.mSquareModeBtn);
            this.mBtnList.add(this.mNormalAutoModeBtn);
        }
    }

    private void setBtnListeners() {
        for (View mBtn : this.mBtnList) {
            mBtn.setOnClickListener(new C08781());
        }
    }

    private void onModeBtnOnClick(View btn) {
        if (btn != null) {
            if (btn.isSelected() || this.mGet.isAnimationShowing() || this.mGet.isModuleChanging() || this.mGet.isCameraChanging() || this.mGet.isCameraReOpeningAfterInAndOutRecording() || this.mGet.getPreviewCoverVisibility() == 0 || !this.mGet.checkModuleValidate(223) || !this.mGet.checkFocusingStateWithFlash()) {
                CamLog.m3d(CameraConstants.TAG, "second screen button already selected or module change...");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "onModeBtnOnClick : " + btn.getId());
            if (btn.equals(this.mNormalAutoModeBtn)) {
                setSelectedViewButton(1, true);
            } else if (btn.equals(this.mNormalManualModeBtn)) {
                setSelectedViewButton(2, true);
            } else if (btn.equals(this.mNormalManualVideoModeBtn)) {
                setSelectedViewButton(3, true);
            }
        }
    }

    private void changeModule(View btn) {
        CamLog.m3d(CameraConstants.TAG, "changeModule : " + btn.getId());
        if (this.mListener == null) {
            CamLog.m3d(CameraConstants.TAG, "mListener is null");
            return;
        }
        if (btn.equals(this.mNormalAutoModeBtn) && this.mListener.checkOnConeMenuClicked(1)) {
            CamLog.m3d(CameraConstants.TAG, "mNormalAutoModeBtn");
            doChangeModuleWithPreviewCover(1);
        }
        if (btn.equals(this.mNormalManualModeBtn) && this.mListener.checkOnConeMenuClicked(2)) {
            CamLog.m3d(CameraConstants.TAG, "mNormalManualModeBtn");
            doChangeModuleWithPreviewCover(2);
        }
        if (btn.equals(this.mNormalManualVideoModeBtn) && this.mListener.checkOnConeMenuClicked(3)) {
            CamLog.m3d(CameraConstants.TAG, "mNormalManualVideoModeBtn");
            doChangeModuleWithPreviewCover(3);
        }
    }

    private void doChangeModuleWithPreviewCover(final int selectedViewMode) {
        this.mGet.removeUIBeforeModeChange();
        this.mGet.setPreviewCoverVisibility(0, true, new AnimationListener() {
            public void onAnimationStart(Animation arg0) {
            }

            public void onAnimationRepeat(Animation arg0) {
            }

            public void onAnimationEnd(Animation arg0) {
                ConeUIManagerBase.this.mGet.postOnUiThread(new HandlerRunnable(ConeUIManagerBase.this) {
                    public void handleRun() {
                        if (ConeUIManagerBase.this.mListener == null) {
                            CamLog.m3d(CameraConstants.TAG, "mListener is null");
                        } else if (selectedViewMode == 1 && ConeUIManagerBase.this.mGet.getCurrentConeMode() != 1) {
                            ConeUIManagerBase.this.mListener.onConeAutoViewButtonClick();
                        } else if (selectedViewMode == 2 && ConeUIManagerBase.this.mGet.getCurrentConeMode() != 2) {
                            ConeUIManagerBase.this.mListener.onConeManualCameraButtonClick();
                        } else if (selectedViewMode == 3 && ConeUIManagerBase.this.mGet.getCurrentConeMode() != 3) {
                            ConeUIManagerBase.this.mListener.onConeManualCameraVideoButtonClick();
                        }
                    }
                });
            }
        }, true, false);
    }

    private void setButtonsUnselected() {
        CamLog.m3d(CameraConstants.TAG, "setButtonsUnselected");
        Iterator it = this.mListItems.iterator();
        while (it.hasNext()) {
            LinearLayout layoutItem = (LinearLayout) it.next();
            for (int i = 0; i < layoutItem.getChildCount(); i++) {
                RotateImageButton button = (RotateImageButton) layoutItem.getChildAt(i);
                if (button != null) {
                    button.setSelected(false);
                    button.setPressed(false);
                }
            }
        }
    }

    public void setRotateDegree(int degree, boolean animation) {
        Iterator it = this.mListItems.iterator();
        while (it.hasNext()) {
            LinearLayout layoutItem = (LinearLayout) it.next();
            for (int i = 0; i < layoutItem.getChildCount(); i++) {
                RotateImageButton button = (RotateImageButton) layoutItem.getChildAt(i);
                if (button != null) {
                    button.setDegree(degree, animation);
                }
            }
        }
    }

    public void showConeViewMode(boolean show) {
        if (this.mConeViewModeItemLayout != null) {
            this.mConeViewModeItemLayout.setVisibility(show ? 0 : 4);
        }
    }

    public void onPause() {
        if (this.mConeViewModeItemLayout != null) {
            this.mConeViewModeItemLayout.removeAllViews();
            this.mConeViewModeItemLayout = null;
        }
        boolean isSaveLastCameraMode = true;
        if (AppControlUtil.isChangedViewModeForConeUIByPhoneState(this.mGet.getAppContext())) {
            isSaveLastCameraMode = false;
        }
        if (isSaveLastCameraMode) {
            SharedPreferenceUtilBase.saveLastCameraMode(this.mGet.getAppContext(), this.mCurrentConeMode);
            this.mGet.saveStartingWindowLayout(this.mCurrentConeMode);
        }
        AppControlUtil.setViewModeChangedForConeUIByPhoneState(false, this.mGet.getAppContext());
    }

    public int[] getConeScreenSize() {
        int[] coneSize = new int[]{0, 0};
        if (this.mConeViewModeItemLayout == null) {
            return coneSize;
        }
        return new int[]{this.mConeViewModeItemLayout.getWidth(), this.mConeViewModeItemLayout.getHeight()};
    }

    public void onDestroy() {
        if (this.mBtnList != null) {
            this.mBtnList.clear();
            this.mBtnList = null;
        }
        if (this.mListItems != null) {
            this.mListItems.clear();
            this.mListItems = null;
        }
        if (this.mModeListItems != null) {
            this.mModeListItems.clear();
            this.mModeListItems = null;
        }
        this.mNormalAutoModeBtn = null;
        this.mNormalManualModeBtn = null;
        this.mNormalManualVideoModeBtn = null;
        this.mSquareModeBtn = null;
    }
}
