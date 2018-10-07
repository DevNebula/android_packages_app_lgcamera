package com.lge.camera.dialog;

import android.os.Build.VERSION;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import com.lge.camera.util.ViewUtil;

public class RotateDialog implements OnRemoveHandler {
    protected int mDegree = -1;
    protected boolean mDoNotShowAgain = false;
    protected CamDialogInterface mGet = null;
    private OnTouchListener mOnBackCoverTouchListener = new C07294();
    protected View mView = null;

    /* renamed from: com.lge.camera.dialog.RotateDialog$2 */
    class C07272 implements OnCheckedChangeListener {
        C07272() {
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            RotateDialog.this.mDoNotShowAgain = isChecked;
            if (VERSION.SDK_INT <= 21) {
                buttonView.playSoundEffect(0);
            }
        }
    }

    /* renamed from: com.lge.camera.dialog.RotateDialog$3 */
    class C07283 implements Runnable {
        C07283() {
        }

        public void run() {
            RotateDialog.this.mGet.removePostRunnable(this);
            if (RotateDialog.this.mView != null) {
                ViewGroup dialog = (ViewGroup) RotateDialog.this.mView.findViewById(C0088R.id.rotate_dialog_layout);
                ScrollView scrView = (ScrollView) RotateDialog.this.mView.findViewById(C0088R.id.message_scroll);
                if (!(scrView == null || scrView.canScrollVertically(1))) {
                    scrView.setFocusable(false);
                }
                if (ViewUtil.isAccessibilityServiceEnabled(RotateDialog.this.mGet.getAppContext())) {
                    dialog.setFocusableInTouchMode(true);
                    if (ViewUtil.isAccessibilityServiceEnabled(RotateDialog.this.mGet.getAppContext())) {
                        ViewUtil.setContentDescriptionForAccessibility(RotateDialog.this.mGet.getAppContext(), dialog);
                        dialog.sendAccessibilityEvent(32768);
                    }
                    dialog.requestFocus();
                }
                RotateDialog.this.notifyFocusingFinished(dialog);
            }
        }
    }

    /* renamed from: com.lge.camera.dialog.RotateDialog$4 */
    class C07294 implements OnTouchListener {
        C07294() {
        }

        public boolean onTouch(View view, MotionEvent event) {
            if (RotateDialog.this.mView != null && event.getAction() == 0) {
                float x = event.getX();
                float y = event.getY();
                View rdl = RotateDialog.this.mView.findViewById(C0088R.id.rotate_dialog_layout);
                if (rdl != null && (x < ((float) rdl.getLeft()) || x > ((float) rdl.getRight()) || y < ((float) rdl.getTop()) || y > ((float) rdl.getBottom()))) {
                    RotateDialog.this.doBackCoverTouch();
                }
            }
            return false;
        }
    }

    protected RotateDialog(CamDialogInterface function) {
        this.mGet = function;
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }

    protected void setView(View dialogView, boolean useCheckBox, boolean isHelpDialog) {
        int checkBoxRes = this.mGet.getAppContext().getResources().getIdentifier("dialog_init_guide", "layout", "com.lge");
        if (useCheckBox || isHelpDialog) {
            CheckBox checkboxText;
            if (checkBoxRes <= 0) {
                checkboxText = new CheckBox(this.mGet.getAppContext());
                checkboxText.setId(16908289);
            } else {
                int c_frame_res = this.mGet.getAppContext().getResources().getIdentifier("dialog_init_guide", "layout", "com.lge");
                LayoutInflater inflater = this.mGet.getActivity().getLayoutInflater();
                checkboxText = (CheckBox) inflater.inflate(checkBoxRes, (LinearLayout) inflater.inflate(c_frame_res, null), true).findViewById(16908289);
            }
            ((ViewGroup) checkboxText.getParent()).removeView(checkboxText);
            checkboxText.setVisibility(8);
            checkboxText.setTextAppearance(this.mGet.getActivity(), C0088R.style.type_d03_sp);
            LinearLayout checkboxParent = (LinearLayout) dialogView.findViewById(C0088R.id.check_box_layout);
            if (checkboxParent != null) {
                checkboxParent.addView(checkboxText);
                checkboxParent.setVisibility(0);
                checkboxParent.setLayoutDirection(3);
            }
        }
        if (!isHelpDialog) {
            ScrollView scrView = (ScrollView) dialogView.findViewById(C0088R.id.message_scroll);
            if (scrView != null) {
                TextView messageText;
                int textViewRes = this.mGet.getAppContext().getResources().getIdentifier("dialog_c_1", "layout", "com.lge");
                if (textViewRes <= 0) {
                    messageText = new TextView(this.mGet.getAppContext());
                    messageText.setId(16908308);
                } else {
                    messageText = (TextView) this.mGet.inflateView(textViewRes);
                }
                messageText.setTextDirection(5);
                messageText.setTextAppearance(this.mGet.getActivity(), C0088R.style.type_d03_dp);
                scrView.addView(messageText);
            }
        }
    }

    protected void create(View v) {
        create(v, true, true);
    }

    protected void create(View v, boolean isCloseByBackCoverTouch, boolean isWarning) {
        create(v, isCloseByBackCoverTouch, isWarning, true);
    }

    protected void create(View v, boolean isCloseByBackCoverTouch, boolean isWarning, boolean showAnim) {
        if (this.mGet != null && v != null) {
            this.mView = v;
            if (isWarning) {
                ImageView icon = (ImageView) v.findViewById(C0088R.id.title_icon);
                if (icon != null) {
                    icon.setVisibility(0);
                }
            }
            ViewGroup vg = (ViewGroup) this.mGet.getActivity().findViewById(C0088R.id.camera_base);
            if (vg == null) {
                CamLog.m3d(CameraConstants.TAG, "Exit because camera_base is null");
                return;
            }
            vg.addView(v);
            LayoutParams params = (LayoutParams) v.getLayoutParams();
            params.addRule(13, 1);
            v.setLayoutParams(params);
            onPrepare();
            if (this.mView != null) {
                this.mView.setVisibility(0);
            }
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (RotateDialog.this.mView != null) {
                        RotateDialog.this.onPrepare();
                        RotateDialog.this.mView.setVisibility(0);
                        RotateDialog.this.requestFocus();
                    }
                }
            }, 0);
            if (showAnim) {
                showBackcoverAnimation();
            }
            if (isCloseByBackCoverTouch) {
                this.mView.findViewById(C0088R.id.backcover).setOnTouchListener(this.mOnBackCoverTouchListener);
            }
            this.mGet.onDialogShowing();
        }
    }

    protected void setCheckBox(View dialogView, boolean isCheckBoxNeeded) {
        if (dialogView != null) {
            CheckBox userCheck = (CheckBox) dialogView.findViewById(16908289);
            if (userCheck == null) {
                return;
            }
            if (isCheckBoxNeeded) {
                userCheck.setVisibility(0);
                userCheck.setText(C0088R.string.sp_eula_popup_do_not_show_this_again_NORMAL);
                userCheck.setChecked(this.mDoNotShowAgain);
                userCheck.setOnCheckedChangeListener(new C07272());
                return;
            }
            userCheck.setVisibility(8);
        }
    }

    protected void setFocus(View dialogView, View nextFocus, boolean useCheckBox) {
        if (dialogView != null && ModelProperties.isKeyPadSupported(this.mGet.getAppContext())) {
            View firstFocus = dialogView;
            if (useCheckBox) {
                firstFocus = (CheckBox) dialogView.findViewById(16908289);
            }
            if (!ModelProperties.isKeyPadSupported(this.mGet.getAppContext()) || firstFocus == null) {
                dialogView.requestFocus();
                return;
            }
            firstFocus.setFocusableInTouchMode(true);
            firstFocus.setFocusable(true);
            firstFocus.requestFocus();
            firstFocus.setNextFocusDownId(nextFocus.getId());
            firstFocus.setNextFocusUpId(nextFocus.getId());
            firstFocus.setNextFocusLeftId(nextFocus.getId());
            firstFocus.setNextFocusRightId(nextFocus.getId());
            nextFocus.setFocusable(true);
            nextFocus.setNextFocusDownId(firstFocus.getId());
            nextFocus.setNextFocusUpId(firstFocus.getId());
            nextFocus.setNextFocusLeftId(firstFocus.getId());
            nextFocus.setNextFocusRightId(firstFocus.getId());
        }
    }

    public void requestFocus() {
        if (ViewUtil.isAccessibilityServiceEnabled(this.mGet.getAppContext()) || ModelProperties.isKeyPadSupported(this.mGet.getAppContext())) {
            this.mGet.getHandler().postDelayed(new Thread(new C07283()), (long) (ViewUtil.isAccessibilityServiceEnabled(this.mGet.getAppContext()) ? 500 : 0));
        }
    }

    protected void alignButtonLine() {
        if (this.mView != null) {
            Button btnOk = (Button) this.mView.findViewById(C0088R.id.ok_button);
            Button btnCancel = (Button) this.mView.findViewById(C0088R.id.cancel_button);
            if (btnOk != null && btnCancel != null) {
                int lineCountBtnOk = btnOk.getLineCount();
                int lineCountBtnCancel = btnCancel.getLineCount();
                if (lineCountBtnOk > lineCountBtnCancel) {
                    btnCancel.setLines(lineCountBtnOk);
                }
                if (lineCountBtnCancel > lineCountBtnOk) {
                    btnOk.setLines(lineCountBtnCancel);
                }
            }
        }
    }

    public void onPrepare() {
        alignButtonLine();
    }

    public boolean onDismiss() {
        if (this.mGet == null || this.mView == null) {
            return false;
        }
        ViewGroup vg = (ViewGroup) this.mGet.getActivity().findViewById(C0088R.id.camera_base);
        if (vg == null) {
            return false;
        }
        vg.removeView(this.mView);
        this.mGet.onDialogDismiss();
        this.mDegree = -1;
        this.mView = null;
        return true;
    }

    public boolean onDismiss(boolean immediately) {
        return false;
    }

    protected void doBackCoverTouch() {
        if (this.mDegree != -1) {
            onDismiss();
        }
    }

    public void setOneButtonLayout(View v, boolean removeCancelBtn) {
        int remainBtnId = C0088R.id.ok_button;
        int removeBtnId = C0088R.id.cancel_button;
        if (!removeCancelBtn) {
            remainBtnId = C0088R.id.cancel_button;
            removeBtnId = C0088R.id.ok_button;
        }
        Button btnOk = (Button) v.findViewById(remainBtnId);
        ((LinearLayout) v.findViewById(C0088R.id.button_inner_layout)).setLayoutDirection(3);
        LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) btnOk.getLayoutParams();
        param.setMarginStart(Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.rotate_dialog_button_inner_layout_new.paddingStart));
        param.setMarginEnd(Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.rotate_dialog_button_inner_layout_new.paddingEnd));
        param.gravity = 1;
        btnOk.setLayoutParams(param);
        ((Button) v.findViewById(removeBtnId)).setVisibility(8);
    }

    public void setDegree(int degree, boolean showAnim) {
        if (this.mGet == null) {
            return;
        }
        if ((this.mGet.isPostviewShowing() || this.mDegree != degree) && this.mView != null) {
            RelativeLayout marginLayout = (RelativeLayout) this.mView.findViewById(C0088R.id.rotate_dialog_margin_layout);
            RotateLayout rotateLayout = (RotateLayout) this.mView.findViewById(C0088R.id.rotate_dialog_layout);
            if (marginLayout != null && rotateLayout != null) {
                this.mDegree = degree;
                if (showAnim) {
                    hideRotateDialogAnimation();
                }
                LayoutParams mlp = (LayoutParams) marginLayout.getLayoutParams();
                LayoutParams rlp = (LayoutParams) rotateLayout.getLayoutParams();
                if (mlp != null && rlp != null) {
                    boolean isLand;
                    if (Utils.isEqualDegree(this.mGet.getActivity().getResources(), degree, 0) || Utils.isEqualDegree(this.mGet.getActivity().getResources(), degree, 180)) {
                        isLand = true;
                    } else {
                        isLand = false;
                    }
                    if (isLand) {
                        mlp.width = -1;
                        mlp.height = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.65f);
                        rlp.width = -2;
                        rlp.height = -1;
                    } else {
                        mlp.width = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.95f);
                        mlp.height = -1;
                        rlp.width = -1;
                        rlp.height = -2;
                    }
                    rotateLayout.setLayoutParams(rlp);
                    marginLayout.setLayoutParams(mlp);
                    rotateLayout.rotateLayout(degree);
                    if (showAnim) {
                        showRotateDialogAnimation();
                    }
                    CamLog.m3d(CameraConstants.TAG, "RotatableDialog startRotataion(degree) end = " + degree);
                    setNextFocusID(degree);
                }
            }
        }
    }

    protected void showBackcoverAnimation() {
        if (this.mView != null) {
            AnimationUtil.startAlphaAnimation((RelativeLayout) this.mView.findViewById(C0088R.id.backcover), 0.0f, 1.0f, 600, null);
        }
    }

    protected void showRotateDialogAnimation() {
        CamLog.m3d(CameraConstants.TAG, "showRotateDialogAnimation()");
        if (this.mView != null) {
            AnimationUtil.startAlphaAnimation(this.mView.findViewById(C0088R.id.rotate_dialog_layout), 0.0f, 1.0f, 300, null);
        }
    }

    protected void hideRotateDialogAnimation() {
        CamLog.m3d(CameraConstants.TAG, "hideRotateDialogAnimation()");
        if (this.mView != null) {
            AnimationUtil.startAlphaAnimation(this.mView.findViewById(C0088R.id.rotate_dialog_layout), 1.0f, 0.0f, 300, null);
        }
    }

    protected void notifyFocusingFinished(View view) {
    }

    protected void setNextFocusID(int degree) {
    }
}
