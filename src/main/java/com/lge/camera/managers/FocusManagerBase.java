package com.lge.camera.managers;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.CameraFocusFaceView;
import com.lge.camera.components.CameraFocusNormalView;
import com.lge.camera.components.CameraManualModeFocusMultiWindowView;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ICameraCallback.CameraAFCallback;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.TalkBackUtil;
import com.lge.camera.util.Utils;

public class FocusManagerBase extends ManagerInterfaceImpl {
    public static final int AE_BAR_CONTROLING_THRESHOLD = 50;
    public static final int HANDLER_ARG_FALSE = 0;
    public static final int HANDLER_ARG_TRUE = 1;
    public static final int HANDLER_CANCEL_AUTOFOCUS_MSG = 5;
    public static final int HANDLER_FACE_DETECTED_MSG = 2;
    public static final int HANDLER_FACE_DETECT_START_MSG = 6;
    public static final int HANDLER_FACE_DRAW_START = 8;
    public static final int HANDLER_FACE_DRAW_STOP = 9;
    public static final int HANDLER_FACE_NOT_DETECTED_MSG = 3;
    public static final int HANDLER_MANUALFOCUS_BUTTON_VISIBLE_MSG = 11;
    public static final int HANDLER_REGISTER_AUTOFOCUS_MSG = 1;
    public static final int HANDLER_REGISTER_FACE_DETECT_MSG = 10;
    public static final int HANDLER_TOUCH_FOCUS_INRECORDING_MSG = 7;
    public static final int HANDLER_TOUCH_TAKE_PICTURE_MSG = 4;
    protected static final int OVER_BOTTOM = 8;
    protected static final int OVER_LEFT = 1;
    protected static final int OVER_RIGHT = 4;
    protected static final int OVER_TOP = 2;
    protected static final int STATE_AE_AF_FOCUS_JUST_LOCKED = 16;
    protected static final int STATE_AE_AF_LOCKED = 12;
    protected static final int STATE_AE_BAR_SHOWING = 1;
    protected static final int STATE_AE_BAR_TOUCHED = 2;
    protected static final int STATE_AE_LOCKED = 8;
    protected static final int STATE_FOCUS_ALL = 31;
    protected static final int STATE_FOCUS_LOCKED = 4;
    protected CameraFocusNormalView mCameraFocusView = null;
    protected CameraManualModeFocusMultiWindowView mCameraManualMultiFocusView = null;
    protected RotateImageButton mEVShutterButton = null;
    protected OnClickListener mEVShutterClickListener = new C07972();
    protected CameraFocusFaceView mFaceDetectView = null;
    protected FocusFace mFaceFocus = null;
    protected FocusManagerInterface mFocusManagerInterface = null;
    protected int mFocusState = 0;
    protected final Handler mHandler = new C07961();
    protected boolean mInitFocus = false;
    private boolean mIsAFPointVisible = false;
    protected boolean mIsManualFocus = false;
    protected RotateImageButton mManualFocusButton;
    protected LayoutParams mManualFocusButtonParam;
    protected View mManualFocusButtonView;
    protected FocusMove mMoveFocus = null;
    protected int mPreviewHeightOnScreen = 0;
    protected int mPreviewSizeHeight = 0;
    protected int mPreviewSizeWidth = 0;
    protected int mPreviewWidthOnScreen = 0;
    protected int mRectHeight = 0;
    protected int mRectWidth = 0;
    protected int mStartMargin = 0;
    protected int mState = 0;
    protected int mTopMargin = 0;
    protected int mTouchAreaHeight = 0;
    protected int mTouchAreaWidth = 0;
    protected FocusTouchInterface mTouchFocusInterface = null;
    protected int mTouchStartX = 0;
    protected int mTouchStartY = 0;

    public interface FocusManagerInterface {
        void doTouchAFInRecording();

        void doTouchShot();

        boolean isShoToShotJustEnd();

        boolean isSupportFaceFocusModule();

        void setTrackingFocusState(boolean z);
    }

    /* renamed from: com.lge.camera.managers.FocusManagerBase$1 */
    class C07961 extends Handler {
        C07961() {
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            boolean isTaf = false;
            CamLog.m7i(CameraConstants.TAG, "handleMessage what =" + msg.what);
            switch (msg.what) {
                case 1:
                    if (FocusManagerBase.this.isManualFocusMode() || AppControlUtil.isNeedQuickShotTaking() || FocusManagerBase.this.mGet.checkCameraChanging(4) || FocusManagerBase.this.mGet.getCameraState() < 1 || FocusManagerBase.this.mGet.isMultishotState(1)) {
                        CamLog.m7i(CameraConstants.TAG, "Focus can't regist on Manual focus or quick shot mode or burst shot state, and camera state : " + FocusManagerBase.this.mGet.getCameraState());
                        return;
                    }
                    if (FocusManagerBase.this.mTouchFocusInterface != null) {
                        CamLog.m3d(CameraConstants.TAG, "cancelAutoFocus");
                        FocusManagerBase.this.mTouchFocusInterface.cancelAutoFocus();
                        FocusManagerBase.this.mTouchFocusInterface.hideFocus();
                    }
                    if (FocusManagerBase.this.mGet.isRecordingPriorityMode()) {
                        CameraProxy cameraDevice = FocusManagerBase.this.mGet.getCameraDevice();
                        if (cameraDevice != null) {
                            CameraParameters parameters = FocusManagerBase.this.mGet.getCameraDevice().getParameters();
                            if (parameters != null) {
                                parameters.setFocusMode(ParamConstants.FOCUS_MODE_CONTINUOUS_VIDEO);
                                try {
                                    cameraDevice.setParameters(parameters);
                                } catch (RuntimeException e) {
                                    CamLog.m5e(CameraConstants.TAG, "setParameters failed: " + e);
                                }
                            }
                        } else {
                            return;
                        }
                    } else if (FocusManagerBase.this.mMoveFocus != null && FocusManagerBase.this.mGet.isRearCamera() && FocusManagerBase.this.mGet.checkModuleValidate(192)) {
                        FocusManagerBase.this.mMoveFocus.cancelAutoFocus();
                        FocusManagerBase.this.mMoveFocus.registerCallback();
                        if (FocusManagerBase.this.isAFPointVisible() && !FocusManagerBase.this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL)) {
                            FocusManagerBase.this.mCameraManualMultiFocusView.manualModeFocusViewInit();
                            if (!(FocusManagerBase.this.mGet.isZoomBarVisible() || FocusManagerBase.this.mGet.isJogZoomMoving())) {
                                FocusManagerBase.this.mMoveFocus.showFocus();
                            }
                        }
                    }
                    FocusManagerBase.this.setFocusState(0);
                    return;
                case 2:
                    int focusState = FocusManagerBase.this.getFocusState();
                    if (focusState >= 1 && focusState <= 4) {
                        isTaf = true;
                    }
                    if (FocusManagerBase.this.mMoveFocus != null && !isTaf) {
                        FocusManagerBase.this.mMoveFocus.hideFocus();
                        FocusManagerBase.this.mMoveFocus.setFaceDetected(true);
                        return;
                    }
                    return;
                case 3:
                    if (FocusManagerBase.this.mMoveFocus != null) {
                        FocusManagerBase.this.mMoveFocus.setFaceDetected(false);
                        return;
                    }
                    return;
                case 4:
                    CamLog.m3d(CameraConstants.TAG, "HANDLER_TOUCH_TAKE_PICTURE_MSG. mFocusState = " + FocusManagerBase.this.mFocusState);
                    int isJustShot = msg.arg1;
                    if (FocusManagerBase.this.mFocusManagerInterface != null) {
                        FocusManagerBase.this.mFocusManagerInterface.doTouchShot();
                        if (isJustShot == 1) {
                            FocusManagerBase.this.mMoveFocus.setFocusRectangle(0, 0, 0, 0);
                            return;
                        }
                        return;
                    }
                    return;
                case 5:
                    if (FocusManagerBase.this.mGet.getCameraDevice() != null) {
                        FocusManagerBase.this.stopAutoFocus();
                        return;
                    }
                    return;
                case 6:
                    if (FocusManagerBase.this.mFaceFocus != null) {
                        FocusManagerBase.this.mFaceFocus.startFaceDetection();
                    }
                    if (FocusManagerBase.this.checkFocusOnShutterButton()) {
                        FocusManagerBase.this.setFocusState(0);
                        return;
                    }
                    return;
                case 7:
                    if (FocusManagerBase.this.mFocusManagerInterface != null) {
                        FocusManagerBase.this.mFocusManagerInterface.doTouchAFInRecording();
                        return;
                    }
                    return;
                case 8:
                    if (FocusManagerBase.this.mFocusManagerInterface != null && FocusManagerBase.this.mFocusManagerInterface.isSupportFaceFocusModule()) {
                        FocusManagerBase.this.mFaceFocus.setEnableDrawRect(true);
                        FocusManagerBase.this.mHandler.sendEmptyMessageDelayed(9, CameraConstants.TOAST_LENGTH_SHORT);
                        return;
                    }
                    return;
                case 9:
                    if (msg.arg1 == 2) {
                        FocusManagerBase.this.mFaceFocus.setEnableDrawRect(false);
                        return;
                    } else {
                        FocusManagerBase.this.mFaceFocus.hideFaceDetectedRectAnimation();
                        return;
                    }
                case 10:
                    if (FocusManagerBase.this.mFaceFocus != null) {
                        FocusManagerBase.this.mFaceFocus.startFaceDetection();
                        return;
                    }
                    return;
                case 11:
                    FocusManagerBase focusManagerBase = FocusManagerBase.this;
                    if (msg.arg1 != 1) {
                        z = false;
                    }
                    focusManagerBase.setManualFocusButtonVisibility(z);
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.lge.camera.managers.FocusManagerBase$2 */
    class C07972 implements OnClickListener {
        C07972() {
        }

        public void onClick(View arg0) {
            FocusManagerBase.this.mGet.onCameraShutterButtonClicked();
        }
    }

    /* renamed from: com.lge.camera.managers.FocusManagerBase$3 */
    class C07983 implements CameraAFCallback {
        C07983() {
        }

        public void onAutoFocus(boolean focused, CameraProxy camera) {
            CamLog.m7i(CameraConstants.TAG, "### CameraAFCallback getFocusState = " + FocusManagerBase.this.mFocusState + ", focused = " + focused);
            if (FocusManagerBase.this.mFocusState == 12 && FocusManagerBase.this.mHandler != null) {
                FocusManagerBase.this.setFocusState(13);
                if (FocusManagerBase.this.mFocusManagerInterface == null || !FocusManagerBase.this.mFocusManagerInterface.isShoToShotJustEnd()) {
                    FocusManagerBase.this.mHandler.sendEmptyMessage(4);
                } else {
                    FocusManagerBase.this.releaseTouchFocus();
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.FocusManagerBase$4 */
    class C07994 implements OnClickListener {
        C07994() {
        }

        public void onClick(View arg0) {
            boolean z = true;
            boolean oldState = FocusManagerBase.this.mGet.isManualFocusModeEx();
            if (oldState) {
                FocusManagerBase.this.registerEVCallback(true, false);
            } else if (FocusManagerBase.this.isFocusLock() || FocusManagerBase.this.isAELock()) {
                CameraProxy cameraDevice = FocusManagerBase.this.mGet.getCameraDevice();
                if (cameraDevice != null) {
                    CameraDeviceUtils.setEnable3ALocks(cameraDevice, false, false);
                } else {
                    return;
                }
            }
            ModuleInterface moduleInterface = FocusManagerBase.this.mGet;
            if (oldState) {
                z = false;
            }
            moduleInterface.setManualFocusModeEx(z);
            FocusManagerBase.this.mManualFocusButton.setImageResource(oldState ? C0088R.drawable.btn_camera_mf_normal : C0088R.drawable.btn_camera_mf_pressed);
            TalkBackUtil.setTalkbackDescOnDoubleTap(FocusManagerBase.this.mGet.getAppContext(), FocusManagerBase.this.mGet.getAppContext().getString(C0088R.string.manual_focus) + " " + FocusManagerBase.this.mGet.getAppContext().getString(oldState ? C0088R.string.off : C0088R.string.on));
            FocusManagerBase.this.updateManualFocusLayout();
        }
    }

    public FocusManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setState(int flag, boolean set) {
        if (set) {
            this.mState |= flag;
        } else {
            this.mState &= flag ^ -1;
        }
    }

    public boolean getState(int flag) {
        return (this.mState & flag) > 0;
    }

    public void onResumeBefore() {
        setState(12, false);
        setManualFocusButtonEnable(true);
        setEVshutterButtonEnable(true);
    }

    public void onPauseBefore() {
        setState(12, false);
        this.mInitFocus = false;
    }

    public void initializeFocusManagerByForce() {
        if (FunctionProperties.isSupportedInAndOutZoom()) {
            releaseFocus();
            this.mInitFocus = false;
            createFocus();
            initLayout();
        }
    }

    protected void initLayout() {
        CamLog.m3d(CameraConstants.TAG, "initLayout");
        View previewFrameLayout = this.mGet.getPreviewFrameLayout();
        if (previewFrameLayout != null) {
            this.mCameraFocusView = (CameraFocusNormalView) previewFrameLayout.findViewById(C0088R.id.camera_focus_view);
            this.mCameraFocusView.setSupportedMultiFocus(this.mGet.isMWAFSupported(), getPreviewSizeOnScreen());
            this.mCameraFocusView.getSupportedTrackingFocus(this.mGet.isFocusTrackingSupported());
            this.mCameraManualMultiFocusView = (CameraManualModeFocusMultiWindowView) previewFrameLayout.findViewById(C0088R.id.manualmultifocus_af_parent);
            this.mFaceDetectView = (CameraFocusFaceView) previewFrameLayout.findViewById(C0088R.id.face_focus_view);
            this.mEVShutterButton = (RotateImageButton) previewFrameLayout.findViewById(C0088R.id.ev_shutter_button);
            this.mEVShutterButton.setOnClickListener(this.mEVShutterClickListener);
            if (this.mMoveFocus != null) {
                if (this.mGet.isMWAFSupported()) {
                    this.mMoveFocus.initCameraFocusView(this.mCameraManualMultiFocusView, this.mCameraManualMultiFocusView);
                    if (this.mCameraManualMultiFocusView != null) {
                        this.mMoveFocus.setCameraMultiFocusView(this.mCameraManualMultiFocusView, false);
                    }
                } else {
                    this.mMoveFocus.setCameraFocusView(this.mCameraFocusView);
                }
            }
            if (this.mFaceFocus != null) {
                this.mFaceFocus.setCameraFocusView(this.mFaceDetectView);
            }
            if (this.mTouchFocusInterface != null) {
                this.mTouchFocusInterface.setCameraFocusView(this.mCameraFocusView);
                this.mTouchFocusInterface.setCameraFocusView(this.mEVShutterButton);
            }
        }
    }

    public void init() {
        super.init();
        if (FunctionProperties.isSupportedManualFocus()) {
            initManualFocusButtonView();
        }
    }

    private int[] getPreviewSizeOnScreen() {
        if (this.mMoveFocus != null) {
            return this.mMoveFocus.getPreviewSizeOnScreen();
        }
        if (this.mFaceFocus != null) {
            return this.mFaceFocus.getPreviewSizeOnScreen();
        }
        return null;
    }

    protected void createFocus() {
        if (this.mMoveFocus == null) {
            this.mMoveFocus = new FocusMove(this.mGet);
            this.mMoveFocus.initFocusRectangle();
            this.mMoveFocus.setFocusManagerHandler(this.mHandler);
        }
        if (this.mFaceFocus == null) {
            this.mFaceFocus = new FocusFace(this.mGet);
            this.mFaceFocus.setFocusManagerHandler(this.mHandler);
        }
        if (this.mTouchFocusInterface == null) {
            if ((this.mGet.isAFSupported() || !this.mGet.isRearCamera()) && this.mGet.isRearCamera()) {
                this.mTouchFocusInterface = new FocusTouch(this.mGet);
                this.mTouchFocusInterface.setFocusManagerHandler(this.mHandler);
                CamLog.m7i(CameraConstants.TAG, "create Focus camera ID = TouchAF = " + this.mTouchFocusInterface);
            } else {
                CamLog.m7i(CameraConstants.TAG, "create Focus camera ID = TouchAE");
                this.mTouchFocusInterface = new FocusTouchAE(this.mGet);
                this.mTouchFocusInterface.setFocusManagerHandler(this.mHandler);
            }
        }
        setFocusState(0);
    }

    public int getFocusState() {
        return this.mFocusState;
    }

    public void setFocusState(int focusState) {
        CamLog.m7i(CameraConstants.TAG, "FocusManager setFocusState = " + focusState);
        this.mFocusState = focusState;
    }

    public boolean doFocusCaf() {
        CamLog.m7i(CameraConstants.TAG, "### doFocusCaf focus state = " + this.mFocusState);
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice == null || !this.mGet.checkModuleValidate(32) || this.mFocusState == 12 || this.mFocusState == 2) {
            CamLog.m3d(CameraConstants.TAG, "doFocusCaf return");
            return false;
        } else if (this.mFocusState == 1) {
            setFocusState(2);
            return true;
        } else {
            setFocusState(12);
            cameraDevice.autoFocus(this.mGet.getHandler(), new C07983());
            return true;
        }
    }

    public void doFocusLock() {
        CamLog.m7i(CameraConstants.TAG, "doFocusLock");
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null) {
            cameraDevice.cancelAutoFocus();
            cameraDevice.autoFocus(this.mGet.getHandler(), null);
        }
    }

    public boolean checkEnableTakePicture() {
        CamLog.m7i(CameraConstants.TAG, "checkEnableTakePicture + focus state = " + this.mFocusState);
        switch (this.mFocusState) {
            case 0:
                if (AppControlUtil.isNeedQuickShotTaking()) {
                    return false;
                }
                return true;
            case 2:
            case 3:
            case 4:
            case 6:
            case 9:
            case 11:
            case 13:
            case 14:
                return true;
            default:
                if (this.mGet.isAFSupported()) {
                    return false;
                }
                return true;
        }
    }

    public boolean checkFocusOnShutterButton() {
        CamLog.m7i(CameraConstants.TAG, "checkFocusOnShutterButton + focus state = " + this.mFocusState);
        switch (this.mFocusState) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 11:
            case 12:
            case 13:
                return false;
            default:
                return true;
        }
    }

    public boolean checkFocusStateForChangingSetting() {
        CamLog.m7i(CameraConstants.TAG, "checkFocusStateForChangingSetting + focus state = " + this.mFocusState);
        switch (this.mFocusState) {
            case 1:
            case 2:
            case 12:
            case 13:
                return false;
            default:
                return true;
        }
    }

    protected void releaseFocus() {
        setFocusState(0);
        CamLog.m7i(CameraConstants.TAG, "releaseFocus");
        if (this.mFaceFocus != null) {
            this.mFaceFocus.unregisterCallback();
            this.mFaceFocus.release();
            this.mFaceFocus = null;
        }
        if (this.mTouchFocusInterface != null) {
            this.mTouchFocusInterface.release();
            this.mTouchFocusInterface = null;
        }
        if (this.mMoveFocus != null) {
            this.mMoveFocus.setFaceDetected(false);
            this.mMoveFocus.unregisterCallback();
            this.mMoveFocus.release();
            this.mMoveFocus = null;
        }
        if (this.mGet.isManualFocusModeEx()) {
            this.mGet.setManualFocusModeEx(false);
            this.mGet.setManualFocusButtonVisibility(false);
        }
    }

    public boolean initAFView() {
        boolean retVal = true;
        if (this.mMoveFocus == null || this.mFaceFocus == null || this.mTouchFocusInterface == null) {
            this.mInitFocus = false;
            return false;
        }
        boolean initResult;
        if (this.mMoveFocus != null) {
            initResult = this.mMoveFocus.initAFView();
            CamLog.m3d(CameraConstants.TAG, "mMoveFocus init : " + initResult);
            retVal = true & initResult;
        }
        if (this.mFaceFocus != null) {
            initResult = this.mFaceFocus.initAFView();
            CamLog.m3d(CameraConstants.TAG, "mFaceFocus init : " + initResult);
            retVal &= initResult;
        }
        if (this.mTouchFocusInterface != null) {
            initResult = this.mTouchFocusInterface.initAFView();
            CamLog.m3d(CameraConstants.TAG, "mTouchFocusInterface init : " + initResult);
            retVal &= initResult;
        }
        this.mInitFocus = retVal;
        CamLog.m3d(CameraConstants.TAG, "all fous init retVal = " + retVal);
        if (retVal) {
            setFocusAreaWindow(this.mPreviewWidthOnScreen, this.mPreviewHeightOnScreen, this.mStartMargin, this.mTopMargin);
            setRotateDegree(this.mGet.getOrientationDegree(), false);
        }
        return retVal;
    }

    public void registerCallback() {
        registerCallback(1);
    }

    public void registerCallback(int isUseFaceFocus) {
        CamLog.m3d(CameraConstants.TAG, "register callback : mInitFocus = " + this.mInitFocus);
        if (isFocusLock()) {
            CamLog.m3d(CameraConstants.TAG, "Can't register callback as locked focus");
        } else if (isAELock() && !this.mGet.isAFSupported()) {
            CamLog.m3d(CameraConstants.TAG, "Can't register callback as locked AE focus");
        } else if (getFocusState() == 14) {
            CamLog.m3d(CameraConstants.TAG, "Can't register callback as FocusTracking");
        } else {
            if (this.mInitFocus) {
                if (!this.mGet.checkModuleValidate(192) || isManualFocusMode()) {
                    return;
                }
            } else if (!initAFView()) {
                return;
            }
            if (this.mGet.isAFSupported() && this.mHandler != null) {
                this.mHandler.removeMessages(1);
                this.mHandler.sendEmptyMessage(1);
            }
            if (isUseFaceFocus == 1 && this.mHandler != null) {
                this.mHandler.removeMessages(10);
                this.mHandler.sendEmptyMessage(10);
            }
            if (!this.mGet.isAFSupported() && this.mTouchFocusInterface != null) {
                this.mTouchFocusInterface.initFocusAreas();
            }
        }
    }

    public void registerCallback(boolean checkFocusState) {
        if (!checkFocusState || (this.mFocusState != 1 && this.mFocusState != 12)) {
            registerCallback();
        }
    }

    public boolean isManualFocusControlShow() {
        return true;
    }

    public void showFocusMove() {
        this.mCameraManualMultiFocusView.manualModeFocusViewInit();
        this.mMoveFocus.showFocus();
    }

    public void setManualFocus(boolean bManual) {
        this.mIsManualFocus = bManual;
    }

    public boolean isManualFocusMode() {
        return this.mIsManualFocus;
    }

    public void stopAutoFocus() {
        stopAutoFocus(true);
    }

    public void stopFaceDetection() {
        if (this.mFaceFocus != null) {
            this.mFaceFocus.cancelAutoFocus();
            this.mFaceFocus.stopFaceDetection();
        }
    }

    public void stopAutoFocus(boolean continueFD) {
        if (!(this.mFaceFocus == null || continueFD)) {
            this.mFaceFocus.stopFaceDetection();
        }
        if (this.mMoveFocus != null) {
            this.mMoveFocus.cancelAutoFocus();
        }
        if (this.mTouchFocusInterface != null) {
            CamLog.m3d(CameraConstants.TAG, "cancelAutoFocus");
            this.mTouchFocusInterface.cancelAutoFocus();
        }
    }

    public void showAllFocus() {
        if (this.mFaceFocus != null) {
            this.mFaceFocus.showFocus();
            this.mFaceFocus.startFaceDetection();
        }
        if (this.mMoveFocus != null) {
            this.mMoveFocus.showFocus();
        }
        if (this.mTouchFocusInterface != null) {
            this.mTouchFocusInterface.showFocus();
        }
    }

    public void showFocus() {
        if (this.mTouchFocusInterface != null) {
            this.mTouchFocusInterface.showFocus();
        }
    }

    public void hideAllFocus() {
        hideAllFocus(false);
    }

    public void hideAllFocus(boolean continueFD) {
        if (!isFocusLock() && !isAELock()) {
            if (this.mMoveFocus != null) {
                this.mMoveFocus.hideFocus();
            }
            if (!(this.mFaceFocus == null || continueFD)) {
                this.mFaceFocus.stopFaceDetection();
            }
            if (this.mTouchFocusInterface != null) {
                this.mTouchFocusInterface.hideFocus();
            }
            hideAEAFText();
            hideTouchMove();
        }
    }

    public void hideAndCancelAllFocus(boolean checkCaptureProgress) {
        hideAndCancelAllFocus(checkCaptureProgress, true, false);
    }

    public void hideAndCancelAllFocus(boolean checkCaptureProgress, boolean unregisterMoveCallback, boolean continueFD) {
        if (!checkCaptureProgress || this.mGet.checkModuleValidate(16)) {
            boolean isRecordingState;
            if (this.mGet.checkModuleValidate(128)) {
                isRecordingState = false;
            } else {
                isRecordingState = true;
            }
            if (!(this.mFaceFocus == null || continueFD || isRecordingState)) {
                this.mFaceFocus.cancelAutoFocus();
                this.mFaceFocus.stopFaceDetection();
            }
            if (!(this.mMoveFocus == null || isRecordingState)) {
                if (unregisterMoveCallback) {
                    this.mMoveFocus.unregisterCallback();
                }
                this.mMoveFocus.hideFocus();
            }
            if (this.mTouchFocusInterface != null) {
                CamLog.m3d(CameraConstants.TAG, "cancelAutoFocus");
                this.mTouchFocusInterface.cancelAutoFocus();
                this.mTouchFocusInterface.unregisterCallback();
                this.mTouchFocusInterface.hideFocus();
                this.mTouchFocusInterface.releaseFocusHandler();
            }
            setAEAFLock(false);
            hideAEAFText();
            hideTouchMove();
            if (getFocusState() != 14) {
                setFocusState(0);
                return;
            }
            return;
        }
        hideAutoFocus();
    }

    public void hideAutoFocus() {
        if (this.mMoveFocus != null) {
            this.mMoveFocus.unregisterCallback();
            this.mMoveFocus.hideFocus();
        }
        if (this.mFaceFocus != null) {
            this.mFaceFocus.stopFaceDetection();
        }
        if (this.mTouchFocusInterface != null) {
            this.mTouchFocusInterface.unregisterCallback();
            this.mTouchFocusInterface.hideFocus();
        }
        hideAEAFText();
        hideTouchMove();
        setFocusState(0);
    }

    public void hideTouchMove() {
        View view = this.mGet.getActivity().findViewById(C0088R.id.focus_touch_move);
        if (view != null) {
            view.setVisibility(8);
        }
    }

    public void setFocusInVisible(boolean isInvisible) {
        if (this.mMoveFocus != null) {
            this.mMoveFocus.setFocusInVisible(isInvisible);
        }
    }

    public void setFocusAreaWindow(int width, int height, int startMargin, int topMargin) {
        if (this.mFaceFocus != null) {
            this.mFaceFocus.setFocusAreaWindow(width, height, startMargin, topMargin);
            this.mFaceFocus.setStartMargin(startMargin);
        }
        if (this.mMoveFocus != null) {
            this.mMoveFocus.setFocusAreaWindow(width, height, startMargin, topMargin);
        }
        if (this.mTouchFocusInterface != null) {
            this.mTouchFocusInterface.setFocusAreaWindow(width, height, startMargin, topMargin);
        }
        if (this.mStartMargin < 0) {
            this.mStartMargin = 0;
        }
        if (this.mTopMargin < 0) {
            this.mTopMargin = 0;
        }
        this.mStartMargin = startMargin;
        this.mTopMargin = topMargin;
        this.mPreviewWidthOnScreen = width;
        this.mPreviewHeightOnScreen = height;
        setTouchAreaWindow(width, height);
    }

    public void setTouchAreaWindow(int width, int height) {
        this.mTouchAreaWidth = width;
        this.mTouchAreaHeight = height;
    }

    public void setFocusInterface(FocusManagerInterface module) {
        this.mFocusManagerInterface = module;
    }

    public void releaseHandlerBeforeTakePicture() {
        if (!(this.mMoveFocus == null || isAFPointVisible())) {
            this.mMoveFocus.releaseFocusHandler();
        }
        if (this.mTouchFocusInterface != null) {
            this.mTouchFocusInterface.releaseFocusHandler();
        }
        if (this.mHandler != null) {
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(10);
        }
    }

    public void releaseTouchFocus() {
        if (this.mTouchFocusInterface != null) {
            this.mTouchFocusInterface.releaseTouchFocus();
        }
    }

    public void releaseFocusHandler() {
        if (this.mTouchFocusInterface != null) {
            this.mTouchFocusInterface.releaseFocusHandler();
        }
    }

    public void setAFPointVisible(boolean isAFPointVisible) {
        CamLog.m3d(CameraConstants.TAG, "setAFPointVisible - isAFPointVisible : " + isAFPointVisible);
        setState(4, false);
        if (!this.mGet.isMWAFSupported() || this.mMoveFocus == null || getFocusState() == 14) {
            this.mIsAFPointVisible = false;
            this.mGet.releaseAeControlBar();
            setEVShutterButtonVisibility(false);
            return;
        }
        this.mIsAFPointVisible = isAFPointVisible;
        if (!this.mIsAFPointVisible || this.mCameraManualMultiFocusView == null) {
            this.mMoveFocus.setCameraMultiFocusView(this.mCameraManualMultiFocusView, false);
            this.mCameraManualMultiFocusView.setLevelControlFocusMode(false);
        } else {
            this.mMoveFocus.setCameraMultiFocusView(this.mCameraManualMultiFocusView, true);
            this.mCameraManualMultiFocusView.setLevelControlFocusMode(true);
        }
        if (!this.mGet.isSettingMenuVisible() && this.mIsAFPointVisible && (CameraConstants.FILM_NONE.equals(this.mGet.getSettingValue(Setting.KEY_FILM_EMULATOR)) || this.mGet.getFilmState() == 4)) {
            registerCallback();
            showFocusMove();
        } else if (this.mGet.isSettingMenuVisible() && this.mIsAFPointVisible) {
            showFocusMove();
        }
    }

    public void setAEAFLock(boolean lock) {
        setFocusLock(lock);
        setAELock(lock);
    }

    public void setFocusLock(boolean lock) {
        if (this.mGet.isAFSupported() && this.mGet.getCameraId() == 0) {
            setState(4, lock);
        } else {
            setState(4, false);
        }
        CamLog.m3d(CameraConstants.TAG, "setFocusLock = " + getState(4));
    }

    public boolean isFocusLock() {
        return getState(4);
    }

    public void setAELock(boolean lock) {
        if (this.mGet.getCameraId() != 0) {
            setState(8, lock);
        } else {
            setState(8, false);
        }
        CamLog.m3d(CameraConstants.TAG, "setAELock = " + getState(8));
    }

    public boolean isAELock() {
        return getState(8);
    }

    public boolean isAEAFJustLocked() {
        return getState(16);
    }

    public boolean isAFPointVisible() {
        return this.mIsAFPointVisible;
    }

    public boolean checkIsInPreview(int x, int y) {
        CamLog.m3d(CameraConstants.TAG, "checkIsInPreview x = " + x + " y = " + y + ", mStartMargin = " + this.mStartMargin);
        int leftMargin = (Utils.getLCDsize(getAppContext(), true)[1] - this.mTouchAreaWidth) / 2;
        int topMargin = this.mStartMargin;
        if (x < leftMargin || x > this.mTouchAreaWidth + leftMargin || y < topMargin || y > this.mTouchAreaHeight + topMargin) {
            return false;
        }
        return true;
    }

    protected boolean isTouchInUIArea(int x, int y) {
        int quickButtonArea;
        int shutterArea = getShutterAreaStart();
        if ("on".equals(this.mGet.getSettingValue(Setting.KEY_TILE_PREVIEW))) {
            quickButtonArea = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.11112f) + RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext());
        } else {
            quickButtonArea = RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext());
        }
        if (y < quickButtonArea || y > shutterArea) {
            return true;
        }
        return false;
    }

    protected boolean isInFocusArea(int x, int y) {
        if (this.mMoveFocus == null) {
            return false;
        }
        x -= this.mTopMargin;
        y -= this.mStartMargin;
        if (Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
            return this.mMoveFocus.mFocusRect.contains(x, y);
        }
        return this.mMoveFocus.mFocusRect.contains(y, x);
    }

    protected int getShutterAreaWidth() {
        View view = this.mGet.findViewById(C0088R.id.shutter_bottom_comp);
        if (view == null) {
            return 0;
        }
        int viewWidth = view.getWidth();
        if (viewWidth == 0) {
            viewWidth = this.mGet.getAppContext().getDrawable(C0088R.drawable.shutter_stroke_normal).getIntrinsicHeight();
        }
        return viewWidth + RatioCalcUtil.getCommandBottomMargin(getAppContext());
    }

    protected int getShutterAreaStart() {
        return Utils.getLCDsize(this.mGet.getAppContext(), true)[0] - getShutterAreaWidth();
    }

    public void hideAEAFText() {
    }

    public void setRotateDegree(int degree, boolean animation) {
        super.setRotateDegree(degree, animation);
        if (this.mEVShutterButton != null) {
            this.mEVShutterButton.setRotated(degree);
        }
    }

    public void setEVshutterButtonEnable(boolean enable) {
        CamLog.m3d(CameraConstants.TAG, "setEVshutterButtonEnable, enable = " + enable);
        if (this.mEVShutterButton != null) {
            this.mEVShutterButton.setEnabled(enable);
        }
    }

    public void setEVShutterButtonVisibility(boolean show) {
        if (this.mEVShutterButton != null) {
            CamLog.m3d(CameraConstants.TAG, "setEVShutterButtonVisibility, show = " + show);
            this.mEVShutterButton.setVisibility(4);
        }
    }

    public void setManualFocusButtonLocation(int screenX, int screenY) {
        if (this.mManualFocusButton != null) {
            this.mManualFocusButton.setVisibility(4);
            this.mManualFocusButton.setImageResource(this.mGet.isManualFocusModeEx() ? C0088R.drawable.btn_camera_mf_pressed : C0088R.drawable.btn_camera_mf_normal);
        }
    }

    public void setManualFocusButtonEnable(boolean enable) {
        if (this.mManualFocusButton != null) {
            this.mManualFocusButton.setEnabled(enable);
            this.mManualFocusButton.setColorFilter(enable ? ColorUtil.getNormalColorByAlpha() : ColorUtil.getDimColorByAlpha());
        }
    }

    private void initManualFocusButtonView() {
        if (this.mGet == null) {
            CamLog.m3d(CameraConstants.TAG, "- manual focus - mGet is null.");
            return;
        }
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
        this.mManualFocusButtonView = this.mGet.inflateView(C0088R.layout.manual_focus_button_layout);
        if (this.mManualFocusButtonView != null && vg != null) {
            vg.addView(this.mManualFocusButtonView);
            this.mManualFocusButton = (RotateImageButton) this.mManualFocusButtonView.findViewById(C0088R.id.manual_focus_button);
            this.mManualFocusButton.setContentDescription(getAppContext().getString(C0088R.string.manual_focus));
            this.mManualFocusButton.setOnClickListener(new C07994());
        }
    }

    public void updateManualFocusLayout() {
        if (this.mManualFocusButton != null) {
            LayoutParams manualFocusButtonParam = (LayoutParams) this.mManualFocusButton.getLayoutParams();
            manualFocusButtonParam.topMargin = ((Utils.getLCDsize(getAppContext(), true)[0] - getAEControlLayoutButtomMargin()) - getAEControlLayoutWidth()) + ((getAEControlLayoutWidth() - manualFocusButtonParam.width) / 2);
            this.mManualFocusButton.setLayoutParams(manualFocusButtonParam);
            this.mManualFocusButton.setDegree(this.mGet.getOrientationDegree(), false);
        }
    }

    public int getAEControlLayoutButtomMargin() {
        return 0;
    }

    public int getAEControlLayoutWidth() {
        return 0;
    }

    public void setDegree(int degree, boolean animation) {
        if (this.mManualFocusButton != null) {
            this.mManualFocusButton.setDegree(degree, animation);
        }
        super.setDegree(degree, animation);
    }

    public void setManualFocusButtonVisibility(boolean show) {
        if (this.mManualFocusButton != null) {
            if (!show || !this.mGet.isRecordingState()) {
                this.mManualFocusButton.setVisibility(show ? 0 : 4);
            }
        }
    }

    protected void setRelativeLayoutPositionOnPreview(View view, int screenX, int screenY, int direction) {
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        if (params != null) {
            int halfWidth = this.mRectWidth / 2;
            int halfHeight = this.mRectHeight / 2;
            int buttonWidth = params.width;
            int buttonHeight = buttonWidth;
            Rect focusRect = adjustFocusRect(new Rect(screenX - halfWidth, screenY - halfHeight, screenX + halfWidth, screenY + halfHeight));
            Rect checkRect = new Rect(focusRect);
            int diffX = RatioCalcUtil.getPreviewTextureRect().left;
            int diffY = RatioCalcUtil.getPreviewTextureRect().top;
            params.leftMargin = focusRect.left - ((buttonWidth - this.mRectWidth) / 2);
            checkRect.bottom = (focusRect.bottom + buttonHeight) - diffX;
            if ((checkingArea(checkRect) & 8) != 0) {
                params.topMargin = ((focusRect.bottom - buttonHeight) - this.mRectHeight) - diffY;
            } else {
                params.topMargin = focusRect.bottom - diffY;
            }
            view.setLayoutParams(params);
        }
    }

    protected int checkingArea(Rect focusRect) {
        return 0;
    }

    protected Rect adjustFocusRect(Rect focusRect) {
        return new Rect();
    }

    public void registerEVCallback(boolean isSet, boolean postResetRunnable) {
    }
}
