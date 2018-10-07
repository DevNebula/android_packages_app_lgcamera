package com.lge.camera.settings;

import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.QuickButton;
import com.lge.camera.components.QuickButtonText;
import com.lge.camera.components.QuickButtonType;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.SettingListView;
import com.lge.camera.components.SettingListView.SettingListViewInterface;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class SettingIntegration extends SettingIntegrationBase implements OnRemoveHandler {
    public HandlerRunnable mDisplaySettingMenu = new HandlerRunnable(this) {
        public void handleRun() {
            SettingIntegration.this.displaySettingView(false);
        }
    };
    public HandlerRunnable mRemoveSettingMenu = new HandlerRunnable(this) {
        public void handleRun() {
            SettingIntegration.this.removeSettingView();
        }
    };

    /* renamed from: com.lge.camera.settings.SettingIntegration$3 */
    class C13993 implements OnScrollListener {
        C13993() {
        }

        public void onScrollStateChanged(AbsListView arg0, int arg1) {
            SettingIntegration.this.mScrollState = arg1;
        }

        public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
        }
    }

    /* renamed from: com.lge.camera.settings.SettingIntegration$4 */
    class C14004 implements SettingListViewInterface {
        C14004() {
        }

        public void onSettingMenuHide() {
        }

        public int getDegree() {
            return SettingIntegration.this.mGet.getOrientationDegree();
        }
    }

    /* renamed from: com.lge.camera.settings.SettingIntegration$5 */
    class C14015 implements OnTouchListener {
        C14015() {
        }

        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == 1) {
                SettingIntegration.this.removeChildSettingViewWithDelay(true, 0);
            }
            return true;
        }
    }

    /* renamed from: com.lge.camera.settings.SettingIntegration$6 */
    class C14026 implements OnTouchListener {
        C14026() {
        }

        public boolean onTouch(View arg0, MotionEvent arg1) {
            return true;
        }
    }

    /* renamed from: com.lge.camera.settings.SettingIntegration$7 */
    class C14037 implements OnClickListener {
        C14037() {
        }

        public void onClick(View v) {
            SettingIntegration.this.onClickQuickSetting(v);
        }
    }

    /* renamed from: com.lge.camera.settings.SettingIntegration$9 */
    class C14079 implements AnimationListener {
        C14079() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            SettingIntegration.this.mGet.onRemoveSettingEnd();
            SettingIntegration.this.mGet.postOnUiThread(new HandlerRunnable(SettingIntegration.this) {
                public void handleRun() {
                    SettingIntegration.this.removeSettingViewAll();
                }
            }, 0);
        }
    }

    public SettingIntegration(SettingInterface setting) {
        super(setting);
    }

    public void initSettingOrder() {
        initRearSettingOrder();
        initFrontSettingOrder();
        initQuickSettingOrder();
    }

    public void displaySettingView(boolean direct) {
        if (this.mSettingViewRemoving) {
            this.mSettingMenuOpening = false;
            this.mGet.removePostRunnable(this.mDisplaySettingMenu);
            this.mGet.postOnUiThread(this.mDisplaySettingMenu, 300);
            CamLog.m3d(CameraConstants.TAG, "settingView : displaySettingView - return, send command");
            return;
        }
        this.mSettingMenuOpening = true;
        CamLog.m3d(CameraConstants.TAG, "settingView : displaySettingView, direct : " + direct);
        this.mDegree = this.mGet.getOrientationDegree();
        if (this.mSettingView == null) {
            this.mSettingView = this.mGet.getActivity().getLayoutInflater().inflate(C0088R.layout.setting_integrate_view, null);
            this.mGet.inflateStub(C0088R.id.stub_setting_view);
            ViewGroup vg = (ViewGroup) this.mGet.getActivity().findViewById(C0088R.id.setting_layout);
            if (vg != null) {
                vg.addView(this.mSettingView);
            }
        }
        makeSettingItemOrder();
        initParentLayout();
        initChildLayout();
        initQuickSettingLayout();
        rotateSettingView(this.mDegree);
        showSettingView(direct);
    }

    protected void makeSettingItemOrder() {
        initSettingOrder();
        ArrayList<String> orderGuide = new ArrayList();
        orderGuide = isRearCamera() ? this.mOrderBackCamera : this.mOrderFrontCamera;
        this.mOrderCurrentSetting.clear();
        if (this.mParentMenuList.size() > 0) {
            this.mParentMenuList.clear();
        }
        HashMap<String, SettingMenuItem> sectionMap = new HashMap();
        sectionMap.put(Setting.SETTING_SECTION_GENERAL, new SettingMenuItem(-100, this.mGet.getAppContext().getString(C0088R.string.general)));
        sectionMap.put(Setting.SETTING_SECTION_MANUAL_CAMERA, new SettingMenuItem(-100, this.mGet.getAppContext().getString(C0088R.string.shortcuts_title_manual_camera)));
        sectionMap.put(Setting.SETTING_SECTION_MANUAL_VIDEO, new SettingMenuItem(-100, this.mGet.getAppContext().getString(C0088R.string.shortcuts_title_manual_video)));
        sectionMap.put(Setting.SETTING_SECTION_FUNCTION, new SettingMenuItem(-100, this.mGet.getAppContext().getString(C0088R.string.useful_features)));
        Iterator it = orderGuide.iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (getCameraSettingMenu().getMenuItem(key) != null) {
                this.mOrderCurrentSetting.add(key);
                this.mParentMenuList.add(getCameraSettingMenu().getMenuItem(key));
            } else {
                this.mParentMenuList.add(sectionMap.get(key));
            }
        }
    }

    private void initParentLayout() {
        int topMargin;
        boolean focusable = ModelProperties.isKeyPadSupported(this.mGet.getAppContext());
        this.mParentAdapter = new SettingParentAdapter(this.mGet.getActivity(), C0088R.layout.setting_parent_item_view, getCameraSettingMenu(), this.mParentMenuList, this.mSettingParentListener);
        this.mParentAdapter.setListItemDegree(this.mGet.getOrientationDegree());
        this.mParentListView = (SettingListView) this.mSettingView.findViewById(C0088R.id.setting_listview);
        this.mParentListView.setFocusable(focusable);
        this.mParentListView.setAdapter(this.mParentAdapter);
        this.mParentListView.setVisibility(0);
        this.mParentListView.setOnItemClickListener(this.mParentItemClickListener);
        this.mParentListView.setOnScrollListener(new C13993());
        this.mParentListView.setSettingListViewInterface(new C14004());
        this.mSettingParentRotateView = (RotateLayout) this.mSettingView.findViewById(C0088R.id.setting_parent_rotate_view);
        View view = this.mGet.findViewById(C0088R.id.setting_parent_view);
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        int settingMenuHeight = getListSize(false);
        if ("on".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW))) {
            topMargin = getSettingMargin();
            settingMenuHeight -= RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext()) - RatioCalcUtil.getNotchDisplayHeight(this.mGet.getAppContext());
        } else {
            topMargin = RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext());
        }
        lp.topMargin = topMargin;
        lp.height = settingMenuHeight;
        view.setLayoutParams(lp);
    }

    protected void initChildLayout() {
        this.mSettingChildPopupLayout = (LinearLayout) this.mSettingView.findViewById(C0088R.id.setting_child_popup_layout);
        this.mSettingChildRotateView = (RotateLayout) this.mSettingView.findViewById(C0088R.id.setting_child_rotate_view);
        this.mSettingChildLayout = (RelativeLayout) this.mSettingView.findViewById(C0088R.id.setting_child_view);
        this.mSettingChildLayout.setOnTouchListener(new C14015());
        this.mSettingChildLayout.setVisibility(8);
    }

    protected void initQuickSettingLayout() {
        int height;
        int topMargin;
        int[] lcdSize = Utils.getLCDsize(this.mGet.getAppContext(), true);
        int degree = this.mGet.getOrientationDegree();
        LinearLayout ll = (LinearLayout) this.mGet.findViewById(C0088R.id.setting_quicksetting_layout);
        ll.setOnTouchListener(new C14026());
        int itemSize = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.2306f);
        int itemTextSize = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.0277f);
        int textTopMaring = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.144f);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(itemSize, itemSize);
        ArrayList<QuickButtonType> quickSettingList = isRearCamera() ? this.mRearQuickSettingList : this.mFrontQuickSettingList;
        if ("on".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW))) {
            height = ((lcdSize[0] - getListSize(false)) - RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.11112f)) - RatioCalcUtil.getNotchDisplayHeight(this.mGet.getAppContext());
            if (ModelProperties.getLCDType() == 2) {
                topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0597f);
            } else {
                topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.03472f);
            }
        } else {
            if (ModelProperties.getLCDType() == 2) {
                topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.077f);
            } else if (ModelProperties.getLCDType() == 1) {
                topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0597f);
            } else {
                topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.00938f);
            }
            height = (lcdSize[0] - RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext())) - getListSize(false);
        }
        for (int i = 0; i < quickSettingList.size(); i++) {
            String key;
            QuickButtonText quickSettingItem = new QuickButtonText(this.mGet.getAppContext(), null, C0088R.style.quick_setting_text_parent);
            QuickButtonType quickSettingType = (QuickButtonType) quickSettingList.get(i);
            boolean isPictureOrVideoSetting = false;
            if (quickSettingType.mKey.equals("picture-size")) {
                key = SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), this.mGet.getCurrentCameraId());
                isPictureOrVideoSetting = true;
            } else if (quickSettingType.mKey.equals(Setting.KEY_VIDEO_RECORDSIZE)) {
                key = SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCurrentCameraId());
                isPictureOrVideoSetting = true;
                if (CameraConstants.MODE_MANUAL_VIDEO.equals(this.mGet.getShotMode())) {
                    key = Setting.KEY_MANUAL_VIDEO_SIZE;
                }
                changeQuickSettingText(quickSettingItem, key, null);
            } else {
                key = quickSettingType.mKey;
            }
            quickSettingType.mKey = key;
            quickSettingType.mEnable = getSettingMenuEnable(key);
            quickSettingItem.init(this.mGet.getAppContext(), quickSettingType);
            quickSettingItem.setTextPaddingTop(textTopMaring);
            updateButtonBySetting((QuickButton) quickSettingItem);
            quickSettingItem.setId(quickSettingType.mId);
            changeQuickSettingText(quickSettingItem, key, null);
            quickSettingItem.setTextSize(itemTextSize);
            if (isPictureOrVideoSetting) {
                quickSettingItem.setTextScaleX(1.0f);
            }
            quickSettingItem.setColorFilter(quickSettingType.mEnable ? ColorUtil.getNormalColorByAlpha() : ColorUtil.getDimColorByAlpha());
            ll.addView(quickSettingItem, buttonParams);
            quickSettingItem.setOnClickListener(new C14037());
            LinearLayout.LayoutParams quickSettingLayoutParam = (LinearLayout.LayoutParams) quickSettingItem.getLayoutParams();
            quickSettingLayoutParam.topMargin = topMargin;
            if (quickSettingList != null && quickSettingList.size() == 3) {
                int margin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.quick_setting_marginEnd);
                quickSettingLayoutParam.setMarginStart(margin);
                quickSettingLayoutParam.setMarginEnd(margin);
            }
            quickSettingItem.setLayoutParams(quickSettingLayoutParam);
            quickSettingItem.setDegree(degree, false);
        }
        LayoutParams llp = (LayoutParams) ll.getLayoutParams();
        llp.height = height;
        ll.setLayoutParams(llp);
        ll.setVisibility(0);
    }

    private void onClickQuickSetting(View v) {
        if (checkClickCondition(v)) {
            QuickButton button = (QuickButton) v;
            if (button.getDrawableIds().length > 1 && button.getContentDescriptionStringId().length > 1) {
                button.changeToNextIndex();
            }
            int msg = button.getClickEventMessages()[button.getIndex()];
            CamLog.m3d(CameraConstants.TAG, "onClickQuickSetting msg : " + msg + ", key : " + button.getKey());
            if (button.getId() == C0088R.id.quick_setting_picture_size || button.getId() == C0088R.id.quick_setting_video_size) {
                onItemClickChildPopup(button.getKey(), 0);
                return;
            }
            if (button.getId() == C0088R.id.quick_setting_full_vision) {
                onFullVisonSettingClicked(msg);
            }
            if (button.getId() == C0088R.id.quick_button_timer || button.getId() == C0088R.id.quick_setting_full_vision) {
                if (msg == 102 || msg == 116) {
                    button.setSelected(false);
                } else {
                    button.setSelected(true);
                }
            }
            Handler handler = this.mGet.getModuleHandler();
            if (handler != null) {
                handler.removeMessages(msg);
                handler.sendEmptyMessage(msg);
            }
        }
    }

    public boolean checkClickCondition(View v) {
        if (v.getId() != C0088R.id.quick_setting_full_vision) {
            return true;
        }
        if (this.mIntervalChecker == null) {
            this.mIntervalChecker = this.mGet.getTimeIntervalChecker();
            if (this.mIntervalChecker == null) {
                return false;
            }
            this.mIntervalChecker.addChecker(6, 500);
        }
        return this.mIntervalChecker.checkTimeInterval(6);
    }

    private void onFullVisonSettingClicked(int msg) {
        if (this.mSettingView != null) {
            boolean toFullVision;
            if (msg == 115) {
                toFullVision = true;
            } else {
                toFullVision = false;
            }
            ListPreference listPref = getSetting().getListPreference(SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), this.mGet.getCurrentCameraId()));
            int index = 0;
            if (listPref != null) {
                CharSequence[] entryValues = listPref.getEntryValues();
                if (toFullVision) {
                    for (int i = 0; i < entryValues.length; i++) {
                        int[] size = Utils.sizeStringToArray(entryValues[i].toString());
                        if (size[0] >= size[1] * 2) {
                            index = i;
                            break;
                        }
                    }
                    if (entryValues.length > 6) {
                        boolean isLowSize;
                        if (listPref.findIndexOfValue(listPref.getValue()) % 2 != 0) {
                            isLowSize = true;
                        } else {
                            isLowSize = false;
                        }
                        if (isLowSize) {
                            index++;
                        }
                    }
                } else {
                    index = SharedPreferenceUtil.getPictureSizeBackupIndex(this.mGet.getAppContext(), SettingKeyWrapper.getPictureSizeBackupKey(this.mGet.getCurrentCameraId()));
                }
                changeQuickSettingText((QuickButton) this.mSettingView.findViewById(C0088R.id.quick_setting_picture_size), SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), this.mGet.getCurrentCameraId()), entryValues[index].toString());
            }
            if (this.mParentAdapter != null) {
                this.mParentAdapter.setAvailableUpdate(true);
            }
        }
    }

    protected void showSettingView(boolean direct) {
        if (direct) {
            this.mSettingView.setVisibility(0);
            this.mParentListView.setVisibility(0);
            this.mSettingMenuOpening = false;
            this.mGet.onShowSettingEnd();
            return;
        }
        this.mGet.postOnUiThread(new HandlerRunnable(this) {

            /* renamed from: com.lge.camera.settings.SettingIntegration$8$1 */
            class C14041 implements AnimationListener {
                C14041() {
                }

                public void onAnimationStart(Animation arg0) {
                }

                public void onAnimationRepeat(Animation arg0) {
                }

                public void onAnimationEnd(Animation arg0) {
                    SettingIntegration.this.mSettingMenuOpening = false;
                }
            }

            public void handleRun() {
                if (SettingIntegration.this.mSettingView == null || SettingIntegration.this.mSettingViewRemoving) {
                    CamLog.m3d(CameraConstants.TAG, "settingView : showSettingView - return");
                    SettingIntegration.this.mSettingMenuOpening = false;
                    return;
                }
                SettingIntegration.this.mGet.setQuickButtonSelected(C0088R.id.quick_button_setting_expand, true);
                AnimationUtil.startShowingAnimation(SettingIntegration.this.mSettingView, true, 300, new C14041());
                SettingIntegration.this.mGet.onShowSettingEnd();
            }
        }, 0);
    }

    public void removeSettingView() {
        CamLog.m3d(CameraConstants.TAG, "settingView : removeSettingView - with animation");
        final View view = this.mGet.getActivity().findViewById(C0088R.id.setting_integrate_layout);
        if (view == null || this.mSettingViewRemoving) {
            CamLog.m3d(CameraConstants.TAG, "settingView : removeSettingView - return");
            return;
        }
        this.mSettingViewRemoving = true;
        this.mSettingMenuOpening = false;
        view.clearAnimation();
        this.mGet.setQuickButtonSelected(C0088R.id.quick_button_setting_expand, false);
        if (this.mGet.isPaused()) {
            if (isChildViewVisible()) {
                removeChildSettingView(false);
            }
            removeSettingViewAll();
            this.mGet.onRemoveSettingEnd();
            return;
        }
        if (isChildViewVisible()) {
            removeChildSettingView(false);
        }
        AnimationUtil.startShowingAnimation(this.mSettingView, false, 150, new C14079());
        view.postDelayed(new Runnable() {
            public void run() {
                view.clearAnimation();
            }
        }, 300);
    }

    public void removeSettingViewAll() {
        CamLog.m3d(CameraConstants.TAG, "settingView : removeSettingViewAll - no animation");
        if (this.mSettingView == null) {
            CamLog.m3d(CameraConstants.TAG, "settingView : removeSettingView -return, mSettingView is null");
            this.mSettingViewRemoving = false;
            return;
        }
        this.mSettingMenuOpening = false;
        if (isChildViewVisible()) {
            removeChildSettingView(false);
        }
        this.mGet.setQuickButtonSelected(C0088R.id.quick_button_setting_expand, false);
        this.mSettingView.setVisibility(4);
        releaseResources();
        if (this.mSettingChildPopupLayout != null) {
            this.mSettingChildPopupLayout = null;
        }
        if (this.mSettingChildLayout != null) {
            this.mSettingChildLayout.setOnTouchListener(null);
            this.mSettingChildLayout = null;
        }
        ViewGroup vg = (ViewGroup) this.mGet.getActivity().findViewById(C0088R.id.setting_layout);
        if (vg != null) {
            vg.removeView(this.mSettingView);
        }
        this.mSettingView = null;
        this.mDegree = -1;
        System.gc();
        this.mGet.onRemoveSettingEnd();
        this.mSettingViewRemoving = false;
    }

    public void removeChildSettingView(boolean isShowAnim) {
        removeChildSettingViewWithDelay(isShowAnim, 0);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 20 || keyCode == 19 || keyCode == 21 || keyCode == 22 || keyCode == 25 || keyCode == 24 || ((keyCode == 4 && event.getRepeatCount() > 0) || !isVisible())) {
            return false;
        }
        if (isChildViewVisible()) {
            removeChildSettingView(true);
            return true;
        }
        removeSettingView();
        return true;
    }

    public void onPause() {
        CamLog.m3d(CameraConstants.TAG, "onPause-start");
        if (isVisible()) {
            removeSettingView();
        }
        CamLog.m3d(CameraConstants.TAG, "onPause-end");
    }

    public void onDestroy() {
        CamLog.m3d(CameraConstants.TAG, "onDestroy-start");
        if (this.mRearSettingMenu != null) {
            this.mRearSettingMenu.close();
        }
        if (this.mFrontSettingMenu != null) {
            this.mFrontSettingMenu.close();
        }
        if (this.mRearSetting != null) {
            this.mRearSetting.close();
        }
        if (this.mFrontSetting != null) {
            this.mFrontSetting.close();
        }
        releaseResources();
        releaseChildMenuResource();
        CamLog.m3d(CameraConstants.TAG, "onDestroy-end");
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }
}
