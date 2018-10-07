package com.lge.camera.dialog;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class HifiRotatableDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.HifiRotatableDialog$1 */
    class C07491 implements OnClickListener {
        C07491() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "ok button click....");
            HifiRotatableDialog.this.onDismiss();
        }
    }

    public HifiRotatableDialog(CamDialogInterface function) {
        super(function);
    }

    public void create() {
        View v = this.mGet.inflateView(C0088R.layout.rotate_dialog);
        setView(v, false, false);
        TextView messageText = (TextView) v.findViewById(16908308);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        ((TextView) v.findViewById(C0088R.id.title_text)).setText(C0088R.string.hifi);
        messageText.setText(C0088R.string.hifi_dialog_text1);
        btnOk.setText(C0088R.string.sp_ok_NORMAL);
        btnCancel.setVisibility(8);
        messageText.setPaddingRelative(0, 0, 0, 0);
        super.create(v, true, false);
        btnOk.setOnClickListener(new C07491());
    }
}
