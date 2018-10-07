package com.lge.camera.settings;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lge.R;
import com.lge.camera.C0088R;
import com.lge.camera.components.QuickButton;
import com.lge.camera.components.QuickButtonType;
import com.lge.camera.components.QuickButtonTypeFullVision;
import com.lge.camera.components.QuickButtonTypePictureSize;
import com.lge.camera.components.QuickButtonTypeTimer;
import com.lge.camera.components.QuickButtonTypeVideoSize;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.SettingListView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraCapabilities;
import com.lge.camera.settings.SettingParentAdapter.ItemViewHolder;
import com.lge.camera.settings.SettingParentAdapter.SettingParentInterface;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CheckStatusManager;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.StorageUtil;
import com.lge.camera.util.TimeIntervalChecker;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.Iterator;

public class SettingIntegrationBase extends SettingManager implements OnRemoveHandler {
    protected OnItemClickListener mChildItemClickListener = new C14168();
    private AnimationListener mChildPopAnimListener = new C14146();
    protected SettingChildAdapter mChildPopupAdapter = null;
    protected ListView mChildPopupListView = null;
    protected ArrayList<SettingMenuItem> mChildPopupMenuList = new ArrayList();
    protected int mDegree = -1;
    protected TimeIntervalChecker mIntervalChecker = null;
    private boolean mIsOrientationChanging = false;
    protected SettingParentAdapter mParentAdapter = null;
    protected OnItemClickListener mParentItemClickListener = new C14157();
    protected SettingListView mParentListView = null;
    protected ArrayList<SettingMenuItem> mParentMenuList = new ArrayList();
    protected int mScrollState = 0;
    protected RelativeLayout mSettingChildLayout = null;
    protected LinearLayout mSettingChildPopupLayout = null;
    protected RotateLayout mSettingChildRotateView = null;
    protected boolean mSettingMenuOpening = false;
    protected SettingParentInterface mSettingParentListener = new C14102();
    protected RotateLayout mSettingParentRotateView = null;
    protected View mSettingView = null;
    protected boolean mSettingViewRemoving = false;
    protected TextView mSubTitle = null;
    private CompoundButton mSwitchButton;

    /* renamed from: com.lge.camera.settings.SettingIntegrationBase$2 */
    class C14102 implements SettingParentInterface {
        C14102() {
        }

        public void onSwitchButtonClicked(String key, boolean isChecking, CompoundButton button, boolean isBulletDividerType) {
            int clickedType = 1;
            String value = SettingIntegrationBase.this.getSettingValue(key);
            if ((!(isChecking && "on".equals(value)) && (isChecking || !"off".equals(value))) || Setting.KEY_TAG_LOCATION.equals(key)) {
                if (Setting.KEY_TAG_LOCATION.equals(key)) {
                    boolean z;
                    if ("on".equals(value) && CheckStatusManager.isSystemSettingUseLocation(SettingIntegrationBase.this.mGet.getActivity().getContentResolver())) {
                        z = true;
                    } else {
                        z = false;
                    }
                    if (isChecking == z) {
                        return;
                    }
                }
                SettingIntegrationBase.this.mParentAdapter.setAvailableUpdate(false);
                String setValue = "on".equals(value) ? "off" : "on";
                if (!isBulletDividerType) {
                    clickedType = -1;
                }
                SettingIntegrationBase.this.mGet.childSettingMenuClicked(key, setValue, clickedType);
                SettingIntegrationBase.this.mSwitchButton = button;
            }
        }

        public void onSettingMenuHide() {
            SettingIntegrationBase.this.mGet.removeSettingMenu(false, false);
        }

        public boolean isScrollState() {
            return SettingIntegrationBase.this.mScrollState != 0;
        }

        public boolean isOrientationChanged() {
            return SettingIntegrationBase.this.mIsOrientationChanging;
        }
    }

    /* renamed from: com.lge.camera.settings.SettingIntegrationBase$3 */
    class C14113 implements ImageGetter {
        C14113() {
        }

        public Drawable getDrawable(String arg0) {
            Drawable d = SettingIntegrationBase.this.mGet.getActivity().getResources().getDrawable(C0088R.drawable.camera_guide_spannable_popup_night);
            if (d != null) {
                int textSize = (int) (((double) SettingIntegrationBase.this.mSubTitle.getTextSize()) * 1.2d);
                d.setBounds(0, 0, textSize, textSize);
            }
            return d;
        }
    }

    /* renamed from: com.lge.camera.settings.SettingIntegrationBase$5 */
    class C14135 implements OnClickListener {
        C14135() {
        }

        public void onClick(View arg0) {
            SettingIntegrationBase.this.removeChildSettingViewWithDelay(true, 0);
        }
    }

    /* renamed from: com.lge.camera.settings.SettingIntegrationBase$6 */
    class C14146 implements AnimationListener {
        C14146() {
        }

        public void onAnimationStart(Animation arg0) {
            SettingIntegrationBase.this.resizeChildListView();
        }

        public void onAnimationRepeat(Animation arg0) {
        }

        public void onAnimationEnd(Animation arg0) {
        }
    }

    /* renamed from: com.lge.camera.settings.SettingIntegrationBase$7 */
    class C14157 implements OnItemClickListener {
        C14157() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (SettingIntegrationBase.this.mParentMenuList != null && SettingIntegrationBase.this.mParentAdapter != null && SettingIntegrationBase.this.mParentListView != null && SettingIntegrationBase.this.mParentAdapter.getItem(position).isEnable() && SettingIntegrationBase.this.checkModuleValidate()) {
                SettingMenuItem menuItem = SettingIntegrationBase.this.mParentAdapter.getItem(position);
                SettingIntegrationBase.this.mParentAdapter.setAvailableUpdate(true);
                SettingIntegrationBase.this.mParentAdapter.setSelectedIndex(-1);
                String curKey = menuItem.getKey();
                String value = SettingIntegrationBase.this.getSettingValue(curKey);
                String setValue = "";
                boolean isCallChildSettingMenuClicked = true;
                CamLog.m3d(CameraConstants.TAG, "mParentItemClickListener key = " + curKey);
                if (Setting.SETTING_ITEM_HELP.equals(curKey)) {
                    SettingIntegrationBase.this.mGet.onHelpButtonClicked(C0088R.id.quick_button_setting_expand);
                    isCallChildSettingMenuClicked = false;
                } else if (menuItem.isToggleType()) {
                    setValue = "on".equals(value) ? "off" : "on";
                } else {
                    SettingIntegrationBase.this.onItemClickChildPopup(curKey, position);
                    isCallChildSettingMenuClicked = false;
                }
                SettingIntegrationBase.this.mSettingChildRotateView.rotateLayout(SettingIntegrationBase.this.mGet.getOrientationDegree());
                if (isCallChildSettingMenuClicked) {
                    SettingIntegrationBase.this.mGet.childSettingMenuClicked(curKey, setValue, 0);
                }
                if (Setting.KEY_TAG_LOCATION.equals(curKey)) {
                    ItemViewHolder holder = (ItemViewHolder) view.getTag();
                    if (holder != null && holder.mSettingOnOffSwitch != null) {
                        SettingIntegrationBase.this.mSwitchButton = holder.mSettingOnOffSwitch;
                    } else {
                        return;
                    }
                }
                SettingIntegrationBase.this.mParentAdapter.update();
            }
        }
    }

    /* renamed from: com.lge.camera.settings.SettingIntegrationBase$8 */
    class C14168 implements OnItemClickListener {
        C14168() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            SettingMenuItem item = (SettingMenuItem) SettingIntegrationBase.this.mChildPopupMenuList.get(position);
            String key = item.getKey();
            String value = item.getValue();
            CamLog.m3d(CameraConstants.TAG, "child menu clicked, position : " + position + ", key : " + key + ", value : " + value);
            SettingIntegrationBase.this.mGet.childSettingMenuClicked(key, value, -1, "picture-size".equals(key) ? 100 : 0);
            boolean useRemoveAnim = true;
            if (Setting.KEY_MANUAL_VIDEO_SIZE.equals(key) || Setting.KEY_MANUAL_VIDEO_FRAME_RATE.equals(key) || key.contains("picture-size")) {
                useRemoveAnim = false;
            }
            SettingIntegrationBase.this.removeChildSettingViewWithDelay(useRemoveAnim, 0);
            if (key.contains("picture-size") || Setting.KEY_SQUARE_PICTURE_SIZE.equals(key)) {
                SettingIntegrationBase.this.changeQuickSettingText((QuickButton) SettingIntegrationBase.this.mSettingView.findViewById(C0088R.id.quick_setting_picture_size), key, value);
            } else if (key.contains(Setting.KEY_VIDEO_RECORDSIZE) || Setting.KEY_SQUARE_VIDEO_SIZE.equals(key) || key.equals(Setting.KEY_MANUAL_VIDEO_SIZE) || Setting.KEY_SLOW_MOTION_VIDEO_SIZE.equals(key)) {
                SettingIntegrationBase.this.changeQuickSettingText((QuickButton) SettingIntegrationBase.this.mSettingView.findViewById(C0088R.id.quick_setting_video_size), key, value);
            }
        }
    }

    public SettingIntegrationBase(SettingInterface setting) {
        super(setting);
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }

    protected void initQuickSettingOrder() {
        if (this.mRearQuickSettingList != null) {
            if (this.mRearQuickSettingList.size() > 0) {
                this.mRearQuickSettingList.clear();
            }
            if (ModelProperties.isLongLCDModel()) {
                this.mRearQuickSettingList.add(new QuickButtonTypeFullVision(true));
            }
            this.mRearQuickSettingList.add(new QuickButtonTypePictureSize(true));
            this.mRearQuickSettingList.add(new QuickButtonTypeVideoSize(true));
            this.mRearQuickSettingList.add(new QuickButtonTypeTimer(true));
            if (this.mFrontQuickSettingList != null) {
                if (this.mFrontQuickSettingList.size() > 0) {
                    this.mFrontQuickSettingList.clear();
                }
                if (ModelProperties.isLongLCDModel()) {
                    this.mFrontQuickSettingList.add(new QuickButtonTypeFullVision(true));
                }
                this.mFrontQuickSettingList.add(new QuickButtonTypePictureSize(true));
                this.mFrontQuickSettingList.add(new QuickButtonTypeVideoSize(true));
                this.mFrontQuickSettingList.add(new QuickButtonTypeTimer(true));
            }
        }
    }

    protected void initRearSettingOrder() {
        if (this.mOrderBackCamera != null) {
            if (this.mOrderBackCamera.size() > 0) {
                this.mOrderBackCamera.clear();
            }
            initManualOnlySetingOrder();
            this.mOrderBackCamera.add(Setting.SETTING_SECTION_GENERAL);
            addSWPixelBinning(this.mOrderBackCamera);
            addLensSelection(this.mOrderBackCamera);
            addOrderHdr(this.mOrderBackCamera);
            if (FunctionProperties.isSupportedSteadyCamera(true)) {
                this.mOrderBackCamera.add(Setting.KEY_VIDEO_STEADY);
            }
            this.mOrderBackCamera.add(Setting.SETTING_SECTION_FUNCTION);
            if (FunctionProperties.isLivePhotoSupported()) {
                this.mOrderBackCamera.add(Setting.KEY_LIVE_PHOTO);
            }
            addOrderVoiceShutter(this.mOrderBackCamera);
            addOrderTilePreview(this.mOrderBackCamera);
            addOrderTrackingAF(this.mOrderBackCamera);
            if (FunctionProperties.isSupportedQrCode(this.mGet.getAppContext())) {
                addOrderQR(this.mOrderBackCamera);
            }
            CameraCapabilities cc = this.mGet.getCameraCapabilities();
            if (cc != null && cc.isFingerDetectionSupported()) {
                CamLog.m3d(CameraConstants.TAG, "[finger-detection]");
                this.mOrderBackCamera.add(Setting.KEY_FINGER_DETECTION);
            }
            this.mOrderBackCamera.add(Setting.KEY_TAG_LOCATION);
            if (CameraConstants.MODE_MANUAL_CAMERA.equals(this.mGet.getShotMode())) {
                this.mOrderBackCamera.add(Setting.KEY_INCLINOMETER);
            } else {
                this.mOrderBackCamera.add(Setting.KEY_FRAME_GRID);
            }
            addOrderSignature(this.mOrderBackCamera);
            addOrderGeneralCommonSetting(this.mOrderBackCamera, false);
        }
    }

    protected void initManualOnlySetingOrder() {
    }

    protected void initFrontSettingOrder() {
        if (this.mOrderFrontCamera != null) {
            if (this.mOrderFrontCamera.size() > 0) {
                this.mOrderFrontCamera.clear();
            }
            this.mOrderFrontCamera.add(Setting.SETTING_SECTION_GENERAL);
            if (!((this.mGet.isAttachIntent() && this.mGet.isVideoCaptureMode()) || CameraConstants.MODE_SNAP.equals(this.mGet.getShotMode()) || CameraConstants.MODE_FLASH_JUMPCUT.equals(this.mGet.getShotMode()) || !FunctionProperties.isShutterlessSupported(this.mGet.getAppContext()))) {
                this.mOrderFrontCamera.add(Setting.KEY_SHUTTERLESS_SELFIE);
            }
            this.mOrderFrontCamera.add(Setting.KEY_SAVE_DIRECTION);
            addLensSelection(this.mOrderFrontCamera);
            addOrderHdr(this.mOrderFrontCamera);
            addOrderMotionQuickView(this.mOrderFrontCamera);
            if (FunctionProperties.isSupportedSteadyCamera(false)) {
                this.mOrderFrontCamera.add(Setting.KEY_VIDEO_STEADY);
            }
            this.mOrderFrontCamera.add(Setting.SETTING_SECTION_FUNCTION);
            if (FunctionProperties.isLivePhotoSupported()) {
                this.mOrderFrontCamera.add(Setting.KEY_LIVE_PHOTO);
            }
            addOrderVoiceShutter(this.mOrderFrontCamera);
            addOrderTilePreview(this.mOrderFrontCamera);
            this.mOrderFrontCamera.add(Setting.KEY_TAG_LOCATION);
            addOrderSignature(this.mOrderFrontCamera);
            addOrderGeneralCommonSetting(this.mOrderFrontCamera, false);
        }
    }

    private void addOrderSignature(ArrayList<String> order) {
        if (order != null && FunctionProperties.isSignatureSupported(this.mGet.getAppContext())) {
            order.add(Setting.KEY_SIGNATURE);
        }
    }

    private void addOrderQR(ArrayList<String> order) {
        if (order != null) {
            order.add(Setting.KEY_QR);
        }
    }

    private void addOrderHdr(ArrayList<String> order) {
        if (order != null && FunctionProperties.isSupportedHDR(isRearCamera()) > 0) {
            order.add("hdr-mode");
        }
    }

    protected void addOrderTilePreview(ArrayList<String> order) {
        if (order != null && FunctionProperties.isSupportedCameraRoll() && !this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
            order.add(Setting.KEY_TILE_PREVIEW);
        }
    }

    private void addOrderVoiceShutter(ArrayList<String> order) {
        if (order != null && FunctionProperties.isSupportedVoiceShutter() && !this.mGet.isRecordingPriorityMode()) {
            order.add(Setting.KEY_VOICESHUTTER);
        }
    }

    protected void addOrderTrackingAF(ArrayList<String> order) {
        if (order != null && this.mGet.isFocusTrackingSupported()) {
            CamLog.m3d(CameraConstants.TAG, "addOrderTrackingAF");
            order.add("tracking-af");
        }
    }

    private void addOrderMotionQuickView(ArrayList<String> order) {
        String shotMode = this.mGet.getShotMode();
        if (FunctionProperties.isSupportedMotionQuickView() && !this.mGet.isAttachIntent() && !CameraConstants.MODE_SNAP.equals(this.mGet.getShotMode()) && shotMode != null && !shotMode.contains(CameraConstants.MODE_SQUARE)) {
            order.add(Setting.KEY_MOTION_QUICKVIEWER);
        }
    }

    private void addOrderStorage(ArrayList<String> order) {
        if (order != null) {
            if (!this.mGet.isStorageRemoved(2) || !this.mGet.isStorageRemoved(1)) {
                order.add(Setting.KEY_STORAGE);
            }
        }
    }

    private void addOrderAuCloud(ArrayList<String> order) {
        if (order != null && FunctionProperties.isSupportedAuClude()) {
            order.add(Setting.KEY_AU_CLOUD);
        }
    }

    protected void addOrderGeneralCommonSetting(ArrayList<String> order, boolean isManualMode) {
        addOrderStorage(order);
        addOrderAuCloud(order);
        if (this.mGet.isNeedHelpItem()) {
            order.add(Setting.SETTING_ITEM_HELP);
        }
    }

    private void addSWPixelBinning(ArrayList<String> order) {
        if (order != null && FunctionProperties.isSupportedBinning(0)) {
            order.add(Setting.KEY_BINNING);
        }
    }

    private void addLensSelection(ArrayList<String> order) {
        if (order != null && FunctionProperties.isSupportedLGLens(this.mGet.getAppContext()) && FunctionProperties.isSupportedGoogleLens()) {
            order.add(Setting.KEY_LENS_SELECTION);
        }
    }

    public void rotateSettingView(int degree) {
        if (this.mSettingParentRotateView != null) {
            this.mSettingParentRotateView.rotateLayout(degree);
        }
    }

    public void rotateChildSettingView(int degree) {
        if (this.mSettingChildRotateView != null) {
            setChildListViewLayoutParams(degree);
            this.mSettingChildRotateView.rotateLayout(degree);
        }
    }

    public void rotateQuickSettingLayout(int degree) {
        LinearLayout ll = (LinearLayout) this.mGet.findViewById(C0088R.id.setting_quicksetting_layout);
        if (ll != null) {
            ArrayList<QuickButtonType> quickSettingList = isRearCamera() ? this.mRearQuickSettingList : this.mFrontQuickSettingList;
            for (int i = 0; i < quickSettingList.size(); i++) {
                QuickButton quickSettingItem = (QuickButton) ll.getChildAt(i);
                quickSettingItem.setLayoutParams((LayoutParams) quickSettingItem.getLayoutParams());
                quickSettingItem.setDegree(degree, true);
            }
        }
    }

    public void removeUselessSetting() {
        if (this.mGet.isStorageRemoved(2) || this.mGet.isStorageRemoved(1)) {
            this.mOrderBackCamera.remove(Setting.KEY_STORAGE);
            this.mOrderFrontCamera.remove(Setting.KEY_STORAGE);
        }
    }

    public void refreshSetting() {
        if (this.mParentAdapter != null) {
            this.mParentAdapter.update();
        }
        if (this.mSettingView != null) {
            LinearLayout quickSettingLayout = (LinearLayout) this.mSettingView.findViewById(C0088R.id.setting_quicksetting_layout);
            if (quickSettingLayout != null) {
                int childCount = quickSettingLayout.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    QuickButton quickButton = (QuickButton) quickSettingLayout.getChildAt(i);
                    if (quickButton != null) {
                        String key = quickButton.getKey();
                        if (!(key == null || Setting.KEY_FULLVISION.equals(key) || this.mCurSetting == null)) {
                            changeQuickSettingText(quickButton, key, this.mCurSetting.getSettingValue(key));
                        }
                    }
                }
            }
        }
    }

    protected void removeChildSettingViewWithDelay(final boolean isShowAnim, long delay) {
        CamLog.m3d(CameraConstants.TAG, "removeChildSettingView, isShowAnim : " + isShowAnim + ", delay : " + delay);
        if (this.mParentAdapter != null) {
            this.mParentAdapter.setSelectedIndex(-1);
            this.mParentAdapter.update();
        }
        if (this.mParentListView != null) {
            this.mParentListView.setImportantForAccessibility(0);
        }
        this.mGet.postOnUiThread(new HandlerRunnable(this) {

            /* renamed from: com.lge.camera.settings.SettingIntegrationBase$1$1 */
            class C14091 implements AnimationListener {
                C14091() {
                }

                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    if (SettingIntegrationBase.this.mSettingChildLayout != null) {
                        SettingIntegrationBase.this.mSettingChildLayout.setVisibility(8);
                        SettingIntegrationBase.this.releaseChildMenuResource();
                        SettingIntegrationBase.this.mGet.onRemoveChildSettingEnd();
                    }
                }
            }

            public void handleRun() {
                if (SettingIntegrationBase.this.mSettingChildLayout != null) {
                    if (!isShowAnim) {
                        SettingIntegrationBase.this.mSettingChildLayout.setVisibility(8);
                        SettingIntegrationBase.this.releaseChildMenuResource();
                    } else if (SettingIntegrationBase.this.mSettingChildLayout.getVisibility() == 0) {
                        AnimationUtil.startShowingAnimation(SettingIntegrationBase.this.mSettingChildLayout, false, 200, new C14091());
                    }
                }
            }
        }, delay);
    }

    protected void releaseResources() {
        if (this.mParentAdapter != null) {
            this.mParentAdapter.close();
            this.mParentAdapter = null;
        }
        if (this.mParentListView != null) {
            this.mParentListView.setAdapter(null);
            this.mParentListView.setOnItemClickListener(null);
            this.mParentListView.removeAllViewsInLayout();
            if (this.mParentListView.getBackground() != null) {
                this.mParentListView.getBackground().setCallback(null);
                this.mParentListView.setBackground(null);
            }
            this.mParentListView = null;
        }
        if (this.mParentMenuList != null) {
            this.mParentMenuList.clear();
        }
        this.mSwitchButton = null;
    }

    protected void releaseChildMenuResource() {
        if (this.mChildPopupAdapter != null) {
            this.mChildPopupAdapter.close();
            this.mChildPopupAdapter = null;
        }
        if (this.mChildPopupListView != null) {
            this.mChildPopupListView.setAdapter(null);
            this.mChildPopupListView.setOnItemClickListener(null);
            this.mChildPopupListView.removeAllViewsInLayout();
            if (this.mChildPopupListView.getBackground() != null) {
                this.mChildPopupListView.getBackground().setCallback(null);
                this.mChildPopupListView.setBackground(null);
            }
        }
        if (this.mChildPopupMenuList != null) {
            this.mChildPopupMenuList.clear();
        }
    }

    public boolean isChildViewVisible() {
        return this.mSettingChildLayout != null && this.mSettingChildLayout.getVisibility() == 0;
    }

    protected int getListSize(boolean isHeight) {
        int[] lcdSize = Utils.getLCDsize(this.mGet.getAppContext(), true);
        if (isHeight) {
            return lcdSize[1];
        }
        return (lcdSize[1] * 4) / 3;
    }

    public int getSettingMargin() {
        int thumbList = 0;
        if ("on".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW))) {
            thumbList = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.11112f);
        }
        int result = thumbList + RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext());
        CamLog.m7i(CameraConstants.TAG, "getSettingMargin result : " + result);
        return result;
    }

    protected boolean checkModuleValidate() {
        if (this.mGet.checkModuleValidate(223)) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "CheckModule invalidate return.");
        return false;
    }

    protected void makeChildSettingMenuItemList(String key, ArrayList<SettingMenuItem> menuItemList) {
        SettingMenuItem settingMenuItem = getCameraSettingMenu().getMenuItem(key);
        if (settingMenuItem != null && menuItemList != null) {
            int count = settingMenuItem.getChildCount();
            if (SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getSharedPreferenceCameraId()).equals(key)) {
                makeVideoSizeSettingMenuItemList(key, menuItemList, settingMenuItem, count);
            } else if (ModelProperties.isLguCloudServiceModel() && Setting.KEY_STORAGE.equals(key)) {
                makeStorageSettingMenuItemList(key, menuItemList, settingMenuItem, count);
            } else {
                for (int i = 0; i < count; i++) {
                    SettingMenuItem childItem = settingMenuItem.getChild(i);
                    childItem.setKey(key);
                    menuItemList.add(childItem);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x003e, code:
            if (com.lge.camera.constants.CameraConstants.VGA_RESOLUTION.equals(r1) == false) goto L_0x004a;
     */
    private void makeVideoSizeSettingMenuItemList(java.lang.String r5, java.util.ArrayList<com.lge.camera.settings.SettingMenuItem> r6, com.lge.camera.settings.SettingMenuItem r7, int r8) {
        /*
        r4 = this;
        r2 = 0;
    L_0x0001:
        if (r2 >= r8) goto L_0x006e;
    L_0x0003:
        r0 = r7.getChild(r2);
        if (r0 != 0) goto L_0x000c;
    L_0x0009:
        r2 = r2 + 1;
        goto L_0x0001;
    L_0x000c:
        r3 = r4.mGet;
        r3 = r3.isAttachIntent();
        if (r3 == 0) goto L_0x0024;
    L_0x0014:
        r3 = r4.mGet;
        r3 = r3.isAttachIntent();
        if (r3 == 0) goto L_0x0051;
    L_0x001c:
        r3 = r4.mGet;
        r3 = r3.isVideoCaptureMode();
        if (r3 != 0) goto L_0x0051;
    L_0x0024:
        r1 = r0.getValue();
        r3 = "320x240";
        r3 = r3.equals(r1);
        if (r3 != 0) goto L_0x0040;
    L_0x0030:
        r3 = "176x144";
        r3 = r3.equals(r1);
        if (r3 != 0) goto L_0x0040;
    L_0x0038:
        r3 = "640x480";
        r3 = r3.equals(r1);
        if (r3 == 0) goto L_0x004a;
    L_0x0040:
        if (r2 != 0) goto L_0x0009;
    L_0x0042:
        r3 = "640x480";
        r3 = r3.equals(r1);
        if (r3 == 0) goto L_0x0009;
    L_0x004a:
        r0.setKey(r5);
        r6.add(r0);
        goto L_0x0009;
    L_0x0051:
        r1 = r0.getValue();
        r3 = "320x240";
        r3 = r3.equals(r1);
        if (r3 != 0) goto L_0x0009;
    L_0x005d:
        r3 = "176x144";
        r3 = r3.equals(r1);
        if (r3 != 0) goto L_0x0009;
    L_0x0065:
        r3 = "1280x720@120";
        r3 = r3.equals(r1);
        if (r3 == 0) goto L_0x004a;
    L_0x006d:
        goto L_0x0009;
    L_0x006e:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.settings.SettingIntegrationBase.makeVideoSizeSettingMenuItemList(java.lang.String, java.util.ArrayList, com.lge.camera.settings.SettingMenuItem, int):void");
    }

    private void makeStorageSettingMenuItemList(String key, ArrayList<SettingMenuItem> menuItemList, SettingMenuItem settingMenuItem, int count) {
        String value = getSettingValue(key);
        for (int i = 0; i < count; i++) {
            SettingMenuItem childItem = settingMenuItem.getChild(i);
            if (childItem != null) {
                String childValue = childItem.getValue();
                if (!this.mGet.isStorageRemoved(StorageUtil.convertStorageNameToType(childValue))) {
                    if (value != null && value.equals(childValue)) {
                        settingMenuItem.setSelectedChildPos(menuItemList.size());
                    }
                    childItem.setKey(key);
                    menuItemList.add(childItem);
                }
            }
        }
    }

    protected void onItemClickChildPopup(String curKey, int position) {
        if (this.mSettingView != null) {
            if (this.mChildPopupMenuList != null) {
                this.mChildPopupMenuList.clear();
            }
            makeChildSettingMenuItemList(curKey, this.mChildPopupMenuList);
            SettingMenuItem settingMenuItem = getCameraSettingMenu().getMenuItem(curKey);
            TextView parentTitle = (TextView) this.mSettingView.findViewById(C0088R.id.setting_child_popup_title);
            String settingTitle = "";
            if (settingMenuItem != null) {
                settingTitle = settingMenuItem.getName();
            }
            parentTitle.setText(settingTitle);
            this.mChildPopupAdapter = new SettingChildAdapter(this.mGet.getActivity(), R.layout.dialog_c_6, getCameraSettingMenu(), this.mChildPopupMenuList);
            if (this.mParentListView != null) {
                this.mParentListView.setImportantForAccessibility(4);
            }
            this.mChildPopupListView = (ListView) this.mSettingView.findViewById(C0088R.id.setting_child_popup_listview);
            this.mChildPopupListView.setOnItemClickListener(this.mChildItemClickListener);
            this.mChildPopupListView.setChoiceMode(1);
            this.mChildPopupListView.setAdapter(this.mChildPopupAdapter);
            if (settingMenuItem != null) {
                this.mChildPopupListView.setItemChecked(settingMenuItem.getSelectedChildPos(), true);
            }
            this.mSubTitle = (TextView) this.mSettingView.findViewById(C0088R.id.setting_child_popup_subtitle);
            String subtitleDesc = "";
            if (Setting.KEY_LENS_SELECTION.equals(curKey)) {
                this.mSubTitle.setText(this.mGet.getAppContext().getString(C0088R.string.setting_visual_search_limitation_google_lens));
                this.mSubTitle.setVisibility(0);
            } else if (Setting.KEY_BINNING.equals(curKey)) {
                String htmlRes = "<img src=\"add_icon\"/>";
                int descResId = FunctionProperties.isUseSuperBright() ? C0088R.string.setting_bright_mode_popup_desc_talkback : C0088R.string.setting_normal_bright_mode_popup_desc;
                this.mSubTitle.setText(Html.fromHtml(this.mGet.getActivity().getResources().getString(FunctionProperties.isUseSuperBright() ? C0088R.string.setting_bright_mode_popup_desc_rev2 : C0088R.string.setting_normal_bright_mode_popup_desc_rev2, new Object[]{"<img src=\"add_icon\"/>"}), new C14113(), null));
                this.mSubTitle.setMovementMethod(new ScrollingMovementMethod());
                this.mSubTitle.setContentDescription(this.mGet.getActivity().getString(descResId));
                this.mSubTitle.setVisibility(0);
            } else {
                this.mSubTitle.setVisibility(8);
            }
            rotateChildSettingView(this.mDegree);
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (SettingIntegrationBase.this.mSettingChildPopupLayout != null) {
                        AnimationUtil.startShowingAnimation(SettingIntegrationBase.this.mSettingChildLayout, true, 200, SettingIntegrationBase.this.mChildPopAnimListener, false);
                    }
                }
            }, 0);
            ((Button) this.mSettingView.findViewById(C0088R.id.setting_child_cancel_button)).setOnClickListener(new C14135());
        }
    }

    protected void resizeChildListView() {
        if (this.mChildPopupListView != null) {
            int maxHeight;
            boolean isPortrait = this.mDegree == 0 || this.mDegree == 180;
            int height = this.mChildPopupListView.getHeight();
            if (isPortrait) {
                maxHeight = (int) (((float) getListSize(true)) * 0.95f);
            } else {
                maxHeight = (int) (((float) getListSize(true)) * 0.6f);
            }
            if (height > maxHeight) {
                CamLog.m3d(CameraConstants.TAG, "resize setting child listview height, " + Utils.getNormalNumeric(height) + " to " + Utils.getNormalNumeric(maxHeight));
                RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) this.mChildPopupListView.getLayoutParams();
                rlp.height = maxHeight;
                this.mChildPopupListView.setLayoutParams(rlp);
            }
        }
    }

    protected void setChildListViewLayoutParams(int degree) {
        if (this.mChildPopupListView != null && this.mSettingChildPopupLayout != null && this.mSettingChildLayout != null && this.mSubTitle != null) {
            RelativeLayout.LayoutParams childPopupLayoutParam = (RelativeLayout.LayoutParams) this.mSettingChildPopupLayout.getLayoutParams();
            RelativeLayout.LayoutParams childListViewLayoutParam = (RelativeLayout.LayoutParams) this.mChildPopupListView.getLayoutParams();
            int[] lcdSize = Utils.getLCDsize(this.mGet.getAppContext(), true);
            int hiddenPadding = (int) Utils.dpToPx(this.mGet.getActivity(), 16.0f);
            if (degree == 0 || degree == 180) {
                childPopupLayoutParam.height = -2;
                childPopupLayoutParam.width = ((int) (((double) lcdSize[1]) * 0.95d)) - (hiddenPadding * 2);
                childPopupLayoutParam.setMarginsRelative(0, hiddenPadding * 2, 0, hiddenPadding * 2);
                childListViewLayoutParam.height = -2;
            } else {
                childPopupLayoutParam.height = -2;
                childPopupLayoutParam.width = ((int) (((double) lcdSize[0]) * 0.65d)) - (hiddenPadding * 2);
                childListViewLayoutParam.height = -2;
                childPopupLayoutParam.setMarginsRelative(0, hiddenPadding * 2, 0, hiddenPadding * 2);
            }
            if (this.mGet.isActivatedTilePreview()) {
                childPopupLayoutParam.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.11112f);
            }
            this.mSubTitle.scrollTo(0, 0);
            this.mSettingChildPopupLayout.setLayoutParams(childPopupLayoutParam);
            this.mChildPopupListView.setLayoutParams(childListViewLayoutParam);
        }
    }

    public void updateLocationSwitchButton() {
        if (this.mSwitchButton != null) {
            CompoundButton compoundButton = this.mSwitchButton;
            boolean z = "on".equals(getSettingValue(Setting.KEY_TAG_LOCATION)) && CheckStatusManager.isSystemSettingUseLocation(this.mGet.getAppContext().getContentResolver());
            compoundButton.setChecked(z);
        }
    }

    public QuickButton getQuickSettingButton(String key) {
        if (key == null) {
            return null;
        }
        View button = null;
        Object obj = -1;
        switch (key.hashCode()) {
            case -1205182474:
                if (key.equals(Setting.KEY_VIDEO_RECORDSIZE)) {
                    obj = 2;
                    break;
                }
                break;
            case -832040080:
                if (key.equals("picture-size")) {
                    obj = 1;
                    break;
                }
                break;
            case -590423157:
                if (key.equals(Setting.KEY_TIMER)) {
                    obj = 3;
                    break;
                }
                break;
            case 1899851031:
                if (key.equals(Setting.KEY_FULLVISION)) {
                    obj = null;
                    break;
                }
                break;
        }
        switch (obj) {
            case null:
                button = this.mSettingView.findViewById(C0088R.id.quick_setting_full_vision);
                break;
            case 1:
                button = this.mSettingView.findViewById(C0088R.id.quick_setting_picture_size);
                break;
            case 2:
                button = this.mSettingView.findViewById(C0088R.id.quick_setting_video_size);
                break;
            case 3:
                button = this.mSettingView.findViewById(C0088R.id.quick_setting_timer);
                break;
        }
        return (QuickButton) button;
    }

    public void onOrientationChanged(final int degree) {
        if (this.mSettingView != null && !this.mSettingViewRemoving && this.mDegree != degree) {
            CamLog.m3d(CameraConstants.TAG, "onOrientationChanged : mDegree = " + this.mDegree + ", degree = " + degree);
            this.mDegree = degree;
            if (this.mParentAdapter != null) {
                this.mParentAdapter.setListItemDegree(this.mDegree);
            }
            this.mIsOrientationChanging = true;
            AnimationUtil.startAlphaAnimation(this.mSettingParentRotateView, 1.0f, 0.0f, 130, new AnimationListener() {
                public void onAnimationStart(Animation arg0) {
                }

                public void onAnimationRepeat(Animation arg0) {
                }

                public void onAnimationEnd(Animation arg0) {
                    SettingIntegrationBase.this.rotateSettingView(degree);
                    SettingIntegrationBase.this.rotateChildSettingView(degree);
                    SettingIntegrationBase.this.rotateQuickSettingLayout(degree);
                    AnimationUtil.startAlphaAnimation(SettingIntegrationBase.this.mParentListView, 0.0f, 1.0f, 130, SettingIntegrationBase.this.mChildPopAnimListener);
                    SettingIntegrationBase.this.mGet.postOnUiThread(new HandlerRunnable(SettingIntegrationBase.this) {
                        public void handleRun() {
                            SettingIntegrationBase.this.mIsOrientationChanging = false;
                        }
                    });
                }
            });
        }
    }

    public boolean isVisible() {
        return this.mSettingView == null ? false : this.mSettingView.isShown();
    }

    public boolean isOpeningSettingMenu() {
        return this.mSettingMenuOpening;
    }

    public void onConfigurationChanged(Configuration config) {
        if (this.mSettingView != null) {
            int degree = this.mGet.getOrientationDegree();
            CamLog.m3d(CameraConstants.TAG, "degree = " + degree);
            onOrientationChanged(degree);
        }
    }

    public void setSettingMenuEnable(String key, boolean enable) {
        if (this.mParentAdapter != null) {
            this.mParentAdapter.setAvailableUpdate(true);
        }
        super.setSettingMenuEnable(key, enable);
    }

    public void changeQuickSettingText(QuickButton button, String key, String value) {
        if (key.contains("picture-size") || key.contains(Setting.KEY_VIDEO_RECORDSIZE) || key.contains("key_square") || Setting.KEY_MANUAL_VIDEO_SIZE.equals(key) || key.equals(Setting.KEY_SLOW_MOTION_VIDEO_SIZE)) {
            ListPreference listPref = this.mCurPrefGroup.findPreference(key);
            if (listPref != null) {
                String entry;
                if (value == null) {
                    entry = getCameraSettingMenu().getCurChildEntry(key);
                } else {
                    entry = (String) listPref.getEntries()[listPref.findIndexOfValue(value)];
                }
                String[] split = entry.split(" ");
                String convertedEntry = "";
                for (int j = 0; j < split.length - 1; j++) {
                    convertedEntry = convertedEntry + split[j];
                }
                button.setText(convertedEntry + "\n" + split[split.length - 1]);
            }
        }
    }

    protected void updateButtonBySetting(QuickButton button) {
        String key = button.getKey();
        if (!"".equals(key)) {
            button.setIndex(getSettingIndex(key));
            button.invalidate();
            if (Setting.KEY_FULLVISION.equals(key)) {
                button.setSelected("on".equals(getSettingValue(Setting.KEY_FULLVISION)));
            }
            if (Setting.KEY_TIMER.equals(key)) {
                button.setSelected(!"0".equals(getSettingValue(Setting.KEY_TIMER)));
            }
        }
    }

    public void updateButtonBySetting(String key) {
        if (this.mSettingView != null && key != null) {
            ArrayList<QuickButtonType> quickSettingList = isRearCamera() ? this.mRearQuickSettingList : this.mFrontQuickSettingList;
            if (quickSettingList != null) {
                Iterator it = quickSettingList.iterator();
                while (it.hasNext()) {
                    QuickButtonType type = (QuickButtonType) it.next();
                    if (type != null && key.equals(type.mKey)) {
                        updateButtonBySetting((QuickButton) this.mSettingView.findViewById(type.mId));
                        return;
                    }
                }
            }
        }
    }
}
