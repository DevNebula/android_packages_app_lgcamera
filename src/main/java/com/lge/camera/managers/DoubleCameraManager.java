package com.lge.camera.managers;

import android.content.res.Configuration;
import android.graphics.ColorFilter;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;

public class DoubleCameraManager extends ManagerInterfaceImpl {
    protected static final int ALL_BUTTONS_ENABLED = 3;
    protected static final float DOUBLE_BTN_BETWEEN = 0.0167f;
    protected static final int NORMAL_BUTTON_ENABLED = 1;
    protected static final int WIDE_BUTTON_ENABLED = 2;
    private RelativeLayout mButtonLayout = null;
    private RelativeLayout mButtonParent = null;
    private View mButtonViewGroup = null;
    protected int mEnableButtons = 3;
    private RotateImageButton mNormalAngleButton = null;
    private CameraSwitchingInterface mSwitchingInterface = null;
    private RotateImageButton mWideAngleAnimation = null;
    private RotateImageButton mWideAngleButton = null;
    private int mWideCameraId = 2;

    public interface CameraSwitchingInterface {
        void onNormalAngleButtonClicked();

        boolean onNormalAngleButtonTouched();

        void onWideAngleButtonClicked();

        boolean onWideAngleButtonTouched();
    }

    /* renamed from: com.lge.camera.managers.DoubleCameraManager$1 */
    class C08831 implements OnClickListener {
        C08831() {
        }

        public void onClick(View arg0) {
            DoubleCameraManager.this.onNormalAngleButtonClicked();
        }
    }

    /* renamed from: com.lge.camera.managers.DoubleCameraManager$2 */
    class C08842 implements OnClickListener {
        C08842() {
        }

        public void onClick(View arg0) {
            DoubleCameraManager.this.onWideAngleButtonClicked();
        }
    }

    /* renamed from: com.lge.camera.managers.DoubleCameraManager$3 */
    class C08853 implements OnTouchListener {
        C08853() {
        }

        public boolean onTouch(View arg0, MotionEvent arg1) {
            DoubleCameraManager.this.onNormalAngleButtonTouched();
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.DoubleCameraManager$4 */
    class C08864 implements OnTouchListener {
        C08864() {
        }

        public boolean onTouch(View arg0, MotionEvent arg1) {
            DoubleCameraManager.this.onWideAngleButtonTouched();
            return false;
        }
    }

    public DoubleCameraManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setCameraSwitchingInterface(CameraSwitchingInterface interfaces) {
        this.mSwitchingInterface = interfaces;
    }

    public void init() {
        super.init();
        setWideCameraId();
        initLayout();
        this.mEnableButtons = 3;
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        init();
        clearEventListener();
        setButtonListener();
        setTouchListener();
    }

    private void setWideCameraId() {
        if (FunctionProperties.getCameraTypeFront() == 1) {
            this.mWideCameraId = 1;
        } else if (FunctionProperties.getCameraTypeRear() == 1) {
            this.mWideCameraId = 2;
        }
    }

    private void initLayout() {
        if ((FunctionProperties.getCameraTypeFront() == 1 || FunctionProperties.getCameraTypeFront() == 2 || FunctionProperties.getCameraTypeRear() == 1) && !CameraConstants.MODE_MULTIVIEW.equals(this.mGet.getShotMode()) && !CameraConstants.MODE_SQUARE_SPLICE.equals(this.mGet.getShotMode())) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            this.mButtonViewGroup = this.mGet.inflateView(C0088R.layout.dualview_button_layout);
            if (!(vg == null || this.mButtonViewGroup == null)) {
                vg.addView(this.mButtonViewGroup, 2);
            }
            if (this.mButtonViewGroup != null) {
                setButtons();
                setButtonsSelected();
            }
        }
    }

    public void setListenerAfterOneShotCallback() {
        if (this.mNormalAngleButton != null && this.mWideAngleButton != null) {
            setButtonListener();
            setTouchListener();
        }
    }

    public void setDisabledButton(int cameraId) {
        int button = 2;
        if (cameraId == 0 || cameraId == 1) {
            button = 1;
        } else if (cameraId == this.mWideCameraId) {
            button = 2;
        }
        this.mEnableButtons = (button ^ -1) & 3;
        setButtonsEnabledSeparately();
    }

    public void setEnabledButton(int cameraId) {
        int button = 2;
        if (cameraId == 0 || cameraId == 1) {
            button = 1;
        } else if (cameraId == this.mWideCameraId) {
            button = 2;
        }
        this.mEnableButtons |= button;
        setButtonsEnabledSeparately();
    }

    public void setButtonsEnabledSeparately() {
        boolean enabled;
        ColorFilter cf = ColorUtil.getNormalColorByAlpha();
        if (this.mNormalAngleButton != null) {
            enabled = (this.mEnableButtons & 1) == 1;
            cf = enabled ? ColorUtil.getNormalColorByAlpha() : ColorUtil.getDimColorByAlpha();
            this.mNormalAngleButton.setEnabled(enabled);
            this.mNormalAngleButton.setColorFilter(cf);
            this.mNormalAngleButton.getBackground().setColorFilter(cf);
        }
        if (this.mWideAngleButton != null) {
            if ((this.mEnableButtons & 2) == 2) {
                enabled = true;
            } else {
                enabled = false;
            }
            cf = enabled ? ColorUtil.getNormalColorByAlpha() : ColorUtil.getDimColorByAlpha();
            this.mWideAngleButton.setEnabled(enabled);
            this.mWideAngleButton.setColorFilter(cf);
            this.mWideAngleButton.getBackground().setColorFilter(cf);
        }
    }

    private void setButtons() {
        this.mButtonLayout = (RelativeLayout) this.mButtonViewGroup.findViewById(C0088R.id.dualview_button_layout);
        this.mButtonParent = (RelativeLayout) this.mButtonViewGroup.findViewById(C0088R.id.dual_button_parent_layout);
        LayoutParams lParam = (LayoutParams) this.mButtonViewGroup.getLayoutParams();
        lParam.addRule(21, 1);
        this.mButtonViewGroup.setLayoutParams(lParam);
        this.mNormalAngleButton = (RotateImageButton) this.mButtonViewGroup.findViewById(C0088R.id.btn_dualview_normal_range);
        this.mWideAngleButton = (RotateImageButton) this.mButtonViewGroup.findViewById(C0088R.id.btn_dualview_wide_range);
        this.mWideAngleAnimation = (RotateImageButton) this.mButtonViewGroup.findViewById(C0088R.id.btn_dualview_wide_range_animation);
        if (FunctionProperties.getCameraTypeFront() == 1 || (FunctionProperties.getCameraTypeFront() == 2 && this.mGet.getCameraId() == 1)) {
            this.mNormalAngleButton.setContentDescription(this.mGet.getAppContext().getText(C0088R.string.selfie_camera));
            this.mWideAngleButton.setContentDescription(this.mGet.getAppContext().getText(C0088R.string.groupfie_camera));
        } else if (FunctionProperties.getCameraTypeRear() == 1) {
            this.mNormalAngleButton.setContentDescription(this.mGet.getAppContext().getText(C0088R.string.normal_angle_lens));
            this.mWideAngleButton.setContentDescription(this.mGet.getAppContext().getText(C0088R.string.wide_angle_lens));
        }
        LayoutParams rlp = (LayoutParams) this.mWideAngleButton.getLayoutParams();
        rlp.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.0167f);
        this.mWideAngleButton.setLayoutParams(rlp);
        this.mWideAngleAnimation.setLayoutParams(rlp);
        setDegree(this.mGet.getOrientationDegree(), false);
    }

    public void setButtonParentLayout(int width, int height, int startMargin, int topMargin) {
        if (this.mButtonParent != null) {
            LayoutParams rlp = (LayoutParams) this.mButtonParent.getLayoutParams();
            if (rlp != null) {
                this.mButtonParent.setGravity(48);
                if (this.mGet.isUspZoneSupportedMode(this.mGet.getShotMode())) {
                    int uspBottomMargin = this.mGet.getUspBottomMargin();
                    if (uspBottomMargin != -1) {
                        int lcdHeight = Utils.getLCDsize(this.mGet.getAppContext(), true)[0];
                        if (startMargin + height > lcdHeight - uspBottomMargin) {
                            height = lcdHeight - uspBottomMargin;
                        }
                    }
                }
                int marginTop = startMargin + (height / 2);
                if (this.mNormalAngleButton != null) {
                    marginTop = (marginTop - this.mNormalAngleButton.getDrawable().getIntrinsicHeight()) - (RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.0167f) / 2);
                }
                rlp.setMarginsRelative(rlp.getMarginStart(), marginTop, rlp.getMarginEnd(), rlp.bottomMargin);
                this.mButtonParent.setLayoutParams(rlp);
            }
        }
    }

    private void setButtonListener() {
        this.mNormalAngleButton.setOnClickListener(new C08831());
        this.mWideAngleButton.setOnClickListener(new C08842());
    }

    private void setTouchListener() {
        this.mNormalAngleButton.setOnTouchListener(new C08853());
        this.mWideAngleButton.setOnTouchListener(new C08864());
    }

    private void clearEventListener() {
        if (this.mNormalAngleButton != null) {
            this.mNormalAngleButton.setOnClickListener(null);
            this.mNormalAngleButton.setOnTouchListener(null);
        }
        if (this.mWideAngleButton != null) {
            this.mWideAngleButton.setOnClickListener(null);
            this.mWideAngleButton.setOnTouchListener(null);
        }
    }

    public void setDegree(int degree, boolean animation) {
        int convertDegree = degree % 360;
        if (Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
            convertDegree = (degree + 180) % 360;
        }
        if (this.mNormalAngleButton != null) {
            this.mNormalAngleButton.setDegree(convertDegree, animation);
        }
        if (this.mWideAngleButton != null) {
            this.mWideAngleButton.setDegree(convertDegree, animation);
        }
    }

    public void onPauseBefore() {
        isStartWideAngleAnimation(false);
        clearEventListener();
    }

    public void onDestroy() {
        super.onDestroy();
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (!(vg == null || this.mButtonViewGroup == null)) {
            vg.removeView(this.mButtonViewGroup);
        }
        this.mNormalAngleButton = null;
        this.mWideAngleButton = null;
        this.mButtonLayout = null;
        this.mButtonParent = null;
        this.mButtonViewGroup = null;
    }

    public void showDualViewControl(boolean show) {
        boolean isRearCamera = this.mGet.isRearCamera();
        if (FunctionProperties.getCameraTypeRear() != 0 || !isRearCamera) {
            if ((FunctionProperties.getCameraTypeFront() != 0 || isRearCamera) && this.mButtonLayout != null) {
                this.mButtonLayout.setVisibility(show ? 0 : 8);
            }
        }
    }

    public void onCameraSwitchingStart() {
        setDualViewControlEnabled(false);
        super.onCameraSwitchingStart();
    }

    public void onCameraSwitchingEnd() {
        setDualViewControlEnabled(true);
        super.onCameraSwitchingEnd();
    }

    public void setDualViewControlEnabled(boolean enable) {
        if (this.mWideAngleButton != null && this.mNormalAngleButton != null) {
            ColorFilter cf;
            if (enable) {
                cf = ColorUtil.getNormalColorByAlpha();
                if ((this.mEnableButtons & 1) == 1) {
                    this.mNormalAngleButton.setEnabled(enable);
                    this.mNormalAngleButton.setColorFilter(cf);
                    this.mNormalAngleButton.getBackground().setColorFilter(cf);
                }
                if ((this.mEnableButtons & 2) == 2) {
                    this.mWideAngleButton.setEnabled(enable);
                    this.mWideAngleButton.setColorFilter(cf);
                    this.mWideAngleButton.getBackground().setColorFilter(cf);
                    return;
                }
                return;
            }
            cf = ColorUtil.getDimColorByAlpha();
            this.mNormalAngleButton.setEnabled(enable);
            this.mNormalAngleButton.setColorFilter(cf);
            this.mNormalAngleButton.getBackground().setColorFilter(cf);
            this.mWideAngleButton.setEnabled(enable);
            this.mWideAngleButton.setColorFilter(cf);
            this.mWideAngleButton.getBackground().setColorFilter(cf);
        }
    }

    private void onNormalAngleButtonClicked() {
        if (this.mSwitchingInterface != null) {
            this.mSwitchingInterface.onNormalAngleButtonClicked();
        }
    }

    private void onWideAngleButtonClicked() {
        if (this.mSwitchingInterface != null) {
            this.mSwitchingInterface.onWideAngleButtonClicked();
        }
    }

    private boolean onNormalAngleButtonTouched() {
        boolean result = false;
        if (this.mSwitchingInterface != null) {
            result = this.mSwitchingInterface.onNormalAngleButtonTouched();
        }
        setButtonsSelected();
        return !result;
    }

    private boolean onWideAngleButtonTouched() {
        boolean result = false;
        if (this.mSwitchingInterface != null) {
            result = this.mSwitchingInterface.onWideAngleButtonTouched();
        }
        setButtonsSelected();
        isStartWideAngleAnimation(false);
        if (result) {
            return false;
        }
        return true;
    }

    public void setButtonsSelected() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (DoubleCameraManager.this.mNormalAngleButton != null && DoubleCameraManager.this.mWideAngleButton != null) {
                    if (!DoubleCameraManager.this.mGet.checkCameraChanging(4) || !DoubleCameraManager.this.mGet.isOpticZoomSupported(null)) {
                        if (DoubleCameraManager.this.mGet.isRearCamera() || FunctionProperties.getCameraTypeFront() != 2) {
                            DoubleCameraManager.this.mNormalAngleButton.setImageResource(C0088R.drawable.btn_dualview_angle);
                            DoubleCameraManager.this.mWideAngleButton.setImageResource(C0088R.drawable.btn_dualview_wide_angle);
                            if (DoubleCameraManager.this.mGet.getCameraId() == DoubleCameraManager.this.mWideCameraId) {
                                DoubleCameraManager.this.mNormalAngleButton.setSelected(false);
                                DoubleCameraManager.this.mWideAngleButton.setSelected(true);
                                return;
                            }
                            DoubleCameraManager.this.mNormalAngleButton.setSelected(true);
                            DoubleCameraManager.this.mWideAngleButton.setSelected(false);
                            return;
                        }
                        DoubleCameraManager.this.mNormalAngleButton.setImageResource(C0088R.drawable.btn_dualview_angle_front);
                        DoubleCameraManager.this.mWideAngleButton.setImageResource(C0088R.drawable.btn_dualview_wide_angle_front);
                        if (SharedPreferenceUtil.getCropAngleButtonId(DoubleCameraManager.this.getAppContext()) == 1) {
                            DoubleCameraManager.this.mNormalAngleButton.setSelected(false);
                            DoubleCameraManager.this.mWideAngleButton.setSelected(true);
                            return;
                        }
                        DoubleCameraManager.this.mNormalAngleButton.setSelected(true);
                        DoubleCameraManager.this.mWideAngleButton.setSelected(false);
                    }
                }
            }
        });
    }

    public int getSelectedCropAngleButtonId() {
        if (!this.mGet.isRearCamera() && FunctionProperties.getCameraTypeFront() == 2) {
            if (this.mNormalAngleButton != null && this.mNormalAngleButton.isSelected()) {
                return 0;
            }
            if (this.mWideAngleButton != null && this.mWideAngleButton.isSelected()) {
                return 1;
            }
        }
        CamLog.m3d(CameraConstants.TAG, "nothing selected");
        return -1;
    }

    public void setForceButtonsSelected(int cameraId) {
        if (CameraDeviceUtils.isRearCamera(cameraId) && this.mNormalAngleButton != null && this.mWideAngleButton != null) {
            this.mNormalAngleButton.setImageResource(C0088R.drawable.btn_dualview_angle);
            this.mWideAngleButton.setImageResource(C0088R.drawable.btn_dualview_wide_angle);
            if (cameraId == this.mWideCameraId) {
                this.mNormalAngleButton.setSelected(false);
                this.mWideAngleButton.setSelected(true);
                return;
            }
            this.mNormalAngleButton.setSelected(true);
            this.mWideAngleButton.setSelected(false);
        }
    }

    public boolean isAngleButtonPressed() {
        if (this.mNormalAngleButton == null || this.mWideAngleButton == null) {
            return false;
        }
        if (this.mNormalAngleButton.isPressed() || this.mWideAngleButton.isPressed()) {
            return true;
        }
        return false;
    }

    public void isStartWideAngleAnimation(boolean isStartWideAngleAni) {
        if (this.mWideAngleAnimation == null) {
            return;
        }
        if ((!this.mGet.isRearCamera() || this.mGet.getCameraId() == 0) && this.mGet.isRearCamera()) {
            this.mWideAngleAnimation.setVisibility(isStartWideAngleAni ? 0 : 8);
            if (isStartWideAngleAni) {
                AnimationUtil.startWideAngleAnimation(this.mWideAngleAnimation, null);
            } else {
                this.mWideAngleAnimation.clearAnimation();
            }
        }
    }
}
