package com.lge.camera.dialog;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SharedPreferenceUtilBase;

public class SnapSaveNoteRotateDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.SnapSaveNoteRotateDialog$1 */
    class C07751 implements OnClickListener {
        C07751() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "ok button click.");
            CheckBox userCheck = (CheckBox) SnapSaveNoteRotateDialog.this.mGet.findViewById(16908289);
            SharedPreferences pref = SnapSaveNoteRotateDialog.this.mGet.getActivity().getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0);
            if (!(userCheck == null || pref == null)) {
                Editor edit = pref.edit();
                edit.putBoolean(CameraConstants.SNAP_DO_NOT_SHOW_SAVE_NOTE, userCheck.isChecked());
                edit.apply();
            }
            SnapSaveNoteRotateDialog.this.onDismiss();
        }
    }

    public SnapSaveNoteRotateDialog(CamDialogInterface function) {
        super(function);
    }

    public void create() {
        CamLog.m3d(CameraConstants.TAG, "SnapSaveNoteRotateDialog");
        View v = this.mGet.inflateView(C0088R.layout.rotate_dialog);
        setView(v, true, false);
        TextView messageText = (TextView) v.findViewById(16908308);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        ((TextView) v.findViewById(C0088R.id.title_text)).setText(C0088R.string.sp_note_dialog_title_NORMAL);
        messageText.setText(C0088R.string.snap_dialog_save_note);
        messageText.setPaddingRelative(0, 0, 0, 0);
        btnOk.setText(C0088R.string.sp_ok_NORMAL);
        btnCancel.setVisibility(8);
        this.mDoNotShowAgain = true;
        setCheckBox(v, true);
        setPositiveButtonListener(btnOk, true);
        create(v);
    }

    protected void create(View v) {
        create(v, true, false);
    }

    protected View inflateHelpDialogView() {
        return this.mGet.inflateView(C0088R.layout.rotate_help_dialog);
    }

    protected void setExtraContents(View rotateHelpView) {
    }

    protected void setPositiveButtonListener(Button btnOk, boolean useCheckBox) {
        if (btnOk != null) {
            btnOk.setOnClickListener(new C07751());
        }
    }

    protected void doBackCoverTouch() {
        onDismiss();
    }
}
