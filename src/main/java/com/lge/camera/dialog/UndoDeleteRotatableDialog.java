package com.lge.camera.dialog;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class UndoDeleteRotatableDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.UndoDeleteRotatableDialog$1 */
    class C07861 implements OnClickListener {
        C07861() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "ok button click....");
            UndoDeleteRotatableDialog.this.onDismiss();
            UndoDeleteRotatableDialog.this.mGet.doDeleteOnUndoDialog();
        }
    }

    /* renamed from: com.lge.camera.dialog.UndoDeleteRotatableDialog$2 */
    class C07872 implements OnClickListener {
        C07872() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "cancel button click....");
            UndoDeleteRotatableDialog.this.onDismiss();
        }
    }

    public UndoDeleteRotatableDialog(CamDialogInterface function) {
        super(function);
    }

    public void create() {
        View v = this.mGet.inflateView(C0088R.layout.rotate_delete_dialog);
        setView(v, false, false);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        ((TextView) v.findViewById(16908308)).setText(C0088R.string.sp_ask_delete_burstshot_image_NORMAL);
        btnOk.setText(C0088R.string.dlg_title_delete);
        btnCancel.setText(C0088R.string.cancel);
        super.create(v, true, false);
        btnOk.setOnClickListener(new C07861());
        btnCancel.setOnClickListener(new C07872());
    }

    public boolean onDismiss() {
        return super.onDismiss();
    }

    protected void doBackCoverTouch() {
        this.mGet.doCancelClickInDeleteConfirmDialog();
        onDismiss();
    }
}
