package com.lge.camera.dialog;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class SnapSaveAndEditRotatableDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.SnapSaveAndEditRotatableDialog$1 */
    class C07731 implements OnClickListener {
        C07731() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "ok button click....");
            ((CamDialogInterfaceSnap) SnapSaveAndEditRotatableDialog.this.mGet).doYesOnEditDialog();
            SnapSaveAndEditRotatableDialog.this.onDismiss();
        }
    }

    /* renamed from: com.lge.camera.dialog.SnapSaveAndEditRotatableDialog$2 */
    class C07742 implements OnClickListener {
        C07742() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "cancel button click....");
            SnapSaveAndEditRotatableDialog.this.onDismiss();
        }
    }

    public SnapSaveAndEditRotatableDialog(CamDialogInterface function) {
        super(function);
    }

    public void create() {
        View v = this.mGet.inflateView(C0088R.layout.rotate_dialog);
        setView(v, true, false);
        TextView messageText = (TextView) v.findViewById(16908308);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        ((TextView) v.findViewById(C0088R.id.title_text)).setText(C0088R.string.sp_note_dialog_title_NORMAL);
        messageText.setText(C0088R.string.snap_completed_and_edit);
        messageText.setPaddingRelative(0, 0, 0, 0);
        btnOk.setText(C0088R.string.yes);
        btnCancel.setText(C0088R.string.no);
        super.create(v, true, false);
        btnOk.setOnClickListener(new C07731());
        btnCancel.setOnClickListener(new C07742());
    }

    protected void doBackCoverTouch() {
        this.mGet.doCancelClickInDeleteConfirmDialog();
        onDismiss();
    }
}
