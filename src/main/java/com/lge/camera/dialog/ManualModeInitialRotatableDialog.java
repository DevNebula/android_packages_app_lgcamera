package com.lge.camera.dialog;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.lge.camera.util.CamLog;
import com.lge.camera.util.DialogCreator;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;

public class ManualModeInitialRotatableDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.ManualModeInitialRotatableDialog$1 */
    class C07501 implements OnClickListener {
        C07501() {
        }

        public void onClick(View v) {
            CheckBox userCheckBox = (CheckBox) ManualModeInitialRotatableDialog.this.mGet.findViewById(16908289);
            if (userCheckBox != null) {
                SharedPreferenceUtil.saveManualModeInitialGuide(ManualModeInitialRotatableDialog.this.mGet.getAppContext(), userCheckBox.isChecked());
            }
            ManualModeInitialRotatableDialog.this.onDismiss();
        }
    }

    public ManualModeInitialRotatableDialog(CamDialogInterface function) {
        super(function);
        this.mDoNotShowAgain = !ModelProperties.isQua();
    }

    public void create(boolean isCheckBoxNeeded, String setting, int dialogId) {
        CamLog.m3d(CameraConstants.TAG, "HelpRotateDialog dialogId:" + dialogId);
        View rotateHelpView = this.mGet.inflateView(C0088R.layout.rotate_help_dialog);
        setView(rotateHelpView, true, true);
        ImageView messageImage = (ImageView) rotateHelpView.findViewById(C0088R.id.message_image);
        Button btnOk = (Button) rotateHelpView.findViewById(C0088R.id.ok_button);
        ((TextView) rotateHelpView.findViewById(C0088R.id.title_text)).setText(C0088R.string.camera_manual_mode_title_text);
        Utils.addTabToNumberedDescription((ViewGroup) rotateHelpView.findViewById(C0088R.id.scroll_view_child_layout), this.mGet.getAppContext().getString(C0088R.string.camera_manual_mode_initial_guide), this.mGet.getActivity(), true, C0088R.style.type_d03_sp);
        messageImage.setImageResource(C0088R.drawable.camera_help_popup_image_manual_dcm);
        btnOk.setText(C0088R.string.sp_ok_NORMAL);
        setCheckBox(rotateHelpView, isCheckBoxNeeded);
        setFocus(rotateHelpView, btnOk, true);
        btnOk.setOnClickListener(new C07501());
        super.create(rotateHelpView);
    }

    public void setDegree(int degree, boolean showAnim) {
        if (this.mGet != null && this.mDegree != degree && this.mView != null) {
            RotateLayout rotateLayout = (RotateLayout) this.mView.findViewById(C0088R.id.rotate_dialog_layout);
            if (rotateLayout != null) {
                this.mDegree = degree;
                if (showAnim) {
                    hideRotateDialogAnimation();
                }
                RelativeLayout marginLayout = (RelativeLayout) this.mView.findViewById(C0088R.id.rotate_dialog_margin_layout);
                marginLayout.setLayoutParams(getSoftKeyNavigationBarModelParams(marginLayout));
                ScrollView messageScroll = (ScrollView) this.mView.findViewById(C0088R.id.message_scroll);
                ImageView messageImage = (ImageView) this.mView.findViewById(C0088R.id.message_image);
                LinearLayout rotateInnerLayout = (LinearLayout) this.mView.findViewById(C0088R.id.rotate_dialog_inner_layout);
                LayoutParams rotateLayoutParams = (LayoutParams) rotateLayout.getLayoutParams();
                LayoutParams marginLayoutParams = (LayoutParams) marginLayout.getLayoutParams();
                LayoutParams messageScrollParams = (LayoutParams) messageScroll.getLayoutParams();
                LayoutParams messageImageParams = (LayoutParams) messageImage.getLayoutParams();
                LayoutParams rotateInnerLayoutParams = (LayoutParams) rotateInnerLayout.getLayoutParams();
                Utils.resetLayoutParameter(marginLayoutParams);
                Utils.resetLayoutParameter(messageScrollParams);
                Utils.resetLayoutParameter(messageImageParams);
                Utils.resetLayoutParameter(rotateInnerLayoutParams);
                boolean isLand = Utils.isEqualDegree(this.mGet.getActivity().getResources(), degree, 0) || Utils.isEqualDegree(this.mGet.getActivity().getResources(), degree, 180);
                if (isLand) {
                    rotateInnerLayoutParams.width = DialogCreator.getDialogWidth(this.mGet.getAppContext(), true);
                    rotateInnerLayoutParams.height = ModelProperties.isPhone(this.mGet.getAppContext()) ? Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.rotate_dialog_horizontal_height) : -2;
                    rotateLayoutParams.height = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.rotate_dialog_horizontal_max_height_tablet);
                    messageImageParams.bottomMargin = 0;
                    messageScrollParams.setMarginStart(Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.rotate_help_dialog_layout_landscape.margin));
                    messageScrollParams.addRule(17, C0088R.id.message_image);
                    messageScrollParams.addRule(6, C0088R.id.message_image);
                } else {
                    rotateInnerLayoutParams.width = DialogCreator.getDialogWidth(this.mGet.getAppContext(), false);
                    rotateInnerLayoutParams.height = -2;
                    marginLayoutParams.addRule(13);
                    rotateLayoutParams.height = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.rotate_dialog_vertical_max_height);
                    messageImageParams.width = -2;
                    messageImageParams.height = -2;
                    messageImageParams.bottomMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.rotate_help_dialog_layout.margin);
                    messageImageParams.addRule(14, -1);
                    messageScrollParams.setMarginStart(0);
                    messageScrollParams.addRule(3, C0088R.id.message_image);
                    messageScrollParams.width = -2;
                }
                marginLayout.setLayoutParams(marginLayoutParams);
                rotateInnerLayout.setLayoutParams(rotateInnerLayoutParams);
                messageScroll.setLayoutParams(messageScrollParams);
                messageImage.setLayoutParams(messageImageParams);
                rotateLayout.setLayoutParams(rotateLayoutParams);
                rotateLayout.rotateLayout(degree);
                if (showAnim) {
                    showRotateDialogAnimation();
                }
                CamLog.m3d(CameraConstants.TAG, "RotatableDialog startRotataion(degree) end = " + degree);
            }
        }
    }

    private LayoutParams getSoftKeyNavigationBarModelParams(RelativeLayout marginLayout) {
        LayoutParams marginParams = (LayoutParams) marginLayout.getLayoutParams();
        if (ModelProperties.isSoftKeyNavigationBarModel(this.mGet.getAppContext())) {
            Utils.resetLayoutParameter(marginParams);
            if (Utils.isConfigureLandscape(this.mGet.getActivity().getResources())) {
                marginParams.addRule(20, 1);
                marginParams.setMarginEnd(0);
                marginParams.bottomMargin = 0;
            } else {
                marginParams.addRule(10, 1);
                marginParams.setMarginEnd(0);
                marginParams.bottomMargin = 0;
            }
        }
        return marginParams;
    }
}
