package com.lge.camera.dialog;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.SharedPreferenceUtil;

public class FingerprintInitialRotatableDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.FingerprintInitialRotatableDialog$1 */
    class C07421 implements OnClickListener {
        C07421() {
        }

        public void onClick(View arg0) {
            SharedPreferenceUtil.saveFingerprintShotGuide(FingerprintInitialRotatableDialog.this.mGet.getAppContext(), true);
            FingerprintInitialRotatableDialog.this.mGet.getAppContext().startActivity(new Intent("com.lge.settings.SHORTCUT_SETTINGS"));
        }
    }

    /* renamed from: com.lge.camera.dialog.FingerprintInitialRotatableDialog$2 */
    class C07432 implements OnClickListener {
        C07432() {
        }

        public void onClick(View arg0) {
            SharedPreferenceUtil.saveFingerprintShotGuide(FingerprintInitialRotatableDialog.this.mGet.getAppContext(), true);
            FingerprintInitialRotatableDialog.this.onDismiss();
        }
    }

    public interface FingerprintSettingInterface {
        void onSettingBtnClicked();
    }

    public FingerprintInitialRotatableDialog(CamDialogInterface function) {
        super(function);
    }

    protected void setView(View dialogView, boolean useCheckBox, boolean isHelpDialog) {
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
            messageText.setTextAppearance(C0088R.style.type_d03_dp);
            messageText.setPadding(0, 0, 0, 0);
            scrView.addView(messageText);
        }
    }

    public void create(boolean showAnim) {
        View v = this.mGet.inflateView(C0088R.layout.rotate_dialog);
        setView(v, false, false);
        TextView titleText = (TextView) v.findViewById(C0088R.id.title_text);
        TextView messageText = (TextView) v.findViewById(16908308);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        setFocus(btnCancel, btnOk, false);
        titleText.setText(C0088R.string.fp_dialog_title);
        if (ModelProperties.isSidePowerKeyModel(this.mGet.getAppContext())) {
            messageText.setText(C0088R.string.fp_dialog_side_power_key_text);
        } else {
            messageText.setText(C0088R.string.fp_dialog_text1);
        }
        btnOk.setText(C0088R.string.fp_dialog_setting);
        btnOk.setTextAppearance(C0088R.style.type_capital_bold);
        btnCancel.setText(C0088R.string.fp_dialog_cancel);
        btnCancel.setTextAppearance(C0088R.style.type_capital_bold);
        super.create(v, false, false, showAnim);
        btnOk.setOnClickListener(new C07421());
        btnCancel.setOnClickListener(new C07432());
    }
}
