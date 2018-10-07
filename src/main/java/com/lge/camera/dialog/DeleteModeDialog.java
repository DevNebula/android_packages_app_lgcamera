package com.lge.camera.dialog;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.settings.ModeItem;
import com.lge.camera.util.CamLog;

public class DeleteModeDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.DeleteModeDialog$2 */
    class C07322 implements OnClickListener {
        C07322() {
        }

        public void onClick(View v) {
            DeleteModeDialog.this.onDismiss();
        }
    }

    protected DeleteModeDialog(CamDialogInterface function) {
        super(function);
    }

    public void create(final ModeItem item) {
        View v = this.mGet.inflateView(C0088R.layout.rotate_delete_dialog);
        setView(v, false, false);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        ((TextView) v.findViewById(16908308)).setText(this.mGet.getAppContext().getString(C0088R.string.delete_mode_popup));
        btnOk.setText(C0088R.string.dlg_title_delete);
        btnCancel.setText(C0088R.string.cancel);
        super.create(v, true, false);
        btnOk.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (DeleteModeDialog.this.mGet != null) {
                    CamLog.m3d(CameraConstants.TAG, "[mode] delete button is clicked");
                    DeleteModeDialog.this.mGet.deleteMode(item);
                    DeleteModeDialog.this.onDismiss();
                }
            }
        });
        btnCancel.setOnClickListener(new C07322());
    }
}
