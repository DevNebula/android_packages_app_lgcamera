package com.lge.camera.dialog;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.lge.camera.C0088R;

public class DeleteSampleRotatableDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.DeleteSampleRotatableDialog$1 */
    class C07371 implements OnClickListener {
        C07371() {
        }

        public void onClick(View v) {
            DeleteSampleRotatableDialog.this.onDismiss();
        }
    }

    public DeleteSampleRotatableDialog(CamDialogInterface function) {
        super(function);
    }

    public void create() {
        View v = this.mGet.inflateView(C0088R.layout.rotate_delete_dialog);
        setView(v, true, false);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        ((TextView) v.findViewById(16908308)).setText(C0088R.string.delete_original_sample);
        btnOk.setText(C0088R.string.sp_ok_NORMAL);
        btnCancel.setVisibility(8);
        setOneButtonLayout(v, true);
        create(v);
        btnOk.setOnClickListener(new C07371());
    }
}
