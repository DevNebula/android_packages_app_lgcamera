package com.lge.camera.managers;

import android.net.Uri;
import android.view.ViewGroup;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.managers.QuickClipStatusManager.Status;
import com.lge.camera.managers.QuickclipManagerIF.DrawerShowOption;
import com.lge.camera.managers.QuickclipManagerIF.onQuickClipListListener;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;

public class QuickClipManager extends QuickClipManagerBase implements OnRemoveHandler {
    protected HandlerRunnable mInitUIHandlerAfterMakingList = new HandlerRunnable(this) {
        public void handleRun() {
            QuickClipManager.this.initQuickClipAfterMakingList();
        }
    };
    protected Thread mMakeListThread = null;

    /* renamed from: com.lge.camera.managers.QuickClipManager$1 */
    class C11041 implements Runnable {
        C11041() {
        }

        public void run() {
            QuickClipManager.this.makeQuickclipList(true);
            QuickClipManager.this.mGet.runOnUiThread(QuickClipManager.this.mInitUIHandlerAfterMakingList);
        }
    }

    public QuickClipManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setQuickClipListListener(onQuickClipListListener listener) {
        this.mQuickClipListListener = listener;
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }

    public void onPauseBefore() {
        CamLog.m3d(CameraConstants.TAG, "onPauseBefore - start");
        super.onPauseBefore();
        this.mIsManagerPausing = true;
        hideBubbleGuide();
        hide(false);
        this.mStatusManager.releaseQuickClipStatus();
        if (this.mStatusManager.getStatus() == Status.INIT) {
            this.mStatusManager.setStatus(Status.NORMAL_VIEW);
            this.mStatusManager.setDrawStatus(Status.NORMAL_VIEW);
        }
        waitReloadThread();
        if (this.mQuickClipPopUpDialog != null) {
            this.mQuickClipPopUpDialog.onDismiss();
        }
        if (this.mDrawerView != null) {
            this.mDrawerView.releaseView();
            this.mDrawerView = null;
        }
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (!(vg == null || this.mQuickClipLayout == null)) {
            vg.removeView(this.mQuickClipLayout);
            CamLog.m3d(CameraConstants.TAG, "remove mQuickClipDrawer");
            this.mQuickClipLayout = null;
        }
        if (this.mMakeListThread != null && this.mMakeListThread.isAlive()) {
            try {
                this.mMakeListThread.join(CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mIsLayoutInited = false;
        setDrawerScroll(false);
        CamLog.m3d(CameraConstants.TAG, "onPauseBefore - end");
    }

    public void onStop() {
        CamLog.m3d(CameraConstants.TAG, "onStop - " + this);
        if (sClipSharedItems != null) {
            sClipSharedItems.clear();
            sClipSharedItems = null;
        }
        this.mStatusManager.releaseQuickClipStatus();
        this.mQuickClipLayout = null;
        this.mSharedUri = null;
        if (!(this.mGet == null || this.mReceiverForSlomoSave == null)) {
            this.mGet.unregisterBroadcastReceiver(this.mReceiverForSlomoSave);
        }
        super.onStop();
    }

    public void setRotateDegree(int degree, boolean animation) {
        CamLog.m3d(CameraConstants.TAG, "setRotateDegree");
        if (!this.mIsScrolling && this.mDrawerView != null) {
            if (this.mIsLayoutInited && this.mGet.getCameraId() != 0 && !this.mDrawerView.isOpened() && this.mStatusManager.getStatus() == Status.CIRCLE_VIEW) {
                setMiniView();
                animation = false;
            }
            if (this.mBubblePopupManager != null) {
                this.mBubblePopupManager.setRotateDegree(degree, false);
            }
            this.mDrawerView.setRotateDegree(degree, animation);
            setAnimViewRotateDegree(degree, animation);
        }
    }

    protected void setAnimViewRotateDegree(int degree, boolean animation) {
        RotateImageView aniArrow = (RotateImageView) this.mGet.findViewById(C0088R.id.quick_clip_animation_arrow);
        RotateImageView aniIcon = (RotateImageView) this.mGet.findViewById(C0088R.id.quick_clip_animation_icon);
        if (aniArrow != null && aniIcon != null) {
            aniArrow.setDegree(degree, animation);
            aniIcon.setDegree(degree, animation);
        }
    }

    public void setQuickClipLongshot(boolean enable) {
        CamLog.m3d(CameraConstants.TAG, "enable : " + enable);
        if (this.mStatusManager != null) {
            if (enable) {
                if (this.mStatusManager.getStatus() != Status.INIT_LONG_SHOT) {
                    hide(false);
                    this.mStatusManager.setStatus(Status.INIT_LONG_SHOT);
                }
            } else if (this.mStatusManager.getStatus() == Status.INIT_LONG_SHOT) {
                setAfterShot();
                if (this.mStatusManager.isSkipOnBurstShot() && !this.mGet.isStillBurstShotSaving()) {
                    CamLog.m7i(CameraConstants.TAG, "Skip updating on burst shot.");
                    Uri lastUri = this.mStatusManager.getSkipOnBurstShotUri();
                    if (lastUri != null) {
                        this.mStatusManager.sendNotifyUriMessage(lastUri, 0);
                    }
                } else if (this.mStatusManager.isUpdatedUri()) {
                    CamLog.m7i(CameraConstants.TAG, "Already Updated Uri for burst shot");
                    this.mStatusManager.setStatus(Status.CIRCLE_VIEW);
                    show(DrawerShowOption.CLOSE, false);
                }
            }
        }
    }

    public void setAfterShot() {
        if (this.mIsLayoutInited) {
            this.mStatusManager.setStatus(Status.INIT);
        } else {
            this.mStatusManager.setReadyStatus();
        }
    }

    public void doBackMultishot() {
        CamLog.m3d(CameraConstants.TAG, "status : " + this.mStatusManager.getStatus());
        if (this.mStatusManager != null) {
            if (this.mStatusManager.getStatus() == Status.INIT_TIMER_SHOT || this.mStatusManager.getStatus() == Status.INIT) {
                rollbackStatus();
            }
        }
    }

    public void showSelfTimer(boolean isShow, boolean keepState) {
        CamLog.m3d(CameraConstants.TAG, "showSelfTimer - isShow = " + isShow + ", keepState = " + keepState);
        if (this.mStatusManager != null) {
            if (isShow) {
                hide(keepState);
                this.mStatusManager.setStatus(Status.INIT_TIMER_SHOT);
            } else if (this.mStatusManager.getStatus() == Status.INIT_TIMER_SHOT) {
                rollbackStatus();
            }
        }
    }

    public void setLayout() {
        CamLog.m3d(CameraConstants.TAG, "QuickClip setLayout");
        if (this.mStatusManager == null) {
            CamLog.m3d(CameraConstants.TAG, "mStatusManager not init.");
        } else if (!this.mIsLayoutInited) {
            CamLog.m3d(CameraConstants.TAG, "QuickClip init start");
            checkStylusModel();
            waitReloadThread();
            Uri sharedUri = this.mGet.isActivatedQuickdetailView() ? this.mSharedUri : this.mGet.getUri();
            if (sharedUri == null) {
                this.mStatusManager.setStatus(Status.IDLE);
                this.mStatusManager.setDrawStatus(Status.IDLE);
            }
            setSharedUri(sharedUri);
            this.mMakeListThread = new Thread(new C11041());
            this.mMakeListThread.start();
        }
    }

    public void initQuickClipAfterMakingList() {
        CamLog.m3d(CameraConstants.TAG, "initQuickClipAfterMakingList");
        if (!this.mGet.isPaused() && !this.mIsManagerPausing) {
            initQuickClipDrawer();
            setRotateDegree(getOrientationDegree(), false);
            if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(this.mGet.getShotMode()) && this.mGet.isSquareSnapAccessView() && this.mStatusManager.getDrawStatus() != Status.IDLE) {
                this.mIsVisible = false;
                setAfterShot();
            }
            setQuickClipIcon(this.mIsVisible);
            this.mStatusManager.setPreferenceListener(true);
            this.mStatusManager.setStatusCallback(this);
            if (this.mBubblePopupManager != null) {
                this.mBubblePopupManager.initializeNotiComplete();
            }
            CamLog.m3d(CameraConstants.TAG, "initQuickClipAfterMakingList End");
        }
    }

    public void onCameraSwitchingStart() {
        hide(false);
        super.onCameraSwitchingStart();
    }

    public boolean isQuickClipShowing() {
        if (this.mStatusManager == null) {
            return false;
        }
        if (this.mStatusManager.getStatus().equals(Status.CIRCLE_VIEW) || this.mStatusManager.getStatus().equals(Status.NORMAL_VIEW)) {
            return true;
        }
        return false;
    }

    public void setForceStatusForTilePreview(boolean isCircleView, boolean isSet) {
        CamLog.m3d(CameraConstants.TAG, "isCircleView : " + isCircleView + " isSet : " + isSet);
        this.mIsTilePreviewOn = isSet;
        if (this.mStatusManager != null) {
            if (this.mStatusManager.getStatus() == Status.IDLE) {
                this.mStatusManager.setStatus(Status.INIT);
            }
            QuickClipStatusManager quickClipStatusManager = this.mStatusManager;
            Status status = (isCircleView && isSet) ? Status.CIRCLE_VIEW : Status.NORMAL_VIEW;
            quickClipStatusManager.setStatus(status);
        }
    }

    public void setForceStatusForSquareMode(boolean init) {
        CamLog.m3d(CameraConstants.TAG, "init : " + init);
        if (init) {
            this.mStatusManager.setStatus(Status.INIT);
        } else if (this.mStatusManager.getStatus() == Status.INIT) {
            this.mStatusManager.setStatus(Status.CIRCLE_VIEW);
        }
    }

    public void setContentType(String contentType) {
        CamLog.m3d(CameraConstants.TAG, "contentType>" + contentType);
        if (contentType != null) {
            if (this.mLastMimeType == null || !this.mLastMimeType.equals(contentType)) {
                this.mSharedUri = Uri.EMPTY;
                this.mLastMimeType = contentType;
                reloadSharedList(false);
                return;
            }
            show(DrawerShowOption.CLOSE, false);
        }
    }

    public void setIsVisible(boolean flag) {
        this.mIsVisible = flag;
    }

    public void checkStylusModel() {
        String penSupported = SharedPreferenceUtil.getPenSupportedValue(getAppContext());
        if (CameraConstants.PEN_SUPPORTED_NOT_DEFINED.equals(penSupported)) {
            boolean isPenSupported = ModelProperties.isSupportedStylusPen(getAppContext());
            if (isPenSupported) {
                SharedPreferenceUtil.savePenSupportedValue(getAppContext(), "true");
            } else {
                SharedPreferenceUtil.savePenSupportedValue(getAppContext(), "false");
            }
            this.mIsPenSupported = isPenSupported;
        } else if ("true".equals(penSupported)) {
            this.mIsPenSupported = true;
        } else {
            this.mIsPenSupported = false;
        }
    }

    public int getTopPosition() {
        return RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, this.mMarginBottomRatio) + Utils.getPx(getAppContext(), C0088R.dimen.review_thumbnail.size);
    }

    public void onResumeAfter() {
        this.mIsClicked = false;
        this.isShowingShareDialog = false;
        this.mIsManagerPausing = false;
    }
}
