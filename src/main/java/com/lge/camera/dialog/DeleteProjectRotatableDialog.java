package com.lge.camera.dialog;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class DeleteProjectRotatableDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.DeleteProjectRotatableDialog$1 */
    class C07331 implements OnClickListener {
        C07331() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "ok button click....");
            DeleteProjectRotatableDialog.this.mGet.doOkClickInDeleteConfirmDialog(142);
            DeleteProjectRotatableDialog.this.onDismiss();
        }
    }

    /* renamed from: com.lge.camera.dialog.DeleteProjectRotatableDialog$2 */
    class C07342 implements OnClickListener {
        C07342() {
        }

        public void onClick(View v) {
            DeleteProjectRotatableDialog.this.onDismiss();
        }
    }

    public DeleteProjectRotatableDialog(CamDialogInterface function) {
        super(function);
    }

    public void create() {
        View v = this.mGet.inflateView(C0088R.layout.rotate_delete_dialog);
        setView(v, false, false);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        ((TextView) v.findViewById(16908308)).setText(C0088R.string.delete_project);
        btnOk.setText(C0088R.string.dlg_title_delete);
        btnCancel.setText(C0088R.string.cancel);
        super.create(v, true, false);
        btnOk.setOnClickListener(new C07331());
        btnCancel.setOnClickListener(new C07342());
    }
}
