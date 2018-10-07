package com.lge.camera.dialog;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class DeleteRotatableDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.DeleteRotatableDialog$2 */
    class C07362 implements OnClickListener {
        C07362() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "cancel button click....");
            DeleteRotatableDialog.this.mGet.doCancelClickInDeleteConfirmDialog();
            DeleteRotatableDialog.this.onDismiss();
        }
    }

    public DeleteRotatableDialog(CamDialogInterface function) {
        super(function);
    }

    public void create(int stringId, final int id) {
        View v = this.mGet.inflateView(C0088R.layout.rotate_delete_dialog);
        setView(v, false, false);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        ((TextView) v.findViewById(16908308)).setText(stringId);
        btnOk.setText(C0088R.string.dlg_title_delete);
        btnCancel.setText(C0088R.string.cancel);
        super.create(v, true, false);
        btnOk.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CamLog.m3d(CameraConstants.TAG, "ok button click....");
                DeleteRotatableDialog.this.mGet.doOkClickInDeleteConfirmDialog(id);
                DeleteRotatableDialog.this.onDismiss();
            }
        });
        btnCancel.setOnClickListener(new C07362());
    }

    protected void doBackCoverTouch() {
        this.mGet.doCancelClickInDeleteConfirmDialog();
        onDismiss();
    }
}
