package com.lge.camera.managers;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.MultiDirectionSlidingDrawer.OnDrawerCloseListener;
import com.lge.camera.components.MultiDirectionSlidingDrawer.OnDrawerOpenListener;
import com.lge.camera.components.MultiDirectionSlidingDrawer.OnDrawerScrollListener;
import com.lge.camera.components.QuickClipSlidingDrawer;
import com.lge.camera.components.QuickClipSlidingDrawer.onTriggerQuickClip;
import com.lge.camera.components.RotateImageView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.dialog.CamDialogInterface;
import com.lge.camera.dialog.QuickClipPopUpDialog;
import com.lge.camera.dialog.QuickClipPopUpDialog.QuickClipPopupInterface;
import com.lge.camera.managers.QuickClipStatusManager.QuickClipStatusCallback;
import com.lge.camera.managers.QuickClipStatusManager.Status;
import com.lge.camera.managers.QuickclipManagerIF.DrawerShowOption;
import com.lge.camera.managers.QuickclipManagerIF.onQuickClipListListener;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.QuickClipSharedItem;
import com.lge.camera.util.QuickClipUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.TalkBackUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;

public class QuickClipManagerBase extends ManagerInterfaceImpl implements QuickClipStatusCallback, OnDrawerOpenListener, OnDrawerCloseListener, OnDrawerScrollListener {
    protected static final int QUICK_CLIP_ANI_DUR = 200;
    public static final String SLOMO_CLIENT_CLASS = "slomo_client_class";
    public static final String SLOMO_CLIENT_PACAKGE = "slomo_client_package";
    public static final String SLOMO_SAVE = "slomo_save";
    public static final String SLOMO_SAVE_TASKID = "slomo_save_taskid";
    public static final String SLOMO_SERVICE_PERMISSION_CONFIRMED = "slomo_service_permission_confirmed";
    protected static ArrayList<QuickClipSharedItem> sClipSharedItems = null;
    private static Object sLock = new Object();
    protected boolean isShowingShareDialog = false;
    protected BubblePopupManager mBubblePopupManager = new BubblePopupManager(this.mGet, this.mMarginBottomRatio);
    protected int mCurrentSelectedMenu = 0;
    protected QuickClipSlidingDrawer mDrawerView = null;
    protected boolean mIsClicked = false;
    protected boolean mIsLayoutInited = false;
    protected boolean mIsManagerPausing = false;
    protected boolean mIsPenSupported = false;
    protected boolean mIsReloaddingList = false;
    protected boolean mIsScrolling = false;
    private boolean mIsSharingSlomoSavedFile = false;
    protected boolean mIsTilePreviewOn = false;
    protected boolean mIsVisible = false;
    protected String mLastMimeType = null;
    protected float mMarginBottomRatio = 0.108f;
    protected AnimatorSet mQuickClipAnimator = null;
    protected OnClickListener mQuickClipClickListner = new C11116();
    protected View mQuickClipLayout = null;
    protected onQuickClipListListener mQuickClipListListener = null;
    protected QuickClipPopUpDialog mQuickClipPopUpDialog = null;
    protected BroadcastReceiver mReceiverForSlomoSave;
    protected Thread mReloadListThread = null;
    protected HandlerRunnable mReloadUIHandler = new HandlerRunnable(this) {
        public void handleRun() {
            if (QuickClipManagerBase.this.mIsLayoutInited && QuickClipManagerBase.sClipSharedItems != null && QuickClipManagerBase.this.mDrawerView != null && QuickClipManagerBase.this.mDrawerView.updateAppIcon(QuickClipManagerBase.sClipSharedItems)) {
                QuickClipManagerBase.this.mStatusManager.resetCircleViewTimer();
                QuickClipManagerBase.this.show(DrawerShowOption.CLOSE, false);
                QuickClipManagerBase.this.mIsReloaddingList = false;
                QuickClipManagerBase.this.enableQuickClip(true);
                CamLog.m7i(CameraConstants.TAG, "reloadShareList() - end");
            }
        }
    };
    protected Uri mSharedUri = null;
    protected QuickClipStatusManager mStatusManager = new QuickClipStatusManager(this.mGet);

    /* renamed from: com.lge.camera.managers.QuickClipManagerBase$3 */
    class C11083 implements onTriggerQuickClip {
        C11083() {
        }

        public void onChanged(boolean isCircleView) {
            CamLog.m3d(CameraConstants.TAG, "onChanged isNormalView : " + isCircleView);
            if (!isCircleView && QuickClipManagerBase.this.mStatusManager.getStatus() != Status.NORMAL_VIEW) {
                QuickClipManagerBase.this.hideBubbleGuide();
                QuickClipManagerBase.this.mStatusManager.setStatus(Status.NORMAL_VIEW);
                QuickClipManagerBase.this.show(DrawerShowOption.CLOSE, true);
            }
        }

        public void onTouchEvent() {
            QuickClipManagerBase.this.mGet.hideModeMenu(false, true);
            if (QuickClipManagerBase.this.mGet.isSettingMenuVisible()) {
                QuickClipManagerBase.this.mGet.removeSettingMenu(false, false);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.QuickClipManagerBase$6 */
    class C11116 implements OnClickListener {
        C11116() {
        }

        public void onClick(View v) {
            if (!QuickClipManagerBase.this.mIsClicked) {
                QuickClipManagerBase.this.mIsClicked = true;
                QuickClipManagerBase.this.setDrawerScroll(false);
                CamLog.m3d(CameraConstants.TAG, "Item Clicked isReload ?" + QuickClipManagerBase.this.mIsReloaddingList);
                QuickClipManagerBase.this.mCurrentSelectedMenu = ((Integer) v.getTag()).intValue();
                if (!QuickClipManagerBase.this.mIsReloaddingList && QuickClipManagerBase.this.mDrawerView != null && !QuickClipManagerBase.this.mDrawerView.isLock()) {
                    if (QuickClipManagerBase.this.mGet == null || !QuickClipManagerBase.this.mGet.checkCollageContentsShareAvailable() || QuickClipManagerBase.this.mCurrentSelectedMenu == 10 || !(CameraConstants.MODE_SQUARE_SPLICE.equals(QuickClipManagerBase.this.mGet.getShotMode()) || CameraConstants.MODE_SQUARE_GRID.equals(QuickClipManagerBase.this.mGet.getShotMode()))) {
                        QuickClipManagerBase.this.doClickQuickClip();
                    } else {
                        QuickClipManagerBase.this.mGet.setCollageContentsSharedFlag();
                    }
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.QuickClipManagerBase$7 */
    class C11127 extends BroadcastReceiver {
        C11127() {
        }

        public void onReceive(Context context, Intent intent) {
            CamLog.m7i(CameraConstants.TAG, "[slomo_share] : " + intent.hasExtra(QuickClipManagerBase.SLOMO_SERVICE_PERMISSION_CONFIRMED) + "       " + intent.getIntExtra(QuickClipManagerBase.SLOMO_SERVICE_PERMISSION_CONFIRMED, 0));
            if (intent.hasExtra(QuickClipManagerBase.SLOMO_SERVICE_PERMISSION_CONFIRMED) && QuickClipManagerBase.this.mGet.getActivity().getTaskId() == intent.getIntExtra(QuickClipManagerBase.SLOMO_SERVICE_PERMISSION_CONFIRMED, 0)) {
                CamLog.m7i(CameraConstants.TAG, "[slomo_share] start service after Permission granted");
                Intent it = new Intent();
                it.setComponent(new ComponentName("com.lge.videostudio", "com.lge.videostudio.main.VEExternalSaveService"));
                it.putExtra(QuickClipManagerBase.SLOMO_SAVE_TASKID, QuickClipManagerBase.this.mGet.getActivity().getTaskId());
                it.putExtra(QuickClipManagerBase.SLOMO_CLIENT_PACAKGE, QuickClipManagerBase.this.mGet.getActivity().getPackageName());
                it.putExtra(QuickClipManagerBase.SLOMO_CLIENT_CLASS, QuickClipManagerBase.this.mGet.getActivity().getClass().getName());
                it.putExtra(QuickClipManagerBase.SLOMO_SAVE, QuickClipManagerBase.this.mSharedUri);
                QuickClipManagerBase.this.hide(false);
                QuickClipManagerBase.this.mGet.showProgressBarDialog(true, 0);
                QuickClipManagerBase.this.mGet.bindSlomoSaveService(it);
            }
        }
    }

    public QuickClipManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setSharedUri(Uri uri) {
        this.mSharedUri = uri;
        if (this.mSharedUri == null) {
            this.mLastMimeType = null;
            hide(false);
            return;
        }
        String newMimeType = QuickClipUtil.getMimeType(this.mSharedUri, this.mGet.getAppContext());
        if (newMimeType == null) {
            this.mSharedUri = null;
            this.mLastMimeType = null;
            hide(false);
        } else if (this.mLastMimeType == null || !this.mLastMimeType.equals(newMimeType)) {
            this.mLastMimeType = newMimeType;
            reloadSharedList(true);
        } else {
            show(DrawerShowOption.CLOSE, false);
        }
    }

    protected void waitReloadThread() {
        if (this.mReloadListThread != null && this.mReloadListThread.isAlive()) {
            try {
                CamLog.m7i(CameraConstants.TAG, " wait Reload Thread");
                this.mReloadListThread.join();
                this.mReloadUIHandler.removeRunnable();
                this.mIsReloaddingList = false;
                CamLog.m7i(CameraConstants.TAG, " wait Done Reload Thread");
            } catch (InterruptedException e) {
                CamLog.m5e(CameraConstants.TAG, "Exception " + e.toString());
            }
        }
    }

    protected void reloadSharedList(final boolean isExistUri) {
        if ((!isExistUri || this.mSharedUri != null) && this.mQuickClipLayout != null && this.mIsLayoutInited) {
            CamLog.m7i(CameraConstants.TAG, "reloadShareList() - start");
            waitReloadThread();
            this.mIsReloaddingList = true;
            this.mReloadListThread = new Thread(new Runnable() {
                public void run() {
                    QuickClipManagerBase.this.makeQuickclipList(isExistUri);
                    QuickClipManagerBase.this.mGet.runOnUiThread(QuickClipManagerBase.this.mReloadUIHandler);
                }
            });
            this.mReloadListThread.start();
        }
    }

    public void setQuickClipIcon(boolean isVisible) {
        CamLog.m3d(CameraConstants.TAG, "isVisible : " + isVisible);
        this.mIsVisible = isVisible;
        if (this.mIsVisible) {
            show(DrawerShowOption.CLOSE, true);
        } else {
            hide(false);
        }
    }

    public void makeQuickclipList(boolean isExistUri) {
        synchronized (sLock) {
            CamLog.m3d(CameraConstants.TAG, "makeQuickclipList init start - " + this + " / sClipSharedItems : " + sClipSharedItems);
            if (sClipSharedItems != null) {
                sClipSharedItems.clear();
                sClipSharedItems = null;
            }
            if (isExistUri) {
                sClipSharedItems = QuickClipUtil.getPreferSharedList(this.mSharedUri, this.mGet.getAppContext(), 5);
            } else {
                sClipSharedItems = QuickClipUtil.getPreferSharedList(this.mLastMimeType, this.mGet.getAppContext(), 5);
            }
            if (sClipSharedItems == null) {
                return;
            }
            try {
                CamLog.m3d(CameraConstants.TAG, "mClipSharedItems : " + sClipSharedItems.size());
                String postfix = " " + this.mGet.getAppContext().getString(C0088R.string.sp_share_SHORT);
                String postfixButton = " " + this.mGet.getAppContext().getString(C0088R.string.button);
                for (int i = 0; i < sClipSharedItems.size(); i++) {
                    ((QuickClipSharedItem) sClipSharedItems.get(i)).setLabel(((QuickClipSharedItem) sClipSharedItems.get(i)).getLabel() + postfix + postfixButton);
                }
                if (sClipSharedItems.size() >= 5) {
                    sClipSharedItems.add(new QuickClipSharedItem(this.mGet.getAppContext().getDrawable(C0088R.drawable.btn_camera_quick_clip_sns_more), this.mGet.getAppContext().getString(C0088R.string.quick_clip_more), "", ""));
                }
            } catch (NullPointerException e) {
                CamLog.m3d(CameraConstants.TAG, "sClipSharedItems is null");
            }
            CamLog.m3d(CameraConstants.TAG, "setQuickClipList init END");
        }
    }

    public void onConfigurationChanged(Configuration config) {
        initQuickClipDrawer();
        if (isOpened()) {
            drawerOpen(true);
        }
        if (this.mBubblePopupManager != null) {
            this.mBubblePopupManager.beforeConfigChanged();
        }
        super.onConfigurationChanged(config);
    }

    public void setQuickClipLayoutDepth(boolean isTilePreviewOn, float z) {
        CamLog.m3d(CameraConstants.TAG, "z : " + z);
        if (this.mQuickClipLayout == null) {
            initQuickClipDrawer();
        } else if (isTilePreviewOn) {
            this.mQuickClipLayout.setZ(1.0f + z);
        }
    }

    protected void initQuickClipDrawer() {
        CamLog.m3d(CameraConstants.TAG, "initQuickClipDrawer");
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        this.mQuickClipLayout = null;
        this.mQuickClipLayout = this.mGet.inflateView(C0088R.layout.quick_clip_selector);
        if (!(vg == null || this.mQuickClipLayout == null)) {
            vg.removeView(this.mQuickClipLayout);
            vg.addView(this.mQuickClipLayout, -1, new LayoutParams(-1, -1));
            this.mDrawerView = (QuickClipSlidingDrawer) this.mGet.findViewById(C0088R.id.quick_clip_menu_slide);
            this.mDrawerView.setOnItemClickedListener(this.mQuickClipClickListner);
            this.mDrawerView.setContext(getAppContext());
            CamLog.m3d(CameraConstants.TAG, "mDrawerView : " + this.mDrawerView + ", this:" + this);
        }
        if (this.mDrawerView != null) {
            if (!(this.mIsLayoutInited || vg == null)) {
                if (this.mDrawerView.makeQuickClipList(vg.getWidth(), sClipSharedItems)) {
                    this.mIsLayoutInited = true;
                } else {
                    vg.removeView(this.mQuickClipLayout);
                    this.mQuickClipLayout = null;
                    this.mDrawerView = null;
                    return;
                }
            }
            updateLayout(this.mStatusManager.getStatus());
            setDrawerListener(this.mDrawerView);
        }
    }

    protected void setDrawerListener(QuickClipSlidingDrawer drawer) {
        if (drawer != null) {
            drawer.setOnQuickClipStatus(new C11083());
            drawer.setOnDrawerOpenListener(this);
            drawer.setOnDrawerCloseListener(this);
            drawer.setOnDrawerScrollListener(this);
        }
    }

    public void drawerOpen(boolean isAnimation) {
        CamLog.m9v(CameraConstants.TAG, "animation open");
        if (this.mDrawerView != null && this.mDrawerView.getVisibility() == 0 && !this.mDrawerView.isOpened()) {
            if (isAnimation) {
                this.mDrawerView.animateOpen();
            } else {
                this.mDrawerView.open();
            }
        }
    }

    public void drawerClose(boolean isAnimation) {
        if (this.mDrawerView == null || this.mDrawerView.getVisibility() != 0 || !this.mDrawerView.isOpened()) {
            return;
        }
        if (isAnimation) {
            this.mDrawerView.animateClose();
        } else {
            this.mDrawerView.close();
        }
    }

    public void show(DrawerShowOption drawerShowOption, boolean forceInvalidate) {
        CamLog.m9v(CameraConstants.TAG, "show mIsVisible:" + this.mIsVisible + " uri :" + this.mSharedUri + " mStatusManager Status:" + this.mStatusManager.getStatus());
        if (!this.mIsLayoutInited || this.mDrawerView == null || this.mGet == null) {
            CamLog.m3d(CameraConstants.TAG, "mIsLayoutInited : " + this.mIsLayoutInited + " mDrawerView : " + this.mDrawerView + ", this :" + this);
        } else if (!this.mIsVisible || this.mSharedUri == null || this.mGet.isJogZoomMoving() || this.mGet.isZoomBarVisible()) {
            hide(false);
        } else {
            final Status status = this.mStatusManager.getStatus();
            final Status drawStatus = this.mStatusManager.getDrawStatus();
            if ((status == Status.NORMAL_VIEW || status == Status.CIRCLE_VIEW) && this.mGet != null) {
                final boolean z = forceInvalidate;
                final DrawerShowOption drawerShowOption2 = drawerShowOption;
                this.mGet.runOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (QuickClipManagerBase.this.updateLayout(status)) {
                            if (status == Status.NORMAL_VIEW) {
                                if (QuickClipManagerBase.this.isOpened() || z || drawStatus == Status.NORMAL_VIEW) {
                                    QuickClipManagerBase.this.showNormalView(drawerShowOption2);
                                } else {
                                    QuickClipManagerBase.this.setQuickClipAnim(false, drawStatus == Status.IDLE, false, QuickClipManagerBase.sClipSharedItems, drawerShowOption2);
                                }
                                QuickClipManagerBase.this.mStatusManager.setDrawStatus(Status.NORMAL_VIEW);
                            } else if (status == Status.CIRCLE_VIEW) {
                                if (drawStatus == Status.CIRCLE_VIEW) {
                                    QuickClipManagerBase.this.showCircleView(status);
                                } else if (QuickClipManagerBase.this.mIsTilePreviewOn || SharedPreferenceUtil.getQuickClipBubble(QuickClipManagerBase.this.mGet.getAppContext())) {
                                    QuickClipManagerBase.this.setQuickClipAnim(true, drawStatus == Status.IDLE, false, QuickClipManagerBase.sClipSharedItems, drawerShowOption2);
                                } else {
                                    QuickClipManagerBase.this.showBubbleGuide();
                                    QuickClipManagerBase.this.showCircleView(status);
                                }
                                QuickClipManagerBase.this.mStatusManager.setDrawStatus(Status.CIRCLE_VIEW);
                            }
                            QuickClipManagerBase.this.mIsClicked = false;
                            return;
                        }
                        CamLog.m3d(CameraConstants.TAG, "updatelayout failed.");
                    }
                });
            }
        }
    }

    protected void showCircleView(Status status) {
        CamLog.m3d(CameraConstants.TAG, "showCircleView");
        if ((status == Status.NORMAL_VIEW || status == Status.CIRCLE_VIEW) && this.mDrawerView != null) {
            View view = this.mGet.findViewById(C0088R.id.quick_clip_menu_view);
            if (view != null) {
                view.setVisibility(0);
            }
            drawerClose(false);
        }
    }

    protected void showNormalView(DrawerShowOption drawerShowOption) {
        CamLog.m3d(CameraConstants.TAG, "showNormalView : drawerShowOption = " + drawerShowOption);
        LinearLayout menuView = (LinearLayout) this.mGet.findViewById(C0088R.id.quick_clip_menu);
        if (menuView == null || this.mDrawerView == null) {
            CamLog.m3d(CameraConstants.TAG, "menuView is null, so return!!");
        } else if (menuView.findViewWithTag(Integer.valueOf(1)) != null || this.mDrawerView.makeQuickClipList(this.mGet.findViewById(C0088R.id.camera_controls).getWidth(), sClipSharedItems)) {
            View view = this.mGet.findViewById(C0088R.id.quick_clip_menu_view);
            if (view != null) {
                view.setVisibility(0);
            }
            if (drawerShowOption == DrawerShowOption.OPEN) {
                drawerOpen(true);
            } else if (drawerShowOption == DrawerShowOption.CLOSE) {
                drawerClose(true);
            }
        } else {
            CamLog.m3d(CameraConstants.TAG, "makeQuickClipList is fail, so return!!");
        }
    }

    public void hide(boolean keepStatus) {
        CamLog.m9v(CameraConstants.TAG, "HideMenuDrawer executed : keepStatus = " + keepStatus);
        if (this.mIsLayoutInited) {
            this.mStatusManager.clearCircleViewTimer();
            hideBubbleGuide();
            final Status status = this.mStatusManager.getStatus();
            final Status drawStatus = this.mStatusManager.getDrawStatus();
            final boolean z = keepStatus;
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (QuickClipManagerBase.this.mDrawerView != null) {
                        if (QuickClipManagerBase.this.isOpened()) {
                            QuickClipManagerBase.this.mDrawerView.close();
                        }
                        QuickClipManagerBase.this.setDrawerScroll(false);
                    }
                    if (!z && status == Status.CIRCLE_VIEW) {
                        QuickClipManagerBase.this.mStatusManager.setStatus(Status.NORMAL_VIEW);
                        QuickClipManagerBase.this.updateLayout(status);
                    }
                    if (!z || QuickClipManagerBase.this.isOpened() || drawStatus == Status.NORMAL_VIEW || drawStatus == Status.IDLE) {
                        QuickClipManagerBase.this.stopQuickClipAnimation();
                        View view = QuickClipManagerBase.this.mGet.findViewById(C0088R.id.quick_clip_menu_view);
                        if (view != null) {
                            view.setVisibility(8);
                            CamLog.m3d(CameraConstants.TAG, "Quick Clip INVISIBLE");
                        }
                    } else {
                        QuickClipManagerBase.this.setQuickClipAnim(false, true, true, QuickClipManagerBase.sClipSharedItems, DrawerShowOption.CLOSE);
                    }
                    QuickClipManagerBase.this.mStatusManager.setDrawStatus(Status.IDLE);
                }
            });
        }
    }

    protected boolean updateLayout(Status status) {
        LinearLayout handleView = (LinearLayout) this.mGet.findViewById(C0088R.id.quick_clip_handle_layout);
        if (handleView == null || this.mDrawerView == null) {
            CamLog.m3d(CameraConstants.TAG, "updateLayout failed ");
            return false;
        }
        int endMargin;
        CamLog.m3d(CameraConstants.TAG, "status : " + status);
        RelativeLayout quickClipMargin = (RelativeLayout) this.mGet.findViewById(C0088R.id.quick_clip_menu_layout);
        this.mMarginBottomRatio = (float) getDefaultBottomMarginRatio();
        int bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, this.mMarginBottomRatio);
        if (status == Status.CIRCLE_VIEW) {
            endMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.quick_clip_handle_circle_padding_end);
        } else {
            endMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.quick_clip_handle_padding_end_non_circle_view);
        }
        quickClipMargin.setPaddingRelative(quickClipMargin.getPaddingStart(), quickClipMargin.getPaddingTop(), endMargin, bottomMargin);
        if (status == Status.CIRCLE_VIEW) {
            this.mDrawerView.setCircleView(true);
            handleView.setBackground(this.mGet.getAppContext().getDrawable(C0088R.drawable.camera_quick_clip_circle_bg));
            int offset = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.quick_clip_circle_bottom_offset);
            if (ModelProperties.isTablet(getAppContext())) {
                offset = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.046f) * -1;
            }
            this.mDrawerView.setBottomOffset(offset);
            this.mDrawerView.updateHandleArrow(102);
            this.mDrawerView.setTouchPadding(false);
            return true;
        }
        int bottomOffset = handleView.getLayoutParams().width - Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.quick_clip_normal_bottom_offset);
        this.mDrawerView.setCircleView(false);
        handleView.setBackground(this.mGet.getAppContext().getDrawable(C0088R.drawable.camera_quick_clip_sns_bg_normal));
        if (this.mIsPenSupported) {
            bottomOffset -= Utils.getPx(getAppContext(), C0088R.dimen.quick_clip_pen_adjust_value);
        }
        this.mDrawerView.setBottomOffset(bottomOffset);
        if (isOpened()) {
            this.mDrawerView.updateHandleArrow(100);
            this.mDrawerView.setTouchPadding(true);
            return true;
        }
        this.mDrawerView.updateHandleArrow(101);
        this.mDrawerView.setTouchPadding(false);
        return true;
    }

    public void rollbackStatus() {
        if (this.mStatusManager != null && this.mStatusManager.rollbackStatus()) {
            show(DrawerShowOption.CLOSE, true);
        }
    }

    public void setSlomoSaveFile(boolean isSlomoSavedFile) {
        this.mIsSharingSlomoSavedFile = isSlomoSavedFile;
    }

    private void bindSlomoSave() {
        CamLog.m7i(CameraConstants.TAG, "[slomo_share] file is video_slomo. start slomo transcording service.");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.lge.videostudio", "com.lge.videostudio.main.VEExternalSaveService"));
        intent.putExtra(SLOMO_SAVE_TASKID, this.mGet.getActivity().getTaskId());
        intent.putExtra(SLOMO_CLIENT_PACAKGE, this.mGet.getActivity().getPackageName());
        intent.putExtra(SLOMO_CLIENT_CLASS, this.mGet.getActivity().getClass().getName());
        intent.putExtra(SLOMO_SAVE, this.mSharedUri);
        hide(false);
        this.mGet.bindSlomoSaveService(intent);
        registerBroadCastForSlomoSave();
    }

    public void registerBroadCastForSlomoSave() {
        this.mReceiverForSlomoSave = new C11127();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SLOMO_SERVICE_PERMISSION_CONFIRMED);
        CamLog.m3d(CameraConstants.TAG, "[slomo_share] registerBroadcastReceiver");
        this.mGet.registerBroadcastReceiver(this.mReceiverForSlomoSave, filter);
    }

    public BroadcastReceiver getSlomoBroadcastReceiver() {
        return this.mReceiverForSlomoSave;
    }

    public void setSlomoBroadcastReceiver(BroadcastReceiver receiver) {
        this.mReceiverForSlomoSave = receiver;
    }

    public void doClickQuickClip() {
        CamLog.m3d(CameraConstants.TAG, "mCurrentSelectedMenu " + this.mCurrentSelectedMenu);
        if (this.mGet != null && this.mDrawerView != null) {
            CamLog.m3d(CameraConstants.TAG, "[slomo_share] mSharedUri = " + this.mSharedUri + ", file path = " + FileUtil.getRealPathFromURI(getAppContext(), this.mSharedUri));
            if (this.mCurrentSelectedMenu != 10 && 12 == FileUtil.getModeColumnFromURI(getAppContext(), this.mSharedUri)) {
                this.mIsSharingSlomoSavedFile = true;
                bindSlomoSave();
            } else if (this.mCurrentSelectedMenu == 10) {
                if (this.mDrawerView.isOpened()) {
                    show(DrawerShowOption.CLOSE, false);
                } else {
                    show(DrawerShowOption.OPEN, false);
                }
            } else if (this.mCurrentSelectedMenu == 5) {
                showShareDialog();
            } else if (sClipSharedItems == null) {
                makeQuickclipList(true);
            } else {
                QuickClipSharedItem quickClipSharedItem = (QuickClipSharedItem) sClipSharedItems.get(this.mCurrentSelectedMenu);
                if (this.mStatusManager.getStatus() == Status.CIRCLE_VIEW) {
                    LdbUtil.sendLDBIntent(this.mGet.getAppContext(), LdbConstants.LDB_FEATURE_NAME_QUICKCLIP_SHAREBYONEITEM);
                } else {
                    LdbUtil.sendLDBIntent(this.mGet.getAppContext(), LdbConstants.LDB_FEATURE_NAME_QUICKCLIP_SHAREBYLIST);
                }
                if (QuickClipUtil.isFakeMode() && QuickClipUtil.sIsEnabled) {
                    setMiniView();
                    showWarningDialog(quickClipSharedItem);
                    if (!this.mDrawerView.isOpened()) {
                        this.mGet.restoreSelfieMenuVisibility();
                        return;
                    }
                    return;
                }
                QuickClipUtil.launchSharedActivity(this.mGet.getActivity(), this.mSharedUri, quickClipSharedItem);
            }
        }
    }

    protected synchronized void showShareDialog() {
        if (!this.isShowingShareDialog) {
            Uri sharedUri = this.mGet.getUri();
            if (this.mGet.isActivatedQuickdetailView() || this.mIsSharingSlomoSavedFile) {
                sharedUri = this.mSharedUri;
            }
            CamLog.m3d(CameraConstants.TAG, "sharedUri = " + sharedUri);
            this.mStatusManager.setStatus(Status.NORMAL_VIEW);
            show(DrawerShowOption.CLOSE, false);
            if (QuickClipUtil.onShowShareDialog(this.mGet.getActivity(), sharedUri)) {
                this.isShowingShareDialog = true;
            }
        }
    }

    protected void showWarningDialog(final QuickClipSharedItem quickClipSharedItem) {
        this.mQuickClipPopUpDialog = new QuickClipPopUpDialog((CamDialogInterface) this.mGet, new QuickClipPopupInterface() {
            public void isUpload(boolean isUpload) {
                QuickClipUtil.launchSharedActivity(QuickClipManagerBase.this.mGet.getActivity(), QuickClipManagerBase.this.mSharedUri, quickClipSharedItem, isUpload);
                if (!isUpload) {
                    QuickClipManagerBase.this.hideBubbleGuide();
                    QuickClipManagerBase.this.mStatusManager.setStatus(Status.NORMAL_VIEW);
                    QuickClipManagerBase.this.updateLayout(QuickClipManagerBase.this.mStatusManager.getStatus());
                    QuickClipManagerBase.this.reloadSharedList(true);
                }
            }

            public void resetClickedFlag() {
                QuickClipManagerBase.this.mIsClicked = false;
            }
        });
        this.mQuickClipPopUpDialog.create();
    }

    public void setMiniView() {
        View view = this.mGet.findViewById(C0088R.id.quick_clip_menu_view);
        if (view != null && view.isShown() && this.mStatusManager != null && !this.mIsReloaddingList) {
            if (this.mStatusManager.getStatus() == Status.NORMAL_VIEW && !isOpened()) {
                return;
            }
            if (this.mDrawerView == null || !this.mDrawerView.isMoving()) {
                CamLog.m7i(CameraConstants.TAG, "setMiniView " + this.mStatusManager.getStatus());
                if (this.mStatusManager.getStatus() == Status.CIRCLE_VIEW) {
                    hideBubbleGuide();
                    this.mStatusManager.setStatus(Status.NORMAL_VIEW);
                }
                if (view.getVisibility() == 0) {
                    show(DrawerShowOption.CLOSE, false);
                }
            }
        }
    }

    public boolean isOpened() {
        if (this.mDrawerView == null) {
            return false;
        }
        if (this.mDrawerView.isOpened() || this.mIsScrolling) {
            return true;
        }
        return false;
    }

    protected void showBubbleGuide() {
        if (!this.mIsTilePreviewOn && !SharedPreferenceUtil.getQuickClipBubble(this.mGet.getAppContext())) {
            View handleBg = this.mGet.findViewById(C0088R.id.quick_clip_handle_layout);
            View initBg = this.mGet.findViewById(C0088R.id.quick_clip_initial_background);
            FrameLayout.LayoutParams layoutParam = (FrameLayout.LayoutParams) initBg.getLayoutParams();
            int rightMargin = Utils.getPx(getAppContext(), C0088R.dimen.quick_clip_init_bg_margin_end);
            if (ModelProperties.isTablet(getAppContext())) {
                rightMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.07f);
            }
            int bubbleHeight = (RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, this.mMarginBottomRatio) - RatioCalcUtil.getCommandBottomMargin(getAppContext())) + Utils.getPx(getAppContext(), C0088R.dimen.quick_clip_init_bg_width);
            layoutParam.rightMargin = rightMargin;
            layoutParam.bottomMargin = RatioCalcUtil.getCommandBottomMargin(getAppContext());
            layoutParam.height = bubbleHeight;
            initBg.setLayoutParams(layoutParam);
            if (!(initBg == null || handleBg == null)) {
                handleBg.setBackground(null);
                if (this.mBubblePopupManager != null) {
                    initBg.setVisibility(0);
                }
            }
            if (this.mBubblePopupManager != null) {
                this.mBubblePopupManager.showBubblePopup();
            }
            SharedPreferenceUtil.saveQuickClipBubble(this.mGet.getAppContext(), true);
        }
    }

    protected void hideBubbleGuide() {
        View handleBg = this.mGet.findViewById(C0088R.id.quick_clip_handle_layout);
        View initBg = this.mGet.findViewById(C0088R.id.quick_clip_initial_background);
        if (!(initBg == null || handleBg == null)) {
            handleBg.setBackground(this.mGet.getAppContext().getDrawable(C0088R.drawable.camera_quick_clip_sns_bg_normal));
            initBg.setVisibility(8);
        }
        if (this.mBubblePopupManager != null) {
            this.mBubblePopupManager.removeBubblePopup(true);
        }
    }

    protected void setDrawerScroll(boolean isScrolling) {
        this.mIsScrolling = isScrolling;
    }

    public void onThumbnailUpdated(Uri uri) {
        setSharedUri(uri);
    }

    public void onStatusChanged(Status status) {
        CamLog.m7i(CameraConstants.TAG, "onStatusChanged " + status);
        if (status == Status.NORMAL_VIEW || status == Status.CIRCLE_VIEW) {
            if (this.mStatusManager.getStatus() == Status.NORMAL_VIEW) {
                hideBubbleGuide();
            }
            if (!this.mIsReloaddingList) {
                show(DrawerShowOption.CLOSE, false);
                enableQuickClip(true);
                return;
            }
            return;
        }
        hide(false);
    }

    public void onScrollEnded() {
        setDrawerScroll(false);
    }

    public void onScrollStarted() {
        setDrawerScroll(true);
        this.mStatusManager.clearCircleViewTimer();
        if (this.mQuickClipListListener != null) {
            this.mQuickClipListListener.onListOpend();
        }
    }

    public void onDrawerOpened() {
        if (this.mDrawerView != null) {
            TalkBackUtil.sendAccessibilityEvent(getAppContext(), getClass().getName(), String.format(this.mGet.getAppContext().getString(C0088R.string.quick_clip_talkback_tray_list), new Object[]{Integer.valueOf(6)}));
            this.mDrawerView.updateHandleArrow(100);
            this.mStatusManager.clearCircleViewTimer();
            if (this.mQuickClipListListener != null) {
                this.mQuickClipListListener.onListOpend();
            }
        }
    }

    public void onDrawerClosed() {
        if (this.mDrawerView != null) {
            if (this.mStatusManager.getStatus() == Status.NORMAL_VIEW) {
                this.mDrawerView.updateHandleArrow(101);
            } else if (this.mStatusManager.getStatus() == Status.CIRCLE_VIEW) {
                this.mDrawerView.updateHandleArrow(102);
            }
            this.mStatusManager.clearCircleViewTimer();
            if (!(this.mQuickClipListListener == null || this.mGet.isTimerShotCountdown())) {
                this.mQuickClipListListener.onListClosed();
            }
            this.mDrawerView.resetAnimating();
        }
    }

    public void enableQuickClip(boolean enable) {
        if (this.mDrawerView == null) {
            return;
        }
        if (enable) {
            this.mDrawerView.unlock();
        } else {
            this.mDrawerView.lock();
        }
    }

    protected void setQuickClipAnim(boolean showIcon, boolean isFirst, boolean goneAfter, ArrayList<QuickClipSharedItem> sharedItem, DrawerShowOption drawerShowOption) {
        CamLog.m3d(CameraConstants.TAG, "setQuickClipAnim - showIcon : " + showIcon + ", isFirst : " + isFirst);
        View view = this.mGet.findViewById(C0088R.id.quick_clip_menu_view);
        if (view != null) {
            view.setVisibility(0);
        }
        if (this.mQuickClipAnimator != null && this.mQuickClipAnimator.isStarted()) {
            this.mQuickClipAnimator.cancel();
        }
        View aniView = this.mGet.findViewById(C0088R.id.quick_clip_animation_layout);
        View aniArrow = (RotateImageView) this.mGet.findViewById(C0088R.id.quick_clip_animation_arrow);
        View aniIcon = (RotateImageView) this.mGet.findViewById(C0088R.id.quick_clip_animation_icon);
        aniIcon.setImageDrawable(getAppIconDrawable(sharedItem));
        aniIcon.setScaleType(ScaleType.FIT_XY);
        int circleEndMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.quick_clip_init_bg_margin_end);
        int handleHeight = this.mGet.getAppContext().getDrawable(C0088R.drawable.camera_quick_clip_sns_bg_normal).getIntrinsicWidth();
        int i = 2;
        float[] fadeInAlpha = new float[]{0.0f, 1.0f};
        float[] fArr = new float[2];
        fArr = new float[]{1.0f, 0.0f};
        long fadeInDur = 200;
        long fadeInDelay = 0;
        if (isFirst && showIcon) {
            fArr = new float[2];
            fArr = new float[]{0.0f, 0.0f};
        } else if (goneAfter) {
            i = 2;
            fadeInAlpha = new float[]{0.0f, 0.0f};
            fArr = new float[2];
            fArr = new float[]{1.0f, 1.0f};
            handleHeight *= 2;
        } else {
            fadeInDur = 100;
            fadeInDelay = 100;
        }
        View fadeInView = aniIcon;
        View fadeOutView = aniArrow;
        float transFrom = (float) (circleEndMargin + handleHeight);
        float transTo = 0.0f;
        if (!showIcon) {
            fadeInView = aniArrow;
            fadeOutView = aniIcon;
            transTo = transFrom;
            transFrom = 0.0f;
        }
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(fadeInView, AnimationManager.ANI_ALPHA, fadeInAlpha);
        fadeInView.setAlpha(0.0f);
        fadeIn.setStartDelay(fadeInDelay);
        fadeIn.setDuration(fadeInDur);
        ObjectAnimator.ofFloat(fadeOutView, AnimationManager.ANI_ALPHA, fArr).setDuration(100);
        ObjectAnimator.ofFloat(aniView, "translationX", new float[]{transFrom, transTo}).setDuration(200);
        this.mQuickClipAnimator = new AnimatorSet();
        this.mQuickClipAnimator.playTogether(new Animator[]{translateX, fadeIn, fadeOut});
        setAnimationListener(drawerShowOption, goneAfter);
        this.mQuickClipAnimator.start();
    }

    protected void setAnimationListener(final DrawerShowOption drawerShowOption, final boolean goneAfter) {
        if (this.mQuickClipAnimator != null) {
            this.mQuickClipAnimator.addListener(new AnimatorListener() {
                public void onAnimationStart(Animator animator) {
                    QuickClipManagerBase.this.setAnimViewVisible(true);
                }

                public void onAnimationEnd(Animator animator) {
                    CamLog.m3d(CameraConstants.TAG, "onAnimationEnd : goneAfter = " + goneAfter);
                    QuickClipManagerBase.this.setAnimViewVisible(false);
                    if (QuickClipManagerBase.this.mQuickClipAnimator != null) {
                        QuickClipManagerBase.this.mQuickClipAnimator.removeAllListeners();
                        QuickClipManagerBase.this.mQuickClipAnimator = null;
                    }
                    if (goneAfter || QuickClipManagerBase.this.mGet.isMenuShowing(1003)) {
                        View view = QuickClipManagerBase.this.mGet.findViewById(C0088R.id.quick_clip_menu_view);
                        if (view != null) {
                            view.setVisibility(8);
                            return;
                        }
                        return;
                    }
                    Status status = QuickClipManagerBase.this.mStatusManager.getStatus();
                    if (status == Status.NORMAL_VIEW) {
                        QuickClipManagerBase.this.showNormalView(drawerShowOption);
                    } else if (status == Status.CIRCLE_VIEW) {
                        QuickClipManagerBase.this.showBubbleGuide();
                        QuickClipManagerBase.this.showCircleView(status);
                    }
                }

                public void onAnimationCancel(Animator animator) {
                    CamLog.m3d(CameraConstants.TAG, "onAnimationCancel");
                    QuickClipManagerBase.this.setAnimViewVisible(false);
                }

                public void onAnimationRepeat(Animator animator) {
                }
            });
        }
    }

    protected void setAnimViewVisible(boolean animStart) {
        CamLog.m3d(CameraConstants.TAG, "setAnimViewVisible - animStart : " + animStart);
        View aniView = this.mGet.findViewById(C0088R.id.quick_clip_animation_layout);
        if (aniView != null && this.mDrawerView != null) {
            aniView.setClickable(false);
            if (animStart) {
                aniView.setVisibility(0);
                this.mDrawerView.setVisibility(8);
                return;
            }
            aniView.setVisibility(8);
            this.mDrawerView.setVisibility(0);
        }
    }

    protected Drawable getAppIconDrawable(ArrayList<QuickClipSharedItem> sharedItem) {
        if (sharedItem != null) {
            for (int i = 0; i < sharedItem.size(); i++) {
                if (i == 0) {
                    return ((QuickClipSharedItem) sharedItem.get(i)).getAppIcon();
                }
            }
        }
        return null;
    }

    protected void stopQuickClipAnimation() {
        if (this.mQuickClipAnimator != null && this.mQuickClipAnimator.isStarted()) {
            CamLog.m3d(CameraConstants.TAG, "stopAnimationAndHide.");
            this.mQuickClipAnimator.cancel();
        }
    }

    private double getDefaultBottomMarginRatio() {
        double margin = (double) RatioCalcUtil.getCommandBottomMargin(getAppContext());
        DisplayMetrics metrics = Utils.getWindowRealMatics(getAppContext());
        return margin / ((double) Math.max(metrics.widthPixels, metrics.heightPixels));
    }
}
