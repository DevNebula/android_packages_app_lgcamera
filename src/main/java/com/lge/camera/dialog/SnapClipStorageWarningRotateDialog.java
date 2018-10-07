package com.lge.camera.dialog;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class SnapClipStorageWarningRotateDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.SnapClipStorageWarningRotateDialog$1 */
    class C07681 implements OnClickListener {
        C07681() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "ok button click.");
            SnapClipStorageWarningRotateDialog.this.onDismiss();
        }
    }

    public SnapClipStorageWarningRotateDialog(CamDialogInterface function) {
        super(function);
    }

    public void create() {
        CamLog.m3d(CameraConstants.TAG, "SnapClipStorageWarningRotateDialog");
        View v = this.mGet.inflateView(C0088R.layout.rotate_dialog);
        setView(v, true, false);
        TextView messageText = (TextView) v.findViewById(16908308);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        ((TextView) v.findViewById(C0088R.id.title_text)).setText(C0088R.string.sp_storage_full_popup_ics_title_NORMAL);
        messageText.setText(C0088R.string.snap_dialog_clip_storage_warning);
        messageText.setPaddingRelative(0, 0, 0, 0);
        btnOk.setText(C0088R.string.sp_ok_NORMAL);
        btnCancel.setVisibility(8);
        setPositiveButtonListener(btnOk, true);
        super.create(v);
    }

    protected View inflateHelpDialogView() {
        return this.mGet.inflateView(C0088R.layout.rotate_help_dialog);
    }

    protected void setExtraContents(View rotateHelpView) {
    }

    protected void setPositiveButtonListener(Button btnOk, boolean useCheckBox) {
        if (btnOk != null) {
            btnOk.setOnClickListener(new C07681());
        }
    }

    protected void doBackCoverTouch() {
        onDismiss();
    }
}
