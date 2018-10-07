package com.lge.camera.managers;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.MemoryUtils;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.Utils;
import com.lge.camera.util.ViewConfigurationHelper;

public class FocusManager extends AEControlManager {
    private View mBaseView = null;
    protected boolean mCheckAEAFLock = false;
    protected boolean mCheckTracking = false;
    protected boolean mIsOnlyTAF = false;
    protected int mObjectOutOfScreenTime = 0;
    private int mPointX = 0;
    private int mPointY = 0;
    Point mPreTrackingPoint = new Point(0, 0);
    protected long mPrevTouchDownTime = 0;
    protected boolean mWasLongPress = false;

    public FocusManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        int touchResId = this.mGet.isRearCamera() ? C0088R.drawable.focus_touch_taf : C0088R.drawable.camera_focus_front_ae;
        Drawable drawable = touchResId > 0 ? this.mGet.getAppContext().getResources().getDrawable(touchResId) : null;
        if (drawable != null) {
            this.mRectWidth = drawable.getIntrinsicWidth();
            this.mRectHeight = drawable.getIntrinsicHeight();
            CamLog.m3d(CameraConstants.TAG, "init mRectWidth = " + this.mRectWidth + " mRectHeight = " + this.mRectHeight);
            Utils.setIgnoreTouchArea(this.mRectWidth);
        }
        createAEAFLockView();
        calRectSize();
        super.init();
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        if (this.mTouchFocusInterface != null) {
            this.mTouchFocusInterface.releaseFocusHandler();
        }
        setTrackingFocusState(false);
        hideAndCancelAllFocus(true);
        releaseFocus();
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages(null);
        }
    }

    public void onDestroy() {
        releaseFocus();
        this.mCameraFocusView = null;
        this.mCameraManualMultiFocusView = null;
        this.mTouchFocusInterface = null;
        this.mFaceDetectView = null;
        this.mInitFocus = false;
        ViewConfigurationHelper.release();
        removeView();
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        createFocus();
        initLayout();
        if (!AppControlUtil.isNeedQuickShotTaking() && this.mGet.getCameraState() == 1) {
            registerCallback();
        }
    }

    public void initializeAfterStartPreviewDone() {
        super.initializeAfterStartPreviewDone();
        if (!this.mGet.isAFSupported() && this.mTouchFocusInterface != null) {
            this.mTouchFocusInterface.initFocusAreas();
        }
    }

    public void onConfigurationChanged(Configuration config) {
        CamLog.m7i(CameraConstants.TAG, "FocusManager onConfigurationChanged-start = " + this.mMoveFocus);
        hideAndCancelAllFocus(true);
        setFocusState(0);
        initLayout();
        initAFView();
        if (!(this.mHandler == null || !this.mGet.isFocusEnableCondition() || this.mGet.isSettingMenuVisible())) {
            registerCallback();
        }
        super.onConfigurationChanged(config);
        CamLog.m7i(CameraConstants.TAG, "FocusManager onConfigurationChanged-end");
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean z = true;
        if (this.mInitFocus) {
            int posX = (int) event.getX();
            int posY = (int) event.getY();
            switch (event.getActionMasked() & 255) {
                case 0:
                    boolean z2;
                    this.mPointX = 0;
                    this.mPointY = 0;
                    if (isFocusLock() || isAELock()) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    this.mCheckAEAFLock = z2;
                    if (getFocusState() != 14) {
                        z = false;
                    }
                    this.mCheckTracking = z;
                    CamLog.m3d(CameraConstants.TAG, "-tf- ACTION_DOWN posX = " + posX + " posY = " + posY);
                    this.mTouchStartX = posX;
                    this.mTouchStartY = posY;
                    this.mPrevTouchDownTime = System.currentTimeMillis();
                    if (isFocusLock() || isAELock()) {
                        doTouchMoveDown(posX, posY);
                    }
                    this.mEVParam = null;
                    break;
                case 1:
                    if (!(isFocusLock() || isAELock() || doAEAFLock(posX, posY))) {
                        doTouchMoveDown(posX, posY);
                        processTouchUpKey(posX, posY);
                    }
                    this.mEVParam = null;
                    setState(16, false);
                    this.mTouchStartX = 0;
                    this.mTouchStartY = 0;
                    CamLog.m3d(CameraConstants.TAG, "-tf- ACTION_UP posX = " + posX + " posY = " + posY);
                    break;
                case 2:
                    CamLog.m3d(CameraConstants.TAG, "-tf- ACTION_MOVE posX = " + posX + " posY = " + posY);
                    doAEAFLock(posX, posY);
                    break;
                case 3:
                    hideTouchMove();
                    break;
            }
        }
        return false;
    }

    public void doTouchMoveDown(int x, int y) {
        CamLog.m3d(CameraConstants.TAG, "doTouchMoveDown : x = " + x + ", y = " + y);
        if (checkIsInPreview(x, y) && !Utils.isIgnoreTouchEvent(this.mGet.getAppContext(), this.mTouchStartX, this.mTouchStartY) && this.mFocusState != 12 && this.mFocusState != 13 && this.mFocusState != 2 && this.mGet.checkModuleValidate(31)) {
            if (this.mGet.checkModuleValidate(192) && !isTouchInUIArea(x, y)) {
                if (this.mTouchFocusInterface != null && (isFocusLock() || isAELock())) {
                    this.mTouchFocusInterface.releaseTouchFocus();
                }
                hideAndCancelAllFocus(false);
                if (!(this.mHandler == null || this.mFaceFocus == null)) {
                    this.mHandler.removeMessages(9);
                    this.mFaceFocus.setEnableDrawRect(false);
                }
            }
            if (this.mHandler != null) {
                this.mHandler.removeMessages(4);
            }
            if (this.mTouchFocusInterface != null) {
                this.mTouchFocusInterface.releaseFocusHandler();
            }
        }
    }

    public void releaseTouchFocus() {
        if (this.mTouchFocusInterface != null) {
            this.mTouchFocusInterface.releaseTouchFocus();
        }
    }

    protected void sendTakePictureMessage(int arg) {
        if (this.mGet.checkModuleValidate(192)) {
            Message msg = new Message();
            msg.what = 4;
            msg.arg1 = arg;
            this.mHandler.sendMessage(msg);
        }
    }

    public boolean doTouchFocus(int x, int y, boolean bTakePicture) {
        CamLog.m3d(CameraConstants.TAG, "-tf- doTouchFocus : x = " + x + ", y = " + y + " bTakePicture " + bTakePicture);
        if (this.mHandler == null || !this.mGet.checkModuleValidate(31) || this.mFocusState == 12 || this.mFocusState == 13) {
            CamLog.m3d(CameraConstants.TAG, "-tf- doTouchFocus return : mFocusState = " + this.mFocusState);
            return false;
        }
        int screenX = x;
        int screenY = y;
        if (this.mHandler != null) {
            this.mHandler.removeMessages(4);
        }
        if (!checkTouchFocusCondition(x, y, bTakePicture)) {
            return false;
        }
        if (wasLongPress() && bTakePicture) {
            setLongPress(false);
        }
        if (Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
            x -= this.mStartMargin;
            y -= this.mTopMargin;
        } else {
            x -= this.mTopMargin;
            y -= this.mStartMargin;
        }
        int[] correctedCoordinate = touchCoordinateCorrection(x, y);
        if (!startFocusByTouchPress(correctedCoordinate[0], correctedCoordinate[1], bTakePicture)) {
            return true;
        }
        showAEAFText(screenX, screenY);
        if (isAEControlBarEnableCondition()) {
            setManualFocusButtonLocation(screenX, screenY);
            showAEControlBar(true);
            this.mAeControlBar.updateCursorPositon(this.mAeControlBar.getDefaultBarValue());
        }
        CamLog.m3d(CameraConstants.TAG, "-tf- doTouchFocus END");
        return true;
    }

    public int[] touchCoordinateCorrection(int x, int y) {
        int previewBottomMargin;
        int[] coordinate = new int[2];
        int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
        int lcdWidth = lcdSize[0];
        int lcdHeight = lcdSize[1];
        if (!Utils.isConfigureLandscape(getActivity().getResources())) {
            lcdWidth = lcdSize[1];
            lcdHeight = lcdSize[0];
        }
        int focusRectWidth = this.mRectWidth / 2;
        int focusRectHeight = this.mRectHeight / 2;
        Resources resources = this.mGet.getActivity().getResources();
        int degree = this.mGet.getOrientationDegree();
        boolean isLand = Utils.isEqualDegree(resources, degree, 0) || Utils.isEqualDegree(resources, degree, 180);
        if (!isLand) {
            int temp = focusRectWidth;
            focusRectWidth = focusRectHeight;
            focusRectHeight = temp;
        }
        int shutterAreaWith = getShutterAreaWidth();
        int quickButtonWidth = RatioCalcUtil.getQuickButtonWidth(getAppContext());
        int previewTopMargin = quickButtonWidth > this.mStartMargin ? quickButtonWidth : this.mStartMargin;
        if (shutterAreaWith < this.mStartMargin) {
            previewBottomMargin = this.mStartMargin;
        } else {
            previewBottomMargin = shutterAreaWith;
        }
        int touchShiftAreaTop = (((int) (((float) focusRectWidth) * 1.0f)) + previewTopMargin) - this.mStartMargin;
        int touchShiftAreaBottom = ((lcdHeight - checkPreviewBottomMargin(previewBottomMargin)) - ((int) (((float) focusRectWidth) * 1.0f))) - this.mStartMargin;
        int touchShiftAreaStart = ((int) (((float) focusRectHeight) * 1.0f)) - this.mTopMargin;
        int touchShiftAreaEnd = (lcdWidth - ((int) (((float) focusRectHeight) * 1.0f))) - this.mTopMargin;
        if (Utils.isConfigureLandscape(getActivity().getResources())) {
            x = Math.min(Math.max(x, touchShiftAreaTop), touchShiftAreaBottom);
            y = Math.min(Math.max(y, touchShiftAreaStart), touchShiftAreaEnd);
        } else {
            y = Math.min(Math.max(y, touchShiftAreaTop), touchShiftAreaBottom);
            x = Math.min(Math.max(x, touchShiftAreaStart), touchShiftAreaEnd);
        }
        coordinate[0] = x;
        coordinate[1] = y;
        return coordinate;
    }

    private int checkPreviewBottomMargin(int bottomMargin) {
        String settingKey;
        if (this.mGet.isRecordingPriorityMode() || !this.mGet.checkModuleValidate(192)) {
            settingKey = SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId());
        } else {
            settingKey = SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId());
        }
        ListPreference listPref = (ListPreference) this.mGet.getListPreference(settingKey);
        if (listPref == null) {
            return bottomMargin;
        }
        int[] previewScreenSize = Utils.sizeStringToArray(listPref.getExtraInfo(2));
        if (previewScreenSize == null) {
            return bottomMargin;
        }
        int[] lcdSize = Utils.getLCDsize(this.mGet.getAppContext(), true);
        if (lcdSize == null) {
            return bottomMargin;
        }
        int previewBottomMargin = lcdSize[0] - (previewScreenSize[0] + this.mStartMargin);
        if (previewBottomMargin <= bottomMargin) {
            previewBottomMargin = bottomMargin;
        }
        return previewBottomMargin;
    }

    public void cancelTouchAutoFocus() {
        if (this.mTouchFocusInterface != null) {
            CamLog.m3d(CameraConstants.TAG, "cancelAutoFocus");
            this.mTouchFocusInterface.cancelAutoFocus();
            this.mTouchFocusInterface.hideFocus();
        }
        if (getFocusState() != 14) {
            setFocusState(0);
        }
    }

    public void onCameraSwitchingStart() {
        super.onCameraSwitchingStart();
        if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
            onPauseBefore();
            onDestroy();
        }
    }

    public void onCameraSwitchingEnd() {
        super.onCameraSwitchingEnd();
        if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
            init();
            onResumeBefore();
            setRotateDegree(this.mGet.getOrientationDegree(), false);
        }
    }

    public void setRotateDegree(final int degree, boolean animation) {
        super.setRotateDegree(degree, animation);
        CamLog.m3d(CameraConstants.TAG, "FocusManger -- setRotateDegree, degree = " + degree);
        if (this.mMoveFocus != null) {
            this.mMoveFocus.setFocusViewDegree(degree);
        }
        if (this.mFaceFocus != null) {
            this.mFaceFocus.setFocusViewDegree(degree);
        }
        if (this.mTouchFocusInterface != null) {
            this.mTouchFocusInterface.setFocusViewDegree(degree);
        }
        if (this.mAEAFLockLayout != null && this.mAEAFLockLayout.isShown()) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (FocusManager.this.mAEAFLockLayout != null) {
                        if (FocusManager.this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL) || FocusManager.this.mGet.isLightFrameOn()) {
                            CamLog.m3d(CameraConstants.TAG, "hide AE/AF text because menu is visible");
                            FocusManager.this.hideAEAFText();
                        } else if (degree != -1) {
                            CamLog.m3d(CameraConstants.TAG, "call showAEAFText");
                            FocusManager.this.showAEAFText(FocusManager.this.mPrevX, FocusManager.this.mPrevY);
                        }
                        FocusManager.this.mAEAFLockLayout.rotateLayout(degree);
                    }
                }
            });
        }
    }

    public boolean isFaceDetectionStarted() {
        if (this.mFaceFocus == null) {
            return false;
        }
        return this.mFaceFocus.isFaceDetectionStarted();
    }

    public void setLongPress(boolean bLongPress) {
        this.mWasLongPress = bLongPress;
    }

    public boolean wasLongPress() {
        return this.mWasLongPress;
    }

    public void setIsOnlyTAF(boolean set) {
        this.mIsOnlyTAF = set;
    }

    public boolean getIsOnlyTAF() {
        return this.mIsOnlyTAF;
    }

    private void createAEAFLockView() {
        if (this.mGet != null) {
            CamLog.m3d(CameraConstants.TAG, "createAEAFLockView");
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
            this.mBaseView = this.mGet.inflateView(C0088R.layout.ae_af_lock_guide);
            if (this.mBaseView != null && vg != null) {
                vg.addView(this.mBaseView);
                this.mAFLockTextView = (TextView) this.mBaseView.findViewById(C0088R.id.ae_af_lock_txt);
                this.mAFLockTextView.setText(this.mGet.getAppContext().getString(C0088R.string.ae_af_lock));
                this.mAEAFLockLayout = (RotateLayout) this.mBaseView.findViewById(C0088R.id.ae_af_lock_rotate_layout);
            }
        }
    }

    protected boolean doAEAFLock(int posX, int posY) {
        if (!this.mGet.canUseAEAFLock() || !checkIsInPreview(posX, posY) || Utils.isIgnoreTouchEvent(this.mGet.getAppContext(), this.mTouchStartX, this.mTouchStartY) || isTouchInUIArea(posX, posY) || (this.mTouchStartX == 0 && this.mTouchStartY == 0)) {
            return false;
        }
        long timeDiff2 = System.currentTimeMillis() - this.mPrevTouchDownTime;
        int xDiff = Math.abs(this.mTouchStartX - posX);
        int yDiff = Math.abs(this.mTouchStartY - posY);
        if (((float) timeDiff2) > 500.0f) {
            if (xDiff >= 100 || yDiff >= 100) {
                CamLog.m3d(CameraConstants.TAG, "AEAFLock fail");
                this.mTouchStartX = 0;
                this.mTouchStartY = 0;
                return false;
            } else if ((this.mGet.getCameraId() == 0 && !isFocusLock()) || !(this.mGet.getCameraId() == 0 || isAELock())) {
                CamLog.m3d(CameraConstants.TAG, "DoAEAFLock success");
                this.mCheckAEAFLock = false;
                this.mCheckTracking = false;
                doTouchMoveDown(posX, posY);
                setAEAFLock(true);
                setTrackingFocusState(false);
                setState(16, true);
                doTouchFocus(posX, posY, false);
                return true;
            }
        }
        return false;
    }

    private boolean checkTouchFocusCondition(int x, int y, boolean bTakePicture) {
        if (bTakePicture && this.mGet.isRearCamera()) {
            if (!this.mGet.isAFSupported() && checkIsInPreview(x, y)) {
                sendTakePictureMessage(0);
                return false;
            } else if (!this.mGet.isMWAFSupported() && isInFocusArea(x, y)) {
                sendTakePictureMessage(1);
                return false;
            }
        } else if (!this.mGet.isRearCamera() && bTakePicture && checkIsInPreview(x, y)) {
            sendTakePictureMessage(0);
            return false;
        }
        return true;
    }

    public boolean checkTouchFocusCancelCondition(int x, int y) {
        if (checkIsInPreview(x, y) && !Utils.isIgnoreTouchEvent(this.mGet.getAppContext(), this.mTouchStartX, this.mTouchStartY) && !isTouchInUIArea(x, y)) {
            CamLog.m3d(CameraConstants.TAG, "-tf- focus cancel");
            if (this.mCheckTracking) {
                resetTrackingFocus();
                return false;
            } else if (!this.mCheckAEAFLock) {
                return true;
            } else {
                resetAEAFFocus();
                return false;
            }
        } else if (checkIsInPreview(this.mTouchStartX, this.mTouchStartY)) {
            CamLog.m3d(CameraConstants.TAG, "-tf- 16 by 9 cancel");
            if (this.mCheckTracking) {
                resetTrackingFocus();
                return false;
            } else if (this.mCheckAEAFLock) {
                resetAEAFFocus();
                return false;
            } else if (this.mGet.isAFSupported()) {
                registerCallback();
                return false;
            } else {
                releaseTouchFocus();
                return false;
            }
        } else {
            CamLog.m3d(CameraConstants.TAG, "-tf- 4 by 3, 1 by 1 not cancel");
            return false;
        }
    }

    public void hideAEAFText() {
        if (this.mAFLockTextView != null) {
            CamLog.m3d(CameraConstants.TAG, "hideAEAFText");
            this.mAFLockTextView.setVisibility(8);
            return;
        }
        CamLog.m11w(CameraConstants.TAG, "cannot hideAEAFText, mAFLockTextView is null");
    }

    private void calRectSize() {
        int touchResId = this.mGet.isRearCamera() ? C0088R.drawable.focus_touch_taf : C0088R.drawable.camera_focus_front_ae;
        Drawable drawable = touchResId > 0 ? this.mGet.getAppContext().getResources().getDrawable(touchResId) : null;
        if (drawable != null) {
            this.mRectWidth = drawable.getIntrinsicWidth();
            this.mRectHeight = drawable.getIntrinsicHeight();
            Utils.setIgnoreTouchArea(this.mRectWidth);
            CamLog.m3d(CameraConstants.TAG, "init mRectWidth = " + this.mRectWidth + " mRectWidth = " + this.mRectWidth);
        }
    }

    private void removeView() {
        CamLog.m3d(CameraConstants.TAG, "removeView");
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (vg != null && this.mBaseView != null) {
            vg.removeView(this.mBaseView);
            MemoryUtils.releaseViews(this.mBaseView);
            this.mBaseView = null;
            this.mAFLockTextView = null;
        }
    }

    public void resetAEAFFocus() {
        CamLog.m3d(CameraConstants.TAG, "resetAEAFFocus");
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(10);
        releaseTouchFocus();
        setAEAFLock(false);
        hideAEAFText();
        resetEVValue(0, false);
    }

    private void processTouchUpKey(int posX, int posY) {
        if (checkTouchFocusCancelCondition(posX, posY)) {
            doTouchFocus(posX, posY, false);
        }
    }

    protected boolean startFocusByTouchPress(int x, int y, boolean bTakePicture) {
        if (!this.mIsManualFocus || this.mGet.isManualFocusModeEx()) {
            if (this.mGet.isSettingMenuVisible()) {
                this.mGet.removeSettingMenu(false, false);
                this.mGet.hideModeMenu(true, false);
                this.mGet.hideHelpList(true, false);
            }
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(10);
            if (this.mGet.isManualFocusModeEx()) {
                hideFocusForce();
                setManualFocusButtonVisibility(false);
                this.mGet.setManualFocusModeEx(false);
                registerEVCallback(true, false);
                return false;
            }
            int xPos = x;
            int yPos = y;
            boolean isTakePicture = bTakePicture;
            CamLog.m3d(CameraConstants.TAG, "-tf- mIsManualFocus = " + this.mIsManualFocus);
            if (this.mTouchFocusInterface != null && this.mGet.isFocusEnableCondition()) {
                CamLog.m3d(CameraConstants.TAG, "-tf- Focus state = " + this.mGet.getFocusState() + " isInFocusArea = " + isInFocusArea(xPos, yPos));
                if (this.mGet.isFocusLock()) {
                    this.mTouchFocusInterface.startFocusByTouchPress(xPos, yPos, false);
                } else {
                    boolean isTrackingAFEnabled = isTrackingState();
                    setTrackingFocusState(isTrackingAFEnabled);
                    if (isTrackingAFEnabled) {
                        this.mTouchFocusInterface.startFocusByTouchPressForTracking(xPos, yPos);
                    } else {
                        this.mTouchFocusInterface.startFocusByTouchPress(xPos, yPos, isTakePicture);
                    }
                }
            }
        }
        return true;
    }

    public void drawTrackingAF(final Rect rect) {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (FocusManager.this.mCameraFocusView != null && FocusManager.this.mGet.getFocusState() == 14) {
                    if (!FocusManager.this.checkDrawingAFTrackingCondition()) {
                        FocusManager.this.hideTrackingAF();
                    } else if (rect.left > 0 && rect.top > 0 && rect.right > 0 && rect.bottom > 0) {
                        FocusManager.this.mObjectOutOfScreenTime = 0;
                        int x = rect.left + (rect.right / 2);
                        int y = rect.top + (rect.bottom / 2);
                        Point point = new Point(x, y);
                        CamLog.m3d(CameraConstants.TAG, "-focus- center x = " + x + " y = " + y);
                        if (FocusManager.this.mCameraFocusView.getVisibility() != 0) {
                            FocusManager.this.mCameraFocusView.setVisibility(0);
                            LdbUtil.sendLDBIntent(FocusManager.this.getAppContext(), LdbConstants.LDB_FEATURE_NAME_TRACKING_FOCUS);
                        }
                        Point previewPoint = FocusManager.this.changeToPreviewCoordinate(point);
                        if (FocusManager.this.mTouchFocusInterface != null) {
                            if (Math.abs(FocusManager.this.mPreTrackingPoint.x - previewPoint.x) > 10 || Math.abs(FocusManager.this.mPreTrackingPoint.y - previewPoint.y) > 10) {
                                FocusManager.this.mTouchFocusInterface.setMoveNormalFocusRect(previewPoint.x, previewPoint.y, false);
                            }
                            FocusManager.this.mPreTrackingPoint = previewPoint;
                        }
                        FocusManager.this.mPointX = point.x;
                        FocusManager.this.mPointY = point.y;
                    } else if (FocusManager.this.mPointX == 0 && FocusManager.this.mPointY == 0) {
                        CamLog.m3d(CameraConstants.TAG, "Object is not focused");
                        FocusManager.this.resetTrackingFocus();
                    } else {
                        FocusManager focusManager = FocusManager.this;
                        focusManager.mObjectOutOfScreenTime++;
                        FocusManager.this.hideTrackingAF();
                        if (FocusManager.this.mObjectOutOfScreenTime > 45) {
                            CamLog.m3d(CameraConstants.TAG, "Object is out of screen for long time. TurnOff tracking");
                            FocusManager.this.resetTrackingFocus();
                            FocusManager.this.mPointX = 0;
                            FocusManager.this.mPointY = 0;
                        }
                    }
                }
            }
        });
    }

    public void setTrackingFocusState(boolean enable) {
        CamLog.m3d(CameraConstants.TAG, "setTrackingFocusState : " + enable);
        if (!this.mGet.isFocusTrackingSupported() || this.mGet == null) {
            return;
        }
        if (enable) {
            setFocusState(14);
        } else if (this.mGet.getFocusState() == 14) {
            setFocusState(0);
            hideTrackingAF();
            if (this.mTouchFocusInterface != null) {
                this.mTouchFocusInterface.startFocusByTouchPressForTracking(-1, -1);
            }
        }
    }

    public void hideTrackingAF() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (FocusManager.this.isTrackingState() && FocusManager.this.mCameraFocusView != null && FocusManager.this.mCameraFocusView.getVisibility() != 8) {
                    CamLog.m3d(CameraConstants.TAG, "-focus- hideTrackingAF");
                    FocusManager.this.mCameraFocusView.setVisibility(8);
                }
            }
        });
    }

    public void resetTrackingFocus() {
        boolean z = false;
        CamLog.m3d(CameraConstants.TAG, "resetTrackingFocus");
        setTrackingFocusState(false);
        if (this.mGet.checkModuleValidate(128)) {
            registerCallback();
            if (CameraConstants.MODE_MANUAL_CAMERA.equals(this.mGet.getShotMode())) {
                if (!isManualFocusMode()) {
                    z = true;
                }
                setAFPointVisible(z);
                return;
            }
            return;
        }
        cancelTouchAutoFocus();
        setIsOnlyTAF(false);
        this.mGet.setCameraFocusMode(ParamConstants.FOCUS_MODE_CONTINUOUS_VIDEO);
    }

    public boolean checkDrawingAFTrackingCondition() {
        if (this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL) || this.mGet.isModuleChanging() || this.mGet.isAnimationShowing() || this.mGet.getPreviewCoverVisibility() == 0 || !this.mGet.checkModuleValidate(64) || this.mGet.getCameraState() == 0) {
            return false;
        }
        return true;
    }

    public Point changeToPreviewCoordinate(Point point) {
        int x = (int) (((float) (this.mPreviewSizeHeight - point.y)) * (((float) this.mPreviewWidthOnScreen) / ((float) this.mPreviewSizeHeight)));
        int y = (int) (((float) point.x) * (((float) this.mPreviewHeightOnScreen) / ((float) this.mPreviewSizeWidth)));
        CamLog.m3d(CameraConstants.TAG, "-focus- x = " + x + " y = " + y);
        return new Point(x, y);
    }

    public void setPreviewSizeForAF() {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null) {
            CameraParameters param = cameraDevice.getParameters();
            if (param != null) {
                Size previeSize = param.getPreviewSize();
                if (previeSize != null) {
                    this.mPreviewSizeWidth = previeSize.getWidth();
                    this.mPreviewSizeHeight = previeSize.getHeight();
                    CamLog.m3d(CameraConstants.TAG, "-focus- mPreviewSizeWidth = " + this.mPreviewSizeWidth + " mPreviewSizeHeight = " + this.mPreviewSizeHeight);
                }
            }
        }
    }
}
