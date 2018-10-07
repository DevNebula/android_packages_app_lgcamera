package com.lge.camera.dialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SharedPreferenceUtilBase;

public class GraphyInstallDialog extends RotateDialog {
    private boolean mCheckBoxActivated = false;

    /* renamed from: com.lge.camera.dialog.GraphyInstallDialog$1 */
    class C07461 implements OnClickListener {
        C07461() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "install button click....");
            GraphyInstallDialog.this.saveGraphyDoNotShowSaveNote();
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setData(Uri.parse("market://details?id=com.lge.graphy.mobile"));
            GraphyInstallDialog.this.mGet.getActivity().startActivity(intent);
            GraphyInstallDialog.this.onDismiss();
        }
    }

    /* renamed from: com.lge.camera.dialog.GraphyInstallDialog$2 */
    class C07472 implements OnClickListener {
        C07472() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "cancel button click....");
            GraphyInstallDialog.this.saveGraphyDoNotShowSaveNote();
            GraphyInstallDialog.this.onDismiss();
        }
    }

    public GraphyInstallDialog(CamDialogInterface function) {
        super(function);
    }

    public void create(boolean showAnim) {
        this.mCheckBoxActivated = showAnim;
        View v = this.mGet.inflateView(C0088R.layout.rotate_dialog);
        setView(v, this.mCheckBoxActivated, false);
        TextView titleText = (TextView) v.findViewById(C0088R.id.title_text);
        TextView messageText = (TextView) v.findViewById(16908308);
        Button btnInstall = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        setFocus(btnInstall, btnCancel, true);
        titleText.setText(C0088R.string.graphy_label_install_guide_title);
        messageText.setText(C0088R.string.graphy_label_install_guide);
        messageText.setPaddingRelative(0, 0, 0, 0);
        btnInstall.setText(C0088R.string.download);
        if (this.mCheckBoxActivated) {
            btnCancel.setText(C0088R.string.later_button);
        } else {
            btnCancel.setText(C0088R.string.cancel);
        }
        this.mDoNotShowAgain = this.mCheckBoxActivated;
        setCheckBox(v, this.mCheckBoxActivated);
        super.create(v, false, false, showAnim);
        btnInstall.setOnClickListener(new C07461());
        btnCancel.setOnClickListener(new C07472());
    }

    private void saveGraphyDoNotShowSaveNote() {
        if (this.mCheckBoxActivated) {
            CheckBox userCheck = (CheckBox) this.mGet.findViewById(16908289);
            SharedPreferences pref = this.mGet.getActivity().getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0);
            if (userCheck != null && pref != null) {
                Editor edit = pref.edit();
                edit.putBoolean(CameraConstantsEx.GRAPHY_DO_NOT_SHOW_SAVE_NOTE, userCheck.isChecked());
                edit.apply();
            }
        }
    }
}
