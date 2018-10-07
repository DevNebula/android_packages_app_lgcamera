package com.lge.camera.managers;

import android.content.res.Configuration;
import android.graphics.drawable.Animatable2.AnimationCallback;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Space;
import com.lge.camera.C0088R;
import com.lge.camera.components.QuickButton;
import com.lge.camera.components.QuickButtonText;
import com.lge.camera.components.QuickButtonType;
import com.lge.camera.components.QuickButtonTypeCAF;
import com.lge.camera.components.QuickButtonTypeCAFFocusable;
import com.lge.camera.components.QuickButtonTypeRecordingFlash;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.TalkBackUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.Iterator;

public class QuickButtonManager extends QuickButtonManagerBase {
    private long mPrevButtonClickedTime = 0;
    private final int mQUICK_BUTTON_COUNT_FIVE = 5;
    private final int mQUICK_BUTTON_COUNT_FOUR = 4;
    private final int mQUICK_BUTTON_COUNT_THREE = 3;
    private final int mQUICK_BUTTON_COUNT_TWO = 2;
    private int mQuickButtonStartMargin = 0;

    /* renamed from: com.lge.camera.managers.QuickButtonManager$1 */
    class C11001 implements OnTouchListener {
        C11001() {
        }

        public boolean onTouch(View view, MotionEvent e) {
            if (e.getAction() == 0) {
                long curTime = System.currentTimeMillis();
                if (Math.abs(QuickButtonManager.this.mPrevButtonClickedTime - curTime) <= 500 || QuickButtonManager.this.mGet.isAnimationShowing()) {
                    return true;
                }
                QuickButtonManager.this.mGet.getBlurredBitmapForSwitchingCamera();
                QuickButtonManager.this.mPrevButtonClickedTime = curTime;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.QuickButtonManager$2 */
    class C11012 implements OnClickListener {
        C11012() {
        }

        public void onClick(View v) {
            QuickButtonManager.this.quickbuttonOnClick(v);
        }
    }

    public QuickButtonManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    protected void makeQuickButtonLayout() {
        LinearLayout quickButtonLayout = (LinearLayout) this.mGet.findViewById(C0088R.id.quick_button_layout);
        if (quickButtonLayout != null) {
            quickButtonLayout.removeAllViews();
            makeQuickButtonLayout(quickButtonLayout);
        }
    }

    private void setChildViewHeightForTilePreview(int height, int parentId) {
        ViewGroup viewGroup = (ViewGroup) this.mGet.getActivity().findViewById(parentId);
        if (viewGroup != null) {
            int itemCnt = viewGroup.getChildCount();
            for (int i = 0; i < itemCnt; i++) {
                View view = viewGroup.getChildAt(i);
                if (view != null) {
                    view.getLayoutParams().height = height;
                    view.setLayoutParams(view.getLayoutParams());
                }
            }
        }
    }

    protected void makeQuickButtonLayout(LinearLayout quickButtonLayout) {
        if (this.mButtonList != null && this.mButtonList.size() != 0) {
            int quickButtonWidth = RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext());
            int notchSize = RatioCalcUtil.getNotchDisplayHeight(getAppContext());
            quickButtonWidth -= notchSize;
            this.mQuickButtonStartMargin = calcQuickButtonStartMargin();
            View view = this.mGet.findViewById(C0088R.id.quick_button_layout_root);
            LayoutParams param = (LayoutParams) view.getLayoutParams();
            param.topMargin = notchSize;
            view.setLayoutParams(param);
            quickButtonLayout.addView(new View(getAppContext()), new LinearLayout.LayoutParams(this.mQuickButtonStartMargin, quickButtonWidth));
            int i = 0;
            while (i < this.mButtonList.size()) {
                RelativeLayout quickBtn = new RelativeLayout(getAppContext());
                MarginLayoutParams params = new MarginLayoutParams(((QuickButtonType) this.mTypeList.get(i)).mWidth, ((QuickButtonType) this.mTypeList.get(i)).mHeight);
                quickBtn.setGravity(17);
                quickBtn.addView((View) this.mButtonList.get(i), params);
                quickButtonLayout.addView(quickBtn, new LinearLayout.LayoutParams(-2, quickButtonWidth));
                Space space = new Space(getAppContext());
                space.setId(C0088R.id.qfl_space);
                LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(0, 0);
                if (!ModelProperties.isTablet(getAppContext()) && ((this.mGet.getCameraState() == 6 || this.mGet.getCameraState() == 7 || this.mGet.getCameraState() == 5) && this.mButtonList.size() == 2)) {
                    spaceParams.weight = i == 0 ? 1.0f : 12.0f;
                } else if (i < this.mButtonList.size() - 1) {
                    spaceParams.weight = 1.0f;
                }
                quickButtonLayout.addView(space, spaceParams);
                i++;
            }
            quickButtonLayout.addView(new View(getAppContext()), new LinearLayout.LayoutParams(this.mQuickButtonStartMargin, quickButtonWidth));
            if ("on".equals(this.mGet.getSettingValue(Setting.KEY_TILE_PREVIEW))) {
                setLayoutForTilePreview();
            }
        }
    }

    public void setLayoutForTilePreview() {
        View view = this.mGet.findViewById(C0088R.id.quick_button_layout_root);
        if (view != null) {
            LayoutParams param = (LayoutParams) view.getLayoutParams();
            if (param != null) {
                int topMargin;
                int height = RatioCalcUtil.getQuickButtonWidth(getAppContext());
                int notchSize = RatioCalcUtil.getNotchDisplayHeight(getAppContext());
                height -= notchSize;
                if (this.mGet.getCameraState() == 6 || this.mGet.getCameraState() == 7 || this.mGet.getCameraState() == 5 || this.mGet.getSettingValue(Setting.KEY_TILE_PREVIEW).equals("off")) {
                    topMargin = notchSize;
                } else {
                    height = RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext()) - notchSize;
                    topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.11112f) + notchSize;
                }
                param.topMargin = topMargin;
                setChildViewHeightForTilePreview(height, C0088R.id.quick_button_layout);
                setChildViewHeightForTilePreview(height, C0088R.id.quick_button_layout_root);
                view.setLayoutParams(param);
                view.refreshDrawableState();
            }
        }
    }

    protected int calcQuickButtonStartMargin() {
        if (this.mGet.getCameraState() != 6 && this.mGet.getCameraState() != 7 && this.mGet.getCameraState() != 5) {
            switch (this.mButtonList.size()) {
                case 3:
                case 4:
                    if (ModelProperties.isTablet(getAppContext())) {
                        return RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.0625f);
                    }
                    return Utils.getPx(getAppContext(), C0088R.dimen.quick_button_item_count_four_margin);
                case 5:
                    if (ModelProperties.isTablet(getAppContext())) {
                        return RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.0625f);
                    }
                    return Utils.getPx(getAppContext(), C0088R.dimen.quick_button_margin);
                default:
                    return Utils.getPx(getAppContext(), C0088R.dimen.quick_button_margin);
            }
        } else if (ModelProperties.isTablet(getAppContext())) {
            return RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.0625f);
        } else {
            return Utils.getPx(getAppContext(), C0088R.dimen.quick_button_margin);
        }
    }

    public int getQuickButtonStartMargin() {
        if (this.mQuickButtonStartMargin == 0) {
            CamLog.m3d(CameraConstants.TAG, "mQuickButtonMargin has not calculated yet");
        }
        return this.mQuickButtonStartMargin;
    }

    protected void addButtonList() {
        if (this.mButtonList != null) {
            addButtonList(this.mTypeList, this.mButtonList);
            makeQuickButtonLayout();
            this.mGet.addQuickButtonListDone();
        }
    }

    public void onConfigurationChanged(Configuration config) {
        if (!isButtonListEmpty()) {
            this.mGet.setQuickButtonByPreset(true, !this.mGet.isSettingMenuVisible());
            refreshButtonEnable(100, true, true);
        }
        super.onConfigurationChanged(config);
    }

    public void onPauseBefore() {
        if (this.mGet.isPaused()) {
            backupPreviousButtonStatus();
        }
        setSelected(100, false);
        setEnable(100, true);
        Iterator it = this.mButtonList.iterator();
        while (it.hasNext()) {
            ((QuickButton) it.next()).setOnClickListener(null);
        }
        this.mButtonList.clear();
        super.onPauseBefore();
    }

    public void onCameraSwitchingStart() {
        CamLog.m3d(CameraConstants.TAG, "Disable buttons because start changing");
        backupPreviousButtonStatus();
        setEnable(100, false);
        this.mButtonListInitEnabled.clear();
        Iterator it = this.mButtonList.iterator();
        while (it.hasNext()) {
            this.mButtonListInitEnabled.put(((QuickButton) it.next()).getId(), 0);
        }
        super.onCameraSwitchingStart();
    }

    public void onCameraSwitchingEnd() {
        CamLog.m3d(CameraConstants.TAG, "Enable buttons because end changing " + this.mButtonList.size());
        Iterator it = this.mButtonList.iterator();
        while (it.hasNext()) {
            QuickButton button = (QuickButton) it.next();
            int initValue = this.mButtonListInitEnabled.get(button.getId());
            int id = button.getId();
            boolean z = initValue == 0 || initValue == 1;
            setEnable(id, z, true);
        }
        super.onCameraSwitchingEnd();
    }

    public void updateButton(int id) {
        if (id == 100) {
            Iterator it = this.mButtonList.iterator();
            while (it.hasNext()) {
                updateButtonBySetting((QuickButton) it.next());
            }
            setButtonDegree(getOrientationDegree(), false);
            return;
        }
        QuickButton button = getButton(id);
        if (button != null) {
            updateButtonBySetting(button);
        }
    }

    public void setQuickButtons(int type, int supportedButtons, boolean enable, boolean visible) {
        setVisibility(100, 4, false);
        if (!this.mGet.checkModuleValidate(8)) {
            enable = false;
        }
        switch (type) {
            case 1:
                setPreviewIncludeBackKeyRearTypeList(enable, supportedButtons);
                break;
            case 2:
                setPreviewIncludeBackKeyFrontTypeList(enable, supportedButtons);
                break;
            case 4:
                setRecordingRearTypeList(enable, supportedButtons);
                break;
            case 5:
                setRecordingFrontTypeList(enable, supportedButtons);
                break;
            case 6:
                setPreviewKeypadList(enable, supportedButtons);
                break;
            case 7:
                setPreviewManualTypeList(enable, supportedButtons);
                break;
            case 8:
                setPreviewManualVideoTypeList(enable);
                break;
            case 9:
                setPreviewSnapMovieTypeList(enable, supportedButtons);
                break;
            case 10:
                setPreviewMultiviewTypeList(enable, supportedButtons);
                break;
        }
        setLayout(type);
        addButtons(false, visible);
        setButtonDegree(getOrientationDegree(), false);
    }

    protected void setRecordingRearTypeList(boolean enable, int supportedButtons) {
        if (!this.mGet.isConfigChanging()) {
            this.mTypeList.clear();
            if ((supportedButtons & 2) != 2) {
                int size = getAppContext().getDrawable(C0088R.drawable.btn_quickbutton_flash_auto_normal).getIntrinsicWidth();
                this.mTypeList.add(new QuickButtonTypeRecordingFlash(enable, size, size));
            }
            if (ModelProperties.isKeyPadSupported(getAppContext())) {
                this.mTypeList.add(new QuickButtonTypeCAFFocusable(enable));
            } else {
                this.mTypeList.add(new QuickButtonTypeCAF(enable));
            }
        }
    }

    protected void addButtonList(ArrayList<QuickButtonType> typeList, ArrayList<QuickButton> buttonList) {
        Iterator it = typeList.iterator();
        while (it.hasNext()) {
            QuickButton button;
            QuickButtonType type = (QuickButtonType) it.next();
            boolean prev = sPrevButtonStatus.get(type.mId, true);
            type.mEnable = prev;
            switch (type.mId) {
                case C0088R.id.quick_button_mode:
                    button = new QuickButtonText(this.mGet.getAppContext(), null, C0088R.style.quick_button_text);
                    break;
                case C0088R.id.quick_button_video_size:
                    button = new QuickButtonText(this.mGet.getAppContext(), null, C0088R.style.quick_button_text_video);
                    break;
                default:
                    button = new QuickButton(this.mGet.getAppContext());
                    break;
            }
            button.setColorFilter(prev ? ColorUtil.getNormalColorByAlpha() : ColorUtil.getDimColorByAlpha());
            button.init(this.mGet.getAppContext(), type);
            button.setQuickButtonAnimationDrawableIds(type.mAnimationDrawableIds);
            if (type.mId == C0088R.id.quick_button_film_emulator || type.mId == C0088R.id.quick_button_color_effect) {
                button.setDrawableId(getFilmQuickButtonSelector());
                button.setSelectedDrawableId(getFilmQuickButtonPressedImage());
            }
            updateButtonBySetting(button);
            if (type.mId == C0088R.id.quick_button_swap_camera) {
                button.setOnTouchListener(new C11001());
            }
            button.setOnClickListener(new C11012());
            buttonList.add(button);
        }
        sPrevButtonStatus.clear();
    }

    protected void quickbuttonOnClick(View v) {
        final QuickButton button = (QuickButton) v;
        if (button == null || !checkButtonClickable(button) || ((button.getId() == C0088R.id.quick_button_flash && !this.mGet.checkInterval(3)) || !this.mGet.checkInterval(4))) {
            CamLog.m3d(CameraConstants.TAG, "Condition for quick button operation is not satisfied, return");
        } else if (button.readyQuickButtonAnimation()) {
            button.startQuickButtonAnimation(new AnimationCallback() {
                public void onAnimationEnd(Drawable drawable) {
                    super.onAnimationEnd(drawable);
                    button.restoreQuickButtonAnimationDrwable();
                    QuickButtonManager.this.doQuickButtonClickAction(button);
                }
            });
        } else {
            doQuickButtonClickAction(button);
        }
    }

    private void doQuickButtonClickAction(QuickButton button) {
        if (button.getDrawableIds().length > 1 && button.getContentDescriptionStringId().length > 1 && button.getId() != C0088R.id.quick_button_multi_view_layout) {
            button.changeToNextIndex();
            button.setSelected(true);
        }
        if (this.mGet.getHandler() != null) {
            int msg = button.getClickEventMessages()[button.getId() != C0088R.id.quick_button_multi_view_layout ? button.getIndex() : 0];
            if (msg == 6) {
                if (!this.mGet.checkModuleValidate(208)) {
                    CamLog.m3d(CameraConstants.TAG, "-swap- swap camera exit");
                    return;
                } else if (!this.mGet.isAnimationShowing()) {
                    CamLog.m7i(CameraConstants.TAG, "-swap- swap camera");
                    this.mGet.setGestureType(2);
                    this.mGet.stopPreviewThread();
                    this.mGet.startCameraSwitchingAnimation(1);
                }
            }
            if (this.mGet.isHelpListVisible() && (msg == 32 || msg == 36)) {
                msg = 49;
            }
            this.mGet.getHandler().removeMessages(msg);
            this.mGet.getHandler().sendEmptyMessage(msg);
            CamLog.m3d(CameraConstants.TAG, "quickbuttonOnClick - send message : " + msg);
            sendLDBInentOnQuickBtn(msg);
            setTalkbackOfQuickBtnOnDoubleTap(msg);
        }
    }

    private void sendLDBInentOnQuickBtn(int quickBtnId) {
        switch (quickBtnId) {
            case 6:
                LdbUtil.sendLDBIntentForSwapCamera(this.mGet.getAppContext(), this.mGet.isRearCamera(), "QFL");
                return;
            default:
                return;
        }
    }

    private void setTalkbackOfQuickBtnOnDoubleTap(int quickBtnId) {
        switch (quickBtnId) {
            case 6:
                TalkBackUtil.setTalkbackDescOnDoubleTap(getAppContext(), getAppContext().getString(this.mGet.isRearCamera() ? C0088R.string.front_camera : C0088R.string.rear_camera));
                return;
            case 50:
            case 56:
                TalkBackUtil.setTalkbackDescOnDoubleTap(getAppContext(), getAppContext().getString(C0088R.string.flash_off));
                return;
            case 51:
            case 57:
                TalkBackUtil.setTalkbackDescOnDoubleTap(getAppContext(), getAppContext().getString(C0088R.string.flash_on));
                return;
            case 52:
                TalkBackUtil.setTalkbackDescOnDoubleTap(getAppContext(), getAppContext().getString(C0088R.string.flash_auto));
                return;
            case 53:
                TalkBackUtil.setTalkbackDescOnDoubleTap(getAppContext(), getAppContext().getString(C0088R.string.rear_flash));
                return;
            default:
                return;
        }
    }

    public int[] getFilmQuickButtonSelector() {
        int filmIconId;
        boolean stickerSupported = true;
        int[] resource = new int[2];
        if (FunctionProperties.isSupportedFilmEmulator()) {
            if (!(FunctionProperties.isSupportedSticker() && ("mode_normal".equals(this.mGet.getShotMode()) || this.mGet.getShotMode().contains(CameraConstants.MODE_BEAUTY)))) {
                stickerSupported = false;
            }
            if (CameraConstants.FILM_NONE.equals(this.mGet.getSettingValue(Setting.KEY_FILM_EMULATOR)) || "mode_food".equals(this.mGet.getShotMode())) {
                filmIconId = stickerSupported ? C0088R.drawable.btn_quickbutton_filter_sticker_off : C0088R.drawable.btn_quickbutton_filter_off;
            } else {
                filmIconId = stickerSupported ? C0088R.drawable.btn_quickbutton_filter_sticker_on : C0088R.drawable.btn_quickbutton_filter_on;
            }
            if (this.mGet.isStickerSelected()) {
                filmIconId = stickerSupported ? C0088R.drawable.btn_quickbutton_filter_sticker_on : C0088R.drawable.btn_quickbutton_filter_on;
            }
        } else {
            boolean isColorEffected;
            String curSetting = this.mGet.getSettingValue(Setting.KEY_COLOR_EFFECT);
            CamLog.m3d(CameraConstants.TAG, "[color] update color quick button : " + curSetting);
            if ("none".equals(curSetting)) {
                isColorEffected = false;
            } else {
                isColorEffected = true;
            }
            filmIconId = isColorEffected ? C0088R.drawable.btn_quickbutton_filter_on : C0088R.drawable.btn_quickbutton_filter_off;
        }
        for (int i = 0; i < resource.length; i++) {
            resource[i] = filmIconId;
        }
        return resource;
    }

    public int getFilmQuickButtonPressedImage() {
        boolean stickerSupported = FunctionProperties.isSupportedSticker() && ("mode_normal".equals(this.mGet.getShotMode()) || this.mGet.getShotMode().contains(CameraConstants.MODE_BEAUTY));
        return stickerSupported ? C0088R.drawable.btn_quickbutton_filter_sticker_pressed : C0088R.drawable.btn_quickbutton_filter_pressed;
    }

    public void backupPreviousButtonStatus() {
        if (sPrevButtonStatus.size() > 0) {
            CamLog.m7i(CameraConstants.TAG, "Already Backup");
            return;
        }
        Iterator it = this.mButtonList.iterator();
        while (it.hasNext()) {
            QuickButton button = (QuickButton) it.next();
            sPrevButtonStatus.put(button.getId(), button.isEnabled());
        }
    }
}
