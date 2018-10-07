package com.lge.camera.managers;

import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.QuickButton;
import com.lge.camera.components.QuickButtonType;
import com.lge.camera.components.QuickButtonTypeCinemaFilter;
import com.lge.camera.components.QuickButtonTypeColorEffect;
import com.lge.camera.components.QuickButtonTypeDualPopType;
import com.lge.camera.components.QuickButtonTypeFilmEmulator;
import com.lge.camera.components.QuickButtonTypeFlash;
import com.lge.camera.components.QuickButtonTypeFrontFlash;
import com.lge.camera.components.QuickButtonTypeLightFrame;
import com.lge.camera.components.QuickButtonTypeManualCameraFlash;
import com.lge.camera.components.QuickButtonTypeManualVideoFlash;
import com.lge.camera.components.QuickButtonTypeMode;
import com.lge.camera.components.QuickButtonTypeMultiviewInterval;
import com.lge.camera.components.QuickButtonTypeRecordingFlash;
import com.lge.camera.components.QuickButtonTypeSettingExpand;
import com.lge.camera.components.QuickButtonTypeSpliceInterval;
import com.lge.camera.components.QuickButtonTypeSwapFront;
import com.lge.camera.components.QuickButtonTypeSwapFrontFocusable;
import com.lge.camera.components.QuickButtonTypeSwapRear;
import com.lge.camera.components.QuickButtonTypeSwapRearFocusable;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.Iterator;

public class QuickButtonManagerBase extends ManagerInterfaceImpl {
    protected static final SparseBooleanArray sPrevButtonStatus = new SparseBooleanArray();
    protected final ArrayList<QuickButton> mButtonList = new ArrayList();
    protected final ArrayList<QuickButton> mButtonListBackup = new ArrayList();
    protected final SparseIntArray mButtonListInitEnabled = new SparseIntArray();
    private boolean mIsOnAnimation = false;
    protected int mPresetTypeBackup = 0;
    protected int mPresetTypeCurrent = 0;
    protected ArrayList<QuickButtonType> mTypeList = new ArrayList();

    public QuickButtonManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    protected QuickButton getButton(int id) {
        if (this.mButtonList != null) {
            Iterator it = this.mButtonList.iterator();
            while (it.hasNext()) {
                QuickButton button = (QuickButton) it.next();
                if (button.getId() == id) {
                    return button;
                }
            }
        }
        return null;
    }

    private QuickButtonType getButtonType(int id) {
        Iterator it = this.mTypeList.iterator();
        while (it.hasNext()) {
            QuickButtonType buttonType = (QuickButtonType) it.next();
            if (buttonType.mId == id) {
                return buttonType;
            }
        }
        return null;
    }

    private void setRecordingLayout(LayoutParams rlp, boolean isLand) {
        if (rlp != null) {
            if (isLand) {
                rlp.addRule(12, 1);
                rlp.setMarginStart(0);
                rlp.bottomMargin = 0;
                return;
            }
            rlp.addRule(20, 1);
            rlp.setMarginStart(0);
            rlp.topMargin = 0;
        }
    }

    private void setKeyPadLayout(LayoutParams rlp, boolean isLand) {
        rlp.addRule(isLand ? 21 : 20);
        rlp.addRule(12);
        int marginBottom = Utils.getPx(getAppContext(), C0088R.dimen.quick_button_caf_marginBottom);
        int marginEnd = RatioCalcUtil.getCommandBottomMargin(getAppContext()) + 2;
        if (isLand) {
            rlp.bottomMargin = marginBottom;
            rlp.setMarginEnd(marginEnd);
            return;
        }
        rlp.bottomMargin = marginEnd;
        rlp.setMarginStart(marginBottom);
    }

    protected void setLayout(int type) {
        int i = 10;
        LinearLayout layout = (LinearLayout) this.mGet.findViewById(C0088R.id.quick_button_layout);
        if (layout != null) {
            LayoutParams rlp = (LayoutParams) layout.getLayoutParams();
            Utils.resetLayoutParameter(rlp);
            rlp.setLayoutDirection(0);
            boolean isLand = Utils.isConfigureLandscape(this.mGet.getActivity().getResources());
            rlp.setMarginStart(0);
            rlp.topMargin = 0;
            rlp.bottomMargin = 0;
            switch (type) {
                case 4:
                case 5:
                    setRecordingLayout(rlp, isLand);
                    break;
                case 6:
                    setKeyPadLayout(rlp, isLand);
                    break;
                default:
                    int i2;
                    if (isLand) {
                        i2 = 20;
                    } else {
                        i2 = 10;
                    }
                    rlp.addRule(i2);
                    if (!isLand) {
                        i = 20;
                    }
                    rlp.addRule(i);
                    break;
            }
            layout.setLayoutParams(rlp);
            this.mPresetTypeCurrent = type;
        }
    }

    protected void setPreviewManualTypeList(boolean enable, int supportedButtons) {
        if (!this.mGet.isConfigChanging()) {
            this.mTypeList.clear();
            this.mTypeList.add(new QuickButtonTypeSettingExpand(enable));
            this.mTypeList.add(new QuickButtonTypeMode(enable));
            if (FunctionProperties.isSupportedFilmEmulator()) {
                this.mTypeList.add(new QuickButtonTypeFilmEmulator(enable));
            } else if (this.mGet.isColorEffectSupported()) {
                this.mTypeList.add(new QuickButtonTypeColorEffect(enable));
            }
            this.mTypeList.add(new QuickButtonTypeSwapRear(enable));
            if ((supportedButtons & 2) != 2) {
                int size = getAppContext().getDrawable(C0088R.drawable.btn_quickbutton_flash_auto_normal).getIntrinsicWidth();
                if (ManualUtil.isManualCameraMode(this.mGet.getShotMode())) {
                    Object quickButtonTypeManualCameraFlash;
                    ArrayList arrayList = this.mTypeList;
                    if (FunctionProperties.isSupportedRearCurtainFlash()) {
                        quickButtonTypeManualCameraFlash = new QuickButtonTypeManualCameraFlash(enable, size, size);
                    } else {
                        quickButtonTypeManualCameraFlash = new QuickButtonTypeFlash(enable, size, size);
                    }
                    arrayList.add(quickButtonTypeManualCameraFlash);
                    return;
                }
                this.mTypeList.add(new QuickButtonTypeManualVideoFlash(enable, size, size));
            }
        }
    }

    protected void setPreviewManualVideoTypeList(boolean enable) {
        if (!this.mGet.isConfigChanging()) {
            this.mTypeList.clear();
            this.mTypeList.add(new QuickButtonTypeSettingExpand(enable));
            this.mTypeList.add(new QuickButtonTypeMode(enable));
            if (FunctionProperties.isSupportedFilmEmulator()) {
                this.mTypeList.add(new QuickButtonTypeFilmEmulator(enable));
            } else if (this.mGet.isColorEffectSupported()) {
                this.mTypeList.add(new QuickButtonTypeColorEffect(enable));
            }
            int size = getAppContext().getDrawable(C0088R.drawable.btn_quickbutton_flash_auto_normal).getIntrinsicWidth();
            this.mTypeList.add(new QuickButtonTypeSwapRear(enable));
            this.mTypeList.add(new QuickButtonTypeManualVideoFlash(enable, size, size));
        }
    }

    protected void setPreviewIncludeBackKeyRearTypeList(boolean enable, int supportedButtons) {
        if (!this.mGet.isConfigChanging()) {
            this.mTypeList.clear();
            this.mTypeList.add(new QuickButtonTypeSettingExpand(enable));
            addOptionalPresetQuickButtons(enable, supportedButtons, true);
        }
    }

    protected void setPreviewIncludeBackKeyFrontTypeList(boolean enable, int supportedButtons) {
        if (!this.mGet.isConfigChanging()) {
            this.mTypeList.clear();
            this.mTypeList.add(new QuickButtonTypeSettingExpand(enable));
            addOptionalPresetQuickButtons(enable, supportedButtons, false);
        }
    }

    protected void setPreviewKeypadList(boolean enable, int supportedButtons) {
        if (!this.mGet.isConfigChanging()) {
            this.mTypeList.clear();
            if ((supportedButtons & 1) == 1) {
                return;
            }
            if (this.mGet.isRearCamera()) {
                this.mTypeList.add(new QuickButtonTypeSwapRearFocusable(enable));
            } else {
                this.mTypeList.add(new QuickButtonTypeSwapFrontFocusable(enable));
            }
        }
    }

    protected void setPreviewMultiviewTypeList(boolean enable, int supportedButtons) {
        if (!this.mGet.isConfigChanging()) {
            this.mTypeList.clear();
            this.mTypeList.add(new QuickButtonTypeSettingExpand(enable));
            this.mTypeList.add(new QuickButtonTypeMode(enable));
            if (FunctionProperties.isSupportedFilmEmulator()) {
                this.mTypeList.add(new QuickButtonTypeFilmEmulator(enable));
            } else if (this.mGet.isColorEffectSupported()) {
                this.mTypeList.add(new QuickButtonTypeColorEffect(enable));
            }
            if (CameraConstants.MODE_SQUARE_SPLICE.equals(this.mGet.getShotMode())) {
                this.mTypeList.add(new QuickButtonTypeSpliceInterval(enable));
            } else {
                this.mTypeList.add(new QuickButtonTypeMultiviewInterval(enable));
            }
            addOptinalForMultiView(enable, supportedButtons);
        }
    }

    protected void setPreviewSnapMovieTypeList(boolean enable, int supportedButtons) {
        if (!this.mGet.isConfigChanging()) {
            this.mTypeList.clear();
            this.mTypeList.add(new QuickButtonTypeSettingExpand(enable));
            this.mTypeList.add(new QuickButtonTypeMode(enable));
            if (FunctionProperties.isSupportedFilmEmulator() && this.mGet.isRearCamera()) {
                this.mTypeList.add(new QuickButtonTypeFilmEmulator(enable));
            } else if (this.mGet.isColorEffectSupported()) {
                this.mTypeList.add(new QuickButtonTypeColorEffect(enable));
            }
            addSwapButtonForSnapMode(enable, supportedButtons);
            addOptinalForMultiView(enable, supportedButtons);
        }
    }

    private void addSwapButtonForSnapMode(boolean enable, int supportedButtons) {
        if ((supportedButtons & 1) != 1) {
            boolean enableSwap = this.mGet.getSettingIndex(Setting.KEY_MULTIVIEW_LAYOUT) <= 0 ? enable : false;
            if (this.mGet.isRearCamera()) {
                this.mTypeList.add(new QuickButtonTypeSwapRear(enableSwap));
            } else {
                this.mTypeList.add(new QuickButtonTypeSwapFront(enableSwap));
            }
        }
    }

    private void addOptinalForMultiView(boolean enable, int supportedButtons) {
        int size = getAppContext().getDrawable(C0088R.drawable.btn_quickbutton_flash_auto_normal).getIntrinsicWidth();
        if (!this.mGet.isRearCamera()) {
            this.mTypeList.add(new QuickButtonTypeLightFrame(false, size, size));
        } else if ((supportedButtons & 2) != 2) {
            this.mTypeList.add(new QuickButtonTypeManualVideoFlash(false, size, size));
        }
        int multiViewLayout = this.mGet.getSettingIndex(Setting.KEY_MULTIVIEW_LAYOUT);
        CamLog.m3d(CameraConstants.TAG, "-multiview- setButtonIndex index = " + multiViewLayout);
        setButtonIndex(C0088R.id.quick_button_multi_view_layout, multiViewLayout);
    }

    private void addOptionalPresetQuickButtons(boolean enable, int supportedButtons, boolean isMainCam) {
        if ((supportedButtons & 23) != 23) {
            if ((supportedButtons & 4) != 4) {
                this.mTypeList.add(new QuickButtonTypeMode(enable));
            }
            if (CameraConstants.MODE_CINEMA.equals(this.mGet.getShotMode())) {
                this.mTypeList.add(new QuickButtonTypeCinemaFilter(enable));
            } else if (FunctionProperties.isSupportedMode(CameraConstants.MODE_DUAL_POP_CAMERA) && (CameraConstants.MODE_DUAL_POP_CAMERA.equals(this.mGet.getShotMode()) || CameraConstants.MODE_POPOUT_CAMERA.equals(this.mGet.getShotMode()))) {
                this.mTypeList.add(new QuickButtonTypeDualPopType(enable));
            } else if (FunctionProperties.isSupportedFilmEmulator() && this.mGet.isRearCamera()) {
                this.mTypeList.add(new QuickButtonTypeFilmEmulator(enable));
            } else if (this.mGet.isColorEffectSupported() && this.mGet.isRearCamera()) {
                this.mTypeList.add(new QuickButtonTypeColorEffect(enable));
            }
            if ((supportedButtons & 1) != 1) {
                if (this.mGet.isRearCamera()) {
                    this.mTypeList.add(new QuickButtonTypeSwapRear(enable));
                } else {
                    this.mTypeList.add(new QuickButtonTypeSwapFront(enable));
                }
            }
            addOptionalPresetFlashQuickButtons(enable, supportedButtons, isMainCam);
        }
    }

    private void addOptionalPresetFlashQuickButtons(boolean enable, int supportedButtons, boolean isMainCam) {
        if ((supportedButtons & 2) != 2) {
            int size = getAppContext().getDrawable(C0088R.drawable.btn_quickbutton_flash_auto_normal).getIntrinsicWidth();
            if (!isMainCam && (supportedButtons & 8) != 8) {
                this.mTypeList.add(new QuickButtonTypeLightFrame(enable, size, size));
            } else if (this.mGet.isRecordingPriorityMode()) {
                this.mTypeList.add(new QuickButtonTypeManualVideoFlash(enable, size, size));
            } else {
                this.mTypeList.add(isMainCam ? new QuickButtonTypeFlash(enable, size, size) : new QuickButtonTypeFrontFlash(enable, size, size));
            }
        }
    }

    protected void setRecordingFrontTypeList(boolean enable, int supportedButtons) {
        if (!this.mGet.isConfigChanging()) {
            this.mTypeList.clear();
            if ((supportedButtons & 8) == 8) {
                int size = getAppContext().getDrawable(C0088R.drawable.btn_quickbutton_flash_auto_normal).getIntrinsicWidth();
                this.mTypeList.add(new QuickButtonTypeRecordingFlash(enable, size, size));
            }
        }
    }

    protected void addButtons(boolean restore, boolean visible) {
        if (this.mGet.isConfigChanging() || restore) {
            clearButtonListAndRestore();
        } else if (this.mButtonList != null) {
            this.mButtonList.clear();
        }
        addButtonList();
        if (!restore) {
            if (visible) {
                show(false, false, true);
            } else {
                hide(false, false, true);
            }
        }
    }

    private void clearButtonListAndRestore() {
        if (this.mTypeList != null && this.mButtonList != null) {
            this.mTypeList.clear();
            for (int i = 0; i < this.mButtonList.size(); i++) {
                QuickButton button = (QuickButton) this.mButtonList.get(i);
                int curDrawableIndex = button.getIndex();
                updateButtonBySetting(button);
                int size = button.getWidth();
                if (size == 0) {
                    size = -2;
                    if (button.getAnimationDrawableIds() != null) {
                        size = getAppContext().getDrawable(C0088R.drawable.btn_quickbutton_flash_auto_normal).getIntrinsicWidth();
                    }
                }
                ArrayList arrayList = this.mTypeList;
                arrayList.add(new QuickButtonType(button.getId(), button.getKey(), size, size, button.isClickable(), button.isFocusable(), button.getContentDescriptionStringId(), button.isEnabled(), button.getDrawableIds(), button.getClickEventMessages(), curDrawableIndex, button.getBackground(), button.getVisibility(), button.getSelectedDrawableId(), button.getSetDisableColorFilter(), button.getStringIds(), button.getAnimationDrawableIds()));
            }
            this.mButtonList.clear();
        }
    }

    protected void updateButtonBySetting(QuickButton button) {
        if (button != null) {
            String key = button.getKey();
            if ("".equals(key)) {
                setEnable(button.getId(), true, true);
                return;
            }
            int curDrawableIndex = this.mGet.getSettingIndex(key);
            if (this.mGet.checkModuleValidate(8) && button.getIndex() != curDrawableIndex) {
                button.setColorFilter(ColorUtil.getNormalColorByAlpha());
                button.setEnabled(true);
            }
            int index = curDrawableIndex;
            if (Setting.KEY_FILM_EMULATOR.equals(key) || Setting.KEY_COLOR_EFFECT.equals(key)) {
                index = 0;
            }
            button.setIndex(index);
            setEnable(button.getId(), this.mGet.getSettingMenuEnable(key), true);
        }
    }

    public boolean checkButtonClickable(QuickButton button) {
        if ("flash-mode".equals(button.getKey())) {
            if (this.mGet.checkFocusStateForChangingSetting()) {
                int disableFeature = this.mGet.checkFeatureDisableBatteryLevel(1, true);
                if (disableFeature != 0) {
                    String messageId;
                    if (disableFeature == 1) {
                        messageId = this.mGet.getAppContext().getString(C0088R.string.msg_battery_too_low_to_use_function_flash);
                    } else {
                        messageId = this.mGet.getAppContext().getString(C0088R.string.msg_voltage_too_low_to_use_function_flash);
                    }
                    this.mGet.showToast(messageId, CameraConstants.TOAST_LENGTH_SHORT);
                    CamLog.m3d(CameraConstants.TAG, "Battery's too low to use flash, return");
                    return false;
                } else if (this.mGet.checkCameraChanging(1)) {
                    CamLog.m3d(CameraConstants.TAG, "Camera is switching, return");
                    return false;
                }
            }
            CamLog.m3d(CameraConstants.TAG, "checkFocusStateForChangingSetting() is false, return");
            return false;
        }
        return checkCommonButtonState(button.getId());
    }

    public boolean checkCommonButtonState(int id) {
        if (!this.mGet.checkQuickButtonAvailable()) {
            return false;
        }
        if ((id == C0088R.id.quick_button_film_emulator && (this.mGet.getFilmState() == 1 || this.mGet.getFilmState() == 5 || this.mGet.getCameraState() <= 0 || !this.mGet.checkModuleValidate(48))) || !this.mGet.checkFocusStateForChangingSetting() || this.mGet.isVolumeKeyPressed() || this.mGet.isGIFEncoding()) {
            CamLog.m3d(CameraConstants.TAG, "Film engine is releasing or preview state is unavailable, return");
            return false;
        } else if (!this.mGet.isQuickViewAniStarted()) {
            return true;
        } else {
            CamLog.m3d(CameraConstants.TAG, "Quick view started, return");
            return false;
        }
    }

    private void setButtonListEnable(ArrayList<QuickButton> buttonList, boolean enabled, boolean changeColor) {
        if (buttonList != null) {
            Iterator it = buttonList.iterator();
            while (it.hasNext()) {
                setButtonEnabled((QuickButton) it.next(), enabled, changeColor);
            }
        }
    }

    protected void setButtonEnabled(QuickButton button, boolean enabled, boolean changeColor) {
        if (button != null) {
            if (changeColor) {
                if (enabled) {
                    button.setColorFilter(ColorUtil.getNormalColorByAlpha());
                } else {
                    button.setColorFilter(ColorUtil.getDimColorByAlpha());
                }
            }
            if (enabled != button.isEnabled()) {
                button.setEnabled(enabled);
            }
        }
    }

    public void setEnable(int id, boolean enabled) {
        setEnable(id, enabled, false);
    }

    public void setEnable(int id, boolean enabled, boolean changeColor) {
        if (!this.mGet.checkModuleValidate(8)) {
            this.mButtonListInitEnabled.put(id, enabled ? 1 : -1);
            CamLog.m3d(CameraConstants.TAG, "Exit enable because camera is switching");
        } else if (id == 100) {
            setButtonListEnable(this.mButtonList, enabled, changeColor);
        } else {
            QuickButton button = getButton(id);
            if (button != null) {
                setButtonEnabled(button, enabled, changeColor);
            }
            QuickButtonType buttonType = getButtonType(id);
            if (buttonType != null) {
                buttonType.mEnable = enabled;
            }
        }
    }

    public void setPressed(int id, boolean pressed) {
        if (id == 100) {
            Iterator it = this.mButtonList.iterator();
            while (it.hasNext()) {
                ((QuickButton) it.next()).setPressed(pressed);
            }
            return;
        }
        QuickButton button = getButton(id);
        if (button != null) {
            button.setPressed(pressed);
        }
    }

    public void setSelected(int id, boolean selected) {
        if (id == 100) {
            Iterator it = this.mButtonList.iterator();
            while (it.hasNext()) {
                ((QuickButton) it.next()).setSelected(selected);
            }
            return;
        }
        QuickButton button = getButton(id);
        if (button != null) {
            button.setSelected(selected);
        }
    }

    public void refreshButtonEnable(int id, boolean enabled, boolean colorChanged) {
        if (id != 100) {
            QuickButton q = getButton(id);
            if (q != null) {
                refreshButton(q, getButtonType(id), enabled, colorChanged);
            }
        } else if (this.mButtonList.size() != 0) {
            for (int i = 0; i < this.mTypeList.size(); i++) {
                refreshButton((QuickButton) this.mButtonList.get(i), (QuickButtonType) this.mTypeList.get(i), enabled, colorChanged);
            }
        }
    }

    private void refreshButton(QuickButton button, QuickButtonType buttonType, boolean enabled, boolean colorChanged) {
        if (button != null && buttonType != null) {
            setButtonEnabled(button, !enabled ? false : buttonType.mEnable, colorChanged);
        }
    }

    public void setButtonIndex(int id, int index) {
        if (id == 100) {
            Iterator it = this.mButtonList.iterator();
            while (it.hasNext()) {
                ((QuickButton) it.next()).setIndex(index);
            }
            return;
        }
        QuickButton button = getButton(id);
        if (button != null) {
            button.setIndex(index);
        }
    }

    public boolean isOnAnimation() {
        return this.mIsOnAnimation;
    }

    private void setAlphaAnimation(final View view, final float start, float end, long duration) {
        if (view != null) {
            view.setVisibility(4);
            AnimationUtil.startAlphaAnimation(view, start, end, duration, new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    QuickButtonManagerBase.this.mIsOnAnimation = true;
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    if (view != null) {
                        view.clearAnimation();
                        view.setVisibility(Float.compare(start, 0.0f) == 0 ? 0 : 8);
                        QuickButtonManagerBase.this.mIsOnAnimation = false;
                    }
                }
            });
        }
    }

    private void setButtonAllVisibility(int visible) {
        if (this.mButtonList != null) {
            Iterator it = this.mButtonList.iterator();
            while (it.hasNext()) {
                ((QuickButton) it.next()).setVisibility(visible);
            }
        }
    }

    private void setLayoutVisibility(boolean ani, View view, float start, float end, int visibility) {
        if (ani) {
            setAlphaAnimation(view, start, end, 200);
        } else {
            view.setVisibility(visibility);
        }
    }

    public void setVisibility(int id, int visibility, boolean animation) {
        View controlsView = this.mGet.findViewById(C0088R.id.camera_controls);
        View quickButtonLayout = this.mGet.findViewById(C0088R.id.quick_button_layout);
        View view = id == 100 ? quickButtonLayout : getButton(id);
        if (controlsView == null || quickButtonLayout == null || view == null) {
            CamLog.m3d(CameraConstants.TAG, "cannot find view");
            return;
        }
        view.clearAnimation();
        if (visibility == 0) {
            if (id == 100) {
                setButtonAllVisibility(visibility);
            } else if (quickButtonLayout.getVisibility() != 0) {
                quickButtonLayout.setVisibility(visibility);
            }
            if (view.getVisibility() != 0) {
                setLayoutVisibility(animation, view, 0.0f, 1.0f, visibility);
                return;
            }
            return;
        }
        setLayoutVisibility(animation, view, 1.0f, 0.0f, visibility);
    }

    public void show(boolean needAlpha, boolean needTrans, boolean showCleanView) {
        boolean showFromLeft = false;
        setVisibility(100, 0, needAlpha);
        if (needTrans) {
            if (this.mPresetTypeCurrent != 6) {
                showFromLeft = true;
            }
            AnimationUtil.startTransAnimation(this.mGet.findViewById(C0088R.id.quick_button_layout), true, Utils.isConfigureLandscape(this.mGet.getAppContext().getResources()), null, showFromLeft);
        }
    }

    public void hide(boolean needAlpha, boolean needTrans, boolean showCleanView) {
        setVisibility(100, 4, needAlpha);
        if (needTrans) {
            AnimationUtil.startTransAnimation(this.mGet.findViewById(C0088R.id.quick_button_layout), false, Utils.isConfigureLandscape(this.mGet.getAppContext().getResources()), null, this.mPresetTypeCurrent != 6);
        }
    }

    public boolean isButtonListEmpty() {
        return this.mButtonList.size() <= 0;
    }

    public void backup() {
        this.mPresetTypeBackup = this.mPresetTypeCurrent;
        this.mButtonListBackup.clear();
        Iterator it = this.mButtonList.iterator();
        while (it.hasNext()) {
            this.mButtonListBackup.add((QuickButton) it.next());
        }
    }

    public void restore(boolean visible) {
        this.mButtonList.clear();
        Iterator it = this.mButtonListBackup.iterator();
        while (it.hasNext()) {
            this.mButtonList.add((QuickButton) it.next());
        }
        setLayout(this.mPresetTypeBackup);
        addButtons(true, visible);
        setButtonDegree(getOrientationDegree(), false);
    }

    protected void setButtonDegree(int degree, boolean animation) {
        Iterator it = this.mButtonList.iterator();
        while (it.hasNext()) {
            QuickButton button = (QuickButton) it.next();
            if (button.getId() != C0088R.id.quick_button_multi_view_layout) {
                button.setDegree(degree, animation);
            }
        }
    }

    public void setRotateDegree(int degree, boolean animation) {
        if (this.mButtonList != null && this.mButtonList.size() != 0) {
            setButtonDegree(degree, animation);
            setRecordingRotateLayoutMargin(degree);
        }
    }

    public void setRecordingRotateLayoutMargin(int degree) {
        if (!this.mGet.checkModuleValidate(128)) {
            LinearLayout layout = (LinearLayout) this.mGet.findViewById(C0088R.id.quick_button_layout);
            if (layout != null) {
                LayoutParams rlp = (LayoutParams) layout.getLayoutParams();
                if (ModelProperties.isLongLCDModel()) {
                    int[] size = Utils.sizeStringToArray(this.mGet.getSettingValue(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId())));
                    int topMargin = RatioCalcUtil.getLongLCDModelTopMargin(getAppContext(), size[0], size[1], 0);
                    int quickbuttonHeight = getAppContext().getDrawable(C0088R.drawable.btn_quickbutton_flash_on_normal).getIntrinsicHeight();
                    if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                        rlp.topMargin = 0;
                    } else {
                        if (topMargin != 0) {
                            rlp.topMargin = topMargin - ((topMargin - quickbuttonHeight) / 2);
                        }
                        if (ModelProperties.getLCDType() == 2 && topMargin == RatioCalcUtil.getNotchDisplayHeight(getAppContext())) {
                            rlp.topMargin = 0;
                        }
                    }
                    CamLog.m3d(CameraConstants.TAG, "[QFL] topMargin : " + topMargin);
                } else {
                    rlp.topMargin = 0;
                    rlp.setMarginStart(0);
                }
                layout.setLayoutParams(rlp);
            }
        }
    }

    protected void addButtonList() {
    }

    public void onDestroy() {
        if (this.mTypeList != null) {
            this.mTypeList.clear();
        }
        if (this.mButtonList != null) {
            this.mButtonList.clear();
        }
        if (this.mButtonListBackup != null) {
            this.mButtonListBackup.clear();
        }
    }

    public void updateButtonIcon(int id, int[] values, int selectedDrawableId) {
        QuickButton button = null;
        for (int i = 0; i < this.mButtonList.size(); i++) {
            if (((QuickButton) this.mButtonList.get(i)).getId() == id) {
                button = (QuickButton) this.mButtonList.get(i);
                break;
            }
        }
        if (button != null) {
            button.setDrawableId(values);
            button.setSelectedDrawableId(selectedDrawableId);
        }
    }
}
