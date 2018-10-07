package com.lge.camera.dialog;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.LdbUtil;

public class SnapDeleteRotatableDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.SnapDeleteRotatableDialog$1 */
    class C07691 implements OnClickListener {
        C07691() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "ok button click....");
            ((CamDialogInterfaceSnap) SnapDeleteRotatableDialog.this.mGet).doYesOnInitDialog();
            LdbUtil.sendLDBIntent(SnapDeleteRotatableDialog.this.mGet.getAppContext(), LdbConstants.LDB_FEATURE_NAME_SNAP_MOVIE, -1, LdbConstants.LDB_RESET);
            SnapDeleteRotatableDialog.this.onDismiss();
        }
    }

    /* renamed from: com.lge.camera.dialog.SnapDeleteRotatableDialog$2 */
    class C07702 implements OnClickListener {
        C07702() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "cancel button click....");
            SnapDeleteRotatableDialog.this.onDismiss();
        }
    }

    public SnapDeleteRotatableDialog(CamDialogInterface function) {
        super(function);
    }

    public void create() {
        View v = this.mGet.inflateView(C0088R.layout.rotate_delete_dialog);
        setView(v, false, false);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        ((TextView) v.findViewById(16908308)).setText(C0088R.string.snap_reset_all);
        btnOk.setText(C0088R.string.reset);
        btnCancel.setText(C0088R.string.cancel);
        super.create(v, true, false);
        btnOk.setOnClickListener(new C07691());
        btnCancel.setOnClickListener(new C07702());
    }

    protected void doBackCoverTouch() {
        this.mGet.doCancelClickInDeleteConfirmDialog();
        onDismiss();
    }
}
